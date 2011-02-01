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

import org.jboss.interceptor.builder.MethodReference;
import org.jboss.interceptor.spi.model.InterceptionType;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * Abstraction of a source of metadata information about a method. Allows the framework client
 * to configure their own way of providing method metadata, rather than relying exclusively on
 * Java reflection.
 * 
 * @author Marius Bogoevici
 */
public interface MethodMetadata
{

   /**
    *  Return true if the implementation will return class data lazily (i.e. when invoking {@link #getJavaMethod()})
    *  Implementations that return true may not provide information about the actual {@link Method} at all, or invoking
    * {@link #getJavaMethod()} may be expensive.
    *
    */
   boolean isDeferringReflection();

   /**
    * Return the {@link Method} that is backing this instance, if available
    *
    * @return
    *
    * Note: this method is optional. If not supported or expensive, {@link #isDeferringReflection()} must return true
    */
   Method getJavaMethod();

   /**
    * A reference to the method that is backing this instance
    * @return
    */
   MethodReference getMethodReference();

   /**
    * The interception types that this method supports
    *
    * @return
    */
   Set<InterceptionType> getSupportedInterceptionTypes();
   
}
