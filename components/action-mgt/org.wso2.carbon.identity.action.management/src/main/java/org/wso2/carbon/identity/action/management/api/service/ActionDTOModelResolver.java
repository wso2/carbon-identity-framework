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

import org.wso2.carbon.identity.action.management.api.exception.ActionDTOModelResolverException;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.model.ActionDTO;

import java.util.List;

/**
 * This interface defines the Action Property Resolver executed in the Action management dao facade layer.
 * Action Property Resolver is the component that is responsible for handling action type specific operations.
 */
public interface ActionDTOModelResolver {

    Action.ActionTypes getSupportedActionType();

    /**
     * Resolve the properties that need to be added in the Action Management Service.
     * This method is responsible for performing necessary CRUD operations for the properties that need to be added
     * using other external services.
     * The Action Management Service ensures that only the properties returned by this method are stored.
     *
     * @param actionDTO    ActionDTO object.
     * @param tenantDomain Tenant domain.
     * @return ActionDTO object with resolved properties.
     * @throws ActionDTOModelResolverException If an error occurs while resolving the properties.
     */
    ActionDTO resolveForAddOperation(ActionDTO actionDTO, String tenantDomain) throws ActionDTOModelResolverException;

    /**
     * Populate the properties according to the references stored in the Action Management Service.
     * This method is responsible for populating the properties that need to be retrieved using other external services.
     * The Action Management Service ensures that only the properties populated by this method are included in the
     * returned ActionDTO object.
     *
     * @param actionDTO    ActionDTO object with properties references.
     * @param tenantDomain Tenant domain.
     * @return ActionDTO object with populated properties.
     * @throws ActionDTOModelResolverException If an error occurs while populating the properties.
     */
    ActionDTO resolveForGetOperation(ActionDTO actionDTO, String tenantDomain) throws ActionDTOModelResolverException;

    /**
     * Populate the properties of the given ActionDTO list according to the references stored in the Action Management
     * Service.
     * This method is responsible for populating the properties that need to be retrieved using other external services.
     * The Action Management Service ensures that only the properties populated by this method are included in the
     * returned ActionDTO object list.
     *
     * @param actionDTOList List of ActionDTO objects with properties references.
     * @param tenantDomain  Tenant domain.
     * @return List of ActionDTO objects with populated properties.
     * @throws ActionDTOModelResolverException If an error occurs while populating the properties.
     */
    List<ActionDTO> resolveForGetOperation(List<ActionDTO> actionDTOList, String tenantDomain)
            throws ActionDTOModelResolverException;

    /**
     * Resolve the properties that need to be updated in the Action Management Service.
     * This method is responsible for performing necessary CRUD operations for the properties that need to be updated
     * using other external services.
     * The Action Management Service ensures that only the properties returned by this method are updated.
     *
     * @param updatingActionDTO ActionDTO object with updated properties.
     * @param existingActionDTO ActionDTO object with existing properties.
     * @param tenantDomain      Tenant domain.
     * @return ActionDTO object with resolved properties.
     * @throws ActionDTOModelResolverException If an error occurs while resolving the properties.
     */
    ActionDTO resolveForUpdateOperation(ActionDTO updatingActionDTO, ActionDTO existingActionDTO, String tenantDomain)
            throws ActionDTOModelResolverException;

    /**
     * Delete the properties that need to be deleted in the Action Management Service.
     * This method is responsible for performing necessary CRUD operations for the properties that need to be deleted
     * using other external services.
     *
     * @param deletingActionDTO ActionDTO object with properties to be deleted.
     * @param tenantDomain      Tenant domain.
     * @throws ActionDTOModelResolverException If an error occurs while deleting the properties.
     */
    void resolveForDeleteOperation(ActionDTO deletingActionDTO, String tenantDomain)
            throws ActionDTOModelResolverException;
}
