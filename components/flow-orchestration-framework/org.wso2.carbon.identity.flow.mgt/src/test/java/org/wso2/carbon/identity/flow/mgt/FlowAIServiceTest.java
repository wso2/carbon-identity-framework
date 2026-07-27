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

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for FlowAIService.
 */
public class FlowAIServiceTest {

    private static final String TEST_FLOW_TYPE = "REGISTRATION";
    private static final String TEST_USER_QUERY = "Create a flow for user authentication";
    private static final String TEST_OPERATION_ID = "test-operation-123";
    private static final String TEST_TENANT_DOMAIN = "test.domain.com";
    private static final String TEST_ENDPOINT = "http://test-ai-service.com";
    private static final String TEST_GENERATE_PATH = "/api/v1/generate";
    private static final String TEST_STATUS_PATH = "/api/v1/status";
    private static final String TEST_RESULT_PATH = "/api/v1/result";

    private MockedStatic<IdentityUtil> identityUtilMockedStatic;
    private MockedStatic<AIHttpClientUtil> aiHttpClientUtilMockedStatic;
    private MockedStatic<IdentityTenantUtil> identityTenantUtilMockedStatic;
    private MockedStatic<FlowMgtUtils> flowMgtUtilsMockedStatic;

    @BeforeMethod
    public void setUp() {

        identityUtilMockedStatic = mockStatic(IdentityUtil.class);
        aiHttpClientUtilMockedStatic = mockStatic(AIHttpClientUtil.class);
        identityTenantUtilMockedStatic = mockStatic(IdentityTenantUtil.class);
        flowMgtUtilsMockedStatic = mockStatic(FlowMgtUtils.class);

        identityUtilMockedStatic.when(() -> IdentityUtil.getProperty(Constants.FlowAIConstants.FLOW_AI_ENDPOINT))
                .thenReturn(TEST_ENDPOINT);
        identityUtilMockedStatic.when(() -> IdentityUtil.getProperty(Constants.FlowAIConstants.FLOW_AI_GENERATE_PATH))
                .thenReturn(TEST_GENERATE_PATH);
        identityUtilMockedStatic.when(() -> IdentityUtil.getProperty(Constants.FlowAIConstants.FLOW_AI_STATUS_PATH))
                .thenReturn(TEST_STATUS_PATH);
        identityUtilMockedStatic.when(() -> IdentityUtil.getProperty(Constants.FlowAIConstants.FLOW_AI_RESULT_PATH))
                .thenReturn(TEST_RESULT_PATH);

        identityTenantUtilMockedStatic.when(IdentityTenantUtil::getTenantDomainFromContext)
                .thenReturn(TEST_TENANT_DOMAIN);
    }

    @AfterMethod
    public void tearDown() {

        if (identityUtilMockedStatic != null) {
            identityUtilMockedStatic.close();
        }
        if (aiHttpClientUtilMockedStatic != null) {
            aiHttpClientUtilMockedStatic.close();
        }
        if (identityTenantUtilMockedStatic != null) {
            identityTenantUtilMockedStatic.close();
        }
        if (flowMgtUtilsMockedStatic != null) {
            flowMgtUtilsMockedStatic.close();
        }
    }

    @Test
    public void testGetInstance() {

        FlowAIService instance1 = FlowAIService.getInstance();
        FlowAIService instance2 = FlowAIService.getInstance();

        assertNotNull(instance1);
        assertNotNull(instance2);
        assertEquals(instance1, instance2, "getInstance should return the same singleton instance");
    }

    @Test
    public void testGenerateFlowSuccess() throws Exception {

        FlowGenerationRequestDTO requestDTO = new FlowGenerationRequestDTO.Builder()
                .flowType(TEST_FLOW_TYPE)
                .userQuery(TEST_USER_QUERY)
                .build();

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put(Constants.FlowAIConstants.OPERATION_ID, TEST_OPERATION_ID);

        aiHttpClientUtilMockedStatic.when(() -> AIHttpClientUtil.executeRequest(
                eq(TEST_ENDPOINT),
                eq(TEST_GENERATE_PATH),
                eq(HttpPost.class),
                any(Map.class)
        )).thenReturn(mockResponse);

        FlowGenerationResponseDTO result = FlowAIService.getInstance().generateFlow(requestDTO);

        assertNotNull(result);
        assertEquals(result.getOperationId(), TEST_OPERATION_ID);
    }

