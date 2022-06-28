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

package org.wso2.carbon.identity.application.mgt.dao;

import org.apache.commons.lang.NotImplementedException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ApplicationBasicInfo;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;

import java.sql.Connection;
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
     * @param applicationId
     * @return
     * @throws IdentityApplicationManagementException
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
     * Retrieve application basic information using the application name.
     *
     * @param name          Name of the application
     * @param tenantDomain  Tenant domain of the application
     * @return ApplicationBasicInfo containing the basic app information
     * @throws IdentityApplicationManagementException
     */
    default ApplicationBasicInfo getApplicationBasicInfoByName(String name, String tenantDomain)
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
}
