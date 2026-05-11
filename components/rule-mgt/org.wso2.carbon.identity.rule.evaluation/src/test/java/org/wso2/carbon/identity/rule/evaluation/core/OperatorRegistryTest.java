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

    @DataProvider(name = "operatorDataProvider")
    public Object[][] operatorDataProvider() {

        return new Object[][]{
                // Equals operator.
                {"equals", "hello", "hello", true},
                {"equals", "hello", "world", false},
                {"equals", 10.0, 10.0, true},
                {"equals", 10.0, 20.0, false},
                {"equals", true, true, true},
                {"equals", true, false, false},
                // NotEquals operator.
                {"notEquals", "hello", "hello", false},
                {"notEquals", "hello", "world", true},
                {"notEquals", 10.0, 10.0, false},
                {"notEquals", 10.0, 20.0, true},
                {"notEquals", true, false, true},
                // Contains operator.
                {"contains", "hello world", "world", true},
                {"contains", "hello world", "mars", false},
                {"contains", "hello", "hello", true},
                {"contains", "hello", "", true},
                {"contains", 10, "hello", false},  // Non-string types.
                // NotContains operator.
                {"notContains", "hello world", "world", false},
                {"notContains", "hello world", "mars", true},
                {"notContains", "hello", "hello", false},
                {"notContains", "hello", "", false},
                {"notContains", 10, "hello", false},  // Non-string types.
                // StartsWith operator.
                {"startsWith", "hello world", "hello", true},
                {"startsWith", "hello world", "world", false},
                {"startsWith", "hello", "hello", true},
                {"startsWith", "hello", "", true},
                {"startsWith", 10, "hello", false},  // Non-string types.
                // EndsWith operator.
                {"endsWith", "hello world", "world", true},
                {"endsWith", "hello world", "hello", false},
                {"endsWith", "hello", "hello", true},
                {"endsWith", "hello", "", true},
                {"endsWith", 10, "hello", false},  // Non-string types.
                // GreaterThan operator.
                {"greaterThan", 20.0, 10.0, true},
                {"greaterThan", 10.0, 20.0, false},
                {"greaterThan", 10.0, 10.0, false},
                {"greaterThan", "b", "a", true},
                {"greaterThan", "a", "b", false},
                {"greaterThan", "a", 1, false},  // Mismatched types.
                // LessThan operator.
                {"lessThan", 10.0, 20.0, true},
                {"lessThan", 20.0, 10.0, false},
                {"lessThan", 10.0, 10.0, false},
                {"lessThan", "a", "b", true},
                {"lessThan", "b", "a", false},
                {"lessThan", "a", 1, false}  // Mismatched types.
        };
    }

    @Test(dataProvider = "operatorDataProvider")
    public void testOperator(String operatorName, Object a, Object b, boolean expected) {

        Operator operator = operatorRegistry.getOperator(operatorName);
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
