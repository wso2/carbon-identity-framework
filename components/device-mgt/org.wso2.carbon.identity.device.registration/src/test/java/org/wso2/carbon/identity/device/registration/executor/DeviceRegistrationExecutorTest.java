/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.device.registration.executor;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.device.mgt.api.service.DeviceManagementService;
import org.wso2.carbon.identity.device.policy.api.service.DevicePolicyEvaluator;
import org.wso2.carbon.identity.device.policy.api.service.DeviceTokenVerifier;
import org.wso2.carbon.identity.device.registration.internal.component.DeviceRegistrationComponentServiceHolder;
import org.wso2.carbon.identity.device.registration.internal.constant.DeviceRegistrationConstants;
import org.wso2.carbon.identity.device.registration.internal.constant.ErrorMessage;
import org.wso2.carbon.identity.device.registration.internal.exception.DeviceRegistrationException;
import org.wso2.carbon.identity.device.registration.internal.handler.DeviceRegistrationHandler;
import org.wso2.carbon.identity.device.registration.internal.model.DeviceRegistrationChallenge;
import org.wso2.carbon.identity.device.registration.internal.util.DeviceRegistrationExceptionHandler;
import org.wso2.carbon.identity.device.registration.model.VerifiedDevice;
import org.wso2.carbon.identity.flow.execution.engine.model.ExecutorResponse;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowUser;
import org.wso2.carbon.identity.flow.mgt.Constants.FlowTypes;
import org.wso2.carbon.identity.flow.mgt.model.ExecutorDTO;
import org.wso2.carbon.identity.flow.mgt.model.GraphConfig;
import org.wso2.carbon.identity.flow.mgt.model.NodeConfig;
import org.wso2.carbon.identity.policy.evaluation.api.exception.PolicyEvaluationException;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorStatus.STATUS_CLIENT_INPUT_REQUIRED;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorStatus.STATUS_COMPLETE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorStatus.STATUS_ERROR;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorStatus.STATUS_USER_ERROR;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.USERNAME_CLAIM_URI;

/**
 * Unit tests for {@link DeviceRegistrationExecutor}.
 */
@WithCarbonHome
public class DeviceRegistrationExecutorTest {

    private static final String TENANT_DOMAIN = "test.com";
    private static final int TENANT_ID = 1;
    private static final String USER_ID = "user-123";
    private static final String USERNAME = "alice";
    private static final String REGISTRATION_ID = "reg-abc-123";
    private static final String CHALLENGE = "challenge-b64url";

    // Mirrors the private field/form-field constants declared in DeviceRegistrationExecutor.
    private static final String META_POLICY_NAME = "policyName";
    private static final String FIELD_PUBLIC_KEY = "publicKey";
    private static final String FIELD_SIGNATURE = "signature";
    private static final String FIELD_DEVICE_DATA = "deviceData";

    private AutoCloseable closeable;
    private DeviceRegistrationExecutor executor;

    @Mock
    private DeviceManagementService deviceManagementService;

    @Mock
    private DevicePolicyEvaluator devicePolicyEvaluator;

    @Mock
    private DeviceTokenVerifier deviceTokenVerifier;

    private DeviceManagementService originalDeviceManagementService;
    private DevicePolicyEvaluator originalDevicePolicyEvaluator;
    private DeviceTokenVerifier originalDeviceTokenVerifier;
    private MockedStatic<IdentityTenantUtil> identityTenantUtilMocked;
    private MockedStatic<LoggerUtils> loggerUtilsMocked;

    @BeforeClass
    public void setUpClass() {

        closeable = MockitoAnnotations.openMocks(this);
        executor = new DeviceRegistrationExecutor();

        // "test.com" is not a real registered tenant, so anything that resolves it via
        // IdentityTenantUtil (diagnostic logging, FlowUser's claim resolution) needs this stubbed —
        // mirrors the existing DeviceRegistrationHandlerTest's setup.
        identityTenantUtilMocked = mockStatic(IdentityTenantUtil.class);
        when(IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);

        // Diagnostic logging would otherwise try to publish a real event through an
        // IdentityEventService that isn't wired up in this unit test environment; the executor's
        // diagnostic calls are a side effect, not something under test, so disable them.
        loggerUtilsMocked = mockStatic(LoggerUtils.class, CALLS_REAL_METHODS);
        loggerUtilsMocked.when(LoggerUtils::isDiagnosticLogsEnabled).thenReturn(false);

        DeviceRegistrationComponentServiceHolder holder = DeviceRegistrationComponentServiceHolder.getInstance();
        originalDeviceManagementService = holder.getDeviceManagementService();
        originalDevicePolicyEvaluator = holder.getDevicePolicyEvaluator();
        originalDeviceTokenVerifier = holder.getDeviceTokenVerifier();

        holder.setDeviceManagementService(deviceManagementService);
        holder.setDevicePolicyEvaluator(devicePolicyEvaluator);
        holder.setDeviceTokenVerifier(deviceTokenVerifier);
    }

