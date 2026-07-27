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

package org.wso2.carbon.identity.action.execution.api.model;

import org.slf4j.MDC;

import java.util.List;
import java.util.Optional;

/**
 * This class models the Action Execution Request.
 * Action Execution Request is the request object that is passed to the Action Executor Service to execute an action.
 * It contains the {@link ActionType}, flow id, {@link Event} and a list of {@link AllowedOperation}.
 */
public class ActionExecutionRequest {

    private static final String CORRELATION_ID_MDC = "Correlation-ID";
    private final ActionType actionType;
    private final String flowId;
    private final Event event;
    private final List<AllowedOperation> allowedOperations;

    public ActionExecutionRequest(Builder builder) {

        this.actionType = builder.actionType;
        this.flowId = builder.flowId;
        this.event = builder.event;
        this.allowedOperations = builder.allowedOperations;
    }

    public ActionType getActionType() {

        return actionType;
    }

    public String getFlowId() {

        return flowId;
    }

    public String getRequestId() {

        return getCorrelationId();
    }

    public Event getEvent() {

        return event;
    }

    public List<AllowedOperation> getAllowedOperations() {

        return allowedOperations;
    }

    private String getCorrelationId() {

        return Optional.ofNullable(MDC.get(CORRELATION_ID_MDC)).orElse("");
    }

    /**
     * Builder for the {@link ActionExecutionRequest}.
     */
    public static class Builder {

        private ActionType actionType;
        private String flowId;
        private Event event;
        private List<AllowedOperation> allowedOperations;

        public Builder actionType(ActionType actionType) {

            this.actionType = actionType;
            return this;
        }

        public Builder flowId(String flowId) {

            this.flowId = flowId;
            return this;
        }

        public Builder event(Event event) {

            this.event = event;
            return this;
        }

        public Builder allowedOperations(List<AllowedOperation> allowedOperations) {

            this.allowedOperations = allowedOperations;
            return this;
        }

        public ActionExecutionRequest build() {

            return new ActionExecutionRequest(this);
        }
    }
}