    @Test
    public void testGenerateFlowWithNullRequest() {

        assertThrows(FlowMgtClientException.class, () ->
                FlowAIService.getInstance().generateFlow(null));
    }

    @Test
    public void testGenerateFlowWithNullFlowType() {

        FlowGenerationRequestDTO requestDTO = new FlowGenerationRequestDTO.Builder()
                .flowType(null)
                .userQuery(TEST_USER_QUERY)
                .build();

        assertThrows(FlowMgtClientException.class, () ->
                FlowAIService.getInstance().generateFlow(requestDTO));
    }

    @Test
    public void testGenerateFlowWithEmptyFlowType() {

        FlowGenerationRequestDTO requestDTO = new FlowGenerationRequestDTO.Builder()
                .flowType("")
                .userQuery(TEST_USER_QUERY)
                .build();

        assertThrows(FlowMgtClientException.class, () ->
                FlowAIService.getInstance().generateFlow(requestDTO));
    }

    @Test
    public void testGenerateFlowWithNullUserQuery() {

        FlowGenerationRequestDTO requestDTO = new FlowGenerationRequestDTO.Builder()
                .flowType(TEST_FLOW_TYPE)
                .userQuery(null)
                .build();

        assertThrows(FlowMgtClientException.class, () ->
                FlowAIService.getInstance().generateFlow(requestDTO));
    }

    @Test
    public void testGenerateFlowWithEmptyUserQuery() {

        FlowGenerationRequestDTO requestDTO = new FlowGenerationRequestDTO.Builder()
                .flowType(TEST_FLOW_TYPE)
                .userQuery("")
                .build();

        assertThrows(FlowMgtClientException.class, () ->
                FlowAIService.getInstance().generateFlow(requestDTO));
    }

    @Test
    public void testGenerateFlowWithUnsupportedFlowType() {

        FlowGenerationRequestDTO requestDTO = new FlowGenerationRequestDTO.Builder()
                .flowType("UNSUPPORTED_FLOW_TYPE")
                .userQuery(TEST_USER_QUERY)
                .build();

        assertThrows(FlowMgtClientException.class, () ->
                FlowAIService.getInstance().generateFlow(requestDTO));
    }

    @Test
    public void testGenerateFlowWithMissingEndpoint() {

        identityUtilMockedStatic.when(() -> IdentityUtil.getProperty(Constants.FlowAIConstants.FLOW_AI_ENDPOINT))
                .thenReturn(null);

        FlowGenerationRequestDTO requestDTO = new FlowGenerationRequestDTO.Builder()
                .flowType(TEST_FLOW_TYPE)
                .userQuery(TEST_USER_QUERY)
                .build();

        FlowMgtServerException expectedException = new FlowMgtServerException("Test exception");
        flowMgtUtilsMockedStatic.when(() -> FlowMgtUtils.handleServerException(
                eq(Constants.ErrorMessages.ERROR_CODE_INVOKING_AI_SERVICE),
                eq(TEST_TENANT_DOMAIN)
        )).thenReturn(expectedException);

        assertThrows(FlowMgtServerException.class, () ->
                FlowAIService.getInstance().generateFlow(requestDTO));
    }

    @Test
    public void testGenerateFlowWithEmptyResponse() throws Exception {

        FlowGenerationRequestDTO requestDTO = new FlowGenerationRequestDTO.Builder()
                .flowType(TEST_FLOW_TYPE)
                .userQuery(TEST_USER_QUERY)
                .build();

        aiHttpClientUtilMockedStatic.when(() -> AIHttpClientUtil.executeRequest(
                eq(TEST_ENDPOINT),
                eq(TEST_GENERATE_PATH),
                eq(HttpPost.class),
                any(Map.class)
        )).thenReturn(new HashMap<>());

        FlowMgtServerException expectedException = new FlowMgtServerException("Test exception");
        flowMgtUtilsMockedStatic.when(() -> FlowMgtUtils.handleServerException(
                eq(Constants.ErrorMessages.ERROR_CODE_INVOKING_AI_SERVICE),
                eq(TEST_TENANT_DOMAIN)
        )).thenReturn(expectedException);

        assertThrows(FlowMgtServerException.class, () ->
                FlowAIService.getInstance().generateFlow(requestDTO));
    }

