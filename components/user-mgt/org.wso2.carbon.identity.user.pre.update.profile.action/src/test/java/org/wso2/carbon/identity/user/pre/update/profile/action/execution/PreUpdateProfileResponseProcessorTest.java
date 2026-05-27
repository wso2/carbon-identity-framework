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

package org.wso2.carbon.identity.user.pre.update.profile.action.execution;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionResponseProcessorException;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionResponseContext;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionStatus;
import org.wso2.carbon.identity.action.execution.api.model.ActionInvocationErrorResponse;
import org.wso2.carbon.identity.action.execution.api.model.ActionInvocationFailureResponse;
import org.wso2.carbon.identity.action.execution.api.model.ActionInvocationResponse;
import org.wso2.carbon.identity.action.execution.api.model.ActionInvocationSuccessResponse;
import org.wso2.carbon.identity.action.execution.api.model.ActionType;
import org.wso2.carbon.identity.action.execution.api.model.Error;
import org.wso2.carbon.identity.action.execution.api.model.Event;
import org.wso2.carbon.identity.action.execution.api.model.Failure;
import org.wso2.carbon.identity.action.execution.api.model.FlowContext;
import org.wso2.carbon.identity.action.execution.api.model.Operation;
import org.wso2.carbon.identity.action.execution.api.model.PerformableOperation;
import org.wso2.carbon.identity.action.execution.api.model.ResponseData;
import org.wso2.carbon.identity.action.execution.api.model.Success;
import org.wso2.carbon.identity.action.execution.api.model.User;
import org.wso2.carbon.identity.action.execution.api.util.RequestBuilderUtil;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.Claim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.context.IdentityContext;
import org.wso2.carbon.identity.user.action.api.model.UserActionContext;
import org.wso2.carbon.identity.user.pre.update.profile.action.internal.component.PreUpdateProfileActionServiceComponentHolder;
import org.wso2.carbon.identity.user.pre.update.profile.action.internal.execution.PreUpdateProfileResponseProcessor;
import org.wso2.carbon.identity.user.pre.update.profile.action.internal.model.PreUpdateProfileEvent;
import org.wso2.carbon.user.core.UniqueIDUserStoreManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

/**
 * User Pre Update Password Action Response Processor Test.
 */
@WithCarbonHome
public class PreUpdateProfileResponseProcessorTest {

    private static final String USER_ID = "user1";
    private static final String SINGLE_CLAIM_URI = "http://wso2.org/claims/country";
    private static final String MULTI_CLAIM_URI = "http://wso2.org/claims/mobileNumbers";
    private static final String SCIM_SCHEMA_URI_PREFIX = "urn:ietf:params:scim:schemas";

    private PreUpdateProfileResponseProcessor preUpdateProfileResponseProcessor;
    private ClaimMetadataManagementService claimMetadataManagementService;
    private UniqueIDUserStoreManager userStoreManager;
    private MockedStatic<RequestBuilderUtil> requestBuilderUtil;
    private MockedStatic<FrameworkUtils> frameworkUtils;
    private MockedStatic<LoggerUtils> loggerUtils;

    @BeforeClass
    void setUp() {

        preUpdateProfileResponseProcessor = new PreUpdateProfileResponseProcessor();
    }

    @BeforeMethod
    void setUpMethod() throws Exception {

        IdentityContext.destroyCurrentContext();

        claimMetadataManagementService = mock(ClaimMetadataManagementService.class);
        userStoreManager = mock(UniqueIDUserStoreManager.class);
        requestBuilderUtil = mockStatic(RequestBuilderUtil.class);
        frameworkUtils = mockStatic(FrameworkUtils.class);
        loggerUtils = mockStatic(LoggerUtils.class);
        frameworkUtils.when(FrameworkUtils::getMultiAttributeSeparator).thenReturn(",");
        loggerUtils.when(LoggerUtils::isDiagnosticLogsEnabled).thenReturn(false);
        requestBuilderUtil.when(() -> RequestBuilderUtil.getUserStoreManager(any()))
                .thenReturn(userStoreManager);

        when(claimMetadataManagementService.getMappedExternalClaimsForLocalClaim(any(), any()))
                .thenReturn(Collections.emptyList());
        when(claimMetadataManagementService.getLocalClaim(any(), any()))
                .thenAnswer(invocation -> {
                    String claimUri = invocation.getArgument(0);
                    if (MULTI_CLAIM_URI.equals(claimUri)) {
                        return Optional.of(mockLocalClaim(MULTI_CLAIM_URI, true, false));
                    }
                    return Optional.of(mockLocalClaim(SINGLE_CLAIM_URI, false, false));
                });

        PreUpdateProfileActionServiceComponentHolder.getInstance()
                .setClaimManagementService(claimMetadataManagementService);
    }

    @AfterMethod
    void tearDownMethod() {

        if (requestBuilderUtil != null) {
            requestBuilderUtil.close();
        }
        if (frameworkUtils != null) {
            frameworkUtils.close();
        }
        if (loggerUtils != null) {
            loggerUtils.close();
        }
        IdentityContext.destroyCurrentContext();
    }

    @Test
    public void testGetSupportedActionType() {

        assertEquals(preUpdateProfileResponseProcessor.getSupportedActionType(), ActionType.PRE_UPDATE_PROFILE);
    }

    @Test
    public void testProcessSuccessResponse() throws ActionExecutionResponseProcessorException {

        FlowContext flowContext = FlowContext.create();
        flowContext.add(UserActionContext.USER_ACTION_CONTEXT_REFERENCE_KEY, mock(UserActionContext.class));
        Event mockEvent = mock(Event.class);

        ActionInvocationSuccessResponse successResponse = new ActionInvocationSuccessResponse.Builder()
                .actionStatus(ActionInvocationResponse.Status.SUCCESS)
                .responseData(mock(ResponseData.class))
                .build();

        ActionExecutionStatus<Success> resultStatus =
                preUpdateProfileResponseProcessor.processSuccessResponse(flowContext,
                        ActionExecutionResponseContext.create(mockEvent, successResponse));

        assertNotNull(resultStatus);
        assertEquals(resultStatus.getStatus(), ActionExecutionStatus.Status.SUCCESS);
        assertEquals(resultStatus.getResponseContext().get(UserActionContext.USER_ACTION_CONTEXT_REFERENCE_KEY),
                flowContext.getContextData().get(UserActionContext.USER_ACTION_CONTEXT_REFERENCE_KEY));
    }

