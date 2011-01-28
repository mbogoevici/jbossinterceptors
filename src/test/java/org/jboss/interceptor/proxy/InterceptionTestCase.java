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

package org.jboss.interceptor.proxy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyObject;
import org.jboss.interceptor.interceptionchain.ClassLoaderReferenceResolver;
import org.jboss.interceptor.invocation.MethodReferenceResolver;
import org.jboss.interceptor.metadata.MethodSignature;
import org.jboss.interceptor.model.InterceptionModelBuilder;
import org.jboss.interceptor.instantiation.DirectClassInterceptorInstantiator;
import org.jboss.interceptor.instantiation.InterceptorInstantiator;
import org.jboss.interceptor.invocation.DefaultInvocationContextFactory;
import org.jboss.interceptor.javassist.CompositeHandler;
import org.jboss.interceptor.javassist.InterceptorProxyCreatorImpl;
import org.jboss.interceptor.reader.DefaultMetadataCachingReader;
import org.jboss.interceptor.reader.MetadataCachingReader;
import org.jboss.interceptor.metadata.ClassMetadata;
import org.jboss.interceptor.model.InterceptionModel;
import org.jboss.interceptor.util.InterceptionUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author <a href="mailto:mariusb@redhat.com">Marius Bogoevici</a>
 */
public class InterceptionTestCase
{
   private static final String TEAM_NAME = "Ajax Amsterdam";

   private String[] expectedLoggedValues = {
         "org.jboss.interceptor.proxy.FirstInterceptor_postConstruct",
         "org.jboss.interceptor.proxy.Team_postConstruct",
         "org.jboss.interceptor.proxy.FootballTeam_postConstruct",
         "org.jboss.interceptor.proxy.FirstInterceptor_aroundInvokeBefore",
         "org.jboss.interceptor.proxy.SecondInterceptor_aroundInvokeBefore",
         "org.jboss.interceptor.proxy.FootballTeam_aroundInvokeBefore",
         "org.jboss.interceptor.proxy.FootballTeam_getName",
         "org.jboss.interceptor.proxy.FootballTeam_aroundInvokeAfter",
         "org.jboss.interceptor.proxy.SecondInterceptor_aroundInvokeAfter",
         "org.jboss.interceptor.proxy.FirstInterceptor_aroundInvokeAfter",
         "org.jboss.interceptor.proxy.SecondInterceptor_preDestroy"
   };

   private String[] expectedLoggedValuesWithGlobalsIgnored = {
         "org.jboss.interceptor.proxy.FirstInterceptor_postConstruct",
         "org.jboss.interceptor.proxy.Team_postConstruct",
         "org.jboss.interceptor.proxy.FootballTeam_postConstruct",
         "org.jboss.interceptor.proxy.SecondInterceptor_aroundInvokeBefore",
         "org.jboss.interceptor.proxy.FootballTeam_aroundInvokeBefore",
         "org.jboss.interceptor.proxy.FootballTeam_getName",
         "org.jboss.interceptor.proxy.FootballTeam_aroundInvokeAfter",
         "org.jboss.interceptor.proxy.SecondInterceptor_aroundInvokeAfter",
         "org.jboss.interceptor.proxy.SecondInterceptor_preDestroy"
   };

   private String[] expectedLoggedValuesOnSerialization = {
         "org.jboss.interceptor.proxy.FirstInterceptor_postConstruct",
         "org.jboss.interceptor.proxy.Team_postConstruct",
         "org.jboss.interceptor.proxy.FootballTeam_postConstruct",
         "org.jboss.interceptor.proxy.FootballTeam_prePassivating",
         "org.jboss.interceptor.proxy.FootballTeam_postActivating",
         "org.jboss.interceptor.proxy.FirstInterceptor_aroundInvokeBefore",
         "org.jboss.interceptor.proxy.SecondInterceptor_aroundInvokeBefore",
         "org.jboss.interceptor.proxy.FootballTeam_aroundInvokeBefore",
         "org.jboss.interceptor.proxy.FootballTeam_getName",
         "org.jboss.interceptor.proxy.FootballTeam_aroundInvokeAfter",
         "org.jboss.interceptor.proxy.SecondInterceptor_aroundInvokeAfter",
         "org.jboss.interceptor.proxy.FirstInterceptor_aroundInvokeAfter",
   };

