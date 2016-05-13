package org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl.util;


import org.wso2.carbon.identity.application.authentication.framework.inbound.FrameworkRuntimeException;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityMessageContext;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.context.AuthenticationContext;


import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl
        .ContextInitializer;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl
        .SequenceBuildFactory;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl.SequenceManager;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;

import java.util.List;

public class HandlerManager {

    private static volatile HandlerManager instance = new HandlerManager();

    private HandlerManager() {

    }

    public static HandlerManager getInstance() {
        return instance;
    }

    public ContextInitializer getContextInitializerHandler(AuthenticationContext authenticationContext){
        List<ContextInitializer> contextInitializers =
                FrameworkServiceDataHolder.getInstance().getContextInitializers();
        for (ContextInitializer contextInitializer : contextInitializers){
            if(contextInitializer.canHandle(authenticationContext)){
                return contextInitializer;
            }
        }
        throw FrameworkRuntimeException.error("Cannot find ContextInitializer to handle this request");
    }


    public SequenceManager getSequenceManager(AuthenticationContext authenticationContext){

        throw FrameworkRuntimeException.error("Cannot find SequenceManager to handle this request");
    }

    public SequenceBuildFactory getSequenceBuildFactory(AuthenticationContext authenticationContext){

        throw FrameworkRuntimeException.error("Cannot find AuthenticationContext to handle this request");
    }



}
