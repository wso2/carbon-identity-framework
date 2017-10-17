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

package org.wso2.carbon.identity.application.authentication.framework.handler.request.impl;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.utils.URIBuilder;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.core.util.AnonymousSessionUtil;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.PostAuthenticationHandler;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceComponent;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementServiceImpl;
import org.wso2.carbon.identity.user.profile.mgt.UserProfileAdmin;
import org.wso2.carbon.identity.user.profile.mgt.UserProfileException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class DefaultPostAuthenticationHandler implements PostAuthenticationHandler {

    private static final Log log = LogFactory.getLog(DefaultPostAuthenticationHandler.class);
    private static final Log AUDIT_LOG = CarbonConstants.AUDIT_LOG;
    private static volatile DefaultPostAuthenticationHandler instance;

    public static DefaultPostAuthenticationHandler getInstance() {

        if (instance == null) {
            synchronized (DefaultPostAuthenticationHandler.class) {
                if (instance == null) {
                    instance = new DefaultPostAuthenticationHandler();
                }
            }
        }

        return instance;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AuthenticationContext context) throws FrameworkException {

        if (log.isDebugEnabled()) {
            log.debug("Post authentication handling for missing claims started");
        }

        Object object = context.getProperty(FrameworkConstants.POST_AUTHENTICATION_REDIRECTION_TRIGGERED);
        boolean postAuthRequestTriggered = false;
        if (object != null && object instanceof Boolean) {
            postAuthRequestTriggered = (boolean) object;
        }

        if (!postAuthRequestTriggered) {
            handlePostAuthenticationForMissingClaimsRequest(request, response, context);
        } else {
            handlePostAuthenticationForMissingClaimsResponse(request, response, context);
        }

    }

    private void handlePostAuthenticationForMissingClaimsRequest(HttpServletRequest request, HttpServletResponse response,
                                                                 AuthenticationContext context) throws FrameworkException {

        AuthenticatedUser user = context.getSequenceConfig().getAuthenticatedUser();
        if (user == null) {
            // no authenticated user found. Cannot process claims
            context.setProperty(FrameworkConstants.POST_AUTHENTICATION_EXTENSION_COMPLETED, true);
            return;
        }

        Map<String, String> mappedAttrs = new HashMap<>();
        Map<ClaimMapping, String> userAttributes = user.getUserAttributes();

        if (userAttributes != null) {

            Map<String, String> spToCarbonClaimMapping = new HashMap<>();
            Object object = context.getProperty(FrameworkConstants.SP_TO_CARBON_CLAIM_MAPPING);

            if (object != null && object instanceof Map) {
                spToCarbonClaimMapping = (Map<String, String>) object;
            }

            for (Map.Entry<ClaimMapping, String> entry : userAttributes.entrySet()) {
                String localClaimUri = entry.getKey().getLocalClaim().getClaimUri();

                //getting the carbon claim uri mapping for other claim dialects
                if (MapUtils.isNotEmpty(spToCarbonClaimMapping) &&
                        spToCarbonClaimMapping.get(localClaimUri) != null) {
                    localClaimUri = spToCarbonClaimMapping.get(localClaimUri);
                }
                mappedAttrs.put(localClaimUri, entry.getValue());
            }
        }

        Map<String, String> mandatoryClaims =
                context.getSequenceConfig().getApplicationConfig().getMandatoryClaimMappings();

        String missingClaims = getMissingClaims(mappedAttrs, mandatoryClaims);

        if (StringUtils.isNotBlank(missingClaims)) {

            if (log.isDebugEnabled()) {
                log.debug("Mandatory claims missing for the application : " + missingClaims);
            }

            //need to request for the missing claims before completing authentication
            request.setAttribute(FrameworkConstants.RequestParams.FLOW_STATUS, AuthenticatorFlowStatus.INCOMPLETE);
            context.setProperty(FrameworkConstants.POST_AUTHENTICATION_EXTENSION_COMPLETED, false);

            try {
                URIBuilder uriBuilder = new URIBuilder("/authenticationendpoint/claims.do");
                uriBuilder.addParameter(FrameworkConstants.MISSING_CLAIMS,
                        missingClaims);
                uriBuilder.addParameter(FrameworkConstants.SESSION_DATA_KEY,
                        context.getContextIdentifier());
                uriBuilder.addParameter("spName",
                        context.getSequenceConfig().getApplicationConfig().getApplicationName());
                response.sendRedirect(uriBuilder.build().toString());
                context.setProperty(FrameworkConstants.POST_AUTHENTICATION_REDIRECTION_TRIGGERED, true);

                if (log.isDebugEnabled()) {
                    log.debug("Redirecting to outside to pick mandatory claims");
                }
            } catch (IOException e) {
                throw new FrameworkException("Error while redirecting to request claims", e);
            } catch (URISyntaxException e) {
                throw new FrameworkException("Error while building redirect URI", e);
            }
        } else {
            context.setProperty(FrameworkConstants.POST_AUTHENTICATION_EXTENSION_COMPLETED, true);
        }
    }

    private void handlePostAuthenticationForMissingClaimsResponse(HttpServletRequest request, HttpServletResponse response,
                                                                  AuthenticationContext context) throws FrameworkException {

        if (log.isDebugEnabled()) {
            log.debug("Starting to process the response with missing claims");
        }

        Map<String, String> claims = new HashMap<String, String>();
        Map<String, String> claimsForContext = new HashMap<String, String>();
        Map<String, String[]> requestParams = request.getParameterMap();
        boolean persistClaims = false;

        AuthenticatedUser user = context.getSequenceConfig().getAuthenticatedUser();

        Map<String, String> carbonToSPClaimMapping = new HashMap<>();
        Object spToCarbonClaimMappingObject = context.getProperty(FrameworkConstants.SP_TO_CARBON_CLAIM_MAPPING);

        if (spToCarbonClaimMappingObject instanceof Map) {

            Map<String, String> spToCarbonClaimMapping = (Map<String, String>) spToCarbonClaimMappingObject;

            for (Map.Entry<String, String> entry : spToCarbonClaimMapping.entrySet()) {
                carbonToSPClaimMapping.put(entry.getValue(), entry.getKey());
            }
        }

        for (Map.Entry<String, String[]> entry : requestParams.entrySet()) {
            if (entry.getKey().startsWith(FrameworkConstants.RequestParams.MANDOTARY_CLAIM_PREFIX)) {

                String localClaimURI = entry.getKey().substring(FrameworkConstants.RequestParams.MANDOTARY_CLAIM_PREFIX.length());
                claims.put(localClaimURI, entry.getValue()[0]);

                if (spToCarbonClaimMappingObject != null) {
                    String spClaimURI = carbonToSPClaimMapping.get(localClaimURI);
                    claimsForContext.put(spClaimURI, entry.getValue()[0]);
                } else {
                    claimsForContext.put(localClaimURI, entry.getValue()[0]);
                }
            }
        }

        Map<ClaimMapping, String> authenticatedUserAttributes = FrameworkUtils.buildClaimMappings(claimsForContext);
        authenticatedUserAttributes.putAll(user.getUserAttributes());

        for (Map.Entry<Integer, StepConfig> entry : context.getSequenceConfig().getStepMap().entrySet()) {
            StepConfig stepConfig = entry.getValue();
            if (stepConfig.isSubjectAttributeStep()) {

                if (stepConfig.getAuthenticatedUser() != null) {
                    user = stepConfig.getAuthenticatedUser();
                }

                if (!user.isFederatedUser()) {
                    persistClaims = true;
                } else {
                    String associatedID = null;
                    UserProfileAdmin userProfileAdmin = UserProfileAdmin.getInstance();
                    String subject = user.getAuthenticatedSubjectIdentifier();
                    try {
                        associatedID = userProfileAdmin.getNameAssociatedWith(stepConfig.getAuthenticatedIdP(),
                                subject);
                        if (StringUtils.isNotBlank(associatedID)) {
                            String fullQualifiedAssociatedUserId = FrameworkUtils.prependUserStoreDomainToName(
                                    associatedID + UserCoreConstants.TENANT_DOMAIN_COMBINER + context.getTenantDomain());
                            user = AuthenticatedUser.createLocalAuthenticatedUserFromSubjectIdentifier(
                                    fullQualifiedAssociatedUserId);
                            persistClaims = true;
                        }
                    } catch (UserProfileException e) {
                        throw new FrameworkException("Error while getting association for " + subject, e);
                    }
                }
                break;
            }
        }

        if (persistClaims) {

            if (log.isDebugEnabled()) {
                log.debug("Local user mapping found. Claims will be persisted");
            }

            try {
                String tenantDomain =
                        context.getSequenceConfig().getApplicationConfig().getServiceProvider().getOwner().getTenantDomain();
                String spName = context.getSequenceConfig().getApplicationConfig().getApplicationName();

                ApplicationManagementServiceImpl applicationManagementService =
                        ApplicationManagementServiceImpl.getInstance();
                Map<String, String> claimMapping =
                        applicationManagementService.getServiceProviderToLocalIdPClaimMapping(spName, tenantDomain);

                Map<String, String> localIdpClaims = new HashMap<>();
                for (Map.Entry<String, String> entry : claims.entrySet()) {
                    String localClaim = claimMapping.get(entry.getKey());
                    localIdpClaims.put(localClaim, entry.getValue());
                }

                if (log.isDebugEnabled()) {
                    log.debug("Updating user profile of user : " + user.getUserName());
                }

                UserRealm realm = getUserRealm(user.getTenantDomain());
                UserStoreManager userStoreManager =
                        realm.getUserStoreManager().getSecondaryUserStoreManager(user.getUserStoreDomain());

                userStoreManager.setUserClaimValues(user.getUserName(), localIdpClaims, null);
            } catch (UserStoreException e) {
                throw new FrameworkException(
                        "Error while updating claims for local user. Could not update profile", e);
            } catch (IdentityApplicationManagementException e) {
                throw new FrameworkException(
                        "Error while retrieving application claim mapping. Could not update profile", e);
            }
        }

        context.getSequenceConfig().getAuthenticatedUser().setUserAttributes(authenticatedUserAttributes);
        context.setProperty(FrameworkConstants.POST_AUTHENTICATION_EXTENSION_COMPLETED, true);

    }

    private String getMissingClaims(Map<String, String> mappedAttrs, Map<String, String> mandatoryClaims) {

        StringBuilder missingClaimsString = new StringBuilder();
        for (Map.Entry<String, String> entry : mandatoryClaims.entrySet()) {
            if (mappedAttrs.get(entry.getKey()) == null) {
                missingClaimsString.append(entry.getKey());
                missingClaimsString.append(",");
            }
        }
        return missingClaimsString.toString();
    }

    private UserRealm getUserRealm(String tenantDomain) throws FrameworkException {
        UserRealm realm;
        try {
            realm = AnonymousSessionUtil.getRealmByTenantDomain(
                    FrameworkServiceComponent.getRegistryService(),
                    FrameworkServiceComponent.getRealmService(), tenantDomain);
        } catch (CarbonException e) {
            throw new FrameworkException("Error occurred while retrieving the Realm for " +
                    tenantDomain + " to handle local claims", e);
        }
        return realm;
    }
}
