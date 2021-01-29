/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.security.keystore;

import org.apache.axiom.om.util.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.core.util.KeyStoreUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.security.SecurityConfigException;
import org.wso2.carbon.security.SecurityConstants;
import org.wso2.carbon.security.keystore.service.CertData;
import org.wso2.carbon.security.keystore.service.CertDataDetail;
import org.wso2.carbon.security.keystore.service.KeyData;
import org.wso2.carbon.security.keystore.service.KeyStoreData;
import org.wso2.carbon.security.util.KeyStoreMgtUtil;

import java.io.ByteArrayOutputStream;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.wso2.carbon.security.SecurityConstants.KeyStoreMgtConstants.ErrorMessage.ERROR_CODE_ADD_CERTIFICATE;
import static org.wso2.carbon.security.SecurityConstants.KeyStoreMgtConstants.ErrorMessage.ERROR_CODE_ADD_PRIVATE_KEY;
import static org.wso2.carbon.security.SecurityConstants.KeyStoreMgtConstants.ErrorMessage.ERROR_CODE_ALIAS_EXISTS;
import static org.wso2.carbon.security.SecurityConstants.KeyStoreMgtConstants.ErrorMessage.ERROR_CODE_AN_ENTRY_FOR_THE_GIVEN_ALIAS_EXISTS;
import static org.wso2.carbon.security.SecurityConstants.KeyStoreMgtConstants.ErrorMessage.ERROR_CODE_BAD_VALUE_FOR_FILTER;
import static org.wso2.carbon.security.SecurityConstants.KeyStoreMgtConstants.ErrorMessage.ERROR_CODE_CANNOT_DELETE_TENANT_CERT;
import static org.wso2.carbon.security.SecurityConstants.KeyStoreMgtConstants.ErrorMessage.ERROR_CODE_CERTIFICATE_EXISTS;
import static org.wso2.carbon.security.SecurityConstants.KeyStoreMgtConstants.ErrorMessage.ERROR_CODE_DELETE_CERTIFICATE;
import static org.wso2.carbon.security.SecurityConstants.KeyStoreMgtConstants.ErrorMessage.ERROR_CODE_EMPTY_ALIAS;
import static org.wso2.carbon.security.SecurityConstants.KeyStoreMgtConstants.ErrorMessage.ERROR_CODE_GET_ALL_PRIVATE_KEYS;
import static org.wso2.carbon.security.SecurityConstants.KeyStoreMgtConstants.ErrorMessage.ERROR_CODE_GET_PRIVATE_KEY;
import static org.wso2.carbon.security.SecurityConstants.KeyStoreMgtConstants.ErrorMessage.ERROR_CODE_INVALID_CERTIFICATE;
import static org.wso2.carbon.security.SecurityConstants.KeyStoreMgtConstants.ErrorMessage.ERROR_CODE_NO_PRIVATE_KEY_FOR_THE_GIVEN_ALIAS;
import static org.wso2.carbon.security.SecurityConstants.KeyStoreMgtConstants.ErrorMessage.ERROR_CODE_ONLY_ONE_PRIVATE_KEY_EXISTS;
import static org.wso2.carbon.security.SecurityConstants.KeyStoreMgtConstants.ErrorMessage.ERROR_CODE_RETRIEVE_CLIENT_TRUSTSTORE;
import static org.wso2.carbon.security.SecurityConstants.KeyStoreMgtConstants.ErrorMessage.ERROR_CODE_RETRIEVE_CLIENT_TRUSTSTORE_CERTIFICATE;
import static org.wso2.carbon.security.SecurityConstants.KeyStoreMgtConstants.ErrorMessage.ERROR_CODE_RETRIEVE_KEYSTORE;
import static org.wso2.carbon.security.SecurityConstants.KeyStoreMgtConstants.ErrorMessage.ERROR_CODE_RETRIEVE_KEYSTORE_INFORMATION;
import static org.wso2.carbon.security.SecurityConstants.KeyStoreMgtConstants.ErrorMessage.ERROR_CODE_UNSUPPORTED_ADDING_KEYS_FOR_SUPER_TENANT;
import static org.wso2.carbon.security.SecurityConstants.KeyStoreMgtConstants.ErrorMessage.ERROR_CODE_UNSUPPORTED_DELETION_OF_SIGNING_KEY;
import static org.wso2.carbon.security.SecurityConstants.KeyStoreMgtConstants.ErrorMessage.ERROR_CODE_UNSUPPORTED_FILTER_OPERATION;
import static org.wso2.carbon.security.SecurityConstants.KeyStoreMgtConstants.ErrorMessage.ERROR_CODE_UPDATE_KEYSTORE;
import static org.wso2.carbon.security.SecurityConstants.KeyStoreMgtConstants.ErrorMessage.ERROR_CODE_VALIDATE_CERTIFICATE;
import static org.wso2.carbon.security.SecurityConstants.KeyStoreMgtConstants.FILTER_FIELD_ALIAS;
import static org.wso2.carbon.security.SecurityConstants.KeyStoreMgtConstants.FILTER_OPERATION_CONTAINS;
import static org.wso2.carbon.security.SecurityConstants.KeyStoreMgtConstants.FILTER_OPERATION_ENDS_WITH;
import static org.wso2.carbon.security.SecurityConstants.KeyStoreMgtConstants.FILTER_OPERATION_EQUALS;
import static org.wso2.carbon.security.SecurityConstants.KeyStoreMgtConstants.FILTER_OPERATION_STARTS_WITH;
import static org.wso2.carbon.security.SecurityConstants.KeyStoreMgtConstants.SERVER_TRUSTSTORE_FILE;

