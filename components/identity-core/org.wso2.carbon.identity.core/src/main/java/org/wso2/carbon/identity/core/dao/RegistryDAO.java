/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.core.dao;

import org.wso2.carbon.database.utils.jdbc.NamedPreparedStatement;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.utils.DBUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.UUID;

import static java.time.ZoneOffset.UTC;

public class RegistryDAO {

    private static final String PATH = "PATH";
    private static final String PATH_ID = "PATH_ID";
    private static final String PATH_PARENT_ID = "PATH_PARENT_ID";
    private static final String TENANT_ID = "TENANT_ID";
    private static final String REG_NAME = "REG_NAME";
    private static final String REG_MEDIA_TYPE = "REG_MEDIA_TYPE";
    private static final String REG_CREATOR = "REG_CREATOR";
    private static final String REG_CREATED_TIME = "REG_CREATED_TIME";
    private static final String REG_LAST_UPDATER = "REG_LAST_UPDATER";
    private static final String REG_LAST_UPDATED_TIME = "REG_LAST_UPDATED_TIME";
    private static final String REG_DESCRIPTION = "REG_DESCRIPTION";
    private static final String REG_UUID = "REG_UUID";
    private static final String REG_VALUE = "REG_VALUE";
    private static final String REG_PROPERTY_ID = "REG_PROPERTY_ID";
    private static final String REG_ID = "REG_ID";
    private static final String REG_CONTENT_ID = "REG_CONTENT_ID";
    private static final String REG_CONTENT_DATA = "REG_CONTENT_DATA";
    private static final String REG_VERSION = "REG_VERSION";
    private static final String REG_PATH_ID = "REG_PATH_ID";
    private static final String AUTHOR = "wso2.system.user";
    private final Calendar calendar;

    public RegistryDAO() {

        calendar = Calendar.getInstance(TimeZone.getTimeZone(UTC));
    }

