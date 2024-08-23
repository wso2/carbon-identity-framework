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

package org.wso2.carbon.identity.action.execution.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * This class is used to represent the failure response of an action invocation.
 * This response will contain the failure reason and the failure description.
 */
@JsonDeserialize(builder = ActionInvocationFailureResponse.Builder.class)
public class ActionInvocationFailureResponse implements ActionInvocationResponse.APIResponse {

    private final ActionInvocationResponse.Status actionStatus;
    private final String failureReason;
    private final String failureDescription;

    private ActionInvocationFailureResponse(ActionInvocationFailureResponse.Builder builder) {

        this.actionStatus = builder.actionStatus;
        this.failureReason = builder.failureReason;
        this.failureDescription = builder.failureDescription;
    }

    public ActionInvocationResponse.Status getActionStatus() {

        return actionStatus;
    }

    public String getFailureReason() {

        return failureReason;
    }

    public String getFailureDescription() {

        return failureDescription;
    }

    /**
     * This class is used to build the {@link ActionInvocationFailureResponse}.
     */
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {

        private ActionInvocationResponse.Status actionStatus;
        private String failureReason;
        private String failureDescription;

        @JsonProperty("actionStatus")
        public ActionInvocationFailureResponse.Builder actionStatus(ActionInvocationResponse.Status actionStatus) {

            this.actionStatus = actionStatus;
            return this;
        }

        @JsonProperty("failureReason")
        public ActionInvocationFailureResponse.Builder failureReason(String failureReason) {

            this.failureReason = failureReason;
            return this;
        }

        @JsonProperty("failureDescription")
        public ActionInvocationFailureResponse.Builder failureDescription(String failureDescription) {

            this.failureDescription = failureDescription;
            return this;
        }

        public ActionInvocationFailureResponse build() {

            if (actionStatus == null) {
                throw new IllegalArgumentException("The actionStatus must not be null.");
            }

            if (!ActionInvocationResponse.Status.FAILURE.equals(actionStatus)) {
                throw new IllegalArgumentException("The actionStatus must be failureReason.");
            }

            if (failureReason == null || failureReason.isEmpty()) {
                throw new IllegalArgumentException("The failureReason cannot be null or empty.");
            }

            return new ActionInvocationFailureResponse(this);
        }
    }
}
