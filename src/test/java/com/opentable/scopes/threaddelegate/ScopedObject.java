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

import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Provider;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
// Sample scoped object
// Keep an atomic counter of how many times perform()  is called
public class ScopedObject
{
    private final AtomicInteger performances = new AtomicInteger();

    public ScopedObject()
    {
    }

    public void perform()
    {
        performances.incrementAndGet();
    }

    public int getPerformances()
    {
        return performances.intValue();
    }

    // Tracks how many handouts there are, eg how many times this provider is called.
    public static class TestObjectProvider implements Provider<ScopedObject>
    {
        private static final AtomicInteger HANDOUTS = new AtomicInteger();

        @Override
        public ScopedObject get()
        {
            HANDOUTS.incrementAndGet();
            return new ScopedObject();
        }

        public static int getHandouts()
        {
            return HANDOUTS.intValue();
        }

        public static void reset()
        {
            HANDOUTS.set(0);
        }
    }

    @Configuration
    public static class ScopedObjectConfiguration {
        // Mark bean so its created in the appropriate scope
        @Bean
        @Scope(ThreadDelegatedContext.SCOPE_THREAD_DELEGATED)
        public ScopedObject getScopedObject() {
            return new TestObjectProvider().get();
        }
    }
}
