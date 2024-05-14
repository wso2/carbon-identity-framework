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

package org.wso2.carbon.identity.mgt.endpoint.util;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.core.ServiceURL;
import org.wso2.carbon.identity.core.ServiceURLBuilder;
import org.wso2.carbon.identity.core.URLBuilderException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.testutil.Whitebox;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.testng.Assert.assertEquals;

/**
 * This class tests the methods of IdentityManagementEndpointUtil class.
 */
@Listeners(MockitoTestNGListener.class)
public class IdentityManagementEndpointUtilTest {

    @Mock
    ServiceURL serviceURL;

    @Mock
    private ServiceURLBuilder serviceURLBuilder;

    private static final String SAMPLE_URL = "https://wso2.org:9443";
    private static final String SAMPLE_TENANTED_URL = "https://wso2.org:9443/t/test.com";

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
    public void testEncodedCallbackUrl(String callbackUrl, String encodedCallbackUrl) throws MalformedURLException {

        assertEquals(IdentityManagementEndpointUtil.encodeURL(callbackUrl), encodedCallbackUrl,
                "callbackUrl is not properly encoded");
    }

    @Test(dataProvider = "dataForQueryParamEncoding")
    public void testEncodedQueryMap (String query, Map<String,String> encodedQueryMap) {

        assertEquals(IdentityManagementEndpointUtil.getEncodedQueryParamMap(query), encodedQueryMap,
                "query params are not properly encoded");
    }

    @DataProvider(name = "getBasePathTestData")
    public Object[][] getBasePathTestData() {

        return new Object[][] {
                // isTenantQualifiedUrlsEnabled
                // contextUrl
                // tenantDomain
                // context
                // expected value
                { true,
                  "https://foo.com",
                  "test.com",
                  IdentityManagementEndpointConstants.UserInfoRecovery.RECOVERY_API_RELATIVE_PATH,
                  "https://foo.com/t/test.com/api/identity/recovery/v0.9"
                },
                { false,
                  "https://foo.com",
                  "test.com",
                  IdentityManagementEndpointConstants.UserInfoRecovery.RECOVERY_API_RELATIVE_PATH,
                  "https://foo.com/t/test.com/api/identity/recovery/v0.9"
                },
                { false,
                  null,
                  "test.com",
                  IdentityManagementEndpointConstants.UserInfoRecovery.RECOVERY_API_RELATIVE_PATH,
                  "https://wso2.org:9443/t/test.com/api/identity/recovery/v0.9"
                },
                { false,
                  "https://foo.com",
                  null,
                  IdentityManagementEndpointConstants.UserInfoRecovery.RECOVERY_API_RELATIVE_PATH,
                  "https://foo.com/api/identity/recovery/v0.9"
                },
                { false,
                  null,
                  null,
                  IdentityManagementEndpointConstants.UserInfoRecovery.RECOVERY_API_RELATIVE_PATH,
                  "https://wso2.org:9443/api/identity/recovery/v0.9"
                },
                { false,
                  null,
                  MultitenantConstants.SUPER_TENANT_DOMAIN_NAME,
                  IdentityManagementEndpointConstants.UserInfoRecovery.RECOVERY_API_RELATIVE_PATH,
                  "https://wso2.org:9443/api/identity/recovery/v0.9"
                }
        };
    }

    @Test(dataProvider = "getBasePathTestData")
    public void testGetBasePath(boolean isTenantQualifiedUrlsEnabled, String contextUrl, String tenantDomain,
            String context, String expected) throws Exception {

        try (MockedStatic<IdentityTenantUtil> identityTenantUtil = mockStatic(IdentityTenantUtil.class);
             MockedStatic<ServiceURLBuilder> serviceURLBuilder = mockStatic(ServiceURLBuilder.class)) {
            prepareGetBasePathTest(isTenantQualifiedUrlsEnabled, contextUrl, context, identityTenantUtil,
                    serviceURLBuilder);
            assertEquals(IdentityManagementEndpointUtil.getBasePath(tenantDomain, context), expected);
        }
    }

