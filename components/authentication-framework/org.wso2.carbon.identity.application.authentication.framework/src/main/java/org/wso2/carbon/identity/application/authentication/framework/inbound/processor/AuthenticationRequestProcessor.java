package org.wso2.carbon.identity.application.authentication.framework.inbound.processor;


import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.inbound.FrameworkHandlerStatus;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityMessageContext;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityProcessor;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityRequest;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityResponse;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.HandlerManager;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.AuthenticationHandlerException;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication
        .AuthenticationHandler;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.response
        .AbstractResponseHandler;
import org.wso2.carbon.identity.base.IdentityException;

public class AuthenticationRequestProcessor extends IdentityProcessor {

    @Override
    public IdentityResponse.IdentityResponseBuilder process(IdentityRequest identityRequest) throws FrameworkException {

        IdentityMessageContext identityMessageContext = null; //read from cache, otherwise throw exception.

        FrameworkHandlerStatus frameworkHandlerStatus = null;
        try {
            frameworkHandlerStatus = doPreAuthenticate(identityMessageContext);
            if (frameworkHandlerStatus.equals(FrameworkHandlerStatus.CONTINUE)) {

                frameworkHandlerStatus = doAuthenticate(identityMessageContext);
                if (frameworkHandlerStatus.equals(FrameworkHandlerStatus.CONTINUE)) {

                    frameworkHandlerStatus = doPostAuthenticate(identityMessageContext);
                    if (frameworkHandlerStatus.equals(FrameworkHandlerStatus.CONTINUE)) {

                        frameworkHandlerStatus = doPreBuildResponse(identityMessageContext);
                        if (frameworkHandlerStatus.equals(FrameworkHandlerStatus.CONTINUE)) {

                            frameworkHandlerStatus = doBuildResponse(identityMessageContext);
                        }

                    }
                }
            }

        } catch (AuthenticationHandlerException e) {
            frameworkHandlerStatus = doBuildErrorResponse(e, identityMessageContext);
        }
        return frameworkHandlerStatus.getIdentityResponseBuilder();
    }

    private FrameworkHandlerStatus doPreBuildResponse(IdentityMessageContext identityMessageContext)
            throws AuthenticationHandlerException {
        return HandlerManager.getInstance().doPreBuildResponse(identityMessageContext);
    }

    protected FrameworkHandlerStatus doBuildResponse(IdentityMessageContext identityMessageContext) {
        AbstractResponseHandler responseBuilderHandler =
                HandlerManager.getInstance().getResponseHandler(identityMessageContext);
        return responseBuilderHandler.doBuildResponse(identityMessageContext);
    }

    private FrameworkHandlerStatus doPreAuthenticate(IdentityMessageContext identityMessageContext)
            throws AuthenticationHandlerException {
        return HandlerManager.getInstance().doPreAuthenticate(identityMessageContext);
    }

    protected FrameworkHandlerStatus doAuthenticate(IdentityMessageContext identityMessageContext)
            throws AuthenticationHandlerException {
        AuthenticationHandler authenticationHandler =
                HandlerManager.getInstance().getAuthenticationHandler(identityMessageContext);
        return authenticationHandler.doAuthenticate(identityMessageContext);
    }

    private FrameworkHandlerStatus doPostAuthenticate(IdentityMessageContext identityMessageContext)
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


    @Override
    public String getName() {
        return null;
    }


    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public boolean canHandle(IdentityRequest identityRequest) {
        return false;
    }
}
