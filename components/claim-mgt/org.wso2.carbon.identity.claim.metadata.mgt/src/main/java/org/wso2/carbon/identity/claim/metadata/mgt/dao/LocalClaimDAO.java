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
import org.wso2.carbon.user.api.UserStoreException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data access object for org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim.
 */
public class LocalClaimDAO extends ClaimDAO {

    private static final Log log = LogFactory.getLog(LocalClaimDAO.class);


    public List<LocalClaim> getLocalClaims(int tenantId) throws ClaimMetadataException {

        List<LocalClaim> localClaims = new ArrayList<>();

        Connection connection = IdentityDatabaseUtil.getDBConnection(false);

        try {
            Map<Integer, Claim> localClaimMap = getClaims(connection, ClaimConstants.LOCAL_CLAIM_DIALECT_URI,
                    tenantId);
            Map<Integer, List<AttributeMapping>> claimAttributeMappingsOfDialect =
                    getClaimAttributeMappingsOfDialect(connection, ClaimConstants.LOCAL_CLAIM_DIALECT_URI, tenantId);
            Map<Integer, Map<String, String>> claimPropertiesOfDialect =
                    getClaimPropertiesOfDialect(connection, ClaimConstants.LOCAL_CLAIM_DIALECT_URI, tenantId);

            for (Map.Entry<Integer, Claim> claimEntry : localClaimMap.entrySet()) {
                int claimId = claimEntry.getKey();
                Claim claim = claimEntry.getValue();

                List<AttributeMapping> attributeMappingsOfClaim = claimAttributeMappingsOfDialect.get(claimId);
                Map<String, String> propertiesOfClaim = claimPropertiesOfDialect.get(claimId);

                localClaims.add(new LocalClaim(claim.getClaimURI(), attributeMappingsOfClaim, propertiesOfClaim));
            }
        } finally {
            IdentityDatabaseUtil.closeConnection(connection);
        }

        return localClaims;
    }

