package org.wso2.carbon.identity.core.util;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import java.util.Map;

import static org.mockito.Mockito.mockStatic;

public class IdentityTenantUtilTest {

    MockedStatic<IdentityTenantUtil> identityTenantUtil;

    @BeforeMethod
    public void setUp() throws Exception {

        IdentityUtil.threadLocalProperties.remove();

        identityTenantUtil = mockStatic(IdentityTenantUtil.class);
    }

    @AfterMethod
    public void tearDown() throws Exception {

        IdentityUtil.threadLocalProperties.remove();

        identityTenantUtil.close();
    }

    @DataProvider
    public Object[][] getShouldUseTenantQualifiedURLsConfigData() {

        return new Object[][]{
                { true, true, true }, // tenant qualified URLs are enabled server wide (default behaviour)
                { false, true,  true  }, // tenant qualified URLs are disabled but is a system application
                { false, false, false },  // tenant qualified URLs are disabled and is a client application
        };
    }


    @Test(dataProvider = "getShouldUseTenantQualifiedURLsConfigData")
    public void testShouldUseTenantQualifiedURLs(boolean isTenantQualifiedURLsEnabled, boolean isSystemApplication,
                                                 boolean expectedResult) {

        identityTenantUtil.when(IdentityTenantUtil::isTenantQualifiedUrlsEnabled).
                thenReturn(isTenantQualifiedURLsEnabled);
        identityTenantUtil.when(IdentityTenantUtil::shouldUseTenantQualifiedURLs).thenCallRealMethod();

        Map<String, Object> threadLocalProperties = IdentityUtil.threadLocalProperties.get();
        threadLocalProperties.clear();
        threadLocalProperties.put(IdentityCoreConstants.IS_SYSTEM_APPLICATION, isSystemApplication);

        assertEquals(IdentityTenantUtil.shouldUseTenantQualifiedURLs(), expectedResult);
    }
}
