/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

    @DataProvider(name = "getEndpointUrlTestDataForTenants")
    public Object[][] getEndpointUrlTestDataForTenants() {
        return new Object[][]{
                {"https://wso2.org:9443/services", "wso2.com", "/api/identity/recovery/v0.9",
                        "https://wso2.org:9443/t/wso2.com/api/identity/recovery/v0.9"},
                {"https://wso2.services:9443/services", "wso2.com", "/api/identity/recovery/v0.9",
                        "https://wso2.services:9443/t/wso2.com/api/identity/recovery/v0.9"},
                {"https://wso2.org:9443", "wso2.com", "/api/identity/recovery/v0.9",
                        "https://wso2.org:9443/t/wso2.com/api/identity/recovery/v0.9"},
        };
    }

    @Test(dataProvider = "getEndpointUrlTestData")
    public void testBuildEndpointUrl(String serviceContextUrl, String path, String expected) throws Exception {

        Whitebox.setInternalState(IdentityManagementServiceUtil.getInstance(), "serviceContextURL", serviceContextUrl);
        assertEquals(IdentityManagementEndpointUtil.buildEndpointUrl(path), expected);

    }

    @Test(dataProvider = "getEndpointUrlTestDataForTenants")
    public void testBuildEndpointUrlForTenants(String serviceContextUrl, String tenantName, String path,
                                               String expected) throws Exception {

        Whitebox.setInternalState(IdentityManagementServiceUtil.getInstance(), "serviceContextURL", serviceContextUrl);
        assertEquals(IdentityManagementEndpointUtil.buildEndpointUrl("t/" + tenantName + path), expected);

    }
}
