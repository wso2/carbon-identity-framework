/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.application.common.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.Base64;
import org.apache.axiom.util.base64.Base64Utils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.CertData;
import org.wso2.carbon.identity.application.common.model.CertificateInfo;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProviderProperty;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.ProvisioningConnectorConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.ThreadLocalProvisioningServiceProvider;
import org.wso2.carbon.identity.base.IdentityConstants;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.namespace.QName;

/**
 * Utility functionality for application management.
 */
public class IdentityApplicationManagementUtil {

    private IdentityApplicationManagementUtil() {

    }

    private static final Log log = LogFactory.getLog(IdentityApplicationManagementUtil.class);
    private static ThreadLocal<ThreadLocalProvisioningServiceProvider> threadLocalProvisioningServiceProvider =
            new ThreadLocal<>();
    
    private static final Map<String, String> xmlSignatureAlgorithms;
    private static final Map<String, String> xmlDigestAlgorithms;
    private static final Map<String, String> xmlAssertionEncryptionAlgorithms;
    private static final Map<String, String> xmlKeyEncryptionAlgorithms;
    private static final Map<String, String> samlAuthnContextClasses;
    private static final List<String> samlAuthnContextComparisonLevels;
    private static HashMap<CertData, String> certificalteValMap = new HashMap<>();
    private static final int MODE_DEFAULT = 1;
    private static final int MODE_ESCAPE = 2;
    private static final int MODE_STRING = 3;
    private static final int MODE_SINGLE_LINE = 4;
    private static final int MODE_MULTI_LINE = 5;
    private static final Pattern JS_LOOP_PATTERN = Pattern.compile("\\b(for|while|forEach)\\b");

    private static ThreadLocal<Boolean> allowUpdateSystemApplicationThreadLocal = new ThreadLocal<>();

