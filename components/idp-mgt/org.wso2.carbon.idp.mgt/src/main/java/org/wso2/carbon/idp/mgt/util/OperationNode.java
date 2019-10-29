package org.wso2.carbon.idp.mgt.util;

/**
 * Operation Node.
 */
public class OperationNode extends Node {

    private String operation;

    OperationNode(String operation) {
        this.operation = operation;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }
}
