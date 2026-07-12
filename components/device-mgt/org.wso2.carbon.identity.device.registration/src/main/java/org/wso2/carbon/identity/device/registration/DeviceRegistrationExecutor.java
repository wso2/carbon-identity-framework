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

package org.wso2.carbon.identity.device.registration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.device.mgt.api.exception.DeviceMgtClientException;
import org.wso2.carbon.identity.device.mgt.api.exception.DeviceMgtException;
import org.wso2.carbon.identity.device.mgt.api.model.Device;
import org.wso2.carbon.identity.device.mgt.api.service.DeviceManagementService;
import org.wso2.carbon.identity.device.policy.api.service.DevicePolicyEvaluator;
import org.wso2.carbon.identity.device.registration.internal.DeviceRegistrationDiagnosticLogger;
import org.wso2.carbon.identity.device.registration.internal.DeviceRegistrationExecutorDataHolder;
import org.wso2.carbon.identity.device.registration.internal.constant.ErrorMessage;
import org.wso2.carbon.identity.device.registration.internal.handler.DeviceRegistrationHandler;
import org.wso2.carbon.identity.device.registration.internal.model.DeviceRegistrationChallenge;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.execution.engine.graph.Executor;
import org.wso2.carbon.identity.flow.execution.engine.model.ExecutorResponse;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.mgt.Constants.FlowTypes;
import org.wso2.carbon.identity.flow.mgt.model.NodeConfig;
import org.wso2.carbon.identity.policy.evaluation.api.exception.PolicyEvaluationException;
import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementClientException;
import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementException;
import org.wso2.carbon.identity.rule.evaluation.api.exception.RuleEvaluationException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorStatus.STATUS_CLIENT_INPUT_REQUIRED;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorStatus.STATUS_COMPLETE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorStatus.STATUS_ERROR;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorStatus.STATUS_USER_ERROR;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.USERNAME_CLAIM_URI;

/**
 * Flow executor for generic device registration.
 *
 * Implements a two-phase challenge-response protocol:
 *   Phase 1 — first call, no registrationId in context:
 *     Generates challenge via DeviceRegistrationHandler.initiate(), stores registrationId in
 *     FlowExecutionContext.properties, returns STATUS_CLIENT_INPUT_REQUIRED so the SDK can sign
 *     the challenge natively.
 *
 *   Phase 2 — second call, registrationId present in context:
 *     Verifies the ECDSA signature. If an optional policyName is configured in executor
 *     metadata, evaluates device compliance before persisting. In REGISTRATION flows the
 *     verified device is stored in context for RegistrationFlowCompletionListener to persist
 *     after UserProvisioningExecutor assigns the real userId. In other flows it is persisted
 *     immediately.
 */
public class DeviceRegistrationExecutor implements Executor {

    private static final Log LOG = LogFactory.getLog(DeviceRegistrationExecutor.class);

    private final DeviceRegistrationDiagnosticLogger diagnosticLogger = new DeviceRegistrationDiagnosticLogger();

    private static final String CTX_REGISTRATION_ID = "device.registration.id";

    private static final String PROP_REGISTRATION_ID = "registrationId";
    private static final String PROP_CHALLENGE = "challenge";

    private static final String FIELD_PUBLIC_KEY = "publicKey";
    private static final String FIELD_SIGNATURE = "signature";
    private static final String FIELD_DEVICE_MODEL = "deviceModel";
    private static final String FIELD_METADATA = "metadata";
    private static final String FIELD_DEVICE_DATA = "deviceData";

    private static final String META_POLICY_NAME = "policyName";

    // Written to context after persistDevice() so rollback() can delete the record if a later executor fails.
    private static final String CTX_PERSISTED_DEVICE_ID = "device.persisted.id";

    @Override
    public String getName() {
        return DeviceRegistrationExecutorConstants.EXECUTOR_NAME;
    }

    @Override
    public ExecutorResponse execute(FlowExecutionContext context) throws FlowEngineException {

        String registrationId = (String) context.getProperty(CTX_REGISTRATION_ID);

        if (registrationId == null) {
            return initiateRegistration(context);
        }
        return completeRegistration(context, registrationId);
    }

