package org.wso2.carbon.identity.gateway.common.model.sp;

import java.util.ArrayList;
import java.util.List;

public class AuthenticationHandlerConfig {

    List<StepHandlerConfig> stepHandlerConfigs = new ArrayList<>();

    public List<StepHandlerConfig> getStepHandlerConfigs() {
        return stepHandlerConfigs;
    }

    public void setStepHandlerConfigs(List<StepHandlerConfig> stepHandlerConfigs) {
        this.stepHandlerConfigs = stepHandlerConfigs;
    }
}