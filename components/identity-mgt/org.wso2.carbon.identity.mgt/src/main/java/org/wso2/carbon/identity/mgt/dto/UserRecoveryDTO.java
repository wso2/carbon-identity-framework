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

import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * This object contains the information of the created user account. This
 * information can be sent to the user to complete the user registration
 * process. Information are such as the temporary password, confirmation code
 * etc
 *
 * @author sga
 */
public class UserRecoveryDTO {


    private String userId;
    private String tenantDomain;
    private int tenantId;
    private String temporaryPassword;
    private String confirmationCode;
    private String notificationType;
    private String notification;

    public UserRecoveryDTO(UserDTO userDTO) {
        this.userId = userDTO.getUserId();
        this.tenantDomain = userDTO.getTenantDomain();
        this.tenantId = userDTO.getTenantId();
    }

    public UserRecoveryDTO(String userId) {
        this.userId = userId;
        this.tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        this.tenantId = MultitenantConstants.SUPER_TENANT_ID;
    }


    /**
     * Returns the temporary password of the created account
     *
     * @return
     */
    public String getTemporaryPassword() {
        return temporaryPassword;
    }

    public UserRecoveryDTO setTemporaryPassword(String temporaryPassword) {
        this.temporaryPassword = temporaryPassword;
        return this;
    }

    /**
     * Returns the confirmation code for the created account
     *
     * @return
     */
    public String getConfirmationCode() {
        return confirmationCode;
    }

    public UserRecoveryDTO setConfirmationCode(String confirmationCode) {
        this.confirmationCode = confirmationCode;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getNotification() {
        return notification;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }
}
