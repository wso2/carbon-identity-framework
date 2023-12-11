/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.user.onboard.core.service.password;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.governance.service.notification.NotificationChannels;
import org.wso2.carbon.identity.recovery.IdentityRecoveryConstants;
import org.wso2.carbon.identity.recovery.IdentityRecoveryException;
import org.wso2.carbon.identity.recovery.RecoveryScenarios;
import org.wso2.carbon.identity.recovery.RecoverySteps;
import org.wso2.carbon.identity.recovery.internal.IdentityRecoveryServiceDataHolder;
import org.wso2.carbon.identity.recovery.model.UserRecoveryData;
import org.wso2.carbon.identity.recovery.store.JDBCRecoveryDataStore;
import org.wso2.carbon.identity.recovery.store.UserRecoveryDataStore;
import org.wso2.carbon.identity.recovery.util.Utils;
import org.wso2.carbon.identity.user.onboard.core.service.model.Configuration;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;

/**
 * Generates the password reset link for the user.
 */
public class ResetLinkGenerator {
    private static final Log LOG = LogFactory.getLog(ResetLinkGenerator.class);

    /**
     * Generate a unique password reset link for the given user.
     *
     * @param configuration Link generation configuration.
     * @return Generated unique link.
     * @throws IdentityRecoveryException on an error.
     */
    public String generateResetLink(Configuration configuration) throws IdentityRecoveryException {

        User user = new User();
        user.setUserName(configuration.getUsername());
        user.setTenantDomain(configuration.getTenantDomain());
        user.setUserStoreDomain(configuration.getUserStore());

        if (!isValidUserStoreExists(user.getUserStoreDomain())) {
            throw Utils.handleClientException(IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_USER_STORE_INVALID,
                    user.getUserStoreDomain());
        }

        if (!isExistingUser(user)) {
            // If the user does not exist, Check for NOTIFY_USER_EXISTENCE property. If the property is not
            // enabled, notify with an empty NotificationResponseBean.
            throw Utils.handleClientException(IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_INVALID_USER,
                    user.getUserName());
        }
        if (Utils.isAccountDisabled(user)) {
            // If the NotifyUserAccountStatus is disabled, notify with an empty NotificationResponseBean.
            throw Utils.handleClientException(IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_DISABLED_ACCOUNT,
                    user.getUserName());
        }
        if (Utils.isAccountLocked(user)) {
            // If the NotifyUserAccountStatus is disabled, notify with an empty NotificationResponseBean.
            throw Utils.handleClientException(IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_LOCKED_ACCOUNT,
                    user.getUserName());
        }

        UserRecoveryData recoveryDataDO = generateNewConfirmationCode(user, NotificationChannels.EXTERNAL_CHANNEL
                .getChannelType());
        String secretKey = recoveryDataDO.getSecret();

        String serverHost = ConfigurationFacade.getInstance().getAccountRecoveryEndpointPath();

        return String.format("%s/confirmrecovery.do?confirmation=%s", serverHost, secretKey);
    }

    /**
     * Generates the new confirmation code details for a corresponding user.
     *
     * @param user                Details of the user that needs the confirmation code.
     * @param notificationChannel Method to send the recovery information. eg : EMAIL, SMS.
     * @return Created recovery data object.
     * @throws IdentityRecoveryException Error while generating the recovery information.
     */
    private UserRecoveryData generateNewConfirmationCode(User user, String notificationChannel)
            throws IdentityRecoveryException {

        UserRecoveryDataStore userRecoveryDataStore = JDBCRecoveryDataStore.getInstance();
        userRecoveryDataStore.invalidate(user);
        String secretKey = Utils.generateSecretKey(notificationChannel, user.getTenantDomain(),
                RecoveryScenarios.NOTIFICATION_BASED_PW_RECOVERY.name());
        UserRecoveryData recoveryDataDO = new UserRecoveryData(user, secretKey,
                RecoveryScenarios.NOTIFICATION_BASED_PW_RECOVERY, RecoverySteps.UPDATE_PASSWORD);

        // Store the notified channel in the recovery object for future reference.
        recoveryDataDO.setRemainingSetIds(notificationChannel);
        userRecoveryDataStore.store(recoveryDataDO);
        return recoveryDataDO;
    }

    /**
     * Check for user existence.
     *
     * @param user User
     * @return True if the user exists
     * @throws IdentityRecoveryException Error while checking user existence
     */
    private boolean isExistingUser(User user) throws IdentityRecoveryException {

        try {
            int tenantId = IdentityTenantUtil.getTenantId(user.getTenantDomain());
            UserStoreManager userStoreManager;
            userStoreManager = IdentityRecoveryServiceDataHolder.getInstance().getRealmService().
                    getTenantUserRealm(tenantId).getUserStoreManager();
            String domainQualifiedUsername = IdentityUtil
                    .addDomainToName(user.getUserName(), user.getUserStoreDomain());
            if (!userStoreManager.isExistingUser(domainQualifiedUsername)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("No user found for recovery with username: " + user.toFullQualifiedUsername());
                }
                return false;
            }
            return true;
        } catch (UserStoreException e) {
            throw Utils.handleServerException(IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_UNEXPECTED, null, e);
        }
    }

    /**
     * To check whether valid type user store is exist.
     *
     * @param userStoreDomain User store domain name.
     * @return True if valid type user store exist for given domain id.
     * @throws UserStoreException If an error occurred while getting the user store manager.
     */
    private boolean isValidUserStoreExists(String userStoreDomain) throws IdentityRecoveryException {

        UserStoreManager userStoreManager;
        try {
            userStoreManager = ((AbstractUserStoreManager) CarbonContext.getThreadLocalCarbonContext().getUserRealm()
                    .getUserStoreManager()).getSecondaryUserStoreManager(userStoreDomain);
        } catch (UserStoreException e) {
            throw Utils.handleServerException(IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_UNEXPECTED, null, e);
        }
        if (userStoreManager == null) {
            return false;
        }
        return true;
    }
}
