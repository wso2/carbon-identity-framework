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

package org.wso2.carbon.identity.role.mgt.core.v2.dao;

import org.apache.axiom.om.OMElement;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.database.utils.jdbc.NamedPreparedStatement;
import org.wso2.carbon.identity.base.IdentityConstants;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.role.mgt.core.GroupBasicInfo;
import org.wso2.carbon.identity.role.mgt.core.IdentityRoleManagementClientException;
import org.wso2.carbon.identity.role.mgt.core.IdentityRoleManagementException;
import org.wso2.carbon.identity.role.mgt.core.IdentityRoleManagementServerException;
import org.wso2.carbon.identity.role.mgt.core.UserBasicInfo;
import org.wso2.carbon.identity.role.mgt.core.util.GroupIDResolver;
import org.wso2.carbon.identity.role.mgt.core.util.RoleManagementUtils;
import org.wso2.carbon.identity.role.mgt.core.util.UserIDResolver;
import org.wso2.carbon.identity.role.mgt.core.v2.AssociatedApplication;
import org.wso2.carbon.identity.role.mgt.core.v2.Permission;
import org.wso2.carbon.identity.role.mgt.core.v2.Role;
import org.wso2.carbon.identity.role.mgt.core.v2.RoleAudience;
import org.wso2.carbon.identity.role.mgt.core.v2.RoleBasicInfo;
import org.wso2.carbon.identity.role.mgt.core.v2.RoleConstants;
import org.wso2.carbon.identity.role.mgt.core.v2.internal.RoleManagementServiceComponentHolder;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreConfigConstants;
import org.wso2.carbon.user.core.authorization.AuthorizationCache;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.common.UserRolesCache;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.xml.namespace.QName;

import static org.wso2.carbon.identity.role.mgt.core.RoleConstants.Error.OPERATION_FORBIDDEN;
import static org.wso2.carbon.identity.role.mgt.core.RoleConstants.Error.ROLE_NOT_FOUND;
import static org.wso2.carbon.identity.role.mgt.core.RoleConstants.RoleTableColumns.USER_NOT_FOUND_ERROR_MESSAGE;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.ADD_GROUP_TO_ROLE_SQL;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.ADD_GROUP_TO_ROLE_SQL_MSSQL;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.ADD_SCIM_ROLE_ID_SQL;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.ADD_USER_TO_ROLE_SQL;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.ADD_USER_TO_ROLE_SQL_MSSQL;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.DELETE_ROLE_SQL;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.DELETE_SCIM_ROLE_SQL;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.GET_GROUP_LIST_OF_ROLE_SQL;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.GET_ROLE_ID_BY_NAME_SQL;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.GET_ROLE_NAME_BY_ID_SQL;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.GET_USER_LIST_OF_ROLE_SQL;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.IS_ROLE_EXIST_SQL;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.IS_ROLE_ID_EXIST_SQL;
import static org.wso2.carbon.identity.role.mgt.core.v2.RoleConstants.APPLICATION;
import static org.wso2.carbon.identity.role.mgt.core.v2.RoleConstants.DB2;
import static org.wso2.carbon.identity.role.mgt.core.v2.RoleConstants.Error.INVALID_LIMIT;
import static org.wso2.carbon.identity.role.mgt.core.v2.RoleConstants.Error.INVALID_OFFSET;
import static org.wso2.carbon.identity.role.mgt.core.v2.RoleConstants.Error.INVALID_REQUEST;
import static org.wso2.carbon.identity.role.mgt.core.v2.RoleConstants.Error.ROLE_ALREADY_EXISTS;
import static org.wso2.carbon.identity.role.mgt.core.v2.RoleConstants.Error.SORTING_NOT_IMPLEMENTED;
import static org.wso2.carbon.identity.role.mgt.core.v2.RoleConstants.Error.UNEXPECTED_SERVER_ERROR;
import static org.wso2.carbon.identity.role.mgt.core.v2.RoleConstants.H2;
import static org.wso2.carbon.identity.role.mgt.core.v2.RoleConstants.INFORMIX;
import static org.wso2.carbon.identity.role.mgt.core.v2.RoleConstants.MARIADB;
import static org.wso2.carbon.identity.role.mgt.core.v2.RoleConstants.MICROSOFT;
import static org.wso2.carbon.identity.role.mgt.core.v2.RoleConstants.MY_SQL;
import static org.wso2.carbon.identity.role.mgt.core.v2.RoleConstants.ORACLE;
import static org.wso2.carbon.identity.role.mgt.core.v2.RoleConstants.ORGANIZATION;
import static org.wso2.carbon.identity.role.mgt.core.v2.RoleConstants.POSTGRE_SQL;
import static org.wso2.carbon.identity.role.mgt.core.v2.dao.SQLQueries.ADD_APP_ROLE_ASSOCIATION_SQL;
import static org.wso2.carbon.identity.role.mgt.core.v2.dao.SQLQueries.ADD_ROLE_AUDIENCE_SQL;
import static org.wso2.carbon.identity.role.mgt.core.v2.dao.SQLQueries.ADD_ROLE_SCOPE_SQL;
import static org.wso2.carbon.identity.role.mgt.core.v2.dao.SQLQueries.ADD_ROLE_WITH_AUDIENCE_SQL;
import static org.wso2.carbon.identity.role.mgt.core.v2.dao.SQLQueries.DELETE_APP_ROLE_ASSOCIATION_BY_ROLE_ID_SQL;
import static org.wso2.carbon.identity.role.mgt.core.v2.dao.SQLQueries.DELETE_ROLE_SCOPE_BY_ROLE_ID_SQL;
import static org.wso2.carbon.identity.role.mgt.core.v2.dao.SQLQueries.DELETE_ROLE_SCOPE_BY_SCOPE_NAME_SQL;
import static org.wso2.carbon.identity.role.mgt.core.v2.dao.SQLQueries.GET_APP_NAME_BY_APP_ID;
import static org.wso2.carbon.identity.role.mgt.core.v2.dao.SQLQueries.GET_ASSOCIATED_APPS_BY_ROLE_ID_SQL;
import static org.wso2.carbon.identity.role.mgt.core.v2.dao.SQLQueries.GET_ROLES_BY_TENANT_AND_ROLE_NAME_DB2;
import static org.wso2.carbon.identity.role.mgt.core.v2.dao.SQLQueries.GET_ROLES_BY_TENANT_AND_ROLE_NAME_INFORMIX;
import static org.wso2.carbon.identity.role.mgt.core.v2.dao.SQLQueries.GET_ROLES_BY_TENANT_AND_ROLE_NAME_MSSQL;
import static org.wso2.carbon.identity.role.mgt.core.v2.dao.SQLQueries.GET_ROLES_BY_TENANT_AND_ROLE_NAME_MYSQL;
import static org.wso2.carbon.identity.role.mgt.core.v2.dao.SQLQueries.GET_ROLES_BY_TENANT_AND_ROLE_NAME_ORACLE;
import static org.wso2.carbon.identity.role.mgt.core.v2.dao.SQLQueries.GET_ROLES_BY_TENANT_AND_ROLE_NAME_POSTGRESQL;
import static org.wso2.carbon.identity.role.mgt.core.v2.dao.SQLQueries.GET_ROLES_BY_TENANT_DB2;
import static org.wso2.carbon.identity.role.mgt.core.v2.dao.SQLQueries.GET_ROLES_BY_TENANT_INFORMIX;
import static org.wso2.carbon.identity.role.mgt.core.v2.dao.SQLQueries.GET_ROLES_BY_TENANT_MSSQL;
import static org.wso2.carbon.identity.role.mgt.core.v2.dao.SQLQueries.GET_ROLES_BY_TENANT_MYSQL;
import static org.wso2.carbon.identity.role.mgt.core.v2.dao.SQLQueries.GET_ROLES_BY_TENANT_ORACLE;
import static org.wso2.carbon.identity.role.mgt.core.v2.dao.SQLQueries.GET_ROLES_BY_TENANT_POSTGRESQL;
import static org.wso2.carbon.identity.role.mgt.core.v2.dao.SQLQueries.GET_ROLE_AUDIENCE_BY_ROLE_NAME_SQL;
import static org.wso2.carbon.identity.role.mgt.core.v2.dao.SQLQueries.GET_ROLE_AUDIENCE_SQL;
import static org.wso2.carbon.identity.role.mgt.core.v2.dao.SQLQueries.GET_ROLE_SCOPE_SQL;

