/*
 *
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

package org.wso2.carbon.identity.mgt.beans;

import org.wso2.carbon.identity.mgt.dto.UserChallengesDTO;
import org.wso2.carbon.identity.mgt.dto.UserEvidenceDTO;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.Arrays;

/**
 * Bean that encapsulates user and tenant info
 */
public class UserIdentityMgtBean {

    /**
     * user identifier according to the user store
     */
    private String userId;

    /**
     * user's password
     */
    private String userPassword;

    /**
     * tenant domain of the user
     */
    private String tenantDomain;

    /**
     * email address of the user
     */
    private String email;

    /**
     * secret key which is assign to user
     */
    private String confirmationCode;

    /**
     * user key that is used to identify the user, other than the user id
     */
    private String userKey;

    /**
     * recovery type
     */
    private String recoveryType;

    /**
     * user challenges that must be answered by user
     */
    private UserChallengesDTO[] userChallenges = new UserChallengesDTO[0];

    /**
     * evidences about user to identify him uniquely
     */
    private UserEvidenceDTO[] userEvidenceDTOs = new UserEvidenceDTO[0];

    public String getUserId() {
        return userId;
    }

    public UserIdentityMgtBean setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getUserTemporaryPassword() {
        return userPassword;
    }

    public UserIdentityMgtBean setUserTemporaryPassword(Object userPassword) {
        this.userPassword = (String) userPassword;
        return this;
    }

    public String getEmail() {
        return email;
    }

    /**
     * @param email
     * @return
     */
    public UserIdentityMgtBean setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getTenantDomain() {
        if (tenantDomain == null) {
            tenantDomain = MultitenantUtils.getTenantDomain(userId);
        }
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    public UserChallengesDTO[] getUserChallenges() {
        return Arrays.copyOf(userChallenges, userChallenges.length);
    }

    public void setUserChallenges(UserChallengesDTO[] userChallenges) {
        this.userChallenges = Arrays.copyOf(userChallenges, userChallenges.length);
    }

    public String getRecoveryType() {
        return recoveryType;
    }

    /**
     * @param recoveryType
     * @return
     */
    public UserIdentityMgtBean setRecoveryType(String recoveryType) {
        this.recoveryType = recoveryType;
        return this;
    }

    public UserEvidenceDTO[] getUserEvidenceDTOs() {
        return Arrays.copyOf(userEvidenceDTOs, userEvidenceDTOs.length);
    }

    public void setUserEvidenceDTOs(UserEvidenceDTO[] userEvidenceDTOs) {
        this.userEvidenceDTOs = Arrays.copyOf(userEvidenceDTOs, userEvidenceDTOs.length);
    }

    public String getConfirmationCode() {
        return confirmationCode;
    }

    /**
     * @param confirmationCode
     * @return
     */
    public UserIdentityMgtBean setConfirmationCode(String confirmationCode) {
        this.confirmationCode = confirmationCode;
        return this;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }
}
