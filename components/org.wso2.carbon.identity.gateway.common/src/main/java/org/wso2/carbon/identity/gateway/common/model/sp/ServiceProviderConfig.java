package org.wso2.carbon.identity.gateway.common.model.sp;


public class ServiceProviderConfig {

    private String name;
    private AuthenticationRequestHandlerConfig authenticationRequestHandlerConfig;
    private AuthenticationSequenceHandlerConfig authenticationSequenceHandlerConfig;

    public AuthenticationRequestHandlerConfig getAuthenticationRequestHandlerConfig() {
        return authenticationRequestHandlerConfig;
    }

    public void setAuthenticationRequestHandlerConfig(AuthenticationRequestHandlerConfig authenticationRequestHandlerConfig) {
        this.authenticationRequestHandlerConfig = authenticationRequestHandlerConfig;
    }

    public AuthenticationSequenceHandlerConfig getAuthenticationSequenceHandlerConfig() {
        return authenticationSequenceHandlerConfig;
    }

    public void setAuthenticationSequenceHandlerConfig(AuthenticationSequenceHandlerConfig authenticationSequenceHandlerConfig) {
        this.authenticationSequenceHandlerConfig = authenticationSequenceHandlerConfig;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
