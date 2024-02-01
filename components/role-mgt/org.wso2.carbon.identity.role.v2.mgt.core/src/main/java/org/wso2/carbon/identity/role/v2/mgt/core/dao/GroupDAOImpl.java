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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementClientException;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementException;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementServerException;
import org.wso2.carbon.identity.role.v2.mgt.core.internal.RoleManagementServiceComponentHolder;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserStoreClientException;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.Error.INVALID_REQUEST;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.Error.UNEXPECTED_SERVER_ERROR;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.Error.UNSUPPORTED_USER_STORE_MANAGER;
import static org.wso2.carbon.user.core.constants.UserCoreErrorConstants.ErrorMessages.ERROR_NO_GROUP_FOUND_WITH_ID;
import static org.wso2.carbon.user.core.constants.UserCoreErrorConstants.ErrorMessages.ERROR_NO_GROUP_FOUND_WITH_NAME;

/**
 * RoleDAO Implementation.
 */
public class GroupDAOImpl implements GroupDAO {

    private static final Log LOG = LogFactory.getLog(GroupDAOImpl.class);

    @Override
    public String getGroupNameByID(String id, String tenantDomain) throws IdentityRoleManagementException {

        try {
            AbstractUserStoreManager userStoreManager = getUserStoreManager(tenantDomain);
            return userStoreManager.getGroupNameByGroupId(id);
        } catch (UserStoreException e) {
            String errorMessage = String.format("Error while resolving the group name for the given " +
                            "group ID: %s in tenantDomain: %s", id, tenantDomain);
            if (e instanceof UserStoreClientException) {
                // This is to ensure backward compatibility with the previous implementation.
                if (StringUtils.isNotBlank(e.getErrorCode()) && ERROR_NO_GROUP_FOUND_WITH_ID.getCode()
                        .equals(e.getErrorCode())) {
                    LOG.debug(String.format("No group found for the given group ID: %s in the tenantDomain: %s. " +
                                    "Therefore, returning an empty String.", id, tenantDomain));
                    return null;
                }
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
                try {
                    groupIdsToNames.put(id, userStoreManager.getGroupNameByGroupId(id));
                } catch (UserStoreClientException e) {
                    // This is to ensure backward compatibility with the previous implementation.
                    if (StringUtils.isNotBlank(e.getErrorCode()) && ERROR_NO_GROUP_FOUND_WITH_ID.getCode()
                            .equals(e.getErrorCode())) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(String.format("No group found for the given group ID: %s in the " +
                                    "tenantDomain: %s. Therefore, skipping group id lookup.", id, tenantDomain));
                        }
                    } else {
                        throw e;
                    }
                }
            }
            return groupIdsToNames;
        } catch (UserStoreException e) {
            String errorMessage = "Error while resolving the group name for the given group Ids in " +
                    "the tenantDomain: " + tenantDomain;
            if (e instanceof UserStoreClientException) {
                throw new IdentityRoleManagementClientException(INVALID_REQUEST.getCode(), errorMessage, e);
            }
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
    }

    @Override
    public String getGroupIDByName(String name, String tenantDomain) throws IdentityRoleManagementException {

        try {
            AbstractUserStoreManager userStoreManager = getUserStoreManager(tenantDomain);
            return userStoreManager.getGroupIdByGroupName(name);
        } catch (UserStoreException e) {
            String errorMessage = String.format("Error while resolving the group id for the given group: %s in " +
                            "tenantDomain: %s", name, tenantDomain);
            if (e instanceof UserStoreClientException) {
                // This is to ensure backward compatibility with the previous implementation.
                if (StringUtils.isNotBlank(e.getErrorCode()) && ERROR_NO_GROUP_FOUND_WITH_NAME.getCode()
                        .equals(e.getErrorCode())) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(String.format("No group found for the given group: %s in the tenantDomain: %s. " +
                                "Therefore, returning an Empty String.", name, tenantDomain));
                    }
                    return null;
                }
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
                try {
                    groupNamesToIDs.put(name, userStoreManager.getGroupIdByGroupName(name));
                } catch (UserStoreClientException e) {
                    // This is to ensure backward compatibility with the previous implementation.
                    if (StringUtils.isNotBlank(e.getErrorCode()) && ERROR_NO_GROUP_FOUND_WITH_NAME.getCode()
                            .equals(e.getErrorCode())) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(String.format("No group id found for the given group: %s in the " +
                                    "tenantDomain: %s. Therefore, skipping group name lookup.", name, tenantDomain));
                        }
                    } else {
                        throw e;
                    }
                }
            }
            return groupNamesToIDs;
        } catch (UserStoreException e) {
            String errorMessage = "Error while resolving the group ID for the given group names " +
                    "in the tenantDomain: " + tenantDomain;
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
