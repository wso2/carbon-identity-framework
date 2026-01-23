/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.lang.ArrayUtils;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.config.loader.SequenceLoader;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.RequestCoordinator;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;

import java.util.Arrays;
import java.util.Map;

import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.StandardInboundProtocols.OAUTH2;

/**
 * Abstract implementation for the request coordinator.
 *
 */
public abstract class AbstractRequestCoordinator implements RequestCoordinator {

    /**
     * Returns the sequence config related to current Authentication Context.
     * @param context  Authentication Context
     * @param parameterMap Parameter Map, retrieved from (Http/etc) Request.
     * @return Generated Sequence Config.
     * @throws FrameworkException when there is an error in loading the Sequence Config, most probably error
     * in underlying data persistence layer.
     */
    public SequenceConfig getSequenceConfig(AuthenticationContext context, Map<String, String[]> parameterMap)
            throws FrameworkException {
        String requestType = context.getRequestType();
        String[] issuers = parameterMap.get(FrameworkConstants.RequestParams.ISSUER);
        String issuer = null;
        if (!ArrayUtils.isEmpty(issuers)) {
            issuer = issuers[0];
        }
        String tenantDomain = context.getTenantDomain();

        SequenceLoader sequenceBuilder = FrameworkServiceDataHolder.getInstance().getSequenceLoader();
        if (sequenceBuilder != null) {
            ServiceProvider serviceProvider = getServiceProvider(requestType, issuer, tenantDomain);
            return sequenceBuilder.getSequenceConfig(context, parameterMap, serviceProvider);
        } else {
            //Backward compatibility, Using the deprecated method.
            //TODO: Need to remove the dependency to this.
            return ConfigurationFacade.getInstance().getSequenceConfig(issuer, requestType, tenantDomain);
        }

    }

    /**
     * Returns the sequence config for a shared application.
     *
     * @param context      Authentication Context.
     * @param parameterMap Parameter Map, retrieved from (Http/etc) Request.
     * @param sharedAppId  Shared Application Id.
     * @return Generated Sequence Config.
     * @throws FrameworkException If an error occurs while retrieving the sequence config.
     */
    public SequenceConfig getSharedAppSequenceConfig(AuthenticationContext context,
                                                     Map<String, String[]> parameterMap, String sharedAppId)
            throws FrameworkException {

        String tenantDomain = context.getTenantDomain();
        SequenceLoader sequenceBuilder = FrameworkServiceDataHolder.getInstance().getSequenceLoader();
        if (sequenceBuilder != null) {
            ServiceProvider sharedApp = getServiceProviderForSharedApp(sharedAppId, tenantDomain);
            return sequenceBuilder.getSequenceConfig(context, parameterMap, sharedApp);
        } else {
            throw new FrameworkException("SequenceLoader is not available to load the sequence config for " +
                    "shared app: " + sharedAppId);
        }
    }

    /**
     * Returns the service provider form persistence layer.
     */
    protected ServiceProvider getServiceProvider(String reqType, String clientId, String tenantDomain)
            throws FrameworkException {

        ApplicationManagementService appInfo = ApplicationManagementService.getInstance();

        // special case for OpenID Connect, these clients are stored as OAuth2 clients
        if ("oidc".equals(reqType)) {
            reqType = "oauth2";
        }

        ServiceProvider serviceProvider;

        try {
            serviceProvider = appInfo.getServiceProviderByClientId(clientId, reqType, tenantDomain);
        } catch (IdentityApplicationManagementException e) {
            throw new FrameworkException("Error occurred while retrieving service provider for client ID: " + clientId
                    + " and tenant: " + tenantDomain, e);
        }
        return serviceProvider;
    }

    private ServiceProvider getServiceProviderForSharedApp(String sharedAppId, String sharedOrgTenantDomain)
            throws FrameworkException {

        ApplicationManagementService applicationManagementService = ApplicationManagementService.getInstance();
        try {
            ServiceProvider sharedApp =
                    applicationManagementService.getApplicationByResourceId(sharedAppId, sharedOrgTenantDomain);
            if (sharedApp == null) {
                throw new FrameworkException("Shared application not found for organization with handle: " +
                        sharedOrgTenantDomain + " and application id: " + sharedAppId);
            }

            InboundAuthenticationConfig inboundAuthConfig = sharedApp.getInboundAuthenticationConfig();
            InboundAuthenticationRequestConfig inboundAuth =
                    (inboundAuthConfig == null || inboundAuthConfig.getInboundAuthenticationRequestConfigs() == null)
                            ? null
                            : Arrays.stream(inboundAuthConfig.getInboundAuthenticationRequestConfigs())
                            .filter(cfg -> OAUTH2.equals(cfg.getInboundAuthType()))
                            .findAny()
                            .orElse(null);

            if (inboundAuth == null) {
                throw new FrameworkException("Inbound authentication is not configured for appId: " + sharedAppId);
            }
            String clientId = inboundAuth.getInboundAuthKey();
            return getServiceProvider(OAUTH2, clientId, sharedOrgTenantDomain);
        } catch (IdentityApplicationManagementException e) {
            throw new FrameworkException("Error while retrieving service provider for appId: " + sharedAppId, e);
        }
    }
}
