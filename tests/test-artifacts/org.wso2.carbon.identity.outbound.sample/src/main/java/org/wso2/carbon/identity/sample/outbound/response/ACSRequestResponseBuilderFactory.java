package org.wso2.carbon.identity.sample.outbound.response;

import org.wso2.carbon.identity.gateway.api.response.GatewayResponse;
import org.wso2.carbon.identity.gateway.api.response.GatewayResponseBuilderFactory;

import javax.ws.rs.core.Response;

public class ACSRequestResponseBuilderFactory extends GatewayResponseBuilderFactory {

    public boolean canHandle(GatewayResponse gatewayResponse) {
        return gatewayResponse instanceof SampleProtocolRequestResponse;
    }


    @Override
    public Response.ResponseBuilder createBuilder(GatewayResponse gatewayResponse) {
        Response.ResponseBuilder builder = Response.noContent();
        createBuilder(builder, gatewayResponse);
        return builder;
    }

    @Override
    public void createBuilder(Response.ResponseBuilder builder, GatewayResponse gatewayResponse) {

        SampleProtocolRequestResponse sampleProtocolRequestResponse = (SampleProtocolRequestResponse) gatewayResponse;
        builder.status(200);
    }

}
