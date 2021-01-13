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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.AnonymousSessionUtil;
import org.wso2.carbon.core.util.PermissionUpdateUtil;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.handler.provisioning.ProvisioningHandler;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceComponent;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.user.profile.mgt.association.federation.FederatedAssociationManager;
import org.wso2.carbon.identity.user.profile.mgt.association.federation.constant.FederatedAssociationConstants;
import org.wso2.carbon.identity.user.profile.mgt.association.federation.exception.FederatedAssociationManagerException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.claim.Claim;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.InternalRoleDomains.APPLICATION_DOMAIN;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.InternalRoleDomains.WORKFLOW_DOMAIN;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.USERNAME_CLAIM;

public class DefaultProvisioningHandler implements ProvisioningHandler {

    private static final Log log = LogFactory.getLog(DefaultProvisioningHandler.class);
    private static final String ALREADY_ASSOCIATED_MESSAGE = "UserAlreadyAssociated";
    private static final String USER_WORKFLOW_ENGAGED_ERROR_CODE = "WFM-10001";
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
            String username = MultitenantUtils.getTenantAwareUsername(subject);

            String userStoreDomain;
            UserStoreManager userStoreManager;
            if (IdentityApplicationConstants.AS_IN_USERNAME_USERSTORE_FOR_JIT
                    .equalsIgnoreCase(provisioningUserStoreId)) {
                String userStoreDomainFromSubject = UserCoreUtil.extractDomainFromName(subject);
                try {
                    userStoreManager = getUserStoreManager(realm, userStoreDomainFromSubject);
                    userStoreDomain = userStoreDomainFromSubject;
                } catch (FrameworkException e) {
                    log.error("User store domain " + userStoreDomainFromSubject + " does not exist for the tenant "
                            + tenantDomain + ", hence provisioning user to "
                            + UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME);
                    userStoreDomain = UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME;
                    userStoreManager = getUserStoreManager(realm, userStoreDomain);
                }
            } else {
                userStoreDomain = getUserStoreDomain(provisioningUserStoreId, realm);
                userStoreManager = getUserStoreManager(realm, userStoreDomain);
            }
            username = UserCoreUtil.removeDomainFromName(username);

            if (log.isDebugEnabled()) {
                log.debug("User: " + username + " with roles : " + roles + " is going to be provisioned");
            }

            // If internal roles exists convert internal role domain names to pre defined camel case domain names.
            List<String> rolesToAdd = convertInternalRoleDomainsToCamelCase(roles);

            String idp = attributes.remove(FrameworkConstants.IDP_ID);
            String subjectVal = attributes.remove(FrameworkConstants.ASSOCIATED_ID);

            Map<String, String> userClaims = prepareClaimMappings(attributes);