    @Test
    public void testProcessFailureResponse() throws ActionExecutionResponseProcessorException {

        FlowContext flowContext = FlowContext.create();
        flowContext.add(UserActionContext.USER_ACTION_CONTEXT_REFERENCE_KEY, mock(UserActionContext.class));
        Event mockEvent = mock(Event.class);

        ActionInvocationFailureResponse failureResponse = new ActionInvocationFailureResponse.Builder()
                .actionStatus(ActionInvocationResponse.Status.FAILED)
                .failureReason("Invalid Request")
                .failureDescription("Unallowed attribute to modify")
                .build();

        ActionExecutionStatus<Failure> resultStatus =
                preUpdateProfileResponseProcessor.processFailureResponse(flowContext,
                        ActionExecutionResponseContext.create(mockEvent, failureResponse));

        assertNotNull(resultStatus);
        assertEquals(resultStatus.getStatus(), ActionExecutionStatus.Status.FAILED);
        assertNotNull(resultStatus.getResponse());
        assertEquals(resultStatus.getResponse().getFailureReason(), failureResponse.getFailureReason());
        assertEquals(resultStatus.getResponse().getFailureDescription(), failureResponse.getFailureDescription());
    }

    @Test
    public void testProcessErrorResponse() throws ActionExecutionResponseProcessorException {

        FlowContext flowContext = FlowContext.create();
        flowContext.add(UserActionContext.USER_ACTION_CONTEXT_REFERENCE_KEY, mock(UserActionContext.class));
        Event mockEvent = mock(Event.class);

        ActionInvocationErrorResponse errorResponse = new ActionInvocationErrorResponse.Builder()
                .errorMessage("Internal Server Error")
                .errorDescription("Error while validating attributes")
                .actionStatus(ActionInvocationResponse.Status.ERROR)
                .build();

        ActionExecutionStatus<Error> resultStatus = preUpdateProfileResponseProcessor.processErrorResponse(flowContext,
                ActionExecutionResponseContext.create(mockEvent, errorResponse));

        assertNotNull(resultStatus);
        assertEquals(resultStatus.getStatus(), ActionExecutionStatus.Status.ERROR);
        assertNotNull(resultStatus.getResponse());
        assertEquals(resultStatus.getResponse().getErrorMessage(), errorResponse.getErrorMessage());
        assertEquals(resultStatus.getResponse().getErrorDescription(), errorResponse.getErrorDescription());
    }

    @Test
    public void testProcessSuccessResponseWhenSingleValuedAddOperation() throws Exception {

        FlowContext flowContext = FlowContext.create();
        flowContext.add(UserActionContext.USER_ACTION_CONTEXT_REFERENCE_KEY, mock(UserActionContext.class));

        when(userStoreManager.getUserClaimValuesWithID(anyString(), any(String[].class), anyString()))
                .thenReturn(new HashMap<>());

        PerformableOperation operation = new PerformableOperation();
        operation.setOp(Operation.ADD);
        operation.setPath("/user/claims[uri=" + SINGLE_CLAIM_URI + "]");
        operation.setValue("Alex");

        ActionInvocationSuccessResponse successResponse = new ActionInvocationSuccessResponse.Builder()
                .actionStatus(ActionInvocationResponse.Status.SUCCESS)
                .operations(Collections.singletonList(operation))
                .responseData(mock(ResponseData.class))
                .build();

        ActionExecutionStatus<Success> resultStatus = preUpdateProfileResponseProcessor.processSuccessResponse(
                flowContext, ActionExecutionResponseContext.create(buildEvent(), successResponse));

        assertNotNull(resultStatus);
        Map<String, String> addedClaims = (Map<String, String>) resultStatus.getResponseContext()
                .get("userClaimsToBeAdded");
        assertEquals(addedClaims.get(SINGLE_CLAIM_URI), "Alex");
    }

    @Test
    public void testProcessSuccessResponseWhenOperationHasCorrectUriPathFormat() throws Exception {

        FlowContext flowContext = FlowContext.create();
        flowContext.add(UserActionContext.USER_ACTION_CONTEXT_REFERENCE_KEY, mock(UserActionContext.class));

        when(userStoreManager.getUserClaimValuesWithID(anyString(), any(String[].class), anyString()))
                .thenReturn(new HashMap<>());

        String validPath = "/user/claims[uri=http://wso2.org/claims/country]";
        PerformableOperation operation = new PerformableOperation();
        operation.setOp(Operation.ADD);
        operation.setPath(validPath);
        operation.setValue("Sri Lanka");

        ActionInvocationSuccessResponse successResponse = new ActionInvocationSuccessResponse.Builder()
                .actionStatus(ActionInvocationResponse.Status.SUCCESS)
                .operations(Collections.singletonList(operation))
                .responseData(mock(ResponseData.class))
                .build();

        ActionExecutionStatus<Success> resultStatus = preUpdateProfileResponseProcessor.processSuccessResponse(
                flowContext, ActionExecutionResponseContext.create(buildEvent(), successResponse));

        assertNotNull(resultStatus);
        Map<String, String> addedClaims = (Map<String, String>) resultStatus.getResponseContext()
                .get("userClaimsToBeAdded");
        assertEquals(addedClaims.get(SINGLE_CLAIM_URI), "Sri Lanka");
    }

    @Test
    public void testProcessSuccessResponseWhenSingleValuedReplaceOperation() throws Exception {

        FlowContext flowContext = FlowContext.create();
        flowContext.add(UserActionContext.USER_ACTION_CONTEXT_REFERENCE_KEY, mock(UserActionContext.class));

        when(userStoreManager.getUserClaimValuesWithID(anyString(), any(String[].class), anyString()))
                .thenReturn(new HashMap<>());

        PerformableOperation operation = new PerformableOperation();
        operation.setOp(Operation.REPLACE);
        operation.setPath("/user/claims[uri=" + SINGLE_CLAIM_URI + "]");
        operation.setValue("AlexModified");

        ActionInvocationSuccessResponse successResponse = new ActionInvocationSuccessResponse.Builder()
                .actionStatus(ActionInvocationResponse.Status.SUCCESS)
                .operations(Collections.singletonList(operation))
                .responseData(mock(ResponseData.class))
                .build();

        ActionExecutionStatus<Success> resultStatus = preUpdateProfileResponseProcessor.processSuccessResponse(
                flowContext, ActionExecutionResponseContext.create(buildEvent(), successResponse));

        assertNotNull(resultStatus);
        Map<String, String> modifiedClaims = (Map<String, String>) resultStatus.getResponseContext()
                .get("userClaimsToBeModified");
        assertEquals(modifiedClaims.get(SINGLE_CLAIM_URI), "AlexModified");
    }

