package org.wso2.carbon.identity.application.authentication.framework.inbound.processor;

import org.wso2.carbon.identity.application.authentication.framework.inbound.FrameworkRuntimeException;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AuthenticationRequest extends IdentityRequest {

    private String requestDataKey;

    private String clientId;
    private String requestType;

    protected AuthenticationRequest(
            AuthenticationRequestBuilder builder) {
        super(builder);
        requestDataKey = builder.requestDataKey;
        clientId = builder.clientId;
        requestType = builder.requestType;
    }

    public String getRequestDataKey() {
        return requestDataKey;
    }

    public String getClientId() {
        return clientId;
    }

    public String getRequestType() {
        return requestType;
    }


    public static class AuthenticationRequestBuilder extends IdentityRequestBuilder {
        private String requestDataKey;

        private String clientId;
        private String requestType;

        public AuthenticationRequestBuilder(HttpServletRequest request, HttpServletResponse response) {
            super(request, response);
        }


        public AuthenticationRequestBuilder setRequestDataKey(String requestDataKey) {
            this.requestDataKey = requestDataKey;
            return this;
        }

        public AuthenticationRequestBuilder setClientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public AuthenticationRequestBuilder setRequestType(String requestType) {
            this.requestType = requestType;
            return this;
        }

        @Override
        public AuthenticationRequest build() throws FrameworkRuntimeException {
            return new AuthenticationRequest(this);
        }
    }

    public static class AuthenticationRequestConstants extends IdentityRequestConstants {
        public static final String REQUEST_DATA_KEY = "RequestDataKey";
    }
}
