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

package org.wso2.carbon.identity.user.registration.engine.validation;

import org.apache.commons.collections.MapUtils;
import org.wso2.carbon.identity.user.registration.engine.exception.RegistrationEngineException;
import org.wso2.carbon.identity.user.registration.engine.listener.AbstractFlowExecutionListener;
import org.wso2.carbon.identity.user.registration.engine.model.RegistrationContext;
import org.wso2.carbon.identity.user.registration.engine.model.RegistrationStep;
import org.wso2.carbon.identity.user.registration.mgt.Constants;
import org.wso2.carbon.identity.user.registration.mgt.model.DataDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.NodeConfig;
import org.wso2.carbon.identity.user.registration.mgt.model.RegistrationGraphConfig;

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
    public boolean doPreExecute(RegistrationContext registrationContext)
            throws RegistrationEngineException {

        if (MapUtils.isNotEmpty(registrationContext.getUserInputData()) &&
                MapUtils.isEmpty(registrationContext.getCurrentStepInputs())) {
            RegistrationGraphConfig registrationGraphConfig = registrationContext.getRegGraph();
            NodeConfig currentNode = registrationGraphConfig.getNodeConfigs().get(registrationGraphConfig.getFirstNodeId());
            DataDTO dataDTO = registrationContext.getRegGraph().getNodePageMappings().get(currentNode.getId()).getData();
            // If the current node is Prompt node then there is nothing to execute
            // hence assigning next node as the current node.
            if (Constants.NodeTypes.PROMPT_ONLY.equalsIgnoreCase(currentNode.getType())) {
                if (currentNode.getEdges() != null && !currentNode.getEdges().isEmpty()) {
                    currentNode.setNextNodeId(currentNode.getEdges().get(0).getTargetNodeId());
                }
                currentNode = moveToNextNode(registrationGraphConfig, currentNode);
                registrationContext.setCurrentNode(currentNode);
            }
            InputValidationService.getInstance().prepareStepInputs(dataDTO, registrationContext);
        }
        InputValidationService.getInstance().validateInputs(registrationContext);
        InputValidationService.getInstance().handleUserInputs(registrationContext);
        return true;
    }

    /**
     * Set the current node as the previous node of the next node and return the next node.
     *
     * @param currentNode Current node.
     * @return Next node.
     */
    private NodeConfig moveToNextNode(RegistrationGraphConfig regConfig, NodeConfig currentNode) {

        String nextNodeId = currentNode.getNextNodeId();
        NodeConfig nextNode = regConfig.getNodeConfigs().get(nextNodeId);
        if (nextNode != null) {
            nextNode.setPreviousNodeId(currentNode.getId());
        }
        return nextNode;
    }

    @Override
    public boolean doPostExecute(RegistrationStep step, RegistrationContext registrationContext)
            throws RegistrationEngineException {

        InputValidationService.getInstance().prepareStepInputs(step.getData(), registrationContext);
        InputValidationService.getInstance().clearUserInputs(registrationContext);
        return true;
    }
}
