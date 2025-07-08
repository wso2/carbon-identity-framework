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

import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.management.api.exception.ActionMgtClientException;
import org.wso2.carbon.identity.action.management.internal.constant.ActionMgtConstants;
import org.wso2.carbon.identity.action.management.internal.util.ActionManagementConfig;
import org.wso2.carbon.identity.action.management.internal.util.ActionValidator;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

/**
 * Unit test class for ActionValidator class.
 */
public class ActionValidatorTest {

    private static final String ERROR_INVALID_REQUEST = "Invalid request.";
    private static final String ERROR_NOT_ALLOWED_HEADER = "Provided Headers are not allowed.";
    private static final String ERROR_NOT_ALLOWED_PARAMETER = "Provided Parameters are not allowed.";
    private ActionValidator actionValidator;
    private ActionManagementConfig actionManagementConfigMock;
    private IdentityConfigParser identityConfigParserMock;
    private MockedStatic<IdentityConfigParser> identityConfigParserMockedStatic;

    @BeforeClass
    public void setUp() {

        actionValidator = new ActionValidator();

        actionManagementConfigMock = mock(ActionManagementConfig.class);
        identityConfigParserMockedStatic = mockStatic(IdentityConfigParser.class);
        identityConfigParserMock = mock(IdentityConfigParser.class);
    }

