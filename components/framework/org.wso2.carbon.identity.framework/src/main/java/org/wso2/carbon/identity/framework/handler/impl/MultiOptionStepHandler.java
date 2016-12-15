package org.wso2.carbon.identity.framework.handler.impl;

import org.wso2.carbon.identity.common.base.message.MessageContext;
import org.wso2.carbon.identity.framework.handler.AbstractHandler;
import org.wso2.carbon.identity.framework.handler.HandlerConfig;
import org.wso2.carbon.identity.framework.handler.HandlerException;
import org.wso2.carbon.identity.framework.handler.HandlerIdentifier;
import org.wso2.carbon.identity.framework.handler.HandlerResponseStatus;

import java.util.ArrayList;
import java.util.List;

public abstract class MultiOptionStepHandler<T1 extends HandlerIdentifier,
        T2 extends HandlerConfig, T3 extends AbstractHandler, T4 extends MessageContext> extends AbstractHandler <T1,
        T2, T3, T4>{

    private List<AbstractHandler> multiOptionHandlers = new ArrayList<>();

    public MultiOptionStepHandler(T1 handlerIdentifier,
                                  T3 nextHandler,
                                  List<AbstractHandler> multiOptionHandler) {
        super(handlerIdentifier, nextHandler);
        this.multiOptionHandlers = multiOptionHandler;
    }

    public MultiOptionStepHandler(T1 handlerIdentifier,
                                  List<AbstractHandler> multiOptionHandler) {
        super(handlerIdentifier);
        this.multiOptionHandlers = multiOptionHandler;
    }

    public void addIdentityGatewayEventHandler(AbstractHandler abstractHandler) {
        this.multiOptionHandlers.add(abstractHandler);
    }

    @Override
    public HandlerConfig getConfiguration(HandlerIdentifier handlerIdentifier) {
        return null;
    }

    @Override
    public HandlerResponseStatus handle(MessageContext messageContext) throws HandlerException{

        AbstractHandler selectedHandler = getSelectedHandler();
        selectedHandler.execute(messageContext);

        return HandlerResponseStatus.CONTINUE;
    }

    protected abstract AbstractHandler getSelectedHandler();

}
