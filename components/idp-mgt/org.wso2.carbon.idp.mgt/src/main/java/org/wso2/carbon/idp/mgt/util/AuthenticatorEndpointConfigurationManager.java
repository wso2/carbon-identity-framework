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

package org.wso2.carbon.idp.mgt.util;

import org.wso2.carbon.identity.action.management.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.model.Action;
import org.wso2.carbon.identity.action.management.model.EndpointConfig;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.UserDefinedFederatedAuthenticatorConfig;
import org.wso2.carbon.identity.base.AuthenticatorPropertyConstants;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementServerException;
import org.wso2.carbon.idp.mgt.internal.IdpMgtServiceComponentHolder;

import static org.wso2.carbon.idp.mgt.util.IdPManagementConstants.ErrorMessage.ERROR_CODE_ENDPOINT_CONFIG_MGT;

/**
 * This class responsible for managing authenticator endpoint configurations as the actions associated.
 */
public class AuthenticatorEndpointConfigurationManager {

    private static final String ACTION_ID_PROPERTY = "actionId";

    /**
     * Create a new action for given endpoint configurations of the user defined authenticator.
     *
     * @param config        The federated application authenticator configuration.
     * @param tenantId      The id of Tenant domain.
     *
     * @throws IdentityProviderManagementServerException If an error occurs while adding the action.
     */
    public void addEndpointConfigurations(FederatedAuthenticatorConfig config, int tenantId)
            throws IdentityProviderManagementServerException {

        if (config.getDefinedByType() == AuthenticatorPropertyConstants.DefinedByType.SYSTEM) {
            return;
        }

        try {
            UserDefinedFederatedAuthenticatorConfig castedConfig = (UserDefinedFederatedAuthenticatorConfig) config;
            Action action = IdpMgtServiceComponentHolder.getInstance().getActionManagementService().addAction(Action.ActionTypes.AUTHENTICATION.toString(),
                    buildActionToCreate(castedConfig.getName(), castedConfig.getEndpointConfig()),
                    IdentityTenantUtil.getTenantDomain(tenantId));
            Property endpointProperty = new Property();
            endpointProperty.setName(ACTION_ID_PROPERTY);
            endpointProperty.setValue(action.getId());
            config.setProperties(new Property[]{endpointProperty});
        } catch (ActionMgtException e) {
            throw new IdentityProviderManagementServerException(ERROR_CODE_ENDPOINT_CONFIG_MGT.getCode(),
                    "Error occurred while adding associated action for the authenticator:" + config.getName(), e);
        }
    }

    /**
     * Updated associated action for given updated endpoint configurations of the user defined authenticator.
     *
     * @param newConfig        The federated application authenticator configuration to be updated.
     * @param oldConfig        The current federated application authenticator configuration.
     * @param tenantId         The id of Tenant domain.
     *
     * @throws IdentityProviderManagementServerException If an error occurs while updating associated action.
     */
    public void updateEndpointConfigurations(FederatedAuthenticatorConfig newConfig, FederatedAuthenticatorConfig oldConfig,
                                       int tenantId) throws IdentityProviderManagementServerException {

        if (oldConfig.getDefinedByType() == AuthenticatorPropertyConstants.DefinedByType.SYSTEM) {
            return;
        }

        String actionId = getActionIdFromProperty(oldConfig.getProperties());
        try {
            UserDefinedFederatedAuthenticatorConfig castedConfig = (UserDefinedFederatedAuthenticatorConfig) newConfig;
            IdpMgtServiceComponentHolder.getInstance().getActionManagementService().updateAction(
                    Action.ActionTypes.AUTHENTICATION.toString(), actionId, buildActionToUpdate(
                            castedConfig.getEndpointConfig()), IdentityTenantUtil.getTenantDomain(tenantId));
        } catch (ActionMgtException e) {
            throw new IdentityProviderManagementServerException(ERROR_CODE_ENDPOINT_CONFIG_MGT.getCode(),
                    String.format("Error occurred while updating associated action with id %s for the authenticator %s",
                            actionId, oldConfig.getName()), e);
        }
    }

