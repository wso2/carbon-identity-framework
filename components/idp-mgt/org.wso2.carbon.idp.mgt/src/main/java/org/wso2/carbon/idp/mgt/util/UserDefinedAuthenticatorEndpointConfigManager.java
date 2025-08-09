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

import org.wso2.carbon.identity.action.management.api.exception.ActionMgtClientException;
import org.wso2.carbon.identity.action.management.api.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.model.EndpointConfig;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.UserDefinedAuthenticatorEndpointConfig;
import org.wso2.carbon.identity.application.common.model.UserDefinedFederatedAuthenticatorConfig;
import org.wso2.carbon.identity.base.AuthenticatorPropertyConstants;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementClientException;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementServerException;
import org.wso2.carbon.idp.mgt.internal.IdpMgtServiceComponentHolder;
import org.wso2.carbon.idp.mgt.util.IdPManagementConstants.ErrorMessage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * This class responsible for managing authenticator endpoint configurations for the user defined federated
 * authenticators.
 */
public class UserDefinedAuthenticatorEndpointConfigManager {

    private static final String ACTION_ID_PROPERTY = "actionId";

    /**
     * Create a new action for given endpoint configurations of the user defined authenticator.
     *
     * @param config        The federated application authenticator configuration.
     * @param tenantDomain      The id of Tenant domain.
     * @throws IdentityProviderManagementServerException If an error occurs while adding the action.
     */
    public void addEndpointConfig(FederatedAuthenticatorConfig config, String tenantDomain)
            throws IdentityProviderManagementException {

        if (config.getDefinedByType() != AuthenticatorPropertyConstants.DefinedByType.USER) {
            return;
        }

        try {
            UserDefinedFederatedAuthenticatorConfig castedConfig = (UserDefinedFederatedAuthenticatorConfig) config;
            Action action = IdpMgtServiceComponentHolder.getInstance().getActionManagementService()
                    .addAction(Action.ActionTypes.AUTHENTICATION.getPathParam(),
                               buildActionToCreate(castedConfig.getName(),
                                                castedConfig.getEndpointConfig().getEndpointConfig()),
                               tenantDomain);
            Property endpointProperty = new Property();
            endpointProperty.setName(ACTION_ID_PROPERTY);
            endpointProperty.setValue(action.getId());
            endpointProperty.setConfidential(false);
            config.setProperties(new Property[]{endpointProperty});
        } catch (ActionMgtException e) {
            throw handleActionMgtException(ErrorMessage.ERROR_CODE_ADDING_ENDPOINT_CONFIG, e, config.getName());
        }
    }

    /**
     * Updated associated action for given updated endpoint configurations of the user defined authenticator.
     *
     * @param newConfig        The federated application authenticator configuration to be updated.
     * @param oldConfig        The current federated application authenticator configuration.
     * @param tenantDomain         The id of Tenant domain.
     * @throws IdentityProviderManagementServerException If an error occurs while updating associated action.
     */
    public void updateEndpointConfig(FederatedAuthenticatorConfig newConfig, FederatedAuthenticatorConfig oldConfig,
                                       String tenantDomain) throws IdentityProviderManagementException {

        if (oldConfig.getDefinedByType() != AuthenticatorPropertyConstants.DefinedByType.USER) {
            return;
        }

        String actionId = getActionIdFromProperty(oldConfig.getProperties(), oldConfig.getName());
        try {
            UserDefinedFederatedAuthenticatorConfig castedConfig = (UserDefinedFederatedAuthenticatorConfig) newConfig;
            IdpMgtServiceComponentHolder.getInstance().getActionManagementService()
                    .updateAction(Action.ActionTypes.AUTHENTICATION.getPathParam(),
                                  actionId,
                                  buildActionToUpdate(castedConfig.getEndpointConfig().getEndpointConfig()),
                                  tenantDomain);
            newConfig.setProperties(oldConfig.getProperties());
        } catch (ActionMgtException e) {
            throw handleActionMgtException(ErrorMessage.ERROR_CODE_UPDATING_ENDPOINT_CONFIG, e, newConfig.getName());
        }
    }

