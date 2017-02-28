package org.wso2.carbon.identity.sample.inbound.validator;

import org.wso2.carbon.identity.common.base.message.MessageContext;
import org.wso2.carbon.identity.gateway.api.context.GatewayMessageContext;
import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.processor.FrameworkHandlerResponse;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.AuthenticationHandlerException;
import org.wso2.carbon.identity.gateway.processor.handler.request.AbstractRequestValidator;
import org.wso2.carbon.identity.gateway.processor.handler.request.RequestValidatorException;
import org.wso2.carbon.identity.sample.inbound.request.SampleProtocolRequest;

public class SampleProtocolValidator extends AbstractRequestValidator {
    @Override
    public FrameworkHandlerResponse validate(AuthenticationContext authenticationContext) throws
                                                                                          RequestValidatorException {
        authenticationContext.setUniqueId("travelocity.com");
        if (authenticationContext.getServiceProvider() == null) {
            throw new RequestValidatorException("No Service Provider Found for this Unique ID");
        }
        return FrameworkHandlerResponse.CONTINUE;
    }

    @Override
    public boolean canHandle(MessageContext messageContext) {
        if (messageContext instanceof GatewayMessageContext) {
            GatewayMessageContext gatewayMessageContext = (GatewayMessageContext) messageContext;
            if (gatewayMessageContext.getIdentityRequest() instanceof SampleProtocolRequest) {
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    protected String getValidatorType() {
        return "SampleProtocolRequestValidator";
    }
}
