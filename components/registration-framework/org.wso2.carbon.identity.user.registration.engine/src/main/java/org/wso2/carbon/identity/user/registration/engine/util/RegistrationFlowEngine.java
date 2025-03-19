/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

package org.wso2.carbon.identity.user.registration.engine.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.input.validation.mgt.exceptions.InputValidationMgtException;
import org.wso2.carbon.identity.input.validation.mgt.model.RulesConfiguration;
import org.wso2.carbon.identity.input.validation.mgt.model.ValidationConfiguration;
import org.wso2.carbon.identity.user.registration.engine.exception.RegistrationEngineException;
import org.wso2.carbon.identity.user.registration.engine.exception.RegistrationEngineServerException;
import org.wso2.carbon.identity.user.registration.engine.graph.PagePromptNode;
import org.wso2.carbon.identity.user.registration.engine.graph.TaskExecutionNode;
import org.wso2.carbon.identity.user.registration.engine.graph.UserChoiceDecisionNode;
import org.wso2.carbon.identity.user.registration.engine.internal.RegistrationFlowEngineDataHolder;
import org.wso2.carbon.identity.user.registration.engine.model.RegistrationContext;
import org.wso2.carbon.identity.user.registration.engine.model.RegistrationStep;
import org.wso2.carbon.identity.user.registration.engine.model.Response;
import org.wso2.carbon.identity.user.registration.mgt.Constants;
import org.wso2.carbon.identity.user.registration.mgt.model.ComponentDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.DataDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.NodeConfig;
import org.wso2.carbon.identity.user.registration.mgt.model.RegistrationGraphConfig;
import org.wso2.carbon.identity.user.registration.mgt.model.ValidationDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.USERNAME;
import static org.wso2.carbon.identity.user.registration.engine.Constants.ERROR;
import static org.wso2.carbon.identity.user.registration.engine.Constants.ErrorMessages.ERROR_CODE_FIRST_NODE_NODE_FOUND;
import static org.wso2.carbon.identity.user.registration.engine.Constants.ErrorMessages.ERROR_CODE_GET_INPUT_VALIDATION_CONFIG_FAILURE;
import static org.wso2.carbon.identity.user.registration.engine.Constants.ErrorMessages.ERROR_CODE_UNSUPPORTED_NODE;
import static org.wso2.carbon.identity.user.registration.engine.Constants.IDENTIFIER;
import static org.wso2.carbon.identity.user.registration.engine.Constants.PASSWORD_KEY;
import static org.wso2.carbon.identity.user.registration.engine.Constants.REDIRECT_URL;
import static org.wso2.carbon.identity.user.registration.engine.Constants.STATUS_COMPLETE;
import static org.wso2.carbon.identity.user.registration.engine.Constants.STATUS_INCOMPLETE;
import static org.wso2.carbon.identity.user.registration.engine.Constants.STATUS_PROMPT_ONLY;
import static org.wso2.carbon.identity.user.registration.engine.Constants.USERNAME_CLAIM_URI;
import static org.wso2.carbon.identity.user.registration.engine.Constants.VALIDATIONS;
import static org.wso2.carbon.identity.user.registration.engine.util.RegistrationFlowEngineUtils.handleServerException;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.StepTypes.REDIRECTION;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.StepTypes.VIEW;

/**
 * Engine to execute the registration flow.
 */
public class RegistrationFlowEngine {

    private static final Log LOG = LogFactory.getLog(RegistrationFlowEngine.class);

    private static final RegistrationFlowEngine instance = new RegistrationFlowEngine();

    // Constants related for OTP field length handling.
    private static final String OTP_LENGTH = "otpLength";
    private static final String OTP_VARIANT = "OTP";
    private static final String LENGTH_CONFIG = "length";

    private RegistrationFlowEngine() {

    }

    public static RegistrationFlowEngine getInstance() {

        return instance;
    }