    /**
     * Retrieve associated action of the user defined authenticator.
     *
     * @param config        The federated application authenticator configuration.
     * @param tenantDomain      The id of Tenant domain.
     * @return Federated authenticator with endpoint configurations resolved.
     * @throws IdentityProviderManagementServerException If an error occurs retrieving updating associated action.
     */
    public FederatedAuthenticatorConfig resolveEndpointConfig(FederatedAuthenticatorConfig config,
                        String tenantDomain) throws IdentityProviderManagementException {

        if (config.getDefinedByType() != AuthenticatorPropertyConstants.DefinedByType.USER) {
            return config;
        }

        String actionId = getActionIdFromProperty(config.getProperties(), config.getName());
        try {
            UserDefinedFederatedAuthenticatorConfig castedConfig = (UserDefinedFederatedAuthenticatorConfig) config;
            Action action = IdpMgtServiceComponentHolder.getInstance().getActionManagementService()
                                    .getActionByActionId(Action.ActionTypes.AUTHENTICATION.getPathParam(),
                                                         actionId,
                                                         tenantDomain);

            castedConfig.setEndpointConfig(buildUserDefinedAuthenticatorEndpointConfig(action.getEndpoint()));
            return castedConfig;
        } catch (ActionMgtException e) {
            throw handleActionMgtException(ErrorMessage.ERROR_CODE_RETRIEVING_ENDPOINT_CONFIG, e, config.getName());
        }
    }

    private UserDefinedAuthenticatorEndpointConfig buildUserDefinedAuthenticatorEndpointConfig(
            EndpointConfig endpointConfig) {

        Map<String, String> propMap = new HashMap<>();
        endpointConfig.getAuthentication().getProperties()
                .forEach(prop -> propMap.put(prop.getName(), prop.getValue()));
        return new UserDefinedAuthenticatorEndpointConfig.UserDefinedAuthenticatorEndpointConfigBuilder()
                .uri(endpointConfig.getUri())
                .authenticationType(endpointConfig.getAuthentication().getType().getName())
                .authenticationProperties(propMap)
                .allowedHeaders(endpointConfig.getAllowedHeaders())
                .allowedParameters(endpointConfig.getAllowedParameters())
                .build();
    }

    /**
     * Delete associated action of the user defined authenticator.
     *
     * @param config                The federated application authenticator configuration.
     * @param tenantDomain              The id of Tenant domain.
     *
     * @throws IdentityProviderManagementServerException If an error occurs while deleting associated action.
     */
    public void deleteEndpointConfig(FederatedAuthenticatorConfig config, String tenantDomain) throws
            IdentityProviderManagementException {

        if (config.getDefinedByType() != AuthenticatorPropertyConstants.DefinedByType.USER) {
            return;
        }

        String actionId = getActionIdFromProperty(config.getProperties(), config.getName());
        try {
            IdpMgtServiceComponentHolder.getInstance().getActionManagementService()
                            .deleteAction(Action.ActionTypes.AUTHENTICATION.getPathParam(),
                                          actionId,
                                          tenantDomain);
        } catch (ActionMgtException e) {
            throw handleActionMgtException(ErrorMessage.ERROR_CODE_DELETING_ENDPOINT_CONFIG, e, config.getName());
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
            throws IdentityProviderManagementServerException {

        return Arrays.stream(properties)
                .filter(property -> ACTION_ID_PROPERTY.equals(property.getName()))
                .map(Property::getValue)
                .findFirst()
                .orElseThrow(() -> new IdentityProviderManagementServerException(
                        "No action Id was found in the properties of the authenticator configurations for the " +
                                "authenticator: " + authenticatorName));
    }

    private static IdentityProviderManagementClientException handleActionMgtException(ErrorMessage idpMgtError,
                                                                      Throwable actionException, String... data)
            throws IdentityProviderManagementException {

        if (actionException instanceof ActionMgtClientException) {
            ActionMgtClientException error = (ActionMgtClientException) actionException;
            throw new IdentityProviderManagementClientException(
                    idpMgtError.getCode(), idpMgtError.getMessage(), error.getDescription());
        }

        throw new IdentityProviderManagementServerException(idpMgtError.getCode(),
                String.format(idpMgtError.getMessage(), data), actionException);
    }
}
