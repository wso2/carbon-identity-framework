package org.wso2.carbon.identity.application.authentication.framework.processor.request;


import org.wso2.carbon.identity.application.authentication.framework.FrameworkRuntimeException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LocalAuthenticationRequest extends AuthenticationRequest {

    private String requestDataKey;
    private String authenticatorName;

    protected LocalAuthenticationRequest(
            LocalAuthenticationRequestBuilder builder) {
        super(builder);
        requestDataKey = builder.requestDataKey;
        authenticatorName = builder.authenticatorName;
    }

    public String getRequestDataKey() {
        return requestDataKey;
    }

    public String getAuthenticatorName() {
        return authenticatorName;
    }

    public static class LocalAuthenticationRequestBuilder extends AuthenticationRequest.AuthenticationRequestBuilder {

        private String requestDataKey;
        private String authenticatorName;

        public LocalAuthenticationRequestBuilder() {
            super();
        }

        public LocalAuthenticationRequestBuilder(HttpServletRequest request, HttpServletResponse response) {
            super(request, response);
        }

        public LocalAuthenticationRequestBuilder setRequestDataKey(String requestDataKey) {
            this.requestDataKey = requestDataKey;
            return this;
        }

        public LocalAuthenticationRequestBuilder setAuthenticatorName(String authenticatorName) {
            this.authenticatorName = authenticatorName;
            return this;
        }


        @Override
        public LocalAuthenticationRequest build() throws FrameworkRuntimeException {
            return new LocalAuthenticationRequest(this);
        }
    }

    public static class LocalAuthenticationRequestConstants extends AuthenticationRequest.IdentityRequestConstants {
        public static final String REQUEST_DATA_KEY = "RequestDataKey";
        public static final String AUTHENTICATOR_NAME = "AuthenticatorName";
    }
}
