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
package org.wso2.carbon.identity.application.mgt.internal;

import org.wso2.carbon.consent.mgt.core.ConsentManager;
import org.wso2.carbon.identity.application.mgt.AbstractInboundAuthenticatorConfig;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.organization.management.service.OrganizationUserResidentResolverService;
import org.wso2.carbon.registry.api.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.HashMap;
import java.util.Map;

/**
 * Static class to hold services discovered via OSGI on this component, solely for the use within this component.
 */
public class ApplicationManagementServiceComponentHolder {

    private static ApplicationManagementServiceComponentHolder instance = new
            ApplicationManagementServiceComponentHolder();
    private static Map<String, AbstractInboundAuthenticatorConfig> inboundAuthenticatorConfigs =
            new HashMap<String, AbstractInboundAuthenticatorConfig>();

    private String authnTemplatesJson;

    private RegistryService registryService;

    private RealmService realmService;

    private ConfigurationContextService configContextService;

    private boolean databaseBackedCertificateStoringSupportAvailable;

    private ConsentManager consentManager;

    private ClaimMetadataManagementService claimMetadataManagementService;

    private OrganizationUserResidentResolverService organizationUserResidentResolverService;

    private ApplicationManagementServiceComponentHolder() {

    }

    public static ApplicationManagementServiceComponentHolder getInstance() {

        return instance;
    }

    /**
     * Add inbound authenticator configuration
     *
     * @param inboundAuthenticator
     */
    public static void addInboundAuthenticatorConfig(AbstractInboundAuthenticatorConfig inboundAuthenticator) {

        inboundAuthenticatorConfigs.put(inboundAuthenticator.getName() + ":" + inboundAuthenticator.getConfigName(),
                inboundAuthenticator);
    }

    /**
     * Get inbound authenticator configuration
     *
     * @param type
     * @return
     */
    public static AbstractInboundAuthenticatorConfig getInboundAuthenticatorConfig(String type) {

        return inboundAuthenticatorConfigs.get(type);
    }

    /**
     * Get inbound authenticator configurations
     *
     * @return inbound authenticator configs
     */
    public static Map<String, AbstractInboundAuthenticatorConfig> getAllInboundAuthenticatorConfig() {

        return inboundAuthenticatorConfigs;
    }

    /**
     * Remove inbound authenticator configuration
     *
     * @param type
     */
    public static void removeInboundAuthenticatorConfig(String type) {

        inboundAuthenticatorConfigs.remove(type);
    }

    public RegistryService getRegistryService() {

        return registryService;
    }

    public void setRegistryService(RegistryService registryService) {

        this.registryService = registryService;
    }

    public RealmService getRealmService() {

        return realmService;
    }

    public void setRealmService(RealmService realmService) {

        this.realmService = realmService;
    }

    public ConfigurationContextService getConfigContextService() {

        return configContextService;
    }

    public void setConfigContextService(
            ConfigurationContextService configContextService) {

        this.configContextService = configContextService;
    }

    public void setDatabaseBackedCertificateStoringSupportAvailable(
            boolean databaseBackedCertificateStoringSupportAvailable) {

        this.databaseBackedCertificateStoringSupportAvailable = databaseBackedCertificateStoringSupportAvailable;
    }

    public boolean isDatabaseBackedCertificateStoringSupportAvailable() {

        return databaseBackedCertificateStoringSupportAvailable;
    }

    public void setAuthenticationTemplatesJson(String jsonTemplate) {

        authnTemplatesJson = jsonTemplate;
    }

    public String getAuthenticationTemplatesJson() {

        return authnTemplatesJson;
    }

    /**
     * Set ConsentManager service instance.
     *
     * @param consentManager ConsentManager service instance.
     */
    public void setConsentManager(ConsentManager consentManager) {

        this.consentManager = consentManager;
    }

    /**
     * Get ConsentManager service instance.
     *
     * @return ConsentManager service instance.
     */
    public ConsentManager getConsentManager() {

        return consentManager;
    }

    public ClaimMetadataManagementService getClaimMetadataManagementService() {

        return claimMetadataManagementService;
    }

    public void setClaimMetadataManagementService(
            ClaimMetadataManagementService claimMetadataManagementService) {

        this.claimMetadataManagementService = claimMetadataManagementService;
    }

    /**
     * Get organization user resident resolver service instance.
     *
     * @return User resident resolver service instance.
     */
    public OrganizationUserResidentResolverService getOrganizationUserResidentResolverService() {

        return organizationUserResidentResolverService;
    }

    /**
     * Set organization user resident resolver service instance.
     *
     * @param organizationUserResidentResolverService OrganizationUserResidentResolverService user resident resolver
     *                                                service instance.
     */
    public void setOrganizationUserResidentResolverService(
            OrganizationUserResidentResolverService organizationUserResidentResolverService) {

        this.organizationUserResidentResolverService = organizationUserResidentResolverService;
    }
}
