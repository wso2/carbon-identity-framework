package org.wso2.carbon.identity.framework.async.status.mgt.internal;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.wso2.carbon.identity.framework.async.status.mgt.AsyncStatusMgtService;
import org.wso2.carbon.identity.framework.async.status.mgt.dao.AsyncStatusMgtDAOImpl;

import java.util.logging.Logger;

/**
 * OSGi service component for asynchronous operation status management bundle.
 */
@Component(
        name = "org.wso2.carbon.identity.framework.internal."
                + "AsyncStatusMgtServiceComponent",
        immediate = true
)
public class AsyncStatusMgtServiceComponent {

    private static final Logger LOGGER =
            Logger.getLogger(AsyncStatusMgtServiceComponent.class.getName());

    private AsyncStatusMgtService asyncStatusMgtService;

    @Activate
    protected void activate(final ComponentContext context) {

        LOGGER.info("Async Status Mgt Component is activated");
        AsyncStatusMgtDataHolder.getInstance().setAsyncStatusMgtDAO(new AsyncStatusMgtDAOImpl());
        LOGGER.info("Async Status Mgt Component is activated");
    }

    @Deactivate
    protected void deactivate(final ComponentContext context) {

        LOGGER.info("Async Status Mgt Component is deactivated");
    }

    @Reference
    protected void setAsyncStatusMgtService(
            final AsyncStatusMgtService service) {

        this.asyncStatusMgtService = service;
    }

    protected void unsetAsyncStatusMgtService(
            final AsyncStatusMgtService service) {

        this.asyncStatusMgtService = null;
    }
}
