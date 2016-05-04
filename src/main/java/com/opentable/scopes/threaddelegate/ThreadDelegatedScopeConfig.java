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

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.opentable.scopes.threaddelegate.servlet.ThreadDelegatingScopeFilter;

/**
 * Installs the ThreadDelegated Scope in an application.
 */
@Configuration
@Import(ThreadDelegatingScopeFilter.class)
public class ThreadDelegatedScopeConfig
{
    @Bean
    public static BeanFactoryPostProcessor getBeanFactoryPostProcessor() {
        return beanFactory ->
                beanFactory.registerScope(ThreadDelegatedContext.SCOPE_THREAD_DELEGATED, ThreadDelegatedScope.SCOPE);
    }

    @Bean
    public ThreadDelegatedScope getThreadDelegatedScope() {
        return ThreadDelegatedScope.SCOPE;
    }
}
