package org.wso2.carbon.light.registry.mgt.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.wso2.carbon.light.registry.mgt.service.RegistryResourceMgtService;
import org.wso2.carbon.light.registry.mgt.service.RegistryResourceMgtServiceImpl;

@Component(
        name = "registry.mgt.component",
        immediate = true
)
public class RegistryMgtServiceComponent {

    private static Log log = LogFactory.getLog(RegistryMgtServiceComponent.class);
    private static BundleContext bundleContext = null;
    private RegistryMgtServiceDataHolder dataHolder = RegistryMgtServiceDataHolder.getInstance();

    @Activate
    protected void activate(ComponentContext context) {

        BundleContext bundleContext = context.getBundleContext();
        try {
            bundleContext.registerService(RegistryResourceMgtService.class.getName(),
                    new RegistryResourceMgtServiceImpl(), null);
            log.debug("AdminAdvisoryManagement bundle is activated");
        } catch (Throwable e) {
            log.error("Failed to activate AdminAdvisoryManagement bundle", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {

        log.debug("AdminAdvisoryManagement bundle is deactivated");
    }
}
