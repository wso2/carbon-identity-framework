/*
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.identity.application.mgt.listener;

import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.dao.ApplicationDAO;

public interface ApplicationMgtListener {

    /**
     * Get the execution order identifier for this listener.
     *
     * @return The execution order identifier integer value.
     */
    int getExecutionOrderId();

    /**
     * Get the default order identifier for this listener.
     *
     * @return default order id
     */
    public int getDefaultOrderId();

    /**
     * Check whether the listener is enabled or not
     *
     * @return true if enabled
     */
    public boolean isEnable();

    /**
     * Define any additional actions before creating an application
     *
     * @param serviceProvider Created Service Provider
     * @param tenantDomain    Tenant domain of the user
     * @param userName        User name of the user
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws IdentityApplicationManagementException
     */
    public boolean doPreCreateApplication(ServiceProvider serviceProvider, String tenantDomain, String userName)
            throws IdentityApplicationManagementException;

    /**
     * Define any additional actions after creating an application
     *
     * @param serviceProvider
     * @param tenantDomain
     * @param userName
     * @return
     * @throws IdentityApplicationManagementException
     */
    public boolean doPostCreateApplication(ServiceProvider serviceProvider, String tenantDomain, String userName)
            throws IdentityApplicationManagementException;

    /**
     * Define any additional actions before updating an application
     *
     * @param serviceProvider
     * @param tenantDomain
     * @param userName
     * @return
     * @throws IdentityApplicationManagementException
     */
    public boolean doPreUpdateApplication(ServiceProvider serviceProvider, String tenantDomain, String userName)
            throws IdentityApplicationManagementException;

    /**
     * Define any additional actions after updating an application
     *
     * @param serviceProvider
     * @param tenantDomain
     * @param userName
     * @return
     * @throws IdentityApplicationManagementException
     */
    public boolean doPostUpdateApplication(ServiceProvider serviceProvider, String tenantDomain, String userName)
            throws IdentityApplicationManagementException;

    /**
     * Define any additional actions before deleting an application
     *
     * @param applicationName
     * @param tenantDomain
     * @param userName
     * @return
     * @throws IdentityApplicationManagementException
     */
    public boolean doPreDeleteApplication(String applicationName, String tenantDomain, String userName)
            throws IdentityApplicationManagementException;

    /**
     * Define any additional actions after deleting an application
     * This method does not need to be overridden,
     * if doPostDeleteApplication(ServiceProvider serviceProvider, String tenantDomain, String userName)
     * is overridden.
     *
     * @param applicationName name of the Service Provider.
     * @param tenantDomain tenant domain of the user who created the Service Provider.
     * @param userName userName of the user who created the Service Provider.
     * @return whether the postDelete action is complete.
     * @throws IdentityApplicationManagementException
     */
    @Deprecated
    boolean doPostDeleteApplication(String applicationName, String tenantDomain, String userName)
            throws IdentityApplicationManagementException;

    /**
     * Define any additional actions after deleting an application
     * This method does not need to be overridden,
     * if doPostDeleteApplication(String applicationName, String tenantDomain, String userName)
     * is overridden.
     *
     * @param serviceProvider created Service Provider object.
     * @param tenantDomain tenant domain of the user who created the Service Provider.
     * @param userName userName of the user who created the Service Provider.
     * @return whether the postDelete action is complete.
     * @throws IdentityApplicationManagementException
     */
    default boolean doPostDeleteApplication(ServiceProvider serviceProvider, String tenantDomain, String userName)
            throws IdentityApplicationManagementException {
        return true;
    }

    /**
     * Define any additional actions before getting a service provider
     *
     * @param applicationName
     * @param tenantDomain
     * @return
     * @throws IdentityApplicationManagementException
     */
    boolean doPreGetServiceProvider(String applicationName, String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * Define any additional actions after getting a service provider
     *
     * @param applicationName
     * @param tenantDomain
     * @return
     * @throws IdentityApplicationManagementException
     */
    boolean doPostGetServiceProvider(ServiceProvider serviceProvider, String applicationName, String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * Define any additional actions before getting a service provider by client id
     *
     * @param clientId
     * @param clientType
     * @param tenantDomain
     * @return
     * @throws IdentityApplicationManagementException
     */
    boolean doPreGetServiceProviderByClientId(String clientId, String clientType, String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * Define any additional actions after getting a service provider by client id
     *
     * @param serviceprovider
     * @param clientId
     * @param clientType
     * @param tenantDomain
     * @return
     * @throws IdentityApplicationManagementException
     */
    boolean doPostGetServiceProviderByClientId(ServiceProvider serviceprovider, String clientId, String clientType, String tenantDomain)
            throws IdentityApplicationManagementException;


    /**
     * Define any additional actions before getting all applications' basic information
     *
     * @param tenantDomain
     * @param username
     * @return
     * @throws IdentityApplicationManagementException
     */
    boolean doPreGetAllApplicationBasicInfo(String tenantDomain, String username)
            throws IdentityApplicationManagementException;

    /**
     * Define any additional actions after getting all applications' basic information
     *
     * @param appDAO
     * @param tenantDomain
     * @param username
     * @return
     * @throws IdentityApplicationManagementException
     */
    boolean doPostGetAllApplicationBasicInfo(ApplicationDAO appDAO, String tenantDomain, String username)
            throws IdentityApplicationManagementException;

    /**
     * Define any additional actions before getting an application excluding file based SPs.
     *
     * @param applicationName
     * @param tenantDomain
     * @return
     * @throws IdentityApplicationManagementException
     */
    boolean doPreGetApplicationExcludingFileBasedSPs(String applicationName, String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * Define any additional actions after getting an application excluding file based SPs.
     *
     * @param serviceProvider
     * @param applicationName
     * @param tenantDomain
     * @return
     * @throws IdentityApplicationManagementException
     */
    boolean doPostGetApplicationExcludingFileBasedSPs(ServiceProvider serviceProvider, String applicationName,
                                                      String tenantDomain) throws IdentityApplicationManagementException;

    /**
     * Define any additional actions before getting all applications' basic information for matching filter.
     *
     * @param tenantDomain Tenant Domain
     * @param username User Name
     * @param filter Application name filter
     * @return Boolean based on pre processing
     * @throws IdentityApplicationManagementException
     */
    default boolean doPreGetApplicationBasicInfo(String tenantDomain, String username, String filter)
            throws IdentityApplicationManagementException {

        return true;
    }

    /**
     * Define any additional actions after getting all applications' basic information for matching filter.
     *
     * @param appDAO Application Data Access Object
     * @param tenantDomain Tenant Domain
     * @param username User Name
     * @param filter Application name filter
     * @return Boolean based on post processing
     * @throws IdentityApplicationManagementException
     */
    default boolean doPostGetApplicationBasicInfo(ApplicationDAO appDAO, String tenantDomain, String username,
                                                 String filter) throws IdentityApplicationManagementException {

        return true;
    }

    /**
     *  Define any additional actions before getting service provider name by client id.
     *
     * @param clientId
     * @param clientType
     * @param tenantDomain
     * @return
     * @throws IdentityApplicationManagementException
     */
    boolean doPreGetServiceProviderNameByClientId(String clientId, String clientType, String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * Define any additional actions after getting service provider name by client id
     *
     * @param name
     * @param clientId
     * @param clientType
     * @param tenantDomain
     * @return
     * @throws IdentityApplicationManagementException
     */
    boolean doPostGetServiceProviderNameByClientId(String name, String clientId, String clientType, String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * Define any additional actions before getting service provider name by client id, excluding file based ones.
     *
     * @param name
     * @param clientId
     * @param type
     * @param tenantDomain
     * @return
     * @throws IdentityApplicationManagementException
     */
    boolean doPreGetServiceProviderNameByClientIdExcludingFileBasedSPs(String name, String clientId, String type,
                                                                       String tenantDomain) throws IdentityApplicationManagementException;

    /**
     * Define any additional actions after getting service provider name by client id, excluding file based ones.
     *
     * @param name
     * @param clientId
     * @param type
     * @param tenantDomain
     * @return
     */
    default boolean doPostGetServiceProviderNameByClientIdExcludingFileBasedSPs(String name, String clientId, String type, String tenantDomain) {

        return true;
    }

    /**
     * Define any additional actions when importing service provider.
     *
     * @param serviceProvider service Provider
     * @throws IdentityApplicationManagementException Identity Application Management Exception
     */
    default void doImportServiceProvider(ServiceProvider serviceProvider) throws IdentityApplicationManagementException {

        return;
    }

    /**
     * Define any additional actions when exporting service provider.
     *
     * @param serviceProvider service Provider
     * @param exportSecrets   is export secrets
     * @throws IdentityApplicationManagementException Identity Application Management Exception
     */
    default void doExportServiceProvider(ServiceProvider serviceProvider, Boolean exportSecrets) throws
            IdentityApplicationManagementException {

        return;
    }

    /**
     * Define any addition actions before creating inbound keys.
     *
     * @param serviceProvider service Provider
     * @param isUpdate        isUpdate
     * @throws IdentityApplicationManagementException Identity Application Management Exception
     */
    default void onPreCreateInbound(ServiceProvider serviceProvider, boolean isUpdate) throws
            IdentityApplicationManagementException {

        return;
    }

    /**
     * Define any additional actions before creating an application template.
     *
     * @param serviceProvider SP template
     * @param tenantDomain tenant domain
     * @return true when pre import application template
     * @throws IdentityApplicationManagementException
     */
    default boolean doPreCreateApplicationTemplate(ServiceProvider serviceProvider, String tenantDomain)
            throws IdentityApplicationManagementException {

        return true;
    }

    /**
     * Define any additional actions before updating an application template.
     *
     * @param serviceProvider SP template
     * @param tenantDomain tenant domain
     * @return true when pre update application template
     * @throws IdentityApplicationManagementException
     */
    default boolean doPreUpdateApplicationTemplate(ServiceProvider serviceProvider, String tenantDomain)
            throws IdentityApplicationManagementException {

        return true;
    }
}