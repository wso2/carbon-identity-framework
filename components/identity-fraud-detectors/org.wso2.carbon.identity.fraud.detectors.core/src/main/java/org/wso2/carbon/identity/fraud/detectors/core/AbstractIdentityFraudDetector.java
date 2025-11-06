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

package org.wso2.carbon.identity.fraud.detectors.core;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.identity.fraud.detectors.core.constant.FraudDetectorConstants;
import org.wso2.carbon.identity.fraud.detectors.core.exception.IdentityFraudDetectorException;
import org.wso2.carbon.identity.fraud.detectors.core.exception.IdentityFraudDetectorRequestException;
import org.wso2.carbon.identity.fraud.detectors.core.exception.IdentityFraudDetectorResponseException;
import org.wso2.carbon.identity.fraud.detectors.core.exception.UnsupportedFraudDetectionEventException;
import org.wso2.carbon.identity.fraud.detectors.core.internal.IdentityFraudDetectorDataHolder;
import org.wso2.carbon.identity.fraud.detectors.core.model.FraudDetectorRequestDTO;
import org.wso2.carbon.identity.fraud.detectors.core.model.FraudDetectorResponseDTO;

import java.io.IOException;

import static org.wso2.carbon.identity.event.IdentityEventConstants.EventProperty.TENANT_DOMAIN;
import static org.wso2.carbon.identity.fraud.detectors.core.constant.FraudDetectorConstants.ErrorType.INVALID_REQUEST;
import static org.wso2.carbon.identity.fraud.detectors.core.constant.FraudDetectorConstants.ErrorType.INVALID_RESPONSE;
import static org.wso2.carbon.identity.fraud.detectors.core.constant.FraudDetectorConstants.ErrorType.UNSUPPORTED_EVENT;
import static org.wso2.carbon.identity.fraud.detectors.core.constant.FraudDetectorConstants.ExecutionStatus.ERROR;
import static org.wso2.carbon.identity.fraud.detectors.core.constant.FraudDetectorConstants.ExecutionStatus.FAILURE;
import static org.wso2.carbon.identity.fraud.detectors.core.constant.FraudDetectorConstants.ExecutionStatus.SKIPPED;

/**
 * Abstract class providing common functionality for Identity Fraud Detectors.
 */
public abstract class AbstractIdentityFraudDetector implements IdentityFraudDetector {

    private static final Log LOG = LogFactory.getLog(AbstractIdentityFraudDetector.class);

    @Override
    public FraudDetectorResponseDTO publishRequest(FraudDetectorRequestDTO requestDTO) {

        String tenantDomain = (String) requestDTO.getProperties().get(TENANT_DOMAIN);
        if (!canHandle(tenantDomain)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("The fraud detector: " + getName() + " is not configured for the tenant: " + tenantDomain +
                        ". Hence not publishing the request.");
            }
            return new FraudDetectorResponseDTO(SKIPPED, requestDTO.getEventName());
        }

        CloseableHttpClient httpClient = IdentityFraudDetectorDataHolder.getInstance().getHttpClient();
        try {
            HttpUriRequest request = buildRequest(requestDTO);
            logRequestPayload(requestDTO.isLogRequestPayload(), request);
            CloseableHttpResponse response = httpClient.execute(request);
            return handleResponse(response.getStatusLine().getStatusCode(), getResponseContent(response), requestDTO);
        } catch (IdentityFraudDetectorException e) {
            return handleFraudDetectorException(e, requestDTO.getEventName());
        } catch (IOException e) {
            return new FraudDetectorResponseDTO(FAILURE, requestDTO.getEventName());
        } catch (Exception e) {
            LOG.error("Unexpected error occurred while publishing the request to the fraud detector: "
                    + getName(), e);
            return new FraudDetectorResponseDTO(FAILURE, requestDTO.getEventName());
        }
    }

    /* Reads and returns the response content from the CloseableHttpResponse.
     *
     * @param response CloseableHttpResponse received from the fraud detector.
     * @return Response content as a String.
     * @throws IdentityFraudDetectorResponseException If an error occurs while reading the response.
     */
    private String getResponseContent(CloseableHttpResponse response) throws IdentityFraudDetectorResponseException {

        String responseContent;
        try {
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                throw new IdentityFraudDetectorResponseException("Error occurred while reading the response from the " +
                        "fraud detector: " + getName() + ". Response entity is null.");
            }
            responseContent = EntityUtils.toString(entity);
            if (StringUtils.isBlank(responseContent)) {
                throw new IdentityFraudDetectorResponseException("Error occurred while reading the response from the " +
                        "fraud detector: " + getName() + ". Response content is empty.");
            }
            return responseContent;
        } catch (IOException e) {
            throw new IdentityFraudDetectorResponseException("Error occurred while reading the response from the " +
                    "fraud detector: " + getName(), e);
        }
    }

    /* Logs the request payload if logging is enabled.
     *
     * @param logRequestPayload Flag indicating whether to log the request payload.
     * @param request           HttpUriRequest sent to the fraud detector.
     */
    private void logRequestPayload(boolean logRequestPayload, HttpUriRequest request) {

        if (!logRequestPayload) {
            return;
        }
        HttpEntityEnclosingRequest httpEntityEnclosingRequest = (HttpEntityEnclosingRequest) request;
        try {
            String requestPayload = httpEntityEnclosingRequest.getEntity() != null ?
                    EntityUtils.toString(httpEntityEnclosingRequest.getEntity()) : null;
            if (StringUtils.isNotEmpty(requestPayload)) {
                String maskedPayload = getMaskedRequestPayload(requestPayload);
                LOG.info("Request payload sent to the fraud detector: " + getName() + " is: " + maskedPayload);
            }
        } catch (IOException | IdentityFraudDetectorException e) {
            LOG.error("Error occurred while logging the request payload for the fraud detector: " + getName(), e);
        }
    }

    /* Handles the IdentityFraudDetectorException and constructs a FraudDetectorResponseDTO.
     *
     * @param e     The IdentityFraudDetectorException thrown during request publishing.
     * @param event The fraud detection event name.
     * @return FraudDetectorResponseDTO containing error details.
     */
    private FraudDetectorResponseDTO handleFraudDetectorException(IdentityFraudDetectorException e,
                                                                  FraudDetectorConstants.FraudDetectionEvents event) {

        FraudDetectorResponseDTO responseDTO = new FraudDetectorResponseDTO(ERROR, event);
        FraudDetectorConstants.ErrorType errorType = null;
        if (e instanceof UnsupportedFraudDetectionEventException) {
            errorType = UNSUPPORTED_EVENT;
        } else if (e instanceof IdentityFraudDetectorRequestException) {
            errorType = INVALID_REQUEST;
        } else if (e instanceof IdentityFraudDetectorResponseException) {
            errorType = INVALID_RESPONSE;
        }
        responseDTO.setErrorType(errorType);
        responseDTO.setErrorReason(e.getMessage());
        return responseDTO;
    }
}
