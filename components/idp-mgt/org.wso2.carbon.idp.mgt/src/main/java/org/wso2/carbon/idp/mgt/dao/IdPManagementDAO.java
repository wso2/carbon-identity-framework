/*
 * Copyright (c) 2014-2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.idp.mgt.dao;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.application.common.model.AccountLookupAttributeMappingConfig;
import org.wso2.carbon.identity.application.common.model.Claim;
import org.wso2.carbon.identity.application.common.model.ClaimConfig;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.FederatedAssociationConfig;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdPGroup;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.IdentityProviderProperty;
import org.wso2.carbon.identity.application.common.model.JustInTimeProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.LocalRole;
import org.wso2.carbon.identity.application.common.model.PermissionsAndRoleConfig;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.ProvisioningConnectorConfig;
import org.wso2.carbon.identity.application.common.model.RoleMapping;
import org.wso2.carbon.identity.application.common.model.UserDefinedFederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationManagementUtil;
import org.wso2.carbon.identity.base.AuthenticatorPropertyConstants;
import org.wso2.carbon.identity.base.AuthenticatorPropertyConstants.AuthenticationType;
import org.wso2.carbon.identity.base.AuthenticatorPropertyConstants.DefinedByType;
import org.wso2.carbon.identity.base.IdentityConstants;
import org.wso2.carbon.identity.core.ConnectorConfig;
import org.wso2.carbon.identity.core.ConnectorException;
import org.wso2.carbon.identity.core.ServiceURLBuilder;
import org.wso2.carbon.identity.core.URLBuilderException;
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.core.util.LambdaExceptionUtils;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.util.OrganizationManagementUtil;
import org.wso2.carbon.identity.organization.management.service.util.Utils;
import org.wso2.carbon.identity.organization.resource.hierarchy.traverse.service.OrgResourceResolverService;
import org.wso2.carbon.identity.organization.resource.hierarchy.traverse.service.exception.OrgResourceHierarchyTraverseException;
import org.wso2.carbon.identity.organization.resource.hierarchy.traverse.service.strategy.MergeAllAggregationStrategy;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementClientException;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementServerException;
import org.wso2.carbon.idp.mgt.internal.IdPManagementServiceComponent;
import org.wso2.carbon.idp.mgt.internal.IdpMgtServiceComponentHolder;
import org.wso2.carbon.idp.mgt.model.ConnectedAppsResult;
import org.wso2.carbon.idp.mgt.model.FilterQueryBuilder;
import org.wso2.carbon.idp.mgt.util.IdPManagementConstants;
import org.wso2.carbon.idp.mgt.util.IdPManagementUtil;
import org.wso2.carbon.idp.mgt.util.IdPSecretsProcessor;
import org.wso2.carbon.tenant.mgt.util.TenantMgtUtil;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.DBUtils;
import org.wso2.carbon.utils.security.KeystoreUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.REMEMBER_ME_TIME_OUT;
import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.REMEMBER_ME_TIME_OUT_DEFAULT;
import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.PRESERVE_CURRENT_SESSION_AT_PASSWORD_UPDATE;
import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.SESSION_IDLE_TIME_OUT;
import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.SESSION_IDLE_TIME_OUT_DEFAULT;
import static org.wso2.carbon.identity.core.util.JdbcUtils.isH2DB;
import static org.wso2.carbon.identity.core.util.JdbcUtils.isOracleDB;
import static org.wso2.carbon.idp.mgt.util.IdPManagementConstants.ACCOUNT_LOOKUP_ATTR_MAPPING_SEPARATOR;
import static org.wso2.carbon.idp.mgt.util.IdPManagementConstants.EMAIL_OTP_AUTHENTICATOR_NAME;
import static org.wso2.carbon.idp.mgt.util.IdPManagementConstants.EMAIL_OTP_ONLY_NUMERIC_CHARS_PROPERTY;
import static org.wso2.carbon.idp.mgt.util.IdPManagementConstants.EMAIL_OTP_USE_ALPHANUMERIC_CHARS_PROPERTY;
import static org.wso2.carbon.idp.mgt.util.IdPManagementConstants.ID;
import static org.wso2.carbon.idp.mgt.util.IdPManagementConstants.IS_TRUSTED_TOKEN_ISSUER;
import static org.wso2.carbon.idp.mgt.util.IdPManagementConstants.MySQL;
import static org.wso2.carbon.idp.mgt.util.IdPManagementConstants.PASSWORD_EXPIRY_RULES_GROUPS;
import static org.wso2.carbon.idp.mgt.util.IdPManagementConstants.PASSWORD_EXPIRY_RULES_KEY_PREFIX;
import static org.wso2.carbon.idp.mgt.util.IdPManagementConstants.RESET_PROVISIONING_ENTITIES_ON_CONFIG_UPDATE;
import static org.wso2.carbon.idp.mgt.util.IdPManagementConstants.SCOPE_LIST_PLACEHOLDER;
import static org.wso2.carbon.idp.mgt.util.IdPManagementConstants.SQLConstants.DEFINED_BY_COLUMN;
import static org.wso2.carbon.idp.mgt.util.IdPManagementConstants.SQLQueries.GET_IDP_NAME_BY_RESOURCE_ID_SQL;
import static org.wso2.carbon.idp.mgt.util.IdPManagementConstants.TEMPLATE_ID_IDP_PROPERTY_DISPLAY_NAME;
import static org.wso2.carbon.idp.mgt.util.IdPManagementConstants.TEMPLATE_ID_IDP_PROPERTY_NAME;

/**
 * This class is used to access the data storage to retrieve and store identity provider configurations.
 */
public class IdPManagementDAO {

    private static final Log log = LogFactory.getLog(IdPManagementDAO.class);
    private final IdPSecretsProcessor idpSecretsProcessorService = new IdPSecretsProcessor();

    private static final String OPENID_IDP_ENTITY_ID = "IdPEntityId";
    private static final String USE_ENTITY_ID_AS_ISSUER = "OAuth.OpenIDConnect.UseEntityIdAsIssuer";
    private static final String ENABLE_SMS_OTP_IF_RECOVERY_NOTIFICATION_ENABLED
            = "OnDemandConfig.OnInitialUse.EnableSMSOTPPasswordRecoveryIfConnectorEnabled";
    private static final String ENABLE_SMS_USERNAME_RECOVERY_IF_CONNECTOR_ENABLED
            = "OnDemandConfig.OnInitialUse.EnableSMSUsernameRecoveryIfConnectorEnabled";

