package org.wso2.carbon.identity.framework.async.status.mgt.models.dos;

import org.wso2.carbon.identity.framework.async.status.mgt.constant.ResourceType;

import java.util.ArrayList;

public class SharingOperationDO {
    private String operationType;
    private String residentResourceId;
    private ResourceType resourceType;
    private String residentOrganizationId;
        private ArrayList<SharingOperationUnitDO> unitSharingList;
    private String sharingPolicy;
    private String initiatorId;
    private String operationStatus;

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getResidentResourceId() {
        return residentResourceId;
    }

    public void setResidentResourceId(String residentResourceId) {
        this.residentResourceId = residentResourceId;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public String getSharingPolicy() {
        return sharingPolicy;
    }

    public void setSharingPolicy(String sharingPolicy) {
        this.sharingPolicy = sharingPolicy;
    }

    public String getResidentOrganizationId() {
        return residentOrganizationId;
    }

    public void setResidentOrganizationId(String residentOrganizationId) {
        this.residentOrganizationId = residentOrganizationId;
    }

    public String getInitiatorId() {
        return initiatorId;
    }

    public void setInitiatorId(String initiatorId) {
        this.initiatorId = initiatorId;
    }

    public String getOperationStatus() {
        return operationStatus;
    }

    public void setOperationStatus(String operationStatus) {
        this.operationStatus = operationStatus;
    }

    public ArrayList<SharingOperationUnitDO> getUnitSharingList() {
        return unitSharingList;
    }

    public void setUnitSharingList(ArrayList<SharingOperationUnitDO> unitSharingList) {
        this.unitSharingList = unitSharingList;
    }
}
