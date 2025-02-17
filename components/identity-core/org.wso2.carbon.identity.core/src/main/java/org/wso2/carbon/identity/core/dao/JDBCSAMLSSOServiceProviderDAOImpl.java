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

import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.NamedPreparedStatement;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.database.utils.jdbc.exceptions.TransactionException;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.model.ServiceProviderProperty;
import org.wso2.carbon.identity.core.model.SAMLSSOServiceProviderDO;
import org.wso2.carbon.identity.core.util.JdbcUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.ATTR_NAME_FORMAT;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML_SCHEMA_VERSION;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.ID;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.TENANT_ID;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.ISSUER;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.DEFAULT_ASSERTION_CONSUMER_URL;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.NAME_ID_FORMAT;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.CERT_ALIAS;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.REQ_SIG_VALIDATION;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.SIGN_RESPONSE;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.SIGN_ASSERTIONS;
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
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.VERSION;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.CREATED_AT;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.UPDATED_AT;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.PROPERTY_NAME;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.PROPERTY_VALUE;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.SP_ID;

import static java.time.ZoneOffset.UTC;

/**
 * Implementation of the SAMLSSOServiceProviderDAO interface for JDBC-based persistence.
 */
public class JDBCSAMLSSOServiceProviderDAOImpl implements SAMLSSOServiceProviderDAO {

    private final Calendar CALENDAR = Calendar.getInstance(TimeZone.getTimeZone(UTC));

    public JDBCSAMLSSOServiceProviderDAOImpl() {

    }

    @Override
    public boolean addServiceProvider(SAMLSSOServiceProviderDO serviceProviderDO, int tenantId)
            throws IdentityException {

        try {
            NamedJdbcTemplate namedJdbcTemplate = JdbcUtils.getNewNamedJdbcTemplate();
            namedJdbcTemplate.withTransaction(template -> {
                processAddServiceProvider(serviceProviderDO, tenantId);
                processAddSPProperties(serviceProviderDO, tenantId);
                return null;
            });
            return true;
        } catch (TransactionException e) {
            throw new IdentityException("Error while adding SAML Service Provider.", e);
        }
    }

    @Override
    public boolean updateServiceProvider(SAMLSSOServiceProviderDO serviceProviderDO, String currentIssuer, int tenantId)
            throws IdentityException {

        try {
            int serviceProviderId = processGetServiceProviderId(currentIssuer, tenantId);
            NamedJdbcTemplate namedJdbcTemplate = JdbcUtils.getNewNamedJdbcTemplate();
            namedJdbcTemplate.withTransaction(template -> {
                processUpdateServiceProvider(serviceProviderDO, serviceProviderId, tenantId);
                processUpdateSPProperties(serviceProviderDO, serviceProviderId);
                return null;
            });
            return true;
        } catch (TransactionException | DataAccessException e) {
            throw new IdentityException("Error while updating SAML Service Provider.", e);
        }
    }

    @Override
    public SAMLSSOServiceProviderDO[] getServiceProviders(int tenantId) throws IdentityException {

        List<SAMLSSOServiceProviderDO> serviceProvidersList;
        try {
            NamedJdbcTemplate namedJdbcTemplate = JdbcUtils.getNewNamedJdbcTemplate();
            serviceProvidersList =
                    namedJdbcTemplate.executeQuery(SAMLSSOServiceProviderConstants.SQLQueries.GET_SAML2_SSO_CONFIGS,
                            (resultSet, rowNumber) -> resourceToObject(resultSet),
                            namedPreparedStatement -> namedPreparedStatement.setInt(TENANT_ID, tenantId));

            for (SAMLSSOServiceProviderDO serviceProviderDO : serviceProvidersList) {
                populateProperties(processGetServiceProviderId(serviceProviderDO.getIssuer(), tenantId), serviceProviderDO);
            }
        } catch (DataAccessException e) {
            throw new IdentityException("Error reading Service Providers", e);
        }
        return serviceProvidersList.toArray(new SAMLSSOServiceProviderDO[0]);
    }

    @Override
    public boolean removeServiceProvider(String issuer, int tenantId) throws IdentityException {

        try {
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
            return true;
        } catch (DataAccessException e) {
            throw new IdentityException("Error while removing SAML Service Provider.", e);        }
    }

    @Override
    public SAMLSSOServiceProviderDO getServiceProvider(String issuer, int tenantId) throws IdentityException {

        SAMLSSOServiceProviderDO serviceProviderDO = null;

        try {
            NamedJdbcTemplate namedJdbcTemplate = JdbcUtils.getNewNamedJdbcTemplate();
            serviceProviderDO = namedJdbcTemplate.fetchSingleRecord(
                    SAMLSSOServiceProviderConstants.SQLQueries.GET_SAML2_SSO_CONFIG_BY_ISSUER,
                    (resultSet, rowNumber) -> resourceToObject(resultSet), namedPreparedStatement -> {
                        namedPreparedStatement.setString(ISSUER, issuer);
                        namedPreparedStatement.setInt(TENANT_ID, tenantId);
                    });

            if (serviceProviderDO != null) {
                populateProperties(processGetServiceProviderId(issuer, tenantId), serviceProviderDO);
            }
        } catch (DataAccessException e) {
            throw IdentityException.error(String.format(
                            "An error occurred while retrieving the " + "the service provider with the issuer '%s'", issuer),
                    e);
        }
        return serviceProviderDO;
    }

