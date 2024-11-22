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
import org.wso2.carbon.identity.action.management.exception.ActionMgtClientException;
import org.wso2.carbon.identity.action.management.exception.ActionPropertyResolverException;
import org.wso2.carbon.identity.action.management.model.Action;

import java.util.Collections;
import java.util.Map;

/**
 * This interface defines the Action Property Resolver.
 * Action Property Resolver is the component that is responsible for handling action type specific operations.
 */
public interface ActionPropertyResolver {

    Action.ActionTypes getSupportedActionType();

    default Map<String, String> addProperties(ActionDTO actionDTO, String tenantDomain)
            throws ActionPropertyResolverException {

        return Collections.emptyMap();
    }

    default Map<String, Object> getProperties(ActionDTO actionDTO, String tenantDomain)
            throws ActionPropertyResolverException {

        return null;
    }

    default Map<String, String> updateProperties(ActionDTO updatingActionDTO, ActionDTO existingActionDTO,
                                                 String tenantDomain) throws ActionPropertyResolverException {

        return Collections.emptyMap();
    }

    default void deleteProperties(ActionDTO deletingActionDTO, String tenantDomain)
            throws ActionPropertyResolverException {
    }

    default void doPreAddActionPropertiesValidations(ActionDTO actionDTO) throws ActionMgtClientException {
    }

    default void doPreUpdateActionPropertiesValidations(ActionDTO updatingActionDTO) throws ActionMgtClientException {
    }
}
