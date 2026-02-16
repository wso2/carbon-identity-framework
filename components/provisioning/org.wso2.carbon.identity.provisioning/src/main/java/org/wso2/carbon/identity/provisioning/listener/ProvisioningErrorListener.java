/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org)
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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.annotation.bundle.Capability;
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
import org.wso2.carbon.identity.core.model.IdentityEventListenerConfig;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.provisioning.IdentityProvisioningConstants;
import org.wso2.carbon.identity.provisioning.IdentityProvisioningException;
import org.wso2.carbon.identity.provisioning.OutboundProvisioningManager;
import org.wso2.carbon.identity.provisioning.ProvisioningEntity;
import org.wso2.carbon.identity.provisioning.ProvisioningEntityType;
import org.wso2.carbon.identity.provisioning.ProvisioningOperation;
import org.wso2.carbon.identity.provisioning.internal.ProvisioningServiceDataHolder;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.core.constants.UserCoreErrorConstants.ErrorMessages;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.listener.UserManagementErrorEventListener;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.user.mgt.UserMgtConstants;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Used for handle provisioning errors.
 */
@Capability(
        namespace = "osgi.service",
        attribute = {
                "objectClass=org.wso2.carbon.user.core.listener.UserManagementErrorEventListener",
                "service.scope=singleton"
        }
)
public class ProvisioningErrorListener extends AbstractIdentityUserMgtFailureEventListener {

    private static final Log log = LogFactory.getLog(ProvisioningErrorListener.class);

    @Override
    public boolean onAddUserFailureWithID(String errorCode, String errorMessage, String userName, Object credential,
                                          String[] roleList, Map<String, String> claims, String profile, UserStoreManager userStoreManager)
            throws UserStoreException {

        // Outbound provisioning calls should not to be reverted if inbound provisioning is success.
        if (errorCode.equalsIgnoreCase(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_ADD_USER.getCode())) {
            return true;
        }
        return deleteOutboundProvisionedUser(userName, userStoreManager);
    }

    @Override
    public boolean onAddRoleFailure(String errorCode, String errorMessage, String roleName, String[] userList,
                                    Permission[] permissions, UserStoreManager userStoreManager)
            throws UserStoreException {

        // Outbound provisioning calls should not to be reverted if inbound provisioning is success.
        if (errorCode.equalsIgnoreCase(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_ADD_ROLE.getCode())) {
            return true;
        }
        return deleteOutboundProvisionedRole(roleName, permissions, userStoreManager);
    }

    @Override
    public boolean onSetUserClaimValuesFailure(String errorCode, String errorMessage, String userName,
                                               Map<String, String> claims, String profileName, UserStoreManager userStoreManager)
            throws UserStoreException {

        // Outbound provisioning calls should not to be reverted if inbound provisioning is success.
        if (errorCode.equalsIgnoreCase(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_SET_USER_CLAIM_VALUES.getCode()) ||
                claims.isEmpty()) {
            return true;
        }
        Set<String> keys = claims.keySet();
        String[] claimURIs = keys.toArray(new String[keys.size()]);
        Map<String, String> inboundAttributes = userStoreManager.getUserClaimValues(userName, claimURIs,
                UserCoreConstants.DEFAULT_PROFILE);
        return setOutboundProvisionedUserClaimValues(userName, inboundAttributes, userStoreManager);
    }