    @AfterClass
    public void tearDownClass() throws Exception {

        DeviceRegistrationComponentServiceHolder holder = DeviceRegistrationComponentServiceHolder.getInstance();
        holder.setDeviceManagementService(originalDeviceManagementService);
        holder.setDevicePolicyEvaluator(originalDevicePolicyEvaluator);
        holder.setDeviceTokenVerifier(originalDeviceTokenVerifier);

        identityTenantUtilMocked.close();
        loggerUtilsMocked.close();

        if (closeable != null) {
            closeable.close();
        }
    }

    @BeforeMethod
    public void setUp() {

        reset(deviceManagementService, devicePolicyEvaluator, deviceTokenVerifier);
        // The executor's diagnostic logger (and FlowUser's own claim-resolution fallback) reads the
        // tenant domain off the thread-local carbon context, not off FlowExecutionContext — seed it
        // here so those calls resolve instead of failing with "Invalid tenant domain null".
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(TENANT_DOMAIN);
    }

    @AfterMethod
    public void tearDownMethod() {

        PrivilegedCarbonContext.endTenantFlow();
    }

    @Test
    public void testGetNameReturnsExecutorName() {

        assertEquals(executor.getName(), DeviceRegistrationConstants.EXECUTOR_NAME);
    }

    @Test
    public void testGetInitiationDataReturnsUsernameClaim() {

        assertEquals(executor.getInitiationData(), java.util.Collections.singletonList(USERNAME_CLAIM_URI));
    }

    // ----- execute() : initiation leg -----

    @Test
    public void testExecuteInitiationReturnsClientInputRequiredWithChallenge() throws Exception {

        FlowExecutionContext context = newContext();
        context.getFlowUser().setUserId(USER_ID);

        ExecutorResponse response;
        try (MockedStatic<DeviceRegistrationHandler> mocked = mockInitiate()) {
            response = executor.execute(context);
        }

        assertEquals(response.getResult(), STATUS_CLIENT_INPUT_REQUIRED);
        assertTrue(response.getRequiredData().contains(FIELD_PUBLIC_KEY));
        assertTrue(response.getRequiredData().contains(FIELD_SIGNATURE));
        assertFalse(response.getRequiredData().contains(FIELD_DEVICE_DATA));
        assertEquals(response.getAdditionalInfo().get("registrationId"), REGISTRATION_ID);
        assertEquals(response.getAdditionalInfo().get("challenge"), CHALLENGE);
        assertNotNull(response.getContextProperties());
        // registrationId and the challenge itself — the challenge now rides in the flow context
        // rather than a server-side cache, so completeRegistration() can read it back later.
        assertEquals(response.getContextProperties().size(), 2);
        assertTrue(response.getContextProperties().containsValue(CHALLENGE));
    }

    @Test
    public void testExecuteInitiationRequiresDeviceDataWhenPolicyConfigured() throws Exception {

        FlowExecutionContext context = newContext();
        context.getFlowUser().setUserId(USER_ID);
        context.setCurrentNode(nodeConfigWithPolicy("strictPolicy"));

        ExecutorResponse response;
        try (MockedStatic<DeviceRegistrationHandler> mocked = mockInitiate()) {
            response = executor.execute(context);
        }

        assertEquals(response.getResult(), STATUS_CLIENT_INPUT_REQUIRED);
        assertTrue(response.getRequiredData().contains(FIELD_DEVICE_DATA));
    }

