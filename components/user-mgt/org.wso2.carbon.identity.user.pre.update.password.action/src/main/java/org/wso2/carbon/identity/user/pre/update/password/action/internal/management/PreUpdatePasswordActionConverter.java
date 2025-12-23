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

package org.wso2.carbon.identity.user.pre.update.password.action.internal.management;

import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.model.ActionDTO;
import org.wso2.carbon.identity.action.management.api.model.ActionProperty;
import org.wso2.carbon.identity.action.management.api.service.ActionConverter;
import org.wso2.carbon.identity.certificate.management.model.Certificate;
import org.wso2.carbon.identity.user.pre.update.password.action.api.model.PasswordSharing;
import org.wso2.carbon.identity.user.pre.update.password.action.api.model.PreUpdatePasswordAction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.user.pre.update.password.action.internal.constant.PreUpdatePasswordActionConstants.ATTRIBUTES;
import static org.wso2.carbon.identity.user.pre.update.password.action.internal.constant.PreUpdatePasswordActionConstants.CERTIFICATE;
import static org.wso2.carbon.identity.user.pre.update.password.action.internal.constant.PreUpdatePasswordActionConstants.PASSWORD_SHARING_FORMAT;

/**
 * This class implements the methods required to build Action objects in Pre Update Password extension.
 */
public class PreUpdatePasswordActionConverter implements ActionConverter {

    @Override
    public Action.ActionTypes getSupportedActionType() {

        return Action.ActionTypes.PRE_UPDATE_PASSWORD;
    }

    /**
     * This method builds the ActionDTO object from the PreUpdatePasswordAction object in action creation and update.
     *
     * @param action PreUpdatePasswordAction object.
     * @return ActionDTO object.
     *
     */
    @Override
    public ActionDTO buildActionDTO(Action action) {

        PreUpdatePasswordAction preUpdatePasswordAction = (PreUpdatePasswordAction) action;
        PasswordSharing passwordSharing = preUpdatePasswordAction.getPasswordSharing();
        List<String> attributes = preUpdatePasswordAction.getAttributes();

        Map<String, ActionProperty> properties = new HashMap<>();
        if (passwordSharing != null && passwordSharing.getFormat() != null) {
            properties.put(PASSWORD_SHARING_FORMAT, new ActionProperty.BuilderForService(passwordSharing.getFormat())
                    .build());
        }
        if (passwordSharing != null && passwordSharing.getCertificate() != null) {
            properties.put(CERTIFICATE, new ActionProperty.BuilderForService(passwordSharing.getCertificate()).build());
        }
        if (attributes != null) {
            properties.put(ATTRIBUTES, new ActionProperty.BuilderForService(attributes).build());
        }

        return new ActionDTO.Builder(preUpdatePasswordAction)
                .properties(properties)
                .build();
    }

    @Override
    public Action buildAction(ActionDTO actionDTO) {

        PasswordSharing passwordSharing = new PasswordSharing.Builder()
                .format((PasswordSharing.Format) actionDTO.getPropertyValue(PASSWORD_SHARING_FORMAT))
                .certificate((Certificate) actionDTO.getPropertyValue(CERTIFICATE))
                .build();
        List<String> attributes = (List<String>) actionDTO.getPropertyValue(ATTRIBUTES);

        return new PreUpdatePasswordAction.ResponseBuilder()
                .id(actionDTO.getId())
                .type(actionDTO.getType())
                .name(actionDTO.getName())
                .description(actionDTO.getDescription())
                .status(actionDTO.getStatus())
                .actionVersion(actionDTO.getActionVersion())
                .createdAt(actionDTO.getCreatedAt())
                .updatedAt(actionDTO.getUpdatedAt())
                .endpoint(actionDTO.getEndpoint())
                .passwordSharing(passwordSharing)
                .attributes(attributes)
                .rule(actionDTO.getActionRule())
                .build();
    }
}
