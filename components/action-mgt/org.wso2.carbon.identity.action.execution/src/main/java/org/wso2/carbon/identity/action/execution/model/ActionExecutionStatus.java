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

import java.util.Map;

/**
 * This class models the Action Execution Status.
 * Action Execution Status is the status object that is returned by the Action Executor Service after executing an
 * action. It contains the status of the action execution and the response context.
 */
public class ActionExecutionStatus {

    private final Status status;
    private final Map<String, Object> responseContext;

    public ActionExecutionStatus(Status status, Map<String, Object> responseContext) {

        this.status = status;
        this.responseContext = responseContext;
    }

    public Status getStatus() {

        return status;
    }

    public Map<String, Object> getResponseContext() {

        return responseContext;
    }

    /**
     * This enum defines the Action Execution Status.
     */
    public enum Status {
        SUCCESS,
        FAILURE,
        INCOMPLETE,
        ERROR
    }
}
