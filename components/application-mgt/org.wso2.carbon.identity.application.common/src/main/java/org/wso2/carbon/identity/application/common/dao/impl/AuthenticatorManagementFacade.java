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

import org.wso2.carbon.identity.application.common.exception.AuthenticatorMgtException;
import org.wso2.carbon.identity.application.common.model.UserDefinedLocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.util.UserDefinedAuthenticatorEndpointConfigManager;
import org.wso2.carbon.identity.base.AuthenticatorPropertyConstants;

import java.util.List;

/**
 * This class responsible for managing authenticator endpoint configurations for the user defined local
 * authenticators.
 */
public class AuthenticatorManagementFacade {

    private final AuthenticatorManagementDAOImpl dao;
    private UserDefinedAuthenticatorEndpointConfigManager endpointConfigManager =
            new UserDefinedAuthenticatorEndpointConfigManager();

    public AuthenticatorManagementFacade(AuthenticatorManagementDAOImpl dao) {

        this.dao = dao;
    }

    public UserDefinedLocalAuthenticatorConfig addUserDefinedLocalAuthenticator(
            UserDefinedLocalAuthenticatorConfig authenticatorConfig,
            int tenantId, AuthenticatorPropertyConstants.AuthenticationType type) throws AuthenticatorMgtException {

        endpointConfigManager.addEndpointConfigurations(authenticatorConfig, tenantId);
        try {
            return dao.addUserDefinedLocalAuthenticator(authenticatorConfig, tenantId, type);
        } catch (AuthenticatorMgtException e) {
            endpointConfigManager.deleteEndpointConfigurations(authenticatorConfig, tenantId);
            throw e;
        }
    }
    
    public UserDefinedLocalAuthenticatorConfig updateUserDefinedLocalAuthenticator(UserDefinedLocalAuthenticatorConfig 
            existingAuthenticatorConfig, UserDefinedLocalAuthenticatorConfig newAuthenticatorConfig, 
            int tenantId) throws AuthenticatorMgtException {
        
        endpointConfigManager.updateEndpointConfigurations(newAuthenticatorConfig, existingAuthenticatorConfig,
                tenantId);
        try {
            return dao.updateUserDefinedLocalAuthenticator(existingAuthenticatorConfig, newAuthenticatorConfig,
                    tenantId);
        } catch (AuthenticatorMgtException e) {
            endpointConfigManager.updateEndpointConfigurations(existingAuthenticatorConfig, newAuthenticatorConfig,
                    tenantId);
            throw e;
        }
    }
    
    public UserDefinedLocalAuthenticatorConfig getUserDefinedLocalAuthenticator(
            String authenticatorConfigName, int tenantId) throws AuthenticatorMgtException {

        UserDefinedLocalAuthenticatorConfig config = dao.getUserDefinedLocalAuthenticator(authenticatorConfigName,
                    tenantId);
        return endpointConfigManager.resolveEndpointConfigurations(config, tenantId);
    }
    
    public List<UserDefinedLocalAuthenticatorConfig> getAllUserDefinedLocalAuthenticator(int tenantId)
            throws AuthenticatorMgtException {

        List<UserDefinedLocalAuthenticatorConfig> configList = dao.getAllUserDefinedLocalAuthenticator(tenantId);
        for (UserDefinedLocalAuthenticatorConfig config : configList) {
            endpointConfigManager.resolveEndpointConfigurations(config, tenantId);
        }
        return configList;
    }
    
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
