/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.security.keystore;

import org.apache.axiom.om.util.Base64;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.core.RegistryResources;
import org.wso2.carbon.core.security.KeyStoreMetadata;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.core.util.KeyStoreUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.security.SecurityConfigException;
import org.wso2.carbon.security.SecurityConstants;
import org.wso2.carbon.security.keystore.service.CertData;
import org.wso2.carbon.security.keystore.service.CertDataDetail;
import org.wso2.carbon.security.keystore.service.KeyStoreData;
import org.wso2.carbon.security.keystore.service.PaginatedCertData;
import org.wso2.carbon.security.keystore.service.PaginatedKeyStoreData;
import org.wso2.carbon.security.util.KeyStoreMgtUtil;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.security.KeystoreUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

public class KeyStoreAdmin {

    private static final Log log = LogFactory.getLog(KeyStoreAdmin.class);
    private final KeyStoreManager keyStoreManager;
    private boolean includeCert = false;

    /**
     * Constructor to create a KeyStoreAdmin object.
     *
     * @param tenantId Tenant id.
     * @param registry Registry.
     * @deprecated Use {@link #KeyStoreAdmin(int)} instead.
     */
    @Deprecated
    public KeyStoreAdmin(int tenantId, Registry registry) {

        keyStoreManager = KeyStoreManager.getInstance(tenantId);
    }

    /**
     * Constructor to create a KeyStoreAdmin object.
     *
     * @param tenantId Tenant id.
     */
    public KeyStoreAdmin(int tenantId) {

        keyStoreManager = KeyStoreManager.getInstance(tenantId);
    }

    public boolean isIncludeCert() {
        return includeCert;
    }

    public void setIncludeCert(boolean includeCert) {
        this.includeCert = includeCert;
    }

    /**
     * Method to retrieve keystore data.
     *
     * @param isSuperTenant - Indication whether the querying super tenant data.
     * @return Array of KeyStoreData objects.
     * @throws SecurityConfigException If an error occurs while retrieving keystore data.
     */
    public KeyStoreData[] getKeyStores(boolean isSuperTenant) throws SecurityConfigException {

        CarbonUtils.checkSecurity();
        List<KeyStoreData> keyStoreDataList = new ArrayList<>();
        try {
            KeyStoreMetadata[] keyStoreMetadataArray = keyStoreManager.getKeyStoresMetadata(isSuperTenant);
            for (KeyStoreMetadata keyStoreMetadata : keyStoreMetadataArray) {
                KeyStoreData keyStoreData = new KeyStoreData();
                keyStoreData.setKeyStoreName(keyStoreMetadata.getKeyStoreName());
                keyStoreData.setKeyStoreType(keyStoreMetadata.getKeyStoreType());
                keyStoreData.setProvider(keyStoreMetadata.getProvider());
                keyStoreData.setPrivateStore(keyStoreMetadata.isPrivateStore());

                // Dump the generated public key to the file system for sub tenants.
                if (!isSuperTenant && keyStoreMetadata.getPublicCert() != null
                        && StringUtils.isNotBlank(keyStoreMetadata.getPublicCertId())
                        && MessageContext.getCurrentMessageContext() != null) {

                    String fileName = generatePubCertFileName(keyStoreMetadata.getKeyStoreName(),
                            keyStoreMetadata.getPublicCertId());
                    String pubKeyFilePath = KeyStoreMgtUtil.dumpCert(
                            MessageContext.getCurrentMessageContext().getConfigurationContext(),
                            keyStoreMetadata.getPublicCert(), fileName);
                    keyStoreData.setPubKeyFilePath(pubKeyFilePath);
                }
                keyStoreDataList.add(keyStoreData);
            }
            return keyStoreDataList.toArray(new KeyStoreData[0]);
        } catch (SecurityException e) {
            String msg = "Error when getting keyStore data";
            log.error(msg, e);
            throw new SecurityConfigException(msg, e);
        }
    }

    public void addKeyStoreWithFilePath(String filePath, String filename, String password,
                                        String provider, String type, String pvtkeyPass) throws SecurityConfigException {
        try {
            addKeyStore(readBytesFromFile(filePath), filename, password, provider, type, pvtkeyPass);
        } catch (IOException e) {
            throw new SecurityConfigException("Error while loading keystore from file " + filePath, e);
        }

    }

    public void addKeyStore(String fileData, String filename, String password, String provider,
                            String type, String pvtkeyPass) throws SecurityConfigException {
        byte[] content = Base64.decode(fileData);
        addKeyStore(content, filename, password, provider, type, pvtkeyPass);
    }

