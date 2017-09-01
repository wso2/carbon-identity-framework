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

package org.wso2.carbon.identity.application.common.model.graph;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

/**
 * Model for Authentication Steps graph.
 * Graph has a start node, set of other connected nodes.
 * @Deprecated We will remove this in favor of Script based dynamic model.
 */
@Deprecated
public class AuthenticationGraphConfig implements Serializable {

    private static final long serialVersionUID = 1610571097373920966L;
    private static final Log log = LogFactory.getLog(AuthenticationGraphConfig.class);

    /* User can incorrectly configure cyclic graph. This will eliminate the cyclic graph evaluation. */
    private static final int MAX_RECURSION_COUNT = 32;

    private Node startNode;
    private Node endNode;
    private String name;
    private String reference;
    private Map<String, Node> nodesMap;

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    /**
     * Builds the graph with Axiom.
     *
     * @param graphOM OM element of the graph.
     * @return the built graph. Will return null if supplied OMElement being null or it is not of the correct type.
     */
    public static AuthenticationGraphConfig build(OMElement graphOM) {

        if (graphOM == null) {
            return null;
        }

        AuthenticationGraphConfig authenticationGraphConfig = null;


        Map<String, Node> nodesMap = new HashMap<>();
        Iterator iterator = graphOM.getChildElements();
        while (iterator.hasNext()) {
            Object o = iterator.next();
            if (o instanceof OMElement) {
                OMElement nodeOM = (OMElement) o;
                switch (nodeOM.getLocalName()) {
                    case GraphConfigConstants.AUTHENTICATION_STEP_LOCAL_NAME:
                        StepNode stepNode = toStepNode(nodeOM);
                        nodesMap.put(stepNode.getName(), stepNode);
                        break;
                    case GraphConfigConstants.AUTHENTICATION_DECISION_LOCAL_NAME:
                        DecisionNode decisionNode = toDecisionNode(nodeOM);
                        nodesMap.put(decisionNode.getName(), decisionNode);
                        break;
                    default:
                        log.error("Unknown element:" + nodeOM.getLocalName() + " in graph node");
                }
            }
        }

        String startNodeName = graphOM.getAttributeValue(new QName(null, "start"));

        Node startNode = nodesMap.get(startNodeName);
        if (startNode == null) {
            log.error("There was no start node found with the name: " + startNodeName +
                    ". No further processing possible on the authentication graph.");
        } else {
            Map<Node, VisitedNodeInfo> visitedNodesInfo = new HashMap<>();
            try {
                visit(nodesMap, startNode, visitedNodesInfo);
                authenticationGraphConfig = new AuthenticationGraphConfig();
                authenticationGraphConfig.startNode = startNode;
                authenticationGraphConfig.nodesMap = nodesMap;
                authenticationGraphConfig.name = graphOM.getAttributeValue(new QName("name"));
            } catch (IdentityApplicationManagementException e) {
                log.error("Error in building authentication graph", e);
            }
        }

        String graphRef = graphOM.getAttributeValue(new QName(IdentityApplicationConstants.REF));
        authenticationGraphConfig.setReference(graphRef);

        return authenticationGraphConfig;
    }

    public Node getNodeByName(String name) {

        return nodesMap.get(name);
    }

    private static void visit(Map<String, Node> nodesMap, Node currentNode,
                              Map<Node, VisitedNodeInfo> visitedNodesInfoMap) throws IdentityApplicationManagementException {

        VisitedNodeInfo visitedNodeInfo = visitedNodesInfoMap.get(currentNode);
        if (visitedNodeInfo == null) {
            visitedNodeInfo = new VisitedNodeInfo();
            visitedNodesInfoMap.put(currentNode, visitedNodeInfo);
        } else {
            //This node is already visited. Just exit visiting.
            visitedNodeInfo.incrementReferenceCount();
            if (visitedNodeInfo.getReferenceCount() > MAX_RECURSION_COUNT) {
                //The graph can not have too many cycles. Most probably a misconfiguration. Need to bail out.
                String error = "Maximum number :[" + MAX_RECURSION_COUNT
                        + "] of reference to the same node detected. Graph building interrupted. Node Name :" + currentNode
                        .getName();
                throw new IdentityApplicationManagementException(error);
            }
            return;
        }

        if (currentNode instanceof StepNode) {
            StepNode stepNode = (StepNode) currentNode;
            Link nextLink = stepNode.getNext();
            if (!stepNode.hasNext()) {
                if (log.isDebugEnabled()) {
                    log.debug("No next Link found for the step : " + stepNode.getName());
                }
                return;
            }
            String nextName = nextLink.getNextLink();
            Node nextNode = nodesMap.get(nextName);
            if (nextNode == null) {
                log.error("There was no node found as for the next link: " + nextName + " for the node: " + currentNode
                        .getName());
            } else {
                visit(nodesMap, nextNode, visitedNodesInfoMap);
            }
        } else if (currentNode instanceof DecisionNode) {
            DecisionNode decisionNode = (DecisionNode) currentNode;
            List<Link> links = decisionNode.getLinks();
            for (Link nextLink : links) {
                if (nextLink == null) {
                    log.error("No next Link found for the decision : " + decisionNode.getName());
                    return;
                }
                String nextName = nextLink.getNextLink();
                Node nextNode = nodesMap.get(nextName);
                if (nextNode == null) {
                    if (log.isDebugEnabled()) {
                        log.debug("There was no node found as for the next link: " + nextName + " for the node: "
                                + currentNode.getName());
                    }
                } else {
                    visit(nodesMap, nextNode, visitedNodesInfoMap);
                }
            }

        }
    }

    public Node getStartNode() {

        return startNode;
    }

    public void setStartNode(Node startNode) {

        this.startNode = startNode;
    }

    public Node getEndNode() {

        return endNode;
    }

    public void setEndNode(Node endNode) {

        this.endNode = endNode;
    }

    private static DecisionNode toDecisionNode(OMElement nodeOM) {

        return DecisionNode.build(nodeOM);
    }

    private static StepNode toStepNode(OMElement nodeOM) {

        return StepNode.build(nodeOM);
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    private static class VisitedNodeInfo {

        int referenceCount = 0;

        public int getReferenceCount() {

            return referenceCount;
        }

        public void incrementReferenceCount() {

            referenceCount++;
        }
    }
}
