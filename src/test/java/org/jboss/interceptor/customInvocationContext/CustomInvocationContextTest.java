package org.jboss.interceptor.customInvocationContext;

import junit.framework.Assert;
import org.jboss.interceptor.invocation.ClassLoaderReferenceResolver;
import org.jboss.interceptor.invocation.MethodReferenceResolver;
import org.jboss.interceptor.model.InterceptionModelBuilder;
import org.jboss.interceptor.invocation.InterceptionChain;
import org.jboss.interceptor.invocation.InvocationContextFactory;
import org.jboss.interceptor.instantiation.DirectClassInterceptorInstantiator;
import org.jboss.interceptor.javassist.InterceptorProxyCreatorImpl;
import org.jboss.interceptor.metadata.ReflectiveClassMetadata;
import org.jboss.interceptor.reader.DefaultMetadataCachingReader;
import org.jboss.interceptor.instantiation.InterceptorInstantiator;
import org.jboss.interceptor.metadata.ClassMetadata;
import org.jboss.interceptor.model.InterceptionModel;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.interceptor.InvocationContext;
import java.lang.reflect.Method;

@Ignore
public class CustomInvocationContextTest
{
   private DefaultMetadataCachingReader metadata;

   @Test
   public void testCustomInvocationContextSupported()
   {
      
      CustomInterceptor.invocationCount = 0;
      InterceptorInstantiator<?, ClassMetadata<?>> interceptorInstantiator = new DirectClassInterceptorInstantiator();
      
      InvocationContextFactory invocationContextFactory = new InvocationContextFactory()
      {
         
         public InvocationContext newInvocationContext(InterceptionChain chain, Object o, Method method, Object[] args)
         {
            return new CustomInvocationContextImpl(chain, o, method, args);
         }

         public InvocationContext newInvocationContext(InterceptionChain chain, Object o, Method method, Object timer)
         {
            throw new UnsupportedOperationException();
         }
      };
      
      ClassMetadata<Service> serviceClassMetadata = ReflectiveClassMetadata.of(Service.class);
      InterceptionModelBuilder<ClassMetadata<?>,?> builder = InterceptionModelBuilder.<ClassMetadata<?>>newBuilderFor(serviceClassMetadata);
      builder.interceptAll().with(metadata.getInterceptorMetadata(CustomInterceptor.class));
      InterceptionModel<ClassMetadata<?>,?> interceptionModel = builder.build();
      MethodReferenceResolver methodReferenceResolver =  new ClassLoaderReferenceResolver(Thread.currentThread().getContextClassLoader());
      InterceptorProxyCreatorImpl interceptorProxyCreator = new InterceptorProxyCreatorImpl(interceptorInstantiator, invocationContextFactory, interceptionModel, methodReferenceResolver);
      
      Service serviceInstance = interceptorProxyCreator.createSubclassingProxy(serviceClassMetadata, new Class<?>[]{}, new Object[]{} );
      
      serviceInstance.invoke();
      
      Assert.assertEquals(1, CustomInterceptor.invocationCount);
      Assert.assertTrue(serviceInstance.isInvoked());
      
   }

   @Before
   public void setUp() throws Exception
   {
      metadata = new DefaultMetadataCachingReader();
   }
}
