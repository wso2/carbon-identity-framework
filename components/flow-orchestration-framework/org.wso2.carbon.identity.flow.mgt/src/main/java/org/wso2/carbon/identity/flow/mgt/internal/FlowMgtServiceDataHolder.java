/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.flow.mgt.internal;

import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.compatibility.settings.core.CompatibilitySettingsManager;
import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.organization.resource.hierarchy.traverse.service.OrgResourceResolverService;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * A singleton class to hold the data of the flow management service.
 */
public class FlowMgtServiceDataHolder {

    private OrganizationManager organizationManager;
    private OrgResourceResolverService orgResourceResolverService;
    private ConfigurationManager configurationManager;
    private CompatibilitySettingsManager compatibilitySettingsManager;
    private ClaimMetadataManagementService claimMetadataManagementService;
    private RealmService realmService;

    private static final FlowMgtServiceDataHolder INSTANCE = new FlowMgtServiceDataHolder();

    private FlowMgtServiceDataHolder() {

    }

    public static FlowMgtServiceDataHolder getInstance() {

        return INSTANCE;
    }

    /**
     * Claim metadata, used to check that a condition names a real claim and uses an operator that
     * claim actually allows.
     *
     * @return The claim metadata service, or null when it is not yet available.
     */
    public ClaimMetadataManagementService getClaimMetadataManagementService() {

        return claimMetadataManagementService;
    }

    public void setClaimMetadataManagementService(ClaimMetadataManagementService claimMetadataManagementService) {

        this.claimMetadataManagementService = claimMetadataManagementService;
    }

    /**
     * The realm service, used to list the user stores a condition on the user's domain may name.
     *
     * @return The realm service, or null when it is not yet available.
     */
    public RealmService getRealmService() {

        return realmService;
    }

    public void setRealmService(RealmService realmService) {

        this.realmService = realmService;
    }

    public OrganizationManager getOrganizationManager() {

        return organizationManager;
    }

    public void setOrganizationManager(OrganizationManager organizationManager) {

        this.organizationManager = organizationManager;
    }

    public OrgResourceResolverService getOrgResourceResolverService() {

        return orgResourceResolverService;
    }

    public void setOrgResourceResolverService(OrgResourceResolverService orgResourceResolverService) {

        this.orgResourceResolverService = orgResourceResolverService;
    }

    public ConfigurationManager getConfigurationManager() {

        return configurationManager;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager) {

        this.configurationManager = configurationManager;
    }

    public CompatibilitySettingsManager getCompatibilitySettingsManager() {

        return compatibilitySettingsManager;
    }

    public void setCompatibilitySettingsManager(CompatibilitySettingsManager compatibilitySettingsManager) {

        this.compatibilitySettingsManager = compatibilitySettingsManager;
    }
}
