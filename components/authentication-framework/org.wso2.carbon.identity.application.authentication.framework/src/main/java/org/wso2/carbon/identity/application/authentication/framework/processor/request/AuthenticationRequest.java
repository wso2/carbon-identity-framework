package org.wso2.carbon.identity.application.authentication.framework.processor.request;

import org.wso2.carbon.identity.application.authentication.framework.FrameworkRuntimeException;
import org.wso2.carbon.identity.application.authentication.framework.IdentityRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AuthenticationRequest extends IdentityRequest {

    protected AuthenticationRequest(
            AuthenticationRequestBuilder builder) {
        super(builder);
    }



    public static class AuthenticationRequestBuilder extends IdentityRequestBuilder {
        public AuthenticationRequestBuilder() {
            super();
        }



        public AuthenticationRequestBuilder(HttpServletRequest request, HttpServletResponse response) {
            super(request, response);
        }


        @Override
        public AuthenticationRequest build() throws FrameworkRuntimeException {
            return new AuthenticationRequest(this);
        }
    }

    public static class AuthenticationRequestConstants extends IdentityRequestConstants {

    }
}
