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

package org.wso2.carbon.identity.action.management.dao;

import org.wso2.carbon.identity.action.management.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.model.Action;
import org.wso2.carbon.identity.action.management.model.AuthType;
import org.wso2.carbon.identity.action.management.model.EndpointConfig;

import java.util.List;
import java.util.Map;

/**
 * This interface performs CRUD operations for {@link Action}
 */
public interface ActionManagementDAO {

    /**
     * Create a new {@link Action}.
     *
     * @param actionType Action Type.
     * @param actionId   Action Id.
     * @param action     Action creation model.
     * @param tenantId   Tenant Id.
     * @return Created <code>Action</code>.
     * @throws ActionMgtException If an error occurs while adding the Action.
     */
    Action addAction(String actionType, String actionId, Action action, Integer tenantId) throws ActionMgtException;

    /**
     * Retrieve the Actions configured for the given type.
     *
     * @param actionType Action Type.
     * @param tenantId   Tenant Id.
     * @return List of <code>Action</code>.
     * @throws ActionMgtException If an error occurs while retrieving the Actions of a given Action Type.
     */
    List<Action> getActionsByActionType(String actionType, Integer tenantId) throws ActionMgtException;

    /**
     * Update {@link Action} by given Action type and Action ID.
     *
     * @param actionType     Action Type.
     * @param actionId       Action ID.
     * @param updatingAction Action update model.
     * @param existingAction Existing Action.
     * @param tenantId       Tenant Id.
     * @return Updated <code>Action</code>.
     * @throws ActionMgtException If an error occurs while updating the Action.
     */
    Action updateAction(String actionType, String actionId, Action updatingAction, Action existingAction,
                        Integer tenantId) throws ActionMgtException;

    /**
     * Delete {@link Action} by given Action Type.
     *
     * @param actionType Action Type.
     * @param actionId   Action Id.
     * @param action     Action to be deleted.
     * @param tenantId   Tenant Id.
     * @throws ActionMgtException If an error occurs while deleting Action.
     */
    void deleteAction(String actionType, String actionId, Action action, Integer tenantId) throws ActionMgtException;

    /**
     * Activate {@link Action} by given Action Type and Action ID.
     *
     * @param actionType   Action Type.
     * @param actionId     Action ID.
     * @param tenantId   Tenant Id.
     * @return Activated <code>Action</code>.
     * @throws ActionMgtException If an error occurs while activating the Action.
     */
    Action activateAction(String actionType, String actionId, Integer tenantId) throws ActionMgtException;

    /**
     * Deactivate {@link Action} by given Action Type and Action ID.
     *
     * @param actionType Action Type.
     * @param actionId   Action ID.
     * @param tenantId   Tenant Id.
     * @return Deactivated <code>Action</code>.
     * @throws ActionMgtException If an error occurs while deactivating the Action.
     */
    Action deactivateAction(String actionType, String actionId, Integer tenantId) throws ActionMgtException;

    /**
     * Get Actions count per Action Type.
     *
     * @param tenantId Tenant Id.
     * @return Map of Action count for configured action types.
     * @throws ActionMgtException If an error occurs while retrieving the Actions count.
     */
    Map<String, Integer> getActionsCountPerType(Integer tenantId) throws ActionMgtException;

    /**
     * Get {@link Action} of a given Action Type and Action ID.
     *
     * @param actionId Action ID.
     * @param tenantId Tenant Id.
     * @return <code>Action</code>.
     * @throws ActionMgtException If an error occurs while retrieving the Action of a given Action ID.
     */
    Action getActionByActionId(String actionId, Integer tenantId) throws ActionMgtException;

    /**
     * Update the endpoint authentication properties of an {@link Action} by given Action ID.
     *
     * @param actionId       Action ID.
     * @param authentication Authentication information to be updated.
     * @param tenantId       Tenant Id.
     * @return Updated <code>Action</code>.
     * @throws ActionMgtException If an error occurs while updating the Action endpoint authentication properties.
     */
    Action updateActionEndpointAuthProperties(String actionId, AuthType authentication, int tenantId)
            throws ActionMgtException;

    /**
     * Update the endpoint authentication properties of an {@link Action} by given Action ID.
     *
     * @param actionType            Action Type.
     * @param actionId              Action ID.
     * @param endpoint              Endpoint information to be updated.
     * @param currentAuthentication Current Action endpoint authentication information.
     * @param tenantId              Tenant Id.
     * @return Updated <code>Action</code>.
     * @throws ActionMgtException If an error occurs while updating the Action endpoint.
     */
    Action updateActionEndpoint(String actionType, String actionId, EndpointConfig endpoint,
                                AuthType currentAuthentication, int tenantId) throws ActionMgtException;
}
