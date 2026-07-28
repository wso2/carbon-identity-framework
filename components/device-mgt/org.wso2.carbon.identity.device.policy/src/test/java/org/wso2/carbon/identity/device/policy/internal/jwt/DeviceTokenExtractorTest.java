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

package org.wso2.carbon.identity.device.policy.internal.jwt;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.device.mgt.api.exception.DeviceMgtException;
import org.wso2.carbon.identity.device.mgt.api.model.Device;
import org.wso2.carbon.identity.device.mgt.api.service.DeviceManagementService;
import org.wso2.carbon.identity.device.policy.api.constant.DevicePolicyErrorMessage;
import org.wso2.carbon.identity.device.policy.internal.component.DevicePolicyComponentServiceHolder;
import org.wso2.carbon.identity.device.policy.internal.constant.DeviceTokenConstants;
import org.wso2.carbon.identity.device.policy.internal.service.impl.DeviceTokenReplayService;
import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementClientException;
import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementException;
import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementServerException;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Unit tests for DeviceTokenExtractor.
 */
public class DeviceTokenExtractorTest {

    private static final String TENANT_DOMAIN = "carbon.super";
    private static final int TENANT_ID = -1234;
    private static final String DEVICE_ID = "test-device-id";
    private static final String CORRELATION_ID = "test-correlation-id";
    private static final String JTI = "test-jti";

    private DeviceTokenExtractor extractor;
    private MockedStatic<IdentityTenantUtil> mockedIdentityTenantUtil;
    private MockedStatic<DeviceTokenReplayService> mockedReplayServiceStatic;
    private MockedStatic<LoggerUtils> mockedLoggerUtils;
    private DeviceTokenReplayService replayServiceMock;
    private DeviceManagementService deviceManagementServiceMock;

    private KeyPair keyPair;
    private String base64PublicKey;
    private String validToken;

    @BeforeMethod
    public void setUp() throws Exception {

        mockedLoggerUtils = mockStatic(LoggerUtils.class);
        mockedLoggerUtils.when(LoggerUtils::isDiagnosticLogsEnabled).thenReturn(false);

        mockedIdentityTenantUtil = mockStatic(IdentityTenantUtil.class);
        mockedIdentityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(anyString())).thenReturn(TENANT_ID);

        replayServiceMock = mock(DeviceTokenReplayService.class);
        mockedReplayServiceStatic = mockStatic(DeviceTokenReplayService.class);
        mockedReplayServiceStatic.when(DeviceTokenReplayService::getInstance).thenReturn(replayServiceMock);

        deviceManagementServiceMock = mock(DeviceManagementService.class);
        DevicePolicyComponentServiceHolder.getInstance().setDeviceManagementService(deviceManagementServiceMock);

        // The extractor resolves the replay service singleton in a field initializer, so it must be
        // constructed only after the static mock above is in place.
        extractor = new DeviceTokenExtractor();

