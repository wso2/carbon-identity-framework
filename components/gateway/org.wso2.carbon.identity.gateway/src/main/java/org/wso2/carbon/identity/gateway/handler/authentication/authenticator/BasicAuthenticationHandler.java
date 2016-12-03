package org.wso2.carbon.identity.gateway.handler.authentication.authenticator;


import org.wso2.carbon.identity.framework.context.IdentityMessageContext;
import org.wso2.carbon.identity.framework.handler.GatewayEventHandler;
import org.wso2.carbon.identity.framework.handler.GatewayInvocationResponse;

public class BasicAuthenticationHandler extends GatewayEventHandler {

    @Override
    public GatewayInvocationResponse handle(IdentityMessageContext identityMessageContext) {
        return GatewayInvocationResponse.SUSPEND ;
    }

    @Override
    public boolean canHandle(IdentityMessageContext identityMessageContext) {
        return true;
    }
}
