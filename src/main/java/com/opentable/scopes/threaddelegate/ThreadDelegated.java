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

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.google.inject.ScopeAnnotation;

/**
 * This is an extension of the "request" scope and somewhat close to the "thread" scope. It allows binding object to
 * an incoming request (controlled by the ThreadDelegatingScopeFilter), but objects in this scope can be "forwarded" to other threads
 * through an Executor or ExecutorService to allow execution "on behalf of the original request". 
 *
 * Objects forwarded can e.g. be tracking information or authentication credentials.
 *
 * This scope was called "Spaghetti" at some point because it is somewhat convoluted and related to threads, which look a lot like Spaghetti.
 */
@Target({TYPE, METHOD})
@Retention(RUNTIME)
@ScopeAnnotation
public @interface ThreadDelegated
{
}