    @Test
    public void testExecuteInitiationFallsBackToUsernameWhenUserIdBlank() throws Exception {

        FlowExecutionContext context = newContext();
        context.getFlowUser().setUserId("  ");
        context.getFlowUser().setUsername(USERNAME);

        try (MockedStatic<DeviceRegistrationHandler> mocked = mockInitiate()) {
            ExecutorResponse response = executor.execute(context);
            assertEquals(response.getResult(), STATUS_CLIENT_INPUT_REQUIRED);
            mocked.verify(() -> DeviceRegistrationHandler.initiate(eq(USERNAME), eq(TENANT_DOMAIN)));
        }
    }

    @Test
    public void testExecuteInitiationNoAuthenticatedUserReturnsUserNotIdentifiedError() throws Exception {

        FlowExecutionContext context = newContext();
        // A real FlowUser's getUsername() never actually returns blank — when the username field
        // itself is unset it falls back to resolving one (email claim, else a random UUID). Mock
        // FlowUser directly so both accessors report blank, exercising the "no user identified" branch.
        FlowUser unidentifiedUser = mock(FlowUser.class);
        when(unidentifiedUser.getUserId()).thenReturn("");
        when(unidentifiedUser.getUsername()).thenReturn("  ");
        context.setFlowUser(unidentifiedUser);

        try (MockedStatic<DeviceRegistrationHandler> mocked = mockStatic(DeviceRegistrationHandler.class)) {
            ExecutorResponse response = executor.execute(context);

            assertEquals(response.getResult(), STATUS_ERROR);
            assertEquals(response.getErrorCode(), ErrorMessage.ERROR_USER_NOT_IDENTIFIED.getCode());
            mocked.verify(() -> DeviceRegistrationHandler.initiate(any(), any()), never());
        }
    }

    @Test
    public void testExecuteInitiationWithNullCurrentNodeDoesNotNpe() throws Exception {

        FlowExecutionContext context = newContext();
        context.getFlowUser().setUserId(USER_ID);
        // context.getCurrentNode() is null by default — must not NPE inside resolvePolicyName().

        try (MockedStatic<DeviceRegistrationHandler> mocked = mockInitiate()) {
            ExecutorResponse response = executor.execute(context);
            assertEquals(response.getResult(), STATUS_CLIENT_INPUT_REQUIRED);
            assertFalse(response.getRequiredData().contains(FIELD_DEVICE_DATA));
        }
    }

    @Test
    public void testExecuteInitiationWithNodeHavingNoExecutorConfigDoesNotNpe() throws Exception {

        FlowExecutionContext context = newContext();
        context.getFlowUser().setUserId(USER_ID);
        context.setCurrentNode(new NodeConfig.Builder().id("node1").type("TASK_EXECUTION").build());

        try (MockedStatic<DeviceRegistrationHandler> mocked = mockInitiate()) {
            ExecutorResponse response = executor.execute(context);
            assertEquals(response.getResult(), STATUS_CLIENT_INPUT_REQUIRED);
            assertFalse(response.getRequiredData().contains(FIELD_DEVICE_DATA));
        }
    }

    // ----- execute() : completion leg -----

    @Test
    public void testExecuteCompletionValidSignatureRegistrationFlowReturnsCompleteWithDeviceOnContext()
            throws Exception {

        FlowExecutionContext context = newContext();
        context.getFlowUser().setUserId(USER_ID);
        context.setFlowType(FlowTypes.REGISTRATION.getType());
        context.setUserInputData(completionInput());

        FlowExecutionContext afterInitiation = runInitiation(context);
        VerifiedDevice verified = buildVerifiedDevice();

        ExecutorResponse response;
        try (MockedStatic<DeviceRegistrationHandler> mocked = mockVerifySuccess(verified)) {
            response = executor.execute(afterInitiation);
        }

        assertEquals(response.getResult(), STATUS_COMPLETE);
        assertEquals(response.getContextProperties()
                .get(DeviceRegistrationConstants.CTX_DEVICE_REGISTRATION), verified);
        verify(deviceManagementService, never()).persistDevice(any(), any());
    }

    @Test
    public void testExecuteCompletionValidSignatureNonRegistrationFlowAlsoDefersToListener()
            throws Exception {

        FlowExecutionContext context = newContext();
        context.getFlowUser().setUserId(USER_ID);
        // No flow type set — persistence must defer to RegistrationFlowCompletionListener
        // regardless of flow type, exactly like the REGISTRATION flow above.
        context.setUserInputData(completionInput());

        FlowExecutionContext afterInitiation = runInitiation(context);
        VerifiedDevice verified = buildVerifiedDevice();

        ExecutorResponse response;
        try (MockedStatic<DeviceRegistrationHandler> mocked = mockVerifySuccess(verified)) {
            response = executor.execute(afterInitiation);
        }

        assertEquals(response.getResult(), STATUS_COMPLETE);
        assertEquals(response.getContextProperties()
                .get(DeviceRegistrationConstants.CTX_DEVICE_REGISTRATION), verified);
        verify(deviceManagementService, never()).persistDevice(any(), any());
    }