/**
 * This class is used to manage the keystore certificates.
 */
public class KeyStoreManagementServiceImpl implements KeyStoreManagementService {

    private static final Log log = LogFactory.getLog(KeyStoreManagementServiceImpl.class);
    private static final String CERT_PEM_START = "-----(BEGIN CERTIFICATE)-----";
    private static final String CERT_PEM_END = "-----(END CERTIFICATE)-----";
    private static final String[] SUPPORTED_FINGER_PRINTS_ALG = {"MD5", "SHA1", "SHA-256"};
    private final Pattern pattern;

    public KeyStoreManagementServiceImpl() {

        this.pattern = Pattern.compile(CERT_PEM_START + "([^-]+)" + CERT_PEM_END);
    }

    @Override
    public List<String> getKeyStoreCertificateAliases(String tenantDomain, String filter)
            throws KeyStoreManagementException {

        KeyStoreData keyStoreInfo = getKeystoreData(tenantDomain, getKeyStoreName(tenantDomain));
        return filterAlias(getAliasList(keyStoreInfo), filter);
    }

    @Override
    public Map<String, X509Certificate> getPublicCertificate(String tenantDomain) throws KeyStoreManagementException {

        Map<String, X509Certificate> certData = new HashMap<>();
        KeyStoreData keyStoreInfo = getKeystoreData(tenantDomain, getKeyStoreName(tenantDomain));
        CertData key = keyStoreInfo.getKey();
        certData.put(key.getAlias(), ((CertDataDetail) key).getCertificate());
        return certData;
    }

    @Override
    public X509Certificate getKeyStoreCertificate(String tenantDomain, String alias)
            throws KeyStoreManagementException {

        if (StringUtils.isEmpty(alias)) {
            throw handleClientException(ERROR_CODE_EMPTY_ALIAS, null);
        }

        KeyStoreData keyStoreInfo = getKeystoreData(tenantDomain, getKeyStoreName(tenantDomain));
        CertData key = keyStoreInfo.getKey();
        if (key != null && StringUtils.equals(key.getAlias(), alias)) {
            return ((CertDataDetail) key).getCertificate();
        }

        CertData[] certDataArray = keyStoreInfo.getCerts();
        for (CertData certData : certDataArray) {
            String aliasFromKeyStore = certData.getAlias();
            if (StringUtils.equals(aliasFromKeyStore, alias)) {
                return ((CertDataDetail) certData).getCertificate();
            }
        }
        return null;
    }

    @Override
    public List<String> getClientCertificateAliases(String tenantDomain, String filter)
            throws KeyStoreManagementException {

        KeyStoreData truststoreInfo = getKeystoreData(tenantDomain, getTrustStoreName());
        return filterAlias(getAliasList(truststoreInfo), filter);
    }