    @Test
    public void testGenerateFlowWithNullOperationId() throws Exception {

        FlowGenerationRequestDTO requestDTO = new FlowGenerationRequestDTO.Builder()
                .flowType(TEST_FLOW_TYPE)
                .userQuery(TEST_USER_QUERY)
                .build();

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put(Constants.FlowAIConstants.OPERATION_ID, null);

        aiHttpClientUtilMockedStatic.when(() -> AIHttpClientUtil.executeRequest(
                eq(TEST_ENDPOINT),
                eq(TEST_GENERATE_PATH),
                eq(HttpPost.class),
                any(Map.class)
        )).thenReturn(mockResponse);

        FlowMgtServerException expectedException = new FlowMgtServerException("Test exception");
        flowMgtUtilsMockedStatic.when(() -> FlowMgtUtils.handleServerException(
                eq(Constants.ErrorMessages.ERROR_CODE_INVOKING_AI_SERVICE),
                eq(TEST_TENANT_DOMAIN)
        )).thenReturn(expectedException);

        assertThrows(FlowMgtServerException.class, () ->
                FlowAIService.getInstance().generateFlow(requestDTO));
    }

    @Test
    public void testGenerateFlowWithAIException() throws Exception {

        FlowGenerationRequestDTO requestDTO = new FlowGenerationRequestDTO.Builder()
                .flowType(TEST_FLOW_TYPE)
                .userQuery(TEST_USER_QUERY)
                .build();

        AIServerException aiException = new AIServerException("AI service error", "AI_ERROR");
        aiHttpClientUtilMockedStatic.when(() -> AIHttpClientUtil.executeRequest(
                eq(TEST_ENDPOINT),
                eq(TEST_GENERATE_PATH),
                eq(HttpPost.class),
                any(Map.class)
        )).thenThrow(aiException);

        FlowMgtServerException expectedException = new FlowMgtServerException("Test exception");
        flowMgtUtilsMockedStatic.when(() -> FlowMgtUtils.handleServerException(
                eq(Constants.ErrorMessages.ERROR_CODE_INVOKING_AI_SERVICE),
                eq(aiException),
                eq(TEST_TENANT_DOMAIN)
        )).thenReturn(expectedException);

        assertThrows(FlowMgtServerException.class, () ->
                FlowAIService.getInstance().generateFlow(requestDTO));
    }

    @Test
    public void testGetFlowGenerationStatusSuccess() throws Exception {

        Map<String, Boolean> statusMap = new HashMap<>();
        statusMap.put(Constants.FlowAIConstants.OPTIMIZING_QUERY, true);
        statusMap.put(Constants.FlowAIConstants.FETCHING_SAMPLES, false);
        statusMap.put(Constants.FlowAIConstants.GENERATING_FLOW, false);
        statusMap.put(Constants.FlowAIConstants.COMPLETED, false);

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put(Constants.FlowAIConstants.STATUS, statusMap);

        aiHttpClientUtilMockedStatic.when(() -> AIHttpClientUtil.executeRequest(
                eq(TEST_ENDPOINT),
                eq(TEST_STATUS_PATH + "/" + TEST_OPERATION_ID),
                eq(HttpGet.class),
                eq(null)
        )).thenReturn(mockResponse);

        FlowGenerationStatusDTO result = FlowAIService.getInstance().getFlowGenerationStatus(TEST_OPERATION_ID);

        assertNotNull(result);
        assertTrue(result.isOptimizingQuery());
        assertFalse(result.isFetchingSamples());
        assertFalse(result.isGeneratingFlow());
        assertFalse(result.isCompleted());
    }

    @Test
    public void testGetFlowGenerationStatusWithNullOperationId() {

        assertThrows(FlowMgtClientException.class, () ->
                FlowAIService.getInstance().getFlowGenerationStatus(null));
    }

    @Test
    public void testGetFlowGenerationStatusWithEmptyOperationId() {

        assertThrows(FlowMgtClientException.class, () ->
                FlowAIService.getInstance().getFlowGenerationStatus(""));
    }

