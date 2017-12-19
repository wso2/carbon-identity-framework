/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
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

package org.wso2.carbon.identity.mgt.store;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.mgt.IdentityMgtConfig;
import org.wso2.carbon.identity.mgt.constants.IdentityMgtConstants;
import org.wso2.carbon.identity.mgt.dto.UserRecoveryDataDO;
import org.wso2.carbon.identity.mgt.internal.IdentityMgtServiceComponent;
import org.wso2.carbon.registry.common.ResourceData;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.exceptions.ResourceNotFoundException;
import org.wso2.carbon.registry.indexing.RegistryConfigLoader;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class RegistryRecoveryDataStore implements UserRecoveryDataStore {

    private static final Log log = LogFactory.getLog(RegistryRecoveryDataStore.class);

    private static final String USE_HASHED_USERNAME_PROPERTY = "UserInfoRecovery.UseHashedUserNames";
    private static final String USERNAME_HASH_ALG_PROPERTY = "UserInfoRecovery.UsernameHashAlg";
    // Registry search filed properties and values
    private static final String REGISTRY_SEARCH_FIELD_PROPERTY_NAME = "propertyName";
    private static final String REGISTRT_SEARCH_FIELD_RIGHT_PROPERTY_VALUE = "rightPropertyValue";
    private static final String REGISTRY_SEARCH_FIELD_RIGHT_OP = "rightOp";
    private static final String REGISTRY_SEARCH_FIELD_RIGHT_OP_EQ = "eq";
    // Identity management configuration to switch to registry search and indexing when deleting old confirmation codes.
    private static final String REGISTRY_INDEXING_ENABLED = "Identity.Mgt.Registry.Indexing";

    @Override
    public void store(UserRecoveryDataDO recoveryDataDO) throws IdentityException {
        Registry registry = null;
        try {
            String tenantDomain = IdentityTenantUtil.getTenantDomain(recoveryDataDO.getTenantId());
            IdentityTenantUtil.initializeRegistry(recoveryDataDO.getTenantId(), tenantDomain);
            registry = IdentityMgtServiceComponent.getRegistryService().
                    getConfigSystemRegistry(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
            registry.beginTransaction();
            Resource resource = registry.newResource();
            resource.setProperty(SECRET_KEY, recoveryDataDO.getSecret());
            /*
            Converted username to lower case to search the resource over 'userId' property easily, as username
            included in the registry resource path is always converted to lowercase, and at any point case sensitive
            usernames are not supported.
             */
            resource.setProperty(USER_ID, recoveryDataDO.getUserName().toLowerCase());
            resource.setProperty(EXPIRE_TIME, recoveryDataDO.getExpireTime());
            resource.setVersionableChange(false);
            String confirmationKeyPath = IdentityMgtConstants.IDENTITY_MANAGEMENT_DATA + "/" + recoveryDataDO.getCode
                    ().toLowerCase();
            registry.put(confirmationKeyPath, resource);
        } catch (RegistryException e) {
            log.error(e);
            throw IdentityException.error("Error while persisting user recovery data for user : " +
                    recoveryDataDO.getUserName());
        } finally {
            if (registry != null) {
                try {
                    registry.commitTransaction();
                } catch (RegistryException e) {
                    log.error("Error while processing registry transaction", e);
                }
            }
        }
    }

    @Override
    public void store(UserRecoveryDataDO[] recoveryDataDOs) throws IdentityException {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public UserRecoveryDataDO load(String code) throws IdentityException {

        Registry registry = null;
        UserRecoveryDataDO dataDO = new UserRecoveryDataDO();

        try {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
            IdentityTenantUtil.initializeRegistry(tenantId, tenantDomain);
            registry = IdentityMgtServiceComponent.getRegistryService().
                    getConfigSystemRegistry(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());

            registry.beginTransaction();
            String secretKeyPath = IdentityMgtConstants.IDENTITY_MANAGEMENT_DATA +
                    RegistryConstants.PATH_SEPARATOR + code.toLowerCase();
            if (registry.resourceExists(secretKeyPath)) {
                Resource resource = registry.get(secretKeyPath);
                Properties props = resource.getProperties();
                for (Object o : props.keySet()) {
                    String key = (String) o;
                    if (key.equals(USER_ID)) {
                        dataDO.setUserName(resource.getProperty(key));
                    } else if (key.equals(SECRET_KEY)) {
                        dataDO.setSecret(resource.getProperty(key));
                    } else if (key.equals(EXPIRE_TIME)) {
                        String time = resource.getProperty(key);
                        dataDO.setExpireTime(time);

                        if (System.currentTimeMillis() > Long.parseLong(time)) {
                            dataDO.setValid(false);
                            break;
                        } else {
                            dataDO.setValid(true);
                        }
                    }
                }
            } else {
                return null;
            }
        } catch (RegistryException e) {
            log.error(e);
            throw IdentityException.error("Error while loading user recovery data for code : " + code);
        } finally {
            if (registry != null) {
                try {
                    registry.commitTransaction();
                } catch (RegistryException e) {
                    log.error("Error while processing registry transaction", e);
                }
            }
        }

        return dataDO;
    }

    @Override
    public void invalidate(String code) throws IdentityException {

        Registry registry = null;
        try {
            registry = IdentityMgtServiceComponent.getRegistryService().
                    getConfigSystemRegistry(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());

            registry.beginTransaction();
            String secretKeyPath = IdentityMgtConstants.IDENTITY_MANAGEMENT_DATA +
                    RegistryConstants.PATH_SEPARATOR + code.toLowerCase();
            if (registry.resourceExists(secretKeyPath)) {
                registry.delete(secretKeyPath);
            }
        } catch (RegistryException e) {
            log.error(e);
            throw IdentityException.error("Error while invalidating user recovery data for code : " + code);
        } finally {
            if (registry != null) {
                try {
                    registry.commitTransaction();
                } catch (RegistryException e) {
                    log.error("Error while processing registry transaction", e);
                }
            }
        }
    }

    @Override
    public void invalidate(UserRecoveryDataDO recoveryDataDO) throws IdentityException {
        Registry registry = null;
        try {
            registry = IdentityMgtServiceComponent.getRegistryService().
                    getConfigSystemRegistry(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
            registry.beginTransaction();
            String dataPath = IdentityMgtConstants.IDENTITY_MANAGEMENT_DATA;
            Collection dataItems = (Collection)registry.get(dataPath);
            for (int i = 0; i < dataItems.getChildren().length; i++) {
                Resource currentResource;
                try {
                    currentResource = registry.get(dataItems.getChildren()[i]);
                } catch (ResourceNotFoundException exception) {
                    if (log.isDebugEnabled()) {
                        log.debug("Resource :" + dataItems.getChildren()[i] + " is already deleted");
                    }
                    continue;
                }
                if (currentResource instanceof Collection) {
                    String[] currentResourceChildren = ((Collection) currentResource).getChildren();
                    for (int j = 0; j < currentResourceChildren.length; j++) {
                        Resource innerResource;
                        try {
                            innerResource = registry.get(currentResourceChildren[i]);
                        } catch (ResourceNotFoundException exception) {
                            if (log.isDebugEnabled()) {
                                log.debug("Resource :" + registry.get(currentResourceChildren[i]) + " is already deleted");
                            }
                            continue;
                        }
                        if (innerResource.getProperty(SECRET_KEY).equals(recoveryDataDO.getSecret())) {
                            registry.delete(currentResourceChildren[j]);
                            return;
                        }
                    }
                } else {
                    if (currentResource.getProperty(SECRET_KEY).equals(recoveryDataDO.getSecret())) {
                        registry.delete(dataItems.getChildren()[i]);
                        return;
                    }
                }
            }
        } catch (RegistryException e) {
            throw IdentityException.error("Error while deleting resource after loading", e);
        } finally {
            if (registry != null) {
                try {
                    registry.commitTransaction();
                } catch (RegistryException e) {
                    log.error("Error while deleting resource after loading.", e);
                }
            }
        }

    }

    @Override
    public void invalidate(String userId, int tenantId) throws IdentityException {
        Registry registry = null;
        try {
            registry = IdentityMgtServiceComponent.getRegistryService().
                    getConfigSystemRegistry(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
            if (IdentityMgtConfig.getInstance().getPoolSize() <= 0) {
                deleteOldResourcesIfFound(registry, userId, IdentityMgtConstants.IDENTITY_MANAGEMENT_DATA);
            } else {
                StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
                if (!"updatePassword".equals(stackTraceElements[2].getMethodName())) {
                    ArtifactDeleteThread artifactDeleteThread = new ArtifactDeleteThread(registry, userId,
                            IdentityMgtConstants.IDENTITY_MANAGEMENT_DATA, tenantId, false);
                    IdentityMgtConfig.getInstance().getExecutors().submit(artifactDeleteThread);
                } else {
                    ArtifactDeleteThread artifactDeleteThread = new ArtifactDeleteThread(registry, userId,
                            IdentityMgtConstants.IDENTITY_MANAGEMENT_DATA, tenantId, true);
                    IdentityMgtConfig.getInstance().getExecutors().submit(artifactDeleteThread);
                }
            }
        } catch (RegistryException e) {
            throw IdentityException.error("Error while deleting the old confirmation code.", e);
        }
    }

    @Override
    public UserRecoveryDataDO[] load(String userName, int tenantId) throws IdentityException {
        return new UserRecoveryDataDO[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    private void deleteOldResourcesIfFound(Registry registry, String userName, String secretKeyPath) {

        boolean useRegistryIndexing = Boolean.parseBoolean(IdentityMgtConfig.getInstance().getProperty
                (REGISTRY_INDEXING_ENABLED));
        if (useRegistryIndexing && RegistryConfigLoader.getInstance().IsStartIndexing()) {

            if (log.isDebugEnabled()) {
                log.debug("Property: " + REGISTRY_INDEXING_ENABLED + " is enabled. Switching to registry search mode " +
                        "" + "to delete old confirmation codes.");
            }
            deleteOldConfirmationCodesByRegistrySearch(registry, userName, secretKeyPath);

        } else {

            if (log.isDebugEnabled()) {
                log.debug("Deleting old confirmation codes iterating over registry resource collection at: " +
                        secretKeyPath);
            }
            deleteOldConfirmationCodesByResourceIteration(registry, userName, secretKeyPath);
        }
    }

    private void deleteOldConfirmationCodesByRegistrySearch(Registry registry, String username, String
            confirmationCodePath) {

        Map<String, String> fields = new HashMap<>();
        fields.put(REGISTRY_SEARCH_FIELD_PROPERTY_NAME, UserRecoveryDataStore.USER_ID);
        /*
        Convert the username to lowercase as 'userId' property always includes the lowercase username.
        @see #store
         */
        fields.put(REGISTRT_SEARCH_FIELD_RIGHT_PROPERTY_VALUE, username.toLowerCase());
        fields.put(REGISTRY_SEARCH_FIELD_RIGHT_OP, REGISTRY_SEARCH_FIELD_RIGHT_OP_EQ);
        ResourceData[] searchResults = null;
        try {
            searchResults = IdentityMgtServiceComponent.getAttributeSearchService().search(fields);
        } catch (RegistryException e) {
            log.error("Error while deleting the old confirmation code. Unable to search resources in registry " +
                    "for: [" + REGISTRY_SEARCH_FIELD_PROPERTY_NAME + " - " + UserRecoveryDataStore.USER_ID + ", " +
                    REGISTRT_SEARCH_FIELD_RIGHT_PROPERTY_VALUE + " - " + username + ", " +
                    REGISTRY_SEARCH_FIELD_RIGHT_OP + " - " + REGISTRY_SEARCH_FIELD_RIGHT_OP_EQ + "]", e);
        }

        if (searchResults != null && !ArrayUtils.isEmpty(searchResults)) {

            if (log.isDebugEnabled()) {
                log.debug("Found: " + searchResults.length + " no of resources for search: [" +
                        REGISTRY_SEARCH_FIELD_PROPERTY_NAME + " - " + UserRecoveryDataStore.USER_ID + ", " +
                        REGISTRT_SEARCH_FIELD_RIGHT_PROPERTY_VALUE + " - " + username + ", " +
                        REGISTRY_SEARCH_FIELD_RIGHT_OP + " - " + REGISTRY_SEARCH_FIELD_RIGHT_OP_EQ + "]");
            }

            for (ResourceData resource : searchResults) {
                String resourcePath = resource.getResourcePath();
                if (resourcePath != null && resourcePath.contains(confirmationCodePath)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Matching resource found for user: " + username + " at resource path : " + resource
                                .getResourcePath());
                    }

                    String resourcePathRelativeToConfigRegistry = resource.getResourcePath().substring
                            (RegistryConstants.CONFIG_REGISTRY_BASE_PATH.length());
                    deleteRegistryResource(registry, resourcePathRelativeToConfigRegistry);
                }
            }
        } else {

            if (log.isDebugEnabled()) {
                if (log.isDebugEnabled()) {
                    log.debug("No registry resource found for search: [" + REGISTRY_SEARCH_FIELD_PROPERTY_NAME + " - " +
                            "" + "" + "" + UserRecoveryDataStore.USER_ID + ", " +
                            REGISTRT_SEARCH_FIELD_RIGHT_PROPERTY_VALUE + " - " + username + ", " +
                            REGISTRY_SEARCH_FIELD_RIGHT_OP + " - " + REGISTRY_SEARCH_FIELD_RIGHT_OP_EQ + "]");
                }
            }
        }
    }

    private void deleteOldConfirmationCodesByResourceIteration(Registry registry, String username, String
            confirmationCodePath) {

        Collection collection = null;
        try {
            collection = (Collection) registry.get(confirmationCodePath.toLowerCase());
        } catch (RegistryException e) {
            log.error("Error while deleting the old confirmation code for user: " + username + ". Cannot find " +
                    "resource collection at resource path: " + confirmationCodePath, e);
        }

        if (collection != null) {

            // Use username hashing to fix resource not being deleted when special characters are
            // present in username.
            String userNameToValidate = username;
            String useHashedUserName = IdentityMgtConfig.getInstance().getProperty(USE_HASHED_USERNAME_PROPERTY);
            if (Boolean.parseBoolean(useHashedUserName)) {
                String hashAlg = IdentityMgtConfig.getInstance().getProperty(USERNAME_HASH_ALG_PROPERTY);
                try {
                    userNameToValidate = hashString(username, hashAlg);
                } catch (NoSuchAlgorithmException e) {
                    log.error("Invalid hash algorithm " + hashAlg, e);
                }
            }

            try {
                String[] resources = collection.getChildren();
                for (String resource : resources) {
                    String[] splittedResource = resource.split("___");
                    if (splittedResource.length == 3) {
                        //PRIMARY USER STORE
                        if (resource.contains("___" + userNameToValidate.toLowerCase() + "___")) {
                            deleteRegistryResource(registry, resource);
                        }
                    } else if (splittedResource.length == 2) {
                        //SECONDARY USER STORE. Resource is a collection.
                        deleteOldResourcesIfFound(registry, username, resource);
                    }
                }
            } catch (RegistryException e) {
                log.error("Error while deleting the old confirmation code for user: " + username + " at resource " +
                        "" + "path: " + confirmationCodePath, e);
            }
        } else {

            if (log.isDebugEnabled()) {
                log.debug("No registry resource found at path: " + confirmationCodePath);
            }
        }
    }

    private void deleteRegistryResource(Registry registry, String resourcePathRelativeToRegistry) {

        boolean isTransactionSucceeded = false;
        try {
            registry.beginTransaction();
            // Check whether the resource still exists for concurrent cases.
            if (registry.resourceExists(resourcePathRelativeToRegistry)) {
                if (log.isDebugEnabled()) {
                    log.debug("Deleting registry resource: " + resourcePathRelativeToRegistry);
                }
                registry.delete(resourcePathRelativeToRegistry);
                isTransactionSucceeded = true;
            } else {
                // Already deleted by another thread. Do nothing.
                if (log.isDebugEnabled()) {
                    log.debug("Registry resource: " + resourcePathRelativeToRegistry + " is already deleted.");
                }
            }
        } catch (RegistryException e) {
            log.error("Error while deleting resource: " + resourcePathRelativeToRegistry, e);
        } finally {
            try {
                if (isTransactionSucceeded) {
                    registry.commitTransaction();
                } else {
                    registry.rollbackTransaction();
                }
            } catch (RegistryException e) {
                log.error("Error while committing registry transaction for resource: " + resourcePathRelativeToRegistry, e);
            }
        }
    }

    private String hashString(String userName, String alg) throws NoSuchAlgorithmException {

        MessageDigest messageDigest = MessageDigest.getInstance(alg);
        byte[] in = messageDigest.digest(userName.getBytes());
        final StringBuilder builder = new StringBuilder();
        for (byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();

    }
}
