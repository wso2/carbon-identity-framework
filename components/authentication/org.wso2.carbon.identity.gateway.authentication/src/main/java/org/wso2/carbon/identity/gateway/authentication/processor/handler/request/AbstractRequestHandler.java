package org.wso2.carbon.identity.gateway.authentication.processor.handler.request;

import org.wso2.carbon.identity.gateway.authentication.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.authentication.processor.handler.FrameworkHandler;
import org.wso2.carbon.identity.gateway.framework.response.FrameworkHandlerResponse;

public abstract class AbstractRequestHandler extends FrameworkHandler {
    public abstract FrameworkHandlerResponse validate(AuthenticationContext authenticationContext)
            throws RequestHandlerException;


}
