package org.wso2.carbon.identity.gateway.internal;

import org.wso2.carbon.identity.framework.util.FrameworkUtil;
import org.wso2.carbon.identity.gateway.handler.callback.GatewayCallbackHandler;
import org.wso2.carbon.kernel.CarbonRuntime;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

/**
 * DataHolder to hold org.wso2.carbon.kernel.CarbonRuntime instance referenced through
 * org.wso2.carbon.helloworld.internal.ServiceComponent.
 *
 * @since 1.0.0-SNAPSHOT
 */
public class DataHolder {
    Logger logger = Logger.getLogger(DataHolder.class.getName());

    private static DataHolder instance = new DataHolder();
    private CarbonRuntime carbonRuntime;

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
     * Returns the CarbonRuntime service which gets set through a service component.
     *
     * @return CarbonRuntime Service
     */
    public CarbonRuntime getCarbonRuntime() {
        return carbonRuntime;
    }

    /**
     * This method is for setting the CarbonRuntime service. This method is used by
     * ServiceComponent.
     *
     * @param carbonRuntime The reference being passed through ServiceComponent
     */
    public void setCarbonRuntime(CarbonRuntime carbonRuntime) {
        this.carbonRuntime = carbonRuntime;
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
    }

}