/**
 * Implementation of the {@link RoleDAO} interface.
 */
public class RoleDAOImpl implements RoleDAO {

    private Log log = LogFactory.getLog(org.wso2.carbon.identity.role.mgt.core.dao.RoleDAOImpl.class);
    private GroupIDResolver groupIDResolver = new GroupIDResolver();
    private UserIDResolver userIDResolver = new UserIDResolver();
    private Set<String> systemRoles = getSystemRoles();

    @Override
    public RoleBasicInfo addRole(String roleName, List<String> userList, List<String> groupList,
                                 List<Permission> permissions, String audience, String audienceId, String tenantDomain)
            throws IdentityRoleManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        if (log.isDebugEnabled()) {
            log.debug("Creating the role: " + roleName + " in the tenantDomain: " + tenantDomain);
        }

        String primaryDomainName = IdentityUtil.getPrimaryDomainName();
        if (primaryDomainName != null) {
            primaryDomainName = primaryDomainName.toUpperCase(Locale.ENGLISH);
        }

        // Remove internal domain before persisting in order to maintain the backward compatibility.
        roleName = removeInternalDomain(roleName);
        String roleID;

        // TODO: Here we need to validate, if audience is APPLICATION, the app should use application level roles.
        if (StringUtils.isEmpty(audience)) {
            audience = ORGANIZATION;
            audienceId = getOrganizationIdByTenantDomain(tenantDomain);
        }

        if (!isExistingRoleName(roleName, tenantDomain)) {
            try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(true)) {
                int roleAudienceId = getRoleAudienceId(audience, audienceId, connection);
                try {
                    try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                            ADD_ROLE_WITH_AUDIENCE_SQL, RoleConstants.RoleTableColumns.UM_ID)) {
                        statement.setString(RoleConstants.RoleTableColumns.UM_ROLE_NAME, roleName);
                        statement.setInt(RoleConstants.RoleTableColumns.UM_TENANT_ID, tenantId);
                        statement.setInt(RoleConstants.RoleTableColumns.AUDIENCE_ID, roleAudienceId);
                        statement.executeUpdate();
                    }

                    String databaseProductName = connection.getMetaData().getDatabaseProductName();
                    // Add users to the created role.
                    if (CollectionUtils.isNotEmpty(userList)) {
                        List<String> userNamesList = getUserNamesByIDs(userList, tenantDomain);
                        String addUsersSQL = ADD_USER_TO_ROLE_SQL;
                        if (MICROSOFT.equals(databaseProductName)) {
                            addUsersSQL = ADD_USER_TO_ROLE_SQL_MSSQL;
                        }
                        processBatchUpdateForUsers(roleName, userNamesList, tenantId, primaryDomainName, connection,
                                addUsersSQL);

                        for (String username : userNamesList) {
                            clearUserRolesCache(username, tenantId);
                        }
                    }

                    // Add groups to the created role.
                    if (CollectionUtils.isNotEmpty(groupList)) {
                        Map<String, String> groupIdsToNames = getGroupNamesByIDs(groupList, tenantDomain);
                        List<String> groupNamesList = new ArrayList<>(groupIdsToNames.values());
                        String addGroupsSQL = ADD_GROUP_TO_ROLE_SQL;
                        if (MICROSOFT.equals(databaseProductName)) {
                            addGroupsSQL = ADD_GROUP_TO_ROLE_SQL_MSSQL;
                        }
                        processBatchUpdateForGroups(roleName, groupNamesList, tenantId, primaryDomainName, connection,
                                addGroupsSQL);
                    }

                    // Handle role id, permissions
                    roleID = addRoleInfo(roleName, permissions, audience, audienceId, tenantDomain);

