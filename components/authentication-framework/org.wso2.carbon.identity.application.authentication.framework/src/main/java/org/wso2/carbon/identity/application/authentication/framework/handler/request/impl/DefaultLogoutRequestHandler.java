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

package org.wso2.carbon.identity.application.authentication.framework.handler.request.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ExternalIdPConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.exception.LogoutFailedException;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.LogoutRequestHandler;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticationResult;
import org.wso2.carbon.identity.application.authentication.framework.model.CommonAuthResponseWrapper;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DefaultLogoutRequestHandler implements LogoutRequestHandler {

    private static final Log log = LogFactory.getLog(DefaultLogoutRequestHandler.class);
    private static volatile DefaultLogoutRequestHandler instance;
    private static final Log AUDIT_LOG = CarbonConstants.AUDIT_LOG;

    public static DefaultLogoutRequestHandler getInstance() {

        if (log.isTraceEnabled()) {
            log.trace("Inside getInstance()");
        }

        if (instance == null) {
            synchronized (DefaultLogoutRequestHandler.class) {

                if (instance == null) {
                    instance = new DefaultLogoutRequestHandler();
                }
            }
        }

        return instance;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AuthenticationContext context)
            throws FrameworkException {

        if (log.isTraceEnabled()) {
            log.trace("Inside handle()");
        }
        SequenceConfig sequenceConfig = context.getSequenceConfig();
        ExternalIdPConfig externalIdPConfig = null;
        if (context.isPreviousSessionFound()) {
            // if this is the start of the logout sequence
            if (context.getCurrentStep() == 0) {
                context.setCurrentStep(1);
            }

            int stepCount = sequenceConfig.getStepMap().size();

            while (context.getCurrentStep() <= stepCount) {
                int currentStep = context.getCurrentStep();
                StepConfig stepConfig = sequenceConfig.getStepMap().get(currentStep);
                AuthenticatorConfig authenticatorConfig = stepConfig.getAuthenticatedAutenticator();
                if (authenticatorConfig == null) {
                    authenticatorConfig = sequenceConfig.getAuthenticatedReqPathAuthenticator();
                }
                ApplicationAuthenticator authenticator =
                        authenticatorConfig.getApplicationAuthenticator();

                String idpName = stepConfig.getAuthenticatedIdP();
                //TODO: Need to fix occurrences where idPName becomes "null"
                if ((idpName == null || "null".equalsIgnoreCase(idpName) || idpName.isEmpty()) &&
                        sequenceConfig.getAuthenticatedReqPathAuthenticator() != null) {
                    idpName = FrameworkConstants.LOCAL_IDP_NAME;
                }
                try {
                    externalIdPConfig = ConfigurationFacade.getInstance()
                            .getIdPConfigByName(idpName, context.getTenantDomain());
                    context.setExternalIdP(externalIdPConfig);
                    context.setAuthenticatorProperties(FrameworkUtils
                            .getAuthenticatorPropertyMapFromIdP(
                                    externalIdPConfig, authenticator.getName()));
                    context.setStateInfo(authenticatorConfig.getAuthenticatorStateInfo());

                    AuthenticatorFlowStatus status = authenticator.process(request, response, context);
                    request.setAttribute(FrameworkConstants.RequestParams.FLOW_STATUS, status);

                    if (!status.equals(AuthenticatorFlowStatus.INCOMPLETE)) {
                        // TODO what if logout fails. this is an edge case
                        currentStep++;
                        context.setCurrentStep(currentStep);
                        continue;
                    }
                    // sends the logout request to the external IdP
                    FrameworkUtils.addAuthenticationContextToCache(context.getContextIdentifier(), context);
                    return;
                } catch (AuthenticationFailedException | LogoutFailedException e) {
                    throw new FrameworkException("Exception while handling logout request", e);
                } catch (IdentityProviderManagementException e) {
                    log.error("Exception while getting IdP by name", e);
                }
            }
            if (sequenceConfig != null && sequenceConfig.getAuthenticatedUser() != null) {
                String auditData = "\"" + "ContextIdentifier" + "\" : \"" + context.getContextIdentifier()
                        + "\",\"" + "LoggedOutUser" + "\" : \"" + sequenceConfig.getAuthenticatedUser().
                        getAuthenticatedSubjectIdentifier()
                        + "\",\"" + "LoggedOutUserTenantDomain" + "\" : \"" + sequenceConfig.
                        getAuthenticatedUser().getTenantDomain()
                        + "\",\"" + "ServiceProviderName" + "\" : \"" + context.getServiceProviderName()
                        + "\",\"" + "RequestType" + "\" : \"" + context.getRequestType()
                        + "\",\"" + "RelyingParty" + "\" : \"" + context.getRelyingParty()
                        + "\",\"" + "AuthenticatedIdPs" + "\" : \"" + sequenceConfig.getAuthenticatedIdPs()
                        + "\"";

                String idpName = null;
                if (externalIdPConfig != null) {
                    idpName = externalIdPConfig.getName();
                }
                AUDIT_LOG.info(String.format(
                        FrameworkConstants.AUDIT_MESSAGE,
                        sequenceConfig.getAuthenticatedUser().getAuthenticatedSubjectIdentifier(),
                        "Logout", idpName, auditData, FrameworkConstants.AUDIT_SUCCESS));
            }
        }

        // remove the SessionContext from the cache
        FrameworkUtils.removeSessionContextFromCache(context.getSessionIdentifier());
        // remove the cookie
        FrameworkUtils.removeAuthCookie(request, response);



        try {
            sendResponse(request, response, context, true);
        } catch (ServletException | IOException e) {
            throw new FrameworkException(e.getMessage(), e);
        }
    }

    protected void sendResponse(HttpServletRequest request, HttpServletResponse response,
                                AuthenticationContext context, boolean isLoggedOut)
            throws ServletException, IOException {

        if (log.isTraceEnabled()) {
            log.trace("Inside sendLogoutResponseToCaller()");
        }

        // Set values to be returned to the calling servlet as request
        // attributes
        request.setAttribute(FrameworkConstants.ResponseParams.LOGGED_OUT, isLoggedOut);

        String redirectURL;

        if(context.getCallerSessionKey() != null) {
            request.setAttribute(FrameworkConstants.SESSION_DATA_KEY, context.getCallerSessionKey());

            AuthenticationResult authenticationResult = new AuthenticationResult();
            authenticationResult.setLoggedOut(true);

            SequenceConfig sequenceConfig = context.getSequenceConfig();
            if (sequenceConfig != null) {
                authenticationResult.setSaaSApp(sequenceConfig.getApplicationConfig().isSaaSApp());
            }

            if (FrameworkUtils.getCacheDisabledAuthenticators().contains(context.getRequestType())
                    && (response instanceof CommonAuthResponseWrapper)) {
                //Set authentication result as request attribute
                addAuthenticationResultToRequest(request, authenticationResult);
            }else{
                FrameworkUtils.addAuthenticationResultToCache(context.getCallerSessionKey(), authenticationResult);
            }

            redirectURL = context.getCallerPath() + "?sessionDataKey=" + context.getCallerSessionKey();
        } else {
            redirectURL = context.getCallerPath();
        }
        
        /*
         * TODO Cache retaining is a temporary fix. Remove after Google fixes
         * http://code.google.com/p/gdata-issues/issues/detail?id=6628
         */
        String retainCache = System.getProperty("retainCache");

        if (retainCache == null) {
            FrameworkUtils.removeAuthenticationContextFromCache(context.getContextIdentifier());
        }

        if (log.isDebugEnabled()) {
            log.debug("Sending response back to: " + context.getCallerPath() + "...\n"
                      + FrameworkConstants.ResponseParams.LOGGED_OUT + " : " + isLoggedOut + "\n"
                      + FrameworkConstants.SESSION_DATA_KEY + ": " + context.getCallerSessionKey());
        }

        // redirect to the caller
        response.sendRedirect(redirectURL);
    }

    /**
     * Add authentication result into request attribute
     *
     * @param request Http servlet request
     * @param authenticationResult Authentication result
     */
    private void addAuthenticationResultToRequest(HttpServletRequest request,
            AuthenticationResult authenticationResult) {
        request.setAttribute(FrameworkConstants.RequestAttribute.AUTH_RESULT, authenticationResult);
    }
}
