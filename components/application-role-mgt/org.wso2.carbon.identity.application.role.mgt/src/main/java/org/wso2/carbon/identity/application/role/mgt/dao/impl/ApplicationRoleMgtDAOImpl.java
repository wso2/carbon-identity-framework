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

import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.database.utils.jdbc.exceptions.TransactionException;
import org.wso2.carbon.identity.application.role.mgt.constants.SQLConstants;
import org.wso2.carbon.identity.application.role.mgt.dao.ApplicationRoleMgtDAO;
import org.wso2.carbon.identity.application.role.mgt.exceptions.ApplicationRoleManagementException;
import org.wso2.carbon.identity.application.role.mgt.exceptions.ApplicationRoleManagementServerException;
import org.wso2.carbon.identity.application.role.mgt.model.ApplicationRole;
import org.wso2.carbon.identity.application.role.mgt.model.Group;
import org.wso2.carbon.identity.application.role.mgt.model.User;
import org.wso2.carbon.identity.application.role.mgt.util.ApplicationRoleMgtUtils;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.List;

import static org.wso2.carbon.identity.application.role.mgt.constants.ApplicationRoleMgtConstants.ErrorMessages.ERROR_CODE_CHECKING_ROLE_EXISTENCE;
import static org.wso2.carbon.identity.application.role.mgt.constants.ApplicationRoleMgtConstants.ErrorMessages.ERROR_CODE_CHECKING_ROLE_EXISTENCE_BY_ID;
import static org.wso2.carbon.identity.application.role.mgt.constants.ApplicationRoleMgtConstants.ErrorMessages.ERROR_CODE_DELETE_ROLE;
import static org.wso2.carbon.identity.application.role.mgt.constants.ApplicationRoleMgtConstants.ErrorMessages.ERROR_CODE_GET_ROLES_BY_APPLICATION;
import static org.wso2.carbon.identity.application.role.mgt.constants.ApplicationRoleMgtConstants.ErrorMessages.ERROR_CODE_GET_ROLES_BY_GROUP_ID;
import static org.wso2.carbon.identity.application.role.mgt.constants.ApplicationRoleMgtConstants.ErrorMessages.ERROR_CODE_GET_ROLES_BY_USER_ID;
import static org.wso2.carbon.identity.application.role.mgt.constants.ApplicationRoleMgtConstants.ErrorMessages.ERROR_CODE_GET_ROLE_ASSIGNED_GROUPS;
import static org.wso2.carbon.identity.application.role.mgt.constants.ApplicationRoleMgtConstants.ErrorMessages.ERROR_CODE_GET_ROLE_ASSIGNED_USERS;
import static org.wso2.carbon.identity.application.role.mgt.constants.ApplicationRoleMgtConstants.ErrorMessages.ERROR_CODE_GET_ROLE_BY_ID;
import static org.wso2.carbon.identity.application.role.mgt.constants.ApplicationRoleMgtConstants.ErrorMessages.ERROR_CODE_GET_SCOPES_BY_ROLE_ID;
import static org.wso2.carbon.identity.application.role.mgt.constants.ApplicationRoleMgtConstants.ErrorMessages.ERROR_CODE_GROUP_ALREADY_ASSIGNED;
import static org.wso2.carbon.identity.application.role.mgt.constants.ApplicationRoleMgtConstants.ErrorMessages.ERROR_CODE_INSERT_ROLE;
import static org.wso2.carbon.identity.application.role.mgt.constants.ApplicationRoleMgtConstants.ErrorMessages.ERROR_CODE_SCOPE_ALREADY_ASSIGNED;
import static org.wso2.carbon.identity.application.role.mgt.constants.ApplicationRoleMgtConstants.ErrorMessages.ERROR_CODE_UPDATE_ROLE;
import static org.wso2.carbon.identity.application.role.mgt.constants.ApplicationRoleMgtConstants.ErrorMessages.ERROR_CODE_UPDATE_ROLE_ASSIGNED_GROUPS;
import static org.wso2.carbon.identity.application.role.mgt.constants.ApplicationRoleMgtConstants.ErrorMessages.ERROR_CODE_UPDATE_ROLE_ASSIGNED_USERS;
import static org.wso2.carbon.identity.application.role.mgt.constants.ApplicationRoleMgtConstants.ErrorMessages.ERROR_CODE_USER_ALREADY_ASSIGNED;
import static org.wso2.carbon.identity.application.role.mgt.constants.ApplicationRoleMgtConstants.ErrorMessages.ERROR_CODE_USER_NOT_FOUND;
import static org.wso2.carbon.identity.application.role.mgt.constants.SQLConstants.GROUP_ROLE_UNIQUE_CONSTRAINT;
import static org.wso2.carbon.identity.application.role.mgt.constants.SQLConstants.ROLE_SCOPE_UNIQUE_CONSTRAINT;
import static org.wso2.carbon.identity.application.role.mgt.constants.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_APP_ID;
import static org.wso2.carbon.identity.application.role.mgt.constants.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_GROUP_ID;
import static org.wso2.carbon.identity.application.role.mgt.constants.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_IDP_ID;
import static org.wso2.carbon.identity.application.role.mgt.constants.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_ROLE_ID;
import static org.wso2.carbon.identity.application.role.mgt.constants.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_ROLE_NAME;
import static org.wso2.carbon.identity.application.role.mgt.constants.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_SCOPE_NAME;
import static org.wso2.carbon.identity.application.role.mgt.constants.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_TENANT_ID;
import static org.wso2.carbon.identity.application.role.mgt.constants.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_USER_ID;
import static org.wso2.carbon.identity.application.role.mgt.constants.SQLConstants.USER_ROLE_UNIQUE_CONSTRAINT;
import static org.wso2.carbon.identity.application.role.mgt.util.ApplicationRoleMgtUtils.getNewTemplate;
import static org.wso2.carbon.identity.application.role.mgt.util.ApplicationRoleMgtUtils.handleServerException;

