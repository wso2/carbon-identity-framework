/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.user.functionality.mgt;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.annotation.bundle.Capability;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.user.functionality.mgt.dao.UserFunctionalityManagerDAO;
import org.wso2.carbon.identity.user.functionality.mgt.dao.UserFunctionalityPropertyDAO;
import org.wso2.carbon.identity.user.functionality.mgt.dao.impl.UserFunctionalityManagerDAOImpl;
import org.wso2.carbon.identity.user.functionality.mgt.dao.impl.UserFunctionalityPropertyDAOImpl;
import org.wso2.carbon.identity.user.functionality.mgt.exception.UserFunctionalityManagementClientException;
import org.wso2.carbon.identity.user.functionality.mgt.exception.UserFunctionalityManagementException;
import org.wso2.carbon.identity.user.functionality.mgt.exception.UserFunctionalityManagementServerException;
import org.wso2.carbon.identity.user.functionality.mgt.internal.UserFunctionalityManagerComponentDataHolder;
import org.wso2.carbon.identity.user.functionality.mgt.model.FunctionalityLockStatus;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UniqueIDUserStoreManager;
import org.wso2.carbon.user.core.constants.UserCoreErrorConstants;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * User functionality manager service implementation.
 */
@Capability(
        namespace = "osgi.service",
        attribute = {
                "objectClass=org.wso2.carbon.identity.user.functionality.mgt.UserFunctionalityManager",
                "service.scope=singleton"
        }
)
public class UserFunctionalityManagerImpl implements UserFunctionalityManager {

