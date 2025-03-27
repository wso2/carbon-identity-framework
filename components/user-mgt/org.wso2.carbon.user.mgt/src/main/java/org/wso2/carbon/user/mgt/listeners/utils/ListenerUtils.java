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
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

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
    public static final String ADD_GROUP_ACTION = "Add-Group";
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
    public static final String NEW_ROLE_NAME = "NewRoleName";

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

    /**
     * Returns initiator based on the masking config.
     *
     * @return Initiator. If log masking is enabled returns the userId, if userId can not be resolved then returns the
     * masked username.
     */
    public static String getInitiator() {

        String initiator = null;
        if (LoggerUtils.isLogMaskingEnable) {
            String username = MultitenantUtils.getTenantAwareUsername(ListenerUtils.getUser());
            String tenantDomain = MultitenantUtils.getTenantDomain(ListenerUtils.getUser());
            if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(tenantDomain)) {
                initiator = IdentityUtil.getInitiatorId(username, tenantDomain);
            }
            if (StringUtils.isBlank(initiator)) {
                initiator = LoggerUtils.getMaskedContent(ListenerUtils.getUser());
            }
        } else {
            initiator = ListenerUtils.getUser();
        }
        return initiator;
    }

    /**
     * Get the initiator for audit logs.
     *
     * @return Initiator id despite masking.
     */
    public static String getInitiatorId() {

        String initiator = null;
        String username = MultitenantUtils.getTenantAwareUsername(ListenerUtils.getUser());
        String tenantDomain = MultitenantUtils.getTenantDomain(ListenerUtils.getUser());
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(tenantDomain)) {
            initiator = IdentityUtil.getInitiatorId(username, tenantDomain);
        }
        if (StringUtils.isBlank(initiator)) {
            if (username.equals(CarbonConstants.REGISTRY_SYSTEM_USERNAME)) {
                // If the initiator is wso2.system, we need not to mask the username.
                return LoggerUtils.Initiator.System.name();
            }
            initiator = LoggerUtils.getMaskedContent(ListenerUtils.getUser());
        }
        return initiator;
    }

    /**
     * Get the initiator for audit logs.
     *
     * @return Initiator based on whether log masking is enabled or not.
     */
    public static String getInitiatorFromContext() {

        String user = CarbonContext.getThreadLocalCarbonContext().getUsername();
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        if (LoggerUtils.isLogMaskingEnable) {
            if (StringUtils.isNotBlank(user) && StringUtils.isNotBlank(tenantDomain)) {
                String initiator = IdentityUtil.getInitiatorId(user, tenantDomain);
                if (StringUtils.isNotBlank(initiator)) {
                    return initiator;
                }
            }
            if (StringUtils.isNotBlank(user)) {
                return LoggerUtils.getMaskedContent(user + "@" + tenantDomain);
            }
            return LoggerUtils.getMaskedContent(CarbonConstants.REGISTRY_SYSTEM_USERNAME);
        } else if (StringUtils.isNotBlank(user)) {
            return user + "@" + tenantDomain;
        }
        return CarbonConstants.REGISTRY_SYSTEM_USERNAME;
    }

    /**
     * Returns the target value based on the masking config.
     *
     * @param userName         Claims map.
     * @param userStoreManager JSON Object which will be added to audit log.
     * @return Target value. If log masking is enabled returns the masked value.
     */
    public static String getTargetForAuditLog(String userName, UserStoreManager userStoreManager) {

        String target = ListenerUtils.getEntityWithUserStoreDomain(userName, userStoreManager);
        if (LoggerUtils.isLogMaskingEnable) {
            return LoggerUtils.getMaskedContent(target);
        }
        return target;
    }
}
