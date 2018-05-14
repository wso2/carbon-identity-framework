/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.config.model;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.application.common.model.Claim;
import org.wso2.carbon.identity.application.common.model.ClaimConfig;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.JustInTimeProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.PermissionsAndRoleConfig;
import org.wso2.carbon.identity.application.common.model.RoleMapping;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ExternalIdPConfig implements Serializable {

    private static final long serialVersionUID = 8406982734621371639L;

    private IdentityProvider identityProvider;
    private ClaimConfig claimConfiguration;
    private PermissionsAndRoleConfig roleConfiguration;
    private JustInTimeProvisioningConfig justInTimeProConfig;

    private Map<String, String> parameterMap = new HashMap<>();
    private Map<String, String> roleMappings = new HashMap<>();

    public ExternalIdPConfig() {
    }

    /**
     * @param identityProvider
     */
    public ExternalIdPConfig(IdentityProvider identityProvider) {
        this.identityProvider = identityProvider;

        claimConfiguration = identityProvider.getClaimConfig();
        roleConfiguration = identityProvider.getPermissionAndRoleConfig();
        justInTimeProConfig = identityProvider.getJustInTimeProvisioningConfig();

        if(roleConfiguration != null) {
            RoleMapping[] mappings = roleConfiguration.getRoleMappings();

            if (mappings != null && mappings.length > 0) {
                for (RoleMapping roleMapping : mappings) {
                    if (StringUtils.isNotEmpty(roleMapping.getLocalRole().getUserStoreId())) {
                        this.roleMappings.put(roleMapping.getRemoteRole(), UserCoreUtil
                                .addDomainToName(roleMapping.getLocalRole().getLocalRoleName(),
                                        roleMapping.getLocalRole().getUserStoreId()));
                    } else {
                        this.roleMappings.put(roleMapping.getRemoteRole(), roleMapping.getLocalRole().getLocalRoleName());
                    }
                }
            }
        }
    }

    /**
     * @return
     */
    public String getIdPName() {
        return identityProvider.getIdentityProviderName();
    }

    /**
     * @return
     */
    public String getPublicCert() {
        return identityProvider.getCertificate();
    }

    /**
     * @return
     */
    public boolean isPrimary() {
        return identityProvider.isPrimary();
    }

    /**
     * @return
     */
    public String getName() {
        return identityProvider.getIdentityProviderName();
    }

    /**
     * @return
     */
    public Map<String, String> getParameterMap() {
        return parameterMap;
    }

    /**
     * @param parameterMap
     */
    public void setParameterMap(Map<String, String> parameterMap) {
        this.parameterMap = parameterMap;
    }

    /**
     * @return
     */
    public String getDomain() {
        return identityProvider.getHomeRealmId();
    }

    /*
     * 
     */
    public boolean isFederationHubIdP() {
        return identityProvider.isFederationHub();
    }

    /**
     * @return
     */
    public ClaimMapping[] getClaimMappings() {
        if (claimConfiguration != null) {
            return claimConfiguration.getClaimMappings();
        }
        return new ClaimMapping[0];
    }

    /**
     * @return
     */
    public String[] getRoles() {
        if (roleConfiguration != null) {
            return roleConfiguration.getIdpRoles();
        }
        return new String[0];
    }

    /**
     * @return
     */
    public Map<String, String> getRoleMappings() {
        return roleMappings;
    }

    /**
     * @return
     */
    public Claim[] getClaims() {
        if (claimConfiguration != null) {
            return claimConfiguration.getIdpClaims();
        }
        return new Claim[0];
    }

    /**
     * @return
     */
    public String getProvisioningUserStoreId() {
        if (justInTimeProConfig != null) {
            return justInTimeProConfig.getProvisioningUserStore();
        }
        return null;
    }

    /**
     * @return
     */
    public String getProvisioningUserStoreClaimURI() {
        if (justInTimeProConfig != null) {
            return justInTimeProConfig.getUserStoreClaimUri();
        }
        return null;
    }

    /**
     * @return
     */
    public String getTokenEndpointAlias() {
        return identityProvider.getAlias();
    }

    /**
     * @return
     */
    public boolean isProvisioningEnabled() {
        if (justInTimeProConfig != null) {
            return justInTimeProConfig.isProvisioningEnabled();
        }
        return false;
    }

    /**
     * To check whether password provisioning is enabled.
     *
     * @return true if the password provisioning is enabled otherwise false.
     */
    public boolean isPasswordProvisioningEnabled() {
        if (justInTimeProConfig != null) {
            return justInTimeProConfig.isPasswordProvisioningEnabled();
        }
        return false;
    }

    /**
     * To check whether modify user name is allowed.
     *
     * @return true if modification of user name is allowed.
     */
    public boolean isModifyUserNameAllowed() {
        if (justInTimeProConfig != null) {
            return justInTimeProConfig.isModifyUserNameAllowed();
        }
        return false;
    }

    /**
     * @return
     */
    public String getRoleClaimUri() {
        if (identityProvider.getClaimConfig() != null) {
            return identityProvider.getClaimConfig().getRoleClaimURI();
        }
        return null;
    }

    /**
     * @return
     */
    public String getUserIdClaimUri() {
        if (identityProvider.getClaimConfig() != null) {
            return identityProvider.getClaimConfig().getUserClaimURI();
        }
        return null;
    }

    /**
     * @return
     */
    public boolean useDefaultLocalIdpDialect() {
        if (claimConfiguration != null) {
            return claimConfiguration.isLocalClaimDialect();
        }

        return false;
    }

    /**
     * @return
     */
    public IdentityProvider getIdentityProvider() {
        return identityProvider;
    }

}
