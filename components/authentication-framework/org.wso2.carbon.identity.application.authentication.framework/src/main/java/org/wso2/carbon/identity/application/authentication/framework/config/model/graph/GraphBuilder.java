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
 * Translate the authentication graph config to runtime model.
 * This is not thread safe. Should be discarded after build is called.
 */
public class GraphBuilder {

    private static final Log log = LogFactory.getLog(GraphBuilder.class);
    private Map<Node, AuthGraphNode> nodesMap = new HashMap<>();
    private Map<AuthGraphNode, Integer> nodeVsInboundLinks = new HashMap<>();
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
            nodeVsInboundLinks.put(result, 1);
        }

        return result;

    }

    private void visit(AuthenticationGraph result, Node configNode, AuthGraphNode node,
                       AuthenticationGraphConfig graphConfig) {

        if (configNode == null) {
            return;
        }

        if (configNode instanceof DecisionNode) {
            AuthDecisionPointNode decisionPointNode = (AuthDecisionPointNode) node;
            DecisionNode decisionNode = (DecisionNode) configNode;
            Node defaultNode = graphConfig.getNodeByName(decisionNode.getDefaultLinkName());
            decisionPointNode.setDefaultEdge(translate(defaultNode));
            for (Link link : decisionNode.getLinks()) {

                if (link.isEnd()) {
                    decisionPointNode.putOutcome(link.getName(), new DecisionOutcome(endStep, link));
                } else {
                    String nextName = link.getNextLink();
                    if (StringUtils.isNotEmpty(nextName)) {
                        Node nextNode = graphConfig.getNodeByName(nextName);
                        AuthGraphNode graphNode = translate(nextNode);
                        decisionPointNode.putOutcome(link.getName(), new DecisionOutcome(graphNode, link));
                        if (hasAlreadyVisited(graphNode)) {
                            visit(result, nextNode, graphNode, graphConfig);
                        }
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
            if (stepNode.hasNext() && StringUtils.isNotEmpty(nextLink.getNextLink())) {
                String nextName = nextLink.getNextLink();
                Node nextNode = graphConfig.getNodeByName(nextName);
                if (nextNode == null) {
                    log.warn("Could not find the next step : " + nextName + ", on step : " + configNode.getName()
                            + ". Assuming it is the end step.");
                    stepConfigGraphNode.setNext(endStep);
                } else {
                    AuthGraphNode graphNode = translate(nextNode);
                    stepConfigGraphNode.setNext(graphNode);
                    if (!hasAlreadyVisited(graphNode)) {
                        visit(result, nextNode, graphNode, graphConfig);
                    }
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

    private boolean hasAlreadyVisited(AuthGraphNode node) {

        Integer i = nodeVsInboundLinks.get(node);
        return i != null && i > 1;
    }
}
