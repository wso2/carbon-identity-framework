/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

import org.mockito.MockedStatic;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.rule.evaluation.api.model.Operator;
import org.wso2.carbon.identity.rule.evaluation.internal.component.RuleEvaluationComponentServiceHolder;
import org.wso2.carbon.identity.rule.evaluation.internal.service.impl.OperatorRegistry;
import org.wso2.carbon.identity.rule.metadata.api.service.RuleMetadataService;
import org.wso2.carbon.identity.rule.metadata.internal.config.OperatorConfig;
import org.wso2.carbon.identity.rule.metadata.internal.config.RuleMetadataConfigFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for OperatorRegistry.
 */
public class OperatorRegistryTest {

    private OperatorRegistry operatorRegistry;
    private MockedStatic<RuleMetadataConfigFactory> ruleMetadataConfigFactoryMockedStatic;

    @BeforeClass
    public void setUp() throws Exception {

        String filePath = Objects.requireNonNull(getClass().getClassLoader().getResource(
                "configs/valid-operators.json")).getFile();
        OperatorConfig operatorConfig = OperatorConfig.load(new File(filePath));

        ruleMetadataConfigFactoryMockedStatic = mockStatic(RuleMetadataConfigFactory.class);
        ruleMetadataConfigFactoryMockedStatic.when(RuleMetadataConfigFactory::getOperatorConfig)
                .thenReturn(operatorConfig);

        RuleMetadataService ruleMetadataService = mock(RuleMetadataService.class);
        when(ruleMetadataService.getApplicableOperatorsInExpressions()).thenReturn(
                new ArrayList<>(operatorConfig.getOperatorsMap().values()));
        RuleEvaluationComponentServiceHolder.getInstance().setRuleMetadataService(ruleMetadataService);
        operatorRegistry = RuleEvaluationComponentServiceHolder.getInstance().getOperatorRegistry();
    }

    @AfterClass
    public void tearDown() {

        ruleMetadataConfigFactoryMockedStatic.close();
    }

    @Test
    public void testAllOperatorsLoaded() {

        assertNotNull(operatorRegistry.getOperator("equals"));
        assertNotNull(operatorRegistry.getOperator("notEquals"));
        assertNotNull(operatorRegistry.getOperator("contains"));
        assertNotNull(operatorRegistry.getOperator("notContains"));
        assertNotNull(operatorRegistry.getOperator("startsWith"));
        assertNotNull(operatorRegistry.getOperator("endsWith"));
        assertNotNull(operatorRegistry.getOperator("greaterThan"));
        assertNotNull(operatorRegistry.getOperator("lessThan"));
    }

    @Test
    public void testGetNonExistentOperator() {

        assertNull(operatorRegistry.getOperator("nonExistent"));
    }

    // Equals operator tests.

    @DataProvider(name = "equalsDataProvider")
    public Object[][] equalsDataProvider() {

        return new Object[][]{
                {"hello", "hello", true},
                {"hello", "world", false},
                {10.0, 10.0, true},
                {10.0, 20.0, false},
                {true, true, true},
                {true, false, false}
        };
    }

    @Test(dataProvider = "equalsDataProvider")
    public void testEqualsOperator(Object a, Object b, boolean expected) {

        Operator operator = operatorRegistry.getOperator("equals");
        assertOperatorResult(operator, a, b, expected);
    }

    // NotEquals operator tests.

    @DataProvider(name = "notEqualsDataProvider")
    public Object[][] notEqualsDataProvider() {

        return new Object[][]{
                {"hello", "hello", false},
                {"hello", "world", true},
                {10.0, 10.0, false},
                {10.0, 20.0, true},
                {true, false, true}
        };
    }

    @Test(dataProvider = "notEqualsDataProvider")
    public void testNotEqualsOperator(Object a, Object b, boolean expected) {

        Operator operator = operatorRegistry.getOperator("notEquals");
        assertOperatorResult(operator, a, b, expected);
    }

    // Contains operator tests.

    @DataProvider(name = "containsDataProvider")
    public Object[][] containsDataProvider() {

        return new Object[][]{
                {"hello world", "world", true},
                {"hello world", "mars", false},
                {"hello", "hello", true},
                {"hello", "", true},
                {10, "hello", false}  // Non-string types.
        };
    }

