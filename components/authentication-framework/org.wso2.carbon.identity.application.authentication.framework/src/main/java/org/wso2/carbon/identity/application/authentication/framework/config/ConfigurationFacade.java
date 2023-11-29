/*
 * Copyright (c) 2013-2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.config;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.config.builder.FileBasedConfigurationBuilder;
import org.wso2.carbon.identity.application.authentication.framework.config.loader.UIBasedConfigurationLoader;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ExternalIdPConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.ServiceURLBuilder;
import org.wso2.carbon.identity.core.URLBuilderException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.util.OrganizationManagementUtil;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.DefaultUrlContexts.ACCOUNT_RECOVERY_ENDPOINT_PATH;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.DefaultUrlContexts.AUTHENTICATION_ENDPOINT;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.DefaultUrlContexts.AUTHENTICATION_ENDPOINT_DYNAMIC_PROMPT;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.DefaultUrlContexts.AUTHENTICATION_ENDPOINT_ERROR;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.DefaultUrlContexts.AUTHENTICATION_ENDPOINT_MISSING_CLAIMS_PROMPT;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.DefaultUrlContexts.AUTHENTICATION_ENDPOINT_RETRY;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.DefaultUrlContexts.AUTHENTICATION_ENDPOINT_WAIT;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.DefaultUrlContexts.IDENTIFIER_FIRST_CONFIRMATION;

/**
 * Configuration facade.
 */
public class ConfigurationFacade {

    private static final Log log = LogFactory.getLog(ConfigurationFacade.class);
    private UIBasedConfigurationLoader uiBasedConfigurationLoader;
    private static volatile ConfigurationFacade instance;

    public ConfigurationFacade() {
        // Read the default config from the files
        FileBasedConfigurationBuilder.getInstance();
        uiBasedConfigurationLoader = new UIBasedConfigurationLoader();
    }

    public static ConfigurationFacade getInstance() {

        if (instance == null) {
            synchronized (ConfigurationFacade.class) {

                if (instance == null) {
                    instance = new ConfigurationFacade();
                }
            }
        }

        return instance;
    }

    /**
     * Returns the sequence config with given parameters.
     * @param reqType
     * @param relyingParty
     * @param tenantDomain
     * @return
     * @throws FrameworkException
     * TODO: Test this.
     * @deprecated Please use  #getSequenceConfig(AuthenticationContext, Map) instead.
     */
    @Deprecated
    public SequenceConfig getSequenceConfig(String reqType, String relyingParty, String tenantDomain)
            throws FrameworkException {

        ApplicationManagementService appInfo = ApplicationManagementService.getInstance();

        // special case for OpenID Connect, these clients are stored as OAuth2 clients
        if ("oidc".equals(reqType)) {
            reqType = "oauth2";
        }

        ServiceProvider serviceProvider;

        try {
            serviceProvider = appInfo.getServiceProviderByClientId(relyingParty, reqType, tenantDomain);
        } catch (IdentityApplicationManagementException e) {
            throw new FrameworkException(e.getMessage(), e);
        }

        if (serviceProvider == null) {
            throw new FrameworkException("ServiceProvider cannot be null");
        }
        AuthenticationStep[] authenticationSteps = serviceProvider.getLocalAndOutBoundAuthenticationConfig()
                .getAuthenticationSteps();

        return uiBasedConfigurationLoader.getSequence(serviceProvider, tenantDomain, authenticationSteps);

    }

    public ExternalIdPConfig getIdPConfigByName(String idpName, String tenantDomain)
            throws IdentityProviderManagementException {

        ExternalIdPConfig externalIdPConfig = null;
        IdentityProvider idpDO = null;

        if (log.isDebugEnabled()) {
            log.debug("Trying to find the IdP for name: " + idpName);
        }

        try {
            IdentityProviderManager idpManager = IdentityProviderManager.getInstance();
            idpDO = idpManager.getEnabledIdPByName(idpName, tenantDomain);

            if (idpDO != null) {

                if (log.isDebugEnabled()) {
                    log.debug("A registered IdP was found");
                }

                externalIdPConfig = new ExternalIdPConfig(idpDO);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("A registered IdP was not found the given name");
                }
            }
        } catch (IdentityProviderManagementException e) {
            throw new IdentityProviderManagementException("Exception while getting IdP by name", e);
        }