    @Test
    public void testProcessSuccessResponseWhenSingleValuedRemoveOperation() throws Exception {

        FlowContext flowContext = FlowContext.create();
        flowContext.add(UserActionContext.USER_ACTION_CONTEXT_REFERENCE_KEY, mock(UserActionContext.class));

        PerformableOperation operation = new PerformableOperation();
        operation.setOp(Operation.REMOVE);
        operation.setPath("/user/claims[uri=" + SINGLE_CLAIM_URI + "]");

        ActionInvocationSuccessResponse successResponse = new ActionInvocationSuccessResponse.Builder()
                .actionStatus(ActionInvocationResponse.Status.SUCCESS)
                .operations(Collections.singletonList(operation))
                .responseData(mock(ResponseData.class))
                .build();

        ActionExecutionStatus<Success> resultStatus = preUpdateProfileResponseProcessor.processSuccessResponse(
                flowContext, ActionExecutionResponseContext.create(buildEvent(), successResponse));

        assertNotNull(resultStatus);
        Map<String, String> removedClaims = (Map<String, String>) resultStatus.getResponseContext()
                .get("userClaimsToBeRemoved");
        assertEquals(removedClaims.get(SINGLE_CLAIM_URI), "");
    }

    @Test
    public void testProcessSuccessResponseWhenMultiValuedReplaceOperation() throws Exception {

        FlowContext flowContext = FlowContext.create();
        flowContext.add(UserActionContext.USER_ACTION_CONTEXT_REFERENCE_KEY, mock(UserActionContext.class));

        Map<String, String> existingClaims = new HashMap<>();
        existingClaims.put(MULTI_CLAIM_URI, "0771234567,0717654329");
        when(userStoreManager.getUserClaimValuesWithID(anyString(), any(String[].class), anyString()))
                .thenReturn(existingClaims);

        PerformableOperation operation = new PerformableOperation();
        operation.setOp(Operation.REPLACE);
        operation.setPath("/user/claims[uri=" + MULTI_CLAIM_URI + "]");
        operation.setValue(Arrays.asList("0771234567", "0709998888"));

        ActionInvocationSuccessResponse successResponse = new ActionInvocationSuccessResponse.Builder()
                .actionStatus(ActionInvocationResponse.Status.SUCCESS)
                .operations(Collections.singletonList(operation))
                .responseData(mock(ResponseData.class))
                .build();

        ActionExecutionStatus<Success> resultStatus = preUpdateProfileResponseProcessor.processSuccessResponse(
                flowContext,
                ActionExecutionResponseContext.create(buildEvent(PreUpdateProfileEvent.FlowInitiatorType.USER),
                        successResponse));

        Map<String, String> modifiedClaims = (Map<String, String>) resultStatus.getResponseContext()
                .get("userClaimsToBeModified");
        assertEquals(modifiedClaims.get(MULTI_CLAIM_URI), "0771234567,0709998888");
    }

    @Test
    public void testProcessSuccessResponseWhenMultiValuedReplaceOperationByAdmin() throws Exception {

        FlowContext flowContext = FlowContext.create();
        flowContext.add(UserActionContext.USER_ACTION_CONTEXT_REFERENCE_KEY, mock(UserActionContext.class));

        Map<String, String> existingClaims = new HashMap<>();
        existingClaims.put(MULTI_CLAIM_URI, "0771234567,0717654329");
        when(userStoreManager.getUserClaimValuesWithID(anyString(), any(String[].class), anyString()))
                .thenReturn(existingClaims);

        PerformableOperation operation = new PerformableOperation();
        operation.setOp(Operation.REPLACE);
        operation.setPath("/user/claims[uri=" + MULTI_CLAIM_URI + "]");
        operation.setValue(Arrays.asList("0771234567", "0709998888"));

        ActionInvocationSuccessResponse successResponse = new ActionInvocationSuccessResponse.Builder()
                .actionStatus(ActionInvocationResponse.Status.SUCCESS)
                .operations(Collections.singletonList(operation))
                .responseData(mock(ResponseData.class))
                .build();

        ActionExecutionStatus<Success> resultStatus = preUpdateProfileResponseProcessor.processSuccessResponse(
                flowContext,
                ActionExecutionResponseContext.create(buildEvent(PreUpdateProfileEvent.FlowInitiatorType.ADMIN),
                        successResponse));

        Map<?, ?> addedClaims = (Map<?, ?>) resultStatus.getResponseContext()
                .get("multiValuedClaimsToBeAdded");
        Map<?, ?> removedClaims = (Map<?, ?>) resultStatus.getResponseContext()
                .get("multiValuedClaimsToBeRemoved");

        assertEquals(addedClaims.get(MULTI_CLAIM_URI), Collections.singletonList("0709998888"));
        assertEquals(removedClaims.get(MULTI_CLAIM_URI), Collections.singletonList("0717654329"));
    }

    @Test
    public void testProcessSuccessResponseWhenMultiValuedRemoveOperation() throws Exception {

        FlowContext flowContext = FlowContext.create();
        flowContext.add(UserActionContext.USER_ACTION_CONTEXT_REFERENCE_KEY, mock(UserActionContext.class));

        PerformableOperation operation = new PerformableOperation();
        operation.setOp(Operation.REMOVE);
        operation.setPath("/user/claims[uri=" + MULTI_CLAIM_URI + "]");

        ActionInvocationSuccessResponse successResponse = new ActionInvocationSuccessResponse.Builder()
                .actionStatus(ActionInvocationResponse.Status.SUCCESS)
                .operations(Collections.singletonList(operation))
                .responseData(mock(ResponseData.class))
                .build();

        ActionExecutionStatus<Success> resultStatus = preUpdateProfileResponseProcessor.processSuccessResponse(
                flowContext, ActionExecutionResponseContext.create(buildEvent(), successResponse));

        assertNotNull(resultStatus);
        Map<String, String> removedClaims = (Map<String, String>) resultStatus.getResponseContext()
                .get("userClaimsToBeRemoved");
        assertEquals(removedClaims.get(MULTI_CLAIM_URI), "");
    }

    @Test
    public void testProcessSuccessResponseWhenMultiValuedRemoveOperationWithSpecificValue() throws Exception {

        FlowContext flowContext = FlowContext.create();
        flowContext.add(UserActionContext.USER_ACTION_CONTEXT_REFERENCE_KEY, mock(UserActionContext.class));

        PerformableOperation operation = new PerformableOperation();
        operation.setOp(Operation.REMOVE);
        operation.setPath("/user/claims[uri=" + MULTI_CLAIM_URI + "]");
        operation.setValue("0717654329");

        ActionInvocationSuccessResponse successResponse = new ActionInvocationSuccessResponse.Builder()
                .actionStatus(ActionInvocationResponse.Status.SUCCESS)
                .operations(Collections.singletonList(operation))
                .responseData(mock(ResponseData.class))
                .build();

        ActionExecutionStatus<Success> resultStatus = preUpdateProfileResponseProcessor.processSuccessResponse(
                flowContext, ActionExecutionResponseContext.create(buildEvent(), successResponse));

        assertNotNull(resultStatus);
        assertTrue(((Map<?, ?>) resultStatus.getResponseContext().get("userClaimsToBeRemoved")).isEmpty());
    }