    @Override
    public boolean isServiceProviderExists(String issuer, int tenantId) throws IdentityException {

        try {
            NamedJdbcTemplate namedJdbcTemplate = JdbcUtils.getNewNamedJdbcTemplate();
            Integer serviceProviderId =
                    namedJdbcTemplate.fetchSingleRecord(SAMLSSOServiceProviderConstants.SQLQueries.GET_SAML_SP_ID_BY_ISSUER,
                            (resultSet, rowNumber) -> resultSet.getInt(ID), preparedStatement -> {
                                preparedStatement.setString(ISSUER, issuer);
                                preparedStatement.setInt(TENANT_ID, tenantId);
                            });
            return serviceProviderId != null;
        } catch (DataAccessException e) {
            String msg = "Error while checking existence of Service Provider with issuer: " + issuer;
            throw new IdentityException(msg, e);
        }
    }

    @Override
    public SAMLSSOServiceProviderDO uploadServiceProvider(SAMLSSOServiceProviderDO serviceProviderDO, int tenantId)
            throws IdentityException {

        if (addServiceProvider(serviceProviderDO, tenantId)) {
            return serviceProviderDO;
        }
        return null;
    }

    private void processAddServiceProvider(SAMLSSOServiceProviderDO serviceProviderDO,int tenantId) throws DataAccessException {

        NamedJdbcTemplate namedJdbcTemplate = JdbcUtils.getNewNamedJdbcTemplate();
        namedJdbcTemplate.executeInsert(SAMLSSOServiceProviderConstants.SQLQueries.ADD_SAML2_SSO_CONFIG,
                namedPreparedStatement -> setServiceProviderParameters(namedPreparedStatement, serviceProviderDO, tenantId),
                serviceProviderDO, false);
    }

    private void processAddSPProperties(SAMLSSOServiceProviderDO serviceProviderDO, int tenantId) throws DataAccessException {

        List<ServiceProviderProperty> properties = serviceProviderDO.getMultiValuedProperties();
        int serviceProviderId = processGetServiceProviderId(serviceProviderDO.getIssuer(), tenantId);

        NamedJdbcTemplate namedJdbcTemplate = JdbcUtils.getNewNamedJdbcTemplate();

        namedJdbcTemplate.executeBatchInsert(SAMLSSOServiceProviderConstants.SQLQueries.ADD_SAML_SSO_ATTR,
                (namedPreparedStatement -> {
                    for (ServiceProviderProperty property : properties) {
                        namedPreparedStatement.setInt(SP_ID, serviceProviderId);
                        namedPreparedStatement.setString(PROPERTY_NAME, property.getKey());
                        namedPreparedStatement.setString(PROPERTY_VALUE, property.getValue());
                        namedPreparedStatement.addBatch();
                    }
                }), serviceProviderDO);
    }

    private void processUpdateServiceProvider(SAMLSSOServiceProviderDO serviceProviderDO, int serviceProviderId, int tenantId)
            throws DataAccessException {

        NamedJdbcTemplate namedJdbcTemplate = JdbcUtils.getNewNamedJdbcTemplate();
        namedJdbcTemplate.executeUpdate(SAMLSSOServiceProviderConstants.SQLQueries.UPDATE_SAML2_SSO_CONFIG,
                namedPreparedStatement -> {
                    namedPreparedStatement.setInt(ID, serviceProviderId);
                    setUpdateServiceProviderParameters(namedPreparedStatement, serviceProviderDO, tenantId);
                });
    }

    private void processUpdateSPProperties(SAMLSSOServiceProviderDO serviceProviderDO, int serviceProviderId)
            throws DataAccessException {

        List<ServiceProviderProperty> properties = serviceProviderDO.getMultiValuedProperties();
        NamedJdbcTemplate namedJdbcTemplate = JdbcUtils.getNewNamedJdbcTemplate();

        namedJdbcTemplate.executeUpdate(SAMLSSOServiceProviderConstants.SQLQueries.DELETE_SAML_SSO_ATTR_BY_ID,
                namedPreparedStatement -> namedPreparedStatement.setInt(SP_ID, serviceProviderId));

        namedJdbcTemplate.executeBatchInsert(SAMLSSOServiceProviderConstants.SQLQueries.ADD_SAML_SSO_ATTR,
                (namedPreparedStatement -> {
                    for (ServiceProviderProperty property : properties) {
                        namedPreparedStatement.setInt(SP_ID, serviceProviderId);
                        namedPreparedStatement.setString(PROPERTY_NAME, property.getKey());
                        namedPreparedStatement.setString(PROPERTY_VALUE, property.getValue());
                        namedPreparedStatement.addBatch();
                    }
                }), serviceProviderDO);
    }

