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

package org.wso2.carbon.identity.core.dao;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.NamedPreparedStatement;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.database.utils.jdbc.exceptions.TransactionException;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.CertificateRetriever;
import org.wso2.carbon.identity.core.CertificateRetrievingException;
import org.wso2.carbon.identity.core.DatabaseCertificateRetriever;
import org.wso2.carbon.identity.core.IdentityRegistryResources;
import org.wso2.carbon.identity.core.KeyStoreCertificateRetriever;
import org.wso2.carbon.identity.core.model.SPProperty;
import org.wso2.carbon.identity.core.model.SAMLSSOServiceProviderDO;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.JdbcUtils;
import org.wso2.carbon.user.api.Tenant;

import java.security.cert.X509Certificate;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.wso2.carbon.identity.core.util.JdbcUtils.isH2DB;

import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.ID;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.TENANT_ID;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.ISSUER;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.DEFAULT_ASSERTION_CONSUMER_URL;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.NAME_ID_FORMAT;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.CERT_ALIAS;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.REQ_SIG_VALIDATION;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.SIGN_RESPONSE;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.SIGNING_ALGO;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.DIGEST_ALGO;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.ENCRYPT_ASSERTION;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.ASSERTION_ENCRYPTION_ALGO;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.KEY_ENCRYPTION_ALGO;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.ATTR_PROFILE_ENABLED;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.ATTR_SERVICE_INDEX;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.SLO_PROFILE_ENABLED;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.SLO_METHOD;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.SLO_RESPONSE_URL;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.SLO_REQUEST_URL;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.IDP_INIT_SSO_ENABLED;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.IDP_INIT_SLO_ENABLED;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.QUERY_REQUEST_PROFILE_ENABLED;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.ECP_ENABLED;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.ARTIFACT_BINDING_ENABLED;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.ARTIFACT_RESOLVE_REQ_SIG_VALIDATION;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.IDP_ENTITY_ID_ALIAS;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.ISSUER_QUALIFIER;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.SUPPORTED_ASSERTION_QUERY_REQUEST_TYPES;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.PROPERTY_NAME;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.PROPERTY_VALUE;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.SP_ID;

public class SAMLSSOServiceProviderDAOImpl implements SAMLSSOServiceProviderDAO {

    private static final Log log = LogFactory.getLog(SAMLSSOServiceProviderDAOImpl.class);
    private final int tenantId;
    private static final String CERTIFICATE_PROPERTY_NAME = "CERTIFICATE";
    private static final String QUERY_TO_GET_APPLICATION_CERTIFICATE_ID = "SELECT " +
            "META.VALUE FROM SP_INBOUND_AUTH INBOUND, SP_APP SP, SP_METADATA META WHERE SP.ID = INBOUND.APP_ID AND " +
            "SP.ID = META.SP_ID AND META.NAME = ? AND INBOUND.INBOUND_AUTH_KEY = ? AND META.TENANT_ID = ?";
    private static final String QUERY_TO_GET_APPLICATION_CERTIFICATE_ID_H2 = "SELECT " +
            "META.`VALUE` FROM SP_INBOUND_AUTH INBOUND, SP_APP SP, SP_METADATA META WHERE SP.ID = INBOUND.APP_ID AND " +
            "SP.ID = META.SP_ID AND META.NAME = ? AND INBOUND.INBOUND_AUTH_KEY = ? AND META.TENANT_ID = ?";

    public SAMLSSOServiceProviderDAOImpl(int tenantId) {

        this.tenantId = tenantId;
    }

    @Override
    public boolean addServiceProvider(SAMLSSOServiceProviderDO serviceProviderDO) throws IdentityException {

        validateServiceProvider(serviceProviderDO);
        try {
            if (processIsServiceProviderExists(serviceProviderDO.getIssuer())) {
                debugLog(serviceProviderInfo(serviceProviderDO) + " already exists.");
                return false;
            }
            NamedJdbcTemplate namedJdbcTemplate = JdbcUtils.getNewNamedJdbcTemplate();
            namedJdbcTemplate.withTransaction(template -> {
                processAddServiceProvider(serviceProviderDO);
                processAddSPProperties(serviceProviderDO);
                return null;
            });
            debugLog(serviceProviderInfo(serviceProviderDO) + " is added successfully.");
            return true;
        } catch (TransactionException | DataAccessException e) {
            String msg = "Error while adding " + serviceProviderInfo(serviceProviderDO);
            log.error(msg, e);
            throw new IdentityException(msg, e);
        }
    }

