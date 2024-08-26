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

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.database.utils.jdbc.NamedPreparedStatement;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.CertificateRetriever;
import org.wso2.carbon.identity.core.CertificateRetrievingException;
import org.wso2.carbon.identity.core.DatabaseCertificateRetriever;
import org.wso2.carbon.identity.core.IdentityRegistryResources;
import org.wso2.carbon.identity.core.KeyStoreCertificateRetriever;
import org.wso2.carbon.identity.core.internal.IdentityCoreServiceDataHolder;
import org.wso2.carbon.identity.core.model.ConfigTuple;
import org.wso2.carbon.identity.core.model.SAMLSSOServiceProviderDO;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;

import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.wso2.carbon.identity.core.util.JdbcUtils.isH2DB;

import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.ID;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.TENANT_UUID;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.ISSUER;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.DEFAULT_ASSERTION_CONSUMER_URL;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.ISSUER_CERT_ALIAS;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.NAME_ID_FORMAT;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.SIGNING_ALGORITHM;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.DIGEST_ALGORITHM;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.ASSERTION_ENCRYPTION_ALGORITHM;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.KEY_ENCRYPTION_ALGORITHM;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.ENABLE_SINGLE_LOGOUT;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.ENABLE_SIGN_RESPONSE;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.ENABLE_ASSERTION_QUERY_REQUEST_PROFILE;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.ENABLE_SAML2_ARTIFACT_BINDING;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.ENABLE_SIGN_ASSERTIONS;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.ENABLE_ECP;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.ENABLE_ATTRIBUTES_BY_DEFAULT;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.ENABLE_IDP_INIT_SSO;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.ENABLE_IDP_INIT_SLO;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.ENABLE_ENCRYPTED_ASSERTION;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.VALIDATE_SIGNATURE_IN_REQUESTS;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.VALIDATE_SIGNATURE_IN_ARTIFACT_RESOLVE;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.SAML2_SSO_ID;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.ATTR_NAME;
import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML2TableColumns.ATTR_VALUE;

public class SAMLSSOServiceProviderDAOImpl implements SAMLSSOServiceProviderDAO {

    private static final Log log = LogFactory.getLog(SAMLSSOServiceProviderDAOImpl.class);
    private final int tenantId;

    private final String tenantUUID;

    private static final String CERTIFICATE_PROPERTY_NAME = "CERTIFICATE";
    private static final String QUERY_TO_GET_APPLICATION_CERTIFICATE_ID = "SELECT " +
            "META.VALUE FROM SP_INBOUND_AUTH INBOUND, SP_APP SP, SP_METADATA META WHERE SP.ID = INBOUND.APP_ID AND " +
            "SP.ID = META.SP_ID AND META.NAME = ? AND INBOUND.INBOUND_AUTH_KEY = ? AND META.TENANT_ID = ?";

    private static final String QUERY_TO_GET_APPLICATION_CERTIFICATE_ID_H2 = "SELECT " +
            "META.`VALUE` FROM SP_INBOUND_AUTH INBOUND, SP_APP SP, SP_METADATA META WHERE SP.ID = INBOUND.APP_ID AND " +
            "SP.ID = META.SP_ID AND META.NAME = ? AND INBOUND.INBOUND_AUTH_KEY = ? AND META.TENANT_ID = ?";

    public SAMLSSOServiceProviderDAOImpl(int tenantId) throws IdentityException {

        this.tenantId = tenantId;
        this.tenantUUID = getTenantUUID(tenantId);
    }

