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
 * This class models the Action Execution Response.
 * Action Execution Response is the response object that is returned by the Action Executor Service after executing an
 * action. It contains the action status and the operations that needs to be performed.
 */
public class ActionInvocationResponse {

    private int httpStatusCode;
    private String actionStatus;
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

        return "SUCCESS".equalsIgnoreCase(actionStatus);
    }

    public boolean isError() {

        return "ERROR".equalsIgnoreCase(actionStatus);
    }

    public boolean isRetry() {

        return retry;
    }

    public String getErrorLog() {

        return errorLog;
    }

    public interface APIResponse {

    }

    public static class Builder {

        private int httpStatusCode;
        private String actionStatus;
        private APIResponse response;
        private boolean retry;

        private String errorLog;

        public Builder setHttpStatusCode(int httpStatusCode) {

            this.httpStatusCode = httpStatusCode;
            return this;
        }

        public Builder setResponse(APIResponse response) {

            if (response instanceof ActionInvocationSuccessResponse) {
                this.actionStatus = "SUCCESS";
            } else if (response instanceof ActionInvocationErrorResponse) {
                this.actionStatus = "ERROR";
            }

            this.response = response;
            return this;
        }

        public Builder setRetry(boolean retry) {

            this.retry = retry;
            this.actionStatus = "ERROR";
            return this;
        }

        public Builder setErrorLog(String errorLog) {

            this.errorLog = errorLog;
            this.actionStatus = "ERROR";
            return this;
        }

        public ActionInvocationResponse build() {

            ActionInvocationResponse response = new ActionInvocationResponse();
            response.httpStatusCode = this.httpStatusCode;
            response.actionStatus = this.actionStatus;
            response.response = this.response;
            response.retry = this.retry;
            response.errorLog = this.errorLog;
            return response;
        }
    }
}