    @Override
    public boolean updateServiceProvider(SAMLSSOServiceProviderDO serviceProviderDO, String currentIssuer)
            throws IdentityException {

        validateServiceProvider(serviceProviderDO);
        String newIssuer = serviceProviderDO.getIssuer();
        boolean isIssuerUpdated = !StringUtils.equals(currentIssuer, newIssuer);

        try {
            if (isIssuerUpdated && processIsServiceProviderExists(newIssuer)) {
                debugLog(serviceProviderInfo(serviceProviderDO) + " already exists.");
                return false;
            }
            int serviceProviderId = processGetServiceProviderId(currentIssuer);
            NamedJdbcTemplate namedJdbcTemplate = JdbcUtils.getNewNamedJdbcTemplate();
            namedJdbcTemplate.withTransaction(template -> {
                processUpdateServiceProvider(serviceProviderDO, serviceProviderId);
                processUpdateSPProperties(serviceProviderDO, serviceProviderId);
                return null;
            });
            debugLog(serviceProviderInfo(serviceProviderDO) + " is updated successfully.");
            return true;
        } catch (TransactionException | DataAccessException e) {
            String msg = "Error while updating " + serviceProviderInfo(serviceProviderDO);
            log.error(msg, e);
            throw new IdentityException(msg, e);
        }
    }

    @Override
    public SAMLSSOServiceProviderDO[] getServiceProviders() throws IdentityException {

        List<SAMLSSOServiceProviderDO> serviceProvidersList;
        try {
            serviceProvidersList = processGetServiceProviders();
        } catch (DataAccessException e) {
            log.error("Error reading Service Providers", e);
            throw new IdentityException("Error reading Service Providers", e);
        }
        return serviceProvidersList.toArray(new SAMLSSOServiceProviderDO[0]);
    }

    @Override
    public boolean removeServiceProvider(String issuer) throws IdentityException {

        if (issuer == null || StringUtils.isEmpty(issuer.trim())) {
            throw new IllegalArgumentException("Trying to delete issuer \'" + issuer + "\'");
        }
        try {
            if (!processIsServiceProviderExists(issuer)) {
                debugLog("Service Provider with issuer " + issuer + " does not exist.");
                return false;
            }
            processDeleteServiceProvider(issuer);
            return true;
        } catch (DataAccessException e) {
            String msg = "Error removing the service provider with name: " + issuer;
            log.error(msg, e);
            throw new IdentityException(msg, e);
        }
    }

    @Override
    public SAMLSSOServiceProviderDO getServiceProvider(String issuer) throws IdentityException {

        SAMLSSOServiceProviderDO serviceProviderDO = null;

        try {
            if (isServiceProviderExists(issuer)) {
                serviceProviderDO = processGetServiceProvider(issuer);
            }
        } catch (DataAccessException e) {
            throw IdentityException.error(String.format(
                            "An error occurred while retrieving the " + "the service provider with the issuer '%s'", issuer),
                    e);
        }
        if (serviceProviderDO == null) {
            return null;
        }
        try {
            String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
            // Load the certificate stored in the database, if signature validation is enabled.
            if (serviceProviderDO.isDoValidateSignatureInRequests() ||
                    serviceProviderDO.isDoValidateSignatureInArtifactResolve() ||
                    serviceProviderDO.isDoEnableEncryptedAssertion()) {

                Tenant tenant = IdentityTenantUtil.getTenant(tenantId);
                serviceProviderDO.setX509Certificate(getApplicationCertificate(serviceProviderDO, tenant));
            }
            serviceProviderDO.setTenantDomain(tenantDomain);
        } catch (DataAccessException | CertificateRetrievingException e) {
            throw IdentityException.error(String.format("An error occurred while getting the " +
                    "application certificate for validating the requests from the issuer '%s'", issuer), e);
        }
        return serviceProviderDO;
    }