    @Override
    public X509Certificate getClientCertificate(String tenantDomain, String alias) throws KeyStoreManagementException {

        if (StringUtils.isEmpty(alias)) {
            throw handleClientException(ERROR_CODE_EMPTY_ALIAS, null);
        }

        KeyStore trustStore = null;
        try {
            trustStore = getKeyStoreAdmin(tenantDomain).getTrustStore();
        } catch (SecurityConfigException e) {
            throw handleServerException(ERROR_CODE_RETRIEVE_CLIENT_TRUSTSTORE, tenantDomain, e);
        }

        if (trustStore != null) {
            try {
                if (trustStore.containsAlias(alias)) {
                    return (X509Certificate) trustStore.getCertificate(alias);
                }
            } catch (KeyStoreException e) {
                throw handleServerException(ERROR_CODE_RETRIEVE_CLIENT_TRUSTSTORE_CERTIFICATE, alias, e);
            }
        }
        return null;
    }

    @Override
    public void addCertificate(String tenantDomain, String alias, String certificate)
            throws KeyStoreManagementException {

        KeyStoreAdmin keyStoreAdmin = getKeyStoreAdmin(tenantDomain);
        String keyStoreName = getKeyStoreName(tenantDomain);
        X509Certificate cert;
        try {
            cert = keyStoreAdmin.extractCertificate(certificate);
        } catch (SecurityConfigException e) {
            throw handleClientException(ERROR_CODE_INVALID_CERTIFICATE, alias);
        }
        KeyStore keyStore;
        String certAlias;
        boolean isAliasExists;
        try {
            keyStore = keyStoreAdmin.getKeyStore(keyStoreName);
            isAliasExists = keyStore.containsAlias(alias);
            certAlias = keyStore.getCertificateAlias(cert);
        } catch (Exception e) {
            throw handleServerException(ERROR_CODE_VALIDATE_CERTIFICATE, null, e);
        }
        if (isAliasExists) {
            throw handleClientException(ERROR_CODE_ALIAS_EXISTS, alias);
        }
        if (certAlias != null) {
            throw handleClientException(ERROR_CODE_CERTIFICATE_EXISTS, certAlias);
        }
        try {
            keyStoreAdmin.importCertToStore(alias, certificate, keyStoreName);
        } catch (SecurityConfigException e) {
            throw handleServerException(ERROR_CODE_ADD_CERTIFICATE, alias, e);
        }
    }

    @Override
    public void deleteCertificate(String tenantDomain, String alias) throws KeyStoreManagementException {

        try {
            Map<String, X509Certificate> publicCertificate = getPublicCertificate(tenantDomain);
            if (publicCertificate.keySet().contains(alias)) {
                throw handleClientException(ERROR_CODE_CANNOT_DELETE_TENANT_CERT, alias);
            }
            getKeyStoreAdmin(tenantDomain).removeCertFromStore(alias, getKeyStoreName(tenantDomain));
            KeyStoreAdmin keyStoreAdmin = getKeyStoreAdmin(tenantDomain);
            String keystoreName = getKeyStoreName(tenantDomain);
            if (!isPrivateKey(keyStoreAdmin, keystoreName, alias)) {
                keyStoreAdmin.removeCertFromStore(alias, keystoreName);
            }
        } catch (Exception e) {
            throw handleServerException(ERROR_CODE_DELETE_CERTIFICATE, alias, e);
        }
    }

