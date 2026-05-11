/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

import org.wso2.carbon.identity.flow.mgt.model.NodeConfig;

/**
 * This class is responsible for holding the flow event context.
 */
public class FlowEventContext {

    private final String contextIdentifier;
    private final String flowType;
    private final String applicationId;
    private final String tenantDomain;
    private final NodeConfig currentNode;
    private final String userId;
    private final String errorCode;
    private final FlowExecutionStep step;
    private final NodeResponse currentNodeResponse;

    private FlowEventContext(Builder builder) {

        this.contextIdentifier = builder.contextIdentifier;
        this.flowType = builder.flowType;
        this.applicationId = builder.applicationId;
        this.tenantDomain = builder.tenantDomain;
        this.currentNode = builder.currentNode;
        this.userId = builder.userId;
        this.errorCode = builder.errorCode;
        this.step = builder.step;
        this.currentNodeResponse = builder.currentNodeResponse;
    }

    /**
     * Get the context identifier.
     *
     * @return Context identifier.
     */
    public String getContextIdentifier() {

        return contextIdentifier;
    }

    /**
     * Get the flow type.
     *
     * @return Flow type.
     */
    public String getFlowType() {

        return flowType;
    }

    /**
     * Get the application ID.
     *
     * @return Application ID.
     */
    public String getApplicationId() {

        return applicationId;
    }

    /**
     * Get the tenant domain.
     *
     * @return Tenant domain.
     */
    public String getTenantDomain() {

        return tenantDomain;
    }

    /**
     * Get the current node configuration.
     *
     * @return Current node configuration.
     */
    public NodeConfig getCurrentNode() {

        return currentNode;
    }

    /**
     * Get the user ID.
     *
     * @return User ID.
     */
    public String getUserId() {

        return userId;
    }

    /**
     * Get the error code.
     *
     * @return Error code.
     */
    public String getErrorCode() {

        return errorCode;
    }

    /**
     * Get the flow execution step.
     *
     * @return Flow execution step.
     */
    public FlowExecutionStep getStep() {

        return step;
    }

    /**
     * Get the current node response.
     *
     * @return Current node response.
     */
    public NodeResponse getCurrentNodeResponse() {

        return currentNodeResponse;
    }

    /**
     * Builder for FlowEventContext.
     */
    public static class Builder {

        private String contextIdentifier;
        private String flowType;
        private String applicationId;
        private String tenantDomain;
        private NodeConfig currentNode;
        private String userId;
        private String errorCode;
        private FlowExecutionStep step;
        private NodeResponse currentNodeResponse;

        /**
         * Set the context identifier.
         *
         * @param contextIdentifier Context identifier.
         * @return Builder.
         */
        public Builder contextIdentifier(String contextIdentifier) {

            this.contextIdentifier = contextIdentifier;
            return this;
        }

        /**
         * Set the flow type.
         *
         * @param flowType Flow type.
         * @return Builder.
         */
        public Builder flowType(String flowType) {

            this.flowType = flowType;
            return this;
        }

        /**
         * Set the application ID.
         *
         * @param applicationId Application ID.
         * @return Builder.
         */
        public Builder applicationId(String applicationId) {

            this.applicationId = applicationId;
            return this;
        }

        /**
         * Set the tenant domain.
         *
         * @param tenantDomain Tenant domain.
         * @return Builder.
         */
        public Builder tenantDomain(String tenantDomain) {

            this.tenantDomain = tenantDomain;
            return this;
        }

        /**
         * Set the current node configuration.
         *
         * @param currentNode Current node configuration.
         * @return Builder.
         */
        public Builder currentNode(NodeConfig currentNode) {

            this.currentNode = currentNode;
            return this;
        }

        /**
         * Set the user ID.
         *
         * @param userId User ID.
         * @return Builder.
         */
        public Builder userId(String userId) {

            this.userId = userId;
            return this;
        }

        /**
         * Set the error code.
         *
         * @param errorCode Error code.
         * @return Builder.
         */
        public Builder errorCode(String errorCode) {

            this.errorCode = errorCode;
            return this;
        }

        /**
         * Set the flow execution step.
         *
         * @param step Flow execution step.
         * @return Builder.
         */
        public Builder step(FlowExecutionStep step) {

            this.step = step;
            return this;
        }

        /**
         * Set the current node response.
         *
         * @param currentNodeResponse Current node response.
         * @return Builder.
         */
        public Builder currentNodeResponse(NodeResponse currentNodeResponse) {

            this.currentNodeResponse = currentNodeResponse;
            return this;
        }

        /**
         * Build the FlowEventContext.
         *
         * @return FlowEventContext.
         */
        public FlowEventContext build() {

            return new FlowEventContext(this);
        }
    }
}
