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

package org.wso2.carbon.identity.mgt.dto;

import org.wso2.carbon.identity.mgt.store.UserIdentityDataStore;

import java.util.HashMap;
import java.util.Map;

/**
 * This encapsulates the user's data that is related user's login information
 */
public class UserIdentityDTO {

    public static final int TRUE = 1;
    public static final int FALSE = 2;
    private String userName;
    private long unlockTime;
    private long lastLogonTime;
    private long lastFailAttemptTime;
    private int failAttempts;
    private boolean accountLock;
    private boolean temporaryLock;
    private boolean passwordChangeRequired;
    private boolean oneTimeLogin;
    private Map<String, String> userDataMap = new HashMap<String, String>();
    private int tenantId;
    private Map<String, String> securityQuestions = new HashMap<String, String>();


    public UserIdentityDTO(String userName) {
        this.userName = userName;
    }

    public UserIdentityDTO(String userName, Map<String, String> userDataMap) {

        this.userName = userName;
        this.userDataMap = userDataMap;

        if (userDataMap.get(UserIdentityDataStore.FAIL_LOGIN_ATTEMPTS) != null) {
            setFailAttempts(Integer.
                    parseInt(userDataMap.get(UserIdentityDataStore.FAIL_LOGIN_ATTEMPTS)));
        }
        if (userDataMap.get(UserIdentityDataStore.LAST_FAILED_LOGIN_ATTEMPT_TIME) != null) {
            setLastFailAttemptTime(Long.
                    parseLong(userDataMap.get(UserIdentityDataStore.LAST_FAILED_LOGIN_ATTEMPT_TIME)));
        }
        if (userDataMap.get(UserIdentityDataStore.TEMPORARY_LOCK) != null) {
            setTemporaryLock(Boolean.
                    parseBoolean(userDataMap.get(UserIdentityDataStore.TEMPORARY_LOCK)));
        }
        if (userDataMap.get(UserIdentityDataStore.UNLOCKING_TIME) != null) {
            setUnlockTime(Long.parseLong(userDataMap.get(UserIdentityDataStore.UNLOCKING_TIME)));
        }
        if (userDataMap.get(UserIdentityDataStore.PASSWORD_CHANGE_REQUIRED) != null) {
            setPasswordChangeRequired(Boolean.
                    parseBoolean(userDataMap.get(UserIdentityDataStore.PASSWORD_CHANGE_REQUIRED)));
        }
        if (userDataMap.get(UserIdentityDataStore.LAST_LOGON_TIME) != null) {
            setLastLogonTime(Long.
                    parseLong(userDataMap.get(UserIdentityDataStore.LAST_LOGON_TIME)));
        }
        if (userDataMap.get(UserIdentityDataStore.ACCOUNT_LOCK) != null) {
            setAccountLock(Boolean.
                    parseBoolean(userDataMap.get(UserIdentityDataStore.ACCOUNT_LOCK)));
        }
    }

    public void setSecurityQuestion(String questionURI, String answer) {
        securityQuestions.put(questionURI, answer);
    }

    public Map<String, String> getSecurityQuestions() {
        return securityQuestions;
    }

    public void setSecurityQuestions(Map<String, String> securityQuestions) {
        this.securityQuestions = securityQuestions;
    }

    public String getUserName() {
        return userName;
    }

    public long getUnlockTime() {
        return unlockTime;
    }

    public void setUnlockTime(long unlockTime) {
        this.unlockTime = unlockTime;
        this.userDataMap.put(UserIdentityDataStore.UNLOCKING_TIME, Long.toString(unlockTime));
    }

    public long getLastLogonTime() {
        return lastLogonTime;
    }

    public void setLastLogonTime(long lastLogonTime) {
        this.lastLogonTime = lastLogonTime;
        this.userDataMap.put(UserIdentityDataStore.LAST_LOGON_TIME, Long.toString(lastLogonTime));
    }

    public long getLastFailAttemptTime() {
        return lastFailAttemptTime;
    }

    public void setLastFailAttemptTime(long lastFailAttemptTime) {
        this.lastFailAttemptTime = lastFailAttemptTime;
        this.userDataMap.put(UserIdentityDataStore.LAST_FAILED_LOGIN_ATTEMPT_TIME, Long.toString(lastFailAttemptTime));
    }

    public int getFailAttempts() {
        return failAttempts;
    }

    public void setFailAttempts(int failAttempts) {
        this.failAttempts = failAttempts;
        this.userDataMap.put(UserIdentityDataStore.FAIL_LOGIN_ATTEMPTS, Integer.toString(failAttempts));
    }

    public void setFailAttempts() {
        this.failAttempts++;
        this.userDataMap.put(UserIdentityDataStore.FAIL_LOGIN_ATTEMPTS, Integer.toString(failAttempts));
    }

    public boolean getOneTimeLogin() {
        return oneTimeLogin;
    }

    public void setOneTimeLogin(boolean oneTimeLogin) {
        this.oneTimeLogin = oneTimeLogin;
        this.userDataMap.put(UserIdentityDataStore.LAST_LOGON_TIME, Boolean.toString(oneTimeLogin));
    }

    public boolean getPasswordChangeRequired() {
        return passwordChangeRequired;
    }

    public void setPasswordChangeRequired(boolean passwordChangeRequired) {
        this.passwordChangeRequired = passwordChangeRequired;
        this.userDataMap.put(UserIdentityDataStore.PASSWORD_CHANGE_REQUIRED, Boolean.toString(passwordChangeRequired));
    }

    public boolean getTemporaryLock() {
        return temporaryLock;
    }

    public void setTemporaryLock(boolean temporaryLock) {
        this.temporaryLock = temporaryLock;
        this.userDataMap.put(UserIdentityDataStore.TEMPORARY_LOCK, Boolean.toString(temporaryLock));
    }

    public boolean isAccountLocked() {
        if (unlockTime != 0 && unlockTime < System.currentTimeMillis()) {
            return false;
        }
        return accountLock;
    }

    public void setAccountLock(boolean accountLock) {
        this.accountLock = accountLock;
        this.userDataMap.put(UserIdentityDataStore.ACCOUNT_LOCK, Boolean.toString(accountLock));
    }

    public Map<String, String> getUserDataMap() {
        return userDataMap;
    }

    public void setUserDataMap(Map<String, String> userDataMap) {
        this.userDataMap = userDataMap;
    }

    /**
     * Sets user identity data claim
     *
     * @param claim
     * @param value
     */
    public void setUserIdentityDataClaim(String claim, String value) {
        userDataMap.put(claim, value);
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public boolean getBoolean(Object value) {
        int IntegerValue = (Integer) value;
        return IntegerValue == TRUE;
    }
}
