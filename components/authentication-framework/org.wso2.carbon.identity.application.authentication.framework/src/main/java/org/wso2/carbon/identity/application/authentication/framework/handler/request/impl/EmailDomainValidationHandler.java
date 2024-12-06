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

package org.wso2.carbon.identity.application.authentication.framework.handler.request.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.FederatedApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.PostAuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.AbstractPostAuthnHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.PostAuthnHandlerFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.organization.config.service.exception.OrganizationConfigClientException;
import org.wso2.carbon.identity.organization.config.service.exception.OrganizationConfigException;
import org.wso2.carbon.identity.organization.config.service.model.ConfigProperty;
import org.wso2.carbon.identity.organization.config.service.model.DiscoveryConfig;
import org.wso2.carbon.identity.organization.discovery.service.model.OrgDiscoveryAttribute;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkErrorConstants.ErrorMessages.ERROR_WHILE_RETRIEVING_ORG_DISCOVERY_ATTRIBUTES;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkErrorConstants.ErrorMessages.INVALID_EMAIL_DOMAIN;

/**
 * This class is responsible for validating the email domain of the user during the authentication flow.
 */
public class EmailDomainValidationHandler extends AbstractPostAuthnHandler {

    private static final Log log = LogFactory.getLog(EmailDomainValidationHandler.class);
    private static final String EMAIL_DOMAIN_ENABLE = "emailDomain.enable";
    public static final String EMAIL_DOMAIN = "emailDomain";

    private EmailDomainValidationHandler() {

    }

    private static class Holder {

        private static final EmailDomainValidationHandler INSTANCE = new EmailDomainValidationHandler();
    }

    public static EmailDomainValidationHandler getInstance() {

        return Holder.INSTANCE;
    }

    @Override
    public boolean isEnabled() {

        if (!super.isEnabled()) {
            return false;
        }

        try {
            OrganizationManager organizationManager = FrameworkServiceDataHolder.getInstance().getOrganizationManager();
            String organizationId = organizationManager.resolveOrganizationId(CarbonContext
                    .getThreadLocalCarbonContext().getTenantDomain());

            if (organizationManager.isPrimaryOrganization(organizationId)) {
                // Skip email domain validation since email domains cannot be mapped to primary organizations.
                return false;
            } else {
                organizationId = organizationManager.getPrimaryOrganizationId(organizationId);
            }

            return isEmailDomainDiscoveryEnabled(organizationId);
        } catch (OrganizationConfigClientException e) {
            if (log.isDebugEnabled()) {
                log.debug("No organization discovery configurations found for organization: " + CarbonContext
                        .getThreadLocalCarbonContext().getTenantDomain());
            }
            return false;
        } catch (OrganizationManagementException | OrganizationConfigException e) {
            log.error("Error while retrieving organization discovery configuration.", e);
            return false;
        }
    }

    @Override
    public int getPriority() {

        int priority = super.getPriority();
        if (priority == -1) {
            priority = 15;
        }
        return priority;
    }

    @Override
    public PostAuthnHandlerFlowStatus handle(HttpServletRequest request, HttpServletResponse response,
                                             AuthenticationContext context) throws PostAuthenticationFailedException {

        SequenceConfig sequenceConfig = context.getSequenceConfig();
        for (Map.Entry<Integer, StepConfig> entry : sequenceConfig.getStepMap().entrySet()) {
            StepConfig stepConfig = entry.getValue();
            AuthenticatorConfig authenticatorConfig = stepConfig.getAuthenticatedAutenticator();
            if (authenticatorConfig == null) {
                continue;
            }

            ApplicationAuthenticator authenticator = authenticatorConfig.getApplicationAuthenticator();
            if (authenticator instanceof FederatedApplicationAuthenticator) {
                Map<String, String> localClaimValues;
                if (stepConfig.isSubjectAttributeStep()) {
                    localClaimValues =
                            (Map<String, String>) context.getProperty(FrameworkConstants.UNFILTERED_LOCAL_CLAIM_VALUES);
                } else {
                    // Need to validate even if this is not the subject attribute step since
                    // jit provisioning will happen in both scenarios.
                    localClaimValues =
                            FrameworkUtils.getLocalClaimValuesOfIDPInNonAttributeSelectionStep(context, stepConfig,
                                    context.getExternalIdP());
                }

                String emailDomain = extractEmailDomain(localClaimValues.get(FrameworkConstants.EMAIL_ADDRESS_CLAIM));

                if (emailDomain == null) {
                    if (log.isDebugEnabled()) {
                        log.debug("email address not found or is not in the correct format." +
                                " Skipping email domain validation.");
                    }
                    return PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED;
                }

                if (!isValidEmailDomain(context, emailDomain)) {
                    throw new PostAuthenticationFailedException(INVALID_EMAIL_DOMAIN.getCode(),
                            String.format(INVALID_EMAIL_DOMAIN.getMessage(), context.getTenantDomain()));
                }
            }
        }
        return PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED;
    }

    private boolean isEmailDomainDiscoveryEnabled(String primaryOrganizationId)
            throws OrganizationConfigException, OrganizationManagementException {

        String tenantDomain = FrameworkServiceDataHolder.getInstance().getOrganizationManager()
                .resolveTenantDomain(primaryOrganizationId);

        DiscoveryConfig discoveryConfiguration =
                FrameworkServiceDataHolder.getInstance().getOrganizationConfigManager()
                        .getDiscoveryConfigurationByTenantId(IdentityTenantUtil.getTenantId(tenantDomain));
        List<ConfigProperty> configProperties = discoveryConfiguration.getConfigProperties();
        for (ConfigProperty configProperty : configProperties) {
            if (configProperty.getKey().equals(EMAIL_DOMAIN_ENABLE)) {
                return Boolean.parseBoolean(configProperty.getValue());
            }
        }
        return false;
    }

    private boolean isValidEmailDomain(AuthenticationContext context, String emaildomain)
            throws PostAuthenticationFailedException {

        try {
            List<OrgDiscoveryAttribute> organizationDiscoveryAttributes =
                    FrameworkServiceDataHolder.getInstance().getOrganizationDiscoveryManager()
                            .getOrganizationDiscoveryAttributes(context.getTenantDomain(), false);

            if (organizationDiscoveryAttributes.isEmpty()) {
                log.debug("No email domains are mapped to the organization. Skipping email domain validation.");
                return true;
            }

            for (OrgDiscoveryAttribute orgDiscoveryAttribute : organizationDiscoveryAttributes) {
                if (!EMAIL_DOMAIN.equals(orgDiscoveryAttribute.getType())) {
                    continue;
                }

                List<String> mappedEmailDomains = orgDiscoveryAttribute.getValues();
                if (mappedEmailDomains != null && !mappedEmailDomains.contains(emaildomain)) {
                    return false;
                }
            }

        } catch (OrganizationManagementException e) {
            log.error(
                    "Error while retrieving organization discovery attributes for tenant: " + context.getTenantDomain(),
                    e);
            throw new PostAuthenticationFailedException(ERROR_WHILE_RETRIEVING_ORG_DISCOVERY_ATTRIBUTES.getCode(),
                    String.format(ERROR_WHILE_RETRIEVING_ORG_DISCOVERY_ATTRIBUTES.getMessage(),
                            context.getTenantDomain()), e);
        }
        return true;
    }

    private String extractEmailDomain(String email) {

        if (StringUtils.isBlank(email)) {
            return null;
        }

        String[] emailSplit = email.split("@");
        if (emailSplit.length == 2) {
            return emailSplit[1];
        }
        return null;
    }
}
