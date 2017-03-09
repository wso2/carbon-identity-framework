package org.wso2.carbon.identity.gateway.authentication.local;

import com.google.common.net.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.gateway.api.response.GatewayResponse;
import org.wso2.carbon.identity.gateway.api.response.GatewayResponseBuilderFactory;

import javax.ws.rs.core.Response;
import java.net.URLEncoder;

public class LocalAuthenticationResponseBuilderFactory extends GatewayResponseBuilderFactory {

    private static Logger log = LoggerFactory.getLogger(LocalAuthenticationResponseBuilderFactory.class);

    public boolean canHandle(GatewayResponse gatewayResponse) {
        return gatewayResponse instanceof LocalAuthenticationResponse;
    }

    @Override
    public Response.ResponseBuilder createBuilder(GatewayResponse gatewayResponse) {
        Response.ResponseBuilder builder = Response.noContent();
        createBuilder(builder, gatewayResponse);
        return builder;
    }

    @Override
    public void createBuilder(Response.ResponseBuilder builder, GatewayResponse gatewayResponse) {

        LocalAuthenticationResponse localAuthenticationResponse = (LocalAuthenticationResponse) gatewayResponse;
        builder.status(302);
        String url = "https://localhost:9292/gateway/endpoint?callback=" + URLEncoder.encode
                ("https://localhost:9292/gateway")+
        "&state=" + (
                (LocalAuthenticationResponse) gatewayResponse).getRelayState()+"&idplist="+(
                        (LocalAuthenticationResponse) gatewayResponse).getIdentityProviderList();
        builder.header(HttpHeaders.LOCATION, url);

    }


    public int getPriority() {
        return 100;
    }
}