    @Test
    public void testProcessSuccessResponseWhenInvalidOperationFormat() throws Exception {

        FlowContext flowContext = FlowContext.create();
        flowContext.add(UserActionContext.USER_ACTION_CONTEXT_REFERENCE_KEY, mock(UserActionContext.class));

        PerformableOperation operation = new PerformableOperation();
        operation.setOp(Operation.ADD);
        operation.setPath("/invalid");
        operation.setValue("value");

        ActionInvocationSuccessResponse successResponse = new ActionInvocationSuccessResponse.Builder()
                .actionStatus(ActionInvocationResponse.Status.SUCCESS)
                .operations(Collections.singletonList(operation))
                .responseData(mock(ResponseData.class))
                .build();

        ActionExecutionStatus<Success> resultStatus = preUpdateProfileResponseProcessor.processSuccessResponse(
                flowContext, ActionExecutionResponseContext.create(buildEvent(), successResponse));

        assertNotNull(resultStatus);
        assertTrue(((Map<?, ?>) resultStatus.getResponseContext().get("userClaimsToBeAdded")).isEmpty());
        assertTrue(((Map<?, ?>) resultStatus.getResponseContext().get("userClaimsToBeModified")).isEmpty());
        assertTrue(((Map<?, ?>) resultStatus.getResponseContext().get("userClaimsToBeRemoved")).isEmpty());
    }

    @Test
    public void testProcessSuccessResponseWhenSingleValuedAddOperationWithPathHasQuotedUri() throws Exception {

        FlowContext flowContext = FlowContext.create();
        flowContext.add(UserActionContext.USER_ACTION_CONTEXT_REFERENCE_KEY, mock(UserActionContext.class));

        PerformableOperation operation = new PerformableOperation();
        operation.setOp(Operation.ADD);
        operation.setPath("/user/claims[uri=\"" + SINGLE_CLAIM_URI + "\"]");
        operation.setValue("Alex");

        ActionInvocationSuccessResponse successResponse = new ActionInvocationSuccessResponse.Builder()
                .actionStatus(ActionInvocationResponse.Status.SUCCESS)
                .operations(Collections.singletonList(operation))
                .responseData(mock(ResponseData.class))
                .build();

        ActionExecutionStatus<Success> resultStatus = preUpdateProfileResponseProcessor.processSuccessResponse(
                flowContext, ActionExecutionResponseContext.create(buildEvent(), successResponse));

        assertTrue(((Map<?, ?>) resultStatus.getResponseContext().get("userClaimsToBeAdded")).isEmpty());
    }

    @Test
    public void testProcessSuccessResponseWhenSingleValuedAddOperationWithPathHasTrailingChars() throws Exception {

        FlowContext flowContext = FlowContext.create();
        flowContext.add(UserActionContext.USER_ACTION_CONTEXT_REFERENCE_KEY, mock(UserActionContext.class));

        PerformableOperation operation = new PerformableOperation();
        operation.setOp(Operation.ADD);
        operation.setPath("/user/claims[uri=" + SINGLE_CLAIM_URI + "]abc");
        operation.setValue("Alex");

        ActionInvocationSuccessResponse successResponse = new ActionInvocationSuccessResponse.Builder()
                .actionStatus(ActionInvocationResponse.Status.SUCCESS)
                .operations(Collections.singletonList(operation))
                .responseData(mock(ResponseData.class))
                .build();

        ActionExecutionStatus<Success> resultStatus = preUpdateProfileResponseProcessor.processSuccessResponse(
                flowContext, ActionExecutionResponseContext.create(buildEvent(), successResponse));

        assertTrue(((Map<?, ?>) resultStatus.getResponseContext().get("userClaimsToBeAdded")).isEmpty());
    }

    @Test
    public void testProcessSuccessResponseWhenSingleValuedAddOperationWithValueMap() throws Exception {

        FlowContext flowContext = FlowContext.create();
        flowContext.add(UserActionContext.USER_ACTION_CONTEXT_REFERENCE_KEY, mock(UserActionContext.class));

        when(userStoreManager.getUserClaimValuesWithID(anyString(), any(String[].class), anyString()))
                .thenReturn(new HashMap<>());

        LinkedHashMap<String, Object> valueMap = new LinkedHashMap<>();
        valueMap.put("uri", SINGLE_CLAIM_URI);
        valueMap.put("value", "Colombo");

        PerformableOperation operation = new PerformableOperation();
        operation.setOp(Operation.ADD);
        operation.setPath("/invalid");
        operation.setValue(valueMap);

        ActionInvocationSuccessResponse successResponse = new ActionInvocationSuccessResponse.Builder()
                .actionStatus(ActionInvocationResponse.Status.SUCCESS)
                .operations(Collections.singletonList(operation))
                .responseData(mock(ResponseData.class))
                .build();

        ActionExecutionStatus<Success> resultStatus = preUpdateProfileResponseProcessor.processSuccessResponse(
                flowContext, ActionExecutionResponseContext.create(buildEvent(), successResponse));

        Map<String, String> addedClaims = (Map<String, String>) resultStatus.getResponseContext()
                .get("userClaimsToBeAdded");
        assertEquals(addedClaims.get(SINGLE_CLAIM_URI), "Colombo");
    }

    @Test
    public void testProcessSuccessResponseWhenSingleValuedAddOperationWithValueMapWithoutUri() throws Exception {

        FlowContext flowContext = FlowContext.create();
        flowContext.add(UserActionContext.USER_ACTION_CONTEXT_REFERENCE_KEY, mock(UserActionContext.class));

        LinkedHashMap<String, Object> valueMap = new LinkedHashMap<>();
        valueMap.put("value", "Colombo");

        PerformableOperation operation = new PerformableOperation();
        operation.setOp(Operation.ADD);
        operation.setPath("/invalid");
        operation.setValue(valueMap);

        ActionInvocationSuccessResponse successResponse = new ActionInvocationSuccessResponse.Builder()
                .actionStatus(ActionInvocationResponse.Status.SUCCESS)
                .operations(Collections.singletonList(operation))
                .responseData(mock(ResponseData.class))
                .build();

        ActionExecutionStatus<Success> resultStatus = preUpdateProfileResponseProcessor.processSuccessResponse(
                flowContext, ActionExecutionResponseContext.create(buildEvent(), successResponse));

        assertTrue(((Map<?, ?>) resultStatus.getResponseContext().get("userClaimsToBeAdded")).isEmpty());
    }

