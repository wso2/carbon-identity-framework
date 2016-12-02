package org.wso2.carbon.identity.gateway.handler.response;

import org.wso2.carbon.identity.framework.context.IdentityMessageContext;
import org.wso2.carbon.identity.framework.handler.IdentityGatewayEventHandler;

public class SAMLResponseHandler  extends IdentityGatewayEventHandler {
    @Override
    public void handle(IdentityMessageContext identityMessageContext) {

    }

    @Override
    public boolean canHandle(IdentityMessageContext identityMessageContext) {
        return false;
    }
}
