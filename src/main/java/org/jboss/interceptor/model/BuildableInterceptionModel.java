package org.jboss.interceptor.model;

import java.lang.reflect.Method;

import org.jboss.interceptor.metadata.InterceptorMetadata;
import org.jboss.interceptor.metadata.MethodSignature;
import org.jboss.interceptor.model.InterceptionModel;
import org.jboss.interceptor.model.InterceptionType;

/**
 * An interception model that can be manipulated by the builder.
 *
 * @author Marius Bogoevici
 */
public interface BuildableInterceptionModel<T, I> extends InterceptionModel<T, I>
{

   void excludeGlobalInterceptors(MethodSignature methodSignature);

   void excludeDefaultInterceptorsGlobally();

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
