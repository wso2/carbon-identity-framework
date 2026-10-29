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
import org.wso2.carbon.identity.core.context.model.Request;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.mgt.Constants.FlowVariableTypes;
import org.wso2.carbon.identity.flow.mgt.Constants.SystemRuntimeValues;
import org.wso2.carbon.identity.flow.mgt.model.FlowRuntimeValueDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowVariableDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowVariableDataType;
import org.wso2.carbon.identity.flow.mgt.model.FlowVariablesDTO;
import org.wso2.carbon.identity.flow.mgt.model.GraphConfig;
import org.wso2.carbon.identity.flow.mgt.utils.FlowMgtUtils;
import org.wso2.carbon.identity.flow.mgt.utils.FlowVariableUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * Keeps the values of the headers and runtime values a flow defines in its execution context.
 * <p>
 * What the flow defines is read from its graph on every request, so the context holds values alone. A value that is
 * not valid, or not the app's to set, is ignored, with a diagnostic log, and the flow continues with the value it had.
 */
public final class FlowVariableResolver {

    private static final FlowVariableDiagnosticLogger DIAGNOSTIC_LOGGER = new FlowVariableDiagnosticLogger();

    private static final String NOT_VALID = "The value is not valid for its type, too long, or holds characters " +
            "that are not allowed.";
    private static final String NOT_CLIENT_WRITABLE = "The runtime value cannot be set by the app.";
    private static final String EXCLUDED = "It is excluded on this server.";
    private static final String NOT_INITIATION = "Runtime values are accepted only when the flow starts.";

    private FlowVariableResolver() {

    }

    /**
     * Keeps the headers the flow defines from the current request, as the identity context holds it. One the request
     * does not have keeps its earlier value.
     *
     * @param context Flow execution context, with the flow's graph attached.
     */
    public static void populateRequestHeaders(FlowExecutionContext context) {

        Request request = IdentityContext.getThreadLocalIdentityContext().getRequest();
        if (!FlowMgtUtils.isFlowVariablesEnabled() || request == null || context.getGraphConfig() == null
                || context.getGraphConfig().getFlowVariables() == null) {
            return;
        }
        Map<String, List<String>> headers = new HashMap<>();
        for (Header header : request.getHeaders()) {
            if (header.getName() != null && header.getValue() != null) {
                headers.computeIfAbsent(header.getName().toLowerCase(Locale.ROOT), key -> new ArrayList<>())
                        .addAll(header.getValue());
            }
        }

        for (FlowVariableDTO header : context.getGraphConfig().getFlowVariables().getHeaders()) {
            String name = FlowVariableUtils.normalizeHeaderName(header.getName());
            if (FlowVariableUtils.isExcludedHeader(name)) {
                context.getRequest().getHeaders().remove(name);
                logIgnored(context, FlowVariableTypes.HEADER, name, EXCLUDED);
                continue;
            }
            applyHeader(context, name, header.getDataType(), headers.get(name));
        }
    }

    /**
     * Keeps the values the app gives the runtime values of the flow, sent among the inputs like any other value.
     * <p>
     * Runtime values are accepted only when the flow starts. Then those the app may set are kept, and so is the A/B
     * testing key the app keeps for its browser, when it is valid; otherwise the flow runs with the initial value.
     * A runtime value sent on a later request is ignored. On every request, runtime values are taken out of the user
     * inputs, so that they are not read as the inputs of a step.
     *
     * @param context    Flow execution context, with the flow's graph attached and the request's inputs added to its
     *                   user inputs.
     * @param inputs     Inputs of the request, by name.
     * @param initiation Whether this is the first request of the flow.
     */
    public static void applyRuntimeValues(FlowExecutionContext context, Map<String, String> inputs,
                                          boolean initiation) {

        removeRuntimeValueInputs(context);
        if (inputs == null || inputs.isEmpty() || !FlowMgtUtils.isFlowVariablesEnabled()) {
            return;
        }
        for (FlowRuntimeValueDTO runtimeValue : defined(context, FlowVariablesDTO::getRuntimeValues)) {
            String name = runtimeValue.getName();
            if (!inputs.containsKey(name)) {
                continue;
            }
            if (!initiation) {
                logIgnored(context, FlowVariableTypes.RUNTIME_VALUE, name, NOT_INITIATION);
            } else if (SystemRuntimeValues.AB_TESTING_KEY.equals(name)) {
                applyAbTestingKey(context, inputs.get(name));
            } else if (!runtimeValue.isClientWritable()) {
                logIgnored(context, FlowVariableTypes.RUNTIME_VALUE, name, NOT_CLIENT_WRITABLE);
            } else {
                Object value = FlowVariableUtils.parse(runtimeValue.getDataType(), inputs.get(name));
                if (value == null) {
                    logIgnored(context, FlowVariableTypes.RUNTIME_VALUE, name, NOT_VALID);
                } else {
                    context.getRuntimeValues().put(name, value);
                }
            }
        }
    }

