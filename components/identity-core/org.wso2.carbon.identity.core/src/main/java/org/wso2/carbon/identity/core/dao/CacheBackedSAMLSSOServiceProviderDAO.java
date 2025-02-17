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

package org.wso2.carbon.identity.core.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.cache.SAMLSSOServiceProviderCache;
import org.wso2.carbon.identity.core.cache.SAMLSSOServiceProviderListCache;
import org.wso2.carbon.identity.core.model.SAMLSSOServiceProviderDO;

/**
 * Cache backed implementation of SAMLSSOServiceProviderDAO.
 */
public class CacheBackedSAMLSSOServiceProviderDAO extends JDBCSAMLSSOServiceProviderDAOImpl {

    private static final Log LOG = LogFactory.getLog(CacheBackedSAMLSSOServiceProviderDAO.class);
    private final SAMLSSOServiceProviderCache samlSSOServiceProviderCache = SAMLSSOServiceProviderCache.getInstance();
    private final SAMLSSOServiceProviderListCache samlSSOServiceProviderListCache =
            SAMLSSOServiceProviderListCache.getInstance();
    private static final CacheBackedSAMLSSOServiceProviderDAO instance = new CacheBackedSAMLSSOServiceProviderDAO();

    private static final String SAML_SP_LIST_CACHE_KEY = "SAML_SP_LIST_CACHE_KEY";

    private CacheBackedSAMLSSOServiceProviderDAO() {

    }

    public static CacheBackedSAMLSSOServiceProviderDAO getInstance() {

        return instance;
    }

    @Override
    public boolean addServiceProvider(SAMLSSOServiceProviderDO serviceProviderDO, int tenantId)
            throws IdentityException {

        if (super.addServiceProvider(serviceProviderDO, tenantId)) {
            samlSSOServiceProviderCache.addToCache(serviceProviderDO.getIssuer(), serviceProviderDO, tenantId);
            samlSSOServiceProviderListCache.clearCacheEntry(SAML_SP_LIST_CACHE_KEY, tenantId);
            return true;
        }
        return false;
    }

    @Override
    public SAMLSSOServiceProviderDO getServiceProvider(String issuer, int tenantId) throws IdentityException {

        SAMLSSOServiceProviderDO samlSSOServiceProviderDO =
                samlSSOServiceProviderCache.getValueFromCache(issuer, tenantId);
        if (samlSSOServiceProviderDO != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Cache hit in SAMLSSOServiceProviderCache for issuer: %s, tenant: %d", issuer,
                        tenantId));
            }
            return samlSSOServiceProviderDO;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Cache miss in SAMLSSOServiceProviderCache for issuer: %s, tenant: %d", issuer,
                    tenantId));
        }
        samlSSOServiceProviderDO = super.getServiceProvider(issuer, tenantId);
        samlSSOServiceProviderCache.addToCache(issuer, samlSSOServiceProviderDO, tenantId);

        return samlSSOServiceProviderDO;
    }

    @Override
    public SAMLSSOServiceProviderDO[] getServiceProviders(int tenantId) throws IdentityException {

        SAMLSSOServiceProviderDO[] samlSSOServiceProviderDOList =
                samlSSOServiceProviderListCache.getValueFromCache(SAML_SP_LIST_CACHE_KEY, tenantId);
        if (samlSSOServiceProviderDOList != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format(
                        "Cache hit in SAMLSSOServiceProviderListCache for SAML Service Providers for tenant: %d",
                        tenantId));
            }
            return samlSSOServiceProviderDOList;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format(
                    "Cache miss in SAMLSSOServiceProviderListCache for SAML Service Providers for tenant: %d",
                    tenantId));
        }
        samlSSOServiceProviderDOList = super.getServiceProviders(tenantId);
        samlSSOServiceProviderListCache.addToCache(SAML_SP_LIST_CACHE_KEY, samlSSOServiceProviderDOList, tenantId);

        return samlSSOServiceProviderDOList;
    }

    @Override
    public boolean removeServiceProvider(String issuer, int tenantId) throws IdentityException {

        if (super.removeServiceProvider(issuer, tenantId)) {
            samlSSOServiceProviderCache.clearCacheEntry(issuer, tenantId);
            samlSSOServiceProviderListCache.clearCacheEntry(SAML_SP_LIST_CACHE_KEY, tenantId);
            return true;
        }
        return false;
    }

    @Override
    public SAMLSSOServiceProviderDO uploadServiceProvider(SAMLSSOServiceProviderDO serviceProviderDO, int tenantId)
            throws IdentityException {

        SAMLSSOServiceProviderDO samlssoserviceProviderDO = super.uploadServiceProvider(serviceProviderDO, tenantId);
        samlSSOServiceProviderCache.addToCache(serviceProviderDO.getIssuer(), serviceProviderDO, tenantId);
        samlSSOServiceProviderListCache.clearCacheEntry(SAML_SP_LIST_CACHE_KEY, tenantId);
        return samlssoserviceProviderDO;
    }

    @Override
    public boolean updateServiceProvider(SAMLSSOServiceProviderDO serviceProviderDO, String currentIssuer, int tenantId)
            throws IdentityException {

        if (super.updateServiceProvider(serviceProviderDO, currentIssuer, tenantId)) {
            samlSSOServiceProviderCache.clearCacheEntry(currentIssuer, tenantId);
            samlSSOServiceProviderListCache.clearCacheEntry(SAML_SP_LIST_CACHE_KEY, tenantId);
            samlSSOServiceProviderCache.addToCache(serviceProviderDO.getIssuer(), serviceProviderDO, tenantId);
            return true;
        }
        return false;
    }
}