    @Override
    public boolean isServiceProviderExists(String issuer) throws IdentityException {

        try {
            return processIsServiceProviderExists(issuer);
        } catch (DataAccessException e) {
            String msg = "Error while checking existence of Service Provider with issuer: " + issuer;
            log.error(msg, e);
            throw new IdentityException(msg, e);
        }
    }

    @Override
    public SAMLSSOServiceProviderDO uploadServiceProvider(SAMLSSOServiceProviderDO serviceProviderDO)
            throws IdentityException {

        addServiceProvider(serviceProviderDO);
        return serviceProviderDO;
    }

    private void debugLog(String message) {

        if (log.isDebugEnabled()) {
            log.debug(message);
        }
    }

    private boolean processIsServiceProviderExists(String issuer) throws DataAccessException {

        NamedJdbcTemplate namedJdbcTemplate = JdbcUtils.getNewNamedJdbcTemplate();
        Integer serviceProviderId =
                namedJdbcTemplate.fetchSingleRecord(SAMLSSOServiceProviderConstants.SQLQueries.GET_SAML_SP_ID_BY_ISSUER,
                        (resultSet, rowNumber) -> resultSet.getInt(ID), preparedStatement -> {
                            preparedStatement.setString(ISSUER, issuer);
                            preparedStatement.setInt(TENANT_ID, tenantId);
                        });
        return serviceProviderId != null;
    }

