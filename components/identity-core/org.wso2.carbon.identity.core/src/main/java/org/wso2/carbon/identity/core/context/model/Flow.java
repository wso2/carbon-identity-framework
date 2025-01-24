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

import java.util.EnumSet;

/**
 * A Flow represents the complete journey of a particular process in the identity system.
 * It can contain multiple requests and is initiated by a specific entity.
 */
public class Flow {

    /**
     * Enum for names.
     * Identifies the flow.
     */
    public enum Name {

        PASSWORD_UPDATE(EnumSet.of(InitiatingPersona.ADMIN, InitiatingPersona.APPLICATION, InitiatingPersona.USER)),
        PASSWORD_RESET(EnumSet.of(InitiatingPersona.ADMIN, InitiatingPersona.APPLICATION, InitiatingPersona.USER)),
        USER_REGISTRATION_INVITE_WITH_PASSWORD(EnumSet.of(InitiatingPersona.ADMIN, InitiatingPersona.APPLICATION));

        private final EnumSet<InitiatingPersona> applicableInitiatingPersonas;

        Name(EnumSet<InitiatingPersona> applicableInitiatingPersonas) {

            this.applicableInitiatingPersonas = applicableInitiatingPersonas;
        }

        public boolean isApplicablePersona(InitiatingPersona initiatingPersona) {

            return applicableInitiatingPersonas.contains(initiatingPersona);
        }

        public EnumSet<InitiatingPersona> getApplicableInitiatingPersonas() {

            return applicableInitiatingPersonas;
        }
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
        USER
    }

    private final Name name;
    private final InitiatingPersona initiatingPersona;

    public Flow(Name name, InitiatingPersona initiatingPersona) {

        if (name.isApplicablePersona(initiatingPersona)) {
            this.name = name;
            this.initiatingPersona = initiatingPersona;
            return;
        }
        throw new IllegalArgumentException("Provided initiating persona is not applicable for the given flow.");
    }

    public Name getName() {

        return name;
    }

    public InitiatingPersona getInitiatingPersona() {

        return initiatingPersona;
    }
}
