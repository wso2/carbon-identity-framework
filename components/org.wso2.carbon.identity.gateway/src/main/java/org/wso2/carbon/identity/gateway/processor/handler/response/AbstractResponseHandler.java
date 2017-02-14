package org.wso2.carbon.identity.gateway.processor.handler.response;


import org.wso2.carbon.identity.common.base.exception.IdentityException;
import org.wso2.carbon.identity.gateway.api.FrameworkHandlerResponse;
import org.wso2.carbon.identity.gateway.api.IdentityMessageContext;
import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.context.SequenceContext;
import org.wso2.carbon.identity.gateway.model.User;
import org.wso2.carbon.identity.gateway.processor.handler.FrameworkHandler;

public abstract class AbstractResponseHandler extends FrameworkHandler {

    public abstract FrameworkHandlerResponse buildErrorResponse(AuthenticationContext authenticationContext,IdentityException identityException)
            throws ResponseException;

    public abstract FrameworkHandlerResponse buildResponse(AuthenticationContext authenticationContext)
            throws ResponseException;

}
