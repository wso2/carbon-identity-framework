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

package org.wso2.carbon.identity.device.policy.internal.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.client.attestation.mgt.exceptions.ClientAttestationMgtException;
import org.wso2.carbon.identity.client.attestation.mgt.model.ClientAttestationContext;
import org.wso2.carbon.identity.client.attestation.mgt.services.ClientAttestationService;
import org.wso2.carbon.identity.client.attestation.mgt.utils.Constants;
import org.wso2.carbon.identity.device.policy.api.constant.DevicePolicyErrorMessage;
import org.wso2.carbon.identity.device.policy.api.exception.DevicePolicyException;
import org.wso2.carbon.identity.device.policy.internal.component.DevicePolicyComponentServiceHolder;
import org.wso2.carbon.identity.device.policy.internal.constant.DeviceTokenConstants;
import org.wso2.carbon.identity.device.policy.internal.util.DevicePolicyDiagnosticLogger;
import org.wso2.carbon.identity.device.policy.internal.util.DevicePolicyExceptionHandler;

import java.util.List;
import java.util.Map;

/**
 * Enriches the device data map with platform-verified integrity values before policy evaluation.
 *
 * <p>Handles both Android and iOS platforms via the underlying client attestation service, which
 * detects the platform automatically from the token format (JWE for Android, CBOR for iOS).
 * Removes {@code attestationToken} from the map before returning — policy rules never see the raw token.
 * Sets {@code androidIntegrity} for Android devices and {@code iosIntegrity} for iOS devices.
 * When the client attestation service is not available, both fields are set to their failed values.
 */
public class IntegrityDataEnricher {

    private static final Log LOG = LogFactory.getLog(IntegrityDataEnricher.class);

    private static final String ANDROID_INTEGRITY_KEY = "androidIntegrity";
    private static final String IOS_INTEGRITY_KEY = "iosIntegrity";
    private static final String INTEGRITY_FAILED = "INTEGRITY_FAILED";

    private final DevicePolicyDiagnosticLogger diagnosticLogger = new DevicePolicyDiagnosticLogger();

    /**
     * Enriches deviceData with platform-verified integrity values.
     *
     * @param deviceData   Mutable map of device attributes.
     * @param appId        Application resource ID for loading attestation credentials.
     * @param tenantDomain Tenant domain.
     * @throws DevicePolicyException If attestation verification fails.
     */
    public void enrich(Map<String, Object> deviceData, String appId, String tenantDomain)
            throws DevicePolicyException {

        String attestationToken = (String) deviceData.remove(DeviceTokenConstants.ATTESTATION_TOKEN_KEY);
        deviceData.remove(ANDROID_INTEGRITY_KEY);
        deviceData.remove(IOS_INTEGRITY_KEY);
        if (attestationToken == null) {
            return;
        }

        ClientAttestationService attestationService = DevicePolicyComponentServiceHolder
                .getInstance().getClientAttestationService();

        if (attestationService == null) {
            diagnosticLogger.logIntegrityEnrichmentFailure(appId, "ClientAttestationService is not available.");
            if (LOG.isDebugEnabled()) {
                LOG.debug("ClientAttestationService is not available. Skipping attestation enrichment.");
            }
            deviceData.put(ANDROID_INTEGRITY_KEY, INTEGRITY_FAILED);
            deviceData.put(IOS_INTEGRITY_KEY, String.valueOf(false));
            return;
        }

        try {
            ClientAttestationContext ctx = attestationService
                    .validateAttestation(attestationToken, appId, tenantDomain);

            if (ctx.getClientType() == Constants.ClientTypes.ANDROID) {
                deviceData.put(ANDROID_INTEGRITY_KEY,
                        resolveAndroidIntegrityLevel(ctx.getDeviceIntegrityVerdicts()));
            } else if (ctx.getClientType() == Constants.ClientTypes.IOS) {
                deviceData.put(IOS_INTEGRITY_KEY, String.valueOf(ctx.isAttested()));
            } else {
                diagnosticLogger.logIntegrityEnrichmentFailure(appId,
                        "Unrecognized client type from attestation context.");
                deviceData.put(ANDROID_INTEGRITY_KEY, INTEGRITY_FAILED);
                deviceData.put(IOS_INTEGRITY_KEY, String.valueOf(false));
            }

        } catch (ClientAttestationMgtException e) {
            diagnosticLogger.logIntegrityEnrichmentFailure(appId,
                    "Attestation token verification failed.");
            throw DevicePolicyExceptionHandler.handleClientException(
                    DevicePolicyErrorMessage.ERROR_DEVICE_ATTESTATION_VERIFICATION_FAILED, e, appId, tenantDomain);
        }
    }

    // Priority: MEETS_STRONG_INTEGRITY > MEETS_DEVICE_INTEGRITY > MEETS_BASIC_INTEGRITY > MEETS_VIRTUAL_INTEGRITY.
    private String resolveAndroidIntegrityLevel(List<String> verdicts) {

        if (verdicts == null || verdicts.isEmpty()) {
            return INTEGRITY_FAILED;
        }
        if (verdicts.contains("MEETS_STRONG_INTEGRITY")) {
            return "MEETS_STRONG_INTEGRITY";
        }
        if (verdicts.contains("MEETS_DEVICE_INTEGRITY")) {
            return "MEETS_DEVICE_INTEGRITY";
        }
        if (verdicts.contains("MEETS_BASIC_INTEGRITY")) {
            return "MEETS_BASIC_INTEGRITY";
        }
        if (verdicts.contains("MEETS_VIRTUAL_INTEGRITY")) {
            return "MEETS_VIRTUAL_INTEGRITY";
        }
        return INTEGRITY_FAILED;
    }
}
