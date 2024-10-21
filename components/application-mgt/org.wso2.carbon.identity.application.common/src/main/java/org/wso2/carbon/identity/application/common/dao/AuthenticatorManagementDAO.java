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
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.base.AuthenticatorPropertyConstants.AuthenticationType;

/**
 * This interface performs CRUD operations for the Local Application Authenticator configurations.
 */
public interface AuthenticatorManagementDAO {

    /**
     * Create a new Local Application Authenticator configuration.
     *
     * @param authenticatorConfig   Local Application Authenticator configuration.
     * @param tenantId              Tenant Id.
     *
     * @return Created LocalAuthenticatorConfig.
     * @throws AuthenticatorMgtException If an error occurs while adding the authenticator configuration.
     */
    LocalAuthenticatorConfig addUserDefinedLocalAuthenticator(
            LocalAuthenticatorConfig authenticatorConfig, Integer tenantId, AuthenticationType type)
            throws AuthenticatorMgtException;

    /**
     * Update a Local Application Authenticator configuration.
     *
     * @param existingAuthenticatorConfig   Existing  Local Application Authenticator configuration.
     * @param updatedAuthenticatorConfig    New Local Application Authenticator configuration.
     * @param tenantId                      Tenant Id.
     *
     * @return Updated LocalAuthenticatorConfig.
     * @throws AuthenticatorMgtException If an error occurs while updating the authenticator configuration.
     */
    LocalAuthenticatorConfig updateUserDefinedLocalAuthenticator(LocalAuthenticatorConfig existingAuthenticatorConfig,
            LocalAuthenticatorConfig updatedAuthenticatorConfig, Integer tenantId)
            throws AuthenticatorMgtException;

    /**
     * Retrieve a Local Application Authenticator configuration by name.
     *
     * @param authenticatorConfigName   Name of the Local Application Authenticator configuration.
     * @param tenantId                  Tenant Id.
     *
     * @return Retrieved LocalAuthenticatorConfig
     * @throws AuthenticatorMgtException If an error occurs while retrieving the authenticator configuration.
     */
    LocalAuthenticatorConfig getUserDefinedLocalAuthenticator(String authenticatorConfigName, Integer tenantId)
            throws AuthenticatorMgtException;

    /**
     * Create a new Local Application Authenticator configuration.
     *
     * @param authenticatorConfigName   Name of the Local Application Authenticator configuration.
     * @param tenantId                  Tenant Id.
     *
     * @throws AuthenticatorMgtException If an error occurs while deleting the authenticator configuration.
     */
    void deleteUserDefinedLocalAuthenticator(String authenticatorConfigName, Integer tenantId)
            throws AuthenticatorMgtException;
}
