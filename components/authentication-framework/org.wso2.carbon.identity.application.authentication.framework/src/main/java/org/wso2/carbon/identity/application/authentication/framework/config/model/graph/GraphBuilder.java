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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.graph.AuthenticationGraphConfig;
import org.wso2.carbon.identity.application.common.model.graph.DecisionNode;
import org.wso2.carbon.identity.application.common.model.graph.Link;
import org.wso2.carbon.identity.application.common.model.graph.Node;
import org.wso2.carbon.identity.application.common.model.graph.StepNode;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This is not thread safe. Should be discarded after build is called.
 */
public class GraphBuilder {

    private static final Log log = LogFactory.getLog(GraphBuilder.class);
    private Map<Node, AuthGraphNode> nodesMap = new HashMap<>();
    private EndStep endStep = new EndStep();

    public AuthenticationGraph createWith(AuthenticationGraphConfig graphConfig) {
        AuthenticationGraph result = new AuthenticationGraph();
        result.setName(graphConfig.getName());
        Node startNodeConfig = graphConfig.getStartNode();
        AuthGraphNode startNode = translate(startNodeConfig);
        result.setStartNode(startNode);
        visit(result, startNodeConfig, startNode, graphConfig);
        return result;
    }

    private AuthGraphNode translate(Node configNode) {
        AuthGraphNode result = null;
        if (configNode == null) {
            return result;
        }
        result = nodesMap.get(configNode);
        if (result != null) {
            return result;
        }
        if (configNode instanceof DecisionNode) {
            DecisionNode decisionNode = (DecisionNode) configNode;

            AuthDecisionPointNode decisionPointNode = new AuthDecisionPointNode(decisionNode);
            result = decisionPointNode;

        } else if (configNode instanceof StepNode) {
            StepNode stepNode = (StepNode) configNode;
            result = createStepConfigurationObject(0, stepNode);
        }
        if (result != null) {
            nodesMap.put(configNode, result);
        }

        return result;

    }

    private void visit(AuthenticationGraph result, Node configNode, AuthGraphNode node,
            AuthenticationGraphConfig graphConfig) {
        if (configNode == null) {
            return;
        }

        if (configNode instanceof DecisionNode) {
            DecisionNode decisionNode = (DecisionNode) configNode;
            for (Link link : decisionNode.getLinks()) {
                AuthDecisionPointNode decisionPointNode = (AuthDecisionPointNode) node;
                if (link.isEnd()) {
                    decisionPointNode.putOutcome(link.getName(), new DecisionOutcome(endStep, link));
                } else {
                    String nextName = link.getNextLink();
                    if (StringUtils.isNotEmpty(nextName)) {
                        Node nextNode = graphConfig.getNodeByName(nextName);
                        AuthGraphNode graphNode = translate(nextNode);
                        if (StringUtils.isEmpty(link.getName()) || "DEFAULT".equalsIgnoreCase(link.getName())) {
                            ((AuthDecisionPointNode) node).setDefaultEdge(graphNode);
                        } else {
                            decisionPointNode.putOutcome(link.getName(), new DecisionOutcome(graphNode, link));
                        }
                        visit(result, nextNode, graphNode, graphConfig);
                    } else {
                        log.warn("Next Link for the the link : " + link.getName() + " is empty at the decision : "
                                + configNode.getName() + ". Ignoring it.");
                    }
                }
            }
        } else if (configNode instanceof StepNode) {
            StepNode stepNode = (StepNode) configNode;
            Link nextLink = stepNode.getNext();
            StepConfigGraphNode stepConfigGraphNode = (StepConfigGraphNode) node;
            if (nextLink != null && StringUtils.isNotEmpty(nextLink.getNextLink())) {
                String nextName = nextLink.getNextLink();
                Node nextNode = graphConfig.getNodeByName(nextName);
                if (nextNode == null) {
                    log.warn("Could not find the next step : " + nextName + ", on step : " + configNode.getName()
                            + ". Assuming it is the end step.");
                    stepConfigGraphNode.setNext(endStep);
                } else {
                    AuthGraphNode graphNode = translate(nextNode);
                    stepConfigGraphNode.setNext(graphNode);
                    visit(result, nextNode, graphNode, graphConfig);
                }
            } else {
                stepConfigGraphNode.setNext(endStep);
            }
        }
    }

    protected StepConfigGraphNode createStepConfigurationObject(int stepOrder, StepNode stepNode) {
        AuthenticationStep authenticationStep = stepNode.getAuthenticationStep();
        StepConfigGraphNode stepConfig = new StepConfigGraphNode(stepNode);
        stepConfig.setOrder(stepOrder);
        stepConfig.setSubjectAttributeStep(authenticationStep.isAttributeStep());
        stepConfig.setSubjectIdentifierStep(authenticationStep.isSubjectStep());
        return stepConfig;
    }

    public Collection<AuthGraphNode> getNodes() {
        return Collections.unmodifiableCollection(nodesMap.values());
    }
}
