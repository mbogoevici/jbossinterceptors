package org.jboss.interceptor.customInvocationContext;

public class Service
{

   boolean invoked = false;
   
   public void invoke()
   {
      invoked = true;
   }
   
   public boolean isInvoked()
   {
      return invoked;
   }
}
