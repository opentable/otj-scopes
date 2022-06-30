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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

/**
 * This is the context object for the scope. All members of the context object can potentially
 * be shared between objects, so they should be thread safe.
 */
public class ThreadDelegatedContext
{
    private static final String NULL_NAME = "name must not be null!";

    public static final String SCOPE_THREAD_DELEGATED = "thread_delegated";

    private final Map<String, Object> contents = new HashMap<>();
    private final Set<ScopeListener> listeners = new HashSet<>();

    ThreadDelegatedContext()
    {
    }

    synchronized boolean containsKey(final String name)
    {
        Preconditions.checkArgument(name != null, NULL_NAME);
        return contents.containsKey(name);
    }

    @SuppressWarnings("unchecked")
    synchronized <T> T get(final String name)
    {
        Preconditions.checkArgument(name != null, NULL_NAME);
        return (T) contents.get(name);
    }

    synchronized void put(final String name, final Object value)
    {
        Preconditions.checkArgument(name != null, NULL_NAME);
        contents.put(name, value);

        if (value instanceof ScopeListener) {
            final ScopeListener listener = ScopeListener.class.cast(value);
            listeners.add(listener);
            // Send an "enter" event to notify the listener that it was put in scope.
            listener.event(ScopeEvent.ENTER);
        }
    }

    @SuppressWarnings("unchecked")
    synchronized <T> T remove(final String name)
    {
        Preconditions.checkArgument(name != null, NULL_NAME);
        return (T) contents.remove(name);
    }

    @VisibleForTesting
    synchronized void clear()
    {
        event(ScopeEvent.LEAVE);
        listeners.clear();
        contents.clear();
    }

    @VisibleForTesting
    synchronized int size()
    {
        return contents.size();
    }

    synchronized void event(final ScopeEvent event)
    {
        for (ScopeListener listener: listeners) {
            listener.event(event);
        }
    }

    /**
     * Objects put in the ThreadDelegated scope can implement this interface to be notified when
     * they are moved from one thread to another.
     */
    public interface ScopeListener
    {
        void event(ScopeEvent event);
    }

    public enum ScopeEvent
    {
        ENTER,
        LEAVE;
    }
}
