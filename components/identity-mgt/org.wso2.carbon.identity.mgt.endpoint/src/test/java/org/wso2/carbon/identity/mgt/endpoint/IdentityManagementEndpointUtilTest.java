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

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

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

    @DataProvider(name = "dataForUrlEncoding")
    public Object[][] dataForUrlEncoding() {

        return new Object[][]{

                {"https://localhost:9443/accountrecoveryendpoint/recoverusername.do?" +
                        "callback=https://localhost:9443/authenticationendpoint_login.do&sp//name=TestSP123",
                        "https://localhost:9443/accountrecoveryendpoint/recoverusername.do?sp%2F%2Fname=TestSP123&" +
                                "callback=https%3A%2F%2Flocalhost%3A9443%2Fauthenticationendpoint_login.do"},
                {"http://example.com/path/to/page?name=ferret&color=purple",
                        "http://example.com/path/to/page?color=purple&name=ferret"},
                {"http://example.com/path/to/page?name=ferret",
                        "http://example.com/path/to/page?name=ferret"},
                {"https://www.facebook.com/Learn-the-Net-330002341216/",
                        "https://www.facebook.com/Learn-the-Net-330002341216/"},
        };
    }

    @DataProvider(name = "dataForQueryParamEncoding")
    public Object[][] dataForQueryParamEncoding() {

        Map<String,String> encodedQueryMap1 = new HashMap<>();
        Map<String,String> encodedQueryMap2 = new HashMap<>();
        Map<String,String> encodedQueryMap3 = new HashMap<>();

        encodedQueryMap1.put("sp%2F%2Fname","TestSP123");
        encodedQueryMap1.put("callback","https%3A%2F%2Flocalhost%3A9443%2Fauthenticationendpoint_login.do");

        encodedQueryMap2.put("color","purple");
        encodedQueryMap2.put("name","ferret");

        return new Object[][]{

                {"callback=https://localhost:9443/authenticationendpoint_login.do&sp//name=TestSP123", encodedQueryMap1},
                {"name=ferret&color=purple", encodedQueryMap2},
                {"", encodedQueryMap3}
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

    @Test(dataProvider = "dataForUrlEncoding")
    public void testEncodedCallbackUrl(String callbackUrl, String encodedCallbackUrl) throws URISyntaxException {

        assertEquals(IdentityManagementEndpointUtil.getURLEncodedCallback(callbackUrl), encodedCallbackUrl,
                "callbackUrl is not properly encoded");
    }

    @Test(dataProvider = "dataForQueryParamEncoding")
    public void testEncodedQueryMap (String query, Map<String,String> encodedQueryMap) {

        assertEquals(IdentityManagementEndpointUtil.getEncodedQueryParamMap(query), encodedQueryMap,
                "query params are not properly encoded");
    }
}
