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

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ApplicationPermission;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.PermissionsAndRoleConfig;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.SpFileStream;
import org.wso2.carbon.identity.application.mgt.dao.ApplicationDAO;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponentHolder;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.registry.api.Collection;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.user.mgt.UserMgtConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import static org.wso2.carbon.user.core.constants.UserCoreErrorConstants.ErrorMessages.ERROR_CODE_ROLE_ALREADY_EXISTS;

public class ApplicationMgtUtil {

    public static final String APPLICATION_ROOT_PERMISSION = "applications";
    public static final String PATH_CONSTANT = RegistryConstants.PATH_SEPARATOR;
    // Regex for validating application name
    public static String APP_NAME_VALIDATING_REGEX = "^[a-zA-Z0-9 ._-]*$";
    private static final String SERVICE_PROVIDERS_NAME_REGEX = "ServiceProviders.SPNameRegex";

    private static Log log = LogFactory.getLog(ApplicationMgtUtil.class);
    private static boolean perSPCertificateSupportAvailable;

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
                throw new IdentityApplicationManagementException("Error while updating application role: " +
                        roleName + " with user " + username, e1);

            }
        } else {
            throw new IdentityApplicationManagementException("Error while creating application role: " + roleName +
                    " with user " + username, e);
        }
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

        try {
            if (log.isDebugEnabled()) {
                log.debug("Deleting application role : " + roleName);
            }
            CarbonContext.getThreadLocalCarbonContext().getUserRealm().getUserStoreManager()
                    .deleteRole(roleName);
        } catch (UserStoreException e) {
            throw new IdentityApplicationManagementException("Error while creating application", e);
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
                .updateRoleName(UserCoreUtil.addInternalDomainName(oldName), UserCoreUtil.addInternalDomainName(newName));

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

        List<ApplicationPermission> loadPermissions = loadPermissions(oldName);
        String newApplicationNode = ApplicationMgtUtil.getApplicationPermissionPath() + PATH_CONSTANT + oldName;
        Registry tenantGovReg = CarbonContext.getThreadLocalCarbonContext().getRegistry(
                RegistryType.USER_GOVERNANCE);
        //creating new application node
        try {
            for (ApplicationPermission applicationPermission : loadPermissions) {
                tenantGovReg.delete(newApplicationNode + PATH_CONSTANT + applicationPermission.getValue());
            }
            tenantGovReg.delete(newApplicationNode);
            Collection permissionNode = tenantGovReg.newCollection();
            permissionNode.setProperty("name", newName);
            newApplicationNode = ApplicationMgtUtil.getApplicationPermissionPath() + PATH_CONSTANT + newName;
            String applicationNode = newApplicationNode;
            tenantGovReg.put(newApplicationNode, permissionNode);
            addPermission(applicationNode, loadPermissions.toArray(new ApplicationPermission[loadPermissions.size()]),
                    tenantGovReg);
        } catch (RegistryException e) {
            throw new IdentityApplicationManagementException("Error while renaming permission node "
                    + oldName + "to " + newName, e);
        }
    }

    /**
     * Stores the permissions to applications.
     *
     * @param applicationName
     * @param permissionsConfig
     * @throws IdentityApplicationManagementException
     */
    public static void storePermissions(String applicationName, String username, PermissionsAndRoleConfig permissionsConfig)
            throws IdentityApplicationManagementException {

        int tenantId = MultitenantConstants.INVALID_TENANT_ID;
        try {
            tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            IdentityTenantUtil.initializeRegistry(tenantId, IdentityTenantUtil.getTenantDomain(tenantId));
        } catch (IdentityException e) {
            throw new IdentityApplicationManagementException("Error loading tenant registry for tenant domain: " +
                    IdentityTenantUtil.getTenantDomain(tenantId), e);
        }
        Registry tenantGovReg = CarbonContext.getThreadLocalCarbonContext().getRegistry(
                RegistryType.USER_GOVERNANCE);

        String permissionResourcePath = getApplicationPermissionPath();
        try {
            if (!tenantGovReg.resourceExists(permissionResourcePath)) {
                boolean loggedInUserChanged = false;
                UserRealm realm =
                        (UserRealm) CarbonContext.getThreadLocalCarbonContext().getUserRealm();
                if (!realm.getAuthorizationManager()
                        .isUserAuthorized(username, permissionResourcePath,
                                UserMgtConstants.EXECUTE_ACTION)) {
                    //Logged in user is not authorized to create the permission.
                    // Temporarily change the user to the admin for creating the permission
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(
                            realm.getRealmConfiguration().getAdminUserName());
                    tenantGovReg = CarbonContext.getThreadLocalCarbonContext()
                            .getRegistry(RegistryType.USER_GOVERNANCE);
                    loggedInUserChanged = true;
                }
                Collection appRootNode = tenantGovReg.newCollection();
                appRootNode.setProperty("name", "Applications");
                tenantGovReg.put(permissionResourcePath, appRootNode);
                if (loggedInUserChanged) {
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(username);
                }
            }

            if (permissionsConfig != null) {
                ApplicationPermission[] permissions = permissionsConfig.getPermissions();
                if (permissions == null || permissions.length < 1) {
                    return;
                }

                // creating the application node in the tree
                String appNode = permissionResourcePath + PATH_CONSTANT + applicationName;
                Collection appNodeColl = tenantGovReg.newCollection();
                tenantGovReg.put(appNode, appNodeColl);

                // now start storing the permissions
                for (ApplicationPermission permission : permissions) {
                    String permissinPath = appNode + PATH_CONSTANT + permission;
                    Resource permissionNode = tenantGovReg.newResource();
                    permissionNode.setProperty("name", permission.getValue());
                    tenantGovReg.put(permissinPath, permissionNode);
                }
            }

        } catch (Exception e) {
            throw new IdentityApplicationManagementException("Error while storing permissions for application " +
                    applicationName, e);
        }
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

        String applicationNode = getApplicationPermissionPath() + PATH_CONSTANT + applicationName;

        Registry tenantGovReg = CarbonContext.getThreadLocalCarbonContext().getRegistry(
                RegistryType.USER_GOVERNANCE);

        try {

            boolean exist = tenantGovReg.resourceExists(applicationNode);
            if (!exist) {
                Collection appRootNode = tenantGovReg.newCollection();
                appRootNode.setProperty("name", applicationName);
                tenantGovReg.put(applicationNode, appRootNode);
            }

            Collection appNodeCollec = (Collection) tenantGovReg.get(applicationNode);
            String[] childern = appNodeCollec.getChildren();

            // new permissions are null. deleting all permissions case
            if ((childern != null && childern.length > 0)
                    && (permissions == null || permissions.length == 0)) { // there are permissions
                tenantGovReg.delete(applicationNode);
            }

            if (ArrayUtils.isEmpty(permissions)) {
                return;
            }

            // no permission exist for the application, create new
            if (childern == null || appNodeCollec.getChildCount() < 1) {

                addPermission(applicationNode, permissions, tenantGovReg);

            } else { // there are permission
                List<ApplicationPermission> loadPermissions = loadPermissions(applicationName);
                for (ApplicationPermission applicationPermission : loadPermissions) {
                    tenantGovReg.delete(applicationNode + PATH_CONSTANT + applicationPermission.getValue());
                }
                addPermission(applicationNode, permissions, tenantGovReg);
            }

        } catch (RegistryException e) {
            throw new IdentityApplicationManagementException("Error while storing permissions", e);
        }

    }

    private static void addPermission(String applicationNode, ApplicationPermission[] permissions, Registry
            tenantGovReg) throws RegistryException {
        for (ApplicationPermission permission : permissions) {
            String permissionValue = permission.getValue();

            if ("/".equals(permissionValue.substring(0, 1))) {         //if permissions are starts with slash remove that
                permissionValue = permissionValue.substring(1);
            }
            String[] splitedPermission = permissionValue.split("/");
            String permissinPath = applicationNode + PATH_CONSTANT;

            for (int i = 0; i < splitedPermission.length; i++) {
                permissinPath = permissinPath + splitedPermission[i] + PATH_CONSTANT;
                Collection permissionNode = tenantGovReg.newCollection();
                permissionNode.setProperty("name", splitedPermission[i]);
                tenantGovReg.put(permissinPath, permissionNode);
            }

        }
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
        String applicationNode = getApplicationPermissionPath() + PATH_CONSTANT + applicationName;
        Registry tenantGovReg = CarbonContext.getThreadLocalCarbonContext().getRegistry(
                RegistryType.USER_GOVERNANCE);
        List<String> paths = new ArrayList<>();

        try {
            boolean exist = tenantGovReg.resourceExists(applicationNode);

            if (!exist) {
                return Collections.emptyList();
            }

            boolean loggedInUserChanged = false;
            String loggedInUser = CarbonContext.getThreadLocalCarbonContext().getUsername();

            UserRealm realm = (UserRealm) CarbonContext.getThreadLocalCarbonContext().getUserRealm();
            if (loggedInUser == null || !realm.getAuthorizationManager().isUserAuthorized(
                    loggedInUser, applicationNode, UserMgtConstants.EXECUTE_ACTION)) {
                //Logged in user is not authorized to read the permission.
                // Temporarily change the user to the admin for reading the permission
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(
                        realm.getRealmConfiguration().getAdminUserName());
                tenantGovReg = CarbonContext.getThreadLocalCarbonContext()
                        .getRegistry(RegistryType.USER_GOVERNANCE);
                loggedInUserChanged = true;
            }


            paths.clear();             //clear current paths
            List<ApplicationPermission> permissions = new ArrayList<ApplicationPermission>();


            permissionPath(tenantGovReg, applicationNode, paths, applicationNode);      //get permission paths
            // recursively

            for (String permissionPath : paths) {
                ApplicationPermission permission;
                permission = new ApplicationPermission();
                permission.setValue(permissionPath);
                permissions.add(permission);
            }

            if (loggedInUserChanged) {
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(loggedInUser);
            }

            return permissions;

        } catch (RegistryException | org.wso2.carbon.user.core.UserStoreException e) {
            throw new IdentityApplicationManagementException("Error while reading permissions", e);
        }
    }

    private static void permissionPath(Registry tenantGovReg, String permissionPath, List<String> paths, String
            applicationNode) throws RegistryException {

        Collection appCollection = (Collection) tenantGovReg.get(permissionPath);
        String[] childern = appCollection.getChildren();

        if (childern == null || childern.length == 0) {
            paths.add(permissionPath.replace(applicationNode, "").substring(2));
        }

        while (childern != null && childern.length != 0) {
            for (int i = 0; i < childern.length; i++) {
                permissionPath(tenantGovReg, childern[i], paths, applicationNode);
            }
            break;

        }
    }

    /**
     * Delete the resource
     *
     * @param applicationName
     * @throws IdentityApplicationManagementException
     */
    public static void deletePermissions(String applicationName) throws IdentityApplicationManagementException {

        String applicationNode = getApplicationPermissionPath() + PATH_CONSTANT + applicationName;
        Registry tenantGovReg = CarbonContext.getThreadLocalCarbonContext().getRegistry(
                RegistryType.USER_GOVERNANCE);

        try {
            boolean exist = tenantGovReg.resourceExists(applicationNode);

            if (exist) {
                tenantGovReg.delete(applicationNode);
            }

        } catch (RegistryException e) {
            throw new IdentityApplicationManagementException("Error while storing permissions", e);
        }
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
    public static boolean isValidApplicationOwner(ServiceProvider serviceProvider) throws IdentityApplicationManagementException {

        try {
            String userName = null;
            String userNameWithDomain = null;
            if (serviceProvider.getOwner() != null) {
                userName = serviceProvider.getOwner().getUserName();
                if (StringUtils.isEmpty(userName) || CarbonConstants.REGISTRY_SYSTEM_USERNAME.equals(userName)) {
                    return false;
                }
                String userStoreDomain = serviceProvider.getOwner().getUserStoreDomain();
                userNameWithDomain = IdentityUtil.addDomainToName(userName, userStoreDomain);
            }
            org.wso2.carbon.user.api.UserRealm realm = CarbonContext.getThreadLocalCarbonContext().getUserRealm();
            if (realm == null || StringUtils.isEmpty(userNameWithDomain)) {
                return false;
            }
            boolean isUserExist = realm.getUserStoreManager().isExistingUser(userNameWithDomain);
            if (!isUserExist) {
                throw new IdentityApplicationManagementException("User validation failed for owner update in the application: " +
                        serviceProvider.getApplicationName() + " as user is not existing.");
            }
        } catch (UserStoreException | IdentityApplicationManagementException e) {
            throw new IdentityApplicationManagementException("User validation failed for owner update in the application: " +
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

    public static void startTenantFlow(String tenantDomain, String userName)
            throws IdentityApplicationManagementException {

        startTenantFlow(tenantDomain);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(userName);
    }

    public static void startTenantFlow(String tenantDomain) throws IdentityApplicationManagementException {

        int tenantId;
        try {
            tenantId = ApplicationManagementServiceComponentHolder.getInstance().getRealmService()
                    .getTenantManager().getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            throw new IdentityApplicationManagementException("Error when setting tenant domain. ", e);
        }
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
    }

    public static void endTenantFlow() {

        PrivilegedCarbonContext.endTenantFlow();
    }
}
