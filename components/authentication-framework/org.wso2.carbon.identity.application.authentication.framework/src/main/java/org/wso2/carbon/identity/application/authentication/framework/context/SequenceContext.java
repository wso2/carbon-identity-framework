package org.wso2.carbon.identity.application.authentication.framework.context;


import org.wso2.carbon.identity.application.authentication.framework.model.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SequenceContext {

    private int currentStep = 1;

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

    public int getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;
    }

    public StepContext getStepContext(int step) {
        StepContext stepAuthenticatorsContext = null;
        if (stepContextList.size() >= step) {
            stepAuthenticatorsContext = stepContextList.get(step);
        }
        return stepAuthenticatorsContext;
    }
    public StepContext getCurrentStepContext() {
        StepContext stepAuthenticatorsContext = null;
        if (stepContextList.size() >= currentStep) {
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
        private int step;
        private String name ;
        private User user;
        private boolean isAuthenticated = false;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isAuthenticated() {
            return isAuthenticated;
        }

        public void setIsAuthenticated(boolean isAuthenticated) {
            this.isAuthenticated = isAuthenticated;
        }

        public int getStep() {
            return step;
        }

        public void setStep(int step) {
            this.step = step;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }
    }




}
