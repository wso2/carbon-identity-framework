package org.wso2.carbon.idp.mgt.object;

/**
 * Operation Node.
 */
public class OperationNode extends Node {

    private String operation;

    public OperationNode(String operation) {
        this.operation = operation;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }
}
