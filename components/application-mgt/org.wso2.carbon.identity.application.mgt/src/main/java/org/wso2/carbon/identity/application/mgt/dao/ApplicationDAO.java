/*
 * Copyright (c) 2014-2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.application.mgt.dao;

import org.apache.commons.lang.NotImplementedException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementServerException;
import org.wso2.carbon.identity.application.common.model.ApplicationBasicInfo;
import org.wso2.carbon.identity.application.common.model.LocalAndOutboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.RoleV2;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.TrustedApp;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.PlatformType;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This interface access the data storage layer to store/update and delete application configurations.
 */
public interface ApplicationDAO {

    /**
     * @param applicationDTO
     * @return
     * @throws IdentityApplicationManagementException
     */
    int createApplication(ServiceProvider applicationDTO, String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * @param applicationName
     * @return
     * @throws IdentityApplicationManagementException
     */
    ServiceProvider getApplication(String applicationName, String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * Get service provider when the application resides in the same tenant of the request initiated.
     *
     * @param applicationId The application id.
     * @return Service provider.
     * @throws IdentityApplicationManagementException throws when an error occurs in retrieving service provider with
     *                                                all the configurations.
     */
    ServiceProvider getApplication(int applicationId) throws IdentityApplicationManagementException;

    /**
     * @return
     * @throws IdentityApplicationManagementException
     */
    ApplicationBasicInfo[] getAllApplicationBasicInfo() throws IdentityApplicationManagementException;

    /**
     * @param applicationDTO
     * @throws IdentityApplicationManagementException
     */
    void updateApplication(ServiceProvider applicationDTO, String tenantDomain) throws
            IdentityApplicationManagementException;

    /**
     * @param applicationName
     * @throws IdentityApplicationManagementException
     */
    void deleteApplication(String applicationName) throws IdentityApplicationManagementException;

    /**
     * Delete applications of a given tenant id.
     *
     * @param tenantId The id of the tenant.
     * @throws IdentityApplicationManagementException throws when an error occurs in deleting applications.
     */
    default void deleteApplications(int tenantId) throws IdentityApplicationManagementException {

    }

    /**
     * @param applicationID
     * @return
     * @throws IdentityApplicationManagementException
     */
    String getApplicationName(int applicationID) throws IdentityApplicationManagementException;

    /**
     * @param clientId
     * @param clientType
     * @param tenantDomain
     * @return
     * @throws IdentityApplicationManagementException
     */
    String getServiceProviderNameByClientId(String clientId, String clientType, String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * Retrieve application resource id using the inboundKey and inboundType.
     *
     * @param inboundKey   inboundKey
     * @param inboundType  inboundType
     * @param tenantDomain tenantDomain
     * @return application resourceId
     * @throws IdentityApplicationManagementException IdentityApplicationManagementException
     */
    default String getApplicationResourceIDByInboundKey(String inboundKey, String inboundType, String tenantDomain)
            throws IdentityApplicationManagementException {

            return null;
    }

    /**
     * Get authenticators configured for an application.
     *
     * @param applicationId ID of an application.
     * @return Authentication configurations.
     * @throws IdentityApplicationManagementException
     * @deprecated use {@link #getConfiguredAuthenticators(String, String)} instead.
     */
    @Deprecated
    LocalAndOutboundAuthenticationConfig getConfiguredAuthenticators(String applicationId)
            throws IdentityApplicationManagementException;

    /**
     * Get authenticators configured for an application.
     *
     * @param applicationId ID of an application.
     * @param tenantDomain  Tenant Domain.
     * @return Authentication configurations.
     * @throws IdentityApplicationManagementException
     */
    LocalAndOutboundAuthenticationConfig getConfiguredAuthenticators(String applicationId, String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * [sp-claim-uri,local-idp-claim-uri]
     *
     * @param serviceProviderName
     * @param tenantDomain
     * @return
     */
    Map<String, String> getServiceProviderToLocalIdPClaimMapping(String serviceProviderName,
                                                                 String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * [local-idp-claim-uri,sp-claim-uri]
     *
     * @param serviceProviderName
     * @param tenantDomain
     * @return
     * @throws IdentityApplicationManagementException
     */
    Map<String, String> getLocalIdPToServiceProviderClaimMapping(String serviceProviderName,
                                                                 String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * Returns back the requested set of claims by the provided service provider in local idp claim
     * dialect.
     *
     * @param serviceProviderName
     * @param tenantDomain
     * @return
     */
    List<String> getAllRequestedClaimsByServiceProvider(String serviceProviderName,
                                                        String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * Checks whether the application already exists with the name.
     *
     * @param serviceProviderName Name of the service provider
     * @param tenantName          tenant name
     * @return whether the application exists or not
     */
    default boolean isApplicationExists(String serviceProviderName, String tenantName)
            throws IdentityApplicationManagementException {

        return false;
    }

    default ApplicationBasicInfo getApplicationBasicInfoByResourceId(String resourceId, String tenantDomain)
            throws IdentityApplicationManagementException {

        return null;
    }

    /**
     * Retrieve application basic information using the sp metadata property key and value.
     *
     * @param key Name of the sp metadata property key
     * @param value Value of the sp metadata property
     * @return ApplicationBasicInfo containing the basic app information
     * @throws IdentityApplicationManagementException if building {@link ApplicationBasicInfo} fails.
     */
    default ApplicationBasicInfo[] getApplicationBasicInfoBySPProperty(String key, String value)
            throws IdentityApplicationManagementException {

        return null;
    }

    /**
     * Retrieve application basic information using the application name.
     *
     * @param name          Name of the application
     * @param tenantDomain  Tenant domain of the application
     * @return ApplicationBasicInfo containing the basic app information
     * @throws IdentityApplicationManagementException if building {@link ApplicationBasicInfo} fails.
     */
    default ApplicationBasicInfo getApplicationBasicInfoByName(String name, String tenantDomain)
            throws IdentityApplicationManagementException {

        throw new NotImplementedException();
    }

    /**
     * Retrieve application UUID using the application name.
     *
     * @param name         Name of the application
     * @param tenantDomain Tenant domain of the application
     * @return Application UUID
     * @throws IdentityApplicationManagementException
     */
    default String getApplicationUUIDByName(String name, String tenantDomain)
            throws IdentityApplicationManagementException {

        throw new NotImplementedException();
    }

    default String addApplication(ServiceProvider application, String tenantDomain)
            throws IdentityApplicationManagementException {

        return null;
    }

    default ServiceProvider getApplicationByResourceId(String resourceId, String tenantDomain)
            throws IdentityApplicationManagementException {

        return null;
    }

    default void updateApplicationByResourceId(String resourceId, String tenantDomain, ServiceProvider updatedApp)
            throws IdentityApplicationManagementException {

    }

    default void deleteApplicationByResourceId(String resourceId, String tenantDomain)
            throws IdentityApplicationManagementException {

    }

    /**
     * Returns basic application information of applications that are flagged as discoverable in the given tenant
     * matching given criteria.
     *
     * @param limit        Maximum no of applications to be returned in the result set (optional).
     * @param offset       Zero based index of the first application to be returned in the result set (optional).
     * @param filter       Filter to search for applications (optional).
     * @param sortOrder    Sort order, ascending or descending (optional).
     * @param sortBy       Attribute to sort from (optional).
     * @param tenantDomain Tenant domain to be filtered from.
     * @return List of ApplicationBasicInfo of applications matching the given criteria.
     * @throws IdentityApplicationManagementException
     */
    default List<ApplicationBasicInfo> getDiscoverableApplicationBasicInfo(int limit, int offset, String filter,
                                                                           String sortOrder, String sortBy, String
                                                                                   tenantDomain) throws
            IdentityApplicationManagementException {

        return null;
    }

    /**
     * Returns basic application information of the application matching given resource Id if discoverable.
     *
     * @param resourceId   Unique resource identifier of the application.
     * @param tenantDomain Tenant domain of the application.
     * @return ApplicationBasicInfo including application basic information.
     * @throws IdentityApplicationManagementException
     */
    default ApplicationBasicInfo getDiscoverableApplicationBasicInfoByResourceId(String resourceId, String tenantDomain)
            throws IdentityApplicationManagementException {

        return null;
    }

    /**
     * Returns if application matching given resource Id in given tenant is discoverable.
     *
     * @param resourceId   Unique resource identifier of the application.
     * @param tenantDomain Tenant domain of the application.
     * @return True if application is flagged as discoverable, false otherwise.
     * @throws IdentityApplicationManagementException
     */
    default boolean isApplicationDiscoverable(String resourceId, String tenantDomain) throws
            IdentityApplicationManagementException {

        return false;
    }

    /**
     * Returns the count of discoverable applications matching given filter.
     *
     * @param filter       Filter to search for applications (optional).
     * @param tenantDomain
     * @return Count of discoverable applications matching given filter.
     * @throws IdentityApplicationManagementException
     */
    default int getCountOfDiscoverableApplications(String filter, String tenantDomain) throws
            IdentityApplicationManagementException {

        return 0;
    }

    /**
     * Method that can be run after updating components related to the service provider. Contains post application
     * dependency update tasks.
     *
     * @param serviceProvider   Service provider application.
     * @param tenantDomain      Tenant domain of the service provider.
     */
    default void clearApplicationFromCache(ServiceProvider serviceProvider, String tenantDomain)
            throws IdentityApplicationManagementException {

    }

    /**
     * Method that checks whether a claim is associated with any service provider.
     *
     * @param dbConnection  Optional DB connection.
     * @param claimUri      Claim URI.
     * @param tenantId      ID of the tenant.
     * @return  True if claim is referred by a service provider.
     * @throws IdentityApplicationManagementException   Error when obtaining claim references.
     */
    default boolean isClaimReferredByAnySp(Connection dbConnection, String claimUri, int tenantId)
            throws IdentityApplicationManagementException {

        return false;
    }

    /**
     * Method that returns service provider with required attributes.
     *
     * @param applicationId       Application identifier.
     * @param requiredAttributes  List of required attributes.
     * @return  ServiceProvider with required attributes added.
     * @throws IdentityApplicationManagementException   Error when obtaining Sp with required attributes.
     */
    default ServiceProvider getApplicationWithRequiredAttributes(int applicationId, List<String> requiredAttributes)
            throws IdentityApplicationManagementException {

        return new ServiceProvider();
    }

    /**
     * Method that return the application id of the main application for a given shared application id.
     *
     * @param sharedAppId Shared application id.
     * @return Application id of the main application.
     * @throws IdentityApplicationManagementServerException Error when obtaining main application id.
     */
    default String getMainAppId(String sharedAppId) throws IdentityApplicationManagementServerException {

        throw new NotImplementedException();
    }

    /**
     * Method that returns the shared application id in the given shared organization for the given main application.
     *
     * @param mainAppId   Main application id.
     * @param ownerOrgId  Owner organization id of the main application.
     * @param sharedOrgId Shared organization id for which the shared application id is requested.
     * @return Shared application id in the given shared organization for the given main application.
     * @throws IdentityApplicationManagementServerException Error when obtaining shared application id.
     */
    default String getSharedAppId(String mainAppId, String ownerOrgId, String sharedOrgId)
            throws IdentityApplicationManagementServerException {

        throw new NotImplementedException();
    }

    /**
     * Method that returns the id the owner organization of the main application of the given shared app.
     *
     * @param sharedAppId Shared application id.
     * @return Owner organization id of the given shared application.
     * @throws IdentityApplicationManagementServerException Error when obtaining owner organization id.
     */
    default String getOwnerOrgId(String sharedAppId) throws IdentityApplicationManagementServerException {

        throw new NotImplementedException();
    }

    /**
     * Method that returns the shared application ids of the main application.
     *
     * @param mainAppId    Main application id.
     * @param ownerOrgId   Owner organization id.
     * @param sharedOrgIds List of shared organization ids.
     * @return Map containing shared application ids and their organization ids.
     * @throws IdentityApplicationManagementServerException Error when obtaining shared applications.
     */
    default Map<String, String> getSharedApplicationIds(String mainAppId, String ownerOrgId, List<String> sharedOrgIds)
            throws IdentityApplicationManagementServerException {

        throw new NotImplementedException();
    }

    /**
     * Method that returns the tenant id of the application.
     *
     * @param applicationId Application id.
     * @return Tenant id of the application.
     * @throws IdentityApplicationManagementServerException Error when obtaining tenant id.
     */
    default int getTenantIdByApp(String applicationId) throws IdentityApplicationManagementServerException {

        throw new NotImplementedException();
    }

    /**
     * Method that returns the SP property value by property key.
     *
     * @param applicationId Application UUID.
     * @param propertyName Property key.
     * @param tenantDomain Tenant domain.
     * @return Property value.
     * @throws IdentityApplicationManagementException Error when retrieving SP property value.
     */
    default String getSPPropertyValueByPropertyKey(String applicationId, String propertyName, String tenantDomain)
            throws IdentityApplicationManagementException {

        throw new NotImplementedException();
    }

    /**
     * Method that returns the associated roles of the application.
     *
     * @param applicationId Application UUID.
     * @param tenantDomain  Tenant domain.
     * @return List of associated roles.
     * @throws IdentityApplicationManagementException Error when retrieving associated roles.
     */
    default List<RoleV2> getAssociatedRolesOfApplication(String applicationId, String tenantDomain)
            throws IdentityApplicationManagementException {

        throw new NotImplementedException();
    }

    /**
     * Create an association between a role and an application.
     *
     * @param applicationUUID Application UUID.
     * @param roleId          Role ID.
     * @throws IdentityApplicationManagementException if an error occurs while adding role to application.
     */
    default void addAssociatedRoleToApplication(String applicationUUID, String roleId)
            throws IdentityApplicationManagementException {

        throw new NotImplementedException();
    }

    /**
     * Returns the list of trusted applications of all tenants based on the requested platform type.
     *
     * @param platformType Platform type of the trusted apps.
     * @return List of trusted apps of all tenants.
     * @throws IdentityApplicationManagementException If an error occurs while retrieving the trusted apps.
     */
    default List<TrustedApp> getTrustedApps(PlatformType platformType) throws IdentityApplicationManagementException {

        return new ArrayList<>();
    }

    /**
     * Retrieve the service provider resource IDs associated with the default federated IDP authenticator.
     *
     * @param idpName                  Name of the identity provider.
     * @param defaultAuthenticatorName default authenticator name.
     * @param tenantDomain             Tenant domain of Identity Provider.
     * @return SPs resource ID list.
     * @throws IdentityApplicationManagementException Error when getting SP resource IDs.
     */
    default String[] getSPsAssociatedWithFederatedIDPAuthenticator(String idpName,
                                                                   String defaultAuthenticatorName,
                                                                   String tenantDomain)
            throws IdentityApplicationManagementException {

        return new String[0];
    }

    /**
     * Update the local and outbound authentication configuration of a service provider.
     *
     * @param applicationDTO Updated service provider instance.
     * @param tenantDomain   Tenant domain of Service Provider.
     * @throws IdentityApplicationManagementException Error when updating local and outbound auth configs for the SP.
     */
    default void updateApplicationLocalAndOutboundAuthConfig(ServiceProvider applicationDTO, String tenantDomain)
            throws IdentityApplicationManagementException {

    }
}
