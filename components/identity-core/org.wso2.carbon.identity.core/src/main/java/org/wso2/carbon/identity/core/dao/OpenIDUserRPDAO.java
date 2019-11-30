/*
 *   Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */
package org.wso2.carbon.identity.core.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.model.OpenIDUserRPDO;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OpenIDUserRPDAO {

    private static final Log log = LogFactory.getLog(OpenIDUserRPDAO.class);

    /**
     * Creates a Relying Party and associates it with the User.
     * If the entry exist, then update with the new data
     *
     * @param rpdo
     */
    public void createOrUpdate(OpenIDUserRPDO rpdo, int tenantId) {

        // first we try to get DO from the database. Return null if no data
        OpenIDUserRPDO existingdo = getOpenIDUserRP(rpdo.getUserName(), rpdo.getRpUrl(), tenantId);

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;

        try {

            if (existingdo != null) { // data found in the database
                // we should update the entry
                prepStmt = connection.prepareStatement(OpenIDSQLQueries.UPDATE_USER_RP);

                prepStmt.setString(5, rpdo.getUserName());
                prepStmt.setInt(6, tenantId);
                prepStmt.setString(7, rpdo.getRpUrl());
                prepStmt.setString(1, rpdo.isTrustedAlways() ? "TRUE" : "FALSE");

                // we set the new current date
                prepStmt.setDate(2, new java.sql.Date(new Date().getTime()));
                // we increment the value which is in the database
                prepStmt.setInt(3, existingdo.getVisitCount() + 1); // increase visit count

                prepStmt.setString(4, rpdo.getDefaultProfileName());

                prepStmt.execute();
                connection.commit();
            } else {
                // data not found, we should create the entry
                prepStmt = connection.prepareStatement(OpenIDSQLQueries.STORE_USER_RP);

                prepStmt.setString(1, rpdo.getUserName());
                prepStmt.setInt(2, tenantId);
                prepStmt.setString(3, rpdo.getRpUrl());
                prepStmt.setString(4, rpdo.isTrustedAlways() ? "TRUE" : "FALSE");

                // we set the current date
                prepStmt.setDate(5, new java.sql.Date(new Date().getTime()));
                // ok, this is the first visit
                prepStmt.setInt(6, 1);

                prepStmt.setString(7, rpdo.getDefaultProfileName());

                prepStmt.execute();
                IdentityDatabaseUtil.commitTransaction(connection);
            }
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            log.error("Failed to store RP:  " + rpdo.getRpUrl() + " for user: " +
                    rpdo.getUserName() + " Error while accessing the database", e);
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
            IdentityDatabaseUtil.closeConnection(connection);
        }
    }

    /**
     * Updates the Relying Party if exists, if not, then creates a new Relying
     * Party
     *
     * @param rpdo
     */
    public void update(OpenIDUserRPDO rpdo, int tenantId) {

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;

        try {
            if (isUserRPExist(connection, rpdo, tenantId)) {
                // we should update the entry
                prepStmt = connection.prepareStatement(OpenIDSQLQueries.UPDATE_USER_RP);

                prepStmt.setString(1, rpdo.getUserName());
                prepStmt.setInt(2, tenantId);
                prepStmt.setString(3, rpdo.getRpUrl());
                prepStmt.setString(4, rpdo.isTrustedAlways() ? "TRUE" : "FALSE");
                prepStmt.setDate(5, new java.sql.Date(rpdo.getLastVisit().getTime()));
                prepStmt.setInt(6, rpdo.getVisitCount() + 1);
                prepStmt.setString(7, rpdo.getDefaultProfileName());
                prepStmt.execute();
                IdentityDatabaseUtil.commitTransaction(connection);
            } else {
                // we should create the entry
                if(log.isDebugEnabled()) {
                    log.debug("Failed to update RP: " + rpdo.getRpUrl() + " for user: " + rpdo.getUserName() + ". " +
                            "Entry does not exist in the database.");
                }
            }
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            log.error("Failed to update RP:  " + rpdo.getRpUrl() + " for user: " +
                    rpdo.getUserName() + " Error while accessing the database", e);
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
            IdentityDatabaseUtil.closeConnection(connection);
        }
    }

    /**
     * Remove the entry from the database.
     *
     * @param opdo
     */
    public void delete(OpenIDUserRPDO opdo, int tenantId) {

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;

        try {

            if (isUserRPExist(connection, opdo, tenantId)) {
                prepStmt = connection.prepareStatement(OpenIDSQLQueries.REMOVE_USER_RP);
                prepStmt.setString(1, opdo.getUserName());
                prepStmt.setInt(2, tenantId);
                prepStmt.setString(3, opdo.getRpUrl());
                prepStmt.execute();
                IdentityDatabaseUtil.commitTransaction(connection);
            }

        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            log.error("Failed to remove RP: " + opdo.getRpUrl() + " of user: " + opdo.getUserName(), e);
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
            IdentityDatabaseUtil.closeConnection(connection);
        }
    }

    /**
     * Returns relying party user settings corresponding to a given user name.
     *
     * @param userName Unique user name
     * @param rpUrl    Relying party urlupdateOpenIDUserRPInfo
     * @return A set of OpenIDUserRPDO, corresponding to the provided user name
     * and RP url.
     */
    public OpenIDUserRPDO getOpenIDUserRP(String userName, String rpUrl, int tenantId) {

        Connection connection = IdentityDatabaseUtil.getDBConnection(false);
        PreparedStatement prepStmt = null;
        OpenIDUserRPDO rpdo = new OpenIDUserRPDO();
        rpdo.setUserName(userName);
        rpdo.setRpUrl(rpUrl);

        try {
            if (isUserRPExist(connection, rpdo, tenantId)) {
                prepStmt = connection.prepareStatement(OpenIDSQLQueries.LOAD_USER_RP);
                prepStmt.setString(1, userName);
                prepStmt.setInt(2, tenantId);
                prepStmt.setString(3, rpUrl);
                OpenIDUserRPDO openIDUserRPDO = buildUserRPDO(prepStmt.executeQuery(), userName);
                return openIDUserRPDO;
            } else {
                if(log.isDebugEnabled()) {
                    log.debug("RP: " + rpUrl + " of user: " + userName + " not found in the database");
                }
            }
        } catch (SQLException e) {
            log.error("Failed to load RP: " + rpUrl + " for user: " + userName, e);
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
            IdentityDatabaseUtil.closeConnection(connection);
        }
        return null;
    }

    /**
     * Returns all registered relying parties
     *
     * @return
     */
    public OpenIDUserRPDO[] getAllOpenIDUserRP() {
        Connection connection = IdentityDatabaseUtil.getDBConnection(false);
        PreparedStatement prepStmt = null;
        ResultSet results = null;
        OpenIDUserRPDO[] rpDOs = null;
        List<OpenIDUserRPDO> rpdos = new ArrayList<>();

        try {
            prepStmt = connection.prepareStatement(OpenIDSQLQueries.LOAD_ALL_USER_RPS);
            results = prepStmt.executeQuery();

            while (results.next()) {
                OpenIDUserRPDO rpdo = new OpenIDUserRPDO();
                rpdo.setUserName(results.getString(1));
                rpdo.setRpUrl(results.getString(3));
                rpdo.setTrustedAlways(Boolean.parseBoolean(results.getString(4)));
                rpdo.setLastVisit(results.getDate(5));
                rpdo.setVisitCount(results.getInt(6));
                rpdo.setDefaultProfileName(results.getString(7));
                rpdos.add(rpdo);
            }

            rpDOs = new OpenIDUserRPDO[rpdos.size()];
            rpDOs = rpdos.toArray(rpDOs);
        } catch (SQLException e) {
            log.error("Error while accessing the database to load RPs.", e);
        } finally {
            IdentityDatabaseUtil.closeResultSet(results);
            IdentityDatabaseUtil.closeStatement(prepStmt);
            IdentityDatabaseUtil.closeConnection(connection);
        }
        return rpDOs;
    }

    /**
     * Returns relying party user settings corresponding to a given user name.
     *
     * @param userName Unique user name
     * @return OpenIDUserRPDO, corresponding to the provided user name and RP
     * url.
     */
    public OpenIDUserRPDO[] getOpenIDUserRPs(String userName, int tenantId) {

        Connection connection = IdentityDatabaseUtil.getDBConnection(false);
        PreparedStatement prepStmt = null;
        ResultSet results = null;
        OpenIDUserRPDO[] rpDOs = null;
        List<OpenIDUserRPDO> rpdos = new ArrayList<>();

        try {
            prepStmt = connection.prepareStatement(OpenIDSQLQueries.LOAD_USER_RPS);
            prepStmt.setString(1, userName);
            prepStmt.setInt(2, tenantId);
            results = prepStmt.executeQuery();

            while (results.next()) {
                OpenIDUserRPDO rpdo = new OpenIDUserRPDO();
                rpdo.setUserName(results.getString(1));
                rpdo.setRpUrl(results.getString(3));
                rpdo.setTrustedAlways(Boolean.parseBoolean(results.getString(4)));
                rpdo.setLastVisit(results.getDate(5));
                rpdo.setVisitCount(results.getInt(6));
                rpdo.setDefaultProfileName(results.getString(7));
                rpdos.add(rpdo);
            }

            rpDOs = new OpenIDUserRPDO[rpdos.size()];
            rpDOs = rpdos.toArray(rpDOs);
        } catch (SQLException e) {
            log.error("Error while accessing the database to load RPs", e);
        } finally {
            IdentityDatabaseUtil.closeResultSet(results);
            IdentityDatabaseUtil.closeStatement(prepStmt);
            IdentityDatabaseUtil.closeConnection(connection);
        }
        return rpDOs;
    }

    /**
     * Returns the default user profile corresponding to the given user name and
     * the RP URL.
     *
     * @param userName Unique user name
     * @param rpUrl    Relying party URL
     * @return Default user profile
     */
    public String getOpenIDDefaultUserProfile(String userName, String rpUrl, int tenantId) {

        Connection connection = IdentityDatabaseUtil.getDBConnection(false);
        PreparedStatement prepStmt = null;

        OpenIDUserRPDO rpdo = new OpenIDUserRPDO();
        rpdo.setUserName(userName);
        rpdo.setRpUrl(rpUrl);

        try {

            if (isUserRPExist(connection, rpdo, tenantId)) {
                prepStmt = connection.prepareStatement(OpenIDSQLQueries.LOAD_USER_RP_DEFAULT_PROFILE);
                prepStmt.setString(1, userName);
                prepStmt.setInt(2, tenantId);
                prepStmt.setString(3, rpUrl);
                return prepStmt.executeQuery().getString(7);
            } else {
                if(log.isDebugEnabled()) {
                    log.debug("RP: " + rpUrl + " of user: " + userName + " not found in the database");
                }
            }
        } catch (SQLException e) {
            log.error("Failed to load RP: " + rpUrl + " for user: " + userName, e);
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
            IdentityDatabaseUtil.closeConnection(connection);
        }
        return null;
    }

    /**
     * Checks if the entry exist in the database;
     *
     * @param connection
     * @param rpDo
     * @return
     * @throws SQLException
     */
    private boolean isUserRPExist(Connection connection, OpenIDUserRPDO rpDo, int tenantId) throws SQLException {

        PreparedStatement prepStmt = null;
        ResultSet results = null;
        boolean result = false;

        try {
            prepStmt = connection.prepareStatement(OpenIDSQLQueries.CHECK_USER_RP_EXIST);
            prepStmt.setString(1, rpDo.getUserName());
            prepStmt.setInt(2, tenantId);
            prepStmt.setString(3, rpDo.getRpUrl());
            results = prepStmt.executeQuery();

            if (results != null && results.next()) {
                result = true;
            }

        } finally {
            IdentityDatabaseUtil.closeResultSet(results);
            IdentityDatabaseUtil.closeStatement(prepStmt);

        }
        return result;
    }

    /**
     * Builds the OpenIDUserRPDO using the results
     *
     * @param results
     * @param userName
     * @return
     */
    private OpenIDUserRPDO buildUserRPDO(ResultSet results, String userName) {

        OpenIDUserRPDO rpdo = new OpenIDUserRPDO();

        try {
            if (!results.next()) {
                if (log.isDebugEnabled()) {
                    log.debug("RememberMe token not found for the user " + userName);
                }
                return null;
            }

            rpdo.setUserName(results.getString(1));
            rpdo.setRpUrl(results.getString(3));
            rpdo.setTrustedAlways(Boolean.parseBoolean(results.getString(4)));
            rpdo.setLastVisit(results.getDate(5));
            rpdo.setVisitCount(results.getInt(6));
            rpdo.setDefaultProfileName(results.getString(7));

        } catch (SQLException e) {
            log.error("Error while accessing the database", e);
        }
        return rpdo;
    }

}