    /**
     * Execute the registration sequence.
     *
     * @param context Registration context.
     * @return Node response.
     * @throws RegistrationEngineException If an error occurs while executing the registration sequence.
     */
    public RegistrationStep execute(RegistrationContext context)
            throws RegistrationEngineException {

        RegistrationGraphConfig graph = context.getRegGraph();

        String tenantDomain = context.getTenantDomain();
        if (graph.getFirstNodeId() == null) {
            throw handleServerException(ERROR_CODE_FIRST_NODE_NODE_FOUND, graph.getId(), tenantDomain);
        }

        NodeConfig currentNode = context.getCurrentNode();
        if (currentNode == null) {
            LOG.debug("Current node is not set. Setting the first node as the current node and starting the " +
                    "registration sequence.");
            currentNode = graph.getNodeConfigs().get(graph.getFirstNodeId());
        }

        while (currentNode != null) {
            Response nodeResponse = triggerNode(currentNode, context);
            if (STATUS_COMPLETE.equals(nodeResponse.getStatus())) {
                currentNode = moveToNextNode(graph, currentNode);
                context.setCurrentNode(currentNode);
                continue;
            }
            if (STATUS_INCOMPLETE.equals(nodeResponse.getStatus()) &&
                    REDIRECTION.equals(nodeResponse.getType())) {
                return resolveStepDetailsForRedirection(context, nodeResponse);
            }
            RegistrationStep step = resolveStepDetailsForPrompt(graph, currentNode, context, nodeResponse);
            if (STATUS_INCOMPLETE.equals(nodeResponse.getStatus()) && VIEW.equals(nodeResponse.getType())) {
                return step;
            }
            if (STATUS_PROMPT_ONLY.equals(nodeResponse.getStatus())) {
                currentNode = moveToNextNode(graph, currentNode);
                context.setCurrentNode(currentNode);
                return step;
            }
        }
        return new RegistrationStep.Builder()
                .flowId(context.getContextIdentifier())
                .flowStatus(STATUS_COMPLETE)
                .stepType(REDIRECTION)
                .data(new DataDTO.Builder()
                        .url(RegistrationFlowEngineUtils.buildMyAccountAccessURL(context.getTenantDomain()))
                        .build())
                .build();
    }

