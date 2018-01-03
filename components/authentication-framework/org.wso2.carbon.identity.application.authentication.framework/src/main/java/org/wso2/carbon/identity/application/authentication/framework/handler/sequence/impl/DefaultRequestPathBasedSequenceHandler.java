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

package org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ApplicationConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthHistory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.exception.InvalidCredentialsException;
import org.wso2.carbon.identity.application.authentication.framework.exception.LogoutFailedException;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.RequestPathBasedSequenceHandler;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedIdPData;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationManagementUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DefaultRequestPathBasedSequenceHandler implements RequestPathBasedSequenceHandler {

    private static final Log log = LogFactory.getLog(DefaultRequestPathBasedSequenceHandler.class);
    private static volatile DefaultRequestPathBasedSequenceHandler instance;

    public static DefaultRequestPathBasedSequenceHandler getInstance() {

        if (instance == null) {
            synchronized (DefaultRequestPathBasedSequenceHandler.class) {

                if (instance == null) {
                    instance = new DefaultRequestPathBasedSequenceHandler();
                }
            }
        }

        return instance;
    }

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AuthenticationContext context) throws FrameworkException {

        if (log.isDebugEnabled()) {
            log.debug("Executing the Request Path Authentication...");
        }

        SequenceConfig seqConfig = context.getSequenceConfig();
        List<AuthenticatorConfig> reqPathAuthenticators = seqConfig.getReqPathAuthenticators();

        for (AuthenticatorConfig reqPathAuthenticator : reqPathAuthenticators) {

            ApplicationAuthenticator authenticator = reqPathAuthenticator.getApplicationAuthenticator();

            if (log.isDebugEnabled()) {
                log.debug("Executing " + authenticator.getName());
            }

            if (authenticator.canHandle(request)) {

                if (log.isDebugEnabled()) {
                    log.debug(authenticator.getName() + " can handle the request");
                }

                try {
                    AuthenticatorFlowStatus status = authenticator.process(request, response, context);
                    request.setAttribute(FrameworkConstants.RequestParams.FLOW_STATUS, status);

                    if (log.isDebugEnabled()) {
                        log.debug(authenticator.getName() + ".authenticate() returned: " + status.toString());
                    }

                    AuthenticatedUser authenticatedUser = context.getSubject();
                    seqConfig.setAuthenticatedUser(authenticatedUser);

                    if (log.isDebugEnabled()) {
                        if (authenticatedUser != null) {
                            log.debug("Authenticated User: " + authenticatedUser.getAuthenticatedSubjectIdentifier());
                            log.debug("Authenticated User Tenant Domain: " + authenticatedUser.getTenantDomain());
                        } else {
                            log.debug("Authenticated User is NULL.");
                        }
                    }

                    AuthenticatedIdPData authenticatedIdPData = new AuthenticatedIdPData();

                    // store authenticated user
                    authenticatedIdPData.setUser(authenticatedUser);

                    // store authenticated idp
                    authenticatedIdPData.setIdpName(FrameworkConstants.LOCAL_IDP_NAME);
                    reqPathAuthenticator.setAuthenticatorStateInfo(context.getStateInfo());
                    authenticatedIdPData.setAuthenticator(reqPathAuthenticator);

                    seqConfig.setAuthenticatedReqPathAuthenticator(reqPathAuthenticator);

                    context.getCurrentAuthenticatedIdPs().put(FrameworkConstants.LOCAL_IDP_NAME, authenticatedIdPData);

                    handlePostAuthentication(request, response, context, authenticatedIdPData);
                    context.addAuthenticationStepHistory(new AuthHistory(authenticator.getName(),
                            authenticatedIdPData.getIdpName()));

                } catch (InvalidCredentialsException e) {
                    if(log.isDebugEnabled()){
                        log.debug("A login attempt was failed due to invalid credentials", e);
                    }
                    context.setRequestAuthenticated(false);
                } catch (AuthenticationFailedException e) {
                    log.error(e.getMessage(), e);
                    context.setRequestAuthenticated(false);
                } catch (LogoutFailedException e) {
                    throw new FrameworkException(e.getMessage(), e);
                }

                context.getSequenceConfig().setCompleted(true);
                return;
            }
        }
    }

    protected void handlePostAuthentication(HttpServletRequest request,
                                            HttpServletResponse response, AuthenticationContext context,
                                            AuthenticatedIdPData authenticatedIdPData) throws FrameworkException {

        if (log.isDebugEnabled()) {
            log.debug("Handling Post Authentication tasks");
        }

        SequenceConfig sequenceConfig = context.getSequenceConfig();
        Map<String, String> mappedAttrs;
        StringBuilder jsonBuilder = new StringBuilder();

        // build the authenticated idps JWT to send to the calling servlet.
        jsonBuilder.append("\"idps\":");
        jsonBuilder.append("[");

        // build the JSON object for this step
        jsonBuilder.append("{");
        jsonBuilder.append("\"idp\":\"").append(authenticatedIdPData.getIdpName()).append("\",");
        jsonBuilder
                .append("\"authenticator\":\"")
                .append(authenticatedIdPData.getAuthenticator().getApplicationAuthenticator()
                                .getName()).append("\"");
        // wrap up the JSON object
        jsonBuilder.append("}");
        jsonBuilder.append("]");

        sequenceConfig.setAuthenticatedIdPs(IdentityApplicationManagementUtil.getSignedJWT(jsonBuilder.toString(),
                        sequenceConfig.getApplicationConfig().getServiceProvider()));

        mappedAttrs = handleClaimMappings(context);
        String spRoleUri = getSpRoleClaimUri(sequenceConfig.getApplicationConfig());
        String roleAttr = mappedAttrs.get(spRoleUri);

        if (StringUtils.isNotBlank(roleAttr)) {
            String[] roles = roleAttr.split(Pattern.quote(FrameworkUtils.getMultiAttributeSeparator()));
            mappedAttrs.put(spRoleUri, getServiceProviderMappedUserRoles(sequenceConfig, Arrays.asList(roles)));
        }

        sequenceConfig.getAuthenticatedUser().setUserAttributes(FrameworkUtils.buildClaimMappings(mappedAttrs));

        if (StringUtils.isNotBlank(context.getSequenceConfig().getApplicationConfig().getSubjectClaimUri())) {
            Map<String, String> unfilteredClaimValues =
                    (Map<String, String>) context.getProperty(FrameworkConstants.UNFILTERED_LOCAL_CLAIM_VALUES);

            String subjectClaimUri = context.getSequenceConfig().getApplicationConfig().getSubjectClaimUri().trim();
            String subjectClaimValue;
            if (unfilteredClaimValues != null) {
                subjectClaimValue = unfilteredClaimValues.get(subjectClaimUri);
            } else {
                subjectClaimValue = mappedAttrs.get(subjectClaimUri);
            }
            if (subjectClaimValue != null) {
                AuthenticatedUser authenticatedUser = sequenceConfig.getAuthenticatedUser();
                authenticatedUser.setAuthenticatedSubjectIdentifier(subjectClaimValue);

                if (log.isDebugEnabled()) {
                    log.debug("Authenticated User: " +
                              sequenceConfig.getAuthenticatedUser().getAuthenticatedSubjectIdentifier());
                    log.debug("Authenticated User Tenant Domain: " + sequenceConfig.getAuthenticatedUser().getTenantDomain());
                }
            }
        }
    }

    /**
     * @param sequenceConfig
     * @param locallyMappedUserRoles
     * @return
     */
    protected String getServiceProviderMappedUserRoles(SequenceConfig sequenceConfig,
                                                       List<String> locallyMappedUserRoles) throws FrameworkException {
        return DefaultSequenceHandlerUtils.getServiceProviderMappedUserRoles(sequenceConfig, locallyMappedUserRoles);
    }

    /**
     * @param appConfig
     * @return
     */
    protected String getSpRoleClaimUri(ApplicationConfig appConfig) throws FrameworkException {
        // get external identity provider role claim uri.
        String spRoleClaimUri = appConfig.getRoleClaim();

        if (spRoleClaimUri == null) {
            // no role claim uri defined
            // we can still try to find it out - lets have a look at the claim
            // mapping.
            Map<String, String> spToLocalClaimMapping = appConfig.getClaimMappings();

            if (spToLocalClaimMapping != null && !spToLocalClaimMapping.isEmpty()) {

                for (Entry<String, String> entry : spToLocalClaimMapping.entrySet()) {
                    if (FrameworkConstants.LOCAL_ROLE_CLAIM_URI.equals(entry.getValue())) {
                        return entry.getKey();
                    }
                }
            }
        }

        if (spRoleClaimUri == null) {
            spRoleClaimUri = FrameworkConstants.LOCAL_ROLE_CLAIM_URI;
            if (log.isDebugEnabled()) {
                String serviceProvider = appConfig.getApplicationName();
                log.debug("Service Provider Role Claim URI not configured for SP: " + serviceProvider +
                        ". Defaulting to " + spRoleClaimUri);
            }
        }

        return spRoleClaimUri;
    }

    /**
     * @param context
     * @return
     * @throws FrameworkException
     */
    protected Map<String, String> handleClaimMappings(AuthenticationContext context) throws FrameworkException {
        Map<String, String> mappedAttrs;
        try {
            mappedAttrs = FrameworkUtils.getClaimHandler().handleClaimMappings(null, context, null, false);
            return mappedAttrs;
        } catch (FrameworkException e) {
            log.error("Claim handling failed!", e);
        }
        // Claim handling failed so we return an empty map
        return Collections.emptyMap();
    }
}