    @Override
    public ExecutorResponse rollback(FlowExecutionContext context) throws FlowEngineException {

        String persistedDeviceId = (String) context.getProperty(CTX_PERSISTED_DEVICE_ID);
        if (persistedDeviceId != null) {
            DeviceManagementService service =
                    DeviceRegistrationExecutorDataHolder.getInstance().getDeviceManagementService();
            try {
                service.deleteDevice(persistedDeviceId, context.getTenantDomain());
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Rolled back persisted device: " + persistedDeviceId
                            + " for tenant: " + context.getTenantDomain());
                }
            } catch (DeviceMgtException e) {
                LOG.error("Failed to rollback persisted device: " + persistedDeviceId
                        + " in tenant: " + context.getTenantDomain(), e);
                ExecutorResponse response = new ExecutorResponse(STATUS_ERROR);
                response.setErrorCode(e.getErrorCode());
                response.setErrorMessage(e.getMessage());
                response.setErrorDescription(e.getDescription());
                return response;
            }
        }
        // Challenge cache entry expires automatically via TTL; nothing else to undo.
        return new ExecutorResponse(STATUS_COMPLETE);
    }

    @Override
    public List<String> getInitiationData() {
        return Collections.singletonList(USERNAME_CLAIM_URI);
    }

    private String resolvePolicyName(FlowExecutionContext context) {

        NodeConfig node = context.getCurrentNode();
        if (node == null || node.getExecutorConfig() == null) {
            return null;
        }
        Map<String, String> meta = node.getExecutorConfig().getMetadata();
        if (meta == null) {
            return null;
        }
        String policyName = meta.get(META_POLICY_NAME);
        return isBlank(policyName) ? null : policyName;
    }

    private ExecutorResponse initiateRegistration(FlowExecutionContext context) {

        try {
            String userId = context.getFlowUser().getUserId();
            String userIdentifier = isNotBlank(userId) ? userId : context.getFlowUser().getUsername();

            if (isBlank(userIdentifier)) {
                diagnosticLogger.logRegistrationFailure("User could not be identified to initiate device "
                        + "registration.");
                ExecutorResponse response = new ExecutorResponse();
                response.setResult(STATUS_ERROR);
                response.setErrorCode(ErrorMessage.ERROR_USER_NOT_IDENTIFIED.getCode());
                response.setErrorMessage(ErrorMessage.ERROR_USER_NOT_IDENTIFIED.getMessage());
                response.setErrorDescription(ErrorMessage.ERROR_USER_NOT_IDENTIFIED.getDescription());
                return response;
            }
            // Set the resolved user identifier on thread-local context so downstream services
            // (audit, events) can read it.
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(userIdentifier);

            DeviceRegistrationChallenge initiation =
                    DeviceRegistrationHandler.initiate(userIdentifier, context.getTenantDomain());

            diagnosticLogger.logRegistrationInitiated(initiation.getRegistrationId());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Device registration initiated for user: " + LoggerUtils.getMaskedContent(userIdentifier)
                        + " registrationId: " + initiation.getRegistrationId());
            }

            Map<String, String> additionalInfo = new HashMap<>();
            additionalInfo.put(PROP_REGISTRATION_ID, initiation.getRegistrationId());
            additionalInfo.put(PROP_CHALLENGE, initiation.getChallenge());

            Map<String, Object> contextProperties = new HashMap<>();
            contextProperties.put(CTX_REGISTRATION_ID, initiation.getRegistrationId());

            List<String> requiredFields = new ArrayList<>(
                    Arrays.asList(FIELD_PUBLIC_KEY, FIELD_SIGNATURE));
            List<String> optionalFields = new ArrayList<>(
                    Arrays.asList(FIELD_DEVICE_MODEL, FIELD_METADATA));
            String policyName = resolvePolicyName(context);
            if (policyName != null) {
                requiredFields.add(FIELD_DEVICE_DATA);
            }

            ExecutorResponse response = new ExecutorResponse();
            response.setResult(STATUS_CLIENT_INPUT_REQUIRED);
            response.setRequiredData(requiredFields);
            response.setOptionalData(optionalFields);
            response.setAdditionalInfo(additionalInfo);
            response.setContextProperty(contextProperties);
            return response;

        } catch (DeviceMgtException e) {
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

        DeviceManagementService service =
                DeviceRegistrationExecutorDataHolder.getInstance().getDeviceManagementService();
        try {
            Map<String, String> input = context.getUserInputData();
            String deviceModel = input.get(FIELD_DEVICE_MODEL);
            String deviceName = buildDeviceName(context, deviceModel);

            // Step 1: Verify signature and clear registration cache entry.
            Device verified = DeviceRegistrationHandler.verify(
                    registrationId,
                    input.get(FIELD_PUBLIC_KEY),
                    input.get(FIELD_SIGNATURE),
                    deviceName,
                    deviceModel,
                    input.get(FIELD_METADATA),
                    context.getTenantDomain());

            // Step 2: Policy compliance check (skipped when policyName not configured).
            String policyName = resolvePolicyName(context);
            if (policyName != null) {
                if (isBlank(input.get(FIELD_DEVICE_DATA))) {
                    diagnosticLogger.logRegistrationFailure("Device data is required for policy evaluation "
                            + "but was not provided.");
                    ExecutorResponse response = new ExecutorResponse();
                    response.setResult(STATUS_USER_ERROR);
                    response.setErrorCode(ErrorMessage.ERROR_DEVICE_DATA_REQUIRED.getCode());
                    response.setErrorMessage(ErrorMessage.ERROR_DEVICE_DATA_REQUIRED.getMessage());
                    response.setErrorDescription(ErrorMessage.ERROR_DEVICE_DATA_REQUIRED.getDescription());
                    return response;
                }
                ExecutorResponse policyResult = evaluatePolicy(policyName, registrationId, context);
                if (policyResult != null) {
                    return policyResult;
                }
            }

            if (FlowTypes.REGISTRATION.getType().equals(context.getFlowType())) {
                ExecutorResponse response = new ExecutorResponse();
                response.setResult(STATUS_COMPLETE);
                Map<String, Object> ctxProps = new HashMap<>();
                ctxProps.put(DeviceRegistrationExecutorConstants.CTX_DEVICE_REGISTRATION, verified);
                response.setContextProperty(ctxProps);
                return response;
            }

            // Non-registration flow: the user already exists, so bind the real userId and persist now.
            // Some flows (e.g. DEVICE_REGISTRATION) identify the user by username via UserResolveExecutor
            // without setting a userId, so resolve it from the username when it is not already present.
            String userId = context.getFlowUser().getUserId();
            if (isBlank(userId)) {
                userId = resolveUserIdFromUsername(context);
            }
            if (isBlank(userId)) {
                diagnosticLogger.logRegistrationFailure("User could not be identified to persist the "
                        + "registered device.");
                ExecutorResponse response = new ExecutorResponse();
                response.setResult(STATUS_ERROR);
                response.setErrorCode(ErrorMessage.ERROR_USER_NOT_IDENTIFIED.getCode());
                response.setErrorMessage(ErrorMessage.ERROR_USER_NOT_IDENTIFIED.getMessage());
                response.setErrorDescription(ErrorMessage.ERROR_USER_NOT_IDENTIFIED.getDescription());
                return response;
            }
            Device toPersist = new Device.Builder(verified).userId(userId).build();
            service.persistDevice(toPersist, context.getTenantDomain());

            diagnosticLogger.logRegistrationCompleted(registrationId);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Device registration completed for registrationId: " + registrationId);
            }
            // Record the device ID so rollback() can delete it if a subsequent executor fails.
            ExecutorResponse response = new ExecutorResponse(STATUS_COMPLETE);
            Map<String, Object> ctxProps = new HashMap<>();
            ctxProps.put(CTX_PERSISTED_DEVICE_ID, toPersist.getId());
            response.setContextProperty(ctxProps);
            return response;

        } catch (DeviceMgtClientException e) {
            ExecutorResponse response = new ExecutorResponse();
            response.setResult(STATUS_USER_ERROR);
            response.setErrorCode(e.getErrorCode());
            response.setErrorMessage(e.getMessage());
            response.setErrorDescription(e.getDescription());
            return response;
        } catch (DeviceMgtException e) {
            diagnosticLogger.logRegistrationFailure("Error completing device registration: " + e.getMessage());
            LOG.error("Error completing device registration for registrationId: "
                    + registrationId, e);
            ExecutorResponse response = new ExecutorResponse();
            response.setResult(STATUS_ERROR);
            response.setErrorCode(e.getErrorCode());
            response.setErrorMessage(e.getMessage());
            response.setErrorDescription(e.getDescription());
            return response;
        }
    }

    private String resolveUserIdFromUsername(FlowExecutionContext context) {

        String username = context.getFlowUser().getUsername();
        if (isBlank(username)) {
            return null;
        }
        try {
            RealmService realmService = DeviceRegistrationExecutorDataHolder.getInstance().getRealmService();
            int tenantId = realmService.getTenantManager().getTenantId(context.getTenantDomain());
            UserStoreManager userStoreManager = realmService.getTenantUserRealm(tenantId).getUserStoreManager();
            return ((AbstractUserStoreManager) userStoreManager).getUserIDFromUserName(username);
        } catch (UserStoreException e) {
            LOG.error("Error resolving userId from username for tenant: " + context.getTenantDomain(), e);
            return null;
        }
    }

    private String buildDeviceName(FlowExecutionContext context, String deviceModel) {

        String userId = context.getFlowUser().getUserId();
        String username = isNotBlank(userId) ? userId : context.getFlowUser().getUsername();
        String base = isNotBlank(username) ? username : "Unknown";
        return isNotBlank(deviceModel) ? base + "'s " + deviceModel : base + "'s Device";
    }

    private ExecutorResponse evaluatePolicy(String policyName, String registrationId, FlowExecutionContext context) {

        DeviceRegistrationExecutorDataHolder holder = DeviceRegistrationExecutorDataHolder.getInstance();
        DevicePolicyEvaluator evaluator = holder.getDevicePolicyEvaluator();

        // The device data is a JWT signed with the same key that was just verified against the challenge,
        // so its claims can be trusted after signature, freshness and replay verification. The challenge
        // check (already passed above) binds this request to the registration, so no extra token binding
        // is needed here; registrationId is passed only for diagnostic correlation.
        Map<String, Object> deviceData;
        try {
            deviceData = holder.getDeviceTokenVerifier().verifyWithPublicKey(
                    context.getUserInputData().get(FIELD_DEVICE_DATA),
                    context.getUserInputData().get(FIELD_PUBLIC_KEY),
                    registrationId,
                    context.getTenantDomain());
        } catch (PolicyManagementClientException e) {
            diagnosticLogger.logRegistrationFailure("Device data token verification failed: " + e.getMessage());
            ExecutorResponse response = new ExecutorResponse();
            response.setResult(STATUS_USER_ERROR);
            response.setErrorCode(e.getErrorCode());
            response.setErrorMessage(e.getMessage());
            response.setErrorDescription(e.getDescription());
            return response;
        } catch (PolicyManagementException e) {
            diagnosticLogger.logRegistrationFailure("Device data token verification failed: " + e.getMessage());
            LOG.error("Device data token verification failed during registration.", e);
            ExecutorResponse response = new ExecutorResponse();
            response.setResult(STATUS_ERROR);
            response.setErrorCode(e.getErrorCode());
            response.setErrorMessage(e.getMessage());
            response.setErrorDescription(e.getDescription());
            return response;
        }

        holder.getIntegrityDataEnricher().enrich(deviceData, context.getApplicationId(),
                context.getTenantDomain());

        try {
            String failedFields = evaluator.evaluate(policyName, deviceData, context.getTenantDomain());
            if (failedFields == null) {
                // Device is compliant.
                diagnosticLogger.logPolicyEvaluation(policyName, true, null);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Device passed policy: " + policyName);
                }
                return null;
            }

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

        } catch (PolicyManagementException | RuleEvaluationException | PolicyEvaluationException e) {
            diagnosticLogger.logRegistrationFailure("Policy evaluation failed for policy: " + policyName);
            LOG.error("Policy evaluation failed for policy: " + policyName, e);
            ExecutorResponse response = new ExecutorResponse();
            response.setResult(STATUS_ERROR);
            response.setErrorCode(ErrorMessage.ERROR_WHILE_EVALUATING_POLICY.getCode());
            response.setErrorMessage(ErrorMessage.ERROR_WHILE_EVALUATING_POLICY.getMessage());
            response.setErrorDescription(String.format(
                    ErrorMessage.ERROR_WHILE_EVALUATING_POLICY.getDescription(), policyName));
            return response;
        }
    }
}
