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

/**
 * This class is used to represent the response of an action invocation.
 * The response can be either a success or an error.
 */
public class ActionInvocationResponse {

    private Status actionStatus;
    private APIResponse response;

    private boolean retry;

    private String errorLog;

    private ActionInvocationResponse() {
        // Private constructor to enforce the use of the Builder
    }

    public APIResponse getResponse() {

        return response;
    }

    public boolean isSuccess() {

        return Status.SUCCESS.equals(actionStatus);
    }

    public boolean isError() {

        return Status.ERROR.equals(actionStatus);
    }

    public boolean isRetry() {

        return retry;
    }

    public String getErrorLog() {

        return errorLog;
    }

    /**
     * Defines action invocation status.
     */
    public enum Status {
        SUCCESS,
        ERROR
    }

    /**
     * This interface defines the response of the API call.
     */
    public interface APIResponse {

    }

    /**
     * This class is used to build the {@link ActionInvocationResponse}.
     */
    public static class Builder {

        private Status actionStatus;
        private APIResponse response;
        private boolean retry;

        private String errorLog;

        public Builder setResponse(APIResponse response) {

            if (response instanceof ActionInvocationSuccessResponse) {
                this.actionStatus = Status.SUCCESS;
            } else if (response instanceof ActionInvocationErrorResponse) {
                this.actionStatus = Status.ERROR;
            }

            this.response = response;
            return this;
        }

        public Builder setRetry(boolean retry) {

            this.retry = retry;
            this.actionStatus = Status.ERROR;
            return this;
        }

        public Builder setErrorLog(String errorLog) {

            this.errorLog = errorLog;
            this.actionStatus = Status.ERROR;
            return this;
        }

        public ActionInvocationResponse build() {

            ActionInvocationResponse response = new ActionInvocationResponse();
            response.actionStatus = this.actionStatus;
            response.response = this.response;
            response.retry = this.retry;
            response.errorLog = this.errorLog;
            return response;
        }
    }
}
