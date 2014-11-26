package me.gastaldi.forge.reflections.facet.impl;

import java.net.URLClassLoader;

import me.gastaldi.forge.reflections.facet.ClassLoaderFacet;
import me.gastaldi.forge.reflections.facet.ReflectionsFacet;

import org.jboss.forge.addon.facets.AbstractFacet;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.projects.Project;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

@FacetConstraint(ClassLoaderFacet.class)
class ReflectionsFacetImpl extends AbstractFacet<Project> implements
         ReflectionsFacet
{
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
      ClassLoaderFacet classLoaderFacet = project.getFacet(ClassLoaderFacet.class);
      URLClassLoader classLoader = classLoaderFacet.getClassLoader();
      ConfigurationBuilder configuration = new ConfigurationBuilder();
      configuration.setUrls(classLoader.getURLs()).setClassLoaders(new ClassLoader[] { classLoader });
      this.reflections = new Reflections(configuration);
   }
}
