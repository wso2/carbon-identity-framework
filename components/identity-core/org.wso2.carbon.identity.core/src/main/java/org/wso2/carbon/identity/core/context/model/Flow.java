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

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

/**
 * A Flow represents the complete journey of a particular process in the identity system.
 * It can contain multiple requests and is initiated by a specific entity.
 */
public class Flow {

    // Map to hold the flow definitions and its applicable initiating personas.
    private static final Map<Name, EnumSet<InitiatingPersona>> FLOW_DEFINITIONS = new EnumMap<>(Name.class);

    static {
        FLOW_DEFINITIONS.put(Name.PASSWORD_RESET,
                EnumSet.of(InitiatingPersona.ADMIN, InitiatingPersona.APPLICATION, InitiatingPersona.USER));
        FLOW_DEFINITIONS.put(Name.USER_REGISTRATION_INVITE_WITH_PASSWORD,
                EnumSet.of(InitiatingPersona.ADMIN, InitiatingPersona.APPLICATION));
        FLOW_DEFINITIONS.put(Name.PROFILE_UPDATE,
                EnumSet.of(InitiatingPersona.ADMIN, InitiatingPersona.APPLICATION, InitiatingPersona.USER));
        FLOW_DEFINITIONS.put(Name.BULK_RESOURCE_UPDATE,
                EnumSet.of(InitiatingPersona.ADMIN, InitiatingPersona.APPLICATION));
        FLOW_DEFINITIONS.put(Name.USER_GROUP_UPDATE,
                EnumSet.of(InitiatingPersona.ADMIN, InitiatingPersona.APPLICATION));
        FLOW_DEFINITIONS.put(Name.GROUP_UPDATE,
                EnumSet.of(InitiatingPersona.ADMIN, InitiatingPersona.APPLICATION));
        FLOW_DEFINITIONS.put(Name.LOGOUT,
                EnumSet.of(InitiatingPersona.APPLICATION, InitiatingPersona.USER));
        FLOW_DEFINITIONS.put(Name.DELETE_USER,
                EnumSet.of(InitiatingPersona.ADMIN, InitiatingPersona.APPLICATION));
        FLOW_DEFINITIONS.put(Name.UPDATE_CREDENTIAL_PASSWORD,
                EnumSet.of(InitiatingPersona.ADMIN, InitiatingPersona.APPLICATION, InitiatingPersona.USER));
        FLOW_DEFINITIONS.put(Name.ACCOUNT_DISABLE,
                EnumSet.of(InitiatingPersona.ADMIN, InitiatingPersona.APPLICATION));
        FLOW_DEFINITIONS.put(Name.ACCOUNT_LOCK,
                EnumSet.of(InitiatingPersona.ADMIN, InitiatingPersona.SYSTEM, InitiatingPersona.APPLICATION));
        FLOW_DEFINITIONS.put(Name.ACCOUNT_UNLOCK,
                EnumSet.of(InitiatingPersona.ADMIN, InitiatingPersona.SYSTEM, InitiatingPersona.APPLICATION));
        FLOW_DEFINITIONS.put(Name.SESSION_REVOKE,
                EnumSet.of(InitiatingPersona.ADMIN, InitiatingPersona.APPLICATION, InitiatingPersona.USER,
                        InitiatingPersona.SYSTEM));
        FLOW_DEFINITIONS.put(Name.USER_SELF_REGISTER, EnumSet.of(InitiatingPersona.USER));
        FLOW_DEFINITIONS.put(Name.REGISTER_WITH_PASSWORD, EnumSet.of(InitiatingPersona.ADMIN));
    }

    /**
     * Enum representing the name of a flow.
     * Each name identifies a specific flow and defines the set of initiating personas allowed to trigger it.
     */
    public enum Name {

        PASSWORD_RESET,
        USER_REGISTRATION_INVITE_WITH_PASSWORD,
        PROFILE_UPDATE,
        BULK_RESOURCE_UPDATE,
        USER_GROUP_UPDATE,
        GROUP_UPDATE,
        LOGOUT,
        UPDATE_CREDENTIAL_PASSWORD,
        DELETE_USER,
        ACCOUNT_LOCK,
        ACCOUNT_UNLOCK,
        ACCOUNT_DISABLE,
        SESSION_REVOKE,
        USER_SELF_REGISTER,
        REGISTER_WITH_PASSWORD
    }

    /**
     * Enum for Initiator Persona.
     * Specifies the type of entity responsible for initiating the Flow.
     * ADMIN : Represents a management flow initiated by an administrator or an authorized user who has
     * management privileges within the organization.
     * APPLICATION: Represents a flow initiated by an application.
     * USER: Represents a self-service flow initiated by an end-user(consumer).
     */
    public enum InitiatingPersona {

        ADMIN,
        APPLICATION,
        USER,
        SYSTEM
    }

    private final Name name;
    private final InitiatingPersona initiatingPersona;

    private Flow(Builder builder) {

        this.name = builder.name;
        this.initiatingPersona = builder.initiatingPersona;
    }

    public Name getName() {

        return name;
    }

    public InitiatingPersona getInitiatingPersona() {

        return initiatingPersona;
    }

    /**
     * Builder class for Flow.
     */
    public static class Builder {

        private Name name;
        private InitiatingPersona initiatingPersona;

        public Builder name(Name name) {

            this.name = name;
            return this;
        }

        public Builder initiatingPersona(InitiatingPersona initiatingPersona) {

            this.initiatingPersona = initiatingPersona;
            return this;
        }

        public Flow build() {

            validate();
            return new Flow(this);
        }

        private void validate() {

            if (name == null) {
                throw new IllegalArgumentException("Flow name cannot be null.");
            }
            if (initiatingPersona == null) {
                throw new IllegalArgumentException("Initiating persona cannot be null.");
            }
            if (FLOW_DEFINITIONS.get(name) == null || !FLOW_DEFINITIONS.get(name).contains(initiatingPersona)) {
                throw new IllegalArgumentException("Provided initiating persona: " + initiatingPersona.name() +
                        " is not applicable for the given flow: " + name.name());
            }
        }
    }
}
