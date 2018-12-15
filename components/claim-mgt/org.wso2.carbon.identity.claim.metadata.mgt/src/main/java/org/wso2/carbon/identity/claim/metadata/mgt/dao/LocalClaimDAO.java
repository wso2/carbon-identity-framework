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
import org.wso2.carbon.identity.claim.metadata.mgt.model.AttributeMapping;
import org.wso2.carbon.identity.claim.metadata.mgt.model.Claim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
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
 * Data access object for org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim
 *
 */
public class LocalClaimDAO extends ClaimDAO {

    private static Log log = LogFactory.getLog(LocalClaimDAO.class);


    public List<LocalClaim> getLocalClaims(int tenantId) throws ClaimMetadataException {

        List<LocalClaim> localClaims = new ArrayList<>();

        Connection connection = IdentityDatabaseUtil.getDBConnection();

        try {
            // Start transaction
            connection.setAutoCommit(false);

            Map<Integer, Claim> localClaimMap = getClaims(connection, ClaimConstants.LOCAL_CLAIM_DIALECT_URI,
                    tenantId);

            for (Map.Entry<Integer, Claim> claimEntry : localClaimMap.entrySet()) {
                int claimId = claimEntry.getKey();
                Claim claim = claimEntry.getValue();

                List<AttributeMapping> attributeMappings = getClaimAttributeMappings(connection, claimId, tenantId);
                Map<String, String> claimProperties = getClaimProperties(connection, claimId, tenantId);

                LocalClaim localClaim = new LocalClaim(claim.getClaimURI(), attributeMappings, claimProperties);
                localClaims.add(localClaim);
            }

            // End transaction
            connection.commit();
        } catch (SQLException e) {
            throw new ClaimMetadataException("Error while listing local claims", e);
        } finally {
            IdentityDatabaseUtil.closeConnection(connection);
        }

        return localClaims;
    }

    public void addLocalClaim(LocalClaim localClaim, int tenantId) throws ClaimMetadataException {

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;

        String localClaimURI = localClaim.getClaimURI();

        try {
            // Start transaction
            connection.setAutoCommit(false);

            int localClaimId = addClaim(connection, ClaimConstants.LOCAL_CLAIM_DIALECT_URI, localClaimURI, tenantId);

            // Some JDBC Drivers returns this in the result, some don't
            if (localClaimId == 0) {
                if (log.isDebugEnabled()) {
                    log.debug("JDBC Driver did not return the claimId, executing Select operation");
                }
                localClaimId = getClaimId(connection, ClaimConstants.LOCAL_CLAIM_DIALECT_URI, localClaimURI, tenantId);
                // TODO : Handle invalid local claim URI

            }

            addClaimAttributeMappings(connection, localClaimId, localClaim.getMappedAttributes(), tenantId);
            addClaimProperties(connection, localClaimId, localClaim.getClaimProperties(), tenantId);

            // End transaction
            connection.commit();
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollBack(connection);
            throw new ClaimMetadataException("Error while adding local claim " + localClaimURI, e);
        } finally {
            IdentityDatabaseUtil.closeConnection(connection);
        }
    }

    public void updateLocalClaim(LocalClaim localClaim, int tenantId) throws ClaimMetadataException {

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;

        String localClaimURI = localClaim.getClaimURI();

        try {
            // Start transaction
            connection.setAutoCommit(false);

            int localClaimId = getClaimId(connection, ClaimConstants.LOCAL_CLAIM_DIALECT_URI, localClaimURI, tenantId);
            // TODO : Handle invalid local claim URI

            deleteClaimAttributeMappings(connection, localClaimId, tenantId);
            addClaimAttributeMappings(connection, localClaimId, localClaim.getMappedAttributes(), tenantId);

            deleteClaimProperties(connection, localClaimId, tenantId);
            addClaimProperties(connection, localClaimId, localClaim.getClaimProperties(), tenantId);

            // End transaction
            connection.commit();
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollBack(connection);
            throw new ClaimMetadataException("Error while updating local claim " + localClaimURI, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, null);
        }
    }

    public void removeLocalClaim(String localClaimURI, int tenantId) throws ClaimMetadataException {

        removeClaim(ClaimConstants.LOCAL_CLAIM_DIALECT_URI, localClaimURI, tenantId);
    }

    private List<AttributeMapping> getClaimAttributeMappings(Connection connection, int localClaimId, int tenantId)
            throws ClaimMetadataException {

        List<AttributeMapping> attributeMappings = new ArrayList<>();

        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        String query = SQLConstants.GET_MAPPED_ATTRIBUTES;

        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setInt(1, localClaimId);
            prepStmt.setInt(2, tenantId);
            rs = prepStmt.executeQuery();

            while (rs.next()) {
                String userStoreDomainName = rs.getString(SQLConstants.USER_STORE_DOMAIN_NAME_COLUMN);
                String attributeName = rs.getString(SQLConstants.ATTRIBUTE_NAME_COLUMN);

                AttributeMapping attributeMapping = new AttributeMapping(userStoreDomainName, attributeName);
                attributeMappings.add(attributeMapping);
            }
        } catch (SQLException e) {
            throw new ClaimMetadataException("Error while retrieving attribute mappings", e);
        } finally {
            IdentityDatabaseUtil.closeResultSet(rs);
            IdentityDatabaseUtil.closeStatement(prepStmt);
        }

        return attributeMappings;
    }

    private void addClaimAttributeMappings(Connection connection, int localClaimId, List<AttributeMapping>
            attributeMappings, int tenantId) throws ClaimMetadataException {

        PreparedStatement prepStmt = null;
        if (localClaimId > 0 && attributeMappings != null) {
            try {
                String query = SQLConstants.ADD_CLAIM_MAPPED_ATTRIBUTE;
                prepStmt = connection.prepareStatement(query);
                for (AttributeMapping attributeMapping : attributeMappings) {
                    if (StringUtils.isBlank(attributeMapping.getUserStoreDomain())) {
                        throw new ClaimMetadataException("User store domain of mapped Attribute cannot be empty for " +
                                "the local claim id : " + localClaimId);
                    } else if (StringUtils.isBlank(attributeMapping.getAttributeName())) {
                        throw new ClaimMetadataException("Mapped attribute of the local claim id : " + localClaimId +
                                " cannot be empty");
                    }
                    prepStmt.setInt(1, localClaimId);
                    prepStmt.setString(2, attributeMapping.getUserStoreDomain());
                    prepStmt.setString(3, attributeMapping.getAttributeName());
                    prepStmt.setInt(4, tenantId);
                    prepStmt.addBatch();
                }

                prepStmt.executeBatch();
            } catch (SQLException e) {
                throw new ClaimMetadataException("Error while adding attribute mappings", e);
            } finally {
                IdentityDatabaseUtil.closeStatement(prepStmt);
            }
        }
    }

    private void deleteClaimAttributeMappings(Connection connection, int localClaimId, int tenantId) throws
            ClaimMetadataException {

        PreparedStatement prepStmt = null;
        try {
            String query = SQLConstants.DELETE_CLAIM_MAPPED_ATTRIBUTE;
            prepStmt = connection.prepareStatement(query);

            prepStmt.setInt(1, localClaimId);
            prepStmt.setInt(2, tenantId);
            prepStmt.execute();
        } catch (SQLException e) {
            throw new ClaimMetadataException("Error while deleting attribute mappings", e);
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
        }
    }
}
