package org.wso2.carbon.light.registry.mgt.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.database.utils.jdbc.NamedPreparedStatement;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.light.registry.mgt.LightRegistryException;
import org.wso2.carbon.light.registry.mgt.constants.LightRegistryConstants;
import org.wso2.carbon.light.registry.mgt.model.Collection;
import org.wso2.carbon.light.registry.mgt.model.CollectionImpl;
import org.wso2.carbon.light.registry.mgt.model.ResourceID;
import org.wso2.carbon.light.registry.mgt.model.ResourceImpl;
import org.wso2.carbon.light.registry.mgt.utils.RegistryUtils;
import org.wso2.carbon.utils.DBUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import static org.wso2.carbon.light.registry.mgt.constants.LightRegistryConstants.PATH_ID;
import static org.wso2.carbon.light.registry.mgt.constants.LightRegistryConstants.PATH_PARENT_ID;
import static org.wso2.carbon.light.registry.mgt.constants.LightRegistryConstants.PATH_SEPARATOR;
import static org.wso2.carbon.light.registry.mgt.constants.LightRegistryConstants.REG_CONTENT_DATA;
import static org.wso2.carbon.light.registry.mgt.constants.LightRegistryConstants.REG_CONTENT_ID;
import static org.wso2.carbon.light.registry.mgt.constants.LightRegistryConstants.REG_CREATED_TIME;
import static org.wso2.carbon.light.registry.mgt.constants.LightRegistryConstants.REG_CREATOR;
import static org.wso2.carbon.light.registry.mgt.constants.LightRegistryConstants.REG_DESCRIPTION;
import static org.wso2.carbon.light.registry.mgt.constants.LightRegistryConstants.REG_ID;
import static org.wso2.carbon.light.registry.mgt.constants.LightRegistryConstants.REG_LAST_UPDATED_TIME;
import static org.wso2.carbon.light.registry.mgt.constants.LightRegistryConstants.REG_LAST_UPDATER;
import static org.wso2.carbon.light.registry.mgt.constants.LightRegistryConstants.REG_MEDIA_TYPE;
import static org.wso2.carbon.light.registry.mgt.constants.LightRegistryConstants.REG_NAME;
import static org.wso2.carbon.light.registry.mgt.constants.LightRegistryConstants.REG_PATH_ID;
import static org.wso2.carbon.light.registry.mgt.constants.LightRegistryConstants.REG_PATH_VALUE;
import static org.wso2.carbon.light.registry.mgt.constants.LightRegistryConstants.REG_PROPERTY_ID;
import static org.wso2.carbon.light.registry.mgt.constants.LightRegistryConstants.REG_UUID;
import static org.wso2.carbon.light.registry.mgt.constants.LightRegistryConstants.REG_VALUE;
import static org.wso2.carbon.light.registry.mgt.constants.LightRegistryConstants.TENANT_ID;

import static java.time.ZoneOffset.UTC;

public class RegistryDAOImpl implements RegistryDAO {

