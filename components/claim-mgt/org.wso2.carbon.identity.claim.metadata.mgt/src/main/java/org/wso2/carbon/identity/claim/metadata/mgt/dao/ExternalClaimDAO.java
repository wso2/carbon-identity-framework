/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.claim.metadata.mgt.dao;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.Claim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ExternalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.carbon.identity.claim.metadata.mgt.util.SQLConstants;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * Data access object for org.wso2.carbon.identity.claim.metadata.mgt.model.ExternalClaim
 *
 */
public class ExternalClaimDAO extends ClaimDAO {

    private static Log log = LogFactory.getLog(ExternalClaimDAO.class);


    public List<ExternalClaim> getExternalClaims(String externalDialectURI, int tenantId) throws
            ClaimMetadataException {

        List<ExternalClaim> externalClaims = new ArrayList<>();

        Connection connection = IdentityDatabaseUtil.getDBConnection();

        try {
            // Start transaction
            connection.setAutoCommit(false);

            Map<Integer, Claim> externalClaimMap = getClaims(connection, externalDialectURI, tenantId);

            for (Map.Entry<Integer, Claim> claimEntry : externalClaimMap.entrySet()) {
                int externalClaimId = claimEntry.getKey();
                Claim claim = claimEntry.getValue();

                String mappedLocalClaimURI = getClaimMapping(connection, externalClaimId, tenantId);

                Map<String, String> claimProperties = getClaimProperties(connection, externalClaimId, tenantId);

                ExternalClaim externalClaim = new ExternalClaim(claim.getClaimDialectURI(), claim.getClaimURI(),
                        mappedLocalClaimURI, claimProperties);

                externalClaims.add(externalClaim);
            }

            // End transaction
            connection.commit();
        } catch (SQLException e) {
            throw new ClaimMetadataException("Error while listing external claims for diaclect " +
                    externalDialectURI, e);
        } finally {
            IdentityDatabaseUtil.closeConnection(connection);
        }

        return externalClaims;
    }

    public void addExternalClaim(ExternalClaim externalClaim, int tenantId) throws ClaimMetadataException {

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;

        String externalClaimURI = externalClaim.getClaimURI();
        String externalClaimDialectURI = externalClaim.getClaimDialectURI();
        String mappedLocalClaimURI = externalClaim.getMappedLocalClaim();

        try {
            // Start transaction
            connection.setAutoCommit(false);

            int externalClaimId = addClaim(connection, externalClaimDialectURI, externalClaimURI, tenantId);

            // Some JDBC Drivers returns this in the result, some don't
            if (externalClaimId == 0) {
                if (log.isDebugEnabled()) {
                    log.debug("JDBC Driver did not return the claimId, executing Select operation");
                }
                externalClaimId = getClaimId(connection, externalClaimDialectURI, externalClaimURI, tenantId);
                // TODO : Handle invalid external claim URI

            }

            int localClaimId = getClaimId(connection, ClaimConstants.LOCAL_CLAIM_DIALECT_URI, mappedLocalClaimURI,
                    tenantId);
            // TODO : Handle invalid external claim URI

            addClaimMapping(connection, externalClaimId, localClaimId, tenantId);
            addClaimProperties(connection, externalClaimId, externalClaim.getClaimProperties(), tenantId);
            // End transaction
            connection.commit();
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollBack(connection);
            throw new ClaimMetadataException("Error while adding external claim " + externalClaimURI + " to " +
                    "dialect " + externalClaimDialectURI, e);
        } finally {
            IdentityDatabaseUtil.closeConnection(connection);
        }
    }

