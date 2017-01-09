package org.wso2.carbon.identity.framework.handler.impl;

import org.wso2.carbon.identity.framework.context.MessageContext;
import org.wso2.carbon.identity.framework.handler.AbstractHandler;
import org.wso2.carbon.identity.framework.handler.HandlerException;
import org.wso2.carbon.identity.framework.handler.HandlerResponseStatus;

import java.util.ArrayList;
import java.util.List;

public abstract class MultiOptionStepHandler<T1 extends MessageContext> extends AbstractHandler<T1> {

    protected List<AbstractHandler> multiOptionHandlers = new ArrayList<>();

    public MultiOptionStepHandler(AbstractHandler nextHandler) {
        super(nextHandler);
    }


    public void addIdentityGatewayEventHandler(AbstractHandler abstractHandler) {

        this.multiOptionHandlers.add(abstractHandler);
    }

    @Override
    public HandlerResponseStatus handle(T1 messageContext) throws HandlerException {

        AbstractHandler selectedHandler = getSelectedHandler(messageContext);
        selectedHandler.execute(messageContext);
        return HandlerResponseStatus.CONTINUE;
    }

    protected abstract AbstractHandler getSelectedHandler(T1 messageContext) throws HandlerException;

    public List<AbstractHandler> getMultiOptionHandlers() {
        return multiOptionHandlers;
    }
}
