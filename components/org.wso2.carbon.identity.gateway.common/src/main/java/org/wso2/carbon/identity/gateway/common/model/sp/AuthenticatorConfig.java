package org.wso2.carbon.identity.gateway.common.model.sp;

public class AuthenticatorConfig {
    private String type;
    private String authenticatorName;

    public String getAuthenticatorName() {
        return authenticatorName;
    }

    public void setAuthenticatorName(String authenticatorName) {
        this.authenticatorName = authenticatorName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}