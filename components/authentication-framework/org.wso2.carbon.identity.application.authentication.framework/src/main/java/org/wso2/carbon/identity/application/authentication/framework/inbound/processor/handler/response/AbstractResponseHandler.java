package org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.response;


import org.wso2.carbon.identity.application.authentication.framework.inbound.FrameworkHandler;
import org.wso2.carbon.identity.application.authentication.framework.inbound.FrameworkHandlerStatus;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityMessageContext;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityResponse;

public abstract class AbstractResponseHandler extends FrameworkHandler {

    public abstract FrameworkHandlerStatus doBuildErrorResponse(IdentityMessageContext identityMessageContext);
    public abstract FrameworkHandlerStatus doBuildResponse(IdentityMessageContext identityMessageContext);
}
