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

package org.wso2.carbon.identity.flow.execution.engine.inflow.extension.management;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.action.management.api.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.service.ActionManagementService;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.flow.execution.engine.inflow.extension.model.AccessConfig;
import org.wso2.carbon.identity.flow.execution.engine.inflow.extension.model.ContextPath;
import org.wso2.carbon.identity.flow.execution.engine.inflow.extension.model.InFlowExtensionAction;
import org.wso2.carbon.identity.flow.execution.engine.internal.FlowExecutionEngineDataHolder;
import org.wso2.carbon.identity.flow.mgt.FlowUpdateInterceptor;
import org.wso2.carbon.identity.flow.mgt.exception.FlowMgtFrameworkException;
import org.wso2.carbon.identity.flow.mgt.exception.FlowMgtServerException;
import org.wso2.carbon.identity.flow.mgt.model.ExecutorDTO;
import org.wso2.carbon.identity.flow.mgt.model.GraphConfig;
import org.wso2.carbon.identity.flow.mgt.model.NodeConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.flow.execution.engine.inflow.extension.management.InFlowExtensionActionConstants.ACCESS_CONFIG_METADATA_KEY;

/**
 * Flow update interceptor that extracts access config overrides from executor metadata
 * and stores them as per-flow-type action properties.
 * <p>
 * This interceptor is invoked <b>before</b> the flow graph is persisted. It:
 * <ol>
 *   <li>Iterates graph nodes looking for In-Flow Extension executors with an
 *       {@code accessConfig} metadata entry.</li>
 *   <li>Parses the unified {@code accessConfig} JSON object
 *       ({@code {"expose": [...], "modify": [...]}}).</li>
 *   <li>Saves the parsed data as per-flow-type override properties on the corresponding action
 *       via {@code ActionManagementService.updateAction()}.</li>
 *   <li><b>Strips</b> the {@code accessConfig} key from executor metadata so it is NOT persisted
 *       to the flow executor metadata table (avoiding VARCHAR(255) column size limitations).</li>
 * </ol>
 * </p>
 */
public class AccessConfigFlowUpdateInterceptor implements FlowUpdateInterceptor {

    private static final Log LOG = LogFactory.getLog(AccessConfigFlowUpdateInterceptor.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String IN_FLOW_EXTENSION_EXECUTOR_NAME = "ExtensionExecutor";
    private static final TypeReference<Map<String, Object>> MAP_TYPE_REF =
            new TypeReference<Map<String, Object>>() { };

    private static final String ACTION_ID_METADATA_KEY = "actionId";
    private static final String EXPOSE_FIELD = "expose";
    private static final String MODIFY_FIELD = "modify";

    @Override
    public void onFlowUpdate(String flowType, GraphConfig graphConfig, int tenantId)
            throws FlowMgtFrameworkException {

        if (graphConfig == null || graphConfig.getNodeConfigs() == null) {
            return;
        }

        ActionManagementService actionMgtService =
                FlowExecutionEngineDataHolder.getInstance().getActionManagementService();
        if (actionMgtService == null) {
            LOG.warn("ActionManagementService is not available. Skipping access config override processing.");
            return;
        }

        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);

        for (NodeConfig node : graphConfig.getNodeConfigs().values()) {
            processNode(node, flowType, tenantDomain, actionMgtService);
        }
    }

