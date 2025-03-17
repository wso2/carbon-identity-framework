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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import org.wso2.carbon.identity.action.execution.internal.service.impl.ResponseDataDeserializer;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to represent the success response of an action invocation.
 * This response will contain the list of operations that need to be performed.
 */
@JsonDeserialize(builder = ActionInvocationSuccessResponse.Builder.class)
public class ActionInvocationSuccessResponse implements ActionInvocationResponse.APIResponse {

    private final ActionInvocationResponse.Status actionStatus;
    private final List<PerformableOperation> operations;
    private final ResponseData data;

    private ActionInvocationSuccessResponse(Builder builder) {

        this.actionStatus = builder.actionStatus;
        this.operations = builder.operations;
        this.data = builder.data;
    }

    @Override
    public ActionInvocationResponse.Status getActionStatus() {

        return actionStatus;
    }

    public List<PerformableOperation> getOperations() {

        return operations;
    }

    public ResponseData getData() {

        return data;
    }

    /**
     * This class is used to build the {@link ActionInvocationSuccessResponse}.
     */
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {

        private ActionInvocationResponse.Status actionStatus;
        private List<PerformableOperation> operations = new ArrayList<>();
        private ResponseData data;

        @JsonProperty("actionStatus")
        public Builder actionStatus(ActionInvocationResponse.Status actionStatus) {

            this.actionStatus = actionStatus;
            return this;
        }

        @JsonProperty("operations")
        public Builder operations(@JsonProperty("operations") List<PerformableOperation> operations) {

            this.operations = operations;
            return this;
        }

        @JsonDeserialize(using = ResponseDataDeserializer.class)
        @JsonProperty("data")
        public Builder responseData(@JsonProperty("data") ResponseData data) {

            this.data = data;
            return this;
        }

        public ActionInvocationSuccessResponse build() {

            if (this.actionStatus == null) {
                throw new IllegalArgumentException("actionStatus must not be null.");
            }

            if (!ActionInvocationResponse.Status.SUCCESS.equals(actionStatus)) {
                throw new IllegalArgumentException("actionStatus must be SUCCESS.");
            }

            return new ActionInvocationSuccessResponse(this);
        }
    }
}
