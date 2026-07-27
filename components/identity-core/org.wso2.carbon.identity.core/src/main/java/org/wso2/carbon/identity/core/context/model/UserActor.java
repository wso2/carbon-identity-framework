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

package org.wso2.carbon.identity.core.context.model;

/**
 * Actor class for User.
 * This class holds the authenticated user actor details for a given flow.
 */
public class UserActor implements Actor {

    private final String userId;
    private final String username;

    public UserActor(Builder builder) {

        this.userId = builder.userId;
        this.username = builder.username;
    }

    public String getUserId() {

        return userId;
    }

    public String getUsername() {

        return username;
    }

    /**
     * Builder for the UserActor.
     */
    public static class Builder {

        private String userId;
        private String username;

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public UserActor build() {
            return new UserActor(this);
        }
    }
}
