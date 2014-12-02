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
import com.opentable.scopes.threaddelegate.ThreadDelegatedContext.ScopeEvent;

import com.google.inject.Key;
import com.google.inject.name.Names;

public class TestThreadDelegatedContextEvents
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
    public void testPutEvent()
    {
        Assert.assertFalse(plate.containsKey(fooStringKey));
        final EventRecordingObject eventTest = new EventRecordingObject();

        plate.put(fooStringKey, eventTest);
        Assert.assertEquals(1, eventTest.getEventCount());
        Assert.assertEquals(ScopeEvent.ENTER, eventTest.getLastEvent());
    }

    @Test
    public void testDoublePutEvent()
    {
        Assert.assertFalse(plate.containsKey(fooStringKey));
        final EventRecordingObject fooEventTest = new EventRecordingObject();

        plate.put(fooStringKey, fooEventTest);
        Assert.assertEquals(1, fooEventTest.getEventCount());
        Assert.assertEquals(ScopeEvent.ENTER, fooEventTest.getLastEvent());

        Assert.assertFalse(plate.containsKey(barStringKey));
        final EventRecordingObject barEventTest = new EventRecordingObject();

        plate.put(barStringKey, barEventTest);
        Assert.assertEquals(1, barEventTest.getEventCount());
        Assert.assertEquals(ScopeEvent.ENTER, barEventTest.getLastEvent());

        // Make sure that the event count for foo is still 1
        Assert.assertEquals(1, fooEventTest.getEventCount());
    }

    @Test
    public void testEventNotify()
    {
        Assert.assertFalse(plate.containsKey(fooStringKey));
        final EventRecordingObject fooEventTest = new EventRecordingObject();

        plate.put(fooStringKey, fooEventTest);
        Assert.assertEquals(1, fooEventTest.getEventCount());
        Assert.assertEquals(ScopeEvent.ENTER, fooEventTest.getLastEvent());

        plate.event(ScopeEvent.ENTER);
        Assert.assertEquals(2, fooEventTest.getEventCount());
        Assert.assertEquals(ScopeEvent.ENTER, fooEventTest.getLastEvent());

        plate.event(ScopeEvent.LEAVE);
        Assert.assertEquals(3, fooEventTest.getEventCount());
        Assert.assertEquals(ScopeEvent.LEAVE, fooEventTest.getLastEvent());
    }


    @Test
    public void testEventClear()
    {
        Assert.assertFalse(plate.containsKey(fooStringKey));
        final EventRecordingObject fooEventTest = new EventRecordingObject();

        plate.put(fooStringKey, fooEventTest);
        Assert.assertEquals(1, fooEventTest.getEventCount());
        Assert.assertEquals(ScopeEvent.ENTER, fooEventTest.getLastEvent());

        plate.clear();

        Assert.assertEquals(0, plate.size());
        Assert.assertEquals(2, fooEventTest.getEventCount());
        Assert.assertEquals(ScopeEvent.LEAVE, fooEventTest.getLastEvent());
    }
}
