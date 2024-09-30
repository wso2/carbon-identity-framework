package org.wso2.carbon.identity.action.management.util;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.management.exception.ActionMgtClientException;

/**
 * Unit test class for ActionValidator class.
 */
public class ActionValidatorTest {

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
                {"Endpoint authentication URI", ""},
                {"Endpoint authentication type", "  "},
                {"Username", null},
                {"Password", ""},
                {"Access token", null},
                {"API key header name", null},
                {"API key value", null}
        };
    }

    @Test(expectedExceptions = ActionMgtClientException.class, dataProvider = "isBlankDataProvider")
    public void testIsBlank(String fieldName, String fieldValue) throws ActionMgtClientException {

        actionValidator.isBlank(fieldName, fieldValue);
    }

    @DataProvider
    public Object[][] isNotBlankDataProvider() {

        return new String[][]{
                {"Action name", "test-action"},
                {"Endpoint authentication URI", "https://testapi.com"},
                {"Endpoint authentication type", "BASIC"},
                {"Username", "testuser"},
                {"Password", "test@123"}
        };
    }

    @Test(dataProvider = "isNotBlankDataProvider")
    public void testIsNotBlank(String fieldName, String fieldValue) {

        try {
            actionValidator.isBlank(fieldName, fieldValue);
        } catch (ActionMgtClientException e) {
            Assert.fail("Exception should not be thrown for the field: " + fieldName + " and value: " + fieldValue);
        }
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

    @Test(expectedExceptions = ActionMgtClientException.class, dataProvider = "invalidActionNameDataProvider")
    public void testIsInvalidActionName(String actionName) throws ActionMgtClientException {

        actionValidator.isValidActionName(actionName);
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
    public void testIsValidActionName(String actionName) {

        try {
            actionValidator.isValidActionName(actionName);
        } catch (ActionMgtClientException e) {
            Assert.fail("Exception should not be thrown for the action name: " + actionName);
        }
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

    @Test(expectedExceptions = ActionMgtClientException.class, dataProvider = "invalidEndpointUriDataProvider")
    public void testIsInvalidEndpointUri(String endpointUri) throws ActionMgtClientException {

        actionValidator.isValidEndpointUri(endpointUri);
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
    public void testIsValidEndpointUriName(String endpointUri) {

        try {
            actionValidator.isValidEndpointUri(endpointUri);
        } catch (ActionMgtClientException e) {
            Assert.fail("Exception should not be thrown for the endpoint URI: " + endpointUri);
        }
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

    @Test(expectedExceptions = ActionMgtClientException.class, dataProvider = "invalidHeaderDataProvider")
    public void testIsInvalidHeader(String header) throws ActionMgtClientException {

        actionValidator.isValidHeader(header);
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
    public void testIsValidHeader(String header) {

        try {
            actionValidator.isValidHeader(header);
        } catch (ActionMgtClientException e) {
            Assert.fail("Exception should not be thrown for the header: " + header);
        }
    }
}
