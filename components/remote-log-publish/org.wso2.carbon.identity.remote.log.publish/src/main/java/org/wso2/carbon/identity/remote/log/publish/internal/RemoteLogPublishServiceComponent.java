package org.wso2.carbon.identity.remote.log.publish.internal;

import java.util.Hashtable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.wso2.carbon.identity.remote.log.publish.RemoteLogPublishConfigService;
import org.wso2.carbon.identity.remote.log.publish.RemoteLogPublishConfigServiceImpl;
import static org.wso2.carbon.identity.remote.log.publish.constants.RemoteLogPublishConstants.SERVICE_PROPERTY_KEY_SERVICE_NAME;
import static org.wso2.carbon.identity.remote.log.publish.constants.RemoteLogPublishConstants.SERVICE_PROPERTY_VAL_REMOTE_LOG_PUBLISH;

@Component(
    name = "RemoteLogPublishServiceComponent",
    immediate = true
)
public class RemoteLogPublishServiceComponent {

    private static final Log log = LogFactory.getLog(RemoteLogPublishServiceComponent.class);

    public RemoteLogPublishServiceComponent() {
    }

    @Activate
    protected void activate(ComponentContext context) {

        try {
            BundleContext bundleCtx = context.getBundleContext();

            Hashtable<String, String> remoteLogPublishServiceProperties = new Hashtable<>();
            remoteLogPublishServiceProperties.put(SERVICE_PROPERTY_KEY_SERVICE_NAME,
                    SERVICE_PROPERTY_VAL_REMOTE_LOG_PUBLISH);
            RemoteLogPublishConfigService remoteLogPublishConfigService = new RemoteLogPublishConfigServiceImpl();
            ServiceRegistration remoteLogPublishSR = bundleCtx
                    .registerService(RemoteLogPublishServiceComponent.class.getName(), remoteLogPublishConfigService,
                            remoteLogPublishServiceProperties);
            if (remoteLogPublishSR != null) {
                log.debug("Remote log publish service registered.");
            } else {
                log.error("Error registering remote log publish service.");
            }
        } catch (Throwable e) {
            log.error("Error while activating remote log publish bundle", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        if (log.isDebugEnabled()) {
            log.debug("Remote log publish bundle is de-activated");
        }
    }
}
