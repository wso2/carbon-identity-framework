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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for holding the node configuration.
 */
@JsonDeserialize(builder = NodeConfig.Builder.class)
public class NodeConfig implements Serializable {

    private static final long serialVersionUID = 1L;
    private List<NodeEdge> edges = new ArrayList<>();
    private String id;
    private String type;
    private boolean isFirstNode;
    private String nextNodeId;
    private String previousNodeId;
    private ExecutorDTO executorConfig;

    public NodeConfig(Builder builder) {

        this.id = builder.id;
        this.type = builder.type;
        this.isFirstNode = builder.isFirstNode;
        this.nextNodeId = builder.nextNodeId;
        this.previousNodeId = builder.previousNodeId;
        this.executorConfig = builder.executorConfig;
        this.edges.addAll(builder.edges);
    }

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
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

    public List<NodeEdge> getEdges() {

        return edges;
    }

    public void addEdge(NodeEdge edge) {

        this.edges.add(edge);
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {

        @JsonProperty("id")
        private String id;

        @JsonProperty("type")
        private String type;

        // Matches "firstNode" in JSON, which Jackson derives from the "isFirstNode" getter.
        @JsonProperty("firstNode")
        private boolean isFirstNode;

        @JsonProperty("nextNodeId")
        private String nextNodeId;

        @JsonProperty("previousNodeId")
        private String previousNodeId;

        @JsonProperty("executorConfig")
        private ExecutorDTO executorConfig;

        @JsonProperty("edges")
        private List<NodeEdge> edges = new ArrayList<>();

        public Builder id(String id) {

            this.id = id;
            return this;
        }

        public Builder type(String type) {

            this.type = type;
            return this;
        }

        public Builder isFirstNode(boolean isFirstNode) {

            this.isFirstNode = isFirstNode;
            return this;
        }

        public Builder nextNodeId(String nextNodeId) {

            this.nextNodeId = nextNodeId;
            return this;
        }

        public Builder previousNodeId(String previousNodeId) {

            this.previousNodeId = previousNodeId;
            return this;
        }

        public Builder executorConfig(ExecutorDTO executorConfig) {

            this.executorConfig = executorConfig;
            return this;
        }

        public Builder edges(List<NodeEdge> edges) {

            this.edges.addAll(edges);
            return this;
        }

        public NodeConfig build() {

            return new NodeConfig(this);
        }
    }
}
