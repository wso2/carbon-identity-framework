package org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler;


import org.wso2.carbon.identity.application.authentication.framework.inbound.FrameworkHandlerStatus;
import org.wso2.carbon.identity.application.authentication.framework.inbound.FrameworkRuntimeException;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityMessageContext;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication
        .AbstractPostAuthenticationHandler;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication
        .AbstractPreAuthenticationHandler;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.AuthenticationHandlerException;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication
        .AuthenticationHandler;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.request.AbstractRequestHandler;


import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.response
        .AbstractPreResponseHandler;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.response
        .AbstractResponseHandler;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;

import java.util.List;

public class HandlerManager {
    private static volatile HandlerManager instance = new HandlerManager();

    private HandlerManager() {

    }

    public static HandlerManager getInstance() {
        return instance;
    }

    public FrameworkHandlerStatus doPreAuthenticate(IdentityMessageContext messageContext)
            throws AuthenticationHandlerException {
        List<AbstractPreAuthenticationHandler> authenticationHandlers =
                FrameworkServiceDataHolder.getInstance().getPreAuthenticationHandlers();
        for (AbstractPreAuthenticationHandler authenticationHandler: authenticationHandlers){
            if(authenticationHandler.canHandle(messageContext)){
                FrameworkHandlerStatus frameworkHandlerStatus = authenticationHandler.doPreAuthenticate(messageContext);
                if(FrameworkHandlerStatus.REDIRECT.equals(frameworkHandlerStatus)){
                    return frameworkHandlerStatus ;
                }
            }
        }
        return FrameworkHandlerStatus.CONTINUE ;
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


    public FrameworkHandlerStatus doPostAuthenticate(IdentityMessageContext messageContext)
            throws AuthenticationHandlerException {
        List<AbstractPostAuthenticationHandler> authenticationHandlers =
                FrameworkServiceDataHolder.getInstance().getPostAuthenticationHandlers();
        for (AbstractPostAuthenticationHandler authenticationHandler: authenticationHandlers){
            if(authenticationHandler.canHandle(messageContext)){
                FrameworkHandlerStatus frameworkHandlerStatus =
                        authenticationHandler.doPostAuthenticate(messageContext);
                if(FrameworkHandlerStatus.REDIRECT.equals(frameworkHandlerStatus)){
                    return frameworkHandlerStatus ;
                }
            }
        }
        return FrameworkHandlerStatus.CONTINUE ;
    }


    public FrameworkHandlerStatus doPreBuildResponse(IdentityMessageContext messageContext)
            throws AuthenticationHandlerException {
        List<AbstractPreResponseHandler> responseHandlers  =
                FrameworkServiceDataHolder.getInstance().getPreResponseHandlerList();
        for (AbstractPreResponseHandler responseHandler: responseHandlers){
            if(responseHandler.canHandle(messageContext)){
                FrameworkHandlerStatus frameworkHandlerStatus = responseHandler.doPreBuildResponse(messageContext);
                if(FrameworkHandlerStatus.REDIRECT.equals(frameworkHandlerStatus)){
                    return frameworkHandlerStatus ;
                }
            }
        }
        return FrameworkHandlerStatus.CONTINUE ;
    }

    public AbstractResponseHandler getResponseHandler(IdentityMessageContext messageContext){
        List<AbstractResponseHandler> responseBuilderHandlers  =
                FrameworkServiceDataHolder.getInstance().getResponseHandlerList();
        for (AbstractResponseHandler responseBuilderHandler: responseBuilderHandlers){
            if(responseBuilderHandler.canHandle(messageContext)){
                return responseBuilderHandler ;
            }
        }
        throw FrameworkRuntimeException.error("Cannot find AbstractResponseHandler to handle this request");
    }



    public AbstractRequestHandler getProtocolRequestHandler(IdentityMessageContext messageContext){
        List<AbstractRequestHandler> protocolRequestHandlers =
                FrameworkServiceDataHolder.getInstance().getProtocolRequestHandlersList();
        for (AbstractRequestHandler protocolRequestHandler: protocolRequestHandlers){
            if(protocolRequestHandler.canHandle(messageContext)){
                return protocolRequestHandler ;
            }
        }
        throw FrameworkRuntimeException.error("Cannot find AbstractRequestHandler to handle this request");
    }
}
