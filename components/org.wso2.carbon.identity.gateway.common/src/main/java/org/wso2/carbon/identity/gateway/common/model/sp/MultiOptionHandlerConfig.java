package org.wso2.carbon.identity.gateway.common.model.sp;

import java.util.ArrayList;
import java.util.List;

public class MultiOptionHandlerConfig{
    List<OptionConfig> optionConfig = new ArrayList<>();

    public List<OptionConfig> getOptionConfig() {
        return optionConfig;
    }

    public void setOptionConfig(List<OptionConfig> optionConfig) {
        this.optionConfig = optionConfig;
    }
}