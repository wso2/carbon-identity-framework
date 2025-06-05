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

package org.wso2.carbon.identity.flow.execution.engine.validation;

import org.apache.commons.collections.MapUtils;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.execution.engine.listener.AbstractFlowExecutionListener;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionStep;
import org.wso2.carbon.identity.flow.mgt.Constants;
import org.wso2.carbon.identity.flow.mgt.model.DataDTO;
import org.wso2.carbon.identity.flow.mgt.model.GraphConfig;
import org.wso2.carbon.identity.flow.mgt.model.NodeConfig;
import org.wso2.carbon.identity.flow.mgt.model.StepDTO;

import java.util.Map;
import java.util.Optional;

/**
 * Listener to handle input validation.
 */
public class InputValidationListener extends AbstractFlowExecutionListener {

    @Override
    public int getDefaultOrderId() {

        return 1;
    }

    @Override
    public int getExecutionOrderId() {

        return 2;
    }

    @Override
    public boolean isEnabled() {

        return true;
    }

    @Override
    public boolean doPreExecute(FlowExecutionContext FlowExecutionContext)
            throws FlowEngineException {

        if (MapUtils.isNotEmpty(FlowExecutionContext.getUserInputData()) &&
                MapUtils.isEmpty(FlowExecutionContext.getCurrentStepInputs())) {
            GraphConfig graphConfig = FlowExecutionContext.getGraphConfig();
            NodeConfig currentNode = graphConfig.getNodeConfigs().get(graphConfig.getFirstNodeId());

            Map<String, StepDTO> mappings = Optional.ofNullable(FlowExecutionContext.getGraphConfig())
                    .map(GraphConfig::getNodePageMappings)
                    .orElse(null);
            StepDTO stepDTO = null;
            if (mappings != null && currentNode != null) {
                stepDTO = mappings.get(currentNode.getId());
            }
            DataDTO dataDTO = (stepDTO != null) ? stepDTO.getData() : null;

            // If the current node is Prompt node then there is nothing to execute
            // hence assigning next node as the current node.
            if (currentNode != null && Constants.NodeTypes.PROMPT_ONLY.equalsIgnoreCase(currentNode.getType())) {
                if (currentNode.getEdges() != null && !currentNode.getEdges().isEmpty()) {
                    currentNode.setNextNodeId(currentNode.getEdges().get(0).getTargetNodeId());
                }
                currentNode = moveToNextNode(graphConfig, currentNode);
                FlowExecutionContext.setCurrentNode(currentNode);
            }
            InputValidationService.getInstance().prepareStepInputs(dataDTO, FlowExecutionContext);
        }
        InputValidationService.getInstance().validateInputs(FlowExecutionContext);
        InputValidationService.getInstance().handleUserInputs(FlowExecutionContext);
        return true;
    }

    /**
     * Set the current node as the previous node of the next node and return the next node.
     *
     * @param currentNode Current node.
     * @return Next node.
     */
    private NodeConfig moveToNextNode(GraphConfig graphConfig, NodeConfig currentNode) {

        String nextNodeId = currentNode.getNextNodeId();
        NodeConfig nextNode = graphConfig.getNodeConfigs().get(nextNodeId);
        if (nextNode != null) {
            nextNode.setPreviousNodeId(currentNode.getId());
        }
        return nextNode;
    }

    @Override
    public boolean doPostExecute(FlowExecutionStep step, FlowExecutionContext FlowExecutionContext)
            throws FlowEngineException {

        InputValidationService.getInstance().prepareStepInputs(step.getData(), FlowExecutionContext);
        InputValidationService.getInstance().clearUserInputs(FlowExecutionContext);
        return true;
    }
}
