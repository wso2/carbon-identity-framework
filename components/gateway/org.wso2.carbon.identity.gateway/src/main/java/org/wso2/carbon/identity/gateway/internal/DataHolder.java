package org.wso2.carbon.identity.gateway.internal;

import org.wso2.carbon.identity.framework.handler.AbstractHandler;
import org.wso2.carbon.identity.framework.util.FrameworkUtil;
import org.wso2.carbon.identity.gateway.element.AbstractGatewayHandler;
import org.wso2.carbon.identity.gateway.element.callback.AbstractCallbackHandler;
import org.wso2.carbon.identity.gateway.element.callback.GatewayCallbackHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * DataHolder to hold the OSGi services consumed by the Identity Gateway component.
 *
 * @since 1.0.0-SNAPSHOT
 */
public class DataHolder {
    Logger logger = Logger.getLogger(DataHolder.class.getName());

    private static DataHolder instance = new DataHolder();
    private List<AbstractCallbackHandler> gatewayCallbackHandlers = new ArrayList<>();
    private Map<String, AbstractGatewayHandler> handlers = new HashMap<>();
    private DataHolder() {

    }

    /**
     * This returns the DataHolder instance.
     *
     * @return The DataHolder instance of this singleton class
     */
    public static DataHolder getInstance() {
        return instance;
    }


    public AbstractGatewayHandler getHandler(String name){
        return handlers.get(name);
    }

    /**
     * Returns the {@link GatewayCallbackHandler} services which gets set through a service component.
     *
     * @return GatewayCallbackHandler Service
     */
    public List<AbstractCallbackHandler> getGatewayCallbackHandlers() {

        return gatewayCallbackHandlers;
    }

    /**
     * This method is for add a {@link GatewayCallbackHandler} service. This method is used by
     * ServiceComponent.
     *
     * @param callbackHandler The reference being passed through ServiceComponent
     */
    public void addGatewayCallbackHandler(AbstractCallbackHandler callbackHandler) {

        gatewayCallbackHandlers.add(callbackHandler);
        Collections.sort(gatewayCallbackHandlers, callbackHandlerComparator);
    }

    public void addHandler(AbstractGatewayHandler handler) {
        handlers.put(handler.getClass().getSimpleName(), handler);
    }

    private static Comparator<AbstractCallbackHandler> callbackHandlerComparator =
            (handler1, handler2) -> FrameworkUtil.comparePriory(handler1.getPriority(), handler2.getPriority());
}
