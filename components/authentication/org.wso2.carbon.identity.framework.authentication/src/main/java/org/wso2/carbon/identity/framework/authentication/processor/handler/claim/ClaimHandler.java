package org.wso2.carbon.identity.framework.authentication.processor.handler.claim;


import org.wso2.carbon.identity.framework.authentication.processor.handler.FrameworkHandler;
import org.wso2.carbon.identity.gateway.context.IdentityMessageContext;
import org.wso2.carbon.identity.gateway.response.FrameworkHandlerResponse;

public class ClaimHandler extends FrameworkHandler {
    @Override
    public String getName() {
        return null;
    }

    protected FrameworkHandlerResponse handleClaims(IdentityMessageContext identityMessageContext)
            throws ClaimHandlerException {
        return null;
    }

}
