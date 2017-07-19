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

import java.io.Serializable;

/**
 * Represents an outgoing link from a node.
 */
public class Link implements Serializable {

    private static final long serialVersionUID = 6552125621313157281L;

    private String nextLink;
    private String name;
    private String expression;
    private boolean isEnd;

    public Link(String nextLink, String name, String expression) {

        this.nextLink = nextLink;
        this.name = name;
        this.expression = expression;
    }

    /**
     * Builds the link with Axiom.
     *
     * @param linkOM OM element of the link.
     * @return the built link. Will result in null if supplied OMElement is null.
     */
    public static Link build(OMElement linkOM) {

        if (linkOM == null) {
            return null;
        }
        String name = linkOM.getAttributeValue(GraphConfigConstants.ATTR_NAME);
        String nextNode = linkOM.getAttributeValue(GraphConfigConstants.ATTR_NEXT);
        String expression = linkOM.getAttributeValue(GraphConfigConstants.ATTR_EXPRESSION);
        String end = linkOM.getAttributeValue(GraphConfigConstants.ATTR_END);
        Link result = new Link(nextNode, name, expression);
        result.isEnd = Boolean.parseBoolean(end);
        return result;
    }

    public String getNextLink() {

        return nextLink;
    }

    public String getName() {

        return name;
    }

    public String getExpression() {

        return expression;
    }

    public boolean isEnd() {

        return isEnd;
    }
}
