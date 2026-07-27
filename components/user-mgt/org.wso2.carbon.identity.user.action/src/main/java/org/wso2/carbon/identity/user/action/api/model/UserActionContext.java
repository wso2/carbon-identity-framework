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

/**
 * Model class for User Action Context.
 * This class holds the user action context details for a given user flow.
 */
public class UserActionContext {

    public static final String USER_ACTION_CONTEXT_REFERENCE_KEY = "USER_ACTION_CONTEXT";

    private String userId;
    private char[] password;
    private String userStoreDomain;
    private UserActionRequestDTO userActionRequestDTO;
    private UserActionResponseDTO userActionResponseDTO;

    public UserActionContext(UserActionRequestDTO userActionRequestDTO) {

        this.userActionRequestDTO = userActionRequestDTO;
        this.userActionResponseDTO = new UserActionResponseDTO(userActionRequestDTO);
    }

    public UserActionRequestDTO getUserActionRequestDTO() {

        return userActionRequestDTO;
    }

    public UserActionResponseDTO getUserActionResponseDTO() {

        return userActionResponseDTO;
    }

    /**
     * This constructor is deprecated and will be removed in future versions.
     * Use {@link #UserActionContext(UserActionRequestDTO)} instead.
     */
    @Deprecated
    private UserActionContext(Builder builder) {

        this.userId = builder.userId;
        this.password = builder.password;
        this.userStoreDomain = builder.userStoreDomain;
    }

    /**
     * This method is deprecated and will be removed in future versions.
     * Use {@link UserActionRequestDTO#getUserId()} instead.
     */
    @Deprecated
    public String getUserId() {

        return userId;
    }

    /**
     * This method is deprecated and will be removed in future versions.
     * Use {@link UserActionRequestDTO#getPassword()} instead.
     */
    @Deprecated
    public char[] getPassword() {

        return password;
    }

    /**
     * This method is deprecated and will be removed in future versions.
     * Use {@link UserActionRequestDTO#getUserStoreDomain()} instead.
     */
    public String getUserStoreDomain() {

        return userStoreDomain;
    }

    /**
     * Builder for the UserActionContext.
     * This class is deprecated and will be removed in future versions.
     * Use {@link #UserActionContext(UserActionRequestDTO)} instead.
     */
    @Deprecated
    public static class Builder {

        private String userId;
        private char[] password;
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

        public UserActionContext build() {

            return new UserActionContext(this);
        }
    }
}
