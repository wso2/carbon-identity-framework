package org.wso2.carbon.identity.framework.async.status.mgt.models.dos;

import org.wso2.carbon.identity.framework.async.status.mgt.constant.ResourceType;

public class SharingOperationDO {
    private String operationType;
    private String residentResourceId;
    private ResourceType resourceType;
    private String sharingPolicy;
    private String residentOrganizationId;
    private String initiatorId;
    private String operationStatus;

}