    /**
     * Retrieve associated action of the user defined authenticator.
     *
     * @param config        The federated application authenticator configuration.
     * @param tenantId      The id of Tenant domain.
     *
     * @throws IdentityProviderManagementServerException If an error occurs retrieving updating associated action.
     */
    public FederatedAuthenticatorConfig resolveEndpointConfigurations(FederatedAuthenticatorConfig config,
                        int tenantId) throws IdentityProviderManagementServerException {

        if (config.getDefinedByType() == AuthenticatorPropertyConstants.DefinedByType.SYSTEM) {
            return config;
        }

        String actionId = getActionIdFromProperty(config.getProperties());
        try {
            UserDefinedFederatedAuthenticatorConfig castedConfig = (UserDefinedFederatedAuthenticatorConfig) config;
            Action action = IdpMgtServiceComponentHolder.getInstance().getActionManagementService()
                    .getActionByActionId(Action.ActionTypes.AUTHENTICATION.toString(),
                    actionId, IdentityTenantUtil.getTenantDomain(tenantId));
            castedConfig.setEndpointConfig(action.getEndpoint());
            return castedConfig;
        } catch (ActionMgtException e) {
            throw new IdentityProviderManagementServerException(ERROR_CODE_ENDPOINT_CONFIG_MGT.getCode(),
                    String.format("Error occurred retrieving associated action with id %s for the authenticator %s",
                            actionId, config.getName()), e);
        }
    }

    /**
     * Delete associated action of the user defined authenticator.
     *
     * @param config                The federated application authenticator configuration.
     * @param tenantId              The id of Tenant domain.
     *
     * @throws IdentityProviderManagementServerException If an error occurs while deleting associated action.
     */
    public void deleteEndpointConfigurations(FederatedAuthenticatorConfig config, int tenantId) throws
            IdentityProviderManagementServerException {

        if (config.getDefinedByType() == AuthenticatorPropertyConstants.DefinedByType.SYSTEM) {
            return;
        }

        String actionId = getActionIdFromProperty(config.getProperties());
        try {
            IdpMgtServiceComponentHolder.getInstance().getActionManagementService().deleteAction(
                    Action.ActionTypes.AUTHENTICATION.toString(), actionId,
                    IdentityTenantUtil.getTenantDomain(tenantId));
        } catch (ActionMgtException e) {
            throw new IdentityProviderManagementServerException(ERROR_CODE_ENDPOINT_CONFIG_MGT.getCode(),
                    String.format("Error occurred while deleting associated action with id %s for the authenticator %s",
                            actionId, config.getName()), e);
        }
    }

    public FederatedAuthenticatorConfig createFederatedAuthenticatorConfig(AuthenticatorPropertyConstants.DefinedByType
            definedByType) {

        if (definedByType == AuthenticatorPropertyConstants.DefinedByType.SYSTEM) {
            return new FederatedAuthenticatorConfig();
        }

        return new UserDefinedFederatedAuthenticatorConfig(
                AuthenticatorPropertyConstants.AuthenticationType.IDENTIFICATION);
    }

    private Action buildActionToCreate(String authenticatorName, EndpointConfig endpointConfig) {

        Action.ActionRequestBuilder actionRequestBuilder = new Action.ActionRequestBuilder();
        actionRequestBuilder.name(authenticatorName);
        actionRequestBuilder.description(String.format("This is the action associated to the user defined federated" +
                "authenticator %s.", authenticatorName));
        actionRequestBuilder.endpoint(endpointConfig);

        return actionRequestBuilder.build();
    }

    private Action buildActionToUpdate(EndpointConfig endpointConfig) {

        Action.ActionRequestBuilder actionRequestBuilder = new Action.ActionRequestBuilder();
        actionRequestBuilder.endpoint(endpointConfig);

        return actionRequestBuilder.build();
    }

    private String getActionIdFromProperty(Property[] properties) throws IdentityProviderManagementServerException {
        for (Property property : properties) {
            if (ACTION_ID_PROPERTY.equals(property.getName())) {
                return property.getValue();
            }
        }
        throw new IdentityProviderManagementServerException("No action id found from the property.");
    }
}
