package org.wso2.carbon.identity.gateway.processor.request;


import org.wso2.carbon.identity.gateway.api.FrameworkRuntimeException;


public class CallbackAuthenticationRequest extends AuthenticationRequest {

    protected CallbackAuthenticationRequest(CallbackAuthenticationRequestBuilder builder) {
        super(builder);

    }

    public static class CallbackAuthenticationRequestBuilder extends AuthenticationRequest.AuthenticationRequestBuilder {

        public CallbackAuthenticationRequestBuilder() {
            super();
        }


        @Override
        public CallbackAuthenticationRequest build() throws FrameworkRuntimeException {
            return new CallbackAuthenticationRequest(this);
        }
    }

    public static class CallbackAuthenticationRequestConstants extends AuthenticationRequest.AuthenticationRequestConstants {

    }
}
