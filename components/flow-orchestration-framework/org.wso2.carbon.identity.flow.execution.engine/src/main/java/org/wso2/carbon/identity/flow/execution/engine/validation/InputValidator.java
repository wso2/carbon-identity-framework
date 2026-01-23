/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

package org.wso2.carbon.identity.flow.execution.engine.validation;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.flow.execution.engine.model.ExecutorResponse;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.NodeResponse;
import org.wso2.carbon.identity.flow.mgt.model.NodeConfig;

import static org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorStatus.STATUS_RETRY;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.STATUS_INCOMPLETE;
import static org.wso2.carbon.identity.flow.mgt.Constants.StepTypes.END;
import static org.wso2.carbon.identity.flow.mgt.Constants.StepTypes.VIEW;

/**
 * Validates user input for flow execution nodes and manages validation retry scenarios.
 * This class is implemented as a thread-safe singleton to provide consistent validation
 * logic across the application.
 */
public class InputValidator {

    private static final Log LOG = LogFactory.getLog(InputValidator.class);
    private static final InputValidator INSTANCE = new InputValidator();

    private static final int DEFAULT_MAX_TRAVERSAL_DEPTH = 10;

    private InputValidator() {
    }

    /**
     * Get the singleton instance of InputValidator.
     *
     * @return singleton instance.
     */
    public static InputValidator getInstance() {

        return INSTANCE;
    }

    /**
     * Validates user input and returns a prompt step if validation fails.
     * If validation passes or no input is present, returns null.
     *
     * @param context The flow execution context containing user input and current node state.
     * @return NodeResponse for re-rendering if validation fails, otherwise null.
     */
    public NodeResponse executeInputValidation(FlowExecutionContext context) {

        if (MapUtils.isEmpty(context.getUserInputData()) &&!END.equals(context.getCurrentNode().getId())) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("User input is required but missing for node: " + context.getCurrentNode().getId() +
                        " in flow: " + context.getContextIdentifier());
            }
            return buildIncompleteResponse();
        }

        ExecutorResponse validationResponse = InputValidationService.getInstance()
                .resolveInputValidationResponse(context);
        if (STATUS_RETRY.equals(validationResponse.getResult())) {
            return buildValidationRetryResponse(context, validationResponse);
        }
        return null;
    }

    /**
     * Handles the validation retry scenario by finding the appropriate node with page mapping
     * and setting up the context for re-rendering.
     *
     * @param context          The flow execution context.
     * @param executorResponse The executor response containing validation error details.
     * @return NodeResponse configured for re-rendering with error information.
     */
    private NodeResponse buildValidationRetryResponse(FlowExecutionContext context,
                                                      ExecutorResponse executorResponse) {

        NodeConfig currentNodeConfig = context.getCurrentNode();
        NodeConfig nodeWithPageMapping = findClosestNodeWithPageMapping(context, currentNodeConfig);
        if (nodeWithPageMapping != null &&
                !nodeWithPageMapping.getId().equals(currentNodeConfig.getId())) {
            context.setCurrentNode(nodeWithPageMapping);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Validation failed. Rolling back to node: " + nodeWithPageMapping.getId() +
                        " for re-rendering with error for flow: " + context.getContextIdentifier());
            }
        }
        return new NodeResponse.Builder()
                .status(STATUS_INCOMPLETE)
                .type(VIEW)
                .requiredData(executorResponse.getRequiredData())
                .optionalData(executorResponse.getOptionalData())
                .error(executorResponse.getErrorMessage())
                .build();
    }

    /**
     * Finds the node that has page mapping for UI components.
     * Traverses backwards from the current node to find a node with page mapping.
     *
     * @param context       The flow execution context.
     * @param currentConfig The current node configuration.
     * @return The node configuration with page mapping, or the current node if none found.
     */
    private NodeConfig findClosestNodeWithPageMapping(FlowExecutionContext context,
                                                      NodeConfig currentConfig) {

        if (currentConfig == null) {
            LOG.warn("Current node config is null for flow: " + context.getContextIdentifier());
            return null;
        }
        if (hasPageMapping(context, currentConfig.getId())) {
            return currentConfig;
        }

        String previousNodeId = currentConfig.getPreviousNodeId();
        int depth = 0;
        while (previousNodeId != null && depth < DEFAULT_MAX_TRAVERSAL_DEPTH) {
            NodeConfig previousNode = context.getGraphConfig().getNodeConfigs().get(previousNodeId);
            if (previousNode == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Previous node " + previousNodeId + " not found in graph for flow: " +
                            context.getContextIdentifier());
                }
                break;
            }
            if (hasPageMapping(context, previousNode.getId())) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Found node with page mapping: " + previousNode.getId() +
                            " at depth " + depth + " for flow: " + context.getContextIdentifier());
                }
                return previousNode;
            }
            previousNodeId = previousNode.getPreviousNodeId();
            depth++;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("No node with page mapping found for flow: " + context.getContextIdentifier() +
                    ". Using current node: " + currentConfig.getId());
        }
        return currentConfig;
    }

    /**
     * Checks if a node has a page mapping in the graph configuration.
     *
     * @param context The flow execution context.
     * @param nodeId  The node ID to check.
     * @return true if the node has a page mapping, false otherwise.
     */
    private boolean hasPageMapping(FlowExecutionContext context, String nodeId) {

        if (context == null || context.getGraphConfig() == null) {
            return false;
        }
        return context.getGraphConfig().getNodePageMappings() != null &&
                context.getGraphConfig().getNodePageMappings().containsKey(nodeId);
    }

    /**
     * Builds a standard incomplete response when user input is missing.
     *
     * @return NodeResponse with incomplete status.
     */
    private NodeResponse buildIncompleteResponse() {

        return new NodeResponse.Builder()
                .status(STATUS_INCOMPLETE)
                .type(VIEW)
                .build();
    }
}