    /**
     * @param dbConnection
     * @param tenantId
     * @param tenantDomain
     * @return
     * @throws IdentityProviderManagementException
     */
    public List<IdentityProvider> getIdPs(Connection dbConnection, int tenantId, String tenantDomain)
            throws IdentityProviderManagementException {

        boolean dbConnInitialized = true;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        List<IdentityProvider> idps = new ArrayList<IdentityProvider>();
        if (dbConnection == null) {
            dbConnection = IdentityDatabaseUtil.getDBConnection(false);
        } else {
            dbConnInitialized = false;
        }
        try {

            String sqlStmt = IdPManagementConstants.SQLQueries.GET_IDPS_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, tenantId);
            prepStmt.setInt(2, MultitenantConstants.SUPER_TENANT_ID);
            rs = prepStmt.executeQuery();
            while (rs.next()) {
                String identityProviderName = rs.getString(1);
                if (!IdentityApplicationConstants.RESIDENT_IDP_RESERVED_NAME
                        .equals(identityProviderName)) {
                    IdentityProvider identityProvider = new IdentityProvider();
                    identityProvider.setIdentityProviderName(identityProviderName);
                    if ((IdPManagementConstants.IS_TRUE_VALUE).equals(rs.getString("IS_PRIMARY"))) {
                        identityProvider.setPrimary(true);
                    } else {
                        identityProvider.setPrimary(false);
                    }
                    identityProvider.setHomeRealmId(rs.getString("HOME_REALM_ID"));
                    identityProvider.setIdentityProviderDescription(rs.getString("DESCRIPTION"));

                    // IS_FEDERATION_HUB_IDP
                    if ((IdPManagementConstants.IS_TRUE_VALUE).equals(rs.getString("IS_FEDERATION_HUB"))) {
                        identityProvider.setFederationHub(false);
                    }

                    // IS_LOCAL_CLAIM_DIALECT
                    if ((IdPManagementConstants.IS_TRUE_VALUE).equals(rs.getString("IS_LOCAL_CLAIM_DIALECT"))) {
                        if (identityProvider.getClaimConfig() == null) {
                            identityProvider.setClaimConfig(new ClaimConfig());
                        }
                        identityProvider.getClaimConfig().setLocalClaimDialect(true);
                    }

                    // IS_ENABLE
                    if ((IdPManagementConstants.IS_TRUE_VALUE).equals(rs.getString("IS_ENABLED"))) {
                        identityProvider.setEnable(true);
                    } else {
                        identityProvider.setEnable(false);
                    }

                    identityProvider.setDisplayName(rs.getString("DISPLAY_NAME"));

                    identityProvider.setId(rs.getString("ID"));
                    List<IdentityProviderProperty> propertyList = getIdentityPropertiesByIdpId(dbConnection,
                            Integer.parseInt(identityProvider.getId()), tenantId);
                    identityProvider
                            .setIdpProperties(propertyList.toArray(new IdentityProviderProperty[0]));
                    identityProvider.setImageUrl(rs.getString("IMAGE_URL"));
                    identityProvider.setResourceId(rs.getString("UUID"));
                    idps.add(identityProvider);
                }

            }
            return idps;
        } catch (SQLException e) {
            throw new IdentityProviderManagementException("Error occurred while retrieving registered Identity " +
                    "Provider Entity IDs " + "for tenant " + tenantDomain, e);
        } finally {
            if (dbConnInitialized) {
                IdentityDatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
            } else {
                IdentityDatabaseUtil.closeAllConnections(null, rs, prepStmt);
            }
        }
    }

    /**
     * @param dbConnection
     * @param tenantId
     * @param tenantDomain
     * @param filter
     * @return
     * @throws IdentityProviderManagementException
     */
    public List<IdentityProvider> getIdPsSearch(Connection dbConnection,
                                                int tenantId, String tenantDomain, String filter)
            throws IdentityProviderManagementException {

        boolean dbConnInitialized = true;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        List<IdentityProvider> idps = new ArrayList<IdentityProvider>();
        if (dbConnection == null) {
            dbConnection = IdentityDatabaseUtil.getDBConnection();
        } else {
            dbConnInitialized = false;
        }
        try {
            String sqlStmt = IdPManagementConstants.SQLQueries.GET_IDPS_NAME_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            if (StringUtils.isNotBlank(filter)) {
                filter = filter.trim();
                filter = filter.replace("*", "%");
                filter = filter.replace("?", "_");
            } else {
                //To avoid any issues when the filter string is blank or null we are assigning "%" to filter.
                filter = "%";
            }
            prepStmt.setInt(1, tenantId);
            prepStmt.setInt(2, MultitenantConstants.SUPER_TENANT_ID);
            prepStmt.setString(3, filter);
            rs = prepStmt.executeQuery();
            while (rs.next()) {
                String identityProviderName = rs.getString(1);
                if (!IdentityApplicationConstants.RESIDENT_IDP_RESERVED_NAME
                        .equals(identityProviderName)) {
                    IdentityProvider identityProvider = new IdentityProvider();
                    identityProvider.setIdentityProviderName(identityProviderName);
                    if ((IdPManagementConstants.IS_TRUE_VALUE).equals(rs
                            .getString("IS_PRIMARY"))) {
                        identityProvider.setPrimary(true);
                    } else {
                        identityProvider.setPrimary(false);
                    }
                    identityProvider.setHomeRealmId(rs.getString("HOME_REALM_ID"));
                    identityProvider.setIdentityProviderDescription(rs
                            .getString("DESCRIPTION"));
                    // IS_FEDERATION_HUB_IDP
                    if ((IdPManagementConstants.IS_TRUE_VALUE).equals(rs
                            .getString("IS_FEDERATION_HUB"))) {
                        identityProvider.setFederationHub(false);
                    }
                    // IS_LOCAL_CLAIM_DIALECT
                    if ((IdPManagementConstants.IS_TRUE_VALUE).equals(rs
                            .getString("IS_LOCAL_CLAIM_DIALECT"))) {
                        if (identityProvider.getClaimConfig() == null) {
                            identityProvider.setClaimConfig(new ClaimConfig());
                        }
                        identityProvider.getClaimConfig().setLocalClaimDialect(true);
                    }
                    // IS_ENABLE
                    if ((IdPManagementConstants.IS_TRUE_VALUE).equals(rs
                            .getString("IS_ENABLED"))) {
                        identityProvider.setEnable(true);
                    } else {
                        identityProvider.setEnable(false);
                    }
                    identityProvider.setDisplayName(rs.getString("DISPLAY_NAME"));
                    identityProvider.setId(rs.getString("ID"));
                    List<IdentityProviderProperty> propertyList = getIdentityPropertiesByIdpId(
                            dbConnection, Integer.parseInt(identityProvider.getId()), tenantId);
                    identityProvider.setIdpProperties(propertyList
                            .toArray(new IdentityProviderProperty[0]));
                    identityProvider.setImageUrl(rs.getString("IMAGE_URL"));
                    identityProvider.setResourceId(rs.getString("UUID"));
                    if (!IdentityApplicationConstants.RESIDENT_IDP_RESERVED_NAME
                            .equals(identityProvider.getIdentityProviderName())) {
                        idps.add(identityProvider);
                    }
                }
            }
            IdentityDatabaseUtil.commitTransaction(dbConnection);
            return idps;
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(dbConnection);
            throw new IdentityProviderManagementException(
                    "Error occurred while retrieving registered Identity "
                            + "Provider Entity IDs " + "for tenant " + tenantDomain, e);
        } finally {
            if (dbConnInitialized) {
                IdentityDatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
            } else {
                IdentityDatabaseUtil.closeAllConnections(null, rs, prepStmt);
            }
        }
    }

    /**
     * Get all basic identity provider information for a matching filter.
     *
     * @param tenantId       tenant Id of the identity provider.
     * @param expressionNode list of filter value for IdP search.
     * @param limit          limit per page.
     * @param offset         offset value.
     * @param sortOrder      order of IdP ASC/DESC.
     * @param sortBy         the attribute need to sort.
     * @return Identity Provider's Basic Information list.
     * @throws IdentityProviderManagementServerException Error when getting list of Identity Providers.
     * @throws IdentityProviderManagementClientException Error when append the filer string.
     */
    List<IdentityProvider> getIdPsSearch(int tenantId, List<ExpressionNode> expressionNode, int limit, int offset,
                                         String sortOrder, String sortBy)
            throws IdentityProviderManagementServerException, IdentityProviderManagementClientException {

        return getIdPsSearch(tenantId, expressionNode, limit, offset, sortOrder, sortBy, null);
    }

    /**
     * Get all identity provider's Basic information along with additionally requested information depends on the
     * requiredAttributes for a given matching filter.
     *
     * @param tenantId           Tenant Id of the identity provider.
     * @param expressionNode     List of filter value for IdP search.
     * @param limit              Limit per page.
     * @param offset             Offset value.
     * @param sortOrder          Order of IdP ASC/DESC.
     * @param sortBy             The attribute need to sort.
     * @param requiredAttributes Required attributes which needs to be return.
     * @return Identity Provider's Basic Information array along with requested attribute information.
     * @throws IdentityProviderManagementServerException Error when getting list of Identity Providers.
     * @throws IdentityProviderManagementClientException Error when append the filer string.
     */
    List<IdentityProvider> getIdPsSearch(int tenantId, List<ExpressionNode> expressionNode, int limit, int offset,
                                         String sortOrder, String sortBy, List<String> requiredAttributes)
            throws IdentityProviderManagementServerException, IdentityProviderManagementClientException {

        FilterQueryBuilder filterQueryBuilder = new FilterQueryBuilder();
        appendFilterQuery(expressionNode, filterQueryBuilder);
        String sortedOrder = sortBy + " " + sortOrder;
        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false);
             ResultSet resultSet = getIdpQueryResultSet(dbConnection, sortedOrder, tenantId, offset, limit,
                     filterQueryBuilder, requiredAttributes)) {
            return populateIdentityProviderList(resultSet, dbConnection, requiredAttributes, tenantId);
        } catch (SQLException e) {
            String message = "Error occurred while retrieving Identity Provider for tenant: " +
                    IdentityTenantUtil.getTenantDomain(tenantId);
            throw IdPManagementUtil.handleServerException(IdPManagementConstants.ErrorMessage
                    .ERROR_CODE_CONNECTING_DATABASE, message, e);
        }
    }

    /**
     * Get all trusted token issuer's Basic information along with additionally requested information depends on the
     * requiredAttributes for a given matching filter.
     *
     * @param tenantId           Tenant Id of the trusted token issuer.
     * @param expressionNode     List of filter value for IdP search.
     * @param limit              Limit per page.
     * @param offset             Offset value.
     * @param sortOrder          Order of IdP ASC/DESC.
     * @param sortBy             The attribute need to sort.
     * @param requiredAttributes Required attributes which needs to be return.
     * @return Identity Provider's Basic Information array along with requested attribute information.
     * @throws IdentityProviderManagementServerException Error when getting list of Identity Providers.
     * @throws IdentityProviderManagementClientException Error when append the filer string.
     */
    List<IdentityProvider> getTrustedTokenIssuerSearch(int tenantId, List<ExpressionNode> expressionNode, int limit, int offset,
                                                       String sortOrder, String sortBy, List<String> requiredAttributes)
            throws IdentityProviderManagementServerException, IdentityProviderManagementClientException {

        FilterQueryBuilder filterQueryBuilder = new FilterQueryBuilder();
        appendFilterQuery(expressionNode, filterQueryBuilder, true);
        String sortedOrder = sortBy + " " + sortOrder;
        try {
            Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false);
            ResultSet resultSet = getTrustedTokenIssuerQueryResultSet(dbConnection, sortedOrder, tenantId, offset, limit,
                    filterQueryBuilder, requiredAttributes);
            return populateIdentityProviderList(resultSet, dbConnection, requiredAttributes, tenantId);
        } catch (SQLException e) {
            String message = "Error occurred while retrieving Identity Provider for tenant: " +
                    IdentityTenantUtil.getTenantDomain(tenantId);
            throw IdPManagementUtil.handleServerException(IdPManagementConstants.ErrorMessage
                    .ERROR_CODE_CONNECTING_DATABASE, message, e);
        }
    }

    /**
     * Get trusted token issuer result set.
     *
     * @param dbConnection       database connection.
     * @param sortedOrder        Sort order.
     * @param tenantId           tenant Id of the trusted token issuer.
     * @param offset             offset value.
     * @param limit              limit per page.
     * @param filterQueryBuilder filter query buider object.
     * @param requiredAttributes Required attributes which needs to be return.
     * @return result set of the query.
     * @throws SQLException                              Database Exception.
     * @throws IdentityProviderManagementServerException Error when getting list of trusted token issuers.
     */
    private ResultSet getTrustedTokenIssuerQueryResultSet(Connection dbConnection, String sortedOrder, int tenantId,
                                                          int offset, int limit, FilterQueryBuilder filterQueryBuilder,
                                                          List<String> requiredAttributes)
            throws SQLException, IdentityProviderManagementServerException, IdentityProviderManagementClientException {

        String sqlQuery;
        String sqlTail;
        PreparedStatement prepStmt;
        Map<Integer, String> filterAttributeValue = filterQueryBuilder.getFilterAttributeValue();
        int filterAttributeValueSize = filterAttributeValue.entrySet().size();
        String databaseProductName = dbConnection.getMetaData().getDatabaseProductName();
        if (databaseProductName.contains("Microsoft") || databaseProductName.contains("H2")) {
            sqlQuery = IdPManagementConstants.SQLQueries.GET_TRUSTED_TOKEN_ISSUER_TENANT_MSSQL;
            sqlQuery = appendRequiredAttributes(sqlQuery, requiredAttributes);
            sqlTail = String.format(IdPManagementConstants.SQLQueries.GET_TRUSTED_TOKEN_ISSUER_TENANT_MSSQL_TAIL, sortedOrder);
            // Add the type filter to the query.
            String tokenIssuerFilterSql = IdPManagementConstants.SQLQueries.TRUSTED_TOKEN_ISSUER_FILTER_SQL;
            filterQueryBuilder.setFilterQuery(filterQueryBuilder.getFilterQuery() + tokenIssuerFilterSql);
            sqlQuery = sqlQuery + IdPManagementConstants.SQLQueries.FROM_TRUSTED_TOKEN_ISSUER_WHERE
                    + filterQueryBuilder.getFilterQuery() + sqlTail;
            prepStmt = dbConnection.prepareStatement(sqlQuery);
            for (Map.Entry<Integer, String> prepareStatement : filterAttributeValue.entrySet()) {
                prepStmt.setString(prepareStatement.getKey(), prepareStatement.getValue());
            }
            prepStmt.setInt(filterAttributeValueSize + 1, tenantId);
            prepStmt.setInt(filterAttributeValueSize + 2, offset);
            prepStmt.setInt(filterAttributeValueSize + 3, limit);
        } else {
            String message = "Error while loading Identity Provider from DB: Database driver could not be identified" +
                    " or not supported.";
            throw IdPManagementUtil.handleServerException(IdPManagementConstants.ErrorMessage
                    .ERROR_CODE_CONNECTING_DATABASE, message);
        }
        return prepStmt.executeQuery();
    }

    /**
     * Get the result set.
     *
     * @param dbConnection       database connection.
     * @param sortedOrder        Sort order.
     * @param tenantId           tenant Id of the identity provider.
     * @param offset             offset value.
     * @param limit              limit per page.
     * @param filterQueryBuilder filter query buider object.
     * @param requiredAttributes Required attributes which needs to be return.
     * @return result set of the query.
     * @throws SQLException                              Database Exception.
     * @throws IdentityProviderManagementServerException Error when getting list of Identity Providers.
     */
    private ResultSet getIdpQueryResultSet(Connection dbConnection, String sortedOrder, int tenantId, int offset,
                                           int limit, FilterQueryBuilder filterQueryBuilder,
                                           List<String> requiredAttributes)
            throws SQLException, IdentityProviderManagementServerException, IdentityProviderManagementClientException {

        String sqlQuery;
        String sqlTail;
        PreparedStatement prepStmt;
        Map<Integer, String> filterAttributeValue = filterQueryBuilder.getFilterAttributeValue();
        int filterAttributeValueSize = filterAttributeValue.entrySet().size();
        String databaseProductName = dbConnection.getMetaData().getDatabaseProductName();
        if (databaseProductName.contains("MySQL")
                || databaseProductName.contains("MariaDB")
                || databaseProductName.contains("H2")) {
            sqlQuery = IdPManagementConstants.SQLQueries.GET_IDP_BY_TENANT_MYSQL;
            sqlQuery = appendRequiredAttributes(sqlQuery, requiredAttributes);
            sqlTail = String.format(IdPManagementConstants.SQLQueries.GET_IDP_BY_TENANT_MYSQL_TAIL, sortedOrder);
            sqlQuery = sqlQuery + IdPManagementConstants.SQLQueries.FROM_IDP_WHERE
                    + filterQueryBuilder.getFilterQuery() + sqlTail;
            prepStmt = dbConnection.prepareStatement(sqlQuery);
            for (Map.Entry<Integer, String> prepareStatement : filterAttributeValue.entrySet()) {
                prepStmt.setString(prepareStatement.getKey(), prepareStatement.getValue());
            }
            prepStmt.setInt(filterAttributeValueSize + 1, tenantId);
            prepStmt.setInt(filterAttributeValueSize + 2, offset);
            prepStmt.setInt(filterAttributeValueSize + 3, limit);
        } else if (databaseProductName.contains("Oracle")) {
            String tempSqlQuery = IdPManagementConstants.SQLQueries.GET_IDP_BY_TENANT_ORACLE;
            tempSqlQuery = appendRequiredAttributes(tempSqlQuery, requiredAttributes);

            String sortBy = String.format(IdPManagementConstants.SQLQueries.FROM_IDP_WHERE_ORACLE, sortedOrder);
            sqlTail = IdPManagementConstants.SQLQueries.GET_IDP_BY_TENANT_ORACLE_TAIL;
            // Keeping legacy query in-order to support older version of oracle such as Oracle 11.
            sqlQuery = tempSqlQuery + IdPManagementConstants.SQLQueries.FROM + tempSqlQuery +
                    IdPManagementConstants.SQLQueries.ROWNUM_FOR_ORACLE + tempSqlQuery + sortBy;
            // Append filter query conditions if provided and tail.
            sqlQuery = sqlQuery + filterQueryBuilder.getFilterQuery() + sqlTail;
            prepStmt = dbConnection.prepareStatement(sqlQuery);
            for (Map.Entry<Integer, String> prepareStatement : filterAttributeValue.entrySet()) {
                prepStmt.setString(prepareStatement.getKey(), prepareStatement.getValue());
            }
            prepStmt.setInt(filterAttributeValueSize + 1, tenantId);
            prepStmt.setInt(filterAttributeValueSize + 2, tenantId);
            prepStmt.setInt(filterAttributeValueSize + 3, offset + limit);
            prepStmt.setInt(filterAttributeValueSize + 4, offset);
        } else if (databaseProductName.contains("Microsoft")) {
            sqlQuery = IdPManagementConstants.SQLQueries.GET_IDP_BY_TENANT_MSSQL;
            sqlQuery = appendRequiredAttributes(sqlQuery, requiredAttributes);
            sqlTail = String.format(IdPManagementConstants.SQLQueries.GET_IDP_BY_TENANT_MSSQL_TAIL, sortedOrder);
            sqlQuery = sqlQuery + IdPManagementConstants.SQLQueries.FROM_IDP_WHERE
                    + filterQueryBuilder.getFilterQuery() + sqlTail;
            prepStmt = dbConnection.prepareStatement(sqlQuery);
            for (Map.Entry<Integer, String> prepareStatement : filterAttributeValue.entrySet()) {
                prepStmt.setString(prepareStatement.getKey(), prepareStatement.getValue());
            }
            prepStmt.setInt(filterAttributeValueSize + 1, tenantId);
            prepStmt.setInt(filterAttributeValueSize + 2, offset);
            prepStmt.setInt(filterAttributeValueSize + 3, limit);
        } else if (databaseProductName.contains("PostgreSQL")) {
            sqlQuery = IdPManagementConstants.SQLQueries.GET_IDP_BY_TENANT_POSTGRESQL;
            sqlQuery = appendRequiredAttributes(sqlQuery, requiredAttributes);
            sqlTail = String.format(IdPManagementConstants.SQLQueries.GET_IDP_BY_TENANT_POSTGRESQL_TAIL, sortedOrder);
            sqlQuery = sqlQuery + IdPManagementConstants.SQLQueries.FROM_IDP_WHERE
                    + filterQueryBuilder.getFilterQuery() + sqlTail;
            prepStmt = dbConnection.prepareStatement(sqlQuery);
            for (Map.Entry<Integer, String> prepareStatement : filterAttributeValue.entrySet()) {
                prepStmt.setString(prepareStatement.getKey(), prepareStatement.getValue());
            }
            prepStmt.setInt(filterAttributeValueSize + 1, tenantId);
            prepStmt.setInt(filterAttributeValueSize + 2, limit);
            prepStmt.setInt(filterAttributeValueSize + 3, offset);
        } else if (databaseProductName.contains("DB2")) {
            sqlQuery = IdPManagementConstants.SQLQueries.GET_IDP_BY_TENANT_DB2SQL;
            sqlQuery = appendRequiredAttributes(sqlQuery, requiredAttributes);
            sqlTail = String.format(IdPManagementConstants.SQLQueries.GET_IDP_BY_TENANT_DB2SQL_TAIL, sortedOrder);
            sqlQuery = sqlQuery + IdPManagementConstants.SQLQueries.FROM_IDP_WHERE
                    + filterQueryBuilder.getFilterQuery() + sqlTail;
            prepStmt = dbConnection.prepareStatement(sqlQuery);
            for (Map.Entry<Integer, String> prepareStatement : filterAttributeValue.entrySet()) {
                prepStmt.setString(prepareStatement.getKey(), prepareStatement.getValue());
            }
            prepStmt.setInt(filterAttributeValueSize + 1, tenantId);
            prepStmt.setInt(filterAttributeValueSize + 2, limit);
            prepStmt.setInt(filterAttributeValueSize + 3, offset);
        } else if (databaseProductName.contains("INFORMIX")) {
            sqlQuery = IdPManagementConstants.SQLQueries.GET_IDP_BY_TENANT_INFORMIX;
            sqlQuery = appendRequiredAttributes(sqlQuery, requiredAttributes);

            sqlTail = String.format(IdPManagementConstants.SQLQueries.GET_IDP_BY_TENANT_INFORMIX_TAIL, sortedOrder);
            sqlQuery = sqlQuery + IdPManagementConstants.SQLQueries.FROM_IDP_WHERE
                    + filterQueryBuilder.getFilterQuery() + sqlTail;
            prepStmt = dbConnection.prepareStatement(sqlQuery);
            prepStmt.setInt(1, offset);
            prepStmt.setInt(2, limit);
            for (Map.Entry<Integer, String> prepareStatement : filterAttributeValue.entrySet()) {
                prepStmt.setString(prepareStatement.getKey() + 2, prepareStatement.getValue());
            }
            prepStmt.setInt(filterAttributeValue.entrySet().size() + 3, tenantId);
        } else {
            log.error("Error while loading Identity Provider from DB: Database driver could not be identified or "
                    + "not supported.");
            String message = "Error while loading Identity Provider from DB: Database driver could not be identified" +
                    " or not supported.";
            throw IdPManagementUtil.handleServerException(IdPManagementConstants.ErrorMessage
                    .ERROR_CODE_CONNECTING_DATABASE, message);
        }
        return prepStmt.executeQuery();
    }

    /**
     * Add requested required attributes in the SQL query.
     *
     * @param sqlQuery           SQL query which need to be modify.
     * @param requiredAttributes Required attributes which needs to be return.
     * @return modified SQL query.
     */
    private String appendRequiredAttributes(String sqlQuery, List<String> requiredAttributes)
            throws IdentityProviderManagementClientException {

        if (CollectionUtils.isNotEmpty(requiredAttributes)) {
            for (String attribute : requiredAttributes) {
                switch (attribute) {
                    case IdPManagementConstants.IDP_UUID:
                        // Skip since it is basic attribute which is by default added.
                        break;
                    case IdPManagementConstants.IDP_NAME:
                        // Skip since it is basic attribute which is by default added.
                        break;
                    case IdPManagementConstants.IDP_DESCRIPTION:
                        // Skip since it is basic attribute which is by default added.
                        break;
                    case IdPManagementConstants.IDP_IS_ENABLED:
                        // Skip since it is basic attribute which is by default added.
                        break;
                    case IdPManagementConstants.IDP_IMAGE_URL:
                        // Skip since it is basic attribute which is by default added.
                        break;
                    case IdPManagementConstants.IDP_IS_PRIMARY:
                        sqlQuery += ", " + IdPManagementConstants.IS_PRIMARY + " ";
                        break;
                    case IdPManagementConstants.IDP_HOME_REALM_ID:
                        sqlQuery += ", " + IdPManagementConstants.HOME_REALM_ID + " ";
                        break;
                    case IdPManagementConstants.IDP_IS_FEDERATION_HUB:
                        sqlQuery += ", " + IdPManagementConstants.IS_FEDERATION_HUB + " ";
                        break;
                    case IdPManagementConstants.IDP_CERTIFICATE:
                        sqlQuery += ", " + IdPManagementConstants.CERTIFICATE + " ";
                        break;
                    case IdPManagementConstants.IDP_ALIAS:
                        sqlQuery += ", " + IdPManagementConstants.ALIAS + " ";
                        break;
                    case IdPManagementConstants.IDP_CLAIMS:
                        sqlQuery += ", " + IdPManagementConstants.CLAIMS + " ";
                        break;
                    case IdPManagementConstants.IDP_ROLES:
                        sqlQuery += ", " + IdPManagementConstants.ROLES + " ";
                        break;
                    case IdPManagementConstants.IDP_FEDERATED_AUTHENTICATORS:
                        sqlQuery += ", " + IdPManagementConstants.FEDERATED_AUTHENTICATORS + " ";
                        break;
                    case IdPManagementConstants.IDP_PROVISIONING:
                        sqlQuery += ", " + IdPManagementConstants.PROVISIONING + " ";
                        break;
                    case IdPManagementConstants.IDP_GROUPS:
                        // Skip since the IDP groups are resolved and added to the IDP object separately.
                        break;
                    default:
                        throw IdPManagementUtil.handleClientException(
                                IdPManagementConstants.ErrorMessage.ERROR_CODE_IDP_ATTRIBUTE_INVALID, attribute);
                }
            }
        }
        return sqlQuery;
    }

    /**
     * Populate the result set.
     *
     * @param resultSet          ResultSet.
     * @param dbConnection       Database Connection.
     * @param requiredAttributes Required attributes which needs to be return.
     * @param tenantId           Tenant Id of the identity provider.
     * @return List of Identity Provider.
     * @throws SQLException Database Exception.
     */
    private List<IdentityProvider> populateIdentityProviderList(ResultSet resultSet, Connection dbConnection,
                                                                List<String> requiredAttributes, int tenantId)
            throws SQLException, IdentityProviderManagementServerException {

        List<IdentityProvider> identityProviderList = new ArrayList<>();
        while (resultSet.next()) {
            IdentityProvider identityProvider = new IdentityProvider();
            // First set the basic attributes such as id, name, description, isEnabled, image url, uuid.
            identityProvider.setId(resultSet.getString("ID"));
            identityProvider.setIdentityProviderName(resultSet.getString("NAME"));
            identityProvider.setIdentityProviderDescription(resultSet.getString("DESCRIPTION"));
            // IS_ENABLE
            if ((IdPManagementConstants.IS_TRUE_VALUE).equals(resultSet.getString("IS_ENABLED"))) {
                identityProvider.setEnable(true);
            } else {
                identityProvider.setEnable(false);
            }
            identityProvider.setImageUrl(resultSet.getString("IMAGE_URL"));
            identityProvider.setResourceId(resultSet.getString("UUID"));

            try {
                populateRequiredAttributesForIdentityProviderList(resultSet, dbConnection, requiredAttributes, tenantId,
                        identityProvider);
            } catch (IdentityProviderManagementClientException e) {
                continue;
            }

            if (!IdentityApplicationConstants.RESIDENT_IDP_RESERVED_NAME
                    .equals(identityProvider.getIdentityProviderName())) {
                identityProviderList.add(identityProvider);
            }
            List<IdentityProviderProperty> propertyList = getIdentityPropertiesByIdpId(dbConnection,
                    Integer.parseInt(resultSet.getString("ID")), tenantId);
            identityProvider.setIdpProperties(propertyList.toArray(new IdentityProviderProperty[0]));
        }
        return identityProviderList;
    }

    /**
     * @param resultSet          ResultSet.
     * @param dbConnection       Database Connection.
     * @param requiredAttributes Required attributes which needs to be return.
     * @param tenantId           Tenant Id of the identity provider.
     * @param identityProvider   Identity Provider Object.
     * @throws SQLException
     * @throws IdentityProviderManagementServerException
     */
    private void populateRequiredAttributesForIdentityProviderList(ResultSet resultSet, Connection dbConnection,
                                                                   List<String> requiredAttributes, int tenantId,
                                                                   IdentityProvider identityProvider)
            throws SQLException, IdentityProviderManagementServerException, IdentityProviderManagementClientException {

        int idpId = Integer.parseInt(identityProvider.getId());
        String idPName = identityProvider.getIdentityProviderName();

        try {
            if (CollectionUtils.isNotEmpty(requiredAttributes)) {
                for (String attribute : requiredAttributes) {
                    switch (attribute) {
                        case IdPManagementConstants.IDP_IS_PRIMARY:
                            if ((IdPManagementConstants.IS_TRUE_VALUE).equals(resultSet.getString("IS_PRIMARY"))) {
                                identityProvider.setPrimary(true);
                            } else {
                                identityProvider.setPrimary(false);
                            }
                            break;
                        case IdPManagementConstants.IDP_HOME_REALM_ID:
                            identityProvider.setHomeRealmId(resultSet.getString("HOME_REALM_ID"));
                            break;
                        case IdPManagementConstants.IDP_IS_FEDERATION_HUB:
                            if ((IdPManagementConstants.IS_TRUE_VALUE)
                                    .equals(resultSet.getString("IS_FEDERATION_HUB"))) {
                                identityProvider.setFederationHub(false);
                            }
                            break;
                        case IdPManagementConstants.IDP_CERTIFICATE:
                            identityProvider.setCertificate(getBlobValue(resultSet.getBinaryStream("CERTIFICATE")));
                            break;
                        case IdPManagementConstants.IDP_ALIAS:
                            identityProvider.setAlias(resultSet.getString("ALIAS"));
                            break;
                        case IdPManagementConstants.IDP_CLAIMS:
                            if (identityProvider.getClaimConfig() == null) {
                                identityProvider.setClaimConfig(new ClaimConfig());
                            }

                            if (IdPManagementConstants.IS_TRUE_VALUE
                                    .equals(resultSet.getString("IS_LOCAL_CLAIM_DIALECT"))) {
                                identityProvider.getClaimConfig().setLocalClaimDialect(true);
                            } else {
                                identityProvider.getClaimConfig().setLocalClaimDialect(false);
                            }

                            String userClaimUri = resultSet.getString("USER_CLAIM_URI");
                            String roleClaimUri = resultSet.getString("ROLE_CLAIM_URI");

                            if (identityProvider.getClaimConfig().isLocalClaimDialect()) {
                                identityProvider.setClaimConfig(getLocalIdPDefaultClaimValues(dbConnection,
                                        idPName, userClaimUri, roleClaimUri, idpId, tenantId));
                            } else {
                                // Get claim configuration.
                                identityProvider.setClaimConfig(getIdPClaimConfiguration(dbConnection, idPName,
                                        userClaimUri, roleClaimUri, idpId, tenantId));
                            }
                            break;
                        case IdPManagementConstants.IDP_ROLES:
                            identityProvider.setProvisioningRole(resultSet.getString("PROVISIONING_ROLE"));
                            // Get permission and role configuration.
                            identityProvider.setPermissionAndRoleConfig(getPermissionsAndRoleConfiguration(
                                    dbConnection, idPName, idpId, tenantId));
                            break;
                        case IdPManagementConstants.IDP_GROUPS:
                            // Get idp groups.
                            identityProvider.setIdPGroupConfig(getIdPGroupConfiguration(dbConnection, idpId));
                            break;
                        case IdPManagementConstants.IDP_FEDERATED_AUTHENTICATORS:
                            String defaultAuthenticatorName = resultSet.getString("DEFAULT_AUTHENTICATOR_NAME");

                            // Get federated authenticators.
                            identityProvider.setFederatedAuthenticatorConfigs(getFederatedAuthenticatorConfigs(
                                    dbConnection, idPName, identityProvider, tenantId));

                            if (defaultAuthenticatorName != null &&
                                    identityProvider.getFederatedAuthenticatorConfigs() != null) {
                                identityProvider.setDefaultAuthenticatorConfig(IdentityApplicationManagementUtil
                                        .getFederatedAuthenticator(
                                                identityProvider.getFederatedAuthenticatorConfigs(),
                                                defaultAuthenticatorName));
                            }
                            break;
                        case IdPManagementConstants.IDP_PROVISIONING:
                            JustInTimeProvisioningConfig jitProConfig = new JustInTimeProvisioningConfig();
                            if ((IdPManagementConstants.IS_TRUE_VALUE)
                                    .equals(resultSet.getString("INBOUND_PROV_ENABLED"))) {
                                jitProConfig.setProvisioningEnabled(true);
                            } else {
                                jitProConfig.setProvisioningEnabled(false);
                            }

                            jitProConfig
                                    .setProvisioningUserStore(resultSet.getString("INBOUND_PROV_USER_STORE_ID"));
                            identityProvider.setJustInTimeProvisioningConfig(jitProConfig);

                            String defaultProvisioningConnectorConfigName =
                                    resultSet.getString("DEFAULT_PRO_CONNECTOR_NAME");
                            if (defaultProvisioningConnectorConfigName != null) {
                                ProvisioningConnectorConfig defaultProConnector = new ProvisioningConnectorConfig();
                                defaultProConnector.setName(defaultProvisioningConnectorConfigName);
                                identityProvider.setDefaultProvisioningConnectorConfig(defaultProConnector);
                            }

                            // Get provisioning connectors.
                            identityProvider.setProvisioningConnectorConfigs(getProvisioningConnectorConfigs(
                                    dbConnection, idPName, idpId, tenantId));
                            break;
                    }
                }
            }
        } catch (IdentityProviderManagementClientException e) {
            throw e;
        } catch (IdentityProviderManagementException e) {
            throw new IdentityProviderManagementServerException("Error occurred while performing required " +
                    "attribute filter", e);
        }
    }

    /**
     * Get number of IdP count for a matching filter.
     *
     * @param tenantId       Tenant Id of the identity provider.
     * @param expressionNode filter value list for IdP search.
     * @return number of IdP count for a given filter
     * @throws IdentityProviderManagementServerException Error when getting count of Identity Providers.
     * @throws IdentityProviderManagementClientException Error when append the filer string.
     */
    int getCountOfFilteredIdPs(int tenantId, List<ExpressionNode> expressionNode)
            throws IdentityProviderManagementServerException, IdentityProviderManagementClientException {

        int countOfFilteredIdp = 0;
        FilterQueryBuilder filterQueryBuilder = new FilterQueryBuilder();
        appendFilterQuery(expressionNode, filterQueryBuilder);
        String filterClause = filterQueryBuilder.getFilterQuery();
        Map<Integer, String> filterValues = filterQueryBuilder.getFilterAttributeValue();

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false)) {
            String dbName = dbConnection.getMetaData().getDatabaseProductName();

            String sqlTail;
            if (dbName.contains("MySQL") || dbName.contains("MariaDB") || dbName.contains("H2")) {
                sqlTail = IdPManagementConstants.SQLQueries.GET_IDP_COUNT_MYSQL_TAIL;
            } else if (dbName.contains("Oracle") || dbName.contains("DB2") || dbName.contains("Informix")) {
                sqlTail = IdPManagementConstants.SQLQueries.GET_IDP_COUNT_ORACLE_TAIL;
            } else if (dbName.contains("Microsoft") || dbName.contains("PostgreSQL")) {
                sqlTail = IdPManagementConstants.SQLQueries.GET_IDP_COUNT_MSSQL_TAIL;
            } else {
                String message = "Error while loading IdP count from DB: Database driver " +
                        "could not be identified or not supported.";
                throw IdPManagementUtil.handleServerException(IdPManagementConstants.ErrorMessage
                        .ERROR_CODE_CONNECTING_DATABASE, message);
            }

            String sql = IdPManagementConstants.SQLQueries.GET_IDP_COUNT_SQL
                    + filterClause
                    + sqlTail;

            try (PreparedStatement prepStmt = dbConnection.prepareStatement(sql)) {
                for (Map.Entry<Integer,String> entry : filterValues.entrySet()) {
                    prepStmt.setString(entry.getKey(), entry.getValue());
                }
                prepStmt.setInt(filterValues.size() + 1, tenantId);

                try (ResultSet rs = prepStmt.executeQuery()) {
                    if (rs.next()) {
                        countOfFilteredIdp = Integer.parseInt(rs.getString(1));
                    }
                }
            }
        } catch (SQLException e) {
            String message = "Error occurred while retrieving Identity Provider count for tenant: "
                    + IdentityTenantUtil.getTenantDomain(tenantId);
            throw IdPManagementUtil.handleServerException(
                    IdPManagementConstants.ErrorMessage.ERROR_CODE_CONNECTING_DATABASE, message, e);
        }
        return countOfFilteredIdp;
    }

    /**
     * Get number of trusted token Issuer count for a matching filter.
     *
     * @param tenantId       Tenant Id of the trusted token issuer.
     * @param expressionNode filter value list for trusted token issuer search.
     * @return number of trusted token issuer count for a given filter
     * @throws IdentityProviderManagementServerException Error when getting count of trusted token issuers.
     * @throws IdentityProviderManagementClientException Error when append the filer string.
     */
    int getCountOfFilteredTokenIssuers(int tenantId, List<ExpressionNode> expressionNode)
            throws IdentityProviderManagementServerException, IdentityProviderManagementClientException {

        String sqlStmt = IdPManagementConstants.SQLQueries.GET_TRUSTED_TOKEN_ISSUER_COUNT_SQL;
        int countOfFilteredIdp = 0;
        FilterQueryBuilder filterQueryBuilder = new FilterQueryBuilder();
        appendFilterQuery(expressionNode, filterQueryBuilder, true);
        String filter = IdPManagementConstants.SQLQueries.TRUSTED_TOKEN_ISSUER_FILTER_SQL;
        filterQueryBuilder.setFilterQuery(filterQueryBuilder.getFilterQuery() + filter);
        Map<Integer, String> filterAttributeValue = filterQueryBuilder.getFilterAttributeValue();
        sqlStmt = sqlStmt + filterQueryBuilder.getFilterQuery() +
                IdPManagementConstants.SQLQueries.GET_TRUSTED_TOKEN_ISSUER_COUNT_SQL_TAIL;
        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement prepStmt = dbConnection.prepareStatement(sqlStmt)) {
            for (Map.Entry<Integer, String> prepareStatement : filterAttributeValue.entrySet()) {
                prepStmt.setString(prepareStatement.getKey(), prepareStatement.getValue());
            }
            prepStmt.setInt(filterAttributeValue.entrySet().size() + 1, tenantId);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    countOfFilteredIdp = Integer.parseInt(rs.getString(1));
                }
            }
        } catch (SQLException e) {
            String message = "Error occurred while retrieving Identity Provider count for a tenant : " +
                    IdentityTenantUtil.getTenantDomain(tenantId);
            throw IdPManagementUtil.handleServerException(IdPManagementConstants.ErrorMessage
                    .ERROR_CODE_CONNECTING_DATABASE, message, e);
        }
        return countOfFilteredIdp;
    }

    /**
     * Create a sql query and prepared statement for filter.
     *
     * @param expressionNodes    list of filters.
     * @param filterQueryBuilder Sql builder object.
     * @throws IdentityProviderManagementClientException throw invalid filer attribute exception.
     */
    private void appendFilterQuery(List<ExpressionNode> expressionNodes, FilterQueryBuilder filterQueryBuilder)
            throws IdentityProviderManagementClientException {

        appendFilterQuery(expressionNodes, filterQueryBuilder, false);
    }
    /**
     * Create a sql query and prepared statement for filter.
     *
     * @param expressionNodes    list of filters.
     * @param filterQueryBuilder Sql builder object.
     * @param isTrustedTokenIssuer is trusted token issuer.
     * @throws IdentityProviderManagementClientException throw invalid filer attribute exception.
     */
    private void appendFilterQuery(List<ExpressionNode> expressionNodes, FilterQueryBuilder filterQueryBuilder,
                                   boolean isTrustedTokenIssuer)
            throws IdentityProviderManagementClientException {

        StringBuilder filter = new StringBuilder();
        if (CollectionUtils.isEmpty(expressionNodes)) {
            filterQueryBuilder.setFilterQuery(IdPManagementConstants.EMPTY_STRING);
        } else {
            for (ExpressionNode expressionNode : expressionNodes) {
                String operation = expressionNode.getOperation();
                String value = expressionNode.getValue();
                String attributeName = expressionNode.getAttributeValue();
                if (StringUtils.isNotBlank(attributeName) && StringUtils.isNotBlank(value) && StringUtils
                        .isNotBlank(operation)) {
                    switch (attributeName) {
                        case IdPManagementConstants.IDP_NAME:
                            attributeName = isTrustedTokenIssuer ? IdPManagementConstants.TRUSTED_TOKEN_ISSUER_NAME :
                                    IdPManagementConstants.NAME;
                            break;
                        case IdPManagementConstants.IDP_DESCRIPTION:
                            attributeName = IdPManagementConstants.DESCRIPTION;
                            break;
                        case IdPManagementConstants.IDP_HOME_REALM_ID:
                            attributeName = IdPManagementConstants.HOME_REALM_ID;
                            break;
                        case IdPManagementConstants.IDP_IS_ENABLED:
                            attributeName = IdPManagementConstants.IS_ENABLED;
                            break;
                        case IdPManagementConstants.IDP_UUID:
                            attributeName = IdPManagementConstants.UUID;
                            break;
                        default:
                            String message = "Invalid filter attribute name. Filter attribute : " + attributeName;
                            throw IdPManagementUtil.handleClientException(IdPManagementConstants.ErrorMessage
                                    .ERROR_CODE_RETRIEVE_IDP, message);
                    }
                    if (IdPManagementConstants.EQ.equals(operation)) {
                        filter.append(attributeName).append(" = ? AND ");
                        filterQueryBuilder.setFilterAttributeValue(value);
                    } else if (IdPManagementConstants.SW.equals(operation)) {
                        filter.append(attributeName).append(" like ? AND ");
                        filterQueryBuilder.setFilterAttributeValue(value + "%");
                    } else if (IdPManagementConstants.EW.equals(operation)) {
                        filter.append(attributeName).append(" like ? AND ");
                        filterQueryBuilder.setFilterAttributeValue("%" + value);
                    } else if (IdPManagementConstants.CO.equals(operation)) {
                        filter.append(attributeName).append(" like ? AND ");
                        filterQueryBuilder.setFilterAttributeValue("%" + value + "%");
                    } else {
                        String message = "Invalid filter value. filter: " + operation;
                        throw IdPManagementUtil.handleClientException(IdPManagementConstants.ErrorMessage
                                .ERROR_CODE_RETRIEVE_IDP, message);
                    }
                }
            }
            if (StringUtils.isBlank(filter.toString())) {
                filterQueryBuilder.setFilterQuery(IdPManagementConstants.EMPTY_STRING);
            } else {
                filterQueryBuilder.setFilterQuery(filter.toString());
            }
        }
    }

    /**
     * Get Identity properties map.
     *
     * @param dbConnection database connection
     * @param idpId        IDP Id
     * @return Identity provider properties
     */
    private List<IdentityProviderProperty> getIdentityPropertiesByIdpId(Connection dbConnection,
                                                                        int idpId, int tenantId)
            throws SQLException {

        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        List<IdentityProviderProperty> idpProperties = new ArrayList<IdentityProviderProperty>();
        boolean isRecoveryNotificationPasswordRecoveryEnabled = false;
        boolean isEmailLinkNotificationPasswordRecoveryEnabled = false;
        boolean isEmailOTPNotificationPasswordRecoveryEnabled = false;
        boolean isSmsOtpNotificationPasswordRecoveryEnabled = false;

        boolean isUsernameRecoveryEnabled = false;
        boolean isEmailUsernameRecoveryEnabled = false;
        boolean isSmsUsernameRecoveryEnabled = false;

        boolean isAdminForcePasswordResetEmailLinkEnabled = false;
        boolean isAdminForcePasswordResetEmailOTPEnabled = false;
        boolean isAdminForcePasswordResetSMSOTPEnabled = false;
        boolean isAdminForcePasswordResetOfflineEnabled = false;

        try {
            String sqlStmt = isH2DB() ? IdPManagementConstants.SQLQueries.GET_IDP_METADATA_BY_IDP_ID_H2 :
                    IdPManagementConstants.SQLQueries.GET_IDP_METADATA_BY_IDP_ID;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, idpId);
            rs = prepStmt.executeQuery();
            while (rs.next()) {
                IdentityProviderProperty property = new IdentityProviderProperty();
                property.setName(rs.getString("NAME"));
                if (IdPManagementConstants.NOTIFICATION_PASSWORD_ENABLE_PROPERTY.equals(property.getName())) {
                    isRecoveryNotificationPasswordRecoveryEnabled =
                            Boolean.parseBoolean(rs.getString("VALUE"));
                }
                if (IdPManagementConstants.EMAIL_LINK_PASSWORD_RECOVERY_PROPERTY.equals(property.getName())) {
                    isEmailLinkNotificationPasswordRecoveryEnabled =
                            Boolean.parseBoolean(rs.getString("VALUE"));
                }
                if (IdPManagementConstants.EMAIL_OTP_PASSWORD_RECOVERY_PROPERTY.equals(property.getName())) {
                    isEmailOTPNotificationPasswordRecoveryEnabled =
                            Boolean.parseBoolean(rs.getString("VALUE"));
                }
                if (IdPManagementConstants.SMS_OTP_PASSWORD_RECOVERY_PROPERTY.equals(property.getName())) {
                    isSmsOtpNotificationPasswordRecoveryEnabled =
                            Boolean.parseBoolean(rs.getString("VALUE"));
                }
                if (IdPManagementConstants.USERNAME_RECOVERY_PROPERTY.equals(property.getName())) {
                    isUsernameRecoveryEnabled = Boolean.parseBoolean(rs.getString("VALUE"));
                }
                if (IdPManagementConstants.EMAIL_USERNAME_RECOVERY_PROPERTY.equals(property.getName())) {
                    isEmailUsernameRecoveryEnabled = Boolean.parseBoolean(rs.getString("VALUE"));
                }
                if (IdPManagementConstants.SMS_USERNAME_RECOVERY_PROPERTY.equals(property.getName())) {
                    isSmsUsernameRecoveryEnabled = Boolean.parseBoolean(rs.getString("VALUE"));
                }
                if (IdPManagementConstants.ENABLE_ADMIN_PASSWORD_RESET_EMAIL_LINK_PROPERTY.equals(property.getName())) {
                    isAdminForcePasswordResetEmailLinkEnabled =
                            Boolean.parseBoolean(rs.getString("VALUE"));
                }
                if (IdPManagementConstants.ENABLE_ADMIN_PASSWORD_RESET_EMAIL_OTP_PROPERTY.equals(property.getName())) {
                    isAdminForcePasswordResetEmailOTPEnabled =
                            Boolean.parseBoolean(rs.getString("VALUE"));
                }
                if (IdPManagementConstants.ENABLE_ADMIN_PASSWORD_RESET_SMS_OTP_PROPERTY.equals(property.getName())) {
                    isAdminForcePasswordResetSMSOTPEnabled =
                            Boolean.parseBoolean(rs.getString("VALUE"));
                }
                if (IdPManagementConstants.ENABLE_ADMIN_PASSWORD_RESET_OFFLINE_PROPERTY.equals(property.getName())) {
                    isAdminForcePasswordResetOfflineEnabled =
                            Boolean.parseBoolean(rs.getString("VALUE"));
                }
                property.setValue(rs.getString("VALUE"));
                property.setDisplayName(rs.getString("DISPLAY_NAME"));
                idpProperties.add(property);
            }
            // If recovery notification are inconsistent, correct the configurations.
            if (isRecoveryNotificationPasswordRecoveryEnabled && !isEmailLinkNotificationPasswordRecoveryEnabled &&
                    !isEmailOTPNotificationPasswordRecoveryEnabled && !isSmsOtpNotificationPasswordRecoveryEnabled) {
                performConfigCorrectionForPasswordRecoveryConfigs(dbConnection, tenantId, idpId, idpProperties);
            }
            // If username recovery configs are inconsistent, correct the configurations.
            if (isUsernameRecoveryEnabled && !isEmailUsernameRecoveryEnabled && !isSmsUsernameRecoveryEnabled) {
                performConfigCorrectionForUsernameRecoveryConfigs(dbConnection, tenantId, idpId, idpProperties);
            }
            // If admin force password reset configs are inconsistent, correct the configurations.
            if (!isAdminForcePasswordResetEmailLinkEnabled && !isAdminForcePasswordResetEmailOTPEnabled
                    && !isAdminForcePasswordResetSMSOTPEnabled && !isAdminForcePasswordResetOfflineEnabled) {
                performConfigCorrectionForAdminForcedPasswordResetConfigs(idpProperties);
            }
        } catch (DataAccessException e) {
            throw new SQLException("Error while retrieving IDP properties for IDP ID: " + idpId, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(null, rs, prepStmt);
        }
        return idpProperties;
    }

    /**
     * Add Identity provider properties
     *
     * @param dbConnection
     * @param idpId
     * @param properties
     * @throws SQLException
     */
    private void addIdentityProviderProperties(Connection dbConnection, int idpId,
                                               List<IdentityProviderProperty> properties, int tenantId)
            throws SQLException {

        PreparedStatement prepStmt = null;
        try {
            boolean isOracleDB = isOracleDB();
            String sqlStmt = isH2DB() ? IdPManagementConstants.SQLQueries.ADD_IDP_METADATA_H2 :
                    IdPManagementConstants.SQLQueries.ADD_IDP_METADATA;
            prepStmt = dbConnection.prepareStatement(sqlStmt);

            for (IdentityProviderProperty property : properties) {
                if (isOracleDB ? StringUtils.isNotEmpty(property.getValue()) : property.getValue() != null) {
                    prepStmt.setInt(1, idpId);
                    prepStmt.setString(2, property.getName());
                    prepStmt.setString(3, property.getValue());
                    prepStmt.setString(4, property.getDisplayName());
                    prepStmt.setInt(5, tenantId);
                    prepStmt.addBatch();
                } else {
                    if (log.isDebugEnabled()) {
                        String msg = "IDP property '%s' of IDP with id:%d of tenantId:%d is null. " +
                                "Not adding the property to 'IDP_METADATA' table.";
                        log.debug(String.format(msg, property.getName(), idpId, tenantId));
                    }
                }
            }
            prepStmt.executeBatch();

        } catch (DataAccessException e) {
            String errorMsg = "Error while adding IDP properties for IDP ID: " + idpId + " and tenant ID:" + tenantId;
            throw new SQLException(errorMsg, e);
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
        }
    }

    /**
     * Update Identity provider properties
     *
     * @param dbConnection
     * @param idpId
     * @param properties
     * @throws SQLException
     */
    private void updateIdentityProviderProperties(Connection dbConnection, int idpId,
                                                  List<IdentityProviderProperty> properties, int tenantId)
            throws SQLException {

        PreparedStatement prepStmt = null;
        try {
            prepStmt = dbConnection.prepareStatement(IdPManagementConstants.SQLQueries.DELETE_IDP_METADATA);
            prepStmt.setInt(1, idpId);
            prepStmt.executeUpdate();

            addIdentityProviderProperties(dbConnection, idpId, properties, tenantId);
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
        }
    }

    /**
     * @param dbConnection
     * @param idPName
     * @param tenantId
     * @return
     * @throws IdentityProviderManagementException
     * @throws SQLException
     */
    private FederatedAuthenticatorConfig[] getFederatedAuthenticatorConfigs(
            Connection dbConnection, String idPName, IdentityProvider federatedIdp, int tenantId)
            throws IdentityProviderManagementException, SQLException {

        int idPId = getIdentityProviderIdentifier(dbConnection, idPName, tenantId);

        PreparedStatement prepStmt1 = null;
        PreparedStatement prepStmt2 = null;
        ResultSet rs = null;
        ResultSet proprs = null;
        String defaultAuthName = null;

        if (federatedIdp != null && federatedIdp.getDefaultAuthenticatorConfig() != null) {
            defaultAuthName = federatedIdp.getDefaultAuthenticatorConfig().getName();
        }

        String sqlStmt = IdPManagementConstants.SQLQueries.GET_ALL_IDP_AUTH_SQL;
        Set<FederatedAuthenticatorConfig> federatedAuthenticatorConfigs = new HashSet<FederatedAuthenticatorConfig>();
        try {
            prepStmt1 = dbConnection.prepareStatement(sqlStmt);
            prepStmt1.setInt(1, idPId);
            rs = prepStmt1.executeQuery();

            while (rs.next()) {
                FederatedAuthenticatorConfig authnConfig = createFederatedAuthenticatorConfig(DefinedByType.valueOf(
                                rs.getString(DEFINED_BY_COLUMN)));
                int authnId = rs.getInt("ID");
                authnConfig.setName(rs.getString("NAME"));

                if ((IdPManagementConstants.IS_TRUE_VALUE).equals(rs.getString("IS_ENABLED"))) {
                    authnConfig.setEnabled(true);
                } else {
                    authnConfig.setEnabled(false);
                }

                authnConfig.setDisplayName(rs.getString("DISPLAY_NAME"));

                if (defaultAuthName != null && authnConfig.getName().equals(defaultAuthName)) {
                    federatedIdp.getDefaultAuthenticatorConfig().setDisplayName(authnConfig.getDisplayName());
                }

                Set<Property> properties = new HashSet<Property>();
                if (isIdpPropertyInheritanceEnabled(IdentityTenantUtil.getTenantDomain(tenantId)) &&
                        federatedIdp != null &&
                        IdentityApplicationConstants.RESIDENT_IDP_RESERVED_NAME.equals(
                                federatedIdp.getIdentityProviderName()) &&
                        IdPManagementConstants.INHERITED_FEDERATED_AUTHENTICATORS.contains(authnConfig.getName())) {
                    properties = resolveInheritedFederatedAuthenticatorProperties(
                            tenantId, authnConfig.getName(), dbConnection);
                } else {
                    properties = getFederatedAuthenticatorProperties(dbConnection, authnId);
                }
                authnConfig.setProperties(properties.toArray(new Property[properties.size()]));
                if (isEmailOTPAuthenticator(authnConfig.getName())) {
                    // This is to support backward compatibility.
                    updateEmailOTPCharTypeProperty(authnConfig, true);
                }
                federatedAuthenticatorConfigs.add(authnConfig);
            }

            return federatedAuthenticatorConfigs
                    .toArray(new FederatedAuthenticatorConfig[federatedAuthenticatorConfigs.size()]);
        } catch (OrganizationManagementException e) {
            throw new IdentityProviderManagementException("Error while resolving organization ID for tenant: " +
                    IdentityTenantUtil.getTenantDomain(tenantId), e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(null, proprs, prepStmt2);
            IdentityDatabaseUtil.closeAllConnections(null, rs, prepStmt1);
        }
    }

    /**
     * Traverse upward the organization hierarchy and resolve the inherited properties for the given federated
     * authenticator.
     *
     * @param tenantId          The tenant ID.
     * @param authenticatorName The name of the federated authenticator.
     * @param dbConnection      The database connection.
     * @return A set of inherited properties for the federated authenticator.
     * @throws IdentityProviderManagementException If an error occurs while resolving the properties.
     */
    private Set<Property> resolveInheritedFederatedAuthenticatorProperties(int tenantId, String authenticatorName,
                                                                           Connection dbConnection)
            throws IdentityProviderManagementException {

        Set<Property> inheritedProperties = new HashSet<>();
        OrgResourceResolverService orgResourceResolverService = IdpMgtServiceComponentHolder.getInstance()
                .getOrgResourceResolverService();
        OrganizationManager orgManager =
                IdpMgtServiceComponentHolder.getInstance().getOrganizationManager();
        try {
            String orgId = orgManager.resolveOrganizationId(IdentityTenantUtil.getTenantDomain(tenantId));
            inheritedProperties = orgResourceResolverService.getResourcesFromOrgHierarchy(
                    orgId,
                    LambdaExceptionUtils.rethrowFunction(
                            orgID -> federatedAuthenticatorPropertyResolver(orgID, authenticatorName,
                                    dbConnection)),
                    new MergeAllAggregationStrategy<>(this::mergePropertySets));
            return inheritedProperties;
        } catch (OrganizationManagementException e) {
            throw new IdentityProviderManagementException("Error while resolving organization ID for tenant: " +
                    IdentityTenantUtil.getTenantDomain(tenantId), e);
        } catch (OrgResourceHierarchyTraverseException e) {
            throw new IdentityProviderManagementException("Error while resolving federated authenticator properties " +
                    "for tenant: " + IdentityTenantUtil.getTenantDomain(tenantId), e);
        }
    }

    /**
     * Resolves the properties of the given federated authenticator in the given organization.
     *
     * @param orgId            The organization ID.
     * @param authenticatorName The name of the federated authenticator.
     * @param dbConnection      The database connection.
     * @return An optional set of properties for the federated authenticator.
     * @throws IdentityProviderManagementException If an error occurs while resolving the properties.
     */
    private Optional<Set<Property>> federatedAuthenticatorPropertyResolver(String orgId, String authenticatorName,
                                                                 Connection dbConnection)
            throws IdentityProviderManagementException {

        OrganizationManager organizationManager = IdpMgtServiceComponentHolder.getInstance().getOrganizationManager();
        try {
            int tenantId = IdentityTenantUtil.getTenantId(organizationManager.resolveTenantDomain(orgId));
            int residentIdpId = getIdentityProviderIdentifier(dbConnection,
                    IdentityApplicationConstants.RESIDENT_IDP_RESERVED_NAME, tenantId);
            int authenticatorId = getAuthenticatorIdentifier(dbConnection, residentIdpId, authenticatorName);
            return Optional.of(getFederatedAuthenticatorProperties(dbConnection, authenticatorId));
        } catch (SQLException e) {
            throw new IdentityProviderManagementServerException("Error while retriving federated authenticator " +
                    "properties of" + authenticatorName + " in organization: " + orgId, e);
        } catch (OrganizationManagementException e) {
            throw new IdentityProviderManagementServerException("Error while resolving tenant domain of: " + orgId, e);
        }
    }

    /**
     * Get federated authenticator properties for the given authenticator id.
     *
     * @param dbConnection   database connection.
     * @param authenticatorId authenticator id.
     * @return Set of properties.
     * @throws SQLException if an error occurs while executing the SQL query.
     */
    private Set<Property> getFederatedAuthenticatorProperties(Connection dbConnection, int authenticatorId)
            throws SQLException {

        Set<Property> properties = new HashSet<Property>();
        String sqlStmt = IdPManagementConstants.SQLQueries.GET_IDP_AUTH_PROPS_SQL;
        PreparedStatement prepStmt = dbConnection.prepareStatement(sqlStmt);
        prepStmt.setInt(1, authenticatorId);
        ResultSet props = prepStmt.executeQuery();
        while (props.next()) {
            Property property = new Property();
            property.setName(props.getString("PROPERTY_KEY"));
            property.setValue(props.getString("PROPERTY_VALUE"));
            if ((IdPManagementConstants.IS_TRUE_VALUE).equals(props.getString("IS_SECRET"))) {
                property.setConfidential(true);
            }
            properties.add(property);
        }
        return properties;
    }

    /**
     * Merges two sets of properties, ensuring that properties with the same name are not duplicated.
     *
     * @param set1 The first set of properties.
     * @param set2 The second set of properties.
     * @return A new set containing the merged properties.
     */
    private Set<Property> mergePropertySets(Set<Property> set1, Set<Property> set2) {

        Set<String> existingPropertyNames = set1.stream()
                .map(Property::getName)
                .collect(Collectors.toSet());
        Set<Property> mergedSet = new HashSet<>(set1);

        for (Property property : set2) {
            if (!existingPropertyNames.contains(property.getName()) &&
                    IdPManagementConstants.INHERITED_FEDERATED_AUTHENTICATOR_PROPERTIES.contains(property.getName())) {
                mergedSet.add(property);
            }
        }
        return mergedSet;
    }

    /**
     * @param newFederatedAuthenticatorConfigs
     * @param oldFederatedAuthenticatorConfigs
     * @param dbConnection
     * @param idpId
     * @param tenantId
     * @throws IdentityProviderManagementException
     * @throws SQLException
     */
    private void updateFederatedAuthenticatorConfigs(
            FederatedAuthenticatorConfig[] newFederatedAuthenticatorConfigs,
            FederatedAuthenticatorConfig[] oldFederatedAuthenticatorConfigs,
            Connection dbConnection, int idpId, int tenantId, boolean isResidentIdP)
            throws IdentityProviderManagementException, SQLException {

        Map<String, FederatedAuthenticatorConfig> oldFedAuthnConfigMap = new HashMap<>();
        if (oldFederatedAuthenticatorConfigs != null && oldFederatedAuthenticatorConfigs.length > 0) {
            for (FederatedAuthenticatorConfig fedAuthnConfig : oldFederatedAuthenticatorConfigs) {
                oldFedAuthnConfigMap.put(fedAuthnConfig.getName(), fedAuthnConfig);
            }
        }

        Map<String, FederatedAuthenticatorConfig> newFedAuthnConfigMap = new HashMap<>();
        if (newFederatedAuthenticatorConfigs != null && newFederatedAuthenticatorConfigs.length > 0) {
            for (FederatedAuthenticatorConfig fedAuthenticator : newFederatedAuthenticatorConfigs) {
                newFedAuthnConfigMap.put(fedAuthenticator.getName(), fedAuthenticator);
                if (fedAuthenticator.isValid()) {
                    if (oldFedAuthnConfigMap.containsKey(fedAuthenticator.getName())) {
                        if (isEmailOTPAuthenticator(fedAuthenticator.getName())) {
                            updateEmailOTPCharTypeProperty(fedAuthenticator, false);
                        }
                        updateFederatedAuthenticatorConfig(fedAuthenticator,
                                oldFedAuthnConfigMap.get(fedAuthenticator.getName()),
                                dbConnection, idpId, tenantId);
                    } else {
                        addFederatedAuthenticatorConfig(fedAuthenticator, dbConnection, idpId, tenantId);
                    }
                }
            }
        }

        if (!isResidentIdP) {
            // Remove deleted federated authenticator configs.
            for (String oldFedAuthenticator : oldFedAuthnConfigMap.keySet()) {
                if (!newFedAuthnConfigMap.containsKey(oldFedAuthenticator)) {
                    deleteFederatedAuthenticatorConfig(oldFedAuthnConfigMap.get(oldFedAuthenticator), dbConnection,
                            idpId, tenantId);
                }
            }
        }
    }

    /**
     * This method writes AlphanumericCharactersForOtp config value to OnlyNumericCharactersForOtp.
     *
     * @param emailOTPAuthenticator EmailOTP authenticator.
     * @param isReadIDP             Whether the operation is reading or updating IDP data.
     */
    private void updateEmailOTPCharTypeProperty(FederatedAuthenticatorConfig emailOTPAuthenticator, boolean isReadIDP) {

        Property alphaNumericCharactersForOtp = new Property();
        Property onlyNumericCharactersForOtp = new Property();
        onlyNumericCharactersForOtp.setName(EMAIL_OTP_ONLY_NUMERIC_CHARS_PROPERTY);
        alphaNumericCharactersForOtp.setName(EMAIL_OTP_USE_ALPHANUMERIC_CHARS_PROPERTY);

        ArrayList<Property> emailOTPProperties = new ArrayList<Property>();
        for (Property property : emailOTPAuthenticator.getProperties()) {
            if (EMAIL_OTP_USE_ALPHANUMERIC_CHARS_PROPERTY.equals(property.getName())) {
                alphaNumericCharactersForOtp = property;
                continue;
            }
            if (EMAIL_OTP_ONLY_NUMERIC_CHARS_PROPERTY.equals(property.getName())) {
                onlyNumericCharactersForOtp = property;
                continue;
            }
            emailOTPProperties.add(property);
        }

        if (isReadIDP) {
            /* 
            If the operation is read IDP configs, read value of onlyNumericCharactersForOtp and 
            set the value of alphaNumericCharactersForOtp accordingly.
            */
            if (StringUtils.isNotBlank(onlyNumericCharactersForOtp.getValue())) {
                boolean useNumericCharactersForOtpValue = Boolean.parseBoolean(onlyNumericCharactersForOtp.getValue());
                alphaNumericCharactersForOtp.setValue(String.valueOf(!useNumericCharactersForOtpValue));
            } else {
                // Default case for email OTP char type.
                onlyNumericCharactersForOtp.setValue("true");
                alphaNumericCharactersForOtp.setValue("false");
            }
        } else {
            /*
            If the operation is update IDP configs, read value of alphaNumericCharactersForOtp and
            set the value of onlyNumericCharactersForOtp accordingly.
            */
            if (StringUtils.isNotBlank(alphaNumericCharactersForOtp.getValue())) {
                boolean alphaNumericCharactersForOtpValue = Boolean.parseBoolean(alphaNumericCharactersForOtp.getValue());
                onlyNumericCharactersForOtp.setValue(String.valueOf(!alphaNumericCharactersForOtpValue));
            } else {
                // Default case for email OTP char type.
                onlyNumericCharactersForOtp.setValue("true");
                alphaNumericCharactersForOtp.setValue("false");
            }
            // Add the OnlyNumericCharactersForOtp property to the list only when updating.
            emailOTPProperties.add(onlyNumericCharactersForOtp);
        }

        // Add alphaNumericCharactersForOtp property to emailOTPAuthenticator property list.
        emailOTPProperties.add(alphaNumericCharactersForOtp);
        emailOTPAuthenticator.setProperties(emailOTPProperties.toArray(new Property[0]));
    }

    /**
     * This method is used to check whether the authenticator is email OTP connector or not.
     *
     * @param authenticatorName   Name of the authenticator.
     *
     * @return True if the authenticator is EmailOTP.
     */
    private boolean isEmailOTPAuthenticator(String authenticatorName) {

        return EMAIL_OTP_AUTHENTICATOR_NAME.equals(authenticatorName);
    }

    /**
     * @param newFederatedAuthenticatorConfig
     * @param oldFederatedAuthenticatorConfig
     * @param dbConnection
     * @param idpId
     * @throws IdentityProviderManagementException
     * @throws SQLException
     */
    private void updateFederatedAuthenticatorConfig(FederatedAuthenticatorConfig newFederatedAuthenticatorConfig,
                                                    FederatedAuthenticatorConfig oldFederatedAuthenticatorConfig,
                                                    Connection dbConnection, int idpId, int tenantId) throws
            IdentityProviderManagementException, SQLException {

        PreparedStatement prepStmt1 = null;

        try {
            String sqlStmt = IdPManagementConstants.SQLQueries.UPDATE_IDP_AUTH_SQL;
            prepStmt1 = dbConnection.prepareStatement(sqlStmt);

            if (newFederatedAuthenticatorConfig.isEnabled()) {
                prepStmt1.setString(1, IdPManagementConstants.IS_TRUE_VALUE);
            } else {
                prepStmt1.setString(1, IdPManagementConstants.IS_FALSE_VALUE);
            }
            prepStmt1.setInt(2, idpId);
            prepStmt1.setString(3, newFederatedAuthenticatorConfig.getName());
            prepStmt1.executeUpdate();

            int authnId = getAuthenticatorIdentifier(dbConnection, idpId,
                    newFederatedAuthenticatorConfig.getName());

            List<Property> unUpdatedProperties = new ArrayList<>();
            List<Property> singleValuedProperties = new ArrayList<>();
            List<Property> multiValuedProperties = new ArrayList<>();

            // Checking for old fed auth config properties that are not updated so we can delete them.
            if (ArrayUtils.isNotEmpty(oldFederatedAuthenticatorConfig.getProperties())) {
                if (ArrayUtils.isNotEmpty(newFederatedAuthenticatorConfig.getProperties())) {
                    for (Property propertyOld : oldFederatedAuthenticatorConfig.getProperties()) {
                        boolean hasProp = false;
                        for (Property propertyNew : newFederatedAuthenticatorConfig.getProperties()) {
                            if (StringUtils.equals(propertyOld.getName(), propertyNew.getName())) {
                                hasProp = true;
                                break;
                            }
                        }
                        if (!hasProp) {
                            unUpdatedProperties.add(propertyOld);
                        }
                    }
                } else {
                    unUpdatedProperties =
                            new ArrayList<>(Arrays.asList(oldFederatedAuthenticatorConfig.getProperties()));
                }
            }

            for (Property property : newFederatedAuthenticatorConfig.getProperties()) {
                if (Pattern.matches(IdPManagementConstants.MULTI_VALUED_PROPERT_IDENTIFIER_PATTERN, property.getName
                        ())) {
                    multiValuedProperties.add(property);
                } else {
                    singleValuedProperties.add(property);
                }
            }

            if (CollectionUtils.isNotEmpty(unUpdatedProperties)) {
                deleteFederatedConfigProperties(dbConnection, authnId, tenantId, unUpdatedProperties);
            }
            if (CollectionUtils.isNotEmpty(singleValuedProperties)) {
                updateSingleValuedFederatedConfigProperties(dbConnection, authnId, tenantId, singleValuedProperties);
            }
            if (CollectionUtils.isNotEmpty(multiValuedProperties)) {
                updateMultiValuedFederatedConfigProperties(dbConnection, oldFederatedAuthenticatorConfig
                        .getProperties(), authnId, tenantId, multiValuedProperties);
            }
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt1);
        }
    }

    /**
     * @param authnConfigs
     * @param dbConnection
     * @param idpId
     * @throws IdentityProviderManagementException
     * @throws SQLException
     */
    public void addFederatedAuthenticatorConfigs(FederatedAuthenticatorConfig[] authnConfigs,
                                                 Connection dbConnection, int idpId, int tenantId)
            throws IdentityProviderManagementException, SQLException {

        for (FederatedAuthenticatorConfig authnConfig : authnConfigs) {
            addFederatedAuthenticatorConfig(authnConfig, dbConnection, idpId, tenantId);
        }
    }

    public void addFederatedAuthenticatorConfig(FederatedAuthenticatorConfig authnConfig,
                                                Connection dbConnection, int idpId, int tenantId)
            throws IdentityProviderManagementException, SQLException {

        PreparedStatement prepStmt1 = null;
        PreparedStatement prepStmt2 = null;
        String sqlStmt = IdPManagementConstants.SQLQueries.ADD_IDP_AUTH_SQL;

        try {
            prepStmt1 = dbConnection.prepareStatement(sqlStmt);
            prepStmt1.setInt(1, idpId);
            prepStmt1.setInt(2, tenantId);
            if (authnConfig.isEnabled()) {
                prepStmt1.setString(3, IdPManagementConstants.IS_TRUE_VALUE);
            } else {
                prepStmt1.setString(3, IdPManagementConstants.IS_FALSE_VALUE);
            }
            prepStmt1.setString(4, authnConfig.getName());
            prepStmt1.setString(5, authnConfig.getDisplayName());
            prepStmt1.setString(6, authnConfig.getDefinedByType().toString());
            /* Federated authenticators are always intended to authenticate externally managed users and share user
            identity and attributes. Therefore, always will be the 'IDENTIFICATION' type. */
            prepStmt1.setString(7, AuthenticationType.IDENTIFICATION.toString());
            prepStmt1.execute();

            int authnId = getAuthenticatorIdentifier(dbConnection, idpId, authnConfig.getName());

            sqlStmt = IdPManagementConstants.SQLQueries.ADD_IDP_AUTH_PROP_SQL;

            if (authnConfig.getProperties() == null) {
                authnConfig.setProperties(new Property[0]);
            }
            for (Property property : authnConfig.getProperties()) {

                prepStmt2 = dbConnection.prepareStatement(sqlStmt);
                prepStmt2.setInt(1, authnId);
                prepStmt2.setInt(2, tenantId);
                prepStmt2.setString(3, property.getName());
                prepStmt2.setString(4, property.getValue());
                if (property.isConfidential()) {
                    prepStmt2.setString(5, IdPManagementConstants.IS_TRUE_VALUE);
                } else {
                    prepStmt2.setString(5, IdPManagementConstants.IS_FALSE_VALUE);
                }
                prepStmt2.executeUpdate();
            }
        } finally {

            IdentityDatabaseUtil.closeStatement(prepStmt2);
            IdentityDatabaseUtil.closeStatement(prepStmt1);
        }
    }

    private void deleteFederatedAuthenticatorConfig(FederatedAuthenticatorConfig authnConfig,
                                                    Connection dbConnection, int idpId, int tenantId)
            throws IdentityProviderManagementException, SQLException {

        try (PreparedStatement prepStmt = dbConnection.prepareStatement(IdPManagementConstants.SQLQueries
                .DELETE_IDP_AUTH_SQL)) {
            prepStmt.setInt(1, idpId);
            prepStmt.setString(2, authnConfig.getName());
            prepStmt.execute();
        }
    }

    private void updateSingleValuedFederatedConfigProperties(Connection dbConnection, int authnId, int tenantId,
                                                             List<Property> singleValuedProperties) throws
            SQLException {

        PreparedStatement prepStmt2 = null;
        PreparedStatement prepStmt3 = null;
        String sqlStmt;

        try {
            for (Property property : singleValuedProperties) {

                sqlStmt = IdPManagementConstants.SQLQueries.UPDATE_IDP_AUTH_PROP_SQL;
                prepStmt2 = dbConnection.prepareStatement(sqlStmt);
                prepStmt2.setString(1, property.getValue());
                if (property.isConfidential()) {
                    prepStmt2.setString(2, IdPManagementConstants.IS_TRUE_VALUE);
                } else {
                    prepStmt2.setString(2, IdPManagementConstants.IS_FALSE_VALUE);
                }
                prepStmt2.setInt(3, authnId);
                prepStmt2.setString(4, property.getName());
                int rows = prepStmt2.executeUpdate();

                if (rows == 0) {
                    // this should be an insert.
                    sqlStmt = IdPManagementConstants.SQLQueries.ADD_IDP_AUTH_PROP_SQL;
                    prepStmt3 = dbConnection.prepareStatement(sqlStmt);
                    prepStmt3.setInt(1, authnId);
                    prepStmt3.setInt(2, tenantId);
                    prepStmt3.setString(3, property.getName());
                    prepStmt3.setString(4, property.getValue());
                    if (property.isConfidential()) {
                        prepStmt3.setString(5, IdPManagementConstants.IS_TRUE_VALUE);
                    } else {
                        prepStmt3.setString(5, IdPManagementConstants.IS_FALSE_VALUE);
                    }

                    prepStmt3.executeUpdate();
                }

            }
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt3);
            IdentityDatabaseUtil.closeStatement(prepStmt2);
        }
    }

    private void updateMultiValuedFederatedConfigProperties(Connection dbConnection, Property[]
            oldFederatedAuthenticatorConfigProperties, int authnId, int tenantId, List<Property>
                                                                    multiValuedProperties) throws SQLException {

        PreparedStatement deleteOldValuePrepStmt = null;
        PreparedStatement addNewPropsPrepStmt = null;
        String sqlStmt;
        try {
            for (Property property : oldFederatedAuthenticatorConfigProperties) {
                if (Pattern.matches(IdPManagementConstants.MULTI_VALUED_PROPERT_IDENTIFIER_PATTERN, property.getName
                        ())) {
                    sqlStmt = IdPManagementConstants.SQLQueries.DELETE_IDP_AUTH_PROP_WITH_KEY_SQL;
                    deleteOldValuePrepStmt = dbConnection.prepareStatement(sqlStmt);
                    deleteOldValuePrepStmt.setString(1, property.getName());
                    deleteOldValuePrepStmt.setInt(2, tenantId);
                    deleteOldValuePrepStmt.setInt(3, authnId);
                    deleteOldValuePrepStmt.executeUpdate();
                }
            }

            for (Property property : multiValuedProperties) {
                sqlStmt = IdPManagementConstants.SQLQueries.ADD_IDP_AUTH_PROP_SQL;
                addNewPropsPrepStmt = dbConnection.prepareStatement(sqlStmt);
                addNewPropsPrepStmt.setInt(1, authnId);
                addNewPropsPrepStmt.setInt(2, tenantId);
                addNewPropsPrepStmt.setString(3, property.getName());
                addNewPropsPrepStmt.setString(4, property.getValue());
                if (property.isConfidential()) {
                    addNewPropsPrepStmt.setString(5, IdPManagementConstants.IS_TRUE_VALUE);
                } else {
                    addNewPropsPrepStmt.setString(5, IdPManagementConstants.IS_FALSE_VALUE);
                }

                addNewPropsPrepStmt.executeUpdate();
            }

        } finally {
            IdentityDatabaseUtil.closeStatement(deleteOldValuePrepStmt);
            IdentityDatabaseUtil.closeStatement(addNewPropsPrepStmt);
        }

    }

    private void deleteFederatedConfigProperties(Connection dbConnection, int authnId, int tenantId, List<Property>
            properties) throws SQLException {

        if (CollectionUtils.isEmpty(properties)) {
            return;
        }

        PreparedStatement deletePrepStmt = null;
        String sqlStmt = IdPManagementConstants.SQLQueries.DELETE_IDP_AUTH_PROP_WITH_KEY_SQL;
        try {
            deletePrepStmt = dbConnection.prepareStatement(sqlStmt);
            for (Property property : properties) {
                deletePrepStmt.setString(1, property.getName());
                deletePrepStmt.setInt(2, tenantId);
                deletePrepStmt.setInt(3, authnId);
                deletePrepStmt.addBatch();
            }
            deletePrepStmt.executeBatch();
        } finally {
            IdentityDatabaseUtil.closeStatement(deletePrepStmt);
        }
    }

    /**
     * @param dbConnection
     * @param idPName
     * @param userClaimUri
     * @param roleClaimUri
     * @param idpId
     * @param tenantId
     * @return
     * @throws SQLException
     */
    private ClaimConfig getLocalIdPDefaultClaimValues(Connection dbConnection, String idPName,
                                                      String userClaimUri, String roleClaimUri,
                                                      int idpId, int tenantId) throws SQLException {

        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String sqlStmt;
        ClaimConfig claimConfig = new ClaimConfig();

        try {

            claimConfig.setLocalClaimDialect(true);
            claimConfig.setRoleClaimURI(roleClaimUri);
            claimConfig.setUserClaimURI(userClaimUri);

            sqlStmt = IdPManagementConstants.SQLQueries.GET_LOCAL_IDP_DEFAULT_CLAIM_VALUES_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);

            prepStmt.setInt(1, idpId);
            prepStmt.setInt(2, tenantId);

            List<ClaimMapping> claimMappings = new ArrayList<ClaimMapping>();

            rs = prepStmt.executeQuery();

            while (rs.next()) {
                ClaimMapping claimMapping = new ClaimMapping();

                // empty claim.
                Claim remoteClaim = new Claim();

                Claim localClaim = new Claim();
                localClaim.setClaimUri(rs.getString("CLAIM_URI"));

                claimMapping.setLocalClaim(localClaim);
                claimMapping.setRemoteClaim(remoteClaim);
                claimMapping.setDefaultValue(rs.getString("DEFAULT_VALUE"));

                if ((IdPManagementConstants.IS_TRUE_VALUE).equals(rs.getString("IS_REQUESTED"))) {
                    claimMapping.setRequested(true);
                } else if (rs.getString("IS_REQUESTED").equals(IdPManagementConstants.IS_TRUE_VALUE)) {
                    claimMapping.setRequested(false);
                }

                claimMappings.add(claimMapping);
            }

            claimConfig.setClaimMappings(claimMappings.toArray(new ClaimMapping[claimMappings
                    .size()]));

            return claimConfig;

        } finally {
            IdentityDatabaseUtil.closeAllConnections(null, rs, prepStmt);
        }
    }

    /**
     * @param dbConnection
     * @param idPName
     * @param tenantId
     * @return
     * @throws IdentityProviderManagementException
     * @throws SQLException
     */
    private ClaimConfig getIdPClaimConfiguration(Connection dbConnection, String idPName,
                                                 String userClaimUri, String roleClaimUri, int idPId, int tenantId)
            throws SQLException {

        PreparedStatement prepStmt1 = null;
        PreparedStatement prepStmt2 = null;
        ResultSet rs1 = null;
        ResultSet rs2 = null;

        try {

            List<Claim> claimList = new ArrayList<Claim>();
            // SP_IDP_CLAIM_ID, SP_IDP_CLAIM
            String sqlStmt = IdPManagementConstants.SQLQueries.GET_IDP_CLAIMS_SQL;
            prepStmt1 = dbConnection.prepareStatement(sqlStmt);
            prepStmt1.setInt(1, idPId);
            rs1 = prepStmt1.executeQuery();

            ClaimConfig claimConfig = new ClaimConfig();

            while (rs1.next()) {
                Claim identityProviderClaim = new Claim();
                identityProviderClaim.setClaimId(rs1.getInt(1));
                identityProviderClaim.setClaimUri(rs1.getString(2));
                claimList.add(identityProviderClaim);
            }

            // populate claim configuration with identity provider claims.
            claimConfig.setIdpClaims(claimList.toArray(new Claim[claimList.size()]));

            claimConfig.setUserClaimURI(userClaimUri);
            claimConfig.setRoleClaimURI(roleClaimUri);

            List<ClaimMapping> claimMappings = new ArrayList<ClaimMapping>();

            // SP_IDP_CLAIMS.SP_IDP_CLAIM SP_IDP_CLAIM_MAPPINGS.SP_LOCAL_CLAIM
            sqlStmt = IdPManagementConstants.SQLQueries.GET_IDP_CLAIM_MAPPINGS_SQL;
            prepStmt2 = dbConnection.prepareStatement(sqlStmt);
            prepStmt2.setInt(1, idPId);
            rs2 = prepStmt2.executeQuery();

            while (rs2.next()) {
                ClaimMapping claimMapping = new ClaimMapping();

                Claim idpClaim = new Claim();
                idpClaim.setClaimUri(rs2.getString("CLAIM"));

                Claim localClaim = new Claim();
                localClaim.setClaimUri(rs2.getString("LOCAL_CLAIM"));

                claimMapping.setLocalClaim(localClaim);
                claimMapping.setRemoteClaim(idpClaim);
                claimMapping.setDefaultValue(rs2.getString("DEFAULT_VALUE"));
                if ((IdPManagementConstants.IS_TRUE_VALUE).equals(rs2.getString("IS_REQUESTED"))) {
                    claimMapping.setRequested(true);
                } else if ((IdPManagementConstants.IS_FALSE_VALUE).equals(rs2.getString("IS_REQUESTED"))) {
                    claimMapping.setRequested(false);
                }
                claimMappings.add(claimMapping);

            }

            claimConfig.setClaimMappings(claimMappings.toArray(new ClaimMapping[claimMappings
                    .size()]));

            return claimConfig;
        } finally {
            IdentityDatabaseUtil.closeAllConnections(null, rs2, prepStmt2);
            IdentityDatabaseUtil.closeAllConnections(null, rs1, prepStmt1);
        }
    }

    /**
     * @param dbConnection
     * @param idPName
     * @param tenantId
     * @return
     * @throws IdentityProviderManagementException
     * @throws SQLException
     */
    public PermissionsAndRoleConfig getPermissionsAndRoleConfiguration(Connection dbConnection,
                                                                       String idPName, int idPId, int tenantId)
            throws SQLException {

        PreparedStatement prepStmt1 = null;
        PreparedStatement prepStmt2 = null;
        ResultSet rs1 = null;
        ResultSet rs2 = null;
        PermissionsAndRoleConfig permissionRoleConfiguration = new PermissionsAndRoleConfig();

        try {

            List<String> idpRoleList = new ArrayList<String>();
            // SP_IDP_ROLE
            String sqlStmt = IdPManagementConstants.SQLQueries.GET_IDP_ROLES_SQL;
            prepStmt1 = dbConnection.prepareStatement(sqlStmt);
            prepStmt1.setInt(1, idPId);
            rs1 = prepStmt1.executeQuery();
            while (rs1.next()) {
                idpRoleList.add(rs1.getString("ROLE"));
            }

            permissionRoleConfiguration.setIdpRoles(idpRoleList.toArray(new String[idpRoleList
                    .size()]));

            List<RoleMapping> roleMappings = new ArrayList<RoleMapping>();
            // SP_IDP_ROLE_MAPPINGS.SP_USER_STORE_ID, SP_IDP_ROLE_MAPPINGS.SP_LOCAL_ROLE,
            // SP_IDP_ROLES.SP_IDP_ROLE

            sqlStmt = IdPManagementConstants.SQLQueries.GET_IDP_ROLE_MAPPINGS_SQL;
            prepStmt2 = dbConnection.prepareStatement(sqlStmt);
            prepStmt2.setInt(1, idPId);
            rs2 = prepStmt2.executeQuery();
            while (rs2.next()) {
                LocalRole localRole = new LocalRole(rs2.getString("USER_STORE_ID"),
                        rs2.getString("LOCAL_ROLE"));
                RoleMapping roleMapping = new RoleMapping(localRole, rs2.getString("ROLE"));
                roleMappings.add(roleMapping);
            }

            permissionRoleConfiguration.setRoleMappings(roleMappings
                    .toArray(new RoleMapping[roleMappings.size()]));
            return permissionRoleConfiguration;
        } finally {
            IdentityDatabaseUtil.closeAllConnections(null, rs2, prepStmt2);
            IdentityDatabaseUtil.closeAllConnections(null, rs1, prepStmt1);
        }
    }

    /**
     * Get the Identity Provider Group Configuration.
     *
     * @param dbConnection Connection to the database.
     * @param idPId        Identity Provider ID.
     * @return IdPGroupConfig Identity Provider Group Configuration.
     * @throws SQLException Error when executing getting idp groups from database.
     */
    public IdPGroup[] getIdPGroupConfiguration(Connection dbConnection, int idPId)
            throws SQLException {

        IdPGroup[] idPGroupConfiguration;

        try (PreparedStatement getIdPGroupsConfigPrepStmt = dbConnection.prepareStatement(
                IdPManagementConstants.SQLQueries.GET_IDP_GROUPS_SQL)) {

            List<IdPGroup> idpGroupList = new ArrayList<>();
            getIdPGroupsConfigPrepStmt.setInt(1, idPId);

            try (ResultSet getIdPGroupsConfigResultSet = getIdPGroupsConfigPrepStmt.executeQuery()) {
                while (getIdPGroupsConfigResultSet.next()) {
                    IdPGroup idPGroup = new IdPGroup();
                    idPGroup.setIdpGroupName(getIdPGroupsConfigResultSet.getString(2));
                    idPGroup.setIdpGroupId(getIdPGroupsConfigResultSet.getString(3));
                    idpGroupList.add(idPGroup);
                }
            }
            idPGroupConfiguration = idpGroupList.toArray(new IdPGroup[0]);
        }
        return idPGroupConfiguration;
    }

    /**
     * @param provisioningConnectors
     * @param dbConnection
     * @param idpId
     * @throws IdentityProviderManagementException
     * @throws SQLException
     */
    private void addProvisioningConnectorConfigs(
            ProvisioningConnectorConfig[] provisioningConnectors, Connection dbConnection,
            int idpId, int tenantId) throws IdentityProviderManagementException, SQLException {

        PreparedStatement prepStmt = null;
        PreparedStatement prepBaseStmt = null;
        ResultSet rs = null;

        try {
            // SP_IDP_ID,SP_IDP_PROV_CONNECTOR_TYPE, SP_IDP_PROV_CONFIG_KEY,
            // SP_IDP_PROV_CONFIG_VALUE, SP_IDP_PROV_CONFIG_IS_SECRET

            // SP_IDP_PROV_CONFIG_PROPERTY
            // TENANT_ID, PROVISIONING_CONFIG_ID, PROPERTY_KEY, PROPERTY_VALUE, PROPERTY_TYPE,
            // IS_SECRET
            String sqlStmt = IdPManagementConstants.SQLQueries.ADD_IDP_PROVISIONING_PROPERTY_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);

            String sqlBaseStmt = IdPManagementConstants.SQLQueries.ADD_IDP_PROVISIONING_CONFIG_SQL;
            String dbProductName = dbConnection.getMetaData().getDatabaseProductName();
            prepBaseStmt = dbConnection.prepareStatement(sqlBaseStmt,
                    new String[]{DBUtils.getConvertedAutoGeneratedColumnName(dbProductName, "ID")});

            if (provisioningConnectors != null) {
                for (ProvisioningConnectorConfig connector : provisioningConnectors) {
                    Property[] connctorProperties = connector.getProvisioningProperties();

                    if (connctorProperties != null) {

                        // SP_IDP_PROVISIONING_CONFIG
                        // TENANT_ID, IDP_ID, PROVISIONING_CONNECTOR_TYPE, IS_ENABLED, IS_DEFAULT
                        prepBaseStmt.setInt(1, tenantId);
                        prepBaseStmt.setInt(2, idpId);
                        prepBaseStmt.setString(3, connector.getName());

                        if (connector.isEnabled()) {
                            prepBaseStmt.setString(4, IdPManagementConstants.IS_TRUE_VALUE);
                        } else {
                            prepBaseStmt.setString(4, IdPManagementConstants.IS_FALSE_VALUE);
                        }

                        if (connector.isBlocking()) {
                            prepBaseStmt.setString(5, IdPManagementConstants.IS_TRUE_VALUE);
                        } else {
                            prepBaseStmt.setString(5, IdPManagementConstants.IS_FALSE_VALUE);
                        }

                        prepBaseStmt.executeUpdate();
                        rs = prepBaseStmt.getGeneratedKeys();

                        if (rs.next()) {
                            int provisioningConfigID = rs.getInt(1);

                            if (connctorProperties.length > 0) {
                                for (Property config : connctorProperties) {

                                    if (config == null) {
                                        continue;
                                    }

                                    // SP_IDP_PROV_CONFIG_PROPERTY
                                    //TENANT_ID, PROVISIONING_CONFIG_ID, PROPERTY_KEY,
                                    // PROPERTY_VALUE, PROPERTY_BLOB_VALUE, PROPERTY_TYPE, IS_SECRET
                                    prepStmt.setInt(1, tenantId);
                                    prepStmt.setInt(2, provisioningConfigID);
                                    prepStmt.setString(3, config.getName());

                                    // TODO : Sect property type accordingly
                                    if (IdentityApplicationConstants.ConfigElements.PROPERTY_TYPE_BLOB.equals
                                            (config.getType())) {
                                        prepStmt.setString(4, null);
                                        setBlobValue(config.getValue(), prepStmt, 5);
                                        prepStmt.setString(6, config.getType());
                                    } else {
                                        prepStmt.setString(4, config.getValue());
                                        setBlobValue(null, prepStmt, 5);
                                        prepStmt.setString(6, IdentityApplicationConstants.ConfigElements.
                                                PROPERTY_TYPE_STRING);
                                    }

                                    if (config.isConfidential()) {
                                        prepStmt.setString(7, IdPManagementConstants.IS_TRUE_VALUE);
                                    } else {
                                        prepStmt.setString(7, IdPManagementConstants.IS_FALSE_VALUE);
                                    }
                                    prepStmt.addBatch();

                                }
                            }

                        }

                        // Adding properties for base config
                        prepStmt.executeBatch();

                    }
                }
            }
        } catch (IOException e) {
            throw new IdentityProviderManagementException("An error occurred while processing content stream.", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(null, rs, prepStmt);
            IdentityDatabaseUtil.closeStatement(prepBaseStmt);
        }
    }

    private void setBlobValue(String value, PreparedStatement prepStmt, int index) throws SQLException, IOException {

        if (value != null) {
            InputStream inputStream = new ByteArrayInputStream(value.getBytes());
            prepStmt.setBinaryStream(index, inputStream, inputStream.available());
        } else {
            prepStmt.setBinaryStream(index, new ByteArrayInputStream(new byte[0]), 0);
        }
    }

    /**
     * @param newProvisioningConnectorConfigs
     * @param dbConnection
     * @param idpId
     * @throws IdentityProviderManagementException
     * @throws SQLException
     */
    private void updateProvisioningConnectorConfigs(
            ProvisioningConnectorConfig[] newProvisioningConnectorConfigs, Connection dbConnection,
            int idpId, int tenantId) throws IdentityProviderManagementException, SQLException {

        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        try {
            if (!isEnableResetProvisioningEntitiesOnConfigUpdate()) {
                if (newProvisioningConnectorConfigs != null && newProvisioningConnectorConfigs.length > 0) {
                    updateProvisioningConfigProperty(newProvisioningConnectorConfigs, dbConnection,
                            idpId, tenantId);
                }
            } else {
                deleteProvisioningConnectorConfigs(dbConnection, idpId);

                if (newProvisioningConnectorConfigs != null && newProvisioningConnectorConfigs.length > 0) {
                    addProvisioningConnectorConfigs(newProvisioningConnectorConfigs, dbConnection,
                            idpId, tenantId);
                }
            }
        } finally {
            IdentityDatabaseUtil.closeAllConnections(null, rs, prepStmt);
        }
    }

    private void updateProvisioningConfigProperty(ProvisioningConnectorConfig[] provisioningConnectors,
                                                  Connection dbConnection, int idpId, int tenantId)
            throws IdentityProviderManagementException, SQLException {

        String sqlStmt = IdPManagementConstants.SQLQueries.UPDATE_IDP_PROVISIONING_CONFIG_PROPERTY_SQL;
        try (PreparedStatement prepStmt = dbConnection.prepareStatement(sqlStmt)) {
            for (ProvisioningConnectorConfig connector : provisioningConnectors) {
                if (isProvisioningConfigAvailableToUpdate(connector, dbConnection, idpId, tenantId)) {
                    updateProvisioningConfig(connector, dbConnection, idpId, tenantId);
                    Property[] connectorProperties = connector.getProvisioningProperties();
                    if (connectorProperties != null && connectorProperties.length > 0) {
                        for (Property config : connectorProperties) {
                            if (config == null) {
                                continue;
                            }
                            prepStmt.setString(1, config.getName());
                            if (IdentityApplicationConstants.ConfigElements.PROPERTY_TYPE_BLOB.equals
                                    (config.getType())) {
                                prepStmt.setString(2, null);
                                setBlobValue(config.getValue(), prepStmt, 3);
                                prepStmt.setString(4, config.getType());
                            } else {
                                prepStmt.setString(2, config.getValue());
                                setBlobValue(null, prepStmt, 3);
                                prepStmt.setString(4, IdentityApplicationConstants.ConfigElements.
                                        PROPERTY_TYPE_STRING);
                            }

                            if (config.isConfidential()) {
                                prepStmt.setString(5, IdPManagementConstants.IS_TRUE_VALUE);
                            } else {
                                prepStmt.setString(5, IdPManagementConstants.IS_FALSE_VALUE);
                            }
                            prepStmt.setInt(6, idpId);
                            prepStmt.setInt(7, tenantId);
                            prepStmt.setString(8, connector.getName());
                            prepStmt.setInt(9, tenantId);
                            prepStmt.setString(10, config.getName());
                            prepStmt.executeUpdate();
                        }
                    }
                } else {
                    addProvisioningConnectorConfigs(new ProvisioningConnectorConfig[]{connector}, dbConnection, idpId,
                            tenantId);
                }
            }
        } catch (IOException e) {
            throw new IdentityProviderManagementException("An error occurred when processing content stream while " +
                    "updating provisioning config properties of Identity Provider : " + idpId, e);
        }
    }

    private boolean isEnableResetProvisioningEntitiesOnConfigUpdate() {

        boolean resetProvisioningEntities = true;

        if (StringUtils.isNotEmpty(IdentityUtil.getProperty(RESET_PROVISIONING_ENTITIES_ON_CONFIG_UPDATE))) {
            resetProvisioningEntities = Boolean
                    .parseBoolean(IdentityUtil.getProperty(RESET_PROVISIONING_ENTITIES_ON_CONFIG_UPDATE));
        }
        return resetProvisioningEntities;
    }

    private boolean isProvisioningConfigAvailableToUpdate(ProvisioningConnectorConfig provisioningConnector,
                                                          Connection dbConnection, int idpId, int tenantId)
            throws IdentityProviderManagementException {

        ResultSet rs = null;
        boolean isAvailable = false;
        String sqlStmt = IdPManagementConstants.SQLQueries.GET_IDP_PROVISIONING_CONFIGS_FOR_CONNECTOR_TYPE_SQL;
        try (PreparedStatement prepStmt = dbConnection.prepareStatement(sqlStmt)) {
            prepStmt.setInt(1, idpId);
            prepStmt.setString(2, provisioningConnector.getName());
            prepStmt.setInt(3, tenantId);
            rs = prepStmt.executeQuery();
            if (rs.next()) {
                isAvailable = rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new IdentityProviderManagementException("Error occurred while searching for provisioning connector " +
                    "config of Identity Provider : " + idpId, e);
        }
        return isAvailable;
    }

    private void updateProvisioningConfig(ProvisioningConnectorConfig provisioningConnector,
                                          Connection dbConnection, int idpId, int tenantId)
            throws IdentityProviderManagementException {

        String sqlStmt = IdPManagementConstants.SQLQueries.UPDATE_IDP_PROVISIONING_CONFIG_SQL;
        try (PreparedStatement prepStmt = dbConnection.prepareStatement(sqlStmt)) {
            if (provisioningConnector.isEnabled()) {
                prepStmt.setString(1, IdPManagementConstants.IS_TRUE_VALUE);
            } else {
                prepStmt.setString(1, IdPManagementConstants.IS_FALSE_VALUE);
            }
            if (provisioningConnector.isBlocking()) {
                prepStmt.setString(2, IdPManagementConstants.IS_TRUE_VALUE);
            } else {
                prepStmt.setString(2, IdPManagementConstants.IS_FALSE_VALUE);
            }
            prepStmt.setInt(3, idpId);
            prepStmt.setString(4, provisioningConnector.getName());
            prepStmt.setInt(5, tenantId);
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            throw new IdentityProviderManagementException("Error occurred while updating the provisioning " +
                    "connector config of Identity Provider : " + idpId, e);
        }
    }

    /**
     * @param dbConnection
     * @param idPName
     * @param tenantId
     * @return
     * @throws IdentityProviderManagementException
     * @throws SQLException
     */
    public ProvisioningConnectorConfig[] getProvisioningConnectorConfigs(Connection dbConnection,
                                                                         String idPName, int idPId, int tenantId)
            throws IdentityProviderManagementException, SQLException {

        PreparedStatement prepStmt = null;
        PreparedStatement prepBaseStmt = null;

        ResultSet rs1 = null;
        ResultSet rs2 = null;

        try {
            // SP_IDP_PROV_CONNECTOR_TYPE,SP_IDP_PROV_CONFIG_KEY,
            // SP_IDP_PROV_CONFIG_VALUE,SP_IDP_PROV_CONFIG_IS_SECRET
            String sqlBaseStmt = IdPManagementConstants.SQLQueries.GET_IDP_PROVISIONING_CONFIGS_SQL;
            prepBaseStmt = dbConnection.prepareStatement(sqlBaseStmt);

            prepBaseStmt.setInt(1, idPId);
            rs1 = prepBaseStmt.executeQuery();

            Map<String, ProvisioningConnectorConfig> provisioningConnectorMap = new HashMap<String,
                    ProvisioningConnectorConfig>();

            while (rs1.next()) {

                ProvisioningConnectorConfig provisioningConnector;

                String type = rs1.getString("PROVISIONING_CONNECTOR_TYPE");
                if (!provisioningConnectorMap.containsKey(type)) {
                    provisioningConnector = new ProvisioningConnectorConfig();
                    provisioningConnector.setName(type);

                    if ((IdPManagementConstants.IS_TRUE_VALUE).equals(rs1.getString("IS_ENABLED"))) {
                        provisioningConnector.setEnabled(true);
                    } else {
                        provisioningConnector.setEnabled(false);
                    }

                    if ((IdPManagementConstants.IS_TRUE_VALUE).equals(rs1.getString("IS_BLOCKING"))) {
                        provisioningConnector.setBlocking(true);
                    } else {
                        provisioningConnector.setBlocking(false);
                    }

                    if (provisioningConnector.getProvisioningProperties() == null
                            || provisioningConnector.getProvisioningProperties().length == 0) {

                        String sqlStmt = IdPManagementConstants.SQLQueries.GET_IDP_PROVISIONING_PROPERTY_SQL;
                        prepStmt = dbConnection.prepareStatement(sqlStmt);

                        int configId = rs1.getInt("ID");
                        prepStmt.setInt(1, tenantId);
                        prepStmt.setInt(2, configId);

                        rs2 = prepStmt.executeQuery();

                        List<Property> provisioningProperties = new ArrayList<Property>();
                        while (rs2.next()) {
                            Property property = new Property();
                            String name = rs2.getString("PROPERTY_KEY");
                            String value = rs2.getString("PROPERTY_VALUE");
                            String blobValue = getBlobValue(rs2.getBinaryStream("PROPERTY_BLOB_VALUE"));

                            String propertyType = rs2.getString("PROPERTY_TYPE");
                            if (propertyType != null) {
                                propertyType = propertyType.trim();
                            }
                            String isSecret = rs2.getString("IS_SECRET");

                            property.setName(name);
                            if (IdentityApplicationConstants.ConfigElements.PROPERTY_TYPE_BLOB.equals(propertyType)) {
                                property.setValue(blobValue);
                            } else {
                                property.setValue(value);
                            }

                            property.setType(propertyType);

                            if ((IdPManagementConstants.IS_TRUE_VALUE).equals(isSecret)) {
                                property.setConfidential(true);
                            } else {
                                property.setConfidential(false);
                            }

                            provisioningProperties.add(property);
                        }
                        provisioningConnector.setProvisioningProperties(provisioningProperties
                                .toArray(new Property[provisioningProperties.size()]));
                    }

                    provisioningConnectorMap.put(type, provisioningConnector);
                }
            }

            return provisioningConnectorMap.values().toArray(
                    new ProvisioningConnectorConfig[provisioningConnectorMap.size()]);

        } finally {

            IdentityDatabaseUtil.closeAllConnections(null, rs2, prepBaseStmt);
            IdentityDatabaseUtil.closeAllConnections(null, rs1, prepStmt);
        }
    }

    private String getBlobValue(InputStream is) throws IdentityProviderManagementException {

        if (is != null) {
            BufferedReader br = null;
            StringBuilder sb = new StringBuilder();
            String line;
            try {
                br = new BufferedReader(new InputStreamReader(is));
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            } catch (IOException e) {
                throw new IdentityProviderManagementException("Error occurred while reading blob value from input " +
                        "stream", e);
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        log.error("Error in retrieving the Blob value", e);
                    }
                }
            }

            return sb.toString();
        }
        return null;
    }

    /**
     * Retrieves an IDP from name.
     *
     * @param dbConnection Database connection.
     * @param idPName      IDP name.
     * @param tenantId     Tenant ID of the IDP.
     * @param tenantDomain Tenant Domain of the IDP.
     * @return An Identity Provider with given name.
     * @throws IdentityProviderManagementException IdentityProviderManagementException
     */
    public IdentityProvider getIdPByName(Connection dbConnection, String idPName, int tenantId,
                                         String tenantDomain) throws IdentityProviderManagementException {

        IdentityProvider idp = getIDP(dbConnection, idPName, -1, null, tenantId, tenantDomain);
        if (IdentityApplicationConstants.RESIDENT_IDP_RESERVED_NAME.equals(idPName)) {
            fillResidentIdpProperties(idp, tenantDomain);
        }
        return idp;
    }

    private String resolveAbsoluteURL(String defaultUrlContext, String urlFromConfig, String urlFromConfigV2,
                                      String tenantDomain)
            throws IdentityProviderManagementServerException {

        if (!IdentityTenantUtil.isTenantQualifiedUrlsEnabled() && StringUtils.isNotBlank(urlFromConfig)) {
            if (log.isDebugEnabled()) {
                log.debug("Resolved URL:" + urlFromConfig + " from file configuration for default url context: " +
                        defaultUrlContext);
            }
            return urlFromConfig;
        }

        if (StringUtils.isNotBlank(urlFromConfigV2)) {
            return urlFromConfigV2;
        }
        try {
            ServiceURLBuilder serviceURLBuilder = ServiceURLBuilder.create().setTenant(tenantDomain);
            return serviceURLBuilder.addPath(defaultUrlContext).build().getAbsolutePublicURL();
        } catch (URLBuilderException e) {
            throw IdentityProviderManagementException.error(IdentityProviderManagementServerException.class,
                    "Error while building URL: " + defaultUrlContext, e);
        }
    }

    private String addTenantPathParamInLegacyMode(String resolvedUrl, String tenantDomain) {

        try {
            if (!IdentityTenantUtil.isTenantQualifiedUrlsEnabled() && StringUtils.isNotBlank(tenantDomain) &&
                    !org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                resolvedUrl = getTenantUrl(resolvedUrl, tenantDomain);
            }
        } catch (URISyntaxException e) {
            log.error(String.format("%s endpoint is malformed.", resolvedUrl), e);
        }
        return resolvedUrl;
    }

    private String getTenantUrl(String url, String tenantDomain) throws URISyntaxException {

        URI uri = new URI(url);
        URI uriModified = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), ("/t/" +
                tenantDomain + uri.getPath()), uri.getQuery(), uri.getFragment());
        return uriModified.toString();
    }

    private String getTenantContextFromTenantDomain(String tenantDomain) {

        if (!org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(tenantDomain)) {
            return org.wso2.carbon.utils.multitenancy.MultitenantConstants.TENANT_AWARE_URL_PREFIX + "/" + tenantDomain + "/";
        }
        return "";
    }

    private Property resolveFedAuthnProperty(String epUrl, FederatedAuthenticatorConfig fedAuthnConfig,
                                             String propertyName) {

        Property property =
                IdentityApplicationManagementUtil.getProperty(fedAuthnConfig.getProperties(), propertyName);

        if (property == null) {
            property = new Property();
            property.setName(propertyName);
            property.setValue(epUrl);
        }
        if (IdentityTenantUtil.isTenantQualifiedUrlsEnabled()) {
            // In tenant qualified mode we have to always give send the calculated URL and not the value stored in DB.
            String existingValue = property.getValue();
            property = new Property();
            property.setName(propertyName);
            if (isUseEntityIDAsIssuerEnabled()) {
                // If the flag to use entity ID as issuer is enabled, we give priority to the existing value in DB.
                property.setValue(existingValue);
            } else {
                property.setValue(epUrl);
            }
        }

        return property;
    }

    private FederatedAuthenticatorConfig buildSAMLProperties(IdentityProvider identityProvider, String tenantDomain)
            throws IdentityProviderManagementException {

        String samlSSOUrl = buildSAMLUrl(IdentityUtil.getProperty(IdentityConstants.ServerConfig.SSO_IDP_URL),
                tenantDomain, IdPManagementConstants.SAMLSSO, true);

        String samlLogoutUrl = buildSAMLUrl(IdentityUtil.getProperty(IdentityConstants.ServerConfig.SSO_IDP_URL),
                tenantDomain, IdPManagementConstants.SAMLSSO, true);

        String samlECPUrl = buildSAMLUrl(IdentityUtil.getProperty(IdentityConstants.ServerConfig.SAML_ECP_URL),
                tenantDomain, IdPManagementConstants.SAML_ECP_URL, true);

        String samlArtifactUrl = buildSAMLUrl(IdentityUtil.getProperty(IdentityConstants.ServerConfig.SSO_ARTIFACT_URL),
                tenantDomain, IdPManagementConstants.SSO_ARTIFACT_URL, false);

        FederatedAuthenticatorConfig samlFederatedAuthConfig = IdentityApplicationManagementUtil
                .getFederatedAuthenticator(identityProvider.getFederatedAuthenticatorConfigs(),
                        IdentityApplicationConstants.Authenticator.SAML2SSO.NAME);
        if (samlFederatedAuthConfig == null) {
            samlFederatedAuthConfig = new FederatedAuthenticatorConfig();
            samlFederatedAuthConfig.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.NAME);
            samlFederatedAuthConfig.setDefinedByType(DefinedByType.SYSTEM);
        }

        List<Property> propertiesList = new ArrayList<>();

        Property samlSSOUrlProperty = resolveFedAuthnProperty(samlSSOUrl, samlFederatedAuthConfig,
                IdentityApplicationConstants.Authenticator.SAML2SSO.SSO_URL);
        propertiesList.add(samlSSOUrlProperty);

        Property samlLogoutUrlProperty = resolveFedAuthnProperty(samlLogoutUrl, samlFederatedAuthConfig,
                IdentityApplicationConstants.Authenticator.SAML2SSO.LOGOUT_REQ_URL);
        propertiesList.add(samlLogoutUrlProperty);

        Property samlECPUrlProperty = resolveFedAuthnProperty(samlECPUrl, samlFederatedAuthConfig,
                IdentityApplicationConstants.Authenticator.SAML2SSO.ECP_URL);
        propertiesList.add(samlECPUrlProperty);

        Property samlArtifactUrlProperty = resolveFedAuthnProperty(samlArtifactUrl, samlFederatedAuthConfig,
                IdentityApplicationConstants.Authenticator.SAML2SSO.ARTIFACT_RESOLVE_URL);
        propertiesList.add(samlArtifactUrlProperty);

        Property idPEntityIdProperty =
                IdentityApplicationManagementUtil.getProperty(samlFederatedAuthConfig.getProperties(),
                        IdentityApplicationConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID);
        if (idPEntityIdProperty == null) {
            idPEntityIdProperty = new Property();
            idPEntityIdProperty.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID);
            idPEntityIdProperty.setValue(IdPManagementUtil.getResidentIdPEntityId());
        }
        propertiesList.add(idPEntityIdProperty);

        // Add SSO URL as a destination URL if not already available.
        addSSOUrlAsDestinationUrl(samlFederatedAuthConfig, samlSSOUrl, propertiesList);

        for (Property property : samlFederatedAuthConfig.getProperties()) {
            if (property != null &&
                    !IdentityApplicationConstants.Authenticator.SAML2SSO.SSO_URL.equals(property.getName()) &&
                    !IdentityApplicationConstants.Authenticator.SAML2SSO.LOGOUT_REQ_URL.equals(property.getName()) &&
                    !IdentityApplicationConstants.Authenticator.SAML2SSO.ECP_URL.equals(property.getName()) &&
                    !IdentityApplicationConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID.equals(property.getName())) {
                propertiesList.add(property);
            }
        }

        Property samlMetadataValidityPeriodProperty =
                IdentityApplicationManagementUtil.getProperty(samlFederatedAuthConfig.
                        getProperties(), IdentityApplicationConstants.Authenticator.SAML2SSO.
                        SAML_METADATA_VALIDITY_PERIOD);
        if (samlMetadataValidityPeriodProperty == null) {
            samlMetadataValidityPeriodProperty = new Property();
            samlMetadataValidityPeriodProperty.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.
                    SAML_METADATA_VALIDITY_PERIOD);
            samlMetadataValidityPeriodProperty.setValue(IdentityApplicationConstants.Authenticator.SAML2SSO.
                    SAML_METADATA_VALIDITY_PERIOD_DEFAULT);
        }
        propertiesList.add(samlMetadataValidityPeriodProperty);
        Property samlMetadataSigningEnabledProperty =
                IdentityApplicationManagementUtil.getProperty(samlFederatedAuthConfig.
                        getProperties(), IdentityApplicationConstants.Authenticator.SAML2SSO.
                        SAML_METADATA_SIGNING_ENABLED);
        if (samlMetadataSigningEnabledProperty == null) {
            samlMetadataSigningEnabledProperty = new Property();
            samlMetadataSigningEnabledProperty.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.
                    SAML_METADATA_SIGNING_ENABLED);
            samlMetadataSigningEnabledProperty.setValue(IdentityApplicationConstants.Authenticator.SAML2SSO.
                    SAML_METADATA_SIGNING_ENABLED_DEFAULT);
        }
        propertiesList.add(samlMetadataSigningEnabledProperty);
        Property samlAuthnRequestSigningProperty =
                IdentityApplicationManagementUtil.getProperty(samlFederatedAuthConfig.
                        getProperties(), IdentityApplicationConstants.Authenticator.SAML2SSO.
                        SAML_METADATA_AUTHN_REQUESTS_SIGNING_ENABLED);
        if (samlAuthnRequestSigningProperty == null) {
            samlAuthnRequestSigningProperty = new Property();
            samlAuthnRequestSigningProperty.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.
                    SAML_METADATA_AUTHN_REQUESTS_SIGNING_ENABLED);
            samlAuthnRequestSigningProperty.setValue(IdentityApplicationConstants.Authenticator.SAML2SSO.
                    SAML_METADATA_AUTHN_REQUESTS_SIGNING_DEFAULT);
        }
        propertiesList.add(samlAuthnRequestSigningProperty);
        samlFederatedAuthConfig.setProperties(propertiesList.toArray(new Property[propertiesList.size()]));
        return samlFederatedAuthConfig;
    }

    private String buildSAMLUrl(String urlFromConfigFile, String tenantDomain, String defaultContext,
                                boolean appendTenantDomainInLegacyMode) throws IdentityProviderManagementException {

        String url = urlFromConfigFile;
        if (StringUtils.isBlank(url)) {
            // Now we need to build the URL based on the default context.
            try {
                url = ServiceURLBuilder.create().addPath(defaultContext).build().getAbsolutePublicURL();
            } catch (URLBuilderException ex) {
                throw new IdentityProviderManagementException("Error while building URL for context: "
                        + defaultContext + " for tenantDomain: " + tenantDomain, ex);
            }
        }
        //Should not append the tenant domain as a query parameter if it is super tenant.
        if (appendTenantDomainInLegacyMode && isNotSuperTenant(tenantDomain)) {
            Map<String, String[]> queryParams = new HashMap<>();
            queryParams.put(
                    org.wso2.carbon.utils.multitenancy.MultitenantConstants.TENANT_DOMAIN, new String[] {tenantDomain});

            try {
                url = IdentityUtil.buildQueryUrl(url, queryParams);
            } catch (UnsupportedEncodingException e) {
                throw new IdentityProviderManagementException("Error while building URL for context: "
                        + defaultContext + " for tenantDomain: " + tenantDomain, e);
            }
        }

        return resolveAbsoluteURL(defaultContext, url, null, tenantDomain);
    }

    private void addSSOUrlAsDestinationUrl(FederatedAuthenticatorConfig federatedAuthenticatorConfig,
                                           String ssoUrl,
                                           List<Property> propertiesList) {

        // First find the available configured destination URLs.
        List<Property> destinationURLs = Arrays.stream(federatedAuthenticatorConfig.getProperties())
                .filter(property -> property.getName()
                        .startsWith(IdentityApplicationConstants.Authenticator.SAML2SSO.DESTINATION_URL_PREFIX))
                .collect(Collectors.toList());

        // Check whether the SSO URL is already available as a destination URL
        boolean isSAMLSSOUrlNotPresentAsDestination = destinationURLs.stream()
                .noneMatch(x -> StringUtils.equals(ssoUrl, x.getValue()));

        if (isSAMLSSOUrlNotPresentAsDestination) {
            // There are no destination properties matching the default SSO URL.
            int propertyNameIndex = destinationURLs.size() + 1;
            Property destinationURLProperty = buildDestinationURLProperty(ssoUrl, propertyNameIndex);
            propertiesList.add(destinationURLProperty);
        }
    }

    private Property buildDestinationURLProperty(String destinationURL, int index) {

        Property destinationURLProperty = new Property();
        destinationURLProperty.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.DESTINATION_URL_PREFIX +
                IdentityApplicationConstants.MULTIVALUED_PROPERTY_CHARACTER + index);
        destinationURLProperty.setValue(destinationURL);
        return destinationURLProperty;
    }

    private boolean isNotSuperTenant(String tenantDomain) {

        return !StringUtils.equals(tenantDomain,
                org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
    }

    private String getOIDCResidentIdPEntityId() {

        String OIDCEntityId = IdentityUtil.getProperty("OAuth.OpenIDConnect.IDTokenIssuerID");
        if (StringUtils.isBlank(OIDCEntityId)) {
            OIDCEntityId = "localhost";
        }
        return OIDCEntityId;
    }

    private void fillResidentIdpProperties(IdentityProvider identityProvider, String tenantDomain)
            throws IdentityProviderManagementException {

        if (identityProvider == null) {
            return;
        }

        String openIdUrl;
        String oauth1RequestTokenUrl;
        String oauth1AuthorizeUrl;
        String oauth1AccessTokenUrl;
        String oauth2AuthzEPUrl;
        String oauth2ParEPUrl;
        String oauth2TokenEPUrl;
        String oauth2RevokeEPUrl;
        String oauth2IntrospectEpUrl;
        String oauth2UserInfoEPUrl;
        String oidcCheckSessionEPUrl;
        String oidcLogoutEPUrl;
        String oIDCWebFingerEPUrl;
        String oAuth2DCREPUrl;
        String oAuth2JWKSPage;
        String oIDCDiscoveryEPUrl;
        String passiveStsUrl;
        String stsUrl;
        String scimUsersEndpoint;
        String scimGroupsEndpoint;
        String scim2UsersEndpoint;
        String scim2GroupsEndpoint;
        String oauth1RequestTokenUrlV2;
        String oauth1AuthorizeUrlV2;
        String oauth1AccessTokenUrlV2;
        String oauth2AuthzEPUrlV2;
        String oauth2ParEPUrlV2;
        String oauth2TokenEPUrlV2;
        String oauth2RevokeEPUrlV2;
        String oauth2IntrospectEpUrlV2;
        String oauth2UserInfoEPUrlV2;
        String oidcCheckSessionEPUrlV2;
        String oidcLogoutEPUrlV2;
        String oIDCWebFingerEPUrlV2;
        String oAuth2DCREPUrlV2;
        String oAuth2JWKSPageV2;
        String oIDCDiscoveryEPUrlV2;

        openIdUrl = IdentityUtil.getProperty(IdentityConstants.ServerConfig.OPENID_SERVER_URL);
        oauth1RequestTokenUrl = IdentityUtil.getProperty(IdentityConstants.OAuth.OAUTH1_REQUEST_TOKEN_URL);
        oauth1AuthorizeUrl = IdentityUtil.getProperty(IdentityConstants.OAuth.OAUTH1_AUTHORIZE_URL);
        oauth1AccessTokenUrl = IdentityUtil.getProperty(IdentityConstants.OAuth.OAUTH1_ACCESSTOKEN_URL);
        oauth2AuthzEPUrl = IdentityUtil.getProperty(IdentityConstants.OAuth.OAUTH2_AUTHZ_EP_URL);
        oauth2ParEPUrl = IdentityUtil.getProperty(IdentityConstants.OAuth.OAUTH2_PAR_EP_URL);
        oauth2TokenEPUrl = IdentityUtil.getProperty(IdentityConstants.OAuth.OAUTH2_TOKEN_EP_URL);
        oauth2UserInfoEPUrl = IdentityUtil.getProperty(IdentityConstants.OAuth.OAUTH2_USERINFO_EP_URL);
        oidcCheckSessionEPUrl = IdentityUtil.getProperty(IdentityConstants.OAuth.OIDC_CHECK_SESSION_EP_URL);
        oidcLogoutEPUrl = IdentityUtil.getProperty(IdentityConstants.OAuth.OIDC_LOGOUT_EP_URL);
        passiveStsUrl = IdentityUtil.getProperty(IdentityConstants.STS.PSTS_IDENTITY_PROVIDER_URL);
        stsUrl = IdentityUtil.getProperty(IdentityConstants.STS.STS_IDENTITY_PROVIDER_URL);
        scimUsersEndpoint = IdentityUtil.getProperty(IdentityConstants.SCIM.USER_EP_URL);
        scimGroupsEndpoint = IdentityUtil.getProperty(IdentityConstants.SCIM.GROUP_EP_URL);
        scim2UsersEndpoint = IdentityUtil.getProperty(IdentityConstants.SCIM2.USER_EP_URL);
        scim2GroupsEndpoint = IdentityUtil.getProperty(IdentityConstants.SCIM2.GROUP_EP_URL);
        oauth2RevokeEPUrl = IdentityUtil.getProperty(IdentityConstants.OAuth.OAUTH2_REVOKE_EP_URL);
        oauth2IntrospectEpUrl = IdentityUtil.getProperty(IdentityConstants.OAuth.OAUTH2_INTROSPECT_EP_URL);
        oIDCWebFingerEPUrl = IdentityUtil.getProperty(IdentityConstants.OAuth.OIDC_WEB_FINGER_EP_URL);
        oAuth2DCREPUrl = IdentityUtil.getProperty(IdentityConstants.OAuth.OAUTH2_DCR_EP_URL);
        oAuth2JWKSPage = IdentityUtil.getProperty(IdentityConstants.OAuth.OAUTH2_JWKS_EP_URL);
        oIDCDiscoveryEPUrl = IdentityUtil.getProperty(IdentityConstants.OAuth.OIDC_DISCOVERY_EP_URL);
        oauth1RequestTokenUrlV2 = IdentityUtil.getProperty(IdentityConstants.OAuth.OAUTH1_REQUEST_TOKEN_URL_V2);
        oauth1AuthorizeUrlV2 = IdentityUtil.getProperty(IdentityConstants.OAuth.OAUTH1_AUTHORIZE_URL_V2);
        oauth1AccessTokenUrlV2 = IdentityUtil.getProperty(IdentityConstants.OAuth.OAUTH1_ACCESSTOKEN_URL_V2);
        oauth2AuthzEPUrlV2 = IdentityUtil.getProperty(IdentityConstants.OAuth.OAUTH2_AUTHZ_EP_URL_V2);
        oauth2ParEPUrlV2 = IdentityUtil.getProperty(IdentityConstants.OAuth.OAUTH2_PAR_EP_URL_V2);
        oauth2TokenEPUrlV2 = IdentityUtil.getProperty(IdentityConstants.OAuth.OAUTH2_TOKEN_EP_URL_V2);
        oauth2UserInfoEPUrlV2 = IdentityUtil.getProperty(IdentityConstants.OAuth.OAUTH2_USERINFO_EP_URL_V2);
        oidcCheckSessionEPUrlV2 = IdentityUtil.getProperty(IdentityConstants.OAuth.OIDC_CHECK_SESSION_EP_URL_V2);
        oidcLogoutEPUrlV2 = IdentityUtil.getProperty(IdentityConstants.OAuth.OIDC_LOGOUT_EP_URL_V2);
        oauth2RevokeEPUrlV2 = IdentityUtil.getProperty(IdentityConstants.OAuth.OAUTH2_REVOKE_EP_URL_V2);
        oauth2IntrospectEpUrlV2 = IdentityUtil.getProperty(IdentityConstants.OAuth.OAUTH2_INTROSPECT_EP_URL_V2);
        oIDCWebFingerEPUrlV2 = IdentityUtil.getProperty(IdentityConstants.OAuth.OIDC_WEB_FINGER_EP_URL_V2);
        oAuth2DCREPUrlV2 = IdentityUtil.getProperty(IdentityConstants.OAuth.OAUTH2_DCR_EP_URL_V2);
        oAuth2JWKSPageV2 = IdentityUtil.getProperty(IdentityConstants.OAuth.OAUTH2_JWKS_EP_URL_V2);
        oIDCDiscoveryEPUrlV2 = IdentityUtil.getProperty(IdentityConstants.OAuth.OIDC_DISCOVERY_EP_URL_V2);

        if (StringUtils.isBlank(openIdUrl)) {
            openIdUrl = IdentityUtil.getServerURL(IdentityConstants.OpenId.OPENID, true, true);
        }

        if (StringUtils.isBlank(oauth1RequestTokenUrl)) {
            oauth1RequestTokenUrl = IdentityUtil.getServerURL(IdentityConstants.OAuth.REQUEST_TOKEN, true, true);
        }

        if (StringUtils.isBlank(oauth1AuthorizeUrl)) {
            oauth1AuthorizeUrl = IdentityUtil.getServerURL(IdentityConstants.OAuth.AUTHORIZE_URL, true, true);
        }

        if (StringUtils.isBlank(oauth1AccessTokenUrl)) {
            oauth1AccessTokenUrl = IdentityUtil.getServerURL(IdentityConstants.OAuth.ACCESS_TOKEN, true, true);
        }

        try {
            // Initiating a tenant flow to resolve tenant qualified URLs. Here the tenant domain appending to the
            // carbon context is the tenant domain where the configurations needs to be retrieved.
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);

            oauth2AuthzEPUrl = resolveAbsoluteURL(IdentityConstants.OAuth.AUTHORIZE, oauth2AuthzEPUrl, oauth2AuthzEPUrlV2,
                    tenantDomain);
            oauth2ParEPUrl =
                    resolveAbsoluteURL(IdentityConstants.OAuth.PAR, oauth2ParEPUrl, oauth2ParEPUrlV2, tenantDomain);
            oauth2TokenEPUrl =
                    resolveAbsoluteURL(IdentityConstants.OAuth.TOKEN, oauth2TokenEPUrl, oauth2TokenEPUrlV2, tenantDomain);
            oauth2RevokeEPUrl = resolveAbsoluteURL(IdentityConstants.OAuth.REVOKE, oauth2RevokeEPUrl, oauth2RevokeEPUrlV2,
                    tenantDomain);
            oauth2IntrospectEpUrl =
                    resolveAbsoluteURL(IdentityConstants.OAuth.INTROSPECT, oauth2IntrospectEpUrl, oauth2IntrospectEpUrlV2,
                            tenantDomain);
            oauth2IntrospectEpUrl = addTenantPathParamInLegacyMode(oauth2IntrospectEpUrl, tenantDomain);
            oauth2UserInfoEPUrl =
                    resolveAbsoluteURL(IdentityConstants.OAuth.USERINFO, oauth2UserInfoEPUrl, oauth2UserInfoEPUrlV2,
                            tenantDomain);
            oidcCheckSessionEPUrl = resolveAbsoluteURL(IdentityConstants.OAuth.CHECK_SESSION, oidcCheckSessionEPUrl,
                    oidcCheckSessionEPUrlV2,
                    tenantDomain);
            oidcLogoutEPUrl =
                    resolveAbsoluteURL(IdentityConstants.OAuth.LOGOUT, oidcLogoutEPUrl, oidcLogoutEPUrlV2, tenantDomain);
            oAuth2DCREPUrl =
                    resolveAbsoluteURL(IdentityConstants.OAuth.DCR, oAuth2DCREPUrl, oAuth2DCREPUrlV2, tenantDomain);
            oAuth2DCREPUrl = addTenantPathParamInLegacyMode(oAuth2DCREPUrl, tenantDomain);
            oAuth2JWKSPage =
                    resolveAbsoluteURL(IdentityConstants.OAuth.JWKS, oAuth2JWKSPage, oAuth2JWKSPageV2, tenantDomain);
            oAuth2JWKSPage = addTenantPathParamInLegacyMode(oAuth2JWKSPage, tenantDomain);
            oIDCDiscoveryEPUrl =
                    resolveAbsoluteURL(IdentityConstants.OAuth.DISCOVERY, oIDCDiscoveryEPUrl, oIDCDiscoveryEPUrlV2,
                            tenantDomain);
            oIDCDiscoveryEPUrl = addTenantPathParamInLegacyMode(oIDCDiscoveryEPUrl, tenantDomain);
            passiveStsUrl = resolveAbsoluteURL(IdentityConstants.STS.PASSIVE_STS, passiveStsUrl, null, tenantDomain);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

        // If sts url is configured in file, change it according to tenant domain. If not configured, add a default url
        if (StringUtils.isNotBlank(stsUrl)) {
            stsUrl = stsUrl.replace(IdentityConstants.STS.WSO2_CARBON_STS,
                    getTenantContextFromTenantDomain(tenantDomain) +
                            IdentityConstants.STS.WSO2_CARBON_STS);
        } else {
            stsUrl = IdentityUtil.getServerURL("services/" + getTenantContextFromTenantDomain(tenantDomain) +
                    IdentityConstants.STS.WSO2_CARBON_STS, true, true);
        }

        if (StringUtils.isBlank(scimUsersEndpoint)) {
            scimUsersEndpoint = IdentityUtil.getServerURL(IdentityConstants.SCIM.USER_EP, true, false);
        }

        if (StringUtils.isBlank(scimGroupsEndpoint)) {
            scimGroupsEndpoint = IdentityUtil.getServerURL(IdentityConstants.SCIM.GROUP_EP, true, false);
        }

        if (StringUtils.isBlank(scim2UsersEndpoint)) {
            scim2UsersEndpoint = IdentityUtil.getServerURL(IdentityConstants.SCIM2.USER_EP, true, false);
        }
        try {
            if (StringUtils.isNotBlank(tenantDomain) &&
                    !org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals
                            (tenantDomain)) {
                scim2UsersEndpoint = getTenantUrl(scim2UsersEndpoint, tenantDomain);
            }
        } catch (URISyntaxException e) {
            log.error("SCIM 2.0 Users endpoint is malformed");
        }

        if (StringUtils.isBlank(scim2GroupsEndpoint)) {
            scim2GroupsEndpoint = IdentityUtil.getServerURL(IdentityConstants.SCIM2.GROUP_EP, true, false);
        }
        try {
            if (StringUtils.isNotBlank(tenantDomain) &&
                    !org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals
                            (tenantDomain)) {
                scim2GroupsEndpoint = getTenantUrl(scim2GroupsEndpoint, tenantDomain);
            }
        } catch (URISyntaxException e) {
            log.error("SCIM 2.0 Groups endpoint is malformed");
        }

        int tenantId;
        try {
            tenantId = IdPManagementServiceComponent.getRealmService().getTenantManager().getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            throw new IdentityProviderManagementException(
                    "Exception occurred while retrieving Tenant ID from Tenant Domain " + tenantDomain, e);
        }
        X509Certificate cert;
        try {
            IdentityTenantUtil.initializeRegistry(tenantId);
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantDomain(tenantDomain, true);
            KeyStoreManager keyStoreManager = KeyStoreManager.getInstance(tenantId);
            if (!org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                // derive key store name
                String fileName = KeystoreUtils.getKeyStoreFileLocation(tenantDomain);
                KeyStore keyStore = keyStoreManager.getKeyStore(fileName);
                cert = (X509Certificate) keyStore.getCertificate(tenantDomain);
            } else {
                cert = keyStoreManager.getDefaultPrimaryCertificate();
            }
        } catch (Exception e) {
            String msg = "Error retrieving primary certificate for tenant : " + tenantDomain;
            throw new IdentityProviderManagementException(msg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

        if (cert == null) {
            throw new IdentityProviderManagementException(
                    "Cannot find the primary certificate for tenant " + tenantDomain);
        }
        try {
            identityProvider.setCertificate(org.apache.axiom.om.util.Base64.encode(cert.getEncoded()));
        } catch (CertificateEncodingException e) {
            String msg = "Error occurred while encoding primary certificate for tenant domain " + tenantDomain;
            throw new IdentityProviderManagementException(msg, e);
        }

        List<FederatedAuthenticatorConfig> fedAuthnConfigs = new ArrayList<>();
        List<Property> propertiesList;

        FederatedAuthenticatorConfig openIdFedAuthn = IdentityApplicationManagementUtil
                .getFederatedAuthenticator(identityProvider.getFederatedAuthenticatorConfigs(),
                        IdentityApplicationConstants.Authenticator.OpenID.NAME);
        if (openIdFedAuthn == null) {
            openIdFedAuthn = new FederatedAuthenticatorConfig();
            openIdFedAuthn.setName(IdentityApplicationConstants.Authenticator.OpenID.NAME);
            openIdFedAuthn.setDefinedByType(DefinedByType.SYSTEM);
        }
        propertiesList = new ArrayList<>(Arrays.asList(openIdFedAuthn.getProperties()));
        if (IdentityApplicationManagementUtil.getProperty(openIdFedAuthn.getProperties(),
                IdentityApplicationConstants.Authenticator.OpenID.OPEN_ID_URL) == null) {
            Property openIdUrlProp = new Property();
            openIdUrlProp.setName(IdentityApplicationConstants.Authenticator.OpenID.OPEN_ID_URL);
            openIdUrlProp.setValue(openIdUrl);
            propertiesList.add(openIdUrlProp);
        }
        openIdFedAuthn.setProperties(propertiesList.toArray(new Property[0]));
        fedAuthnConfigs.add(openIdFedAuthn);

        // SAML2 related endpoints.
        FederatedAuthenticatorConfig saml2SSOFedAuthn = buildSAMLProperties(identityProvider, tenantDomain);
        fedAuthnConfigs.add(saml2SSOFedAuthn);

        FederatedAuthenticatorConfig oauth1FedAuthn = IdentityApplicationManagementUtil
                .getFederatedAuthenticator(identityProvider.getFederatedAuthenticatorConfigs(),
                        IdentityApplicationConstants.OAuth10A.NAME);
        if (oauth1FedAuthn == null) {
            oauth1FedAuthn = new FederatedAuthenticatorConfig();
            oauth1FedAuthn.setName(IdentityApplicationConstants.OAuth10A.NAME);
            oauth1FedAuthn.setDefinedByType(DefinedByType.SYSTEM);
        }
        propertiesList = new ArrayList<>(Arrays.asList(oauth1FedAuthn.getProperties()));
        if (IdentityApplicationManagementUtil.getProperty(oauth1FedAuthn.getProperties(),
                IdentityApplicationConstants.OAuth10A.OAUTH1_REQUEST_TOKEN_URL) == null) {
            Property oauth1ReqTokUrlProp = new Property();
            oauth1ReqTokUrlProp.setName(IdentityApplicationConstants.OAuth10A.OAUTH1_REQUEST_TOKEN_URL);
            oauth1ReqTokUrlProp.setValue(
                    StringUtils.isNotBlank(oauth1RequestTokenUrlV2) ? oauth1RequestTokenUrlV2 : oauth1RequestTokenUrl);
            propertiesList.add(oauth1ReqTokUrlProp);
        }
        if (IdentityApplicationManagementUtil.getProperty(oauth1FedAuthn.getProperties(),
                IdentityApplicationConstants.OAuth10A.OAUTH1_AUTHORIZE_URL) == null) {
            Property oauth1AuthzUrlProp = new Property();
            oauth1AuthzUrlProp.setName(IdentityApplicationConstants.OAuth10A.OAUTH1_AUTHORIZE_URL);
            oauth1AuthzUrlProp.setValue(
                    StringUtils.isNotBlank(oauth1AuthorizeUrlV2) ? oauth1AuthorizeUrlV2 : oauth1AuthorizeUrl);
            propertiesList.add(oauth1AuthzUrlProp);
        }
        if (IdentityApplicationManagementUtil.getProperty(oauth1FedAuthn.getProperties(),
                IdentityApplicationConstants.OAuth10A.OAUTH1_ACCESS_TOKEN_URL) == null) {
            Property oauth1AccessTokUrlProp = new Property();
            oauth1AccessTokUrlProp.setName(IdentityApplicationConstants.OAuth10A.OAUTH1_ACCESS_TOKEN_URL);
            oauth1AccessTokUrlProp.setValue(
                    StringUtils.isNotBlank(oauth1AccessTokenUrlV2) ? oauth1AccessTokenUrlV2 : oauth1AccessTokenUrl);
            propertiesList.add(oauth1AccessTokUrlProp);
        }
        oauth1FedAuthn.setProperties(propertiesList.toArray(new Property[0]));
        fedAuthnConfigs.add(oauth1FedAuthn);

        FederatedAuthenticatorConfig oidcFedAuthn = IdentityApplicationManagementUtil
                .getFederatedAuthenticator(identityProvider.getFederatedAuthenticatorConfigs(),
                        IdentityApplicationConstants.Authenticator.OIDC.NAME);
        if (oidcFedAuthn == null) {
            oidcFedAuthn = new FederatedAuthenticatorConfig();
            oidcFedAuthn.setName(IdentityApplicationConstants.Authenticator.OIDC.NAME);
            oidcFedAuthn.setDefinedByType(DefinedByType.SYSTEM);
        }
        propertiesList = new ArrayList<>();

        Property idPEntityIdProp;
        // When the tenant qualified urls are enabled, we need to see the oauth2 token endpoint.
        if (IdentityTenantUtil.isTenantQualifiedUrlsEnabled()) {
            if (isUseEntityIDAsIssuerEnabled()) {
                if (log.isDebugEnabled()) {
                    log.debug("Using the configured entity ID as the issuer in the " + tenantDomain+ " tenant.");
                }
                idPEntityIdProp = resolveFedAuthnProperty(getOIDCResidentIdPEntityId(), oidcFedAuthn,
                        OPENID_IDP_ENTITY_ID);
            } else {
                idPEntityIdProp = resolveFedAuthnProperty(oauth2TokenEPUrl, oidcFedAuthn,
                        OPENID_IDP_ENTITY_ID);
            }
        } else {
            idPEntityIdProp = resolveFedAuthnProperty(getOIDCResidentIdPEntityId(), oidcFedAuthn, OPENID_IDP_ENTITY_ID);
        }
        propertiesList.add(idPEntityIdProp);

        Property authzUrlProp = resolveFedAuthnProperty(oauth2AuthzEPUrl, oidcFedAuthn,
                IdentityApplicationConstants.Authenticator.OIDC.OAUTH2_AUTHZ_URL);
        propertiesList.add(authzUrlProp);

        Property parUrlProp = resolveFedAuthnProperty(oauth2ParEPUrl, oidcFedAuthn,
                IdentityApplicationConstants.OAuth2.OAUTH2_PAR_URL);
        propertiesList.add(parUrlProp);

        Property tokenUrlProp = resolveFedAuthnProperty(oauth2TokenEPUrl, oidcFedAuthn,
                IdentityApplicationConstants.Authenticator.OIDC.OAUTH2_TOKEN_URL);
        propertiesList.add(tokenUrlProp);

        Property revokeUrlProp = resolveFedAuthnProperty(oauth2RevokeEPUrl, oidcFedAuthn,
                IdentityApplicationConstants.Authenticator.OIDC.OAUTH2_REVOKE_URL);
        propertiesList.add(revokeUrlProp);

        Property introspectUrlProp = resolveFedAuthnProperty(oauth2IntrospectEpUrl, oidcFedAuthn,
                IdentityApplicationConstants.Authenticator.OIDC.OAUTH2_INTROSPECT_URL);
        propertiesList.add(introspectUrlProp);

        Property userInfoUrlProp = resolveFedAuthnProperty(oauth2UserInfoEPUrl, oidcFedAuthn,
                IdentityApplicationConstants.Authenticator.OIDC.OAUTH2_USER_INFO_EP_URL);
        propertiesList.add(userInfoUrlProp);

        Property checkSessionUrlProp = resolveFedAuthnProperty(oidcCheckSessionEPUrl, oidcFedAuthn,
                IdentityApplicationConstants.Authenticator.OIDC.OIDC_CHECK_SESSION_URL);
        propertiesList.add(checkSessionUrlProp);

        Property logoutUrlProp = resolveFedAuthnProperty(oidcLogoutEPUrl, oidcFedAuthn,
                IdentityApplicationConstants.Authenticator.OIDC.OIDC_LOGOUT_URL);
        propertiesList.add(logoutUrlProp);

        Property dcrUrlProp = resolveFedAuthnProperty(oAuth2DCREPUrl, oidcFedAuthn,
                IdentityApplicationConstants.Authenticator.OIDC.OAUTH2_DCR_EP_URL);
        propertiesList.add(dcrUrlProp);

        Property webFingerUrlProp = resolveFedAuthnProperty(
                StringUtils.isNotBlank(oIDCWebFingerEPUrlV2) ? oIDCWebFingerEPUrlV2 : oIDCWebFingerEPUrl, oidcFedAuthn,
                IdentityApplicationConstants.Authenticator.OIDC.OIDC_WEB_FINGER_EP_URL);
        propertiesList.add(webFingerUrlProp);

        Property jwksUrlProp = resolveFedAuthnProperty(oAuth2JWKSPage, oidcFedAuthn,
                IdentityApplicationConstants.Authenticator.OIDC.OAUTH2_JWKS_EP_URL);
        propertiesList.add(jwksUrlProp);

        Property discoveryUrlProp = resolveFedAuthnProperty(oIDCDiscoveryEPUrl, oidcFedAuthn,
                IdentityApplicationConstants.Authenticator.OIDC.OIDC_DISCOVERY_EP_URL);
        propertiesList.add(discoveryUrlProp);

        Property jwtScopeAsArrayProp = IdentityApplicationManagementUtil.getProperty(oidcFedAuthn.getProperties(),
                IdentityApplicationConstants.Authenticator.OIDC.ENABLE_JWT_SCOPE_AS_ARRAY);
        if (jwtScopeAsArrayProp == null) {
            jwtScopeAsArrayProp = new Property();
            jwtScopeAsArrayProp.setName(
                    IdentityApplicationConstants.Authenticator.OIDC.ENABLE_JWT_SCOPE_AS_ARRAY);
            jwtScopeAsArrayProp.setValue(
                    IdentityApplicationConstants.Authenticator.OIDC.ENABLE_JWT_SCOPE_AS_ARRAY_DEFAULT);
        }
        propertiesList.add(jwtScopeAsArrayProp);

        oidcFedAuthn.setProperties(propertiesList.toArray(new Property[0]));
        fedAuthnConfigs.add(oidcFedAuthn);

        FederatedAuthenticatorConfig passiveSTSFedAuthn = IdentityApplicationManagementUtil
                .getFederatedAuthenticator(identityProvider.getFederatedAuthenticatorConfigs(),
                        IdentityApplicationConstants.Authenticator.PassiveSTS.NAME);
        if (passiveSTSFedAuthn == null) {
            passiveSTSFedAuthn = new FederatedAuthenticatorConfig();
            passiveSTSFedAuthn.setName(IdentityApplicationConstants.Authenticator.PassiveSTS.NAME);
            passiveSTSFedAuthn.setDefinedByType(DefinedByType.SYSTEM);
        }

        propertiesList = new ArrayList<>();
        Property passiveSTSUrlProperty = IdentityApplicationManagementUtil.getProperty(passiveSTSFedAuthn
                .getProperties(), IdentityApplicationConstants.Authenticator.PassiveSTS.IDENTITY_PROVIDER_URL);
        if (passiveSTSUrlProperty == null) {
            passiveSTSUrlProperty = new Property();
            passiveSTSUrlProperty.setName(IdentityApplicationConstants.Authenticator.PassiveSTS.IDENTITY_PROVIDER_URL);
        }
        passiveSTSUrlProperty.setValue(passiveStsUrl);
        propertiesList.add(passiveSTSUrlProperty);

        Property stsIdPEntityIdProperty = IdentityApplicationManagementUtil.getProperty(passiveSTSFedAuthn
                .getProperties(), IdentityApplicationConstants.Authenticator.PassiveSTS.IDENTITY_PROVIDER_ENTITY_ID);
        if (stsIdPEntityIdProperty == null) {
            stsIdPEntityIdProperty = new Property();
            stsIdPEntityIdProperty.setName(IdentityApplicationConstants.Authenticator.PassiveSTS
                    .IDENTITY_PROVIDER_ENTITY_ID);
            stsIdPEntityIdProperty.setValue(IdPManagementUtil.getResidentIdPEntityId());
        }
        propertiesList.add(stsIdPEntityIdProperty);

        for (Property property : passiveSTSFedAuthn.getProperties()) {
            if (property != null && !IdentityApplicationConstants.Authenticator.PassiveSTS.IDENTITY_PROVIDER_URL
                    .equals(property.getName()) && !IdentityApplicationConstants.Authenticator.PassiveSTS
                    .IDENTITY_PROVIDER_ENTITY_ID.equals(property.getName())) {
                propertiesList.add(property);
            }
        }

        passiveSTSFedAuthn.setProperties(propertiesList.toArray(new Property[0]));
        fedAuthnConfigs.add(passiveSTSFedAuthn);

        FederatedAuthenticatorConfig stsFedAuthn = IdentityApplicationManagementUtil
                .getFederatedAuthenticator(identityProvider.getFederatedAuthenticatorConfigs(),
                        IdentityApplicationConstants.Authenticator.WSTrust.NAME);
        if (stsFedAuthn == null) {
            stsFedAuthn = new FederatedAuthenticatorConfig();
            stsFedAuthn.setName(IdentityApplicationConstants.Authenticator.WSTrust.NAME);
            stsFedAuthn.setDefinedByType(DefinedByType.SYSTEM);
        }
        propertiesList = new ArrayList<>(Arrays.asList(stsFedAuthn.getProperties()));
        if (IdentityApplicationManagementUtil.getProperty(stsFedAuthn.getProperties(),
                IdentityApplicationConstants.Authenticator.WSTrust.IDENTITY_PROVIDER_URL) == null) {
            Property stsUrlProp = new Property();
            stsUrlProp.setName(IdentityApplicationConstants.Authenticator.WSTrust.IDENTITY_PROVIDER_URL);
            stsUrlProp.setValue(stsUrl);
            propertiesList.add(stsUrlProp);
        }
        stsFedAuthn.setProperties(propertiesList.toArray(new Property[0]));
        fedAuthnConfigs.add(stsFedAuthn);

        FederatedAuthenticatorConfig sessionTimeoutConfig = new FederatedAuthenticatorConfig();
        sessionTimeoutConfig.setName(IdentityApplicationConstants.NAME);
        sessionTimeoutConfig.setDefinedByType(DefinedByType.SYSTEM);

        propertiesList = new ArrayList<>(Arrays.asList(sessionTimeoutConfig.getProperties()));

        Property cleanUpPeriodProp = new Property();
        cleanUpPeriodProp.setName(IdentityApplicationConstants.CLEAN_UP_PERIOD);
        String cleanUpPeriod = IdentityUtil.getProperty(IdentityConstants.ServerConfig.CLEAN_UP_PERIOD);
        if (StringUtils.isBlank(cleanUpPeriod)) {
            cleanUpPeriod = IdentityApplicationConstants.CLEAN_UP_PERIOD_DEFAULT;
        } else if (!StringUtils.isNumeric(cleanUpPeriod)) {
            log.warn("PersistanceCleanUpPeriod in identity.xml should be a numeric value");
            cleanUpPeriod = IdentityApplicationConstants.CLEAN_UP_PERIOD_DEFAULT;
        }
        cleanUpPeriodProp.setValue(cleanUpPeriod);
        propertiesList.add(cleanUpPeriodProp);

        sessionTimeoutConfig.setProperties(propertiesList.toArray(new Property[0]));
        fedAuthnConfigs.add(sessionTimeoutConfig);

        identityProvider.setFederatedAuthenticatorConfigs(fedAuthnConfigs
                .toArray(new FederatedAuthenticatorConfig[0]));

        ProvisioningConnectorConfig scimProvConn = IdentityApplicationManagementUtil
                .getProvisioningConnector(identityProvider.getProvisioningConnectorConfigs(),
                        "scim");
        if (scimProvConn == null) {
            scimProvConn = new ProvisioningConnectorConfig();
            scimProvConn.setName("scim");
        }
        propertiesList = new ArrayList<>(Arrays.asList(scimProvConn.getProvisioningProperties()));
        Property scimUserEndpointProperty = IdentityApplicationManagementUtil.getProperty(scimProvConn
                .getProvisioningProperties(), IdentityApplicationConstants.SCIM.USERS_EP_URL);
        if (scimUserEndpointProperty == null) {
            Property property = new Property();
            property.setName(IdentityApplicationConstants.SCIM.USERS_EP_URL);
            property.setValue(scimUsersEndpoint);
            propertiesList.add(property);
        } else if (!scimUsersEndpoint.equalsIgnoreCase(scimUserEndpointProperty.getValue())) {
            scimUserEndpointProperty.setValue(scimUsersEndpoint);
        }
        Property scimGroupEndpointProperty = IdentityApplicationManagementUtil.getProperty(scimProvConn
                .getProvisioningProperties(), IdentityApplicationConstants.SCIM.GROUPS_EP_URL);
        if (scimGroupEndpointProperty == null) {
            Property property = new Property();
            property.setName(IdentityApplicationConstants.SCIM.GROUPS_EP_URL);
            property.setValue(scimGroupsEndpoint);
            propertiesList.add(property);
        } else if (!scimGroupsEndpoint.equalsIgnoreCase(scimGroupEndpointProperty.getValue())) {
            scimGroupEndpointProperty.setValue(scimGroupsEndpoint);
        }

        Property scim2UserEndpointProperty = IdentityApplicationManagementUtil.getProperty(scimProvConn
                .getProvisioningProperties(), IdentityApplicationConstants.SCIM2.USERS_EP_URL);
        if (scim2UserEndpointProperty == null) {
            Property property = new Property();
            property.setName(IdentityApplicationConstants.SCIM2.USERS_EP_URL);
            property.setValue(scim2UsersEndpoint);
            propertiesList.add(property);
        } else if (!scim2UsersEndpoint.equalsIgnoreCase(scim2UserEndpointProperty.getValue())) {
            scim2UserEndpointProperty.setValue(scim2UsersEndpoint);
        }
        Property scim2GroupEndpointProperty = IdentityApplicationManagementUtil.getProperty(scimProvConn
                .getProvisioningProperties(), IdentityApplicationConstants.SCIM2.GROUPS_EP_URL);
        if (scim2GroupEndpointProperty == null) {
            Property property = new Property();
            property.setName(IdentityApplicationConstants.SCIM2.GROUPS_EP_URL);
            property.setValue(scim2GroupsEndpoint);
            propertiesList.add(property);
        } else if (!scim2GroupsEndpoint.equalsIgnoreCase(scim2GroupEndpointProperty.getValue())) {
            scim2GroupEndpointProperty.setValue(scim2GroupsEndpoint);
        }
        scimProvConn.setProvisioningProperties(propertiesList.toArray(new Property[0]));
        identityProvider.setProvisioningConnectorConfigs(new ProvisioningConnectorConfig[]{scimProvConn});
    }

    /**
     * Retrieves an IDP by it's ID.
     *
     * @param dbConnection Database Connection.
     * @param idpId        Identity Provider ID.
     * @param tenantId     Tenant ID of the IDP.
     * @param tenantDomain Tenant Domain of the IDP.
     * @return An Identity Provider with given name.
     * @throws IdentityProviderManagementException IdentityProviderManagementException
     */
    public IdentityProvider getIDPbyId(Connection dbConnection, int idpId, int tenantId,
                                       String tenantDomain) throws IdentityProviderManagementException {

        IdentityProvider idp = getIDP(dbConnection, null, idpId, null, tenantId, tenantDomain);
        if (idp != null
                && IdentityApplicationConstants.RESIDENT_IDP_RESERVED_NAME.equals(idp.getIdentityProviderName())) {
            fillResidentIdpProperties(idp, tenantDomain);
        }
        return idp;
    }

    /**
     * Retrieves an IDP by it's ID.
     *
     * @param dbConnection Database Connection.
     * @param resourceId   Identity Provider Resource ID.
     * @param tenantId     Tenant ID of the IDP.
     * @param tenantDomain Tenant Domain of the IDP.
     * @return An Identity Provider with given name.
     * @throws IdentityProviderManagementException IdentityProviderManagementException
     */
    public IdentityProvider getIDPbyResourceId(Connection dbConnection, String resourceId, int tenantId,
                                       String tenantDomain) throws IdentityProviderManagementException {

        IdentityProvider idp = getIDP(dbConnection, null, -1, resourceId, tenantId, tenantDomain);
        if (idp != null
                && IdentityApplicationConstants.RESIDENT_IDP_RESERVED_NAME.equals(idp.getIdentityProviderName())) {
            fillResidentIdpProperties(idp, tenantDomain);
        }
        return idp;
    }

    /**
     * Retrieve the identity provider name by the resource id.
     *
     * @param resourceId UUID of the IDP.
     * @return Name of the IDP.
     * @throws IdentityProviderManagementException Error while retrieving the IDP name from uuid.
     */
    public String getIDPNameByResourceId(String resourceId) throws IdentityProviderManagementException {

        String idpName;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(true)) {
            try (PreparedStatement prepStmt =
                         connection.prepareStatement(GET_IDP_NAME_BY_RESOURCE_ID_SQL)) {
                prepStmt.setString(1, resourceId);
                try (ResultSet result = prepStmt.executeQuery()) {
                    if (result.next()) {
                        idpName = result.getString("NAME");
                    } else {
                        return null;
                    }
                }
            }
        } catch (SQLException e) {
            throw new IdentityProviderManagementException(
                    "Error occurred while retrieving IDP name from uuid: " + resourceId, e);
        }
        return idpName;
    }

    // Check whether config is enabled to configure Entity ID in Resident IDP
    private boolean isUseEntityIDAsIssuerEnabled() {

        return Boolean.parseBoolean(IdentityUtil.getProperty(USE_ENTITY_ID_AS_ISSUER));
    }

    /**
     * @param dbConnection
     * @param idPName
     * @param idpId
     * @param tenantId
     * @param tenantDomain
     * @return
     * @throws IdentityProviderManagementException
     */
    private IdentityProvider getIDP(Connection dbConnection, String idPName, int idpId, String resourceId, int
                                            tenantId, String tenantDomain) throws IdentityProviderManagementException {

        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        IdentityProvider federatedIdp = null;
        boolean dbConnectionInitialized = true;
        if (dbConnection == null) {
            dbConnection = IdentityDatabaseUtil.getDBConnection(false);
        } else {
            dbConnectionInitialized = false;
        }

        try {
            // SP_IDP_ID, SP_IDP_PRIMARY, SP_IDP_HOME_REALM_ID,SP_IDP_CERTIFICATE,
            // SP_IDP_TOKEN_EP_ALIAS,
            // SP_IDP_INBOUND_PROVISIONING_ENABLED,SP_IDP_INBOUND_PROVISIONING_USER_STORE_ID,
            // SP_IDP_USER_CLAIM_URI,
            // SP_IDP_ROLE_CLAIM_URI,SP_IDP_DEFAULT_AUTHENTICATOR_NAME,SP_IDP_DEFAULT_PRO_CONNECTOR_NAME
            String sqlStmt = IdPManagementConstants.SQLQueries.GET_IDP_BY_RESOURCE_ID_SQL;
            if (StringUtils.isBlank(resourceId)) {
                sqlStmt = IdPManagementConstants.SQLQueries.GET_IDP_BY_NAME_SQL;
                if (StringUtils.isEmpty(idPName)) {
                    sqlStmt = IdPManagementConstants.SQLQueries.GET_IDP_BY_ID_SQL;
                }
            }
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, tenantId);
            prepStmt.setInt(2, MultitenantConstants.SUPER_TENANT_ID);
            if (StringUtils.isNotEmpty(resourceId)) {
                prepStmt.setString(3, resourceId);
            } else if (StringUtils.isNotEmpty(idPName)) {
                prepStmt.setString(3, idPName);
            } else {
                prepStmt.setInt(3, idpId);
            }

            rs = prepStmt.executeQuery();

            if (rs.next()) {
                federatedIdp = new IdentityProvider();
                idpId = rs.getInt("ID");
                federatedIdp.setId(Integer.toString(idpId));
                idPName = rs.getString("NAME");
                federatedIdp.setIdentityProviderName(idPName);
                resourceId = rs.getString("UUID");
                federatedIdp.setResourceId(resourceId);
                federatedIdp.setImageUrl(rs.getString("IMAGE_URL"));

                if ((IdPManagementConstants.IS_TRUE_VALUE).equals(rs.getString("IS_PRIMARY"))) {
                    federatedIdp.setPrimary(true);
                } else {
                    federatedIdp.setPrimary(false);
                }

                federatedIdp.setHomeRealmId(rs.getString("HOME_REALM_ID"));
                federatedIdp.setCertificate(getBlobValue(rs.getBinaryStream("CERTIFICATE")));
                federatedIdp.setAlias(rs.getString("ALIAS"));

                JustInTimeProvisioningConfig jitProConfig = new JustInTimeProvisioningConfig();
                if ((IdPManagementConstants.IS_TRUE_VALUE).equals(rs.getString("INBOUND_PROV_ENABLED"))) {
                    jitProConfig.setProvisioningEnabled(true);
                } else {
                    jitProConfig.setProvisioningEnabled(false);
                }

                jitProConfig.setProvisioningUserStore(rs.getString("INBOUND_PROV_USER_STORE_ID"));
                federatedIdp.setJustInTimeProvisioningConfig(jitProConfig);

                String userClaimUri = rs.getString("USER_CLAIM_URI");
                String roleClaimUri = rs.getString("ROLE_CLAIM_URI");

                String defaultAuthenticatorName = rs.getString("DEFAULT_AUTHENTICATOR_NAME");
                String defaultProvisioningConnectorConfigName = rs.getString("DEFAULT_PRO_CONNECTOR_NAME");
                federatedIdp.setIdentityProviderDescription(rs.getString("DESCRIPTION"));

                // IS_FEDERATION_HUB_IDP
                if (IdPManagementConstants.IS_TRUE_VALUE.equals(rs.getString("IS_FEDERATION_HUB"))) {
                    federatedIdp.setFederationHub(true);
                } else {
                    federatedIdp.setFederationHub(false);
                }

                if (federatedIdp.getClaimConfig() == null) {
                    federatedIdp.setClaimConfig(new ClaimConfig());
                }

                // IS_LOCAL_CLAIM_DIALECT
                if (IdPManagementConstants.IS_TRUE_VALUE.equals(rs.getString("IS_LOCAL_CLAIM_DIALECT"))) {
                    federatedIdp.getClaimConfig().setLocalClaimDialect(true);
                } else {
                    federatedIdp.getClaimConfig().setLocalClaimDialect(false);
                }

                federatedIdp.setProvisioningRole(rs.getString("PROVISIONING_ROLE"));

                if (IdPManagementConstants.IS_TRUE_VALUE.equals(rs.getString("IS_ENABLED"))) {
                    federatedIdp.setEnable(true);
                } else {
                    federatedIdp.setEnable(false);
                }

                federatedIdp.setDisplayName(rs.getString("DISPLAY_NAME"));

                if (defaultProvisioningConnectorConfigName != null) {
                    ProvisioningConnectorConfig defaultProConnector = new ProvisioningConnectorConfig();
                    defaultProConnector.setName(defaultProvisioningConnectorConfigName);
                    federatedIdp.setDefaultProvisioningConnectorConfig(defaultProConnector);
                }

                // get federated authenticators.
                federatedIdp.setFederatedAuthenticatorConfigs(getFederatedAuthenticatorConfigs(
                        dbConnection, idPName, federatedIdp, tenantId));

                // Retrieve encrypted secrets from DB, decrypt and set to the system federated authenticator configs.
                if (federatedIdp.getFederatedAuthenticatorConfigs().length > 0 &&
                        federatedIdp.getFederatedAuthenticatorConfigs()[0].getDefinedByType() == DefinedByType.SYSTEM) {
                    try {
                        if (!StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenantDomain)) {
                            PrivilegedCarbonContext.startTenantFlow();
                            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
                            PrivilegedCarbonContext.getThreadLocalCarbonContext()
                                    .setTenantId(IdentityTenantUtil.getTenantId(tenantDomain));
                        }
                        federatedIdp = idpSecretsProcessorService.decryptAssociatedSecrets(federatedIdp);
                    } finally {
                        if (!StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenantDomain)) {
                            PrivilegedCarbonContext.endTenantFlow();
                        }
                    }
                }

                if (defaultAuthenticatorName != null && federatedIdp.getFederatedAuthenticatorConfigs() != null) {
                    federatedIdp.setDefaultAuthenticatorConfig(IdentityApplicationManagementUtil
                            .getFederatedAuthenticator(federatedIdp.getFederatedAuthenticatorConfigs(),
                                    defaultAuthenticatorName));
                }

                if (federatedIdp.getClaimConfig().isLocalClaimDialect()) {
                    federatedIdp.setClaimConfig(getLocalIdPDefaultClaimValues(dbConnection,
                            idPName, userClaimUri, roleClaimUri, idpId, tenantId));
                } else {
                    // get claim configuration.
                    federatedIdp.setClaimConfig(getIdPClaimConfiguration(dbConnection, idPName,
                            userClaimUri, roleClaimUri, idpId, tenantId));
                }

                // get provisioning connectors.
                federatedIdp.setProvisioningConnectorConfigs(getProvisioningConnectorConfigs(
                        dbConnection, idPName, idpId, tenantId));

                // get permission and role configuration.
                federatedIdp.setPermissionAndRoleConfig(getPermissionsAndRoleConfiguration(
                        dbConnection, idPName, idpId, tenantId));

                // Get federated idp groups.
                federatedIdp.setIdPGroupConfig(getIdPGroupConfiguration(dbConnection, idpId));

                List<IdentityProviderProperty> propertyList = filterIdentityProperties(federatedIdp,
                        getIdentityPropertiesByIdpId(dbConnection, idpId, tenantId));
                if (IdentityApplicationConstants.RESIDENT_IDP_RESERVED_NAME.equals(idPName)) {
                    // Resolve resident IdP properties from the organization hierarchy if inheritance is enabled.
                    if (isIdpPropertyInheritanceEnabled(tenantDomain)) {
                        propertyList = resolveResidentIdpProperties(tenantDomain, federatedIdp, dbConnection);
                    }
                    // Populate non-existing properties with default values.
                    propertyList = resolveConnectorProperties(propertyList, tenantDomain);
                }

                federatedIdp.setIdpProperties(propertyList.toArray(new IdentityProviderProperty[0]));

            }
            return federatedIdp;
        } catch (SQLException e) {
            throw new IdentityProviderManagementException("Error occurred while retrieving Identity Provider " +
                    "information for tenant : " + tenantDomain + " and Identity Provider name : " + idPName, e);
        } catch (ConnectorException e) {
            throw new IdentityProviderManagementException("Error occurred while retrieving the identity connector " +
                    "configurations.", e);
        } catch (SecretManagementException e) {
            throw new IdentityProviderManagementException("Error while retrieving secrets of Identity provider : " +
                    idPName + " in tenant : " + tenantDomain, e);
        } catch (OrganizationManagementException e) {
            throw new IdentityProviderManagementException("Error while checking if the tenant: " + tenantDomain +
                    " is an organization.", e);
        } finally {
            if (dbConnectionInitialized) {
                IdentityDatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
            } else {
                IdentityDatabaseUtil.closeAllConnections(null, rs, prepStmt);
            }
        }
    }

    /**
     * Checks whether the resident IDP properties should be inherited from the organization hierarchy.
     *
     * @param tenantDomain Tenant domain of the IDP.
     * @return true if inheritance is enabled, false otherwise.
     * @throws OrganizationManagementException When an error occurs while checking if the tenant is an organization.
     */
    private boolean isIdpPropertyInheritanceEnabled(String tenantDomain) throws OrganizationManagementException {

        return OrganizationManagementUtil.isOrganization(tenantDomain) &&
                !TenantMgtUtil.isTenantCreation() &&
                Utils.isLoginAndRegistrationConfigInheritanceEnabled(tenantDomain);
    }

    /**
     * Handles inheritance of the the resident IDP properties from the organization hierarchy.
     *
     * @param tenantDomain  Tenant domain of the IDP.
     * @param federatedIdp  The federated IDP.
     * @param dbConnection  Database connection.
     * @return List of inherited IdentityProviderProperties of the resident IDP.
     * @throws IdentityProviderManagementException IdentityProviderManagementException
     */
    private List<IdentityProviderProperty> resolveResidentIdpProperties(String tenantDomain,
                                                                        IdentityProvider federatedIdp,
                                                                        Connection dbConnection)
            throws IdentityProviderManagementException {

        OrganizationManager organizationManager =
                IdpMgtServiceComponentHolder.getInstance().getOrganizationManager();
        OrgResourceResolverService orgResourceResolverService =
                IdpMgtServiceComponentHolder.getInstance().getOrgResourceResolverService();

        try {
            String orgId = organizationManager.resolveOrganizationId(tenantDomain);
            return orgResourceResolverService.getResourcesFromOrgHierarchy(
                    orgId,
                    LambdaExceptionUtils.rethrowFunction(
                            orgID -> idpPropertyRetriever(orgID, federatedIdp, dbConnection)),
                    new MergeAllAggregationStrategy<>(this::mergePropertyLists));
        } catch (OrganizationManagementException e) {
                throw new IdentityProviderManagementException("Error occurred while retrieving organization " +
                        "information for tenant : " + tenantDomain, e);
        } catch (OrgResourceHierarchyTraverseException e) {
            throw new IdentityProviderManagementException("Error occurred while traversing the organization " +
                    "hierarchy for tenant : " + tenantDomain, e);
        }
    }

    /**
     * Retrieves the IDP properties from the database.
     *
     * @param orgId               Organization ID.
     * @param identityProvider    Identity Provider.
     * @param dbConnection        Database connection.
     * @return List of IdentityProviderProperties of a specific IDP.
     * @throws OrganizationManagementException OrganizationManagementException
     * @throws SQLException                      SQLException
     * @throws IdentityProviderManagementClientException IdentityProviderManagementClientException
     */
    private Optional<List<IdentityProviderProperty>> idpPropertyRetriever(String orgId,
                                                                          IdentityProvider identityProvider,
                                                                          Connection dbConnection)
            throws IdentityProviderManagementException {

        OrganizationManager organizationManager =
                IdpMgtServiceComponentHolder.getInstance().getOrganizationManager();
        try {
            String tenantDomain = organizationManager.resolveTenantDomain(orgId);
            int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

            return Optional.of(filterIdentityProperties(identityProvider, getIdentityPropertiesByIdpId(dbConnection,
                    getIdentityProviderIdentifier(dbConnection, IdentityApplicationConstants.RESIDENT_IDP_RESERVED_NAME,
                            tenantId), tenantId)));
        } catch (SQLException e) {
            throw new IdentityProviderManagementServerException("Error while retrieving resident IDP properties of " +
                    orgId, e);
        } catch (OrganizationManagementException e) {
            throw new IdentityProviderManagementServerException("Error while resolving tenant domain of: " + orgId, e);
        }
    }

    /**
     * Merges two lists of IdentityProviderProperty objects.
     *
     * @param list1 First list of IdentityProviderProperty objects.
     * @param list2 Second list of IdentityProviderProperty objects.
     * @return Merged list of IdentityProviderProperty objects.
     */
    private List<IdentityProviderProperty> mergePropertyLists(List<IdentityProviderProperty> list1,
                                                              List<IdentityProviderProperty> list2) {

        /* Filter properties that should not be inherited.
         * startsWith is used since password expiery rules are in the format of
         * passwordExpiry.rulex where x is the rule number.
         */
        list2 = list2.stream()
                .filter(property -> {
                    if (IdPManagementConstants.INHERITANCE_DISABLED_GOVERNANCE_PROPERTIES.contains(
                            property.getName())) {
                        return false;
                    }

                    if (property.getName().startsWith(PASSWORD_EXPIRY_RULES_KEY_PREFIX)) {
                        String[] ruleTokens = property.getValue().split(",");
                        if (Arrays.stream(ruleTokens).anyMatch(token -> PASSWORD_EXPIRY_RULES_GROUPS.equals(token))) {
                            return false;
                        }
                    }

                    return true;
                }).collect(Collectors.toList());

        for (IdentityProviderProperty property : list2) {
            if (list1.stream().noneMatch(p -> p.getName().equals(property.getName()))) {
                list1.add(property);
            }
        }
        return list1;
    }

    /**
     * To filter out the properties related with just in time provisioning and to send back only the remaning IDP
     * properties.
     *
     * @param federatedIdp               Relevant IDP.
     * @param identityProviderProperties Identity Provider Properties.
     * @return identity provider properties after removing the relevant JIT specific properties.
     */
    private List<IdentityProviderProperty> filterIdentityProperties(IdentityProvider federatedIdp,
                                                                    List<IdentityProviderProperty>
                                                                            identityProviderProperties)
            throws IdentityProviderManagementException {

        JustInTimeProvisioningConfig justInTimeProvisioningConfig = federatedIdp.getJustInTimeProvisioningConfig();
        FederatedAssociationConfig federatedAssociationConfig = new FederatedAssociationConfig();

        if (justInTimeProvisioningConfig != null) {
            identityProviderProperties.forEach(identityProviderProperty -> {
                if (identityProviderProperty.getName().equals(IdPManagementConstants.PASSWORD_PROVISIONING_ENABLED)) {
                    justInTimeProvisioningConfig
                            .setPasswordProvisioningEnabled(Boolean.parseBoolean(identityProviderProperty.getValue()));
                } else if (identityProviderProperty.getName().equals(IdPManagementConstants.MODIFY_USERNAME_ENABLED)) {
                    justInTimeProvisioningConfig
                            .setModifyUserNameAllowed(Boolean.parseBoolean(identityProviderProperty.getValue()));
                } else if (identityProviderProperty.getName().equals(IdPManagementConstants.PROMPT_CONSENT_ENABLED)) {
                    justInTimeProvisioningConfig
                            .setPromptConsent(Boolean.parseBoolean(identityProviderProperty.getValue()));
                } else if (identityProviderProperty.getName()
                        .equals(IdPManagementConstants.ASSOCIATE_LOCAL_USER_ENABLED)) {
                    justInTimeProvisioningConfig
                            .setAssociateLocalUserEnabled(Boolean.parseBoolean(identityProviderProperty.getValue()));
                } else if (identityProviderProperty.getName()
                        .equals(IdPManagementConstants.SKIP_JIT_ON_ATTR_ACCOUNT_LOOKUP_FAILURE)) {
                    justInTimeProvisioningConfig.setSkipJITOnAttrAccLookUpFailureEnabled(
                            Boolean.parseBoolean(identityProviderProperty.getValue()));
                } else if (IdPManagementConstants.SYNC_ATTRIBUTE_METHOD
                        .equals(identityProviderProperty.getName())) {
                    justInTimeProvisioningConfig.setAttributeSyncMethod(identityProviderProperty.getValue());
                }
            });
            populateAccountLookupAttributes(justInTimeProvisioningConfig, identityProviderProperties.toArray(
                    new IdentityProviderProperty[0]));
        }

        identityProviderProperties.forEach(identityProviderProperty -> {
            switch (identityProviderProperty.getName()) {
                case IdPManagementConstants.FEDERATED_ASSOCIATION_ENABLED:
                    federatedAssociationConfig
                            .setEnabled(Boolean.parseBoolean(identityProviderProperty.getValue()));
                    break;
                case IdPManagementConstants.LOOKUP_ATTRIBUTES:
                    if (identityProviderProperty.getValue() != null) {
                        String[] attributes = identityProviderProperty.getValue().split(",");
                        federatedAssociationConfig.setLookupAttributes(attributes);
                    }
                    break;
            }
        });

        federatedIdp.setFederatedAssociationConfig(federatedAssociationConfig);

        String templateId = getTemplateId(identityProviderProperties);
        if (StringUtils.isNotEmpty(templateId)) {
            federatedIdp.setTemplateId(templateId);
        }
        identityProviderProperties.removeIf(identityProviderProperty -> (
                identityProviderProperty.getName().equals(IdPManagementConstants.MODIFY_USERNAME_ENABLED)
                        || identityProviderProperty.getName()
                        .equals(IdPManagementConstants.PASSWORD_PROVISIONING_ENABLED) || identityProviderProperty
                        .getName().equals(IdPManagementConstants.PROMPT_CONSENT_ENABLED) ||
                        IdPManagementConstants.ASSOCIATE_LOCAL_USER_ENABLED
                                .equals(identityProviderProperty.getName()) ||
                        IdPManagementConstants.PRIMARY_ACCOUNT_LOOKUP_ATTRIBUTE_MAPPING
                                .equals(identityProviderProperty.getName()) ||
                        IdPManagementConstants.SECONDARY_ACCOUNT_LOOKUP_ATTRIBUTE_MAPPING
                                .equals(identityProviderProperty.getName()) ||
                        IdPManagementConstants.SKIP_JIT_ON_ATTR_ACCOUNT_LOOKUP_FAILURE
                                        .equals(identityProviderProperty.getName()) ||
                        IdPManagementConstants.SYNC_ATTRIBUTE_METHOD
                                .equals(identityProviderProperty.getName()) ||
                        IdPManagementConstants.FEDERATED_ASSOCIATION_ENABLED
                                .equals(identityProviderProperty.getName()) ||
                        IdPManagementConstants.LOOKUP_ATTRIBUTES
                                .equals(identityProviderProperty.getName())));
        return identityProviderProperties;
    }

    private void populateAccountLookupAttributes(JustInTimeProvisioningConfig justInTimeProvisioningConfig,
                                                 IdentityProviderProperty[] identityProviderProperties)
            throws IdentityProviderManagementException {

        IdentityProviderProperty primaryAccountLookupAttributeMappingProperty =
                IdentityApplicationManagementUtil.getProperty(identityProviderProperties,
                        IdPManagementConstants.PRIMARY_ACCOUNT_LOOKUP_ATTRIBUTE_MAPPING);
        IdentityProviderProperty secondaryAccountLookupAttributeMappingProperty =
                IdentityApplicationManagementUtil.getProperty(identityProviderProperties,
                        IdPManagementConstants.SECONDARY_ACCOUNT_LOOKUP_ATTRIBUTE_MAPPING);
        if (primaryAccountLookupAttributeMappingProperty == null ||
                StringUtils.isBlank(primaryAccountLookupAttributeMappingProperty.getValue())) {
            return;
        }
        AccountLookupAttributeMappingConfig primaryAccountLookupAttributeMappingConfig =
                buildAccountLookupAttributeMappingConfig(primaryAccountLookupAttributeMappingProperty.getValue());
        List<AccountLookupAttributeMappingConfig> accountLookupAttributeMappingConfigs = new ArrayList<>();
        accountLookupAttributeMappingConfigs.add(primaryAccountLookupAttributeMappingConfig);

        if (secondaryAccountLookupAttributeMappingProperty != null &&
                StringUtils.isNotBlank(secondaryAccountLookupAttributeMappingProperty.getValue())) {
            AccountLookupAttributeMappingConfig secondaryAccountLookupAttributeMappingConfig =
                    buildAccountLookupAttributeMappingConfig(secondaryAccountLookupAttributeMappingProperty.getValue());
            accountLookupAttributeMappingConfigs.add(secondaryAccountLookupAttributeMappingConfig);
        }
        justInTimeProvisioningConfig.setAccountLookupAttributeMappings(accountLookupAttributeMappingConfigs.toArray(
                new AccountLookupAttributeMappingConfig[0]));
    }

    private AccountLookupAttributeMappingConfig buildAccountLookupAttributeMappingConfig(
            String accountLookupAttributeMappingString)
            throws IdentityProviderManagementException {

        String[] parts = accountLookupAttributeMappingString.split(ACCOUNT_LOOKUP_ATTR_MAPPING_SEPARATOR);
        if (parts.length != 2 || StringUtils.isBlank(parts[0]) || StringUtils.isBlank(parts[1])) {
            throw new IdentityProviderManagementException("Invalid account lookup attribute mapping property: " +
                    accountLookupAttributeMappingString);
        }

        AccountLookupAttributeMappingConfig accountLookupAttributeMappingConfig =
                new AccountLookupAttributeMappingConfig();
        accountLookupAttributeMappingConfig.setFederatedAttribute(parts[0].trim());
        accountLookupAttributeMappingConfig.setLocalAttribute(parts[1].trim());
        return accountLookupAttributeMappingConfig;
    }

    private void fillAccountLookUpAttributesIdpProperties(JustInTimeProvisioningConfig justInTimeProvisioningConfig,
                                                          IdentityProviderProperty primaryAccountLookupAttributesProperty,
                                                          IdentityProviderProperty secondaryAccountLookupAttributesProperty)
            throws IdentityProviderManagementException {

        if (justInTimeProvisioningConfig == null) {
            return;
        }
        AccountLookupAttributeMappingConfig[] accountLookupAttributeMappings =
                justInTimeProvisioningConfig.getAccountLookupAttributeMappings();
        if (accountLookupAttributeMappings == null || accountLookupAttributeMappings.length == 0) {
            return;
        }
        if (accountLookupAttributeMappings.length > 2) {
            throw new IdentityProviderManagementException("Error occurred while storing account lookup attributes." +
                    " More than two account lookup attribute mappings are not supported.");
        }

        AccountLookupAttributeMappingConfig primaryMapping = accountLookupAttributeMappings[0];
        if (primaryMapping == null || (StringUtils.isBlank(primaryMapping.getFederatedAttribute()) &
                StringUtils.isBlank(primaryMapping.getLocalAttribute()))) {
            return;
        }
        if (StringUtils.isBlank(primaryMapping.getFederatedAttribute()) ||
                StringUtils.isBlank(primaryMapping.getLocalAttribute())) {
            throw new IdentityProviderManagementException("Error occurred while storing account lookup attributes." +
                    " Primary account lookup attribute mapping is empty.");
        }
        primaryAccountLookupAttributesProperty.setValue(
                primaryMapping.getFederatedAttribute() + ACCOUNT_LOOKUP_ATTR_MAPPING_SEPARATOR +
                        primaryMapping.getLocalAttribute());
        if (accountLookupAttributeMappings.length > 1) {
            AccountLookupAttributeMappingConfig secondaryMapping = accountLookupAttributeMappings[1];
            if (secondaryMapping == null || (StringUtils.isBlank(secondaryMapping.getFederatedAttribute()) &&
                    StringUtils.isBlank(secondaryMapping.getLocalAttribute()))) {
                return;
            }
            if (StringUtils.isBlank(secondaryMapping.getFederatedAttribute()) ||
                    StringUtils.isBlank(secondaryMapping.getLocalAttribute())) {
                throw new IdentityProviderManagementException(
                        "Error occurred while storing account lookup attributes." +
                                " Secondary account lookup attribute mapping is empty.");
            }
            secondaryAccountLookupAttributesProperty.setValue(
                    secondaryMapping.getFederatedAttribute() + ACCOUNT_LOOKUP_ATTR_MAPPING_SEPARATOR +
                            secondaryMapping.getLocalAttribute());
        }
    }

    /**
     * Add templateId property for the IDP.
     *
     * @param identityProviderProperties List of IdP properties.
     * @param identityProvider           Identity provider.
     */
    private void addTemplateIdProperty(List<IdentityProviderProperty> identityProviderProperties,
                                                           IdentityProvider identityProvider) {

        boolean containsTemplateIdProperty = identityProviderProperties.stream().anyMatch(identityProviderProperty ->
                TEMPLATE_ID_IDP_PROPERTY_NAME.equals(identityProviderProperty.getName()));

        if (StringUtils.isNotBlank(identityProvider.getTemplateId()) && !containsTemplateIdProperty) {
            IdentityProviderProperty templateIdProperty = new IdentityProviderProperty();
            templateIdProperty.setName(TEMPLATE_ID_IDP_PROPERTY_NAME);
            templateIdProperty.setDisplayName(TEMPLATE_ID_IDP_PROPERTY_DISPLAY_NAME);
            templateIdProperty.setValue(identityProvider.getTemplateId());
            identityProviderProperties.add(templateIdProperty);
        }
    }

    /**
     * Get template id from IDP meta data.
     *
     * @param propertyList IDP meta data.
     * @return Template id.
     */
    private String getTemplateId(List<IdentityProviderProperty> propertyList) {

        return propertyList.stream()
                .filter(property -> TEMPLATE_ID_IDP_PROPERTY_NAME.equals(property.getName()))
                .findFirst()
                .map(IdentityProviderProperty::getValue)
                .orElse(StringUtils.EMPTY);
    }

    /**
     * @param dbConnection
     * @param property      Property which has a unique value like EntityID to specifically identify a IdentityProvider
     *                      Unless it will return first matched IdentityProvider
     * @param value
     * @param authenticator
     * @param tenantId
     * @param tenantDomain
     * @return
     * @throws IdentityProviderManagementException
     */
    public IdentityProvider getIdPByAuthenticatorPropertyValue(Connection dbConnection, String property, String
            value, String authenticator, int tenantId, String tenantDomain) throws IdentityProviderManagementException {

        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        IdentityProvider federatedIdp = null;
        boolean dbConnectionInitialized = true;
        if (dbConnection == null) {
            dbConnection = IdentityDatabaseUtil.getDBConnection(false);
        } else {
            dbConnectionInitialized = false;
        }
        try {
            // SP_IDP_ID, SP_IDP_NAME, SP_IDP_PRIMARY, SP_IDP_HOME_REALM_ID,SP_IDP_CERTIFICATE,
            // SP_IDP_TOKEN_EP_ALIAS,
            // SP_IDP_INBOUND_PROVISIONING_ENABLED,SP_IDP_INBOUND_PROVISIONING_USER_STORE_ID,
            // SP_IDP_USER_CLAIM_URI,
            // SP_IDP_ROLE_CLAIM_URI,SP_IDP_DEFAULT_AUTHENTICATOR_NAME,SP_IDP_DEFAULT_PRO_CONNECTOR_NAME
            String sqlStmt = IdPManagementConstants.SQLQueries.GET_IDP_BY_AUTHENTICATOR_PROPERTY_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setString(1, property);
            prepStmt.setString(2, value);
            prepStmt.setInt(3, tenantId);
            prepStmt.setString(4, authenticator);
            rs = prepStmt.executeQuery();
            int idpId = -1;
            String idPName = "";

            if (rs.next()) {
                federatedIdp = new IdentityProvider();

                idpId = rs.getInt("ID");
                idPName = rs.getString("NAME");

                federatedIdp.setIdentityProviderName(idPName);

                if ((IdPManagementConstants.IS_TRUE_VALUE).equals(rs.getString("IS_PRIMARY"))) {
                    federatedIdp.setPrimary(true);
                } else {
                    federatedIdp.setPrimary(false);
                }

                federatedIdp.setHomeRealmId(rs.getString("HOME_REALM_ID"));
                federatedIdp.setCertificate(getBlobValue(rs.getBinaryStream("CERTIFICATE")));
                federatedIdp.setAlias(rs.getString("ALIAS"));

                JustInTimeProvisioningConfig jitProConfig = new JustInTimeProvisioningConfig();
                if (IdPManagementConstants.IS_TRUE_VALUE.equals(rs.getString("INBOUND_PROV_ENABLED"))) {
                    jitProConfig.setProvisioningEnabled(true);
                } else {
                    jitProConfig.setProvisioningEnabled(false);
                }

                jitProConfig.setProvisioningUserStore(rs.getString("INBOUND_PROV_USER_STORE_ID"));
                federatedIdp.setJustInTimeProvisioningConfig(jitProConfig);

                String userClaimUri = rs.getString("USER_CLAIM_URI");
                String roleClaimUri = rs.getString("ROLE_CLAIM_URI");

                String defaultAuthenticatorName = rs.getString("DEFAULT_AUTHENTICATOR_NAME");
                String defaultAuthenticatorDefinedByType = rs.getString(DEFINED_BY_COLUMN);
                String defaultProvisioningConnectorConfigName = rs.getString("DEFAULT_PRO_CONNECTOR_NAME");
                federatedIdp.setIdentityProviderDescription(rs.getString("DESCRIPTION"));

                // IS_FEDERATION_HUB_IDP
                if (IdPManagementConstants.IS_TRUE_VALUE.equals(rs.getString("IS_FEDERATION_HUB"))) {
                    federatedIdp.setFederationHub(true);
                } else {
                    federatedIdp.setFederationHub(false);
                }

                if (federatedIdp.getClaimConfig() == null) {
                    federatedIdp.setClaimConfig(new ClaimConfig());
                }

                // IS_LOCAL_CLAIM_DIALECT
                if (IdPManagementConstants.IS_TRUE_VALUE.equals(rs.getString("IS_LOCAL_CLAIM_DIALECT"))) {
                    federatedIdp.getClaimConfig().setLocalClaimDialect(true);
                } else {
                    federatedIdp.getClaimConfig().setLocalClaimDialect(false);
                }

                federatedIdp.setProvisioningRole(rs.getString("PROVISIONING_ROLE"));

                if (IdPManagementConstants.IS_TRUE_VALUE.equals(rs.getString("IS_ENABLED"))) {
                    federatedIdp.setEnable(true);
                } else {
                    federatedIdp.setEnable(false);
                }

                federatedIdp.setDisplayName(rs.getString("DISPLAY_NAME"));

                if (defaultAuthenticatorName != null) {
                    FederatedAuthenticatorConfig defaultAuthenticator = new FederatedAuthenticatorConfig();
                    defaultAuthenticator.setName(defaultAuthenticatorName);
                    defaultAuthenticator.setDefinedByType(DefinedByType.valueOf(
                            defaultAuthenticatorDefinedByType));
                    federatedIdp.setDefaultAuthenticatorConfig(defaultAuthenticator);
                }

                if (defaultProvisioningConnectorConfigName != null) {
                    ProvisioningConnectorConfig defaultProConnector = new ProvisioningConnectorConfig();
                    defaultProConnector.setName(defaultProvisioningConnectorConfigName);
                    federatedIdp.setDefaultProvisioningConnectorConfig(defaultProConnector);
                }

                // get federated authenticators.
                federatedIdp.setFederatedAuthenticatorConfigs(getFederatedAuthenticatorConfigs(
                        dbConnection, idPName, federatedIdp, tenantId));

                if (federatedIdp.getClaimConfig().isLocalClaimDialect()) {
                    federatedIdp.setClaimConfig(getLocalIdPDefaultClaimValues(dbConnection,
                            idPName, userClaimUri, roleClaimUri, idpId, tenantId));
                } else {
                    // get claim configuration.
                    federatedIdp.setClaimConfig(getIdPClaimConfiguration(dbConnection, idPName,
                            userClaimUri, roleClaimUri, idpId, tenantId));
                }

                // get provisioning connectors.
                federatedIdp.setProvisioningConnectorConfigs(getProvisioningConnectorConfigs(
                        dbConnection, idPName, idpId, tenantId));

                // get permission and role configuration.
                federatedIdp.setPermissionAndRoleConfig(getPermissionsAndRoleConfiguration(
                        dbConnection, idPName, idpId, tenantId));

                // Get federated idp groups.
                federatedIdp.setIdPGroupConfig(getIdPGroupConfiguration(dbConnection, idpId));

                List<IdentityProviderProperty> propertyList = filterIdentityProperties(federatedIdp,
                        getIdentityPropertiesByIdpId(dbConnection, Integer.parseInt(rs.getString("ID"))
                                ,tenantId));
                if (IdentityApplicationConstants.RESIDENT_IDP_RESERVED_NAME.equals(idPName)) {
                    propertyList = resolveConnectorProperties(propertyList, tenantDomain);
                    fillResidentIdpProperties(federatedIdp, tenantDomain);
                }
                federatedIdp.setIdpProperties(propertyList.toArray(new IdentityProviderProperty[0]));

            }
            return federatedIdp;
        } catch (SQLException e) {
            throw new IdentityProviderManagementException("Error occurred while retrieving Identity Provider " +
                    "information for Authenticator Property : " + property + " and value : " + value, e);
        } catch (ConnectorException e) {
            throw new IdentityProviderManagementException("Error occurred while retrieving the identity connector " +
                    "configurations.", e);
        } finally {
            if (dbConnectionInitialized) {
                IdentityDatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
            } else {
                IdentityDatabaseUtil.closeAllConnections(null, rs, prepStmt);
            }
        }
    }

    /**
     * @param dbConnection
     * @param property     Property which has a unique value like EntityID to specifically identify a IdentityProvider
     *                     Unless it will return first matched IdentityProvider
     * @param value
     * @param tenantId
     * @param tenantDomain
     * @return
     * @throws IdentityProviderManagementException
     */
    public IdentityProvider getIdPByAuthenticatorPropertyValue(Connection dbConnection, String property, String value,
                                                               int tenantId, String tenantDomain)
            throws IdentityProviderManagementException {

        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        IdentityProvider federatedIdp = null;
        boolean dbConnectionInitialized = true;
        if (dbConnection == null) {
            dbConnection = IdentityDatabaseUtil.getDBConnection(false);
        } else {
            dbConnectionInitialized = false;
        }
        try {
            // SP_IDP_ID, SP_IDP_NAME, SP_IDP_PRIMARY, SP_IDP_HOME_REALM_ID,SP_IDP_CERTIFICATE,
            // SP_IDP_TOKEN_EP_ALIAS,
            // SP_IDP_INBOUND_PROVISIONING_ENABLED,SP_IDP_INBOUND_PROVISIONING_USER_STORE_ID,
            // SP_IDP_USER_CLAIM_URI,
            // SP_IDP_ROLE_CLAIM_URI,SP_IDP_DEFAULT_AUTHENTICATOR_NAME,SP_IDP_DEFAULT_PRO_CONNECTOR_NAME
            String sqlStmt = IdPManagementConstants.SQLQueries.GET_IDP_BY_AUTHENTICATOR_PROPERTY;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setString(1, property);
            prepStmt.setString(2, value);
            prepStmt.setInt(3, tenantId);
            rs = prepStmt.executeQuery();
            int idpId = -1;
            String idPName = "";

            if (rs.next()) {
                federatedIdp = new IdentityProvider();

                idpId = rs.getInt("ID");
                idPName = rs.getString("NAME");

                federatedIdp.setIdentityProviderName(idPName);

                if ((IdPManagementConstants.IS_TRUE_VALUE).equals(rs.getString("IS_PRIMARY"))) {
                    federatedIdp.setPrimary(true);
                } else {
                    federatedIdp.setPrimary(false);
                }

                federatedIdp.setHomeRealmId(rs.getString("HOME_REALM_ID"));
                federatedIdp.setCertificate(getBlobValue(rs.getBinaryStream("CERTIFICATE")));
                federatedIdp.setAlias(rs.getString("ALIAS"));

                JustInTimeProvisioningConfig jitProConfig = new JustInTimeProvisioningConfig();
                if (IdPManagementConstants.IS_TRUE_VALUE.equals(rs.getString("INBOUND_PROV_ENABLED"))) {
                    jitProConfig.setProvisioningEnabled(true);
                } else {
                    jitProConfig.setProvisioningEnabled(false);
                }

                jitProConfig.setProvisioningUserStore(rs.getString("INBOUND_PROV_USER_STORE_ID"));
                federatedIdp.setJustInTimeProvisioningConfig(jitProConfig);

                String userClaimUri = rs.getString("USER_CLAIM_URI");
                String roleClaimUri = rs.getString("ROLE_CLAIM_URI");

                String defaultAuthenticatorName = rs.getString("DEFAULT_AUTHENTICATOR_NAME");
                String defaultProvisioningConnectorConfigName = rs.getString("DEFAULT_PRO_CONNECTOR_NAME");
                federatedIdp.setIdentityProviderDescription(rs.getString("DESCRIPTION"));

                // IS_FEDERATION_HUB_IDP
                if (IdPManagementConstants.IS_TRUE_VALUE.equals(rs.getString("IS_FEDERATION_HUB"))) {
                    federatedIdp.setFederationHub(true);
                } else {
                    federatedIdp.setFederationHub(false);
                }

                if (federatedIdp.getClaimConfig() == null) {
                    federatedIdp.setClaimConfig(new ClaimConfig());
                }

                // IS_LOCAL_CLAIM_DIALECT
                if (IdPManagementConstants.IS_TRUE_VALUE.equals(rs.getString("IS_LOCAL_CLAIM_DIALECT"))) {
                    federatedIdp.getClaimConfig().setLocalClaimDialect(true);
                } else {
                    federatedIdp.getClaimConfig().setLocalClaimDialect(false);
                }

                federatedIdp.setProvisioningRole(rs.getString("PROVISIONING_ROLE"));

                if (IdPManagementConstants.IS_TRUE_VALUE.equals(rs.getString("IS_ENABLED"))) {
                    federatedIdp.setEnable(true);
                } else {
                    federatedIdp.setEnable(false);
                }

                federatedIdp.setDisplayName(rs.getString("DISPLAY_NAME"));

                if (defaultAuthenticatorName != null) {
                    FederatedAuthenticatorConfig defaultAuthenticator = new FederatedAuthenticatorConfig();
                    defaultAuthenticator.setName(defaultAuthenticatorName);
                    federatedIdp.setDefaultAuthenticatorConfig(defaultAuthenticator);
                }

                if (defaultProvisioningConnectorConfigName != null) {
                    ProvisioningConnectorConfig defaultProConnector = new ProvisioningConnectorConfig();
                    defaultProConnector.setName(defaultProvisioningConnectorConfigName);
                    federatedIdp.setDefaultProvisioningConnectorConfig(defaultProConnector);
                }

                // get federated authenticators.
                federatedIdp.setFederatedAuthenticatorConfigs(getFederatedAuthenticatorConfigs(
                        dbConnection, idPName, federatedIdp, tenantId));

                if (federatedIdp.getClaimConfig().isLocalClaimDialect()) {
                    federatedIdp.setClaimConfig(getLocalIdPDefaultClaimValues(dbConnection,
                            idPName, userClaimUri, roleClaimUri, idpId, tenantId));
                } else {
                    // get claim configuration.
                    federatedIdp.setClaimConfig(getIdPClaimConfiguration(dbConnection, idPName,
                            userClaimUri, roleClaimUri, idpId, tenantId));
                }

                // get provisioning connectors.
                federatedIdp.setProvisioningConnectorConfigs(getProvisioningConnectorConfigs(
                        dbConnection, idPName, idpId, tenantId));

                // get permission and role configuration.
                federatedIdp.setPermissionAndRoleConfig(getPermissionsAndRoleConfiguration(
                        dbConnection, idPName, idpId, tenantId));

                // Get federated idp groups.
                federatedIdp.setIdPGroupConfig(getIdPGroupConfiguration(dbConnection, idpId));

                List<IdentityProviderProperty> propertyList = filterIdentityProperties(federatedIdp,
                        getIdentityPropertiesByIdpId(dbConnection, Integer.parseInt(rs.getString("ID")),
                                tenantId));

                if (IdentityApplicationConstants.RESIDENT_IDP_RESERVED_NAME.equals(idPName)) {
                    propertyList = resolveConnectorProperties(propertyList, tenantDomain);
                    fillResidentIdpProperties(federatedIdp, tenantDomain);
                }
                federatedIdp.setIdpProperties(propertyList.toArray(new IdentityProviderProperty[0]));

            }
            return federatedIdp;
        } catch (SQLException e) {
            throw new IdentityProviderManagementException("Error occurred while retrieving Identity Provider " +
                    "information for Authenticator Property : " + property + " and value : " + value, e);
        } catch (ConnectorException e) {
            throw new IdentityProviderManagementException("Error occurred while retrieving the identity connector " +
                    "configurations.", e);
        } finally {
            if (dbConnectionInitialized) {
                IdentityDatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
            } else {
                IdentityDatabaseUtil.closeAllConnections(null, rs, prepStmt);
            }
        }
    }

    /**
     * @param realmId
     * @param tenantId
     * @param tenantDomain
     * @return
     * @throws IdentityProviderManagementException
     * @throws SQLException
     */
    public IdentityProvider getIdPByRealmId(String realmId, int tenantId, String tenantDomain)
            throws IdentityProviderManagementException {

        Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false);
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String idPName = null;

        try {
            String sqlStmt = IdPManagementConstants.SQLQueries.GET_IDP_NAME_BY_REALM_ID_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, tenantId);
            prepStmt.setInt(2, MultitenantConstants.SUPER_TENANT_ID);
            prepStmt.setString(3, realmId);
            rs = prepStmt.executeQuery();
            if (rs.next()) {
                idPName = rs.getString("NAME");
            }
            return getIdPByName(dbConnection, idPName, tenantId, tenantDomain);
        } catch (SQLException e) {
            throw new IdentityProviderManagementException("Error while retreiving Identity Provider by realm " +
                    realmId, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
        }

    }

    /**
     * Get the enabled IDP of the given realm id.
     *
     * @param realmId       Realm ID of the required identity provider.
     * @param tenantId      Tenant ID of the required identity provider.
     * @param tenantDomain  Tenant domain of the required identity provider.
     * @return              Enabled identity provider of the given realm id.
     * @throws IdentityProviderManagementException Error when getting the identity provider.
     * @throws SQLException                        Error when executing SQL query.
     */
    public IdentityProvider getEnabledIdPByRealmId(String realmId, int tenantId, String tenantDomain)
            throws IdentityProviderManagementException {

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false)) {
            String idPName = null;
            String sqlStmt = IdPManagementConstants.SQLQueries.GET_ENABLED_IDP_NAME_BY_REALM_ID_SQL;
            PreparedStatement prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, tenantId);
            prepStmt.setInt(2, MultitenantConstants.SUPER_TENANT_ID);
            prepStmt.setString(3, realmId);
            prepStmt.setString(4, IdPManagementConstants.IS_TRUE_VALUE);
            ResultSet rs = prepStmt.executeQuery();
            if (rs.next()) {
                idPName = rs.getString(IdPManagementConstants.NAME);
            }
            return getIdPByName(dbConnection, idPName, tenantId, tenantDomain);
        } catch (SQLException e) {
            throw new IdentityProviderManagementException("Error while retrieving Identity Provider by realm " +
                    realmId, e);
        }
    }

    /**
     * @param identityProvider
     * @param tenantId
     * @throws IdentityProviderManagementException
     */
    public void addIdP(IdentityProvider identityProvider, int tenantId)
            throws IdentityProviderManagementException {

        addIdPWithResourceId(identityProvider, tenantId);
    }

    /**
     * Add IDP.
     *
     * @param identityProvider  Identity provider information.
     * @param tenantId          Tenant ID.
     * @return Resource ID of created IDP.
     * @throws IdentityProviderManagementException
     */
    public String addIdPWithResourceId(IdentityProvider identityProvider, int tenantId)
            throws IdentityProviderManagementException {

        Connection dbConnection = IdentityDatabaseUtil.getDBConnection(true);
        PreparedStatement prepStmt = null;
        try {
            if (identityProvider.isPrimary()) {
                // this is going to be the primary. Switch off any other primary set up in the
                // system.
                switchOffPrimary(dbConnection, tenantId);
            }

            // SP_TENANT_ID, SP_IDP_NAME, SP_IDP_PRIMARY, SP_IDP_HOME_REALM_ID, SP_IDP_CERTIFICATE,
            // SP_IDP_TOKEN_EP_ALIAS,
            // SP_IDP_INBOUND_PROVISIONING_ENABLED,SP_IDP_INBOUND_PROVISIONING_USER_STORE_ID,
            // SP_IDP_USER_CLAIM_URI,SP_IDP_ROLE_CLAIM_URI,SP_IDP_DEFAULT_AUTHENTICATOR_NAME,
            // SP_IDP_DEFAULT_PRO_CONNECTOR_NAME
            String sqlStmt = IdPManagementConstants.SQLQueries.ADD_IDP_SQL;

            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, tenantId);
            prepStmt.setString(2, identityProvider.getIdentityProviderName());

            if (identityProvider.isPrimary()) {
                prepStmt.setString(3, IdPManagementConstants.IS_TRUE_VALUE);
            } else {
                prepStmt.setString(3, IdPManagementConstants.IS_FALSE_VALUE);
            }

            prepStmt.setString(4, identityProvider.getHomeRealmId());

            if (ArrayUtils.isNotEmpty(identityProvider.getCertificateInfoArray())) {
                try {
                    // Check whether certificate decoding and certificate generation fails or not.
                    IdentityApplicationManagementUtil.getCertDataArray(identityProvider.getCertificateInfoArray());
                } catch (CertificateException ex) {
                    throw new IdentityProviderManagementClientException("Malformed Public Certificate file has been " +
                            "provided.", ex);
                }
            }
            JSONArray certificateInfoJsonArray = new JSONArray(identityProvider.getCertificateInfoArray());
            setBlobValue(certificateInfoJsonArray.toString(), prepStmt, 5);

            prepStmt.setString(6, identityProvider.getAlias());


            if (identityProvider.getJustInTimeProvisioningConfig() != null) {
                // just in time provisioning enabled for this identity provider.
                // based on the authentication response from the identity provider - user will be
                // provisioned locally.
                if (identityProvider.getJustInTimeProvisioningConfig().isProvisioningEnabled()) {
                    prepStmt.setString(7, IdPManagementConstants.IS_TRUE_VALUE);
                } else {
                    prepStmt.setString(7, IdPManagementConstants.IS_FALSE_VALUE);
                }
                // user will be provisioned to the configured user store.
                prepStmt.setString(8, identityProvider.getJustInTimeProvisioningConfig().getProvisioningUserStore());
            } else {
                prepStmt.setString(7, IdPManagementConstants.IS_FALSE_VALUE);
                prepStmt.setString(8, null);
            }

            if (identityProvider.getClaimConfig() != null) {
                // this is how we find the subject name from the authentication response.
                // this claim URI is in identity provider's own dialect.
                prepStmt.setString(9, identityProvider.getClaimConfig().getUserClaimURI());
                // this is how we find the role name from the authentication response.
                // this claim URI is in identity provider's own dialect.
                prepStmt.setString(10, identityProvider.getClaimConfig().getRoleClaimURI());
            } else {
                prepStmt.setString(9, null);
                prepStmt.setString(10, null);
            }

            if (identityProvider.getDefaultAuthenticatorConfig() != null) {
                prepStmt.setString(11, identityProvider.getDefaultAuthenticatorConfig().getName());
            } else {
                prepStmt.setString(11, null);
            }

            if (identityProvider.getDefaultProvisioningConnectorConfig() != null) {
                prepStmt.setString(12, identityProvider.getDefaultProvisioningConnectorConfig().getName());
            } else {
                prepStmt.setString(12, null);
            }

            prepStmt.setString(13, identityProvider.getIdentityProviderDescription());

            if (identityProvider.isFederationHub()) {
                prepStmt.setString(14, IdPManagementConstants.IS_TRUE_VALUE);
            } else {
                prepStmt.setString(14, IdPManagementConstants.IS_FALSE_VALUE);
            }

            if (identityProvider.getClaimConfig() != null
                    && identityProvider.getClaimConfig().isLocalClaimDialect()) {
                prepStmt.setString(15, IdPManagementConstants.IS_TRUE_VALUE);
            } else {
                prepStmt.setString(15, IdPManagementConstants.IS_FALSE_VALUE);
            }

            prepStmt.setString(16, identityProvider.getProvisioningRole());

            // enabled by default
            prepStmt.setString(17, IdPManagementConstants.IS_TRUE_VALUE);

            prepStmt.setString(18, identityProvider.getDisplayName());

            prepStmt.setString(19, identityProvider.getImageUrl());

            String resourceId = UUID.randomUUID().toString();
            prepStmt.setString(20, resourceId);
            prepStmt.executeUpdate();
            prepStmt.clearParameters();

            // get newly added Identity provider.
            IdentityProvider createdIDP = getIDPbyResourceId(dbConnection, resourceId, tenantId,
                    IdentityTenantUtil.getTenantDomain(tenantId));
            // get the id of the just added identity provider.
            int idPId = Integer.parseInt(createdIDP.getId());

            if (idPId <= 0) {
                String msg = "Error adding Identity Provider for tenant " + tenantId;
                throw new IdentityProviderManagementException(msg);
            }

            // add provisioning connectors.
            if (identityProvider.getProvisioningConnectorConfigs() != null
                    && identityProvider.getProvisioningConnectorConfigs().length > 0) {
                addProvisioningConnectorConfigs(identityProvider.getProvisioningConnectorConfigs(),
                        dbConnection, idPId, tenantId);
            }

            identityProvider.setId(createdIDP.getId());
            // Add system federated authenticator secret properties to IDN_SECRET table.
            if (identityProvider.getFederatedAuthenticatorConfigs().length > 0 &&
                    identityProvider.getFederatedAuthenticatorConfigs()[0].getDefinedByType() == DefinedByType.SYSTEM) {
                identityProvider = idpSecretsProcessorService.encryptAssociatedSecrets(identityProvider);
            }

            // add federated authenticators.
            addFederatedAuthenticatorConfigs(identityProvider.getFederatedAuthenticatorConfigs(),
                    dbConnection, idPId, tenantId);

            // add role configuration.
            if (identityProvider.getPermissionAndRoleConfig() != null) {
                if (identityProvider.getPermissionAndRoleConfig().getIdpRoles() != null
                        && identityProvider.getPermissionAndRoleConfig().getIdpRoles().length > 0) {
                    // add roles.
                    addIdPRoles(dbConnection, idPId, tenantId, identityProvider
                            .getPermissionAndRoleConfig().getIdpRoles());

                    if (identityProvider.getPermissionAndRoleConfig().getRoleMappings() != null
                            && identityProvider.getPermissionAndRoleConfig().getRoleMappings().length > 0) {
                        // add role mappings.
                        addIdPRoleMappings(dbConnection, idPId, tenantId, identityProvider
                                .getPermissionAndRoleConfig().getRoleMappings());
                    }
                }
            }

            // Add idp groups configuration.
            if (identityProvider.getIdPGroupConfig() != null) {
                addIdPGroups(dbConnection, idPId, tenantId, identityProvider.getIdPGroupConfig(), false);
            }

            // add claim configuration.
            if (identityProvider.getClaimConfig() != null
                    && identityProvider.getClaimConfig().getClaimMappings() != null
                    && identityProvider.getClaimConfig().getClaimMappings().length > 0) {
                if (identityProvider.getClaimConfig().isLocalClaimDialect()) {
                    // identity provider is using local claim dialect - we do not need to add
                    // claims.
                    addDefaultClaimValuesForLocalIdP(dbConnection, idPId, tenantId,
                            identityProvider.getClaimConfig().getClaimMappings());
                } else {
                    addIdPClaims(dbConnection, idPId, tenantId, identityProvider.getClaimConfig()
                            .getIdpClaims());

                    addIdPClaimMappings(dbConnection, idPId, tenantId, identityProvider
                            .getClaimConfig().getClaimMappings());
                }

            }

            IdentityProviderProperty[] idpProperties = identityProvider.getIdpProperties();
            if (IdentityApplicationConstants.RESIDENT_IDP_RESERVED_NAME
                    .equals(identityProvider.getIdentityProviderName())) {
                idpProperties = filterConnectorProperties(
                        idpProperties, IdentityTenantUtil.getTenantDomain(tenantId))
                        .toArray(new IdentityProviderProperty[0]);
            }
            List<IdentityProviderProperty> identityProviderProperties = getCombinedProperties(identityProvider
                    .getJustInTimeProvisioningConfig(), identityProvider.getFederatedAssociationConfig(),
                    idpProperties);

            if (!identityProviderProperties.stream().anyMatch(identityProviderProperty ->
                    IS_TRUSTED_TOKEN_ISSUER.equals(identityProviderProperty.getName()))) {
                IdentityProviderProperty trustedIdpProperty = new IdentityProviderProperty();
                trustedIdpProperty.setName(IS_TRUSTED_TOKEN_ISSUER);
                trustedIdpProperty.setValue(String.valueOf(identityProvider.isTrustedTokenIssuer()));
                identityProviderProperties.add(trustedIdpProperty);
            }
            addTemplateIdProperty(identityProviderProperties, identityProvider);
            addIdentityProviderProperties(dbConnection, idPId, identityProviderProperties, tenantId);
            IdentityDatabaseUtil.commitTransaction(dbConnection);
            return resourceId;
        } catch (IOException e) {
            throw new IdentityProviderManagementException("An error occurred while processing content stream.", e);
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(dbConnection);
            throw new IdentityProviderManagementException("Error occurred while adding Identity Provider for tenant "
                    + tenantId, e);
        } catch (ConnectorException e) {
            throw new IdentityProviderManagementException("An error occurred while filtering IDP properties.", e);
        } catch (SecretManagementException e) {
            throw new IdentityProviderManagementException("An error occurred while storing encrypted IDP secrets of " +
                    "Identity provider : " + identityProvider.getIdentityProviderName() + " in tenant : "
                    + IdentityTenantUtil.getTenantDomain(tenantId), e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(dbConnection, null, prepStmt);
        }
    }

    /**
     * To get the combined list of identity properties with IDP meta data properties as well as Just in time configs.
     *
     * @param justInTimeProvisioningConfig JustInTimeProvisioningConfig.
     * @param idpProperties                IDP Properties.
     * @return combined list of identity properties.
     */
    private List<IdentityProviderProperty> getCombinedProperties(JustInTimeProvisioningConfig
                                                                         justInTimeProvisioningConfig,
                                                                 FederatedAssociationConfig federatedAssociationConfig,
                                                                 IdentityProviderProperty[] idpProperties)
            throws IdentityProviderManagementException {

        List<IdentityProviderProperty> identityProviderProperties = new ArrayList<>();
        if (ArrayUtils.isNotEmpty(idpProperties)) {
            identityProviderProperties = new ArrayList<>(Arrays.asList(idpProperties));
        }
        IdentityProviderProperty passwordProvisioningProperty = new IdentityProviderProperty();
        passwordProvisioningProperty.setName(IdPManagementConstants.PASSWORD_PROVISIONING_ENABLED);
        passwordProvisioningProperty.setValue("false");

        IdentityProviderProperty modifyUserNameProperty = new IdentityProviderProperty();
        modifyUserNameProperty.setName(IdPManagementConstants.MODIFY_USERNAME_ENABLED);
        modifyUserNameProperty.setValue("false");

        IdentityProviderProperty promptConsentProperty = new IdentityProviderProperty();
        promptConsentProperty.setName(IdPManagementConstants.PROMPT_CONSENT_ENABLED);
        promptConsentProperty.setValue("false");

        IdentityProviderProperty associateLocalUser = new IdentityProviderProperty();
        associateLocalUser.setName(IdPManagementConstants.ASSOCIATE_LOCAL_USER_ENABLED);
        associateLocalUser.setValue("false");

        IdentityProviderProperty skipJITOnAttributeLookupFailure = new IdentityProviderProperty();
        skipJITOnAttributeLookupFailure.setName(IdPManagementConstants.SKIP_JIT_ON_ATTR_ACCOUNT_LOOKUP_FAILURE);
        skipJITOnAttributeLookupFailure.setValue("false");

        IdentityProviderProperty primaryAccountLookupAttributeMapping = new IdentityProviderProperty();
        primaryAccountLookupAttributeMapping.setName(IdPManagementConstants.PRIMARY_ACCOUNT_LOOKUP_ATTRIBUTE_MAPPING);
        primaryAccountLookupAttributeMapping.setValue("");

        IdentityProviderProperty secondaryAccountLookupAttributeMapping = new IdentityProviderProperty();
        secondaryAccountLookupAttributeMapping.setName(
                IdPManagementConstants.SECONDARY_ACCOUNT_LOOKUP_ATTRIBUTE_MAPPING);
        secondaryAccountLookupAttributeMapping.setValue("");

        IdentityProviderProperty federatedAssociationProperty = new IdentityProviderProperty();
        federatedAssociationProperty.setName(IdPManagementConstants.FEDERATED_ASSOCIATION_ENABLED);
        federatedAssociationProperty.setValue(IdPManagementConstants.FEDERATED_ASSOCIATION_ENABLED_DEFAULT_VALUE);

        IdentityProviderProperty lookupAttribute = null;

        IdentityProviderProperty attributeSyncMethod = new IdentityProviderProperty();
        attributeSyncMethod.setName(IdPManagementConstants.SYNC_ATTRIBUTE_METHOD);
        attributeSyncMethod.setValue(IdPManagementConstants.DEFAULT_SYNC_ATTRIBUTE);
        if (IdPManagementUtil.isPreserveLocallyAddedClaims()) {
            attributeSyncMethod.setValue(IdPManagementConstants.PRESERVE_LOCAL_ATTRIBUTE_SYNC);
        }

        if (justInTimeProvisioningConfig != null) {
            passwordProvisioningProperty
                    .setValue(String.valueOf(justInTimeProvisioningConfig.isPasswordProvisioningEnabled()));
            modifyUserNameProperty.setValue(String.valueOf(justInTimeProvisioningConfig.isModifyUserNameAllowed()));
            promptConsentProperty.setValue(String.valueOf(justInTimeProvisioningConfig.isPromptConsent()));
            associateLocalUser.setValue(String.valueOf(justInTimeProvisioningConfig.isAssociateLocalUserEnabled()));
            skipJITOnAttributeLookupFailure.setValue(
                    String.valueOf(justInTimeProvisioningConfig.isSkipJITOnAttrAccLookUpFailureEnabled()));
            fillAccountLookUpAttributesIdpProperties(justInTimeProvisioningConfig, primaryAccountLookupAttributeMapping,
                    secondaryAccountLookupAttributeMapping);
            attributeSyncMethod.setValue(justInTimeProvisioningConfig.getAttributeSyncMethod());
        }

        if (federatedAssociationConfig != null && federatedAssociationConfig.isEnabled()) {
            federatedAssociationProperty.setValue(String.valueOf(federatedAssociationConfig.isEnabled()));
            lookupAttribute = new IdentityProviderProperty();
            lookupAttribute.setName(IdPManagementConstants.LOOKUP_ATTRIBUTES);
            lookupAttribute.setValue(StringUtils.join(federatedAssociationConfig.getLookupAttributes(), ","));
        }

        identityProviderProperties.add(passwordProvisioningProperty);
        identityProviderProperties.add(modifyUserNameProperty);
        identityProviderProperties.add(promptConsentProperty);
        identityProviderProperties.add(associateLocalUser);
        identityProviderProperties.add(skipJITOnAttributeLookupFailure);
        identityProviderProperties.add(primaryAccountLookupAttributeMapping);
        identityProviderProperties.add(secondaryAccountLookupAttributeMapping);
        identityProviderProperties.add(attributeSyncMethod);
        identityProviderProperties.add(federatedAssociationProperty);
        if (lookupAttribute != null) {
            identityProviderProperties.add(lookupAttribute);
        }

        return identityProviderProperties;
    }

    /**
     * @param newIdentityProvider
     * @param currentIdentityProvider
     * @param tenantId
     * @throws IdentityProviderManagementException
     */
    public void updateIdP(IdentityProvider newIdentityProvider,
                          IdentityProvider currentIdentityProvider, int tenantId)
            throws IdentityProviderManagementException {

        updateIdPWithResourceId(null, newIdentityProvider, currentIdentityProvider, tenantId);
    }

    /**
     *
     * @param resourceId
     * @param newIdentityProvider
     * @param currentIdentityProvider
     * @param tenantId
     * @throws IdentityProviderManagementException
     */
    public void updateIdPWithResourceId(String resourceId, IdentityProvider
            newIdentityProvider, IdentityProvider currentIdentityProvider, int tenantId)
            throws IdentityProviderManagementException {

        if (StringUtils.isBlank(resourceId)) {
            resourceId = currentIdentityProvider.getResourceId();
        }
        Connection dbConnection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt1 = null;
        PreparedStatement prepStmt2 = null;
        ResultSet rs = null;
        try {

            int idPId = getIdentityProviderIdByName(dbConnection,
                    currentIdentityProvider.getIdentityProviderName(), tenantId);

            if (idPId <= 0) {
                String msg = "Trying to update non-existent Identity Provider for tenant "
                        + tenantId;
                throw new IdentityProviderManagementException(msg);
            }

            // SP_IDP_NAME=?, SP_IDP_PRIMARY=?,SP_IDP_HOME_REALM_ID=?, SP_IDP_CERTIFICATE=?,
            // SP_IDP_TOKEN_EP_ALIAS=?,
            // SP_IDP_INBOUND_PROVISIONING_ENABLED=?,SP_IDP_INBOUND_PROVISIONING_USER_STORE_ID=?,
            // SP_IDP_USER_CLAIM_URI=?,
            // SP_IDP_ROLE_CLAIM_URI=?,SP_IDP_DEFAULT_AUTHENTICATOR_NAME=?,SP_IDP_DEFAULT_PRO_CONNECTOR_NAME=?
            String sqlStmt = IdPManagementConstants.SQLQueries.UPDATE_IDP_BY_RESOURCE_ID_SQL;
            if (StringUtils.isBlank(resourceId)) {
                sqlStmt = IdPManagementConstants.SQLQueries.UPDATE_IDP_SQL;
            }
            prepStmt1 = dbConnection.prepareStatement(sqlStmt);

            prepStmt1.setString(1, newIdentityProvider.getIdentityProviderName());

            if (newIdentityProvider.isPrimary()) {
                prepStmt1.setString(2, IdPManagementConstants.IS_TRUE_VALUE);
            } else {
                prepStmt1.setString(2, IdPManagementConstants.IS_FALSE_VALUE);
            }

            prepStmt1.setString(3, newIdentityProvider.getHomeRealmId());

            if (ArrayUtils.isNotEmpty(newIdentityProvider.getCertificateInfoArray())) {
                try {
                    IdentityApplicationManagementUtil.getCertDataArray(newIdentityProvider.getCertificateInfoArray());
                } catch (CertificateException ex) {
                    throw new IdentityProviderManagementClientException("Malformed Public Certificate file has been " +
                            "provided.", ex);
                }
            }
            JSONArray certificateInfoJsonArray = new JSONArray(newIdentityProvider.getCertificateInfoArray());
            setBlobValue(certificateInfoJsonArray.toString(), prepStmt1, 4);

            prepStmt1.setString(5, newIdentityProvider.getAlias());

            if (newIdentityProvider.getJustInTimeProvisioningConfig() != null
                    && newIdentityProvider.getJustInTimeProvisioningConfig()
                    .isProvisioningEnabled()) {
                prepStmt1.setString(6, IdPManagementConstants.IS_TRUE_VALUE);
            } else {
                prepStmt1.setString(6, IdPManagementConstants.IS_FALSE_VALUE);
            }
            // user will be provisioned to the configured user store.
            if (newIdentityProvider.getJustInTimeProvisioningConfig() != null) {
                prepStmt1.setString(7, newIdentityProvider.getJustInTimeProvisioningConfig().getProvisioningUserStore());
            } else {
                prepStmt1.setString(7, null);
            }

            if (newIdentityProvider.getClaimConfig() != null) {
                prepStmt1.setString(8, newIdentityProvider.getClaimConfig().getUserClaimURI());
                prepStmt1.setString(9, newIdentityProvider.getClaimConfig().getRoleClaimURI());
            } else {
                prepStmt1.setString(8, null);
                prepStmt1.setString(9, null);
            }

            // update the default authenticator
            if (newIdentityProvider.getDefaultAuthenticatorConfig() != null
                    && newIdentityProvider.getDefaultAuthenticatorConfig().getName() != null) {
                prepStmt1.setString(10, newIdentityProvider.getDefaultAuthenticatorConfig().getName());
            } else {
                // its not a must to have a default authenticator.
                prepStmt1.setString(10, null);
            }

            // update the default provisioning connector.
            if (newIdentityProvider.getDefaultProvisioningConnectorConfig() != null
                    && newIdentityProvider.getDefaultProvisioningConnectorConfig().getName() != null) {
                prepStmt1.setString(11, newIdentityProvider.getDefaultProvisioningConnectorConfig().getName());
            } else {
                // its not a must to have a default provisioning connector..
                prepStmt1.setString(11, null);
            }

            prepStmt1.setString(12, newIdentityProvider.getIdentityProviderDescription());

            if (newIdentityProvider.isFederationHub()) {
                prepStmt1.setString(13, IdPManagementConstants.IS_TRUE_VALUE);
            } else {
                prepStmt1.setString(13, IdPManagementConstants.IS_FALSE_VALUE);
            }

            if (newIdentityProvider.getClaimConfig() != null
                    && newIdentityProvider.getClaimConfig().isLocalClaimDialect()) {
                prepStmt1.setString(14, IdPManagementConstants.IS_TRUE_VALUE);
            } else {
                prepStmt1.setString(14, IdPManagementConstants.IS_FALSE_VALUE);
            }

            prepStmt1.setString(15, newIdentityProvider.getProvisioningRole());

            if (newIdentityProvider.isEnable()) {
                prepStmt1.setString(16, IdPManagementConstants.IS_TRUE_VALUE);
            } else {
                prepStmt1.setString(16, IdPManagementConstants.IS_FALSE_VALUE);
            }

            prepStmt1.setString(17, newIdentityProvider.getDisplayName());

            if (StringUtils.isBlank(resourceId)) {
                prepStmt1.setInt(18, tenantId);
                prepStmt1.setString(19, currentIdentityProvider.getIdentityProviderName());
            } else {
                prepStmt1.setString(18, newIdentityProvider.getImageUrl());
                prepStmt1.setString(19, resourceId);
            }

            prepStmt1.executeUpdate();

            sqlStmt = IdPManagementConstants.SQLQueries.GET_IDP_BY_RESOURCE_ID_SQL;
            if (StringUtils.isBlank(resourceId)) {
                sqlStmt = IdPManagementConstants.SQLQueries.GET_IDP_BY_NAME_SQL;
            }
            prepStmt2 = dbConnection.prepareStatement(sqlStmt);
            prepStmt2.setInt(1, tenantId);
            prepStmt2.setInt(2, MultitenantConstants.SUPER_TENANT_ID);
            if (StringUtils.isBlank(resourceId)) {
                prepStmt2.setString(3, newIdentityProvider.getIdentityProviderName());
            } else {
                prepStmt2.setString(3, resourceId);
            }
            rs = prepStmt2.executeQuery();

            if (rs.next()) {

                // id of the updated identity provider.
                int idpId = rs.getInt("ID");

                boolean isResidentIdP = IdentityApplicationConstants.RESIDENT_IDP_RESERVED_NAME
                        .equals(newIdentityProvider.getIdentityProviderName());

                newIdentityProvider.setId(Integer.toString(idpId));
                // Update secrets of system federated authenticator config in IDN_SECRET table.
                if (newIdentityProvider.getFederatedAuthenticatorConfigs().length > 0 && newIdentityProvider
                        .getFederatedAuthenticatorConfigs()[0].getDefinedByType() == DefinedByType.SYSTEM) {
                    newIdentityProvider = idpSecretsProcessorService.encryptAssociatedSecrets(newIdentityProvider);
                }

                // update federated authenticators.
                updateFederatedAuthenticatorConfigs(
                        newIdentityProvider.getFederatedAuthenticatorConfigs(),
                        currentIdentityProvider.getFederatedAuthenticatorConfigs(), dbConnection,
                        idpId, tenantId, isResidentIdP);

                // update claim configuration.
                updateClaimConfiguration(dbConnection, idpId, tenantId,
                        newIdentityProvider.getClaimConfig());

                // update role configuration.
                updateRoleConfiguration(dbConnection, idpId, tenantId,
                        newIdentityProvider.getPermissionAndRoleConfig());

                // Update idp group configuration.
                updateIdPGroupConfiguration(dbConnection, idpId, tenantId,
                        newIdentityProvider.getIdPGroupConfig());

                // // update provisioning connectors.
                updateProvisioningConnectorConfigs(
                        newIdentityProvider.getProvisioningConnectorConfigs(), dbConnection, idpId,
                        tenantId);

                // This will only contain updated/new property values if the IDP is resident IDP in a sub-org.
                IdentityProviderProperty[] idpProperties = newIdentityProvider.getIdpProperties();
                if (isResidentIdP) {
                    if (OrganizationManagementUtil.isOrganization(tenantId)) {
                        // Get existing properties from database.
                        List<IdentityProviderProperty> existingpropertyList = getIdentityPropertiesByIdpId(dbConnection,
                                idpId, tenantId);
                        existingpropertyList.removeIf(this::isExcludedJITProvisioningProperty);

                        // Add the values from the DB to idpProperties if not already present.
                        for (IdentityProviderProperty property : existingpropertyList) {
                            if (Arrays.stream(idpProperties).noneMatch(prop ->
                                    prop.getName().equals(property.getName()))) {
                                idpProperties = (IdentityProviderProperty[]) ArrayUtils.add(idpProperties, property);
                            }
                        }
                    } else {
                        idpProperties = filterConnectorProperties(
                                idpProperties,
                                IdentityTenantUtil.getTenantDomain(tenantId)).toArray(new IdentityProviderProperty[0]);
                    }
                }
                List<IdentityProviderProperty> identityProviderProperties = getCombinedProperties
                        (newIdentityProvider.getJustInTimeProvisioningConfig(),
                                newIdentityProvider.getFederatedAssociationConfig(), idpProperties);

                boolean hasTrustedTokenIssuerProperty = false;
                for (IdentityProviderProperty property : identityProviderProperties) {
                    if (property.getName().equals(IS_TRUSTED_TOKEN_ISSUER)) {
                        property.setValue(String.valueOf(newIdentityProvider.isTrustedTokenIssuer()));
                        hasTrustedTokenIssuerProperty = true;
                        break;
                    }
                }

                if (!hasTrustedTokenIssuerProperty) {
                    IdentityProviderProperty property = new IdentityProviderProperty();
                    property.setName(IS_TRUSTED_TOKEN_ISSUER);
                    property.setValue(String.valueOf(newIdentityProvider.isTrustedTokenIssuer()));
                    identityProviderProperties.add(property);
                }

                updateIdentityProviderProperties(dbConnection, idpId, identityProviderProperties, tenantId);
            }
            IdentityDatabaseUtil.commitTransaction(dbConnection);
        } catch (IOException e) {
            throw new IdentityProviderManagementException("An error occurred while processing content stream.", e);
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(dbConnection);
            throw new IdentityProviderManagementException("Error occurred while updating Identity Provider " +
                    "information  for tenant " + tenantId, e);
        } catch (ConnectorException e) {
            throw new IdentityProviderManagementException("An error occurred while filtering IDP properties.", e);
        } catch (SecretManagementException e) {
            throw new IdentityProviderManagementException("An error occurred while updating the secrets of the " +
                    "identity provider : " + currentIdentityProvider.getIdentityProviderName() + " in tenant : " +
                    IdentityTenantUtil.getTenantDomain(tenantId), e);
        } catch (OrganizationManagementException e) {
            throw new IdentityProviderManagementException(String.format("An error occurred while checking if %s is " +
                    "an organization", IdentityTenantUtil.getTenantDomain(tenantId)), e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt1);
            IdentityDatabaseUtil.closeStatement(prepStmt2);
        }
    }

    public boolean isIdpReferredBySP(String idPName, int tenantId)
            throws IdentityProviderManagementException {

        boolean isReffered = false;
        Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false);
        PreparedStatement prepStmtFedIdp = null;
        ResultSet rsFedIdp = null;
        PreparedStatement prepStmtProvIdp = null;
        ResultSet rsProvIdp = null;

        try {
            String sqlStmt = IdPManagementConstants.SQLQueries.GET_SP_FEDERATED_IDP_REFS;
            prepStmtFedIdp = dbConnection.prepareStatement(sqlStmt);
            prepStmtFedIdp.setInt(1, tenantId);
            prepStmtFedIdp.setInt(2, MultitenantConstants.SUPER_TENANT_ID);
            prepStmtFedIdp.setString(3, idPName);
            rsFedIdp = prepStmtFedIdp.executeQuery();
            if (rsFedIdp.next()) {
                isReffered = rsFedIdp.getInt(1) > 0;
            }
            if (!isReffered) {
                sqlStmt = IdPManagementConstants.SQLQueries.GET_SP_PROVISIONING_CONNECTOR_REFS;
                prepStmtProvIdp = dbConnection.prepareStatement(sqlStmt);
                prepStmtProvIdp.setInt(1, tenantId);
                prepStmtProvIdp.setString(2, idPName);
                rsProvIdp = prepStmtProvIdp.executeQuery();
                if (rsProvIdp.next()) {
                    isReffered = rsProvIdp.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new IdentityProviderManagementException("Error occurred while searching for IDP references in SP ",
                    e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(null, rsProvIdp, prepStmtProvIdp);
            IdentityDatabaseUtil.closeAllConnections(dbConnection, rsFedIdp, prepStmtFedIdp);
        }
        return isReffered;
    }

    /**
     * Check whether the specified IDP authenticator is associated with any service providers.
     *
     * @param idpName           Name of the IDP.
     * @param authenticatorName Name of the authenticator.
     * @param tenantId          ID of the tenant.
     * @return Whether the specified IDP authenticator is referenced by any service providers.
     * @throws IdentityProviderManagementException Error when checking IDP authenticator associations.
     */
    public boolean isAuthenticatorReferredBySP(String idpName, String authenticatorName, int tenantId)
            throws IdentityProviderManagementException {

        boolean isReferred = false;
        Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false);
        PreparedStatement prepStmtFedAuth = null;
        ResultSet rsFedAuth = null;

        try {
            prepStmtFedAuth = dbConnection.prepareStatement(
                    IdPManagementConstants.SQLQueries.GET_SP_FEDERATED_IDP_AUTHENTICATOR_REF);
            prepStmtFedAuth.setInt(1, tenantId);
            prepStmtFedAuth.setInt(2, MultitenantConstants.SUPER_TENANT_ID);
            prepStmtFedAuth.setString(3, idpName);
            prepStmtFedAuth.setString(4, authenticatorName);
            rsFedAuth = prepStmtFedAuth.executeQuery();
            if (rsFedAuth.next()) {
                isReferred = rsFedAuth.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new IdentityProviderManagementException(
                    "Error occurred while searching for IDP Authenticator references in SP ",
                    e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(dbConnection, rsFedAuth, prepStmtFedAuth);
        }

        return isReferred;
    }

    /**
     * Check whether the specified IDP outbound connector is associated with any service providers.
     *
     * @param idpName       Name of the IDP.
     * @param connectorName Name of the outbound connector.
     * @param tenantId      ID of the tenant.
     * @return Whether the specified IDP outbound connector is referenced by any service providers.
     * @throws IdentityProviderManagementException Error when checking IDP outbound connector associations.
     */
    public boolean isOutboundConnectorReferredBySP(String idpName, String connectorName, int tenantId)
            throws IdentityProviderManagementException {

        boolean isReferred = false;
        Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false);
        PreparedStatement prepStmt = null;
        ResultSet resultSet = null;

        try {
            prepStmt = dbConnection.prepareStatement(
                    IdPManagementConstants.SQLQueries.GET_SP_PROVISIONING_CONNECTOR_IDP_REFS);
            prepStmt.setInt(1, tenantId);
            prepStmt.setString(2, idpName);
            prepStmt.setString(3, connectorName);
            resultSet = prepStmt.executeQuery();
            if (resultSet.next()) {
                isReferred = resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new IdentityProviderManagementException(
                    "Error occurred while searching for IDP outbound connector references in SP ",
                    e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(dbConnection, resultSet, prepStmt);
        }

        return isReferred;
    }

    /**
     * @param idPName
     * @param tenantId
     * @param tenantDomain
     * @throws IdentityProviderManagementException
     */
    public void deleteIdP(String idPName, int tenantId, String tenantDomain)
            throws IdentityProviderManagementException {

        Connection dbConnection = IdentityDatabaseUtil.getDBConnection();
        try {
            IdentityProvider identityProvider = getIdPByName(dbConnection, idPName, tenantId,
                    tenantDomain);
            if (identityProvider == null) {
                String msg = "Trying to delete non-existent Identity Provider: %s in tenantDomain: %s";
                throw new IdentityProviderManagementException(String.format(msg, idPName, tenantDomain));
            }
            deleteIdP(dbConnection, tenantId, idPName, null);
            IdentityDatabaseUtil.commitTransaction(dbConnection);
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(dbConnection);
            throw new IdentityProviderManagementException("Error occurred while deleting Identity Provider of tenant "
                    + tenantDomain, e);
        } finally {
            IdentityDatabaseUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Delete all IDPs of a given tenant id.
     *
     * @param tenantId Id of the tenant
     * @throws IdentityProviderManagementException
     */
    public void deleteIdPs(int tenantId) throws IdentityProviderManagementException {

        try (Connection conn = IdentityDatabaseUtil.getDBConnection(false)) {
            PreparedStatement prepStmt = conn.prepareStatement(
                    IdPManagementConstants.SQLQueries.DELETE_ALL_IDP_BY_TENANT_ID_SQL);
            prepStmt.setInt(1, tenantId);
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            throw new IdentityProviderManagementException("Error occurred while deleting Identity Providers of tenant "
                    + tenantId, e);
        }
    }

    /**
     * @param resourceId
     * @param tenantId
     * @param tenantDomain
     * @throws IdentityProviderManagementException
     */
    public void deleteIdPByResourceId(String resourceId, int tenantId, String tenantDomain)
            throws IdentityProviderManagementException {

        Connection dbConnection = IdentityDatabaseUtil.getDBConnection();
        String idPName = "";
        try {
            IdentityProvider identityProvider = getIDPbyResourceId(dbConnection, resourceId, tenantId,
                    tenantDomain);
            if (identityProvider == null) {
                String msg = "Trying to delete non-existent Identity Provider with resource ID: %s in tenantDomain: %s";
                throw new IdentityProviderManagementException(String.format(msg, resourceId, tenantDomain));
            }
            idPName = identityProvider.getIdentityProviderName();
            deleteIdP(dbConnection, tenantId, null, resourceId);
            // Delete IdP related secrets from the IDN_SECRET table.
            idpSecretsProcessorService.deleteAssociatedSecrets(identityProvider);
            IdentityDatabaseUtil.commitTransaction(dbConnection);
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(dbConnection);
            throw new IdentityProviderManagementException("Error occurred while deleting Identity Provider of tenant "
                    + tenantDomain, e);
        } catch (SecretManagementException e) {
            throw new IdentityProviderManagementException("Error while deleting IDP secrets of Identity provider : " +
                    idPName + " in tenant : " + tenantDomain, e);
        } finally {
            IdentityDatabaseUtil.closeConnection(dbConnection);
        }
    }

    public void forceDeleteIdP(String idPName,
                               int tenantId,
                               String tenantDomain) throws IdentityProviderManagementException {

        Connection dbConnection = IdentityDatabaseUtil.getDBConnection();
        try {
            IdentityProvider identityProvider = getIdPByName(dbConnection, idPName, tenantId, tenantDomain);
            if (identityProvider == null) {
                String msg = "Trying to force delete non-existent Identity Provider: %s in tenantDomain: %s";
                throw new IdentityProviderManagementException(String.format(msg, idPName, tenantDomain));
            }
            if (log.isDebugEnabled()) {
                log.debug(String.format("Deleting SP Authentication Associations for IDP:%s of tenantDomain:%s",
                        idPName, tenantDomain));
            }
            // Delete IDPs association with SPs in authentication sequences
            deleteIdpSpAuthAssociations(dbConnection, tenantId, idPName);
            // Delete IDPs association with SPs in outbound provisioning
            if (log.isDebugEnabled()) {
                log.debug(String.format("Deleting SP Provisioning Associations for IDP:%s of tenantDomain:%s",
                        idPName, tenantDomain));
            }
            deleteIdpSpProvisioningAssociations(dbConnection, tenantId, idPName);
            deleteIdP(dbConnection, tenantId, idPName, null);
            IdentityDatabaseUtil.commitTransaction(dbConnection);
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(dbConnection);
            throw new IdentityProviderManagementException(
                    String.format("Error occurred while deleting Identity Provider:%s of tenant:%s ",
                            idPName, tenantDomain), e);
        } finally {
            IdentityDatabaseUtil.closeConnection(dbConnection);
        }
    }

    public void forceDeleteIdPByResourceId(String resourceId, int tenantId, String tenantDomain) throws
            IdentityProviderManagementException {

        Connection dbConnection = IdentityDatabaseUtil.getDBConnection();
        try {
            IdentityProvider identityProvider = getIDPbyResourceId(dbConnection, resourceId, tenantId,
                    tenantDomain);
            if (identityProvider == null) {
                String msg = "Trying to force delete non-existent Identity Provider with resource ID: %s in " +
                        "tenantDomain: %s";
                throw new IdentityProviderManagementException(String.format(msg, resourceId, tenantDomain));
            }
            if (log.isDebugEnabled()) {
                log.debug(String.format("Deleting SP Authentication Associations for IDP:%s of tenantDomain:%s",
                        identityProvider.getIdentityProviderName(), tenantDomain));
            }
            // Delete IDPs association with SPs in authentication sequences
            deleteIdpSpAuthAssociations(dbConnection, tenantId, identityProvider.getIdentityProviderName());
            // Delete IDPs association with SPs in outbound provisioning
            if (log.isDebugEnabled()) {
                log.debug(String.format("Deleting SP Provisioning Associations for IDP:%s of tenantDomain:%s",
                        identityProvider.getIdentityProviderName(), tenantDomain));
            }
            deleteIdpSpProvisioningAssociations(dbConnection, tenantId, identityProvider.getIdentityProviderName());
            deleteIdP(dbConnection, tenantId, null, resourceId);
            IdentityDatabaseUtil.commitTransaction(dbConnection);
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(dbConnection);
            throw new IdentityProviderManagementException(
                    String.format("Error occurred while deleting Identity Provider with resource ID:%s of tenant:%s ",
                            resourceId, tenantDomain), e);
        } finally {
            IdentityDatabaseUtil.closeConnection(dbConnection);
        }
    }

    public void deleteTenantRole(int tenantId, String role, String tenantDomain)
            throws IdentityProviderManagementException {

        Connection dbConnection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        try {
            String sqlStmt = IdPManagementConstants.SQLQueries.DELETE_ROLE_LISTENER_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, tenantId);
            prepStmt.setString(2, role);
            prepStmt.executeUpdate();
            IdentityDatabaseUtil.commitTransaction(dbConnection);
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(dbConnection);
            throw new IdentityProviderManagementException("Error occurred while deleting tenant role " + role +
                    " of tenant " + tenantDomain, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(dbConnection, null, prepStmt);
        }
    }

    public void renameTenantRole(String newRoleName, String oldRoleName, int tenantId,
                                 String tenantDomain) throws IdentityProviderManagementException {

        Connection dbConnection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        try {
            String sqlStmt = IdPManagementConstants.SQLQueries.RENAME_ROLE_LISTENER_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setString(1, newRoleName);
            prepStmt.setInt(2, tenantId);
            prepStmt.setString(3, oldRoleName);
            prepStmt.executeUpdate();
            IdentityDatabaseUtil.commitTransaction(dbConnection);
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(dbConnection);
            throw new IdentityProviderManagementException("Error occurred while renaming tenant role " + oldRoleName
                    + " to "
                    + newRoleName + " of tenant " + tenantDomain, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(dbConnection, null, prepStmt);
        }
    }

    private boolean isExcludedJITProvisioningProperty(IdentityProviderProperty idpProperty) {

        return IdPManagementConstants.MODIFY_USERNAME_ENABLED.equals(idpProperty.getName()) ||
                IdPManagementConstants.PASSWORD_PROVISIONING_ENABLED.equals(idpProperty.getName()) ||
                IdPManagementConstants.PROMPT_CONSENT_ENABLED.equals(idpProperty.getName()) ||
                IdPManagementConstants.ASSOCIATE_LOCAL_USER_ENABLED.equals(idpProperty.getName()) ||
                IdPManagementConstants.SKIP_JIT_ON_ATTR_ACCOUNT_LOOKUP_FAILURE.equals(idpProperty.getName()) ||
                IdPManagementConstants.PRIMARY_ACCOUNT_LOOKUP_ATTRIBUTE_MAPPING.equals(idpProperty.getName()) ||
                IdPManagementConstants.SECONDARY_ACCOUNT_LOOKUP_ATTRIBUTE_MAPPING.equals(idpProperty.getName()) ||
                IdPManagementConstants.SYNC_ATTRIBUTE_METHOD.equals(idpProperty.getName()) ||
                IdPManagementConstants.FEDERATED_ASSOCIATION_ENABLED.equals(idpProperty.getName()) ||
                IdPManagementConstants.LOOKUP_ATTRIBUTES.equals(idpProperty.getName());
    }

    /**
     * @param dbConnection
     * @param idpId
     * @throws IdentityProviderManagementException
     * @throws SQLException
     */
    private void deleteAllIdPClaims(Connection dbConnection, int idpId)
            throws IdentityProviderManagementException, SQLException {

        PreparedStatement prepStmt = null;
        try {
            String sqlStmt = IdPManagementConstants.SQLQueries.DELETE_ALL_CLAIMS_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, idpId);
            prepStmt.executeUpdate();
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
        }
    }

    /**
     * @param dbConnection
     * @param idpId
     * @param tenantId
     * @throws IdentityProviderManagementException
     * @throws SQLException
     */
    private void deleteLocalIdPClaimValues(Connection dbConnection, int idpId, int tenantId)
            throws IdentityProviderManagementException, SQLException {

        PreparedStatement prepStmt = null;
        try {
            String sqlStmt = IdPManagementConstants.SQLQueries.DELETE_LOCAL_IDP_DEFAULT_CLAIM_VALUES_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, idpId);
            prepStmt.setInt(2, tenantId);

            prepStmt.executeUpdate();
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);

        }
    }

    /**
     * @param dbConnection
     * @param idpId
     * @throws IdentityProviderManagementException
     * @throws SQLException
     */
    private void deleteAllIdPRoles(Connection dbConnection, int idpId)
            throws IdentityProviderManagementException, SQLException {

        PreparedStatement prepStmt = null;
        try {
            String sqlStmt = IdPManagementConstants.SQLQueries.DELETE_ALL_ROLES_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, idpId);
            prepStmt.executeUpdate();
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);

        }
    }

    /**
     * Delete all the idp groups associated with the identity provider.
     *
     * @param dbConnection Connection to the database.
     * @param idpId        ID of the identity provider.
     * @throws IdentityProviderManagementException Error when deleting the identity provider.
     * @throws SQLException                        SQL Error when deleting the identity provider.
     */
    private void deleteAllIdPGroups(Connection dbConnection, int idpId)
            throws IdentityProviderManagementException, SQLException {

        try (PreparedStatement prepStmt = dbConnection.prepareStatement(
                IdPManagementConstants.SQLQueries.DELETE_ALL_GROUPS_SQL)) {

            prepStmt.setInt(1, idpId);
            prepStmt.executeUpdate();
        }
    }

    /**
     * @param newClaimURI
     * @param oldClaimURI
     * @param tenantId
     * @param tenantDomain
     * @throws IdentityProviderManagementException
     */
    public void renameClaimURI(String newClaimURI, String oldClaimURI, int tenantId,
                               String tenantDomain) throws IdentityProviderManagementException {

        Connection dbConnection = IdentityDatabaseUtil.getDBConnection();
        ;
        PreparedStatement prepStmt = null;
        try {
            String sqlStmt = IdPManagementConstants.SQLQueries.RENAME_CLAIM_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setString(1, newClaimURI);
            prepStmt.setInt(2, tenantId);
            prepStmt.setString(3, oldClaimURI);
            prepStmt.executeUpdate();
            IdentityDatabaseUtil.commitTransaction(dbConnection);
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(dbConnection);
            throw new IdentityProviderManagementException("Error occurred while renaming tenant role " + oldClaimURI
                    + " to "
                    + newClaimURI + " of tenant " + tenantDomain, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(dbConnection, null, prepStmt);
        }
    }

    /**
     * @param conn
     * @param tenantId
     * @throws SQLException
     */
    private void switchOffPrimary(Connection conn, int tenantId) throws SQLException {

        PreparedStatement prepStmt = null;
        // SP_IDP_PRIMARY
        String sqlStmt = IdPManagementConstants.SQLQueries.SWITCH_IDP_PRIMARY_SQL;

        try {
            prepStmt = conn.prepareStatement(sqlStmt);
            prepStmt.setString(1, "0");
            prepStmt.setInt(2, tenantId);
            prepStmt.setString(3, "1");
            prepStmt.executeUpdate();
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
        }
    }

    private void doAppointPrimary(Connection conn, int tenantId, String tenantDomain)
            throws SQLException, IdentityProviderManagementException {

        List<IdentityProvider> tenantIdPs = getIdPs(conn, tenantId, tenantDomain);
        if (!tenantIdPs.isEmpty()) {
            PreparedStatement prepStmt = null;
            try {
                String sqlStmt = IdPManagementConstants.SQLQueries.SWITCH_IDP_PRIMARY_ON_DELETE_SQL;
                prepStmt = conn.prepareStatement(sqlStmt);
                prepStmt.setString(1, IdPManagementConstants.IS_TRUE_VALUE);
                prepStmt.setInt(2, tenantId);
                prepStmt.setString(3, tenantIdPs.get(0).getIdentityProviderName());
                prepStmt.setString(4, IdPManagementConstants.IS_FALSE_VALUE);
                prepStmt.executeUpdate();
            } finally {
                IdentityDatabaseUtil.closeStatement(prepStmt);
            }
        } else {
            String msg = "No Identity Providers registered for tenant " + tenantDomain;
            log.warn(msg);
        }
    }

    /**
     * @param conn
     * @param idPId
     * @param claims
     * @throws SQLException
     */
    private void addIdPClaims(Connection conn, int idPId, int tenantId, Claim[] claims)
            throws SQLException {

        PreparedStatement prepStmt = null;

        if (claims == null || claims.length == 0) {
            return;
        }

        try {
            // SP_IDP_ID, SP_IDP_CLAIM
            String sqlStmt = IdPManagementConstants.SQLQueries.ADD_IDP_CLAIMS_SQL;
            prepStmt = conn.prepareStatement(sqlStmt);
            for (Claim claim : claims) {
                prepStmt.setInt(1, idPId);
                prepStmt.setInt(2, tenantId);
                prepStmt.setString(3, claim.getClaimUri());
                prepStmt.addBatch();
                prepStmt.clearParameters();
            }
            prepStmt.executeBatch();
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
        }
    }

    /**
     * @param conn
     * @param idPId
     * @param tenantId
     * @param claimMappings
     * @throws SQLException
     * @throws IdentityProviderManagementException
     */
    private void addDefaultClaimValuesForLocalIdP(Connection conn, int idPId, int tenantId,
                                                  ClaimMapping[] claimMappings) throws SQLException,
            IdentityProviderManagementException {

        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String sqlStmt;

        try {

            if (claimMappings == null || claimMappings.length == 0) {
                return;
            }

            sqlStmt = IdPManagementConstants.SQLQueries.ADD_LOCAL_IDP_DEFAULT_CLAIM_VALUES_SQL;
            prepStmt = conn.prepareStatement(sqlStmt);
            for (ClaimMapping mapping : claimMappings) {
                if (mapping != null && mapping.getLocalClaim() != null
                        && mapping.getLocalClaim().getClaimUri() != null) {

                    prepStmt.setInt(1, idPId);
                    prepStmt.setString(2, mapping.getLocalClaim().getClaimUri());
                    prepStmt.setString(3, mapping.getDefaultValue());
                    prepStmt.setInt(4, tenantId);
                    if (mapping.isRequested()) {
                        prepStmt.setString(5, IdPManagementConstants.IS_TRUE_VALUE);
                    } else {
                        prepStmt.setString(5, IdPManagementConstants.IS_FALSE_VALUE);
                    }
                    prepStmt.addBatch();
                }
            }

            prepStmt.executeBatch();

        } finally {
            IdentityDatabaseUtil.closeAllConnections(null, rs, prepStmt);
        }
    }

    /**
     * @param conn
     * @param idPId
     * @param tenantId
     * @param claimMappings
     * @throws SQLException
     * @throws IdentityProviderManagementException
     */
    private void addIdPClaimMappings(Connection conn, int idPId, int tenantId,
                                     ClaimMapping[] claimMappings) throws SQLException,
            IdentityProviderManagementException {

        Map<String, Integer> claimIdMap = new HashMap<String, Integer>();
        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        try {

            if (claimMappings == null || claimMappings.length == 0) {
                return;
            }

            String sqlStmt = IdPManagementConstants.SQLQueries.GET_IDP_CLAIMS_SQL;
            prepStmt = conn.prepareStatement(sqlStmt);
            prepStmt.setInt(1, idPId);
            rs = prepStmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("ID");
                String claim = rs.getString("CLAIM");
                claimIdMap.put(claim, id);
            }

            prepStmt.clearParameters();

            if (claimIdMap.isEmpty()) {
                String message = "No Identity Provider claim URIs defined for tenant " + tenantId;
                throw new IdentityProviderManagementException(message);
            }

            sqlStmt = IdPManagementConstants.SQLQueries.ADD_IDP_CLAIM_MAPPINGS_SQL;
            prepStmt = conn.prepareStatement(sqlStmt);
            for (ClaimMapping mapping : claimMappings) {
                if (mapping != null && mapping.getRemoteClaim() != null
                        && claimIdMap.containsKey(mapping.getRemoteClaim().getClaimUri())) {

                    int idpClaimId = claimIdMap.get(mapping.getRemoteClaim().getClaimUri());
                    String localClaimURI = mapping.getLocalClaim().getClaimUri();

                    prepStmt.setInt(1, idpClaimId);
                    prepStmt.setInt(2, tenantId);
                    prepStmt.setString(3, localClaimURI);
                    prepStmt.setString(4, mapping.getDefaultValue());

                    if (mapping.isRequested()) {
                        prepStmt.setString(5, IdPManagementConstants.IS_TRUE_VALUE);
                    } else {
                        prepStmt.setString(5, IdPManagementConstants.IS_FALSE_VALUE);
                    }

                    prepStmt.addBatch();
                } else {
                    throw new IdentityProviderManagementException("Cannot find Identity Provider claim mapping for " +
                            "tenant "
                            + tenantId);
                }
            }

            prepStmt.executeBatch();

        } finally {
            IdentityDatabaseUtil.closeAllConnections(null, rs, prepStmt);
        }
    }

    /**
     * @param conn
     * @param idPId
     * @param idpRoleNames
     * @throws SQLException
     */
    private void addIdPRoles(Connection conn, int idPId, int tenantId, String[] idpRoleNames)
            throws SQLException {

        PreparedStatement prepStmt = null;
        // SP_IDP_ID, SP_IDP_ROLE
        String sqlStmt = IdPManagementConstants.SQLQueries.ADD_IDP_ROLES_SQL;

        if (idpRoleNames == null || idpRoleNames.length == 0) {
            return;
        }

        try {
            prepStmt = conn.prepareStatement(sqlStmt);

            for (String idpRole : idpRoleNames) {
                prepStmt.setInt(1, idPId);
                prepStmt.setInt(2, tenantId);
                prepStmt.setString(3, idpRole);
                prepStmt.addBatch();
                prepStmt.clearParameters();
            }

            prepStmt.executeBatch();

        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
        }
    }

    /**
     * Add Identity Provider groups.
     *
     * @param conn      Database connection.
     * @param idPId     Identity Provider ID.
     * @param tenantId  Tenant ID of the Identity Provider.
     * @param idPGroups List of Identity Provider groups.
     * @throws SQLException Database exception when adding Identity Provider groups.
     */
    private void addIdPGroups(Connection conn, int idPId, int tenantId, IdPGroup[] idPGroups, boolean isUpdate)
            throws SQLException {

        if (idPGroups == null || idPGroups.length == 0) {
            return;
        }
        try (PreparedStatement addIdPGroupsPrepStmt = conn.prepareStatement(
                IdPManagementConstants.SQLQueries.ADD_IDP_GROUPS_SQL)) {
            for (IdPGroup idpGroup : idPGroups) {
                if (StringUtils.isBlank(idpGroup.getIdpGroupName())) {
                    continue;
                }
                if (!isUpdate && StringUtils.isNotBlank(idpGroup.getIdpGroupId())) {
                    log.debug("IdP Group ID should be null for new IdP Group. Hence not adding the IdP group.");
                    continue;
                }
                if (StringUtils.isBlank(idpGroup.getIdpGroupId())) {
                    idpGroup.setIdpGroupId(UUID.randomUUID().toString());
                }
                addIdPGroupsPrepStmt.setInt(1, idPId);
                addIdPGroupsPrepStmt.setInt(2, tenantId);
                addIdPGroupsPrepStmt.setString(3, idpGroup.getIdpGroupName());
                addIdPGroupsPrepStmt.setString(4, idpGroup.getIdpGroupId());
                addIdPGroupsPrepStmt.addBatch();
                addIdPGroupsPrepStmt.clearParameters();
            }
            addIdPGroupsPrepStmt.executeBatch();
        }
    }

    /**
     * @param conn
     * @param idPId
     * @param tenantId
     * @param roleMappings
     * @throws SQLException
     * @throws IdentityProviderManagementException
     */
    private void addIdPRoleMappings(Connection conn, int idPId, int tenantId,
                                    RoleMapping[] roleMappings) throws SQLException,
            IdentityProviderManagementException {

        Map<String, Integer> roleIdMap = new HashMap<String, Integer>();
        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        // SP_IDP_ROLE_ID, SP_IDP_ROL
        String sqlStmt = IdPManagementConstants.SQLQueries.GET_IDP_ROLES_SQL;

        try {

            prepStmt = conn.prepareStatement(sqlStmt);
            prepStmt.setInt(1, idPId);
            rs = prepStmt.executeQuery();

            while (rs.next()) {
                int idpRoleId = rs.getInt("ID");
                String roleName = rs.getString("ROLE");
                roleIdMap.put(roleName, idpRoleId);
            }

            if (roleIdMap.isEmpty()) {
                String message = "No Identity Provider roles defined for tenant " + tenantId;
                throw new IdentityProviderManagementException(message);
            }

            sqlStmt = IdPManagementConstants.SQLQueries.ADD_IDP_ROLE_MAPPINGS_SQL;
            prepStmt = conn.prepareStatement(sqlStmt);

            for (RoleMapping mapping : roleMappings) {
                if (mapping.getRemoteRole() != null
                        && roleIdMap.containsKey(mapping.getRemoteRole())) {

                    int idpRoleId = roleIdMap.get(mapping.getRemoteRole());

                    String userStoreId = mapping.getLocalRole().getUserStoreId();
                    String localRole = mapping.getLocalRole().getLocalRoleName();

                    // SP_IDP_ROLE_ID, SP_TENANT_ID, SP_USER_STORE_ID, SP_LOCAL_ROLE
                    prepStmt.setInt(1, idpRoleId);
                    prepStmt.setInt(2, tenantId);
                    prepStmt.setString(3, userStoreId);
                    prepStmt.setString(4, localRole);
                    prepStmt.addBatch();
                } else {
                    throw new IdentityProviderManagementException("Cannot find Identity Provider role " +
                            mapping.getRemoteRole() + " for tenant " + tenantId);
                }
            }

            prepStmt.executeBatch();

        } finally {
            IdentityDatabaseUtil.closeAllConnections(null, rs, prepStmt);
        }

    }

    /**
     * @param conn
     * @param idPId
     * @param tenantId
     * @param newClaimConfig
     * @throws SQLException
     * @throws IdentityProviderManagementException
     */
    private void updateClaimConfiguration(Connection conn, int idPId, int tenantId,
                                          ClaimConfig newClaimConfig) throws SQLException,
            IdentityProviderManagementException {

        // remove all identity provider claims - this will also remove associated claim mappings.
        deleteAllIdPClaims(conn, idPId);

        // delete local claim identity provider claim values.
        deleteLocalIdPClaimValues(conn, idPId, tenantId);

        if (newClaimConfig == null) {
            // bad data - we do not need.
            return;
        }

        if (newClaimConfig.isLocalClaimDialect()) {
            if (newClaimConfig.getClaimMappings() != null && newClaimConfig.getClaimMappings().length > 0) {
                // add claim mappings only.
                addDefaultClaimValuesForLocalIdP(conn, idPId, tenantId,
                        newClaimConfig.getClaimMappings());
            }
        } else {
            boolean addedClaims = false;
            if (newClaimConfig.getIdpClaims() != null && newClaimConfig.getIdpClaims().length > 0) {
                // add identity provider claims.
                addIdPClaims(conn, idPId, tenantId, newClaimConfig.getIdpClaims());
                addedClaims = true;
            }
            if (addedClaims && newClaimConfig.getClaimMappings() != null &&
                    newClaimConfig.getClaimMappings().length > 0) {
                // add identity provider claim mappings if and only if IdP claims are not empty.
                addIdPClaimMappings(conn, idPId, tenantId, newClaimConfig.getClaimMappings());
            }
        }
    }

    /**
     * @param conn
     * @param idPId
     * @param addedRoles
     * @param deletedRoles
     * @param renamedOldRoles
     * @param renamedNewRoles
     * @throws SQLException
     */
    private void updateIdPRoles(Connection conn, int idPId, List<String> addedRoles,
                                List<String> deletedRoles, List<String> renamedOldRoles, List<String> renamedNewRoles)
            throws SQLException {

        PreparedStatement prepStmt1 = null;
        PreparedStatement prepStmt2 = null;
        PreparedStatement prepStmt3 = null;
        String sqlStmt = null;

        try {

            for (String deletedRole : deletedRoles) {
                sqlStmt = IdPManagementConstants.SQLQueries.DELETE_IDP_ROLES_SQL;
                prepStmt1 = conn.prepareStatement(sqlStmt);
                prepStmt1.setInt(1, idPId);
                prepStmt1.setString(2, deletedRole);
                prepStmt1.addBatch();
            }

            prepStmt1.executeBatch();

            for (String addedRole : addedRoles) {
                sqlStmt = IdPManagementConstants.SQLQueries.ADD_IDP_ROLES_SQL;
                prepStmt2 = conn.prepareStatement(sqlStmt);
                prepStmt2.setInt(1, idPId);
                prepStmt2.setString(2, addedRole);
                prepStmt2.addBatch();
            }

            prepStmt2.executeBatch();
            prepStmt2.clearParameters();
            prepStmt2.clearBatch();

            for (int i = 0; i < renamedOldRoles.size(); i++) {
                sqlStmt = IdPManagementConstants.SQLQueries.UPDATE_IDP_ROLES_SQL;
                prepStmt3 = conn.prepareStatement(sqlStmt);
                prepStmt3.setString(1, renamedNewRoles.get(i));
                prepStmt3.setInt(2, idPId);
                prepStmt3.setString(3, renamedOldRoles.get(i));
                prepStmt3.addBatch();
            }

            prepStmt3.executeBatch();

        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt3);
            IdentityDatabaseUtil.closeStatement(prepStmt2);
            IdentityDatabaseUtil.closeStatement(prepStmt1);
        }

    }

    /**
     * @param conn
     * @param idPId
     * @param tenantId
     * @param newRoleConfiguration
     * @param newRoleConfiguration
     * @throws SQLException
     * @throws IdentityProviderManagementException
     */
    private void updateRoleConfiguration(Connection conn, int idPId, int tenantId,
                                         PermissionsAndRoleConfig newRoleConfiguration) throws SQLException,
            IdentityProviderManagementException {

        // delete all identity provider roles - this will also clean up idp role mappings.
        deleteAllIdPRoles(conn, idPId);

        if (newRoleConfiguration == null) {
            // bad data - we do not need to deal with.
            return;
        }

        // add identity provider roles.
        addIdPRoles(conn, idPId, tenantId, newRoleConfiguration.getIdpRoles());

        if (newRoleConfiguration.getRoleMappings() == null
                || newRoleConfiguration.getRoleMappings().length == 0) {
            // we do not have any role mappings in the system.
            return;
        }

        // add identity provider role mappings.
        addIdPRoleMappings(conn, idPId, tenantId, newRoleConfiguration.getRoleMappings());

    }

    /**
     * Update the idp group configuration.
     *
     * @param conn              Database connection.
     * @param idPId             IdP id of the Identity Provider.
     * @param tenantId          Tenant id of the Identity Provider.
     * @param newIdPGroupConfig New idp group configuration.
     * @throws SQLException                        Exception when updating the idp groups in the database.
     * @throws IdentityProviderManagementException Exception when updating the idp groups.
     */
    private void updateIdPGroupConfiguration(Connection conn, int idPId, int tenantId, IdPGroup[] newIdPGroupConfig)
            throws SQLException, IdentityProviderManagementException {

        // Filter IdP groups with valid UUIDs.
        validateIdForIdPGroups(conn, idPId, newIdPGroupConfig);
        // Delete all identity provider groups.
        deleteAllIdPGroups(conn, idPId);
        // Add identity provider groups.
        addIdPGroups(conn, idPId, tenantId, newIdPGroupConfig, true);
    }

    /**
     * Check whether the idp groups to be updated have valid UUIDs.
     *
     * @param conn              Database connection.
     * @param idPId             IdP id of the Identity Provider.
     * @param IdPGroupsToUpdate New idp group configuration.
     * @throws SQLException Exception when retrieving the old idp groups in the database.
     */
    private void validateIdForIdPGroups(Connection conn, int idPId, IdPGroup[] IdPGroupsToUpdate) throws SQLException {

        if (ArrayUtils.isEmpty(IdPGroupsToUpdate)) {
            return;
        }
        IdPGroup[] existingIdPGroups = getIdPGroupConfiguration(conn, idPId);
        String[] existingIdPGroupIds = Arrays.stream(existingIdPGroups).map(IdPGroup::getIdpGroupId)
                .toArray(String[]::new);
        // Set null to the IdP group id if it is not in the existing IdP groups.
        Arrays.stream(IdPGroupsToUpdate)
                .filter(idPGroup -> !ArrayUtils.contains(existingIdPGroupIds, idPGroup.getIdpGroupId()))
                .forEach(idPGroup -> idPGroup.setIdpGroupId(null));
    }

    /**
     * @param conn
     * @param conn
     * @param idPId
     * @throws SQLException
     */
    private void deleteProvisioningConnectorConfigs(Connection conn, int idPId) throws SQLException {

        String databaseProductName = conn.getMetaData().getDatabaseProductName();
        if (databaseProductName.contains(MySQL)) {
            ResultSet resultSetGetConfigId;
            String sqlStmtGetConfigId = IdPManagementConstants.SQLQueries.GET_IDP_PROVISIONING_CONFIGS_ID;
            try (PreparedStatement prepStmtGetConfigId = conn.prepareStatement(sqlStmtGetConfigId)) {
                prepStmtGetConfigId.setInt(1, idPId);
                resultSetGetConfigId = prepStmtGetConfigId.executeQuery();
                while (resultSetGetConfigId.next()) {
                    int id = resultSetGetConfigId.getInt(ID);
                    deleteIdpProvConfigProperty(conn, id);
                }
            }
        }
        String sqlStmt = IdPManagementConstants.SQLQueries.DELETE_PROVISIONING_CONNECTORS;

        try (PreparedStatement prepStmt = conn.prepareStatement(sqlStmt)) {
            prepStmt.setInt(1, idPId);
            prepStmt.executeUpdate();
        }
    }

    /**
     * Delete IDP provisioning configuration property.
     *
     * @param connection             Connection to the DB.
     * @param provisioningConfigId   Provisioning Configuration Id of the IdP.
     * @throws SQLException          Database Exception.
     */
    private void deleteIdpProvConfigProperty(Connection connection, int provisioningConfigId) throws SQLException {

        String sqlStmt = IdPManagementConstants.SQLQueries.DELETE_IDP_PROV_CONFIG_PROPERTY;
        try (PreparedStatement prepStmt = connection.prepareStatement(sqlStmt)) {
            prepStmt.setInt(1, provisioningConfigId);
            prepStmt.executeUpdate();
        }
    }

    /**
     * Delete Identity provider in the given tenant.
     *
     * @param conn              Connection to the DB.
     * @param tenantId          Tenant Id of the IdP.
     * @param idPName           Name of the IdP.
     * @param resourceId        Resource Id (UUID) of the IdP.
     * @throws SQLException     Database Exception.
     */
    private void deleteIdP(Connection conn, int tenantId, String idPName, String resourceId) throws SQLException {

        String databaseProductName = conn.getMetaData().getDatabaseProductName();
        String sqlStmt = IdPManagementConstants.SQLQueries.DELETE_IDP_BY_RESOURCE_ID_SQL;
        String sqlStmtGetIdpId = IdPManagementConstants.SQLQueries.GET_IDP_CONFIGS_ID_FROM_TENANT_ID_AND_NAME;
        String sqlStmtIdpIdFromUUID = IdPManagementConstants.SQLQueries.GET_IDP_CONFIGS_ID_FROM_UUID;

        if (StringUtils.isBlank(resourceId)) {
            sqlStmt = IdPManagementConstants.SQLQueries.DELETE_IDP_SQL;
        }

        try (PreparedStatement prepStmt = conn.prepareStatement(sqlStmt);
        PreparedStatement prepStmtGetIdpId = conn.prepareStatement(sqlStmtGetIdpId);
            PreparedStatement prepStmtIdpIdFromUUID = conn.prepareStatement(sqlStmtIdpIdFromUUID)) {
            if (StringUtils.isBlank(resourceId)) {
                if (databaseProductName.contains(MySQL)) {
                    ResultSet resultSetGetIdpId = null;
                    prepStmtGetIdpId.setInt(1, tenantId);
                    prepStmtGetIdpId.setString(2, idPName);
                    resultSetGetIdpId = prepStmtGetIdpId.executeQuery();
                    while (resultSetGetIdpId.next()) {
                        int id = resultSetGetIdpId.getInt(ID);
                        deleteProvisioningConnectorConfigs(conn, id);
                    }
                }
                prepStmt.setInt(1, tenantId);
                prepStmt.setString(2, idPName);
            } else {
                if (databaseProductName.contains(MySQL)) {
                    ResultSet resultSetGetIdpId = null;
                    prepStmtIdpIdFromUUID.setString(1, resourceId);
                    resultSetGetIdpId = prepStmtIdpIdFromUUID.executeQuery();
                    while (resultSetGetIdpId.next()) {
                        int id = resultSetGetIdpId.getInt(ID);
                        deleteProvisioningConnectorConfigs(conn, id);
                    }
                }
                prepStmt.setString(1, resourceId);
            }
            prepStmt.executeUpdate();
        }
    }

    /**
     * Delete authentication steps involving the deleted IDP in all SPs in the given tenant.
     *
     * @param conn
     * @param tenantId
     * @param idpName
     * @throws SQLException
     */
    private void deleteIdpSpAuthAssociations(Connection conn, int tenantId, String idpName) throws SQLException {

        PreparedStatement removeAuthStepPreparedStatement = null;
        PreparedStatement removeEmptyAuthStepPreparedStatement = null;
        String removeAuthStepsSql = IdPManagementConstants.SQLQueries.DELETE_IDP_SP_AUTH_ASSOCIATIONS;
        String removeEmptyAuthStepsSql = IdPManagementConstants.SQLQueries.REMOVE_EMPTY_SP_AUTH_STEPS;

        try {
            // Remove authentication steps in SPs with the IDP being deleted
            removeAuthStepPreparedStatement = conn.prepareStatement(removeAuthStepsSql);
            removeAuthStepPreparedStatement.setString(1, idpName);
            removeAuthStepPreparedStatement.setInt(2, tenantId);
            removeAuthStepPreparedStatement.executeUpdate();

            // Clean up any empty steps left over after deletion
            removeEmptyAuthStepPreparedStatement = conn.prepareStatement(removeEmptyAuthStepsSql);
            removeEmptyAuthStepPreparedStatement.executeUpdate();
        } finally {
            IdentityDatabaseUtil.closeStatement(removeAuthStepPreparedStatement);
            IdentityDatabaseUtil.closeStatement(removeEmptyAuthStepPreparedStatement);
        }
    }

    /**
     * Delete Provisioning associations of the deleted IDP with any SPs in a given tenant.
     *
     * @param conn
     * @param tenantId
     * @param idpName
     * @throws SQLException
     */
    private void deleteIdpSpProvisioningAssociations(Connection conn,
                                                     int tenantId,
                                                     String idpName) throws SQLException {

        PreparedStatement prepStmt = null;
        String sqlStmt = IdPManagementConstants.SQLQueries.DELETE_IDP_SP_PROVISIONING_ASSOCIATIONS;

        try {
            prepStmt = conn.prepareStatement(sqlStmt);
            prepStmt.setString(1, idpName);
            prepStmt.setInt(2, tenantId);
            prepStmt.executeUpdate();
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
        }
    }

    /**
     * @param dbConnection
     * @param idpName
     * @param tenantId
     * @return
     * @throws SQLException
     * @throws IdentityProviderManagementException
     */
    private int getIdentityProviderIdByName(Connection dbConnection, String idpName, int tenantId)
            throws SQLException, IdentityProviderManagementException {

        boolean dbConnInitialized = true;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        if (dbConnection == null) {
            dbConnection = IdentityDatabaseUtil.getDBConnection(false);
        } else {
            dbConnInitialized = false;
        }
        try {

            String sqlStmt = IdPManagementConstants.SQLQueries.GET_IDP_ROW_ID_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, tenantId);
            prepStmt.setInt(2, MultitenantConstants.SUPER_TENANT_ID);
            prepStmt.setString(3, idpName);
            rs = prepStmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } finally {
            if (dbConnInitialized) {
                IdentityDatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
            } else {
                IdentityDatabaseUtil.closeAllConnections(null, rs, prepStmt);
            }
        }
        return 0;
    }

    /**
     * @param o1
     * @param o2
     * @return
     */
    private Property[] concatArrays(Property[] o1, Property[] o2) {

        Property[] ret = new Property[o1.length + o2.length];

        System.arraycopy(o1, 0, ret, 0, o1.length);
        System.arraycopy(o2, 0, ret, o1.length, o2.length);

        return ret;
    }

    /**
     * @param dbConnection
     * @param idPName
     * @param tenantId
     * @return
     * @throws SQLException
     * @throws IdentityProviderManagementException
     */
    private int getIdentityProviderIdentifier(Connection dbConnection, String idPName, int tenantId)
            throws SQLException, IdentityProviderManagementClientException {

        String sqlStmt = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        try {
            sqlStmt = IdPManagementConstants.SQLQueries.GET_IDP_BY_NAME_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, tenantId);
            prepStmt.setInt(2, MultitenantConstants.SUPER_TENANT_ID);
            prepStmt.setString(3, idPName);
            rs = prepStmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("ID");
            } else {
                throw new IdentityProviderManagementClientException("Invalid Identity Provider Name "
                        + idPName);
            }
        } finally {
            IdentityDatabaseUtil.closeAllConnections(null, rs, prepStmt);
        }
    }

    public boolean isIdPAvailableForAuthenticatorProperty(String authenticatorName, String propertyName, String
            idPEntityId, int tenantId)
            throws IdentityProviderManagementException {

        boolean isAvailable = false;
        Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false);
        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        try {
            String sqlStmt = IdPManagementConstants.SQLQueries.GET_SIMILAR_IDP_ENTITIY_IDS;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setString(1, propertyName);
            prepStmt.setString(2, idPEntityId);
            prepStmt.setInt(3, tenantId);
            prepStmt.setString(4, authenticatorName);
            rs = prepStmt.executeQuery();
            if (rs.next()) {
                isAvailable = rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new IdentityProviderManagementException("Error occurred while searching for similar IdP EntityIds",
                    e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
        }
        return isAvailable;
    }

    /**
     * Get list of applications that are connected to the identity provider from DB.
     *
     * @param resourceId IDP resource ID.
     * @param limit      Limit parameter for pagination.
     * @param offset     Offset parameter for pagination.
     * @return ConnectedAppsResult.
     * @throws IdentityProviderManagementException
     */
    public ConnectedAppsResult getConnectedApplications(String resourceId, int limit, int offset)
            throws IdentityProviderManagementException {

        ConnectedAppsResult connectedAppsResult = new ConnectedAppsResult();
        List<String> connectedApps = new ArrayList<>();
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            try (PreparedStatement prepStmt = createConnectedAppsSqlStatement(connection, resourceId, limit, offset)) {
                try (ResultSet resultSet = prepStmt.executeQuery()) {
                    while (resultSet.next()) {
                        connectedApps.add(resultSet.getString("UUID"));
                    }
                }
            }
            String sqlQuery = IdPManagementConstants.SQLQueries.CONNECTED_APPS_TOTAL_COUNT_SQL;
            try (PreparedStatement prepStmt = connection.prepareStatement(sqlQuery)) {
                prepStmt.setString(1, resourceId);
                prepStmt.setString(2, resourceId);
                try (ResultSet resultSet = prepStmt.executeQuery()) {
                    if (resultSet.next()) {
                        connectedAppsResult.setTotalAppCount(resultSet.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Error occurred during retrieving connected applications of IDP: " + resourceId, e);
            throw IdPManagementUtil.handleServerException(IdPManagementConstants.ErrorMessage
                    .ERROR_CODE_RETRIEVE_IDP_CONNECTED_APPS, resourceId);
        }
        connectedAppsResult.setApps(connectedApps);
        connectedAppsResult.setLimit(limit);
        connectedAppsResult.setOffSet(offset);
        return connectedAppsResult;
    }

    /**
     * Get configured applications for a local authenticator.
     *
     * @param authenticatorId   ID of local authenticator.
     * @param tenantId          Tenant ID.
     * @param limit             Limit per page.
     * @param offset            Offset value.
     * @return Connected applications.
     * @throws IdentityProviderManagementException If an error occurred when retrieving connected applications.
     */
    public ConnectedAppsResult getConnectedAppsOfLocalAuthenticator(String authenticatorId, int tenantId,
                                                                    Integer limit, Integer offset)
            throws IdentityProviderManagementException {

        ConnectedAppsResult connectedAppsResult = new ConnectedAppsResult();
        List<String> connectedApps = new ArrayList<>();
        String identityProviderName = new String(Base64.getUrlDecoder().decode(authenticatorId), StandardCharsets.UTF_8);
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            try (PreparedStatement prepStmt = createConnectedAppsOfLocalAuthenticatorSqlStatement(connection,
                    identityProviderName, tenantId, limit, offset)) {

                try (ResultSet resultSet = prepStmt.executeQuery()) {
                    while (resultSet.next()) {
                        connectedApps.add(resultSet.getString("UUID"));
                    }
                }
            }
            String sqlQuery = IdPManagementConstants.SQLQueries.LOCAL_AUTH_CONNECTED_APPS_TOTAL_COUNT_SQL;
            try (PreparedStatement prepStmt = connection.prepareStatement(sqlQuery)) {
                prepStmt.setString(1, identityProviderName);
                prepStmt.setInt(2, tenantId);
                prepStmt.setString(3, identityProviderName);
                prepStmt.setInt(4, tenantId);
                try (ResultSet resultSet = prepStmt.executeQuery()) {
                    if (resultSet.next()) {
                        connectedAppsResult.setTotalAppCount(resultSet.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Error occurred during retrieving connected applications of authenticator: " +
                    authenticatorId, e);
            throw IdPManagementUtil.handleServerException(IdPManagementConstants.ErrorMessage
                    .ERROR_CODE_RETRIEVE_IDP_CONNECTED_APPS, authenticatorId);
        }
        connectedAppsResult.setApps(connectedApps);
        connectedAppsResult.setLimit(limit);
        return connectedAppsResult;
    }

    private PreparedStatement createConnectedAppsSqlStatement(Connection connection, String id, int limit, int offset)
            throws SQLException, IdentityProviderManagementServerException {

        String sqlQuery;
        PreparedStatement prepStmt;
        String databaseProductName = connection.getMetaData().getDatabaseProductName();
        if (databaseProductName.contains("MySQL")
                || databaseProductName.contains("MariaDB")
                || databaseProductName.contains("H2")) {
            sqlQuery = IdPManagementConstants.SQLQueries.GET_CONNECTED_APPS_MYSQL;
            prepStmt = connection.prepareStatement(sqlQuery);
            prepStmt.setString(1, id);
            prepStmt.setString(2, id);
            prepStmt.setString(3, id);
            prepStmt.setInt(4, offset);
            prepStmt.setInt(5, limit);
        } else if (databaseProductName.contains("Oracle")) {
            sqlQuery = IdPManagementConstants.SQLQueries.GET_CONNECTED_APPS_ORACLE;
            prepStmt = connection.prepareStatement(sqlQuery);
            prepStmt.setString(1, id);
            prepStmt.setString(2, id);
            prepStmt.setString(3, id);
            prepStmt.setInt(4, offset + limit);
            prepStmt.setInt(5, offset);
        } else if (databaseProductName.contains("Microsoft")) {
            sqlQuery = IdPManagementConstants.SQLQueries.GET_CONNECTED_APPS_MSSQL;
            prepStmt = connection.prepareStatement(sqlQuery);
            prepStmt.setString(1, id);
            prepStmt.setString(2, id);
            prepStmt.setString(3, id);
            prepStmt.setInt(4, offset);
            prepStmt.setInt(5, limit);
        } else if (databaseProductName.contains("PostgreSQL")) {
            sqlQuery = IdPManagementConstants.SQLQueries.GET_CONNECTED_APPS_POSTGRESSQL;
            prepStmt = connection.prepareStatement(sqlQuery);
            prepStmt.setString(1, id);
            prepStmt.setString(2, id);
            prepStmt.setString(3, id);
            prepStmt.setInt(4, limit);
            prepStmt.setInt(5, offset);
        } else if (databaseProductName.contains("DB2")) {
            sqlQuery = IdPManagementConstants.SQLQueries.GET_CONNECTED_APPS_DB2SQL;
            prepStmt = connection.prepareStatement(sqlQuery);
            prepStmt.setString(1, id);
            prepStmt.setString(2, id);
            prepStmt.setString(3, id);
            prepStmt.setInt(4, limit);
            prepStmt.setInt(5, offset);
        } else if (databaseProductName.contains("INFORMIX")) {
            sqlQuery = IdPManagementConstants.SQLQueries.GET_CONNECTED_APPS_INFORMIX;
            prepStmt = connection.prepareStatement(sqlQuery);
            prepStmt.setInt(1, offset);
            prepStmt.setInt(2, limit);
            prepStmt.setString(3, id);
        } else {
            String message = "Error while loading Identity Provider Connected Applications from DB: Database driver " +
                    "could not be identified or not supported.";
            log.error(message);
            throw IdPManagementUtil.handleServerException(IdPManagementConstants.ErrorMessage
                    .ERROR_CODE_CONNECTING_DATABASE, message);
        }
        return prepStmt;
    }

    private PreparedStatement createConnectedAppsOfLocalAuthenticatorSqlStatement(Connection connection, String name,
                                                                                  int tenantId, int limit, int offset)
            throws SQLException, IdentityProviderManagementServerException {

        String sqlQuery;
        PreparedStatement prepStmt;
        String databaseProductName = connection.getMetaData().getDatabaseProductName();
        // We are passing the authenticator name for the queries for IDP_NAME since the details are stored in
        // IDP related tables.
        if (databaseProductName.contains("MySQL")
                || databaseProductName.contains("MariaDB")
                || databaseProductName.contains("H2")) {
            sqlQuery = IdPManagementConstants.SQLQueries.GET_CONNECTED_APPS_LOCAL_MYSQL;
            prepStmt = createSqlStatement(connection, sqlQuery, name, tenantId, tenantId, name, offset, limit);
        } else if (databaseProductName.contains("Oracle")) {
            sqlQuery = IdPManagementConstants.SQLQueries.GET_CONNECTED_APPS_LOCAL_ORACLE;
            prepStmt = createSqlStatement(connection, sqlQuery, name, tenantId, tenantId, name, offset + limit, offset);
        } else if (databaseProductName.contains("Microsoft")) {
            sqlQuery = IdPManagementConstants.SQLQueries.GET_CONNECTED_APPS_LOCAL_MSSQL;
            prepStmt = createSqlStatement(connection, sqlQuery, name, tenantId, tenantId, name, offset, limit);
        } else if (databaseProductName.contains("PostgreSQL")) {
            sqlQuery = IdPManagementConstants.SQLQueries.GET_CONNECTED_APPS_LOCAL_POSTGRESSQL;
            prepStmt = createSqlStatement(connection, sqlQuery, name, tenantId, tenantId, name, limit, offset);
        } else if (databaseProductName.contains("DB2")) {
            sqlQuery = IdPManagementConstants.SQLQueries.GET_CONNECTED_APPS_LOCAL_DB2SQL;
            prepStmt = createSqlStatement(connection, sqlQuery, name, tenantId, tenantId, name, limit, offset);
        } else if (databaseProductName.contains("INFORMIX")) {
            sqlQuery = IdPManagementConstants.SQLQueries.GET_CONNECTED_APPS_LOCAL_INFORMIX;
            prepStmt = createSqlStatement(connection, sqlQuery, offset, limit, name, tenantId);
        } else {
            String message = "Error while loading Identity Provider Connected Applications from DB: Database driver " +
                    "could not be identified or not supported.";
            log.error(message);
            throw IdPManagementUtil.handleServerException(IdPManagementConstants.ErrorMessage
                    .ERROR_CODE_CONNECTING_DATABASE, message);
        }
        return prepStmt;
    }

    private PreparedStatement createSqlStatement(Connection connection, String sqlQuery, Object... params)
            throws SQLException, IdentityProviderManagementServerException {

        PreparedStatement prepStmt = connection.prepareStatement(sqlQuery);
        if (params != null && params.length > 0) {
            for (int i = 0; i < params.length; i++) {
                Object param = params[i];
                if (param == null) {
                    throw new IdentityProviderManagementServerException("Invalid data provided.");
                } else if (param instanceof String) {
                    prepStmt.setString(i + 1, (String) param);
                } else if (param instanceof Integer) {
                    prepStmt.setInt(i + 1, (Integer) param);
                } else if (param instanceof Date) {
                    prepStmt.setTimestamp(i + 1, new Timestamp(System.currentTimeMillis()));
                } else if (param instanceof Boolean) {
                    prepStmt.setBoolean(i + 1, (Boolean) param);
                }
            }
        }
        return prepStmt;
    }

    private int getAuthenticatorIdentifier(Connection dbConnection, int idPId, String authnType)
            throws SQLException, IdentityProviderManagementException {

        String sqlStmt = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        try {
            sqlStmt = IdPManagementConstants.SQLQueries.GET_IDP_AUTH_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, idPId);
            prepStmt.setString(2, authnType);
            rs = prepStmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("ID");
            } else {
                throw new IdentityProviderManagementException("Cannot find authenticator : "
                        + authnType);
            }
        } finally {
            IdentityDatabaseUtil.closeAllConnections(null, rs, prepStmt);
        }
    }

    private List<IdentityProviderProperty> resolveConnectorProperties(List<IdentityProviderProperty> propertiesFromDb,
                                                                      String tenantDomain)
            throws ConnectorException {

        Map<String, IdentityProviderProperty> propertyMapFromDb =
                propertiesFromDb.stream().collect(Collectors.toMap(IdentityProviderProperty::getName, property -> property));

        Map<String, IdentityProviderProperty> propertiesFromConnectors = getConnectorProperties(tenantDomain);

        // Replace the property default values with the values saved in the database.
        propertiesFromConnectors.putAll(propertyMapFromDb);
        resolveOtpConnectorProperties(propertiesFromConnectors);
        setDefaultSessionConfigs(propertiesFromConnectors);

        return new ArrayList<>(propertiesFromConnectors.values());
    }

    private void setDefaultSessionConfigs(Map<String, IdentityProviderProperty> propertiesFromConnectors) {

        if (propertiesFromConnectors.get(REMEMBER_ME_TIME_OUT) == null) {
            String configuredRemberMeTimeout = IdentityUtil.getProperty(
                    IdentityConstants.ServerConfig.REMEMBER_ME_TIME_OUT);
            if (StringUtils.isBlank(configuredRemberMeTimeout) || !StringUtils.isNumeric(configuredRemberMeTimeout)
                    || Integer.parseInt(configuredRemberMeTimeout) <= 0) {
                configuredRemberMeTimeout = REMEMBER_ME_TIME_OUT_DEFAULT;
            }

            IdentityProviderProperty rememberMeTimeOut = new IdentityProviderProperty();
            rememberMeTimeOut.setName(REMEMBER_ME_TIME_OUT);
            rememberMeTimeOut.setValue(configuredRemberMeTimeout);
            propertiesFromConnectors.put(REMEMBER_ME_TIME_OUT, rememberMeTimeOut);
        }

        if (propertiesFromConnectors.get(SESSION_IDLE_TIME_OUT) == null) {
            String configuredSessionIdleTimeout = IdentityUtil.getProperty(
                    IdentityConstants.ServerConfig.SESSION_IDLE_TIMEOUT);
            if (StringUtils.isBlank(configuredSessionIdleTimeout) ||
                    !StringUtils.isNumeric(configuredSessionIdleTimeout) ||
                    Integer.parseInt(configuredSessionIdleTimeout) <= 0) {
                configuredSessionIdleTimeout = SESSION_IDLE_TIME_OUT_DEFAULT;
            }

            IdentityProviderProperty sessionIdleTimeOut = new IdentityProviderProperty();
            sessionIdleTimeOut.setName(SESSION_IDLE_TIME_OUT);
            sessionIdleTimeOut.setValue(configuredSessionIdleTimeout);
            propertiesFromConnectors.put(SESSION_IDLE_TIME_OUT, sessionIdleTimeOut);
        }

        if (propertiesFromConnectors.get(PRESERVE_CURRENT_SESSION_AT_PASSWORD_UPDATE) == null) {
            String preserveLoggedInSessionAtPasswordUpdate = IdentityUtil.getProperty(
                    IdentityConstants.ServerConfig.PRESERVE_LOGGED_IN_SESSION_AT_PASSWORD_UPDATE);
            if (StringUtils.isBlank(preserveLoggedInSessionAtPasswordUpdate)) {
                preserveLoggedInSessionAtPasswordUpdate = "false";
            }

            IdentityProviderProperty preserveCurrentSessionAtPasswordUpdateProperty = new IdentityProviderProperty();
            preserveCurrentSessionAtPasswordUpdateProperty.setName(PRESERVE_CURRENT_SESSION_AT_PASSWORD_UPDATE);
            preserveCurrentSessionAtPasswordUpdateProperty.setValue(preserveLoggedInSessionAtPasswordUpdate);
            propertiesFromConnectors.put(PRESERVE_CURRENT_SESSION_AT_PASSWORD_UPDATE,
                    preserveCurrentSessionAtPasswordUpdateProperty);
        }
    }

    private List<IdentityProviderProperty> filterConnectorProperties(IdentityProviderProperty[] propertiesFromRequest,
                                                                     String tenantDomain)
            throws ConnectorException {

        Map<String, IdentityProviderProperty> propertiesFromConnectors = getConnectorProperties(tenantDomain);

        /*
         * Session configuration properties are not stored by default.
         * If the values from the request match the default values, they should be removed.
         * Default session configs are set in the connector properties to ensure correct handling in the logic below.
         */
        setDefaultSessionConfigs(propertiesFromConnectors);
        Map<String, IdentityProviderProperty> propertyMapFromRequest = Arrays.stream(propertiesFromRequest)
                .collect(Collectors.toMap(IdentityProviderProperty::getName, property -> property));

        for (Map.Entry<String, IdentityProviderProperty> entry : propertiesFromConnectors.entrySet()) {
            IdentityProviderProperty propertyFromRequest = propertyMapFromRequest.get(entry.getKey());
            if (propertyFromRequest != null && entry.getValue().getValue().equals(propertyFromRequest.getValue())) {
                // If the value received from the update request is equal to the default value, remove the entry.
                propertyMapFromRequest.remove(entry.getKey());
            }
        }

        return new ArrayList<>(propertyMapFromRequest.values());
    }


    private Map<String, IdentityProviderProperty> getConnectorProperties(String tenantDomain) throws ConnectorException {

        List<ConnectorConfig> connectorConfigList =
                IdpMgtServiceComponentHolder.getInstance().getIdentityConnectorConfigList();
        Map<String, IdentityProviderProperty> propertiesFromConnectors = new HashMap<>();
        for (ConnectorConfig connectorConfig : connectorConfigList) {
            String[] propertyNames = connectorConfig.getPropertyNames();
            Properties defaultPropertyValues = connectorConfig.getDefaultPropertyValues(tenantDomain);
            Map<String, String> displayNames = connectorConfig.getPropertyNameMapping();
            for (String property : propertyNames) {
                IdentityProviderProperty identityProviderProperty = new IdentityProviderProperty();
                identityProviderProperty.setName(property);
                identityProviderProperty.setValue(defaultPropertyValues.getProperty(property));
                identityProviderProperty.setDisplayName(displayNames.get(property));
                propertiesFromConnectors.put(property, identityProviderProperty);
            }
        }
        return propertiesFromConnectors;
    }

    /**
     * Retrieves the first matching IDP for the given metadata property.
     * Intended to ony be used to retrieve IDP name based on a unique metadata property.
     *
     * @param dbConnection Optional. DB connection.
     * @param property IDP metadata property name.
     * @param value Value associated with given Property.
     * @param tenantId Tenant id whose information is requested.
     * @return Identity Provider name.
     * @throws IdentityProviderManagementException IdentityProviderManagementException.
     */
    public String getIdPNameByMetadataProperty(Connection dbConnection, String property, String value, int tenantId)
            throws IdentityProviderManagementException {

        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        boolean dbConnectionInitialized = true;
        if (dbConnection == null) {
            dbConnection = IdentityDatabaseUtil.getDBConnection(false);
        } else {
            dbConnectionInitialized = false;
        }
        try {
            String sqlStmt = isH2DB() ? IdPManagementConstants.SQLQueries.GET_IDP_NAME_BY_METADATA_H2 :
                    IdPManagementConstants.SQLQueries.GET_IDP_NAME_BY_METADATA;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setString(1, property);
            prepStmt.setString(2, value);
            prepStmt.setInt(3, tenantId);
            rs = prepStmt.executeQuery();
            String idPName = null;

            if (rs.next()) {
                idPName = rs.getString(1);
            }
            return idPName;
        } catch (DataAccessException | SQLException e) {
            throw new IdentityProviderManagementException("Error occurred while retrieving Identity Provider " +
                    "information for IDP metadata property name: " + property + " value: " + value, e);
        } finally {
            if (dbConnectionInitialized) {
                IdentityDatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
            } else {
                IdentityDatabaseUtil.closeAllConnections(null, rs, prepStmt);
            }
        }
    }

    /**
     * Method that checks whether a claim is associated with any identity provider.
     *
     * @param dbConnection  Optional DB connection.
     * @param claimUri      Claim URI.
     * @param tenantId      ID of the tenant.
     * @return  True if claim is referred by an identity provider.
     * @throws IdentityProviderManagementException   Error when obtaining claim references.
     */
    public boolean isClaimReferredByAnyIdp(Connection dbConnection, String claimUri, int tenantId)
            throws IdentityProviderManagementException {

        boolean dbConnInitialized = true;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        boolean isClaimReferred = false;
        if (dbConnection == null) {
            dbConnection = IdentityDatabaseUtil.getDBConnection(false);
        } else {
            dbConnInitialized = false;
        }

        try {
            String sqlStmt = IdPManagementConstants.SQLQueries.GET_TOTAL_IDP_CLAIM_USAGES;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, tenantId);
            prepStmt.setString(2, claimUri);
            rs = prepStmt.executeQuery();

            if (rs.next()) {
                isClaimReferred = rs.getInt(1) > 0;
            }
            return isClaimReferred;
        } catch (SQLException e) {
            throw new IdentityProviderManagementException("Error occurred while retrieving IDP usages of the claim "
                    + claimUri, e);
        } finally {
            if (dbConnInitialized) {
                IdentityDatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
            } else {
                IdentityDatabaseUtil.closeAllConnections(null, rs, prepStmt);
            }
        }
    }

    /**
     * Method that retrieves identityProvider names of a idpId list.
     *
     * @param tenantId Tenant id.
     * @param idpIds Set of identity provider ids.
     * @return A map of identity provider names keyed by idp id.
     * @throws IdentityProviderManagementException
     */
    public Map<String, String> getIdPNamesById(int tenantId, Set<String> idpIds)
            throws IdentityProviderManagementException {

        Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false);
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        Map<String, String> idpNameMap = new HashMap<>();

        try {
            String placeholder = String.join(", ", idpIds);
            String sqlStmt = IdPManagementConstants.SQLQueries.GET_IDPS_BY_IDP_ID_LIST.replace(
                    SCOPE_LIST_PLACEHOLDER, placeholder);
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, tenantId);

            rs = prepStmt.executeQuery();
            while (rs.next()) {
                int idpId = rs.getInt("ID");
                String idpName = rs.getString("NAME");
                if (idpId != 0 && StringUtils.isNotBlank(idpName) &&
                        !IdentityApplicationConstants.RESIDENT_IDP_RESERVED_NAME.equals(idpName) &&
                        !idpName.startsWith(IdPManagementConstants.SHARED_IDP_PREFIX)) {
                    idpNameMap.put(Integer.toString(idpId), idpName);
                }
            }
            IdentityDatabaseUtil.commitTransaction(dbConnection);

            return idpNameMap;
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(dbConnection);
            throw new IdentityProviderManagementException("Error occurred while retrieving registered Identity " +
                    "Providers for IDP IDs for tenantId: " + tenantId, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
        }
    }

    /**
     * Get IDP group data by IDP group IDs.
     *
     * @param idpGroupIds List of IDP group IDs.
     * @param tenantId    Tenant ID.
     * @return List of IDP groups.
     * @throws IdentityProviderManagementException If an error occurred while retrieving IDP groups.
     */
    public List<IdPGroup> getIdPGroupsByIds(List<String> idpGroupIds, int tenantId)
            throws IdentityProviderManagementException {

        if (CollectionUtils.isEmpty(idpGroupIds)) {
            return Collections.emptyList();
        }
        String query = IdPManagementConstants.SQLQueries.GET_IDP_GROUPS_BY_IDP_GROUP_IDS;
        String placeholders = String.join(",", Collections.nCopies(idpGroupIds.size(), "?"));
        query = query.replace(IdPManagementConstants.IDP_GROUP_LIST_PLACEHOLDER, placeholders);
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement prepStmt = connection.prepareStatement(query)) {
            prepStmt.setInt(1, tenantId);
            for (int i = 0; i < idpGroupIds.size(); i++) {
                prepStmt.setString(i + 2, idpGroupIds.get(i));
            }
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                List<IdPGroup> idpGroups = new ArrayList<>();
                while (resultSet.next()) {
                    IdPGroup idpGroup = new IdPGroup();
                    idpGroup.setIdpGroupId(resultSet.getString("UUID"));
                    idpGroup.setIdpGroupName(resultSet.getString("GROUP_NAME"));
                    idpGroup.setIdpId(resultSet.getString("IDP_ID"));
                    idpGroups.add(idpGroup);
                }
                return idpGroups;
            }
        } catch (SQLException e) {
            throw new IdentityProviderManagementException("Error occurred while retrieving IDP groups for IDP group " +
                    "IDs: " + idpGroupIds, e);
        }
    }

    /**
     * Get all user defined federated authenticators.
     *
     * @param tenantId Tenant ID.
     * @return User defined FederatedAuthenticatorConfig list
     * @throws IdentityProviderManagementException If an error occurred while retrieving user defined
     *                                             federated authenticator list.
     */
    public List<FederatedAuthenticatorConfig> getAllUserDefinedFederatedAuthenticators(int tenantId)
            throws IdentityProviderManagementException {

        List<FederatedAuthenticatorConfig> federatedAuthenticatorConfigs = new ArrayList<>();
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement prepStmt = connection.prepareStatement(
                    IdPManagementConstants.SQLQueries.GET_ALL_USER_DEFINED_FEDERATED_AUTHENTICATORS)) {
            prepStmt.setInt(1, tenantId);
            prepStmt.setString(2, "LOCAL");
            prepStmt.setInt(3, tenantId);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                while (resultSet.next()) {
                    UserDefinedFederatedAuthenticatorConfig federatedAuthenticatorConfig =
                            new UserDefinedFederatedAuthenticatorConfig();
                    federatedAuthenticatorConfig.setName(resultSet.getString("NAME"));
                    federatedAuthenticatorConfig.setDisplayName(resultSet.getString("DISPLAY_NAME"));
                    federatedAuthenticatorConfig.setEnabled(resultSet.getBoolean("IS_ENABLED"));
                    federatedAuthenticatorConfig.setDefinedByType(DefinedByType.USER);
                    federatedAuthenticatorConfigs.add(federatedAuthenticatorConfig);
                    int authnId = resultSet.getInt("ID");

                    getFederatedProperties(connection, authnId, federatedAuthenticatorConfig);
                }
            }
            return federatedAuthenticatorConfigs;
        } catch (SQLException e) {
            throw new IdentityProviderManagementException("Error occurred while retrieving all user defined federated " +
                    "authenticators for tenant: " + tenantId, e);
        }
    }

    /**
     * Deletes the specified properties of an identity provider.
     *
     * @param idpId         ID of the identity provider.
     * @param propertyNames List of property names to be deleted.
     * @param tenantDomain  Tenant domain of the identity provider.
     * @throws IdentityProviderManagementException If an error occurred while deleting properties.
     */
    public void deleteIdpProperties(int idpId, List<String> propertyNames, String tenantDomain)
            throws IdentityProviderManagementException {

        String query = IdPManagementConstants.SQLQueries.DELETE_IDP_METADATA_BY_PROPERTY_NAME;
        query = query.replace(
                IdPManagementConstants.IDP_METADATA_PROPERTY_LIST_PLACEHOLDER,
                String.join(",", Collections.nCopies(propertyNames.size(), "?")));

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, idpId);
            for (int i = 0; i < propertyNames.size(); i++) {
                preparedStatement.setString(i + 2, propertyNames.get(i));
            }

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new IdentityProviderManagementException("Error occurred while deleting properties of IDP:" + idpId +
                    " in tenant:" + tenantDomain, e);
        }
    }

    private void getFederatedProperties(Connection connection, int authnId,
            FederatedAuthenticatorConfig federatedAuthenticatorConfig) throws SQLException{

        try (PreparedStatement prepStmtProp = connection.prepareStatement(
                IdPManagementConstants.SQLQueries.GET_IDP_AUTH_PROPS_SQL)) {
            prepStmtProp.setInt(1, authnId);
            Set<Property> properties = new HashSet<Property>();
            try (ResultSet resultSetProp = prepStmtProp.executeQuery()) {
                while (resultSetProp.next()) {
                    Property property = new Property();
                    property.setName(resultSetProp.getString(IdPManagementConstants.SQLConstants.PROPERTY_KEY));
                    property.setValue(resultSetProp.getString(IdPManagementConstants.SQLConstants.PROPERTY_VALUE));
                    properties.add(property);
                }
                federatedAuthenticatorConfig.setProperties(properties.toArray(new Property[properties.size()]));
            }
        }
    }

    private void resolveOtpConnectorProperties(
            Map<String, IdentityProviderProperty> propertiesFromConnectors) throws ConnectorException{

        resolveOTPPattern(propertiesFromConnectors, "SelfRegistration");
        resolveOTPPattern(propertiesFromConnectors,
                "Recovery.Notification.Password");
        resolveOTPPattern(propertiesFromConnectors,
                "LiteRegistration");
    }

    private void resolveOTPPattern(
            Map<String, IdentityProviderProperty> propertiesFromConnectors, String connectorName) throws ConnectorException{

        String useUppercaseProperty = connectorName + ".OTP.UseUppercaseCharactersInOTP";
        String useLowercaseProperty = connectorName + ".OTP.UseLowercaseCharactersInOTP";
        String useNumericProperty = connectorName + ".OTP.UseNumbersInOTP";
        String otpLengthProperty = connectorName + ".OTP.OTPLength";
        String otpRegexProperty = connectorName + ".SMSOTP.Regex";
        if (connectorName.equals("Recovery.Notification.Password")) {
            otpRegexProperty = connectorName + ".smsOtp.Regex";
        }
        String validOTPRegex = "^\\[((a-z)|(A-Z)|(0-9)){1,3}\\]\\{[0-9]+\\}$";

        if(!propertiesFromConnectors.containsKey(useUppercaseProperty) || !propertiesFromConnectors.containsKey(useLowercaseProperty) ||
                !propertiesFromConnectors.containsKey(useNumericProperty) || !propertiesFromConnectors.containsKey(otpRegexProperty)) {
            return;
        }
        String useUppercase = propertiesFromConnectors.get(useUppercaseProperty).getValue();
        String useLowercase = propertiesFromConnectors.get(useLowercaseProperty).getValue();
        String useNumeric = propertiesFromConnectors.get(useNumericProperty).getValue();
        String otpRegex = propertiesFromConnectors.get(otpRegexProperty).getValue();

        if (!useUppercase.isEmpty() && !useLowercase.isEmpty() && !useNumeric.isEmpty()) {
            propertiesFromConnectors.get(useUppercaseProperty).setValue(String.valueOf(Boolean.parseBoolean(useUppercase)));
            propertiesFromConnectors.get(useLowercaseProperty).setValue(String.valueOf(Boolean.parseBoolean(useLowercase)));
            propertiesFromConnectors.get(useNumericProperty).setValue(String.valueOf(Boolean.parseBoolean(useNumeric)));
            return;
        }
        if (otpRegex.isEmpty() || !Pattern.matches(validOTPRegex,otpRegex)) {
            propertiesFromConnectors.get(useUppercaseProperty).setValue(Boolean.TRUE.toString());
            propertiesFromConnectors.get(useLowercaseProperty).setValue(Boolean.TRUE.toString());
            propertiesFromConnectors.get(useNumericProperty).setValue(Boolean.TRUE.toString());
            return;
        }
        setOldOTPRegexValue(propertiesFromConnectors, useUppercaseProperty, useLowercaseProperty, useNumericProperty,
                otpRegexProperty, otpLengthProperty);
    }

    private void setOldOTPRegexValue(
            Map<String, IdentityProviderProperty> propertiesFromConnectors, String useUppercaseProperty,
            String useLowercaseProperty, String useNumericProperty, String otpRegexProperty, String otpLengthProperty) {

        String charsRegex = propertiesFromConnectors.get(otpRegexProperty).getValue().replaceAll("[{].*", "");
        int otpLength = Integer.parseInt(propertiesFromConnectors.get(otpRegexProperty).getValue().
                replaceAll(".*[{]", "").replaceAll("}", ""));
        if (charsRegex.contains("A-Z")) {
            propertiesFromConnectors.get(useUppercaseProperty).setValue(Boolean.TRUE.toString());
        } else {
            propertiesFromConnectors.get(useUppercaseProperty).setValue(Boolean.FALSE.toString());
        }
        if (charsRegex.contains("a-z")) {
            propertiesFromConnectors.get(useLowercaseProperty).setValue(Boolean.TRUE.toString());
        } else {
            propertiesFromConnectors.get(useLowercaseProperty).setValue(Boolean.FALSE.toString());
        }
        if (charsRegex.contains("0-9")) {
            propertiesFromConnectors.get(useNumericProperty).setValue(Boolean.TRUE.toString());
        } else {
            propertiesFromConnectors.get(useNumericProperty).setValue(Boolean.FALSE.toString());
        }
        propertiesFromConnectors.get(otpLengthProperty).setValue(Integer.toString(otpLength));
    }

    private void performConfigCorrectionForPasswordRecoveryConfigs(Connection dbConnection, int tenantId,
                                       int idpId, List<IdentityProviderProperty> idpProperties) throws SQLException {

        // Enable all recovery options when Recovery.Notification.Password.Enable value is set as enabled.
        // This keeps functionality consistent with previous API versions for migrating customers.
        idpProperties.stream().filter(
                idp -> IdPManagementConstants.EMAIL_LINK_PASSWORD_RECOVERY_PROPERTY.equals(idp.getName())).findFirst()
                .ifPresentOrElse(
                        emailLinkProperty -> emailLinkProperty.setValue(String.valueOf(true)),
                        () -> {
                            IdentityProviderProperty identityProviderProperty = new IdentityProviderProperty();
                            identityProviderProperty.setName(
                                    IdPManagementConstants.EMAIL_LINK_PASSWORD_RECOVERY_PROPERTY);
                            identityProviderProperty.setValue("true");
                            idpProperties.add(identityProviderProperty);
                        });
        if (Boolean.parseBoolean(IdentityUtil.getProperty(ENABLE_SMS_OTP_IF_RECOVERY_NOTIFICATION_ENABLED))) {
            idpProperties.stream().filter(
                    idp -> IdPManagementConstants.SMS_OTP_PASSWORD_RECOVERY_PROPERTY.equals(idp.getName())).findFirst()
                    .ifPresentOrElse(
                            smsOtpProperty -> smsOtpProperty.setValue(String.valueOf(true)),
                            () -> {
                                IdentityProviderProperty identityProviderProperty = new IdentityProviderProperty();
                                identityProviderProperty.setName(
                                        IdPManagementConstants.SMS_OTP_PASSWORD_RECOVERY_PROPERTY);
                                identityProviderProperty.setValue("true");
                                idpProperties.add(identityProviderProperty);
                            });
        }
        updateIdentityProviderProperties(dbConnection, idpId, idpProperties, tenantId);
    }

    private void performConfigCorrectionForAdminForcedPasswordResetConfigs(
            List<IdentityProviderProperty> idpProperties) {

        /*
         * Enable email link option as the default option when all other options are disabled.
         * This config value will not update the database and only do the correction when getting the idp properties.
         * */
        idpProperties.stream().filter(
                        idp -> IdPManagementConstants.ENABLE_ADMIN_PASSWORD_RESET_EMAIL_LINK_PROPERTY.equals(idp.getName()))
                .findFirst()
                .ifPresentOrElse(
                        adminForcedPasswordResetProperty -> adminForcedPasswordResetProperty.setValue(
                                String.valueOf(true)),
                        () -> {
                            IdentityProviderProperty identityProviderProperty = new IdentityProviderProperty();
                            identityProviderProperty.setName(
                                    IdPManagementConstants.ENABLE_ADMIN_PASSWORD_RESET_EMAIL_LINK_PROPERTY);
                            identityProviderProperty.setValue(String.valueOf(true));
                            idpProperties.add(identityProviderProperty);
                        });
    }

    private FederatedAuthenticatorConfig createFederatedAuthenticatorConfig(AuthenticatorPropertyConstants.DefinedByType
                                                                                   definedByType) {

        if (definedByType == AuthenticatorPropertyConstants.DefinedByType.SYSTEM) {
            return new FederatedAuthenticatorConfig();
        }
        return new UserDefinedFederatedAuthenticatorConfig();
    }

    private void performConfigCorrectionForUsernameRecoveryConfigs(Connection dbConnection, int tenantId, int idpId,
                                                                   List<IdentityProviderProperty> idpProperties)
            throws SQLException {

        /*
         Enable all recovery options when Recovery.Notification.Username.Enable value is true and
         OnDemandConfig.OnInitialUse.EnableSMSUsernameRecoveryIfConnectorEnabled config is enabled in the toml.
         This keeps functionality consistent with previous API versions for migrating customers.
        */

        idpProperties.stream().filter(
                        idp -> IdPManagementConstants.
                                EMAIL_USERNAME_RECOVERY_PROPERTY.equals(idp.getName())).findFirst()
                .ifPresentOrElse(
                        emailProperty -> emailProperty.setValue(String.valueOf(true)),
                        () -> {
                            IdentityProviderProperty identityProviderProperty = new IdentityProviderProperty();
                            identityProviderProperty.setName(
                                    IdPManagementConstants.EMAIL_USERNAME_RECOVERY_PROPERTY);
                            identityProviderProperty.setValue(String.valueOf(true));
                            idpProperties.add(identityProviderProperty);
                        });
        if (Boolean.parseBoolean(IdentityUtil.getProperty(ENABLE_SMS_USERNAME_RECOVERY_IF_CONNECTOR_ENABLED))) {
            idpProperties.stream().filter(
                            idp -> IdPManagementConstants.
                                    SMS_USERNAME_RECOVERY_PROPERTY.equals(idp.getName())).findFirst()
                    .ifPresentOrElse(
                            smsProperty -> smsProperty.setValue(String.valueOf(true)),
                            () -> {
                                IdentityProviderProperty identityProviderProperty = new IdentityProviderProperty();
                                identityProviderProperty.setName(
                                        IdPManagementConstants.SMS_USERNAME_RECOVERY_PROPERTY);
                                identityProviderProperty.setValue(String.valueOf(true));
                                idpProperties.add(identityProviderProperty);
                            });
        }
        updateIdentityProviderProperties(dbConnection, idpId, idpProperties, tenantId);
    }
}
