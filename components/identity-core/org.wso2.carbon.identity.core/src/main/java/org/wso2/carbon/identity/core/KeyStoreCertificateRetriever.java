/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.core;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.security.KeyStore;
import java.security.cert.X509Certificate;

/**
 * The Keystore file (JKS) based implementation of @{@link CertificateRetriever}
 */
public class KeyStoreCertificateRetriever implements CertificateRetriever {

    private static final Log LOG = LogFactory.getLog(KeyStoreCertificateRetriever.class);

    /**
     * @param certificateId Alias of the certificate to be retrieved.
     * @param tenant        The tenant where the key store file should be loaded from.
     *                      If the tenant is the super tenant, the primary key store will be used.
     * @return The certificate for the given alias
     */
    @Override
    public X509Certificate getCertificate(String certificateId, Tenant tenant) throws CertificateRetrievingException {

        if (StringUtils.isBlank(certificateId)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Invalid alias received for retrieving a certificate in tenant domain: "
                        + tenant.getDomain());
            }
            return null;
        }

        KeyStoreManager keyStoreManager = KeyStoreManager.getInstance(tenant.getId());

        KeyStore keyStore;

        try {
            if (tenant.getId() != MultitenantConstants.SUPER_TENANT_ID) {
                // This is a tenant. So load the tenant key store.
                keyStore = keyStoreManager.getKeyStore(getKeyStoreName(tenant.getDomain()));
            } else {
                // This is the super tenant. So load the primary key store.
                keyStore = keyStoreManager.getPrimaryKeyStore();
            }
            X509Certificate certificate = (X509Certificate) keyStore.getCertificate(certificateId);
            return certificate;
        } catch (Exception e) {
            String errorMsg = String.format("Error occurred while retrieving the certificate for the alias '%s' " +
                    "of the tenant domain '%s'.", certificateId, tenant.getDomain());
            throw new CertificateRetrievingException(errorMsg, e);
        }
    }

    private String getKeyStoreName(String tenantDomain) {
        String keyStoreName = tenantDomain.trim().replace(".", "-");
        return keyStoreName + ".jks";
    }
}
