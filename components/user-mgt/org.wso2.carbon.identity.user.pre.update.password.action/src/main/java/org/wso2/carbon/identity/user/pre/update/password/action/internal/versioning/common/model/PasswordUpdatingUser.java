/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.user.pre.update.password.action.internal.versioning.common.model;

import com.google.gson.Gson;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEEncrypter;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSAEncrypter;
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionRequestBuilderException;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionRequest;
import org.wso2.carbon.identity.action.execution.api.model.Organization;
import org.wso2.carbon.identity.action.execution.api.model.User;
import org.wso2.carbon.identity.action.execution.api.model.UserClaim;

import java.security.Key;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.List;

/**
 * This class models the user object at a pre update password trigger.
 * UserRequest is the entity that represents the user object that is sent to Action
 * over {@link ActionExecutionRequest}.
 */
public class PasswordUpdatingUser extends User {

    private final Object updatingCredential;

    private PasswordUpdatingUser(Builder builder) {

        super(new User.Builder(builder.id)
                .claims(builder.claims)
                .groups(builder.groups)
                .organization(builder.organization));
        this.updatingCredential = builder.updatingCredential;
    }

    public Object getUpdatingCredential() {

        return updatingCredential;
    }

    /**
     * Builder for TokenRequest.
     */
    public static class Builder {

        private String id;
        private final List<UserClaim> claims = new ArrayList<>();
        private final List<String> groups = new ArrayList<>();
        private Organization organization;
        private Object updatingCredential;
        private Credential unEncryptedCredential;
        private boolean isCredentialEncryptionRequired = false;
        private X509Certificate certificate;

        public Builder id(String id) {

            this.id = id;
            return this;
        }

        public Builder claims(List<? extends UserClaim> claims) {

            this.claims.addAll(claims);
            return this;
        }

        public Builder groups(List<String> groups) {

            this.groups.addAll(groups);
            return this;
        }

        public Builder organization(Organization organization) {

            this.organization = organization;
            return this;
        }

        public Builder updatingCredential(Credential unEncryptedCredential,
                                          boolean isCredentialEncryptionRequired, X509Certificate certificate) {

            this.unEncryptedCredential = unEncryptedCredential;
            this.isCredentialEncryptionRequired = isCredentialEncryptionRequired;
            this.certificate = certificate;
            return this;
        }

        public PasswordUpdatingUser build() throws ActionExecutionRequestBuilderException {

            if (!isCredentialEncryptionRequired) {
                this.updatingCredential = unEncryptedCredential;
                return new PasswordUpdatingUser(this);
            }

            if (certificate == null) {
                throw new IllegalStateException("Certificate is required for encryption.");
            }

            this.updatingCredential = encryptCredential(unEncryptedCredential, certificate);
            return new PasswordUpdatingUser(this);
        }

        private String encryptCredential(Credential unEncryptedCredential, X509Certificate certificate)
                throws ActionExecutionRequestBuilderException {

            try {
                // Extract the public key from the certificate
                Key publicKey = certificate.getPublicKey();

                // Serialize the object as a JSON string using Gson
                String credentialJson = new Gson().toJson(unEncryptedCredential);

                // Create a JWE header with encryption details
                JWEHeader header = new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256GCM)
                        .contentType("application/json") // Specify content type as JSON
                        .build();

                // Create a JWE object with the header and the raw JSON payload
                JWEObject jweObject = new JWEObject(header, new Payload(credentialJson));

                // Encrypt the JWE using the public key
                JWEEncrypter encrypter = new RSAEncrypter((RSAPublicKey) publicKey);
                jweObject.encrypt(encrypter);

                // Serialize the JWE to a compact form for transmission
                return jweObject.serialize();
            } catch (JOSEException e) {
                throw new ActionExecutionRequestBuilderException("Error occurred while encrypting the credential.", e);
            }
        }
    }
}
