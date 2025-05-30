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

import static org.wso2.carbon.identity.user.registration.mgt.Constants.StepTypes.REDIRECTION;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.StepTypes.VIEW;
import static org.wso2.carbon.identity.user.registration.mgt.dao.SQLConstants.DELETE_FLOW;
import static org.wso2.carbon.identity.user.registration.mgt.dao.SQLConstants.GET_FIRST_STEP_ID;
import static org.wso2.carbon.identity.user.registration.mgt.dao.SQLConstants.GET_FLOW;
import static org.wso2.carbon.identity.user.registration.mgt.dao.SQLConstants.GET_NODES_WITH_MAPPINGS_QUERY;
import static org.wso2.carbon.identity.user.registration.mgt.dao.SQLConstants.GET_VIEW_PAGES_IN_FLOW;
import static org.wso2.carbon.identity.user.registration.mgt.dao.SQLConstants.INSERT_FLOW_INTO_IDN_FLOW;
import static org.wso2.carbon.identity.user.registration.mgt.dao.SQLConstants.INSERT_FLOW_NODE_INFO;
import static org.wso2.carbon.identity.user.registration.mgt.dao.SQLConstants.INSERT_FLOW_PAGE_INFO;
import static org.wso2.carbon.identity.user.registration.mgt.dao.SQLConstants.INSERT_FLOW_PAGE_META;
import static org.wso2.carbon.identity.user.registration.mgt.dao.SQLConstants.INSERT_NODE_EDGES;
import static org.wso2.carbon.identity.user.registration.mgt.dao.SQLConstants.INSERT_NODE_EXECUTOR_INFO;
import static org.wso2.carbon.identity.user.registration.mgt.dao.SQLConstants.SQLPlaceholders.DB_SCHEMA_ALIAS_FLOW_ID;
import static org.wso2.carbon.identity.user.registration.mgt.dao.SQLConstants.SQLPlaceholders.DB_SCHEMA_ALIAS_NEXT_NODE_ID;
import static org.wso2.carbon.identity.user.registration.mgt.dao.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_COORDINATE_X;
import static org.wso2.carbon.identity.user.registration.mgt.dao.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_COORDINATE_Y;
import static org.wso2.carbon.identity.user.registration.mgt.dao.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_EXECUTOR_NAME;
import static org.wso2.carbon.identity.user.registration.mgt.dao.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_HEIGHT;
import static org.wso2.carbon.identity.user.registration.mgt.dao.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_IDP_NAME;
import static org.wso2.carbon.identity.user.registration.mgt.dao.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_IS_FIRST_NODE;
import static org.wso2.carbon.identity.user.registration.mgt.dao.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_NODE_ID;
import static org.wso2.carbon.identity.user.registration.mgt.dao.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_NODE_TYPE;
import static org.wso2.carbon.identity.user.registration.mgt.dao.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_PAGE_CONTENT;
import static org.wso2.carbon.identity.user.registration.mgt.dao.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_PAGE_TYPE;
import static org.wso2.carbon.identity.user.registration.mgt.dao.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_STEP_ID;
import static org.wso2.carbon.identity.user.registration.mgt.dao.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_TRIGGERING_ELEMENT;
import static org.wso2.carbon.identity.user.registration.mgt.dao.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_WIDTH;
import static org.wso2.carbon.identity.user.registration.mgt.dao.SQLConstants.SQLPlaceholders.REGISTRATION_FLOW;
import static org.wso2.carbon.identity.user.registration.mgt.utils.RegistrationMgtUtils.handleServerException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.database.utils.jdbc.exceptions.TransactionException;
import org.wso2.carbon.identity.core.util.JdbcUtils;
import org.wso2.carbon.identity.core.util.LambdaExceptionUtils;
import org.wso2.carbon.identity.user.registration.mgt.Constants;
import org.wso2.carbon.identity.user.registration.mgt.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.registration.mgt.exception.RegistrationServerException;
import org.wso2.carbon.identity.user.registration.mgt.model.ActionDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.ComponentDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.DataDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.ExecutorDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.NodeConfig;
import org.wso2.carbon.identity.user.registration.mgt.model.NodeEdge;
import org.wso2.carbon.identity.user.registration.mgt.model.RegistrationFlowDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.RegistrationGraphConfig;
import org.wso2.carbon.identity.user.registration.mgt.model.StepDTO;

