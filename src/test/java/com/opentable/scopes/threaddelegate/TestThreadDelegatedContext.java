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

import com.opentable.scopes.threaddelegate.ThreadDelegatedContext;

import com.google.inject.Key;
import com.google.inject.name.Names;

public class TestThreadDelegatedContext
{
    private ThreadDelegatedContext plate = null;

    private final Key<String> fooStringKey = Key.get(String.class, Names.named("foo"));
    private final Key<String> barStringKey = Key.get(String.class, Names.named("bar"));

    @Before
    public void setUp()
    {
        Assert.assertNull(plate);
        this.plate = new ThreadDelegatedContext();
    }

    @After
    public void tearDown()
    {
        Assert.assertNotNull(plate);
        this.plate = null;
    }

    @Test
    public void testEmptyPlate()
    {
        Assert.assertEquals(0, plate.size());
        Assert.assertFalse(plate.containsKey(Key.get(String.class)));
    }

    @Test
    public void testSimplePut()
    {
        Assert.assertFalse(plate.containsKey(fooStringKey));

        plate.put(fooStringKey, "hallo");
        Assert.assertEquals(1, plate.size());
        Assert.assertTrue(plate.containsKey(fooStringKey));
        Assert.assertEquals("hallo", plate.get(fooStringKey));
    }

    @Test
    public void testDoubleSamePut()
    {
        Assert.assertFalse(plate.containsKey(fooStringKey));

        plate.put(fooStringKey, "hallo");
        Assert.assertEquals(1, plate.size());
        Assert.assertTrue(plate.containsKey(fooStringKey));
        Assert.assertEquals("hallo", plate.get(fooStringKey));

        plate.put(fooStringKey, "hallo");
        Assert.assertEquals(1, plate.size());
        Assert.assertTrue(plate.containsKey(fooStringKey));
        Assert.assertEquals("hallo", plate.get(fooStringKey));
    }

    @Test
    public void testOverridePut()
    {
        plate.put(fooStringKey, "hallo");
        Assert.assertEquals(1, plate.size());
        Assert.assertTrue(plate.containsKey(fooStringKey));
        Assert.assertEquals("hallo", plate.get(fooStringKey));

        // Override put
        plate.put(fooStringKey, "world");
        Assert.assertEquals(1, plate.size());
        Assert.assertTrue(plate.containsKey(fooStringKey));
        Assert.assertEquals("world", plate.get(fooStringKey));
    }

    @Test
    public void testDoublePut()
    {
        Assert.assertFalse(plate.containsKey(fooStringKey));
        Assert.assertFalse(plate.containsKey(barStringKey));

        plate.put(fooStringKey, "hallo");
        Assert.assertEquals(1, plate.size());
        Assert.assertTrue(plate.containsKey(fooStringKey));
        Assert.assertEquals("hallo", plate.get(fooStringKey));

        plate.put(barStringKey, "world");
        Assert.assertEquals(2, plate.size());
        Assert.assertTrue(plate.containsKey(barStringKey));
        Assert.assertEquals("world", plate.get(barStringKey));
    }

    @Test
    public void testNullValue()
    {
        plate.put(fooStringKey, null);
        Assert.assertEquals(1, plate.size());
        Assert.assertTrue(plate.containsKey(fooStringKey));
        Assert.assertNull(plate.get(fooStringKey));

        // Override put
        plate.put(fooStringKey, "world");
        Assert.assertEquals(1, plate.size());
        Assert.assertTrue(plate.containsKey(fooStringKey));
        Assert.assertEquals("world", plate.get(fooStringKey));
    }

    @Test
    public void testClear()
    {
        Assert.assertFalse(plate.containsKey(fooStringKey));
        Assert.assertFalse(plate.containsKey(barStringKey));

        plate.put(fooStringKey, "hallo");
        Assert.assertEquals(1, plate.size());
        Assert.assertTrue(plate.containsKey(fooStringKey));
        Assert.assertEquals("hallo", plate.get(fooStringKey));

        plate.put(barStringKey, "world");
        Assert.assertEquals(2, plate.size());
        Assert.assertTrue(plate.containsKey(barStringKey));
        Assert.assertEquals("world", plate.get(barStringKey));

        plate.clear();

        Assert.assertFalse(plate.containsKey(fooStringKey));
        Assert.assertFalse(plate.containsKey(barStringKey));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testNullKeyGet()
    {
        plate.get(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testNullKeyPut()
    {
        plate.put(null, "Hello");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testNullKeyContains()
    {
        plate.containsKey(null);
    }
}
