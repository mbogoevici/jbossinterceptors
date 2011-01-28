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

package org.jboss.interceptor.metadata;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.jboss.interceptor.exceptions.InterceptorException;
import org.jboss.interceptor.util.ReflectionUtils;


/**
 * A method's reference: includes the declaring class name and the method signature
 *
 * @author <a href="mailto:mariusb@redhat.com">Marius Bogoevici</a>
 */
public class MethodReference implements Serializable
{
   private String declaringClassName;

   private MethodSignature methodSignature;

   public static MethodReference of(Method method)
   {
      return new MethodReference(method.getDeclaringClass().getName(), MethodSignature.of(method));
   }

   public MethodReference(String declaringClassName, MethodSignature methodSignature)
   {
      this.declaringClassName = declaringClassName;
      this.methodSignature = methodSignature;
   }

   public String getDeclaringClassName()
   {
      return declaringClassName;
   }

   public MethodSignature getMethodSignature()
   {
      return methodSignature;
   }

   @Override
   public boolean equals(Object o)
   {
      if (this == o)
      {
         return true;
      }
      if (o == null || getClass() != o.getClass())
      {
         return false;
      }

      MethodReference that = (MethodReference) o;

      if (declaringClassName != null ? !declaringClassName.equals(that.declaringClassName) : that.declaringClassName != null)
      {
         return false;
      }
      if (methodSignature != null ? !methodSignature.equals(that.methodSignature) : that.methodSignature != null)
      {
         return false;
      }

      return true;
   }

   @Override
   public int hashCode()
   {
      int result = declaringClassName != null ? declaringClassName.hashCode() : 0;
      result = 31 * result + (methodSignature != null ? methodSignature.hashCode() : 0);
      return result;
   }

}
