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
package com.opentable.scopes.threaddelegate.concurrent;

import static com.opentable.scopes.threaddelegate.ThreadDelegatedScope.SCOPE;

import com.opentable.scopes.threaddelegate.ThreadDelegatedContext;


class DelegatingRunnable implements Runnable
{
    private final Runnable wrappedRunnable;
    private final ThreadDelegatedContext callerContext;

    DelegatingRunnable(final Runnable wrappedRunnable)
    {
        this.wrappedRunnable = wrappedRunnable;
        this.callerContext = SCOPE.getContext();
    }

    @Override
    public void run()
    {
        final ThreadDelegatedContext originalContext = SCOPE.getContext();
        try {
            // Assign the caller context.
            SCOPE.changeScope(callerContext);

            wrappedRunnable.run();
        }
        finally {
            // Reassign the original context.
            SCOPE.changeScope(originalContext);
        }
    }
}
