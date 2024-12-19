/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com).
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

package org.wso2.carbon.identity.core.internal;

import org.wso2.carbon.identity.organization.management.service.OrganizationUserResidentResolverService;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * Identity core service data holder.
 */
public class IdentityCoreServiceDataHolder {

    private static IdentityCoreServiceDataHolder instance = new IdentityCoreServiceDataHolder();
    private RealmService realmService = null;
    private OrganizationUserResidentResolverService organizationUserResidentResolverService = null;

    private boolean isTenantQualifiedUrlsEnabled;

    private boolean isTenantedSessionsEnabled;

    private IdentityCoreServiceDataHolder() {

    }

    public static IdentityCoreServiceDataHolder getInstance() {

        return instance;
    }

    /**
     * Get realm service.
     *
     * @return realm service.
     */
    public RealmService getRealmService() {

        return realmService;
    }

    /**
     * Set realm service.
     *
     * @param realmService Realm service.
     */
    public void setRealmService(RealmService realmService) {

        this.realmService = realmService;
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

    /**
     * Get tenant qualified URLs enabled status.
     *
     * @return Tenant qualified URLs enabled status.
     */
    public boolean isTenantQualifiedUrlsEnabled() {

        return isTenantQualifiedUrlsEnabled;
    }

    /**
     * Get tenanted sessions enabled status.
     *
     * @return Tenanted sessions enabled status.
     */
    public boolean isTenantedSessionsEnabled() {

        return isTenantedSessionsEnabled;
    }

    public void setTenantQualifiedUrlsEnabled(boolean tenantQualifiedUrlsEnabled) {

        isTenantQualifiedUrlsEnabled = tenantQualifiedUrlsEnabled;
    }

    public void setTenantedSessionsEnabled(boolean tenantedSessionsEnabled) {

        isTenantedSessionsEnabled = tenantedSessionsEnabled;
    }
}
