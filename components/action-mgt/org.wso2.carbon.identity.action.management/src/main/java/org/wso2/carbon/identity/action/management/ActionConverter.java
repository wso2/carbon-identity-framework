/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.action.management;

import org.wso2.carbon.identity.action.management.dao.model.ActionDTO;
import org.wso2.carbon.identity.action.management.model.Action;

/**
 * This interface defines the Action ActionConverter.
 * Action ActionConverter is the component that is responsible for the conversions between Action and ExtendedAction
 * objects.
 */
public interface ActionConverter {

    Action.ActionTypes getSupportedActionType();

    /**
     * Convert Action object into ActionDTO object.
     *
     * @param action Action object.
     * @return ActionDTO object.
     */
    default ActionDTO buildActionDTO(Action action) {

        return new ActionDTO.Builder()
                .id(action.getId())
                .type(action.getType())
                .name(action.getName())
                .description(action.getDescription())
                .status(action.getStatus())
                .endpoint(action.getEndpoint())
                .build();
    }
    /**
     * Convert ActionDTO object into Action object.
     *
     * @param actionDTO ActionDTO object.
     * @return Action object.
     */
    default Action buildAction(ActionDTO actionDTO) {

        return new Action.ActionResponseBuilder()
                .id(actionDTO.getId())
                .type(actionDTO.getType())
                .name(actionDTO.getName())
                .description(actionDTO.getDescription())
                .status(actionDTO.getStatus())
                .endpoint(actionDTO.getEndpoint())
                .build();
    }
}