    private UserFunctionalityManagerDAO userFunctionalityManagerDAO = new UserFunctionalityManagerDAOImpl();
    private UserFunctionalityPropertyDAO userFunctionalityPropertyDAO = new UserFunctionalityPropertyDAOImpl();
    private static final Log log = LogFactory.getLog(UserFunctionalityManagerImpl.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public FunctionalityLockStatus getLockStatus(String userId, int tenantId, String functionalityIdentifier)
            throws UserFunctionalityManagementException {

        if (!isPerUserFunctionalityLockingEnabled()) {
            throw new UnsupportedOperationException("Per-user functionality locking is not enabled.");
        }

        if (StringUtils.isEmpty(userId) || !isUserIdExists(userId, tenantId)) {
            if (log.isDebugEnabled()) {
                log.debug("Cannot retrieve user from userId: " + userId);
            }
            throw buildUserNotFoundError();
        }

        FunctionalityLockStatus
                functionalityLockStatus =
                userFunctionalityManagerDAO.getFunctionalityLockStatus(userId, tenantId, functionalityIdentifier);
        if (functionalityLockStatus == null) {
            return FunctionalityLockStatus.UNLOCKED_STATUS;
        }
        long unlockTime = functionalityLockStatus.getUnlockTime();
        if (unlockTime < System.currentTimeMillis()) {
            userFunctionalityManagerDAO.deleteMappingForUser(userId, tenantId, functionalityIdentifier);
            return FunctionalityLockStatus.UNLOCKED_STATUS;
        }
        return functionalityLockStatus;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getProperties(String userId, int tenantId, String functionalityIdentifier)
            throws UserFunctionalityManagementException {

        if (!isPerUserFunctionalityLockingEnabled()) {
            throw new UnsupportedOperationException("Per-user functionality locking is not enabled.");
        }
        if (StringUtils.isEmpty(userId) || !isUserIdExists(userId, tenantId)) {
            if (log.isDebugEnabled()) {
                log.debug("Cannot retrieve user from userId: " + userId);
            }
            throw buildUserNotFoundError();
        }
        return userFunctionalityPropertyDAO.getAllProperties(userId, tenantId, functionalityIdentifier);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProperties(String userId, int tenantId, String functionalityIdentifier,
                              Map<String, String> functionalityLockProperties)
            throws UserFunctionalityManagementException {

        if (!isPerUserFunctionalityLockingEnabled()) {
            throw new UnsupportedOperationException("Per-user functionality locking is not enabled.");
        }
        if (StringUtils.isEmpty(userId) || !isUserIdExists(userId, tenantId)) {
            if (log.isDebugEnabled()) {
                log.debug("Cannot retrieve user from userId: " + userId);
            }
            throw buildUserNotFoundError();
        }

        Map<String, String> existingProperties = getProperties(userId, tenantId, functionalityIdentifier);
        if (MapUtils.isNotEmpty(functionalityLockProperties)) {
            addOrUpdateProperties(functionalityLockProperties, existingProperties, userId, tenantId,
                    functionalityIdentifier);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void lock(String userId, int tenantId, String functionalityIdentifier, long timeToLock,
                     String functionalityLockReasonCode, String functionalityLockReason)
            throws UserFunctionalityManagementException {

        if (!isPerUserFunctionalityLockingEnabled()) {
            throw new UnsupportedOperationException("Per-user functionality locking is not enabled.");
        }
        if (StringUtils.isEmpty(userId) || !isUserIdExists(userId, tenantId)) {
            if (log.isDebugEnabled()) {
                log.debug("Cannot retrieve user from userId: " + userId);
            }
            throw buildUserNotFoundError();
        }
        long unlockTime = Long.MAX_VALUE;
        if (timeToLock >= 0) {
            unlockTime = System.currentTimeMillis() + timeToLock;
        }
        FunctionalityLockStatus functionalityLockStatus =
                userFunctionalityManagerDAO.getFunctionalityLockStatus(userId, tenantId, functionalityIdentifier);
        if (functionalityLockStatus != null) {
            boolean isFunctionalityLockedForUser = functionalityLockStatus.getLockStatus();
            long oldUnlockTime = functionalityLockStatus.getUnlockTime();

            if (!isFunctionalityLockedForUser) {
                FunctionalityLockStatus newFunctionalityLockStatus =
                        new FunctionalityLockStatus(true, unlockTime, functionalityLockReasonCode,
                                functionalityLockReason);
                userFunctionalityManagerDAO
                        .updateLockStatusForUser(userId, tenantId, functionalityIdentifier, newFunctionalityLockStatus);

            } else if (oldUnlockTime < unlockTime) {
                functionalityLockStatus.setLockReasonCode(functionalityLockReasonCode);
                functionalityLockStatus.setLockReason(functionalityLockReason);
                functionalityLockStatus.setUnlockTime(unlockTime);
                userFunctionalityManagerDAO
                        .updateLockStatusForUser(userId, tenantId, functionalityIdentifier, functionalityLockStatus);
            }
        } else {
            FunctionalityLockStatus newFunctionalityLockStatus =
                    new FunctionalityLockStatus(true, unlockTime, functionalityLockReasonCode, functionalityLockReason);
            userFunctionalityManagerDAO
                    .addFunctionalityLock(userId, tenantId, functionalityIdentifier, newFunctionalityLockStatus);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unlock(String userId, int tenantId, String functionalityIdentifier)
            throws UserFunctionalityManagementException {

        if (!isPerUserFunctionalityLockingEnabled()) {
            throw new UnsupportedOperationException("Per-user functionality locking is not enabled.");
        }
        if (StringUtils.isEmpty(userId) || !isUserIdExists(userId, tenantId)) {
            if (log.isDebugEnabled()) {
                log.debug("Cannot retrieve user from userId: " + userId);
            }
            throw buildUserNotFoundError();
        }
        userFunctionalityManagerDAO.deleteMappingForUser(userId, tenantId, functionalityIdentifier);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAllPropertiesForUser(String userId, int tenantId, String functionalityIdentifier)
            throws UserFunctionalityManagementException {

        if (!isPerUserFunctionalityLockingEnabled()) {
            throw new UnsupportedOperationException("Per-user functionality locking is not enabled.");
        }
        if (StringUtils.isEmpty(userId) || !isUserIdExists(userId, tenantId)) {
            if (log.isDebugEnabled()) {
                log.debug("Cannot retrieve user from userId: " + userId);
            }
            throw buildUserNotFoundError();
        }
        userFunctionalityPropertyDAO.deleteAllPropertiesForUser(userId, tenantId, functionalityIdentifier);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deletePropertiesForUser(String userId, int tenantId, String functionalityIdentifier,
                                        Set<String> propertiesToDelete)
            throws UserFunctionalityManagementException {

        if (!isPerUserFunctionalityLockingEnabled()) {
            throw new UnsupportedOperationException("Per-user functionality locking is not enabled.");
        }
        if (StringUtils.isEmpty(userId) || !isUserIdExists(userId, tenantId)) {
            if (log.isDebugEnabled()) {
                log.debug("Cannot retrieve user from userId: " + userId);
            }
            throw buildUserNotFoundError();
        }
        userFunctionalityPropertyDAO.deletePropertiesForUser(userId, tenantId, functionalityIdentifier, propertiesToDelete);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAllMappingsForTenant(int tenantId) throws UserFunctionalityManagementServerException {

        userFunctionalityManagerDAO.deleteAllMappingsForTenant(tenantId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAllPropertiesForTenant(int tenantId) throws UserFunctionalityManagementServerException {

        userFunctionalityPropertyDAO.deleteAllPropertiesForTenant(tenantId);
    }

    private void addOrUpdateProperties(Map<String, String> newProperties, Map<String, String> oldProperties,
                                       String userId, int tenantId, String functionalityIdentifier)
            throws UserFunctionalityManagementServerException {

        Map<String, String> propertiesToAdd = new HashMap<String, String>();
        Map<String, String> propertiesToUpdate = new HashMap<String, String>();

        if (MapUtils.isNotEmpty(oldProperties)) {
            newProperties.forEach((k, v) -> {
                if (oldProperties.containsKey(k)) {
                    propertiesToUpdate.put(k, v);
                } else {
                    propertiesToAdd.put(k, v);
                }
            });
        } else {
            newProperties.forEach(propertiesToAdd::put);
        }
        if (MapUtils.isNotEmpty(propertiesToAdd)) {
            userFunctionalityPropertyDAO.addProperties(userId, tenantId, functionalityIdentifier, propertiesToAdd);
        }
        if (MapUtils.isNotEmpty(propertiesToUpdate)) {
            userFunctionalityPropertyDAO
                    .updateProperties(userId, tenantId, functionalityIdentifier, propertiesToUpdate);
        }
    }

    private boolean isPerUserFunctionalityLockingEnabled() {

        return Boolean.parseBoolean(
                IdentityUtil.getProperty(UserFunctionalityMgtConstants.ENABLE_PER_USER_FUNCTIONALITY_LOCKING));
    }

    private boolean isUserIdExists(String userId, int tenantId) throws UserFunctionalityManagementClientException,
            UserFunctionalityManagementServerException {

        boolean isUserExists;

        try {
            UniqueIDUserStoreManager uniqueIdEnabledUserStoreManager = getUniqueIdEnabledUserStoreManager(
                    UserFunctionalityManagerComponentDataHolder.getInstance().getRealmService(),
                    IdentityTenantUtil.getTenantDomain(tenantId));

            isUserExists = uniqueIdEnabledUserStoreManager.isExistingUserWithID(userId);
            return isUserExists;
        } catch (UserStoreException e) {
            if (isUserNotExistingError(e, userId)) {
                if (log.isDebugEnabled()) {
                    log.debug("Cannot retrieve user from userId: " + userId, e);
                }
                throw buildUserNotFoundError();
            }
            throw new UserFunctionalityManagementServerException(
                    UserFunctionalityMgtConstants.ErrorMessages.ERROR_OCCURRED_WHILE_RETRIEVING_USER.getCode(),
                    UserFunctionalityMgtConstants.ErrorMessages.ERROR_OCCURRED_WHILE_RETRIEVING_USER.getDescription());
        }

    }

    private UniqueIDUserStoreManager getUniqueIdEnabledUserStoreManager(RealmService realmService, String tenantDomain)
            throws UserStoreException, UserFunctionalityManagementClientException {

        UserStoreManager userStoreManager = realmService.getTenantUserRealm(
                IdentityTenantUtil.getTenantId(tenantDomain)).getUserStoreManager();
        if (!(userStoreManager instanceof UniqueIDUserStoreManager)) {
            if (log.isDebugEnabled()) {
                String msg = "Provided user store manager does not support unique user IDs in the tenant domain" +
                        tenantDomain;
                log.debug(msg);
            }
            throw buildUserNotFoundError();
        }
        return (UniqueIDUserStoreManager) userStoreManager;
    }

    private boolean isUserNotExistingError(UserStoreException e, String userId) {

        if (log.isDebugEnabled()) {
            String msg = "Provided user corresponding to the userid" + userId + "does not exist";
            log.debug(msg);
        }
        return e instanceof org.wso2.carbon.user.core.UserStoreException &&
                UserCoreErrorConstants.ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getCode().equals(
                        ((org.wso2.carbon.user.core.UserStoreException) e).getErrorCode());
    }

    private UserFunctionalityManagementClientException buildUserNotFoundError() {

        return new UserFunctionalityManagementClientException(
                UserFunctionalityMgtConstants.ErrorMessages.USER_NOT_FOUND.getCode(),
                UserFunctionalityMgtConstants.ErrorMessages.USER_NOT_FOUND.getDescription());
    }
}
