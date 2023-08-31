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

package org.wso2.carbon.identity.application.role.mgt.util;

import org.apache.commons.lang.ArrayUtils;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.identity.application.role.mgt.constants.ApplicationRoleMgtConstants;
import org.wso2.carbon.identity.application.role.mgt.exceptions.ApplicationRoleManagementClientException;
import org.wso2.carbon.identity.application.role.mgt.exceptions.ApplicationRoleManagementException;
import org.wso2.carbon.identity.application.role.mgt.exceptions.ApplicationRoleManagementServerException;
import org.wso2.carbon.identity.application.role.mgt.internal.ApplicationRoleMgtServiceComponentHolder;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * Application role management util.
 */
public class ApplicationRoleMgtUtils {

    /**
     * Get a new Jdbc Template.
     *
     * @return a new Jdbc Template.
     */
    public static NamedJdbcTemplate getNewTemplate() {

        return new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
    }

    /**
     * Handle server exceptions.
     *
     * @param error Error message.
     * @param e     Throwable.
     * @param data  Data to be replaced in the error description.
     * @return ApplicationRoleManagementServerException.
     */
    public static ApplicationRoleManagementServerException handleServerException(
            ApplicationRoleMgtConstants.ErrorMessages error, Throwable e, String... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }
        return new ApplicationRoleManagementServerException(error.getMessage(), description, error.getCode(), e);
    }

    /**
     * Handle client exceptions.
     *
     * @param error Error message.
     * @param data  Data to be replaced in the error description.
     * @return ApplicationRoleManagementClientException.
     */
    public static ApplicationRoleManagementClientException handleClientException(
            ApplicationRoleMgtConstants.ErrorMessages error, String... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }
        return new ApplicationRoleManagementClientException(error.getMessage(), description, error.getCode());
    }

    /**
     * Check group exists by id.
     *
     * @param id Group ID.
     * @throws ApplicationRoleManagementException Error occurred while checking group exists.
     */
    public static boolean isGroupExists(String id) throws ApplicationRoleManagementException {

        AbstractUserStoreManager userStoreManager;
        try {
            userStoreManager = getUserStoreManager(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
            return userStoreManager.isGroupExist(id);
        } catch (UserStoreException e) {
            throw new ApplicationRoleManagementServerException("Error occurred while retrieving the userstore manager "
                    + "to resolve group name for the groupID", "Error occurred while retrieving the userstore manager "
                    + "to resolve group name for the groupID: " + id, e);
        }
    }

    /**
     * Get group name by id.
     *
     * @param id Group ID.
     * @param tenantDomain Tenant domain.
     * @throws ApplicationRoleManagementException Error occurred while getting group name by id.
     */
    public static String getGroupNameByID(String id, String tenantDomain) throws ApplicationRoleManagementException {

        String groupName;
        AbstractUserStoreManager userStoreManager;
        try {
            userStoreManager = getUserStoreManager(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
            groupName =  userStoreManager.getGroupNameByGroupId(id);
        } catch (UserStoreException e) {
            throw new ApplicationRoleManagementServerException("Error occurred while retrieving the userstore manager "
                    + "to resolve group name for the groupID", "Error occurred while retrieving the userstore manager "
                    + "to resolve group name for the groupID: " + id, e);
        }
        if (groupName == null) {
            String errorMessage = "A group doesn't exist with id: " + id + " in the tenantDomain: " + tenantDomain;
            throw new ApplicationRoleManagementClientException(errorMessage, errorMessage, "");
        }
        return groupName;
    }

    /**
     * Check user exists by id.
     *
     * @param id User ID.
     * @throws ApplicationRoleManagementException Error occurred while checking user exists.
     */
    public static boolean isUserExists(String id) throws ApplicationRoleManagementException {

        AbstractUserStoreManager userStoreManager;
        try {
            userStoreManager = getUserStoreManager(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
            return userStoreManager.isExistingUserWithID(id);
        } catch (UserStoreException e) {
            throw new ApplicationRoleManagementServerException("Error occurred while retrieving the userstore manager "
                    + "to resolve group name for the groupID", "Error occurred while retrieving the userstore manager "
                    + "to resolve group name for the groupID: " + id, e);
        }
    }

    /**
     * Get username by id.
     *
     * @param id User ID.
     * @param tenantDomain Tenant domain.
     * @throws ApplicationRoleManagementException Error occurred while getting username by id.
     */
    public static String getUserNameByID(String id, String tenantDomain) throws ApplicationRoleManagementException {

        String userName;
        AbstractUserStoreManager userStoreManager;
        try {
            userStoreManager = getUserStoreManager(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
            userName =  userStoreManager.getUserNameFromUserID(id);
        } catch (UserStoreException e) {
            throw new ApplicationRoleManagementServerException("Error occurred while retrieving the userstore manager "
                    + "to resolve username for the groupID", "Error occurred while retrieving the userstore manager "
                    + "to resolve username for the groupID: " + id, e);
        }
        if (userName == null) {
            String errorMessage = "A user doesn't exist with id: " + id + " in the tenantDomain: " + tenantDomain;
            throw new ApplicationRoleManagementClientException(errorMessage, errorMessage, "");
        }
        return userName;
    }

    private static AbstractUserStoreManager getUserStoreManager(int tenantId) throws UserStoreException {

        RealmService realmService = ApplicationRoleMgtServiceComponentHolder.getInstance().getRealmService();
        UserRealm tenantUserRealm = realmService.getTenantUserRealm(tenantId);

        return (AbstractUserStoreManager) tenantUserRealm.getUserStoreManager();
    }
}
