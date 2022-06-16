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

package org.wso2.carbon.identity.role.mgt.core.dao;

import org.apache.axiom.om.OMElement;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.database.utils.jdbc.NamedPreparedStatement;
import org.wso2.carbon.identity.base.IdentityConstants;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.role.mgt.core.GroupBasicInfo;
import org.wso2.carbon.identity.role.mgt.core.IdentityRoleManagementClientException;
import org.wso2.carbon.identity.role.mgt.core.IdentityRoleManagementException;
import org.wso2.carbon.identity.role.mgt.core.IdentityRoleManagementServerException;
import org.wso2.carbon.identity.role.mgt.core.Role;
import org.wso2.carbon.identity.role.mgt.core.RoleBasicInfo;
import org.wso2.carbon.identity.role.mgt.core.RoleConstants;
import org.wso2.carbon.identity.role.mgt.core.RoleConstants.RoleTableColumns;
import org.wso2.carbon.identity.role.mgt.core.UserBasicInfo;
import org.wso2.carbon.identity.role.mgt.core.internal.RoleManagementServiceComponentHolder;
import org.wso2.carbon.identity.role.mgt.core.util.GroupIDResolver;
import org.wso2.carbon.identity.role.mgt.core.util.UserIDResolver;
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
import org.wso2.carbon.user.mgt.UserRealmProxy;
import org.wso2.carbon.user.mgt.common.UIPermissionNode;
import org.wso2.carbon.user.mgt.common.UserAdminException;

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

import static org.wso2.carbon.identity.role.mgt.core.RoleConstants.DB2;
import static org.wso2.carbon.identity.role.mgt.core.RoleConstants.Error.INVALID_LIMIT;
import static org.wso2.carbon.identity.role.mgt.core.RoleConstants.Error.INVALID_OFFSET;
import static org.wso2.carbon.identity.role.mgt.core.RoleConstants.Error.INVALID_REQUEST;
import static org.wso2.carbon.identity.role.mgt.core.RoleConstants.Error.OPERATION_FORBIDDEN;
import static org.wso2.carbon.identity.role.mgt.core.RoleConstants.Error.ROLE_ALREADY_EXISTS;
import static org.wso2.carbon.identity.role.mgt.core.RoleConstants.Error.ROLE_NOT_FOUND;
import static org.wso2.carbon.identity.role.mgt.core.RoleConstants.Error.SORTING_NOT_IMPLEMENTED;
import static org.wso2.carbon.identity.role.mgt.core.RoleConstants.Error.UNEXPECTED_SERVER_ERROR;
import static org.wso2.carbon.identity.role.mgt.core.RoleConstants.H2;
import static org.wso2.carbon.identity.role.mgt.core.RoleConstants.INFORMIX;
import static org.wso2.carbon.identity.role.mgt.core.RoleConstants.MARIADB;
import static org.wso2.carbon.identity.role.mgt.core.RoleConstants.MICROSOFT;
import static org.wso2.carbon.identity.role.mgt.core.RoleConstants.MY_SQL;
import static org.wso2.carbon.identity.role.mgt.core.RoleConstants.ORACLE;
import static org.wso2.carbon.identity.role.mgt.core.RoleConstants.POSTGRE_SQL;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.ADD_GROUP_TO_ROLE_SQL;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.ADD_GROUP_TO_ROLE_SQL_MSSQL;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.ADD_ROLE_SQL;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.ADD_SCIM_ROLE_ID_SQL;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.ADD_USER_TO_ROLE_SQL;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.ADD_USER_TO_ROLE_SQL_MSSQL;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.COUNT_ROLES_BY_TENANT_DB2;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.COUNT_ROLES_BY_TENANT_INFORMIX;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.COUNT_ROLES_BY_TENANT_MSSQL;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.COUNT_ROLES_BY_TENANT_MYSQL;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.COUNT_ROLES_BY_TENANT_ORACLE;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.COUNT_ROLES_BY_TENANT_POSTGRESQL;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.DELETE_GROUP_SQL;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.DELETE_ROLE_SQL;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.DELETE_SCIM_ROLE_SQL;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.DELETE_USER_SQL;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.GET_GROUP_LIST_OF_ROLE_SQL;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.GET_ROLES_BY_TENANT_AND_ROLE_NAME_DB2;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.GET_ROLES_BY_TENANT_AND_ROLE_NAME_INFORMIX;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.GET_ROLES_BY_TENANT_AND_ROLE_NAME_MSSQL;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.GET_ROLES_BY_TENANT_AND_ROLE_NAME_MYSQL;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.GET_ROLES_BY_TENANT_AND_ROLE_NAME_ORACLE;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.GET_ROLES_BY_TENANT_AND_ROLE_NAME_POSTGRESQL;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.GET_ROLES_BY_TENANT_DB2;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.GET_ROLES_BY_TENANT_INFORMIX;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.GET_ROLES_BY_TENANT_MSSQL;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.GET_ROLES_BY_TENANT_MYSQL;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.GET_ROLES_BY_TENANT_ORACLE;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.GET_ROLES_BY_TENANT_POSTGRESQL;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.GET_ROLE_ID_BY_NAME_SQL;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.GET_ROLE_NAME_BY_ID_SQL;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.GET_USER_LIST_OF_ROLE_SQL;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.IS_ROLE_EXIST_SQL;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.IS_ROLE_ID_EXIST_SQL;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.REMOVE_GROUP_FROM_ROLE_SQL;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.REMOVE_USER_FROM_ROLE_SQL;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.UPDATE_ROLE_NAME_SQL;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.UPDATE_SCIM_ROLE_NAME_SQL;