    @DataProvider(name = "getBasePathTestData2")
    public Object[][] getBasePathTestData2() {

        return new Object[][] {
                // isTenantQualifiedUrlsEnabled
                // contextUrl
                // tenantDomain
                // isEndpointTenantAware
                // context
                // expected value
                { true,
                  "https://foo.com",
                  "test.com",
                  true,
                  IdentityManagementEndpointConstants.UserInfoRecovery.RECOVERY_API_RELATIVE_PATH,
                  "https://foo.com/t/test.com/api/identity/recovery/v0.9"
                },
                { true,
                  "https://foo.com",
                  "test.com",
                  false,
                  IdentityManagementEndpointConstants.UserInfoRecovery.RECOVERY_API_RELATIVE_PATH,
                  "https://foo.com/api/identity/recovery/v0.9"
                },
                { false,
                  "https://foo.com",
                  "test.com",
                  true,
                  IdentityManagementEndpointConstants.UserInfoRecovery.RECOVERY_API_RELATIVE_PATH,
                  "https://foo.com/t/test.com/api/identity/recovery/v0.9"
                },
                { false,
                  "https://foo.com",
                  "test.com",
                  false,
                  IdentityManagementEndpointConstants.UserInfoRecovery.RECOVERY_API_RELATIVE_PATH,
                  "https://foo.com/api/identity/recovery/v0.9"
                },
                { false,
                  null,
                  "test.com",
                  true,
                  IdentityManagementEndpointConstants.UserInfoRecovery.RECOVERY_API_RELATIVE_PATH,
                  "https://wso2.org:9443/t/test.com/api/identity/recovery/v0.9"
                },
                { false,
                  null,
                  "test.com",
                  false,
                  IdentityManagementEndpointConstants.UserInfoRecovery.RECOVERY_API_RELATIVE_PATH,
                  "https://wso2.org:9443/api/identity/recovery/v0.9"
                },
                { false,
                  "https://foo.com",
                  null,
                  true,
                  IdentityManagementEndpointConstants.UserInfoRecovery.RECOVERY_API_RELATIVE_PATH,
                  "https://foo.com/api/identity/recovery/v0.9"
                },
                { false,
                  "https://foo.com",
                  null,
                  false,
                  IdentityManagementEndpointConstants.UserInfoRecovery.RECOVERY_API_RELATIVE_PATH,
                  "https://foo.com/api/identity/recovery/v0.9"
                },
                { false,
                  null,
                  null,
                  true,
                  IdentityManagementEndpointConstants.UserInfoRecovery.RECOVERY_API_RELATIVE_PATH,
                  "https://wso2.org:9443/api/identity/recovery/v0.9"
                },
                { false,
                  null,
                  null,
                  false,
                  IdentityManagementEndpointConstants.UserInfoRecovery.RECOVERY_API_RELATIVE_PATH,
                  "https://wso2.org:9443/api/identity/recovery/v0.9"
                },
                { false,
                  null,
                  MultitenantConstants.SUPER_TENANT_DOMAIN_NAME,
                  true,
                  IdentityManagementEndpointConstants.UserInfoRecovery.RECOVERY_API_RELATIVE_PATH,
                  "https://wso2.org:9443/api/identity/recovery/v0.9"
                },
                { false,
                  null,
                  MultitenantConstants.SUPER_TENANT_DOMAIN_NAME,
                  false,
                  IdentityManagementEndpointConstants.UserInfoRecovery.RECOVERY_API_RELATIVE_PATH,
                  "https://wso2.org:9443/api/identity/recovery/v0.9"
                }
        };
    }

    @Test(dataProvider = "getBasePathTestData2")
    public void testGetBasePath2(boolean isTenantQualifiedUrlsEnabled, String contextUrl, String tenantDomain,
            boolean isEndpointTenantAware, String context, String expected) throws Exception {

        try (MockedStatic<IdentityTenantUtil> identityTenantUtil = mockStatic(IdentityTenantUtil.class);
             MockedStatic<ServiceURLBuilder> serviceURLBuilder = mockStatic(ServiceURLBuilder.class)) {
            prepareGetBasePathTest(isTenantQualifiedUrlsEnabled, contextUrl, context, identityTenantUtil,
                    serviceURLBuilder);
            assertEquals(IdentityManagementEndpointUtil.getBasePath(tenantDomain, context, isEndpointTenantAware),
                    expected);
        }
    }

    private void prepareGetBasePathTest(boolean isTenantQualifiedUrlsEnabled, String contextUrl, String context,
                                        MockedStatic<IdentityTenantUtil> identityTenantUtil,
                                        MockedStatic<ServiceURLBuilder> serviceURLBuilder) throws Exception {


        prepareServiceURLBuilder(serviceURLBuilder);
        identityTenantUtil.when(
                IdentityTenantUtil::isTenantQualifiedUrlsEnabled).thenReturn(isTenantQualifiedUrlsEnabled);
        if (isTenantQualifiedUrlsEnabled) {
            lenient().when(serviceURL.getAbsoluteInternalURL()).thenReturn(SAMPLE_TENANTED_URL + context);
        } else {
            lenient().when(serviceURL.getAbsoluteInternalURL()).thenReturn(SAMPLE_URL);
        }
        Whitebox.setInternalState(IdentityManagementServiceUtil.getInstance(), "contextURL", contextUrl);
    }

    private void prepareServiceURLBuilder(MockedStatic<ServiceURLBuilder> serviceURLBuilder)
            throws URLBuilderException {

        serviceURLBuilder.when(ServiceURLBuilder::create).thenReturn(this.serviceURLBuilder);
        lenient().when(this.serviceURLBuilder.addPath(any())).thenReturn(this.serviceURLBuilder);
        lenient().when(this.serviceURLBuilder.addFragmentParameter(any(), any())).thenReturn(this.serviceURLBuilder);
        lenient().when(this.serviceURLBuilder.addParameter(any(), any())).thenReturn(this.serviceURLBuilder);
        lenient().when(this.serviceURLBuilder.build()).thenReturn(serviceURL);
    }
}
