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

package org.wso2.carbon.identity.fraud.detection.core;

import org.apache.http.client.methods.HttpUriRequest;
import org.wso2.carbon.identity.fraud.detection.core.exception.IdentityFraudDetectionException;
import org.wso2.carbon.identity.fraud.detection.core.model.FraudDetectorRequestDTO;
import org.wso2.carbon.identity.fraud.detection.core.model.FraudDetectorResponseDTO;

/**
 * Interface to be implemented by all Identity Fraud Detectors.
 */
public interface IdentityFraudDetector {

    /**
     * Returns the unique name of the fraud detector.
     *
     * @return Name of the fraud detector.
     */
    String getName();

    /**
     * Indicates whether this fraud detector can handle the current request.
     *
     * @param tenantDomain The tenant domain for which the fraud detection is to be performed.
     * @return true if the fraud detector can handle the request, false otherwise.
     */
    boolean canHandle(String tenantDomain);

    /**
     * Publishes the request to the fraud detector and returns the response.
     *
     * @param requestDTO Request DTO.
     * @return Response DTO.
     */
    FraudDetectorResponseDTO publishRequest(FraudDetectorRequestDTO requestDTO);

    /**
     * Builds the request payload to be sent to the fraud detector.
     *
     * @param requestDTO Request DTO containing necessary information.
     * @return HttpUriRequest to be sent to the fraud detector.
     * @throws IdentityFraudDetectionException If an error occurs while building the request.
     */
    HttpUriRequest buildRequest(FraudDetectorRequestDTO requestDTO) throws IdentityFraudDetectionException;

    /**
     * Handles the response received from the fraud detector.
     *
     * @param responseStatusCode The HTTP response status code received from the fraud detector.
     * @param responseContent The response content received from the fraud detector.
     * @param requestDTO Request DTO containing necessary information.
     * @return Response DTO containing the processed response information.
     * @throws IdentityFraudDetectionException If an error occurs while handling the response.
     */
    FraudDetectorResponseDTO handleResponse(int responseStatusCode, String responseContent,
                                            FraudDetectorRequestDTO requestDTO)
            throws IdentityFraudDetectionException;

    /**
     * Masks sensitive information in the request payload for logging purposes.
     *
     * @param payload The original request payload.
     * @return The masked request payload.
     * @throws IdentityFraudDetectionException If an error occurs while masking the payload.
     */
    String getMaskedRequestPayload(String payload) throws IdentityFraudDetectionException;
}
