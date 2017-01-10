package org.wso2.carbon.identity.gateway.handler;

import org.wso2.carbon.identity.framework.context.MessageContext;
import org.wso2.carbon.identity.framework.handler.AbstractHandler;
import org.wso2.carbon.identity.framework.handler.HandlerException;
import org.wso2.carbon.identity.framework.handler.HandlerResponseStatus;
import org.wso2.carbon.identity.gateway.context.GatewayMessageContext;

import java.util.ArrayList;
import java.util.List;

public abstract class MultiOptionGatewayHandler extends AbstractGatewayHandler<GatewayMessageContext> {

    protected List<AbstractGatewayHandler> multiOptionHandlers = new ArrayList<>();

    public MultiOptionGatewayHandler(AbstractGatewayHandler nextHandler) {
        super(nextHandler);
    }


    public void addIdentityGatewayEventHandler(AbstractGatewayHandler abstractHandler) {

        this.multiOptionHandlers.add(abstractHandler);
    }

    @Override
    public HandlerResponseStatus handle(GatewayMessageContext messageContext) throws HandlerException {

        AbstractHandler selectedHandler = getSelectedHandler(messageContext);
        selectedHandler.execute(messageContext);
        return HandlerResponseStatus.CONTINUE;
    }

    protected abstract AbstractGatewayHandler getSelectedHandler(GatewayMessageContext messageContext) throws HandlerException;

    public List<AbstractGatewayHandler> getMultiOptionHandlers() {
        return multiOptionHandlers;
    }
}
