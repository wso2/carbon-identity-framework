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

package org.wso2.carbon.identity.flow.execution.engine.validation;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections.MapUtils;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.execution.engine.listener.AbstractFlowExecutionListener;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionStep;
import org.wso2.carbon.identity.flow.mgt.model.DataDTO;
import org.wso2.carbon.identity.flow.mgt.model.GraphConfig;
import org.wso2.carbon.identity.flow.mgt.model.NodeConfig;
import org.wso2.carbon.identity.flow.mgt.model.StepDTO;

/**
 * Listener to handle input processing.
 */
public class InputProcessingListener extends AbstractFlowExecutionListener {

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
    public boolean doPreExecute(FlowExecutionContext context)
            throws FlowEngineException {

        if (MapUtils.isNotEmpty(context.getUserInputData()) && MapUtils.isEmpty(context.getCurrentStepInputs())) {
            GraphConfig graphConfig = context.getGraphConfig();
            NodeConfig currentNode = graphConfig.getNodeConfigs().get(graphConfig.getFirstNodeId());

            Map<String, StepDTO> mappings = Optional.ofNullable(context.getGraphConfig())
                    .map(GraphConfig::getNodePageMappings)
                    .orElse(null);
            StepDTO stepDTO = null;
            if (mappings != null && currentNode != null) {
                stepDTO = mappings.get(currentNode.getId());
            }
            DataDTO dataDTO = (stepDTO != null) ? stepDTO.getData() : null;
            InputValidationService.getInstance().prepareStepInputs(dataDTO, context);
        }
        return true;
    }

    @Override
    public boolean doPostExecute(FlowExecutionStep step, FlowExecutionContext context) throws FlowEngineException {

        InputValidationService.getInstance().prepareStepInputs(step.getData(), context);
        InputValidationService.getInstance().clearUserInputs(context);
        return true;
    }
}
