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
 * Actor class for Application.
 * This class holds the application actor details for a given flow.
 */
public class ApplicationActor implements Actor {

    /**
     * Enum for authentication types.
     * Specifies the type of authentication used by the application.
     */
    public enum AuthType {
        OAUTH2
    }

    private final String applicationId;
    private final String applicationName;
    private final AuthType authenticationType;
    private final String entityId;

    private ApplicationActor(Builder builder) {

        this.applicationId = builder.applicationId;
        this.applicationName = builder.applicationName;
        this.authenticationType = builder.authenticationType;
        this.entityId = builder.entityId;
    }

    public String getApplicationId() {

        return applicationId;
    }

    public String getApplicationName() {

        return applicationName;
    }

    /**
     * Builder for the ApplicationEntity.
     */
    public static class Builder {

        private String applicationId;
        private String applicationName;
        private AuthType authenticationType;
        private String entityId;

        public Builder() {

        }

        public Builder(ApplicationActor applicationActor) {

            this.applicationId = applicationActor.applicationId;
            this.applicationName = applicationActor.applicationName;
            this.authenticationType = applicationActor.authenticationType;
            this.entityId = applicationActor.entityId;
        }

        public Builder applicationId(String applicationId) {

            this.applicationId = applicationId;
            return this;
        }

        public Builder applicationName(String applicationName) {

            this.applicationName = applicationName;
            return this;
        }

        public Builder authenticationType(AuthType authenticationType) {

            this.authenticationType = authenticationType;
            return this;
        }

        public Builder entityId(String entityId) {

            this.entityId = entityId;
            return this;
        }

        public ApplicationActor build() {

            if (entityId != null && authenticationType == null) {
                throw new IllegalArgumentException("Authentication type should be provided with the entity id.");
            }
            return new ApplicationActor(this);
        }
    }
}
