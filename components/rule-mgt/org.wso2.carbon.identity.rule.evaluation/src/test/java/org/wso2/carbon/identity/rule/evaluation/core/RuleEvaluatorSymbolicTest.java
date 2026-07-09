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

package org.wso2.carbon.identity.rule.evaluation.core;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.rule.evaluation.api.exception.RuleEvaluationException;
import org.wso2.carbon.identity.rule.evaluation.api.model.FieldValue;
import org.wso2.carbon.identity.rule.evaluation.api.resolver.SymbolicValueResolver;
import org.wso2.carbon.identity.rule.evaluation.api.resolver.SymbolicValueResolverRegistry;
import org.wso2.carbon.identity.rule.evaluation.internal.component.RuleEvaluationComponentServiceHolder;
import org.wso2.carbon.identity.rule.evaluation.internal.service.impl.OperatorRegistry;
import org.wso2.carbon.identity.rule.evaluation.internal.service.impl.RuleEvaluator;
import org.wso2.carbon.identity.rule.management.api.model.ANDCombinedRule;
import org.wso2.carbon.identity.rule.management.api.model.Expression;
import org.wso2.carbon.identity.rule.management.api.model.ORCombinedRule;
import org.wso2.carbon.identity.rule.management.api.model.Rule;
import org.wso2.carbon.identity.rule.management.api.model.Value;
import org.wso2.carbon.identity.rule.metadata.api.model.Operator;
import org.wso2.carbon.identity.rule.metadata.api.service.RuleMetadataService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for evaluating rules with symbolic values in RuleEvaluator.
 */
public class RuleEvaluatorSymbolicTest {

    private static final String OS_VERSION = "osVersion";

    // Test resolver: symbolic tokens resolve to numeric values; special tokens exercise the error paths.
    private static final SymbolicValueResolver TEST_RESOLVER = symbolicValue -> {
        switch (symbolicValue) {
            case "LATEST":
                return new Value(Value.Type.NUMBER, "15");
            case "LATEST,PREVIOUS":
                return new Value(Value.Type.LIST, "15,14");
            case "BAD_LIST":
                return new Value(Value.Type.LIST, "abc,def");
            case "BAD_TYPE":
                return new Value(Value.Type.STRING, "x");
            default:
                return new Value(Value.Type.NUMBER, symbolicValue);
        }
    };

    private RuleEvaluator ruleEvaluator;

    @BeforeClass
    public void setUpClass() {

        RuleMetadataService ruleMetadataService = mock(RuleMetadataService.class);
        when(ruleMetadataService.getApplicableOperatorsInExpressions()).thenReturn(
                Arrays.asList(new Operator("equals", "equals"), new Operator("in", "in")));
        RuleEvaluationComponentServiceHolder.getInstance().setRuleMetadataService(ruleMetadataService);

        SymbolicValueResolverRegistry.getInstance().register(OS_VERSION, TEST_RESOLVER);
    }

    @BeforeMethod
    public void setUpMethod() {

        OperatorRegistry operatorRegistry = RuleEvaluationComponentServiceHolder.getInstance().getOperatorRegistry();
        ruleEvaluator = new RuleEvaluator(operatorRegistry);
    }

    @AfterClass
    public void tearDownClass() {

        SymbolicValueResolverRegistry.getInstance().deregister(OS_VERSION);
    }

    @Test
    public void testSymbolicInWithListMatch() throws Exception {

        assertTrue(ruleEvaluator.evaluate(symbolicRule(OS_VERSION, "in", "LATEST,PREVIOUS"),
                numberData(OS_VERSION, 15)));
    }

    @Test
    public void testSymbolicInWithListNoMatch() throws Exception {

        assertFalse(ruleEvaluator.evaluate(symbolicRule(OS_VERSION, "in", "LATEST,PREVIOUS"),
                numberData(OS_VERSION, 99)));
    }

    @Test
    public void testSymbolicInWithSingleValueMatch() throws Exception {

        // 'in' with a single resolved value (NUMBER) must still do a membership check and match.
        assertTrue(ruleEvaluator.evaluate(symbolicRule(OS_VERSION, "in", "LATEST"),
                numberData(OS_VERSION, 15)));
    }

    @Test
    public void testSymbolicEqualsMatch() throws Exception {

        assertTrue(ruleEvaluator.evaluate(symbolicRule(OS_VERSION, "equals", "LATEST"),
                numberData(OS_VERSION, 15)));
    }

    @Test
    public void testSymbolicEqualsNoMatch() throws Exception {

        assertFalse(ruleEvaluator.evaluate(symbolicRule(OS_VERSION, "equals", "LATEST"),
                numberData(OS_VERSION, 14)));
    }

    @Test(expectedExceptions = RuleEvaluationException.class,
            expectedExceptionsMessageRegExp = "Failed to parse numeric value in 'in' expression: abc,def")
    public void testSymbolicInWithNonNumericValueThrows() throws Exception {

        ruleEvaluator.evaluate(symbolicRule(OS_VERSION, "in", "BAD_LIST"), numberData(OS_VERSION, 15));
    }

    @Test(expectedExceptions = RuleEvaluationException.class,
            expectedExceptionsMessageRegExp = "Symbolic resolver returned unsupported type: STRING")
    public void testSymbolicUnsupportedResolvedTypeThrows() throws Exception {

        ruleEvaluator.evaluate(symbolicRule(OS_VERSION, "equals", "BAD_TYPE"), numberData(OS_VERSION, 15));
    }

    @Test(expectedExceptions = RuleEvaluationException.class,
            expectedExceptionsMessageRegExp = "No symbolic resolver registered for field: noResolverField")
    public void testSymbolicNoResolverRegisteredThrows() throws Exception {

        ruleEvaluator.evaluate(symbolicRule("noResolverField", "equals", "LATEST"),
                numberData("noResolverField", 15));
    }

    private Rule symbolicRule(String field, String operator, String symbolicValue) {

        Expression expression = new Expression.Builder().field(field).operator(operator)
                .value(new Value(Value.Type.SYMBOLIC, symbolicValue)).build();
        ANDCombinedRule andRule = new ANDCombinedRule.Builder().addExpression(expression).build();
        return new ORCombinedRule.Builder().addRule(andRule).build();
    }

    private Map<String, FieldValue> numberData(String field, double value) {

        Map<String, FieldValue> evaluationData = new HashMap<>();
        evaluationData.put(field, new FieldValue(field, value));
        return evaluationData;
    }
}
