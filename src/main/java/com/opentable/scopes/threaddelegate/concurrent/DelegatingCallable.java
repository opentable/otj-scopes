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

import java.util.concurrent.Callable;

import com.opentable.scopes.threaddelegate.ThreadDelegatedContext;


class DelegatingCallable<C> implements Callable<C>
{
    private final Callable<C> wrappedCallable;
    private final ThreadDelegatedContext callerContext;

    DelegatingCallable(final Callable<C> wrappedCallable)
    {
        this.wrappedCallable = wrappedCallable;
        this.callerContext = SCOPE.getContext();
    }

    @Override
    public C call() throws Exception
    {
        final ThreadDelegatedContext originalContext = SCOPE.getContext();
        try {
            // Assign the caller context.
            SCOPE.changeScope(callerContext);

            return wrappedCallable.call();
        }
        finally {
            // Tell the current context, that we are leaving.
            SCOPE.changeScope(originalContext);
        }
    }
}
