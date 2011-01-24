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

import javax.annotation.PostConstruct;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

/**
 * @author <a href="mailto:mariusb@redhat.com">Marius Bogoevici</a>
 */
public class FootballTeam extends Team 
{

   private String teamName;

   public FootballTeam(String s) {
        this.teamName = s;
    }

    public String getName() {
        InterceptorTestLogger.add(FootballTeam.class, "getName");
        return teamName;
    }

    public int echo(String i)
    {
       return Integer.parseInt(i);
    }

    public int echo2(ValueBearer vb)
    {
       return vb.getValue();
    }

    public int echoInt(int i)
    {
       return i;
    }

    public long echoLong(long i)
    {
       return i;
    }

    public Long echoLongAsObject(Long i)
    {
       return i;
    }

    public Object[] echoObjectArray(Object[] i)
    {
       return i;
    }

    public String[] echoStringArray(String[] i)
    {
       return i;
    }

    public char echoChar(char c)
    {
       return c;
    }

    public boolean echoBoolean(boolean b)
    {
       return b;
    }

    public Character echoCharacter(Character c)
    {
       return c;
    }

    public Boolean echoBooleanAsObject(Boolean b)
    {
       return b;
    }

    @PrePassivate
    public void beforePassivating()
    {
       InterceptorTestLogger.add(FootballTeam.class, "prePassivating");
    }

    @PostActivate
    public void afterActivating()
    {
       InterceptorTestLogger.add(FootballTeam.class, "postActivating");
    }

    @AroundInvoke
    public Object itsMe(InvocationContext invocationContext) throws Exception {
        InterceptorTestLogger.add(FootballTeam.class, "aroundInvokeBefore");
        Object result = invocationContext.proceed();
        InterceptorTestLogger.add(FootballTeam.class, "aroundInvokeAfter");
        return result;
    }

   @PostConstruct
   public void afterConstruction()
   {
      InterceptorTestLogger.add(FootballTeam.class, "postConstruct");
   }

   public final void doNothing()
   {
      int i = 0;
      i++;
   }
}

