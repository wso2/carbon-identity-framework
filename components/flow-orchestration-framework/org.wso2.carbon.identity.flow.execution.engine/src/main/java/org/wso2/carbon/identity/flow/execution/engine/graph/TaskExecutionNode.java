/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.flow.execution.engine.graph;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.execution.engine.internal.FlowExecutionEngineDataHolder;
import org.wso2.carbon.identity.flow.execution.engine.model.ExecutorResponse;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowUser;
import org.wso2.carbon.identity.flow.execution.engine.model.NodeResponse;
import org.wso2.carbon.identity.flow.mgt.model.NodeConfig;

import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_EXECUTOR_FAILURE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_EXECUTOR_NOT_FOUND;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_FLOW_FAILURE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_REQUEST_PROCESSING_FAILURE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_UNSUPPORTED_EXECUTOR;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_UNSUPPORTED_EXECUTOR_STATUS;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorStatus.STATUS_CLIENT_INPUT_REQUIRED;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorStatus.STATUS_ERROR;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorStatus.STATUS_EXTERNAL_REDIRECTION;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorStatus.STATUS_RETRY;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorStatus.STATUS_USER_ERROR;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorStatus.STATUS_USER_INPUT_REQUIRED;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorStatus.STATUS_WEBAUTHN;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.STATUS_COMPLETE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.STATUS_INCOMPLETE;
import static org.wso2.carbon.identity.flow.execution.engine.util.FlowExecutionEngineUtils.handleClientException;
import static org.wso2.carbon.identity.flow.execution.engine.util.FlowExecutionEngineUtils.handleServerException;
import static org.wso2.carbon.identity.flow.mgt.Constants.NodeTypes.TASK_EXECUTION;
import static org.wso2.carbon.identity.flow.mgt.Constants.StepTypes.INTERNAL_PROMPT;
import static org.wso2.carbon.identity.flow.mgt.Constants.StepTypes.REDIRECTION;
import static org.wso2.carbon.identity.flow.mgt.Constants.StepTypes.VIEW;
import static org.wso2.carbon.identity.flow.mgt.Constants.StepTypes.WEBAUTHN;

/**
 * Implementation of a node specific to executing a flow executor.
 */
public class TaskExecutionNode implements Node {

    private static final Log LOG = LogFactory.getLog(TaskExecutionNode.class);

    @Override
    public String getName() {

        return TASK_EXECUTION;
    }

    @Override
    public NodeResponse execute(FlowExecutionContext context, NodeConfig configs)
            throws FlowEngineException {

        if (configs.getExecutorConfig() == null) {
            throw handleServerException(context.getFlowType(), ERROR_CODE_EXECUTOR_NOT_FOUND, context.getFlowType(),
                    context.getGraphConfig().getId(), context.getTenantDomain());
        }
        return triggerExecutor(context, configs);
    }

    private Executor resolveExecutor(String flowType, NodeConfig configs, String graphId, String tenantDomain)
            throws FlowEngineException {

        String executorName = configs.getExecutorConfig().getName();

        Executor mappedFlowExecutor = FlowExecutionEngineDataHolder.getInstance().getExecutors().get(executorName);
        if (mappedFlowExecutor == null) {
            throw handleServerException(flowType, ERROR_CODE_UNSUPPORTED_EXECUTOR, executorName, flowType, graphId,
                    tenantDomain);
        }
        return mappedFlowExecutor;
    }

    @Override
    public NodeResponse rollback(FlowExecutionContext context, NodeConfig config) throws FlowEngineException {

        if (config.getExecutorConfig() == null) {
            throw handleServerException(context.getFlowType(), ERROR_CODE_EXECUTOR_NOT_FOUND, context.getFlowType(),
                    context.getGraphConfig().getId(), context.getTenantDomain());
        }
        Executor mappedFlowExecutor = resolveExecutor(context.getFlowType(), config, context.getGraphConfig().getId(),
                context.getTenantDomain());
        mappedFlowExecutor.rollback(context);
        context.setCurrentNode(context.getGraphConfig().getNodeConfigs().get(config.getPreviousNodeId()));
        // Ignore the response from executor for rollback.
        return new NodeResponse.Builder().status(STATUS_COMPLETE).build();
    }

