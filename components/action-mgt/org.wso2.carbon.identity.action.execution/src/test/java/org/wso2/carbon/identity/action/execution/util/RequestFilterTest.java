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

public class RequestFilterTest {

    private MockedStatic<ActionExecutorConfig> mockedActionExecutorConfig;
    private List<Header> requestHeaders;
    private List<Param> requestParams;

    @BeforeMethod
    public void setUp() {

        ActionExecutorConfig config = Mockito.mock(ActionExecutorConfig.class);
        mockedActionExecutorConfig = mockStatic(ActionExecutorConfig.class);
        mockedActionExecutorConfig.when(ActionExecutorConfig::getInstance).thenReturn(config);

        requestHeaders = new ArrayList<>();
        requestHeaders.add(new Header("Content-Type", new String[]{"application/json"}));
        requestHeaders.add(new Header("X-Header-1", new String[]{"X-header-1-value"}));
        requestHeaders.add(new Header("x-header-2", new String[]{"X-header-2-value"}));
        requestHeaders.add(new Header("X-Header-3", new String[]{"X-header-3-value"}));

        requestParams = new ArrayList<>();
        requestParams.add(new Param("x-param-1", new String[]{"X-param-1-value"}));
        requestParams.add(new Param("X-Param-2", new String[]{"X-Param-2-Value"}));
        requestParams.add(new Param("x-param-3", new String[]{"X-param-3-value"}));
    }

    @AfterMethod
    public void tearDown() {

        mockedActionExecutorConfig.close();

        requestHeaders = Collections.emptyList();
        requestParams = Collections.emptyList();
    }

    @Test(description = "Ensures the default behaviour when allowed headers are defined only at action level.")
    public void testGetFilteredHeadersWhenAllowedHeadersAreConfigured() {

        ActionExecutorConfig config = ActionExecutorConfig.getInstance();
        Mockito.when(config.getExcludedHeadersInActionRequestForActionType(ActionType.PRE_ISSUE_ACCESS_TOKEN))
                .thenReturn(Collections.emptySet());

        List<String> allowedHeadersInAction = new ArrayList<>();
        allowedHeadersInAction.add("X-Header-1");
        allowedHeadersInAction.add("x-header-2");
        allowedHeadersInAction.add("X-Header-3");

        List<Header> filteredHeaders = RequestFilter.getFilteredHeaders(requestHeaders, allowedHeadersInAction,
                ActionType.PRE_ISSUE_ACCESS_TOKEN);

        assertEquals(filteredHeaders.size(), 3);
        filteredHeaders.forEach(filteredHeader -> {
            if (filteredHeader.getName().equals("X-Header-1")) {
                assertEquals(filteredHeader.getValue(), new String[]{"X-header-1-value"});
            } else if (filteredHeader.getName().equals("X-header-2")) {
                assertEquals(filteredHeader.getValue(), new String[]{"X-header-2-value"});
            } else if (filteredHeader.getName().equals("X-Header-3")) {
                assertEquals(filteredHeader.getValue(), new String[]{"X-header-3-value"});
            }
        });
    }

    @Test(description = "Ensure server-level allowed headers are ignored when there is action-level config defined.")
    public void testGetFilteredHeadersWhenAllowedHeadersAreConfiguredAlsoInServer() {

        Set<String> allowedHeadersInServer = new HashSet<>();
        allowedHeadersInServer.add("content-type");
        allowedHeadersInServer.add("x-header-3");

        ActionExecutorConfig config = ActionExecutorConfig.getInstance();
        Mockito.when(config.getAllowedHeadersForActionType(ActionType.PRE_ISSUE_ACCESS_TOKEN))
                .thenReturn(allowedHeadersInServer);

        List<String> allowedHeadersInAction = new ArrayList<>();
        allowedHeadersInAction.add("X-Header-1");
        allowedHeadersInAction.add("x-header-2");

        List<Header> filteredHeaders = RequestFilter.getFilteredHeaders(requestHeaders, allowedHeadersInAction,
                ActionType.PRE_ISSUE_ACCESS_TOKEN);

        assertEquals(filteredHeaders.size(), 2);
        filteredHeaders.forEach(filteredHeader -> {
            if (filteredHeader.getName().equals("X-Header-1")) {
                assertEquals(filteredHeader.getValue(), new String[]{"X-header-1-value"});
            } else if (filteredHeader.getName().equals("X-header-2")) {
                assertEquals(filteredHeader.getValue(), new String[]{"X-header-2-value"});
            }
        });
    }

