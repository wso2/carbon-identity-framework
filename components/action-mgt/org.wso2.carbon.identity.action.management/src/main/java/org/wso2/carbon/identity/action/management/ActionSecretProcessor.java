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

package org.wso2.carbon.identity.action.management;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.action.management.internal.ActionMgtServiceComponentHolder;
import org.wso2.carbon.identity.action.management.model.AuthProperty;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;
import org.wso2.carbon.identity.secret.mgt.core.model.ResolvedSecret;
import org.wso2.carbon.identity.secret.mgt.core.model.Secret;
import org.wso2.carbon.identity.secret.mgt.core.model.SecretType;

import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.identity.action.management.constant.ActionMgtConstants.IDN_SECRET_TYPE_ACTION_SECRETS;

/**
 * Action secrets processor service implementation.
 */
public class ActionSecretProcessor {

    private SecretType secretType;

    public ActionSecretProcessor() {
    }

    public List<AuthProperty> encryptAssociatedSecrets(List<AuthProperty> authProperties, String actionId,
                                                       String authType) throws SecretManagementException {

        List<AuthProperty> encryptedAuthProperties = new ArrayList<>();
        for (AuthProperty prop : authProperties) {
            if (!prop.getIsConfidential()) {
                encryptedAuthProperties.add(prop);
                continue;
            }
            String secretName = buildSecretName(actionId, authType, prop.getName());
            if (ActionMgtServiceComponentHolder.getInstance().getSecretManager()
                    .isSecretExist(IDN_SECRET_TYPE_ACTION_SECRETS, secretName)) {
                // Update existing secret property.
                updateExistingSecretProperty(secretName, prop);
            } else {
                // Add secret to the DB.
                if (StringUtils.isEmpty(prop.getValue())) {
                    continue;
                }
                addNewActionSecretProperty(secretName, prop);
            }
            encryptedAuthProperties.add(new AuthProperty.AuthPropertyBuilder()
                    .name(prop.getName())
                    .isConfidential(prop.getIsConfidential())
                    .value(buildSecretReference(secretName)).build());
        }
        return encryptedAuthProperties;
    }

    public List<AuthProperty> decryptAssociatedSecrets(List<AuthProperty> authProperties, String actionId,
                                                       String authType) throws SecretManagementException {

        List<AuthProperty> decryptedAuthProperties = new ArrayList<>();
        for (AuthProperty prop : authProperties) {
            if (!prop.getIsConfidential()) {
                decryptedAuthProperties.add(prop);
                continue;
            }
            String secretName = buildSecretName(actionId, authType, prop.getName());
            if (ActionMgtServiceComponentHolder.getInstance().getSecretManager()
                    .isSecretExist(IDN_SECRET_TYPE_ACTION_SECRETS, secretName)) {
                ResolvedSecret resolvedSecret = ActionMgtServiceComponentHolder.getInstance().getSecretResolveManager()
                        .getResolvedSecret(IDN_SECRET_TYPE_ACTION_SECRETS, secretName);
                decryptedAuthProperties.add(new AuthProperty.AuthPropertyBuilder()
                        .name(prop.getName())
                        .isConfidential(prop.getIsConfidential())
                        .value(resolvedSecret.getResolvedSecretValue()).build());
            }
        }
        return decryptedAuthProperties;
    }

    public void deleteAssociatedSecrets(List<AuthProperty> authProperties, String actionId, String authType)
            throws SecretManagementException {

        for (AuthProperty prop : authProperties) {
            if (!prop.getIsConfidential()) {
                continue;
            }
            String secretName = buildSecretName(actionId, authType, prop.getName());
            if (ActionMgtServiceComponentHolder.getInstance().getSecretManager()
                    .isSecretExist(IDN_SECRET_TYPE_ACTION_SECRETS, secretName)) {
                ActionMgtServiceComponentHolder.getInstance().getSecretManager()
                        .deleteSecret(IDN_SECRET_TYPE_ACTION_SECRETS, secretName);
            }
        }
    }

    public List<AuthProperty> getPropertiesWithSecretReferences(List<AuthProperty> authProperties, String actionId,
                                                                String authType) throws SecretManagementException {

        List<AuthProperty> referenceUpdatedProperties = new ArrayList<>();
        for (AuthProperty prop : authProperties) {
            if (!prop.getIsConfidential()) {
                referenceUpdatedProperties.add(prop);
                continue;
            }
            referenceUpdatedProperties.add(new AuthProperty.AuthPropertyBuilder()
                    .name(prop.getName())
                    .isConfidential(prop.getIsConfidential())
                    .value(buildSecretReference(buildSecretName(actionId, authType, prop.getName()))).build());
        }
        return referenceUpdatedProperties;
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

        if (secretType == null) {
            secretType = ActionMgtServiceComponentHolder.getInstance().getSecretManager()
                    .getSecretType(IDN_SECRET_TYPE_ACTION_SECRETS);
        }
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
                .getResolvedSecret(IDN_SECRET_TYPE_ACTION_SECRETS,
                secretName);
        if (!resolvedSecret.getResolvedSecretValue().equals(property.getValue())) {
            ActionMgtServiceComponentHolder.getInstance().getSecretManager()
                    .updateSecretValue(IDN_SECRET_TYPE_ACTION_SECRETS, secretName, property.getValue());
        }
    }
}
