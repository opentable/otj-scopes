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

import javax.servlet.http.HttpServletRequest;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.opentable.scopes.threaddelegate.ScopedObject;
import com.opentable.scopes.threaddelegate.ThreadDelegatedScope;
import com.opentable.scopes.threaddelegate.ThreadDelegatedScopeConfiguration;

public class TestThreadDelegatingScopeFilter
{
    @Autowired
    private ThreadDelegatingScopeFilter filter = null;

    @Autowired
    private ApplicationContext context = null;

    @Before
    public void setUp()
    {
        // Clean out the scope
        ThreadDelegatedScope.SCOPE.changeScope(null);

        // Instantiate spring manually, and add the ScopedObject
        final ApplicationContext context =
                new AnnotationConfigApplicationContext(ThreadDelegatedScopeConfiguration.class,
                        ScopedObject.ScopedObjectConfiguration.class);
        final AutowireCapableBeanFactory factory = context.getAutowireCapableBeanFactory();
        factory.autowireBean(this);
        Assert.assertNotNull(filter);
        Assert.assertNotNull(this.context);
    }

    @After
    public void tearDown()
    {
        Assert.assertNotNull(filter);
        filter = null;

        Assert.assertNotNull(context);
        context = null;

        ThreadDelegatedScope.SCOPE.changeScope(null);
    }

    @Test
    public void testSimpleFilter() throws Exception
    {
        // We get the object
        final ScopedObject t1 = getBean(ScopedObject.class);
        Assert.assertNotNull(t1);

        final AtomicReference<ScopedObject> refHolder = new AtomicReference<>();

        HttpServletRequest request = EasyMock.createNiceMock(HttpServletRequest.class);
        EasyMock.replay(request);

        // Call the filter
        filter.doFilter(request, null, (req, res) ->
        {
            // Get a scoped object. Before and after, the context will be cleared
            final ScopedObject t2 = getBean(ScopedObject.class);
            Assert.assertNotNull(t2);
            refHolder.set(t2);
        });

        final ScopedObject t2 = refHolder.get();
        Assert.assertNotNull(t2);
        final ScopedObject t3 = getBean(ScopedObject.class);
        Assert.assertNotNull(t3);

        // t1 and t2 are NOT the same, because of the cleared context
        // t1 created
        // clear
        // t2 created
        // clear
        // t3 created
        Assert.assertNotSame(t1, t2);
        Assert.assertNotSame(t1, t3);
        Assert.assertNotSame(t2, t3);
        EasyMock.verify(request);
    }

    private <T> T getBean(Class<T> cls)
    {
        return context.getAutowireCapableBeanFactory().getBean(cls);
    }
}
