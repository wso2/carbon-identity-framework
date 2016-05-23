package org.wso2.carbon.identity.application.authentication.framework.processor.handler.request;

import org.wso2.carbon.identity.application.authentication.framework.FrameworkHandlerResponse;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.IdentityMessageContext;
import org.wso2.carbon.identity.application.authentication.framework.processor.handler.FrameworkHandler;

public abstract class AbstractRequestHandler extends FrameworkHandler {
    public abstract FrameworkHandlerResponse validate(AuthenticationContext authenticationContext)
            throws RequestHandlerException;


}
