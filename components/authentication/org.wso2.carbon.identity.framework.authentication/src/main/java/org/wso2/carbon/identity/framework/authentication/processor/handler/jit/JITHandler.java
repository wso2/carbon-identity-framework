package org.wso2.carbon.identity.framework.authentication.processor.handler.jit;


import org.wso2.carbon.identity.application.authentication.framework.FrameworkHandlerResponse;
import org.wso2.carbon.identity.framework.authentication.context.IdentityMessageContext;
import org.wso2.carbon.identity.framework.authentication.processor.handler.FrameworkHandler;


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