    @Test
    public void testExecuteCompletionMissingChallengeReturnsContextNotFoundError() throws Exception {

        FlowExecutionContext context = newContext();
        context.getFlowUser().setUserId(USER_ID);
        context.setUserInputData(completionInput());

        FlowExecutionContext afterInitiation = runInitiation(context);
        // Simulate the challenge being absent from context (e.g. an incomplete/corrupted context)
        // without hardcoding the executor's private context-property key — drop whichever
        // property holds the challenge value that runInitiation() populated.
        afterInitiation.getProperties().values().removeIf(CHALLENGE::equals);

        // DeviceRegistrationHandler.verify() must never be called when the challenge is missing —
        // left unmocked here, so if the executor called the real implementation it would attempt
        // real crypto validation and fail with a different error code, not this one.
        ExecutorResponse response = executor.execute(afterInitiation);

        assertEquals(response.getResult(), STATUS_USER_ERROR);
        assertEquals(response.getErrorCode(), ErrorMessage.ERROR_REGISTRATION_CONTEXT_NOT_FOUND.getCode());
        verify(deviceManagementService, never()).persistDevice(any(), any());
    }

    @Test
    public void testExecuteCompletionSecondCallWithSameRegistrationIdFailsAfterChallengeConsumed()
            throws Exception {

        FlowExecutionContext context = newContext();
        context.getFlowUser().setUserId(USER_ID);
        context.setUserInputData(completionInput());
        FlowExecutionContext afterInitiation = runInitiation(context);

        VerifiedDevice verified = buildVerifiedDevice();
        ExecutorResponse firstResponse;
        try (MockedStatic<DeviceRegistrationHandler> mocked = mockVerifySuccess(verified)) {
            firstResponse = executor.execute(afterInitiation);
        }
        assertEquals(firstResponse.getResult(), STATUS_COMPLETE);
        afterInitiation.addProperties(firstResponse.getContextProperties());

        // The challenge was consumed by the first, successful call. A second completion attempt
        // with the same registrationId must fail rather than silently re-verify — left unmocked,
        // so this also proves DeviceRegistrationHandler.verify() is not reached a second time.
        ExecutorResponse secondResponse = executor.execute(afterInitiation);

        assertEquals(secondResponse.getResult(), STATUS_USER_ERROR);
        assertEquals(secondResponse.getErrorCode(), ErrorMessage.ERROR_REGISTRATION_CONTEXT_NOT_FOUND.getCode());
    }

    @Test
    public void testExecuteCompletionInvalidSignatureReturnsErrorResponseNotException() throws Exception {

        FlowExecutionContext context = newContext();
        context.getFlowUser().setUserId(USER_ID);
        context.setUserInputData(completionInput());

        FlowExecutionContext afterInitiation = runInitiation(context);

        DeviceRegistrationException invalidSignature = DeviceRegistrationExceptionHandler.handleClientException(
                ErrorMessage.ERROR_INVALID_DEVICE_SIGNATURE, REGISTRATION_ID);

        ExecutorResponse response;
        try (MockedStatic<DeviceRegistrationHandler> mocked = mockVerifyThrows(invalidSignature)) {
            response = executor.execute(afterInitiation);
        }

        assertNotNull(response);
        assertEquals(response.getResult(), STATUS_USER_ERROR);
        assertEquals(response.getErrorCode(), ErrorMessage.ERROR_INVALID_DEVICE_SIGNATURE.getCode());
    }