    /**
     * Set the current node as the previous node of the next node and return the next node.
     *
     * @param currentNode Current node.
     * @return Next node.
     */
    private NodeConfig moveToNextNode(RegistrationGraphConfig regConfig, NodeConfig currentNode) {

        String nextNodeId = currentNode.getNextNodeId();
        NodeConfig nextNode = regConfig.getNodeConfigs().get(nextNodeId);
        if (nextNode != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Current node " + currentNode.getId() + " is completed. "
                        + "Moving to the next node: " + nextNodeId
                        + " and setting " + currentNode.getId() + " as the previous node.");
            }
            nextNode.setPreviousNodeId(currentNode.getId());
        }
        return nextNode;
    }

    /**
     * Trigger the node.
     *
     * @param nodeConfig Node configuration.
     * @param context    Registration context.
     * @return Node response.
     * @throws RegistrationEngineException If an error occurs while triggering the node.
     */
    private Response triggerNode(NodeConfig nodeConfig, RegistrationContext context)
            throws RegistrationEngineException {

        switch (nodeConfig.getType()) {
            case Constants.NodeTypes.DECISION:
                return new UserChoiceDecisionNode().execute(context, nodeConfig);
            case Constants.NodeTypes.TASK_EXECUTION:
                return new TaskExecutionNode().execute(context, nodeConfig);
            case Constants.NodeTypes.PROMPT_ONLY:
                return new PagePromptNode().execute(context, nodeConfig);
            default:
                throw handleServerException(ERROR_CODE_UNSUPPORTED_NODE, nodeConfig.getType(),
                        context.getRegGraph().getId(), context.getTenantDomain());
        }
    }

    private RegistrationStep resolveStepDetailsForPrompt(RegistrationGraphConfig graph, NodeConfig currentNode,
                                                         RegistrationContext context, Response response)
            throws RegistrationEngineServerException {

        DataDTO dataDTO = graph.getNodePageMappings().get(currentNode.getId()).getData();
        handleValidationDTO(dataDTO, context.getTenantDomain());
        handleFieldLengths(dataDTO, response);
        handleError(dataDTO, response);
        return new RegistrationStep.Builder()
                .flowId(context.getContextIdentifier())
                .flowStatus(STATUS_INCOMPLETE)
                .stepType(VIEW)
                .data(dataDTO)
                .build();
    }

    private RegistrationStep resolveStepDetailsForRedirection(RegistrationContext context, Response response) {

        String redirectUrl = response.getAdditionalInfo().get(REDIRECT_URL);
        response.getAdditionalInfo().remove(REDIRECT_URL);
        return new RegistrationStep.Builder()
                .flowId(context.getContextIdentifier())
                .flowStatus(STATUS_INCOMPLETE)
                .stepType(REDIRECTION)
                .data(new DataDTO.Builder()
                        .url(redirectUrl)
                        .additionalData(response.getAdditionalInfo())
                        .requiredParams(response.getRequiredData())
                        .build())
                .build();
    }

    private void handleError(DataDTO dataDTO, Response response) {

        if (StringUtils.isNotBlank(response.getError())) {
            dataDTO.addAdditionalData(ERROR, response.getError());
        }
    }

    private void handleValidationDTO(DataDTO dataDTO, String tenantDomain)
            throws RegistrationEngineServerException {

        List<String> expectedInputs = getInputsFromDataDTO(dataDTO);
        if (expectedInputs == null || expectedInputs.isEmpty()) {
            return;
        }
        Map<String, ArrayList<ValidationDTO>> validationMap = new HashMap<>();

        // Fetch validations for expected inputs upfront to avoid repeated calls.
        for (String input : expectedInputs) {
            if (USERNAME_CLAIM_URI.equals(input) || PASSWORD_KEY.equals(input)) {
                validationMap.put(input, getValidationDTOs(tenantDomain, input));
            }
        }
        // Process all components and apply validations at once.
        processComponentValidations(dataDTO.getComponents(), validationMap);
    }

    /**
     * Handle OTP field lengths. If the response contains the OTP length, set the length for the OTP fields.
     *
     * @param dataDTO  DataDTO.
     * @param response Response.
     */
    private void handleFieldLengths(DataDTO dataDTO, Response response) {

        if (response == null || response.getAdditionalInfo() == null ||
                !response.getAdditionalInfo().containsKey(OTP_LENGTH)) {
            return;
        }
        int otpLength = Integer.parseInt(response.getAdditionalInfo().get(OTP_LENGTH));
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
            throws RegistrationEngineServerException {

        if (!USERNAME_CLAIM_URI.equals(key) && !PASSWORD_KEY.equals(key)) {
            return new ArrayList<>();
        }

        if (USERNAME_CLAIM_URI.equals(key)) {
            // Use "username" as the key to fetch the input validation configuration.
            key = USERNAME;
        }

        Map<String, ValidationDTO> validationDTOs = new HashMap<>();

        try {
            List<ValidationConfiguration> validationConfigurations = RegistrationFlowEngineDataHolder.getInstance()
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

        processValidations(config.getRules(), "RULE", validationDTOs);
    }

    private void processRegexValidations(ValidationConfiguration config, Map<String, ValidationDTO> validationDTOs) {

        processValidations(config.getRegEx(), "REGEX", validationDTOs);
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

    private List<String> getInputsFromDataDTO(DataDTO dataDTO) {

        if (dataDTO == null || dataDTO.getComponents() == null) {
            return new ArrayList<>();
        }
        return dataDTO.getComponents().stream()
                .flatMap(this::extractInputs)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private Stream<String> extractInputs(ComponentDTO componentDTO) {

        if (componentDTO == null) {
            return Stream.empty();
        }

        if (Constants.ComponentTypes.INPUT.equals(componentDTO.getType())) {
            String identifier = getIdentifier(componentDTO);
            return identifier != null ? Stream.of(identifier) : Stream.empty();
        }

        if (Constants.ComponentTypes.FORM.equals(componentDTO.getType()) && componentDTO.getComponents() != null) {
            return componentDTO.getComponents().stream()
                    .filter(component -> Constants.ComponentTypes.INPUT.equals(component.getType()))
                    .map(this::getIdentifier)
                    .filter(Objects::nonNull);
        }
        return Stream.empty();
    }

    private String getIdentifier(ComponentDTO componentDTO) {

        Object identifier = componentDTO.getConfigs().get(IDENTIFIER);
        return identifier != null ? String.valueOf(identifier) : null;
    }
}
