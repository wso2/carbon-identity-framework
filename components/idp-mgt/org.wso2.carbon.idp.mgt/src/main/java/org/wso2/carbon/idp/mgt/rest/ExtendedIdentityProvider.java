/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.idp.mgt.rest;

import org.wso2.carbon.identity.application.common.model.ClaimConfig;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.IdentityProviderProperty;
import org.wso2.carbon.identity.application.common.model.JustInTimeProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.PermissionsAndRoleConfig;
import org.wso2.carbon.identity.application.common.model.ProvisioningConnectorConfig;

public class ExtendedIdentityProvider extends IdentityProvider {

    private IdentityProvider identityProvider;

    public ExtendedIdentityProvider(IdentityProvider identityProvider) {

        this.identityProvider = identityProvider;
    }

    /**
     * @return
     */
    public FederatedAuthenticatorConfig[] getFederatedAuthenticatorConfigs() {

        return this.getFederatedAuthenticatorConfigs();
    }

    /**
     * @return
     */
    public FederatedAuthenticatorConfig getDefaultAuthenticatorConfig() {

        return this.identityProvider.getDefaultAuthenticatorConfig();
    }

    /**
     * @return
     */
    public String getIdentityProviderName() {

        return this.identityProvider.getIdentityProviderName();
    }

    /**
     * @return
     */
    public String getIdentityProviderDescription() {

        return this.identityProvider.getIdentityProviderDescription();
    }

    /**
     * @return
     */
    public ProvisioningConnectorConfig getDefaultProvisioningConnectorConfig() {

        return this.getDefaultProvisioningConnectorConfig();
    }

    /**
     * @return
     */
    public ProvisioningConnectorConfig[] getProvisioningConnectorConfigs() {

        return this.identityProvider.getProvisioningConnectorConfigs();
    }

    /**
     * @return
     */
    public boolean isPrimary() {

        return this.identityProvider.isPrimary();
    }

    /**
     * @return
     */
    public String getAlias() {

        return this.identityProvider.getAlias();
    }

    /**
     * @return
     */
    public String getCertificate() {

        return this.identityProvider.getCertificate();
    }

    /**
     * @return
     */
    public ClaimConfig getClaimConfig() {

        return this.identityProvider.getClaimConfig();
    }

    /**
     * @return
     */
    public PermissionsAndRoleConfig getPermissionAndRoleConfig() {

        return this.identityProvider.getPermissionAndRoleConfig();
    }

    /**
     * @return
     */
    public String getHomeRealmId() {

        return this.identityProvider.getHomeRealmId();
    }

    /**
     * @return
     */
    public JustInTimeProvisioningConfig getJustInTimeProvisioningConfig() {

        return this.identityProvider.getJustInTimeProvisioningConfig();
    }

    /**
     * This represents a federation hub identity provider.
     *
     * @return
     */
    public boolean isFederationHub() {

        return this.identityProvider.isFederationHub();
    }

    /**
     * This represents a provisioning role of identity provider.
     *
     * @return
     */
    public String getProvisioningRole() {

        return this.identityProvider.getProvisioningRole();
    }

    /**
     * This represents whether the idp enable.
     *
     * @return
     */
    public boolean isEnable() {

        return this.identityProvider.isEnable();
    }

    /**
     * This represents a display name of identity provider.
     *
     * @return
     */
    public String getDisplayName() {

        return this.identityProvider.getDisplayName();
    }

    /**
     * Get IDP properties
     *
     * @return
     */
    public IdentityProviderProperty[] getIdpProperties() {

        return this.identityProvider.getIdpProperties();
    }

    public String getId() {

        return this.identityProvider.getId();
    }

}
