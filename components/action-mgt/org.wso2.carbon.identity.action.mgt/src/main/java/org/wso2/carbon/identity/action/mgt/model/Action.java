/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.action.mgt.model;

/**
 * Action.
 */
public class Action  {

    private String id;
    private TypeEnums.ActionTypes type;
    private String name;
    private String description;

    /**
     * Action Status Enum.
     */
    public enum Status {

        ACTIVE("ACTIVE"),
        INACTIVE("INACTIVE");

        private final String value;

        Status(String v) {
            this.value = v;
        }

        public String value() {
            return value;
        }

        public static Status fromValue(String value) {
            for (Status b : Status.values()) {
                if (b.value.equals(value)) {
                    return b;
                }
            }
            throw new IllegalArgumentException("Unexpected value '" + value + "'");
        }
    }

    private Status status;
    private EndpointConfig endpointConfig;

    public Action() {
    }

    public Action(ActionResponseBuilder actionResponseBuilder) {

        this.id = actionResponseBuilder.id;
        this.type = actionResponseBuilder.type;
        this.name = actionResponseBuilder.name;
        this.description = actionResponseBuilder.description;
        this.status = actionResponseBuilder.status;
        this.endpointConfig = actionResponseBuilder.endpointConfig;
    }

    public Action(ActionRequestBuilder actionRequestBuilder) {

        this.name = actionRequestBuilder.name;
        this.description = actionRequestBuilder.description;
        this.endpointConfig = actionRequestBuilder.endpointConfig;
    }

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public TypeEnums.ActionTypes getType() {

        return type;
    }

    public void setType(TypeEnums.ActionTypes type) {

        this.type = type;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    public Status getStatus() {

        return status;
    }

    public void setStatus(Status status) {

        this.status = status;
    }

    public EndpointConfig getEndpoint() {

        return endpointConfig;
    }

    public void setEndpoint(EndpointConfig endpointConfig) {

        this.endpointConfig = endpointConfig;
    }

    /**
     * ActionResponseBuilder.
     */
    public static class ActionResponseBuilder {

        private String id;
        private TypeEnums.ActionTypes type;
        private String name;
        private String description;
        private Status status;
        private EndpointConfig endpointConfig;

        public ActionResponseBuilder() {
        }

        public ActionResponseBuilder id(String id) {

            this.id = id;
            return this;
        }

        public ActionResponseBuilder type(TypeEnums.ActionTypes type) {

            this.type = type;
            return this;
        }

        public ActionResponseBuilder name(String name) {

            this.name = name;
            return this;
        }

        public ActionResponseBuilder description(String description) {

            this.description = description;
            return this;
        }

        public ActionResponseBuilder status(Status status) {

            this.status = status;
            return this;
        }

        public ActionResponseBuilder endpoint(EndpointConfig endpointConfig) {

            this.endpointConfig = endpointConfig;
            return this;
        }

        public Action build() {

            return new Action(this);
        }
    }

    /**
     * ActionRequestBuilder.
     */
    public static class ActionRequestBuilder {

        private String name;
        private String description;
        private EndpointConfig endpointConfig;

        public ActionRequestBuilder() {
        }

        public ActionRequestBuilder name(String name) {

            this.name = name;
            return this;
        }

        public ActionRequestBuilder description(String description) {

            this.description = description;
            return this;
        }

        public ActionRequestBuilder endpoint(EndpointConfig endpointConfig) {

            this.endpointConfig = endpointConfig;
            return this;
        }

        public Action build() {

            return new Action(this);
        }
    }
}
