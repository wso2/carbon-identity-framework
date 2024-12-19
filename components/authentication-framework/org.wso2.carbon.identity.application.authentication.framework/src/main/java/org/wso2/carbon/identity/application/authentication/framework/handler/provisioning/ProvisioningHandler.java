/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.application.authentication.framework.handler.provisioning;

import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;

import java.util.List;
import java.util.Map;

/**
 * Provisioning handler interface.
 */
public interface ProvisioningHandler {

    /**
     * Handle provisioning with v1 roles.
     *
     * @param roles                   List of role names.
     * @param subject                 Subject identifier.
     * @param attributes              Attributes.
     * @param provisioningUserStoreId Provisioning user store Id.
     * @param tenantDomain            Tenant domain.
     * @throws FrameworkException If an error occurred while handling provisioning.
     * @deprecated This method is deprecated and use {@link #handleWithV2Roles(List, String, Map, String, String)}.
     */
    @Deprecated
    public void handle(List<String> roles, String subject, Map<String, String> attributes,
                       String provisioningUserStoreId, String tenantDomain) throws FrameworkException;

    /**
     * Default implementation to handle provisioning with v1 roles by validating idp role mappings by keeping backward
     * compatibility.
     *
     * @param roles                   List of role names.
     * @param subject                 Subject identifier.
     * @param attributes              Attributes.
     * @param provisioningUserStoreId Provisioning user store Id.
     * @param tenantDomain            Tenant domain.
     * @param idpToLocalRoleMapping   IdP to local role mapping.
     * @throws FrameworkException If an error occurred while handling provisioning.
     * @deprecated This method is deprecated and use {@link #handleWithV2Roles(List, String, Map, String, String)}.
     */
    @Deprecated
    default void handle(List<String> roles, String subject, Map<String, String> attributes,
            String provisioningUserStoreId, String tenantDomain, List<String> idpToLocalRoleMapping)
            throws FrameworkException {
        throw new FrameworkException("Operation is not supported.");
    }

    /**
     * Handle provisioning with v2 roles.
     *
     * @param roleIdList              List of role ids.
     * @param subject                 Subject identifier.
     * @param attributes              Attributes.
     * @param provisioningUserStoreId Provisioning user store Id.
     * @param tenantDomain            Tenant domain.
     * @throws FrameworkException If an error occurred while handling provisioning with v2 roles.
     */
    default void handleWithV2Roles(List<String> roleIdList, String subject, Map<String, String> attributes,
                                   String provisioningUserStoreId, String tenantDomain) throws FrameworkException {

        throw new FrameworkException("Operation is not supported.");
    }
}
