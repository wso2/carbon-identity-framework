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
 * Cache entry: <constant key, latest SAMLSSOServiceProviderDO list>
 */
public class SAMLSSOServiceProviderListCache extends BaseCache<String, SAMLSSOServiceProviderDO[]> {

    private static final String CACHE_NAME = "SAMLSSOServiceProviderListCache";
    private static final SAMLSSOServiceProviderListCache instance = new SAMLSSOServiceProviderListCache();

    private SAMLSSOServiceProviderListCache() {

        super(CACHE_NAME);
    }

    public static SAMLSSOServiceProviderListCache getInstance() {

        return instance;
    }

    @Override
    public void addToCache(String key, SAMLSSOServiceProviderDO[] samlSSOServiceProviderDOs, int tenantId) {

        SAMLSSOServiceProviderDO[] samlSSOServiceProviderDOList = createCopy(samlSSOServiceProviderDOs);
        super.addToCache(key, samlSSOServiceProviderDOList, tenantId);
    }

    @Override
    public SAMLSSOServiceProviderDO[] getValueFromCache(String key, int tenantId) {

        SAMLSSOServiceProviderDO[] samlSSOServiceProviderDOs = super.getValueFromCache(key, tenantId);
        return createCopy(samlSSOServiceProviderDOs);
    }

    private SAMLSSOServiceProviderDO[] createCopy(SAMLSSOServiceProviderDO[] samlSSOServiceProviderDOs) {

        if (samlSSOServiceProviderDOs == null) {
            return null;
        }
        SAMLSSOServiceProviderDO[] samlSSOServiceProviderDOList =
                new SAMLSSOServiceProviderDO[samlSSOServiceProviderDOs.length];
        for (int i = 0; i < samlSSOServiceProviderDOs.length; i++) {
            if (samlSSOServiceProviderDOs[i] != null) {
                samlSSOServiceProviderDOList[i] = new SAMLSSOServiceProviderDO(samlSSOServiceProviderDOs[i]);
            }
        }
        return samlSSOServiceProviderDOList;
    }
}
