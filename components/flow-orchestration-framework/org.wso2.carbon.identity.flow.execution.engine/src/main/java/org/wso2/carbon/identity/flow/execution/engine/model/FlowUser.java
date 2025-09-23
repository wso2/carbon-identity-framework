/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.flow.execution.engine.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.execution.engine.util.FlowExecutionEngineUtils;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.EMAIL_ADDRESS_CLAIM;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.USERNAME_CLAIM_URI;

/**
 * This class is responsible for holding the user profile of the current user in the flow.
 */
public class FlowUser implements Serializable {

    private static final long serialVersionUID = -1873658743998134877L;
    private static final Log LOG = LogFactory.getLog(FlowUser.class);

    private static final String MANAGED_ORG_CLAIM_URI = "http://wso2.org/claims/identity/managedOrg";
    private static final String LOCAL_CREDENTIAL_EXISTS_CLAIM_URI = "http://wso2.org/claims/identity/localCredentialExists";

    private final Map<String, String> claims = new HashMap<>();

    @JsonProperty("userCredentials")
    @JsonSerialize(using = UserCredentialsSerializer.class)
    @JsonDeserialize(using = UserCredentialsDeserializer.class)
    private final Map<String, char[]> userCredentials = new HashMap<>();

    private final Map<String, String> federatedAssociations = new HashMap<>();

    @JsonProperty("username")
    private String username;

    private String userId;
    private String userStoreDomain;

    @JsonIgnore
    public String getUsername() {

        if (StringUtils.isBlank(username)) {
            username = claims.get(USERNAME_CLAIM_URI);
        }
        if (StringUtils.isBlank(username)) {
            return resolveUsername(this, IdentityTenantUtil.getTenantDomainFromContext());
        }
        return username;
    }

    @JsonIgnore
    public void setUsername(String username) {

        this.username = username;
        this.claims.put(USERNAME_CLAIM_URI, username);
    }

    public Map<String, String> getClaims() {

        return claims;
    }

    public void addClaims(Map<String, String> claims) {

        this.claims.putAll(claims);
    }

    public Object getClaim(String claimUri) {

        return this.claims.get(claimUri);
    }

    public void addClaim(String claimUri, String claimValue) {

        this.claims.put(claimUri, claimValue);
    }

    public Map<String, char[]> getUserCredentials() {

        return userCredentials;
    }

    public void setUserCredentials(Map<String, char[]> credentials) {

        this.userCredentials.clear();
        this.userCredentials.putAll(credentials);
    }

    public String getUserId() {

        return userId;
    }

    public void setUserId(String userId) {

        this.userId = userId;
    }

    public String getUserStoreDomain() {

        return userStoreDomain;
    }

    public void setUserStoreDomain(String userStoreDomain) {

        this.userStoreDomain = userStoreDomain;
    }

    public Map<String, String> getFederatedAssociations() {

        return federatedAssociations;
    }

    public void addFederatedAssociation(String idpName, String idpSubject) {

        this.federatedAssociations.put(idpName, idpSubject);
    }

    /**
     * Check whether the user credentials are managed locally.
     *
     * @return true if credentials are managed locally, false otherwise.
     */
    @JsonIgnore
    public boolean isCredentialsManagedLocally() {

        // Credentials are NOT managed locally if any of these conditions are true.
        final String managedOrgId = claims.get(MANAGED_ORG_CLAIM_URI);
        final boolean isManagedByDifferentOrg = StringUtils.isNotBlank(managedOrgId);

        final String userSourceId = claims.get(FrameworkConstants.PROVISIONED_SOURCE_ID_CLAIM);
        final String localCredentialExistsStr = claims.get(LOCAL_CREDENTIAL_EXISTS_CLAIM_URI);
        // This case covers an external user source where local credentials are explicitly flagged as not existing.
        boolean isExternalUserWithoutLocalCreds = false;
        if (StringUtils.isNotEmpty(userSourceId)) {
            if (!Boolean.parseBoolean(localCredentialExistsStr)) {
                isExternalUserWithoutLocalCreds = true;
            }
        }

        final boolean isManagedExternally = isManagedByDifferentOrg || isExternalUserWithoutLocalCreds;
        return !isManagedExternally;
    }

