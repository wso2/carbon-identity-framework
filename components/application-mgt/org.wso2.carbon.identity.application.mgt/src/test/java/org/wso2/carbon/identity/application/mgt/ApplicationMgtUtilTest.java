package org.wso2.carbon.identity.application.mgt;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@PrepareForTest(IdentityUtil.class)
/*
  Unit tests for ApplicationMgtUtil
 */
public class ApplicationMgtUtilTest extends PowerMockTestCase {

    @DataProvider(name = "getAppNamesForDefaultRegex")
    public Object[][] getAppNamesForDefaultRegex() {

        return new Object[][]{
                {"MyAppName99", true},
                {"My App Name1", true},
                {"My-App-Name2", true},
                {"My.App.Name3", true},
                {"My_App_Name4", true},
                {"My_App_Name5", true},
                {"My_App.Name-1234567890", true},

                {" My_App_Name", false},
                {"My_App_Name ", false},
                {" My_App_Name ", false},
                {"My_App.Name@carbon.super", false},
        };
    }

    @Test(dataProvider = "getAppNamesForDefaultRegex")
    public void testIsRegexValidated(String appName, boolean isValidName) {

        // Default app validation regex should allow names with alphanumeric, dot, space, underscore and hyphens.
        // Should not allow leading or trailing spaces.
        Assert.assertEquals(ApplicationMgtUtil.isRegexValidated(appName), isValidName);
    }

    @DataProvider(name = "getAppNamesForCustomRegex")
    public Object[][] getAppNamesForCustomRegex() {

        return new Object[][]{
                {"MyAppName99", true},
                // These two names are valid according to our default regex. Our custom regex will make these invalid.
                {"My AppName 99", false},
                {"MyAppName99-", false},
        };
    }

    @Test(dataProvider = "getAppNamesForCustomRegex")
    public void testSpNameValidationWithCustomRegex(String appName, boolean isValidName) {

        final String CUSTOM_REGEX = "^[a-zA-Z0-9]+";

        mockStatic(IdentityUtil.class);
        when(IdentityUtil.getProperty("ServiceProviders.SPNameRegex")).thenReturn(CUSTOM_REGEX);

        Assert.assertEquals(ApplicationMgtUtil.isRegexValidated(appName), isValidName);
    }
}