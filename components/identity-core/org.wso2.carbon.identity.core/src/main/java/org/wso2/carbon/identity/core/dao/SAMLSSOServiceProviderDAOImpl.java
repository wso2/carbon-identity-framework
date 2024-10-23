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
import org.wso2.carbon.database.utils.jdbc.NamedPreparedStatement;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.CertificateRetriever;
import org.wso2.carbon.identity.core.CertificateRetrievingException;
import org.wso2.carbon.identity.core.DatabaseCertificateRetriever;
import org.wso2.carbon.identity.core.IdentityRegistryResources;
import org.wso2.carbon.identity.core.KeyStoreCertificateRetriever;
import org.wso2.carbon.identity.core.model.ConfigTuple;
import org.wso2.carbon.identity.core.model.SAMLSSOServiceProviderDO;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.user.api.Tenant;

import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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

    public SAMLSSOServiceProviderDAOImpl(int tenantId) throws IdentityException {

        this.tenantId = tenantId;
    }

    @Override
    public boolean addServiceProvider(SAMLSSOServiceProviderDO serviceProviderDO) throws IdentityException {

        validateServiceProvider(serviceProviderDO);

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(true)) {
            try {
                if (processIsServiceProviderExists(connection, serviceProviderDO.getIssuer())) {
                    if (log.isDebugEnabled()) {
                        log.debug(serviceProviderInfo(serviceProviderDO) + " already exists.");
                    }
                    return false;
                }
                processAddServiceProvider(connection, serviceProviderDO);
                processAddSPProperties(connection, serviceProviderDO);

                IdentityDatabaseUtil.commitTransaction(connection);
                if (log.isDebugEnabled()) {
                    log.debug(serviceProviderInfo(serviceProviderDO) + " is added successfully.");
                }
            } catch (SQLException e) {
                IdentityDatabaseUtil.rollbackTransaction(connection);
                throw e;
            }
            return true;
        } catch (SQLException e) {
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
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(true)) {
            try {
                if (isIssuerUpdated && processIsServiceProviderExists(connection, newIssuer)) {
                    if (log.isDebugEnabled()) {
                        log.debug(serviceProviderInfo(serviceProviderDO) + " already exists.");
                    }
                    return false;
                }
                int serviceProviderId = processGetServiceProviderId(connection, currentIssuer);
                processUpdateServiceProvider(connection, serviceProviderDO, serviceProviderId);
                processUpdateSPProperties(connection, serviceProviderDO, serviceProviderId);
                IdentityDatabaseUtil.commitTransaction(connection);
                if (log.isDebugEnabled()) {
                    log.debug(serviceProviderInfo(serviceProviderDO) + " is updated successfully.");
                }
                return true;
            } catch (SQLException e) {
                IdentityDatabaseUtil.rollbackTransaction(connection);
                throw e;
            }
        } catch (SQLException e) {
            String msg = "Error while updating " + serviceProviderInfo(serviceProviderDO);
            log.error(msg, e);
            throw new IdentityException(msg, e);
        }
    }

    @Override
    public SAMLSSOServiceProviderDO[] getServiceProviders() throws IdentityException {

        List<SAMLSSOServiceProviderDO> serviceProvidersList;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            serviceProvidersList = processGetServiceProviders(connection);
        } catch (SQLException e) {
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

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            if (!processIsServiceProviderExists(connection, issuer)) {
                if (log.isDebugEnabled()) {
                    log.debug("Service Provider with issuer " + issuer + " does not exist.");
                }
                return false;
            }
            processDeleteServiceProvider(connection, issuer);
            return true;
        } catch (SQLException e) {
            String msg = "Error removing the service provider with name: " + issuer;
            log.error(msg, e);
            throw new IdentityException(msg, e);
        }
    }

    @Override
    public SAMLSSOServiceProviderDO getServiceProvider(String issuer) throws IdentityException {

        SAMLSSOServiceProviderDO serviceProviderDO = null;

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            if (isServiceProviderExists(issuer)) {
                serviceProviderDO = processGetServiceProvider(connection, issuer);
            }
        } catch (SQLException e) {
            throw IdentityException.error(String.format("An error occurred while retrieving the " +
                    "the service provider with the issuer '%s'", issuer), e);
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
        } catch (SQLException | CertificateRetrievingException e) {
            throw IdentityException.error(String.format("An error occurred while getting the " +
                    "application certificate for validating the requests from the issuer '%s'", issuer), e);
        }
        return serviceProviderDO;
    }

    @Override
    public boolean isServiceProviderExists(String issuer) throws IdentityException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            return processIsServiceProviderExists(connection, issuer);
        } catch (SQLException e) {
            String msg = "Error while checking existence of Service Provider with issuer: " + issuer;
            log.error(msg, e);
            throw new IdentityException(msg, e);
        }
    }

    @Override
    public SAMLSSOServiceProviderDO uploadServiceProvider(SAMLSSOServiceProviderDO serviceProviderDO)
            throws IdentityException {

        validateServiceProvider(serviceProviderDO);
        if (serviceProviderDO.getDefaultAssertionConsumerUrl() == null || StringUtils.isBlank(
                serviceProviderDO.getDefaultAssertionConsumerUrl())) {
            throw new IdentityException("No default assertion consumer URL provided for service provider :" +
                    serviceProviderDO.getIssuer());
        }

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(true)) {
            try {
                if (processIsServiceProviderExists(connection, serviceProviderDO.getIssuer())) {
                    if (log.isDebugEnabled()) {
                        log.debug(serviceProviderInfo(serviceProviderDO) + " already exists.");
                    }
                    throw IdentityException.error("A Service Provider already exists.");
                }

                processAddServiceProvider(connection, serviceProviderDO);
                processAddSPProperties(connection, serviceProviderDO);

                IdentityDatabaseUtil.commitTransaction(connection);
                if (log.isDebugEnabled()) {
                    log.debug(serviceProviderInfo(serviceProviderDO) + " is added successfully.");
                }
                return serviceProviderDO;
            } catch (SQLException e) {
                IdentityDatabaseUtil.rollbackTransaction(connection);
                throw e;
            }
        } catch (SQLException e) {
            String msg = "Error while adding " + serviceProviderInfo(serviceProviderDO);
            log.error(msg, e);
            throw new IdentityException(msg, e);
        }
    }

    private boolean processIsServiceProviderExists(Connection connection, String issuer) throws SQLException {

        boolean isExist = false;

        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                SAMLSSOServiceProviderConstants.SQLQueries.GET_SAML_SP_ID_BY_ISSUER)) {
            statement.setString(ISSUER, issuer);
            statement.setInt(TENANT_ID, tenantId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    isExist = true;
                }
            }
        }
        return isExist;
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
            return "SAML2 Service Provider with issuer: " + getIssuerWithoutQualifier
                    (serviceProviderDO.getIssuer()) + " and qualifier name " + serviceProviderDO
                    .getIssuerQualifier();
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

    private void addProperties(Connection connection, int serviceProviderId,
                               SAMLSSOServiceProviderDO serviceProviderDO) throws SQLException {

        List<ConfigTuple> properties = new ArrayList<>();
        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                SAMLSSOServiceProviderConstants.SQLQueries.GET_SAML_SSO_ATTR_BY_ID)) {
            statement.setInt(SP_ID, serviceProviderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String key = resultSet.getString(PROPERTY_NAME);
                    String value = resultSet.getString(PROPERTY_VALUE);
                    properties.add(new ConfigTuple(key, value));
                }
                serviceProviderDO.addCustomAttributes(properties);
            }
        }
    }

    private void setServiceProviderParameters(NamedPreparedStatement statement,
                                              SAMLSSOServiceProviderDO serviceProviderDO)
            throws SQLException {

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
        statement.setBoolean(QUERY_REQUEST_PROFILE_ENABLED,
                serviceProviderDO.isAssertionQueryRequestProfileEnabled());
        statement.setBoolean(ECP_ENABLED, serviceProviderDO.isSamlECP());
        statement.setBoolean(ARTIFACT_BINDING_ENABLED, serviceProviderDO.isEnableSAML2ArtifactBinding());
        statement.setBoolean(ARTIFACT_RESOLVE_REQ_SIG_VALIDATION,
                serviceProviderDO.isDoValidateSignatureInArtifactResolve());
        statement.setString(IDP_ENTITY_ID_ALIAS, serviceProviderDO.getIdpEntityIDAlias());
        statement.setString(ISSUER_QUALIFIER, serviceProviderDO.getIssuerQualifier());
        statement.setString(SUPPORTED_ASSERTION_QUERY_REQUEST_TYPES,
                serviceProviderDO.getSupportedAssertionQueryRequestTypes());
    }

    private int processGetServiceProviderId(Connection connection, String issuer) throws SQLException {

        int serviceProviderId;

        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                SAMLSSOServiceProviderConstants.SQLQueries.GET_SAML_SP_ID_BY_ISSUER)) {
            statement.setString(ISSUER, issuer);
            statement.setInt(TENANT_ID, tenantId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    serviceProviderId = resultSet.getInt(ID);
                } else {
                    throw new SQLException("Error while retrieving the service provider ID for issuer: " + issuer);
                }
            }
        }
        return serviceProviderId;
    }

    private void processAddServiceProvider(Connection connection, SAMLSSOServiceProviderDO serviceProviderDO)
            throws SQLException {

        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                SAMLSSOServiceProviderConstants.SQLQueries.ADD_SAML2_SSO_CONFIG)) {
            setServiceProviderParameters(statement, serviceProviderDO);
            statement.executeUpdate();
        }
    }

    private void processAddSPProperties(Connection connection, SAMLSSOServiceProviderDO serviceProviderDO)
            throws SQLException {

        List<ConfigTuple> properties = serviceProviderDO.getCustomAttributes();
        int serviceProviderId = processGetServiceProviderId(connection, serviceProviderDO.getIssuer());

        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                SAMLSSOServiceProviderConstants.SQLQueries.ADD_SAML_SSO_ATTR)) {

            for (ConfigTuple property : properties) {
                String key = property.getKey();
                String value = property.getValue();
                statement.setInt(SP_ID, serviceProviderId);
                statement.setString(PROPERTY_NAME, key);
                statement.setString(PROPERTY_VALUE, value);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void processUpdateServiceProvider(Connection connection, SAMLSSOServiceProviderDO serviceProviderDO,
                                              int serviceProviderId) throws SQLException {

        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                SAMLSSOServiceProviderConstants.SQLQueries.UPDATE_SAML2_SSO_CONFIG)) {
            statement.setInt(ID, serviceProviderId);
            setServiceProviderParameters(statement, serviceProviderDO);
            statement.executeUpdate();
        }
    }

    private void processUpdateSPProperties(Connection connection, SAMLSSOServiceProviderDO serviceProviderDO,
                                           int serviceProviderId) throws SQLException {

        List<ConfigTuple> properties = serviceProviderDO.getCustomAttributes();

        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                SAMLSSOServiceProviderConstants.SQLQueries.DELETE_SAML_SSO_ATTR_BY_ID)) {
            statement.setInt(SP_ID, serviceProviderId);
            statement.executeUpdate();
        }

        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                SAMLSSOServiceProviderConstants.SQLQueries.ADD_SAML_SSO_ATTR)) {
            for (ConfigTuple property : properties) {
                String key = property.getKey();
                String value = property.getValue();
                statement.setInt(SP_ID, serviceProviderId);
                statement.setString(PROPERTY_NAME, key);
                statement.setString(PROPERTY_VALUE, value);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private SAMLSSOServiceProviderDO processGetServiceProvider(Connection connection, String issuer)
            throws SQLException {

        SAMLSSOServiceProviderDO serviceProviderDO = null;
        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                SAMLSSOServiceProviderConstants.SQLQueries.GET_SAML2_SSO_CONFIG_BY_ISSUER)) {
            statement.setString(ISSUER, issuer);
            statement.setInt(TENANT_ID, tenantId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    serviceProviderDO = resourceToObject(resultSet);
                    addProperties(connection, resultSet.getInt(1), serviceProviderDO);
                }
            }
        }
        return serviceProviderDO;
    }

    private List<SAMLSSOServiceProviderDO> processGetServiceProviders(Connection connection) throws SQLException {

        List<SAMLSSOServiceProviderDO> serviceProvidersList = new ArrayList<>();
        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                SAMLSSOServiceProviderConstants.SQLQueries.GET_SAML2_SSO_CONFIGS)) {
            statement.setInt(TENANT_ID, tenantId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    SAMLSSOServiceProviderDO serviceProviderDO = resourceToObject(resultSet);
                    addProperties(connection, resultSet.getInt(1), serviceProviderDO);
                    serviceProvidersList.add(serviceProviderDO);
                }
            }
        }
        return serviceProvidersList;
    }

    private void processDeleteServiceProvider(Connection connection, String issuer) throws SQLException {

        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                SAMLSSOServiceProviderConstants.SQLQueries.DELETE_SAML2_SSO_CONFIG_BY_ISSUER)) {
            statement.setString(ISSUER, issuer);
            statement.setInt(TENANT_ID, tenantId);
            statement.executeUpdate();
        }

        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                SAMLSSOServiceProviderConstants.SQLQueries.DELETE_SAML_SSO_ATTR)) {
            statement.setString(ISSUER, issuer);
            statement.setInt(TENANT_ID, tenantId);
            statement.executeUpdate();
        }
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
    private int getApplicationCertificateId(String issuer, int tenantId) throws SQLException {

        try {
            String sqlStmt = isH2DB() ? QUERY_TO_GET_APPLICATION_CERTIFICATE_ID_H2 :
                    QUERY_TO_GET_APPLICATION_CERTIFICATE_ID;
            try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
                 PreparedStatement statementToGetApplicationCertificate =
                         connection.prepareStatement(sqlStmt)) {
                statementToGetApplicationCertificate.setString(1, CERTIFICATE_PROPERTY_NAME);
                statementToGetApplicationCertificate.setString(2, issuer);
                statementToGetApplicationCertificate.setInt(3, tenantId);

                try (ResultSet queryResults = statementToGetApplicationCertificate.executeQuery()) {
                    if (queryResults.next()) {
                        return queryResults.getInt(1);
                    }
                }
            }
            return -1;
        } catch (DataAccessException e) {
            String errorMsg = "Error while retrieving application certificate data for issuer: " + issuer +
                    " and tenant Id: " + tenantId;
            throw new SQLException(errorMsg, e);
        }
    }
}