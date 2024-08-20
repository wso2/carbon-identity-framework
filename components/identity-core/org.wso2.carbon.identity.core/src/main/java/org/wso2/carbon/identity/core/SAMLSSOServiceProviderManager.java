/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderDAO;
import org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderDAOImpl;
import org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderRegistryDAOImpl;
import org.wso2.carbon.identity.core.model.SAMLSSOServiceProviderDO;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.core.Registry;

/**
 * This class is used for managing SAML SSO providers. Adding, retrieving and removing service
 * providers are supported here.
 */
public class SAMLSSOServiceProviderManager {

    private static final Log LOG = LogFactory.getLog(SAMLSSOServiceProviderManager.class);

    private static final String SAML_CONFIGS_LOCATION_CONFIG = "RegistryDataStoreLocation.SAMLConfigs";

    private static final String DATABASE = "database";

    /**
     * Build the SAML service provider.
     *
     * @param tenantId Tenant ID.
     * @return SAML service provider.
     */
    private SAMLSSOServiceProviderDAO buildSAMLSSOProvider(int tenantId) throws IdentityException {

        String samlConfigsDatabase = IdentityUtil.getProperty(SAML_CONFIGS_LOCATION_CONFIG);
        if (DATABASE.equals(samlConfigsDatabase)) {
            return new SAMLSSOServiceProviderDAOImpl(tenantId);
        }
        try {
            Registry registry = IdentityTenantUtil.getRegistryService().getConfigSystemRegistry(tenantId);
            return new SAMLSSOServiceProviderRegistryDAOImpl(registry);
        } catch (RegistryException e) {
            LOG.error("Error while retrieving registry", e);
            throw new IdentityException("Error while retrieving registry", e);
        }
    }

    /**
     * Add a saml service provider.
     *
     * @param serviceProviderDO Service provider information object.
     * @param tenantId          Tenant ID.
     * @return True if success.
     * @throws IdentityException Error when adding the SAML service provider.
     */
    public boolean addServiceProvider(SAMLSSOServiceProviderDO serviceProviderDO, int tenantId)
            throws IdentityException {

        SAMLSSOServiceProviderDAO serviceProviderDAO = buildSAMLSSOProvider(tenantId);
        return serviceProviderDAO.addServiceProvider(serviceProviderDO);
    }

    /**
     * Update a saml service provider if already exists.
     *
     * @param serviceProviderDO Service provider information object.
     * @param currentIssuer     Issuer of the service provider before the update.
     * @param tenantId          Tenant ID.
     * @return True if success.
     * @throws IdentityException Error when updating the SAML service provider.
     */
    public boolean updateServiceProvider(SAMLSSOServiceProviderDO serviceProviderDO, String currentIssuer, int tenantId)
            throws IdentityException {

        SAMLSSOServiceProviderDAO serviceProviderDAO = buildSAMLSSOProvider(tenantId);
        return serviceProviderDAO.updateServiceProvider(serviceProviderDO, currentIssuer);
    }

    /**
     * Get all the saml service providers.
     *
     * @param tenantId Tenant ID.
     * @return Array of SAMLSSOServiceProviderDO.
     * @throws IdentityException Error when getting the SAML service providers.
     */
    public SAMLSSOServiceProviderDO[] getServiceProviders(int tenantId)
            throws IdentityException {

        SAMLSSOServiceProviderDAO serviceProviderDOA = buildSAMLSSOProvider(tenantId);
        return serviceProviderDOA.getServiceProviders();
    }

    /**
     * Get SAML issuer properties from service provider by saml issuer name.
     *
     * @param issuer   SAML issuer name.
     * @param tenantId Tenant ID.
     * @return SAMLSSOServiceProviderDO
     * @throws IdentityException Error when getting the SAML service provider.
     */
    public SAMLSSOServiceProviderDO getServiceProvider(String issuer, int tenantId)
            throws IdentityException {

        SAMLSSOServiceProviderDAO serviceProviderDAO = buildSAMLSSOProvider(tenantId);
        return serviceProviderDAO.getServiceProvider(issuer);

    }

    /**
     * Check whether SAML issuer exists by saml issuer name.
     *
     * @param issuer   SAML issuer name.
     * @param tenantId Tenant ID.
     * @return True if exists
     * @throws IdentityException Error when checking the SAML service provider.
     */
    public boolean isServiceProviderExists(String issuer, int tenantId)
            throws IdentityException {

        SAMLSSOServiceProviderDAO serviceProviderDAO = buildSAMLSSOProvider(tenantId);
        return serviceProviderDAO.isServiceProviderExists(issuer);
    }

    /**
     * Removes the SAML configuration related to the application, idenfied by the issuer.
     *
     * @param issuer   Issuer of the SAML application.
     * @param tenantId Tenant ID.
     * @throws IdentityException Error when removing the SAML configuration.
     */
    public boolean removeServiceProvider(String issuer, int tenantId)
            throws IdentityException {

        SAMLSSOServiceProviderDAO serviceProviderDAO = buildSAMLSSOProvider(tenantId);
        return serviceProviderDAO.removeServiceProvider(issuer);
    }

    /**
     * Upload the SAML configuration related to the application, using metadata.
     *
     * @param samlssoServiceProviderDO SAML service provider information object.
     * @param tenantId                 Tenant ID.
     * @return SAML service provider information object.
     * @throws IdentityException Error when uploading the SAML configuration.
     */
    public SAMLSSOServiceProviderDO uploadServiceProvider(SAMLSSOServiceProviderDO samlssoServiceProviderDO,
                                                          int tenantId)
            throws IdentityException {

        SAMLSSOServiceProviderDAO serviceProviderDAO = buildSAMLSSOProvider(tenantId);
        return serviceProviderDAO.uploadServiceProvider(samlssoServiceProviderDO);
    }
}
