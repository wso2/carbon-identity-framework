/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.handler.provisioning.impl;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.core.util.AnonymousSessionUtil;
import org.wso2.carbon.core.util.PermissionUpdateUtil;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.handler.provisioning.ProvisioningHandler;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceComponent;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultProvisioningHandler implements ProvisioningHandler {

    private static final Log log = LogFactory.getLog(DefaultProvisioningHandler.class);
    private static volatile DefaultProvisioningHandler instance;
    private SecureRandom random = new SecureRandom();

    public static DefaultProvisioningHandler getInstance() {
        if (instance == null) {
            synchronized (DefaultProvisioningHandler.class) {
                if (instance == null) {
                    instance = new DefaultProvisioningHandler();
                }
            }
        }
        return instance;
    }

    @Override
    public void handle(List<String> roles, String subject, Map<String, String> attributes,
                       String provisioningUserStoreId, String tenantDomain) throws FrameworkException {

        RegistryService registryService = FrameworkServiceComponent.getRegistryService();
        RealmService realmService = FrameworkServiceComponent.getRealmService();

        try {
            int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
            UserRealm realm = AnonymousSessionUtil.getRealmByTenantDomain(registryService,
                                                                          realmService, tenantDomain);

            String userStoreDomain = getUserStoreDomain(provisioningUserStoreId, realm);

            String username = MultitenantUtils.getTenantAwareUsername(subject);

            UserStoreManager userStoreManager = getUserStoreManager(realm, userStoreDomain);

            // Remove userStoreManager domain from username if the userStoreDomain is not primary
            if (realm.getUserStoreManager().getRealmConfiguration().isPrimary()) {
                username = UserCoreUtil.removeDomainFromName(username);
            }

            String[] newRoles = new String[]{};

            if (roles != null) {
                roles = removeDomainFromNamesExcludeInternal(roles, userStoreManager.getTenantId());
                newRoles = roles.toArray(new String[roles.size()]);
            }

            if (log.isDebugEnabled()) {
                log.debug("User " + username + " contains roles : " + Arrays.toString(newRoles)
                          + " going to be provisioned");
            }

            // addingRoles = newRoles AND allExistingRoles
            Collection<String> addingRoles = getRolesToAdd(userStoreManager, newRoles);

            Map<String, String> userClaims = prepareClaimMappings(attributes);

            if (userStoreManager.isExistingUser(username)) {

                if (roles != null && !roles.isEmpty()) {
                    // Update user
                    Collection<String> currentRolesList = Arrays.asList(userStoreManager
                                                                                .getRoleListOfUser(username));
                    // addingRoles = (newRoles AND existingRoles) - currentRolesList)
                    addingRoles.removeAll(currentRolesList);

                    Collection<String> deletingRoles = new ArrayList<String>();
                    deletingRoles.addAll(currentRolesList);
                    // deletingRoles = currentRolesList - newRoles
                    deletingRoles.removeAll(Arrays.asList(newRoles));

                    // Exclude Internal/everyonerole from deleting role since its cannot be deleted
                    deletingRoles.remove(realm.getRealmConfiguration().getEveryOneRoleName());

                    // TODO : Does it need to check this?
                    // Check for case whether superadmin login
                    handleFederatedUserNameEqualsToSuperAdminUserName(realm, username, userStoreManager, deletingRoles);

                    updateUserWithNewRoleSet(username, userStoreManager, newRoles, addingRoles, deletingRoles);
                }

                if (!userClaims.isEmpty()) {
                    userStoreManager.setUserClaimValues(username, userClaims, null);
                }

            } else {

                userStoreManager.addUser(username, generatePassword(), addingRoles.toArray(
                        new String[addingRoles.size()]), userClaims, null);

                if (log.isDebugEnabled()) {
                    log.debug("Federated user: " + username
                              + " is provisioned by authentication framework with roles : "
                              + Arrays.toString(addingRoles.toArray(new String[addingRoles.size()])));
                }
            }

            PermissionUpdateUtil.updatePermissionTree(tenantId);

        } catch (org.wso2.carbon.user.api.UserStoreException | CarbonException e) {
            throw new FrameworkException("Error while provisioning user : " + subject, e);
        }
    }

    private void updateUserWithNewRoleSet(String username, UserStoreManager userStoreManager, String[] newRoles,
                                          Collection<String> addingRoles, Collection<String> deletingRoles)
            throws UserStoreException {
        if (log.isDebugEnabled()) {
            log.debug("Deleting roles : "
                      + Arrays.toString(deletingRoles.toArray(new String[deletingRoles.size()]))
                      + " and Adding roles : "
                      + Arrays.toString(addingRoles.toArray(new String[addingRoles.size()])));
        }
        userStoreManager.updateRoleListOfUser(username, deletingRoles.toArray(new String[deletingRoles
                                                      .size()]),
                                              addingRoles.toArray(new String[addingRoles.size()]));
        if (log.isDebugEnabled()) {
            log.debug("Federated user: " + username
                      + " is updated by authentication framework with roles : "
                      + Arrays.toString(newRoles));
        }
    }

    private void handleFederatedUserNameEqualsToSuperAdminUserName(UserRealm realm, String username,
                                                                   UserStoreManager userStoreManager,
                                                                   Collection<String> deletingRoles)
            throws UserStoreException, FrameworkException {
        if (userStoreManager.getRealmConfiguration().isPrimary()
            && username.equals(realm.getRealmConfiguration().getAdminUserName())) {
            if (log.isDebugEnabled()) {
                log.debug("Federated user's username is equal to super admin's username of local IdP.");
            }

            // Whether superadmin login without superadmin role is permitted
            if (deletingRoles
                    .contains(realm.getRealmConfiguration().getAdminRoleName())) {
                if (log.isDebugEnabled()) {
                    log.debug("Federated user doesn't have super admin role. Unable to sync roles, since" +
                              " super admin role cannot be unassigned from super admin user");
                }
                throw new FrameworkException(
                        "Federated user which having same username to super admin username of local IdP," +
                        " trying login without having super admin role assigned");
            }
        }
    }

    private Map<String, String> prepareClaimMappings(Map<String, String> attributes) {
        Map<String, String> userClaims = new HashMap<>();
        if (attributes != null && !attributes.isEmpty()) {
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                String claimURI = entry.getKey();
                String claimValue = entry.getValue();
                if (!(StringUtils.isEmpty(claimURI) || StringUtils.isEmpty(claimValue))) {
                    userClaims.put(claimURI, claimValue);
                }
            }
        }
        return userClaims;
    }

    private Collection<String> getRolesToAdd(UserStoreManager userStoreManager, String[] newRoles)
            throws UserStoreException {
        Collection<String> addingRoles = new ArrayList<>();
        Collections.addAll(addingRoles, newRoles);
        Collection<String> allExistingRoles = removeDomainFromNamesExcludeInternal(
                Arrays.asList(userStoreManager.getRoleNames()), userStoreManager.getTenantId());
        addingRoles.retainAll(allExistingRoles);
        return addingRoles;
    }

    private UserStoreManager getUserStoreManager(UserRealm realm, String userStoreDomain)
            throws UserStoreException, FrameworkException {
        UserStoreManager userStoreManager;
        if (userStoreDomain != null && !userStoreDomain.isEmpty()) {
            userStoreManager = realm.getUserStoreManager().getSecondaryUserStoreManager(
                    userStoreDomain);
        } else {
            userStoreManager = realm.getUserStoreManager();
        }

        if (userStoreManager == null) {
            throw new FrameworkException("Specified user store is invalid");
        }
        return userStoreManager;
    }

    /**
     * Compute the user store which user to be provisioned
     *
     * @return
     * @throws UserStoreException
     */
    private String getUserStoreDomain(String userStoreDomain, UserRealm realm)
            throws FrameworkException, UserStoreException {

        // If the any of above value is invalid, keep it empty to use primary userstore
        if (userStoreDomain != null
            && realm.getUserStoreManager().getSecondaryUserStoreManager(userStoreDomain) == null) {
            throw new FrameworkException("Specified user store domain " + userStoreDomain
                                         + " is not valid.");
        }

        return userStoreDomain;
    }

    /**
     * Generates (random) password for user to be provisioned
     *
     * @return
     */
    protected String generatePassword() {
        return RandomStringUtils.randomNumeric(12);
    }

    /**
     * remove user store domain from names except the domain 'Internal'
     *
     * @param names
     * @return
     */
    private List<String> removeDomainFromNamesExcludeInternal(List<String> names, int tenantId) {
        List<String> nameList = new ArrayList<String>();
        for (String name : names) {
            String userStoreDomain = IdentityUtil.extractDomainFromName(name);
            if (UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(userStoreDomain)) {
                nameList.add(name);
            } else {
                nameList.add(UserCoreUtil.removeDomainFromName(name));
            }
        }
        return nameList;
    }
}
