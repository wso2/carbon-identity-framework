/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.flow.execution.engine.validation;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineServerException;
import org.wso2.carbon.identity.flow.execution.engine.internal.FlowExecutionEngineDataHolder;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.NodeResponse;
import org.wso2.carbon.identity.flow.execution.engine.util.FlowExecutionEngineUtils;
import org.wso2.carbon.identity.flow.mgt.Constants;
import org.wso2.carbon.identity.flow.mgt.model.ComponentDTO;
import org.wso2.carbon.identity.flow.mgt.model.DataDTO;
import org.wso2.carbon.identity.flow.mgt.model.ValidationDTO;
import org.wso2.carbon.identity.input.validation.mgt.exceptions.InputValidationMgtException;
import org.wso2.carbon.identity.input.validation.mgt.model.RulesConfiguration;
import org.wso2.carbon.identity.input.validation.mgt.model.ValidationConfiguration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.flow.execution.engine.Constants.CLAIM_URI_PREFIX;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.DEFAULT_ACTION;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_GET_INPUT_VALIDATION_CONFIG_FAILURE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_INVALID_ACTION_ID;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_INVALID_USER_INPUT;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.IDENTIFIER;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.LENGTH_CONFIG;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.OTP_LENGTH;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.OTP_VARIANT;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.PASSWORD_KEY;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.REQUIRED;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.USERNAME_CLAIM_URI;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.VALIDATIONS;
import static org.wso2.carbon.identity.flow.execution.engine.util.FlowExecutionEngineUtils.handleServerException;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.REGEX;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.RULES;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.USERNAME;

/**
 * This class is responsible for validating user inputs during the flow execution process.
 * It retrieves validation configurations and applies them to the input fields.
 */
public class InputValidationService {

    private static final Log LOG = LogFactory.getLog(InputValidationService.class);
    private static final InputValidationService instance = new InputValidationService();
    public static final String CONFIRMATION_CODE = "confirmationCode";

    private InputValidationService() {

    }

    public static InputValidationService getInstance() {

        return instance;
    }

    /**
     * Validate the required inputs.
     *
     * @param context Flow context.
     * @throws FlowEngineException Flow engine exception.
     */
    public void validateInputs(FlowExecutionContext context) throws FlowEngineException {

        // Check if the actionId is empty and set it to default action.
        String actionId = context.getCurrentActionId();
        actionId = StringUtils.isBlank(actionId) ? DEFAULT_ACTION : actionId;
        if (context.getCurrentStepInputs() == null || context.getCurrentStepInputs().isEmpty()) {
            return;
        }

        String flowType = context.getFlowType();
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Validating inputs for flow type: %s with action ID: %s", flowType, actionId));
        }
        if (context.getCurrentStepInputs().get(actionId) == null) {
            throw FlowExecutionEngineUtils.handleClientException(flowType, ERROR_CODE_INVALID_ACTION_ID, actionId);
        }

        // Fail if required inputs are not there.
        if (context.getCurrentRequiredInputs().get(actionId) != null) {
            for (String requiredInput : context.getCurrentRequiredInputs().get(actionId)) {
                if (context.getUserInputData().get(requiredInput) == null ||
                        context.getUserInputData().get(requiredInput).isEmpty()) {
                    throw FlowExecutionEngineUtils.handleClientException(flowType, ERROR_CODE_INVALID_USER_INPUT,
                            flowType);
                }
            }
        }

