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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.opentable.scopes.threaddelegate.ScopedObject.TestObjectProvider;

public class TestThreadDelegatedScopeConfig
{
    @Before
    public void setUp()
    {
        ThreadDelegatedScope.SCOPE.changeScope(null);
        TestObjectProvider.reset();
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
    public void testSimple()
    {
        final BeanFactory factory = getSimpleBeanFactory();
        final ThreadDelegatedScope scope = factory.getBean(ThreadDelegatedScope.class);
        Assert.assertNotNull(scope);

        // This is important because the executor decorators rely on the object
        // available for injection is the same as the constant!
        Assert.assertSame(ThreadDelegatedScope.SCOPE, scope);
    }

    @Test
    public void testScopedObject()
    {
        final BeanFactory factory = getScopedObjectBeanFactory();

        final ThreadDelegatedScope scope = factory.getBean(ThreadDelegatedScope.class);
        Assert.assertNotNull(scope);

        final ScopedObject t1 = factory.getBean(ScopedObject.class);
        final ScopedObject t2 = factory.getBean(ScopedObject.class);
        Assert.assertNotNull(t1);
        Assert.assertNotNull(t2);
        Assert.assertSame(t1, t2);
    }

    @Test
    public void testScopeChange()
    {
        final BeanFactory factory = getScopedObjectBeanFactory();

        final ThreadDelegatedScope scope = factory.getBean(ThreadDelegatedScope.class);
        Assert.assertNotNull(scope);

        final ScopedObject t1 = factory.getBean(ScopedObject.class);
        Assert.assertNotNull(t1);

        scope.changeScope(null);

        final ScopedObject t2 = factory.getBean(ScopedObject.class);
        Assert.assertNotNull(t2);

        Assert.assertNotSame(t1, t2);
    }

    @Test
    public void testScopeHandoff()
    {
        final BeanFactory factory = getScopedObjectBeanFactory();

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

    @Test
    public void testThreaded() throws Exception
    {
        final BeanFactory factory = getScopedObjectBeanFactory();

        final ScopedObject testObject = factory.getBean(ScopedObject.class);

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

        Assert.assertEquals(threadCount + 1, TestObjectProvider.getHandouts());
        Assert.assertEquals(0, testObject.getPerformances());
    }

    @Test
    public void testThreadHandover() throws Exception
    {
        final BeanFactory factory = getScopedObjectBeanFactory();

        final ScopedObject testObject = factory.getBean(ScopedObject.class);
        Assert.assertEquals(1, TestObjectProvider.getHandouts());

        final ThreadDelegatedScope scope = factory.getBean(ThreadDelegatedScope.class);
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

        Assert.assertEquals(1, TestObjectProvider.getHandouts());
        Assert.assertEquals(threadCount, testObject.getPerformances());
    }

    private static BeanFactory getSimpleBeanFactory() {
        return new AnnotationConfigApplicationContext(ThreadDelegatedScopeConfig.class).getAutowireCapableBeanFactory();
    }

    private static BeanFactory getScopedObjectBeanFactory() {
        final ApplicationContext context =
                new AnnotationConfigApplicationContext(ThreadDelegatedScopeConfig.class, ScopedObject.Config.class);
        return context.getAutowireCapableBeanFactory();
    }

}
