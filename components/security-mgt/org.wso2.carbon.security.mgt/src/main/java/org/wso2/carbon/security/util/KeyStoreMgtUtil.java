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

package org.wso2.carbon.security.util;

import org.apache.axiom.om.util.Base64;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.IdentityProviderProperty;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationManagementUtil;
import org.wso2.carbon.identity.core.util.IdentityIOStreamUtils;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.security.SecurityServiceHolder;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.utils.WSO2Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Hashtable;
import java.util.Map;

public class KeyStoreMgtUtil {

    private static final Log log = LogFactory.getLog(KeyStoreMgtUtil.class);
    private static final String SECURITY_KEY_STORE_PW = "Security.KeyStore.Password";
    private static final String SECURITY_KEY_STORE_ALIAS = "Security.KeyStore.KeyAlias";

    private KeyStoreMgtUtil(){}

    /**
     * Dumping the generated pub. cert to a file
     *
     * @param configurationContext
     * @param cert                 content of the certificate
     * @param fileName             file name
     * @return file system location of the pub. cert
     */
    public static String dumpCert(ConfigurationContext configurationContext, byte[] cert,
                                  String fileName) {
        if (!verifyCertExistence(fileName, configurationContext)) {
            String workDir = (String) configurationContext.getProperty(ServerConstants.WORK_DIR);
            File pubCert = new File(workDir + File.separator + "pub_certs");

            if (fileName == null) {
                fileName = String.valueOf(System.currentTimeMillis() + new SecureRandom().nextDouble()) + ".cert";
            }
            if (!pubCert.exists()) {
                pubCert.mkdirs();
            }

            String filePath = workDir + File.separator + "pub_certs" + File.separator + fileName;
            OutputStream outStream = null;
            try {
                outStream = new FileOutputStream(filePath);
                outStream.write(cert);
            } catch (Exception e) {
                String msg = "Error when writing the public certificate to a file";
                log.error(msg);
                throw new SecurityException("msg", e);
            } finally {
                IdentityIOStreamUtils.flushOutputStream(outStream);
                IdentityIOStreamUtils.closeOutputStream(outStream);
            }

            Map fileResourcesMap = (Map) configurationContext.getProperty(WSO2Constants.FILE_RESOURCE_MAP);
            if (fileResourcesMap == null) {
                fileResourcesMap = new Hashtable();
                configurationContext.setProperty(WSO2Constants.FILE_RESOURCE_MAP, fileResourcesMap);
            }

            fileResourcesMap.put(fileName, filePath);
        }
        return WSO2Constants.ContextPaths.DOWNLOAD_PATH + "?id=" + fileName;
    }

    /**
     * Check whether the certificate is available in the file system
     *
     * @param fileName             file name
     * @param configurationContext configuration context of the current message
     */
    private static boolean verifyCertExistence(String fileName, ConfigurationContext configurationContext) {
        String workDir = (String) configurationContext.getProperty(ServerConstants.WORK_DIR);
        String filePath = workDir + File.separator + "pub_certs" + File.separator + fileName;
        File pubCert = new File(workDir + File.separator + "pub_certs" + File.separator + fileName);

        //if cert is still available then exit
        if (pubCert.exists()) {
            Map fileResourcesMap = (Map) configurationContext.getProperty(WSO2Constants.FILE_RESOURCE_MAP);
            if (fileResourcesMap == null) {
                fileResourcesMap = new Hashtable();
                configurationContext.setProperty(WSO2Constants.FILE_RESOURCE_MAP, fileResourcesMap);
            }
            if (fileResourcesMap.get(fileName) == null) {
                fileResourcesMap.put(fileName, filePath);
            }
            return true;
        }
        return false;
    }

    /**
     * Returns true if the alias is the signing key alias.
     *
     * @param alias  Alias of a private key.
     * @param tenant Tenant.
     * @return Returns true if the given alias is the signing key alias, else return false.
     * @throws IdentityProviderManagementException IdentityProviderManagementException.
     */
    public static boolean isSigningKeyAlias(String alias, String tenant) throws IdentityProviderManagementException {

        String signingKeyAlias = getSigningKeyAlias(tenant);
        boolean isSigningKey = false;
        if (StringUtils.equalsIgnoreCase(alias, signingKeyAlias)) {
            isSigningKey = true;
        }
        return isSigningKey;
    }

