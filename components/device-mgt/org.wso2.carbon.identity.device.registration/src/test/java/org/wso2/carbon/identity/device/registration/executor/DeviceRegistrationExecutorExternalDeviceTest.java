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

import org.mockito.MockedStatic;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.device.mgt.api.service.DeviceManagementService;
import org.wso2.carbon.identity.device.policy.api.service.DevicePolicyEvaluator;
import org.wso2.carbon.identity.device.registration.internal.component.DeviceRegistrationComponentServiceHolder;
import org.wso2.carbon.identity.device.registration.internal.constant.DeviceRegistrationConstants;
import org.wso2.carbon.identity.device.registration.internal.constant.ErrorMessage;
import org.wso2.carbon.identity.device.registration.internal.handler.CrossDeviceRegistrationHandler;
import org.wso2.carbon.identity.device.registration.model.VerifiedDevice;
import org.wso2.carbon.identity.flow.execution.engine.graph.AuthenticationExecutor;
import org.wso2.carbon.identity.flow.execution.engine.internal.FlowExecutionEngineDataHolder;
import org.wso2.carbon.identity.flow.execution.engine.metadata.FlowExecutorPhase;
import org.wso2.carbon.identity.flow.execution.engine.model.ExecutorResponse;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowUser;
import org.wso2.carbon.identity.flow.execution.engine.model.OTFIRetention;
import org.wso2.carbon.identity.flow.execution.engine.model.PollingState;
import org.wso2.carbon.identity.flow.mgt.model.ExecutorDTO;
import org.wso2.carbon.identity.flow.mgt.model.GraphConfig;
import org.wso2.carbon.identity.flow.mgt.model.NodeConfig;
import org.wso2.carbon.identity.flow.mgt.model.NodeEdge;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorStatus.STATUS_CLIENT_INPUT_REQUIRED;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorStatus.STATUS_COMPLETE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorStatus.STATUS_ERROR;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorStatus.STATUS_POLL;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorStatus.STATUS_RETRY;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorStatus.STATUS_USER_ERROR;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorStatus.STATUS_USER_INPUT_REQUIRED;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.OTFI;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.PHASE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.POLL_VALIDITY_SECONDS;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.RETAIN_OTFI;

/**
 * Unit tests for {@link DeviceRegistrationExecutor} QR code registration of another device, and node phases.
 */
@WithCarbonHome
public class DeviceRegistrationExecutorExternalDeviceTest {

    private static final String TENANT_DOMAIN = "test.com";
    private static final int TENANT_ID = 1;
    private static final String AUTHENTICATION_EXECUTOR_NAME = "EmailOTPExecutor";
    private static final String USERNAME = "alice";
    private static final String REGISTRATION_ID = "reg-abc-123";
    private static final String FLOW_ID = "flow-id";
    private static final String POLL_TOKEN_VALUE = "poll-token";
    private static final String QR_DATA = "{\"endpoint\":"
            + "\"https://localhost:9443/t/test.com/api/server/v1/flow/execute\","
            + "\"method\":\"POST\",\"data\":{\"flowId\":\"%s\"}}";
    private static final String INIT_NODE = "initNode";
    private static final String PROCESS_NODE = "processNode";
    private static final String FINISH_NODE = "finishNode";

    private DeviceRegistrationExecutor executor;
    private AuthenticationExecutor authenticationExecutor;
    private DeviceManagementService deviceManagementService;
    private DevicePolicyEvaluator devicePolicyEvaluator;
    private DeviceManagementService originalDeviceManagementService;
    private DevicePolicyEvaluator originalDevicePolicyEvaluator;
    private MockedStatic<IdentityTenantUtil> identityTenantUtilMocked;
    private MockedStatic<LoggerUtils> loggerUtilsMocked;
    private MockedStatic<CrossDeviceRegistrationHandler> crossDeviceHandlerMocked;
    private KeyPair deviceKey;
    private int pollEntries;

