package org.wso2.carbon.identity.gateway.processor.request;


import org.wso2.carbon.identity.gateway.api.FrameworkRuntimeException;


public class LocalAuthenticationRequest extends AuthenticationRequest {

    protected LocalAuthenticationRequest(
            LocalAuthenticationRequestBuilder builder) {
        super(builder);

    }

    public static class LocalAuthenticationRequestBuilder extends AuthenticationRequest.AuthenticationRequestBuilder {

        public LocalAuthenticationRequestBuilder() {
            super();
        }


        @Override
        public LocalAuthenticationRequest build() throws FrameworkRuntimeException {
            return new LocalAuthenticationRequest(this);
        }
    }

    public static class LocalAuthenticationRequestConstants extends AuthenticationRequest.IdentityRequestConstants {

    }
}
