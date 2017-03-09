package org.wso2.carbon.identity.gateway.authentication.local;

import org.wso2.carbon.identity.gateway.api.context.GatewayMessageContext;
import org.wso2.carbon.identity.gateway.api.response.GatewayResponse;


public class LocalAuthenticationResponse extends GatewayResponse{
    protected String endpointURL;
    protected String relayState;
    //#TODO:This has to be improve to transfer more data to the endpoint. For now this is redirection and must be
    // post with all idp:auth list and the execution strategy. Based on that we may have to do lot of stuff in UI.
    protected String identityProviderList ;

    protected LocalAuthenticationResponse(LocalAuthenticationResponseBuilder builder) {
        super(builder);
        this.endpointURL = builder.endpointURL;
        this.relayState = builder.relayState ;
    }

    public String getEndpointURL() {
        return endpointURL;
    }

    public String getRelayState() {
        return relayState;
    }

    public String getIdentityProviderList() {
        return identityProviderList;
    }

    public static class LocalAuthenticationResponseBuilder extends GatewayResponseBuilder {
        protected String endpointURL;
        protected String relayState ;
        protected String identityProviderList ;

        public LocalAuthenticationResponseBuilder(GatewayMessageContext context) {
            this.context = context;
        }

        public LocalAuthenticationResponseBuilder() {

        }
        public GatewayResponseBuilder setEndpointURL(String endpointURL) {
            this.endpointURL = endpointURL;
            return this;
        }

        public GatewayResponseBuilder setIdentityProviderList(String identityProviderList) {
            this.identityProviderList = identityProviderList;
            return this;
        }

        public GatewayResponseBuilder setRelayState(String relayState) {
            this.relayState = relayState;
            return this;
        }
        public LocalAuthenticationResponse build() {
            return new LocalAuthenticationResponse(this);
        }

    }
}
