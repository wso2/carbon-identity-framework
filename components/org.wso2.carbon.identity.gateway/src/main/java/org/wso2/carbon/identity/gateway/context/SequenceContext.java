package org.wso2.carbon.identity.gateway.context;



import org.wso2.carbon.identity.gateway.model.User;

import java.util.ArrayList;
import java.util.List;

public class SequenceContext {

    private int currentStep = 0;

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
        if (stepContextList.size() >= step && step > 0) {
            stepAuthenticatorsContext = stepContextList.get(step-1);
        }
        return stepAuthenticatorsContext;
    }

    public StepContext getCurrentStepContext() {
        StepContext stepAuthenticatorsContext = null;
        if (currentStep > 0 &&  stepContextList.size() >= currentStep) {
            stepAuthenticatorsContext = stepContextList.get(stepContextList.size()-1);
        }
        return stepAuthenticatorsContext;
    }

    public StepContext addStepContext() {
        StepContext stepContext = new StepContext();
        stepContext.setStep(++currentStep);
        stepContextList.add(stepContext);
        return stepContext ;
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
        private String authenticatorName;
        private String identityProviderName;
        private User user;
        private boolean isAuthenticated = false;

        public String getAuthenticatorName() {
            return authenticatorName;
        }

        public void setAuthenticatorName(String authenticatorName) {
            this.authenticatorName = authenticatorName;
        }

        public String getIdentityProviderName() {
            return identityProviderName;
        }

        public void setIdentityProviderName(String identityProviderName) {
            this.identityProviderName = identityProviderName;
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
