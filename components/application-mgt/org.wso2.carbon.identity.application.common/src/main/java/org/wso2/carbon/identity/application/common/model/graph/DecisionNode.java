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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.QName;

/**
 * Config for the node representing a decision point in authentication steps graph.
 *
 */
public class DecisionNode extends Node implements Serializable {

    private static final long serialVersionUID = 6552125621314157281L;

    private static final Log log = LogFactory.getLog(DecisionNode.class);

    private String evaluatorName;
    private List<Link> links = new ArrayList<>();

    /**
     * Builds the decision node with Axiom.
     * @param decisionOM OM element of the decision.
     * @return the built decision node.
     */
    public static DecisionNode build(OMElement decisionOM) {
        DecisionNode decisionNode = new DecisionNode();
        Iterator<?> iter = decisionOM.getChildElements();

        String name = decisionOM.getAttributeValue(new QName(null, "name"));
        decisionNode.setName(name);
        while (iter.hasNext()) {
            OMElement member = (OMElement) iter.next();
            if ("Link".equals(member.getLocalName())) {
                Link link = Link.build(member);
                decisionNode.links.add(link);
            } else if ("Evaluator".equals(member.getLocalName())) {
                decisionNode.setEvaluatorName(member.getText());
            } else {
                log.error("Unsupported element: " + member.getLocalName() + ", in DecisionNode : " + name);
            }
        }

        return decisionNode;
    }

    public String getEvaluatorName() {
        return evaluatorName;
    }

    public void setEvaluatorName(String evaluatorName) {
        this.evaluatorName = evaluatorName;
    }

    public List<Link> getLinks() {
        return links;
    }
}
