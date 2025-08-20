/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.user.profile.mgt;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.base.IdentityConstants;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.user.profile.mgt.association.federation.FederatedAssociationManager;
import org.wso2.carbon.identity.user.profile.mgt.association.federation.exception.FederatedAssociationManagerException;
import org.wso2.carbon.identity.user.profile.mgt.association.federation.model.FederatedAssociation;
import org.wso2.carbon.identity.user.profile.mgt.internal.IdentityUserProfileServiceDataHolder;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdpManager;
import org.wso2.carbon.user.api.Claim;
import org.wso2.carbon.user.api.ClaimMapping;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.profile.ProfileConfiguration;
import org.wso2.carbon.user.core.profile.ProfileConfigurationManager;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.wso2.carbon.identity.user.profile.mgt.association.federation.constant.FederatedAssociationConstants.ErrorMessages.INVALID_FEDERATED_ASSOCIATION;

public class UserProfileAdmin extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(UserProfileAdmin.class);
    private static final Log audit_log = CarbonConstants.AUDIT_LOG;
    private static final String AUDIT_SUCCESS = "Success";
    private static final String AUDIT_FAIL = "Fail";
    private static String AUDIT_MESSAGE = "Initiator : %s | Action : %s | Target : %s | Data : { %s } | Result : %s ";

    private static String auditActionForCreateAssociation = "Associate local user account with federated account";
    private static String auditActionForDeleteAssociation = "Remove local user account association with federated " +
            "account";

    private static UserProfileAdmin userProfileAdmin = new UserProfileAdmin();
    private String authorizationFailureMessage = "You are not authorized to perform this action.";

    private static final String USER_PROFILE_DELETE_PERMISSION = "/manage/identity/userprofile/delete";
    private static final String USER_PROFILE_VIEW_PERMISSION = "/manage/identity/userprofile/view";
    private static final String USER_PROFILE_MANAGE_PERMISSION = "/manage/identity/userprofile";
    private static final String TRANSPORT_HTTP_SERVLET_REQUEST = "transport.http.servletRequest";
    private static final String LOGGED_IN_DOMAIN = "logged_in_domain";

    public static UserProfileAdmin getInstance() {
        return userProfileAdmin;
    }

    public boolean isReadOnlyUserStore() throws UserProfileException {
        try {
            UserRealm realm = getUserRealm();
            if ("true".equals(realm.getRealmConfiguration().getUserStoreProperty(
                    UserCoreConstants.RealmConfig.PROPERTY_READ_ONLY))) {
                return true;
            }
            return false;
        } catch (UserStoreException e) {
            log.error(e.getMessage(), e);
            throw new UserProfileException(e.getMessage(), e);
        }
    }

    public void setUserProfile(String username, UserProfileDTO profile) throws UserProfileException {
        UserRealm realm = null;
        try {

            if (!this.isAuthorized(username, USER_PROFILE_MANAGE_PERMISSION)) {
                throw new UserProfileException(authorizationFailureMessage);
            }

            // Check whether we are trying to change the admin user profile. Only admin user can change his profile.
            // Any other attempt is unauthorized. So attempts will be logged and denied.
            if (isAdminProfileSpoof(username)) {
                log.warn("Unauthorized attempt. User " + CarbonContext.getThreadLocalCarbonContext().getUsername() +
                        " is trying to modify the profile of the admin user.");
                throw new UserProfileException(authorizationFailureMessage);
            }

            int indexOne;
            indexOne = username.indexOf("/");

            if (indexOne < 0) {
                /*if domain is not provided, this can be the scenario where user from a secondary user store
                logs in without domain name and tries to view his own profile*/
                MessageContext messageContext = MessageContext.getCurrentMessageContext();
                HttpServletRequest request = (HttpServletRequest) messageContext
                        .getProperty("transport.http.servletRequest");
                String domainName = (String) request.getSession().getAttribute("logged_in_domain");

                if (domainName != null) {
                    username = domainName + "/" + username;
                }
            }

            realm = getUserRealm();

            UserFieldDTO[] udatas = profile.getFieldValues();
            Map<String, String> map = new HashMap<String, String>();
            for (UserFieldDTO data : udatas) {
                String claimURI = data.getClaimUri();
                String value = data.getFieldValue();
                if (!data.isReadOnly()) {
                    // Quick fix for not to remove OTP checkbox when false
                    if (value == "" && "http://wso2.org/claims/identity/otp".equals(claimURI)) {
                        value = "false";
                    }
                    map.put(claimURI, value);
                }
            }

            if (profile.getProfileConifuration() != null) {
                map.put(UserCoreConstants.PROFILE_CONFIGURATION, profile.getProfileConifuration());
            } else {
                map.put(UserCoreConstants.PROFILE_CONFIGURATION,
                        UserCoreConstants.DEFAULT_PROFILE_CONFIGURATION);
            }

            UserStoreManager admin = realm.getUserStoreManager();

            // User store manager expects tenant aware username
            admin.setUserClaimValues(username, map, profile.getProfileName());

        } catch (UserStoreException e) {
            // Not logging. Already logged.
            throw new UserProfileException(e.getMessage(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new UserProfileException(e.getMessage(), e);
        }
    }

    public void deleteUserProfile(String username, String profileName) throws UserProfileException {
        UserRealm realm = null;
        try {
            if (!this.isAuthorized(username, USER_PROFILE_DELETE_PERMISSION)) {
                throw new UserProfileException(authorizationFailureMessage);
            }

            // Check whether we are trying to delete the admin user profile. Only admin user can delete his profile.
            // Any other attempt is unauthorized. So attempts will be logged and denied.
            if (isAdminProfileSpoof(username)) {
                log.warn("Unauthorized attempt. User " + CarbonContext.getThreadLocalCarbonContext().getUsername() +
                        " is trying to delete the profile of the admin user.");
                throw new UserProfileException(authorizationFailureMessage);
            }

            if (UserCoreConstants.DEFAULT_PROFILE.equals(profileName)) {
                throw new UserProfileException("Cannot delete default profile");
            }
            realm = getUserRealm();

            ClaimManager claimManager = realm.getClaimManager();
            String[] claims = claimManager.getAllClaimUris();

            UserStoreManager admin = realm.getUserStoreManager();
            admin.deleteUserClaimValues(username, claims, profileName);
            admin.deleteUserClaimValue(username, UserCoreConstants.PROFILE_CONFIGURATION,
                    profileName);
        } catch (UserStoreException e) {
            // Not logging. Already logged.
            throw new UserProfileException(e.getMessage(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new UserProfileException(e.getMessage(), e);
        }
    }

    public UserProfileDTO[] getUserProfiles(String username)
            throws UserProfileException {
        UserProfileDTO[] profiles;
        String[] availableProfileConfigurations = new String[0];
        String profileConfig = null;
        try {
            if (!this.isAuthorized(username, USER_PROFILE_VIEW_PERMISSION)) {
                throw new UserProfileException(authorizationFailureMessage);
            }

            // Check whether we are trying to view the admin user profile. Only admin user can view his profile.
            // Any other attempt is unauthorized. So attempts will be logged and denied.
            if (isAdminProfileSpoof(username)) {
                log.warn("Unauthorized attempt. User " + CarbonContext.getThreadLocalCarbonContext().getUsername() +
                        " is trying to view the profile of the admin user.");
                throw new UserProfileException(authorizationFailureMessage);
            }

            UserRealm realm = getUserRealm();

            UserStoreManager userStoreManager = realm.getUserStoreManager();

            boolean isReadOnly = userStoreManager.isReadOnly();

            int index;
            index = username.indexOf("/");

            UserStoreManager secUserStoreManager = null;

            // Check whether we have a secondary UserStoreManager setup.
            if (index > 0) {
                // Using the short-circuit. User name comes with the domain name.
                String domain = username.substring(0, index);

                if (userStoreManager instanceof AbstractUserStoreManager) {
                    secUserStoreManager = ((AbstractUserStoreManager) userStoreManager)
                            .getSecondaryUserStoreManager(domain);
                    if (secUserStoreManager != null) {
                        isReadOnly = secUserStoreManager.isReadOnly();
                    }
                }
            }

            ProfileConfigurationManager profileAdmin = realm
                    .getProfileConfigurationManager();
            if (profileAdmin != null) {
                availableProfileConfigurations = getAvailableProfileConfiguration(profileAdmin);
            }

            String[] profileNames = null;

            if (secUserStoreManager != null) {
                profileNames = secUserStoreManager.getProfileNames(username);
            } else {
                profileNames = userStoreManager.getProfileNames(username);
            }

            profiles = new UserProfileDTO[profileNames.length];
            Claim[] claims = getAllSupportedClaims(realm, UserCoreConstants.DEFAULT_CARBON_DIALECT);
            String[] claimUris = new String[claims.length + 1];
            for (int i = 0; i < claims.length; i++) {
                claimUris[i] = claims[i].getClaimUri();
            }

            claimUris[claims.length] = UserCoreConstants.PROFILE_CONFIGURATION;

            for (int i = 0; i < profileNames.length; i++) {
                String profile = profileNames[i];
                Map<String, String> valueMap =
                        userStoreManager.getUserClaimValues(username, claimUris, profile);
                List<UserFieldDTO> userFields = new ArrayList<UserFieldDTO>();
                for (int j = 0; j < claims.length; j++) {
                    UserFieldDTO data = new UserFieldDTO();
                    Claim claim = claims[j];
                    String claimUri = claim.getClaimUri();
                    if (!UserCoreConstants.PROFILE_CONFIGURATION.equals(claimUri)) {
                        data.setClaimUri(claimUri);
                        data.setFieldValue(valueMap.get(claimUri));
                        data.setDisplayName(claim.getDisplayTag());
                        data.setRegEx(claim.getRegEx());
                        data.setRequired(claim.isRequired());
                        data.setDisplayOrder(claim.getDisplayOrder());
                        data.setCheckedAttribute(claim.isCheckedAttribute());
                        data.setReadOnly(claim.isReadOnly());
                        userFields.add(data);
                    }
                }

                UserProfileDTO temp = new UserProfileDTO();
                temp.setProfileName(profile);
                temp.setFieldValues(userFields.toArray(new UserFieldDTO[userFields.size()]));
                temp.setProfileConfigurations(availableProfileConfigurations);

                profileConfig = valueMap.get(UserCoreConstants.PROFILE_CONFIGURATION);
                if (profileConfig == null) {
                    profileConfig = UserCoreConstants.DEFAULT_PROFILE_CONFIGURATION;
                }

                if (isReadOnly) {
                    profileConfig = "readonly";
                }

                temp.setProfileConifuration(profileConfig);
                profiles[i] = temp;
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            // Not logging. Already logged.
            throw new UserProfileException(e.getMessage(), e);
        }
        return profiles;

    }

    public UserProfileDTO getProfileFieldsForInternalStore() throws UserProfileException {
        UserFieldDTO[] datas;
        UserProfileDTO profile = new UserProfileDTO();
        String[] availableProfileConfigurations = new String[0];
        try {
            UserRealm realm = getUserRealm();
            Claim[] claims = getClaimsToEnterData(realm);

            ProfileConfigurationManager profileAdmin = realm
                    .getProfileConfigurationManager();
            if (profileAdmin != null) {
                availableProfileConfigurations = getAvailableProfileConfiguration(profileAdmin);
            }

            String[] claimUris = new String[claims.length];
            for (int i = 0; i < claims.length; i++) {
                claimUris[i] = claims[i].getClaimUri();
            }
            datas = new UserFieldDTO[claims.length];
            for (int j = 0; j < claims.length; j++) {
                UserFieldDTO data = new UserFieldDTO();
                Claim claim = claims[j];
                String claimUri = claim.getClaimUri();
                data.setClaimUri(claimUri);
                data.setDisplayName(claim.getDisplayTag());
                data.setRegEx(claim.getRegEx());
                data.setRequired(claim.isRequired());
                data.setDisplayOrder(claim.getDisplayOrder());
                data.setRegEx(claim.getRegEx());
                data.setCheckedAttribute(claim.isCheckedAttribute());
                data.setReadOnly(claim.isReadOnly());
                datas[j] = data;
            }

        } catch (Exception e) {
            // Not logging. Already logged.
            throw new UserProfileException(e.getMessage(), e);
        }

        profile.setFieldValues(datas);
        profile.setProfileConfigurations(availableProfileConfigurations);

        return profile;
    }

    public UserProfileDTO getUserProfile(String username, String profileName)
            throws UserProfileException {
        UserProfileDTO profile = new UserProfileDTO();
        String[] availableProfileConfigurations = new String[0];
        String profileConfig = null;

        try {

            if (username == null || profileName == null) {
                throw new UserProfileException("Invalid input parameters");
            }

            if (!this.isAuthorized(username, USER_PROFILE_VIEW_PERMISSION)) {
                throw new UserProfileException(authorizationFailureMessage);
            }

            // Check whether we are trying to view the admin user profile. Only admin user can view his profile.
            // Any other attempt is unauthorized. So attempts will be logged and denied.
            if (isAdminProfileSpoof(username)) {
                log.warn("Unauthorized attempt. User " + CarbonContext.getThreadLocalCarbonContext().getUsername() +
                        " is trying to view the profile of the admin user.");
                throw new UserProfileException(authorizationFailureMessage);
            }

            UserRealm realm = getUserRealm();

            UserStoreManager userStoreManager = realm.getUserStoreManager();

            boolean isReadOnly = userStoreManager.isReadOnly();

            int indexOne;
            indexOne = username.indexOf("/");

            if (indexOne < 0) {
                /*if domain is not provided, this can be the scenario where user from a secondary user store
                logs in without domain name and tries to view his own profile*/
                MessageContext messageContext = MessageContext.getCurrentMessageContext();
                HttpServletRequest request = (HttpServletRequest) messageContext
                        .getProperty("transport.http.servletRequest");
                String domainName = (String) request.getSession().getAttribute("logged_in_domain");

                if (domainName != null) {
                    username = domainName + "/" + username;
                }
            }
            int index;
            index = username.indexOf("/");

            UserStoreManager secUserStoreManager = null;

            // Check whether we have a secondary UserStoreManager setup.
            if (index > 0) {
                // Using the short-circuit. User name comes with the domain name.
                String domain = username.substring(0, index);

                if (userStoreManager instanceof AbstractUserStoreManager) {
                    secUserStoreManager = ((AbstractUserStoreManager) userStoreManager)
                            .getSecondaryUserStoreManager(domain);
                    if (secUserStoreManager != null) {
                        isReadOnly = secUserStoreManager.isReadOnly();
                    }
                }
            }

            ProfileConfigurationManager profileAdmin = realm
                    .getProfileConfigurationManager();

            String[] profileNames = null;

            if (secUserStoreManager != null) {
                profileNames = secUserStoreManager.getProfileNames(username);
            } else {
                profileNames = userStoreManager.getProfileNames(username);
            }

            boolean found = false;

            if (profileNames != null && profileNames.length > 0) {
                for (int i = 0; i < profileNames.length; i++) {
                    if (profileName.equals(profileNames[i])) {
                        found = true;
                        break;
                    }
                }
            }

            if (!found) {
                return null;
            }

            if (profileAdmin != null) {
                availableProfileConfigurations = getAvailableProfileConfiguration(profileAdmin);
            }

            Claim[] claims = getClaimsToEnterData(realm);
            String[] claimUris = new String[claims.length + 1];
            for (int i = 0; i < claims.length; i++) {
                claimUris[i] = claims[i].getClaimUri();
            }

            claimUris[claims.length] = UserCoreConstants.PROFILE_CONFIGURATION;

            Map<String, String> valueMap =
                    userStoreManager
                            .getUserClaimValues(username, claimUris, profileName);
            List<UserFieldDTO> userFields = new ArrayList<UserFieldDTO>();

            for (int j = 0; j < claims.length; j++) {
                UserFieldDTO data = new UserFieldDTO();
                Claim claim = claims[j];
                String claimUri = claim.getClaimUri();
                if (!UserCoreConstants.PROFILE_CONFIGURATION.equals(claimUri)) {
                    data.setClaimUri(claimUri);
                    data.setFieldValue(valueMap.get(claimUri));
                    data.setDisplayName(claim.getDisplayTag());
                    data.setRegEx(claim.getRegEx());
                    data.setRequired(claim.isRequired());
                    data.setDisplayOrder(claim.getDisplayOrder());
                    data.setReadOnly(claim.isReadOnly());
                    data.setCheckedAttribute(claim.isCheckedAttribute());
                    userFields.add(data);
                }
            }

            profile.setProfileName(profileName);
            profile.setProfileConfigurations(availableProfileConfigurations);

            profileConfig = valueMap.get(UserCoreConstants.PROFILE_CONFIGURATION);
            if (profileConfig == null) {
                profileConfig = UserCoreConstants.DEFAULT_PROFILE_CONFIGURATION;
            }

            if (isReadOnly) {
                profileConfig = "readonly";
            }

            profile.setProfileConifuration(profileConfig);
            profile.setFieldValues(userFields.toArray(new UserFieldDTO[userFields.size()]));

        } catch (Exception e) {

            log.error(String.format("An error occurred while getting the user profile '%s' of the user '%s'",
                    profileName, username), e);

            throw new UserProfileException(e.getMessage(), e);
        }
        return profile;
    }

    public boolean isAddProfileEnabled() throws UserProfileException {
        UserRealm realm = getUserRealm();
        UserStoreManager userStoreManager = null;
        try {
            userStoreManager = realm.getUserStoreManager();
        } catch (UserStoreException e) {
            String errorMessage = "Error in obtaining UserStoreManager.";
            log.error(errorMessage, e);
            throw new UserProfileException(errorMessage, e);
        }
        return userStoreManager.isMultipleProfilesAllowed();
    }


    public boolean isAddProfileEnabledForDomain(String domain) throws UserProfileException {

        org.wso2.carbon.user.core.UserStoreManager userStoreManager = null;
        org.wso2.carbon.user.core.UserRealm realm = getUserRealm();
        boolean isAddProfileEnabled = false;

        try {
            if (StringUtils.isBlank(domain) || StringUtils.equals(domain, UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME)) {
                userStoreManager = realm.getUserStoreManager();
            } else {
                userStoreManager = realm.getUserStoreManager().getSecondaryUserStoreManager(domain);
            }

        } catch (UserStoreException e) {
            String errorMessage = "Error in obtaining SecondaryUserStoreManager.";
            log.error(errorMessage, e);
            throw new UserProfileException(errorMessage, e);
        }

        if (userStoreManager != null) {
            isAddProfileEnabled = userStoreManager.isMultipleProfilesAllowed();
        }

        return isAddProfileEnabled;
    }


    private Claim[] getClaimsToEnterData(UserRealm realm)
            throws UserStoreException {
        try {
            return getAllSupportedClaims(realm, UserCoreConstants.DEFAULT_CARBON_DIALECT);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException(e);
        }
    }


    private boolean isAuthorized(String targetUser, String permissionString) throws UserStoreException,
            CarbonException {
        boolean isAuthrized = false;
        MessageContext msgContext = MessageContext.getCurrentMessageContext();
        HttpServletRequest request = (HttpServletRequest) msgContext
                .getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
        HttpSession httpSession = request.getSession(false);
        if (httpSession != null) {
            String userName = (String) httpSession.getAttribute(ServerConstants.USER_LOGGED_IN);
            isAuthrized = isUserAuthorizedToConfigureProfile(getUserRealm(), userName, targetUser, permissionString);
        }
        return isAuthrized;
    }

    private static boolean isUserAuthorizedToConfigureProfile(UserRealm realm, String currentUserName,
                                                              String targetUser, String permission)
            throws UserStoreException {
        boolean isAuthrized = false;
        if (currentUserName == null) {
            //do nothing
        } else if (currentUserName.equals(targetUser)) {
            isAuthrized = true;
        } else {
            AuthorizationManager authorizer = realm.getAuthorizationManager();
            isAuthrized = authorizer.isUserAuthorized(currentUserName,
                    CarbonConstants.UI_ADMIN_PERMISSION_COLLECTION + permission,
                    "ui.execute");
        }
        return isAuthrized;
    }

    /**
     * @return
     * @throws UserStoreException
     */
    private Claim[] getAllSupportedClaims(UserRealm realm, String dialectUri)
            throws org.wso2.carbon.user.api.UserStoreException {
        ClaimMapping[] claims = null;
        List<Claim> reqClaims = null;

        claims = realm.getClaimManager().getAllSupportClaimMappingsByDefault();
        reqClaims = new ArrayList<Claim>();
        for (int i = 0; i < claims.length; i++) {
            if (dialectUri.equals(claims[i].getClaim().getDialectURI()) && (claims[i] != null && claims[i].getClaim().getDisplayTag() != null
                    && !claims[i].getClaim().getClaimUri().equals(IdentityConstants.CLAIM_PPID))) {

                reqClaims.add((Claim) claims[i].getClaim());
            }
        }

        return reqClaims.toArray(new Claim[reqClaims.size()]);
    }

    private String[] getAvailableProfileConfiguration(
            ProfileConfigurationManager profileAdmin) throws UserStoreException {
        ProfileConfiguration[] configurations;
        String[] profileNames = new String[0];
        try {
            configurations = (ProfileConfiguration[]) profileAdmin.getAllProfiles();
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException(e);
        }

        if (configurations != null) {
            profileNames = new String[configurations.length];
            for (int i = 0; i < configurations.length; i++) {
                profileNames[i] = configurations[i].getProfileName();
            }
        }

        return profileNames;
    }

    /**
     * Associate the user logged in with the given federated identifier.
     *
     * @param idpID        Identity Provider ID
     * @param associatedID Federated Identity ID
     * @throws UserProfileException
     */
    public void associateID(String idpID, String associatedID) throws UserProfileException {

        String tenantAwareUsername = CarbonContext.getThreadLocalCarbonContext().getUsername();
        User user = getUser(tenantAwareUsername);
        try {
            getFederatedAssociationManager().createFederatedAssociation(user, idpID, associatedID);
        } catch (FederatedAssociationManagerException e) {
            String msg = "Error while creating association for user: " + tenantAwareUsername + ", with federated IdP: "
                    + idpID + ", in tenant: " + getTenantDomain();
            throw new UserProfileException(msg, e);
        }
    }

    /**
     * Return the username of the local user associated with the given federated identifier.
     *
     * @param idpID        Identity Provider ID
     * @param associatedID Federated Identity ID
     * @return the username of the user associated with
     * @throws UserProfileException
     */
    public String getNameAssociatedWith(String idpID, String associatedID) throws UserProfileException {
        
        try {
            return getFederatedAssociationManager().getUserForFederatedAssociation(
                    CarbonContext.getThreadLocalCarbonContext().getTenantDomain(), idpID, associatedID);
        } catch (FederatedAssociationManagerException e) {
            String msg = "Error while retrieving user associated for federated IdP: " + idpID + " with federated " +
                    "identifier:" + associatedID + " in tenant: " + getTenantDomain();
            throw new UserProfileException(msg, e);
        }
    }

    /**
     * Return an array of federated identifiers associated with the logged in user.
     *
     * @return an array of AssociatedAccountDTO objects which contains the federated identifier info
     * @throws UserProfileException
     */
    public AssociatedAccountDTO[] getAssociatedIDs() throws UserProfileException {

        String tenantAwareUsername = CarbonContext.getThreadLocalCarbonContext().getUsername();
        User user = getUser(tenantAwareUsername);
        try {
            return getAssociatedAccounts(user);
        } catch (FederatedAssociationManagerException e) {
            String msg = "Error while retrieving federated identifiers associated for user: " + tenantAwareUsername +
                    " in tenant: " + getTenantDomain();
            throw new UserProfileException(msg, e);
        }
    }

    /**
     * Remove the association with the given federated identifier for the logged in user.
     *
     * @param idpID        Identity Provider ID
     * @param associatedID Federated Identity ID
     * @throws UserProfileException
     */
    public void removeAssociateID(String idpID, String associatedID) throws UserProfileException {

        String tenantAwareUsername = CarbonContext.getThreadLocalCarbonContext().getUsername();
        User user = getUser(tenantAwareUsername);
        try {
            getFederatedAssociationManager().deleteFederatedAssociation(user, idpID, associatedID);
        } catch (FederatedAssociationManagerException e) {
            String msg = "Error while removing association with federated IdP: " + idpID + " for user: " +
                    tenantAwareUsername + " in tenant: " + getTenantDomain();
            throw new UserProfileException(msg, e);
        }
    }

    /**
     * Associate the given user with the given federated identifier.
     *
     * @param username     username of the user to be associated with
     * @param idpID        Identity Provider ID
     * @param associatedID Federated Identity ID
     * @throws UserProfileException
     */
    public void associateIDForUser(String username, String idpID, String associatedID) throws UserProfileException {

        User user = getUser(username);
        String auditData = getAuditData(username, idpID, associatedID);
        try {
            getFederatedAssociationManager().createFederatedAssociation(user, idpID, associatedID);
            audit(auditActionForCreateAssociation, username, auditData, AUDIT_SUCCESS);
        } catch (FederatedAssociationManagerException e) {
            audit(auditActionForCreateAssociation, username, auditData, AUDIT_FAIL);
            String msg = "Error while creating association for user: " + username + " with federated IdP: " + idpID +
                    " in tenant: " + getTenantDomain();
            throw new UserProfileException(msg, e);
        }
    }

    /**
     * Remove the association with the given federated identifier for the given user.
     *
     * @param username     username of the user to be associated with
     * @param idpID        Identity Provider ID
     * @param associatedID Federated Identity ID
     * @throws UserProfileException
     */
    public void removeAssociateIDForUser(String username, String idpID, String associatedID) throws
            UserProfileException {

        String auditData = getAuditData(username, idpID, associatedID);
        validateFederatedAssociationParameters(username, idpID, associatedID, auditData);
        User user = getUser(username);
        try {
            getFederatedAssociationManager().deleteFederatedAssociation(user, idpID, associatedID);
            audit(auditActionForDeleteAssociation, username, auditData, AUDIT_SUCCESS);
        } catch (FederatedAssociationManagerException e) {
            // This error could be caused if federated association trying to delete does not exists for the user. In
            // order to preserve backward compatibility, this error is silently ignored.
            if (!isFederatedAssociationDoesNotExistsError(e)) {
                audit(auditActionForDeleteAssociation, username, auditData, AUDIT_FAIL);
                String msg = "Error while removing association with federated IdP: " + idpID + " for user: " + username +
                        " in tenant: " + getTenantDomain();
                throw new UserProfileException(msg, e);
            }
        }
    }

    /**
     * Return an array of federated identifiers associated with the given in user.
     *
     * @param username username of the user to find associations with
     * @return an array of AssociatedAccountDTO objects which contains the federated identifier info
     * @throws UserProfileException
     */
    public AssociatedAccountDTO[] getAssociatedIDsForUser(String username) throws UserProfileException {

        User user = getUser(username);
        try {
            return getAssociatedAccounts(user);
        } catch (FederatedAssociationManagerException e) {
            String msg = "Error while retrieving federated identifiers associated for user: " + username + " in " +
                    "tenant: " + getTenantDomain();
            throw new UserProfileException(msg, e);
        }
    }

    /**
     * Retrieve a claim of the authorized user.
     *
     * @param claimUri    Claim URI in wso2 dialect.
     * @param profileName User profile name.
     * @return Claim value.
     * @throws UserProfileException
     */
    public String getUserClaim(String claimUri, String profileName) throws UserProfileException {

        if (StringUtils.isBlank(claimUri)) {
            throw new UserProfileException("Invalid input parameter. Claim URI cannot be null.");
        }
        if (StringUtils.isBlank(profileName)) {
            throw new UserProfileException("Invalid input parameter. Profile name cannot be null.");
        }
        String loggedInUsername = CarbonContext.getThreadLocalCarbonContext().getUsername();
        if (StringUtils.isBlank(loggedInUsername)) {
            throw new UserProfileException("Could not find a logged in user in the current carbon context.");
        }

        String claimValue = null;
        try {
            UserStoreManager userStoreManager = getUserRealm().getUserStoreManager();
            int index = loggedInUsername.indexOf(UserCoreConstants.DOMAIN_SEPARATOR);

            if (index < 0) {
                if (log.isDebugEnabled()) {
                    log.debug("Logged in username : '" + loggedInUsername + "' does not contain domain name.");
                }
                /* if domain is not provided, this can be the scenario where user from a secondary user store
                logs in without domain name and tries to view his own profile. */
                MessageContext messageContext = MessageContext.getCurrentMessageContext();
                HttpServletRequest request = (HttpServletRequest) messageContext
                        .getProperty(TRANSPORT_HTTP_SERVLET_REQUEST);
                String domainName = (String) request.getSession().getAttribute(LOGGED_IN_DOMAIN);
                if (StringUtils.isNotBlank(domainName)) {
                    loggedInUsername = domainName + UserCoreConstants.DOMAIN_SEPARATOR + loggedInUsername;
                }
            }
            index = loggedInUsername.indexOf(UserCoreConstants.DOMAIN_SEPARATOR);
            UserStoreManager secUserStoreManager = null;

            // Check whether we have a secondary UserStoreManager setup.
            if (index > 0) {
                // Using the short-circuit. User name comes with the domain name.
                String domain = loggedInUsername.substring(0, index);
                if (log.isDebugEnabled()) {
                    log.debug("Domain name found in the logged in username. Domain name: " + domain);
                }
                if (userStoreManager instanceof AbstractUserStoreManager) {
                    secUserStoreManager = ((AbstractUserStoreManager) userStoreManager)
                            .getSecondaryUserStoreManager(domain);
                }
            }
            Map<String, String> claimValues;
            if (secUserStoreManager != null) {
                claimValues = secUserStoreManager.getUserClaimValues(loggedInUsername, new String[]{claimUri},
                        profileName);
            } else {
                claimValues = userStoreManager.getUserClaimValues(loggedInUsername, new String[]{claimUri},
                        profileName);
            }
            if (claimValues != null) {
                claimValue = claimValues.get(claimUri);
            }
        } catch (UserStoreException e) {
            String message = String.format("An error occurred while getting the user claim '%s' in '%s' profile of " +
                    "the user '%s'", claimUri, profileName, loggedInUsername);
            log.error(message, e);
            throw new UserProfileException(message, e);
        }
        return claimValue;
    }

    private AssociatedAccountDTO[] getAssociatedAccounts(User user)
            throws FederatedAssociationManagerException, UserProfileException {

        FederatedAssociation[] federatedAssociations = getFederatedAssociationManager()
                .getFederatedAssociationsOfUser(user);
        List<AssociatedAccountDTO> associatedAccountDTOS = new ArrayList<>();
        for (FederatedAssociation federatedAssociation : federatedAssociations) {
            String identityProviderName = getIdentityProviderName(getTenantDomain(),
                    federatedAssociation.getIdp().getId());
            associatedAccountDTOS.add(new AssociatedAccountDTO(
                    federatedAssociation.getId(),
                    identityProviderName,
                    federatedAssociation.getFederatedUserId()
            ));
        }
        return associatedAccountDTOS.toArray(new AssociatedAccountDTO[0]);
    }

    /**
     * Checks whether the given user name is admin user name and the currently logged in user also admin.
     * Only admin user is allowed for admin user profile related operations.
     *
     * @param username Username to be checked.
     * @return True only if admin user.
     * @throws UserStoreException Error occurred while retrieving realm configuration.
     */
    private boolean isAdminProfileSpoof(String username) throws UserStoreException {

        if (StringUtils.isEmpty(username)) {
            return false;
        }

        RealmConfiguration realmConfiguration = getUserRealm().getRealmConfiguration();
        String adminUsername = IdentityUtil.addDomainToName(realmConfiguration.getAdminUserName(),
                IdentityUtil.getPrimaryDomainName());
        String targetUsername = IdentityUtil.addDomainToName(username, IdentityUtil.getPrimaryDomainName());

        // If the given user name is not the admin username, simply we can allow and return false. Our intention is to
        // check whether a non admin user is trying to do operations on an admin profile.
        if (!StringUtils.equalsIgnoreCase(targetUsername, adminUsername)) {
            return false;
        }

        String loggedInUsername = CarbonContext.getThreadLocalCarbonContext().getUsername();
        if (loggedInUsername != null) {
            loggedInUsername = IdentityUtil.addDomainToName(loggedInUsername, IdentityUtil.getPrimaryDomainName());
        }

        // If the currently logged in user is also the admin user this isn't a spoof attempt. Hence returning false.
        return !StringUtils.equalsIgnoreCase(loggedInUsername, adminUsername);
    }

    private void audit(String action, String target, String data, String result) {

        if (!LoggerUtils.isEnableV2AuditLogs()) {
            audit_log.info(String.format(AUDIT_MESSAGE, getUsername() + UserCoreConstants.TENANT_DOMAIN_COMBINER +
                    getTenantDomain(), action, target, data, result));
        }
    }

    private String getAuditData(String username, String idpID, String federatedUserID) {

        return "\"" + "Username" + "\" : \"" + username + "\", " + "\"" + "IdP" + "\" : \"" + idpID + "\", " + "\"" +
                "FederatedID" + "\" : \"" + federatedUserID + "\"";
    }


    private FederatedAssociationManager getFederatedAssociationManager() throws UserProfileException{

        FederatedAssociationManager federatedAssociationManager = IdentityUserProfileServiceDataHolder.getInstance()
                .getFederatedAssociationManager();
        if (federatedAssociationManager == null) {
            if (log.isDebugEnabled()) {
                log.debug("FederatedAssociationManager is not available in the OSGi framework");
            }
            String msg = "Error while working with federated associations";
            throw new UserProfileException(msg);
        }
        return federatedAssociationManager;
    }

    private User getUser(String domainAwareUserName) {

        User user = new User();
        user.setTenantDomain(CarbonContext.getThreadLocalCarbonContext().getTenantDomain());
        user.setUserStoreDomain(UserCoreUtil.extractDomainFromName(domainAwareUserName));
        user.setUserName(MultitenantUtils.getTenantAwareUsername(UserCoreUtil.removeDomainFromName(domainAwareUserName)));
        return user;
    }

    private boolean isFederatedAssociationDoesNotExistsError(FederatedAssociationManagerException e) {

        return e.getErrorCode().contains(String.valueOf(INVALID_FEDERATED_ASSOCIATION.getCode()));
    }

    private void validateFederatedAssociationParameters(String username, String idpID, String associatedID,
                                                        String auditData) throws UserProfileException {

        if (StringUtils.isEmpty(idpID) || StringUtils.isEmpty(associatedID)) {
            if (log.isDebugEnabled()) {
                log.debug("Required parameters, idpId or the associatedId are empty.");
            }
            audit(auditActionForDeleteAssociation, username, auditData, AUDIT_FAIL);
            String msg = "Error while removing association with federated IdP: " + idpID + " for user: " + username +
                    " in tenant: " + getTenantDomain();
            throw new UserProfileException(msg);
        }
    }

    private String getIdentityProviderName(String tenantDomain, String idpId)
            throws UserProfileException {

        try {
            IdpManager idpManager = IdentityUserProfileServiceDataHolder.getInstance().getIdpManager();
            if (idpManager != null) {
                IdentityProvider identityProvider = idpManager.getIdPByResourceId(idpId, tenantDomain, false);
                return identityProvider.getIdentityProviderName();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("The IdpManager service is not available in the runtime");
                }
                String msg = "Error while retrieving identity provider for the federated association";
                throw new UserProfileException(msg);
            }
        } catch (IdentityProviderManagementException e) {
            if (log.isDebugEnabled()) {
                log.debug("Could not resolve the identity provider for the id: "
                        + idpId + ", in the tenant domain: " + tenantDomain);
            }
            String msg = "Error while resolving identity provider";
            throw new UserProfileException(msg);
        }
    }
}
