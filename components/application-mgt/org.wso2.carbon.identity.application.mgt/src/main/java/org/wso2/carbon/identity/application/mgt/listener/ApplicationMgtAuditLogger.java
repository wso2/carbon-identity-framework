/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.identity.application.mgt.listener;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.json.JSONObject;
import org.json.XML;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.ClaimConfig;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.RoleMapping;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.CarbonUtils;

import java.util.Arrays;

import static org.wso2.carbon.identity.application.mgt.ApplicationMgtUtil.getUsernameWithUserTenantDomain;

/**
 * Audit log implementation for Application (Service Provider) changes.
 */
public class ApplicationMgtAuditLogger extends AbstractApplicationMgtListener {

    private static final Log audit = CarbonConstants.AUDIT_LOG;
    private static final String AUDIT_MESSAGE = "Initiator : %s | Action : %s | Target : %s | Data : { Changed-State " +
            ": { %s } } | Result : %s ";
    private static final String SUCCESS = "Success";

    @Override
    public boolean isEnable() {

        if (super.isEnable()) {
            // Legacy audit logs should be enabled to log these audit logs.
            return !CarbonUtils.isLegacyAuditLogsDisabled();
        }
        return false;
    }

    @Override
    public int getDefaultOrderId() {
        return 200;
    }

    @Override
    public boolean doPostCreateApplication(ServiceProvider serviceProvider, String tenantDomain, String userName)
            throws IdentityApplicationManagementException {

        String appId = getAppId(serviceProvider);
        String initiator = getInitiatorForLog(userName, tenantDomain);
        String data = buildData(serviceProvider);
        audit.info(String.format(AUDIT_MESSAGE, initiator, "Add-Application", appId, data, SUCCESS));
        return true;
    }

    @Override
    public boolean doPostUpdateApplication(ServiceProvider serviceProvider, String tenantDomain, String userName)
            throws IdentityApplicationManagementException {

        String appId = getAppId(serviceProvider);
        String initiator = getInitiatorForLog(userName, tenantDomain);
        String data = buildData(serviceProvider);

        audit.info(String.format(AUDIT_MESSAGE, initiator, "Update-Application", appId, data, SUCCESS));
        return true;
    }

    @Override
    public boolean doPostDeleteApplication(ServiceProvider serviceProvider, String tenantDomain, String userName)
            throws IdentityApplicationManagementException {

        String applicationName = getApplicationName(serviceProvider);
        String appId = getAppId(serviceProvider);
        String initiator = getInitiatorForLog(userName, tenantDomain);
        audit.info(String.format(AUDIT_MESSAGE, initiator, "Delete-Application", appId, applicationName, SUCCESS));
        return true;
    }

    private String getAppId(ServiceProvider serviceProvider) {

        if (serviceProvider != null) {
            return serviceProvider.getApplicationResourceId();
        }
        return StringUtils.EMPTY;
    }

    private String getApplicationName(ServiceProvider serviceProvider) {

        if (serviceProvider != null) {
            return serviceProvider.getApplicationName();
        }
        return "Undefined";
    }

    private String buildInitiatorUsername(String tenantDomain, String userName)
            throws IdentityApplicationManagementException {

        // Append tenant domain to username build the full qualified username of initiator.
        if (StringUtils.isEmpty(userName)) {
            return getUsernameWithUserTenantDomain(tenantDomain);
        }
        return UserCoreUtil.addTenantDomainToEntry(userName, tenantDomain);
    }

