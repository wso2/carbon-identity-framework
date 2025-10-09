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

package org.wso2.carbon.identity.flow.mgt;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.wso2.carbon.identity.ai.service.mgt.exceptions.AIClientException;
import org.wso2.carbon.identity.ai.service.mgt.exceptions.AIServerException;
import org.wso2.carbon.identity.ai.service.mgt.util.AIHttpClientUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.flow.mgt.exception.FlowMgtClientException;
import org.wso2.carbon.identity.flow.mgt.exception.FlowMgtServerException;
import org.wso2.carbon.identity.flow.mgt.model.FlowGenerationRequestDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowGenerationResponseDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowGenerationResultDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowGenerationStatusDTO;
import org.wso2.carbon.identity.flow.mgt.utils.FlowMgtUtils;

import java.util.HashMap;
import java.util.Map;

import static org.wso2.carbon.identity.ai.service.mgt.util.AIHttpClientUtil.executeRequest;
import static org.wso2.carbon.identity.flow.mgt.Constants.ErrorMessages.ERROR_CODE_INVOKING_AI_SERVICE;
import static org.wso2.carbon.registry.core.RegistryConstants.PATH_SEPARATOR;

/**
 * Service class for Flow AI operations.
 * This service provides methods to generate flows using AI, check generation status,
 * and retrieve generated flow results.
 */
public class FlowAIService {

    private static final Log LOG = LogFactory.getLog(FlowAIService.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final FlowAIService INSTANCE = new FlowAIService();

    private FlowAIService() {

    }

    /**
     * Holds the lazily-initialized configuration properties for the FlowAIService.
     * This class is loaded and initialized on its first use, providing thread-safe lazy initialization.
     */
    private static class ConfigHolder {

        static final String FLOW_AI_ENDPOINT = IdentityUtil.getProperty(Constants.FlowAIConstants.FLOW_AI_ENDPOINT);
        static final String FLOW_AI_GENERATE_PATH = IdentityUtil.getProperty(Constants.FlowAIConstants.FLOW_AI_GENERATE_PATH);
        static final String FLOW_AI_STATUS_PATH = IdentityUtil.getProperty(Constants.FlowAIConstants.FLOW_AI_STATUS_PATH);
        static final String FLOW_AI_RESULT_PATH = IdentityUtil.getProperty(Constants.FlowAIConstants.FLOW_AI_RESULT_PATH);
    }

    /**
     * Get FlowAIService instance.
     *
     * @return FlowAIService singleton instance.
     */
    public static FlowAIService getInstance() {

        return INSTANCE;
    }

    /**
     * Generate flow based on the provided request.
     *
     * @param flowGenerationRequestDTO Request containing flow type and user query.
     * @return Response containing operation ID for the generated flow.
     * @throws FlowMgtClientException If the request is invalid.
     * @throws FlowMgtServerException If an error occurs while invoking the AI service.
     */
    public FlowGenerationResponseDTO generateFlow(FlowGenerationRequestDTO flowGenerationRequestDTO)
            throws FlowMgtServerException, FlowMgtClientException {

        validateFlowGenerationRequest(flowGenerationRequestDTO);
        validateAIServiceConfiguration();

        String tenantDomain = IdentityTenantUtil.getTenantDomainFromContext();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Generating flow for tenant: " + tenantDomain + ", flowType: " +
                    flowGenerationRequestDTO.getFlowType());
        }

