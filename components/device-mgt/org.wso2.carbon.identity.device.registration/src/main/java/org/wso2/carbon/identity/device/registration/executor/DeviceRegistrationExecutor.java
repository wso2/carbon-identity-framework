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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.device.policy.api.exception.DevicePolicyClientException;
import org.wso2.carbon.identity.device.policy.api.exception.DevicePolicyException;
import org.wso2.carbon.identity.device.policy.api.model.DevicePolicyEvaluationResult;
import org.wso2.carbon.identity.device.policy.api.service.DevicePolicyEvaluator;
import org.wso2.carbon.identity.device.registration.internal.component.DeviceRegistrationComponentServiceHolder;
import org.wso2.carbon.identity.device.registration.internal.constant.DeviceRegistrationConstants;
import org.wso2.carbon.identity.device.registration.internal.constant.ErrorMessage;
import org.wso2.carbon.identity.device.registration.internal.exception.DeviceRegistrationException;
import org.wso2.carbon.identity.device.registration.internal.handler.CrossDeviceRegistrationHandler;
import org.wso2.carbon.identity.device.registration.internal.handler.DeviceRegistrationHandler;
import org.wso2.carbon.identity.device.registration.internal.model.DeviceRegistrationChallenge;
import org.wso2.carbon.identity.device.registration.internal.util.DeviceRegistrationDiagnosticLogger;
import org.wso2.carbon.identity.device.registration.model.VerifiedDevice;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.execution.engine.graph.Executor;
import org.wso2.carbon.identity.flow.execution.engine.metadata.FlowExecutorInfo;
import org.wso2.carbon.identity.flow.execution.engine.metadata.FlowExecutorMetadata;
import org.wso2.carbon.identity.flow.execution.engine.metadata.FlowExecutorMetadataService;
import org.wso2.carbon.identity.flow.execution.engine.metadata.FlowExecutorPhase;
import org.wso2.carbon.identity.flow.execution.engine.model.ExecutorResponse;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.OTFIRetention;
import org.wso2.carbon.identity.flow.mgt.Constants.FlowTypes;
import org.wso2.carbon.identity.flow.mgt.model.ExecutorDTO;
import org.wso2.carbon.identity.flow.mgt.model.NodeConfig;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.wso2.carbon.identity.device.registration.internal.constant.DeviceRegistrationConstants.PHASE_FINISH_REGISTRATION;
import static org.wso2.carbon.identity.device.registration.internal.constant.DeviceRegistrationConstants.PHASE_INIT_REGISTRATION;
import static org.wso2.carbon.identity.device.registration.internal.constant.DeviceRegistrationConstants.PHASE_NATIVE_DEVICE_REGISTRATION;
import static org.wso2.carbon.identity.device.registration.internal.constant.DeviceRegistrationConstants.PHASE_PROCESS_DEVICE;
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
import static org.wso2.carbon.identity.flow.execution.engine.Constants.USERNAME_CLAIM_URI;
import static org.wso2.carbon.identity.flow.mgt.Constants.FlowTypes.INVITED_USER_REGISTRATION;
import static org.wso2.carbon.identity.flow.mgt.Constants.FlowTypes.REGISTRATION;

/**
 * Flow executor that registers the user's device. Each node runs the phase named in its executor metadata.
 * NATIVE_DEVICE_REGISTRATION (the default) registers the device the flow runs on by challenge, signature and an
 * optional policy check. INIT_REGISTRATION, PROCESS_DEVICE and FINISH_REGISTRATION run in consecutive nodes to
 * register another device by QR code and pairing code.
 * The verified device is always deferred to
 * {@link org.wso2.carbon.identity.device.registration.listener.RegistrationFlowCompletionListener},
 * since a userId is not guaranteed to be available on FlowUser until the whole flow completes.
 * Only one QR code registration is supported in a flow.
 */
public class DeviceRegistrationExecutor implements Executor {

    private static final Log LOG = LogFactory.getLog(DeviceRegistrationExecutor.class);

    private static final String POLICY_EVALUATION_FAILED = "Policy evaluation failed for policy: ";

    private final DeviceRegistrationDiagnosticLogger diagnosticLogger = new DeviceRegistrationDiagnosticLogger();

    @Override
    public String getName() {

        return DeviceRegistrationConstants.EXECUTOR_NAME;
    }

