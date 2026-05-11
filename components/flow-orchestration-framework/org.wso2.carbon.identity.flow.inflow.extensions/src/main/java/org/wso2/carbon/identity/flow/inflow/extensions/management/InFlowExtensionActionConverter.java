/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.flow.inflow.extensions.management;

import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.model.ActionDTO;
import org.wso2.carbon.identity.action.management.api.model.ActionProperty;
import org.wso2.carbon.identity.action.management.api.service.ActionConverter;
import org.wso2.carbon.identity.certificate.management.model.Certificate;
import org.wso2.carbon.identity.flow.inflow.extensions.model.AccessConfig;
import org.wso2.carbon.identity.flow.inflow.extensions.model.Encryption;
import org.wso2.carbon.identity.flow.inflow.extensions.model.ContextPath;
import org.wso2.carbon.identity.flow.inflow.extensions.model.InFlowExtensionAction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.flow.inflow.extensions.InFlowExtensionConstants.ActionManagement.ACCESS_CONFIG_EXPOSE;
import static org.wso2.carbon.identity.flow.inflow.extensions.InFlowExtensionConstants.ActionManagement.ACCESS_CONFIG_EXPOSE_PREFIX;
import static org.wso2.carbon.identity.flow.inflow.extensions.InFlowExtensionConstants.ActionManagement.ACCESS_CONFIG_MODIFY;
import static org.wso2.carbon.identity.flow.inflow.extensions.InFlowExtensionConstants.ActionManagement.ACCESS_CONFIG_MODIFY_PREFIX;
import static org.wso2.carbon.identity.flow.inflow.extensions.InFlowExtensionConstants.ActionManagement.CERTIFICATE;
import static org.wso2.carbon.identity.flow.inflow.extensions.InFlowExtensionConstants.ActionManagement.ICON_URL;

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
     * Maps the access config fields (expose, modify) and flow type overrides
     * into the DTO's properties map using prefixed keys.
     *
     * @param action The InFlowExtensionAction to convert.
     * @return ActionDTO with access config properties.
     */
    @Override
    public ActionDTO buildActionDTO(Action action) {

        if (!(action instanceof InFlowExtensionAction inFlowExtensionAction)) {
            return new ActionDTO.Builder(action).build();
        }

        Map<String, ActionProperty> properties = new HashMap<>();
        putDefaultAccessConfigProperties(properties, inFlowExtensionAction.getAccessConfig());
        putEncryptionProperty(properties, inFlowExtensionAction.getEncryption());
        if (inFlowExtensionAction.getIconUrl() != null) {
            properties.put(ICON_URL,
                    new ActionProperty.BuilderForService(inFlowExtensionAction.getIconUrl()).build());
        }
        putFlowTypeOverrideProperties(properties, inFlowExtensionAction.getFlowTypeOverrides());

        return new ActionDTO.Builder(inFlowExtensionAction)
                .properties(properties)
                .build();
    }

    private void putDefaultAccessConfigProperties(Map<String, ActionProperty> properties, AccessConfig accessConfig) {

        if (accessConfig == null) {
            return;
        }
        if (accessConfig.getExpose() != null) {
            properties.put(ACCESS_CONFIG_EXPOSE,
                    new ActionProperty.BuilderForService(accessConfig.getExpose()).build());
        }
        if (accessConfig.getModify() != null) {
            properties.put(ACCESS_CONFIG_MODIFY,
                    new ActionProperty.BuilderForService(accessConfig.getModify()).build());
        }
    }

    private void putEncryptionProperty(Map<String, ActionProperty> properties, Encryption encryption) {

        if (encryption == null) {
            return;
        }
        if (encryption.getCertificate() != null) {
            properties.put(CERTIFICATE,
                    new ActionProperty.BuilderForService(encryption.getCertificate()).build());
        } else {
            // Encryption object present but no certificate — signals explicit removal.
            properties.put(CERTIFICATE,
                    new ActionProperty.BuilderForService("").build());
        }
    }

    private void putFlowTypeOverrideProperties(Map<String, ActionProperty> properties,
                                               Map<String, AccessConfig> overrides) {

        if (overrides == null) {
            return;
        }
        for (Map.Entry<String, AccessConfig> entry : overrides.entrySet()) {
            String flowType = entry.getKey();
            AccessConfig overrideConfig = entry.getValue();
            if (overrideConfig.getExpose() != null) {
                properties.put(ACCESS_CONFIG_EXPOSE_PREFIX + flowType,
                        new ActionProperty.BuilderForService(overrideConfig.getExpose()).build());
            }
            if (overrideConfig.getModify() != null) {
                properties.put(ACCESS_CONFIG_MODIFY_PREFIX + flowType,
                        new ActionProperty.BuilderForService(overrideConfig.getModify()).build());
            }
        }
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
        List<ContextPath> expose = (List<ContextPath>) actionDTO.getPropertyValue(ACCESS_CONFIG_EXPOSE);
        List<ContextPath> modify =
                (List<ContextPath>) actionDTO.getPropertyValue(ACCESS_CONFIG_MODIFY);

        AccessConfig accessConfig = null;
        if (expose != null || modify != null) {
            accessConfig = new AccessConfig(expose, modify);
        }

        // Encryption certificate (separate from access config).
        Encryption encryption = null;
        Object certValue = actionDTO.getPropertyValue(CERTIFICATE);
        if (certValue instanceof Certificate certificate) {
            encryption = new Encryption(certificate);
        }

        // Icon URL.
        String iconUrl = null;
        Object iconUrlValue = actionDTO.getPropertyValue(ICON_URL);
        if (iconUrlValue instanceof String iconUrlStr) {
            iconUrl = iconUrlStr;
        }

        // Reconstruct per-flow-type overrides from prefixed keys.
        Map<String, AccessConfig> flowTypeOverrides = new HashMap<>();
        if (actionDTO.getProperties() != null) {
            for (String propertyKey : actionDTO.getProperties().keySet()) {
                if (propertyKey.startsWith(ACCESS_CONFIG_EXPOSE_PREFIX)) {
                    String flowType = propertyKey.substring(ACCESS_CONFIG_EXPOSE_PREFIX.length());
                    AccessConfig existing = flowTypeOverrides.getOrDefault(flowType,
                            new AccessConfig(null, null));
                    flowTypeOverrides.put(flowType, new AccessConfig(
                            (List<ContextPath>) actionDTO.getPropertyValue(propertyKey),
                            existing.getModify()));
                } else if (propertyKey.startsWith(ACCESS_CONFIG_MODIFY_PREFIX)) {
                    String flowType = propertyKey.substring(ACCESS_CONFIG_MODIFY_PREFIX.length());
                    AccessConfig existing = flowTypeOverrides.getOrDefault(flowType,
                            new AccessConfig(null, null));
                    flowTypeOverrides.put(flowType, new AccessConfig(
                            existing.getExpose(),
                            (List<ContextPath>) actionDTO.getPropertyValue(propertyKey)));
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
                .encryption(encryption)
                .iconUrl(iconUrl)
                .flowTypeOverrides(flowTypeOverrides)
                .rule(actionDTO.getActionRule())
                .build();
    }
}
