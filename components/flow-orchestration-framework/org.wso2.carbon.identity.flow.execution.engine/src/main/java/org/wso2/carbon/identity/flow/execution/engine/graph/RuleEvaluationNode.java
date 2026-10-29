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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.flow.execution.engine.Constants;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.execution.engine.internal.FlowExecutionEngineDataHolder;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowRequest;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowUser;
import org.wso2.carbon.identity.flow.execution.engine.model.NodeResponse;
import org.wso2.carbon.identity.flow.execution.engine.rule.AbstractFlowRuleEvaluationDataProvider;
import org.wso2.carbon.identity.flow.execution.engine.util.RuleEvaluationDiagnosticLogger;
import org.wso2.carbon.identity.flow.mgt.model.BranchDTO;
import org.wso2.carbon.identity.flow.mgt.model.GraphConfig;
import org.wso2.carbon.identity.flow.mgt.model.NodeConfig;
import org.wso2.carbon.identity.flow.mgt.model.StepDTO;
import org.wso2.carbon.identity.flow.mgt.utils.FlowMgtUtils;
import org.wso2.carbon.identity.rule.evaluation.api.exception.RuleEvaluationException;
import org.wso2.carbon.identity.rule.evaluation.api.model.FlowContext;
import org.wso2.carbon.identity.rule.evaluation.api.model.FlowType;
import org.wso2.carbon.identity.rule.evaluation.api.service.RuleEvaluationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.flow.execution.engine.util.FlowExecutionEngineUtils.handleServerException;
import static org.wso2.carbon.identity.flow.mgt.Constants.NodeTypes.RULE_EVALUATION;

/**
 * A step that decides where the flow goes by evaluating rules against the context.
 */
public class RuleEvaluationNode implements Node {

    private static final Log LOG = LogFactory.getLog(RuleEvaluationNode.class);
    private static final RuleEvaluationDiagnosticLogger DIAGNOSTIC_LOGGER = new RuleEvaluationDiagnosticLogger();

    @Override
    public String getName() {

        return RULE_EVALUATION;
    }

    @Override
    public NodeResponse execute(FlowExecutionContext context, NodeConfig nodeConfig) throws FlowEngineException {

        if (!FlowMgtUtils.isConditionalBranchingEnabled()) {
            DIAGNOSTIC_LOGGER.logConditionalBranchingDisabled(context.getFlowType(), nodeConfig.getId());
            throw handleServerException(Constants.ErrorMessages.ERROR_CODE_CONDITIONAL_BRANCHING_DISABLED,
                    nodeConfig.getId(), context.getFlowType(), context.getTenantDomain());
        }
        BranchDTO selected = selectBranch(context, nodeConfig, branchesOf(context, nodeConfig));
        if (selected == null) {
            DIAGNOSTIC_LOGGER.logNoBranchSelected(context.getFlowType(), nodeConfig.getId());
            throw handleServerException(Constants.ErrorMessages.ERROR_CODE_UNSUPPORTED_NODE,
                    nodeConfig.getType(), context.getFlowType(),
                    context.getGraphConfig().getId(), context.getTenantDomain());
        }

        nodeConfig.setNextNodeId(selected.getNextId());
        if (LOG.isDebugEnabled()) {
            LOG.debug("Rule evaluation " + nodeConfig.getId() + " selected branch " + selected.getId() + ".");
        }
        return new NodeResponse.Builder().status(Constants.STATUS_COMPLETE).build();
    }

    @Override
    public NodeResponse rollback(FlowExecutionContext context, NodeConfig nodeConfig) throws FlowEngineException {

        return new NodeResponse.Builder().status(Constants.STATUS_COMPLETE).build();
    }

