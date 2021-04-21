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
package org.wso2.carbon.identity.application.authentication.framework.util;

import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.PostAuthnMissingClaimHandler;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceComponent;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.testutil.powermock.PowerMockIdentityBaseTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.io.UnsupportedEncodingException;

import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

@WithCarbonHome
@PrepareForTest({ConfigurationFacade.class, FrameworkServiceComponent.class})
public class FrameworkUtilsTest extends PowerMockIdentityBaseTest {

    @Mock
    ConfigurationFacade mockedConfigurationFacade;


    private PostAuthnMissingClaimHandler testPostAuthenticationHandler;

    private List<ApplicationAuthenticator> authenticators;

    @BeforeTest
    public void setUp() {
        testPostAuthenticationHandler = new PostAuthnMissingClaimHandler();
    }

    private void setMockedConfigurationFacade() {
        mockStatic(ConfigurationFacade.class);
        when(ConfigurationFacade.getInstance()).thenReturn(mockedConfigurationFacade);
    }

    private void mockFrameworkServiceComponent() {
        mockStatic(FrameworkServiceComponent.class);
        when(FrameworkServiceComponent.getAuthenticators()).thenReturn(authenticators);
    }

    private ApplicationAuthenticator initAuthenticators(String name) {
        ApplicationAuthenticator applicationAuthenticator = mock(ApplicationAuthenticator.class);
        when(applicationAuthenticator.getName()).thenReturn(name);

        return applicationAuthenticator;
    }

    @Test
    public void testGetAppAuthenticatorByNameValidAuthenticator(){
        authenticators = new ArrayList<>();
        String name = "Authenticator1";
        ApplicationAuthenticator authenticator1 = initAuthenticators(name);
        authenticators.add(authenticator1);

        mockFrameworkServiceComponent();

        ApplicationAuthenticator out = FrameworkUtils.getAppAuthenticatorByName(name);
        assertEquals(out, authenticator1);
    }

    @Test
    public void testGetAppAuthenticatorByNameNonExistAuthenticator(){
        ApplicationAuthenticator out = FrameworkUtils.getAppAuthenticatorByName("nonExistingAuthenticator");
        assertNull(out);
    }

    @DataProvider(name = "providePostAuthenticationData")
    public Object[][] provideInvalidData() {

        Map<String, Object> map1 = new HashMap<>();
        map1.put(FrameworkConstants.Config.QNAME_EXT_POST_AUTHENTICATION_HANDLER, testPostAuthenticationHandler);

        Map<String, Object> map2 = new HashMap<>();
        map2.put(FrameworkConstants.Config.QNAME_EXT_POST_AUTHENTICATION_HANDLER, new Object());

        return new Object[][]{
                {map1, true},
                {map2, false}
        };
    }

    @Test(dataProvider = "provideURLParamData")
    public void testAppendQueryParamsToUrl(String url, Map<String, String> queryParamMap, String expectedOutput)
            throws Exception {

        String out = FrameworkUtils.appendQueryParamsToUrl(url, queryParamMap);
        assertEquals(out, expectedOutput);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            dataProvider = "provideQueryParamData")
    public void testAppendQueryParamsToUrlEmptyUrl(Map<String, String> queryParamMap)
            throws Exception {
        FrameworkUtils.appendQueryParamsToUrl(null,queryParamMap );
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            dataProvider = "provideQueryParamData")
    public void testBuildURLWithQueryParamsEmptyUrl(Map<String, String> queryParamMap)
            throws Exception {
        FrameworkUtils.appendQueryParamsToUrl(null,queryParamMap);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAppendQueryParamsToUrlEmptyQueryParams() throws Exception {
        FrameworkUtils.appendQueryParamsToUrl("https://www.example.com", null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testBuildURLWithQueryParamsEmptyQueryParams() throws Exception {
        FrameworkUtils.appendQueryParamsToUrl("https://www.example.com", null);
    }

    @Test(dataProvider = "provideURLParamData")
    public void testBuildURLWithQueryParams(String url, Map<String, String> queryParamMap, String expectedOutput)
            throws UnsupportedEncodingException {

        String out = FrameworkUtils.buildURLWithQueryParams(url, queryParamMap);
        assertEquals(out, expectedOutput);
    }

    @DataProvider(name = "provideURLParamData")
    public Object[][] provideURLParamData() {

        String url1 = "https://www.example.com";
        String url2 = "https://www.example.com?x=asd";

        Map<String, String> queryParamMap1 = new HashMap<>();
        queryParamMap1.put("a", "wer");
        queryParamMap1.put("b", "dfg");
        String queryParamString = "a=wer&b=dfg";

        Map<String, String> queryParamMap2 = new HashMap<>();
        queryParamMap2.put("a", "http://wso2.com");

        Map<String, String> queryParamMap3 = new HashMap<>();

        String expectedOutput1 = url1 + "?" + queryParamString;
        String expectedOutput2 = url2 + "&" + queryParamString;
        String expectedOutput3 = url1 + "?a=http%3A%2F%2Fwso2.com";

        return new Object[][]{
                {url1, queryParamMap1, expectedOutput1},
                {url2, queryParamMap1, expectedOutput2},
                {url1, queryParamMap2, expectedOutput3},
                {url1, queryParamMap3, url1},
                {url2, queryParamMap3, url2}
        };
    }

    @DataProvider(name = "provideQueryParamData")
    public Object[][] provideQueryParamData() {

        Map<String, String> queryParamMap1 = new HashMap<>();
        queryParamMap1.put("a", "wer");
        queryParamMap1.put("b", "dfg");

        return new Object[][]{
                {queryParamMap1}
        };
    }
}
