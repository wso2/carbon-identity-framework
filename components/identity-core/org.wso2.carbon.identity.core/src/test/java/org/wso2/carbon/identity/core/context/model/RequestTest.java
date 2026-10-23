/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.core.context.model;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link Request}.
 */
public class RequestTest {

    private static final String EXCLUDED_QUERY_PARAMS_CONFIG = "IdentityContext.Request.ExcludedQueryParams.Param";
    private static final String MAX_QUERY_PARAMS_CONFIG = "IdentityContext.Request.MaxQueryParams";
    private static final int DEFAULT_MAX_QUERY_PARAMS = 50;

    private MockedStatic<IdentityUtil> identityUtil;

    @BeforeMethod
    public void setUp() throws Exception {

        clearCachedConfigs();
        identityUtil = mockStatic(IdentityUtil.class);
        mockExcludedQueryParams(Arrays.asList("my_secret", " custom_key ", ""));
    }

    @AfterMethod
    public void tearDown() throws Exception {

        identityUtil.close();
        clearCachedConfigs();
    }

    @Test
    public void testResolveQueryParametersDoesNotReadRequestBody() throws IOException {

        HttpServletRequest httpRequest = mockHttpRequest("a=1&b=2");

        Request request = new Request.Builder().fromHttpRequest(httpRequest).build();

        assertEquals(request.getQueryParameters().size(), 2);
        verify(httpRequest, never()).getParameter(anyString());
        verify(httpRequest, never()).getParameterMap();
        verify(httpRequest, never()).getParameterNames();
        verify(httpRequest, never()).getParameterValues(anyString());
        verify(httpRequest, never()).getInputStream();
        verify(httpRequest, never()).getReader();
    }

    @DataProvider
    public Object[][] queryStringDataProvider() {

        return new Object[][]{
                {null, Collections.emptyMap()},
                {"", Collections.emptyMap()},
                {"a=1", Collections.singletonMap("a", Collections.singletonList("1"))},
                {"a=1&a=2", Collections.singletonMap("a", Arrays.asList("1", "2"))},
                {"a=&b", Map.of("a", Collections.singletonList(""), "b", Collections.singletonList(""))},
                {"&&a=1&", Collections.singletonMap("a", Collections.singletonList("1"))},
                {"=1&a=1", Collections.singletonMap("a", Collections.singletonList("1"))},
                {"a=x%3Dy%26z&b=hello+world", Map.of("a", Collections.singletonList("x=y&z"),
                        "b", Collections.singletonList("hello world"))},
                {"na%6De=%E0%B6%B4", Collections.singletonMap("name", Collections.singletonList("ප"))},
                {"a=%zz", Collections.singletonMap("a", Collections.singletonList("%zz"))},
        };
    }

    @Test(dataProvider = "queryStringDataProvider")
    public void testResolveQueryParameters(String queryString, Map<String, List<String>> expected) {

        Request request = new Request.Builder().fromHttpRequest(mockHttpRequest(queryString)).build();

        assertEquals(toMap(request.getQueryParameters()), expected);
    }

    @Test
    public void testDefaultExcludedQueryParametersAreNotCapturedWithoutConfiguration() {

        mockExcludedQueryParams(Collections.emptyList());
        HttpServletRequest httpRequest = mockHttpRequest("username=admin&password=pw&PASSWORD=pw&pass%77ord=pw" +
                "&client_secret=s&code=c&access_token=at&refresh_token=rt&id_token_hint=h&token=t&SAMLResponse=r" +
                "&samlresponse=r&otp=1&my_secret=v");

        Request request = new Request.Builder().fromHttpRequest(httpRequest).build();

        assertEquals(toMap(request.getQueryParameters()), Map.of(
                "username", Collections.singletonList("admin"),
                "my_secret", Collections.singletonList("v")));
    }

    @DataProvider
    public Object[][] sessionKeyQueryParamsDataProvider() {

        return new Object[][]{
                {"sessionDataKey"},
                {"SESSIONDATAKEY"},
                {"sessionDataKeyConsent"},
                {"sessiondatakeyconsent"}
        };
    }

    @Test(dataProvider = "sessionKeyQueryParamsDataProvider")
    public void testAuthenticationSessionKeysAreNotCaptured(String sessionKeyParam) {

        mockExcludedQueryParams(Collections.emptyList());
        HttpServletRequest httpRequest = mockHttpRequest("username=admin&" + sessionKeyParam + "=key");

        Request request = new Request.Builder().fromHttpRequest(httpRequest).build();

        assertEquals(toMap(request.getQueryParameters()),
                Collections.singletonMap("username", Collections.singletonList("admin")));
    }

