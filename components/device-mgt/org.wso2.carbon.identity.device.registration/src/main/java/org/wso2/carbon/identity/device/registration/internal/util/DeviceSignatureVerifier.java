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

import org.wso2.carbon.identity.device.registration.internal.constant.ErrorMessage;
import org.wso2.carbon.identity.device.registration.internal.exception.DeviceRegistrationException;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Verifies the ECDSA challenge-response signature produced by a device during registration.
 */
public class DeviceSignatureVerifier {

    private DeviceSignatureVerifier() {

    }

    /**
     * Verifies that the given signature is a valid ECDSA signature over the challenge, produced by
     * the private key corresponding to the given public key.
     *
     * @param registrationId   Registration ID, used only for error correlation.
     * @param challenge        Base64url encoded challenge that was signed.
     * @param publicKeyBase64  Base64 encoded EC public key (X.509/SubjectPublicKeyInfo DER).
     * @param signatureBase64  Base64 encoded ECDSA signature over the challenge bytes.
     * @throws DeviceRegistrationException If the signature is invalid (client error) or
     *                                      verification fails for an unexpected reason (server error).
     */
    public static void verify(String registrationId, String challenge,
                               String publicKeyBase64, String signatureBase64) throws DeviceRegistrationException {

        try {
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));

            byte[] challengeBytes = Base64.getUrlDecoder().decode(challenge);
            byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);

            Signature sig = Signature.getInstance("SHA256withECDSA");
            sig.initVerify(publicKey);
            sig.update(challengeBytes);
            boolean valid = sig.verify(signatureBytes);

            if (!valid) {
                throw DeviceRegistrationExceptionHandler.handleClientException(
                        ErrorMessage.ERROR_INVALID_DEVICE_SIGNATURE, registrationId);
            }
        } catch (IllegalArgumentException | SignatureException e) {
            // Malformed base64 input or an unparsable/incorrectly-encoded signature is a client error.
            throw DeviceRegistrationExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_INVALID_DEVICE_SIGNATURE, e, registrationId);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException e) {
            throw DeviceRegistrationExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_WHILE_VERIFYING_SIGNATURE, e, registrationId);
        }
    }
}
