package org.wso2.carbon.identity.application.authentication.framework.processor.request;


import org.wso2.carbon.identity.application.authentication.framework.FrameworkRuntimeException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ClientAuthenticationRequest extends AuthenticationRequest{

    private String clientId;
    private String requestType;

    protected ClientAuthenticationRequest(
            ClientAuthenticationRequestBuilder builder) {
        super(builder);
        clientId = builder.clientId;
        requestType = builder.requestType;
    }

    public String getClientId() {
        return clientId;
    }

    public String getRequestType() {
        return requestType;
    }


    public static class ClientAuthenticationRequestBuilder extends AuthenticationRequest.AuthenticationRequestBuilder {

        private String clientId;
        private String requestType;

        public ClientAuthenticationRequestBuilder() {
            super();
        }

        public ClientAuthenticationRequestBuilder(HttpServletRequest request, HttpServletResponse response) {
            super(request, response);
        }

        public ClientAuthenticationRequestBuilder setClientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public ClientAuthenticationRequestBuilder setRequestType(String requestType) {
            this.requestType = requestType;
            return this;
        }

        @Override
        public ClientAuthenticationRequest build() throws FrameworkRuntimeException {
            return new ClientAuthenticationRequest(this);
        }
    }

    public static class ClientAuthenticationRequestConstants extends AuthenticationRequest.IdentityRequestConstants {
        public static final String CLIENT_ID = "ClientId";
        public static final String REQUEST_TYPE = "RequestType";
    }
}
