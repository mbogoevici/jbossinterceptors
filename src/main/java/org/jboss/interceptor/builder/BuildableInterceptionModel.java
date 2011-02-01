package org.jboss.interceptor.builder;

import java.lang.reflect.Method;

import org.jboss.interceptor.spi.metadata.ClassMetadata;
import org.jboss.interceptor.spi.metadata.InterceptorMetadata;
import org.jboss.interceptor.spi.model.InterceptionModel;
import org.jboss.interceptor.spi.model.InterceptionType;

/**
 * An interception model that can be manipulated by {@link InterceptionModelBuilder}.
 *
 * @author Marius Bogoevici
 */
public interface BuildableInterceptionModel<T, I> extends InterceptionModel<T, I>
{
   /**
    * Marks the method as excluding global interceptors
    *
    * @param methodSignature
    */
   void excludeGlobalInterceptors(MethodSignature methodSignature);


   /**
    * Marks the whole model as excluding default interceptors
    *
    */
   void excludeDefaultInterceptorsGlobally();

   /**
    * Marks the method as excluding global interceptors
    *
    * @param methodSignature
    */
   void excludeDefaultInterceptors(MethodSignature methodSignature);

   /**
    * Appends interceptors to the model
    *
    * @param interceptionType - the interception type
    * @param method           - the method for which the interceptors will be appended global if null
    * @param interceptors     - the interceptors that will be appended
    */
   void appendInterceptors(InterceptionType interceptionType, MethodSignature method, InterceptorMetadata<I>... interceptors);

}
