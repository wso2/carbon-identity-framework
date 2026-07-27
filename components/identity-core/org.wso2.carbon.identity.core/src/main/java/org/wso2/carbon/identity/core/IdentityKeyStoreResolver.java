/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.core;

import org.apache.axiom.om.OMElement;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.core.RegistryResources;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.core.util.KeyStoreUtil;
import org.wso2.carbon.identity.core.model.IdentityKeyStoreMapping;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.core.util.IdentityKeyStoreResolverConstants;
import org.wso2.carbon.identity.core.util.IdentityKeyStoreResolverConstants.InboundProtocol;
import org.wso2.carbon.identity.core.util.IdentityKeyStoreResolverException;
import org.wso2.carbon.identity.core.util.IdentityKeyStoreResolverUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.wso2.carbon.identity.core.util.IdentityKeyStoreResolverConstants.ErrorMessages;


/**
 * KeyStore manager for inbound authentication protocols.
 * Retrieve keystores, private keys, public keys and public certificates.
 */
public class IdentityKeyStoreResolver {

    private static IdentityKeyStoreResolver instance = null;

    private static ConcurrentHashMap<InboundProtocol, IdentityKeyStoreMapping>
            keyStoreMappings = new ConcurrentHashMap<>();

    // Hashmaps to store retrieved private keys and certificates.
    // This will reduce the time required to read configs and load data from keystores everytime.
    private static Map<String, Key> privateKeys = new ConcurrentHashMap<>();
    private static Map<String, Certificate> publicCerts = new ConcurrentHashMap<>();

    private static final Log LOG = LogFactory.getLog(IdentityKeyStoreResolver.class);

    private IdentityKeyStoreResolver() {

        parseIdentityKeyStoreMappingConfigs();
    }

    public static IdentityKeyStoreResolver getInstance() {

        if (instance == null) {
            instance = new IdentityKeyStoreResolver();
        }
        return instance;
    }

    /**
     * Return Primary or tenant keystore according to given tenant domain.
     *
     * @param tenantDomain  Tenant domain.
     * @return Primary or tenant keystore.
     * @throws IdentityKeyStoreResolverException the exception in the IdentityKeyStoreResolver class.
     */
    private KeyStore getKeyStore(String tenantDomain) throws IdentityKeyStoreResolverException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        KeyStoreManager keyStoreManager = KeyStoreManager.getInstance(tenantId);
        try {
            if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                // Get primary keystore from keyStoreManager
                return keyStoreManager.getPrimaryKeyStore();
            }

