package org.jboss.interceptor.reader;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.interceptor.InvocationContext;

import org.jboss.interceptor.metadata.ClassMetadataInterceptorReference;
import org.jboss.interceptor.metadata.ClassMetadata;
import org.jboss.interceptor.metadata.InterceptorMetadata;
import org.jboss.interceptor.metadata.InterceptorReference;
import org.jboss.interceptor.metadata.MethodMetadata;
import org.jboss.interceptor.metadata.MethodSignature;
import org.jboss.interceptor.metadata.SimpleInterceptorMetadata;
import org.jboss.interceptor.model.InterceptionType;
import org.jboss.interceptor.util.InterceptionTypeRegistry;
import org.jboss.interceptor.util.InterceptorMetadataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marius Bogoevici
 */
public class InterceptorMetadataUtils
{
   protected static final String OBJECT_CLASS_NAME = Object.class.getName();

   private static final Logger LOG = LoggerFactory.getLogger(InterceptorMetadataUtils.class);


   public static InterceptorMetadata readMetadataForInterceptorClass(InterceptorReference<?> interceptorReference)
   {
      return new SimpleInterceptorMetadata(interceptorReference, false, buildMethodMap(interceptorReference.getClassMetadata(), false));
   }

   public static InterceptorMetadata readMetadataForTargetClass(ClassMetadata<?> classMetadata)
   {
      return new SimpleInterceptorMetadata(ClassMetadataInterceptorReference.of(classMetadata), true, buildMethodMap(classMetadata, true));
   }

   public static boolean isInterceptorMethod(InterceptionType interceptionType, MethodMetadata method, boolean forTargetClass)
   {

      if (!method.getSupportedInterceptionTypes().contains(interceptionType))
      {
         return false;
      }

      if (interceptionType.isLifecycleCallback())
      {
         if (!Void.TYPE.getName().equals(method.getReturnTypeName()))
         {
            if (LOG.isWarnEnabled())
            {
              LOG.warn(getStandardIgnoredMessage(interceptionType, method.getName(), method.getDeclaringClassName()) + "does not have a void return type");
            }
            return false;
         }

         String[] parameterTypeNames = method.getParameterTypeNames();

         if (forTargetClass && parameterTypeNames.length != 0)
         {
            if (LOG.isWarnEnabled())
            {
               LOG.warn(getStandardIgnoredMessage(interceptionType, method.getName(), method.getDeclaringClassName()) + "is defined on the target class and does not have 0 arguments");
            }
            return false;
         }

         if (!forTargetClass && parameterTypeNames.length != 1)
         {
            if (LOG.isWarnEnabled())
            {
               LOG.warn(getStandardIgnoredMessage(interceptionType, method.getName(), method.getDeclaringClassName()) + "does not have exactly one parameter");
            }
            return false;
         }

         if (parameterTypeNames.length == 1 && !InvocationContext.class.getName().equals(parameterTypeNames[0]))
         {
            if (LOG.isWarnEnabled())
            {
               LOG.warn(getStandardIgnoredMessage(interceptionType, method.getName(), method.getDeclaringClassName()) + "its single argument is not a " + InvocationContext.class.getName());
            }
            return false;
         }

         return true;
      }
      else
      {
         if (!Object.class.getName().equals(method.getReturnTypeName()))
         {
            if (LOG.isWarnEnabled())
            {
               LOG.warn(getStandardIgnoredMessage(interceptionType, method.getName(), method.getDeclaringClassName()) + "does not return a " + OBJECT_CLASS_NAME);
            }
            return false;
         }

         String[] parameterTypeNames = method.getParameterTypeNames();

         if (parameterTypeNames.length != 1)
         {
            if (LOG.isWarnEnabled())
            {
               LOG.debug(getStandardIgnoredMessage(interceptionType, method.getName(), method.getDeclaringClassName()) + "does not have exactly 1 parameter");
            }
            return false;
         }

         if (!InvocationContext.class.getName().equals(parameterTypeNames[0]))
         {
            if (LOG.isWarnEnabled())
            {
               LOG.debug(getStandardIgnoredMessage(interceptionType, method.getName(), method.getDeclaringClassName()) + "does not have a " + InvocationContext.class.getName() + " parameter ");
            }
            return false;
         }

         return true;
      }
   }

   static String getStandardIgnoredMessage(InterceptionType interceptionType, String methodName, String declaringClassName)
   {
      return "Method " + methodName + " defined on class " + declaringClassName
            + " will not be used for interception, since it is not defined according to the specification. It is annotated with @"
            + interceptionType.annotationClassName() + ", but ";
   }

   static Map<InterceptionType, List<MethodMetadata>> buildMethodMap(ClassMetadata<?> interceptorClass, boolean forTargetClass)
   {
      Map<InterceptionType, List<MethodMetadata>> methodMap = new HashMap<InterceptionType, List<MethodMetadata>>();
      ClassMetadata<?> currentClass = interceptorClass;
      Set<MethodSignature> foundMethods = new HashSet<MethodSignature>();
      do
      {
         Set<InterceptionType> detectedInterceptorTypes = new HashSet<InterceptionType>();

         for (MethodMetadata method : currentClass.getDeclaredMethods())
         {
            //  Modifier.isPrivate(method.getJavaMethod().getModifiers()
            MethodSignature methodSignature = new MethodSignature(method.getName(), method.getParameterTypeNames());
            if (method.isPrivate() || !foundMethods.contains(methodSignature))
            {
               for (InterceptionType interceptionType : InterceptionTypeRegistry.getSupportedInterceptionTypes())
               {
                  if (isInterceptorMethod(interceptionType, method, forTargetClass))
                  {
                     if (methodMap.get(interceptionType) == null)
                     {
                        methodMap.put(interceptionType, new LinkedList<MethodMetadata>());
                     }
                     if (detectedInterceptorTypes.contains(interceptionType))
                     {
                        throw new InterceptorMetadataException("Same interception type cannot be specified twice on the same class");
                     }
                     else
                     {
                        detectedInterceptorTypes.add(interceptionType);
                     }
                     // add method in the list - if it is there already, it means that it has been added by a subclass
                     // private methods are treated separately, as a private method cannot override another method nor be
                     // overridden
                     if (!foundMethods.contains(methodSignature));
                     {
                        methodMap.get(interceptionType).add(0, method);
                     }
                  }
               }
               // the method reference must be added anyway - overridden methods are not taken into consideration
               foundMethods.add(methodSignature);
            }
         }
         currentClass = currentClass.getSuperclass();
      }
      while (currentClass != null && !OBJECT_CLASS_NAME.equals(currentClass.getClassName()));
      return methodMap;
   }
}
