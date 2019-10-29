package org.wso2.carbon.idp.mgt.util;

/**
 * This is the node representation of the AST which is used in the filtering operations.
 */
public class ExpressionNode extends Node {

    private String operation;
    private String value;
    private String attributeValue;

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    public void setAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
    }
}
