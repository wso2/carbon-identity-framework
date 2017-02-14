package org.wso2.carbon.identity.gateway.processor.util;


import org.wso2.carbon.identity.gateway.api.FrameworkHandlerResponse;
import org.wso2.carbon.identity.gateway.api.FrameworkRuntimeException;
import org.wso2.carbon.identity.gateway.api.IdentityMessageContext;
import org.wso2.carbon.identity.gateway.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.gateway.processor.handler.FrameworkHandlerException;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.AuthenticationHandler;
import org.wso2.carbon.identity.gateway.processor.handler.request.AbstractRequestHandler;
import org.wso2.carbon.identity.gateway.processor.handler.response.AbstractResponseHandler;

import java.util.List;

public class HandlerManager {

    private static volatile HandlerManager instance = new HandlerManager();

    private HandlerManager() {

    }

    public static HandlerManager getInstance() {
        return instance;
    }


    public AuthenticationHandler getAuthenticationHandler(IdentityMessageContext messageContext) {
        List<AuthenticationHandler> authenticationHandlers =
                FrameworkServiceDataHolder.getInstance().getAuthenticationHandlers();
        if(authenticationHandlers != null) {
            for (AuthenticationHandler authenticationHandler : authenticationHandlers) {
                if (authenticationHandler.canHandle(messageContext)) {
                    return authenticationHandler;
                }
            }
        }
        throw new FrameworkRuntimeException("Cannot find AuthenticationHandler to handle this request.");
    }


    public AbstractResponseHandler getResponseHandler(IdentityMessageContext messageContext) {
        List<AbstractResponseHandler> responseBuilderHandlers =
                FrameworkServiceDataHolder.getInstance().getResponseHandlers();
        if(responseBuilderHandlers != null) {
            for (AbstractResponseHandler responseBuilderHandler : responseBuilderHandlers) {
                if (responseBuilderHandler.canHandle(messageContext)) {
                    return responseBuilderHandler;
                }
            }
        }
        throw new FrameworkRuntimeException("Cannot find AbstractResponseHandler to handle this request.");
    }


    public AbstractRequestHandler getProtocolRequestHandler(IdentityMessageContext messageContext) {
        List<AbstractRequestHandler> protocolRequestHandlers =
                FrameworkServiceDataHolder.getInstance().getRequestHandlers();
        if(protocolRequestHandlers != null) {
            for (AbstractRequestHandler protocolRequestHandler : protocolRequestHandlers) {
                if (protocolRequestHandler.canHandle(messageContext)) {
                    return protocolRequestHandler;
                }
            }
        }
        throw new FrameworkRuntimeException("Cannot find AbstractRequestHandler to handle this request.");
    }
}
