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

public class TestThreadDelegatedContext
{
    private ThreadDelegatedContext plate = null;

    private final String fooName = "foo";
    private final String barName = "bar";
    private final String bazName = "baz";

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
        Assert.assertFalse(plate.containsKey(bazName));
    }

    @Test
    public void testSimplePut()
    {
        Assert.assertFalse(plate.containsKey(fooName));

        plate.put(fooName, "hallo");
        Assert.assertEquals(1, plate.size());
        Assert.assertTrue(plate.containsKey(fooName));
        Assert.assertEquals("hallo", plate.get(fooName));
    }

    @Test
    public void testDoubleSamePut()
    {
        Assert.assertFalse(plate.containsKey(fooName));

        plate.put(fooName, "hallo");
        Assert.assertEquals(1, plate.size());
        Assert.assertTrue(plate.containsKey(fooName));
        Assert.assertEquals("hallo", plate.get(fooName));

        plate.put(fooName, "hallo");
        Assert.assertEquals(1, plate.size());
        Assert.assertTrue(plate.containsKey(fooName));
        Assert.assertEquals("hallo", plate.get(fooName));
    }

    @Test
    public void testOverridePut()
    {
        plate.put(fooName, "hallo");
        Assert.assertEquals(1, plate.size());
        Assert.assertTrue(plate.containsKey(fooName));
        Assert.assertEquals("hallo", plate.get(fooName));

        // Override put
        plate.put(fooName, "world");
        Assert.assertEquals(1, plate.size());
        Assert.assertTrue(plate.containsKey(fooName));
        Assert.assertEquals("world", plate.get(fooName));
    }

    @Test
    public void testDoublePut()
    {
        Assert.assertFalse(plate.containsKey(fooName));
        Assert.assertFalse(plate.containsKey(barName));

        plate.put(fooName, "hallo");
        Assert.assertEquals(1, plate.size());
        Assert.assertTrue(plate.containsKey(fooName));
        Assert.assertEquals("hallo", plate.get(fooName));

        plate.put(barName, "world");
        Assert.assertEquals(2, plate.size());
        Assert.assertTrue(plate.containsKey(barName));
        Assert.assertEquals("world", plate.get(barName));
    }

    @Test
    public void testNullValue()
    {
        plate.put(fooName, null);
        Assert.assertEquals(1, plate.size());
        Assert.assertTrue(plate.containsKey(fooName));
        Assert.assertNull(plate.get(fooName));

        // Override put
        plate.put(fooName, "world");
        Assert.assertEquals(1, plate.size());
        Assert.assertTrue(plate.containsKey(fooName));
        Assert.assertEquals("world", plate.get(fooName));
    }

    @Test
    public void testClear()
    {
        Assert.assertFalse(plate.containsKey(fooName));
        Assert.assertFalse(plate.containsKey(barName));

        plate.put(fooName, "hallo");
        Assert.assertEquals(1, plate.size());
        Assert.assertTrue(plate.containsKey(fooName));
        Assert.assertEquals("hallo", plate.get(fooName));

        plate.put(barName, "world");
        Assert.assertEquals(2, plate.size());
        Assert.assertTrue(plate.containsKey(barName));
        Assert.assertEquals("world", plate.get(barName));

        plate.clear();

        Assert.assertFalse(plate.containsKey(fooName));
        Assert.assertFalse(plate.containsKey(barName));
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