    @BeforeClass
    public void setUpClass() throws Exception {

        executor = new DeviceRegistrationExecutor();
        authenticationExecutor = mock(AuthenticationExecutor.class);
        when(authenticationExecutor.getName()).thenReturn(AUTHENTICATION_EXECUTOR_NAME);
        deviceManagementService = mock(DeviceManagementService.class);
        devicePolicyEvaluator = mock(DevicePolicyEvaluator.class);

        identityTenantUtilMocked = mockStatic(IdentityTenantUtil.class);
        when(IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);
        loggerUtilsMocked = mockStatic(LoggerUtils.class, CALLS_REAL_METHODS);
        loggerUtilsMocked.when(LoggerUtils::isDiagnosticLogsEnabled).thenReturn(false);
        // The QR code data needs the service URL builder of a running server.
        crossDeviceHandlerMocked = mockStatic(CrossDeviceRegistrationHandler.class, CALLS_REAL_METHODS);
        crossDeviceHandlerMocked.when(() -> CrossDeviceRegistrationHandler.buildQrCodeData(anyString(), anyString()))
                .thenAnswer(invocation -> String.format(QR_DATA, invocation.<String>getArgument(0)));

        DeviceRegistrationComponentServiceHolder holder = DeviceRegistrationComponentServiceHolder.getInstance();
        originalDeviceManagementService = holder.getDeviceManagementService();
        originalDevicePolicyEvaluator = holder.getDevicePolicyEvaluator();
        holder.setDeviceManagementService(deviceManagementService);
        holder.setDevicePolicyEvaluator(devicePolicyEvaluator);

        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(new ECGenParameterSpec("secp256r1"));
        deviceKey = generator.generateKeyPair();
    }

    @AfterClass
    public void tearDownClass() {

        DeviceRegistrationComponentServiceHolder holder = DeviceRegistrationComponentServiceHolder.getInstance();
        holder.setDeviceManagementService(originalDeviceManagementService);
        holder.setDevicePolicyEvaluator(originalDevicePolicyEvaluator);
        identityTenantUtilMocked.close();
        loggerUtilsMocked.close();
        crossDeviceHandlerMocked.close();
    }

    @BeforeMethod
    public void setUp() throws Exception {

        reset(deviceManagementService, devicePolicyEvaluator);
        when(deviceManagementService.generateDeviceId()).thenReturn(REGISTRATION_ID);
        pollEntries = 0;
        FlowExecutionEngineDataHolder.getInstance().addExecutor(authenticationExecutor);
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(TENANT_DOMAIN);
    }

    @AfterMethod
    public void tearDownMethod() {

        FlowExecutionEngineDataHolder.getInstance().removeExecutor(authenticationExecutor);
        PrivilegedCarbonContext.endTenantFlow();
    }

    @Test
    public void testMetadataListsPhases() {

        List<FlowExecutorPhase> phases = executor.getExecutorMetadata().getPhases();
        assertEquals(phases.stream().map(FlowExecutorPhase::getName).collect(Collectors.toList()),
                Arrays.asList("NATIVE_DEVICE_REGISTRATION", "INIT_REGISTRATION", "PROCESS_DEVICE",
                        "FINISH_REGISTRATION"));
        phases.forEach(phase -> assertNotNull(phase.getDescription()));
    }

    @DataProvider
    public Object[][] nativeDevicePhases() {

        return new Object[][]{{null}, {""}, {"NATIVE_DEVICE_REGISTRATION"}};
    }

    @Test(dataProvider = "nativeDevicePhases")
    public void testNativeDeviceRegistrationIsTheDefault(String phase) throws Exception {

        FlowExecutionContext context = newContext(phase);

        ExecutorResponse response = executor.execute(context);

        assertEquals(response.getResult(), STATUS_CLIENT_INPUT_REQUIRED);
        assertTrue(response.getRequiredData().contains("publicKey"));
        assertTrue(response.getRequiredData().contains("signature"));
        assertNotNull(response.getAdditionalInfo().get("challenge"));
        assertFalse(response.getContextProperties().containsKey(OTFI));
        assertNull(context.getPollingState());
    }

