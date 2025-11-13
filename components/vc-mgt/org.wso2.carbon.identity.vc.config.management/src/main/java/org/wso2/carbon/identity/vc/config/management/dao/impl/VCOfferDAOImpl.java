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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.vc.config.management.constant.VCConfigManagementConstants;
import org.wso2.carbon.identity.vc.config.management.dao.SQLQueries;
import org.wso2.carbon.identity.vc.config.management.dao.VCOfferDAO;
import org.wso2.carbon.identity.vc.config.management.exception.VCConfigMgtClientException;
import org.wso2.carbon.identity.vc.config.management.exception.VCConfigMgtException;
import org.wso2.carbon.identity.vc.config.management.exception.VCConfigMgtServerException;
import org.wso2.carbon.identity.vc.config.management.model.VCOffer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JDBC implementation of {@link VCOfferDAO}.
 */
public class VCOfferDAOImpl implements VCOfferDAO {

    private static final Log LOG = LogFactory.getLog(VCOfferDAOImpl.class);

    @Override
    public List<VCOffer> list(int tenantId) throws VCConfigMgtException {

        List<VCOffer> results = new ArrayList<>();
        String sql = SQLQueries.LIST_OFFERS;
        try (Connection conn = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tenantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(buildOfferListItem(rs, conn, tenantId));
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
    public VCOffer get(String offerId, int tenantId) throws VCConfigMgtException {

        String sql = SQLQueries.GET_OFFER_BY_ID;
        try (Connection conn = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tenantId);
            ps.setString(2, offerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return buildOffer(rs, conn, tenantId);
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
    public boolean existsByOfferId(String offerId, int tenantId) throws VCConfigMgtException {

        String sql = SQLQueries.EXISTS_OFFER_BY_ID;
        try (Connection conn = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tenantId);
            ps.setString(2, offerId);
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
    public VCOffer add(VCOffer offer, int tenantId) throws VCConfigMgtException {

        String insertOffer = SQLQueries.INSERT_OFFER;
        try (Connection conn = IdentityDatabaseUtil.getDBConnection(true);
             PreparedStatement ps = conn.prepareStatement(insertOffer)) {
            try {
                String id = UUID.randomUUID().toString();
                ps.setString(1, id);
                ps.setInt(2, tenantId);
                ps.setString(3, offer.getDisplayName());
                ps.executeUpdate();

                if (CollectionUtils.isNotEmpty(offer.getCredentialConfigurationIds())) {
                    addCredentialConfigurations(conn, id, offer.getCredentialConfigurationIds(), tenantId);
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
    public VCOffer update(String offerId, VCOffer offer, int tenantId) throws VCConfigMgtException {

        String updateOffer = SQLQueries.UPDATE_OFFER;
        try (Connection conn = IdentityDatabaseUtil.getDBConnection(true);
             PreparedStatement ps = conn.prepareStatement(updateOffer)) {
            try {
                ps.setString(1, offer.getDisplayName());
                ps.setInt(2, tenantId);
                ps.setString(3, offerId);
                int updated = ps.executeUpdate();
                if (updated == 0) {
                    throw new VCConfigMgtClientException(
                            VCConfigManagementConstants.ErrorMessages.ERROR_CODE_OFFER_NOT_FOUND.getCode(),
                            VCConfigManagementConstants.ErrorMessages.ERROR_CODE_OFFER_NOT_FOUND.getMessage());
                }

                deleteCredentialConfigurations(conn, offerId, tenantId);
                if (CollectionUtils.isNotEmpty(offer.getCredentialConfigurationIds())) {
                    addCredentialConfigurations(conn, offerId, offer.getCredentialConfigurationIds(), tenantId);
                }

                IdentityDatabaseUtil.commitTransaction(conn);
                return get(offerId, tenantId);
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
    public void delete(String offerId, int tenantId) throws VCConfigMgtException {

        String deleteOffer = SQLQueries.DELETE_OFFER;
        try (Connection conn = IdentityDatabaseUtil.getDBConnection(true);
             PreparedStatement ps = conn.prepareStatement(deleteOffer)) {
            try {
                ps.setInt(1, tenantId);
                ps.setString(2, offerId);
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
     * Build VC offer from result set with full details.
     *
     * @param rs   Result set.
     * @param conn DB connection.
     * @return VC offer.
     * @throws SQLException on SQL errors.
     */
    private VCOffer buildOffer(ResultSet rs, Connection conn, int tenantId) throws SQLException {

        VCOffer offer = new VCOffer();
        offer.setOfferId(rs.getString("OFFER_ID"));
        offer.setDisplayName(rs.getString("DISPLAY_NAME"));
        offer.setCredentialConfigurationIds(getCredentialConfigurationsByOfferId(conn, offer.getOfferId(), tenantId));
        return offer;
    }

    /**
     * Build VC offer from result set for list view.
     *
     * @param rs   Result set.
     * @param conn DB connection.
     * @return VC offer.
     * @throws SQLException on SQL errors.
     */
    private VCOffer buildOfferListItem(ResultSet rs, Connection conn, int tenantId) throws SQLException {

        VCOffer offer = new VCOffer();
        offer.setOfferId(rs.getString("OFFER_ID"));
        offer.setDisplayName(rs.getString("DISPLAY_NAME"));
        offer.setCredentialConfigurationIds(getCredentialConfigurationsByOfferId(conn, offer.getOfferId(), tenantId));
        return offer;
    }

    /**
     * Get credential configuration IDs by offer ID.
     *
     * @param conn    DB connection.
     * @param offerId Offer ID.
     * @param tenantId Tenant ID.
     * @return List of credential configuration IDs.
     * @throws SQLException on SQL errors.
     */
    private List<String> getCredentialConfigurationsByOfferId(Connection conn, String offerId, int tenantId)
            throws SQLException {

        String sql = SQLQueries.LIST_CREDENTIAL_CONFIGS_BY_OFFER_ID;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, offerId);
            ps.setInt(2, tenantId);
            try (ResultSet rs = ps.executeQuery()) {
                List<String> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(rs.getString("CONFIG_ID"));
                }
                return list;
            }
        }
    }

    /**
     * Add credential configurations for an offer.
     *
     * @param conn                       DB connection.
     * @param offerId                    Offer ID.
     * @param credentialConfigurationIds List of credential configuration IDs.
     * @param tenantId                   Tenant ID.
     * @throws SQLException on SQL errors.
     */
    private void addCredentialConfigurations(Connection conn, String offerId,
                                             List<String> credentialConfigurationIds, int tenantId)
            throws SQLException {

        String insert = SQLQueries.INSERT_OFFER_CREDENTIAL_CONFIG;
        try (PreparedStatement ps = conn.prepareStatement(insert)) {
            for (String configId : credentialConfigurationIds) {
                ps.setString(1, offerId);
                ps.setString(2, configId);
                ps.setInt(3, tenantId);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /**
     * Delete credential configurations for an offer.
     *
     * @param conn    DB connection.
     * @param offerId Offer ID.
     * @param tenantId Tenant ID.
     * @throws SQLException on SQL errors.
     */
    private void deleteCredentialConfigurations(Connection conn, String offerId, int tenantId) throws SQLException {

        try (PreparedStatement ps = conn.prepareStatement(SQLQueries.DELETE_OFFER_CREDENTIAL_CONFIGS_BY_OFFER_ID)) {
            ps.setString(1, offerId);
            ps.setInt(2, tenantId);
            ps.executeUpdate();
        }
    }
}

