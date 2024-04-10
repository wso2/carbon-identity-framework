/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.core.persistence.registry;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.dao.RegistryDAO;
import org.wso2.carbon.registry.core.CollectionImpl;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

/**
 * A Util OSGi service that exposes Registry resource management functionality based on locale.
 */
public class RegistryResourceMgtServiceImpl implements RegistryResourceMgtService {

    private static final Log log = LogFactory.getLog(RegistryResourceMgtServiceImpl.class);
    private static final RegistryDAO registryDAO = new RegistryDAO();
    private static final String PATH_SPLIT_REGEX = "/(?!.*/)";
    private static final String PATH_VALID_REGEX = ".+/.+";
    private static final String AUTHOR = "wso2.system.user";
    private static final String EN_US = "en_us";
    private static final String BLACKLIST_REGEX = ".*[/\\\\<>`\"].*";

    private static final String MSG_RESOURCE_PERSIST = "Resource persisted at %s in %s tenant registry.";

    private static final String ERROR_GET_RESOURCE = "Error retrieving registry resource from %s for tenant %s.";
    private static final String ERROR_ADD_RESOURCE = "Error adding registry resource to %s tenant at %s. Resource " +
            "already exists at the path";
    private static final String ERROR_DELETE_RESOURCE = "Error deleting registry resource of tenant : %s at %s.";

    private static final String ERROR_PERSIST_RESOURCE = "Error persisting registry resource of %s tenant at %s";
    private static final String ERROR_NO_RESOURCE_FOUND = "Resource does not exist at %s in %s tenant domain.";

    @Override
    public Resource getIdentityResource(String path,
                                        String tenantDomain,
                                        String locale) throws IdentityRuntimeException {

        path = getRegistryPath(path, locale);
        return getIdentityResource(path, tenantDomain);
    }

    @Override
    public void putIdentityResource(Resource identityResource,
                                    String path,
                                    String tenantDomain,
                                    String locale) throws IdentityRuntimeException {

        path = getRegistryPath(path, locale);
        putIdentityResource(identityResource, path, tenantDomain);
    }

    @Override
    public void addIdentityResource(Resource identityResource,
                                    String path,
                                    String tenantDomain,
                                    String locale) throws IdentityRuntimeException {

        path = getRegistryPath(path, locale);
        addIdentityResource(identityResource, path, tenantDomain);
    }

    @Override
    public void deleteIdentityResource(String path,
                                       String tenantDomain,
                                       String locale) throws IdentityRuntimeException {

        path = getRegistryPath(path, locale);
        deleteIdentityResource(path, tenantDomain);
    }

    @Override
    public boolean isResourceExists(String path,
                                    String tenantDomain,
                                    String locale) throws IdentityRuntimeException {

        path = getRegistryPath(path, locale);
        return isResourceExists(path, tenantDomain);
    }