    @Test
    public void testInitRegistrationReturnsThePollStep() throws Exception {

        FlowExecutionContext context = newCrossDeviceContext();
        long requestedAt = System.currentTimeMillis();

        ExecutorResponse response = executor.execute(context);

        assertEquals(response.getResult(), STATUS_POLL);
        // The engine creates the polling entry and adds the poll data to the step.
        assertEquals(context.getPollingState(), PollingState.REQUESTED);
        assertNull(context.getPollToken());
        assertFalse(context.isPolling());
        Map<String, Object> properties = response.getContextProperties();
        String oneTimeFlowId = (String) properties.get(OTFI);
        assertNotNull(oneTimeFlowId);
        assertNotEquals(oneTimeFlowId, FLOW_ID);
        assertEquals(((OTFIRetention) properties.get(RETAIN_OTFI)).getExecutorName(),
                DeviceRegistrationConstants.EXECUTOR_NAME);
        assertEquals(properties.get(DeviceRegistrationConstants.CTX_ONE_TIME_FLOW_ID), oneTimeFlowId);
        assertEquals(properties.get(DeviceRegistrationConstants.CTX_QR_COUNT), "1");
        // The QR code expires with the polling entry.
        long expiresAt = Long.parseLong((String) properties.get(DeviceRegistrationConstants.CTX_QR_EXPIRES_AT));
        assertTrue(expiresAt >= requestedAt + POLL_VALIDITY_SECONDS * 1000L);
        assertTrue(expiresAt <= System.currentTimeMillis() + POLL_VALIDITY_SECONDS * 1000L);
        assertEquals(response.getAdditionalInfo(), Collections.singletonMap("qrCodeData",
                String.format(QR_DATA, oneTimeFlowId)));
        // The device's first request sends no inputs.
        assertNull(response.getRequiredData());
        assertNull(response.getOptionalData());
    }

    @DataProvider
    public Object[][] invalidPhases() {

        return new Object[][]{{"UNKNOWN"}, {"init_registration"}};
    }

    @Test(dataProvider = "invalidPhases")
    public void testInvalidPhaseIsRejected(String phase) throws Exception {

        FlowExecutionContext context = newContext(phase);

        ExecutorResponse response = executor.execute(context);

        assertEquals(response.getResult(), STATUS_ERROR);
        assertEquals(response.getErrorCode(), ErrorMessage.ERROR_INVALID_PHASE.getCode());
        assertNull(context.getPollingState());
    }