            if (userStoreManager.isExistingUser(username)) {
                if (!userClaims.isEmpty()) {
                    userClaims.remove(FrameworkConstants.PASSWORD);
                    userClaims.remove(USERNAME_CLAIM);
                    userStoreManager.setUserClaimValues(UserCoreUtil.removeDomainFromName(username), userClaims, null);
                    /*
                    Since the user is exist following code is get all active claims of user and crosschecking against
                    tobeDeleted claims (claims came from federated idp as null). If there is a match those claims
                    will be deleted.
                    */
                    List<String> toBeDeletedUserClaims = prepareToBeDeletedClaimMappings(attributes);
                    if (CollectionUtils.isNotEmpty(toBeDeletedUserClaims)) {
                        Claim[] userActiveClaims =
                                userStoreManager.getUserClaimValues(UserCoreUtil.removeDomainFromName(username), null);
                        for (Claim claim : userActiveClaims) {
                            if (toBeDeletedUserClaims.contains(claim.getClaimUri())) {
                                if (log.isDebugEnabled()) {
                                    log.debug("Claim from external attributes " + claim.getClaimUri() +
                                            " has null value But user has not null claim value for Claim " +
                                            claim.getClaimUri() + ". Hence user claim value will be deleted.");
                                }
                                userStoreManager.deleteUserClaimValue(UserCoreUtil.removeDomainFromName(username),
                                        claim.getClaimUri(), null);
                            }
                        }
                    }
                }
                String associatedUserName = FrameworkUtils.getFederatedAssociationManager()
                        .getUserForFederatedAssociation(tenantDomain, idp, subjectVal);
                if (StringUtils.isEmpty(associatedUserName)) {
                    // Associate User
                    associateUser(username, userStoreDomain, tenantDomain, subjectVal, idp);
                }
            } else {
                boolean isUserProvidedPassword = false;
                String password = generatePassword();
                String passwordFromUser = userClaims.get(FrameworkConstants.PASSWORD);
                if (StringUtils.isNotEmpty(passwordFromUser)) {
                    isUserProvidedPassword = true;
                    password = passwordFromUser;
                }

                // Check for inconsistencies in username attribute and the username claim.
                if (userClaims.containsKey(USERNAME_CLAIM) && !userClaims.get(USERNAME_CLAIM).equals(username)) {
                    // If so update the username claim with the username attribute.
                    userClaims.put(USERNAME_CLAIM, username);
                }

                userClaims.remove(FrameworkConstants.PASSWORD);
                boolean userWorkflowEngaged = false;
                try {
                    // This thread local is set to skip the password pattern validation if it is a generated one.
                    if (!isUserProvidedPassword) {
                        UserCoreUtil.setSkipPasswordPatternValidationThreadLocal(true);
                    }
                    userStoreManager.addUser(username, password, null, userClaims, null);
                } catch (UserStoreException e) {
                    // Add user operation will fail if a user operation workflow is already defined for the same user.
                    if (USER_WORKFLOW_ENGAGED_ERROR_CODE.equals(e.getErrorCode())) {
                        userWorkflowEngaged = true;
                        if (log.isDebugEnabled()) {
                            log.debug("Failed to add the user while JIT provisioning since user workflows are engaged" +
                                    " and there is a workflow already defined for the same user");
                        }
                    } else {
                        throw e;
                    }
                } finally {
                    if (!isUserProvidedPassword) {
                        UserCoreUtil.removeSkipPasswordPatternValidationThreadLocal();
                    }
                }

                if (userWorkflowEngaged ||
                        !userStoreManager.isExistingUser(UserCoreUtil.addDomainToName(username, userStoreDomain))) {
                    if (log.isDebugEnabled()) {
                        log.debug("User is not found in the userstore. Most probably the local user creation is not " +
                                "complete while JIT provisioning due to user operation workflow engagement. Therefore" +
                                " the user account association and role and permission update are skipped.");
                    }
                    return;
                }

                // Associate user only if the user is existing in the userstore.
                associateUser(username, userStoreDomain, tenantDomain, subjectVal, idp);

                if (log.isDebugEnabled()) {
                    log.debug("Federated user: " + username + " is provisioned by authentication framework.");
                }
            }

            if (roles != null) {
                // Update user with roles
                List<String> currentRolesList = Arrays.asList(userStoreManager.getRoleListOfUser(username));
                Collection<String> deletingRoles = retrieveRolesToBeDeleted(realm, currentRolesList, rolesToAdd);
                rolesToAdd.removeAll(currentRolesList);

                // TODO : Does it need to check this?
                // Check for case whether superadmin login
                handleFederatedUserNameEqualsToSuperAdminUserName(realm, username, userStoreManager, deletingRoles);

                updateUserWithNewRoleSet(username, userStoreManager, rolesToAdd, deletingRoles);
            }

