package org.wso2.carbon.identity.gateway.processor.request;

import org.wso2.carbon.identity.gateway.api.FrameworkRuntimeException;
import org.wso2.carbon.identity.gateway.api.IdentityRequest;
import org.wso2.msf4j.Request;

import java.util.UUID;


public class AuthenticationRequest extends IdentityRequest {

    private String requestDataKey;

    protected AuthenticationRequest(
            AuthenticationRequestBuilder builder) {
        super(builder);
        requestDataKey = builder.requestDataKey;
        if(requestDataKey == null){
            requestDataKey = UUID.randomUUID().toString();
        }
    }

    public String getRequestDataKey() {
        return requestDataKey;
    }

    public static class AuthenticationRequestBuilder extends IdentityRequestBuilder {

        private String requestDataKey;

        public AuthenticationRequestBuilder() {
            super();
        }

        public AuthenticationRequestBuilder setRequestDataKey(String requestDataKey) {
            this.requestDataKey = requestDataKey;
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
