package org.wso2.carbon.identity.gateway.processor.handler.response;


import org.wso2.carbon.identity.framework.context.IdentityMessageContext;
import org.wso2.carbon.identity.framework.response.FrameworkHandlerResponse;
import org.wso2.carbon.identity.gateway.processor.handler.FrameworkHandler;

public abstract class AbstractResponseHandler extends FrameworkHandler {

    public abstract FrameworkHandlerResponse buildErrorResponse(IdentityMessageContext identityMessageContext)
            throws ResponseException;

    public abstract FrameworkHandlerResponse buildResponse(IdentityMessageContext identityMessageContext)
            throws ResponseException;

}
