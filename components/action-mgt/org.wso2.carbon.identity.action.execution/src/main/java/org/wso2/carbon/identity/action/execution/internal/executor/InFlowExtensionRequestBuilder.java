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

package org.wso2.carbon.identity.action.execution.internal.executor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionRequestBuilderException;
import org.wso2.carbon.identity.action.execution.api.model.*;
import org.wso2.carbon.identity.action.execution.api.service.ActionExecutionRequestBuilder;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowUser;
import org.wso2.carbon.identity.flow.mgt.model.NodeConfig;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * This class is responsible for building the Action Execution Request for In-Flow Extension actions.
 * It converts the FlowExecutionContext into the standard ActionExecutionRequest format.
 */
public class InFlowExtensionRequestBuilder implements ActionExecutionRequestBuilder {

    private static final Log LOG = LogFactory.getLog(InFlowExtensionRequestBuilder.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final TypeReference<List<Map<String, Object>>> OPERATION_LIST_TYPE_REF = 
            new TypeReference<List<Map<String, Object>>>() { };

    @Override
    public ActionType getSupportedActionType() {

        return ActionType.IN_FLOW_EXTENSION;
    }

    @Override
    public ActionExecutionRequest buildActionExecutionRequest(FlowContext flowContext,
                                                              ActionExecutionRequestContext actionExecutionContext)
            throws ActionExecutionRequestBuilderException {

//        FlowExecutionContext flowExecutionContext = flowContext.getValue(FLOW_EXECUTION_CONTEXT_KEY,
//                FlowExecutionContext.class);

//        if (flowExecutionContext == null) {
//            throw new ActionExecutionRequestBuilderException("FlowExecutionContext not found in flow context.");
//        }

        InFlowExtensionEvent event = buildEvent(flowContext);

        // Build allowed operations from metadata configuration
        List<AllowedOperation> allowedOperations = buildAllowedOperations(flowContext);

        return new ActionExecutionRequest.Builder()
                .actionType(ActionType.IN_FLOW_EXTENSION)
                .flowId(flowContext.getValue(InFlowExtensionExecutor.CORRELATION_ID_KEY, String.class))
                .event(event)
                .allowedOperations(allowedOperations)
                .build();
    }

    /**
     * Build allowed operations from the flow context.
     * Parses the allowedOperations JSON from executor metadata.
     *
     * @param flowContext The flow context containing allowed operations configuration.
     * @return List of AllowedOperation objects.
     */
    private List<AllowedOperation> buildAllowedOperations(FlowContext flowContext) {

        String allowedOperationsJson = flowContext.getValue(
                InFlowExtensionExecutor.ALLOWED_OPERATIONS_KEY, String.class);

        if (allowedOperationsJson == null || allowedOperationsJson.isEmpty()) {
            LOG.debug("No allowed operations configured in executor metadata. Using empty list.");
            return Collections.emptyList();
        }

        try {
            List<Map<String, Object>> operationConfigs = objectMapper.readValue(
                    allowedOperationsJson, OPERATION_LIST_TYPE_REF);

            List<AllowedOperation> allowedOperations = new ArrayList<>();
            for (Map<String, Object> config : operationConfigs) {
                AllowedOperation operation = createAllowedOperationFromConfig(config);
                if (operation != null) {
                    allowedOperations.add(operation);
                }
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Built " + allowedOperations.size() + " allowed operations from executor metadata.");
            }

            return allowedOperations;
        } catch (JsonProcessingException e) {
            LOG.error("Failed to parse allowed operations from executor metadata: " + e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Create an AllowedOperation from a configuration map.
     *
     * @param config The configuration map containing 'op' and 'paths'.
     * @return AllowedOperation object or null if invalid.
     */
    @SuppressWarnings("unchecked")
    private AllowedOperation createAllowedOperationFromConfig(Map<String, Object> config) {

        String opString = (String) config.get("op");
        Object pathsObj = config.get("paths");

        if (opString == null || pathsObj == null) {
            LOG.warn("Invalid allowed operation config: missing 'op' or 'paths'.");
            return null;
        }

        Operation operation;
        try {
            operation = Operation.valueOf(opString.toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException e) {
            LOG.warn("Unknown operation type: " + opString);
            return null;
        }

        List<String> paths;
        if (pathsObj instanceof List) {
            paths = (List<String>) pathsObj;
        } else {
            LOG.warn("Invalid 'paths' type in allowed operation config.");
            return null;
        }

        AllowedOperation allowedOperation = new AllowedOperation();
        allowedOperation.setOp(operation);
        allowedOperation.setPaths(new ArrayList<>(paths));
        return allowedOperation;
    }

    /**
     * Build the InFlowExtensionEvent from FlowExecutionContext.
     *
     * @param context The FlowExecutionContext.
     * @return The InFlowExtensionEvent.
     */
    private InFlowExtensionEvent buildEvent(FlowContext context) {

        InFlowExtensionEvent.Builder eventBuilder = new InFlowExtensionEvent.Builder();

        // Set tenant information
        String tenantDomain = context.getValue(InFlowExtensionExecutor.TENET_DOMAIN_KEY, String.class);
        if (tenantDomain != null) {
            int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
            eventBuilder.tenant(new Tenant(String.valueOf(tenantId), tenantDomain));
        }

        // Set application information if available
        String applicationId = context.getValue(InFlowExtensionExecutor.APPLICATION_ID_KEY, String.class);
        if (applicationId != null) {
            eventBuilder.application(new Application(applicationId, null));
        }

        // Set user information from FlowUser
        FlowUser flowUser = context.getValue(InFlowExtensionExecutor.FLOW_USER_KEY, FlowUser.class);
        if (flowUser != null) {
            eventBuilder.user(buildUser(flowUser));

            // Set user store if available
            String userStoreDomain = flowUser.getUserStoreDomain();
            if (userStoreDomain != null) {
                eventBuilder.userStore(new UserStore(userStoreDomain));
            }
        }

        // Set flow-specific information
        eventBuilder.flowType(context.getValue(InFlowExtensionExecutor.FLOW_TYPE_KEY, String.class));

        // Set current node ID if available
        NodeConfig currentNode = context.getValue(InFlowExtensionExecutor.CURRENT_NODE_KEY, NodeConfig.class);
        if (currentNode != null) {
            eventBuilder.currentNodeId(currentNode.getId());
        }

        // Set user inputs
        eventBuilder.userInputs(context.getValue(InFlowExtensionExecutor.FLOW_USER_INPUT_DATA_KEY, Map.class));

        // Set flow properties (filtering out sensitive data)
        eventBuilder.flowProperties(filterFlowProperties(context.getValue(InFlowExtensionExecutor.FLOW_PROPERTIES_KEY, Map.class)));

        return eventBuilder.build();
    }

    /**
     * Build the User model from FlowUser.
     *
     * @param flowUser The FlowUser from flow context.
     * @return The User model for the event.
     */
    private User buildUser(FlowUser flowUser) {

        User.Builder userBuilder = new User.Builder(flowUser.getUserId());

        // Convert FlowUser claims to UserClaims
        Map<String, String> claims = flowUser.getClaims();
        if (claims != null && !claims.isEmpty()) {
            List<UserClaim> userClaims = new ArrayList<>();
            for (Map.Entry<String, String> claim : claims.entrySet()) {
                userClaims.add(new UserClaim(claim.getKey(), claim.getValue()));
            }
            userBuilder.claims(userClaims);
        }

        return userBuilder.build();
    }

    /**
     * Filter flow properties to remove sensitive data before sending to external service.
     *
     * @param properties The original flow properties.
     * @return Filtered properties map.
     */
    private Map<String, Object> filterFlowProperties(Map<String, Object> properties) {

        if (properties == null) {
            return new HashMap<>();
        }

        Map<String, Object> filtered = new HashMap<>();
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            String key = entry.getKey();
            // Filter out sensitive keys
            if (!isSensitiveKey(key)) {
                filtered.put(key, entry.getValue());
            }
        }
        return filtered;
    }

    /**
     * Check if a property key is sensitive and should be filtered out.
     *
     * @param key The property key.
     * @return true if the key is sensitive, false otherwise.
     */
    private boolean isSensitiveKey(String key) {

        if (key == null) {
            return false;
        }
        String lowerKey = key.toLowerCase(Locale.ENGLISH);
        return lowerKey.contains("password") ||
                lowerKey.contains("secret") ||
                lowerKey.contains("credential") ||
                lowerKey.contains("token") ||
                lowerKey.contains("key");
    }
}
