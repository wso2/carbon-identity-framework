package org.wso2.carbon.identity.sample.inbound.response;

import org.wso2.carbon.identity.common.base.exception.IdentityException;
import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.processor.FrameworkHandlerResponse;
import org.wso2.carbon.identity.gateway.processor.handler.response.AbstractResponseHandler;
import org.wso2.carbon.identity.gateway.processor.handler.response.ResponseException;

public class SampleProtocolResponseHandler extends AbstractResponseHandler {
    @Override
    public FrameworkHandlerResponse buildErrorResponse(AuthenticationContext authenticationContext, IdentityException identityException) throws ResponseException {
        return null;
    }

    @Override
    public FrameworkHandlerResponse buildResponse(AuthenticationContext authenticationContext) throws ResponseException {
        return null;
    }

    @Override
    protected String getValidatorType() {
        return null;
    }
}
