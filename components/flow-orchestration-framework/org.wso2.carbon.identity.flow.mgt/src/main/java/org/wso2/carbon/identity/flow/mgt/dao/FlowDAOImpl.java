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

package org.wso2.carbon.identity.flow.mgt.dao;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.Template;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.database.utils.jdbc.exceptions.TransactionException;
import org.wso2.carbon.identity.core.util.JdbcUtils;
import org.wso2.carbon.identity.core.util.LambdaExceptionUtils;
import org.wso2.carbon.identity.flow.mgt.Constants;
import org.wso2.carbon.identity.flow.mgt.exception.FlowMgtFrameworkException;
import org.wso2.carbon.identity.flow.mgt.exception.FlowMgtServerException;
import org.wso2.carbon.identity.flow.mgt.model.ActionDTO;
import org.wso2.carbon.identity.flow.mgt.model.BranchDTO;
import org.wso2.carbon.identity.flow.mgt.model.DataDTO;
import org.wso2.carbon.identity.flow.mgt.model.ExecutorDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowDTO;
import org.wso2.carbon.identity.flow.mgt.model.GraphConfig;
import org.wso2.carbon.identity.flow.mgt.model.NodeConfig;
import org.wso2.carbon.identity.flow.mgt.model.NodeEdge;
import org.wso2.carbon.identity.flow.mgt.model.StepDTO;
import org.wso2.carbon.identity.rule.management.api.model.ORCombinedRule;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static org.wso2.carbon.identity.flow.mgt.Constants.END_NODE_ID;
import static org.wso2.carbon.identity.flow.mgt.Constants.StepTypes.END;
import static org.wso2.carbon.identity.flow.mgt.Constants.StepTypes.EXECUTION;
import static org.wso2.carbon.identity.flow.mgt.Constants.StepTypes.RULE_EVALUATION;
import static org.wso2.carbon.identity.flow.mgt.Constants.StepTypes.VIEW;
import static org.wso2.carbon.identity.flow.mgt.dao.SQLConstants.DELETE_FLOW;
import static org.wso2.carbon.identity.flow.mgt.dao.SQLConstants.GET_FIRST_STEP_ID;
import static org.wso2.carbon.identity.flow.mgt.dao.SQLConstants.GET_FLOW;
import static org.wso2.carbon.identity.flow.mgt.dao.SQLConstants.GET_FLOW_NODE_BRANCHES;
import static org.wso2.carbon.identity.flow.mgt.dao.SQLConstants.GET_NODES_WITH_MAPPINGS_QUERY;
import static org.wso2.carbon.identity.flow.mgt.dao.SQLConstants.GET_NODE_EXECUTOR_META;
import static org.wso2.carbon.identity.flow.mgt.dao.SQLConstants.GET_VIEW_PAGES_IN_FLOW;
import static org.wso2.carbon.identity.flow.mgt.dao.SQLConstants.INSERT_FLOW_INTO_IDN_FLOW;
import static org.wso2.carbon.identity.flow.mgt.dao.SQLConstants.INSERT_FLOW_NODE_INFO;
import static org.wso2.carbon.identity.flow.mgt.dao.SQLConstants.INSERT_FLOW_NODE_BRANCH;
import static org.wso2.carbon.identity.flow.mgt.dao.SQLConstants.INSERT_FLOW_PAGE_INFO;
import static org.wso2.carbon.identity.flow.mgt.dao.SQLConstants.INSERT_FLOW_PAGE_META;
import static org.wso2.carbon.identity.flow.mgt.dao.SQLConstants.INSERT_NODE_EDGES;
import static org.wso2.carbon.identity.flow.mgt.dao.SQLConstants.INSERT_NODE_EXECUTOR_INFO;
import static org.wso2.carbon.identity.flow.mgt.dao.SQLConstants.INSERT_NODE_EXECUTOR_META;
import static org.wso2.carbon.identity.flow.mgt.dao.SQLConstants.SQLPlaceholders.DB_SCHEMA_ALIAS_FLOW_ID;
import static org.wso2.carbon.identity.flow.mgt.dao.SQLConstants.SQLPlaceholders.DB_SCHEMA_ALIAS_NEXT_NODE_ID;
import static org.wso2.carbon.identity.flow.mgt.dao.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_COORDINATE_X;
import static org.wso2.carbon.identity.flow.mgt.dao.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_COORDINATE_Y;
import static org.wso2.carbon.identity.flow.mgt.dao.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_EXECUTOR_ID;
import static org.wso2.carbon.identity.flow.mgt.dao.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_EXECUTOR_NAME;
import static org.wso2.carbon.identity.flow.mgt.dao.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_HEIGHT;
import static org.wso2.carbon.identity.flow.mgt.dao.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_IS_FIRST_NODE;
import static org.wso2.carbon.identity.flow.mgt.dao.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_METADATA_NAME;
import static org.wso2.carbon.identity.flow.mgt.dao.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_METADATA_VALUE;
import static org.wso2.carbon.identity.flow.mgt.dao.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_NODE_ID;
import static org.wso2.carbon.identity.flow.mgt.dao.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_NODE_TYPE;
import static org.wso2.carbon.identity.flow.mgt.dao.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_PAGE_CONTENT;
import static org.wso2.carbon.identity.flow.mgt.dao.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_PAGE_TYPE;
import static org.wso2.carbon.identity.flow.mgt.dao.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_RULE_CONTENT;
import static org.wso2.carbon.identity.flow.mgt.dao.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_BRANCH_KEY;
import static org.wso2.carbon.identity.flow.mgt.dao.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_BRANCH_NAME;
import static org.wso2.carbon.identity.flow.mgt.dao.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_STEP_ID;
import static org.wso2.carbon.identity.flow.mgt.dao.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_TRIGGERING_ELEMENT;
import static org.wso2.carbon.identity.flow.mgt.dao.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_WIDTH;
import static org.wso2.carbon.identity.flow.mgt.utils.FlowMgtUtils.handleServerException;

