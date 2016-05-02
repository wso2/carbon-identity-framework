package org.wso2.carbon.identity.application.authentication.framework.inbound;

public enum FrameworkHandlerStatus {
    REDIRECT, CONTINUE ;

    private IdentityResponse.IdentityResponseBuilder identityResponseBuilder ;

    public IdentityResponse.IdentityResponseBuilder getIdentityResponseBuilder() {
        return identityResponseBuilder;
    }

    public void setIdentityResponseBuilder(
            IdentityResponse.IdentityResponseBuilder identityResponseBuilder) {
        this.identityResponseBuilder = identityResponseBuilder;
    }
}
