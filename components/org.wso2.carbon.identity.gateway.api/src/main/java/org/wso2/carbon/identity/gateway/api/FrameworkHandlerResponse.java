package org.wso2.carbon.identity.gateway.api;

public enum FrameworkHandlerResponse {
    REDIRECT, CONTINUE;

    private IdentityResponse.IdentityResponseBuilder identityResponseBuilder;

    public IdentityResponse.IdentityResponseBuilder getIdentityResponseBuilder() {
        return identityResponseBuilder;
    }

    public void setIdentityResponseBuilder(
            IdentityResponse.IdentityResponseBuilder identityResponseBuilder) {
        this.identityResponseBuilder = identityResponseBuilder;
    }
}
