package org.wso2.carbon.identity.workflow.mgt.bean;

public class WorkflowRequestFilterResponse {

    private WorkflowRequest[] requests;
    private int totalCount;

    public WorkflowRequestFilterResponse(WorkflowRequest[] requests, int totalCount) {

        this.requests = requests;
        this.totalCount = totalCount;
    }

    public void setRequests(WorkflowRequest[] requests) {

        this.requests = requests;
    }

    public WorkflowRequest[] getRequests() {

        return requests;
    }

    public int getTotalCount() {

        return totalCount;
    }

    public void setTotalCount(int totalCount) {

        this.totalCount = totalCount;
    }
}
