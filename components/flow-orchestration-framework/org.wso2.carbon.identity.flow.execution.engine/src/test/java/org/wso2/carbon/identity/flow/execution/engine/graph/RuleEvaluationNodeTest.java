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

package org.wso2.carbon.identity.flow.execution.engine.graph;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.flow.execution.engine.Constants;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.execution.engine.internal.FlowExecutionEngineDataHolder;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.NodeResponse;
import org.wso2.carbon.identity.flow.mgt.model.ActionDTO;
import org.wso2.carbon.identity.flow.mgt.model.BranchDTO;
import org.wso2.carbon.identity.flow.mgt.model.DataDTO;
import org.wso2.carbon.identity.flow.mgt.model.GraphConfig;
import org.wso2.carbon.identity.flow.mgt.model.NodeConfig;
import org.wso2.carbon.identity.flow.mgt.model.StepDTO;
import org.wso2.carbon.identity.rule.evaluation.api.model.RuleEvaluationResult;
import org.wso2.carbon.identity.rule.evaluation.api.service.RuleEvaluationService;
import org.wso2.carbon.identity.rule.management.api.model.ANDCombinedRule;
import org.wso2.carbon.identity.rule.management.api.model.Expression;
import org.wso2.carbon.identity.rule.management.api.model.ORCombinedRule;
import org.wso2.carbon.identity.rule.management.api.model.Rule;
import org.wso2.carbon.identity.rule.management.api.model.Value;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;

/**
 * Tests which branch a rule evaluation takes.
 * <p>
 * Selection is the whole feature: the first branch whose rule holds wins, the branch without a rule
 * catches everything else, and neither depends on where a branch happens to sit in the list.
 */
public class RuleEvaluationNodeTest {

    private static final String DECISION_NODE_ID = "step_route";
    private static final String LK_STEP = "step_lk";
    private static final String ADMIN_STEP = "step_admin";
    private static final String GENERIC_STEP = "step_generic";

    private RuleEvaluationNode ruleEvaluationNode;
    private RuleEvaluationService ruleEvaluationService;

    @BeforeMethod
    public void setUpMethod() {

        ruleEvaluationNode = new RuleEvaluationNode();
        ruleEvaluationService = mock(RuleEvaluationService.class);
        FlowExecutionEngineDataHolder.getInstance().setRuleEvaluationService(ruleEvaluationService);
    }

    @Test
    public void testFirstMatchingBranchWins() throws Exception {

        // Both guarded branches hold; the earlier one must win.
        stubEvaluation(true);
        NodeConfig node = node();

        ruleEvaluationNode.execute(contextFor(node, guardedThenDefault()), node);

        assertEquals(node.getNextNodeId(), LK_STEP);
    }

    @Test
    public void testDefaultIsTakenWhenNothingMatches() throws Exception {

        stubEvaluation(false);
        NodeConfig node = node();

        ruleEvaluationNode.execute(contextFor(node, guardedThenDefault()), node);

        assertEquals(node.getNextNodeId(), GENERIC_STEP);
    }

    /**
     * A branch with no rule always holds, so returning the first branch that holds would let a
     * default sitting at the top of the list shadow every guarded branch below it -- silently, for
     * every user. Position must not decide; the absence of a rule is what makes a branch the default.
     */
    @Test
    public void testDefaultFirstInTheListDoesNotShadowGuardedBranches() throws Exception {

        stubEvaluation(true);
        NodeConfig node = node();

        List<BranchDTO> defaultFirst = Arrays.asList(
                branch("br_default", "Everyone else", GENERIC_STEP, null),
                branch("br_lk", "Sri Lanka", LK_STEP, ruleOn("user.claims")));

        ruleEvaluationNode.execute(contextFor(node, defaultFirst), node);

        assertEquals(node.getNextNodeId(), LK_STEP, "A guarded branch must be tried before the default.");
    }

