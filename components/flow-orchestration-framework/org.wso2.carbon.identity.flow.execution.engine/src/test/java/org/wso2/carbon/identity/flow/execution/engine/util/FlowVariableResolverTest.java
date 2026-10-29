/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.flow.execution.engine.util;

import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.core.context.IdentityContext;
import org.wso2.carbon.identity.core.context.model.Request;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.mgt.Constants;
import org.wso2.carbon.identity.flow.mgt.model.FlowRuntimeValueDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowVariableDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowVariableDataType;
import org.wso2.carbon.identity.flow.mgt.model.FlowVariablesDTO;
import org.wso2.carbon.identity.flow.mgt.model.GraphConfig;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Tests how the headers and runtime values of a flow are kept in its context across requests.
 */
public class FlowVariableResolverTest {

    private static final String AB_TESTING_KEY = Constants.SystemRuntimeValues.AB_TESTING_KEY;

    private MockedStatic<IdentityUtil> identityUtilMock;
    private MockedStatic<LoggerUtils> loggerUtilsMock;

    @BeforeMethod
    public void setUp() {

        identityUtilMock = mockStatic(IdentityUtil.class, CALLS_REAL_METHODS);
        identityUtilMock.when(() -> IdentityUtil.getProperty(ArgumentMatchers.anyString())).thenReturn(null);
        identityUtilMock.when(() -> IdentityUtil.getPropertyAsList(ArgumentMatchers.anyString()))
                .thenReturn(Collections.emptyList());
        identityUtilMock.when(() -> IdentityUtil.getProperty(Constants.FlowVariableConfigs.ENABLE))
                .thenReturn("true");
        loggerUtilsMock = mockStatic(LoggerUtils.class);
        loggerUtilsMock.when(LoggerUtils::isDiagnosticLogsEnabled).thenReturn(false);
    }

    @AfterMethod
    public void tearDown() {

        identityUtilMock.close();
        loggerUtilsMock.close();
        IdentityContext.destroyCurrentContext();
    }

    /**
     * A request without a header keeps the value an earlier request gave it, and an invalid value is ignored.
     */
    @Test
    public void testRequestValuesAreKeptAcrossRequests() {

        FlowExecutionContext context = contextWith(variables());

        receive(Collections.singletonMap("X-AB-Variant", Collections.singletonList("B")));
        FlowVariableResolver.populateRequestHeaders(context);
        receive(Collections.emptyMap());
        FlowVariableResolver.populateRequestHeaders(context);
        receive(Collections.singletonMap("x-ab-variant", Collections.singletonList("<b>")));
        FlowVariableResolver.populateRequestHeaders(context);

        assertEquals(context.getRequest().getHeaders().get("x-ab-variant"), Collections.singletonList("B"));
    }

    /**
     * A header excluded after the flow started loses the value an earlier request gave it.
     */
    @Test
    public void testExcludedRequestValuesAreDropped() {

        FlowExecutionContext context = contextWith(variables());
        receive(Collections.singletonMap("x-ab-variant", Collections.singletonList("B")));
        FlowVariableResolver.populateRequestHeaders(context);

        identityUtilMock.when(() -> IdentityUtil.getPropertyAsList(Constants.FlowVariableConfigs.EXCLUDED_HEADERS))
                .thenReturn(Collections.singletonList("x-ab-*"));
        receive(Collections.singletonMap("x-ab-variant", Collections.singletonList("A")));
        FlowVariableResolver.populateRequestHeaders(context);

        assertTrue(context.getRequest().getHeaders().isEmpty());
    }

    @Test
    public void testUndefinedRequestValuesAreNotKept() {

        FlowExecutionContext context = contextWith(variables());

        receive(Collections.singletonMap("x-other", Collections.singletonList("1")));
        FlowVariableResolver.populateRequestHeaders(context);

        assertTrue(context.getRequest().getHeaders().isEmpty());
    }

