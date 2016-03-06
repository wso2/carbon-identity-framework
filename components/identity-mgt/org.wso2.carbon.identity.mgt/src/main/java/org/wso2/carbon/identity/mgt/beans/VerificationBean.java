/*
 *
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
package org.wso2.carbon.identity.mgt.beans;

import org.wso2.carbon.identity.mgt.dto.NotificationDataDTO;

import java.io.Serializable;

/**
 * Bean that encapsulates the verification info.
 */
public class VerificationBean implements Serializable {

    private static final long serialVersionUID = -2913500119053797062L;

    public static final String ERROR_CODE_INVALID_CODE = "18001";
    public static final String ERROR_CODE_EXPIRED_CODE = "18002";
    public static final String ERROR_CODE_INVALID_USER = "18003";
    public static final String ERROR_CODE_INVALID_CAPTCHA = "18004";
    public static final String ERROR_CODE_UNEXPECTED = "18013";
    public static final String ERROR_CODE_LOADING_DATA_FAILURE = "18014";
    public static final String ERROR_CODE_RECOVERY_NOTIFICATION_FAILURE = "18015";
    public static final String ERROR_CODE_INVALID_TENANT = "18016";
    public static final String ERROR_CODE_CHALLENGE_QUESTION_NOT_FOUND = "18016";
    public static final String ERROR_CODE_INVALID_CREDENTIALS = "17002";
    public static final String ERROR_CODE_DISABLED_ACCOUNT = "17003";

    /**
     * user identifier according to the user store
     */
    private String userId;

    /**
     * key that is received after successful verification
     */
    private String key;

    /**
     * Error that is received after unsuccessful verification
     */
    private String error;

    /**
     * whether verification successful or unsuccessful
     */
    private boolean verified;

    private NotificationDataDTO notificationData;

    /**
     * redirected path or next step after verification is successful
     */
    private String redirectPath;

    public VerificationBean() {
    }

    public VerificationBean(boolean verified) {
        this.verified = verified;
    }

    public VerificationBean(String userId, String key) {
        this.key = key;
        this.userId = userId;
        this.verified = true;
    }

    public VerificationBean(String error) {
        this.error = error;
        this.verified = false;
    }

    public NotificationDataDTO getNotificationData() {
        return notificationData;
    }

    public void setNotificationData(NotificationDataDTO notificationData) {
        this.notificationData = notificationData;
    }

    public String getRedirectPath() {
        return redirectPath;
    }

    public void setRedirectPath(String redirectPath) {
        this.redirectPath = redirectPath;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }
}
