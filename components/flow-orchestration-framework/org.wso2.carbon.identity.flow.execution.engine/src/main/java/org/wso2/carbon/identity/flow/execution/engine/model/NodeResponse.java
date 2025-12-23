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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Model class to represent the response of a node in the flow sequence.
 */
@JsonDeserialize(builder = NodeResponse.Builder.class)
public class NodeResponse implements Serializable {

    private static final long serialVersionUID = 123456789L;
    private final List<String> requiredData;
    private final List<String> optionalData;
    private String status;
    private String type;
    private String error;
    private Map<String, String> additionalInfo;

    private NodeResponse(Builder builder) {

        this.status = builder.status;
        this.type = builder.type;
        this.error = builder.error;
        this.requiredData = builder.requiredData;
        this.additionalInfo = builder.additionalInfo;
        this.optionalData = builder.optionalData;
    }

    public String getStatus() {

        return status;
    }

    public void setStatus(String status) {

        this.status = status;
    }

    public List<String> getRequiredData() {

        return requiredData;
    }

    public List<String> getOptionalData() {

        return optionalData;
    }

    public Map<String, String> getAdditionalInfo() {

        return additionalInfo;
    }

    public void setAdditionalInfo(Map<String, String> additionalInfo) {

        this.additionalInfo = additionalInfo;
    }

    public String getType() {

        return type;
    }

    public void setType(String type) {

        this.type = type;
    }

    public String getError() {

        return error;
    }

    public void setError(String error) {

        this.error = error;
    }

    /**
     * Builder class to build {@link NodeResponse} objects.
     */
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {

        @JsonProperty("status")
        private String status;
        @JsonProperty("type")
        private String type;
        @JsonProperty("error")
        private String error;
        @JsonProperty("requiredData")
        private List<String> requiredData;
        @JsonProperty("optionalData")
        private List<String> optionalData;
        @JsonProperty("additionalInfo")
        private Map<String, String> additionalInfo;

        public Builder status(String status) {

            this.status = status;
            return this;
        }

        public Builder type(String type) {

            this.type = type;
            return this;
        }

        public Builder error(String error) {

            this.error = error;
            return this;
        }

        public Builder requiredData(List<String> requiredData) {

            this.requiredData = requiredData;
            return this;
        }

        public Builder optionalData(List<String> optionalData) {

            this.optionalData = optionalData;
            return this;
        }

        public Builder additionalInfo(Map<String, String> additionalInfo) {

            this.additionalInfo = additionalInfo;
            return this;
        }

        public NodeResponse build() {

            return new NodeResponse(this);
        }
    }
}
