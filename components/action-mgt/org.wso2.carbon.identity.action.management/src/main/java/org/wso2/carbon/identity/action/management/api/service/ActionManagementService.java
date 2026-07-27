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

package org.wso2.carbon.identity.action.management.api.service;

import org.wso2.carbon.identity.action.management.api.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.model.Authentication;

import java.util.List;
import java.util.Map;

/**
 * Action Manager Interface.
 */
public interface ActionManagementService {

    /**
     * Add Action to the given action type.
     *
     * @param actionType   Action Type.
     * @param action       Action creation model.
     * @param tenantDomain Tenant domain.
     * @return Action creation response.
     * @throws ActionMgtException If an error occurs while adding the Action.
     */
    Action addAction(String actionType, Action action, String tenantDomain) throws ActionMgtException;

    /**
     * Get Actions of a given Action Type.
     *
     * @param actionType   Action Type.
     * @param tenantDomain Tenant domain.
     * @return List of Actions.
     * @throws ActionMgtException If an error occurs while retrieving the Actions of a given Action Type.
     */
    List<Action> getActionsByActionType(String actionType, String tenantDomain) throws ActionMgtException;

    /**
     * Update Action by given Action type and Action ID.
     *
     * @param actionType   Action Type.
     * @param actionId     Action ID.
     * @param action       Action update model.
     * @param tenantDomain Tenant domain.
     * @return Action response after the update.
     * @throws ActionMgtException If an error occurs while updating the Action.
     */
    Action updateAction(String actionType, String actionId, Action action, String tenantDomain)
            throws ActionMgtException;

    /**
     * Delete Action by given Action Type.
     *
     * @param actionType   Action Type.
     * @param actionId     Action ID.
     * @param tenantDomain Tenant domain.
     * @throws ActionMgtException If an error occurs while deleting Action.
     */
    void deleteAction(String actionType, String actionId, String tenantDomain) throws ActionMgtException;

    /**
     * Activate Action by given Action Type and Action ID.
     *
     * @param actionType   Action Type.
     * @param actionId     Action ID.
     * @param tenantDomain Tenant domain.
     * @return Action response after the activation.
     * @throws ActionMgtException If an error occurs while activating the Action.
     */
    Action activateAction(String actionType, String actionId, String tenantDomain) throws ActionMgtException;

    /**
     * Deactivate Action by given Action Type and Action ID.
     *
     * @param actionType   Action Type.
     * @param actionId     Action ID.
     * @param tenantDomain Tenant domain.
     * @return Action response after the deactivation.
     * @throws ActionMgtException If an error occurs while deactivating the Action.
     */
    Action deactivateAction(String actionType, String actionId, String tenantDomain) throws ActionMgtException;

    /**
     * Get Actions count per Action Type.
     *
     * @param tenantDomain Tenant domain.
     * @return Map of Action count for configured action types.
     * @throws ActionMgtException If an error occurs while retrieving the Actions count.
     */
    Map<String, Integer> getActionsCountPerType(String tenantDomain) throws ActionMgtException;

    /**
     * Get Action of a given Action ID.
     *
     * @param actionType   Action Type.
     * @param actionId     Action Id.
     * @param tenantDomain Tenant domain.
     * @return Action response.
     * @throws ActionMgtException If an error occurs while retrieving the Action of a given Action ID.
     */
    Action getActionByActionId(String actionType, String actionId, String tenantDomain) throws ActionMgtException;

    /**
     * Update the authentication of the action endpoint.
     *
     * @param actionType     Action Type.
     * @param actionId       Action ID.
     * @param authentication Authentication Information to be updated.
     * @param tenantDomain   Tenant domain.
     * @return Action response after update.
     * @throws ActionMgtException If an error occurs while updating action endpoint authentication information.
     */
    Action updateActionEndpointAuthentication(String actionType, String actionId, Authentication authentication,
                                              String tenantDomain) throws ActionMgtException;
}