   private String[] expectedLoggedValuesWhenRaw = {
         "org.jboss.interceptor.proxy.FootballTeam_getName",
   };

   private Map<Class<?>, InterceptionModel<ClassMetadata<?>,?>> interceptionModelRegistry;

   private InterceptorInstantiator<?, ClassMetadata<?>> interceptorInstantiator;
   
   MetadataCachingReader metadataCachingReader = new DefaultMetadataCachingReader();
   private MethodReferenceResolver methodReferenceResolver;

   @Before
   public void setUp()
   {
      interceptorInstantiator = new DirectClassInterceptorInstantiator();

      methodReferenceResolver = new ClassLoaderReferenceResolver(Thread.currentThread().getContextClassLoader());
   }

   public void resetLogAndSetupClassesForMethod() throws Exception
   {
      InterceptorTestLogger.reset();
      ClassMetadata<?> footballTeamClass = metadataCachingReader.getClassMetadata(FootballTeam.class);
      InterceptionModelBuilder<ClassMetadata<?>,?> builder =
             InterceptionModelBuilder.<ClassMetadata<?>>newBuilderFor(footballTeamClass);
      builder.interceptAroundInvoke(new MethodSignature("getName")).with(
             metadataCachingReader.getInterceptorMetadata(FirstInterceptor.class),  metadataCachingReader.getInterceptorMetadata(SecondInterceptor.class));
      builder.interceptPostConstruct().with( metadataCachingReader.getInterceptorMetadata(FirstInterceptor.class));
      builder.interceptPreDestroy().with( metadataCachingReader.getInterceptorMetadata(SecondInterceptor.class));
      InterceptionModel<ClassMetadata<?>, ?> interceptionModel;
      interceptionModel = builder.build();
      this.interceptionModelRegistry = new HashMap<Class<?>, InterceptionModel<ClassMetadata<?>,?>>();
      this.interceptionModelRegistry.put(FootballTeam.class, interceptionModel);

   }

   public void resetLogAndSetupClassesGlobally() throws Exception
   {
      InterceptorTestLogger.reset();

      ClassMetadata<?> footballTeamClass =  metadataCachingReader.getClassMetadata(FootballTeam.class);
      InterceptionModelBuilder<ClassMetadata<?>,?> builder =
             InterceptionModelBuilder.<ClassMetadata<?>>newBuilderFor(footballTeamClass);

      builder.interceptAll().with( metadataCachingReader.getInterceptorMetadata(FirstInterceptor.class),  metadataCachingReader.getInterceptorMetadata(SecondInterceptor.class));
      InterceptionModel<ClassMetadata<?>,?> interceptionModel;
      interceptionModel = builder.build();
      this.interceptionModelRegistry = new HashMap<Class<?>, InterceptionModel<ClassMetadata<?>,?>>();
      this.interceptionModelRegistry.put(FootballTeam.class, interceptionModel);

   }

   public void resetLogAndSetupClassesMixed() throws Exception
   {
      InterceptorTestLogger.reset();

      ClassMetadata<?> footballTeamClass =  metadataCachingReader.getClassMetadata(FootballTeam.class);
      InterceptionModelBuilder<ClassMetadata<?>,?> builder =
             InterceptionModelBuilder.<ClassMetadata<?>>newBuilderFor(footballTeamClass);
      builder.interceptAll().with( metadataCachingReader.getInterceptorMetadata(FirstInterceptor.class));
      builder.interceptPreDestroy().with( metadataCachingReader.getInterceptorMetadata(SecondInterceptor.class));
      builder.interceptAroundInvoke(new MethodSignature("getName")).with( metadataCachingReader.getInterceptorMetadata(SecondInterceptor.class));
      InterceptionModel<ClassMetadata<?>,?> interceptionModel;
      interceptionModel = builder.build();
      this.interceptionModelRegistry = new HashMap<Class<?>, InterceptionModel<ClassMetadata<?>,?>>();
      this.interceptionModelRegistry.put(FootballTeam.class, interceptionModel);

   }