    @JsonIgnore
    public boolean isAccountLocked() {

        String accountLocked = claims.get(FrameworkConstants.ACCOUNT_LOCKED_CLAIM_URI);
        return Boolean.parseBoolean(accountLocked);
    }

    @JsonIgnore
    public boolean isAccountDisabled() {

        String accountDisabled = claims.get(FrameworkConstants.ACCOUNT_DISABLED_CLAIM_URI);
        return Boolean.parseBoolean(accountDisabled);
    }

    private String resolveUsername(FlowUser user, String tenantDomain) {

        String username = Optional.ofNullable(user.getClaims().get(USERNAME_CLAIM_URI)).orElse("");
        if (StringUtils.isNotBlank(username)) {
            return username;
        }
        try {
            if ((FlowExecutionEngineUtils.isEmailUsernameValidator(tenantDomain) ||
                    IdentityUtil.isEmailUsernameEnabled())
                    && StringUtils.isNotBlank((String) user.getClaim(EMAIL_ADDRESS_CLAIM))) {
                return (String) user.getClaim(EMAIL_ADDRESS_CLAIM);
            }
        } catch (FlowEngineException e) {
            LOG.error("Error while resolving username for the user in the flow.", e);
        }
        username = UUID.randomUUID().toString();
        UserCoreUtil.setSkipUsernamePatternValidationThreadLocal(true);
        return username;
    }

    /**
     * Custom serializer for user credentials.
     * This is used to encrypt the user credentials before serializing to JSON.
     */
    public static class UserCredentialsSerializer extends JsonSerializer<Map<String, char[]>> {

        @Override
        public void serialize(Map<String, char[]> value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {

            Map<String, String> encrypted = new HashMap<>();
            for (Map.Entry<String, char[]> entry : value.entrySet()) {
                char[] chars = entry.getValue();
                byte[] bytes = null;
                try {
                    // Encode char[] directly to byte[] using UTF-8.
                    bytes = StandardCharsets.UTF_8.encode(CharBuffer.wrap(chars)).array();
                    String encryptedVal = CryptoUtil.getDefaultCryptoUtil().encryptAndBase64Encode(bytes);
                    encrypted.put(entry.getKey(), encryptedVal);
                } catch (CryptoException e) {
                    throw new IOException("Error while encrypting user credentials.", e);
                } finally {
                    // Wipe the byte array to prevent credential leakage.
                    if (bytes != null) {
                        java.util.Arrays.fill(bytes, (byte) 0);
                    }
                }
            }
            gen.writeObject(encrypted);
        }

    }

    /**
     * Custom deserializer for user credentials.
     * This is used to decrypt the user credentials from the JSON representation.
     */
    public static class UserCredentialsDeserializer extends JsonDeserializer<Map<String, char[]>> {

        @Override
        public Map<String, char[]> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

            Map<String, char[]> decrypted = new HashMap<>();
            try {
                Map<String, String> encrypted = p.readValueAs(new StringMapTypeReference());
                for (Map.Entry<String, String> entry : encrypted.entrySet()) {
                    byte[] decoded = null;
                    CharBuffer charBuffer = null;
                    try {
                        decoded = CryptoUtil.getDefaultCryptoUtil().base64DecodeAndDecrypt(entry.getValue());
                        // Decode byte[] directly to CharBuffer and then to char[].
                        charBuffer = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(decoded));
                        char[] chars = new char[charBuffer.remaining()];
                        charBuffer.get(chars);
                        decrypted.put(entry.getKey(), chars);
                    } catch (CryptoException e) {
                        throw new IOException("Error while decrypting user credentials.", e);
                    } finally {
                        // Wipe sensitive data.
                        if (decoded != null) {
                            java.util.Arrays.fill(decoded, (byte) 0);
                        }
                        if (charBuffer != null) {
                            charBuffer.clear();
                        }
                    }
                }
            } catch (IOException e) {
                throw e;
            }
            return decrypted;
        }
    }

    /**
     * Static type reference to avoid anonymous inner class.
     */
    private static final class StringMapTypeReference extends TypeReference<Map<String, String>> {

    }
}
