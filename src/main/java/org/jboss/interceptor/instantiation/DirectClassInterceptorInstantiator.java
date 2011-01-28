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

package org.jboss.interceptor.instantiation;

import org.jboss.interceptor.exceptions.InterceptorException;
import org.jboss.interceptor.metadata.ClassMetadata;
import org.jboss.interceptor.metadata.InterceptorReference;

/**
 * A simple implementation of an {@link InterceptorInstantiator} that uses {@link java.lang.Class#newInstance()}
 *
 * @author <a href="mailto:mariusb@redhat.com">Marius Bogoevici</a>
 */
public class DirectClassInterceptorInstantiator implements InterceptorInstantiator<Object, ClassMetadata<?>>
{

   public DirectClassInterceptorInstantiator()
   {
   }

   public Object createFor(InterceptorReference<ClassMetadata<?>> clazz)
   {
      try
      {
         return clazz.getInterceptor().getJavaClass().newInstance();
      }
      catch (InstantiationException e)
      {
         throw new InterceptorException(e);
      }
      catch (IllegalAccessException e)
      {
         throw new InterceptorException(e);
      }
   }
}
