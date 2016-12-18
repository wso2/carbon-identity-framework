package org.wso2.carbon.identity.framework.handler.impl;

import org.wso2.carbon.identity.framework.context.MessageContext;
import org.wso2.carbon.identity.framework.handler.AbstractHandler;
import org.wso2.carbon.identity.framework.handler.HandlerConfig;
import org.wso2.carbon.identity.framework.handler.HandlerException;
import org.wso2.carbon.identity.framework.handler.HandlerIdentifier;
import org.wso2.carbon.identity.framework.handler.HandlerResponseStatus;

import java.util.ArrayList;
import java.util.List;

public abstract class MultiOptionStepHandler<T1 extends HandlerIdentifier,
        T2 extends HandlerConfig, T3 extends AbstractHandler, T4 extends MessageContext> extends AbstractHandler<T1,
        T2, T3, T4> {

    protected List<T3> multiOptionHandlers = new ArrayList<>();

    public MultiOptionStepHandler(T1 handlerIdentifier) {
        super(handlerIdentifier);
    }

    public MultiOptionStepHandler(T1 handlerIdentifier, List<T3> multiOptionHandler) {

        super(handlerIdentifier);
        this.multiOptionHandlers = multiOptionHandler;
    }

    public void addIdentityGatewayEventHandler(T3 abstractHandler) {

        this.multiOptionHandlers.add(abstractHandler);
    }

    @Override
    public T2 getConfiguration(T1 handlerIdentifier) {

        return null;
    }

    @Override
    public HandlerResponseStatus handle(T4 messageContext) throws HandlerException {

        AbstractHandler selectedHandler = getSelectedHandler(messageContext);
        selectedHandler.execute(messageContext);
        return HandlerResponseStatus.CONTINUE;
    }

    protected abstract AbstractHandler getSelectedHandler(T4 messageContext) throws HandlerException;

    public List<T3> getMultiOptionHandlers() {
        return multiOptionHandlers;
    }
}
