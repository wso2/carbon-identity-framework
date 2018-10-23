/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph;

import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;

/**
 * Graph node extending traditional authentication step.
 * Provides the Graph (Node) constructs so that the Step can be executed within a graph.
 */
public class StepConfigGraphNode extends AbstractAuthGraphNode implements AuthGraphNode {

    private static final long serialVersionUID = -5648175409277330725L;
    private AuthGraphNode nextLink;
    private StepConfig stepConfig;

    public StepConfigGraphNode(StepConfig stepConfig) {
        this.stepConfig = stepConfig;
    }

    public StepConfig getStepConfig() {

        return stepConfig;
    }

    public void setNext(AuthGraphNode nextLink) {
        this.nextLink = nextLink;
    }

    public AuthGraphNode getNext() {
        return nextLink;
    }

    @Override
    public String getName() {
        //todo:need to more name generation
        return stepConfig == null ? null : "Step: " + stepConfig.getOrder();
    }
}
