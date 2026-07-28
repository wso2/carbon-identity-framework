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

package org.wso2.carbon.identity.device.policy.internal.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.client.attestation.mgt.exceptions.ClientAttestationMgtException;
import org.wso2.carbon.identity.client.attestation.mgt.model.ClientAttestationContext;
import org.wso2.carbon.identity.client.attestation.mgt.services.ClientAttestationService;
import org.wso2.carbon.identity.client.attestation.mgt.utils.Constants;
import org.wso2.carbon.identity.device.policy.internal.component.DevicePolicyComponentServiceHolder;
import org.wso2.carbon.identity.device.policy.internal.service.IntegrityDataEnricher;
import org.wso2.carbon.identity.device.policy.internal.util.DevicePolicyDiagnosticLogger;

import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link IntegrityDataEnricher}.
 */
public class IntegrityDataEnricherImpl implements IntegrityDataEnricher {

    private static final Log LOG = LogFactory.getLog(IntegrityDataEnricherImpl.class);

    private static final String ATTESTATION_TOKEN_KEY = "attestationToken";
    private static final String ANDROID_INTEGRITY_KEY = "androidIntegrity";
    private static final String IOS_INTEGRITY_KEY = "iosIntegrity";
    private static final String INTEGRITY_FAILED = "INTEGRITY_FAILED";

    private final DevicePolicyDiagnosticLogger diagnosticLogger = new DevicePolicyDiagnosticLogger();

    @Override
    public void enrich(Map<String, Object> deviceData, String appId, String tenantDomain) {

        String attestationToken = (String) deviceData.remove(ATTESTATION_TOKEN_KEY);
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
            }

        } catch (ClientAttestationMgtException e) {
            diagnosticLogger.logIntegrityEnrichmentFailure(appId,
                    "Attestation token verification failed.");
            LOG.error("Failed to verify attestation token for device policy enrichment "
                    + "in application: " + appId + ", tenant: " + tenantDomain, e);
            deviceData.put(ANDROID_INTEGRITY_KEY, INTEGRITY_FAILED);
            deviceData.put(IOS_INTEGRITY_KEY, String.valueOf(false));
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
