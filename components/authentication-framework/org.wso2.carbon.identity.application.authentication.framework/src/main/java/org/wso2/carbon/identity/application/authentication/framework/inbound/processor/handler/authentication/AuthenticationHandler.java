package org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication;


import org.wso2.carbon.identity.application.authentication.framework.inbound.FrameworkHandlerResponse;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityMessageContext;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.FrameworkHandler;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl
        .AuthenticationResponse;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl
        .HandlerManager;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl.handler.SequenceHandler;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl
        .handler.ContextInitializeHandler;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl.handler.SequenceConfigBuildHandler;


import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.bean.context.MessageContext;

public class AuthenticationHandler extends FrameworkHandler {
    @Override
    public String getName() {
        return null;
    }

    public FrameworkHandlerResponse doAuthenticate(IdentityMessageContext identityMessageContext) throws
                                                                                                AuthenticationHandlerException {
        FrameworkHandlerResponse frameworkHandlerResponse = null ;

        HandlerManager handlerManager = HandlerManager.getInstance();
        try {
            SequenceConfigBuildHandler sequenceBuildHandler = handlerManager.getSequenceBuildHandler(identityMessageContext);
            SequenceConfig sequenceConfig = sequenceBuildHandler.buildSequenceConfig(identityMessageContext);

            ContextInitializeHandler contextInitializerHandler =
                    handlerManager.getContextInitializerHandler(identityMessageContext);
            contextInitializerHandler.initialize(identityMessageContext, sequenceConfig);

            SequenceHandler sequenceHandler =
                    handlerManager.getSequenceProcessHandler(identityMessageContext);

            AuthenticationResponse authenticationResponse =
                    sequenceHandler.handleSequence(identityMessageContext);

            frameworkHandlerResponse = buildFrameworkHandlerResponse(authenticationResponse);

        } catch (Exception e) {
            e.printStackTrace();
        }


        return frameworkHandlerResponse;
    }

    @Override
    public boolean canHandle(MessageContext messageContext) throws IdentityRuntimeException {
        return true;
    }

    private FrameworkHandlerResponse buildFrameworkHandlerResponse(AuthenticationResponse handlerResponse){
        return null ;
    }


}