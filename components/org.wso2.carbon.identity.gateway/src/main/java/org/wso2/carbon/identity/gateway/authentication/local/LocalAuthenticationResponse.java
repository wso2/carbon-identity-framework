package org.wso2.carbon.identity.gateway.authentication.local;

import org.wso2.carbon.identity.gateway.api.context.GatewayMessageContext;
import org.wso2.carbon.identity.gateway.api.response.GatewayResponse;


public class LocalAuthenticationResponse extends GatewayResponse{
    protected String endpointURL;
    protected LocalAuthenticationResponse(LocalAuthenticationResponseBuilder builder) {
        super(builder);
        this.endpointURL = builder.endpointURL;
    }

    public String getEndpointURL() {
        return endpointURL;
    }

    public static class LocalAuthenticationResponseBuilder extends GatewayResponseBuilder {
        protected String endpointURL;

        public LocalAuthenticationResponseBuilder(GatewayMessageContext context) {
            this.context = context;
        }

        public LocalAuthenticationResponseBuilder() {

        }
        public GatewayResponseBuilder setEndpointURL(String endpointURL) {
            this.endpointURL = endpointURL;
            return this;
        }
        public LocalAuthenticationResponse build() {
            return new LocalAuthenticationResponse(this);
        }

    }
}
