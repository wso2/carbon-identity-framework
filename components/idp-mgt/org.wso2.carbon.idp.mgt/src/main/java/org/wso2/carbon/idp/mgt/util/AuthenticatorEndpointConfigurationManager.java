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
import org.wso2.carbon.identity.action.management.model.AuthProperty;
import org.wso2.carbon.identity.action.management.model.EndpointConfig;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.UserDefinedAuthenticatorEndpointConfig;
import org.wso2.carbon.identity.application.common.model.UserDefinedFederatedAuthenticatorConfig;
import org.wso2.carbon.identity.base.AuthenticatorPropertyConstants;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.idp.mgt.AuthenticatorEndpointConfigurationServerException;
import org.wso2.carbon.idp.mgt.internal.IdpMgtServiceComponentHolder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.wso2.carbon.idp.mgt.util.IdPManagementConstants.ErrorMessage.ERROR_CODE_ENDPOINT_CONFIG_MGT;

/**
 * This class responsible for managing authenticator endpoint configurations for the user defined federated
 * authenticators.
 */
public class AuthenticatorEndpointConfigurationManager {

    private static final String ACTION_ID_PROPERTY = "actionId";

    /**
     * Create a new action for given endpoint configurations of the user defined authenticator.
     *
     * @param config        The federated application authenticator configuration.
     * @param tenantId      The id of Tenant domain.
     * @throws AuthenticatorEndpointConfigurationServerException If an error occurs while adding the action.
     */
    public void addEndpointConfigurations(FederatedAuthenticatorConfig config, int tenantId)
            throws AuthenticatorEndpointConfigurationServerException {

        if (config.getDefinedByType() == AuthenticatorPropertyConstants.DefinedByType.SYSTEM) {
            return;
        }

        try {
            UserDefinedFederatedAuthenticatorConfig castedConfig = (UserDefinedFederatedAuthenticatorConfig) config;
            Action action = IdpMgtServiceComponentHolder.getInstance().getActionManagementService()
                    .addAction(Action.ActionTypes.AUTHENTICATION.getPathParam(),
                               buildActionToCreate(castedConfig.getName(),
                                                castedConfig.getEndpointConfig().getEndpointConfig()),
                               IdentityTenantUtil.getTenantDomain(tenantId));
            Property endpointProperty = new Property();
            endpointProperty.setName(ACTION_ID_PROPERTY);
            endpointProperty.setValue(action.getId());
            config.setProperties(new Property[]{endpointProperty});
        } catch (ActionMgtException e) {
            throw new AuthenticatorEndpointConfigurationServerException(ERROR_CODE_ENDPOINT_CONFIG_MGT.getCode(),
                    "Error occurred while adding associated action for the authenticator:" + config.getName(), e);
        }
    }

    /**
     * Updated associated action for given updated endpoint configurations of the user defined authenticator.
     *
     * @param newConfig        The federated application authenticator configuration to be updated.
     * @param oldConfig        The current federated application authenticator configuration.
     * @param tenantId         The id of Tenant domain.
     * @throws AuthenticatorEndpointConfigurationServerException If an error occurs while updating associated action.
     */
    public void updateEndpointConfigurations(FederatedAuthenticatorConfig newConfig, FederatedAuthenticatorConfig oldConfig,
                                       int tenantId) throws AuthenticatorEndpointConfigurationServerException {

        if (oldConfig.getDefinedByType() == AuthenticatorPropertyConstants.DefinedByType.SYSTEM) {
            return;
        }

        String actionId = getActionIdFromProperty(oldConfig.getProperties(), oldConfig.getName());
        try {
            UserDefinedFederatedAuthenticatorConfig castedConfig = (UserDefinedFederatedAuthenticatorConfig) newConfig;
            IdpMgtServiceComponentHolder.getInstance().getActionManagementService()
                    .updateAction(Action.ActionTypes.AUTHENTICATION.getPathParam(),
                                  actionId,
                                  buildActionToUpdate(castedConfig.getEndpointConfig().getEndpointConfig()),
                                  IdentityTenantUtil.getTenantDomain(tenantId));
            newConfig.setProperties(oldConfig.getProperties());
        } catch (ActionMgtException e) {
            throw new AuthenticatorEndpointConfigurationServerException(ERROR_CODE_ENDPOINT_CONFIG_MGT.getCode(),
                    String.format("Error occurred while updating associated action with id %s for the authenticator %s",
                            actionId, oldConfig.getName()), e);
        }
    }

