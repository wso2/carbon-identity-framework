package org.wso2.carbon.identity.framework.async.status.mgt.internal;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.wso2.carbon.identity.framework.async.status.mgt.AsyncStatusMgtService;
import org.wso2.carbon.identity.framework.async.status.mgt.constant.OperationType;
import org.wso2.carbon.identity.framework.async.status.mgt.constant.ResourceType;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.SharingOperationDO;

import java.util.logging.Logger;

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
        asyncStatusMgtService.test("B2B App Share");

        SharingOperationDO sharingOperationDO = new SharingOperationDO();
        sharingOperationDO.setOperationType(OperationType.SHARE.toString());
        sharingOperationDO.setResidentResourceId("resourceId");
        sharingOperationDO.setResourceType(ResourceType.B2B_APPLICATION);
        sharingOperationDO.setSharingPolicy("policy");
        sharingOperationDO.setResidentOrganizationId("orgId");
        sharingOperationDO.setInitiatorId("initiatorId");
        sharingOperationDO.setOperationStatus("status");

        LOGGER.info("SharingOperationDO created.");

        asyncStatusMgtService.processB2BAsyncOperationStatus(sharingOperationDO);
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
