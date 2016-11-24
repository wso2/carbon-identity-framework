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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.mgt.IdentityMgtConfig;
import org.wso2.carbon.identity.mgt.constants.IdentityMgtConstants;
import org.wso2.carbon.identity.mgt.dto.UserRecoveryDataDO;
import org.wso2.carbon.identity.mgt.internal.IdentityMgtServiceComponent;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.exceptions.ResourceNotFoundException;

import java.util.Properties;

public class RegistryRecoveryDataStore implements UserRecoveryDataStore {

    private static final Log log = LogFactory.getLog(RegistryRecoveryDataStore.class);

    @Override
    public void store(UserRecoveryDataDO recoveryDataDO) throws IdentityException {
        Registry registry = null;
        try {
            registry = IdentityMgtServiceComponent.getRegistryService().
                    getConfigSystemRegistry(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
            registry.beginTransaction();
            Resource resource = registry.newResource();
            resource.setProperty(SECRET_KEY, recoveryDataDO.getSecret());
            resource.setProperty(USER_ID, recoveryDataDO.getUserName());
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

        Collection collection = null;
        try {
            collection = (Collection) registry.get(secretKeyPath.toLowerCase());
        } catch (RegistryException e) {
            log.error("Error while deleting the old confirmation code. Unable to find data collection in registry." + e);
        }

        try {
            if (collection != null) {
                String[] resources = collection.getChildren();
                for (String resource : resources) {
                    String[] splittedResource = resource.split("___");
                    if (splittedResource.length == 3) {
                        //PRIMARY USER STORE
                        if (resource.contains("___" + userName.toLowerCase() + "___")) {

                            registry.beginTransaction();
                            // Check whether the resource still exists for concurrent cases.
                            if (registry.resourceExists(resource)) {
                                registry.delete(resource);
                                registry.commitTransaction();
                            } else {
                                // Already deleted by another thread. Do nothing.
                                registry.rollbackTransaction();
                                if (log.isDebugEnabled()) {
                                    log.debug("Confirmation code already deleted in path of resource : " + resource);
                                }
                            }
                        }
                    } else if (splittedResource.length == 2) {
                        //SECONDARY USER STORE. Resource is a collection.
                        deleteOldResourcesIfFound(registry, userName, resource);
                    }
                }
            }
        } catch (RegistryException e) {
            log.error("Error while deleting the old confirmation code \n" + e);
        }
    }
}
