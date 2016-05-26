package org.wso2.carbon.identity.framework.authentication.processor.handler.authentication.impl
        .util;


import org.wso2.carbon.identity.application.authentication.framework.FrameworkRuntimeException;
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
        List<ContextInitializer> contextInitializers =
                FrameworkServiceDataHolder.getInstance().getContextInitializers();
        for (ContextInitializer contextInitializer : contextInitializers) {
            if (contextInitializer.canHandle(authenticationContext)) {
                return contextInitializer;
            }
        }
        throw FrameworkRuntimeException.error("Cannot find ContextInitializer to handle this request");
    }


    public SequenceManager getSequenceManager(AuthenticationContext authenticationContext) {

        throw FrameworkRuntimeException.error("Cannot find SequenceManager to handle this request");
    }

    public AbstractSequenceBuildFactory getSequenceBuildFactory(AuthenticationContext authenticationContext) {

        throw FrameworkRuntimeException.error("Cannot find AuthenticationContext to handle this request");
    }

    public StepHandler getStepHandler(AuthenticationContext authenticationContext) {

        throw FrameworkRuntimeException.error("Cannot find StepHandler to handle this request");
    }

    public RequestPathHandler getRequestPathHandler(AuthenticationContext authenticationContext) {

        throw FrameworkRuntimeException.error("Cannot find RequestPathHandler to handle this request");
    }

}
