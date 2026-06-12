/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.flow.extension.model;

import org.wso2.carbon.identity.action.execution.api.model.User;
import org.wso2.carbon.identity.action.execution.api.model.UserClaim;

import java.util.ArrayList;
import java.util.List;

/**
 * Models the user object sent to an In-Flow Extension action.
 *
 * <p>Extends the shared {@link User} with the identity leaves specific to the flow extension
 * contract ({@code /user/username} and {@code /user/userStoreDomain}). These are kept out of
 * the shared {@link User} model so they are not advertised to action types that have no notion
 * of them. This mirrors the existing pattern used by {@code PasswordUpdatingUser}.</p>
 *
 * <p>Both fields are nullable: they are only populated when the corresponding path is exposed.
 * The request mapper serializes with {@code NON_NULL}/{@code NON_EMPTY}, so an unset field is
 * omitted from the outbound payload.</p>
 */
public class FlowExtensionUser extends User {

    private final String username;
    private final String userStoreDomain;

    private FlowExtensionUser(Builder builder) {

        super(new User.Builder(builder.id)
                .claims(builder.claims));
        this.username = builder.username;
        this.userStoreDomain = builder.userStoreDomain;
    }

    public String getUsername() {

        return username;
    }

    public String getUserStoreDomain() {

        return userStoreDomain;
    }

    /**
     * Builder for {@link FlowExtensionUser}.
     */
    public static class Builder {

        private final String id;
        private String username;
        private String userStoreDomain;
        private final List<UserClaim> claims = new ArrayList<>();

        public Builder(String id) {

            this.id = id;
        }

        public Builder username(String username) {

            this.username = username;
            return this;
        }

        public Builder userStoreDomain(String userStoreDomain) {

            this.userStoreDomain = userStoreDomain;
            return this;
        }

        public Builder claims(List<? extends UserClaim> claims) {

            this.claims.addAll(claims);
            return this;
        }

        public FlowExtensionUser build() {

            return new FlowExtensionUser(this);
        }
    }
}