    private int processGetServiceProviderId(String issuer, int tenantId) throws DataAccessException {

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

    private void populateProperties(int serviceProviderId, SAMLSSOServiceProviderDO serviceProviderDO)
            throws DataAccessException {

        NamedJdbcTemplate namedJdbcTemplate = JdbcUtils.getNewNamedJdbcTemplate();
        List<ServiceProviderProperty> properties =
                namedJdbcTemplate.executeQuery(SAMLSSOServiceProviderConstants.SQLQueries.GET_SAML_SSO_ATTR_BY_ID,
                        (resultSet, rowNumber) -> new ServiceProviderProperty(resultSet.getString(PROPERTY_NAME),
                                resultSet.getString(PROPERTY_VALUE)),
                        namedPreparedStatement -> namedPreparedStatement.setInt(SP_ID, serviceProviderId));
        serviceProviderDO.addMultiValuedProperties(properties);
    }

    private void setUpdateServiceProviderParameters(NamedPreparedStatement statement,
                                                    SAMLSSOServiceProviderDO serviceProviderDO, int tenantId) throws SQLException {

        statement.setInt(TENANT_ID, tenantId);
        statement.setString(ISSUER, serviceProviderDO.getIssuer());
        statement.setString(DEFAULT_ASSERTION_CONSUMER_URL, serviceProviderDO.getDefaultAssertionConsumerUrl());
        statement.setString(NAME_ID_FORMAT, serviceProviderDO.getNameIDFormat());
        statement.setString(CERT_ALIAS, serviceProviderDO.getCertAlias());
        statement.setBoolean(REQ_SIG_VALIDATION, serviceProviderDO.isDoValidateSignatureInRequests());
        statement.setBoolean(SIGN_RESPONSE, serviceProviderDO.isDoSignResponse());
        statement.setBoolean(SIGN_ASSERTIONS, serviceProviderDO.isDoSignAssertions());
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
        statement.setTimeStamp(UPDATED_AT, new Timestamp(new Date().getTime()), CALENDAR);
        statement.setString(ATTR_NAME_FORMAT, serviceProviderDO.getAttributeNameFormat());
    }

    private void setServiceProviderParameters(NamedPreparedStatement statement,
                                              SAMLSSOServiceProviderDO serviceProviderDO, int tenantId) throws SQLException {

        Timestamp currentTime = new Timestamp(new Date().getTime());
        statement.setInt(TENANT_ID, tenantId);
        statement.setString(ISSUER, serviceProviderDO.getIssuer());
        statement.setString(DEFAULT_ASSERTION_CONSUMER_URL, serviceProviderDO.getDefaultAssertionConsumerUrl());
        statement.setString(NAME_ID_FORMAT, serviceProviderDO.getNameIDFormat());
        statement.setString(CERT_ALIAS, serviceProviderDO.getCertAlias());
        statement.setBoolean(REQ_SIG_VALIDATION, serviceProviderDO.isDoValidateSignatureInRequests());
        statement.setBoolean(SIGN_RESPONSE, serviceProviderDO.isDoSignResponse());
        statement.setBoolean(SIGN_ASSERTIONS, serviceProviderDO.isDoSignAssertions());
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
        statement.setString(VERSION, SAML_SCHEMA_VERSION);
        statement.setTimeStamp(CREATED_AT, currentTime, CALENDAR);
        statement.setTimeStamp(UPDATED_AT, currentTime, CALENDAR);
        statement.setString(ATTR_NAME_FORMAT, serviceProviderDO.getAttributeNameFormat());
    }

    private SAMLSSOServiceProviderDO resourceToObject(ResultSet resultSet) throws SQLException {

        SAMLSSOServiceProviderDO serviceProviderDO = new SAMLSSOServiceProviderDO();

        serviceProviderDO.setIssuer(resultSet.getString(ISSUER));
        serviceProviderDO.setDefaultAssertionConsumerUrl(resultSet.getString(DEFAULT_ASSERTION_CONSUMER_URL));
        serviceProviderDO.setNameIDFormat(resultSet.getString(NAME_ID_FORMAT));
        serviceProviderDO.setCertAlias(resultSet.getString(CERT_ALIAS));
        serviceProviderDO.setDoValidateSignatureInRequests(resultSet.getBoolean(REQ_SIG_VALIDATION));
        serviceProviderDO.setDoSignResponse(resultSet.getBoolean(SIGN_RESPONSE));
        serviceProviderDO.setDoSignAssertions(resultSet.getBoolean(SIGN_ASSERTIONS));
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
        serviceProviderDO.setAttributeNameFormat(resultSet.getString(ATTR_NAME_FORMAT));

        return serviceProviderDO;
    }
}