    private Map<Integer, List<AttributeMapping>> getClaimAttributeMappingsOfDialect(Connection connection,
                                                                                    String claimDialectURI,
                                                                                    int tenantId)
            throws ClaimMetadataException {

        Map<Integer, List<AttributeMapping>> attributeMappings = new HashMap<>();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            preparedStatement = connection.prepareStatement(SQLConstants.GET_MAPPED_ATTRIBUTES);
            preparedStatement.setString(1, claimDialectURI);
            preparedStatement.setInt(2, tenantId);
            preparedStatement.setInt(3, tenantId);
            preparedStatement.setInt(4, tenantId);

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String userStoreDomainName = resultSet.getString(SQLConstants.USER_STORE_DOMAIN_NAME_COLUMN);
                String attributeName = resultSet.getString(SQLConstants.ATTRIBUTE_NAME_COLUMN);
                int localClaimId = resultSet.getInt(SQLConstants.LOCAL_CLAIM_ID_COLUMN);

                AttributeMapping attributeMapping = new AttributeMapping(userStoreDomainName, attributeName);
                List<AttributeMapping> existingAttributeMapping = attributeMappings.get(localClaimId);

                if (existingAttributeMapping == null) {
                    existingAttributeMapping = new ArrayList<>();
                }

                existingAttributeMapping.add(attributeMapping);
                attributeMappings.put(localClaimId, existingAttributeMapping);
            }
        } catch (SQLException e) {
            throw new ClaimMetadataException("Error occurred while retrieving attribute mappings.", e);
        } finally {
            IdentityDatabaseUtil.closeResultSet(resultSet);
            IdentityDatabaseUtil.closeStatement(preparedStatement);
        }
        return attributeMappings;
    }

    private Map<Integer, Map<String, String>> getClaimPropertiesOfDialect(Connection connection, String claimDialectURI,
                                                                          int tenantId) throws ClaimMetadataException {

        Map<Integer, Map<String, String>> claimPropertyMap = new HashMap<>();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            preparedStatement = connection.prepareStatement(SQLConstants.GET_CLAIMS_PROPERTIES_QUERY);
            preparedStatement.setString(1, claimDialectURI);
            preparedStatement.setInt(2, tenantId);
            preparedStatement.setInt(3, tenantId);

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String propertyName = resultSet.getString(SQLConstants.PROPERTY_NAME_COLUMN);
                String propertyValue = resultSet.getString(SQLConstants.PROPERTY_VALUE_COLUMN);
                int localClaimId = resultSet.getInt(SQLConstants.LOCAL_CLAIM_ID_COLUMN);

                Map<String, String> existingAttributeMap = claimPropertyMap.get(localClaimId);

                if (existingAttributeMap == null) {
                    existingAttributeMap = new HashMap<>();
                }

                existingAttributeMap.put(propertyName, propertyValue);
                claimPropertyMap.put(localClaimId, existingAttributeMap);
            }
        } catch (SQLException e) {
            throw new ClaimMetadataException("Error occurred while retrieving attribute mappings.", e);
        } finally {
            IdentityDatabaseUtil.closeResultSet(resultSet);
            IdentityDatabaseUtil.closeStatement(preparedStatement);
        }
        return claimPropertyMap;
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
            rollbackTransaction(connection);
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
            rollbackTransaction(connection);
            throw new ClaimMetadataException("Error while updating local claim " + localClaimURI, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, null);
        }
    }

    /**
     * Update attribute claim mappings related to tenant id and domain.
     *
     * @param localClaimList  List of local claims.
     * @param tenantId        Tenant Id.
     * @param userStoreDomain Domain name.
     * @throws ClaimMetadataException If an error occurred while updating local claims.
     */
    public void updateLocalClaimMappings(List<LocalClaim> localClaimList, int tenantId, String userStoreDomain)
            throws ClaimMetadataException {

        Connection connection = IdentityDatabaseUtil.getDBConnection(true);

        try {
            // Start transaction.
            connection.setAutoCommit(false);

            Map<Integer, List<AttributeMapping>> claimAttributeMappingsOfDialect =
                    getClaimAttributeMappingsOfDialect(connection, ClaimConstants.LOCAL_CLAIM_DIALECT_URI, tenantId);

            for (LocalClaim localClaim : localClaimList) {
                String localClaimURI = localClaim.getClaimURI();
                int localClaimId = getClaimId(connection, ClaimConstants.LOCAL_CLAIM_DIALECT_URI,
                        localClaimURI, tenantId);
                List<AttributeMapping> existingClaimAttributeMappings =
                        claimAttributeMappingsOfDialect.get(localClaimId);
                existingClaimAttributeMappings.removeIf(attributeMapping -> attributeMapping.getUserStoreDomain().
                        equals(userStoreDomain.toUpperCase()));
                existingClaimAttributeMappings.add(new AttributeMapping(userStoreDomain,
                        localClaim.getMappedAttribute(userStoreDomain)));

                deleteClaimAttributeMappings(connection, localClaimId, tenantId);
                addClaimAttributeMappings(connection, localClaimId, existingClaimAttributeMappings, tenantId);
            }

            // End transaction.
            connection.commit();
        } catch (SQLException e) {
            rollbackTransaction(connection);
            throw new ClaimMetadataException("Error while updating local claims ", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, null);
        }
    }

    public void removeLocalClaim(String localClaimURI, int tenantId) throws ClaimMetadataException {

        removeClaim(ClaimConstants.LOCAL_CLAIM_DIALECT_URI, localClaimURI, tenantId);
    }

    /**
     * Delete claim attribute mappings.
     *
     * @param tenantId        Tenant Id
     * @param userstoreDomain Domain name
     * @throws UserStoreException If an error occurred while deleting claim mappings
     */
    public void deleteClaimMappingAttributes(int tenantId, String userstoreDomain) throws UserStoreException {

        if (StringUtils.isEmpty(userstoreDomain)) {
            String message = ClaimConstants.ErrorMessage.ERROR_CODE_EMPTY_TENANT_DOMAIN.getMessage();
            if (log.isDebugEnabled()) {
                log.debug(message);
            }
            throw new UserStoreException(message);
        }
        userstoreDomain = userstoreDomain.toUpperCase();
        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(true);
             PreparedStatement preparedStatement = dbConnection
                     .prepareStatement(SQLConstants.DELETE_IDN_CLAIM_MAPPED_ATTRIBUTE)) {
            preparedStatement.setString(1, userstoreDomain);
            preparedStatement.setInt(2, tenantId);
            preparedStatement.executeUpdate();
            IdentityDatabaseUtil.commitTransaction(dbConnection);
        } catch (SQLException e) {
            String message =
                    String.format(ClaimConstants.ErrorMessage.ERROR_CODE_DELETE_IDN_CLAIM_MAPPED_ATTRIBUTE.getMessage(),
                            userstoreDomain, tenantId);
            throw new UserStoreException(message, e);
        }
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

    /**
     * Revoke the transaction when catch then sql transaction errors.
     *
     * @param dbConnection database connection.
     */
    private static void rollbackTransaction(Connection dbConnection) {

        try {
            if (dbConnection != null) {
                dbConnection.rollback();
            }
        } catch (SQLException e1) {
            log.error("An error occurred while rolling back transactions. ", e1);
        }
    }

    /**
     * Fetch mapped external claims of a local claim.
     *
     * @param tenantId      Tenant Id.
     * @param localClaimURI URI of the local claim.
     * @return List of associated external claims.
     * @throws ClaimMetadataException When trying to fetch mapped external claims for a local claim.
     */
    public List<Claim> fetchMappedExternalClaims(String localClaimURI, int tenantId)
            throws ClaimMetadataException {

        List<Claim> mappedExternalClaims = new ArrayList<>();
        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement prepStmt =
                     dbConnection.prepareStatement(SQLConstants.FETCH_EXTERNAL_MAPPED_CLAIM_OF_LOCAL_CLAIM)) {
            prepStmt.setString(1, localClaimURI);
            prepStmt.setInt(2, tenantId);
            ResultSet resultSet = prepStmt.executeQuery();
            while (resultSet.next()) {
                String claimURI = resultSet.getString(SQLConstants.CLAIM_URI_COLUMN);
                String dialectURI = resultSet.getString(SQLConstants.DIALECT_URI_COLUMN);
                Claim claim = new Claim(dialectURI, claimURI);
                mappedExternalClaims.add(claim);
            }
            return mappedExternalClaims;
        } catch (SQLException e) {
            throw new ClaimMetadataException("Error while obtaining mapped external claims for local claim.", e);
        }
    }
}
