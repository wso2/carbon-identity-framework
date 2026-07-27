/*
 * Copyright (c) 2016-2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.claim.metadata.mgt.dao;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ExternalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.carbon.identity.claim.metadata.mgt.util.SQLConstants;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.CANONICAL_VALUES_PROPERTY;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.CANONICAL_VALUE_PREFIX;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.SUB_ATTRIBUTES_PROPERTY;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.SUB_ATTRIBUTE_PREFIX;

/**
 *
 * Data access object for org.wso2.carbon.identity.claim.metadata.mgt.model.ExternalClaim
 *
 */
public class ExternalClaimDAO extends ClaimDAO {

    private static final Log log = LogFactory.getLog(ExternalClaimDAO.class);


    public List<ExternalClaim> getExternalClaims(String externalDialectURI, int tenantId) throws
            ClaimMetadataException {

        List<ExternalClaim> externalClaims = new ArrayList<>();
        Connection connection = IdentityDatabaseUtil.getDBConnection(false);
        try {
            externalClaims = getExternalClaimsFromDB(connection, externalDialectURI, tenantId);
        }
        finally {
            IdentityDatabaseUtil.closeConnection(connection);
        }
        return externalClaims;
    }

    public void addExternalClaim(ExternalClaim externalClaim, int tenantId) throws ClaimMetadataException {

        Connection connection = IdentityDatabaseUtil.getDBConnection();

        String externalClaimURI = externalClaim.getClaimURI();
        String externalClaimDialectURI = externalClaim.getClaimDialectURI();
        String mappedLocalClaimURI = externalClaim.getMappedLocalClaim();

        try {
            // Start transaction
            connection.setAutoCommit(false);

            // If an invalid local claim is provided an exception will be thrown.
            int localClaimId = getClaimId(connection, ClaimConstants.LOCAL_CLAIM_DIALECT_URI, mappedLocalClaimURI,
                    tenantId);
            int externalClaimId = addClaim(connection, externalClaimDialectURI, externalClaimURI, tenantId);

            // Some JDBC Drivers returns this in the result, some don't
            if (externalClaimId == 0) {
                if (log.isDebugEnabled()) {
                    log.debug("JDBC Driver did not return the claimId, executing Select operation");
                }
                externalClaimId = getClaimId(connection, externalClaimDialectURI, externalClaimURI, tenantId);
                // TODO : Handle invalid external claim URI

            }

            // TODO : Handle invalid external claim URI

            addClaimMapping(connection, externalClaimId, localClaimId, tenantId);
            addClaimProperties(connection, externalClaimId, externalClaim.getClaimProperties(), tenantId);
            // End transaction
            connection.commit();
        } catch (SQLException e) {
            rollbackTransaction(connection);
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
            rollbackTransaction(connection);
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

        Connection connection = IdentityDatabaseUtil.getDBConnection(false);
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

    public boolean isLocalClaimMappedWithinDialect(String mappedLocalClaimURI, String externalClaimDialectURI,
                                                   int tenantId) throws ClaimMetadataException {

        boolean isLocalClaimMappedWithinDialect = false;

        String query = SQLConstants.IS_LOCAL_CLAIM_MAPPED_EX_DIALECT;

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false); PreparedStatement prepStmt =
                connection.prepareStatement(query)) {

            int i = 1;
            prepStmt.setString(i++, ClaimConstants.LOCAL_CLAIM_DIALECT_URI);
            prepStmt.setInt(i++, tenantId);
            prepStmt.setString(i++, mappedLocalClaimURI);
            prepStmt.setInt(i++, tenantId);
            prepStmt.setInt(i++, tenantId);
            prepStmt.setString(i++, externalClaimDialectURI);
            prepStmt.setInt(i, tenantId);

            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    isLocalClaimMappedWithinDialect = true;
                }
            }
        } catch (SQLException e) {
            throw new ClaimMetadataException("Error while checking mapped local claim " + mappedLocalClaimURI, e);
        }

        return isLocalClaimMappedWithinDialect;
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

    /**
     * This method retrieve the external claim, mapped URI,claim properties of the given claimDialectURI
     *
     * @param connection connection to the DB
     * @param claimDialectURI claimDialectURI to retrieve external claims
     * @param tenantId  tenantID of the claims to be retrieved
     * @return  List of External claims
     * @throws ClaimMetadataException
     */
    private List<ExternalClaim> getExternalClaimsFromDB(Connection connection, String claimDialectURI, int tenantId)
            throws ClaimMetadataException {

        Map<Integer, ExternalClaim> claimMap = new HashMap<>();
        Map<String, String> propmap;

        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        String query = SQLConstants.GET_CLAIMS;

        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, claimDialectURI);
            prepStmt.setInt(2, tenantId);
            prepStmt.setInt(3, tenantId);
            rs = prepStmt.executeQuery();

            Map<Integer, List<String>> canonicalValuesMap = new HashMap<>();
            while (rs.next()) {
                String claimPropertyName = rs.getString(SQLConstants.PROPERTY_NAME_COLUMN);
                String claimPropertyValue = rs.getString(SQLConstants.PROPERTY_VALUE_COLUMN);
                int localId = rs.getInt(SQLConstants.ID_COLUMN);
                if (claimMap.get(localId) == null) {
                    String mappedURI = rs.getString(SQLConstants.MAPPED_URI_COLUMN);
                    String claimURI = rs.getString(SQLConstants.CLAIM_URI_COLUMN);
                    propmap = new HashMap<>();
                    if (claimPropertyName != null) {
                        if (claimPropertyName.startsWith(SUB_ATTRIBUTE_PREFIX)) {
                            claimPropertyName = SUB_ATTRIBUTES_PROPERTY;
                        } else if (claimPropertyName.startsWith(CANONICAL_VALUE_PREFIX)) {
                            claimPropertyName = CANONICAL_VALUES_PROPERTY;
                            List<String> canonicalValues = new ArrayList<>();
                            canonicalValues.add(claimPropertyValue);
                            canonicalValuesMap.put(localId, canonicalValues);
                            claimPropertyValue = canonicalValues.toString();
                        }
                        propmap.put(claimPropertyName, claimPropertyValue);
                    }
                    ExternalClaim temp = new ExternalClaim(claimDialectURI, claimURI, mappedURI, propmap);
                    claimMap.put(localId, temp);
                } else {
                    if (claimPropertyName.startsWith(SUB_ATTRIBUTE_PREFIX)) {
                        String subAttributes = claimMap.get(localId).getClaimProperties().get(SUB_ATTRIBUTES_PROPERTY);
                        if (subAttributes == null) {
                            subAttributes = StringUtils.EMPTY;
                        } else {
                            subAttributes += " ";
                        }
                        claimPropertyValue = subAttributes + claimPropertyValue;
                        claimPropertyName = SUB_ATTRIBUTES_PROPERTY;
                    } else if (claimPropertyName.startsWith(CANONICAL_VALUE_PREFIX)) {
                        List<String> canonicalValuesList = canonicalValuesMap.get(localId);
                        if (canonicalValuesList == null) {
                            canonicalValuesList = new ArrayList<>();
                        }
                        canonicalValuesList.add(claimPropertyValue);
                        canonicalValuesMap.put(localId, canonicalValuesList);
                        claimPropertyName = CANONICAL_VALUES_PROPERTY;
                        claimPropertyValue = canonicalValuesList.toString();
                    }
                    claimMap.get(localId).getClaimProperties().put(claimPropertyName, claimPropertyValue);
                }
            }
        } catch (SQLException e) {
            throw new ClaimMetadataException("Error while listing claims for dialect " + claimDialectURI, e);
        } finally {
            IdentityDatabaseUtil.closeResultSet(rs);
            IdentityDatabaseUtil.closeStatement(prepStmt);
        }
        return new ArrayList<ExternalClaim>(claimMap.values());
    }
    /**
     * Revoke the transaction when catch then sql transaction errors.
     *
     * @param dbConnection database connection.
     */
    private void rollbackTransaction(Connection dbConnection) {

        try {
            if (dbConnection != null) {
                dbConnection.rollback();
            }
        } catch (SQLException e1) {
            log.error("An error occurred while rolling back transactions. ", e1);
        }
    }
}
