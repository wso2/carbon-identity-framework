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
import org.wso2.carbon.identity.core.model.SAMLSSOServiceProviderDO;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.core.Registry;

/**
 * This class is used for managing SAML SSO providers. Adding, retrieving and removing service
 * providers are supported here.
 */
public class SAMLSSOServiceProviderManager {

    private static Log log = LogFactory.getLog(SAMLSSOServiceProviderManager.class);

    private static SAMLSSOServiceProviderManager instance;

    private SAMLSSOServiceProviderManager() {
    }

    /**
     * Get the instance of SAMLSSOServiceProviderManager.
     *
     * @return Instance of SAMLSSOServiceProviderManager.
     */
    public static SAMLSSOServiceProviderManager getInstance() {
        if (instance == null) {
            if (log.isDebugEnabled()) {
                log.debug("SAMLSSOServiceProviderManager instance created");
            }
            instance = new SAMLSSOServiceProviderManager();
        }
        return instance;
    }

    /**
     * Build the SAML service provider.
     *
     * @param tenantId Tenant ID.
     * @return SAML service provider.
     */
    private SAMLSSOServiceProviderDAO buildSAMLSSOProvider(int tenantId) throws RegistryException {
        Registry registry = IdentityTenantUtil.getRegistryService().getConfigSystemRegistry(tenantId);
        return new SAMLSSOServiceProviderDAO(registry);
    }


    /**
     * Add a saml service provider.
     *
     * @param serviceProviderDO     Service provider information object.
     * @param tenantId              Tenant ID.
     * @return True if success.
     * @throws IdentityException Error when adding the SAML service provider.
     */
    public boolean addServiceProvider(SAMLSSOServiceProviderDO serviceProviderDO, int tenantId)
            throws IdentityException {

        try {
            SAMLSSOServiceProviderDAO serviceProviderDAO = buildSAMLSSOProvider(tenantId);
            return serviceProviderDAO.addServiceProvider(serviceProviderDO);
        } catch (RegistryException e) {
            log.error("Error while adding service provider", e);
            throw new IdentityException("Error while retrieving registry", e);
        }
    }

    /**
     * Get all the saml service providers.
     *
     * @param tenantId  Tenant ID.
     * @return Array of SAMLSSOServiceProviderDO.
     * @throws IdentityException Error when getting the SAML service providers.
     */
    public SAMLSSOServiceProviderDO[] getServiceProviders(int tenantId)
            throws IdentityException {
        try {
            SAMLSSOServiceProviderDAO serviceProviderDOA = buildSAMLSSOProvider(tenantId);
            return serviceProviderDOA.getServiceProviders();
        } catch (RegistryException e) {
            log.error("Error while getting service providers", e);
            throw new IdentityException("Error while retrieving registry", e);
        }
    }

    /**
     * Get SAML issuer properties from service provider by saml issuer name.
     *
     * @param issuer    SAML issuer name.
     * @param tenantId  Tenant ID.
     * @return SAMLSSOServiceProviderDO
     * @throws IdentityException Error when getting the SAML service provider.
     */
    public SAMLSSOServiceProviderDO getServiceProvider(String issuer, int tenantId)
            throws IdentityException {

        try {
            SAMLSSOServiceProviderDAO serviceProviderDAO = buildSAMLSSOProvider(tenantId);
            return serviceProviderDAO.getServiceProvider(issuer);
        } catch (RegistryException e) {
            log.error("Error while getting service provider", e);
            throw new IdentityException("Error while retrieving SAML issuer " + e.getMessage());
        }

    }

    /**
     * Check whether SAML issuer exists by saml issuer name.
     *
     * @param issuer    SAML issuer name.
     * @param tenantId  Tenant ID.
     * @return True if exists
     * @throws IdentityException Error when checking the SAML service provider.
     */
    public boolean isServiceProviderExists(String issuer, int tenantId)
            throws IdentityException {

        try {
            SAMLSSOServiceProviderDAO serviceProviderDAO = buildSAMLSSOProvider(tenantId);
            return serviceProviderDAO.isServiceProviderExists(issuer);
        } catch (RegistryException e) {
            log.error("Error while getting service provider", e);
            throw new IdentityException("Error while retrieving SAML issuer " + e.getMessage());
        }
    }

    /**
     * Removes the SAML configuration related to the application, idenfied by the issuer.
     *
     * @param issuer    Issuer of the SAML application.
     * @param tenantId  Tenant ID.
     * @throws IdentityException Error when removing the SAML configuration.
     */
    public boolean removeServiceProvider(String issuer, int tenantId)
            throws IdentityException {

        try {
            SAMLSSOServiceProviderDAO serviceProviderDAO = buildSAMLSSOProvider(tenantId);
            return serviceProviderDAO.removeServiceProvider(issuer);
        } catch (RegistryException e) {
            log.error("Error while removing service provider", e);
            throw new IdentityException("Error while deleting SAML issuer " + e.getMessage());
        }
    }

    /**
     * Upload the SAML configuration related to the application, using metadata.
     *
     * @param samlssoServiceProviderDO  SAML service provider information object.
     * @param tenantId                  Tenant ID.
     * @return SAML service provider information object.
     * @throws IdentityException Error when uploading the SAML configuration.
     */
    public SAMLSSOServiceProviderDO uploadServiceProvider(SAMLSSOServiceProviderDO samlssoServiceProviderDO, int tenantId)
            throws IdentityException {

        try {
            SAMLSSOServiceProviderDAO serviceProviderDAO = buildSAMLSSOProvider(tenantId);
            return serviceProviderDAO.uploadServiceProvider(samlssoServiceProviderDO);
        } catch (RegistryException e) {
            log.error("Error while uploading service provider", e);
            throw new IdentityException("Error while uploading SAML issuer " + e.getMessage());
        }
    }
}
