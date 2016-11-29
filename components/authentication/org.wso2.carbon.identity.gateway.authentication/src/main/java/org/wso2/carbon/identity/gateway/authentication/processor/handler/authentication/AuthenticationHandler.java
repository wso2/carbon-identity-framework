package org.wso2.carbon.identity.gateway.authentication.processor.handler.authentication;


import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.bean.context.MessageContext;
import org.wso2.carbon.identity.gateway.authentication.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.authentication.processor.handler.FrameworkHandler;
import org.wso2.carbon.identity.gateway.authentication.processor.handler.authentication.impl.AbstractSequenceBuildFactory;
import org.wso2.carbon.identity.gateway.authentication.processor.handler.authentication.impl.AuthenticationResponse;
import org.wso2.carbon.identity.gateway.authentication.processor.handler.authentication.impl.SequenceManager;
import org.wso2.carbon.identity.gateway.authentication.processor.handler.authentication.impl.model.AbstractSequence;
import org.wso2.carbon.identity.gateway.authentication.processor.handler.authentication.impl.util.HandlerManager;
import org.wso2.carbon.identity.gateway.framework.response.FrameworkHandlerResponse;

public class AuthenticationHandler extends FrameworkHandler {
    @Override
    public String getName() {
        return getClass().getName();
    }

    public FrameworkHandlerResponse doAuthenticate(AuthenticationContext authenticationContext)
            throws AuthenticationHandlerException {


        FrameworkHandlerResponse frameworkHandlerResponse = null;

        HandlerManager handlerManager = HandlerManager.getInstance();

        AbstractSequenceBuildFactory abstractSequenceBuildFactory =
                handlerManager.getSequenceBuildFactory(authenticationContext);
        AbstractSequence abstractSequence = abstractSequenceBuildFactory.buildSequence(authenticationContext);

        authenticationContext.setSequence(abstractSequence);

        /*
        ContextInitializer contextInitializerHandler =
                handlerManager.getContextInitializerHandler(authenticationContext);
        contextInitializerHandler.initialize(authenticationContext);
        */
        SequenceManager sequenceManager =
                handlerManager.getSequenceManager(authenticationContext);

        AuthenticationResponse authenticationResponse =
                sequenceManager.handleSequence(authenticationContext);

        frameworkHandlerResponse = buildFrameworkHandlerResponse(authenticationResponse);


        return frameworkHandlerResponse;
    }

    @Override
    public boolean canHandle(MessageContext messageContext) throws IdentityRuntimeException {
        return true;
    }

    private FrameworkHandlerResponse buildFrameworkHandlerResponse(AuthenticationResponse handlerResponse) {
        FrameworkHandlerResponse frameworkHandlerResponse = null;
        if (AuthenticationResponse.AUTHENTICATED.equals(handlerResponse)) {
            frameworkHandlerResponse = FrameworkHandlerResponse.CONTINUE;
        } else if (AuthenticationResponse.INCOMPLETE.equals(handlerResponse)) {
            frameworkHandlerResponse = FrameworkHandlerResponse.REDIRECT;
            frameworkHandlerResponse.setIdentityResponseBuilder(handlerResponse.getIdentityResponseBuilder());
        }
        return frameworkHandlerResponse;
    }


}