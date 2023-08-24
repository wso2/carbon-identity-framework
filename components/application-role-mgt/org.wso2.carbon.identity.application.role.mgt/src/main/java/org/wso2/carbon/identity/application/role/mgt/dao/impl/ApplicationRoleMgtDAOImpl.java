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

package org.wso2.carbon.identity.application.role.mgt.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.database.utils.jdbc.exceptions.TransactionException;
import org.wso2.carbon.identity.application.common.model.IdPGroup;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.role.mgt.constants.SQLConstants;
import org.wso2.carbon.identity.application.role.mgt.dao.ApplicationRoleMgtDAO;
import org.wso2.carbon.identity.application.role.mgt.exceptions.ApplicationRoleManagementClientException;
import org.wso2.carbon.identity.application.role.mgt.exceptions.ApplicationRoleManagementException;
import org.wso2.carbon.identity.application.role.mgt.exceptions.ApplicationRoleManagementServerException;
import org.wso2.carbon.identity.application.role.mgt.model.ApplicationRole;
import org.wso2.carbon.identity.application.role.mgt.model.Group;
import org.wso2.carbon.identity.application.role.mgt.model.User;
import org.wso2.carbon.identity.application.role.mgt.util.GroupIDResolver;
import org.wso2.carbon.identity.application.role.mgt.util.UserIDResolver;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.application.role.mgt.constants.ApplicationRoleMgtConstants.ErrorMessages.ERROR_CODE_CHECKING_ROLE_EXISTENCE;
import static org.wso2.carbon.identity.application.role.mgt.constants.ApplicationRoleMgtConstants.ErrorMessages.ERROR_CODE_DELETE_ROLE;
import static org.wso2.carbon.identity.application.role.mgt.constants.ApplicationRoleMgtConstants.ErrorMessages.ERROR_CODE_GET_ROLES_BY_APPLICATION;
import static org.wso2.carbon.identity.application.role.mgt.constants.ApplicationRoleMgtConstants.ErrorMessages.ERROR_CODE_GET_ROLES_BY_GROUP_ID;
import static org.wso2.carbon.identity.application.role.mgt.constants.ApplicationRoleMgtConstants.ErrorMessages.ERROR_CODE_GET_ROLES_BY_USER_ID;
import static org.wso2.carbon.identity.application.role.mgt.constants.ApplicationRoleMgtConstants.ErrorMessages.ERROR_CODE_GET_ROLE_ASSIGNED_GROUPS;
import static org.wso2.carbon.identity.application.role.mgt.constants.ApplicationRoleMgtConstants.ErrorMessages.ERROR_CODE_GET_ROLE_ASSIGNED_USERS;
import static org.wso2.carbon.identity.application.role.mgt.constants.ApplicationRoleMgtConstants.ErrorMessages.ERROR_CODE_GET_ROLE_BY_ID;
import static org.wso2.carbon.identity.application.role.mgt.constants.ApplicationRoleMgtConstants.ErrorMessages.ERROR_CODE_INSERT_ROLE;
import static org.wso2.carbon.identity.application.role.mgt.constants.ApplicationRoleMgtConstants.ErrorMessages.ERROR_CODE_UPDATE_ROLE;
import static org.wso2.carbon.identity.application.role.mgt.constants.ApplicationRoleMgtConstants.ErrorMessages.ERROR_CODE_UPDATE_ROLE_ASSIGNED_GROUPS;
import static org.wso2.carbon.identity.application.role.mgt.constants.ApplicationRoleMgtConstants.ErrorMessages.ERROR_CODE_UPDATE_ROLE_ASSIGNED_USERS;
import static org.wso2.carbon.identity.application.role.mgt.constants.ApplicationRoleMgtConstants.LOCAL_IDP;
import static org.wso2.carbon.identity.application.role.mgt.constants.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_APP_ID;
import static org.wso2.carbon.identity.application.role.mgt.constants.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_GROUP_ID;
import static org.wso2.carbon.identity.application.role.mgt.constants.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_IDP_ID;
import static org.wso2.carbon.identity.application.role.mgt.constants.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_ROLE_ID;
import static org.wso2.carbon.identity.application.role.mgt.constants.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_ROLE_NAME;
import static org.wso2.carbon.identity.application.role.mgt.constants.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_TENANT_ID;
import static org.wso2.carbon.identity.application.role.mgt.constants.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_USER_ID;
import static org.wso2.carbon.identity.application.role.mgt.util.ApplicationRoleMgtUtils.getNewTemplate;
import static org.wso2.carbon.identity.application.role.mgt.util.ApplicationRoleMgtUtils.handleServerException;