/**
 * The DAO class for the flow.
 */
public class FlowDAOImpl implements FlowDAO {

    private static final Log LOG = LogFactory.getLog(FlowDAOImpl.class);
    // Configured once here: a shared mapper is only safe to use concurrently once it stops changing.
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    public void updateFlow(String flowType, GraphConfig graphConfig, int tenantId, String flowName)
            throws FlowMgtFrameworkException {

        String flowId = graphConfig.getId();
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.withTransaction(template -> {
                // Delete the existing flow for the tenant.
                template.executeUpdate(DELETE_FLOW,
                        preparedStatement -> {
                            preparedStatement.setInt(1, tenantId);
                            preparedStatement.setBoolean(2, true);
                            preparedStatement.setString(3, flowType);
                        });

                // Insert into IDN_FLOW.
                template.executeInsert(INSERT_FLOW_INTO_IDN_FLOW,
                        preparedStatement -> {
                            preparedStatement.setString(1, flowId);
                            preparedStatement.setInt(2, tenantId);
                            preparedStatement.setString(3, flowName);
                            preparedStatement.setString(4, flowType);
                            preparedStatement.setBoolean(5, true);
                            preparedStatement.setTimestamp(6, new Timestamp(new Date().getTime()), null);
                        }, graphConfig, false);

                // Insert into IDN_FLOW_NODE.
                Map<String, Integer> nodeIdToNodeIdMap = new HashMap<>();
                for (Map.Entry<String, NodeConfig> entry : graphConfig.getNodeConfigs().entrySet()) {
                    NodeConfig node = entry.getValue();
                    int regNodeId = template.executeInsert(INSERT_FLOW_NODE_INFO,
                            preparedStatement -> {
                                preparedStatement.setString(1, node.getId());
                                preparedStatement.setString(2, flowId);
                                preparedStatement.setString(3, node.getType());
                                preparedStatement.setBoolean(4, node.isFirstNode());
                            }, entry, true);

                    nodeIdToNodeIdMap.put(node.getId(), regNodeId);

                    // Insert into IDN_FLOW_NODE_EXECUTOR.
                    ExecutorDTO executorConfig = node.getExecutorConfig();
                    if (executorConfig != null) {
                        int executorId = template.executeInsert(INSERT_NODE_EXECUTOR_INFO,
                                preparedStatement -> {
                                    preparedStatement.setInt(1,
                                            nodeIdToNodeIdMap.get(node.getId()));
                                    preparedStatement.setString(2, executorConfig.getName());
                                }, null, true);

                        // Insert into IDN_FLOW_NODE_EXECUTOR_META.
                        if (executorConfig.getMetadata() != null) {
                            for (Map.Entry<String, String> metaEntry : executorConfig.getMetadata().entrySet()) {
                                template.executeInsert(INSERT_NODE_EXECUTOR_META,
                                        preparedStatement -> {
                                            preparedStatement.setInt(1, executorId);
                                            preparedStatement.setString(2, metaEntry.getKey());
                                            preparedStatement.setString(3, metaEntry.getValue());
                                        }, null, false);
                            }
                        }
                    }
                }
                // Insert graph edges into IDN_FLOW_NODE_MAPPING.
                for (Map.Entry<String, NodeConfig> entry : graphConfig.getNodeConfigs().entrySet()) {
                    NodeConfig node = entry.getValue();
                    if (node.getEdges() != null) {
                        for (NodeEdge edge : node.getEdges()) {
                            template.executeInsert(INSERT_NODE_EDGES,
                                    preparedStatement -> {
                                        preparedStatement.setInt(1, nodeIdToNodeIdMap.get(
                                                edge.getSourceNodeId()));
                                        preparedStatement.setInt(2, nodeIdToNodeIdMap.get(
                                                edge.getTargetNodeId()));
                                        preparedStatement.setString(3, edge.getTriggeringActionId());
                                    }, null, false);
                        }
                    }
                }

                // Insert into IDN_FLOW_PAGE.
                for (Map.Entry<String, StepDTO> entry : graphConfig.getNodePageMappings().entrySet()) {

                    StepDTO stepDTO = entry.getValue();
                    Optional<byte[]> pageContent = serializeStepData(stepDTO, tenantId);
                    int regNodeId = nodeIdToNodeIdMap.get(entry.getKey());
                    if (RULE_EVALUATION.equals(stepDTO.getType())) {
                        insertBranches(template, regNodeId, stepDTO, tenantId);
                    }

                    int pageAutoIncId = template.executeInsert(INSERT_FLOW_PAGE_INFO,
                            preparedStatement -> {
                                preparedStatement.setString(1, flowId);
                                preparedStatement.setInt(2, regNodeId);
                                preparedStatement.setString(3, stepDTO.getId());
                                if (pageContent.isPresent()) {
                                    preparedStatement.setBytes(4, pageContent.get());
                                } else {
                                    preparedStatement.setBytes(4, null);
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
    public FlowDTO getFlow(String flowType, int tenantId) throws FlowMgtServerException {

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

                        resolvePageContent(stepDTO, resultSet.getBytes(DB_SCHEMA_COLUMN_NAME_PAGE_CONTENT), tenantId);
                        return stepDTO;
                    })), preparedStatement -> {
                        preparedStatement.setInt(1, tenantId);
                        preparedStatement.setBoolean(2, true);
                        preparedStatement.setString(3, flowType);
                    });

            if (steps.isEmpty()) {
                LOG.debug("No steps are found in the " + flowType + " flow ");
                return null;
            }
            FlowDTO flowDTO = new FlowDTO();
            String firstStepId = getFirstStepId(tenantId, flowType);
            StepDTO firstStep = steps.stream()
                    .filter(step -> step.getId().equals(firstStepId))
                    .findFirst()
                    .orElseThrow(() -> handleServerException(Constants.ErrorMessages.ERROR_CODE_INVALID_NODE, firstStepId,
                                                             tenantId));
            if (StringUtils.isNotBlank(firstStepId)) {
                flowDTO.getSteps().add(firstStep);
                steps.remove(firstStep);
            }
            flowDTO.getSteps().addAll(steps);
            attachBranches(jdbcTemplate, flowType, tenantId, flowDTO.getSteps());
            return flowDTO;
        } catch (DataAccessException e) {
            throw handleServerException(Constants.ErrorMessages.ERROR_CODE_GET_DEFAULT_FLOW, e, tenantId);
        }
    }

    @Override
    public void deleteFlow(String flowType, int tenantId) throws FlowMgtFrameworkException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.executeUpdate(DELETE_FLOW,
                    preparedStatement -> {
                        preparedStatement.setInt(1, tenantId);
                        preparedStatement.setBoolean(2, true);
                        preparedStatement.setString(3, flowType);
                    });
        } catch (DataAccessException e) {
            throw handleServerException(Constants.ErrorMessages.ERROR_CODE_DELETE_FLOW, e, tenantId);
        }
    }

