package me.gastaldi.forge.reflections;

import java.util.Arrays;
import java.util.Set;

import javax.faces.FacesException;
import javax.inject.Inject;
import javax.persistence.Entity;
import javax.persistence.GenerationType;

import me.gastaldi.forge.reflections.facet.ReflectionsFacet;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.javaee.facets.JavaEE7Facet;
import org.jboss.forge.addon.javaee.jpa.PersistenceOperations;
import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.parser.java.resources.JavaResource;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFacet;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.reflections.Reflections;

@RunWith(Arquillian.class)
public class ReflectionsFacetTest
{
   @Deployment
   @Dependencies({
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
            @AddonDependency(name = "org.jboss.forge.addon:maven"),
            @AddonDependency(name = "org.jboss.forge.addon:projects"),
            @AddonDependency(name = "org.jboss.forge.addon:javaee"),
            @AddonDependency(name = "me.gastaldi.forge:reflections") })
   public static ForgeArchive getDeployment()
   {
      ForgeArchive archive = ShrinkWrap
               .create(ForgeArchive.class)
               .addBeansXML()
               .addAsAddonDependencies(
                        AddonDependencyEntry
                                 .create("org.jboss.forge.furnace.container:cdi"),
                        AddonDependencyEntry
                                 .create("org.jboss.forge.addon:projects"),
                        AddonDependencyEntry
                                 .create("org.jboss.forge.addon:javaee"),
                        AddonDependencyEntry
                                 .create("me.gastaldi.forge:reflections"));
      return archive;
   }

   @Inject
   private ProjectFactory projectFactory;

   @Inject
   private FacetFactory facetFactory;

   @Inject
   private PersistenceOperations persistenceOperations;

   private Project project;

   @Before
   public void setUp()
   {
      project = projectFactory.createTempProject(Arrays.<Class<? extends ProjectFacet>> asList(JavaSourceFacet.class));
   }

   @Test
   public void testReflections() throws Exception
   {
      facetFactory.install(project, JavaEE7Facet.class);
      Assert.assertTrue(project.hasFacet(ReflectionsFacet.class));
      ReflectionsFacet facet = project.getFacet(ReflectionsFacet.class);
      Reflections reflections = facet.getReflections();
      Set<Class<? extends FacesException>> subTypes = reflections.getSubTypesOf(FacesException.class);
      Assert.assertEquals(13, subTypes.size());
   }

   @Test
   public void testEntityInProject() throws Exception
   {
      facetFactory.install(project, JavaEE7Facet.class);
      String packageName = project.getFacet(JavaSourceFacet.class).getBasePackage() + ".model";
      JavaResource newEntity = persistenceOperations.newEntity(project, "Customer", packageName, GenerationType.AUTO);
      ReflectionsFacet facet = project.getFacet(ReflectionsFacet.class);
      Reflections reflections = facet.getReflections();
      Set<Class<?>> entities = reflections.getTypesAnnotatedWith(Entity.class);
      Assert.assertEquals(1, entities.size());
      Assert.assertEquals(newEntity.getJavaType().getQualifiedName(), entities.iterator().next().getName());
   }

}