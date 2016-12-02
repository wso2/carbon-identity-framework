package org.wso2.carbon.identity.gateway.handler.authentication;

import org.wso2.carbon.identity.framework.context.IdentityMessageContext;
import org.wso2.carbon.identity.framework.handler.IdentityGatewayEventHandler;

import java.util.ArrayList;
import java.util.List;


public class MultiStepAuthenticationHandler extends IdentityGatewayEventHandler {

    private List<IdentityGatewayEventHandler> gatewayEventHandlers = new ArrayList<>();


    public void addIdentityGatewayEventHandler(IdentityGatewayEventHandler identityGatewayEventHandler){
        this.gatewayEventHandlers.add(identityGatewayEventHandler);
    }

    @Override
    public void handle(IdentityMessageContext identityMessageContext) {
        for (IdentityGatewayEventHandler identityGatewayEventHandler: gatewayEventHandlers){
            if(identityGatewayEventHandler.canHandle(identityMessageContext)){
                setNextHandler(identityGatewayEventHandler);
                break;
            }
        }
    }

    @Override
    public boolean canHandle(IdentityMessageContext identityMessageContext) {
        return false;
    }
}
