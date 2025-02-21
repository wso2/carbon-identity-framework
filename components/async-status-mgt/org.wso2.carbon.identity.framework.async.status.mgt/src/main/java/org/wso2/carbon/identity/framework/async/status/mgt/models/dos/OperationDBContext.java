package org.wso2.carbon.identity.framework.async.status.mgt.models.dos;

import java.util.ArrayList;

public class OperationDBContext {
    private OperationContext operationContext;
    private ArrayList<String> audience;

    // Constructor
    public OperationDBContext(OperationContext operationContext, ArrayList<String> audience) {
        this.operationContext = operationContext;
        this.audience = audience;
    }

    public OperationContext getOperationContext() {
        return operationContext;
    }

    public void setOperationContext(OperationContext operationContext) {
        this.operationContext = operationContext;
    }

    public ArrayList<String> getAudience() {
        return audience;
    }

    public void setAudience(ArrayList<String> audience) {
        this.audience = audience;
    }
}