    @Override
    public ExecutorResponse execute(FlowExecutionContext context) throws FlowEngineException {

        if (!isUserAuthenticated(context)) {
            diagnosticLogger.logRegistrationFailure("User must be authenticated before registering a device.");
            ExecutorResponse response = new ExecutorResponse();
            response.setResult(STATUS_ERROR);
            response.setErrorCode(ErrorMessage.ERROR_USER_NOT_AUTHENTICATED.getCode());
            response.setErrorMessage(ErrorMessage.ERROR_USER_NOT_AUTHENTICATED.getMessage());
            response.setErrorDescription(ErrorMessage.ERROR_USER_NOT_AUTHENTICATED.getDescription());
            return response;
        }

        String phase = resolvePhase(context.getCurrentNode());
        switch (phase) {
            case PHASE_NATIVE_DEVICE_REGISTRATION:
                String registrationId = (String) context.getProperty(DeviceRegistrationConstants.CTX_REGISTRATION_ID);
                if (registrationId == null) {
                    return initiateRegistration(context);
                }
                return completeRegistration(context, registrationId);
            case PHASE_INIT_REGISTRATION:
                return initCrossDeviceRegistration(context);
            case PHASE_PROCESS_DEVICE:
                return processDevice(context);
            case PHASE_FINISH_REGISTRATION:
                return finishCrossDeviceRegistration(context);
            default:
                diagnosticLogger.logRegistrationFailure("Invalid device registration phase: " + phase);
                return errorResponse(STATUS_ERROR, ErrorMessage.ERROR_INVALID_PHASE, phase);
        }
    }

    @Override
    public ExecutorResponse rollback(FlowExecutionContext context) throws FlowEngineException {

        // Nothing to compensate: this executor never persists a device itself. Persistence is
        // always deferred to RegistrationFlowCompletionListener, which only runs once the whole
        // flow completes.
        return null;
    }

    @Override
    public List<String> getInitiationData() {

        return Collections.singletonList(USERNAME_CLAIM_URI);
    }

    @Override
    public Set<FlowTypes> getSupportedFlowTypes() {

        return EnumSet.of(REGISTRATION, INVITED_USER_REGISTRATION);
    }

    @Override
    public FlowExecutorMetadata getExecutorMetadata() {

        return FlowExecutorMetadata.builder()
                .displayName("Device Registration")
                .description("Register the user's device.")
                .icon("assets/images/icons/device-registration.svg")
                .phase(new FlowExecutorPhase(PHASE_NATIVE_DEVICE_REGISTRATION,
                        "Register the device the flow runs on. This is the default."))
                .phase(new FlowExecutorPhase(PHASE_INIT_REGISTRATION,
                        "Show a QR code to register another device and wait for the device."))
                .phase(new FlowExecutorPhase(PHASE_PROCESS_DEVICE,
                        "Verify the device that scanned the QR code and show it a pairing code."))
                .phase(new FlowExecutorPhase(PHASE_FINISH_REGISTRATION,
                        "Ask for the pairing code shown on the device and register the device."))
                .build();
    }

    /**
     * Identifying the user, for example with a user resolve step, does not prove who is registering the device.
     * Require an authentication executor to have completed earlier on the executed path so the device can only be
     * bound to an authenticated user.
     *
     * @param context Flow execution context.
     * @return True if an authentication executor has completed in this flow.
     */
    private boolean isUserAuthenticated(FlowExecutionContext context) {

        for (NodeConfig completedNode : context.getCompletedNodes()) {
            ExecutorDTO executorConfig = completedNode.getExecutorConfig();
            if (executorConfig != null && FlowExecutorMetadataService.getInstance()
                    .getExecutor(executorConfig.getName())
                    .map(FlowExecutorInfo::isAuthenticationExecutor)
                    .orElse(false)) {
                return true;
            }
        }
        return false;
    }

    private ExecutorResponse initiateRegistration(FlowExecutionContext context) {

        try {
            String username = context.getFlowUser().getUsername();

            if (isBlank(username)) {
                diagnosticLogger.logRegistrationFailure("User could not be identified to initiate device "
                        + "registration.");
                ExecutorResponse response = new ExecutorResponse();
                response.setResult(STATUS_ERROR);
                response.setErrorCode(ErrorMessage.ERROR_USER_NOT_IDENTIFIED.getCode());
                response.setErrorMessage(ErrorMessage.ERROR_USER_NOT_IDENTIFIED.getMessage());
                response.setErrorDescription(ErrorMessage.ERROR_USER_NOT_IDENTIFIED.getDescription());
                return response;
            }

            DeviceRegistrationChallenge initiation =
                    DeviceRegistrationHandler.initiate(username, context.getTenantDomain());

            diagnosticLogger.logRegistrationInitiated(initiation.getRegistrationId());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Device registration initiated for user: " + LoggerUtils.getMaskedContent(username)
                        + " registrationId: " + initiation.getRegistrationId());
            }

            Map<String, String> additionalInfo = new HashMap<>();
            additionalInfo.put(DeviceRegistrationConstants.PROP_REGISTRATION_ID, initiation.getRegistrationId());
            additionalInfo.put(DeviceRegistrationConstants.PROP_CHALLENGE, initiation.getChallenge());

