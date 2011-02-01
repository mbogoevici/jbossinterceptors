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
import java.util.Iterator;

import org.jboss.interceptor.spi.metadata.ClassMetadata;
import org.jboss.interceptor.spi.metadata.MethodMetadata;
import org.jboss.interceptor.util.ArrayIterator;
import org.jboss.interceptor.util.ImmutableIteratorWrapper;

/**
 * @author Marius Bogoevici
 */
public class ReflectiveClassMetadata<T> implements ClassMetadata<T>, Serializable
{

   private static final long serialVersionUID = -2088679292389273922L;
   
   private Class<T> clazz;

   private ReflectiveClassMetadata(Class<T> clazz)
   {
      this.clazz = clazz;
   }

   public static <T> ClassMetadata<T> of(Class<T> clazz)
   {
      return new ReflectiveClassMetadata<T>(clazz);
   }

   public String getJavaClassName()
   {
      return clazz.getName();
   }

   public Iterable<MethodMetadata> getDeclaredMethods()
   {
      return new Iterable<MethodMetadata>()
      {
         public Iterator<MethodMetadata> iterator()
         {
             return new ImmutableIteratorWrapper<Method>(new ArrayIterator<Method>(ReflectiveClassMetadata.this.clazz.getDeclaredMethods()))
             {
                @Override
                protected MethodMetadata wrapObject(Method method)
                {
                   return ReflectiveMethodMetadata.of(method);
                }
             };
         }
      };     
   }

   public Class<T> getJavaClass()
   {
      return clazz;
   }

   public boolean isDeferringReflection()
   {
      return false;
   }

   @SuppressWarnings({ "unchecked", "rawtypes" })
   public ClassMetadata<?> getSuperclass()
   {
      Class<?> superClass = clazz.getSuperclass();
      return superClass == null? null : new ReflectiveClassMetadata(superClass);
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      ReflectiveClassMetadata<?> other = (ReflectiveClassMetadata<?>) obj;
      if (clazz == null)
      {
         if (other.clazz != null)
            return false;
      }
      else if (!clazz.equals(other.clazz))
         return false;
      return true;
   }
   
   

}
