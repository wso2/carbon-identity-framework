package org.wso2.carbon.identity.workflow.mgt.exception;

public class WorkflowClientException extends WorkflowException {

    private static final long serialVersionUID = -542581829909714581L;

    public WorkflowClientException(String message) {
        super(message);
    }

    public WorkflowClientException(String message, Throwable e) { super(message, e); }
}
