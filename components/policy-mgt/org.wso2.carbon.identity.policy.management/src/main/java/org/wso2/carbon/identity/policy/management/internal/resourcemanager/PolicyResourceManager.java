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

package org.wso2.carbon.identity.policy.management.internal.resourcemanager;

import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementException;
import org.wso2.carbon.identity.policy.management.api.model.PolicyResource;
import org.wso2.carbon.identity.policy.management.api.model.ResourceType;

/**
 * Internal contract for managing a specific {@link ResourceType} of policy resource in its backing
 * service. Implementations are registered in the component holder and dispatched to by {@link ResourceType}.
 */
public interface PolicyResourceManager {

    /**
     * Returns the resource type this manager handles.
     *
     * @return Supported resource type.
     */
    ResourceType getSupportedResourceType();

    /**
     * Creates the resource's backing entity in its owning service.
     *
     * @param resource     Resource to create.
     * @param tenantDomain Tenant domain.
     * @return A new resource referencing the created backing entity's ID.
     * @throws PolicyManagementException If creation fails.
     */
    PolicyResource create(PolicyResource resource, String tenantDomain) throws PolicyManagementException;

    /**
     * Hydrates the resource with its backing entity's full payload.
     *
     * @param resource     Resource to hydrate.
     * @param tenantDomain Tenant domain.
     * @return A new, hydrated resource.
     * @throws PolicyManagementException If hydration fails.
     */
    PolicyResource hydrate(PolicyResource resource, String tenantDomain) throws PolicyManagementException;

    /**
     * Deletes the resource's backing entity from its owning service. Best-effort: used both for
     * routine deletion and for saga compensation, so implementations must not throw and should
     * instead log any failure.
     *
     * @param resource     Resource to delete.
     * @param tenantDomain Tenant domain.
     */
    void delete(PolicyResource resource, String tenantDomain);
}
