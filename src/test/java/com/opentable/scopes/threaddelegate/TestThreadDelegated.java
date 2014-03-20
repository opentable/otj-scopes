/**
 * Copyright (C) 2012 Ness Computing, Inc.
 *
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

import com.opentable.scopes.threaddelegate.ThreadDelegated;
import com.opentable.scopes.threaddelegate.ThreadDelegatedContext;
import com.opentable.scopes.threaddelegate.ThreadDelegatedScope;
import com.opentable.scopes.threaddelegate.ThreadDelegatedScopeModule;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Stage;
import com.google.inject.servlet.GuiceFilter;

public class TestThreadDelegated
{
    private Injector injector = null;

    private static final AtomicInteger HANDED_OUT = new AtomicInteger();

    @Before
    public void setUp()
    {
        ThreadDelegatedScope.SCOPE.changeScope(null);
        HANDED_OUT.set(0);
    }

    @After
    public void tearDown()
    {
        if (injector != null) {
            // Get rid of the stupid "duplicate Servlet module warning"
            final GuiceFilter filter = injector.getInstance(GuiceFilter.class);
            filter.destroy();
        }
        ThreadDelegatedScope.SCOPE.changeScope(null);
    }

    @Test
    public void testScopedObject()
    {
        injector = Guice.createInjector(Stage.PRODUCTION,
                                        new ThreadDelegatedScopeModule(),
                                        new ScopedModule());


        final ThreadDelegatedScope scope = injector.getInstance(ThreadDelegatedScope.class);
        Assert.assertNotNull(scope);

        final ScopedObject t1 = injector.getInstance(ScopedObject.class);
        final ScopedObject t2 = injector.getInstance(ScopedObject.class);
        Assert.assertNotNull(t1);
        Assert.assertNotNull(t2);
        Assert.assertSame(t1, t2);
    }

    @Test
    public void testScopeChange()
    {
        injector = Guice.createInjector(Stage.PRODUCTION,
                                        new ThreadDelegatedScopeModule(),
                                        new ScopedModule());

        final ThreadDelegatedScope scope = injector.getInstance(ThreadDelegatedScope.class);
        Assert.assertNotNull(scope);

        final ScopedObject t1 = injector.getInstance(ScopedObject.class);
        Assert.assertNotNull(t1);

        scope.changeScope(null);

        final ScopedObject t2 = injector.getInstance(ScopedObject.class);
        Assert.assertNotNull(t2);

        Assert.assertNotSame(t1, t2);
    }

    @Test
    public void testScopeHandoff()
    {
        injector = Guice.createInjector(Stage.PRODUCTION,
                                        new ThreadDelegatedScopeModule(),
                                        new ScopedModule());

        final ThreadDelegatedScope scope = injector.getInstance(ThreadDelegatedScope.class);
        Assert.assertNotNull(scope);

        final ScopedObject t1 = injector.getInstance(ScopedObject.class);
        Assert.assertNotNull(t1);

        final ThreadDelegatedContext plate = scope.getContext();

        scope.changeScope(null);

        final ScopedObject t2 = injector.getInstance(ScopedObject.class);
        Assert.assertNotNull(t2);

        Assert.assertNotSame(t1, t2);

        scope.changeScope(plate);

        final ScopedObject t3 = injector.getInstance(ScopedObject.class);
        Assert.assertNotNull(t3);

        Assert.assertSame(t1, t3);
        Assert.assertNotSame(t2, t3);
    }

    @Test
    public void testThreaded() throws Exception
    {
        injector = Guice.createInjector(Stage.PRODUCTION,
                                        new ThreadDelegatedScopeModule(),
                                        new ScopedModule());

        final ScopedObject testObject = injector.getInstance(ScopedObject.class);

        int threadCount = 10;
        final CountDownLatch latch = new CountDownLatch(threadCount);
        for (int i = 0 ; i < threadCount; i++) {
            new Thread(new Runnable() {

                @Override
                public void run()
                {
                    final ScopedObject testObject = injector.getInstance(ScopedObject.class);
                    Assert.assertEquals(0, testObject.getPerformances());
                    testObject.perform();
                    latch.countDown();
                }

            }).start();

        }

        Assert.assertTrue("Some threads got stuck!", latch.await(1, TimeUnit.SECONDS));

        Assert.assertEquals(threadCount + 1, HANDED_OUT.get());
        Assert.assertEquals(0, testObject.getPerformances());
    }

    @Test
    public void testThreadHandover() throws Exception
    {
        injector = Guice.createInjector(Stage.PRODUCTION,
                                        new ThreadDelegatedScopeModule(),
                                        new ScopedModule());

        final ScopedObject testObject = injector.getInstance(ScopedObject.class);
        Assert.assertEquals(1, HANDED_OUT.get());

        final ThreadDelegatedScope scope = injector.getInstance(ThreadDelegatedScope.class);
        Assert.assertNotNull(scope);

        int threadCount = 10;

        final ThreadDelegatedContext parentPlate = scope.getContext();

        final CountDownLatch latch = new CountDownLatch(threadCount);
        for (int i = 0 ; i < threadCount; i++) {
            new Thread(new Runnable() {

                @Override
                public void run()
                {
                    scope.changeScope(parentPlate);
                    final ScopedObject testObject = injector.getInstance(ScopedObject.class);
                    testObject.perform();
                    scope.changeScope(null);
                    latch.countDown();
                }

            }).start();

        }

        Assert.assertTrue("Some threads got stuck!", latch.await(1, TimeUnit.SECONDS));

        Assert.assertEquals(1, HANDED_OUT.get());
        Assert.assertEquals(threadCount, testObject.getPerformances());
    }

    public static class ScopedModule implements Module
    {
        @Override
        public void configure(final Binder binder)
        {
        }

        @Provides
        @ThreadDelegated
        public ScopedObject getTestObject()
        {
            HANDED_OUT.incrementAndGet();
            return new ScopedObject();
        }
    }
}
