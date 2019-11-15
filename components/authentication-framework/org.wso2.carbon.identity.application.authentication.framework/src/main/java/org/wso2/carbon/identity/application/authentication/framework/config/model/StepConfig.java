/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds the login page and the authenticator objects
 * of a particular factor
 */
public class StepConfig implements Serializable {

    private static final long serialVersionUID = 7271079506748466841L;

    private int order;
    private String loginPage;
    private AuthenticatedUser authenticatedUser;
    private boolean subjectIdentifierStep;
    private boolean subjectAttributeStep;
    private String authenticatedIdP;
    private AuthenticatorConfig authenticatedAutenticator;
    private List<AuthenticatorConfig> authenticatorList = new ArrayList<>();
    private List<String> authenticatorMappings = new ArrayList<>();

    private boolean completed;
    private boolean multiOption;
    private boolean retrying;

    public StepConfig() {
    }

    /**
     * @return
     */
    public String getLoginPage() {
        return loginPage;
    }

    /**
     * @param loginPage
     */
    public void setLoginPage(String loginPage) {
        this.loginPage = loginPage;
    }

    /**
     * @return
     */
    public List<String> getAuthenticatorMappings() {
        return authenticatorMappings;
    }

    /**
     * @param authenticatorMappings
     */
    public void setAuthenticatorMappings(List<String> authenticatorMappings) {
        this.authenticatorMappings = authenticatorMappings;
    }

    /**
     * @return
     */
    public int getOrder() {
        return order;
    }

    /**
     * @param order
     */
    public void setOrder(int order) {
        this.order = order;
    }

    /**
     * @return
     */
    public List<AuthenticatorConfig> getAuthenticatorList() {
        return authenticatorList;
    }

    /**
     * @param authenticatorList
     */
    public void setAuthenticatorList(List<AuthenticatorConfig> authenticatorList) {
        this.authenticatorList = authenticatorList;
    }

    /**
     * @return
     */
    public AuthenticatedUser getAuthenticatedUser() {
        return authenticatedUser;
    }

    /**
     * @param authenticatedUser
     */
    public void setAuthenticatedUser(AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
    }

    /**
     * @return
     */
    public String getAuthenticatedIdP() {
        return authenticatedIdP;
    }

    /**
     * @param authenticatedIdP
     */
    public void setAuthenticatedIdP(String authenticatedIdP) {
        this.authenticatedIdP = authenticatedIdP;
    }

    /**
     * @return
     */
    public AuthenticatorConfig getAuthenticatedAutenticator() {
        return authenticatedAutenticator;
    }

    /**
     * @param authenticatedAutenticator
     */
    public void setAuthenticatedAutenticator(
            AuthenticatorConfig authenticatedAutenticator) {
        this.authenticatedAutenticator = authenticatedAutenticator;
    }

    /**
     * @return
     */
    public boolean isSubjectAttributeStep() {
        return subjectAttributeStep;
    }

    /**
     * @param subjectAttributeStep
     */
    public void setSubjectAttributeStep(boolean subjectAttributeStep) {
        this.subjectAttributeStep = subjectAttributeStep;
    }

    /**
     * @return
     */
    public boolean isSubjectIdentifierStep() {
        return subjectIdentifierStep;
    }

    /**
     * @param subjectIdentifierStep
     */
    public void setSubjectIdentifierStep(boolean subjectIdentifierStep) {
        this.subjectIdentifierStep = subjectIdentifierStep;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isMultiOption() {
        return multiOption;
    }

    public void setMultiOption(boolean multiOption) {
        this.multiOption = multiOption;
    }

    public boolean isRetrying() {
        return retrying;
    }

    public void setRetrying(boolean retrying) {
        this.retrying = retrying;
    }

    /**
     * Deep clone of StepConfig inorder to keep backup.
     *
     * @return cloned StepConfig.
     */
    public StepConfig deepClone() {
        StepConfig clone = new StepConfig();
        clone.order = order;
        clone.loginPage = loginPage;
        clone.authenticatedUser = authenticatedUser;
        clone.subjectIdentifierStep = subjectIdentifierStep;
        clone.subjectAttributeStep = subjectAttributeStep;
        clone.authenticatedIdP = authenticatedIdP;
        clone.authenticatedAutenticator =
                authenticatedAutenticator != null ? authenticatedAutenticator.deepClone() : authenticatedAutenticator;
        clone.authenticatorList = new ArrayList<>();
        for (AuthenticatorConfig authenticator : authenticatorList) {
            clone.authenticatorList.add(authenticator.deepClone());
        }
        clone.authenticatorMappings = new ArrayList<>(authenticatorMappings);
        clone.completed = completed;
        clone.multiOption = multiOption;
        clone.retrying = retrying;
        return clone;
    }

    /**
     * Apply any state changes to the newly clone stepConfig object from backup, by comparing with the StepConfig which
     * obtain from SequenceConfig stepMap.
     *
     * @param stepConfigFromContext StepConfig which obtain from SequenceConfig stepMap.
     */
    public void applyStateChangesToNewObjectFromContextStepMap(StepConfig stepConfigFromContext) {

        if (stepConfigFromContext != null) {
            this.completed = stepConfigFromContext.completed;
            this.multiOption = stepConfigFromContext.multiOption;
            this.retrying = stepConfigFromContext.retrying;
            this.subjectIdentifierStep = stepConfigFromContext.subjectIdentifierStep;
            this.subjectAttributeStep = stepConfigFromContext.subjectAttributeStep;
        }
    }
}
