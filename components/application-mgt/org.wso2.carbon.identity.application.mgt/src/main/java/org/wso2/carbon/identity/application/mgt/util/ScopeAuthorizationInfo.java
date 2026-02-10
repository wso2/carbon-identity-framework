package org.wso2.carbon.identity.application.mgt.util;

/**
 * A class to hold the scope authorization information.
 */
public class ScopeAuthorizationInfo {

    private String scopeId;
    private String apiId;
    private String scopeName;

    public ScopeAuthorizationInfo(String scopeId, String apiId, String scopeName) {
        this.scopeId = scopeId;
        this.apiId = apiId;
        this.scopeName = scopeName;
    }

    public String getScopeId() {

        return scopeId;
    }

    public String getApiId() {

        return apiId;
    }

    public String getScopeName() {

        return scopeName;
    }

    public void setScopeId(String scopeId) {

        this.scopeId = scopeId;
    }

    public void setApiId(String apiId) {

        this.apiId = apiId;
    }

    public void setScopeName(String scopeName) {

        this.scopeName = scopeName;
    }
}
