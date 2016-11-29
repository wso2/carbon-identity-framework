package org.wso2.carbon.identity.gateway.authentication.processor.handler.jit;


import org.wso2.carbon.identity.gateway.authentication.processor.handler.FrameworkHandler;
import org.wso2.carbon.identity.gateway.framework.context.IdentityMessageContext;
import org.wso2.carbon.identity.gateway.framework.response.FrameworkHandlerResponse;


public class JITHandler extends FrameworkHandler {

    @Override
    public String getName() {
        return null;
    }

    protected FrameworkHandlerResponse provision(IdentityMessageContext identityMessageContext)
            throws JITHandlerException {
        return null;
    }


}