    static {
        //initialize xmlSignatureAlgorithms
        Map<String, String> xmlSignatureAlgorithmMap = new LinkedHashMap<>();
        xmlSignatureAlgorithmMap.put(IdentityApplicationConstants.XML.SignatureAlgorithm.DSA_SHA1,
                IdentityApplicationConstants.XML.SignatureAlgorithmURI.DSA_SHA1);
        xmlSignatureAlgorithmMap.put(
                IdentityApplicationConstants.XML.SignatureAlgorithm.ECDSA_SHA1,
                IdentityApplicationConstants.XML.SignatureAlgorithmURI.ECDSA_SHA1);
        xmlSignatureAlgorithmMap.put(
                IdentityApplicationConstants.XML.SignatureAlgorithm.ECDSA_SHA256,
                IdentityApplicationConstants.XML.SignatureAlgorithmURI.ECDSA_SHA256);
        xmlSignatureAlgorithmMap.put(
                IdentityApplicationConstants.XML.SignatureAlgorithm.ECDSA_SHA384,
                IdentityApplicationConstants.XML.SignatureAlgorithmURI.ECDSA_SHA384);
        xmlSignatureAlgorithmMap.put(
                IdentityApplicationConstants.XML.SignatureAlgorithm.ECDSA_SHA512,
                IdentityApplicationConstants.XML.SignatureAlgorithmURI.ECDSA_SHA512);
        xmlSignatureAlgorithmMap.put(IdentityApplicationConstants.XML.SignatureAlgorithm.RSA_MD5,
                IdentityApplicationConstants.XML.SignatureAlgorithmURI.RSA_MD5);
        xmlSignatureAlgorithmMap.put(
                IdentityApplicationConstants.XML.SignatureAlgorithm.RSA_RIPEMD160,
                IdentityApplicationConstants.XML.SignatureAlgorithmURI.RSA_RIPEMD160);
        xmlSignatureAlgorithmMap.put(IdentityApplicationConstants.XML.SignatureAlgorithm.RSA_SHA1,
                IdentityApplicationConstants.XML.SignatureAlgorithmURI.RSA_SHA1);
        xmlSignatureAlgorithmMap.put(
                IdentityApplicationConstants.XML.SignatureAlgorithm.RSA_SHA256,
                IdentityApplicationConstants.XML.SignatureAlgorithmURI.RSA_SHA256);
        xmlSignatureAlgorithmMap.put(
                IdentityApplicationConstants.XML.SignatureAlgorithm.RSA_SHA384,
                IdentityApplicationConstants.XML.SignatureAlgorithmURI.RSA_SHA384);
        xmlSignatureAlgorithmMap.put(
                IdentityApplicationConstants.XML.SignatureAlgorithm.RSA_SHA512,
                IdentityApplicationConstants.XML.SignatureAlgorithmURI.RSA_SHA512);
        xmlSignatureAlgorithms = Collections.unmodifiableMap(xmlSignatureAlgorithmMap);

        //initialize xmlDigestAlgorithms
        Map<String, String> xmlDigestAlgorithmMap = new LinkedHashMap<>();
        xmlDigestAlgorithmMap.put(IdentityApplicationConstants.XML.DigestAlgorithm.MD5,
                IdentityApplicationConstants.XML.DigestAlgorithmURI.MD5);
        xmlDigestAlgorithmMap.put(IdentityApplicationConstants.XML.DigestAlgorithm.RIPEMD160,
                IdentityApplicationConstants.XML.DigestAlgorithmURI.RIPEMD160);
        xmlDigestAlgorithmMap.put(IdentityApplicationConstants.XML.DigestAlgorithm.SHA1,
                IdentityApplicationConstants.XML.DigestAlgorithmURI.SHA1);
        xmlDigestAlgorithmMap.put(IdentityApplicationConstants.XML.DigestAlgorithm.SHA256,
                IdentityApplicationConstants.XML.DigestAlgorithmURI.SHA256);
        xmlDigestAlgorithmMap.put(IdentityApplicationConstants.XML.DigestAlgorithm.SHA384,
                IdentityApplicationConstants.XML.DigestAlgorithmURI.SHA384);
        xmlDigestAlgorithmMap.put(IdentityApplicationConstants.XML.DigestAlgorithm.SHA512,
                IdentityApplicationConstants.XML.DigestAlgorithmURI.SHA512);
        xmlDigestAlgorithms = Collections.unmodifiableMap(xmlDigestAlgorithmMap);

        // initialize xmlAssertionEncryptionAlgorithms
        Map<String, String> xmlAssertionEncryptionAlgorithmMap = new LinkedHashMap<>();
        xmlAssertionEncryptionAlgorithmMap.put(IdentityApplicationConstants.XML.AssertionEncryptionAlgorithm.AES256,
                IdentityApplicationConstants.XML.AssertionEncryptionAlgorithmURI.AES256);
        xmlAssertionEncryptionAlgorithmMap.put(IdentityApplicationConstants.XML.AssertionEncryptionAlgorithm.AES192,
                IdentityApplicationConstants.XML.AssertionEncryptionAlgorithmURI.AES192);
        xmlAssertionEncryptionAlgorithmMap.put(IdentityApplicationConstants.XML.AssertionEncryptionAlgorithm.AES128,
                IdentityApplicationConstants.XML.AssertionEncryptionAlgorithmURI.AES128);
        xmlAssertionEncryptionAlgorithmMap.put(IdentityApplicationConstants.XML.AssertionEncryptionAlgorithm.TRIPLEDES,
                IdentityApplicationConstants.XML.AssertionEncryptionAlgorithmURI.TRIPLEDES);
        xmlAssertionEncryptionAlgorithmMap.put(IdentityApplicationConstants.XML.AssertionEncryptionAlgorithm.AES128_GCM,
                IdentityApplicationConstants.XML.AssertionEncryptionAlgorithmURI.AES128_GCM);
        xmlAssertionEncryptionAlgorithmMap.put(IdentityApplicationConstants.XML.AssertionEncryptionAlgorithm.AES192_GCM,
                IdentityApplicationConstants.XML.AssertionEncryptionAlgorithmURI.AES192_GCM);
        xmlAssertionEncryptionAlgorithmMap.put(IdentityApplicationConstants.XML.AssertionEncryptionAlgorithm.AES256_GCM,
                IdentityApplicationConstants.XML.AssertionEncryptionAlgorithmURI.AES256_GCM);
        xmlAssertionEncryptionAlgorithms = Collections.unmodifiableMap(xmlAssertionEncryptionAlgorithmMap);

        // initialize xmlKeyEncryptionAlgorithms
        Map<String, String> xmlKeyEncryptionAlgorithmMap = new LinkedHashMap<>();
        xmlKeyEncryptionAlgorithmMap.put(IdentityApplicationConstants.XML.KeyEncryptionAlgorithm.RSAOAEP,
                IdentityApplicationConstants.XML.KeyEncryptionAlgorithmURI.RSAOAEP);
        xmlKeyEncryptionAlgorithmMap.put(IdentityApplicationConstants.XML.KeyEncryptionAlgorithm.RSA15,
                IdentityApplicationConstants.XML.KeyEncryptionAlgorithmURI.RSA15);
        xmlKeyEncryptionAlgorithmMap.put(IdentityApplicationConstants.XML.KeyEncryptionAlgorithm.RSAOAEP11,
                IdentityApplicationConstants.XML.KeyEncryptionAlgorithmURI.RSAOAEP11);
        xmlKeyEncryptionAlgorithms = Collections.unmodifiableMap(xmlKeyEncryptionAlgorithmMap);
        
        //initialize samlAuthnContextClasses
        Map<String, String> samlAuthnContextClassMap = new LinkedHashMap<>();
        samlAuthnContextClassMap.put(IdentityApplicationConstants.SAML2.AuthnContextClass.IP,
                IdentityApplicationConstants.SAML2.AuthnContextClassURI.IP);
        samlAuthnContextClassMap.put(
                IdentityApplicationConstants.SAML2.AuthnContextClass.IP_PASSWORD,
                IdentityApplicationConstants.SAML2.AuthnContextClassURI.IP_PASSWORD);
        samlAuthnContextClassMap.put(
                IdentityApplicationConstants.SAML2.AuthnContextClass.KERBEROS,
                IdentityApplicationConstants.SAML2.AuthnContextClassURI.KERBEROS);
        samlAuthnContextClassMap
                .put(IdentityApplicationConstants.SAML2.AuthnContextClass.MOBILE_ONE_FACTOR_UNREGISTERED,
                        IdentityApplicationConstants.SAML2.AuthnContextClassURI.MOBILE_ONE_FACTOR_UNREGISTERED);
        samlAuthnContextClassMap
                .put(IdentityApplicationConstants.SAML2.AuthnContextClass.MOBILE_TWO_FACTOR_UNREGISTERED,
                        IdentityApplicationConstants.SAML2.AuthnContextClassURI.MOBILE_TWO_FACTOR_UNREGISTERED);
        samlAuthnContextClassMap.put(
                IdentityApplicationConstants.SAML2.AuthnContextClass.MOBILE_ONE_FACTOR_CONTRACT,
                IdentityApplicationConstants.SAML2.AuthnContextClassURI.MOBILE_ONE_FACTOR_CONTRACT);
        samlAuthnContextClassMap.put(
                IdentityApplicationConstants.SAML2.AuthnContextClass.MOBILE_TWO_FACTOR_CONTRACT,
                IdentityApplicationConstants.SAML2.AuthnContextClassURI.MOBILE_TWO_FACTOR_CONTRACT);
        samlAuthnContextClassMap.put(
                IdentityApplicationConstants.SAML2.AuthnContextClass.PASSWORD,
                IdentityApplicationConstants.SAML2.AuthnContextClassURI.PASSWORD);
        samlAuthnContextClassMap
                .put(IdentityApplicationConstants.SAML2.AuthnContextClass.PASSWORD_PROTECTED_TRANSPORT,
                        IdentityApplicationConstants.SAML2.AuthnContextClassURI.PASSWORD_PROTECTED_TRANSPORT);
        samlAuthnContextClassMap.put(
                IdentityApplicationConstants.SAML2.AuthnContextClass.PREVIOUS_SESSION,
                IdentityApplicationConstants.SAML2.AuthnContextClassURI.PREVIOUS_SESSION);
        samlAuthnContextClassMap.put(IdentityApplicationConstants.SAML2.AuthnContextClass.X509,
                IdentityApplicationConstants.SAML2.AuthnContextClassURI.X509);
        samlAuthnContextClassMap.put(IdentityApplicationConstants.SAML2.AuthnContextClass.PGP,
                IdentityApplicationConstants.SAML2.AuthnContextClassURI.PGP);
        samlAuthnContextClassMap.put(IdentityApplicationConstants.SAML2.AuthnContextClass.SPKI,
                IdentityApplicationConstants.SAML2.AuthnContextClassURI.SPKI);
        samlAuthnContextClassMap.put(
                IdentityApplicationConstants.SAML2.AuthnContextClass.XML_DSIG,
                IdentityApplicationConstants.SAML2.AuthnContextClassURI.XML_DSIG);
        samlAuthnContextClassMap.put(
                IdentityApplicationConstants.SAML2.AuthnContextClass.SMARTCARD,
                IdentityApplicationConstants.SAML2.AuthnContextClassURI.SMARTCARD);
        samlAuthnContextClassMap.put(
                IdentityApplicationConstants.SAML2.AuthnContextClass.SMARTCARD_PKI,
                IdentityApplicationConstants.SAML2.AuthnContextClassURI.SMARTCARD_PKI);
        samlAuthnContextClassMap.put(
                IdentityApplicationConstants.SAML2.AuthnContextClass.SOFTWARE_PKI,
                IdentityApplicationConstants.SAML2.AuthnContextClassURI.SOFTWARE_PKI);
        samlAuthnContextClassMap.put(
                IdentityApplicationConstants.SAML2.AuthnContextClass.TELEPHONY,
                IdentityApplicationConstants.SAML2.AuthnContextClassURI.TELEPHONY);
        samlAuthnContextClassMap.put(
                IdentityApplicationConstants.SAML2.AuthnContextClass.NOMAD_TELEPHONY,
                IdentityApplicationConstants.SAML2.AuthnContextClassURI.NOMAD_TELEPHONY);
        samlAuthnContextClassMap.put(
                IdentityApplicationConstants.SAML2.AuthnContextClass.PERSONAL_TELEPHONY,
                IdentityApplicationConstants.SAML2.AuthnContextClassURI.PERSONAL_TELEPHONY);
        samlAuthnContextClassMap.put(
                IdentityApplicationConstants.SAML2.AuthnContextClass.AUTHENTICATED_TELEPHONY,
                IdentityApplicationConstants.SAML2.AuthnContextClassURI.AUTHENTICATED_TELEPHONY);
        samlAuthnContextClassMap.put(
                IdentityApplicationConstants.SAML2.AuthnContextClass.SECURE_REMOTE_PASSWORD,
                IdentityApplicationConstants.SAML2.AuthnContextClassURI.SECURE_REMOTE_PASSWORD);
        samlAuthnContextClassMap.put(
                IdentityApplicationConstants.SAML2.AuthnContextClass.TLS_CLIENT,
                IdentityApplicationConstants.SAML2.AuthnContextClassURI.TLS_CLIENT);
        samlAuthnContextClassMap.put(
                IdentityApplicationConstants.SAML2.AuthnContextClass.TIME_SYNC_TOKEN,
                IdentityApplicationConstants.SAML2.AuthnContextClassURI.TIME_SYNC_TOKEN);
        samlAuthnContextClassMap.put(
                IdentityApplicationConstants.SAML2.AuthnContextClass.UNSPECIFIED,
                IdentityApplicationConstants.SAML2.AuthnContextClassURI.UNSPECIFIED);
        samlAuthnContextClasses = Collections.unmodifiableMap(samlAuthnContextClassMap);
        
        ////initialize samlAuthnContextComparisonLevels map
        List<String> samlAuthnContextComparisonLevelList = new ArrayList<>();
        samlAuthnContextComparisonLevelList.add(IdentityApplicationConstants.SAML2.AuthnContextComparison.EXACT);
        samlAuthnContextComparisonLevelList.add(IdentityApplicationConstants.SAML2.AuthnContextComparison.MINIMUM);
        samlAuthnContextComparisonLevelList.add(IdentityApplicationConstants.SAML2.AuthnContextComparison.MAXIMUM);
        samlAuthnContextComparisonLevelList.add(IdentityApplicationConstants.SAML2.AuthnContextComparison.BETTER);
        samlAuthnContextComparisonLevels = Collections.unmodifiableList(samlAuthnContextComparisonLevelList);
    }

