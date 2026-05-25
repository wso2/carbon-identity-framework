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

package org.wso2.carbon.identity.flow.extension.management;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.model.ActionDTO;
import org.wso2.carbon.identity.action.management.api.model.ActionProperty;
import org.wso2.carbon.identity.action.management.api.service.ActionConverter;
import org.wso2.carbon.identity.certificate.management.model.Certificate;
import org.wso2.carbon.identity.flow.extension.model.AccessConfig;
import org.wso2.carbon.identity.flow.extension.model.ContextPath;
import org.wso2.carbon.identity.flow.extension.model.FlowExtensionAction;

import com.ctc.wstx.util.StringUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.flow.extension.FlowExtensionConstants.ActionManagement.ACCESS_CONFIG_EXPOSE;
import static org.wso2.carbon.identity.flow.extension.FlowExtensionConstants.ActionManagement.ACCESS_CONFIG_MODIFY;
import static org.wso2.carbon.identity.flow.extension.FlowExtensionConstants.ActionManagement.CERTIFICATE;
import static org.wso2.carbon.identity.flow.extension.FlowExtensionConstants.ActionManagement.ICON_URL;

/**
 * ActionConverter implementation for Flow Extension actions.
 * <p>
 * Handles the conversion between {@link FlowExtensionAction} (domain model) and
 * {@link ActionDTO} (data transfer object) by mapping the {@link AccessConfig} fields
 * to/from action properties.
 * </p>
 */
public class FlowExtensionActionConverter implements ActionConverter {

    @Override
    public Action.ActionTypes getSupportedActionType() {

        return Action.ActionTypes.FLOW_EXTENSION;
    }

    /**
     * Converts an {@link FlowExtensionAction} to an {@link ActionDTO} for persistence.
     * Maps the access config fields (expose, modify) into the DTO's properties map.
     *
     * @param action The FlowExtensionAction to convert.
     * @return ActionDTO with access config properties.
     */
    @Override
    public ActionDTO buildActionDTO(Action action) {

        if (!(action instanceof FlowExtensionAction flowExtensionAction)) {
            return new ActionDTO.Builder(action).build();
        }

        Map<String, ActionProperty> properties = new HashMap<>();
        putDefaultAccessConfigProperties(properties, flowExtensionAction.getAccessConfig());
        putCertificateProperty(properties, flowExtensionAction.getCertificate());
        if (flowExtensionAction.getIconUrl() != null) {
            properties.put(ICON_URL,
                    new ActionProperty.BuilderForService(flowExtensionAction.getIconUrl()).build());
        }

        return new ActionDTO.Builder(flowExtensionAction)
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

    private void putCertificateProperty(Map<String, ActionProperty> properties, Certificate certificate) {

        if (certificate == null) {
            return;
        }
        String content = certificate.getCertificateContent();
        if (StringUtils.isBlank(content)) {
            properties.put(CERTIFICATE,
                    new ActionProperty.BuilderForService(certificate).build());
        } else {
            // Certificate with no content — signals explicit removal at the resolver layer.
            properties.put(CERTIFICATE,
                    new ActionProperty.BuilderForService("").build());
        }
    }

    /**
     * Converts an {@link ActionDTO} back to an {@link FlowExtensionAction}.
     * Reconstructs the default {@link AccessConfig} from the DTO's properties map.
     *
     * @param actionDTO The ActionDTO to convert.
     * @return FlowExtensionAction with access config populated.
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

        // External service certificate (separate from access config).
        Certificate certificate = null;
        Object certValue = actionDTO.getPropertyValue(CERTIFICATE);
        if (certValue instanceof Certificate cert) {
            certificate = cert;
        }

        // Icon URL.
        String iconUrl = null;
        Object iconUrlValue = actionDTO.getPropertyValue(ICON_URL);
        if (iconUrlValue instanceof String iconUrlStr) {
            iconUrl = iconUrlStr;
        }

        return new FlowExtensionAction.ResponseBuilder()
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
                .certificate(certificate)
                .iconUrl(iconUrl)
                .rule(actionDTO.getActionRule())
                .build();
    }
}
