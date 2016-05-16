package org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl;


import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityResponse;

public enum AuthenticationResponse {
    AUTHENTICATED,
    FAILED,
    INCOMPLETE;

    private IdentityResponse.IdentityResponseBuilder identityResponseBuilder;

    public IdentityResponse.IdentityResponseBuilder getIdentityResponseBuilder() {
        return identityResponseBuilder;
    }

    public void setIdentityResponseBuilder(
            IdentityResponse.IdentityResponseBuilder identityResponseBuilder) {
        this.identityResponseBuilder = identityResponseBuilder;
    }
}
