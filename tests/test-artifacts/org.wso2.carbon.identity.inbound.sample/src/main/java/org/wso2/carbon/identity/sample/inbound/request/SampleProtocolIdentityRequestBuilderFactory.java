package org.wso2.carbon.identity.sample.inbound.request;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.gateway.api.exception.GatewayClientException;
import org.wso2.carbon.identity.gateway.api.exception.GatewayServerException;
import org.wso2.carbon.identity.gateway.api.request.GatewayRequest;
import org.wso2.carbon.identity.gateway.api.request.GatewayRequestBuilderFactory;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.util.Utility;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;

public class SampleProtocolIdentityRequestBuilderFactory extends GatewayRequestBuilderFactory {

    private static Logger log = LoggerFactory.getLogger(SampleProtocolIdentityRequestBuilderFactory.class);

    @Override
    public String getName() {
        return "SampleIdentityRequestBuilderFactory";
    }

    @Override
    public boolean canHandle(Request request) throws GatewayClientException, GatewayServerException {
        String sampleProtocol = Utility.getParameter(request, "sampleProtocol");
        String errorWhileCanHandleClient = Utility.getParameter(request, "canHandleErrorClient");
        String errorWhileCanHandleServer = Utility.getParameter(request, "canHandleErrorServer");
        if (errorWhileCanHandleClient != null) {
            throw new GatewayClientException("Throwing client exception");
        }

        if (errorWhileCanHandleServer != null) {
            throw new GatewayServerException("Throwing Server exception");
        }
        if (StringUtils.isNotBlank(sampleProtocol)) {
            return true;
        }
        return false;
    }

    @Override
    public int getPriority() {
        return 50;
    }

    @Override
    public GatewayRequest.GatewayRequestBuilder create(Request request) throws GatewayClientException {

        GatewayRequest.GatewayRequestBuilder builder = new SampleProtocolRequest.SampleProtocolRequestBuilder(request);
        super.create(builder, request);
        return builder;
    }

    public Response.ResponseBuilder handleException(GatewayClientException exception) {
      return  super.handleException(exception);
    }
}
