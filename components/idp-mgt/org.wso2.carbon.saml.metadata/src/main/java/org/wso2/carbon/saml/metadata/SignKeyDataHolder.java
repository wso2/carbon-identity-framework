/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
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

package org.wso2.carbon.saml.metadata;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.signature.XMLSignature;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.credential.CredentialContextSet;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.security.x509.X509Credential;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.idp.mgt.MetadataException;
import org.wso2.carbon.saml.metadata.internal.IDPMetadataSAMLServiceComponentHolder;
import org.wso2.carbon.security.keystore.KeyStoreAdmin;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.crypto.SecretKey;
import java.io.File;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;

public class SignKeyDataHolder implements X509Credential {

    private String signatureAlgorithm = null;

    private X509Certificate[] issuerCerts = null;

    private PrivateKey issuerPK = null;

    private static Log log = LogFactory.getLog(SignKeyDataHolder.class);

    /**
     * Represent OpenSAML compatible certificate credential
     *
     *
     */
    public SignKeyDataHolder() throws MetadataException {
        String keyAlias;
        KeyStoreAdmin keyAdmin;
        KeyStoreManager keyMan;
        Certificate[] certificates;
        int tenantID;
        String userTenantDomain;

        try {
            userTenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

            if (log.isDebugEnabled()) {
                log.debug("Key store used for signing is based on the tenant:  " + userTenantDomain);
            }

            if (tenantID != MultitenantConstants.SUPER_TENANT_ID) {
                String keyStoreName = userTenantDomain.trim().replace(".", "-") + ".jks";
                keyAlias = userTenantDomain;
                keyMan = KeyStoreManager.getInstance(tenantID);
                File f;

                KeyStore keyStore = keyMan.getKeyStore(keyStoreName);
                issuerPK = (PrivateKey) keyMan.getPrivateKey(keyStoreName, userTenantDomain);
                certificates = keyStore.getCertificateChain(keyAlias);
                issuerCerts = new X509Certificate[certificates.length];

                int i = 0;
                for (Certificate certificate : certificates) {
                    issuerCerts[i++] = (X509Certificate) certificate;
                }

                signatureAlgorithm = XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256;

                String pubKeyAlgo = issuerCerts[0].getPublicKey().getAlgorithm();
                if (pubKeyAlgo.equalsIgnoreCase("DSA")) {
                    signatureAlgorithm = XMLSignature.ALGO_ID_SIGNATURE_DSA;
                }

            } else {
                keyAlias = ServerConfiguration.getInstance().getFirstProperty("Security.KeyStore.KeyAlias");

                keyAdmin = new KeyStoreAdmin(tenantID, IDPMetadataSAMLServiceComponentHolder.getInstance().getRegistryService()
                        .getGovernanceSystemRegistry());
                keyMan = KeyStoreManager.getInstance(tenantID);

                issuerPK = (PrivateKey) keyAdmin.getPrivateKey(keyAlias, true);
                certificates = keyMan.getPrimaryKeyStore().getCertificateChain(keyAlias);
                issuerCerts = new X509Certificate[certificates.length];

                int i = 0;
                for (Certificate certificate : certificates) {
                    issuerCerts[i++] = (X509Certificate) certificate;
                }

                signatureAlgorithm = XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256;

                String pubKeyAlgo = issuerCerts[0].getPublicKey().getAlgorithm();
                if (pubKeyAlgo.equalsIgnoreCase("DSA")) {
                    signatureAlgorithm = XMLSignature.ALGO_ID_SIGNATURE_DSA;
                }
            }

        } catch (Exception e) {
            throw new MetadataException("Error occurred while creating certificate credentials", e);
        }

    }

    public Collection<X509CRL> getCRLs() {
        return null;
    }

    public X509Certificate getEntityCertificate() {
        return issuerCerts[0];
    }

    public Collection<X509Certificate> getEntityCertificateChain() {
        return Arrays.asList(issuerCerts);
    }

    public CredentialContextSet getCredentalContextSet() {
        return null;
    }

    public Class<? extends Credential> getCredentialType() {
        return null;
    }

    public String getEntityId() {
        return null;
    }

    public Collection<String> getKeyNames() {
        return null;
    }

    public PrivateKey getPrivateKey() {
        return issuerPK;
    }

    public PublicKey getPublicKey() {
        return issuerCerts[0].getPublicKey();
    }

    public SecretKey getSecretKey() {
        return null;
    }

    public UsageType getUsageType() {
        return null;
    }

}
