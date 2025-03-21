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

import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.model.SAMLSSOServiceProviderDO;

import java.util.HashMap;
import java.util.Map;

/**
 * HybridSAMLSSOServiceProviderDAOImpl is a hybrid implementation of SAMLSSOServiceProviderDAO.
 * It uses both JDBC and Registry implementations to handle SAML service provider data.
 * If the service provider is already in the registry, it will be maintained there, including new versions.
 * New service providers will be persisted in the database.
 */
public class HybridSAMLSSOServiceProviderDAOImpl implements SAMLSSOServiceProviderDAO {

    SAMLSSOServiceProviderDAO jdbcSAMLSSOServiceProviderDAOImpl = CacheBackedSAMLSSOServiceProviderDAO.getInstance();
    SAMLSSOServiceProviderDAO registrySAMLSSOServiceProviderDAOImpl = new RegistrySAMLSSOServiceProviderDAOImpl();

    @Override
    public boolean addServiceProvider(SAMLSSOServiceProviderDO serviceProviderDO, int tenantId)
            throws IdentityException {

        return jdbcSAMLSSOServiceProviderDAOImpl.addServiceProvider(serviceProviderDO, tenantId);
    }

    @Override
    public boolean updateServiceProvider(SAMLSSOServiceProviderDO serviceProviderDO, String currentIssuer, int tenantId)
            throws IdentityException {

        if (jdbcSAMLSSOServiceProviderDAOImpl.isServiceProviderExists(currentIssuer, tenantId)) {
            return jdbcSAMLSSOServiceProviderDAOImpl.updateServiceProvider(serviceProviderDO, currentIssuer, tenantId);
        }
        return registrySAMLSSOServiceProviderDAOImpl.updateServiceProvider(serviceProviderDO, currentIssuer, tenantId);
    }

    @Override
    public SAMLSSOServiceProviderDO[] getServiceProviders(int tenantId) throws IdentityException {

        SAMLSSOServiceProviderDO[] jdbcSAMLSSOServiceProviders =
                jdbcSAMLSSOServiceProviderDAOImpl.getServiceProviders(tenantId);
        SAMLSSOServiceProviderDO[] registrySAMLSSOServiceProviders =
                registrySAMLSSOServiceProviderDAOImpl.getServiceProviders(tenantId);
        return mergeAndRemoveDuplicates(jdbcSAMLSSOServiceProviders, registrySAMLSSOServiceProviders);
    }

    @Override
    public boolean removeServiceProvider(String issuer, int tenantId) throws IdentityException {

        if (jdbcSAMLSSOServiceProviderDAOImpl.isServiceProviderExists(issuer, tenantId)) {
            return jdbcSAMLSSOServiceProviderDAOImpl.removeServiceProvider(issuer, tenantId);
        }
        return registrySAMLSSOServiceProviderDAOImpl.removeServiceProvider(issuer, tenantId);

    }

    @Override
    public SAMLSSOServiceProviderDO getServiceProvider(String issuer, int tenantId) throws IdentityException {

        if (jdbcSAMLSSOServiceProviderDAOImpl.isServiceProviderExists(issuer, tenantId)) {
            return jdbcSAMLSSOServiceProviderDAOImpl.getServiceProvider(issuer, tenantId);
        }
        return registrySAMLSSOServiceProviderDAOImpl.getServiceProvider(issuer, tenantId);
    }

    @Override
    public boolean isServiceProviderExists(String issuer, int tenantId) throws IdentityException {

        return (jdbcSAMLSSOServiceProviderDAOImpl.isServiceProviderExists(issuer, tenantId) ||
                registrySAMLSSOServiceProviderDAOImpl.isServiceProviderExists(issuer, tenantId));
    }

    @Override
    public SAMLSSOServiceProviderDO uploadServiceProvider(SAMLSSOServiceProviderDO serviceProviderDO, int tenantId)
            throws IdentityException {

        return jdbcSAMLSSOServiceProviderDAOImpl.uploadServiceProvider(serviceProviderDO, tenantId);
    }

    /**
     * Merges two arrays of SAMLSSOServiceProviderDO and removes duplicates.
     *
     * @param jdbcSAMLSSOServiceProviders     The JDBC SAML SSO ServiceProviders array.
     * @param registrySAMLSSOServiceProviders The Registry SAML SSO ServiceProviders array.
     * @return Merged array without duplicates.
     */
    public static SAMLSSOServiceProviderDO[] mergeAndRemoveDuplicates(
            SAMLSSOServiceProviderDO[] jdbcSAMLSSOServiceProviders,
            SAMLSSOServiceProviderDO[] registrySAMLSSOServiceProviders) {

        if (jdbcSAMLSSOServiceProviders == null) jdbcSAMLSSOServiceProviders = new SAMLSSOServiceProviderDO[0];
        if (registrySAMLSSOServiceProviders == null) registrySAMLSSOServiceProviders = new SAMLSSOServiceProviderDO[0];

        Map<String, SAMLSSOServiceProviderDO> uniqueMap = new HashMap<>();
        for (SAMLSSOServiceProviderDO serviceProviderDO : registrySAMLSSOServiceProviders) {
            uniqueMap.put(serviceProviderDO.getIssuer(), serviceProviderDO);
        }
        for (SAMLSSOServiceProviderDO serviceProviderDO : jdbcSAMLSSOServiceProviders) {
            uniqueMap.put(serviceProviderDO.getIssuer(), serviceProviderDO);
        }

        return uniqueMap.values().toArray(new SAMLSSOServiceProviderDO[0]);
    }
}