    /**
     * On the first request only the runtime values the app may set, and the A/B testing key, take the value given.
     */
    @Test
    public void testFirstRequestKeepsTheClientWritableRuntimeValues() {

        FlowExecutionContext context = contextWith(variables());
        context.getRuntimeValues().put("plan", "basic");
        Map<String, String> inputs = new HashMap<>();
        inputs.put("plan", "gold");
        inputs.put("attempts", "5");
        inputs.put("unknown", "x");
        inputs.put(AB_TESTING_KEY, "42");

        FlowVariableResolver.applyRuntimeValues(context, inputs, true);

        assertEquals(context.getRuntimeValues().get("plan"), "basic");
        assertEquals(context.getRuntimeValues().get("attempts"), 5L);
        assertFalse(context.getRuntimeValues().containsKey("unknown"));
        assertEquals(context.getRuntimeValues().get(AB_TESTING_KEY), 42L);
    }

    /**
     * Each value is read from its text for its type, and an array from a JSON array.
     */
    @Test
    public void testRuntimeValuesAreReadForTheirType() {

        FlowVariablesDTO variables = variables();
        variables.getRuntimeValues().add(clientWritable("trusted", FlowVariableDataType.BOOLEAN));
        variables.getRuntimeValues().add(clientWritable("score", FlowVariableDataType.DECIMAL));
        variables.getRuntimeValues().add(clientWritable("note", FlowVariableDataType.STRING));
        variables.getRuntimeValues().add(clientWritable("tags", FlowVariableDataType.STRING_ARRAY));
        variables.getRuntimeValues().add(clientWritable("limits", FlowVariableDataType.NUMBER_ARRAY));
        FlowExecutionContext context = contextWith(variables);
        Map<String, String> inputs = new HashMap<>();
        inputs.put("attempts", "10");
        inputs.put("trusted", "true");
        inputs.put("score", "1.5");
        inputs.put("note", "7");
        inputs.put("tags", "[\"a\", \"2\"]");
        inputs.put("limits", "[1, 2.5]");
        inputs.put(AB_TESTING_KEY, "15");

        FlowVariableResolver.applyRuntimeValues(context, inputs, true);

        assertEquals(context.getRuntimeValues().get("attempts"), 10L);
        assertEquals(context.getRuntimeValues().get("trusted"), true);
        assertEquals(context.getRuntimeValues().get("score"), 1.5);
        assertEquals(context.getRuntimeValues().get("note"), "7");
        assertEquals(context.getRuntimeValues().get("tags"), Arrays.asList("a", "2"));
        assertEquals(context.getRuntimeValues().get("limits"), Arrays.asList(1.0, 2.5));
        assertEquals(context.getRuntimeValues().get(AB_TESTING_KEY), 15L);
    }

    @Test
    public void testRuntimeValuesAreIgnoredWhenFlowVariablesAreDisabled() {

        identityUtilMock.when(() -> IdentityUtil.getProperty(Constants.FlowVariableConfigs.ENABLE))
                .thenReturn("false");
        FlowExecutionContext context = contextWith(variables());

        FlowVariableResolver.applyRuntimeValues(context, Collections.singletonMap(AB_TESTING_KEY, "42"), true);

        assertTrue(context.getRuntimeValues().isEmpty());
    }

    /**
     * The server makes up no key: without a valid one from the client, the flow keeps the initial value.
     */
    @Test
    public void testAbTestingKeyKeepsItsInitialValueWithoutAValidOne() {

        for (String sent : Arrays.asList("100", "-1", "abc", "4.5", "true", "[3]")) {
            FlowExecutionContext context = contextWith(variables());
            context.getRuntimeValues().put(AB_TESTING_KEY, 5L);

            FlowVariableResolver.applyRuntimeValues(context, Collections.singletonMap(AB_TESTING_KEY, sent), true);

            assertEquals(context.getRuntimeValues().get(AB_TESTING_KEY), 5L, "Sent: " + sent);
        }
    }

