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

import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.flow.execution.engine.Constants;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.execution.engine.internal.FlowExecutionEngineDataHolder;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowUser;
import org.wso2.carbon.identity.flow.execution.engine.model.NodeResponse;
import org.wso2.carbon.identity.flow.execution.engine.rule.AbstractFlowRuleEvaluationDataProvider;
import org.wso2.carbon.identity.flow.mgt.model.ActionDTO;
import org.wso2.carbon.identity.flow.mgt.model.BranchDTO;
import org.wso2.carbon.identity.flow.mgt.model.DataDTO;
import org.wso2.carbon.identity.flow.mgt.model.GraphConfig;
import org.wso2.carbon.identity.flow.mgt.model.NodeConfig;
import org.wso2.carbon.identity.flow.mgt.model.StepDTO;
import org.wso2.carbon.identity.rule.evaluation.api.exception.RuleEvaluationException;
import org.wso2.carbon.identity.rule.evaluation.api.model.FlowContext;
import org.wso2.carbon.identity.rule.evaluation.api.model.RuleEvaluationResult;
import org.wso2.carbon.identity.rule.evaluation.api.service.RuleEvaluationService;
import org.wso2.carbon.identity.rule.management.api.model.ANDCombinedRule;
import org.wso2.carbon.identity.rule.management.api.model.Expression;
import org.wso2.carbon.identity.rule.management.api.model.ORCombinedRule;
import org.wso2.carbon.identity.rule.management.api.model.Rule;
import org.wso2.carbon.identity.rule.management.api.model.Value;
import org.wso2.carbon.utils.DiagnosticLog;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

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
    private static final String CONDITIONAL_BRANCHING_ENABLED =
            org.wso2.carbon.identity.flow.mgt.Constants.CONDITIONAL_BRANCHING_ENABLED;

    private RuleEvaluationNode ruleEvaluationNode;
    private RuleEvaluationService ruleEvaluationService;
    private MockedStatic<IdentityUtil> identityUtilMock;
    private MockedStatic<LoggerUtils> loggerUtilsMock;

    @BeforeClass
    public void setUpClass() {

        loggerUtilsMock = mockStatic(LoggerUtils.class);
        loggerUtilsMock.when(LoggerUtils::isDiagnosticLogsEnabled).thenReturn(false);
        identityUtilMock = mockStatic(IdentityUtil.class, CALLS_REAL_METHODS);
        identityUtilMock.when(() -> IdentityUtil.getProperty(CONDITIONAL_BRANCHING_ENABLED))
                .thenReturn("true");
    }

    @AfterClass
    public void tearDownClass() {

        identityUtilMock.close();
        loggerUtilsMock.close();
    }

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

    /**
     * The rule component gets a copy holding only what the rule fields read, never the flow's own context, so it
     * cannot change the flow and never sees the user's credentials, inputs or the flow graph.
     */
    @Test
    public void testRuleComponentGetsACopyOfTheContext() throws Exception {

        stubEvaluation(false);
        NodeConfig node = node();
        FlowExecutionContext context = contextFor(node, guardedThenDefault());
        context.setApplicationId("app-1");
        FlowUser user = context.getFlowUser();
        user.setUserId("user-1");
        user.setUserStoreDomain("PRIMARY");
        user.addClaim("http://wso2.org/claims/country", "LK");
        user.addUpdatedClaim("http://wso2.org/claims/givenname", "Jane");
        user.getUserCredentials().put("password", "secret".toCharArray());
        context.getUserInputData().put("http://wso2.org/claims/country", "LK");

        ruleEvaluationNode.execute(context, node);

        ArgumentCaptor<FlowContext> captor = ArgumentCaptor.forClass(FlowContext.class);
        verify(ruleEvaluationService, times(2)).evaluate(any(Rule.class), captor.capture(), anyString());
        FlowExecutionContext first = ruleContextOf(captor.getAllValues().get(0));
        FlowExecutionContext ruleContext = ruleContextOf(captor.getAllValues().get(1));
        assertSame(ruleContext, first, "One copy serves every branch of the decision.");
        assertNotSame(ruleContext, context);
        assertNotSame(ruleContext.getFlowUser(), user);
        assertEquals(ruleContext.getTenantDomain(), "carbon.super");
        assertEquals(ruleContext.getApplicationId(), "app-1");
        assertEquals(ruleContext.getFlowType(), "REGISTRATION");
        assertEquals(ruleContext.getFlowUser().getUserId(), "user-1");
        assertEquals(ruleContext.getFlowUser().getUserStoreDomain(), "PRIMARY");
        assertEquals(ruleContext.getFlowUser().getClaims(), user.getClaims());
        assertNotSame(ruleContext.getFlowUser().getClaims(), user.getClaims());
        assertEquals(ruleContext.getFlowUser().getUpdatedClaimUris(),
                Collections.singleton("http://wso2.org/claims/givenname"));
        assertTrue(ruleContext.getFlowUser().getUserCredentials().isEmpty());
        assertTrue(ruleContext.getUserInputData().isEmpty());
        assertNull(ruleContext.getGraphConfig());
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
                branch("br_lk", "Sri Lanka", LK_STEP, ruleOn("user.attributes")));

        ruleEvaluationNode.execute(contextFor(node, defaultFirst), node);

        assertEquals(node.getNextNodeId(), LK_STEP, "A guarded branch must be tried before the default.");
    }

    /**
     * A condition that cannot be evaluated fails the flow rather than counting as not holding, so a failed lookup
     * never sends the user down another branch.
     */
    @Test
    public void testBranchThatFailsToEvaluateFailsTheFlow() throws Exception {

        when(ruleEvaluationService.evaluate(any(Rule.class), any(), anyString()))
                .thenThrow(new RuleEvaluationException("The groups of the user could not be read."));
        NodeConfig node = node();
        FlowExecutionContext context = contextFor(node, guardedThenDefault());

        assertThrows(FlowEngineException.class, () -> ruleEvaluationNode.execute(context, node));
    }

    @Test
    public void testDecisionFailsWhenConditionalBranchingIsDisabled() {

        setConditionalBranching(false);
        try {
            NodeConfig node = node();
            FlowExecutionContext context = contextFor(node, guardedThenDefault());

            assertThrows(FlowEngineException.class, () -> ruleEvaluationNode.execute(context, node));
        } finally {
            setConditionalBranching(true);
        }
    }

    private void setConditionalBranching(boolean enabled) {

        identityUtilMock.when(() -> IdentityUtil.getProperty(CONDITIONAL_BRANCHING_ENABLED))
                .thenReturn(String.valueOf(enabled));
    }

    @Test
    public void testDiagnosticLogRecordsTheBranchTaken() throws Exception {

        stubEvaluation(true);
        NodeConfig node = node();

        DiagnosticLog log = singleDiagnosticLog(() -> ruleEvaluationNode.execute(
                contextFor(node, guardedThenDefault()), node));

        assertEquals(log.getComponentId(), "flow-execution");
        assertEquals(log.getActionId(), "rule-evaluation");
        assertEquals(log.getResultStatus(), DiagnosticLog.ResultStatus.SUCCESS.name());
        assertEquals(log.getInput().get("branchId"), "br_lk");
        assertEquals(log.getInput().get("nextId"), LK_STEP);
        assertEquals(log.getInput().get("stepId"), DECISION_NODE_ID);
    }

    @Test
    public void testDiagnosticLogRecordsTheDefaultBranch() throws Exception {

        stubEvaluation(false);
        NodeConfig node = node();

        DiagnosticLog log = singleDiagnosticLog(() -> ruleEvaluationNode.execute(
                contextFor(node, guardedThenDefault()), node));

        assertEquals(log.getResultStatus(), DiagnosticLog.ResultStatus.SUCCESS.name());
        assertEquals(log.getInput().get("branchId"), "br_default");
    }

    @Test
    public void testDiagnosticLogRecordsAFailedEvaluation() throws Exception {

        when(ruleEvaluationService.evaluate(any(Rule.class), any(), anyString()))
                .thenThrow(new RuleEvaluationException("The groups of the user could not be read."));
        NodeConfig node = node();

        DiagnosticLog log = singleDiagnosticLog(() -> assertThrows(FlowEngineException.class,
                () -> ruleEvaluationNode.execute(contextFor(node, guardedThenDefault()), node)));

        assertEquals(log.getResultStatus(), DiagnosticLog.ResultStatus.FAILED.name());
        assertEquals(log.getInput().get("branchName"), "Sri Lanka");
    }

    /**
     * Runs the action with diagnostic logs enabled and returns the one log it wrote.
     */
    private DiagnosticLog singleDiagnosticLog(ThrowingRunnable action) throws Exception {

        loggerUtilsMock.clearInvocations();
        loggerUtilsMock.when(LoggerUtils::isDiagnosticLogsEnabled).thenReturn(true);
        try {
            action.run();
            ArgumentCaptor<DiagnosticLog.DiagnosticLogBuilder> captor =
                    ArgumentCaptor.forClass(DiagnosticLog.DiagnosticLogBuilder.class);
            loggerUtilsMock.verify(() -> LoggerUtils.triggerDiagnosticLogEvent(captor.capture()));
            return captor.getValue().build();
        } finally {
            loggerUtilsMock.when(LoggerUtils::isDiagnosticLogsEnabled).thenReturn(false);
        }
    }

    private interface ThrowingRunnable {

        void run() throws Exception;
    }

    @Test
    public void testNoEvaluationServiceFailsTheFlow() {

        FlowExecutionEngineDataHolder.getInstance().setRuleEvaluationService(null);
        NodeConfig node = node();
        FlowExecutionContext context = contextFor(node, guardedThenDefault());

        assertThrows(FlowEngineException.class, () -> ruleEvaluationNode.execute(context, node));
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
                branch("br_lk", "Sri Lanka", LK_STEP, ruleOn("user.attributes")));
        FlowExecutionContext context = contextFor(node, guardedOnly);

        assertThrows(FlowEngineException.class, () -> ruleEvaluationNode.execute(context, node));
    }

    private static FlowExecutionContext ruleContextOf(FlowContext flowContext) {

        return (FlowExecutionContext) flowContext.getContextData()
                .get(AbstractFlowRuleEvaluationDataProvider.FLOW_EXECUTION_CONTEXT);
    }

    private void stubEvaluation(boolean satisfied) throws Exception {

        when(ruleEvaluationService.evaluate(any(Rule.class), any(), anyString()))
                .thenReturn(new RuleEvaluationResult("rule-id", satisfied));
    }

    private List<BranchDTO> guardedThenDefault() {

        return Arrays.asList(
                branch("br_lk", "Sri Lanka", LK_STEP, ruleOn("user.attributes")),
                branch("br_admin", "Internal staff", ADMIN_STEP, ruleOn("user.attributes")),
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

        ActionDTO action = new ActionDTO.Builder().type("ROUTE").branches(branches).build();
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
