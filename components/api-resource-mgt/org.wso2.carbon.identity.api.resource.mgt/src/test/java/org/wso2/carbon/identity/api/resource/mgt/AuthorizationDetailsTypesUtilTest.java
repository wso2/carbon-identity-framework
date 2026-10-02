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

package org.wso2.carbon.identity.api.resource.mgt;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.api.resource.mgt.util.AuthorizationDetailsTypesUtil;

import java.util.List;
import java.util.Map;

/**
 * Test class for {@link AuthorizationDetailsTypesUtil}.
 */
public class AuthorizationDetailsTypesUtilTest {

    private static final String SCHEMA = "{\"type\":\"object\",\"required\":[\"type\"],\"minProperties\":1," +
            "\"properties\":{\"type\":{\"type\":\"string\",\"const\":\"test_type\"}," +
            "\"actions\":{\"type\":\"array\",\"items\":{\"type\":\"string\"},\"minItems\":1,\"maxItems\":3.0}," +
            "\"amount\":{\"type\":\"number\",\"minimum\":0.5,\"maximum\":9999999999}}," +
            "\"allOf\":[{\"maxProperties\":5}]}";

    @Test
    @SuppressWarnings("unchecked")
    public void testParseSchemaForValidationPreservesIntegralValues() {

        final Map<String, Object> schema = AuthorizationDetailsTypesUtil.parseSchemaForValidation(SCHEMA);
        final Map<String, Object> properties = (Map<String, Object>) schema.get("properties");
        final Map<String, Object> actions = (Map<String, Object>) properties.get("actions");
        final Map<String, Object> amount = (Map<String, Object>) properties.get("amount");
        final Map<String, Object> allOf = ((List<Map<String, Object>>) schema.get("allOf")).get(0);

        Assert.assertEquals(schema.get("minProperties"), 1);
        Assert.assertEquals(actions.get("minItems"), 1);
        Assert.assertEquals(actions.get("maxItems"), 3);
        Assert.assertEquals(allOf.get("maxProperties"), 5);
        Assert.assertEquals(amount.get("minimum"), 0.5d);
        Assert.assertEquals(amount.get("maximum"), 9999999999L);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testParseSchemaKeepsExistingNumberFormat() {

        final Map<String, Object> schema = AuthorizationDetailsTypesUtil.parseSchema(SCHEMA);
        final Map<String, Object> actions = (Map<String, Object>) ((Map<String, Object>) schema.get("properties"))
                .get("actions");

        Assert.assertEquals(schema.get("minProperties"), 1.0d);
        Assert.assertEquals(actions.get("minItems"), 1.0d);
        Assert.assertEquals(actions.get("maxItems"), 3.0d);
    }
}
