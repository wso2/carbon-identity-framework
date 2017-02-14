package org.wso2.carbon.identity.gateway.processor.request;


import org.wso2.carbon.identity.gateway.api.FrameworkRuntimeException;

public class ClientAuthenticationRequest extends AuthenticationRequest {

    protected ClientAuthenticationRequest(ClientAuthenticationRequest.ClientAuthenticationRequestBuilder builder) {
        super(builder);

    }

    public static class ClientAuthenticationRequestBuilder extends AuthenticationRequest.AuthenticationRequestBuilder {

        public ClientAuthenticationRequestBuilder() {
            super();
        }


        @Override
        public ClientAuthenticationRequest build() throws FrameworkRuntimeException {
            return new ClientAuthenticationRequest(this);
        }
    }

    public static class ClientAuthenticationRequestConstants extends AuthenticationRequest.IdentityRequestConstants {

    }
}
