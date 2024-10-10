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
import org.wso2.carbon.identity.action.execution.model.ActionType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.mockStatic;
import static org.testng.Assert.assertEquals;

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
    public void testGetFilteredHeaders() {

        Set<String> excludedHeaders = new HashSet<>();
        excludedHeaders.add("x-header-1");
        excludedHeaders.add("x-header-2");

        ActionExecutorConfig config = ActionExecutorConfig.getInstance();
        Mockito.when(config.getExcludedHeadersInActionRequestForActionType(ActionType.PRE_ISSUE_ACCESS_TOKEN))
                .thenReturn(excludedHeaders);

        Map<String, String[]> headers = new HashMap<>();
        headers.put("Content-Type", new String[]{"application/json"});
        headers.put("X-Header-1", new String[]{"X-header-1-value"});
        headers.put("X-Header-3", new String[]{"X-header-3-value"});

        Map<String, String[]> filteredHeaders =
                RequestFilter.getFilteredHeaders(headers, ActionType.PRE_ISSUE_ACCESS_TOKEN);
        assertEquals(filteredHeaders.size(), 2);
        assertEquals(filteredHeaders.get("Content-Type"), new String[]{"application/json"});
        assertEquals(filteredHeaders.get("X-Header-3"), new String[]{"X-header-3-value"});
    }

    @Test
    public void testGetFilteredParams() {

        Set<String> excludedParams = new HashSet<>();
        excludedParams.add("x-param-1");
        excludedParams.add("x-param-2");

        ActionExecutorConfig config = ActionExecutorConfig.getInstance();
        Mockito.when(config.getExcludedParamsInActionRequestForActionType(ActionType.PRE_ISSUE_ACCESS_TOKEN))
                .thenReturn(excludedParams);

        Map<String, String[]> params = new HashMap<>();
        params.put("x-param-1", new String[]{"X-param-1-value"});
        params.put("X-Param-2", new String[]{"X-Param-2-Value"});
        params.put("x-param-3", new String[]{"X-param-3-value"});

        Map<String, String[]> filteredParams =
                RequestFilter.getFilteredParams(params, ActionType.PRE_ISSUE_ACCESS_TOKEN);
        assertEquals(filteredParams.size(), 2);
        assertEquals(filteredParams.get("x-param-3"), new String[]{"X-param-3-value"});
        assertEquals(filteredParams.get("X-Param-2"), new String[]{"X-Param-2-Value"});
    }
}
