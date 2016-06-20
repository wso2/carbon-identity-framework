/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework;

import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.SessionContext;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class AuthnDataPublishHandlerManager {

    private static AuthnDataPublishHandlerManager authenticationDataPublishHandler = new
            AuthnDataPublishHandlerManager();

    private List<AbstractAuthenticationDataPublisher> dataPublishers = FrameworkServiceDataHolder.getInstance()
            .getDataPublishers();

    public static AuthnDataPublishHandlerManager getInstance() {
        return authenticationDataPublishHandler;
    }

    /**
     * Publish authentication success after managing handler operations
     *
     * @param request Request which comes to the framework for authentication
     * @param context Authentication context
     * @param params  Other parameters which are need to be passed
     */
    public void publishAuthenticationStepSuccess(HttpServletRequest request, AuthenticationContext context,
                                                 Map<String, Object> params) {
        sortDataPublishers(dataPublishers, context);
        for (AbstractAuthenticationDataPublisher publisher : dataPublishers) {
            if (publisher.isEnabled(context) && publisher.canHandle(context)) {
                publisher.publishAuthenticationStepSuccess(request, context, params);
            }
        }
    }

    /**
     * Published authentication step failure after managing handler operations
     *
     * @param request         Incoming Http request to framework for authentication
     * @param context         Authentication Context
     * @param unmodifiableMap Other relevant parameters which needs to be published
     */
    public void publishAuthenticationStepFailure(HttpServletRequest request, AuthenticationContext context,
                                                 Map<String, Object> unmodifiableMap) {
        sortDataPublishers(dataPublishers, context);
        for (AbstractAuthenticationDataPublisher publisher : dataPublishers) {
            if (publisher.isEnabled(context) && publisher.canHandle(context)) {
                publisher.publishAuthenticationStepFailure(request, context, unmodifiableMap);
            }
        }

    }

    /**
     * Publishes authentication success after managing handler operations
     *
     * @param request         Incoming request for authentication
     * @param context         Authentication context
     * @param unmodifiableMap Other relevant parameters which needs to be published
     */
    public void publishAuthenticationSuccess(HttpServletRequest request, AuthenticationContext context,
                                             Map<String, Object> unmodifiableMap) {
        sortDataPublishers(dataPublishers, context);
        for (AbstractAuthenticationDataPublisher publisher : dataPublishers) {
            if (publisher != null && publisher.isEnabled(context) && publisher.canHandle(context)) {
                publisher.publishAuthenticationSuccess(request, context, unmodifiableMap);
            }
        }

    }

    /**
     * Publishes authentication failure after managing handler operations
     *
     * @param request         Incoming authentication request
     * @param context         Authentication context
     * @param unmodifiableMap Other relevant parameters which needs to be published
     */
    public void publishAuthenticationFailure(HttpServletRequest request, AuthenticationContext context,
                                             Map<String, Object> unmodifiableMap) {
        sortDataPublishers(dataPublishers, context);
        for (AbstractAuthenticationDataPublisher publisher : dataPublishers) {
            if (publisher != null && publisher.isEnabled(context) && publisher.canHandle(context)) {
                publisher.publishAuthenticationFailure(request, context, unmodifiableMap);
            }
        }
    }

    /**
     * Publishes session creation information after managing handler operations
     *
     * @param request         Incoming request for authentication
     * @param context         Authentication Context
     * @param sessionContext  Session context
     * @param unmodifiableMap Other relevant parameters which needs to be published
     */
    public void publishSessionCreation(HttpServletRequest request, AuthenticationContext context, SessionContext
            sessionContext, Map<String, Object> unmodifiableMap) {
        sortDataPublishers(dataPublishers, context);
        for (AbstractAuthenticationDataPublisher publisher : dataPublishers) {
            if (publisher != null && publisher.isEnabled(context) && publisher.canHandle(context)) {
                publisher.publishSessionCreation(request, context, sessionContext, unmodifiableMap);
            }
        }
    }

    /**
     * Publishes session update after managing handler operations
     *
     * @param request         Incoming request for authentication
     * @param context         Authentication context
     * @param sessionContext  Session context
     * @param unmodifiableMap Other relevant parameters which needs to be published
     */

    public void publishSessionUpdate(HttpServletRequest request, AuthenticationContext context, SessionContext
            sessionContext, Map<String, Object> unmodifiableMap) {
        sortDataPublishers(dataPublishers, context);
        for (AbstractAuthenticationDataPublisher publisher : dataPublishers) {
            if (publisher != null && publisher.isEnabled(context) && publisher.canHandle(context)) {
                publisher.publishSessionUpdate(request, context, sessionContext, unmodifiableMap);
            }
        }

    }

    /**
     * Publishes session termination
     *
     * @param request         Incoming request for authentication
     * @param context         Authentication context
     * @param sessionContext  Session context
     * @param unmodifiableMap Other relevant parameters which needs to be published
     */

    public void publishSessionTermination(HttpServletRequest request, AuthenticationContext context,
                                          SessionContext sessionContext, Map<String, Object> unmodifiableMap) {
        sortDataPublishers(dataPublishers, context);
        for (AbstractAuthenticationDataPublisher publisher : dataPublishers) {
            if (publisher != null && publisher.isEnabled(context) && publisher.canHandle(context)) {
                publisher.publishSessionTermination(request, context, sessionContext, unmodifiableMap);
            }
        }
    }

    /**
     * Sort publishers based on context information. Here it only uses order configured in identity.xml
     *
     * @param authenticationDataPublishers
     * @param context
     */
    protected void sortDataPublishers(List<AbstractAuthenticationDataPublisher> authenticationDataPublishers,
                                      final AuthenticationContext context) {
        Collections.sort(authenticationDataPublishers, new Comparator<AbstractAuthenticationDataPublisher>() {
            public int compare(AbstractAuthenticationDataPublisher publisher1, AbstractAuthenticationDataPublisher
                    publisher2) {
                return publisher1.getPriority(context) - publisher2.getPriority(context);
            }
        });
    }

    /**
     * Checks whether registered listeners are available.
     * @return true if listeners are available
     */
    public boolean isListenersAvailable() {
        if (dataPublishers != null && dataPublishers.size() > 0) {
            return true;
        }
        return false;
    }
}
