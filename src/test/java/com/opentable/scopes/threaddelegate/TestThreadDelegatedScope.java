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

import com.opentable.scopes.threaddelegate.ThreadDelegatedContext.ScopeEvent;

// Primary purpose of this class is to test various scope changes and show the context
// returns the right instance and that the events trigger as expected
public class TestThreadDelegatedScope
{
    private ThreadDelegatedScope scope = null;

    private final String fooName = "foo";

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

    // We get a new context and calling getContext twice, we apparently get the same Context, and its empty
    @Test
    public void testNewPlate()
    {
        final ThreadDelegatedContext plate = scope.getContext();
        Assert.assertNotNull(plate);
        Assert.assertEquals(0, plate.size());

        final ThreadDelegatedContext plate2 = scope.getContext();
        Assert.assertNotNull(plate2);
        Assert.assertEquals(0, plate2.size());

        Assert.assertSame(plate, plate2);
    }

    // We get a new context and show when we call changeScope(null), the next context handed out is not the same
    @Test
    public void testScopeLeave()
    {
        final ThreadDelegatedContext plate = scope.getContext();
        Assert.assertNotNull(plate);
        Assert.assertEquals(0, plate.size());

        scope.changeScope(null);

        final ThreadDelegatedContext plate2 = scope.getContext();
        Assert.assertNotNull(plate2);
        Assert.assertEquals(0, plate2.size());

        Assert.assertNotSame(plate, plate2);
    }

    // We call getContext, and its empty of course.
    // calling changeScope with a manually instantiated second context, and then calling getContext
    // shows we replaced it with second context
    @Test
    public void testScopePromote()
    {
        final ThreadDelegatedContext newPlate = new ThreadDelegatedContext();

        final ThreadDelegatedContext plate = scope.getContext();
        Assert.assertNotNull(plate);
        Assert.assertEquals(0, plate.size());

        scope.changeScope(newPlate);

        final ThreadDelegatedContext plate2 = scope.getContext();
        Assert.assertNotNull(plate2);
        Assert.assertEquals(0, plate2.size());

        // Old plate has disappeared.
        Assert.assertNotSame(plate, plate2);

        // But the plate returned is the new plate.
        Assert.assertSame(newPlate, plate2);
    }

    // We grab a new empty context with getContext()
    // Then we show after we put a ScopeListener into the context
    // and then clear the context, we received the correct events (ENTER and then LEAVE)
    @Test
    public void testChangeScopeEvents()
    {
        final ThreadDelegatedContext plate = scope.getContext();
        Assert.assertNotNull(plate);
        Assert.assertEquals(0, plate.size());

        final EventRecordingObject fooEventTest = new EventRecordingObject();

        plate.put(fooName, fooEventTest);
        Assert.assertEquals(1, fooEventTest.getEventCount());
        Assert.assertEquals(ScopeEvent.ENTER, fooEventTest.getLastEvent());

        scope.changeScope(null);

        Assert.assertEquals(2, fooEventTest.getEventCount());
        Assert.assertEquals(ScopeEvent.LEAVE, fooEventTest.getLastEvent());
    }

    @Test
    public void testScopeEnterLeaveEvents()
    {
        final ThreadDelegatedContext plate = new ThreadDelegatedContext();
        final EventRecordingObject fooEventTest = new EventRecordingObject();

        // We get an Enter
        plate.put(fooName, fooEventTest);
        Assert.assertEquals(1, fooEventTest.getEventCount());
        Assert.assertEquals(ScopeEvent.ENTER, fooEventTest.getLastEvent());

        // We stick the Context into the Scope
        scope.changeScope(plate);

        Assert.assertSame(plate, scope.getContext());

        // We get a second Enter. Note that Listeners get 1 for being placed initially and also for the context itself.
        Assert.assertEquals(2, fooEventTest.getEventCount());
        Assert.assertEquals(ScopeEvent.ENTER, fooEventTest.getLastEvent());

        // We kill the context
        scope.changeScope(null);

        // Prove we got a leave, and that the new context aint the same
        Assert.assertNotSame(plate, scope.getContext());
        Assert.assertEquals(3, fooEventTest.getEventCount());
        Assert.assertEquals(ScopeEvent.LEAVE, fooEventTest.getLastEvent());

        // Put the plate back in.
        scope.changeScope(plate);

        Assert.assertSame(plate, scope.getContext());

        // Listeners don't reset, despite this context in/out
        Assert.assertEquals(4, fooEventTest.getEventCount());
        Assert.assertEquals(ScopeEvent.ENTER, fooEventTest.getLastEvent());

        // Replace with a new plate
        scope.changeScope(new ThreadDelegatedContext());

        Assert.assertNotSame(plate, scope.getContext());

        Assert.assertEquals(5, fooEventTest.getEventCount());
        Assert.assertEquals(ScopeEvent.LEAVE, fooEventTest.getLastEvent());

        // Put it back in one more time...
        scope.changeScope(plate);

        Assert.assertSame(plate, scope.getContext());

        Assert.assertEquals(6, fooEventTest.getEventCount());
        Assert.assertEquals(ScopeEvent.ENTER, fooEventTest.getLastEvent());

        // Pathologic case: Replace with itself.
        // This is a no op and doesn't trigger an event
        scope.changeScope(plate);

        Assert.assertSame(plate, scope.getContext());

        Assert.assertEquals(6, fooEventTest.getEventCount());
        Assert.assertEquals(ScopeEvent.ENTER, fooEventTest.getLastEvent());
    }

}
