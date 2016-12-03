package org.wso2.carbon.identity.framework.handler;


import org.wso2.carbon.identity.framework.context.IdentityMessageContext;

public abstract class GatewayEventHandler {

    private GatewayEventHandler parentHandler = null ;
    private GatewayEventHandler nextHandler = null ;

    public void setNextHandler(GatewayEventHandler nextHandler) {
        this.nextHandler = nextHandler ;
        this.nextHandler.setParentHandler(this);
    }

    public GatewayEventHandler getNextHandler() {
        return this.nextHandler;
    }

    protected void setParentHandler(GatewayEventHandler parentHandler) {
        this.parentHandler = parentHandler ;
    }

    public GatewayEventHandler getParentHandler() {
        return this.parentHandler;
    }

    public void execute(IdentityMessageContext identityMessageContext){
        GatewayInvocationResponse gatewayInvocationResponse = GatewayInvocationResponse.CONTINUE ;
        if(canHandle(identityMessageContext)){
            gatewayInvocationResponse = handle(identityMessageContext);
        }
        if(getNextHandler() != null && gatewayInvocationResponse.name().equals(GatewayInvocationResponse.CONTINUE.name())) {
            getNextHandler().execute(identityMessageContext);
        }
    }

    public abstract GatewayInvocationResponse handle(IdentityMessageContext identityMessageContext);

    public abstract boolean canHandle(IdentityMessageContext identityMessageContext);
}
