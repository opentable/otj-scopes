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

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * @deprecated This class has moved to components-ness-executors, so use that version
 * instead.
 */
@Deprecated
public final class ThreadDelegatingDecorator
{
    private ThreadDelegatingDecorator()
    {
    }

    public static Executor wrapExecutor(final Executor wrappedExecutor)
    {
        return new ThreadDelegatingExecutor(wrappedExecutor);
    }

    public static ExecutorService wrapExecutorService(final ExecutorService wrappedExecutorService)
    {
        return new ThreadDelegatingExecutorService(wrappedExecutorService);
    }
}
