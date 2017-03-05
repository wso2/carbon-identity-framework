package org.wso2.carbon.identity.sample.inbound.response;

import org.wso2.carbon.identity.common.base.exception.IdentityException;
import org.wso2.carbon.identity.common.base.message.MessageContext;
import org.wso2.carbon.identity.gateway.api.exception.GatewayException;
import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.api.response.GatewayHandlerResponse;
import org.wso2.carbon.identity.gateway.processor.handler.response.AbstractResponseHandler;
import org.wso2.carbon.identity.gateway.processor.handler.response.ResponseHandlerException;
import org.wso2.carbon.identity.sample.inbound.request.SampleProtocolRequest;

public class SampleProtocolResponseHandler extends AbstractResponseHandler {


    @Override
    public GatewayHandlerResponse buildErrorResponse(AuthenticationContext authenticationContext,
                                                     GatewayException exception) throws ResponseHandlerException {
        return null;
    }

    @Override
    public GatewayHandlerResponse buildResponse(AuthenticationContext authenticationContext) throws
                                                                                               ResponseHandlerException {
        GatewayHandlerResponse response = GatewayHandlerResponse.REDIRECT;
        SampleLoginResponse.SampleLoginResponseBuilder builder = new SampleLoginResponse.SampleLoginResponseBuilder
                (authenticationContext);
        builder.setSubject(authenticationContext.getSequenceContext().getStepContext(1).getUser().getUserIdentifier());
        addSessionKey(builder, authenticationContext);
        response.setGatewayResponseBuilder(builder);
        return response;
    }

    @Override
    public boolean canHandle(AuthenticationContext authenticationContext, GatewayException e) {
        return false;
    }

    protected String getValidatorType() {
        return "SAMPLE";
    }


    public boolean canHandle(MessageContext messageContext) {
        if (messageContext instanceof AuthenticationContext) {
            return ((AuthenticationContext) messageContext).getInitialAuthenticationRequest() instanceof
                    SampleProtocolRequest;
        }
        return false;
    }

}
