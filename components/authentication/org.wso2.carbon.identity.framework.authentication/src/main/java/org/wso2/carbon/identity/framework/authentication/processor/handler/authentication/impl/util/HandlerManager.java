package org.wso2.carbon.identity.framework.authentication.processor.handler.authentication.impl
        .util;


import org.wso2.carbon.identity.core.handler.AbstractIdentityHandler;
import org.wso2.carbon.identity.framework.FrameworkRuntimeException;
import org.wso2.carbon.identity.framework.authentication.context.AuthenticationContext;
import org.wso2.carbon.identity.framework.authentication.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.framework.authentication.processor.handler.authentication.impl
        .AbstractSequenceBuildFactory;
import org.wso2.carbon.identity.framework.authentication.processor.handler.authentication.impl.ContextInitializer;
import org.wso2.carbon.identity.framework.authentication.processor.handler.authentication.impl.RequestPathHandler;
import org.wso2.carbon.identity.framework.authentication.processor.handler.authentication.impl.SequenceManager;
import org.wso2.carbon.identity.framework.authentication.processor.handler.authentication.impl.StepHandler;

import java.util.List;

public class HandlerManager {

    private static volatile HandlerManager instance = new HandlerManager();

    private HandlerManager() {

    }

    public static HandlerManager getInstance() {
        return instance;
    }

    public ContextInitializer getContextInitializerHandler(AuthenticationContext authenticationContext) {
        return (ContextInitializer)getHandler(FrameworkServiceDataHolder.getInstance().getContextInitializers(), authenticationContext);
    }


    public SequenceManager getSequenceManager(AuthenticationContext authenticationContext) {
        return (SequenceManager)getHandler(FrameworkServiceDataHolder.getInstance().getSequenceManagers(), authenticationContext);
    }

    public AbstractSequenceBuildFactory getSequenceBuildFactory(AuthenticationContext authenticationContext) {
        return (AbstractSequenceBuildFactory)getHandler(FrameworkServiceDataHolder.getInstance().getSequenceBuildFactories(), authenticationContext);
    }

    public StepHandler getStepHandler(AuthenticationContext authenticationContext) {
        return (StepHandler)getHandler(FrameworkServiceDataHolder.getInstance().getStepHandlers(), authenticationContext);
    }

    public RequestPathHandler getRequestPathHandler(AuthenticationContext authenticationContext) {
        return (RequestPathHandler)getHandler(FrameworkServiceDataHolder.getInstance().getRequestPathHandlers(), authenticationContext);
    }


    private AbstractIdentityHandler getHandler(List<? extends AbstractIdentityHandler > abstractIdentityHandlers, AuthenticationContext authenticationContext){
        if(abstractIdentityHandlers != null){
            for(AbstractIdentityHandler abstractIdentityHandler: abstractIdentityHandlers){
                if(abstractIdentityHandler.canHandle(authenticationContext)){
                    return abstractIdentityHandler ;
                }
            }
        }
        throw FrameworkRuntimeException.error("Cannot find a Handler to handle this request");
    }

}
