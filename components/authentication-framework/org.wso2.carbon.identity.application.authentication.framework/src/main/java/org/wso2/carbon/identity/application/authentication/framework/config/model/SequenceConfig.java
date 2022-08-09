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

import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.AuthenticationGraph;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration holder for an application.
 */
public class SequenceConfig implements Serializable, Cloneable {

    private static final long serialVersionUID = 6822366703354668075L;

    private String name;
    private boolean isForceAuthn;
    private boolean isCheckAuthn;
    private String applicationId;
    private Map<Integer, StepConfig> stepMap = new HashMap<>();
    private AuthenticationGraph authenticationGraph;
    private List<AuthenticatorConfig> reqPathAuthenticators = new ArrayList<>();
    private ApplicationConfig applicationConfig = null;
    private OptimizedApplicationConfig optApplicationConfig = null;
    private boolean completed;

    private AuthenticatedUser authenticatedUser;
    private String authenticatedIdPs;

    private AuthenticatorConfig authenticatedReqPathAuthenticator;
    private List<String> requestedAcr;

    public SequenceConfig() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<Integer, StepConfig> getStepMap() {
        return stepMap;
    }

    public void setStepMap(Map<Integer, StepConfig> stepMap) {
        this.stepMap = stepMap;
    }

    public boolean isForceAuthn() {
        return isForceAuthn;
    }

    public void setForceAuthn(boolean isForceAuthn) {
        this.isForceAuthn = isForceAuthn;
    }

    public boolean isCheckAuthn() {
        return isCheckAuthn;
    }

    public void setCheckAuthn(boolean isCheckAuthn) {
        this.isCheckAuthn = isCheckAuthn;
    }

    public List<AuthenticatorConfig> getReqPathAuthenticators() {
        return reqPathAuthenticators;
    }

    public void setReqPathAuthenticators(
            List<AuthenticatorConfig> reqPathAuthenticators) {
        this.reqPathAuthenticators = reqPathAuthenticators;
    }

    public ApplicationConfig getApplicationConfig() {
        return applicationConfig;
    }

    public void setApplicationConfig(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public AuthenticatedUser getAuthenticatedUser() {
        return authenticatedUser;
    }

    public void setAuthenticatedUser(AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
    }

    public String getAuthenticatedIdPs() {
        return authenticatedIdPs;
    }

    public void setAuthenticatedIdPs(String authenticatedIdPs) {
        this.authenticatedIdPs = authenticatedIdPs;
    }

    public AuthenticatorConfig getAuthenticatedReqPathAuthenticator() {
        return authenticatedReqPathAuthenticator;
    }

    public void setAuthenticatedReqPathAuthenticator(
            AuthenticatorConfig authenticatedReqPathAuthenticator) {
        this.authenticatedReqPathAuthenticator = authenticatedReqPathAuthenticator;
    }

    public AuthenticationGraph getAuthenticationGraph() {
        return authenticationGraph;
    }

    public void setAuthenticationGraph(AuthenticationGraph authenticationGraph) {
        this.authenticationGraph = authenticationGraph;
    }

    public List<String> getRequestedAcr() {
        if (requestedAcr == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(requestedAcr);
    }

    public void addRequestedAcr(String acr) {
        if (requestedAcr == null) {
            requestedAcr = new ArrayList<>();
        }
        requestedAcr.add(acr);
    }

    /**
     * This method will clone current class objects.
     * This method is to solve the issue - multiple requests for same user/SP
     *
     * @return Object object
     */
    public Object clone() throws CloneNotSupportedException {
        SequenceConfig sequenceConfig = (SequenceConfig) super.clone();
        sequenceConfig.setApplicationConfig((ApplicationConfig) applicationConfig.clone());
        sequenceConfig.setStepMap(new HashMap<>(this.stepMap));
        sequenceConfig.setReqPathAuthenticators(new ArrayList<>(this.reqPathAuthenticators));
        sequenceConfig.setName(this.getName());
        sequenceConfig.setForceAuthn(this.isForceAuthn());
        sequenceConfig.setCheckAuthn(this.isCheckAuthn());
        sequenceConfig.setApplicationId(this.getApplicationId());
        sequenceConfig.setCompleted(this.isCompleted());
        sequenceConfig.setAuthenticatedUser(this.getAuthenticatedUser());
        sequenceConfig.setAuthenticatedIdPs(this.getAuthenticatedIdPs());
        sequenceConfig.setAuthenticatedReqPathAuthenticator(this.getAuthenticatedReqPathAuthenticator());
        sequenceConfig.requestedAcr = new ArrayList<>(this.getRequestedAcr());
        sequenceConfig.setAuthenticationGraph(this.getAuthenticationGraph());
        sequenceConfig.setOptApplicationConfig(this.getOptApplicationConfig());
        return sequenceConfig;
    }

    public void setRequestedAcr(List<String> requestedAcr) {

        this.requestedAcr = requestedAcr;
    }

    public OptimizedApplicationConfig getOptApplicationConfig() {

        return this.optApplicationConfig;
    }

    public void setOptApplicationConfig(OptimizedApplicationConfig optApplicationConfig) {

        this.optApplicationConfig = optApplicationConfig;
    }
}
