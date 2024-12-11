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
package org.wso2.carbon.identity.input.validation.mgt.test.model.validators;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.input.validation.mgt.exceptions.InputValidationMgtClientException;
import org.wso2.carbon.identity.input.validation.mgt.model.ValidationContext;
import org.wso2.carbon.identity.input.validation.mgt.model.validators.AbstractRulesValidator;
import org.wso2.carbon.identity.input.validation.mgt.model.validators.LengthValidator;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.MAX_LENGTH;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.MAX_PASSWORD_ALLOWED_LENGTH;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.MIN_LENGTH;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.PASSWORD;

/**
 * Testing the AbstractRulesValidator class
 */
public class AbstractRulesValidatorTest {

    // Test constants.
    private static final String TENANT_DOMAIN = "carbon.super";
    @Mock
    private ValidationContext mockContext;
    private MockedStatic<IdentityUtil> identityUtil;

    @BeforeMethod
    public void setup() {

        MockitoAnnotations.openMocks(this);
        identityUtil = mockStatic(IdentityUtil.class);
    }

    @AfterMethod
    public void tearDown() {

        identityUtil.close();
    }

    @DataProvider(name = "validationScenarios")
    public Object[][] validationScenarios() {

        Map<String, String> validProperties = new HashMap<>();
        validProperties.put(MIN_LENGTH, "5");
        validProperties.put(MAX_LENGTH, "10");

        Map<String, String> invalidMinProperties = new HashMap<>();
        invalidMinProperties.put(MIN_LENGTH, "-1");

        Map<String, String> invalidMaxProperties = new HashMap<>();
        invalidMaxProperties.put(MAX_LENGTH, "-1");

        Map<String, String> minGreaterThanMaxProperties = new HashMap<>();
        minGreaterThanMaxProperties.put(MIN_LENGTH, "15");
        minGreaterThanMaxProperties.put(MAX_LENGTH, "10");

        Map<String, String> maxLengthExceedsPassword = new HashMap<>();
        maxLengthExceedsPassword.put(MAX_LENGTH, "65");

        Map<String, String> validPasswordProperties = new HashMap<>();
        validPasswordProperties.put(MAX_LENGTH, "64");

        Map<String, String> passwordPropertiesWithoutMax = new HashMap<>();
        passwordPropertiesWithoutMax.put(MIN_LENGTH, "8");

        return new Object[][]{
                // Valid scenario.
                {validProperties, "VALID_FIELD", TENANT_DOMAIN, true, null},

                // Invalid MIN_LENGTH.
                {invalidMinProperties, "VALID_FIELD", TENANT_DOMAIN, false, InputValidationMgtClientException.class},

                // Invalid MAX_LENGTH.
                {invalidMaxProperties, "VALID_FIELD", TENANT_DOMAIN, false, InputValidationMgtClientException.class},

                // MIN_LENGTH greater than MAX_LENGTH.
                {minGreaterThanMaxProperties, "VALID_FIELD", TENANT_DOMAIN, false,
                        InputValidationMgtClientException.class},

                // MAX_LENGTH exceeds max password length.
                {maxLengthExceedsPassword, PASSWORD, TENANT_DOMAIN, false, InputValidationMgtClientException.class},

                // Valid password properties.
                {validPasswordProperties, PASSWORD, TENANT_DOMAIN, true, null},

                // Password properties without MAX_LENGTH.
                {passwordPropertiesWithoutMax, PASSWORD, TENANT_DOMAIN, true, null}};
    }

    @Test(dataProvider = "validationScenarios")
    public void testValidateProps(Map<String, String> properties, String field, String tenantDomain,
                                  boolean expectedResult, Class<? extends Exception> expectedException) {
        // Mock context.
        when(mockContext.getProperties()).thenReturn(properties);
        when(mockContext.getField()).thenReturn(field);
        when(mockContext.getTenantDomain()).thenReturn(tenantDomain);

        // Mock IdentityUtil.
        when(IdentityUtil.getProperty(MAX_PASSWORD_ALLOWED_LENGTH)).thenReturn("64");

        // Test execution.
        AbstractRulesValidator validator = new LengthValidator(); // Replace with your validator class name
        try {
            boolean result = validator.validateProps(mockContext);
            assertEquals(result, expectedResult, "Unexpected validation result.");
            if (expectedException != null) {
                fail("Expected exception but none was thrown.");
            }
        } catch (Exception e) {
            if (expectedException == null || !expectedException.isInstance(e)) {
                fail("Unexpected exception: " + e.getMessage());
            }
        }
    }
}

