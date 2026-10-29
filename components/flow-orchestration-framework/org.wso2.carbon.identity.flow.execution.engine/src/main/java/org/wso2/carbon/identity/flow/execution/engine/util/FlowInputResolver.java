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

import org.wso2.carbon.identity.core.context.IdentityContext;
import org.wso2.carbon.identity.core.context.model.Header;
import org.wso2.carbon.identity.core.context.model.Parameter;
import org.wso2.carbon.identity.core.context.model.Request;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.mgt.Constants.FlowInputTypes;
import org.wso2.carbon.identity.flow.mgt.Constants.SystemVariables;
import org.wso2.carbon.identity.flow.mgt.model.FlowInputDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowInputDataType;
import org.wso2.carbon.identity.flow.mgt.model.FlowInputsDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowVariableInputDTO;
import org.wso2.carbon.identity.flow.mgt.model.GraphConfig;
import org.wso2.carbon.identity.flow.mgt.utils.FlowInputUtils;
import org.wso2.carbon.identity.flow.mgt.utils.FlowMgtUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;


/**
 * Keeps the values of the headers, query parameters and variables a flow defines in its execution context.
 * <p>
 * What the flow defines is read from its graph on every request, so the context holds values alone. A value that is
 * not defined, not valid, or not the user's to change is ignored, with a diagnostic log, and the flow continues with
 * the value it had.
 */
public final class FlowInputResolver {

    private static final FlowInputDiagnosticLogger DIAGNOSTIC_LOGGER = new FlowInputDiagnosticLogger();

    private static final String NOT_DEFINED = "The flow does not define it.";
    private static final String NOT_VALID = "The value is not valid for its type, too long, or holds characters " +
            "that are not allowed.";
    private static final String NOT_CLIENT_WRITABLE = "The variable cannot be set by the app.";
    private static final String EXCLUDED = "It is excluded on this server.";
    private static final String NOT_INITIATION = "Variables are accepted only when the flow starts.";

    private FlowInputResolver() {

    }

    /**
     * Keeps the headers and query parameters the flow defines from the current request, as the identity context
     * holds it. One the request does not have keeps its earlier value.
     *
     * @param context Flow execution context, with the flow's graph attached.
     */
    public static void populateRequestHeadersAndParams(FlowExecutionContext context) {

        Request request = IdentityContext.getThreadLocalIdentityContext().getRequest();
        if (!FlowMgtUtils.isFlowInputsEnabled() || request == null || context.getGraphConfig() == null
                || context.getGraphConfig().getFlowInputs() == null) {
            return;
        }
        Map<String, List<String>> headers = new HashMap<>();
        for (Header header : request.getHeaders()) {
            if (header.getName() != null && header.getValue() != null) {
                headers.computeIfAbsent(header.getName().toLowerCase(Locale.ROOT), key -> new ArrayList<>())
                        .addAll(header.getValue());
            }
        }
        Map<String, List<String>> queryParams = new HashMap<>();
        for (Parameter queryParam : request.getQueryParameters()) {
            if (queryParam.getName() != null && queryParam.getValue() != null) {
                queryParams.computeIfAbsent(queryParam.getName(), key -> new ArrayList<>())
                        .addAll(queryParam.getValue());
            }
        }

        FlowInputsDTO flowInputs = context.getGraphConfig().getFlowInputs();

        for (FlowInputDTO header : flowInputs.getHeaders()) {
            String name = FlowInputUtils.normalizeHeaderName(header.getName());
            if (FlowInputUtils.isExcludedHeader(name)) {
                logIgnored(context, FlowInputTypes.HEADER, name, EXCLUDED);
                continue;
            }
            applyRequestValue(context, FlowInputTypes.HEADER, name, header.getDataType(),
                    headers.get(name), context.getRequest().getHeaders());
        }
        for (FlowInputDTO queryParam : flowInputs.getQueryParams()) {
            String name = queryParam.getName();
            if (FlowInputUtils.isExcludedQueryParam(name)) {
                logIgnored(context, FlowInputTypes.QUERY_PARAM, name, EXCLUDED);
                continue;
            }
            applyRequestValue(context, FlowInputTypes.QUERY_PARAM, name, queryParam.getDataType(),
                    queryParams.get(name), context.getRequest().getQueryParameters());
        }
    }

    /**
     * Keeps the values the client gives the variables of the flow, sent as the {@code variables} object of the inputs.
     * <p>
     * Variables are accepted only when the flow starts. Then those the app may set are kept, and so is the A/B
     * testing key the client keeps for its browser, when it is valid; otherwise the flow runs with the key's initial
     * value. A variable sent on a later request is ignored.
     *
     * @param context        Flow execution context.
     * @param variableInputs Values the client gives the variables, by name, as read from JSON.
     * @param initiation     Whether this is the first request of the flow.
     */
    public static void applyVariableInputs(FlowExecutionContext context, Map<String, Object> variableInputs,
                                           boolean initiation) {

        if (variableInputs == null || variableInputs.isEmpty() || !FlowMgtUtils.isFlowInputsEnabled()) {
            return;
        }
        for (Map.Entry<String, Object> variableInput : variableInputs.entrySet()) {
            String variableName = variableInput.getKey();
            if (!initiation) {
                logIgnored(context, FlowInputTypes.VARIABLE, variableName, NOT_INITIATION);
            } else if (SystemVariables.AB_TESTING_KEY.equals(variableName)) {
                assignAbTestingKey(context, variableInput.getValue());
            } else {
                applyVariableInput(context, variableName, variableInput.getValue());
            }
        }
    }

