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
import org.wso2.carbon.identity.action.management.api.service.impl.DefaultActionValidator;
import org.wso2.carbon.identity.action.management.internal.util.ActionManagementConfig;
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
    private DefaultActionValidator actionValidator;
    private ActionManagementConfig actionManagementConfigMock;
    private IdentityConfigParser identityConfigParserMock;
    private MockedStatic<IdentityConfigParser> identityConfigParserMockedStatic;

    @BeforeClass
    public void setUp() {

        actionValidator = new DefaultActionValidator();

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
    public Object[][] invalidHeaderData() {

        return new String[][]{
                {"-test-header"},
                {".test-header"},
                {"test@header"},
                {"test_header"}
        };
    }

    @Test(dataProvider = "invalidHeaderData")
    public void testValidateHeaderWithInvalidData(String header) {

        try {
            actionValidator.validateHeader(header);
        } catch (ActionMgtClientException e) {
            Assert.assertEquals(e.getMessage(), ERROR_INVALID_REQUEST);
            Assert.assertEquals(e.getDescription(), header + " is invalid.");
        }
    }

    @DataProvider
    public Object[][] validHeaderData() {

        return new String[][]{
                {"test-header"},
                {"test.header"},
                {"testheader1"},
                {"testHeader"}
        };
    }

    @Test(dataProvider = "validHeaderData")
    public void testValidateHeaderWithValidData(String header) throws ActionMgtClientException {

        actionValidator.validateHeader(header);
    }

    @DataProvider
    public Object[][] invalidParameterData() {

        return new String[][]{
                {"test/param"},
                {":test-param"},
                {"parameter?"},
                {"test#param"}
        };
    }

    @Test(dataProvider = "invalidParameterData")
    public void testValidateParameterWithInvalidData(String parameter) {

        try {
            actionValidator.validateParameter(parameter);
        } catch (ActionMgtClientException e) {
            Assert.assertEquals(e.getMessage(), ERROR_INVALID_REQUEST);
            Assert.assertEquals(e.getDescription(), parameter + " is invalid.");
        }
    }

    @DataProvider
    public Object[][] validParameterData() {

        return new String[][]{
                {"test-param"},
                {"test-param-1"},
                {"testParam"},
                {"test param"}
        };
    }

    @Test(dataProvider = "validParameterData")
    public void testValidateParameterWithValidData(String parameter) throws ActionMgtClientException {

        actionValidator.validateParameter(parameter);
    }

    @DataProvider
    public Object[][] doValidateAllowedHeadersDataProvider() {

        List<String> allowedHeaderList1 = new ArrayList<>();
        allowedHeaderList1.add("test-header-1");
        allowedHeaderList1.add("test-header-2");
        allowedHeaderList1.add("test-header-3");

        List<String> allowedHeaderList2 = new ArrayList<>();
        allowedHeaderList2.add(" ");

        List<String> allowedHeaderList3 = new ArrayList<>();
        allowedHeaderList3.add("test-header-#");

        return new Object[][]{
                { allowedHeaderList1,  null},
                { allowedHeaderList2, "Allowed headers is empty." },
                { allowedHeaderList3, allowedHeaderList3.get(0) + " is invalid." }
        };
    }

    @Test(dataProvider = "doValidateAllowedHeadersDataProvider")
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
    public Object[][] doValidateAllowedHeadersWithExcludedHeadersDataProvider() {

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

    @Test(dataProvider = "doValidateAllowedHeadersWithExcludedHeadersDataProvider")
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
    public Object[][] doValidateAllowedParamsDataProvider() {

        List<String> allowedParamList1 = new ArrayList<>();
        allowedParamList1.add("test-param-1");
        allowedParamList1.add("test-param-2");
        allowedParamList1.add("test-param-3");

        List<String> allowedParamList2 = new ArrayList<>();
        allowedParamList2.add("    ");

        List<String> allowedParamList3 = new ArrayList<>();
        allowedParamList3.add("test/param");

        return new Object[][]{
                { allowedParamList1, null },
                { allowedParamList2, "Allowed parameters is empty." },
                { allowedParamList3, allowedParamList3.get(0) + " is invalid." }
        };
    }

    @Test(dataProvider = "doValidateAllowedParamsDataProvider")
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
    public Object[][] doValidateAllowedParametersWithExcludedParamsDataProvider() {

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

    @Test(dataProvider = "doValidateAllowedParametersWithExcludedParamsDataProvider")
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
