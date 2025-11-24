/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.debug.framework.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.debug.framework.core.DebugRequestCoordinator;

/**
 * OSGi service component for Debug Framework.
 * This component provides the framework infrastructure for debug operations.
 * Protocol-specific implementations (e.g., OAuth2ContextResolver, OAuth2Executor) are provided
 * by protocol modules and automatically discovered via OSGi service lookups.
 */
@Component(
        name = "identity.debug.service.component",
        immediate = true
)
public class DebugServiceComponent {

    private static final Log LOG = LogFactory.getLog(DebugServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Debug Framework OSGi component activating");
            }

            // Register DebugRequestCoordinator as an OSGi service.
            // This service handles protocol-agnostic routing of debug requests from both
            // the API layer (initial debug requests) and /commonauth (OAuth callbacks).
            DebugRequestCoordinator requestCoordinator = new DebugRequestCoordinator();
            context.getBundleContext().registerService(
                DebugRequestCoordinator.class.getName(),
                requestCoordinator,
                null
            );

            if (LOG.isDebugEnabled()) {
                LOG.debug("DebugRequestCoordinator registered as OSGi service");
            }

        } catch (Throwable e) {
            LOG.error("Debug Framework activation failed", e);
            throw new RuntimeException("Debug Framework activation failed: " + e.getMessage(), e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        LOG.info("Debug Framework OSGi component deactivated");
    }

    /**
     * Sets the ClaimMetadataManagementService.
     *
     * @param service the ClaimMetadataManagementService instance
     */
    @Reference(
            name = "claimMetadataManagementService",
            service = ClaimMetadataManagementService.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetClaimMetadataManagementService"
    )
    protected void setClaimMetadataManagementService(ClaimMetadataManagementService service) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("ClaimMetadataManagementService set in DebugServiceComponent");
        }
        DebugFrameworkServiceDataHolder.getInstance().setClaimMetadataManagementService(service);
    }

    /**
     * Unsets the ClaimMetadataManagementService.
     *
     * @param claimMetadataManagementService the ClaimMetadataManagementService instance
     */
    protected void unsetClaimMetadataManagementService(ClaimMetadataManagementService claimMetadataManagementService) {
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("ClaimMetadataManagementService unset in DebugServiceComponent");
        }
        DebugFrameworkServiceDataHolder.getInstance().setClaimMetadataManagementService(null);
    }
}