    private static void applyVariableInput(FlowExecutionContext context, String variableName,
                                           Object variableInputValue) {

        FlowVariableInputDTO variable = findVariable(context, variableName);
        if (variable == null) {
            logIgnored(context, FlowInputTypes.VARIABLE, variableName, NOT_DEFINED);
            return;
        }
        if (!variable.isClientWritable()) {
            logIgnored(context, FlowInputTypes.VARIABLE, variableName, NOT_CLIENT_WRITABLE);
            return;
        }
        Object parsedValue = FlowInputUtils.fromInput(variable.getDataType(), variableInputValue);
        if (parsedValue == null) {
            logIgnored(context, FlowInputTypes.VARIABLE, variableName, NOT_VALID);
            return;
        }
        context.getVariables().put(variableName, parsedValue);
    }

    /**
     * The value of a variable, in the form kept for its type, for a condition to compare.
     *
     * @param context Flow execution context.
     * @param name    Name of the variable.
     * @return The value, or null when the variable is not defined or has no value.
     */
    public static Object getVariable(FlowExecutionContext context, String name) {

        return FlowInputUtils.toValue(typeOf(findVariable(context, name)), context.getVariables().get(name));
    }

    /**
     * The value of a header, in the form kept for its type, for a condition to compare.
     *
     * @param context Flow execution context.
     * @param name    Name of the header, in any case.
     * @return The value, or null when the header is not defined or has not been received.
     */
    public static Object getHeader(FlowExecutionContext context, String name) {

        String key = FlowInputUtils.normalizeHeaderName(name);
        FlowInputDTO header = find(context, FlowInputsDTO::getHeaders, key);
        return FlowInputUtils.toValue(typeOf(header), context.getRequest().getHeaders().get(key));
    }

    /**
     * The value of a query parameter, in the form kept for its type, for a condition to compare.
     *
     * @param context Flow execution context.
     * @param name    Name of the query parameter.
     * @return The value, or null when the query parameter is not defined or has not been received.
     */
    public static Object getQueryParam(FlowExecutionContext context, String name) {

        FlowInputDTO queryParam = find(context, FlowInputsDTO::getQueryParams, name);
        return FlowInputUtils.toValue(typeOf(queryParam), context.getRequest().getQueryParameters().get(name));
    }

    /**
     * The type of a variable.
     *
     * @param context Flow execution context.
     * @param name    Name of the variable.
     * @return The type, or null when the variable is not defined.
     */
    public static FlowInputDataType getVariableType(FlowExecutionContext context, String name) {

        return typeOf(findVariable(context, name));
    }

    /**
     * The type of a header.
     *
     * @param context Flow execution context.
     * @param name    Name of the header, in any case.
     * @return The type, or null when the header is not defined.
     */
    public static FlowInputDataType getHeaderType(FlowExecutionContext context, String name) {

        return typeOf(find(context, FlowInputsDTO::getHeaders, FlowInputUtils.normalizeHeaderName(name)));
    }

    /**
     * The type of a query parameter.
     *
     * @param context Flow execution context.
     * @param name    Name of the query parameter.
     * @return The type, or null when the query parameter is not defined.
     */
    public static FlowInputDataType getQueryParamType(FlowExecutionContext context, String name) {

        return typeOf(find(context, FlowInputsDTO::getQueryParams, name));
    }

    private static void applyRequestValue(FlowExecutionContext context, String inputType, String name,
                                          FlowInputDataType dataType, List<String> texts, Map<String, Object> values) {

        if (texts == null || texts.isEmpty()) {
            return;
        }
        List<Object> value = FlowInputUtils.parseEach(dataType, texts);
        if (value == null) {
            logIgnored(context, inputType, name, NOT_VALID);
            return;
        }
        values.put(name, value);
    }

    private static void assignAbTestingKey(FlowExecutionContext context, Object variableInputValue) {

        if (findVariable(context, SystemVariables.AB_TESTING_KEY) == null) {
            logIgnored(context, FlowInputTypes.VARIABLE, SystemVariables.AB_TESTING_KEY, NOT_DEFINED);
            return;
        }
        Object parsedValue = FlowInputUtils.fromInput(FlowInputDataType.INTEGER, variableInputValue);
        Long abTestingKeyValue = parsedValue == null ? null : FlowInputUtils.toAbTestingKey(parsedValue);
        if (abTestingKeyValue == null) {
            logIgnored(context, FlowInputTypes.VARIABLE, SystemVariables.AB_TESTING_KEY, NOT_VALID);
            return;
        }
        context.getVariables().put(SystemVariables.AB_TESTING_KEY, abTestingKeyValue);
    }

    private static FlowVariableInputDTO findVariable(FlowExecutionContext context, String name) {

        return find(context, FlowInputsDTO::getVariables, name);
    }

    private static <T extends FlowInputDTO> T find(FlowExecutionContext context,
                                                   Function<FlowInputsDTO, List<T>> kind, String name) {

        for (T input : defined(context, kind)) {
            if (input.getName().equals(name)) {
                return input;
            }
        }
        return null;
    }

    private static <T extends FlowInputDTO> List<T> defined(FlowExecutionContext context,
                                                            Function<FlowInputsDTO, List<T>> kind) {

        GraphConfig graphConfig = context.getGraphConfig();
        FlowInputsDTO inputs = graphConfig == null ? null : graphConfig.getFlowInputs();
        return inputs == null ? Collections.emptyList() : kind.apply(inputs);
    }

    private static FlowInputDataType typeOf(FlowInputDTO input) {

        return input == null ? null : input.getDataType();
    }

    private static void logIgnored(FlowExecutionContext context, String inputType, String name, String reason) {

        DIAGNOSTIC_LOGGER.logValueIgnored(context.getFlowType(), inputType, name, reason);
    }
}
