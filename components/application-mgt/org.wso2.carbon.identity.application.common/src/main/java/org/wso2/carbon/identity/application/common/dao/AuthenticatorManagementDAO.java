/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.common.dao;

import org.wso2.carbon.identity.application.common.exception.AuthenticatorMgtException;
import org.wso2.carbon.identity.application.common.model.UserDefinedLocalAuthenticatorConfig;
import org.wso2.carbon.identity.base.AuthenticatorPropertyConstants.AuthenticationType;

import java.util.List;

/**
 * This interface performs CRUD operations for the User defined Local Application Authenticator configurations.
 */
public interface AuthenticatorManagementDAO {

    /**
     * Create a new user defined Local Application Authenticator configuration.
     *
     * @param authenticatorConfig   Local Application Authenticator configuration.
     * @param tenantId              Tenant Id.
     *
     * @return Created UserDefinedLocalAuthenticatorConfig.
     * @throws AuthenticatorMgtException If an error occurs while adding the authenticator configuration.
     */
    UserDefinedLocalAuthenticatorConfig addUserDefinedLocalAuthenticator(
            UserDefinedLocalAuthenticatorConfig authenticatorConfig, int tenantId, AuthenticationType type)
            throws AuthenticatorMgtException;

    /**
     * Update a user defined Local Application Authenticator configuration.
     *
     * @param existingAuthenticatorConfig   Existing  Local Application Authenticator configuration.
     * @param updatedAuthenticatorConfig    New Local Application Authenticator configuration.
     * @param tenantId                      Tenant Id.
     *
     * @return Updated UserDefinedLocalAuthenticatorConfig.
     * @throws AuthenticatorMgtException If an error occurs while updating the authenticator configuration.
     */
    UserDefinedLocalAuthenticatorConfig updateUserDefinedLocalAuthenticator(
            UserDefinedLocalAuthenticatorConfig existingAuthenticatorConfig,
            UserDefinedLocalAuthenticatorConfig updatedAuthenticatorConfig, int tenantId)
            throws AuthenticatorMgtException;

    /**
     * Retrieve a Local user defined Application Authenticator configuration by name.
     *
     * @param authenticatorConfigName   Name of the Local Application Authenticator configuration.
     * @param tenantId                  Tenant Id.
     *
     * @return Retrieved UserDefinedLocalAuthenticatorConfig
     * @throws AuthenticatorMgtException If an error occurs while retrieving the authenticator configuration.
     */
    UserDefinedLocalAuthenticatorConfig getUserDefinedLocalAuthenticator(
            String authenticatorConfigName, int tenantId) throws AuthenticatorMgtException;

    /**
     * Retrieve all user defined Local Application Authenticator configurations.
     *
     * @param tenantId                  Tenant Id.
     *
     * @return Retrieved UserDefinedLocalAuthenticatorConfig
     * @throws AuthenticatorMgtException If an error occurs while retrieving the authenticator configurations.
     */
    List<UserDefinedLocalAuthenticatorConfig> getAllUserDefinedLocalAuthenticator(int tenantId)
            throws AuthenticatorMgtException;

    /**
     * Create a new Local Application Authenticator configuration.
     *
     * @param authenticatorConfigName   Name of the Local Application Authenticator configuration.
     * @param tenantId                  Tenant Id.
     *
     * @throws AuthenticatorMgtException If an error occurs while deleting the authenticator configuration.
     */
    void deleteUserDefinedLocalAuthenticator(String authenticatorConfigName, UserDefinedLocalAuthenticatorConfig
            authenticatorConfig, int tenantId) throws AuthenticatorMgtException;
}