    private String buildData(ServiceProvider serviceProvider) {

        if (serviceProvider == null) {
            return StringUtils.EMPTY;
        }
        StringBuilder data = new StringBuilder();
        data.append("Name:").append(serviceProvider.getApplicationName()).append(", ");
        data.append("Description:").append(serviceProvider.getDescription()).append(", ");
        data.append("Application Version:").append(serviceProvider.getApplicationVersion()).append(", ");
        data.append("Resource ID:").append(serviceProvider.getApplicationResourceId()).append(", ");
        data.append("Access URL:").append(serviceProvider.getAccessUrl()).append(", ");
        data.append("Is Discoverable:").append(serviceProvider.isDiscoverable()).append(", ");
        data.append("Is SaaS:").append(serviceProvider.isSaasApp()).append(", ");

        if (serviceProvider.getInboundAuthenticationConfig() != null && ArrayUtils.isNotEmpty(serviceProvider
                .getInboundAuthenticationConfig().getInboundAuthenticationRequestConfigs())) {
            InboundAuthenticationRequestConfig[] requestConfigs = serviceProvider.getInboundAuthenticationConfig()
                    .getInboundAuthenticationRequestConfigs();
            data.append("Inbound Authentication Configs:").append("[");
            for (InboundAuthenticationRequestConfig requestConfig : requestConfigs) {
                data.append("{");
                data.append("Auth Key:").append(LoggerUtils.getMaskedContent(requestConfig.getInboundAuthKey())).
                        append(", ");
                data.append("Auth Type:").append(requestConfig.getInboundAuthType()).append(", ");
                data.append("Config Type:").append(requestConfig.getInboundConfigType()).append(", ");
                data.append("Inbound configuration:").
                        append(maskInboundConfigurations(requestConfig.getInboundConfiguration()));
                Property[] properties = requestConfig.getProperties();
                if (ArrayUtils.isNotEmpty(properties)) {
                    data.append("Properties:").append("[");
                    String joiner = "";
                    for (Property property : properties) {
                        data.append(joiner);
                        joiner = ", ";
                        data.append("{");
                        data.append(property.getName()).append(":");
                        if (property.getName().equals("oauthConsumerSecret")) {
                            data.append(LoggerUtils.getMaskedContent(property.getValue()));
                        } else {
                            data.append(property.getValue());
                        }
                        data.append("}");
                    }
                    data.append("]");
                }
                data.append("}");
            }
            data.append("]");
        }

        if (serviceProvider.getLocalAndOutBoundAuthenticationConfig() != null) {
            data.append(", Local and Outbound Configuration:{");
            data.append("Auth Type:").append(serviceProvider.getLocalAndOutBoundAuthenticationConfig()
                    .getAuthenticationType());
            AuthenticationStep[] authSteps = serviceProvider.getLocalAndOutBoundAuthenticationConfig()
                    .getAuthenticationSteps();
            if (ArrayUtils.isNotEmpty(authSteps)) {
                data.append(", Authentication Steps:[");
                for (AuthenticationStep authStep : authSteps) {
                    data.append("{");
                    data.append("Step Order:").append(authStep.getStepOrder()).append(", ");
                    LocalAuthenticatorConfig[] localConfigs = authStep.getLocalAuthenticatorConfigs();
                    if (ArrayUtils.isNotEmpty(localConfigs)) {
                        data.append(", Local Authenticators:[");
                        String joiner = "";
                        for (LocalAuthenticatorConfig localConfig: localConfigs) {
                            data.append(joiner);
                            joiner = ", ";
                            data.append(localConfig.getName());
                        }
                        data.append("]");
                    }
                    IdentityProvider[] fedIDPs = authStep.getFederatedIdentityProviders();
                    if (ArrayUtils.isNotEmpty(fedIDPs)) {
                        data.append("Federated Authenticators:[");
                        String joiner = "";
                        for (IdentityProvider provider: fedIDPs) {
                            data.append(joiner);
                            joiner = ", ";
                            data.append("{IDP:").append(provider.getIdentityProviderName()).append(",");
                            if (provider.getDefaultAuthenticatorConfig() != null) {
                                data.append("Authenticator:").append(provider.getDefaultAuthenticatorConfig()
                                        .getName()).append("}");
                            }
                        }
                        data.append("]");
                    }
                    data.append("}");
                }
                data.append("]");
            }
            data.append("}");
        }
        if (serviceProvider.getClaimConfig() != null) {
            data.append(", Claim Configuration:{");
            ClaimConfig claimConfig = serviceProvider.getClaimConfig();
            data.append("User Claim URI:").append(claimConfig.getUserClaimURI()).append(", ");
            data.append("Role Claim URI:").append(claimConfig.getRoleClaimURI());
            ClaimMapping[] claimMappings = claimConfig.getClaimMappings();
            if (ArrayUtils.isNotEmpty(claimMappings)) {
                data.append(", Claim Mappings: [");
                String joiner = "";
                for (ClaimMapping mapping: claimMappings) {
                    data.append("{");
                    data.append(joiner);
                    joiner = ", ";
                    if (mapping.getLocalClaim() != null && StringUtils.isNotBlank(mapping.getLocalClaim()
                            .getClaimUri())) {
                        data.append("Local Claim:").append(mapping.getLocalClaim().getClaimUri());
                    }
                    if (mapping.getRemoteClaim() != null && StringUtils.isNotBlank(mapping.getLocalClaim()
                            .getClaimUri())) {
                        data.append(", ").append("Remote Claim:").append(mapping.getRemoteClaim().getClaimUri());
                    }
                    data.append("}");
                }
                data.append("]");
            }
            data.append("}");
        }
        if (serviceProvider.getPermissionAndRoleConfig() != null) {
            RoleMapping[] roleMappings = serviceProvider.getPermissionAndRoleConfig().getRoleMappings();
            if (ArrayUtils.isNotEmpty(roleMappings)) {
                data.append(", Role Mappings:[");
                for (RoleMapping mapping: roleMappings) {
                    data.append("{");
                    if (mapping.getLocalRole() != null && StringUtils.isNotBlank(mapping.getLocalRole()
                            .getLocalRoleName())) {
                        data.append("Local Role:").append(mapping.getLocalRole().getLocalRoleName());
                    }
                    if (StringUtils.isNotBlank(mapping.getRemoteRole())) {
                        data.append(", Remote Role:").append(mapping.getRemoteRole());
                    }
                    data.append("}");
                }
                data.append("]");
            }
        }

        if (serviceProvider.getInboundProvisioningConfig() != null) {
            data.append(", Inbound Provisioning Configuration:{");
            data.append("Provisioning Userstore:").append(serviceProvider.getInboundProvisioningConfig()
                    .getProvisioningUserStore()).append(", ");
            data.append("Is Dumb Mode:").append(serviceProvider.getInboundProvisioningConfig().isDumbMode());
            data.append("}");
        }

        if (serviceProvider.getOutboundProvisioningConfig() != null) {
            data.append(", Outbound Provisioning Configuration:{");
            String[] provisionRoles = serviceProvider.getOutboundProvisioningConfig().getProvisionByRoleList();
            if (ArrayUtils.isNotEmpty(provisionRoles)) {
                data.append("Provisioning Roles:[");
                String joiner = "";
                for (String role: provisionRoles) {
                    data.append(joiner);
                    joiner = ", ";
                    data.append(role);
                }
                data.append("]");
            }
            IdentityProvider[] provisionIdPs = serviceProvider.getOutboundProvisioningConfig()
                    .getProvisioningIdentityProviders();
            if (ArrayUtils.isNotEmpty(provisionIdPs)) {
                data.append("Provisioning IDPs:[");
                String joiner = "";
                for (IdentityProvider provider: provisionIdPs) {
                    data.append(joiner);
                    joiner = ", ";
                    data.append(provider.getIdentityProviderName());
                }
                data.append("]");
            }
            data.append("}");
        }

        if (serviceProvider.getTrustedAppMetadata() != null) {
            data.append(", Trusted App Metadata:{");
            data.append("isConsentGranted:").append(serviceProvider.getTrustedAppMetadata()
                    .getIsConsentGranted()).append(", ");
            data.append("isFidoTrusted:").append(serviceProvider.getTrustedAppMetadata()
                    .getIsFidoTrusted()).append(", ");
            data.append("androidPackageName:").append(serviceProvider.getTrustedAppMetadata()
                    .getAndroidPackageName()).append(", ");
            data.append("androidThumbprints:").append(Arrays.toString(serviceProvider.getTrustedAppMetadata()
                    .getAndroidThumbprints())).append(", ");
            data.append("appleAppId:").append(serviceProvider.getTrustedAppMetadata()
                    .getAppleAppId()).append(", ");
            data.append("}");
        }

        if (ArrayUtils.isNotEmpty(serviceProvider.getSpProperties())) {
            data.append(", Service Provider Properties:[");
            ServiceProviderProperty[] spProperties = serviceProvider.getSpProperties();
            String joiner = "";
            for (ServiceProviderProperty spProperty : spProperties) {
                data.append(joiner);
                joiner = ", ";
                data.append("{").append(spProperty.getName()).append(":").append(spProperty.getValue()).append("}");
            }
            data.append("]");
        }
        return data.toString();
    }


