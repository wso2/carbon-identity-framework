package org.wso2.carbon.identity.sample.inbound.validator;

import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.processor.FrameworkHandlerResponse;
import org.wso2.carbon.identity.gateway.processor.handler.request.AbstractRequestHandler;
import org.wso2.carbon.identity.gateway.processor.handler.request.RequestHandlerException;

public class SampleProtocolValidator extends AbstractRequestHandler{
    @Override
    public FrameworkHandlerResponse validate(AuthenticationContext authenticationContext) throws RequestHandlerException {
        return null;
    }

    @Override
    protected String getValidatorType() {
        return "SampleProtocolRequestValidator";
    }
}
