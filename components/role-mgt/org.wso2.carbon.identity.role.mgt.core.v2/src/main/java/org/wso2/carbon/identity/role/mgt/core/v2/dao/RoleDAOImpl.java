package org.wso2.carbon.identity.role.mgt.core.v2.dao;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.database.utils.jdbc.NamedPreparedStatement;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.role.mgt.core.IdentityRoleManagementClientException;
import org.wso2.carbon.identity.role.mgt.core.IdentityRoleManagementException;
import org.wso2.carbon.identity.role.mgt.core.IdentityRoleManagementServerException;
import org.wso2.carbon.identity.role.mgt.core.v2.Role;
import org.wso2.carbon.identity.role.mgt.core.util.GroupIDResolver;
import org.wso2.carbon.identity.role.mgt.core.util.UserIDResolver;
import org.wso2.carbon.identity.role.mgt.core.v2.Permission;
import org.wso2.carbon.identity.role.mgt.core.v2.RoleAudience;
import org.wso2.carbon.identity.role.mgt.core.v2.RoleBasicInfo;
import org.wso2.carbon.identity.role.mgt.core.v2.RoleConstants;
import org.wso2.carbon.identity.role.mgt.core.v2.internal.RoleManagementServiceComponentHolder;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserCoreConstants;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static org.wso2.carbon.identity.role.mgt.core.v2.dao.SQLQueries.GET_ROLES_BY_TENANT_AND_ROLE_NAME_DB2;
import static org.wso2.carbon.identity.role.mgt.core.v2.dao.SQLQueries.GET_ROLES_BY_TENANT_AND_ROLE_NAME_INFORMIX;
import static org.wso2.carbon.identity.role.mgt.core.v2.dao.SQLQueries.GET_ROLES_BY_TENANT_AND_ROLE_NAME_MSSQL;
import static org.wso2.carbon.identity.role.mgt.core.v2.dao.SQLQueries.GET_ROLES_BY_TENANT_AND_ROLE_NAME_MYSQL;
import static org.wso2.carbon.identity.role.mgt.core.v2.dao.SQLQueries.GET_ROLES_BY_TENANT_AND_ROLE_NAME_ORACLE;
import static org.wso2.carbon.identity.role.mgt.core.v2.dao.SQLQueries.GET_ROLES_BY_TENANT_AND_ROLE_NAME_POSTGRESQL;
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
import static org.wso2.carbon.identity.role.mgt.core.v2.RoleConstants.POSTGRE_SQL;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.ADD_GROUP_TO_ROLE_SQL;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.ADD_GROUP_TO_ROLE_SQL_MSSQL;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.ADD_SCIM_ROLE_ID_SQL;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.ADD_USER_TO_ROLE_SQL;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.ADD_USER_TO_ROLE_SQL_MSSQL;
import static org.wso2.carbon.identity.role.mgt.core.v2.dao.SQLQueries.GET_ROLES_BY_TENANT_DB2;
import static org.wso2.carbon.identity.role.mgt.core.v2.dao.SQLQueries.GET_ROLES_BY_TENANT_INFORMIX;
import static org.wso2.carbon.identity.role.mgt.core.v2.dao.SQLQueries.GET_ROLES_BY_TENANT_MSSQL;
import static org.wso2.carbon.identity.role.mgt.core.v2.dao.SQLQueries.GET_ROLES_BY_TENANT_MYSQL;
import static org.wso2.carbon.identity.role.mgt.core.v2.dao.SQLQueries.GET_ROLES_BY_TENANT_ORACLE;
import static org.wso2.carbon.identity.role.mgt.core.v2.dao.SQLQueries.GET_ROLES_BY_TENANT_POSTGRESQL;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.GET_ROLE_ID_BY_NAME_SQL;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.IS_ROLE_EXIST_SQL;
import static org.wso2.carbon.identity.role.mgt.core.v2.RoleConstants.APPLICATION;
import static org.wso2.carbon.identity.role.mgt.core.v2.RoleConstants.ORGANIZATION;
import static org.wso2.carbon.identity.role.mgt.core.v2.dao.SQLQueries.ADD_ROLE_AUDIENCE_SQL;
import static org.wso2.carbon.identity.role.mgt.core.v2.dao.SQLQueries.ADD_ROLE_SCOPE_SQL;
import static org.wso2.carbon.identity.role.mgt.core.v2.dao.SQLQueries.ADD_ROLE_WITH_AUDIENCE_SQL;
import static org.wso2.carbon.identity.role.mgt.core.v2.dao.SQLQueries.GET_APP_NAME_BY_APP_ID;
import static org.wso2.carbon.identity.role.mgt.core.v2.dao.SQLQueries.GET_ROLE_AUDIENCE_BY_ID_SQL;
import static org.wso2.carbon.identity.role.mgt.core.v2.dao.SQLQueries.GET_ROLE_AUDIENCE_SQL;

