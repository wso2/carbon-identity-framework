/*
 * Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.idp.mgt.listener;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.application.common.model.*;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.wso2.carbon.utils.CarbonUtils.isLegacyAuditLogsDisabled;

public class IDPMgtAuditLogger extends AbstractIdentityProviderMgtListener {


    Log audit = CarbonConstants.AUDIT_LOG;
    private static String AUDIT_MESSAGE = "Initiator : %s | Action : %s | Target : %s | Data : { Changed-State : { %s } }" +
            " | Result : %s ";
    private final String SUCCESS = "Success";

    // Properties with the following key values will not be printed in audit logs.
    private static final Set<String> UNLOGGABLE_PARAMS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("ClientSecret", "SPNPassword", "APISecret", "scim2-password", "sf-password",
                    "sf-client-secret", "scim-password", "scim-default-pwd", "scim2-default-pwd")));

    @Override
    public boolean isEnable() {

        if (super.isEnable()) {
            return !isLegacyAuditLogsDisabled();
        }
        return false;
    }

    @Override
    public int getDefaultOrderId() {

        return 220;
    }

    @Override
    public boolean doPostAddIdP(IdentityProvider identityProvider, String tenantDomain) throws
            IdentityProviderManagementException {

        String resourceId = "Undefined";
        if (identityProvider != null && StringUtils.isNotBlank(identityProvider.getResourceId())) {
            resourceId = identityProvider.getResourceId();
        }
        String data = buildData(identityProvider);
        audit.info(String.format(AUDIT_MESSAGE, getUser(), "Add-IDP", resourceId, data, SUCCESS));

        return true;
    }

    @Override
    public boolean doPostUpdateIdP(String oldIdPName, IdentityProvider identityProvider, String tenantDomain) throws
            IdentityProviderManagementException {

        String resourceId = "Undefined";

        if (identityProvider != null && StringUtils.isNotEmpty(identityProvider.getResourceId())) {
            resourceId = identityProvider.getResourceId();
        }
        String data = buildData(identityProvider);
        audit.info(String.format(AUDIT_MESSAGE, getUser(), "Update-IDP", resourceId, data, SUCCESS));
        return true;
    }

    /**
     * Post delete of IDP.
     *
     * @param idPName Name of the IDP
     * @param tenantDomain Tenant Domain
     * @return
     * @throws IdentityProviderManagementException
     */
    @Deprecated
    @Override
    public boolean doPostDeleteIdP(String idPName, String tenantDomain) throws IdentityProviderManagementException {

        return true;
    }

    /**
     * Post delete of IDP by resource ID.
     *
     * @param resourceId Resource ID of the IDP.
     * @param identityProvider Identity Provider.
     * @param tenantDomain Tenant Domain.
     * @return
     * @throws IdentityProviderManagementException
     */
    public boolean doPostDeleteIdPByResourceId(String resourceId, IdentityProvider identityProvider, String
            tenantDomain) throws IdentityProviderManagementException {

        if (StringUtils.isBlank(resourceId)) {
            resourceId = "Undefined";
        }
        audit.info(String.format(AUDIT_MESSAGE, getUser(), "Delete-IDP", resourceId, null, SUCCESS));
        return true;
    }

    /**
     * Additional actions after deleting IdPs of a given tenant id.
     *
     * @param tenantDomain Tenant domain to delete IdPs
     * @return
     * @throws IdentityProviderManagementException
     */
    @Override
    public boolean doPostDeleteIdPs(String tenantDomain) throws IdentityProviderManagementException {

        audit.info(String.format(AUDIT_MESSAGE, getUser(), "Delete-All-IDPs", tenantDomain, null, SUCCESS));
        return true;
    }

    private String getUser() {

        String user = CarbonContext.getThreadLocalCarbonContext().getUsername();
        if (StringUtils.isNotBlank(user)) {
            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            if (LoggerUtils.isLogMaskingEnable && StringUtils.isNotBlank(tenantDomain)) {
                String userId = IdentityUtil.getInitiatorId(user, tenantDomain);
                return StringUtils.isNotBlank(userId) ? userId : user + "@" + tenantDomain;
            }
            return user + "@" + tenantDomain;
        } else {
            return LoggerUtils.isLogMaskingEnable ?
                    LoggerUtils.getMaskedContent(CarbonConstants.REGISTRY_SYSTEM_USERNAME) :
                    CarbonConstants.REGISTRY_SYSTEM_USERNAME;
        }
    }

    private String buildData(IdentityProvider identityProvider) {

        if (identityProvider == null) {
            return StringUtils.EMPTY;
        }

        StringBuilder data = new StringBuilder();
        data.append("Name:").append(identityProvider.getIdentityProviderName()).append(", ");
        data.append("Display Name:").append(identityProvider.getDisplayName()).append(", ");
        data.append("Resource ID:").append(identityProvider.getResourceId()).append(", ");
        data.append("Description:").append(identityProvider.getIdentityProviderDescription()).append(", ");
        data.append("Alias:").append(identityProvider.getAlias()).append(", ");
        data.append("Home Realm ID:").append(identityProvider.getHomeRealmId()).append(", ");
        data.append("Image URL:").append(identityProvider.getImageUrl()).append(", ");
        data.append("Provisioning Role:").append(identityProvider.getProvisioningRole());
        if (identityProvider.getClaimConfig() != null) {
            ClaimConfig claimConfig = identityProvider.getClaimConfig();
            data.append(", Claim Configuration:{");
            data.append("User Claim URI:").append(claimConfig.getUserClaimURI()).append(", ");
            data.append("User Role URI:").append(claimConfig.getRoleClaimURI());
            if (ArrayUtils.isNotEmpty(claimConfig.getIdpClaims())) {
                data.append(", IDP claims:[");
                String joiner = "";
                for (Claim claim : claimConfig.getIdpClaims()) {
                    if (claim != null) {
                        data.append(joiner);
                        joiner = ", ";
                        data.append(claim.getClaimUri());
                    }
                }
                data.append("]");
            }
            if (ArrayUtils.isNotEmpty(claimConfig.getClaimMappings())) {
                data.append("Claim Mappings:[");
                String joiner = "";
                for (ClaimMapping mapping : claimConfig.getClaimMappings()) {
                    data.append(joiner);
                    joiner = ", ";
                    data.append("{");
                    if (mapping.getLocalClaim() != null) {
                        data.append("Local Claim:").append(mapping.getLocalClaim().getClaimUri());
                    }
                    if (mapping.getRemoteClaim() != null) {
                        data.append(", Remote Claim:").append(mapping.getRemoteClaim().getClaimUri());
                    }
                    if (StringUtils.isNotBlank(mapping.getDefaultValue())) {
                        data.append(", Default Value:").append(mapping.getDefaultValue());
                    }
                    data.append("}");
                }
                data.append("]");
            }
            data.append("}");

        }
        if (identityProvider.getPermissionAndRoleConfig() != null) {
            PermissionsAndRoleConfig roleConfig = identityProvider.getPermissionAndRoleConfig();
            data.append(", Role Configuration:{");
            if (ArrayUtils.isNotEmpty(roleConfig.getIdpRoles())) {
                data.append("IDP roles:[");
                String joiner = "";
                for (String role : roleConfig.getIdpRoles()) {
                    data.append(joiner);
                    joiner = ", ";
                    data.append(role);
                }
                data.append("]");
            }
            if (ArrayUtils.isNotEmpty(roleConfig.getRoleMappings())) {
                data.append("Role Mappings:[");
                String joiner = "";
                for (RoleMapping mapping : roleConfig.getRoleMappings()) {
                    data.append(joiner);
                    joiner = ", ";
                    data.append("{");
                    if (mapping.getLocalRole() != null) {
                        data.append("Local Role:").append(mapping.getLocalRole().getLocalRoleName());
                    }
                    if (StringUtils.isNotBlank(mapping.getRemoteRole())) {
                        data.append(", Remote Role:").append(mapping.getRemoteRole());
                    }
                    data.append("}");
                }
                data.append("]");
            }
            data.append("}");

        }
        if (ArrayUtils.isNotEmpty(identityProvider.getFederatedAuthenticatorConfigs())) {
            FederatedAuthenticatorConfig[] authConfigs = identityProvider.getFederatedAuthenticatorConfigs();
            data.append(", Federated Authenticator Configs:[");
            String joiner = "";
            for (FederatedAuthenticatorConfig authConfig : authConfigs) {
                data.append(joiner);
                joiner = ", ";
                data.append("{Name:").append(authConfig.getName());
                if (ArrayUtils.isNotEmpty(authConfig.getProperties())) {
                    data.append(", Properties:[");
                    joiner = "";
                    for (Property property : authConfig.getProperties()) {
                        if (!UNLOGGABLE_PARAMS.contains(property.getName())) {
                            data.append(joiner);
                            joiner = ", ";
                            data.append("{").append(property.getName()).append(":").append(
                                    LoggerUtils.getMaskedContent(property.getValue())).append("}");
                        }
                    }
                    data.append("]");
                }
                data.append("}");
            }
            data.append("]");
        }
        if (identityProvider.getDefaultAuthenticatorConfig() != null) {
            FederatedAuthenticatorConfig defaultAuthConfig = identityProvider.getDefaultAuthenticatorConfig();
            data.append(", Default Authenticator:").append(defaultAuthConfig.getName());
        }
        if (ArrayUtils.isNotEmpty(identityProvider.getProvisioningConnectorConfigs())) {
            ProvisioningConnectorConfig[] provisionConfigs = identityProvider.getProvisioningConnectorConfigs();
            data.append(", Provisioning Connector Configs:[");
            String joiner = "";
            for (ProvisioningConnectorConfig provConfig : provisionConfigs) {
                data.append(joiner);
                joiner = ", ";
                data.append("{Name:").append(provConfig.getName());
                if (ArrayUtils.isNotEmpty(provConfig.getProvisioningProperties())) {
                    data.append(", Properties:[");
                    joiner = "";
                    for (Property property : provConfig.getProvisioningProperties()) {
                        if (!UNLOGGABLE_PARAMS.contains(property.getName())) {
                            data.append(joiner);
                            joiner = ", ";
                            data.append("{").append(property.getName()).append(":").append(
                                    LoggerUtils.getMaskedContent(property.getValue())).append("}");
                        }
                    }
                    data.append("]");
                }
                data.append("}");
            }
            data.append("]");
        }
        if (identityProvider.getDefaultProvisioningConnectorConfig() != null) {
            ProvisioningConnectorConfig defaultProvConfig = identityProvider.getDefaultProvisioningConnectorConfig();
            data.append(", Default Provisioning Connector:").append(defaultProvConfig.getName());
        }
        if (identityProvider.getJustInTimeProvisioningConfig() != null) {
            JustInTimeProvisioningConfig justInTimeConfig = identityProvider.getJustInTimeProvisioningConfig();
            data.append(", Just In Time Provisioning Configuration:{");
            data.append("Is Provisioning Enabled:").append(justInTimeConfig.isProvisioningEnabled()).append(", ");
            data.append("Is Modify Username Allowed:").append(justInTimeConfig.isModifyUserNameAllowed()).append(", ");
            data.append("Is Prompt Consent:").append(justInTimeConfig.isPromptConsent()).append(", ");
            data.append("Is Password Provisioning Enabled:").append(justInTimeConfig.isPasswordProvisioningEnabled())
                    .append(", ");
            data.append("Userstore Claim URI:").append(justInTimeConfig.getUserStoreClaimUri()).append(", ");
            data.append("Provisioning Userstore:").append(justInTimeConfig.getProvisioningUserStore()).append(", ");
            data.append("Is Dumb Mode:").append(justInTimeConfig.isDumbMode());
            data.append("}");
        }
        if (ArrayUtils.isNotEmpty(identityProvider.getIdpProperties())) {
            IdentityProviderProperty[] idpProperties = identityProvider.getIdpProperties();
            data.append(", IDP Properties:[");
            String joiner = "";
            for (IdentityProviderProperty property : idpProperties) {
                if (!UNLOGGABLE_PARAMS.contains(property.getName())) {
                    data.append(joiner);
                    joiner = ", ";
                    data.append("{").append(property.getName()).append(":").append(
                            LoggerUtils.getMaskedContent(property.getValue())).append("}");
                }
            }
            data.append("]");
        }
        return data.toString();
    }
}
