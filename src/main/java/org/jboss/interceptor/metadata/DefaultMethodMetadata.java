/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual
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

package org.jboss.interceptor.metadata;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.interceptor.model.InterceptionType;
import org.jboss.interceptor.reader.AnnotatedMethodReader;
import org.jboss.interceptor.reader.ReflectiveAnnotatedMethodReader;
import org.jboss.interceptor.util.InterceptionTypeRegistry;

/**
 * Represents information about an interceptor method
 *
 * @author Marius Bogoevici
 */
public class DefaultMethodMetadata<M> implements MethodMetadata, Serializable
{
   
   private static final long serialVersionUID = -4538617003189564552L;
   
   private Set<InterceptionType> supportedInterceptorTypes = new HashSet<InterceptionType>();
   private String[] parameterTypeNames;
   private String name;
   private String methodName;
   private String declaringClassName;
   private boolean methodPrivate;

   private DefaultMethodMetadata(M methodReference, AnnotatedMethodReader<M> annotationReader)
   {
      Method javaMethod = annotationReader.getJavaMethod(methodReference);

      for (InterceptionType interceptionType: InterceptionTypeRegistry.getSupportedInterceptionTypes())
      {
         if (annotationReader.getAnnotation(InterceptionTypeRegistry.getAnnotationClass(interceptionType), methodReference) != null)
         {
            supportedInterceptorTypes.add(interceptionType);
         }
      }

      Class<?>[] parameterTypes = javaMethod.getParameterTypes();
      List<String> parameterTypeNames = new ArrayList<String>();
      for (Class<?> parameterType : parameterTypes)
      {
         parameterTypeNames.add(parameterType.getName());
      }
      this.parameterTypeNames = parameterTypeNames.toArray(new String[parameterTypeNames.size()]);

      name = javaMethod.getReturnType().getName();
      methodName = javaMethod.getName();
      declaringClassName = javaMethod.getDeclaringClass().getName();
      methodPrivate = Modifier.isPrivate(javaMethod.getModifiers());
   }

   public static <M> MethodMetadata of(M methodReference, AnnotatedMethodReader<M> methodReader)
   {
      return new DefaultMethodMetadata(methodReference, methodReader);
   }

   public static MethodMetadata of(Method method)
   {
      return new DefaultMethodMetadata(method, new ReflectiveAnnotatedMethodReader());
   }


   public Set<InterceptionType> getSupportedInterceptionTypes()
   {
      return supportedInterceptorTypes;
   }

   public String getName()
   {
      return methodName;
   }

   public String[] getParameterTypeNames()
   {
      return parameterTypeNames;
   }

   public String getDeclaringClassName()
   {
      return declaringClassName;
   }

   public boolean isPrivate()
   {
      return methodPrivate;
   }

   public String getReturnTypeName()
   {
      return name;
   }



}
