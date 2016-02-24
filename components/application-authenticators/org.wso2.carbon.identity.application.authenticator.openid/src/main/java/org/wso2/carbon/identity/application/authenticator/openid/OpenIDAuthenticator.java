/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.application.authenticator.openid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.AbstractApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.FederatedApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.authenticator.openid.exception.OpenIDException;
import org.wso2.carbon.identity.application.authenticator.openid.manager.DefaultOpenIDManager;
import org.wso2.carbon.identity.application.authenticator.openid.manager.OpenIDManager;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class OpenIDAuthenticator extends AbstractApplicationAuthenticator implements
        FederatedApplicationAuthenticator {

    private static final long serialVersionUID = 2878862656196592256L;
    private static final String OPENID_MANAGER = "OpenIDManager";
    private static Log log = LogFactory.getLog(OpenIDAuthenticator.class);

    @Override
    public boolean canHandle(HttpServletRequest request) {

        if (log.isTraceEnabled()) {
            log.trace("Inside canHandle()");
        }

        String opeidMode = request.getParameter(OpenIDAuthenticatorConstants.MODE);
        if (opeidMode != null && !"checkid_immediate".equals(opeidMode)
                && !"checkid_setup".equals(opeidMode) && !"check_authentication".equals(opeidMode)) {
            return true;
        }

        return false;
    }

    @Override
    protected void initiateAuthenticationRequest(HttpServletRequest request,
                                                 HttpServletResponse response, AuthenticationContext context)
            throws AuthenticationFailedException {

        OpenIDManager manager = getNewOpenIDManagerInstance();

        if (context.getExternalIdP() != null || request.getParameter("claimed_id") != null) {
            try {

                Map<String, String> authenticatorProperties = context.getAuthenticatorProperties();

                if(authenticatorProperties != null){
                    setOpenIDServerUrl(authenticatorProperties);
                }

                if (getOpenIDServerUrl() != null) {
                    // this is useful in case someone wants to overrode the default OpenID
                    // authenticator.
                    authenticatorProperties.put(
                            IdentityApplicationConstants.Authenticator.OpenID.OPEN_ID_URL,
                            getOpenIDServerUrl());
                }

                String loginPage = manager.doOpenIDLogin(request, response, context);
                String domain = request.getParameter("domain");

                if (domain != null) {
                    loginPage = loginPage + "&fidp=" + domain;
                }


                if (authenticatorProperties != null) {
                    String queryString = authenticatorProperties
                            .get(FrameworkConstants.QUERY_PARAMS);
                    if (queryString != null) {
                        if (!queryString.startsWith("&")) {
                            loginPage = loginPage + "&" + queryString;
                        } else {
                            loginPage = loginPage + queryString;
                        }
                    }
                }

                response.sendRedirect(loginPage);
            } catch (IOException e) {
                log.error("Error when sending to OpenID Provider", e);
                throw new AuthenticationFailedException(e.getMessage(), e);
            } catch (OpenIDException e) {
                log.error("Error when sending to OpenID Provider", e);
                throw new AuthenticationFailedException(e.getMessage(), e);
            }
        } else { // Claimed Identity
            String loginPage = ConfigurationFacade.getInstance().getAuthenticationEndpointURL();
            String queryParams = FrameworkUtils.getQueryStringWithFrameworkContextId(
                    context.getQueryParams(), context.getCallerSessionKey(),
                    context.getContextIdentifier());

            try {
                // TODO should be able to remove loginType=openid
                response.sendRedirect(loginPage + ("?" + queryParams + "&loginType=openid")
                        + "&authenticators=" + getName() + ":" + "LOCAL");
            } catch (IOException e) {
                log.error("Error when sending to the login page", e);
                throw new AuthenticationFailedException(e.getMessage(), e);
            }
        }

        return;
    }

    @Override
    protected void processAuthenticationResponse(HttpServletRequest request,
                                                 HttpServletResponse response, AuthenticationContext context)
            throws AuthenticationFailedException {

        OpenIDManager manager = getNewOpenIDManagerInstance();

        try {
            manager.processOpenIDLoginResponse(request, response, context);

            AuthenticatedUser authenticatedSubject = context.getSubject();
            String subject = null;
            String isSubjectInClaimsProp = context.getAuthenticatorProperties().get(
                    IdentityApplicationConstants.Authenticator.SAML2SSO.IS_USER_ID_IN_CLAIMS);
            if ("true".equalsIgnoreCase(isSubjectInClaimsProp)) {
                subject = getSubjectFromUserIDClaimURI(context);
            }

            if (subject == null) {
                subject = authenticatedSubject.getAuthenticatedSubjectIdentifier();
            }

            if (subject == null) {
                throw new OpenIDException("Cannot find federated User Identifier");
            }

            authenticatedSubject.setAuthenticatedSubjectIdentifier(subject);

        } catch (OpenIDException e) {
            log.error("Error when processing response from OpenID Provider", e);
            throw new AuthenticationFailedException(e.getMessage(), e);
        }
    }

    @Override
    public String getContextIdentifier(HttpServletRequest request) {

        if (log.isTraceEnabled()) {
            log.trace("Inside getContextIdentifier()");
        }

        return request.getParameter(OpenIDAuthenticatorConstants.SESSION_DATA_KEY);
    }

    private OpenIDManager getNewOpenIDManagerInstance() {

        OpenIDManager openIDManager = null;
        String managerClassName = getAuthenticatorConfig().getParameterMap().get(OPENID_MANAGER);
        if (managerClassName != null) {
            try {
                // Bundle class loader will cache the loaded class and returned
                // the already loaded instance, hence calling this method
                // multiple times doesn't cost.
                Class clazz = Thread.currentThread().getContextClassLoader()
                        .loadClass(managerClassName);
                openIDManager = (OpenIDManager) clazz.newInstance();

            } catch (ClassNotFoundException e) {
                log.error("Error while instantiating the OpenIDManager ", e);
            } catch (InstantiationException e) {
                log.error("Error while instantiating the OpenIDManager ", e);
            } catch (IllegalAccessException e) {
                log.error("Error while instantiating the OpenIDManager ", e);
            }
        } else {
            openIDManager = new DefaultOpenIDManager();
        }

        return openIDManager;
    }

    @Override
    public String getClaimDialectURI() {
        return OpenIDAuthenticatorConstants.CLAIM_DIALECT_URI;
    }

    @Override
    public String getFriendlyName() {
        return OpenIDAuthenticatorConstants.AUTHENTICATOR_FRIENDLY_NAME;
    }

    @Override
    public String getName() {
        return OpenIDAuthenticatorConstants.AUTHENTICATOR_NAME;
    }

    /**
     * @return
     */
    protected String getOpenIDServerUrl() {
        return null;
    }

    /**
     *
     * @param authenticatorProperties
     */
    protected void setOpenIDServerUrl(Map<String, String> authenticatorProperties){
    }

    /**
     * @subject
     */
    protected String getSubjectFromUserIDClaimURI(AuthenticationContext context) {
        String subject = null;
        try {
            subject = FrameworkUtils.getFederatedSubjectFromClaims(context, getClaimDialectURI());
        } catch (Exception e) {
            if(log.isDebugEnabled()) {
                log.debug("Couldn't find the subject claim from claim mappings ", e);
            }
        }
        return subject;
    }

}
