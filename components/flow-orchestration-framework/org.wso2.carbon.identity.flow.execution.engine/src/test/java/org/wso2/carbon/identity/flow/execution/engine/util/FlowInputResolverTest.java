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
import org.wso2.carbon.identity.flow.mgt.model.FlowInputDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowInputDataType;
import org.wso2.carbon.identity.flow.mgt.model.FlowInputsDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowVariableInputDTO;
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
 * Tests how the headers, query parameters and variables of a flow are kept in its context across requests.
 */
public class FlowInputResolverTest {

    private static final String AB_TESTING_KEY = Constants.SystemVariables.AB_TESTING_KEY;

    private MockedStatic<IdentityUtil> identityUtilMock;
    private MockedStatic<LoggerUtils> loggerUtilsMock;

    @BeforeMethod
    public void setUp() {

        identityUtilMock = mockStatic(IdentityUtil.class, CALLS_REAL_METHODS);
        identityUtilMock.when(() -> IdentityUtil.getProperty(ArgumentMatchers.anyString())).thenReturn(null);
        identityUtilMock.when(() -> IdentityUtil.getPropertyAsList(ArgumentMatchers.anyString()))
                .thenReturn(Collections.emptyList());
        identityUtilMock.when(() -> IdentityUtil.getProperty(Constants.FlowInputConfigs.ENABLE))
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

        FlowExecutionContext context = contextWith(inputs());

        receive(Collections.singletonMap("X-AB-Variant", Collections.singletonList("B")), "age=18");
        FlowInputResolver.populateRequestHeadersAndParams(context);
        receive(Collections.emptyMap(), null);
        FlowInputResolver.populateRequestHeadersAndParams(context);
        receive(Collections.singletonMap("x-ab-variant", Collections.singletonList("<b>")), "age=eighteen");
        FlowInputResolver.populateRequestHeadersAndParams(context);

        assertEquals(context.getRequest().getHeaders().get("x-ab-variant"), Collections.singletonList("B"));
        assertEquals(context.getRequest().getQueryParameters().get("age"), Collections.singletonList(18.0));
    }

    @Test
    public void testUndefinedRequestValuesAreNotKept() {

        FlowExecutionContext context = contextWith(inputs());

        receive(Collections.singletonMap("x-other", Collections.singletonList("1")), "other=1");
        FlowInputResolver.populateRequestHeadersAndParams(context);

        assertTrue(context.getRequest().getHeaders().isEmpty());
        assertTrue(context.getRequest().getQueryParameters().isEmpty());
    }

    /**
     * On the first request only the variables the user may update, and the A/B testing key, take the value given.
     */
    @Test
    public void testFirstRequestKeepsTheClientWritableVariables() {

        FlowExecutionContext context = contextWith(inputs());
        context.getVariables().put("plan", "basic");
        Map<String, Object> variableInputs = new HashMap<>();
        variableInputs.put("plan", "gold");
        variableInputs.put("attempts", 5);
        variableInputs.put("unknown", "x");
        variableInputs.put(AB_TESTING_KEY, 42);

        FlowInputResolver.applyVariableInputs(context, variableInputs, true);

        assertEquals(context.getVariables().get("plan"), "basic");
        assertEquals(context.getVariables().get("attempts"), 5L);
        assertFalse(context.getVariables().containsKey("unknown"));
        assertEquals(context.getVariables().get(AB_TESTING_KEY), 42L);
    }

    /**
     * A value may be given as JSON of its type or as text, so "10" and 10 are the same integer.
     */
    @Test
    public void testVariableInputsAreReadForTheirType() {

        FlowInputsDTO inputs = inputs();
        inputs.getVariables().add(clientWritable("trusted", FlowInputDataType.BOOLEAN));
        inputs.getVariables().add(clientWritable("score", FlowInputDataType.DECIMAL));
        inputs.getVariables().add(clientWritable("note", FlowInputDataType.STRING));
        inputs.getVariables().add(clientWritable("tags", FlowInputDataType.STRING_ARRAY));
        inputs.getVariables().add(clientWritable("limits", FlowInputDataType.NUMBER_ARRAY));
        FlowExecutionContext context = contextWith(inputs);
        Map<String, Object> variableInputs = new HashMap<>();
        variableInputs.put("attempts", "10");
        variableInputs.put("trusted", "true");
        variableInputs.put("score", 1.5);
        variableInputs.put("note", 7);
        variableInputs.put("tags", Arrays.asList("a", 2));
        variableInputs.put("limits", Arrays.asList(1, "2.5"));
        variableInputs.put(AB_TESTING_KEY, "15");

        FlowInputResolver.applyVariableInputs(context, variableInputs, true);

        assertEquals(context.getVariables().get("attempts"), 10L);
        assertEquals(context.getVariables().get("trusted"), true);
        assertEquals(context.getVariables().get("score"), 1.5);
        assertEquals(context.getVariables().get("note"), "7");
        assertEquals(context.getVariables().get("tags"), Arrays.asList("a", "2"));
        assertEquals(context.getVariables().get("limits"), Arrays.asList(1.0, 2.5));
        assertEquals(context.getVariables().get(AB_TESTING_KEY), 15L);
    }