    @Test
    public void testGetFlowGenerationStatusWithEmptyResponse() throws Exception {

        aiHttpClientUtilMockedStatic.when(() -> AIHttpClientUtil.executeRequest(
                eq(TEST_ENDPOINT),
                eq(TEST_STATUS_PATH + "/" + TEST_OPERATION_ID),
                eq(HttpGet.class),
                eq(null)
        )).thenReturn(new HashMap<>());

        FlowMgtServerException expectedException = new FlowMgtServerException("Test exception");
        flowMgtUtilsMockedStatic.when(() -> FlowMgtUtils.handleServerException(
                eq(Constants.ErrorMessages.ERROR_CODE_INVOKING_AI_SERVICE),
                eq(TEST_TENANT_DOMAIN)
        )).thenReturn(expectedException);

        assertThrows(FlowMgtServerException.class, () ->
                FlowAIService.getInstance().getFlowGenerationStatus(TEST_OPERATION_ID));
    }

    @Test
    public void testGetFlowGenerationStatusWithNullResponse() throws Exception {

        aiHttpClientUtilMockedStatic.when(() -> AIHttpClientUtil.executeRequest(
                eq(TEST_ENDPOINT),
                eq(TEST_STATUS_PATH + "/" + TEST_OPERATION_ID),
                eq(HttpGet.class),
                eq(null)
        )).thenReturn(null);

        FlowMgtServerException expectedException = new FlowMgtServerException("Test exception");
        flowMgtUtilsMockedStatic.when(() -> FlowMgtUtils.handleServerException(
                eq(Constants.ErrorMessages.ERROR_CODE_INVOKING_AI_SERVICE),
                eq(TEST_TENANT_DOMAIN)
        )).thenReturn(expectedException);

        assertThrows(FlowMgtServerException.class, () ->
                FlowAIService.getInstance().getFlowGenerationStatus(TEST_OPERATION_ID));
    }

    @Test
    public void testGetFlowGenerationResultSuccess() throws Exception {

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("flowId", "test-flow-123");
        dataMap.put("steps", 5);

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put(Constants.FlowAIConstants.STATUS, "COMPLETED");
        mockResponse.put(Constants.FlowAIConstants.DATA, dataMap);

        aiHttpClientUtilMockedStatic.when(() -> AIHttpClientUtil.executeRequest(
                eq(TEST_ENDPOINT),
                eq(TEST_RESULT_PATH + "/" + TEST_OPERATION_ID),
                eq(HttpGet.class),
                eq(null)
        )).thenReturn(mockResponse);

        FlowGenerationResultDTO result = FlowAIService.getInstance().getFlowGenerationResult(TEST_OPERATION_ID);

        assertNotNull(result);
        assertEquals(result.getStatus(), "COMPLETED");
        assertNotNull(result.getData());
        assertEquals(result.getData().get("flowId"), "test-flow-123");
        assertEquals(result.getData().get("steps"), 5);
    }

    @Test
    public void testGetFlowGenerationResultWithNullOperationId() {

        assertThrows(FlowMgtClientException.class, () ->
                FlowAIService.getInstance().getFlowGenerationResult(null));
    }

    @Test
    public void testGetFlowGenerationResultWithMissingStatus() throws Exception {

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put(Constants.FlowAIConstants.DATA, new HashMap<>());
        aiHttpClientUtilMockedStatic.when(() -> AIHttpClientUtil.executeRequest(
                eq(TEST_ENDPOINT),
                eq(TEST_RESULT_PATH + "/" + TEST_OPERATION_ID),
                eq(HttpGet.class),
                eq(null)
        )).thenReturn(mockResponse);

        FlowMgtServerException expectedException = new FlowMgtServerException("Test exception");
        flowMgtUtilsMockedStatic.when(() -> FlowMgtUtils.handleServerException(
                eq(Constants.ErrorMessages.ERROR_CODE_INVOKING_AI_SERVICE),
                eq(TEST_TENANT_DOMAIN)
        )).thenReturn(expectedException);

        assertThrows(FlowMgtServerException.class, () ->
                FlowAIService.getInstance().getFlowGenerationResult(TEST_OPERATION_ID));
    }

