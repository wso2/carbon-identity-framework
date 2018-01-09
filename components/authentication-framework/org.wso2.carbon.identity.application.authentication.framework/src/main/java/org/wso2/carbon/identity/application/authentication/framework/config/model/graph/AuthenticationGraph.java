/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph;

import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Authentication Graph.
 */
public class AuthenticationGraph implements Serializable {

    private static final long serialVersionUID = 7602728707258687636L;
    private String name;
    private AuthGraphNode startNode;
    private Map<Integer, StepConfig> stepMap = new HashMap<>();
    private boolean enabled;

    private boolean buildSuccessful = true;
    private String errorReason;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AuthGraphNode getStartNode() {
        return startNode;
    }

    public void setStartNode(AuthGraphNode startNode) {
        this.startNode = startNode;
    }

    public Map<Integer, StepConfig> getStepMap() {
        return stepMap;
    }

    public void setStepMap(Map<Integer, StepConfig> stepMap) {
        this.stepMap = stepMap;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isBuildSuccessful() {
        return buildSuccessful;
    }

    public void setBuildSuccessful(boolean buildSuccessful) {
        this.buildSuccessful = buildSuccessful;
    }

    public String getErrorReason() {
        return errorReason;
    }

    public void setErrorReason(String errorReason) {
        this.errorReason = errorReason;
    }
}
