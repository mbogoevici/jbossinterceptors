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

/**
 * {@link org.jboss.interceptor.metadata.ClassMetadata}-based implementation of {@link org.jboss.interceptor.metadata.InterceptorReference}
 *
 * This is used internally by the framework.
 */
public class ClassMetadataInterceptorReference implements InterceptorReference<ClassMetadata<?>>
{
   private ClassMetadata<?> classMetadata;

   private ClassMetadataInterceptorReference(ClassMetadata<?> classMetadata)
   {
      this.classMetadata = classMetadata;
   }

   public static InterceptorReference<ClassMetadata<?>> of(ClassMetadata<?> classMetadata)
   {
      return new ClassMetadataInterceptorReference(classMetadata);
   }

   public ClassMetadata<?> getClassMetadata()
   {
      return classMetadata;
   }

   public ClassMetadata<?> getInterceptor()
   {
      // here the interceptor type is the class itself, so this duplicates getClassMetadata()
      return getClassMetadata();
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

      ClassMetadataInterceptorReference that = (ClassMetadataInterceptorReference) o;

      if (classMetadata != null ? !classMetadata.equals(that.classMetadata) : that.classMetadata != null)
      {
         return false;
      }

      return true;
   }

   @Override
   public int hashCode()
   {
      return classMetadata != null ? classMetadata.hashCode() : 0;
   }
}
