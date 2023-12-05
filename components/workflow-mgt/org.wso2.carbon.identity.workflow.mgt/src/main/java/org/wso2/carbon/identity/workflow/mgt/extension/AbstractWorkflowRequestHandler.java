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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.workflow.mgt.WorkFlowExecutorManager;
import org.wso2.carbon.identity.workflow.mgt.WorkflowExecutorResult;
import org.wso2.carbon.identity.workflow.mgt.bean.Entity;
import org.wso2.carbon.identity.workflow.mgt.bean.RequestParameter;
import org.wso2.carbon.identity.workflow.mgt.dto.WorkflowRequest;
import org.wso2.carbon.identity.workflow.mgt.exception.InternalWorkflowException;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowException;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowRuntimeException;
import org.wso2.carbon.identity.workflow.mgt.internal.WorkflowServiceDataHolder;
import org.wso2.carbon.identity.workflow.mgt.util.ExecutorResultState;
import org.wso2.carbon.identity.workflow.mgt.util.WFConstant;
import org.wso2.carbon.identity.workflow.mgt.util.WorkflowDataType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractWorkflowRequestHandler implements WorkflowRequestHandler {

    /**
     * Used to skip the workflow execution on the successive call after workflow completion.
     */
    private static ThreadLocal<Boolean> workFlowCompleted = new ThreadLocal<Boolean>();

    private static final  Log log = LogFactory.getLog(AbstractWorkflowRequestHandler.class);
    public static void unsetWorkFlowCompleted() {

        AbstractWorkflowRequestHandler.workFlowCompleted.remove();
    }
    public static Boolean getWorkFlowCompleted() {

        return workFlowCompleted.get();
    }
    public static void setWorkFlowCompleted(Boolean workFlowCompleted) {

        AbstractWorkflowRequestHandler.workFlowCompleted.set(workFlowCompleted);
    }

    /**
     * Start a new workflow.
     *
     * @param wfParams    Parameters related to workflow
     * @param nonWfParams Other parameters
     * @return
     * @throws WorkflowException
     */
    public WorkflowExecutorResult startWorkFlow(Map<String, Object> wfParams, Map<String, Object> nonWfParams)
            throws WorkflowException {

        return startWorkFlow(wfParams, nonWfParams, null);
    }

    /**
     * Start a new workflow.
     *
     * @param wfParams    Parameters related to workflow
     * @param nonWfParams Other parameters
     * @param uuid        Unique ID of request
     * @return
     * @throws WorkflowException
     */
    public WorkflowExecutorResult startWorkFlow(Map<String, Object> wfParams, Map<String, Object> nonWfParams, String uuid)
            throws WorkflowException {

        if (isWorkflowCompleted()) {
            return new WorkflowExecutorResult(ExecutorResultState.COMPLETED);
        }
        if (!isAssociated()) {
            return new WorkflowExecutorResult(ExecutorResultState.NO_ASSOCIATION);
        }

        WorkflowRequest workFlowRequest = new WorkflowRequest();
        List<RequestParameter> parameters = new ArrayList<RequestParameter>(wfParams.size() + nonWfParams.size() + 1);
        for (Map.Entry<String, Object> paramEntry : wfParams.entrySet()) {
            parameters.add(getParameter(paramEntry.getKey(), paramEntry.getValue(), true));
        }
        for (Map.Entry<String, Object> paramEntry : nonWfParams.entrySet()) {
            parameters.add(getParameter(paramEntry.getKey(), paramEntry.getValue(), false));
        }
        RequestParameter uuidParameter = new RequestParameter();
        uuidParameter.setName(WFConstant.REQUEST_ID);
        uuidParameter.setValue(uuid);
        uuidParameter.setRequiredInWorkflow(true);
        uuidParameter.setValueType(WorkflowDataType.STRING_TYPE);
        parameters.add(uuidParameter);
        workFlowRequest.setRequestParameters(parameters);
        workFlowRequest.setTenantId(CarbonContext.getThreadLocalCarbonContext().getTenantId());
        workFlowRequest.setUuid(uuid);

        engageWorkflow(workFlowRequest);

        WorkflowExecutorResult workflowExecutorResult =
                WorkFlowExecutorManager.getInstance().executeWorkflow(workFlowRequest);

        if(workflowExecutorResult.getExecutorResultState() == ExecutorResultState.FAILED){
            throw new WorkflowException(workflowExecutorResult.getMessage());
        }

        return workflowExecutorResult;
    }

    protected boolean isValueValid(String paramName, Object paramValue, String expectedType) {

        switch (expectedType) {
            case WorkflowDataType.BOOLEAN_TYPE:
                return paramValue instanceof Boolean;
            case WorkflowDataType.STRING_TYPE:
                return paramValue instanceof String;
            case WorkflowDataType.INTEGER_TYPE:
                return paramValue instanceof Integer || paramValue instanceof Long || paramValue instanceof Character ||
                        paramValue instanceof Byte || paramValue instanceof Short;
            case WorkflowDataType.DOUBLE_TYPE:
                return paramValue instanceof Float || paramValue instanceof Double;
            case WorkflowDataType.STRING_LIST_TYPE:
            case WorkflowDataType.DOUBLE_LIST_TYPE:
            case WorkflowDataType.INTEGER_LIST_TYPE:
            case WorkflowDataType.BOOLEAN_LIST_TYPE:
                return paramValue instanceof Collection;
            case WorkflowDataType.STRING_STRING_MAP_TYPE:
                return paramValue instanceof Map;
        }
        return false;
    }

    /**
     * Wraps the parameters to the WorkflowParameter
     *
     * @param name     Name of the parameter
     * @param value    Value of the parameter
     * @param required Whether it is required to sent to the workflow executor
     * @return
     */
    protected RequestParameter getParameter(String name, Object value, boolean required)
            throws WorkflowRuntimeException {

        RequestParameter parameter = new RequestParameter();
        parameter.setName(name);
        parameter.setValue(value);
        parameter.setRequiredInWorkflow(required);
        String valueType = getParamDefinitions().get(name);
        if (valueType == null || value == null) {
            //null value as param, or undefined param
            parameter.setValueType(WorkflowDataType.OTHER_TYPE);
        } else {
            if (isValueValid(name, value, valueType)) {
                parameter.setValueType(valueType);
            } else {
                throw new WorkflowRuntimeException("Invalid value for '" + name + "', Expected: '" + valueType + "', " +
                        "but was of " + value.getClass().getName());
            }
        }
        return parameter;
    }

    @Override
    public void engageWorkflow(WorkflowRequest workFlowRequest) throws WorkflowException {

        workFlowRequest.setEventType(getEventId());

    }

    @Override
    public void onWorkflowCompletion(String status, WorkflowRequest originalRequest, Map<String, Object>
            responseParams) throws WorkflowException {

        try {
            Map<String, Object> requestParams = new HashMap<String, Object>();
            for (RequestParameter parameter : originalRequest.getRequestParameters()) {
                requestParams.put(parameter.getName(), parameter.getValue());
            }
            if (retryNeedAtCallback()) {
                setWorkFlowCompleted(true);
            }
            onWorkflowCompletion(status, requestParams, responseParams, originalRequest.getTenantId());
        } finally {
            unsetWorkFlowCompleted();
        }
    }

    /**
     * Callback method from the executor
     *
     * @param status                   The return status from the workflow executor
     * @param requestParams            The params that were in the original request
     * @param responseAdditionalParams The params sent from the workflow executor
     * @param tenantId
     */
    public abstract void onWorkflowCompletion(String status, Map<String, Object> requestParams, Map<String, Object>
            responseAdditionalParams, int tenantId) throws WorkflowException;

    /**
     * Whether the same request is initiated at the callback. If set to <code>true</code>, this will take actions to
     * skip the request initiated at the callback.
     * <b>Note:</b> Do not set this to true unless necessary, It will lead to memory leaks
     *
     * @return
     */
    public abstract boolean retryNeedAtCallback();

    /**
     * Check if an operation engaged with a workflow valid to execute
     *
     * @param entities Array of entities involved in operation
     * @return
     */
    public boolean isValidOperation(Entity[] entities) throws WorkflowException {
        return true;
    }

    private boolean isWorkflowCompleted() {

        if (retryNeedAtCallback() && getWorkFlowCompleted() != null && getWorkFlowCompleted()) {
            return true;
        } else return false;
    }

    /**
     * We can check whether the current event type is already associated with
     * at-least one association or not by using isAssociated method.
     *
     * @return Boolean value for result of isAssociated
     * @throws WorkflowException
     */
    public boolean isAssociated() throws WorkflowException{
        boolean eventEngaged = false ;
        try {
            eventEngaged = WorkflowServiceDataHolder.getInstance().getWorkflowService().isEventAssociated(getEventId());
        } catch (InternalWorkflowException e) {
            String errorMsg = "Error occurred while checking any association for this event, " + e.getMessage() ;
            log.error(errorMsg, e);
            throw new WorkflowException(errorMsg,e);
        }
        return eventEngaged ;
    }
}
