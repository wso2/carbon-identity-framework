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

import org.wso2.carbon.identity.core.context.model.ApplicationEntity;
import org.wso2.carbon.identity.core.context.model.InitiatingEntity;

/**
 * Class for the Initiator.
 * This class holds the initiator details for a given flow.
 */
public class Initiator {

    /**
     * Enum for the Initiator Flow.
     */
    public enum InitiatorFlow {
        ADMIN,
        USER
    }

    private final InitiatorFlow initiatorFlow;
    private final InitiatingEntity initiatingEntity;

    private Initiator(Builder builder) {

        this.initiatorFlow = builder.initiatorFlow;
        this.initiatingEntity = builder.initiatingEntity;
    }

    public InitiatorFlow getInitiatorFlow() {

        return initiatorFlow;
    }

    public InitiatingEntity getInitiatingEntity() {

        return initiatingEntity;
    }

    public UserEntity getUserEntity() {

        if (isUserEntity()) {
            return (UserEntity) initiatingEntity;
        }
        return null;
    }

    public boolean isUserEntity() {

        return initiatingEntity instanceof UserEntity;
    }

    public ApplicationEntity getApplicationEntity() {

        if (isApplicationEntity()) {
            return (ApplicationEntity) initiatingEntity;
        }
        return null;
    }

    public boolean isApplicationEntity() {

        return initiatingEntity instanceof ApplicationEntity;
    }

    /**
     * Builder for the Initiator.
     */
    public static class Builder {

        private InitiatorFlow initiatorFlow;
        private InitiatingEntity initiatingEntity;

        public Builder() {

            Initiator initiator = IdentityContext.getThreadLocalIdentityContext().getInitiator();
            if (initiator != null) {
                this.initiatorFlow = initiator.initiatorFlow;
                this.initiatingEntity = initiator.initiatingEntity;
            }
        }

        public Builder initiatorFlow(InitiatorFlow initiatorFlow) {

            this.initiatorFlow = initiatorFlow;
            return this;
        }

        public Builder initiatingEntity(InitiatingEntity initiatingEntity) {

            this.initiatingEntity = initiatingEntity;
            return this;
        }

        public Initiator build() {

            return new Initiator(this);
        }
    }
}
