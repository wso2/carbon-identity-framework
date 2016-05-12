package org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.request;

import org.wso2.carbon.identity.application.authentication.framework.inbound.FrameworkHandlerResponse;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityMessageContext;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.FrameworkHandler;

public abstract class AbstractRequestHandler extends FrameworkHandler {
    public abstract FrameworkHandlerResponse validate(IdentityMessageContext identityMessageContext)
            throws RequestHandlerException;


}