/**
 * The DAO class for the registration flow.
 */
public class RegistrationFlowDAOImpl implements RegistrationFlowDAO {

    private static final Log LOG = LogFactory.getLog(RegistrationFlowDAOImpl.class);

    @Override
    public void updateDefaultRegistrationFlowByTenant(RegistrationGraphConfig regFlowConfig, int tenantId,
                                                      String flowName) throws RegistrationFrameworkException {

        String flowId = regFlowConfig.getId();
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.withTransaction(template -> {
                // Delete the existing flow for the tenant.
                template.executeUpdate(DELETE_FLOW,
                        preparedStatement -> {
                            preparedStatement.setInt(1, tenantId);
                            preparedStatement.setBoolean(2, true);
                            preparedStatement.setString(3, REGISTRATION_FLOW);
                        });

                // Insert into IDN_FLOW.
                template.executeInsert(INSERT_FLOW_INTO_IDN_FLOW,
                        preparedStatement -> {
                            preparedStatement.setString(1, flowId);
                            preparedStatement.setInt(2, tenantId);
                            preparedStatement.setString(3, flowName);
                            preparedStatement.setString(4, REGISTRATION_FLOW);
                            preparedStatement.setBoolean(5, true);
                        }, regFlowConfig, false);

                // Insert into IDN_FLOW_NODE.
                Map<String, Integer> nodeIdToRegNodeIdMap = new HashMap<>();
                for (Map.Entry<String, NodeConfig> entry : regFlowConfig.getNodeConfigs().entrySet()) {
                    NodeConfig node = entry.getValue();
                    int regNodeId = template.executeInsert(INSERT_FLOW_NODE_INFO,
                            preparedStatement -> {
                                preparedStatement.setString(1, node.getId());
                                preparedStatement.setString(2, flowId);
                                preparedStatement.setString(3, node.getType());
                                preparedStatement.setBoolean(4, node.isFirstNode());
                            }, entry, true);

                    nodeIdToRegNodeIdMap.put(node.getId(), regNodeId);

                    // Insert into IDN_FLOW_NODE_EXECUTOR.
                    ExecutorDTO executorConfig = node.getExecutorConfig();
                    if (executorConfig != null) {
                        template.executeInsert(INSERT_NODE_EXECUTOR_INFO,
                                preparedStatement -> {
                                    preparedStatement.setInt(1,
                                            nodeIdToRegNodeIdMap.get(node.getId()));
                                    preparedStatement.setString(2, executorConfig.getName());
                                    preparedStatement.setString(3, executorConfig.getIdpName());
                                }, null, false);
                    }
                }
                // Insert graph edges into IDN_FLOW_NODE_MAPPING.
                for (Map.Entry<String, NodeConfig> entry : regFlowConfig.getNodeConfigs().entrySet()) {
                    NodeConfig node = entry.getValue();
                    if (node.getEdges() != null) {
                        for (NodeEdge edge : node.getEdges()) {
                            template.executeInsert(INSERT_NODE_EDGES,
                                    preparedStatement -> {
                                        preparedStatement.setInt(1, nodeIdToRegNodeIdMap.get(
                                                edge.getSourceNodeId()));
                                        preparedStatement.setInt(2, nodeIdToRegNodeIdMap.get(
                                                edge.getTargetNodeId()));
                                        preparedStatement.setString(3, edge.getTriggeringActionId());
                                    }, null, false);
                        }
                    }
                }

                // Insert into IDN_FLOW_PAGE.
                for (Map.Entry<String, StepDTO> entry : regFlowConfig.getNodePageMappings().entrySet()) {

                    StepDTO stepDTO = entry.getValue();
                    Optional<byte[]> pageContent = serializeStepData(stepDTO, tenantId);
                    int regNodeId = nodeIdToRegNodeIdMap.get(entry.getKey());

                    int pageAutoIncId = template.executeInsert(INSERT_FLOW_PAGE_INFO,
                            preparedStatement -> {
                                preparedStatement.setString(1, flowId);
                                preparedStatement.setInt(2, regNodeId);
                                preparedStatement.setString(3, stepDTO.getId());
                                if (pageContent.isPresent()) {
                                    preparedStatement.setBinaryStream(4, new ByteArrayInputStream(pageContent.get()));
                                } else {
                                    preparedStatement.setBinaryStream(4, null);
                                }
                                preparedStatement.setString(5, stepDTO.getType());
                            }, entry, true);

                    // Insert into IDN_FLOW_PAGE_META.
                    template.executeInsert(INSERT_FLOW_PAGE_META,
                            preparedStatement -> {
                                preparedStatement.setInt(1, pageAutoIncId);
                                preparedStatement.setDouble(2, stepDTO.getCoordinateX());
                                preparedStatement.setDouble(3, stepDTO.getCoordinateY());
                                preparedStatement.setDouble(4, stepDTO.getHeight());
                                preparedStatement.setDouble(5, stepDTO.getWidth());
                            }, null, false);
                }
                return null;
            });
        } catch (TransactionException e) {
            throw handleServerException(Constants.ErrorMessages.ERROR_CODE_ADD_DEFAULT_FLOW, e, tenantId);
        }
    }

    @Override
    public RegistrationFlowDTO getDefaultRegistrationFlowByTenant(int tenantId) throws RegistrationServerException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();

        try {
            List<StepDTO> steps = jdbcTemplate
                    .executeQuery(GET_FLOW, (LambdaExceptionUtils.rethrowRowMapper((resultSet, rowNumber) -> {
                        StepDTO stepDTO = new StepDTO.Builder()
                                .id(resultSet.getString(DB_SCHEMA_COLUMN_NAME_STEP_ID))
                                .type(resultSet.getString(DB_SCHEMA_COLUMN_NAME_PAGE_TYPE))
                                .coordinateX(resultSet.getDouble(DB_SCHEMA_COLUMN_NAME_COORDINATE_X))
                                .coordinateY(resultSet.getDouble(DB_SCHEMA_COLUMN_NAME_COORDINATE_Y))
                                .height(resultSet.getDouble(DB_SCHEMA_COLUMN_NAME_HEIGHT))
                                .width(resultSet.getDouble(DB_SCHEMA_COLUMN_NAME_WIDTH))
                                .build();

                        resolvePageContent(stepDTO, resultSet.getBinaryStream(DB_SCHEMA_COLUMN_NAME_PAGE_CONTENT), tenantId);
                        return stepDTO;
                    })), preparedStatement -> {
                        preparedStatement.setInt(1, tenantId);
                        preparedStatement.setBoolean(2, true);
                        preparedStatement.setString(3, REGISTRATION_FLOW);
                    });

            RegistrationFlowDTO registrationFlowDTO = new RegistrationFlowDTO();
            if (steps.isEmpty()) {
                LOG.debug("No steps are found in the default flow of tenant " + tenantId);
                return registrationFlowDTO;
            }
            String firstStepId = getFirstStepId(tenantId);
            StepDTO firstStep = steps.stream()
                    .filter(step -> step.getId().equals(firstStepId))
                    .findFirst()
                    .orElseThrow(() -> handleServerException(Constants.ErrorMessages.ERROR_CODE_INVALID_NODE, firstStepId,
                                                             tenantId));
            if (StringUtils.isNotBlank(firstStepId)) {
                registrationFlowDTO.getSteps().add(firstStep);
                steps.remove(firstStep);
            }
            registrationFlowDTO.getSteps().addAll(steps);
            return registrationFlowDTO;
        } catch (DataAccessException e) {
            throw handleServerException(Constants.ErrorMessages.ERROR_CODE_GET_DEFAULT_FLOW, e, tenantId);
        }
    }

    @Override
    public RegistrationGraphConfig getDefaultRegistrationGraphByTenant(int tenantId) throws RegistrationFrameworkException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            // Step 1: Fetch Nodes with Executors and Mappings.
            List<Map<String, Object>> rows = getGraphNodes(tenantId, jdbcTemplate);

            // Step 2: Process Data to Avoid Duplication.
            RegistrationGraphConfig regGraph = buildGraph(rows);

            // Step 3: Fetch Page Content with JOIN query.
            Map<String, StepDTO> nodePageMappings = getViewPagesForFlow(regGraph.getId(),tenantId, jdbcTemplate);

            // Step 4: Set the page mappings.
            regGraph.setNodePageMappings(nodePageMappings);
            return regGraph;
        } catch (DataAccessException e) {
            LOG.error("Failed to retrieve registration graph for tenant: " + tenantId, e);
            throw handleServerException(Constants.ErrorMessages.ERROR_CODE_GET_REG_GRAPH_FAILED, e, tenantId);
        }
    }

    private String getFirstStepId(int tenantId) throws RegistrationServerException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            List<String> stepIds = jdbcTemplate.executeQuery(GET_FIRST_STEP_ID, (resultSet, rowNumber) -> {
                return resultSet.getString(DB_SCHEMA_COLUMN_NAME_STEP_ID);
            }, preparedStatement -> {
                preparedStatement.setBoolean(1, true);
                preparedStatement.setInt(2, tenantId);
                preparedStatement.setString(3, REGISTRATION_FLOW);
            });
            return stepIds.isEmpty() ? null : stepIds.get(0);
        } catch (DataAccessException e) {
            throw handleServerException(Constants.ErrorMessages.ERROR_CODE_GET_FIRST_STEP_ID, e, tenantId);
        }
    }

    private List<Map<String, Object>> getGraphNodes(int tenantId, JdbcTemplate jdbcTemplate) throws DataAccessException {

        return jdbcTemplate.executeQuery(GET_NODES_WITH_MAPPINGS_QUERY, (resultSet, rowNumber) -> {
            Map<String, Object> row = new HashMap<>();
            row.put(DB_SCHEMA_ALIAS_FLOW_ID, resultSet.getString(DB_SCHEMA_ALIAS_FLOW_ID));
            row.put(DB_SCHEMA_COLUMN_NAME_NODE_ID, resultSet.getString(DB_SCHEMA_COLUMN_NAME_NODE_ID));
            row.put(DB_SCHEMA_COLUMN_NAME_NODE_TYPE, resultSet.getString(DB_SCHEMA_COLUMN_NAME_NODE_TYPE));
            row.put(DB_SCHEMA_COLUMN_NAME_IS_FIRST_NODE, resultSet.getBoolean(DB_SCHEMA_COLUMN_NAME_IS_FIRST_NODE));
            row.put(DB_SCHEMA_COLUMN_NAME_EXECUTOR_NAME, resultSet.getString(DB_SCHEMA_COLUMN_NAME_EXECUTOR_NAME));
            row.put(DB_SCHEMA_COLUMN_NAME_IDP_NAME, resultSet.getString(DB_SCHEMA_COLUMN_NAME_IDP_NAME));
            row.put(DB_SCHEMA_ALIAS_NEXT_NODE_ID, resultSet.getString(DB_SCHEMA_ALIAS_NEXT_NODE_ID));
            row.put(DB_SCHEMA_COLUMN_NAME_TRIGGERING_ELEMENT, resultSet.getString(DB_SCHEMA_COLUMN_NAME_TRIGGERING_ELEMENT));
            return row;
        }, preparedStatement -> {
            preparedStatement.setInt(1, tenantId);
            preparedStatement.setBoolean(2, true);
            preparedStatement.setString(3, REGISTRATION_FLOW);
        });
    }

    private Map<String, StepDTO> getViewPagesForFlow(String flowId, int tenantId, JdbcTemplate jdbcTemplate)
            throws DataAccessException {

        Map<String, StepDTO> nodePageMappings = new HashMap<>();
        jdbcTemplate.executeQuery(GET_VIEW_PAGES_IN_FLOW, (LambdaExceptionUtils.rethrowRowMapper((resultSet, rowNumber) -> {
            StepDTO stepDTO = new StepDTO.Builder()
                    .id(resultSet.getString(DB_SCHEMA_COLUMN_NAME_STEP_ID))
                    .type(VIEW)
                    .build();
            resolvePageContent(stepDTO, resultSet.getBinaryStream(DB_SCHEMA_COLUMN_NAME_PAGE_CONTENT), tenantId);
            nodePageMappings.put(resultSet.getString(DB_SCHEMA_COLUMN_NAME_NODE_ID), stepDTO);
            return null;
        })), preparedStatement -> {
            preparedStatement.setString(1, flowId);
            preparedStatement.setString(2, VIEW);
        });
        return nodePageMappings;
    }

    private RegistrationGraphConfig buildGraph(List<Map<String, Object>> rows) {

        RegistrationGraphConfig registrationGraphConfig = new RegistrationGraphConfig();
        Map<String, NodeConfig> nodeConfigs = new HashMap<>();

        for (Map<String, Object> row : rows) {
            if (registrationGraphConfig.getId() == null) {
                registrationGraphConfig.setId((String) row.get(DB_SCHEMA_ALIAS_FLOW_ID));
            }
            String nodeId = (String) row.get(DB_SCHEMA_COLUMN_NAME_NODE_ID);
            nodeConfigs.putIfAbsent(nodeId, new NodeConfig.Builder()
                    .id(nodeId)
                    .type((String) row.get(DB_SCHEMA_COLUMN_NAME_NODE_TYPE))
                    .isFirstNode((Boolean) row.get(DB_SCHEMA_COLUMN_NAME_IS_FIRST_NODE))
                    .build());

            NodeConfig nodeConfig = nodeConfigs.get(nodeId);

            // Update first node of the graph.
            if (nodeConfig.isFirstNode()) {
                registrationGraphConfig.setFirstNodeId(nodeId);
            }
            // Attach executor details if present.
            if (row.get(DB_SCHEMA_COLUMN_NAME_EXECUTOR_NAME) != null) {
                nodeConfig.setExecutorConfig(
                        new ExecutorDTO((String) row.get(DB_SCHEMA_COLUMN_NAME_EXECUTOR_NAME),
                                        (String) row.get(DB_SCHEMA_COLUMN_NAME_IDP_NAME)));
            }

            // Attach edges (mappings).
            if (row.get(DB_SCHEMA_ALIAS_NEXT_NODE_ID) != null) {
                nodeConfig.addEdge(new NodeEdge(nodeId, (String) row.get(DB_SCHEMA_ALIAS_NEXT_NODE_ID),
                                     (String) row.get(DB_SCHEMA_COLUMN_NAME_TRIGGERING_ELEMENT))
                );
            }
        }
        registrationGraphConfig.setNodeConfigs(nodeConfigs);
        return registrationGraphConfig;
    }

    private void resolvePageContent(StepDTO stepDTO, InputStream pageContent, int tenantId)
            throws RegistrationServerException {

        try {
            if (pageContent == null) {
                // The step does not have any data to be resolved.
                stepDTO.setData(new DataDTO.Builder().build());
                return;
            }
            try (ObjectInputStream ois = new ObjectInputStream(pageContent)) {
                Object obj = ois.readObject();
                if (VIEW.equals(stepDTO.getType()) && obj instanceof List<?>) {
                    List<?> tempList = (List<?>) obj;
                    if (!tempList.isEmpty() && tempList.get(0) instanceof ComponentDTO) {
                        List<ComponentDTO> components = tempList.stream()
                                .map(ComponentDTO.class::cast)
                                .collect(Collectors.toList());
                        stepDTO.setData(new DataDTO.Builder().components(components).build());
                    } else {
                        throw handleServerException(Constants.ErrorMessages.ERROR_CODE_DESERIALIZE_PAGE_CONTENT,
                                                    stepDTO.getId(), tenantId);
                    }
                } else if (REDIRECTION.equals(stepDTO.getType())) {
                    if (obj instanceof ActionDTO) {
                        ActionDTO action = (ActionDTO) obj;
                        stepDTO.setData(new DataDTO.Builder().action(action).build());
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            throw handleServerException(Constants.ErrorMessages.ERROR_CODE_DESERIALIZE_PAGE_CONTENT, e, stepDTO.getId(),
                                        tenantId);
        }
    }

    private static byte[] serializeObject(Object obj) throws IOException {

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
            oos.flush();
            return baos.toByteArray();
        }
    }

    private static Optional<byte[]> serializeStepData(StepDTO stepDTO, int tenantId)
            throws RegistrationFrameworkException {

        try {
            if (VIEW.equals(stepDTO.getType())) {
                List<ComponentDTO> components = stepDTO.getData().getComponents();
                return Optional.of(serializeObject(components));
            } else if (REDIRECTION.equals(stepDTO.getType())) {
                ActionDTO action = stepDTO.getData().getAction();
                return Optional.of(serializeObject(action));
            } else {
                return Optional.empty();
            }
        } catch (IOException e) {
            throw handleServerException(Constants.ErrorMessages.ERROR_CODE_SERIALIZE_PAGE_CONTENT, e,
                                        stepDTO.getId(), tenantId);
        }
    }
}