    @Test
    public void testFullExternalDeviceRegistration() throws Exception {

        FlowExecutionContext context = newCrossDeviceContext();
        ExecutorResponse qrResponse = request(context, Collections.emptyMap());
        assertEquals(qrResponse.getResult(), STATUS_POLL);
        assertEquals(context.getCurrentNode().getId(), INIT_NODE);
        assertTrue(context.isPolling());
        String oneTimeFlowId = (String) qrResponse.getContextProperties().get(OTFI);

        // Device sends only the flow id: INIT_REGISTRATION completes and PROCESS_DEVICE sends a challenge.
        ExecutorResponse challengeResponse = request(context, Collections.emptyMap());
        assertEquals(context.getCurrentNode().getId(), PROCESS_NODE);
        assertEquals(challengeResponse.getResult(), STATUS_CLIENT_INPUT_REQUIRED);
        assertEquals(challengeResponse.getRequiredData(), Arrays.asList("publicKey", "signature"));
        assertEquals(challengeResponse.getOptionalData(), Collections.singletonList("deviceModel"));
        assertNotNull(challengeResponse.getAdditionalInfo().get("accountHint"));
        assertNull(context.getProperty(DeviceRegistrationConstants.CTX_PUBLIC_KEY));
        String challenge = challengeResponse.getAdditionalInfo().get("challenge");
        assertEquals(challengeResponse.getAdditionalInfo().get("registrationId"), REGISTRATION_ID);
        assertFalse(challengeResponse.getAdditionalInfo().containsKey("deviceDataRequired"));
        // The one time flow id is set again for the device's next request.
        assertEquals(challengeResponse.getContextProperties().get(OTFI), oneTimeFlowId);
        assertEquals(OTFIRetention.fromContext(context).getExecutorName(), DeviceRegistrationConstants.EXECUTOR_NAME);

        // Device sends the public key and signature: it gets the pairing code and retention ends.
        ExecutorResponse deviceResponse = request(context, deviceInput(challenge, "Pixel 8"));
        assertEquals(context.getCurrentNode().getId(), PROCESS_NODE);
        assertEquals(deviceResponse.getResult(), STATUS_USER_INPUT_REQUIRED);
        assertEquals(deviceResponse.getAdditionalInfo().get("deviceId"), REGISTRATION_ID);
        String pairingCode = deviceResponse.getAdditionalInfo().get("pairingCode");
        assertTrue(pairingCode.matches("\\d{6}"));
        assertNotNull(deviceResponse.getAdditionalInfo().get("accountHint"));
        assertFalse(context.getProperties().containsKey(RETAIN_OTFI));
        assertFalse(deviceResponse.getContextProperties().containsKey(OTFI));
        assertNull(context.getProperty(DeviceRegistrationConstants.CTX_ONE_TIME_FLOW_ID));
        // The engine marks the polling entry as successful once the flow id continues the flow.
        assertEquals(context.getPollingState(), PollingState.COMPLETED);
        assertFalse(context.isPolling());
        assertTrue(context.isPollingCompleted());
        assertNull(deviceResponse.getRequiredData());
        // The flow keeps only a hash of the pairing code.
        assertFalse(context.getProperties().containsValue(pairingCode));
        // Nothing is stored for the device yet.
        assertNull(context.getProperty(DeviceRegistrationConstants.CTX_DEVICE_REGISTRATION));
        assertEquals(context.getProperty(DeviceRegistrationConstants.CTX_PUBLIC_KEY), publicKey());
        assertEquals(context.getProperty(DeviceRegistrationConstants.CTX_DEVICE_MODEL), "Pixel 8");
        assertNull(context.getProperty(DeviceRegistrationConstants.CTX_CHALLENGE));

        // Browser continues without inputs: PROCESS_DEVICE completes and FINISH_REGISTRATION asks for the code.
        ExecutorResponse codeInput = request(context, Collections.emptyMap());
        assertEquals(context.getCurrentNode().getId(), FINISH_NODE);
        assertEquals(codeInput.getResult(), STATUS_USER_INPUT_REQUIRED);
        assertEquals(codeInput.getAdditionalInfo().get("deviceModel"), "Pixel 8");
        assertEquals(codeInput.getRequiredData(), Collections.singletonList("pairingCode"));
        // The browser continued, so the engine deletes the polling entry.
        assertNull(context.getPollingState());
        assertNull(context.getPollToken());

        // The right code completes the step with the verified device.
        ExecutorResponse completed = request(context, Collections.singletonMap("pairingCode", pairingCode));
        assertEquals(completed.getResult(), STATUS_COMPLETE);
        VerifiedDevice device = (VerifiedDevice) completed.getContextProperties()
                .get(DeviceRegistrationConstants.CTX_DEVICE_REGISTRATION);
        assertEquals(device.getId(), REGISTRATION_ID);
        assertEquals(device.getDeviceModel(), "Pixel 8");
        assertEquals(device.getPublicKey(), publicKey());
        assertNull(context.getPollingState());
        assertNull(context.getProperty(DeviceRegistrationConstants.CTX_PAIRING_CODE_HASH));
    }

    @Test
    public void testProcessDeviceWithoutQrCodeIsRejected() throws Exception {

        FlowExecutionContext context = newCrossDeviceContext();
        context.setCurrentNode(context.getGraphConfig().getNodeConfigs().get(PROCESS_NODE));

        ExecutorResponse response = request(context, Collections.emptyMap());

        assertEquals(response.getResult(), STATUS_ERROR);
        assertEquals(response.getErrorCode(), ErrorMessage.ERROR_INVALID_PHASE_ORDER.getCode());
    }

    @Test
    public void testFinishRegistrationWithoutVerifiedDeviceIsRejected() throws Exception {

        FlowExecutionContext context = newCrossDeviceContext();
        context.setCurrentNode(context.getGraphConfig().getNodeConfigs().get(FINISH_NODE));

        ExecutorResponse response = request(context, Collections.emptyMap());

        assertEquals(response.getResult(), STATUS_ERROR);
        assertEquals(response.getErrorCode(), ErrorMessage.ERROR_INVALID_PHASE_ORDER.getCode());
    }

