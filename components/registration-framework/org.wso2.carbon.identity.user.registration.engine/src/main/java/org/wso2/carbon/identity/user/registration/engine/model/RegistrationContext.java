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

package org.wso2.carbon.identity.user.registration.engine.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.MDC;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ExternalIdPConfig;
import org.wso2.carbon.identity.user.registration.mgt.model.NodeConfig;
import org.wso2.carbon.identity.user.registration.mgt.model.RegistrationGraphConfig;

/**
 * This class is responsible for holding the registration context.
 */
public class RegistrationContext implements Serializable {

    private static final long serialVersionUID = 542871476395078667L;
    private static final String CORRELATION_ID_MDC = "Correlation-ID";
    private final Map<String, String> userInputData = new HashMap<>();
    private final Map<String, Object> properties = new HashMap<>();
    private Map<String, String> authenticatorProperties;
    private NodeConfig currentNode;
    private RegistrationGraphConfig regGraph;
    private RegisteringUser registeringUser = new RegisteringUser();
    private String tenantDomain;
    private String contextIdentifier;
    private String userId;
    private String currentActionId;
    private ExternalIdPConfig externalIdPConfig;

    public NodeConfig getCurrentNode() {

        return currentNode;
    }

    public void setCurrentNode(NodeConfig currentNode) {

        this.currentNode = currentNode;
    }

    public RegistrationGraphConfig getRegGraph() {

        return regGraph;
    }

    public void setRegGraph(RegistrationGraphConfig regGraph) {

        this.regGraph = regGraph;
    }

    public Map<String, String> getUserInputData() {

        return userInputData;
    }

    public void addUserInputData(String key, String value) {

        userInputData.put(key, value);
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

    public RegisteringUser getRegisteringUser() {

        return registeringUser;
    }

    public void setRegisteringUser(RegisteringUser registeringUser) {

        this.registeringUser = registeringUser;
    }

    public Map<String, Object> getProperties() {

        return properties;
    }

    public void addProperties(Map<String, Object> properties) {

        this.properties.putAll(properties);
    }

    public Object getProperty(String key) {

        return this.properties.get(key);
    }

    public void setProperty(String key, Object value) {

        this.properties.put(key, value);
    }

    public String getUserId() {

        return userId;
    }

    public void setUserId(String userId) {

        this.userId = userId;
    }

    public String getCurrentActionId() {

        return currentActionId;
    }

    public void setCurrentActionId(String currentActionId) {

        this.currentActionId = currentActionId;
    }

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
}
