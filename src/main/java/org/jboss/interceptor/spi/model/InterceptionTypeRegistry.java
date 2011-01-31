/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.interceptor.spi.model;


import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jboss.interceptor.proxy.InterceptorException;
import org.jboss.interceptor.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helper class for converting between interception types and annotations, as well as for determining
 * the availability of the various interception types (i.e. interceptor annotations) at runtime on the classpath.
 *
 * @author Marius Bogoevici
 */
public final class InterceptionTypeRegistry
{

   private static final Logger LOG = LoggerFactory.getLogger(InterceptionTypeRegistry.class);
   private static Map<InterceptionType, Class<? extends Annotation>> foundInterceptionAnnotationClasses;
   private static Map<String, InterceptionType> interceptionTypesByAnnotation;

   static
   {
      foundInterceptionAnnotationClasses = new HashMap<InterceptionType, Class<? extends Annotation>>();
      interceptionTypesByAnnotation = new HashMap<String, InterceptionType>();

      for (InterceptionType interceptionType : InterceptionType.values())
      {
         interceptionTypesByAnnotation.put(interceptionType.annotationClassName(), interceptionType);
         try
         {
            foundInterceptionAnnotationClasses.put(interceptionType, (Class<? extends Annotation>) ReflectionUtils.classForName(interceptionType.annotationClassName()));
         }
         catch (Exception e)
         {
            LOG.warn("Class '" + interceptionType.annotationClassName() + "' not found, interception based on it is not enabled");
         }
      }

   }

   /**
    * Returns the interception types for which the corresponding annotations have been found on the classpath
    *
    * @return the interception types found on the classpath
    */
   public static Collection<InterceptionType> getSupportedInterceptionTypes()
   {
      return foundInterceptionAnnotationClasses.keySet();
   }

   /**
    * Determines whether a certain interception type is currently supported (i.e. its corresponding annotation is
    * available on the classpath). Useful when scanning interception types for annotation methods.
    *
    * @param interceptionType
    * @return
    */
   public static boolean isSupported(InterceptionType interceptionType)
   {
      return foundInterceptionAnnotationClasses.containsKey(interceptionType);
   }

   /**
    * Converts an interception type to its corresponding annotation.
    *
    * @param interceptionType - the interception type
    * @return the annotation class that corresponds to a given interception type or null if the annotation does not
    *         exist on the classpath
    */
   public static Class<? extends Annotation> getAnnotationClass(InterceptionType interceptionType)
   {
      return foundInterceptionAnnotationClasses.get(interceptionType);
   }


   /**
    * Convert the name of an {@link Annotation} class to an {@link InterceptionType}.
    *
    * @param annotationClassName - the name of the annotation class.
    * @return the interception type
    * @throws IllegalArgumentException if annotationClassName is not an interception type annotation
    */
   public static InterceptionType forAnnotation(String annotationClassName) throws IllegalArgumentException
   {
      InterceptionType interceptionType = interceptionTypesByAnnotation.get(annotationClassName);

      if (interceptionType == null)
      {
         throw new IllegalArgumentException(annotationClassName + " is not a supported interception type");
      }
      return interceptionType;
   }

   /**
    * Convert a given {@link Annotation} class to an {@link InterceptionType}.
    *
    * @param annotationClass - the annotation class.
    * @return the interception type
    * @throws IllegalArgumentException if annotationClassName is not an interception type annotation
    */
   public static InterceptionType forAnnotation(Class<? extends Annotation> annotationClass) throws IllegalArgumentException
   {
      return forAnnotation(annotationClass.getName());
   }

   /**
    * Convert a given {@link Annotation} instance to an {@link InterceptionType}.
    *
    * @param annotation an annotation instance
    * @return the interception type
    * @throws IllegalArgumentException if the annotation is not an interception type annotation
    */
   public static InterceptionType forAnnotation(Annotation annotation) throws IllegalArgumentException
   {
      return forAnnotation(annotation.annotationType());
   }


   /**
    * Determines whether the given class name is an interceptor type annotation.
    *
    * @param annotationClassName - the name of the annotation class.
    * @return true if the class is an interceptor type annotation
    */
   public static boolean isInterceptionType(String annotationClassName) throws IllegalArgumentException
   {
      return interceptionTypesByAnnotation.containsKey(annotationClassName);
   }

   /**
    * Determines whether the given class is an interceptor type annotation.
    *
    * @param annotationClass - the name of the annotation class.
    * @return true if the class is an interceptor type annotation
    */
   public static boolean isInterceptionType(Class<? extends Annotation> annotationClass) throws IllegalArgumentException
   {
      return isInterceptionType(annotationClass.getName());
   }

   /**
    * Determines whether the given annotation is an interceptor type annotation.
    *
    * @param annotation- the name of the annotation class.
    * @return true if the class is an interceptor type annotation
    */
   public static boolean isInterceptionType(Annotation annotation) throws IllegalArgumentException
   {
      return isInterceptionType(annotation.annotationType());
   }

}
