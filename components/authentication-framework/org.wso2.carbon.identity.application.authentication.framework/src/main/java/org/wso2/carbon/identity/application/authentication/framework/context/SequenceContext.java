package org.wso2.carbon.identity.application.authentication.framework.context;


import java.util.ArrayList;
import java.util.List;

public class SequenceContext {

    private int stepOrder = 0 ;
    private boolean current

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

    public void addCurrentStepAuthenticatorContext(StepAuthenticatorContext  stepAuthenticatorContext){
        stepAuthenticatorContextList.add(stepAuthenticatorContext);
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;
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

    public static class StepAuthenticatorContext {
        private int step;
        private boolean isAuthenticated = false;

        public int getStep() {
            return step;
        }

        public void setStep(int step) {
            this.step = step;
        }

        public boolean isAuthenticated() {
            return isAuthenticated;
        }

        public void setIsAuthenticated(boolean isAuthenticated) {
            this.isAuthenticated = isAuthenticated;
        }
    }


}
