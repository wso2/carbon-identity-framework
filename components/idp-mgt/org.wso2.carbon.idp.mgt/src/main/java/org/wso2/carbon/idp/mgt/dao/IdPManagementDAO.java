/*
 * Copyright (c) 2014 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.application.common.model.Claim;
import org.wso2.carbon.identity.application.common.model.ClaimConfig;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.IdentityProviderProperty;
import org.wso2.carbon.identity.application.common.model.JustInTimeProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.LocalRole;
import org.wso2.carbon.identity.application.common.model.PermissionsAndRoleConfig;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.ProvisioningConnectorConfig;
import org.wso2.carbon.identity.application.common.model.RoleMapping;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationManagementUtil;
import org.wso2.carbon.identity.core.ConnectorConfig;
import org.wso2.carbon.identity.core.ConnectorException;
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementClientException;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementServerException;
import org.wso2.carbon.idp.mgt.internal.IdpMgtServiceComponentHolder;
import org.wso2.carbon.idp.mgt.model.ConnectedAppsResult;
import org.wso2.carbon.idp.mgt.model.FilterQueryBuilder;
import org.wso2.carbon.idp.mgt.util.IdPManagementConstants;
import org.wso2.carbon.idp.mgt.util.IdPManagementUtil;
import org.wso2.carbon.utils.DBUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.cert.CertificateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.core.util.JdbcUtils.isH2DB;
import static org.wso2.carbon.idp.mgt.util.IdPManagementConstants.RESET_PROVISIONING_ENTITIES_ON_CONFIG_UPDATE;
import static org.wso2.carbon.idp.mgt.util.IdPManagementConstants.SQLQueries.GET_IDP_NAME_BY_RESOURCE_ID_SQL;
import static org.wso2.carbon.idp.mgt.util.IdPManagementConstants.TEMPLATE_ID_IDP_PROPERTY_DISPLAY_NAME;
import static org.wso2.carbon.idp.mgt.util.IdPManagementConstants.TEMPLATE_ID_IDP_PROPERTY_NAME;
import static org.wso2.carbon.idp.mgt.util.IdPManagementConstants.MySQL;
import static org.wso2.carbon.idp.mgt.util.IdPManagementConstants.ID;

/**
 * This class is used to access the data storage to retrieve and store identity provider configurations.
 */
public class IdPManagementDAO {