            PermissionUpdateUtil.updatePermissionTree(tenantId);

        } catch (org.wso2.carbon.user.api.UserStoreException | CarbonException |
                FederatedAssociationManagerException e) {
            throw new FrameworkException("Error while provisioning user : " + subject, e);
        } finally {
            IdentityUtil.clearIdentityErrorMsg();
        }
    }

    protected void associateUser(String username, String userStoreDomain, String tenantDomain, String subject,
                                 String idp) throws FrameworkException {

        String usernameWithUserstoreDomain = UserCoreUtil.addDomainToName(username, userStoreDomain);
        try {
            // start tenant flow
            FrameworkUtils.startTenantFlow(tenantDomain);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(usernameWithUserstoreDomain);

            if (!StringUtils.isEmpty(idp) && !StringUtils.isEmpty(subject)) {
                FederatedAssociationManager federatedAssociationManager = FrameworkUtils
                        .getFederatedAssociationManager();
                User user = getAssociatedUser(tenantDomain, userStoreDomain, username);
                federatedAssociationManager.createFederatedAssociation(user, idp, subject);

                if (log.isDebugEnabled()) {
                    log.debug("Associated local user: " + usernameWithUserstoreDomain + " in tenant: " +
                            tenantDomain + " to the federated subject : " + subject + " in IdP: " + idp);
                }
            } else {
                throw new FrameworkException("Error while associating local user: " + usernameWithUserstoreDomain +
                        " in tenant: " + tenantDomain + " to the federated subject : " + subject + " in IdP: " + idp);
            }
        } catch (FederatedAssociationManagerException e) {
            if (isUserAlreadyAssociated(e)) {
                log.info("An association already exists for user: " + subject + ". Skip association while JIT " +
                        "provisioning");
            } else {
                throw new FrameworkException("Error while associating local user: " + usernameWithUserstoreDomain +
                        " in tenant: " + tenantDomain + " to the federated subject : " + subject + " in IdP: " + idp, e);
            }
        } finally {
            // end tenant flow
            FrameworkUtils.endTenantFlow();
        }
    }

    private User getAssociatedUser(String tenantDomain, String userStoreDomain, String username) {

        User user = new User();
        user.setTenantDomain(tenantDomain);
        user.setUserStoreDomain(userStoreDomain);
        user.setUserName(MultitenantUtils.getTenantAwareUsername(username));
        return user;
    }

    private boolean isUserAlreadyAssociated(FederatedAssociationManagerException e) {

        return e.getMessage() != null && e.getMessage().contains(FederatedAssociationConstants.ErrorMessages
                .FEDERATED_ASSOCIATION_ALREADY_EXISTS.getDescription());
    }

    private void updateUserWithNewRoleSet(String username, UserStoreManager userStoreManager, List<String> rolesToAdd,
                                          Collection<String> deletingRoles) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("Deleting roles : " + Arrays.toString(deletingRoles.toArray(new String[0]))
                    + " and Adding roles : " + Arrays.toString(rolesToAdd.toArray(new String[0])));
        }
        userStoreManager.updateRoleListOfUser(username, deletingRoles.toArray(new String[0]),
                rolesToAdd.toArray(new String[0]));
        if (log.isDebugEnabled()) {
            log.debug("Federated user: " + username + " is updated by authentication framework with roles : "
                    + rolesToAdd);
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

    /**
     * This method is used to get null value claims passed from idp to be deleted from current user active claims.
     *
     * @param attributes User attributes.
     * @return toBeDeletedClaims
     */
    private List<String> prepareToBeDeletedClaimMappings(Map<String, String> attributes) {

        List<String> toBeDeletedUserClaims = new ArrayList<>();
        if (MapUtils.isNotEmpty(attributes)) {
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                String claimURI = entry.getKey();
                String claimValue = entry.getValue();
                if (StringUtils.isNotBlank(claimURI) && StringUtils.isBlank(claimValue)) {
                    toBeDeletedUserClaims.add(claimURI);
                }
            }
        }
        return toBeDeletedUserClaims;
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

    /**
     * Check for internal roles and convert internal role domain names to camel case to match with predefined
     * internal role domains.
     *
     * @param roles roles to verify and update
     * @return updated role list
     */
    private List<String> convertInternalRoleDomainsToCamelCase(List<String> roles) {

        List<String> updatedRoles = new ArrayList<>();

        if (roles != null) {
            // If internal roles exist, convert internal role domain names to case sensitive predefined domain names.
            for (String role : roles) {
                if (StringUtils.containsIgnoreCase(role, UserCoreConstants.INTERNAL_DOMAIN + CarbonConstants
                        .DOMAIN_SEPARATOR)) {
                    updatedRoles.add(UserCoreConstants.INTERNAL_DOMAIN + CarbonConstants.DOMAIN_SEPARATOR +
                            UserCoreUtil.removeDomainFromName(role));
                } else if (StringUtils.containsIgnoreCase(role, APPLICATION_DOMAIN + CarbonConstants.DOMAIN_SEPARATOR)) {
                    updatedRoles.add(APPLICATION_DOMAIN + CarbonConstants.DOMAIN_SEPARATOR + UserCoreUtil
                            .removeDomainFromName(role));
                } else if (StringUtils.containsIgnoreCase(role, WORKFLOW_DOMAIN + CarbonConstants.DOMAIN_SEPARATOR)) {
                    updatedRoles.add(WORKFLOW_DOMAIN + CarbonConstants.DOMAIN_SEPARATOR + UserCoreUtil
                            .removeDomainFromName(role));
                } else {
                    updatedRoles.add(role);
                }
            }
        }

        return updatedRoles;
    }

    /**
     * Retrieve the list of roles to be deleted.
     *
     * @param realm            user realm
     * @param currentRolesList current role list of the user
     * @param rolesToAdd       roles that are about to be added
     * @return roles to be deleted
     * @throws UserStoreException When failed to get realm configuration
     */
    protected List<String> retrieveRolesToBeDeleted(UserRealm realm, List<String> currentRolesList,
                                                    List<String> rolesToAdd) throws UserStoreException {

        List<String> deletingRoles = new ArrayList<String>();
        deletingRoles.addAll(currentRolesList);

        // deletingRoles = currentRolesList - rolesToAdd
        deletingRoles.removeAll(rolesToAdd);

        // Exclude Internal/everyonerole from deleting role since its cannot be deleted
        deletingRoles.remove(realm.getRealmConfiguration().getEveryOneRoleName());

        return deletingRoles;
    }

}
