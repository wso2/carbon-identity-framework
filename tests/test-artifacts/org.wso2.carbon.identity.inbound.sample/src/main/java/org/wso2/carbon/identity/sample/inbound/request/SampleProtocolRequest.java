package org.wso2.carbon.identity.sample.inbound.request;

import org.wso2.carbon.identity.gateway.api.exception.GatewayRuntimeException;
import org.wso2.carbon.identity.gateway.processor.request.ClientAuthenticationRequest;
import org.wso2.msf4j.Request;

public class SampleProtocolRequest extends ClientAuthenticationRequest {

    protected SampleProtocolRequest(SampleProtocolRequestBuilder builder) {
        super(builder);
    }


    public static class SampleProtocolRequestBuilder extends ClientAuthenticationRequest
            .ClientAuthenticationRequestBuilder {

        public SampleProtocolRequestBuilder(Request request) {
            super();
        }

        public SampleProtocolRequest build() throws GatewayRuntimeException {
            return new SampleProtocolRequest(this);
        }
    }



}
