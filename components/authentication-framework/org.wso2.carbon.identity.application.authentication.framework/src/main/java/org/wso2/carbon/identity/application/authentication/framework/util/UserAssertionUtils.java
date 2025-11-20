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

package org.wso2.carbon.identity.application.authentication.framework.util;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.inbound.FrameworkClientException;
import org.wso2.carbon.identity.core.ServiceURLBuilder;
import org.wso2.carbon.identity.core.URLBuilderException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.core.util.LambdaExceptionUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for handling auto-login assertions.
 */
public class UserAssertionUtils {

    private static final Log LOG = LogFactory.getLog(UserAssertionUtils.class);
    private static final Map<Integer, Key> PRIVATE_KEYS = new ConcurrentHashMap<>();
    private static final Map<Integer, Certificate> PUBLIC_CERTS = new ConcurrentHashMap<>();

    public static final String JWT_USER_ASSERTION_TYPE = "JWT+UA";

    /**
     * Generates a signed JWT user assertion with the provided claims set.
     *
     * @param claimsSet    The JWT claims set to be included in the assertion.
     * @param tenantDomain The tenant domain for which the assertion is generated.
     * @return A signed JWT user assertion as a string.
     * @throws FrameworkException If an error occurs while generating the signed user assertion.
     */
    public static String generateSignedUserAssertion(JWTClaimsSet claimsSet, String tenantDomain)
            throws FrameworkException {

        try {
            RSAPrivateKey privateKey = (RSAPrivateKey) getPrivateKey(tenantDomain);
            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256).type(new JOSEObjectType(JWT_USER_ASSERTION_TYPE)).build(),
                    claimsSet);
            signedJWT.sign(new RSASSASigner(privateKey));
            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new FrameworkException("Error occurred while signing JWT", e);
        }
    }

    /**
     * Retrieves the JWT claims set from a user assertion token.
     *
     * @param token        The signed JWT user assertion token.
     * @param tenantDomain The tenant domain for which the assertion is generated.
     * @return An Optional containing the JWT claims set if successfully retrieved, otherwise empty.
     * @throws FrameworkException If an error occurs while verifying the user assertion or retrieving claims.
     */
    public static Optional<JWTClaimsSet> retrieveClaimsFromUserAssertion(String token, String tenantDomain)
            throws FrameworkException {

        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            validateJWTType(signedJWT);
            verifyJWTSignature(signedJWT, tenantDomain);
            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
            validateUserAssertion(claimsSet);
            return Optional.of(claimsSet);
        } catch (ParseException e) {
            throw new FrameworkException("Error while verifying the user assertion.", e);
        }
    }

    /**
     * Validates that the JWT has the expected type header (jwt+UA).
     *
     * @param signedJWT The signed JWT to validate.
     * @throws FrameworkException If the JWT type is not jwt+UA or is missing.
     */
    private static void validateJWTType(SignedJWT signedJWT) throws FrameworkException {

        JOSEObjectType type = signedJWT.getHeader().getType();
        if (type == null) {
            throw new FrameworkException("JWT type header is missing. Expected type: " + JWT_USER_ASSERTION_TYPE);
        }
        if (!JWT_USER_ASSERTION_TYPE.equals(type.getType())) {
            throw new FrameworkClientException("Invalid JWT type. Expected: " + JWT_USER_ASSERTION_TYPE + ", " +
                    "Found: " + type);
        }
    }

    private static void validateUserAssertion(JWTClaimsSet claimsSet) throws FrameworkException {

        try {
            Date now = new Date();
            if (claimsSet.getExpirationTime() == null || claimsSet.getExpirationTime().before(now)) {
                throw new FrameworkClientException("User assertion is expired.");
            }
            String expectedIssuer = ServiceURLBuilder.create().build(IdentityUtil.getHostName()).getAbsolutePublicURL();
            if (!expectedIssuer.equals(claimsSet.getIssuer())) {
                throw new FrameworkClientException("Invalid issuer in the user assertion.");
            }

            if (claimsSet.getAudience() == null || !claimsSet.getAudience().contains(expectedIssuer)) {
                throw new FrameworkClientException("Invalid audience in the user assertion.");
            }
            if (claimsSet.getSubject() == null) {
                throw new FrameworkClientException("Subject is missing in the user assertion.");
            }
        } catch (URLBuilderException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error while building the expected issuer URL for user assertion.", e);
            }
            throw new FrameworkClientException("Error while building the expected issuer URL for user assertion.");
        }
    }

    private static void verifyJWTSignature(SignedJWT signedJWT, String tenantDomain) throws FrameworkException {

        try {
            String issuer = signedJWT.getJWTClaimsSet().getIssuer();
            if (StringUtils.isBlank(issuer)) {
                throw new FrameworkException("Issuer is missing in the user assertion.");
            }
            String serverURL = ServiceURLBuilder.create().build(IdentityUtil.getHostName()).getAbsolutePublicURL();
            if (!issuer.equals(serverURL)) {
                throw new FrameworkException("Invalid issuer in the user assertion.");
            }
            RSAPublicKey publicKey = (RSAPublicKey) getCertificate(tenantDomain).getPublicKey();
            if (!signedJWT.verify(new RSASSAVerifier(publicKey))) {
                throw new FrameworkException("Signature verification failed for the user assertion.");
            }
        } catch (JOSEException | ParseException | URLBuilderException e) {
            throw new FrameworkException("Error while verifying the user assertion signature.", e);
        }
    }

    private static Key getPrivateKey(String tenantDomain) throws FrameworkException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        try {
            return PRIVATE_KEYS.computeIfAbsent(tenantId, LambdaExceptionUtils.rethrowFunction(id -> {
                IdentityTenantUtil.initializeRegistry(id);
                KeyStoreManager ksm = KeyStoreManager.getInstance(id);
                if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                    return ksm.getDefaultPrivateKey();
                }
                String jksName = tenantDomain.trim().replace(".", "-") + ".jks";
                return ksm.getPrivateKey(jksName, tenantDomain);
            }));
        } catch (RuntimeException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new FrameworkException("Error obtaining private key for tenant " + tenantDomain, cause);
        }
    }

    private static Certificate getCertificate(String tenantDomain) throws FrameworkException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        try {
            return PUBLIC_CERTS.computeIfAbsent(tenantId, LambdaExceptionUtils.rethrowFunction(id -> {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("Obtaining certificate for tenant %s", tenantDomain));
                }
                IdentityTenantUtil.initializeRegistry(id);
                KeyStoreManager ksm = KeyStoreManager.getInstance(id);
                if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                    return ksm.getDefaultPrimaryCertificate();
                }
                String jksName = tenantDomain.trim().replace(".", "-") + ".jks";
                KeyStore keyStore = ksm.getKeyStore(jksName);
                return keyStore.getCertificate(tenantDomain);
            }));
        } catch (RuntimeException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new FrameworkException("Error obtaining certificate for tenant " + tenantDomain, cause);
        }
    }
}