    public void updateExternalClaim(ExternalClaim externalClaim, int tenantId) throws ClaimMetadataException {

        Connection connection = IdentityDatabaseUtil.getDBConnection();

        String externalClaimURI = externalClaim.getClaimURI();
        String externalClaimDialectURI = externalClaim.getClaimDialectURI();
        String mappedLocalClaimURI = externalClaim.getMappedLocalClaim();

        try {
            // Start transaction
            connection.setAutoCommit(false);

            int externalClaimId = getClaimId(connection, externalClaimDialectURI, externalClaimURI, tenantId);
            // TODO : Handle invalid external claim URI

            int localClaimId = getClaimId(connection, ClaimConstants.LOCAL_CLAIM_DIALECT_URI, mappedLocalClaimURI,
                    tenantId);
            // TODO : Handle invalid local claim URI

            updateClaimMapping(connection, externalClaimId, localClaimId, tenantId);

            deleteClaimProperties(connection, externalClaimId, tenantId);
            addClaimProperties(connection, externalClaimId, externalClaim.getClaimProperties(), tenantId);

            // End transaction
            connection.commit();
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollBack(connection);
            throw new ClaimMetadataException("Error while updating external claim " + externalClaimURI + " in " +
                    "dialect " + externalClaimDialectURI, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, null);
        }
    }


    public void removeExternalClaim(String externalClaimDialectURI, String externalClaimURI, int tenantId) throws
            ClaimMetadataException {

        removeClaim(externalClaimDialectURI, externalClaimURI, tenantId);
    }

    public boolean isMappedLocalClaim(String mappedLocalClaimURI, int tenantId) throws
            ClaimMetadataException {

        boolean isMappedLocalClaim = false;

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        String query = SQLConstants.IS_CLAIM_MAPPING;

        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, ClaimConstants.LOCAL_CLAIM_DIALECT_URI);
            prepStmt.setInt(2, tenantId);
            prepStmt.setString(3, mappedLocalClaimURI);
            prepStmt.setInt(4, tenantId);
            prepStmt.setInt(5, tenantId);
            rs = prepStmt.executeQuery();

            if (rs.next()) {
                isMappedLocalClaim = true;
            }

        } catch (SQLException e) {
            throw new ClaimMetadataException("Error while checking mapped local claim " + mappedLocalClaimURI, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, rs, prepStmt);
        }

        return isMappedLocalClaim;
    }

    private void addClaimMapping(Connection connection, int externalClaimId, int localClaimId, int tenantId)
            throws ClaimMetadataException {

        PreparedStatement prepStmt = null;

        String query = SQLConstants.ADD_CLAIM_MAPPING;
        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setInt(1, localClaimId);
            prepStmt.setInt(2, externalClaimId);
            prepStmt.setInt(3, tenantId);
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            throw new ClaimMetadataException("Error while adding claim mapping", e);
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
        }
    }

    private void updateClaimMapping(Connection connection, int externalClaimId, int localClaimId, int tenantId)
            throws ClaimMetadataException {

        PreparedStatement prepStmt = null;

        String query = SQLConstants.UPDATE_CLAIM_MAPPING;
        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setInt(1, localClaimId);
            prepStmt.setInt(2, externalClaimId);
            prepStmt.setInt(3, tenantId);
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            throw new ClaimMetadataException("Error while updating claim mapping", e);
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
        }
    }

    private String getClaimMapping(Connection connection, int externalClaimId, int tenantId)
            throws ClaimMetadataException {

        String mappedLocalClaimURI = null;

        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        String query = SQLConstants.GET_CLAIM_MAPPING;
        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setInt(1, externalClaimId);
            prepStmt.setInt(2, tenantId);
            prepStmt.setInt(3, tenantId);
            rs = prepStmt.executeQuery();

            while (rs.next()) {
                mappedLocalClaimURI = rs.getString(SQLConstants.CLAIM_URI_COLUMN);
            }
        } catch (SQLException e) {
            throw new ClaimMetadataException("Error while retrieving claim mapping", e);
        } finally {
            IdentityDatabaseUtil.closeResultSet(rs);
            IdentityDatabaseUtil.closeStatement(prepStmt);
        }

        if (StringUtils.isBlank(mappedLocalClaimURI)) {
            throw new ClaimMetadataException("Invalid external claim URI. Claim mapping cannot be empty.");
        }

        return mappedLocalClaimURI;
    }
}
