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

package org.wso2.carbon.identity.device.policy.internal.util;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.device.mgt.api.exception.DeviceMgtException;
import org.wso2.carbon.identity.device.mgt.api.model.Device;
import org.wso2.carbon.identity.device.policy.api.constant.DevicePolicyErrorMessage;
import org.wso2.carbon.identity.device.policy.api.exception.DevicePolicyException;
import org.wso2.carbon.identity.device.policy.api.exception.DevicePolicyServerException;
import org.wso2.carbon.identity.device.policy.internal.component.DevicePolicyComponentServiceHolder;
import org.wso2.carbon.identity.device.policy.internal.constant.DeviceTokenConstants;
import org.wso2.carbon.identity.device.policy.internal.service.impl.DeviceTokenReplayProtectionService;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Extracts and verifies device attribute data from a signed device JWT.
 * The JWT header must contain a deviceId custom parameter used to look up the registered public key.
 * Signature is verified using ECDSA before claims are returned.
 */
public class DeviceTokenExtractor {

    private static final Log LOG = LogFactory.getLog(DeviceTokenExtractor.class);
    private static final String DEVICE_ID_PARAM = "deviceId";

    private final DevicePolicyDiagnosticLogger diagnosticLogger = new DevicePolicyDiagnosticLogger();
    private final DeviceTokenReplayProtectionService replayService = DeviceTokenReplayProtectionService.getInstance();

    /**
     * Extracts verified device attributes from a raw JWT string.
     * Use this when the token has already been extracted from a query param or header.
     *
     * @param token        Raw JWT string to parse and verify.
     * @param tenantDomain Tenant domain used for device lookup.
     * @return Map of device field names to their values from verified JWT claims.
     * @throws DevicePolicyException If the token is invalid or signature verification fails.
     */
    public Map<String, Object> extractFromToken(String token, String tenantDomain)
            throws DevicePolicyException {

        try {
            SignedJWT signedJWT = parseSignedJWT(token);

            Object deviceIdParam = signedJWT.getHeader().getCustomParam(DEVICE_ID_PARAM);
            if (!(deviceIdParam instanceof String) || StringUtils.isBlank((String) deviceIdParam)) {
                diagnosticLogger.logTokenValidationFailure(null, "Device token header is missing the deviceId.");
                throw DevicePolicyExceptionHandler.handleClientException(
                        DevicePolicyErrorMessage.ERROR_DEVICE_TOKEN_MISSING_DEVICE_ID);
            }
            String deviceId = (String) deviceIdParam;
            // Strip any stray leading/trailing quotes or backslashes introduced by JWT serialisation bugs.
            deviceId = deviceId.replaceAll("[\"\\\\]+$", "").replaceAll("^[\"\\\\]+", "").trim();

            Device device = DevicePolicyComponentServiceHolder.getInstance()
                    .getDeviceManagementService()
                    .getDeviceById(deviceId, tenantDomain);

            if (device == null || device.getStatus() != Device.Status.ACTIVE) {
                diagnosticLogger.logTokenValidationFailure(deviceId,
                        "Device is not registered or is not active.");
                throw DevicePolicyExceptionHandler.handleClientException(
                        DevicePolicyErrorMessage.ERROR_DEVICE_NOT_ACTIVE, deviceId);
            }

            ECPublicKey publicKey = decodePublicKey(device.getPublicKey());
            return verifyAndExtract(signedJWT, publicKey, deviceId, tenantDomain);

        } catch (ParseException e) {
            diagnosticLogger.logTokenValidationFailure(null, "Device token could not be parsed.");
            throw DevicePolicyExceptionHandler.handleClientException(
                    DevicePolicyErrorMessage.ERROR_DEVICE_TOKEN_PARSE_FAILED, e);
        } catch (JOSEException e) {
            diagnosticLogger.logTokenValidationFailure(null, "ECDSA verification of the device token failed.");
            throw DevicePolicyExceptionHandler.handleServerException(
                    DevicePolicyErrorMessage.ERROR_DEVICE_ECDSA_VERIFICATION_FAILED, e);
        } catch (DeviceMgtException e) {
            diagnosticLogger.logTokenValidationFailure(null, "Device lookup failed during token validation.");
            throw DevicePolicyExceptionHandler.handleServerException(
                    DevicePolicyErrorMessage.ERROR_DEVICE_LOOKUP_FAILED, e);
        }
    }