    public int getPathId(String path, String tenantDomain) throws IdentityException {

        int pathId = -1;
        try (Connection connection = IdentityDatabaseUtil.getGovernanceDBConnection(false)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                    RegistrySQLQueries.GET_REG_PATH_ID)) {
                statement.setString(PATH, path);
                statement.setInt(TENANT_ID, IdentityTenantUtil.getTenantId(tenantDomain));
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        pathId = resultSet.getInt(1);
                    }
                }
            } catch (SQLException e) {
                throw new IdentityException("Error while retrieving registry path id.", e);
            }
        } catch (SQLException e) {
            throw new IdentityException("Error while retrieving registry path id.", e);
        }
        return pathId;
    }

    public Map<String, String> getCollectionProperties(int pathId, String tenantDomain) throws IdentityException {

        Map<String, String> properties = new HashMap<>();
        try (Connection connection = IdentityDatabaseUtil.getGovernanceDBConnection(false)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                    RegistrySQLQueries.GET_REG_RESOURCE_PROPERTY_COLLECTION)) {
                statement.setInt(PATH_ID, pathId);
                statement.setInt(TENANT_ID, IdentityTenantUtil.getTenantId(tenantDomain));
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        String key = resultSet.getString(1);
                        String value = resultSet.getString(2);
                        properties.put(key, value);
                    }
                }
            } catch (SQLException e) {
                throw new IdentityException("Error while retrieving registry resource collection properties.", e);
            }
        } catch (SQLException e) {
            throw new IdentityException("Error while retrieving registry resource collection properties.", e);
        }
        return properties;
    }

    public String[] getCollectionChildren(String path, int pathId, String tenantDomain) throws IdentityException {

        List<String> children = new ArrayList<>();
        try (Connection connection = IdentityDatabaseUtil.getGovernanceDBConnection(false)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                    RegistrySQLQueries.GET_REG_NAME_COLLECTION)) {
                statement.setInt(PATH_ID, pathId);
                statement.setInt(TENANT_ID, IdentityTenantUtil.getTenantId(tenantDomain));
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        children.add(path + "/" + resultSet.getString(1));
                    }
                }
            } catch (SQLException e) {
                throw new IdentityException("Error while retrieving registry resource collection sub resource paths.",
                        e);
            }

            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                    RegistrySQLQueries.GET_REG_PATH_COLLECTION)) {
                statement.setInt(PATH_PARENT_ID, pathId);
                statement.setInt(TENANT_ID, IdentityTenantUtil.getTenantId(tenantDomain));
                statement.setInt(TENANT_ID, IdentityTenantUtil.getTenantId(tenantDomain));
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        String childPathName = resultSet.getString(2).split("/(?!.*/)")[1];
                        children.add(path + "/" + childPathName);
                    }
                }
            } catch (SQLException e) {
                throw new IdentityException("Error while retrieving registry resource collection sub paths.", e);
            }
        } catch (SQLException e) {
            throw new IdentityException("Error while retrieving registry resource collection children.", e);
        }
        return children.toArray(new String[0]);
    }

    public Map<String, String> getResourceProperties(int pathId, String resourceName, String tenantDomain)
            throws IdentityException {

        Map<String, String> properties = new HashMap<>();
        try (Connection connection = IdentityDatabaseUtil.getGovernanceDBConnection(false)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                    RegistrySQLQueries.GET_REG_RESOURCE_PROPERTY_RESOURCE)) {
                statement.setInt(PATH_ID, pathId);
                statement.setString(REG_NAME, resourceName);
                statement.setInt(TENANT_ID, IdentityTenantUtil.getTenantId(tenantDomain));
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        String key = resultSet.getString(1);
                        String value = resultSet.getString(2);
                        properties.put(key, value);
                    }
                }
            } catch (SQLException e) {
                throw new IdentityException("Error while retrieving registry resource properties.", e);
            }
        } catch (SQLException e) {
            throw new IdentityException("Error while retrieving registry resource properties.", e);
        }
        return properties;
    }

    public byte[] getResourceContent(int pathId, String resourceName, String tenantDomain)
            throws IdentityException {

        byte[] content = null;
        try (Connection connection = IdentityDatabaseUtil.getGovernanceDBConnection(false)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                    RegistrySQLQueries.GET_REG_CONTENT)) {
                statement.setInt(PATH_ID, pathId);
                statement.setString(REG_NAME, resourceName);
                statement.setInt(TENANT_ID, IdentityTenantUtil.getTenantId(tenantDomain));
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        content = resultSet.getBytes(1);
                    }
                }
            } catch (SQLException e) {
                throw new IdentityException("Error while retrieving registry resource content.", e);
            }
        } catch (SQLException e) {
            throw new IdentityException("Error while retrieving registry resource content.", e);
        }
        return content;
    }

    public void addCollection(Resource resource, String path, int parentPathId, String tenantDomain)
            throws IdentityException {

        Connection connection = null;
        try {
            connection = IdentityDatabaseUtil.getGovernanceDBConnection(false);
            connection.setAutoCommit(false);
            int insertedPathId = addPath(connection, path, parentPathId, tenantDomain);
            addResource(connection, insertedPathId, null, resource.getMediaType(), resource.getAuthorUserName(), LocalDateTime.now(),
                    resource.getLastUpdaterUserName(), LocalDateTime.now(), resource.getDescription(), tenantDomain,
                    UUID.randomUUID().toString(),
                    null);
            addProperties(connection, resource.getProperties(), tenantDomain, insertedPathId, null);
            IdentityDatabaseUtil.commitTransaction(connection);
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            throw new IdentityException("Error while inserting registry resource.", e);
        } finally {
            IdentityDatabaseUtil.closeConnection(connection);
        }
    }

    public int addCollectionPath(String path, int parentPathId, String tenantDomain)
            throws IdentityException {

        int insertedPathId;
        Connection connection = null;
        try {
            connection = IdentityDatabaseUtil.getGovernanceDBConnection(false);
            connection.setAutoCommit(false);
            insertedPathId = addPath(connection, path, parentPathId, tenantDomain);
            addResource(connection, insertedPathId, null, null, AUTHOR, LocalDateTime.now(),
                    AUTHOR, LocalDateTime.now(), null, tenantDomain, UUID.randomUUID().toString(), null);
            IdentityDatabaseUtil.commitTransaction(connection);
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            throw new IdentityException("Error while inserting registry collection path.", e);
        } finally {
            IdentityDatabaseUtil.closeConnection(connection);
        }
        return insertedPathId;
    }

    public void addResource(Resource resource, int pathId, String resourceName, String tenantDomain)
            throws IdentityException {

        Connection connection = null;
        try {
            connection = IdentityDatabaseUtil.getGovernanceDBConnection(false);
            connection.setAutoCommit(false);
            addResource(connection, pathId, resourceName, resource.getMediaType(), resource.getAuthorUserName(), LocalDateTime.now(),
                    resource.getLastUpdaterUserName(), LocalDateTime.now(), resource.getDescription(), tenantDomain,
                    UUID.randomUUID().toString(),
                    resource.getContent());
            addProperties(connection, resource.getProperties(), tenantDomain, pathId, resourceName);
            IdentityDatabaseUtil.commitTransaction(connection);
        } catch (SQLException | RegistryException e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            throw new IdentityException("Error while inserting registry resource.", e);
        } finally {
            IdentityDatabaseUtil.closeConnection(connection);
        }
    }

    public void deleteResourceObj(int pathId, String resourceName, String tenantDomain)
            throws IdentityException {

        Connection connection = null;
        try {
            connection = IdentityDatabaseUtil.getGovernanceDBConnection(false);
            connection.setAutoCommit(false);
            deleteResource(connection, pathId, resourceName, tenantDomain);
            deleteProperties(connection, pathId, resourceName, tenantDomain);
            IdentityDatabaseUtil.commitTransaction(connection);
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            throw new IdentityException("Error while deleting registry resource.", e);
        } finally {
            IdentityDatabaseUtil.closeConnection(connection);
        }
    }

    public void deletePath(int pathId, String tenantDomain) throws IdentityException {

        try (Connection connection = IdentityDatabaseUtil.getGovernanceDBConnection(false)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                    RegistrySQLQueries.DELETE_PATH)) {
                statement.setInt(PATH_ID, pathId);
                statement.setInt(TENANT_ID, IdentityTenantUtil.getTenantId(tenantDomain));
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new IdentityException("Error while deleting registry path.", e);
            }
        } catch (SQLException e) {
            throw new IdentityException("Error while deleting registry path.", e);
        }
    }

    public boolean isResourceExists(int pathId, String resourceName, String tenantDomain) throws IdentityException {

        boolean isExists = false;
        try (Connection connection = IdentityDatabaseUtil.getGovernanceDBConnection(false)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                    RegistrySQLQueries.IS_RESOURCE_EXISTS)) {
                statement.setInt(PATH_ID, pathId);
                statement.setString(REG_NAME, resourceName);
                statement.setInt(TENANT_ID, IdentityTenantUtil.getTenantId(tenantDomain));
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        isExists = resultSet.getInt(1) > 0;
                    }
                }
            } catch (SQLException e) {
                throw new IdentityException("Error while checking registry resource existence.", e);
            }
        } catch (SQLException e) {
            throw new IdentityException("Error while checking registry resource existence.", e);
        }
        return isExists;
    }

    private int addPath(Connection connection, String path, int pathParentId, String tenantDomain)
            throws SQLException {

        int pathId = -1;
        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                RegistrySQLQueries.ADD_REG_PATH,
                DBUtils.getConvertedAutoGeneratedColumnName(connection.getMetaData().getDatabaseProductName(),
                        REG_PATH_ID))) {
            statement.setString(PATH, path);
            statement.setInt(PATH_PARENT_ID, pathParentId);
            statement.setInt(TENANT_ID, IdentityTenantUtil.getTenantId(tenantDomain));
            statement.executeUpdate();
            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    pathId = resultSet.getInt(1);
                }
            }
        }
        return pathId;
    }

    private void addResource(Connection connection, int pathId, String resourceName, String mediaType,
                             String creator,
                             LocalDateTime createdTime, String lastUpdater, LocalDateTime lastUpdatedTime,
                             String description, String tenantDomain, String uuid, Object content)
            throws SQLException {

        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                RegistrySQLQueries.ADD_REG_RESOURCE,
                DBUtils.getConvertedAutoGeneratedColumnName(connection.getMetaData().getDatabaseProductName(),
                        REG_VERSION))) {
            statement.setInt(PATH_ID, pathId);
            statement.setString(REG_NAME, resourceName);
            statement.setString(REG_MEDIA_TYPE, mediaType);
            statement.setString(REG_CREATOR, creator);
            statement.setTimeStamp(REG_CREATED_TIME, Timestamp.valueOf(createdTime), calendar);
            statement.setString(REG_LAST_UPDATER, lastUpdater);
            statement.setTimeStamp(REG_LAST_UPDATED_TIME, Timestamp.valueOf(lastUpdatedTime), calendar);
            statement.setString(REG_DESCRIPTION, description);
            statement.setInt(TENANT_ID, IdentityTenantUtil.getTenantId(tenantDomain));
            statement.setString(REG_UUID, uuid);
            statement.executeUpdate();
            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    int regVersion = resultSet.getInt(1);
                    if (content != null) {
                        addContent(connection, content, tenantDomain, regVersion);
                    }
                }
            }
        }
    }

    private void addContent(Connection connection, Object content, String tenantDomain, int regVersion)
            throws SQLException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        try (NamedPreparedStatement statement1 = new NamedPreparedStatement(connection,
                RegistrySQLQueries.ADD_REG_CONTENT,
                DBUtils.getConvertedAutoGeneratedColumnName(connection.getMetaData().getDatabaseProductName(),
                        REG_CONTENT_ID))) {
            statement1.setObject(REG_CONTENT_DATA, content);
            statement1.setInt(TENANT_ID, tenantId);
            statement1.executeUpdate();
            try (ResultSet resultSet = statement1.getGeneratedKeys()) {
                if (resultSet.next()) {
                    int contentId = resultSet.getInt(1);
                    NamedPreparedStatement statement2 =
                            new NamedPreparedStatement(connection, RegistrySQLQueries.UPDATE_REG_RESOURCE);
                    statement2.setInt(REG_CONTENT_ID, contentId);
                    statement2.setInt(REG_VERSION, regVersion);
                    statement2.setInt(TENANT_ID, tenantId);
                    statement2.executeUpdate();
                }
            }
        }
    }

    private void addProperty(Connection connection, String key, String value, String tenantDomain, int pathId,
                             String resourceName) throws SQLException {

        try (NamedPreparedStatement statement1 = new NamedPreparedStatement(connection,
                RegistrySQLQueries.ADD_REG_PROPERTY,
                DBUtils.getConvertedAutoGeneratedColumnName(connection.getMetaData().getDatabaseProductName(),
                        REG_ID))) {
            int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
            statement1.setString(REG_NAME, key);
            statement1.setString(REG_VALUE, value);
            statement1.setInt(TENANT_ID, tenantId);
            statement1.executeUpdate();
            try (ResultSet resultSet = statement1.getGeneratedKeys()) {
                if (resultSet.next()) {
                    int propertyId = resultSet.getInt(1);
                    NamedPreparedStatement statement2 =
                            new NamedPreparedStatement(connection, RegistrySQLQueries.ADD_REG_RESOURCE_PROPERTY);
                    statement2.setInt(REG_PROPERTY_ID, propertyId);
                    statement2.setInt(PATH_ID, pathId);
                    statement2.setString(REG_NAME, resourceName);
                    statement2.setInt(TENANT_ID, tenantId);
                    statement2.executeUpdate();
                }
            }
        }
    }

    private void addProperties(Connection connection, Properties props, String tenantDomain, int pathId,
                               String resourceName) throws SQLException {

        if (props != null) {
            for (Map.Entry<Object, Object> entry : props.entrySet()) {
                Object key = entry.getKey();
                for (Object value : (List<?>) entry.getValue()) {
                    if (value != null) {
                        addProperty(connection, key.toString(), value.toString(), tenantDomain, pathId,
                                resourceName);
                    }
                }
            }
        }
    }

    private void deleteResource(Connection connection, int pathId, String resourceName, String tenantDomain)
            throws SQLException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        if (resourceName == null) {
            NamedPreparedStatement statement1 = new NamedPreparedStatement(connection,
                    RegistrySQLQueries.DELETE_REG_RESOURCE_COLLECTION);
            statement1.setInt(PATH_ID, pathId);
            statement1.setInt(TENANT_ID, tenantId);
            statement1.executeUpdate();
        } else {
            int contentId = -1;
            try (NamedPreparedStatement statement2 = new NamedPreparedStatement(connection,
                    RegistrySQLQueries.GET_CONTENT_ID)) {
                statement2.setInt(PATH_ID, pathId);
                statement2.setString(REG_NAME, resourceName);
                statement2.setInt(TENANT_ID, tenantId);
                try (ResultSet resultSet = statement2.executeQuery()) {
                    if (resultSet.next()) {
                        contentId = resultSet.getInt(1);
                    }
                }
            }
            try (NamedPreparedStatement statement3 = new NamedPreparedStatement(connection,
                    RegistrySQLQueries.DELETE_REG_RESOURCE)) {
                statement3.setInt(PATH_ID, pathId);
                statement3.setString(REG_NAME, resourceName);
                statement3.setInt(TENANT_ID, tenantId);
                statement3.executeUpdate();
                if (contentId != -1) {
                    deleteContent(connection, contentId, tenantDomain);
                }
            }
        }
    }

    private void deleteContent(Connection connection, int contentId, String tenantDomain)
            throws SQLException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        try (NamedPreparedStatement statement1 = new NamedPreparedStatement(connection,
                RegistrySQLQueries.DELETE_REG_CONTENT,
                DBUtils.getConvertedAutoGeneratedColumnName(connection.getMetaData().getDatabaseProductName(),
                        REG_CONTENT_ID))) {
            statement1.setObject(REG_CONTENT_ID, contentId);
            statement1.setInt(TENANT_ID, tenantId);
            statement1.executeUpdate();
        }
    }

    private void deleteProperties(Connection connection, int pathId, String resourceName, String tenantDomain)
            throws SQLException {

        List<Integer> propIds = new ArrayList<>();
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        NamedPreparedStatement statement1;
        if (resourceName == null) {
            statement1 = new NamedPreparedStatement(connection, RegistrySQLQueries.GET_REG_PROPERTY_ID_COLLECTION);
            statement1.setInt(PATH_ID, pathId);
            statement1.setInt(TENANT_ID, tenantId);
        } else {
            statement1 = new NamedPreparedStatement(connection, RegistrySQLQueries.GET_REG_PROPERTY_ID_RESOURCE);
            statement1.setInt(PATH_ID, pathId);
            statement1.setString(REG_NAME, resourceName);
            statement1.setInt(TENANT_ID, tenantId);
        }
        try (ResultSet resultSet = statement1.executeQuery()) {
            while (resultSet.next()) {
                propIds.add(resultSet.getInt(1));
            }
        }

        try (NamedPreparedStatement statement2 = new NamedPreparedStatement(connection,
                RegistrySQLQueries.DELETE_REG_RESOURCE_PROPERTY)) {
            NamedPreparedStatement statement3 = new NamedPreparedStatement(connection,
                    RegistrySQLQueries.DELETE_REG_PROPERTY);
            for (int propId : propIds) {
                statement2.setInt(REG_PROPERTY_ID, propId);
                statement2.setInt(TENANT_ID, tenantId);
                statement2.addBatch();

                statement3.setInt(REG_ID, propId);
                statement3.setInt(TENANT_ID, tenantId);
                statement3.addBatch();
            }
            statement2.executeBatch();
            statement3.executeBatch();
        }
    }
}
