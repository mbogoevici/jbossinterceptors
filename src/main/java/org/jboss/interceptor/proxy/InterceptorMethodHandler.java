package org.jboss.interceptor.proxy;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyObject;
import org.jboss.interceptor.reader.InterceptorMetadataUtils;
import org.jboss.interceptor.spi.context.InvocationContextFactory;
import org.jboss.interceptor.spi.instance.InterceptorInstantiator;
import org.jboss.interceptor.spi.metadata.ClassMetadata;
import org.jboss.interceptor.spi.metadata.InterceptorMetadata;
import org.jboss.interceptor.spi.model.InterceptionModel;
import org.jboss.interceptor.spi.model.InterceptionType;
import org.jboss.interceptor.spi.model.InterceptionTypeRegistry;
import org.jboss.interceptor.util.InterceptionUtils;
import org.jboss.interceptor.util.ReflectionUtils;

/**
 * @author Marius Bogoevici
 */
public class InterceptorMethodHandler implements MethodHandler, Serializable
{
   private static MethodHandler DEFAULT_METHOD_HANDLER = new MethodHandler()
   {

      public Object invoke(Object self, Method m,
                           Method proceed, Object[] args)
            throws Exception
      {
         return proceed.invoke(self, args);
      }
   };

   private Map<InterceptorMetadata<?>, Object> interceptorHandlerInstances = new HashMap<InterceptorMetadata<?>, Object>();
   private InterceptorMetadata<ClassMetadata<?>> targetClassInterceptorMetadata;
   private InterceptionModel<ClassMetadata<?>, ?> interceptionModel;
   private Object targetInstance;
   private InvocationContextFactory invocationContextFactory;

   public InterceptorMethodHandler(Object targetInstance,
                                   ClassMetadata<?> targetClassMetadata,
                                   InterceptionModel<ClassMetadata<?>, ?> interceptionModel,
                                   InterceptorInstantiator<?,?> interceptorInstantiator,
                                   InvocationContextFactory invocationContextFactory )
   {
      this.targetInstance = targetInstance;
      this.invocationContextFactory = invocationContextFactory;
      if (interceptionModel == null)
      {
         throw new IllegalArgumentException("Interception model must not be null");
      }
      if (interceptorInstantiator == null)
      {
         throw new IllegalArgumentException("Interception handler factory must not be null");
      }
      this.interceptionModel = interceptionModel;
      for (InterceptorMetadata interceptorMetadata : this.interceptionModel.getAllInterceptors())
      {
         interceptorHandlerInstances.put(interceptorMetadata, interceptorInstantiator.createFor(interceptorMetadata.getInterceptorReference()));
      }
      targetClassInterceptorMetadata = InterceptorMetadataUtils.readMetadataForTargetClass(targetClassMetadata);
   }

   protected boolean isProxy()
   {
      return targetInstance != null;
   }

   public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable
   {
      ReflectionUtils.ensureAccessible(thisMethod);
      if (proceed == null)
      {
         if (thisMethod.getName().equals(InterceptionUtils.POST_CONSTRUCT))
         {
            return executeInterception(isProxy() ? null : self, null, null, null, InterceptionType.POST_CONSTRUCT);
         }
         else if (thisMethod.getName().equals(InterceptionUtils.PRE_DESTROY))
         {
            return executeInterception(isProxy() ? null : self, null, null, null, InterceptionType.PRE_DESTROY);
         }
      }
      else
      {
         if (!InterceptionUtils.isInterceptionCandidate(thisMethod))
         {
            if (isProxy())
            {
               return thisMethod.invoke(targetInstance, args);
            }
            else
            {
               return proceed.invoke(self, args);
            }
         }
         if (InterceptionTypeRegistry.isSupported(InterceptionType.AROUND_TIMEOUT) && thisMethod.isAnnotationPresent(InterceptionTypeRegistry.getAnnotationClass(InterceptionType.AROUND_TIMEOUT)))
         {
            return executeInterception(isProxy() ? null : self, thisMethod, thisMethod, args, InterceptionType.AROUND_TIMEOUT);
         }
         else
         {
            return executeInterception(isProxy() ? null : self, thisMethod, thisMethod, args, InterceptionType.AROUND_INVOKE);
         }
      }
      return null;

   }

   private Object executeInterception(Object self, Method proceedingMethod, Method thisMethod, Object[] args, InterceptionType interceptionType) throws Throwable
   {

      List<? extends InterceptorMetadata<?>> interceptorList = interceptionModel.getInterceptors(interceptionType, thisMethod);
      Collection<InterceptorInvocation<?>> interceptorInvocations = new ArrayList<InterceptorInvocation<?>>();
      for (InterceptorMetadata interceptorReference : interceptorList)
      {
         interceptorInvocations.add(new InterceptorInvocation(interceptorHandlerInstances.get(interceptorReference), interceptorReference, interceptionType));
      }
      if (targetClassInterceptorMetadata != null && targetClassInterceptorMetadata.getInterceptorMethods(interceptionType) != null && !targetClassInterceptorMetadata.getInterceptorMethods(interceptionType).isEmpty())
      {
         interceptorInvocations.add(new InterceptorInvocation(isProxy() ? targetInstance : self, targetClassInterceptorMetadata, interceptionType));
      }
      SimpleInterceptionChain chain = new SimpleInterceptionChain(interceptorInvocations, interceptionType, isProxy() ? targetInstance : self, isProxy() ? thisMethod : proceedingMethod);
      return chain.invokeNextInterceptor(invocationContextFactory.newInvocationContext(chain, isProxy() ? targetInstance : self, isProxy() ? thisMethod : proceedingMethod, args));
   }

   private void writeObject(ObjectOutputStream objectOutputStream) throws IOException
   {
      try
      {
         executeInterception(isProxy() ? targetInstance : null, null, null, null, InterceptionType.PRE_PASSIVATE);
         objectOutputStream.defaultWriteObject();
      }
      catch (Throwable throwable)
      {
         throw new IOException("Error while serializing class", throwable);
      }
   }

   private void readObject(ObjectInputStream objectInputStream) throws IOException
   {
      try
      {
         objectInputStream.defaultReadObject();
         if (isProxy() && targetInstance instanceof ProxyObject && ((ProxyObject) targetInstance).getHandler() == null)
         {
            ((ProxyObject) targetInstance).setHandler(DEFAULT_METHOD_HANDLER);
         }
         executeInterception(isProxy() ? targetInstance : null, null, null, null, InterceptionType.POST_ACTIVATE);
      }
      catch (Throwable throwable)
      {
         throw new IOException("Error while deserializing class", throwable);
      }
   }

}
