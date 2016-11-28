package org.wso2.carbon.identity.framework.authentication.processor.handler.jit;


import org.wso2.carbon.identity.framework.authentication.processor.handler.FrameworkHandler;
import org.wso2.carbon.identity.gateway.context.IdentityMessageContext;
import org.wso2.carbon.identity.gateway.response.FrameworkHandlerResponse;


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