    /**
     *
     */
    public static void resetThreadLocalProvisioningServiceProvider() {
        threadLocalProvisioningServiceProvider.remove();
    }

    /**
     *
     * @return
     */
    public static ThreadLocalProvisioningServiceProvider getThreadLocalProvisioningServiceProvider() {
        return threadLocalProvisioningServiceProvider.get();
    }

    /**
     * @param serviceProvider In-bound - or Just-in-Time provisioning service provider.
     */
    public static void setThreadLocalProvisioningServiceProvider(
            ThreadLocalProvisioningServiceProvider serviceProvider) {
        threadLocalProvisioningServiceProvider.set(serviceProvider);
    }

    /**
     * Validates an URI.
     *
     * @param uriString URI String
     * @return <code>true</code> if valid URI, <code>false</code> otherwise
     */
    public static boolean validateURI(String uriString) {

        if (uriString != null) {
            try {
                URL url = new URL(uriString);
            } catch (MalformedURLException e) {
                log.debug(e.getMessage(), e);
                return false;
            }
        } else {
            String errorMsg = "Invalid URL: \'NULL\'";
            log.debug(errorMsg);
            return false;
        }
        return true;
    }

    /**
     * Utility method to close a database connection
     *
     * @param dbConnection Database <code>Connection</code> object
     */
    public static void closeConnection(Connection dbConnection) {

        IdentityDatabaseUtil.closeConnection(dbConnection);
    }

