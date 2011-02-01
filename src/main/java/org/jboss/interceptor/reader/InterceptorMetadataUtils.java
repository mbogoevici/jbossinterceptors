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

package org.jboss.interceptor.reader;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.interceptor.InvocationContext;

import org.jboss.interceptor.builder.MethodSignature;
import org.jboss.interceptor.spi.metadata.ClassMetadata;
import org.jboss.interceptor.spi.metadata.InterceptorMetadata;
import org.jboss.interceptor.spi.metadata.InterceptorReference;
import org.jboss.interceptor.spi.metadata.MethodMetadata;
import org.jboss.interceptor.spi.model.InterceptionType;
import org.jboss.interceptor.spi.model.InterceptionTypeRegistry;
import org.jboss.interceptor.util.InterceptorMetadataException;
import org.jboss.interceptor.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marius Bogoevici
 */
public class InterceptorMetadataUtils
{
   protected static final String OBJECT_CLASS_NAME = Object.class.getName();

   private static final Logger LOG = LoggerFactory.getLogger(InterceptorMetadataUtils.class);


   public static InterceptorMetadata readMetadataForInterceptorClass(InterceptorReference<?> interceptorReference)
   {
      return new SimpleInterceptorMetadata(interceptorReference, false, buildInterceptorMethodMap(interceptorReference.getClassMetadata(), false));
   }

   public static InterceptorMetadata readMetadataForTargetClass(ClassMetadata<?> classMetadata)
   {
      return new SimpleInterceptorMetadata(ClassMetadataInterceptorReference.of(classMetadata), true, buildInterceptorMethodMap(classMetadata, true));
   }

   public static boolean validateInterceptorMethod(InterceptionType interceptionType, MethodMetadata method, boolean forTargetClass)
   {

      // is the method annotated with an interceptor annotation?
      if (!method.getSupportedInterceptionTypes().contains(interceptionType))
      {
         return false;
      }

      int parameterCount = method.getMethodReference().getMethodSignature().getArgumentTypeNames().length;

      // the method is annotated correctly, but is its signature valid?
      if (interceptionType.isLifecycleCallback())
      {
         if (!isReturningVoid(method))
         {
            throw new InterceptorMethodSignatureException(getErrorMessage(interceptionType, method.getJavaMethod()) + "does not have a void return type");
         }


         if (forTargetClass && parameterCount != 0)
         {
            throw new InterceptorMethodSignatureException(getErrorMessage(interceptionType, method.getJavaMethod()) + "is defined on the target class and does not have 0 arguments");
         }

         if (!forTargetClass && parameterCount != 1)
         {
            throw new InterceptorMethodSignatureException(getErrorMessage(interceptionType, method.getJavaMethod()) + "does not have exactly one parameter");
         }

         if (!forTargetClass && !hasSingleInvocationContextArgument(method))
         {
            throw new InterceptorMethodSignatureException(getErrorMessage(interceptionType, method.getJavaMethod()) + "its single argument is not a " + InvocationContext.class.getName());
         }
      }
      else
      {
         if (!isReturningObject(method))
         {
            throw new InterceptorMethodSignatureException(getErrorMessage(interceptionType, method.getJavaMethod()) + "does not return a " + OBJECT_CLASS_NAME);
         }

         String[] parameterTypes = method.getMethodReference().getMethodSignature().getArgumentTypeNames();

         if (parameterTypes.length != 1)
         {
            throw new InterceptorMethodSignatureException(getErrorMessage(interceptionType, method.getJavaMethod()) + "does not have exactly 1 parameter");
         }

         if (!hasSingleInvocationContextArgument(method))
         {
            throw new InterceptorMethodSignatureException(getErrorMessage(interceptionType, method.getJavaMethod()) + "does not have a " + InvocationContext.class.getName() + " parameter ");
         }
      }
      return true;
   }