                    IdentityDatabaseUtil.commitUserDBTransaction(connection);
                } catch (SQLException | IdentityRoleManagementException e) {
                    IdentityDatabaseUtil.rollbackTransaction(connection);
                    String errorMessage = "Error while creating the role: %s in the tenantDomain: %s";
                    throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                            String.format(errorMessage, roleName, tenantDomain), e);
                }
            } catch (SQLException e) {
                String errorMessage = "Error while creating the role: %s in the tenantDomain: %s";
                throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                        String.format(errorMessage, roleName, tenantDomain), e);
            }
        } else {
            throw new IdentityRoleManagementClientException(ROLE_ALREADY_EXISTS.getCode(),
                    "Role already exist for the role name: " + roleName);
        }
        return new RoleBasicInfo(roleID, roleName);
    }

    @Override
    public List<RoleBasicInfo> getRoles(Integer limit, Integer offset, String sortBy, String sortOrder,
                                        String tenantDomain) throws IdentityRoleManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        limit = validateLimit(limit);
        offset = validateOffset(offset);
        validateAttributesForSorting(sortBy, sortOrder);
        List<RoleBasicInfo> roles;

        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(false)) {
            String databaseProductName = connection.getMetaData().getDatabaseProductName();
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                    getDBTypeSpecificRolesRetrievalQuery(databaseProductName), RoleConstants.RoleTableColumns.UM_ID)) {
                statement.setInt(RoleConstants.RoleTableColumns.UM_TENANT_ID, tenantId);
                roles = processListRolesQuery(limit, offset, statement, tenantDomain);
            }
        } catch (SQLException e) {
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                    "Error while listing roles in tenantDomain: " + tenantDomain, e);
        }
        return Collections.unmodifiableList(roles);
    }

    @Override
    public List<RoleBasicInfo> getRoles(String filter, Integer limit, Integer offset, String sortBy, String sortOrder,
                                        String tenantDomain) throws IdentityRoleManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        if (StringUtils.isBlank(filter) || RoleConstants.WILDCARD_CHARACTER.equals(filter)) {
            return getRoles(limit, offset, sortBy, sortOrder, tenantDomain);
        }
        String filterResolvedForSQL = resolveSQLFilter(filter);
        limit = validateLimit(limit);
        offset = validateOffset(offset);
        validateAttributesForSorting(sortBy, sortOrder);
        List<RoleBasicInfo> roles;

        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(false)) {
            String databaseProductName = connection.getMetaData().getDatabaseProductName();
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                    getDBTypeSpecificRolesRetrievalQueryByRoleName(databaseProductName),
                    RoleConstants.RoleTableColumns.UM_ID)) {
                statement.setInt(RoleConstants.RoleTableColumns.UM_TENANT_ID, tenantId);
                statement.setString(RoleConstants.RoleTableColumns.UM_ROLE_NAME, filterResolvedForSQL);
                roles = processListRolesQuery(limit, offset, statement, tenantDomain);
            }
        } catch (SQLException e) {
            throw new IdentityRoleManagementServerException(RoleConstants.Error.UNEXPECTED_SERVER_ERROR.getCode(),
                    "Error while listing roles in tenantDomain: " + tenantDomain, e);
        }
        return Collections.unmodifiableList(roles);
    }

    @Override
    public Role getRole(String roleID, String tenantDomain) throws IdentityRoleManagementException {

        Role role = new Role();
        String roleName = getRoleNameByID(roleID, tenantDomain);
        RoleAudience roleAudience = getAudienceIdByRoleName(roleName, tenantDomain);
        if (roleAudience != null) {
            role.setAudience(roleAudience.getAudience());
            role.setAudienceId(roleAudience.getAudienceId());
            role.setAudienceName(roleAudience.getAudienceName());

            if (ORGANIZATION.equalsIgnoreCase(roleAudience.getAudience())) {
                role.setAssociatedApplications(getAssociatedAppsById(roleID));
            }
        }
        role.setId(roleID);
        role.setName(roleName);
        role.setTenantDomain(tenantDomain);
        role.setUsers(getUserListOfRole(roleID, tenantDomain));
        role.setGroups(getGroupListOfRole(roleID, tenantDomain));
        role.setPermissions(getPermissions(roleID, tenantDomain));

        return role;
    }

    @Override
    public RoleBasicInfo getRoleBasicInfoById(String roleID, String tenantDomain)
            throws IdentityRoleManagementException {

        String roleName = getRoleNameByID(roleID, tenantDomain);
        RoleBasicInfo roleBasicInfo = new RoleBasicInfo(roleID, roleName);
        RoleAudience roleAudience = getAudienceIdByRoleName(roleName, tenantDomain);
        if (roleAudience != null) {
            roleBasicInfo.setAudience(roleAudience.getAudience());
            roleBasicInfo.setAudienceId(roleAudience.getAudienceId());
            roleBasicInfo.setAudienceName(roleAudience.getAudienceName());
        }
        return roleBasicInfo;
    }

    @Override
    public List<Permission> getPermissionListOfRole(String roleID, String tenantDomain)
            throws IdentityRoleManagementException {

        return getPermissions(roleID, tenantDomain);
    }

    @Override
    public RoleBasicInfo updatePermissionListOfRole(String roleId, List<Permission> addedPermissions,
                                                    List<Permission> deletedPermissions, String tenantDomain)
            throws IdentityRoleManagementException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(true)) {
            addPermissions(roleId, addedPermissions, tenantDomain, connection);
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                    DELETE_ROLE_SCOPE_BY_SCOPE_NAME_SQL)) {
                for (Permission permission : deletedPermissions) {
                    statement.setString(RoleConstants.RoleTableColumns.ROLE_ID, roleId);
                    statement.setString(RoleConstants.RoleTableColumns.SCOPE_NAME, permission.getName());
                    statement.addBatch();
                }
                statement.executeUpdate();
                IdentityDatabaseUtil.commitTransaction(connection);
            } catch (SQLException e) {
                IdentityDatabaseUtil.rollbackTransaction(connection);
                String errorMessage = "Error while adding permissions to roleID : " + roleId;
                throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                        errorMessage, e);
            }
        } catch (SQLException | IdentityRoleManagementException e) {
            String errorMessage = "Error while adding permissions to roleID : " + roleId;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                    errorMessage, e);
        }
        return getRoleBasicInfoById(roleId, tenantDomain);
    }

    /**
     * Delete all role associations (permissions, apps, shared roles).
     *
     * @param roleId Role ID.
     * @param connection DB connection.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    private void deleteRoleAssociations(String roleId, Connection connection) throws IdentityRoleManagementException {

        try {
            deleteAllPermissionsOfRole(roleId, connection);
            deleteAppRoleAssociation(roleId, connection);
            // TODO: handle shared roles
        } catch (IdentityRoleManagementException e) {
            String errorMessage = "Error while handling role deletion of roleID : " + roleId;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                    errorMessage, e);
        }
    }

    @Override
    public void deleteRole(String roleID, String tenantDomain) throws IdentityRoleManagementException {

        String roleName = getRoleNameByID(roleID, tenantDomain);
        if (systemRoles.contains(roleName)) {
            throw new IdentityRoleManagementClientException(OPERATION_FORBIDDEN.getCode(),
                    "Invalid operation. Role: " + roleName + " Cannot be deleted since it's a read only system role.");
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        UserRealm userRealm;
        try {
            userRealm = CarbonContext.getThreadLocalCarbonContext().getUserRealm();
            if (UserCoreUtil.isEveryoneRole(roleName, userRealm.getRealmConfiguration())) {
                throw new IdentityRoleManagementClientException(OPERATION_FORBIDDEN.getCode(),
                        "Invalid operation. Role: " + roleName + " Cannot be deleted.");
            }
        } catch (UserStoreException e) {
            throw new IdentityRoleManagementServerException(RoleConstants.Error.UNEXPECTED_SERVER_ERROR.getCode(),
                    "Error while getting the realmConfiguration.", e);
        }

        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(true)) {
            try {
                try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, DELETE_ROLE_SQL,
                        RoleConstants.RoleTableColumns.UM_ID)) {
                    statement.setString(RoleConstants.RoleTableColumns.UM_ROLE_NAME, roleName);
                    statement.setInt(RoleConstants.RoleTableColumns.UM_TENANT_ID, tenantId);
                    statement.executeUpdate();
                }

                // Delete the role from IDN_SCIM_GROUP table.
                deleteSCIMRole(roleID, roleName, tenantDomain);

                /* UM_ROLE_PERMISSION Table, roles are associated with Domain ID.
                   At this moment Role name doesn't contain the Domain prefix.
                   clearRoleAuthorization() expects domain qualified name.
                   Hence we add the "Internal" Domain name explicitly here. */
                if (!roleName.contains(UserCoreConstants.DOMAIN_SEPARATOR)) {
                    roleName = UserCoreUtil.addDomainToName(roleName, UserCoreConstants.INTERNAL_DOMAIN);
                }
                // Also need to clear role authorization.
                try {
                    userRealm.getAuthorizationManager().clearRoleAuthorization(roleName);
                } catch (UserStoreException e) {
                    throw new IdentityRoleManagementServerException(RoleConstants.Error.UNEXPECTED_SERVER_ERROR
                            .getCode(),
                            "Error while getting the authorizationManager.", e);
                }

                IdentityDatabaseUtil.commitUserDBTransaction(connection);
            } catch (SQLException | IdentityRoleManagementException e) {
                IdentityDatabaseUtil.rollbackUserDBTransaction(connection);
                String message = "Error while deleting the role name: %s in the tenantDomain: %s";
                throw new IdentityRoleManagementServerException(RoleConstants.Error.UNEXPECTED_SERVER_ERROR.getCode(),
                        String.format(message, roleName, tenantDomain), e);
            }
        } catch (SQLException e) {
            String message = "Error while deleting the role name: %s in the tenantDomain: %s";
            throw new IdentityRoleManagementServerException(RoleConstants.Error.UNEXPECTED_SERVER_ERROR.getCode(),
                    String.format(message, roleName, tenantDomain), e);
        }
        clearUserRolesCacheByTenant(tenantId);
    }

    private Set<String> getSystemRoles() {

        // If the system roles are not enabled in the system no need to continue.
        if (!IdentityUtil.isSystemRolesEnabled()) {
            return Collections.emptySet();
        }
        Set<String> systemRoles = new HashSet<>();
        IdentityConfigParser configParser = IdentityConfigParser.getInstance();
        OMElement systemRolesConfig = configParser
                .getConfigElement(IdentityConstants.SystemRoles.SYSTEM_ROLES_CONFIG_ELEMENT);
        if (systemRolesConfig == null) {
            if (log.isDebugEnabled()) {
                log.debug("'" + IdentityConstants.SystemRoles.SYSTEM_ROLES_CONFIG_ELEMENT
                        + "' config cannot be found.");
            }
            return Collections.emptySet();
        }

        Iterator roleIdentifierIterator = systemRolesConfig
                .getChildrenWithLocalName(IdentityConstants.SystemRoles.ROLE_CONFIG_ELEMENT);
        if (roleIdentifierIterator == null) {
            if (log.isDebugEnabled()) {
                log.debug("'" + IdentityConstants.SystemRoles.ROLE_CONFIG_ELEMENT + "' config cannot be found.");
            }
            return Collections.emptySet();
        }

        while (roleIdentifierIterator.hasNext()) {
            OMElement roleIdentifierConfig = (OMElement) roleIdentifierIterator.next();
            String roleName = roleIdentifierConfig.getFirstChildWithName(
                    new QName(IdentityCoreConstants.IDENTITY_DEFAULT_NAMESPACE,
                            IdentityConstants.SystemRoles.ROLE_NAME_CONFIG_ELEMENT)).getText();

            if (StringUtils.isNotBlank(roleName)) {
                systemRoles.add(roleName.trim());
            }
        }
        return systemRoles;
    }

    /**
     * Add role info (role id, permissions, associated apps).
     *
     * @param roleName Role name.
     * @param permissions Permissions.
     * @param audience Audience.
     * @param audienceId Audience ID.
     * @param tenantDomain Tenant Domain.
     * @return Role ID.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    private String addRoleInfo(String roleName, List<Permission> permissions, String audience,
                               String audienceId, String tenantDomain) throws IdentityRoleManagementException {

        String id;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(true)) {
            try {
                id = addRoleID(roleName, tenantDomain, connection);
                addPermissions(id, permissions, tenantDomain, connection);
                if (APPLICATION.equals(audience)) {
                    addAppRoleAssociation(id, audienceId, connection);
                }
                IdentityDatabaseUtil.commitTransaction(connection);
            } catch (IdentityRoleManagementException e) {
                IdentityDatabaseUtil.rollbackTransaction(connection);
                String errorMessage = "Error while adding role info for role : " + roleName;
                throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
            }

        } catch (SQLException e) {
            String errorMessage = "Error while adding role info for role : " + roleName;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
        return id;
    }

    /**
     * Add application role association.
     *
     * @param roleId Role ID.
     * @param appId Application ID.
     * @param connection DB connection.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    private void addAppRoleAssociation(String roleId, String appId, Connection connection)
            throws IdentityRoleManagementException {

        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, ADD_APP_ROLE_ASSOCIATION_SQL)) {
            statement.setString(RoleConstants.RoleTableColumns.ROLE_ID, roleId);
            statement.setString(RoleConstants.RoleTableColumns.APP_ID, appId);
            statement.executeUpdate();
        } catch (SQLException e) {
            String errorMessage = "Error while adding the app role association for the roleId : " + roleId
                    + " appId : " + appId;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
    }

    /**
     * Delete application role association.
     *
     * @param roleId Role ID.
     * @param connection DB connection.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    private void deleteAppRoleAssociation(String roleId, Connection connection)
            throws IdentityRoleManagementException {

        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                DELETE_APP_ROLE_ASSOCIATION_BY_ROLE_ID_SQL)) {
            statement.setString(RoleConstants.RoleTableColumns.ROLE_ID, roleId);
            statement.executeUpdate();
        } catch (SQLException e) {
            String errorMessage = "Error while deleting the app role association for the roleId : " + roleId;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
    }

    /**
     * Get associated applications by role id.
     *
     * @param roleId Role ID.
     * @return List of associated application.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    private List<AssociatedApplication> getAssociatedAppsById(String roleId)
            throws IdentityRoleManagementException {

        List<AssociatedApplication> associatedApplications = new ArrayList<>();
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                    GET_ASSOCIATED_APPS_BY_ROLE_ID_SQL)) {
                statement.setString(RoleConstants.RoleTableColumns.ROLE_ID, roleId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        associatedApplications.add(new AssociatedApplication(resultSet.getString(1),
                                resultSet.getString(2)));
                    }
                }
            }
        } catch (SQLException e) {
            String errorMessage =
                    "Error while retrieving associated app for role id: " + roleId;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
        return associatedApplications;
    }

    /**
     * Add role ID.
     *
     * @param roleName Role ID.
     * @param tenantDomain Tenant Domain.
     * @return Role Id.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    private String addRoleID(String roleName, String tenantDomain) throws IdentityRoleManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        String id = UUID.randomUUID().toString();
        // Append internal domain in order to maintain the backward compatibility.
        roleName = appendInternalDomain(roleName);
        if (log.isDebugEnabled()) {
            log.debug("Adding the roleID: " + id + " for the role: " + roleName + " in the tenantDomain: "
                    + tenantDomain);
        }

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(true)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, ADD_SCIM_ROLE_ID_SQL)) {
                statement.setInt(RoleConstants.RoleTableColumns.TENANT_ID, tenantId);
                statement.setString(RoleConstants.RoleTableColumns.ROLE_NAME, roleName);
                statement.setString(RoleConstants.RoleTableColumns.ATTR_NAME, RoleConstants.ID_URI);
                statement.setString(RoleConstants.RoleTableColumns.ATTR_VALUE, id);
                statement.executeUpdate();

                IdentityDatabaseUtil.commitTransaction(connection);
            } catch (SQLException e) {
                IdentityDatabaseUtil.rollbackTransaction(connection);
                String errorMessage = "Error while adding the the roleID: %s for the role: %s in the tenantDomain: %s";
                throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                        String.format(errorMessage, id, roleName, tenantDomain), e);
            }
        } catch (SQLException e) {
            String errorMessage = "Error while adding the the roleID: %s for the role: %s in the tenantDomain: %s";
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                    String.format(errorMessage, id, roleName, tenantDomain), e);
        }
        return id;
    }

    /**
     * Add role ID.
     *
     * @param roleName Role ID.
     * @param tenantDomain Tenant Domain.
     * @param connection DB connection.
     * @return Role Id.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    private String addRoleID(String roleName, String tenantDomain, Connection connection)
            throws IdentityRoleManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        String id = UUID.randomUUID().toString();
        // Append internal domain in order to maintain the backward compatibility.
        roleName = appendInternalDomain(roleName);
        if (log.isDebugEnabled()) {
            log.debug("Adding the roleID: " + id + " for the role: " + roleName + " in the tenantDomain: "
                    + tenantDomain);
        }
        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, ADD_SCIM_ROLE_ID_SQL)) {
            statement.setInt(RoleConstants.RoleTableColumns.TENANT_ID, tenantId);
            statement.setString(RoleConstants.RoleTableColumns.ROLE_NAME, roleName);
            statement.setString(RoleConstants.RoleTableColumns.ATTR_NAME, RoleConstants.ID_URI);
            statement.setString(RoleConstants.RoleTableColumns.ATTR_VALUE, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            String errorMessage = "Error while adding the the roleID: %s for the role: %s in the tenantDomain: %s";
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                    String.format(errorMessage, id, roleName, tenantDomain), e);
        }
        return id;
    }

    /**
     * Add permissions.
     *
     * @param roleId Role ID.
     * @param permissions Permissions.
     * @param tenantDomain Tenant Domain.
     * @param connection DB connection.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    private void addPermissions(String roleId, List<Permission> permissions, String tenantDomain, Connection connection)
            throws IdentityRoleManagementException {

        if (permissions == null || permissions.isEmpty()) {
            return;
        }
        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, ADD_ROLE_SCOPE_SQL)) {
            for (Permission permission : permissions) {
                statement.setString(RoleConstants.RoleTableColumns.ROLE_ID, roleId);
                statement.setString(RoleConstants.RoleTableColumns.SCOPE_NAME, permission.getName());
                statement.setInt(RoleConstants.RoleTableColumns.TENANT_ID,
                        IdentityTenantUtil.getTenantId(tenantDomain));
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            String errorMessage = "Error while adding permissions to roleID : " + roleId;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                    errorMessage, e);
        }
    }

    /**
     * Delete all permissions of the role.
     *
     * @param roleId Role ID.
     * @param connection DB connection.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    private void deleteAllPermissionsOfRole(String roleId, Connection connection)
            throws IdentityRoleManagementException {

        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                DELETE_ROLE_SCOPE_BY_ROLE_ID_SQL)) {
            statement.setString(RoleConstants.RoleTableColumns.ROLE_ID, roleId);
            statement.executeUpdate();
        } catch (SQLException e) {
            String errorMessage = "Error while deleting permissions to roleID : " + roleId;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                    errorMessage, e);
        }

    }

    /**
     * Get permissions by role id.
     *
     * @param roleId Role name.
     * @param tenantDomain Tenant Domain.
     * @return List of permissions.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    private List<Permission> getPermissions(String roleId, String tenantDomain) throws IdentityRoleManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        List<Permission> permissions = new ArrayList<>();
        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(false)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, GET_ROLE_SCOPE_SQL)) {
                statement.setString(RoleConstants.RoleTableColumns.ROLE_ID, roleId);
                statement.setInt(RoleConstants.RoleTableColumns.TENANT_ID, tenantId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        permissions.add(new Permission(resultSet.getString(1),
                                resultSet.getString(2)));
                    }
                }
            }
        } catch (SQLException e) {
            String errorMessage =
                    "Error while retrieving permissions for role id: " + roleId + " and tenantDomain : " + tenantDomain;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
        return permissions;
    }

    /**
     * Get role audience ref id.
     *
     * @param audience Audience.
     * @param audienceId Audience ID.
     * @return audience ref id.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    private int getRoleAudienceId(String audience, String audienceId, Connection connection)
            throws IdentityRoleManagementException {

        int id = -1;
        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, GET_ROLE_AUDIENCE_SQL)) {
                statement.setString(RoleConstants.RoleTableColumns.AUDIENCE, audience);
                statement.setString(RoleConstants.RoleTableColumns.AUDIENCE_ID, audienceId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        id = resultSet.getInt(1);
                    }
                    // Create new audience.
                    if (id == -1) {
                        createRoleAudience(audience, audienceId, connection);
                        return getRoleAudienceId(audience, audienceId, connection);
                    }
                }
        } catch (SQLException e) {
            String errorMessage =
                    "Error while resolving the role audiences for the given audience: " + audience
                            + " and audienceId : " + audienceId;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
        return id;
    }

    /**
     * Create role audience.
     *
     * @param audience Audience.
     * @param audienceId Audience ID.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    private void createRoleAudience(String audience, String audienceId, Connection connection)
            throws IdentityRoleManagementException {

        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, ADD_ROLE_AUDIENCE_SQL)) {
            statement.setString(RoleConstants.RoleTableColumns.AUDIENCE, audience);
            statement.setString(RoleConstants.RoleTableColumns.AUDIENCE_ID, audienceId);
            statement.executeUpdate();

            IdentityDatabaseUtil.commitTransaction(connection);
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            String errorMessage = "Error while adding role audiences for the given audience: " + audience
                    + " and audienceId : " + audienceId;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
    }

    /**
     * Append internal domain if there is no domain appended already.
     *
     * @param roleName Role name.
     * @return Domain appended role name.
     */
    private String appendInternalDomain(String roleName) {

        if (!roleName.contains(UserCoreConstants.DOMAIN_SEPARATOR)) {
            return UserCoreConstants.INTERNAL_DOMAIN + UserCoreConstants.DOMAIN_SEPARATOR + roleName;
        }
        return roleName;
    }

    /**
     * Remove internal domain.
     *
     * @param roleName Role name.
     * @return Domain removed role name.
     */
    private String removeInternalDomain(String roleName) {

        if (UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(IdentityUtil.extractDomainFromName(roleName))) {
            return UserCoreUtil.removeDomainFromName(roleName);
        }
        return roleName;
    }

    @Override
    public boolean isExistingRoleName(String roleName, String tenantDomain) throws IdentityRoleManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        boolean isExist = false;
        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(false)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, IS_ROLE_EXIST_SQL,
                    RoleConstants.RoleTableColumns.UM_ID)) {
                statement.setString(RoleConstants.RoleTableColumns.UM_ROLE_NAME, removeInternalDomain(roleName));
                statement.setInt(RoleConstants.RoleTableColumns.UM_TENANT_ID, tenantId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        isExist = resultSet.getInt(1) > 0;
                    }
                }
            }
        } catch (SQLException e) {
            String errorMessage = "Error while checking is existing role for role name: %s in the tenantDomain: %s";
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                    String.format(errorMessage, roleName, tenantDomain), e);
        }
        if (log.isDebugEnabled()) {
            log.debug("Is roleName: " + roleName + " Exist: " + isExist + " in the tenantDomain: " + tenantDomain);
        }
        return isExist;
    }

    private List<String> getUserNamesByIDs(List<String> userIDs, String tenantDomain)
            throws IdentityRoleManagementException {

        return userIDResolver.getNamesByIDs(userIDs, tenantDomain);
    }

    private Map<String, String> getGroupNamesByIDs(List<String> groupIDs, String tenantDomain)
            throws IdentityRoleManagementException {

        return groupIDResolver.getNamesByIDs(groupIDs, tenantDomain);
    }

    private void processBatchUpdateForUsers(String roleName, List<String> userNamesList, int tenantId,
                                            String primaryDomainName, Connection connection,
                                            String removeUserFromRoleSql) throws SQLException {

        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, removeUserFromRoleSql,
                RoleConstants.RoleTableColumns.UM_ID)) {
            for (String userName : userNamesList) {
                // Add domain if not set.
                userName = UserCoreUtil.addDomainToName(userName, primaryDomainName);
                // Get domain from name.
                String domainName = UserCoreUtil.extractDomainFromName(userName);
                if (domainName != null) {
                    domainName = domainName.toUpperCase(Locale.ENGLISH);
                }
                String nameWithoutDomain = UserCoreUtil.removeDomainFromName(userName);
                statement.setString(RoleConstants.RoleTableColumns.UM_USER_NAME, nameWithoutDomain);
                statement.setString(RoleConstants.RoleTableColumns.UM_ROLE_NAME, roleName);
                statement.setInt(RoleConstants.RoleTableColumns.UM_TENANT_ID, tenantId);
                statement.setString(RoleConstants.RoleTableColumns.UM_DOMAIN_NAME, domainName);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void processBatchUpdateForGroups(String roleName, List<String> groupNamesList, int tenantId,
                                             String primaryDomainName, Connection connection, String sql)
            throws SQLException {

        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, sql,
                RoleConstants.RoleTableColumns.UM_ID)) {
            for (String groupName : groupNamesList) {
                // Add domain if not set.
                groupName = UserCoreUtil.addDomainToName(groupName, primaryDomainName);
                // Get domain from name.
                String domainName = UserCoreUtil.extractDomainFromName(groupName);
                if (domainName != null) {
                    domainName = domainName.toUpperCase(Locale.ENGLISH);
                }
                String nameWithoutDomain = UserCoreUtil.removeDomainFromName(groupName);
                statement.setString(RoleConstants.RoleTableColumns.UM_GROUP_NAME, nameWithoutDomain);
                statement.setString(RoleConstants.RoleTableColumns.UM_ROLE_NAME, roleName);
                statement.setInt(RoleConstants.RoleTableColumns.UM_TENANT_ID, tenantId);
                statement.setString(RoleConstants.RoleTableColumns.UM_DOMAIN_NAME, domainName);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void clearUserRolesCache(String usernameWithDomain, int tenantId) {

        String userStoreDomain = IdentityUtil.extractDomainFromName(usernameWithDomain);
        if (isUserRoleCacheEnabled(tenantId, userStoreDomain)) {
            UserRolesCache.getInstance().clearCacheEntry(getCacheIdentifier(tenantId, userStoreDomain),
                    tenantId, usernameWithDomain);
        }
        AuthorizationCache authorizationCache = AuthorizationCache.getInstance();
        authorizationCache.clearCacheByUser(tenantId, usernameWithDomain);
    }

    private boolean isUserRoleCacheEnabled(int tenantId, String userStoreDomain) {

        return Boolean.parseBoolean(getUserStoreProperty(
                UserCoreConstants.RealmConfig.PROPERTY_ROLES_CACHE_ENABLED, tenantId, userStoreDomain));
    }

    private String getCacheIdentifier(int tenantId, String userStoreDomain) {

        String userCoreCacheIdentifier = getUserStoreProperty(
                UserCoreConstants.RealmConfig.PROPERTY_USER_CORE_CACHE_IDENTIFIER, tenantId, userStoreDomain);

        if (StringUtils.isNotBlank(userCoreCacheIdentifier)) {
            return userCoreCacheIdentifier;
        }

        return UserCoreConstants.DEFAULT_CACHE_IDENTIFIER;
    }

    private String getUserStoreProperty(String property, int tenantId, String userStoreDomain) {

        RealmService realmService = RoleManagementServiceComponentHolder.getInstance().getRealmService();
        String propValue = null;
        if (realmService != null) {
            try {
                if (IdentityUtil.getPrimaryDomainName().equals(userStoreDomain)) {
                    propValue = realmService.getTenantUserRealm(tenantId).getRealmConfiguration()
                            .getUserStoreProperty(property);
                } else {
                    UserStoreManager userStoreManager = realmService.getTenantUserRealm(tenantId).getUserStoreManager();
                    if (userStoreManager instanceof AbstractUserStoreManager) {
                        propValue = ((AbstractUserStoreManager) userStoreManager)
                                .getSecondaryUserStoreManager(userStoreDomain).getRealmConfiguration()
                                .getUserStoreProperty(property);
                    }
                }
            } catch (UserStoreException e) {
                log.error(String.format("Error while retrieving property %s for userstore %s in tenantId %s. " +
                        "Returning null.", property, userStoreDomain, tenantId), e);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug(String.format("Userstore property %s is set to %s for userstore %s in tenantId %s",
                    property, propValue, userStoreDomain, tenantId));
        }
        return propValue;
    }

    private String getAudienceName(String audience, String audienceId, String tenantDomain)
            throws IdentityRoleManagementException {

        if (APPLICATION.equals(audience)) {
            return getApplicationName(audienceId, tenantDomain);
        } else if (ORGANIZATION.equals(audience)) {
            return getOrganizationName(audienceId);
        }
        return null;
    }

    private String getApplicationName(String applicationID, String tenantDomain)
            throws IdentityRoleManagementException {

        String name = null;
        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(false)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, GET_APP_NAME_BY_APP_ID)) {
                statement.setString(RoleConstants.RoleTableColumns.APP_ID, applicationID);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        name = resultSet.getString(1);
                    }
                }
            }
        } catch (SQLException e) {
            String errorMessage = "Error while retrieving the application name for the given id: " + applicationID;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
        return name;
    }

    private String getOrganizationIdByTenantDomain(String tenantDomain) throws IdentityRoleManagementServerException {

        try {
            return RoleManagementServiceComponentHolder.getInstance().getOrganizationManager()
                    .resolveOrganizationId(tenantDomain);

        } catch (OrganizationManagementException e) {
            String errorMessage = "Error while retrieving the organization id for the given tenantDomain: "
                    + tenantDomain;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
    }

    private String getOrganizationName(String organizationId) throws IdentityRoleManagementServerException {

        try {
            return RoleManagementServiceComponentHolder.getInstance().getOrganizationManager()
                    .getOrganizationNameById(organizationId);
        } catch (OrganizationManagementException e) {
            String errorMessage = "Error while retrieving the organization name for the given id: " + organizationId;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
    }

    /**
     * Validate limit.
     *
     * @param limit given limit value.
     * @return validated limit value.
     * @throws IdentityRoleManagementClientException IdentityRoleManagementClientException.
     */
    private int validateLimit(Integer limit) throws IdentityRoleManagementClientException {

        int maximumItemsPerPage = IdentityUtil.getMaximumItemPerPage();
        if (limit == null) {
            limit = IdentityUtil.getDefaultItemsPerPage();
            if (log.isDebugEnabled()) {
                log.debug("Given limit is null. Therefore assigning the default limit: " + limit);
            }
        } else if (limit < 0) {
            String errorMessage =
                    "Invalid limit requested. Limit value should be greater than or equal to zero. limit: " + limit;
            throw new IdentityRoleManagementClientException(INVALID_LIMIT.getCode(), errorMessage);
        } else if (limit > maximumItemsPerPage) {
            limit = maximumItemsPerPage;
            if (log.isDebugEnabled()) {
                log.debug("Given limit exceed the maximum limit. Therefore assigning the maximum limit: "
                        + maximumItemsPerPage);
            }
        }
        return limit;
    }

    /**
     * Validate offset.
     *
     * @param offset given offset value.
     * @return validated offset value.
     * @throws IdentityRoleManagementClientException IdentityRoleManagementClientException.
     */
    private int validateOffset(Integer offset) throws IdentityRoleManagementClientException {

        if (offset == null) {
            // Return first page offset.
            offset = 0;
        } else if (offset > 0) {
            /* SCIM 2.0 implementation is using one based startIndex.
            Therefore we are converting it to a zero based start index here. */
            offset = offset - 1;
        } else if (offset < 0) {
            String errorMessage =
                    "Invalid offset requested. Offset value should be zero or greater than zero. offSet: " + offset;
            throw new IdentityRoleManagementClientException(INVALID_OFFSET.getCode(), errorMessage);
        }
        return offset;
    }

    /**
     * Validates the offset and limit values for pagination.
     *
     * @param sortBy    Sort By value.
     * @param sortOrder Sort order value.
     * @throws IdentityRoleManagementClientException IdentityRoleManagementClientException.
     */
    private void validateAttributesForSorting(String sortBy, String sortOrder)
            throws IdentityRoleManagementClientException {

        if (StringUtils.isNotBlank(sortBy) || StringUtils.isNotBlank(sortOrder)) {
            throw new IdentityRoleManagementClientException(SORTING_NOT_IMPLEMENTED.getCode(),
                    "Sorting not supported.");
        }
    }

    private String getDBTypeSpecificRolesRetrievalQuery(String databaseProductName)
            throws IdentityRoleManagementException {

        if (MY_SQL.equals(databaseProductName)
                || MARIADB.equals(databaseProductName)
                || H2.equals(databaseProductName)) {
            return GET_ROLES_BY_TENANT_MYSQL;
        } else if (ORACLE.equals(databaseProductName)) {
            return GET_ROLES_BY_TENANT_ORACLE;
        } else if (MICROSOFT.equals(databaseProductName)) {
            return GET_ROLES_BY_TENANT_MSSQL;
        } else if (POSTGRE_SQL.equals(databaseProductName)) {
            return GET_ROLES_BY_TENANT_POSTGRESQL;
        } else if (databaseProductName != null && databaseProductName.contains(DB2)) {
            return GET_ROLES_BY_TENANT_DB2;
        } else if (INFORMIX.equals(databaseProductName)) {
            return GET_ROLES_BY_TENANT_INFORMIX;
        }

        throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                "Error while listing roles from DB. Database driver for " + databaseProductName
                        + "could not be identified or not supported.");
    }

    private String getDBTypeSpecificRolesRetrievalQueryByRoleName(String databaseProductName)
            throws IdentityRoleManagementException {

        if (RoleConstants.MY_SQL.equals(databaseProductName)
                || RoleConstants.MARIADB.equals(databaseProductName)
                || RoleConstants.H2.equals(databaseProductName)) {
            return GET_ROLES_BY_TENANT_AND_ROLE_NAME_MYSQL;
        } else if (RoleConstants.ORACLE.equals(databaseProductName)) {
            return GET_ROLES_BY_TENANT_AND_ROLE_NAME_ORACLE;
        } else if (RoleConstants.MICROSOFT.equals(databaseProductName)) {
            return GET_ROLES_BY_TENANT_AND_ROLE_NAME_MSSQL;
        } else if (RoleConstants.POSTGRE_SQL.equals(databaseProductName)) {
            return GET_ROLES_BY_TENANT_AND_ROLE_NAME_POSTGRESQL;
        } else if (databaseProductName != null && databaseProductName.contains(RoleConstants.DB2)) {
            return GET_ROLES_BY_TENANT_AND_ROLE_NAME_DB2;
        } else if (RoleConstants.INFORMIX.equals(databaseProductName)) {
            return GET_ROLES_BY_TENANT_AND_ROLE_NAME_INFORMIX;
        }

        throw new IdentityRoleManagementServerException(RoleConstants.Error.UNEXPECTED_SERVER_ERROR.getCode(),
                "Error while listing roles from DB. Database driver for " + databaseProductName
                        + "could not be identified or not supported.");
    }

    private List<RoleBasicInfo> processListRolesQuery(int limit, int offset, NamedPreparedStatement statement,
                                                      String tenantDomain) throws SQLException,
            IdentityRoleManagementException {

        statement.setInt(RoleConstants.OFFSET, offset);
        statement.setInt(RoleConstants.LIMIT, limit);
        statement.setInt(RoleConstants.ZERO_BASED_START_INDEX, offset);
        statement.setInt(RoleConstants.ONE_BASED_START_INDEX, offset + 1);
        statement.setInt(RoleConstants.END_INDEX, offset + limit);
        return buildRolesList(statement, tenantDomain);
    }

    private List<RoleBasicInfo> buildRolesList(NamedPreparedStatement statement, String tenantDomain)
            throws SQLException, IdentityRoleManagementException {

        List<RoleBasicInfo> roles = new ArrayList<>();
        List<String> roleNames = new ArrayList<>();
        Map<String, RoleAudience> roleNamesToAudience = new HashMap<>();
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                String roleName = resultSet.getString(1);
                roleNames.add(appendInternalDomain(roleName));
                roleNamesToAudience.put(appendInternalDomain(roleName),
                        new RoleAudience(resultSet.getString(2), resultSet.getString(3)));
            }
        }
        Map<String, String> roleNamesToIDs = getRoleIDsByNames(roleNames, tenantDomain);

        // Filter scim disabled roles.
        roleNames.removeAll(new ArrayList<>(roleNamesToIDs.keySet()));
        // Add roleIDs for scim disabled roles.
        for (String roleName : roleNames) {
            roleNamesToIDs.put(roleName, addRoleID(roleName, tenantDomain));
        }

        for (Map.Entry<String, String> entry : roleNamesToIDs.entrySet()) {
            String roleName = entry.getKey();
            String roleID = entry.getValue();
            RoleBasicInfo roleBasicInfo = new RoleBasicInfo(roleID, removeInternalDomain(roleName));
            RoleAudience roleAudience = roleNamesToAudience.get(roleName);
            if (roleAudience != null) {
                roleBasicInfo.setAudience(roleAudience.getAudience());
                roleBasicInfo.setAudienceId(roleAudience.getAudienceId());
                roleBasicInfo.setAudienceName(getAudienceName(roleAudience.getAudience(),
                        roleAudience.getAudienceId(), tenantDomain));
            }
            roles.add(roleBasicInfo);
        }
        return roles;
    }

    private Map<String, String> getRoleIDsByNames(List<String> roleNames, String tenantDomain)
            throws IdentityRoleManagementException {

        Map<String, String> roleNamesToIDs;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            roleNamesToIDs = batchProcessRoleNames(roleNames, tenantDomain, connection);
        } catch (SQLException e) {
            String errorMessage =
                    "Error while resolving the role ID for the given group names in the tenantDomain: " + tenantDomain;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
        return roleNamesToIDs;
    }

    private Map<String, String> batchProcessRoleNames(List<String> roleNames, String tenantDomain,
                                                      Connection connection) throws SQLException,
            IdentityRoleManagementException {

        Map<String, String> roleNamesToIDs = new HashMap<>();
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        String roleID;
        for (String roleName : roleNames) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, GET_ROLE_ID_BY_NAME_SQL)) {
                statement.setInt(RoleConstants.RoleTableColumns.TENANT_ID, tenantId);
                statement.setString(RoleConstants.RoleTableColumns.ROLE_NAME, roleName);
                statement.setString(RoleConstants.RoleTableColumns.ATTR_NAME, RoleConstants.ID_URI);
                int count = 0;
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        // Handle multiple matching roles.
                        count++;
                        if (count > 1) {
                            String errorMessage =
                                    "Invalid scenario. Multiple roles found for the given role name: " + roleName
                                            + " and tenantDomain: " + tenantDomain;
                            throw new IdentityRoleManagementClientException(INVALID_REQUEST.getCode(), errorMessage);
                        }
                        roleID = resultSet.getString(1);
                        roleNamesToIDs.put(roleName, roleID);
                    }
                }
            }
        }
        return roleNamesToIDs;
    }

    private RoleAudience getAudienceIdByRoleName(String roleName, String tenantDomain)
            throws IdentityRoleManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        RoleAudience roleAudience;
        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(false)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                    GET_ROLE_AUDIENCE_BY_ROLE_NAME_SQL)) {
                statement.setString(RoleConstants.RoleTableColumns.ROLE_NAME, roleName);
                statement.setInt(RoleConstants.RoleTableColumns.TENANT_ID, tenantId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        roleAudience = new RoleAudience(resultSet.getString(1),
                                resultSet.getString(2));
                        roleAudience.setAudienceName(getAudienceName(roleAudience.getAudience(),
                                roleAudience.getAudienceId(), tenantDomain));
                        return roleAudience;
                    }
                }
            }
        } catch (SQLException e) {
            String errorMessage =
                    "Error while resolving the role audience for the given role name: " + roleName;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
        return null;
    }

    private String resolveSQLFilter(String filter) {

        // To avoid any issues when the filter string is blank or null, assigning "%" to SQLFilter.
        String sqlfilter = "%";
        if (StringUtils.isNotBlank(filter)) {
            sqlfilter = filter.trim().replace(RoleConstants.WILDCARD_CHARACTER, "%")
                    .replace("?", "_");
        }
        if (log.isDebugEnabled()) {
            log.debug("Input filter: " + filter + " resolved for SQL filter: " + sqlfilter);
        }
        return sqlfilter;
    }

    private String getRoleNameByID(String roleID, String tenantDomain) throws IdentityRoleManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        String roleName = null;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, GET_ROLE_NAME_BY_ID_SQL)) {
                statement.setInt(RoleConstants.RoleTableColumns.TENANT_ID, tenantId);
                statement.setString(RoleConstants.RoleTableColumns.ATTR_NAME, RoleConstants.ID_URI);
                statement.setString(RoleConstants.RoleTableColumns.ATTR_VALUE, roleID);
                int count = 0;
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        // Handle multiple matching roles.
                        count++;
                        if (count > 1) {
                            String message = "Invalid scenario. Multiple roles found for the given role ID: " + roleID
                                    + " and tenantDomain: " + tenantDomain;
                            log.warn(message);
                        }
                        roleName = resultSet.getString(1);
                    }
                }
            }
        } catch (SQLException e) {
            String errorMessage =
                    "Error while resolving the role name for the given role ID: " + roleID + " and tenantDomain: "
                            + tenantDomain;
            throw new IdentityRoleManagementServerException(RoleConstants.Error.UNEXPECTED_SERVER_ERROR.getCode(),
                    errorMessage, e);
        }
        // Verify whether the roleName is either null or it's not contain any prefix Application/Internal
        if (roleName == null || !RoleManagementUtils.isHybridRole(roleName)) {
            String errorMessage = "A role doesn't exist with id: " + roleID + " in the tenantDomain: " + tenantDomain;
            throw new IdentityRoleManagementClientException(ROLE_NOT_FOUND.getCode(), errorMessage);
        }
        return removeInternalDomain(roleName);
    }

    private List<UserBasicInfo> getUserListOfRole(String roleID, String tenantDomain)
            throws IdentityRoleManagementException {

        if (!isExistingRoleID(roleID, tenantDomain)) {
            throw new IdentityRoleManagementClientException(ROLE_NOT_FOUND.getCode(),
                    "Role id: " + roleID + " does not exist in the system.");
        }
        List<UserBasicInfo> userList = new ArrayList<>();
        String roleName = getRoleNameByID(roleID, tenantDomain);
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        try {
            UserRealm userRealm = CarbonContext.getThreadLocalCarbonContext().getUserRealm();
            if (UserCoreUtil.isEveryoneRole(roleName, userRealm.getRealmConfiguration())) {
                List<org.wso2.carbon.user.core.common.User> users = ((AbstractUserStoreManager) userRealm
                        .getUserStoreManager()).listUsersWithID(RoleConstants.WILDCARD_CHARACTER, -1);
                for (org.wso2.carbon.user.core.common.User user : users) {
                    userList.add(new UserBasicInfo(user.getUserID(), user.getDomainQualifiedUsername()));
                }
            }
        } catch (UserStoreException e) {
            throw new IdentityRoleManagementServerException(RoleConstants.Error.UNEXPECTED_SERVER_ERROR.getCode(),
                    "Error while getting the realmConfiguration.", e);
        }

        List<String> disabledDomainName = getDisabledDomainNames();

        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(false)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, GET_USER_LIST_OF_ROLE_SQL,
                    RoleConstants.RoleTableColumns.UM_ID)) {
                statement.setString(RoleConstants.RoleTableColumns.UM_ROLE_NAME, roleName);
                statement.setInt(RoleConstants.RoleTableColumns.UM_TENANT_ID, tenantId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        String name = resultSet.getString(1);
                        String domain = resultSet.getString(2);
                        if (!disabledDomainName.contains(domain)) {
                            if (StringUtils.isNotEmpty(domain)) {
                                name = UserCoreUtil.addDomainToName(name, domain);
                            }
                            String userId;
                            try {
                                userId = getUserIDByName(name, tenantDomain);
                            } catch (IdentityRoleManagementClientException roleManagementClientException) {
                                String errorMessage = String.format(USER_NOT_FOUND_ERROR_MESSAGE, name, tenantDomain);
                                if (roleManagementClientException.getMessage().equals(errorMessage)) {
                                    if (log.isDebugEnabled()) {
                                        log.debug(errorMessage);
                                    }
                                    continue;
                                } else {
                                    throw roleManagementClientException;
                                }
                            }
                            userList.add(new UserBasicInfo(userId, name));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            String errorMessage =
                    "Error while while getting the user list of role for role name: %s in the " + "tenantDomain: %s";
            throw new IdentityRoleManagementServerException(RoleConstants.Error.UNEXPECTED_SERVER_ERROR.getCode(),
                    String.format(errorMessage, roleName, tenantDomain), e);
        }
        return userList;
    }

    private boolean isExistingRoleID(String roleID, String tenantDomain) throws IdentityRoleManagementException {

        boolean isExist = false;
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, IS_ROLE_ID_EXIST_SQL)) {
                statement.setInt(RoleConstants.RoleTableColumns.TENANT_ID, tenantId);
                statement.setString(RoleConstants.RoleTableColumns.ATTR_NAME, RoleConstants.ID_URI);
                statement.setString(RoleConstants.RoleTableColumns.ATTR_VALUE, roleID);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        isExist = resultSet.getInt(1) > 0;
                    }
                }
            }
        } catch (SQLException e) {
            String errorMessage = "Error while checking is existing role for role id: %s in the tenantDomain: %s";
            throw new IdentityRoleManagementServerException(RoleConstants.Error.UNEXPECTED_SERVER_ERROR.getCode(),
                    String.format(errorMessage, roleID, tenantDomain), e);
        }
        if (log.isDebugEnabled()) {
            log.debug("Is roleID: " + roleID + " Exist: " + isExist + " in the tenantDomain: " + tenantDomain);
        }
        return isExist;
    }

    /**
     * Get the disabled domain names.
     *
     * @return disabled domain names.
     */
    private List<String> getDisabledDomainNames() throws IdentityRoleManagementException {

        RealmConfiguration secondaryRealmConfiguration;
        try {
            if (CarbonContext.getThreadLocalCarbonContext().getUserRealm() == null || (
                    CarbonContext.getThreadLocalCarbonContext().getUserRealm().getRealmConfiguration() == null)) {
                return new ArrayList<>();
            }
            secondaryRealmConfiguration = CarbonContext.getThreadLocalCarbonContext().getUserRealm().
                    getRealmConfiguration().getSecondaryRealmConfig();
        } catch (UserStoreException e) {
            throw new IdentityRoleManagementServerException(RoleConstants.Error.UNEXPECTED_SERVER_ERROR.getCode(),
                    "Error while retrieving user store configurations", e);
        }
        List<String> disableDomainName = new ArrayList<>();
        if (secondaryRealmConfiguration != null) {
            do {
                if (Boolean.parseBoolean(secondaryRealmConfiguration.getUserStoreProperty(RoleConstants.DISABLED))) {
                    String domainName = secondaryRealmConfiguration
                            .getUserStoreProperty(UserStoreConfigConstants.DOMAIN_NAME);
                    disableDomainName.add(domainName.toUpperCase(Locale.ENGLISH));
                }
                secondaryRealmConfiguration = secondaryRealmConfiguration.getSecondaryRealmConfig();
            } while (secondaryRealmConfiguration != null);
        }
        return disableDomainName;
    }

    private String getUserIDByName(String name, String tenantDomain) throws IdentityRoleManagementException {

        return userIDResolver.getIDByName(name, tenantDomain);
    }

    private List<GroupBasicInfo> getGroupListOfRole(String roleID, String tenantDomain)
            throws IdentityRoleManagementException {

        if (!isExistingRoleID(roleID, tenantDomain)) {
            throw new IdentityRoleManagementClientException(ROLE_NOT_FOUND.getCode(),
                    "Role id: " + roleID + " does not exist in the system.");
        }
        String roleName = getRoleNameByID(roleID, tenantDomain);
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        List<GroupBasicInfo> groupList = new ArrayList<>();
        List<String> groupNames = new ArrayList<>();
        List<String> disabledDomainName = getDisabledDomainNames();

        String primaryDomainName = IdentityUtil.getPrimaryDomainName();
        if (primaryDomainName != null) {
            primaryDomainName = primaryDomainName.toUpperCase(Locale.ENGLISH);
        }

        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(false)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                    GET_GROUP_LIST_OF_ROLE_SQL, RoleConstants.RoleTableColumns.UM_ID)) {
                statement.setString(RoleConstants.RoleTableColumns.UM_ROLE_NAME, roleName);
                statement.setInt(RoleConstants.RoleTableColumns.UM_TENANT_ID, tenantId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        String name = resultSet.getString(1);
                        String domain = resultSet.getString(2);
                        if (!disabledDomainName.contains(domain)) {
                            if (!StringUtils.equals(primaryDomainName, domain)) {
                                name = UserCoreUtil.addDomainToName(name, domain);
                            } else {
                                name = primaryDomainName + UserCoreConstants.DOMAIN_SEPARATOR + name;
                            }
                            groupNames.add(name);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            String errorMessage =
                    "Error while while getting the group list of role for role name: %s in the " + "tenantDomain: %s";
            throw new IdentityRoleManagementServerException(RoleConstants.Error.UNEXPECTED_SERVER_ERROR.getCode(),
                    String.format(errorMessage, roleName, tenantDomain), e);
        }
        Map<String, String> groupNamesToIDs = getGroupIDsByNames(groupNames, tenantDomain);
        groupNamesToIDs.forEach((groupName, groupID) -> groupList.add(new GroupBasicInfo(groupID, groupName)));
        return groupList;
    }

    private Map<String, String> getGroupIDsByNames(List<String> names, String tenantDomain)
            throws IdentityRoleManagementException {

        return groupIDResolver.getIDsByNames(names, tenantDomain);
    }

    private void deleteSCIMRole(String roleId, String roleName, String tenantDomain)
            throws IdentityRoleManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        // Append internal domain in order to maintain the backward compatibility.
        roleName = appendInternalDomain(roleName);
        if (log.isDebugEnabled()) {
            log.debug("Deleting the role: " + roleName + " for the role: " + roleName + " in the tenantDomain: "
                    + tenantDomain);
        }

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(true)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, DELETE_SCIM_ROLE_SQL)) {
                statement.setInt(RoleConstants.RoleTableColumns.TENANT_ID, tenantId);
                statement.setString(RoleConstants.RoleTableColumns.ROLE_NAME, roleName);
                statement.executeUpdate();

                deleteRoleAssociations(roleId, connection);

                IdentityDatabaseUtil.commitTransaction(connection);
            } catch (SQLException | IdentityRoleManagementException e) {
                IdentityDatabaseUtil.rollbackTransaction(connection);
                String errorMessage = "Error while deleting the the role: %s for the role: %s in the tenantDomain: %s";
                throw new IdentityRoleManagementServerException(RoleConstants.Error.UNEXPECTED_SERVER_ERROR.getCode(),
                        String.format(errorMessage, roleName, roleName, tenantDomain), e);
            }
        } catch (SQLException e) {
            String errorMessage = "Error while deleting the the role: %s for the role: %s in the tenantDomain: %s";
            throw new IdentityRoleManagementServerException(RoleConstants.Error.UNEXPECTED_SERVER_ERROR.getCode(),
                    String.format(errorMessage, roleName, roleName, tenantDomain), e);
        }
    }

    private void clearUserRolesCacheByTenant(int tenantId) {

        /*
          Ideally we need to check all userstores to see if the UserRolesCache is enabled in at least one userstore
          before removing the cache. This needs to be done by iterating over all userstores of the tenant.
          Since this method is triggered only when a role related configuration is changed it is not worth
          it to iterate over the userstores and check whether UserRolesCache is enabled. Therefore we are simply
          removing the cache so the cache will be removed if its available.
         */
        UserRolesCache.getInstance().clearCacheByTenant(tenantId);

        AuthorizationCache authorizationCache = AuthorizationCache.getInstance();
        authorizationCache.clearCacheByTenant(tenantId);
    }
}
