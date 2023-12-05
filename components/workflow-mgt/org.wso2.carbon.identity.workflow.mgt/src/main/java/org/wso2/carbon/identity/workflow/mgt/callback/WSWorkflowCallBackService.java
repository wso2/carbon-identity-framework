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

package org.wso2.carbon.identity.workflow.mgt.callback;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.workflow.mgt.WorkFlowExecutorManager;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This is the Callback service for the WS Workflow requests. Once workflow executor completes its workflow,
 * it will call back this service with the results.
 */
public class WSWorkflowCallBackService {

    private static final Log log = LogFactory.getLog(WSWorkflowCallBackService.class);

    /**
     * The callback operation to be called on the completion of the workflow executor service
     *
     * @param response  Response received from workflow engine
     */
    public void onCallback(WSWorkflowResponse response) {
        if (response != null) {
            Map<String, Object> outputParams;
            if (response.getOutputParams() != null) {
                outputParams = new HashMap<>(response.getOutputParams().length);
                for (WSParameter parameter : response.getOutputParams()) {
                    outputParams.put(parameter.getName(), parameter.getValue());
                }
            } else {
                outputParams = Collections.emptyMap();
            }
            try {
                WorkFlowExecutorManager.getInstance()
                        .handleCallback(response.getUuid(), response.getStatus(), outputParams);
            } catch (WorkflowException e) {
                log.error("Error when handling callback for the workflow, id: " + response.getUuid() + ", status:" +
                        response.getStatus(), e);
            }
        }
    }
}
