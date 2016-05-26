package org.wso2.carbon.identity.framework.authentication.processor.handler.response;


import org.wso2.carbon.identity.framework.FrameworkHandlerResponse;
import org.wso2.carbon.identity.framework.IdentityMessageContext;
import org.wso2.carbon.identity.framework.authentication.processor.handler.FrameworkHandler;

public abstract class AbstractResponseHandler extends FrameworkHandler {

    public abstract FrameworkHandlerResponse buildErrorResponse(IdentityMessageContext identityMessageContext)
            throws ResponseException;

    public abstract FrameworkHandlerResponse buildResponse(IdentityMessageContext identityMessageContext)
            throws ResponseException;

}
