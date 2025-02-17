/*
 * Copyright (c) 2023-2024, WSO2 LLC. (http://www.wso2.com).
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
import org.wso2.carbon.identity.api.resource.mgt.AuthorizationDetailsTypeManager;
import org.wso2.carbon.identity.api.resource.mgt.AuthorizationDetailsTypeManagerImpl;
import org.wso2.carbon.identity.api.resource.mgt.constant.APIResourceManagementConstants;
import org.wso2.carbon.identity.api.resource.mgt.util.APIResourceManagementUtil;
import org.wso2.carbon.identity.core.util.IdentityCoreInitializedEvent;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.event.services.IdentityEventService;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;

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
            bundleCtx.registerService(AuthorizationDetailsTypeManager.class,
                    new AuthorizationDetailsTypeManagerImpl(), null);
            APIResourceManagementServiceComponentHolder.getInstance()
                    .setRichAuthorizationRequestsEnabled(this.isRichAuthorizationRequestsEnabled());
            // Register system APIs in the super tenant.
            APIResourceManagementUtil.addSystemAPIs();
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
            bundleCtx.ungetService(bundleCtx.getServiceReference(AuthorizationDetailsTypeManager.class));
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

        APIResourceManagementServiceComponentHolder.getInstance().setOrganizationManager(organizationManager);
        LOG.debug("OrganizationManager set in API Resource Management bundle.");
    }

    protected void unsetOrganizationManager(OrganizationManager organizationManager) {

        APIResourceManagementServiceComponentHolder.getInstance().setOrganizationManager(null);
        LOG.debug("OrganizationManager unset in API Resource Management bundle.");
    }

    @Reference(
            name = "identity.event.service",
            service = IdentityEventService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetIdentityEventService"
    )
    protected void setIdentityEventService(IdentityEventService identityEventService) {

        APIResourceManagementServiceComponentHolder.getInstance().setIdentityEventService(identityEventService);
        LOG.debug("IdentityEventService set in API Resource Management bundle.");
    }

    protected void unsetIdentityEventService(IdentityEventService identityEventService) {

        APIResourceManagementServiceComponentHolder.getInstance().setIdentityEventService(null);
        LOG.debug("IdentityEventService unset in API Resource Management bundle.");
    }

    /**
     * Checks if RAR is enabled by verifying the OAuth.EnableRichAuthorizationRequests config at identity.xml.
     *
     * @return {@code true} if RAR is enabled from the config; {@code false} otherwise.
     */
    private boolean isRichAuthorizationRequestsEnabled() {

        return Boolean.parseBoolean(IdentityUtil.getProperty(APIResourceManagementConstants
                .APIResourceConfigBuilderConstants.RICH_AUTHORIZATION_REQUESTS_ENABLED));
    }
}