/**
 * Application role DAO implementation.
 */
public class ApplicationRoleMgtDAOImpl implements ApplicationRoleMgtDAO {

    @Override
    public ApplicationRole addApplicationRole(ApplicationRole applicationRole, String tenantDomain)
            throws ApplicationRoleManagementException {

        NamedJdbcTemplate namedJdbcTemplate = getNewTemplate();
        try {
            namedJdbcTemplate.withTransaction(template -> {
                // Insert app id, role id, role name.
                template.executeInsert(SQLConstants.ADD_APPLICATION_ROLE, preparedStatement -> {
                    preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ROLE_ID, applicationRole.getRoleId());
                    preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_APP_ID,
                        applicationRole.getApplicationId());
                    preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ROLE_NAME,
                        applicationRole.getRoleName());
                    preparedStatement.setInt(DB_SCHEMA_COLUMN_NAME_TENANT_ID, getTenantId(tenantDomain));
                    }, null, false);
                // Insert scopes.
                if (applicationRole != null && applicationRole.getPermissions().length > 0) {
                    template.executeBatchInsert(SQLConstants.ADD_ROLE_SCOPE,
                        preparedStatement -> {
                            for (String scope : applicationRole.getPermissions()) {
                                preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ROLE_ID, applicationRole.getRoleId());
                                preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_SCOPE_NAME, scope);
                                preparedStatement.setInt(DB_SCHEMA_COLUMN_NAME_TENANT_ID, getTenantId(tenantDomain));
                                preparedStatement.addBatch();
                            }
                        }, applicationRole.getRoleId());
                }
                return null;
            });
            return getApplicationRoleById(applicationRole.getRoleId(), tenantDomain);
        } catch (TransactionException e) {
            if (checkUniqueKeyConstrainViolated(e, ROLE_SCOPE_UNIQUE_CONSTRAINT)) {
                throw ApplicationRoleMgtUtils.handleClientException(ERROR_CODE_SCOPE_ALREADY_ASSIGNED,
                        applicationRole.getRoleId());
            }
            throw handleServerException(ERROR_CODE_INSERT_ROLE, e, applicationRole.getRoleName(),
                    applicationRole.getApplicationId());
        }
    }

    @Override
    public ApplicationRole getApplicationRoleById(String roleId, String tenantDomain)
            throws ApplicationRoleManagementServerException {

        NamedJdbcTemplate namedJdbcTemplate = getNewTemplate();
        try {
            ApplicationRole applicationRole =
                    namedJdbcTemplate.fetchSingleRecord(SQLConstants.GET_APPLICATION_ROLE_BY_ID,
                        (resultSet, rowNumber) ->
                                new ApplicationRole(resultSet.getString(DB_SCHEMA_COLUMN_NAME_ROLE_ID),
                                        resultSet.getString(DB_SCHEMA_COLUMN_NAME_ROLE_NAME),
                                        resultSet.getString(DB_SCHEMA_COLUMN_NAME_APP_ID)),
                        preparedStatement -> {
                            preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ROLE_ID, roleId);
                            preparedStatement.setInt(DB_SCHEMA_COLUMN_NAME_TENANT_ID, getTenantId(tenantDomain));
                    });
            List<String> scopes;
            scopes = namedJdbcTemplate.executeQuery(SQLConstants.GET_APPLICATION_ROLE_SCOPE,
                    (resultSet, rowNumber) -> resultSet.getString(DB_SCHEMA_COLUMN_NAME_SCOPE_NAME),
                    preparedStatement -> {
                        preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ROLE_ID, roleId);
                        preparedStatement.setInt(DB_SCHEMA_COLUMN_NAME_TENANT_ID, getTenantId(tenantDomain));
                    });
            applicationRole.setPermissions(scopes.toArray(new String[0]));
            return  applicationRole;
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
                    preparedStatement -> preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_APP_ID, applicationId));
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_GET_ROLES_BY_APPLICATION, e, applicationId);
        }
    }

    @Override
    public ApplicationRole updateApplicationRole(String roleId, String newName, List<String> addedScopes,
                                      List<String> removedScopes, String tenantDomain)
            throws ApplicationRoleManagementException {

        NamedJdbcTemplate namedJdbcTemplate = getNewTemplate();
        try {
            namedJdbcTemplate.withTransaction(template -> {
                // Update role name.
                if (newName != null) {
                    template.executeUpdate(SQLConstants.UPDATE_APPLICATION_ROLE_BY_ID, preparedStatement -> {
                        preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ROLE_NAME, newName);
                        preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ROLE_ID, roleId);
                        preparedStatement.setInt(DB_SCHEMA_COLUMN_NAME_TENANT_ID, getTenantId(tenantDomain));
                    });
                }
                // Add scopes.
                if (addedScopes != null && !addedScopes.isEmpty()) {
                    template.executeBatchInsert(SQLConstants.ADD_ROLE_SCOPE,
                        preparedStatement -> {
                            for (String scope : addedScopes) {
                                preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ROLE_ID, roleId);
                                preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_SCOPE_NAME, scope);
                                preparedStatement.setInt(DB_SCHEMA_COLUMN_NAME_TENANT_ID, getTenantId(tenantDomain));
                                preparedStatement.addBatch();
                            }
                        }, roleId);
                }
                // Remove scopes.
                if (removedScopes != null && !removedScopes.isEmpty()) {
                    for (String scope: removedScopes) {
                        template.executeUpdate(SQLConstants.DELETE_ROLE_SCOPE,
                            preparedStatement -> {
                                preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ROLE_ID, roleId);
                                preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_SCOPE_NAME, scope);
                                preparedStatement.setInt(DB_SCHEMA_COLUMN_NAME_TENANT_ID, getTenantId(tenantDomain));
                            });
                    }
                }
                return null;
            });
            return getApplicationRoleById(roleId, tenantDomain);
        } catch (TransactionException e) {
            if (checkUniqueKeyConstrainViolated(e, ROLE_SCOPE_UNIQUE_CONSTRAINT)) {
                throw ApplicationRoleMgtUtils.handleClientException(ERROR_CODE_SCOPE_ALREADY_ASSIGNED, roleId);
            }
            throw handleServerException(ERROR_CODE_UPDATE_ROLE, e, roleId);
        }
    }

    @Override
    public void deleteApplicationRole(String roleId, String tenantDomain)
            throws ApplicationRoleManagementServerException {

        NamedJdbcTemplate namedJdbcTemplate = getNewTemplate();
        try {
            namedJdbcTemplate.executeUpdate(SQLConstants.DELETE_APPLICATION_ROLE_BY_ID,
                preparedStatement -> {
                    preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ROLE_ID, roleId);
                    preparedStatement.setInt(DB_SCHEMA_COLUMN_NAME_TENANT_ID, getTenantId(tenantDomain));
                });
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_DELETE_ROLE, e, roleId);
        }
    }

    @Override
    public boolean isExistingRole(String applicationId, String roleName, String tenantDomain)
            throws ApplicationRoleManagementServerException {


        NamedJdbcTemplate namedJdbcTemplate = getNewTemplate();
        try {
            return namedJdbcTemplate.fetchSingleRecord(SQLConstants.IS_APPLICATION_ROLE_EXISTS,
                    (resultSet, rowNumber) -> resultSet.getInt(1) > 0,
                    preparedStatement -> {
                        preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ROLE_NAME, roleName);
                        preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_APP_ID, applicationId);
                        preparedStatement.setInt(DB_SCHEMA_COLUMN_NAME_TENANT_ID, getTenantId(tenantDomain));
                    });
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_CHECKING_ROLE_EXISTENCE, e, roleName, applicationId);
        }
    }

    @Override
    public boolean checkRoleExists(String roleId, String tenantDomain) throws ApplicationRoleManagementServerException {

        NamedJdbcTemplate namedJdbcTemplate = getNewTemplate();
        try {
            return namedJdbcTemplate.fetchSingleRecord(SQLConstants.IS_APPLICATION_ROLE_EXISTS_BY_ID,
                    (resultSet, rowNumber) -> resultSet.getInt(1) > 0,
                    preparedStatement -> {
                        preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ROLE_ID, roleId);
                        preparedStatement.setInt(DB_SCHEMA_COLUMN_NAME_TENANT_ID, getTenantId(tenantDomain));
                    });
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_CHECKING_ROLE_EXISTENCE_BY_ID, e, roleId);
        }
    }

    @Override
    public ApplicationRole updateApplicationRoleAssignedUsers(String roleId, List<String> addedUsers,
                                                              List<String> removedUsers, String tenantDomain)
            throws ApplicationRoleManagementException {

        // Validate given userIds are exists.
        validateUserIds(addedUsers);
        NamedJdbcTemplate namedJdbcTemplate = getNewTemplate();
        try {
            namedJdbcTemplate.withTransaction(template -> {
                // Add users.
                if (addedUsers.size() > 0) {
                    template.executeBatchInsert(SQLConstants.ADD_APPLICATION_ROLE_USER, (preparedStatement -> {
                        for (String userId : addedUsers) {
                            preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ROLE_ID, roleId);
                            preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_USER_ID, userId);
                            preparedStatement.setInt(DB_SCHEMA_COLUMN_NAME_TENANT_ID, getTenantId(tenantDomain));
                            preparedStatement.addBatch();
                        }
                    }), roleId);
                }
                // Remove users.
                if (removedUsers.size() > 0) {
                    for (String userId: removedUsers) {
                        template.executeUpdate(SQLConstants.DELETE_ASSIGNED_USER_APPLICATION_ROLE,
                                preparedStatement -> {
                                    preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ROLE_ID, roleId);
                                    preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_USER_ID, userId);
                                    preparedStatement.setInt(DB_SCHEMA_COLUMN_NAME_TENANT_ID,
                                            getTenantId(tenantDomain));
                                });
                    }
                }
                return null;
            });
            return getApplicationRoleAssignedUsers(roleId, tenantDomain);
        } catch (TransactionException e) {
            if (checkUniqueKeyConstrainViolated(e, USER_ROLE_UNIQUE_CONSTRAINT)) {
                throw ApplicationRoleMgtUtils.handleClientException(ERROR_CODE_USER_ALREADY_ASSIGNED, roleId);
            }
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
                    preparedStatement -> {
                        preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ROLE_ID, roleId);
                        preparedStatement.setInt(DB_SCHEMA_COLUMN_NAME_TENANT_ID, getTenantId(tenantDomain));
                    });
            for (User user : users) {
                user.setUserName(ApplicationRoleMgtUtils.getUserNameByID(user.getId(), tenantDomain));
            }
            ApplicationRole applicationRole = new ApplicationRole();
            applicationRole.setAssignedUsers(users);
            return applicationRole;
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_GET_ROLE_ASSIGNED_USERS, e, roleId);
        }
    }

    @Override
    public ApplicationRole updateApplicationRoleAssignedGroups(String roleId, List<Group> addedGroups,
                                                               List<String> removedGroups,
                                                    String tenantDomain)
            throws ApplicationRoleManagementException {

        NamedJdbcTemplate namedJdbcTemplate = getNewTemplate();
        try {
            namedJdbcTemplate.withTransaction(template -> {
                // Add groups.
                if (addedGroups.size() > 0) {
                    template.executeBatchInsert(SQLConstants.ADD_APPLICATION_ROLE_GROUP, (preparedStatement -> {
                        for (Group group : addedGroups) {
                            preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ROLE_ID, roleId);
                            preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_GROUP_ID, group.getGroupId());
                            preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_IDP_ID,
                                    group.getIdpId());
                            preparedStatement.setInt(DB_SCHEMA_COLUMN_NAME_TENANT_ID,
                                    getTenantId(tenantDomain));
                            preparedStatement.addBatch();
                        }
                    }), roleId);
                }
                // Remove groups.
                if (removedGroups.size() > 0) {
                    for (String groupId: removedGroups) {
                        template.executeUpdate(SQLConstants.DELETE_ASSIGNED_GROUP_APPLICATION_ROLE,
                                preparedStatement -> {
                                    preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ROLE_ID, roleId);
                                    preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_GROUP_ID, groupId);
                                    preparedStatement.setInt(DB_SCHEMA_COLUMN_NAME_TENANT_ID,
                                            getTenantId(tenantDomain));
                                });
                    }
                }
                return null;
            });
            return getApplicationRoleAssignedGroups(roleId, tenantDomain);
        } catch (TransactionException e) {
            if (checkUniqueKeyConstrainViolated(e, GROUP_ROLE_UNIQUE_CONSTRAINT)) {
                throw ApplicationRoleMgtUtils.handleClientException(ERROR_CODE_GROUP_ALREADY_ASSIGNED, roleId);
            }
            throw handleServerException(ERROR_CODE_UPDATE_ROLE_ASSIGNED_GROUPS, e, roleId);
        }
    }

    @Override
    public ApplicationRole getApplicationRoleAssignedGroups(String roleId, String tenantDomain) throws
            ApplicationRoleManagementException {

        NamedJdbcTemplate namedJdbcTemplate = getNewTemplate();
        try {
            List<Group> groups;
            groups = namedJdbcTemplate.executeQuery(SQLConstants.GET_ASSIGNED_GROUPS_OF_APPLICATION_ROLE,
                    (resultSet, rowNumber) -> new Group(resultSet.getString(DB_SCHEMA_COLUMN_NAME_GROUP_ID),
                            resultSet.getString(DB_SCHEMA_COLUMN_NAME_IDP_ID)),
                    preparedStatement -> {
                        preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ROLE_ID, roleId);
                        preparedStatement.setInt(DB_SCHEMA_COLUMN_NAME_TENANT_ID, getTenantId(tenantDomain));
                    });
            ApplicationRole applicationRole = new ApplicationRole();
            applicationRole.setAssignedGroups(groups);
            return applicationRole;
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_GET_ROLE_ASSIGNED_GROUPS, e, roleId);
        }
    }

    @Override
    public ApplicationRole getApplicationRoleAssignedGroups(String roleId, String idpId, String tenantDomain)
            throws ApplicationRoleManagementException {

        NamedJdbcTemplate namedJdbcTemplate = getNewTemplate();
        try {
            List<Group> groups;
            groups = namedJdbcTemplate.executeQuery(SQLConstants.GET_ASSIGNED_GROUPS_OF_APPLICATION_ROLE_IDP_FILTER,
                    (resultSet, rowNumber) -> new Group(resultSet.getString(DB_SCHEMA_COLUMN_NAME_GROUP_ID),
                            resultSet.getString(DB_SCHEMA_COLUMN_NAME_IDP_ID)),
                    preparedStatement -> {
                        preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ROLE_ID, roleId);
                        preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_IDP_ID, idpId);
                        preparedStatement.setInt(DB_SCHEMA_COLUMN_NAME_TENANT_ID, getTenantId(tenantDomain));
                    });
            ApplicationRole applicationRole = new ApplicationRole();
            applicationRole.setAssignedGroups(groups);
            return applicationRole;
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_GET_ROLE_ASSIGNED_GROUPS, e, roleId);
        }
    }

    @Override
    public List<ApplicationRole> getApplicationRolesByUserId(String userId, String tenantDomain)
            throws ApplicationRoleManagementException {

        NamedJdbcTemplate namedJdbcTemplate = getNewTemplate();
        try {
            return namedJdbcTemplate.executeQuery(SQLConstants.GET_APPLICATION_ROLES_BY_USER_ID,
                    (resultSet, rowNumber) ->
                            new ApplicationRole(resultSet.getString(DB_SCHEMA_COLUMN_NAME_ROLE_ID),
                                    resultSet.getString(DB_SCHEMA_COLUMN_NAME_ROLE_NAME),
                                    resultSet.getString(DB_SCHEMA_COLUMN_NAME_APP_ID)),
                    preparedStatement -> {
                        preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_USER_ID, userId);
                        preparedStatement.setInt(DB_SCHEMA_COLUMN_NAME_TENANT_ID, getTenantId(tenantDomain));
                    });
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_GET_ROLES_BY_USER_ID, e, userId);
        }
    }

    @Override
    public List<ApplicationRole> getApplicationRolesByUserId(String userId, String appId, String tenantDomain)
            throws ApplicationRoleManagementException {

        NamedJdbcTemplate namedJdbcTemplate = getNewTemplate();
        try {
            return namedJdbcTemplate.executeQuery(SQLConstants.GET_APPLICATION_ROLES_BY_USER_ID_APP_ID,
                    (resultSet, rowNumber) ->
                            new ApplicationRole(resultSet.getString(DB_SCHEMA_COLUMN_NAME_ROLE_ID),
                                    resultSet.getString(DB_SCHEMA_COLUMN_NAME_ROLE_NAME),
                                    resultSet.getString(DB_SCHEMA_COLUMN_NAME_APP_ID)),
                    preparedStatement -> {
                        preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_USER_ID, userId);
                        preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_APP_ID, appId);
                        preparedStatement.setInt(DB_SCHEMA_COLUMN_NAME_TENANT_ID, getTenantId(tenantDomain));
                    });
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_GET_ROLES_BY_USER_ID, e, userId);
        }
    }

    @Override
    public List<ApplicationRole> getApplicationRolesByGroupId(String groupId, String tenantDomain)
            throws ApplicationRoleManagementException {

        NamedJdbcTemplate namedJdbcTemplate = getNewTemplate();
        try {
            return namedJdbcTemplate.executeQuery(SQLConstants.GET_APPLICATION_ROLES_BY_GROUP_ID,
                    (resultSet, rowNumber) ->
                            new ApplicationRole(resultSet.getString(DB_SCHEMA_COLUMN_NAME_ROLE_ID),
                                    resultSet.getString(DB_SCHEMA_COLUMN_NAME_ROLE_NAME),
                                    resultSet.getString(DB_SCHEMA_COLUMN_NAME_APP_ID)),
                    preparedStatement -> {
                        preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_GROUP_ID, groupId);
                        preparedStatement.setInt(DB_SCHEMA_COLUMN_NAME_TENANT_ID, getTenantId(tenantDomain));
                    });
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_GET_ROLES_BY_GROUP_ID, e, groupId);
        }
    }

    @Override
    public List<ApplicationRole> getApplicationRolesByGroupId(String groupId, String appId, String tenantDomain)
            throws ApplicationRoleManagementException {

        NamedJdbcTemplate namedJdbcTemplate = getNewTemplate();
        try {
            return namedJdbcTemplate.executeQuery(SQLConstants.GET_APPLICATION_ROLES_BY_GROUP_ID_APP_ID,
                    (resultSet, rowNumber) ->
                            new ApplicationRole(resultSet.getString(DB_SCHEMA_COLUMN_NAME_ROLE_ID),
                                    resultSet.getString(DB_SCHEMA_COLUMN_NAME_ROLE_NAME),
                                    resultSet.getString(DB_SCHEMA_COLUMN_NAME_APP_ID)),
                    preparedStatement -> {
                        preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_GROUP_ID, groupId);
                        preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_APP_ID, appId);
                        preparedStatement.setInt(DB_SCHEMA_COLUMN_NAME_TENANT_ID, getTenantId(tenantDomain));
                    });
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_GET_ROLES_BY_GROUP_ID, e, groupId);
        }
    }

    @Override
    public List<ApplicationRole> getApplicationRolesByGroupIds(List<String> groupIds, String appId, String tenantDomain)
            throws ApplicationRoleManagementException {

        NamedJdbcTemplate namedJdbcTemplate = getNewTemplate();
        try {
            String query = buildArrayFlattenQuery(SQLConstants.GET_APPLICATION_ROLES_BY_GROUP_IDS,
                    DB_SCHEMA_COLUMN_NAME_GROUP_ID, groupIds);
            return namedJdbcTemplate.executeQuery(query,
                    (resultSet, rowNumber) ->
                            new ApplicationRole(resultSet.getString(DB_SCHEMA_COLUMN_NAME_ROLE_ID),
                                    resultSet.getString(DB_SCHEMA_COLUMN_NAME_ROLE_NAME),
                                    resultSet.getString(DB_SCHEMA_COLUMN_NAME_APP_ID)),
                    preparedStatement -> {
                        preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_APP_ID, appId);
                        preparedStatement.setInt(DB_SCHEMA_COLUMN_NAME_TENANT_ID, getTenantId(tenantDomain));
                        for (int i = 0; i < groupIds.size(); i++) {
                            preparedStatement.setString(i + 3, groupIds.get(i));
                        }
                    });
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_GET_ROLES_BY_GROUP_ID, e);
        }
    }

    @Override
    public List<String> getScopesByRoleIds(List<String> roleIds, String tenantDomain)
            throws ApplicationRoleManagementException {

        NamedJdbcTemplate namedJdbcTemplate = getNewTemplate();
        try {
            String query = buildArrayFlattenQuery(SQLConstants.GET_SCOPES_BY_ROLE_IDS,
                    DB_SCHEMA_COLUMN_NAME_ROLE_ID, roleIds);
            return namedJdbcTemplate.executeQuery(query,
                    (resultSet, rowNumber) ->
                            resultSet.getString(DB_SCHEMA_COLUMN_NAME_SCOPE_NAME),
                    preparedStatement -> {
                        preparedStatement.setInt(DB_SCHEMA_COLUMN_NAME_TENANT_ID, getTenantId(tenantDomain));
                        for (int i = 0; i < roleIds.size(); i++) {
                            preparedStatement.setString(i + 2, roleIds.get(i));
                        }
                    });
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_GET_SCOPES_BY_ROLE_ID, e);
        }
    }

    /**
     * Build flatten query for given query with ids.
     *
     * @param query SQL query.
     * @param colName Column name.
     * @param ids List of IDs.
     */
    private String buildArrayFlattenQuery(String query, String colName, List<String> ids) {

        StringBuilder queryBuilder = new StringBuilder(query);
        queryBuilder.append(" AND ").append(colName).append(" IN (");

        // Create placeholders for the IDs
        for (int i = 0; i < ids.size(); i++) {
            queryBuilder.append("?");
            if (i < ids.size() - 1) {
                queryBuilder.append(",");
            }
        }

        queryBuilder.append(");");

        return queryBuilder.toString();
    }

    /**
     * Validate users.
     *
     * @param users User IDs.
     * @throws ApplicationRoleManagementException Error occurred while validating users.
     */
    private void validateUserIds(List<String> users)
            throws ApplicationRoleManagementException {

        for (String userId : users) {
            boolean isExists = ApplicationRoleMgtUtils.isUserExists(userId);
            if (!isExists) {
                throw ApplicationRoleMgtUtils.handleClientException(ERROR_CODE_USER_NOT_FOUND, userId);
            }
        }
    }

    /**
     * Check SQL Unique Key Constrain Violated.
     *
     * @param e Transaction Exception.
     * @param constraint SQL constraint.
     */
    private boolean checkUniqueKeyConstrainViolated(TransactionException e, String constraint) {

        String errorMessage = e.getCause().getCause().getMessage();
        return errorMessage.toLowerCase().contains(constraint.toLowerCase());
    }

    /**
     * Get tenant id by name.
     *
     * @param tenantDomain Tenant Domain.
     */
    private int getTenantId(String tenantDomain) {

        int tenantID;
        if (tenantDomain != null) {
            tenantID = IdentityTenantUtil.getTenantId(tenantDomain);
        } else {
            tenantID = MultitenantConstants.INVALID_TENANT_ID;
        }
        return tenantID;
    }


}