    @Override
    public Resource getIdentityResource(String path,
                                        String tenantDomain) throws IdentityRuntimeException {

        startTenantFlow(tenantDomain);
        try {
            Resource resource = null;
            if (isResourceExists(path, tenantDomain)) {
                String rootPath = getRootPath(path);
                resource = getResourceForPath(rootPath, tenantDomain);
                if (resource instanceof CollectionImpl) {
                    resource = getCollectionResource(resource, path, tenantDomain);
                } else {
                    resource = getResourceObject(resource, rootPath, tenantDomain);
                }
            }
            return resource;
        } catch (RegistryException | IdentityException e) {
            String errorMsg = String.format(ERROR_GET_RESOURCE, path, tenantDomain);
            throw IdentityRuntimeException.error(errorMsg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public void putIdentityResource(Resource identityResource,
                                    String path,
                                    String tenantDomain) throws IdentityRuntimeException {

        startTenantFlow(tenantDomain);
        try {
            String rootPath = getRootPath(path);
            if (isResourceExists(path, tenantDomain)) {
                Resource resource = getResourceForPath(rootPath, tenantDomain);
                if (resource instanceof CollectionImpl) {
                    int pathId = registryDAO.getPathId(rootPath, tenantDomain);
                    registryDAO.deleteResourceObj(pathId, null, tenantDomain);
                } else {
                    String[] pathComponents = rootPath.split(PATH_SPLIT_REGEX);
                    int parentPathId = registryDAO.getPathId(pathComponents[0], tenantDomain);
                    registryDAO.deleteResourceObj(parentPathId, pathComponents[1], tenantDomain);
                }
            }
            addIdentityResource(identityResource, path, tenantDomain);

        } catch (IdentityException e) {
            String errorMsg = String.format(ERROR_PERSIST_RESOURCE, tenantDomain, path);
            throw IdentityRuntimeException.error(errorMsg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public void addIdentityResource(Resource identityResource,
                                    String path,
                                    String tenantDomain) throws IdentityRuntimeException {

        startTenantFlow(tenantDomain);
        try {
            if (isResourceExists(path, tenantDomain)) {
                // resource already exists at the path, so we throw an exception
                String errorMsg = String.format(ERROR_ADD_RESOURCE, tenantDomain, path);
                throw IdentityRuntimeException.error(errorMsg);
            }
            String rootPath = getRootPath(path);
            String parentPath = rootPath.split(PATH_SPLIT_REGEX)[0];
            int parentPathId;
            setAuthorUserName(identityResource);
            if (identityResource instanceof CollectionImpl) {
                parentPathId = insertPathRecursive(parentPath, tenantDomain);
                registryDAO.addCollection(identityResource, rootPath, parentPathId, tenantDomain);
            } else {
                parentPathId = registryDAO.getPathId(parentPath, tenantDomain);
                registryDAO.addResource(identityResource, parentPathId, rootPath.split(PATH_SPLIT_REGEX)[1],
                        tenantDomain);
            }
            if (log.isDebugEnabled()) {
                log.debug(String.format(MSG_RESOURCE_PERSIST, rootPath, tenantDomain));
            }
        } catch (IdentityException e) {
            String errorMsg = String.format(ERROR_PERSIST_RESOURCE, tenantDomain, path);
            throw IdentityRuntimeException.error(errorMsg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public void deleteIdentityResource(String path,
                                       String tenantDomain) throws IdentityRuntimeException {

        startTenantFlow(tenantDomain);
        try {
            if (isResourceExists(path, tenantDomain)) {
                deletePathRecursive(getRootPath(path), tenantDomain);
            } else {
                String errorMsg = String.format(ERROR_NO_RESOURCE_FOUND, path, tenantDomain);
                log.error(errorMsg);
                throw IdentityRuntimeException.error(errorMsg);
            }
        } catch (RegistryException | IdentityException e) {
            String errorMsg = String.format(ERROR_DELETE_RESOURCE, tenantDomain, path);
            throw IdentityRuntimeException.error(errorMsg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public boolean isResourceExists(String path,
                                    String tenantDomain) throws IdentityRuntimeException {

        startTenantFlow(tenantDomain);
        try {
            path = getRootPath(path);
            int pathId = registryDAO.getPathId(path, tenantDomain);
            if (pathId == -1) {
                if (!path.matches(PATH_VALID_REGEX)) {
                    return false;
                }
                String[] pathComponents = path.split(PATH_SPLIT_REGEX);
                int parentPathId = registryDAO.getPathId(pathComponents[0], tenantDomain);
                if (parentPathId == -1) {
                    return false;
                }
                return registryDAO.isResourceExists(parentPathId, pathComponents[1], tenantDomain);
            }
            return true;
        } catch (IdentityException e) {
            String errorMsg = "Error when checking for resource existence at %s in %s tenant domain.";
            throw IdentityRuntimeException.error(String.format(errorMsg, path, tenantDomain), e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Get the registry path for the resource based on it's locale. Here we follow a convention that the leaf element
     * of the path is the resource and we name the resource by it's locale value. We can derive the type of the resource
     * using the path. ( eg: /identity/challengeQuestions/Set1/question1/en_us)
     *
     * @param path   Path to the resource relative to the root of the configuration registry to the parent directory of
     *               the resource
     * @param locale Locale of the resource which will also be the name of the resource.
     * @return Registry path.
     */
    private String getRegistryPath(String path, String locale) {

        locale = validateLocale(locale);
        path = path + RegistryConstants.PATH_SEPARATOR + locale;
        return path;
    }

    private void startTenantFlow(String tenantDomain) {

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
    }

    /**
     * Validate whether the provided locale string contains invalid characters. If and empty or null string is provided
     * we consider the locale as the default locale en_US
     *
     * @param locale Locale of the resource which will also be the name of the resource.
     * @return Locale.
     * @throws IllegalArgumentException Exception while validating locale.
     */
    private String validateLocale(String locale) {

        String localeString = StringUtils.isBlank(locale) ? EN_US : locale.toLowerCase();

        if (localeString.matches(BLACKLIST_REGEX)) {
            throw new IllegalArgumentException("Locale contains invalid special characters : " + locale);
        }
        return localeString;
    }

    private String getRootPath(String path) {

        // TODO: check if we can get this dynamically
        return "/_system/config" + path;
    }

    private Resource getResourceForPath(String path, String tenantDomain) throws IdentityException {

        int pathId = registryDAO.getPathId(path, tenantDomain);
        if (pathId == -1) {
            return new ResourceImpl();
        }
        CollectionImpl resource = new CollectionImpl();
        resource.setPathID(pathId);
        return resource;
    }

    private Resource getCollectionResource(Resource resource, String path, String tenantDomain)
            throws IdentityException, RegistryException {

        CollectionImpl collection = (CollectionImpl) resource;
        collection.setPath(path);
        registryDAO.getCollectionProperties(collection.getPathID(), tenantDomain)
                .forEach(collection::addProperty);
        collection.setChildren(
                registryDAO.getCollectionChildren(path, collection.getPathID(), tenantDomain));

        return collection;
    }

    private Resource getResourceObject(Resource resource, String path, String tenantDomain)
            throws IdentityException, RegistryException {

        ResourceImpl resourceObj = (ResourceImpl) resource;
        resourceObj.setPath(path);
        String resourceName = path.split(PATH_SPLIT_REGEX)[1];
        path = path.split(PATH_SPLIT_REGEX)[0];
        int parentPathId = registryDAO.getPathId(path, tenantDomain);
        registryDAO.getResourceProperties(parentPathId, resourceName, tenantDomain)
                .forEach(resourceObj::setProperty);
        resourceObj.setContent(registryDAO.getResourceContent(parentPathId, resourceName, tenantDomain));

        return resourceObj;
    }

    private int insertPathRecursive(String path, String tenantDomain) throws IdentityException {

        int currentPathId = registryDAO.getPathId(path, tenantDomain);
        if (currentPathId == -1) {
            int parentId = insertPathRecursive(path.split(PATH_SPLIT_REGEX)[0], tenantDomain);
            currentPathId = registryDAO.addCollectionPath(path, parentId, tenantDomain);
        }
        return currentPathId;
    }

    private void deletePathRecursive(String path, String tenantDomain) throws IdentityException, RegistryException {

        Resource resource = getResourceForPath(path, tenantDomain);
        int pathId = registryDAO.getPathId(path, tenantDomain);
        if (resource instanceof CollectionImpl) {
            String[] children = registryDAO.getCollectionChildren(path, pathId, tenantDomain);
            for (String child : children) {
                deletePathRecursive(child, tenantDomain);
            }
            registryDAO.deleteResourceObj(pathId, null, tenantDomain);
            registryDAO.deletePath(pathId, tenantDomain);
        } else {
            String[] pathComponents = path.split(PATH_SPLIT_REGEX);
            int parentPathId = registryDAO.getPathId(pathComponents[0], tenantDomain);
            registryDAO.deleteResourceObj(parentPathId, pathComponents[1], tenantDomain);
        }
    }

    private void setAuthorUserName(Resource identityResource) {

        String authorUserName =
                identityResource.getAuthorUserName() == null ? AUTHOR : identityResource.getAuthorUserName();
        String lastUpdaterUserName =
                identityResource.getLastUpdaterUserName() == null ? AUTHOR : identityResource.getLastUpdaterUserName();
        if (identityResource instanceof CollectionImpl) {
            ((CollectionImpl) identityResource).setAuthorUserName(authorUserName);
            ((CollectionImpl) identityResource).setLastUpdaterUserName(lastUpdaterUserName);
        } else {
            ((ResourceImpl) identityResource).setAuthorUserName(authorUserName);
            ((ResourceImpl) identityResource).setLastUpdaterUserName(lastUpdaterUserName);
        }
    }
}
