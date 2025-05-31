/*
 * Copyright (c) 2019-2025, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.configuration.mgt.core.internal;

import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.organization.resource.hierarchy.traverse.service.OrgResourceResolverService;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * A class to keep the data of the configuration manager component.
 */
public class ConfigurationManagerComponentDataHolder {

    private static ConfigurationManagerComponentDataHolder instance = new ConfigurationManagerComponentDataHolder();
    private static boolean useCreatedTime = false;

    private boolean configurationManagementEnabled;
    private RealmService realmService;
    private OrganizationManager organizationManager;
    private OrgResourceResolverService orgResourceResolverService;

    public static ConfigurationManagerComponentDataHolder getInstance() {

        return instance;
    }

    public static boolean getUseCreatedTime() {

        return ConfigurationManagerComponentDataHolder.useCreatedTime;
    }

    public static void setUseCreatedTime(boolean useCreatedTime) {

        ConfigurationManagerComponentDataHolder.useCreatedTime = useCreatedTime;
    }

    public boolean isConfigurationManagementEnabled() {

        return configurationManagementEnabled;
    }

    public void setConfigurationManagementEnabled(boolean configurationManagementEnabled) {

        this.configurationManagementEnabled = configurationManagementEnabled;
    }

    public RealmService getRealmService() {

        return realmService;
    }

    public void setRealmService(RealmService realmService) {

        this.realmService = realmService;
    }

    /**
     * Get the OrganizationManager.
     *
     * @return OrganizationManager instance.
     */
    public OrganizationManager getOrganizationManager() {

        return organizationManager;
    }

    /**
     * Set the OrganizationManager.
     *
     * @param organizationManager OrganizationManager instance.
     */
    public void setOrganizationManager(OrganizationManager organizationManager) {

        this.organizationManager = organizationManager;
    }

    /**
     * Get the OrgResourceResolverService.
     *
     * @return OrgResourceResolverService instance.
     */
    public OrgResourceResolverService getOrgResourceResolverService() {

        return orgResourceResolverService;
    }

    /**
     * Set the OrgResourceResolverService.
     *
     * @param orgResourceResolverService OrgResourceResolverService instance.
     */
    public void setOrgResourceResolverService(OrgResourceResolverService orgResourceResolverService) {

        this.orgResourceResolverService = orgResourceResolverService;
    }
}