   public void resetLogAndSetupClassesWithGlobalsIgnored() throws Exception
   {
      InterceptorTestLogger.reset();

      ClassMetadata<?> footballTeamClass =  metadataCachingReader.getClassMetadata(FootballTeam.class);
      InterceptionModelBuilder<ClassMetadata<?>,?> builder =
             InterceptionModelBuilder.<ClassMetadata<?>>newBuilderFor(footballTeamClass);
      builder.interceptAll().with( metadataCachingReader.getInterceptorMetadata(FirstInterceptor.class));
      builder.interceptPreDestroy().with( metadataCachingReader.getInterceptorMetadata(SecondInterceptor.class));
      builder.interceptAroundInvoke(new MethodSignature("getName")).with( metadataCachingReader.getInterceptorMetadata(SecondInterceptor.class));
      builder.excludeGlobalInterceptors(new MethodSignature("getName"));
      InterceptionModel<ClassMetadata<?>,?> interceptionModel;
      interceptionModel = builder.build();
      this.interceptionModelRegistry = new HashMap<Class<?>, InterceptionModel<ClassMetadata<?>,?>>();
      this.interceptionModelRegistry.put(FootballTeam.class, interceptionModel);

   }


   @Test
   public void testInterceptionWithMethodRegisteredInterceptors() throws Exception
   {
      resetLogAndSetupClassesForMethod();
      FootballTeam proxy = createAdvisedInstance(FootballTeam.class, TEAM_NAME);
      InterceptionUtils.executePostConstruct(proxy);
      Assert.assertEquals(TEAM_NAME, proxy.getName());
      InterceptionUtils.executePredestroy(proxy);
      Object[] logValues = InterceptorTestLogger.getLog().toArray();
      Assert.assertArrayEquals(iterateAndDisplay(logValues), expectedLoggedValues, logValues);
      assertRawObject(proxy);
   }

   @Test
   public void testInterceptionWithGlobalInterceptors() throws Exception
   {
      resetLogAndSetupClassesGlobally();
      FootballTeam proxy = createAdvisedInstance(FootballTeam.class, TEAM_NAME);
      InterceptionUtils.executePostConstruct(proxy);
      Assert.assertEquals(TEAM_NAME, proxy.getName());
      InterceptionUtils.executePredestroy(proxy);
      assertRawObject(proxy);
   }

   @Test
   public void testInterceptionWithMixedInterceptors() throws Exception
   {
      resetLogAndSetupClassesMixed();
      FootballTeam proxy = createAdvisedInstance(FootballTeam.class, TEAM_NAME);
      InterceptionUtils.executePostConstruct(proxy);
      Assert.assertEquals(TEAM_NAME, proxy.getName());
      InterceptionUtils.executePredestroy(proxy);
      Object[] logValues = InterceptorTestLogger.getLog().toArray();
      Assert.assertArrayEquals(iterateAndDisplay(logValues), expectedLoggedValues, logValues);
      assertRawObject(proxy);
   }

   @Test
   public void testInterceptionWithGlobalsIgnored() throws Exception
   {
      resetLogAndSetupClassesWithGlobalsIgnored();
      FootballTeam proxy = createAdvisedInstance(FootballTeam.class, TEAM_NAME);
      InterceptionUtils.executePostConstruct(proxy);
      Assert.assertEquals(TEAM_NAME, proxy.getName());
      InterceptionUtils.executePredestroy(proxy);
      Object[] logValues = InterceptorTestLogger.getLog().toArray();
      Assert.assertArrayEquals(iterateAndDisplay(logValues), expectedLoggedValuesWithGlobalsIgnored, logValues);
      assertRawObject(proxy);
   }


