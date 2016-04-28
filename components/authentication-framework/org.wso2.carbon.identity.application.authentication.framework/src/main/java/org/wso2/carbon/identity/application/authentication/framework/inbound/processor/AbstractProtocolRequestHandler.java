package org.wso2.carbon.identity.application.authentication.framework.inbound.processor;

import org.wso2.carbon.identity.application.authentication.framework.inbound.FrameworkHandler;
import org.wso2.carbon.identity.application.authentication.framework.inbound.FrameworkHandlerStatus;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityMessageContext;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityResponse;

public abstract class AbstractProtocolRequestHandler extends FrameworkHandler{
    public abstract FrameworkHandlerStatus validate(IdentityMessageContext identityMessageContext);
}