        // Fail if extra inputs are there.
        for (Map.Entry<String, String> userInput : context.getUserInputData().entrySet()) {
            if (!context.getCurrentStepInputs().get(actionId).contains(userInput.getKey())) {
                throw FlowExecutionEngineUtils.handleClientException(flowType, ERROR_CODE_INVALID_USER_INPUT, flowType);
            }
        }
    }

    /**
     * Prepare the step inputs for the flow execution process.
     *
     * @param dataDTO Data transfer object containing components.
     * @param context Flow context.
     * @throws FlowEngineServerException If an error occurs while processing the inputs.
     */
    public void prepareStepInputs(DataDTO dataDTO, FlowExecutionContext context)
            throws FlowEngineServerException {

        if (dataDTO == null) {
            return;
        }
        processStepInputs(dataDTO, context);
        processFieldLengths(dataDTO, context.getCurrentNodeResponse());
        processValidations(dataDTO, context);
    }

    /**
     * Processes the current user inputs and adds the user claims to the registering user.
     * Clear the user input data after processing.
     *
     * @param context Flow context.
     */
    public void handleUserInputs(FlowExecutionContext context) {

        context.getUserInputData().forEach(
                (key, value) -> {
                    if (key.startsWith(CLAIM_URI_PREFIX)) {
                        context.getFlowUser().addClaim(key, value);
                    }
                }
        );
        context.getFlowUser().getClaims().forEach(
                (key, value) -> {
                    context.getUserInputData().remove(key);
                }
        );
    }

    /**
     * Clears the user inputs from the flow context.
     *
     * @param context Flow context.
     */
    public void clearUserInputs(FlowExecutionContext context) {

        if (context != null) {
            context.getUserInputData().clear();
        }
    }

    /**
     * Processes the step inputs and sets them in the context.
     *
     * @param dataDTO Data transfer object containing components.
     * @param context Flow context.
     */
    private void processStepInputs(DataDTO dataDTO, FlowExecutionContext context) {

        // Clear the current step inputs and required inputs.
        context.getCurrentStepInputs().clear();
        context.getCurrentRequiredInputs().clear();

        if (dataDTO.getComponents() != null) {
            for (ComponentDTO component : dataDTO.getComponents()) {

                if (Constants.ComponentTypes.BUTTON.equalsIgnoreCase(component.getType())) {
                    context.getCurrentStepInputs().put(component.getId(), new HashSet<>());
                }

                if (!Constants.ComponentTypes.FORM.equalsIgnoreCase(component.getType())) {
                    continue;
                }

                List<ComponentDTO> children = component.getComponents();
                if (children == null || children.isEmpty()) {
                    continue;
                }

                Set<String> inputIdentifiers = new HashSet<>();
                Set<String> requiredInputIdentifiers = new HashSet<>();

                for (ComponentDTO child : children) {
                    if (Constants.ComponentTypes.INPUT.equalsIgnoreCase(child.getType())) {
                        String identifier = getIdentifier(child);
                        if (identifier != null) {
                            if (isRequiredField(child)) {
                                requiredInputIdentifiers.add(identifier);
                            }
                            inputIdentifiers.add(identifier);
                        }
                    }
                    if (Constants.ComponentTypes.BUTTON.equalsIgnoreCase(child.getType())) {
                        // If the button has an executor, add the required inputs defined from the executor.
                        // Ideally these should be available within the form.
                        if (child.getAction() != null && child.getAction().getExecutor() != null &&
                                dataDTO.getRequiredParams() != null) {
                            requiredInputIdentifiers.addAll(dataDTO.getRequiredParams());
                        }
                        context.getCurrentStepInputs().put(child.getId(), inputIdentifiers);
                        context.getCurrentRequiredInputs().put(child.getId(), requiredInputIdentifiers);
                    }
                }
            }
        }

        // If the dataDTO has required params, add them to the current step inputs and required inputs.
        if ((dataDTO.getComponents() == null || dataDTO.getComponents().isEmpty()) &&
                dataDTO.getRequiredParams() != null && !dataDTO.getRequiredParams().isEmpty()) {
            handleRequiredInputs(dataDTO, context);
        }

        // If there are no action components, set the required params as the step inputs with the default action.
        // This is to handle cases where the flow temporarily ends with static view and expects inputs when the flow
        // resumes.
        if ((dataDTO.getRequiredParams() != null && !dataDTO.getRequiredParams().isEmpty()) &&
                context.getCurrentStepInputs().isEmpty()) {
            handleRequiredInputs(dataDTO, context);
        }

        handleOptionalInputs(dataDTO, context);
    }

    /**
     * Handle required inputs by adding them to the current step inputs and required inputs in the context.
     *
     * @param dataDTO Data transfer object containing components and required parameters.
     * @param context Flow context.
     */
    private static void handleRequiredInputs(DataDTO dataDTO, FlowExecutionContext context) {

        context.getCurrentRequiredInputs().put(DEFAULT_ACTION, new HashSet<>(dataDTO.getRequiredParams()));
        context.getCurrentStepInputs().put(DEFAULT_ACTION, new HashSet<>(dataDTO.getRequiredParams()));
    }

    /**
     * Handle optional inputs by adding them to the current step inputs in the context.
     *
     * @param dataDTO Data transfer object containing components and optional parameters.
     * @param context Flow context.
     */
    private static void handleOptionalInputs(DataDTO dataDTO, FlowExecutionContext context) {

        if (dataDTO.getOptionalParams() == null || dataDTO.getOptionalParams().isEmpty()) {
            return;
        }

        if (context.getCurrentStepInputs().isEmpty()) {
            context.getCurrentStepInputs().put(DEFAULT_ACTION, new HashSet<>());
        }

        for (Map.Entry<String, Set<String>> entry : context.getCurrentStepInputs().entrySet()) {
            entry.getValue().addAll(dataDTO.getOptionalParams());
        }
    }


    /**
     * Handle OTP field lengths. If the nodeResponse contains the OTP length, set the length for the OTP fields.
     *
     * @param dataDTO      DataDTO.
     * @param nodeResponse NodeResponse.
     */
    private void processFieldLengths(DataDTO dataDTO, NodeResponse nodeResponse) {

        if (nodeResponse == null || nodeResponse.getAdditionalInfo() == null ||
                !nodeResponse.getAdditionalInfo().containsKey(OTP_LENGTH)) {
            return;
        }
        int otpLength = Integer.parseInt(nodeResponse.getAdditionalInfo().get(OTP_LENGTH));
        for (ComponentDTO component : dataDTO.getComponents()) {
            if (Constants.ComponentTypes.INPUT.equals(component.getType())) {
                if (OTP_VARIANT.equals(component.getVariant())) {
                    component.addConfig(LENGTH_CONFIG, otpLength);
                }
            }
            if (Constants.ComponentTypes.FORM.equals(component.getType())) {
                for (ComponentDTO nestedComponent : component.getComponents()) {
                    if (Constants.ComponentTypes.INPUT.equals(nestedComponent.getType())) {
                        if (OTP_VARIANT.equals(nestedComponent.getVariant())) {
                            nestedComponent.addConfig(LENGTH_CONFIG, otpLength);
                        }
                    }
                }
            }
        }
    }

    /**
     * Processes the validations for the given DataDTO and inputs.
     *
     * @param dataDTO Data transfer object containing components.
     * @param context Flow context.
     * @throws FlowEngineServerException If an error occurs while processing the validations.
     */
    private void processValidations(DataDTO dataDTO, FlowExecutionContext context)
            throws FlowEngineServerException {

        Map<String, Set<String>> inputs = context.getCurrentStepInputs();
        if (inputs.isEmpty()) {
            return;
        }

        List<String> allInputs = inputs.values().stream()
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());
        Map<String, ArrayList<ValidationDTO>> validationMap = new HashMap<>();
        // Fetch validations for expected inputs upfront to avoid repeated calls.
        for (String input : allInputs) {
            if (USERNAME_CLAIM_URI.equals(input) || PASSWORD_KEY.equals(input)) {
                validationMap.put(input, getValidationDTOs(context.getTenantDomain(), input));
            }
        }
        // Process all components and apply validations at once.
        processComponentValidations(dataDTO.getComponents(), validationMap);
    }

    private void processComponentValidations(List<ComponentDTO> components,
                                             Map<String, ArrayList<ValidationDTO>> validationMap) {

        if (components == null || components.isEmpty()) {
            return;
        }

        for (ComponentDTO component : components) {
            if (Constants.ComponentTypes.INPUT.equals(component.getType())) {
                String identifier = (String) component.getConfigs().get(IDENTIFIER);
                applyValidationIfNeeded(component, identifier, validationMap);
            } else if (Constants.ComponentTypes.FORM.equals(component.getType())) {
                // Process nested components.
                processComponentValidations(component.getComponents(), validationMap);
            }
        }
    }

    private void applyValidationIfNeeded(ComponentDTO component, String identifier,
                                         Map<String, ArrayList<ValidationDTO>> validationMap) {

        if (identifier != null && validationMap.containsKey(identifier)) {
            component.getConfigs().put(VALIDATIONS, validationMap.get(identifier));
        }
    }

    private ArrayList<ValidationDTO> getValidationDTOs(String tenantDomain, String key)
            throws FlowEngineServerException {

        if (!USERNAME_CLAIM_URI.equals(key) && !PASSWORD_KEY.equals(key)) {
            return new ArrayList<>();
        }

        if (USERNAME_CLAIM_URI.equals(key)) {
            // Check whether the username validation is enabled.
            boolean isUsernameValidationEnabled = Boolean.parseBoolean(IdentityUtil.getProperty(
                    org.wso2.carbon.identity.flow.execution.engine.Constants.IS_USERNAME_VALIDATION_ENABLED));
            if (!isUsernameValidationEnabled) {
                LOG.debug("Username validation is disabled. Skip adding validation rules for the username.");
                return new ArrayList<>();
            }

            // Use "username" as the key to fetch the input validation configuration.
            key = USERNAME;
        }

        Map<String, ValidationDTO> validationDTOs = new HashMap<>();

        try {
            List<ValidationConfiguration> validationConfigurations = FlowExecutionEngineDataHolder.getInstance()
                    .getInputValidationManagementService().getInputValidationConfiguration(tenantDomain);

            for (ValidationConfiguration config : validationConfigurations) {
                if (!key.equals(config.getField())) {
                    continue;
                }

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Validation configuration found for field: " + key);
                }

                processRuleValidations(config, validationDTOs);
                processRegexValidations(config, validationDTOs);
            }
        } catch (InputValidationMgtException e) {
            throw handleServerException(ERROR_CODE_GET_INPUT_VALIDATION_CONFIG_FAILURE, tenantDomain);
        }
        return validationDTOs.isEmpty() ? new ArrayList<>() : new ArrayList<>(validationDTOs.values());
    }

    private void processRuleValidations(ValidationConfiguration config, Map<String, ValidationDTO> validationDTOs) {

        processValidations(config.getRules(), RULES, validationDTOs);
    }

    private void processRegexValidations(ValidationConfiguration config, Map<String, ValidationDTO> validationDTOs) {

        processValidations(config.getRegEx(), REGEX, validationDTOs);
    }

    private void processValidations(List<RulesConfiguration> rules, String type, Map<String,
            ValidationDTO> validationDTOs) {

        if (rules == null || rules.isEmpty()) {
            return;
        }

        for (RulesConfiguration rule : rules) {
            String validatorName = rule.getValidatorName();
            List<ValidationDTO.Condition> conditions = createConditionsFromProperties(rule.getProperties());

            ValidationDTO validationDTO = validationDTOs.computeIfAbsent(validatorName, k -> {
                ValidationDTO newValidation = new ValidationDTO();
                newValidation.setName(validatorName);
                newValidation.setType(type);
                newValidation.setConditions(new ArrayList<>());
                return newValidation;
            });
            validationDTO.getConditions().addAll(conditions);
        }
    }

    private List<ValidationDTO.Condition> createConditionsFromProperties(Map<String, String> properties) {

        if (properties == null || properties.isEmpty()) {
            return new ArrayList<>();
        }
        return properties.entrySet().stream()
                .map(entry -> new ValidationDTO.Condition(entry.getKey(), entry.getValue()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private String getIdentifier(ComponentDTO componentDTO) {

        Object identifier = componentDTO.getConfigs().get(IDENTIFIER);
        return identifier != null ? String.valueOf(identifier) : null;
    }

    private boolean isRequiredField(ComponentDTO componentDTO) {

        Object isRequired = componentDTO.getConfigs().get(REQUIRED);
        return isRequired != null && (boolean) isRequired;
    }
}
