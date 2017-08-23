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
import org.wso2.carbon.identity.application.common.model.AuthenticationStep;

import java.io.Serializable;

/**
 * Authentication step in a graph.
 */
public class StepNode extends Node implements Serializable {

    private static final long serialVersionUID = 6552125621314155271L;

    private Link next;
    private AuthenticationStep authenticationStep;

    /**
     * Builds the step with Axiom.
     *
     * @param stepElement OM element of the step.
     * @return the built step. Will result in null if supplied OMElement is null.
     */
    public static StepNode build(OMElement stepElement) {

        if (stepElement == null) {
            return null;
        }
        StepNode stepNode = new StepNode();
        String nextNodeName = stepElement.getAttributeValue(GraphConfigConstants.ATTR_NEXT);
        String name = stepElement.getAttributeValue(GraphConfigConstants.ATTR_NAME);
        stepNode.setName(name);
        if (nextNodeName != null) {
            stepNode.next = new Link(nextNodeName, name, null);
        }
        stepNode.authenticationStep = AuthenticationStep.build(stepElement);

        return stepNode;
    }

    public Link getNext() {

        return next;
    }

    public boolean hasNext() {

        if (next == null) {
            return false;
        }
        return true;
    }

    public AuthenticationStep getAuthenticationStep() {

        return authenticationStep;
    }

    public void setAuthenticationStep(AuthenticationStep authenticationStep) {

        this.authenticationStep = authenticationStep;
    }
}
