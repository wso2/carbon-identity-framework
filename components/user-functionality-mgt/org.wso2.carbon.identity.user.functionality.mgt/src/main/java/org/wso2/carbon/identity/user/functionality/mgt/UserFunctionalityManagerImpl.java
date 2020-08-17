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
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.user.functionality.mgt.dao.UserFunctionalityManagerDAO;
import org.wso2.carbon.identity.user.functionality.mgt.dao.UserFunctionalityPropertyDAO;
import org.wso2.carbon.identity.user.functionality.mgt.dao.impl.UserFunctionalityManagerDAOImpl;
import org.wso2.carbon.identity.user.functionality.mgt.dao.impl.UserFunctionalityPropertyDAOImpl;
import org.wso2.carbon.identity.user.functionality.mgt.exception.UserFunctionalityManagementException;
import org.wso2.carbon.identity.user.functionality.mgt.exception.UserFunctionalityManagementServerException;
import org.wso2.carbon.identity.user.functionality.mgt.model.FunctionalityLockStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * User functionality manager service implementation.
 */
public class UserFunctionalityManagerImpl implements UserFunctionalityManager {

    private UserFunctionalityManagerDAO userFunctionalityManagerDAO = new UserFunctionalityManagerDAOImpl();
    private UserFunctionalityPropertyDAO userFunctionalityPropertyDAO = new UserFunctionalityPropertyDAOImpl();

    /**
     * {@inheritDoc}
     */
    @Override
    public FunctionalityLockStatus getLockStatus(String userId, int tenantId, String functionalityIdentifier)
            throws UserFunctionalityManagementServerException {

        if (!isPerUserFunctionalityLockingEnabled()) {
            throw new UnsupportedOperationException("Per-user functionality locking is not enabled.");
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
            throws UserFunctionalityManagementServerException {

        if (!isPerUserFunctionalityLockingEnabled()) {
            throw new UnsupportedOperationException("Per-user functionality locking is not enabled.");
        }
        return userFunctionalityPropertyDAO.getAllProperties(userId, tenantId, functionalityIdentifier);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProperties(String userId, int tenantId, String functionalityIdentifier,
                              Map<String, String> functionalityLockProperties)
            throws UserFunctionalityManagementServerException {

        if (!isPerUserFunctionalityLockingEnabled()) {
            throw new UnsupportedOperationException("Per-user functionality locking is not enabled.");
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
            throws UserFunctionalityManagementServerException {

        if (!isPerUserFunctionalityLockingEnabled()) {
            throw new UnsupportedOperationException("Per-user functionality locking is not enabled.");
        }
        userFunctionalityManagerDAO.deleteMappingForUser(userId, tenantId, functionalityIdentifier);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAllPropertiesForUser(String userId, int tenantId, String functionalityIdentifier)
            throws UserFunctionalityManagementServerException {

        if (!isPerUserFunctionalityLockingEnabled()) {
            throw new UnsupportedOperationException("Per-user functionality locking is not enabled.");
        }
        userFunctionalityPropertyDAO.deleteAllPropertiesForUser(userId, tenantId, functionalityIdentifier);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deletePropertiesForUser(String userId, int tenantId, String functionalityIdentifier,
                                        Set<String> propertiesToDelete)
            throws UserFunctionalityManagementServerException {

        if (!isPerUserFunctionalityLockingEnabled()) {
            throw new UnsupportedOperationException("Per-user functionality locking is not enabled.");
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
}
