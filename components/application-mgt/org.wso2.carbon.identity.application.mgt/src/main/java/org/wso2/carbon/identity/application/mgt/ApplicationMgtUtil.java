/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.mgt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementClientException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ApplicationPermission;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.PermissionsAndRoleConfig;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.SpFileStream;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.application.mgt.dao.ApplicationDAO;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponentHolder;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import static org.wso2.carbon.identity.application.mgt.ApplicationConstants.ENABLE_APPLICATION_ROLE_VALIDATION_PROPERTY;
import static org.wso2.carbon.user.core.constants.UserCoreErrorConstants.ErrorMessages.ERROR_CODE_ROLE_ALREADY_EXISTS;

/**
 * Few common utility functions related to Application (aka. Service Provider) Management.
 */
public class ApplicationMgtUtil {

    public static final String APPLICATION_ROOT_PERMISSION = "applications";
    public static final String PATH_CONSTANT = RegistryConstants.PATH_SEPARATOR;
    // Default regex for validating application name.
    // This regex allows alphanumeric characters, dot, underscore, hyphen and spaces in the name.
    // Does not allow leading and trailing whitespaces.
    public static final String APP_NAME_VALIDATING_REGEX = "^[a-zA-Z0-9._-]+(?: [a-zA-Z0-9._-]+)*$";
    private static final String SERVICE_PROVIDERS_NAME_REGEX = "ServiceProviders.SPNameRegex";
    public static final String MASKING_CHARACTER = "*";
    public static final String MASKING_REGEX = "(?<!^.?).(?!.?$)";
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final String DOMAIN_QUALIFIED_REGISTRY_SYSTEM_USERNAME =
            UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME + "/" + CarbonConstants.REGISTRY_SYSTEM_USERNAME;

    private static Log log = LogFactory.getLog(ApplicationMgtUtil.class);

    private ApplicationMgtUtil() {

    }

    public static org.wso2.carbon.user.api.Permission[] buildPermissions(String applicationName,
                                                                         String[] permissions) {

        org.wso2.carbon.user.api.Permission[] permissionSet = null;

        if (permissions != null) {
            permissionSet = new org.wso2.carbon.user.api.Permission[permissions.length];
            int i = 0;
            for (String permissionString : permissions) {
                permissionSet[i] = new org.wso2.carbon.user.api.Permission(applicationName + "\\"
                        + permissionString, "ui.execute");
            }
        }
        return permissionSet;
    }

    /**
     * Check whether validate roles is enabled via ApplicationMgt.EnableRoleValidation configuration in the
     * identity.xml.
     *
     * @return True if the config is set to true or if the config is not specified in the identity.xml.
     */
    public static boolean validateRoles() {

        String allowRoleValidationProperty = IdentityUtil.getProperty(ENABLE_APPLICATION_ROLE_VALIDATION_PROPERTY);
        if (StringUtils.isBlank(allowRoleValidationProperty)) {
            /*
            This means the configuration does not exist in the identity.xml. In that case, true needs to be
            returned to preserve backward compatibility.
             */
            return true;
        }
        return Boolean.parseBoolean(allowRoleValidationProperty);
    }

    public static boolean isUserAuthorized(String applicationName, String username, int applicationID)
            throws IdentityApplicationManagementException {

        if (!isUserAuthorized(applicationName, username)) {
            // maybe the role name of the app has updated. In this case, lets
            // load back the old app name
            ApplicationDAO appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
            String storedApplicationName = appDAO.getApplicationName(applicationID);
            return isUserAuthorized(storedApplicationName, username);
        }

        return true;
    }

