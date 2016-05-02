package org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler;


import org.wso2.carbon.identity.application.authentication.framework.inbound.FrameworkRuntimeException;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityMessageContext;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication
        .AuthenticationHandler;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;

import java.util.List;

public class HandlerManager {
    private static volatile HandlerManager instance = new HandlerManager();

    private HandlerManager() {

    }

    public static HandlerManager getInstance() {
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

    public ResponseHandler getResponseBuilderHandler(IdentityMessageContext messageContext){
        List<ResponseHandler> responseBuilderHandlers  =
                FrameworkServiceDataHolder.getInstance().getResponseBuilderHandlerList();
        for (ResponseHandler responseBuilderHandler: responseBuilderHandlers){
            if(responseBuilderHandler.canHandle(messageContext)){
                return responseBuilderHandler ;
            }
        }
        throw FrameworkRuntimeException.error("Cannot find ResponseBuilderHandler to handle this request");
    }


    public AbstractProtocolRequestHandler getProtocolRequestHandler(IdentityMessageContext messageContext){
        List<AbstractProtocolRequestHandler> protocolRequestHandlers =
                FrameworkServiceDataHolder.getInstance().getProtocolRequestHandlersList();
        for (AbstractProtocolRequestHandler protocolRequestHandler: protocolRequestHandlers){
            if(protocolRequestHandler.canHandle(messageContext)){
                return protocolRequestHandler ;
            }
        }
        throw FrameworkRuntimeException.error("Cannot find AbstractProtocolRequestHandler to handle this request");
    }
}
