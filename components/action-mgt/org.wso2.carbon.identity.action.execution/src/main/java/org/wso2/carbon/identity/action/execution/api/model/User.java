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
    private Organization organization;
    /**
     * Represents the user id when the user is shared across sub-organizations.
     * This field differs from the regular user id ({@link #id}) in scenarios where a user is accessed in the context
     * of a sub-organization, and the shared user id is used to identify the user in the shared organization.
     * If the user is not shared, this value will be null.
     */
    private String sharedUserId;
    /**
     * Represents the type of the user whether the user is 'local', 'federated' or 'shared'.
     */
    private String userType;
    /**
     * Represents the federated identity provider if the user is a federated user.
     * If the user is not federated, this value will be null.
     */
    private String federatedIdP;
    /**
     * Represents the organization which user is accessing.
     * Applicable in the Organization switch grant, in this context accessing organization is the organization
     * which the user is switching to.
     */
    private Organization accessingOrganization;

    public User(String id) {

        this.id = id;
    }

    public User(Builder builder) {

        this.id = builder.id;
        this.claims.addAll(builder.claims);
        this.groups.addAll(builder.groups);
        this.roles.addAll(builder.roles);
        this.organization = builder.organization;
        this.sharedUserId = builder.sharedUserId;
        this.userType = builder.userType;
        this.federatedIdP = builder.federatedIdP;
        this.accessingOrganization = builder.accessingOrganization;
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

    public Organization getOrganization() {

        return organization;
    }

    public String getSharedUserId() {

        return sharedUserId;
    }

    public String getUserType() {

        return userType;
    }

    public String getFederatedIdP() {

        return federatedIdP;
    }

    public Organization getAccessingOrganization() {

        return accessingOrganization;
    }

    /**
     * Builder for the User.
     */
    public static class Builder {

        private final String id;
        private final List<UserClaim> claims = new ArrayList<>();
        private final List<String> groups = new ArrayList<>();
        private final List<String> roles = new ArrayList<>();
        private Organization organization;
        private String sharedUserId;
        private String userType;
        private String federatedIdP;
        private Organization accessingOrganization;

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

        public Builder organization(Organization organization) {

            this.organization = organization;
            return this;
        }

        public Builder sharedUserId(String sharedUserId) {

            this.sharedUserId = sharedUserId;
            return this;
        }

        public Builder userType(String userType) {

            this.userType = userType;
            return this;
        }

        public Builder federatedIdP(String federatedIdP) {

            this.federatedIdP = federatedIdP;
            return this;
        }

        public Builder accessingOrganization(Organization accessingOrganization) {

            this.accessingOrganization = accessingOrganization;
            return this;
        }

        public User build() {

            return new User(this);
        }
    }
}
