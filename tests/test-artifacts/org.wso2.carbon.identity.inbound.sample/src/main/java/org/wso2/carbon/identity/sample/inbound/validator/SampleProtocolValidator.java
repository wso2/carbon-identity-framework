package org.wso2.carbon.identity.sample.inbound.validator;

import org.wso2.carbon.identity.common.base.message.MessageContext;
import org.wso2.carbon.identity.gateway.api.context.GatewayMessageContext;
import org.wso2.carbon.identity.gateway.handler.GatewayHandlerResponse;
import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.handler.validator.AbstractRequestValidator;
import org.wso2.carbon.identity.gateway.exception.RequestValidatorException;
import org.wso2.carbon.identity.sample.inbound.request.SampleProtocolRequest;

public class SampleProtocolValidator extends AbstractRequestValidator {
    @Override
    public GatewayHandlerResponse validate(AuthenticationContext authenticationContext) throws
                                                                                          RequestValidatorException {
        authenticationContext.setUniqueId("travelocity.com");
        if (authenticationContext.getServiceProvider() == null) {
            throw new RequestValidatorException("No Service Provider Found for this Unique ID");
        }

        if (authenticationContext.getIdentityRequest().getParameter("NotProtocolCompliant") != null) {
            throw new RequestValidatorException("Error while validating request");
        }
        // Can access validator configurations.
        getValidatorConfig(authenticationContext);
        return GatewayHandlerResponse.CONTINUE;
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
