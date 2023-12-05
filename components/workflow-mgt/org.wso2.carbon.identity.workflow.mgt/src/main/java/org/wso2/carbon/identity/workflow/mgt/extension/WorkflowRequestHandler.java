/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.workflow.mgt.extension;

import org.wso2.carbon.identity.workflow.mgt.dto.WorkflowRequest;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowException;

import java.util.Map;

public interface WorkflowRequestHandler {

    /**
     * Creates the workflow request for an event and engage it.
     *
     * @param workFlowRequest
     * @throws WorkflowException
     */
    void engageWorkflow(WorkflowRequest workFlowRequest) throws WorkflowException;

    /**
     * Gets the event that this handler is subscribed, used when handling the callback
     *
     * @return
     */
    String getEventId();

    /**
     * Called on workflow completion
     *
     * @param status          The workflow status
     * @param originalRequest The original request that was made by
     *                        {@link #engageWorkflow(WorkflowRequest)}
     * @param responseParams  Additional data returned from workflow
     * @throws WorkflowException
     */
    void onWorkflowCompletion(String status, WorkflowRequest originalRequest, Map<String, Object> responseParams) throws
            WorkflowException;

    /**
     * Returns the parameter names and their types. Will be used in input validation and in UIs.
     *
     * @return
     */
    Map<String, String> getParamDefinitions();

    /**
     * Gets the human friendly name for the event associated with this handler
     * @return
     */
    String getFriendlyName();

    /**
     * Gets the human friendly description for the event associated with this handler
     * @return
     */
    String getDescription();

    /**
     * Gets the category of the event associated with this handler
     * @return
     */
    String getCategory();

}
