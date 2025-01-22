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
 * Entity class for Application.
 * This class holds the application entity details for a given flow.
 */
public class ApplicationEntity implements InitiatingEntity {

    private final String applicationId;
    private final String applicationName;
    private final String clientId;
    private final String issuer;
    private final String realm;

    private ApplicationEntity(Builder builder) {

        this.applicationId = builder.applicationId;
        this.applicationName = builder.applicationName;
        this.clientId = builder.clientId;
        this.issuer = builder.issuer;
        this.realm = builder.realm;
    }

    public String getApplicationId() {

        return applicationId;
    }

    public String getApplicationName() {

        return applicationName;
    }

    public String getClientId() {

        return clientId;
    }

    public String getIssuer() {

        return issuer;
    }

    public String getRealm() {

        return realm;
    }

    /**
     * Builder for the ApplicationEntity.
     */
    public static class Builder {

        private String applicationId;
        private String applicationName;
        private String clientId;
        private String issuer;
        private String realm;

        public Builder() {

        }

        public Builder(ApplicationEntity applicationEntity) {

            this.applicationId = applicationEntity.applicationId;
            this.applicationName = applicationEntity.applicationName;
            this.clientId = applicationEntity.clientId;
            this.issuer = applicationEntity.issuer;
            this.realm = applicationEntity.realm;
        }

        public Builder applicationId(String applicationId) {

            this.applicationId = applicationId;
            return this;
        }

        public Builder applicationName(String applicationName) {

            this.applicationName = applicationName;
            return this;
        }

        public Builder clientId(String clientId) {

            if (issuer != null && realm != null) {
                throw new IllegalStateException("Cannot set both clientId and issuer/realm at the same time.");
            }
            this.clientId = clientId;
            return this;
        }

        public Builder issuer(String issuer) {

            if (clientId != null && realm != null) {
                throw new IllegalStateException("Cannot set both issuer and clientId/realm at the same time.");
            }
            this.issuer = issuer;
            return this;
        }

        public Builder realm(String realm) {

            if (clientId != null && issuer != null) {
                throw new IllegalStateException("Cannot set both realm and clientId/issuer at the same time.");
            }
            this.realm = realm;
            return this;
        }

        public ApplicationEntity build() {

            return new ApplicationEntity(this);
        }
    }

}
