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

package org.wso2.carbon.light.registry.mgt.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.light.registry.mgt.LightRegistryException;
import org.wso2.carbon.light.registry.mgt.constants.LightRegistryConstants;
import org.wso2.carbon.light.registry.mgt.dao.RegistryDAO;
import org.wso2.carbon.light.registry.mgt.dao.RegistryDAOImpl;
import org.wso2.carbon.light.registry.mgt.model.CollectionImpl;
import org.wso2.carbon.light.registry.mgt.model.Resource;
import org.wso2.carbon.light.registry.mgt.model.ResourceID;
import org.wso2.carbon.light.registry.mgt.model.ResourceImpl;
import org.wso2.carbon.light.registry.mgt.utils.RegistryUtils;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * A Util OSGi service that exposes Registry resource management functionality based on locale.
 */
public class LightRegistryMgtServiceImpl implements LightRegistryMgtService {

    private static final Log log = LogFactory.getLog(LightRegistryMgtServiceImpl.class);
    private static final RegistryDAO registryDAO = RegistryDAOImpl.getInstance();
    private static final String ILLEGAL_CHARACTERS_FOR_PATH = ".*[~!@#;%^*+={}\\|\\\\<>\",\'].*";
    private static final String MSG_RESOURCE_PERSIST = "Resource persisted at %s in %s tenant registry.";
    private static final String ERROR_GET_RESOURCE = "Error retrieving registry resource from %s for tenant %s.";
    private static final String ERROR_DELETE_RESOURCE = "Error deleting registry resource of tenant : %s at %s.";
    private static final String ERROR_PERSIST_RESOURCE = "Error persisting registry resource of %s tenant at %s";
    private final Pattern illegalCharactersPattern = Pattern.compile(ILLEGAL_CHARACTERS_FOR_PATH);

