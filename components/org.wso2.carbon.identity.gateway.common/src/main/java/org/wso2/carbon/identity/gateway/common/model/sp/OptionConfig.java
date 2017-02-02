package org.wso2.carbon.identity.gateway.common.model.sp;

class OptionConfig extends GenericHandlerConfig {
    private AuthenticationHandlerConfig authenticationHandlerConfig;

    public AuthenticationHandlerConfig getAuthenticationHandlerConfig() {
        return authenticationHandlerConfig;
    }

    public void setAuthenticationHandlerConfig(AuthenticationHandlerConfig authenticationHandlerConfig) {
        this.authenticationHandlerConfig = authenticationHandlerConfig;
    }
}