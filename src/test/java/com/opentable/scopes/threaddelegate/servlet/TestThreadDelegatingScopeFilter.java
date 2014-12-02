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
package com.opentable.scopes.threaddelegate.servlet;

import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.opentable.scopes.threaddelegate.ScopedObject;
import com.opentable.scopes.threaddelegate.ThreadDelegatedScope;
import com.opentable.scopes.threaddelegate.ThreadDelegatedScopeModule;
import com.opentable.scopes.threaddelegate.ScopedObject.TestObjectProvider;
import com.opentable.scopes.threaddelegate.servlet.ThreadDelegatingScopeFilter;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.inject.servlet.GuiceFilter;

public class TestThreadDelegatingScopeFilter
{
    @Inject
    private ThreadDelegatingScopeFilter filter = null;

    @Inject
    private Injector injector = null;

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
        Assert.assertNotNull(filter);
        Assert.assertNotNull(injector);
    }

    @After
    public void tearDown()
    {
        Assert.assertNotNull(filter);
        filter = null;

        Assert.assertNotNull(injector);
        // Get rid of the stupid "duplicate Servlet module warning"
        final GuiceFilter filter = injector.getInstance(GuiceFilter.class);
        filter.destroy();
        injector = null;

        ThreadDelegatedScope.SCOPE.changeScope(null);
    }


    @Test
    public void testSimpleFilter() throws Exception
    {
        final ScopedObject t1 = injector.getInstance(ScopedObject.class);
        Assert.assertNotNull(t1);

        final AtomicReference<ScopedObject> refHolder = new AtomicReference<ScopedObject>();

        HttpServletRequest request = EasyMock.createNiceMock(HttpServletRequest.class);
        EasyMock.replay(request);

        filter.doFilter(request, null, new FilterChain() {
            @Override
            public void doFilter(ServletRequest req, ServletResponse res) {
                final ScopedObject t2 = injector.getInstance(ScopedObject.class);
                Assert.assertNotNull(t2);
                refHolder.set(t2);
            }
        });

        final ScopedObject t2 = refHolder.get();
        Assert.assertNotNull(t2);
        final ScopedObject t3 = injector.getInstance(ScopedObject.class);
        Assert.assertNotNull(t3);

        Assert.assertNotSame(t1, t2);
        Assert.assertNotSame(t1, t3);
        Assert.assertNotSame(t2, t3);

        EasyMock.verify(request);
    }
}