        keyPair = generateKeyPair();
        base64PublicKey = encodePublicKey(keyPair);
        validToken = buildToken(keyPair, DEVICE_ID, JTI, new Date());
    }

    @AfterMethod
    public void tearDown() {

        DevicePolicyComponentServiceHolder.getInstance().setDeviceManagementService(null);
        mockedReplayServiceStatic.close();
        mockedIdentityTenantUtil.close();
        mockedLoggerUtils.close();
    }

    // ------------------------------------------------------------------ extractFromToken

    @Test
    public void testExtractFromToken() throws Exception {

        registerDevice(DEVICE_ID, Device.Status.ACTIVE, base64PublicKey);

        Map<String, Object> result = extractor.extractFromToken(validToken, TENANT_DOMAIN);

        assertNotNull(result);
        assertEquals(result.get("platform"), "android");
        assertEquals(result.get("jti"), JTI);
    }

    @Test
    public void testExtractFromTokenStringifiesNonStringClaims() throws Exception {

        registerDevice(DEVICE_ID, Device.Status.ACTIVE, base64PublicKey);
        String token = buildTokenWithClaims(keyPair, DEVICE_ID, new JWTClaimsSet.Builder()
                .jwtID(JTI)
                .issueTime(new Date())
                .claim("osVersion", 15)
                .claim("isRooted", false));

        Map<String, Object> result = extractor.extractFromToken(token, TENANT_DOMAIN);

        assertEquals(result.get("osVersion"), "15");
        assertEquals(result.get("isRooted"), "false");
    }

    @Test
    public void testExtractFromTokenOmitsNullClaims() throws Exception {

        registerDevice(DEVICE_ID, Device.Status.ACTIVE, base64PublicKey);
        String token = buildTokenWithClaims(keyPair, DEVICE_ID, new JWTClaimsSet.Builder()
                .jwtID(JTI)
                .issueTime(new Date())
                .claim("platform", "android")
                .claim("nullClaim", null));

        Map<String, Object> result = extractor.extractFromToken(token, TENANT_DOMAIN);

        assertTrue(result.containsKey("platform"));
        assertFalse(result.containsKey("nullClaim"), "Null claims must not be copied into the device data.");
    }

    @Test
    public void testExtractFromTokenWithUnsignedJwt() {

        String plainJwt = new PlainJWT(new JWTClaimsSet.Builder().jwtID(JTI).build()).serialize();

        assertClientError(() -> extractor.extractFromToken(plainJwt, TENANT_DOMAIN),
                DevicePolicyErrorMessage.ERROR_DEVICE_TOKEN_PARSE_FAILED);
    }

    @Test
    public void testExtractFromTokenWithMalformedToken() {

        assertClientError(() -> extractor.extractFromToken("this-is-not-a-jwt", TENANT_DOMAIN),
                DevicePolicyErrorMessage.ERROR_DEVICE_TOKEN_PARSE_FAILED);
    }

    @Test
    public void testExtractFromTokenWithMissingDeviceId() throws Exception {

        String token = buildToken(keyPair, null, JTI, new Date());

        assertClientError(() -> extractor.extractFromToken(token, TENANT_DOMAIN),
                DevicePolicyErrorMessage.ERROR_DEVICE_TOKEN_MISSING_DEVICE_ID);
    }

    @Test
    public void testExtractFromTokenWithBlankDeviceId() throws Exception {

        String token = buildToken(keyPair, "   ", JTI, new Date());

        assertClientError(() -> extractor.extractFromToken(token, TENANT_DOMAIN),
                DevicePolicyErrorMessage.ERROR_DEVICE_TOKEN_MISSING_DEVICE_ID);
    }

    @Test
    public void testExtractFromTokenSanitisesDeviceId() throws Exception {

        registerDevice(DEVICE_ID, Device.Status.ACTIVE, base64PublicKey);
        String token = buildToken(keyPair, "\"\\" + DEVICE_ID + "\\\"", JTI, new Date());

        extractor.extractFromToken(token, TENANT_DOMAIN);

        ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
        verify(deviceManagementServiceMock).getDeviceById(idCaptor.capture(), eq(TENANT_DOMAIN));
        assertEquals(idCaptor.getValue(), DEVICE_ID, "Stray quotes and backslashes must be stripped.");
    }

    @Test
    public void testExtractFromTokenWhenDeviceNotFound() throws Exception {

        when(deviceManagementServiceMock.getDeviceById(DEVICE_ID, TENANT_DOMAIN)).thenReturn(null);

        PolicyManagementException e = assertClientError(() -> extractor.extractFromToken(validToken, TENANT_DOMAIN),
                DevicePolicyErrorMessage.ERROR_DEVICE_NOT_ACTIVE);
        assertTrue(e.getDescription().contains(DEVICE_ID));
    }

    @Test
    public void testExtractFromTokenWhenDeviceInactive() throws Exception {

        registerDevice(DEVICE_ID, Device.Status.INACTIVE, base64PublicKey);

        assertClientError(() -> extractor.extractFromToken(validToken, TENANT_DOMAIN),
                DevicePolicyErrorMessage.ERROR_DEVICE_NOT_ACTIVE);
    }

    @Test
    public void testExtractFromTokenWhenDeviceLookupFails() throws Exception {

        when(deviceManagementServiceMock.getDeviceById(DEVICE_ID, TENANT_DOMAIN))
                .thenThrow(new DeviceMgtException("lookup failed", "desc", "DM-65000"));

        assertServerError(() -> extractor.extractFromToken(validToken, TENANT_DOMAIN),
                DevicePolicyErrorMessage.ERROR_DEVICE_LOOKUP_FAILED);
    }

    @Test
    public void testExtractFromTokenWithUndecodablePublicKey() throws Exception {

        registerDevice(DEVICE_ID, Device.Status.ACTIVE, "not-a-valid-key");

        assertServerError(() -> extractor.extractFromToken(validToken, TENANT_DOMAIN),
                DevicePolicyErrorMessage.ERROR_DEVICE_PUBLIC_KEY_DECODE_FAILED);
    }

    @Test
    public void testExtractFromTokenWithInvalidSignature() throws Exception {

        registerDevice(DEVICE_ID, Device.Status.ACTIVE, encodePublicKey(generateKeyPair()));

        assertClientError(() -> extractor.extractFromToken(validToken, TENANT_DOMAIN),
                DevicePolicyErrorMessage.ERROR_DEVICE_TOKEN_SIGNATURE_INVALID);
    }

    // ------------------------------------------------------------------ jti / iat enforcement

    @Test
    public void testExtractFromTokenWithMissingJti() throws Exception {

        registerDevice(DEVICE_ID, Device.Status.ACTIVE, base64PublicKey);
        String token = buildToken(keyPair, DEVICE_ID, null, new Date());

        assertClientError(() -> extractor.extractFromToken(token, TENANT_DOMAIN),
                DevicePolicyErrorMessage.ERROR_DEVICE_TOKEN_MISSING_JTI);
    }

    @Test
    public void testExtractFromTokenWithBlankJti() throws Exception {

        registerDevice(DEVICE_ID, Device.Status.ACTIVE, base64PublicKey);
        String token = buildToken(keyPair, DEVICE_ID, "  ", new Date());

        assertClientError(() -> extractor.extractFromToken(token, TENANT_DOMAIN),
                DevicePolicyErrorMessage.ERROR_DEVICE_TOKEN_MISSING_JTI);
    }

    @Test
    public void testExtractFromTokenWithMissingIat() throws Exception {

        registerDevice(DEVICE_ID, Device.Status.ACTIVE, base64PublicKey);
        String token = buildToken(keyPair, DEVICE_ID, JTI, null);

        assertClientError(() -> extractor.extractFromToken(token, TENANT_DOMAIN),
                DevicePolicyErrorMessage.ERROR_DEVICE_TOKEN_MISSING_IAT);
    }

    @Test
    public void testExtractFromTokenWithStaleIat() throws Exception {

        registerDevice(DEVICE_ID, Device.Status.ACTIVE, base64PublicKey);
        Date stale = new Date(System.currentTimeMillis()
                - DeviceTokenConstants.TOKEN_FRESHNESS_WINDOW_MILLIS - 60_000L);
        String token = buildToken(keyPair, DEVICE_ID, JTI, stale);

        PolicyManagementException e = assertClientError(() -> extractor.extractFromToken(token, TENANT_DOMAIN),
                DevicePolicyErrorMessage.ERROR_DEVICE_TOKEN_EXPIRED);
        assertTrue(e.getDescription().contains(
                String.valueOf(DeviceTokenConstants.TOKEN_FRESHNESS_WINDOW_MILLIS / 1000)));
    }

    @Test
    public void testExtractFromTokenWithIatBeyondClockSkew() throws Exception {

        registerDevice(DEVICE_ID, Device.Status.ACTIVE, base64PublicKey);
        Date future = new Date(System.currentTimeMillis() + DeviceTokenConstants.CLOCK_SKEW_MILLIS + 60_000L);
        String token = buildToken(keyPair, DEVICE_ID, JTI, future);

        assertClientError(() -> extractor.extractFromToken(token, TENANT_DOMAIN),
                DevicePolicyErrorMessage.ERROR_DEVICE_TOKEN_EXPIRED);
    }

    @Test
    public void testExtractFromTokenWithIatWithinClockSkew() throws Exception {

        registerDevice(DEVICE_ID, Device.Status.ACTIVE, base64PublicKey);
        Date slightlyAhead = new Date(System.currentTimeMillis() + (DeviceTokenConstants.CLOCK_SKEW_MILLIS / 2));
        String token = buildToken(keyPair, DEVICE_ID, JTI, slightlyAhead);

        assertNotNull(extractor.extractFromToken(token, TENANT_DOMAIN),
                "A token issued within the allowed clock skew must be accepted.");
    }

    @Test
    public void testExtractFromTokenAtFreshnessBoundary() throws Exception {

        registerDevice(DEVICE_ID, Device.Status.ACTIVE, base64PublicKey);
        // Just inside the window: the check rejects only when age is strictly greater than the window.
        Date boundary = new Date(System.currentTimeMillis()
                - DeviceTokenConstants.TOKEN_FRESHNESS_WINDOW_MILLIS + 2_000L);
        String token = buildToken(keyPair, DEVICE_ID, JTI, boundary);

        assertNotNull(extractor.extractFromToken(token, TENANT_DOMAIN));
    }

    @Test
    public void testExtractFromTokenRecordsJtiForReplayProtection() throws Exception {

        registerDevice(DEVICE_ID, Device.Status.ACTIVE, base64PublicKey);
        Date issuedAt = new Date();
        String token = buildToken(keyPair, DEVICE_ID, JTI, issuedAt);

        extractor.extractFromToken(token, TENANT_DOMAIN);

        ArgumentCaptor<String> jtiCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Date> iatCaptor = ArgumentCaptor.forClass(Date.class);
        verify(replayServiceMock).assertUnusedAndRecord(jtiCaptor.capture(), iatCaptor.capture(),
                eq(TENANT_ID), anyString());
        assertEquals(jtiCaptor.getValue(), JTI);
        // JWT iat has second precision, so compare at that granularity.
        assertEquals(iatCaptor.getValue().getTime() / 1000, issuedAt.getTime() / 1000);
    }

    @Test
    public void testExtractFromTokenWhenJtiIsReplayed() throws Exception {

        registerDevice(DEVICE_ID, Device.Status.ACTIVE, base64PublicKey);
        doThrow(new PolicyManagementClientException("Device token replayed.", "already used",
                DevicePolicyErrorMessage.ERROR_DEVICE_TOKEN_REPLAYED.getCode()))
                .when(replayServiceMock).assertUnusedAndRecord(anyString(), any(Date.class), anyInt(), anyString());

        assertClientError(() -> extractor.extractFromToken(validToken, TENANT_DOMAIN),
                DevicePolicyErrorMessage.ERROR_DEVICE_TOKEN_REPLAYED);
    }

    // ------------------------------------------------------------------ extractWithPublicKey

    @Test
    public void testExtractWithPublicKey() throws Exception {

        Map<String, Object> result =
                extractor.extractWithPublicKey(validToken, base64PublicKey, CORRELATION_ID, TENANT_DOMAIN);

        assertNotNull(result);
        assertEquals(result.get("platform"), "android");
    }

    @Test
    public void testExtractWithPublicKeySkipsDeviceLookup() throws Exception {

        extractor.extractWithPublicKey(validToken, base64PublicKey, CORRELATION_ID, TENANT_DOMAIN);

        verify(deviceManagementServiceMock, never()).getDeviceById(anyString(), anyString());
    }

    @Test
    public void testExtractWithPublicKeyWithMalformedToken() {

        assertClientError(() -> extractor.extractWithPublicKey("not-a-jwt", base64PublicKey,
                        CORRELATION_ID, TENANT_DOMAIN),
                DevicePolicyErrorMessage.ERROR_DEVICE_TOKEN_PARSE_FAILED);
    }

    @Test
    public void testExtractWithPublicKeyWithUndecodableKey() {

        assertServerError(() -> extractor.extractWithPublicKey(validToken, "not-a-valid-key",
                        CORRELATION_ID, TENANT_DOMAIN),
                DevicePolicyErrorMessage.ERROR_DEVICE_PUBLIC_KEY_DECODE_FAILED);
    }

    @Test
    public void testExtractWithPublicKeyWithInvalidSignature() throws Exception {

        String otherKey = encodePublicKey(generateKeyPair());

        PolicyManagementException e = assertClientError(() -> extractor.extractWithPublicKey(validToken, otherKey,
                        CORRELATION_ID, TENANT_DOMAIN),
                DevicePolicyErrorMessage.ERROR_DEVICE_TOKEN_SIGNATURE_INVALID);
        assertTrue(e.getDescription().contains(CORRELATION_ID),
                "The correlation id must be surfaced in the error description.");
    }

    @Test
    public void testExtractWithPublicKeyWithMissingJti() throws Exception {

        String token = buildToken(keyPair, DEVICE_ID, null, new Date());

        assertClientError(() -> extractor.extractWithPublicKey(token, base64PublicKey,
                        CORRELATION_ID, TENANT_DOMAIN),
                DevicePolicyErrorMessage.ERROR_DEVICE_TOKEN_MISSING_JTI);
    }

    @Test
    public void testExtractWithPublicKeyWithStaleIat() throws Exception {

        Date stale = new Date(System.currentTimeMillis()
                - DeviceTokenConstants.TOKEN_FRESHNESS_WINDOW_MILLIS - 60_000L);
        String token = buildToken(keyPair, DEVICE_ID, JTI, stale);

        assertClientError(() -> extractor.extractWithPublicKey(token, base64PublicKey,
                        CORRELATION_ID, TENANT_DOMAIN),
                DevicePolicyErrorMessage.ERROR_DEVICE_TOKEN_EXPIRED);
    }

    // ------------------------------------------------------------------ helpers

    private interface ThrowingCall {

        void call() throws PolicyManagementException;
    }

    private PolicyManagementException assertClientError(ThrowingCall call, DevicePolicyErrorMessage expected) {

        try {
            call.call();
            fail("Expected PolicyManagementClientException with code: " + expected.getCode());
            return null;
        } catch (PolicyManagementException e) {
            assertTrue(e instanceof PolicyManagementClientException,
                    "Expected a client exception but got: " + e.getClass().getSimpleName());
            assertEquals(e.getErrorCode(), expected.getCode());
            return e;
        }
    }

    private PolicyManagementException assertServerError(ThrowingCall call, DevicePolicyErrorMessage expected) {

        try {
            call.call();
            fail("Expected PolicyManagementServerException with code: " + expected.getCode());
            return null;
        } catch (PolicyManagementException e) {
            assertTrue(e instanceof PolicyManagementServerException,
                    "Expected a server exception but got: " + e.getClass().getSimpleName());
            assertEquals(e.getErrorCode(), expected.getCode());
            return e;
        }
    }

    private void registerDevice(String deviceId, Device.Status status, String publicKey) throws DeviceMgtException {

        Device device = mock(Device.class);
        when(device.getStatus()).thenReturn(status);
        when(device.getPublicKey()).thenReturn(publicKey);
        when(deviceManagementServiceMock.getDeviceById(deviceId, TENANT_DOMAIN)).thenReturn(device);
    }

    private KeyPair generateKeyPair() throws Exception {

        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(new ECGenParameterSpec("secp256r1"));
        return generator.generateKeyPair();
    }

    private String encodePublicKey(KeyPair pair) {

        return Base64.getEncoder().encodeToString(((ECPublicKey) pair.getPublic()).getEncoded());
    }

    private String buildToken(KeyPair pair, String deviceId, String jti, Date issuedAt) throws Exception {

        JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder().claim("platform", "android");
        if (jti != null) {
            claims.jwtID(jti);
        }
        if (issuedAt != null) {
            claims.issueTime(issuedAt);
        }
        return buildTokenWithClaims(pair, deviceId, claims);
    }

    private String buildTokenWithClaims(KeyPair pair, String deviceId, JWTClaimsSet.Builder claims) throws Exception {

        JWSHeader.Builder header = new JWSHeader.Builder(JWSAlgorithm.ES256);
        if (deviceId != null) {
            header.customParam("deviceId", deviceId);
        }
        SignedJWT signedJWT = new SignedJWT(header.build(), claims.build());
        JWSSigner signer = new ECDSASigner((ECPrivateKey) pair.getPrivate());
        signedJWT.sign(signer);
        return signedJWT.serialize();
    }
}