    @Test(dataProvider = "containsDataProvider")
    public void testContainsOperator(Object a, Object b, boolean expected) {

        Operator operator = operatorRegistry.getOperator("contains");
        assertOperatorResult(operator, a, b, expected);
    }

    // NotContains operator tests.

    @DataProvider(name = "notContainsDataProvider")
    public Object[][] notContainsDataProvider() {

        return new Object[][]{
                {"hello world", "world", false},
                {"hello world", "mars", true},
                {"hello", "hello", false},
                {"hello", "", false},
                {10, "hello", false}  // Non-string types.
        };
    }

    @Test(dataProvider = "notContainsDataProvider")
    public void testNotContainsOperator(Object a, Object b, boolean expected) {

        Operator operator = operatorRegistry.getOperator("notContains");
        assertOperatorResult(operator, a, b, expected);
    }

    // StartsWith operator tests.

    @DataProvider(name = "startsWithDataProvider")
    public Object[][] startsWithDataProvider() {

        return new Object[][]{
                {"hello world", "hello", true},
                {"hello world", "world", false},
                {"hello", "hello", true},
                {"hello", "", true},
                {10, "hello", false}  // Non-string types.
        };
    }

    @Test(dataProvider = "startsWithDataProvider")
    public void testStartsWithOperator(Object a, Object b, boolean expected) {

        Operator operator = operatorRegistry.getOperator("startsWith");
        assertOperatorResult(operator, a, b, expected);
    }

    // EndsWith operator tests.

    @DataProvider(name = "endsWithDataProvider")
    public Object[][] endsWithDataProvider() {

        return new Object[][]{
                {"hello world", "world", true},
                {"hello world", "hello", false},
                {"hello", "hello", true},
                {"hello", "", true},
                {10, "hello", false}  // Non-string types.
        };
    }

    @Test(dataProvider = "endsWithDataProvider")
    public void testEndsWithOperator(Object a, Object b, boolean expected) {

        Operator operator = operatorRegistry.getOperator("endsWith");
        assertOperatorResult(operator, a, b, expected);
    }

    // GreaterThan operator tests.

    @DataProvider(name = "greaterThanDataProvider")
    public Object[][] greaterThanDataProvider() {

        return new Object[][]{
                {20.0, 10.0, true},
                {10.0, 20.0, false},
                {10.0, 10.0, false},
                {"b", "a", true},
                {"a", "b", false},
                {"a", 1, false}  // Mismatched types.
        };
    }

    @Test(dataProvider = "greaterThanDataProvider")
    public void testGreaterThanOperator(Object a, Object b, boolean expected) {

        Operator operator = operatorRegistry.getOperator("greaterThan");
        assertOperatorResult(operator, a, b, expected);
    }

    // LessThan operator tests.

    @DataProvider(name = "lessThanDataProvider")
    public Object[][] lessThanDataProvider() {

        return new Object[][]{
                {10.0, 20.0, true},
                {20.0, 10.0, false},
                {10.0, 10.0, false},
                {"a", "b", true},
                {"b", "a", false},
                {"a", 1, false}  // Mismatched types.
        };
    }

    @Test(dataProvider = "lessThanDataProvider")
    public void testLessThanOperator(Object a, Object b, boolean expected) {

        Operator operator = operatorRegistry.getOperator("lessThan");
        assertOperatorResult(operator, a, b, expected);
    }

    @Test
    public void testLoadOperatorsWithNullMetadataService() {

        RuleMetadataService originalService = RuleEvaluationComponentServiceHolder.getInstance()
                .getRuleMetadataService();
        try {
            // Temporarily clear the metadata service to test the null path.
            RuleEvaluationComponentServiceHolder.getInstance().setRuleMetadataService(null);
            OperatorRegistry registry = OperatorRegistry.loadOperators();
            assertNotNull(registry);
        } finally {
            RuleEvaluationComponentServiceHolder.getInstance().setRuleMetadataService(originalService);
        }
    }

    private void assertOperatorResult(Operator operator, Object a, Object b, boolean expected) {

        if (expected) {
            assertTrue(operator.apply(a, b));
        } else {
            assertFalse(operator.apply(a, b));
        }
    }
}
