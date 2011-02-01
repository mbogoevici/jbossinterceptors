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

package org.jboss.interceptor.spi.metadata;

import java.io.Serializable;

/**
 * Abstraction of class metadata. Allows for the framework client to configure
 * their own way of reading information about a class, rather than relying exclusively
 * on Java reflection.
 * 
 * @author Marius Bogoevici
 */
public interface ClassMetadata<T> extends Serializable
{
   /**
    * Behaves similarly to {@link Class#getDeclaredMethods()}, but returns {@link MethodMetadata} allowing
    * implementors to use other mechanisms than reflection to provide information about methods
    *
    * @return
    */
   Iterable<MethodMetadata> getDeclaredMethods();

   /**
    *  Return true if the implementation will return class data lazily (i.e. when invoking {@link #getJavaClass()})
    *  Implementations that return true may not provide information about the actual {@link Class} at all, or invoking
    * {@link #getJavaClass()} may be expensive.
    *
    */
   boolean isDeferringReflection();

   /**
    * Return the actual class that is backing the ClassMetadata, if available
    *
    * @return
    *
    * Note: this method is optional. If not supported or expensive, {@link #isDeferringReflection()} must return true
    */
   Class<T> getJavaClass();
   
   String getJavaClassName();

   ClassMetadata<?> getSuperclass();

}
