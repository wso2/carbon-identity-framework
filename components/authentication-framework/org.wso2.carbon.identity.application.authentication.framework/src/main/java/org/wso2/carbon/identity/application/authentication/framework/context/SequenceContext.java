package org.wso2.carbon.identity.application.authentication.framework.context;


import java.util.ArrayList;
import java.util.List;

public class SequenceContext {


    private RequestPathAuthenticatorContext requestPathAuthenticatorContext = null;
    private List<StepAuthenticatorContext> stepAuthenticatorContextList = new ArrayList<>();

    public RequestPathAuthenticatorContext getRequestPathAuthenticator() {
        return requestPathAuthenticatorContext;
    }

    public void setRequestPathAuthenticator(
            RequestPathAuthenticatorContext requestPathAuthenticatorContext) {
        this.requestPathAuthenticatorContext = requestPathAuthenticatorContext;
    }

    public StepAuthenticatorContext getCurrentStepAuthenticator() {
        StepAuthenticatorContext stepAuthenticatorsContext = null;
        if (stepAuthenticatorContextList.size() > 0) {
            stepAuthenticatorsContext = stepAuthenticatorContextList.get(stepAuthenticatorContextList.size());
        }
        return stepAuthenticatorsContext;
    }

    public static class RequestPathAuthenticatorContext {
        private boolean isAuthenticated = false;
    }

    public static class StepAuthenticatorContext {
        private int step;
        private boolean isAuthenticated = false;
    }


}