    /**
     * Retrieve associated action of the user defined authenticator.
     *
     * @param config        The federated application authenticator configuration.
     * @param tenantId      The id of Tenant domain.
     * @return Federated authenticator with endpoint configurations resolved.
     * @throws AuthenticatorEndpointConfigurationServerException If an error occurs retrieving updating associated action.
     */
    public FederatedAuthenticatorConfig resolveEndpointConfigurations(FederatedAuthenticatorConfig config,
                        int tenantId) throws AuthenticatorEndpointConfigurationServerException {

        if (config.getDefinedByType() == AuthenticatorPropertyConstants.DefinedByType.SYSTEM) {
            return config;
        }

        String actionId = getActionIdFromProperty(config.getProperties(), config.getName());
        try {
            UserDefinedFederatedAuthenticatorConfig castedConfig = (UserDefinedFederatedAuthenticatorConfig) config;
            Action action = IdpMgtServiceComponentHolder.getInstance().getActionManagementService()
                                    .getActionByActionId(Action.ActionTypes.AUTHENTICATION.getPathParam(),
                                                         actionId,
                                                         IdentityTenantUtil.getTenantDomain(tenantId));

            castedConfig.setEndpointConfig(buildUserDefinedAuthenticatorEndpointConfig(action.getEndpoint()));
            return castedConfig;
        } catch (ActionMgtException e) {
            throw new AuthenticatorEndpointConfigurationServerException(ERROR_CODE_ENDPOINT_CONFIG_MGT.getCode(),
                    String.format("Error occurred retrieving associated action with id %s for the authenticator %s",
                            actionId, config.getName()), e);
        }
    }

    private UserDefinedAuthenticatorEndpointConfig buildUserDefinedAuthenticatorEndpointConfig(
            EndpointConfig endpointConfig) {

        UserDefinedAuthenticatorEndpointConfig.UserDefinedAuthenticatorEndpointConfigBuilder endpointConfigBuilder =
                new UserDefinedAuthenticatorEndpointConfig.UserDefinedAuthenticatorEndpointConfigBuilder();
        endpointConfigBuilder.uri(endpointConfig.getUri());
        endpointConfigBuilder.authenticationType(endpointConfig.getAuthentication().getType().getName());
        Map<String, String> propMap = new HashMap<>();
        endpointConfig.getAuthentication().getProperties()
                .forEach(prop -> propMap.put(prop.getName(), prop.getValue()));
        endpointConfigBuilder.authenticationProperties(propMap);
        return endpointConfigBuilder.build();
    }

    /**
     * Delete associated action of the user defined authenticator.
     *
     * @param config                The federated application authenticator configuration.
     * @param tenantId              The id of Tenant domain.
     *
     * @throws AuthenticatorEndpointConfigurationServerException If an error occurs while deleting associated action.
     */
    public void deleteEndpointConfigurations(FederatedAuthenticatorConfig config, int tenantId) throws
            AuthenticatorEndpointConfigurationServerException {

        if (config.getDefinedByType() == AuthenticatorPropertyConstants.DefinedByType.SYSTEM) {
            return;
        }

        String actionId = getActionIdFromProperty(config.getProperties(), config.getName());
        try {
            IdpMgtServiceComponentHolder.getInstance().getActionManagementService()
                            .deleteAction(Action.ActionTypes.AUTHENTICATION.getPathParam(),
                                          actionId,
                                          IdentityTenantUtil.getTenantDomain(tenantId));
        } catch (ActionMgtException e) {
            throw new AuthenticatorEndpointConfigurationServerException(ERROR_CODE_ENDPOINT_CONFIG_MGT.getCode(),
                    String.format("Error occurred while deleting associated action with id %s for the authenticator %s",
                            actionId, config.getName()), e);
        }
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

    private String getActionIdFromProperty(Property[] properties, String authenticatorName)
            throws AuthenticatorEndpointConfigurationServerException {

        return Arrays.stream(properties)
                .filter(property -> ACTION_ID_PROPERTY.equals(property.getName()))
                .map(Property::getValue)
                .findFirst()
                .orElseThrow(() -> new AuthenticatorEndpointConfigurationServerException(
                        "No action Id was found in the properties of the authenticator configurations for the authenticator: "
                                + authenticatorName));
    }
}
