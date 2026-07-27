/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.rule.metadata.config;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.rule.metadata.api.exception.RuleMetadataConfigException;
import org.wso2.carbon.identity.rule.metadata.api.model.Operator;
import org.wso2.carbon.identity.rule.metadata.internal.config.OperatorConfig;

import java.io.File;
import java.util.Map;
import java.util.Objects;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class OperatorConfigTest {

    @Test
    public void testLoadOperatorsFromValidConfig() throws RuleMetadataConfigException {

        String filePath = Objects.requireNonNull(getClass().getClassLoader().getResource(
                        "configs/valid-operators.json"))
                .getFile();
        OperatorConfig operatorConfig = OperatorConfig.load(new File(filePath));

        Map<String, Operator> operatorsMap = operatorConfig.getOperatorsMap();

        assertNotNull(operatorsMap);
        assertEquals(operatorsMap.size(), 3);

        Operator equalsOperator = operatorsMap.get("equals");
        assertNotNull(equalsOperator);
        assertEquals(equalsOperator.getName(), "equals");
        assertEquals(equalsOperator.getDisplayName(), "equals");

        Operator notEqualsOperator = operatorsMap.get("notEquals");
        assertNotNull(notEqualsOperator);
        assertEquals(notEqualsOperator.getName(), "notEquals");
        assertEquals(notEqualsOperator.getDisplayName(), "not equals");

        Operator containsOperator = operatorsMap.get("contains");
        assertNotNull(containsOperator);
        assertEquals(containsOperator.getName(), "contains");
        assertEquals(containsOperator.getDisplayName(), "contains");
    }

    @DataProvider(name = "invalidConfigFiles")
    public Object[][] invalidConfigFiles() {

        return new Object[][]{
                {"configs/invalid-operators-missing-operator-name.json"},
                {"configs/invalid-operators-missing-operator-displayname.json"},
                {"unavailable-file.json"}
        };
    }

    @Test(dataProvider = "invalidConfigFiles", expectedExceptions = RuleMetadataConfigException.class,
            expectedExceptionsMessageRegExp = "Error while loading operators from file: .*")
    public void testLoadOperatorsFromInvalidConfig(String filePath) throws RuleMetadataConfigException {

        OperatorConfig.load(filePath.equals("unavailable-file.json") ? new File(filePath) :
                new File(getClass().getClassLoader().getResource(filePath).getFile()));
    }
}
