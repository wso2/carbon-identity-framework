package org.wso2.carbon.identity.gateway.common.model.sp;

class Option extends HandlerInterceptor {
    private AuthenticationHandler authenticationHandler;

    public AuthenticationHandler getAuthenticationHandler() {
        return authenticationHandler;
    }

    public void setAuthenticationHandler(AuthenticationHandler authenticationHandler) {
        this.authenticationHandler = authenticationHandler;
    }
}