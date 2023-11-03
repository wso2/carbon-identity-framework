/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.identity.client.attestation.mgt.services;

import org.wso2.carbon.identity.client.attestation.mgt.exceptions.ClientAttestationMgtException;
import org.wso2.carbon.identity.client.attestation.mgt.model.ClientAttestationContext;

/**
 * This interface defines methods for client attestation, which involves validating attestation
 * data and returning a context.
 */
public interface ClientAttestationService {

    /**
     * Validates the attestation data for a client and tenant and returns a context with the validation result.
     *
     * @param attestationObject         The attestation data to be validated.
     * @param applicationResourceId     The application Resource Id.
     * @param tenantDomain              The tenant domain.
     * @return A context with the validation result.
     * @throws ClientAttestationMgtException If an error occurs during the attestation validation process.
     */
    public ClientAttestationContext validateAttestation(String attestationObject, String applicationResourceId,
                                                        String tenantDomain) throws ClientAttestationMgtException;
}