    @Override
    public Resource getResource(String path,
                                String tenantDomain) throws IdentityRuntimeException {

        startTenantFlow(tenantDomain);
        try {
            int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
            String purePath = RegistryUtils.getPureResourcePath(path);

            ResourceImpl resource = registryDAO.getResourceMetaData(tenantId, purePath);
            if (resource == null) {
                return null;
            }
            registryDAO.fillResource(resource);
            return resource;
        } catch (LightRegistryException e) {
            String errorMsg = String.format(ERROR_GET_RESOURCE, path, tenantDomain);
            throw IdentityRuntimeException.error(errorMsg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public void addOrUpdateResource(Resource resource,
                                    String path,
                                    String tenantDomain) throws IdentityRuntimeException {

        startTenantFlow(tenantDomain);
        try {
            if (illegalCharactersPattern.matcher(path).matches()) {
                throw new IdentityRuntimeException("The path '" + path + "' contains one or more illegal " +
                        "characters " + ILLEGAL_CHARACTERS_FOR_PATH);
            }
            int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
            // validating the resource property names for NULL. This is important when adding properties via the API.
            validateProperties(path, resource);

            String purePath = RegistryUtils.getPureResourcePath(path);

            ResourceID resourceID =
                    registryDAO.getResourceID(purePath, tenantId, resource instanceof CollectionImpl);
            boolean resourceExists = false;
            if (resourceID != null) {

                // load the metadata for the existing resource
                ResourceImpl oldResource = registryDAO.getResourceMetaData(resourceID);

                if (oldResource != null) {
                    // the resource exists
                    resourceExists = true;
                    prepareUpdate((ResourceImpl) resource, resourceID, oldResource);
                    registryDAO.update((ResourceImpl) resource);
                }
            }
            if (!resourceExists) {

                // now we are checking whether there is an entry with the inverse type
                ResourceID inverseResourceID =
                        registryDAO.getResourceID(purePath, tenantId, !(resource instanceof CollectionImpl));
                if (inverseResourceID != null) {
                    ResourceImpl inverseOldResource = registryDAO.getResourceMetaData(inverseResourceID);
                    if (inverseOldResource != null) {
                        // well, in fact there is an inverse type => we are deleting the resource
                        deleteSubTree(inverseResourceID, inverseOldResource);
                    }
                }

                // resource does not exist. Add the resource.
                addResource(resource, purePath, tenantDomain);
            }

        } catch (LightRegistryException e) {
            String errorMsg = String.format(ERROR_PERSIST_RESOURCE, tenantDomain, path);
            throw IdentityRuntimeException.error(errorMsg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public void deleteResource(String path,
                               String tenantDomain) throws IdentityRuntimeException {

        startTenantFlow(tenantDomain);
        try {
            path = RegistryUtils.getPureResourcePath(path);
            int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
            ResourceID resourceID = registryDAO.getResourceID(path, tenantId);
            ResourceImpl resource = registryDAO.getResourceMetaData(resourceID);
            if (resource == null) {
                boolean isCollection = resourceID.isCollection();
                // then we will check for non-collections as the getResourceID only check the collection existence
                if (isCollection) {
                    resourceID = registryDAO.getResourceID(path, tenantId, false);
                    if (resourceID != null) {
                        resource = registryDAO.getResourceMetaData(resourceID);
                    }
                }
                if (resource == null) {
                    String msg = "Failed to delete resource " + path + ". Resource does not exist.";
                    log.error(msg);
                    throw new LightRegistryException(msg);
                }
            }
            deleteSubTree(resourceID, resource);
        } catch (LightRegistryException e) {
            String errorMsg = String.format(ERROR_DELETE_RESOURCE, tenantDomain, path);
            throw IdentityRuntimeException.error(errorMsg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public boolean resourceExists(String path,
                                  String tenantDomain) throws IdentityRuntimeException {

        startTenantFlow(tenantDomain);
        try {
            int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
            ResourceID resourceID = registryDAO.getResourceID(path, tenantId);
            if (resourceID != null) {
                return registryDAO.resourceExists(resourceID);
            }
            return false;
        } catch (LightRegistryException e) {
            String errorMsg = "Error when checking for resource existence at %s in %s tenant domain.";
            throw IdentityRuntimeException.error(String.format(errorMsg, path, tenantDomain), e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Method to add a new resource to the registry.
     *
     * @param resource     the resource object.
     * @param path         the path of the resource.
     * @param tenantDomain the tenant domain.
     * @throws IdentityRuntimeException if the operation failed.
     */
    private void addResource(Resource resource,
                             String path,
                             String tenantDomain) throws IdentityRuntimeException {

        try {
            int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
            String parentPath = RegistryUtils.getParentPath(path);
            // first, let's check if there is a parent for this resource. we do this to speed up the
            // common use case where new resources are added to an existing parent.
            ResourceID parentResourceId = registryDAO.getResourceID(parentPath, tenantId);
            if (parentResourceId == null || !registryDAO.resourceExists(parentResourceId)) {
                addEmptyCollection(parentPath, tenantId);
                if (parentResourceId == null) {
                    parentResourceId = registryDAO.getResourceID(parentPath, tenantId, true);
                }
            }
            if (((ResourceImpl) resource).getUUID() == null) {
                setUUIDForResource((ResourceImpl) resource);
            }
            registryDAO.add(path, tenantId, parentResourceId, (ResourceImpl) resource);

            if (log.isDebugEnabled()) {
                log.debug(String.format(MSG_RESOURCE_PERSIST, path, tenantDomain));
            }
        } catch (LightRegistryException e) {
            String errorMsg = String.format(ERROR_PERSIST_RESOURCE, tenantDomain, path);
            throw IdentityRuntimeException.error(errorMsg, e);
        }
    }

    /**
     * Method to delete a subtree of the collection hierarchy.
     *
     * @param resourceId the resource identifier.
     * @param resource   the resource object.
     * @throws LightRegistryException if the operation failed.
     */
    private void deleteSubTree(ResourceID resourceId, ResourceImpl resource) throws LightRegistryException {

        if (resourceId.isCollection()) {
            // recursively call for all the resources in the tree.
            List<ResourceID> childIds = registryDAO.getChildPathIds(resourceId);
            for (ResourceID childId : childIds) {
                ResourceImpl childResource = registryDAO.getResourceMetaData(childId);
                if (childResource != null) {
                    deleteSubTree(childId, childResource);
                }
            }
        }
        deleteNode(resourceId, resource);
    }

    /**
     * Method to delete just the node in the collection hierarchy.
     *
     * @param resourceID the resource identifier.
     * @param resource   the resource object.
     * @throws LightRegistryException if the operation failed.
     */
    private void deleteNode(ResourceID resourceID,
                           ResourceImpl resource) throws LightRegistryException {

        ResourceImpl resourceImpl;
        if (resourceID.isCollection()) {
            resourceImpl = new CollectionImpl(resourceID.getPath(), resource);
        } else {
            resourceImpl = new ResourceImpl(resourceID.getPath(), resource);
        }

        // just delete the resource and content
        // delete the old entry from the resource table
        removeResource(resourceImpl);
    }

    private void startTenantFlow(String tenantDomain) {

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
    }

    private void addEmptyCollection(String path, int tenantId) throws LightRegistryException {

        ResourceID assumedResourceId = registryDAO.getResourceID(path, tenantId, false);
        if (assumedResourceId != null && registryDAO.resourceExists(assumedResourceId)) {
            String msg = "Failed to add new Collection " + path + "There already exist " +
                    "non collection resource.";
            log.error(msg);
            throw new LightRegistryException(msg);
        }
        String parentPath = RegistryUtils.getParentPath(path);
        ResourceID parentResourceId = null;
        if (parentPath != null) {
            parentResourceId = registryDAO.getResourceID(parentPath, tenantId, true);
            if (parentResourceId == null || !registryDAO.resourceExists(parentResourceId)) {
                addEmptyCollection(parentPath, tenantId);
                if (parentResourceId == null) {
                    parentResourceId = registryDAO.getResourceID(parentPath, tenantId, true);
                }
            }
        } else if (!path.equals(LightRegistryConstants.ROOT_PATH)) {
            return;
        }

        CollectionImpl collection = new CollectionImpl();
        if (collection.getUUID() == null) {
            setUUIDForResource(collection);
        }
        registryDAO.add(path, tenantId, parentResourceId, collection);
    }

    private void setUUIDForResource(ResourceImpl resource) {

        resource.setUUID(UUID.randomUUID().toString());
    }

    /**
     * This method will validate the resource properties to make sure the values are legit.
     *
     * @param path     path of the resource
     * @param resource resource object
     * @throws LightRegistryException If operation failed
     */
    private void validateProperties(String path, Resource resource) throws LightRegistryException {

        Properties properties = resource.getProperties();

        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            if (rejectIfNull(entry.getKey())) {
                String errMsg = "The resource at " + path +
                        " contains a property that has a key with NULL.";
                log.warn(errMsg);
                throw new LightRegistryException(errMsg);
            }
        }
    }

    private boolean rejectIfNull(Object value) {

        return value == null;
    }

    private void prepareUpdate(ResourceImpl resource, ResourceID resourceId, ResourceImpl oldResource)
            throws LightRegistryException {
        // copying the id attribute for the resource
        resource.setPathID(resourceId.getPathID());
        resource.setTenantId(resourceId.getTenantId());
        resource.setName(resourceId.getName());
        resource.setPath(resourceId.getPath());
        // get the original details
        resource.setCreatedTime(oldResource.getCreatedTime());
        resource.setAuthorUserName(oldResource.getAuthorUserName());
        if (resource.getUUID() == null) {
            setUUIDForResource(resource);
        }

        // delete the old resource and content delete the old entry from the resource table
        removeResource(oldResource);
    }

    /**
     * Method to remove a resource.
     *
     * @param resourceImpl the resource.
     * @throws LightRegistryException if the operation failed.
     */
    private void removeResource(ResourceImpl resourceImpl) throws LightRegistryException {

        registryDAO.deleteResource(resourceImpl);
        if (!(resourceImpl instanceof CollectionImpl)) {
            int contentId = resourceImpl.getDbBasedContentID();
            if (contentId > 0) {
                // delete the old content stream from the latest table
                registryDAO.deleteContent(contentId, resourceImpl.getTenantId());
            }
        }
        registryDAO.removeProperties(resourceImpl);
    }
}
