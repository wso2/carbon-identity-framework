/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.identity.core.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.IdentityRegistryResources;
import org.wso2.carbon.identity.core.model.OpenIDUserDO;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.utils.Transaction;
import org.wso2.carbon.user.core.UserRealm;

public class OpenIDUserDAO extends AbstractDAO<OpenIDUserDO> {

    protected Log log = LogFactory.getLog(OpenIDUserDAO.class);
    private UserRealm realm;

    public OpenIDUserDAO(Registry registry, UserRealm realm) {
        this.registry = registry;
        this.realm = realm;
    }

    protected OpenIDUserDO resourceToObject(Resource resource) {
        OpenIDUserDO openIDUserDo = new OpenIDUserDO();
        openIDUserDo.setUserName(resource
                .getProperty(IdentityRegistryResources.PROP_OPENID_SIGN_UP_USERID));
        openIDUserDo.setOpenID(resource.getProperty(IdentityRegistryResources.PROP_OPENID));
        return openIDUserDo;
    }

    public boolean addAssociation(OpenIDUserDO openIDuserDO) {
        String path = null;
        Resource resource = null;
        Collection userResource = null;

        try {
            if (openIDuserDO == null) {
                return false;
            }

            path = IdentityRegistryResources.OPENID_SIGN_UP
                    + getOpenIdModified(openIDuserDO.getOpenID());

            if (registry.resourceExists(path)) {
                if (log.isInfoEnabled()) {
                    log.info("Already added Signed-Up for the OpenId " + openIDuserDO.getOpenID());
                }
                return false;
            }

            resource = registry.newResource();
            resource.addProperty(IdentityRegistryResources.PROP_OPENID_SIGN_UP_USERID, openIDuserDO
                    .getUserName());
            resource.addProperty(IdentityRegistryResources.PROP_OPENID, openIDuserDO.getOpenID());
            boolean transactionStarted = Transaction.isStarted();
            try {

                if (!transactionStarted) {
                    registry.beginTransaction();
                }
                registry.put(path, resource);

                if (!registry.resourceExists(RegistryConstants.PROFILES_PATH
                        + openIDuserDO.getUserName())) {
                    userResource = registry.newCollection();
                    registry.put(RegistryConstants.PROFILES_PATH + openIDuserDO.getUserName(),
                            userResource);
                } else {
                    //userResource = (Collection) registry.get(RegistryConstants.PROFILES_PATH
                    //        + openIDuserDO.getUserName());
                }

                // Add the association
                registry.addAssociation(RegistryConstants.PROFILES_PATH + openIDuserDO.getUserName(),
                        path, IdentityRegistryResources.ASSOCIATION_USER_OPENID);
                if (!transactionStarted) {
                    registry.commitTransaction();
                }
            } catch (Exception e) {
                if (!transactionStarted) {
                    registry.rollbackTransaction();
                }
                if (e instanceof RegistryException) {
                    throw (RegistryException) e;
                } else {
                    log.error("Error adding OpenID Sign-Up", e);
                }
            }


        } catch (RegistryException e) {
            log.error("Error adding OpenID Sign-Up", e);
        }

        return true;
    }

    public String getUserIdForAssociation(String openId) {

        try {
            if (registry
                    .resourceExists(IdentityRegistryResources.OPENID_SIGN_UP
                            + getOpenIdModified(openId))) {
                return resourceToObject(
                        registry.get(IdentityRegistryResources.OPENID_SIGN_UP
                                + getOpenIdModified(openId))).getUserName();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Unable to find an Sign-Up for " + openId);
                }
            }
        } catch (RegistryException e) {
            log.error("Error retrieving a resource from Registry", e);
        }

        return null;
    }

    public boolean hasAssociation(String openId) {
        try {
            if (registry.resourceExists(IdentityRegistryResources.OPENID_SIGN_UP
                    + getOpenIdModified(openId))) {

                OpenIDUserDO openIDUserDo = resourceToObject(registry
                        .get(IdentityRegistryResources.OPENID_SIGN_UP + getOpenIdModified(openId)));

                // Checks whether the user is existing.
                if (realm.getUserStoreManager().isExistingUser(openIDUserDo.getUserName())) {
                    return true;
                } else {
                    registry.delete(IdentityRegistryResources.OPENID_SIGN_UP
                            + getOpenIdModified(openId));
                    return false;
                }

            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Unable to find an Sign-Up for " + openId);
                }
            }
        } catch (RegistryException e) {
            log.error("Error retrieving the resource", e);
        } catch (Exception e) {
            log.error("Error retrieving  the user information from user store", e);
        }
        return false;
    }

    /**
     * Get all OpenIDs of an user
     *
     * @param username
     * @return
     */
    public String[] getOpenIDsForUser(String username) {
        String[] openIDSet = new String[0];
        Resource openIDResource = null;

        try {
            if (registry.resourceExists(RegistryConstants.PROFILES_PATH + username)) {
                Association[] openIDAssociations = registry.getAssociations(
                        RegistryConstants.PROFILES_PATH + username,
                        IdentityRegistryResources.ASSOCIATION_USER_OPENID);
                openIDSet = new String[openIDAssociations.length];

                int i = 0;

                for (Association association : openIDAssociations) {
                    String openIDAssociation = association.getDestinationPath().trim();
                    String openID = "";
                    if (registry.resourceExists(openIDAssociation)) {
                        openIDResource = registry.get(openIDAssociation);
                        openID = openIDResource.getProperty(IdentityRegistryResources.PROP_OPENID);
                    }
                    openIDSet[i] = openID;
                    i++;
                }
            }
        } catch (RegistryException e) {
            log.error("Error retrieving user information from registry.", e);
        }
        return openIDSet;
    }

    public void removeOpenIDSignUp(String openID) {
        try {
            if (registry.resourceExists(IdentityRegistryResources.OPENID_SIGN_UP + getOpenIdModified(openID))) {
                registry.delete(IdentityRegistryResources.OPENID_SIGN_UP + getOpenIdModified(openID));
            }
        } catch (RegistryException e) {
            log.error("Error Removing the OpenID", e);
        }
    }

    private String getOpenIdModified(String openId) {
        openId = openId.trim().replace("/", "FORWARD_SLASH");
        openId = openId.replace("=", "WSO2_EQUAL_SIGN");
        return openId;
    }
}
