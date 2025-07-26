/*
 * Copyright (c) 2017-2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.core.internal.component;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.KeyProviderService;
import org.wso2.carbon.identity.core.KeyStoreManagerExtension;
import org.wso2.carbon.utils.CarbonUtils;

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

    public DefaultKeyProviderService(KeyStoreManagerExtension defaultKeyStoreManagerExtension) {
        if (defaultKeyStoreManagerExtension == null) {
            throw new NullPointerException(
                    "The " + DefaultKeyProviderService.class.getName() + " can not be constructed with null "
                            + KeyStoreManagerExtension.class.getName());
        }
        this.keyStoreManagerExtension = defaultKeyStoreManagerExtension;
    }

    /**
     * Sets the current used KeyStoreManagerExtension with the new extension.
     *
     * Care must be taken not to set KeyStoreManagerExtension to null as it throws a NullPointerException.
     * 
     * @param keyStoreManagerExtension the new KeyStoreManagerExtension, can not be set to null,
     *                                 it has to be valid KeyStoreManagerExtension reference..
     */
    protected void setKeyStoreManagerExtension(KeyStoreManagerExtension keyStoreManagerExtension) {
        CarbonUtils.checkSecurity();
        if (log.isDebugEnabled()) {
            log.debug("Setting the current KeyStoreManagerExtension with new KeyStoreManagerExtension : "
                    + keyStoreManagerExtension);
        }
        if (keyStoreManagerExtension == null) {
            throw new NullPointerException(
                    "The " + DefaultKeyProviderService.class.getName() + " can not be set with null "
                            + KeyStoreManagerExtension.class.getName());
        }
        this.keyStoreManagerExtension = keyStoreManagerExtension;
    }

    @Override
    public PrivateKey getPrivateKey(String tenantDomain) throws IdentityException {
        CarbonUtils.checkSecurity();
        return keyStoreManagerExtension.getPrivateKey(tenantDomain);
    }

    @Override
    public Certificate getCertificate(String tenantDomain) throws IdentityException {
        CarbonUtils.checkSecurity();
        return keyStoreManagerExtension.getCertificate(tenantDomain);
    }
}