    @Test
    public void testWrongPairingCodesAreExhaustedAtThree() throws Exception {

        FlowExecutionContext context = pairingCodeContext();

        ExecutorResponse first = request(context, Collections.singletonMap("pairingCode", "000000x"));
        assertEquals(first.getResult(), STATUS_RETRY);
        assertEquals(first.getErrorMessage(), DeviceRegistrationConstants.I18N_INVALID_PAIRING_CODE);
        assertEquals(first.getRequiredData(), Collections.singletonList("pairingCode"));
        ExecutorResponse second = request(context, Collections.singletonMap("pairingCode", "000000y"));
        assertEquals(second.getResult(), STATUS_RETRY);
        ExecutorResponse third = request(context, Collections.singletonMap("pairingCode", "000000z"));
        assertEquals(third.getResult(), STATUS_USER_ERROR);
        assertEquals(third.getErrorCode(), ErrorMessage.ERROR_INVALID_PAIRING_CODE.getCode());
        assertNull(third.getContextProperties());
        // The browser continued to the pairing code, so its polling has already ended.
        assertNull(context.getPollingState());
    }

    @Test
    public void testBadSignatureFailsTheDevice() throws Exception {

        FlowExecutionContext context = newCrossDeviceContext();
        request(context, Collections.emptyMap());
        request(context, Collections.emptyMap());

        Map<String, String> input = new HashMap<>();
        input.put("publicKey", publicKey());
        input.put("signature", Base64.getEncoder().encodeToString("not a signature".getBytes(StandardCharsets.UTF_8)));
        ExecutorResponse response = request(context, input);

        assertEquals(response.getResult(), STATUS_USER_ERROR);
        assertEquals(response.getErrorCode(), ErrorMessage.ERROR_INVALID_DEVICE_SIGNATURE.getCode());
        assertNull(response.getContextProperties());
        // The error ends the flow; the engine deletes the polling entry on any flow error.
        assertEquals(context.getPollingState(), PollingState.IN_PROGRESS);
    }

    @Test
    public void testSignatureMustMatchThePublicKeyOfTheRequest() throws Exception {

        FlowExecutionContext context = newCrossDeviceContext();
        request(context, Collections.emptyMap());
        ExecutorResponse challengeResponse = request(context, Collections.emptyMap());

        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(new ECGenParameterSpec("secp256r1"));
        Map<String, String> input = deviceInput(challengeResponse.getAdditionalInfo().get("challenge"), null);
        input.put("publicKey", Base64.getEncoder().encodeToString(
                generator.generateKeyPair().getPublic().getEncoded()));
        ExecutorResponse response = request(context, input);

        assertEquals(response.getResult(), STATUS_USER_ERROR);
        assertEquals(response.getErrorCode(), ErrorMessage.ERROR_INVALID_DEVICE_SIGNATURE.getCode());
        assertNull(context.getProperty(DeviceRegistrationConstants.CTX_PUBLIC_KEY));
        // The challenge is single use.
        assertNull(context.getProperty(DeviceRegistrationConstants.CTX_CHALLENGE));
        assertEquals(context.getPollingState(), PollingState.IN_PROGRESS);
    }

    @Test
    public void testExpiredQrCodeFailsTheDevice() throws Exception {

        FlowExecutionContext context = newCrossDeviceContext();
        request(context, Collections.emptyMap());
        context.setProperty(DeviceRegistrationConstants.CTX_QR_EXPIRES_AT,
                String.valueOf(System.currentTimeMillis() - 1));

        ExecutorResponse response = request(context, Collections.emptyMap());

        assertEquals(response.getResult(), STATUS_USER_ERROR);
        assertEquals(response.getErrorCode(), ErrorMessage.ERROR_INVALID_QR_CODE.getCode());
        assertEquals(context.getPollingState(), PollingState.IN_PROGRESS);
    }