   @Test
   @Ignore
   public void testInterceptionWithSerializedProxy() throws Exception
   {
      resetLogAndSetupClassesForMethod();
      FootballTeam proxy = createAdvisedInstance(FootballTeam.class, TEAM_NAME);
      InterceptionUtils.executePostConstruct(proxy);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      new ObjectOutputStream(baos).writeObject(proxy);
      proxy = (FootballTeam) new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())).readObject();
      Assert.assertEquals(TEAM_NAME, proxy.getName());
      Object[] logValues = InterceptorTestLogger.getLog().toArray();
      Assert.assertArrayEquals(iterateAndDisplay(logValues), expectedLoggedValuesOnSerialization, logValues);
      Assert.assertTrue(((ProxyObject)proxy).getHandler() instanceof CompositeHandler);
      assertRawObject(proxy);
   }


   @Test
   public void testSerialization() throws Exception
   {
      FootballTeam proxy = new FootballTeam("Ajax Amsterdam");
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      new ObjectOutputStream(baos).writeObject(proxy);
      proxy = (FootballTeam) new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())).readObject();
      Assert.assertNotNull(proxy);
   }


   @Test
   public void testMethodParameterOverriding() throws Exception
   {
      InterceptorTestLogger.reset();

      ClassMetadata<?> footballTeamClass =  metadataCachingReader.getClassMetadata(FootballTeam.class);
      InterceptionModelBuilder<ClassMetadata<?>,?> builder =
             InterceptionModelBuilder.<ClassMetadata<?>>newBuilderFor(footballTeamClass);
      builder.interceptAroundInvoke(new MethodSignature("echo", String.class.getName())).with( metadataCachingReader.getInterceptorMetadata(ParameterOverridingInterceptor.class));
      InterceptionModel<ClassMetadata<?>,?> interceptionModel;
      interceptionModel = builder.build();
      this.interceptionModelRegistry = new HashMap<Class<?>, InterceptionModel<ClassMetadata<?>,?>>();
      this.interceptionModelRegistry.put(FootballTeam.class, interceptionModel);

      FootballTeam proxy = createAdvisedInstance(FootballTeam.class, TEAM_NAME);
      Assert.assertEquals(42, proxy.echo("1"));
   }

   @Test
   public void testMethodParameterOverridingWithPrimitive() throws Exception
   {
      InterceptorTestLogger.reset();

      ClassMetadata<?> footballTeamClass =  metadataCachingReader.getClassMetadata(FootballTeam.class);
      InterceptionModelBuilder<ClassMetadata<?>,?> builder =
             InterceptionModelBuilder.<ClassMetadata<?>>newBuilderFor(footballTeamClass);

      builder.interceptAroundInvoke(new MethodSignature("echoInt", int.class.getName())).with( metadataCachingReader.getInterceptorMetadata(ParameterOverridingInterceptorWithInteger.class));
      InterceptionModel<ClassMetadata<?>,?> interceptionModel;
      interceptionModel = builder.build();
      this.interceptionModelRegistry = new HashMap<Class<?>, InterceptionModel<ClassMetadata<?>,?>>();
      this.interceptionModelRegistry.put(FootballTeam.class, interceptionModel);

      FootballTeam proxy = createAdvisedInstance(FootballTeam.class, TEAM_NAME);
      Assert.assertEquals(42, proxy.echoInt(1));
   }

   @Test(expected = IllegalArgumentException.class)
   public void testMethodParameterOverridingWithObject() throws Exception
   {
      InterceptorTestLogger.reset();

      ClassMetadata<?> footballTeamClass =  metadataCachingReader.getClassMetadata(FootballTeam.class);
      InterceptionModelBuilder<ClassMetadata<?>,?> builder =
             InterceptionModelBuilder.<ClassMetadata<?>>newBuilderFor(footballTeamClass);

      builder.interceptAroundInvoke(new MethodSignature("echoLongAsObject", Long.class.getName())).with( metadataCachingReader.getInterceptorMetadata(ParameterOverridingInterceptorWithInteger.class));
      InterceptionModel<ClassMetadata<?>,?> interceptionModel;
      interceptionModel = builder.build();
      this.interceptionModelRegistry = new HashMap<Class<?>, InterceptionModel<ClassMetadata<?>,?>>();
      this.interceptionModelRegistry.put(FootballTeam.class, interceptionModel);

      FootballTeam proxy = createAdvisedInstance(FootballTeam.class, TEAM_NAME);
      Assert.assertEquals(new Long(42), proxy.echoLongAsObject(1l));
   }

   @Test
   public void testMethodParameterOverridingWithObjectSucceed() throws Exception
   {
      InterceptorTestLogger.reset();

      ClassMetadata<?> footballTeamClass =  metadataCachingReader.getClassMetadata(FootballTeam.class);
      InterceptionModelBuilder<ClassMetadata<?>,?> builder =
             InterceptionModelBuilder.<ClassMetadata<?>>newBuilderFor(footballTeamClass);

      builder.interceptAroundInvoke(new MethodSignature("echoLongAsObject", Long.class)).with( metadataCachingReader.getInterceptorMetadata(ParameterOverridingInterceptorWithLong.class));
      InterceptionModel<ClassMetadata<?>,?> interceptionModel;
      interceptionModel = builder.build();
      this.interceptionModelRegistry = new HashMap<Class<?>, InterceptionModel<ClassMetadata<?>,?>>();
      this.interceptionModelRegistry.put(FootballTeam.class, interceptionModel);

      FootballTeam proxy = createAdvisedInstance(FootballTeam.class, TEAM_NAME);
      Assert.assertEquals(new Long(42), proxy.echoLongAsObject(1l));
   }

   @Test
   public void testMethodParameterOverridingWithPrimitiveWidening() throws Exception
   {
      InterceptorTestLogger.reset();

      ClassMetadata<?> footballTeamClass =  metadataCachingReader.getClassMetadata(FootballTeam.class);
      InterceptionModelBuilder<ClassMetadata<?>,?> builder =
             InterceptionModelBuilder.<ClassMetadata<?>>newBuilderFor(footballTeamClass);

      builder.interceptAroundInvoke(new MethodSignature("echoLong", long.class)).with( metadataCachingReader.getInterceptorMetadata(ParameterOverridingInterceptorWithInteger.class));
      InterceptionModel<ClassMetadata<?>,?> interceptionModel;
      interceptionModel = builder.build();
      this.interceptionModelRegistry = new HashMap<Class<?>, InterceptionModel<ClassMetadata<?>,?>>();
      this.interceptionModelRegistry.put(FootballTeam.class, interceptionModel);

      FootballTeam proxy = createAdvisedInstance(FootballTeam.class, TEAM_NAME);
      Assert.assertEquals(42, proxy.echoLong(1));
   }

   @Test(expected = IllegalArgumentException.class)
   public void testMethodParameterOverridingWithPrimitiveNarrowing() throws Exception
   {
      InterceptorTestLogger.reset();

      ClassMetadata<?> footballTeamClass =  metadataCachingReader.getClassMetadata(FootballTeam.class);
      InterceptionModelBuilder<ClassMetadata<?>,?> builder =
             InterceptionModelBuilder.<ClassMetadata<?>>newBuilderFor(footballTeamClass);

      builder.interceptAroundInvoke(new MethodSignature("echoInt", int.class)).with( metadataCachingReader.getInterceptorMetadata(ParameterOverridingInterceptorWithLong.class));
      InterceptionModel<ClassMetadata<?>,?> interceptionModel;
      interceptionModel = builder.build();
      this.interceptionModelRegistry = new HashMap<Class<?>, InterceptionModel<ClassMetadata<?>,?>>();
      this.interceptionModelRegistry.put(FootballTeam.class, interceptionModel);

      FootballTeam proxy = createAdvisedInstance(FootballTeam.class, TEAM_NAME);
      Assert.assertEquals(42, proxy.echoInt(1));
   }

   @Test
   public void testMethodParameterOverridingWithArray() throws Exception
   {
      InterceptorTestLogger.reset();

      ClassMetadata<?> footballTeamClass =  metadataCachingReader.getClassMetadata(FootballTeam.class);
      InterceptionModelBuilder<ClassMetadata<?>,?> builder =
             InterceptionModelBuilder.<ClassMetadata<?>>newBuilderFor(footballTeamClass);

      builder.interceptAroundInvoke(new MethodSignature("echoObjectArray", Object[].class)).with( metadataCachingReader.getInterceptorMetadata(ParameterOverridingInterceptorWithLongArray.class));
      InterceptionModel<ClassMetadata<?>,?> interceptionModel;
      interceptionModel = builder.build();
      this.interceptionModelRegistry = new HashMap<Class<?>, InterceptionModel<ClassMetadata<?>,?>>();
      this.interceptionModelRegistry.put(FootballTeam.class, interceptionModel);

      FootballTeam proxy = createAdvisedInstance(FootballTeam.class, TEAM_NAME);
      Assert.assertEquals(new Long[]{42l}, proxy.echoObjectArray(new Object[]{}));
   }

   @Test(expected = IllegalArgumentException.class)
   public void testMethodParameterOverridingWithArrayOnString() throws Exception
   {
      InterceptorTestLogger.reset();

      ClassMetadata<?> footballTeamClass =  metadataCachingReader.getClassMetadata(FootballTeam.class);
      InterceptionModelBuilder<ClassMetadata<?>,?> builder =
             InterceptionModelBuilder.<ClassMetadata<?>>newBuilderFor(footballTeamClass);

      builder.interceptAroundInvoke(new MethodSignature("echoStringArray", String[].class)).with( metadataCachingReader.getInterceptorMetadata(ParameterOverridingInterceptorWithLongArray.class));
      InterceptionModel<ClassMetadata<?>,?> interceptionModel;
      interceptionModel = builder.build();
      this.interceptionModelRegistry = new HashMap<Class<?>, InterceptionModel<ClassMetadata<?>,?>>();
      this.interceptionModelRegistry.put(FootballTeam.class, interceptionModel);

      FootballTeam proxy = createAdvisedInstance(FootballTeam.class, TEAM_NAME);
      Assert.assertEquals(new Long[]{42l}, proxy.echoStringArray(new String[]{}));
   }

   @Test
   public void testMethodParameterOverridingWithSubclass() throws Exception
   {
      InterceptorTestLogger.reset();


      ClassMetadata<?> footballTeamClass =  metadataCachingReader.getClassMetadata(FootballTeam.class);
      InterceptionModelBuilder<ClassMetadata<?>,?> builder =
             InterceptionModelBuilder.<ClassMetadata<?>>newBuilderFor(footballTeamClass);

      builder.interceptAroundInvoke(new MethodSignature("echo2", ValueBearer.class)).with( metadataCachingReader.getInterceptorMetadata(ParameterOverridingInterceptor2.class));
      InterceptionModel<ClassMetadata<?>,?> interceptionModel;
      interceptionModel = builder.build();
      this.interceptionModelRegistry = new HashMap<Class<?>, InterceptionModel<ClassMetadata<?>,?>>();
      this.interceptionModelRegistry.put(FootballTeam.class, interceptionModel);

      FootballTeam proxy = createAdvisedInstance(FootballTeam.class, TEAM_NAME);
      proxy.doNothing();
      Assert.assertEquals(42, proxy.echo2(new ValueBearerImpl(1)));
   }

   @Test
   public void testMethodParameterOverridingWithChar() throws Exception
   {
      InterceptorTestLogger.reset();

      ClassMetadata<?> footballTeamClass =  metadataCachingReader.getClassMetadata(FootballTeam.class);
      InterceptionModelBuilder<ClassMetadata<?>,?> builder =
             InterceptionModelBuilder.<ClassMetadata<?>>newBuilderFor(footballTeamClass);
      builder.interceptAll()
            .with( metadataCachingReader.getInterceptorMetadata((ParameterOverridingInterceptorWithChar.class)),
            metadataCachingReader.getInterceptorMetadata((ParameterOverridingInterceptorWithCharacter.class)));

      InterceptionModel<ClassMetadata<?>,?> interceptionModel;
      interceptionModel = builder.build();
      this.interceptionModelRegistry = new HashMap<Class<?>, InterceptionModel<ClassMetadata<?>,?>>();
      this.interceptionModelRegistry.put(FootballTeam.class, interceptionModel);

      FootballTeam proxy = createAdvisedInstance(FootballTeam.class, TEAM_NAME);
      Assert.assertEquals('z', proxy.echoChar('a'));
      Assert.assertEquals((Object)'z', proxy.echoCharacter('a'));
   }

   @Test
   public void testMethodParameterOverridingWithCharacterAndLong() throws Exception
   {
      InterceptorTestLogger.reset();

      ClassMetadata<?> footballTeamClass =  metadataCachingReader.getClassMetadata(FootballTeam.class);
      InterceptionModelBuilder<ClassMetadata<?>,?> builder =
             InterceptionModelBuilder.<ClassMetadata<?>>newBuilderFor(footballTeamClass);
      builder.interceptAll()
            .with(metadataCachingReader.getInterceptorMetadata((ParameterOverridingInterceptorWithCharacterAndLong.class)));

      InterceptionModel<ClassMetadata<?>,?> interceptionModel;
      interceptionModel = builder.build();
      this.interceptionModelRegistry = new HashMap<Class<?>, InterceptionModel<ClassMetadata<?>,?>>();
      this.interceptionModelRegistry.put(FootballTeam.class, interceptionModel);

      FootballTeam proxy = createAdvisedInstance(FootballTeam.class, TEAM_NAME);
      Assert.assertEquals((long)'z', proxy.echoLong('a'));
   }

   @Test
   public void testMethodParameterOverridingWithBoolean() throws Exception
   {
      InterceptorTestLogger.reset();

      ClassMetadata<?> footballTeamClass =  metadataCachingReader.getClassMetadata(FootballTeam.class);
      InterceptionModelBuilder<ClassMetadata<?>,?> builder =
             InterceptionModelBuilder.<ClassMetadata<?>>newBuilderFor(footballTeamClass);
      builder.interceptAll().with( metadataCachingReader.getInterceptorMetadata((ParameterOverridingInterceptorWithBoolean.class)),
            metadataCachingReader.getInterceptorMetadata((ParameterOverridingInterceptorWithBooleanAsObject.class)));
      InterceptionModel<ClassMetadata<?>,?> interceptionModel;
      interceptionModel = builder.build();
      this.interceptionModelRegistry = new HashMap<Class<?>, InterceptionModel<ClassMetadata<?>,?>>();
      this.interceptionModelRegistry.put(FootballTeam.class, interceptionModel);

      FootballTeam proxy = createAdvisedInstance(FootballTeam.class, TEAM_NAME);
      Assert.assertEquals(false, proxy.echoBoolean(true));
      Assert.assertEquals(false, proxy.echoBooleanAsObject(true));
   }

   public void assertRawObject(FootballTeam proxy)
   {
//      InterceptorTestLogger.reset();
//      FootballTeam rawInstance = InterceptionUtils.getRawInstance(proxy);
//      Assert.assertEquals(TEAM_NAME, rawInstance.getName());
//      Object[] logValues = InterceptorTestLogger.getLog().toArray();
//      Assert.assertArrayEquals(iterateAndDisplay(logValues), expectedLoggedValuesWhenRaw, logValues);logValues
   }

   private String iterateAndDisplay(Object[] logValues)
   {
      StringBuffer buffer = new StringBuffer();
      for (Object logValue : logValues)
      {
         buffer.append(logValue.toString()).append("\n");
      }
      return buffer.toString();
   }

   private <T> T createAdvisedInstance(Class<? extends T> targetClass, Object... args) throws Exception
   {
      ArrayList<Class<?>> argumentTypes = new ArrayList<Class<?>>();
      for (Object arg: args)
      {
         argumentTypes.add(arg.getClass());
      }
      Constructor<? extends T> constructor = targetClass.getConstructor(argumentTypes.toArray(new Class<?>[]{}));
      T instance = constructor.newInstance(args);
      return proxifyInstance(instance, targetClass);
   }

   private <T> T proxifyInstance(T instance, Class<? extends T> targetClass)
   {
      DefaultInvocationContextFactory contextFactory = new DefaultInvocationContextFactory();
      InterceptorProxyCreatorImpl ipc = new InterceptorProxyCreatorImpl(interceptorInstantiator, contextFactory, (InterceptionModel<ClassMetadata<?>, Object>) interceptionModelRegistry.get(targetClass), methodReferenceResolver);
      ClassMetadata<? extends T> targetClassMetadata =  metadataCachingReader.getClassMetadata(targetClass);
      MethodHandler methodHandler = ipc.createMethodHandler(instance, targetClassMetadata);
      Class<? extends T> proxyClassWithHandler = InterceptionUtils.createProxyClassWithHandler(targetClassMetadata, methodHandler);
      return ipc.createProxyInstance(proxyClassWithHandler, methodHandler);
   }
}
