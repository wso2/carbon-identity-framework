package org.wso2.carbon.identity.gateway.common.model.sp;

import java.util.ArrayList;
import java.util.List;

class AuthenticationSequenceHandlerConfig extends GenericHandlerConfig {

    List<StepHandlerConfig> stepHandlers = new ArrayList<>();

    public List<StepHandlerConfig> getStepHandlers() {
        return stepHandlers;
    }

    public void setStepHandlers(List<StepHandlerConfig> stepHandlers) {
        this.stepHandlers = stepHandlers;
    }
}