/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.user.registration.mgt.dao;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.database.utils.jdbc.exceptions.TransactionException;
import org.wso2.carbon.identity.core.util.JdbcUtils;
import org.wso2.carbon.identity.user.registration.mgt.Constants;
import org.wso2.carbon.identity.user.registration.mgt.model.ExecutorDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.NodeConfig;
import org.wso2.carbon.identity.user.registration.mgt.model.RegistrationFlowConfig;
import org.wso2.carbon.identity.user.registration.mgt.model.RegistrationFlowDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.StepDTO;

/**
 * The DAO class for the registration flow.
 */
public class RegistrationFlowDAO {

    private static final Log LOG = LogFactory.getLog(RegistrationFlowDAO.class);

    private static final RegistrationFlowDAO instance = new RegistrationFlowDAO();

    private RegistrationFlowDAO() {

    }

    public static RegistrationFlowDAO getInstance() {

        return instance;
    }

    public void addRegistrationFlow(RegistrationFlowConfig regFlowConfig, int tenantId) {

            JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();

            String flowId = regFlowConfig.getId();

            try {
                jdbcTemplate.withTransaction(template -> {
                    // Insert into IDN_FLOW
                    template.executeInsert(
                            "INSERT INTO IDN_FLOW (ID, TENANT_ID, FLOW_NAME, TYPE) VALUES (?, ?, ?, ?)",
                            preparedStatement -> {
                                preparedStatement.setString(1, flowId);
                                preparedStatement.setInt(2, tenantId);
                                preparedStatement.setString(3, regFlowConfig.getName());
                                preparedStatement.setString(4, "REGISTRATION");
                            }, regFlowConfig, false);

                    // Insert into IDN_FLOW_NODE
                    Map<String, Integer> nodeIdToRegNodeIdMap = new HashMap<>();
                    for (Map.Entry<String, NodeConfig> entry : regFlowConfig.getNodeConfigs().entrySet()) {
                        NodeConfig node = entry.getValue();
                        int regNodeId = template.executeInsert("INSERT INTO IDN_FLOW_NODE (NODE_ID, FLOW_ID, NODE_TYPE, IS_FIRST_NODE) VALUES (?, ?, ?, ?)",
                                                               preparedStatement -> {
                                                                   preparedStatement.setString(1, node.getUuid());
                                                                   preparedStatement.setString(2, flowId);
                                                                   preparedStatement.setString(3, node.getType());
                                                                   preparedStatement.setBoolean(4, node.isFirstNode());
                                                               }, entry, true);

                        nodeIdToRegNodeIdMap.put(node.getUuid(), regNodeId);

                        // Insert into IDN_FLOW_NODE_EXECUTOR
                        ExecutorDTO executorConfig = node.getExecutorConfig();
                        if (executorConfig != null) {
                            template.executeInsert(
                                    "INSERT INTO IDN_FLOW_NODE_EXECUTOR (FLOW_NODE_ID, EXECUTOR_NAME, AUTHENTICATOR_ID) VALUES (?, ?, ?)",
                                    preparedStatement -> {
                                        preparedStatement.setInt(1, nodeIdToRegNodeIdMap.get(node.getUuid()));
                                        preparedStatement.setString(2, executorConfig.getName());
                                        preparedStatement.setString(3, executorConfig.getIdpName());
                                    }, null, false);
                        }
                    }

                    // Insert into IDN_FLOW_PAGE
                    for (Map.Entry<String, StepDTO> entry : regFlowConfig.getNodePageMappings().entrySet()) {
                        int regNodeId = nodeIdToRegNodeIdMap.get(entry.getKey());

                        int pageAutoIncId = template.executeInsert(
                                "INSERT INTO IDN_FLOW_PAGE (FLOW_ID, FLOW_NODE_ID, PAGE_CONTENT, TYPE) VALUES (?, ?, ?, ?)",
                                preparedStatement -> {
                                    preparedStatement.setString(1, flowId);
                                    preparedStatement.setInt(2, regNodeId);
                                    preparedStatement.setBlob(3, resolvePageContent(entry.getValue()));
                                    preparedStatement.setString(4, entry.getValue().getType());
                                }, entry, true);


                    }
                    return null;
                });
            } catch (TransactionException e) {
                LOG.error("Failed to store the flow object", e);
            }
        }

//    /**
//     * Add the registration object to the database.
//     *
//     * @param regDTO   The registration object.
//     * @param tenantId The tenant ID.
//     */
//    public void addRegistrationObject(RegistrationFlowConfig regDTO, int tenantId) {
//
//        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
//
//        try {
//            jdbcTemplate.withTransaction(template -> {
//                // Insert into REG_FLOW
//                template.executeInsert(
//                        "INSERT INTO REG_FLOW (ID, TENANT_ID, FLOW_NAME, FLOW_JSON) VALUES (?, ?, ?, ?)",
//                        preparedStatement -> {
//                            preparedStatement.setString(1, regDTO.getId());
//                            preparedStatement.setInt(2, tenantId);
//                            preparedStatement.setString(3, "default");
//                        }, regDTO, false);
//
//                // Insert into REG_NODE
//                Map<String, Integer> nodeIdToRegNodeIdMap = new HashMap<>();
//                for (Map.Entry<String, NodeConfig> entry : regDTO.getNodeConfigs().entrySet()) {
//                    NodeConfig node = entry.getValue();
//                    int regNodeId = template.executeInsert("INSERT INTO REG_NODE (NODE_ID, FLOW_ID, NODE_TYPE, " +
//                                                                   "IS_FIRST_NODE) VALUES (?, ?, ?, ?)",
//                                                           preparedStatement -> {
//                                                               preparedStatement.setString(1, node.getUuid());
//                                                               preparedStatement.setString(2, regDTO.getId());
//                                                               preparedStatement.setString(3, node.getType());
//                                                               preparedStatement.setBoolean(4, node.isFirstNode());
//                                                           }, entry, true);
//
//                    nodeIdToRegNodeIdMap.put(node.getUuid(), regNodeId);
//                    // Insert into REG_NODE_PROPERTIES
//                    ExecutorDTO executorConfig = node.getExecutorConfig();
//                    if (executorConfig != null) {
//                        template.executeInsert(
//                                "INSERT INTO REG_NODE_PROPERTIES (REG_NODE_ID, PROPERTY_KEY, PROPERTY_VALUE) VALUES " +
//                                        "(?, " +
//                                        "?, ?)",
//                                preparedStatement -> {
//                                    preparedStatement.setInt(1, nodeIdToRegNodeIdMap.get(node.getUuid()));
//                                    preparedStatement.setString(2, Constants.EXECUTOR_NAME);
//                                    preparedStatement.setString(3, executorConfig.getName());
//                                }, null, false);
//
//                        template.executeInsert(
//                                "INSERT INTO REG_NODE_PROPERTIES (REG_NODE_ID, PROPERTY_KEY, PROPERTY_VALUE) VALUES " +
//                                        "(?, " +
//                                        "?, ?)",
//                                preparedStatement -> {
//                                    preparedStatement.setInt(1, nodeIdToRegNodeIdMap.get(node.getUuid()));
//                                    preparedStatement.setString(2, Constants.AUTHENTICATOR_ID);
//                                    preparedStatement.setString(3, executorConfig.getAuthenticatorId());
//                                }, null, false);
//                    }
//                }
//
//                // Insert into REG_PAGE
////                for (Map.Entry<String, String> entry : regDTO.getNodePageMappings().entrySet()) {
////                    int regNodeId = nodeIdToRegNodeIdMap.get(entry.getKey());
////                    template.executeInsert(
////                            "INSERT INTO REG_PAGE (REG_NODE_ID, PAGE_CONTENT) VALUES (?, ?)",
////                            preparedStatement -> {
////                                preparedStatement.setInt(1, regNodeId);
////                                preparedStatement.setBlob(2, new ByteArrayInputStream(entry.getValue().getBytes()));
////                            }, entry, false);
////                }
//                return null;
//            });
//        } catch (TransactionException e) {
//            LOG.error("Failed to store the flow object", e);
//        }
//    }

