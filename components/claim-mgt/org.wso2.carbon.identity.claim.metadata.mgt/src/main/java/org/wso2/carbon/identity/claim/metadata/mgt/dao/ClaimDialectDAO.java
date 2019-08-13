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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ClaimDialect;
import org.wso2.carbon.identity.claim.metadata.mgt.util.SQLConstants;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Data access object for org.wso2.carbon.identity.claim.metadata.mgt.model.ClaimDialect
 *
 */
public class ClaimDialectDAO {

    private static final Log log = LogFactory.getLog(ClaimDialectDAO.class);

    public List<ClaimDialect> getClaimDialects(int tenantId) throws ClaimMetadataException {

        List<ClaimDialect> claimDialects = new ArrayList<>();

        Connection connection = IdentityDatabaseUtil.getDBConnection(false);
        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        String query = SQLConstants.GET_CLAIM_DIALECTS;

        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setInt(1, tenantId);
            rs = prepStmt.executeQuery();

            while (rs.next()) {
                String claimDialectURI = rs.getString(SQLConstants.DIALECT_URI_COLUMN);
                ClaimDialect claimDialect = new ClaimDialect(claimDialectURI);
                claimDialects.add(claimDialect);
            }
        } catch (SQLException e) {
            throw new ClaimMetadataException("Error while listing claim dialects", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, rs, prepStmt);
        }

        return claimDialects;
    }

    public void addClaimDialect(ClaimDialect claimDialect, int tenantId) throws ClaimMetadataException {

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;

        String query = SQLConstants.ADD_CLAIM_DIALECT;

        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, claimDialect.getClaimDialectURI());
            prepStmt.setInt(2, tenantId);
            prepStmt.executeUpdate();
            IdentityDatabaseUtil.commitTransaction(connection);
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            throw new ClaimMetadataException("Error while adding claim dialect " + claimDialect
                    .getClaimDialectURI(), e);
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
            IdentityDatabaseUtil.closeConnection(connection);
        }
    }

    public void renameClaimDialect(ClaimDialect oldClaimDialect, ClaimDialect newClaimDialect, int tenantId) throws
            ClaimMetadataException {

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;

        String query = SQLConstants.UPDATE_CLAIM_DIALECT;
        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, newClaimDialect.getClaimDialectURI());
            prepStmt.setString(2, oldClaimDialect.getClaimDialectURI());
            prepStmt.setInt(3, tenantId);
            prepStmt.executeUpdate();
            IdentityDatabaseUtil.commitTransaction(connection);
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            throw new ClaimMetadataException("Error while renaming claim dialect " + oldClaimDialect
                    .getClaimDialectURI(), e);
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
            IdentityDatabaseUtil.closeConnection(connection);
        }
    }

    public void removeClaimDialect(ClaimDialect claimDialect, int tenantId) throws ClaimMetadataException {

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;

        String query = SQLConstants.REMOVE_CLAIM_DIALECT;
        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, claimDialect.getClaimDialectURI());
            prepStmt.setInt(2, tenantId);
            prepStmt.executeUpdate();
            IdentityDatabaseUtil.commitTransaction(connection);
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            throw new ClaimMetadataException("Error while deleting claim dialect " + claimDialect
                    .getClaimDialectURI(), e);
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
            IdentityDatabaseUtil.closeConnection(connection);
        }
    }
}
