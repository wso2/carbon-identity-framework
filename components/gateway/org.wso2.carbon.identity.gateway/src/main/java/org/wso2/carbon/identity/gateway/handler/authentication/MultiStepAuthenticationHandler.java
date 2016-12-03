package org.wso2.carbon.identity.gateway.handler.authentication;

import org.wso2.carbon.identity.framework.context.IdentityMessageContext;
import org.wso2.carbon.identity.framework.handler.GatewayEventHandler;
import org.wso2.carbon.identity.framework.handler.GatewayInvocationResponse;

import java.util.ArrayList;
import java.util.List;


public class MultiStepAuthenticationHandler extends GatewayEventHandler {

    private List<GatewayEventHandler> gatewayEventHandlers = new ArrayList<>();


    public void addIdentityGatewayEventHandler(GatewayEventHandler gatewayEventHandler){
        this.gatewayEventHandlers.add(gatewayEventHandler);
    }

    @Override
    public GatewayInvocationResponse handle(IdentityMessageContext identityMessageContext) {
        for (GatewayEventHandler gatewayEventHandler : gatewayEventHandlers){
            if(gatewayEventHandler.canHandle(identityMessageContext)){
                setNextHandler(gatewayEventHandler);
                break;
            }
        }
        return GatewayInvocationResponse.CONTINUE ;
    }

    @Override
    public boolean canHandle(IdentityMessageContext identityMessageContext) {
        return true;
    }
}
