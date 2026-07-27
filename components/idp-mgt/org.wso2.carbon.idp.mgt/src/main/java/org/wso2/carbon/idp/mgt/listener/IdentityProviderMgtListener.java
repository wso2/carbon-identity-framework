/*
 * Copyright (c) 2005-2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.idp.mgt.listener;

import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;

import java.util.List;

/**
 * Listener interface for Identity Provider Managements tasks.
 */
public interface IdentityProviderMgtListener {

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
     * Define any additional actions before adding resident idp
     *
     * @param identityProvider Created Resident Identity Provider
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws IdentityProviderManagementException
     */
    public boolean doPreAddResidentIdP(IdentityProvider identityProvider, String tenantDomain) throws
            IdentityProviderManagementException;

    /**
     * Define any additional actions after adding resident idp
     *
     * @param identityProvider Created Resident Identity Provider
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws IdentityProviderManagementException
     */
    public boolean doPostAddResidentIdP(IdentityProvider identityProvider, String tenantDomain) throws
            IdentityProviderManagementException;

    /**
     * Define any additional actions before updating resident idp
     *
     * @param identityProvider Updated Resident Identity Provider
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws IdentityProviderManagementException
     */
    public boolean doPreUpdateResidentIdP(IdentityProvider identityProvider, String tenantDomain) throws
            IdentityProviderManagementException;

    /**
     * Define any additional actions after updating resident idp
     *
     * @param identityProvider Updated Resident Identity Provider
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws IdentityProviderManagementException
     */
    public boolean doPostUpdateResidentIdP(IdentityProvider identityProvider, String tenantDomain) throws
            IdentityProviderManagementException;

    /**
     * Define any additional actions before deleting resident idp properties.
     *
     * @param propertyNames List of property names to be deleted.
     * @param tenantDomain Tenant domain of the resident idp.
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws IdentityProviderManagementException When an error occurs while handling the event.
     */
    default boolean doPreDeleteResidentIdpProperties(List<String> propertyNames, String tenantDomain) throws
            IdentityProviderManagementException {

        return true;
    }

    /**
     * Define any additional actions after deleting resident idp properties.
     *
     * @param propertyNames List of property names deleted.
     * @param tenantDomain Tenant domain of the resident idp.
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws IdentityProviderManagementException When an error occurs while handling the event.
     */
    default boolean doPostDeleteResidentIdpProperties(List<String> propertyNames, String tenantDomain) throws
            IdentityProviderManagementException {

        return true;
    }

    /**
     * Define any additional actions after getting the resident idp.
     *
     * @param identityProvider Resident Identity Provider
     * @return Whether the post get operations were successful.
     * @throws IdentityProviderManagementException When an error occurs while handling the event.
     */
    default boolean doPostGetResidentIdP(IdentityProvider identityProvider, String tenantDomain) throws
            IdentityProviderManagementException {

        return true;
    }

    /**
     * Define any additional actions before adding idp
     *
     * @param identityProvider Created Identity Provider
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws IdentityProviderManagementException
     */
    public boolean doPreAddIdP(IdentityProvider identityProvider, String tenantDomain) throws
            IdentityProviderManagementException;

    /**
     * Define any additional actions after adding idp
     *
     * @param identityProvider Created Identity Provider
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws IdentityProviderManagementException
     */
    public boolean doPostAddIdP(IdentityProvider identityProvider, String tenantDomain) throws
            IdentityProviderManagementException;

    /**
     * Define any additional actions before deleting idp
     *
     * @param idPName Name of the idp
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws IdentityProviderManagementException
     */
    public boolean doPreDeleteIdP(String idPName, String tenantDomain) throws IdentityProviderManagementException;

    /**
     * Define any additional actions before deleting all IdPs of a given tenant id.
     *
     * @param tenantDomain Tenant domain to delete IdPs.
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws IdentityProviderManagementException
     */
    default boolean doPreDeleteIdPs(String tenantDomain) throws IdentityProviderManagementException {

        return true;
    }

    /**
     * Define any additional actions before deleting idp.
     *
     * @param resourceId    Resource ID of the idp.
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws IdentityProviderManagementException
     */
    default boolean doPreDeleteIdPByResourceId(String resourceId, String tenantDomain) throws
            IdentityProviderManagementException {

        return true;
    }

    /**
     * Define any additional actions after deleting idp
     *
     * @param idPName Name of the idp
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws IdentityProviderManagementException
     */
    public boolean doPostDeleteIdP(String idPName, String tenantDomain) throws IdentityProviderManagementException;

    /**
     * Define any additional actions after deleting IdPs of a given tenant id.
     *
     * @param tenantDomain Tenant domain to delete IdPs.
     * @return Whether execution of the deletion must happen.
     * @throws IdentityProviderManagementException
     */
    default boolean doPostDeleteIdPs(String tenantDomain) throws IdentityProviderManagementException {

        return true;
    }

    /**
     * Define any additional actions after deleting idp
     *
     * @param resourceId        Resource ID of the idp.
     * @param identityProvider  Identity Provider.
     * @param tenantDomain      Tenant domain of IDP.
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws IdentityProviderManagementException
     */
    default boolean doPostDeleteIdPByResourceId(String resourceId, IdentityProvider identityProvider, String
            tenantDomain) throws IdentityProviderManagementException {
        return true;
    }

    /**
     * Define any additional actions before updating idp
     *
     * @param oldIdPName Name of the old idp
     * @param identityProvider Updated Identity Provider
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws IdentityProviderManagementException
     */
    public boolean doPreUpdateIdP(String oldIdPName, IdentityProvider identityProvider, String tenantDomain) throws
            IdentityProviderManagementException;

    /**
     * Define any additional actions before updating idp
     *
     * @param resourceId        Resource ID of the IDP.
     * @param identityProvider  Updated Identity Provider.
     * @param tenantDomain      Tenant domain of IDP.
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws IdentityProviderManagementException
     */
    default boolean doPreUpdateIdPByResourceId(String resourceId, IdentityProvider identityProvider, String
            tenantDomain) throws IdentityProviderManagementException {
        return true;
    }

    /**
     * Define any additional actions after updating idp
     *
     * @param oldIdPName Name of the old idp
     * @param identityProvider Updated Identity Provider
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws IdentityProviderManagementException
     */
    public boolean doPostUpdateIdP(String oldIdPName, IdentityProvider identityProvider, String tenantDomain) throws
            IdentityProviderManagementException;

    /**
     * Define any additional actions after updating idp
     *
     * @param resourceId            Resource ID of the idp.
     * @param oldIdentityProvider   Existing Identity Provider.
     * @param newIdentityProvider   New Identity Provider.
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws IdentityProviderManagementException
     */
    default boolean doPostUpdateIdPByResourceId(String resourceId, IdentityProvider oldIdentityProvider,
                                                IdentityProvider newIdentityProvider, String
            tenantDomain) throws IdentityProviderManagementException {
        return true;
    }

}
