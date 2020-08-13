/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.user.mgt.listeners.utils;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.util.UserCoreUtil;

/**
 * Utility class that handles the relevant utility tasks of listeners.
 */
public class ListenerUtils {

    /**
     * User Management Actions
     */
    public static final String ADD_USER_ACTION = "Add-User";
    public static final String DELETE_USER_ACTION = "Delete-User";
    public static final String SET_USER_CLAIM_VALUE_ACTION = "Set-User-Claim-Value";
    public static final String SET_USER_CLAIM_VALUES_ACTION = "Set-User-Claim-Values";
    public static final String DELETE_USER_CLAIM_VALUES_ACTION = "Delete-User-Claim-Values";
    public static final String DELETE_USER_CLAIM_VALUE_ACTION = "Delete-User-Claim-Value";
    public static final String CHANGE_PASSWORD_BY_USER_ACTION = "Change-Password-by-User";
    public static final String CHANGE_PASSWORD_BY_ADMIN_ACTION = "Change-Password-by-Administrator";
    public static final String DELETE_ROLE_ACTION = "Delete-Role";
    public static final String ADD_ROLE_ACTION = "Add-Role";
    public static final String UPDATE_ROLE_NAME_ACTION = "Update-Role-Name";
    public static final String UPDATE_USERS_OF_ROLE_ACTION = "Update-Users-of-Role";
    public static final String UPDATE_ROLES_OF_USER_ACTION = "Update-Roles-of-User";
    public static final String GET_USER_CLAIM_VALUE_ACTION = "Get-User-Claim-Value";
    public static final String GET_USER_CLAIM_VALUES_ACTION = "Get-User-Claim-Values";
    public static final String GET_USER_LIST_ACTION = "Get-User-List";
    public static final String GET_ROLES_OF_USER_ACTION = "Get-Roles-of-User";
    public static final String GET_USERS_OF_ROLE_ACTION = "Get-Users-of-Role";
    public static final String AUTHENTICATION_ACTION = "Authentication";
    public static final String UPDATE_PERMISSIONS_OF_ROLE_ACTION = "Update-Permissions-of-Role";

    /**
     * Audit Log listener data fields.
     */
    public static final String ROLES_FIELD = "Roles";
    public static final String CLAIMS_FIELD = "Claims";
    public static final String CLAIM_URI_FIELD = "Claim";
    public static final String CLAIM_VALUE_FIELD = "Claim Value";
    public static final String USERS_FIELD = "Users";
    public static final String PERMISSIONS_FIELD = "Permissions";
    public static final String DELETED_USERS = "Deleted Users";
    public static final String NEW_USERS = "New Users";
    public static final String DELETED_ROLES = "Deleted Roles";
    public static final String NEW_ROLES = "New Roles";
    public static final String PROFILE_FIELD = "Profile";
    public static final String FILTER_FIELD = "Filter";

    /**
     * Audit log fields.
     */
    public static final String INITIATOR = "Initiator";
    public static final String ACTION = "Action";
    public static final String TARGET = "Target";
    public static final String DATA = "Data";
    public static final String OUTCOME = "Outcome";
    public static final String ERROR = "Error";

    /**
     * To get the current user, who is doing the current task.
     *
     * @return current logged-in user
     */
    public static String getUser() {

        String user = CarbonContext.getThreadLocalCarbonContext().getUsername();
        if (StringUtils.isNotEmpty(user)) {
            user = UserCoreUtil
                    .addTenantDomainToEntry(user, CarbonContext.getThreadLocalCarbonContext().getTenantDomain());
        } else {
            user = CarbonConstants.REGISTRY_SYSTEM_USERNAME;
        }
        return user;
    }

    /**
     * This method will append the user store domain with user/role name.
     *
     * @param entity           Entity that need to modified.
     * @param userStoreManager UserStore Manager particular user/role handled by.
     * @return UserStoreDomain/UserName or UserStoreDomain/RoleName
     */
    public static String getEntityWithUserStoreDomain(String entity, UserStoreManager userStoreManager) {

        String entityWithUserStoreDomain = entity;
        if (StringUtils.isNotEmpty(entity) && userStoreManager != null) {
            String userStoreDomain = UserCoreUtil.getDomainName(userStoreManager.getRealmConfiguration());
            entityWithUserStoreDomain = UserCoreUtil.addDomainToName(entity, userStoreDomain);
        }
        return entityWithUserStoreDomain;
    }
}