   private static boolean isReturningObject(MethodMetadata method)
   {
      if (!method.isDeferringReflection())
      {
         return Object.class.equals(method.getJavaMethod().getReturnType());
      }
      else
      {
         return OBJECT_CLASS_NAME.equals(method.getMethodReference().getReturnTypeName());
      }
   }

    private static boolean isReturningVoid(MethodMetadata method)
   {
      if (!method.isDeferringReflection())
      {
         return void.class.equals(method.getJavaMethod().getReturnType());
      }
      else
      {
         return "void".equals(method.getMethodReference().getReturnTypeName());
      }
   }

   private static boolean hasSingleInvocationContextArgument(MethodMetadata methodMetadata)
   {
      // if the reflective information is not available on the method metadata object, we check the argument type by name
      // otherwise we allow for subclasses of InvocationContext to be present as well
      if (methodMetadata.isDeferringReflection())
      {
         return methodMetadata.getMethodReference().getMethodSignature().getArgumentTypeNames().length == 1
               && methodMetadata.getMethodReference().getMethodSignature().getArgumentTypeNames()[0].equals(InvocationContext.class.getName());
      }
      else
      {
         return methodMetadata.getJavaMethod().getParameterTypes().length == 1
               && InvocationContext.class.isAssignableFrom(methodMetadata.getJavaMethod().getParameterTypes()[0]);
      }
   }

   static String getErrorMessage(InterceptionType interceptionType, Method method)
   {
      return "Method " + method.getName() + " defined on class " + method.getDeclaringClass().getName()
            + " will not be used for interception, since it is not defined according to the specification. It is annotated with @"
            + interceptionType.annotationClassName() + ", but ";
   }

   static Map<InterceptionType, List<MethodMetadata>> buildInterceptorMethodMap(ClassMetadata<?> interceptorClass, boolean forTargetClass)
   {
      Map<InterceptionType, List<MethodMetadata>> methodMap = new HashMap<InterceptionType, List<MethodMetadata>>();
      ClassMetadata<?> currentClass = interceptorClass;
      Set<MethodSignature> foundMethods = new HashSet<MethodSignature>();
      do
      {
         Set<InterceptionType> detectedInterceptorTypes = new HashSet<InterceptionType>();

         for (MethodMetadata method : currentClass.getDeclaredMethods())
         {
            MethodSignature methodReference = MethodSignature.of(method.getJavaMethod());
            // ignore a method if a signature has been found in a subclass already - it means that it is overridden
            // however, if the method is private, don't ignore it - private methods are never overridden
            if (!foundMethods.contains(methodReference) || Modifier.isPrivate(method.getJavaMethod().getModifiers()))
            {
               for (InterceptionType interceptionType : InterceptionTypeRegistry.getSupportedInterceptionTypes())
               {
                  if (validateInterceptorMethod(interceptionType, method, forTargetClass))
                  {
                     if (methodMap.get(interceptionType) == null)
                     {
                        methodMap.put(interceptionType, new LinkedList<MethodMetadata>());
                     }
                     if (detectedInterceptorTypes.contains(interceptionType))
                     {
                        throw new InterceptorMetadataException("Same interception type cannot be specified twice on the same class");
                     }
                     else
                     {
                        detectedInterceptorTypes.add(interceptionType);
                     }
                     // add method in the list - if it is there already, it means that it has been added by a subclass
                     // final methods are treated separately, as a final method cannot override another method nor be
                     // overridden
                     ReflectionUtils.ensureAccessible(method.getJavaMethod());
                     if (!foundMethods.contains(methodReference) && Modifier.isPrivate(method.getJavaMethod().getModifiers()))
                     {
                        ;
                     }
                     {
                        methodMap.get(interceptionType).add(0, method);
                     }
                  }
               }
               // the method reference must be added anyway - overridden methods are not taken into consideration
               foundMethods.add(methodReference);
            }
         }
         currentClass = currentClass.getSuperclass();
      }
      while (currentClass != null);
      return methodMap;
   }
}