    /**
     * The first branch whose rule holds, or the default.
     *
     * @param context    Flow execution context.
     * @param nodeConfig The decision step.
     * @param branches   Branches of the decision, in evaluation order.
     * @return The winning branch, or null when the decision has no default and nothing matched.
     * @throws FlowEngineException If a branch cannot be evaluated.
     */
    private BranchDTO selectBranch(FlowExecutionContext context, NodeConfig nodeConfig, List<BranchDTO> branches)
            throws FlowEngineException {

        FlowExecutionContext ruleContext = ruleEvaluationContext(context);
        BranchDTO defaultBranch = null;
        for (BranchDTO branch : branches) {
            if (branch.isDefault()) {
                defaultBranch = branch;
                continue;
            }
            if (holds(branch, ruleContext, context, nodeConfig)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Branch '" + branch.getName() + "' holds.");
                }
                DIAGNOSTIC_LOGGER.logBranchSelected(context.getFlowType(), nodeConfig.getId(), branch.getId(),
                        branch.getName(), branch.getNextId());
                return branch;
            }
        }
        if (defaultBranch != null) {
            DIAGNOSTIC_LOGGER.logDefaultBranchSelected(context.getFlowType(), nodeConfig.getId(),
                    defaultBranch.getId(), defaultBranch.getName());
        }
        return defaultBranch;
    }

    /**
     * Whether a branch's rule holds. A branch that cannot be evaluated fails the flow rather than counting as not
     * holding, so a failed lookup never sends the user down another branch.
     *
     * @param branch      The branch.
     * @param ruleContext The copy of the context handed to the rule component.
     * @param context     Flow execution context.
     * @param nodeConfig  The decision step.
     * @return True if the branch's rule is satisfied.
     * @throws FlowEngineException If the branch cannot be evaluated.
     */
    private boolean holds(BranchDTO branch, FlowExecutionContext ruleContext, FlowExecutionContext context,
                          NodeConfig nodeConfig) throws FlowEngineException {

        RuleEvaluationService evaluationService =
                FlowExecutionEngineDataHolder.getInstance().getRuleEvaluationService();
        try {
            if (evaluationService == null) {
                throw new IllegalStateException("Rule evaluation service is not available.");
            }
            Map<String, Object> contextData = new HashMap<>();
            contextData.put(AbstractFlowRuleEvaluationDataProvider.FLOW_EXECUTION_CONTEXT, ruleContext);
            FlowContext ruleFlowContext = new FlowContext(
                    FlowType.valueOf(context.getFlowType()), contextData);

            return evaluationService.evaluate(branch.getRule(), ruleFlowContext, context.getTenantDomain())
                    .isRuleSatisfied();
        } catch (RuleEvaluationException | RuntimeException e) {
            DIAGNOSTIC_LOGGER.logBranchEvaluationFailed(context.getFlowType(), nodeConfig.getId(), branch.getName(),
                    e.getMessage());
            throw handleServerException(Constants.ErrorMessages.ERROR_CODE_BRANCH_EVALUATION_FAILURE, e,
                    branch.getName(), nodeConfig.getId(), context.getFlowType(), context.getTenantDomain());
        }
    }

    /**
     * A copy of the context holding only what the rule fields read, to hand to the rule component. The flow's own
     * context is never passed out, so evaluation cannot change it, and the user's credentials, inputs and the flow
     * graph stay in the engine. Of the graph, only the variables go along, as they type the headers and runtime
     * values.
     *
     * @param context Flow execution context.
     * @return The copy.
     */
    private static FlowExecutionContext ruleEvaluationContext(FlowExecutionContext context) {

        FlowExecutionContext copy = new FlowExecutionContext();
        copy.setContextIdentifier(context.getContextIdentifier());
        copy.setTenantDomain(context.getTenantDomain());
        copy.setApplicationId(context.getApplicationId());
        copy.setFlowType(context.getFlowType());
        copy.setRuntimeValues(new HashMap<>(context.getRuntimeValues()));

        copy.setRequest(new FlowRequest(context.getRequest()));

        if (context.getGraphConfig() != null) {
            GraphConfig variablesOnly = new GraphConfig();
            variablesOnly.setFlowVariables(context.getGraphConfig().getFlowVariables());
            copy.setGraphConfig(variablesOnly);
        }

        FlowUser user = context.getFlowUser();
        if (user == null) {
            copy.setFlowUser(null);
            return copy;
        }
        // Copied field by field: FlowUser#getUsername makes up a username when none is given.
        FlowUser userCopy = new FlowUser();
        userCopy.setUserId(user.getUserId());
        userCopy.setUserStoreDomain(user.getUserStoreDomain());
        if (user.getClaims() != null) {
            userCopy.addClaims(new HashMap<>(user.getClaims()));
            user.getUpdatedClaimUris().forEach(claimUri ->
                    userCopy.addUpdatedClaim(claimUri, user.getClaims().get(claimUri)));
        }
        copy.setFlowUser(userCopy);
        return copy;
    }

    private List<BranchDTO> branchesOf(FlowExecutionContext context, NodeConfig nodeConfig)
            throws FlowEngineException {

        StepDTO step = context.getGraphConfig() == null
                ? null : context.getGraphConfig().getNodePageMappings().get(nodeConfig.getId());
        if (step == null || step.getData() == null || step.getData().getAction() == null
                || step.getData().getAction().getBranches() == null) {
            throw handleServerException(Constants.ErrorMessages.ERROR_CODE_UNSUPPORTED_NODE,
                    nodeConfig.getType(), context.getFlowType(),
                    context.getGraphConfig() == null ? null : context.getGraphConfig().getId(),
                    context.getTenantDomain());
        }
        return step.getData().getAction().getBranches();
    }
}
