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

package org.wso2.carbon.identity.user.registration.engine.node;

import static org.wso2.carbon.identity.user.registration.engine.util.Constants.ErrorMessages.ERROR_EXECUTOR_NOT_FOUND;
import static org.wso2.carbon.identity.user.registration.engine.util.Constants.ErrorMessages.ERROR_EXECUTOR_UNHANDLED_DATA;
import static org.wso2.carbon.identity.user.registration.engine.util.Constants.STATUS_ACTION_COMPLETE;
import static org.wso2.carbon.identity.user.registration.engine.util.Constants.STATUS_ATTR_REQUIRED;
import static org.wso2.carbon.identity.user.registration.engine.util.Constants.STATUS_COMPLETE;
import static org.wso2.carbon.identity.user.registration.engine.util.Constants.STATUS_CRED_REQUIRED;
import static org.wso2.carbon.identity.user.registration.engine.util.Constants.STATUS_NODE_COMPLETE;
import static org.wso2.carbon.identity.user.registration.engine.util.Constants.STATUS_NEXT_ACTION_PENDING;
import static org.wso2.carbon.identity.user.registration.engine.util.Constants.STATUS_VERIFICATION_REQUIRED;
import java.util.Optional;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.user.registration.engine.executor.RegistrationExecutor;
import org.wso2.carbon.identity.user.registration.engine.executor.action.Authentication;
import org.wso2.carbon.identity.user.registration.mgt.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.registration.engine.executor.action.AttributeCollection;
import org.wso2.carbon.identity.user.registration.engine.executor.action.CredentialEnrollment;
import org.wso2.carbon.identity.user.registration.mgt.exception.RegistrationServerException;
import org.wso2.carbon.identity.user.registration.engine.executor.Executor;
import org.wso2.carbon.identity.user.registration.engine.executor.action.Verification;
import org.wso2.carbon.identity.user.registration.engine.internal.UserRegistrationServiceDataHolder;
import org.wso2.carbon.identity.user.registration.mgt.Constants;
import org.wso2.carbon.identity.user.registration.mgt.model.NodeConfig;
import org.wso2.carbon.identity.user.registration.engine.model.ExecutorResponse;
import org.wso2.carbon.identity.user.registration.engine.model.NodeResponse;
import org.wso2.carbon.identity.user.registration.engine.model.RegistrationContext;
import org.wso2.carbon.identity.user.registration.engine.model.RegistrationRequestedUser;

/**
 * Implementation of a node specific to executing a registration executor.
 */
public class TaskExecutionNode implements Node {

    private static final Log LOG = LogFactory.getLog(TaskExecutionNode.class);

    @Override
    public String getName() {

        return Constants.NodeTypes.TASK_EXECUTION;
    }

    @Override
    public NodeResponse execute(RegistrationContext context, NodeConfig configs)
            throws RegistrationFrameworkException {

        if (configs.getExecutorConfig() == null) {
            throw new RegistrationServerException(ERROR_EXECUTOR_NOT_FOUND.getCode(),
                                                  ERROR_EXECUTOR_NOT_FOUND.getMessage(),
                                                  String.format(ERROR_EXECUTOR_NOT_FOUND.getDescription(),
                                                                configs.getUuid()));
        }

        String executorName = configs.getExecutorConfig().getName();
        Executor mappedRegExecutor = null;
        for (Executor executor : UserRegistrationServiceDataHolder.getExecutors()) {
            if (executorName.equals(executor.getName())) {
                mappedRegExecutor = executor;
                break;
            }
        }

        NodeResponse nodeResponse = triggerExecutor(context, mappedRegExecutor);

        if (STATUS_NODE_COMPLETE.equals(nodeResponse.getStatus()) && mappedRegExecutor instanceof Authentication) {
            context.addAuthenticatedMethod(mappedRegExecutor.getName());
        }
        return nodeResponse;
    }

    @Override
    public NodeResponse rollback(RegistrationContext context, NodeConfig nodeConfig)
            throws RegistrationFrameworkException {

        LOG.debug("Rollback is not supported for TaskExecutionNode.");
        return null;
    }

    private NodeResponse triggerExecutor(RegistrationContext context, Executor executor)
            throws RegistrationFrameworkException {

        if (executor instanceof RegistrationExecutor) {
            ExecutorResponse response = ((RegistrationExecutor) executor).execute(context);
            if (STATUS_COMPLETE.equals(response.getResult())) {
                handleCompleteStatus(context, response, executor.getName());
                return new NodeResponse(STATUS_NODE_COMPLETE);
            } else {
                return handleIncompleteStatus(context, response);
            }
        } else {
            return triggerAction(context, executor);
        }
    }

