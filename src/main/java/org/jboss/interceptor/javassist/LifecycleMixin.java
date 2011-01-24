package org.jboss.interceptor.javassist;

/**
 * @author Marius Bogoevici
*/
public interface LifecycleMixin
{
   String POST_CONSTRUCT = "lifecycle_mixin_$$_postConstruct";
   String PRE_DESTROY = "lifecycle_mixin_$$_preDestroy";

   public void lifecycle_mixin_$$_postConstruct();

   public void lifecycle_mixin_$$_preDestroy();
}
