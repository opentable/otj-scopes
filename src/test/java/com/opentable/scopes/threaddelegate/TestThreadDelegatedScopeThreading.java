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

import javax.inject.Provider;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.opentable.scopes.threaddelegate.ScopedObject.TestObjectProvider;

public class TestThreadDelegatedScopeThreading
{
    private ThreadDelegatedScope scope = null;

    private final String fooName = "foo";

    @Before
    public void setUp()
    {
        Assert.assertNull(scope);
        this.scope = new ThreadDelegatedScope();
        TestObjectProvider.reset();
        scope.changeScope(null);
    }

    @After
    public void tearDown()
    {
        Assert.assertNotNull(scope);
        scope.changeScope(null);
        this.scope = null;
    }

    @Test
    public void testThreaded() throws Exception
    {
        final TestObjectProvider unscopedProvider = new TestObjectProvider();
        final Provider<ScopedObject> scopedProvider = scope.provider(fooName, unscopedProvider);

        int threadCount = 10;
        final CountDownLatch latch = new CountDownLatch(threadCount);
        for (int i = 0 ; i < threadCount; i++) {
            new Thread(new Runnable() {

                @Override
                public void run()
                {
                    final ScopedObject testObject = scopedProvider.get();
                    Assert.assertEquals(0, testObject.getPerformances());
                    testObject.perform();
                    latch.countDown();
                }

            }).start();
        }

        Assert.assertTrue("Some threads got stuck!", latch.await(1, TimeUnit.SECONDS));

        Assert.assertEquals(threadCount, TestObjectProvider.getHandouts());
        Assert.assertEquals(0, scopedProvider.get().getPerformances());
    }

    @Test
    public void testThreadHandover() throws Exception
    {
        final TestObjectProvider unscopedProvider = new TestObjectProvider();
        final Provider<ScopedObject> scopedProvider = scope.provider(fooName, unscopedProvider);

        int threadCount = 10;

        final ThreadDelegatedContext parentPlate = scope.getContext();

        final CountDownLatch latch = new CountDownLatch(threadCount);
        for (int i = 0 ; i < threadCount; i++) {
            new Thread(new Runnable() {

                @Override
                public void run()
                {
                    scope.changeScope(parentPlate);
                    final ScopedObject testObject = scopedProvider.get();
                    testObject.perform();
                    scope.changeScope(null);
                    latch.countDown();
                }

            }).start();
        }

        Assert.assertTrue("Some threads got stuck!", latch.await(1, TimeUnit.SECONDS));

        Assert.assertEquals(1, TestObjectProvider.getHandouts());
        Assert.assertEquals(threadCount, scopedProvider.get().getPerformances());
    }
}
