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

package org.wso2.carbon.identity.application.common.util;

import org.wso2.carbon.identity.action.management.api.exception.ActionMgtClientException;
import org.wso2.carbon.identity.action.management.api.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.model.EndpointConfig;
import org.wso2.carbon.identity.application.common.exception.AuthenticatorMgtClientException;
import org.wso2.carbon.identity.application.common.exception.AuthenticatorMgtException;
import org.wso2.carbon.identity.application.common.exception.AuthenticatorMgtServerException;
import org.wso2.carbon.identity.application.common.internal.ApplicationCommonServiceDataHolder;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.UserDefinedAuthenticatorEndpointConfig;
import org.wso2.carbon.identity.application.common.model.UserDefinedLocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.util.AuthenticatorMgtExceptionBuilder.AuthenticatorMgtError;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.wso2.carbon.identity.application.common.util.AuthenticatorMgtExceptionBuilder.buildServerException;
import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.Authenticator.ACTION_ID_PROPERTY;

/**
 * This class responsible for managing authenticator endpoint configurations for the user defined Local
 * authenticators.
 */
public class UserDefinedAuthenticatorEndpointConfigManager {

    /**
     * Create a new action for given endpoint configurations of the user defined authenticator.
     *
     * @param config        The Local application authenticator configuration.
     * @param tenantId      The id of Tenant domain.
     * @throws AuthenticatorMgtException If an error occurs while adding the action.
     */
    public void addEndpointConfigurations(UserDefinedLocalAuthenticatorConfig config, int tenantId)
            throws AuthenticatorMgtException {

        try {
            Action action = ApplicationCommonServiceDataHolder.getInstance().getActionManagementService()
                    .addAction(Action.ActionTypes.AUTHENTICATION.getPathParam(),
                            buildActionToCreate(config.getName(), config.getEndpointConfig().getEndpointConfig()),
                            IdentityTenantUtil.getTenantDomain(tenantId));
            Property endpointProperty = new Property();
            endpointProperty.setName(ACTION_ID_PROPERTY);
            endpointProperty.setValue(action.getId());
            config.setProperties(new Property[]{endpointProperty});
        } catch (ActionMgtException e) {
            throw handleActionMgtException(AuthenticatorMgtError.ERROR_CODE_ADDING_ENDPOINT_CONFIG,
                    e, config.getName());
        }
    }

    /**
     * Updated associated action for given updated endpoint configurations of the user defined authenticator.
     *
     * @param newConfig        The Local application authenticator configuration to be updated.
     * @param oldConfig        The current Local application authenticator configuration.
     * @param tenantId         The id of Tenant domain.
     * @throws AuthenticatorMgtException If an error occurs while updating associated action.
     */
    public void updateEndpointConfigurations(UserDefinedLocalAuthenticatorConfig newConfig,
            UserDefinedLocalAuthenticatorConfig oldConfig, int tenantId)
            throws AuthenticatorMgtException {

        String actionId = getActionIdFromProperty(oldConfig.getProperties(), oldConfig.getName());
        try {
            ApplicationCommonServiceDataHolder.getInstance().getActionManagementService()
                    .updateAction(Action.ActionTypes.AUTHENTICATION.getPathParam(),
                            actionId,
                            buildActionToUpdate(newConfig.getEndpointConfig().getEndpointConfig()),
                            IdentityTenantUtil.getTenantDomain(tenantId));
            newConfig.setProperties(oldConfig.getProperties());
        } catch (ActionMgtException e) {
            throw handleActionMgtException(AuthenticatorMgtError.ERROR_CODE_UPDATING_ENDPOINT_CONFIG, e,
                            actionId, oldConfig.getName());
        }
    }

    /**
     * Retrieve associated action of the user defined authenticator.
     *
     * @param config        The Local application authenticator configuration.
     * @param tenantId      The id of Tenant domain.
     * @return Local authenticator with endpoint configurations resolved.
     * @throws AuthenticatorMgtException If an error occurs retrieving updating associated action.
     */
    public UserDefinedLocalAuthenticatorConfig resolveEndpointConfigurations(UserDefinedLocalAuthenticatorConfig config,
            int tenantId) throws AuthenticatorMgtException {

        if (config == null) {
            return null;
        }
        String actionId = getActionIdFromProperty(config.getProperties(), config.getName());
        try {
            Action action = ApplicationCommonServiceDataHolder.getInstance().getActionManagementService()
                    .getActionByActionId(Action.ActionTypes.AUTHENTICATION.getPathParam(),
                            actionId,
                            IdentityTenantUtil.getTenantDomain(tenantId));

            config.setEndpointConfig(buildUserDefinedAuthenticatorEndpointConfig(action.getEndpoint()));
            return config;
        } catch (ActionMgtException e) {
            throw handleActionMgtException(AuthenticatorMgtError.ERROR_CODE_RETRIEVING_ENDPOINT_CONFIG, e,
                            actionId, config.getName());
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
        endpointConfigBuilder.allowedHeaders(endpointConfig.getAllowedHeaders());
        endpointConfigBuilder.allowedParameters(endpointConfig.getAllowedParameters());
        return endpointConfigBuilder.build();
    }

    /**
     * Delete associated action of the user defined authenticator.
     *
     * @param config                The Local application authenticator configuration.
     * @param tenantId              The id of Tenant domain.
     * @throws AuthenticatorMgtException If an error occurs while deleting associated action.
     */
    public void deleteEndpointConfigurations(UserDefinedLocalAuthenticatorConfig config, int tenantId) throws
            AuthenticatorMgtException {

        String actionId = getActionIdFromProperty(config.getProperties(), config.getName());
        try {
            ApplicationCommonServiceDataHolder.getInstance().getActionManagementService()
                    .deleteAction(Action.ActionTypes.AUTHENTICATION.getPathParam(),
                            actionId,
                            IdentityTenantUtil.getTenantDomain(tenantId));
        } catch (ActionMgtException e) {
            throw handleActionMgtException(AuthenticatorMgtError.ERROR_CODE_DELETING_ENDPOINT_CONFIG, e,
                    actionId, config.getName());
        }
    }

    private Action buildActionToCreate(String authenticatorName, EndpointConfig endpointConfig) {

        Action.ActionRequestBuilder actionRequestBuilder = new Action.ActionRequestBuilder();
        actionRequestBuilder.name(authenticatorName);
        actionRequestBuilder.description(String.format("This is the action associated to the user defined Local" +
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
            throws AuthenticatorMgtServerException {

        return Arrays.stream(properties)
                .filter(property -> ACTION_ID_PROPERTY.equals(property.getName()))
                .map(Property::getValue)
                .findFirst()
                .orElseThrow(() -> buildServerException(AuthenticatorMgtError.ERROR_CODE_NO_ACTION_ID_FOUND,
                        authenticatorName));
    }

    private static AuthenticatorMgtException handleActionMgtException(AuthenticatorMgtError authenticatorMgtError,
            Throwable actionException, String... data)
            throws AuthenticatorMgtException {

        if (actionException instanceof ActionMgtClientException) {
            ActionMgtClientException error = (ActionMgtClientException) actionException;
            throw new AuthenticatorMgtClientException(
                    authenticatorMgtError.getCode(), authenticatorMgtError.getMessage(), error.getDescription());
        }

        throw buildServerException(authenticatorMgtError, data);
    }
}
