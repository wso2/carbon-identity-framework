/*
* Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.identity.device.registration.internal.handler;

import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.device.mgt.api.constant.ErrorMessage;
import org.wso2.carbon.identity.device.mgt.api.exception.DeviceMgtException;
import org.wso2.carbon.identity.device.mgt.api.model.Device;
import org.wso2.carbon.identity.device.registration.internal.model.DeviceRegistrationChallenge;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.util.Base64;
import java.util.UUID;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DeviceRegistrationHandler}: challenge generation and signature
 * verification for the device registration protocol.
 */
@WithCarbonHome
public class DeviceRegistrationHandlerTest {

    private static final String TEST_USERNAME = "alice";
    private static final String TENANT_DOMAIN = "test.com";
    private static final int TENANT_ID = 1;

    private MockedStatic<IdentityTenantUtil> identityTenantUtilMocked;

    @BeforeClass
    public void setUpClass() {

        identityTenantUtilMocked = mockStatic(IdentityTenantUtil.class);
        when(IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);
    }

    @AfterClass
    public void tearDownClass() {

        identityTenantUtilMocked.close();
    }

    @Test
    public void testInitiateReturnsRegistrationIdAndChallenge() throws DeviceMgtException {

        DeviceRegistrationChallenge result = DeviceRegistrationHandler.initiate(TEST_USERNAME, TENANT_DOMAIN);

        Assert.assertNotNull(result);
        Assert.assertNotNull(result.getRegistrationId());
        Assert.assertFalse(result.getRegistrationId().isBlank());
        Assert.assertNotNull(result.getChallenge());
        Assert.assertFalse(result.getChallenge().isBlank());
    }

    @Test
    public void testInitiateGeneratesUniqueChallenges() throws DeviceMgtException {

        DeviceRegistrationChallenge r1 = DeviceRegistrationHandler.initiate(TEST_USERNAME, TENANT_DOMAIN);
        DeviceRegistrationChallenge r2 = DeviceRegistrationHandler.initiate(TEST_USERNAME, TENANT_DOMAIN);

        Assert.assertNotEquals(r1.getChallenge(), r2.getChallenge());
    }