    @AfterClass
    public void tearDown() {

        actionValidator = null;
        identityConfigParserMockedStatic.close();
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
            Assert.assertEquals(e.getDescription(), header + " is invalid.");
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

    @DataProvider
    public Object[][] validateAllowedHeadersDataProvider() {

        List<String> allowedHeadersTest1 = new ArrayList<>();
        allowedHeadersTest1.add("test-header-1");
        allowedHeadersTest1.add("test-header-2");
        allowedHeadersTest1.add("test-header-3");

        List<String> allowedHeadersTest2 = new ArrayList<>();
        allowedHeadersTest2.add(" ");

        List<String> allowedHeadersTest3 = new ArrayList<>();
        allowedHeadersTest3.add("test-header-#");

        return new Object[][]{
                { allowedHeadersTest1,  null},
                { allowedHeadersTest2, "Allowed headers is empty." },
                { allowedHeadersTest3, ActionMgtConstants.ALLOWED_HEADERS_FIELD + " is invalid." }
        };
    }

    @Test(dataProvider = "validateAllowedHeadersDataProvider")
    public void testDoValidateAllowedHeaders(List<String> allowedHeadersInAction, String expectedError) {

        String propertyKey = ActionManagementConfig.ActionTypeConfig
                .PRE_ISSUE_ACCESS_TOKEN.getExcludedHeadersProperty();
        Map<String, Object> idnConfigMap = new HashMap<>();
        idnConfigMap.put(propertyKey, Collections.emptyList());

        identityConfigParserMockedStatic.when(IdentityConfigParser::getInstance).thenReturn(identityConfigParserMock);
        identityConfigParserMockedStatic.when(() -> IdentityConfigParser.getInstance().getConfiguration())
                .thenReturn(idnConfigMap);

        try {
            actionValidator.doValidateAllowedHeaders(allowedHeadersInAction);
        } catch (ActionMgtClientException e) {
            Assert.assertEquals(e.getMessage(), ERROR_INVALID_REQUEST);
            Assert.assertEquals(e.getDescription(), expectedError);
        }
    }

    @DataProvider
    public Object[][] validateAllowedHeadersWithExcludedHeadersDataProvider() {

        List<String> allowedHeadersTest1 = new ArrayList<>();
        allowedHeadersTest1.add("test-header-1");
        allowedHeadersTest1.add("test-header-2");
        allowedHeadersTest1.add("test-header-3");
        allowedHeadersTest1.add("test-header-4");

        List<String> excludedHeadersTest1 = new ArrayList<>();
        excludedHeadersTest1.add("test-header-2");
        excludedHeadersTest1.add("test-header-3");

        return new Object[][]{
                { allowedHeadersTest1,  excludedHeadersTest1 },
        };
    }

    @Test(dataProvider = "validateAllowedHeadersWithExcludedHeadersDataProvider")
    public void testDoValidateAllowedHeadersWithExcludedHeaders(List<String> allowedHeadersInAction,
                                                                List<String> excludedHeaders) {

        String propertyKey = ActionManagementConfig.ActionTypeConfig
                .PRE_ISSUE_ACCESS_TOKEN.getExcludedHeadersProperty();
        Map<String, Object> idnConfigMap = new HashMap<>();
        idnConfigMap.put(propertyKey, excludedHeaders);

        identityConfigParserMockedStatic.when(IdentityConfigParser::getInstance).thenReturn(identityConfigParserMock);
        identityConfigParserMockedStatic.when(() -> IdentityConfigParser.getInstance().getConfiguration())
                .thenReturn(idnConfigMap);

        try {
            actionValidator.doValidateAllowedHeaders(allowedHeadersInAction);
        } catch (ActionMgtClientException e) {
            Assert.assertEquals(e.getMessage(), ERROR_NOT_ALLOWED_HEADER);
        }
    }

    @DataProvider
    public Object[][] validateAllowedParamsDataProvider() {

        List<String> allowedParams1 = new ArrayList<>();
        allowedParams1.add("test-param-1");
        allowedParams1.add("test-param-2");
        allowedParams1.add("test-param-3");

        List<String> allowedParams2 = new ArrayList<>();
        allowedParams2.add("    ");

        List<String> allowedParams3 = new ArrayList<>();
        allowedParams3.add("test/param");

        return new Object[][]{
                { allowedParams1, null },
                { allowedParams2, "Allowed parameters is empty." },
                { allowedParams3, "Allowed parameters is invalid." }
        };
    }

    @Test(dataProvider = "validateAllowedParamsDataProvider")
    public void testDoValidateAllowedParams(List<String> allowedParams, String expectedError) {

        String propertyKey = ActionManagementConfig.ActionTypeConfig.PRE_ISSUE_ACCESS_TOKEN.getExcludedParamsProperty();
        Map<String, Object> idnConfigMap = new HashMap<>();
        idnConfigMap.put(propertyKey, Collections.emptyList());

        identityConfigParserMockedStatic.when(IdentityConfigParser::getInstance).thenReturn(identityConfigParserMock);
        identityConfigParserMockedStatic.when(() -> IdentityConfigParser.getInstance().getConfiguration())
                .thenReturn(idnConfigMap);

        try {
            actionValidator.doValidateAllowedParams(allowedParams);
        } catch (ActionMgtClientException e) {
            Assert.assertEquals(e.getMessage(), ERROR_INVALID_REQUEST);
            Assert.assertEquals(e.getDescription(), expectedError);
        }
    }

    @DataProvider
    public Object[][] validateAllowedParametersWithExcludedParamsDataProvider() {

        List<String> allowedParamsTest1 = new ArrayList<>();
        allowedParamsTest1.add("test-param-1");
        allowedParamsTest1.add("test-param-2");
        allowedParamsTest1.add("test-param-3");
        allowedParamsTest1.add("test-param-4");

        List<String> excludedParamsTest1 = new ArrayList<>();
        excludedParamsTest1.add("test-param-1");
        excludedParamsTest1.add("test-param-3");

        return new Object[][]{
                { allowedParamsTest1,  excludedParamsTest1 },
        };
    }

    @Test(dataProvider = "validateAllowedParametersWithExcludedParamsDataProvider")
    public void testDoValidateAllowedParametersWithExcludedParams(List<String> allowedParametersInAction,
                                                                List<String> excludedParameters) {

        String propertyKey = ActionManagementConfig.ActionTypeConfig
                .PRE_ISSUE_ACCESS_TOKEN.getExcludedParamsProperty();
        Map<String, Object> idnConfigMap = new HashMap<>();
        idnConfigMap.put(propertyKey, excludedParameters);

        identityConfigParserMockedStatic.when(IdentityConfigParser::getInstance).thenReturn(identityConfigParserMock);
        identityConfigParserMockedStatic.when(() -> IdentityConfigParser.getInstance().getConfiguration())
                .thenReturn(idnConfigMap);

        try {
            actionValidator.doValidateAllowedParams(allowedParametersInAction);
        } catch (ActionMgtClientException e) {
            Assert.assertEquals(e.getMessage(), ERROR_NOT_ALLOWED_PARAMETER);
        }
    }
}
