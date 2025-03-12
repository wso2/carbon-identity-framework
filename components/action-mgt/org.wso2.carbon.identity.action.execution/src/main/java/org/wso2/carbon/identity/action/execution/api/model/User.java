/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.action.execution.api.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class models the User.
 * User is the entity that represents the user for whom the action is triggered for.
 */
public class User {

    private final String id;
    private final List<UserClaim> claims =  new ArrayList<>();
    private final List<String> groups = new ArrayList<>();
    private final List<String> roles = new ArrayList<>();

    public User(String id) {

        this.id = id;
    }

    public User(Builder builder) {

        this.id = builder.id;
        this.claims.addAll(builder.claims);
        this.groups.addAll(builder.groups);
        this.roles.addAll(builder.roles);
    }

    public String getId() {

        return id;
    }

    public List<UserClaim> getClaims() {

        return Collections.unmodifiableList(claims);
    }

    public List<String> getGroups() {

        return Collections.unmodifiableList(groups);
    }

    public List<String> getRoles() {

        return Collections.unmodifiableList(roles);
    }

    /**
     * Builder for the User.
     */
    public static class Builder {

        private final String id;
        private final List<UserClaim> claims = new ArrayList<>();
        private final List<String> groups = new ArrayList<>();
        private final List<String> roles = new ArrayList<>();

        public Builder(String id) {

            this.id = id;
        }

        public Builder claims(List<? extends UserClaim> claims) {

            this.claims.addAll(claims);
            return this;
        }

        public Builder groups(List<String> groups) {

            this.groups.addAll(groups);
            return this;
        }

        public Builder roles(List<String> roles) {

            this.roles.addAll(roles);
            return this;
        }

        public User build() {

            return new User(this);
        }
    }
}