    @Test
    public void testGetFlowGenerationResultWithMissingData() throws Exception {

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put(Constants.FlowAIConstants.STATUS, "COMPLETED");

        aiHttpClientUtilMockedStatic.when(() -> AIHttpClientUtil.executeRequest(
                eq(TEST_ENDPOINT),
                eq(TEST_RESULT_PATH + "/" + TEST_OPERATION_ID),
                eq(HttpGet.class),
                eq(null)
        )).thenReturn(mockResponse);

        FlowGenerationResultDTO result = FlowAIService.getInstance().getFlowGenerationResult(TEST_OPERATION_ID);

        assertNotNull(result);
        assertEquals(result.getStatus(), "COMPLETED");
        assertNotNull(result.getData());
        assertTrue(result.getData().isEmpty());
    }

    @Test
    public void testFlowGenerationRequestDTO() {

        FlowGenerationRequestDTO requestDTO = new FlowGenerationRequestDTO.Builder()
                .flowType(TEST_FLOW_TYPE)
                .userQuery(TEST_USER_QUERY)
                .build();

        assertNotNull(requestDTO);
        assertEquals(requestDTO.getFlowType(), TEST_FLOW_TYPE);
        assertEquals(requestDTO.getUserQuery(), TEST_USER_QUERY);
    }

    @Test
    public void testFlowGenerationResponseDTO() {

        FlowGenerationResponseDTO responseDTO = new FlowGenerationResponseDTO.Builder()
                .operationId(TEST_OPERATION_ID)
                .build();

        assertNotNull(responseDTO);
        assertEquals(responseDTO.getOperationId(), TEST_OPERATION_ID);
    }

    @Test
    public void testFlowGenerationStatusDTO() {

        FlowGenerationStatusDTO optimizingStatus = new FlowGenerationStatusDTO.Builder()
                .optimizingQuery(true)
                .fetchingSamples(false)
                .generatingFlow(false)
                .completed(false)
                .build();

        assertNotNull(optimizingStatus);
        assertTrue(optimizingStatus.isOptimizingQuery());
        assertFalse(optimizingStatus.isFetchingSamples());
        assertFalse(optimizingStatus.isGeneratingFlow());
        assertFalse(optimizingStatus.isCompleted());

        FlowGenerationStatusDTO fetchingStatus = new FlowGenerationStatusDTO.Builder()
                .optimizingQuery(false)
                .fetchingSamples(true)
                .generatingFlow(false)
                .completed(false)
                .build();

        assertNotNull(fetchingStatus);
        assertFalse(fetchingStatus.isOptimizingQuery());
        assertTrue(fetchingStatus.isFetchingSamples());
        assertFalse(fetchingStatus.isGeneratingFlow());
        assertFalse(fetchingStatus.isCompleted());

        FlowGenerationStatusDTO generatingStatus = new FlowGenerationStatusDTO.Builder()
                .optimizingQuery(false)
                .fetchingSamples(false)
                .generatingFlow(true)
                .completed(false)
                .build();

        assertNotNull(generatingStatus);
        assertFalse(generatingStatus.isOptimizingQuery());
        assertFalse(generatingStatus.isFetchingSamples());
        assertTrue(generatingStatus.isGeneratingFlow());
        assertFalse(generatingStatus.isCompleted());

        FlowGenerationStatusDTO completedStatus = new FlowGenerationStatusDTO.Builder()
                .optimizingQuery(false)
                .fetchingSamples(false)
                .generatingFlow(false)
                .completed(true)
                .build();

        assertNotNull(completedStatus);
        assertFalse(completedStatus.isOptimizingQuery());
        assertFalse(completedStatus.isFetchingSamples());
        assertFalse(completedStatus.isGeneratingFlow());
        assertTrue(completedStatus.isCompleted());
    }

    @Test
    public void testFlowGenerationStatusDTODefaults() {

        FlowGenerationStatusDTO defaultStatus = new FlowGenerationStatusDTO.Builder().build();

        assertNotNull(defaultStatus);
        assertFalse(defaultStatus.isOptimizingQuery());
        assertFalse(defaultStatus.isFetchingSamples());
        assertFalse(defaultStatus.isGeneratingFlow());
        assertFalse(defaultStatus.isCompleted());
    }

    @Test
    public void testFlowGenerationResultDTO() {

        Map<String, Object> testData = new HashMap<>();
        testData.put("flowId", "test-flow-123");
        testData.put("steps", 5);

        FlowGenerationResultDTO resultDTO = new FlowGenerationResultDTO.Builder()
                .status("COMPLETED")
                .data(testData)
                .build();

        assertNotNull(resultDTO);
        assertEquals(resultDTO.getStatus(), "COMPLETED");
        assertNotNull(resultDTO.getData());
        assertEquals(resultDTO.getData().get("flowId"), "test-flow-123");
        assertEquals(resultDTO.getData().get("steps"), 5);
    }

