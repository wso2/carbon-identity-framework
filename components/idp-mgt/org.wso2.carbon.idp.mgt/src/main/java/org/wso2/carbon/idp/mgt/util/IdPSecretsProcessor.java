/*
 * Copyright (c) 2023-2026, WSO2 LLC. (http://www.wso2.com).
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

import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.ProvisioningConnectorConfig;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.secret.mgt.core.SecretManager;
import org.wso2.carbon.identity.secret.mgt.core.SecretResolveManager;
import org.wso2.carbon.identity.secret.mgt.core.SecretsProcessor;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;
import org.wso2.carbon.identity.secret.mgt.core.model.ResolvedSecret;
import org.wso2.carbon.identity.secret.mgt.core.model.Secret;
import org.wso2.carbon.identity.secret.mgt.core.model.SecretType;
import org.wso2.carbon.idp.mgt.internal.IdpMgtServiceComponentHolder;

import static org.wso2.carbon.identity.secret.mgt.core.constant.SecretConstants.IDN_SECRET_TYPE_IDP_SECRETS;

/**
 * Identity provider secrets processor service implementation.
 */
public class IdPSecretsProcessor implements SecretsProcessor<IdentityProvider> {

    private static final Log log = LogFactory.getLog(IdPSecretsProcessor.class);

    private final SecretManager secretManager;
    private final SecretResolveManager secretResolveManager;
    private final Gson gson;

    public IdPSecretsProcessor() {
        this.gson = new Gson();
        secretManager = IdpMgtServiceComponentHolder.getInstance().getSecretManager();
        secretResolveManager = IdpMgtServiceComponentHolder.getInstance().getSecretResolveManager();
    }

