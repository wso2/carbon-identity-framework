/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.idp.mgt.listener;

import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;

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
     * Define any additional actions after deleting idp
     *
     * @param idPName Name of the idp
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws IdentityProviderManagementException
     */
    public boolean doPostDeleteIdP(String idPName, String tenantDomain) throws IdentityProviderManagementException;

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
     * Define any additional actions after updating idp
     *
     * @param oldIdPName Name of the old idp
     * @param identityProvider Updated Identity Provider
     * @return Whether execution of this method of the underlying UserStoreManager must happen.
     * @throws IdentityProviderManagementException
     */
    public boolean doPostUpdateIdP(String oldIdPName, IdentityProvider identityProvider, String tenantDomain) throws
            IdentityProviderManagementException;

}