    private NodeResponse triggerExecutor(FlowExecutionContext context, NodeConfig configs)
            throws FlowEngineException {

        Executor mappedFlowExecutor = resolveExecutor(context.getFlowType(), configs, context.getGraphConfig().getId(),
                context.getTenantDomain());

        if (mappedFlowExecutor instanceof AuthenticationExecutor) {
            ((AuthenticationExecutor) mappedFlowExecutor).addIdpConfigsToContext(context, configs.getExecutorConfig());
        }

        ExecutorResponse response = mappedFlowExecutor.execute(context);
        if (response == null) {
            throw handleServerException(context.getFlowType(), ERROR_CODE_EXECUTOR_FAILURE,
                    "Executor response is null for executor: " + mappedFlowExecutor.getName());
        }
        if (response.getContextProperties() != null && !response.getContextProperties().isEmpty()) {
            context.addProperties(response.getContextProperties());
        }
        if (STATUS_COMPLETE.equals(response.getResult())) {
            context.addCompletedNode(configs);
            return handleCompleteStatus(context, response, mappedFlowExecutor.getName(), configs);
        }
        return handleIncompleteStatus(context, response);
    }

    private NodeResponse handleIncompleteStatus(FlowExecutionContext context, ExecutorResponse response)
            throws FlowEngineException {

        if (response.getContextProperties() != null && !response.getContextProperties().isEmpty()) {
            context.addProperties(response.getContextProperties());
        }
        String flowType = context != null ? context.getFlowType() : null;
        switch (response.getResult()) {
            case STATUS_RETRY:
                return new NodeResponse.Builder()
                        .status(STATUS_INCOMPLETE)
                        .type(VIEW)
                        .requiredData(response.getRequiredData())
                        .optionalData(response.getOptionalData())
                        .error(response.getErrorMessage())
                        .build();
            case STATUS_USER_INPUT_REQUIRED:
                return new NodeResponse.Builder()
                        .status(STATUS_INCOMPLETE)
                        .type(VIEW)
                        .requiredData(response.getRequiredData())
                        .optionalData(response.getOptionalData())
                        .additionalInfo(response.getAdditionalInfo())
                        .build();
            case STATUS_CLIENT_INPUT_REQUIRED:
                return new NodeResponse.Builder()
                        .status(STATUS_INCOMPLETE)
                        .type(INTERNAL_PROMPT)
                        .requiredData(response.getRequiredData())
                        .optionalData(response.getOptionalData())
                        .additionalInfo(response.getAdditionalInfo())
                        .build();
            case STATUS_WEBAUTHN:
                return new NodeResponse.Builder()
                        .status(STATUS_INCOMPLETE)
                        .type(WEBAUTHN)
                        .requiredData(response.getRequiredData())
                        .optionalData(response.getOptionalData())
                        .additionalInfo(response.getAdditionalInfo())
                        .build();
            case STATUS_EXTERNAL_REDIRECTION:
                return new NodeResponse.Builder()
                        .status(STATUS_INCOMPLETE)
                        .type(REDIRECTION)
                        .requiredData(response.getRequiredData())
                        .optionalData(response.getOptionalData())
                        .additionalInfo(response.getAdditionalInfo())
                        .build();
            case STATUS_USER_ERROR:
                if (response.getErrorCode() != null){
                    throw handleClientException(flowType, response);
                }
                throw handleClientException(flowType, ERROR_CODE_FLOW_FAILURE, response.getErrorMessage());
            case STATUS_ERROR:
                if (response.getErrorCode() != null){
                    throw handleServerException(flowType, response);
                }
                throw handleServerException(flowType, ERROR_CODE_EXECUTOR_FAILURE, response.getErrorMessage());
            default:
                throw handleServerException(flowType, ERROR_CODE_UNSUPPORTED_EXECUTOR_STATUS, response.getResult());
        }
    }

    private NodeResponse handleCompleteStatus(FlowExecutionContext context, ExecutorResponse response, String executorName,
                                              NodeConfig configs) {

        if ((response.getRequiredData() != null && !response.getRequiredData().isEmpty()) ||
                (response.getOptionalData() != null && !response.getOptionalData().isEmpty()) ||
                (response.getAdditionalInfo() != null && !response.getAdditionalInfo().isEmpty())) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Unhandled data from the executor " + executorName + " will be ignored.");
            }
        }

        FlowUser user = context.getFlowUser();
        if (response.getUpdatedUserClaims() != null) {
            response.getUpdatedUserClaims().forEach((key, value) -> user.addClaim(key, String.valueOf(value)));
        }
        if (response.getUserCredentials() != null) {
            user.getUserCredentials().putAll(response.getUserCredentials());
        }

        if (CollectionUtils.isNotEmpty(configs.getEdges())) {
            configs.setNextNodeId(configs.getEdges().get(0).getTargetNodeId());
        }
        return new NodeResponse.Builder().status(STATUS_COMPLETE).build();
    }
}