    @Test
    public void testFlowGenerationResultDTOWithNullValues() {

        FlowGenerationResultDTO resultDTO = new FlowGenerationResultDTO.Builder()
                .status(null)
                .data(null)
                .build();

        assertNotNull(resultDTO);
        assertNull(resultDTO.getStatus());
        assertNull(resultDTO.getData());
    }

    @Test
    public void testFlowGenerationResultDTOWithEmptyData() {

        String status = "IN_PROGRESS";
        Map<String, Object> emptyData = new HashMap<>();

        FlowGenerationResultDTO resultDTO = new FlowGenerationResultDTO.Builder()
                .status(status)
                .data(emptyData)
                .build();

        assertNotNull(resultDTO);
        assertEquals(resultDTO.getStatus(), status);
        assertNotNull(resultDTO.getData());
        assertTrue(resultDTO.getData().isEmpty());
    }

    @Test
    public void testValidFlowTypes() {

        for (Constants.FlowTypes flowType : Constants.FlowTypes.values()) {
            FlowGenerationRequestDTO requestDTO = new FlowGenerationRequestDTO.Builder()
                    .flowType(flowType.getType())
                    .userQuery(TEST_USER_QUERY)
                    .build();

            assertNotNull(requestDTO);
            assertEquals(requestDTO.getFlowType(), flowType.getType());
        }
    }

    @Test
    public void testFlowTypeConstants() {

        assertEquals(Constants.FlowTypes.REGISTRATION.getType(), "REGISTRATION");
        assertEquals(Constants.FlowTypes.PASSWORD_RECOVERY.getType(), "PASSWORD_RECOVERY");
        assertEquals(Constants.FlowTypes.INVITED_USER_REGISTRATION.getType(), "INVITED_USER_REGISTRATION");
    }

    @Test
    public void testFlowAIConstants() {

        assertEquals(Constants.FlowAIConstants.FLOW_TYPE, "flow_type");
        assertEquals(Constants.FlowAIConstants.USER_QUERY, "user_query");
        assertEquals(Constants.FlowAIConstants.OPERATION_ID, "operation_id");
        assertEquals(Constants.FlowAIConstants.STATUS, "status");
        assertEquals(Constants.FlowAIConstants.OPTIMIZING_QUERY, "optimizing_query");
        assertEquals(Constants.FlowAIConstants.FETCHING_SAMPLES, "fetching_samples");
        assertEquals(Constants.FlowAIConstants.GENERATING_FLOW, "generating_flow");
        assertEquals(Constants.FlowAIConstants.COMPLETED, "completed");
        assertEquals(Constants.FlowAIConstants.DATA, "data");
    }

    @Test
    public void testComplexFlowGenerationStatusScenarios() {

        FlowGenerationStatusDTO allActiveStatus = new FlowGenerationStatusDTO.Builder()
                .optimizingQuery(true)
                .fetchingSamples(true)
                .generatingFlow(true)
                .completed(true)
                .build();

        assertNotNull(allActiveStatus);
        assertTrue(allActiveStatus.isOptimizingQuery());
        assertTrue(allActiveStatus.isFetchingSamples());
        assertTrue(allActiveStatus.isGeneratingFlow());
        assertTrue(allActiveStatus.isCompleted());

        FlowGenerationStatusDTO[] phases = {
                new FlowGenerationStatusDTO.Builder().optimizingQuery(true).build(),
                new FlowGenerationStatusDTO.Builder().fetchingSamples(true).build(),
                new FlowGenerationStatusDTO.Builder().generatingFlow(true).build(),
                new FlowGenerationStatusDTO.Builder().completed(true).build()
        };

        for (int i = 0; i < phases.length; i++) {
            assertNotNull(phases[i]);
            int activeCount = 0;
            if (phases[i].isOptimizingQuery()) activeCount++;
            if (phases[i].isFetchingSamples()) activeCount++;
            if (phases[i].isGeneratingFlow()) activeCount++;
            if (phases[i].isCompleted()) activeCount++;
            assertEquals(activeCount, 1, "Phase " + i + " should have exactly one active status");
        }
    }
}
