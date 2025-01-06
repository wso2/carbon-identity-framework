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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.dao.SAMLServiceProviderPersistenceManagerFactory;
import org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderDAO;
import org.wso2.carbon.identity.core.model.SAMLSSOServiceProviderDO;

/**
 * This class is used for managing SAML SSO providers. Adding, retrieving and removing service
 * providers are supported here.
 */
public class SAMLSSOServiceProviderManager {

    SAMLServiceProviderPersistenceManagerFactory samlSSOPersistenceManagerFactory =
            new SAMLServiceProviderPersistenceManagerFactory();
    SAMLSSOServiceProviderDAO serviceProviderDAO =
            samlSSOPersistenceManagerFactory.getSAMLServiceProviderPersistenceManager();
    private static Log LOG = LogFactory.getLog(SAMLSSOServiceProviderManager.class);

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

        validateServiceProvider(serviceProviderDO);
        if (isServiceProviderExists(serviceProviderDO.getIssuer(), tenantId)) {
            if (LOG.isDebugEnabled()){
                LOG.debug(serviceProviderInfo(serviceProviderDO) + " already exists.");
            }
            return false;
        }
        return serviceProviderDAO.addServiceProvider(serviceProviderDO, tenantId);
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

        validateServiceProvider(serviceProviderDO);
        String newIssuer = serviceProviderDO.getIssuer();
        boolean isIssuerUpdated = !StringUtils.equals(currentIssuer, newIssuer);
        if (isIssuerUpdated && isServiceProviderExists(newIssuer, tenantId)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(serviceProviderInfo(serviceProviderDO) + " already exists.");
            }
            return false;
        }
        return serviceProviderDAO.updateServiceProvider(serviceProviderDO, currentIssuer, tenantId);
    }

    /**
     * Get all the saml service providers.
     *
     * @param tenantId Tenant ID.
     * @return Array of SAMLSSOServiceProviderDO.
     * @throws IdentityException Error when getting the SAML service providers.
     */
    public SAMLSSOServiceProviderDO[] getServiceProviders(int tenantId) throws IdentityException {

        return serviceProviderDAO.getServiceProviders(tenantId);
    }

    /**
     * Get SAML issuer properties from service provider by saml issuer name.
     *
     * @param issuer   SAML issuer name.
     * @param tenantId Tenant ID.
     * @return SAMLSSOServiceProviderDO
     * @throws IdentityException Error when getting the SAML service provider.
     */
    public SAMLSSOServiceProviderDO getServiceProvider(String issuer, int tenantId) throws IdentityException {

        return serviceProviderDAO.getServiceProvider(issuer, tenantId);
    }

    /**
     * Check whether SAML issuer exists by saml issuer name.
     *
     * @param issuer   SAML issuer name.
     * @param tenantId Tenant ID.
     * @return True if exists
     * @throws IdentityException Error when checking the SAML service provider.
     */
    public boolean isServiceProviderExists(String issuer, int tenantId) throws IdentityException {

        return serviceProviderDAO.isServiceProviderExists(issuer, tenantId);
    }

    /**
     * Removes the SAML configuration related to the application, idenfied by the issuer.
     *
     * @param issuer   Issuer of the SAML application.
     * @param tenantId Tenant ID.
     * @throws IdentityException Error when removing the SAML configuration.
     */
    public boolean removeServiceProvider(String issuer, int tenantId) throws IdentityException {

        if (issuer == null || StringUtils.isEmpty(issuer.trim())) {
            throw new IllegalArgumentException("Trying to delete issuer \'" + issuer + "\'");
        }
        if (!isServiceProviderExists(issuer, tenantId)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Service Provider with issuer: " + issuer + " does not exist.");
            }
            return false;
        }
        return serviceProviderDAO.removeServiceProvider(issuer, tenantId);
    }

    /**
     * Upload the SAML configuration related to the application, using metadata.
     *
     * @param serviceProviderDO SAML service provider information object.
     * @param tenantId                 Tenant ID.
     * @return SAML service provider information object.
     * @throws IdentityException Error when uploading the SAML configuration.
     */
    public SAMLSSOServiceProviderDO uploadServiceProvider(SAMLSSOServiceProviderDO serviceProviderDO,
                                                          int tenantId) throws IdentityException {

        validateServiceProvider(serviceProviderDO);
        if (serviceProviderDO.getDefaultAssertionConsumerUrl() == null) {
            throw new IdentityException("No default assertion consumer URL provided for service provider :" +
                    serviceProviderDO.getIssuer());
        }
        if (isServiceProviderExists(serviceProviderDO.getIssuer(), tenantId)) {
            if (LOG.isDebugEnabled()){
                LOG.debug(serviceProviderInfo(serviceProviderDO) + " already exists.");
            }
            throw new IdentityException("A Service Provider already exists.");
        }

        return serviceProviderDAO.uploadServiceProvider(serviceProviderDO, tenantId);
    }

    private void validateServiceProvider(SAMLSSOServiceProviderDO serviceProviderDO) throws IdentityException {

        if (serviceProviderDO == null || serviceProviderDO.getIssuer() == null ||
                StringUtils.isBlank(serviceProviderDO.getIssuer())) {
            throw new IdentityException("Issuer cannot be found in the provided arguments.");
        }

        if (StringUtils.isNotBlank(serviceProviderDO.getIssuerQualifier()) &&
                !serviceProviderDO.getIssuer().contains(IdentityRegistryResources.QUALIFIER_ID)) {
            serviceProviderDO.setIssuer(
                    getIssuerWithQualifier(serviceProviderDO.getIssuer(), serviceProviderDO.getIssuerQualifier()));
        }
    }

    private String serviceProviderInfo(SAMLSSOServiceProviderDO serviceProviderDO) {

        if (StringUtils.isNotBlank(serviceProviderDO.getIssuerQualifier())) {
            return "SAML2 Service Provider with issuer: " + getIssuerWithoutQualifier(serviceProviderDO.getIssuer()) +
                    " and qualifier name " + serviceProviderDO.getIssuerQualifier();
        } else {
            return "SAML2 Service Provider with issuer: " + serviceProviderDO.getIssuer();
        }
    }

    /**
     * Get the issuer value to be added to registry by appending the qualifier.
     *
     * @param issuer value given as 'issuer' when configuring SAML SP.
     * @return issuer value with qualifier appended.
     */
    private String getIssuerWithQualifier(String issuer, String qualifier) {

        return issuer + IdentityRegistryResources.QUALIFIER_ID + qualifier;
    }

    /**
     * Get the issuer value by removing the qualifier.
     *
     * @param issuerWithQualifier issuer value saved in the registry.
     * @return issuer value given as 'issuer' when configuring SAML SP.
     */
    private String getIssuerWithoutQualifier(String issuerWithQualifier) {

        String issuerWithoutQualifier = StringUtils.substringBeforeLast(issuerWithQualifier,
                IdentityRegistryResources.QUALIFIER_ID);
        return issuerWithoutQualifier;
    }
}