            Map<String, Object> contextProperties = new HashMap<>();
            contextProperties.put(DeviceRegistrationConstants.CTX_REGISTRATION_ID, initiation.getRegistrationId());
            contextProperties.put(DeviceRegistrationConstants.CTX_CHALLENGE, initiation.getChallenge());

            List<String> requiredFields = new ArrayList<>(
                    Arrays.asList(DeviceRegistrationConstants.FIELD_PUBLIC_KEY,
                            DeviceRegistrationConstants.FIELD_SIGNATURE));
            List<String> optionalFields = new ArrayList<>(
                    Collections.singletonList(DeviceRegistrationConstants.FIELD_DEVICE_MODEL));
            String policyName = resolvePolicyName(context);
            if (policyName != null) {
                requiredFields.add(DeviceRegistrationConstants.FIELD_DEVICE_DATA);
            }

            ExecutorResponse response = new ExecutorResponse();
            response.setResult(STATUS_CLIENT_INPUT_REQUIRED);
            response.setRequiredData(requiredFields);
            response.setOptionalData(optionalFields);
            response.setAdditionalInfo(additionalInfo);
            response.setContextProperty(contextProperties);
            return response;

        } catch (DeviceRegistrationException e) {
            diagnosticLogger.logRegistrationFailure("Error initiating device registration: " + e.getMessage());
            LOG.error("Error initiating device registration in tenant: "
                    + context.getTenantDomain(), e);
            ExecutorResponse response = new ExecutorResponse();
            response.setResult(STATUS_ERROR);
            response.setErrorCode(e.getErrorCode());
            response.setErrorMessage(e.getMessage());
            response.setErrorDescription(e.getDescription());
            return response;
        }
    }

    private ExecutorResponse completeRegistration(FlowExecutionContext context,
            String registrationId) {

        try {
            String challenge = (String) context.getProperty(DeviceRegistrationConstants.CTX_CHALLENGE);
            if (isBlank(challenge)) {
                diagnosticLogger.logRegistrationFailure(
                        "Registration context not found for registrationId: " + registrationId);
                ExecutorResponse response = new ExecutorResponse();
                response.setResult(STATUS_ERROR);
                response.setErrorCode(ErrorMessage.ERROR_REGISTRATION_CONTEXT_NOT_FOUND.getCode());
                response.setErrorMessage(ErrorMessage.ERROR_REGISTRATION_CONTEXT_NOT_FOUND.getMessage());
                response.setErrorDescription(String.format(
                        ErrorMessage.ERROR_REGISTRATION_CONTEXT_NOT_FOUND.getDescription(), registrationId));
                return response;
            }

            Map<String, String> input = context.getUserInputData();
            String deviceModel = input.get(DeviceRegistrationConstants.FIELD_DEVICE_MODEL);
            String deviceName = buildDeviceName(context, deviceModel);

            // Step 1: Verify signature.
            VerifiedDevice verified = DeviceRegistrationHandler.verify(
                    registrationId,
                    challenge,
                    input.get(DeviceRegistrationConstants.FIELD_PUBLIC_KEY),
                    input.get(DeviceRegistrationConstants.FIELD_SIGNATURE),
                    deviceName,
                    deviceModel);

            // Step 2: Policy compliance check (skipped when policyName not configured).
            String policyName = resolvePolicyName(context);
            if (policyName != null) {
                if (isBlank(input.get(DeviceRegistrationConstants.FIELD_DEVICE_DATA))) {
                    diagnosticLogger.logRegistrationFailure("Device data is required for policy evaluation "
                            + "but was not provided.");
                    ExecutorResponse response = new ExecutorResponse();
                    response.setResult(STATUS_USER_ERROR);
                    response.setErrorCode(ErrorMessage.ERROR_DEVICE_DATA_REQUIRED.getCode());
                    response.setErrorMessage(ErrorMessage.ERROR_DEVICE_DATA_REQUIRED.getMessage());
                    response.setErrorDescription(ErrorMessage.ERROR_DEVICE_DATA_REQUIRED.getDescription());
                    return response;
                }
                ExecutorResponse policyResult = evaluatePolicy(policyName, registrationId,
                        input.get(DeviceRegistrationConstants.FIELD_DEVICE_DATA),
                        input.get(DeviceRegistrationConstants.FIELD_PUBLIC_KEY), context);
                if (policyResult != null) {
                    context.getProperties().remove(DeviceRegistrationConstants.CTX_CHALLENGE);
                    return policyResult;
                }
            }

            context.getProperties().remove(DeviceRegistrationConstants.CTX_CHALLENGE);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Device registration verified for registrationId: " + registrationId);
            }
            ExecutorResponse response = new ExecutorResponse();
            response.setResult(STATUS_COMPLETE);
            Map<String, Object> ctxProps = new HashMap<>();
            ctxProps.put(DeviceRegistrationConstants.CTX_DEVICE_REGISTRATION, verified);
            response.setContextProperty(ctxProps);
            return response;

        } catch (DeviceRegistrationException e) {
            ExecutorResponse response = new ExecutorResponse();
            if (e.isClientError()) {
                response.setResult(STATUS_USER_ERROR);
            } else {
                diagnosticLogger.logRegistrationFailure("Error completing device registration: " + e.getMessage());
                LOG.error("Error completing device registration for registrationId: "
                        + registrationId, e);
                response.setResult(STATUS_ERROR);
            }
            response.setErrorCode(e.getErrorCode());
            response.setErrorMessage(e.getMessage());
            response.setErrorDescription(e.getDescription());
            return response;
        }
    }

    /**
     * INIT_REGISTRATION: start polling for the browser and show a QR code carrying a retained one time flow id.
     * The device is then the flow's only writer until it registers. Completes on the device's first request.
     *
     * @param context Flow execution context.
     * @return Poll response, or complete once the device has scanned the QR code.
     */
    private ExecutorResponse initCrossDeviceRegistration(FlowExecutionContext context) {

        if (context.getProperty(DeviceRegistrationConstants.CTX_ONE_TIME_FLOW_ID) != null) {
            // Only the device knows the one time flow id, so this is its first request.
            ExecutorResponse response = new ExecutorResponse();
            response.setResult(STATUS_COMPLETE);
            return response;
        }
        if (isBlank(context.getFlowUser().getUsername())) {
            diagnosticLogger.logRegistrationFailure("User could not be identified to initiate device registration.");
            return errorResponse(STATUS_ERROR, ErrorMessage.ERROR_USER_NOT_IDENTIFIED);
        }
        int qrCodeCount = parseCount(context.getProperty(DeviceRegistrationConstants.CTX_QR_COUNT));
        if (qrCodeCount >= DeviceRegistrationConstants.MAX_QR_CODES) {
            diagnosticLogger.logRegistrationFailure("The maximum number of QR codes for the step was reached.");
            return errorResponse(STATUS_USER_ERROR, ErrorMessage.ERROR_QR_CODE_LIMIT_REACHED);
        }

        try {
            String oneTimeFlowId = CrossDeviceRegistrationHandler.generateOneTimeFlowId();
            String qrCodeData = CrossDeviceRegistrationHandler.buildQrCodeData(oneTimeFlowId,
                    context.getTenantDomain());

            // The engine creates the polling entry and adds its poll data. Re-entering the step replaces it.
            context.startPolling();
            // The QR code expires with the polling entry.
            long expiresAt = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(POLL_VALIDITY_SECONDS);
            clearDeviceProperties(context);

            Map<String, Object> contextProperties = new HashMap<>();
            contextProperties.put(OTFI, oneTimeFlowId);
            contextProperties.put(RETAIN_OTFI, new OTFIRetention.Builder().executorName(getName()).build());
            contextProperties.put(DeviceRegistrationConstants.CTX_ONE_TIME_FLOW_ID, oneTimeFlowId);
            contextProperties.put(DeviceRegistrationConstants.CTX_QR_COUNT, String.valueOf(qrCodeCount + 1));
            contextProperties.put(DeviceRegistrationConstants.CTX_QR_EXPIRES_AT, String.valueOf(expiresAt));

            Map<String, String> additionalInfo = new HashMap<>();
            additionalInfo.put(DeviceRegistrationConstants.PROP_QR_CODE_DATA, qrCodeData);

            // The device's first request sends only the one time flow id.
            ExecutorResponse response = new ExecutorResponse();
            response.setResult(STATUS_POLL);
            response.setAdditionalInfo(additionalInfo);
            response.setContextProperty(contextProperties);
            return response;
        } catch (DeviceRegistrationException e) {
            diagnosticLogger.logRegistrationFailure("Error creating the QR code: " + e.getMessage());
            LOG.error("Error creating the device registration QR code in tenant: " + context.getTenantDomain(), e);
            return errorResponse(e);
        }
    }

    /**
     * PROCESS_DEVICE: verify the device by challenge and signature, then give it a pairing code, end the one time
     * flow id and end polling successfully. Completes on the browser's next request.
     *
     * @param context Flow execution context.
     * @return Executor response.
     */
    private ExecutorResponse processDevice(FlowExecutionContext context) {

        if (context.isPollingCompleted()) {
            // The browser continues after its poll reports success.
            ExecutorResponse response = new ExecutorResponse();
            response.setResult(STATUS_COMPLETE);
            return response;
        }
        if (!context.isPolling() || context.getProperty(DeviceRegistrationConstants.CTX_ONE_TIME_FLOW_ID) == null) {
            diagnosticLogger.logRegistrationFailure("The device step runs without a QR code in the flow.");
            return errorResponse(STATUS_ERROR, ErrorMessage.ERROR_INVALID_PHASE_ORDER, PHASE_PROCESS_DEVICE);
        }
        long expiresAt = parseLong(context.getProperty(DeviceRegistrationConstants.CTX_QR_EXPIRES_AT));
        if (System.currentTimeMillis() >= expiresAt) {
            diagnosticLogger.logRegistrationFailure("The device used an expired QR code.");
            return errorResponse(STATUS_USER_ERROR, ErrorMessage.ERROR_INVALID_QR_CODE);
        }
        String registrationId = (String) context.getProperty(DeviceRegistrationConstants.CTX_REGISTRATION_ID);
        try {
            if (registrationId == null) {
                return initiateExternalDevice(context);
            }
            return completeExternalDevice(context, registrationId);
        } catch (DeviceRegistrationException e) {
            if (!e.isClientError()) {
                diagnosticLogger.logRegistrationFailure("Error registering the device: " + e.getMessage());
                LOG.error("Error registering the device for registrationId: " + registrationId, e);
                return errorResponse(e);
            }
            diagnosticLogger.logRegistrationFailure("The device could not be registered: " + e.getMessage());
            return errorResponse(e);
        }
    }

    private ExecutorResponse initiateExternalDevice(FlowExecutionContext context)
            throws DeviceRegistrationException {

        String username = context.getFlowUser().getUsername();
        DeviceRegistrationChallenge initiation = DeviceRegistrationHandler.initiate(username,
                context.getTenantDomain());
        diagnosticLogger.logRegistrationInitiated(initiation.getRegistrationId());

        Map<String, Object> contextProperties = new HashMap<>();
        contextProperties.put(DeviceRegistrationConstants.CTX_REGISTRATION_ID, initiation.getRegistrationId());
        contextProperties.put(DeviceRegistrationConstants.CTX_CHALLENGE, initiation.getChallenge());
        // Keep the one time flow id for the device's next request.
        contextProperties.put(OTFI, context.getProperty(DeviceRegistrationConstants.CTX_ONE_TIME_FLOW_ID));

        List<String> requiredFields = new ArrayList<>(Arrays.asList(DeviceRegistrationConstants.FIELD_PUBLIC_KEY,
                DeviceRegistrationConstants.FIELD_SIGNATURE));
        // A device policy needs the device data.
        if (resolvePolicyName(context.getCurrentNode()) != null) {
            requiredFields.add(DeviceRegistrationConstants.FIELD_DEVICE_DATA);
        }
        Map<String, String> additionalInfo = new HashMap<>();
        additionalInfo.put(DeviceRegistrationConstants.PROP_REGISTRATION_ID, initiation.getRegistrationId());
        additionalInfo.put(DeviceRegistrationConstants.PROP_CHALLENGE, initiation.getChallenge());
        additionalInfo.put(DeviceRegistrationConstants.PROP_ACCOUNT_HINT, LoggerUtils.getMaskedContent(username));

        ExecutorResponse response = new ExecutorResponse();
        response.setResult(STATUS_CLIENT_INPUT_REQUIRED);
        response.setRequiredData(requiredFields);
        response.setOptionalData(new ArrayList<>(
                Collections.singletonList(DeviceRegistrationConstants.FIELD_DEVICE_MODEL)));
        response.setAdditionalInfo(additionalInfo);
        response.setContextProperty(contextProperties);
        return response;
    }

    private ExecutorResponse completeExternalDevice(FlowExecutionContext context, String registrationId)
            throws DeviceRegistrationException {

        String challenge = (String) context.getProperty(DeviceRegistrationConstants.CTX_CHALLENGE);
        if (isBlank(challenge)) {
            return errorResponse(STATUS_USER_ERROR, ErrorMessage.ERROR_REGISTRATION_CONTEXT_NOT_FOUND,
                    registrationId);
        }
        Map<String, String> input = context.getUserInputData();
        String publicKey = input.get(DeviceRegistrationConstants.FIELD_PUBLIC_KEY);
        String deviceModel = input.get(DeviceRegistrationConstants.FIELD_DEVICE_MODEL);
        // The challenge is single use.
        context.getProperties().remove(DeviceRegistrationConstants.CTX_CHALLENGE);
        if (deviceModel != null && deviceModel.length() > DeviceRegistrationConstants.MAX_DEVICE_MODEL_LENGTH) {
            return errorResponse(STATUS_USER_ERROR, ErrorMessage.ERROR_INVALID_DEVICE_MODEL,
                    String.valueOf(DeviceRegistrationConstants.MAX_DEVICE_MODEL_LENGTH));
        }
        DeviceRegistrationHandler.verifySignature(registrationId, challenge, publicKey,
                input.get(DeviceRegistrationConstants.FIELD_SIGNATURE));

        String policyName = resolvePolicyName(context.getCurrentNode());
        if (policyName != null) {
            String deviceData = input.get(DeviceRegistrationConstants.FIELD_DEVICE_DATA);
            if (isBlank(deviceData)) {
                diagnosticLogger.logRegistrationFailure("Device data is required for policy evaluation "
                        + "but was not provided.");
                return errorResponse(STATUS_USER_ERROR, ErrorMessage.ERROR_DEVICE_DATA_REQUIRED);
            }
            ExecutorResponse policyResult = evaluatePolicy(policyName, registrationId, deviceData, publicKey,
                    context);
            if (policyResult != null) {
                return policyResult;
            }
        }

        String pairingCode = CrossDeviceRegistrationHandler.generatePairingCode();
        Map<String, Object> contextProperties = new HashMap<>();
        contextProperties.put(DeviceRegistrationConstants.CTX_PAIRING_CODE_HASH,
                CrossDeviceRegistrationHandler.hashPairingCode(pairingCode));
        contextProperties.put(DeviceRegistrationConstants.CTX_PAIRING_ATTEMPTS, "0");
        // Used to build the verified device after the pairing code.
        contextProperties.put(DeviceRegistrationConstants.CTX_PUBLIC_KEY, publicKey);
        if (isNotBlank(deviceModel)) {
            contextProperties.put(DeviceRegistrationConstants.CTX_DEVICE_MODEL, deviceModel);
        }
        // End the one time flow id so the browser continues with its own flow id, once its poll reports success.
        context.getProperties().remove(RETAIN_OTFI);
        context.getProperties().remove(DeviceRegistrationConstants.CTX_ONE_TIME_FLOW_ID);
        context.endPolling(true);

        Map<String, String> additionalInfo = new HashMap<>();
        additionalInfo.put(DeviceRegistrationConstants.PROP_DEVICE_ID, registrationId);
        additionalInfo.put(DeviceRegistrationConstants.PROP_PAIRING_CODE, pairingCode);
        additionalInfo.put(DeviceRegistrationConstants.PROP_ACCOUNT_HINT,
                LoggerUtils.getMaskedContent(context.getFlowUser().getUsername()));
        if (LOG.isDebugEnabled()) {
            LOG.debug("Device verified for registrationId: " + registrationId + ". Waiting for the pairing code.");
        }

        // The device gets this node's page; the browser continues without inputs.
        ExecutorResponse response = new ExecutorResponse();
        response.setResult(STATUS_USER_INPUT_REQUIRED);
        response.setAdditionalInfo(additionalInfo);
        response.setContextProperty(contextProperties);
        return response;
    }

    /**
     * FINISH_REGISTRATION: verify the pairing code shown on the device, then end polling. Three wrong codes end the
     * flow.
     *
     * @param context Flow execution context.
     * @return Executor response.
     */
    private ExecutorResponse finishCrossDeviceRegistration(FlowExecutionContext context) {

        String expectedHash = (String) context.getProperty(DeviceRegistrationConstants.CTX_PAIRING_CODE_HASH);
        if (expectedHash == null) {
            diagnosticLogger.logRegistrationFailure("The pairing code step runs before the device was verified.");
            return errorResponse(STATUS_ERROR, ErrorMessage.ERROR_INVALID_PHASE_ORDER, PHASE_FINISH_REGISTRATION);
        }
        String pairingCode = context.getUserInputData().get(DeviceRegistrationConstants.FIELD_PAIRING_CODE);
        if (pairingCode == null) {
            return pairingCodeResponse(context, STATUS_USER_INPUT_REQUIRED);
        }

        try {
            if (!CrossDeviceRegistrationHandler.matches(CrossDeviceRegistrationHandler.hashPairingCode(pairingCode),
                    expectedHash)) {
                int attempts = parseCount(context.getProperty(DeviceRegistrationConstants.CTX_PAIRING_ATTEMPTS)) + 1;
                if (attempts >= DeviceRegistrationConstants.MAX_PAIRING_ATTEMPTS) {
                    diagnosticLogger.logRegistrationFailure("The pairing code was entered wrongly too many times.");
                    return errorResponse(STATUS_USER_ERROR, ErrorMessage.ERROR_INVALID_PAIRING_CODE);
                }
                context.setProperty(DeviceRegistrationConstants.CTX_PAIRING_ATTEMPTS, String.valueOf(attempts));
                ExecutorResponse response = pairingCodeResponse(context, STATUS_RETRY);
                response.setErrorMessage(DeviceRegistrationConstants.I18N_INVALID_PAIRING_CODE);
                return response;
            }

            String registrationId = (String) context.getProperty(DeviceRegistrationConstants.CTX_REGISTRATION_ID);
            String deviceModel = (String) context.getProperty(DeviceRegistrationConstants.CTX_DEVICE_MODEL);
            VerifiedDevice verified = new VerifiedDevice.Builder()
                    .id(registrationId)
                    .deviceName(buildDeviceName(context, deviceModel))
                    .deviceModel(deviceModel)
                    .publicKey((String) context.getProperty(DeviceRegistrationConstants.CTX_PUBLIC_KEY))
                    .registeredAt(Timestamp.from(Instant.now()))
                    .build();

            clearDeviceProperties(context);
            context.getProperties().remove(DeviceRegistrationConstants.CTX_QR_COUNT);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Pairing code verified for registrationId: " + registrationId);
            }

            ExecutorResponse response = new ExecutorResponse();
            response.setResult(STATUS_COMPLETE);
            Map<String, Object> ctxProps = new HashMap<>();
            ctxProps.put(DeviceRegistrationConstants.CTX_DEVICE_REGISTRATION, verified);
            response.setContextProperty(ctxProps);
            return response;
        } catch (DeviceRegistrationException e) {
            diagnosticLogger.logRegistrationFailure("Error verifying the pairing code: " + e.getMessage());
            LOG.error("Error verifying the device registration pairing code.", e);
            return errorResponse(e);
        } catch (IllegalArgumentException e) {
            diagnosticLogger.logRegistrationFailure("The verified device in the flow context is incomplete.");
            return errorResponse(STATUS_ERROR, ErrorMessage.ERROR_REGISTRATION_CONTEXT_NOT_FOUND,
                    StringUtils.EMPTY);
        }
    }

    private ExecutorResponse pairingCodeResponse(FlowExecutionContext context, String status) {

        Map<String, String> additionalInfo = new HashMap<>();
        String deviceModel = (String) context.getProperty(DeviceRegistrationConstants.CTX_DEVICE_MODEL);
        if (isNotBlank(deviceModel)) {
            additionalInfo.put(DeviceRegistrationConstants.PROP_DEVICE_MODEL, deviceModel);
        }
        ExecutorResponse response = new ExecutorResponse();
        response.setResult(status);
        response.setRequiredData(new ArrayList<>(
                Collections.singletonList(DeviceRegistrationConstants.FIELD_PAIRING_CODE)));
        response.setAdditionalInfo(additionalInfo);
        return response;
    }

    /**
     * Resolve the phase a node runs.
     *
     * @param node Node of the executor.
     * @return Phase of the node, NATIVE_DEVICE_REGISTRATION if it does not name one.
     */
    private static String resolvePhase(NodeConfig node) {

        if (node == null || node.getExecutorConfig() == null || node.getExecutorConfig().getMetadata() == null) {
            return PHASE_NATIVE_DEVICE_REGISTRATION;
        }
        String phase = node.getExecutorConfig().getMetadata().get(PHASE);
        return isBlank(phase) ? PHASE_NATIVE_DEVICE_REGISTRATION : phase;
    }

    private void clearDeviceProperties(FlowExecutionContext context) {

        context.getProperties().remove(DeviceRegistrationConstants.CTX_REGISTRATION_ID);
        context.getProperties().remove(DeviceRegistrationConstants.CTX_CHALLENGE);
        context.getProperties().remove(DeviceRegistrationConstants.CTX_PUBLIC_KEY);
        context.getProperties().remove(DeviceRegistrationConstants.CTX_DEVICE_MODEL);
        context.getProperties().remove(DeviceRegistrationConstants.CTX_PAIRING_CODE_HASH);
        context.getProperties().remove(DeviceRegistrationConstants.CTX_PAIRING_ATTEMPTS);
        context.getProperties().remove(DeviceRegistrationConstants.CTX_QR_EXPIRES_AT);
    }

    private static int parseCount(Object value) {

        try {
            return value == null ? 0 : Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static long parseLong(Object value) {

        try {
            return value == null ? 0L : Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private static ExecutorResponse errorResponse(DeviceRegistrationException e) {

        return errorResponse(e.isClientError() ? STATUS_USER_ERROR : STATUS_ERROR, e.getErrorCode(), e.getMessage(),
                e.getDescription());
    }

    private static ExecutorResponse errorResponse(String status, ErrorMessage error, String... data) {

        String description = error.getDescription();
        if (data != null && data.length > 0) {
            description = String.format(description, (Object[]) data);
        }
        return errorResponse(status, error.getCode(), error.getMessage(), description);
    }

    private static ExecutorResponse errorResponse(String status, String code, String message, String description) {

        ExecutorResponse response = new ExecutorResponse();
        response.setResult(status);
        response.setErrorCode(code);
        response.setErrorMessage(message);
        response.setErrorDescription(description);
        return response;
    }

    private String buildDeviceName(FlowExecutionContext context, String deviceModel) {

        String username = context.getFlowUser().getUsername();
        String base = isNotBlank(username) ? username : "Unknown";
        return isNotBlank(deviceModel) ? base + "'s " + deviceModel : base + "'s Device";
    }

    private String resolvePolicyName(FlowExecutionContext context) {

        return resolvePolicyName(context.getCurrentNode());
    }

    private static String resolvePolicyName(NodeConfig node) {

        if (node == null || node.getExecutorConfig() == null) {
            return null;
        }
        Map<String, String> meta = node.getExecutorConfig().getMetadata();
        if (meta == null) {
            return null;
        }
        String policyName = meta.get(DeviceRegistrationConstants.META_POLICY_NAME);
        return isBlank(policyName) ? null : policyName;
    }

    private ExecutorResponse evaluatePolicy(String policyName, String registrationId, String deviceData,
                                            String publicKey, FlowExecutionContext context) {

        DeviceRegistrationComponentServiceHolder holder = DeviceRegistrationComponentServiceHolder.getInstance();
        DevicePolicyEvaluator evaluator = holder.getDevicePolicyEvaluator();

        try {
            // The evaluator verifies the device data token with the key established through the
            // challenge signature, then evaluates the policy against the resolved device data.
            DevicePolicyEvaluationResult result = evaluator.evaluateFromTokenByPolicyName(
                    policyName,
                    deviceData,
                    publicKey,
                    registrationId,
                    context.getApplicationId(),
                    context.getTenantDomain());
            if (result.isCompliant()) {
                // Device is compliant.
                diagnosticLogger.logPolicyEvaluation(policyName, true, null);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Device passed policy: " + policyName);
                }
                return null;
            }

            String failedFields = resolveFailureDetail(result);

            diagnosticLogger.logPolicyEvaluation(policyName, false, failedFields);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Device failed policy: " + policyName + " fields: [" + failedFields + "]");
            }

            ExecutorResponse response = new ExecutorResponse();
            response.setResult(STATUS_USER_ERROR);
            response.setErrorCode(ErrorMessage.ERROR_DEVICE_POLICY_NOT_COMPLIANT.getCode());
            response.setErrorMessage(ErrorMessage.ERROR_DEVICE_POLICY_NOT_COMPLIANT.getMessage());
            response.setErrorDescription(String.format(
                    ErrorMessage.ERROR_DEVICE_POLICY_NOT_COMPLIANT.getDescription(),
                    policyName, failedFields));
            return response;

        } catch (DevicePolicyClientException e) {
            diagnosticLogger.logRegistrationFailure(POLICY_EVALUATION_FAILED + policyName
                    + ": " + e.getMessage());
            ExecutorResponse response = new ExecutorResponse();
            response.setResult(STATUS_USER_ERROR);
            response.setErrorCode(e.getErrorCode());
            response.setErrorMessage(e.getMessage());
            response.setErrorDescription(e.getDescription());
            return response;
        } catch (DevicePolicyException e) {
            diagnosticLogger.logRegistrationFailure(POLICY_EVALUATION_FAILED + policyName);
            LOG.error(POLICY_EVALUATION_FAILED + policyName, e);
            ExecutorResponse response = new ExecutorResponse();
            response.setResult(STATUS_ERROR);
            response.setErrorCode(ErrorMessage.ERROR_WHILE_EVALUATING_POLICY.getCode());
            response.setErrorMessage(ErrorMessage.ERROR_WHILE_EVALUATING_POLICY.getMessage());
            response.setErrorDescription(String.format(
                    ErrorMessage.ERROR_WHILE_EVALUATING_POLICY.getDescription(), policyName));
            return response;
        }
    }

    /**
     * Resolves the failure detail reported to the device for a non-compliant evaluation result.
     *
     * @param result Non-compliant device policy evaluation result.
     * @return Comma separated failed or missing field names, or the policy not found detail.
     */
    private String resolveFailureDetail(DevicePolicyEvaluationResult result) {

        switch (result.getStatus()) {
            case NON_COMPLIANT:
                return String.join(", ", result.getFailedFields());
            case INCOMPLETE_DEVICE_DATA:
                return String.join(", ", result.getMissingFields());
            default:
                return StringUtils.EMPTY;
        }
    }
}