    private static final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(UTC));
    private static final Log log = LogFactory.getLog(RegistryDAOImpl.class);

    @Override
    public boolean resourceExists(ResourceID resourceId, int tenantId) throws LightRegistryException {

        boolean isExists;
        try (Connection connection = IdentityDatabaseUtil.getGovernanceDBConnection(false)) {
            isExists =
                    executeResourceExists(connection, resourceId.getPathID(), resourceId.getResourceName(), tenantId);
        } catch (SQLException e) {
            throw new LightRegistryException("Error while checking registry resource existence.", e);
        }
        return isExists;
    }

    @Override
    public int getPathID(String path, int tenantId) throws LightRegistryException {

        path = RegistryUtils.getAbsolutePath(path);
        int pathId = -1;
        try (Connection connection = IdentityDatabaseUtil.getGovernanceDBConnection(false)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                    RegistrySQLQueries.GET_REG_PATH_ID)) {
                statement.setString(LightRegistryConstants.PATH, path);
                statement.setInt(TENANT_ID, tenantId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        pathId = resultSet.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new LightRegistryException("Error while retrieving registry path id.", e);
        }
        return pathId;
    }

    @Override
    public ResourceID getResourceID(String path, int tenantId) throws LightRegistryException {

        int pathID = getPathID(path, tenantId);
        boolean isCollection = pathID != -1;

        return getResourceID(path, tenantId, isCollection);
    }

    @Override
    public ResourceID getResourceID(String path, int tenantId, boolean isCollection) throws LightRegistryException {

        int pathID;
        String resourceName = null;
        if (!isCollection) {
            String parentPath = RegistryUtils.getParentPath(path);
            resourceName = RegistryUtils.getResourceName(path);
            pathID = getPathID(parentPath, tenantId);
        } else {
            pathID = getPathID(path, tenantId);
        }

        if (pathID != -1) {
            ResourceID resourceID = new ResourceID();
            resourceID.setCollection(isCollection);
            resourceID.setResourceName(resourceName);
            resourceID.setPathID(pathID);
            resourceID.setPath(path);
            resourceID.setTenantId(tenantId);
            return resourceID;
        }
        return null;
    }

    @Override
    public ResourceImpl get(String path, int tenantId) throws LightRegistryException {

        ResourceID resourceId = getResourceID(path, tenantId);
        if (resourceId == null) {
            return null;
        }
        ResourceImpl resourceImpl = getResourceMetaData(tenantId, resourceId);
        if (resourceImpl == null) {
            // it is possible the resource doesn't exist
            return null;
        }
        if (resourceId.isCollection()) {
            fillChildren((CollectionImpl) resourceImpl, tenantId);
        } else if (resourceImpl.getDbBasedContentID() > 0) {
            fillResourceContent(tenantId, resourceImpl);
        }
        fillResourceProperties(tenantId, resourceId, resourceImpl);
        return resourceImpl;
    }

    @Override
    public ResourceImpl getResourceMetaData(int tenantId, ResourceID resourceID) throws LightRegistryException {

        ResourceImpl resource;
        try (Connection connection = IdentityDatabaseUtil.getGovernanceDBConnection(false)) {
            resource = executeGetResource(connection, tenantId, resourceID);
        } catch (SQLException e) {
            throw new LightRegistryException("Error while retrieving registry resource meta data.", e);
        }
        return resource;
    }

    @Override
    public ResourceImpl getResourceMetaData(int tenantId, String path) throws LightRegistryException {

        ResourceID resourceId = getResourceID(path, tenantId);
        ResourceImpl resourceImpl = null;
        if (resourceId != null) {
            resourceImpl = getResourceMetaData(tenantId, resourceId);

            if (resourceImpl == null && resourceId.isCollection()) {
                // we should check the resourceID for a non-collection too.
                resourceId = getResourceID(path, tenantId, false);
                if (resourceId != null) {
                    resourceImpl = getResourceMetaData(tenantId, resourceId);
                }
            }
        }
        return resourceImpl;
    }

    @Override
    public void fillResource(ResourceImpl resourceImpl, int tenantId) throws LightRegistryException {

        if (resourceImpl == null) {
            throw new LightRegistryException("Unable to fill null resource");
        }
        if (!(resourceImpl instanceof CollectionImpl) && resourceImpl.getDbBasedContentID() > 0) {
            fillResourceContent(tenantId, resourceImpl);
        } else {
            if (resourceImpl instanceof CollectionImpl) {
                fillChildren((CollectionImpl) resourceImpl, tenantId);
            }
        }
        fillResourceProperties(tenantId, resourceImpl.getResourceID(), resourceImpl);
    }

    @Override
    public void fillResourceProperties(int tenantId, ResourceID resourceId, ResourceImpl resource)
            throws LightRegistryException {

        Map<String, String> properties;
        try (Connection connection = IdentityDatabaseUtil.getGovernanceDBConnection(false)) {
            properties = executeGetResourceProperties(connection, tenantId, resourceId);
        } catch (SQLException e) {
            throw new LightRegistryException("Error while retrieving registry resource properties.", e);
        }
        properties.forEach(resource::setProperty);
    }

    @Override
    public void add(String path, ResourceID parentID, ResourceImpl resourceImpl, int tenantId)
            throws LightRegistryException {

        // creating the resourceID
        createAndApplyResourceID(path, tenantId, parentID, resourceImpl);

        addResourceWithoutContentId(resourceImpl, tenantId);
        if (!(resourceImpl instanceof Collection)) {
            addContent(resourceImpl, tenantId);
            if (resourceImpl.getDbBasedContentID() > 0) {
                updateContentId(resourceImpl, tenantId);
            }
        }
        addProperties(resourceImpl);
    }

    public int addPath(String path, int parentPathId, int tenantId) throws LightRegistryException {

        int pathId = -1;
        try (Connection connection = IdentityDatabaseUtil.getGovernanceDBConnection(false)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                    RegistrySQLQueries.ADD_REG_PATH,
                    DBUtils.getConvertedAutoGeneratedColumnName(connection.getMetaData().getDatabaseProductName(),
                            REG_PATH_ID))) {
                statement.setString(LightRegistryConstants.PATH, path);
                statement.setInt(LightRegistryConstants.PATH_PARENT_ID, parentPathId);
                statement.setInt(LightRegistryConstants.TENANT_ID, tenantId);
                statement.executeUpdate();
                try (ResultSet resultSet = statement.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        pathId = resultSet.getInt(REG_PATH_ID);
                    }
                }
            }
        } catch (SQLException e) {
            if (e instanceof SQLIntegrityConstraintViolationException || "23505".equals(e.getSQLState())) {
                if (log.isDebugEnabled()) {
                    log.debug("Failed to insert duplicate path : " + path);
                }
                // we expect an exception for duplicate paths
                pathId = getPathID(path, tenantId);
                if (pathId > 0) {
                    return pathId;
                }
            }
            throw new LightRegistryException("Error while inserting registry path.", e);
        }
        return pathId;
    }

    public void createAndApplyResourceID(String path, int tenantId, ResourceID parentID, ResourceImpl resource)
            throws LightRegistryException {

        ResourceID resourceId =
                createResourceID(path, tenantId, parentID, resource instanceof CollectionImpl);
        resource.setName(resourceId.getResourceName());
        resource.setPath(resourceId.getPath());
        resource.setPathID(resourceId.getPathID());
    }

    private ResourceID createResourceID(String path, int tenantId, ResourceID parentId, boolean isCollection)
            throws LightRegistryException {

        ResourceID resourceId = new ResourceID();
        int parentPathId = -1;
        if (parentId != null) {
            parentPathId = parentId.getPathID();
        }
        if (isCollection) {
            resourceId.setResourceName(null);
            resourceId.setPath(path);
            int pathID = getPathID(path, tenantId);
            if (pathID == -1) {
                pathID = addPath(path, parentPathId, tenantId);
            }
            resourceId.setPathID(pathID);
        } else {
            String resourceName = RegistryUtils.getResourceName(path);
            resourceId.setResourceName(resourceName);
            resourceId.setPath(path);
            resourceId.setPathID(parentPathId);
        }
        return resourceId;
    }

    @Override
    public void deleteContent(int contentId, int tenantId) throws LightRegistryException {

        try (Connection connection = IdentityDatabaseUtil.getGovernanceDBConnection(false)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                    RegistrySQLQueries.DELETE_REG_CONTENT)) {
                statement.setObject(REG_CONTENT_ID, contentId);
                statement.setInt(TENANT_ID, tenantId);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new LightRegistryException("Error while deleting registry resource content.", e);
        }
    }

    @Override
    public void update(ResourceImpl resourceImpl, int tenantId) throws LightRegistryException {

        addResourceWithoutContentId(resourceImpl, tenantId);
        if (!(resourceImpl instanceof Collection)) {
            addContent(resourceImpl, tenantId);
            if (resourceImpl.getDbBasedContentID() > 0) {
                updateContentId(resourceImpl, tenantId);
            }
        }
        addProperties(resourceImpl);
    }

    @Override
    public void fillChildren(CollectionImpl collection, int tenantId) throws LightRegistryException {

        String[] childPaths = getChildren(collection, tenantId);
        collection.setContent(childPaths);
        if (childPaths != null) {
            collection.setChildCount(childPaths.length);
        }
    }

    @Override
    public String[] getChildren(CollectionImpl collection, int tenantId) throws LightRegistryException {

        List<String> children = new ArrayList<>();
        String path = collection.getPath();
        int pathId = collection.getPathID();
        try (Connection connection = IdentityDatabaseUtil.getGovernanceDBConnection(false)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                    RegistrySQLQueries.GET_REG_NAME_COLLECTION)) {
                statement.setInt(PATH_ID, pathId);
                statement.setInt(TENANT_ID, tenantId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        String childName = resultSet.getString(REG_NAME);
                        if (childName == null) {
                            // skip the null named resource (which is the parent resource)
                            continue;
                        }
                        String childPath = path + (path.equals(PATH_SEPARATOR) ? "" : PATH_SEPARATOR) + childName;
                        children.add(childPath);
                    }
                }
            }

            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                    RegistrySQLQueries.GET_REG_PATH_COLLECTION)) {
                statement.setInt(PATH_PARENT_ID, pathId);
                statement.setInt(TENANT_ID, tenantId);
                statement.setInt(TENANT_ID, tenantId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        String pathValue = resultSet.getString(REG_PATH_VALUE);
                        int startIndex = pathValue.indexOf(path) + path.length();
                        String childPath = pathValue.substring(startIndex);
                        children.add(path + childPath);
                    }
                }
            }
        } catch (SQLException e) {
            throw new LightRegistryException("Error while retrieving registry resource collection children.", e);
        }
        Collections.sort(children);
        return children.toArray(new String[0]);
    }

    @Override
    public void fillResourceContent(int tenantId, ResourceImpl resourceImpl) throws LightRegistryException {

        int contentId = resourceImpl.getDbBasedContentID();
        byte[] content = null;
        try (Connection connection = IdentityDatabaseUtil.getGovernanceDBConnection(false)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                    RegistrySQLQueries.GET_REG_CONTENT)) {
                statement.setInt(REG_CONTENT_ID, contentId);
                statement.setInt(TENANT_ID, tenantId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        content = resultSet.getBytes(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new LightRegistryException("Error while retrieving registry resource content data.", e);
        }
        if (content != null) {
            resourceImpl.setContent(content);
        }
    }

    @Override
    public void updateContentId(ResourceImpl resourceImpl, int tenantId) throws LightRegistryException {

        try (Connection connection = IdentityDatabaseUtil.getGovernanceDBConnection(false)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                    RegistrySQLQueries.UPDATE_REG_RESOURCE)) {
                statement.setInt(REG_CONTENT_ID, resourceImpl.getDbBasedContentID());
                statement.setInt(REG_PATH_ID, resourceImpl.getPathID());
                statement.setInt(TENANT_ID, tenantId);
                statement.setString(REG_NAME, resourceImpl.getName());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new LightRegistryException("Error while updating registry resource content id.", e);
        }
    }

    @Override
    public void addResourceWithoutContentId(ResourceImpl resourceImpl, int tenantId)
            throws LightRegistryException {

        Timestamp createdTimestamp;
        String authorName = resourceImpl.getAuthorUserName();
        if (authorName == null) {
            resourceImpl.setAuthorUserName(LightRegistryConstants.AUTHOR);
        }
        Date createdTime = resourceImpl.getCreatedTime();
        if (createdTime == null) {
            createdTimestamp = new Timestamp(System.currentTimeMillis());
        } else {
            createdTimestamp = new Timestamp(createdTime.getTime());
        }

        try (Connection connection = IdentityDatabaseUtil.getGovernanceDBConnection(false)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                    RegistrySQLQueries.ADD_REG_RESOURCE)) {
                statement.setInt(PATH_ID, resourceImpl.getPathID());
                statement.setString(REG_NAME, resourceImpl.getName());
                statement.setString(REG_MEDIA_TYPE, resourceImpl.getMediaType());
                statement.setString(REG_CREATOR, resourceImpl.getAuthorUserName());
                statement.setTimeStamp(REG_CREATED_TIME, createdTimestamp, calendar);
                statement.setString(REG_LAST_UPDATER, LightRegistryConstants.AUTHOR);
                statement.setTimeStamp(REG_LAST_UPDATED_TIME, new Timestamp(System.currentTimeMillis()), calendar);
                statement.setString(REG_DESCRIPTION, resourceImpl.getDescription());
                statement.setInt(TENANT_ID, tenantId);
                statement.setString(REG_UUID, resourceImpl.getUUID());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new LightRegistryException("Error while adding registry resource content.", e);
        }
    }

    @Override
    public void deleteResource(ResourceImpl resource, int tenantId) throws LightRegistryException {

        try (Connection connection = IdentityDatabaseUtil.getGovernanceDBConnection(false)) {
            try {
                NamedPreparedStatement statement;
                if (resource instanceof CollectionImpl) {
                    statement =
                            new NamedPreparedStatement(connection, RegistrySQLQueries.DELETE_REG_RESOURCE_COLLECTION);
                } else {
                    statement = new NamedPreparedStatement(connection, RegistrySQLQueries.DELETE_REG_RESOURCE);
                    statement.setString(REG_NAME, resource.getName());
                }
                statement.setInt(REG_PATH_ID, resource.getPathID());
                statement.setInt(TENANT_ID, tenantId);
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new LightRegistryException("Error while deleting registry resource.", e);
            }
        } catch (SQLException e) {
            throw new LightRegistryException("Error while deleting registry resource.", e);
        }
    }

    @Override
    public void addProperties(ResourceImpl resource) throws LightRegistryException {

        ResourceID resourceId = resource.getResourceID();
        Properties props = resource.getProperties();
        try (Connection connection = IdentityDatabaseUtil.getGovernanceDBConnection(false)) {
            if (props != null) {
                for (Map.Entry<Object, Object> entry : props.entrySet()) {
                    Object key = entry.getKey();
                    for (Object value : (List<?>) entry.getValue()) {
                        if (value != null) {
                            addProperty(connection, key.toString(), value.toString(), resourceId);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new LightRegistryException("Error while adding registry resource properties.", e);
        }

    }

    @Override
    public void removeProperties(ResourceImpl resource, int tenantId) throws LightRegistryException {

        Integer[] propertyIds = getPropertyIds(resource, tenantId);
        try (Connection connection = IdentityDatabaseUtil.getGovernanceDBConnection(false)) {
            try (NamedPreparedStatement statement1 = new NamedPreparedStatement(connection,
                    RegistrySQLQueries.DELETE_REG_RESOURCE_PROPERTY)) {
                NamedPreparedStatement statement2 = new NamedPreparedStatement(connection,
                        RegistrySQLQueries.DELETE_REG_PROPERTY);
                for (int propId : propertyIds) {
                    statement1.setInt(REG_PROPERTY_ID, propId);
                    statement1.setInt(TENANT_ID, tenantId);
                    statement1.addBatch();

                    statement2.setInt(REG_ID, propId);
                    statement2.setInt(TENANT_ID, tenantId);
                    statement2.addBatch();
                }
                if (propertyIds.length > 0) {
                    try {
                        statement1.executeBatch();
                        statement2.executeBatch();
                    } catch (SQLException e) {
                        statement1.clearBatch();
                        statement2.clearBatch();
                        throw e;
                    }
                }
            }
        } catch (SQLException e) {
            throw new LightRegistryException("Error while deleting registry resource properties.", e);
        }
    }

    @Override
    public void addContent(ResourceImpl resourceImpl, int tenantId) throws LightRegistryException {

        if (!(resourceImpl.getContent() instanceof byte[])) {
            resourceImpl.setDbBasedContentID(0);
            return;
        }
        int contentID = addContentBytes(resourceImpl.getContent(), tenantId);
        resourceImpl.setDbBasedContentID(contentID);
    }

    @Override
    public int addContentBytes(Object content, int tenantId) throws LightRegistryException {

        int contentId = -1;
        try (Connection connection = IdentityDatabaseUtil.getGovernanceDBConnection(false)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                    RegistrySQLQueries.ADD_REG_CONTENT,
                    DBUtils.getConvertedAutoGeneratedColumnName(connection.getMetaData().getDatabaseProductName(),
                            REG_CONTENT_ID))) {
                statement.setObject(REG_CONTENT_DATA, content);
                statement.setInt(TENANT_ID, tenantId);
                statement.executeUpdate();
                try (ResultSet resultSet = statement.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        contentId = resultSet.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new LightRegistryException("Error while adding registry resource content.", e);
        }
        return contentId;
    }

    @Override
    public List<ResourceID> getChildPathIds(ResourceID resourceId) throws LightRegistryException {

        List<ResourceID> children = new ArrayList<>();
        String path = resourceId.getPath();
        int pathId = resourceId.getPathID();
        int tenantId = resourceId.getTenantId();
        try (Connection connection = IdentityDatabaseUtil.getGovernanceDBConnection(false)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                    RegistrySQLQueries.GET_REG_NAME_COLLECTION)) {
                statement.setInt(PATH_ID, pathId);
                statement.setInt(TENANT_ID, tenantId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        String childName = resultSet.getString(REG_NAME);
                        if (childName == null) {
                            // skip the null named resource (which is the parent resource)
                            continue;
                        }
                        String childPath = path + (path.equals(PATH_SEPARATOR) ? "" : PATH_SEPARATOR) + childName;
                        ResourceID childResourceId = new ResourceID();
                        childResourceId.setPath(childPath);
                        childResourceId.setCollection(false);
                        childResourceId.setResourceName(childName);
                        childResourceId.setPathID(resourceId.getPathID());
                        children.add(childResourceId);
                    }
                }
            }

            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                    RegistrySQLQueries.GET_REG_PATH_COLLECTION)) {
                statement.setInt(PATH_PARENT_ID, pathId);
                statement.setInt(TENANT_ID, tenantId);
                statement.setInt(TENANT_ID, tenantId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        String pathValue = resultSet.getString(REG_PATH_VALUE);
                        int childPathId = resultSet.getInt(REG_PATH_ID);

                        int startIndex = pathValue.indexOf(path) + path.length();
                        String childPath = pathValue.substring(startIndex);

                        ResourceID childResourceID = new ResourceID();
                        childResourceID.setPath(path + childPath);
                        childResourceID.setCollection(true);
                        childResourceID.setResourceName(null);
                        childResourceID.setPathID(childPathId);
                        children.add(childResourceID);
                    }
                }
            }
        } catch (SQLException e) {
            throw new LightRegistryException("Error while retrieving registry resource children.", e);
        }
        return children;
    }

    private void addProperty(Connection connection, String key, String value, ResourceID resourceId)
            throws SQLException {

        int tenantId = resourceId.getTenantId();
        int pathId = resourceId.getPathID();
        String resourceName = resourceId.getResourceName();

        try (NamedPreparedStatement statement1 = new NamedPreparedStatement(connection,
                RegistrySQLQueries.ADD_REG_PROPERTY,
                DBUtils.getConvertedAutoGeneratedColumnName(connection.getMetaData().getDatabaseProductName(),
                        REG_ID))) {
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

    private boolean executeResourceExists(Connection connection, int pathId, String resourceName, int tenantId)
            throws LightRegistryException {

        boolean isExists = false;
        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                RegistrySQLQueries.IS_RESOURCE_EXISTS)) {
            statement.setInt(PATH_ID, pathId);
            statement.setString(REG_NAME, resourceName);
            statement.setInt(TENANT_ID, tenantId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    isExists = resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new LightRegistryException("Error while checking registry resource existence.", e);
        }
        return isExists;
    }

    private ResourceImpl executeGetResource(Connection connection, int tenantId, ResourceID resourceID)
            throws LightRegistryException {

        ResourceImpl resource;
        int pathId = resourceID.getPathID();
        String path = resourceID.getPath();
        String resourceName = resourceID.getResourceName();
        boolean isCollection = resourceID.isCollection();

        try {
            NamedPreparedStatement statement;
            if (isCollection) {
                statement = new NamedPreparedStatement(connection,
                        RegistrySQLQueries.GET_COLLECTION_RESOURCE);
            } else {
                statement = new NamedPreparedStatement(connection,
                        RegistrySQLQueries.GET_FILE_RESOURCE);
                statement.setString(REG_NAME, resourceName);
            }
            statement.setInt(PATH_ID, pathId);
            statement.setInt(TENANT_ID, tenantId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    if (isCollection) {
                        resource = new CollectionImpl();
                    } else {
                        resource = new ResourceImpl();
                    }
                    resource.setPathID(pathId);
                    resource.setPath(path);
                    resource.setName(resourceName);
                    resource.setMediaType(resultSet.getString(REG_MEDIA_TYPE));
                    resource.setAuthorUserName(resultSet.getString(REG_CREATOR));
                    resource.setCreatedTime(resultSet.getTimestamp(REG_CREATED_TIME));
                    resource.setLastUpdaterUserName(resultSet.getString(REG_LAST_UPDATER));
                    resource.setLastModified(resultSet.getTimestamp(REG_LAST_UPDATED_TIME));
                    resource.setDescription(resultSet.getString(REG_DESCRIPTION));
                    resource.setDbBasedContentID(resultSet.getInt(REG_CONTENT_ID));
                    resource.setUUID(resultSet.getString(REG_UUID));
                    return resource;
                }
            }
        } catch (SQLException e) {
            throw new LightRegistryException("Error while retrieving registry resource.", e);
        }
        return null;
    }

    private Map<String, String> executeGetResourceProperties(Connection connection, int tenantId, ResourceID resourceId)
            throws LightRegistryException {

        boolean isCollection = resourceId.isCollection();
        Map<String, String> properties = new HashMap<>();
        try {
            NamedPreparedStatement statement;
            if (isCollection) {
                statement = new NamedPreparedStatement(connection,
                        RegistrySQLQueries.GET_REG_RESOURCE_PROPERTY_COLLECTION);
            } else {
                statement = new NamedPreparedStatement(connection,
                        RegistrySQLQueries.GET_REG_RESOURCE_PROPERTY_RESOURCE);
                statement.setString(REG_NAME, resourceId.getResourceName());
            }
            statement.setInt(PATH_ID, resourceId.getPathID());
            statement.setInt(TENANT_ID, tenantId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String key = resultSet.getString(1);
                    String value = resultSet.getString(2);
                    properties.put(key, value);
                }
            }
        } catch (SQLException e) {
            throw new LightRegistryException("Error while retrieving registry resource properties.", e);
        }
        return properties;
    }

    private Integer[] getPropertyIds(ResourceImpl resource, int tenantId) throws LightRegistryException {

        List<Integer> propIds = new ArrayList<>();

        try (Connection connection = IdentityDatabaseUtil.getGovernanceDBConnection(false)) {
            NamedPreparedStatement statement1;
            if (resource instanceof CollectionImpl) {
                statement1 = new NamedPreparedStatement(connection, RegistrySQLQueries.GET_REG_PROPERTY_ID_COLLECTION);
                statement1.setInt(PATH_ID, resource.getPathID());
                statement1.setInt(TENANT_ID, tenantId);
            } else {
                statement1 = new NamedPreparedStatement(connection, RegistrySQLQueries.GET_REG_PROPERTY_ID_RESOURCE);
                statement1.setInt(PATH_ID, resource.getPathID());
                statement1.setString(REG_NAME, resource.getName());
                statement1.setInt(TENANT_ID, tenantId);
            }
            try (ResultSet resultSet = statement1.executeQuery()) {
                while (resultSet.next()) {
                    propIds.add(resultSet.getInt(REG_PROPERTY_ID));
                }
            }

        } catch (SQLException e) {
            throw new LightRegistryException("Error while retrieving registry resource property ids.", e);
        }
        return propIds.toArray(new Integer[0]);
    }
}
