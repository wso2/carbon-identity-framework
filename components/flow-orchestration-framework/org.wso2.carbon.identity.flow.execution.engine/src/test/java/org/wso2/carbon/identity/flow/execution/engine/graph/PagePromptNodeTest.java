/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.flow.execution.engine.graph;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.NodeResponse;
import org.wso2.carbon.identity.flow.mgt.model.NodeConfig;
import org.wso2.carbon.identity.flow.mgt.model.NodeEdge;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.STATUS_COMPLETE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.STATUS_INCOMPLETE;
import static org.wso2.carbon.identity.flow.mgt.Constants.NodeTypes.PROMPT_ONLY;
import static org.wso2.carbon.identity.flow.mgt.Constants.StepTypes.VIEW;

/**
 * Unit tests for PagePromptNode.
 */
public class PagePromptNodeTest {

    private static final String ACTION_ID = "action1";
    private static final String TARGET_NODE_ID = "targetNode";
    private static final String SOURCE_NODE_ID = "sourceNode";
    private static final String NODE_ID = "testNode";

    private PagePromptNode pagePromptNode;
    private FlowExecutionContext context;

    @BeforeMethod
    public void setUp() {

        pagePromptNode = new PagePromptNode();
        context = new FlowExecutionContext();
    }

    @Test
    public void testGetName() {

        assertEquals(pagePromptNode.getName(), PROMPT_ONLY);
    }

    @Test
    public void testExecute_withNoTriggeredAction_andNoNextNodeId_returnsIncomplete() throws FlowEngineException {

        NodeConfig nodeConfig = new NodeConfig.Builder()
                .id(NODE_ID)
                .type(PROMPT_ONLY)
                .build();

        NodeResponse response = pagePromptNode.execute(context, nodeConfig);

        assertEquals(response.getStatus(), STATUS_INCOMPLETE);
        assertEquals(response.getType(), VIEW);
    }

    @Test
    public void testExecute_withNoTriggeredAction_andPresetNextNodeId_returnsIncomplete() throws FlowEngineException {

        NodeConfig nodeConfig = new NodeConfig.Builder()
                .id(NODE_ID)
                .type(PROMPT_ONLY)
                .build();
        nodeConfig.setNextNodeId(TARGET_NODE_ID);

        NodeResponse response = pagePromptNode.execute(context, nodeConfig);

        assertEquals(response.getStatus(), STATUS_INCOMPLETE);
        assertEquals(response.getType(), VIEW);
    }

    @Test
    public void testExecute_withMatchingTriggeredAction_setsNextNodeIdAndReturnsComplete()
            throws FlowEngineException {

        context.setCurrentActionId(ACTION_ID);
        List<NodeEdge> edges = new ArrayList<>();
        edges.add(new NodeEdge(SOURCE_NODE_ID, TARGET_NODE_ID, ACTION_ID));

        NodeConfig nodeConfig = new NodeConfig.Builder()
                .id(NODE_ID)
                .type(PROMPT_ONLY)
                .edges(edges)
                .build();

        NodeResponse response = pagePromptNode.execute(context, nodeConfig);

        assertEquals(response.getStatus(), STATUS_COMPLETE);
        assertEquals(nodeConfig.getNextNodeId(), TARGET_NODE_ID);
        assertNull(context.getCurrentActionId());
    }

    @Test
    public void testExecute_withMatchingTriggeredAction_andPresetNextNodeId_preservesNextNodeIdAndReturnsComplete()
            throws FlowEngineException {

        context.setCurrentActionId(ACTION_ID);
        List<NodeEdge> edges = new ArrayList<>();
        edges.add(new NodeEdge(SOURCE_NODE_ID, "edgeTargetNode", ACTION_ID));

        NodeConfig nodeConfig = new NodeConfig.Builder()
                .id(NODE_ID)
                .type(PROMPT_ONLY)
                .edges(edges)
                .build();
        nodeConfig.setNextNodeId(TARGET_NODE_ID);

        NodeResponse response = pagePromptNode.execute(context, nodeConfig);

        assertEquals(response.getStatus(), STATUS_COMPLETE);
        assertEquals(nodeConfig.getNextNodeId(), TARGET_NODE_ID);
        assertNull(context.getCurrentActionId());
    }

    @Test
    public void testExecute_withNonMatchingTriggeredAction_clearsActionAndReturnsIncomplete()
            throws FlowEngineException {

        context.setCurrentActionId(ACTION_ID);
        List<NodeEdge> edges = new ArrayList<>();
        edges.add(new NodeEdge(SOURCE_NODE_ID, TARGET_NODE_ID, "differentAction"));

        NodeConfig nodeConfig = new NodeConfig.Builder()
                .id(NODE_ID)
                .type(PROMPT_ONLY)
                .edges(edges)
                .build();

        NodeResponse response = pagePromptNode.execute(context, nodeConfig);

        assertEquals(response.getStatus(), STATUS_INCOMPLETE);
        assertEquals(response.getType(), VIEW);
        assertNull(context.getCurrentActionId());
    }

    @Test
    public void testExecute_withTriggeredActionAndEmptyEdges_clearsActionAndReturnsIncomplete()
            throws FlowEngineException {

        context.setCurrentActionId(ACTION_ID);
        NodeConfig nodeConfig = new NodeConfig.Builder()
                .id(NODE_ID)
                .type(PROMPT_ONLY)
                .build();

        NodeResponse response = pagePromptNode.execute(context, nodeConfig);

        assertEquals(response.getStatus(), STATUS_INCOMPLETE);
        assertEquals(response.getType(), VIEW);
        assertNull(context.getCurrentActionId());
    }

    @Test
    public void testExecute_withTriggeredActionAndNullEdges_clearsActionAndReturnsIncomplete()
            throws FlowEngineException {

        context.setCurrentActionId(ACTION_ID);
        NodeConfig nodeConfig = mock(NodeConfig.class);
        when(nodeConfig.getEdges()).thenReturn(null);
        when(nodeConfig.getNextNodeId()).thenReturn(null);

        NodeResponse response = pagePromptNode.execute(context, nodeConfig);

        assertEquals(response.getStatus(), STATUS_INCOMPLETE);
        assertEquals(response.getType(), VIEW);
        assertNull(context.getCurrentActionId());
    }

    @Test
    public void testRollback_returnsNull() throws FlowEngineException {

        NodeConfig nodeConfig = new NodeConfig.Builder()
                .id(NODE_ID)
                .type(PROMPT_ONLY)
                .build();

        NodeResponse response = pagePromptNode.rollback(context, nodeConfig);

        assertNull(response);
    }
}
