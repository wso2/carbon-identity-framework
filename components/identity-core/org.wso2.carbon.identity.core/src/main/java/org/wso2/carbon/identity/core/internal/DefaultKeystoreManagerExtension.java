/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.core.internal;

import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.KeyStoreManagerExtension;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.security.KeystoreUtils;

import java.security.PrivateKey;
import java.security.cert.Certificate;

/**
 * Provide the default implementation to fetch the tenant specific private key.
 * This default implementation is used if there isn't any other implementation
 * registered as an OSGi service.
 */

public class DefaultKeystoreManagerExtension implements KeyStoreManagerExtension {

    private RealmService realmService;

    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }

    @Override
    public PrivateKey getPrivateKey(String tenantDomain) throws IdentityException {
        PrivateKey privateKey;

        try {
            int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
            KeyStoreManager keyStoreManager = KeyStoreManager.getInstance(tenantId);
            if (!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                // derive key store name
                String PKCS12Name = KeystoreUtils.getKeyStoreFileLocation(tenantDomain);
                privateKey = (PrivateKey) keyStoreManager.getPrivateKey(PKCS12Name, tenantDomain);

            } else {
                privateKey = keyStoreManager.getDefaultPrivateKey();
            }
        } catch (Exception e) {
            throw new IdentityException("Error retrieving private key for tenant: " + tenantDomain, e);
        }
        return privateKey;
    }

    @Override
    public Certificate getCertificate(String tenantDomain) throws IdentityException {
        try {
            int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
            KeyStoreManager keyStoreManager = KeyStoreManager.getInstance(tenantId);
            return keyStoreManager.getDefaultPrimaryCertificate();
        } catch (UserStoreException e) {
            throw new IdentityException("Error retrieving the tenant ID for tenant: " + tenantDomain, e);
        } catch (Exception e) {
            throw new IdentityException(
                    "Error retrieving the primary certificate of the server, the tenant is: " + tenantDomain, e);
        }
    }
}
