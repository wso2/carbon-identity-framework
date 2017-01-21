package org.wso2.carbon.identity.gateway.processor.util;


import org.wso2.carbon.identity.gateway.api.FrameworkHandlerResponse;
import org.wso2.carbon.identity.gateway.api.FrameworkRuntimeException;
import org.wso2.carbon.identity.gateway.api.IdentityMessageContext;
import org.wso2.carbon.identity.gateway.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.gateway.processor.handler.FrameworkHandlerException;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.AuthenticationHandler;
import org.wso2.carbon.identity.gateway.processor.handler.extension.AbstractPostHandler;
import org.wso2.carbon.identity.gateway.processor.handler.extension.AbstractPreHandler;
import org.wso2.carbon.identity.gateway.processor.handler.extension.ExtensionHandlerPoints;
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

    public FrameworkHandlerResponse doPreHandle(ExtensionHandlerPoints extensionHandlerPoint,
                                                IdentityMessageContext identityMessageContext)
            throws FrameworkHandlerException {
        List<AbstractPreHandler> abstractPreHandlers =
                FrameworkServiceDataHolder.getInstance().getPreHandler().get(extensionHandlerPoint);
        if(abstractPreHandlers != null) {
            for (AbstractPreHandler abstractPreHandler : abstractPreHandlers) {
                if (abstractPreHandler.canHandle(identityMessageContext)) {
                    FrameworkHandlerResponse handlerStatus = abstractPreHandler.handle(identityMessageContext);
                    if (FrameworkHandlerResponse.REDIRECT.equals(handlerStatus)) {
                        return handlerStatus;
                    }
                }
            }
        }
        return FrameworkHandlerResponse.CONTINUE;
    }

    public FrameworkHandlerResponse doPostHandle(ExtensionHandlerPoints extensionHandlerPoint,
                                                 IdentityMessageContext identityMessageContext)
            throws FrameworkHandlerException {
        List<AbstractPostHandler> abstractPostHandlers =
                FrameworkServiceDataHolder.getInstance().getPostHandler().get(extensionHandlerPoint);
        if(abstractPostHandlers != null) {
            for (AbstractPostHandler abstractPostHandler : abstractPostHandlers) {
                if (abstractPostHandler.canHandle(identityMessageContext)) {
                    FrameworkHandlerResponse handlerStatus = abstractPostHandler.handle(identityMessageContext);
                    if (FrameworkHandlerResponse.REDIRECT.equals(handlerStatus)) {
                        return handlerStatus;
                    }
                }
            }
        }
        return FrameworkHandlerResponse.CONTINUE;
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
        throw FrameworkRuntimeException.error("Cannot find AuthenticationHandler to handle this request");
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
        throw FrameworkRuntimeException.error("Cannot find AbstractResponseHandler to handle this request");
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
        throw FrameworkRuntimeException.error("Cannot find AbstractRequestHandler to handle this request");
    }
}
