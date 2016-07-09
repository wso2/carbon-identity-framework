package org.wso2.carbon.identity.application.authentication.framework.model;

public class SessionData {

    private String user;
    private String userStoreDomain;
    private String tenantDomain;
    private String sessionId;
    private long createdTimestamp;
    private long updatedTimestamp;
    private long terminationTimestamp;
    private boolean isRememberMe;
    private String remoteIP;
    private String userAgent;

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

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

}
