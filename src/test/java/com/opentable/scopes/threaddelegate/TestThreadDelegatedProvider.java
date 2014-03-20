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


import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.opentable.scopes.threaddelegate.ThreadDelegatedContext;
import com.opentable.scopes.threaddelegate.ThreadDelegatedScope;
import com.opentable.scopes.threaddelegate.ScopedObject.TestObjectProvider;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.name.Names;

public class TestThreadDelegatedProvider
{
    private ThreadDelegatedScope scope = null;

    private final Key<ScopedObject> fooTestObjectKey = Key.get(ScopedObject.class, Names.named("foo"));

    @Before
    public void setUp()
    {
        Assert.assertNull(scope);
        this.scope = new ThreadDelegatedScope();
    }

    @After
    public void tearDown()
    {
        Assert.assertNotNull(scope);
        this.scope = null;
    }

    @Test
    public void testUnscopedProvider()
    {
        final Provider<ScopedObject> unscopedProvider = new TestObjectProvider();

        final ScopedObject t1 = unscopedProvider.get();
        Assert.assertNotNull(t1);

        final ScopedObject t2 = unscopedProvider.get();
        Assert.assertNotNull(t2);

        Assert.assertNotSame(t1, t2);
    }

    @Test
    public void testSimpleProvider()
    {
        final Provider<ScopedObject> scopedProvider = scope.scope(fooTestObjectKey, new TestObjectProvider());
        Assert.assertNotNull(scopedProvider);

        final ScopedObject t1 = scopedProvider.get();
        Assert.assertNotNull(t1);

        final ScopedObject t2 = scopedProvider.get();
        Assert.assertNotNull(t2);

        Assert.assertSame(t1, t2);
    }

    @Test
    public void testScopeChange()
    {
        final Provider<ScopedObject> scopedProvider = scope.scope(fooTestObjectKey, new TestObjectProvider());
        Assert.assertNotNull(scopedProvider);

        final ScopedObject t1 = scopedProvider.get();
        Assert.assertNotNull(t1);

        scope.changeScope(null);

        final ScopedObject t2 = scopedProvider.get();
        Assert.assertNotNull(t2);

        Assert.assertNotSame(t1, t2);
    }

    @Test
    public void testScopeHandoff()
    {
        final Provider<ScopedObject> scopedProvider = scope.scope(fooTestObjectKey, new TestObjectProvider());
        Assert.assertNotNull(scopedProvider);

        final ScopedObject t1 = scopedProvider.get();
        Assert.assertNotNull(t1);

        final ThreadDelegatedContext plate = scope.getContext();

        scope.changeScope(null);

        final ScopedObject t2 = scopedProvider.get();
        Assert.assertNotNull(t2);

        Assert.assertNotSame(t1, t2);

        scope.changeScope(plate);

        final ScopedObject t3 = scopedProvider.get();
        Assert.assertNotNull(t3);

        Assert.assertSame(t1, t3);
        Assert.assertNotSame(t2, t3);
    }
}