    @Test(description = "Ensures server‑level allowed headers are applied when no action‑level headers are defined.")
    public void testGetFilteredHeadersWhenAllowedHeadersAreConfiguredOnlyInServer() {

        Set<String> allowedHeadersInServer = new HashSet<>();
        allowedHeadersInServer.add("content-type");
        allowedHeadersInServer.add("x-header-3");

        ActionExecutorConfig config = ActionExecutorConfig.getInstance();
        Mockito.when(config.getAllowedHeadersForActionType(ActionType.PRE_ISSUE_ACCESS_TOKEN))
                .thenReturn(allowedHeadersInServer);

        List<Header> filteredHeaders = RequestFilter.getFilteredHeaders(requestHeaders, Collections.emptyList(),
                ActionType.PRE_ISSUE_ACCESS_TOKEN);

        assertEquals(filteredHeaders.size(), 2);
        filteredHeaders.forEach(filteredHeader -> {
            if (filteredHeader.getName().equals("Content-Type")) {
                assertEquals(filteredHeader.getValue(), new String[]{"application/json"});
            } else if (filteredHeader.getName().equals("X-Header-3")) {
                assertEquals(filteredHeader.getValue(), new String[]{"X-header-3-value"});
            }
        });
    }

    @Test(description = "Ensures excluded headers are removed from the action-level config.")
    public void testGetFilteredHeadersWhenExcludedHeadersAreConfigured() {

        Set<String> excludedHeaders = new HashSet<>();
        excludedHeaders.add("x-header-2");

        ActionExecutorConfig config = ActionExecutorConfig.getInstance();
        Mockito.when(config.getExcludedHeadersInActionRequestForActionType(ActionType.PRE_ISSUE_ACCESS_TOKEN))
                .thenReturn(excludedHeaders);

        List<String> allowedHeadersInAction = new ArrayList<>();
        allowedHeadersInAction.add("X-Header-1");
        allowedHeadersInAction.add("x-header-2");
        allowedHeadersInAction.add("X-Header-3");

        List<Header> filteredHeaders = RequestFilter.getFilteredHeaders(requestHeaders, allowedHeadersInAction,
                        ActionType.PRE_ISSUE_ACCESS_TOKEN);

        assertEquals(filteredHeaders.size(), 2);
        filteredHeaders.forEach(filteredHeader -> {
            if (filteredHeader.getName().equals("X-Header-1")) {
                assertEquals(filteredHeader.getValue(), new String[]{"X-header-1-value"});
            } else if (filteredHeader.getName().equals("X-Header-3")) {
                assertEquals(filteredHeader.getValue(), new String[]{"X-header-3-value"});
            }
        });
    }

    @Test(description = "Ensures the default behaviour when allowed parameters are defined only at action level." +
            "Case-sensitivity is considered.")
    public void testGetFilteredParamsWhenAllowedParamsAreConfigured() {

        ActionExecutorConfig config = ActionExecutorConfig.getInstance();
        Mockito.when(config.getExcludedParamsInActionRequestForActionType(ActionType.PRE_ISSUE_ACCESS_TOKEN))
                .thenReturn(Collections.emptySet());

        List<String> allowedParamsInAction = new ArrayList<>();
        allowedParamsInAction.add("x-param-1");
        allowedParamsInAction.add("X-Param-2");
        allowedParamsInAction.add("x-param-3");

        List<Param> filteredParams = RequestFilter.getFilteredParams(requestParams, allowedParamsInAction,
                ActionType.PRE_ISSUE_ACCESS_TOKEN);

        assertEquals(filteredParams.size(), 3);
        filteredParams.forEach(filteredParam -> {
            if (filteredParam.getName().equals("x-param-1")) {
                assertEquals(filteredParam.getValue(), new String[]{"X-param-1-value"});
            } else if (filteredParam.getName().equals("x-param-2")) {
                assertEquals(filteredParam.getValue(), new String[]{"X-param-2-value"});
            } else if (filteredParam.getName().equals("x-Param-3")) {
                assertEquals(filteredParam.getValue(), new String[]{"X-Param-3-Value"});
            }
        });
    }

    @Test(description = "Ensure server-level allowed parameters are ignored when there is action-level" +
            "config defined. Case-sensitivity is considered.")
    public void testGetFilteredParamsWhenAllowedParamsAreConfiguredAlsoInServer() {

        Set<String> allowedParamsInServer = new HashSet<>();
        allowedParamsInServer.add("X-Param-2");

        ActionExecutorConfig config = ActionExecutorConfig.getInstance();
        Mockito.when(config.getAllowedParamsForActionType(ActionType.PRE_ISSUE_ACCESS_TOKEN))
                .thenReturn(allowedParamsInServer);

        List<String> allowedParamsInAction = new ArrayList<>();
        allowedParamsInAction.add("x-param-1");
        allowedParamsInAction.add("x-param-3");

        List<Param> filteredParams = RequestFilter.getFilteredParams(requestParams, allowedParamsInAction,
                ActionType.PRE_ISSUE_ACCESS_TOKEN);

        assertEquals(filteredParams.size(), 2);
        filteredParams.forEach(filteredParam -> {
            if (filteredParam.getName().equals("x-param-1")) {
                assertEquals(filteredParam.getValue(), new String[]{"X-param-1-value"});
            } else if (filteredParam.getName().equals("x-param-3")) {
                assertEquals(filteredParam.getValue(), new String[]{"X-param-3-value"});
            }
        });
    }