    @Test
    public void testDeviceModelTooLong() throws Exception {

        FlowExecutionContext context = newCrossDeviceContext();
        request(context, Collections.emptyMap());
        ExecutorResponse challengeResponse = request(context, Collections.emptyMap());

        ExecutorResponse response = request(context, deviceInput(challengeResponse.getAdditionalInfo().get("challenge"),
                new String(new char[65]).replace('\0', 'a')));

        assertEquals(response.getResult(), STATUS_USER_ERROR);
        assertEquals(response.getErrorCode(), ErrorMessage.ERROR_INVALID_DEVICE_MODEL.getCode());
        assertEquals(context.getPollingState(), PollingState.IN_PROGRESS);
    }

    @Test
    public void testAtMostFiveQrCodesPerStep() throws Exception {

        FlowExecutionContext context = newCrossDeviceContext();
        for (int i = 1; i <= DeviceRegistrationConstants.MAX_QR_CODES; i++) {
            // The flow re-enters the step, for example through a loop.
            context.getProperties().remove(DeviceRegistrationConstants.CTX_ONE_TIME_FLOW_ID);
            ExecutorResponse response = executor.execute(context);
            assertEquals(response.getResult(), STATUS_POLL);
            // Each new QR code starts polling again, so the engine replaces the previous polling entry.
            assertEquals(context.getPollingState(), PollingState.REQUESTED);
            PollingState previousPollingState = context.getPollingState();
            applyProperties(context, response);
            applyPolling(context, previousPollingState);
            assertEquals(context.getPollToken(), POLL_TOKEN_VALUE + i);
        }
        context.getProperties().remove(DeviceRegistrationConstants.CTX_ONE_TIME_FLOW_ID);

        ExecutorResponse response = executor.execute(context);

        assertEquals(response.getResult(), STATUS_USER_ERROR);
        assertEquals(response.getErrorCode(), ErrorMessage.ERROR_QR_CODE_LIMIT_REACHED.getCode());
    }

    @Test
    public void testPolicyOfTheDeviceNodeRequiresDeviceData() throws Exception {

        FlowExecutionContext context = newCrossDeviceContext();
        context.getGraphConfig().getNodeConfigs().get(PROCESS_NODE).getExecutorConfig()
                .addMetadata("policyName", "strict");
        request(context, Collections.emptyMap());
        ExecutorResponse challengeResponse = request(context, Collections.emptyMap());
        assertEquals(challengeResponse.getRequiredData(), Arrays.asList("publicKey", "signature", "deviceData"));

        ExecutorResponse response = request(context,
                deviceInput(challengeResponse.getAdditionalInfo().get("challenge"), null));

        assertEquals(response.getResult(), STATUS_USER_ERROR);
        assertEquals(response.getErrorCode(), ErrorMessage.ERROR_DEVICE_DATA_REQUIRED.getCode());
        assertEquals(context.getPollingState(), PollingState.IN_PROGRESS);
    }

    /**
     * A flow where the device has shown the pairing code, waiting for the browser's poll token.
     */
    private FlowExecutionContext deviceCodeContext() throws Exception {

        FlowExecutionContext context = newCrossDeviceContext();
        request(context, Collections.emptyMap());
        ExecutorResponse challengeResponse = request(context, Collections.emptyMap());
        request(context, deviceInput(challengeResponse.getAdditionalInfo().get("challenge"), null));
        assertNotNull(context.getProperty(DeviceRegistrationConstants.CTX_PAIRING_CODE_HASH));
        return context;
    }

    /**
     * A flow at the pairing code input.
     */
    private FlowExecutionContext pairingCodeContext() throws Exception {

        FlowExecutionContext context = deviceCodeContext();
        request(context, Collections.emptyMap());
        assertEquals(context.getCurrentNode().getId(), FINISH_NODE);
        return context;
    }