    /**
     * A condition that cannot be evaluated does not hold, so the decision falls through rather than
     * failing. A misconfigured condition should cost one branch, not the whole flow.
     */
    @Test
    public void testBranchThatFailsToEvaluateDoesNotHold() throws Exception {

        when(ruleEvaluationService.evaluate(any(Rule.class), any(), anyString()))
                .thenThrow(new RuntimeException("evaluation blew up"));
        NodeConfig node = node();

        ruleEvaluationNode.execute(contextFor(node, guardedThenDefault()), node);

        assertEquals(node.getNextNodeId(), GENERIC_STEP);
    }

    @Test
    public void testNoEvaluationServiceFallsThroughToTheDefault() throws Exception {

        FlowExecutionEngineDataHolder.getInstance().setRuleEvaluationService(null);
        NodeConfig node = node();

        ruleEvaluationNode.execute(contextFor(node, guardedThenDefault()), node);

        assertEquals(node.getNextNodeId(), GENERIC_STEP);
    }

    /**
     * A decision never pauses: it resolves and completes inside the same request, so the engine's
     * loop moves straight on to the step it chose.
     */
    @Test
    public void testDecisionCompletesWithoutPausing() throws Exception {

        stubEvaluation(true);
        NodeConfig node = node();

        NodeResponse response = ruleEvaluationNode.execute(contextFor(node, guardedThenDefault()), node);

        assertEquals(response.getStatus(), Constants.STATUS_COMPLETE);
    }

    /**
     * Validation rejects a decision without a default when the flow is saved, so reaching runtime
     * without one means the stored flow is broken. Failing loudly beats routing nowhere.
     */
    @Test
    public void testDecisionWithNoDefaultAndNoMatchFails() throws Exception {

        stubEvaluation(false);
        NodeConfig node = node();
        List<BranchDTO> guardedOnly = Arrays.asList(
                branch("br_lk", "Sri Lanka", LK_STEP, ruleOn("user.claims")));
        FlowExecutionContext context = contextFor(node, guardedOnly);

        assertThrows(FlowEngineException.class, () -> ruleEvaluationNode.execute(context, node));
    }

    private void stubEvaluation(boolean satisfied) throws Exception {

        when(ruleEvaluationService.evaluate(any(Rule.class), any(), anyString()))
                .thenReturn(new RuleEvaluationResult("rule-id", satisfied));
    }

    private List<BranchDTO> guardedThenDefault() {

        return Arrays.asList(
                branch("br_lk", "Sri Lanka", LK_STEP, ruleOn("user.claims")),
                branch("br_admin", "Internal staff", ADMIN_STEP, ruleOn("user.claims")),
                branch("br_default", "Everyone else", GENERIC_STEP, null));
    }

    private BranchDTO branch(String id, String name, String nextId, ORCombinedRule rule) {

        return new BranchDTO.Builder().id(id).name(name).nextId(nextId).rule(rule).build();
    }

    private ORCombinedRule ruleOn(String field) {

        return new ORCombinedRule.Builder()
                .addRule(new ANDCombinedRule.Builder()
                        .addExpression(new Expression.Builder()
                                .field(field).fieldQualifier("http://wso2.org/claims/country").operator("equals")
                                .value(new Value(Value.Type.STRING, "LK")).build())
                        .build())
                .build();
    }

    private NodeConfig node() {

        return new NodeConfig.Builder().id(DECISION_NODE_ID).type("RULE_EVALUATION").build();
    }

    private FlowExecutionContext contextFor(NodeConfig node, List<BranchDTO> branches) {

        ActionDTO action = new ActionDTO.Builder().type("RULE_EVALUATOR").branches(branches).build();
        StepDTO step = new StepDTO.Builder()
                .id(DECISION_NODE_ID)
                .type("RULE_EVALUATION")
                .data(new DataDTO.Builder().action(action).build())
                .build();

        Map<String, StepDTO> pageMappings = new HashMap<>();
        pageMappings.put(DECISION_NODE_ID, step);
        GraphConfig graphConfig = new GraphConfig();
        graphConfig.setId("graph-id");
        graphConfig.setNodePageMappings(pageMappings);

        FlowExecutionContext context = new FlowExecutionContext();
        context.setCurrentNode(node);
        context.setGraphConfig(graphConfig);
        context.setFlowType("REGISTRATION");
        context.setTenantDomain("carbon.super");
        return context;
    }
}
