package org.wso2.carbon.identity.gateway.model;

import java.io.Serializable;
import java.util.List;

public abstract class User implements Serializable {

    private String userIdentifier;
    private User attributeStepUser = this;

    public String getUserIdentifier() {
        return userIdentifier;
    }

    public void setUserIdentifier(String userIdentifier) {
        this.userIdentifier = userIdentifier;
    }

    public abstract List<UserClaim> getUserClaims();

}