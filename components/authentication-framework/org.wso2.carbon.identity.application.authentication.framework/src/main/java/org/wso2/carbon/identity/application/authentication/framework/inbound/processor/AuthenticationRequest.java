package org.wso2.carbon.identity.application.authentication.framework.inbound.processor;

import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AuthenticationRequest extends IdentityRequest{

    private String requestDataKey ;

    protected AuthenticationRequest(
            IdentityRequestBuilder builder) {
        super(builder);
    }

    public static class AuthenticationRequestBuilder extends IdentityRequestBuilder{
        private String requestDataKey ;

        public AuthenticationRequestBuilder(HttpServletRequest request, HttpServletResponse response) {
            super(request, response);
        }

        public String getRequestDataKey() {
            return requestDataKey;
        }

        public void setRequestDataKey(String requestDataKey) {
            this.requestDataKey = requestDataKey;
        }
    }

    public static class AuthenticationRequestConstants{
        private static final String REQUEST_DATA_KEY = "RequestDataKey" ;
    }
}
