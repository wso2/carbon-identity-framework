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

package org.wso2.carbon.identity.client.attestation.mgt.validators;

import org.wso2.carbon.identity.client.attestation.mgt.exceptions.ClientAttestationMgtException;
import org.wso2.carbon.identity.client.attestation.mgt.model.ClientAttestationContext;

/**
 * This interface handles attestation validation for public clients.
 */
public interface ClientAttestationValidator {

    /**
     * Validates the attestation data from the given attestationObject and updates the
     * client attestation context accordingly.
     *
     * @param attestationObject         The attestation object received from the client.
     * @param clientAttestationContext  The context to store the validation results and updated information.
     * @throws ClientAttestationMgtException If an error occurs during the attestation validation process.
     */
    void validateAttestation(String attestationObject, ClientAttestationContext clientAttestationContext)
            throws ClientAttestationMgtException;


    /**
     * This method indicates which client Attestation validation type, it can handle.
     *
     * @return Attestation validation type.
     */
    String getAttestationValidationType();

}
