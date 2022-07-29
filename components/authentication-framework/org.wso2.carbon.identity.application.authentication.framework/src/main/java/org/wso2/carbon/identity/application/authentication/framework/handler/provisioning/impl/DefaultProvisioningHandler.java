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

import org.apache.axiom.om.OMElement;
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
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.user.profile.mgt.association.federation.FederatedAssociationManager;
import org.wso2.carbon.identity.user.profile.mgt.association.federation.constant.FederatedAssociationConstants;
import org.wso2.carbon.identity.user.profile.mgt.association.federation.exception.FederatedAssociationManagerException;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.claim.Claim;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.Config.SEND_MANUALLY_ADDED_LOCAL_ROLES_OF_IDP;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.Config.SEND_ONLY_LOCALLY_MAPPED_ROLES_OF_IDP;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.InternalRoleDomains.APPLICATION_DOMAIN;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.InternalRoleDomains.WORKFLOW_DOMAIN;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.PROVISIONED_SOURCE_ID_CLAIM;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.USERNAME_CLAIM;

/**
 * Default provisioning handler.
 */
public class DefaultProvisioningHandler implements ProvisioningHandler {

    private static final Log log = LogFactory.getLog(DefaultProvisioningHandler.class);
    private static final String USER_WORKFLOW_ENGAGED_ERROR_CODE = "WFM-10001";
    private static volatile DefaultProvisioningHandler instance;
    private static final String LOCAL_DEFAULT_CLAIM_DIALECT = "http://wso2.org/claims";

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

