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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.remotefetch.common.exceptions.RemoteFetchCoreException;
import org.wso2.carbon.identity.remotefetch.common.RemoteFetchConfiguration;
import org.wso2.carbon.identity.remotefetch.core.dao.RemoteFetchConfigurationDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

/**
 * this class accesses IDN_RF_CONFIG table to store/update and delete Remote Fetch configurations.
 */
public class RemoteFetchConfigurationDAOImpl implements RemoteFetchConfigurationDAO {

    private static final String CREATE_CONFIG = "INSERT IDN_RF_CONFIG (TENANT_ID, REPO_CONNECTOR_TYPE, ACTION_LISTENER_TYPE, " +
            "CONFIG_DEPLOYER_TYPE, ATTRIBUTES_JSON) VALUES (?,?,?,?,?)";

    private static final String LIST_CONFIGS = "SELECT ID, TENANT_ID, REPO_CONNECTOR_TYPE, ACTION_LISTENER_TYPE," +
            " CONFIG_DEPLOYER_TYPE, ATTRIBUTES_JSON FROM `IDN_RF_CONFIG`";

    private static final String GET_CONFIG = "SELECT ID, TENANT_ID, REPO_CONNECTOR_TYPE, ACTION_LISTENER_TYPE," +
            " CONFIG_DEPLOYER_TYPE, ATTRIBUTES_JSON FROM `IDN_RF_CONFIG` WHERE ID = ?";

    private static final String UPDATE_CONFIG = "UPDATE IDN_RF_CONFIG SET TENANT_ID = ?, REPO_CONNECTOR_TYPE = ?, " +
            "ACTION_LISTENER_TYPE = ?, CONFIG_DEPLOYER_TYPE = ?, ATTRIBUTES_JSON = ? WHERE ID = ?";

    private static final String DELETE_CONFIG = "DELETE FROM IDN_RF_CONFIG WHERE ID = ?";

    private Log log = LogFactory.getLog(RemoteFetchConfigurationDAOImpl.class);

    public RemoteFetchConfigurationDAOImpl() {

    }

    /**
     *
     * @param configuration
     * @return
     * @throws RemoteFetchCoreException
     */
    @Override
    public int createRemoteFetchConfiguration(RemoteFetchConfiguration configuration) throws RemoteFetchCoreException {
        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement addStmnt = null;
        ResultSet result = null;
        try {
            addStmnt = connection.prepareStatement(RemoteFetchConfigurationDAOImpl.CREATE_CONFIG, Statement.RETURN_GENERATED_KEYS);
            addStmnt.setInt(1,configuration.getTenantId());
            addStmnt.setString(2,configuration.getRepositoryConnectorType());
            addStmnt.setString(3,configuration.getActionListenerType());
            addStmnt.setString(4,configuration.getConfgiurationDeployerType());

            //Encode object attributes to JSON
            JSONObject attributesBundle = new JSONObject();
            attributesBundle.put("repositoryConnectorAttributes",configuration.getRepositoryConnectorAttributes());
            attributesBundle.put("actionListenerAttributes",configuration.getActionListenerAttributes());
            attributesBundle.put("confgiurationDeployerAttributes",configuration.getConfgiurationDeployerAttributes());

            addStmnt.setString(5,attributesBundle.toString(4));
            addStmnt.execute();

            int configId = -1;
            result = addStmnt.getGeneratedKeys();

            // TODO if no ID SELECT and return id

            if (!connection.getAutoCommit()) {
                connection.commit();
            }

            return configId;

        } catch (SQLException e){
            throw new RemoteFetchCoreException("Error creating new object",e);
        } finally {
            IdentityDatabaseUtil.closeResultSet(result);
            IdentityDatabaseUtil.closeStatement(addStmnt);
            IdentityDatabaseUtil.closeConnection(connection);

        }

    }

    /**
     * @param configurationId
     * @return
     * @throws RemoteFetchCoreException
     */
    @Override
    public RemoteFetchConfiguration getRemoteFetchConfiguration(int configurationId) throws RemoteFetchCoreException {
        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement selectStmnt = null;
        ResultSet result = null;
        try {
            selectStmnt = connection.prepareStatement(RemoteFetchConfigurationDAOImpl.GET_CONFIG);
            selectStmnt.setInt(1,configurationId);
            result = selectStmnt.executeQuery();

            if(result.next()){
                JSONObject attributesBundle = new JSONObject(result.getString(6));
                return new RemoteFetchConfiguration(
                        result.getInt(1),
                        result.getInt(2),
                        result.getString(3),
                        result.getString(4),
                        result.getString(5),
                        this.attributeToMap(attributesBundle.getJSONObject("repositoryConnectorAttributes")),
                        this.attributeToMap(attributesBundle.getJSONObject("actionListenerAttributes")),
                        this.attributeToMap(attributesBundle.getJSONObject("confgiurationDeployerAttributes"))
                );
            }else {
                return null;
            }

        } catch (SQLException e){
            throw new RemoteFetchCoreException("Error reading objects from database",e);
        } finally {
            IdentityDatabaseUtil.closeResultSet(result);
            IdentityDatabaseUtil.closeStatement(selectStmnt);
            IdentityDatabaseUtil.closeConnection(connection);

        }
    }