    @Test
    public void testProcessSuccessResponseWhenGroupClaimIsModified() throws Exception {

        FlowContext flowContext = FlowContext.create();
        flowContext.add(UserActionContext.USER_ACTION_CONTEXT_REFERENCE_KEY, mock(UserActionContext.class));

        String groupClaim = "http://wso2.org/claims/groups";
        LocalClaim localClaim = mockLocalClaim(groupClaim, true, false);
        when(claimMetadataManagementService.getLocalClaim(eq(groupClaim), any()))
                .thenReturn(Optional.of(localClaim));

        PerformableOperation operation = new PerformableOperation();
        operation.setOp(Operation.REPLACE);
        operation.setPath("/user/claims[uri=" + groupClaim + "]");
        operation.setValue(Arrays.asList("g1", "g2"));

        ActionInvocationSuccessResponse successResponse = new ActionInvocationSuccessResponse.Builder()
                .actionStatus(ActionInvocationResponse.Status.SUCCESS)
                .operations(Collections.singletonList(operation))
                .responseData(mock(ResponseData.class))
                .build();

        ActionExecutionStatus<Success> resultStatus = preUpdateProfileResponseProcessor.processSuccessResponse(
                flowContext, ActionExecutionResponseContext.create(buildEvent(), successResponse));

        assertTrue(((Map<?, ?>) resultStatus.getResponseContext().get("userClaimsToBeModified")).isEmpty());
        assertTrue(((Map<?, ?>) resultStatus.getResponseContext().get("multiValuedClaimsToBeAdded")).isEmpty());
    }

    @Test
    public void testProcessSuccessResponseWhenFlowInitiatorClaimIsModified() throws Exception {

        FlowContext flowContext = FlowContext.create();
        flowContext.add(UserActionContext.USER_ACTION_CONTEXT_REFERENCE_KEY, mock(UserActionContext.class));

        String flowInitiatorClaim = "http://wso2.org/claims/flowInitiator";
        LocalClaim localClaim = mockLocalClaim(flowInitiatorClaim, false, true);
        when(claimMetadataManagementService.getLocalClaim(eq(flowInitiatorClaim), any()))
                .thenReturn(Optional.of(localClaim));

        PerformableOperation operation = new PerformableOperation();
        operation.setOp(Operation.REPLACE);
        operation.setPath("/user/claims[uri=" + flowInitiatorClaim + "]");
        operation.setValue("true");

        ActionInvocationSuccessResponse successResponse = new ActionInvocationSuccessResponse.Builder()
                .actionStatus(ActionInvocationResponse.Status.SUCCESS)
                .operations(Collections.singletonList(operation))
                .responseData(mock(ResponseData.class))
                .build();

        ActionExecutionStatus<Success> resultStatus = preUpdateProfileResponseProcessor.processSuccessResponse(
                flowContext, ActionExecutionResponseContext.create(buildEvent(), successResponse));

        assertTrue(((Map<?, ?>) resultStatus.getResponseContext().get("userClaimsToBeModified")).isEmpty());
    }

    @Test
    public void testProcessSuccessResponseWhenMultiValuedReplaceOperationWithReducedArray() throws Exception {

        FlowContext flowContext = FlowContext.create();
        flowContext.add(UserActionContext.USER_ACTION_CONTEXT_REFERENCE_KEY, mock(UserActionContext.class));

        Map<String, String> existingClaims = new HashMap<>();
        existingClaims.put(MULTI_CLAIM_URI, "0771234567,0717654329");
        when(userStoreManager.getUserClaimValuesWithID(anyString(), any(String[].class), anyString()))
                .thenReturn(existingClaims);

        PerformableOperation operation = new PerformableOperation();
        operation.setOp(Operation.REPLACE);
        operation.setPath("/user/claims[uri=" + MULTI_CLAIM_URI + "]");
        operation.setValue(Collections.singletonList("0771234567"));

        ActionInvocationSuccessResponse successResponse = new ActionInvocationSuccessResponse.Builder()
                .actionStatus(ActionInvocationResponse.Status.SUCCESS)
                .operations(Collections.singletonList(operation))
                .responseData(mock(ResponseData.class))
                .build();

        ActionExecutionStatus<Success> resultStatus = preUpdateProfileResponseProcessor.processSuccessResponse(
                flowContext,
                ActionExecutionResponseContext.create(buildEvent(PreUpdateProfileEvent.FlowInitiatorType.ADMIN),
                        successResponse));

        assertTrue(((Map<?, ?>) resultStatus.getResponseContext().get("multiValuedClaimsToBeAdded")).isEmpty());
        assertTrue(((Map<?, ?>) resultStatus.getResponseContext().get("multiValuedClaimsToBeRemoved")).isEmpty());
    }

    @Test
    public void testProcessSuccessResponseWhenScimReadOnlyAttributeReplaceOperation() throws Exception {

        FlowContext flowContext = FlowContext.create();
        flowContext.add(UserActionContext.USER_ACTION_CONTEXT_REFERENCE_KEY, mock(UserActionContext.class));

        when(claimMetadataManagementService.getMappedExternalClaimsForLocalClaim(eq(SINGLE_CLAIM_URI), any()))
                .thenReturn(Collections.singletonList(mockScimClaim(true, false, false)));

        PerformableOperation operation = new PerformableOperation();
        operation.setOp(Operation.REPLACE);
        operation.setPath("/user/claims[uri=" + SINGLE_CLAIM_URI + "]");
        operation.setValue("Updated");

        ActionInvocationSuccessResponse successResponse = new ActionInvocationSuccessResponse.Builder()
                .actionStatus(ActionInvocationResponse.Status.SUCCESS)
                .operations(Collections.singletonList(operation))
                .responseData(mock(ResponseData.class))
                .build();

        ActionExecutionStatus<Success> resultStatus = preUpdateProfileResponseProcessor.processSuccessResponse(
                flowContext, ActionExecutionResponseContext.create(buildEvent(), successResponse));

        assertTrue(((Map<?, ?>) resultStatus.getResponseContext().get("userClaimsToBeModified")).isEmpty());
    }

    @Test
    public void testProcessSuccessResponseWhenScimRequiredAttributeRemoveOperation() throws Exception {

        FlowContext flowContext = FlowContext.create();
        flowContext.add(UserActionContext.USER_ACTION_CONTEXT_REFERENCE_KEY, mock(UserActionContext.class));

        when(claimMetadataManagementService.getMappedExternalClaimsForLocalClaim(eq(SINGLE_CLAIM_URI), any()))
                .thenReturn(Collections.singletonList(mockScimClaim(false, true, false)));

        PerformableOperation operation = new PerformableOperation();
        operation.setOp(Operation.REMOVE);
        operation.setPath("/user/claims[uri=" + SINGLE_CLAIM_URI + "]");

        ActionInvocationSuccessResponse successResponse = new ActionInvocationSuccessResponse.Builder()
                .actionStatus(ActionInvocationResponse.Status.SUCCESS)
                .operations(Collections.singletonList(operation))
                .responseData(mock(ResponseData.class))
                .build();

        ActionExecutionStatus<Success> resultStatus = preUpdateProfileResponseProcessor.processSuccessResponse(
                flowContext, ActionExecutionResponseContext.create(buildEvent(), successResponse));

        assertTrue(((Map<?, ?>) resultStatus.getResponseContext().get("userClaimsToBeRemoved")).isEmpty());
    }

