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

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.LogoutFailedException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatorData;
import org.wso2.carbon.identity.application.common.model.Property;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
    boolean canHandle(HttpServletRequest request);


    /**
     * Check whether the request from multi-option page can be handled by the authenticator.
     *
     * @param request HTTP servlet request.
     * @param context Authentication context object.
     * @return boolean
     */
    default boolean canHandleMultiOptionRequest(HttpServletRequest request, AuthenticationContext context) {

        return canHandle(request);
    }

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
    AuthenticatorFlowStatus process(HttpServletRequest request,
                                           HttpServletResponse response, AuthenticationContext context)
            throws AuthenticationFailedException, LogoutFailedException;

    /**
     * Get the Context identifier sent with the request. This identifier is used
     * to retrieve the state of the authentication/logout flow
     *
     * @param request
     * @return
     */
    String getContextIdentifier(HttpServletRequest request);

    /**
     * Get the name of the Authenticator
     *
     * @return name
     */
    String getName();

    /**
     * @return
     */
    String getFriendlyName();

    /**
     * Get the claim dialect URI if this authenticator receives claims in a standard dialect
     * and needs to be mapped to the Carbon dialect http://wso2.org/claims
     *
     * @return boolean
     */
    String getClaimDialectURI();

    /**
     * @return
     */
    List<Property> getConfigurationProperties();

    /**
     * Get Authentication Mechanism
     *
     * @return authentication mechanism
     */
    default String getAuthMechanism() {
        return getName();
    }

    /**
     * Get the tag list of the authenticators.
     *
     * @return List of tags.
     */
    default String[] getTags() {

        return new String[0];
    }

    /**
     * Check whether user satisfies all the pre-requisites of the authenticator to be authenticated with.
     *
     * @param request       Request which comes to the framework for authentication.
     * @param context       Authentication context.
     * @return boolean if satisfies all the pre-requisites of the authenticator.
     */
    default boolean isSatisfyAuthenticatorPrerequisites(HttpServletRequest request, AuthenticationContext context)
            throws AuthenticationFailedException {
        return true;
    }

    /**
     * Check whether the authenticator supports API based authentication.
     *
     * @return true if the authenticator supports API based authentication.
     */
    default boolean isAPIBasedAuthenticationSupported() {

        return false;
    }

    /**
     * Get the authentication initiation data.
     *
     * @param context Authentication context.
     * @return AuthenticatorData containing authentication initiation data.
     * @throws AuthenticationFailedException Authentication failed exception.
     */
    default Optional<AuthenticatorData> getAuthInitiationData(AuthenticationContext context) throws
            AuthenticationFailedException {

        return Optional.empty();
    }

    /**
     * Get the i18Key supported from the authenticator level.
     *
     * @return i18key
     */
    default String getI18nKey() {

        return StringUtils.EMPTY;
    }

}