    public void addKeyStore(byte[] content, String filename, String password, String provider,
                            String type, String pvtkeyPass) throws SecurityConfigException {

        char[] passwordChar = new char[0];
        char[] privateKeyPasswordChar = new char[0];
        try {
            if (password == null) {
                throw new SecurityException("Key store password can't be null");
            }

            passwordChar = password.toCharArray();
            if (pvtkeyPass != null) {
                privateKeyPasswordChar = pvtkeyPass.toCharArray();
            }
            keyStoreManager.addKeyStore(content, filename, passwordChar, provider, type, privateKeyPasswordChar);
        } catch (SecurityException e) {
            String msg = "Error when adding a keyStore";
            log.error(msg, e);
            throw new SecurityConfigException(msg, e);
        } finally {
            Arrays.fill(passwordChar, '\0');
            Arrays.fill(privateKeyPasswordChar, '\0');
        }
    }

    public void addTrustStore(String fileData, String filename, String password, String provider,
                              String type) throws SecurityConfigException {
        byte[] content = Base64.decode(fileData);
        addTrustStore(content, filename, password, provider, type);
    }

    public void addTrustStore(byte[] content, String filename, String password, String provider, String type)
            throws SecurityConfigException {

        try {
            keyStoreManager.addKeyStore(content, filename, password.toCharArray(), provider, type, null);
        } catch (SecurityException e) {
            String msg = "Error when adding a trustStore";
            log.error(msg, e);
            throw new SecurityConfigException(msg, e);
        }
    }

    public void deleteStore(String keyStoreName) throws SecurityConfigException {

        try {
            keyStoreManager.deleteStore(keyStoreName);
        } catch (SecurityException e) {
            String msg = "Error when deleting a keyStore";
            log.error(msg, e);
            throw new SecurityConfigException(msg, e);
        }
    }

    public void importCertToStore(String fileName, String certData, String keyStoreName)
            throws SecurityConfigException {
        try {
            if (keyStoreName == null) {
                throw new SecurityConfigException("Key Store name can't be null");
            }

            KeyStore ks = this.keyStoreManager.getKeyStore(keyStoreName);
            X509Certificate cert = extractCertificate(certData);

            if (ks.getCertificateAlias(cert) != null) {
                // We already have this certificate in the key store - ignore
                // adding it twice
                return;
            }

            ks.setCertificateEntry(fileName, cert);

            this.keyStoreManager.updateKeyStore(keyStoreName, ks);

            if (KeyStoreUtil.isTrustStore(keyStoreName)) {
                System.setProperty(IdentityUtil.PROP_TRUST_STORE_UPDATE_REQUIRED, "true");
            }

        } catch (SecurityConfigException e) {
            throw e;
        } catch (Exception e) {
            String msg = "Error when importing cert to the keyStore";
            log.error(msg, e);
            throw new SecurityConfigException(msg, e);
        }

    }

    public String importCertToStore(String certData, String keyStoreName)
            throws SecurityConfigException {
        String alias = null;

        try {
            if (keyStoreName == null) {
                throw new SecurityConfigException("Key Store name can't be null");
            }

            KeyStore ks = this.keyStoreManager.getKeyStore(keyStoreName);
            X509Certificate cert = extractCertificate(certData);

            if (ks.getCertificateAlias(cert) != null) {
                // We already have this certificate in the key store - ignore
                // adding it twice
                return null;
            }
            alias = cert.getSubjectDN().getName();
            ks.setCertificateEntry(alias, cert);

            this.keyStoreManager.updateKeyStore(keyStoreName, ks);

            if (KeyStoreUtil.isTrustStore(keyStoreName)) {
                System.setProperty(IdentityUtil.PROP_TRUST_STORE_UPDATE_REQUIRED, "true");
            }

            return alias;

        } catch (SecurityConfigException e) {
            throw e;
        } catch (Exception e) {
            String msg = "Error when importing cert to keyStore";
            log.error(msg, e);
            throw new SecurityConfigException(msg);
        }
    }

