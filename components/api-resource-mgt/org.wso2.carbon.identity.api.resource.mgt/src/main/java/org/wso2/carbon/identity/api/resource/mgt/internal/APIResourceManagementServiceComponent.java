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

package org.wso2.carbon.identity.api.resource.mgt.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceManager;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceManagerImpl;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceMgtException;
import org.wso2.carbon.identity.api.resource.mgt.constant.APIResourceManagementConstants;
import org.wso2.carbon.identity.api.resource.mgt.listener.APIResourceManagementListener;
import org.wso2.carbon.identity.api.resource.mgt.model.APIResourceSearchResult;
import org.wso2.carbon.identity.api.resource.mgt.util.APIResourceManagementConfigBuilder;
import org.wso2.carbon.identity.application.common.model.APIResource;
import org.wso2.carbon.identity.core.util.IdentityCoreInitializedEvent;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.Map;

/**
 * Service component for the API resource management.
 */
@Component(
        name = "api.resource.mgt.service.component",
        immediate = true
)
public class APIResourceManagementServiceComponent {

    private static final Log LOG = LogFactory.getLog(APIResourceManagementServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        try {
            BundleContext bundleCtx = context.getBundleContext();
            bundleCtx.registerService(APIResourceManager.class, APIResourceManagerImpl.getInstance(), null);
            bundleCtx.registerService(TenantMgtListener.class, new APIResourceManagementListener(),
                    null);
            registerSystemAPIsInSuperTenant();
            LOG.debug("API resource management bundle is activated");
        } catch (Throwable e) {
            LOG.error("Error while initializing API resource management component.", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        try {
            BundleContext bundleCtx = context.getBundleContext();
            bundleCtx.ungetService(bundleCtx.getServiceReference(APIResourceManager.class));
            LOG.debug("API resource management bundle is deactivated");
        } catch (Throwable e) {
            LOG.error("Error while deactivating API resource management component.", e);
        }
    }

    @Reference(
            name = "identityCoreInitializedEventService",
            service = IdentityCoreInitializedEvent.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetIdentityCoreInitializedEventService"
    )
    protected void setIdentityCoreInitializedEventService(IdentityCoreInitializedEvent identityCoreInitializedEvent) {
        /* reference IdentityCoreInitializedEvent service to guarantee that this component will wait until identity core
         is started */
    }

    protected void unsetIdentityCoreInitializedEventService(IdentityCoreInitializedEvent identityCoreInitializedEvent) {
        /* reference IdentityCoreInitializedEvent service to guarantee that this component will wait until identity core
         is started */
    }

    @Reference(
            name = "organization.service",
            service = OrganizationManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetOrganizationManager"
    )
    protected void setOrganizationManager(OrganizationManager organizationManager) {
        /* reference Organization Management service to guarantee that this component will wait until organization
        management service is started */
    }

    protected void unsetOrganizationManager(OrganizationManager organizationManager) {
        /* reference Organization Management service to guarantee that this component will wait until organization
        management service is started */
    }

    private void registerSystemAPIsInSuperTenant() {

        String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        try {
            APIResourceSearchResult systemAPIResources = APIResourceManagerImpl.getInstance()
                    .getAPIResources(null, null, 1, APIResourceManagementConstants.SYSTEM_API_FILTER,
                            APIResourceManagementConstants.ASC, tenantDomain);
            if (systemAPIResources.getTotalCount() == 0) {
                LOG.info("Registering System APIs in tenant domain: " + tenantDomain);
                Map<String, APIResource> configs = APIResourceManagementConfigBuilder.getInstance()
                        .getAPIResourceMgtConfigurations();
                for (APIResource apiResource : configs.values()) {
                    if (apiResource != null) {
                        APIResourceManagerImpl.getInstance().addAPIResource(apiResource, tenantDomain);
                    }
                }
                LOG.info("System APIs successfully registered in tenant domain: " + tenantDomain);
            }
        } catch (APIResourceMgtException e) {
            LOG.error("Error while registering system API resources in the tenant: " + tenantDomain);
        }
    }
}
