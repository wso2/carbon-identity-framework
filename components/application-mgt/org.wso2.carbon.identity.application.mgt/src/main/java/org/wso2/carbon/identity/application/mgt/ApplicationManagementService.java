/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.application.mgt;

import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ApplicationBasicInfo;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.RequestPathAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;

import java.util.List;
import java.util.Map;

/**
 * Application management service abstract class.
 */
public abstract class ApplicationManagementService {

    /**
     * Get ApplicationManagementService instance.
     *
     * @return ApplicationManagementService instance
     */
    public static ApplicationManagementService getInstance() {
        return ApplicationManagementServiceImpl.getInstance();
    }

    /**
     * Creates a service provider with basic information.First we need to create
     * a role with the
     * application name. Only the users in this role will be able to edit/update
     * the application.The
     * user will assigned to the created role.Internal roles used.
     * @param serviceProvider Service Provider Name

     * @param username User Name
     * @return
     * @throws IdentityApplicationManagementException
     */
    public abstract void createApplication(ServiceProvider serviceProvider, String username)
            throws IdentityApplicationManagementException;

    /**
     * Get Application for given application name
     *
     * @param applicationName Application Name
     * @return ServiceProvider
     * @throws org.wso2.carbon.identity.application.common.IdentityApplicationManagementException
     */
    public abstract ServiceProvider getApplicationExcludingFileBasedSPs(String applicationName)
            throws IdentityApplicationManagementException;

    /**
     * Get All Application Basic Information
     * @param username User Name
     * @return ApplicationBasicInfo[]
     * @throws org.wso2.carbon.identity.application.common.IdentityApplicationManagementException
     */
    public abstract ApplicationBasicInfo[] getAllApplicationBasicInfo(String username)
            throws IdentityApplicationManagementException;

    /**
     * Update Application
     *
     * @param serviceProvider Service Provider
     * @param username User Name
     * @throws org.wso2.carbon.identity.application.common.IdentityApplicationManagementException
     */
    public abstract void updateApplication(ServiceProvider serviceProvider, String username)
            throws IdentityApplicationManagementException;

    /**
     * Delete Application
     * @param applicationName Application name
     * @param username User Name
     * @throws org.wso2.carbon.identity.application.common.IdentityApplicationManagementException
     */
    public abstract void deleteApplication(String applicationName, String username)
            throws IdentityApplicationManagementException;

    /**
     * Get Identity Provider
     * @param federatedIdPName Federated identity provider name
     * @return IdentityProvider
     * @throws org.wso2.carbon.identity.application.common.IdentityApplicationManagementException
     */
    public abstract IdentityProvider getIdentityProvider(String federatedIdPName)
            throws IdentityApplicationManagementException;

    /**
     * Get All Identity Providers
     *
     * @return IdentityProvider[]
     * @throws org.wso2.carbon.identity.application.common.IdentityApplicationManagementException
     */
    public abstract IdentityProvider[] getAllIdentityProviders()
            throws IdentityApplicationManagementException;

    /**
     * Get All Local Authenticators
     *
     * @return LocalAuthenticatorConfig[]
     * @throws org.wso2.carbon.identity.application.common.IdentityApplicationManagementException
     */
    public abstract LocalAuthenticatorConfig[] getAllLocalAuthenticators()
            throws IdentityApplicationManagementException;

    /**
     * Get All Request Path Authenticators
     *
     * @return RequestPathAuthenticatorConfig[]
     * @throws org.wso2.carbon.identity.application.common.IdentityApplicationManagementException
     */
    public abstract RequestPathAuthenticatorConfig[] getAllRequestPathAuthenticators()
            throws IdentityApplicationManagementException;

    /**
     * Get All local claim uris
     *
     * @return String[] All Local Claim Uris
     * @throws org.wso2.carbon.identity.application.common.IdentityApplicationManagementException
     */
    public abstract String[] getAllLocalClaimUris() throws IdentityApplicationManagementException;

    public abstract String getServiceProviderNameByClientIdExcludingFileBasedSPs(String clientId, String type)
            throws IdentityApplicationManagementException;

    public abstract Map<String, String> getServiceProviderToLocalIdPClaimMapping(String serviceProviderName)
            throws IdentityApplicationManagementException;

    public abstract Map<String, String> getLocalIdPToServiceProviderClaimMapping(String serviceProviderName)
            throws IdentityApplicationManagementException;

    public abstract List<String> getAllRequestedClaimsByServiceProvider(String serviceProviderName)
            throws IdentityApplicationManagementException;

    /**
     * Get application data for given client Id and type
     *
     * @param clientId Client Id
     * @param type     Type
     * @return ServiceProvider
     * @throws org.wso2.carbon.identity.application.common.IdentityApplicationManagementException
     */
    public abstract String getServiceProviderNameByClientId(String clientId, String type)
            throws IdentityApplicationManagementException;

    public abstract ServiceProvider getServiceProvider(String serviceProviderName)
            throws IdentityApplicationManagementException;

    public abstract ServiceProvider getServiceProvider(int appId) throws IdentityApplicationManagementException;

    public abstract ServiceProvider getServiceProviderByClientId(String clientId, String clientType)
            throws IdentityApplicationManagementException;
}