    @Override
    public boolean onDeleteRoleFailure(String errorCode, String errorMessage, String roleName,
                                       UserStoreManager userStoreManager) throws UserStoreException {

        // Outbound provisioning calls should not to be reverted if inbound provisioning is success.
        if (errorCode.equalsIgnoreCase(ErrorMessages.ERROR_CODE_ERROR_DURING_POST_DELETE_ROLE.getCode())) {
            return true;
        }
        Permission[] permissions = null;
        String[] users = userStoreManager.getUserListOfRole(roleName);
        String tenantDomainName = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomainName);
        try {
            String[] permissionList = ProvisioningServiceDataHolder.getInstance().getRolePermissionManagementService()
                    .getRolePermissions(roleName, tenantId);
            if (permissionList.length > 0) {
                permissions = new Permission[permissionList.length];
                for (int index = 0; index < permissionList.length; index++) {
                    permissions[index] = new Permission(permissionList[index], UserMgtConstants.EXECUTE_ACTION);
                }
            }
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Error while getting the permissions for a role.", e);
            }
        }
        if (permissions == null) {
            permissions = new Permission[0];
        }
        return addOutboundProvisioningRole(roleName, users, userStoreManager);
    }

    /**
     * Method to add role.
     *
     * @param roleName          Name of the role.
     * @param userList          Users assigned to the role.
     * @param userStoreManager  User store.
     * @return {boolean} Status shows that the error is handled.
     * @throws UserStoreException if error occurred while adding an outbound provisioning role.
     */
    private boolean addOutboundProvisioningRole(String roleName, String[] userList, UserStoreManager userStoreManager)
            throws  UserStoreException {

        Map<ClaimMapping, List<String>> outboundAttributes = new HashMap<>();
        if (StringUtils.isNotEmpty(roleName)) {
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
            log.debug("Adding domain name : " + domainName + " to role : " + roleName);
        }
        String domainAwareName = UserCoreUtil.addDomainToName(roleName, domainName);

        ProvisioningEntity provisioningEntity = new ProvisioningEntity(
                ProvisioningEntityType.GROUP, domainAwareName, ProvisioningOperation.POST,
                outboundAttributes);
        return outboundProvisionEntity(provisioningEntity);
    }

    /**
     * Method to set values for user claims.
     *
     * @param userName          UserName.
     * @param inboundAttributes Claims.
     * @param userStoreManager  User Store.
     * @return {boolean} Status shows that the error is handled.
     * @throws IdentityProvisioningException if error occurred while setting outbound provisioned user claims.
     */
    private boolean setOutboundProvisionedUserClaimValues(String userName, Map<String, String> inboundAttributes,
                                                          UserStoreManager userStoreManager) throws IdentityProvisioningException {

        Map<ClaimMapping, List<String>> outboundAttributes = new HashMap<>();
        if (StringUtils.isNotEmpty(userName)) {
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
        // Set the in-bound attribute list.
        provisioningEntity.setInboundAttributes(inboundAttributes);
        return outboundProvisionEntity(provisioningEntity);
    }

    /**
     * Method to delete a role.
     *
     * @param roleName          Name of the role.
     * @param userStoreManager  User Store.
     * @return {boolean} Status shows that the error is handled.
     * @throws UserStoreException if error occurred while deleting an outbound provisioned role.
     */
    private boolean deleteOutboundProvisionedRole(String roleName, Permission[] permissions,
                                                  UserStoreManager userStoreManager) throws UserStoreException {

        Map<ClaimMapping, List<String>> outboundAttributes = new HashMap<>();
        if (StringUtils.isNotEmpty(roleName)) {
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
        return outboundProvisionEntity(provisioningEntity);
    }

    /**
     * Method to delete a user.
     *
     * @param userName          UserName.
     * @param userStoreManager  User Store.
     * @return {boolean} Status shows that the error is handled.
     * @throws UserStoreException if error occurred while deleting an outbound provisioned user.
     */
    private boolean deleteOutboundProvisionedUser(String userName, UserStoreManager userStoreManager) throws UserStoreException {
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
        return outboundProvisionEntity(provisioningEntity);
    }

    /**
     * Method to get the priority of the listener.
     *
     * @return {int} priority.
     */
    public int getExecutionOrderId() {

        IdentityEventListenerConfig identityEventListenerConfig = IdentityUtil.readEventListenerProperty
                (UserManagementErrorEventListener.class.getName(), this.getClass().getName());
        if (identityEventListenerConfig == null) {
            return IdentityCoreConstants.EVENT_LISTENER_ORDER_ID;
        }
        return identityEventListenerConfig.getOrder();
    }

    /**
     * Method to out provision the entity.
     *
     * @param provisioningEntity Entity to be provisioned.
     * @return {boolean} Status shows that the entity is provisioned.
     * @throws IdentityProvisioningException if error occurred while out provisioning an entity.
     */
    private boolean outboundProvisionEntity(ProvisioningEntity provisioningEntity) throws IdentityProvisioningException {

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
            // Call framework method to provision the user.
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
}