    private static final Log log = LogFactory.getLog(IdPManagementDAO.class);

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
                            Integer.parseInt(identityProvider.getId()));
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
                            dbConnection, Integer.parseInt(identityProvider.getId()));
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
            prepStmt.setInt(filterAttributeValueSize + 2, offset + limit);
            prepStmt.setInt(filterAttributeValueSize + 3, offset);
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
                    Integer.parseInt(resultSet.getString("ID")));
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

        String sqlStmt = IdPManagementConstants.SQLQueries.GET_IDP_COUNT_SQL;
        int countOfFilteredIdp = 0;
        FilterQueryBuilder filterQueryBuilder = new FilterQueryBuilder();
        appendFilterQuery(expressionNode, filterQueryBuilder);
        Map<Integer, String> filterAttributeValue = filterQueryBuilder.getFilterAttributeValue();
        sqlStmt = sqlStmt + filterQueryBuilder.getFilterQuery() +
                IdPManagementConstants.SQLQueries.GET_IDP_COUNT_SQL_TAIL;
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
                            attributeName = IdPManagementConstants.NAME;
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
    private List<IdentityProviderProperty> getIdentityPropertiesByIdpId(Connection dbConnection, int idpId)
            throws SQLException {

        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        List<IdentityProviderProperty> idpProperties = new ArrayList<IdentityProviderProperty>();

        try {
            String sqlStmt = isH2DB() ? IdPManagementConstants.SQLQueries.GET_IDP_METADATA_BY_IDP_ID_H2 :
                    IdPManagementConstants.SQLQueries.GET_IDP_METADATA_BY_IDP_ID;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, idpId);
            rs = prepStmt.executeQuery();
            while (rs.next()) {
                IdentityProviderProperty property = new IdentityProviderProperty();
                property.setName(rs.getString("NAME"));
                property.setValue(rs.getString("VALUE"));
                property.setDisplayName(rs.getString("DISPLAY_NAME"));
                idpProperties.add(property);
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
            String sqlStmt = isH2DB() ? IdPManagementConstants.SQLQueries.ADD_IDP_METADATA_H2 :
                    IdPManagementConstants.SQLQueries.ADD_IDP_METADATA;
            prepStmt = dbConnection.prepareStatement(sqlStmt);

            for (IdentityProviderProperty property : properties) {
                if (property.getValue() != null) {
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
            throws IdentityProviderManagementClientException, SQLException {

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
                FederatedAuthenticatorConfig authnConfig = new FederatedAuthenticatorConfig();
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

                sqlStmt = IdPManagementConstants.SQLQueries.GET_IDP_AUTH_PROPS_SQL;
                prepStmt2 = dbConnection.prepareStatement(sqlStmt);
                prepStmt2.setInt(1, authnId);
                proprs = prepStmt2.executeQuery();
                Set<Property> properties = new HashSet<Property>();
                while (proprs.next()) {
                    Property property = new Property();
                    property.setName(proprs.getString("PROPERTY_KEY"));
                    property.setValue(proprs.getString("PROPERTY_VALUE"));
                    if ((IdPManagementConstants.IS_TRUE_VALUE).equals(proprs.getString("IS_SECRET"))) {
                        property.setConfidential(true);
                    }
                    properties.add(property);
                }
                authnConfig.setProperties(properties.toArray(new Property[properties.size()]));
                federatedAuthenticatorConfigs.add(authnConfig);
            }

            return federatedAuthenticatorConfigs
                    .toArray(new FederatedAuthenticatorConfig[federatedAuthenticatorConfigs.size()]);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(null, proprs, prepStmt2);
            IdentityDatabaseUtil.closeAllConnections(null, rs, prepStmt1);
        }
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
                            String isSecret = rs2.getString("IS_SECRET");

                            property.setName(name);
                            if (propertyType != null && IdentityApplicationConstants.ConfigElements.
                                    PROPERTY_TYPE_BLOB.equals(propertyType.trim())) {
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

        return getIDP(dbConnection, idPName, -1, null, tenantId, tenantDomain);
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

        return getIDP(dbConnection, null, idpId, null, tenantId, tenantDomain);

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

        return getIDP(dbConnection, null, -1, resourceId, tenantId, tenantDomain);
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

                List<IdentityProviderProperty> propertyList = filterIdenityProperties(federatedIdp,
                        getIdentityPropertiesByIdpId(dbConnection, idpId));

                if (IdentityApplicationConstants.RESIDENT_IDP_RESERVED_NAME.equals(idPName)) {
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
        } finally {
            if (dbConnectionInitialized) {
                IdentityDatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
            } else {
                IdentityDatabaseUtil.closeAllConnections(null, rs, prepStmt);
            }
        }
    }

    /**
     * To filter out the properties related with just in time provisioning and to send back only the remaning IDP
     * properties.
     *
     * @param federatedIdp               Relevant IDP.
     * @param identityProviderProperties Identity Provider Properties.
     * @return identity provider properties after removing the relevant JIT specific properties.
     */
    private List<IdentityProviderProperty> filterIdenityProperties(IdentityProvider federatedIdp,
                                                                   List<IdentityProviderProperty>
                                                                           identityProviderProperties) {

        JustInTimeProvisioningConfig justInTimeProvisioningConfig = federatedIdp.getJustInTimeProvisioningConfig();

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
                }
            });
        }
        String templateId = getTemplateId(identityProviderProperties);
        if (StringUtils.isNotEmpty(templateId)) {
            federatedIdp.setTemplateId(templateId);
        }
        identityProviderProperties.removeIf(identityProviderProperty -> (
                identityProviderProperty.getName().equals(IdPManagementConstants.MODIFY_USERNAME_ENABLED)
                        || identityProviderProperty.getName()
                        .equals(IdPManagementConstants.PASSWORD_PROVISIONING_ENABLED) || identityProviderProperty
                        .getName().equals(IdPManagementConstants.PROMPT_CONSENT_ENABLED)));
        return identityProviderProperties;
    }

    /**
     * Add templateId property for the IDP.
     *
     * @param identityProviderProperties List of IdP properties.
     * @param identityProvider           Identity provider.
     */
    private void addTemplateIdProperty(List<IdentityProviderProperty> identityProviderProperties,
                                                           IdentityProvider identityProvider) {

        if (StringUtils.isNotBlank(identityProvider.getTemplateId())) {
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

                List<IdentityProviderProperty> propertyList = filterIdenityProperties(federatedIdp,
                        getIdentityPropertiesByIdpId(dbConnection, Integer.parseInt(rs.getString("ID"))));
                if (IdentityApplicationConstants.RESIDENT_IDP_RESERVED_NAME.equals(idPName)) {
                    propertyList = resolveConnectorProperties(propertyList, tenantDomain);
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

                List<IdentityProviderProperty> propertyList = filterIdenityProperties(federatedIdp,
                        getIdentityPropertiesByIdpId(dbConnection, Integer.parseInt(rs.getString("ID"))));

                if (IdentityApplicationConstants.RESIDENT_IDP_RESERVED_NAME.equals(idPName)) {
                    propertyList = resolveConnectorProperties(propertyList, tenantDomain);
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
                    .getJustInTimeProvisioningConfig(), idpProperties);
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
                                                                 IdentityProviderProperty[] idpProperties) {

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

        if (justInTimeProvisioningConfig != null && justInTimeProvisioningConfig.isProvisioningEnabled()) {
            passwordProvisioningProperty
                    .setValue(String.valueOf(justInTimeProvisioningConfig.isPasswordProvisioningEnabled()));
            modifyUserNameProperty.setValue(String.valueOf(justInTimeProvisioningConfig.isModifyUserNameAllowed()));
            promptConsentProperty.setValue(String.valueOf(justInTimeProvisioningConfig.isPromptConsent()));
        }
        identityProviderProperties.add(passwordProvisioningProperty);
        identityProviderProperties.add(modifyUserNameProperty);
        identityProviderProperties.add(promptConsentProperty);
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

                // // update provisioning connectors.
                updateProvisioningConnectorConfigs(
                        newIdentityProvider.getProvisioningConnectorConfigs(), dbConnection, idpId,
                        tenantId);

                IdentityProviderProperty[] idpProperties = newIdentityProvider.getIdpProperties();
                if (isResidentIdP) {
                    idpProperties =
                            filterConnectorProperties(idpProperties,
                                    IdentityTenantUtil.getTenantDomain(tenantId))
                                    .toArray(new IdentityProviderProperty[0]);
                }
                List<IdentityProviderProperty> identityProviderProperties = getCombinedProperties
                        (newIdentityProvider.getJustInTimeProvisioningConfig(), idpProperties);
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
        try {
            IdentityProvider identityProvider = getIDPbyResourceId(dbConnection, resourceId, tenantId,
                    tenantDomain);
            if (identityProvider == null) {
                String msg = "Trying to delete non-existent Identity Provider with resource ID: %s in tenantDomain: %s";
                throw new IdentityProviderManagementException(String.format(msg, resourceId, tenantDomain));
            }
            deleteIdP(dbConnection, tenantId, null, resourceId);
            IdentityDatabaseUtil.commitTransaction(dbConnection);
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(dbConnection);
            throw new IdentityProviderManagementException("Error occurred while deleting Identity Provider of tenant "
                    + tenantDomain, e);
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
            prepStmt.setInt(3, offset);
            prepStmt.setInt(4, limit);
        } else if (databaseProductName.contains("Oracle")) {
            sqlQuery = IdPManagementConstants.SQLQueries.GET_CONNECTED_APPS_ORACLE;
            prepStmt = connection.prepareStatement(sqlQuery);
            prepStmt.setString(1, id);
            prepStmt.setString(2, id);
            prepStmt.setInt(3, offset + limit);
            prepStmt.setInt(4, offset);
        } else if (databaseProductName.contains("Microsoft")) {
            sqlQuery = IdPManagementConstants.SQLQueries.GET_CONNECTED_APPS_MSSQL;
            prepStmt = connection.prepareStatement(sqlQuery);
            prepStmt.setString(1, id);
            prepStmt.setString(2, id);
            prepStmt.setInt(3, offset);
            prepStmt.setInt(4, limit);
        } else if (databaseProductName.contains("PostgreSQL")) {
            sqlQuery = IdPManagementConstants.SQLQueries.GET_CONNECTED_APPS_POSTGRESSQL;
            prepStmt = connection.prepareStatement(sqlQuery);
            prepStmt.setString(1, id);
            prepStmt.setString(2, id);
            prepStmt.setInt(3, limit);
            prepStmt.setInt(4, offset);
        } else if (databaseProductName.contains("DB2")) {
            sqlQuery = IdPManagementConstants.SQLQueries.GET_CONNECTED_APPS_DB2SQL;
            prepStmt = connection.prepareStatement(sqlQuery);
            prepStmt.setString(1, id);
            prepStmt.setString(2, id);
            prepStmt.setInt(3, limit);
            prepStmt.setInt(4, offset);
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

        return new ArrayList<>(propertiesFromConnectors.values());
    }

    private List<IdentityProviderProperty> filterConnectorProperties(IdentityProviderProperty[] propertiesFromRequest,
                                                                     String tenantDomain)
            throws ConnectorException {

        Map<String, IdentityProviderProperty> propertiesFromConnectors = getConnectorProperties(tenantDomain);

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
}
