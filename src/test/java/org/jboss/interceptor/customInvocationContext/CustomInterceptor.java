package org.jboss.interceptor.customInvocationContext;

import javax.interceptor.AroundInvoke;

public class CustomInterceptor
{
   
   static int invocationCount = 0;
   
   @AroundInvoke
   public Object aroundInvoke(CustomInvocationContext context) throws Exception
   {
      invocationCount ++;
      return context.proceed();
   }
   
}
