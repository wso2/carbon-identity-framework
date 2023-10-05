/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.role.v2.mgt.core.dao;

import org.wso2.carbon.identity.role.v2.mgt.core.IdentityRoleManagementException;

import java.util.List;
import java.util.Map;

/**
 * GroupDAO interface.
 */
public interface GroupDAO {

    /**
     * Retrieve the group name for the given ID.
     *
     * @param id           Group ID.
     * @param tenantDomain Tenant domain.
     * @return group name.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    String getGroupNameByID(String id, String tenantDomain) throws IdentityRoleManagementException;

    /**
     * Retrieve the group names for the given ID list.
     *
     * @param ids          Group ID list.
     * @param tenantDomain Tenant domain.
     * @return List of group names.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    Map<String, String> getGroupNamesByIDs(List<String> ids, String tenantDomain)
            throws IdentityRoleManagementException;

    /**
     * Retrieve the group ID for the given name.
     *
     * @param name         Group name.
     * @param tenantDomain Tenant domain.
     * @return group ID.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    String getGroupIDByName(String name, String tenantDomain) throws IdentityRoleManagementException;

    /**
     * Retrieve the group IDs for the given names list.
     *
     * @param names        Group names list.
     * @param tenantDomain Tenant domain.
     * @return List of group IDs.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    Map<String, String> getGroupIDsByNames(List<String> names, String tenantDomain)
            throws IdentityRoleManagementException;

}
