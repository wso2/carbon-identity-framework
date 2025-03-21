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

package org.wso2.carbon.identity.action.management.internal.util;

import org.wso2.carbon.identity.action.management.api.model.AuthProperty;
import org.wso2.carbon.identity.action.management.api.model.Authentication;
import org.wso2.carbon.identity.action.management.internal.component.ActionMgtServiceComponentHolder;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;
import org.wso2.carbon.identity.secret.mgt.core.model.ResolvedSecret;
import org.wso2.carbon.identity.secret.mgt.core.model.Secret;
import org.wso2.carbon.identity.secret.mgt.core.model.SecretType;

import java.util.ArrayList;
import java.util.List;

/**
 * Action secrets processor service implementation.
 */
public class ActionSecretProcessor {

    private static final String IDN_SECRET_TYPE_ACTION_SECRETS = "ACTION_API_ENDPOINT_AUTH_SECRETS";

    public List<AuthProperty> encryptAssociatedSecrets(Authentication authentication, String actionId)
            throws SecretManagementException {

        List<AuthProperty> encryptedAuthProperties = new ArrayList<>();
        for (AuthProperty authProperty : authentication.getProperties()) {
            if (!authProperty.getIsConfidential()) {
                encryptedAuthProperties.add(authProperty);
            } else {
                encryptedAuthProperties.add(encryptProperty(authProperty, authentication.getType().name(), actionId));
            }
        }

        return encryptedAuthProperties;
    }

    public List<AuthProperty> decryptAssociatedSecrets(Authentication authentication, String actionId)
            throws SecretManagementException {

        List<AuthProperty> decryptedAuthProperties = new ArrayList<>();
        for (AuthProperty authProperty : authentication.getProperties()) {
            if (!authProperty.getIsConfidential()) {
                decryptedAuthProperties.add(authProperty);
            } else {
                decryptedAuthProperties.add(decryptProperty(authProperty, authentication.getType().name(), actionId));
            }
        }

        return decryptedAuthProperties;
    }

    public void deleteAssociatedSecrets(Authentication authentication, String actionId)
            throws SecretManagementException {

        for (AuthProperty authProperty : authentication.getProperties()) {
            if (authProperty.getIsConfidential()) {
                String secretName = buildSecretName(actionId, authentication.getType().name(), authProperty.getName());
                if (isSecretPropertyExists(secretName)) {
                    ActionMgtServiceComponentHolder.getInstance().getSecretManager()
                            .deleteSecret(IDN_SECRET_TYPE_ACTION_SECRETS, secretName);
                }
            }
        }
    }

    public List<AuthProperty> getPropertiesWithSecretReferences(List<AuthProperty> authProperties, String actionId,
                                                                String authType) throws SecretManagementException {

        List<AuthProperty> referenceUpdatedProperties = new ArrayList<>();
        for (AuthProperty prop : authProperties) {
            if (!prop.getIsConfidential()) {
                referenceUpdatedProperties.add(prop);
            } else {
                referenceUpdatedProperties.add(new AuthProperty.AuthPropertyBuilder()
                        .name(prop.getName())
                        .isConfidential(prop.getIsConfidential())
                        .value(buildSecretReference(buildSecretName(actionId, authType, prop.getName()))).build());
            }
        }

        return referenceUpdatedProperties;
    }

    /**
     * Encrypt secret property.
     *
     * @param authProperty Authentication property object.
     * @param authType     Authentication Type
     * @param actionId     Action Id.
     * @return Encrypted Auth Property if it is a confidential property.
     * @throws SecretManagementException If an error occurs while encrypting the secret.
     */
    private AuthProperty encryptProperty(AuthProperty authProperty, String authType, String actionId)
            throws SecretManagementException {

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
     * Decrypt secret property.
     *
     * @param authProperty Authentication property object.
     * @param authType     Authentication Type.
     * @param actionId     Action Id.
     * @return Decrypted Auth Property if it is a confidential property.
     * @throws SecretManagementException If an error occurs while decrypting the secret.
     */
    private AuthProperty decryptProperty(AuthProperty authProperty, String authType, String actionId)
            throws SecretManagementException {

        String secretName = buildSecretName(actionId, authType, authProperty.getName());
        if (!isSecretPropertyExists(secretName)) {
            throw new SecretManagementException(String.format("Unable to find the Secret Property: %s of " +
                    "Auth Type: %s and Action ID: %s from the system.", authProperty.getName(), authType, actionId));
        }
        ResolvedSecret resolvedSecret = ActionMgtServiceComponentHolder.getInstance().getSecretResolveManager()
                .getResolvedSecret(IDN_SECRET_TYPE_ACTION_SECRETS, secretName);

        return new AuthProperty.AuthPropertyBuilder()
                .name(authProperty.getName())
                .isConfidential(authProperty.getIsConfidential())
                .value(resolvedSecret.getResolvedSecretValue())
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

        return ActionMgtServiceComponentHolder.getInstance().getSecretManager()
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

        SecretType secretType = ActionMgtServiceComponentHolder.getInstance().getSecretManager()
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
        ActionMgtServiceComponentHolder.getInstance().getSecretManager().addSecret(IDN_SECRET_TYPE_ACTION_SECRETS,
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

        ResolvedSecret resolvedSecret = ActionMgtServiceComponentHolder.getInstance().getSecretResolveManager()
                .getResolvedSecret(IDN_SECRET_TYPE_ACTION_SECRETS, secretName);
        if (!resolvedSecret.getResolvedSecretValue().equals(property.getValue())) {
            ActionMgtServiceComponentHolder.getInstance().getSecretManager()
                    .updateSecretValue(IDN_SECRET_TYPE_ACTION_SECRETS, secretName, property.getValue());
        }
    }
}
