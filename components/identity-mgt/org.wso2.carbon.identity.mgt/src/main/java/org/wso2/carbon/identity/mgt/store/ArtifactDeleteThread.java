/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.identity.mgt.IdentityMgtConfig;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ArtifactDeleteThread implements Runnable {

    private static Log log = LogFactory.getLog(ArtifactDeleteThread.class);

    private static final String USE_HASHED_USERNAME_PROPERTY = "UserInfoRecovery.UseHashedUserNames";
    private static final String USERNAME_HASH_ALG_PROPERTY = "UserInfoRecovery.UsernameHashAlg";
    private Registry registry = null;
    private String userName = null;
    private String secretKeyPath = null;
    private int tenantId = -1234;
    private boolean isDeleteAll = false;

    public ArtifactDeleteThread(Registry registry, String userName, String secretKeyPath, int tenantId,
            boolean isDeleteAll) {

        this.registry = registry;
        this.userName = userName;
        this.secretKeyPath = secretKeyPath;
        this.tenantId = tenantId;
        this.isDeleteAll = isDeleteAll;

    }

    @Override public void run() {

        deleteOldResourcesIfFound();
    }

    private void deleteOldResourcesIfFound() {

        log.debug("---starting delete old resource task---");
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
        Collection collection = null;
        try {
            collection = (Collection) registry.get(secretKeyPath.toLowerCase());
        } catch (RegistryException e) {
            log.error(
                    "Error while deleting the old confirmation code. Unable to find data collection in registry." + e);
        }

        //Introduced property to fix resource not being introduced deleted when special characters are present.
        String userNameToValidate = userName;
        String useHashedUserName = IdentityMgtConfig.getInstance().getProperty(USE_HASHED_USERNAME_PROPERTY);
        if (Boolean.parseBoolean(useHashedUserName)) {
            String hashAlg = IdentityMgtConfig.getInstance().getProperty(USERNAME_HASH_ALG_PROPERTY);
            try {
                userNameToValidate = hashString(userName, hashAlg);
            } catch (NoSuchAlgorithmException e) {
                log.error("Invalid hash algorithm " + hashAlg, e);
            }
        }

        try {
            if (collection != null) {
                String[] resources = collection.getChildren();
                List<Resource> userResources = new ArrayList();
                for (String resource : resources) {
                    String[] splittedResource = resource.split("___");
                    if (splittedResource.length == 3) {
                        //PRIMARY USER STORE
                        if (resource.contains("___" + userNameToValidate.toLowerCase() + "___")) {
                            if (!isDeleteAll) {
                                //add resources belong to particular user to a list
                                userResources.add(registry.get(resource));
                            } else {
                                //if caller is updatePassword method need to delete all resources
                                deleteResource(registry.get(resource).getPath());
                            }

                        }
                    } else if (splittedResource.length == 2) {
                        //SECONDARY USER STORE. Resource is a collection.
                        deleteOldResourcesIfFound();
                    }
                }

                if (!isDeleteAll) {
                    //sort resource list ascending order by expireTime property
                    Collections.sort(userResources, new Comparator<Resource>() {

                        public int compare(Resource r1, Resource r2) {

                            return ((Long) Long.parseLong(r1.getProperty("expireTime")))
                                    .compareTo((Long) Long.parseLong(r2.getProperty("expireTime")));

                        }
                    });

                    //delete all resources except finally created resource
                    for (int i = 0; i < userResources.size() - 1; i++) {
                        deleteResource(userResources.get(i).getPath());
                    }
                }
            }
        } catch (RegistryException e) {
            log.error("Error while deleting the old confirmation code \n" + e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        log.debug("---ending delete old resource task---");
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

    private void deleteResource(String resource) throws RegistryException {

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
}
