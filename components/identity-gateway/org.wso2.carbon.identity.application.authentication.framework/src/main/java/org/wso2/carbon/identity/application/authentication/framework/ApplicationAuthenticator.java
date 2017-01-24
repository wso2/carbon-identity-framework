/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.LogoutFailedException;
import org.wso2.carbon.identity.application.common.model.Property;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.List;

/**
 * API of the Application Authenticators.
 */
public interface ApplicationAuthenticator extends Serializable {

    /**
     * Check whether the authentication or logout request can be handled by the
     * authenticator
     *
     * @param request
     * @return boolean
     */
    public boolean canHandle(HttpServletRequest request);

    /**
     * Process the authentication or logout request.
     *
     * @param request
     * @param response
     * @param context
     * @return the status of the flow
     * @throws AuthenticationFailedException
     * @throws LogoutFailedException
     */
    public AuthenticatorFlowStatus process(HttpServletRequest request,
                                           HttpServletResponse response, AuthenticationContext context)
            throws AuthenticationFailedException, LogoutFailedException;

    /**
     * Get the Context identifier sent with the request. This identifier is used
     * to retrieve the state of the authentication/logout flow
     *
     * @param request
     * @return
     */
    public String getContextIdentifier(HttpServletRequest request);

    /**
     * Get the name of the Authenticator
     *
     * @return name
     */
    public String getName();

    /**
     * @return
     */
    public String getFriendlyName();

    /**
     * Get the claim dialect URI if this authenticator receives claims in a standard dialect
     * and needs to be mapped to the Carbon dialect http://wso2.org/claims
     *
     * @return boolean
     */
    public String getClaimDialectURI();

    /**
     * @return
     */
    public List<Property> getConfigurationProperties();
}