package org.wso2.carbon.identity.application.authentication.framework.inbound.processor.request;


import org.wso2.carbon.identity.application.authentication.framework.inbound.FrameworkRuntimeException;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LocalAuthenticationRequest extends AuthenticationRequest{

    private String requestDataKey;

    protected LocalAuthenticationRequest(
            LocalAuthenticationRequestBuilder builder) {
        super(builder);
        requestDataKey = builder.requestDataKey;
    }

    public String getRequestDataKey() {
        return requestDataKey;
    }

    public static class LocalAuthenticationRequestBuilder extends AuthenticationRequest.AuthenticationRequestBuilder {

        private String requestDataKey;

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


        @Override
        public LocalAuthenticationRequest build() throws FrameworkRuntimeException {
            return new LocalAuthenticationRequest(this);
        }
    }

    public static class LocalAuthenticationRequestConstants extends AuthenticationRequest.IdentityRequestConstants {
        public static final String REQUEST_DATA_KEY = "RequestDataKey";
    }
}
