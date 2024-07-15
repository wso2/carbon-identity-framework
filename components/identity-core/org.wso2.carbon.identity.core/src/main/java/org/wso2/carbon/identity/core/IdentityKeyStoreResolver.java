/*
 * Copyright (c) 2024, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.core;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.identity.core.model.IdentityKeyStoreMapping;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.core.util.IdentityKeyStoreResolverConstants;
import org.wso2.carbon.identity.core.util.IdentityKeyStoreResolverConstants.InboundProtocol;
import org.wso2.carbon.identity.core.util.IdentityKeyStoreResolverException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.wso2.carbon.identity.core.util.IdentityKeyStoreResolverUtil.buildCustomKeyStoreName;
import static org.wso2.carbon.identity.core.util.IdentityKeyStoreResolverUtil.buildTenantKeyStoreName;
import static org.wso2.carbon.identity.core.util.IdentityKeyStoreResolverUtil.getQNameWithIdentityNameSpace;


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

    private static Log log = LogFactory.getLog(IdentityKeyStoreResolver.class);

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
     * @param tenantDomain  Tenant domain
     * @return Primary or tenant keystore
     * @throws IdentityKeyStoreResolverException the exception in the IdentityKeyStoreResolver class
     */
    public KeyStore getKeyStore(String tenantDomain) throws IdentityKeyStoreResolverException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        KeyStoreManager keyStoreManager = KeyStoreManager.getInstance(tenantId);
        try {
            if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                // Get primary keystore from keyStoreManager
                return keyStoreManager.getPrimaryKeyStore();
            }

            // Get tenant keystore from keyStoreManager
            String tenantKeyStoreName = buildTenantKeyStoreName(tenantDomain);
            return keyStoreManager.getTenantKeyStore(tenantKeyStoreName);
        } catch (Exception e) {
            throw new IdentityKeyStoreResolverException(
                    "Error occurred when retrieving keystore for tenant: " + tenantDomain, e);
        }
    }

    /**
     * Return Primary, tenant or custom keystore.
     *
     * @param tenantDomain      Tenant domain
     * @param inboundProtocol   Inbound authentication protocol of the application
     * @return Primary, tenant or custom keystore
     * @throws IdentityKeyStoreResolverException the exception in the IdentityKeyStoreResolver class
     */
    public KeyStore getKeyStore(String tenantDomain, InboundProtocol inboundProtocol)
            throws IdentityKeyStoreResolverException {

        if (keyStoreMappings.containsKey(inboundProtocol)) {
            if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain) ||
                    keyStoreMappings.get(inboundProtocol).getUseInAllTenants()) {
                if (log.isDebugEnabled()) {
                    log.debug("Custom keystore configuration avialble for " + inboundProtocol + " protocol.");
                }

                String keyStoreName = buildCustomKeyStoreName(keyStoreMappings.get(inboundProtocol).getKeyStoreName());

                try {
                    int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
                    KeyStoreManager keyStoreManager = KeyStoreManager.getInstance(tenantId);
                    return keyStoreManager.getCustomKeyStore(keyStoreName);
                } catch (Exception e) {
                    throw new IdentityKeyStoreResolverException(
                            "Error occurred when retrieving keystore for protocol: " + inboundProtocol, e);
                }
            }
        }

        return getKeyStore(tenantDomain);
    }

    /**
     * Return Primary key of the Primary or tenant keystore according to given tenant domain.
     *
     * @param tenantDomain  Tenant domain
     * @return Primary key of Primary or tenant keystore
     * @throws IdentityKeyStoreResolverException the exception in the IdentityKeyStoreResolver class
     */
    public Key getPrivateKey(String tenantDomain) throws IdentityKeyStoreResolverException {

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
                String tenantKeyStoreName = buildTenantKeyStoreName(tenantDomain);
                privateKey = keyStoreManager.getTenantPrivateKey(tenantKeyStoreName, tenantDomain);
            }
        } catch (Exception e) {
            throw new IdentityKeyStoreResolverException(
                    "Error occurred when retrieving private key tenant: " + tenantDomain, e);
        }

        privateKeys.put(String.valueOf(tenantId), privateKey);
        return privateKey;
    }

    /**
     * Return Private Key of the Primary, tenant or custom keystore.
     *
     * @param tenantDomain      Tenant domain
     * @param inboundProtocol   Inbound authentication protocol of the application
     * @return Private Key of the Primary, tenant or custom keystore
     * @throws IdentityKeyStoreResolverException the exception in the IdentityKeyStoreResolver class
     */
    public Key getPrivateKey(String tenantDomain, InboundProtocol inboundProtocol)
            throws IdentityKeyStoreResolverException {

        if (keyStoreMappings.containsKey(inboundProtocol)) {
            if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain) ||
                    keyStoreMappings.get(inboundProtocol).getUseInAllTenants()) {
                if (log.isDebugEnabled()) {
                    log.debug("Custom keystore configuration availble for " + inboundProtocol + " protocol.");
                }

                if (privateKeys.containsKey(inboundProtocol.toString())) {
                    return privateKeys.get(inboundProtocol.toString());
                }

                String keyStoreName = buildCustomKeyStoreName(keyStoreMappings.get(inboundProtocol).getKeyStoreName());

                try {
                    int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
                    KeyStoreManager keyStoreManager = KeyStoreManager.getInstance(tenantId);
                    Key privateKey = keyStoreManager.getCustomKeyStorePrivateKey(keyStoreName);
                    privateKeys.put(inboundProtocol.toString(), privateKey);
                    return privateKey;
                } catch (Exception e) {
                    throw new IdentityKeyStoreResolverException(
                            "Error occurred when retrieving private key from keystore: " + keyStoreName, e);
                }
            }
        }
        return getPrivateKey(tenantDomain);
    }

    /**
     * Return Public Certificate of the Primary or tenant keystore according to given tenant domain.
     *
     * @param tenantDomain  Tenant domain
     * @return Public Certificate of Primary or tenant keystore
     * @throws IdentityKeyStoreResolverException the exception in the IdentityKeyStoreResolver class
     */
    public Certificate getCertificate(String tenantDomain) throws IdentityKeyStoreResolverException {

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
                String tenantKeyStoreName = buildTenantKeyStoreName(tenantDomain);
                publicCert = keyStoreManager.getTenantCertificate(tenantKeyStoreName, tenantDomain);
            }
        } catch (Exception e) {
            throw new IdentityKeyStoreResolverException("Error occurred when retrieving public certificate", e);
        }

        publicCerts.put(String.valueOf(tenantId), publicCert);
        return publicCert;
    }

    /**
     * Return Public Certificate of the Primary, tenant or custom keystore.
     *
     * @param tenantDomain      Tenant domain
     * @param inboundProtocol   Inbound authentication protocol of the application
     * @return Public Certificate of the Primary, tenant or custom keystore
     * @throws IdentityKeyStoreResolverException the exception in the IdentityKeyStoreResolver class
     */
    public Certificate getCertificate(String tenantDomain, InboundProtocol inboundProtocol)
            throws IdentityKeyStoreResolverException {

        if (keyStoreMappings.containsKey(inboundProtocol)) {
            if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain) ||
                    keyStoreMappings.get(inboundProtocol).getUseInAllTenants()) {
                if (log.isDebugEnabled()) {
                    log.debug("Custom keystore configuration for " + inboundProtocol + " protocol.");
                }

                if (publicCerts.containsKey(inboundProtocol.toString())) {
                    return publicCerts.get(inboundProtocol.toString());
                }

                String keyStoreName = buildCustomKeyStoreName(keyStoreMappings.get(inboundProtocol).getKeyStoreName());

                try {
                    int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
                    KeyStoreManager keyStoreManager = KeyStoreManager.getInstance(tenantId);
                    Certificate publicCert = keyStoreManager.getCustomKeyStoreCertificate(keyStoreName);
                    publicCerts.put(inboundProtocol.toString(), publicCert);
                    return publicCert;
                } catch (Exception e) {
                    throw new IdentityKeyStoreResolverException("Error occurred when retrieving public certificate", e);
                }
            }
        }
        return getCertificate(tenantDomain);
    }

    /**
     * Return Public Key of the Primary or tenant keystore according to given tenant domain.
     *
     * @param tenantDomain  Tenant domain
     * @return Public Key of Primary or tenant keystore
     * @throws IdentityKeyStoreResolverException the exception in the IdentityKeyStoreResolver class
     */
    public RSAPublicKey getPublicKey(String tenantDomain) throws IdentityKeyStoreResolverException {

        return (RSAPublicKey) getCertificate(tenantDomain).getPublicKey();
    }

    /**
     * Return Public Key of the Primary, tenant or custom keystore.
     *
     * @param tenantDomain      Tenant domain
     * @param inboundProtocol   Inbound authentication protocol of the application
     * @return Public Key of the Primary, tenant or custom keystore
     * @throws IdentityKeyStoreResolverException the exception in the IdentityKeyStoreResolver class
     */
    public RSAPublicKey getPublicKey(String tenantDomain, InboundProtocol inboundProtocol)
            throws IdentityKeyStoreResolverException {

        // Conditions are checked in getCertificate method
        return (RSAPublicKey) getCertificate(tenantDomain, inboundProtocol).getPublicKey();
    }

    private void parseIdentityKeyStoreMappingConfigs() {

        OMElement securityElem = IdentityConfigParser.getInstance().getConfigElement(
                IdentityKeyStoreResolverConstants.CONFIG_ELEM_SECURITY);
        OMElement keyStoreMappingsElem = securityElem.getFirstChildWithName(
                getQNameWithIdentityNameSpace(IdentityKeyStoreResolverConstants.CONFIG_ELEM_KEYSTORE_MAPPINGS));

        if (keyStoreMappingsElem == null) {
            if (log.isDebugEnabled()) {
                log.debug("No CustomKeyStoreMapping configurations found.");
            }
            return;
        }

        Iterator<OMElement> iterator = keyStoreMappingsElem.getChildrenWithName(
                getQNameWithIdentityNameSpace(IdentityKeyStoreResolverConstants.CONFIG_ELEM_KEYSTORE_MAPPING));
        while (iterator.hasNext()) {
            OMElement keyStoreMapping = iterator.next();

            // Parse inbound protocol
            OMElement protocolElement = keyStoreMapping.getFirstChildWithName(
                    getQNameWithIdentityNameSpace(IdentityKeyStoreResolverConstants.ATTR_NAME_PROTOCOL));
            if (protocolElement == null) {
                log.error("Error occurred when reading configuration. CustomKeyStoreMapping Protocol value null.");
                continue;
            }

            // Parse inbound protocol name
            InboundProtocol protocol = InboundProtocol.fromString(protocolElement.getText());
            if (protocol == null) {
                log.warn("Invalid authentication protocol configuration in CustomKeyStoreMappings. Config ignored.");
                continue;
            }
            if (keyStoreMappings.containsKey(protocol)) {
                log.warn("Multiple CustomKeyStoreMappings configured for " + protocol.toString() +
                        " protocol. Second config ignored.");
                continue;
            }
            // TODO: Remove after SAML implementation
            if (protocol == InboundProtocol.SAML) {
                log.warn("CustomKeyStoreMapping for SAML configured. This is not supported yet." +
                        " Please use [keystore.saml] configuration.");
            }

            // Parse keystore name
            OMElement keyStoreNameElement = keyStoreMapping.getFirstChildWithName(
                    getQNameWithIdentityNameSpace(IdentityKeyStoreResolverConstants.ATTR_NAME_KEYSTORE_NAME));
            if (keyStoreNameElement == null) {
                log.error("Error occurred when reading configuration. CustomKeyStoreMapping KeyStoreName value null.");
                continue;
            }
            String keyStoreName = keyStoreNameElement.getText();

            // Parse UseInAllTenants config
            OMElement useInAllTenantsElement = keyStoreMapping.getFirstChildWithName(
                    getQNameWithIdentityNameSpace(IdentityKeyStoreResolverConstants.ATTR_NAME_USE_IN_ALL_TENANTS));
            if (useInAllTenantsElement == null) {
                log.error("Error occurred when reading configuration. " +
                        "CustomKeyStoreMapping useInAllTenants value null.");
                continue;
            }
            Boolean useInAllTenants = Boolean.valueOf(useInAllTenantsElement.getText());

            // Add custom keystore mapping to the map
            IdentityKeyStoreMapping identityKeyStoreMapping = new IdentityKeyStoreMapping(
                    keyStoreName, protocol, useInAllTenants);
            keyStoreMappings.put(protocol, identityKeyStoreMapping);
        }
    }
}