    @Override
    public IdentityProvider decryptAssociatedSecrets(IdentityProvider identityProvider) throws SecretManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Decrypting federated authenticator secrets for IDP: "
                    + identityProvider.getIdentityProviderName());
        }

        IdentityProvider clonedIdP = gson.fromJson(gson.toJson(identityProvider), IdentityProvider.class);
        for (FederatedAuthenticatorConfig fedAuthConfig : clonedIdP.getFederatedAuthenticatorConfigs()) {
            for (Property prop : fedAuthConfig.getProperties()) {
                String secretName = buildSecretName(clonedIdP.getId(), fedAuthConfig.getName(), prop.getName());
                decryptAndSetSecretProperty(prop, secretName);
            }
        }

        return clonedIdP;
    }

    @Override
    public IdentityProvider encryptAssociatedSecrets(IdentityProvider identityProvider) throws SecretManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Encrypting federated authenticator secrets for IDP: "
                    + identityProvider.getIdentityProviderName());
        }

        IdentityProvider clonedIdP = gson.fromJson(gson.toJson(identityProvider), IdentityProvider.class);
        for (FederatedAuthenticatorConfig fedAuthConfig : clonedIdP.getFederatedAuthenticatorConfigs()) {
            for (Property prop : fedAuthConfig.getProperties()) {
                String secretName = buildSecretName(clonedIdP.getId(), fedAuthConfig.getName(), prop.getName());
                encryptAndSetSecretProperty(prop, secretName);
            }
        }

        return clonedIdP;
    }

    @Override
    public void deleteAssociatedSecrets(IdentityProvider identityProvider) throws SecretManagementException {

        // Delete federated authenticator config secrets.
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

        if (isProvisioningConfidentialConfigProtectionDisabled()) {
            return;
        }

        // Delete provisioning connector config secrets.
        for (ProvisioningConnectorConfig provConfig : identityProvider.getProvisioningConnectorConfigs()) {
            for (Property prop : provConfig.getProvisioningProperties()) {
                if (!prop.isConfidential()) {
                    continue;
                }
                String secretName = buildProvisioningSecretName(identityProvider.getId(), provConfig.getName(),
                        prop.getName());
                if (secretManager.isSecretExist(IDN_SECRET_TYPE_IDP_SECRETS, secretName)) {
                    secretManager.deleteSecret(IDN_SECRET_TYPE_IDP_SECRETS, secretName);
                }
            }
        }
    }

    /**
     * Decrypt provisioning connector config secrets only.
     * This method is used to avoid redundant decryption when provisioning connectors are loaded separately.
     *
     * @param identityProvider Identity provider.
     * @return Identity provider with decrypted provisioning connector secrets.
     * @throws SecretManagementException If an error occurs while decrypting the secrets.
     */
    public IdentityProvider decryptProvisioningConnectorSecrets(IdentityProvider identityProvider)
            throws SecretManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Decrypting provisioning connector secrets for IDP: "
                    + identityProvider.getIdentityProviderName());
        }

        if (isProvisioningConfidentialConfigProtectionDisabled()) {
            if (log.isDebugEnabled()) {
                log.debug("Provisioning confidential data protection is disabled. Skipping decryption of " +
                        "provisioning connector secrets for IDP: " + identityProvider.getIdentityProviderName());
            }
            return identityProvider;
        }

        IdentityProvider clonedIdP = gson.fromJson(gson.toJson(identityProvider), IdentityProvider.class);
        if (clonedIdP.getProvisioningConnectorConfigs() == null) {
            if (log.isDebugEnabled()) {
                log.debug("No provisioning connector configs found for IDP: " +
                        identityProvider.getIdentityProviderName());
            }
            return clonedIdP;
        }

        for (ProvisioningConnectorConfig provConfig : clonedIdP.getProvisioningConnectorConfigs()) {
            for (Property prop : provConfig.getProvisioningProperties()) {
                String secretName = buildProvisioningSecretName(clonedIdP.getId(), provConfig.getName(),
                        prop.getName());
                decryptAndSetSecretProperty(prop, secretName);
            }
        }

        return clonedIdP;
    }

    /**
     * Encrypt provisioning connector config secrets only.
     * This method is used to avoid redundant encryption when provisioning connectors are updated separately.
     *
     * @param identityProvider Identity provider.
     * @return Identity provider with encrypted provisioning connector secrets.
     * @throws SecretManagementException If an error occurs while encrypting the secrets.
     */
    public IdentityProvider encryptProvisioningConnectorSecrets(IdentityProvider identityProvider)
            throws SecretManagementException {

        if (log.isDebugEnabled()) {
            log.debug(
                    "Encrypting provisioning connector secrets for IDP: " + identityProvider.getIdentityProviderName());
        }

        if (isProvisioningConfidentialConfigProtectionDisabled()) {
            if (log.isDebugEnabled()) {
                log.debug("Provisioning confidential data protection is disabled. Skipping encryption of " +
                        "provisioning connector secrets for IDP: " + identityProvider.getIdentityProviderName());
            }
            return identityProvider;
        }

        IdentityProvider clonedIdP = gson.fromJson(gson.toJson(identityProvider), IdentityProvider.class);
        if (clonedIdP.getProvisioningConnectorConfigs() == null) {
            if (log.isDebugEnabled()) {
                log.debug("No provisioning connector configs found for IDP: " +
                        identityProvider.getIdentityProviderName());
            }
            return clonedIdP;
        }

        for (ProvisioningConnectorConfig provConfig : clonedIdP.getProvisioningConnectorConfigs()) {
            for (Property prop : provConfig.getProvisioningProperties()) {
                String secretName = buildProvisioningSecretName(clonedIdP.getId(), provConfig.getName(),
                        prop.getName());
                encryptAndSetSecretProperty(prop, secretName);
            }
        }

        return clonedIdP;
    }

    private String buildSecretName(String idpId, String fedAuthName, String propName) {

        return idpId + ":" + fedAuthName + ":" + propName;
    }

    /**
     * Build secret name for provisioning connector secrets.
     * Format: {idpId}:provisioning:{connectorName}:{propertyName}
     * The "provisioning" distinguisher prevents naming collisions when the same provider
     * (e.g., Google) is used for both federation and provisioning.
     *
     * @param idpId        Identity provider ID.
     * @param connectorName Provisioning connector name (e.g., "scim2", "google", "salesforce").
     * @param propName     Property name.
     * @return Secret name.
     */
    private String buildProvisioningSecretName(String idpId, String connectorName, String propName) {

        return idpId + ":" + "provisioning" + ":" + connectorName + ":" + propName;
    }

    /**
     * Decrypt secret property and set the original secret value to the property.
     * @param prop       Property.
     * @param secretName Secret name.
     * @throws SecretManagementException If an error occurs while decrypting the secret.
     */
    private void decryptAndSetSecretProperty(Property prop, String secretName)
            throws SecretManagementException {

        if (!prop.isConfidential()) {
            return;
        }
        if (secretManager.isSecretExist(IDN_SECRET_TYPE_IDP_SECRETS, secretName)) {
            ResolvedSecret resolvedSecret =
                    secretResolveManager.getResolvedSecret(IDN_SECRET_TYPE_IDP_SECRETS, secretName);
            // Replace secret reference with decrypted original secret.
            prop.setValue(resolvedSecret.getResolvedSecretValue());
        }
    }

    /**
     * Encrypt secret property and set the secret reference to the property.
     * @param prop       Property.
     * @param secretName Secret name.
     * @throws SecretManagementException If an error occurs while encrypting the secret.
     */
    private void encryptAndSetSecretProperty(Property prop, String secretName) throws SecretManagementException {

        if (!prop.isConfidential()) {
            return;
        }

        if (secretManager.isSecretExist(IDN_SECRET_TYPE_IDP_SECRETS, secretName)) {
            // Update existing secret property.
            updateExistingSecretProperty(secretName, prop);
            prop.setValue(buildSecretReference(secretName));
        } else {
            // Add secret to the DB.
            if (StringUtils.isEmpty(prop.getValue())) {
                return;
            }
            addNewIdpSecretProperty(secretName, prop);
            prop.setValue(buildSecretReference(secretName));
        }
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

    /**
     * Check if outbound provisioning confidential data protection is enabled.
     *
     * @return true if OUTBOUND_PROVISIONING_CONFIDENTIAL_DATA_PROTECTION_ENABLED is enabled, false otherwise.
     */
    private boolean isProvisioningConfidentialConfigProtectionDisabled() {

        return !Boolean.parseBoolean(IdentityUtil.getProperty(
                IdPManagementConstants.OUTBOUND_PROVISIONING_CONFIDENTIAL_DATA_PROTECTION_ENABLED));
    }
}