    public RegistrationFlowConfig getRegistrationObjectByTenantId(int tenantId) {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            // Retrieve REG_FLOW
            RegistrationFlowConfig registrationFlowConfig =
                    jdbcTemplate.fetchSingleRecord("SELECT * FROM REG_FLOW WHERE TENANT_ID = ?",
                                                   (resultSet, rowNumber) -> {
                                                       RegistrationFlowConfig config = new RegistrationFlowConfig();
                                                       config.setId(resultSet.getString("ID"));
                                                       return config;
                                                   }, preparedStatement -> preparedStatement.setInt(1, tenantId));

            if (registrationFlowConfig == null) {
                return null;
            }

            String flowId = registrationFlowConfig.getId();

            // Retrieve REG_NODE
            Map<String, Integer> nodeIdToRegNodeIdMap = new HashMap<>();
            List<NodeConfig> nodes =
                    jdbcTemplate.executeQuery("SELECT * FROM REG_NODE WHERE FLOW_ID = ?", (resultSet, rowNumber) -> {
                        NodeConfig node = new NodeConfig();
                        node.setUuid(resultSet.getString("NODE_ID"));
                        node.setType(resultSet.getString("NODE_TYPE"));
                        node.setFirstNode(resultSet.getBoolean("IS_FIRST_NODE"));
                        int regNodeId = resultSet.getInt("ID");
                        nodeIdToRegNodeIdMap.put(node.getUuid(), regNodeId);
                        return node;
                    }, preparedStatement -> preparedStatement.setString(1, flowId));

            for (NodeConfig node : nodes) {
                // Retrieve REG_NODE_PROPERTIES
                int regNodeId = nodeIdToRegNodeIdMap.get(node.getUuid());
                Map<String, String> properties =
                        jdbcTemplate.executeQuery("SELECT * FROM REG_NODE_PROPERTIES WHERE REG_NODE_ID = ?",
                                                  (resultSet, rowNumber) -> {
                                                      return new AbstractMap.SimpleEntry<>(
                                                              resultSet.getString("PROPERTY_KEY"),
                                                              resultSet.getString("PROPERTY_VALUE"));
                                                  }, preparedStatement -> preparedStatement.setInt(1, regNodeId))
                                .stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                if (properties.containsKey(Constants.EXECUTOR_NAME) &&
                        properties.containsKey(Constants.AUTHENTICATOR_ID)) {
                    ExecutorDTO executorConfig = new ExecutorDTO(properties.get(Constants.EXECUTOR_NAME),
                                                                 properties.get(Constants.AUTHENTICATOR_ID));
                    node.setExecutorConfig(executorConfig);
                }
                registrationFlowConfig.addNodeConfig(node);
            }
            // Retrieve REG_PAGE
            for (NodeConfig node : nodes) {
                int regNodeId = nodeIdToRegNodeIdMap.get(node.getUuid());
                jdbcTemplate.executeQuery("SELECT * FROM REG_PAGE WHERE REG_NODE_ID = ?", (resultSet, rowNumber) -> {
                    String pageContent = new String(resultSet.getBytes("PAGE_CONTENT"));
//                    registrationFlowConfig.addNodePageMapping(node.getUuid(), pageContent);
                    return null;
                }, preparedStatement -> preparedStatement.setInt(1, regNodeId));
            }
            return registrationFlowConfig;
        } catch (DataAccessException e) {
            LOG.error("Failed to retrieve the flow object", e);
            return null;
        }
    }

    /**
     * Retrieve the flow_json as a string by tenant ID.
     *
     * @param tenantId The tenant ID.
     * @return The flow\_json as a string.
     */
    public String getFlowJsonByTenantId(String tenantId) {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            return jdbcTemplate.fetchSingleRecord("SELECT flow_json FROM REG_FLOW WHERE tenant_id = ?",
                                                  (resultSet, rowNumber) -> new String(resultSet.getBytes("flow_json")),
                                                  preparedStatement -> preparedStatement.setString(1, tenantId));
        } catch (DataAccessException e) {
            LOG.error("Failed to retrieve the flow_json", e);
            return null;
        }
    }

    private InputStream resolvePageContent(StepDTO stepDTO) {

        if (Constants.StepTypes.VIEW.equals(stepDTO.getType())){
            return new ByteArrayInputStream(stepDTO.getBlocks().toString().getBytes());
        } else {
            return new ByteArrayInputStream(stepDTO.getActionDTO().toString().getBytes());
        }
    }
}
