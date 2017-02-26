package org.wso2.carbon.identity.sample.inbound.response;

import org.wso2.carbon.identity.gateway.api.response.GatewayResponse;
import org.wso2.carbon.identity.gateway.api.response.GatewayResponseBuilderFactory;

import javax.ws.rs.core.Response;

public class SampleProtocolResponseBuilderFactory extends GatewayResponseBuilderFactory {

    public boolean canHandle(GatewayResponse gatewayResponse) {
        return gatewayResponse instanceof SampleLoginResponse;
    }


    @Override
    public Response.ResponseBuilder createBuilder(GatewayResponse gatewayResponse) {
        Response.ResponseBuilder builder = Response.noContent();
        createBuilder(builder, gatewayResponse);
        return builder;
    }

    @Override
    public void createBuilder(Response.ResponseBuilder builder, GatewayResponse gatewayResponse) {
        StringBuilder httpQueryString = new StringBuilder("authenticatedUser=" + ((SampleLoginResponse)
                gatewayResponse).getSubject());
        String location = "https://localhost:9443/response";
        location = location.concat("?").concat(httpQueryString.toString());
        builder.status(302);
        builder.header("location", location);
    }

}
