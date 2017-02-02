package org.wso2.carbon.identity.gateway.common.model.sp;

import java.util.ArrayList;
import java.util.List;

class MultiOptionHandlerConfig extends GenericHandlerConfig {
    List<OptionConfig> optionConfigs = new ArrayList<>();

    public List<OptionConfig> getOptionConfigs() {
        return optionConfigs;
    }

    public void setOptionConfigs(List<OptionConfig> optionConfigs) {
        this.optionConfigs = optionConfigs;
    }
}