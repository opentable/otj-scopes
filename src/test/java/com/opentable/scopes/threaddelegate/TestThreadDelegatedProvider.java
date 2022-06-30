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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.ObjectFactory;

import com.opentable.scopes.threaddelegate.ScopedObject.TestObjectProvider;

// Shows you can stick a provider in, and scope changes work right, and handouts are as expected
public class TestThreadDelegatedProvider
{
    private ThreadDelegatedScope scope = null;

    private final String fooName = "foo";

    @Before
    public void setUp()
    {
        TestObjectProvider.reset();
        Assert.assertNull(scope);
        this.scope = new ThreadDelegatedScope();
    }

    @After
    public void tearDown()
    {
        TestObjectProvider.reset();
        Assert.assertNotNull(scope);
        this.scope = null;
    }

    // This provider is not scoped, so we expect it to return 2 separate items,
    @Test
    public void testUnscopedProvider()
    {
        final ObjectFactory<ScopedObject> unscopedProvider = new TestObjectProvider();

        final ScopedObject t1 = unscopedProvider.getObject();
        Assert.assertNotNull(t1);

        final ScopedObject t2 = unscopedProvider.getObject();
        Assert.assertNotNull(t2);

        Assert.assertNotSame(t1, t2);
        // God knows why Steven made this static, but I guess we'll find out...
        Assert.assertEquals(2, TestObjectProvider.getHandouts());
    }

    @Test
    public void testSimpleProvider()
    {
        // Get the provider
        final ObjectFactory<ScopedObject> scopedProvider = scope.provider(fooName, new TestObjectProvider());
        Assert.assertNotNull(scopedProvider);

        // Get a ScopedObject from it
        final ScopedObject t1 = scopedProvider.getObject();
        Assert.assertNotNull(t1);

        // And a second
        final ScopedObject t2 = scopedProvider.getObject();
        Assert.assertNotNull(t2);

        // They remain the same
        Assert.assertSame(t1, t2);
        // It was cached
        Assert.assertEquals(1, TestObjectProvider.getHandouts());
    }

    // We get an object from the Provider, like the previous test,
    // We then clear the scope, and get an object.
    // Since the context was cleared, we expect a new one
    @Test
    public void testScopeChange()
    {
        final ObjectFactory<ScopedObject> scopedProvider = scope.provider(fooName, new TestObjectProvider());
        Assert.assertNotNull(scopedProvider);

        final ScopedObject t1 = scopedProvider.getObject();
        Assert.assertNotNull(t1);

        scope.changeScope(null);

        final ScopedObject t2 = scopedProvider.getObject();
        Assert.assertNotNull(t2);

        Assert.assertNotSame(t1, t2);
        Assert.assertEquals(2, TestObjectProvider.getHandouts());
    }

    // get the provider
    // Grab and object
    // change the scope and get the object again.
    // Like the previous test it's not the same
    // NOW,change back to the previous context, and the orginal object should be there
    @Test
    public void testScopeHandoff()
    {
        final ObjectFactory<ScopedObject> scopedProvider = scope.provider(fooName, new TestObjectProvider());
        Assert.assertNotNull(scopedProvider);

        final ScopedObject t1 = scopedProvider.getObject();
        Assert.assertNotNull(t1);

        final ThreadDelegatedContext plate = scope.getContext();

        scope.changeScope(null);

        final ScopedObject t2 = scopedProvider.getObject();
        Assert.assertNotNull(t2);

        Assert.assertNotSame(t1, t2);

        scope.changeScope(plate);

        final ScopedObject t3 = scopedProvider.getObject();
        Assert.assertNotNull(t3);

        Assert.assertSame(t1, t3);
        Assert.assertNotSame(t2, t3);
        Assert.assertEquals(2, TestObjectProvider.getHandouts());
    }
}
