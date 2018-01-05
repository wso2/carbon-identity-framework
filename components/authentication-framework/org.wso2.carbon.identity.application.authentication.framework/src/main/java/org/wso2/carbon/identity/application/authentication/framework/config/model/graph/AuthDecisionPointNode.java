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

import org.wso2.carbon.identity.application.authentication.framework.AuthenticationDecisionEvaluator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Authentication Decision Node.
 * A decision Node contains the decision evaluator runtime class and set of outcomes.
 */
public class AuthDecisionPointNode implements AuthGraphNode {

    private static final long serialVersionUID = 9217119784147648132L;
    private Map<String, DecisionOutcome> outcomes = new HashMap<>();
    private AuthenticationDecisionEvaluator authenticationDecisionEvaluator;

    public AuthDecisionPointNode() {
    }

    public void setDefaultEdge(AuthGraphNode defaultEdge) {
        this.defaultEdge = defaultEdge;
    }

    private AuthGraphNode defaultEdge;

    public AuthGraphNode getDefaultEdge() {
        return defaultEdge;
    }

    public DecisionOutcome getOutcome(String name) {
        return outcomes.get(name);
    }

    public void putOutcome(String name, DecisionOutcome outcome) {
        outcomes.put(name, outcome);
    }

    public AuthenticationDecisionEvaluator getAuthenticationDecisionEvaluator() {
        return authenticationDecisionEvaluator;
    }

    public void setAuthenticationDecisionEvaluator(AuthenticationDecisionEvaluator authenticationDecisionEvaluator) {
        this.authenticationDecisionEvaluator = authenticationDecisionEvaluator;
    }

    public List<DecisionOutcome> getOutcomes() {
        return outcomes.entrySet().stream().map(e ->e.getValue()).collect(Collectors.toList());
    }

    @Override
    public String getName() {
        //TODO: Implement this
        return null;
    }
}
