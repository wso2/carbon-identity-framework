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

package org.wso2.carbon.identity.device.registration.internal.util;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.device.registration.internal.constant.ErrorMessage;
import org.wso2.carbon.identity.device.registration.internal.exception.DeviceRegistrationException;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Signature;
import java.util.Base64;

/**
 * Unit tests for {@link DeviceSignatureVerifier}.
 */
public class DeviceSignatureVerifierTest {

    private static final String REGISTRATION_ID = "reg-id-001";

    @Test
    public void testValidSignatureVerifies() throws Exception {

        KeyPair kp = generateEcKeyPair();
        String challenge = randomChallenge();
        String signature = sign(kp, challenge);

        // Must not throw.
        DeviceSignatureVerifier.verify(REGISTRATION_ID, challenge, publicKeyB64(kp), signature);
    }

    @Test
    public void testTamperedSignatureRejectedAsClientError() throws Exception {

        KeyPair kp = generateEcKeyPair();
        String challenge = randomChallenge();
        String signature = tamper(sign(kp, challenge));

        try {
            DeviceSignatureVerifier.verify(REGISTRATION_ID, challenge, publicKeyB64(kp), signature);
            Assert.fail("Expected DeviceRegistrationException for a tampered signature");
        } catch (DeviceRegistrationException ex) {
            Assert.assertTrue(ex.isClientError());
            Assert.assertEquals(ex.getErrorCode(), ErrorMessage.ERROR_INVALID_DEVICE_SIGNATURE.getCode());
        }
    }

    @Test
    public void testSignatureFromDifferentKeyPairRejected() throws Exception {

        KeyPair signingKeyPair = generateEcKeyPair();
        KeyPair unrelatedKeyPair = generateEcKeyPair();
        String challenge = randomChallenge();
        String signature = sign(signingKeyPair, challenge);

        try {
            // Verifying against the wrong public key.
            DeviceSignatureVerifier.verify(REGISTRATION_ID, challenge, publicKeyB64(unrelatedKeyPair), signature);
            Assert.fail("Expected DeviceRegistrationException for a signature made with a different key pair");
        } catch (DeviceRegistrationException ex) {
            Assert.assertTrue(ex.isClientError());
            Assert.assertEquals(ex.getErrorCode(), ErrorMessage.ERROR_INVALID_DEVICE_SIGNATURE.getCode());
        }
    }

    @Test
    public void testMalformedBase64PublicKeyRejectedCleanly() throws Exception {

        KeyPair kp = generateEcKeyPair();
        String challenge = randomChallenge();
        String signature = sign(kp, challenge);

        try {
            DeviceSignatureVerifier.verify(REGISTRATION_ID, challenge, "not-valid-base64!!", signature);
            Assert.fail("Expected DeviceRegistrationException, not a raw IllegalArgumentException");
        } catch (DeviceRegistrationException ex) {
            Assert.assertTrue(ex.isClientError());
            Assert.assertEquals(ex.getErrorCode(), ErrorMessage.ERROR_INVALID_DEVICE_SIGNATURE.getCode());
        }
    }

    @Test
    public void testMalformedBase64SignatureRejectedCleanly() throws Exception {

        KeyPair kp = generateEcKeyPair();
        String challenge = randomChallenge();

        try {
            DeviceSignatureVerifier.verify(REGISTRATION_ID, challenge, publicKeyB64(kp), "also-not-valid-base64!!");
            Assert.fail("Expected DeviceRegistrationException, not a raw IllegalArgumentException");
        } catch (DeviceRegistrationException ex) {
            Assert.assertTrue(ex.isClientError());
            Assert.assertEquals(ex.getErrorCode(), ErrorMessage.ERROR_INVALID_DEVICE_SIGNATURE.getCode());
        }
    }

    @Test
    public void testStructurallyInvalidKeyRejected() throws Exception {

        String challenge = randomChallenge();
        // Valid base64, but not a well-formed X.509/SubjectPublicKeyInfo EC key.
        String garbageKey = Base64.getEncoder().encodeToString(new byte[]{1, 2, 3, 4, 5});
        String fakeSignature = Base64.getEncoder().encodeToString(new byte[]{0, 0, 0, 0});

        try {
            DeviceSignatureVerifier.verify(REGISTRATION_ID, challenge, garbageKey, fakeSignature);
            Assert.fail("Expected DeviceRegistrationException for a structurally invalid key");
        } catch (DeviceRegistrationException ex) {
            Assert.assertFalse(ex.isClientError());
            Assert.assertEquals(ex.getErrorCode(), ErrorMessage.ERROR_WHILE_VERIFYING_SIGNATURE.getCode());
        }
    }

    private static KeyPair generateEcKeyPair() throws Exception {

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(256);
        return kpg.generateKeyPair();
    }

    private static String randomChallenge() {

        byte[] challengeBytes = new byte[32];
        new SecureRandom().nextBytes(challengeBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(challengeBytes);
    }

    private static String publicKeyB64(KeyPair kp) {

        return Base64.getEncoder().encodeToString(kp.getPublic().getEncoded());
    }

    private static String sign(KeyPair kp, String challengeB64Url) throws Exception {

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
