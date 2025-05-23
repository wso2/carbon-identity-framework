/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.workflow.mgt;

import org.wso2.carbon.identity.workflow.mgt.util.ExecutorResultState;

import java.io.Serializable;

/**
 * WorkflowExecutorResult for return result of the workflow execution.
 */
public class WorkflowExecutorResult implements Serializable {

    private static final long serialVersionUID = 578423481187017212L;
    private ExecutorResultState executorResultState;
    private String message;

    public WorkflowExecutorResult() {

    }

    public WorkflowExecutorResult(ExecutorResultState executorResultState) {

        this.executorResultState = executorResultState;
    }

    public WorkflowExecutorResult(ExecutorResultState executorResultState, String message) {

        this.message = message;
        this.executorResultState = executorResultState;
    }

    public ExecutorResultState getExecutorResultState() {

        return executorResultState;
    }

    public void setExecutorResultState(ExecutorResultState executorResultState) {

        this.executorResultState = executorResultState;
    }

    public String getMessage() {

        return message;
    }

    public void setMessage(String message) {

        this.message = message;
    }
}
