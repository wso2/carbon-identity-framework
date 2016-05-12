package org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl;


import org.wso2.carbon.identity.application.authentication.framework.inbound.FrameworkRuntimeException;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityMessageContext;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl.handler.SequenceHandler;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl
        .handler.ContextInitializeHandler;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl.handler.RequestPathAuthenticationHandler;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl.handler.SequenceConfigBuildHandler;



import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl.handler.StepAuthenticationHandler;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;

import java.util.List;

public class HandlerManager {

    private static volatile HandlerManager instance = new HandlerManager();

    private HandlerManager() {

    }

    public static HandlerManager getInstance() {
        return instance;
    }

    public ContextInitializeHandler getContextInitializerHandler(IdentityMessageContext identityMessageContext){
        List<ContextInitializeHandler> contextInitializeHandlers =
                FrameworkServiceDataHolder.getInstance().getContextInitializeHandlers();
        for (ContextInitializeHandler contextInitializeHandler: contextInitializeHandlers){
            if(contextInitializeHandler.canHandle(identityMessageContext)){
                return contextInitializeHandler ;
            }
        }
        throw FrameworkRuntimeException.error("Cannot find ContextInitializeHandler to handle this request");
    }

    public RequestPathAuthenticationHandler getRequestPathProcessHandler(IdentityMessageContext identityMessageContext){

        throw FrameworkRuntimeException.error("Cannot find RequestPathAuthenticationHandler to handle this request");
    }

    public SequenceConfigBuildHandler getSequenceBuildHandler(IdentityMessageContext identityMessageContext){

        throw FrameworkRuntimeException.error("Cannot find SequenceConfigBuildHandler to handle this request");
    }

    public SequenceHandler getSequenceProcessHandler(IdentityMessageContext identityMessageContext){

        throw FrameworkRuntimeException.error("Cannot find SequenceHandler to handle this request");
    }

    public StepAuthenticationHandler getStepProcessHandler(IdentityMessageContext identityMessageContext){

        throw FrameworkRuntimeException.error("Cannot find StepAuthenticationHandler to handle this request");
    }

}
