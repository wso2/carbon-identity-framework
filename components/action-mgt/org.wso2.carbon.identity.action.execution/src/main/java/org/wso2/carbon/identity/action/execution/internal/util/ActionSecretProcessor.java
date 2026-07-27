/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.action.execution.internal.util;

import org.wso2.carbon.identity.action.execution.internal.component.ActionExecutionServiceComponentHolder;
import org.wso2.carbon.identity.action.management.api.model.AuthProperty;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;
import org.wso2.carbon.identity.secret.mgt.core.model.ResolvedSecret;
import org.wso2.carbon.identity.secret.mgt.core.model.Secret;
import org.wso2.carbon.identity.secret.mgt.core.model.SecretType;

/**
 * Action secrets processor service implementation.
 */
public class ActionSecretProcessor {

    private static final String IDN_SECRET_TYPE_ACTION_SECRETS = "ACTION_API_ENDPOINT_AUTH_SECRETS";

    /**
     * Encrypt secret property.
     *
     * @param authProperty Authentication property object.
     * @param authType     Authentication Type
     * @param actionId     Action Id.
     * @return Encrypted Auth Property if it is a confidential property.
     * @throws SecretManagementException If an error occurs while encrypting the secret.
     */
    public AuthProperty encryptProperty(AuthProperty authProperty, String authType, String actionId)
            throws SecretManagementException {

        if (!authProperty.getIsConfidential()) {
            return authProperty;
        }

        String secretName = buildSecretName(actionId, authType, authProperty.getName());
        if (isSecretPropertyExists(secretName)) {
            updateExistingSecretProperty(secretName, authProperty);
        } else {
            addNewActionSecretProperty(secretName, authProperty);
        }

        return new AuthProperty.AuthPropertyBuilder()
                .name(authProperty.getName())
                .isConfidential(authProperty.getIsConfidential())
                .value(buildSecretReference(secretName))
                .build();
    }

    /**
     * Check whether the secret property exists.
     *
     * @param secretName Secret Name.
     * @return True if the secret property exists.
     * @throws SecretManagementException If an error occurs while checking the existence of the secret.
     */
    private boolean isSecretPropertyExists(String secretName) throws SecretManagementException {

        return ActionExecutionServiceComponentHolder.getInstance().getSecretManager()
                .isSecretExist(IDN_SECRET_TYPE_ACTION_SECRETS, secretName);
    }

    /**
     * Create secret name.
     *
     * @param actionId     Action Id.
     * @param authType     Authentication Type.
     * @param authProperty Authentication Property.
     * @return Secret Name.
     */
    private String buildSecretName(String actionId, String authType, String authProperty) {

        return actionId + ":" + authType + ":" + authProperty;
    }

    /**
     * Create secret reference name.
     *
     * @param secretName Name of the secret.
     * @return Secret reference name.
     * @throws SecretManagementException If an error occurs while retrieving the secret type.
     */
    private String buildSecretReference(String secretName) throws SecretManagementException {

        SecretType secretType = ActionExecutionServiceComponentHolder.getInstance().getSecretManager()
                .getSecretType(IDN_SECRET_TYPE_ACTION_SECRETS);
        return secretType.getId() + ":" + secretName;
    }

    /**
     * Add new Secret for Action secret type.
     *
     * @param secretName Name of the secret.
     * @param property   Secret property.
     * @throws SecretManagementException If an error occurs while adding the secret.
     */
    private void addNewActionSecretProperty(String secretName, AuthProperty property) throws SecretManagementException {

        Secret secret = new Secret();
        secret.setSecretName(secretName);
        secret.setSecretValue(property.getValue());
        ActionExecutionServiceComponentHolder.getInstance().getSecretManager().addSecret(IDN_SECRET_TYPE_ACTION_SECRETS,
                secret);
    }

    /**
     * Update an existing secret of Action secret type.
     *
     * @param secretName Name of the secret.
     * @param property   Secret property.
     * @throws SecretManagementException If an error occurs while adding the secret.
     */
    private void updateExistingSecretProperty(String secretName, AuthProperty property)
            throws SecretManagementException {

        ResolvedSecret resolvedSecret = ActionExecutionServiceComponentHolder.getInstance().getSecretResolveManager()
                .getResolvedSecret(IDN_SECRET_TYPE_ACTION_SECRETS, secretName);
        if (!resolvedSecret.getResolvedSecretValue().equals(property.getValue())) {
            ActionExecutionServiceComponentHolder.getInstance().getSecretManager()
                    .updateSecretValue(IDN_SECRET_TYPE_ACTION_SECRETS, secretName, property.getValue());
        }
    }
}
