/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.flow.execution.engine.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.MDC;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ExternalIdPConfig;
import org.wso2.carbon.identity.flow.mgt.model.GraphConfig;
import org.wso2.carbon.identity.flow.mgt.model.NodeConfig;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * This class is responsible for holding the flow context.
 */
public class FlowExecutionContext implements Serializable {

    private static final long serialVersionUID = 542871476395078667L;
    private static final String CORRELATION_ID_MDC = "Correlation-ID";
    private Map<String, String> userInputData = new HashMap<>();
    private Map<String, Object> properties = new HashMap<>();
    private Map<String, String> authenticatorProperties;
    private Map<String, Set<String>> currentStepInputs = new HashMap<>();
    private Map<String, Set<String>> currentRequiredInputs = new HashMap<>();
    private List<NodeConfig> completedNodes = new ArrayList<>();
    private NodeConfig currentNode;
    private GraphConfig graphConfig;
    private FlowUser flowUser = new FlowUser();
    private String tenantDomain;
    private String contextIdentifier;
    private String currentActionId;
    private NodeResponse currentNodeNodeResponse;
    private String callbackUrl;
    private String portalUrl;
    private String applicationId;
    private String flowType;
    @JsonIgnore
    private ExternalIdPConfig externalIdPConfig;
    private boolean generateAuthenticationAssertion = false;

    public NodeConfig getCurrentNode() {

        return currentNode;
    }

    public void setCurrentNode(NodeConfig currentNode) {

        this.currentNode = currentNode;
    }

    public GraphConfig getGraphConfig() {

        return graphConfig;
    }

    public void setGraphConfig(GraphConfig graphConfig) {

        this.graphConfig = graphConfig;
    }

    public Map<String, String> getUserInputData() {

        return userInputData;
    }

    public void addUserInputData(String key, String value) {

        userInputData.put(key, value);
    }

    public void setUserInputData(Map<String, String> userInputData) {
        this.userInputData = userInputData;
    }

    public String getContextIdentifier() {

        return contextIdentifier;
    }

    public void setContextIdentifier(String contextIdentifier) {

        this.contextIdentifier = contextIdentifier;
    }

    public String getTenantDomain() {

        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {

        this.tenantDomain = tenantDomain;
    }

    public FlowUser getFlowUser() {

        return flowUser;
    }

    public void setFlowUser(FlowUser flowUser) {

        this.flowUser = flowUser;
    }

    public Map<String, Object> getProperties() {

        return properties;
    }

    public void addProperties(Map<String, Object> properties) {

        this.properties.putAll(properties);
    }

    public void setProperties(Map<String, Object> properties) {

        this.properties = properties;
    }

    public Object getProperty(String key) {

        return this.properties.get(key);
    }

    public void setProperty(String key, Object value) {

        this.properties.put(key, value);
    }

    public String getCurrentActionId() {

        return currentActionId;
    }

    public void setCurrentActionId(String currentActionId) {

        this.currentActionId = currentActionId;
    }

    @JsonIgnore
    public String getCorrelationId() {

        return Optional.ofNullable(MDC.get(CORRELATION_ID_MDC)).orElse("");
    }

    public Map<String, String> getAuthenticatorProperties() {

        return authenticatorProperties;
    }

    public void setAuthenticatorProperties(Map<String, String> authenticatorProperties) {

        this.authenticatorProperties = authenticatorProperties;
    }

    public ExternalIdPConfig getExternalIdPConfig() {

        return externalIdPConfig;
    }

    public void setExternalIdPConfig(
            ExternalIdPConfig externalIdPConfig) {

        this.externalIdPConfig = externalIdPConfig;
    }

    public Map<String, Set<String>> getCurrentStepInputs() {

        return currentStepInputs;
    }

    public void setCurrentStepInputs(Map<String, Set<String>> currentExpectedInputs) {

        this.currentStepInputs = currentExpectedInputs;
    }

    public Map<String, Set<String>> getCurrentRequiredInputs() {

        return currentRequiredInputs;
    }

    public void setCurrentRequiredInputs(Map<String, Set<String>> currentRequiredInputs) {

        this.currentRequiredInputs = currentRequiredInputs;
    }

    public List<NodeConfig> getCompletedNodes() {

        return completedNodes;
    }

    public void addCompletedNode(NodeConfig nodeConfig) {

        this.completedNodes.add(nodeConfig);
    }

    public void setCompletedNodes(List<NodeConfig> completedNodes) {

        this.completedNodes = completedNodes;
    }

    public NodeResponse getCurrentNodeResponse() {

        return currentNodeNodeResponse;
    }

    public void setCurrentNodeResponse(NodeResponse nodeResponse) {

        this.currentNodeNodeResponse = nodeResponse;
    }

    public String getCallbackUrl() {

        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {

        this.callbackUrl = callbackUrl;
    }

    public String getApplicationId() {

        return applicationId;
    }

    public void setApplicationId(String applicationId) {

        this.applicationId = applicationId;
    }

    public String getFlowType() {

        return flowType;
    }

    public void setFlowType(String flowType) {

        this.flowType = flowType;
    }

    public String getPortalUrl() {

        return portalUrl;
    }

    public void setPortalUrl(String portalUrl) {

        this.portalUrl = portalUrl;
    }

    public boolean isGenerateAuthenticationAssertion() {

        return generateAuthenticationAssertion;
    }

    public void setGenerateAuthenticationAssertion(boolean generateAuthenticationAssertion) {

        this.generateAuthenticationAssertion = generateAuthenticationAssertion;
    }
}
