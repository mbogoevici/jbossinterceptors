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
import java.util.List;

import org.jboss.interceptor.model.InterceptionType;

/**
 * Describes the capabilities of an interceptor. It assumes that the interceptor is
 * backed by a class
 *
 * @author <a href="mailto:mariusb@redhat.com">Marius Bogoevici</a>
 */
public interface InterceptorMetadata<T> extends Serializable
{
   /**
    * The reference t
    * 
    * @return
    */
   InterceptorReference<T> getInterceptorReference();

   ClassMetadata<?> getInterceptorClass();
   
   /**
    * Returns the sequence of interceptor methods that should be invoked on this interceptor for a given
    * interception type.
    *
    * @param interceptionType
    * @return a list of methods
    */
   List<MethodMetadata> getInterceptorMethods(InterceptionType interceptionType);
   
   /**
    * Indicates whether the interceptor corresponding to this {@link InterceptorMetadata} can
    * handle a particular {@link InterceptionType}
    *   
    * @param interceptionType The {@link InterceptionType}
    * @return
    */
   boolean isEligible(InterceptionType interceptionType);

   /**
    * Indicates whether this interceptor metadata has been creat
    * @return
    */
   boolean isTargetClass();
}
