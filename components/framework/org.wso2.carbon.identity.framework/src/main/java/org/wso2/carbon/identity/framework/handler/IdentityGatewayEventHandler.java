package org.wso2.carbon.identity.framework.handler;


import org.wso2.carbon.identity.framework.context.IdentityMessageContext;

public abstract class IdentityGatewayEventHandler {

    private IdentityGatewayEventHandler parentHandler = null ;
    private IdentityGatewayEventHandler nextHandler = null ;

    public void setNextHandler(IdentityGatewayEventHandler nextHandler) {
        this.nextHandler = nextHandler ;
        this.nextHandler.setParentHandler(this);
    }

    public IdentityGatewayEventHandler getNextHandler() {
        return this.nextHandler;
    }

    protected void setParentHandler(IdentityGatewayEventHandler parentHandler) {
        this.parentHandler = parentHandler ;
    }

    public IdentityGatewayEventHandler getParentHandler() {
        return this.parentHandler;
    }

    public void execute(IdentityMessageContext identityMessageContext){
        if(canHandle(identityMessageContext)){
            handle(identityMessageContext);
        }
        if(getNextHandler() != null) {
            getNextHandler().execute(identityMessageContext);
        }
    }

    public abstract void handle(IdentityMessageContext identityMessageContext);

    public abstract boolean canHandle(IdentityMessageContext identityMessageContext);
}
