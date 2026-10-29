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

package org.wso2.carbon.identity.flow.mgt.utils;

import org.wso2.carbon.identity.flow.mgt.Constants;
import org.wso2.carbon.identity.flow.mgt.Constants.SystemRuntimeValues;
import org.wso2.carbon.identity.flow.mgt.exception.FlowMgtClientException;
import org.wso2.carbon.identity.flow.mgt.model.FlowDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowRuntimeValueDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowVariableDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowVariableDataType;
import org.wso2.carbon.identity.flow.mgt.model.FlowVariablesDTO;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static org.wso2.carbon.identity.flow.mgt.utils.FlowMgtUtils.handleClientException;
import static org.wso2.carbon.identity.flow.mgt.utils.FlowMgtUtils.handleFlowVariableException;

/**
 * Checks the headers and runtime values a flow defines, when the flow is saved.
 * <p>
 * Header names are put in lower case and initial values in the form kept for their type, so what is saved is what
 * the flow runs with.
 */
public final class FlowVariableValidator {

    private static final String HEADER = "header";
    private static final String RUNTIME_VALUE = "runtime value";

    private static final String HEADER_NAME_RULE =
            "Use letters, digits, hyphens and underscores, up to 64 characters, starting with a letter or digit.";
    private static final String RUNTIME_VALUE_NAME_RULE = "Use only letters and digits, up to 255 characters.";

    private FlowVariableValidator() {

    }

    /**
     * Validate the variables of a flow being saved. A flow that leaves out a system runtime value is given it first,
     * so it is validated and stored with the rest.
     *
     * @param flowDTO The flow being saved.
     * @throws FlowMgtClientException If any variable is not valid.
     */
    public static void validate(FlowDTO flowDTO) throws FlowMgtClientException {

        flowDTO.setFlowVariables(FlowVariableUtils.withSystemRuntimeValues(flowDTO.getFlowVariables()));
        FlowVariablesDTO variables = flowDTO.getFlowVariables();
        if (variables == null || variables.isEmpty()) {
            return;
        }
        if (!FlowMgtUtils.isFlowVariablesEnabled()) {
            throw handleClientException(Constants.ErrorMessages.ERROR_CODE_FLOW_VARIABLES_DISABLED);
        }
        validateHeaders(variables.getHeaders());
        validateRuntimeValues(variables.getRuntimeValues());
    }

    private static void validateHeaders(List<FlowVariableDTO> headers) throws FlowMgtClientException {

        validateCount(headers.size(), HEADER, FlowVariableUtils.getMaxHeaders());
        Set<String> names = new HashSet<>();
        for (FlowVariableDTO header : headers) {
            if (header != null) {
                header.setName(FlowVariableUtils.normalizeHeaderName(header.getName()));
            }
            validateName(header, HEADER, FlowVariableUtils::isValidHeaderName, HEADER_NAME_RULE, names);
            if (FlowVariableUtils.isExcludedHeader(header.getName())) {
                throw handleFlowVariableException("Excluded header.",
                        "The header %s cannot be read by flows on this server.", header.getName());
            }
            if (header.getDataType() == null || !header.getDataType().isArray()) {
                throw handleFlowVariableException("Invalid data type.",
                        "The header %s must be of type %s or %s, as a request may repeat it.",
                        header.getName(), FlowVariableDataType.STRING_ARRAY, FlowVariableDataType.NUMBER_ARRAY);
            }
        }
    }

    private static void validateRuntimeValues(List<FlowRuntimeValueDTO> runtimeValues)
            throws FlowMgtClientException {

        int defined = 0;
        for (FlowRuntimeValueDTO runtimeValue : runtimeValues) {
            if (runtimeValue == null || !FlowVariableUtils.isSystemRuntimeValue(runtimeValue.getName())) {
                defined++;
            }
        }
        validateCount(defined, RUNTIME_VALUE, FlowVariableUtils.getMaxRuntimeValues());
        Set<String> names = new HashSet<>();
        for (FlowRuntimeValueDTO runtimeValue : runtimeValues) {
            validateName(runtimeValue, RUNTIME_VALUE, FlowVariableUtils::isValidRuntimeValueName, RUNTIME_VALUE_NAME_RULE,
                    names);
            if (SystemRuntimeValues.AB_TESTING_KEY.equals(runtimeValue.getName())) {
                validateAbTestingKey(runtimeValue);
                continue;
            }
            if (runtimeValue.getDataType() == null) {
                throw handleFlowVariableException("Data type not provided.",
                        "The runtime value %s does not say what type of value it holds.", runtimeValue.getName());
            }
            if (runtimeValue.getValue() != null) {
                Object value = FlowVariableUtils.toValue(runtimeValue.getDataType(), runtimeValue.getValue());
                if (value == null) {
                    throw handleFlowVariableException("Invalid initial value.",
                            "The initial value of the runtime value %s is not a valid %s, or it is too long or holds " +
                                    "characters that are not allowed.", runtimeValue.getName(),
                            runtimeValue.getDataType());
                }
                runtimeValue.setValue(value);
            }
        }
    }

    private static void validateAbTestingKey(FlowRuntimeValueDTO runtimeValue) throws FlowMgtClientException {

        if (runtimeValue.getDataType() != FlowVariableDataType.INTEGER) {
            throw handleFlowVariableException("Invalid system runtime value.",
                    "The runtime value %s is of type %s. Only its initial value can change.", runtimeValue.getName(),
                    FlowVariableDataType.INTEGER);
        }
        // The app keeps the key for its browser and sends it back, so the app may always set it.
        runtimeValue.setClientWritable(true);
        if (runtimeValue.getValue() == null) {
            runtimeValue.setValue(SystemRuntimeValues.AB_TESTING_KEY_INITIAL_VALUE);
            return;
        }
        Long value = FlowVariableUtils.toAbTestingKey(runtimeValue.getValue());
        if (value == null) {
            throw handleFlowVariableException("Invalid initial value.",
                    "The initial value of the runtime value %s must be a whole number from %d to %d.",
                    runtimeValue.getName(), SystemRuntimeValues.AB_TESTING_KEY_MIN,
                    SystemRuntimeValues.AB_TESTING_KEY_MAX);
        }
        runtimeValue.setValue(value);
    }

    private static void validateCount(int count, String kind, int max) throws FlowMgtClientException {

        if (count > max) {
            throw handleFlowVariableException("Too many " + kind + "s.",
                    "A flow can define at most %d %ss, but %d are defined.", max, kind, count);
        }
    }

    private static void validateName(FlowVariableDTO variable, String kind, Predicate<String> validName, String nameRule,
                                     Set<String> names) throws FlowMgtClientException {

        if (variable == null || !validName.test(variable.getName())) {
            throw handleFlowVariableException("Invalid " + kind + " name.",
                    "The %s name %s is not valid. %s", kind, variable == null ? null : variable.getName(), nameRule);
        }
        if (!names.add(variable.getName())) {
            throw handleFlowVariableException("Duplicate " + kind + ".",
                    "The %s %s is defined more than once.", kind, variable.getName());
        }
    }
}