    public void removeCertFromStore(String alias, String keyStoreName)
            throws SecurityConfigException {
        try {
            if (keyStoreName == null) {
                throw new SecurityConfigException("Key Store name can't be null");
            }

            KeyStore ks = this.keyStoreManager.getKeyStore(keyStoreName);

            if (ks.getCertificate(alias) == null) {
                return;
            }

            ks.deleteEntry(alias);
            this.keyStoreManager.updateKeyStore(keyStoreName, ks);

            if (KeyStoreUtil.isTrustStore(keyStoreName)) {
                System.setProperty(IdentityUtil.PROP_TRUST_STORE_UPDATE_REQUIRED, Boolean.TRUE.toString());
            }
        } catch (SecurityConfigException e) {
            throw e;
        } catch (Exception e) {
            String msg = "Error when removing cert from store";
            log.error(msg, e);
            throw new SecurityConfigException(msg);
        }
    }

    public String[] getStoreEntries(String keyStoreName) throws SecurityConfigException {
        String[] names;
        try {
            if (keyStoreName == null) {
                throw new Exception("keystore name cannot be null");
            }

            //KeyStoreManager keyMan = KeyStoreManager.getInstance(tenantId);
            KeyStore ks = this.keyStoreManager.getKeyStore(keyStoreName);

            Enumeration<String> enm = ks.aliases();
            List<String> lst = new ArrayList<>();
            while (enm.hasMoreElements()) {
                lst.add(enm.nextElement());
            }

            names = lst.toArray(new String[lst.size()]);
        } catch (SecurityConfigException e) {
            throw e;
        } catch (Exception e) {
            String msg = "Error when getting store entries";
            log.error(msg, e);
            throw new SecurityConfigException(msg);
        }

        return names;
    }