    private NodeResponse triggerAction(RegistrationContext context, Executor executor)
            throws RegistrationFrameworkException {

        Optional<NodeResponse> attributeCollectionResponse = triggerAttributeCollection(context, executor);
        if (attributeCollectionResponse.isPresent()) {
            return attributeCollectionResponse.get();
        }

        Optional<NodeResponse> credEnrollmentResponse = triggerCredentialEnrollment(context, executor);
        if (credEnrollmentResponse.isPresent()) {
            return credEnrollmentResponse.get();
        }

        Optional<NodeResponse> verificationResponse = triggerVerification(context, executor);
        return verificationResponse.orElseGet(() -> new NodeResponse(STATUS_NODE_COMPLETE));

        // Todo Need to verify whether there can be any other executor types than this.
    }

    private Optional<NodeResponse> triggerAttributeCollection(RegistrationContext context, Executor executor)
            throws RegistrationFrameworkException {

        String executorStatus = context.getExecutorStatus();
        ExecutorResponse response;
        if ((STATUS_NEXT_ACTION_PENDING.equals(executorStatus) || STATUS_ATTR_REQUIRED.equals(executorStatus)) &&
                executor instanceof AttributeCollection) {
            response = ((AttributeCollection) executor).collect(context);
            if (!STATUS_ACTION_COMPLETE.equals(response.getResult())) {
                return Optional.of(handleIncompleteStatus(context, response));
            } else {
                context.setExecutorStatus(STATUS_NEXT_ACTION_PENDING);
                handleCompleteStatus(context, response, executor.getName());
            }
        }
        return Optional.empty();
    }

    private Optional<NodeResponse> triggerCredentialEnrollment(RegistrationContext context, Executor executor)
            throws RegistrationServerException {

        String executorStatus = context.getExecutorStatus();
        ExecutorResponse response;
        if ((STATUS_NEXT_ACTION_PENDING.equals(executorStatus) || STATUS_CRED_REQUIRED.equals(executorStatus)) &&
                executor instanceof CredentialEnrollment) {
            response = ((CredentialEnrollment) executor).enrollCredential(context);
            if (!STATUS_ACTION_COMPLETE.equals(response.getResult())) {
                return Optional.of(handleIncompleteStatus(context, response));
            } else {
                context.setExecutorStatus(STATUS_NEXT_ACTION_PENDING);
                handleCompleteStatus(context, response, executor.getName());
            }
        }
        return Optional.empty();
    }

    private Optional<NodeResponse> triggerVerification(RegistrationContext context, Executor executor)
            throws RegistrationServerException {

        String executorStatus = context.getExecutorStatus();
        ExecutorResponse response;
        if ((STATUS_NEXT_ACTION_PENDING.equals(executorStatus) ||
                STATUS_VERIFICATION_REQUIRED.equals(executorStatus)) &&
                executor instanceof Verification) {
            response = ((Verification) executor).verify(context);
            if (!STATUS_ACTION_COMPLETE.equals(response.getResult())) {
                return Optional.of(handleIncompleteStatus(context, response));
            } else {
                context.setExecutorStatus(STATUS_NEXT_ACTION_PENDING);
                handleCompleteStatus(context, response, executor.getName());
            }
        }
        return Optional.empty();
    }

    private NodeResponse handleIncompleteStatus(RegistrationContext context, ExecutorResponse response) {

        context.setExecutorStatus(response.getResult());
        if (response.getContextProperties() != null && !response.getContextProperties().isEmpty()) {
            context.addProperties(response.getContextProperties());
        }

        NodeResponse nodeResponse = new NodeResponse(response.getResult());
        nodeResponse.addRequiredData(response.getRequiredData());
        nodeResponse.addAdditionalInfo(response.getAdditionalInfo());
        nodeResponse.setMessage(response.getMessage());
        return nodeResponse;
    }

    private void handleCompleteStatus(RegistrationContext context, ExecutorResponse response, String executorName)
            throws RegistrationServerException {

        if ((response.getRequiredData() != null && !response.getRequiredData().isEmpty()) ||
                (response.getAdditionalInfo() != null && !response.getAdditionalInfo().isEmpty())) {
            throw new RegistrationServerException(ERROR_EXECUTOR_UNHANDLED_DATA.getCode(),
                                                  ERROR_EXECUTOR_UNHANDLED_DATA.getMessage(),
                                                  String.format(ERROR_EXECUTOR_UNHANDLED_DATA.getDescription(),
                                                                executorName));
        }

        // todo handle the scenario where the user is already onboarded and the executor is trying to update a user
        //  in the userstore.
        RegistrationRequestedUser user = context.getRegisteringUser();
        if (response.getUpdatedUserClaims() != null) {
            user.addClaims(response.getUpdatedUserClaims());
        }
        if (response.getUserCredentials() != null) {
            user.addUserCredentials(response.getUserCredentials());
        }
    }
}
