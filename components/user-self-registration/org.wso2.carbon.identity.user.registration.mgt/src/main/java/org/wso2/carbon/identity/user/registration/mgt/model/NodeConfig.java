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

package org.wso2.carbon.identity.user.registration.mgt.model;

import java.util.ArrayList;
import java.util.List;

public class NodeConfig {

    private final List<NodeEdge> edges = new ArrayList<>();
    private int id;
    private String uuid;
    private String type;
    private String triggeredActionId;
    private boolean isFirstNode;
    private String nextNodeId = null;
    private String previousNodeId = null;
    private List<String> nextNodeIds = new ArrayList<>();
    private ExecutorDTO executorConfig = null;

    public int getId() {

        return id;
    }

    public void setId(int id) {

        this.id = id;
    }

    public String getUuid() {

        return uuid;
    }

    public void setUuid(String uuid) {

        this.uuid = uuid;
    }

    public String getType() {

        return type;
    }

    public void setType(String type) {

        this.type = type;
    }

    public boolean isFirstNode() {

        return isFirstNode;
    }

    public void setFirstNode(boolean firstNode) {

        isFirstNode = firstNode;
    }

    public List<String> getNextNodeIds() {

        return nextNodeIds;
    }

    public void setNextNodeIds(List<String> nextNodeIds) {

        this.nextNodeIds = nextNodeIds;
    }

    public void addNextNodeId(String nextNodeId) {

        this.nextNodeIds.add(nextNodeId);
    }

    public ExecutorDTO getExecutorConfig() {

        return executorConfig;
    }

    public void setExecutorConfig(ExecutorDTO executorConfig) {

        this.executorConfig = executorConfig;
    }

    public String getNextNodeId() {

        return nextNodeId;
    }

    public void setNextNodeId(String nextNodeId) {

        this.nextNodeId = nextNodeId;
    }

    public String getPreviousNodeId() {

        return previousNodeId;
    }

    public void setPreviousNodeId(String previousNodeId) {

        this.previousNodeId = previousNodeId;
    }

    public String getTriggeredActionId() {

        return triggeredActionId;
    }

    public void setTriggeredActionId(String triggeredActionId) {

        this.triggeredActionId = triggeredActionId;
    }

    public List<NodeEdge> getEdges() {

        return edges;
    }

    public void addEdge(NodeEdge edge) {

        this.edges.add(edge);
    }
}
