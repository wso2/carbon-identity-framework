package org.wso2.carbon.identity.gateway.processor.handler.authentication.impl;


import org.wso2.carbon.identity.gateway.api.IdentityResponse;

public enum AuthenticationResponse {
    AUTHENTICATED,
    INCOMPLETE;

    private IdentityResponse.IdentityResponseBuilder identityResponseBuilder;

    public IdentityResponse.IdentityResponseBuilder getIdentityResponseBuilder() {
        return identityResponseBuilder;
    }

    public AuthenticationResponse setIdentityResponseBuilder(
            IdentityResponse.IdentityResponseBuilder identityResponseBuilder) {
        this.identityResponseBuilder = identityResponseBuilder;
        return this;
    }
}