    @Test
    public void testExecuteCompletionPolicyConfiguredButNoDeviceDataReturnsDeviceDataRequiredError()
            throws Exception {

        FlowExecutionContext context = newContext();
        context.getFlowUser().setUserId(USER_ID);
        context.setCurrentNode(nodeConfigWithPolicy("strictPolicy"));
        Map<String, String> input = completionInput();
        input.remove(FIELD_DEVICE_DATA);
        context.setUserInputData(input);

        FlowExecutionContext afterInitiation = runInitiation(context);
        VerifiedDevice verified = buildVerifiedDevice();

        ExecutorResponse response;
        try (MockedStatic<DeviceRegistrationHandler> mocked = mockVerifySuccess(verified)) {
            response = executor.execute(afterInitiation);
        }

        assertEquals(response.getResult(), STATUS_USER_ERROR);
        assertEquals(response.getErrorCode(), ErrorMessage.ERROR_DEVICE_DATA_REQUIRED.getCode());
        verify(devicePolicyEvaluator, never()).evaluate(any(), any(), any(), any());

        // Retry on the same context with deviceData now supplied: this was a client input problem,
        // not an actual policy verdict, so the challenge must still be valid for a retry to succeed.
        Map<String, String> retryInput = completionInput();
        retryInput.put(FIELD_DEVICE_DATA, "{\"osVersion\":\"12\"}");
        afterInitiation.setUserInputData(retryInput);

        when(deviceTokenVerifier.verifyWithPublicKey(any(), any(), any(), any())).thenReturn(new HashMap<>());
        when(devicePolicyEvaluator.evaluate(eq("strictPolicy"), any(), any(), eq(TENANT_DOMAIN)))
                .thenReturn(null);

        ExecutorResponse retryResponse;
        try (MockedStatic<DeviceRegistrationHandler> mocked = mockVerifySuccess(verified)) {
            retryResponse = executor.execute(afterInitiation);
        }

        assertEquals(retryResponse.getResult(), STATUS_COMPLETE);
        verify(devicePolicyEvaluator).evaluate(eq("strictPolicy"), any(), any(), eq(TENANT_DOMAIN));
    }

    @Test
    public void testExecuteCompletionPolicyNotCompliantReturnsUserError() throws Exception {

        FlowExecutionContext context = newContext();
        context.getFlowUser().setUserId(USER_ID);
        context.setCurrentNode(nodeConfigWithPolicy("strictPolicy"));
        Map<String, String> input = completionInput();
        input.put(FIELD_DEVICE_DATA, "{\"osVersion\":\"9\"}");
        context.setUserInputData(input);

        FlowExecutionContext afterInitiation = runInitiation(context);
        VerifiedDevice verified = buildVerifiedDevice();

        when(deviceTokenVerifier.verifyWithPublicKey(any(), any(), any(), any())).thenReturn(new HashMap<>());
        when(devicePolicyEvaluator.evaluate(eq("strictPolicy"), any(), any(), eq(TENANT_DOMAIN)))
                .thenReturn("osVersion,imei");

        ExecutorResponse response;
        try (MockedStatic<DeviceRegistrationHandler> mocked = mockVerifySuccess(verified)) {
            response = executor.execute(afterInitiation);
        }

        assertEquals(response.getResult(), STATUS_USER_ERROR);
        assertEquals(response.getErrorCode(), ErrorMessage.ERROR_DEVICE_POLICY_NOT_COMPLIANT.getCode());
        verify(deviceManagementService, never()).persistDevice(any(), any());
    }

    @Test
    public void testExecuteCompletionPolicyEvaluationThrowsReturnsEvaluatingPolicyError() throws Exception {

        FlowExecutionContext context = newContext();
        context.getFlowUser().setUserId(USER_ID);
        context.setCurrentNode(nodeConfigWithPolicy("strictPolicy"));
        Map<String, String> input = completionInput();
        input.put(FIELD_DEVICE_DATA, "{\"osVersion\":\"9\"}");
        context.setUserInputData(input);

        FlowExecutionContext afterInitiation = runInitiation(context);
        VerifiedDevice verified = buildVerifiedDevice();

        when(deviceTokenVerifier.verifyWithPublicKey(any(), any(), any(), any())).thenReturn(new HashMap<>());
        when(devicePolicyEvaluator.evaluate(eq("strictPolicy"), any(), any(), eq(TENANT_DOMAIN)))
                .thenThrow(new PolicyEvaluationException("boom"));

        ExecutorResponse response;
        try (MockedStatic<DeviceRegistrationHandler> mocked = mockVerifySuccess(verified)) {
            response = executor.execute(afterInitiation);
        }

        assertEquals(response.getResult(), STATUS_ERROR);
        assertEquals(response.getErrorCode(), ErrorMessage.ERROR_WHILE_EVALUATING_POLICY.getCode());
    }

