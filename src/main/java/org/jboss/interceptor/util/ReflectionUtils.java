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

package org.jboss.interceptor.util;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * @author <a href="mailto:mariusb@redhat.com">Marius Bogoevici</a>
 */
public class ReflectionUtils
{

   public static void ensureAccessible(final Method method)
   {
      doSecurely(new PrivilegedAction<Object>()
      {
         public Object run()
         {
            method.setAccessible(true);
            return null;
         }
      });

   }

   public static Class<?> classForName(String className) throws ClassNotFoundException
   {
      ClassLoader threadContextClassLoader = getThreadContextClassLoader(true);
      if (threadContextClassLoader != null)
      {
         return threadContextClassLoader.loadClass(className);
      }
      else
      {
         return Class.forName(className);
      }
   }

   public static ClassLoader getThreadContextClassLoader(boolean securely)
   {
      if (securely)
      {
         return doSecurely(new PrivilegedAction<ClassLoader>()
         {
            public ClassLoader run()
            {
               return Thread.currentThread().getContextClassLoader();
            }
         });
      }
      else
      {
         return Thread.currentThread().getContextClassLoader();
      }
   }

   private static <O> O doSecurely(final PrivilegedAction<O> action)
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null)
      {
         return AccessController.doPrivileged(action);
      }
      else
      {
         return action.run();
      }
   }

}
