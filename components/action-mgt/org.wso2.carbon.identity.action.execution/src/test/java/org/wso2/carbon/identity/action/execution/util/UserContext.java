package org.wso2.carbon.identity.action.execution.util;

import org.wso2.carbon.identity.action.execution.model.ActionType;
import org.wso2.carbon.identity.action.execution.model.Context;

public class UserContext implements Context {

    public static final ActionType ACTION_TYPE = ActionType.AUTHENTICATION;

    private String id;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