    /**
     * This method will list 1. Certificate aliases 2. Private key alise 3. Private key value to a
     * given keystore.
     *
     * @param keyStoreName The name of the keystore
     * @return Instance of KeyStoreData
     * @throws SecurityConfigException will be thrown
     */
    public KeyStoreData getKeystoreInfo(String keyStoreName) throws SecurityConfigException {

        try {
            if (keyStoreName == null) {
                throw new Exception("keystore name cannot be null");
            }

            KeyStore keyStore;
            String keyStoreType;
            ServerConfiguration serverConfig = ServerConfiguration.getInstance();
            if (KeyStoreUtil.isPrimaryStore(keyStoreName)) {
                keyStore = this.keyStoreManager.getPrimaryKeyStore();
                keyStoreType = serverConfig.getFirstProperty(
                        RegistryResources.SecurityManagement.SERVER_PRIMARY_KEYSTORE_TYPE);
            } else if (KeyStoreUtil.isTrustStore(keyStoreName)) {
                keyStore = this.keyStoreManager.getTrustStore();
                keyStoreType = serverConfig.getFirstProperty(
                        RegistryResources.SecurityManagement.SERVER_TRUSTSTORE_TYPE);
            } else {
                keyStore = this.keyStoreManager.getKeyStore(keyStoreName);
                keyStoreType = keyStore.getType();
            }
            // Fill the information about the certificates
            Enumeration<String> aliases = keyStore.aliases();
            List<org.wso2.carbon.security.keystore.service.CertData> certDataList = new ArrayList<>();
            Format formatter = new SimpleDateFormat("dd/MM/yyyy");

            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                if (keyStore.isCertificateEntry(alias)) {
                    X509Certificate cert = (X509Certificate) keyStore.getCertificate(alias);
                    certDataList.add(fillCertData(cert, alias, formatter));
                }
            }

            // Create a cert array
            CertData[] certs = certDataList.toArray(new CertData[certDataList.size()]);

            // Create a KeyStoreData bean, set the name and fill in the cert information
            KeyStoreData keyStoreData = new KeyStoreData();
            keyStoreData.setKeyStoreName(keyStoreName);
            keyStoreData.setCerts(certs);
            keyStoreData.setKeyStoreType(keyStoreType);

            aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                // There be only one entry in WSAS related keystores
                if (keyStore.isKeyEntry(alias)) {
                    X509Certificate cert = (X509Certificate) keyStore.getCertificate(alias);
                    keyStoreData.setKey(fillCertData(cert, alias, formatter));
                    PrivateKey key = (PrivateKey) this.keyStoreManager.getPrivateKey(keyStoreName, alias);
                    String pemKey;
                    pemKey = "-----BEGIN PRIVATE KEY-----\n";
                    pemKey += Base64.encode(key.getEncoded());
                    pemKey += "\n-----END PRIVATE KEY-----";
                    keyStoreData.setKeyValue(pemKey);
                    break;

                }
            }
            return keyStoreData;
        } catch (Exception e) {
            String msg = "Error has encounted while loading the keystore to the given keystore name "
                    + keyStoreName;
            log.error(msg, e);
            throw new SecurityConfigException(msg);
        }
    }

    public Key getPrivateKey(String alias, boolean isSuperTenant) throws SecurityConfigException {
        KeyStoreData[] keystores = getKeyStores(isSuperTenant);
        KeyStore keyStore = null;
        String privateKeyPassowrd = null;

        try {

            for (int i = 0; i < keystores.length; i++) {
                if (KeyStoreUtil.isPrimaryStore(keystores[i].getKeyStoreName())) {
                    keyStore = this.keyStoreManager.getPrimaryKeyStore();
                    ServerConfiguration serverConfig = ServerConfiguration.getInstance();
                    privateKeyPassowrd = serverConfig
                            .getFirstProperty(RegistryResources.SecurityManagement.SERVER_PRIVATE_KEY_PASSWORD);
                    return keyStore.getKey(alias, privateKeyPassowrd.toCharArray());
                }
            }
        } catch (Exception e) {
            String msg = "Error has encounted while loading the key for the given alias " + alias;
            log.error(msg, e);
            throw new SecurityConfigException(msg);
        }
        return null;
    }

    private CertData fillCertData(X509Certificate cert, String alise, Format formatter)
            throws CertificateEncodingException {
        CertData certData = null;

        if (includeCert) {
            certData = new CertDataDetail();
        } else {
            certData = new CertData();
        }
        certData.setAlias(alise);
        certData.setSubjectDN(cert.getSubjectDN().getName());
        certData.setIssuerDN(cert.getIssuerDN().getName());
        certData.setSerialNumber(cert.getSerialNumber());
        certData.setVersion(cert.getVersion());
        certData.setNotAfter(formatter.format(cert.getNotAfter()));
        certData.setNotBefore(formatter.format(cert.getNotBefore()));
        certData.setPublicKey(Base64.encode(cert.getPublicKey().getEncoded()));

        if (includeCert) {
            ((CertDataDetail) certData).setCertificate(cert);
        }

        return certData;
    }

    private byte[] readBytesFromFile(String filePath) throws IOException {
        InputStream inputStream = null;
        File file = new File(filePath);
        long length;
        byte[] bytes;
        int offset = 0;
        int numRead = 0;

        try {
            inputStream = new FileInputStream(file);
            length = file.length();
            bytes = new byte[(int) length];

            while (offset < bytes.length
                    && (numRead = inputStream.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }

        return bytes;
    }

    /**
     * This method is used to generate the file name of the public cert of a tenant.
     *
     * @param keyStoreName Keystore Name.
     * @param uuid         UUID appender.
     * @return file name of the public cert.
     */
    private String generatePubCertFileName(String keyStoreName, String uuid) {

        for (KeystoreUtils.StoreFileType fileType: KeystoreUtils.StoreFileType.values()) {
            String fileExtension = KeystoreUtils.StoreFileType.getExtension(fileType);
            if (keyStoreName.endsWith(fileExtension)) {
                keyStoreName = keyStoreName.replace(fileExtension, "");
            }
        }
        return keyStoreName + "-" + uuid + ".cert";
    }

    /**
     * This method is used internally to do the pagination purposes.
     *
     * @param pageNumber  page Number
     * @param certDataSet set of keyStoreData
     * @return PaginatedPolicySetDTO object containing the number of pages and the set of policies
     * that reside in the given page.
     */
    private PaginatedCertData doPaging(int pageNumber, CertData[] certDataSet) {

        PaginatedCertData paginatedCertData = new PaginatedCertData();
        if (certDataSet.length == 0) {
            paginatedCertData.setCertDataSet(new CertData[0]);
            return paginatedCertData;
        }
        int itemsPerPageInt = SecurityConstants.ITEMS_PER_PAGE;
        int numberOfPages = (int) Math.ceil((double) certDataSet.length / itemsPerPageInt);
        if (pageNumber > numberOfPages - 1) {
            pageNumber = numberOfPages - 1;
        }
        int startIndex = pageNumber * itemsPerPageInt;
        int endIndex = certDataSet.length;
        if (numberOfPages > SecurityConstants.CACHING_PAGE_SIZE) {
            endIndex = (pageNumber + SecurityConstants.CACHING_PAGE_SIZE) * itemsPerPageInt;
        }
        CertData[] returnedCertDataSet = new CertData[endIndex];

        for (int i = startIndex, j = 0; i < endIndex && i < certDataSet.length; i++, j++) {
            returnedCertDataSet[j] = certDataSet[i];
        }

        paginatedCertData.setCertDataSet(returnedCertDataSet);
        paginatedCertData.setNumberOfPages(numberOfPages);

        return paginatedCertData;
    }

    /**
     * This method is used internally for the filtering purposes.
     *
     * @param filter      Filter string.
     * @param certDataSet Certificate or key array.
     * @return Cert Data array after filtering.
     */
    private static CertData[] doFilter(String filter, CertData[] certDataSet) {

        if (certDataSet != null && certDataSet.length != 0) {
            String regPattern = filter.replace("*", ".*");
            List<CertData> certDataList = new ArrayList<CertData>();

            for (CertData cert : certDataSet) {
                if (cert != null && cert.getAlias().toLowerCase().matches(regPattern.toLowerCase())) {
                    certDataList.add(cert);
                }
            }

            return (CertData[]) certDataList.toArray(new CertData[0]);
        } else {
            return new CertData[0];
        }
    }

    /**
     * Gets the keystore info by keystore name with its certificates and key certificates.
     *
     * @param keyStoreName The name of the keystore
     * @param pageNumber   page number
     * @return Instance of KeyStoreData
     * @throws SecurityConfigException will be thrown
     */
    public PaginatedKeyStoreData getPaginatedKeystoreInfo(String keyStoreName, int pageNumber)
            throws SecurityConfigException {

        if (StringUtils.isEmpty(keyStoreName)) {
            throw new SecurityConfigException("Keystore name cannot be empty or null.");
        }

        try {
            // Get keystore.
            KeyStore keyStore = this.keyStoreManager.getKeyStore(keyStoreName);
            // Get keystore type.
            String keyStoreType = keyStore.getType();

            // Extract certificates from aliases as list.
            List<CertData> certDataList = getCertificates(keyStore);
            List<CertData> keyCertDataList = getKeyCertificates(keyStore);

            // Create a certificate array.
            CertData[] certs = certDataList.toArray(new CertData[certDataList.size()]);
            // Get paginated certificates.
            PaginatedCertData paginatedCerts = doPaging(pageNumber, certs);

            // Create a key certificate array.
            CertData[] keyCerts = keyCertDataList.toArray(new CertData[keyCertDataList.size()]);
            // Get paginated key certificates.
            PaginatedCertData paginatedKeyCerts = doPaging(pageNumber, keyCerts);

            // Fill information about the keystore to PaginatedKeyStoreData.
            PaginatedKeyStoreData keyStoreData = fillPaginatedKeyStoreData(keyStoreName, keyStoreType,
                    paginatedCerts, paginatedKeyCerts);

            return keyStoreData;
        } catch (Exception e) {
            throw new SecurityConfigException(e.getMessage());
        }

    }

    /**
     * Gets the keystore info by keystore name and filters its certificates and key certificates
     * by applying the filter for certificate aliases.
     *
     * @param keyStoreName The name of the keystore.
     * @param pageNumber   Page number.
     * @param filter       Filter for certificate alias.
     * @return Instance of KeyStoreData.
     * @throws SecurityConfigException will be thrown.
     */
    public PaginatedKeyStoreData getFilteredPaginatedKeyStoreInfo(String keyStoreName, int pageNumber,
                                                                  String filter) throws SecurityConfigException {

        if (StringUtils.isEmpty(keyStoreName)) {
            throw new SecurityConfigException("Keystore name cannot be empty or null.");
        }

        try {
            // Get keystore.
            KeyStore keyStore = this.keyStoreManager.getKeyStore(keyStoreName);
            // Get keystore type.
            String keyStoreType = keyStore.getType();

            // Extract certificates from aliases as list.
            List<CertData> certDataList = getCertificates(keyStore);
            List<CertData> keyCertDataList = getKeyCertificates(keyStore);
            // Filter and paginate certs and keyCerts.
            PaginatedCertData paginatedCerts = filterAndPaginateCerts(certDataList, filter, pageNumber);
            PaginatedCertData paginatedKeyCerts = filterAndPaginateCerts(keyCertDataList, filter, pageNumber);
            // Fill information about the keystore to PaginatedKeyStoreData.
            PaginatedKeyStoreData keyStoreData = fillPaginatedKeyStoreData(keyStoreName, keyStoreType,
                    paginatedCerts, paginatedKeyCerts);

            return keyStoreData;
        } catch (Exception e) {
            throw new SecurityConfigException(e.getMessage());
        }
    }

    /**
     * Fill PaginatedKeyStoreData with keystore details.
     *
     * @param keyStoreName Name of the keystore.
     * @param keyStoreType Type of the keystore.
     * @param certs        Paginated certificates.
     * @param keyCerts     Paginated key certificates.
     * @return Paginated KeyStore Data.
     */
    private PaginatedKeyStoreData fillPaginatedKeyStoreData(String keyStoreName, String keyStoreType,
                                                            PaginatedCertData certs, PaginatedCertData keyCerts) {

        // Create a KeyStoreData bean, set the name, type and fill in the cert information.
        PaginatedKeyStoreData keyStoreData = new PaginatedKeyStoreData();
        keyStoreData.setKeyStoreName(keyStoreName);
        keyStoreData.setKeyStoreType(keyStoreType);
        keyStoreData.setPaginatedCertData(certs);
        keyStoreData.setPaginatedKeyData(keyCerts);
        return keyStoreData;
    }

    /**
     * Get certificates related to alias from the keystore.
     *
     * @param keyStore Keystore
     * @return List of certificate data.
     * @throws KeyStoreException
     * @throws CertificateEncodingException
     */
    private List<CertData> getCertificates(KeyStore keyStore)
            throws KeyStoreException, CertificateEncodingException {

        Enumeration<String> aliases = keyStore.aliases();
        // Create lists for cert and key lists.
        List<CertData> certDataList = new ArrayList<>();
        Format formatter = new SimpleDateFormat("dd/MM/yyyy");

        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            X509Certificate cert = (X509Certificate) keyStore.getCertificate(alias);
            if (keyStore.isCertificateEntry(alias)) {
                certDataList.add(fillCertData(cert, alias, formatter));
            }
        }
        return certDataList;
    }

    /**
     * Get key certificates related to alias from the keystore.
     *
     * @param keyStore Keystore
     * @return List of certificate data.
     * @throws KeyStoreException
     * @throws CertificateEncodingException
     */
    private List<CertData> getKeyCertificates(KeyStore keyStore)
            throws KeyStoreException, CertificateEncodingException {

        Enumeration<String> aliases = keyStore.aliases();
        // Create lists for cert and key lists.
        List<CertData> certDataList = new ArrayList<>();
        Format formatter = new SimpleDateFormat("dd/MM/yyyy");

        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            X509Certificate cert = (X509Certificate) keyStore.getCertificate(alias);
            if (keyStore.isKeyEntry(alias)) {
                certDataList.add(fillCertData(cert, alias, formatter));
            }
        }
        return certDataList;
    }

    /**
     * Filter and paginate certificate list.
     *
     * @param certDataList Certificate list.
     * @param filterString Filter text.
     * @param pageNumber   Page number.
     * @return Paginated and Filtered Certificate Data.
     */
    private PaginatedCertData filterAndPaginateCerts(List<CertData> certDataList, String filterString, int pageNumber) {

        PaginatedCertData paginatedCerts;
        CertData[] certs = certDataList.toArray(new CertData[0]);
        certs = (doFilter(filterString, certs));
        paginatedCerts = doPaging(pageNumber, certs);
        return paginatedCerts;
    }

    /**
     * Load the default trust store (allowed only for super tenant).
     *
     * @return trust store object
     * @throws SecurityConfigException if retrieving the truststore fails.
     */
    public KeyStore getTrustStore() throws SecurityConfigException {
    
        try {
            return this.keyStoreManager.getTrustStore();
        } catch (CarbonException | SecurityException e) {
            throw new SecurityConfigException("Error occurred while loading keystore.", e);
        }
    }

    /**
     * Retrieves the {@link KeyStore} object of the given keystore name.
     *
     * @param keyStoreName name of the keystore.
     * @return {@link KeyStore} object.
     * @throws Exception if retrieving the keystore fails.
     */
    public KeyStore getKeyStore(String keyStoreName) throws Exception {

        return this.keyStoreManager.getKeyStore(keyStoreName);
    }

    /**
     * Extract the encoded certificate into {@link X509Certificate}.
     *
     * @param certData encoded certificate.
     * @return {@link X509Certificate} object.
     * @throws SecurityConfigException if extracting the certificate fails.
     */
    public X509Certificate extractCertificate(String certData) throws SecurityConfigException {

        byte[] bytes = Base64.decode(certData);
        X509Certificate cert;
        try {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            cert = (X509Certificate) factory
                    .generateCertificate(new ByteArrayInputStream(bytes));
        } catch (CertificateException e) {
            if (log.isDebugEnabled()) {
                log.debug(e.getMessage(), e);
            }
            throw new SecurityConfigException("Invalid format of the provided certificate file");
        }
        return cert;
    }
}
