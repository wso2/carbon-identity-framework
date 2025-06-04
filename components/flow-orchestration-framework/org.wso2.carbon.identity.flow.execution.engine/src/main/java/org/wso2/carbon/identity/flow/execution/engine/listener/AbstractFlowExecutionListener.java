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

package org.wso2.carbon.identity.flow.execution.engine.listener;

import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionStep;

/**
 * Abstract class for flow execution listeners.
 */
public class AbstractFlowExecutionListener implements FlowExecutionListener {

    @Override
    public int getExecutionOrderId() {

        return -1;
    }

    @Override
    public int getDefaultOrderId() {

        return -1;
    }

    @Override
    public boolean isEnabled() {

        return false;
    }

    @Override
    public boolean doPreExecute(FlowExecutionContext FlowExecutionContext) throws FlowEngineException {

        return true;
    }

    @Override
    public boolean doPostExecute(FlowExecutionStep step, FlowExecutionContext FlowExecutionContext) throws FlowEngineException {

        return true;
    }

}