    @Test
    public void testProcessSuccessResponseWhenScimRequiredAttributeReplaceWithEmptyValue() throws Exception {

        FlowContext flowContext = FlowContext.create();
        flowContext.add(UserActionContext.USER_ACTION_CONTEXT_REFERENCE_KEY, mock(UserActionContext.class));

        when(claimMetadataManagementService.getMappedExternalClaimsForLocalClaim(eq(SINGLE_CLAIM_URI), any()))
                .thenReturn(Collections.singletonList(mockScimClaim(false, true, false)));

        PerformableOperation operation = new PerformableOperation();
        operation.setOp(Operation.REPLACE);
        operation.setPath("/user/claims[uri=" + SINGLE_CLAIM_URI + "]");
        operation.setValue("   ");

        ActionInvocationSuccessResponse successResponse = new ActionInvocationSuccessResponse.Builder()
                .actionStatus(ActionInvocationResponse.Status.SUCCESS)
                .operations(Collections.singletonList(operation))
                .responseData(mock(ResponseData.class))
                .build();

        ActionExecutionStatus<Success> resultStatus = preUpdateProfileResponseProcessor.processSuccessResponse(
                flowContext, ActionExecutionResponseContext.create(buildEvent(), successResponse));

        assertTrue(((Map<?, ?>) resultStatus.getResponseContext().get("userClaimsToBeModified")).isEmpty());
    }

    @Test
    public void testProcessSuccessResponseWhenScimSingleValuedAttributeReplaceWithMultipleValues() throws Exception {

        FlowContext flowContext = FlowContext.create();
        flowContext.add(UserActionContext.USER_ACTION_CONTEXT_REFERENCE_KEY, mock(UserActionContext.class));

        when(claimMetadataManagementService.getMappedExternalClaimsForLocalClaim(eq(SINGLE_CLAIM_URI), any()))
                .thenReturn(Collections.singletonList(mockScimClaim(false, false, false)));

        PerformableOperation operation = new PerformableOperation();
        operation.setOp(Operation.REPLACE);
        operation.setPath("/user/claims[uri=" + SINGLE_CLAIM_URI + "]");
        operation.setValue(Arrays.asList("value1", "value2"));

        ActionInvocationSuccessResponse successResponse = new ActionInvocationSuccessResponse.Builder()
                .actionStatus(ActionInvocationResponse.Status.SUCCESS)
                .operations(Collections.singletonList(operation))
                .responseData(mock(ResponseData.class))
                .build();

        ActionExecutionStatus<Success> resultStatus = preUpdateProfileResponseProcessor.processSuccessResponse(
                flowContext, ActionExecutionResponseContext.create(buildEvent(), successResponse));

        assertTrue(((Map<?, ?>) resultStatus.getResponseContext().get("userClaimsToBeModified")).isEmpty());
    }

    @Test
    public void testProcessSuccessResponseWhenSingleValuedReplaceOperationWithValueMapWithoutValue() throws Exception {

        FlowContext flowContext = FlowContext.create();
        flowContext.add(UserActionContext.USER_ACTION_CONTEXT_REFERENCE_KEY, mock(UserActionContext.class));

        LinkedHashMap<String, Object> valueMap = new LinkedHashMap<>();
        valueMap.put("uri", SINGLE_CLAIM_URI);

        PerformableOperation operation = new PerformableOperation();
        operation.setOp(Operation.REPLACE);
        operation.setPath("/user/claims[uri=" + SINGLE_CLAIM_URI + "]");
        operation.setValue(valueMap);

        ActionInvocationSuccessResponse successResponse = new ActionInvocationSuccessResponse.Builder()
                .actionStatus(ActionInvocationResponse.Status.SUCCESS)
                .operations(Collections.singletonList(operation))
                .responseData(mock(ResponseData.class))
                .build();

        ActionExecutionStatus<Success> resultStatus = preUpdateProfileResponseProcessor.processSuccessResponse(
                flowContext, ActionExecutionResponseContext.create(buildEvent(), successResponse));

        assertTrue(((Map<?, ?>) resultStatus.getResponseContext().get("userClaimsToBeModified")).isEmpty());
    }

    @Test
    public void testProcessSuccessResponseWhenMultiValuedReplaceOperationWithInvalidListValueType() throws Exception {

        FlowContext flowContext = FlowContext.create();
        flowContext.add(UserActionContext.USER_ACTION_CONTEXT_REFERENCE_KEY, mock(UserActionContext.class));

        LinkedHashMap<String, Object> valueMap = new LinkedHashMap<>();
        valueMap.put("value", Arrays.asList("0771234567", 123));

        PerformableOperation operation = new PerformableOperation();
        operation.setOp(Operation.REPLACE);
        operation.setPath("/user/claims[uri=" + MULTI_CLAIM_URI + "]");
        operation.setValue(valueMap);

        ActionInvocationSuccessResponse successResponse = new ActionInvocationSuccessResponse.Builder()
                .actionStatus(ActionInvocationResponse.Status.SUCCESS)
                .operations(Collections.singletonList(operation))
                .responseData(mock(ResponseData.class))
                .build();

        ActionExecutionStatus<Success> resultStatus = preUpdateProfileResponseProcessor.processSuccessResponse(
                flowContext, ActionExecutionResponseContext.create(buildEvent(), successResponse));

        assertTrue(((Map<?, ?>) resultStatus.getResponseContext().get("multiValuedClaimsToBeAdded")).isEmpty());
        assertTrue(((Map<?, ?>) resultStatus.getResponseContext().get("multiValuedClaimsToBeRemoved")).isEmpty());
    }