    /**
     * Extracts verified device attributes from a JWT signed with a caller-supplied public key.
     * Unlike {@link #extractFromToken(String, String)}, this does not look the device up in the registry;
     * the key is supplied by the caller (e.g. the registration executor, which has just established trust
     * in the key via the challenge signature). Signature, {@code iat} freshness and {@code jti} single-use
     * are still enforced before the claims are trusted.
     *
     * @param token           Raw JWT string to parse and verify.
     * @param base64PublicKey Base64-encoded X.509 EC public key to verify the signature against.
     * @param correlationId   Identifier used only for diagnostic correlation (e.g. the registrationId).
     * @param tenantDomain    Tenant domain used for replay-store scoping.
     * @return Map of device field names to their values from verified JWT claims.
     * @throws DevicePolicyException If the token is invalid, stale, or replayed.
     */
    public Map<String, Object> extractWithPublicKey(String token, String base64PublicKey,
            String correlationId, String tenantDomain) throws DevicePolicyException {

        try {
            SignedJWT signedJWT = parseSignedJWT(token);
            ECPublicKey publicKey = decodePublicKey(base64PublicKey);
            return verifyAndExtract(signedJWT, publicKey, correlationId, tenantDomain);

        } catch (ParseException e) {
            diagnosticLogger.logTokenValidationFailure(correlationId, "Device token could not be parsed.");
            throw DevicePolicyExceptionHandler.handleClientException(
                    DevicePolicyErrorMessage.ERROR_DEVICE_TOKEN_PARSE_FAILED, e);
        } catch (JOSEException e) {
            diagnosticLogger.logTokenValidationFailure(correlationId,
                    "ECDSA verification of the device token failed.");
            throw DevicePolicyExceptionHandler.handleServerException(
                    DevicePolicyErrorMessage.ERROR_DEVICE_ECDSA_VERIFICATION_FAILED, e);
        }
    }

    private SignedJWT parseSignedJWT(String token) throws DevicePolicyException, ParseException {

        JWT parsedJWT = JWTParser.parse(token);
        if (!(parsedJWT instanceof SignedJWT)) {
            diagnosticLogger.logTokenValidationFailure(null, "Device token is not a signed JWT.");
            throw DevicePolicyExceptionHandler.handleClientException(
                    DevicePolicyErrorMessage.ERROR_DEVICE_TOKEN_PARSE_FAILED);
        }
        return (SignedJWT) parsedJWT;
    }

    private Map<String, Object> verifyAndExtract(SignedJWT signedJWT, ECPublicKey publicKey,
            String correlationId, String tenantDomain)
            throws DevicePolicyException, JOSEException, ParseException {

        if (!signedJWT.verify(new ECDSAVerifier(publicKey))) {
            diagnosticLogger.logTokenValidationFailure(correlationId, "Device token signature is invalid.");
            throw DevicePolicyExceptionHandler.handleClientException(
                    DevicePolicyErrorMessage.ERROR_DEVICE_TOKEN_SIGNATURE_INVALID, correlationId);
        }

        // Signature is valid, but a valid token can still be replayed. Enforce iat freshness and
        // single-use of the jti before trusting the claims.
        JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
        verifyTokenFreshnessAndUniqueness(correlationId, claimsSet, IdentityTenantUtil.getTenantId(tenantDomain));

        Map<String, Object> deviceData = new HashMap<>();
        claimsSet.getClaims().forEach((key, value) -> {
            if (value != null) {
                deviceData.put(key, String.valueOf(value));
            }
        });

        diagnosticLogger.logTokenValidationSuccess(correlationId);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Device token verified successfully for: " + correlationId);

        }
        return deviceData;
    }

    private void verifyTokenFreshnessAndUniqueness(String correlationId, JWTClaimsSet claimsSet, int tenantId)
            throws DevicePolicyException {

        String jti = claimsSet.getJWTID();
        if (jti == null || jti.trim().isEmpty()) {
            diagnosticLogger.logTokenValidationFailure(correlationId, "Device token is missing the jti claim.");
            throw DevicePolicyExceptionHandler.handleClientException(
                    DevicePolicyErrorMessage.ERROR_DEVICE_TOKEN_MISSING_JTI);
        }

        Date issuedAt = claimsSet.getIssueTime();
        if (issuedAt == null) {
            diagnosticLogger.logTokenValidationFailure(correlationId, "Device token is missing the iat claim.");
            throw DevicePolicyExceptionHandler.handleClientException(
                    DevicePolicyErrorMessage.ERROR_DEVICE_TOKEN_MISSING_IAT);
        }

        long age = System.currentTimeMillis() - issuedAt.getTime();
        // Reject tokens older than the freshness window, or issued in the future beyond the allowed skew.
        if (age > DeviceTokenConstants.TOKEN_FRESHNESS_WINDOW_MILLIS
                || age < -DeviceTokenConstants.CLOCK_SKEW_MILLIS) {
            diagnosticLogger.logTokenValidationFailure(correlationId,
                    "Device token iat is outside the accepted freshness window.");
            throw DevicePolicyExceptionHandler.handleClientException(
                    DevicePolicyErrorMessage.ERROR_DEVICE_TOKEN_EXPIRED,
                    String.valueOf(DeviceTokenConstants.TOKEN_FRESHNESS_WINDOW_MILLIS / 1000));
        }

        replayService.assertUnusedAndRecord(jti, issuedAt, tenantId, correlationId);
    }

    private ECPublicKey decodePublicKey(String base64PublicKey)
            throws DevicePolicyServerException {

        if (base64PublicKey == null) {
            throw DevicePolicyExceptionHandler.handleServerException(
                    DevicePolicyErrorMessage.ERROR_DEVICE_PUBLIC_KEY_DECODE_FAILED);
        }
        try {
            byte[] keyBytes = Base64.getDecoder().decode(base64PublicKey);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            return (ECPublicKey) KeyFactory.getInstance("EC").generatePublic(keySpec);
        } catch (IllegalArgumentException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw DevicePolicyExceptionHandler.handleServerException(
                    DevicePolicyErrorMessage.ERROR_DEVICE_PUBLIC_KEY_DECODE_FAILED, e);
        }
    }
}
