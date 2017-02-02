package org.wso2.carbon.identity.gateway.common.model.sp;

class StepHandler extends HandlerInterceptor {
    private MultiOptionHandler multiOptionHandler;
    private AuthenticationHandler authenticationHandler;

    public AuthenticationHandler getAuthenticationHandler() {
        return authenticationHandler;
    }

    public void setAuthenticationHandler(AuthenticationHandler authenticationHandler) {
        this.authenticationHandler = authenticationHandler;
    }

    public MultiOptionHandler getMultiOptionHandler() {
        return multiOptionHandler;
    }

    public void setMultiOptionHandler(MultiOptionHandler multiOptionHandler) {
        this.multiOptionHandler = multiOptionHandler;
    }
}