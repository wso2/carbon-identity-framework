package org.wso2.carbon.identity.gateway.processor.request;

import org.wso2.carbon.identity.gateway.api.FrameworkRuntimeException;
import org.wso2.carbon.identity.gateway.api.IdentityRequest;
import org.wso2.msf4j.Request;

import java.util.UUID;


public class AuthenticationRequest extends IdentityRequest {

    protected String requestDataKey;
    protected String sessionDataKey;

    protected AuthenticationRequest(
            AuthenticationRequestBuilder builder) {
        super(builder);
        requestDataKey = builder.requestDataKey;
        sessionDataKey = builder.sessionDataKey;
        if(requestDataKey == null){
            requestDataKey = UUID.randomUUID().toString();
        }
    }

    public String getRequestDataKey() {
        return requestDataKey;
    }

    public String getSessionDataKey() {
        return sessionDataKey;
    }

    public static class AuthenticationRequestBuilder extends IdentityRequestBuilder {

        protected String requestDataKey;
        protected String sessionDataKey;

        public AuthenticationRequestBuilder setRequestDataKey(String requestDataKey) {
            this.requestDataKey = requestDataKey;
            return this;
        }

        public AuthenticationRequestBuilder setSessionDataKey(String sessionDataKey) {
            this.sessionDataKey = sessionDataKey;
            return this;
        }

        @Override
        public AuthenticationRequest build() throws FrameworkRuntimeException {
            return new AuthenticationRequest(this);
        }
    }

    public static class AuthenticationRequestConstants extends IdentityRequestConstants {
        public static final String REQUEST_DATA_KEY = "RequestDataKey";
        public static final String SESSION_DATA_KEY = "SessionDataKey";
    }
}