    /**
     * Mask inbound configurations with secrets,keys.
     *
     * @param inboundConfigurations Inbound configurations.
     *
     * @return masked inbound configurations.
     */
    private String maskInboundConfigurations(String inboundConfigurations) {

        if (!LoggerUtils.isLogMaskingEnable) {
            return inboundConfigurations;
        }
        if (StringUtils.isNotBlank(inboundConfigurations)) {
            if (inboundConfigurations.contains("<oauthConsumerSecret>")) {
                JSONObject oauthAppDO = XML.toJSONObject(inboundConfigurations);
                JSONObject configs = oauthAppDO.getJSONObject("oAuthAppDO");
                configs.put("oauthConsumerSecret",
                        LoggerUtils.getMaskedContent(configs.getString("oauthConsumerSecret")));
                oauthAppDO.put("oAuthAppDO", configs);
                inboundConfigurations = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                        XML.toString(oauthAppDO);
            }
        }
        return inboundConfigurations;
    }

    /**
     * Get the initiator for audit logs.
     *
     * @param username      Username of the initiator.
     * @param tenantDomain  Tenant domain of the initiator.
     *
     * @return initiator for the log.
     */
    private String getInitiatorForLog(String username, String tenantDomain) throws
            IdentityApplicationManagementException {

        if (!LoggerUtils.isLogMaskingEnable) {
            // Append tenant domain to username.
            return buildInitiatorUsername(tenantDomain, username);
        }
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(tenantDomain)) {
            String initiator = IdentityUtil.getInitiatorId(username, tenantDomain);
            if (StringUtils.isNotBlank(initiator)) {
                return initiator;
            }
        }
        return LoggerUtils.getMaskedContent(username);
    }
}