/**
 * Implementation of the {@link RoleDAO} interface.
 */
public class RoleDAOImpl implements RoleDAO {

    private Log log = LogFactory.getLog(org.wso2.carbon.identity.role.mgt.core.dao.RoleDAOImpl.class);
    private GroupIDResolver groupIDResolver = new GroupIDResolver();
    private UserIDResolver userIDResolver = new UserIDResolver();

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

        String roleAudienceId = getRoleAudienceId(audience, audienceId);

        if (!isExistingRoleName(roleName, tenantDomain)) {
            try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(true)) {
                try {
                    try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                            ADD_ROLE_WITH_AUDIENCE_SQL, RoleConstants.RoleTableColumns.UM_ID)) {
                        statement.setString(RoleConstants.RoleTableColumns.UM_ROLE_NAME, roleName);
                        statement.setInt(RoleConstants.RoleTableColumns.UM_TENANT_ID, tenantId);
                        statement.setString(RoleConstants.RoleTableColumns.AUDIENCE_ID, roleAudienceId);
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
                        addPermissions(roleID, permissions, tenantDomain);
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
        RoleBasicInfo roleBasicInfo =  new RoleBasicInfo(roleID, roleName);
        roleBasicInfo.setAudience(audience);
        roleBasicInfo.setAudienceId(audienceId);
        roleBasicInfo.setAudienceName(getAudienceName(audience, audienceId, tenantDomain));
        return roleBasicInfo;
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
                    getDBTypeSpecificRolesRetrievalQueryByRoleName(databaseProductName), RoleConstants.RoleTableColumns.UM_ID)) {
                statement.setInt(RoleConstants.RoleTableColumns.UM_TENANT_ID, tenantId);
                statement.setString(RoleConstants.RoleTableColumns.UM_ROLE_NAME, filterResolvedForSQL);
                roles = processListRolesQuery(limit, offset, statement, tenantDomain);
            }
        } catch (SQLException e) {
            throw new IdentityRoleManagementServerException(org.wso2.carbon.identity.role.mgt.core.RoleConstants.Error.UNEXPECTED_SERVER_ERROR.getCode(),
                    "Error while listing roles in tenantDomain: " + tenantDomain, e);
        }
        return Collections.unmodifiableList(roles);
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

    private void addPermissions(String roleId, List<Permission> permissions, String tenantDomain)
            throws IdentityRoleManagementException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(true)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, ADD_ROLE_SCOPE_SQL)) {
                for (Permission permission: permissions) {
                    statement.setString(RoleConstants.RoleTableColumns.ROLE_ID, roleId);
                    statement.setString(RoleConstants.RoleTableColumns.SCOPE_NAME, permission.getName());
                    statement.setInt(RoleConstants.RoleTableColumns.TENANT_ID,
                            IdentityTenantUtil.getTenantId(tenantDomain));
                    statement.addBatch();
                }
                statement.executeBatch();
                IdentityDatabaseUtil.commitTransaction(connection);
            } catch (SQLException e) {
                IdentityDatabaseUtil.rollbackTransaction(connection);
                String errorMessage = "Error while adding permissions to roleID : " +  roleId;
                throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                        errorMessage, e);
            }
        } catch (SQLException e) {
            String errorMessage = "Error while adding permissions to roleID : " +  roleId;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                    errorMessage, e);
        }
    }

    private String getRoleAudienceId(String audience, String audienceId) throws IdentityRoleManagementException {

        String id = null;
        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(false)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, GET_ROLE_AUDIENCE_SQL)) {
                statement.setString(RoleConstants.RoleTableColumns.AUDIENCE, audience);
                statement.setString(RoleConstants.RoleTableColumns.AUDIENCE_ID, audienceId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        id = resultSet.getString(1);
                    }
                }
            }
        } catch (SQLException e) {
            String errorMessage =
                    "Error while resolving the role audiences for the given audience: " + audience
                            + " and audienceId : " + audienceId;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
        if (id == null) {
            createRoleAudience(audience, audienceId);
            getRoleAudienceId(audience, audienceId);
        }
        return id;
    }

    private void createRoleAudience(String audience, String audienceId) throws IdentityRoleManagementException {

        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(true)) {
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
        } catch (SQLException e) {
            String errorMessage = "Error while adding role audiences for the given audience: " + audience
                    + " and audienceId : " + audienceId;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
    }

    private String removeInternalDomain(String roleName) {

        if (UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(IdentityUtil.extractDomainFromName(roleName))) {
            return UserCoreUtil.removeDomainFromName(roleName);
        }
        return roleName;
    }

    private boolean isExistingRoleName(String roleName, String tenantDomain) throws IdentityRoleManagementException {

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

    private String getApplicationName(String applicationID, String tenantDomain) throws IdentityRoleManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
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
            String errorMessage =
                    "Error while retrieving the application name for the given id: " + applicationID;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
        return name;
    }

    private String getOrganizationName(String organizationId) throws IdentityRoleManagementServerException {

        try {
            return RoleManagementServiceComponentHolder.getInstance().getOrganizationManager()
                    .getOrganizationNameById(organizationId);

        } catch (OrganizationManagementException e) {
            String errorMessage =
                    "Error while retrieving the organization name for the given id: " + organizationId;
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

        throw new IdentityRoleManagementServerException(org.wso2.carbon.identity.role.mgt.core.RoleConstants.Error.UNEXPECTED_SERVER_ERROR.getCode(),
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
        Map<String, String> roleNamesToAudience = new HashMap<>();
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                String roleName = resultSet.getString(1);
                roleNames.add(appendInternalDomain(roleName));
                roleNamesToAudience.put(appendInternalDomain(roleName), resultSet.getString(2));
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
            RoleAudience roleAudience = getRoleAudienceById(roleNamesToAudience.get(roleName), tenantDomain);
            if (roleAudience != null) {
                roleBasicInfo.setAudience(roleAudience.getAudience());
                roleAudience.setAudienceId(roleAudience.getAudienceId());
                roleAudience.setAudienceName(roleAudience.getAudienceName());
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

        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(false)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                    GET_ROLE_AUDIENCE_BY_ID_SQL)) {
                statement.setString(RoleConstants.RoleTableColumns.AUDIENCE_ID, audienceId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        RoleAudience roleAudience =  new RoleAudience(resultSet.getString(1),
                                resultSet.getString(2));
                        roleAudience.setAudienceName(getAudienceName(roleAudience.getAudience(),
                                roleAudience.getAudienceId(), tenantDomain));
                        return roleAudience;
                    }
                }
            }
        } catch (SQLException e) {
            String errorMessage =
                    "Error while resolving the role audience for the given audience id: " + audienceId;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
        return null;
    }

    private RoleAudience getRoleAudienceById(String audienceId, String tenantDomain)
            throws IdentityRoleManagementException {

            try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(false)) {
                try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                        GET_ROLE_AUDIENCE_BY_ID_SQL)) {
                    statement.setString(RoleConstants.RoleTableColumns.AUDIENCE_ID, audienceId);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        while (resultSet.next()) {
                            RoleAudience roleAudience =  new RoleAudience(resultSet.getString(1),
                                    resultSet.getString(2));
                            roleAudience.setAudienceName(getAudienceName(roleAudience.getAudience(),
                                    roleAudience.getAudienceId(), tenantDomain));
                            return roleAudience;
                        }
                    }
                }
            } catch (SQLException e) {
                String errorMessage =
                        "Error while resolving the role audience for the given audience id: " + audienceId;
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
}
