/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.opentable.scopes.threaddelegate;

import javax.annotation.Nullable;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

import com.opentable.scopes.threaddelegate.ThreadDelegatedContext.ScopeEvent;

/**
 * Maintains the global state for the ThreadDelegatedScope.
 */
public class ThreadDelegatedScope implements Scope
{
    private final ThreadLocal<ThreadDelegatedContext> threadLocal;

    /** The global scope object to bind. This is created at load time of this class. */
    public static final ThreadDelegatedScope SCOPE = new ThreadDelegatedScope();

    ThreadDelegatedScope()
    {
        this.threadLocal = new ThreadLocal<>();
    }

    /**
     * Returns the context (the set of objects bound to the scope) for the current thread.
     * A context may be shared by multiple threads.
     */
    public ThreadDelegatedContext getContext()
    {
        ThreadDelegatedContext context = threadLocal.get();
        if (context == null) {
            context = new ThreadDelegatedContext();
            threadLocal.set(context);
        }
        return context;
    }

    /**
     * A thread enters the scope. Clear the current context. If a new context
     * was given, assign it to the scope, otherwise leave it empty.
     */
    public void changeScope(@Nullable final ThreadDelegatedContext context)
    {
        final ThreadDelegatedContext oldContext = threadLocal.get();
        if (oldContext != null) {
            if (oldContext == context) {
                // If the context gets exchanged with itself, do nothing.
                return;
            }
            else {
                // This must not clear the context. It might still be
                // referenced by another thread.
                oldContext.event(ScopeEvent.LEAVE);
            }
        }

        if (context != null) {
            threadLocal.set(context);
            context.event(ScopeEvent.ENTER);
        }
        else {
            threadLocal.remove();
        }
    }

    @Override
    public Object get(String name, ObjectFactory<?> objectFactory)
    {
        return provider(name, objectFactory::getObject).getObject();
    }

    @Override
    public String getConversationId()
    {
        return Thread.currentThread().getName();
    }

    @Override
    public void registerDestructionCallback(String name, Runnable callback)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object remove(String name)
    {
        return getContext().remove(name);
    }

    @Override
    public Object resolveContextualObject(String key)
    {
        return null;
    }

    @VisibleForTesting
    <T> ThreadDelegatedScopeProvider<T> provider(final String name, final ObjectFactory<T> unscoped)
    {
        return new ThreadDelegatedScopeProvider<>(name, unscoped);
    }

    public class ThreadDelegatedScopeProvider<T> implements ObjectFactory<T>
    {
        private final String name;
        private final ObjectFactory<T> unscoped;

        public ThreadDelegatedScopeProvider(final String name, final ObjectFactory<T> unscoped)
        {
            Preconditions.checkArgument(name != null, "key must not be null!");
            Preconditions.checkArgument(unscoped != null, "unscoped provider must not be null!");

            this.name = name;
            this.unscoped = unscoped;
        }

        @Override
        public T getObject()
        {
            final ThreadDelegatedContext context = getContext();
            // This must be synchronized around the context, because otherwise
            // multiple threads will try to set the same value at the same time.
            synchronized(context) {
                if (context.containsKey(name)) {
                    return context.get(name);
                }
                else {
                    final T value = unscoped.getObject();
                    context.put(name, value);
                    return value;
                }
            }
        }

        private volatile String toString = null;

        @Override
        public synchronized String toString()
        {
            if (toString == null) {
                toString = String.format("ThreadDelegatedScoped provider (name: %s) of %s", name, unscoped.toString());
            }
            return toString;
        }
    }
}
