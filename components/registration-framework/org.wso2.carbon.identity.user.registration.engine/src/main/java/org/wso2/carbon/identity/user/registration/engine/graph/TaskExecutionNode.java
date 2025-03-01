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

package org.wso2.carbon.identity.user.registration.engine.graph;

import static org.wso2.carbon.identity.user.registration.engine.util.Constants.ErrorMessages.ERROR_CODE_EXECUTOR_NOT_FOUND;
import static org.wso2.carbon.identity.user.registration.engine.util.Constants.ErrorMessages.ERROR_CODE_UNSUPPORTED_EXECUTOR;
import static org.wso2.carbon.identity.user.registration.engine.util.Constants.ErrorMessages.ERROR_CODE_UNSUPPORTED_EXECUTOR_STATUS;
import static org.wso2.carbon.identity.user.registration.engine.util.Constants.ExecutorStatus.STATUS_EXTERNAL_REDIRECTION;
import static org.wso2.carbon.identity.user.registration.engine.util.Constants.ExecutorStatus.STATUS_USER_CREATED;
import static org.wso2.carbon.identity.user.registration.engine.util.Constants.ExecutorStatus.STATUS_USER_INPUT_REQUIRED;
import static org.wso2.carbon.identity.user.registration.engine.util.Constants.STATUS_COMPLETE;
import static org.wso2.carbon.identity.user.registration.engine.util.Constants.STATUS_INCOMPLETE;
import static org.wso2.carbon.identity.user.registration.engine.util.RegistrationFlowEngineUtils.handleServerException;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.NodeTypes.TASK_EXECUTION;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.StepTypes.REDIRECTION;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.StepTypes.VIEW;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.user.registration.engine.internal.RegistrationFlowEngineDataHolder;
import org.wso2.carbon.identity.user.registration.engine.model.ExecutorResponse;
import org.wso2.carbon.identity.user.registration.engine.model.RegisteringUser;
import org.wso2.carbon.identity.user.registration.engine.model.RegistrationContext;
import org.wso2.carbon.identity.user.registration.engine.model.Response;
import org.wso2.carbon.identity.user.registration.mgt.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.registration.mgt.exception.RegistrationServerException;
import org.wso2.carbon.identity.user.registration.mgt.model.NodeConfig;

/**
 * Implementation of a node specific to executing a registration executor.
 */
public class TaskExecutionNode implements Node {

    private static final Log LOG = LogFactory.getLog(TaskExecutionNode.class);

    @Override
    public String getName() {

        return TASK_EXECUTION;
    }

    @Override
    public Response execute(RegistrationContext context, NodeConfig configs)
            throws RegistrationFrameworkException {

        if (configs.getExecutorConfig() == null) {
            throw handleServerException(ERROR_CODE_EXECUTOR_NOT_FOUND, context.getRegGraph().getId(),
                                        context.getTenantDomain());
        }
        // TODO: 2021/4/6 Add executor configuration to the context where application
        return triggerExecutor(context, configs);
    }

    private Executor resolveExecutor(NodeConfig configs, String graphId, String tenantDomain)
            throws RegistrationServerException {

        String executorName = configs.getExecutorConfig().getName();

        Executor mappedRegExecutor = RegistrationFlowEngineDataHolder.getInstance().getExecutors().get(executorName);
        if (mappedRegExecutor == null) {
            throw handleServerException(ERROR_CODE_UNSUPPORTED_EXECUTOR, executorName, graphId, tenantDomain);
        }
        return mappedRegExecutor;
    }

    @Override
    public Response rollback(RegistrationContext context, NodeConfig nodeConfig)
            throws RegistrationFrameworkException {

        LOG.debug("Rollback is not supported for TaskExecutionNode.");
        return null;
    }

    private Response triggerExecutor(RegistrationContext context, NodeConfig configs)
            throws RegistrationFrameworkException {

        Executor mappedRegExecutor = resolveExecutor(configs, context.getRegGraph().getId(), context.getTenantDomain());

        ExecutorResponse response = mappedRegExecutor.execute(context);
        if (STATUS_COMPLETE.equals(response.getResult()) || STATUS_USER_CREATED.equals(response.getResult())) {
            return handleCompleteStatus(context, response, mappedRegExecutor.getName(), configs);
        } else {
            return handleIncompleteStatus(context, response, mappedRegExecutor.getName());
        }
    }

    private Response handleIncompleteStatus(RegistrationContext context, ExecutorResponse response, String name)
            throws RegistrationServerException {

        if (response.getContextProperties() != null && !response.getContextProperties().isEmpty()) {
            context.addProperties(response.getContextProperties());
        }
        if (STATUS_USER_INPUT_REQUIRED.equals(response.getResult())) {
            return new Response.Builder()
                    .status(STATUS_INCOMPLETE)
                    .type(VIEW)
                    .requiredData(response.getRequiredData())
                    .build();
        } else if (STATUS_EXTERNAL_REDIRECTION.equals(response.getResult())) {
            return new Response.Builder()
                    .status(STATUS_INCOMPLETE)
                    .type(REDIRECTION)
                    .requiredData(response.getRequiredData())
                    .additionalInfo(response.getAdditionalInfo())
                    .build();
        }
        throw handleServerException(ERROR_CODE_UNSUPPORTED_EXECUTOR_STATUS, response.getResult());
    }

    private Response handleCompleteStatus(RegistrationContext context, ExecutorResponse response, String executorName
            , NodeConfig configs) {

        if ((response.getRequiredData() != null && !response.getRequiredData().isEmpty()) ||
                (response.getAdditionalInfo() != null && !response.getAdditionalInfo().isEmpty())) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Unhandled data from the executor " + executorName + " will be ignored.");
            }
        }

        RegisteringUser user = context.getRegisteringUser();
        if (response.getUpdatedUserClaims() != null) {
            user.addClaims(response.getUpdatedUserClaims());
        }
        if (response.getUserCredentials() != null) {
            user.getUserCredentials().putAll(response.getUserCredentials());
        }

        if (CollectionUtils.isNotEmpty(configs.getEdges())) {
            configs.setNextNodeId(configs.getEdges().get(0).getTargetNodeId());
        }
        return new Response.Builder().status(STATUS_COMPLETE).build();
    }
}
