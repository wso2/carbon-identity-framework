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
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.vc.config.management.constant.VCConfigManagementConstants;
import org.wso2.carbon.identity.vc.config.management.dao.SQLQueries;
import org.wso2.carbon.identity.vc.config.management.dao.VCConfigMgtDAO;
import org.wso2.carbon.identity.vc.config.management.exception.VCConfigMgtClientException;
import org.wso2.carbon.identity.vc.config.management.exception.VCConfigMgtException;
import org.wso2.carbon.identity.vc.config.management.exception.VCConfigMgtServerException;
import org.wso2.carbon.identity.vc.config.management.model.ClaimMapping;
import org.wso2.carbon.identity.vc.config.management.model.VCCredentialConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link VCConfigMgtDAO}.
 */
public class VCConfigMgtDAOImpl implements VCConfigMgtDAO {

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
    public VCCredentialConfiguration getByConfigId(String configId, int tenantId) throws VCConfigMgtException {

        String sql = SQLQueries.GET_CONFIG_BY_ID;
        try (Connection conn = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int configPk = parseConfigId(configId);
            ps.setInt(1, tenantId);
            ps.setInt(2, configPk);
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
    public VCCredentialConfiguration create(VCCredentialConfiguration configuration, int tenantId)
            throws VCConfigMgtException {

        String insertCfg = SQLQueries.INSERT_CONFIG;
        try (Connection conn = IdentityDatabaseUtil.getDBConnection(true);
             PreparedStatement ps = conn.prepareStatement(insertCfg, new String[]{"ID"})) {
            try {
                ps.setInt(1, tenantId);
                ps.setString(2, configuration.getIdentifier());
                ps.setString(3, configuration.getConfigurationId());
                ps.setString(4, configuration.getScope());
                ps.setString(5, configuration.getFormat());
                ps.setString(6, configuration.getCredentialSigningAlgValuesSupported());
                ps.setString(7, configuration.getCredentialType());
                ps.setString(8, configuration.getCredentialMetadata() != null
                        ? configuration.getCredentialMetadata().getDisplay() : null);
                if (configuration.getExpiryInSeconds() != null) {
                    ps.setInt(9, configuration.getExpiryInSeconds());
                } else {
                    ps.setNull(9, Types.INTEGER);
                }
                ps.executeUpdate();

                int pk = getGeneratedId(ps);

                if (CollectionUtils.isNotEmpty(configuration.getClaimMappings())) {
                    addClaims(conn, pk, configuration.getClaimMappings());
                }

                IdentityDatabaseUtil.commitTransaction(conn);
                return getByConfigId(String.valueOf(pk), tenantId);
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
    public VCCredentialConfiguration update(String configId, VCCredentialConfiguration configuration, int tenantId)
            throws VCConfigMgtException {

        String updateCfg = SQLQueries.UPDATE_CONFIG;
        try (Connection conn = IdentityDatabaseUtil.getDBConnection(true);
             PreparedStatement ps = conn.prepareStatement(updateCfg)) {
            try {
                int configPk = parseConfigId(configId);
                ps.setString(1, configuration.getIdentifier());
                ps.setString(2, configuration.getConfigurationId());
                ps.setString(3, configuration.getScope());
                ps.setString(4, configuration.getFormat());
                ps.setString(5, configuration.getCredentialSigningAlgValuesSupported());
                ps.setString(6, configuration.getCredentialType());
                ps.setString(7, configuration.getCredentialMetadata() != null
                        ? configuration.getCredentialMetadata().getDisplay() : null);
                if (configuration.getExpiryInSeconds() != null) {
                    ps.setInt(8, configuration.getExpiryInSeconds());
                } else {
                    ps.setNull(8, Types.INTEGER);
                }
                ps.setInt(9, tenantId);
                ps.setInt(10, configPk);
                int updated = ps.executeUpdate();
                if (updated == 0) {
                    throw new VCConfigMgtClientException(
                            VCConfigManagementConstants.ErrorMessages.ERROR_CODE_CONFIG_NOT_FOUND.getCode(),
                            VCConfigManagementConstants.ErrorMessages.ERROR_CODE_CONFIG_NOT_FOUND.getMessage());
                }

                int pk = configPk;

                deleteClaims(conn, pk);
                if (CollectionUtils.isNotEmpty(configuration.getClaimMappings())) {
                    addClaims(conn, pk, configuration.getClaimMappings());
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
                ps.setInt(2, parseConfigId(configId));
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

    private VCCredentialConfiguration buildConfiguration(ResultSet rs, Connection conn) throws SQLException {

        VCCredentialConfiguration cfg = new VCCredentialConfiguration();
        cfg.setId(String.valueOf(rs.getInt("ID")));
        cfg.setIdentifier(rs.getString("IDENTIFIER"));
        cfg.setConfigurationId(rs.getString("CONFIGURATION_ID"));
        cfg.setScope(rs.getString("SCOPE"));
        cfg.setFormat(rs.getString("FORMAT"));
        cfg.setCredentialSigningAlgValuesSupported(rs.getString("CREDENTIAL_SIGNING_ALG"));
        cfg.setCredentialType(rs.getString("CREDENTIAL_TYPE"));
        VCCredentialConfiguration.CredentialMetadata metadata =
                new VCCredentialConfiguration.CredentialMetadata();
        metadata.setDisplay(rs.getString("CREDENTIAL_METADATA"));
        cfg.setCredentialMetadata(metadata);
        int expiry = rs.getInt("EXPIRY_IN_SECONDS");
        if (!rs.wasNull()) {
            cfg.setExpiryInSeconds(expiry);
        }
        cfg.setClaimMappings(getClaimsByConfigPk(conn, rs.getInt("ID")));
        return cfg;
    }

    private List<ClaimMapping> getClaimsByConfigPk(Connection conn, int configPk) throws SQLException {

        String sql = SQLQueries.LIST_CLAIMS_BY_CONFIG_PK;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, configPk);
            try (ResultSet rs = ps.executeQuery()) {
                List<ClaimMapping> list = new ArrayList<>();
                while (rs.next()) {
                    ClaimMapping cm = new ClaimMapping();
                    cm.setClaimURI(rs.getString("CLAIM_URI"));
                    cm.setDisplay(rs.getString("DISPLAY"));
                    list.add(cm);
                }
                return list;
            }
        }
    }

    private void addClaims(Connection conn, int configPk, List<ClaimMapping> claims) throws SQLException {

        String insert = SQLQueries.INSERT_CLAIM;
        try (PreparedStatement ps = conn.prepareStatement(insert)) {
            for (ClaimMapping cm : claims) {
                ps.setInt(1, configPk);
                ps.setString(2, cm.getClaimURI());
                ps.setString(3, cm.getDisplay());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void deleteClaims(Connection conn, int configPk) throws SQLException {

        try (PreparedStatement ps = conn.prepareStatement(SQLQueries.DELETE_CLAIMS_BY_CONFIG_PK)) {
            ps.setInt(1, configPk);
            ps.executeUpdate();
        }
    }

    private int getGeneratedId(PreparedStatement ps) throws SQLException {

        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        throw new SQLException("Failed to retrieve generated key for VC_CONFIG record.");
    }

    private int parseConfigId(String configId) throws VCConfigMgtClientException {

        try {
            return Integer.parseInt(configId);
        } catch (NumberFormatException e) {
            throw new VCConfigMgtClientException(
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_INVALID_REQUEST.getCode(),
                    "Configuration id must be a numeric value.", e);
        }
    }
}
