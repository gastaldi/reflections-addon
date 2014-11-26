package me.gastaldi.forge.reflections.facet;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.jboss.forge.addon.dependencies.Dependency;
import org.jboss.forge.addon.facets.AbstractFacet;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.facets.DependencyFacet;
import org.jboss.forge.addon.projects.facets.PackagingFacet;
import org.jboss.forge.addon.resource.FileResource;
import org.jboss.forge.addon.resource.Resource;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

@FacetConstraint(DependencyFacet.class)
class ReflectionsFacetImpl extends AbstractFacet<Project> implements
         ReflectionsFacet
{
   private static final Logger log = Logger
            .getLogger(ReflectionsFacetImpl.class.getName());

   private Reflections reflections;

   @Override
   public boolean install()
   {
      return isInstalled();
   }

   @Override
   public boolean isInstalled()
   {
      return true;
   }

   @Override
   public Reflections getReflections()
   {
      if (reflections == null)
      {
         initializeReflections();
      }
      return reflections;
   }

   private void initializeReflections()
   {
      Project project = getFaceted();
      DependencyFacet facet = project.getFacet(DependencyFacet.class);
      List<Dependency> effectiveDependencies = facet
               .getEffectiveDependencies();
      List<URL> urls = new ArrayList<>();
      ConfigurationBuilder configuration = new ConfigurationBuilder();
      // Add project dependencies
      for (Dependency dependency : effectiveDependencies)
      {
         FileResource<?> artifact = dependency.getArtifact();
         if (artifact != null)
         {
            File artifactFile = artifact.getUnderlyingResourceObject();
            try
            {
               urls.add(artifactFile.toURI().toURL());
            }
            catch (MalformedURLException e)
            {
               log.warning("Error while configuring Reflections: "
                        + e.getMessage());
            }
         }
      }
      // Add project build
      PackagingFacet packagingFacet = project.getFacet(PackagingFacet.class);
      Resource<?> finalArtifact = packagingFacet.getFinalArtifact();
      if (!finalArtifact.exists())
      {
         // Force build
         finalArtifact = packagingFacet.createBuilder().quiet(true).build();
      }
      if (finalArtifact instanceof FileResource)
      {
         File artifact = ((FileResource<?>) finalArtifact).getUnderlyingResourceObject();
         try
         {
            urls.add(artifact.toURI().toURL());
         }
         catch (MalformedURLException e)
         {
            log.warning("Error while configuring Reflections: "
                     + e.getMessage());
         }
      }
      // Project Classloader. May introduce memory leaks
      URLClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]));
      configuration.setUrls(urls).addClassLoader(classLoader);
      this.reflections = new Reflections(configuration);
   }
}
