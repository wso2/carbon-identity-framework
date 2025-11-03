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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.vc.config.management.constant.VCConfigManagementConstants;
import org.wso2.carbon.identity.vc.config.management.dao.SQLQueries;
import org.wso2.carbon.identity.vc.config.management.dao.VCConfigMgtDAO;
import org.wso2.carbon.identity.vc.config.management.exception.VCConfigMgtClientException;
import org.wso2.carbon.identity.vc.config.management.exception.VCConfigMgtException;
import org.wso2.carbon.identity.vc.config.management.exception.VCConfigMgtServerException;
import org.wso2.carbon.identity.vc.config.management.model.VCCredentialConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JDBC implementation of {@link VCConfigMgtDAO}.
 */
public class VCConfigMgtDAOImpl implements VCConfigMgtDAO {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public List<VCCredentialConfiguration> list(int tenantId) throws VCConfigMgtException {

        List<VCCredentialConfiguration> results = new ArrayList<>();
        String sql = SQLQueries.LIST_CONFIGS;
        try (Connection conn = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tenantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(buildConfiguration(rs, conn));
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
    public VCCredentialConfiguration get(String id, int tenantId) throws VCConfigMgtException {

        String sql = SQLQueries.GET_CONFIG_BY_ID;
        try (Connection conn = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tenantId);
            ps.setString(2, id);
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
    public VCCredentialConfiguration getByConfigId(String configId, int tenantId) throws VCConfigMgtException {
        String sql = SQLQueries.GET_CONFIG_BY_CONFIG_ID;
        try (Connection conn = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tenantId);
            ps.setString(2, configId);
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

        String sql = SQLQueries.EXISTS_BY_IDENTIFIER;
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
    public boolean existsByConfigurationId(String configurationId, int tenantId) throws VCConfigMgtException {

        String sql = SQLQueries.EXISTS_BY_CONFIGURATION_ID;
        try (Connection conn = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tenantId);
            ps.setString(2, configurationId);
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

        String insertCfg = SQLQueries.INSERT_CONFIG;
        try (Connection conn = IdentityDatabaseUtil.getDBConnection(true);
             PreparedStatement ps = conn.prepareStatement(insertCfg)) {
            try {
                String serializedMetadata = OBJECT_MAPPER.writeValueAsString(configuration.getMetadata());
                String id = UUID.randomUUID().toString();
                ps.setString(1, id);
                ps.setInt(2, tenantId);
                ps.setString(3, configuration.getIdentifier());
                ps.setString(4, configuration.getConfigurationId());
                ps.setString(5, configuration.getScope());
                ps.setString(6, configuration.getFormat());
                ps.setString(7, configuration.getSigningAlgorithm());
                ps.setString(8, configuration.getType());
                ps.setString(9, configuration.getMetadata() != null ? serializedMetadata : null);
                ps.setInt(10, configuration.getExpiryIn());
                ps.executeUpdate();

                if (CollectionUtils.isNotEmpty(configuration.getClaims())) {
                    addClaims(conn, id, configuration.getClaims());
                }

                IdentityDatabaseUtil.commitTransaction(conn);
                return getByConfigId(id, tenantId);
            } catch (SQLException | VCConfigMgtException e) {
                IdentityDatabaseUtil.rollbackTransaction(conn);
                if (e instanceof VCConfigMgtException) {
                    throw (VCConfigMgtException) e;
                }
                throw new VCConfigMgtServerException(
                        VCConfigManagementConstants.ErrorMessages.ERROR_CODE_PERSISTENCE_ERROR.getCode(),
                        VCConfigManagementConstants.ErrorMessages.ERROR_CODE_PERSISTENCE_ERROR.getMessage(), e);
            } catch (JsonProcessingException e) {
                throw new VCConfigMgtServerException(
                        VCConfigManagementConstants.ErrorMessages.ERROR_CODE_PERSISTENCE_ERROR.getCode(),
                        "Error serializing credential metadata.", e);
            }
        } catch (SQLException e) {
            throw new VCConfigMgtServerException(
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_PERSISTENCE_ERROR.getCode(),
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_PERSISTENCE_ERROR.getMessage(), e);
        }
    }

    @Override
    public VCCredentialConfiguration update(String configId, VCCredentialConfiguration configuration, int tenantId)
            throws VCConfigMgtException {

        String updateCfg = SQLQueries.UPDATE_CONFIG;
        try (Connection conn = IdentityDatabaseUtil.getDBConnection(true);
             PreparedStatement ps = conn.prepareStatement(updateCfg)) {
            try {
                String serializedMetadata = OBJECT_MAPPER.writeValueAsString(configuration.getMetadata());
                ps.setString(1, configuration.getIdentifier());
                ps.setString(2, configuration.getConfigurationId());
                ps.setString(3, configuration.getScope());
                ps.setString(4, configuration.getFormat());
                ps.setString(5, configuration.getSigningAlgorithm());
                ps.setString(6, configuration.getType());
                ps.setString(7, configuration.getMetadata() != null ? serializedMetadata : null);
                ps.setInt(8, configuration.getExpiryIn());
                ps.setInt(9, tenantId);
                ps.setString(10, configId);
                int updated = ps.executeUpdate();
                if (updated == 0) {
                    throw new VCConfigMgtClientException(
                            VCConfigManagementConstants.ErrorMessages.ERROR_CODE_CONFIG_NOT_FOUND.getCode(),
                            VCConfigManagementConstants.ErrorMessages.ERROR_CODE_CONFIG_NOT_FOUND.getMessage());
                }

                deleteClaims(conn, configId);
                if (CollectionUtils.isNotEmpty(configuration.getClaims())) {
                    addClaims(conn, configId, configuration.getClaims());
                }

                IdentityDatabaseUtil.commitTransaction(conn);
                return getByConfigId(configId, tenantId);
            } catch (SQLException | VCConfigMgtException e) {
                IdentityDatabaseUtil.rollbackTransaction(conn);
                if (e instanceof VCConfigMgtException) {
                    throw (VCConfigMgtException) e;
                }
                throw new VCConfigMgtServerException(
                        VCConfigManagementConstants.ErrorMessages.ERROR_CODE_PERSISTENCE_ERROR.getCode(),
                        VCConfigManagementConstants.ErrorMessages.ERROR_CODE_PERSISTENCE_ERROR.getMessage(), e);
            } catch (JsonProcessingException e) {
                throw new VCConfigMgtServerException(
                        VCConfigManagementConstants.ErrorMessages.ERROR_CODE_PERSISTENCE_ERROR.getCode(),
                        "Error serializing credential metadata.", e);
            }
        } catch (SQLException e) {
            throw new VCConfigMgtServerException(
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_PERSISTENCE_ERROR.getCode(),
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_PERSISTENCE_ERROR.getMessage(), e);
        }
    }

    @Override
    public void delete(String configId, int tenantId) throws VCConfigMgtException {

        String deleteCfg = SQLQueries.DELETE_CONFIG;
        try (Connection conn = IdentityDatabaseUtil.getDBConnection(true);
             PreparedStatement ps = conn.prepareStatement(deleteCfg)) {
            try {
                ps.setInt(1, tenantId);
                ps.setString(2, configId);
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

    /**
     * Build VC credential configuration from result set.
     *
     * @param rs Result set
     * @param conn DB connection
     * @return VC credential configuration
     * @throws SQLException on SQL errors
     */
    private VCCredentialConfiguration buildConfiguration(ResultSet rs, Connection conn) throws SQLException {

        VCCredentialConfiguration cfg = new VCCredentialConfiguration();
        cfg.setId(rs.getString("ID"));
        cfg.setIdentifier(rs.getString("IDENTIFIER"));
        cfg.setConfigurationId(rs.getString("CONFIGURATION_ID"));
        cfg.setScope(rs.getString("SCOPE"));
        cfg.setFormat(rs.getString("FORMAT"));
        cfg.setSigningAlgorithm(rs.getString("SIGNING_ALG"));
        cfg.setType(rs.getString("TYPE"));
        // Deserialize metadata
        String metadataStr = rs.getString("METADATA");
        if (!rs.wasNull() && metadataStr != null) {
            try {
                VCCredentialConfiguration.Metadata metadata =
                        OBJECT_MAPPER.readValue(metadataStr, VCCredentialConfiguration.Metadata.class);
                cfg.setMetadata(metadata);
            } catch (JsonProcessingException e) {
                throw new SQLException("Error deserializing credential metadata.", e);
            }
        }
        int expiry = rs.getInt("EXPIRY_IN");
        if (!rs.wasNull()) {
            cfg.setExpiryIn(expiry);
        }
        cfg.setClaims(getClaimsByConfigId(conn, cfg.getId()));
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

        String sql = SQLQueries.LIST_CLAIMS_BY_CONFIG_PK;
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

        String insert = SQLQueries.INSERT_CLAIM;
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

        try (PreparedStatement ps = conn.prepareStatement(SQLQueries.DELETE_CLAIMS_BY_CONFIG_PK)) {
            ps.setString(1, configId);
            ps.executeUpdate();
        }
    }
}
