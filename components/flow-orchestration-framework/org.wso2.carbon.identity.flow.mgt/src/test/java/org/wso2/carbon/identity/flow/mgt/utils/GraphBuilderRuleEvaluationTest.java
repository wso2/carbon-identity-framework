/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.flow.mgt.utils;

import org.testng.annotations.Test;
import org.wso2.carbon.identity.flow.mgt.Constants;
import org.wso2.carbon.identity.flow.mgt.exception.FlowMgtClientException;
import org.wso2.carbon.identity.flow.mgt.exception.FlowMgtFrameworkException;
import org.wso2.carbon.identity.flow.mgt.model.ActionDTO;
import org.wso2.carbon.identity.flow.mgt.model.BranchDTO;
import org.wso2.carbon.identity.flow.mgt.model.DataDTO;
import org.wso2.carbon.identity.flow.mgt.model.ExecutorDTO;
import org.wso2.carbon.identity.flow.mgt.model.GraphConfig;
import org.wso2.carbon.identity.flow.mgt.model.NodeConfig;
import org.wso2.carbon.identity.flow.mgt.model.NodeEdge;
import org.wso2.carbon.identity.flow.mgt.model.StepDTO;
import org.wso2.carbon.identity.rule.management.api.model.ANDCombinedRule;
import org.wso2.carbon.identity.rule.management.api.model.Expression;
import org.wso2.carbon.identity.rule.management.api.model.ORCombinedRule;
import org.wso2.carbon.identity.rule.management.api.model.Value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

/**
 * Tests the graph a rule decision step builds.
 */
public class GraphBuilderRuleEvaluationTest {

    private static final String DECISION_STEP_ID = "step_route";

    @Test
    public void testRuleEvaluationStepBecomesARuleEvaluationNode() throws Exception {

        GraphConfig graph = buildGraph(decisionStep(branchesWithDefaultLast()));

        NodeConfig node = graph.getNodeConfigs().get(DECISION_STEP_ID);
        assertNotNull(node, "A rule evaluation step must produce a node.");
        assertEquals(node.getType(), Constants.NodeTypes.RULE_EVALUATION);
    }

    /**
     * Evaluating the branches is what the node type itself does, so the node carries no executor.
     */
    @Test
    public void testNodeCarriesNoExecutor() throws Exception {

        GraphConfig graph = buildGraph(decisionStep(branchesWithDefaultLast()));

        NodeConfig node = graph.getNodeConfigs().get(DECISION_STEP_ID);
        assertNull(node.getExecutorConfig());
    }

    /**
     * One edge per branch, so the graph stays traversable and a branch pointing nowhere is caught.
     * Each edge is triggered by its branch, which is what ties a stored rule to its target.
     */
    @Test
    public void testEveryBranchBecomesAnEdge() throws Exception {

        GraphConfig graph = buildGraph(decisionStep(branchesWithDefaultLast()));

        List<NodeEdge> edges = graph.getNodeConfigs().get(DECISION_STEP_ID).getEdges();
        assertEquals(edges.size(), 3);
        List<String> triggers = new ArrayList<>();
        for (NodeEdge edge : edges) {
            assertEquals(edge.getSourceNodeId(), DECISION_STEP_ID);
            triggers.add(edge.getTriggeringActionId());
        }
        assertEquals(triggers, Arrays.asList("br_lk", "br_admin", "br_default"));
    }

    /**
     * The rules have to survive into the graph, because that is where evaluation reads them from.
     */
    @Test
    public void testBranchesAndRulesSurviveOnTheStepData() throws Exception {

        GraphConfig graph = buildGraph(decisionStep(branchesWithDefaultLast()));

        StepDTO storedStep = graph.getNodePageMappings().get(DECISION_STEP_ID);
        List<BranchDTO> branches = storedStep.getData().getAction().getBranches();

        assertEquals(branches.size(), 3);
        assertEquals(branches.get(0).getName(), "Sri Lanka");
        assertNotNull(branches.get(0).getRule());
        assertTrue(branches.get(2).isDefault(), "The branch without a rule is the default.");
    }

    /**
     * Without a default the flow has nowhere to go when nothing matches. Caught here rather than
     * when a user walks the flow, which is the only other time it would show up.
     */
    @Test
    public void testDecisionWithoutADefaultBranchIsRejected() {

        List<BranchDTO> guardedOnly = new ArrayList<>();
        guardedOnly.add(new BranchDTO.Builder().id("br_lk").name("Sri Lanka")
                .nextId(Constants.END_NODE_ID).rule(ruleOn("user.claims")).build());

        assertThrows(FlowMgtClientException.class, () -> buildGraph(decisionStep(guardedOnly)));
    }

