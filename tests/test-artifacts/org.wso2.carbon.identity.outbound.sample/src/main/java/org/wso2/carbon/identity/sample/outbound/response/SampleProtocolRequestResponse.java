package org.wso2.carbon.identity.sample.outbound.response;

import org.wso2.carbon.identity.gateway.api.response.GatewayResponse;

public class SampleProtocolRequestResponse extends GatewayResponse {

    protected SampleProtocolRequestResponse(GatewayResponseBuilder builder) {
        super(builder);
    }

    public static class SampleProtocolRequestResponseBuilder extends GatewayResponseBuilder {

        public SampleProtocolRequestResponse build() {
            return new SampleProtocolRequestResponse(this);
        }
    }

}
