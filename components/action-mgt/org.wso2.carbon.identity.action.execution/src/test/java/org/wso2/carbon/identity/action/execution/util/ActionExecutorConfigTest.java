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

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.execution.model.ActionType;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class ActionExecutorConfigTest {

    private ActionExecutorConfig actionExecutorConfig;

    @Mock
    private IdentityConfigParser mockIdentityConfigParser;

    private MockedStatic<IdentityConfigParser> identityConfigParser;

    @BeforeMethod
    public void setUp() {

        MockitoAnnotations.openMocks(this);
        actionExecutorConfig = ActionExecutorConfig.getInstance();

        identityConfigParser = mockStatic(IdentityConfigParser.class);
        identityConfigParser.when(IdentityConfigParser::getInstance).thenReturn(mockIdentityConfigParser);
    }

    @AfterMethod
    public void tearDown() {

        identityConfigParser.close();
    }

    @Test
    public void testIsExecutionForActionTypeEnabledForEnabledConfig() {

        Map<String, Object> configMap = new HashMap<>();
        configMap.put("Actions.Types.PreIssueAccessToken.Enable", "true");

        when(mockIdentityConfigParser.getConfiguration()).thenReturn(configMap);

        assertTrue(actionExecutorConfig.isExecutionForActionTypeEnabled(ActionType.PRE_ISSUE_ACCESS_TOKEN));
    }

    @Test
    public void testIsExecutionForActionTypeEnabledForDisabledConfig() {

        Map<String, Object> configMap = new HashMap<>();
        configMap.put("Actions.Types.PreIssueAccessToken.Enable", "false");

        when(mockIdentityConfigParser.getConfiguration()).thenReturn(configMap);

        assertFalse(actionExecutorConfig.isExecutionForActionTypeEnabled(ActionType.PRE_ISSUE_ACCESS_TOKEN));
    }

    @Test
    public void testIsExecutionForActionTypeEnabledForInvalidConfig() {

        Map<String, Object> configMap = new HashMap<>();
        configMap.put("Actions.Types.PreIssueAccessToken.Enable", "invalid");

        when(mockIdentityConfigParser.getConfiguration()).thenReturn(configMap);
        assertFalse(actionExecutorConfig.isExecutionForActionTypeEnabled(ActionType.PRE_ISSUE_ACCESS_TOKEN));
    }

    @Test
    public void testGetExcludedHeadersInActionRequestForValidConfigForAllTypesOnly() {

        Map<String, Object> configMap = new HashMap<>();
        configMap.put("Actions.ActionRequest.ExcludedHeaders.Header", Arrays.asList("header1", "header2"));

        when(mockIdentityConfigParser.getConfiguration()).thenReturn(configMap);

        Set<String> excludedHeaders =
                actionExecutorConfig.getExcludedHeadersInActionRequestForActionType(ActionType.PRE_ISSUE_ACCESS_TOKEN);
        assertEquals(excludedHeaders, new HashSet<>(Arrays.asList("header1", "header2")));
    }

    @Test
    public void testGetExcludedHeadersInActionRequestForValidConfigWithDuplicatedHeadersForAllTypes() {

        Map<String, Object> configMap = new HashMap<>();
        configMap.put("Actions.ActionRequest.ExcludedHeaders.Header", Arrays.asList("header1", "header2", "header1"));

        when(mockIdentityConfigParser.getConfiguration()).thenReturn(configMap);

        Set<String> excludedHeaders =
                actionExecutorConfig.getExcludedHeadersInActionRequestForActionType(ActionType.PRE_ISSUE_ACCESS_TOKEN);
        assertEquals(excludedHeaders, new HashSet<>(Arrays.asList("header1", "header2")));
    }

    @Test
    public void testGetExcludedHeadersInActionRequestForValidConfigForDefinedTypeOnly() {

        Map<String, Object> configMap = new HashMap<>();
        configMap.put("Actions.Types.PreIssueAccessToken.ActionRequest.ExcludedHeaders.Header",
                Arrays.asList("header1", "header2"));

        when(mockIdentityConfigParser.getConfiguration()).thenReturn(configMap);

        Set<String> excludedHeaders =
                actionExecutorConfig.getExcludedHeadersInActionRequestForActionType(ActionType.PRE_ISSUE_ACCESS_TOKEN);
        assertEquals(excludedHeaders, new HashSet<>(Arrays.asList("header1", "header2")));
    }

    @Test
    public void testGetExcludedHeadersInActionRequestForValidConfigWithOneValueForDefinedTypeOnly() {

        Map<String, Object> configMap = new HashMap<>();
        configMap.put("Actions.Types.PreIssueAccessToken.ActionRequest.ExcludedHeaders.Header", "header1");

        when(mockIdentityConfigParser.getConfiguration()).thenReturn(configMap);

        Set<String> excludedHeaders =
                actionExecutorConfig.getExcludedHeadersInActionRequestForActionType(ActionType.PRE_ISSUE_ACCESS_TOKEN);
        assertEquals(excludedHeaders, new HashSet<>(Collections.singletonList("header1")));
    }

    @Test
    public void testGetExcludedHeadersInActionRequestForValidConfigForAllAndDefinedType() {

        Map<String, Object> configMap = new HashMap<>();
        configMap.put("Actions.ActionRequest.ExcludedHeaders.Header", Arrays.asList("header1", "header2"));
        configMap.put("Actions.Types.PreIssueAccessToken.ActionRequest.ExcludedHeaders.Header",
                Arrays.asList("header3", "header4"));

        when(mockIdentityConfigParser.getConfiguration()).thenReturn(configMap);

        Set<String> excludedHeaders =
                actionExecutorConfig.getExcludedHeadersInActionRequestForActionType(ActionType.PRE_ISSUE_ACCESS_TOKEN);
        assertEquals(excludedHeaders, new HashSet<>(Arrays.asList("header1", "header2", "header3", "header4")));
    }

    @Test
    public void testGetExcludedHeadersInActionRequestForInvalidConfigForAllTypes() {

        Map<String, Object> configMap = new HashMap<>();
        configMap.put("Actions.ActionRequest.ExcludedHeaders.Header", 12);

        when(mockIdentityConfigParser.getConfiguration()).thenReturn(configMap);

        Set<String> excludedHeaders =
                actionExecutorConfig.getExcludedHeadersInActionRequestForActionType(ActionType.PRE_ISSUE_ACCESS_TOKEN);
        assertEquals(excludedHeaders, Collections.emptySet());
    }

    @Test
    public void testGetExcludedHeadersInActionRequestForInvalidConfigForDefinedType() {

        Map<String, Object> configMap = new HashMap<>();
        configMap.put("Actions.Types.PreIssueAccessToken.ActionRequest.ExcludedHeaders.Header", 12);

        when(mockIdentityConfigParser.getConfiguration()).thenReturn(configMap);

        Set<String> excludedHeaders =
                actionExecutorConfig.getExcludedHeadersInActionRequestForActionType(ActionType.PRE_ISSUE_ACCESS_TOKEN);
        assertEquals(excludedHeaders, Collections.emptySet());
    }

    @Test
    public void testGetExcludedHeadersInActionRequestForEmptyConfigForAllAndDefinedType() {

        Map<String, Object> configMap = new HashMap<>();
        configMap.put("Actions.ActionRequest.ExcludedHeaders.Header", Collections.emptyList());
        configMap.put("Actions.Types.PreIssueAccessToken.ActionRequest.ExcludedHeaders.Header",
                Collections.emptyList());

        when(mockIdentityConfigParser.getConfiguration()).thenReturn(configMap);

        Set<String> excludedHeaders =
                actionExecutorConfig.getExcludedHeadersInActionRequestForActionType(ActionType.PRE_ISSUE_ACCESS_TOKEN);
        assertEquals(excludedHeaders, Collections.emptySet());
    }

    @Test
    public void testGetExcludedParamsInActionRequestForValidConfigForAllTypesOnly() {

        Map<String, Object> configMap = new HashMap<>();
        configMap.put("Actions.ActionRequest.ExcludedParameters.Parameter", Arrays.asList("param1", "param2"));

        when(mockIdentityConfigParser.getConfiguration()).thenReturn(configMap);

        Set<String> excludedParams =
                actionExecutorConfig.getExcludedParamsInActionRequestForActionType(ActionType.PRE_ISSUE_ACCESS_TOKEN);
        assertEquals(excludedParams, new HashSet<>(Arrays.asList("param1", "param2")));
    }

    @Test
    public void testGetExcludedParamsInActionRequestForValidConfigWithDuplicatedParamsForAllTypes() {

        Map<String, Object> configMap = new HashMap<>();
        configMap.put("Actions.ActionRequest.ExcludedParameters.Parameter",
                Arrays.asList("param1", "param2", "param1"));

        when(mockIdentityConfigParser.getConfiguration()).thenReturn(configMap);

        Set<String> excludedParams =
                actionExecutorConfig.getExcludedParamsInActionRequestForActionType(ActionType.PRE_ISSUE_ACCESS_TOKEN);
        assertEquals(excludedParams, new HashSet<>(Arrays.asList("param1", "param2")));
    }

    @Test
    public void testGetExcludedParamsInActionRequestForValidConfigForDefinedTypeOnly() {

        Map<String, Object> configMap = new HashMap<>();
        configMap.put("Actions.Types.PreIssueAccessToken.ActionRequest.ExcludedParameters.Parameter",
                Arrays.asList("param1", "param2"));

        when(mockIdentityConfigParser.getConfiguration()).thenReturn(configMap);

        Set<String> excludedParams =
                actionExecutorConfig.getExcludedParamsInActionRequestForActionType(ActionType.PRE_ISSUE_ACCESS_TOKEN);
        assertEquals(excludedParams, new HashSet<>(Arrays.asList("param1", "param2")));
    }

    @Test
    public void testGetExcludedParamsInActionRequestForValidConfigForAllAndDefinedType() {

        Map<String, Object> configMap = new HashMap<>();
        configMap.put("Actions.ActionRequest.ExcludedParameters.Parameter", Arrays.asList("param1", "param2"));
        configMap.put("Actions.Types.PreIssueAccessToken.ActionRequest.ExcludedParameters.Parameter",
                Arrays.asList("param3", "param4"));

        when(mockIdentityConfigParser.getConfiguration()).thenReturn(configMap);

        Set<String> excludedParams =
                actionExecutorConfig.getExcludedParamsInActionRequestForActionType(ActionType.PRE_ISSUE_ACCESS_TOKEN);
        assertEquals(excludedParams, new HashSet<>(Arrays.asList("param1", "param2", "param3", "param4")));
    }

    @Test
    public void testGetExcludedParamsInActionRequestForInvalidConfigForAllTypes() {

        Map<String, Object> configMap = new HashMap<>();
        configMap.put("Actions.ActionRequest.ExcludedParameters.Parameter", 12);

        when(mockIdentityConfigParser.getConfiguration()).thenReturn(configMap);

        Set<String> excludedParams =
                actionExecutorConfig.getExcludedParamsInActionRequestForActionType(ActionType.PRE_ISSUE_ACCESS_TOKEN);
        assertEquals(excludedParams, Collections.emptySet());
    }

    @Test
    public void testGetExcludedParamsInActionRequestForInvalidConfigForDefinedType() {

        Map<String, Object> configMap = new HashMap<>();
        configMap.put("Actions.Types.PreIssueAccessToken.ActionRequest.ExcludedParameters.Parameter", 12);

        when(mockIdentityConfigParser.getConfiguration()).thenReturn(configMap);

        Set<String> excludedParams =
                actionExecutorConfig.getExcludedParamsInActionRequestForActionType(ActionType.PRE_ISSUE_ACCESS_TOKEN);
        assertEquals(excludedParams, Collections.emptySet());
    }

    @Test
    public void testGetExcludedParamsInActionRequestForEmptyConfigForAllAndDefinedType() {

        Map<String, Object> configMap = new HashMap<>();
        configMap.put("Actions.ActionRequest.ExcludedParameters.Parameter", Collections.emptyList());
        configMap.put("Actions.Types.PreIssueAccessToken.ActionRequest.ExcludedParameters.Parameter",
                Collections.emptyList());

        when(mockIdentityConfigParser.getConfiguration()).thenReturn(configMap);

        Set<String> excludedHeaders =
                actionExecutorConfig.getExcludedParamsInActionRequestForActionType(ActionType.PRE_ISSUE_ACCESS_TOKEN);
        assertEquals(excludedHeaders, Collections.emptySet());
    }

    @Test
    public void testGetHttpReadTimeout() {

        Map<String, Object> configMap = new HashMap<>();
        configMap.put("Actions.HTTPConnections.HTTPReadTimeout", "5000");
        when(mockIdentityConfigParser.getConfiguration()).thenReturn(configMap);
        Assert.assertEquals(5000, actionExecutorConfig.getHttpReadTimeout());
    }

    @Test
    public void testGetHttpConnectionRequestTimeout() {

        Map<String, Object> configMap = new HashMap<>();
        configMap.put("Actions.HTTPConnections.HTTPConnectionRequestTimeout", "2000");
        when(mockIdentityConfigParser.getConfiguration()).thenReturn(configMap);
        Assert.assertEquals(2000, actionExecutorConfig.getHttpConnectionRequestTimeout());
    }

    @Test
    public void testGetHttpConnectionTimeout() {

        Map<String, Object> configMap = new HashMap<>();
        configMap.put("Actions.HTTPConnections.HTTPConnectionTimeout", "2000");
        when(mockIdentityConfigParser.getConfiguration()).thenReturn(configMap);
        Assert.assertEquals(2000, actionExecutorConfig.getHttpConnectionTimeout());
    }
}
