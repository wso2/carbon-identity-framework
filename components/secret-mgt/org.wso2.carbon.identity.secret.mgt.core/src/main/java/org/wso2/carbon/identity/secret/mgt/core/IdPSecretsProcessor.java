/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.secret.mgt.core;

import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;
import org.wso2.carbon.identity.secret.mgt.core.model.ResolvedSecret;
import org.wso2.carbon.identity.secret.mgt.core.model.Secret;
import org.wso2.carbon.identity.secret.mgt.core.model.SecretType;
import static org.wso2.carbon.identity.secret.mgt.core.constant.SecretConstants.IDN_SECRET_TYPE_IDP_SECRETS;

/**
 * Identity provider secrets processor service implementation.
 */
public class IdPSecretsProcessor implements SecretsProcessor<IdentityProvider> {

    private final SecretManager secretManager;
    private final SecretResolveManager secretResolveManager;
    private final Gson gson;

    public IdPSecretsProcessor() {

        this.secretManager = new SecretManagerImpl();
        this.secretResolveManager = new SecretResolveManagerImpl();
        this.gson = new Gson();
    }

    @Override
    public IdentityProvider decryptAssociatedSecrets(IdentityProvider identityProvider) throws SecretManagementException {

        IdentityProvider clonedIdP = gson.fromJson(gson.toJson(identityProvider), IdentityProvider.class);
        for (FederatedAuthenticatorConfig fedAuthConfig : clonedIdP.getFederatedAuthenticatorConfigs()) {
            for (Property prop : fedAuthConfig.getProperties()) {
                if (!prop.isConfidential()) {
                    continue;
                }
                String secretName = buildSecretName(clonedIdP.getId(), fedAuthConfig.getName(), prop.getName());
                if (secretManager.isSecretExist(IDN_SECRET_TYPE_IDP_SECRETS, secretName)) {
                    ResolvedSecret resolvedSecret =
                            secretResolveManager.getResolvedSecret(IDN_SECRET_TYPE_IDP_SECRETS, secretName);
                    // Replace secret reference with decrypted original secret.
                    prop.setValue(resolvedSecret.getResolvedSecretValue());
                }
            }
        }

        return clonedIdP;
    }

    @Override
    public IdentityProvider encryptAssociatedSecrets(IdentityProvider identityProvider) throws SecretManagementException {

        IdentityProvider clonedIdP = gson.fromJson(gson.toJson(identityProvider), IdentityProvider.class);
        for (FederatedAuthenticatorConfig fedAuthConfig : clonedIdP.getFederatedAuthenticatorConfigs()) {
            for (Property prop : fedAuthConfig.getProperties()) {
                if (!prop.isConfidential()) {
                    continue;
                }
                String secretName = buildSecretName(clonedIdP.getId(), fedAuthConfig.getName(), prop.getName());
                if (secretManager.isSecretExist(IDN_SECRET_TYPE_IDP_SECRETS, secretName)) {
                    // Update existing secret property.
                    updateExistingSecretProperty(secretName, prop);
                    prop.setValue(buildSecretReference(secretName));
                } else {
                    // Add secret to the DB.
                    if (StringUtils.isEmpty(prop.getValue())) {
                        continue;
                    }
                    addNewIdpSecretProperty(secretName, prop);
                    prop.setValue(buildSecretReference(secretName));
                }
            }
        }

        return clonedIdP;
    }

    @Override
    public void deleteAssociatedSecrets(IdentityProvider identityProvider) throws SecretManagementException {

        for (FederatedAuthenticatorConfig fedAuthConfig : identityProvider.getFederatedAuthenticatorConfigs()) {
            for (Property prop : fedAuthConfig.getProperties()) {
                if (!prop.isConfidential()) {
                    continue;
                }
                String secretName = buildSecretName(identityProvider.getId(), fedAuthConfig.getName(), prop.getName());
                if (secretManager.isSecretExist(IDN_SECRET_TYPE_IDP_SECRETS, secretName)) {
                    secretManager.deleteSecret(IDN_SECRET_TYPE_IDP_SECRETS, secretName);
                }
            }
        }
    }

    private String buildSecretName(String idpId, String fedAuthName, String propName) {

        return idpId + ":" + fedAuthName + ":" + propName;
    }

    private String buildSecretReference(String secretName) throws SecretManagementException {

        SecretType secretType = secretManager.getSecretType(IDN_SECRET_TYPE_IDP_SECRETS);
        return secretType.getId() + ":" + secretName;
    }

    private void addNewIdpSecretProperty(String secretName, Property property) throws SecretManagementException {

        Secret secret = new Secret();
        secret.setSecretName(secretName);
        secret.setSecretValue(property.getValue());
        secretManager.addSecret(IDN_SECRET_TYPE_IDP_SECRETS, secret);
    }

    private void updateExistingSecretProperty(String secretName, Property property) throws SecretManagementException {

        ResolvedSecret resolvedSecret = secretResolveManager.getResolvedSecret(IDN_SECRET_TYPE_IDP_SECRETS, secretName);
        if (!resolvedSecret.getResolvedSecretValue().equals(property.getValue())) {
            secretManager.updateSecretValue(IDN_SECRET_TYPE_IDP_SECRETS, secretName, property.getValue());
        }
    }
}