    // ----- rollback() -----

    @Test
    public void testRollbackIsAlwaysNoOp() throws Exception {

        FlowExecutionContext context = newContext();
        context.getFlowUser().setUserId(USER_ID);
        context.setUserInputData(completionInput());
        FlowExecutionContext afterInitiation = runInitiation(context);

        VerifiedDevice verified = buildVerifiedDevice();
        try (MockedStatic<DeviceRegistrationHandler> mocked = mockVerifySuccess(verified)) {
            executor.execute(afterInitiation);
        }

        // The executor never persists a device itself, so there is nothing for rollback() to
        // compensate for — it must be a pure no-op regardless of what ran before it.
        assertNull(executor.rollback(afterInitiation));
        verify(deviceManagementService, never()).deleteDevice(any(), any());
    }

    // ----- helpers -----

    private FlowExecutionContext newContext() {

        FlowExecutionContext context = new FlowExecutionContext();
        context.setTenantDomain(TENANT_DOMAIN);
        context.setGraphConfig(new GraphConfig());
        context.setFlowUser(new FlowUser());
        // getInitiationData() declares USERNAME_CLAIM_URI, so the flow engine guarantees a username
        // is already collected by the time this executor runs — set one here so getUsername() never
        // has to fall through to its own resolution fallback (tenant config lookup, random UUID),
        // which needs runtime dependencies this test module doesn't have on its classpath.
        context.getFlowUser().setUsername(USERNAME);
        return context;
    }

    private NodeConfig nodeConfigWithPolicy(String policyName) {

        ExecutorDTO executorDTO = new ExecutorDTO();
        executorDTO.setName(DeviceRegistrationConstants.EXECUTOR_NAME);
        executorDTO.addMetadata(META_POLICY_NAME, policyName);
        return new NodeConfig.Builder().id("node1").type("TASK_EXECUTION").executorConfig(executorDTO).build();
    }

    private Map<String, String> completionInput() {

        Map<String, String> input = new HashMap<>();
        input.put(FIELD_PUBLIC_KEY, "base64PublicKey");
        input.put(FIELD_SIGNATURE, "base64Signature");
        return input;
    }

    private VerifiedDevice buildVerifiedDevice() {

        return new VerifiedDevice.Builder()
                .id(REGISTRATION_ID)
                .deviceName("Alice's Device")
                .publicKey("base64PublicKey")
                .registeredAt(Timestamp.from(Instant.now()))
                .build();
    }

    private MockedStatic<DeviceRegistrationHandler> mockInitiate() {

        MockedStatic<DeviceRegistrationHandler> mocked = mockStatic(DeviceRegistrationHandler.class);
        mocked.when(() -> DeviceRegistrationHandler.initiate(any(), any()))
                .thenReturn(new DeviceRegistrationChallenge(REGISTRATION_ID, CHALLENGE));
        return mocked;
    }

    private MockedStatic<DeviceRegistrationHandler> mockVerifySuccess(VerifiedDevice device) {

        MockedStatic<DeviceRegistrationHandler> mocked = mockStatic(DeviceRegistrationHandler.class);
        mocked.when(() -> DeviceRegistrationHandler.verify(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(device);
        return mocked;
    }

    private MockedStatic<DeviceRegistrationHandler> mockVerifyThrows(DeviceRegistrationException exception) {

        MockedStatic<DeviceRegistrationHandler> mocked = mockStatic(DeviceRegistrationHandler.class);
        mocked.when(() -> DeviceRegistrationHandler.verify(any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(exception);
        return mocked;
    }

    /**
     * Runs the initiation leg (mocking {@code DeviceRegistrationHandler.initiate}) and merges the
     * returned context properties (the registration id) back onto the context, so a subsequent
     * {@code executor.execute(context)} call takes the completion leg — without ever hardcoding
     * the executor's private context-property key.
     */
    private FlowExecutionContext runInitiation(FlowExecutionContext context) throws Exception {

        ExecutorResponse initiationResponse;
        try (MockedStatic<DeviceRegistrationHandler> mocked = mockInitiate()) {
            initiationResponse = executor.execute(context);
        }
        assertEquals(initiationResponse.getResult(), STATUS_CLIENT_INPUT_REQUIRED);
        context.addProperties(initiationResponse.getContextProperties());
        return context;
    }
}
