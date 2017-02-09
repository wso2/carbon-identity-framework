package org.wso2.carbon.identity.gateway.common.model.sp;

public class OptionConfig  {

    AuthenticatorConfig authenticatorConfig ;

    public AuthenticatorConfig getAuthenticatorConfig() {
        return authenticatorConfig;
    }

    public void setAuthenticatorConfig(AuthenticatorConfig authenticatorConfig) {
        this.authenticatorConfig = authenticatorConfig;
    }
}