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

import java.io.Serializable;
import java.lang.reflect.Method;

import org.jboss.interceptor.builder.MethodReference;
import org.jboss.interceptor.spi.metadata.MethodMetadata;
import org.jboss.interceptor.spi.model.InterceptionType;
import org.jboss.interceptor.spi.model.InterceptionTypeRegistry;

/**
 * Represents information about an interceptor method
 *
 * @author Marius Bogoevici
 */
public class ReflectiveMethodMetadata extends AbstractMethodMetadata implements Serializable
{

   private static final long serialVersionUID = -4538617003189564552L;

   private transient Method javaMethod;

   private MethodReference methodReference;

   private ReflectiveMethodMetadata(Method javaMethod)
   {
      this.javaMethod = javaMethod;
      this.methodReference = MethodReference.of(javaMethod);
      initSupportedInterceptionTypes();
   }

   @Override
   protected boolean interceptionAnnotationExists(InterceptionType interceptionType)
   {
      return this.javaMethod.getAnnotation(InterceptionTypeRegistry.getAnnotationClass(interceptionType)) != null;
   }

   public static MethodMetadata of(Method method)
   {
      return new ReflectiveMethodMetadata(method);
   }


   @Override
   public Method getJavaMethod()
   {
      return javaMethod;
   }

   @Override
   public MethodReference getMethodReference()
   {
      return methodReference;
   }

}
