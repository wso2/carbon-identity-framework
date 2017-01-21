/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.gateway.common.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class IdentityProvider implements Serializable {

    private static final long serialVersionUID = 2199048941051702943L;

    private static final Log log = LogFactory.getLog(IdentityProvider.class);
    private static final String FILE_ELEMENT_IDENTITY_PROVIDER_NAME = "IdentityProviderName";
    private static final String FILE_ELEMENT_IDENTITY_PROVIDER_DESCRIPTION = "IdentityProviderDescription";
    private static final String FILE_ELEMENT_ALIAS = "Alias";
    private static final String FILE_ELEMENT_DISPLAY_NAME = "DisplayName";
    private static final String FILE_ELEMENT_IS_PRIMARY = "IsPrimary";
    private static final String FILE_ELEMENT_IS_ENABLED = "IsEnabled";
    private static final String FILE_ELEMENT_IS_FEDERATION_HUB = "IsFederationHub";
    private static final String FILE_ELEMENT_HOME_REALM_ID = "HomeRealmId";
    private static final String FILE_ELEMENT_PROVISIONING_ROLE = "ProvisioningRole";
    private static final String FILE_ELEMENT_FEDERATED_AUTHENTICATOR_CONFIGS = "FederatedAuthenticatorConfigs";
    private static final String FILE_ELEMENT_DEFAULT_AUTHENTICATOR_CONFIG = "DefaultAuthenticatorConfig";
    private static final String FILE_ELEMENT_PROVISIONING_CONNECTOR_CONFIGS = "ProvisioningConnectorConfigs";
    private static final String FILE_ELEMENT_DEFAULT_PROVISIONING_CONNECTOR_CONFIG =
            "DefaultProvisioningConnectorConfig";
    private static final String FILE_ELEMENT_CLAIM_CONFIG = "ClaimConfig";
    private static final String FILE_ELEMENT_CERTIFICATE = "Certificate";
    private static final String FILE_ELEMENT_PERMISSION_AND_ROLE_CONFIG = "PermissionAndRoleConfig";
    private static final String FILE_ELEMENT_JUST_IN_TIME_PROVISIONING_CONFIG = "JustInTimeProvisioningConfig";

    private String identityProviderName;
    private String identityProviderDescription;
    private String alias;
    private boolean primary;
    private boolean federationHub;
    private String homeRealmId;
    private String provisioningRole;
    private String displayName;
    private boolean enable;
    private FederatedAuthenticatorConfig[] federatedAuthenticatorConfigs = new FederatedAuthenticatorConfig[0];
    private FederatedAuthenticatorConfig defaultAuthenticatorConfig;
    private ProvisioningConnectorConfig[] provisioningConnectorConfigs = new ProvisioningConnectorConfig[0];
    private ProvisioningConnectorConfig defaultProvisioningConnectorConfig;
    private ClaimConfig claimConfig;
    private String certificate;
    private PermissionsAndRoleConfig permissionAndRoleConfig;
    private JustInTimeProvisioningConfig justInTimeProvisioningConfig;
    private IdentityProviderProperty []idpProperties = new IdentityProviderProperty[0];



    /**
     * @return
     */
    public FederatedAuthenticatorConfig[] getFederatedAuthenticatorConfigs() {
        return federatedAuthenticatorConfigs;
    }

    /**
     * @param federatedAuthenticatorConfigs
     */
    public void setFederatedAuthenticatorConfigs(
            FederatedAuthenticatorConfig[] federatedAuthenticatorConfigs) {

        if (federatedAuthenticatorConfigs == null) {
            return;
        }
        Set<FederatedAuthenticatorConfig> propertySet =
                new HashSet<FederatedAuthenticatorConfig>(Arrays.asList(federatedAuthenticatorConfigs));
        this.federatedAuthenticatorConfigs = propertySet.toArray(new FederatedAuthenticatorConfig[propertySet.size()]);
    }

    /**
     * @return
     */
    public FederatedAuthenticatorConfig getDefaultAuthenticatorConfig() {
        return defaultAuthenticatorConfig;
    }

    /**
     * @param defaultAuthenticatorConfig
     */
    public void setDefaultAuthenticatorConfig(
            FederatedAuthenticatorConfig defaultAuthenticatorConfig) {
        this.defaultAuthenticatorConfig = defaultAuthenticatorConfig;
    }

    /**
     * @return
     */
    public String getIdentityProviderName() {
        return identityProviderName;
    }

    /**
     * @param identityProviderName
     */
    public void setIdentityProviderName(String identityProviderName) {
        this.identityProviderName = identityProviderName;
    }

    /**
     * @return
     */
    public String getIdentityProviderDescription() {
        return identityProviderDescription;
    }

    /**
     * @param identityProviderDescription
     */
    public void setIdentityProviderDescription(String identityProviderDescription) {
        this.identityProviderDescription = identityProviderDescription;
    }

    /**
     * @return
     */
    public ProvisioningConnectorConfig getDefaultProvisioningConnectorConfig() {
        return defaultProvisioningConnectorConfig;
    }

    /**
     * @param defaultProvisioningConnectorConfig
     */
    public void setDefaultProvisioningConnectorConfig(
            ProvisioningConnectorConfig defaultProvisioningConnectorConfig) {
        this.defaultProvisioningConnectorConfig = defaultProvisioningConnectorConfig;
    }

    /**
     * @return
     */
    public ProvisioningConnectorConfig[] getProvisioningConnectorConfigs() {
        return provisioningConnectorConfigs;
    }

    /**
     * @param provisioningConnectorConfigs
     */
    public void setProvisioningConnectorConfigs(
            ProvisioningConnectorConfig[] provisioningConnectorConfigs) {
        if (provisioningConnectorConfigs == null) {
            return;
        }
        Set<ProvisioningConnectorConfig> propertySet =
                new HashSet<ProvisioningConnectorConfig>(Arrays.asList(provisioningConnectorConfigs));
        this.provisioningConnectorConfigs = propertySet.toArray(new ProvisioningConnectorConfig[propertySet.size()]);
    }

    /**
     * @return
     */
    public boolean isPrimary() {
        return primary;
    }

    /**
     * @param primary
     */
    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    /**
     * @return
     */
    public String getAlias() {
        return alias;
    }

    /**
     * @param alias
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * @return
     */
    public String getCertificate() {
        return certificate;
    }

    /**
     * @param certificate
     */
    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    /**
     * @return
     */
    public ClaimConfig getClaimConfig() {
        return claimConfig;
    }

    /**
     * @param claimConfig
     */
    public void setClaimConfig(ClaimConfig claimConfig) {
        this.claimConfig = claimConfig;
    }

    /**
     * @return
     */
    public PermissionsAndRoleConfig getPermissionAndRoleConfig() {
        return permissionAndRoleConfig;
    }

    /**
     * @param permissionAndRoleConfig
     */
    public void setPermissionAndRoleConfig(PermissionsAndRoleConfig permissionAndRoleConfig) {
        this.permissionAndRoleConfig = permissionAndRoleConfig;
    }

    /**
     * @return
     */
    public String getHomeRealmId() {
        return homeRealmId;
    }

    /**
     * @param homeRealmId
     */
    public void setHomeRealmId(String homeRealmId) {
        this.homeRealmId = homeRealmId;
    }

    /**
     * @return
     */
    public JustInTimeProvisioningConfig getJustInTimeProvisioningConfig() {
        return justInTimeProvisioningConfig;
    }

    /**
     * @param justInTimeProvisioningConfig
     */
    public void setJustInTimeProvisioningConfig(
            JustInTimeProvisioningConfig justInTimeProvisioningConfig) {
        this.justInTimeProvisioningConfig = justInTimeProvisioningConfig;
    }

    /**
     * This represents a federation hub identity provider.
     *
     * @return
     */
    public boolean isFederationHub() {
        return federationHub;
    }

    /**
     * @param federationHub
     */
    public void setFederationHub(boolean federationHub) {
        this.federationHub = federationHub;
    }

    /**
     * This represents a provisioning role of identity provider.
     *
     * @return
     */
    public String getProvisioningRole() {
        return provisioningRole;
    }

    /**
     * @param provisioningRole
     */
    public void setProvisioningRole(String provisioningRole) {
        this.provisioningRole = provisioningRole;
    }

    /**
     * This represents whether the idp enable.
     *
     * @return
     */
    public boolean isEnable() {
        return enable;
    }

    /**
     * @param enable
     */
    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    /**
     * This represents a display name of identity provider.
     *
     * @return
     */
    public String getDisplayName() {
        return displayName;
    }

    /*
     * <IdentityProvider> <IdentityProviderName></IdentityProviderName>
     * <IdentityProviderDescription></IdentityProviderDescription> <Alias></Alias>
     * <IsPrimary></IsPrimary> <IsFederationHub></IsFederationHub><HomeRealmId></HomeRealmId>
     * <ProvisioningRole></ProvisioningRole>
     * <FederatedAuthenticatorConfigs></FederatedAuthenticatorConfigs>
     * <DefaultAuthenticatorConfig></DefaultAuthenticatorConfig>
     * <ProvisioningConnectorConfigs></ProvisioningConnectorConfigs>
     * <DefaultProvisioningConnectorConfig></DefaultProvisioningConnectorConfig>
     * <ClaimConfig></ClaimConfig> <Certificate></Certificate>
     * <PermissionAndRoleConfig></PermissionAndRoleConfig>
     * <JustInTimeProvisioningConfig></JustInTimeProvisioningConfig> </IdentityProvider>
     */

    /**
     * @param displayName
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IdentityProvider)) return false;

        IdentityProvider that = (IdentityProvider) o;

        if (identityProviderName != null ? !identityProviderName.equals(that.identityProviderName) :
                that.identityProviderName != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return identityProviderName != null ? identityProviderName.hashCode() : 0;
    }

    /**
     * Get IDP properties
     * @return
     */
    public IdentityProviderProperty[] getIdpProperties() {
        return idpProperties;
    }

    /**
     * Set IDP Properties
     * @param idpProperties
     */
    public void setIdpProperties(IdentityProviderProperty []idpProperties) {
        this.idpProperties = idpProperties;
    }

}