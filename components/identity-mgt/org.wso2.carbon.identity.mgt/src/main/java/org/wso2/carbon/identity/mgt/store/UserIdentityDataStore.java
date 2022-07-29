/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
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

package org.wso2.carbon.identity.mgt.store;

import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.mgt.dto.UserIdentityClaimsDO;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserCoreConstants;

/**
 * This interface provides to plug module for preferred persistence store.
 * @deprecated use {@link org.wso2.carbon.identity.governance.store.UserIdentityDataStore} instead.
 */
@Deprecated
public abstract class UserIdentityDataStore {

    public static final String ONE_TIME_PASSWORD = "http://wso2.org/claims/identity/otp";
    public static final String PASSWORD_CHANGE_REQUIRED = "http://wso2.org/claims/identity/passwordChangeRequired";
    public static final String TEMPORARY_LOCK = "http://wso2.org/claims/identity/temporaryLock";
    public static final String LAST_FAILED_LOGIN_ATTEMPT_TIME = "http://wso2.org/claims/identity/lastFailedLoginAttemptTime";
    public static final String FAIL_LOGIN_ATTEMPTS = "http://wso2.org/claims/identity/failedLoginAttempts";
    public static final String LAST_LOGON_TIME = "http://wso2.org/claims/identity/lastLogonTime";
    public static final String UNLOCKING_TIME = "http://wso2.org/claims/identity/unlockTime";
    public static final String ACCOUNT_LOCK = "http://wso2.org/claims/identity/accountLocked";
    public static final String ACCOUNT_DISABLED = "http://wso2.org/claims/identity/accountDisabled";
    public static final String ACCOUNT_LOCKED_REASON = "http://wso2.org/claims/identity/lockedReason";

    /**
     * Get all claim types that is need to persist in the store
     *
     * @return
     */
    public String[] getUserIdentityDataClaims() throws IdentityException {

        return new String[]{ONE_TIME_PASSWORD, PASSWORD_CHANGE_REQUIRED, TEMPORARY_LOCK,
                LAST_FAILED_LOGIN_ATTEMPT_TIME, FAIL_LOGIN_ATTEMPTS, LAST_LOGON_TIME,
                UNLOCKING_TIME, ACCOUNT_LOCK, ACCOUNT_DISABLED, UserCoreConstants.ClaimTypeURIs.CHALLENGE_QUESTION_URI};

    }

    /**
     * Stores data
     *
     * @param userIdentityDTO
     * @param userStoreManager
     */
    public abstract void store(UserIdentityClaimsDO userIdentityDTO, UserStoreManager userStoreManager)
            throws IdentityException;

    /**
     * Loads
     *
     * @param userName
     * @param userStoreManager
     * @return
     */
    public abstract UserIdentityClaimsDO load(String userName, UserStoreManager userStoreManager);


    /**
     * Removes
     *
     * @param userName
     * @param userStoreManager
     */
    public abstract void remove(String userName, UserStoreManager userStoreManager) throws IdentityException;
}