/**
 * Application role DAO implementation.
 */
public class ApplicationRoleMgtDAOImpl implements ApplicationRoleMgtDAO {

    private static final Log LOG = LogFactory.getLog(ApplicationRoleMgtDAOImpl.class);
    private UserIDResolver userIDResolver = new UserIDResolver();
    private GroupIDResolver groupIDResolver = new GroupIDResolver();

    @Override
    public ApplicationRole addApplicationRole(ApplicationRole applicationRole, String tenantDomain)
            throws ApplicationRoleManagementServerException {

        int tenantID;
        if (tenantDomain != null) {
            tenantID = IdentityTenantUtil.getTenantId(tenantDomain);
        } else {
            tenantID = MultitenantConstants.INVALID_TENANT_ID;
        }

        NamedJdbcTemplate namedJdbcTemplate = getNewTemplate();
        try {
            namedJdbcTemplate.withTransaction(template -> {
                template.executeInsert(SQLConstants.ADD_APPLICATION_ROLE, namedPreparedStatement -> {
                    namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ROLE_ID, applicationRole.getRoleId());
                    namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_APP_ID, applicationRole.getApplicationId());
                    namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ROLE_NAME, applicationRole.getRoleName());
                    namedPreparedStatement.setInt(DB_SCHEMA_COLUMN_NAME_TENANT_ID, tenantID);
                }, null, false);
                return null;
            });
        } catch (TransactionException e) {
            throw handleServerException(ERROR_CODE_INSERT_ROLE, e, applicationRole.getRoleName(),
                    applicationRole.getApplicationId());
        }
        return applicationRole;
    }

    @Override
    public ApplicationRole getApplicationRoleById(String roleId, String tenantDomain)
            throws ApplicationRoleManagementServerException {

        int tenantID;
        if (tenantDomain != null) {
            tenantID = IdentityTenantUtil.getTenantId(tenantDomain);
        } else {
            tenantID = MultitenantConstants.INVALID_TENANT_ID;
        }

        NamedJdbcTemplate namedJdbcTemplate = getNewTemplate();
        try {
            return namedJdbcTemplate.fetchSingleRecord(SQLConstants.GET_APPLICATION_ROLE_BY_ID,
                    (resultSet, rowNumber) ->
                            new ApplicationRole(resultSet.getString(DB_SCHEMA_COLUMN_NAME_ROLE_ID),
                                    resultSet.getString(DB_SCHEMA_COLUMN_NAME_ROLE_NAME),
                                    resultSet.getString(DB_SCHEMA_COLUMN_NAME_APP_ID)),
                    namedPreparedStatement -> {
                        namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ROLE_ID, roleId);
                        namedPreparedStatement.setInt(DB_SCHEMA_COLUMN_NAME_TENANT_ID, tenantID);
                    });
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_GET_ROLE_BY_ID, e, roleId);
        }
    }

    @Override
    public List<ApplicationRole> getApplicationRoles(String applicationId)
            throws ApplicationRoleManagementServerException {

        NamedJdbcTemplate namedJdbcTemplate = getNewTemplate();
        try {
            return namedJdbcTemplate.executeQuery(SQLConstants.GET_APPLICATION_ROLES_OF_APPLICATION,
                    (resultSet, rowNumber) ->
                            new ApplicationRole(resultSet.getString(DB_SCHEMA_COLUMN_NAME_ROLE_ID),
                                    resultSet.getString(DB_SCHEMA_COLUMN_NAME_ROLE_NAME),
                                    resultSet.getString(DB_SCHEMA_COLUMN_NAME_APP_ID)),
                    namedPreparedStatement -> {
                        namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_APP_ID, applicationId);
                    });
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_GET_ROLES_BY_APPLICATION, e, applicationId);
        }
    }

    @Override
    public void updateApplicationRole(String applicationId, String roleId, String tenantDomain)
            throws ApplicationRoleManagementServerException {

        NamedJdbcTemplate namedJdbcTemplate = getNewTemplate();
        try {
            namedJdbcTemplate.withTransaction(template -> {
                template.executeUpdate(SQLConstants.UPDATE_APPLICATION_ROLE_BY_ID, namedPreparedStatement -> {
                    namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ROLE_ID, roleId);
                });
                return null;
            });
        } catch (TransactionException e) {
            throw handleServerException(ERROR_CODE_UPDATE_ROLE, e, roleId, applicationId);
        }
    }

    @Override
    public void deleteApplicationRole(String roleId, String tenantDomain)
            throws ApplicationRoleManagementServerException {

        NamedJdbcTemplate namedJdbcTemplate = getNewTemplate();
        try {
            namedJdbcTemplate.executeQuery(SQLConstants.DELETE_APPLICATION_ROLE_BY_ID, (resultSet, rowNumber) -> null,
                    namedPreparedStatement -> {
                        namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ROLE_ID, roleId);
                    });
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_DELETE_ROLE, e, roleId);
        }
    }

    @Override
    public boolean isExistingRole(String applicationId, String roleName, String tenantDomain)
            throws ApplicationRoleManagementServerException {

        int tenantID;
        if (tenantDomain != null) {
            tenantID = IdentityTenantUtil.getTenantId(tenantDomain);
        } else {
            tenantID = MultitenantConstants.INVALID_TENANT_ID;
        }

        NamedJdbcTemplate namedJdbcTemplate = getNewTemplate();
        try {
            return namedJdbcTemplate.fetchSingleRecord(SQLConstants.IS_APPLICATION_ROLE_EXISTS,
                    (resultSet, rowNumber) -> resultSet.getInt(1) > 0,
                    namedPreparedStatement -> {
                        namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ROLE_NAME, roleName);
                        namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_APP_ID, applicationId);
                        namedPreparedStatement.setInt(DB_SCHEMA_COLUMN_NAME_TENANT_ID, tenantID);
                    });
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_CHECKING_ROLE_EXISTENCE, e, roleName, applicationId);
        }
    }

    @Override
    public void updateApplicationRoleAssignedUsers(String roleId, List<String> addedUsers, List<String> removedUsers,
                                                   String tenantDomain)
            throws ApplicationRoleManagementException {

        int tenantID;
        if (tenantDomain != null) {
            tenantID = IdentityTenantUtil.getTenantId(tenantDomain);
        } else {
            tenantID = MultitenantConstants.INVALID_TENANT_ID;
        }
        validateUserIds(addedUsers, tenantDomain);

        NamedJdbcTemplate namedJdbcTemplate = getNewTemplate();
        try {
            String sqlStmt = SQLConstants.ADD_APPLICATION_ROLE_USER;
            namedJdbcTemplate.withTransaction(template -> {
                namedJdbcTemplate.executeBatchInsert(sqlStmt, (preparedStatement -> {
                    for (String userId : addedUsers) {
                        preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ROLE_ID, roleId);
                        preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_USER_ID, userId);
                        preparedStatement.setInt(DB_SCHEMA_COLUMN_NAME_TENANT_ID, tenantID);
                        preparedStatement.addBatch();
                    }
                }), roleId);
                for (String userId: removedUsers) {
                    namedJdbcTemplate.executeUpdate(SQLConstants.DELETE_ASSIGNED_USER_APPLICATION_ROLE,
                            namedPreparedStatement -> {
                                namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ROLE_ID, roleId);
                                namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_USER_ID, userId);
                            });
                }
                return null;
            });
        } catch (TransactionException e) {
            throw handleServerException(ERROR_CODE_UPDATE_ROLE_ASSIGNED_USERS, e, roleId);
        }
    }

    @Override
    public ApplicationRole getApplicationRoleAssignedUsers(String roleId, String tenantDomain)
            throws ApplicationRoleManagementException {

        NamedJdbcTemplate namedJdbcTemplate = getNewTemplate();
        try {
            List<User> users;
            users = namedJdbcTemplate.executeQuery(SQLConstants.GET_ASSIGNED_USERS_OF_APPLICATION_ROLE,
                    (resultSet, rowNumber) -> new User(resultSet.getString(DB_SCHEMA_COLUMN_NAME_USER_ID)),
                    namedPreparedStatement -> {
                        namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ROLE_ID, roleId);
                    });
            for (User user : users) {
                user.setUserName(getUserNamesByID(user.getId(), tenantDomain));
            }
            ApplicationRole applicationRole = new ApplicationRole(roleId);
            applicationRole.setAssignedUsers(users);
            return  applicationRole;
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_GET_ROLE_ASSIGNED_USERS, e, roleId);
        }
    }

    @Override
    public void updateApplicationRoleAssignedGroups(String roleId, IdentityProvider identityProvider,
                                                    List<String> addedGroups, List<String> removedGroups,
                                                    String tenantDomain)
            throws ApplicationRoleManagementException {

        int tenantID;
        if (tenantDomain != null) {
            tenantID = IdentityTenantUtil.getTenantId(tenantDomain);
        } else {
            tenantID = MultitenantConstants.INVALID_TENANT_ID;
        }
        validateGroupIds(identityProvider, addedGroups, tenantDomain);
        validateGroupIds(identityProvider, removedGroups, tenantDomain);
        NamedJdbcTemplate namedJdbcTemplate = getNewTemplate();
        try {
            namedJdbcTemplate.withTransaction(template -> {
                if (addedGroups.size() > 0) {
                    namedJdbcTemplate.executeBatchInsert(SQLConstants.ADD_APPLICATION_ROLE_GROUP,
                            (preparedStatement -> {
                                for (String groupId : addedGroups) {
                                    preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ROLE_ID, roleId);
                                    preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_GROUP_ID, groupId);
                                    preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_IDP_ID,
                                            identityProvider.getResourceId());
                                    preparedStatement.setInt(DB_SCHEMA_COLUMN_NAME_TENANT_ID, tenantID);
                                    preparedStatement.addBatch();
                        }
                    }), roleId);
                }
                if (removedGroups.size() > 0) {
                    for (String groupId: removedGroups) {
                        namedJdbcTemplate.executeUpdate(SQLConstants.DELETE_ASSIGNED_GROUP_APPLICATION_ROLE,
                                namedPreparedStatement -> {
                                    namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ROLE_ID, roleId);
                                    namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_GROUP_ID, groupId);
                                });
                    }
                }
                return null;
            });
        } catch (TransactionException e) {
            throw handleServerException(ERROR_CODE_UPDATE_ROLE_ASSIGNED_GROUPS, e, roleId);
        }
    }

    @Override
    public ApplicationRole getApplicationRoleAssignedGroups(String roleId, IdentityProvider identityProvider,
                                                            String tenantDomain) throws
            ApplicationRoleManagementException {

        NamedJdbcTemplate namedJdbcTemplate = getNewTemplate();
        try {
            List<Group> groups;
            groups = namedJdbcTemplate.executeQuery(SQLConstants.GET_ASSIGNED_GROUPS_OF_APPLICATION_ROLE,
                    (resultSet, rowNumber) -> new Group(resultSet.getString(DB_SCHEMA_COLUMN_NAME_GROUP_ID),
                            resultSet.getString(DB_SCHEMA_COLUMN_NAME_IDP_ID)),
                    namedPreparedStatement -> {
                        namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ROLE_ID, roleId);
                        namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_IDP_ID,
                                identityProvider.getResourceId());
                    });
            if (LOCAL_IDP.equals(identityProvider.getIdentityProviderName())) {
                for (Group group : groups) {
                    group.setGroupName(getGroupNamesByID(group.getGroupId(), tenantDomain));
                    group.setIdpName(identityProvider.getIdentityProviderName());
                }
            } else {
                IdPGroup[] idpGroups = identityProvider.getIdPGroupConfig();
                Map<String, String> idToNameMap = new HashMap<>();
                for (IdPGroup idpGroup : idpGroups) {
                    idToNameMap.put(idpGroup.getIdpGroupId(), idpGroup.getIdpGroupName());
                }
                for (Group group : groups) {
                    if (idToNameMap.containsKey(group.getGroupId())) {
                        group.setGroupName(idToNameMap.get(group.getGroupId()));
                        group.setIdpName(identityProvider.getIdentityProviderName());
                    }
                }
            }
            ApplicationRole applicationRole = new ApplicationRole(roleId);
            applicationRole.setAssignedGroups(groups);
            return  applicationRole;
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_GET_ROLE_ASSIGNED_GROUPS, e, roleId);
        }
    }

    @Override
    public List<ApplicationRole> getApplicationRolesByUserId(String userId) throws ApplicationRoleManagementException {

        NamedJdbcTemplate namedJdbcTemplate = getNewTemplate();
        try {
            return namedJdbcTemplate.executeQuery(SQLConstants.GET_APPLICATION_ROLES_BY_USER_ID,
                    (resultSet, rowNumber) ->
                            new ApplicationRole(resultSet.getString(DB_SCHEMA_COLUMN_NAME_ROLE_ID),
                                    resultSet.getString(DB_SCHEMA_COLUMN_NAME_ROLE_NAME),
                                    resultSet.getString(DB_SCHEMA_COLUMN_NAME_APP_ID)),
                    namedPreparedStatement -> {
                        namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_USER_ID, userId);
                    });
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_GET_ROLES_BY_USER_ID, e, userId);
        }
    }

    @Override
    public List<ApplicationRole> getApplicationRolesByGroupId(String groupId)
            throws ApplicationRoleManagementException {

            NamedJdbcTemplate namedJdbcTemplate = getNewTemplate();
            try {
                return namedJdbcTemplate.executeQuery(SQLConstants.GET_APPLICATION_ROLES_BY_GROUP_ID,
                        (resultSet, rowNumber) ->
                                new ApplicationRole(resultSet.getString(DB_SCHEMA_COLUMN_NAME_ROLE_ID),
                                        resultSet.getString(DB_SCHEMA_COLUMN_NAME_ROLE_NAME),
                                        resultSet.getString(DB_SCHEMA_COLUMN_NAME_APP_ID)),
                        namedPreparedStatement -> {
                            namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_GROUP_ID, groupId);
                        });
            } catch (DataAccessException e) {
                throw handleServerException(ERROR_CODE_GET_ROLES_BY_GROUP_ID, e, groupId);
            }
    }

    public void validateGroupIds(IdentityProvider identityProvider, List<String> groups, String tenantDomain)
            throws ApplicationRoleManagementException {

        if (LOCAL_IDP.equals(identityProvider.getIdentityProviderName())) {
            for (String groupId : groups) {
                boolean isExists = groupIDResolver.isExists(groupId, tenantDomain);
                if (!isExists) {
                    throw new ApplicationRoleManagementClientException("Given groupId is not found",
                                    "Given groupId is not found", "");
                }
            }
        } else {
            IdPGroup[] idpGroups = identityProvider.getIdPGroupConfig();
            Map<String, String> idToNameMap = new HashMap<>();
            for (IdPGroup idpGroup : idpGroups) {
                idToNameMap.put(idpGroup.getIdpGroupId(), idpGroup.getIdpGroupName());
            }
            for (String groupId : groups) {
                if (!idToNameMap.containsKey(groupId)) {
                    throw new ApplicationRoleManagementClientException("Given groupId is not found",
                            "Given groupId is not found", "");
                }
            }
        }
    }

    public void validateUserIds(List<String> users, String tenantDomain)
            throws ApplicationRoleManagementException {

        for (String userId : users) {
            boolean isExists = userIDResolver.isExists(userId, tenantDomain);
            if (!isExists) {
                throw new ApplicationRoleManagementClientException("Given user Id is not found",
                        "Given user Id is not found", "");
            }
        }
    }

    private String getUserNamesByID(String userID, String tenantDomain)
            throws ApplicationRoleManagementException {

        return userIDResolver.getNameByID(userID, tenantDomain);
    }

    private String getGroupNamesByID(String groupID, String tenantDomain)
            throws ApplicationRoleManagementException {

        return groupIDResolver.getNameByID(groupID, tenantDomain);
    }

}
