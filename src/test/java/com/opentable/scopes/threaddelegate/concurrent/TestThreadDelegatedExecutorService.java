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
package com.opentable.scopes.threaddelegate.concurrent;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.opentable.scopes.threaddelegate.ScopedObject;
import com.opentable.scopes.threaddelegate.ThreadDelegatedScope;
import com.opentable.scopes.threaddelegate.ThreadDelegatedScopeModule;
import com.opentable.scopes.threaddelegate.ScopedObject.TestObjectProvider;
import com.opentable.scopes.threaddelegate.concurrent.ThreadDelegatingDecorator;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.inject.servlet.GuiceFilter;

public class TestThreadDelegatedExecutorService
{
    @Inject
    private Injector injector = null;

    private ThreadPoolExecutor unwrappedExecutor = null;


    @Before
    public void setUp()
    {
        ThreadDelegatedScope.SCOPE.changeScope(null);

        final Injector injector = Guice.createInjector(Stage.PRODUCTION,
                                                       new ThreadDelegatedScopeModule(),
                                                       new AbstractModule() {
            @Override
            public void configure() {
                bind(ScopedObject.class).toProvider(TestObjectProvider.class).in(ThreadDelegatedScope.SCOPE);
            }
        });

        injector.injectMembers(this);
        Assert.assertNotNull(injector);

        unwrappedExecutor = new ThreadPoolExecutor(5, 5, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        Assert.assertFalse(unwrappedExecutor.isShutdown());
        Assert.assertFalse(unwrappedExecutor.isTerminated());
    }

    @After
    public void tearDown()
    {
        Assert.assertNotNull(unwrappedExecutor);
        unwrappedExecutor.shutdown();
        unwrappedExecutor = null;

        Assert.assertNotNull(injector);
        // Get rid of the stupid "duplicate Servlet module warning"
        final GuiceFilter filter = injector.getInstance(GuiceFilter.class);
        filter.destroy();
        injector = null;

        ThreadDelegatedScope.SCOPE.changeScope(null);
    }

    @Test
    public void testUnwrappedExecutor() throws Exception
    {
        final ScopedObject t1 = injector.getInstance(ScopedObject.class);
        Assert.assertNotNull(t1);

        final ScopedCallable callable = new ScopedCallable();
        final Future<ScopedObject> future = unwrappedExecutor.submit(callable);

        Assert.assertNotSame(t1, future.get());
    }

    @Test
    public void testWrappedExecutor() throws Exception
    {
        final ScopedObject t1 = injector.getInstance(ScopedObject.class);
        Assert.assertNotNull(t1);

        final ScopedCallable callable = new ScopedCallable();
        final Future<ScopedObject> future = ThreadDelegatingDecorator.wrapExecutorService(unwrappedExecutor).submit(callable);

        Assert.assertSame(t1, future.get());
    }

    @Test
    public void testMultipleUnwrappedExecutor() throws Exception
    {
        final ScopedObject t1 = injector.getInstance(ScopedObject.class);
        Assert.assertNotNull(t1);

        final int testCount = 10;

        @SuppressWarnings("unchecked")
        final Future<ScopedObject> [] futures = new Future[testCount];

        for (int i = 0; i < testCount; i++) {
            futures[i] = unwrappedExecutor.submit(new ScopedCallable());
        }

        for (int i  = 0; i < testCount; i++) {
            Assert.assertNotSame(t1, futures[i].get());
        }
    }

    @Test
    public void testMultipleWrappedExecutor() throws Exception
    {
        final ScopedObject t1 = injector.getInstance(ScopedObject.class);
        Assert.assertNotNull(t1);

        final int testCount = 10;

        @SuppressWarnings("unchecked")
        final Future<ScopedObject> [] futures = new Future[testCount];

        final ExecutorService wrappedExecutorService = ThreadDelegatingDecorator.wrapExecutorService(unwrappedExecutor);

        for (int i = 0; i < testCount; i++) {
            futures[i] = wrappedExecutorService.submit(new ScopedCallable());
        }

        for (int i  = 0; i < testCount; i++) {
            Assert.assertSame(t1, futures[i].get());
        }
    }

    @Test
    public void testAllTogetherNow() throws Exception
    {
        final ScopedObject t1 = injector.getInstance(ScopedObject.class);
        Assert.assertNotNull(t1);

        final int testCount = 10;

        final List<ScopedCallable> callables = Lists.newArrayList();
        for (int i = 0; i < testCount; i++) {
            callables.add(new ScopedCallable());
        }

        final ExecutorService wrappedExecutorService = ThreadDelegatingDecorator.wrapExecutorService(unwrappedExecutor);

        final List<Future<ScopedObject>> futures = wrappedExecutorService.invokeAll(callables);

        for (Future<ScopedObject> future : futures) {
            Assert.assertSame(t1, future.get());
        }
    }


    public class ScopedCallable implements Callable<ScopedObject>
    {
        public ScopedCallable()
        {
        }

        @Override
        public ScopedObject call()
        {
            return injector.getInstance(ScopedObject.class);
        }
    }
}