    /**
     * Get the alias of the private key used for signing in the respective tenant.
     *
     * @param tenantDomain Tenant domain.
     * @return The alias of the private key used for signing in the respective tenant.
     * @throws IdentityProviderManagementException IdentityProviderManagementException.
     */
    public static String getSigningKeyAlias(String tenantDomain) throws IdentityProviderManagementException {

        String signingKeyAlias = null;
        if (StringUtils.isBlank(tenantDomain)) {
            return signingKeyAlias;
        }
        IdentityProvider residentIdP = null;
        residentIdP = SecurityServiceHolder.getIdentityProviderService().getResidentIdP(tenantDomain);
        IdentityProviderProperty signingKeyAliasProp =
                IdentityApplicationManagementUtil.getProperty(residentIdP.getIdpProperties(),
                        IdentityApplicationConstants.SIGNING_KEY_ALIAS);
        if (signingKeyAliasProp != null) {
            signingKeyAlias = signingKeyAliasProp.getValue();
        } else {
            if (StringUtils.equalsIgnoreCase(tenantDomain, MultitenantConstants.IS_SUPER_TENANT)) {
                signingKeyAlias = CarbonUtils.getServerConfiguration().getFirstProperty(SECURITY_KEY_STORE_ALIAS);
            }
        }
        return signingKeyAlias;
    }

    /**
     * Get the default private key of super tenant.
     *
     * @return Private Key of super tenant.
     * @throws Exception Exception
     */
    public static PrivateKey getDefaultPrivateKey() throws Exception {

        KeyStore primaryKeystore =
                KeyStoreManager.getInstance(MultitenantConstants.SUPER_TENANT_ID).getPrimaryKeyStore();
        String alias = getSigningKeyAlias(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        char[] password = CarbonUtils.getServerConfiguration().getFirstProperty(SECURITY_KEY_STORE_PW).toCharArray();
        return (PrivateKey) primaryKeystore.getKey(alias, password);
    }

    /**
     * Return the public key of the super tenant.
     *
     * @return PublicKey of super tenant.
     * @throws Exception Exception.
     */
    public static PublicKey getDefaultPublicKey() throws Exception {

        KeyStore primaryKeystore =
                KeyStoreManager.getInstance(MultitenantConstants.SUPER_TENANT_ID).getPrimaryKeyStore();
        String alias = getSigningKeyAlias(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        return primaryKeystore.getCertificate(alias).getPublicKey();
    }

    /**
     * Return the certificate associated with the default public key of the super tenant keystore.
     *
     * @return The certificate of the super tenant keystore.
     * @throws Exception Exception.
     */
    public static X509Certificate getDefaultPrimaryCertificate() throws Exception {

        KeyStore primaryKeystore =
                KeyStoreManager.getInstance(MultitenantConstants.SUPER_TENANT_ID).getPrimaryKeyStore();
        String alias = getSigningKeyAlias(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        return (X509Certificate) primaryKeystore.getCertificate(alias);
    }

    /**
     * Extracts private key from string content.
     * @param privateKeyContent PrivateKeyContent.
     * @return Private Key.
     * @throws NoSuchAlgorithmException NoSuchAlgorithmException.
     * @throws InvalidKeySpecException InvalidKeySpecException.
     */
    public static Key extractPrivateKey(String privateKeyContent) throws NoSuchAlgorithmException, InvalidKeySpecException {

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decode(privateKeyContent));
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePrivate(keySpec);
    }

    public static String getCertFingerPrint(String mdAlg, Certificate cert) throws Exception {

        byte[] encCertInfo = cert.getEncoded();
        MessageDigest md = MessageDigest.getInstance(mdAlg);
        byte[] digest = md.digest(encCertInfo);
        return toHexString(digest);
    }

    /**
     * Converts a byte array to hex string
     */
    private static String toHexString(byte[] block) {

        StringBuffer buf = new StringBuffer();
        int len = block.length;
        for (int i = 0; i < len; i++) {
            byte2hex(block[i], buf);
            if (i < len - 1) {
                buf.append(":");
            }
        }
        return buf.toString();
    }

    /**
     * Converts a byte to hex digit and writes to the supplied buffer
     */
    private static void byte2hex(byte b, StringBuffer buf) {

        char[] hexChars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        int high = ((b & 0xf0) >> 4);
        int low = (b & 0x0f);
        buf.append(hexChars[high]);
        buf.append(hexChars[low]);
    }
}