    @Override
    public void addPrivateKey(String alias, String privateKeyContent, String certificateChain, String tenantDomain) throws KeyStoreManagementException {

        if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            log.error("Adding private key for super tenant is not supported");
            throw handleClientException(ERROR_CODE_UNSUPPORTED_ADDING_KEYS_FOR_SUPER_TENANT, alias);
        }
        String keyStoreName = getKeyStoreName(tenantDomain);
        KeyStore keyStore;
        KeyStoreManager keyStoreManager;
        try {
            keyStoreManager = getKeyStoreManager(tenantDomain);
            keyStore = keyStoreManager.getKeyStore(keyStoreName);
        } catch (Exception e) {
            throw handleServerException(ERROR_CODE_ADD_PRIVATE_KEY, alias, e);
        }
        boolean isKeyStoreContainsAlias;
        try {
            isKeyStoreContainsAlias = keyStore.containsAlias(alias);
        } catch (Exception e) {
            throw handleServerException(ERROR_CODE_ADD_PRIVATE_KEY, alias, e);
        }
        if (isKeyStoreContainsAlias) {
            if (log.isDebugEnabled()) {
                log.debug("Keystore of the tenant: " + tenantDomain + " already contains an entry with the alias: "
                        + alias);
            }
            throw handleClientException(ERROR_CODE_AN_ENTRY_FOR_THE_GIVEN_ALIAS_EXISTS, alias);
        }
        try {
            char[] password = keyStoreManager.getKeyStorePassword(keyStoreName).toCharArray();
            Key privateKey = KeyStoreMgtUtil.extractPrivateKey(privateKeyContent);
            Certificate[] x509CertificateChain = extractCertificateChain(certificateChain);
            keyStore.setKeyEntry(alias, privateKey, password, x509CertificateChain);
            updateKeyStore(keyStore, tenantDomain, password);
        } catch (Exception e) {
            throw handleServerException(ERROR_CODE_ADD_PRIVATE_KEY, alias, e);
        }
    }

    @Override
    public KeyData getPrivateKeyData(String alias, String tenantDomain) throws KeyStoreManagementException {

        KeyData data = null;
        KeyStoreAdmin keyStoreAdmin = getKeyStoreAdmin(tenantDomain);
        String keystoreName = getKeyStoreName(tenantDomain);
        KeyStore keyStore;
        try {
            keyStore = keyStoreAdmin.getKeyStore(keystoreName);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Error while getting private key for the alias: " + alias);
            }
            throw handleServerException(ERROR_CODE_GET_PRIVATE_KEY, alias, e);
        }
        try {
            if (!keyStore.isKeyEntry(alias)) {
                if (log.isDebugEnabled()) {
                    log.debug("The alias: " + alias + " does not have any private key");
                }
                throw handleClientException(ERROR_CODE_NO_PRIVATE_KEY_FOR_THE_GIVEN_ALIAS, alias);
            }
        } catch (KeyStoreException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error while deleting private key for the alias: " + alias);
            }
            throw handleServerException(ERROR_CODE_GET_PRIVATE_KEY, alias, e);
        }
        try {
            data = getPrivateKeyInfo(alias, keyStore);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Error while getting private key for the alias: " + alias);
            }
            throw handleServerException(ERROR_CODE_GET_PRIVATE_KEY, alias, e);
        }
        return data;
    }

    @Override
    public List<KeyData> getAllPrivateKeys(String filter, String tenantDomain) throws KeyStoreManagementException {

        List<KeyData> keyDataList = new ArrayList<>();
        try {
            KeyStoreManager keyStoreManager = getKeyStoreManager(tenantDomain);
            String keystoreName = getKeyStoreName(tenantDomain);
            KeyStore keyStore = keyStoreManager.getKeyStore(keystoreName);
            List<String> privateKeyAliasList = getPrivateKeyAliasList(keyStore);
            List<String> filteredAlias = filterAlias(privateKeyAliasList, filter);
            for (String alias : filteredAlias) {
                KeyData certData = getPrivateKeyInfo(alias, keyStore);
                // Obtain private key data.
                keyDataList.add(certData);
            }
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Error while getting all private keys for the tenant: " + tenantDomain);
            }
            throw handleServerException(ERROR_CODE_GET_ALL_PRIVATE_KEYS, tenantDomain, e);
        }
        return keyDataList;
    }

    @Override
    public void deletePrivateKey(String alias, String tenantDomain) throws KeyStoreManagementException {

        if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            log.error("Delete a private key for super tenant is not supported");
            return;
        }
        KeyStoreAdmin keyStoreAdmin = getKeyStoreAdmin(tenantDomain);
        String keystoreName = getKeyStoreName(tenantDomain);
        boolean isPrivateKey = isPrivateKey(keyStoreAdmin, keystoreName, alias);
        KeyStore keyStore;
        if (!isPrivateKey) {
            if (log.isDebugEnabled()) {
                log.debug("The alias: " + alias + " does not have any private key");
            }
            throw handleClientException(ERROR_CODE_NO_PRIVATE_KEY_FOR_THE_GIVEN_ALIAS, alias);
        }
        try {
            keyStore = keyStoreAdmin.getKeyStore(keystoreName);
        } catch (Exception e) {
            throw handleServerException(ERROR_CODE_DELETE_CERTIFICATE, alias, e);
        }
        if (!isAnotherKeyExists(keyStore, alias)) {
            // Can't delete if this is the only private key in the keystore.
            if (log.isDebugEnabled()) {
                log.debug("Can't delete the private key with the alias: " + alias + " as there are no " + "any " +
                        "other private keys exists in the keystore");
            }
            throw handleClientException(ERROR_CODE_ONLY_ONE_PRIVATE_KEY_EXISTS, alias);
        }
        if (isSigningKey(alias, tenantDomain)) {
            //Can't delete if this keys is used as signing key in the keystore
            throw handleClientException(ERROR_CODE_UNSUPPORTED_DELETION_OF_SIGNING_KEY, alias);
        }
        try {
            keyStoreAdmin.removeCertFromStore(alias, keystoreName);
        } catch (SecurityConfigException e) {
            throw handleServerException(ERROR_CODE_DELETE_CERTIFICATE, alias, e);
        }
    }

    /**
     * Check whether the key alias sent for deletion is the key configured as signing key alias in the tenant.
     *
     * @param alias  Alias of the key.
     * @param tenant Tenant domain.
     * @return True if the key is configured as signing key alias.
     * @throws KeyStoreManagementServerException Throws KeyStoreManagementServerException.
     */
    private boolean isSigningKey(String alias, String tenant) throws KeyStoreManagementServerException {
        try {
            return KeyStoreMgtUtil.isSigningKeyAlias(alias, tenant);
        } catch (IdentityProviderManagementException e) {
            throw handleServerException(ERROR_CODE_DELETE_CERTIFICATE, alias, e);
        }
    }

    private Certificate[] extractCertificateChain(String certificateChain) throws CertificateException {

        Matcher m = pattern.matcher(certificateChain);
        List<String> tokens = new LinkedList<String>();
        while(m.find())
        {
            String token = m.group( 0 ); //group 0 is always the entire match
            tokens.add(token);
        }
        List<Certificate> certificateList = new ArrayList<>();
        for(String certificate: tokens) {
            Certificate cert = IdentityUtil.convertPEMEncodedContentToCertificate(certificate);
            certificateList.add(cert);
        }
        Certificate[] certificatesArray = new Certificate[certificateList.size()];
        return certificateList.toArray(certificatesArray);
    }

    private List<String> getPrivateKeyAliasList(KeyStore keyStore) throws Exception {

        List<String> privateKeyAliasList = new ArrayList<>();
        Enumeration<String> aliases = keyStore.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            if (keyStore.isKeyEntry(alias)) {
                privateKeyAliasList.add(alias);
            }
        }
        return privateKeyAliasList;
    }

    private boolean isPrivateKey(KeyStoreAdmin keyStoreAdmin, String keystoreName, String alias) throws
            KeyStoreManagementServerException {
        try {
            KeyStore keyStore = keyStoreAdmin.getKeyStore(keystoreName);
            return keyStore.isKeyEntry(alias);
        } catch (Exception e) {
            throw handleServerException(ERROR_CODE_DELETE_CERTIFICATE, alias, e);
        }
    }

    private boolean isAnotherKeyExists(KeyStore keyStore, String alias) throws KeyStoreManagementServerException {
        boolean anotherPrivateKeyExists = false;
        Enumeration<String> aliases;
        try {
            aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String someAlias = aliases.nextElement();
                if (!someAlias.equals(alias) && keyStore.isKeyEntry(someAlias)) {
                    anotherPrivateKeyExists = true;
                }
            }
        } catch (KeyStoreException e) {
            throw handleServerException(ERROR_CODE_DELETE_CERTIFICATE, alias, e);
        }
        return anotherPrivateKeyExists;
    }

    private KeyData getPrivateKeyInfo(String alias, KeyStore keyStore) throws Exception {

        Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
        X509Certificate cert = (X509Certificate) keyStore.getCertificate(alias);
        return fillCertData(alias, cert, formatter);
    }

    private KeyData fillCertData(String alias, X509Certificate cert, Format formatter) throws Exception {

        KeyData keyData = new KeyData();
        keyData.setAlias(alias);
        keyData.setSubjectDN(cert.getSubjectDN().getName());
        keyData.setIssuerDN(cert.getIssuerDN().getName());
        keyData.setSerialNumber(cert.getSerialNumber());
        keyData.setVersion(cert.getVersion());
        keyData.setNotAfter(formatter.format(cert.getNotAfter()));
        keyData.setNotBefore(formatter.format(cert.getNotBefore()));
        keyData.setPublicKey(Base64.encode(cert.getPublicKey().getEncoded()));
        keyData.setSignatureAlgName(cert.getSigAlgName());
        Map<String, String> fingerprints = new HashMap<>();
        for (String alg: SUPPORTED_FINGER_PRINTS_ALG){
            String fingerprint = KeyStoreMgtUtil.getCertFingerPrint(alg, cert);
            if(StringUtils.isNotBlank(fingerprint)){
                fingerprints.put(alg, fingerprint);
            }
        }
        keyData.setFingerprint(fingerprints);
        KeyStoreMgtUtil.getCertFingerPrint("SHA1", cert);
        return keyData;
    }

    private String getKeyStoreName(String tenantDomain) throws KeyStoreManagementException {

        KeyStoreData[] keyStoreDataArray = new KeyStoreData[0];
        try {
            keyStoreDataArray = getKeyStoreAdmin(tenantDomain).getKeyStores(isSuperTenant(tenantDomain));
        } catch (SecurityConfigException e) {
            throw handleServerException(ERROR_CODE_RETRIEVE_KEYSTORE, tenantDomain, e);
        }

        for (KeyStoreData keyStoreData : keyStoreDataArray) {
            if (keyStoreData == null) {
                break;
            }
            String keyStoreName = keyStoreData.getKeyStoreName();
            if (isSuperTenant(tenantDomain)) {
                if (KeyStoreUtil.isPrimaryStore(keyStoreName)) {
                    return keyStoreName;
                }
            } else {
                String tenantKeyStoreName = tenantDomain.trim().replace(".", "-") + ".jks";
                if (StringUtils.equals(keyStoreName, tenantKeyStoreName)) {
                    return keyStoreName;
                }
            }
        }
        throw handleServerException(ERROR_CODE_RETRIEVE_KEYSTORE, tenantDomain);
    }

    private KeyStoreData getKeystoreData(String tenantDomain, String keyStoreName) throws KeyStoreManagementException {

        KeyStoreAdmin keyStoreAdmin = getKeyStoreAdmin(tenantDomain);
        KeyStoreData keyStoreData = null;
        keyStoreAdmin.setIncludeCert(true);
        try {
            keyStoreData = keyStoreAdmin.getKeystoreInfo(keyStoreName);
        } catch (SecurityConfigException e) {
            throw handleServerException(ERROR_CODE_RETRIEVE_KEYSTORE_INFORMATION, keyStoreName, e);
        }
        return keyStoreData;
    }

    private List<String> getAliasList(KeyStoreData keyStoreData) {

        List<String> aliasList = new ArrayList<>();
        CertData key = keyStoreData.getKey();
        if (key != null && key.getAlias() != null) {
            aliasList.add(key.getAlias());
        }

        CertData[] certDataArray = keyStoreData.getCerts();
        for (CertData certData : certDataArray) {
            String alias = certData.getAlias();
            if (alias != null) {
                aliasList.add(alias);
            }
        }
        return aliasList;
    }

    private List<String> filterAlias(List<String> aliases, String filter) throws KeyStoreManagementException {

        if (filter != null) {
            filter = filter.replace(" ", "+");
            String[] extractedFilter = filter.split("[+]");
            if (extractedFilter.length == 3) {
                if (StringUtils.equals(extractedFilter[0], FILTER_FIELD_ALIAS)) {
                    String operation = extractedFilter[1];
                    String value = extractedFilter[2];
                    if (StringUtils.equals(operation, FILTER_OPERATION_EQUALS)) {
                        aliases = aliases.stream().filter(alias -> alias.matches(value))
                                .collect(Collectors.toList());
                    } else if (StringUtils.equals(operation, FILTER_OPERATION_STARTS_WITH)) {
                        aliases = aliases.stream().filter(alias -> alias.startsWith(value))
                                .collect(Collectors.toList());
                    } else if (StringUtils.equals(operation, FILTER_OPERATION_ENDS_WITH)) {
                        aliases = aliases.stream().filter(alias -> alias.endsWith(value))
                                .collect(Collectors.toList());
                    } else if (StringUtils.equals(operation, FILTER_OPERATION_CONTAINS)) {
                        aliases = aliases.stream().filter(alias -> alias.contains(value))
                                .collect(Collectors.toList());
                    } else {
                        throw handleClientException(ERROR_CODE_UNSUPPORTED_FILTER_OPERATION, operation);
                    }
                }
            } else {
                throw handleClientException(ERROR_CODE_BAD_VALUE_FOR_FILTER, filter);
            }
        }
        return aliases;
    }

    private KeyStoreAdmin getKeyStoreAdmin(String tenantDomain) {

        return new KeyStoreAdmin(IdentityTenantUtil.getTenantId(tenantDomain),
                (Registry) CarbonContext.getThreadLocalCarbonContext().getRegistry(RegistryType.SYSTEM_GOVERNANCE));
    }

    private boolean isSuperTenant(String tenantDomain) {

        return IdentityTenantUtil.getTenantId(tenantDomain) == MultitenantConstants.SUPER_TENANT_ID;
    }

    private String getTrustStoreName() {

        ServerConfiguration serverConfiguration = ServerConfiguration.getInstance();
        String filePath = serverConfiguration.getFirstProperty(SERVER_TRUSTSTORE_FILE);
        return Paths.get(filePath).getFileName().toString();
    }

    private KeyStoreManagementServerException handleServerException(
            SecurityConstants.KeyStoreMgtConstants.ErrorMessage error, String data) {

        String message = includeData(error, data);
        return new KeyStoreManagementServerException(error.getCode(), message);
    }

    private KeyStoreManagementServerException handleServerException(
            SecurityConstants.KeyStoreMgtConstants.ErrorMessage error, String data,
            Throwable e) {

        String message = includeData(error, data);
        return new KeyStoreManagementServerException(error.getCode(), message, e);
    }

    private KeyStoreManagementClientException handleClientException(
            SecurityConstants.KeyStoreMgtConstants.ErrorMessage error, String data) {

        String message = includeData(error, data);
        return new KeyStoreManagementClientException(error.getCode(), message);
    }

    private static String includeData(SecurityConstants.KeyStoreMgtConstants.ErrorMessage error, String data) {

        String message;
        if (StringUtils.isNotBlank(data)) {
            message = String.format(error.getMessage(), data);
        } else {
            message = error.getMessage();
        }
        return message;
    }

    /**
     * Update the keystore in the registry.
     *
     * @param keyStore     Keystore of the tenant
     * @param tenantDomain Tenant domain.
     * @param password     Password of the keystore
     * @throws KeyStoreManagementServerException Throws KeyStoreManagementServerException.
     */
    private void updateKeyStore(KeyStore keyStore, String tenantDomain, char[] password) throws
            KeyStoreManagementServerException {

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            keyStore.store(outputStream, password);
            outputStream.flush();
            outputStream.close();
            String keyStoreName = getKeyStoreName(tenantDomain);
            KeyStoreManager keyStoreManager = getKeyStoreManager(tenantDomain);
            keyStoreManager.updateKeyStore(keyStoreName, keyStore);
        } catch (Exception e) {
            throw handleServerException(ERROR_CODE_UPDATE_KEYSTORE, tenantDomain, e);
        }
    }

    private KeyStoreManager getKeyStoreManager(String tenantDomain) {

        int tenantId = getTenantId(tenantDomain);
        return KeyStoreManager.getInstance(tenantId);
    }

    private int getTenantId(String tenantDomain){
        return IdentityTenantUtil.getTenantId(tenantDomain);
    }
}
