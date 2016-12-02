package org.wso2.carbon.identity.gateway.handler.validation;

import org.wso2.carbon.identity.framework.context.IdentityMessageContext;
import org.wso2.carbon.identity.framework.handler.IdentityGatewayEventHandler;
import org.wso2.carbon.identity.framework.request.IdentityRequest;

public class SAMLValidationHandler extends IdentityGatewayEventHandler{
    @Override
    public void handle(IdentityMessageContext identityMessageContext) {
        IdentityRequest identityRequest = identityMessageContext.getIdentityRequest();
        String requestURI = identityRequest.getRequestURI();
    }

    @Override
    public boolean canHandle(IdentityMessageContext identityMessageContext) {
        return true;
    }
}