    @Test
    public void testInitiateWithBlankUsernameThrows() {

        try {
            DeviceRegistrationHandler.initiate("  ", TENANT_DOMAIN);
            Assert.fail("Expected DeviceMgtClientException");
        } catch (DeviceMgtException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorMessage.ERROR_INVALID_DEVICE_FIELD.getCode());
        }
    }

    @Test
    public void testInitiateWithBlankTenantDomainThrows() {

        try {
            DeviceRegistrationHandler.initiate(TEST_USERNAME, "");
            Assert.fail("Expected DeviceMgtClientException");
        } catch (DeviceMgtException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorMessage.ERROR_INVALID_DEVICE_FIELD.getCode());
        }
    }

    @Test
    public void testVerifyWithValidSignatureSucceeds() throws Exception {

        KeyPair kp = generateEcKeyPair();
        DeviceRegistrationChallenge initiation = DeviceRegistrationHandler.initiate(TEST_USERNAME, TENANT_DOMAIN);
        String sig = signChallengeB64(kp, initiation.getChallenge());

        Device result = DeviceRegistrationHandler.verify(
                initiation.getRegistrationId(), publicKeyB64(kp), sig,
                "Alice's iPhone", null, null, TENANT_DOMAIN);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.getPublicKey(), publicKeyB64(kp));
        // userId is deliberately left unset by verify(); the caller must bind the real,
        // provisioned userId (from UserProvisioningExecutor) before persisting.
        Assert.assertNull(result.getUserId());
    }

    @Test
    public void testVerifyWithTamperedSignatureThrows() throws Exception {

        KeyPair kp = generateEcKeyPair();
        DeviceRegistrationChallenge initiation = DeviceRegistrationHandler.initiate(TEST_USERNAME, TENANT_DOMAIN);
        String sig = signChallengeB64(kp, initiation.getChallenge());
        String tamperedSig = tamper(sig);

        try {
            DeviceRegistrationHandler.verify(
                    initiation.getRegistrationId(), publicKeyB64(kp), tamperedSig,
                    "Alice's iPhone", null, null, TENANT_DOMAIN);
            Assert.fail("Expected DeviceMgtClientException");
        } catch (DeviceMgtException ex) {
            Assert.assertEquals(ex.getErrorCode(), "DR-60002");
        }
    }

    @Test
    public void testVerifyWithWrongKeyThrows() throws Exception {

        KeyPair kp1 = generateEcKeyPair();
        KeyPair kp2 = generateEcKeyPair();
        DeviceRegistrationChallenge initiation = DeviceRegistrationHandler.initiate(TEST_USERNAME, TENANT_DOMAIN);
        String sigFromWrongKey = signChallengeB64(kp2, initiation.getChallenge());

        try {
            DeviceRegistrationHandler.verify(
                    initiation.getRegistrationId(), publicKeyB64(kp1), sigFromWrongKey,
                    "Alice's iPhone", null, null, TENANT_DOMAIN);
            Assert.fail("Expected DeviceMgtClientException");
        } catch (DeviceMgtException ex) {
            Assert.assertEquals(ex.getErrorCode(), "DR-60002");
        }
    }

    @Test
    public void testVerifyWithMalformedBase64PublicKeyThrows() throws Exception {

        DeviceRegistrationChallenge initiation = DeviceRegistrationHandler.initiate(TEST_USERNAME, TENANT_DOMAIN);

        try {
            DeviceRegistrationHandler.verify(
                    initiation.getRegistrationId(), "not-valid-base64!!", "also-not-valid-base64!!",
                    "Device", null, null, TENANT_DOMAIN);
            Assert.fail("Expected DeviceMgtClientException for malformed base64 input");
        } catch (DeviceMgtException ex) {
            Assert.assertEquals(ex.getErrorCode(), "DR-60002");
        }
    }

    @Test
    public void testVerifyWithStructurallyInvalidKeyThrows() throws Exception {

        DeviceRegistrationChallenge initiation = DeviceRegistrationHandler.initiate(TEST_USERNAME, TENANT_DOMAIN);
        // Valid base64, but not a well-formed X.509 EC public key.
        String badKey = Base64.getEncoder().encodeToString(new byte[]{1, 2, 3, 4, 5});
        String fakeSig = Base64.getEncoder().encodeToString(new byte[]{0, 0, 0, 0});

        try {
            DeviceRegistrationHandler.verify(
                    initiation.getRegistrationId(), badKey, fakeSig,
                    "Device", null, null, TENANT_DOMAIN);
            Assert.fail("Expected DeviceMgtServerException");
        } catch (DeviceMgtException ex) {
            Assert.assertEquals(ex.getErrorCode(), "DR-65001");
        }
    }

    @Test
    public void testVerifyWithUnknownRegistrationIdThrows() throws Exception {

        KeyPair kp = generateEcKeyPair();

        try {
            DeviceRegistrationHandler.verify(
                    UUID.randomUUID().toString(), publicKeyB64(kp), "fakeSig",
                    "Device", null, null, TENANT_DOMAIN);
            Assert.fail("Expected DeviceMgtClientException");
        } catch (DeviceMgtException ex) {
            Assert.assertEquals(ex.getErrorCode(), "DR-60001");
        }
    }

    @Test
    public void testVerifyConsumesContext() throws Exception {

        KeyPair kp = generateEcKeyPair();
        DeviceRegistrationChallenge initiation = DeviceRegistrationHandler.initiate(TEST_USERNAME, TENANT_DOMAIN);
        String sig = signChallengeB64(kp, initiation.getChallenge());

        DeviceRegistrationHandler.verify(
                initiation.getRegistrationId(), publicKeyB64(kp), sig,
                "Device", null, null, TENANT_DOMAIN);

        // A second verify with the same (now expired/consumed) registrationId must fail.
        try {
            DeviceRegistrationHandler.verify(
                    initiation.getRegistrationId(), publicKeyB64(kp), sig,
                    "Device", null, null, TENANT_DOMAIN);
            Assert.fail("Expected context-not-found after context was consumed");
        } catch (DeviceMgtException ex) {
            Assert.assertEquals(ex.getErrorCode(), "DR-60001");
        }
    }

    @Test
    public void testVerifyWithBlankRequiredFieldThrows() {

        try {
            DeviceRegistrationHandler.verify(
                    UUID.randomUUID().toString(), "", "sig",
                    "Device", null, null, TENANT_DOMAIN);
            Assert.fail("Expected DeviceMgtClientException for blank publicKey");
        } catch (DeviceMgtException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorMessage.ERROR_INVALID_DEVICE_FIELD.getCode());
        }

        try {
            DeviceRegistrationHandler.verify(
                    UUID.randomUUID().toString(), "pk", "  ",
                    "Device", null, null, TENANT_DOMAIN);
            Assert.fail("Expected DeviceMgtClientException for blank signature");
        } catch (DeviceMgtException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorMessage.ERROR_INVALID_DEVICE_FIELD.getCode());
        }

        try {
            DeviceRegistrationHandler.verify(
                    UUID.randomUUID().toString(), "pk", "sig",
                    "", null, null, TENANT_DOMAIN);
            Assert.fail("Expected DeviceMgtClientException for blank deviceName");
        } catch (DeviceMgtException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorMessage.ERROR_INVALID_DEVICE_FIELD.getCode());
        }
    }

    private static KeyPair generateEcKeyPair() throws Exception {

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(256);
        return kpg.generateKeyPair();
    }

    private static String publicKeyB64(KeyPair kp) {

        return Base64.getEncoder().encodeToString(kp.getPublic().getEncoded());
    }

    private static String signChallengeB64(KeyPair kp, String challengeB64Url) throws Exception {

        Signature sig = Signature.getInstance("SHA256withECDSA");
        sig.initSign(kp.getPrivate());
        sig.update(Base64.getUrlDecoder().decode(challengeB64Url));
        return Base64.getEncoder().encodeToString(sig.sign());
    }

    private static String tamper(String signatureB64) {

        byte[] bytes = Base64.getDecoder().decode(signatureB64);
        bytes[bytes.length - 1] ^= 0xFF;
        return Base64.getEncoder().encodeToString(bytes);
    }
}
