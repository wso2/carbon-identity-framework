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
            
            LOG.info("Debug Framework OSGi component activated successfully. " +
                    "Waiting for protocol-specific implementations (DebugContextResolver, DebugExecutor)");
            
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
