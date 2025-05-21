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

package org.wso2.carbon.identity.user.action.api.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the request object for user related operations to be consumed by user actions.
 */
public class UserActionRequestDTO {

    private final String userId;
    private final char[] password;
    private final List<String> roles;
    private final List<String> groups;
    private final Map<String, Object> claims;
    private final String userStoreDomain;

    private UserActionRequestDTO(Builder builder) {

        this.userId = builder.userId;
        this.password = builder.password;
        this.userStoreDomain = builder.userStoreDomain;
        this.claims = builder.claims;
        this.roles = builder.roles;
        this.groups = builder.groups;
    }

    public String getUserId() {

        return userId;
    }

    public char[] getPassword() {

        return password;
    }

    public List<String> getRoles() {

        return Collections.unmodifiableList(roles);
    }

    public List<String> getGroups() {

        return Collections.unmodifiableList(groups);
    }

    public Map<String, Object> getClaims() {

        return Collections.unmodifiableMap(claims);
    }

    public String getUserStoreDomain() {

        return userStoreDomain;
    }

    /**
     * Builder for the UserActionRequestDTO.
     */
    public static class Builder {

        private String userId;
        private char[] password;
        private final List<String> roles = new ArrayList<>();
        private final List<String> groups = new ArrayList<>();
        private final Map<String, Object> claims = new HashMap<>();
        private String userStoreDomain;

        public Builder userId(String userId) {

            this.userId = userId;
            return this;
        }

        public Builder password(char[] password) {

            this.password = password;
            return this;
        }

        public Builder userStoreDomain(String userStoreDomain) {

            this.userStoreDomain = userStoreDomain;
            return this;
        }

        public Builder addRole(String role) {

            this.roles.add(role);
            return this;
        }

        public Builder addGroup(String group) {

            this.groups.add(group);
            return this;
        }

        public Builder addClaim(String claimURI, String claimValue) {

            this.claims.put(claimURI, claimValue);
            return this;
        }

        public Builder addClaim(String claimURI, String[] claimValue) {

            this.claims.put(claimURI, claimValue);
            return this;
        }

        public UserActionRequestDTO build() {

            return new UserActionRequestDTO(this);
        }
    }
}
