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

package org.wso2.carbon.identity.flow.mgt.model;

import java.io.Serializable;

/**
 * DTO class for NodeEdge in the graph.
 */
public class NodeEdge implements Serializable {

    private static final long serialVersionUID = 1L;
    String sourceNodeId;
    String targetNodeId;
    String triggeringActionId;

    public NodeEdge() {

    }

    public NodeEdge(String sourceNodeId, String targetNodeId, String triggeringActionId) {

        this.sourceNodeId = sourceNodeId;
        this.targetNodeId = targetNodeId;
        this.triggeringActionId = triggeringActionId;
    }

    public String getSourceNodeId() {

        return sourceNodeId;
    }

    public void setSourceNodeId(String sourceNodeId) {

        this.sourceNodeId = sourceNodeId;
    }

    public String getTargetNodeId() {

        return targetNodeId;
    }

    public void setTargetNodeId(String targetNodeId) {

        this.targetNodeId = targetNodeId;
    }

    public String getTriggeringActionId() {

        return triggeringActionId;
    }

    public void setTriggeringActionId(String triggeringActionId) {

        this.triggeringActionId = triggeringActionId;
    }
}
