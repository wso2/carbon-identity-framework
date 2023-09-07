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
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.identity.application.common.model.IdPGroup;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.role.mgt.constants.ApplicationRoleMgtConstants;
import org.wso2.carbon.identity.application.role.mgt.exceptions.ApplicationRoleManagementClientException;
import org.wso2.carbon.identity.application.role.mgt.exceptions.ApplicationRoleManagementException;
import org.wso2.carbon.identity.application.role.mgt.exceptions.ApplicationRoleManagementServerException;
import org.wso2.carbon.identity.application.role.mgt.internal.ApplicationRoleMgtServiceComponentHolder;
import org.wso2.carbon.identity.application.role.mgt.model.Group;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.application.role.mgt.constants.ApplicationRoleMgtConstants.ErrorMessages.ERROR_CODE_GROUP_NOT_FOUND;
import static org.wso2.carbon.identity.application.role.mgt.constants.ApplicationRoleMgtConstants.ErrorMessages.ERROR_CODE_IDP_NOT_FOUND;
import static org.wso2.carbon.identity.application.role.mgt.constants.ApplicationRoleMgtConstants.LOCAL_IDP;

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
        if (StringUtils.isBlank(groupName)) {
            String errorMessage = "A group doesn't exist with id: " + id + " in the tenantDomain: " + tenantDomain;
            throw new ApplicationRoleManagementServerException(errorMessage);
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
        if (StringUtils.isBlank(userName)) {
            String errorMessage = "A user doesn't exist with id: " + id + " in the tenantDomain: " + tenantDomain;
            throw new ApplicationRoleManagementServerException(errorMessage);
        }
        return userName;
    }

    /**
     * Get username by id.
     *
     * @param tenantId Tenant ID.
     * @throws UserStoreException Error occurred while getting user store manager.
     */
    private static AbstractUserStoreManager getUserStoreManager(int tenantId) throws UserStoreException {

        RealmService realmService = ApplicationRoleMgtServiceComponentHolder.getInstance().getRealmService();
        UserRealm tenantUserRealm = realmService.getTenantUserRealm(tenantId);

        return (AbstractUserStoreManager) tenantUserRealm.getUserStoreManager();
    }

    /**
     * Remove common values in given two lists.
     *
     * @param list1 List 1.
     * @param list2 List 2.
     */
    public static void removeCommonValues(List<String> list1, List<String> list2) {
        HashSet<String> set = new HashSet<>(list1);

        Iterator<String> iterator = list2.iterator();
        while (iterator.hasNext()) {
            String value = iterator.next();
            if (set.contains(value)) {
                iterator.remove();
                list1.remove(value);
            }
        }
    }

    /**
     * Remove common values in given two lists.
     *
     * @param list1 List 1.
     * @param list2 List 2.
     */
    public static void removeCommonGroupValues(List<Group> list1, List<String> list2) {
        HashSet<String> set = new HashSet<>(list2);

        Iterator<Group> iterator = list1.iterator();
        while (iterator.hasNext()) {
            Group value = iterator.next();
            if (set.contains(value.getGroupId())) {
                iterator.remove();
                list2.remove(value.getGroupId());
            }
        }
    }

    /**
     * Validate groups.
     *
     * @param groups Groups.
     * @throws ApplicationRoleManagementException Error occurred while validating groups.
     */
    public static void validateGroupIds(List<Group> groups)
            throws ApplicationRoleManagementException {

        for (Group group : groups) {

            IdentityProvider identityProvider;
            if (LOCAL_IDP.equalsIgnoreCase(group.getIdpId()) || group.getIdpId() == null) {

                identityProvider = getResidentIdp();
                boolean isExists = ApplicationRoleMgtUtils.isGroupExists(group.getGroupId());
                if (!isExists) {
                    throw ApplicationRoleMgtUtils.handleClientException(ERROR_CODE_GROUP_NOT_FOUND,
                            group.getGroupId());
                }
            } else {
                identityProvider = getIdpById(group.getIdpId());
                IdPGroup[] idpGroups = identityProvider.getIdPGroupConfig();
                Map<String, String> idToNameMap = new HashMap<>();
                for (IdPGroup idpGroup : idpGroups) {
                    idToNameMap.put(idpGroup.getIdpGroupId(), idpGroup.getIdpGroupName());
                }
                if (!idToNameMap.containsKey(group.getGroupId())) {
                    throw ApplicationRoleMgtUtils.handleClientException(ERROR_CODE_GROUP_NOT_FOUND,
                            group.getGroupId());
                }
            }
            if (identityProvider == null) {
                throw handleClientException(ERROR_CODE_IDP_NOT_FOUND, group.getGroupId());
            }
            group.setIdpId(identityProvider.getResourceId());
        }
    }

    /**
     * Get resident idp.
     *
     * @throws ApplicationRoleManagementException Error occurred while validating groups.
     */
    public static IdentityProvider getResidentIdp() throws ApplicationRoleManagementException {

        IdentityProvider identityProvider;
        try {
            identityProvider = ApplicationRoleMgtServiceComponentHolder.getInstance()
                    .getIdentityProviderManager().getResidentIdP(getTenantDomain());
        } catch (IdentityProviderManagementException e) {
            throw new ApplicationRoleManagementException("Error while retrieving idp", "Error while retrieving " +
                    "resident idp.", e);
        }
        return identityProvider;
    }

    /**
     * Get idp by id.
     *
     * @throws ApplicationRoleManagementException Error occurred while validating groups.
     */
    public static IdentityProvider getIdpById(String idpId) throws ApplicationRoleManagementException {

        IdentityProvider identityProvider;
        try {
            identityProvider = ApplicationRoleMgtServiceComponentHolder.getInstance()
                    .getIdentityProviderManager().getIdPByResourceId(idpId, getTenantDomain(), true);
        } catch (IdentityProviderManagementException e) {
            throw new ApplicationRoleManagementException("Error while retrieving idp", "Error while retrieving idp "
                    + "for idpId: " + idpId, e);
        }
        return identityProvider;
    }

    public static String getTenantDomain() {

        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
    }
}