    /**
     * @param applicationName
     * @param username
     * @return
     * @throws IdentityApplicationManagementException
     */
    public static boolean isUserAuthorized(String applicationName, String username)
            throws IdentityApplicationManagementException {

        boolean validateRoles = validateRoles();
        if (!validateRoles) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Validating user with application roles is disabled. Therefore, " +
                        "user: %s will be authorized for application: %s", username, applicationName));
            }
            return true;
        }
        String applicationRoleName = getAppRoleName(applicationName);
        try {
            if (log.isDebugEnabled()) {
                log.debug("Checking whether user has role : " + applicationRoleName + " by retrieving role list of " +
                        "user : " + username);
            }

            UserStoreManager userStoreManager = CarbonContext.getThreadLocalCarbonContext().getUserRealm()
                    .getUserStoreManager();
            if (userStoreManager instanceof AbstractUserStoreManager) {
                return ((AbstractUserStoreManager) userStoreManager).isUserInRole(username, applicationRoleName);
            }

            String[] userRoles = userStoreManager.getRoleListOfUser(username);
            for (String userRole : userRoles) {
                if (applicationRoleName.equals(userRole)) {
                    return true;
                }
            }
        } catch (UserStoreException e) {
            throw new IdentityApplicationManagementException("Error while checking authorization for user: " +
                    username + " for application: " + applicationName, e);
        }
        return false;
    }

    /**
     * Create a role for the application and assign the user to that role.
     *
     * @param applicationName
     * @throws IdentityApplicationManagementException
     */
    public static void createAppRole(String applicationName, String username)
            throws IdentityApplicationManagementException {

        boolean validateRoles = validateRoles();
        if (!validateRoles) {
            if (log.isDebugEnabled()) {
                log.debug("Validating user with application roles is disabled. Therefore, the application " +
                        "role will not be created for application: " + applicationName);
            }
            return;
        }
        String roleName = getAppRoleName(applicationName);
        String[] usernames = {username};
        UserStoreManager userStoreManager = null;
        try {
            userStoreManager = CarbonContext.getThreadLocalCarbonContext().getUserRealm().getUserStoreManager();
            // create a role for the application and assign the user to that role.
            if (log.isDebugEnabled()) {
                log.debug("Creating application role : " + roleName + " and assign the user : "
                        + Arrays.toString(usernames) + " to that role");
            }
            userStoreManager.addRole(roleName, usernames, null);
        } catch (UserStoreException e) {
            assignRoleToUser(username, roleName, userStoreManager, e);
        }
    }

    /**
     * If the Application/<sp-name> role addition has failed giving role already exists issue, then
     * assign the role to user.
     *
     * @param username         User name
     * @param roleName         Role name
     * @param userStoreManager User store manager
     * @param e                User store exception threw.
     * @throws IdentityApplicationManagementException
     */
    private static void assignRoleToUser(String username, String roleName, UserStoreManager userStoreManager,
                                         UserStoreException e) throws IdentityApplicationManagementException {

        String errorMsgString = String.format(ERROR_CODE_ROLE_ALREADY_EXISTS.getMessage(), roleName);
        String errMsg = e.getMessage();
        if (errMsg != null && (errMsg.contains(ERROR_CODE_ROLE_ALREADY_EXISTS.getCode()) ||
                errorMsgString.contains(errMsg))) {
            String[] newRoles = {roleName};
            if (log.isDebugEnabled()) {
                log.debug("Application role is already created. Skip creating: " + roleName + " and assigning" +
                        " the user: " + username);
            }
            try {
                userStoreManager.updateRoleListOfUser(username, null, newRoles);
            } catch (UserStoreException e1) {
                String msg = "Error while updating application role: " + roleName + " with user " + username;

                // If concurrent requests were made, the role could already be assigned to the user. When that
                // validation is done upon a user store exception(rather than checking it prior updating the role
                // list of the user), even the extreme case where the concurrent request assigns the role just before
                // db query is executed, is handled.
                try {
                    if (isRoleAlreadyApplied(username, roleName, userStoreManager)) {
                        if (log.isDebugEnabled()) {
                            log.debug("The role: " + roleName + ", is already assigned to the user: " + username
                                    + ". Skip assigning");
                        }
                        return;
                    }
                } catch (UserStoreException ex) {
                    msg = "Error while getting existing application roles of the user " + username;
                    throw new IdentityApplicationManagementException(msg, ex);
                }

                // Throw the error, unless the error caused from role being already assigned.
                throw new IdentityApplicationManagementException(msg, e1);
            }
        } else {
            throw new IdentityApplicationManagementException("Error while creating application role: " + roleName +
                    " with user " + username, e);
        }
    }

    private static boolean isRoleAlreadyApplied(String username, String roleName, UserStoreManager userStoreManager)
            throws UserStoreException {

        boolean isRoleAlreadyApplied = false;
        String[] roleListOfUser = userStoreManager.getRoleListOfUser(username);
        if (roleListOfUser != null) {
            isRoleAlreadyApplied = Arrays.asList(roleListOfUser).contains(roleName);
        }
        return isRoleAlreadyApplied;
    }

    private static String getAppRoleName(String applicationName) {

        return ApplicationConstants.APPLICATION_DOMAIN + UserCoreConstants.DOMAIN_SEPARATOR + applicationName;
    }

    /**
     * Delete the role of the app
     *
     * @param applicationName
     * @throws IdentityApplicationManagementException
     */
    public static void deleteAppRole(String applicationName) throws IdentityApplicationManagementException {

        String roleName = getAppRoleName(applicationName);
        if (log.isDebugEnabled()) {
            log.debug("Deleting application role : " + roleName);
        }
        UserStoreManager userStoreManager;
        try {
            userStoreManager = CarbonContext.getThreadLocalCarbonContext().getUserRealm().getUserStoreManager();
        } catch (UserStoreException e) {
            throw new IdentityApplicationManagementException(String.format("Error while getting the userstoreManager " +
                    "to delete the application role: %s for application: %s", roleName, applicationName), e);
        }
        try {
            userStoreManager.deleteRole(roleName);
        } catch (Exception e) {
            /*
             * For more information read https://github.com/wso2/product-is/issues/12579. This is to overcome the
             * above issue.
             */
            log.error(String.format("Initial attempt to delete the role: %s failed for application: %s. " +
                    "Retrying again", roleName, applicationName), e);
            boolean isOperationFailed = true;
            for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
                try {
                    Thread.sleep(1000);
                    userStoreManager.deleteRole(roleName);
                    isOperationFailed = false;
                    log.info(String.format("Role: %s deleted for application: %s in the retry attempt: %s", roleName,
                            applicationName, attempt));
                    break;
                } catch (Exception exception) {
                    log.error(String.format("Retry attempt: %s failed to delete role: %s for application: %s",
                            attempt, roleName, applicationName), exception);
                }
            }
            if (isOperationFailed) {
                throw new IdentityApplicationManagementException(String.format("Error occurred while trying to " +
                        "delete the application role: %s for application: %s", roleName, applicationName), e);
            }
        }
    }

    /**
     * @param oldName
     * @param newName
     * @throws IdentityApplicationManagementException
     */
    public static void renameRole(String oldName, String newName) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("Renaming application role : " + UserCoreUtil.addInternalDomainName(oldName)
                    + " to new role : " + UserCoreUtil.addInternalDomainName(newName));
        }
        CarbonContext.getThreadLocalCarbonContext().getUserRealm().getUserStoreManager()
                .updateRoleName(UserCoreUtil.addInternalDomainName(oldName),
                        UserCoreUtil.addInternalDomainName(newName));

    }

    /**
     * Rename the registry path node name for a deleted Service provider role.
     *
     * @param oldName
     * @param newName
     * @throws IdentityApplicationManagementException
     */
    public static void renameAppPermissionPathNode(String oldName, String newName)
            throws IdentityApplicationManagementException {

        ApplicationManagementServiceComponentHolder.getInstance().getApplicationPermissionProvider()
                .renameAppPermissionName(oldName, newName);
    }

    /**
     * Stores the permissions to applications.
     *
     * @param applicationName
     * @param permissionsConfig
     * @throws IdentityApplicationManagementException
     */
    public static void storePermissions(String applicationName, String username,
                                        PermissionsAndRoleConfig permissionsConfig)
            throws IdentityApplicationManagementException {

        ApplicationManagementServiceComponentHolder.getInstance().getApplicationPermissionProvider()
                .storePermissions(applicationName, permissionsConfig);
    }

    /**
     * Updates the permissions of the application
     *
     * @param applicationName
     * @param permissions
     * @throws IdentityApplicationManagementException
     */
    public static void updatePermissions(String applicationName, ApplicationPermission[] permissions)
            throws IdentityApplicationManagementException {

        ApplicationManagementServiceComponentHolder.getInstance().getApplicationPermissionProvider()
                .updatePermissions(applicationName, permissions);
    }

    /**
     * Loads the permissions of the application
     *
     * @param applicationName
     * @return
     * @throws IdentityApplicationManagementException
     */
    public static List<ApplicationPermission> loadPermissions(String applicationName)
            throws IdentityApplicationManagementException {

        return ApplicationManagementServiceComponentHolder.getInstance().getApplicationPermissionProvider()
                .loadPermissions(applicationName);
    }

    /**
     * Delete the resource
     *
     * @param applicationName
     * @throws IdentityApplicationManagementException
     */
    public static void deletePermissions(String applicationName) throws IdentityApplicationManagementException {

        ApplicationManagementServiceComponentHolder.getInstance().getApplicationPermissionProvider()
                .deletePermissions(applicationName);
    }

    /**
     * @param o1
     * @param o2
     * @return
     */
    public static Property[] concatArrays(Property[] o1, Property[] o2) {

        Property[] ret = new Property[o1.length + o2.length];

        System.arraycopy(o1, 0, ret, 0, o1.length);
        System.arraycopy(o2, 0, ret, o1.length, o2.length);

        return ret;
    }

    public static String getApplicationPermissionPath() {

        return CarbonConstants.UI_PERMISSION_NAME + RegistryConstants.PATH_SEPARATOR + APPLICATION_ROOT_PERMISSION;

    }

    /**
     * Validate application name according to the regex
     *
     * @return validated or not
     */
    public static boolean isRegexValidated(String applicationName) {

        String spValidatorRegex = getSPValidatorRegex();
        Pattern regexPattern = Pattern.compile(spValidatorRegex);
        return regexPattern.matcher(applicationName).matches();
    }

    /**
     * Return the Service Provider validation regex.
     *
     * @return regex.
     */
    public static String getSPValidatorRegex() {

        String spValidatorRegex = IdentityUtil.getProperty(SERVICE_PROVIDERS_NAME_REGEX);
        if (StringUtils.isBlank(spValidatorRegex)) {
            spValidatorRegex = APP_NAME_VALIDATING_REGEX;
        }
        return spValidatorRegex;
    }

    /**
     * Get Property values
     *
     * @param tenantDomain  Tenant domain
     * @param spIssuer      SP Issuer
     * @param propertyNames Property names
     * @return Properties map
     * @throws IdentityApplicationManagementException
     */
    protected Map<String, String> getPropertyValues(String tenantDomain, String spIssuer, List<String> propertyNames)
            throws IdentityApplicationManagementException {

        ServiceProvider serviceProvider = ApplicationMgtSystemConfig.getInstance().getApplicationDAO()
                .getApplication(spIssuer, tenantDomain);

        if (serviceProvider == null) {
            throw new IdentityApplicationManagementException(
                    "No service provider exists in the provided tenant, with the given issuer id " + spIssuer);
        }

        Map<String, String> propKeyValueMap = new HashMap<String, String>();

        InboundAuthenticationRequestConfig[] inboundAuthReqConfigs = serviceProvider.getInboundAuthenticationConfig()
                .getInboundAuthenticationRequestConfigs();

        if (inboundAuthReqConfigs != null && inboundAuthReqConfigs.length > 0) {
            for (InboundAuthenticationRequestConfig authConfig : inboundAuthReqConfigs) {
                Property[] properties = authConfig.getProperties();
                for (Property prop : properties) {
                    if (propertyNames.contains(prop.getName())) {
                        propKeyValueMap.put(prop.getName(), prop.getValue());
                    }
                }
            }
        }

        return propKeyValueMap;
    }

    /**
     * To check whether the application owner is valid by validating user existence and permissions.
     *
     * @param serviceProvider service provider
     * @return true if the application owner is valid.
     * @throws IdentityApplicationManagementException when an error occurs while validating the user.
     */
    public static boolean isValidApplicationOwner(ServiceProvider serviceProvider)
            throws IdentityApplicationManagementException {

        try {
            String userName;
            String userNameWithDomain;
            if (serviceProvider.getOwner() != null) {
                userName = serviceProvider.getOwner().getUserName();
                if (StringUtils.isEmpty(userName) || CarbonConstants.REGISTRY_SYSTEM_USERNAME.equals(userName)) {
                    return false;
                }
                String userStoreDomain = serviceProvider.getOwner().getUserStoreDomain();
                userNameWithDomain = IdentityUtil.addDomainToName(userName, userStoreDomain);

                String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
                Optional<User> user = getUser(tenantDomain, userNameWithDomain);
                if (user.isPresent()) {
                    return true;
                } else {
                    org.wso2.carbon.user.api.UserRealm realm = CarbonContext.getThreadLocalCarbonContext()
                            .getUserRealm();
                    if (realm == null) {
                        return false;
                    }

                    if (log.isDebugEnabled()) {
                        log.debug("Owner does not exist for application: " + serviceProvider.getApplicationName() +
                                ". Hence making the tenant admin the owner of the application.");
                    }
                    // Since the SP owner does not exist, set the tenant admin user as the owner.
                    User owner = new User();
                    String adminUserName = realm.getRealmConfiguration().getAdminUserName();
                    owner.setUserName(adminUserName);
                    owner.setUserStoreDomain(realm.getRealmConfiguration().
                            getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME));
                    owner.setTenantDomain(getUserTenantDomain(tenantDomain, adminUserName));
                    serviceProvider.setOwner(owner);
                }
            } else {
                return false;
            }
        } catch (UserStoreException e) {
            throw new IdentityApplicationManagementException("User validation failed for owner update in the " +
                    "application: " +
                    serviceProvider.getApplicationName(), e);
        }
        return true;
    }

    /**
     * Get Service provider name from XML configuration file
     *
     * @param spFileStream
     * @param tenantDomain
     * @return ServiceProvider
     * @throws IdentityApplicationManagementException
     */
    public static ServiceProvider getApplicationFromSpFileStream(SpFileStream spFileStream, String tenantDomain)
            throws IdentityApplicationManagementException {

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(ServiceProvider.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return (ServiceProvider) unmarshaller.unmarshal(spFileStream.getFileStream());

        } catch (JAXBException e) {
            throw new IdentityApplicationManagementException(String.format("Error in reading Service Provider " +
                    "configuration file %s uploaded by tenant: %s", spFileStream.getFileName(), tenantDomain), e);
        }
    }

    /**
     * Resolve user.
     *
     * @param tenantDomain The tenant domain which user is trying to access.
     * @param username     The username of resolving user.
     * @return User object.
     * @throws IdentityApplicationManagementException Error when user cannot be resolved.
     */
    public static Optional<User> getUser(String tenantDomain, String username)
            throws IdentityApplicationManagementException {

        User user = null;
        String userId = null;
        try {
            int tenantID = IdentityTenantUtil.getTenantId(tenantDomain);
            if (StringUtils.isBlank(username)) {
                userId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserId();
            }
            if (tenantID == MultitenantConstants.SUPER_TENANT_ID) {
                user = getUserFromTenant(username, userId, tenantID);
            } else {
                Tenant tenant = ApplicationManagementServiceComponentHolder.getInstance().getRealmService()
                        .getTenantManager().getTenant(tenantID);
                String accessedOrganizationId = tenant.getAssociatedOrganizationUUID();
                if (accessedOrganizationId == null) {
                    user = getUserFromTenant(username, userId, tenantID);
                } else {
                    Optional<org.wso2.carbon.user.core.common.User> resolvedUser =
                            ApplicationManagementServiceComponentHolder.getInstance()
                                    .getOrganizationUserResidentResolverService()
                                    .resolveUserFromResidentOrganization(username, userId, accessedOrganizationId);
                    if (resolvedUser.isPresent()) {
                        user = new User(resolvedUser.get());
                    }
                }
            }
        } catch (UserStoreException | OrganizationManagementException e) {
            throw new IdentityApplicationManagementException("Error resolving user.", e);
        }
        return Optional.ofNullable(user);
    }

    /**
     * Get user from tenant by username or user id.
     *
     * @param username The username.
     * @param userId   The user id.
     * @param tenantId The tenant id where user resides.
     * @return User object from tenant userStoreManager.
     * @throws IdentityApplicationManagementException Error when user cannot be resolved.
     */
    private static User getUserFromTenant(String username, String userId, int tenantId)
            throws IdentityApplicationManagementException {

        User user = null;
        try {
            AbstractUserStoreManager userStoreManager =
                    (AbstractUserStoreManager) ApplicationManagementServiceComponentHolder.getInstance()
                            .getRealmService().getTenantUserRealm(tenantId).getUserStoreManager();
            if (username != null) {
                if (userStoreManager.isExistingUser(username)) {
                    user = new User(userStoreManager.getUser(null, username));
                } else if (userStoreManager.isExistingUserWithID(username)) {
                    /*
                    For federated admin user flow, sometimes their user id will be sent to federated tenant as name.
                    Thus need to fetch user details using username as user id from their original
                    tenant's user-store manager.
                    */
                    user = new User(userStoreManager.getUser(username, null));
                }
            } else if (userId != null && userStoreManager.isExistingUserWithID(userId)) {
                user = new User(userStoreManager.getUser(userId, null));
            }
        } catch (UserStoreException e) {
            throw new IdentityApplicationManagementException("Error finding user in tenant.", e);
        }
        return user;
    }

    /**
     * Get user's tenant domain.
     *
     * @param tenantDomain The tenant domain which user is trying to access.
     *                     This is the same tenant that application resides.
     * @param username     The username of the user.
     * @return The tenant domain where the user resides.
     * @throws IdentityApplicationManagementException Error when user cannot be resolved.
     */
    public static String getUserTenantDomain(String tenantDomain, String username)
            throws IdentityApplicationManagementException {

        try {
            if (useApplicationTenantDomainAsUserTenantDomain(tenantDomain, username)) {
                return tenantDomain;
            }
            /*
             Else situation occur when the application creator is deleted. At that point,
             set the tenant domain of the application as the user's tenant domain.
             */
            return getUser(tenantDomain, username).map(User::getTenantDomain).orElse(tenantDomain);
        } catch (UserStoreException e) {
            throw new IdentityApplicationManagementException("Error while retrieving tenant.", e);
        }
    }

    private static boolean useApplicationTenantDomainAsUserTenantDomain(String tenantDomain, String username)
            throws UserStoreException {

        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain) ||
                DOMAIN_QUALIFIED_REGISTRY_SYSTEM_USERNAME.equals(username) ||
                !ApplicationManagementServiceComponentHolder.getInstance().isOrganizationManagementEnabled()) {
            return true;
        }
        /*
        If the tenant doesn't have an associated organization, return the application tenant
        as the user's tenant domain.
         */
        int tenantID = IdentityTenantUtil.getTenantId(tenantDomain);
        Tenant tenant = ApplicationManagementServiceComponentHolder.getInstance().getRealmService()
                .getTenantManager().getTenant(tenantID);
        String accessedOrganizationId = tenant.getAssociatedOrganizationUUID();
        return StringUtils.isEmpty(accessedOrganizationId);
    }

    /**
     * Get user's username.
     *
     * @param tenantDomain The tenant domain which user is trying to access.
     * @return username  The username.
     * @throws IdentityApplicationManagementException Error when user cannot be resolved.
     */
    public static String getUsername(String tenantDomain) throws IdentityApplicationManagementException {

        String username = CarbonContext.getThreadLocalCarbonContext().getUsername();
        if (username == null) {
            Optional<User> maybeUser = getUser(tenantDomain, null);
            User user = maybeUser
                    .orElseThrow(() -> new IdentityApplicationManagementException("Error resolving user."));
            username = IdentityUtil.addDomainToName(user.getUserName(), user.getUserStoreDomain());
        }
        return username;
    }

    /**
     * Get username with user's tenant domain appended.
     *
     * @param tenantDomain The tenant domain which user is trying to access.
     * @return The username with tenant domain.
     * @throws IdentityApplicationManagementException Error when user cannot be resolved.
     */
    public static String getUsernameWithUserTenantDomain(String tenantDomain)
            throws IdentityApplicationManagementException {

        String username = CarbonContext.getThreadLocalCarbonContext().getUsername();
        if (username == null) {
            Optional<User> maybeUser = getUser(tenantDomain, null);
            User user = maybeUser
                    .orElseThrow(() -> new IdentityApplicationManagementException("Error resolving user."));
            username = UserCoreUtil.addTenantDomainToEntry(IdentityUtil.addDomainToName(user.getUserName(),
                    user.getUserStoreDomain()), user.getTenantDomain());
        }
        return UserCoreUtil.addTenantDomainToEntry(username, tenantDomain);
    }

    public static void startTenantFlow(String tenantDomain, String userName)
            throws IdentityApplicationManagementException {

        startTenantFlow(tenantDomain);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(userName);
    }

    public static void startTenantFlow(String tenantDomain) throws IdentityApplicationManagementException {

        String userId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserId();
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUserId(userId);
    }

    /**
     * Method to verify if the tenant is active before accessing.
     *
     * @param tenantDomain The tenant domain which is trying to access.
     * @throws IdentityApplicationManagementException Error when tenant is deactivated.
     */
    public static void validateTenant(String tenantDomain) throws IdentityApplicationManagementException {

        if (StringUtils.isEmpty(tenantDomain)) {
            return;
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        if (MultitenantConstants.SUPER_TENANT_ID != tenantId && !IdentityTenantUtil.getTenant(tenantId).isActive()) {
            throw new IdentityApplicationManagementClientException("Tenant " + tenantDomain + " is deactivated.");
        }
    }

    public static void endTenantFlow() {

        PrivilegedCarbonContext.endTenantFlow();
    }

    /**
     * Method to get the ItemsPerPage property configured in the carbon.xml file.
     *
     * @return Items per page in pagination.
     */
    public static int getItemsPerPage() {

        String itemsPerPagePropertyValue =
                ServerConfiguration.getInstance().getFirstProperty(ApplicationConstants.ITEMS_PER_PAGE_PROPERTY);

        try {
            if (StringUtils.isNotBlank(itemsPerPagePropertyValue)) {
                int itemsPerPage = Math.abs(Integer.parseInt(itemsPerPagePropertyValue));
                if (log.isDebugEnabled()) {
                    log.debug("Items per page for pagination is set to : " + itemsPerPage);
                }
                return itemsPerPage;
            }
        } catch (NumberFormatException e) {
            // No need to handle exception since the default value is already set.
            log.warn("Error occurred while parsing the 'ItemsPerPage' property value in carbon.xml. Defaulting to: "
                    + ApplicationConstants.DEFAULT_RESULTS_PER_PAGE);
        }

        return ApplicationConstants.DEFAULT_RESULTS_PER_PAGE;
    }

    /**
     * Get the application id from service provider object.
     *
     * @param serviceProvider Service provider object.
     * @return Id of the service provider.
     */
    public static String getAppId(ServiceProvider serviceProvider) {

        if (serviceProvider != null) {
            return serviceProvider.getApplicationResourceId();
        }
        return StringUtils.EMPTY;
    }

    /**
     * Get the application name from service provider object.
     *
     * @param serviceProvider Service provider object.
     * @return Name of the service provider.
     */
    public static String getApplicationName(ServiceProvider serviceProvider) {

        if (serviceProvider != null) {
            return serviceProvider.getApplicationName();
        }
        return "Undefined";
    }

    /**
     * Get the initiator id.
     *
     * @param userName     Username of the initiator.
     * @param tenantDomain Tenant domain of the initiator.
     * @return User id of the initiator.
     * @deprecated
     * This method is moved to IdentityUtil class as this will be used from other components as well.
     */
    @Deprecated
    public static String getInitiatorId(String userName, String tenantDomain) {

        return IdentityUtil.getInitiatorId(userName, tenantDomain);
    }

    /**
     * Build the service provider JSON string masking the sensitive information.
     *
     * @param serviceProvider Service provider object.
     * @return JSON string of the service provider object.
     */
    public static String buildSPData(ServiceProvider serviceProvider) {

        if (serviceProvider == null) {
            return StringUtils.EMPTY;
        }
        try {
            JSONObject serviceProviderJSONObject =
                    new JSONObject(new ObjectMapper().writeValueAsString(serviceProvider));
            JSONObject inboundAuthenticationConfig =
                    serviceProviderJSONObject.optJSONObject("inboundAuthenticationConfig");
            if (inboundAuthenticationConfig != null) {
                JSONArray inboundAuthenticationRequestConfigsArray =
                        inboundAuthenticationConfig.optJSONArray("inboundAuthenticationRequestConfigs");
                if (inboundAuthenticationRequestConfigsArray != null) {
                    for (int i = 0; i < inboundAuthenticationRequestConfigsArray.length(); i++) {
                        JSONObject requestConfig = inboundAuthenticationRequestConfigsArray.getJSONObject(i);
                        JSONArray properties = requestConfig.optJSONArray("properties");
                        if (properties != null) {
                            for (int j = 0; j < properties.length(); j++) {
                                JSONObject property = properties.optJSONObject(j);
                                if (property != null && StringUtils.equalsIgnoreCase("oauthConsumerSecret",
                                        (String) property.get("name"))) {
                                    if (property.get("value") != null) {
                                        String secret = property.get("value").toString();
                                        String maskedSecret = secret.replaceAll(MASKING_REGEX, MASKING_CHARACTER);
                                        property.put("value", maskedSecret);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return serviceProviderJSONObject.toString();
        } catch (JsonProcessingException e) {
            log.error("Error while converting service provider object to json.");
        }
        return StringUtils.EMPTY;
    }

}
