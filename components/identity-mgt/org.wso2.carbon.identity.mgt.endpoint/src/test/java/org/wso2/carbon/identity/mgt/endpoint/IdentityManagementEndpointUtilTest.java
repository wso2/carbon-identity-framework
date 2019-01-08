package org.wso2.carbon.identity.mgt.endpoint;

import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.testutil.Whitebox;

import static org.testng.Assert.assertEquals;

/**
 * This class tests the methods of IdentityManagementEndpointUtil class.
 */
public class IdentityManagementEndpointUtilTest extends PowerMockTestCase {

    @DataProvider(name = "getEndpointUrlTestData")
    public Object[][] getEndpointUrlTestData() {
        return new Object[][]{
                {"https://wso2.org:9443/services", "/api/identity/recovery/v0.9",
                        "https://wso2.org:9443/api/identity/recovery/v0.9"},
                {"https://wso2.org:9443/services", "api/identity/recovery/v0.9",
                        "https://wso2.org:9443/api/identity/recovery/v0.9"},
                {"https://wso2.services:9443/services", "/api/identity/recovery/v0.9",
                        "https://wso2.services:9443/api/identity/recovery/v0.9"},
                {"https://wso2.services:9443/services", "api/identity/recovery/v0.9",
                        "https://wso2.services:9443/api/identity/recovery/v0.9"},
                {"https://wso2.org:9443", "api/identity/recovery/v0.9",
                        "https://wso2.org:9443/api/identity/recovery/v0.9"},
        };
    }

    @Test(dataProvider = "getEndpointUrlTestData")
    public void testBuildEndpointUrl(String serviceContextUrl, String path, String expected) throws Exception {

        Whitebox.setInternalState(IdentityManagementServiceUtil.getInstance(), "serviceContextURL", serviceContextUrl);
        assertEquals(IdentityManagementEndpointUtil.buildEndpointUrl(path), expected);

    }
}
