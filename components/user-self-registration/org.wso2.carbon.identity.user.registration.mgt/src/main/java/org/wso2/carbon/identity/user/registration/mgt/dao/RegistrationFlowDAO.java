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
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
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
import org.wso2.carbon.identity.user.registration.mgt.model.ActionDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.ComponentDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.ExecutorDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.NodeConfig;
import org.wso2.carbon.identity.user.registration.mgt.model.NodeEdge;
import org.wso2.carbon.identity.user.registration.mgt.model.RegistrationFlowConfig;
import org.wso2.carbon.identity.user.registration.mgt.model.RegistrationFlowDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.StepDTO;
import org.wso2.carbon.identity.user.registration.mgt.utils.RegistrationMgtUtils;

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

    public void addRegistrationFlow(RegistrationFlowConfig regFlowConfig, int tenantId, String flowName,
                                    boolean isDefault) {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();

        String flowId = regFlowConfig.getId();

        try {
            jdbcTemplate.withTransaction(template -> {
                // Insert into IDN_FLOW
                template.executeInsert(
                        "INSERT INTO IDN_FLOW (ID, TENANT_ID, FLOW_NAME, TYPE, IS_DEFAULT) VALUES (?, ?, ?, ?, ?)",
                        preparedStatement -> {
                            preparedStatement.setString(1, flowId);
                            preparedStatement.setInt(2, tenantId);
                            preparedStatement.setString(3, flowId);
                            preparedStatement.setString(4, "REGISTRATION");
                            preparedStatement.setBoolean(5, isDefault);
                        }, regFlowConfig, false);

                // Insert into IDN_FLOW_NODE
                Map<String, Integer> nodeIdToRegNodeIdMap = new HashMap<>();
                for (Map.Entry<String, NodeConfig> entry : regFlowConfig.getNodeConfigs().entrySet()) {
                    NodeConfig node = entry.getValue();
                    int regNodeId = template.executeInsert(
                            "INSERT INTO IDN_FLOW_NODE (NODE_ID, FLOW_ID, NODE_TYPE, IS_FIRST_NODE) VALUES (?, ?, ?, " +
                                    "?)",
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
                                "INSERT INTO IDN_FLOW_NODE_EXECUTOR (FLOW_NODE_ID, EXECUTOR_NAME, IDP_NAME) " +
                                        "VALUES (?, ?, ?)",
                                preparedStatement -> {
                                    preparedStatement.setInt(1, nodeIdToRegNodeIdMap.get(node.getUuid()));
                                    preparedStatement.setString(2, executorConfig.getName());
                                    preparedStatement.setString(3, executorConfig.getIdpName());
                                }, null, false);
                    }
                }
                // Insert graph edges into IDN_FLOW_NODE_MAPPING
                for (Map.Entry<String, NodeConfig> entry : regFlowConfig.getNodeConfigs().entrySet()) {
                    NodeConfig node = entry.getValue();
                    if (node.getEdges() != null) {
                        for (NodeEdge edge : node.getEdges()) {
                            template.executeInsert(
                                    "INSERT INTO IDN_FLOW_NODE_MAPPING (FLOW_NODE_ID, NEXT_NODE_ID, " +
                                            "TRIGGERING_ELEMENT) VALUES (?, ?, ?)",
                                    preparedStatement -> {
                                        preparedStatement.setInt(1, nodeIdToRegNodeIdMap.get(edge.getSourceNodeId()));
                                        preparedStatement.setInt(2, nodeIdToRegNodeIdMap.get(edge.getTargetNodeId()));
                                        preparedStatement.setString(3, edge.getTriggeringActionId());
                                    }, null, false);
                        }
                    }
                }

                // Insert into IDN_FLOW_PAGE
                for (Map.Entry<String, StepDTO> entry : regFlowConfig.getNodePageMappings().entrySet()) {

                    StepDTO stepDTO = entry.getValue();
                    int regNodeId = nodeIdToRegNodeIdMap.get(entry.getKey());

                    int pageAutoIncId = template.executeInsert(
                            "INSERT INTO IDN_FLOW_PAGE (FLOW_ID, FLOW_NODE_ID, PAGE_CONTENT, TYPE) VALUES (?, ?, ?, ?)",
                            preparedStatement -> {
                                preparedStatement.setString(1, flowId);
                                preparedStatement.setInt(2, regNodeId);
                                preparedStatement.setBinaryStream(3, serializePageContent(stepDTO));
                                preparedStatement.setString(4, stepDTO.getType());
                            }, entry, true);

                    // Insert into IDN_FLOW_PAGE_META
                    template.executeInsert(
                            "INSERT INTO IDN_FLOW_PAGE_META (PAGE_ID, COORDINATE_X, COORDINATE_Y, HEIGHT, WIDTH) " +
                                    "VALUES (?, ?, ?, ?, ?)",
                            preparedStatement -> {
                                preparedStatement.setInt(1, pageAutoIncId);
                                preparedStatement.setDouble(2, stepDTO.getCoordinateX());
                                preparedStatement.setDouble(3, stepDTO.getCoordinateY());
                                preparedStatement.setDouble(4, stepDTO.getCoordinateX());
                                preparedStatement.setDouble(5, stepDTO.getCoordinateY());
                            }, null, false);
                }
                return null;
            });
        } catch (TransactionException e) {
            LOG.error("Failed to store the flow object", e);
        }
    }

    public RegistrationFlowDTO getDefaultRegistrationFlowByTenant(int tenantId) {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();

        String query =
                "SELECT P.ID AS PAGE_ID, P.STEP_ID, P.PAGE_CONTENT, P.TYPE AS PAGE_TYPE, M.COORDINATE_X, M" +
                        ".COORDINATE_Y, M.HEIGHT, M.WIDTH FROM IDN_FLOW F JOIN IDN_FLOW_PAGE P ON F.ID = P.FLOW_ID " +
                        "LEFT JOIN IDN_FLOW_PAGE_META M ON P.ID = M.PAGE_ID WHERE F.TENANT_ID = ? AND F.IS_DEFAULT = " +
                        "TRUE AND F.TYPE = 'REGISTRATION';";

        try {
            return jdbcTemplate.fetchSingleRecord(query, (resultSet, rowNumber) -> {
                RegistrationFlowDTO registrationFlowDTO = new RegistrationFlowDTO();
                StepDTO stepDTO = new StepDTO.Builder()
                        .id(resultSet.getString("STEP_ID"))
                        .type(resultSet.getString("PAGE_TYPE"))
                        .coordinateX(resultSet.getDouble("COORDINATE_X"))
                        .coordinateY(resultSet.getDouble("COORDINATE_Y"))
                        .height(resultSet.getDouble("HEIGHT"))
                        .width(resultSet.getDouble("WIDTH"))
                        .build();
                resolvePageContent(stepDTO, resultSet.getBinaryStream("PAGE_CONTENT"));
                registrationFlowDTO.getSteps().add(stepDTO);
                return registrationFlowDTO;
            }, preparedStatement -> preparedStatement.setInt(1, tenantId));
        } catch (DataAccessException e) {
            LOG.error("Failed to retrieve the default registration flow for tenant: " + tenantId, e);
            return null;
        }
    }

