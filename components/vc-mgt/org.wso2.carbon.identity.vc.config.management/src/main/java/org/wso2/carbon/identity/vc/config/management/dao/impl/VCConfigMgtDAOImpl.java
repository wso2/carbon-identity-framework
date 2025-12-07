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

package org.wso2.carbon.identity.vc.config.management.dao.impl;

import org.apache.commons.collections.CollectionUtils;
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.vc.config.management.constant.SQLConstants;
import org.wso2.carbon.identity.vc.config.management.constant.VCConfigManagementConstants;
import org.wso2.carbon.identity.vc.config.management.dao.VCConfigMgtDAO;
import org.wso2.carbon.identity.vc.config.management.exception.VCConfigMgtClientException;
import org.wso2.carbon.identity.vc.config.management.exception.VCConfigMgtException;
import org.wso2.carbon.identity.vc.config.management.exception.VCConfigMgtServerException;
import org.wso2.carbon.identity.vc.config.management.model.VCCredentialConfiguration;
import org.wso2.carbon.identity.vc.config.management.util.VCConfigFilterQueryBuilder;
import org.wso2.carbon.identity.vc.config.management.util.VCConfigFilterUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.wso2.carbon.identity.vc.config.management.constant.VCConfigManagementConstants.AFTER;
import static org.wso2.carbon.identity.vc.config.management.constant.VCConfigManagementConstants.BEFORE;

/**
 * JDBC implementation of {@link VCConfigMgtDAO}.
 */
public class VCConfigMgtDAOImpl implements VCConfigMgtDAO {

