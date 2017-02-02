package org.wso2.carbon.identity.gateway.common.model.sp;

import java.util.ArrayList;
import java.util.List;

class AuthenticationSequenceHandler extends HandlerInterceptor {

    List<StepHandler> stepHandlers = new ArrayList<>();

    public List<StepHandler> getStepHandlers() {
        return stepHandlers;
    }

    public void setStepHandlers(List<StepHandler> stepHandlers) {
        this.stepHandlers = stepHandlers;
    }
}