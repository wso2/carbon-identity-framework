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

    /**
     * Map to hold the credential flow definitions and its applicable initiating personas.
     * The requirement of having this separation is to allow the credential flows to have a
     * credential type associated only with it.
     */
    private static final Map<Name, EnumSet<InitiatingPersona>> CREDENTIAL_FLOW_DEFINITIONS = new EnumMap<>(Name.class);

    static {
        // ------------------------------------ Invited registration flows --------------------------------
        FLOW_DEFINITIONS.put(Name.INVITE,
                EnumSet.of(InitiatingPersona.ADMIN, InitiatingPersona.APPLICATION));

        // Deprecated: To be removed when migration is complete.
        FLOW_DEFINITIONS.put(Name.INVITED_USER_REGISTRATION,
                EnumSet.of(InitiatingPersona.ADMIN, InitiatingPersona.APPLICATION));
        // -----------------------------------------------------------------------------------------------

        // -------------------------------- Direct and self-registration flows ---------------------------
        FLOW_DEFINITIONS.put(Name.REGISTER,
                EnumSet.of(InitiatingPersona.ADMIN, InitiatingPersona.APPLICATION, InitiatingPersona.USER));
        // -----------------------------------------------------------------------------------------------

        // -------------------------------- JIT Provisioning flows ----------------------------------------
        FLOW_DEFINITIONS.put(Name.JUST_IN_TIME_PROVISION, EnumSet.of(InitiatingPersona.USER));
        // -----------------------------------------------------------------------------------------------

        // -------------------------------- Authentication flows -----------------------------------------
        FLOW_DEFINITIONS.put(Name.LOGOUT,
                EnumSet.of(InitiatingPersona.APPLICATION, InitiatingPersona.USER));
        FLOW_DEFINITIONS.put(Name.LOGIN,
                EnumSet.of(InitiatingPersona.APPLICATION, InitiatingPersona.USER));
        // -----------------------------------------------------------------------------------------------

        // ------------------------------- User management flows -----------------------------------------
        FLOW_DEFINITIONS.put(Name.PROFILE_UPDATE,
                EnumSet.of(InitiatingPersona.ADMIN, InitiatingPersona.APPLICATION, InitiatingPersona.USER));
        FLOW_DEFINITIONS.put(Name.USER_ACCOUNT_DELETE,
                EnumSet.of(InitiatingPersona.ADMIN, InitiatingPersona.APPLICATION));
        FLOW_DEFINITIONS.put(Name.USER_ACCOUNT_LOCK,
                EnumSet.of(InitiatingPersona.ADMIN, InitiatingPersona.SYSTEM, InitiatingPersona.APPLICATION));
        FLOW_DEFINITIONS.put(Name.USER_ACCOUNT_UNLOCK,
                EnumSet.of(InitiatingPersona.ADMIN, InitiatingPersona.SYSTEM, InitiatingPersona.APPLICATION));
        FLOW_DEFINITIONS.put(Name.USER_ACCOUNT_ENABLE,
                EnumSet.of(InitiatingPersona.ADMIN, InitiatingPersona.APPLICATION));
        FLOW_DEFINITIONS.put(Name.USER_ACCOUNT_DISABLE,
                EnumSet.of(InitiatingPersona.ADMIN, InitiatingPersona.APPLICATION));
        // -----------------------------------------------------------------------------------------------

        // -------------------------- Credential management flows -------------------------------------
        CREDENTIAL_FLOW_DEFINITIONS.put(Name.CREDENTIAL_ENROLL,
                EnumSet.of(InitiatingPersona.ADMIN, InitiatingPersona.APPLICATION, InitiatingPersona.USER));
        CREDENTIAL_FLOW_DEFINITIONS.put(Name.CREDENTIAL_UPDATE,
                EnumSet.of(InitiatingPersona.ADMIN, InitiatingPersona.APPLICATION, InitiatingPersona.USER));
        CREDENTIAL_FLOW_DEFINITIONS.put(Name.CREDENTIAL_RESET,
                EnumSet.of(InitiatingPersona.ADMIN, InitiatingPersona.APPLICATION, InitiatingPersona.USER));
        CREDENTIAL_FLOW_DEFINITIONS.put(Name.CREDENTIAL_REVOKE,
                EnumSet.of(InitiatingPersona.ADMIN, InitiatingPersona.APPLICATION, InitiatingPersona.USER));

        // Deprecated: To be removed when migration is complete.
        FLOW_DEFINITIONS.put(Name.PASSWORD_RESET,
                EnumSet.of(InitiatingPersona.ADMIN, InitiatingPersona.APPLICATION, InitiatingPersona.USER));
        // ------------------------------------------------------------------------------------------------

        // ----------------------------- Session management flows ----------------------------------------
        FLOW_DEFINITIONS.put(Name.SESSION_REVOKE,
                EnumSet.of(InitiatingPersona.ADMIN, InitiatingPersona.APPLICATION, InitiatingPersona.USER,
                        InitiatingPersona.SYSTEM));
        // -----------------------------------------------------------------------------------------------

        // ------------------------------------- Token flows --------------------------------------------
        FLOW_DEFINITIONS.put(Name.TOKEN_ISSUE, EnumSet.of(InitiatingPersona.APPLICATION, InitiatingPersona.USER));
        FLOW_DEFINITIONS.put(Name.TOKEN_REVOKE,
                EnumSet.of(InitiatingPersona.ADMIN, InitiatingPersona.APPLICATION, InitiatingPersona.USER,
                        InitiatingPersona.SYSTEM));
        // -----------------------------------------------------------------------------------------------

        // -------------------------- Bulk resource management flows -------------------------------------
        FLOW_DEFINITIONS.put(Name.BULK_RESOURCE_UPDATE,
                EnumSet.of(InitiatingPersona.ADMIN, InitiatingPersona.APPLICATION));
        // -----------------------------------------------------------------------------------------------

        // -------------------------- User Group management flows ----------------------------------------
        FLOW_DEFINITIONS.put(Name.USER_GROUP_UPDATE,
                EnumSet.of(InitiatingPersona.ADMIN, InitiatingPersona.APPLICATION));
        FLOW_DEFINITIONS.put(Name.GROUP_UPDATE,
                EnumSet.of(InitiatingPersona.ADMIN, InitiatingPersona.APPLICATION));
        // -----------------------------------------------------------------------------------------------
    }

    /**
     * Enum representing the name of a flow.
     * Each name identifies a specific flow and defines the set of initiating personas allowed to trigger it.
     */
    public enum Name {

        // ------------ Invited registration flows ----------
        INVITE,
        @Deprecated // Use INVITE instead.
        INVITED_USER_REGISTRATION,
        // --------------------------------------------------

        // --------Direct and self-registration flows--------
        REGISTER,
        // --------------------------------------------------

        // -------------JIT Provisioning flows---------------
        JUST_IN_TIME_PROVISION,
        // --------------------------------------------------

        // -------------Authentication flows-----------------
        LOGIN,
        LOGOUT,
        // --------------------------------------------------

        // -----------User management flows------------------
        PROFILE_UPDATE,
        USER_ACCOUNT_DELETE,
        USER_ACCOUNT_LOCK,
        USER_ACCOUNT_UNLOCK,
        USER_ACCOUNT_ENABLE,
        USER_ACCOUNT_DISABLE,
        // --------------------------------------------------

        // -------------Credential management flows----------
        /*
        These flows should always come with the type of the credential at the flow itself.
        i.e CREDENTIAL_RESET-password, CREDENTIAL_ENROLL-otp, etc.
         */
        CREDENTIAL_ENROLL,
        CREDENTIAL_UPDATE,
        CREDENTIAL_RESET,
        @Deprecated // Use CREDENTIAL_RESET instead.
        PASSWORD_RESET,
        CREDENTIAL_REVOKE,
        // --------------------------------------------------

        // ---------Session management flows-----------------
        SESSION_REVOKE,
        // --------------------------------------------------

        // ---------Token flows-----------------
        TOKEN_ISSUE,
        TOKEN_REVOKE,
        // --------------------------------------------------

        // ---------Bulk resource management flows-----------
        BULK_RESOURCE_UPDATE,
        // --------------------------------------------------

        // ---------User Group management flows--------------
        USER_GROUP_UPDATE,
        GROUP_UPDATE
        // --------------------------------------------------
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

    public enum CredentialType {

        PASSWORD,
        PASSKEY
    }

    private final Name name;
    private final InitiatingPersona initiatingPersona;
    private CredentialType credentialType;

    private Flow(Builder builder) {

        this.name = builder.name;
        this.initiatingPersona = builder.initiatingPersona;
    }

    private Flow(CredentialFlowBuilder builder) {

        this.name = builder.name;
        this.initiatingPersona = builder.initiatingPersona;
        this.credentialType = builder.credentialType;
    }

    public Name getName() {

        return name;
    }

    public InitiatingPersona getInitiatingPersona() {

        return initiatingPersona;
    }

    public CredentialType getCredentialType() {

        return credentialType;
    }

    /**
     * Checks if the given flow name corresponds to a credential flow.
     *
     * @param name The name of the flow.
     * @return true if it is a credential flow; false otherwise.
     */
    public static boolean isCredentialFlow(Name name) {

        return CREDENTIAL_FLOW_DEFINITIONS.containsKey(name);
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

    /**
     * Builder class for a credential Flow.
     * Dedicated builder for credential flows to handle the credential type.
     */
    public static class CredentialFlowBuilder {

        private Name name;
        private InitiatingPersona initiatingPersona;
        private CredentialType credentialType;

        public CredentialFlowBuilder name(Name name) {

            this.name = name;
            return this;
        }

        public CredentialFlowBuilder initiatingPersona(InitiatingPersona initiatingPersona) {

            this.initiatingPersona = initiatingPersona;
            return this;
        }

        public CredentialFlowBuilder credentialType(CredentialType credentialType) {

            this.credentialType = credentialType;
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
            if (credentialType == null) {
                throw new IllegalArgumentException("Credential type cannot be null for a credential flow.");
            }
            if (CREDENTIAL_FLOW_DEFINITIONS.get(name) == null ||
                    !CREDENTIAL_FLOW_DEFINITIONS.get(name).contains(initiatingPersona)) {
                throw new IllegalArgumentException("Provided initiating persona: " + initiatingPersona.name() +
                        " is not applicable for the given flow: " + name.name());
            }
        }
    }
}