    @Test
    public void testProcessSuccessResponseWhenMultiValuedAddOperationWithInvalidListValueType() throws Exception {

        FlowContext flowContext = FlowContext.create();
        flowContext.add(UserActionContext.USER_ACTION_CONTEXT_REFERENCE_KEY, mock(UserActionContext.class));

        LinkedHashMap<String, Object> valueMap = new LinkedHashMap<>();
        valueMap.put("value", Arrays.asList("0771234567", 123));

        PerformableOperation operation = new PerformableOperation();
        operation.setOp(Operation.ADD);
        operation.setPath("/user/claims[uri=" + MULTI_CLAIM_URI + "]");
        operation.setValue(valueMap);

        ActionInvocationSuccessResponse successResponse = new ActionInvocationSuccessResponse.Builder()
                .actionStatus(ActionInvocationResponse.Status.SUCCESS)
                .operations(Collections.singletonList(operation))
                .responseData(mock(ResponseData.class))
                .build();

        ActionExecutionStatus<Success> resultStatus = preUpdateProfileResponseProcessor.processSuccessResponse(
                flowContext, ActionExecutionResponseContext.create(buildEvent(), successResponse));

        assertTrue(((Map<?, ?>) resultStatus.getResponseContext().get("multiValuedClaimsToBeAdded")).isEmpty());
    }

    @Test
    public void testProcessSuccessResponseWhenScimRequiredAttributeAddWithEmptyMapValue() throws Exception {

        FlowContext flowContext = FlowContext.create();
        flowContext.add(UserActionContext.USER_ACTION_CONTEXT_REFERENCE_KEY, mock(UserActionContext.class));

        when(claimMetadataManagementService.getMappedExternalClaimsForLocalClaim(eq(SINGLE_CLAIM_URI), any()))
                .thenReturn(Collections.singletonList(mockScimClaim(false, true, false)));

        LinkedHashMap<String, Object> valueMap = new LinkedHashMap<>();
        valueMap.put("value", "   ");

        PerformableOperation operation = new PerformableOperation();
        operation.setOp(Operation.ADD);
        operation.setPath("/user/claims[uri=" + SINGLE_CLAIM_URI + "]");
        operation.setValue(valueMap);

        ActionInvocationSuccessResponse successResponse = new ActionInvocationSuccessResponse.Builder()
                .actionStatus(ActionInvocationResponse.Status.SUCCESS)
                .operations(Collections.singletonList(operation))
                .responseData(mock(ResponseData.class))
                .build();

        ActionExecutionStatus<Success> resultStatus = preUpdateProfileResponseProcessor.processSuccessResponse(
                flowContext, ActionExecutionResponseContext.create(buildEvent(), successResponse));

        assertTrue(((Map<?, ?>) resultStatus.getResponseContext().get("userClaimsToBeAdded")).isEmpty());
    }

    @Test
    public void testProcessSuccessResponseWhenScimSingleValuedAttributeReplaceWithMapListValues() throws Exception {

        FlowContext flowContext = FlowContext.create();
        flowContext.add(UserActionContext.USER_ACTION_CONTEXT_REFERENCE_KEY, mock(UserActionContext.class));

        when(claimMetadataManagementService.getMappedExternalClaimsForLocalClaim(eq(SINGLE_CLAIM_URI), any()))
                .thenReturn(Collections.singletonList(mockScimClaim(false, false, false)));

        LinkedHashMap<String, Object> valueMap = new LinkedHashMap<>();
        valueMap.put("value", Arrays.asList("value1", "value2"));

        PerformableOperation operation = new PerformableOperation();
        operation.setOp(Operation.REPLACE);
        operation.setPath("/user/claims[uri=" + SINGLE_CLAIM_URI + "]");
        operation.setValue(valueMap);

        ActionInvocationSuccessResponse successResponse = new ActionInvocationSuccessResponse.Builder()
                .actionStatus(ActionInvocationResponse.Status.SUCCESS)
                .operations(Collections.singletonList(operation))
                .responseData(mock(ResponseData.class))
                .build();

        ActionExecutionStatus<Success> resultStatus = preUpdateProfileResponseProcessor.processSuccessResponse(
                flowContext, ActionExecutionResponseContext.create(buildEvent(), successResponse));

        assertTrue(((Map<?, ?>) resultStatus.getResponseContext().get("userClaimsToBeModified")).isEmpty());
    }

    @Test
    public void testProcessSuccessResponseWhenScimMappingsAreNotScimDialect() throws Exception {

        FlowContext flowContext = FlowContext.create();
        flowContext.add(UserActionContext.USER_ACTION_CONTEXT_REFERENCE_KEY, mock(UserActionContext.class));

        Claim nonScimClaim = new Claim("custom:dialect", SINGLE_CLAIM_URI);
        when(claimMetadataManagementService.getMappedExternalClaimsForLocalClaim(eq(SINGLE_CLAIM_URI), any()))
                .thenReturn(Collections.singletonList(nonScimClaim));
        when(userStoreManager.getUserClaimValuesWithID(anyString(), any(String[].class), anyString()))
                .thenReturn(new HashMap<>());

        PerformableOperation operation = new PerformableOperation();
        operation.setOp(Operation.ADD);
        operation.setPath("/user/claims[uri=" + SINGLE_CLAIM_URI + "]");
        operation.setValue("NoScimValidation");

        ActionInvocationSuccessResponse successResponse = new ActionInvocationSuccessResponse.Builder()
                .actionStatus(ActionInvocationResponse.Status.SUCCESS)
                .operations(Collections.singletonList(operation))
                .responseData(mock(ResponseData.class))
                .build();

        ActionExecutionStatus<Success> resultStatus = preUpdateProfileResponseProcessor.processSuccessResponse(
                flowContext, ActionExecutionResponseContext.create(buildEvent(), successResponse));

        Map<String, String> addedClaims = (Map<String, String>) resultStatus.getResponseContext()
                .get("userClaimsToBeAdded");
        assertEquals(addedClaims.get(SINGLE_CLAIM_URI), "NoScimValidation");
    }

    @Test
    public void testProcessSuccessResponseFailureWhenUserStoreManagerLoadingFails() {

        FlowContext flowContext = FlowContext.create();
        flowContext.add(UserActionContext.USER_ACTION_CONTEXT_REFERENCE_KEY, mock(UserActionContext.class));

        requestBuilderUtil.when(() -> RequestBuilderUtil.getUserStoreManager(any()))
                .thenThrow(new org.wso2.carbon.identity.action.execution.api.exception
                        .ActionExecutionRequestBuilderException("User store manager error"));

        ActionInvocationSuccessResponse successResponse = new ActionInvocationSuccessResponse.Builder()
                .actionStatus(ActionInvocationResponse.Status.SUCCESS)
                .operations(Collections.singletonList(new PerformableOperation()))
                .responseData(mock(ResponseData.class))
                .build();

        expectThrows(ActionExecutionResponseProcessorException.class,
                () -> preUpdateProfileResponseProcessor.processSuccessResponse(
                        flowContext, ActionExecutionResponseContext.create(buildEvent(), successResponse)));
    }