    @Override
    public GraphConfig getGraphConfig(String flowType, int tenantId) throws FlowMgtFrameworkException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            // Step 1: Fetch Nodes with Executors and Mappings.
            List<Map<String, Object>> rows = getGraphNodes(flowType, tenantId, jdbcTemplate);

            // Step 2: Process Data to Avoid Duplication.
            GraphConfig graphConfig = buildGraph(jdbcTemplate, rows);

            // Step 3: Fetch Page Content with JOIN (Updated Query).
            Map<String, StepDTO> nodePageMappings = getViewPagesForFlow(graphConfig.getId(),tenantId, jdbcTemplate);

            // Step 4: Put the branches of each rule evaluation back on its step.
            attachBranches(jdbcTemplate, flowType, tenantId, nodePageMappings.values());

            // Step 5: Set the page mappings.
            graphConfig.setNodePageMappings(nodePageMappings);
            return graphConfig;
        } catch (DataAccessException e) {
            LOG.error("Failed to retrieve graph for tenant: " + tenantId, e);
            throw handleServerException(Constants.ErrorMessages.ERROR_CODE_GET_GRAPH_FAILED, e, tenantId);
        }
    }

    private String getFirstStepId(int tenantId, String flowType) throws FlowMgtServerException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            List<String> stepIds = jdbcTemplate.executeQuery(GET_FIRST_STEP_ID, (resultSet, rowNumber) -> {
                return resultSet.getString(DB_SCHEMA_COLUMN_NAME_STEP_ID);
            }, preparedStatement -> {
                preparedStatement.setBoolean(1, true);
                preparedStatement.setInt(2, tenantId);
                preparedStatement.setString(3, flowType);
            });
            return stepIds.isEmpty() ? null : stepIds.get(0);
        } catch (DataAccessException e) {
            throw handleServerException(Constants.ErrorMessages.ERROR_CODE_GET_FIRST_STEP_ID, e, tenantId);
        }
    }

    private List<Map<String, Object>> getGraphNodes(String flowType, int tenantId, JdbcTemplate jdbcTemplate)
            throws DataAccessException {

        return jdbcTemplate.executeQuery(GET_NODES_WITH_MAPPINGS_QUERY, (resultSet, rowNumber) -> {
            Map<String, Object> row = new HashMap<>();
            row.put(DB_SCHEMA_ALIAS_FLOW_ID, resultSet.getString(DB_SCHEMA_ALIAS_FLOW_ID));
            row.put(DB_SCHEMA_COLUMN_NAME_NODE_ID, resultSet.getString(DB_SCHEMA_COLUMN_NAME_NODE_ID));
            row.put(DB_SCHEMA_COLUMN_NAME_NODE_TYPE, resultSet.getString(DB_SCHEMA_COLUMN_NAME_NODE_TYPE));
            row.put(DB_SCHEMA_COLUMN_NAME_IS_FIRST_NODE, resultSet.getBoolean(DB_SCHEMA_COLUMN_NAME_IS_FIRST_NODE));
            row.put(DB_SCHEMA_COLUMN_NAME_EXECUTOR_NAME, resultSet.getString(DB_SCHEMA_COLUMN_NAME_EXECUTOR_NAME));
            row.put(DB_SCHEMA_COLUMN_NAME_EXECUTOR_ID, resultSet.getInt(DB_SCHEMA_COLUMN_NAME_EXECUTOR_ID));
            row.put(DB_SCHEMA_ALIAS_NEXT_NODE_ID, resultSet.getString(DB_SCHEMA_ALIAS_NEXT_NODE_ID));
            row.put(DB_SCHEMA_COLUMN_NAME_TRIGGERING_ELEMENT, resultSet.getString(DB_SCHEMA_COLUMN_NAME_TRIGGERING_ELEMENT));
            return row;
        }, preparedStatement -> {
            preparedStatement.setInt(1, tenantId);
            preparedStatement.setBoolean(2, true);
            preparedStatement.setString(3, flowType);
        });
    }

    private Map<String, StepDTO> getViewPagesForFlow(String flowId, int tenantId, JdbcTemplate jdbcTemplate)
            throws DataAccessException {

        Map<String, StepDTO> nodePageMappings = new HashMap<>();

        addStepsOfType(jdbcTemplate, flowId, tenantId, VIEW, stepDTO -> true, nodePageMappings);
        // An execution only counts as a page when it carries components; the rest run and move on.
        addStepsOfType(jdbcTemplate, flowId, tenantId, EXECUTION,
                stepDTO -> stepDTO.getData() != null && stepDTO.getData().getComponents() != null,
                nodePageMappings);
        addStepsOfType(jdbcTemplate, flowId, tenantId, END, stepDTO -> true, nodePageMappings);
        // A rule evaluation renders nothing, but the node reads its branches from its step, which
        // attachBranches fills in from IDN_FLOW_NODE_BRANCH.
        addStepsOfType(jdbcTemplate, flowId, tenantId, RULE_EVALUATION, stepDTO -> true, nodePageMappings);

        return nodePageMappings;
    }

    /**
     * Reads the stored steps of one type into the node to step mapping.
     *
     * @param jdbcTemplate     Template to query with.
     * @param flowId           Flow being loaded.
     * @param tenantId         Tenant of the flow.
     * @param stepType         Type of step to read.
     * @param include          Decides whether a step that was read belongs in the mapping.
     * @param nodePageMappings Mapping to add to.
     * @throws DataAccessException If the query fails.
     */
    private void addStepsOfType(JdbcTemplate jdbcTemplate, String flowId, int tenantId, String stepType,
                                Predicate<StepDTO> include, Map<String, StepDTO> nodePageMappings)
            throws DataAccessException {

        jdbcTemplate.executeQuery(GET_VIEW_PAGES_IN_FLOW, LambdaExceptionUtils.rethrowRowMapper(
                (resultSet, rowNumber) -> {
                    StepDTO stepDTO = new StepDTO.Builder()
                            .id(resultSet.getString(DB_SCHEMA_COLUMN_NAME_STEP_ID))
                            .type(stepType)
                            .build();

                    resolvePageContent(stepDTO, resultSet.getBytes(DB_SCHEMA_COLUMN_NAME_PAGE_CONTENT), tenantId);
                    if (include.test(stepDTO)) {
                        nodePageMappings.put(resultSet.getString(DB_SCHEMA_COLUMN_NAME_NODE_ID), stepDTO);
                    }

                    return null;
                }), preparedStatement -> {
                    preparedStatement.setString(1, flowId);
                    preparedStatement.setString(2, stepType);
                });
    }

    private GraphConfig buildGraph(JdbcTemplate jdbcTemplate, List<Map<String, Object>> rows) throws DataAccessException {

        GraphConfig graphConfig = new GraphConfig();
        Map<String, NodeConfig> nodeConfigs = new HashMap<>();

        for (Map<String, Object> row : rows) {
            if (graphConfig.getId() == null) {
                graphConfig.setId((String) row.get(DB_SCHEMA_ALIAS_FLOW_ID));
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
                graphConfig.setFirstNodeId(nodeId);
            }
            // Attach executor details if present.
            if (row.get(DB_SCHEMA_COLUMN_NAME_EXECUTOR_NAME) != null) {
                ExecutorDTO executorDTO = new ExecutorDTO((String) row.get(DB_SCHEMA_COLUMN_NAME_EXECUTOR_NAME));
                // Fetch executor metadata.
                Map<String, String> metadata = getExecutorMetadata(jdbcTemplate,
                        (Integer) row.get(DB_SCHEMA_COLUMN_NAME_EXECUTOR_ID));
                executorDTO.setMetadata(metadata);
                nodeConfig.setExecutorConfig(executorDTO);
            }

            // Attach edges (mappings).
            if (row.get(DB_SCHEMA_ALIAS_NEXT_NODE_ID) != null) {
                nodeConfig.addEdge(new NodeEdge(nodeId, (String) row.get(DB_SCHEMA_ALIAS_NEXT_NODE_ID),
                                     (String) row.get(DB_SCHEMA_COLUMN_NAME_TRIGGERING_ELEMENT))
                );
            }
        }
        graphConfig.setNodeConfigs(nodeConfigs);
        return graphConfig;
    }

    private Map<String, String> getExecutorMetadata(JdbcTemplate jdbcTemplate, int executorId)
            throws DataAccessException {

        Map<String, String> metadata = new HashMap<>();
        jdbcTemplate.executeQuery(GET_NODE_EXECUTOR_META, (resultSet, rowNumber) -> {
            metadata.put(resultSet.getString(DB_SCHEMA_COLUMN_NAME_METADATA_NAME),
                    resultSet.getString(DB_SCHEMA_COLUMN_NAME_METADATA_VALUE));
            return null;
        }, preparedStatement -> {
            preparedStatement.setInt(1, executorId);
        });
        return metadata;
    }

    private void resolvePageContent(StepDTO stepDTO, byte[] pageContent, int tenantId)
            throws FlowMgtServerException {

        try {
            if (pageContent == null) {
                // The step does not have any data to be resolved.
                stepDTO.setData(new DataDTO.Builder().build());
                return;
            }
            DataDTO dataDTO = OBJECT_MAPPER.readValue(pageContent, DataDTO.class);
            if (dataDTO != null) {
                stepDTO.setData(dataDTO);
            } else {
                throw handleServerException(Constants.ErrorMessages.ERROR_CODE_DESERIALIZE_PAGE_CONTENT,
                        stepDTO.getId(), tenantId);
            }
        } catch (IOException e) {
            throw handleServerException(Constants.ErrorMessages.ERROR_CODE_DESERIALIZE_PAGE_CONTENT, e, stepDTO.getId(),
                    tenantId);
        }
    }

    /**
     * Stores the branches of a rule evaluation step, one row each, in evaluation order, each with its rule.
     * <p>
     * Where a branch leads is not stored here: that is the target of the edge it triggers in
     * IDN_FLOW_NODE_MAPPING, whose triggering element is the branch key.
     */
    private void insertBranches(Template<?> template, int flowNodeId,
                                StepDTO stepDTO, int tenantId) throws FlowMgtFrameworkException, DataAccessException {

        ActionDTO action = stepDTO.getData() == null ? null : stepDTO.getData().getAction();
        if (action == null || action.getBranches() == null) {
            return;
        }
        List<BranchDTO> branches = action.getBranches();
        for (int order = 0; order < branches.size(); order++) {
            BranchDTO branch = branches.get(order);
            byte[] content = serializeRule(branch.getRule(), stepDTO.getId(), tenantId);
            int ruleOrder = order;
            template.executeInsert(INSERT_FLOW_NODE_BRANCH,
                    preparedStatement -> {
                        preparedStatement.setInt(1, flowNodeId);
                        preparedStatement.setString(2, branch.getId());
                        preparedStatement.setString(3, branch.getName());
                        preparedStatement.setInt(4, ruleOrder);
                        preparedStatement.setBytes(5, content);
                    }, null, false);
        }
    }

    /**
     * Puts the stored branches of each rule evaluation step back on the step, as the branches of its action.
     * <p>
     * A branch with no edge leads to the end of the flow: an edge to END is not recorded when the flow has no
     * explicit end step.
     */
    private void attachBranches(JdbcTemplate jdbcTemplate, String flowType, int tenantId, Collection<StepDTO> steps)
            throws DataAccessException {

        Map<String, List<BranchDTO>> branchesByNode = new HashMap<>();
        jdbcTemplate.executeQuery(GET_FLOW_NODE_BRANCHES, LambdaExceptionUtils.rethrowRowMapper(
                (resultSet, rowNumber) -> {
                    String nodeId = resultSet.getString(DB_SCHEMA_COLUMN_NAME_NODE_ID);
                    String nextNodeId = resultSet.getString(DB_SCHEMA_ALIAS_NEXT_NODE_ID);
                    BranchDTO branch = new BranchDTO.Builder()
                            .id(resultSet.getString(DB_SCHEMA_COLUMN_NAME_BRANCH_KEY))
                            .name(resultSet.getString(DB_SCHEMA_COLUMN_NAME_BRANCH_NAME))
                            .nextId(nextNodeId == null ? END_NODE_ID : nextNodeId)
                            .rule(deserializeRule(resultSet.getBytes(DB_SCHEMA_COLUMN_NAME_RULE_CONTENT), nodeId,
                                    tenantId))
                            .build();
                    branchesByNode.computeIfAbsent(nodeId, key -> new ArrayList<>()).add(branch);
                    return null;
                }), preparedStatement -> {
                    preparedStatement.setInt(1, tenantId);
                    preparedStatement.setBoolean(2, true);
                    preparedStatement.setString(3, flowType);
                });

        for (StepDTO step : steps) {
            List<BranchDTO> branches = branchesByNode.get(step.getId());
            if (branches == null || !RULE_EVALUATION.equals(step.getType())) {
                continue;
            }
            if (step.getData() == null) {
                step.setData(new DataDTO.Builder().build());
            }
            if (step.getData().getAction() == null) {
                step.getData().setAction(new ActionDTO.Builder().type(Constants.ActionTypes.RULE_EVALUATOR).build());
            }
            step.getData().getAction().setBranches(branches);
        }
    }

    private static byte[] serializeRule(ORCombinedRule rule, String stepId, int tenantId)
            throws FlowMgtFrameworkException {

        if (rule == null) {
            // No rule: the default.
            return null;
        }
        try {
            return serializeObject(rule);
        } catch (IOException e) {
            throw handleServerException(Constants.ErrorMessages.ERROR_CODE_SERIALIZE_PAGE_CONTENT, e, stepId, tenantId);
        }
    }

    private static ORCombinedRule deserializeRule(byte[] content, String stepId, int tenantId)
            throws FlowMgtServerException {

        if (content == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(content, ORCombinedRule.class);
        } catch (IOException e) {
            throw handleServerException(Constants.ErrorMessages.ERROR_CODE_DESERIALIZE_PAGE_CONTENT, e, stepId,
                    tenantId);
        }
    }

    private static byte[] serializeObject(Object obj) throws IOException {

        return OBJECT_MAPPER.writeValueAsBytes(obj);
    }

    private static Optional<byte[]> serializeStepData(StepDTO stepDTO, int tenantId)
            throws FlowMgtFrameworkException {

        try {
            if (stepDTO.getData() == null) {
                return Optional.empty();
            }
            if (!RULE_EVALUATION.equals(stepDTO.getType())) {
                return Optional.of(serializeObject(stepDTO.getData()));
            }
            // The page content is what the builder renders. The branches are stored in their own table, so they
            // are left out here rather than kept in two places.
            JsonNode data = OBJECT_MAPPER.valueToTree(stepDTO.getData());
            JsonNode action = data.get("action");
            if (action instanceof ObjectNode) {
                ((ObjectNode) action).remove("branches");
            }
            return Optional.of(OBJECT_MAPPER.writeValueAsBytes(data));
        } catch (IOException e) {
            throw handleServerException(Constants.ErrorMessages.ERROR_CODE_SERIALIZE_PAGE_CONTENT, e,
                                        stepDTO.getId(), tenantId);
        }
    }
}
