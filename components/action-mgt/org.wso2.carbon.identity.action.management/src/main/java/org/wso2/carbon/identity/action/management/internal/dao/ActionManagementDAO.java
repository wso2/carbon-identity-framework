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

package org.wso2.carbon.identity.action.management.internal.dao;

import org.wso2.carbon.identity.action.management.api.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.model.ActionDTO;

import java.util.List;
import java.util.Map;

/**
 * This interface performs CRUD operations for {@link ActionDTO}.
 */
public interface ActionManagementDAO {

    /**
     * Create a new {@link ActionDTO}.
     *
     * @param actionDTO Action creation model.
     * @param tenantId  Tenant Id.
     * @throws ActionMgtException If an error occurs while adding the Action.
     */
    void addAction(ActionDTO actionDTO, Integer tenantId) throws ActionMgtException;

    /**
     * Retrieve the Actions configured for the given type.
     *
     * @param actionType Action Type.
     * @param tenantId   Tenant Id.
     * @return List of <code>Action</code>.
     * @throws ActionMgtException If an error occurs while retrieving the Actions of a given Action Type.
     */
    List<ActionDTO> getActionsByActionType(String actionType, Integer tenantId) throws ActionMgtException;

    /**
     * Get {@link ActionDTO} of a given Action Type and Action ID.
     *
     * @param actionId Action ID.
     * @param tenantId Tenant Id.
     * @return <code>Action</code>.
     * @throws ActionMgtException If an error occurs while retrieving the Action of a given Action ID.
     */
    ActionDTO getActionByActionId(String actionType, String actionId, Integer tenantId) throws ActionMgtException;

    /**
     * Update {@link ActionDTO} by given Action type and Action ID.
     *
     * @param updatingActionDTO Action update model.
     * @param existingActionDTO Existing Action.
     * @param tenantId          Tenant Id.
     * @throws ActionMgtException If an error occurs while updating the Action.
     */
    void updateAction(ActionDTO updatingActionDTO, ActionDTO existingActionDTO, Integer tenantId)
            throws ActionMgtException;

    /**
     * Delete {@link ActionDTO} by given Action Type.
     *
     * @param deletingActionDTO Action to be deleted.
     * @param tenantId          Tenant Id.
     * @throws ActionMgtException If an error occurs while deleting Action.
     */
    void deleteAction(ActionDTO deletingActionDTO, Integer tenantId) throws ActionMgtException;

    /**
     * Activate {@link Action} by given Action Type and Action ID.
     *
     * @param actionType Action Type.
     * @param actionId   Action ID.
     * @param tenantId   Tenant Id.
     * @return Activated <code>Action</code>.
     * @throws ActionMgtException If an error occurs while activating the Action.
     */
    ActionDTO activateAction(String actionType, String actionId, Integer tenantId) throws ActionMgtException;

    /**
     * Deactivate {@link Action} by given Action Type and Action ID.
     *
     * @param actionType Action Type.
     * @param actionId   Action ID.
     * @param tenantId   Tenant Id.
     * @return Deactivated <code>Action</code>.
     * @throws ActionMgtException If an error occurs while deactivating the Action.
     */
    ActionDTO deactivateAction(String actionType, String actionId, Integer tenantId) throws ActionMgtException;

    /**
     * Get Actions count per Action Type.
     *
     * @param tenantId Tenant Id.
     * @return Map of Action count for configured action types.
     * @throws ActionMgtException If an error occurs while retrieving the Actions count.
     */
    Map<String, Integer> getActionsCountPerType(Integer tenantId) throws ActionMgtException;
}
