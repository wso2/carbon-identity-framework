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

package org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl;

import org.wso2.carbon.identity.application.authentication.framework.AbstractFrameworkTest;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.graph.DecisionNode;
import org.wso2.carbon.identity.application.common.model.graph.Link;

public class AmrDecisionEvaluatorTest extends AbstractFrameworkTest {

    private static final String APPLICATION_AUTHENTICATION_FILE_NAME = "application-authentication-GraphStepHandlerTest.xml";
    private AmrDecisionEvaluator evaluator = new AmrDecisionEvaluator();

    public void testEvaluate_NoAmr() throws Exception {
        ServiceProvider sp1 = getTestServiceProvider("graph-sp-1.xml");
        AuthenticationContext context = getAuthenticationContext("", APPLICATION_AUTHENTICATION_FILE_NAME, sp1);

        String result = evaluator.evaluate(context, sp1, createTestNode("one", "two"));
        assertNull(result);
    }

    DecisionNode createTestNode(String... outcomes) {
        DecisionNode node = new DecisionNode();
        for (String s : outcomes) {
            Link link = new Link(s, s, s);
            node.getLinks().add(link);
        }
        return node;
    }

}