            initializeTenantRegistry(tenantDomain);
            // Get tenant keystore from keyStoreManager
            String tenantKeyStoreName = IdentityKeyStoreResolverUtil.buildTenantKeyStoreName(tenantDomain);
            return keyStoreManager.getKeyStore(tenantKeyStoreName);
        } catch (Exception e) {
            throw new IdentityKeyStoreResolverException(
                    ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_TENANT_KEYSTORE.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_TENANT_KEYSTORE.getDescription(),
                            tenantDomain), e);
        }
    }

    /**
     * Return Primary, tenant or custom keystore.
     *
     * @param tenantDomain      Tenant domain.
     * @param inboundProtocol   Inbound authentication protocol of the application.
     * @return Primary, tenant or custom keystore.
     * @throws IdentityKeyStoreResolverException the exception in the IdentityKeyStoreResolver class.
     */
    public KeyStore getKeyStore(String tenantDomain, InboundProtocol inboundProtocol)
            throws IdentityKeyStoreResolverException {

        if (StringUtils.isEmpty(tenantDomain)) {
            throw new IdentityKeyStoreResolverException(
                    ErrorMessages.ERROR_CODE_INVALID_ARGUMENT.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_INVALID_ARGUMENT.getDescription(), "Tenant domain"));
        }
        if (inboundProtocol == null) {
            throw new IdentityKeyStoreResolverException(
                    ErrorMessages.ERROR_CODE_INVALID_ARGUMENT.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_INVALID_ARGUMENT.getDescription(), "Inbound protocol"));
        }

        if (keyStoreMappings.containsKey(inboundProtocol)) {
            if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain) ||
                    keyStoreMappings.get(inboundProtocol).getUseInAllTenants()) {

                String keyStoreName = IdentityKeyStoreResolverUtil.buildCustomKeyStoreName(
                        keyStoreMappings.get(inboundProtocol).getKeyStoreName());

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Custom key store configuration available for " + inboundProtocol + " protocol. " +
                            "Retrieving keystore " + keyStoreName);
                }

                try {
                    int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
                    KeyStoreManager keyStoreManager = KeyStoreManager.getInstance(tenantId);
                    return keyStoreManager.getKeyStore(keyStoreName);
                } catch (Exception e) {
                    throw new IdentityKeyStoreResolverException(
                            ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_CUSTOM_KEYSTORE.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_CUSTOM_KEYSTORE.getDescription(),
                                    keyStoreName), e);
                }
            }
        }

        return getKeyStore(tenantDomain);
    }

    /**
     * Return Primary key of the Primary or tenant keystore according to given tenant domain.
     *
     * @param tenantDomain  Tenant domain.
     * @return Primary key of Primary or tenant keystore.
     * @throws IdentityKeyStoreResolverException the exception in the IdentityKeyStoreResolver class.
     */
    private Key getPrivateKey(String tenantDomain) throws IdentityKeyStoreResolverException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        if (privateKeys.containsKey(String.valueOf(tenantId))) {
            return privateKeys.get(String.valueOf(tenantId));
        }

        KeyStoreManager keyStoreManager = KeyStoreManager.getInstance(tenantId);
        Key privateKey;

        try {
            if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                privateKey = keyStoreManager.getDefaultPrivateKey();
            } else {
                initializeTenantRegistry(tenantDomain);
                String tenantKeyStoreName = IdentityKeyStoreResolverUtil.buildTenantKeyStoreName(tenantDomain);
                privateKey = keyStoreManager.getPrivateKey(tenantKeyStoreName, tenantDomain);
            }
        } catch (Exception e) {
            throw new IdentityKeyStoreResolverException(
                    ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_TENANT_PRIVATE_KEY.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_TENANT_PRIVATE_KEY.getDescription(),
                            tenantDomain), e);
        }

        privateKeys.put(String.valueOf(tenantId), privateKey);
        return privateKey;
    }

    /**
     * Return Private Key of the Primary, tenant or custom keystore.
     *
     * @param tenantDomain      Tenant domain.
     * @param inboundProtocol   Inbound authentication protocol of the application.
     * @return Private Key of the Primary, tenant or custom keystore.
     * @throws IdentityKeyStoreResolverException the exception in the IdentityKeyStoreResolver class.
     */
    public Key getPrivateKey(String tenantDomain, InboundProtocol inboundProtocol)
            throws IdentityKeyStoreResolverException {

        if (StringUtils.isEmpty(tenantDomain)) {
            throw new IdentityKeyStoreResolverException(
                    ErrorMessages.ERROR_CODE_INVALID_ARGUMENT.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_INVALID_ARGUMENT.getDescription(), "Tenant domain"));
        }
        if (inboundProtocol == null) {
            throw new IdentityKeyStoreResolverException(
                    ErrorMessages.ERROR_CODE_INVALID_ARGUMENT.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_INVALID_ARGUMENT.getDescription(), "Inbound protocol"));
        }

        if (keyStoreMappings.containsKey(inboundProtocol)) {
            if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain) ||
                    keyStoreMappings.get(inboundProtocol).getUseInAllTenants()) {

                String keyStoreName = IdentityKeyStoreResolverUtil.buildCustomKeyStoreName(
                        keyStoreMappings.get(inboundProtocol).getKeyStoreName());

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Custom key store configuration available for " + inboundProtocol + " protocol. " +
                            "Retrieving private key from " + keyStoreName + " key store.");
                }

                if (privateKeys.containsKey(inboundProtocol.toString())) {
                    return privateKeys.get(inboundProtocol.toString());
                }

                try {
                    int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
                    KeyStoreManager keyStoreManager = KeyStoreManager.getInstance(tenantId);
                    Key privateKey = keyStoreManager.getPrivateKey(keyStoreName, null);
                    privateKeys.put(inboundProtocol.toString(), privateKey);
                    return privateKey;
                } catch (Exception e) {
                    throw new IdentityKeyStoreResolverException(
                            ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_CUSTOM_PRIVATE_KEY.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_CUSTOM_PRIVATE_KEY.getDescription(),
                                    keyStoreName), e);
                }
            }
        }
        return getPrivateKey(tenantDomain);
    }

    /**
     * Retrieves the public certificate for a given tenant domain and context.
     * <p>
     * This method fetches the public certificate associated with a specific tenant domain and context.
     * If the context is blank, it delegates the call to the overloaded
     * {@code getCertificate(String tenantDomain)} method.
     * The method first checks if the certificate is cached; if not, it retrieves the certificate from
     * the KeyStoreManager, caches it, and then returns it.
     * </p>
     *
     * @param tenantDomain the tenant domain for which the certificate is requested.
     * @param context      the specific context for the tenant's certificate. If blank, the default certificate for the tenant is fetched.
     * @return the public certificate for the specified tenant domain and context.
     * @throws IdentityKeyStoreResolverException if there is an error while retrieving the certificate.
     */

    private Certificate getCertificate(String tenantDomain, String context) throws IdentityKeyStoreResolverException {

        if (StringUtils.isBlank(context)) {
            getCertificate(tenantDomain);
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        if (publicCerts.containsKey(buildTenantIdWithContext(tenantId, context))) {
            return publicCerts.get(buildTenantIdWithContext(tenantId, context));
        }

        initializeTenantRegistry(tenantDomain);
        KeyStoreManager keyStoreManager = KeyStoreManager.getInstance(tenantId);
        Certificate publicCert;
        String tenantKeyStoreName = IdentityKeyStoreResolverUtil.buildTenantKeyStoreName(tenantDomain, context);
        try {
            publicCert = keyStoreManager.getCertificate(tenantKeyStoreName, tenantDomain +
                    IdentityKeyStoreResolverConstants.KEY_STORE_CONTEXT_SEPARATOR + context);

        } catch (SecurityException e) {
            if (e.getMessage() != null && e.getMessage().contains("Key Store with a name: " + tenantKeyStoreName
                    + " does not exist.")) {

                throw new IdentityKeyStoreResolverException(
                        ErrorMessages.ERROR_RETRIEVING_TENANT_CONTEXT_PUBLIC_CERTIFICATE_KEYSTORE_NOT_EXIST.getCode(),
                        String.format(
                                ErrorMessages.ERROR_RETRIEVING_TENANT_CONTEXT_PUBLIC_CERTIFICATE_KEYSTORE_NOT_EXIST
                                        .getDescription(), tenantDomain), e);
            } else {
                throw new IdentityKeyStoreResolverException(
                        ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_TENANT_PUBLIC_CERTIFICATE.getCode(),
                        String.format(ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_TENANT_PUBLIC_CERTIFICATE.getDescription(),
                                tenantDomain), e);
            }
        } catch (Exception e) {
            throw new IdentityKeyStoreResolverException(
                    ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_TENANT_PUBLIC_CERTIFICATE.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_TENANT_PUBLIC_CERTIFICATE.getDescription(),
                            tenantDomain), e);
        }

        publicCerts.put(buildTenantIdWithContext(tenantId, context), publicCert);
        return publicCert;
    }

    /**
     * Concatenates tenantId and context with the separator.
     *
     * @param tenantId the key store name
     * @param context the context
     * @return a concatenated string in the format tenantDomain:context
     */
    private String buildTenantIdWithContext(int tenantId, String context) {

        return tenantId + IdentityKeyStoreResolverConstants.KEY_STORE_CONTEXT_SEPARATOR + context;
    }

    /**
     * Return Public Certificate of the Primary or tenant keystore according to given tenant domain.
     *
     * @param tenantDomain  Tenant domain.
     * @return Public Certificate of Primary or tenant keystore.
     * @throws IdentityKeyStoreResolverException the exception in the IdentityKeyStoreResolver class.
     */
    private Certificate getCertificate(String tenantDomain) throws IdentityKeyStoreResolverException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        if (publicCerts.containsKey(String.valueOf(tenantId))) {
            return publicCerts.get(String.valueOf(tenantId));
        }

        KeyStoreManager keyStoreManager = KeyStoreManager.getInstance(tenantId);
        Certificate publicCert;
        try {
            if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                publicCert = keyStoreManager.getDefaultPrimaryCertificate();
            } else {
                initializeTenantRegistry(tenantDomain);
                String tenantKeyStoreName = IdentityKeyStoreResolverUtil.buildTenantKeyStoreName(tenantDomain);
                publicCert = keyStoreManager.getCertificate(tenantKeyStoreName, tenantDomain);
            }
        } catch (Exception e) {
            throw new IdentityKeyStoreResolverException(
                    ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_TENANT_PUBLIC_CERTIFICATE.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_TENANT_PUBLIC_CERTIFICATE.getDescription(),
                            tenantDomain), e);
        }

        publicCerts.put(String.valueOf(tenantId), publicCert);
        return publicCert;
    }

    /**
     * Return Public Certificate of the Primary, tenant or custom keystore.
     *
     * @param tenantDomain      Tenant domain.
     * @param inboundProtocol   Inbound authentication protocol of the application.
     * @param context           Context of the keystore.
     * @return Public Certificate of the Primary, tenant or custom keystore.
     * @throws IdentityKeyStoreResolverException the exception in the IdentityKeyStoreResolver class.
     */
    public Certificate getCertificate(String tenantDomain, InboundProtocol inboundProtocol, String context)
            throws IdentityKeyStoreResolverException {


        if (StringUtils.isEmpty(tenantDomain)) {
            throw new IdentityKeyStoreResolverException(
                    ErrorMessages.ERROR_CODE_INVALID_ARGUMENT.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_INVALID_ARGUMENT.getDescription(), "Tenant domain"));
        }
        if (context != null) {
            return getCertificate(tenantDomain, context);
        }
        if (inboundProtocol == null) {
            return getCertificate(tenantDomain);
        }

        if (keyStoreMappings.containsKey(inboundProtocol)) {
            if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain) ||
                    keyStoreMappings.get(inboundProtocol).getUseInAllTenants()) {

                String keyStoreName = IdentityKeyStoreResolverUtil.buildCustomKeyStoreName(
                        keyStoreMappings.get(inboundProtocol).getKeyStoreName());

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Custom key store configuration available for " + inboundProtocol + " protocol. " +
                            "Retrieving public certificate from " + keyStoreName + " key store.");
                }

                if (publicCerts.containsKey(inboundProtocol.toString())) {
                    return publicCerts.get(inboundProtocol.toString());
                }

                try {
                    int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
                    KeyStoreManager keyStoreManager = KeyStoreManager.getInstance(tenantId);
                    Certificate publicCert = keyStoreManager.getCertificate(keyStoreName, null);
                    publicCerts.put(inboundProtocol.toString(), publicCert);
                    return publicCert;
                } catch (Exception e) {
                    throw new IdentityKeyStoreResolverException(
                            ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_CUSTOM_PUBLIC_CERTIFICATE.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_CUSTOM_PUBLIC_CERTIFICATE
                                    .getDescription(), keyStoreName), e);
                }
            }
        }
        return getCertificate(tenantDomain);
    }

    /**
     * Return Public Certificate of the Primary, tenant or custom keystore.
     *
     * @param tenantDomain      Tenant domain.
     * @param inboundProtocol   Inbound authentication protocol of the application.
     * @return Public Certificate of the Primary, tenant or custom keystore.
     * @throws IdentityKeyStoreResolverException the exception in the IdentityKeyStoreResolver class.
     */
    public Certificate getCertificate(String tenantDomain, InboundProtocol inboundProtocol)
            throws IdentityKeyStoreResolverException {

        return getCertificate(tenantDomain, inboundProtocol, null);
    }

    /**
     * Return Public Key of the Primary or tenant keystore according to given tenant domain.
     *
     * @param tenantDomain  Tenant domain.
     * @return Public Key of Primary or tenant keystore.
     * @throws IdentityKeyStoreResolverException the exception in the IdentityKeyStoreResolver class.
     */
    private RSAPublicKey getPublicKey(String tenantDomain) throws IdentityKeyStoreResolverException {

        return (RSAPublicKey) getCertificate(tenantDomain).getPublicKey();
    }

    /**
     * Return Public Key of the Primary, tenant or custom keystore.
     *
     * @param tenantDomain      Tenant domain.
     * @param inboundProtocol   Inbound authentication protocol of the application.
     * @return Public Key of the Primary, tenant or custom keystore.
     * @throws IdentityKeyStoreResolverException the exception in the IdentityKeyStoreResolver class.
     */
    public RSAPublicKey getPublicKey(String tenantDomain, InboundProtocol inboundProtocol)
            throws IdentityKeyStoreResolverException {

        // Conditions are checked in getCertificate method
        return (RSAPublicKey) getCertificate(tenantDomain, inboundProtocol).getPublicKey();
    }

    /**
     * Return keystore name of the Primary, tenant or custom keystore.
     *
     * @param tenantDomain      Tenant domain.
     * @param inboundProtocol   Inbound authentication protocol of the application.
     * @return Keystore name of the Primary, tenant or custom keystore.
     * @throws IdentityKeyStoreResolverException the exception in the IdentityKeyStoreResolver class.
     */
    public String getKeyStoreName(String tenantDomain, InboundProtocol inboundProtocol)
            throws IdentityKeyStoreResolverException {

        if (StringUtils.isEmpty(tenantDomain)) {
            throw new IdentityKeyStoreResolverException(
                    ErrorMessages.ERROR_CODE_INVALID_ARGUMENT.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_INVALID_ARGUMENT.getDescription(), "Tenant domain"));
        }
        if (inboundProtocol == null) {
            throw new IdentityKeyStoreResolverException(
                    ErrorMessages.ERROR_CODE_INVALID_ARGUMENT.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_INVALID_ARGUMENT.getDescription(), "Inbound protocol"));
        }

        if (keyStoreMappings.containsKey(inboundProtocol)) {
            if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain) ||
                    keyStoreMappings.get(inboundProtocol).getUseInAllTenants()) {

                return IdentityKeyStoreResolverUtil.buildCustomKeyStoreName(
                        keyStoreMappings.get(inboundProtocol).getKeyStoreName());
            }
        }

        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            try {
                File keyStoreFile = new File(getPrimaryKeyStoreConfig(
                        RegistryResources.SecurityManagement.CustomKeyStore.PROP_LOCATION));
                return keyStoreFile.getName();
            } catch (Exception e) {
                throw new IdentityKeyStoreResolverException(
                        ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_PRIMARY_KEYSTORE_CONFIGURATION.getCode(),
                        ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_PRIMARY_KEYSTORE_CONFIGURATION.getDescription(), e);
            }
        }

        return IdentityKeyStoreResolverUtil.buildTenantKeyStoreName(tenantDomain);
    }

    /**
     * Return key store configs of the Primary, tenant or custom keystore.
     *
     * @param tenantDomain      Tenant domain.
     * @param inboundProtocol   Inbound authentication protocol of the application.
     * @param configName        Name of the configuration needed.
     * @return Configuration value.
     * @throws IdentityKeyStoreResolverException the exception in the IdentityKeyStoreResolver class.
     */
    public String getKeyStoreConfig(String tenantDomain, InboundProtocol inboundProtocol, String configName)
            throws IdentityKeyStoreResolverException {

        if (StringUtils.isEmpty(tenantDomain)) {
            throw new IdentityKeyStoreResolverException(
                    ErrorMessages.ERROR_CODE_INVALID_ARGUMENT.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_INVALID_ARGUMENT.getDescription(), "Tenant domain"));
        }
        if (inboundProtocol == null) {
            throw new IdentityKeyStoreResolverException(
                    ErrorMessages.ERROR_CODE_INVALID_ARGUMENT.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_INVALID_ARGUMENT.getDescription(), "Inbound protocol"));
        }
        if (StringUtils.isEmpty(configName)) {
            throw new IdentityKeyStoreResolverException(
                    ErrorMessages.ERROR_CODE_INVALID_ARGUMENT.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_INVALID_ARGUMENT.getDescription(), "Config name"));
        }

        if (keyStoreMappings.containsKey(inboundProtocol)) {
            if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain) ||
                    keyStoreMappings.get(inboundProtocol).getUseInAllTenants()) {

                String keyStoreName = IdentityKeyStoreResolverUtil.buildCustomKeyStoreName(
                        keyStoreMappings.get(inboundProtocol).getKeyStoreName());

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Custom key store configuration available for " + inboundProtocol + " protocol. " +
                            "Retreiving " + configName + " config for " + keyStoreName + " key store.");
                }

                return getCustomKeyStoreConfig(keyStoreName, configName);
            }
        }

        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            return getPrimaryKeyStoreConfig(configName);
        }

        return getTenantKeyStoreConfig(tenantDomain, configName);

    }

    /**
     * Return custom key store.
     *
     * @param tenantDomain Tenant domain.
     * @param keyStoreName Name of the custom key store.
     * @return Custom key store.
     * @throws IdentityKeyStoreResolverException the exception in the IdentityKeyStoreResolver class.
     */
    public KeyStore getCustomKeyStore(String tenantDomain, String keyStoreName)
            throws IdentityKeyStoreResolverException {

        if (StringUtils.isEmpty(tenantDomain)) {
            throw new IdentityKeyStoreResolverException(
                    ErrorMessages.ERROR_CODE_INVALID_ARGUMENT.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_INVALID_ARGUMENT.getDescription(), "Tenant domain"));
        }
        if (StringUtils.isEmpty(keyStoreName)) {
            throw new IdentityKeyStoreResolverException(
                    ErrorMessages.ERROR_CODE_INVALID_ARGUMENT.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_INVALID_ARGUMENT.getDescription(), "Key store name"));
        }

        String customKeyStoreName = IdentityKeyStoreResolverUtil.buildCustomKeyStoreName(keyStoreName);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Custom key store configuration available. " +
                    "Retrieving keystore " + customKeyStoreName);
        }

        try {
            int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
            KeyStoreManager keyStoreManager = KeyStoreManager.getInstance(tenantId);
            return keyStoreManager.getKeyStore(customKeyStoreName);
        } catch (Exception e) {
            throw new IdentityKeyStoreResolverException(
                    ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_CUSTOM_KEYSTORE.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_CUSTOM_KEYSTORE.getDescription(),
                            customKeyStoreName), e);
        }
    }

    /**
     * Returns the trust store.
     *
     * @param tenantDomain Tenant domain.
     * @return Trust store.
     * @throws IdentityKeyStoreResolverException if an error occurs while retrieving the trust store.
     */
    public KeyStore getTrustStore(String tenantDomain) throws IdentityKeyStoreResolverException {

        try {
            KeyStoreManager keyStoreManager = KeyStoreManager.getInstance(IdentityTenantUtil.getTenantId(tenantDomain));
            return keyStoreManager.getTrustStore();
        } catch (CarbonException e) {
            throw new IdentityKeyStoreResolverException(
                    ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_TRUSTSTORE.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_TRUSTSTORE.getDescription(), tenantDomain),
                    e);
        }
    }

    private String getPrimaryKeyStoreConfig(String configName) throws IdentityKeyStoreResolverException {

        try {
            KeyStoreUtil.validateKeyStoreConfigName(configName);

            String fullConfigPath = IdentityKeyStoreResolverConstants.PRIMARY_KEYSTORE_CONFIG_PATH + configName;
            return CarbonUtils.getServerConfiguration().getFirstProperty(fullConfigPath);
        } catch (CarbonException e) {
            throw new IdentityKeyStoreResolverException(
                    ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_PRIMARY_KEYSTORE_CONFIGURATION.getCode(),
                    ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_PRIMARY_KEYSTORE_CONFIGURATION.getDescription(), e);
        }
    }

    private String getTenantKeyStoreConfig(String tenantDomain, String configName)
            throws IdentityKeyStoreResolverException {

        initializeTenantRegistry(tenantDomain);
        try {
            KeyStoreUtil.validateKeyStoreConfigName(configName);

            int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
            KeyStoreManager keyStoreManager = KeyStoreManager.getInstance(tenantId);
            String keyStoreName = IdentityKeyStoreResolverUtil.buildTenantKeyStoreName(tenantDomain);
            switch (configName) {
                case (RegistryResources.SecurityManagement.CustomKeyStore.PROP_LOCATION):
                    // Returning only key store name because tenant key stores reside within the registry.
                    return keyStoreName;
                case (RegistryResources.SecurityManagement.CustomKeyStore.PROP_TYPE):
                    KeyStore keyStore = keyStoreManager.getKeyStore(keyStoreName);
                    return keyStore.getType();
                case (RegistryResources.SecurityManagement.CustomKeyStore.PROP_PASSWORD):
                    return keyStoreManager.getKeyStorePassword(keyStoreName);
                case (RegistryResources.SecurityManagement.CustomKeyStore.PROP_KEY_PASSWORD):
                    return keyStoreManager.getKeyStorePassword(keyStoreName);
                case (RegistryResources.SecurityManagement.CustomKeyStore.PROP_KEY_ALIAS):
                    return tenantDomain;
                default:
                    // This state is not possible since config name is validated above.
                    throw new IdentityKeyStoreResolverException(
                            ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_TENANT_KEYSTORE_CONFIGURATION.getCode(),
                            String.format(ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_TENANT_KEYSTORE_CONFIGURATION
                                    .getDescription(), tenantDomain));
            }
        } catch (Exception e) {
            throw new IdentityKeyStoreResolverException(
                    ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_TENANT_KEYSTORE_CONFIGURATION.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_TENANT_KEYSTORE_CONFIGURATION
                            .getDescription(), tenantDomain), e);
        }
    }

    public String getCustomKeyStoreConfig(String keyStoreName, String configName)
            throws IdentityKeyStoreResolverException {

        try {
            KeyStoreUtil.validateKeyStoreConfigName(configName);

            OMElement configElement = KeyStoreUtil
                    .getCustomKeyStoreConfigElement(keyStoreName, CarbonUtils.getServerConfiguration());
            return KeyStoreUtil.getCustomKeyStoreConfig(configElement, configName);
        } catch (CarbonException e) {
            throw new IdentityKeyStoreResolverException(
                    ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_CUSTOM_KEYSTORE_CONFIGURATION.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_CUSTOM_KEYSTORE_CONFIGURATION
                            .getDescription(), keyStoreName), e);
        }
    }

    private void parseIdentityKeyStoreMappingConfigs() {

        OMElement keyStoreMappingsElem = null;
        IdentityConfigParser configParser = IdentityConfigParser.getInstance();
        if (configParser != null) {
            OMElement securityElem = configParser.getConfigElement(
                    IdentityKeyStoreResolverConstants.CONFIG_ELEM_SECURITY);
            if (securityElem != null) {
                keyStoreMappingsElem = securityElem.getFirstChildWithName(
                        IdentityKeyStoreResolverUtil.getQNameWithIdentityNameSpace(
                                IdentityKeyStoreResolverConstants.CONFIG_ELEM_KEYSTORE_MAPPING));
            }
        }

        if (keyStoreMappingsElem == null) {
            LOG.warn(String.format("%s.%s element not found in identity.xml file.",
                    IdentityKeyStoreResolverConstants.CONFIG_ELEM_SECURITY,
                    IdentityKeyStoreResolverConstants.CONFIG_ELEM_KEYSTORE_MAPPING));
            return;
        }

        // Parse OAuth KeyStore Mapping.
        OMElement oauthKeyStoreMapping = keyStoreMappingsElem.getFirstChildWithName(
                IdentityKeyStoreResolverUtil.getQNameWithIdentityNameSpace(
                        IdentityKeyStoreResolverConstants.CONFIG_ELEM_OAUTH));
        if (oauthKeyStoreMapping != null) {
            addKeyStoreMapping(InboundProtocol.OAUTH, oauthKeyStoreMapping);
        }

        // Parse WS-Trust KeyStore Mapping.
        OMElement wsTrustKeyStoreMapping = keyStoreMappingsElem.getFirstChildWithName(
                IdentityKeyStoreResolverUtil.getQNameWithIdentityNameSpace(
                        IdentityKeyStoreResolverConstants.CONFIG_ELEM_WS_TRUST));
        if (wsTrustKeyStoreMapping != null) {
            addKeyStoreMapping(InboundProtocol.WS_TRUST, wsTrustKeyStoreMapping);
        }

        // Parse WS-Federation KeyStore Mapping.
        OMElement wsFedKeyStoreMapping = keyStoreMappingsElem.getFirstChildWithName(
                IdentityKeyStoreResolverUtil.getQNameWithIdentityNameSpace(
                        IdentityKeyStoreResolverConstants.CONFIG_ELEM_WS_FEDERATION));
        if (wsFedKeyStoreMapping != null) {
            addKeyStoreMapping(InboundProtocol.WS_FEDERATION, wsFedKeyStoreMapping);
        }
    }

    private void addKeyStoreMapping(InboundProtocol protocol, OMElement keyStoreMapping) {

        // Parse keystore name
        OMElement keyStoreNameElement = keyStoreMapping.getFirstChildWithName(
                IdentityKeyStoreResolverUtil.getQNameWithIdentityNameSpace(
                        IdentityKeyStoreResolverConstants.ATTR_NAME_KEYSTORE_NAME));
        if (keyStoreNameElement == null || keyStoreNameElement.getText().isEmpty()) {
            LOG.error("Error occurred when reading KeyStoreMapping configuration. KeyStoreName value null.");
            return;
        }
        String keyStoreName = keyStoreNameElement.getText();

        // Parse UseInAllTenants config
        OMElement useInAllTenantsElement = keyStoreMapping.getFirstChildWithName(
                IdentityKeyStoreResolverUtil.getQNameWithIdentityNameSpace(
                        IdentityKeyStoreResolverConstants.ATTR_NAME_USE_IN_ALL_TENANTS));
        if (useInAllTenantsElement == null || useInAllTenantsElement.getText().isEmpty()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("use_in_all_tenants config null for " + protocol.toString() + ". Using default value as false.");
            }
        }
        Boolean useInAllTenants = Boolean.valueOf(useInAllTenantsElement.getText());

        // Add custom keystore mapping to the map
        IdentityKeyStoreMapping identityKeyStoreMapping = new IdentityKeyStoreMapping(
                keyStoreName, protocol, useInAllTenants);
        keyStoreMappings.put(protocol, identityKeyStoreMapping);
    }

    /**
     * Initialize tenant registry.
     *
     * @param tenantDomain Tenant domain.
     * @throws IdentityKeyStoreResolverException If an error occurs while loading the registry.
     */
    private void initializeTenantRegistry(String tenantDomain) throws IdentityKeyStoreResolverException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        try {
            IdentityTenantUtil.initializeRegistry(tenantId);
        } catch (Exception e) {
            throw new IdentityKeyStoreResolverException(ErrorMessages.ERROR_WHILE_LOADING_REGISTRY.getCode(),
                    String.format(ErrorMessages.ERROR_WHILE_LOADING_REGISTRY.getDescription(),
                            tenantDomain), e);
        }
    }
}