    /**
     * Run a request as the engine does, moving to the next node in the same request when a node completes.
     */
    private ExecutorResponse request(FlowExecutionContext context, Map<String, String> input) throws Exception {

        context.getUserInputData().clear();
        context.getUserInputData().putAll(input);
        PollingState previousPollingState = context.getPollingState();
        while (true) {
            ExecutorResponse response = executor.execute(context);
            applyProperties(context, response);
            NodeConfig node = context.getCurrentNode();
            if (!STATUS_COMPLETE.equals(response.getResult()) || node.getEdges() == null
                    || node.getEdges().isEmpty()) {
                applyPolling(context, previousPollingState);
                return response;
            }
            context.addCompletedNode(node);
            context.setCurrentNode(context.getGraphConfig().getNodeConfigs()
                    .get(node.getEdges().get(0).getTargetNodeId()));
        }
    }

    /**
     * Inputs of the device's second request: the public key, the signed challenge and an optional device model.
     */
    private Map<String, String> deviceInput(String challenge, String deviceModel) throws Exception {

        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initSign(deviceKey.getPrivate());
        signature.update(Base64.getUrlDecoder().decode(challenge));
        Map<String, String> input = new HashMap<>();
        input.put("publicKey", publicKey());
        input.put("signature", Base64.getEncoder().encodeToString(signature.sign()));
        if (deviceModel != null) {
            input.put("deviceModel", deviceModel);
        }
        return input;
    }

    private String publicKey() {

        return Base64.getEncoder().encodeToString(deviceKey.getPublic().getEncoded());
    }

    /**
     * Apply the response's context properties as the engine does after each node.
     */
    private void applyProperties(FlowExecutionContext context, ExecutorResponse response) {

        if (response.getContextProperties() != null) {
            context.addProperties(response.getContextProperties());
        }
    }

    /**
     * Apply the polling state as the engine does at the end of a request, creating or deleting the polling entry.
     */
    private void applyPolling(FlowExecutionContext context, PollingState previousPollingState) {

        PollingState pollingState = context.getPollingState();
        if (pollingState == PollingState.IN_PROGRESS
                || (pollingState == PollingState.COMPLETED && previousPollingState != PollingState.COMPLETED)) {
            return;
        }
        context.setPollToken(null);
        context.setPollingState(null);
        if (pollingState == PollingState.REQUESTED) {
            context.setPollToken(POLL_TOKEN_VALUE + ++pollEntries);
            context.setPollingState(PollingState.IN_PROGRESS);
        }
    }

    private FlowExecutionContext newContext(String phase) {

        FlowExecutionContext context = new FlowExecutionContext();
        context.setContextIdentifier(FLOW_ID);
        context.setTenantDomain(TENANT_DOMAIN);
        context.setGraphConfig(new GraphConfig());
        context.setFlowUser(new FlowUser());
        context.getFlowUser().setUsername(USERNAME);
        context.addCompletedNode(new NodeConfig.Builder().id("otpNode").type("TASK_EXECUTION")
                .executorConfig(new ExecutorDTO(AUTHENTICATION_EXECUTOR_NAME)).build());
        context.setCurrentNode(deviceNode("deviceNode", phase, null));
        return context;
    }

    /**
     * A flow with the three QR code registration nodes, at the first of them.
     */
    private FlowExecutionContext newCrossDeviceContext() {

        FlowExecutionContext context = newContext(null);
        GraphConfig graph = context.getGraphConfig();
        graph.addNodeConfig(deviceNode(INIT_NODE, "INIT_REGISTRATION", PROCESS_NODE));
        graph.addNodeConfig(deviceNode(PROCESS_NODE, "PROCESS_DEVICE", FINISH_NODE));
        graph.addNodeConfig(deviceNode(FINISH_NODE, "FINISH_REGISTRATION", null));
        context.setCurrentNode(graph.getNodeConfigs().get(INIT_NODE));
        return context;
    }

    private static NodeConfig deviceNode(String id, String phase, String nextNodeId) {

        ExecutorDTO executorDTO = new ExecutorDTO();
        executorDTO.setName(DeviceRegistrationConstants.EXECUTOR_NAME);
        if (phase != null) {
            executorDTO.addMetadata(PHASE, phase);
        }
        NodeConfig node = new NodeConfig.Builder().id(id).type("TASK_EXECUTION").executorConfig(executorDTO).build();
        if (nextNodeId != null) {
            node.addEdge(new NodeEdge(id, nextNodeId, null));
        }
        return node;
    }
}
