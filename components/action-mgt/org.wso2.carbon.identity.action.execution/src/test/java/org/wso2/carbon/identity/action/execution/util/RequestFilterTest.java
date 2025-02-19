/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.action.execution.util;

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.execution.api.model.ActionType;
import org.wso2.carbon.identity.action.execution.api.model.Header;
import org.wso2.carbon.identity.action.execution.api.model.Param;
import org.wso2.carbon.identity.action.execution.internal.util.ActionExecutorConfig;
import org.wso2.carbon.identity.action.execution.internal.util.RequestFilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.mockStatic;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;

public class RequestFilterTest {

    private MockedStatic<ActionExecutorConfig> mockedActionExecutorConfig;

    @BeforeMethod
    public void setUp() {

        ActionExecutorConfig config = Mockito.mock(ActionExecutorConfig.class);
        mockedActionExecutorConfig = mockStatic(ActionExecutorConfig.class);
        mockedActionExecutorConfig.when(ActionExecutorConfig::getInstance).thenReturn(config);
    }

    @AfterMethod
    public void tearDown() {

        mockedActionExecutorConfig.close();
    }

    @Test
    public void testGetFilteredHeadersWhenExcludedHeadersAreConfigured() {

        Set<String> excludedHeaders = new HashSet<>();
        excludedHeaders.add("x-header-1");
        excludedHeaders.add("x-header-2");

        ActionExecutorConfig config = ActionExecutorConfig.getInstance();
        Mockito.when(config.getExcludedHeadersInActionRequestForActionType(ActionType.PRE_ISSUE_ACCESS_TOKEN))
                .thenReturn(excludedHeaders);

        List<Header> headers = new ArrayList<>();
        headers.add(new Header("Content-Type", new String[]{"application/json"}));
        headers.add(new Header("X-Header-1", new String[]{"X-header-1-value"}));
        headers.add(new Header("X-Header-3", new String[]{"X-header-3-value"}));

        List<Header> filteredHeaders =
                RequestFilter.getFilteredHeaders(headers, ActionType.PRE_ISSUE_ACCESS_TOKEN);
        assertEquals(filteredHeaders.size(), 2);
        filteredHeaders.forEach(filteredHeader -> {
            if (filteredHeader.getName().equals("Content-Type")) {
                assertEquals(filteredHeader.getValue(), new String[]{"application/json"});
            } else if (filteredHeader.getName().equals("X-Header-3")) {
                assertEquals(filteredHeader.getValue(), new String[]{"X-header-3-value"});
            }
        });
    }

    @Test
    public void testGetFilteredParamsWhenExcludedParamsAreConfigured() {

        Set<String> excludedParams = new HashSet<>();
        excludedParams.add("x-param-1");
        excludedParams.add("x-param-2");

        ActionExecutorConfig config = ActionExecutorConfig.getInstance();
        Mockito.when(config.getExcludedParamsInActionRequestForActionType(ActionType.PRE_ISSUE_ACCESS_TOKEN))
                .thenReturn(excludedParams);

        List<Param> params = new ArrayList<>();
        params.add(new Param("x-param-1", new String[]{"X-param-1-value"}));
        params.add(new Param("X-Param-2", new String[]{"X-Param-2-Value"}));
        params.add(new Param("x-param-3", new String[]{"X-param-3-value"}));

        List<Param> filteredParams =
                RequestFilter.getFilteredParams(params, ActionType.PRE_ISSUE_ACCESS_TOKEN);
        assertEquals(filteredParams.size(), 2);
        filteredParams.forEach(filteredParam -> {
            if (filteredParam.getName().equals("x-param-3")) {
                assertEquals(filteredParam.getValue(), new String[]{"X-param-3-value"});
            } else if (filteredParam.getName().equals("X-Param-2")) {
                assertEquals(filteredParam.getValue(), new String[]{"X-Param-2-Value"});
            }
        });
    }

    @Test
    public void testGetFilteredHeadersWhenAllowedHeadersAreConfigured() {

        Set<String> allowedHeaders = new HashSet<>();
        allowedHeaders.add("content-type");
        allowedHeaders.add("x-header-3");

        ActionExecutorConfig config = ActionExecutorConfig.getInstance();
        Mockito.when(config.getAllowedHeadersForActionType(ActionType.PRE_ISSUE_ACCESS_TOKEN))
                .thenReturn(allowedHeaders);

        List<Header> headers = new ArrayList<>();
        headers.add(new Header("Content-Type", new String[]{"application/json"}));
        headers.add(new Header("X-Header-1", new String[]{"X-header-1-value"}));
        headers.add(new Header("X-Header-3", new String[]{"X-header-3-value"}));

        List<Header> filteredHeaders = RequestFilter.getFilteredHeaders(headers, ActionType.PRE_ISSUE_ACCESS_TOKEN);
        assertEquals(filteredHeaders.size(), 2);
        filteredHeaders.forEach(filteredHeader -> {
            if (filteredHeader.getName().equals("Content-Type")) {
                assertEquals(filteredHeader.getValue(), new String[]{"application/json"});
            } else if (filteredHeader.getName().equals("X-Header-3")) {
                assertEquals(filteredHeader.getValue(), new String[]{"X-header-3-value"});
            }
        });
    }

