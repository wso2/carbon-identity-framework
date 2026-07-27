/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.common.model;

import java.util.Objects;

/**
 * ResolvedUser is the class that represents a user whose unique user ID has been resolved.
 * This extends the basic User class by adding the user ID information.
 */
public class ResolvedUser extends User {

    private static final long serialVersionUID = -4835843966924064267L;

    private String userId = null;

    /**
     * Instantiates a ResolvedUser.
     */
    public ResolvedUser() {

        super();
    }

    /**
     * Creates a ResolvedUser instance from a User object.
     *
     * @param user The User object to create the ResolvedUser from.
     */
    public ResolvedUser(User user) {

        this.setUserName(user.getUserName());
        this.setUserStoreDomain(user.getUserStoreDomain());
        this.setTenantDomain(user.getTenantDomain());
    }

    /**
     * Returns the user ID.
     *
     * @return The user ID.
     */
    public String getUserId() {

        return userId;
    }

    /**
     * Sets the user ID.
     *
     * @param userId The user ID to set.
     */
    public void setUserId(String userId) {

        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (!(o instanceof ResolvedUser)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        ResolvedUser resolvedUser = (ResolvedUser) o;
        return Objects.equals(this.userId, resolvedUser.userId);
    }

    @Override
    public int hashCode() {

        int result = super.hashCode();
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        return result;
    }
}