//    public RegistrationFlowConfig getDefaultRegistrationFlowConfigByTenant(int tenantId) {
//
//        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
//
//        String query = "SELECT F.ID AS FLOW_ID, F.TENANT_ID, F.FLOW_NAME, F.TYPE AS FLOW_TYPE, F.IS_DEFAULT, " +
//                "N.ID AS NODE_DB_ID, N.NODE_ID, N.NODE_TYPE, N.IS_FIRST_NODE, NM.NEXT_NODE_ID, NM.TRIGGERING_ELEMENT, " +
//                "P.ID AS PAGE_ID, P.STEP_ID, P.PAGE_CONTENT, P.TYPE AS PAGE_TYPE, E.EXECUTOR_NAME, E.IDP_NAME AS AUTHENTICATOR_ID " +
//                "FROM IDN_FLOW F JOIN IDN_FLOW_NODE N ON F.ID = N.FLOW_ID " +
//                "LEFT JOIN IDN_FLOW_NODE_MAPPING NM ON N.ID = NM.FLOW_NODE_ID " +
//                "LEFT JOIN IDN_FLOW_PAGE P ON N.ID = P.FLOW_NODE_ID " +
//                "LEFT JOIN IDN_FLOW_NODE_EXECUTOR E ON N.ID = E.FLOW_NODE_ID " +
//                "WHERE F.TENANT_ID = ? AND F.IS_DEFAULT = TRUE AND F.TYPE = 'REGISTRATION';";
//
//        try {
//            return jdbcTemplate.fetchSingleRecord(query, (resultSet, rowNumber) -> {
//                RegistrationFlowConfig flowConfig = new RegistrationFlowConfig();
//                flowConfig.setId(resultSet.getString("FLOW_ID"));
//                flowConfig.setFirstNodeId(resultSet.getString("NODE_ID"));
//
//                do {
//                    String nodeId = resultSet.getString("NODE_ID");
//                    NodeConfig nodeConfig = flowConfig.getNodeConfigs().computeIfAbsent(nodeId, k -> new NodeConfig());
//                    nodeConfig.setUuid(nodeId);
//                    nodeConfig.setType(resultSet.getString("NODE_TYPE"));
//                    nodeConfig.setFirstNode(resultSet.getBoolean("IS_FIRST_NODE"));
//
//                    if (resultSet.getString("NEXT_NODE_ID") != null) {
//
//                        int
//                        NodeEdge edge = new NodeEdge(nodeId, );
//                        edge.setSourceNodeId(nodeId);
//                        edge.setTargetNodeId(resultSet.getString("NEXT_NODE_ID"));
//                        edge.setTriggeringActionId(resultSet.getString("TRIGGERING_ELEMENT"));
//                        nodeConfig.getEdges().add(edge);
//                    }
//
//                    if (resultSet.getString("PAGE_ID") != null) {
//                        StepDTO stepDTO = new StepDTO.Builder()
//                                .id(resultSet.getString("STEP_ID"))
//                                .type(resultSet.getString("PAGE_TYPE"))
//                                .build();
//                        resolvePageContent(stepDTO, resultSet.getBinaryStream("PAGE_CONTENT"));
//                        flowConfig.addNodePageMapping(nodeId, stepDTO);
//                    }
//
//                    if (resultSet.getString("EXECUTOR_NAME") != null) {
//                        ExecutorDTO executorDTO = new ExecutorDTO();
//                        executorDTO.setName(resultSet.getString("EXECUTOR_NAME"));
//                        executorDTO.setIdpName(resultSet.getString("AUTHENTICATOR_ID"));
//                        nodeConfig.setExecutorConfig(executorDTO);
//                    }
//
//                } while (resultSet.next());
//
//                return flowConfig;
//            }, preparedStatement -> preparedStatement.setInt(1, tenantId));
//        } catch (DataAccessException e) {
//            LOG.error("Failed to retrieve the default registration flow config for tenant: " + tenantId, e);
//            return null;
//        }
//
//    }

    private InputStream serializePageContent(StepDTO stepDTO) {

        if (Constants.StepTypes.VIEW.equals(stepDTO.getType())) {

            return new ByteArrayInputStream(RegistrationMgtUtils.getComponentDTOs(stepDTO.getData()).toString().getBytes());
        } else {
            return new ByteArrayInputStream(RegistrationMgtUtils.getActionDTO(stepDTO.getData()).toString().getBytes());
        }
    }

    private void resolvePageContent(StepDTO stepDTO, InputStream pageContent) {

        try (ObjectInputStream ois = new ObjectInputStream(pageContent)) {
            Object obj = ois.readObject();
            if (Constants.StepTypes.VIEW.equals(stepDTO.getType())) {
                if (obj instanceof List<?>) {
                    List<?> tempList = (List<?>) obj;
                    if (!tempList.isEmpty() &&
                            tempList.get(0) instanceof ComponentDTO) {
                        List<ComponentDTO> blocks = tempList.stream()
                                .map(ComponentDTO.class::cast)
                                .collect(Collectors.toList());
                        stepDTO.addData(Constants.Fields.COMPONENTS, blocks);
                    } else {
                        LOG.error("Deserialized list does not contain BlockDTO objects.");
                    }
                }
            } else if (Constants.StepTypes.REDIRECTION.equals(stepDTO.getType())) {
                if (obj instanceof ActionDTO) {
                    ActionDTO action = (ActionDTO) obj;
                    stepDTO.addData(Constants.Fields.ACTION, action);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
