*
        * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
        *
        * WSO2 Inc. licenses this file to you under the Apache License,
        * Version 2.0 (the "License"); you may not use this file except
        * in compliance with the License.
        * You may obtain a copy of the License at
        *
        *      http://www.apache.org/licenses/LICENSE-2.0
        *
        * Unless required by applicable law or agreed to in writing,
        * software distributed under the License is distributed on an
        * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
        * KIND, either express or implied.  See the License for the
        * specific language governing permissions and limitations
        * under the License.
        */

        package org.wso2.carbon.identity.provisioning.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.ProvisioningServiceProviderType;
import org.wso2.carbon.identity.application.common.model.ThreadLocalProvisioningServiceProvider;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationManagementUtil;
import org.wso2.carbon.identity.application.mgt.ApplicationConstants;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.core.AbstractIdentityUserMgtFailureEventListener;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.provisioning.IdentityProvisioningConstants;
import org.wso2.carbon.identity.provisioning.IdentityProvisioningException;
import org.wso2.carbon.identity.provisioning.OutboundProvisioningManager;
import org.wso2.carbon.identity.provisioning.ProvisioningEntity;
import org.wso2.carbon.identity.provisioning.ProvisioningEntityType;
import org.wso2.carbon.identity.provisioning.ProvisioningOperation;
import org.wso2.carbon.identity.provisioning.internal.ProvisioningServiceDataHolder;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.user.mgt.UserMgtConstants;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OutboundProvisioningErrorListener extends AbstractIdentityUserMgtFailureEventListener {

    private static final Log log = LogFactory.getLog(OutboundProvisioningErrorListener.class);

    // Add user related error codes.
    private String ERROR_CODE_INVALID_USER_NAME = "31301";
    private String ERROR_CODE_INVALID_PASSWORD = "30003";
    private String ERROR_CODE_USER_ALREADY_EXISTS = "30004";
    private String ERROR_CODE_INTERNAL_ROLE_NOT_EXISTS = "31303";
    private String ERROR_CODE_EXTERNAL_ROLE_NOT_EXISTS = "31304";
    private String ERROR_CODE_UNABLE_TO_FETCH_CLAIM_MAPPING = "31305";
    private String ERROR_CODE_INVALID_CLAIM_URI = "30005";
    private String ERROR_CODE_ERROR_WHILE_ADDING_USER = "31306";

    // Add roles related error codes.
    private String ERROR_CODE_READONLY_USER_STORE = "30002";
    private String ERROR_CODE_INVALID_ROLE_NAME = "30011";
    private String ERROR_CODE_ERROR_WHILE_ADDING_ROLE = "31702";
    private String ERROR_CODE_WRITE_GROUPS_NOT_ENABLED = "30014";
    private String ERROR_CODE_ROLE_ALREADY_EXISTS = "30012";

    // Set claims related error codes.
    private String ERROR_CODE_ERROR_WHILE_SETTING_USER_CLAIM_VALUES = "39002";

    // Delete Claim values related codes.
    private String ERROR_CODE_ERROR_WHILE_DELETING_USER_CLAIM_VALUES = "31103";
    private String ERROR_CODE_ERROR_WHILE_DELETING_USER_CLAIM_VALUE = "31203";

    // Delete role related codes.
    private String ERROR_CODE_ERROR_WHILE_DELETE_ROLE = "31802";

    @Override
    public boolean onAddUserFailureWithID(String errorCode, String errorMessage, String userName, Object credential,
                                          String[] roleList, Map<String, String> claims, String profile, UserStoreManager userStoreManager)
            throws UserStoreException {

        if (errorCode.equalsIgnoreCase(ERROR_CODE_INVALID_USER_NAME) ||
                errorCode.equalsIgnoreCase(ERROR_CODE_INVALID_PASSWORD) ||
                errorCode.equalsIgnoreCase(ERROR_CODE_USER_ALREADY_EXISTS) ||
                errorCode.equalsIgnoreCase(ERROR_CODE_INTERNAL_ROLE_NOT_EXISTS) ||
                errorCode.equalsIgnoreCase(ERROR_CODE_EXTERNAL_ROLE_NOT_EXISTS) ||
                errorCode.equalsIgnoreCase(ERROR_CODE_UNABLE_TO_FETCH_CLAIM_MAPPING) ||
                errorCode.equalsIgnoreCase(ERROR_CODE_INVALID_CLAIM_URI) ||
                errorCode.equalsIgnoreCase(ERROR_CODE_ERROR_WHILE_ADDING_USER)
        ) {
            return deleteUser(userName, userStoreManager);
        }
        return true;
    }

    @Override
    public boolean onAddRoleFailure(String errorCode, String errorMessage, String roleName, String[] userList,
                                    org.wso2.carbon.user.api.Permission[] permissions, UserStoreManager userStoreManager)
            throws UserStoreException {

        if (errorCode.equalsIgnoreCase(ERROR_CODE_READONLY_USER_STORE) ||
                errorCode.equalsIgnoreCase(ERROR_CODE_INVALID_ROLE_NAME) ||
                errorCode.equalsIgnoreCase(ERROR_CODE_ERROR_WHILE_ADDING_ROLE) ||
                errorCode.equalsIgnoreCase(ERROR_CODE_WRITE_GROUPS_NOT_ENABLED) ||
                errorCode.equalsIgnoreCase(ERROR_CODE_ROLE_ALREADY_EXISTS)
        ) {
            deleteRole(roleName, permission, userStoreManager);
        }
        return true;
    }

    @Override
    public boolean onSetUserClaimValuesFailure(String errorCode, String errorMessage, String userName,
                                               Map<String, String> claims, String profileName, UserStoreManager userStoreManager)
            throws UserStoreException {

        if (errorCode.equalsIgnoreCase(ERROR_CODE_ERROR_WHILE_SETTING_USER_CLAIM_VALUES) ||
                errorCode.equalsIgnoreCase(ERROR_CODE_READONLY_USER_STORE)
        ) {
            Set<String> keys = claims.keySet();
            String[] claimURIs = keys.toArray(new String[keys.size()]);

            if (claimURIs.length > 0) {
                Map<String, String> inboundAttributes = userStoreManager.getUserClaimValues(userName, claimURIs,
                        UserCoreConstants.DEFAULT_PROFILE);
                setUserClaimValues(userName, inboundAttributes, userStoreManager);
            }
        }
        return true;
    }

    @Override
    public boolean onDeleteUserClaimValuesFailure(String errorCode, String errorMessage, String userName, String[] claims,
                                                  String profileName, UserStoreManager userStoreManager)
            throws UserStoreException {

        if (errorCode.equalsIgnoreCase(ERROR_CODE_ERROR_WHILE_DELETING_USER_CLAIM_VALUES)) {
            if (claims.length > 0) {
                Map<String, String> inboundAttributes = userStoreManager.getUserClaimValues(userName, claims,
                        UserCoreConstants.DEFAULT_PROFILE);
                setUserClaimValues(userName, inboundAttributes, userStoreManager);
            }
        }
        return true;
    }

    @Override
    public boolean onDeleteUserClaimValueFailure(String errorCode, String errorMessage, String userName, String claimURI,
                                                 String profileName, UserStoreManager userStoreManager)
            throws UserStoreException {

        if (errorCode.equalsIgnoreCase(ERROR_CODE_ERROR_WHILE_DELETING_USER_CLAIM_VALUE)) {
            if (!claimURI.isEmpty()) {
                Map<String, String> inboundAttributes = userStoreManager.getUserClaimValues(userName, new String[] {
                        claimURI}, UserCoreConstants.DEFAULT_PROFILE);
                setUserClaimValues(userName, inboundAttributes, userStoreManager);
            }
        }
        return true;
    }

    @Override
    public boolean onDeleteRoleFailure(String errorCode, String errorMessage, String roleName,
                                       UserStoreManager userStoreManager) throws UserStoreException {

        if (errorCode.equalsIgnoreCase(ERROR_CODE_READONLY_USER_STORE) ||
                errorCode.equalsIgnoreCase(ERROR_CODE_WRITE_GROUPS_NOT_ENABLED) ||
                errorCode.equalsIgnoreCase(ERROR_CODE_ERROR_WHILE_DELETE_ROLE)
        ) {
            org.wso2.carbon.user.api.Permission[] permissions = null;
            String[] users = userStoreManager.getUserListOfRole(roleName);
            String tenantDomainName = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            int tenantId = IdentityTenantUtil.getTenantId(tenantDomainName);
            try {
                String[] permissionList = ProvisioningServiceDataHolder.getInstance().getRolePermissionManagementService()
                        .getRolePermissions(roleName, tenantId);
                if (permissionList.length > 0) {
                    for (int index=0; index<permissionList.length; index++) {
                        permissions[index] = new org.wso2.carbon.user.api.Permission(permissionList[index],
                                UserMgtConstants.EXECUTE_ACTION);
                    }
                }
            } catch (Exception e) {
                // Do nothing
            }
            if (permissions == null) {
                permissions = new org.wso2.carbon.user.api.Permission[0];
            }
            addRole(roleName, users, userStoreManager);
        }
        return true;
    }

    /**
     * Method to add role.
     * @param roleName - Name of the role
     * @param userList - Users assigned to the role
     * @param userStoreManager - User store
     * @return boolean
     * @throws UserStoreException
     */
    private boolean addRole(String roleName, String[] userList, UserStoreManager userStoreManager)
            throws  UserStoreException {

        Map<ClaimMapping, List<String>> outboundAttributes = new HashMap<>();

        if (roleName != null) {
            outboundAttributes.put(ClaimMapping.build(
                    IdentityProvisioningConstants.GROUP_CLAIM_URI, null, null, false), Arrays
                    .asList(new String[]{roleName}));
        }

        if (userList != null && userList.length > 0) {
            outboundAttributes.put(ClaimMapping.build(
                    IdentityProvisioningConstants.USERNAME_CLAIM_URI, null, null, false), Arrays
                    .asList(userList));
        }

        String domainName = UserCoreUtil.getDomainName(userStoreManager.getRealmConfiguration());
        if (log.isDebugEnabled()) {
            log.debug("Adding domain name : " + domainName + " to user : " + roleName);
        }
        String domainAwareName = UserCoreUtil.addDomainToName(roleName, domainName);

        ProvisioningEntity provisioningEntity = new ProvisioningEntity(
                ProvisioningEntityType.GROUP, domainAwareName, ProvisioningOperation.POST,
                outboundAttributes);

        String tenantDomainName = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();

        ThreadLocalProvisioningServiceProvider threadLocalServiceProvider;
        threadLocalServiceProvider = IdentityApplicationManagementUtil
                .getThreadLocalProvisioningServiceProvider();

        if (threadLocalServiceProvider != null) {
            String serviceProvider = threadLocalServiceProvider.getServiceProviderName();
            tenantDomainName = threadLocalServiceProvider.getTenantDomain();
            if (threadLocalServiceProvider.getServiceProviderType() == ProvisioningServiceProviderType.OAUTH) {
                try {
                    serviceProvider = ApplicationManagementService.getInstance()
                            .getServiceProviderNameByClientId(
                                    threadLocalServiceProvider.getServiceProviderName(),
                                    IdentityApplicationConstants.OAuth2.NAME, tenantDomainName);
                } catch (IdentityApplicationManagementException e) {
                    log.error("Error while provisioning", e);
                    return true;
                }
            }

            // call framework method to provision the user.
            OutboundProvisioningManager.getInstance().provision(provisioningEntity,
                    serviceProvider, threadLocalServiceProvider.getClaimDialect(),
                    tenantDomainName, threadLocalServiceProvider.isJustInTimeProvisioning());
        } else {
            // call framework method to provision the group.
            OutboundProvisioningManager.getInstance()
                    .provision(provisioningEntity, ApplicationConstants.LOCAL_SP,
                            IdentityProvisioningConstants.WSO2_CARBON_DIALECT, tenantDomainName, false);
        }

        return true;
    }

    /**
     * Method to set values for user claims.
     * @param userName - UserName
     * @param inboundAttributes - Claims
     * @param userStoreManager - User Store
     * @return boolean
     * @throws IdentityProvisioningException
     */
    private boolean setUserClaimValues(String userName, Map<String, String> inboundAttributes,
                                       UserStoreManager userStoreManager) throws IdentityProvisioningException {

        Map<ClaimMapping, List<String>> outboundAttributes = new HashMap<>();

        if (userName != null) {
            outboundAttributes.put(ClaimMapping.build(
                            IdentityProvisioningConstants.USERNAME_CLAIM_URI, null, null, false),
                    Arrays.asList(new String[]{userName}));
        }

        String domainName = UserCoreUtil.getDomainName(userStoreManager.getRealmConfiguration());
        if (log.isDebugEnabled()) {
            log.debug("Adding domain name : " + domainName + " to user : " + userName);
        }
        String domainAwareName = UserCoreUtil.addDomainToName(userName, domainName);

        ProvisioningEntity provisioningEntity = new ProvisioningEntity(
                ProvisioningEntityType.USER, domainAwareName, ProvisioningOperation.PUT,
                outboundAttributes);

        // set the in-bound attribute list.
        provisioningEntity.setInboundAttributes(inboundAttributes);

        String tenantDomainName = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();

        ThreadLocalProvisioningServiceProvider threadLocalServiceProvider;
        threadLocalServiceProvider = IdentityApplicationManagementUtil
                .getThreadLocalProvisioningServiceProvider();

        if (threadLocalServiceProvider != null) {

            String serviceProvider = threadLocalServiceProvider.getServiceProviderName();
            tenantDomainName = threadLocalServiceProvider.getTenantDomain();
            if (threadLocalServiceProvider.getServiceProviderType() == ProvisioningServiceProviderType.OAUTH) {
                try {
                    serviceProvider = ApplicationManagementService.getInstance()
                            .getServiceProviderNameByClientId(
                                    threadLocalServiceProvider.getServiceProviderName(),
                                    IdentityApplicationConstants.OAuth2.NAME, tenantDomainName);
                } catch (IdentityApplicationManagementException e) {
                    log.error("Error while provisioning", e);
                    return true;
                }
            }

            // call framework method to provision the user.
            OutboundProvisioningManager.getInstance().provision(provisioningEntity,
                    serviceProvider, threadLocalServiceProvider.getClaimDialect(),
                    tenantDomainName, threadLocalServiceProvider.isJustInTimeProvisioning());
        } else {
            // call framework method to provision the user.
            OutboundProvisioningManager.getInstance()
                    .provision(provisioningEntity, ApplicationConstants.LOCAL_SP,
                            IdentityProvisioningConstants.WSO2_CARBON_DIALECT, tenantDomainName, false);
        }
        return true;
    }

    /**
     * Method to delete a role.
     * @param roleName - Name of the role
     * @param userStoreManager - User Store
     * @return boolean
     * @throws UserStoreException
     */
    private boolean deleteRole(String roleName, org.wso2.carbon.user.api.Permission[] permissions,
                               UserStoreManager userStoreManager) throws UserStoreException {

        Map<ClaimMapping, List<String>> outboundAttributes = new HashMap<>();

        if (roleName != null) {
            outboundAttributes.put(ClaimMapping.build(
                    IdentityProvisioningConstants.GROUP_CLAIM_URI, null, null, false), Arrays
                    .asList(new String[]{roleName}));
        }

        String domainName = UserCoreUtil.getDomainName(userStoreManager.getRealmConfiguration());
        if (log.isDebugEnabled()) {
            log.debug("Adding domain name : " + domainName + " to user : " + roleName);
        }
        String domainAwareName = UserCoreUtil.addDomainToName(roleName, domainName);

        ProvisioningEntity provisioningEntity = new ProvisioningEntity(
                ProvisioningEntityType.GROUP, domainAwareName, ProvisioningOperation.DELETE,
                outboundAttributes);

        String tenantDomainName = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();

        ThreadLocalProvisioningServiceProvider threadLocalServiceProvider;
        threadLocalServiceProvider = IdentityApplicationManagementUtil
                .getThreadLocalProvisioningServiceProvider();

        if (threadLocalServiceProvider != null) {
            String serviceProvider = threadLocalServiceProvider.getServiceProviderName();
            tenantDomainName = threadLocalServiceProvider.getTenantDomain();
            if (threadLocalServiceProvider.getServiceProviderType() == ProvisioningServiceProviderType.OAUTH) {
                try {
                    serviceProvider = ApplicationManagementService.getInstance()
                            .getServiceProviderNameByClientId(
                                    threadLocalServiceProvider.getServiceProviderName(),
                                    IdentityApplicationConstants.OAuth2.NAME, tenantDomainName);
                } catch (IdentityApplicationManagementException e) {
                    log.error("Error while provisioning", e);
                    return true;
                }
            }

            // call framework method to provision the user.
            OutboundProvisioningManager.getInstance().provision(provisioningEntity,
                    serviceProvider, threadLocalServiceProvider.getClaimDialect(),
                    tenantDomainName, threadLocalServiceProvider.isJustInTimeProvisioning());
        } else {
            // call framework method to provision the group.
            OutboundProvisioningManager.getInstance()
                    .provision(provisioningEntity, ApplicationConstants.LOCAL_SP,
                            IdentityProvisioningConstants.WSO2_CARBON_DIALECT, tenantDomainName, false);
        }
        return true;
    }

    /**
     * Method to delete a user.
     * @param userName - UserName
     * @param userStoreManager - User Store
     * @return boolean
     * @throws UserStoreException
     */
    private boolean deleteUser(String userName, UserStoreManager userStoreManager) throws UserStoreException {
        Map<ClaimMapping, List<String>> outboundAttributes = new HashMap<>();

        outboundAttributes.put(ClaimMapping.build(
                IdentityProvisioningConstants.USERNAME_CLAIM_URI, null, null, false), Arrays
                .asList(new String[]{userName}));

        String domainName = UserCoreUtil.getDomainName(userStoreManager.getRealmConfiguration());
        if (log.isDebugEnabled()) {
            log.debug("Adding domain name : " + domainName + " to user : " + userName);
        }
        String domainAwareName = UserCoreUtil.addDomainToName(userName, domainName);

        ProvisioningEntity provisioningEntity = new ProvisioningEntity(
                ProvisioningEntityType.USER, domainAwareName, ProvisioningOperation.DELETE,
                outboundAttributes);
        String tenantDomainName = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();

        ThreadLocalProvisioningServiceProvider threadLocalServiceProvider;
        threadLocalServiceProvider = IdentityApplicationManagementUtil
                .getThreadLocalProvisioningServiceProvider();

        if (threadLocalServiceProvider != null) {
            String serviceProvider = threadLocalServiceProvider.getServiceProviderName();
            tenantDomainName = threadLocalServiceProvider.getTenantDomain();
            if (threadLocalServiceProvider.getServiceProviderType() == ProvisioningServiceProviderType.OAUTH) {
                try {
                    serviceProvider = ApplicationManagementService.getInstance()
                            .getServiceProviderNameByClientId(
                                    threadLocalServiceProvider.getServiceProviderName(),
                                    IdentityApplicationConstants.OAuth2.NAME, tenantDomainName);
                } catch (IdentityApplicationManagementException e) {
                    log.error("Error while provisioning", e);
                    return true;
                }
            }

            // call framework method to provision the user.
            OutboundProvisioningManager.getInstance().provision(provisioningEntity,
                    serviceProvider, threadLocalServiceProvider.getClaimDialect(),
                    tenantDomainName, threadLocalServiceProvider.isJustInTimeProvisioning());
        } else {
            OutboundProvisioningManager.getInstance()
                    .provision(provisioningEntity, ApplicationConstants.LOCAL_SP,
                            IdentityProvisioningConstants.WSO2_CARBON_DIALECT, tenantDomainName, false);
        }
        return true;
    }

    /**
     * Method to get the priority of the listener.
     * @return {int} priority
     */
    public int getExecutionOrderId() {
        return 9;
    }
}
