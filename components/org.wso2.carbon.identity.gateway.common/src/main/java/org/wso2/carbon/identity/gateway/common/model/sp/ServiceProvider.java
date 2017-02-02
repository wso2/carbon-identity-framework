package org.wso2.carbon.identity.gateway.common.model.sp;


public class ServiceProvider {

    private String name;
    private AuthenticationRequestHandler authenticationRequestHandler;
    private AuthenticationSequenceHandler authenticationSequenceHandler;

    public AuthenticationRequestHandler getAuthenticationRequestHandler() {
        return authenticationRequestHandler;
    }

    public void setAuthenticationRequestHandler(AuthenticationRequestHandler authenticationRequestHandler) {
        this.authenticationRequestHandler = authenticationRequestHandler;
    }

    public AuthenticationSequenceHandler getAuthenticationSequenceHandler() {
        return authenticationSequenceHandler;
    }

    public void setAuthenticationSequenceHandler(AuthenticationSequenceHandler authenticationSequenceHandler) {
        this.authenticationSequenceHandler = authenticationSequenceHandler;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
