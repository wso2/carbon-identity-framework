/*
 * Copyright (c) 2010 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.user.mgt.common;


public class UserStoreInfo {

    private boolean isReadOnly = true;

    private boolean readGroupsEnabled = true;

    private boolean writeGroupsEnabled = true;

    private boolean isPasswordsExternallyManaged = false;

    private String userNameRegEx;

    private String roleNameRegEx;

    private String passwordRegEx;

    private String passwordRegExViolationErrorMsg;

    private String usernameRegExViolationErrorMsg;

    private boolean isBulkImportSupported;
    
    private String externalIdP;

    private String domainName;

    private int maxRoleLimit;

    private int maxUserLimit;

    private boolean caseSensitiveUsername;

    public boolean isBulkImportSupported() {
        return isBulkImportSupported;
    }

    public void setBulkImportSupported(boolean bulkImportSupported) {
        isBulkImportSupported = bulkImportSupported;
    }

    public boolean isPasswordsExternallyManaged() {
        return isPasswordsExternallyManaged;
    }

    public void setPasswordsExternallyManaged(boolean passwordsExternallyManaged) {
        isPasswordsExternallyManaged = passwordsExternallyManaged;
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

    public boolean isReadGroupsEnabled() {
        return readGroupsEnabled;
    }

    public boolean isWriteGroupsEnabled() {
        return writeGroupsEnabled;
    }

    public void setReadOnly(boolean isReadOnly) {
        this.isReadOnly = isReadOnly;
    }

    public void setReadGroupsEnabled(boolean readGroupsEnabled) {
        this.readGroupsEnabled = readGroupsEnabled;
    }

    public void setWriteGroupsEnabled(boolean writeGroupsEnabled) {
        this.writeGroupsEnabled = writeGroupsEnabled;
    }

    public String getUserNameRegEx() {
        return userNameRegEx;
    }

    public void setUserNameRegEx(String userNameRegEx) {
        this.userNameRegEx = userNameRegEx;
    }

    public String getRoleNameRegEx() {
        return roleNameRegEx;
    }

    public void setRoleNameRegEx(String roleNameRegEx) {
        this.roleNameRegEx = roleNameRegEx;
    }

    public String getExternalIdP() {
        return externalIdP;
    }

    public void setExternalIdP(String externalIdP) {
        this.externalIdP = externalIdP;
    }

    public String getPasswordRegEx() {
        return passwordRegEx;
    }

    public void setPasswordRegEx(String passwordRegEx) {
        this.passwordRegEx = passwordRegEx;
    }

    public int getMaxRoleLimit() {
        return maxRoleLimit;
    }

    public void setMaxRoleLimit(int maxRoleLimit) {
        this.maxRoleLimit = maxRoleLimit;
    }

    public int getMaxUserLimit() {
        return maxUserLimit;
    }

    public void setMaxUserLimit(int maxUserLimit) {
        this.maxUserLimit = maxUserLimit;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getPasswordRegExViolationErrorMsg() {
        return passwordRegExViolationErrorMsg;
    }

    public void setPasswordRegExViolationErrorMsg(String passwordRegExViolationErrorMsg) {
        this.passwordRegExViolationErrorMsg = passwordRegExViolationErrorMsg;
    }

    public String getUsernameRegExViolationErrorMsg() {
        return usernameRegExViolationErrorMsg;
    }

    public void setUsernameRegExViolationErrorMsg(String usernameRegExViolationErrorMsg) {
        this.usernameRegExViolationErrorMsg = usernameRegExViolationErrorMsg;
    }

    public boolean isCaseSensitiveUsername() {
        return caseSensitiveUsername;
    }

    public void setCaseSensitiveUsername(boolean caseSensitiveUsername) {
        this.caseSensitiveUsername = caseSensitiveUsername;
    }
}
