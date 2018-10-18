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
import org.wso2.carbon.identity.core.handler.IdentityMessageHandler;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 * The data publishing changed to publish data through event handling. {@link AuthenticationDataPublisher} interface is
 * not to be used to register data publishing services from this release.
 * {@link org.wso2.carbon.identity.event.event.Event} can be used to create a event and use
 * {@link org.wso2.carbon.identity.event.handler.IdentityEventServiceImpl} \} to handle the event.
 */
public interface AuthenticationDataPublisher extends IdentityMessageHandler {

    /**
     * Publish authentication success
     *
     * @param request Request which comes to the framework for authentication
     * @param context Authentication context
     * @param params  Other parameters which are need to be passed
     */
    void publishAuthenticationStepSuccess(HttpServletRequest request, AuthenticationContext context,
                                          Map<String, Object> params);

    /**
     * Published authentication step failure
     *
     * @param request Incoming Http request to framework for authentication
     * @param context Authentication Context
     * @param params  Other relevant parameters which needs to be published
     */
    void publishAuthenticationStepFailure(HttpServletRequest request, AuthenticationContext context,
                                          Map<String, Object> params);

    /**
     * Publishes authentication success
     *
     * @param request Incoming request for authentication
     * @param context Authentication context
     * @param params  Other relevant parameters which needs to be published
     */
    public void publishAuthenticationSuccess(HttpServletRequest request, AuthenticationContext context,
                                             Map<String, Object> params);

    /**
     * Publishes authentication failure
     *
     * @param request Incoming authentication request
     * @param context Authentication context
     * @param params  Other relevant parameters which needs to be published
     */
    public void publishAuthenticationFailure(HttpServletRequest request, AuthenticationContext context,
                                             Map<String, Object> params);

    /**
     * Publishes session creation information
     *
     * @param request        Incoming request for authentication
     * @param context        Authentication Context
     * @param sessionContext Session context
     * @param params         Other relevant parameters which needs to be published
     */
    public void publishSessionCreation(HttpServletRequest request, AuthenticationContext context, SessionContext
            sessionContext, Map<String, Object> params);

    /**
     * Publishes session update
     *
     * @param request        Incoming request for authentication
     * @param context        Authentication context
     * @param sessionContext Session context
     * @param params         Other relevant parameters which needs to be published
     */
    public void publishSessionUpdate(HttpServletRequest request, AuthenticationContext context, SessionContext
            sessionContext, Map<String, Object> params);

    /**
     * Publishes session termination
     *
     * @param request        Incoming request for authentication
     * @param context        Authentication context
     * @param sessionContext Session context
     * @param params         Other relevant parameters which needs to be published
     */
    public void publishSessionTermination(HttpServletRequest request, AuthenticationContext context,
                                          SessionContext sessionContext, Map<String, Object> params);

}
