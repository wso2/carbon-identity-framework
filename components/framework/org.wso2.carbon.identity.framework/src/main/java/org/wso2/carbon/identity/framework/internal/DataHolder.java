package org.wso2.carbon.identity.framework.internal;

import org.wso2.carbon.identity.framework.IdentityProcessor;
import org.wso2.carbon.identity.framework.util.FrameworkUtil;
import org.wso2.carbon.kernel.CarbonRuntime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

/**
 * DataHolder to hold org.wso2.carbon.kernel.CarbonRuntime instance referenced through
 * org.wso2.carbon.helloworld.internal.ServiceComponent.
 *
 * @since 1.0.0
 */
public class DataHolder {
    Logger logger = Logger.getLogger(DataHolder.class.getName());

    private static DataHolder instance = new DataHolder();
    private CarbonRuntime carbonRuntime;
    private List<IdentityProcessor> identityProcessors = new ArrayList<>();

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


    public void setIdentityProcessor(IdentityProcessor identityProcessor) {
        identityProcessors.add(identityProcessor);
        Collections.sort(identityProcessors, identityProcessorComparator);
    }

    public void removeIdentityProcessor(IdentityProcessor identityProcessor) {
        identityProcessors.remove(identityProcessor);
    }

    public List<IdentityProcessor> getIdentityProcessors() {
        return identityProcessors;
    }


    private Comparator<IdentityProcessor> identityProcessorComparator =
            (processor1, processor2) -> FrameworkUtil.comparePriory(processor1.getPriority(), processor2.getPriority());
}