    @Test
    public void testConfiguredExcludedQueryParametersAreAddedToDefaults() {

        HttpServletRequest httpRequest =
                mockHttpRequest("username=admin&my_secret=v&CUSTOM_KEY=k&password=pw&client_secret=s");

        Request request = new Request.Builder().fromHttpRequest(httpRequest).build();

        assertEquals(toMap(request.getQueryParameters()),
                Collections.singletonMap("username", Collections.singletonList("admin")));
    }

    @Test
    public void testQueryParametersAreCappedAtConfiguredMax() {

        mockMaxQueryParams("3");
        // Excluded parameters do not count towards the limit. Repeated values count individually.
        HttpServletRequest httpRequest = mockHttpRequest("password=pw&a=1&a=2&b=3&c=4&d=5");

        Request request = new Request.Builder().fromHttpRequest(httpRequest).build();

        assertEquals(toMap(request.getQueryParameters()),
                Map.of("a", Arrays.asList("1", "2"), "b", Collections.singletonList("3")));
    }

    @DataProvider
    public Object[][] invalidMaxQueryParamsDataProvider() {

        return new Object[][]{{null}, {""}, {"abc"}, {"-1"}, {"99999999999"}};
    }

    @Test(dataProvider = "invalidMaxQueryParamsDataProvider")
    public void testQueryParametersAreCappedAtDefaultMax(String configuredMax) {

        mockMaxQueryParams(configuredMax);
        String queryString = IntStream.range(0, DEFAULT_MAX_QUERY_PARAMS + 50)
                .mapToObj(i -> "p" + i + "=" + i).collect(Collectors.joining("&"));

        Request request = new Request.Builder().fromHttpRequest(mockHttpRequest(queryString)).build();

        assertEquals(request.getQueryParameters().size(),
                DEFAULT_MAX_QUERY_PARAMS);
        assertEquals(request.getQueryParameters().get(0).getName(), "p0");
    }

    @Test
    public void testQueryParametersAreNotCapturedWhenMaxIsZero() {

        mockMaxQueryParams("0");

        Request request = new Request.Builder().fromHttpRequest(mockHttpRequest("a=1&b=2")).build();

        assertTrue(request.getQueryParameters().isEmpty());
    }

    @Test
    public void testConfigurationsAreResolvedOnce() {

        new Request.Builder().fromHttpRequest(mockHttpRequest("a=1")).build();
        new Request.Builder().fromHttpRequest(mockHttpRequest("b=2")).build();

        identityUtil.verify(() -> IdentityUtil.getPropertyAsList(
                EXCLUDED_QUERY_PARAMS_CONFIG), times(1));
        identityUtil.verify(() -> IdentityUtil.getProperty(
                MAX_QUERY_PARAMS_CONFIG), times(1));
    }

    @Test
    public void testParameterValueIsACopy() {

        List<String> values = new ArrayList<>(Collections.singletonList("1"));
        Parameter parameter = new Parameter("a", values);
        values.add("2");

        assertEquals(parameter.getValue(), Collections.singletonList("1"));
        assertTrue(new Parameter("a", null).getValue().isEmpty());
    }

    private void clearCachedConfigs() throws Exception {

        // Configurations are cached in static fields of the builder. Reset them so that each test resolves its own.
        for (String fieldName : Arrays.asList("excludedQueryParams", "maxQueryParams")) {
            Field field = Request.Builder.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(null, null);
        }
    }

    private void mockExcludedQueryParams(List<String> params) {

        identityUtil.when(() -> IdentityUtil.getPropertyAsList(
                EXCLUDED_QUERY_PARAMS_CONFIG)).thenReturn(params);
    }

    private void mockMaxQueryParams(String max) {

        identityUtil.when(() -> IdentityUtil.getProperty(
                MAX_QUERY_PARAMS_CONFIG)).thenReturn(max);
    }

    private HttpServletRequest mockHttpRequest(String queryString) {

        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        when(httpRequest.getQueryString()).thenReturn(queryString);
        when(httpRequest.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        return httpRequest;
    }

    private Map<String, List<String>> toMap(List<Parameter> parameters) {

        return parameters.stream().collect(Collectors.toMap(Parameter::getName, Parameter::getValue));
    }
}
