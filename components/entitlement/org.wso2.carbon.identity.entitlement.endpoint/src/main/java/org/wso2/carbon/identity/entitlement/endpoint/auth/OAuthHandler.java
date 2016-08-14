/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.entitlement.endpoint.auth;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.message.Message;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.common.model.ProvisioningServiceProviderType;
import org.wso2.carbon.identity.application.common.model.ThreadLocalProvisioningServiceProvider;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationManagementUtil;
import org.wso2.carbon.identity.entitlement.endpoint.util.EntitlementEndpointConstants;
import org.wso2.carbon.identity.oauth2.OAuth2TokenValidationService;
import org.wso2.carbon.identity.oauth2.dto.OAuth2ClientApplicationDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationRequestDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationResponseDTO;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class OAuthHandler implements EntitlementAuthenticationHandler {

    private static Log log = LogFactory.getLog(BasicAuthHandler.class);
    /* constants specific to this authenticator */
    private final String BEARER_AUTH_HEADER = "Bearer";
    private final String LOCAL_PREFIX = "local";
    private final int DEFAULT_PRIORITY = 10;
    private final String LOCAL_AUTH_SERVER = "local://services";
    /* properties map to be initialized */
    private Map<String, String> properties;
    /* properties specific to this authenticator */
    private String remoteServiceURL;
    private int priority;
    private String userName;
    private String password;

    // Ideally this should be configurable. For the moment, hard code the priority.

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setDefaultPriority() {
        this.priority = DEFAULT_PRIORITY;
    }

    public void setDefaultAuthzServer() {
        this.remoteServiceURL = LOCAL_AUTH_SERVER;
    }

    public boolean canHandle(Message message, ClassResourceInfo classResourceInfo) {
        // check the "Authorization" header and if "Bearer" is there, can be handled.

        // get the map of protocol headers
        Map protocolHeaders = (TreeMap) message.get(Message.PROTOCOL_HEADERS);
        // get the value for Authorization Header
        List authzHeaders = (ArrayList) protocolHeaders
                .get(EntitlementEndpointConstants.AUTHORIZATION_HEADER);
        if (authzHeaders != null) {
            // get the authorization header value, if provided
            String authzHeader = (String) authzHeaders.get(0);
            if (authzHeader != null && authzHeader.contains(BEARER_AUTH_HEADER)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAuthenticated(Message message, ClassResourceInfo classResourceInfo) {
        // get the map of protocol headers
        Map protocolHeaders = (TreeMap) message.get(Message.PROTOCOL_HEADERS);
        // get the value for Authorization Header
        List authzHeaders = (ArrayList) protocolHeaders
                .get(EntitlementEndpointConstants.AUTHORIZATION_HEADER);
        if (authzHeaders != null) {
            // get the authorization header value, if provided
            String authzHeader = (String) authzHeaders.get(0);

            // extract access token
            String accessToken = authzHeader.trim().substring(7).trim();
            // validate access token
            try {
                OAuth2ClientApplicationDTO validationApp = this.validateAccessToken(accessToken);
                OAuth2TokenValidationResponseDTO validationResponse = null;

                if (validationApp != null) {
                    validationResponse = validationApp.getAccessTokenValidationResponse();
                }

                if (validationResponse != null && validationResponse.isValid()) {
                    String userName = validationResponse.getAuthorizedUser();
                    authzHeaders.set(0, userName);

                    // setup thread local variable to be consumed by the provisioning framework.
                    RealmService realmService = (RealmService) PrivilegedCarbonContext
                            .getThreadLocalCarbonContext().getOSGiService(RealmService.class);
                    ThreadLocalProvisioningServiceProvider serviceProvider = new ThreadLocalProvisioningServiceProvider();
                    serviceProvider.setServiceProviderName(validationApp.getConsumerKey());
                    serviceProvider
                            .setServiceProviderType(ProvisioningServiceProviderType.OAUTH);
                    serviceProvider.setClaimDialect(EntitlementEndpointConstants.DEFAULT_SCIM_DIALECT);
                    serviceProvider.setTenantDomain(MultitenantUtils.getTenantDomain(userName));
                    IdentityApplicationManagementUtil
                            .setThreadLocalProvisioningServiceProvider(serviceProvider);
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                    String tenantDomain = MultitenantUtils.getTenantDomain(userName);
                    carbonContext.setUsername(MultitenantUtils.getTenantAwareUsername(userName));
                    carbonContext.setTenantId(realmService.getTenantManager().getTenantId(tenantDomain));
                    carbonContext.setTenantDomain(tenantDomain);
                    return true;
                }
            } catch (Exception e) {
                String error = "Error in validating OAuth access token.";
                log.error(error, e);
            }
        }
        return false;
    }

    /**
     * To set the properties specific to each authenticator
     *
     * @param authenticatorProperties
     */
    public void setProperties(Map<String, String> authenticatorProperties) {
        this.properties = authenticatorProperties;
        String priorityString = properties.get(EntitlementEndpointConstants.PROPERTY_NAME_PRIORITY);
        if (priorityString != null) {
            priority = Integer.parseInt(priorityString);
        } else {
            priority = DEFAULT_PRIORITY;
        }
        String remoteURLString = properties.get(EntitlementEndpointConstants.PROPERTY_NAME_AUTH_SERVER);
        if (remoteURLString != null) {
            remoteServiceURL = remoteURLString;
        } else {
            remoteServiceURL = LOCAL_AUTH_SERVER;
        }
        userName = properties.get(EntitlementEndpointConstants.PROPERTY_NAME_USERNAME);
        password = properties.get(EntitlementEndpointConstants.PROPERTY_NAME_PASSWORD);
    }

    private String getOAuthAuthzServerURL() {
        if (remoteServiceURL != null && !remoteServiceURL.endsWith("/")) {
            remoteServiceURL += "/";
        }
        return remoteServiceURL;
    }

    private OAuth2ClientApplicationDTO validateAccessToken(String accessTokenIdentifier)
            throws Exception {

        // if it is specified to use local authz server (i.e: local://services)
        if (remoteServiceURL.startsWith(LOCAL_PREFIX)) {
            OAuth2TokenValidationRequestDTO oauthValidationRequest = new OAuth2TokenValidationRequestDTO();
            OAuth2TokenValidationRequestDTO.OAuth2AccessToken accessToken = oauthValidationRequest.new OAuth2AccessToken();
            accessToken.setTokenType(OAuthServiceClient.BEARER_TOKEN_TYPE);
            accessToken.setIdentifier(accessTokenIdentifier);
            oauthValidationRequest.setAccessToken(accessToken);

            OAuth2TokenValidationService oauthValidationService = new OAuth2TokenValidationService();
            OAuth2ClientApplicationDTO oauthValidationResponse = oauthValidationService
                    .findOAuthConsumerIfTokenIsValid(oauthValidationRequest);

            return oauthValidationResponse;
        }
        // else do a web service call to the remote authz server
        try {
            ConfigurationContext configContext = ConfigurationContextFactory
                    .createConfigurationContextFromFileSystem(null, null);
            OAuthServiceClient oauthClient = new OAuthServiceClient(getOAuthAuthzServerURL(),
                    userName, password, configContext);
            org.wso2.carbon.identity.oauth2.stub.dto.OAuth2ClientApplicationDTO validationResponse;
            validationResponse = oauthClient.findOAuthConsumerIfTokenIsValid(accessTokenIdentifier);

            OAuth2ClientApplicationDTO appDTO = new OAuth2ClientApplicationDTO();
            appDTO.setConsumerKey(validationResponse.getConsumerKey());

            OAuth2TokenValidationResponseDTO validationDto = new OAuth2TokenValidationResponseDTO();
            validationDto.setAuthorizedUser(validationResponse.getAccessTokenValidationResponse()
                    .getAuthorizedUser());
            validationDto
                    .setValid(validationResponse.getAccessTokenValidationResponse().getValid());
            appDTO.setAccessTokenValidationResponse(validationDto);
            return appDTO;
        } catch (AxisFault axisFault) {
            throw axisFault;
        } catch (Exception exception) {
            throw exception;
        }
    }
}
