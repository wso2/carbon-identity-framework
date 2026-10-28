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

import org.wso2.carbon.identity.flow.mgt.Constants.SystemVariables;
import org.wso2.carbon.identity.flow.mgt.Constants;
import org.wso2.carbon.identity.flow.mgt.exception.FlowMgtClientException;
import org.wso2.carbon.identity.flow.mgt.model.FlowDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowInputDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowInputDataType;
import org.wso2.carbon.identity.flow.mgt.model.FlowInputsDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowVariableInputDTO;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static org.wso2.carbon.identity.flow.mgt.utils.FlowMgtUtils.handleClientException;
import static org.wso2.carbon.identity.flow.mgt.utils.FlowMgtUtils.handleFlowInputException;

/**
 * Checks the headers, query parameters and variables a flow defines, when the flow is saved.
 * <p>
 * Header names are put in lower case and initial values in the form kept for their type, so what is saved is what
 * the flow runs with.
 */
public final class FlowInputValidator {

    private static final String HEADER = "header";
    private static final String QUERY_PARAM = "query parameter";
    private static final String VARIABLE = "variable";

    private static final String HEADER_NAME_RULE =
            "Use letters, digits, hyphens and underscores, up to 64 characters, starting with a letter or digit.";
    private static final String QUERY_PARAM_NAME_RULE =
            "Use letters, digits, dots, hyphens and underscores, up to 64 characters, starting with a letter, digit or " +
                    "underscore.";
    private static final String VARIABLE_NAME_RULE = "Use only letters and digits, up to 255 characters.";

    private FlowInputValidator() {

    }

    /**
     * Validate the inputs of a flow being saved. A flow that leaves out a system variable is given it first, so it is
     * validated and stored with the rest.
     *
     * @param flowDTO The flow being saved.
     * @throws FlowMgtClientException If any input is not valid.
     */
    public static void validate(FlowDTO flowDTO) throws FlowMgtClientException {

        flowDTO.setFlowInputs(FlowInputUtils.withSystemVariables(flowDTO.getFlowInputs()));
        FlowInputsDTO inputs = flowDTO.getFlowInputs();
        if (inputs == null || inputs.isEmpty()) {
            return;
        }
        if (!FlowMgtUtils.isFlowInputsEnabled()) {
            throw handleClientException(Constants.ErrorMessages.ERROR_CODE_FLOW_INPUTS_DISABLED);
        }
        for (FlowInputDTO header : inputs.getHeaders()) {
            if (header != null) {
                header.setName(FlowInputUtils.normalizeHeaderName(header.getName()));
            }
        }
        validateRequestValues(inputs.getHeaders(), HEADER, FlowInputUtils.getMaxHeaders(),
                FlowInputUtils::isValidHeaderName, HEADER_NAME_RULE, FlowInputUtils::isExcludedHeader);
        validateRequestValues(inputs.getQueryParams(), QUERY_PARAM, FlowInputUtils.getMaxQueryParams(),
                FlowInputUtils::isValidQueryParamName, QUERY_PARAM_NAME_RULE, FlowInputUtils::isExcludedQueryParam);
        validateVariables(inputs.getVariables());
    }

    private static void validateRequestValues(List<FlowInputDTO> inputs, String kind, int max,
                                              Predicate<String> validName, String nameRule,
                                              Predicate<String> excluded) throws FlowMgtClientException {

        validateCount(inputs.size(), kind, max);
        Set<String> names = new HashSet<>();
        for (FlowInputDTO input : inputs) {
            validateName(input, kind, validName, nameRule, names);
            if (excluded.test(input.getName())) {
                throw handleFlowInputException("Excluded " + kind + ".",
                        "The %s %s cannot be read by flows on this server.", kind, input.getName());
            }
            if (input.getDataType() == null || !input.getDataType().isArray()) {
                throw handleFlowInputException("Invalid data type.",
                        "The %s %s must be of type %s or %s, as a request may repeat it.", kind,
                        input.getName(), FlowInputDataType.STRING_ARRAY, FlowInputDataType.NUMBER_ARRAY);
            }
        }
    }

    private static void validateVariables(List<FlowVariableInputDTO> variables) throws FlowMgtClientException {

        int defined = 0;
        for (FlowVariableInputDTO variable : variables) {
            if (variable == null || !FlowInputUtils.isSystemVariable(variable.getName())) {
                defined++;
            }
        }
        validateCount(defined, VARIABLE, FlowInputUtils.getMaxVariables());
        Set<String> names = new HashSet<>();
        for (FlowVariableInputDTO variable : variables) {
            validateName(variable, VARIABLE, FlowInputUtils::isValidVariableName, VARIABLE_NAME_RULE, names);
            if (SystemVariables.AB_TESTING_KEY.equals(variable.getName())) {
                validateAbTestingKey(variable);
                continue;
            }
            if (variable.getDataType() == null) {
                throw handleFlowInputException("Data type not provided.",
                        "The variable %s does not say what type of value it holds.", variable.getName());
            }
            if (variable.getValue() != null) {
                Object value = FlowInputUtils.toValue(variable.getDataType(), variable.getValue());
                if (value == null) {
                    throw handleFlowInputException("Invalid initial value.",
                            "The initial value of the variable %s is not a valid %s, or it is too long or holds " +
                                    "characters that are not allowed.", variable.getName(), variable.getDataType());
                }
                variable.setValue(value);
            }
        }
    }

    private static void validateAbTestingKey(FlowVariableInputDTO variable) throws FlowMgtClientException {

        if (variable.getDataType() != FlowInputDataType.INTEGER || variable.isClientWritable()) {
            throw handleFlowInputException("Invalid system variable.",
                    "The variable %s is of type %s and cannot be set by the app. Only its initial value can " +
                            "change.", variable.getName(), FlowInputDataType.INTEGER);
        }
        if (variable.getValue() == null) {
            variable.setValue(SystemVariables.AB_TESTING_KEY_INITIAL_VALUE);
            return;
        }
        Object value = FlowInputUtils.toValue(FlowInputDataType.INTEGER, variable.getValue());
        if (value == null || (Long) value < SystemVariables.AB_TESTING_KEY_MIN
                || (Long) value > SystemVariables.AB_TESTING_KEY_MAX) {
            throw handleFlowInputException("Invalid initial value.",
                    "The initial value of the variable %s must be a whole number from %d to %d.",
                    variable.getName(), SystemVariables.AB_TESTING_KEY_MIN, SystemVariables.AB_TESTING_KEY_MAX);
        }
        variable.setValue(value);
    }

    private static void validateCount(int count, String kind, int max) throws FlowMgtClientException {

        if (count > max) {
            throw handleFlowInputException("Too many " + kind + "s.",
                    "A flow can define at most %d %ss, but %d are defined.", max, kind, count);
        }
    }

    private static void validateName(FlowInputDTO input, String kind, Predicate<String> validName, String nameRule,
                                     Set<String> names) throws FlowMgtClientException {

        if (input == null || !validName.test(input.getName())) {
            throw handleFlowInputException("Invalid " + kind + " name.",
                    "The %s name %s is not valid. %s", kind, input == null ? null : input.getName(), nameRule);
        }
        if (!names.add(input.getName())) {
            throw handleFlowInputException("Duplicate " + kind + ".",
                    "The %s %s is defined more than once.", kind, input.getName());
        }
    }
}
