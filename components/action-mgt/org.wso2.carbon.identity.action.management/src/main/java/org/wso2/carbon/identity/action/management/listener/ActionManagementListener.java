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

package org.wso2.carbon.identity.action.management.listener;

import org.wso2.carbon.identity.action.management.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.model.Action;

/**
 * Provides a set of methods to act as listeners before and after key operations related to action management.
 * These listeners can be utilized to enforce additional checks, validations, or side-effects for
 * action management functions.
 */
public interface ActionManagementListener {

    /**
     * Get the execution order identifier for this listener.
     *
     * @return The execution order identifier integer value.
     */
    int getExecutionOrderId();

    /**
     * Get the default order identifier for this listener.
     *
     * @return default order id
     */
    int getDefaultOrderId();

    /**
     * Check whether the listener is enabled or not.
     *
     * @return true if enabled
     */
    boolean isEnable();

    /**
     * Invoked before a new action is added.
     *
     * @param actionType   Type of action to be added.
     * @param action       Action model to be added.
     * @param tenantDomain Tenant domain in which the action will be added.
     * @throws ActionMgtException if an error occurs.
     */
    void preAddAction(String actionType, Action action, String tenantDomain) throws ActionMgtException;

    /**
     * Invoked after a new action is added.
     *
     * @param actionType   Type of action added.
     * @param action       Added action model.
     * @param tenantDomain Tenant domain in which the added action existed.
     * @throws ActionMgtException if an error occurs.
     */
    void postAddAction(String actionType, Action action, String tenantDomain) throws ActionMgtException;

    /**
     * Invoked before an action is added.
     *
     * @param actionType   Type of action to be updated.
     * @param actionId     ID of the action to be updated.
     * @param action       Action model to be updated.
     * @param tenantDomain Tenant domain in which the action to be updated exist.
     * @throws ActionMgtException if an error occurs.
     */
    void preUpdateAction(String actionType, String actionId, Action action, String tenantDomain)
            throws ActionMgtException;

    /**
     * Invoked after an action is added.
     *
     * @param actionType   Type of the action updated.
     * @param actionId     ID of the action updated.
     * @param action       Updated action model.
     * @param tenantDomain Tenant domain in which the updated action existed.
     * @throws ActionMgtException if an error occurs.
     */
    void postUpdateAction(String actionType, String actionId, Action action, String tenantDomain)
            throws ActionMgtException;

    /**
     * Invoked before an action is deleted.
     *
     * @param actionType   Type of action to be deleted.
     * @param actionId     ID of the action to be deleted.
     * @param tenantDomain Tenant domain in which the action to be deleted exist.
     * @throws ActionMgtException if an error occurs.
     */
    void preDeleteAction(String actionType, String actionId, String tenantDomain) throws ActionMgtException;

    /**
     * Invoked after an action is deleted.
     *
     * @param actionType   Type of the action deleted.
     * @param actionId     ID of the delete action.
     * @param tenantDomain Tenant domain in which the deleted action existed.
     * @throws ActionMgtException if an error occurs.
     */
    void postDeleteAction(String actionType, String actionId, String tenantDomain) throws ActionMgtException;

    /**
     * Invoked before an action is activated.
     *
     * @param actionType   Type of the action to be activated.
     * @param actionId     ID of the action to be activated.
     * @param tenantDomain Tenant domain in which the action to be activated exist.
     * @throws ActionMgtException if an error occurs.
     */
    void preActivateAction(String actionType, String actionId, String tenantDomain) throws ActionMgtException;

    /**
     * Invoked after an action is activated.
     *
     * @param actionType   Type of the action activated.
     * @param actionId     ID of the action activated.
     * @param tenantDomain Tenant domain in which the action to be activated exist.
     * @throws ActionMgtException if an error occurs.
     */
    void postActivateAction(String actionType, String actionId, String tenantDomain) throws ActionMgtException;

    /**
     * Invoked before an action is deactivated.
     *
     * @param actionType   Type of the action to be deactivated.
     * @param actionId     ID of the action to be deactivated.
     * @param tenantDomain Tenant domain in which the action to be deactivated exist.
     * @throws ActionMgtException if an error occurs.
     */
    void preDeactivateAction(String actionType, String actionId, String tenantDomain) throws ActionMgtException;

    /**
     * Invoked after an action is deactivated.
     *
     * @param actionType   Type of action deactivated.
     * @param actionId     ID of the action deactivated.
     * @param tenantDomain Tenant domain in which the action to be deactivated exist.
     * @throws ActionMgtException if an error occurs.
     */
    void postDeactivateAction(String actionType, String actionId, String tenantDomain) throws ActionMgtException;

}
