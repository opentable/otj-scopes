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

import com.opentable.scopes.threaddelegate.ThreadDelegatedContext.ScopeEvent;
import com.opentable.scopes.threaddelegate.ThreadDelegatedContext.ScopeListener;

// As an implementation of ScopeListener we can count how many events occurred and return the last event
class EventRecordingObject implements ScopeListener
{
    private int eventCount = 0;
    private ScopeEvent lastEvent = null;

    @Override
    public void event(final ScopeEvent event)
    {
        eventCount++;
        this.lastEvent = event;
    }

    public int getEventCount()
    {
        return eventCount;
    }

    public ScopeEvent getLastEvent()
    {
        return lastEvent;
    }
}
