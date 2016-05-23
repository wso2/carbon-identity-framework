package org.wso2.carbon.identity.application.authentication.framework.model;

import java.io.Serializable;

public class User implements Serializable {
    private String userIdentifier;
    private User attributeStepUser = this ;

    public String getUserIdentifier() {
        return userIdentifier;
    }

    public void setUserIdentifier(String userIdentifier) {
        this.userIdentifier = userIdentifier;
    }

    public UserAttribute getAttribute(){
        return this.attributeStepUser.getAttribute();
    }

    public void setAttributeStepUser(
            User attributeStepUser) {
        this.attributeStepUser = attributeStepUser;
    }
}