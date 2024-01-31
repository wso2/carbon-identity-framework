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

import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementClientException;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementException;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementServerException;
import org.wso2.carbon.identity.role.v2.mgt.core.internal.RoleManagementServiceComponentHolder;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserStoreClientException;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.Error.INVALID_REQUEST;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.Error.UNEXPECTED_SERVER_ERROR;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.Error.UNSUPPORTED_USER_STORE_MANAGER;

/**
 * RoleDAO Implementation.
 */
public class GroupDAOImpl implements GroupDAO {

    @Override
    public String getGroupNameByID(String id, String tenantDomain) throws IdentityRoleManagementException {

        try {
            AbstractUserStoreManager userStoreManager = getUserStoreManager(tenantDomain);
            return userStoreManager.getGroupNameByGroupId(id);
        } catch (UserStoreException e) {
            String errorMessage = String.format(
                    "Error while resolving the group name for the given group ID: %s " + "and tenantDomain: %s", id,
                    tenantDomain);
            if (e instanceof UserStoreClientException) {
                throw new IdentityRoleManagementClientException(INVALID_REQUEST.getCode(), errorMessage, e);
            }
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
    }

    @Override
    public Map<String, String> getGroupNamesByIDs(List<String> ids, String tenantDomain)
            throws IdentityRoleManagementException {

        try {
            Map<String, String> groupIdsToNames = new HashMap<>();
            AbstractUserStoreManager userStoreManager = getUserStoreManager(tenantDomain);
            for (String id : ids) {
                groupIdsToNames.put(id, userStoreManager.getGroupNameByGroupId(id));
            }
            return groupIdsToNames;
        } catch (UserStoreException e) {
            String errorMessage =
                    "Error while resolving the group name for the given group Ids in the tenantDomain: " + tenantDomain;
            if (e instanceof UserStoreClientException) {
                throw new IdentityRoleManagementClientException(INVALID_REQUEST.getCode(), errorMessage, e);
            }
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
    }

    @Override
    public String getGroupIDByName(String name, String tenantDomain) throws IdentityRoleManagementException {

        String domainAwareName = UserCoreUtil.addDomainToName(name, tenantDomain);
        try {
            AbstractUserStoreManager userStoreManager = getUserStoreManager(tenantDomain);
            return userStoreManager.getGroupIdByGroupName(UserCoreUtil.addDomainToName(name, domainAwareName));
        } catch (UserStoreException e) {
            String errorMessage = String.format(
                    "Error while resolving the group id for the given group: %s " + "and tenantDomain: %s",
                    domainAwareName, tenantDomain);
            if (e instanceof UserStoreClientException) {
                throw new IdentityRoleManagementClientException(INVALID_REQUEST.getCode(), errorMessage, e);
            }
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
    }

    @Override
    public Map<String, String> getGroupIDsByNames(List<String> names, String tenantDomain)
            throws IdentityRoleManagementException {

        try {
            Map<String, String> groupNamesToIDs = new HashMap<>();
            AbstractUserStoreManager userStoreManager = getUserStoreManager(tenantDomain);
            for (String name : names) {
                String domainAwareName = UserCoreUtil.addDomainToName(name, tenantDomain);
                groupNamesToIDs.put(name,
                        userStoreManager.getGroupIdByGroupName(UserCoreUtil.addDomainToName(name, domainAwareName)));
            }
            return groupNamesToIDs;
        } catch (UserStoreException e) {
            String errorMessage =
                    "Error while resolving the group ID for the given group names in the tenantDomain: " + tenantDomain;
            if (e instanceof UserStoreClientException) {
                throw new IdentityRoleManagementClientException(INVALID_REQUEST.getCode(), errorMessage, e);
            }
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
    }

    /**
     * Get the AbstractUserStoreManager for the given tenant domain.
     *
     * @param tenantDomain Tenant domain.
     * @return UserStoreManager.
     * @throws IdentityRoleManagementServerException If an error occurred while getting the AbstractUserStoreManager
     *                                               instance of the tenant.
     */
    private AbstractUserStoreManager getUserStoreManager(String tenantDomain)
            throws IdentityRoleManagementServerException {

        RealmService realmService = RoleManagementServiceComponentHolder.getInstance().getRealmService();
        UserStoreManager userStoreManager;
        try {
            userStoreManager =
                    realmService.getTenantUserRealm(realmService.getTenantManager().getTenantId(tenantDomain))
                            .getUserStoreManager();
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                    "Error occurred while getting the userStoreManager for tenant: " + tenantDomain, e);
        }
        if (userStoreManager == null) {
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                    "Error occurred while getting the userStoreManager for tenant: " + tenantDomain);
        }
        if (!(userStoreManager instanceof AbstractUserStoreManager)) {
            throw new IdentityRoleManagementServerException(UNSUPPORTED_USER_STORE_MANAGER.getCode(),
                    "Underlying userStoreManager does not support getGroupNameByID operation " + tenantDomain);
        }
        return (AbstractUserStoreManager) userStoreManager;
    }
}
