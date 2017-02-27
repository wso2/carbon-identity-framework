package org.wso2.carbon.identity.sample.inbound.response;

import org.wso2.carbon.identity.common.base.exception.IdentityException;
import org.wso2.carbon.identity.common.base.message.MessageContext;
import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.processor.FrameworkHandlerResponse;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.AuthenticationHandlerException;
import org.wso2.carbon.identity.gateway.processor.handler.response.AbstractResponseHandler;
import org.wso2.carbon.identity.gateway.processor.handler.response.ResponseException;
import org.wso2.carbon.identity.sample.inbound.request.SampleProtocolRequest;

import java.util.Properties;

public class SampleProtocolResponseHandler extends AbstractResponseHandler {
    @Override
    public FrameworkHandlerResponse buildErrorResponse(AuthenticationContext authenticationContext, IdentityException identityException) throws ResponseException {
        return null;
    }

    @Override
    public FrameworkHandlerResponse buildResponse(AuthenticationContext authenticationContext) throws ResponseException {
        FrameworkHandlerResponse response = FrameworkHandlerResponse.REDIRECT;
        SampleLoginResponse.SampleLoginResponseBuilder builder = new SampleLoginResponse.SampleLoginResponseBuilder
                (authenticationContext);
        builder.setSubject(authenticationContext.getSequenceContext().getStepContext(1).getUser().getUserIdentifier());
        addSessionKey(builder, authenticationContext);
        response.setGatewayResponseBuilder(builder);
        return response;
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
