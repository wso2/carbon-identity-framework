package org.wso2.carbon.identity.framework.authentication.processor.handler.request;

import org.wso2.carbon.identity.framework.authentication.context.AuthenticationContext;
import org.wso2.carbon.identity.framework.authentication.processor.handler.FrameworkHandler;
import org.wso2.carbon.identity.gateway.response.FrameworkHandlerResponse;

public abstract class AbstractRequestHandler extends FrameworkHandler {
    public abstract FrameworkHandlerResponse validate(AuthenticationContext authenticationContext)
            throws RequestHandlerException;


}