        List<String> idpToLocalRoleMapping =
                (List<String>) IdentityUtil.threadLocalProperties.get()
                        .get(FrameworkConstants.IDP_TO_LOCAL_ROLE_MAPPING);
        handle(roles, subject, attributes, provisioningUserStoreId, tenantDomain, idpToLocalRoleMapping);

    }

    @Override
    public void handle(List<String> roles, String subject, Map<String, String> attributes,
            String provisioningUserStoreId, String tenantDomain, List<String> idpToLocalRoleMapping)
            throws FrameworkException {

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
                /*
                Set PROVISIONED_USER thread local property to true, to identify already provisioned
                user claim update scenario.
                 */
                IdentityUtil.threadLocalProperties.get().put(FrameworkConstants.JIT_PROVISIONING_FLOW, true);
                if (!userClaims.isEmpty()) {
                    /*
                    In the syncing process of existing claim mappings with IDP claim mappings for JIT provisioned user,
                    To delete corresponding existing claim mapping, if any IDP claim mapping is absence.
                     */
                    List<String> toBeDeletedUserClaims = prepareToBeDeletedClaimMappings(attributes);
                    Claim[] existingUserClaimList = userStoreManager.getUserClaimValues(
                            UserCoreUtil.removeDomainFromName(username), UserCoreConstants.DEFAULT_PROFILE);
                    if (existingUserClaimList != null) {
                        List<Claim> toBeDeletedFromExistingUserClaims = new ArrayList<>(
                                Arrays.asList(existingUserClaimList));

                        // Claim mappings which do not come with the IDP claim mapping set but must not delete.
                        Set<String> indelibleClaimSet = getIndelibleClaims();
                        toBeDeletedFromExistingUserClaims.removeIf(claim -> claim.getClaimUri().contains("/identity/")
                                || indelibleClaimSet.contains(claim.getClaimUri()) ||
                                userClaims.containsKey(claim.getClaimUri()));

                        for (Claim claim : toBeDeletedFromExistingUserClaims) {
                            toBeDeletedUserClaims.add(claim.getClaimUri());
                        }
                    }

                    userClaims.remove(FrameworkConstants.PASSWORD);
                    userClaims.remove(USERNAME_CLAIM);
                    userStoreManager.setUserClaimValues(UserCoreUtil.removeDomainFromName(username), userClaims, null);
                    /*
                    Since the user is exist following code is get all active claims of user and crosschecking against
                    tobeDeleted claims (claims came from federated idp as null). If there is a match those claims
                    will be deleted.
                    */
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
                String password = generatePassword();
                String passwordFromUser = userClaims.get(FrameworkConstants.PASSWORD);
                if (StringUtils.isNotEmpty(passwordFromUser)) {
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
                    /*
                    This thread local is set to skip the username and password pattern validation even if the password
                    is generated, or user entered one. If it is required to check password pattern validation,
                    need to write a provisioning handler extending the "DefaultProvisioningHandler".
                     */
                    UserCoreUtil.setSkipPasswordPatternValidationThreadLocal(true);
                    UserCoreUtil.setSkipUsernamePatternValidationThreadLocal(true);
                    if (FrameworkUtils.isJITProvisionEnhancedFeatureEnabled()) {
                        setJitProvisionedSource(tenantDomain, idp, userClaims);
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
                    UserCoreUtil.removeSkipPasswordPatternValidationThreadLocal();
                    UserCoreUtil.removeSkipUsernamePatternValidationThreadLocal();
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

            boolean includeManuallyAddedLocalRoles = Boolean
                    .parseBoolean(IdentityUtil.getProperty(SEND_MANUALLY_ADDED_LOCAL_ROLES_OF_IDP));

            List<String> currentRolesList = Arrays.asList(userStoreManager.getRoleListOfUser(username));
            Collection<String> deletingRoles = retrieveRolesToBeDeleted(realm, currentRolesList, rolesToAdd);

            // Updating user roles.
            if (roles != null && roles.size() > 0) {

                if (idpToLocalRoleMapping != null && !idpToLocalRoleMapping.isEmpty()) {
                    boolean excludeUnmappedRoles = false;

                    if (StringUtils.isNotEmpty(IdentityUtil.getProperty(SEND_ONLY_LOCALLY_MAPPED_ROLES_OF_IDP))) {
                        excludeUnmappedRoles = Boolean
                                .parseBoolean(IdentityUtil.getProperty(SEND_ONLY_LOCALLY_MAPPED_ROLES_OF_IDP));
                    }

                    if (excludeUnmappedRoles && includeManuallyAddedLocalRoles) {
                        /*
                            Get the intersection of deletingRoles with idpRoleMappings. Here we're deleting mapped
                            roles and keeping manually added local roles.
                        */
                        deletingRoles = deletingRoles.stream().distinct().filter(idpToLocalRoleMapping::contains)
                                .collect(Collectors.toSet());
                    }
                }

                // No need to add already existing roles again.
                rolesToAdd.removeAll(currentRolesList);

                // Cannot add roles that doesn't exists in the system.
                List<String> nonExistingUnmappedIdpRoles = new ArrayList<>();
                for (String role : rolesToAdd) {
                    if (!userStoreManager.isExistingRole(role)) {
                        nonExistingUnmappedIdpRoles.add(role);
                    }
                }
                rolesToAdd.removeAll(nonExistingUnmappedIdpRoles);

                // TODO : Does it need to check this?
                // Check for case whether super admin login
                handleFederatedUserNameEqualsToSuperAdminUserName(realm, username, userStoreManager, deletingRoles);

                updateUserWithNewRoleSet(username, userStoreManager, rolesToAdd, deletingRoles);
            } else {
                if (includeManuallyAddedLocalRoles) {
                    // Remove only IDP mapped roles and keep manually added local roles.
                    if (CollectionUtils.isNotEmpty(idpToLocalRoleMapping)) {
                        deletingRoles = deletingRoles.stream().distinct().filter(idpToLocalRoleMapping::contains)
                                .collect(Collectors.toSet());
                        updateUserWithNewRoleSet(username, userStoreManager, new ArrayList<>(), deletingRoles);
                    }
                } else {
                    // Remove all roles of the user.
                    updateUserWithNewRoleSet(username, userStoreManager, new ArrayList<>(), deletingRoles);
                }
            }

            PermissionUpdateUtil.updatePermissionTree(tenantId);

        } catch (org.wso2.carbon.user.api.UserStoreException | CarbonException |
                FederatedAssociationManagerException e) {
            throw new FrameworkException("Error while provisioning user : " + subject, e);
        } finally {
            IdentityUtil.clearIdentityErrorMsg();
            IdentityUtil.threadLocalProperties.get().remove(FrameworkConstants.JIT_PROVISIONING_FLOW);
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
                        " in tenant: " + tenantDomain + " to the federated subject: " + subject + " in IdP: " + idp, e);
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
                /*
                 For claimValues not mapped to local claim dialect uris, need to skip to prevent user provision failure.
                 Password is a different case where we have to keep for password provisioning.
                 */
                if (!(StringUtils.isEmpty(claimURI) || StringUtils.isEmpty(claimValue)) &&
                        (claimURI.equals(FrameworkConstants.PASSWORD) ||
                                claimURI.contains(LOCAL_DEFAULT_CLAIM_DIALECT))) {
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
                } else if (StringUtils.containsIgnoreCase(role, APPLICATION_DOMAIN
                        + CarbonConstants.DOMAIN_SEPARATOR)) {
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

        // Exclude Internal/everyone role from deleting roles, since it cannot be deleted.
        deletingRoles.remove(realm.getRealmConfiguration().getEveryOneRoleName());

        return deletingRoles;
    }

    /**
     * Set the identity provider's resource id as the source of the provisioned user.
     *
     * @param tenantDomain Tenant domain.
     * @param idpName      Identity provider name.
     * @param userClaims   User claims.
     * @throws FrameworkException If an error occurs while retrieving the resource id of the identity provider.
     */
    private void setJitProvisionedSource(String tenantDomain, String idpName, Map<String, String> userClaims)
            throws FrameworkException {

        try {
            String idpId = IdentityProviderManager.getInstance().getIdPByName(idpName, tenantDomain,
                    true).getResourceId();
            userClaims.put(PROVISIONED_SOURCE_ID_CLAIM, idpId);
        } catch (IdentityProviderManagementException e) {
            throw new FrameworkException("Error while getting the federated IDP name of the IDP: "
                    + idpName + "in the tenant: " + tenantDomain, e);
        }
    }

    /**
     * Claims which must not delete during existing claim mapping syncing process with idp claims.
     *
     * @return Claims not to delete.
     */
    private Set<String> getIndelibleClaims() {

        OMElement jitProvisioningConfig = IdentityConfigParser.getInstance().
                getConfigElement(FrameworkConstants.Config.JIT_PROVISIONING_CONFIG);
        if (jitProvisioningConfig == null) {
            if (log.isDebugEnabled()) {
                log.debug(FrameworkConstants.Config.JIT_PROVISIONING_CONFIG + " config not found.");
            }
            return Collections.emptySet();
        }

        Iterator indelibleClaimsConfig = jitProvisioningConfig.getChildrenWithLocalName
                (FrameworkConstants.Config.INCREDIBLE_CLAIMS_CONFIG_ELEMENT);
        if (indelibleClaimsConfig == null) {
            if (log.isDebugEnabled()) {
                log.debug(FrameworkConstants.Config.INCREDIBLE_CLAIMS_CONFIG_ELEMENT + " config not found.");
            }
            return Collections.emptySet();
        }

        Set<String> indelibleClaims = new HashSet<>();
        while (indelibleClaimsConfig.hasNext()) {
            OMElement claimURIIdentifierIterator = (OMElement) indelibleClaimsConfig.next();
            Iterator claimURIIdentifieConfig = claimURIIdentifierIterator
                    .getChildrenWithLocalName(FrameworkConstants.Config.CLAIM_URI_CONFIG_ELEMENT);
            if (claimURIIdentifieConfig == null) {
                if (log.isDebugEnabled()) {
                    log.debug(FrameworkConstants.Config.CLAIM_URI_CONFIG_ELEMENT + " config not found.");
                }
                return Collections.emptySet();
            }

            while (claimURIIdentifieConfig.hasNext()) {
                OMElement claimURIIdentifierConfig = (OMElement) claimURIIdentifieConfig.next();
                String claimURI = claimURIIdentifierConfig.getText();
                if (StringUtils.isNotBlank(claimURI)) {
                    indelibleClaims.add(claimURI.trim());
                }
            }
        }
        return indelibleClaims;
    }
}
