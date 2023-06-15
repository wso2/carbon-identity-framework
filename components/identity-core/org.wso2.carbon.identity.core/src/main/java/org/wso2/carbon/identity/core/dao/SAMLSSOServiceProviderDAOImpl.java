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
            statement.setString(1, issuer);
            statement.setString(2, tenantUUID);
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
            statement.setString(1, issuer);
            statement.setString(2, tenantUUID);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    configId = resultSet.getString(1);
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
            statement.setString(1, configId);
            statement.setString(2, tenantUUID);

            statement.setString(3, serviceProviderDO.getIssuer());
            statement.setString(4, serviceProviderDO.getDefaultAssertionConsumerUrl());
            statement.setString(5, serviceProviderDO.getCertAlias());
            statement.setString(6, serviceProviderDO.getNameIDFormat());
            statement.setString(7, serviceProviderDO.getSigningAlgorithmUri());
            statement.setString(8, serviceProviderDO.getDigestAlgorithmUri());
            statement.setString(9, serviceProviderDO.getAssertionEncryptionAlgorithmUri());
            statement.setString(10, serviceProviderDO.getKeyEncryptionAlgorithmUri());

            statement.setBoolean(11, serviceProviderDO.isDoSingleLogout());
            statement.setBoolean(12, serviceProviderDO.isDoSignResponse());
            statement.setBoolean(13, serviceProviderDO.isAssertionQueryRequestProfileEnabled());
            statement.setBoolean(14, serviceProviderDO.isEnableSAML2ArtifactBinding());
            statement.setBoolean(15, serviceProviderDO.isDoSignAssertions());
            statement.setBoolean(16, serviceProviderDO.isSamlECP());
            statement.setBoolean(17, serviceProviderDO.isEnableAttributesByDefault());
            statement.setBoolean(18, serviceProviderDO.isIdPInitSSOEnabled());
            statement.setBoolean(19, serviceProviderDO.isIdPInitSLOEnabled());
            statement.setBoolean(20, serviceProviderDO.isDoEnableEncryptedAssertion());
            statement.setBoolean(21, serviceProviderDO.isDoValidateSignatureInRequests());
            statement.setBoolean(22, serviceProviderDO.isDoValidateSignatureInArtifactResolve());

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
                statement.setString(1, UUID.randomUUID().toString());
                statement.setString(2, configId);
                statement.setString(3, key);
                statement.setString(4, value);
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
            statement.setString(1, serviceProviderDO.getIssuer());
            statement.setString(2, serviceProviderDO.getDefaultAssertionConsumerUrl());
            statement.setString(3, serviceProviderDO.getCertAlias());
            statement.setString(4, serviceProviderDO.getNameIDFormat());
            statement.setString(5, serviceProviderDO.getSigningAlgorithmUri());
            statement.setString(6, serviceProviderDO.getDigestAlgorithmUri());
            statement.setString(7, serviceProviderDO.getAssertionEncryptionAlgorithmUri());
            statement.setString(8, serviceProviderDO.getKeyEncryptionAlgorithmUri());

            statement.setBoolean(9, serviceProviderDO.isDoSingleLogout());
            statement.setBoolean(10, serviceProviderDO.isDoSignResponse());
            statement.setBoolean(11, serviceProviderDO.isAssertionQueryRequestProfileEnabled());
            statement.setBoolean(12, serviceProviderDO.isEnableSAML2ArtifactBinding());
            statement.setBoolean(13, serviceProviderDO.isDoSignAssertions());
            statement.setBoolean(14, serviceProviderDO.isSamlECP());
            statement.setBoolean(15, serviceProviderDO.isEnableAttributesByDefault());
            statement.setBoolean(16, serviceProviderDO.isIdPInitSSOEnabled());
            statement.setBoolean(17, serviceProviderDO.isIdPInitSLOEnabled());
            statement.setBoolean(18, serviceProviderDO.isDoEnableEncryptedAssertion());
            statement.setBoolean(19, serviceProviderDO.isDoValidateSignatureInRequests());
            statement.setBoolean(20, serviceProviderDO.isDoValidateSignatureInArtifactResolve());

            statement.setString(21, configId);
            statement.setString(22, tenantUUID);

            statement.executeUpdate();
        }
    }

    private void processUpdateCustomAttributes(Connection connection, SAMLSSOServiceProviderDO serviceProviderDO,
                                               String configId) throws SQLException {

        List<ConfigTuple> customAttributes = serviceProviderDO.getCustomAttributes();

        // Delete existing custom attributes.
        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                SAMLSSOServiceProviderConstants.SqlQueries.DELETE_SAML_SSO_ATTR_BY_ID)) {
            statement.setString(1, configId);
            statement.executeUpdate();
        }

        // Add custom attributes as a batch.
        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                SAMLSSOServiceProviderConstants.SqlQueries.ADD_SAML_SSO_ATTR)) {
            for (ConfigTuple customAttribute : customAttributes) {
                String key = customAttribute.getKey();
                String value = customAttribute.getValue();
                statement.setString(1, UUID.randomUUID().toString());
                statement.setString(2, configId);
                statement.setString(3, key);
                statement.setString(4, value);
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
            statement.setString(1, issuer);
            statement.setString(2, tenantUUID);
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
            statement.setString(1, tenantUUID);
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
            statement.setString(1, configId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String key = resultSet.getString(2);
                    String value = resultSet.getString(3);
                    customAttributes.add(new ConfigTuple(key, value));
                }
            }
        }
        return customAttributes;
    }

    private void processDeleteServiceProvider(Connection connection, String issuer) throws SQLException {

        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                SAMLSSOServiceProviderConstants.SqlQueries.DELETE_SAML2_SSO_CONFIG_BY_ISSUER)) {
            statement.setString(1, issuer);
            statement.setString(2, tenantUUID);
            statement.executeUpdate();
        }

        // Delete custom attributes.
        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                SAMLSSOServiceProviderConstants.SqlQueries.DELETE_SAML_SSO_ATTR)) {
            statement.setString(1, issuer);
            statement.setString(2, tenantUUID);
            statement.executeUpdate();
        }
    }

    private SAMLSSOServiceProviderDO resourceToObject(ResultSet resultSet) throws SQLException {

        SAMLSSOServiceProviderDO serviceProviderDO = new SAMLSSOServiceProviderDO();

        serviceProviderDO.setIssuer(resultSet.getString(2));
        serviceProviderDO.setDefaultAssertionConsumerUrl(resultSet.getString(3));
        serviceProviderDO.setCertAlias(resultSet.getString(4));
        serviceProviderDO.setNameIDFormat(resultSet.getString(5));
        serviceProviderDO.setSigningAlgorithmUri(resultSet.getString(6));
        serviceProviderDO.setDigestAlgorithmUri(resultSet.getString(7));
        serviceProviderDO.setAssertionEncryptionAlgorithmUri(resultSet.getString(8));
        serviceProviderDO.setKeyEncryptionAlgorithmUri(resultSet.getString(9));

        serviceProviderDO.setDoSingleLogout(resultSet.getBoolean(10));
        serviceProviderDO.setDoSignResponse(resultSet.getBoolean(11));
        serviceProviderDO.setAssertionQueryRequestProfileEnabled(resultSet.getBoolean(12));
        serviceProviderDO.setEnableSAML2ArtifactBinding(resultSet.getBoolean(13));
        serviceProviderDO.setDoSignAssertions(resultSet.getBoolean(14));
        serviceProviderDO.setSamlECP(resultSet.getBoolean(15));
        serviceProviderDO.setEnableAttributesByDefault(resultSet.getBoolean(16));
        serviceProviderDO.setIdPInitSSOEnabled(resultSet.getBoolean(17));
        serviceProviderDO.setIdPInitSLOEnabled(resultSet.getBoolean(18));
        serviceProviderDO.setDoEnableEncryptedAssertion(resultSet.getBoolean(19));
        serviceProviderDO.setDoValidateSignatureInRequests(resultSet.getBoolean(20));
        serviceProviderDO.setDoValidateSignatureInArtifactResolve(resultSet.getBoolean(21));

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
