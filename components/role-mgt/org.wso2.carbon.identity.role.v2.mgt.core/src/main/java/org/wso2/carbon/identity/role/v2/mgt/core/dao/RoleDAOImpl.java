/*
 * Copyright (c) 2023-2024, WSO2 LLC. (http://www.wso2.com).
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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.axiom.om.OMElement;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.database.utils.jdbc.NamedPreparedStatement;
import org.wso2.carbon.identity.application.common.model.IdPGroup;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.base.IdentityConstants;
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.util.OrganizationManagementUtil;
import org.wso2.carbon.identity.role.v2.mgt.core.FilterQueryBuilder;
import org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementClientException;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementException;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementServerException;
import org.wso2.carbon.identity.role.v2.mgt.core.internal.RoleManagementServiceComponentHolder;
import org.wso2.carbon.identity.role.v2.mgt.core.model.AssociatedApplication;
import org.wso2.carbon.identity.role.v2.mgt.core.model.GroupBasicInfo;
import org.wso2.carbon.identity.role.v2.mgt.core.model.IdpGroup;
import org.wso2.carbon.identity.role.v2.mgt.core.model.Permission;
import org.wso2.carbon.identity.role.v2.mgt.core.model.Role;
import org.wso2.carbon.identity.role.v2.mgt.core.model.RoleAudience;
import org.wso2.carbon.identity.role.v2.mgt.core.model.RoleBasicInfo;
import org.wso2.carbon.identity.role.v2.mgt.core.model.RoleDTO;
import org.wso2.carbon.identity.role.v2.mgt.core.model.UserBasicInfo;
import org.wso2.carbon.identity.role.v2.mgt.core.util.GroupIDResolver;
import org.wso2.carbon.identity.role.v2.mgt.core.util.UserIDResolver;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
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
import java.util.AbstractMap;
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
import java.util.stream.Collectors;

import javax.xml.namespace.QName;

import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.
        ErrorMessages.ERROR_CODE_INVALID_ORGANIZATION_ID;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.APPLICATION;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.CONSOLE_ORG_SCOPE_PREFIX;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.CONSOLE_SCOPE_PREFIX;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.DB2;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.Error.INVALID_LIMIT;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.Error.INVALID_OFFSET;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.Error.INVALID_REQUEST;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.Error.OPERATION_FORBIDDEN;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.Error.PERMISSION_ALREADY_ADDED;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.Error.ROLE_ALREADY_EXISTS;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.Error.ROLE_NOT_FOUND;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.Error.SORTING_NOT_IMPLEMENTED;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.Error.UNEXPECTED_SERVER_ERROR;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.H2;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.INFORMIX;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.INTERNAL_DOMAIN;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.INTERNAL_ORG_SCOPE_PREFIX;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.INTERNAL_SCOPE_PREFIX;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.MARIADB;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.MICROSOFT;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.MY_SQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.ORACLE;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.ORGANIZATION;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.POSTGRE_SQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.RoleTableColumns.ROLE_NAME;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.RoleTableColumns.USER_NOT_FOUND_ERROR_MESSAGE;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.SYSTEM;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.ADD_APP_ROLE_ASSOCIATION_SQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.ADD_GROUP_TO_ROLE_SQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.ADD_GROUP_TO_ROLE_SQL_MSSQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.ADD_IDP_GROUPS_SQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.ADD_ROLE_AUDIENCE_SQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.ADD_ROLE_SCOPE_SQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.ADD_ROLE_SQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.ADD_SCIM_ROLE_ID_SQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.ADD_USER_TO_ROLE_SQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.ADD_USER_TO_ROLE_SQL_MSSQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.COUNT_ROLES_BY_TENANT_DB2;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.COUNT_ROLES_BY_TENANT_INFORMIX;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.COUNT_ROLES_BY_TENANT_MSSQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.COUNT_ROLES_BY_TENANT_MYSQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.COUNT_ROLES_BY_TENANT_ORACLE;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.COUNT_ROLES_BY_TENANT_POSTGRESQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.DELETE_APP_ROLE_ASSOCIATION_BY_ROLE_ID_SQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.DELETE_IDP_GROUPS_SQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.DELETE_ROLES_BY_APP_ID_SQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.DELETE_ROLE_SCOPE_BY_ROLE_ID_SQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.DELETE_ROLE_SCOPE_BY_SCOPE_NAME_SQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.DELETE_ROLE_SQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.DELETE_SCIM_ROLE_SQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.DELETE_SHARED_ROLE;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.DELETE_SHARED_SCIM_ROLES_WITH_MAIN_ROLE_SQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.GET_ASSOCIATED_APPS_BY_ROLE_ID_SQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.GET_ASSOCIATED_APP_IDS_BY_ROLE_ID_SQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.GET_AUDIENCE_BY_ID_SQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.GET_AUDIENCE_REF_BY_ID_FROM_UM_HYBRID_ROLE_SQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.GET_GROUP_LIST_OF_ROLE_SQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.GET_IDP_GROUPS_SQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.GET_LIMITED_USER_LIST_OF_ROLE_DB2;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.GET_LIMITED_USER_LIST_OF_ROLE_MSSQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.GET_LIMITED_USER_LIST_OF_ROLE_ORACLE;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.GET_LIMITED_USER_LIST_OF_ROLE_SQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.GET_MAIN_ROLE_TO_SHARED_ROLE_MAPPINGS_BY_SUBORG_SQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.GET_ROLES_BY_APP_ID_SQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.GET_ROLES_BY_TENANT_AND_ROLE_NAME_DB2;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.GET_ROLES_BY_TENANT_AND_ROLE_NAME_INFORMIX;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.GET_ROLES_BY_TENANT_AND_ROLE_NAME_MSSQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.GET_ROLES_BY_TENANT_AND_ROLE_NAME_MYSQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.GET_ROLES_BY_TENANT_AND_ROLE_NAME_ORACLE;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.GET_ROLES_BY_TENANT_AND_ROLE_NAME_POSTGRESQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.GET_ROLES_BY_TENANT_AND_ROLE_NAME_TAIL_DB2;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.GET_ROLES_BY_TENANT_AND_ROLE_NAME_TAIL_INFORMIX;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.GET_ROLES_BY_TENANT_AND_ROLE_NAME_TAIL_MSSQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.GET_ROLES_BY_TENANT_AND_ROLE_NAME_TAIL_MYSQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.GET_ROLES_BY_TENANT_AND_ROLE_NAME_TAIL_ORACLE;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.GET_ROLES_BY_TENANT_AND_ROLE_NAME_TAIL_POSTGRESQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.GET_ROLES_BY_TENANT_DB2;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.GET_ROLES_BY_TENANT_INFORMIX;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.GET_ROLES_BY_TENANT_MSSQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.GET_ROLES_BY_TENANT_MYSQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.GET_ROLES_BY_TENANT_ORACLE;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.GET_ROLES_BY_TENANT_POSTGRESQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.GET_ROLE_AUDIENCE_SQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.GET_ROLE_ID_BY_NAME_AND_AUDIENCE_SQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.GET_ROLE_ID_LIST_OF_GROUP_SQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.GET_ROLE_ID_LIST_OF_IDP_GROUPS_SQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.GET_ROLE_ID_LIST_OF_USER_SQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.GET_ROLE_LIST_OF_GROUP_SQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.GET_ROLE_LIST_OF_IDP_GROUPS_SQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.GET_ROLE_LIST_OF_USER_SQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.GET_ROLE_NAME_BY_ID_SQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.GET_ROLE_SCOPE_NAMES_SQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.GET_ROLE_SCOPE_SQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.GET_ROLE_TENANT_DOMAIN_BY_ID;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.GET_ROLE_UM_ID_BY_UUID;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.GET_SCOPE_BY_ROLES_SQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.GET_SHARED_HYBRID_ROLE_WITH_MAIN_ROLE_SQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.GET_SHARED_ROLES_MAIN_ROLE_IDS_SQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.GET_SHARED_ROLES_SQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.GET_SHARED_ROLE_MAIN_ROLE_ID_SQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.INSERT_MAIN_TO_SHARED_ROLE_RELATIONSHIP;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.IS_ROLE_EXIST_SQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.IS_ROLE_ID_EXIST_FROM_UM_HYBRID_ROLE_SQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.IS_SHARED_ROLE_SQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.REMOVE_GROUP_FROM_ROLE_SQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.REMOVE_USER_FROM_ROLE_SQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.UPDATE_ROLE_NAME_SQL;
import static org.wso2.carbon.identity.role.v2.mgt.core.dao.SQLQueries.UPDATE_SCIM_ROLE_NAME_SQL;

/**
 * Implementation of the {@link RoleDAO} interface.
 */
@SuppressFBWarnings({"MC_OVERRIDABLE_METHOD_CALL_IN_CONSTRUCTOR", "CT_CONSTRUCTOR_THROW"})
public class RoleDAOImpl implements RoleDAO {

    private static final Log LOG = LogFactory.getLog(RoleDAOImpl.class);
    private final GroupIDResolver groupIDResolver = new GroupIDResolver();
    private final UserIDResolver userIDResolver = new UserIDResolver();
    private final Set<String> systemRoles = getSystemRoles();
    private static final String USERS = "users";
    private static final String GROUPS = "groups";
    private static final String PERMISSIONS = "permissions";
    private static final String ASSOCIATED_APPLICATIONS = "associatedApplications";

