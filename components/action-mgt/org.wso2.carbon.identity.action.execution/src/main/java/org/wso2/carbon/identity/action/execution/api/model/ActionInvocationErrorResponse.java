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
import org.apache.commons.lang.StringUtils;

import java.util.regex.Pattern;

/**
 * This class is used to represent the error response of an action invocation.
 */
@JsonDeserialize(builder = ActionInvocationErrorResponse.Builder.class)
public class ActionInvocationErrorResponse implements ActionInvocationResponse.APIResponse {

    private final ActionInvocationResponse.Status actionStatus;
    private final String errorMessage;
    private final String errorDescription;

    private ActionInvocationErrorResponse(Builder builder) {

        this.actionStatus = builder.actionStatus;
        this.errorMessage = builder.errorMessage;
        this.errorDescription = builder.errorDescription;
    }

    public ActionInvocationResponse.Status getActionStatus() {

        return actionStatus;
    }

    public String getErrorMessage() {

        return errorMessage;
    }

    public String getErrorDescription() {

        return errorDescription;
    }

    /**
     * This class is used to build the {@link ActionInvocationErrorResponse}.
     */
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {

        private ActionInvocationResponse.Status actionStatus;
        private String errorMessage;
        private String errorDescription;

        private static final Pattern ERROR_VALIDATION_PATTERN =
                Pattern.compile("^[a-zA-Z0-9\\s\\-_.!?;:'()\\[\\]]{1,100}$");
        private static final Pattern ERROR_DESCRIPTION_VALIDATION_PATTERN =
                Pattern.compile("^[a-zA-Z0-9\\s\\-_.!?;:'()\\[\\]]{1,300}$");

        @JsonProperty("actionStatus")
        public Builder actionStatus(ActionInvocationResponse.Status actionStatus) {

            this.actionStatus = actionStatus;
            return this;
        }

        @JsonProperty("errorMessage")
        public Builder errorMessage(String error) {

            this.errorMessage = error;
            return this;
        }

        @JsonProperty("errorDescription")
        public Builder errorDescription(String errorDescription) {

            this.errorDescription = errorDescription;
            return this;
        }

        public ActionInvocationErrorResponse build() {

            if (actionStatus == null) {
                throw new IllegalArgumentException("actionStatus must not be null.");
            }

            if (!ActionInvocationResponse.Status.ERROR.equals(actionStatus)) {
                throw new IllegalArgumentException("actionStatus must be ERROR.");
            }

            if (StringUtils.isEmpty(errorMessage)) {
                throw new IllegalArgumentException("errorMessage cannot be null or empty.");
            }

            if (!ERROR_VALIDATION_PATTERN.matcher(errorMessage).matches()) {
                throw new IllegalArgumentException("Invalid errorMessage format.");
            }

            if (StringUtils.isNotEmpty(errorDescription) && !ERROR_DESCRIPTION_VALIDATION_PATTERN
                    .matcher(errorDescription).matches()) {
                throw new IllegalArgumentException("Invalid errorDescription format.");
            }

            return new ActionInvocationErrorResponse(this);
        }
    }
}
