package org.wso2.carbon.identity.application.authentication.framework.inbound.processor;


import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.inbound.FrameworkHandlerStatus;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityMessageContext;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityRequest;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityResponse;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler
        .FrameworkHandlerException;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.HandlerManager;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication
        .AuthenticationHandlerException;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.extension
        .ExtensionHandlerPoints;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.request
        .AbstractRequestHandler;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.request
        .RequestHandlerException;

public class LoginRequestProcessor extends AbstractRequestProcessor {


    @Override
    public IdentityResponse.IdentityResponseBuilder process(IdentityRequest identityRequest) throws FrameworkException {

        IdentityMessageContext identityMessageContext = null; //read from cache, otherwise throw exception.
        IdentityResponse.IdentityResponseBuilder identityResponseBuilder = null;

        FrameworkHandlerStatus identityFrameworkHandlerStatus = null;
        try {
            identityFrameworkHandlerStatus = validate(identityMessageContext);
            if (identityFrameworkHandlerStatus.equals(FrameworkHandlerStatus.CONTINUE)) {
                identityFrameworkHandlerStatus = authenticate(identityMessageContext);
                if (identityFrameworkHandlerStatus.equals(FrameworkHandlerStatus.CONTINUE)) {

                }
            }
        } catch (AuthenticationHandlerException e) {
            identityFrameworkHandlerStatus = doBuildErrorResponse(e, identityMessageContext);
        }
        return identityFrameworkHandlerStatus.getIdentityResponseBuilder();
    }

    protected FrameworkHandlerStatus doValidate(IdentityMessageContext identityMessageContext)
            throws AuthenticationHandlerException, RequestHandlerException {
        AbstractRequestHandler protocolRequestHandler =
                HandlerManager.getInstance().getProtocolRequestHandler(identityMessageContext);
        return protocolRequestHandler.validate(identityMessageContext);
    }

    protected FrameworkHandlerStatus validate(IdentityMessageContext identityMessageContext)
            throws FrameworkHandlerException {

        FrameworkHandlerStatus frameworkHandlerStatus = null;

        frameworkHandlerStatus = doPostHandle(ExtensionHandlerPoints.REQUEST_HANDLER, identityMessageContext);
        if (frameworkHandlerStatus.equals(FrameworkHandlerStatus.CONTINUE)) {

            frameworkHandlerStatus = doValidate(identityMessageContext);
            if (frameworkHandlerStatus.equals(FrameworkHandlerStatus.CONTINUE)) {

                frameworkHandlerStatus = doPostHandle(ExtensionHandlerPoints
                                                              .REQUEST_HANDLER, identityMessageContext);

            }
        }
        return frameworkHandlerStatus;

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