    /**
     * Runtime values are accepted only when the flow starts, so on a later request they keep their values.
     */
    @Test
    public void testLaterRequestIgnoresEveryRuntimeValue() {

        FlowExecutionContext context = contextWith(variables());
        context.getRuntimeValues().put("attempts", 1L);
        context.getRuntimeValues().put(AB_TESTING_KEY, 7L);
        Map<String, String> inputs = new HashMap<>();
        inputs.put("attempts", "2");
        inputs.put(AB_TESTING_KEY, "1");

        FlowVariableResolver.applyRuntimeValues(context, inputs, false);

        assertEquals(context.getRuntimeValues().get("attempts"), 1L);
        assertEquals(context.getRuntimeValues().get(AB_TESTING_KEY), 7L);
    }

    @Test
    public void testInvalidRuntimeValueKeepsTheInitialValue() {

        FlowExecutionContext context = contextWith(variables());
        context.getRuntimeValues().put("attempts", 1L);

        FlowVariableResolver.applyRuntimeValues(context, Collections.singletonMap("attempts", "two"), true);

        assertEquals(context.getRuntimeValues().get("attempts"), 1L);
    }

    @Test
    public void testConditionsReadValuesInTheFormOfTheirType() {

        FlowExecutionContext context = contextWith(variables());
        // As read back from the stored context, where JSON keeps a small integer as an Integer.
        context.getRuntimeValues().put("attempts", 3);
        context.getRequest().getHeaders().put("x-ab-variant", Collections.singletonList("B"));

        assertEquals(FlowVariableResolver.getRuntimeValue(context, "attempts"), 3L);
        assertEquals(FlowVariableResolver.getHeader(context, "X-AB-Variant"), Collections.singletonList("B"));
        assertNull(FlowVariableResolver.getRuntimeValue(context, "undefined"));
        assertEquals(FlowVariableResolver.getRuntimeValueType(context, Constants.SystemRuntimeValues.AB_TESTING_KEY),
                FlowVariableDataType.INTEGER);
    }

    private static FlowVariablesDTO variables() {

        FlowVariablesDTO variables = new FlowVariablesDTO();
        FlowRuntimeValueDTO plan = new FlowRuntimeValueDTO("plan", FlowVariableDataType.STRING);
        plan.setValue("basic");
        variables.getRuntimeValues().add(plan);
        FlowRuntimeValueDTO attempts = new FlowRuntimeValueDTO("attempts", FlowVariableDataType.INTEGER);
        attempts.setClientWritable(true);
        variables.getRuntimeValues().add(attempts);
        variables.getHeaders().add(new FlowVariableDTO("x-ab-variant", FlowVariableDataType.STRING_ARRAY));
        FlowRuntimeValueDTO abTestingKey =
                new FlowRuntimeValueDTO(Constants.SystemRuntimeValues.AB_TESTING_KEY, FlowVariableDataType.INTEGER);
        abTestingKey.setValue(Constants.SystemRuntimeValues.AB_TESTING_KEY_INITIAL_VALUE);
        variables.getRuntimeValues().add(abTestingKey);
        return variables;
    }

    private static FlowRuntimeValueDTO clientWritable(String name, FlowVariableDataType dataType) {

        FlowRuntimeValueDTO runtimeValue = new FlowRuntimeValueDTO(name, dataType);
        runtimeValue.setClientWritable(true);
        return runtimeValue;
    }

    private static FlowExecutionContext contextWith(FlowVariablesDTO variables) {

        GraphConfig graphConfig = new GraphConfig();
        graphConfig.setFlowVariables(variables);
        FlowExecutionContext context = new FlowExecutionContext();
        context.setGraphConfig(graphConfig);
        context.setFlowType("REGISTRATION");
        return context;
    }

    /**
     * Sets the request the identity context holds, as the valve does for each request.
     */
    private static void receive(Map<String, List<String>> headers) {

        IdentityContext.destroyCurrentContext();
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        when(httpRequest.getHeaderNames()).thenReturn(Collections.enumeration(headers.keySet()));
        headers.forEach((name, values) -> when(httpRequest.getHeaders(name))
                .thenReturn(Collections.enumeration(values)));
        IdentityContext.getThreadLocalIdentityContext()
                .setRequest(new Request.Builder().fromHttpRequest(httpRequest).build());
    }
}
