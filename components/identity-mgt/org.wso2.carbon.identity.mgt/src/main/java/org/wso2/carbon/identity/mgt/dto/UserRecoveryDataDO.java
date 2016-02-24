/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.carbon.identity.mgt.IdentityMgtConfig;

/**
 * This object represents an entry of the identity metadata database.
 */
public class UserRecoveryDataDO {

    public static final String METADATA_TEMPORARY_CREDENTIAL = "TEMPORARY_CREDENTIAL";
    public static final String METADATA_CONFIRMATION_CODE = "CONFIRMATION_CODE";
    public static final String METADATA_PRIMARAY_SECURITY_QUESTION = "PRIMARAY_SEC_QUESTION";

    private String userName;
    private int tenantId;
    private String code;
    private String secret;
    private String expireTime;
    private boolean isValid;


    public UserRecoveryDataDO() {
    }

    public UserRecoveryDataDO(String userName, int tenantId) {
        this.tenantId = tenantId;
        this.userName = userName;
        int expireTimeInMinutes = IdentityMgtConfig.getInstance().getNotificationExpireTime();
        this.expireTime = Long.toString(System.currentTimeMillis() + (expireTimeInMinutes * 60 * 1000L));
        this.isValid = true;
    }

    public UserRecoveryDataDO(String userName, int tenantId, String code, String secret) {
        this.userName = userName;
        this.tenantId = tenantId;
        this.code = code;
        this.secret = secret;
        int expireTimeInMinutes = IdentityMgtConfig.getInstance().getNotificationExpireTime();
        this.expireTime = Long.toString(System.currentTimeMillis() + (expireTimeInMinutes * 60 * 1000L));
        this.isValid = true;
    }


    /**
     * @return the userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param userName the userName to set
     * @return
     */
    public UserRecoveryDataDO setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    /**
     * @return the tenantId
     */
    public int getTenantId() {
        return tenantId;
    }

    /**
     * @param tenantId the tenantId to set
     * @return
     */
    public UserRecoveryDataDO setTenantId(int tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    /**
     * @return the isValid
     */
    public boolean isValid() {
        return isValid;
    }

    /**
     * @param isValid the isValid to set
     */
    public void setValid(boolean isValid) {
        this.isValid = isValid;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(String expireTime) {
        this.expireTime = expireTime;
    }
}
