package org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.util;


import org.wso2.carbon.identity.core.handler.AbstractIdentityMessageHandler;
import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.framework.exception.FrameworkRuntimeException;
import org.wso2.carbon.identity.gateway.internal.GatewayDataHolder;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.AbstractSequenceBuildFactory;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.ContextInitializer;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.RequestPathHandler;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.SequenceManager;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.StepHandler;

import java.util.List;

public class HandlerManager {

    private static volatile HandlerManager instance = new HandlerManager();

    private HandlerManager() {

    }

    public static HandlerManager getInstance() {
        return instance;
    }

    public ContextInitializer getContextInitializerHandler(AuthenticationContext authenticationContext) {
        return (ContextInitializer) getHandler(GatewayDataHolder.getInstance().getContextInitializers(), authenticationContext);
    }


    public SequenceManager getSequenceManager(AuthenticationContext authenticationContext) {
        return (SequenceManager) getHandler(GatewayDataHolder.getInstance().getSequenceManagers(), authenticationContext);
    }

    public AbstractSequenceBuildFactory getSequenceBuildFactory(AuthenticationContext authenticationContext) {
        return (AbstractSequenceBuildFactory) getHandler(GatewayDataHolder.getInstance().getSequenceBuildFactories(), authenticationContext);
    }

    public StepHandler getStepHandler(AuthenticationContext authenticationContext) {
        return (StepHandler) getHandler(GatewayDataHolder.getInstance().getStepHandlers(), authenticationContext);
    }

    public RequestPathHandler getRequestPathHandler(AuthenticationContext authenticationContext) {
        return (RequestPathHandler) getHandler(GatewayDataHolder.getInstance().getRequestPathHandlers(), authenticationContext);
    }


    private AbstractIdentityMessageHandler getHandler(List<? extends AbstractIdentityMessageHandler> abstractIdentityHandlers, AuthenticationContext authenticationContext) {
        if (abstractIdentityHandlers != null) {
            for (AbstractIdentityMessageHandler abstractIdentityHandler : abstractIdentityHandlers) {
                if (abstractIdentityHandler.canHandle(authenticationContext)) {
                    return abstractIdentityHandler;
                }
            }
        }
        throw FrameworkRuntimeException.error("Cannot find a Handler to handle this request");
    }

}