        return externalIdPConfig;
    }

    public ExternalIdPConfig getIdPConfigByRealm(String realm, String tenantDomain)
            throws IdentityProviderManagementException {

        ExternalIdPConfig externalIdPConfig = null;
        IdentityProvider idpDO = null;

        if (log.isDebugEnabled()) {
            log.debug("Trying to find the IdP for realm: " + realm);
        }

        try {
            IdentityProviderManager idpManager = IdentityProviderManager.getInstance();
            idpDO = idpManager.getEnabledIdPByRealmId(realm, tenantDomain);

            if (idpDO != null) {

                if (log.isDebugEnabled()) {
                    log.debug("A registered IdP was found");
                }

                externalIdPConfig = new ExternalIdPConfig(idpDO);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("A registered IdP was not found the given realm");
                }
            }
        } catch (IdentityProviderManagementException e) {
            throw new IdentityProviderManagementException("Exception while getting IdP by realm", e);
        }

        return externalIdPConfig;
    }

    public String getAuthenticationEndpointURL() {

        return buildUrl(AUTHENTICATION_ENDPOINT,
                FileBasedConfigurationBuilder.getInstance()::getAuthenticationEndpointURL);
    }

    public String getAuthenticationEndpointAbsoluteURL() {

        return buildAbsoluteUrl(AUTHENTICATION_ENDPOINT,
                FileBasedConfigurationBuilder.getInstance()::getAuthenticationEndpointURL);
    }

    public String getAuthenticationEndpointRetryURL() {

        return buildUrl(AUTHENTICATION_ENDPOINT_RETRY,
                FileBasedConfigurationBuilder.getInstance()::getAuthenticationEndpointRetryURL);
    }

    public String getAuthenticationEndpointErrorURL() {

        return buildUrl(AUTHENTICATION_ENDPOINT_ERROR,
                FileBasedConfigurationBuilder.getInstance()::getAuthenticationEndpointErrorURL);
    }

    public String getAuthenticationEndpointWaitURL() {

        return buildUrl(AUTHENTICATION_ENDPOINT_WAIT,
                FileBasedConfigurationBuilder.getInstance()::getAuthenticationEndpointWaitURL);
    }

    public String getAccountRecoveryEndpointAbsolutePath() {

        return buildAbsoluteUrl(ACCOUNT_RECOVERY_ENDPOINT_PATH, this::readAccountRecoveryEndpointPath);
    }

    public String getAccountRecoveryEndpointPath() {

        return buildUrl(ACCOUNT_RECOVERY_ENDPOINT_PATH, this::readAccountRecoveryEndpointPath);
    }

    public String getIdentifierFirstConfirmationURL() {

        return buildUrl(IDENTIFIER_FIRST_CONFIRMATION,
                FileBasedConfigurationBuilder.getInstance()::getIdentifierFirstConfirmationURL);
    }

    public String getAuthenticationEndpointPromptURL() {

        return buildUrl(AUTHENTICATION_ENDPOINT_DYNAMIC_PROMPT,
                FileBasedConfigurationBuilder.getInstance()::getAuthenticationEndpointPromptURL);
    }

    /**
     * Get the missing claims request URL of authentication flow
     *
     * @return claims request URL
     */
    public String getAuthenticationEndpointMissingClaimsURL() {

        return buildUrl(AUTHENTICATION_ENDPOINT_MISSING_CLAIMS_PROMPT,
                FileBasedConfigurationBuilder.getInstance()::getAuthenticationEndpointMissingClaimsURL);
    }

    /**
     * Get the tenant list receiving urls
     *
     * @return Tenant list receiving urls
     */
    public List<String> getTenantDataEndpointURLs() {
        return FileBasedConfigurationBuilder.getInstance().getTenantDataEndpointURLs();
    }

    /**
     * Get the value for tenant list dropdown enable or disable
     *
     * @return Tenant list dropdown enabled or disabled value
     */
    public boolean getTenantDropdownEnabled() {
        return FileBasedConfigurationBuilder.getInstance().isTenantDomainDropdownEnabled();
    }

    /**
     * Get the authenticator config for the given authenticator name.
     *
     * @param name Name of the authenticator.
     * @return AuthenticatorConfig.
     */
    public AuthenticatorConfig getAuthenticatorConfig(String name) {

        return FileBasedConfigurationBuilder.getInstance().getAuthenticatorBean(name);
    }

    public boolean isDumbMode() {
        return FileBasedConfigurationBuilder.getInstance().isDumbMode();
    }

    public Map<String, Object> getExtensions() {
        return FileBasedConfigurationBuilder.getInstance().getExtensions();
    }

    public Map<String, String> getAuthenticatorNameMappings() {
        return FileBasedConfigurationBuilder.getInstance().getAuthenticatorNameMappings();
    }

    public Map<String, Integer> getCacheTimeouts() {
        return FileBasedConfigurationBuilder.getInstance().getCacheTimeouts();
    }

    public int getMaxLoginAttemptCount() {
        return FileBasedConfigurationBuilder.getInstance().getMaxLoginAttemptCount();
    }

    private String readAccountRecoveryEndpointPath() {

        return preprocessEndpointPath(IdentityUtil.getProperty("RecoveryEndpoint.Path"));
    }

    private String preprocessEndpointPath(String endpointPath) {

        if (StringUtils.isNotBlank(endpointPath)) {
            if (!endpointPath.startsWith("/")) {
                endpointPath = "/" + endpointPath;
            }
            if (endpointPath.endsWith("/")) {
                endpointPath = endpointPath.substring(0, endpointPath.length() - 1);
            }
            return endpointPath;
        } else {
            return null;
        }
    }

    private String buildUrl(String defaultContext, Supplier<String> getValueFromFileBasedConfig) {

        if (IdentityTenantUtil.isTenantQualifiedUrlsEnabled()) {
            try {
                String organizationId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getOrganizationId();
                return ServiceURLBuilder.create().addPath(defaultContext).setOrganization(organizationId).build()
                        .getAbsolutePublicURL();
            } catch (URLBuilderException e) {
                throw new IdentityRuntimeException(
                        "Error while building tenant qualified url for context: " + defaultContext, e);
            }
        } else {
            String urlFromFileBasedConfig = getValueFromFileBasedConfig.get();
            if (StringUtils.isNotBlank(urlFromFileBasedConfig)) {
                // If the file based URL is set, then we have to return the file based URL.
                return urlFromFileBasedConfig;
            } else {
                return defaultContext;
            }
        }
    }

    private String buildAbsoluteUrl(String defaultContext, Supplier<String> getValueFromFileBasedConfig) {

        String urlFromFileBasedConfig = getValueFromFileBasedConfig.get();
        String path = defaultContext;
        if (StringUtils.isNotBlank(urlFromFileBasedConfig)) {
            // If a value is configured in the file, then we have to use it.
            if (urlFromFileBasedConfig.startsWith("http://") || urlFromFileBasedConfig.startsWith("https://")) {
                // If the full URL is configured, we can use it as it is.
                return urlFromFileBasedConfig;
            } else {
                path = urlFromFileBasedConfig;
            }
        }
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String organizationId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getOrganizationId();
        ServiceURLBuilder serviceURLBuilder = ServiceURLBuilder.create().addPath(path);
        try {
            // If the organization ID is not set in the context for a tenant which is associated to an organization.
            if (StringUtils.isEmpty(organizationId) && OrganizationManagementUtil.isOrganization(tenantDomain)) {
                organizationId = FrameworkServiceDataHolder.getInstance().getOrganizationManager()
                        .resolveOrganizationId(tenantDomain);
            }
            return serviceURLBuilder.setOrganization(organizationId).build().getAbsolutePublicURL();
        } catch (URLBuilderException e) {
            throw new IdentityRuntimeException(
                    "Error while building tenant qualified url for context: " + defaultContext, e);
        } catch (OrganizationManagementException e) {
            throw new IdentityRuntimeException(
                    "Error while resolving organization by tenant domain: " + tenantDomain, e);
        }
    }
}
