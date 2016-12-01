package org.wso2.carbon.identity.gateway.authentication.processor.handler.response;


import org.wso2.carbon.identity.gateway.authentication.processor.handler.FrameworkHandler;
import org.wso2.carbon.identity.gateway.framework.context.IdentityMessageContext;
import org.wso2.carbon.identity.gateway.framework.response.FrameworkHandlerResponse;

public abstract class AbstractResponseHandler extends FrameworkHandler {

    public abstract FrameworkHandlerResponse buildErrorResponse(IdentityMessageContext identityMessageContext)
            throws ResponseException;

    public abstract FrameworkHandlerResponse buildResponse(IdentityMessageContext identityMessageContext)
            throws ResponseException;

}
