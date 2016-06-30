package org.wso2.carbon.identity.application.authentication.endpoint.util.bean;

import java.io.Serializable;


public class User implements Serializable {

    private static final long serialVersionUID = 928301275168169633L;

    protected String tenantDomain;
    protected String userStoreDomain;
    protected String userName;

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
}