    @Test
    public void testVariableInputsAreIgnoredWhenFlowInputsAreDisabled() {

        identityUtilMock.when(() -> IdentityUtil.getProperty(Constants.FlowInputConfigs.ENABLE))
                .thenReturn("false");
        FlowExecutionContext context = contextWith(inputs());

        FlowInputResolver.applyVariableInputs(context, Collections.singletonMap(AB_TESTING_KEY, 42), true);

        assertTrue(context.getVariables().isEmpty());
    }

    /**
     * The server makes up no key: without a valid one from the client, the flow keeps the initial value.
     */
    @Test
    public void testAbTestingKeyKeepsItsInitialValueWithoutAValidOne() {

        for (Object sent : Arrays.asList(100, "-1", "abc", 4.5, true, Collections.singletonList(3), null)) {
            FlowExecutionContext context = contextWith(inputs());
            context.getVariables().put(AB_TESTING_KEY, 5L);

            FlowInputResolver.applyVariableInputs(context, Collections.singletonMap(AB_TESTING_KEY, sent), true);

            assertEquals(context.getVariables().get(AB_TESTING_KEY), 5L, "Sent: " + sent);
        }
    }

    /**
     * Variables are accepted only when the flow starts, so on a later request they keep their values.
     */
    @Test
    public void testLaterRequestIgnoresEveryVariableInput() {

        FlowExecutionContext context = contextWith(inputs());
        context.getVariables().put("attempts", 1L);
        context.getVariables().put(AB_TESTING_KEY, 7L);
        Map<String, Object> variableInputs = new HashMap<>();
        variableInputs.put("attempts", 2);
        variableInputs.put(AB_TESTING_KEY, 1);

        FlowInputResolver.applyVariableInputs(context, variableInputs, false);

        assertEquals(context.getVariables().get("attempts"), 1L);
        assertEquals(context.getVariables().get(AB_TESTING_KEY), 7L);
    }

    @Test
    public void testInvalidVariableInputKeepsTheInitialValue() {

        FlowExecutionContext context = contextWith(inputs());
        context.getVariables().put("attempts", 1L);

        FlowInputResolver.applyVariableInputs(context, Collections.singletonMap("attempts", "two"), true);

        assertEquals(context.getVariables().get("attempts"), 1L);
    }

    @Test
    public void testConditionsReadValuesInTheFormOfTheirType() {

        FlowExecutionContext context = contextWith(inputs());
        // As read back from the stored context, where JSON keeps a small integer as an Integer.
        context.getVariables().put("attempts", 3);
        context.getRequest().getHeaders().put("x-ab-variant", Collections.singletonList("B"));

        assertEquals(FlowInputResolver.getVariable(context, "attempts"), 3L);
        assertEquals(FlowInputResolver.getHeader(context, "X-AB-Variant"), Collections.singletonList("B"));
        assertNull(FlowInputResolver.getVariable(context, "undefined"));
        assertEquals(FlowInputResolver.getVariableType(context, Constants.SystemVariables.AB_TESTING_KEY),
                FlowInputDataType.INTEGER);
    }

    private static FlowInputsDTO inputs() {

        FlowInputsDTO inputs = new FlowInputsDTO();
        FlowVariableInputDTO plan = new FlowVariableInputDTO("plan", FlowInputDataType.STRING);
        plan.setValue("basic");
        inputs.getVariables().add(plan);
        FlowVariableInputDTO attempts = new FlowVariableInputDTO("attempts", FlowInputDataType.INTEGER);
        attempts.setClientWritable(true);
        inputs.getVariables().add(attempts);
        inputs.getHeaders().add(new FlowInputDTO("x-ab-variant", FlowInputDataType.STRING_ARRAY));
        inputs.getQueryParams().add(new FlowInputDTO("age", FlowInputDataType.NUMBER_ARRAY));
        FlowVariableInputDTO abTestingKey =
                new FlowVariableInputDTO(Constants.SystemVariables.AB_TESTING_KEY, FlowInputDataType.INTEGER);
        abTestingKey.setValue(Constants.SystemVariables.AB_TESTING_KEY_INITIAL_VALUE);
        inputs.getVariables().add(abTestingKey);
        return inputs;
    }

    private static FlowVariableInputDTO clientWritable(String name, FlowInputDataType dataType) {

        FlowVariableInputDTO variable = new FlowVariableInputDTO(name, dataType);
        variable.setClientWritable(true);
        return variable;
    }

    private static FlowExecutionContext contextWith(FlowInputsDTO inputs) {

        GraphConfig graphConfig = new GraphConfig();
        graphConfig.setFlowInputs(inputs);
        FlowExecutionContext context = new FlowExecutionContext();
        context.setGraphConfig(graphConfig);
        context.setFlowType("REGISTRATION");
        return context;
    }

    /**
     * Sets the request the identity context holds, as the valve does for each request.
     */
    private static void receive(Map<String, List<String>> headers, String queryString) {

        IdentityContext.destroyCurrentContext();
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        when(httpRequest.getHeaderNames()).thenReturn(Collections.enumeration(headers.keySet()));
        headers.forEach((name, values) -> when(httpRequest.getHeaders(name))
                .thenReturn(Collections.enumeration(values)));
        when(httpRequest.getQueryString()).thenReturn(queryString);
        IdentityContext.getThreadLocalIdentityContext()
                .setRequest(new Request.Builder().fromHttpRequest(httpRequest).build());
    }
}
