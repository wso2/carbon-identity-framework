package org.wso2.carbon.identity.gateway.common.model.sp;

class StepHandlerConfig extends GenericHandlerConfig {
    private MultiOptionHandlerConfig multiOptionHandler;
    private AuthenticationHandlerConfig authenticationHandlerConfig;

    public AuthenticationHandlerConfig getAuthenticationHandlerConfig() {
        return authenticationHandlerConfig;
    }

    public void setAuthenticationHandlerConfig(AuthenticationHandlerConfig authenticationHandlerConfig) {
        this.authenticationHandlerConfig = authenticationHandlerConfig;
    }

    public MultiOptionHandlerConfig getMultiOptionHandler() {
        return multiOptionHandler;
    }

    public void setMultiOptionHandler(MultiOptionHandlerConfig multiOptionHandler) {
        this.multiOptionHandler = multiOptionHandler;
    }
}