/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.config.model;

import org.wso2.carbon.identity.application.authentication.framework.exception.SessionContextLoaderException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class is used to store the optimized step config attributes in session context.
 */
public class OptimizedStepConfig implements Serializable {

    private int order;
    private String loginPage;
    //private AuthenticatedUser authenticatedUser;
    private String authenticatedUserName;
    private boolean subjectIdentifierStep;
    private boolean subjectAttributeStep;
    private String authenticatedIdP;
    private String authenticatedAuthenticatorName;
    private List<OptimizedAuthenticatorConfig> optimizedAuthenticatorList;
    private List<String> authenticatorMappings;

    private boolean completed;
    private boolean multiOption;
    private boolean retrying;
    private boolean forced;

    public OptimizedStepConfig(StepConfig stepConfig) {

        this.order = stepConfig.getOrder();
        this.loginPage = stepConfig.getLoginPage();
        //this.authenticatedUser = stepConfig.getAuthenticatedUser();
        this.authenticatedUserName = stepConfig.getAuthenticatedUser().getUserName();
        this.subjectIdentifierStep = stepConfig.isSubjectIdentifierStep();
        this.subjectAttributeStep = stepConfig.isSubjectAttributeStep();
        this.authenticatedIdP = stepConfig.getAuthenticatedIdP();
        if (stepConfig.getAuthenticatedAutenticator() != null) {
            this.authenticatedAuthenticatorName = stepConfig.getAuthenticatedAutenticator().getName();
        }
        this.optimizedAuthenticatorList = getOptimizedAuthenticatorList(stepConfig.getAuthenticatorList());
        this.authenticatorMappings = stepConfig.getAuthenticatorMappings();
        this.completed = stepConfig.isCompleted();
        this.multiOption = stepConfig.isMultiOption();
        this.retrying = stepConfig.isRetrying();
        this.forced = stepConfig.isForced();

    }

    private List<OptimizedAuthenticatorConfig> getOptimizedAuthenticatorList(List<AuthenticatorConfig>
                                                                                     authenticatorList) {

        List<OptimizedAuthenticatorConfig> optimizedAuthenticatorList = new ArrayList<>();
        for (AuthenticatorConfig authenticatorConfig : authenticatorList) {
            optimizedAuthenticatorList.add(new OptimizedAuthenticatorConfig(authenticatorConfig));
        }
        return optimizedAuthenticatorList;
    }

    public int getOrder() {

        return order;
    }

    public String getLoginPage() {

        return loginPage;
    }

    /*public AuthenticatedUser getAuthenticatedUser() {

        return authenticatedUser;
    }*/

    public boolean isSubjectIdentifierStep() {

        return subjectIdentifierStep;
    }

    public boolean isSubjectAttributeStep() {

        return subjectAttributeStep;
    }

    public String getAuthenticatedIdP() {

        return authenticatedIdP;
    }

    public String getAuthenticatedAuthenticatorName() {

        return authenticatedAuthenticatorName;
    }

    public List<OptimizedAuthenticatorConfig> getOptimizedAuthenticatorList() {

        return optimizedAuthenticatorList;
    }

    public List<String> getAuthenticatorMappings() {

        return authenticatorMappings;
    }

    public boolean isCompleted() {

        return completed;
    }

    public boolean isMultiOption() {

        return multiOption;
    }

    public boolean isRetrying() {

        return retrying;
    }

    public boolean isForced() {

        return forced;
    }

    public StepConfig getStepConfig(String tenantDomain, Map<String, AuthenticatedUser> authenticatedUsers)
            throws SessionContextLoaderException {

        StepConfig stepConfig = new StepConfig();
        stepConfig.setOrder(this.order);
        stepConfig.setLoginPage(this.loginPage);
        //stepConfig.setAuthenticatedUser(this.authenticatedUser);
        stepConfig.setAuthenticatedUser(authenticatedUsers.get(this.authenticatedUserName));
        stepConfig.setSubjectIdentifierStep(this.subjectIdentifierStep);
        stepConfig.setSubjectAttributeStep(this.subjectAttributeStep);
        stepConfig.setAuthenticatedIdP(this.authenticatedIdP);
        List<AuthenticatorConfig> authenticatorList = new ArrayList<>();
        for (OptimizedAuthenticatorConfig optimizedAuthenticatorConfig : this.optimizedAuthenticatorList) {
            AuthenticatorConfig authConfig = optimizedAuthenticatorConfig.getAuthenticatorConfig(tenantDomain);
            authenticatorList.add(authConfig);
            if (authConfig.getName().equals(this.authenticatedAuthenticatorName)) {
                stepConfig.setAuthenticatedAutenticator(authConfig);
            }
        }
        stepConfig.setAuthenticatorList(authenticatorList);
        stepConfig.setAuthenticatorMappings(this.authenticatorMappings);
        stepConfig.setCompleted(this.completed);
        stepConfig.setMultiOption(this.multiOption);
        stepConfig.setRetrying(this.retrying);
        stepConfig.setForced(this.forced);
        return stepConfig;
    }
}
