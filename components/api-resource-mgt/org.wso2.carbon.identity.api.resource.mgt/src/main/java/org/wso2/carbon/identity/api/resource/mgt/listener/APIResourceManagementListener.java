/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.api.resource.mgt.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceManager;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceManagerImpl;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceMgtException;
import org.wso2.carbon.identity.api.resource.mgt.internal.APIResourceManagementServiceComponentHolder;
import org.wso2.carbon.identity.api.resource.mgt.util.APIResourceManagementConfigBuilder;
import org.wso2.carbon.identity.application.common.model.APIResource;
import org.wso2.carbon.identity.core.AbstractIdentityTenantMgtListener;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.Map;

/**
 * APIResourceManagementListener class.
 */
public class APIResourceManagementListener extends AbstractIdentityTenantMgtListener {

    private final APIResourceManager apiResourceManager = APIResourceManagerImpl.getInstance();

    private static final Log LOG = LogFactory.getLog(APIResourceManagementListener.class);

    @Override
    public void onTenantCreate(TenantInfoBean tenantInfo) {

        if (!isEnable()) {
            LOG.debug("API resource management related APIResourceManagementListener is not enabled.");
            return;
        }

        int tenantId = tenantInfo.getTenantId();
        if (LOG.isDebugEnabled()) {
            LOG.debug("API resource management related APIResourceManagementListener fired for tenant " +
                    "creation for Tenant ID: " + tenantId);
        }

        try {
            Tenant tenant = APIResourceManagementServiceComponentHolder.getInstance()
                    .getRealmService().getTenantManager().getTenant(tenantId);
            // TODO: Remove this line once org creation on tenant creation gets merged.
            if (tenant.getAssociatedOrganizationUUID() != null) {
                String organizationId = APIResourceManagementServiceComponentHolder.getInstance()
                        .getOrganizationManager().resolveOrganizationId(tenantInfo.getTenantDomain());
                // If the tenant is not a primary organization, skip registering the system API resources.
                if (organizationId != null) {
                    if (!APIResourceManagementServiceComponentHolder.getInstance().getOrganizationManager()
                            .isPrimaryOrganization(organizationId)) {
                        return;
                    }
                }
            }
            addSystemAPIs(tenantInfo.getTenantDomain());
        } catch (OrganizationManagementException e) {
            LOG.error("Error while registering system API resources in tenant: " + tenantInfo.getTenantDomain());
        } catch (UserStoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * TODO: Make this private.
     * Fetch the configuration from the XML file and register the system API in the given tenant.
     *
     * @param tenantDomain tenant domain.
     */
    public void addSystemAPIs(String tenantDomain) {

        Map<String, APIResource> configs = APIResourceManagementConfigBuilder.getInstance()
                .getAPIResourceMgtConfigurations();
        for (APIResource apiResource : configs.values()) {
            if (apiResource != null) {
                try {
                    apiResourceManager.addAPIResource(apiResource, tenantDomain);
                } catch (APIResourceMgtException e) {
                    LOG.error("Error while registering system API resources in the tenant: " + tenantDomain);
                }
            }
        }
    }
}
