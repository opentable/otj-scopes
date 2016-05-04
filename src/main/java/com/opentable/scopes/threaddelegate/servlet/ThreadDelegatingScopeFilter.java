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
package com.opentable.scopes.threaddelegate.servlet;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opentable.scopes.threaddelegate.ThreadDelegatedScope;

@Named
public class ThreadDelegatingScopeFilter implements Filter
{
    private static final Logger LOG = LoggerFactory.getLogger(ThreadDelegatingScopeFilter.class);

    public static final String THREAD_DELEGATING_SCOPE_ACTIVE = ThreadDelegatingScopeFilter.class.getName() + ".active";

    private final ThreadDelegatedScope scope;

    @Inject
    ThreadDelegatingScopeFilter(final ThreadDelegatedScope scope)
    {
        this.scope = scope;
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException
    {
        try {
            // chase out all objects that might be here, because the thread managed earlier to escape without
            // going though the LEAVE below. This is very unlikely to happen, though.
            scope.changeScope(null);

            // Mark to the filters further down that the scope is now active.
            request.setAttribute(THREAD_DELEGATING_SCOPE_ACTIVE, Boolean.TRUE);
            chain.doFilter(request, response);
        }
        finally {
            // All accumulated members (and listeners) will see a 'LEAVE' event now.
            scope.changeScope(null);

            // Remove the marker from the request.
            request.removeAttribute(THREAD_DELEGATING_SCOPE_ACTIVE);
        }
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException
    {
        LOG.info("ThreadDelegating scope now available!");
    }

    @Override
    public void destroy()
    {
        LOG.info("ThreadDelegating scope destroyed");
    }
}