/**
 * Implementation of the {@link RoleDAO} interface.
 */
public class RoleDAOImpl implements RoleDAO {

    private Log log = LogFactory.getLog(RoleDAOImpl.class);
    private GroupIDResolver groupIDResolver = new GroupIDResolver();
    private UserIDResolver userIDResolver = new UserIDResolver();
    private Set<String> systemRoles = getSystemRoles();

    @Override
    public RoleBasicInfo addRole(String roleName, List<String> userList, List<String> groupList,
                                 List<String> permissions, String tenantDomain) throws IdentityRoleManagementException {

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

        if (!isExistingRoleName(roleName, tenantDomain)) {
            try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(true)) {
                try {
                    try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, ADD_ROLE_SQL,
                            RoleTableColumns.UM_ID)) {
                        statement.setString(RoleTableColumns.UM_ROLE_NAME, roleName);
                        statement.setInt(RoleTableColumns.UM_TENANT_ID, tenantId);
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

                    // Add role ID.
                    roleID = addRoleID(roleName, tenantDomain);
                    // Add role permissions.
                    if (CollectionUtils.isNotEmpty(permissions)) {
                        setPermissions(roleID, permissions, tenantDomain, roleName);
                    }

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

    protected String addRoleID(String roleName, String tenantDomain) throws IdentityRoleManagementException {

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
                statement.setInt(RoleTableColumns.TENANT_ID, tenantId);
                statement.setString(RoleTableColumns.ROLE_NAME, roleName);
                statement.setString(RoleTableColumns.ATTR_NAME, RoleConstants.ID_URI);
                statement.setString(RoleTableColumns.ATTR_VALUE, id);
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

    private String removeInternalDomain(String roleName) {

        if (UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(IdentityUtil.extractDomainFromName(roleName))) {
            return UserCoreUtil.removeDomainFromName(roleName);
        }
        return roleName;
    }

    @Override
    public Role getRole(String roleID, String tenantDomain) throws IdentityRoleManagementException {

        Role role = new Role();
        String roleName = getRoleNameByID(roleID, tenantDomain);
        role.setId(roleID);
        role.setName(roleName);
        role.setTenantDomain(tenantDomain);
        role.setUsers(getUserListOfRole(roleID, tenantDomain));
        role.setGroups(getGroupListOfRole(roleID, tenantDomain));
        role.setPermissions(getPermissionListOfRole(roleID, tenantDomain));

        return role;
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
                    getDBTypeSpecificRolesRetrievalQuery(databaseProductName), RoleTableColumns.UM_ID)) {
                statement.setInt(RoleTableColumns.UM_TENANT_ID, tenantId);
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
                    getDBTypeSpecificRolesRetrievalQueryByRoleName(databaseProductName), RoleTableColumns.UM_ID)) {
                statement.setInt(RoleTableColumns.UM_TENANT_ID, tenantId);
                statement.setString(RoleTableColumns.UM_ROLE_NAME, filterResolvedForSQL);
                roles = processListRolesQuery(limit, offset, statement, tenantDomain);
            }
        } catch (SQLException e) {
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                    "Error while listing roles in tenantDomain: " + tenantDomain, e);
        }
        return Collections.unmodifiableList(roles);
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
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                String roleName = resultSet.getString(1);
                roleNames.add(appendInternalDomain(roleName));
            }
        }
        Map<String, String> roleNamesToIDs = getRoleIDsByNames(roleNames, tenantDomain);

        // Filter scim disabled roles.
        roleNames.removeAll(new ArrayList<>(roleNamesToIDs.keySet()));
        // Add roleIDs for scim disabled roles.
        for (String roleName : roleNames) {
            roleNamesToIDs.put(roleName, addRoleID(roleName, tenantDomain));
        }

        roleNamesToIDs
                .forEach((roleName, roleID) -> roles.add(new RoleBasicInfo(roleID, removeInternalDomain(roleName))));
        return roles;
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

    private String resolveSQLFilter(String filter) {

        // To avoid any issues when the filter string is blank or null, assigning "%" to SQLFilter.
        String sqlfilter = "%";
        if (StringUtils.isNotBlank(filter)) {
            sqlfilter = filter.trim().replace(RoleConstants.WILDCARD_CHARACTER, "%").replace("?", "_");
        }
        if (log.isDebugEnabled()) {
            log.debug("Input filter: " + filter + " resolved for SQL filter: " + sqlfilter);
        }
        return sqlfilter;
    }

    private String getDBTypeSpecificRolesRetrievalQueryByRoleName(String databaseProductName)
            throws IdentityRoleManagementException {

        if (MY_SQL.equals(databaseProductName)
                || MARIADB.equals(databaseProductName)
                || H2.equals(databaseProductName)) {
            return GET_ROLES_BY_TENANT_AND_ROLE_NAME_MYSQL;
        } else if (ORACLE.equals(databaseProductName)) {
            return GET_ROLES_BY_TENANT_AND_ROLE_NAME_ORACLE;
        } else if (MICROSOFT.equals(databaseProductName)) {
            return GET_ROLES_BY_TENANT_AND_ROLE_NAME_MSSQL;
        } else if (POSTGRE_SQL.equals(databaseProductName)) {
            return GET_ROLES_BY_TENANT_AND_ROLE_NAME_POSTGRESQL;
        } else if (databaseProductName != null && databaseProductName.contains(DB2)) {
            return GET_ROLES_BY_TENANT_AND_ROLE_NAME_DB2;
        } else if (INFORMIX.equals(databaseProductName)) {
            return GET_ROLES_BY_TENANT_AND_ROLE_NAME_INFORMIX;
        }

        throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                "Error while listing roles from DB. Database driver for " + databaseProductName
                        + "could not be identified or not supported.");
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

    private String getDBTypeSpecificRolesCountQuery(String databaseProductName)
            throws IdentityRoleManagementException {

        if (MY_SQL.equals(databaseProductName)
                || MARIADB.equals(databaseProductName)
                || H2.equals(databaseProductName)) {
            return COUNT_ROLES_BY_TENANT_MYSQL;
        } else if (ORACLE.equals(databaseProductName)) {
            return COUNT_ROLES_BY_TENANT_ORACLE;
        } else if (MICROSOFT.equals(databaseProductName)) {
            return COUNT_ROLES_BY_TENANT_MSSQL;
        } else if (POSTGRE_SQL.equals(databaseProductName)) {
            return COUNT_ROLES_BY_TENANT_POSTGRESQL;
        } else if (databaseProductName != null && databaseProductName.contains(DB2)) {
            return COUNT_ROLES_BY_TENANT_DB2;
        } else if (INFORMIX.equals(databaseProductName)) {
            return COUNT_ROLES_BY_TENANT_INFORMIX;
        }

        throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                "Error while counting roles from DB. Database driver for " + databaseProductName
                        + "could not be identified or not supported.");
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

    @Override
    public RoleBasicInfo updateUserListOfRole(String roleID, List<String> newUserIDList, List<String> deletedUserIDList,
                                              String tenantDomain) throws IdentityRoleManagementException {

        if (!isExistingRoleID(roleID, tenantDomain)) {
            throw new IdentityRoleManagementClientException(ROLE_NOT_FOUND.getCode(),
                    "Role id: " + roleID + " does not exist in the system.");
        }
        String roleName = getRoleNameByID(roleID, tenantDomain);
        if (CollectionUtils.isEmpty(newUserIDList) && CollectionUtils.isEmpty(deletedUserIDList)) {
            if (log.isDebugEnabled()) {
                log.debug("User lists are empty.");
            }
            return new RoleBasicInfo(roleID, roleName);
        }

        String primaryDomainName = IdentityUtil.getPrimaryDomainName();
        if (primaryDomainName != null) {
            primaryDomainName = primaryDomainName.toUpperCase(Locale.ENGLISH);
        }

        List<String> newUserNamesList = getUserNamesByIDs(newUserIDList, tenantDomain);
        List<String> deletedUserNamesList = getUserNamesByIDs(deletedUserIDList, tenantDomain);
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        // Validate the user removal operation based on the default system roles.
        validateUserRemovalFromRole(deletedUserNamesList, roleName, tenantDomain);

        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(true)) {

            try {
                // Add new users to the role.
                String addUsersSQL = ADD_USER_TO_ROLE_SQL;
                String databaseProductName = connection.getMetaData().getDatabaseProductName();
                if (MICROSOFT.equals(databaseProductName)) {
                    addUsersSQL = ADD_USER_TO_ROLE_SQL_MSSQL;
                }
                processBatchUpdateForUsers(roleName, newUserNamesList, tenantId, primaryDomainName, connection,
                        addUsersSQL);

                // Delete existing users from the role.
                processBatchUpdateForUsers(roleName, deletedUserNamesList, tenantId, primaryDomainName, connection,
                        REMOVE_USER_FROM_ROLE_SQL);

                IdentityDatabaseUtil.commitUserDBTransaction(connection);
            } catch (SQLException e) {
                IdentityDatabaseUtil.rollbackUserDBTransaction(connection);
                String errorMessage = "Error while updating users to the role: %s in the tenantDomain: %s";
                throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                        String.format(errorMessage, roleName, tenantDomain), e);
            }
        } catch (SQLException e) {
            String errorMessage = "Error while updating users to the role: %s in the tenantDomain: %s";
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                    String.format(errorMessage, roleName, tenantDomain), e);
        }
        if (CollectionUtils.isNotEmpty(deletedUserNamesList)) {
            for (String username : deletedUserNamesList) {
                clearUserRolesCache(username, tenantId);
            }
        }
        if (CollectionUtils.isNotEmpty(newUserNamesList)) {
            for (String username : newUserNamesList) {
                clearUserRolesCache(username, tenantId);
            }
        }
        return new RoleBasicInfo(roleID, roleName);
    }

    private void validateUserRemovalFromRole(List<String> deletedUserNamesList, String roleName, String tenantDomain)
            throws IdentityRoleManagementException {

        if (!IdentityUtil.isSystemRolesEnabled() || deletedUserNamesList.isEmpty()) {
            return;
        }
        try {
            String username = CarbonContext.getThreadLocalCarbonContext().getUsername();
            UserRealm userRealm = CarbonContext.getThreadLocalCarbonContext().getUserRealm();
            String adminUserName = userRealm.getRealmConfiguration().getAdminUserName();
            org.wso2.carbon.user.core.UserStoreManager userStoreManager =
                    (org.wso2.carbon.user.core.UserStoreManager) userRealm
                    .getUserStoreManager();
            boolean isUseCaseSensitiveUsernameForCacheKeys = IdentityUtil
                    .isUseCaseSensitiveUsernameForCacheKeys(userStoreManager);
            // Only the tenant owner can remove users from Administrator role.
            if (RoleConstants.ADMINISTRATOR.equalsIgnoreCase(roleName)) {
                if ((isUseCaseSensitiveUsernameForCacheKeys && !StringUtils.equals(username, adminUserName)) || (
                        !isUseCaseSensitiveUsernameForCacheKeys && !StringUtils
                                .equalsIgnoreCase(username, adminUserName))) {
                    String errorMessage = "Invalid operation. Only the tenant owner can remove users from the role: %s";
                    throw new IdentityRoleManagementClientException(OPERATION_FORBIDDEN.getCode(),
                            String.format(errorMessage, RoleConstants.ADMINISTRATOR));
                } else {
                    // Tenant owner cannot be removed from Administrator role.
                    if (deletedUserNamesList.contains(adminUserName)) {
                        String errorMessage = "Invalid operation. Tenant owner cannot be removed from the role: %s";
                        throw new IdentityRoleManagementClientException(OPERATION_FORBIDDEN.getCode(),
                                String.format(errorMessage, RoleConstants.ADMINISTRATOR));
                    }
                }
            }
        } catch (UserStoreException e) {
            String errorMessage = "Error while validating user removal from the role: %s in the tenantDomain: %s";
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                    String.format(errorMessage, roleName, tenantDomain), e);
        }
    }

    private void processBatchUpdateForUsers(String roleName, List<String> userNamesList, int tenantId,
                                            String primaryDomainName, Connection connection,
                                            String removeUserFromRoleSql) throws SQLException {

        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, removeUserFromRoleSql,
                RoleTableColumns.UM_ID)) {
            for (String userName : userNamesList) {
                // Add domain if not set.
                userName = UserCoreUtil.addDomainToName(userName, primaryDomainName);
                // Get domain from name.
                String domainName = UserCoreUtil.extractDomainFromName(userName);
                if (domainName != null) {
                    domainName = domainName.toUpperCase(Locale.ENGLISH);
                }
                String nameWithoutDomain = UserCoreUtil.removeDomainFromName(userName);
                statement.setString(RoleTableColumns.UM_USER_NAME, nameWithoutDomain);
                statement.setString(RoleTableColumns.UM_ROLE_NAME, roleName);
                statement.setInt(RoleTableColumns.UM_TENANT_ID, tenantId);
                statement.setString(RoleTableColumns.UM_DOMAIN_NAME, domainName);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    @Override
    public RoleBasicInfo updateGroupListOfRole(String roleID, List<String> newGroupIDList,
                                               List<String> deletedGroupIDList, String tenantDomain)
            throws IdentityRoleManagementException {

        if (!isExistingRoleID(roleID, tenantDomain)) {
            throw new IdentityRoleManagementClientException(ROLE_NOT_FOUND.getCode(),
                    "Role id: " + roleID + " does not exist in the system.");
        }
        String roleName = getRoleNameByID(roleID, tenantDomain);
        // Validate the group removal operation based on the default system roles.
        validateGroupRemovalFromRole(deletedGroupIDList, roleName, tenantDomain);
        if (CollectionUtils.isEmpty(newGroupIDList) && CollectionUtils.isEmpty(deletedGroupIDList)) {
            if (log.isDebugEnabled()) {
                log.debug("Group lists are empty.");
            }
            return new RoleBasicInfo(roleID, roleName);
        }

        String primaryDomainName = IdentityUtil.getPrimaryDomainName();
        if (primaryDomainName != null) {
            primaryDomainName = primaryDomainName.toUpperCase(Locale.ENGLISH);
        }

        // Resolve group names from group IDs.
        Map<String, String> newGroupIdsToNames = getGroupNamesByIDs(newGroupIDList, tenantDomain);
        List<String> newGroupNamesList = new ArrayList<>(newGroupIdsToNames.values());
        Map<String, String> deletedGroupIdsToNames = getGroupNamesByIDs(deletedGroupIDList, tenantDomain);
        List<String> deletedGroupNamesList = new ArrayList<>(deletedGroupIdsToNames.values());
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(true)) {
            try {
                // Add new groups to the role.
                String addGroupsSQL = ADD_GROUP_TO_ROLE_SQL;
                String databaseProductName = connection.getMetaData().getDatabaseProductName();
                if (MICROSOFT.equals(databaseProductName)) {
                    addGroupsSQL = ADD_GROUP_TO_ROLE_SQL_MSSQL;
                }
                processBatchUpdateForGroups(roleName, newGroupNamesList, tenantId, primaryDomainName, connection,
                        addGroupsSQL);

                // Delete existing groups from the role.
                processBatchUpdateForGroups(roleName, deletedGroupNamesList, tenantId, primaryDomainName, connection,
                        REMOVE_GROUP_FROM_ROLE_SQL);

                IdentityDatabaseUtil.commitUserDBTransaction(connection);
            } catch (SQLException e) {
                IdentityDatabaseUtil.rollbackUserDBTransaction(connection);
                String errorMessage = "Error while updating groups to the role: %s in the tenantDomain: %s";
                throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                        String.format(errorMessage, roleName, tenantDomain), e);
            }
        } catch (SQLException e) {
            String errorMessage = "Error while updating groups to the role: %s in the tenantDomain: %s";
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                    String.format(errorMessage, roleName, tenantDomain), e);
        }
        clearUserRolesCacheByTenant(tenantId);
        return new RoleBasicInfo(roleID, roleName);
    }

    private void processBatchUpdateForGroups(String roleName, List<String> groupNamesList, int tenantId,
                                             String primaryDomainName, Connection connection, String sql)
            throws SQLException {

        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, sql,
                RoleTableColumns.UM_ID)) {
            for (String groupName : groupNamesList) {
                // Add domain if not set.
                groupName = UserCoreUtil.addDomainToName(groupName, primaryDomainName);
                // Get domain from name.
                String domainName = UserCoreUtil.extractDomainFromName(groupName);
                if (domainName != null) {
                    domainName = domainName.toUpperCase(Locale.ENGLISH);
                }
                String nameWithoutDomain = UserCoreUtil.removeDomainFromName(groupName);
                statement.setString(RoleTableColumns.UM_GROUP_NAME, nameWithoutDomain);
                statement.setString(RoleTableColumns.UM_ROLE_NAME, roleName);
                statement.setInt(RoleTableColumns.UM_TENANT_ID, tenantId);
                statement.setString(RoleTableColumns.UM_DOMAIN_NAME, domainName);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void validateGroupRemovalFromRole(List<String> deletedGroupIDList, String roleName, String tenantDomain)
            throws IdentityRoleManagementException {

        if (!IdentityUtil.isSystemRolesEnabled() || deletedGroupIDList.isEmpty()) {
            return;
        }
        try {
            String username = CarbonContext.getThreadLocalCarbonContext().getUsername();
            UserRealm userRealm = CarbonContext.getThreadLocalCarbonContext().getUserRealm();
            String adminUserName = userRealm.getRealmConfiguration().getAdminUserName();
            org.wso2.carbon.user.core.UserStoreManager userStoreManager =
                    (org.wso2.carbon.user.core.UserStoreManager) userRealm
                    .getUserStoreManager();
            boolean isUseCaseSensitiveUsernameForCacheKeys = IdentityUtil
                    .isUseCaseSensitiveUsernameForCacheKeys(userStoreManager);
            // Only the tenant owner can remove groups from Administrator role.
            if (RoleConstants.ADMINISTRATOR.equalsIgnoreCase(roleName)) {
                if ((isUseCaseSensitiveUsernameForCacheKeys && !StringUtils.equals(username, adminUserName)) || (
                        !isUseCaseSensitiveUsernameForCacheKeys && !StringUtils
                                .equalsIgnoreCase(username, adminUserName))) {
                    String errorMessage = "Invalid operation. Only the tenant owner can remove groups from the role: "
                            + "%s";
                    throw new IdentityRoleManagementClientException(OPERATION_FORBIDDEN.getCode(),
                            String.format(errorMessage, RoleConstants.ADMINISTRATOR));
                }
            }
        } catch (UserStoreException e) {
            String errorMessage = "Error while validating group removal from the role: %s in the tenantDomain: %s";
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                    String.format(errorMessage, roleName, tenantDomain), e);
        }
    }

    @Override
    public RoleBasicInfo updateRoleName(String roleID, String newRoleName, String tenantDomain)
            throws IdentityRoleManagementException {

        String roleName = getRoleNameByID(roleID, tenantDomain);
        if (systemRoles.contains(roleName)) {
            throw new IdentityRoleManagementClientException(OPERATION_FORBIDDEN.getCode(),
                    "Invalid operation. Role: " + roleName + " Cannot be renamed since it's a read only system role.");
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        if (!isExistingRoleID(roleID, tenantDomain)) {
            throw new IdentityRoleManagementClientException(ROLE_NOT_FOUND.getCode(),
                    "Role id: " + roleID + " does not exist in the system.");
        }
        if (!StringUtils.equalsIgnoreCase(roleName, newRoleName) && isExistingRoleName(newRoleName, tenantDomain)) {
            throw new IdentityRoleManagementClientException(ROLE_ALREADY_EXISTS.getCode(),
                    "Role name: " + newRoleName + " is already there in the system. Please pick another role name.");
        }
        if (log.isDebugEnabled()) {
            log.debug("Updating the roleName: " + roleName + " to :" + newRoleName + " in the tenantDomain: "
                    + tenantDomain);
        }

        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(true)) {

            try {
                try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, UPDATE_ROLE_NAME_SQL,
                        RoleTableColumns.UM_ID)) {
                    statement.setString(RoleTableColumns.NEW_UM_ROLE_NAME, newRoleName);
                    statement.setString(RoleTableColumns.UM_ROLE_NAME, roleName);
                    statement.setInt(RoleTableColumns.UM_TENANT_ID, tenantId);
                    statement.executeUpdate();
                }

                // Update the role name in IDN_SCIM_GROUP table.
                updateSCIMRoleName(roleName, newRoleName, tenantDomain);

                /* UM_ROLE_PERMISSION Table, roles are associated with Domain ID.
                   At this moment Role name doesn't contain the Domain prefix.
                   resetPermissionOnUpdateRole() expects domain qualified name.
                   Hence we add the "Internal" Domain name explicitly here. */
                if (!roleName.contains(UserCoreConstants.DOMAIN_SEPARATOR)) {
                    roleName = UserCoreUtil.addDomainToName(roleName, UserCoreConstants.INTERNAL_DOMAIN);
                }
                if (!newRoleName.contains(UserCoreConstants.DOMAIN_SEPARATOR)) {
                    newRoleName = UserCoreUtil.addDomainToName(newRoleName, UserCoreConstants.INTERNAL_DOMAIN);
                }
                // Reset role authorization.
                try {
                    UserRealm userRealm = CarbonContext.getThreadLocalCarbonContext().getUserRealm();
                    userRealm.getAuthorizationManager().resetPermissionOnUpdateRole(roleName, newRoleName);
                } catch (UserStoreException e) {
                    throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                            "Error while getting the authorizationManager.", e);
                }

                IdentityDatabaseUtil.commitUserDBTransaction(connection);
            } catch (SQLException | IdentityRoleManagementException e) {
                IdentityDatabaseUtil.rollbackUserDBTransaction(connection);
                String message = "Error while updating the role name: %s in the tenantDomain: %s";
                throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                        String.format(message, roleName, tenantDomain), e);
            }
        } catch (SQLException e) {
            String message = "Error while updating the role name: %s in the tenantDomain: %s";
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                    String.format(message, roleName, tenantDomain), e);
        }

        clearUserRolesCacheByTenant(tenantId);
        return new RoleBasicInfo(roleID, newRoleName);
    }

    protected void updateSCIMRoleName(String roleName, String newRoleName, String tenantDomain)
            throws IdentityRoleManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        // Append internal domain in order to maintain the backward compatibility.
        roleName = appendInternalDomain(roleName);
        newRoleName = appendInternalDomain(newRoleName);
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(true)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, UPDATE_SCIM_ROLE_NAME_SQL)) {
                statement.setString(RoleTableColumns.NEW_ROLE_NAME, newRoleName);
                statement.setInt(RoleTableColumns.TENANT_ID, tenantId);
                statement.setString(RoleTableColumns.ROLE_NAME, roleName);
                statement.executeUpdate();

                IdentityDatabaseUtil.commitTransaction(connection);
            } catch (SQLException e) {
                IdentityDatabaseUtil.rollbackTransaction(connection);
                String errorMessage = "Error while updating the the roleName: %s in the tenantDomain: " + "%s";
                throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                        String.format(errorMessage, roleName, tenantDomain), e);
            }
        } catch (SQLException e) {
            String errorMessage = "Error while updating the the roleName: %s in the tenantDomain: " + "%s";
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                    String.format(errorMessage, roleName, tenantDomain), e);
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
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                    "Error while getting the realmConfiguration.", e);
        }

        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(true)) {
            try {
                try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, DELETE_ROLE_SQL,
                        RoleTableColumns.UM_ID)) {
                    statement.setString(RoleTableColumns.UM_ROLE_NAME, roleName);
                    statement.setInt(RoleTableColumns.UM_TENANT_ID, tenantId);
                    statement.executeUpdate();
                }

                // Delete the role from IDN_SCIM_GROUP table.
                deleteSCIMRole(roleName, tenantDomain);

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
                    throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                            "Error while getting the authorizationManager.", e);
                }

                IdentityDatabaseUtil.commitUserDBTransaction(connection);
            } catch (SQLException | IdentityRoleManagementException e) {
                IdentityDatabaseUtil.rollbackUserDBTransaction(connection);
                String message = "Error while deleting the role name: %s in the tenantDomain: %s";
                throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                        String.format(message, roleName, tenantDomain), e);
            }
        } catch (SQLException e) {
            String message = "Error while deleting the role name: %s in the tenantDomain: %s";
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                    String.format(message, roleName, tenantDomain), e);
        }
        clearUserRolesCacheByTenant(tenantId);
    }

    protected void deleteSCIMRole(String roleName, String tenantDomain) throws IdentityRoleManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        // Append internal domain in order to maintain the backward compatibility.
        roleName = appendInternalDomain(roleName);
        if (log.isDebugEnabled()) {
            log.debug("Deleting the role: " + roleName + " for the role: " + roleName + " in the tenantDomain: "
                    + tenantDomain);
        }

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(true)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, DELETE_SCIM_ROLE_SQL)) {
                statement.setInt(RoleTableColumns.TENANT_ID, tenantId);
                statement.setString(RoleTableColumns.ROLE_NAME, roleName);
                statement.executeUpdate();

                IdentityDatabaseUtil.commitTransaction(connection);
            } catch (SQLException e) {
                IdentityDatabaseUtil.rollbackTransaction(connection);
                String errorMessage = "Error while deleting the the role: %s for the role: %s in the tenantDomain: %s";
                throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                        String.format(errorMessage, roleName, roleName, tenantDomain), e);
            }
        } catch (SQLException e) {
            String errorMessage = "Error while deleting the the role: %s for the role: %s in the tenantDomain: %s";
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                    String.format(errorMessage, roleName, roleName, tenantDomain), e);
        }
    }

    @Override
    public boolean isExistingRoleName(String roleName, String tenantDomain) throws IdentityRoleManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        boolean isExist = false;
        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(false)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, IS_ROLE_EXIST_SQL,
                    RoleTableColumns.UM_ID)) {
                statement.setString(RoleTableColumns.UM_ROLE_NAME, removeInternalDomain(roleName));
                statement.setInt(RoleTableColumns.UM_TENANT_ID, tenantId);
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

    @Override
    public boolean isExistingRoleID(String roleID, String tenantDomain) throws IdentityRoleManagementException {

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
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                    String.format(errorMessage, roleID, tenantDomain), e);
        }
        if (log.isDebugEnabled()) {
            log.debug("Is roleID: " + roleID + " Exist: " + isExist + " in the tenantDomain: " + tenantDomain);
        }
        return isExist;
    }

    @Override
    public List<UserBasicInfo> getUserListOfRole(String roleID, String tenantDomain)
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
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                    "Error while getting the realmConfiguration.", e);
        }

        List<String> disabledDomainName = getDisabledDomainNames();

        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(false)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, GET_USER_LIST_OF_ROLE_SQL,
                    RoleTableColumns.UM_ID)) {
                statement.setString(RoleTableColumns.UM_ROLE_NAME, roleName);
                statement.setInt(RoleTableColumns.UM_TENANT_ID, tenantId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        String name = resultSet.getString(1);
                        String domain = resultSet.getString(2);
                        if (!disabledDomainName.contains(domain)) {
                            if (StringUtils.isNotEmpty(domain)) {
                                name = UserCoreUtil.addDomainToName(name, domain);
                            }
                            userList.add(new UserBasicInfo(getUserIDByName(name, tenantDomain), name));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            String errorMessage =
                    "Error while while getting the user list of role for role name: %s in the " + "tenantDomain: %s";
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                    String.format(errorMessage, roleName, tenantDomain), e);
        }
        return userList;
    }

    protected String getUserIDByName(String name, String tenantDomain) throws IdentityRoleManagementException {

        return userIDResolver.getIDByName(name, tenantDomain);
    }

    @Override
    public List<GroupBasicInfo> getGroupListOfRole(String roleID, String tenantDomain)
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
                    GET_GROUP_LIST_OF_ROLE_SQL, RoleTableColumns.UM_ID)) {
                statement.setString(RoleTableColumns.UM_ROLE_NAME, roleName);
                statement.setInt(RoleTableColumns.UM_TENANT_ID, tenantId);
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
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                    String.format(errorMessage, roleName, tenantDomain), e);
        }
        Map<String, String> groupNamesToIDs = getGroupIDsByNames(groupNames, tenantDomain);
        groupNamesToIDs.forEach((groupName, groupID) -> groupList.add(new GroupBasicInfo(groupID, groupName)));
        return groupList;
    }

    protected Map<String, String> getGroupIDsByNames(List<String> names, String tenantDomain)
            throws IdentityRoleManagementException {

        return groupIDResolver.getIDsByNames(names, tenantDomain);
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
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
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

    @Override
    public List<String> getPermissionListOfRole(String roleID, String tenantDomain)
            throws IdentityRoleManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        String roleName = appendInternalDomain(getRoleNameByID(roleID, tenantDomain));
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantDomain(tenantDomain);
            carbonContext.setTenantId(tenantId);
            return getSelectedPermissions(getUserAdminProxy().getRolePermissions(roleName, tenantId));
        } catch (UserAdminException e) {
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                    "An error occurred when retrieving permissions of role : " + roleID, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public RoleBasicInfo setPermissionsForRole(String roleID, List<String> permissions, String tenantDomain)
            throws IdentityRoleManagementException {

        String roleName = getRoleNameByID(roleID, tenantDomain);
        if (systemRoles.contains(roleName)) {
            throw new IdentityRoleManagementClientException(OPERATION_FORBIDDEN.getCode(),
                    "Invalid operation. Permissions cannot be modified in the role: " + roleName
                            + " since it's a read only system role.");
        }
        return setPermissions(roleID, permissions, tenantDomain, roleName);
    }

    private RoleBasicInfo setPermissions(String roleID, List<String> permissions, String tenantDomain, String roleName)
            throws IdentityRoleManagementServerException {

        roleName = appendInternalDomain(roleName);
        /*
        Permission list can be empty in case we want to remove the permissions.
        Therefore validating for NULL will be sufficient.
         */
        if (permissions == null) {
            if (log.isDebugEnabled()) {
                log.debug("Permissions list is null. Therefore not proceeding further.");
            }
            return new RoleBasicInfo(roleID, roleName);
        }
        try {
            getUserAdminProxy().setRoleUIPermission(roleName, permissions.toArray(new String[0]));
            clearUserRolesCacheByTenant(IdentityTenantUtil.getTenantId(tenantDomain));
            return new RoleBasicInfo(roleID, roleName);
        } catch (UserAdminException e) {
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                    "An error occurred when setting permissions for the role: " + roleName, e);
        }
    }

    @Override
    public void deleteUser(String userID, String tenantDomain) throws IdentityRoleManagementException {

        String userName = getUserNameByID(userID, tenantDomain);
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        String primaryDomainName = IdentityUtil.getPrimaryDomainName();
        if (primaryDomainName != null) {
            primaryDomainName = primaryDomainName.toUpperCase(Locale.ENGLISH);
        }
        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(true)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, DELETE_USER_SQL,
                    RoleTableColumns.UM_ID)) {
                // Add domain if not set.
                userName = UserCoreUtil.addDomainToName(userName, primaryDomainName);
                // Get domain from name.
                String domainName = UserCoreUtil.extractDomainFromName(userName);
                if (domainName != null) {
                    domainName = domainName.toUpperCase(Locale.ENGLISH);
                }
                String nameWithoutDomain = UserCoreUtil.removeDomainFromName(userName);
                statement.setString(RoleTableColumns.UM_USER_NAME, nameWithoutDomain);
                statement.setInt(RoleTableColumns.UM_TENANT_ID, tenantId);
                statement.setString(RoleTableColumns.UM_DOMAIN_NAME, domainName);
                statement.executeUpdate();

                IdentityDatabaseUtil.commitUserDBTransaction(connection);
            } catch (SQLException e) {
                IdentityDatabaseUtil.rollbackUserDBTransaction(connection);
                String errorMessage = "Error while removing the user: %s in the tenantDomain: %s";
                throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                        String.format(errorMessage, userName, tenantDomain), e);
            }
        } catch (SQLException e) {
            String errorMessage = "Error while removing the user: %s in the tenantDomain: %s";
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                    String.format(errorMessage, userName, tenantDomain), e);
        }

        clearUserRolesCache(userName, tenantId);
    }

    protected String getUserNameByID(String id, String tenantDomain) throws IdentityRoleManagementException {

        return userIDResolver.getNameByID(id, tenantDomain);
    }

    protected List<String> getUserNamesByIDs(List<String> userIDs, String tenantDomain)
            throws IdentityRoleManagementException {

        return userIDResolver.getNamesByIDs(userIDs, tenantDomain);
    }

    @Override
    public void deleteGroup(String groupID, String tenantDomain) throws IdentityRoleManagementException {

        String groupName = getGroupNameByID(groupID, tenantDomain);
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        String primaryDomainName = IdentityUtil.getPrimaryDomainName();
        if (primaryDomainName != null) {
            primaryDomainName = primaryDomainName.toUpperCase(Locale.ENGLISH);
        }
        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(true)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, DELETE_GROUP_SQL,
                    RoleTableColumns.UM_ID)) {
                // Add domain if not set.
                groupName = UserCoreUtil.addDomainToName(groupName, primaryDomainName);
                // Get domain from name.
                String domainName = UserCoreUtil.extractDomainFromName(groupName);
                if (domainName != null) {
                    domainName = domainName.toUpperCase(Locale.ENGLISH);
                }
                String nameWithoutDomain = UserCoreUtil.removeDomainFromName(groupName);
                statement.setString(RoleTableColumns.UM_GROUP_NAME, nameWithoutDomain);
                statement.setInt(RoleTableColumns.UM_TENANT_ID, tenantId);
                statement.setString(RoleTableColumns.UM_DOMAIN_NAME, domainName);
                statement.executeUpdate();

                IdentityDatabaseUtil.commitUserDBTransaction(connection);
            } catch (SQLException e) {
                IdentityDatabaseUtil.rollbackUserDBTransaction(connection);
                String errorMessage = "Error while removing the group: %s in the tenantDomain: %s";
                throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                        String.format(errorMessage, groupName, tenantDomain), e);
            }
        } catch (SQLException e) {
            String errorMessage = "Error while removing the group: %s in the tenantDomain: %s";
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                    String.format(errorMessage, groupName, tenantDomain), e);
        }
        clearUserRolesCacheByTenant(tenantId);
    }

    protected String getGroupNameByID(String id, String tenantDomain) throws IdentityRoleManagementException {

        return groupIDResolver.getNameByID(id, tenantDomain);
    }

    protected Map<String, String> getGroupNamesByIDs(List<String> groupIDs, String tenantDomain)
            throws IdentityRoleManagementException {

        return groupIDResolver.getNamesByIDs(groupIDs, tenantDomain);
    }

    @Override
    public String getRoleIDByName(String roleName, String tenantDomain) throws IdentityRoleManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        String roleID = null;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
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
                    }
                }
            }
        } catch (SQLException e) {
            String errorMessage =
                    "Error while resolving the role ID for the given role name: " + roleName + " and tenantDomain: "
                            + tenantDomain;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
        if (roleID == null) {
            String errorMessage =
                    "A role doesn't exist with name: " + roleName + " in the tenantDomain: " + tenantDomain;
            throw new IdentityRoleManagementClientException(INVALID_REQUEST.getCode(), errorMessage);
        }
        return roleID;
    }

    protected Map<String, String> getRoleIDsByNames(List<String> roleNames, String tenantDomain)
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

    @Override
    public String getRoleNameByID(String roleID, String tenantDomain) throws IdentityRoleManagementException {

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
                            String message =
                                    "Invalid scenario. Multiple roles found for the given role ID: " + roleID + " and "
                                            + "tenantDomain: " + tenantDomain;
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
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
        if (roleName == null) {
            String errorMessage = "A role doesn't exist with id: " + roleID + " in the tenantDomain: " + tenantDomain;
            throw new IdentityRoleManagementClientException(ROLE_NOT_FOUND.getCode(), errorMessage);
        }
        return removeInternalDomain(roleName);
    }

    @Override
    public Set<String> getSystemRoles() {

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
                log.debug(
                        "'" + IdentityConstants.SystemRoles.SYSTEM_ROLES_CONFIG_ELEMENT + "' config cannot be found.");
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

    @Override
    public int getRolesCount(String tenantDomain) throws IdentityRoleManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(false)) {
            String databaseProductName = connection.getMetaData().getDatabaseProductName();
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                    getDBTypeSpecificRolesCountQuery(databaseProductName))) {
                statement.setInt(RoleTableColumns.UM_TENANT_ID, tenantId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                    "Error while getting total roles count in tenantDomain: " + tenantDomain, e);
        }
        return 0;
    }

    /**
     * Get the UserAdmin service.
     *
     * @return UserRealmProxy of UserAdmin service.
     */
    private UserRealmProxy getUserAdminProxy() {

        UserRealm realm = CarbonContext.getThreadLocalCarbonContext().getUserRealm();
        return new UserRealmProxy((org.wso2.carbon.user.core.UserRealm) realm);
    }

    /**
     * Recursively go through UIPermissionNode, do not go through leaves if root node selected.
     *
     * @param node UIPermissionNode of permissions.
     * @return List of permissions.
     */
    private List<String> getSelectedPermissions(UIPermissionNode node) {

        List<String> permissions = new ArrayList<>();
        if (node.isSelected()) {
            // Assuming all child nodes selected and no traversing further.
            permissions.add(node.getResourcePath());
            if (log.isDebugEnabled()) {
                log.debug("Permission: " + node.getDisplayName() + " and resourcePath: " + node.getResourcePath()
                        + " added to the permission map.");
            }
        } else {
            UIPermissionNode[] childNodes = node.getNodeList();
            if (ArrayUtils.isNotEmpty(childNodes)) {
                for (UIPermissionNode childNode : childNodes) {
                    permissions.addAll(getSelectedPermissions(childNode));
                }
            }
        }
        return permissions;
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
}
