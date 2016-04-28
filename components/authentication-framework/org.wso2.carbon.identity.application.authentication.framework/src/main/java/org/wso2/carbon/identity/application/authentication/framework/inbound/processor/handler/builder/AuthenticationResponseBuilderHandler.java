package org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.builder;

import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.ResponseBuilderHandler;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityMessageContext;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityResponse;

public class AuthenticationResponseBuilderHandler extends ResponseBuilderHandler {
    @Override
    public IdentityResponse.IdentityResponseBuilder buildErrorResponse(IdentityMessageContext identityMessageContext) {
        return null;
    }
}
