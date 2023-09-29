/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.role.v2.mgt.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.role.v2.mgt.core.IdentityRoleManagementClientException;
import org.wso2.carbon.identity.role.v2.mgt.core.IdentityRoleManagementException;
import org.wso2.carbon.identity.role.v2.mgt.core.dao.GroupDAO;
import org.wso2.carbon.identity.role.v2.mgt.core.dao.RoleMgtDAOFactory;

import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.role.mgt.core.RoleConstants.Error.INVALID_REQUEST;

/**
 * GroupIDResolver Implementation of the {@link IDResolver} interface.
 */
public class GroupIDResolver implements IDResolver {

    private Log log = LogFactory.getLog(GroupIDResolver.class);

    @Override
    public String getNameByID(String id, String tenantDomain) throws IdentityRoleManagementException {

        GroupDAO groupDAO = RoleMgtDAOFactory.getInstance().getGroupDAO();
        String groupName = groupDAO.getGroupNameByID(id, tenantDomain);
        if (groupName == null) {
            String errorMessage = "A group doesn't exist with id: " + id + " in the tenantDomain: " + tenantDomain;
            throw new IdentityRoleManagementClientException(INVALID_REQUEST.getCode(), errorMessage);
        }
        return groupName;
    }

    /**
     * Retrieve the group names for the given ID list.
     *
     * @param idList       List of group IDs.
     * @param tenantDomain Tenant domain.
     * @return List of group names.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    public Map<String, String> getNamesByIDs(List<String> idList, String tenantDomain)
            throws IdentityRoleManagementException {

        GroupDAO groupDAO = RoleMgtDAOFactory.getInstance().getGroupDAO();
        return groupDAO.getGroupNamesByIDs(idList, tenantDomain);
    }

    @Override
    public String getIDByName(String name, String tenantDomain) throws IdentityRoleManagementException {

        GroupDAO groupDAO = RoleMgtDAOFactory.getInstance().getGroupDAO();
        String groupName = groupDAO.getGroupIDByName(name, tenantDomain);
        if (groupName == null) {
            String errorMessage = "A group doesn't exist with name: " + name + " in the tenantDomain: " + tenantDomain;
            throw new IdentityRoleManagementClientException(INVALID_REQUEST.getCode(), errorMessage);
        }
        return groupName;
    }

    /**
     * Retrieve the group IDs for the given names list.
     *
     * @param namesList    Group names list.
     * @param tenantDomain Tenant domain.
     * @return List of group IDs.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    public Map<String, String> getIDsByNames(List<String> namesList, String tenantDomain)
            throws IdentityRoleManagementException {

        GroupDAO groupDAO = RoleMgtDAOFactory.getInstance().getGroupDAO();
        return groupDAO.getGroupIDsByNames(namesList, tenantDomain);
    }
}
