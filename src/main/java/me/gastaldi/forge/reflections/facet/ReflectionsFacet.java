package me.gastaldi.forge.reflections.facet;

import org.jboss.forge.addon.projects.ProjectFacet;
import org.reflections.Reflections;

/**
 * Returns a Reflections object based on the project dependencies
 * 
 * @author George Gastaldi
 */
public interface ReflectionsFacet extends ProjectFacet
{
   /**
    * The {@link Reflections} object based on the project dependencies
    */
   Reflections getReflections();
}
