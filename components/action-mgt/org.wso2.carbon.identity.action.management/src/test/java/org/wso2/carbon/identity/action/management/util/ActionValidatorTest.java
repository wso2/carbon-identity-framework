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

package org.wso2.carbon.identity.action.management.util;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.management.api.exception.ActionMgtClientException;
import org.wso2.carbon.identity.action.management.internal.util.ActionValidator;

/**
 * Unit test class for ActionValidator class.
 */
public class ActionValidatorTest {

    private static final String ERROR_INVALID_REQUEST = "Invalid request.";
    private ActionValidator actionValidator;

    @BeforeClass
    public void setUp() {

        actionValidator = new ActionValidator();
    }

    @AfterClass
    public void tearDown() {

        actionValidator = null;
    }

    @DataProvider
    public Object[][] isBlankDataProvider() {

        return new String[][]{
                {"Action name", null},
                {"Endpoint authentication URI", ""}
        };
    }

    @Test(dataProvider = "isBlankDataProvider")
    public void testIsBlank(String fieldName, String fieldValue) {

        try {
            actionValidator.validateForBlank(fieldName, fieldValue);
        } catch (ActionMgtClientException e) {
            Assert.assertEquals(e.getMessage(), ERROR_INVALID_REQUEST);
            Assert.assertEquals(e.getDescription(), fieldName + " is empty.");
        }
    }

    @DataProvider
    public Object[][] isNotBlankDataProvider() {

        return new String[][]{
                {"Action name", "test-action"}
        };
    }

    @Test(dataProvider = "isNotBlankDataProvider")
    public void testIsNotBlank(String fieldName, String fieldValue) throws ActionMgtClientException {

        actionValidator.validateForBlank(fieldName, fieldValue);
    }

    @DataProvider
    public Object[][] invalidActionNameDataProvider() {

        return new String[][]{
                {"@test-action"},
                {"test#action"},
                {"test(header"},
                {"test*header"},
        };
    }

    @Test(dataProvider = "invalidActionNameDataProvider")
    public void testIsInvalidActionName(String actionName) {

        try {
            actionValidator.validateActionName(actionName);
        } catch (ActionMgtClientException e) {
            Assert.assertEquals(e.getMessage(), ERROR_INVALID_REQUEST);
            Assert.assertEquals(e.getDescription(), "Action name is invalid.");
        }
    }

    @DataProvider
    public Object[][] validActionNameDataProvider() {

        return new String[][]{
                {"test-action"},
                {"testaction1"},
                {"test_header"},
                {"testHeader"},
        };
    }

    @Test(dataProvider = "validActionNameDataProvider")
    public void testIsValidActionName(String actionName) throws ActionMgtClientException {

        actionValidator.validateActionName(actionName);
    }

    @DataProvider
    public Object[][] invalidEndpointUriDataProvider() {

        return new String[][]{
                {"http://example.com/path?query=param"},
                {"https://example .com "},
                {"ftps://fileserver.com/resource"},
                {"https://-example.com "},
        };
    }

    @Test(dataProvider = "invalidEndpointUriDataProvider")
    public void testIsInvalidEndpointUri(String endpointUri) {

        try {
            actionValidator.validateEndpointUri(endpointUri);
        } catch (ActionMgtClientException e) {
            Assert.assertEquals(e.getMessage(), ERROR_INVALID_REQUEST);
            Assert.assertEquals(e.getDescription(), "Endpoint URI is invalid.");
        }
    }

    @DataProvider
    public Object[][] validEndpointUriDataProvider() {

        return new String[][]{
                {"https://example.com/path?query=param"},
                {"https://example.com"},
                {"https://example.uk"}
        };
    }

    @Test(dataProvider = "validEndpointUriDataProvider")
    public void testIsValidEndpointUriName(String endpointUri) throws ActionMgtClientException {

        actionValidator.validateEndpointUri(endpointUri);
    }

    @DataProvider
    public Object[][] invalidHeaderDataProvider() {

        return new String[][]{
                {"-test-header"},
                {".test-header"},
                {"test@header"},
                {"test_header"}
        };
    }

    @Test(dataProvider = "invalidHeaderDataProvider")
    public void testIsInvalidHeader(String header) {

        try {
            actionValidator.validateHeader(header);
        } catch (ActionMgtClientException e) {
            Assert.assertEquals(e.getMessage(), ERROR_INVALID_REQUEST);
            Assert.assertEquals(e.getDescription(), "API key header name is invalid.");
        }
    }

    @DataProvider
    public Object[][] validHeaderDataProvider() {

        return new String[][]{
                {"test-header"},
                {"test.header"},
                {"testheader1"},
                {"testHeader"}
        };
    }

    @Test(dataProvider = "validHeaderDataProvider")
    public void testIsValidHeader(String header) throws ActionMgtClientException {

        actionValidator.validateHeader(header);
    }
}
