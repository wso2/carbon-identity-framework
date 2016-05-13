package org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication;


import org.wso2.carbon.identity.application.authentication.framework.inbound.FrameworkHandlerResponse;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.FrameworkHandler;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl
        .AuthenticationResponse;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl
        .ContextInitializer;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl
        .SequenceBuildFactory;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl
        .SequenceManager;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl
        .model.Sequence;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl
        .util.HandlerManager;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.bean.context.MessageContext;

public class AuthenticationHandler extends FrameworkHandler {
    @Override
    public String getName() {
        return null;
    }

    public FrameworkHandlerResponse doAuthenticate(AuthenticationContext authenticationContext) throws
                                                                                                AuthenticationHandlerException {
        FrameworkHandlerResponse frameworkHandlerResponse = null;

        HandlerManager handlerManager = HandlerManager.getInstance();

        SequenceBuildFactory sequenceBuildFactory = handlerManager.getSequenceBuildFactory(authenticationContext);
        Sequence sequence = sequenceBuildFactory.buildSequence(authenticationContext);

        ContextInitializer contextInitializerHandler =
                handlerManager.getContextInitializerHandler(authenticationContext);
        contextInitializerHandler.initialize(authenticationContext, sequence);

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
        return null;
    }


}