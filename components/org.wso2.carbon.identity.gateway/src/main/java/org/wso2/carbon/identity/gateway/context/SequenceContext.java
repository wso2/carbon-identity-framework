/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.gateway.context;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.gateway.api.exception.GatewayRuntimeException;
import org.wso2.carbon.identity.gateway.common.model.sp.AuthenticationStepConfig;
import org.wso2.carbon.identity.gateway.model.User;
import org.wso2.carbon.identity.gateway.util.GatewayUtil;
import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SequenceContext implements Serializable {

    private static final long serialVersionUID = -3397856701064644528L;
    private static transient Logger logger = LoggerFactory.getLogger(GatewayUtil.class);
    private int currentStep = 1;

    private RequestPathAuthenticatorContext requestPathAuthenticatorContext = null;
    private List<StepContext> stepContextList = new ArrayList<>();

    public SequenceContext() {

    }

    public StepContext addStepContext() {
        StepContext stepContext = new StepContext();
        stepContext.setStep(currentStep);
        stepContextList.add(stepContext);
        return stepContext;
    }

    public Set<Claim> getClaims() {
        Set<Claim> aggregatedClaims = new HashSet<Claim>();
        stepContextList.stream().forEach(stepContext -> {
            try {
                stepContext.getUser().getClaims().forEach(claim ->
                        aggregatedClaims
                                .add(claim));
            } catch (IdentityStoreException | UserNotFoundException e) {
                String errorMessage = "Error occurred while calling to getCliams, " + e.getMessage();
                logger.error(errorMessage, e);
                throw new GatewayRuntimeException(errorMessage, e);
            }
        });
        return aggregatedClaims;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;
    }

    public StepContext getCurrentStepContext() {
        StepContext stepAuthenticatorsContext = null;
        if (currentStep > 0 && stepContextList.size() >= currentStep) {
            stepAuthenticatorsContext = stepContextList.get(stepContextList.size() - 1);
        }
        return stepAuthenticatorsContext;
    }

    public RequestPathAuthenticatorContext getRequestPathAuthenticator() {
        return requestPathAuthenticatorContext;
    }

    public void setRequestPathAuthenticator(
            RequestPathAuthenticatorContext requestPathAuthenticatorContext) {
        this.requestPathAuthenticatorContext = requestPathAuthenticatorContext;
    }

    public StepContext getStepContext(int step) {
        StepContext stepAuthenticatorsContext = null;
        if (stepContextList.size() >= step && step > 0) {
            stepAuthenticatorsContext = stepContextList.get(step - 1);
        }
        return stepAuthenticatorsContext;
    }

    public static enum Status {
        INITIAL,
        INCOMPLETE,
        AUTHENTICATED,
        FAILED;
    }

    public static class RequestPathAuthenticatorContext implements Serializable {


        private static final long serialVersionUID = 6683606766294458119L;

        private String authenticatorName;
        private User user;
        private boolean isAuthenticated = false;

        public String getAuthenticatorName() {
            return authenticatorName;
        }

        public void setAuthenticatorName(String authenticatorName) {
            this.authenticatorName = authenticatorName;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public boolean isAuthenticated() {
            return isAuthenticated;
        }

        public void setIsAuthenticated(boolean isAuthenticated) {
            this.isAuthenticated = isAuthenticated;
        }
    }

    public static class StepContext implements Serializable {

        private static final long serialVersionUID = -6517237540622607778L;
        private Status status = Status.INITIAL;
        private int step;
        private String authenticatorName;
        private String identityProviderName;
        private User user;
        private int retryCount = 0;


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

        public boolean isAuthenticated() {
            if (status.equals(Status.AUTHENTICATED)) {
                return true;
            }
            return false;
        }

        public Status getStatus() {
            return status;
        }

        public void setStatus(Status status) {
            this.status = status;
        }

        public int getRetryCount() {
            return retryCount;
        }

        public void setRetryCount(int retryCount) {
            this.retryCount = retryCount;
        }
    }
}
