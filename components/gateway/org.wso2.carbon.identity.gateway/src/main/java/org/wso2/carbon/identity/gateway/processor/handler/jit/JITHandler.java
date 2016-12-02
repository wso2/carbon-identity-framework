package org.wso2.carbon.identity.gateway.processor.handler.jit;


import org.wso2.carbon.identity.framework.context.IdentityMessageContext;
import org.wso2.carbon.identity.framework.response.FrameworkHandlerResponse;
import org.wso2.carbon.identity.gateway.processor.handler.FrameworkHandler;


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