    /**
     *
     * @param configuration
     * @throws RemoteFetchCoreException
     */
    @Override
    public void updateRemoteFetchConfiguration(RemoteFetchConfiguration configuration) throws RemoteFetchCoreException {
        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement updateStmnt = null;
        ResultSet result = null;
        try {
            updateStmnt = connection.prepareStatement(RemoteFetchConfigurationDAOImpl.UPDATE_CONFIG);
            updateStmnt.setInt(1,configuration.getTenantId());
            updateStmnt.setString(2,configuration.getRepositoryConnectorType());
            updateStmnt.setString(3,configuration.getActionListenerType());
            updateStmnt.setString(4,configuration.getConfgiurationDeployerType());

            //Encode object attributes to JSON
            JSONObject attributesBundle = new JSONObject();
            attributesBundle.put("repositoryConnectorAttributes",configuration.getRepositoryConnectorAttributes());
            attributesBundle.put("actionListenerAttributes",configuration.getActionListenerAttributes());
            attributesBundle.put("confgiurationDeployerAttributes",configuration.getConfgiurationDeployerAttributes());

            updateStmnt.setString(5,attributesBundle.toString(4));
            updateStmnt.setInt(6,configuration.getRemoteFetchConfigurationId());
            updateStmnt.execute();

            if (!connection.getAutoCommit()) {
                connection.commit();
            }

        } catch (SQLException e){
            throw new RemoteFetchCoreException("Error creating new object",e);
        } finally {
            IdentityDatabaseUtil.closeResultSet(result);
            IdentityDatabaseUtil.closeStatement(updateStmnt);
            IdentityDatabaseUtil.closeConnection(connection);

        }
    }

    /**
     * @param configurationId
     * @throws RemoteFetchCoreException
     */
    @Override
    public void deleteRemoteFetchConfiguration(int configurationId) throws RemoteFetchCoreException {
        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement deleteStmnt = null;
        ResultSet result = null;
        try {
            deleteStmnt = connection.prepareStatement(RemoteFetchConfigurationDAOImpl.DELETE_CONFIG);
            deleteStmnt.setInt(1,configurationId);
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
     *
     * @return
     * @throws RemoteFetchCoreException
     */
    @Override
    public List<RemoteFetchConfiguration> getAllRemoteFetchConfigurations() throws RemoteFetchCoreException {
        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement selectStmnt = null;
        ResultSet result = null;
        List<RemoteFetchConfiguration> rfcList = new ArrayList<>();
        try {
            selectStmnt = connection.prepareStatement(RemoteFetchConfigurationDAOImpl.LIST_CONFIGS);
            result = selectStmnt.executeQuery();

            while (result.next()){

                JSONObject attributesBundle = new JSONObject(result.getString(6));
                RemoteFetchConfiguration rfc = new RemoteFetchConfiguration(
                        result.getInt(1),
                        result.getInt(2),
                        result.getString(3),
                        result.getString(4),
                        result.getString(5),
                        this.attributeToMap(attributesBundle.getJSONObject("repositoryConnectorAttributes")),
                        this.attributeToMap(attributesBundle.getJSONObject("actionListenerAttributes")),
                        this.attributeToMap(attributesBundle.getJSONObject("confgiurationDeployerAttributes"))
                );
                rfcList.add(rfc);
            }

            return rfcList;

        } catch (SQLException e){
            throw new RemoteFetchCoreException("Error reading objects from database",e);
        } finally {
            IdentityDatabaseUtil.closeResultSet(result);
            IdentityDatabaseUtil.closeStatement(selectStmnt);
            IdentityDatabaseUtil.closeConnection(connection);

        }
    }

    private Map<String,String> attributeToMap(JSONObject attributes){
        Map<String,String> attrMap = new HashMap<>();
        attributes.keySet().forEach((Object key) -> {
            if(key.getClass() == String.class){
                attrMap.put((String) key, attributes.getString((String) key));
            }
        });
        return attrMap;
    }
}
