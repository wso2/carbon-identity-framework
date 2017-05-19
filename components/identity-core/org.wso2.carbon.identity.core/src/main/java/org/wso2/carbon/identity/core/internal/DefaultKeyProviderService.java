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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.KeyProviderService;
import org.wso2.carbon.identity.core.KeyStoreManagerExtension;

import java.security.PrivateKey;
import java.security.cert.Certificate;

/**
 * Provide the default implementation to fetch the tenant specific private key.
 * This default implementation is used if there isn't any other implementation
 * registered as an OSGi service.
 */
public class DefaultKeyProviderService implements KeyProviderService {

    private static final Log log = LogFactory.getLog(DefaultKeyProviderService.class);

    private KeyStoreManagerExtension keyStoreManagerExtension;
    private KeyStoreManagerExtension defaultKeyStoreManagerExtension;

    public DefaultKeyProviderService(KeyStoreManagerExtension defaultKeyStoreManagerExtension) {
        this.defaultKeyStoreManagerExtension = defaultKeyStoreManagerExtension;
        this.keyStoreManagerExtension = defaultKeyStoreManagerExtension;
    }

    /**
     * Updates the current used KeyStoreManagerExtension with the new extension.
     * Updating with null value will cause the KeyProviderService to revert to using the default
     * KeyStoreManagerExtension.
     * @param keyStoreManagerExtension the new KeyStoreManagerExtension, or null to select the default one.
     */
    public void updateKeyStoreManagerExtension(KeyStoreManagerExtension keyStoreManagerExtension) {
        if (log.isDebugEnabled()) {
            log.debug("Updating the current KeyStoreManagerExtension with new KeyStoreManagerExtension : "
                    + keyStoreManagerExtension);
        }
        if (keyStoreManagerExtension == null) {
            if (log.isDebugEnabled()) {
                log.debug("The new KeyStoreManagerExtension is null hence using the default : "
                        + defaultKeyStoreManagerExtension);
            }
            keyStoreManagerExtension = defaultKeyStoreManagerExtension;
        }
        this.keyStoreManagerExtension = keyStoreManagerExtension;
    }

    @Override
    public PrivateKey getPrivateKey(String tenantDomain) throws IdentityException {
        return keyStoreManagerExtension.getPrivateKey(tenantDomain);
    }

    @Override
    public Certificate getCertificate(String tenantDomain) throws IdentityException {
        return keyStoreManagerExtension.getCertificate(tenantDomain);
    }
}
