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

package org.wso2.carbon.identity.application.common.dao.impl;

import org.wso2.carbon.identity.application.common.dao.AuthenticatorManagementDAO;
import org.wso2.carbon.identity.application.common.exception.AuthenticatorMgtException;
import org.wso2.carbon.identity.application.common.model.UserDefinedLocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.util.UserDefinedAuthenticatorEndpointConfigManager;

import java.util.List;

/**
 * This class responsible for managing authenticator endpoint configurations for the user defined local
 * authenticators.
 */
public class AuthenticatorManagementFacade implements AuthenticatorManagementDAO {

    private final AuthenticatorManagementDAO dao;
    private UserDefinedAuthenticatorEndpointConfigManager endpointConfigManager =
            new UserDefinedAuthenticatorEndpointConfigManager();

    public AuthenticatorManagementFacade(AuthenticatorManagementDAO dao) {

        this.dao = dao;
    }

    @Override
    public UserDefinedLocalAuthenticatorConfig addUserDefinedLocalAuthenticator(
            UserDefinedLocalAuthenticatorConfig authenticatorConfig, int tenantId) throws AuthenticatorMgtException {

        endpointConfigManager.addEndpointConfigurations(authenticatorConfig, tenantId);
        try {
            return endpointConfigManager.resolveEndpointConfigurations(
                    dao.addUserDefinedLocalAuthenticator(authenticatorConfig, tenantId), tenantId);
        } catch (AuthenticatorMgtException e) {
            endpointConfigManager.deleteEndpointConfigurations(authenticatorConfig, tenantId);
            throw e;
        }
    }

    @Override
    public UserDefinedLocalAuthenticatorConfig updateUserDefinedLocalAuthenticator(UserDefinedLocalAuthenticatorConfig
            existingAuthenticatorConfig, UserDefinedLocalAuthenticatorConfig newAuthenticatorConfig,
            int tenantId) throws AuthenticatorMgtException {

        endpointConfigManager.updateEndpointConfigurations(newAuthenticatorConfig, existingAuthenticatorConfig,
                tenantId);
        try {
            return endpointConfigManager.resolveEndpointConfigurations(
                    dao.updateUserDefinedLocalAuthenticator(existingAuthenticatorConfig, newAuthenticatorConfig,
                    tenantId), tenantId);
        } catch (AuthenticatorMgtException e) {
            endpointConfigManager.updateEndpointConfigurations(existingAuthenticatorConfig, newAuthenticatorConfig,
                    tenantId);
            throw e;
        }
    }

    @Override
    public UserDefinedLocalAuthenticatorConfig getUserDefinedLocalAuthenticator(
            String authenticatorConfigName, int tenantId) throws AuthenticatorMgtException {

        UserDefinedLocalAuthenticatorConfig config = dao.getUserDefinedLocalAuthenticator(authenticatorConfigName,
                    tenantId);
        return endpointConfigManager.resolveEndpointConfigurations(config, tenantId);
    }

    @Override
    public List<UserDefinedLocalAuthenticatorConfig> getAllUserDefinedLocalAuthenticators(int tenantId)
            throws AuthenticatorMgtException {

        List<UserDefinedLocalAuthenticatorConfig> configList = dao.getAllUserDefinedLocalAuthenticators(tenantId);
        for (UserDefinedLocalAuthenticatorConfig config : configList) {
            endpointConfigManager.resolveEndpointConfigurations(config, tenantId);
        }
        return configList;
    }

    @Override
    public void deleteUserDefinedLocalAuthenticator(String authenticatorConfigName, UserDefinedLocalAuthenticatorConfig
            authenticatorConfig, int tenantId) throws AuthenticatorMgtException {

        endpointConfigManager.deleteEndpointConfigurations(authenticatorConfig, tenantId);
        try {
            dao.deleteUserDefinedLocalAuthenticator(authenticatorConfigName, authenticatorConfig, tenantId);
        } catch (AuthenticatorMgtException e) {
            endpointConfigManager.addEndpointConfigurations(authenticatorConfig, tenantId);
            throw e;
        }
    }
}
