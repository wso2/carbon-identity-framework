package org.wso2.carbon.identity.application.common.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.identity.action.management.api.service.ActionManagementService;
import org.wso2.carbon.identity.application.common.ApplicationAuthenticatorService;

/**
 * OSGI service component for the Application Common Service Component.
 */
@Component(
        name = "application.common.service.component",
        immediate = true
)
public class ApplicationCommonServiceComponent {

    private static final Log LOG = LogFactory.getLog(ApplicationCommonServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        try {
            BundleContext bundleCtx = context.getBundleContext();
            bundleCtx.registerService(ApplicationAuthenticatorService.class.getName(),
                    ApplicationAuthenticatorService.getInstance(),
                    null);
            LOG.debug("Application Authenticator Service is activated.");
        } catch (Throwable e) {
            LOG.error("Error while initializing Application Authenticator Service component.", e);
        }
    }

    @Reference(
            name = "action.management.service",
            service = ActionManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetActionManagementService"
    )
    protected void setActionManagementService(ActionManagementService actionManagementService) {

        if (LOG.isDebugEnabled()) {
            LOG.debug(
                    "Registering a reference for ActionManagementService in the ApplicationCommonServiceComponent.");
        }
        ApplicationCommonServiceDataHolder.getInstance().setActionManagementService(actionManagementService);
    }

    protected void unsetActionManagementService(ActionManagementService actionManagementService) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Unregistering the reference for ActionManagementService in the " +
                    "ApplicationCommonServiceComponent.");
        }
        if (ApplicationCommonServiceDataHolder.getInstance().getActionManagementService()
                .equals(actionManagementService)) {
            ApplicationCommonServiceDataHolder.getInstance().setActionManagementService(null);
        }
    }
}
