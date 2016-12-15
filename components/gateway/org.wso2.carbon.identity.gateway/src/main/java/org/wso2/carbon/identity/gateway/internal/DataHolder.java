package org.wso2.carbon.identity.gateway.internal;

import org.wso2.carbon.identity.framework.util.FrameworkUtil;
import org.wso2.carbon.identity.gateway.element.callback.GatewayCallbackHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

/**
 * DataHolder to hold the OSGi services consumed by the Identity Gateway component.
 *
 * @since 1.0.0-SNAPSHOT
 */
public class DataHolder {
    Logger logger = Logger.getLogger(DataHolder.class.getName());

    private static DataHolder instance = new DataHolder();
    private List<GatewayCallbackHandler> gatewayCallbackHandlers = new ArrayList<>();

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


    /**
     * Returns the {@link GatewayCallbackHandler} services which gets set through a service component.
     *
     * @return GatewayCallbackHandler Service
     */
    public List<GatewayCallbackHandler> getGatewayCallbackHandlers() {

        return gatewayCallbackHandlers;
    }

    /**
     * This method is for add a {@link GatewayCallbackHandler} service. This method is used by
     * ServiceComponent.
     *
     * @param gatewayCallbackHandler The reference being passed through ServiceComponent
     */
    public void addGatewayCallbackHandler(GatewayCallbackHandler gatewayCallbackHandler) {

        gatewayCallbackHandlers.add(gatewayCallbackHandler);
        Collections.sort(gatewayCallbackHandlers, callbackHandlerComparator);
    }


    private static Comparator<GatewayCallbackHandler> callbackHandlerComparator =
            (handler1, handler2) -> FrameworkUtil.comparePriory(handler1.getPriority(), handler2.getPriority());
}