    /**
     * Utility method to rollback a database connection
     *
     * @param dbConnection Database <code>Connection</code> object
     * @deprecated Please use IdentityDatabaseUtil.rollbackTransaction(Connection dbConnection) instead.
     */
    @Deprecated
    public static void rollBack(Connection dbConnection) {

        IdentityDatabaseUtil.rollBack(dbConnection);
    }

    /**
     * @param o1
     * @param o2
     * @return
     */
    public static ProvisioningConnectorConfig[] concatArrays(ProvisioningConnectorConfig[] o1,
                                                             ProvisioningConnectorConfig[] o2) {
        ProvisioningConnectorConfig[] ret = new ProvisioningConnectorConfig[o1.length + o2.length];

        System.arraycopy(o1, 0, ret, 0, o1.length);
        System.arraycopy(o2, 0, ret, o1.length, o2.length);

        return ret;
    }

    /**
     * @param o1
     * @param o2
     * @return
     */
    public static Property[] concatArrays(Property[] o1, Property[] o2) {

        Set<Property> properties = new HashSet<>(Arrays.asList(removeEmptyElements(o1)));
        properties.addAll(Arrays.asList(removeEmptyElements(o2)));
        return properties.toArray(new Property[properties.size()]);
    }

    /**
     * @param o1
     * @param o2
     * @return
     */
    public static FederatedAuthenticatorConfig[] concatArrays(FederatedAuthenticatorConfig[] o1,
                                                              FederatedAuthenticatorConfig[] o2) {
        FederatedAuthenticatorConfig[] ret = new FederatedAuthenticatorConfig[o1.length + o2.length];

        System.arraycopy(o1, 0, ret, 0, o1.length);
        System.arraycopy(o2, 0, ret, o1.length, o2.length);

        return ret;
    }

