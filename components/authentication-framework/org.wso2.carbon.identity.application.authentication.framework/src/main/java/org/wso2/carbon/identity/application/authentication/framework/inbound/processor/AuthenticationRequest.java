package org.wso2.carbon.identity.application.authentication.framework.inbound.processor;

import org.wso2.carbon.identity.application.authentication.framework.inbound.FrameworkRuntimeException;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AuthenticationRequest extends IdentityRequest{

    private String requestDataKey ;

    protected AuthenticationRequest(
            AuthenticationRequestBuilder builder) {
        super(builder);
        requestDataKey = builder.requestDataKey;
    }

    public String getRequestDataKey() {
        return requestDataKey;
    }

    public static class AuthenticationRequestBuilder extends IdentityRequestBuilder{
        private String requestDataKey ;

        public AuthenticationRequestBuilder(HttpServletRequest request, HttpServletResponse response) {
            super(request, response);
        }


        public AuthenticationRequestBuilder setRequestDataKey(String requestDataKey) {
            this.requestDataKey = requestDataKey;
            return this;
        }

        @Override
        public IdentityRequest build() throws FrameworkRuntimeException {
            return new AuthenticationRequest(this);
        }
    }

    public static class AuthenticationRequestConstants{
        public static final String REQUEST_DATA_KEY = "RequestDataKey" ;
    }
}