        try {
            Map<String, Object> requestBody = buildGenerateFlowRequestBody(flowGenerationRequestDTO);
            Map<String, Object> response = AIHttpClientUtil.executeRequest(ConfigHolder.FLOW_AI_ENDPOINT,
                    ConfigHolder.FLOW_AI_GENERATE_PATH, HttpPost.class, requestBody);

            if (response == null || response.isEmpty()) {
                throw FlowMgtUtils.handleServerException(ERROR_CODE_INVOKING_AI_SERVICE, tenantDomain);
            }

            String operationId = (String) response.get(Constants.FlowAIConstants.OPERATION_ID);
            if (StringUtils.isBlank(operationId)) {
                throw FlowMgtUtils.handleServerException(ERROR_CODE_INVOKING_AI_SERVICE, tenantDomain);
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Flow generation request submitted successfully with operation ID: " + operationId);
            }
            return new FlowGenerationResponseDTO.Builder()
                    .operationId(operationId)
                    .build();
        } catch (AIServerException | AIClientException e) {
            throw FlowMgtUtils.handleServerException(ERROR_CODE_INVOKING_AI_SERVICE, e, tenantDomain);
        }
    }

    /**
     * Get the status of the flow generation operation.
     *
     * @param operationId The operation ID of the flow generation request.
     * @return Status of the flow generation operation.
     * @throws FlowMgtClientException If the operation ID is invalid.
     * @throws FlowMgtServerException If an error occurs while retrieving the status.
     */
    public FlowGenerationStatusDTO getFlowGenerationStatus(String operationId)
            throws FlowMgtServerException, FlowMgtClientException {

        validateOperationId(operationId);
        validateAIServiceConfiguration();

        String tenantDomain = IdentityTenantUtil.getTenantDomainFromContext();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Getting flow generation status for operation ID: " + operationId + ", tenant: " + tenantDomain);
        }
        try {
            Map<String, Object> response = executeRequest(ConfigHolder.FLOW_AI_ENDPOINT,
                    ConfigHolder.FLOW_AI_STATUS_PATH + PATH_SEPARATOR + operationId,
                    HttpGet.class, null);

            if (response == null || response.isEmpty()) {
                throw FlowMgtUtils.handleServerException(ERROR_CODE_INVOKING_AI_SERVICE, tenantDomain);
            }

            Object statusObj = response.get(Constants.FlowAIConstants.STATUS);
            if (statusObj == null) {
                LOG.warn("Status field is missing in AI service response for operation ID: " + operationId);
                return createDefaultStatusResponse();
            }

            if (!(statusObj instanceof Map)) {
                throw FlowMgtUtils.handleServerException(ERROR_CODE_INVOKING_AI_SERVICE, tenantDomain);
            }

            Map<String, Boolean> statusMap = OBJECT_MAPPER.convertValue(statusObj, new StatusMapTypeReference());
            if (statusMap == null) {
                LOG.warn("Failed to convert status object to status map for operation ID: " + operationId);
                return createDefaultStatusResponse();
            }
            return buildStatusResponse(statusMap);

        } catch (AIServerException | AIClientException e) {
            throw FlowMgtUtils.handleServerException(ERROR_CODE_INVOKING_AI_SERVICE, e, tenantDomain);
        }
    }

    /**
     * Get the generated flow result for the given operation ID.
     *
     * @param operationId The operation ID of the flow generation request.
     * @return Generated flow result.
     * @throws FlowMgtClientException If the operation ID is invalid.
     * @throws FlowMgtServerException If an error occurs while retrieving the result.
     */
    public FlowGenerationResultDTO getFlowGenerationResult(String operationId)
            throws FlowMgtServerException, FlowMgtClientException {

        validateOperationId(operationId);
        validateAIServiceConfiguration();

        String tenantDomain = IdentityTenantUtil.getTenantDomainFromContext();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Getting flow generation result for operation ID: " + operationId + ", tenant: " + tenantDomain);
        }

        try {
            Map<String, Object> response = executeRequest(ConfigHolder.FLOW_AI_ENDPOINT,
                    ConfigHolder.FLOW_AI_RESULT_PATH + PATH_SEPARATOR + operationId,
                    HttpGet.class, null);

            if (response == null || response.isEmpty()) {
                throw FlowMgtUtils.handleServerException(ERROR_CODE_INVOKING_AI_SERVICE, tenantDomain);
            }

            String status = (String) response.get(Constants.FlowAIConstants.STATUS);
            if (StringUtils.isBlank(status)) {
                throw FlowMgtUtils.handleServerException(ERROR_CODE_INVOKING_AI_SERVICE, tenantDomain);
            }

            Object resultObj = response.get(Constants.FlowAIConstants.DATA);
            if (resultObj == null) {
                LOG.warn("Data field is missing in AI service response for operation ID: " + operationId);
            }

            Map<String, Object> dataMap = resultObj != null ?
                    OBJECT_MAPPER.convertValue(resultObj, new ResultMapTypeReference()) : new HashMap<>();

            if (LOG.isDebugEnabled()) {
                LOG.debug("Flow generation result retrieved successfully for operation ID: " + operationId);
            }
            return new FlowGenerationResultDTO.Builder()
                    .status(status)
                    .data(dataMap)
                    .build();

        } catch (AIServerException | AIClientException e) {
            throw FlowMgtUtils.handleServerException(ERROR_CODE_INVOKING_AI_SERVICE, e, tenantDomain);
        }
    }

    /**
     * Validate the flow generation request.
     *
     * @param request The flow generation request to validate.
     * @throws FlowMgtClientException If the request is invalid.
     */
    private void validateFlowGenerationRequest(FlowGenerationRequestDTO request) throws FlowMgtClientException {

        if (request == null) {
            throw new FlowMgtClientException("Flow generation request cannot be null");
        }

        if (StringUtils.isBlank(request.getFlowType())) {
            throw new FlowMgtClientException("Flow type cannot be null or empty");
        }

        if (StringUtils.isBlank(request.getUserQuery())) {
            throw new FlowMgtClientException("User query cannot be null or empty");
        }

        // Validate flow type against supported types.
        boolean isValidFlowType = false;
        for (Constants.FlowTypes flowType : Constants.FlowTypes.values()) {
            if (flowType.getType().equals(request.getFlowType())) {
                isValidFlowType = true;
                break;
            }
        }

        if (!isValidFlowType) {
            throw new FlowMgtClientException("Unsupported flow type: " + request.getFlowType());
        }
    }

    /**
     * Validate the operation ID.
     *
     * @param operationId The operation ID to validate.
     * @throws FlowMgtClientException If the operation ID is invalid.
     */
    private void validateOperationId(String operationId) throws FlowMgtClientException {

        if (StringUtils.isBlank(operationId)) {
            throw new FlowMgtClientException("Operation ID cannot be null or empty");
        }
    }

    /**
     * Validate AI service configuration.
     *
     * @throws FlowMgtServerException If the AI service is not properly configured.
     */
    private void validateAIServiceConfiguration() throws FlowMgtServerException {

        if (StringUtils.isBlank(ConfigHolder.FLOW_AI_ENDPOINT)) {
            throw FlowMgtUtils.handleServerException(ERROR_CODE_INVOKING_AI_SERVICE,
                    IdentityTenantUtil.getTenantDomainFromContext());
        }
        if (StringUtils.isBlank(ConfigHolder.FLOW_AI_GENERATE_PATH) ||
                StringUtils.isBlank(ConfigHolder.FLOW_AI_STATUS_PATH) ||
                StringUtils.isBlank(ConfigHolder.FLOW_AI_RESULT_PATH)) {
            throw FlowMgtUtils.handleServerException(ERROR_CODE_INVOKING_AI_SERVICE,
                    IdentityTenantUtil.getTenantDomainFromContext());
        }
    }

    /**
     * Build the request body for flow generation.
     *
     * @param request The flow generation request.
     * @return The request body map.
     */
    private Map<String, Object> buildGenerateFlowRequestBody(FlowGenerationRequestDTO request) {

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(Constants.FlowAIConstants.FLOW_TYPE, request.getFlowType());
        requestBody.put(Constants.FlowAIConstants.USER_QUERY, request.getUserQuery());
        return requestBody;
    }

    /**
     * Build the status response from the AI service response.
     *
     * @param statusMap The status map from AI service.
     * @return The flow generation status DTO.
     */
    private FlowGenerationStatusDTO buildStatusResponse(Map<String, Boolean> statusMap) {

        if (statusMap == null) {
            return createDefaultStatusResponse();
        }

        boolean optimizingQuery = statusMap.getOrDefault(Constants.FlowAIConstants.OPTIMIZING_QUERY, false);
        boolean fetchingSamples = statusMap.getOrDefault(Constants.FlowAIConstants.FETCHING_SAMPLES, false);
        boolean generatingFlow = statusMap.getOrDefault(Constants.FlowAIConstants.GENERATING_FLOW, false);
        boolean completed = statusMap.getOrDefault(Constants.FlowAIConstants.COMPLETED, false);

        return new FlowGenerationStatusDTO.Builder()
                .optimizingQuery(optimizingQuery)
                .fetchingSamples(fetchingSamples)
                .generatingFlow(generatingFlow)
                .completed(completed)
                .build();
    }

    /**
     * Create a default status response when status information is not available.
     *
     * @return Default flow generation status DTO.
     */
    private FlowGenerationStatusDTO createDefaultStatusResponse() {

        return new FlowGenerationStatusDTO.Builder()
                .optimizingQuery(false)
                .fetchingSamples(false)
                .generatingFlow(false)
                .completed(false)
                .build();
    }

    /**
     * Type reference for deserializing a map of flow generation status.
     */
    private static final class StatusMapTypeReference extends TypeReference<Map<String, Boolean>> {

    }

    /**
     * Type reference for deserializing a map of flow generation result data.
     */
    private static final class ResultMapTypeReference extends TypeReference<Map<String, Object>> {

    }
}
