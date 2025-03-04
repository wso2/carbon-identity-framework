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

package org.wso2.carbon.identity.user.pre.update.profile.action.api.model;

import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionRequest;
import org.wso2.carbon.identity.action.execution.api.model.User;

import java.security.cert.X509Certificate;
import java.util.List;

/**
 * This class models the user object at a pre update profile trigger.
 * UserRequest is the entity that represents the user object that is sent to Action
 * over {@link ActionExecutionRequest}.
 */
public class ProfileUpdatingUser extends User {

    private final List<String> groups;
    private final List<String> roles;
    private final List<ProfileUserClaim> claims;

    private ProfileUpdatingUser(Builder builder) {

        super(builder.id);
        this.groups = builder.groups;
        this.roles = builder.roles;
        this.claims = builder.claims;
    }

    public static class Builder {

        private String id;
        private List<String> groups;
        private List<String> roles;
        private List<ProfileUserClaim> claims;

        public Builder id(String id) {

            this.id = id;
            return this;
        }

        public Builder claims(List<ProfileUserClaim> claims) {

            this.claims = claims;
            return this;
        }

        public Builder groups(List<String> groups) {

            this.groups = groups;
            return this;
        }

        public Builder roles(List<String> roles) {

            this.roles = roles;
            return this;
        }

        public ProfileUpdatingUser build() {

            return new ProfileUpdatingUser(this);
        }
    }
}