    @Test(description = "Ensures server‑level allowed parameters are applied when no action‑level" +
            "parameters are defined. Case-sensitivity is considered.")
    public void testGetFilteredParamsWhenAllowedParamsAreConfiguredOnlyInServer() {

        Set<String> allowedParamsInServer = new HashSet<>();
        allowedParamsInServer.add("x-Param-2");
        allowedParamsInServer.add("x-param-3");

        ActionExecutorConfig config = ActionExecutorConfig.getInstance();
        Mockito.when(config.getAllowedParamsForActionType(ActionType.PRE_ISSUE_ACCESS_TOKEN))
                .thenReturn(allowedParamsInServer);

        List<Param> filteredParams = RequestFilter.getFilteredParams(requestParams, Collections.emptyList(),
                ActionType.PRE_ISSUE_ACCESS_TOKEN);

        assertEquals(filteredParams.size(), 1);
        filteredParams.forEach(filteredParam -> {
            if (filteredParam.getName().equals("x-Param-3")) {
                assertEquals(filteredParam.getValue(), new String[]{"X-Param-3-Value"});
            }
        });
    }

    @Test(description = "Ensures excluded parameters are removed from the action-level config." +
            "Case-sensitivity is considered.")
    public void testGetFilteredParametersWhenExcludedParamsAreConfigured() {

        Set<String> excludedParams = new HashSet<>();
        excludedParams.add("x-param-1");
        excludedParams.add("x-param-2");
        excludedParams.add("x-param-3");

        ActionExecutorConfig config = ActionExecutorConfig.getInstance();
        Mockito.when(config.getExcludedParamsInActionRequestForActionType(ActionType.PRE_ISSUE_ACCESS_TOKEN))
                .thenReturn(excludedParams);

        List<String> allowedParamsInAction = new ArrayList<>();
        allowedParamsInAction.add("x-param-1");
        allowedParamsInAction.add("X-Param-2");
        allowedParamsInAction.add("x-param-3");

        List<Param> filteredParams = RequestFilter.getFilteredParams(requestParams, allowedParamsInAction,
                ActionType.PRE_ISSUE_ACCESS_TOKEN);

        assertEquals(filteredParams.size(), 1);
        filteredParams.forEach(filteredParam -> {
            if (filteredParam.getName().equals("X-Param-2")) {
                assertEquals(filteredParam.getValue(), new String[]{"X-Param-2-Value"});
            }
        });
    }

    @Test
    public void testGetFilteredHeadersWhenNeitherAllowedOrExcludedHeadersAreConfigured() {

        ActionExecutorConfig config = ActionExecutorConfig.getInstance();
        Mockito.when(config.getAllowedHeadersForActionType(ActionType.PRE_UPDATE_PASSWORD))
                .thenReturn(Collections.emptySet());
        Mockito.when(config.getExcludedHeadersInActionRequestForActionType(ActionType.PRE_UPDATE_PASSWORD))
                .thenReturn(Collections.emptySet());

        List<Header> requestHeaders = new ArrayList<>();
        requestHeaders.add(new Header("Content-Type", new String[]{"application/json"}));
        requestHeaders.add(new Header("X-Header-1", new String[]{"X-header-1-value"}));
        requestHeaders.add(new Header("X-Header-3", new String[]{"X-header-3-value"}));

        List<Header> filteredHeaders = RequestFilter.getFilteredHeaders(requestHeaders, Collections.emptyList(),
                ActionType.PRE_UPDATE_PASSWORD);
        assertEquals(filteredHeaders.size(), 0);
    }

    @Test
    public void testGetFilteredHeadersWhenNeitherAllowedOrExcludedParamsAreConfigured() {

        ActionExecutorConfig config = ActionExecutorConfig.getInstance();
        Mockito.when(config.getAllowedParamsForActionType(ActionType.PRE_UPDATE_PASSWORD))
                .thenReturn(Collections.emptySet());
        Mockito.when(config.getExcludedParamsInActionRequestForActionType(ActionType.PRE_UPDATE_PASSWORD))
                .thenReturn(Collections.emptySet());

        List<Header> requestParams = new ArrayList<>();
        requestParams.add(new Header("Content-Type", new String[]{"application/json"}));
        requestParams.add(new Header("X-param-1", new String[]{"X-param-1-value"}));
        requestParams.add(new Header("X-Param-3", new String[]{"X-param-3-value"}));

        List<Header> filteredParams = RequestFilter.getFilteredHeaders(requestParams, Collections.emptyList(),
                ActionType.PRE_UPDATE_PASSWORD);
        assertEquals(filteredParams.size(), 0);
    }
}
