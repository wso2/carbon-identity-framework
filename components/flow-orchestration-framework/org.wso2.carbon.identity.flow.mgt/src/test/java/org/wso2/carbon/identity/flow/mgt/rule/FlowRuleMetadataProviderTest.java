/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.flow.mgt.rule;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.rule.metadata.api.model.FieldDefinition;
import org.wso2.carbon.identity.rule.metadata.api.model.FlowType;
import org.wso2.carbon.identity.rule.metadata.api.model.InputValue;
import org.wso2.carbon.identity.rule.metadata.api.model.Operator;
import org.wso2.carbon.identity.rule.metadata.api.model.OptionsReferenceValue;
import org.wso2.carbon.identity.rule.metadata.internal.config.OperatorConfig;
import org.wso2.carbon.identity.rule.metadata.internal.config.RuleMetadataConfigFactory;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mockStatic;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests what a flow publishes for its conditions to be written against.
 */
public class FlowRuleMetadataProviderTest {

    private static final String TENANT_DOMAIN = "carbon.super";

    private final FlowRuleMetadataProvider provider = new FlowRuleMetadataProvider();
    private MockedStatic<RuleMetadataConfigFactory> configFactoryMock;

    @BeforeClass
    public void setUpClass() throws Exception {

        String operators = Objects.requireNonNull(
                getClass().getClassLoader().getResource("configs/valid-operators.json")).getFile();
        OperatorConfig operatorConfig = OperatorConfig.load(new File(operators));
        configFactoryMock = mockStatic(RuleMetadataConfigFactory.class);
        configFactoryMock.when(RuleMetadataConfigFactory::getOperatorConfig).thenReturn(operatorConfig);
    }

    @AfterClass
    public void tearDownClass() {

        configFactoryMock.close();
    }

    @Test
    public void testEveryOrchestrationFlowPublishesTheSameFields() throws Exception {

        for (FlowType flowType : new FlowType[]{FlowType.REGISTRATION, FlowType.PASSWORD_RECOVERY,
                FlowType.INVITED_USER_REGISTRATION}) {
            assertEquals(fieldsOf(flowType).keySet().size(), 3, flowType + " should publish three fields.");
            assertTrue(fieldsOf(flowType).keySet().containsAll(Arrays.asList(
                    FlowRuleMetadataProvider.USER_CLAIMS, FlowRuleMetadataProvider.USER_COLLECTED_CLAIMS,
                    FlowRuleMetadataProvider.FLOW_PROPERTIES)));
        }
    }

    @Test
    public void testOtherFlowsPublishNothing() throws Exception {

        assertTrue(provider.getExpressionMeta(FlowType.PRE_ISSUE_ACCESS_TOKEN, TENANT_DOMAIN).isEmpty());
    }

    /**
     * The claim key is picked from the claims endpoint, by URI, so the console can offer a list
     * rather than a text box.
     */
    @Test
    public void testClaimFieldsArePickedFromTheClaimList() throws Exception {

        for (String name : new String[]{FlowRuleMetadataProvider.USER_CLAIMS,
                FlowRuleMetadataProvider.USER_COLLECTED_CLAIMS}) {
            FieldDefinition field = fieldsOf(FlowType.REGISTRATION).get(name);
            assertTrue(field.getField().getQualifier() instanceof OptionsReferenceValue,
                    name + " should be qualified by a list.");
            OptionsReferenceValue qualifier = (OptionsReferenceValue) field.getField().getQualifier();
            assertEquals(qualifier.getValueReferenceAttribute(), "claimURI");
            assertEquals(qualifier.getLinks().size(), 1);
            assertEquals(qualifier.getLinks().get(0).getRel(), "values");
            assertTrue(qualifier.getLinks().get(0).getHref().startsWith("/claim-dialects/local/claims"));
            assertTrue(operatorNames(field).contains("in"));
            assertTrue(operatorNames(field).contains("greaterThan"));
        }
    }

    @Test
    public void testFlowPropertyQualifierIsTyped() throws Exception {

        FieldDefinition field = fieldsOf(FlowType.REGISTRATION).get(FlowRuleMetadataProvider.FLOW_PROPERTIES);

        assertTrue(field.getField().getQualifier() instanceof InputValue);
        assertFalse(operatorNames(field).contains("in"), "Properties are single values.");
        assertFalse(operatorNames(field).contains("greaterThan"), "Properties are compared as text.");
    }

    @Test
    public void testPublishedFieldsAreTheKeyedOnes() throws Exception {

        for (FieldDefinition field : provider.getExpressionMeta(FlowType.REGISTRATION, TENANT_DOMAIN)) {
            assertTrue(FlowRuleMetadataProvider.isQualified(field.getField().getName()),
                    field.getField().getName() + " is published with a qualifier and must be treated as qualified.");
        }
        assertFalse(FlowRuleMetadataProvider.isQualified("user.groups"));
    }

    private Map<String, FieldDefinition> fieldsOf(FlowType flowType) throws Exception {

        return provider.getExpressionMeta(flowType, TENANT_DOMAIN).stream()
                .collect(Collectors.toMap(field -> field.getField().getName(), Function.identity()));
    }

    private static List<String> operatorNames(FieldDefinition field) {

        return field.getOperators().stream().map(Operator::getName).collect(Collectors.toList());
    }
}
