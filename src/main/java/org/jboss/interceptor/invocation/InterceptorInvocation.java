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

package org.jboss.interceptor.invocation;

import java.util.ArrayList;
import java.util.Collection;

import org.jboss.interceptor.metadata.InterceptorMetadata;
import org.jboss.interceptor.metadata.MethodMetadata;
import org.jboss.interceptor.metadata.MethodReference;
import org.jboss.interceptor.metadata.MethodSignature;
import org.jboss.interceptor.model.InterceptionType;

/**
 * An
 *
 * @author Marius Bogoevici
 */
public class InterceptorInvocation<T>
{
   private T instance;

   private InterceptorMetadata<?> interceptorMetadata;

   private InterceptionType interceptionType;

   private MethodReferenceResolver methodReferenceResolver;

   public InterceptorInvocation(T instance, InterceptorMetadata<?> interceptorMetadata, InterceptionType interceptionType, MethodReferenceResolver methodReferenceResolver)
   {
      this.instance = instance;
      this.interceptorMetadata = interceptorMetadata;
      this.interceptionType = interceptionType;
      this.methodReferenceResolver = methodReferenceResolver;
   }

   public Collection<InterceptorMethodInvocation> getInterceptorMethodInvocations()
   {
      Collection<InterceptorMethodInvocation> interceptorMethodInvocations = new ArrayList<InterceptorMethodInvocation>();
      for (MethodMetadata method : interceptorMetadata.getInterceptorMethods(interceptionType))
      {
         interceptorMethodInvocations.add(new InterceptorMethodInvocation(instance, methodReferenceResolver.resolve(
               new MethodReference(method.getDeclaringClassName(), new MethodSignature(method.getName(), method.getParameterTypeNames())))));
      }
      return interceptorMethodInvocations;
   }

}
