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

    }

    public APIResponse getResponse() {

        return response;
    }

    public boolean isSuccess() {

        return Status.SUCCESS.equals(actionStatus);
    }

    public boolean isIncomplete() {

        return Status.INCOMPLETE.equals(actionStatus);
    }

    public boolean isFailure() {

        return Status.FAILED.equals(actionStatus);
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
        INCOMPLETE,
        FAILED,
        ERROR
    }

    /**
     * This interface defines the response of the API call.
     */
    public interface APIResponse {

        Status getActionStatus();

    }

    /**
     * This class is used to build the {@link ActionInvocationResponse}.
     */
    public static class Builder {

        private Status actionStatus;
        private APIResponse response;
        private boolean retry;

        private String errorLog;

        public Builder response(APIResponse response) {

            this.actionStatus = response.getActionStatus();
            this.response = response;
            return this;
        }

        public Builder retry(boolean retry) {

            this.retry = retry;
            this.actionStatus = Status.ERROR;
            return this;
        }

        public Builder errorLog(String errorLog) {

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
