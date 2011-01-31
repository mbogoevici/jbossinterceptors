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

package org.jboss.interceptor.reader;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.interceptor.builder.MethodReference;
import org.jboss.interceptor.proxy.InterceptorException;
import org.jboss.interceptor.spi.metadata.MethodMetadata;
import org.jboss.interceptor.spi.model.InterceptionType;
import org.jboss.interceptor.spi.model.InterceptionTypeRegistry;
import org.jboss.interceptor.util.ReflectionUtils;

/**
 * Represents information about an interceptor method
 *
 * @author Marius Bogoevici
 */
public class DefaultMethodMetadata<M> implements MethodMetadata, Serializable
{
   
   private static final long serialVersionUID = -4538617003189564552L;
   
   private Method javaMethod;

   private Set<InterceptionType> supportedInterceptorTypes = new HashSet<InterceptionType>();

   private DefaultMethodMetadata(M methodReference, AnnotatedMethodReader<M> annotationReader)
   {
      this.javaMethod = annotationReader.getJavaMethod(methodReference);
      for (InterceptionType interceptionType: InterceptionTypeRegistry.getSupportedInterceptionTypes())
      {
         if (annotationReader.getAnnotation(InterceptionTypeRegistry.getAnnotationClass(interceptionType), methodReference) != null)
         {
            supportedInterceptorTypes.add(interceptionType);
         }
      }
   }

   private DefaultMethodMetadata(Set<InterceptionType> interceptionTypes, Method method)
   {
      this.supportedInterceptorTypes = interceptionTypes;
      this.javaMethod = method;
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

   public Method getJavaMethod()
   {
      return javaMethod;
   }

   public Class<?> getReturnType()
   {
      return javaMethod.getReturnType();
   }

   private Object writeReplace()
   {
      return new DefaultMethodMetadataSerializationProxy(supportedInterceptorTypes, MethodReference.of(this));
   }


   private static class DefaultMethodMetadataSerializationProxy implements Serializable
   {
      private Set<InterceptionType> supportedInterceptionTypes;
      private MethodReference methodReference;

      private DefaultMethodMetadataSerializationProxy(Set<InterceptionType> supportedInterceptionTypes, MethodReference methodReference)
      {
         this.supportedInterceptionTypes = supportedInterceptionTypes;
         this.methodReference = methodReference;
      }

      private Object readResolve() throws ObjectStreamException
      {
         ClassLoader classLoader = ReflectionUtils.getThreadContextClassLoader(true);
         Method method = null;
         try
         {
            String[] argumentTypeNames = methodReference.getMethodSignature().getArgumentTypeNames();
            List<Class<?>> argumentTypes = new ArrayList<Class<?>>();
            for (String argumentTypeName : argumentTypeNames)
            {
               argumentTypes.add(classLoader.loadClass(argumentTypeName));
            }
            Class<?>[] argumentTypesArray = argumentTypes.toArray(new Class<?>[argumentTypes.size()]);
            method = classLoader.loadClass(methodReference.getDeclaringClassName())
                        .getDeclaredMethod(methodReference.getMethodSignature().getMethodName(), argumentTypesArray);
         }
         catch (Exception e)
         {
            throw new InterceptorException(e);
         }
         return new DefaultMethodMetadata(supportedInterceptionTypes, method);
      }

   }

}