    @Test
    public void testGetFilteredParamsWhenAllowedParamsAreConfigured() {

        Set<String> allowedParams = new HashSet<>();
        allowedParams.add("x-Param-1");
        allowedParams.add("x-Param-2");
        allowedParams.add("x-param-3");

        ActionExecutorConfig config = ActionExecutorConfig.getInstance();
        Mockito.when(config.getAllowedParamsForActionType(ActionType.PRE_ISSUE_ACCESS_TOKEN)).thenReturn(allowedParams);

        List<Param> params = new ArrayList<>();
        params.add(new Param("x-param-1", new String[]{"X-param-1-value"}));
        params.add(new Param("x-Param-2", new String[]{"X-Param-2-Value"}));
        params.add(new Param("x-param-3", new String[]{"X-param-3-value"}));

        List<Param> filteredParams = RequestFilter.getFilteredParams(params, ActionType.PRE_ISSUE_ACCESS_TOKEN);
        assertEquals(filteredParams.size(), 2);
        filteredParams.forEach(filteredParam -> {
            if (filteredParam.getName().equals("x-param-3")) {
                assertEquals(filteredParam.getValue(), new String[]{"X-param-3-value"});
            } else if (filteredParam.getName().equals("x-Param-2")) {
                assertEquals(filteredParam.getValue(), new String[]{"X-Param-2-Value"});
            }
        });
    }

    @Test
    public void testGetFilteredHeadersWhenNeitherAllowedOrExcludedHeadersAreConfigured() {

        ActionExecutorConfig config = ActionExecutorConfig.getInstance();
        Mockito.when(config.getAllowedHeadersForActionType(ActionType.PRE_ISSUE_ACCESS_TOKEN))
                .thenReturn(Collections.emptySet());
        Mockito.when(config.getExcludedHeadersInActionRequestForActionType(ActionType.PRE_ISSUE_ACCESS_TOKEN))
                .thenReturn(Collections.emptySet());

        List<Header> headers = new ArrayList<>();
        headers.add(new Header("Content-Type", new String[]{"application/json"}));
        headers.add(new Header("X-Header-1", new String[]{"X-header-1-value"}));
        headers.add(new Header("X-Header-3", new String[]{"X-header-3-value"}));

        List<Header> filteredHeaders = RequestFilter.getFilteredHeaders(headers, ActionType.PRE_ISSUE_ACCESS_TOKEN);
        assertEquals(filteredHeaders.size(), 0);
    }

    @Test
    public void testGetFilteredHeadersWhenNeitherAllowedOrExcludedParamsAreConfigured() {

        ActionExecutorConfig config = ActionExecutorConfig.getInstance();
        Mockito.when(config.getAllowedParamsForActionType(ActionType.PRE_ISSUE_ACCESS_TOKEN))
                .thenReturn(Collections.emptySet());
        Mockito.when(config.getExcludedParamsInActionRequestForActionType(ActionType.PRE_ISSUE_ACCESS_TOKEN))
                .thenReturn(Collections.emptySet());

        List<Header> headers = new ArrayList<>();
        headers.add(new Header("Content-Type", new String[]{"application/json"}));
        headers.add(new Header("X-Header-1", new String[]{"X-header-1-value"}));
        headers.add(new Header("X-Header-3", new String[]{"X-header-3-value"}));

        List<Header> filteredHeaders = RequestFilter.getFilteredHeaders(headers, ActionType.PRE_ISSUE_ACCESS_TOKEN);
        assertEquals(filteredHeaders.size(), 0);
    }

    @Test
    public void testGetFilteredHeadersThrowsIllegalStateException() {

        Set<String> allowedHeaders = new HashSet<>();
        allowedHeaders.add("content-type");

        Set<String> excludedHeaders = new HashSet<>();
        excludedHeaders.add("x-header-1");

        ActionExecutorConfig config = ActionExecutorConfig.getInstance();
        Mockito.when(config.getAllowedHeadersForActionType(ActionType.PRE_ISSUE_ACCESS_TOKEN))
                .thenReturn(allowedHeaders);
        Mockito.when(config.getExcludedHeadersInActionRequestForActionType(ActionType.PRE_ISSUE_ACCESS_TOKEN))
                .thenReturn(excludedHeaders);

        List<Header> headers = new ArrayList<>();
        headers.add(new Header("Content-Type", new String[]{"application/json"}));
        headers.add(new Header("X-Header-1", new String[]{"X-header-1-value"}));

        assertThrows(IllegalStateException.class, () -> {
            RequestFilter.getFilteredHeaders(headers, ActionType.PRE_ISSUE_ACCESS_TOKEN);
        });
    }

    @Test
    public void testGetFilteredParamsThrowsIllegalStateException() {

        Set<String> allowedParams = new HashSet<>();
        allowedParams.add("x-param-1");

        Set<String> excludedParams = new HashSet<>();
        excludedParams.add("x-param-2");

        ActionExecutorConfig config = ActionExecutorConfig.getInstance();
        Mockito.when(config.getAllowedParamsForActionType(ActionType.PRE_ISSUE_ACCESS_TOKEN)).thenReturn(allowedParams);
        Mockito.when(config.getExcludedParamsInActionRequestForActionType(ActionType.PRE_ISSUE_ACCESS_TOKEN))
                .thenReturn(excludedParams);

        List<Param> params = new ArrayList<>();
        params.add(new Param("x-param-1", new String[]{"X-param-1-value"}));
        params.add(new Param("X-Param-2", new String[]{"X-Param-2-Value"}));

        assertThrows(IllegalStateException.class, () -> {
            RequestFilter.getFilteredParams(params, ActionType.PRE_ISSUE_ACCESS_TOKEN);
        });
    }
}
