package org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler;


import org.wso2.carbon.identity.application.authentication.framework.inbound.FrameworkRuntimeException;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityMessageContext;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication
        .AuthenticationHandler;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;

import java.util.List;

public class IdentityFrameworkHandlerManager {
    private static volatile IdentityFrameworkHandlerManager instance = new IdentityFrameworkHandlerManager();

    private IdentityFrameworkHandlerManager() {

    }

    public static IdentityFrameworkHandlerManager getInstance() {
        return instance;
    }

    public AuthenticationHandler getAuthenticationHandler(IdentityMessageContext messageContext){
        List<AuthenticationHandler> authenticationHandlers =
                FrameworkServiceDataHolder.getInstance().getAuthenticationHandlers();
        for (AuthenticationHandler authenticationHandler: authenticationHandlers){
            if(authenticationHandler.canHandle(messageContext)){
                return authenticationHandler ;
            }
        }
        throw FrameworkRuntimeException.error("Cannot find AuthenticationHandler to handle this request");
    }
}
