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

package org.jboss.interceptor.metadata;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simplified representation of a method signature, that can be used as a key instead of the
 * {@link Method} itself. Argument types are represented by {@link String values}
 *
 * @author Marius Bogoevici
 */
public class MethodSignature implements Serializable
{
   private String methodName;

   private String[] argTypeNames;

   public MethodSignature(String methodName)
   {
     this(methodName, new String[]{});
   }

   public MethodSignature(String methodName, String... argTypeNames)
   {
      this.methodName = methodName;
      this.argTypeNames = argTypeNames;
   }

   public MethodSignature(String methodName, Class<?>... argTypeNames)
   {
      this.methodName = methodName;
      this.argTypeNames = toNames(argTypeNames);
   }

   public static MethodSignature of(Method m)
   {
      String methodName = m.getName();
      Class<?>[] parameterTypes = m.getParameterTypes();
      String[] argTypeNames = toNames(parameterTypes);
      return new MethodSignature(methodName, argTypeNames);
   }

   private static String[] toNames(Class<?>... parameterTypes)
   {
      List<String> argTypeNamesList = new ArrayList<String>();
      for (Class<?> parameterType : parameterTypes)
      {
         argTypeNamesList.add(parameterType.getName());
      }
      return argTypeNamesList.toArray(new String[argTypeNamesList.size()]);
   }

   public String getMethodName()
   {
      return methodName;
   }

   public String[] getArgumentTypeNames()
   {
      return argTypeNames;
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

      MethodSignature that = (MethodSignature) o;

      if (!Arrays.equals(argTypeNames, that.argTypeNames))
      {
         return false;
      }
      if (methodName != null ? !methodName.equals(that.methodName) : that.methodName != null)
      {
         return false;
      }

      return true;
   }

   @Override
   public int hashCode()
   {
      int result = methodName != null ? methodName.hashCode() : 0;
      result = 31 * result + (argTypeNames != null ? Arrays.hashCode(argTypeNames) : 0);
      return result;
   }
}
