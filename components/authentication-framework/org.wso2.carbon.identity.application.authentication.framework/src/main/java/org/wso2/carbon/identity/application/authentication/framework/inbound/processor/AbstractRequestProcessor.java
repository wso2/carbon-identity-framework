package org.wso2.carbon.identity.application.authentication.framework.inbound.processor;

import org.wso2.carbon.identity.application.authentication.framework.inbound.FrameworkHandlerStatus;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityMessageContext;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityProcessor;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.response.AbstractResponseHandler;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.HandlerManager;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.AuthenticationHandlerException;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication
        .AuthenticationHandler;
import org.wso2.carbon.identity.base.IdentityException;


public abstract class AbstractRequestProcessor  extends IdentityProcessor {

    protected FrameworkHandlerStatus authenticate(IdentityMessageContext identityMessageContext)
            throws AuthenticationHandlerException {

        FrameworkHandlerStatus frameworkHandlerStatus = null;

        frameworkHandlerStatus = doPreAuthenticate(identityMessageContext);
        if (frameworkHandlerStatus.equals(FrameworkHandlerStatus.CONTINUE)) {

            frameworkHandlerStatus = doAuthenticate(identityMessageContext);
            if (frameworkHandlerStatus.equals(FrameworkHandlerStatus.CONTINUE)) {

                frameworkHandlerStatus = doPostAuthenticate(identityMessageContext);

            }
        }
        return frameworkHandlerStatus;

    }


    protected FrameworkHandlerStatus doPreAuthenticate(IdentityMessageContext identityMessageContext)
            throws AuthenticationHandlerException {
        return HandlerManager.getInstance().doPreAuthenticate(identityMessageContext);
    }

    protected FrameworkHandlerStatus doAuthenticate(IdentityMessageContext identityMessageContext)
            throws AuthenticationHandlerException {
        AuthenticationHandler authenticationHandler =
                HandlerManager.getInstance().getAuthenticationHandler(identityMessageContext);
        return authenticationHandler.doAuthenticate(identityMessageContext);
    }

    protected FrameworkHandlerStatus doPostAuthenticate(IdentityMessageContext identityMessageContext)
            throws AuthenticationHandlerException {
        return HandlerManager.getInstance().doPostAuthenticate(identityMessageContext);
    }


    protected FrameworkHandlerStatus doBuildErrorResponse(IdentityException e,
                                                          IdentityMessageContext
                                                                  identityMessageContext) {
        AbstractResponseHandler responseBuilderHandler =
                HandlerManager.getInstance().getResponseHandler(identityMessageContext);
        return responseBuilderHandler.doBuildErrorResponse(identityMessageContext);
    }

}