    /**
     * Process a single node: extract unified accessConfig from executor metadata, save as action
     * properties, and strip the accessConfig key from metadata before DAO persistence.
     */
    @SuppressWarnings("unchecked")
    private void processNode(NodeConfig node, String flowType, String tenantDomain,
                             ActionManagementService actionMgtService) throws FlowMgtFrameworkException {

        ExecutorDTO executorConfig = node.getExecutorConfig();
        if (executorConfig == null || !executorConfig.getName().equals(IN_FLOW_EXTENSION_EXECUTOR_NAME)
                ||executorConfig.getMetadata() == null) {
            return;
        }

        Map<String, String> metadata = executorConfig.getMetadata();
        String actionId = metadata.get(ACTION_ID_METADATA_KEY);
        if (actionId == null || actionId.isEmpty()) {
            return;
        }

        String accessConfigJson = metadata.get(ACCESS_CONFIG_METADATA_KEY);
        if (accessConfigJson == null) {
            return;
        }

        // Always strip the accessConfig key from metadata — it must NOT be persisted
        // to executor metadata (VARCHAR(255) column size limitation).
        metadata.remove(ACCESS_CONFIG_METADATA_KEY);

        try {
            // Parse the unified accessConfig JSON: {"expose": [...], "modify": [...]}
            Map<String, Object> accessConfigMap = OBJECT_MAPPER.readValue(accessConfigJson, MAP_TYPE_REF);

            List<ContextPath> expose = parseContextPaths(accessConfigMap.get(EXPOSE_FIELD));
            List<ContextPath> modify = parseContextPaths(accessConfigMap.get(MODIFY_FIELD));
            // Certificate is action-level only — not overridable per flow type.
            AccessConfig overrideConfig = new AccessConfig(expose, modify);

            // Retrieve the existing action to merge overrides (preserving other flow types).
            Action currentAction = actionMgtService.getActionByActionId(
                    Action.ActionTypes.IN_FLOW_EXTENSION.getPathParam(), actionId, tenantDomain);
            if (!(currentAction instanceof InFlowExtensionAction)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Action " + actionId + " is not an InFlowExtensionAction. Skipping override.");
                }
                return;
            }

            InFlowExtensionAction existingAction = (InFlowExtensionAction) currentAction;

            // Merge: preserve other flow types, add/replace this one.
            Map<String, AccessConfig> overrides = new HashMap<>(existingAction.getFlowTypeOverrides());
            overrides.put(flowType, overrideConfig);

            InFlowExtensionAction updatedAction = new InFlowExtensionAction.RequestBuilder(existingAction)
                    .accessConfig(existingAction.getAccessConfig())
                    .flowTypeOverrides(overrides)
                    .build();

            actionMgtService.updateAction(
                    Action.ActionTypes.IN_FLOW_EXTENSION.getPathParam(),
                    actionId, updatedAction, tenantDomain);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Updated access config override for action " + actionId
                        + " in flow type " + flowType + " and stripped from executor metadata.");
            }

        } catch (ActionMgtException e) {
            throw new FlowMgtServerException("FLOW-60010",
                    "Error updating access config override.",
                    "Error updating access config override for action: " + actionId, e);
        } catch (JsonProcessingException e) {
            throw new FlowMgtServerException("FLOW-60011",
                    "Error parsing access config metadata.",
                    "Error parsing access config metadata for action: " + actionId, e);
        }
    }

    /**
     * Parse expose entries from the raw JSON value.
     * Accepts both simple strings (no encryption) and objects ({path, encrypted}).
     */
    @SuppressWarnings("unchecked")
    private List<ContextPath> parseContextPaths(Object value) {

        if (value == null) {
            return null;
        }
        if (!(value instanceof List)) {
            LOG.warn("Invalid expose value in flow override accessConfig. Expected list.");
            return null;
        }

        List<?> rawList = (List<?>) value;
        List<ContextPath> result = new ArrayList<>();
        for (Object item : rawList) {
            if (item instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) item;
                String path = (String) map.get("path");
                boolean encrypted = toBooleanSafe(map.get("encrypted"));
                if (path != null) {
                    result.add(new ContextPath(path, encrypted));
                }
            }
            else {

            }
        }
        return result.isEmpty() ? null : result;
    }

    /**
     * Safely converts a value to boolean, handling both {@link Boolean} and {@link String} types.
     * Jackson deserializes JSON {@code true} as {@link Boolean} but JSON {@code "true"} as {@link String}.
     *
     * @param value The value to convert.
     * @return {@code true} if the value is Boolean TRUE or the string "true" (case-insensitive).
     */
    private static boolean toBooleanSafe(Object value) {

        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return false;
    }
}
