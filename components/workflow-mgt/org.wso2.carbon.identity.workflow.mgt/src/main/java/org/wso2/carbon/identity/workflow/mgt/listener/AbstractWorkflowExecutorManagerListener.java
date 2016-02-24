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

package org.wso2.carbon.identity.workflow.mgt.listener;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.core.model.IdentityEventListenerConfig;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.workflow.mgt.WorkflowExecutorResult;
import org.wso2.carbon.identity.workflow.mgt.dto.WorkflowRequest;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowException;

import java.util.Map;

public abstract class AbstractWorkflowExecutorManagerListener implements WorkflowExecutorManagerListener {

    /**
     * Trigger before executing a workflow request
     *
     * @param workFlowRequest Details of request to execute
     * @throws WorkflowException
     */
    @Override
    public void doPreExecuteWorkflow(WorkflowRequest workFlowRequest) throws WorkflowException {

    }

    /**
     * Trigger after executing a workflow request
     *
     * @param workFlowRequest Details of request to execute
     * @param result          Result of the original operation
     * @throws WorkflowException
     */
    @Override
    public void doPostExecuteWorkflow(WorkflowRequest workFlowRequest, WorkflowExecutorResult result) throws
            WorkflowException {

    }

    /**
     * Trigger before handling a callback
     *
     * @param uuid             UUID to request which callback received
     * @param status           Status of call back
     * @param additionalParams Additional parameters required to execute request
     * @throws WorkflowException
     */
    @Override
    public void doPreHandleCallback(String uuid, String status, Map<String, Object> additionalParams) throws
            WorkflowException {

    }

    /**
     * Trigger after handling a callback
     *
     * @param uuid             UUID to request which callback received
     * @param status           Status of call back
     * @param additionalParams Additional parameters required to execute request
     * @throws WorkflowException
     */
    @Override
    public void doPostHandleCallback(String uuid, String status, Map<String, Object> additionalParams) throws
            WorkflowException {

    }

    /**
     * Check if listener is enabled or not.
     *
     * @return
     */
    public boolean isEnable() {
        IdentityEventListenerConfig listenerConfig = IdentityUtil.readEventListenerProperty
                (WorkflowExecutorManagerListener.class.getName(), this.getClass().getName());

        if (listenerConfig == null) {
            return true;
        }

        if (StringUtils.isNotBlank(listenerConfig.getEnable())) {
            return Boolean.parseBoolean(listenerConfig.getEnable());
        } else {
            return true;
        }
    }

    /**
     * get order ID (priority of current listener)
     *
     * @return
     */
    public int getOrderId() {
        IdentityEventListenerConfig listenerConfig = IdentityUtil.readEventListenerProperty
                (WorkflowExecutorManagerListener.class.getName(), this.getClass().getName());
        if (listenerConfig == null) {
            return IdentityCoreConstants.EVENT_LISTENER_ORDER_ID;
        }
        return listenerConfig.getOrder();
    }
}
