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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.flow.execution.engine.Constants;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.execution.engine.internal.FlowExecutionEngineDataHolder;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.NodeResponse;
import org.wso2.carbon.identity.flow.execution.engine.rule.AbstractFlowRuleEvaluationDataProvider;
import org.wso2.carbon.identity.flow.mgt.model.BranchDTO;
import org.wso2.carbon.identity.flow.mgt.model.NodeConfig;
import org.wso2.carbon.identity.flow.mgt.model.StepDTO;
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
 * <p>
 * Distinct from the user-choice decision node, which branches on what the user pressed. This one
 * never pauses: it evaluates, picks a successor and completes within the same request. Evaluating
 * the branches is what the node type itself means, so it runs no executor.
 */
public class RuleEvaluationNode implements Node {

    private static final Log LOG = LogFactory.getLog(RuleEvaluationNode.class);

    @Override
    public String getName() {

        return RULE_EVALUATION;
    }

    @Override
    public NodeResponse execute(FlowExecutionContext context, NodeConfig nodeConfig) throws FlowEngineException {

        BranchDTO selected = selectBranch(context, branchesOf(context, nodeConfig));
        if (selected == null) {
            /*
             * Every decision must be total: the branch without a rule always holds, so reaching here
             * means the flow was stored without one. Validation rejects that on save, which is why
             * this is a server error rather than something to route around.
             */
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

        // A decision changes nothing, so there is nothing to undo.
        return new NodeResponse.Builder().status(Constants.STATUS_COMPLETE).build();
    }

    /**
     * The first branch whose rule holds, or the default.
     * <p>
     * Order is the order the branches were authored in. The default is the branch carrying no rule,
     * which is what makes a decision total -- it is taken whatever the context says.
     *
     * @param context  Flow execution context.
     * @param branches Branches of the decision, in evaluation order.
     * @return The winning branch, or null when the decision has no default and nothing matched.
     */
    private BranchDTO selectBranch(FlowExecutionContext context, List<BranchDTO> branches) {

        BranchDTO defaultBranch = null;
        for (BranchDTO branch : branches) {
            if (branch.isDefault()) {
                // Held back: a default earlier in the list must not pre-empt a guarded branch.
                defaultBranch = branch;
                continue;
            }
            if (holds(branch, context)) {
                LOG.debug("Branch '" + branch.getName() + "' holds.");
                return branch;
            }
        }
        return defaultBranch;
    }

    private boolean holds(BranchDTO branch, FlowExecutionContext context) {

        try {
            RuleEvaluationService evaluationService =
                    FlowExecutionEngineDataHolder.getInstance().getRuleEvaluationService();
            if (evaluationService == null) {
                LOG.warn("Rule evaluation service is unavailable. Branch '" + branch.getName()
                        + "' will not hold.");
                return false;
            }
            Map<String, Object> contextData = new HashMap<>();
            contextData.put(AbstractFlowRuleEvaluationDataProvider.FLOW_EXECUTION_CONTEXT, context);
            FlowContext flowContext = new FlowContext(
                    FlowType.valueOf(context.getFlowType()), contextData);

            return evaluationService.evaluate(branch.getRule(), flowContext, context.getTenantDomain())
                    .isRuleSatisfied();
        } catch (Exception e) {
            /*
             * A branch that cannot be evaluated does not hold, so the decision falls through to the
             * next branch and ultimately to the default. Failing the flow instead would turn a
             * misconfigured condition into an outage.
             */
            LOG.warn("Could not evaluate branch '" + branch.getName() + "'. It will not hold.", e);
            return false;
        }
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
