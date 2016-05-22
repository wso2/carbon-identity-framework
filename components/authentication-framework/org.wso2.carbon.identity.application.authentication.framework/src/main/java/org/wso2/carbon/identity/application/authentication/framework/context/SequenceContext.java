package org.wso2.carbon.identity.application.authentication.framework.context;


import java.util.ArrayList;
import java.util.List;

public class SequenceContext {
    private int currentStepCount = 1;
    private RequestPathAuthenticatorContext requestPathAuthenticatorContext = null;
    private List<StepContext> stepContextList = new ArrayList<>();
    public SequenceContext() {

    }

    public RequestPathAuthenticatorContext getRequestPathAuthenticator() {
        return requestPathAuthenticatorContext;
    }

    public void setRequestPathAuthenticator(
            RequestPathAuthenticatorContext requestPathAuthenticatorContext) {
        this.requestPathAuthenticatorContext = requestPathAuthenticatorContext;
    }

    public int getCurrentStepCount() {
        return currentStepCount;
    }

    public void setCurrentStepCount(int currentStepCount) {
        this.currentStepCount = currentStepCount;
    }

    public StepContext getCurrentStepContext() {
        StepContext stepAuthenticatorsContext = null;
        if (stepContextList.size() >= currentStepCount) {
            stepAuthenticatorsContext = stepContextList.get(stepContextList.size());
        }
        return stepAuthenticatorsContext;
    }

    public void addCurrentStepAuthenticatorContext(StepContext stepContext) {
        stepContextList.add(stepContext);
    }


    public static class RequestPathAuthenticatorContext {
        private boolean isAuthenticated = false;

        public boolean isAuthenticated() {
            return isAuthenticated;
        }

        public void setIsAuthenticated(boolean isAuthenticated) {
            this.isAuthenticated = isAuthenticated;
        }
    }

    public static class StepContext {
        private int stepCount;
        private String name;
        private boolean isLocalApplicationAuthenticator = false;
        private boolean isAuthenticated = false;

        public int getStepCount() {
            return stepCount;
        }

        public void setStepCount(int stepCount) {
            this.stepCount = stepCount;
        }

        public boolean isAuthenticated() {
            return isAuthenticated;
        }

        public void setIsAuthenticated(boolean isAuthenticated) {
            this.isAuthenticated = isAuthenticated;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isLocalApplicationAuthenticator() {
            return isLocalApplicationAuthenticator;
        }

        public void setIsLocalApplicationAuthenticator(boolean isLocalApplicationAuthenticator) {
            this.isLocalApplicationAuthenticator = isLocalApplicationAuthenticator;
        }
    }


}
