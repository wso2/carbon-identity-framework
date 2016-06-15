package org.wso2.carbon.identity.application.authentication.framework.model;

public class SessionData {

    String user;
    String userStoreDomain;
    String tenantDomain;
    String sessionId;
    long createdTimestamp;
    long updatedTimestamp;
    long terminationTimestamp;
    boolean isRememberMe;
    String remoteIP;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUserStoreDomain() {
        return userStoreDomain;
    }

    public void setUserStoreDomain(String userStoreDomain) {
        this.userStoreDomain = userStoreDomain;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(long createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public long getUpdatedTimestamp() {
        return updatedTimestamp;
    }

    public void setUpdatedTimestamp(long updatedTimestamp) {
        this.updatedTimestamp = updatedTimestamp;
    }

    public long getTerminationTimestamp() {
        return terminationTimestamp;
    }

    public void setTerminationTimestamp(long terminationTimestamp) {
        this.terminationTimestamp = terminationTimestamp;
    }

    public boolean isRememberMe() {
        return isRememberMe;
    }

    public void setIsRememberMe(boolean isRememberMe) {
        this.isRememberMe = isRememberMe;
    }

    public String getRemoteIP() {
        return remoteIP;
    }

    public void setRemoteIP(String remoteIP) {
        this.remoteIP = remoteIP;
    }
}