    /**
     * Extract key store filename
     *
     * @param filePath File path of a key store
     * @return Key store file name
     */
    public static String extractKeyStoreFileName(String filePath) {

        if (filePath != null) {
            String name = null;
            int index = filePath.lastIndexOf('/');
            if (index != -1) {
                name = filePath.substring(index + 1);
            } else {
                index = filePath.lastIndexOf(File.separatorChar);
                if (index != -1) {
                    name = filePath.substring(filePath.lastIndexOf(File.separatorChar));
                } else {
                    name = filePath;
                }
            }
            return name;
        } else {
            String errorMsg = "Invalid file path: \'NULL\'";
            log.debug(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
    }

    /**
     * Generate thumbprint of certificate
     *
     * @param encodedCert Base64 encoded certificate
     * @return Certificate thumbprint
     * @throws java.security.NoSuchAlgorithmException Unsupported hash algorithm
     */
    public static String generateThumbPrint(String encodedCert) throws NoSuchAlgorithmException {

        if (encodedCert != null) {
            MessageDigest digestValue = null;
            String algorithm;
            if (Boolean.parseBoolean(IdentityUtil.getProperty(IdentityConstants.CERT_THUMBPRINT_ENABLE_SHA256))) {
                algorithm = "SHA-256";
            } else {
                algorithm = "SHA-1";
            }
            digestValue = MessageDigest.getInstance(algorithm);
            byte[] der = Base64.decode(encodedCert);
            digestValue.update(der);
            byte[] digestInBytes = digestValue.digest();
            String publicCertThumbprint = hexify(digestInBytes);
            return publicCertThumbprint;
        } else {
            String errorMsg = "Invalid encoded certificate: \'NULL\'";
            log.debug(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
    }

    /**
     * Generate thumbprint of certificate
     *
     * @param encodedCert Base64 encoded certificate
     * @return Decoded <code>Certificate</code>
     * @throws java.security.cert.CertificateException Error when decoding certificate
     */
    public static Certificate decodeCertificate(String encodedCert) throws CertificateException {

        if (encodedCert != null) {
            byte[] bytes = Base64.decode(encodedCert);
            CertificateFactory factory = CertificateFactory.getInstance(IdentityApplicationConstants.CERTIFICATE_TYPE);
            X509Certificate cert = (X509Certificate) factory
                    .generateCertificate(new ByteArrayInputStream(bytes));
            return cert;
        } else {
            String errorMsg = "Invalid encoded certificate: \'NULL\'";
            log.debug(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
    }

    /**
     * extract one certificate from series of certificates.
     *
     * @param decodedCertificate series of certificate value in readable format
     * @param ordinal            relating to the order of the certificate in a series of certificate values
     * @return
     */
    public static String extractCertificate(String decodedCertificate, int ordinal) {

        String certificateVal;
        int numberOfCertificatesInCertificate = StringUtils.countMatches(decodedCertificate,
                IdentityUtil.PEM_BEGIN_CERTFICATE);
        if (ordinal == numberOfCertificatesInCertificate) {
            certificateVal = decodedCertificate.substring(StringUtils.ordinalIndexOf(decodedCertificate
                    , IdentityUtil.PEM_BEGIN_CERTFICATE, ordinal));
        } else {
            certificateVal = decodedCertificate.substring(StringUtils.ordinalIndexOf(
                    decodedCertificate, IdentityUtil.PEM_BEGIN_CERTFICATE, ordinal),
                    StringUtils.ordinalIndexOf(decodedCertificate,
                            IdentityUtil.PEM_BEGIN_CERTFICATE, ordinal + 1));
        }
        return certificateVal;
    }

    /**
     * >>>>>>> .r201641 Helper method to hexify a byte array. TODO:need to verify the logic
     *
     * @param bytes
     * @return hexadecimal representation
     */
    public static String hexify(byte bytes[]) {

        if (bytes != null) {
            char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c',
                    'd', 'e', 'f'};
            StringBuilder buf = new StringBuilder(bytes.length * 2);
            for (int i = 0; i < bytes.length; ++i) {
                buf.append(hexDigits[(bytes[i] & 0xf0) >> 4]);
                buf.append(hexDigits[bytes[i] & 0x0f]);
            }
            return buf.toString();
        } else {
            String errorMsg = "Invalid byte array: \'NULL\'";
            log.debug(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
    }

    /**
     * @param encodedCert
     * @return
     * @throws CertificateException
     */
    public static CertData getCertData(String encodedCert) throws CertificateException {

        if (encodedCert != null) {
            return createCertData(encodedCert);
        } else {
            String errorMsg = "Invalid encoded certificate: \'NULL\'";
            log.debug(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
    }

    /**
     * Generate CertData array
     *
     * @param certificateInfo array of certificate info
     * @return CertData array
     * @throws CertificateException
     */
    public static List<CertData> getCertDataArray(CertificateInfo[] certificateInfo) throws CertificateException {

        if (ArrayUtils.isNotEmpty(certificateInfo)) {
            List<CertData> certDataList = new ArrayList<>();
            HashMap<CertData, String> certDataMap = new HashMap<>();
            int i = 0;
            for (CertificateInfo certificateInfoVal : certificateInfo) {
                String certVal = certificateInfoVal.getCertValue();
                CertData certData = createCertData(certVal);
                certDataList.add(certData);
                certDataMap.put(certData, certVal);
                i++;
            }
            setCertDataMap(certDataMap);
            return certDataList;
        } else {
            String errorMsg = "Certificate info array is empty";
            if (log.isDebugEnabled()) {
                log.debug(errorMsg);
            }
            throw new IllegalArgumentException(errorMsg);
        }
    }

    private static CertData createCertData(String encodedCert) throws CertificateException {

        byte[] bytes = Base64.decode(encodedCert);
        CertificateFactory factory = CertificateFactory.getInstance(IdentityApplicationConstants.CERTIFICATE_TYPE);
        X509Certificate cert = (X509Certificate) factory
                .generateCertificate(new ByteArrayInputStream(bytes));
        Format formatter = new SimpleDateFormat(IdentityApplicationConstants.DATE_FORMAT);
        return fillCertData(cert, formatter);
    }

    private static void setCertDataMap(HashMap<CertData, String> certDataMap) {

        certificalteValMap = certDataMap;
    }

    public static HashMap<CertData, String> getCertDataMap() {

        return certificalteValMap;
    }

    /**
     * @param array
     * @return
     */
    public static boolean exclusiveOR(boolean[] array) {
        boolean foundTrue = false;
        for (boolean temp : array) {
            if (temp) {
                if (foundTrue) {
                    return false;
                } else {
                    foundTrue = true;
                }
            }
        }
        return foundTrue;
    }

    /**
     * @param cert
     * @param formatter
     * @return
     * @throws CertificateEncodingException
     */
    private static CertData fillCertData(X509Certificate cert, Format formatter)
            throws CertificateEncodingException {

        CertData certData = new CertData();
        certData.setSubjectDN(cert.getSubjectDN().getName());
        certData.setIssuerDN(cert.getIssuerDN().getName());
        certData.setSerialNumber(cert.getSerialNumber());
        certData.setVersion(cert.getVersion());
        certData.setNotAfter(formatter.format(cert.getNotAfter()));
        certData.setNotBefore(formatter.format(cert.getNotBefore()));
        certData.setPublicKey(Base64.encode(cert.getPublicKey().getEncoded()));
        return certData;
    }

    /**
     * @param rs
     */
    public static void closeResultSet(ResultSet rs) {
        IdentityDatabaseUtil.closeResultSet(rs);
    }

    public static void closeStatement(PreparedStatement preparedStatement) {
        IdentityDatabaseUtil.closeStatement(preparedStatement);
    }

    /**
     * @param configElem
     * @return
     */
    public static Properties readProperties(OMElement configElem) {

        Properties properties = new Properties();
        OMElement propsElem = configElem
                .getFirstChildWithName(
                        getQNameWithIdentityApplicationNS(IdentityApplicationConstants.ConfigElements.PROPERTIES));
        if (propsElem != null) {
            Iterator propItr = propsElem
                    .getChildrenWithLocalName(IdentityApplicationConstants.ConfigElements.PROPERTY);
            for (; propItr.hasNext(); ) {
                OMElement propElem = (OMElement) propItr.next();
                String propName = propElem.getAttributeValue(
                        new QName(IdentityApplicationConstants.ConfigElements.ATTR_NAME)).trim();
                String propValue = propElem.getText().trim();
                properties.put(propName, propValue);
                if (log.isDebugEnabled()) {
                    log.debug("Property name : " + propName + ", Property Value : " + propValue);
                }
            }
        }
        return properties;
    }

    /**
     * @param localPart
     * @return
     */
    public static QName getQNameWithIdentityApplicationNS(String localPart) {
        return new QName(IdentityApplicationConstants.APPLICATION_AUTHENTICATION_DEFAULT_NAMESPACE, localPart);
    }

    /**
     * @param federatedAuthenticators
     * @param authenticatorName
     * @return
     */
    public static FederatedAuthenticatorConfig getFederatedAuthenticator(
            FederatedAuthenticatorConfig[] federatedAuthenticators, String authenticatorName) {

        if (ArrayUtils.isNotEmpty(federatedAuthenticators)) {
            for (FederatedAuthenticatorConfig authenticator : federatedAuthenticators) {
                if (authenticator != null && authenticator.getName().equals(authenticatorName)) {
                    return authenticator;
                }
            }
        }
        return null;
    }

    /**
     * @param provisioningConnectors
     * @param connectorType
     * @return
     */
    public static ProvisioningConnectorConfig getProvisioningConnector(
            ProvisioningConnectorConfig[] provisioningConnectors, String connectorType) {

        for (ProvisioningConnectorConfig connector : provisioningConnectors) {
            if (connector.getName().equals(connectorType)) {
                return connector;
            }
        }
        return null;
    }

    public static Property getProperty(Property[] properties, String propertyName) {

        if (ArrayUtils.isEmpty(properties) || StringUtils.isBlank(propertyName)) {
            return null;
        }

        for (Property property : properties) {
            if (property == null) {
                continue;
            }
            if (propertyName.equals(property.getName())) {
                return property;
            }
        }
        return null;
    }

    public static IdentityProviderProperty getProperty(IdentityProviderProperty[] properties, String propertyName) {

        for (IdentityProviderProperty property : properties) {
            if (property.getName().equals(propertyName)) {
                return property;
            }
        }
        return null;
    }

    /**
     * @param jsonObj
     * @return Base64 encoded JWT
     */
    public static String getSignedJWT(String jsonObj, ServiceProvider serviceProvider) {

        String oauthConsumerSecret = null;

        if (serviceProvider.getInboundAuthenticationConfig() != null
                && serviceProvider.getInboundAuthenticationConfig()
                .getInboundAuthenticationRequestConfigs() != null
                && serviceProvider.getInboundAuthenticationConfig()
                .getInboundAuthenticationRequestConfigs().length > 0) {

            InboundAuthenticationRequestConfig[] authReqConfigs = serviceProvider
                    .getInboundAuthenticationConfig().getInboundAuthenticationRequestConfigs();

            for (InboundAuthenticationRequestConfig authReqConfig : authReqConfigs) {
                if ((IdentityApplicationConstants.OAuth2.NAME).equals(authReqConfig.getInboundAuthType())) {
                    if (authReqConfig.getProperties() != null) {
                        for (Property property : authReqConfig.getProperties()) {
                            if ((IdentityApplicationConstants.OAuth2.OAUTH_CONSUMER_SECRET)
                                    .equalsIgnoreCase(property.getName())) {
                                oauthConsumerSecret = property.getValue();
                                break;
                            }
                        }
                    }
                }
            }

        }

        long expiryTime = new Date().getTime() + 3000;
        String jwtBody = "{\"iss\":\"wso2\",\"exp\":" + expiryTime + ",\"iat\":"
                + new Date().getTime() + "," + jsonObj + "}";
        String jwtHeader = "{\"typ\":\"JWT\",\"alg\":\"HS256\"}";

        if (oauthConsumerSecret == null) {
            jwtHeader = "{\"typ\":\"JWT\",\"alg\":\"none\"}";
        }

        String base64EncodedHeader = java.util.Base64.getUrlEncoder().withoutPadding().
                encodeToString(jwtHeader.getBytes(StandardCharsets.UTF_8));
        String base64EncodedBody = java.util.Base64.getUrlEncoder().withoutPadding().
                encodeToString(jwtBody.getBytes(StandardCharsets.UTF_8));

        if (log.isDebugEnabled()) {
            log.debug("JWT Header :" + jwtHeader);
            log.debug("JWT Body :" + jwtBody);
        }

        String assertion = base64EncodedHeader + "." + base64EncodedBody;

        if (oauthConsumerSecret == null) {
            return assertion + ".";
        } else {
            String signedAssertion;
            try {
                signedAssertion = calculateHmacSha256(oauthConsumerSecret, assertion);
                return assertion + "." + signedAssertion;
            } catch (SignatureException e) {
                log.error("Error while signing the assertion", e);
                return assertion + ".";
            }
        }
    }

    private static String calculateHmacSha256(String key, String value) throws SignatureException {

        String result;
        try {
            SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
            result = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(rawHmac);
        } catch (NoSuchAlgorithmException e) {
            if (log.isDebugEnabled()) {
                log.debug("Failed to create the HMAC Signature", e);
            }
            throw new SignatureException("Invalid algorithm provided while calculating HMAC signature.", e);
        } catch (InvalidKeyException e) {
            if (log.isDebugEnabled()) {
                log.debug("Failed to create the HMAC Signature", e);
            }
            throw new SignatureException("Failed to calculate HMAC signature.", e);
        }
        return result;
    }

    /**
     * @param key
     * @param value
     * @return
     * @throws SignatureException
     */
    @Deprecated
    public static String calculateHmacSha1(String key, String value) throws SignatureException {
        String result;
        try {
            SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
            result = Base64Utils.encode(rawHmac);
        } catch (NoSuchAlgorithmException e) {
            if (log.isDebugEnabled()) {
                log.debug("Failed to create the HMAC Signature", e);
            }
            throw new SignatureException("Invalid algorithm provided while calculating HMAC signature.", e);
        } catch (InvalidKeyException e) {
            if (log.isDebugEnabled()) {
                log.debug("Failed to create the HMAC Signature", e);
            }
            throw new SignatureException("Failed to calculate HMAC signature.", e);
        }
        return result;
    }
    
    public static Map<String, String> getXMLSignatureAlgorithms() {
        return xmlSignatureAlgorithms;
    }
    
    public static Map<String, String> getXMLDigestAlgorithms() {
        return xmlDigestAlgorithms;
    }

    public static Map<String, String> getXMLAssertionEncryptionAlgorithms() {
        return xmlAssertionEncryptionAlgorithms;
    }

    public static Map<String, String> getXMLKeyEncryptionAlgorithms() {
        return xmlKeyEncryptionAlgorithms;
    }
    
    public static Map<String, String> getSAMLAuthnContextClasses() {
        return samlAuthnContextClasses;
    }
    
    public static List<String> getSAMLAuthnContextComparisonLevels() {
        return samlAuthnContextComparisonLevels;
    }
    
    public static Set<String> getXMLSignatureAlgorithmNames() {
        return xmlSignatureAlgorithms.keySet();
    }
    
    public static Set<String> getXMLDigestAlgorithmNames() {
        return xmlDigestAlgorithms.keySet();
    }
    
    public static Set<String> getSAMLAuthnContextClassNames() {
        return samlAuthnContextClasses.keySet();
    }

    /**
     * @return the Signing Algorithm URI defined in configuration
     */
    public static String getSigningAlgoURIByConfig() {
        if (StringUtils.isNotBlank(IdentityUtil.getProperty(IdentityConstants.ServerConfig
                .SSO_DEFAULT_SIGNING_ALGORITHM))) {
            return IdentityUtil.getProperty(IdentityConstants.ServerConfig.SSO_DEFAULT_SIGNING_ALGORITHM).trim();
        } else {
            return IdentityApplicationConstants.XML.SignatureAlgorithmURI.RSA_SHA256;
        }
    }

    /**
     * @return the Digest Algorithm URI defined in configuration
     */
    public static String getDigestAlgoURIByConfig() {
        if (StringUtils.isNotBlank(IdentityUtil.getProperty(IdentityConstants.ServerConfig
                .SSO_DEFAULT_DIGEST_ALGORITHM))) {
            return IdentityUtil.getProperty(IdentityConstants.ServerConfig.SSO_DEFAULT_DIGEST_ALGORITHM).trim();
        } else {
            return IdentityApplicationConstants.XML.DigestAlgorithmURI.SHA256;
        }
    }

    /**
     * @return the Assertion Encryption Algorithm URI defined in configuration
     */
    public static String getAssertionEncryptionAlgorithmURIByConfig() {

        if (StringUtils.isNotBlank(IdentityUtil.getProperty(IdentityConstants.ServerConfig
                .SSO_DEFAULT_ASSERTION_ENCRYPTION_ALGORITHM))) {
            return IdentityUtil.getProperty(IdentityConstants.ServerConfig.SSO_DEFAULT_ASSERTION_ENCRYPTION_ALGORITHM)
                    .trim();
        } else {
            return IdentityApplicationConstants.XML.AssertionEncryptionAlgorithmURI.AES256;
        }
    }

    /**
     * @return the Key Encryption Algorithm URI defined in configuration
     */
    public static String getKeyEncryptionAlgorithmURIByConfig() {
        if (StringUtils.isNotBlank(IdentityUtil.getProperty(IdentityConstants.ServerConfig
                .SSO_DEFAULT_KEY_ENCRYPTION_ALGORITHM))) {
            return IdentityUtil.getProperty(IdentityConstants.ServerConfig.SSO_DEFAULT_KEY_ENCRYPTION_ALGORITHM).trim();
        } else {
            return IdentityApplicationConstants.XML.KeyEncryptionAlgorithmURI.RSAOAEP;
        }
    }

    /**
     * This is used in back end. Property is the type of stub generated property
     *
     * @param authnConfigs       authenticatorConfigs to iterate
     * @param authenticatorName  authenticator name of which the values are needed
     * @param propNameStartsWith the prefix of the property name
     * @return the list of values which statrts with the propNameStartsWith.
     */
    public static List<String> getPropertyValuesForNameStartsWith(FederatedAuthenticatorConfig[] authnConfigs, String
            authenticatorName, String propNameStartsWith) {
        List<String> propValueSet = new ArrayList<>();
        for (FederatedAuthenticatorConfig config : authnConfigs) {
            if (authenticatorName.equals(config.getName())) {
                for (Property prop : config.getProperties()) {
                    if (prop.getName().startsWith(propNameStartsWith)) {
                        propValueSet.add(prop.getValue());
                    }
                }

            }
        }
        return propValueSet;
    }

    /**
     * Get a list of property values for a given property name prefix.
     *
     * @param properties            Authenticator config properties to iterate with.
     * @param propNameStartsWith    the prefix of the property name.
     * @return the list of values which starts with the propNameStartsWith.
     */
    public static List<String> getPropertyValuesForNameStartsWith(Property[] properties, String propNameStartsWith) {

        List<String> propValueSet = new ArrayList<>();
        for (Property property : properties) {
            if (property.getName().startsWith(propNameStartsWith)) {
                propValueSet.add(property.getValue());
            }
        }

        return propValueSet;
    }

    public static String getPropertyValue(Property[] properties, String propertyName) {

        Property property = getProperty(properties, propertyName);
        if (property != null) {
            if (StringUtils.isNotBlank(property.getValue())) {
                return property.getValue();
            }
        }
        return null;
    }

    private static Property[] removeEmptyElements(Property[] properties) {
        List<Property> propertyList = new ArrayList<>();
        if (ArrayUtils.isNotEmpty(properties)) {
            for (Property property : properties) {
                if (property != null && StringUtils.isNotBlank(property.getName())) {
                    propertyList.add(property);
                }
            }
        }
        return propertyList.toArray(new Property[0]);
    }

    /**
     * Used to check if a given adaptive auth script contains loops.
     *
     * @param script Adaptive auth script.
     * @return True if a loop is present.
     */
    public static boolean isLoopsPresentInAdaptiveAuthScript(String script) {

        script = getCleanedAdaptiveAuthScript(script);
        Matcher matcher = JS_LOOP_PATTERN.matcher(script);
        return matcher.find();
    }

    /**
     * Check if loops are allowed in adaptive auth scripts.
     *
     * @return true is loops are allowed.
     */
    public static boolean isLoopsInAdaptiveAuthScriptAllowed() {

        String confAllowLoops = IdentityUtil.getProperty(IdentityConstants.ServerConfig.ADAPTIVE_AUTH_ALLOW_LOOPS);

        // By default allow loops.
        if (StringUtils.isBlank(confAllowLoops)) {
            return true;
        }

        return Boolean.parseBoolean(confAllowLoops);
    }

    /**
     * Set updating system apps allowed for the current thread.
     *
     * @param isAllowUpdateSystem True if updating system apps is allowed.
     */
    public static void setAllowUpdateSystemApplicationThreadLocal(Boolean isAllowUpdateSystem) {

        allowUpdateSystemApplicationThreadLocal.set(isAllowUpdateSystem);
    }

    /**
     * Get updating system apps allowed for the current thread.
     *
     * @return True if updating system apps is allowed.
     */
    public static boolean getAllowUpdateSystemApplicationThreadLocal() {

        return allowUpdateSystemApplicationThreadLocal.get() != null ?
                allowUpdateSystemApplicationThreadLocal.get() : false;
    }

    /**
     * Clear allow update system application thread local.
     */
    public static void removeAllowUpdateSystemApplicationThreadLocal() {

        allowUpdateSystemApplicationThreadLocal.remove();
    }

    /**
     * Remove comments and strings from a script
     * and get only the script code.
     *
     * @param script script.
     * @return cleaned script.
     */
    private static String getCleanedAdaptiveAuthScript(String script) {

        StringBuilder cleanedScript = new StringBuilder();
        int mode = MODE_DEFAULT;
        for (int i = 0; i < script.length(); i++) {
            String subString = script.substring(i, Math.min(i + 2, script.length()));
            char c = script.charAt(i);
            switch (mode) {
                case MODE_DEFAULT: // Checks if start of a comment if not checks if start of a string.
                    mode = subString.equals("/*") ? MODE_MULTI_LINE
                            : (subString.equals("//") ? MODE_SINGLE_LINE
                            : (((c == '"') || (c == '\'')) ? MODE_STRING : MODE_DEFAULT));
                    break;
                case MODE_STRING: // Checks if end of a string if not checks if a char is a escape character.
                    mode = ((c == '"') || (c == '\'')) ? MODE_DEFAULT : ((c == '\\') ? MODE_ESCAPE : MODE_STRING);
                    if (mode == MODE_DEFAULT) {
                        continue;
                    }
                    break;
                case MODE_ESCAPE: // Marks end of escape character.
                    mode = MODE_STRING;
                    break;
                case MODE_SINGLE_LINE: // Checks to see if new line which marks end of a single line comment.
                    mode = (c == '\n') ? MODE_DEFAULT : MODE_SINGLE_LINE;
                    continue;
                case MODE_MULTI_LINE: // Checks to see if end of a new line comment.
                    mode = subString.equals("*/") ? MODE_DEFAULT : MODE_MULTI_LINE;
                    if (mode == MODE_DEFAULT) {
                        i += 1;
                    }
                    continue;
            }
            // If char is not part of a comment or part
            // of a string then append to cleaned script.
            if (mode < 2) {
                cleanedScript.append(c);
            }
        }
        return cleanedScript.toString();
    }
}
