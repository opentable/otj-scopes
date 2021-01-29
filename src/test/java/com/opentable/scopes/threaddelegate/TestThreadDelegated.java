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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

// We are using the "real" Spring singleton instance of the Scope here.
// A manually instantiated scope version of these tests is in TestThreadDelegatedScopeThreading
public class TestThreadDelegated
{
    private static final AtomicInteger HANDED_OUT = new AtomicInteger();

    @Before
    public void setUp()
    {
        // Clear anything in the context
        ThreadDelegatedScope.SCOPE.changeScope(null);
        HANDED_OUT.set(0);
    }

    @After
    public void tearDown()
    {
        ThreadDelegatedScope.SCOPE.changeScope(null);
    }


    // Get the Scope, and show it returns the same ScopedObject repeatedly
    @Test
    public void testScopedObject()
    {
        final BeanFactory factory = getFactory();

        final ThreadDelegatedScope scope = factory.getBean(ThreadDelegatedScope.class);
        Assert.assertNotNull(scope);

        final ScopedObject t1 = factory.getBean(ScopedObject.class);
        final ScopedObject t2 = factory.getBean(ScopedObject.class);
        Assert.assertNotNull(t1);
        Assert.assertNotNull(t2);
        Assert.assertSame(t1, t2);
    }

    // Get the Scope, and show once we change scope, the object changes
    @Test
    public void testScopeChange()
    {
        final BeanFactory factory = getFactory();

        final ThreadDelegatedScope scope = factory.getBean(ThreadDelegatedScope.class);
        Assert.assertNotNull(scope);

        final ScopedObject t1 = factory.getBean(ScopedObject.class);
        Assert.assertNotNull(t1);

        scope.changeScope(null);

        final ScopedObject t2 = factory.getBean(ScopedObject.class);
        Assert.assertNotNull(t2);

        Assert.assertNotSame(t1, t2);
    }

    // Get the Scope, change the scope, and the object changes as previously
    // Change back and its the same as the original object
    @Test
    public void testScopeHandoff()
    {
        final BeanFactory factory = getFactory();

        final ThreadDelegatedScope scope = factory.getBean(ThreadDelegatedScope.class);
        Assert.assertNotNull(scope);

        final ScopedObject t1 = factory.getBean(ScopedObject.class);
        Assert.assertNotNull(t1);

        final ThreadDelegatedContext plate = scope.getContext();

        scope.changeScope(null);

        final ScopedObject t2 = factory.getBean(ScopedObject.class);
        Assert.assertNotNull(t2);

        Assert.assertNotSame(t1, t2);

        scope.changeScope(plate);

        final ScopedObject t3 = factory.getBean(ScopedObject.class);
        Assert.assertNotNull(t3);

        Assert.assertSame(t1, t3);
        Assert.assertNotSame(t2, t3);
    }

    // Our first threaded test!
    // Proves the basic thesis that objects in parent and all child threads will be independent.
    @Test
    public void testThreaded() throws Exception
    {
        final BeanFactory factory = getFactory();

        final ScopedObject testObject = factory.getBean(ScopedObject.class);
        Assert.assertEquals(1, HANDED_OUT.get());

        int threadCount = 10;
        // Launch 10 threads. We'll use the same Scope object, but we expect the context
        // to be different in different threads
        final CountDownLatch latch = new CountDownLatch(threadCount);
        for (int i = 0 ; i < threadCount; i++) {
            new Thread(() ->
            {
                // Hence this object will be different in each thread
                final ScopedObject testObject2 = factory.getBean(ScopedObject.class);
                Assert.assertEquals(0, testObject2.getPerformances());
                testObject2.perform();
                latch.countDown();
            }).start();

        }

        Assert.assertTrue("Some threads got stuck!", latch.await(1, TimeUnit.SECONDS));


        Assert.assertEquals(threadCount + 1, HANDED_OUT.get());
        // This was in the main thread, so shouldn't increment.
        Assert.assertEquals(0, testObject.getPerformances());
    }


    // This is similar to the previous test, but shows that if we
    // manually switch back to original context, the same object is handed out and mutated
    @Test
    public void testThreadHandover() throws Exception
    {
        final BeanFactory factory = getFactory();

        final ScopedObject testObject = factory.getBean(ScopedObject.class);
        Assert.assertEquals(1, HANDED_OUT.get());

        final ThreadDelegatedScope scope = factory.getBean(ThreadDelegatedScope.class);
        Assert.assertNotNull(scope);

        int threadCount = 10;

        final ThreadDelegatedContext parentPlate = scope.getContext();

        final CountDownLatch latch = new CountDownLatch(threadCount);
        for (int i = 0 ; i < threadCount; i++) {
            new Thread(() ->
            {
                scope.changeScope(parentPlate);
                final ScopedObject testObject2 = factory.getBean(ScopedObject.class);
                testObject2.perform();
                Assert.assertSame(testObject2, testObject);
                scope.changeScope(null);
                latch.countDown();
            }).start();
        }

        Assert.assertTrue("Some threads got stuck!", latch.await(1, TimeUnit.SECONDS));

        Assert.assertEquals(1, HANDED_OUT.get());
        Assert.assertEquals(threadCount, testObject.getPerformances());
    }


    // We are injecting a single object into the scope.
    // Our expectations would be it will be cached and only thread changes or changeContext would change that
    // We'd also expect only when changeContext or threead changes would HANDED_OUT increment
    @Configuration
    public static class ScopedConfig
    {
        @Bean
        @Scope(ThreadDelegatedContext.SCOPE_THREAD_DELEGATED)
        public ScopedObject getTestObject()
        {
            HANDED_OUT.incrementAndGet();
            return new ScopedObject();
        }
    }

    private static BeanFactory getFactory() {
        final ApplicationContext context =
                new AnnotationConfigApplicationContext(ThreadDelegatedScopeConfiguration.class, ScopedConfig.class);
        return context.getAutowireCapableBeanFactory();
    }
}