    @Test
    public void testDecisionWithTwoDefaultBranchesIsRejected() {

        List<BranchDTO> twoDefaults = branchesWithDefaultLast();
        twoDefaults.add(new BranchDTO.Builder().id("br_other").name("Also everyone else")
                .nextId(Constants.END_NODE_ID).build());

        assertThrows(FlowMgtClientException.class, () -> buildGraph(decisionStep(twoDefaults)));
    }

    @Test
    public void testDuplicateBranchIdIsRejected() {

        List<BranchDTO> duplicated = branchesWithDefaultLast();
        duplicated.add(new BranchDTO.Builder().id("br_lk").name("Sri Lanka again")
                .nextId(Constants.END_NODE_ID).rule(ruleOn("user.claims")).build());

        assertThrows(FlowMgtClientException.class, () -> buildGraph(decisionStep(duplicated)));
    }

    /**
     * The name is what an error message cites, so a branch without one cannot be reported on.
     */
    @Test
    public void testBranchWithoutANameIsRejected() {

        List<BranchDTO> unnamed = new ArrayList<>();
        unnamed.add(new BranchDTO.Builder().id("br_lk").nextId(Constants.END_NODE_ID)
                .rule(ruleOn("user.claims")).build());
        unnamed.add(new BranchDTO.Builder().id("br_default").name("Everyone else")
                .nextId(Constants.END_NODE_ID).build());

        assertThrows(FlowMgtClientException.class, () -> buildGraph(decisionStep(unnamed)));
    }

    @Test
    public void testBranchTargetingItsOwnStepIsRejected() {

        List<BranchDTO> selfLooping = new ArrayList<>();
        selfLooping.add(new BranchDTO.Builder().id("br_lk").name("Sri Lanka")
                .nextId(DECISION_STEP_ID).rule(ruleOn("user.claims")).build());
        selfLooping.add(new BranchDTO.Builder().id("br_default").name("Everyone else")
                .nextId(Constants.END_NODE_ID).build());

        assertThrows(FlowMgtClientException.class, () -> buildGraph(decisionStep(selfLooping)));
    }

    @Test
    public void testDecisionWithoutBranchesIsRejected() {

        StepDTO step = decisionStep(Collections.emptyList());

        assertThrows(FlowMgtClientException.class, () -> buildGraph(step));
    }

    @Test
    public void testAnyExecutorIsRejected() {

        StepDTO step = decisionStep(branchesWithDefaultLast(), "PasswordProvisioningExecutor");

        assertThrows(FlowMgtClientException.class, () -> buildGraph(step));
    }

    /**
     * The decision plus a real end step for its branches to point at. Without a node called END in
     * the graph, an edge to it is treated as leaving the flow and is not recorded on the source.
     */
    private GraphConfig buildGraph(StepDTO step) throws FlowMgtFrameworkException {

        StepDTO endStep = new StepDTO.Builder()
                .id(Constants.END_NODE_ID)
                .type(Constants.StepTypes.END)
                .data(new DataDTO.Builder().build())
                .build();

        return new GraphBuilder().withSteps(Arrays.asList(step, endStep)).build();
    }

    private StepDTO decisionStep(List<BranchDTO> branches) {

        return decisionStep(branches, null);
    }

    private StepDTO decisionStep(List<BranchDTO> branches, String executorName) {

        ActionDTO action = new ActionDTO.Builder()
                .type(Constants.ActionTypes.RULE_EVALUATOR)
                .branches(branches)
                .build();

        if (executorName != null) {
            action.setExecutor(new ExecutorDTO(executorName));
        }

        return new StepDTO.Builder()
                .id(DECISION_STEP_ID)
                .type(Constants.StepTypes.RULE_EVALUATION)
                .data(new DataDTO.Builder().action(action).build())
                .build();
    }

    private List<BranchDTO> branchesWithDefaultLast() {

        List<BranchDTO> branches = new ArrayList<>();
        branches.add(new BranchDTO.Builder().id("br_lk").name("Sri Lanka")
                .nextId(Constants.END_NODE_ID).rule(ruleOn("user.claims")).build());
        branches.add(new BranchDTO.Builder().id("br_admin").name("Internal staff")
                .nextId(Constants.END_NODE_ID).rule(ruleOn("user.groups")).build());
        // No rule: the default.
        branches.add(new BranchDTO.Builder().id("br_default").name("Everyone else")
                .nextId(Constants.END_NODE_ID).build());
        return branches;
    }

    private ORCombinedRule ruleOn(String field) {

        return new ORCombinedRule.Builder()
                .setRules(Arrays.asList(new ANDCombinedRule.Builder()
                        .addExpression(new Expression.Builder()
                                .field(field).operator("equals")
                                .value(new Value(Value.Type.STRING, "LK")).build())
                        .build()))
                .build();
    }
}
