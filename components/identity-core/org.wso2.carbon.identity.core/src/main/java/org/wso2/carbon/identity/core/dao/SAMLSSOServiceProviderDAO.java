/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.core.dao;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.CertificateRetriever;
import org.wso2.carbon.identity.core.CertificateRetrievingException;
import org.wso2.carbon.identity.core.DatabaseCertificateRetriever;
import org.wso2.carbon.identity.core.IdentityRegistryResources;
import org.wso2.carbon.identity.core.KeyStoreCertificateRetriever;
import org.wso2.carbon.identity.core.model.SAMLSSOServiceProviderDO;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.utils.Transaction;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;

import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SAMLSSOServiceProviderDAO extends AbstractDAO<SAMLSSOServiceProviderDO> {

    private static final String CERTIFICATE_PROPERTY_NAME = "CERTIFICATE";
    private static String QUERY_TO_GET_APPLICATION_CERTIFICATE_ID = "SELECT " +
            "META.VALUE " +
            "FROM " +
            "SP_INBOUND_AUTH INBOUND," +
            "SP_APP SP," +
            "SP_METADATA META " +
            "WHERE SP.ID = INBOUND.APP_ID AND " +
            "SP.ID = META.SP_ID AND " +
            "META.NAME = ? AND " +
            "INBOUND.INBOUND_AUTH_KEY = ?";

    private static Log log = LogFactory.getLog(SAMLSSOServiceProviderDAO.class);

    public SAMLSSOServiceProviderDAO(Registry registry) {
        this.registry = registry;
    }

    protected SAMLSSOServiceProviderDO resourceToObject(Resource resource) {
        SAMLSSOServiceProviderDO serviceProviderDO = new SAMLSSOServiceProviderDO();
        serviceProviderDO.setIssuer(resource
                .getProperty(IdentityRegistryResources.PROP_SAML_SSO_ISSUER));
        serviceProviderDO.setAssertionConsumerUrls(resource.getPropertyValues(
                IdentityRegistryResources.PROP_SAML_SSO_ASSERTION_CONS_URLS));
        serviceProviderDO.setDefaultAssertionConsumerUrl(resource.getProperty(
                IdentityRegistryResources.PROP_DEFAULT_SAML_SSO_ASSERTION_CONS_URL));
        serviceProviderDO.setCertAlias(resource
                .getProperty(IdentityRegistryResources.PROP_SAML_SSO_ISSUER_CERT_ALIAS));

        if (StringUtils.isNotEmpty(resource.getProperty(IdentityRegistryResources.PROP_SAML_SSO_SIGNING_ALGORITHM))) {
            serviceProviderDO.setSigningAlgorithmUri(resource.getProperty(IdentityRegistryResources
                    .PROP_SAML_SSO_SIGNING_ALGORITHM));
        }

        if (resource.getProperty(IdentityRegistryResources.PROP_SAML_SSO_ASSERTION_QUERY_REQUEST_PROFILE_ENABLED) !=
                null) {
            serviceProviderDO.setAssertionQueryRequestProfileEnabled(Boolean.valueOf(resource.getProperty(
                    IdentityRegistryResources.PROP_SAML_SSO_ASSERTION_QUERY_REQUEST_PROFILE_ENABLED).trim()));
        }

        if (resource.getProperty(IdentityRegistryResources.PROP_SAML_SSO_SUPPORTED_ASSERTION_QUERY_REQUEST_TYPES) !=
                null) {
            serviceProviderDO.setSupportedAssertionQueryRequestTypes(resource.getProperty(
                    IdentityRegistryResources.PROP_SAML_SSO_SUPPORTED_ASSERTION_QUERY_REQUEST_TYPES).trim());
        }

        if (StringUtils.isNotEmpty(resource.getProperty(IdentityRegistryResources.PROP_SAML_SSO_DIGEST_ALGORITHM))) {
            serviceProviderDO.setDigestAlgorithmUri(resource.getProperty(IdentityRegistryResources
                    .PROP_SAML_SSO_DIGEST_ALGORITHM));
        }

        if (StringUtils.isNotEmpty(resource.getProperty(IdentityRegistryResources
                .PROP_SAML_SSO_ASSERTION_ENCRYPTION_ALGORITHM))) {
            serviceProviderDO.setAssertionEncryptionAlgorithmUri(resource.getProperty(IdentityRegistryResources
                    .PROP_SAML_SSO_ASSERTION_ENCRYPTION_ALGORITHM));
        }

        if (StringUtils.isNotEmpty(resource.getProperty(IdentityRegistryResources
                .PROP_SAML_SSO_KEY_ENCRYPTION_ALGORITHM))) {
            serviceProviderDO.setKeyEncryptionAlgorithmUri(resource.getProperty(IdentityRegistryResources
                    .PROP_SAML_SSO_KEY_ENCRYPTION_ALGORITHM));
        }

        if (resource.getProperty(IdentityRegistryResources.PROP_SAML_SSO_DO_SINGLE_LOGOUT) != null) {
            serviceProviderDO.setDoSingleLogout(Boolean.valueOf(resource.getProperty(
                    IdentityRegistryResources.PROP_SAML_SSO_DO_SINGLE_LOGOUT).trim()));
        }

        if (resource.getProperty(IdentityRegistryResources.PROP_SAML_SSO_NAMEID_FORMAT) != null) {
            serviceProviderDO.setNameIDFormat(resource.
                    getProperty(IdentityRegistryResources.PROP_SAML_SSO_NAMEID_FORMAT));
        }

        if (resource
                .getProperty(IdentityRegistryResources.PROP_SAML_SSO_ENABLE_NAMEID_CLAIMURI) != null) {
            if (Boolean.valueOf(resource.getProperty(
                    IdentityRegistryResources.PROP_SAML_SSO_ENABLE_NAMEID_CLAIMURI).trim())) {
                serviceProviderDO.setNameIdClaimUri(resource.
                        getProperty(IdentityRegistryResources.PROP_SAML_SSO_NAMEID_CLAIMURI));
            }
        }

        serviceProviderDO.setLoginPageURL(resource.
                getProperty(IdentityRegistryResources.PROP_SAML_SSO_LOGIN_PAGE_URL));

        if (resource.getProperty(IdentityRegistryResources.PROP_SAML_SSO_DO_SIGN_RESPONSE) != null) {
            serviceProviderDO.setDoSignResponse(Boolean.valueOf(resource.getProperty(
                    IdentityRegistryResources.PROP_SAML_SSO_DO_SIGN_RESPONSE).trim()));
        }

        if (serviceProviderDO.isDoSingleLogout()) {
            serviceProviderDO.setSloResponseURL(resource.getProperty(IdentityRegistryResources
                    .PROP_SAML_SLO_RESPONSE_URL));
            serviceProviderDO.setSloRequestURL(resource.getProperty(IdentityRegistryResources
                    .PROP_SAML_SLO_REQUEST_URL));
        }

        if (resource.getProperty(IdentityRegistryResources.PROP_SAML_SSO_DO_SIGN_ASSERTIONS) != null) {
            serviceProviderDO.setDoSignAssertions(Boolean.valueOf(resource.getProperty(
                    IdentityRegistryResources.PROP_SAML_SSO_DO_SIGN_ASSERTIONS).trim()));
        }

        if (resource
                .getProperty(IdentityRegistryResources.PROP_SAML_SSO_ATTRIB_CONSUMING_SERVICE_INDEX) != null) {
            serviceProviderDO
                    .setAttributeConsumingServiceIndex(resource
                            .getProperty(IdentityRegistryResources.PROP_SAML_SSO_ATTRIB_CONSUMING_SERVICE_INDEX));
        } else {
            // Specific DB's (like oracle) returns empty strings as null.
            serviceProviderDO.setAttributeConsumingServiceIndex("");
        }

        if (resource.getProperty(IdentityRegistryResources.PROP_SAML_SSO_REQUESTED_CLAIMS) != null) {
            serviceProviderDO.setRequestedClaims(resource
                    .getPropertyValues(IdentityRegistryResources.PROP_SAML_SSO_REQUESTED_CLAIMS));
        }

        if (resource.getProperty(IdentityRegistryResources.PROP_SAML_SSO_REQUESTED_AUDIENCES) != null) {
            serviceProviderDO.setRequestedAudiences(resource
                    .getPropertyValues(IdentityRegistryResources.PROP_SAML_SSO_REQUESTED_AUDIENCES));
        }

        if (resource.getProperty(IdentityRegistryResources.PROP_SAML_SSO_REQUESTED_RECIPIENTS) != null) {
            serviceProviderDO.setRequestedRecipients(resource
                    .getPropertyValues(IdentityRegistryResources.PROP_SAML_SSO_REQUESTED_RECIPIENTS));
        }

        if (resource
                .getProperty(IdentityRegistryResources.PROP_SAML_SSO_ENABLE_ATTRIBUTES_BY_DEFAULT) != null) {
            String enableAttrByDefault = resource
                    .getProperty(IdentityRegistryResources.PROP_SAML_SSO_ENABLE_ATTRIBUTES_BY_DEFAULT);
            serviceProviderDO.setEnableAttributesByDefault(Boolean.valueOf(enableAttrByDefault));
        }
        if (resource.getProperty(IdentityRegistryResources.PROP_SAML_SSO_IDP_INIT_SSO_ENABLED) != null) {
            serviceProviderDO.setIdPInitSSOEnabled(Boolean.valueOf(resource.getProperty(
                    IdentityRegistryResources.PROP_SAML_SSO_IDP_INIT_SSO_ENABLED).trim()));
        }
        if (resource.getProperty(IdentityRegistryResources.PROP_SAML_SLO_IDP_INIT_SLO_ENABLED) != null) {
            serviceProviderDO.setIdPInitSLOEnabled(Boolean.valueOf(resource.getProperty(
                    IdentityRegistryResources.PROP_SAML_SLO_IDP_INIT_SLO_ENABLED).trim()));
            if (serviceProviderDO.isIdPInitSLOEnabled() && resource.getProperty(
                    IdentityRegistryResources.PROP_SAML_IDP_INIT_SLO_RETURN_URLS) != null) {
                serviceProviderDO.setIdpInitSLOReturnToURLs(resource.getPropertyValues(
                        IdentityRegistryResources.PROP_SAML_IDP_INIT_SLO_RETURN_URLS));
            }
        }
        if (resource.getProperty(IdentityRegistryResources.PROP_SAML_SSO_ENABLE_ENCRYPTED_ASSERTION) != null) {
            serviceProviderDO.setDoEnableEncryptedAssertion(Boolean.valueOf(resource.getProperty(
                    IdentityRegistryResources.PROP_SAML_SSO_ENABLE_ENCRYPTED_ASSERTION).trim()));
        }
        if (resource.getProperty(IdentityRegistryResources.PROP_SAML_SSO_VALIDATE_SIGNATURE_IN_REQUESTS) != null) {
            serviceProviderDO.setDoValidateSignatureInRequests(Boolean.valueOf(resource.getProperty(
                    IdentityRegistryResources.PROP_SAML_SSO_VALIDATE_SIGNATURE_IN_REQUESTS).trim()));
        }
        return serviceProviderDO;
    }

    /**
     * Add the service provider information to the registry.
     * @param serviceProviderDO Service provider information object.
     * @return True if addition successful.
     * @throws IdentityException Error while persisting to the registry.
     */
    public boolean addServiceProvider(SAMLSSOServiceProviderDO serviceProviderDO) throws IdentityException {

        if (serviceProviderDO == null || serviceProviderDO.getIssuer() == null ||
                StringUtils.isBlank(serviceProviderDO.getIssuer())) {
            throw new IdentityException("Issuer cannot be found in the provided arguments.");
        }

        String path = IdentityRegistryResources.SAML_SSO_SERVICE_PROVIDERS + encodePath(serviceProviderDO.getIssuer());

        boolean isTransactionStarted = Transaction.isStarted();
        boolean isErrorOccurred = false;
        try {
            if (registry.resourceExists(path)) {
                if (log.isDebugEnabled()) {
                    log.debug("Service Provider already exists with the same issuer name"
                            + serviceProviderDO.getIssuer());
                }
                return false;
            }

            Resource resource = createResource(serviceProviderDO);
            if (!isTransactionStarted) {
                registry.beginTransaction();
            }
            registry.put(path, resource);
            if (log.isDebugEnabled()) {
                log.debug("Service Provider " + serviceProviderDO.getIssuer() + " is added successfully.");
            }
            return true;
        } catch (RegistryException e) {
            isErrorOccurred = true;
            String msg = "Error while adding Service Provider for issuer: " + serviceProviderDO.getIssuer();
            log.error(msg, e);
            throw IdentityException.error(msg, e);
        } finally {
            commitOrRollbackTransaction(isErrorOccurred);
        }
    }

    private Resource createResource(SAMLSSOServiceProviderDO serviceProviderDO) throws RegistryException {
        Resource resource;
        resource = registry.newResource();
        resource.addProperty(IdentityRegistryResources.PROP_SAML_SSO_ISSUER,
                serviceProviderDO.getIssuer());
        resource.setProperty(IdentityRegistryResources.PROP_SAML_SSO_ASSERTION_CONS_URLS,
                serviceProviderDO.getAssertionConsumerUrlList());
        resource.addProperty(IdentityRegistryResources.PROP_DEFAULT_SAML_SSO_ASSERTION_CONS_URL,
                serviceProviderDO.getDefaultAssertionConsumerUrl());
        resource.addProperty(IdentityRegistryResources.PROP_SAML_SSO_ISSUER_CERT_ALIAS,
                serviceProviderDO.getCertAlias());
        resource.addProperty(IdentityRegistryResources.PROP_SAML_SSO_LOGIN_PAGE_URL,
                serviceProviderDO.getLoginPageURL());
        resource.addProperty(
                IdentityRegistryResources.PROP_SAML_SSO_NAMEID_FORMAT,
                serviceProviderDO.getNameIDFormat());
        resource.addProperty(IdentityRegistryResources.PROP_SAML_SSO_SIGNING_ALGORITHM, serviceProviderDO
                .getSigningAlgorithmUri());
        resource.addProperty(IdentityRegistryResources.PROP_SAML_SSO_DIGEST_ALGORITHM, serviceProviderDO
                .getDigestAlgorithmUri());
        resource.addProperty(IdentityRegistryResources.PROP_SAML_SSO_ASSERTION_ENCRYPTION_ALGORITHM, serviceProviderDO
                .getAssertionEncryptionAlgorithmUri());
        resource.addProperty(IdentityRegistryResources.PROP_SAML_SSO_KEY_ENCRYPTION_ALGORITHM, serviceProviderDO
                .getKeyEncryptionAlgorithmUri());
        if (serviceProviderDO.getNameIdClaimUri() != null
                && serviceProviderDO.getNameIdClaimUri().trim().length() > 0) {
            resource.addProperty(
                    IdentityRegistryResources.PROP_SAML_SSO_ENABLE_NAMEID_CLAIMURI,
                    "true");
            resource.addProperty(
                    IdentityRegistryResources.PROP_SAML_SSO_NAMEID_CLAIMURI,
                    serviceProviderDO.getNameIdClaimUri());
        } else {
            resource.addProperty(
                    IdentityRegistryResources.PROP_SAML_SSO_ENABLE_NAMEID_CLAIMURI,
                    "false");
        }

        String doSingleLogout = String.valueOf(serviceProviderDO.isDoSingleLogout());
        resource.addProperty(IdentityRegistryResources.PROP_SAML_SSO_DO_SINGLE_LOGOUT, doSingleLogout);
        if (serviceProviderDO.isDoSingleLogout()) {
            if (StringUtils.isNotBlank(serviceProviderDO.getSloResponseURL())) {
                resource.addProperty(IdentityRegistryResources.PROP_SAML_SLO_RESPONSE_URL,
                        serviceProviderDO.getSloResponseURL());
            }
            if (StringUtils.isNotBlank(serviceProviderDO.getSloRequestURL())) {
                resource.addProperty(IdentityRegistryResources.PROP_SAML_SLO_REQUEST_URL,
                        serviceProviderDO.getSloRequestURL());
            }
        }

        String doSignResponse = String.valueOf(serviceProviderDO.isDoSignResponse());
        resource.addProperty(IdentityRegistryResources.PROP_SAML_SSO_DO_SIGN_RESPONSE,
                doSignResponse);
        String isAssertionQueryRequestProfileEnabled = String.valueOf(serviceProviderDO
                .isAssertionQueryRequestProfileEnabled());
        resource.addProperty(IdentityRegistryResources.PROP_SAML_SSO_ASSERTION_QUERY_REQUEST_PROFILE_ENABLED,
                isAssertionQueryRequestProfileEnabled);
        String supportedAssertionQueryRequestTypes = serviceProviderDO.getSupportedAssertionQueryRequestTypes();
        resource.addProperty(IdentityRegistryResources.PROP_SAML_SSO_SUPPORTED_ASSERTION_QUERY_REQUEST_TYPES,
                supportedAssertionQueryRequestTypes);
        String doSignAssertions = String.valueOf(serviceProviderDO.isDoSignAssertions());
        resource.addProperty(IdentityRegistryResources.PROP_SAML_SSO_DO_SIGN_ASSERTIONS,
                doSignAssertions);
        if (CollectionUtils.isNotEmpty(serviceProviderDO.getRequestedClaimsList())) {
            resource.setProperty(IdentityRegistryResources.PROP_SAML_SSO_REQUESTED_CLAIMS,
                    serviceProviderDO.getRequestedClaimsList());
        }
        if (serviceProviderDO.getAttributeConsumingServiceIndex() != null) {
            resource.addProperty(
                    IdentityRegistryResources.PROP_SAML_SSO_ATTRIB_CONSUMING_SERVICE_INDEX,
                    serviceProviderDO.getAttributeConsumingServiceIndex());
        }
        if (CollectionUtils.isNotEmpty(serviceProviderDO.getRequestedAudiencesList())) {
            resource.setProperty(IdentityRegistryResources.PROP_SAML_SSO_REQUESTED_AUDIENCES,
                    serviceProviderDO.getRequestedAudiencesList());
        }
        if (CollectionUtils.isNotEmpty(serviceProviderDO.getRequestedRecipientsList())) {
            resource.setProperty(IdentityRegistryResources.PROP_SAML_SSO_REQUESTED_RECIPIENTS,
                    serviceProviderDO.getRequestedRecipientsList());
        }

        String enableAttributesByDefault = String.valueOf(serviceProviderDO.isEnableAttributesByDefault());
        resource.addProperty(IdentityRegistryResources.PROP_SAML_SSO_ENABLE_ATTRIBUTES_BY_DEFAULT,
                enableAttributesByDefault);
        String idPInitSSOEnabled = String.valueOf(serviceProviderDO.isIdPInitSSOEnabled());
        resource.addProperty(IdentityRegistryResources.PROP_SAML_SSO_IDP_INIT_SSO_ENABLED,
                idPInitSSOEnabled);
        String idPInitSLOEnabled = String.valueOf(serviceProviderDO.isIdPInitSLOEnabled());
        resource.addProperty(IdentityRegistryResources.PROP_SAML_SLO_IDP_INIT_SLO_ENABLED, idPInitSLOEnabled);
        if (serviceProviderDO.isIdPInitSLOEnabled() && serviceProviderDO.getIdpInitSLOReturnToURLList().size() > 0) {
            resource.setProperty(IdentityRegistryResources.PROP_SAML_IDP_INIT_SLO_RETURN_URLS,
                    serviceProviderDO.getIdpInitSLOReturnToURLList());
        }
        String enableEncryptedAssertion = String.valueOf(serviceProviderDO.isDoEnableEncryptedAssertion());
        resource.addProperty(IdentityRegistryResources.PROP_SAML_SSO_ENABLE_ENCRYPTED_ASSERTION,
                enableEncryptedAssertion);

        String validateSignatureInRequests = String.valueOf(serviceProviderDO.isDoValidateSignatureInRequests());
        resource.addProperty(IdentityRegistryResources.PROP_SAML_SSO_VALIDATE_SIGNATURE_IN_REQUESTS,
                validateSignatureInRequests);
        return resource;
    }

    public SAMLSSOServiceProviderDO[] getServiceProviders() throws IdentityException {
        SAMLSSOServiceProviderDO[] serviceProvidersList = new SAMLSSOServiceProviderDO[0];
        try {
            if (registry.resourceExists(IdentityRegistryResources.SAML_SSO_SERVICE_PROVIDERS)) {
                String[] providers = (String[]) registry.get(
                        IdentityRegistryResources.SAML_SSO_SERVICE_PROVIDERS).getContent();
                if (providers != null) {
                    serviceProvidersList = new SAMLSSOServiceProviderDO[providers.length];
                    for (int i = 0; i < providers.length; i++) {
                        serviceProvidersList[i] = resourceToObject(registry.get(providers[i]));
                    }
                }
            }
        } catch (RegistryException e) {
            log.error("Error reading Service Providers from Registry", e);
            throw IdentityException.error("Error reading Service Providers from Registry", e);
        }
        return serviceProvidersList;
    }

    /**
     * Remove the service provider with the given name.
     * @return True if deletion success.
     * @param issuer Name of the SAML issuer.
     * @throws IdentityException Error occurred while removing the SAML service provider from registry.
     */
    public boolean removeServiceProvider(String issuer) throws IdentityException {

        if (issuer == null || StringUtils.isEmpty(issuer.trim())) {
            throw new IllegalArgumentException("Trying to delete issuer \'" + issuer + "\'");
        }

        String path = IdentityRegistryResources.SAML_SSO_SERVICE_PROVIDERS + encodePath(issuer);
        boolean isTransactionStarted = Transaction.isStarted();
        boolean isErrorOccurred = false;
        try {
            if (!registry.resourceExists(path)) {
                if (log.isDebugEnabled()) {
                    log.debug("Registry resource does not exist for the path: " + path);
                }
                return false;
            }

            // Since we are getting a global registry object, better to check whether this is a task inside already
            // started transaction.
            if (!isTransactionStarted) {
                registry.beginTransaction();
            }
            registry.delete(path);
            return true;
        } catch (RegistryException e) {
            isErrorOccurred = true;
            String msg = "Error removing the service provider from the registry with name: " + issuer;
            log.error(msg, e);
            throw IdentityException.error(msg, e);
        } finally {
            commitOrRollbackTransaction(isErrorOccurred);
        }
    }

    /**
     * Get the service provider.
     *
     * @param issuer
     * @return
     * @throws IdentityException
     */
    public SAMLSSOServiceProviderDO getServiceProvider(String issuer) throws IdentityException {

        String path = IdentityRegistryResources.SAML_SSO_SERVICE_PROVIDERS + encodePath(issuer);
        SAMLSSOServiceProviderDO serviceProviderDO = null;

        UserRegistry userRegistry = null;
        String tenantDomain = null;
        try {
            userRegistry = (UserRegistry) registry;
            tenantDomain = IdentityTenantUtil.getRealmService().getTenantManager().getDomain(userRegistry.
                    getTenantId());
            if (registry.resourceExists(path)) {
                serviceProviderDO = resourceToObject(registry.get(path));

                // Load the certificate stored in the database, if signature validation is enabled..
                if (serviceProviderDO.isDoValidateSignatureInRequests()) {
                    Tenant tenant = new Tenant();
                    tenant.setDomain(tenantDomain);
                    tenant.setId(userRegistry.getTenantId());

                    serviceProviderDO.setX509Certificate(getApplicationCertificate(serviceProviderDO, tenant));
                }
                serviceProviderDO.setTenantDomain(tenantDomain);
            }
        } catch (RegistryException e) {
            throw IdentityException.error("Error occurred while checking if resource path \'" + path + "\' exists in " +
                    "registry for tenant domain : " + tenantDomain, e);
        } catch (UserStoreException e) {
            throw IdentityException.error("Error occurred while getting tenant domain from tenant ID : " +
                    userRegistry.getTenantId(), e);
        } catch (SQLException e) {
            throw IdentityException.error(String.format("An error occurred while getting the " +
                    "application certificate id for validating the requests from the issuer '%s'", issuer), e);
        } catch (CertificateRetrievingException e) {
            throw IdentityException.error(String.format("An error occurred while getting the " +
                    "application certificate for validating the requests from the issuer '%s'", issuer), e);
        }
        return serviceProviderDO;
    }

    /**
     * Returns the {@link java.security.cert.Certificate} which should used to validate the requests
     * for the given service provider.
     *
     * @param serviceProviderDO
     * @param tenant
     * @return
     * @throws SQLException
     * @throws CertificateRetrievingException
     */
    private X509Certificate getApplicationCertificate(SAMLSSOServiceProviderDO serviceProviderDO, Tenant tenant)
            throws SQLException, CertificateRetrievingException {

        // Check whether there is a certificate stored against the service provider (in the database)
        int applicationCertificateId = getApplicationCertificateId(serviceProviderDO.getIssuer());

        CertificateRetriever certificateRetriever = null;
        String certificateIdentifier = null;
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
     * @param issuer
     * @return
     * @throws SQLException
     */
    private int getApplicationCertificateId(String issuer) throws SQLException {

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement statementToGetApplicationCertificate = null;
        ResultSet queryResults = null;

        try {
            statementToGetApplicationCertificate = connection.prepareStatement(QUERY_TO_GET_APPLICATION_CERTIFICATE_ID);
            statementToGetApplicationCertificate.setString(1, CERTIFICATE_PROPERTY_NAME);
            statementToGetApplicationCertificate.setString(2, issuer);

            queryResults = statementToGetApplicationCertificate.executeQuery();

            while (queryResults.next()) {
                return queryResults.getInt(1);
            }
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, queryResults, statementToGetApplicationCertificate);
        }

        return -1;
    }

    public boolean isServiceProviderExists(String issuer) throws IdentityException {
        String path = IdentityRegistryResources.SAML_SSO_SERVICE_PROVIDERS + encodePath(issuer);
        try {
            return registry.resourceExists(path);
        } catch (RegistryException e) {
            throw IdentityException.error("Error occurred while checking if resource path \'" + path + "\' exists in " +
                    "registry");
        }
    }

    private String encodePath(String path) {
        String encodedStr = new String(Base64.encodeBase64(path.getBytes()));
        return encodedStr.replace("=", "");
    }

    /**
     * Upload service Provider using metadata file..
     * @param serviceProviderDO Service provider information object.
     * @return True if upload success.
     * @throws IdentityException Error occurred while adding the information to registry.
     */
    public SAMLSSOServiceProviderDO uploadServiceProvider(SAMLSSOServiceProviderDO serviceProviderDO) throws
            IdentityException {

        if (serviceProviderDO == null || serviceProviderDO.getIssuer() == null) {
            throw new IdentityException("Issuer cannot be found in the provided arguments.");
        }

        if (serviceProviderDO.getDefaultAssertionConsumerUrl() == null) {
            throw new IdentityException("No default assertion consumer URL provided for service provider :" +
                    serviceProviderDO.getIssuer());
        }

        String path = IdentityRegistryResources.SAML_SSO_SERVICE_PROVIDERS + encodePath(serviceProviderDO.getIssuer());

        boolean isTransactionStarted = Transaction.isStarted();
        boolean isErrorOccurred = false;
        try {
            if (registry.resourceExists(path)) {
                if (log.isDebugEnabled()) {
                    log.debug("A Service Provider already exists with the same issuer name" + serviceProviderDO
                            .getIssuer());
                }
                throw IdentityException.error("A Service Provider already exists.");
            }

            if (!isTransactionStarted) {
                registry.beginTransaction();
            }

            Resource resource = createResource(serviceProviderDO);
            registry.put(path, resource);
            if (log.isDebugEnabled()) {
                log.debug("Service Provider " + serviceProviderDO.getIssuer() + " is added successfully.");
            }
            return serviceProviderDO;
        } catch (RegistryException e) {
            isErrorOccurred = true;
            throw IdentityException.error("Error while adding Service Provider.", e);
        } finally {
            commitOrRollbackTransaction(isErrorOccurred);
        }
    }

    /**
     * Commit or rollback the registry operation depends on the error condition.
     * @param isErrorOccurred Identifier for error transactions.
     * @throws IdentityException Error while committing or running rollback on the transaction.
     */
    private void commitOrRollbackTransaction(boolean isErrorOccurred) throws IdentityException {

        try {
            // Rollback the transaction if there is an error, Otherwise try to commit.
            if (isErrorOccurred) {
                registry.rollbackTransaction();
            } else {
                registry.commitTransaction();
            }
        } catch (RegistryException ex) {
            throw new IdentityException("Error occurred while trying to commit or rollback the registry operation.", ex);
        }
    }
}
