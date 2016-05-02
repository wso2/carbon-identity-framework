package org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.builder;

import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.ResponseHandler;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityMessageContext;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityResponse;

public class AuthenticationResponseBuilderHandler extends ResponseHandler {
    @Override
    public IdentityResponse.IdentityResponseBuilder buildErrorResponse(IdentityMessageContext identityMessageContext) {
        return null;
    }


    @Override
    public IdentityResponse.IdentityResponseBuilder buildResponse(IdentityMessageContext identityMessageContext) {
        return null;
    }
}
