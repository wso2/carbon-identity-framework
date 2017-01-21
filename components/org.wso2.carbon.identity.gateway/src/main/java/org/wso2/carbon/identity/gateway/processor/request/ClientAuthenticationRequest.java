package org.wso2.carbon.identity.gateway.processor.request;


import org.wso2.carbon.identity.gateway.api.FrameworkRuntimeException;

public class ClientAuthenticationRequest extends AuthenticationRequest {

    private String uniqueId;
    private String type;

    protected ClientAuthenticationRequest(
            ClientAuthenticationRequestBuilder builder, String uniqueId, String type) {
        super(builder);
        this.uniqueId = uniqueId ;
        this.type = type ;
    }

    public String getUniqueId() {
        return uniqueId;
    }


    public String getType() {
        return type;
    }

    public static class ClientAuthenticationRequestBuilder extends AuthenticationRequest.AuthenticationRequestBuilder {

        private String uniqueId;
        private String type;


        public ClientAuthenticationRequestBuilder() {
            super();
        }


        public ClientAuthenticationRequestBuilder setUniqueId(String uniqueId) {
            this.uniqueId = uniqueId;
            return this;
        }
        public ClientAuthenticationRequestBuilder setType(String type) {
            this.type = type;
            return this;
        }
        @Override
        public ClientAuthenticationRequest build() throws FrameworkRuntimeException {
            return new ClientAuthenticationRequest(this, uniqueId, type);
        }
    }

    public static class ClientAuthenticationRequestConstants extends AuthenticationRequest.IdentityRequestConstants {

    }
}
