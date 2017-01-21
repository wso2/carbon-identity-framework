package org.wso2.carbon.identity.gateway.model;

import java.util.List;

public class LocalUser extends User {

    private String tenantDomain;
    private String userStoreDomain;
    private String userName;

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    public String getUserStoreDomain() {
        return userStoreDomain;
    }

    public void setUserStoreDomain(String userStoreDomain) {
        this.userStoreDomain = userStoreDomain;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }


    @Override
    public List<UserClaim> getUserClaims() {
        //TODO:read from user store
        return null;
    }
}