    private String getTenantUUID(int tenantId) throws IdentityException {
        // Super tenant does not have a tenant UUID. Therefore, set a hard coded value.
        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            // Set a hard length of 32 characters for super tenant ID.
            // This is to avoid the database column length constraint violation.
            return String.format("%1$-32d", tenantId);
        }
        if (tenantId == MultitenantConstants.INVALID_TENANT_ID) {
            throw new IdentityException("Invalid tenant id: " + tenantId);
        }
        RealmService realmService = IdentityCoreServiceDataHolder.getInstance().getRealmService();
        try {
            Tenant tenant = realmService.getTenantManager().getTenant(tenantId);
            if (tenant != null && StringUtils.isBlank(tenant.getTenantUniqueID())) {
                return tenant.getTenantUniqueID();
            }
            throw new IdentityException("Invalid tenant id: " + tenantId);
        } catch (UserStoreException e) {
            throw new IdentityException("Error occurred while getting tenant from tenant id :" + tenantId);
        }
    }

    @Override
    public boolean addServiceProvider(SAMLSSOServiceProviderDO serviceProviderDO) throws IdentityException {

        if (serviceProviderDO == null || serviceProviderDO.getIssuer() == null ||
                StringUtils.isBlank(serviceProviderDO.getIssuer())) {
            throw new IdentityException("Issuer cannot be found in the provided arguments.");
        }

        // If an issuer qualifier value is specified, it is appended to the end of the issuer value.
        if (StringUtils.isNotBlank(serviceProviderDO.getIssuerQualifier())) {
            serviceProviderDO.setIssuer(getIssuerWithQualifier(serviceProviderDO.getIssuer(),
                    serviceProviderDO.getIssuerQualifier()));
        }

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(true)) {
            try {
                // Check whether the issuer already exists.
                if (processIsServiceProviderExists(connection, serviceProviderDO.getIssuer())) {
                    if (log.isDebugEnabled()) {
                        if (StringUtils.isNotBlank(serviceProviderDO.getIssuerQualifier())) {
                            log.debug("SAML2 Service Provider already exists with the same issuer name "
                                    + getIssuerWithoutQualifier(serviceProviderDO.getIssuer()) + " and qualifier name "
                                    + serviceProviderDO.getIssuerQualifier());
                        } else {
                            log.debug("SAML2 Service Provider already exists with the same issuer name "
                                    + serviceProviderDO.getIssuer());
                        }
                    }
                    return false;
                }
                String configId = UUID.randomUUID().toString();
                processAddServiceProvider(configId, connection, serviceProviderDO);
                // Add custom properties.
                processAddCustomAttributes(configId, connection, serviceProviderDO);

                IdentityDatabaseUtil.commitTransaction(connection);
                if (log.isDebugEnabled()) {
                    if (StringUtils.isNotBlank(serviceProviderDO.getIssuerQualifier())) {
                        log.debug("SAML2 Service Provider " + serviceProviderDO.getIssuer() + " with issuer "
                                + getIssuerWithoutQualifier(serviceProviderDO.getIssuer()) + " and qualifier " +
                                serviceProviderDO.getIssuerQualifier() + " is added successfully.");
                    } else {
                        log.debug(
                                "SAML2 Service Provider " + serviceProviderDO.getIssuer() + " is added successfully.");
                    }
                }
            } catch (SQLException e) {
                IdentityDatabaseUtil.rollbackTransaction(connection);
                throw e;
            }
            return true;
        } catch (SQLException e) {
            String msg;
            if (StringUtils.isNotBlank(serviceProviderDO.getIssuerQualifier())) {
                msg = "Error while adding SAML2 Service Provider for issuer: " + getIssuerWithoutQualifier
                        (serviceProviderDO.getIssuer()) + " and qualifier name " + serviceProviderDO
                        .getIssuerQualifier();
            } else {
                msg = "Error while adding SAML2 Service Provider for issuer: " + serviceProviderDO.getIssuer();
            }
            log.error(msg, e);
            throw new IdentityException(msg, e);
        }
    }

    @Override
    public boolean updateServiceProvider(SAMLSSOServiceProviderDO serviceProviderDO, String currentIssuer)
            throws IdentityException {

        if (serviceProviderDO == null || serviceProviderDO.getIssuer() == null ||
                StringUtils.isBlank(serviceProviderDO.getIssuer())) {
            throw new IdentityException("Issuer cannot be found in the provided arguments.");
        }

        // If an issuer qualifier value is specified, it is appended to the end of the issuer value.
        if (StringUtils.isNotBlank(serviceProviderDO.getIssuerQualifier())) {
            serviceProviderDO.setIssuer(getIssuerWithQualifier(serviceProviderDO.getIssuer(),
                    serviceProviderDO.getIssuerQualifier()));
        }

        String newIssuer = serviceProviderDO.getIssuer();

        boolean isIssuerUpdated = !StringUtils.equals(currentIssuer, newIssuer);
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(true)) {
            try {
                // Check if the updated issuer value already exists.
                if (isIssuerUpdated && processIsServiceProviderExists(connection, newIssuer)) {
                    if (log.isDebugEnabled()) {
                        if (StringUtils.isNotBlank(serviceProviderDO.getIssuerQualifier())) {
                            log.debug("SAML2 Service Provider already exists with the same issuer name "
                                    + getIssuerWithoutQualifier(serviceProviderDO.getIssuer()) + " and qualifier name "
                                    + serviceProviderDO.getIssuerQualifier());
                        } else {
                            log.debug("SAML2 Service Provider already exists with the same issuer name "
                                    + serviceProviderDO.getIssuer());
                        }
                    }
                    return false;
                }
                String configId = processGetServiceProviderConfigId(connection, currentIssuer);
                // Update the resource.
                processUpdateServiceProvider(connection, serviceProviderDO, configId);
                // Update custom properties.
                processUpdateCustomAttributes(connection, serviceProviderDO, configId);
                IdentityDatabaseUtil.commitTransaction(connection);
                if (log.isDebugEnabled()) {
                    if (StringUtils.isNotBlank(serviceProviderDO.getIssuerQualifier())) {
                        log.debug("SAML2 Service Provider " + serviceProviderDO.getIssuer() + " with issuer "
                                + getIssuerWithoutQualifier(serviceProviderDO.getIssuer()) + " and qualifier " +
                                serviceProviderDO.getIssuerQualifier() + " is updated successfully.");
                    } else {
                        log.debug("SAML2 Service Provider " + serviceProviderDO.getIssuer() +
                                " is updated successfully.");
                    }
                }
                return true;
            } catch (SQLException e) {
                IdentityDatabaseUtil.rollbackTransaction(connection);
                throw e;
            }
        } catch (SQLException e) {
            String msg;
            if (StringUtils.isNotBlank(serviceProviderDO.getIssuerQualifier())) {
                msg = "Error while updating SAML2 Service Provider for issuer: " + getIssuerWithoutQualifier
                        (serviceProviderDO.getIssuer()) + " and qualifier name " + serviceProviderDO
                        .getIssuerQualifier();
            } else {
                msg = "Error while updating SAML2 Service Provider for issuer: " + serviceProviderDO.getIssuer();
            }
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
            String msg = "Error removing the service provider from with name: " + issuer;
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
            throw IdentityException.error(String.format("An error occurred while getting the " +
                    "application certificate id for validating the requests from the issuer '%s'", issuer), e);
        }
        if (serviceProviderDO == null) {
            return null;
        }

        try {
            String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
            // Load the certificate stored in the database, if signature validation is enabled..
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
    public SAMLSSOServiceProviderDO uploadServiceProvider(SAMLSSOServiceProviderDO serviceProviderDO)
            throws IdentityException {

        throw new NotImplementedException("This operation is not implemented.");

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

    // Private methods

    private boolean processIsServiceProviderExists(Connection connection, String issuer) throws SQLException {

        boolean isExist = false;

        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                SAMLSSOServiceProviderConstants.SqlQueries.GET_SAML2_SSO_CONFIG_ID_BY_ISSUER)) {
            statement.setString(ISSUER, issuer);
            statement.setString(TENANT_UUID, tenantUUID);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    isExist = true;
                }
            }
        }
        return isExist;
    }

    private String processGetServiceProviderConfigId(Connection connection, String issuer) throws SQLException {

        String configId = null;

        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                SAMLSSOServiceProviderConstants.SqlQueries.GET_SAML2_SSO_CONFIG_ID_BY_ISSUER)) {
            statement.setString(ISSUER, issuer);
            statement.setString(TENANT_UUID, tenantUUID);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    configId = resultSet.getString(ID);
                }
            }
        }
        return configId;
    }

    private void processAddServiceProvider(String configId, Connection connection,
                                           SAMLSSOServiceProviderDO serviceProviderDO)
            throws SQLException {

        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                    SAMLSSOServiceProviderConstants.SqlQueries.ADD_SAML2_SSO_CONFIG)) {
            statement.setString(ID, configId);
            statement.setString(TENANT_UUID, tenantUUID);

            statement.setString(ISSUER, serviceProviderDO.getIssuer());
            statement.setString(DEFAULT_ASSERTION_CONSUMER_URL, serviceProviderDO.getDefaultAssertionConsumerUrl());
            statement.setString(ISSUER_CERT_ALIAS, serviceProviderDO.getCertAlias());
            statement.setString(NAME_ID_FORMAT, serviceProviderDO.getNameIDFormat());
            statement.setString(SIGNING_ALGORITHM, serviceProviderDO.getSigningAlgorithmUri());
            statement.setString(DIGEST_ALGORITHM, serviceProviderDO.getDigestAlgorithmUri());
            statement.setString(ASSERTION_ENCRYPTION_ALGORITHM, serviceProviderDO.getAssertionEncryptionAlgorithmUri());
            statement.setString(KEY_ENCRYPTION_ALGORITHM, serviceProviderDO.getKeyEncryptionAlgorithmUri());

            statement.setBoolean(ENABLE_SINGLE_LOGOUT, serviceProviderDO.isDoSingleLogout());
            statement.setBoolean(ENABLE_SIGN_RESPONSE, serviceProviderDO.isDoSignResponse());
            statement.setBoolean(ENABLE_ASSERTION_QUERY_REQUEST_PROFILE, serviceProviderDO.isAssertionQueryRequestProfileEnabled());
            statement.setBoolean(ENABLE_SAML2_ARTIFACT_BINDING, serviceProviderDO.isEnableSAML2ArtifactBinding());
            statement.setBoolean(ENABLE_SIGN_ASSERTIONS, serviceProviderDO.isDoSignAssertions());
            statement.setBoolean(ENABLE_ECP, serviceProviderDO.isSamlECP());
            statement.setBoolean(ENABLE_ATTRIBUTES_BY_DEFAULT, serviceProviderDO.isEnableAttributesByDefault());
            statement.setBoolean(ENABLE_IDP_INIT_SSO, serviceProviderDO.isIdPInitSSOEnabled());
            statement.setBoolean(ENABLE_IDP_INIT_SLO, serviceProviderDO.isIdPInitSLOEnabled());
            statement.setBoolean(ENABLE_ENCRYPTED_ASSERTION, serviceProviderDO.isDoEnableEncryptedAssertion());
            statement.setBoolean(VALIDATE_SIGNATURE_IN_REQUESTS, serviceProviderDO.isDoValidateSignatureInRequests());
            statement.setBoolean(VALIDATE_SIGNATURE_IN_ARTIFACT_RESOLVE, serviceProviderDO.isDoValidateSignatureInArtifactResolve());

            statement.executeUpdate();
        }
    }

    private void processAddCustomAttributes(String configId, Connection connection,
                                            SAMLSSOServiceProviderDO serviceProviderDO) throws SQLException {

        List<ConfigTuple> customAttributes = serviceProviderDO.getCustomAttributes();

        // Add custom attributes as a batch.
        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                SAMLSSOServiceProviderConstants.SqlQueries.ADD_SAML_SSO_ATTR)) {

            for (ConfigTuple customAttribute : customAttributes) {
                String key = customAttribute.getKey();
                String value = customAttribute.getValue();
                statement.setString(ID, UUID.randomUUID().toString());
                statement.setString(SAML2_SSO_ID, configId);
                statement.setString(ATTR_NAME, key);
                statement.setString(ATTR_VALUE, value);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void processUpdateServiceProvider(Connection connection, SAMLSSOServiceProviderDO serviceProviderDO,
                                              String configId)
            throws SQLException {

        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                SAMLSSOServiceProviderConstants.SqlQueries.UPDATE_SAML2_SSO_CONFIG)) {
            statement.setString(ISSUER, serviceProviderDO.getIssuer());
            statement.setString(DEFAULT_ASSERTION_CONSUMER_URL, serviceProviderDO.getDefaultAssertionConsumerUrl());
            statement.setString(ISSUER_CERT_ALIAS, serviceProviderDO.getCertAlias());
            statement.setString(NAME_ID_FORMAT, serviceProviderDO.getNameIDFormat());
            statement.setString(SIGNING_ALGORITHM, serviceProviderDO.getSigningAlgorithmUri());
            statement.setString(DIGEST_ALGORITHM, serviceProviderDO.getDigestAlgorithmUri());
            statement.setString(ASSERTION_ENCRYPTION_ALGORITHM, serviceProviderDO.getAssertionEncryptionAlgorithmUri());
            statement.setString(KEY_ENCRYPTION_ALGORITHM, serviceProviderDO.getKeyEncryptionAlgorithmUri());

            statement.setBoolean(ENABLE_SINGLE_LOGOUT, serviceProviderDO.isDoSingleLogout());
            statement.setBoolean(ENABLE_SIGN_RESPONSE, serviceProviderDO.isDoSignResponse());
            statement.setBoolean(ENABLE_ASSERTION_QUERY_REQUEST_PROFILE, serviceProviderDO.isAssertionQueryRequestProfileEnabled());
            statement.setBoolean(ENABLE_SAML2_ARTIFACT_BINDING, serviceProviderDO.isEnableSAML2ArtifactBinding());
            statement.setBoolean(ENABLE_SIGN_ASSERTIONS, serviceProviderDO.isDoSignAssertions());
            statement.setBoolean(ENABLE_ECP, serviceProviderDO.isSamlECP());
            statement.setBoolean(ENABLE_ATTRIBUTES_BY_DEFAULT, serviceProviderDO.isEnableAttributesByDefault());
            statement.setBoolean(ENABLE_IDP_INIT_SSO, serviceProviderDO.isIdPInitSSOEnabled());
            statement.setBoolean(ENABLE_IDP_INIT_SLO, serviceProviderDO.isIdPInitSLOEnabled());
            statement.setBoolean(ENABLE_ENCRYPTED_ASSERTION, serviceProviderDO.isDoEnableEncryptedAssertion());
            statement.setBoolean(VALIDATE_SIGNATURE_IN_REQUESTS, serviceProviderDO.isDoValidateSignatureInRequests());
            statement.setBoolean(VALIDATE_SIGNATURE_IN_ARTIFACT_RESOLVE, serviceProviderDO.isDoValidateSignatureInArtifactResolve());

            statement.setString(ID, configId);
            statement.setString(TENANT_UUID, tenantUUID);

            statement.executeUpdate();
        }
    }

    private void processUpdateCustomAttributes(Connection connection, SAMLSSOServiceProviderDO serviceProviderDO,
                                               String configId) throws SQLException {

        List<ConfigTuple> customAttributes = serviceProviderDO.getCustomAttributes();

        // Delete existing custom attributes.
        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                SAMLSSOServiceProviderConstants.SqlQueries.DELETE_SAML_SSO_ATTR_BY_ID)) {
            statement.setString(SAML2_SSO_ID, configId);
            statement.executeUpdate();
        }

        // Add custom attributes as a batch.
        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                SAMLSSOServiceProviderConstants.SqlQueries.ADD_SAML_SSO_ATTR)) {
            for (ConfigTuple customAttribute : customAttributes) {
                String key = customAttribute.getKey();
                String value = customAttribute.getValue();
                statement.setString(ID, UUID.randomUUID().toString());
                statement.setString(SAML2_SSO_ID, configId);
                statement.setString(ATTR_NAME, key);
                statement.setString(ATTR_VALUE, value);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private SAMLSSOServiceProviderDO processGetServiceProvider(Connection connection, String issuer)
            throws SQLException {

        SAMLSSOServiceProviderDO serviceProviderDO = null;
        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                SAMLSSOServiceProviderConstants.SqlQueries.GET_SAML2_SSO_CONFIG_BY_ISSUER)) {
            statement.setString(ISSUER, issuer);
            statement.setString(TENANT_UUID, tenantUUID);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    serviceProviderDO = resourceToObject(resultSet);
                    // Get custom attributes.
                    serviceProviderDO.addCustomAttributes(
                            processGetCustomAttributes(connection, resultSet.getString(1)));
                }
            }
        }
        return serviceProviderDO;
    }

    private List<SAMLSSOServiceProviderDO> processGetServiceProviders(Connection connection) throws SQLException {

        List<SAMLSSOServiceProviderDO> serviceProvidersList = new ArrayList<>();
        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                SAMLSSOServiceProviderConstants.SqlQueries.GET_SAML2_SSO_CONFIGS)) {
            statement.setString(TENANT_UUID, tenantUUID);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    SAMLSSOServiceProviderDO serviceProviderDO = resourceToObject(resultSet);
                    // Get custom attributes.
                    serviceProviderDO.addCustomAttributes(
                            processGetCustomAttributes(connection, resultSet.getString(1)));
                    serviceProvidersList.add(serviceProviderDO);
                }
            }
        }
        return serviceProvidersList;
    }

    private List<ConfigTuple> processGetCustomAttributes(Connection connection, String configId) throws SQLException {

        List<ConfigTuple> customAttributes = new ArrayList<>();
        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                SAMLSSOServiceProviderConstants.SqlQueries.GET_SAML_SSO_ATTR_BY_ID)) {
            statement.setString(SAML2_SSO_ID, configId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String key = resultSet.getString(ATTR_NAME);
                    String value = resultSet.getString(ATTR_VALUE);
                    customAttributes.add(new ConfigTuple(key, value));
                }
            }
        }
        return customAttributes;
    }

    private void processDeleteServiceProvider(Connection connection, String issuer) throws SQLException {

        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                SAMLSSOServiceProviderConstants.SqlQueries.DELETE_SAML2_SSO_CONFIG_BY_ISSUER)) {
            statement.setString(ISSUER, issuer);
            statement.setString(TENANT_UUID, tenantUUID);
            statement.executeUpdate();
        }

        // Delete custom attributes.
        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                SAMLSSOServiceProviderConstants.SqlQueries.DELETE_SAML_SSO_ATTR)) {
            statement.setString(ISSUER, issuer);
            statement.setString(TENANT_UUID, tenantUUID);
            statement.executeUpdate();
        }
    }

    private SAMLSSOServiceProviderDO resourceToObject(ResultSet resultSet) throws SQLException {

        SAMLSSOServiceProviderDO serviceProviderDO = new SAMLSSOServiceProviderDO();

        serviceProviderDO.setIssuer(resultSet.getString(ISSUER));
        serviceProviderDO.setDefaultAssertionConsumerUrl(resultSet.getString(DEFAULT_ASSERTION_CONSUMER_URL));
        serviceProviderDO.setCertAlias(resultSet.getString(ISSUER_CERT_ALIAS));
        serviceProviderDO.setNameIDFormat(resultSet.getString(NAME_ID_FORMAT));
        serviceProviderDO.setSigningAlgorithmUri(resultSet.getString(SIGNING_ALGORITHM));
        serviceProviderDO.setDigestAlgorithmUri(resultSet.getString(DIGEST_ALGORITHM));
        serviceProviderDO.setAssertionEncryptionAlgorithmUri(resultSet.getString(ASSERTION_ENCRYPTION_ALGORITHM));
        serviceProviderDO.setKeyEncryptionAlgorithmUri(resultSet.getString(KEY_ENCRYPTION_ALGORITHM));

        serviceProviderDO.setDoSingleLogout(resultSet.getBoolean(ENABLE_SINGLE_LOGOUT));
        serviceProviderDO.setDoSignResponse(resultSet.getBoolean(ENABLE_SIGN_RESPONSE));
        serviceProviderDO.setAssertionQueryRequestProfileEnabled(resultSet.getBoolean(ENABLE_ASSERTION_QUERY_REQUEST_PROFILE));
        serviceProviderDO.setEnableSAML2ArtifactBinding(resultSet.getBoolean(ENABLE_SAML2_ARTIFACT_BINDING));
        serviceProviderDO.setDoSignAssertions(resultSet.getBoolean(ENABLE_SIGN_ASSERTIONS));
        serviceProviderDO.setSamlECP(resultSet.getBoolean(ENABLE_ECP));
        serviceProviderDO.setEnableAttributesByDefault(resultSet.getBoolean(ENABLE_ATTRIBUTES_BY_DEFAULT));
        serviceProviderDO.setIdPInitSSOEnabled(resultSet.getBoolean(ENABLE_IDP_INIT_SSO));
        serviceProviderDO.setIdPInitSLOEnabled(resultSet.getBoolean(ENABLE_IDP_INIT_SLO));
        serviceProviderDO.setDoEnableEncryptedAssertion(resultSet.getBoolean(ENABLE_ENCRYPTED_ASSERTION));
        serviceProviderDO.setDoValidateSignatureInRequests(resultSet.getBoolean(VALIDATE_SIGNATURE_IN_REQUESTS));
        serviceProviderDO.setDoValidateSignatureInArtifactResolve(resultSet.getBoolean(VALIDATE_SIGNATURE_IN_ARTIFACT_RESOLVE));

        return serviceProviderDO;
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
