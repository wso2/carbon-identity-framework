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

package org.wso2.carbon.identity.action.management.api.service;

import org.wso2.carbon.identity.action.management.api.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.api.model.Action;

/**
 * This interface to the validate action in the Action management service layer.
 */
public interface ActionValidator {

    /**
     * Get the action type that this validator supports.
     *
     * @return Supported action type.
     */
    Action.ActionTypes getSupportedActionType();

    /**
     * Perform pre validations on action model when creating an action.
     *
     * @param action Action creation model.
     * @throws ActionMgtException if action model is invalid.
     */
    void doPreAddActionValidations(Action.ActionTypes actionType, String actionVersion, Action action)
            throws ActionMgtException;

    /**
     * Perform pre validations on action model when updating an existing action.
     * This is specifically used during HTTP PATCH operation and only validate non-null and non-empty fields.
     *
     * @param action Action update model.
     * @throws ActionMgtException if action model is invalid.
     */
    void doPreUpdateActionValidations(Action.ActionTypes actionType, String actionVersion, Action action)
            throws ActionMgtException;
}