    private void validateServiceProvider(SAMLSSOServiceProviderDO serviceProviderDO) throws IdentityException {

        if (serviceProviderDO == null || serviceProviderDO.getIssuer() == null ||
                StringUtils.isBlank(serviceProviderDO.getIssuer())) {
            throw new IdentityException("Issuer cannot be found in the provided arguments.");
        }

        if (StringUtils.isNotBlank(serviceProviderDO.getIssuerQualifier())) {
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

        return StringUtils.substringBeforeLast(issuerWithQualifier, IdentityRegistryResources.QUALIFIER_ID);
    }

    private SAMLSSOServiceProviderDO resourceToObject(ResultSet resultSet) throws SQLException {

        SAMLSSOServiceProviderDO serviceProviderDO = new SAMLSSOServiceProviderDO();

        serviceProviderDO.setIssuer(resultSet.getString(ISSUER));
        serviceProviderDO.setDefaultAssertionConsumerUrl(resultSet.getString(DEFAULT_ASSERTION_CONSUMER_URL));
        serviceProviderDO.setNameIDFormat(resultSet.getString(NAME_ID_FORMAT));
        serviceProviderDO.setCertAlias(resultSet.getString(CERT_ALIAS));
        serviceProviderDO.setDoValidateSignatureInRequests(resultSet.getBoolean(REQ_SIG_VALIDATION));
        serviceProviderDO.setDoSignResponse(resultSet.getBoolean(SIGN_RESPONSE));
        serviceProviderDO.setSigningAlgorithmUri(resultSet.getString(SIGNING_ALGO));
        serviceProviderDO.setDigestAlgorithmUri(resultSet.getString(DIGEST_ALGO));
        serviceProviderDO.setDoEnableEncryptedAssertion(resultSet.getBoolean(ENCRYPT_ASSERTION));
        serviceProviderDO.setAssertionEncryptionAlgorithmUri(resultSet.getString(ASSERTION_ENCRYPTION_ALGO));
        serviceProviderDO.setKeyEncryptionAlgorithmUri(resultSet.getString(KEY_ENCRYPTION_ALGO));
        serviceProviderDO.setEnableAttributesByDefault(resultSet.getBoolean(ATTR_PROFILE_ENABLED));
        serviceProviderDO.setAttributeConsumingServiceIndex(resultSet.getString(ATTR_SERVICE_INDEX));
        serviceProviderDO.setDoSingleLogout(resultSet.getBoolean(SLO_PROFILE_ENABLED));
        serviceProviderDO.setSloResponseURL(resultSet.getString(SLO_RESPONSE_URL));
        serviceProviderDO.setSloRequestURL(resultSet.getString(SLO_REQUEST_URL));
        serviceProviderDO.setIdPInitSSOEnabled(resultSet.getBoolean(IDP_INIT_SSO_ENABLED));
        serviceProviderDO.setIdPInitSLOEnabled(resultSet.getBoolean(IDP_INIT_SLO_ENABLED));
        serviceProviderDO.setAssertionQueryRequestProfileEnabled(resultSet.getBoolean(QUERY_REQUEST_PROFILE_ENABLED));
        serviceProviderDO.setSamlECP(resultSet.getBoolean(ECP_ENABLED));
        serviceProviderDO.setEnableSAML2ArtifactBinding(resultSet.getBoolean(ARTIFACT_BINDING_ENABLED));
        serviceProviderDO.setDoValidateSignatureInArtifactResolve(
                resultSet.getBoolean(ARTIFACT_RESOLVE_REQ_SIG_VALIDATION));
        serviceProviderDO.setIdpEntityIDAlias(resultSet.getString(IDP_ENTITY_ID_ALIAS));
        serviceProviderDO.setIssuerQualifier(resultSet.getString(ISSUER_QUALIFIER));
        serviceProviderDO.setSupportedAssertionQueryRequestTypes(
                resultSet.getString(SUPPORTED_ASSERTION_QUERY_REQUEST_TYPES));
        serviceProviderDO.setDoFrontChannelLogout(!"BackChannel".equals(resultSet.getString(SLO_METHOD)));
        if (serviceProviderDO.isDoFrontChannelLogout()) {
            serviceProviderDO.setFrontChannelLogoutBinding(resultSet.getString(SLO_METHOD));
        }
        serviceProviderDO.setDoSignAssertions(Boolean.TRUE);

        return serviceProviderDO;
    }

    private void addProperties(int serviceProviderId, SAMLSSOServiceProviderDO serviceProviderDO)
            throws DataAccessException {

        NamedJdbcTemplate namedJdbcTemplate = JdbcUtils.getNewNamedJdbcTemplate();
        List<SPProperty> properties =
                namedJdbcTemplate.executeQuery(SAMLSSOServiceProviderConstants.SQLQueries.GET_SAML_SSO_ATTR_BY_ID,
                        (resultSet, rowNumber) -> new SPProperty(resultSet.getString(PROPERTY_NAME),
                                resultSet.getString(PROPERTY_VALUE)),
                        namedPreparedStatement -> namedPreparedStatement.setInt(SP_ID, serviceProviderId));
        serviceProviderDO.addMultiValuedProperties(properties);
    }

    private void setServiceProviderParameters(NamedPreparedStatement statement,
                                              SAMLSSOServiceProviderDO serviceProviderDO) throws SQLException {

        statement.setInt(TENANT_ID, tenantId);
        statement.setString(ISSUER, serviceProviderDO.getIssuer());
        statement.setString(DEFAULT_ASSERTION_CONSUMER_URL, serviceProviderDO.getDefaultAssertionConsumerUrl());
        statement.setString(NAME_ID_FORMAT, serviceProviderDO.getNameIDFormat());
        statement.setString(CERT_ALIAS, serviceProviderDO.getCertAlias());
        statement.setBoolean(REQ_SIG_VALIDATION, serviceProviderDO.isDoValidateSignatureInRequests());
        statement.setBoolean(SIGN_RESPONSE, serviceProviderDO.isDoSignResponse());
        statement.setString(SIGNING_ALGO, serviceProviderDO.getSigningAlgorithmUri());
        statement.setString(DIGEST_ALGO, serviceProviderDO.getDigestAlgorithmUri());
        statement.setBoolean(ENCRYPT_ASSERTION, serviceProviderDO.isDoEnableEncryptedAssertion());
        statement.setString(ASSERTION_ENCRYPTION_ALGO, serviceProviderDO.getAssertionEncryptionAlgorithmUri());
        statement.setString(KEY_ENCRYPTION_ALGO, serviceProviderDO.getKeyEncryptionAlgorithmUri());
        statement.setBoolean(ATTR_PROFILE_ENABLED, serviceProviderDO.isEnableAttributesByDefault());
        statement.setString(ATTR_SERVICE_INDEX, serviceProviderDO.getAttributeConsumingServiceIndex());
        statement.setBoolean(SLO_PROFILE_ENABLED, serviceProviderDO.isDoSingleLogout());
        statement.setString(SLO_METHOD, serviceProviderDO.getSingleLogoutMethod());
        statement.setString(SLO_RESPONSE_URL, serviceProviderDO.getSloResponseURL());
        statement.setString(SLO_REQUEST_URL, serviceProviderDO.getSloRequestURL());
        statement.setBoolean(IDP_INIT_SSO_ENABLED, serviceProviderDO.isIdPInitSSOEnabled());
        statement.setBoolean(IDP_INIT_SLO_ENABLED, serviceProviderDO.isIdPInitSLOEnabled());
        statement.setBoolean(QUERY_REQUEST_PROFILE_ENABLED, serviceProviderDO.isAssertionQueryRequestProfileEnabled());
        statement.setBoolean(ECP_ENABLED, serviceProviderDO.isSamlECP());
        statement.setBoolean(ARTIFACT_BINDING_ENABLED, serviceProviderDO.isEnableSAML2ArtifactBinding());
        statement.setBoolean(ARTIFACT_RESOLVE_REQ_SIG_VALIDATION,
                serviceProviderDO.isDoValidateSignatureInArtifactResolve());
        statement.setString(IDP_ENTITY_ID_ALIAS, serviceProviderDO.getIdpEntityIDAlias());
        statement.setString(ISSUER_QUALIFIER, serviceProviderDO.getIssuerQualifier());
        statement.setString(SUPPORTED_ASSERTION_QUERY_REQUEST_TYPES,
                serviceProviderDO.getSupportedAssertionQueryRequestTypes());
    }

    private int processGetServiceProviderId(String issuer) throws DataAccessException {

        NamedJdbcTemplate namedJdbcTemplate = JdbcUtils.getNewNamedJdbcTemplate();
        Integer serviceProviderId =
                namedJdbcTemplate.fetchSingleRecord(SAMLSSOServiceProviderConstants.SQLQueries.GET_SAML_SP_ID_BY_ISSUER,
                        (resultSet, rowNumber) -> resultSet.getInt(ID), namedPreparedStatement -> {
                            namedPreparedStatement.setString(ISSUER, issuer);
                            namedPreparedStatement.setInt(TENANT_ID, tenantId);
                        });
        if (serviceProviderId == null) {
            throw new DataAccessException("No record found for the given issuer: " + issuer);
        }
        return serviceProviderId.intValue();
    }

    private void processAddServiceProvider(SAMLSSOServiceProviderDO serviceProviderDO) throws DataAccessException {

        NamedJdbcTemplate namedJdbcTemplate = JdbcUtils.getNewNamedJdbcTemplate();
        namedJdbcTemplate.executeInsert(SAMLSSOServiceProviderConstants.SQLQueries.ADD_SAML2_SSO_CONFIG,
                namedPreparedStatement -> setServiceProviderParameters(namedPreparedStatement, serviceProviderDO),
                serviceProviderDO, false);
    }

    private void processAddSPProperties(SAMLSSOServiceProviderDO serviceProviderDO) throws DataAccessException {

        List<SPProperty> properties = serviceProviderDO.getMultiValuedProperties();
        int serviceProviderId = processGetServiceProviderId(serviceProviderDO.getIssuer());

        NamedJdbcTemplate namedJdbcTemplate = JdbcUtils.getNewNamedJdbcTemplate();

        namedJdbcTemplate.executeBatchInsert(SAMLSSOServiceProviderConstants.SQLQueries.ADD_SAML_SSO_ATTR,
                (namedPreparedStatement -> {
                    for (SPProperty property : properties) {
                        namedPreparedStatement.setInt(SP_ID, serviceProviderId);
                        namedPreparedStatement.setString(PROPERTY_NAME, property.getKey());
                        namedPreparedStatement.setString(PROPERTY_VALUE, property.getValue());
                        namedPreparedStatement.addBatch();
                    }
                }), serviceProviderDO);
    }

    private void processUpdateServiceProvider(SAMLSSOServiceProviderDO serviceProviderDO, int serviceProviderId)
            throws DataAccessException {

        NamedJdbcTemplate namedJdbcTemplate = JdbcUtils.getNewNamedJdbcTemplate();
        namedJdbcTemplate.executeUpdate(SAMLSSOServiceProviderConstants.SQLQueries.UPDATE_SAML2_SSO_CONFIG,
                namedPreparedStatement -> {
                    namedPreparedStatement.setInt(ID, serviceProviderId);
                    setServiceProviderParameters(namedPreparedStatement, serviceProviderDO);
                });
    }

    private void processUpdateSPProperties(SAMLSSOServiceProviderDO serviceProviderDO, int serviceProviderId)
            throws DataAccessException {

        List<SPProperty> properties = serviceProviderDO.getMultiValuedProperties();
        NamedJdbcTemplate namedJdbcTemplate = JdbcUtils.getNewNamedJdbcTemplate();

        namedJdbcTemplate.executeUpdate(SAMLSSOServiceProviderConstants.SQLQueries.DELETE_SAML_SSO_ATTR_BY_ID,
                namedPreparedStatement -> namedPreparedStatement.setInt(SP_ID, serviceProviderId));

        namedJdbcTemplate.executeBatchInsert(SAMLSSOServiceProviderConstants.SQLQueries.ADD_SAML_SSO_ATTR,
                (namedPreparedStatement -> {
                    for (SPProperty property : properties) {
                        namedPreparedStatement.setInt(SP_ID, serviceProviderId);
                        namedPreparedStatement.setString(PROPERTY_NAME, property.getKey());
                        namedPreparedStatement.setString(PROPERTY_VALUE, property.getValue());
                        namedPreparedStatement.addBatch();
                    }
                }), serviceProviderDO);
    }

    private SAMLSSOServiceProviderDO processGetServiceProvider(String issuer) throws DataAccessException {

        NamedJdbcTemplate namedJdbcTemplate = JdbcUtils.getNewNamedJdbcTemplate();
        SAMLSSOServiceProviderDO serviceProviderDO = namedJdbcTemplate.fetchSingleRecord(
                SAMLSSOServiceProviderConstants.SQLQueries.GET_SAML2_SSO_CONFIG_BY_ISSUER,
                (resultSet, rowNumber) -> resourceToObject(resultSet), namedPreparedStatement -> {
                    namedPreparedStatement.setString(ISSUER, issuer);
                    namedPreparedStatement.setInt(TENANT_ID, tenantId);
                });

        if (serviceProviderDO != null) {
            addProperties(processGetServiceProviderId(issuer), serviceProviderDO);
        }
        return serviceProviderDO;
    }

    private List<SAMLSSOServiceProviderDO> processGetServiceProviders() throws DataAccessException {

        List<SAMLSSOServiceProviderDO> serviceProvidersList;
        NamedJdbcTemplate namedJdbcTemplate = JdbcUtils.getNewNamedJdbcTemplate();
        serviceProvidersList =
                namedJdbcTemplate.executeQuery(SAMLSSOServiceProviderConstants.SQLQueries.GET_SAML2_SSO_CONFIGS,
                        (resultSet, rowNumber) -> resourceToObject(resultSet),
                        namedPreparedStatement -> namedPreparedStatement.setInt(TENANT_ID, tenantId));

        for (SAMLSSOServiceProviderDO serviceProviderDO : serviceProvidersList) {
            addProperties(processGetServiceProviderId(serviceProviderDO.getIssuer()), serviceProviderDO);
        }
        return serviceProvidersList;
    }

    private void processDeleteServiceProvider(String issuer) throws DataAccessException {

        NamedJdbcTemplate namedJdbcTemplate = JdbcUtils.getNewNamedJdbcTemplate();

        namedJdbcTemplate.executeUpdate(SAMLSSOServiceProviderConstants.SQLQueries.DELETE_SAML2_SSO_CONFIG_BY_ISSUER,
                namedPreparedStatement -> {
                    namedPreparedStatement.setString(ISSUER, issuer);
                    namedPreparedStatement.setInt(TENANT_ID, tenantId);
                });

        namedJdbcTemplate.executeUpdate(SAMLSSOServiceProviderConstants.SQLQueries.DELETE_SAML_SSO_ATTR,
                namedPreparedStatement -> {
                    namedPreparedStatement.setString(ISSUER, issuer);
                    namedPreparedStatement.setInt(TENANT_ID, tenantId);
                });
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
            throws CertificateRetrievingException, DataAccessException {

        // Check whether there is a certificate stored against the service provider (in the database)
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
     * @param issuer
     * @return
     * @throws SQLException
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