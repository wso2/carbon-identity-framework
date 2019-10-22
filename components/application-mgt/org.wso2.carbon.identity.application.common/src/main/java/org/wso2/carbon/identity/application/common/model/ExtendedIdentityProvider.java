/*
 * Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.common.model;

/**
 * Extended Identity Provider model for Identity Provider REST API.
 */
public class ExtendedIdentityProvider extends IdentityProvider {

    private IdentityProvider identityProvider;
    private String resourceId;
    private String imageUrl;

    public ExtendedIdentityProvider() {

        this.identityProvider = new IdentityProvider();
    }

    public ExtendedIdentityProvider(IdentityProvider identityProvider) {

        this.identityProvider = identityProvider;
    }

    public IdentityProvider getIdentityProvider() {

        return identityProvider;
    }

    public void setIdentityProvider(IdentityProvider identityProvider) {

        this.identityProvider = identityProvider;
    }

    public FederatedAuthenticatorConfig[] getFederatedAuthenticatorConfigs() {

        return this.identityProvider.getFederatedAuthenticatorConfigs();
    }

    public void setFederatedAuthenticatorConfigs(FederatedAuthenticatorConfig[] federatedAuthenticatorConfigs) {

        this.identityProvider.setFederatedAuthenticatorConfigs(federatedAuthenticatorConfigs);
    }

    public FederatedAuthenticatorConfig getDefaultAuthenticatorConfig() {

        return this.identityProvider.getDefaultAuthenticatorConfig();
    }

    public void setDefaultAuthenticatorConfig(
            FederatedAuthenticatorConfig defaultAuthenticatorConfig) {

        this.identityProvider.setDefaultAuthenticatorConfig(defaultAuthenticatorConfig);
    }

    public String getIdentityProviderName() {

        return this.identityProvider.getIdentityProviderName();
    }

    public void setIdentityProviderName(String identityProviderName) {

        this.identityProvider.setIdentityProviderName(identityProviderName);
    }

    public String getIdentityProviderDescription() {

        return this.identityProvider.getIdentityProviderDescription();
    }

    public void setIdentityProviderDescription(String identityProviderDescription) {

        this.identityProvider.setIdentityProviderDescription(identityProviderDescription);
    }

    public ProvisioningConnectorConfig getDefaultProvisioningConnectorConfig() {

        return this.identityProvider.getDefaultProvisioningConnectorConfig();
    }

    public void setDefaultProvisioningConnectorConfig(
            ProvisioningConnectorConfig defaultProvisioningConnectorConfig) {

        this.identityProvider.setDefaultProvisioningConnectorConfig(defaultProvisioningConnectorConfig);
    }

    public ProvisioningConnectorConfig[] getProvisioningConnectorConfigs() {

        return this.identityProvider.getProvisioningConnectorConfigs();
    }

    public void setProvisioningConnectorConfigs(
            ProvisioningConnectorConfig[] provisioningConnectorConfigs) {

        this.identityProvider.setProvisioningConnectorConfigs(provisioningConnectorConfigs);
    }

    public boolean isPrimary() {

        return this.identityProvider.isPrimary();
    }

    public void setPrimary(boolean primary) {

        this.identityProvider.setPrimary(primary);
    }

    public String getAlias() {

        return this.identityProvider.getAlias();
    }

    public void setAlias(String alias) {

        this.identityProvider.setAlias(alias);
    }

    public String getCertificate() {

        return this.identityProvider.getCertificate();
    }

    public void setCertificate(String certificate) {

        this.identityProvider.setCertificate(certificate);
    }

    public ClaimConfig getClaimConfig() {

        return this.identityProvider.getClaimConfig();
    }

    public void setClaimConfig(ClaimConfig claimConfig) {

        this.identityProvider.setClaimConfig(claimConfig);
    }

    public PermissionsAndRoleConfig getPermissionAndRoleConfig() {

        return this.identityProvider.getPermissionAndRoleConfig();
    }

    public void setPermissionAndRoleConfig(PermissionsAndRoleConfig permissionAndRoleConfig) {

        this.identityProvider.setPermissionAndRoleConfig(permissionAndRoleConfig);
    }

    public String getHomeRealmId() {

        return this.identityProvider.getHomeRealmId();
    }

    public void setHomeRealmId(String homeRealmId) {

        this.identityProvider.setHomeRealmId(homeRealmId);
    }

    public JustInTimeProvisioningConfig getJustInTimeProvisioningConfig() {

        return this.identityProvider.getJustInTimeProvisioningConfig();
    }

    public void setJustInTimeProvisioningConfig(
            JustInTimeProvisioningConfig justInTimeProvisioningConfig) {

        this.identityProvider.setJustInTimeProvisioningConfig(justInTimeProvisioningConfig);
    }

    public boolean isFederationHub() {

        return this.identityProvider.isFederationHub();
    }

    public void setFederationHub(boolean federationHub) {

        this.identityProvider.setFederationHub(federationHub);
    }

    public String getProvisioningRole() {

        return this.identityProvider.getProvisioningRole();
    }

    public void setProvisioningRole(String provisioningRole) {

        this.identityProvider.setProvisioningRole(provisioningRole);
    }

    public boolean isEnable() {

        return this.identityProvider.isEnable();
    }

    public void setEnable(boolean enable) {

        this.identityProvider.setEnable(enable);
    }

    public String getDisplayName() {

        return this.identityProvider.getDisplayName();
    }

    public void setDisplayName(String displayName) {

        this.identityProvider.setDisplayName(displayName);
    }

    public IdentityProviderProperty[] getIdpProperties() {

        return this.identityProvider.getIdpProperties();
    }

    public void setIdpProperties(IdentityProviderProperty[] idpProperties) {

        this.identityProvider.setIdpProperties(idpProperties);
    }

    public void setId(String id) {

        this.identityProvider.setId(id);
    }

    public String getId() {

        return this.identityProvider.getId();
    }

    public String getResourceId() {

        return resourceId;
    }

    public void setResourceId(String resourceId) {

        this.resourceId = resourceId;
    }

    public String getImageUrl() {

        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {

        this.imageUrl = imageUrl;
    }

}
