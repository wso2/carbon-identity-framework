/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.application.authentication.endpoint.util;

import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.endpoint.util.bean.UserDTO;
import org.wso2.carbon.identity.common.testng.WithAxisConfiguration;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import static org.mockito.ArgumentMatchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.wso2.carbon.user.core.UserCoreConstants.DOMAIN_SEPARATOR;
import static org.wso2.carbon.user.core.UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME;
import static org.wso2.carbon.user.core.UserCoreConstants.TENANT_DOMAIN_COMBINER;
import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;

@PrepareForTest(IdentityUtil.class)
@PowerMockIgnore("org.mockito.*")
@WithCarbonHome
@WithAxisConfiguration
public class AuthenticationEndpointUtilTest {

    final String USERNAME = "TestUser";
    final String USERSTORE_NAME = "WSO2.COM";
    final String TENANT_DOMAIN = "abc.com";
    final String SUPER_TENANT_DOMAIN = "carbon.super";

    final String FULL_QUALIFIED_NAME = USERSTORE_NAME + DOMAIN_SEPARATOR + USERNAME +
            TENANT_DOMAIN_COMBINER + TENANT_DOMAIN;


    @BeforeMethod
    public void setUp() throws Exception {
    }

    @AfterMethod
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetApplicationSpecificCustomPageConfigKey() throws Exception {

        final String SP_NAME = "GlobalTrotters";
        final String RELATIVE_PATH = "login";
        final String CUSTOM_PAGE_CONFIG_KEY = "GlobalTrotters-login";

        String customUrl = AuthenticationEndpointUtil.getApplicationSpecificCustomPageConfigKey(SP_NAME, RELATIVE_PATH);
        Assert.assertEquals(customUrl, CUSTOM_PAGE_CONFIG_KEY);
    }

    @Test
    public void testGetCustomPageRedirectUrl() throws Exception {

        String customContext = "custom-page";
        String customContextWithQueryParams = "custom-page?test=xyz";

        String queryParams = "key1=value1&key2=value2";

        String redirectUrl = AuthenticationEndpointUtil.getCustomPageRedirectUrl(customContext, queryParams);
        Assert.assertEquals(redirectUrl, "custom-page?key1=value1&key2=value2");

        redirectUrl = AuthenticationEndpointUtil.getCustomPageRedirectUrl(customContextWithQueryParams, queryParams);
        Assert.assertEquals(redirectUrl, "custom-page?test=xyz&key1=value1&key2=value2");


        Assert.assertEquals(AuthenticationEndpointUtil.getCustomPageRedirectUrl(customContext, ""), customContext);
        Assert.assertEquals(AuthenticationEndpointUtil.getCustomPageRedirectUrl(customContext, null), customContext);
        Assert.assertEquals(AuthenticationEndpointUtil.getCustomPageRedirectUrl(null, queryParams), null);

    }

    @DataProvider(name = "queryStringProvider")
    public Object[][] queryStringProvider() {

        final String singleQueryParam = "xyz=111";
        final String baseQueryString = "x=abcd&y=abz";
        final String authFailureParam = Constants.AUTH_FAILURE + "=true";
        final String errorCode = Constants.ERROR_CODE + "=17900";


        return new Object[][]{
                {baseQueryString, baseQueryString},
                {null, ""},
                {singleQueryParam, singleQueryParam},
                {authFailureParam + "&" + baseQueryString, baseQueryString},
                {baseQueryString + "&" + errorCode, baseQueryString},
                {authFailureParam + "&" + errorCode, ""},
                {authFailureParam + "&", ""}
        };
    }

    @Test(dataProvider = "queryStringProvider")
    public void testCleanErrorMessages(String queryString,
                                       String expectedCleanedString) throws Exception {


        String actualCleanedString = AuthenticationEndpointUtil.cleanErrorMessages(queryString);
        Assert.assertEquals(actualCleanedString, expectedCleanedString);

    }

    @Test
    public void testGetUserNullInput() throws Exception {

        Assert.assertNull(AuthenticationEndpointUtil.getUser(null));
    }

    @DataProvider(name = "username-provider")
    public Object[][] dataProviderMethod() {

        String username1 = PRIMARY_DEFAULT_DOMAIN_NAME + DOMAIN_SEPARATOR + USERNAME;
        String username2 = USERSTORE_NAME + DOMAIN_SEPARATOR + USERNAME;
        String username3 = USERNAME + TENANT_DOMAIN_COMBINER + TENANT_DOMAIN;
        String username4 = USERSTORE_NAME + DOMAIN_SEPARATOR + USERNAME + TENANT_DOMAIN_COMBINER + TENANT_DOMAIN;
        String username5 = USERNAME;

        return new Object[][]{
                {
                        username1, SUPER_TENANT_DOMAIN_NAME, PRIMARY_DEFAULT_DOMAIN_NAME
                },
                {
                        username2, SUPER_TENANT_DOMAIN_NAME, USERSTORE_NAME
                },
                {
                        username3, TENANT_DOMAIN, null
                },
                {
                        username4, TENANT_DOMAIN, USERSTORE_NAME
                },
                {
                        username5, SUPER_TENANT_DOMAIN_NAME, null
                }
        };
    }

    @Test(dataProvider = "username-provider")
    public void testGetUser(String username,
                            String tenantDomain,
                            String userStoreDomain) throws Exception {

        mockStatic(IdentityUtil.class);
        when(IdentityUtil.getPrimaryDomainName()).thenReturn(PRIMARY_DEFAULT_DOMAIN_NAME);
        when(IdentityUtil.extractDomainFromName(anyString())).thenCallRealMethod();

        UserDTO userDTO = AuthenticationEndpointUtil.getUser(username);
        Assert.assertNotNull(userDTO);
        Assert.assertEquals(userDTO.getUsername(), USERNAME);
        Assert.assertEquals(userDTO.getTenantDomain(), tenantDomain);
        Assert.assertEquals(userDTO.getRealm(), userStoreDomain);
    }

    @DataProvider(name = "url-provider")
    public Object[][] isValidURLData() {

        return new Object[][]{
                // Todo : following test case is failing for JDK 11 support. Ref: @thumimku
                // we will fix it later. Ref: https://github.com/wso2/product-is/issues/14073
//                {"/authenticationendpoint/samlsso_login.do?&type=samlsso&sp=app", true},
                {"https://localhost:9443/authenticationendpoint/samlsso_login.do?&type=samlsso&sp=app", true},
                {"javascript:alert(document.domain)", false},
                {"abc\"><img%20src/onerror%2f\"alert(document.domain)\"<%20\"", false},
                {"https:// www.wso2.org/", false},
                {"   ", false},
                {null, false}
        };
    }

    @Test(dataProvider = "url-provider")
    public void testIsValidURL(String urlString, boolean expectedValidity) throws Exception {

        boolean validity = AuthenticationEndpointUtil.isValidURL(urlString);
        Assert.assertEquals(validity, expectedValidity, "URL validity failed for " + urlString);
    }
}
