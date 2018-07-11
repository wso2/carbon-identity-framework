/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.remotefetch.core.dao.impl;

import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.remotefetch.common.DeploymentRevision;
import org.wso2.carbon.identity.remotefetch.common.exceptions.RemoteFetchCoreException;
import org.wso2.carbon.identity.remotefetch.core.dao.DeploymentRevisionDAO;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DeploymentRevisionDAOImpl implements DeploymentRevisionDAO {

    private static final String CREATE_REVISION = "INSERT IDN_RF_REVISIONS (CONFIG_ID, FILE_PATH, FILE_HASH," +
            " ITEM_TYPE, ITEM_NAME) VALUES(?,?,?,?.?)";

    private static final String UPDATE_REVISION = "UPDATE IDN_RF_REVISIONS SET CONFIG_ID = ?, FILE_PATH = ?," +
            " FILE_HASH = ?, DEPLOYED_DATE = ?, DEPLOYMENT_STATUS = ?, ITEM_TYPE = ?, ITEM_NAME = ? WHERE ID = ?";

    private static final String DELETE_REVISION = "DELETE FROM IDN_RF_REVISIONS WHERE ID = ?";

    private static final String GET_REVISIONS_BY_CONFIG = "SELECT CONFIG_ID, FILE_PATH, FILE_HASH, DEPLOYED_DATE," +
            " DEPLOYMENT_STATUS, ITEM_TYPE, ITEM_NAME FROM IDN_RF_REVISIONS WHERE CONFIG_ID = ?";

    /**
     * @param deploymentRevision
     * @return
     * @throws RemoteFetchCoreException
     */
    @Override
    public int createDeploymentRevision(DeploymentRevision deploymentRevision) throws RemoteFetchCoreException {

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement addStmnt = null;
        ResultSet result = null;
        try {
            addStmnt = connection.prepareStatement(DeploymentRevisionDAOImpl.CREATE_REVISION,
                    Statement.RETURN_GENERATED_KEYS);
            addStmnt.setInt(1,deploymentRevision.getConfigId());
            addStmnt.setString(2,deploymentRevision.getFile().getPath());
            addStmnt.setString(3,deploymentRevision.getFileHash());
            addStmnt.setString(4,deploymentRevision.getItemType());
            addStmnt.setString(5,deploymentRevision.getItemName());
            addStmnt.execute();

            int configId = -1;
            result = addStmnt.getGeneratedKeys();

            // TODO if no ID SELECT and return id

            if (!connection.getAutoCommit()) {
                connection.commit();
            }

            return configId;

        } catch (SQLIntegrityConstraintViolationException e){
            throw new RemoteFetchCoreException("Constraint violation, duplicated entry",e);
        } catch(SQLException e){
            throw new RemoteFetchCoreException("Error creating new object",e);
        } finally {
            IdentityDatabaseUtil.closeResultSet(result);
            IdentityDatabaseUtil.closeStatement(addStmnt);
            IdentityDatabaseUtil.closeConnection(connection);
        }
    }

    /**
     * @param deploymentRevision
     * @throws RemoteFetchCoreException
     */
    @Override
    public void updateDeploymentRevision(DeploymentRevision deploymentRevision) throws RemoteFetchCoreException {

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement updateStmnt = null;
        try {
            updateStmnt = connection.prepareStatement(DeploymentRevisionDAOImpl.UPDATE_REVISION);

            updateStmnt.setInt(1,deploymentRevision.getConfigId());
            updateStmnt.setString(2,deploymentRevision.getFile().getPath());
            updateStmnt.setString(3,deploymentRevision.getFileHash());
            updateStmnt.setTimestamp(4, new Timestamp(deploymentRevision.getDeployedDate().getTime()));
            updateStmnt.setString(5,deploymentRevision.getDeploymentStatus());
            updateStmnt.setString(4,deploymentRevision.getItemType());
            updateStmnt.setString(5,deploymentRevision.getItemName());
            updateStmnt.execute();

            if (!connection.getAutoCommit()) {
                connection.commit();
            }


        } catch (SQLIntegrityConstraintViolationException e){
            throw new RemoteFetchCoreException("Constraint violation, duplicated entry",e);
        } catch (SQLException e){
            throw new RemoteFetchCoreException("Error updating object",e);
        } finally {
            IdentityDatabaseUtil.closeStatement(updateStmnt);
            IdentityDatabaseUtil.closeConnection(connection);
        }

    }

    /**
     * @param deploymentRevisionId
     * @throws RemoteFetchCoreException
     */
    @Override
    public void deleteDeploymentRevision(int deploymentRevisionId) throws RemoteFetchCoreException {
        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement deleteStmnt = null;
        ResultSet result = null;
        try {
            deleteStmnt = connection.prepareStatement(DeploymentRevisionDAOImpl.DELETE_REVISION);
            deleteStmnt.setInt(1,deploymentRevisionId);
            deleteStmnt.execute();

            if (!connection.getAutoCommit()) {
                connection.commit();
            }

        } catch (SQLException e){
            throw new RemoteFetchCoreException("Error Deleting object",e);
        } finally {
            IdentityDatabaseUtil.closeResultSet(result);
            IdentityDatabaseUtil.closeStatement(deleteStmnt);
            IdentityDatabaseUtil.closeConnection(connection);

        }
    }

    /**
     * @param remoteFetchConfigurationId
     * @return
     * @throws RemoteFetchCoreException
     */
    @Override
    public List<DeploymentRevision> getDeploymentRevisionsByDeploymentFetchConfigurationId(
            int remoteFetchConfigurationId) throws RemoteFetchCoreException {
        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement selectStmnt = null;
        ResultSet result = null;
        List<DeploymentRevision> deploymentRevisions = new ArrayList<>();
        try {
            selectStmnt = connection.prepareStatement(DeploymentRevisionDAOImpl.GET_REVISIONS_BY_CONFIG);
            selectStmnt.setInt(1, remoteFetchConfigurationId);
            result = selectStmnt.executeQuery();

            while (result.next()) {
                DeploymentRevision deploymentRevision = new DeploymentRevision(
                        result.getInt(1),
                        new File(result.getString(2))
                );

                deploymentRevision.setFileHash(result.getString(3));
                deploymentRevision.setDeployedDate(new Date(result.getTimestamp(4).getTime()));
                deploymentRevision.setDeploymentStatus(result.getString(5));
                deploymentRevision.setItemType(result.getString(6));
                deploymentRevision.setItemName(result.getString(7));

                deploymentRevisions.add(deploymentRevision);
            }

            return deploymentRevisions;
        } catch (SQLException e){
            throw new RemoteFetchCoreException("Error reading objects from database",e);
        } finally {
            IdentityDatabaseUtil.closeResultSet(result);
            IdentityDatabaseUtil.closeStatement(selectStmnt);
            IdentityDatabaseUtil.closeConnection(connection);

        }
    }
}
