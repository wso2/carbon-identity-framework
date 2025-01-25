/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.core.cache;

import org.wso2.carbon.identity.core.model.SAMLSSOServiceProviderDO;

/**
 * Cache implementation for SAML service providers.
 * Cache entry: <service provider issuer, latest SAMLSSOServiceProviderDO>
 */
public class SAMLSSOServiceProviderCache extends BaseCache<String, SAMLSSOServiceProviderDO> {

    private static final String CACHE_NAME = "SAMLSSOServiceProviderCache";
    private static final SAMLSSOServiceProviderCache instance = new SAMLSSOServiceProviderCache();

    private SAMLSSOServiceProviderCache() {

        super(CACHE_NAME);
    }

    public static SAMLSSOServiceProviderCache getInstance() {

        return instance;
    }

    @Override
    public void addToCache(String key, SAMLSSOServiceProviderDO samlSSOServiceProviderDO, int tenantId) {

        if (samlSSOServiceProviderDO != null) {
            SAMLSSOServiceProviderDO samlSSOServiceProviderDOCopy =
                    new SAMLSSOServiceProviderDO(samlSSOServiceProviderDO);
            super.addToCache(key, samlSSOServiceProviderDOCopy, tenantId);
        }
    }

    @Override
    public SAMLSSOServiceProviderDO getValueFromCache(String key, int tenantId) {

        SAMLSSOServiceProviderDO samlSSOServiceProviderDO = super.getValueFromCache(key, tenantId);
        SAMLSSOServiceProviderDO samlSSOServiceProviderDOCopy = null;
        if (samlSSOServiceProviderDO != null) {
            samlSSOServiceProviderDOCopy = new SAMLSSOServiceProviderDO(samlSSOServiceProviderDO);
        }
        return samlSSOServiceProviderDOCopy;
    }
}