    /**
     * The value of a runtime value, in the form kept for its type, for a condition to compare.
     *
     * @param context Flow execution context.
     * @param name    Name of the runtime value.
     * @return The value, or null when the runtime value is not defined or has no value.
     */
    public static Object getRuntimeValue(FlowExecutionContext context, String name) {

        return FlowVariableUtils.toValue(typeOf(findRuntimeValue(context, name)),
                context.getRuntimeValues().get(name));
    }

    /**
     * The value of a header, in the form kept for its type, for a condition to compare.
     *
     * @param context Flow execution context.
     * @param name    Name of the header, in any case.
     * @return The value, or null when the header is not defined or has not been received.
     */
    public static Object getHeader(FlowExecutionContext context, String name) {

        String key = FlowVariableUtils.normalizeHeaderName(name);
        FlowVariableDTO header = find(context, FlowVariablesDTO::getHeaders, key);
        return FlowVariableUtils.toValue(typeOf(header), context.getRequest().getHeaders().get(key));
    }

    /**
     * The type of a runtime value.
     *
     * @param context Flow execution context.
     * @param name    Name of the runtime value.
     * @return The type, or null when the runtime value is not defined.
     */
    public static FlowVariableDataType getRuntimeValueType(FlowExecutionContext context, String name) {

        return typeOf(findRuntimeValue(context, name));
    }

    /**
     * The type of a header.
     *
     * @param context Flow execution context.
     * @param name    Name of the header, in any case.
     * @return The type, or null when the header is not defined.
     */
    public static FlowVariableDataType getHeaderType(FlowExecutionContext context, String name) {

        return typeOf(find(context, FlowVariablesDTO::getHeaders, FlowVariableUtils.normalizeHeaderName(name)));
    }

    /**
     * Takes the runtime values out of the user inputs. Otherwise the first request would read as a submission of the
     * first step, and a later one would carry an input its step does not have. A name the current step asks for is
     * kept, as it is then the user's own input. The A/B testing key is taken out even when flow variables are
     * disabled, as the app sends it regardless.
     */
    private static void removeRuntimeValueInputs(FlowExecutionContext context) {

        Map<String, String> userInputs = context.getUserInputData();
        if (userInputs == null || userInputs.isEmpty()) {
            return;
        }
        Set<String> names = new HashSet<>();
        names.add(SystemRuntimeValues.AB_TESTING_KEY);
        if (FlowMgtUtils.isFlowVariablesEnabled()) {
            for (FlowRuntimeValueDTO runtimeValue : defined(context, FlowVariablesDTO::getRuntimeValues)) {
                names.add(runtimeValue.getName());
            }
        }
        Set<String> stepInputs = new HashSet<>();
        if (context.getCurrentStepInputs() != null) {
            context.getCurrentStepInputs().values().stream().filter(Objects::nonNull).forEach(stepInputs::addAll);
        }
        names.removeAll(stepInputs);
        userInputs.keySet().removeAll(names);
    }

    private static void applyHeader(FlowExecutionContext context, String name, FlowVariableDataType dataType,
                                    List<String> texts) {

        if (texts == null || texts.isEmpty()) {
            return;
        }
        List<Object> value = FlowVariableUtils.parseEach(dataType, texts);
        if (value == null) {
            logIgnored(context, FlowVariableTypes.HEADER, name, NOT_VALID);
            return;
        }
        context.getRequest().getHeaders().put(name, value);
    }

    private static void applyAbTestingKey(FlowExecutionContext context, String text) {

        Object parsedValue = FlowVariableUtils.parse(FlowVariableDataType.INTEGER, text);
        Long abTestingKey = parsedValue == null ? null : FlowVariableUtils.toAbTestingKey(parsedValue);
        if (abTestingKey == null) {
            logIgnored(context, FlowVariableTypes.RUNTIME_VALUE, SystemRuntimeValues.AB_TESTING_KEY, NOT_VALID);
            return;
        }
        context.getRuntimeValues().put(SystemRuntimeValues.AB_TESTING_KEY, abTestingKey);
    }

    private static FlowRuntimeValueDTO findRuntimeValue(FlowExecutionContext context, String name) {

        return find(context, FlowVariablesDTO::getRuntimeValues, name);
    }

    private static <T extends FlowVariableDTO> T find(FlowExecutionContext context,
                                                      Function<FlowVariablesDTO, List<T>> kind, String name) {

        for (T variable : defined(context, kind)) {
            if (variable.getName().equals(name)) {
                return variable;
            }
        }
        return null;
    }

    private static <T extends FlowVariableDTO> List<T> defined(FlowExecutionContext context,
                                                               Function<FlowVariablesDTO, List<T>> kind) {

        GraphConfig graphConfig = context.getGraphConfig();
        FlowVariablesDTO variables = graphConfig == null ? null : graphConfig.getFlowVariables();
        return variables == null ? Collections.emptyList() : kind.apply(variables);
    }

    private static FlowVariableDataType typeOf(FlowVariableDTO variable) {

        return variable == null ? null : variable.getDataType();
    }

    private static void logIgnored(FlowExecutionContext context, String variableType, String name, String reason) {

        DIAGNOSTIC_LOGGER.logValueIgnored(context.getFlowType(), variableType, name, reason);
    }
}
