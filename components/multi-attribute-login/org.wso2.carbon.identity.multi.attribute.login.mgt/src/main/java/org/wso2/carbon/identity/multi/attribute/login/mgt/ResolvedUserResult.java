/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.multi.attribute.login.mgt;

import org.wso2.carbon.user.core.common.User;

/**
 * Represents a resolved user result after a resolving username using multi attribute login identifier.
 */
public class ResolvedUserResult {

    private UserResolvedStatus resolvedStatus;
    private String resolvedClaim = null;
    private String resolvedValue = null;
    private User user;
    private String errorMessage; // TODO: add error code

    public ResolvedUserResult(UserResolvedStatus resolvedStatus) {

        this.resolvedStatus = resolvedStatus;
    }

    public UserResolvedStatus getResolvedStatus() {

        return resolvedStatus;
    }

    public void setResolvedStatus(UserResolvedStatus resolvedStatus) {

        this.resolvedStatus = resolvedStatus;
    }

    public String getResolvedClaim() {

        return resolvedClaim;
    }

    public void setResolvedClaim(String resolvedClaim) {

        this.resolvedClaim = resolvedClaim;
    }

    public String getResolvedValue() {

        return resolvedValue;
    }

    public void setResolvedValue(String resolvedValue) {

        this.resolvedValue = resolvedValue;
    }

    public User getUser() {

        return user;
    }

    public void setUser(User user) {

        this.user = user;
    }

    public String getErrorMessage() {

        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {

        this.errorMessage = errorMessage;
    }

    /**
     * This enum is used to keep status of whether the user has resolved or not.
     */
    public enum UserResolvedStatus {
        SUCCESS, FAIL
    }
}
