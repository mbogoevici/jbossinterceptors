/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual
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

package org.jboss.interceptor.model;

import java.util.HashMap;
import java.util.Map;

import org.jboss.interceptor.exceptions.InterceptorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for converting {@link InterceptionTypes} to and from their respective annotations.
 *
 * @author Marius Bogoevici
 */
public class InterceptionTypes
{
   private static final Logger LOG = LoggerFactory.getLogger(InterceptionTypes.class);

   private static Map<String, InterceptionType> interceptionTypesByAnnotationClassName;

   static
   {
      interceptionTypesByAnnotationClassName = new HashMap<String, InterceptionType>();
      for (InterceptionType interceptionType : InterceptionType.values())
      {
         interceptionTypesByAnnotationClassName.put(interceptionType.annotationClassName(), interceptionType);
      }
   }

   public static InterceptionType forAnnotation(String annotationClassName)
   {
      InterceptionType interceptionType = interceptionTypesByAnnotationClassName.get(annotationClassName);
      if (interceptionType == null)
      {
         throw new InterceptorException(annotationClassName + " is not a supported interception type");
      }
      return interceptionType;
   }

}
