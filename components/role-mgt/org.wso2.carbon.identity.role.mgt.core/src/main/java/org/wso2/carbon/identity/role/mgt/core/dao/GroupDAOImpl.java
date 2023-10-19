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

import org.wso2.carbon.database.utils.jdbc.NamedPreparedStatement;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.role.mgt.core.IdentityRoleManagementClientException;
import org.wso2.carbon.identity.role.mgt.core.IdentityRoleManagementException;
import org.wso2.carbon.identity.role.mgt.core.IdentityRoleManagementServerException;
import org.wso2.carbon.identity.role.mgt.core.RoleConstants;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.role.mgt.core.RoleConstants.Error.INVALID_REQUEST;
import static org.wso2.carbon.identity.role.mgt.core.RoleConstants.Error.UNEXPECTED_SERVER_ERROR;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.GET_GROUP_ID_BY_NAME_SQL;
import static org.wso2.carbon.identity.role.mgt.core.dao.SQLQueries.GET_GROUP_NAME_BY_ID_SQL;

/**
 * RoleDAO Implementation.
 */
public class GroupDAOImpl implements GroupDAO {

    @Override
    public String getGroupNameByID(String id, String tenantDomain) throws IdentityRoleManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        String groupName = null;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, GET_GROUP_NAME_BY_ID_SQL)) {
                statement.setInt(RoleConstants.RoleTableColumns.TENANT_ID, tenantId);
                statement.setString(RoleConstants.RoleTableColumns.ATTR_NAME, RoleConstants.ID_URI);
                statement.setString(RoleConstants.RoleTableColumns.ATTR_VALUE, id);
                int count = 0;
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        // Handle multiple matching groups.
                        count++;
                        if (count > 1) {
                            String errorMessage =
                                    "Invalid scenario. Multiple groups found for the given group ID: " + id + " and "
                                            + "tenantDomain: " + tenantDomain;
                            throw new IdentityRoleManagementClientException(INVALID_REQUEST.getCode(), errorMessage);
                        }
                        groupName = resultSet.getString(1);
                    }
                }
            }
        } catch (SQLException e) {
            String errorMessage =
                    "Error while resolving the group name for the given group ID: " + id + " and tenantDomain: "
                            + tenantDomain;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
        return groupName;
    }

    @Override
    public Map<String, String> getGroupNamesByIDs(List<String> ids, String tenantDomain)
            throws IdentityRoleManagementException {

        Map<String, String> groupIdsToNames;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            groupIdsToNames = batchProcessGroupIDs(ids, tenantDomain, connection);
        } catch (SQLException e) {
            String errorMessage =
                    "Error while resolving the group name for the given group Ids in the tenantDomain: " + tenantDomain;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
        return groupIdsToNames;
    }

    private Map<String, String> batchProcessGroupIDs(List<String> ids, String tenantDomain, Connection connection)
            throws SQLException, IdentityRoleManagementException {

        Map<String, String> groupIdsToNames = new HashMap<>();
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        String groupName;
        for (String id : ids) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, GET_GROUP_NAME_BY_ID_SQL)) {
                statement.setInt(RoleConstants.RoleTableColumns.TENANT_ID, tenantId);
                statement.setString(RoleConstants.RoleTableColumns.ATTR_NAME, RoleConstants.ID_URI);
                statement.setString(RoleConstants.RoleTableColumns.ATTR_VALUE, id);
                int count = 0;
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        // Handle multiple matching groups.
                        count++;
                        if (count > 1) {
                            String errorMessage =
                                    "Invalid scenario. Multiple groups found for the given group ID: " + id + " and "
                                            + "tenantDomain: " + tenantDomain;
                            throw new IdentityRoleManagementClientException(INVALID_REQUEST.getCode(), errorMessage);
                        }
                        groupName = resultSet.getString(1);
                        groupIdsToNames.put(id, groupName);
                    }
                }
            }
        }
        return groupIdsToNames;
    }

    @Override
    public String getGroupIDByName(String name, String tenantDomain) throws IdentityRoleManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        String groupID = null;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, GET_GROUP_ID_BY_NAME_SQL)) {
                statement.setInt(RoleConstants.RoleTableColumns.TENANT_ID, tenantId);
                statement.setString(RoleConstants.RoleTableColumns.ROLE_NAME, name);
                statement.setString(RoleConstants.RoleTableColumns.ATTR_NAME, RoleConstants.ID_URI);
                int count = 0;
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        // Handle multiple matching groups.
                        count++;
                        if (count > 1) {
                            String errorMessage =
                                    "Invalid scenario. Multiple groups found for the given group name: " + name + " "
                                            + "and tenantDomain: " + tenantDomain;
                            throw new IdentityRoleManagementClientException(INVALID_REQUEST.getCode(), errorMessage);
                        }
                        groupID = resultSet.getString(1);
                    }
                }
            }
        } catch (SQLException e) {
            String errorMessage =
                    "Error while resolving the group ID for the given group name: " + name + " and tenantDomain: "
                            + tenantDomain;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
        return groupID;
    }

    @Override
    public Map<String, String> getGroupIDsByNames(List<String> names, String tenantDomain)
            throws IdentityRoleManagementException {

        Map<String, String> groupNamesToIDs;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            groupNamesToIDs = batchProcessGroupNames(names, tenantDomain, connection);
        } catch (SQLException e) {
            String errorMessage =
                    "Error while resolving the group ID for the given group names in the tenantDomain: " + tenantDomain;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
        return groupNamesToIDs;
    }

    private Map<String, String> batchProcessGroupNames(List<String> names, String tenantDomain, Connection connection)
            throws SQLException, IdentityRoleManagementException {

        Map<String, String> groupNamesToIDs = new HashMap<>();
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        String groupID;
        for (String name : names) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, GET_GROUP_ID_BY_NAME_SQL)) {
                statement.setInt(RoleConstants.RoleTableColumns.TENANT_ID, tenantId);
                statement.setString(RoleConstants.RoleTableColumns.ROLE_NAME, name);
                statement.setString(RoleConstants.RoleTableColumns.ATTR_NAME, RoleConstants.ID_URI);
                int count = 0;
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        // Handle multiple matching groups.
                        count++;
                        if (count > 1) {
                            String errorMessage =
                                    "Invalid scenario. Multiple groups found for the given group name: " + name + " "
                                            + "and tenantDomain: " + tenantDomain;
                            throw new IdentityRoleManagementClientException(INVALID_REQUEST.getCode(), errorMessage);
                        }
                        groupID = resultSet.getString(1);
                        groupNamesToIDs.put(name, groupID);
                    }
                }
            }
        }
        return groupNamesToIDs;
    }
}
