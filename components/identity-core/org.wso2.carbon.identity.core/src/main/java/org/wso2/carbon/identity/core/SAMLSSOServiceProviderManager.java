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
import org.osgi.annotation.bundle.Capability;
import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.dao.SAMLServiceProviderPersistenceManagerFactory;
import org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderDAO;
import org.wso2.carbon.identity.core.model.SAMLSSOServiceProviderDO;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.JdbcUtils;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.security.cert.X509Certificate;

import static org.wso2.carbon.identity.core.util.JdbcUtils.isH2DB;

import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.CERTIFICATE_PROPERTY_NAME;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SQLQueries.QUERY_TO_GET_APPLICATION_CERTIFICATE_ID;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SQLQueries.QUERY_TO_GET_APPLICATION_CERTIFICATE_ID_H2;


/**
 * This class is used for managing SAML SSO providers. Adding, retrieving and removing service
 * providers are supported here.
 */
@Capability(
        namespace = "osgi.service",
        attribute = {
                "objectClass=org.wso2.carbon.identity.core.SAMLSSOServiceProviderManager",
                "service.scope=singleton"
        }
)
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
            if (LOG.isDebugEnabled()) {
                LOG.debug(serviceProviderInfo(serviceProviderDO) + " already exists.");
            }
            return false;
        }
        try {
            boolean isAdded = serviceProviderDAO.addServiceProvider(serviceProviderDO, tenantId);
            if (isAdded && LOG.isDebugEnabled()) {
                LOG.debug(serviceProviderInfo(serviceProviderDO) + " added successfully.");
            }
            return isAdded;
        } catch (IdentityException e) {
            throw new IdentityException("Error while adding " + serviceProviderInfo(serviceProviderDO), e);
        }
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
        try {
            boolean isUpdated = serviceProviderDAO.updateServiceProvider(serviceProviderDO, currentIssuer, tenantId);
            if (isUpdated && LOG.isDebugEnabled()) {
                LOG.debug(serviceProviderInfo(serviceProviderDO) + " updated successfully.");
            }
            return isUpdated;
        } catch (IdentityException e) {
            throw new IdentityException("Error while updating " + serviceProviderInfo(serviceProviderDO), e);
        }
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

        SAMLSSOServiceProviderDO serviceProviderDO = serviceProviderDAO.getServiceProvider(issuer, tenantId);

        if (serviceProviderDO != null) {
            try {
                String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
                // Load the certificate stored in the database, if signature validation is enabled.
                if (serviceProviderDO.isDoValidateSignatureInRequests() ||
                        serviceProviderDO.isDoValidateSignatureInArtifactResolve() ||
                        serviceProviderDO.isDoEnableEncryptedAssertion()) {

                    Tenant tenant;
                    if (tenantId != MultitenantConstants.SUPER_TENANT_ID) {
                        tenant = IdentityTenantUtil.getTenant(tenantId);
                    } else {
                        tenant = new Tenant();
                        tenant.setId(MultitenantConstants.SUPER_TENANT_ID);
                        tenant.setDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                    }

                    serviceProviderDO.setX509Certificate(getApplicationCertificate(serviceProviderDO, tenant));
                }
                serviceProviderDO.setTenantDomain(tenantDomain);
            } catch (DataAccessException | CertificateRetrievingException e) {
                throw new IdentityException(String.format("An error occurred while getting the " +
                        "application certificate for validating the requests from the issuer '%s'", issuer), e);
            }
        }
        return serviceProviderDO;
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

        if (issuer == null || StringUtils.isBlank(issuer)) {
            throw new IllegalArgumentException("Trying to delete issuer \'" + issuer + "\'");
        }
        if (!isServiceProviderExists(issuer, tenantId)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Service Provider with issuer: " + issuer + " does not exist.");
            }
            return false;
        }
        try {
            boolean isRemoved = serviceProviderDAO.removeServiceProvider(issuer, tenantId);
            if (isRemoved && LOG.isDebugEnabled()) {
                LOG.debug("Service Provider with issuer: " + issuer + " removed successfully.");
            }
            return isRemoved;
        } catch (IdentityException e) {
            throw new IdentityException("Error while removing Service Provider with " + issuer, e);
        }
    }

    /**
     * Upload the SAML configuration related to the application, using metadata.
     *
     * @param serviceProviderDO SAML service provider information object.
     * @param tenantId          Tenant ID.
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
            if (LOG.isDebugEnabled()) {
                LOG.debug(serviceProviderInfo(serviceProviderDO) + " already exists.");
            }
            throw new IdentityException("A Service Provider already exists.");
        }
        try {
            SAMLSSOServiceProviderDO uploadedServiceProvider =
                    serviceProviderDAO.uploadServiceProvider(serviceProviderDO, tenantId);
            if (!(uploadedServiceProvider == null) && LOG.isDebugEnabled()) {
                LOG.debug(serviceProviderInfo(serviceProviderDO) + " uploaded successfully.");
            }
            return uploadedServiceProvider;
        } catch (IdentityException e) {
            throw new IdentityException("Error while uploading " + serviceProviderInfo(serviceProviderDO), e);
        }
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

    /**
     * Returns the {@link java.security.cert.Certificate} which should used to validate the requests
     * for the given service provider.
     *
     * @param serviceProviderDO service provider information object.
     * @param tenant            tenant Domain.
     * @return The X509 certificate used to validate the requests.
     * @throws DataAccessException            If an error occurs while retrieving the certificate ID.
     * @throws CertificateRetrievingException If an error occurs while retrieving the certificate.
     */
    private X509Certificate getApplicationCertificate(SAMLSSOServiceProviderDO serviceProviderDO, Tenant tenant)
            throws CertificateRetrievingException, DataAccessException {

        // Check whether there is a certificate stored against the service provider (in the database).
        int applicationCertificateId = getApplicationCertificateId(serviceProviderDO.getIssuer(), tenant.getId());

        CertificateRetriever certificateRetriever;
        String certificateIdentifier;
        if (applicationCertificateId != -1) {
            certificateRetriever = new DatabaseCertificateRetriever();
            certificateIdentifier = Integer.toString(applicationCertificateId);
        } else {
            certificateRetriever = new KeyStoreCertificateRetriever();
            certificateIdentifier = serviceProviderDO.getCertAlias();
        }

        return certificateRetriever.getCertificate(certificateIdentifier, tenant);
    }

    /**
     * Returns the certificate reference ID for the given issuer (Service Provider) if there is one.
     *
     * @param issuer   the issuer of the service provider.
     * @param tenantId the tenant ID.
     * @return the certificate reference ID, or -1 if no certificate is found.
     * @throws DataAccessException if an error occurs while retrieving the certificate ID.
     */
    private int getApplicationCertificateId(String issuer, int tenantId) throws DataAccessException {

        NamedJdbcTemplate namedJdbcTemplate = JdbcUtils.getNewNamedJdbcTemplate();
        String sqlStmt =
                isH2DB() ? QUERY_TO_GET_APPLICATION_CERTIFICATE_ID_H2 : QUERY_TO_GET_APPLICATION_CERTIFICATE_ID;
        Integer certificateId =
                namedJdbcTemplate.fetchSingleRecord(sqlStmt, (resultSet, rowNumber) -> resultSet.getInt(1),
                        namedPreparedStatement -> {
                            namedPreparedStatement.setString(1, CERTIFICATE_PROPERTY_NAME);
                            namedPreparedStatement.setString(2, issuer);
                            namedPreparedStatement.setInt(3, tenantId);
                        });

        return certificateId != null ? certificateId : -1;
    }
}