    @Override
    public RoleBasicInfo addRole(String roleName, List<String> userList, List<String> groupList,
                                 List<Permission> permissions, String audience, String audienceId, String tenantDomain)
            throws IdentityRoleManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating the role: " + roleName + " in the tenantDomain: " + tenantDomain);
        }

        String primaryDomainName = IdentityUtil.getPrimaryDomainName();
        if (primaryDomainName != null) {
            primaryDomainName = primaryDomainName.toUpperCase(Locale.ENGLISH);
        }

        // Remove internal domain before persisting in order to maintain the backward compatibility.
        roleName = removeInternalDomain(roleName);

        List<String> userNamesList = getUserNamesByIDs(userList, tenantDomain);
        Map<String, String> groupIdsToNames = getGroupNamesByIDs(groupList, tenantDomain);
        String roleId = UUID.randomUUID().toString();
        if (!isExistingRoleName(roleName, audience, audienceId, tenantDomain)) {
            try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(true)) {
                int audienceRefId = getRoleAudienceRefId(audience, audienceId, connection);
                try {
                    try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                            ADD_ROLE_SQL, RoleConstants.RoleTableColumns.UM_ID)) {
                        statement.setString(RoleConstants.RoleTableColumns.UM_ROLE_NAME, roleName);
                        statement.setInt(RoleConstants.RoleTableColumns.UM_TENANT_ID, tenantId);
                        statement.setInt(RoleConstants.RoleTableColumns.UM_AUDIENCE_REF_ID, audienceRefId);
                        statement.setString(RoleConstants.RoleTableColumns.UM_UUID, roleId);
                        statement.executeUpdate();
                    }

                    String databaseProductName = connection.getMetaData().getDatabaseProductName();
                    // Add users to the created role.
                    if (CollectionUtils.isNotEmpty(userList)) {
                        String addUsersSQL = ADD_USER_TO_ROLE_SQL;
                        if (MICROSOFT.equals(databaseProductName)) {
                            addUsersSQL = ADD_USER_TO_ROLE_SQL_MSSQL;
                        }
                        processBatchUpdateForUsers(roleName, audienceRefId, userNamesList, tenantId,
                                primaryDomainName, connection, addUsersSQL);

                        for (String username : userNamesList) {
                            clearUserRolesCache(username, tenantId);
                        }
                    }

                    // Add groups to the created role.
                    if (CollectionUtils.isNotEmpty(groupList)) {
                        List<String> groupNamesList = new ArrayList<>(groupIdsToNames.values());
                        String addGroupsSQL = ADD_GROUP_TO_ROLE_SQL;
                        if (MICROSOFT.equals(databaseProductName)) {
                            addGroupsSQL = ADD_GROUP_TO_ROLE_SQL_MSSQL;
                        }
                        processBatchUpdateForGroups(roleName, audienceRefId, groupNamesList, tenantId,
                                primaryDomainName, connection, addGroupsSQL);
                    }

                    // Handle role id, permissions

                    addRoleInfo(roleId, roleName, permissions, audience, audienceId, audienceRefId, tenantDomain);

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
                    "Role already exist for the role name: " + roleName + " audience: " + audience + " audienceId: "
                            + audienceId);
        }
        return new RoleBasicInfo(roleId, roleName);
    }

    @Override
    public List<RoleBasicInfo> getRoles(Integer limit, Integer offset, String sortBy, String sortOrder,
                                        String tenantDomain) throws IdentityRoleManagementException {

        return getRolesBasicInfo(limit, offset, sortBy, sortOrder, tenantDomain);
    }

    private List<RoleBasicInfo> getRolesBasicInfo(Integer limit, Integer offset, String sortBy, String sortOrder,
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
        return roles;
    }

    @Override
    public List<Role> getRoles(Integer limit, Integer offset, String sortBy, String sortOrder, String tenantDomain,
                                 List<String> requiredAttributes) throws IdentityRoleManagementException {

        List<RoleBasicInfo> roleBasicInfoList = getRolesBasicInfo(limit, offset, sortBy, sortOrder, tenantDomain);
        return getRolesRequestedAttributes(roleBasicInfoList, requiredAttributes, tenantDomain);
    }

    @Override
    public List<RoleBasicInfo> getRoles(List<ExpressionNode> expressionNodes, Integer limit, Integer offset,
                                        String sortBy, String sortOrder, String tenantDomain)
            throws IdentityRoleManagementException {

        return getFilteredRolesBasicInfo(expressionNodes, limit, offset, sortBy, sortOrder, tenantDomain);
    }

    private List<RoleBasicInfo> getFilteredRolesBasicInfo(List<ExpressionNode> expressionNodes, Integer limit,
                                                          Integer offset, String sortBy, String sortOrder,
                                                          String tenantDomain) throws IdentityRoleManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        FilterQueryBuilder filterQueryBuilder = new FilterQueryBuilder();
        appendFilterQuery(expressionNodes, filterQueryBuilder);
        Map<String, String> filterAttributeValue = filterQueryBuilder.getFilterAttributeValue();
        limit = validateLimit(limit);
        offset = validateOffset(offset);
        validateAttributesForSorting(sortBy, sortOrder);
        List<RoleBasicInfo> roles;

        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(false)) {
            String databaseProductName = connection.getMetaData().getDatabaseProductName();
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                    getDBTypeSpecificRolesRetrievalQueryByFilter(databaseProductName,
                            filterQueryBuilder.getFilterQuery()),
                    RoleConstants.RoleTableColumns.UM_ID)) {
                statement.setInt(RoleConstants.RoleTableColumns.UM_TENANT_ID, tenantId);
                if (filterAttributeValue != null) {
                    for (Map.Entry<String, String> entry : filterAttributeValue.entrySet()) {
                        statement.setString(entry.getKey(), entry.getValue());
                    }
                }
                roles = processListRolesQuery(limit, offset, statement, tenantDomain);
            }
        } catch (SQLException e) {
            throw new IdentityRoleManagementServerException(RoleConstants.Error.UNEXPECTED_SERVER_ERROR.getCode(),
                    "Error while listing roles in tenantDomain: " + tenantDomain, e);
        }
        return roles;
    }

    @Override
    public List<Role> getRoles(List<ExpressionNode> expressionNodes, Integer limit, Integer offset, String sortBy,
                                 String sortOrder, String tenantDomain, List<String> requiredAttributes)
            throws IdentityRoleManagementException {

        List<RoleBasicInfo> roleBasicInfoList = getFilteredRolesBasicInfo(expressionNodes, limit, offset, sortBy,
                sortOrder, tenantDomain);
        return getRolesRequestedAttributes(roleBasicInfoList, requiredAttributes, tenantDomain);
    }

    private List<Role> getRolesRequestedAttributes(List<RoleBasicInfo> roles, List<String> requiredAttributes,
                                                   String tenantDomain)
            throws IdentityRoleManagementException {

        List<Role> rolesList = new ArrayList();
        for (RoleBasicInfo roleBasicInfo : roles) {
            Role role = new Role();
            role.setId(roleBasicInfo.getId());
            role.setName(roleBasicInfo.getName());
            role.setAudienceId(roleBasicInfo.getAudienceId());
            role.setAudienceName(roleBasicInfo.getAudienceName());
            role.setAudience(roleBasicInfo.getAudience());
            if (requiredAttributes != null && !requiredAttributes.isEmpty()) {
                if (requiredAttributes.contains(USERS)) {
                    role.setUsers(getUserListOfRole(roleBasicInfo.getId(), tenantDomain));
                }
                if (requiredAttributes.contains(GROUPS)) {
                    role.setGroups(getGroupListOfRole(roleBasicInfo.getId(), tenantDomain));
                    role.setIdpGroups(getIdpGroupListOfRole(roleBasicInfo.getId(), tenantDomain));
                }
                if (requiredAttributes.contains(PERMISSIONS)) {
                    if (isSharedRole(roleBasicInfo.getId(), tenantDomain)) {
                        role.setPermissions(getPermissionsOfSharedRole(roleBasicInfo.getId(), tenantDomain));
                    } else {
                        role.setPermissions(getPermissions(roleBasicInfo.getId(), tenantDomain));
                    }
                }
                if (requiredAttributes.contains(ASSOCIATED_APPLICATIONS)) {
                    if (ORGANIZATION.equals(roleBasicInfo.getAudience())) {
                        role.setAssociatedApplications(getAssociatedAppsById(roleBasicInfo.getId(), tenantDomain));
                    } else if (APPLICATION.equals(roleBasicInfo.getAudience())) {
                        List<AssociatedApplication> associatedApplications = new ArrayList<>();
                        associatedApplications.add(new AssociatedApplication(roleBasicInfo.getAudienceId(),
                                roleBasicInfo.getAudienceName()));
                        role.setAssociatedApplications(associatedApplications);
                    }
                }
            }
            rolesList.add(role);
        }
        return rolesList;
    }

    @Override
    public Role getRole(String roleId, String tenantDomain) throws IdentityRoleManagementException {

        if (!isExistingRoleID(roleId, tenantDomain)) {
            throw new IdentityRoleManagementClientException(RoleConstants.Error.ROLE_NOT_FOUND.getCode(),
                    "Role id: " + roleId + " does not exist in the tenant: " + tenantDomain);
        }
        Role role = new Role();
        String roleName = getRoleNameByID(roleId, tenantDomain);
        RoleAudience roleAudience = getAudienceByRoleID(roleId, tenantDomain);
        role.setAudience(roleAudience.getAudience());
        role.setAudienceId(roleAudience.getAudienceId());
        role.setAudienceName(roleAudience.getAudienceName());

        if (ORGANIZATION.equals(roleAudience.getAudience())) {
            role.setAssociatedApplications(getAssociatedAppsById(roleId, tenantDomain));
        } else if (APPLICATION.equals(roleAudience.getAudience())) {
            List<AssociatedApplication> associatedApplications = new ArrayList<>();
            associatedApplications.add(new AssociatedApplication(roleAudience.getAudienceId(),
                    roleAudience.getAudienceName()));
            role.setAssociatedApplications(associatedApplications);
        }
        role.setId(roleId);
        role.setName(roleName);
        role.setTenantDomain(tenantDomain);
        role.setUsers(getUserListOfRole(roleId, tenantDomain));
        role.setGroups(getGroupListOfRole(roleId, tenantDomain));
        role.setIdpGroups(getIdpGroupListOfRole(roleId, tenantDomain));
        if (isSharedRole(roleId, tenantDomain)) {
            role.setPermissions(getPermissionsOfSharedRole(roleId, tenantDomain));
        } else {
            role.setPermissions(getPermissions(roleId, tenantDomain));
        }
        return role;
    }

    @Override
    public Role getRole(String roleId) throws IdentityRoleManagementException {

        String tenantDomain = getRoleTenantDomainById(roleId);
        return getRole(roleId, tenantDomain);
    }

    private String getRoleTenantDomainById(String roleId) throws IdentityRoleManagementException {

        String tenantId = null;
        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(false);
             NamedPreparedStatement statement = new NamedPreparedStatement(connection, GET_ROLE_TENANT_DOMAIN_BY_ID)) {
            statement.setString(RoleConstants.RoleTableColumns.UM_UUID, roleId);
            int count = 0;
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    // Handle multiple matching roles.
                    count++;
                    if (count > 1) {
                        String message = "Invalid scenario. Multiple roles found for the given role ID: " + roleId;
                        LOG.warn(message);
                    }
                    tenantId = resultSet.getString(1);
                }
            }
        } catch (SQLException e) {
            String errorMessage =
                    "Error while resolving the tenant domain for the given role ID: " + roleId;
            throw new IdentityRoleManagementServerException(RoleConstants.Error.UNEXPECTED_SERVER_ERROR.getCode(),
                    errorMessage, e);
        }
        if (tenantId == null) {
            String errorMessage = "A role doesn't exist with id: " + roleId;
            throw new IdentityRoleManagementClientException(ROLE_NOT_FOUND.getCode(), errorMessage);
        }

        return IdentityTenantUtil.getTenantDomain(Integer.parseInt(tenantId));
    }

    @Override
    public RoleBasicInfo getRoleBasicInfoById(String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

        String roleName = getRoleNameByID(roleId, tenantDomain);
        RoleBasicInfo roleBasicInfo = new RoleBasicInfo(roleId, roleName);
        RoleAudience roleAudience = getAudienceByRoleID(roleId, tenantDomain);
        roleBasicInfo.setAudience(roleAudience.getAudience());
        roleBasicInfo.setAudienceId(roleAudience.getAudienceId());
        roleBasicInfo.setAudienceName(roleAudience.getAudienceName());
        return roleBasicInfo;
    }

    @Override
    public List<Permission> getPermissionListOfRole(String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

        if (isOrganization(tenantDomain)) {
            return getPermissionsOfSharedRole(roleId, tenantDomain);
        } else {
            return getPermissions(roleId, tenantDomain);
        }
    }

    @Override
    public List<String> getPermissionListOfRoles(List<String> roleIds, String tenantDomain)
            throws IdentityRoleManagementException {

        if (isOrganization(tenantDomain)) {
            return getPermissionsOfSharedRoles(roleIds, tenantDomain);
        } else {
            return getPermissionListOfRolesByIds(roleIds, tenantDomain);
        }
    }

    private List<String> getPermissionListOfRolesByIds(List<String> roleIds, String tenantDomain)
            throws IdentityRoleManagementException {

        List<String> permissions = new ArrayList<>();
        String query = GET_SCOPE_BY_ROLES_SQL + String.join(", ",
                Collections.nCopies(roleIds.size(), "?")) + ")";
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             NamedPreparedStatement statement = new NamedPreparedStatement(connection, query)) {

            for (int i = 0; i < roleIds.size(); i++) {
                statement.setString(i + 1, roleIds.get(i));
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    permissions.add(resultSet.getString(1));
                }
            }
        } catch (SQLException e) {
            String errorMessage =
                    "Error while retrieving permissions for role ids: " + StringUtils.join(roleIds, ", ")
                            + " and tenantDomain : " + tenantDomain;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
        return permissions;
    }

    @Override
    public void updatePermissionListOfRole(String roleId, List<Permission> addedPermissions,
                                           List<Permission> deletedPermissions, String tenantDomain)
            throws IdentityRoleManagementException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(true)) {
            addPermissions(roleId, addedPermissions, tenantDomain, connection);
            for (Permission permission : deletedPermissions) {
                try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                        DELETE_ROLE_SCOPE_BY_SCOPE_NAME_SQL)) {
                    statement.setString(RoleConstants.RoleTableColumns.ROLE_ID, roleId);
                    statement.setString(RoleConstants.RoleTableColumns.SCOPE_NAME, permission.getName());
                    statement.executeUpdate();
                } catch (SQLException e) {
                    IdentityDatabaseUtil.rollbackTransaction(connection);
                    String errorMessage = "Error while adding permissions to roleId : " + roleId;
                    throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                            errorMessage, e);
                }
            }
            IdentityDatabaseUtil.commitTransaction(connection);
        } catch (SQLException | IdentityRoleManagementException e) {
            String errorMessage = "Error while adding permissions to roleId : " + roleId;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                    errorMessage, e);
        }
    }

    @Override
    public List<IdpGroup> getIdpGroupListOfRole(String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        List<IdpGroup> groups = new ArrayList<>();
        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(false);
             NamedPreparedStatement statement = new NamedPreparedStatement(connection, GET_IDP_GROUPS_SQL)) {

            statement.setString(RoleConstants.RoleTableColumns.UM_UUID, roleId);
            statement.setInt(RoleConstants.RoleTableColumns.UM_TENANT_ID, tenantId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    groups.add(new IdpGroup(resultSet.getString(1)));
                }
            }
        } catch (SQLException e) {
            String errorMessage =
                    "Error while retrieving idp groups for role id: " + roleId + " and tenantDomain : " + tenantDomain;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
        resolveIdpGroups(groups, tenantDomain);
        return groups;
    }

    @Override
    public void updateIdpGroupListOfRole(String roleId, List<IdpGroup> newGroupList,
                                         List<IdpGroup> deletedGroupList, String tenantDomain)
            throws IdentityRoleManagementException {

        validateGroupIds(newGroupList, tenantDomain);
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(true)) {
            try {
                addIdpGroups(roleId, newGroupList, tenantDomain, connection);
                for (IdpGroup idpGroup : deletedGroupList) {
                    try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                            DELETE_IDP_GROUPS_SQL)) {
                        statement.setString(RoleConstants.RoleTableColumns.UM_GROUP_ID, idpGroup.getGroupId());
                        statement.setString(RoleConstants.RoleTableColumns.UM_UUID, roleId);
                        statement.setInt(RoleConstants.RoleTableColumns.UM_TENANT_ID, tenantId);
                        statement.executeUpdate();
                    } catch (SQLException e) {
                        IdentityDatabaseUtil.rollbackTransaction(connection);
                        String errorMessage = "Error while assign idp groups to roleId : " + roleId;
                        throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                                errorMessage, e);
                    }
                }
                IdentityDatabaseUtil.commitTransaction(connection);
            } catch (IdentityRoleManagementException e) {
                IdentityDatabaseUtil.rollbackTransaction(connection);
                String errorMessage = "Error while assign idp groups to roleId : " + roleId;
                throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
            }

        } catch (SQLException e) {
            String errorMessage = "Error while assign idp groups to roleId : " + roleId;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
    }

    @Override
    public void deleteRole(String roleId, String tenantDomain) throws IdentityRoleManagementException {

        String roleName = getRoleNameByID(roleId, tenantDomain);
        if (systemRoles.contains(roleName) && !isOrganization(tenantDomain)) {
            throw new IdentityRoleManagementClientException(OPERATION_FORBIDDEN.getCode(),
                    "Invalid operation. Role: " + roleName + " Cannot be deleted since it's a read only system role.");
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        UserRealm userRealm;
        try {
            userRealm = CarbonContext.getThreadLocalCarbonContext().getUserRealm();
            if ((UserCoreUtil.isEveryoneRole(roleName, userRealm.getRealmConfiguration())
                    || isInternalAdminOrSystemRole(roleId, tenantDomain, userRealm))
                    && !isOrganization(tenantDomain)) {
                throw new IdentityRoleManagementClientException(OPERATION_FORBIDDEN.getCode(),
                        "Invalid operation. Role: " + roleName + " Cannot be deleted.");
            }
        } catch (UserStoreException e) {
            throw new IdentityRoleManagementServerException(RoleConstants.Error.UNEXPECTED_SERVER_ERROR.getCode(),
                    "Error while getting the realmConfiguration.", e);
        }
        int audienceRefId = getAudienceRefByID(roleId, tenantDomain);
        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(true)) {
            try {
                List<RoleDTO> sharedHybridRoles = getSharedHybridRoles(roleId, tenantId, connection);
                deleteSharedHybridRoles(roleId, tenantId, connection);
                try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, DELETE_ROLE_SQL,
                        RoleConstants.RoleTableColumns.UM_ID)) {
                    statement.setString(RoleConstants.RoleTableColumns.UM_UUID, roleId);
                    statement.setInt(RoleConstants.RoleTableColumns.UM_TENANT_ID, tenantId);
                    statement.executeUpdate();
                }

                // Delete the role from IDN_SCIM_GROUP table.
                deleteSCIMRole(roleId, roleName, audienceRefId, sharedHybridRoles, tenantDomain);

                /* UM_ROLE_PERMISSION Table, roles are associated with Domain ID.
                   At this moment Role name doesn't contain the Domain prefix.
                   clearRoleAuthorization() expects domain qualified name.
                   Hence, we add the "Internal" Domain name explicitly here. */
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

    /**
     * Check whether the given role is an internal admin or system role.
     *
     * @param roleId           Role ID.
     * @param tenantDomain     Tenant domain.
     * @param userRealm        User realm.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    private boolean isInternalAdminOrSystemRole(String roleId, String tenantDomain, UserRealm userRealm)
            throws IdentityRoleManagementException, UserStoreException {

        RoleBasicInfo roleBasicInfo = getRoleBasicInfoById(roleId, tenantDomain);
        /* There won't be multiple internal/admin and internal/system roles in the same organization
        with organization audience. */
        if (ORGANIZATION.equalsIgnoreCase(roleBasicInfo.getAudience())) {
            String roleNameWithDomain = UserCoreUtil.addInternalDomainName(roleBasicInfo.getName());
            return isInternalSystemRole(roleNameWithDomain) || isInternalAdminRole(roleNameWithDomain, userRealm);
        }
        return false;
    }

    /**
     * Check whether the given role is an internal system role.
     *
     * @param roleName Role name with domain.
     */
    private boolean isInternalSystemRole(String roleName) {

        String internalSystemRole = INTERNAL_DOMAIN + UserCoreConstants.DOMAIN_SEPARATOR + SYSTEM;
        return internalSystemRole.equalsIgnoreCase(roleName);
    }

    /**
     * Check whether the given role is an internal admin role.
     *
     * @param roleName  Role name with domain.
     * @param userRealm User realm.
     * @throws UserStoreException User store exception.
     */
    private boolean isInternalAdminRole(String roleName, UserRealm userRealm) throws UserStoreException {

        RealmConfiguration realmConfig = userRealm.getRealmConfiguration();
        return realmConfig.isPrimary() && realmConfig.getAdminRoleName().equalsIgnoreCase(roleName);
    }

    @Override
    public void updateRoleName(String roleId, String newRoleName, String tenantDomain)
            throws IdentityRoleManagementException {

        String roleName = getRoleNameByID(roleId, tenantDomain);
        if (systemRoles.contains(roleName)) {
            throw new IdentityRoleManagementClientException(OPERATION_FORBIDDEN.getCode(),
                    "Invalid operation. Role: " + roleName + " Cannot be renamed since it's a read only system role.");
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        if (!isExistingRoleID(roleId, tenantDomain)) {
            throw new IdentityRoleManagementClientException(ROLE_NOT_FOUND.getCode(),
                    "Role id: " + roleId + " does not exist in the system.");
        }
        RoleAudience roleAudience = getAudienceByRoleID(roleId, tenantDomain);
        if (!StringUtils.equalsIgnoreCase(roleName, newRoleName) && isExistingRoleName(newRoleName,
                roleAudience.getAudience(), roleAudience.getAudienceId(), tenantDomain)) {
            throw new IdentityRoleManagementClientException(RoleConstants.Error.ROLE_ALREADY_EXISTS.getCode(),
                    "Role already exist for the role name: " + roleName + " audience: " + roleAudience.getAudience()
                            + " audienceId: " + roleAudience.getAudienceId());
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Updating the roleName: " + roleName + " to :" + newRoleName + " in the tenantDomain: "
                    + tenantDomain);
        }
        int audienceRefId = getAudienceRefByID(roleId, tenantDomain);
        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(true)) {
            List<RoleDTO> sharedRoles = getSharedHybridRoles(roleId, tenantId, connection);
            try {
                try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, UPDATE_ROLE_NAME_SQL,
                        RoleConstants.RoleTableColumns.UM_ID)) {
                    statement.setString(RoleConstants.RoleTableColumns.NEW_UM_ROLE_NAME, newRoleName);
                    statement.setString(RoleConstants.RoleTableColumns.UM_UUID, roleId);
                    statement.setInt(RoleConstants.RoleTableColumns.UM_TENANT_ID, tenantId);
                    statement.executeUpdate();
                }
                // Update shared hybrid role names
                updateSharedHybridRolesName(sharedRoles, newRoleName, connection);
                // Update the role name in IDN_SCIM_GROUP table.
                updateSCIMRoleName(roleName, newRoleName, audienceRefId, sharedRoles, tenantDomain);

                IdentityDatabaseUtil.commitUserDBTransaction(connection);
            } catch (SQLException | IdentityRoleManagementException e) {
                IdentityDatabaseUtil.rollbackUserDBTransaction(connection);
                String message = "Error while updating the role name: %s in the tenantDomain: %s";
                throw new IdentityRoleManagementServerException(RoleConstants.Error.UNEXPECTED_SERVER_ERROR.getCode(),
                        String.format(message, roleName, tenantDomain), e);
            }
        } catch (SQLException e) {
            String message = "Error while updating the role name: %s in the tenantDomain: %s";
            throw new IdentityRoleManagementServerException(RoleConstants.Error.UNEXPECTED_SERVER_ERROR.getCode(),
                    String.format(message, roleName, tenantDomain), e);
        }

        clearUserRolesCacheByTenant(tenantId);
    }

    private void updateSharedHybridRolesName(List<RoleDTO> sharedRoles, String newRoleName, Connection connection)
            throws IdentityRoleManagementException {

        for (RoleDTO roleDTO : sharedRoles) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, UPDATE_ROLE_NAME_SQL,
                    RoleConstants.RoleTableColumns.UM_ID)) {
                statement.setString(RoleConstants.RoleTableColumns.NEW_UM_ROLE_NAME, newRoleName);
                statement.setString(RoleConstants.RoleTableColumns.UM_UUID, roleDTO.getId());
                statement.setInt(RoleConstants.RoleTableColumns.UM_TENANT_ID, roleDTO.getTenantId());
                statement.executeUpdate();
            } catch (SQLException e) {
                IdentityDatabaseUtil.rollbackUserDBTransaction(connection);
                String message = "Error while updating the role name of shared role: %s";
                throw new IdentityRoleManagementServerException(RoleConstants.Error.UNEXPECTED_SERVER_ERROR.getCode(),
                        String.format(message, roleDTO.getId()), e);
            }
        }
    }

    @Override
    public int getRolesCount(String tenantDomain) throws IdentityRoleManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(false)) {
            String databaseProductName = connection.getMetaData().getDatabaseProductName();
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                    getDBTypeSpecificRolesCountQuery(databaseProductName))) {
                statement.setInt(RoleConstants.RoleTableColumns.UM_TENANT_ID,
                        tenantId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new IdentityRoleManagementServerException(RoleConstants.Error.UNEXPECTED_SERVER_ERROR.getCode(),
                    "Error while getting total roles count in tenantDomain: " + tenantDomain, e);
        }
        return 0;
    }

    @Override
    public Role getRoleWithoutUsers(String roleId, String tenantDomain) throws IdentityRoleManagementException {

        if (!isExistingRoleID(roleId, tenantDomain)) {
            throw new IdentityRoleManagementClientException(RoleConstants.Error.ROLE_NOT_FOUND.getCode(),
                    "Role id: " + roleId + " does not exist in the tenant: " + tenantDomain);
        }
        Role role = new Role();
        String roleName = getRoleNameByID(roleId, tenantDomain);
        RoleAudience roleAudience = getAudienceByRoleID(roleId, tenantDomain);
        role.setAudience(roleAudience.getAudience());
        role.setAudienceId(roleAudience.getAudienceId());
        role.setAudienceName(roleAudience.getAudienceName());

        if (ORGANIZATION.equals(roleAudience.getAudience())) {
            role.setAssociatedApplications(getAssociatedAppsById(roleId, tenantDomain));
        } else if (APPLICATION.equals(roleAudience.getAudience())) {
            List<AssociatedApplication> associatedApplications = new ArrayList<>();
            associatedApplications.add(new AssociatedApplication(roleAudience.getAudienceId(),
                    roleAudience.getAudienceName()));
            role.setAssociatedApplications(associatedApplications);
        }
        role.setId(roleId);
        role.setName(roleName);
        role.setTenantDomain(tenantDomain);
        role.setGroups(getGroupListOfRole(roleId, tenantDomain));
        role.setIdpGroups(getIdpGroupListOfRole(roleId, tenantDomain));
        if (isSharedRole(roleId, tenantDomain)) {
            role.setPermissions(getPermissionsOfSharedRole(roleId, tenantDomain));
        } else {
            role.setPermissions(getPermissions(roleId, tenantDomain));
        }
        return role;
    }

    @Override
    public void addMainRoleToSharedRoleRelationship(String mainRoleUUID, String sharedRoleUUID,
                                                    String mainRoleTenantDomain, String sharedRoleTenantDomain)
            throws IdentityRoleManagementException {

        String mainRoleName = getRoleNameByID(mainRoleUUID, mainRoleTenantDomain);
        int mainRoleTenantId = IdentityTenantUtil.getTenantId(mainRoleTenantDomain);

        String sharedRoleName = getRoleNameByID(sharedRoleUUID, sharedRoleTenantDomain);
        int sharedRoleTenantId = IdentityTenantUtil.getTenantId(sharedRoleTenantDomain);

        int mainRoleUMId = 0;
        int sharedRoleUMId = 0;
        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(false)) {
            try (NamedPreparedStatement stmt = new NamedPreparedStatement(connection, GET_ROLE_UM_ID_BY_UUID)) {
                stmt.setString(RoleConstants.RoleTableColumns.UM_UUID, mainRoleUUID);
                ResultSet resultSet = stmt.executeQuery();
                while (resultSet.next()) {
                    mainRoleUMId = resultSet.getInt(1);
                }
            } catch (SQLException e) {
                String message = "Error while resolving id of role name: %s in the tenantDomain: %s.";
                throw new IdentityRoleManagementServerException(RoleConstants.Error.UNEXPECTED_SERVER_ERROR.getCode(),
                        String.format(message, mainRoleName, mainRoleTenantDomain), e);
            }

            try (NamedPreparedStatement stmt = new NamedPreparedStatement(connection, GET_ROLE_UM_ID_BY_UUID)) {
                stmt.setString(RoleConstants.RoleTableColumns.UM_UUID, sharedRoleUUID);
                ResultSet resultSet = stmt.executeQuery();
                while (resultSet.next()) {
                    sharedRoleUMId = resultSet.getInt(1);
                }
            } catch (SQLException e) {
                String message = "Error while resolving id of role name: %s in the tenantDomain: %s.";
                throw new IdentityRoleManagementServerException(RoleConstants.Error.UNEXPECTED_SERVER_ERROR.getCode(),
                        String.format(message, sharedRoleName, sharedRoleTenantDomain), e);
            }

            if (mainRoleUMId == 0 || sharedRoleUMId == 0) {
                String message = "Error while resolving role id.";
                throw new IdentityRoleManagementServerException(RoleConstants.Error.UNEXPECTED_SERVER_ERROR.getCode(),
                        message);
            }
            try (NamedPreparedStatement preparedStatement = new NamedPreparedStatement(connection,
                    INSERT_MAIN_TO_SHARED_ROLE_RELATIONSHIP)) {
                preparedStatement.setInt(RoleConstants.RoleTableColumns.UM_SHARED_ROLE_ID, sharedRoleUMId);
                preparedStatement.setInt(RoleConstants.RoleTableColumns.UM_MAIN_ROLE_ID, mainRoleUMId);
                preparedStatement.setInt(RoleConstants.RoleTableColumns.UM_SHARED_ROLE_TENANT_ID, sharedRoleTenantId);
                preparedStatement.setInt(RoleConstants.RoleTableColumns.UM_MAIN_ROLE_TENANT_ID, mainRoleTenantId);
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                String message = "Error while adding the role relationship of role: %s.";
                throw new IdentityRoleManagementServerException(RoleConstants.Error.UNEXPECTED_SERVER_ERROR.getCode(),
                        String.format(message, sharedRoleName), e);
            }
        } catch (SQLException e) {
            String message = "Error while adding the role relationship of role: %s.";
            throw new IdentityRoleManagementServerException(RoleConstants.Error.UNEXPECTED_SERVER_ERROR.getCode(),
                    String.format(message, sharedRoleName), e);
        }
    }

    @Override
    public List<RoleBasicInfo> getRoleListOfUser(String userId, String tenantDomain)
            throws IdentityRoleManagementException {

        String userName = getUsernameByUserID(userId, tenantDomain);
        String primaryDomainName = IdentityUtil.getPrimaryDomainName();
        if (primaryDomainName != null) {
            primaryDomainName = primaryDomainName.toUpperCase(Locale.ENGLISH);
        }
        userName = UserCoreUtil.addDomainToName(userName, primaryDomainName);
        // Get domain from name.
        String domainName = UserCoreUtil.extractDomainFromName(userName);
        if (domainName != null) {
            domainName = domainName.toUpperCase(Locale.ENGLISH);
        }
        String nameWithoutDomain = UserCoreUtil.removeDomainFromName(userName);
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        List<RoleBasicInfo> roles = new ArrayList<>();
        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(false);
             NamedPreparedStatement statement = new NamedPreparedStatement(connection, GET_ROLE_LIST_OF_USER_SQL)) {

            statement.setString(RoleConstants.RoleTableColumns.UM_USER_NAME, nameWithoutDomain);
            statement.setInt(RoleConstants.RoleTableColumns.UM_TENANT_ID, tenantId);
            statement.setString(RoleConstants.RoleTableColumns.UM_DOMAIN_NAME, domainName);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String roleName = resultSet.getString(1);
                    String roleId = resultSet.getString(2);
                    String audience = resultSet.getString(3);
                    String audienceId = resultSet.getString(4);
                    RoleBasicInfo roleBasicInfo = new RoleBasicInfo(roleId, roleName);
                    roleBasicInfo.setAudience(audience);
                    roleBasicInfo.setAudienceId(audienceId);
                    roleBasicInfo.setAudienceName(getAudienceName(audience, audienceId, tenantDomain));
                    roles.add(roleBasicInfo);
                }
            }
            if (!isOrganization(tenantDomain)) {
                roles.add(getEveryOneRole(tenantDomain));
            }
        } catch (SQLException e) {
            String errorMessage =
                    "Error while retrieving role list of user by id: " + userId + " and tenantDomain : " + tenantDomain;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
        return roles;
    }

    /**
     * Get everyone role name.
     *
     * @param tenantDomain Tenant domain.
     * @return every one role name.
     * @throws IdentityRoleManagementException if error occurred while retrieving everyone role name.
     */
    private String getEveryOneRoleName(String tenantDomain) throws IdentityRoleManagementException {

        String everyOneRoleName;
        try {
            everyOneRoleName = CarbonContext.getThreadLocalCarbonContext().getUserRealm().getRealmConfiguration()
                    .getEveryOneRoleName();
        } catch (UserStoreException e) {
            throw new IdentityRoleManagementException("Error while retrieving everyone role name", e);
        }
        if (everyOneRoleName == null) {
            String errorMessage =
                    "Everyone role name not found for tenantDomain : " + tenantDomain;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage);
        }
        return removeInternalDomain(everyOneRoleName);
    }

    /**
     * Get everyone role basic info.
     *
     * @param tenantDomain Tenant domain.
     * @return basic info of every one role.
     * @throws IdentityRoleManagementException if error occurred while retrieving everyone role.
     */
    private RoleBasicInfo getEveryOneRole(String tenantDomain) throws IdentityRoleManagementException {

        String everyOneRoleName = getEveryOneRoleName(tenantDomain);
        String orgId = getOrganizationId(tenantDomain);
        String roleId = getRoleIdByName(everyOneRoleName, ORGANIZATION, orgId, tenantDomain);
        RoleBasicInfo roleBasicInfo = new RoleBasicInfo(roleId, everyOneRoleName);
        roleBasicInfo.setAudience(ORGANIZATION);
        roleBasicInfo.setAudienceId(getOrganizationId(tenantDomain));
        roleBasicInfo.setAudienceName(getOrganizationName(orgId));
        return roleBasicInfo;
    }

    /**
     * Get everyone role id.
     *
     * @param tenantDomain Tenant domain.
     * @return every one role id.
     * @throws IdentityRoleManagementException if error occurred while retrieving everyone role id.
     */
    private String getEveryOneRoleId(String tenantDomain) throws IdentityRoleManagementException {

        String everyOneRoleName = getEveryOneRoleName(tenantDomain);
        String orgId = getOrganizationId(tenantDomain);
        return getRoleIdByName(everyOneRoleName, ORGANIZATION, orgId, tenantDomain);
    }

    private String getUsernameByUserID(String userId, String tenantDomain) throws IdentityRoleManagementException {

        return userIDResolver.getNameByID(userId, tenantDomain);
    }

    @Override
    public List<RoleBasicInfo> getRoleListOfGroups(List<String> groupIds, String tenantDomain)
            throws IdentityRoleManagementException {

        Map<String, String> groupIdsToNames = getGroupNamesByIDs(groupIds, tenantDomain);
        List<String> groupNamesList = new ArrayList<>(groupIdsToNames.values());
        String primaryDomainName = IdentityUtil.getPrimaryDomainName();
        if (primaryDomainName != null) {
            primaryDomainName = primaryDomainName.toUpperCase(Locale.ENGLISH);
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        List<RoleBasicInfo> roles = new ArrayList<>();
        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(false);
             NamedPreparedStatement statement = new NamedPreparedStatement(connection, GET_ROLE_LIST_OF_GROUP_SQL)) {
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
                statement.setInt(RoleConstants.RoleTableColumns.UM_TENANT_ID, tenantId);
                statement.setString(RoleConstants.RoleTableColumns.UM_DOMAIN_NAME, domainName);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        String roleName = resultSet.getString(1);
                        String roleId = resultSet.getString(2);
                        String audience = resultSet.getString(3);
                        String audienceId = resultSet.getString(4);
                        RoleBasicInfo roleBasicInfo = new RoleBasicInfo(roleId, roleName);
                        roleBasicInfo.setAudience(audience);
                        roleBasicInfo.setAudienceId(audienceId);
                        roleBasicInfo.setAudienceName(getAudienceName(audience, audienceId, tenantDomain));
                        roles.add(roleBasicInfo);
                    }
                }
            }
        } catch (SQLException e) {
            String errorMessage =
                    "Error while retrieving role list of groups by ids: " + String.join(", ", groupIds)
                            + " and tenantDomain : " + tenantDomain;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
        return new ArrayList<>(roles.stream()
                .collect(Collectors.toMap(RoleBasicInfo::getId, role -> role, (existing, replacement) -> existing))
                .values());
    }

    @Override
    public List<RoleBasicInfo> getRoleListOfIdpGroups(List<String> groupIds, String tenantDomain)
            throws IdentityRoleManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        List<RoleBasicInfo> roles = new ArrayList<>();
        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(false);
             NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                     GET_ROLE_LIST_OF_IDP_GROUPS_SQL)) {
            for (String groupId : groupIds) {
                statement.setString(RoleConstants.RoleTableColumns.UM_GROUP_ID, groupId);
                statement.setInt(RoleConstants.RoleTableColumns.UM_TENANT_ID, tenantId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        String roleName = resultSet.getString(1);
                        String roleId = resultSet.getString(2);
                        String audience = resultSet.getString(3);
                        String audienceId = resultSet.getString(4);
                        RoleBasicInfo roleBasicInfo = new RoleBasicInfo(roleId, roleName);
                        roleBasicInfo.setAudience(audience);
                        roleBasicInfo.setAudienceId(audienceId);
                        roleBasicInfo.setAudienceName(getAudienceName(audience, audienceId, tenantDomain));
                        roles.add(roleBasicInfo);
                    }
                }
            }
        } catch (SQLException e) {
            String errorMessage =
                    "Error while retrieving role list of idp groups by ids: " + String.join(", ", groupIds)
                            + " and tenantDomain : " + tenantDomain;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
        return new ArrayList<>(roles.stream()
                .collect(Collectors.toMap(RoleBasicInfo::getId, role -> role, (existing, replacement) -> existing))
                .values());
    }

    @Override
    public List<String> getRoleIdListOfUser(String userId, String tenantDomain) throws IdentityRoleManagementException {

        String userName = getUsernameByUserID(userId, tenantDomain);
        String primaryDomainName = IdentityUtil.getPrimaryDomainName();
        if (primaryDomainName != null) {
            primaryDomainName = primaryDomainName.toUpperCase(Locale.ENGLISH);
        }
        userName = UserCoreUtil.addDomainToName(userName, primaryDomainName);
        // Get domain from name.
        String domainName = UserCoreUtil.extractDomainFromName(userName);
        if (domainName != null) {
            domainName = domainName.toUpperCase(Locale.ENGLISH);
        }
        String nameWithoutDomain = UserCoreUtil.removeDomainFromName(userName);
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        List<String> roleIds = new ArrayList<>();
        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(false);
             NamedPreparedStatement statement = new NamedPreparedStatement(connection, GET_ROLE_ID_LIST_OF_USER_SQL)) {

            statement.setString(RoleConstants.RoleTableColumns.UM_USER_NAME, nameWithoutDomain);
            statement.setInt(RoleConstants.RoleTableColumns.UM_TENANT_ID, tenantId);
            statement.setString(RoleConstants.RoleTableColumns.UM_DOMAIN_NAME, domainName);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String roleId = resultSet.getString(1);
                    roleIds.add(roleId);
                }
            }
            if (!isOrganization(tenantDomain)) {
                roleIds.add(getEveryOneRoleId(tenantDomain));
            }
        } catch (SQLException e) {
            String errorMessage = "Error while retrieving role id list of user by id: " + userId + " and " +
                    "tenantDomain : " + tenantDomain;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
        return roleIds;
    }

    @Override
    public List<String> getRoleIdListOfGroups(List<String> groupIds, String tenantDomain)
            throws IdentityRoleManagementException {

        Map<String, String> groupIdsToNames = getGroupNamesByIDs(groupIds, tenantDomain);
        List<String> groupNamesList = new ArrayList<>(groupIdsToNames.values());
        String primaryDomainName = IdentityUtil.getPrimaryDomainName();
        if (primaryDomainName != null) {
            primaryDomainName = primaryDomainName.toUpperCase(Locale.ENGLISH);
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        List<String> roleIds = new ArrayList<>();
        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(false);
             NamedPreparedStatement statement = new NamedPreparedStatement(connection, GET_ROLE_ID_LIST_OF_GROUP_SQL)) {
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
                statement.setInt(RoleConstants.RoleTableColumns.UM_TENANT_ID, tenantId);
                statement.setString(RoleConstants.RoleTableColumns.UM_DOMAIN_NAME, domainName);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        String roleId = resultSet.getString(1);
                        roleIds.add(roleId);
                    }
                }
            }
        } catch (SQLException e) {
            String errorMessage =
                    "Error while retrieving role list of groups by ids: " + String.join(", ", groupIds)
                            + " and tenantDomain : " + tenantDomain;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
        return roleIds.stream().distinct().collect(Collectors.toList());
    }

    @Override
    public List<String> getRoleIdListOfIdpGroups(List<String> groupIds, String tenantDomain)
            throws IdentityRoleManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        List<String> roleIds = new ArrayList<>();
        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(false);
             NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                     GET_ROLE_ID_LIST_OF_IDP_GROUPS_SQL)) {
            for (String groupId : groupIds) {
                statement.setString(RoleConstants.RoleTableColumns.UM_GROUP_ID, groupId);
                statement.setInt(RoleConstants.RoleTableColumns.UM_TENANT_ID, tenantId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        String roleId = resultSet.getString(1);
                        roleIds.add(roleId);
                    }
                }
            }
        } catch (SQLException e) {
            String errorMessage =
                    "Error while retrieving role list of idp groups by ids: " + String.join(", ", groupIds)
                            + " and tenantDomain : " + tenantDomain;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
        return roleIds.stream().distinct().collect(Collectors.toList());
    }

    @Override
    public void deleteRolesByApplication(String applicationId, String tenantDomain)
            throws IdentityRoleManagementException {

        List<RoleDTO> hybridRoles = getHybridRolesByApplication(applicationId, tenantDomain);
        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(true);
             NamedPreparedStatement statement = new NamedPreparedStatement(connection, DELETE_ROLES_BY_APP_ID_SQL)) {
            try {
                statement.setInt(RoleConstants.RoleTableColumns.UM_TENANT_ID,
                        IdentityTenantUtil.getTenantId(tenantDomain));
                statement.setString(RoleConstants.RoleTableColumns.UM_AUDIENCE, APPLICATION);
                statement.setString(RoleConstants.RoleTableColumns.UM_AUDIENCE_ID, applicationId);
                statement.executeUpdate();
                for (RoleDTO role : hybridRoles) {
                    // Delete the role from IDN_SCIM_GROUP table.
                    deleteSCIMRole(role.getId(), role.getName(), role.getAudienceRefId(), null,
                            tenantDomain);
                }
                IdentityDatabaseUtil.commitUserDBTransaction(connection);
            } catch (SQLException | IdentityRoleManagementException e) {
                IdentityDatabaseUtil.rollbackUserDBTransaction(connection);
                String message = "Error while deleting roles by app id : " + applicationId;
                throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                        message, e);
            }
        } catch (SQLException e) {
            String message = "Error while deleting roles by app id : " + applicationId;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                    message, e);
        }
    }

    @Override
    public Map<String, String> getMainRoleToSharedRoleMappingsBySubOrg(List<String> roleIds,
                                                                       String subOrgTenantDomain)
            throws IdentityRoleManagementException {

        Map<String, String> rolesMap = new HashMap<>();
        if (CollectionUtils.isEmpty(roleIds)) {
            return rolesMap;
        }
        String query = GET_MAIN_ROLE_TO_SHARED_ROLE_MAPPINGS_BY_SUBORG_SQL +
                String.join(", ", Collections.nCopies(roleIds.size(), "?")) + ")";
        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(false);
             NamedPreparedStatement statement = new NamedPreparedStatement(connection, query)) {

            statement.setInt(1, IdentityTenantUtil.getTenantId(subOrgTenantDomain));
            for (int i = 0; i < roleIds.size(); i++) {
                statement.setString(i + 2, roleIds.get(i));
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String mainRoleId = resultSet.getString(1);
                    String sharedRoleId = resultSet.getString(2);
                    rolesMap.put(mainRoleId, sharedRoleId);
                }
            }
        } catch (SQLException e) {
            String errorMessage = "Error while retrieving main role to shared role mappings by sub org tenant : "
                    + subOrgTenantDomain;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                    errorMessage, e);
        }
        return rolesMap;
    }

    @Override
    public List<String> getAssociatedApplicationIdsByRoleId(String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

        List<String> associatedApplicationIds = new ArrayList<>();
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                     GET_ASSOCIATED_APP_IDS_BY_ROLE_ID_SQL)) {

            statement.setString(RoleConstants.RoleTableColumns.ROLE_ID, roleId);
            statement.setInt(RoleConstants.RoleTableColumns.TENANT_ID,
                    IdentityTenantUtil.getTenantId(tenantDomain));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    associatedApplicationIds.add(resultSet.getString(1));
                }
            }
        } catch (SQLException e) {
            String errorMessage =
                    "Error while retrieving associated app for role id: " + roleId;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
        return associatedApplicationIds;
    }

    private List<RoleDTO> getHybridRolesByApplication(String applicationId, String tenantDomain)
            throws IdentityRoleManagementException {

        List<RoleDTO> hybridRoles = new ArrayList<>();
        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(false);
             NamedPreparedStatement statement = new NamedPreparedStatement(connection, GET_ROLES_BY_APP_ID_SQL)) {

            statement.setInt(RoleConstants.RoleTableColumns.UM_TENANT_ID, IdentityTenantUtil.getTenantId(tenantDomain));
            statement.setString(RoleConstants.RoleTableColumns.UM_AUDIENCE, APPLICATION);
            statement.setString(RoleConstants.RoleTableColumns.UM_AUDIENCE_ID, applicationId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String id = resultSet.getString(1);
                    String name = resultSet.getString(2);
                    int tenantId = resultSet.getInt(3);
                    int audienceRefId = resultSet.getInt(4);
                    hybridRoles.add(new RoleDTO(name, id, audienceRefId, tenantId));
                }
            }
        } catch (SQLException e) {
            String errorMessage = "Error while retrieving roles by app id : " + applicationId;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                    errorMessage, e);
        }
        return hybridRoles;
    }

    /**
     * Update scim role name.
     *
     * @param roleName      roleName.
     * @param newRoleName   New role name.
     * @param audienceRefId Audience Ref ID.
     * @param tenantDomain  Tenant domain.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    private void updateSCIMRoleName(String roleName, String newRoleName, int audienceRefId, List<RoleDTO> sharedRoles,
                                    String tenantDomain)
            throws IdentityRoleManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        // Append internal domain in order to maintain the backward compatibility.
        roleName = appendInternalDomain(roleName);
        newRoleName = appendInternalDomain(newRoleName);
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(true)) {
            // Update shared scim role names
            updateSharedSCIMRolesName(sharedRoles, newRoleName, connection);
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, UPDATE_SCIM_ROLE_NAME_SQL)) {
                statement.setString(RoleConstants.RoleTableColumns.NEW_ROLE_NAME, newRoleName);
                statement.setInt(RoleConstants.RoleTableColumns.TENANT_ID, tenantId);
                statement.setString(ROLE_NAME, roleName);
                statement.setInt(RoleConstants.RoleTableColumns.AUDIENCE_REF_ID, audienceRefId);
                statement.executeUpdate();

                IdentityDatabaseUtil.commitTransaction(connection);
            } catch (SQLException e) {
                IdentityDatabaseUtil.rollbackTransaction(connection);
                String errorMessage = "Error while updating the roleName: %s in the tenantDomain: " + "%s";
                throw new IdentityRoleManagementServerException(RoleConstants.Error.UNEXPECTED_SERVER_ERROR.getCode(),
                        String.format(errorMessage, roleName, tenantDomain), e);
            }
        } catch (SQLException e) {
            String errorMessage = "Error while updating the roleName: %s in the tenantDomain: " + "%s";
            throw new IdentityRoleManagementServerException(RoleConstants.Error.UNEXPECTED_SERVER_ERROR.getCode(),
                    String.format(errorMessage, roleName, tenantDomain), e);
        }
    }

    private void updateSharedSCIMRolesName(List<RoleDTO> sharedRoles, String newRoleName, Connection connection)
            throws IdentityRoleManagementException {

        for (RoleDTO roleDTO : sharedRoles) {
            String roleName = appendInternalDomain(roleDTO.getName());
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, UPDATE_SCIM_ROLE_NAME_SQL,
                    RoleConstants.RoleTableColumns.ID)) {
                statement.setString(RoleConstants.RoleTableColumns.NEW_ROLE_NAME, newRoleName);
                statement.setInt(RoleConstants.RoleTableColumns.TENANT_ID, roleDTO.getTenantId());
                statement.setString(ROLE_NAME, roleName);
                statement.setInt(RoleConstants.RoleTableColumns.AUDIENCE_REF_ID, roleDTO.getAudienceRefId());
                statement.executeUpdate();
            } catch (SQLException e) {
                IdentityDatabaseUtil.rollbackUserDBTransaction(connection);
                String message = "Error while updating the scim role names of shared role: %s";
                throw new IdentityRoleManagementServerException(RoleConstants.Error.UNEXPECTED_SERVER_ERROR.getCode(),
                        String.format(message, roleDTO.getId()), e);
            }
        }
    }

    /**
     * Add idp groups.
     *
     * @param roleId       Role ID.
     * @param groups       Permissions.
     * @param tenantDomain Tenant Domain.
     * @param connection   DB connection.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    private void addIdpGroups(String roleId, List<IdpGroup> groups, String tenantDomain,
                              Connection connection) throws IdentityRoleManagementException {

        if (groups == null || groups.isEmpty()) {
            return;
        }
        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, ADD_IDP_GROUPS_SQL)) {
            for (IdpGroup group : groups) {
                statement.setString(RoleConstants.RoleTableColumns.UM_UUID, roleId);
                statement.setInt(RoleConstants.RoleTableColumns.UM_TENANT_ID,
                        IdentityTenantUtil.getTenantId(tenantDomain));
                statement.setString(RoleConstants.RoleTableColumns.UM_GROUP_ID, group.getGroupId());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            String errorMessage = "Error while assigning idp groups to role id : " + roleId;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                    errorMessage, e);
        }
    }

    /**
     * Resolve idp groups.
     *
     * @param groups       Groups.
     * @param tenantDomain Tenant Domain.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    private void resolveIdpGroups(List<IdpGroup> groups, String tenantDomain) throws IdentityRoleManagementException {

        if (groups == null || groups.isEmpty()) {
            return;
        }

        List<String> idpGroupIds = groups.stream()
                .map(IdpGroup::getGroupId)
                .collect(Collectors.toList());
        List<IdPGroup> idpGroups = getIdpGroupsByIds(idpGroupIds, tenantDomain);

        groups.clear();
        idpGroups.stream().map(this::convertToIdpGroup).forEach(groups::add);
    }

    /**
     * Delete all role associations (permissions, apps, shared roles).
     *
     * @param roleId     Role ID.
     * @param connection DB connection.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    private void deleteRoleAssociations(String roleId, Connection connection) throws IdentityRoleManagementException {

        deleteAllPermissionsOfRole(roleId, connection);
        deleteAppRoleAssociation(roleId, connection);
    }

    /**
     * Add role info (role id, permissions, associated apps).
     *
     * @param roleName     Role name.
     * @param permissions  Permissions.
     * @param tenantDomain Tenant Domain.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    private void addRoleInfo(String roleId, String roleName, List<Permission> permissions, String audience,
                             String audienceId, int audienceRefId, String tenantDomain)
            throws IdentityRoleManagementException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(true)) {
            try {
                addRoleID(roleId, roleName, audienceRefId, tenantDomain, connection);
                addPermissions(roleId, permissions, tenantDomain, connection);

                if (APPLICATION.equals(audience) && !isOrganization(tenantDomain)) {
                    addAppRoleAssociation(roleId, audienceId, connection);
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
    }

    /**
     * Check role is a shared role.
     *
     * @param roleId       Role ID.
     * @param tenantDomain Tenant Domain.
     * @return is Shared role.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    private boolean isSharedRole(String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        boolean isShared = false;
        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(false);
             NamedPreparedStatement statement = new NamedPreparedStatement(connection, IS_SHARED_ROLE_SQL)) {

            statement.setString(RoleConstants.RoleTableColumns.UM_UUID, roleId);
            statement.setInt(RoleConstants.RoleTableColumns.UM_SHARED_ROLE_TENANT_ID, tenantId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    isShared = resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            String errorMessage = "Error while checking is existing role for role id: %s in the tenantDomain: %s";
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                    String.format(errorMessage, roleId, tenantDomain), e);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Is roleId: " + roleId + " Shared: " + isShared + " in the tenantDomain: " + tenantDomain);
        }
        return isShared;
    }

    /**
     * Check tenant is a sub organization.
     *
     * @param tenantDomain Tenant Domain.
     * @return is Shared Organization.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    private boolean isOrganization(String tenantDomain) throws IdentityRoleManagementException {

        try {
            return OrganizationManagementUtil.isOrganization(tenantDomain);
        } catch (OrganizationManagementException e) {
            String errorMessage = "Error while checking is sub org by tenant domain: " + tenantDomain;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
    }

    /**
     * Get permission of shared role.
     *
     * @param roleId       Role ID.
     * @param tenantDomain Tenant domain.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    private List<Permission> getPermissionsOfSharedRole(String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        String mainRoleId = null;
        int mainTenantId = -1;
        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(false);
             NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                     GET_SHARED_ROLE_MAIN_ROLE_ID_SQL)) {

            statement.setString(RoleConstants.RoleTableColumns.UM_UUID, roleId);
            statement.setInt(RoleConstants.RoleTableColumns.UM_TENANT_ID, tenantId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    mainRoleId = resultSet.getString(RoleConstants.RoleTableColumns.UM_UUID);
                    mainTenantId = resultSet.getInt(RoleConstants.RoleTableColumns.UM_TENANT_ID);
                }
            }
            if (StringUtils.isNotEmpty(mainRoleId) && mainTenantId != -1) {
                String mainTenantDomain = IdentityTenantUtil.getTenantDomain(mainTenantId);
                if (StringUtils.isNotEmpty(mainRoleId) && StringUtils.isNotEmpty(mainTenantDomain)) {
                    List<Permission> permissions = getPermissions(mainRoleId, mainTenantDomain);
                    return permissions.stream()
                            .filter(permission -> isValidSubOrgPermission(permission.getName()))
                            .collect(Collectors.toList());
                }
            }
        } catch (SQLException | IdentityRoleManagementException e) {
            String errorMessage = "Error while retrieving permissions for role id: %s in the tenantDomain: %s";
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                    String.format(errorMessage, roleId, tenantDomain), e);
        }
        return null;
    }

    /**
     * Check permission is a valid sub organization permission.
     *
     * @param permission Permission.
     * @return is valid sub organization permission.
     */
    private boolean isValidSubOrgPermission(String permission) {

        return permission.startsWith(INTERNAL_ORG_SCOPE_PREFIX) || permission.startsWith(CONSOLE_ORG_SCOPE_PREFIX) ||
                (!permission.startsWith(INTERNAL_SCOPE_PREFIX) && !permission.startsWith(CONSOLE_SCOPE_PREFIX));
    }

    /**
     * Get permission of shared roles.
     *
     * @param roleIds      Role IDs.
     * @param tenantDomain Tenant domain.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    private List<String> getPermissionsOfSharedRoles(List<String> roleIds, String tenantDomain)
            throws IdentityRoleManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        List<String> mainRoleIds = new ArrayList<>();
        int mainTenantId = -1;
        String query = GET_SHARED_ROLES_MAIN_ROLE_IDS_SQL + String.join(", ",
                Collections.nCopies(roleIds.size(), "?")) + ")";
        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(false);
             NamedPreparedStatement statement = new NamedPreparedStatement(connection, query)) {

            statement.setInt(RoleConstants.RoleTableColumns.UM_TENANT_ID, tenantId);
            for (int i = 0; i < roleIds.size(); i++) {
                statement.setString(i + 2, roleIds.get(i));
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    mainRoleIds.add(resultSet.getString(RoleConstants.RoleTableColumns.UM_UUID));
                    if (mainTenantId == -1) {
                        mainTenantId = resultSet.getInt(RoleConstants.RoleTableColumns.UM_TENANT_ID);
                    }
                }
            }
            if (!mainRoleIds.isEmpty() && mainTenantId != -1) {
                String mainTenantDomain = IdentityTenantUtil.getTenantDomain(mainTenantId);
                if (StringUtils.isNotEmpty(mainTenantDomain)) {
                    return getPermissionListOfRolesByIds(mainRoleIds, mainTenantDomain);
                }
            }
        } catch (SQLException | IdentityRoleManagementException e) {
            String errorMessage = "Error while retrieving permissions for role ids : "
                    + StringUtils.join(roleIds, ",") + "in the tenantDomain: " + tenantDomain;
            throw new IdentityRoleManagementServerException(errorMessage, e);
        }
        return null;
    }

    /**
     * Delete application role association.
     *
     * @param roleId     Role ID.
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
    private List<AssociatedApplication> getAssociatedAppsById(String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

        List<AssociatedApplication> associatedApplications = new ArrayList<>();
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                     GET_ASSOCIATED_APPS_BY_ROLE_ID_SQL)) {
            statement.setString(RoleConstants.RoleTableColumns.ROLE_ID, roleId);
            statement.setInt(RoleConstants.RoleTableColumns.TENANT_ID,
                    IdentityTenantUtil.getTenantId(tenantDomain));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    associatedApplications.add(new AssociatedApplication(resultSet.getString(1),
                            resultSet.getString(2)));
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
     * @param roleName     Role ID.
     * @param tenantDomain Tenant Domain.
     * @param connection   DB connection.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    private void addRoleID(String roleId, String roleName, int audienceRefId, String tenantDomain,
                           Connection connection) throws IdentityRoleManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        // Append internal domain in order to maintain the backward compatibility.
        roleName = appendInternalDomain(roleName);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Adding the roleId: " + roleId + " for the role: " + roleName + " in the tenantDomain: "
                    + tenantDomain);
        }
        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, ADD_SCIM_ROLE_ID_SQL)) {
            statement.setInt(RoleConstants.RoleTableColumns.TENANT_ID, tenantId);
            statement.setString(ROLE_NAME, roleName);
            statement.setString(RoleConstants.RoleTableColumns.ATTR_NAME, RoleConstants.ID_URI);
            statement.setString(RoleConstants.RoleTableColumns.ATTR_VALUE, roleId);
            statement.setInt(RoleConstants.RoleTableColumns.AUDIENCE_REF_ID, audienceRefId);
            statement.executeUpdate();
        } catch (SQLException e) {
            String errorMessage = "Error while adding the roleId: %s for the role: %s in the tenantDomain: %s";
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                    String.format(errorMessage, roleId, roleName, tenantDomain), e);
        }
    }

    /**
     * Add app role association.
     *
     * @param roleId     Role ID.
     * @param appId      App ID.
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
            String errorMessage = "Error while adding the app role association for app : " + appId + " and role : "
                    + roleId;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
    }

    /**
     * Add permissions.
     *
     * @param roleId       Role ID.
     * @param permissions  Permissions.
     * @param tenantDomain Tenant Domain.
     * @param connection   DB connection.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    private void addPermissions(String roleId, List<Permission> permissions, String tenantDomain, Connection connection)
            throws IdentityRoleManagementException {

        if (permissions == null || permissions.isEmpty()) {
            return;
        }
        checkPermissionsAlreadyAdded(roleId, permissions, tenantDomain, connection);
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
            String errorMessage = "Error while adding permissions to roleId : " + roleId;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                    errorMessage, e);
        }
    }

    /**
     * Check permissions already added.
     *
     * @param roleId       Role ID.
     * @param permissions  Permissions.
     * @param tenantDomain Tenant Domain.
     * @param connection   DB connection.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    private void checkPermissionsAlreadyAdded(String roleId, List<Permission> permissions, String tenantDomain,
                                              Connection connection) throws IdentityRoleManagementException {

        List<String> alreadyAddedPermissions = getPermissionNames(roleId, tenantDomain, connection);
        for (Permission permission : permissions) {
            if (alreadyAddedPermissions.contains(permission.getName())) {
                throw new IdentityRoleManagementClientException(PERMISSION_ALREADY_ADDED.getCode(),
                        "Permission: " + permission.getName() + " already assigned to role : " + roleId);
            }
        }
    }

    /**
     * Delete all permissions of the role.
     *
     * @param roleId     Role ID.
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
            String errorMessage = "Error while deleting permissions to roleId : " + roleId;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                    errorMessage, e);
        }

    }

    /**
     * Get permissions by role id.
     *
     * @param roleId       Role name.
     * @param tenantDomain Tenant Domain.
     * @return List of permissions.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    private List<Permission> getPermissions(String roleId, String tenantDomain) throws IdentityRoleManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        List<Permission> permissions = new ArrayList<>();
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             NamedPreparedStatement statement = new NamedPreparedStatement(connection, GET_ROLE_SCOPE_SQL)) {

            statement.setString(RoleConstants.RoleTableColumns.ROLE_ID, roleId);
            statement.setInt(RoleConstants.RoleTableColumns.TENANT_ID, tenantId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    permissions.add(new Permission(resultSet.getString(1),
                            resultSet.getString(2), resultSet.getString(3)));

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
     * Get permissions by role id.
     *
     * @param roleId       Role name.
     * @param tenantDomain Tenant Domain.
     * @return List of permissions.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    private List<String> getPermissionNames(String roleId, String tenantDomain, Connection connection)
            throws IdentityRoleManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        List<String> permissions = new ArrayList<>();
        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, GET_ROLE_SCOPE_NAMES_SQL)) {
            statement.setString(RoleConstants.RoleTableColumns.ROLE_ID, roleId);
            statement.setInt(RoleConstants.RoleTableColumns.TENANT_ID, tenantId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    permissions.add(resultSet.getString(1));
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
     * @param audience   Audience.
     * @param audienceId Audience ID.
     * @return audience ref id.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    private int getRoleAudienceRefId(String audience, String audienceId, Connection connection)
            throws IdentityRoleManagementException {

        int id = -1;
        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, GET_ROLE_AUDIENCE_SQL)) {
            statement.setString(RoleConstants.RoleTableColumns.UM_AUDIENCE, audience);
            statement.setString(RoleConstants.RoleTableColumns.UM_AUDIENCE_ID, audienceId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    id = resultSet.getInt(1);
                }
                // Create new audience.
                if (id == -1) {
                    createRoleAudience(audience, audienceId);
                    return getRoleAudienceRefId(audience, audienceId, connection);
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

    @Override
    public int getRoleAudienceRefId(String audience, String audienceId) throws IdentityRoleManagementException {

        int id = -1;
        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(false);
             NamedPreparedStatement statement = new NamedPreparedStatement(connection, GET_ROLE_AUDIENCE_SQL)) {

            statement.setString(RoleConstants.RoleTableColumns.UM_AUDIENCE, audience);
            statement.setString(RoleConstants.RoleTableColumns.UM_AUDIENCE_ID, audienceId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    id = resultSet.getInt(1);
                }
                // Create new audience.
                if (id == -1) {
                    createRoleAudience(audience, audienceId);
                    return getRoleAudienceRefId(audience, audienceId, connection);
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
     * @param audience   Audience.
     * @param audienceId Audience ID.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    private void createRoleAudience(String audience, String audienceId)
            throws IdentityRoleManagementException {

        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(true);) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, ADD_ROLE_AUDIENCE_SQL)) {
                statement.setString(RoleConstants.RoleTableColumns.UM_AUDIENCE, audience);
                statement.setString(RoleConstants.RoleTableColumns.UM_AUDIENCE_ID, audienceId);
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
    public boolean isExistingRoleName(String roleName, String audience, String audienceId, String tenantDomain)
            throws IdentityRoleManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        boolean isExist = false;
        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(true)) {
            int audienceRefId = getRoleAudienceRefId(audience, audienceId, connection);
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, IS_ROLE_EXIST_SQL,
                    RoleConstants.RoleTableColumns.UM_ID)) {
                statement.setString(RoleConstants.RoleTableColumns.UM_ROLE_NAME, removeInternalDomain(roleName));
                statement.setInt(RoleConstants.RoleTableColumns.UM_TENANT_ID, tenantId);
                statement.setInt(RoleConstants.RoleTableColumns.UM_AUDIENCE_REF_ID, audienceRefId);
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("Is roleName: " + roleName + " Exist: " + isExist + " in the tenantDomain: " + tenantDomain);
        }
        return isExist;
    }

    /**
     * Get usernames by IDs.
     *
     * @param userIDs      User IDs.
     * @param tenantDomain Tenant Domain.
     * @return List of usernames.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    private List<String> getUserNamesByIDs(List<String> userIDs, String tenantDomain)
            throws IdentityRoleManagementException {

        return userIDResolver.getNamesByIDs(userIDs, tenantDomain);
    }

    /**
     * Get group names by IDs.
     *
     * @param groupIDs     Group IDs.
     * @param tenantDomain Tenant Domain.
     * @return group names.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    private Map<String, String> getGroupNamesByIDs(List<String> groupIDs, String tenantDomain)
            throws IdentityRoleManagementException {

        return groupIDResolver.getNamesByIDs(groupIDs, tenantDomain);
    }

    /**
     * Process Batch Update For Users.
     *
     * @param roleName              Group IDs.
     * @param audienceRefId         Audience ref ID.
     * @param userNamesList         Username list
     * @param tenantId              Tenant ID.
     * @param primaryDomainName     Primary domain name.
     * @param connection            Connection.
     * @param removeUserFromRoleSql removeUserFromRole SQL query.
     * @throws SQLException SQLException.
     */
    private void processBatchUpdateForUsers(String roleName, int audienceRefId, List<String> userNamesList,
                                            int tenantId, String primaryDomainName, Connection connection,
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
                statement.setInt(RoleConstants.RoleTableColumns.UM_AUDIENCE_REF_ID, audienceRefId);
                statement.setString(RoleConstants.RoleTableColumns.UM_DOMAIN_NAME, domainName);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    /**
     * Process Batch Update For Groups.
     *
     * @param roleName          Group IDs.
     * @param audienceRefId     Audience ref ID.
     * @param groupNamesList    Group name list
     * @param tenantId          Tenant ID.
     * @param primaryDomainName Primary domain name.
     * @param connection        Connection.
     * @param sql               SQL query.
     * @throws SQLException SQLException.
     */
    private void processBatchUpdateForGroups(String roleName, int audienceRefId, List<String> groupNamesList,
                                             int tenantId, String primaryDomainName, Connection connection, String sql)
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
                statement.setInt(RoleConstants.RoleTableColumns.UM_AUDIENCE_REF_ID, audienceRefId);
                statement.setString(RoleConstants.RoleTableColumns.UM_DOMAIN_NAME, domainName);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    /**
     * Clear User Roles Cache.
     *
     * @param usernameWithDomain Group IDs.
     * @param tenantId           Tenant ID.
     */
    private void clearUserRolesCache(String usernameWithDomain, int tenantId) {

        String userStoreDomain = IdentityUtil.extractDomainFromName(usernameWithDomain);
        if (isUserRoleCacheEnabled(tenantId, userStoreDomain)) {
            UserRolesCache.getInstance().clearCacheEntry(getCacheIdentifier(tenantId, userStoreDomain),
                    tenantId, usernameWithDomain);
        }
        AuthorizationCache authorizationCache = AuthorizationCache.getInstance();
        authorizationCache.clearCacheByUser(tenantId, usernameWithDomain);
    }

    /**
     * Check User Role Cache Enabled.
     *
     * @param userStoreDomain User store domain.
     * @param tenantId        Tenant ID.
     * @return is user role cache enabled
     */
    private boolean isUserRoleCacheEnabled(int tenantId, String userStoreDomain) {

        return Boolean.parseBoolean(getUserStoreProperty(
                UserCoreConstants.RealmConfig.PROPERTY_ROLES_CACHE_ENABLED, tenantId, userStoreDomain));
    }

    /**
     * Get Cache Identifier.
     *
     * @param userStoreDomain User store domain.
     * @param tenantId        Tenant ID.
     * @return cache identifier
     */
    private String getCacheIdentifier(int tenantId, String userStoreDomain) {

        String userCoreCacheIdentifier = getUserStoreProperty(
                UserCoreConstants.RealmConfig.PROPERTY_USER_CORE_CACHE_IDENTIFIER, tenantId, userStoreDomain);

        if (StringUtils.isNotBlank(userCoreCacheIdentifier)) {
            return userCoreCacheIdentifier;
        }

        return UserCoreConstants.DEFAULT_CACHE_IDENTIFIER;
    }

    /**
     * Get User Store Property.
     *
     * @param property        Property
     * @param userStoreDomain User store domain.
     * @param tenantId        Tenant ID.
     * @return user store property
     */
    private String getUserStoreProperty(String property, int tenantId, String userStoreDomain) {

        RealmService realmService = RoleManagementServiceComponentHolder.getInstance().getRealmService();
        String propValue = null;
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
            LOG.error(String.format("Error while retrieving property %s for user store %s in tenantId %s. " +
                    "Returning null.", property, userStoreDomain, tenantId), e);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("User store property %s is set to %s for user store %s in tenantId %s",
                    property, propValue, userStoreDomain, tenantId));
        }
        return propValue;
    }

    /**
     * Get Role Audience name.
     *
     * @param audience     Audience.
     * @param audienceId   Audience ID.
     * @param tenantDomain Tenant Domain.
     * @return role audience name.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    private String getAudienceName(String audience, String audienceId, String tenantDomain)
            throws IdentityRoleManagementException {

        if (ORGANIZATION.equalsIgnoreCase(audience)) {
            return getOrganizationName(audienceId);
        }
        return null;
    }

    /**
     * Get organization name.
     *
     * @param organizationId Organization ID.n.
     * @return organization name.
     * @throws IdentityRoleManagementServerException IdentityRoleManagementServerException.
     */
    private String getOrganizationName(String organizationId) throws IdentityRoleManagementServerException {

        try {
            return RoleManagementServiceComponentHolder.getInstance().getOrganizationManager()
                    .getOrganizationNameById(organizationId);
        } catch (OrganizationManagementException e) {
            if (ERROR_CODE_INVALID_ORGANIZATION_ID.getCode().equals(e.getErrorCode())) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Returning an empty string as the organization name as the " +
                            "name is not returned for the given id :" + organizationId);
                }
                return StringUtils.EMPTY;
            }
            String errorMessage = "Error while retrieving the organization name for the given id: " + organizationId;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
    }

    /**
     * Get organization id by tenant domain.
     *
     * @param tenantDomain Tenant domain.
     * @return organization id.
     * @throws IdentityRoleManagementServerException IdentityRoleManagementServerException.
     */
    private String getOrganizationId(String tenantDomain) throws IdentityRoleManagementServerException {

        try {
            return RoleManagementServiceComponentHolder.getInstance().getOrganizationManager()
                    .resolveOrganizationId(tenantDomain);
        } catch (OrganizationManagementException e) {
            String errorMessage = "Error while retrieving the organization id for the tenant domain: " + tenantDomain;
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
            if (LOG.isDebugEnabled()) {
                LOG.debug("Given limit is null. Therefore assigning the default limit: " + limit);
            }
        } else if (limit < 0) {
            String errorMessage =
                    "Invalid limit requested. Limit value should be greater than or equal to zero. limit: " + limit;
            throw new IdentityRoleManagementClientException(INVALID_LIMIT.getCode(), errorMessage);
        } else if (limit > maximumItemsPerPage) {
            limit = maximumItemsPerPage;
            if (LOG.isDebugEnabled()) {
                LOG.debug("Given limit exceed the maximum limit. Therefore assigning the maximum limit: "
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
            Therefore, we are converting it to a zero based start index here. */
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

    /**
     * Get type specific role retrieval query.
     *
     * @param databaseProductName DB type.
     * @return sql query.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
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

    /**
     * Get type specific role retrieval query with filter.
     *
     * @param databaseProductName DB type.
     * @param filterQuery         Filter query.
     * @return sql query.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    private String getDBTypeSpecificRolesRetrievalQueryByFilter(String databaseProductName, String filterQuery)
            throws IdentityRoleManagementException {

        if (RoleConstants.MY_SQL.equals(databaseProductName)
                || RoleConstants.MARIADB.equals(databaseProductName)
                || RoleConstants.H2.equals(databaseProductName)) {
            return GET_ROLES_BY_TENANT_AND_ROLE_NAME_MYSQL + filterQuery + GET_ROLES_BY_TENANT_AND_ROLE_NAME_TAIL_MYSQL;
        } else if (RoleConstants.ORACLE.equals(databaseProductName)) {
            return GET_ROLES_BY_TENANT_AND_ROLE_NAME_ORACLE + filterQuery +
                    GET_ROLES_BY_TENANT_AND_ROLE_NAME_TAIL_ORACLE;
        } else if (RoleConstants.MICROSOFT.equals(databaseProductName)) {
            return GET_ROLES_BY_TENANT_AND_ROLE_NAME_MSSQL + filterQuery + GET_ROLES_BY_TENANT_AND_ROLE_NAME_TAIL_MSSQL;
        } else if (RoleConstants.POSTGRE_SQL.equals(databaseProductName)) {
            return GET_ROLES_BY_TENANT_AND_ROLE_NAME_POSTGRESQL + filterQuery +
                    GET_ROLES_BY_TENANT_AND_ROLE_NAME_TAIL_POSTGRESQL;
        } else if (databaseProductName != null && databaseProductName.contains(RoleConstants.DB2)) {
            return GET_ROLES_BY_TENANT_AND_ROLE_NAME_DB2 + filterQuery + GET_ROLES_BY_TENANT_AND_ROLE_NAME_TAIL_DB2;
        } else if (RoleConstants.INFORMIX.equals(databaseProductName)) {
            return GET_ROLES_BY_TENANT_AND_ROLE_NAME_INFORMIX + filterQuery +
                    GET_ROLES_BY_TENANT_AND_ROLE_NAME_TAIL_INFORMIX;
        }

        throw new IdentityRoleManagementServerException(RoleConstants.Error.UNEXPECTED_SERVER_ERROR.getCode(),
                "Error while listing roles from DB. Database driver for " + databaseProductName
                        + "could not be identified or not supported.");
    }

    /**
     * Get type specific role count query.
     *
     * @param databaseProductName DB type.
     * @return sql query.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    private String getDBTypeSpecificRolesCountQuery(String databaseProductName) throws IdentityRoleManagementException {

        if (RoleConstants.MY_SQL.equals(databaseProductName)
                || RoleConstants.MARIADB.equals(databaseProductName)
                || RoleConstants.H2.equals(databaseProductName)) {
            return COUNT_ROLES_BY_TENANT_MYSQL;
        } else if (RoleConstants.ORACLE.equals(databaseProductName)) {
            return COUNT_ROLES_BY_TENANT_ORACLE;
        } else if (RoleConstants.MICROSOFT.equals(databaseProductName)) {
            return COUNT_ROLES_BY_TENANT_MSSQL;
        } else if (RoleConstants.POSTGRE_SQL.equals(databaseProductName)) {
            return COUNT_ROLES_BY_TENANT_POSTGRESQL;
        } else if (databaseProductName != null && databaseProductName.contains(RoleConstants.DB2)) {
            return COUNT_ROLES_BY_TENANT_DB2;
        } else if (RoleConstants.INFORMIX.equals(databaseProductName)) {
            return COUNT_ROLES_BY_TENANT_INFORMIX;
        }

        throw new IdentityRoleManagementServerException(RoleConstants.Error.UNEXPECTED_SERVER_ERROR.getCode(),
                "Error while counting roles from DB. Database driver for " + databaseProductName
                        + "could not be identified or not supported.");
    }

    /**
     * Retrieves the type-specific SQL query for fetching a user list by role.
     *
     * @param databaseProductName DB type.
     * @return SQL query.
     * @throws IdentityRoleManagementException If the database type is unsupported.
     */
    private String getDBTypeSpecificUserListByRoleQuery(String databaseProductName)
            throws IdentityRoleManagementException {

        if (MY_SQL.equals(databaseProductName)
                || MARIADB.equals(databaseProductName)
                || POSTGRE_SQL.equals(databaseProductName)
                || H2.equals(databaseProductName)) {
            return GET_LIMITED_USER_LIST_OF_ROLE_SQL;
        } else if (databaseProductName != null && databaseProductName.contains(RoleConstants.DB2)) {
            return GET_LIMITED_USER_LIST_OF_ROLE_DB2;
        } else if (ORACLE.equals(databaseProductName)) {
            return GET_LIMITED_USER_LIST_OF_ROLE_ORACLE;
        } else if (MICROSOFT.equals(databaseProductName)) {
            return GET_LIMITED_USER_LIST_OF_ROLE_MSSQL;
        }

        throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                "Error while listing users by role from DB. Database driver for " + databaseProductName
                        + " could not be identified or not supported.");
    }

    /**
     * Process list of roles query.
     *
     * @param limit        Limit.
     * @param offset       Offset.
     * @param statement    Statement.
     * @param tenantDomain Tenant domain.
     * @return list of role basic info.
     * @throws SQLException SQLException.
     */
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

    /**
     * Build role list.
     *
     * @param statement    Statement.
     * @param tenantDomain Tenant domain.
     * @return list of role basic info.
     * @throws SQLException SQLException.
     */
    private List<RoleBasicInfo> buildRolesList(NamedPreparedStatement statement, String tenantDomain)
            throws SQLException, IdentityRoleManagementException {

        List<RoleBasicInfo> roles = new ArrayList<>();
        List<RoleDTO> roleDTOs = new ArrayList<>();
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                String roleName = resultSet.getString(1);
                String roleId = resultSet.getString(2);
                RoleDTO roleDTO = new RoleDTO(appendInternalDomain(roleName), resultSet.getInt(5));
                roleDTO.setId(roleId);
                roleDTO.setRoleAudience(new RoleAudience(resultSet.getString(3),
                        resultSet.getString(4)));
                roleDTOs.add(roleDTO);
            }
        }

        for (RoleDTO roleDTO : roleDTOs) {
            RoleBasicInfo roleBasicInfo = new RoleBasicInfo(roleDTO.getId(), removeInternalDomain(roleDTO.getName()));
            RoleAudience roleAudience = roleDTO.getRoleAudience();
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


    @Override
    public String getRoleNameByID(String roleId, String tenantDomain) throws IdentityRoleManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        String roleName = null;
        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(false);
             NamedPreparedStatement statement = new NamedPreparedStatement(connection, GET_ROLE_NAME_BY_ID_SQL)) {

            statement.setInt(RoleConstants.RoleTableColumns.UM_TENANT_ID, tenantId);
            statement.setString(RoleConstants.RoleTableColumns.UM_UUID, roleId);
            int count = 0;
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    // Handle multiple matching roles.
                    count++;
                    if (count > 1) {
                        String message = "Invalid scenario. Multiple roles found for the given role ID: " + roleId
                                + " and tenantDomain: " + tenantDomain;
                        LOG.warn(message);
                    }
                    roleName = resultSet.getString(1);
                }
            }
        } catch (SQLException e) {
            String errorMessage =
                    "Error while resolving the role name for the given role ID: " + roleId + " and tenantDomain: "
                            + tenantDomain;
            throw new IdentityRoleManagementServerException(RoleConstants.Error.UNEXPECTED_SERVER_ERROR.getCode(),
                    errorMessage, e);
        }
        if (roleName == null) {
            String errorMessage = "A role doesn't exist with id: " + roleId + " in the tenantDomain: " + tenantDomain;
            throw new IdentityRoleManagementClientException(ROLE_NOT_FOUND.getCode(), errorMessage);
        }
        return removeInternalDomain(roleName);
    }

    @Override
    public String getRoleIdByName(String roleName, String audience, String audienceId, String tenantDomain)
            throws IdentityRoleManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        String roleId = null;
        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(true);
             NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                     GET_ROLE_ID_BY_NAME_AND_AUDIENCE_SQL)) {
            statement.setString(RoleConstants.RoleTableColumns.UM_ROLE_NAME, removeInternalDomain(roleName));
            statement.setInt(RoleConstants.RoleTableColumns.UM_TENANT_ID, tenantId);
            statement.setString(RoleConstants.RoleTableColumns.UM_AUDIENCE, audience);
            statement.setString(RoleConstants.RoleTableColumns.UM_AUDIENCE_ID, audienceId);
            int count = 0;
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    // Handle multiple matching roles.
                    count++;
                    if (count > 1) {
                        String errorMessage =
                                "Invalid scenario. Multiple roles found for the given role name: " + roleName
                                        + " and tenantDomain: " + tenantDomain;
                        throw new IdentityRoleManagementClientException(
                                RoleConstants.Error.INVALID_REQUEST.getCode(), errorMessage);
                    }
                    roleId = resultSet.getString(1);
                }
            }
        } catch (SQLException e) {
            String errorMessage = "Error while retrieving the role id by name :" + roleName + " audience :" + audience
                    + " audienceId :" + audienceId;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                    String.format(errorMessage, roleName, tenantDomain), e);
        }
        if (roleId == null) {
            String errorMessage =
                    "A role doesn't exist with name: " + roleName + " in the tenantDomain: " + tenantDomain;
            throw new IdentityRoleManagementClientException(RoleConstants.Error.INVALID_REQUEST.getCode(),
                    errorMessage);
        }
        return roleId;
    }

    /**
     * Get role audience by role ID.
     *
     * @param roleId       Role ID.
     * @param tenantDomain Tenant domain.
     * @return role audience
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    private RoleAudience getAudienceByRoleID(String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

        RoleAudience roleAudience;
        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(false);
             NamedPreparedStatement statement = new NamedPreparedStatement(connection, GET_AUDIENCE_BY_ID_SQL)) {

            statement.setString(RoleConstants.RoleTableColumns.UM_UUID, roleId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    roleAudience = new RoleAudience(resultSet.getString(1),
                            resultSet.getString(2));
                    roleAudience.setAudienceName(getAudienceName(roleAudience.getAudience(),
                            roleAudience.getAudienceId(), tenantDomain));
                    return roleAudience;
                } else {
                    String errorMessage =
                            "Error while retrieving audience by role ID: " + roleId + " and tenantDomain: "
                                    + tenantDomain;
                    throw new IdentityRoleManagementServerException(RoleConstants.Error.UNEXPECTED_SERVER_ERROR
                            .getCode(), errorMessage);
                }
            }
        } catch (SQLException e) {
            String errorMessage =
                    "Error while resolving the audience ref for the given role ID: " + roleId + " and tenantDomain: "
                            + tenantDomain;
            throw new IdentityRoleManagementServerException(RoleConstants.Error.UNEXPECTED_SERVER_ERROR.getCode(),
                    errorMessage, e);
        }
    }

    private int getAudienceRefByID(String roleId, String tenantDomain) throws IdentityRoleManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        int refId;
        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(false);
             NamedPreparedStatement statement = new NamedPreparedStatement(
                     connection, GET_AUDIENCE_REF_BY_ID_FROM_UM_HYBRID_ROLE_SQL)) {

            statement.setInt(RoleConstants.RoleTableColumns.UM_TENANT_ID, tenantId);
            statement.setString(RoleConstants.RoleTableColumns.UM_UUID, roleId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    refId = resultSet.getInt(1);
                } else {
                    String errorMessage =
                            "Error while retrieving audience ref id by role ID: " + roleId + " and tenantDomain: "
                                    + tenantDomain;
                    throw new IdentityRoleManagementServerException(RoleConstants.Error.UNEXPECTED_SERVER_ERROR
                            .getCode(), errorMessage);
                }
            }
        } catch (SQLException e) {
            String errorMessage =
                    "Error while resolving the role name for the given role ID: " + roleId + " and tenantDomain: "
                            + tenantDomain;
            throw new IdentityRoleManagementServerException(RoleConstants.Error.UNEXPECTED_SERVER_ERROR.getCode(),
                    errorMessage, e);
        }
        // Verify whether the roleName is either null or it's not contain any prefix Application/Internal
        if (refId == -2) {
            String errorMessage = "A role audience ref id doesn't exist with id: " + roleId + " in the tenantDomain: "
                    + tenantDomain;
            throw new IdentityRoleManagementClientException(ROLE_NOT_FOUND.getCode(), errorMessage);
        }
        return refId;
    }

    @Override
    public List<UserBasicInfo> getUserListOfRole(String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

        if (!isExistingRoleID(roleId, tenantDomain)) {
            throw new IdentityRoleManagementClientException(ROLE_NOT_FOUND.getCode(),
                    "Role id: " + roleId + " does not exist in the system.");
        }
        List<UserBasicInfo> userList = new ArrayList<>();
        String roleName = getRoleNameByID(roleId, tenantDomain);
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        int limit = IdentityUtil.getMaximumUsersListPerRole();
        int offset = 0;
        try {
            UserRealm userRealm = CarbonContext.getThreadLocalCarbonContext().getUserRealm();
            if (UserCoreUtil.isEveryoneRole(roleName, userRealm.getRealmConfiguration())) {
                List<org.wso2.carbon.user.core.common.User> users = ((AbstractUserStoreManager) userRealm
                        .getUserStoreManager()).listUsersWithID(RoleConstants.WILDCARD_CHARACTER, limit);
                for (org.wso2.carbon.user.core.common.User user : users) {
                    userList.add(new UserBasicInfo(user.getUserID(), user.getDomainQualifiedUsername()));
                }
            }
        } catch (UserStoreException e) {
            throw new IdentityRoleManagementServerException(RoleConstants.Error.UNEXPECTED_SERVER_ERROR.getCode(),
                    "Error while getting the realmConfiguration.", e);
        }

        List<String> disabledDomainName = getDisabledDomainNames();
        int audienceRefId = getAudienceRefByID(roleId, tenantDomain);
        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(false)) {
            String databaseProductName = connection.getMetaData().getDatabaseProductName();
            String query = getDBTypeSpecificUserListByRoleQuery(databaseProductName);

            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, query)) {
                statement.setString(RoleConstants.RoleTableColumns.UM_ROLE_NAME, roleName);
                statement.setInt(RoleConstants.RoleTableColumns.UM_TENANT_ID, tenantId);
                statement.setInt(RoleConstants.RoleTableColumns.UM_AUDIENCE_REF_ID, audienceRefId);
                statement.setInt(RoleConstants.LIMIT, limit);
                statement.setInt(RoleConstants.OFFSET, offset);
                statement.setInt(RoleConstants.ZERO_BASED_START_INDEX, offset);
                statement.setInt(RoleConstants.END_INDEX, offset + limit);
                statement.setInt(RoleConstants.ONE_BASED_START_INDEX, offset + 1);

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
                                    if (LOG.isDebugEnabled()) {
                                        LOG.debug(errorMessage);
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
                    "Error while getting the user list of role for role name: %s in the tenantDomain: %s";
            throw new IdentityRoleManagementServerException(RoleConstants.Error.UNEXPECTED_SERVER_ERROR.getCode(),
                    String.format(errorMessage, roleName, tenantDomain), e);
        }
        return userList;
    }

    @Override
    public void updateGroupListOfRole(String roleId, List<String> newGroupIDList,
                                      List<String> deletedGroupIDList, String tenantDomain)
            throws IdentityRoleManagementException {

        if (!isExistingRoleID(roleId, tenantDomain)) {
            throw new IdentityRoleManagementClientException(ROLE_NOT_FOUND.getCode(),
                    "Role id: " + roleId + " does not exist in the system.");
        }
        String roleName = getRoleNameByID(roleId, tenantDomain);
        // Validate the group removal operation based on the default system roles.
        validateGroupRemovalFromRole(deletedGroupIDList, roleName, tenantDomain);
        if (CollectionUtils.isEmpty(newGroupIDList) && CollectionUtils.isEmpty(deletedGroupIDList)) {
            LOG.debug("Group lists are empty.");
            return;
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
        int audienceRefId = getAudienceRefByID(roleId, tenantDomain);
        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(true)) {
            try {
                // Add new groups to the role.
                String addGroupsSQL = ADD_GROUP_TO_ROLE_SQL;
                String databaseProductName = connection.getMetaData().getDatabaseProductName();
                if (RoleConstants.MICROSOFT.equals(databaseProductName)) {
                    addGroupsSQL = SQLQueries.ADD_GROUP_TO_ROLE_SQL_MSSQL;
                }
                processBatchUpdateForGroups(roleName, audienceRefId, newGroupNamesList, tenantId, primaryDomainName,
                        connection, addGroupsSQL);

                // Delete existing groups from the role.
                processBatchUpdateForGroups(roleName, audienceRefId, deletedGroupNamesList, tenantId,
                        primaryDomainName, connection, REMOVE_GROUP_FROM_ROLE_SQL);

                IdentityDatabaseUtil.commitUserDBTransaction(connection);
            } catch (SQLException e) {
                IdentityDatabaseUtil.rollbackUserDBTransaction(connection);
                String errorMessage = "Error while updating groups to the role: %s in the tenantDomain: %s";
                throw new IdentityRoleManagementServerException(RoleConstants.Error.UNEXPECTED_SERVER_ERROR.getCode(),
                        String.format(errorMessage, roleName, tenantDomain), e);
            }
        } catch (SQLException e) {
            String errorMessage = "Error while updating groups to the role: %s in the tenantDomain: %s";
            throw new IdentityRoleManagementServerException(RoleConstants.Error.UNEXPECTED_SERVER_ERROR.getCode(),
                    String.format(errorMessage, roleName, tenantDomain), e);
        }
        clearUserRolesCacheByTenant(tenantId);
    }

    @Override
    public boolean isExistingRoleID(String roleId, String tenantDomain) throws IdentityRoleManagementException {

        boolean isExist = false;
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(false);
             NamedPreparedStatement statement = new NamedPreparedStatement(
                     connection, IS_ROLE_ID_EXIST_FROM_UM_HYBRID_ROLE_SQL)) {

            statement.setInt(RoleConstants.RoleTableColumns.UM_TENANT_ID, tenantId);
            statement.setString(RoleConstants.RoleTableColumns.UM_UUID, roleId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    isExist = resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            String errorMessage = "Error while checking is existing role for role id: %s in the tenantDomain: %s";
            throw new IdentityRoleManagementServerException(RoleConstants.Error.UNEXPECTED_SERVER_ERROR.getCode(),
                    String.format(errorMessage, roleId, tenantDomain), e);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Is roleId: " + roleId + " Exist: " + isExist + " in the tenantDomain: " + tenantDomain);
        }
        return isExist;
    }

    /**
     * Get the disabled domain names.
     *
     * @return disabled domain names.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
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

    /**
     * Get user ID by name.
     *
     * @param name         Username.
     * @param tenantDomain Tenant domain.
     * @return user ID.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    private String getUserIDByName(String name, String tenantDomain) throws IdentityRoleManagementException {

        return userIDResolver.getIDByName(name, tenantDomain);
    }

    @Override
    public List<GroupBasicInfo> getGroupListOfRole(String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

        if (!isExistingRoleID(roleId, tenantDomain)) {
            throw new IdentityRoleManagementClientException(ROLE_NOT_FOUND.getCode(),
                    "Role id: " + roleId + " does not exist in the system.");
        }
        String roleName = getRoleNameByID(roleId, tenantDomain);
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        List<GroupBasicInfo> groupList = new ArrayList<>();
        List<String> groupNames = new ArrayList<>();
        List<String> disabledDomainName = getDisabledDomainNames();

        String primaryDomainName = IdentityUtil.getPrimaryDomainName();
        if (primaryDomainName != null) {
            primaryDomainName = primaryDomainName.toUpperCase(Locale.ENGLISH);
        }
        int audienceRefId = getAudienceRefByID(roleId, tenantDomain);
        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(false);
             NamedPreparedStatement statement = new NamedPreparedStatement(connection, GET_GROUP_LIST_OF_ROLE_SQL)) {

            statement.setString(RoleConstants.RoleTableColumns.UM_ROLE_NAME, roleName);
            statement.setInt(RoleConstants.RoleTableColumns.UM_TENANT_ID, tenantId);
            statement.setInt(RoleConstants.RoleTableColumns.UM_AUDIENCE_REF_ID, audienceRefId);
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

    @Override
    public void updateUserListOfRole(String roleId, List<String> newUserIDList, List<String> deletedUserIDList,
                                     String tenantDomain) throws IdentityRoleManagementException {

        if (!isExistingRoleID(roleId, tenantDomain)) {
            throw new IdentityRoleManagementClientException(RoleConstants.Error.ROLE_NOT_FOUND.getCode(),
                    "Role id: " + roleId + " does not exist in the system.");
        }
        String roleName = getRoleNameByID(roleId, tenantDomain);
        if (CollectionUtils.isEmpty(newUserIDList) && CollectionUtils.isEmpty(deletedUserIDList)) {
            LOG.debug("User lists are empty.");
            return;
        }

        String primaryDomainName = IdentityUtil.getPrimaryDomainName();
        if (primaryDomainName != null) {
            primaryDomainName = primaryDomainName.toUpperCase(Locale.ENGLISH);
        }

        List<String> newUserNamesList = getUserNamesByIDs(newUserIDList, tenantDomain);
        List<String> deletedUserNamesList = getUserNamesByIDs(deletedUserIDList, tenantDomain);
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        int audienceRefId = getAudienceRefByID(roleId, tenantDomain);
        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(true)) {

            try {
                // Add new users to the role.
                String addUsersSQL = SQLQueries.ADD_USER_TO_ROLE_SQL;
                String databaseProductName = connection.getMetaData().getDatabaseProductName();
                if (RoleConstants.MICROSOFT.equals(databaseProductName)) {
                    addUsersSQL = SQLQueries.ADD_USER_TO_ROLE_SQL_MSSQL;
                }
                processBatchUpdateForUsers(roleName, audienceRefId, newUserNamesList, tenantId, primaryDomainName,
                        connection, addUsersSQL);

                // Delete existing users from the role.
                processBatchUpdateForUsers(roleName, audienceRefId, deletedUserNamesList, tenantId, primaryDomainName,
                        connection, REMOVE_USER_FROM_ROLE_SQL);

                IdentityDatabaseUtil.commitUserDBTransaction(connection);
            } catch (SQLException e) {
                IdentityDatabaseUtil.rollbackUserDBTransaction(connection);
                String errorMessage = "Error while updating users to the role: %s in the tenantDomain: %s";
                throw new IdentityRoleManagementServerException(RoleConstants.Error.UNEXPECTED_SERVER_ERROR.getCode(),
                        String.format(errorMessage, roleName, tenantDomain), e);
            }
        } catch (SQLException e) {
            String errorMessage = "Error while updating users to the role: %s in the tenantDomain: %s";
            throw new IdentityRoleManagementServerException(RoleConstants.Error.UNEXPECTED_SERVER_ERROR.getCode(),
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
    }

    /**
     * Get group IDs by names.
     *
     * @param names        Group names.
     * @param tenantDomain Tenant domain.
     * @return group ID.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    private Map<String, String> getGroupIDsByNames(List<String> names, String tenantDomain)
            throws IdentityRoleManagementException {

        return groupIDResolver.getIDsByNames(names, tenantDomain);
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
            if (LOG.isDebugEnabled()) {
                LOG.debug("'" + IdentityConstants.SystemRoles.SYSTEM_ROLES_CONFIG_ELEMENT
                        + "' config cannot be found.");
            }
            return Collections.emptySet();
        }

        Iterator roleIdentifierIterator = systemRolesConfig
                .getChildrenWithLocalName(IdentityConstants.SystemRoles.ROLE_CONFIG_ELEMENT);
        if (roleIdentifierIterator == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("'" + IdentityConstants.SystemRoles.ROLE_CONFIG_ELEMENT + "' config cannot be found.");
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
     * Validate group removal from role.
     *
     * @param deletedGroupIDList Deleted group id list.
     * @param roleName           Role name.
     * @param tenantDomain       Tenant domain.
     * @throws IdentityRoleManagementException Error occurred while validating group removal from role.
     */
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
            throw new IdentityRoleManagementServerException(RoleConstants.Error.UNEXPECTED_SERVER_ERROR.getCode(),
                    String.format(errorMessage, roleName, tenantDomain), e);
        }
    }

    /**
     * Validate groups.
     *
     * @param groups Groups.
     * @throws IdentityRoleManagementException Error occurred while validating idp groups.
     */
    public void validateGroupIds(List<IdpGroup> groups, String tenantDomain)
            throws IdentityRoleManagementException {

        for (IdpGroup group : groups) {

            IdentityProvider identityProvider = getIdpById(group.getIdpId(), tenantDomain);
            if (identityProvider == null) {
                throw new IdentityRoleManagementException("Idp not found.",
                        "Idp not found for id : " + group.getIdpId());
            }
            IdPGroup[] idpGroups = identityProvider.getIdPGroupConfig();
            List<String> idpGroupIdList = new ArrayList<>();
            for (IdPGroup idpGroup : idpGroups) {
                idpGroupIdList.add(idpGroup.getIdpGroupId());
            }
            if (!idpGroupIdList.contains(group.getGroupId())) {
                throw new IdentityRoleManagementException("Idp group not found.",
                        "Idp group not found for id : " + group.getGroupId());
            }
        }
    }

    /**
     * Get idp by id.
     *
     * @param idpId        IDP ID.
     * @param tenantDomain Tenant domain.
     * @throws IdentityRoleManagementException Error occurred while retrieving idp by id.
     */
    private IdentityProvider getIdpById(String idpId, String tenantDomain)
            throws IdentityRoleManagementException {

        IdentityProvider identityProvider;
        try {
            identityProvider = RoleManagementServiceComponentHolder.getInstance()
                    .getIdentityProviderManager().getIdPByResourceId(idpId, tenantDomain, true);
        } catch (IdentityProviderManagementException e) {
            throw new IdentityRoleManagementException("Error while retrieving idp", "Error while retrieving idp "
                    + "for idpId: " + idpId, e);
        }
        return identityProvider;
    }

    /**
     * Get idp groups by id.
     *
     * @param idpGroupIds  Idp group ids.
     * @param tenantDomain Tenant domain.
     * @throws IdentityRoleManagementException Error occurred while retrieving idp groups by id.
     */
    private List<IdPGroup> getIdpGroupsByIds(List<String> idpGroupIds, String tenantDomain)
            throws IdentityRoleManagementException {

        List<IdPGroup> idpGroups;
        try {
            idpGroups = RoleManagementServiceComponentHolder.getInstance()
                    .getIdentityProviderManager().getValidIdPGroupsByIdPGroupIds(idpGroupIds, tenantDomain);
        } catch (IdentityProviderManagementException e) {
            throw new IdentityRoleManagementException("Error while retrieving idp groups", "Error while retrieving idp "
                    + "groups for idp Ids: " + idpGroupIds, e);
        }
        return idpGroups;
    }

    /**
     * Delete SCIM role.
     *
     * @param roleId            Role ID.
     * @param hybridSharedRoles list of roles
     * @param tenantDomain      Tenant domain.
     * @throws IdentityRoleManagementException Error occurred while deleting SCIM role.
     */
    private void deleteSCIMRole(String roleId, String roleName, int audienceRefId, List<RoleDTO> hybridSharedRoles,
                                String tenantDomain)
            throws IdentityRoleManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        // Append internal domain in order to maintain the backward compatibility.
        roleName = appendInternalDomain(roleName);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Deleting the role id: " + roleId + " in the tenantDomain: "
                    + tenantDomain);
        }

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(true)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, DELETE_SCIM_ROLE_SQL)) {
                statement.setInt(RoleConstants.RoleTableColumns.TENANT_ID, tenantId);
                statement.setString(ROLE_NAME, roleName);
                statement.setInt(RoleConstants.RoleTableColumns.AUDIENCE_REF_ID, audienceRefId);
                statement.executeUpdate();
                if (hybridSharedRoles != null && !hybridSharedRoles.isEmpty()) {
                    deleteSharedSCIMRoles(hybridSharedRoles, connection);
                }
                deleteRoleAssociations(roleId, connection);

                IdentityDatabaseUtil.commitTransaction(connection);
            } catch (SQLException | IdentityRoleManagementException e) {
                IdentityDatabaseUtil.rollbackTransaction(connection);
                String errorMessage = "Error while deleting the role: %s for the role: %s in the tenantDomain: %s";
                throw new IdentityRoleManagementServerException(RoleConstants.Error.UNEXPECTED_SERVER_ERROR.getCode(),
                        String.format(errorMessage, roleName, roleName, tenantDomain), e);
            }
        } catch (SQLException e) {
            String errorMessage = "Error while deleting the role: %s for the role: %s in the tenantDomain: %s";
            throw new IdentityRoleManagementServerException(RoleConstants.Error.UNEXPECTED_SERVER_ERROR.getCode(),
                    String.format(errorMessage, roleName, roleName, tenantDomain), e);
        }
    }

    /**
     * Delete shared hybrid roles.
     *
     * @param roleId       Main role ID.
     * @param mainTenantId Main tenant ID.
     * @param connection   DB connection.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    private void deleteSharedHybridRoles(String roleId, int mainTenantId, Connection connection)
            throws IdentityRoleManagementException {

        try {
            try (NamedPreparedStatement selectStatement =
                         new NamedPreparedStatement(connection, GET_SHARED_HYBRID_ROLE_WITH_MAIN_ROLE_SQL)) {
                selectStatement.setInt(RoleConstants.RoleTableColumns.UM_TENANT_ID, mainTenantId);
                selectStatement.setString(RoleConstants.RoleTableColumns.UM_UUID, roleId);

                List<Map.Entry<Integer, Integer>> idsToDelete = new ArrayList<>();
                try (ResultSet resultSet = selectStatement.executeQuery()) {
                    while (resultSet.next()) {
                        idsToDelete.add(new AbstractMap.SimpleEntry<>(
                                resultSet.getInt(1), resultSet.getInt(2)));
                    }
                }

                try (NamedPreparedStatement deleteStatement = new NamedPreparedStatement(connection,
                        DELETE_SHARED_ROLE)) {
                    for (Map.Entry<Integer, Integer> idPair : idsToDelete) {
                        deleteStatement.setInt(RoleConstants.RoleTableColumns.UM_ID, idPair.getKey());
                        deleteStatement.setInt(RoleConstants.RoleTableColumns.UM_TENANT_ID, idPair.getValue());
                        deleteStatement.addBatch();
                    }
                    deleteStatement.executeBatch();
                }
            }
        } catch (SQLException e) {
            String errorMessage = "Error while deleting shared roles of role id : " + roleId;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
    }

    /**
     * Delete shared hybrid roles.
     *
     * @param roleId       Main role Id.
     * @param mainTenantId Main tenant ID.
     * @param connection   DB connection.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    private List<RoleDTO> getSharedHybridRoles(String roleId, int mainTenantId,
                                               Connection connection) throws IdentityRoleManagementException {

        List<RoleDTO> hybridRoles = new ArrayList<>();
        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                GET_SHARED_ROLES_SQL)) {
            statement.setInt(RoleConstants.RoleTableColumns.UM_TENANT_ID, mainTenantId);
            statement.setString(RoleConstants.RoleTableColumns.UM_UUID, roleId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String name = resultSet.getString(1);
                    int tenantId = resultSet.getInt(2);
                    int audienceRefId = resultSet.getInt(3);
                    String id = resultSet.getString(4);
                    hybridRoles.add(new RoleDTO(name, id, audienceRefId, tenantId));
                }
            }
        } catch (SQLException e) {
            String errorMessage = "Error while deleting shared roles of role id : " + roleId;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                    errorMessage, e);
        }
        return hybridRoles;
    }

    /**
     * Delete shared scim roles.
     *
     * @param deletedSharedSCIMRoles Deleted shared scim2 roles.
     * @param connection             DB connection.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    private void deleteSharedSCIMRoles(List<RoleDTO> deletedSharedSCIMRoles, Connection connection)
            throws IdentityRoleManagementException {

        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                DELETE_SHARED_SCIM_ROLES_WITH_MAIN_ROLE_SQL)) {
            for (RoleDTO roleDTO : deletedSharedSCIMRoles) {
                statement.setString(RoleConstants.RoleTableColumns.ROLE_NAME, appendInternalDomain(roleDTO.getName()));
                statement.setInt(RoleConstants.RoleTableColumns.TENANT_ID, roleDTO.getTenantId());
                statement.setInt(RoleConstants.RoleTableColumns.AUDIENCE_REF_ID, roleDTO.getAudienceRefId());
                statement.executeUpdate();
            }

        } catch (SQLException e) {
            String errorMessage = "Error while deleting shared scim roles";
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                    errorMessage, e);
        }
    }

    /**
     * Clear user role cache.
     *
     * @param tenantId Tenant ID.
     */
    private void clearUserRolesCacheByTenant(int tenantId) {

        /*
          Ideally we need to check all user stores to see if the UserRolesCache is enabled in at least one user store
          before removing the cache. This needs to be done by iterating over all user stores of the tenant.
          Since this method is triggered only when a role related configuration is changed it is not worth
          it to iterate over the user stores and check whether UserRolesCache is enabled. Therefore, we are simply
          removing the cache so the cache will be removed if it's available.
         */
        UserRolesCache.getInstance().clearCacheByTenant(tenantId);

        AuthorizationCache authorizationCache = AuthorizationCache.getInstance();
        authorizationCache.clearCacheByTenant(tenantId);
    }

    /**
     * Append the filter query to the query builder.
     *
     * @param expressionNodes    List of expression nodes.
     * @param filterQueryBuilder Filter query builder.
     * @throws IdentityRoleManagementException If an error occurs while appending the filter query.
     */
    private void appendFilterQuery(List<ExpressionNode> expressionNodes, FilterQueryBuilder filterQueryBuilder)
            throws IdentityRoleManagementException {

        StringBuilder filter = new StringBuilder();
        if (CollectionUtils.isEmpty(expressionNodes)) {
            filterQueryBuilder.setFilterQuery(StringUtils.EMPTY);
        } else {
            for (ExpressionNode expressionNode : expressionNodes) {
                String operation = expressionNode.getOperation();
                String value = expressionNode.getValue();
                if (ORGANIZATION.equalsIgnoreCase(value)) {
                    value = ORGANIZATION;
                } else if (APPLICATION.equalsIgnoreCase(value)) {
                    value = APPLICATION;
                }
                String attributeValue = expressionNode.getAttributeValue();
                String attributeName = RoleConstants.ATTRIBUTE_COLUMN_MAP.get(attributeValue);

                if (StringUtils.isNotBlank(attributeName) && StringUtils.isNotBlank(value) && StringUtils
                        .isNotBlank(operation)) {
                    switch (operation) {
                        case RoleConstants.EQ: {
                            equalFilterBuilder(value, attributeName, filter, filterQueryBuilder);
                            break;
                        }
                        case RoleConstants.NE: {
                            notEqualFilterBuilder(value, attributeName, filter, filterQueryBuilder);
                            break;
                        }
                        case RoleConstants.SW: {
                            startWithFilterBuilder(value, attributeName, filter, filterQueryBuilder);
                            break;
                        }
                        case RoleConstants.EW: {
                            endWithFilterBuilder(value, attributeName, filter, filterQueryBuilder);
                            break;
                        }
                        case RoleConstants.CO: {
                            containsFilterBuilder(value, attributeName, filter, filterQueryBuilder);
                            break;
                        }
                        case RoleConstants.GE: {
                            greaterThanOrEqualFilterBuilder(value, attributeName, filter, filterQueryBuilder);
                            break;
                        }
                        case RoleConstants.LE: {
                            lessThanOrEqualFilterBuilder(value, attributeName, filter, filterQueryBuilder);
                            break;
                        }
                        case RoleConstants.GT: {
                            greaterThanFilterBuilder(value, attributeName, filter, filterQueryBuilder);
                            break;
                        }
                        case RoleConstants.LT: {
                            lessThanFilterBuilder(value, attributeName, filter, filterQueryBuilder);
                            break;
                        }
                        default: {
                            break;
                        }
                    }
                } else {
                    throw new IdentityRoleManagementClientException(INVALID_REQUEST.getCode(), "Invalid filter");
                }
            }
            if (StringUtils.isBlank(filter.toString())) {
                filterQueryBuilder.setFilterQuery(StringUtils.EMPTY);
            } else {
                filterQueryBuilder.setFilterQuery(filter.toString());
            }
        }
    }

    private void equalFilterBuilder(String value, String attributeName, StringBuilder filter,
                                    FilterQueryBuilder filterQueryBuilder) {

        String filterString = " =:" + attributeName + "; AND ";
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(attributeName, value);
    }

    private void notEqualFilterBuilder(String value, String attributeName, StringBuilder filter,
                                    FilterQueryBuilder filterQueryBuilder) {

        String filterString = " !=:" + attributeName + "; AND ";
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(attributeName, value);
    }

    private void startWithFilterBuilder(String value, String attributeName, StringBuilder filter,
                                        FilterQueryBuilder filterQueryBuilder) {

        String filterString = " LIKE :" + attributeName + "; AND ";
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(attributeName, value + "%");
    }

    private void endWithFilterBuilder(String value, String attributeName, StringBuilder filter,
                                      FilterQueryBuilder filterQueryBuilder) {

        String filterString = " LIKE :" + attributeName + "; AND ";
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(attributeName, "%" + value);
    }

    private void containsFilterBuilder(String value, String attributeName, StringBuilder filter,
                                       FilterQueryBuilder filterQueryBuilder) {

        String filterString = " LIKE :" + attributeName + "; AND ";
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(attributeName, "%" + value + "%");
    }

    private void greaterThanOrEqualFilterBuilder(String value, String attributeName, StringBuilder filter,
                                                 FilterQueryBuilder filterQueryBuilder) {

        String filterString = " >= :" + attributeName + "; AND ";
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(attributeName, value);
    }

    private void lessThanOrEqualFilterBuilder(String value, String attributeName, StringBuilder filter,
                                              FilterQueryBuilder filterQueryBuilder) {

        String filterString = " <= :" + attributeName + "; AND ";
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(attributeName, value);
    }

    private void greaterThanFilterBuilder(String value, String attributeName, StringBuilder filter,
                                          FilterQueryBuilder filterQueryBuilder) {

        String filterString = " > :" + attributeName + "; AND ";
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(attributeName, value);
    }

    private void lessThanFilterBuilder(String value, String attributeName, StringBuilder filter,
                                       FilterQueryBuilder filterQueryBuilder) {

        String filterString = " < :" + attributeName + "; AND ";
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(attributeName, value);
    }

    private IdpGroup convertToIdpGroup(IdPGroup idpGroup) {

        IdpGroup convertedGroup = new IdpGroup(idpGroup.getIdpGroupId(), idpGroup.getIdpId());
        convertedGroup.setGroupName(idpGroup.getIdpGroupName());
        return convertedGroup;
    }

    @Override
    public List<RoleDTO> getSharedHybridRoles(String roleId, int mainTenantId) throws IdentityRoleManagementException {

        List<RoleDTO> hybridRoles = new ArrayList<>();
        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(true);
             NamedPreparedStatement statement = new NamedPreparedStatement(connection, GET_SHARED_ROLES_SQL)) {
            statement.setInt(RoleConstants.RoleTableColumns.UM_TENANT_ID, mainTenantId);
            statement.setString(RoleConstants.RoleTableColumns.UM_UUID, roleId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String name = resultSet.getString(1);
                    int tenantId = resultSet.getInt(2);
                    int audienceRefId = resultSet.getInt(3);
                    String id = resultSet.getString(4);
                    hybridRoles.add(new RoleDTO(name, id, audienceRefId, tenantId));
                }
            }
        } catch (SQLException e) {
            String errorMessage = "Error while retrieving shared roles of role id : " + roleId;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
        return hybridRoles;
    }
}
