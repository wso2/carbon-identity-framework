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
package org.wso2.carbon.identity.input.validation.mgt.test.model.validators;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.input.validation.mgt.exceptions.InputValidationMgtClientException;
import org.wso2.carbon.identity.input.validation.mgt.model.ValidationContext;
import org.wso2.carbon.identity.input.validation.mgt.model.validators.AlphanumericValidator;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.ENABLE_SPECIAL_CHARACTERS;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.ENABLE_VALIDATOR;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.USERNAME;

/**
 * Testing the AlphanumericValidator class.
 */
public class AlphanumericValidatorTest {

    private static final String TENANT_DOMAIN = "carbon.super";
    private static final String NOT_SET = null;

    @DataProvider(name = "alphanumericScenarios")
    public Object[][] alphanumericScenarios() {

        return new Object[][]{
                // enable.special.characters value, username, expected to be valid.
                {NOT_SET, "alice123", true},
                {NOT_SET, "alice-123", false},
                {"false", "alice123", true},
                {"false", "alice-123", false},
                {"false", "alice.smith", false},
                {"true", "alice123", true},
                {"true", "alice-123", true},
                {"true", "alice.smith", true},
        };
    }

    @Test(dataProvider = "alphanumericScenarios")
    public void testValidate(String enableSpecialCharacters, String username, boolean expectedValid) {

        Map<String, String> properties = new HashMap<>();
        properties.put(ENABLE_VALIDATOR, "true");
        if (enableSpecialCharacters != null) {
            properties.put(ENABLE_SPECIAL_CHARACTERS, enableSpecialCharacters);
        }
        ValidationContext context = new ValidationContext();
        context.setField(USERNAME);
        context.setValue(username);
        context.setTenantDomain(TENANT_DOMAIN);
        context.setProperties(properties);

        try {
            assertTrue(new AlphanumericValidator().validate(context));
            if (!expectedValid) {
                fail("Username '" + username + "' should be rejected when " + ENABLE_SPECIAL_CHARACTERS + " is "
                        + enableSpecialCharacters);
            }
        } catch (InputValidationMgtClientException e) {
            if (expectedValid) {
                fail("Username '" + username + "' should be accepted when " + ENABLE_SPECIAL_CHARACTERS + " is "
                        + enableSpecialCharacters + ": " + e.getDescription());
            }
        }
    }
}
