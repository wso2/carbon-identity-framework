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

import com.fasterxml.jackson.annotation.JsonInclude;
import org.wso2.carbon.identity.action.execution.api.model.User;
import org.wso2.carbon.identity.action.execution.api.model.UserClaim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class models the user object sent to the Flow Extension action.
 * It extends the shared {@link User} with the username, user store domain, and credentials specific
 * to the flow extension contract. All fields are nullable and populated only when the corresponding
 * path is exposed. Per-property {@link JsonInclude} settings on the getters control how each field
 * is serialized in the outbound payload.
 */
public class FlowExtensionUser extends User {

    private final String username;
    private final String userStoreDomain;
    private final Map<String, Object> credentials;

    private FlowExtensionUser(Builder builder) {

        super(new User.Builder(builder.id)
                .claims(builder.claims));
        this.username = builder.username;
        this.userStoreDomain = builder.userStoreDomain;
        this.credentials = builder.credentials.isEmpty()
                ? null
                : new LinkedHashMap<>(builder.credentials);
    }

    /**
     * Get the user id.
     * Overrides {@link User#getId()} purely to relax the inclusion policy: an exposed-but-empty
     * user id must serialize as {@code ""} rather than be dropped by the mapper's {@code NON_EMPTY}
     * default. The value itself is still held by the shared {@link User} model.
     *
     * @return The user id, or {@code null} if not set.
     */
    @Override
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getId() {

        return super.getId();
    }

    /**
     * Get the username.
     *
     * @return The username, or {@code null} if not exposed.
     */
    public String getUsername() {

        return username;
    }

    /**
     * Get the user store domain.
     * NON_NULL overrides the ObjectMapper-level NON_EMPTY so that an exposed user store domain with
     * no value is serialized as {@code ""} (denoting the primary user store) rather than omitted.
     *
     * @return The user store domain, or {@code null} if not exposed.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getUserStoreDomain() {

        return userStoreDomain;
    }

    /**
     * Get the exposed user credentials, keyed by credential name (e.g. {@code password}). Each value
     * is already in its final wire form: when the path is {@code encrypted: true} it is the JWE
     * compact string of the typed credential object, otherwise it is the typed credential object
     * itself (e.g. {@code {"type": "PLAIN_TEXT", "value": "<secret>"}}). Omitted from the payload
     * when empty.
     *
     * @return The credentials map, or {@code null} if none are exposed.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public Map<String, Object> getCredentials() {

        return credentials == null ? null : Collections.unmodifiableMap(credentials);
    }

    /**
     * Builder for {@link FlowExtensionUser}.
     */
    public static class Builder {

        private final String id;
        private String username;
        private String userStoreDomain;
        private final List<UserClaim> claims = new ArrayList<>();
        private final Map<String, Object> credentials = new LinkedHashMap<>();

        /**
         * Create a builder for a user with the given id.
         *
         * @param id The user id (may be {@code null} when not exposed).
         */
        public Builder(String id) {

            this.id = id;
        }

        /**
         * Set the username.
         *
         * @param username The username.
         * @return This builder.
         */
        public Builder username(String username) {

            this.username = username;
            return this;
        }

        /**
         * Set the user store domain.
         *
         * @param userStoreDomain The user store domain ({@code ""} denotes the primary user store).
         * @return This builder.
         */
        public Builder userStoreDomain(String userStoreDomain) {

            this.userStoreDomain = userStoreDomain;
            return this;
        }

        /**
         * Add the given user claims to the ones already collected.
         *
         * @param claims The user claims to add.
         * @return This builder.
         */
        public Builder claims(List<? extends UserClaim> claims) {

            this.claims.addAll(claims);
            return this;
        }

        /**
         * Add the given credentials (keyed by credential name) to the ones already collected. Each
         * value is expected to be in its final wire form: the JWE compact string when encrypted, or
         * the typed credential object (e.g. {@code {"type": "PLAIN_TEXT", "value": "<secret>"}})
         * otherwise. A {@code null} map is ignored.
         *
         * @param credentials The credentials to add.
         * @return This builder.
         */
        public Builder credentials(Map<String, Object> credentials) {

            if (credentials != null) {
                this.credentials.putAll(credentials);
            }
            return this;
        }

        /**
         * Build the {@link FlowExtensionUser} from the values collected on this builder.
         *
         * @return A new {@link FlowExtensionUser} instance.
         */
        public FlowExtensionUser build() {

            return new FlowExtensionUser(this);
        }
    }
}
