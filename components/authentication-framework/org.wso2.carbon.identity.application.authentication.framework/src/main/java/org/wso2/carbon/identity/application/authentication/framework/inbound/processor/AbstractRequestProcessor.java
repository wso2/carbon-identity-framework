package org.wso2.carbon.identity.application.authentication.framework.inbound.processor;

import org.wso2.carbon.identity.application.authentication.framework.inbound.FrameworkHandlerStatus;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityMessageContext;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityProcessor;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler
        .FrameworkHandlerException;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.HandlerManager;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication
        .AuthenticationHandler;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication
        .AuthenticationHandlerException;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.extension
        .ExtensionHandlerPoints;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.response
        .AbstractResponseHandler;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.response
        .ResponseException;
import org.wso2.carbon.identity.base.IdentityException;


public abstract class AbstractRequestProcessor extends IdentityProcessor {


    protected FrameworkHandlerStatus authenticate(IdentityMessageContext identityMessageContext)
            throws FrameworkHandlerException {

        FrameworkHandlerStatus frameworkHandlerStatus = null;

        frameworkHandlerStatus = doPreHandle(ExtensionHandlerPoints.AUTHENTICATION_HANDLER, identityMessageContext);
        if (frameworkHandlerStatus.equals(FrameworkHandlerStatus.CONTINUE)) {

            frameworkHandlerStatus = doAuthenticate(identityMessageContext);
            if (frameworkHandlerStatus.equals(FrameworkHandlerStatus.CONTINUE)) {

                frameworkHandlerStatus = doPostHandle(ExtensionHandlerPoints
                                                              .AUTHENTICATION_HANDLER, identityMessageContext);

            }
        }
        return frameworkHandlerStatus;

    }


    protected FrameworkHandlerStatus doAuthenticate(IdentityMessageContext identityMessageContext)
            throws AuthenticationHandlerException {
        AuthenticationHandler authenticationHandler =
                HandlerManager.getInstance().getAuthenticationHandler(identityMessageContext);
        return authenticationHandler.doAuthenticate(identityMessageContext);
    }


    protected FrameworkHandlerStatus doBuildErrorResponse(IdentityException e,
                                                          IdentityMessageContext
                                                                  identityMessageContext) throws ResponseException {
        AbstractResponseHandler responseBuilderHandler =
                HandlerManager.getInstance().getResponseHandler(identityMessageContext);
        return responseBuilderHandler.buildErrorResponse(identityMessageContext);
    }


    protected FrameworkHandlerStatus doPreHandle(ExtensionHandlerPoints extensionHandlerPoints,
                                                 IdentityMessageContext identityMessageContext)

            throws FrameworkHandlerException {
        return HandlerManager.getInstance().doPreHandle(extensionHandlerPoints, identityMessageContext);
    }

    protected FrameworkHandlerStatus doPostHandle(ExtensionHandlerPoints extensionHandlerPoints,
                                                  IdentityMessageContext identityMessageContext)

            throws FrameworkHandlerException {
        return HandlerManager.getInstance().doPostHandle(extensionHandlerPoints, identityMessageContext);
    }

}
