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

import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.model.ActionDTO;
import org.wso2.carbon.identity.action.management.api.model.ActionProperty;
import org.wso2.carbon.identity.action.management.api.service.ActionConverter;
import org.wso2.carbon.identity.flow.execution.engine.inflow.extension.model.AccessConfig;
import org.wso2.carbon.identity.flow.execution.engine.inflow.extension.model.InFlowExtensionAction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.flow.execution.engine.inflow.extension.management.InFlowExtensionActionConstants.ACCESS_CONFIG_ALLOWED_OPERATIONS;
import static org.wso2.carbon.identity.flow.execution.engine.inflow.extension.management.InFlowExtensionActionConstants.ACCESS_CONFIG_ALLOWED_OPERATIONS_PREFIX;
import static org.wso2.carbon.identity.flow.execution.engine.inflow.extension.management.InFlowExtensionActionConstants.ACCESS_CONFIG_EXPOSE;
import static org.wso2.carbon.identity.flow.execution.engine.inflow.extension.management.InFlowExtensionActionConstants.ACCESS_CONFIG_EXPOSE_PREFIX;

/**
 * ActionConverter implementation for In-Flow Extension actions.
 * <p>
 * Handles the conversion between {@link InFlowExtensionAction} (domain model) and
 * {@link ActionDTO} (data transfer object) by mapping the {@link AccessConfig} fields
 * to/from action properties.
 * </p>
 */
public class InFlowExtensionActionConverter implements ActionConverter {

    @Override
    public Action.ActionTypes getSupportedActionType() {

        return Action.ActionTypes.IN_FLOW_EXTENSION;
    }

    /**
     * Converts an {@link InFlowExtensionAction} to an {@link ActionDTO} for persistence.
     * Maps the access config fields (expose, allowedOperations) and flow type overrides
     * into the DTO's properties map using prefixed keys.
     *
     * @param action The InFlowExtensionAction to convert.
     * @return ActionDTO with access config properties.
     */
    @Override
    public ActionDTO buildActionDTO(Action action) {

        if (!(action instanceof InFlowExtensionAction)) {
            return new ActionDTO.Builder(action).build();
        }

        InFlowExtensionAction inFlowExtensionAction = (InFlowExtensionAction) action;
        AccessConfig accessConfig = inFlowExtensionAction.getAccessConfig();

        Map<String, ActionProperty> properties = new HashMap<>();
        // Default access config (no prefix).
        if (accessConfig != null) {
            if (accessConfig.getExpose() != null) {
                properties.put(ACCESS_CONFIG_EXPOSE,
                        new ActionProperty.BuilderForService(accessConfig.getExpose()).build());
            }
            if (accessConfig.getAllowedOperations() != null) {
                properties.put(ACCESS_CONFIG_ALLOWED_OPERATIONS,
                        new ActionProperty.BuilderForService(accessConfig.getAllowedOperations()).build());
            }
        }

        // Per-flow-type overrides using prefixed keys.
        Map<String, AccessConfig> overrides = inFlowExtensionAction.getFlowTypeOverrides();
        if (overrides != null) {
            for (Map.Entry<String, AccessConfig> entry : overrides.entrySet()) {
                String flowType = entry.getKey();
                AccessConfig overrideConfig = entry.getValue();
                if (overrideConfig.getExpose() != null) {
                    properties.put(ACCESS_CONFIG_EXPOSE_PREFIX + flowType,
                            new ActionProperty.BuilderForService(overrideConfig.getExpose()).build());
                }
                if (overrideConfig.getAllowedOperations() != null) {
                    properties.put(ACCESS_CONFIG_ALLOWED_OPERATIONS_PREFIX + flowType,
                            new ActionProperty.BuilderForService(overrideConfig.getAllowedOperations()).build());
                }
            }
        }

        return new ActionDTO.Builder(inFlowExtensionAction)
                .properties(properties)
                .build();
    }

    /**
     * Converts an {@link ActionDTO} back to an {@link InFlowExtensionAction}.
     * Reconstructs the default {@link AccessConfig} and per-flow-type overrides from the DTO's properties map.
     *
     * @param actionDTO The ActionDTO to convert.
     * @return InFlowExtensionAction with access config and overrides populated.
     */
    @SuppressWarnings("unchecked")
    @Override
    public Action buildAction(ActionDTO actionDTO) {

        // Default access config.
        List<String> expose = (List<String>) actionDTO.getPropertyValue(ACCESS_CONFIG_EXPOSE);
        List<Map<String, Object>> allowedOperations =
                (List<Map<String, Object>>) actionDTO.getPropertyValue(ACCESS_CONFIG_ALLOWED_OPERATIONS);

        AccessConfig accessConfig = null;
        if (expose != null || allowedOperations != null) {
            accessConfig = new AccessConfig(expose, allowedOperations);
        }

        // Reconstruct per-flow-type overrides from prefixed keys.
        Map<String, AccessConfig> flowTypeOverrides = new HashMap<>();
        if (actionDTO.getProperties() != null) {
            for (String propertyKey : actionDTO.getProperties().keySet()) {
                if (propertyKey.startsWith(ACCESS_CONFIG_EXPOSE_PREFIX)) {
                    String flowType = propertyKey.substring(ACCESS_CONFIG_EXPOSE_PREFIX.length());
                    AccessConfig existing = flowTypeOverrides.getOrDefault(flowType, new AccessConfig(null, null));
                    flowTypeOverrides.put(flowType, new AccessConfig(
                            (List<String>) actionDTO.getPropertyValue(propertyKey),
                            existing.getAllowedOperations()));
                } else if (propertyKey.startsWith(ACCESS_CONFIG_ALLOWED_OPERATIONS_PREFIX)) {
                    String flowType = propertyKey.substring(ACCESS_CONFIG_ALLOWED_OPERATIONS_PREFIX.length());
                    AccessConfig existing = flowTypeOverrides.getOrDefault(flowType, new AccessConfig(null, null));
                    flowTypeOverrides.put(flowType, new AccessConfig(
                            existing.getExpose(),
                            (List<Map<String, Object>>) actionDTO.getPropertyValue(propertyKey)));
                }
            }
        }

        return new InFlowExtensionAction.ResponseBuilder()
                .id(actionDTO.getId())
                .type(actionDTO.getType())
                .name(actionDTO.getName())
                .description(actionDTO.getDescription())
                .status(actionDTO.getStatus())
                .actionVersion(actionDTO.getActionVersion())
                .createdAt(actionDTO.getCreatedAt())
                .updatedAt(actionDTO.getUpdatedAt())
                .endpoint(actionDTO.getEndpoint())
                .accessConfig(accessConfig)
                .flowTypeOverrides(flowTypeOverrides)
                .rule(actionDTO.getActionRule())
                .build();
    }
}
