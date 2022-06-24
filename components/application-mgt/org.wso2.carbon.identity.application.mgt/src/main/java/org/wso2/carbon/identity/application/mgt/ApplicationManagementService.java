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

import org.apache.commons.lang.NotImplementedException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ApplicationBasicInfo;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.ImportResponse;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.RequestPathAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.SpFileContent;
import org.wso2.carbon.identity.application.common.model.SpTemplate;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponentHolder;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Application management service abstract class.
 */
public abstract class ApplicationManagementService implements ApplicationPaginationAndSearching,
        ApplicationResourceManager {

    /**
     * Get ApplicationManagementService instance.
     *
     * @return ApplicationManagementService instance
     */
    public static ApplicationManagementService getInstance() {
        return ApplicationManagementServiceImpl.getInstance();
    }

    /**
     * Creates a service provider with basic information and returns the created service provider. First we need to
     * create an internal role with the application name. Only the users in this role will be able to edit/update
     * the application.Then the user will assigned to the created role.
     *
     * @param serviceProvider Service Provider Name
     * @param tenantDomain Tenant Domain
     * @param username User Name
     * @return created service provider
     * @throws IdentityApplicationManagementException
     */
    @Deprecated
    public abstract ServiceProvider addApplication(ServiceProvider serviceProvider, String tenantDomain, String
            username) throws IdentityApplicationManagementException;

    /**
     * Creates a service provider with the provided service provider template.
     *
     * @param serviceProvider Service Provider Name
     * @param tenantDomain Tenant Domain
     * @param username User Name
     * @param templateName SP template name
     * @return created service provider
     * @throws IdentityApplicationManagementException
     */
    public abstract ServiceProvider createApplicationWithTemplate(ServiceProvider serviceProvider, String tenantDomain,
                                                                  String username, String templateName)
            throws IdentityApplicationManagementException;

    /**
     * Get Application for given application name
     *
     * @param applicationName Application Name
     * @param tenantDomain Tenant Domain
     * @return ServiceProvider
     * @throws org.wso2.carbon.identity.application.common.IdentityApplicationManagementException
     */
    public abstract ServiceProvider getApplicationExcludingFileBasedSPs(String applicationName, String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * Get All Application Basic Information
     *
     * @param tenantDomain Tenant Domain
     * @param username User Name
     * @return ApplicationBasicInfo[]
     * @throws org.wso2.carbon.identity.application.common.IdentityApplicationManagementException
     */
    public abstract ApplicationBasicInfo[] getAllApplicationBasicInfo(String tenantDomain, String username)
            throws IdentityApplicationManagementException;

    /**
     * Get all basic application information for a matching filter.
     *
     * @param tenantDomain Tenant Domain
     * @param username User Name
     * @param filter Application name filter
     * @return Application Basic Information array
     * @throws org.wso2.carbon.identity.application.common.IdentityApplicationManagementException
     */
    public abstract ApplicationBasicInfo[] getApplicationBasicInfo(String tenantDomain, String username, String filter)
            throws IdentityApplicationManagementException;

    /**
     * Update Application
     *
     * @param tenantDomain Tenant Domain
     * @param serviceProvider Service Provider
     * @param username User Name
     * @throws org.wso2.carbon.identity.application.common.IdentityApplicationManagementException
     */
    public abstract void updateApplication(ServiceProvider serviceProvider, String tenantDomain, String username)
            throws IdentityApplicationManagementException;

    /**
     * Delete Application
     *
     * @param tenantDomain Tenant Domain
     * @param applicationName Application name
     * @param username User Name
     * @throws org.wso2.carbon.identity.application.common.IdentityApplicationManagementException
     */
    public abstract void deleteApplication(String applicationName, String tenantDomain, String username)
            throws IdentityApplicationManagementException;

    /**
     * Delete Applications by tenant id.
     *
     * @param tenantId The id of the tenant.
     * @throws org.wso2.carbon.identity.application.common.IdentityApplicationManagementException
     */
    public abstract void deleteApplications(int tenantId) throws IdentityApplicationManagementException;

    /**
     * Get Identity Provider
     *
     * @param tenantDomain Tenant Domain
     * @param federatedIdPName Federated identity provider name
     * @return IdentityProvider
     * @throws org.wso2.carbon.identity.application.common.IdentityApplicationManagementException
     */
    public abstract IdentityProvider getIdentityProvider(String federatedIdPName, String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * Get All Identity Providers
     *
     * @param tenantDomain Tenant Domain
     * @return IdentityProvider[]
     * @throws org.wso2.carbon.identity.application.common.IdentityApplicationManagementException
     */
    public abstract IdentityProvider[] getAllIdentityProviders(String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * Get All Local Authenticators
     *
     * @param tenantDomain Tenant Domain
     * @return LocalAuthenticatorConfig[]
     * @throws org.wso2.carbon.identity.application.common.IdentityApplicationManagementException
     */
    public abstract LocalAuthenticatorConfig[] getAllLocalAuthenticators(String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * Get All Request Path Authenticators
     *
     * @param tenantDomain Tenant Domain
     * @return RequestPathAuthenticatorConfig[]
     * @throws org.wso2.carbon.identity.application.common.IdentityApplicationManagementException
     */
    public abstract RequestPathAuthenticatorConfig[] getAllRequestPathAuthenticators(String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * Get All local claim uris
     *
     * @param tenantDomain Tenant Domain
     * @return String[] All Local Claim Uris
     * @throws org.wso2.carbon.identity.application.common.IdentityApplicationManagementException
     */
    public abstract String[] getAllLocalClaimUris(String tenantDomain) throws IdentityApplicationManagementException;

    public abstract String getServiceProviderNameByClientIdExcludingFileBasedSPs(String clientId, String type, String
            tenantDomain)
            throws IdentityApplicationManagementException;

    public abstract Map<String, String> getServiceProviderToLocalIdPClaimMapping(String serviceProviderName,
                                                                          String tenantDomain)
            throws IdentityApplicationManagementException;

    public abstract Map<String, String> getLocalIdPToServiceProviderClaimMapping(String serviceProviderName,
                                                                          String tenantDomain)
            throws IdentityApplicationManagementException;

    public abstract List<String> getAllRequestedClaimsByServiceProvider(String serviceProviderName,
                                                                 String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * Get application data for given client Id and type
     *
     * @param clientId Client Id
     * @param type     Type
     * @param tenantDomain Tenant Domain
     * @return ServiceProvider
     * @throws org.wso2.carbon.identity.application.common.IdentityApplicationManagementException
     */
    public abstract String getServiceProviderNameByClientId(String clientId, String type, String tenantDomain)
            throws IdentityApplicationManagementException;

    public abstract ServiceProvider getServiceProvider(String serviceProviderName, String tenantDomain)
            throws IdentityApplicationManagementException;

    public abstract ServiceProvider getServiceProvider(int appId) throws IdentityApplicationManagementException;

    public abstract ServiceProvider getServiceProviderByClientId(String clientId, String clientType,
                                                                 String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * Export Service Provider application with required attributes.
     *
     * @param applicationId      ID of the SP
     * @param requiredAttributes List of required attributes.
     * @return SP with required attributes attached.
     * @throws IdentityApplicationManagementException Identity Application Management Exception
     */
    public abstract ServiceProvider getApplicationWithRequiredAttributes(int applicationId,
                                                                         List<String> requiredAttributes)
            throws IdentityApplicationManagementException;

    /**
     * Export Service Provider application using application ID.
     *
     * @param applicationId ID of the SP
     * @param exportSecrets Specify whether to export the secrets or not.
     * @param tenantDomain  Tenant domain
     * @return xml string of the SP
     * @throws IdentityApplicationManagementException Identity Application Management Exception
     */
    public String exportSPApplicationFromAppID(String applicationId, boolean exportSecrets,
                                               String tenantDomain) throws IdentityApplicationManagementException {

        return null;
    }

    /**
     * Export Service Provider application.
     *
     * @param applicationName name of the SP
     * @param exportSecrets   is export the secrets
     * @param tenantDomain    tenant Domain
     * @return xml string of the SP
     * @throws IdentityApplicationManagementException Identity Application Management Exception
     */
    public abstract String exportSPApplication(String applicationName, boolean exportSecrets, String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * Import Service Provider application from file.
     *
     * @param spFileContent xml string of the SP and file name
     * @param tenantDomain  tenant Domain
     * @param username      username
     * @param isUpdate      isUpdate
     * @return ImportResponse
     * @throws IdentityApplicationManagementException Identity Application Management Exception
     */
    public abstract ImportResponse importSPApplication(SpFileContent spFileContent, String tenantDomain, String
            username, boolean isUpdate) throws IdentityApplicationManagementException;

    /**
     * Import Service Provider application from object.
     *
     * @param serviceProvider
     * @param tenantDomain
     * @param username
     * @param isUpdate
     * @return ImportResponse
     * @throws IdentityApplicationManagementException
     */
    public abstract ImportResponse importSPApplication(ServiceProvider serviceProvider, String tenantDomain, String
            username, boolean isUpdate) throws IdentityApplicationManagementException;

    /**
     * Create Service provider template.
     *
     * @param spTemplate service provider template info
     * @param tenantDomain  tenant domain
     * @throws IdentityApplicationManagementException
     */
    public abstract void createApplicationTemplate(SpTemplate spTemplate, String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * Add configured service provider as a template.
     *
     * @param serviceProvider Service provider to be configured as a template
     * @param spTemplate   service provider template basic info
     * @param tenantDomain  tenant domain
     * @throws IdentityApplicationManagementException
     */
    public abstract void createApplicationTemplateFromSP(ServiceProvider serviceProvider, SpTemplate spTemplate,
                                                                   String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * Get Service provider template.
     *
     * @param templateName template name
     * @param tenantDomain tenant domain
     * @return service provider template info
     * @throws IdentityApplicationManagementException
     */
    public abstract SpTemplate getApplicationTemplate(String templateName, String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * Delete a application template.
     *
     * @param templateName name of the template
     * @param tenantDomain tenant domain
     * @throws IdentityApplicationManagementException
     */
    public abstract void deleteApplicationTemplate(String templateName, String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * Update an application template.
     *
     * @param templateName name of the template
     * @param spTemplate SP template info to be updated
     * @param tenantDomain  tenant domain
     * @throws IdentityApplicationManagementException
     */
    public abstract void updateApplicationTemplate(String templateName, SpTemplate spTemplate,
                                                             String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * Check existence of a application template.
     *
     * @param templateName template name
     * @param tenantDomain tenant domain
     * @return true if a template with the specified name exists
     * @throws IdentityApplicationManagementException
     */
    public abstract boolean isExistingApplicationTemplate(String templateName, String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * Get template info of all the service provider templates.
     *
     * @param tenantDomain tenant domain
     * @return list of all application template info
     * @throws IdentityApplicationManagementException
     */
    public abstract List<SpTemplate> getAllApplicationTemplateInfo(String tenantDomain)
            throws IdentityApplicationManagementException;

    @Override
    public ApplicationBasicInfo[] getApplicationBasicInfo(String tenantDomain, String username, String filter,
                                                          int offset, int limit)
            throws IdentityApplicationManagementException {

        return new ApplicationBasicInfo[0];
    }

    /**
     * Retrieve application basic information using the application name.
     *
     * @param name          Name of the application
     * @param tenantDomain  Tenant domain of the application
     * @return ApplicationBasicInfo containing the basic app information
     * @throws IdentityApplicationManagementException
     */
    public ApplicationBasicInfo getApplicationBasicInfoByName(String name, String tenantDomain)
            throws IdentityApplicationManagementException {

        throw new NotImplementedException();
    }

    /**
     * Get custom inbound authenticator configurations.
     *
     * @return custom inbound authenticator configs maps.
     * Ex: cas:cas -> CAS_Authenticator_Config_Object
     */
    public Map<String, AbstractInboundAuthenticatorConfig> getAllInboundAuthenticatorConfig() {

        return ApplicationManagementServiceComponentHolder.getAllInboundAuthenticatorConfig();
    }

    @Override
    public ApplicationBasicInfo[] getApplicationBasicInfo(String tenantDomain, String username, int offset,
                                                          int limit) throws IdentityApplicationManagementException {

        return new ApplicationBasicInfo[0];
    }

    /**
     * Retrieve the set of authentication templates configured from file system in JSON format.
     *
     * @return Authentication templates.
     */
    public String getAuthenticationTemplatesJSON() {

        return ApplicationManagementServiceComponentHolder.getInstance().getAuthenticationTemplatesJson();
    }

    /**
     * Get system applications defined for the server.
     *
     * @return system applications set.
     */
    public Set<String> getSystemApplications() {

        return Collections.emptySet();
    }

}