    @Test
    public void testProcessSuccessResponseWhenPathMissingClosingBracket() throws Exception {

        FlowContext flowContext = FlowContext.create();
        flowContext.add(UserActionContext.USER_ACTION_CONTEXT_REFERENCE_KEY, mock(UserActionContext.class));

        PerformableOperation operation = new PerformableOperation();
        operation.setOp(Operation.REPLACE);
        operation.setPath("/user/claims[uri=" + SINGLE_CLAIM_URI);
        operation.setValue("Alex");

        ActionInvocationSuccessResponse successResponse = new ActionInvocationSuccessResponse.Builder()
                .actionStatus(ActionInvocationResponse.Status.SUCCESS)
                .operations(Collections.singletonList(operation))
                .responseData(mock(ResponseData.class))
                .build();

        ActionExecutionStatus<Success> resultStatus = preUpdateProfileResponseProcessor.processSuccessResponse(
                flowContext, ActionExecutionResponseContext.create(buildEvent(), successResponse));

        assertTrue(((Map<?, ?>) resultStatus.getResponseContext().get("userClaimsToBeModified")).isEmpty());
    }

    @Test
    public void testProcessSuccessResponseWhenLocalClaimLookupThrowsException() throws Exception {

        FlowContext flowContext = FlowContext.create();
        flowContext.add(UserActionContext.USER_ACTION_CONTEXT_REFERENCE_KEY, mock(UserActionContext.class));

        when(claimMetadataManagementService.getLocalClaim(eq(SINGLE_CLAIM_URI), any()))
                .thenThrow(new ClaimMetadataException("Local claim lookup error"));

        PerformableOperation operation = new PerformableOperation();
        operation.setOp(Operation.ADD);
        operation.setPath("/user/claims[uri=" + SINGLE_CLAIM_URI + "]");
        operation.setValue("Alex");

        ActionInvocationSuccessResponse successResponse = new ActionInvocationSuccessResponse.Builder()
                .actionStatus(ActionInvocationResponse.Status.SUCCESS)
                .operations(Collections.singletonList(operation))
                .responseData(mock(ResponseData.class))
                .build();

        ActionExecutionStatus<Success> resultStatus = preUpdateProfileResponseProcessor.processSuccessResponse(
                flowContext, ActionExecutionResponseContext.create(buildEvent(), successResponse));

        assertTrue(((Map<?, ?>) resultStatus.getResponseContext().get("userClaimsToBeAdded")).isEmpty());
    }

    @Test
    public void testProcessSuccessResponseWhenScimClaimMappingLookupThrowsException() throws Exception {

        FlowContext flowContext = FlowContext.create();
        flowContext.add(UserActionContext.USER_ACTION_CONTEXT_REFERENCE_KEY, mock(UserActionContext.class));

        when(claimMetadataManagementService.getMappedExternalClaimsForLocalClaim(eq(SINGLE_CLAIM_URI), any()))
                .thenThrow(new ClaimMetadataException("SCIM mapping lookup error"));

        PerformableOperation operation = new PerformableOperation();
        operation.setOp(Operation.REPLACE);
        operation.setPath("/user/claims[uri=" + SINGLE_CLAIM_URI + "]");
        operation.setValue("Alex");

        ActionInvocationSuccessResponse successResponse = new ActionInvocationSuccessResponse.Builder()
                .actionStatus(ActionInvocationResponse.Status.SUCCESS)
                .operations(Collections.singletonList(operation))
                .responseData(mock(ResponseData.class))
                .build();

        ActionExecutionStatus<Success> resultStatus = preUpdateProfileResponseProcessor.processSuccessResponse(
                flowContext, ActionExecutionResponseContext.create(buildEvent(), successResponse));

        assertTrue(((Map<?, ?>) resultStatus.getResponseContext().get("userClaimsToBeModified")).isEmpty());
    }

    @Test
    public void testProcessSuccessResponseWhenGetUserClaimValuesThrowsException() throws Exception {

        FlowContext flowContext = FlowContext.create();
        flowContext.add(UserActionContext.USER_ACTION_CONTEXT_REFERENCE_KEY, mock(UserActionContext.class));

        when(userStoreManager.getUserClaimValuesWithID(anyString(), any(String[].class), anyString()))
                .thenThrow(new org.wso2.carbon.user.core.UserStoreException("user store read error"));

        PerformableOperation operation = new PerformableOperation();
        operation.setOp(Operation.ADD);
        operation.setPath("/user/claims[uri=" + SINGLE_CLAIM_URI + "]");
        operation.setValue("Alex");

        ActionInvocationSuccessResponse successResponse = new ActionInvocationSuccessResponse.Builder()
                .actionStatus(ActionInvocationResponse.Status.SUCCESS)
                .operations(Collections.singletonList(operation))
                .responseData(mock(ResponseData.class))
                .build();

        ActionExecutionStatus<Success> resultStatus = preUpdateProfileResponseProcessor.processSuccessResponse(
                flowContext, ActionExecutionResponseContext.create(buildEvent(), successResponse));

        assertTrue(((Map<?, ?>) resultStatus.getResponseContext().get("userClaimsToBeAdded")).isEmpty());
    }

    private static PreUpdateProfileEvent buildEvent() {

        return buildEvent(PreUpdateProfileEvent.FlowInitiatorType.ADMIN);
    }

    private static PreUpdateProfileEvent buildEvent(PreUpdateProfileEvent.FlowInitiatorType initiatorType) {

        return new PreUpdateProfileEvent.Builder()
                .initiatorType(initiatorType)
                .action(PreUpdateProfileEvent.Action.UPDATE)
                .user(new User.Builder(USER_ID).build())
                .build();
    }

    private static LocalClaim mockLocalClaim(String claimUri, boolean isMultiValued, boolean flowInitiatorClaim) {

        LocalClaim localClaim = mock(LocalClaim.class);
        when(localClaim.getClaimURI()).thenReturn(claimUri);
        when(localClaim.getFlowInitiator()).thenReturn(flowInitiatorClaim);
        when(localClaim.getClaimProperty(ClaimConstants.MULTI_VALUED_PROPERTY))
                .thenReturn(String.valueOf(isMultiValued));
        return localClaim;
    }

    private static Claim mockScimClaim(boolean readOnly, boolean required, boolean multiValued) {

        Map<String, String> claimProperties = new HashMap<>();
        claimProperties.put(ClaimConstants.READ_ONLY_PROPERTY, String.valueOf(readOnly));
        claimProperties.put(ClaimConstants.REQUIRED_PROPERTY, String.valueOf(required));
        claimProperties.put(ClaimConstants.MULTI_VALUED_PROPERTY, String.valueOf(multiValued));

        Claim scimClaim = new Claim(SCIM_SCHEMA_URI_PREFIX, SINGLE_CLAIM_URI, claimProperties);
        return scimClaim;
    }
}