    @Override
    public List<VCCredentialConfiguration> list(int tenantId) throws VCConfigMgtException {

        List<VCCredentialConfiguration> results = new ArrayList<>();
        String sql = SQLConstants.LIST_CONFIGS;
        try (Connection conn = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tenantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(buildConfigurationListItem(rs));
                }
            }
        } catch (SQLException e) {
            throw new VCConfigMgtServerException(
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_RETRIEVAL_ERROR.getCode(),
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_RETRIEVAL_ERROR.getMessage(), e);
        }
        return results;
    }

    @Override
    public List<VCCredentialConfiguration> list(Integer limit, Integer tenantId, String sortOrder,
                                                List<ExpressionNode> expressionNodes) throws VCConfigMgtException {

        List<VCCredentialConfiguration> results = new ArrayList<>();
        try {
            VCConfigFilterQueryBuilder filterQueryBuilder =
                    VCConfigFilterUtil.getFilterQueryBuilder(expressionNodes);
            Map<Integer, String> filterAttributeValue = filterQueryBuilder.getFilterAttributeValue();

            try (Connection conn = IdentityDatabaseUtil.getDBConnection(false)) {
                String databaseName = conn.getMetaData().getDatabaseProductName();
                String sqlStmt = buildGetConfigurationsSqlStatement(databaseName, tenantId,
                        filterQueryBuilder.getFilterQuery(), sortOrder, limit);
                
                try (PreparedStatement ps = conn.prepareStatement(sqlStmt)) {
                    if (filterAttributeValue != null) {
                        for (Map.Entry<Integer, String> entry : filterAttributeValue.entrySet()) {
                            ps.setString(entry.getKey(), entry.getValue());
                        }
                    }
                    
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            VCCredentialConfiguration cfg = buildConfigurationListItem(rs);
                            cfg.setCursorKey(rs.getInt("CURSOR_KEY"));
                            results.add(cfg);
                        }
                    }
                }
            }
        } catch (VCConfigMgtClientException e) {
            throw e;
        } catch (SQLException e) {
            throw new VCConfigMgtServerException(
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_RETRIEVAL_ERROR.getCode(),
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_RETRIEVAL_ERROR.getMessage(), e);
        }
        return results;
    }

    @Override
    public Integer getConfigurationsCount(Integer tenantId, List<ExpressionNode> expressionNodes)
            throws VCConfigMgtException {

        try {
            // Remove after/before from expression nodes for count query.
            List<ExpressionNode> expressionNodesCopy = new ArrayList<>(expressionNodes);
            expressionNodesCopy.removeIf(expressionNode ->
                    AFTER.equals(expressionNode.getAttributeValue()) ||
                    BEFORE.equals(expressionNode.getAttributeValue()));

            VCConfigFilterQueryBuilder filterQueryBuilder =
                    VCConfigFilterUtil.getFilterQueryBuilder(expressionNodesCopy);
            Map<Integer, String> filterAttributeValue = filterQueryBuilder.getFilterAttributeValue();

            String sqlStmt = SQLConstants.GET_VC_CONFIGS_COUNT + filterQueryBuilder.getFilterQuery() +
                    SQLConstants.GET_VC_CONFIGS_COUNT_TAIL;

            try (Connection conn = IdentityDatabaseUtil.getDBConnection(false);
                 PreparedStatement ps = conn.prepareStatement(sqlStmt)) {
                
                if (filterAttributeValue != null) {
                    for (Map.Entry<Integer, String> entry : filterAttributeValue.entrySet()) {
                        ps.setString(entry.getKey(), entry.getValue());
                    }
                }
                ps.setInt((filterAttributeValue != null ? filterAttributeValue.size() : 0) + 1, tenantId);
                
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (VCConfigMgtClientException e) {
            throw e;
        } catch (SQLException e) {
            throw new VCConfigMgtServerException(
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_RETRIEVAL_ERROR.getCode(),
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_RETRIEVAL_ERROR.getMessage(), e);
        }
        return 0;
    }

    @Override
    public VCCredentialConfiguration get(String id, int tenantId) throws VCConfigMgtException {

        String sql = SQLConstants.GET_CONFIG_BY_ID;
        try (Connection conn = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setInt(2, tenantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return buildConfiguration(rs, conn);
                }
            }
        } catch (SQLException e) {
            throw new VCConfigMgtServerException(
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_RETRIEVAL_ERROR.getCode(),
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_RETRIEVAL_ERROR.getMessage(), e);
        }
        return null;
    }

    @Override
    public VCCredentialConfiguration getByIdentifier(String identifier, int tenantId) throws VCConfigMgtException {
        String sql = SQLConstants.GET_CONFIG_BY_IDENTIFIER;
        try (Connection conn = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, identifier);
            ps.setInt(2, tenantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return buildConfiguration(rs, conn);
                }
            }
        } catch (SQLException e) {
            throw new VCConfigMgtServerException(
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_RETRIEVAL_ERROR.getCode(),
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_RETRIEVAL_ERROR.getMessage(), e);
        }
        return null;
    }

    @Override
    public VCCredentialConfiguration getByOfferId(String offerId, int tenantId) throws VCConfigMgtException {

        String sql = SQLConstants.GET_CONFIG_BY_OFFER_ID;
        try (Connection conn = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, offerId);
            ps.setInt(2, tenantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return buildConfiguration(rs, conn);
                }
            }
        } catch (SQLException e) {
            throw new VCConfigMgtServerException(
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_RETRIEVAL_ERROR.getCode(),
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_RETRIEVAL_ERROR.getMessage(), e);
        }
        return null;
    }

    @Override
    public boolean existsByIdentifier(String identifier, int tenantId) throws VCConfigMgtException {

        String sql = SQLConstants.EXISTS_BY_IDENTIFIER;
        try (Connection conn = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tenantId);
            ps.setString(2, identifier);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new VCConfigMgtServerException(
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_RETRIEVAL_ERROR.getCode(),
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_RETRIEVAL_ERROR.getMessage(), e);
        }
    }

    @Override
    public VCCredentialConfiguration add(VCCredentialConfiguration configuration, int tenantId)
            throws VCConfigMgtException {

        String insertCfg = SQLConstants.INSERT_CONFIG;
        try (Connection conn = IdentityDatabaseUtil.getDBConnection(true);
             PreparedStatement ps = conn.prepareStatement(insertCfg)) {
            try {
                String id = UUID.randomUUID().toString();
                ps.setString(1, id);
                ps.setInt(2, tenantId);
                ps.setString(3, configuration.getIdentifier());
                ps.setString(4, configuration.getDisplayName());
                ps.setString(5, configuration.getScope());
                ps.setString(6, configuration.getFormat());
                ps.setString(7, configuration.getSigningAlgorithm());
                ps.setString(8, configuration.getType());
                ps.setInt(9, configuration.getExpiresIn());
                ps.setString(10, configuration.getOfferId());
                ps.executeUpdate();

                if (CollectionUtils.isNotEmpty(configuration.getClaims())) {
                    addClaims(conn, id, configuration.getClaims());
                }

                IdentityDatabaseUtil.commitTransaction(conn);
                return get(id, tenantId);
            } catch (SQLException | VCConfigMgtException e) {
                IdentityDatabaseUtil.rollbackTransaction(conn);
                if (e instanceof VCConfigMgtException) {
                    throw (VCConfigMgtException) e;
                }
                throw new VCConfigMgtServerException(
                        VCConfigManagementConstants.ErrorMessages.ERROR_CODE_PERSISTENCE_ERROR.getCode(),
                        VCConfigManagementConstants.ErrorMessages.ERROR_CODE_PERSISTENCE_ERROR.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new VCConfigMgtServerException(
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_PERSISTENCE_ERROR.getCode(),
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_PERSISTENCE_ERROR.getMessage(), e);
        }
    }

    @Override
    public VCCredentialConfiguration update(String id, VCCredentialConfiguration configuration, int tenantId)
            throws VCConfigMgtException {

        String updateCfg = SQLConstants.UPDATE_CONFIG;
        try (Connection conn = IdentityDatabaseUtil.getDBConnection(true);
             PreparedStatement ps = conn.prepareStatement(updateCfg)) {
            try {
                ps.setString(1, configuration.getIdentifier());
                ps.setString(2, configuration.getDisplayName());
                ps.setString(3, configuration.getScope());
                ps.setString(4, configuration.getFormat());
                ps.setString(5, configuration.getSigningAlgorithm());
                ps.setString(6, configuration.getType());
                ps.setInt(7, configuration.getExpiresIn());
                ps.setString(8, configuration.getOfferId());
                ps.setInt(9, tenantId);
                ps.setString(10, id);
                int updated = ps.executeUpdate();
                if (updated == 0) {
                    throw new VCConfigMgtClientException(
                            VCConfigManagementConstants.ErrorMessages.ERROR_CODE_CONFIG_NOT_FOUND.getCode(),
                            VCConfigManagementConstants.ErrorMessages.ERROR_CODE_CONFIG_NOT_FOUND.getMessage());
                }

                deleteClaims(conn, id);
                if (CollectionUtils.isNotEmpty(configuration.getClaims())) {
                    addClaims(conn, id, configuration.getClaims());
                }

                IdentityDatabaseUtil.commitTransaction(conn);
                return get(id, tenantId);
            } catch (SQLException | VCConfigMgtException e) {
                IdentityDatabaseUtil.rollbackTransaction(conn);
                if (e instanceof VCConfigMgtException) {
                    throw (VCConfigMgtException) e;
                }
                throw new VCConfigMgtServerException(
                        VCConfigManagementConstants.ErrorMessages.ERROR_CODE_PERSISTENCE_ERROR.getCode(),
                        VCConfigManagementConstants.ErrorMessages.ERROR_CODE_PERSISTENCE_ERROR.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new VCConfigMgtServerException(
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_PERSISTENCE_ERROR.getCode(),
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_PERSISTENCE_ERROR.getMessage(), e);
        }
    }

    @Override
    public void delete(String id, int tenantId) throws VCConfigMgtException {

        String deleteCfg = SQLConstants.DELETE_CONFIG;
        try (Connection conn = IdentityDatabaseUtil.getDBConnection(true);
             PreparedStatement ps = conn.prepareStatement(deleteCfg)) {
            try {
                ps.setInt(1, tenantId);
                ps.setString(2, id);
                ps.executeUpdate();
                IdentityDatabaseUtil.commitTransaction(conn);
            } catch (SQLException e) {
                IdentityDatabaseUtil.rollbackTransaction(conn);
                throw new VCConfigMgtServerException(
                        VCConfigManagementConstants.ErrorMessages.ERROR_CODE_DELETION_ERROR.getCode(),
                        VCConfigManagementConstants.ErrorMessages.ERROR_CODE_DELETION_ERROR.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new VCConfigMgtServerException(
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_DELETION_ERROR.getCode(),
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_DELETION_ERROR.getMessage(), e);
        }
    }

    @Override
    public void updateOfferId(String configId, String offerId, int tenantId) throws VCConfigMgtException {

        String sql = SQLConstants.UPDATE_OFFER_ID;
        try (Connection conn = IdentityDatabaseUtil.getDBConnection(true);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            try {
                ps.setString(1, offerId);
                ps.setInt(2, tenantId);
                ps.setString(3, configId);
                int updated = ps.executeUpdate();
                if (updated == 0) {
                    throw new VCConfigMgtClientException(
                            VCConfigManagementConstants.ErrorMessages.ERROR_CODE_CONFIG_NOT_FOUND.getCode(),
                            VCConfigManagementConstants.ErrorMessages.ERROR_CODE_CONFIG_NOT_FOUND.getMessage());
                }
                IdentityDatabaseUtil.commitTransaction(conn);
            } catch (SQLException | VCConfigMgtException e) {
                IdentityDatabaseUtil.rollbackTransaction(conn);
                if (e instanceof VCConfigMgtException) {
                    throw (VCConfigMgtException) e;
                }
                throw new VCConfigMgtServerException(
                        VCConfigManagementConstants.ErrorMessages.ERROR_CODE_PERSISTENCE_ERROR.getCode(),
                        VCConfigManagementConstants.ErrorMessages.ERROR_CODE_PERSISTENCE_ERROR.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new VCConfigMgtServerException(
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_PERSISTENCE_ERROR.getCode(),
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_PERSISTENCE_ERROR.getMessage(), e);
        }
    }

    /**
     * Build SQL statement to get configurations with pagination.
     *
     * @param databaseName Database name.
     * @param tenantId     Tenant ID.
     * @param filterQuery  Filter query.
     * @param sortOrder    Sort order.
     * @param limit        Limit.
     * @return SQL statement to retrieve configurations.
     */
    private String buildGetConfigurationsSqlStatement(String databaseName, Integer tenantId, String filterQuery,
                                                      String sortOrder, Integer limit) {

        if (databaseName.contains(SQLConstants.MICROSOFT)) {
            return String.format(SQLConstants.GET_VC_CONFIGS_MSSQL, limit) + filterQuery +
                    String.format(SQLConstants.GET_VC_CONFIGS_TAIL_MSSQL, tenantId, sortOrder);
        } else if (databaseName.contains(SQLConstants.ORACLE)) {
            return SQLConstants.GET_VC_CONFIGS + filterQuery +
                    String.format(SQLConstants.GET_VC_CONFIGS_TAIL_ORACLE, tenantId, sortOrder, limit);
        }
        return SQLConstants.GET_VC_CONFIGS + filterQuery +
                String.format(SQLConstants.GET_VC_CONFIGS_TAIL, tenantId, sortOrder, limit);
    }

    /**
     * Build VC credential configuration from result set.
     *
     * @param rs   Result set
     * @param conn DB connection
     * @return VC credential configuration
     * @throws SQLException on SQL errors
     */
    private VCCredentialConfiguration buildConfiguration(ResultSet rs, Connection conn) throws SQLException {

        VCCredentialConfiguration cfg = new VCCredentialConfiguration();
        cfg.setId(rs.getString("ID"));
        cfg.setIdentifier(rs.getString("IDENTIFIER"));
        cfg.setDisplayName(rs.getString("DISPLAY_NAME"));
        cfg.setScope(rs.getString("SCOPE"));
        cfg.setFormat(rs.getString("FORMAT"));
        cfg.setSigningAlgorithm(rs.getString("SIGNING_ALG"));
        cfg.setType(rs.getString("TYPE"));
        int expiresIn = rs.getInt("EXPIRES_IN");
        if (!rs.wasNull()) {
            cfg.setExpiresIn(expiresIn);
        }
        String offerId = rs.getString("OFFER_ID");
        if (!rs.wasNull()) {
            cfg.setOfferId(offerId);
        }
        cfg.setClaims(getClaimsByConfigId(conn, cfg.getId()));
        return cfg;
    }

    /**
     * Build VC credential configuration from result set.
     *
     * @param rs Result set
     * @return VC credential configuration
     * @throws SQLException on SQL errors
     */
    private VCCredentialConfiguration buildConfigurationListItem(ResultSet rs) throws SQLException {

        VCCredentialConfiguration cfg = new VCCredentialConfiguration();
        cfg.setId(rs.getString("ID"));
        cfg.setIdentifier(rs.getString("IDENTIFIER"));
        cfg.setDisplayName(rs.getString("DISPLAY_NAME"));
        cfg.setScope(rs.getString("SCOPE"));
        return cfg;
    }

    /**
     * Get claims by configuration primary key.
     *
     * @param conn     DB connection
     * @param configId Configuration primary key
     * @return List of claim URIs
     * @throws SQLException on SQL errors
     */
    private List<String> getClaimsByConfigId(Connection conn, String configId) throws SQLException {

        String sql = SQLConstants.LIST_CLAIMS_BY_CONFIG_PK;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, configId);
            try (ResultSet rs = ps.executeQuery()) {
                List<String> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(rs.getString("CLAIM_URI"));
                }
                return list;
            }
        }
    }

    /**
     * Add claims for a configuration.
     *
     * @param conn     DB connection
     * @param configId Configuration primary key
     * @param claims   List of claim URIs
     * @throws SQLException on SQL errors
     */
    private void addClaims(Connection conn, String configId, List<String> claims) throws SQLException {

        String insert = SQLConstants.INSERT_CLAIM;
        try (PreparedStatement ps = conn.prepareStatement(insert)) {
            for (String claim : claims) {
                ps.setString(1, configId);
                ps.setString(2, claim);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /**
     * Delete claims for a configuration.
     *
     * @param conn     DB connection
     * @param configId Configuration primary key
     * @throws SQLException on SQL errors
     */
    private void deleteClaims(Connection conn, String configId) throws SQLException {

        try (PreparedStatement ps = conn.prepareStatement(SQLConstants.DELETE_CLAIMS_BY_CONFIG_PK)) {
            ps.setString(1, configId);
            ps.executeUpdate();
        }
    }
}
