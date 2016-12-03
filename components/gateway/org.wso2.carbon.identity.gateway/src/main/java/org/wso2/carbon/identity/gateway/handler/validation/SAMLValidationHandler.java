package org.wso2.carbon.identity.gateway.handler.validation;

import org.wso2.carbon.identity.framework.context.IdentityMessageContext;
import org.wso2.carbon.identity.framework.handler.GatewayEventHandler;
import org.wso2.carbon.identity.framework.handler.GatewayInvocationResponse;
import org.wso2.carbon.identity.framework.request.IdentityRequest;

public class SAMLValidationHandler extends GatewayEventHandler {
    @Override
    public GatewayInvocationResponse handle(IdentityMessageContext identityMessageContext) {
        IdentityRequest identityRequest = identityMessageContext.getIdentityRequest();
        String requestURI = identityRequest.getRequestURI();
        return GatewayInvocationResponse.CONTINUE ;
    }

    @Override
    public boolean canHandle(IdentityMessageContext identityMessageContext) {
        return true;
    }
}
