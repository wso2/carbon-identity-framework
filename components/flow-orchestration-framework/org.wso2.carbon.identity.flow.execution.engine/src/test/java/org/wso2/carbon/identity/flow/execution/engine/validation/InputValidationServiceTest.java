/*
 * Copyright (c) 2025-2026, WSO2 LLC. (http://www.wso2.com).
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

import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimValidationUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineClientException;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineServerException;
import org.wso2.carbon.identity.flow.execution.engine.internal.FlowExecutionEngineDataHolder;
import org.wso2.carbon.identity.flow.execution.engine.model.ExecutorResponse;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.mgt.Constants;
import org.wso2.carbon.identity.flow.mgt.model.ActionDTO;
import org.wso2.carbon.identity.flow.mgt.model.ComponentDTO;
import org.wso2.carbon.identity.flow.mgt.model.DataDTO;
import org.wso2.carbon.identity.flow.mgt.model.ExecutorDTO;
import org.wso2.carbon.identity.flow.mgt.model.GraphConfig;
import org.wso2.carbon.identity.flow.mgt.model.NodeConfig;
import org.wso2.carbon.identity.input.validation.mgt.exceptions.InputValidationMgtClientException;
import org.wso2.carbon.identity.input.validation.mgt.exceptions.InputValidationMgtException;
import org.wso2.carbon.identity.input.validation.mgt.model.RulesConfiguration;
import org.wso2.carbon.identity.input.validation.mgt.model.ValidationConfiguration;
import org.wso2.carbon.identity.input.validation.mgt.model.ValidationContext;
import org.wso2.carbon.identity.input.validation.mgt.model.Validator;
import org.wso2.carbon.identity.input.validation.mgt.services.InputValidationManagementService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.CLAIM_URI_PREFIX;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.DEFAULT_ACTION;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_CLAIM_META_DATA_NOT_FOUND;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_CLAIM_REGEX_VALIDATION_FAILED;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_CLAIM_UNIQUENESS_VALIDATION_FAILED;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_PASSWORD_FORMAT_VALIDATION_FAILED;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_USERNAME_FORMAT_VALIDATION_FAILED;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.IS_USERNAME_VALIDATION_ENABLED;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.PASSWORD_KEY;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorStatus.STATUS_COMPLETE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorStatus.STATUS_RETRY;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorStatus.STATUS_USER_INPUT_REQUIRED;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.IDENTIFIER;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.USERNAME_CLAIM_URI;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.VALIDATIONS;

public class InputValidationServiceTest {

    public static final String ACTION_1 = "action1";
    private FlowExecutionContext FlowExecutionContext;
    private InputValidationService inputValidationService;
    private GraphConfig defaultGraph;

    @BeforeMethod
    public void setUp() {

        defaultGraph = new GraphConfig();
        inputValidationService = InputValidationService.getInstance();
    }

    @AfterMethod
    public void tearDown() {

    }

    @Test(expectedExceptions = FlowEngineClientException.class)
    @SuppressWarnings("deprecation")
    public void testValidateInputsMissingInputs() throws FlowEngineException {

        FlowExecutionContext = initiateFlowContext();
        FlowExecutionContext.setGraphConfig(defaultGraph);
        Map<String, Set<String>> inputData = new HashMap<>();
        Set<String> value = new HashSet<>();
        value.add("input1");
        inputData.put(ACTION_1, value);
        FlowExecutionContext.setCurrentRequiredInputs(inputData);
        FlowExecutionContext.setCurrentStepInputs(inputData);

        Map<String, String> userInputData = new HashMap<>();
        userInputData.put("input2", "value2");
        FlowExecutionContext.getUserInputData().putAll(userInputData);
        FlowExecutionContext.setCurrentActionId(ACTION_1);
        inputValidationService.validateInputs(FlowExecutionContext);
    }

    @Test(expectedExceptions = FlowEngineClientException.class)
    @SuppressWarnings("deprecation")
    public void testValidateInputsExtraInputs() throws FlowEngineException {

        FlowExecutionContext = initiateFlowContext();
        FlowExecutionContext.setGraphConfig(defaultGraph);
        Map<String, Set<String>> inputData = new HashMap<>();
        Set<String> value = new HashSet<>();
        value.add("input1");
        inputData.put(ACTION_1, value);
        FlowExecutionContext.setCurrentRequiredInputs(inputData);
        FlowExecutionContext.setCurrentStepInputs(inputData);

        Map<String, String> userInputData = new HashMap<>();
        userInputData.put("input1", "value1");
        userInputData.put("input2", "value2");
        FlowExecutionContext.getUserInputData().putAll(userInputData);
        FlowExecutionContext.setCurrentActionId(ACTION_1);
        inputValidationService.validateInputs(FlowExecutionContext);
    }

    @Test
    public void testHandleStepInputsViewStep() throws FlowEngineServerException {

        FlowExecutionContext = initiateFlowContext();
        FlowExecutionContext.setGraphConfig(defaultGraph);
        List<ComponentDTO> formComponents = new ArrayList<>();
        ComponentDTO input1 = new ComponentDTO.Builder()
                .type(Constants.ComponentTypes.INPUT)
                .configs(Collections.singletonMap("identifier", "input1"))
                .build();
        Map<String, Object> configs = new HashMap<>();
        configs.put("identifier", "input2");
        configs.put("required", true);
        ComponentDTO input2 = new ComponentDTO.Builder().type(Constants.ComponentTypes.INPUT).configs(configs).build();
        ComponentDTO button1 = new ComponentDTO.Builder()
                .type(Constants.ComponentTypes.BUTTON)
                .id("action1")
                .action(new ActionDTO.Builder().type(Constants.ActionTypes.EXECUTOR).nextId("action2").build())
                .build();
        formComponents.add(input1);
        formComponents.add(input2);
        formComponents.add(button1);
        ComponentDTO formDTO = new ComponentDTO.Builder()
                .type(Constants.ComponentTypes.FORM)
                .components(formComponents)
                .build();
        ComponentDTO redirectionDTO = new ComponentDTO.Builder()
                .type(Constants.ComponentTypes.BUTTON)
                .id("action2")
                .action(new ActionDTO.Builder().type(Constants.ActionTypes.EXECUTOR).nextId("action3").build())
                .build();
        List<ComponentDTO> components = new ArrayList<>();
        components.add(formDTO);
        components.add(redirectionDTO);
        DataDTO dataDTO = new DataDTO.Builder()
                .components(components)
                .requiredParams(Collections.singletonList("input3"))
                .build();
        inputValidationService.prepareStepInputs(dataDTO, FlowExecutionContext);
        Assert.assertEquals(FlowExecutionContext.getCurrentStepInputs().size(), 2);
        Assert.assertEquals(FlowExecutionContext.getCurrentStepInputs().get("action1").size(), 2);
        Assert.assertTrue(FlowExecutionContext.getCurrentStepInputs().get("action1").contains("input1"));
        Assert.assertTrue(FlowExecutionContext.getCurrentStepInputs().get("action1").contains("input2"));
        Assert.assertEquals(FlowExecutionContext.getCurrentRequiredInputs().get("action1").size(), 1);
        Assert.assertTrue(FlowExecutionContext.getCurrentRequiredInputs().get("action1").contains("input2"));
    }

    @Test
    public void testHandleStepInputsRedirectionStep() throws FlowEngineServerException {

        FlowExecutionContext = initiateFlowContext();
        FlowExecutionContext.setGraphConfig(defaultGraph);
        DataDTO dataDTO = new DataDTO.Builder()
                .requiredParams(Collections.singletonList("input1"))
                .optionalParams(Collections.singletonList("input2"))
                .build();
        inputValidationService.prepareStepInputs(dataDTO, FlowExecutionContext);
        Assert.assertEquals(FlowExecutionContext.getCurrentStepInputs().size(), 1);
        Assert.assertEquals(FlowExecutionContext.getCurrentStepInputs().get(DEFAULT_ACTION).size(), 2);
        Assert.assertTrue(FlowExecutionContext.getCurrentStepInputs().get(DEFAULT_ACTION).contains("input1"));
        Assert.assertTrue(FlowExecutionContext.getCurrentStepInputs().get(DEFAULT_ACTION).contains("input2"));
    }

    @Test
    public void testHandleUserInputs() {

        FlowExecutionContext = initiateFlowContext();
        FlowExecutionContext.setGraphConfig(defaultGraph);
        Map<String, String> userInputData = new HashMap<>();
        userInputData.put(CLAIM_URI_PREFIX + "input1", "value1");
        userInputData.put("input2", "value2");
        FlowExecutionContext.getUserInputData().putAll(userInputData);
        inputValidationService.handleUserInputs(FlowExecutionContext);
        Assert.assertEquals(FlowExecutionContext.getFlowUser().getClaims().size(), 1);
        Assert.assertEquals(FlowExecutionContext.getUserInputData().size(), 1);
        Assert.assertEquals(FlowExecutionContext.getFlowUser().getClaims().get(CLAIM_URI_PREFIX + "input1"),
                "value1");
        Assert.assertEquals(FlowExecutionContext.getUserInputData().get("input2"), "value2");
    }

    @Test
    public void testPrepareStepInputsWithUsernameValidationDisabled() throws Exception {

        FlowExecutionContext = initiateFlowContext();
        FlowExecutionContext.setGraphConfig(defaultGraph);

        // Mock the InputValidationManagementService.
        InputValidationManagementService mockValidationService = mock(InputValidationManagementService.class);
        FlowExecutionEngineDataHolder.getInstance().setInputValidationManagementService(mockValidationService);

        // Create a form with username input.
        List<ComponentDTO> formComponents = new ArrayList<>();
        Map<String, Object> usernameConfig = new HashMap<>();
        usernameConfig.put(IDENTIFIER, USERNAME_CLAIM_URI);
        usernameConfig.put("required", true);
        ComponentDTO usernameInput = new ComponentDTO.Builder()
                .type(Constants.ComponentTypes.INPUT)
                .configs(usernameConfig)
                .build();

        ComponentDTO button = new ComponentDTO.Builder()
                .type(Constants.ComponentTypes.BUTTON)
                .id("submitButton")
                .action(new ActionDTO.Builder().type(Constants.ActionTypes.EXECUTOR).nextId("action2").build())
                .build();

        formComponents.add(usernameInput);
        formComponents.add(button);

        ComponentDTO formDTO = new ComponentDTO.Builder()
                .type(Constants.ComponentTypes.FORM)
                .components(formComponents)
                .build();

        List<ComponentDTO> components = new ArrayList<>();
        components.add(formDTO);

        DataDTO dataDTO = new DataDTO.Builder()
                .components(components)
                .build();

        // Mock IdentityUtil to return false for username validation enabled.
        try (MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class)) {
            identityUtilMock.when(() -> IdentityUtil.getProperty(
                            org.wso2.carbon.identity.flow.execution.engine.Constants.IS_USERNAME_VALIDATION_ENABLED))
                    .thenReturn("false");

            // Execute the method.
            inputValidationService.prepareStepInputs(dataDTO, FlowExecutionContext);

            // Verify that the username input does not have validations added.
            ComponentDTO processedForm = dataDTO.getComponents().get(0);
            ComponentDTO processedUsernameInput = processedForm.getComponents().get(0);
            Object validations = processedUsernameInput.getConfigs().get(VALIDATIONS);

            // Validations should be null or empty since username validation is disabled.
            Assert.assertTrue(validations == null || ((List<?>) validations).isEmpty(),
                    "Username field should not have validations when username validation is disabled");
        }
    }

    @Test
    public void testPrepareStepInputsWithUsernameValidationEnabled() throws Exception {

        FlowExecutionContext = initiateFlowContext();
        FlowExecutionContext.setGraphConfig(defaultGraph);

        // Mock the InputValidationManagementService.
        InputValidationManagementService mockValidationService = mock(InputValidationManagementService.class);
        FlowExecutionEngineDataHolder.getInstance().setInputValidationManagementService(mockValidationService);

        // Create mock validation configurations for username.
        List<ValidationConfiguration> validationConfigurations = new ArrayList<>();
        ValidationConfiguration usernameValidationConfig = mock(ValidationConfiguration.class);
        when(usernameValidationConfig.getField()).thenReturn("username");

        // Mock rules for username validation.
        RulesConfiguration rulesConfig = mock(RulesConfiguration.class);
        when(rulesConfig.getValidatorName()).thenReturn("length");
        Map<String, String> properties = new HashMap<>();
        properties.put("min", "3");
        properties.put("max", "50");
        when(rulesConfig.getProperties()).thenReturn(properties);

        List<RulesConfiguration> rules = new ArrayList<>();
        rules.add(rulesConfig);
        when(usernameValidationConfig.getRules()).thenReturn(rules);
        when(usernameValidationConfig.getRegEx()).thenReturn(new ArrayList<>());

        validationConfigurations.add(usernameValidationConfig);

        when(mockValidationService.getInputValidationConfiguration(anyString()))
                .thenReturn(validationConfigurations);

        // Create a form with username input.
        List<ComponentDTO> formComponents = new ArrayList<>();
        Map<String, Object> usernameConfig = new HashMap<>();
        usernameConfig.put(IDENTIFIER, USERNAME_CLAIM_URI);
        usernameConfig.put("required", true);
        ComponentDTO usernameInput = new ComponentDTO.Builder()
                .type(Constants.ComponentTypes.INPUT)
                .configs(usernameConfig)
                .build();

        ComponentDTO button = new ComponentDTO.Builder()
                .type(Constants.ComponentTypes.BUTTON)
                .id("submitButton")
                .action(new ActionDTO.Builder().type(Constants.ActionTypes.EXECUTOR).nextId("action2").build())
                .build();

        formComponents.add(usernameInput);
        formComponents.add(button);

        ComponentDTO formDTO = new ComponentDTO.Builder()
                .type(Constants.ComponentTypes.FORM)
                .components(formComponents)
                .build();

        List<ComponentDTO> components = new ArrayList<>();
        components.add(formDTO);

        DataDTO dataDTO = new DataDTO.Builder()
                .components(components)
                .build();

        // Mock IdentityUtil to return true for username validation enabled.
        try (MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class)) {
            identityUtilMock.when(() -> IdentityUtil.getProperty(
                            org.wso2.carbon.identity.flow.execution.engine.Constants.IS_USERNAME_VALIDATION_ENABLED))
                    .thenReturn("true");

            // Execute the method.
            inputValidationService.prepareStepInputs(dataDTO, FlowExecutionContext);

            // Verify that the username input has validations added.
            ComponentDTO processedForm = dataDTO.getComponents().get(0);
            ComponentDTO processedUsernameInput = processedForm.getComponents().get(0);
            Object validations = processedUsernameInput.getConfigs().get(VALIDATIONS);

            // Validations should be present since username validation is enabled.
            Assert.assertNotNull(validations, "Username field should have validations when username validation is enabled");
            Assert.assertTrue(validations instanceof List, "Validations should be a list");
            List<?> validationsList = (List<?>) validations;
            Assert.assertFalse(validationsList.isEmpty(), "Username field should have at least one validation rule");
        }
    }

    @Test
    public void testPrepareStepInputsWithPasswordValidation() throws Exception {

        FlowExecutionContext = initiateFlowContext();
        FlowExecutionContext.setGraphConfig(defaultGraph);

        // Mock the InputValidationManagementService.
        InputValidationManagementService mockValidationService = mock(InputValidationManagementService.class);
        FlowExecutionEngineDataHolder.getInstance().setInputValidationManagementService(mockValidationService);

        // Create mock validation configurations for password.
        List<ValidationConfiguration> validationConfigurations = new ArrayList<>();
        ValidationConfiguration passwordValidationConfig = mock(ValidationConfiguration.class);
        when(passwordValidationConfig.getField()).thenReturn("password");

        // Mock rules for password validation.
        RulesConfiguration rulesConfig = mock(RulesConfiguration.class);
        when(rulesConfig.getValidatorName()).thenReturn("length");
        Map<String, String> properties = new HashMap<>();
        properties.put("min", "8");
        when(rulesConfig.getProperties()).thenReturn(properties);

        List<RulesConfiguration> rules = new ArrayList<>();
        rules.add(rulesConfig);
        when(passwordValidationConfig.getRules()).thenReturn(rules);
        when(passwordValidationConfig.getRegEx()).thenReturn(new ArrayList<>());

        validationConfigurations.add(passwordValidationConfig);

        when(mockValidationService.getInputValidationConfiguration(anyString()))
                .thenReturn(validationConfigurations);

        // Create a form with password input.
        List<ComponentDTO> formComponents = new ArrayList<>();
        Map<String, Object> passwordConfig = new HashMap<>();
        passwordConfig.put(IDENTIFIER, "password");
        passwordConfig.put("required", true);
        ComponentDTO passwordInput = new ComponentDTO.Builder()
                .type(Constants.ComponentTypes.INPUT)
                .configs(passwordConfig)
                .build();

        ComponentDTO button = new ComponentDTO.Builder()
                .type(Constants.ComponentTypes.BUTTON)
                .id("submitButton")
                .action(new ActionDTO.Builder().type(Constants.ActionTypes.EXECUTOR).nextId("action2").build())
                .build();

        formComponents.add(passwordInput);
        formComponents.add(button);

        ComponentDTO formDTO = new ComponentDTO.Builder()
                .type(Constants.ComponentTypes.FORM)
                .components(formComponents)
                .build();

        List<ComponentDTO> components = new ArrayList<>();
        components.add(formDTO);

        DataDTO dataDTO = new DataDTO.Builder()
                .components(components)
                .build();

        // Mock IdentityUtil to return false for username validation (should not affect password).
        try (MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class)) {
            identityUtilMock.when(() -> IdentityUtil.getProperty(
                            org.wso2.carbon.identity.flow.execution.engine.Constants.IS_USERNAME_VALIDATION_ENABLED))
                    .thenReturn("false");

            // Execute the method.
            inputValidationService.prepareStepInputs(dataDTO, FlowExecutionContext);

            // Verify that the password input has validations added even when username validation is disabled.
            ComponentDTO processedForm = dataDTO.getComponents().get(0);
            ComponentDTO processedPasswordInput = processedForm.getComponents().get(0);
            Object validations = processedPasswordInput.getConfigs().get(VALIDATIONS);

            // Validations should be present for password regardless of username validation setting.
            Assert.assertNotNull(validations, "Password field should have validations");
            Assert.assertTrue(validations instanceof List, "Validations should be a list");
            List<?> validationsList = (List<?>) validations;
            Assert.assertFalse(validationsList.isEmpty(), "Password field should have at least one validation rule");
        }
    }

    @Test
    public void testResolveInputValidationResponseWithEmptyUserInput() {

        FlowExecutionContext = initiateFlowContext();
        FlowExecutionContext.setGraphConfig(defaultGraph);

        ExecutorResponse response = inputValidationService.resolveInputValidationResponse(FlowExecutionContext);

        Assert.assertEquals(response.getResult(), STATUS_USER_INPUT_REQUIRED);
    }

    @Test
    public void testResolveInputValidationResponseWithValidInput() {

        FlowExecutionContext = initiateFlowContext();
        FlowExecutionContext.setGraphConfig(defaultGraph);

        Map<String, String> userInputData = new HashMap<>();
        userInputData.put("input1", "value1");
        FlowExecutionContext.getUserInputData().putAll(userInputData);

        ExecutorResponse response = inputValidationService.resolveInputValidationResponse(FlowExecutionContext);

        Assert.assertEquals(response.getResult(), STATUS_COMPLETE);
    }

    @Test
    public void testClearUserInputs() {

        FlowExecutionContext = initiateFlowContext();
        FlowExecutionContext.setGraphConfig(defaultGraph);

        Map<String, String> userInputData = new HashMap<>();
        userInputData.put("input1", "value1");
        userInputData.put("input2", "value2");
        FlowExecutionContext.getUserInputData().putAll(userInputData);

        Assert.assertEquals(FlowExecutionContext.getUserInputData().size(), 2);

        inputValidationService.clearUserInputs(FlowExecutionContext);

        Assert.assertTrue(FlowExecutionContext.getUserInputData().isEmpty());
    }

    @Test
    public void testClearUserInputsWithNullContext() {

        // Should not throw any exception when context is null.
        inputValidationService.clearUserInputs(null);
    }

    @Test
    public void testValidateUserClaimsWhenClaimMetadataServiceIsNull()
            throws FlowEngineException {

        // Set ClaimMetadataManagementService to null.
        FlowExecutionEngineDataHolder.getInstance().setClaimMetadataManagementService(null);

        // Should not throw any exception when ClaimMetadataManagementService is null.
        invokeValidateUserClaims("test.com", USERNAME_CLAIM_URI, "testuser", false);
    }

    @Test
    public void testValidateUserClaimsWhenLocalClaimNotPresent()
            throws FlowEngineException, ClaimMetadataException {

        ClaimMetadataManagementService mockClaimService = mock(ClaimMetadataManagementService.class);
        FlowExecutionEngineDataHolder.getInstance().setClaimMetadataManagementService(mockClaimService);

        when(mockClaimService.getLocalClaim(anyString(), anyString())).thenReturn(Optional.empty());

        // Should not throw any exception when local claim is not present.
        invokeValidateUserClaims("test.com", CLAIM_URI_PREFIX + "email", "test@example.com", false);
    }

    @Test
    public void testValidateUserClaimsWhenClaimPropertiesAreNull()
            throws FlowEngineException, ClaimMetadataException {

        ClaimMetadataManagementService mockClaimService = mock(ClaimMetadataManagementService.class);
        FlowExecutionEngineDataHolder.getInstance().setClaimMetadataManagementService(mockClaimService);

        LocalClaim mockLocalClaim = mock(LocalClaim.class);
        when(mockLocalClaim.getClaimProperties()).thenReturn(null);
        when(mockClaimService.getLocalClaim(anyString(), anyString())).thenReturn(Optional.of(mockLocalClaim));

        // Should not throw any exception when claim properties are null.
        invokeValidateUserClaims("test.com", CLAIM_URI_PREFIX + "email", "test@example.com", false);
    }

    @Test
    public void testValidateUserClaimsWhenUniquenessValidationNotRequired()
            throws FlowEngineException, ClaimMetadataException {

        ClaimMetadataManagementService mockClaimService = mock(ClaimMetadataManagementService.class);
        FlowExecutionEngineDataHolder.getInstance().setClaimMetadataManagementService(mockClaimService);

        LocalClaim mockLocalClaim = mock(LocalClaim.class);
        Map<String, String> claimProperties = new HashMap<>();
        when(mockLocalClaim.getClaimProperties()).thenReturn(claimProperties);
        when(mockClaimService.getLocalClaim(anyString(), anyString())).thenReturn(Optional.of(mockLocalClaim));

        try (MockedStatic<ClaimValidationUtil> claimValidationUtilMock = mockStatic(ClaimValidationUtil.class)) {
            claimValidationUtilMock.when(() -> ClaimValidationUtil.getClaimUniquenessScope(claimProperties))
                    .thenReturn(ClaimConstants.ClaimUniquenessScope.NONE);
            claimValidationUtilMock.when(() -> ClaimValidationUtil.shouldValidateUniqueness(
                    ClaimConstants.ClaimUniquenessScope.NONE)).thenReturn(false);

            // Should not throw any exception when uniqueness validation is not required.
            invokeValidateUserClaims("test.com", CLAIM_URI_PREFIX + "givenname", "John", false);
        }
    }

    @Test
    public void testValidateUserClaimsWhenClaimIsUnique()
            throws FlowEngineException, ClaimMetadataException {

        ClaimMetadataManagementService mockClaimService = mock(ClaimMetadataManagementService.class);
        FlowExecutionEngineDataHolder.getInstance().setClaimMetadataManagementService(mockClaimService);

        LocalClaim mockLocalClaim = mock(LocalClaim.class);
        Map<String, String> claimProperties = new HashMap<>();
        when(mockLocalClaim.getClaimProperties()).thenReturn(claimProperties);
        when(mockClaimService.getLocalClaim(anyString(), anyString())).thenReturn(Optional.of(mockLocalClaim));

        try (MockedStatic<ClaimValidationUtil> claimValidationUtilMock = mockStatic(ClaimValidationUtil.class)) {
            claimValidationUtilMock.when(() -> ClaimValidationUtil.getClaimUniquenessScope(claimProperties))
                    .thenReturn(ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES);
            claimValidationUtilMock.when(() -> ClaimValidationUtil.shouldValidateUniqueness(
                    ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES)).thenReturn(true);
            claimValidationUtilMock.when(() -> ClaimValidationUtil.isClaimDuplicated(anyString(), anyString()))
                    .thenReturn(false);

            // Should not throw any exception when claim is unique.
            invokeValidateUserClaims("test.com", CLAIM_URI_PREFIX + "email", "unique@example.com", false);
        }
    }

    @Test(expectedExceptions = FlowEngineClientException.class)
    public void testValidateUserClaimsWhenClaimIsDuplicate()
            throws FlowEngineException, ClaimMetadataException {

        ClaimMetadataManagementService mockClaimService = mock(ClaimMetadataManagementService.class);
        FlowExecutionEngineDataHolder.getInstance().setClaimMetadataManagementService(mockClaimService);

        LocalClaim mockLocalClaim = mock(LocalClaim.class);
        Map<String, String> claimProperties = new HashMap<>();
        when(mockLocalClaim.getClaimProperties()).thenReturn(claimProperties);
        when(mockClaimService.getLocalClaim(anyString(), anyString())).thenReturn(Optional.of(mockLocalClaim));

        try (MockedStatic<ClaimValidationUtil> claimValidationUtilMock = mockStatic(ClaimValidationUtil.class)) {
            claimValidationUtilMock.when(() -> ClaimValidationUtil.getClaimUniquenessScope(claimProperties))
                    .thenReturn(ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES);
            claimValidationUtilMock.when(() -> ClaimValidationUtil.shouldValidateUniqueness(
                    ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES)).thenReturn(true);
            claimValidationUtilMock.when(() -> ClaimValidationUtil.isClaimDuplicated(anyString(), anyString()))
                    .thenReturn(true);

            // Should throw FlowEngineClientException when claim is duplicate.
            invokeValidateUserClaims("test.com", CLAIM_URI_PREFIX + "email", "duplicate@example.com", false);
        }
    }

    @Test
    public void testValidateUserClaimsWithUsernameClaimUnique()
            throws FlowEngineException, ClaimMetadataException {

        ClaimMetadataManagementService mockClaimService = mock(ClaimMetadataManagementService.class);
        FlowExecutionEngineDataHolder.getInstance().setClaimMetadataManagementService(mockClaimService);

        LocalClaim mockLocalClaim = mock(LocalClaim.class);
        Map<String, String> claimProperties = new HashMap<>();
        when(mockLocalClaim.getClaimProperties()).thenReturn(claimProperties);
        when(mockClaimService.getLocalClaim(anyString(), anyString())).thenReturn(Optional.of(mockLocalClaim));

        try (MockedStatic<ClaimValidationUtil> claimValidationUtilMock = mockStatic(ClaimValidationUtil.class)) {
            // Even if shouldValidateUniqueness returns false, username claim should be validated.
            claimValidationUtilMock.when(() -> ClaimValidationUtil.getClaimUniquenessScope(claimProperties))
                    .thenReturn(ClaimConstants.ClaimUniquenessScope.NONE);
            claimValidationUtilMock.when(() -> ClaimValidationUtil.shouldValidateUniqueness(
                    ClaimConstants.ClaimUniquenessScope.NONE)).thenReturn(false);
            claimValidationUtilMock.when(() -> ClaimValidationUtil.isClaimDuplicated(anyString(), anyString()))
                    .thenReturn(false);

            // Should not throw any exception when username claim is unique.
            invokeValidateUserClaims("test.com", USERNAME_CLAIM_URI, "uniqueuser", false);
        }
    }

    @Test(expectedExceptions = FlowEngineClientException.class)
    public void testValidateUserClaimsWithUsernameClaimDuplicate()
            throws FlowEngineException, ClaimMetadataException {

        ClaimMetadataManagementService mockClaimService = mock(ClaimMetadataManagementService.class);
        FlowExecutionEngineDataHolder.getInstance().setClaimMetadataManagementService(mockClaimService);

        LocalClaim mockLocalClaim = mock(LocalClaim.class);
        Map<String, String> claimProperties = new HashMap<>();
        when(mockLocalClaim.getClaimProperties()).thenReturn(claimProperties);
        when(mockClaimService.getLocalClaim(anyString(), anyString())).thenReturn(Optional.of(mockLocalClaim));

        try (MockedStatic<ClaimValidationUtil> claimValidationUtilMock = mockStatic(ClaimValidationUtil.class)) {
            claimValidationUtilMock.when(() -> ClaimValidationUtil.getClaimUniquenessScope(claimProperties))
                    .thenReturn(ClaimConstants.ClaimUniquenessScope.NONE);
            claimValidationUtilMock.when(() -> ClaimValidationUtil.shouldValidateUniqueness(
                    ClaimConstants.ClaimUniquenessScope.NONE)).thenReturn(false);
            claimValidationUtilMock.when(() -> ClaimValidationUtil.isClaimDuplicated(anyString(), anyString()))
                    .thenReturn(true);

            // Should throw FlowEngineClientException when username claim is duplicate.
            invokeValidateUserClaims("test.com", USERNAME_CLAIM_URI, "duplicateuser", false);
        }
    }

    @Test(expectedExceptions = FlowEngineServerException.class)
    public void testValidateUserClaimsWhenClaimMetadataExceptionThrown()
            throws FlowEngineException, ClaimMetadataException {

        ClaimMetadataManagementService mockClaimService = mock(ClaimMetadataManagementService.class);
        FlowExecutionEngineDataHolder.getInstance().setClaimMetadataManagementService(mockClaimService);

        when(mockClaimService.getLocalClaim(anyString(), anyString()))
                .thenThrow(new ClaimMetadataException("Test exception"));

        // Should throw FlowEngineServerException when ClaimMetadataException is thrown.
        invokeValidateUserClaims("test.com", USERNAME_CLAIM_URI, "testuser", false);
    }

    @Test
    public void testValidateUserClaimsWithBlankClaimValue()
            throws FlowEngineException, ClaimMetadataException {

        ClaimMetadataManagementService mockClaimService = mock(ClaimMetadataManagementService.class);
        FlowExecutionEngineDataHolder.getInstance().setClaimMetadataManagementService(mockClaimService);

        LocalClaim mockLocalClaim = mock(LocalClaim.class);
        Map<String, String> claimProperties = new HashMap<>();
        when(mockLocalClaim.getClaimProperties()).thenReturn(claimProperties);
        when(mockClaimService.getLocalClaim(anyString(), anyString())).thenReturn(Optional.of(mockLocalClaim));

        try (MockedStatic<ClaimValidationUtil> claimValidationUtilMock = mockStatic(ClaimValidationUtil.class)) {
            claimValidationUtilMock.when(() -> ClaimValidationUtil.getClaimUniquenessScope(claimProperties))
                    .thenReturn(ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES);
            claimValidationUtilMock.when(() -> ClaimValidationUtil.shouldValidateUniqueness(
                    ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES)).thenReturn(true);

            // Should not validate uniqueness when claim value is blank.
            invokeValidateUserClaims("test.com", USERNAME_CLAIM_URI, "", false);
            invokeValidateUserClaims("test.com", USERNAME_CLAIM_URI, "   ", false);
            invokeValidateUserClaims("test.com", USERNAME_CLAIM_URI, null, false);

            // Verify isClaimDuplicated was never called for blank values.
            claimValidationUtilMock.verify(() -> ClaimValidationUtil.isClaimDuplicated(anyString(), anyString()),
                    org.mockito.Mockito.never());
        }
    }

    @Test
    public void testValidateUserInputsWithEmptyInputData() {

        FlowExecutionContext = initiateFlowContext();
        // When user input data is empty, resolve should request user input.
        ExecutorResponse response = inputValidationService.resolveInputValidationResponse(FlowExecutionContext);
        Assert.assertEquals(response.getResult(), STATUS_USER_INPUT_REQUIRED);
    }

    @Test
    public void testValidateUserInputsWithNonClaimInputsOnly() {

        FlowExecutionContext = initiateFlowContext();
        FlowExecutionEngineDataHolder.getInstance().setInputValidationManagementService(null);
        Map<String, String> userInputData = new HashMap<>();
        userInputData.put("password", "password123");
        userInputData.put("confirmPassword", "password123");
        FlowExecutionContext.getUserInputData().putAll(userInputData);
        ExecutorResponse response = inputValidationService.resolveInputValidationResponse(FlowExecutionContext);
        Assert.assertEquals(response.getResult(), STATUS_COMPLETE);
    }

    @Test
    public void testValidateUserInputsWithValidClaimInput()
            throws ClaimMetadataException {

        FlowExecutionContext = initiateFlowContext();
        Map<String, String> userInputData = new HashMap<>();
        userInputData.put(CLAIM_URI_PREFIX + "email", "unique@example.com");
        FlowExecutionContext.getUserInputData().putAll(userInputData);

        ClaimMetadataManagementService mockClaimService = mock(ClaimMetadataManagementService.class);
        FlowExecutionEngineDataHolder.getInstance().setClaimMetadataManagementService(mockClaimService);

        LocalClaim mockLocalClaim = mock(LocalClaim.class);
        Map<String, String> claimProperties = new HashMap<>();
        when(mockLocalClaim.getClaimProperties()).thenReturn(claimProperties);
        when(mockClaimService.getLocalClaim(anyString(), anyString())).thenReturn(Optional.of(mockLocalClaim));

        try (MockedStatic<ClaimValidationUtil> claimValidationUtilMock = mockStatic(ClaimValidationUtil.class)) {
            claimValidationUtilMock.when(() -> ClaimValidationUtil.getClaimUniquenessScope(claimProperties))
                    .thenReturn(ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES);
            claimValidationUtilMock.when(() -> ClaimValidationUtil.shouldValidateUniqueness(
                    ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES)).thenReturn(true);
            claimValidationUtilMock.when(() -> ClaimValidationUtil.isClaimDuplicated(anyString(), anyString()))
                    .thenReturn(false);
            ExecutorResponse response = inputValidationService.resolveInputValidationResponse(FlowExecutionContext);
            Assert.assertEquals(response.getResult(), STATUS_COMPLETE);
        }
    }

    @Test
    public void testValidateUserInputsWithDuplicateClaimValue()
            throws ClaimMetadataException {

        FlowExecutionContext = initiateFlowContext();
        Map<String, String> userInputData = new HashMap<>();
        userInputData.put(CLAIM_URI_PREFIX + "email", "duplicate@example.com");
        FlowExecutionContext.getUserInputData().putAll(userInputData);

        ClaimMetadataManagementService mockClaimService = mock(ClaimMetadataManagementService.class);
        FlowExecutionEngineDataHolder.getInstance().setClaimMetadataManagementService(mockClaimService);

        LocalClaim mockLocalClaim = mock(LocalClaim.class);
        Map<String, String> claimProperties = new HashMap<>();
        when(mockLocalClaim.getClaimProperties()).thenReturn(claimProperties);
        when(mockClaimService.getLocalClaim(anyString(), anyString())).thenReturn(Optional.of(mockLocalClaim));

        try (MockedStatic<ClaimValidationUtil> claimValidationUtilMock = mockStatic(ClaimValidationUtil.class)) {
            claimValidationUtilMock.when(() -> ClaimValidationUtil.getClaimUniquenessScope(claimProperties))
                    .thenReturn(ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES);
            claimValidationUtilMock.when(() -> ClaimValidationUtil.shouldValidateUniqueness(
                    ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES)).thenReturn(true);
            claimValidationUtilMock.when(() -> ClaimValidationUtil.isClaimDuplicated(anyString(), anyString()))
                    .thenReturn(true);
            // Duplicate claim validation failure should result in RETRY status.
            ExecutorResponse response = inputValidationService.resolveInputValidationResponse(FlowExecutionContext);
            Assert.assertEquals(response.getResult(), STATUS_RETRY);
        }
    }

    @Test
    public void testValidateUserInputsDuplicateClaimErrorCode()
            throws ClaimMetadataException {

        FlowExecutionContext = initiateFlowContext();
        Map<String, String> userInputData = new HashMap<>();
        userInputData.put(CLAIM_URI_PREFIX + "email", "duplicate@example.com");
        FlowExecutionContext.getUserInputData().putAll(userInputData);

        ClaimMetadataManagementService mockClaimService = mock(ClaimMetadataManagementService.class);
        FlowExecutionEngineDataHolder.getInstance().setClaimMetadataManagementService(mockClaimService);

        LocalClaim mockLocalClaim = mock(LocalClaim.class);
        Map<String, String> claimProperties = new HashMap<>();
        when(mockLocalClaim.getClaimProperties()).thenReturn(claimProperties);
        when(mockClaimService.getLocalClaim(anyString(), anyString())).thenReturn(Optional.of(mockLocalClaim));

        try (MockedStatic<ClaimValidationUtil> claimValidationUtilMock = mockStatic(ClaimValidationUtil.class)) {
            claimValidationUtilMock.when(() -> ClaimValidationUtil.getClaimUniquenessScope(claimProperties))
                    .thenReturn(ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES);
            claimValidationUtilMock.when(() -> ClaimValidationUtil.shouldValidateUniqueness(
                    ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES)).thenReturn(true);
            claimValidationUtilMock.when(() -> ClaimValidationUtil.isClaimDuplicated(anyString(), anyString()))
                    .thenReturn(true);

            ExecutorResponse response = inputValidationService.resolveInputValidationResponse(FlowExecutionContext);
            Assert.assertEquals(response.getResult(), STATUS_RETRY);
            Assert.assertEquals(response.getErrorCode(), ERROR_CODE_CLAIM_UNIQUENESS_VALIDATION_FAILED.getCode());
        }
    }

    @Test
    public void testValidateUserInputsWithServerError()
            throws ClaimMetadataException {

        FlowExecutionContext = initiateFlowContext();
        Map<String, String> userInputData = new HashMap<>();
        userInputData.put(CLAIM_URI_PREFIX + "email", "test@example.com");
        FlowExecutionContext.getUserInputData().putAll(userInputData);

        ClaimMetadataManagementService mockClaimService = mock(ClaimMetadataManagementService.class);
        FlowExecutionEngineDataHolder.getInstance().setClaimMetadataManagementService(mockClaimService);

        when(mockClaimService.getLocalClaim(anyString(), anyString()))
                .thenThrow(new ClaimMetadataException("Test server error"));
        // Server errors during input validation result in RETRY status from resolveInputValidationResponse.
        ExecutorResponse response = inputValidationService.resolveInputValidationResponse(FlowExecutionContext);
        Assert.assertEquals(response.getResult(), STATUS_RETRY);
    }

    @Test
    public void testValidateUserInputsServerErrorCode()
            throws ClaimMetadataException {

        FlowExecutionContext = initiateFlowContext();
        Map<String, String> userInputData = new HashMap<>();
        userInputData.put(CLAIM_URI_PREFIX + "email", "test@example.com");
        FlowExecutionContext.getUserInputData().putAll(userInputData);

        ClaimMetadataManagementService mockClaimService = mock(ClaimMetadataManagementService.class);
        FlowExecutionEngineDataHolder.getInstance().setClaimMetadataManagementService(mockClaimService);
        when(mockClaimService.getLocalClaim(anyString(), anyString()))
                .thenThrow(new ClaimMetadataException("Test server error"));

        ExecutorResponse response = inputValidationService.resolveInputValidationResponse(FlowExecutionContext);
        Assert.assertEquals(response.getResult(), STATUS_RETRY);
        Assert.assertEquals(response.getErrorCode(), ERROR_CODE_CLAIM_META_DATA_NOT_FOUND.getCode());
    }

    @Test
    public void testValidateUserInputsWithMultipleValidClaimInputs()
            throws ClaimMetadataException {

        FlowExecutionContext = initiateFlowContext();
        Map<String, String> userInputData = new HashMap<>();
        userInputData.put(CLAIM_URI_PREFIX + "email", "unique@example.com");
        userInputData.put(CLAIM_URI_PREFIX + "givenname", "John");
        FlowExecutionContext.getUserInputData().putAll(userInputData);

        ClaimMetadataManagementService mockClaimService = mock(ClaimMetadataManagementService.class);
        FlowExecutionEngineDataHolder.getInstance().setClaimMetadataManagementService(mockClaimService);

        LocalClaim mockLocalClaim = mock(LocalClaim.class);
        Map<String, String> claimProperties = new HashMap<>();
        when(mockLocalClaim.getClaimProperties()).thenReturn(claimProperties);
        when(mockClaimService.getLocalClaim(anyString(), anyString())).thenReturn(Optional.of(mockLocalClaim));

        try (MockedStatic<ClaimValidationUtil> claimValidationUtilMock = mockStatic(ClaimValidationUtil.class)) {
            claimValidationUtilMock.when(() -> ClaimValidationUtil.getClaimUniquenessScope(claimProperties))
                    .thenReturn(ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES);
            claimValidationUtilMock.when(() -> ClaimValidationUtil.shouldValidateUniqueness(
                    ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES)).thenReturn(true);
            claimValidationUtilMock.when(() -> ClaimValidationUtil.isClaimDuplicated(anyString(), anyString()))
                    .thenReturn(false);
            ExecutorResponse response = inputValidationService.resolveInputValidationResponse(FlowExecutionContext);
            Assert.assertEquals(response.getResult(), STATUS_COMPLETE);
        }
    }

    @Test
    public void testValidateUserInputsWithMixedClaimAndNonClaimInputs()
            throws ClaimMetadataException {

        FlowExecutionContext = initiateFlowContext();
        Map<String, String> userInputData = new HashMap<>();
        userInputData.put(CLAIM_URI_PREFIX + "email", "unique@example.com");
        userInputData.put("password", "password123");
        FlowExecutionContext.getUserInputData().putAll(userInputData);

        ClaimMetadataManagementService mockClaimService = mock(ClaimMetadataManagementService.class);
        FlowExecutionEngineDataHolder.getInstance().setClaimMetadataManagementService(mockClaimService);
        FlowExecutionEngineDataHolder.getInstance().setInputValidationManagementService(null);

        LocalClaim mockLocalClaim = mock(LocalClaim.class);
        Map<String, String> claimProperties = new HashMap<>();
        when(mockLocalClaim.getClaimProperties()).thenReturn(claimProperties);
        when(mockClaimService.getLocalClaim(anyString(), anyString())).thenReturn(Optional.of(mockLocalClaim));

        try (MockedStatic<ClaimValidationUtil> claimValidationUtilMock = mockStatic(ClaimValidationUtil.class)) {
            claimValidationUtilMock.when(() -> ClaimValidationUtil.getClaimUniquenessScope(claimProperties))
                    .thenReturn(ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES);
            claimValidationUtilMock.when(() -> ClaimValidationUtil.shouldValidateUniqueness(
                    ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES)).thenReturn(true);
            claimValidationUtilMock.when(() -> ClaimValidationUtil.isClaimDuplicated(anyString(), anyString()))
                    .thenReturn(false);
            ExecutorResponse response = inputValidationService.resolveInputValidationResponse(FlowExecutionContext);
            Assert.assertEquals(response.getResult(), STATUS_COMPLETE);
        }
    }

    @Test
    public void testValidateUserInputsWhenClaimMetadataServiceIsNull() {

        FlowExecutionContext = initiateFlowContext();
        Map<String, String> userInputData = new HashMap<>();
        userInputData.put(CLAIM_URI_PREFIX + "email", "test@example.com");
        FlowExecutionContext.getUserInputData().putAll(userInputData);

        FlowExecutionEngineDataHolder.getInstance().setClaimMetadataManagementService(null);
        ExecutorResponse response = inputValidationService.resolveInputValidationResponse(FlowExecutionContext);
        Assert.assertEquals(response.getResult(), STATUS_COMPLETE);
    }

    @Test
    public void testResolveInputValidationResponseWithDuplicateClaim()
            throws ClaimMetadataException {

        FlowExecutionContext = initiateFlowContext();
        FlowExecutionContext.setGraphConfig(defaultGraph);

        Map<String, String> userInputData = new HashMap<>();
        userInputData.put(CLAIM_URI_PREFIX + "email", "duplicate@example.com");
        FlowExecutionContext.getUserInputData().putAll(userInputData);

        ClaimMetadataManagementService mockClaimService = mock(ClaimMetadataManagementService.class);
        FlowExecutionEngineDataHolder.getInstance().setClaimMetadataManagementService(mockClaimService);

        LocalClaim mockLocalClaim = mock(LocalClaim.class);
        Map<String, String> claimProperties = new HashMap<>();
        when(mockLocalClaim.getClaimProperties()).thenReturn(claimProperties);
        when(mockClaimService.getLocalClaim(anyString(), anyString())).thenReturn(Optional.of(mockLocalClaim));

        try (MockedStatic<ClaimValidationUtil> claimValidationUtilMock = mockStatic(ClaimValidationUtil.class)) {
            claimValidationUtilMock.when(() -> ClaimValidationUtil.getClaimUniquenessScope(claimProperties))
                    .thenReturn(ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES);
            claimValidationUtilMock.when(() -> ClaimValidationUtil.shouldValidateUniqueness(
                    ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES)).thenReturn(true);
            claimValidationUtilMock.when(() -> ClaimValidationUtil.isClaimDuplicated(anyString(), anyString()))
                    .thenReturn(true);

            ExecutorResponse response = inputValidationService.resolveInputValidationResponse(FlowExecutionContext);

            Assert.assertEquals(response.getResult(), STATUS_RETRY);
            Assert.assertNotNull(response.getErrorCode());
            Assert.assertNotNull(response.getErrorMessage());
        }
    }

    @Test
    public void testValidateUserClaimsWithMatchingRegex()
            throws FlowEngineException, ClaimMetadataException {

        ClaimMetadataManagementService mockClaimService = mock(ClaimMetadataManagementService.class);
        FlowExecutionEngineDataHolder.getInstance().setClaimMetadataManagementService(mockClaimService);

        LocalClaim mockLocalClaim = mock(LocalClaim.class);
        Map<String, String> claimProperties = new HashMap<>();
        claimProperties.put(ClaimConstants.REGULAR_EXPRESSION_PROPERTY, "^[0-9]{10}$");
        when(mockLocalClaim.getClaimProperties()).thenReturn(claimProperties);
        when(mockClaimService.getLocalClaim(anyString(), anyString())).thenReturn(Optional.of(mockLocalClaim));

        try (MockedStatic<ClaimValidationUtil> claimValidationUtilMock = mockStatic(ClaimValidationUtil.class)) {
            claimValidationUtilMock.when(() -> ClaimValidationUtil.getClaimUniquenessScope(claimProperties))
                    .thenReturn(ClaimConstants.ClaimUniquenessScope.NONE);
            claimValidationUtilMock.when(() -> ClaimValidationUtil.shouldValidateUniqueness(
                    ClaimConstants.ClaimUniquenessScope.NONE)).thenReturn(false);

            // Should not throw any exception when value matches the regex.
            invokeValidateUserClaims("test.com", CLAIM_URI_PREFIX + "mobile", "0771234567", false);
        }
    }

    @Test(expectedExceptions = FlowEngineClientException.class)
    public void testValidateUserClaimsWithNonMatchingRegex()
            throws FlowEngineException, ClaimMetadataException {

        ClaimMetadataManagementService mockClaimService = mock(ClaimMetadataManagementService.class);
        FlowExecutionEngineDataHolder.getInstance().setClaimMetadataManagementService(mockClaimService);

        LocalClaim mockLocalClaim = mock(LocalClaim.class);
        Map<String, String> claimProperties = new HashMap<>();
        claimProperties.put(ClaimConstants.REGULAR_EXPRESSION_PROPERTY, "^[0-9]{10}$");
        when(mockLocalClaim.getClaimProperties()).thenReturn(claimProperties);
        when(mockClaimService.getLocalClaim(anyString(), anyString())).thenReturn(Optional.of(mockLocalClaim));

        try (MockedStatic<ClaimValidationUtil> claimValidationUtilMock = mockStatic(ClaimValidationUtil.class)) {
            claimValidationUtilMock.when(() -> ClaimValidationUtil.getClaimUniquenessScope(claimProperties))
                    .thenReturn(ClaimConstants.ClaimUniquenessScope.NONE);
            claimValidationUtilMock.when(() -> ClaimValidationUtil.shouldValidateUniqueness(
                    ClaimConstants.ClaimUniquenessScope.NONE)).thenReturn(false);

            // Should throw FlowEngineClientException when value does not match the regex.
            invokeValidateUserClaims("test.com", CLAIM_URI_PREFIX + "mobile",
                    "not-a-phone-number", false);
        }
    }

    @Test
    public void testValidateUserClaimsRegexValidationErrorCode()
            throws FlowEngineException, ClaimMetadataException {

        ClaimMetadataManagementService mockClaimService = mock(ClaimMetadataManagementService.class);
        FlowExecutionEngineDataHolder.getInstance().setClaimMetadataManagementService(mockClaimService);

        LocalClaim mockLocalClaim = mock(LocalClaim.class);
        Map<String, String> claimProperties = new HashMap<>();
        claimProperties.put(ClaimConstants.REGULAR_EXPRESSION_PROPERTY, "^[0-9]{10}$");
        when(mockLocalClaim.getClaimProperties()).thenReturn(claimProperties);
        when(mockClaimService.getLocalClaim(anyString(), anyString())).thenReturn(Optional.of(mockLocalClaim));

        try (MockedStatic<ClaimValidationUtil> claimValidationUtilMock = mockStatic(ClaimValidationUtil.class)) {
            claimValidationUtilMock.when(() -> ClaimValidationUtil.getClaimUniquenessScope(claimProperties))
                    .thenReturn(ClaimConstants.ClaimUniquenessScope.NONE);
            claimValidationUtilMock.when(() -> ClaimValidationUtil.shouldValidateUniqueness(
                    ClaimConstants.ClaimUniquenessScope.NONE)).thenReturn(false);

            try {
                invokeValidateUserClaims("test.com", CLAIM_URI_PREFIX + "mobile",
                        "not-a-phone-number", false);
                Assert.fail("Expected FlowEngineClientException to be thrown");
            } catch (FlowEngineClientException e) {
                Assert.assertEquals(e.getErrorCode(), ERROR_CODE_CLAIM_REGEX_VALIDATION_FAILED.getCode());
            }
        }
    }

    @Test
    public void testValidateUserClaimsSkipsRegexForBlankValue()
            throws FlowEngineException, ClaimMetadataException {

        ClaimMetadataManagementService mockClaimService = mock(ClaimMetadataManagementService.class);
        FlowExecutionEngineDataHolder.getInstance().setClaimMetadataManagementService(mockClaimService);

        LocalClaim mockLocalClaim = mock(LocalClaim.class);
        Map<String, String> claimProperties = new HashMap<>();
        claimProperties.put(ClaimConstants.REGULAR_EXPRESSION_PROPERTY, "^[0-9]{10}$");
        when(mockLocalClaim.getClaimProperties()).thenReturn(claimProperties);
        when(mockClaimService.getLocalClaim(anyString(), anyString())).thenReturn(Optional.of(mockLocalClaim));

        try (MockedStatic<ClaimValidationUtil> claimValidationUtilMock = mockStatic(ClaimValidationUtil.class)) {
            claimValidationUtilMock.when(() -> ClaimValidationUtil.getClaimUniquenessScope(claimProperties))
                    .thenReturn(ClaimConstants.ClaimUniquenessScope.NONE);
            claimValidationUtilMock.when(() -> ClaimValidationUtil.shouldValidateUniqueness(
                    ClaimConstants.ClaimUniquenessScope.NONE)).thenReturn(false);

            // Should not throw for blank values even when regex is configured.
            invokeValidateUserClaims("test.com", CLAIM_URI_PREFIX + "mobile", "", false);
            invokeValidateUserClaims("test.com", CLAIM_URI_PREFIX + "mobile", null, false);
        }
    }

    @Test
    public void testValidateUserClaimsSkipsRegexWhenNotConfigured()
            throws FlowEngineException, ClaimMetadataException {

        ClaimMetadataManagementService mockClaimService = mock(ClaimMetadataManagementService.class);
        FlowExecutionEngineDataHolder.getInstance().setClaimMetadataManagementService(mockClaimService);

        LocalClaim mockLocalClaim = mock(LocalClaim.class);
        Map<String, String> claimProperties = new HashMap<>();
        // No regex property configured.
        when(mockLocalClaim.getClaimProperties()).thenReturn(claimProperties);
        when(mockClaimService.getLocalClaim(anyString(), anyString())).thenReturn(Optional.of(mockLocalClaim));

        try (MockedStatic<ClaimValidationUtil> claimValidationUtilMock = mockStatic(ClaimValidationUtil.class)) {
            claimValidationUtilMock.when(() -> ClaimValidationUtil.getClaimUniquenessScope(claimProperties))
                    .thenReturn(ClaimConstants.ClaimUniquenessScope.NONE);
            claimValidationUtilMock.when(() -> ClaimValidationUtil.shouldValidateUniqueness(
                    ClaimConstants.ClaimUniquenessScope.NONE)).thenReturn(false);

            // Should not throw even for values that would fail a regex, since none is configured.
            invokeValidateUserClaims("test.com", CLAIM_URI_PREFIX + "mobile",
                    "not-a-phone-number", false);
        }
    }

    @Test
    public void testValidateUserClaimsSkipsRegexForUsernameClaim()
            throws FlowEngineException, ClaimMetadataException {

        ClaimMetadataManagementService mockClaimService = mock(ClaimMetadataManagementService.class);
        FlowExecutionEngineDataHolder.getInstance().setClaimMetadataManagementService(mockClaimService);
        // Null out the service so username format validation via InputValidationManagementService is skipped.
        FlowExecutionEngineDataHolder.getInstance().setInputValidationManagementService(null);

        LocalClaim mockLocalClaim = mock(LocalClaim.class);
        Map<String, String> claimProperties = new HashMap<>();
        // Regex that would reject the value if applied to username.
        claimProperties.put(ClaimConstants.REGULAR_EXPRESSION_PROPERTY, "^[0-9]+$");
        when(mockLocalClaim.getClaimProperties()).thenReturn(claimProperties);
        when(mockClaimService.getLocalClaim(anyString(), anyString())).thenReturn(Optional.of(mockLocalClaim));

        try (MockedStatic<ClaimValidationUtil> claimValidationUtilMock = mockStatic(ClaimValidationUtil.class)) {
            claimValidationUtilMock.when(() -> ClaimValidationUtil.getClaimUniquenessScope(claimProperties))
                    .thenReturn(ClaimConstants.ClaimUniquenessScope.NONE);
            claimValidationUtilMock.when(() -> ClaimValidationUtil.shouldValidateUniqueness(
                    ClaimConstants.ClaimUniquenessScope.NONE)).thenReturn(false);

            // Claim RegEx property should be skipped for username — validated via InputValidationManagementService.
            invokeValidateUserClaims("test.com", USERNAME_CLAIM_URI, "john@example.com", false);
        }
    }

    @Test
    public void testValidateUsernameFormatPassesWithValidUsername()
            throws FlowEngineException, ClaimMetadataException, InputValidationMgtException {

        ClaimMetadataManagementService mockClaimService = mock(ClaimMetadataManagementService.class);
        FlowExecutionEngineDataHolder.getInstance().setClaimMetadataManagementService(mockClaimService);

        LocalClaim mockLocalClaim = mock(LocalClaim.class);
        Map<String, String> claimProperties = new HashMap<>();
        when(mockLocalClaim.getClaimProperties()).thenReturn(claimProperties);
        when(mockClaimService.getLocalClaim(anyString(), anyString())).thenReturn(Optional.of(mockLocalClaim));

        InputValidationManagementService mockInputValidationService = mock(InputValidationManagementService.class);
        FlowExecutionEngineDataHolder.getInstance().setInputValidationManagementService(mockInputValidationService);

        Validator mockValidator = mock(Validator.class);
        when(mockValidator.validate(org.mockito.ArgumentMatchers.any(ValidationContext.class))).thenReturn(true);
        Map<String, Validator> validators = new HashMap<>();
        validators.put("LengthValidator", mockValidator);

        ValidationConfiguration usernameConfig = new ValidationConfiguration();
        usernameConfig.setField("username");
        RulesConfiguration rule = new RulesConfiguration();
        rule.setValidatorName("LengthValidator");
        rule.setProperties(new HashMap<>());
        usernameConfig.setRules(Collections.singletonList(rule));

        when(mockInputValidationService.getInputValidationConfiguration(anyString()))
                .thenReturn(Collections.singletonList(usernameConfig));
        when(mockInputValidationService.getValidators(anyString())).thenReturn(validators);

        try (MockedStatic<ClaimValidationUtil> claimValidationUtilMock = mockStatic(ClaimValidationUtil.class);
             MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class)) {
            identityUtilMock.when(() -> IdentityUtil.getProperty(IS_USERNAME_VALIDATION_ENABLED))
                    .thenReturn("true");
            claimValidationUtilMock.when(() -> ClaimValidationUtil.getClaimUniquenessScope(claimProperties))
                    .thenReturn(ClaimConstants.ClaimUniquenessScope.NONE);
            claimValidationUtilMock.when(() -> ClaimValidationUtil.shouldValidateUniqueness(
                    ClaimConstants.ClaimUniquenessScope.NONE)).thenReturn(false);

            // Should pass when all validators return true.
            invokeValidateUserClaims("test.com", USERNAME_CLAIM_URI, "validuser", false);
        }
    }

    @Test
    public void testValidateUsernameFormatFailsWithInvalidUsername()
            throws FlowEngineException, ClaimMetadataException, InputValidationMgtException {

        ClaimMetadataManagementService mockClaimService = mock(ClaimMetadataManagementService.class);
        FlowExecutionEngineDataHolder.getInstance().setClaimMetadataManagementService(mockClaimService);

        LocalClaim mockLocalClaim = mock(LocalClaim.class);
        Map<String, String> claimProperties = new HashMap<>();
        when(mockLocalClaim.getClaimProperties()).thenReturn(claimProperties);
        when(mockClaimService.getLocalClaim(anyString(), anyString())).thenReturn(Optional.of(mockLocalClaim));

        InputValidationManagementService mockInputValidationService = mock(InputValidationManagementService.class);
        FlowExecutionEngineDataHolder.getInstance().setInputValidationManagementService(mockInputValidationService);

        Validator mockValidator = mock(Validator.class);
        when(mockValidator.validate(org.mockito.ArgumentMatchers.any(ValidationContext.class)))
                .thenThrow(new InputValidationMgtClientException("60001", "Username too short", "Username too short"));
        Map<String, Validator> validators = new HashMap<>();
        validators.put("LengthValidator", mockValidator);

        ValidationConfiguration usernameConfig = new ValidationConfiguration();
        usernameConfig.setField("username");
        RulesConfiguration rule = new RulesConfiguration();
        rule.setValidatorName("LengthValidator");
        rule.setProperties(new HashMap<>());
        usernameConfig.setRules(Collections.singletonList(rule));

        when(mockInputValidationService.getInputValidationConfiguration(anyString()))
                .thenReturn(Collections.singletonList(usernameConfig));
        when(mockInputValidationService.getValidators(anyString())).thenReturn(validators);

        try (MockedStatic<ClaimValidationUtil> claimValidationUtilMock = mockStatic(ClaimValidationUtil.class);
             MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class)) {
            identityUtilMock.when(() -> IdentityUtil.getProperty(IS_USERNAME_VALIDATION_ENABLED))
                    .thenReturn("true");
            claimValidationUtilMock.when(() -> ClaimValidationUtil.getClaimUniquenessScope(claimProperties))
                    .thenReturn(ClaimConstants.ClaimUniquenessScope.NONE);
            claimValidationUtilMock.when(() -> ClaimValidationUtil.shouldValidateUniqueness(
                    ClaimConstants.ClaimUniquenessScope.NONE)).thenReturn(false);

            try {
                invokeValidateUserClaims("test.com", USERNAME_CLAIM_URI, "ab", false);
                Assert.fail("Expected FlowEngineClientException to be thrown");
            } catch (FlowEngineClientException e) {
                Assert.assertEquals(e.getErrorCode(), ERROR_CODE_USERNAME_FORMAT_VALIDATION_FAILED.getCode());
            }
        }
    }

    @Test
    public void testValidateUsernameFormatSkippedWhenValidationDisabled()
            throws FlowEngineException, ClaimMetadataException, InputValidationMgtException {

        ClaimMetadataManagementService mockClaimService = mock(ClaimMetadataManagementService.class);
        FlowExecutionEngineDataHolder.getInstance().setClaimMetadataManagementService(mockClaimService);

        LocalClaim mockLocalClaim = mock(LocalClaim.class);
        Map<String, String> claimProperties = new HashMap<>();
        when(mockLocalClaim.getClaimProperties()).thenReturn(claimProperties);
        when(mockClaimService.getLocalClaim(anyString(), anyString())).thenReturn(Optional.of(mockLocalClaim));

        InputValidationManagementService mockInputValidationService = mock(InputValidationManagementService.class);
        FlowExecutionEngineDataHolder.getInstance().setInputValidationManagementService(mockInputValidationService);

        Validator mockValidator = mock(Validator.class);
        when(mockValidator.validate(org.mockito.ArgumentMatchers.any(ValidationContext.class)))
                .thenThrow(new InputValidationMgtClientException("60001", "Fail", "Fail"));
        Map<String, Validator> validators = new HashMap<>();
        validators.put("LengthValidator", mockValidator);

        ValidationConfiguration usernameConfig = new ValidationConfiguration();
        usernameConfig.setField("username");
        RulesConfiguration rule = new RulesConfiguration();
        rule.setValidatorName("LengthValidator");
        rule.setProperties(new HashMap<>());
        usernameConfig.setRules(Collections.singletonList(rule));

        when(mockInputValidationService.getInputValidationConfiguration(anyString()))
                .thenReturn(Collections.singletonList(usernameConfig));
        when(mockInputValidationService.getValidators(anyString())).thenReturn(validators);

        try (MockedStatic<ClaimValidationUtil> claimValidationUtilMock = mockStatic(ClaimValidationUtil.class);
             MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class)) {
            identityUtilMock.when(() -> IdentityUtil.getProperty(IS_USERNAME_VALIDATION_ENABLED))
                    .thenReturn("false");
            claimValidationUtilMock.when(() -> ClaimValidationUtil.getClaimUniquenessScope(claimProperties))
                    .thenReturn(ClaimConstants.ClaimUniquenessScope.NONE);
            claimValidationUtilMock.when(() -> ClaimValidationUtil.shouldValidateUniqueness(
                    ClaimConstants.ClaimUniquenessScope.NONE)).thenReturn(false);

            // Should not throw even with a failing validator when username validation is disabled.
            invokeValidateUserClaims("test.com", USERNAME_CLAIM_URI, "ab", false);
        }
    }

    @Test
    public void testValidateUserInputsWithRegexMismatch() throws ClaimMetadataException {

        FlowExecutionContext = initiateFlowContext();
        Map<String, String> userInputData = new HashMap<>();
        userInputData.put(CLAIM_URI_PREFIX + "mobile", "not-a-phone-number");
        FlowExecutionContext.getUserInputData().putAll(userInputData);

        ClaimMetadataManagementService mockClaimService = mock(ClaimMetadataManagementService.class);
        FlowExecutionEngineDataHolder.getInstance().setClaimMetadataManagementService(mockClaimService);

        LocalClaim mockLocalClaim = mock(LocalClaim.class);
        Map<String, String> claimProperties = new HashMap<>();
        claimProperties.put(ClaimConstants.REGULAR_EXPRESSION_PROPERTY, "^[0-9]{10}$");
        when(mockLocalClaim.getClaimProperties()).thenReturn(claimProperties);
        when(mockClaimService.getLocalClaim(anyString(), anyString())).thenReturn(Optional.of(mockLocalClaim));

        try (MockedStatic<ClaimValidationUtil> claimValidationUtilMock = mockStatic(ClaimValidationUtil.class)) {
            claimValidationUtilMock.when(() -> ClaimValidationUtil.getClaimUniquenessScope(claimProperties))
                    .thenReturn(ClaimConstants.ClaimUniquenessScope.NONE);
            claimValidationUtilMock.when(() -> ClaimValidationUtil.shouldValidateUniqueness(
                    ClaimConstants.ClaimUniquenessScope.NONE)).thenReturn(false);

            ExecutorResponse response = inputValidationService.resolveInputValidationResponse(FlowExecutionContext);
            Assert.assertEquals(response.getResult(), STATUS_RETRY);
            Assert.assertEquals(response.getErrorCode(), ERROR_CODE_CLAIM_REGEX_VALIDATION_FAILED.getCode());
        }
    }

    @Test
    public void testValidatePasswordFormatPassesWithValidPassword()
            throws InputValidationMgtException {

        FlowExecutionContext = initiateFlowContext();
        Map<String, String> userInputData = new HashMap<>();
        userInputData.put(PASSWORD_KEY, "ValidPass@123");
        FlowExecutionContext.getUserInputData().putAll(userInputData);

        InputValidationManagementService mockInputValidationService = mock(InputValidationManagementService.class);
        FlowExecutionEngineDataHolder.getInstance().setInputValidationManagementService(mockInputValidationService);

        Validator mockValidator = mock(Validator.class);
        when(mockValidator.validate(org.mockito.ArgumentMatchers.any(ValidationContext.class))).thenReturn(true);
        Map<String, Validator> validators = new HashMap<>();
        validators.put("LengthValidator", mockValidator);

        ValidationConfiguration passwordConfig = new ValidationConfiguration();
        passwordConfig.setField(PASSWORD_KEY);
        RulesConfiguration rule = new RulesConfiguration();
        rule.setValidatorName("LengthValidator");
        rule.setProperties(new HashMap<>());
        passwordConfig.setRules(Collections.singletonList(rule));

        when(mockInputValidationService.getInputValidationConfiguration(anyString()))
                .thenReturn(Collections.singletonList(passwordConfig));
        when(mockInputValidationService.getValidators(anyString())).thenReturn(validators);

        ExecutorResponse response = inputValidationService.resolveInputValidationResponse(FlowExecutionContext);
        Assert.assertEquals(response.getResult(), STATUS_COMPLETE);
    }

    @Test
    public void testValidatePasswordFormatFailsWithInvalidPassword()
            throws InputValidationMgtException {

        FlowExecutionContext = initiateFlowContext();
        Map<String, String> userInputData = new HashMap<>();
        userInputData.put(PASSWORD_KEY, "weak");
        FlowExecutionContext.getUserInputData().putAll(userInputData);

        InputValidationManagementService mockInputValidationService = mock(InputValidationManagementService.class);
        FlowExecutionEngineDataHolder.getInstance().setInputValidationManagementService(mockInputValidationService);

        Validator mockValidator = mock(Validator.class);
        when(mockValidator.validate(org.mockito.ArgumentMatchers.any(ValidationContext.class)))
                .thenThrow(new InputValidationMgtClientException("60001", "Password too short", "Password too short"));
        Map<String, Validator> validators = new HashMap<>();
        validators.put("LengthValidator", mockValidator);

        ValidationConfiguration passwordConfig = new ValidationConfiguration();
        passwordConfig.setField(PASSWORD_KEY);
        RulesConfiguration rule = new RulesConfiguration();
        rule.setValidatorName("LengthValidator");
        rule.setProperties(new HashMap<>());
        passwordConfig.setRules(Collections.singletonList(rule));

        when(mockInputValidationService.getInputValidationConfiguration(anyString()))
                .thenReturn(Collections.singletonList(passwordConfig));
        when(mockInputValidationService.getValidators(anyString())).thenReturn(validators);

        ExecutorResponse response = inputValidationService.resolveInputValidationResponse(FlowExecutionContext);
        Assert.assertEquals(response.getResult(), STATUS_RETRY);
        Assert.assertEquals(response.getErrorCode(), ERROR_CODE_PASSWORD_FORMAT_VALIDATION_FAILED.getCode());
    }

    @Test
    public void testValidatePasswordFormatErrorCodeDoesNotExposePassword()
            throws InputValidationMgtException {

        FlowExecutionContext = initiateFlowContext();
        Map<String, String> userInputData = new HashMap<>();
        userInputData.put(PASSWORD_KEY, "secret123");
        FlowExecutionContext.getUserInputData().putAll(userInputData);

        InputValidationManagementService mockInputValidationService = mock(InputValidationManagementService.class);
        FlowExecutionEngineDataHolder.getInstance().setInputValidationManagementService(mockInputValidationService);

        Validator mockValidator = mock(Validator.class);
        when(mockValidator.validate(org.mockito.ArgumentMatchers.any(ValidationContext.class)))
                .thenThrow(new InputValidationMgtClientException("60001", "Fail", "Fail"));
        Map<String, Validator> validators = new HashMap<>();
        validators.put("LengthValidator", mockValidator);

        ValidationConfiguration passwordConfig = new ValidationConfiguration();
        passwordConfig.setField(PASSWORD_KEY);
        RulesConfiguration rule = new RulesConfiguration();
        rule.setValidatorName("LengthValidator");
        rule.setProperties(new HashMap<>());
        passwordConfig.setRules(Collections.singletonList(rule));

        when(mockInputValidationService.getInputValidationConfiguration(anyString()))
                .thenReturn(Collections.singletonList(passwordConfig));
        when(mockInputValidationService.getValidators(anyString())).thenReturn(validators);

        ExecutorResponse response = inputValidationService.resolveInputValidationResponse(FlowExecutionContext);
        Assert.assertEquals(response.getResult(), STATUS_RETRY);
        Assert.assertFalse(response.getErrorMessage() != null && response.getErrorMessage().contains("secret123"),
                "Error message must not expose the password value");
    }

    @Test
    public void testValidatePasswordFormatSkippedWhenServiceIsNull() {

        FlowExecutionContext = initiateFlowContext();
        Map<String, String> userInputData = new HashMap<>();
        userInputData.put(PASSWORD_KEY, "AnyPassword");
        FlowExecutionContext.getUserInputData().putAll(userInputData);

        FlowExecutionEngineDataHolder.getInstance().setInputValidationManagementService(null);

        ExecutorResponse response = inputValidationService.resolveInputValidationResponse(FlowExecutionContext);
        Assert.assertEquals(response.getResult(), STATUS_COMPLETE);
    }

    @Test
    public void testValidatePasswordFormatServerErrorThrowsServerException()
            throws InputValidationMgtException {

        FlowExecutionContext = initiateFlowContext();
        Map<String, String> userInputData = new HashMap<>();
        userInputData.put(PASSWORD_KEY, "SomePassword");
        FlowExecutionContext.getUserInputData().putAll(userInputData);

        InputValidationManagementService mockInputValidationService = mock(InputValidationManagementService.class);
        FlowExecutionEngineDataHolder.getInstance().setInputValidationManagementService(mockInputValidationService);

        when(mockInputValidationService.getInputValidationConfiguration(anyString()))
                .thenThrow(new InputValidationMgtException("65000", "Server error", "Server error"));

        // Server errors during password validation result in RETRY status from resolveInputValidationResponse.
        ExecutorResponse response = inputValidationService.resolveInputValidationResponse(FlowExecutionContext);
        Assert.assertEquals(response.getResult(), STATUS_RETRY);
        Assert.assertNotNull(response.getErrorCode());
    }

    @Test
    public void testRunInputValidationSkipsWhenNoConfigMatchesField()
            throws InputValidationMgtException {

        FlowExecutionContext = initiateFlowContext();
        Map<String, String> userInputData = new HashMap<>();
        userInputData.put(PASSWORD_KEY, "weak");
        FlowExecutionContext.getUserInputData().putAll(userInputData);

        InputValidationManagementService mockInputValidationService = mock(InputValidationManagementService.class);
        FlowExecutionEngineDataHolder.getInstance().setInputValidationManagementService(mockInputValidationService);

        ValidationConfiguration usernameConfig = new ValidationConfiguration();
        usernameConfig.setField("username");
        when(mockInputValidationService.getInputValidationConfiguration(anyString()))
                .thenReturn(Collections.singletonList(usernameConfig));
        when(mockInputValidationService.getValidators(anyString())).thenReturn(new HashMap<>());

        ExecutorResponse response = inputValidationService.resolveInputValidationResponse(FlowExecutionContext);
        Assert.assertEquals(response.getResult(), STATUS_COMPLETE);
    }

    @Test
    public void testRunInputValidationSkipsRuleWhenValidatorNotInMap()
            throws InputValidationMgtException {

        FlowExecutionContext = initiateFlowContext();
        Map<String, String> userInputData = new HashMap<>();
        userInputData.put(PASSWORD_KEY, "SomePassword");
        FlowExecutionContext.getUserInputData().putAll(userInputData);

        InputValidationManagementService mockInputValidationService = mock(InputValidationManagementService.class);
        FlowExecutionEngineDataHolder.getInstance().setInputValidationManagementService(mockInputValidationService);

        ValidationConfiguration passwordConfig = new ValidationConfiguration();
        passwordConfig.setField(PASSWORD_KEY);
        RulesConfiguration rule = new RulesConfiguration();
        rule.setValidatorName("UnknownValidator");
        rule.setProperties(new HashMap<>());
        passwordConfig.setRules(Collections.singletonList(rule));

        when(mockInputValidationService.getInputValidationConfiguration(anyString()))
                .thenReturn(Collections.singletonList(passwordConfig));
        when(mockInputValidationService.getValidators(anyString())).thenReturn(new HashMap<>());

        ExecutorResponse response = inputValidationService.resolveInputValidationResponse(FlowExecutionContext);
        Assert.assertEquals(response.getResult(), STATUS_COMPLETE);
    }

    @Test
    public void testRunInputValidationSkipsWhenRulesAndRegexAreNull()
            throws InputValidationMgtException {

        FlowExecutionContext = initiateFlowContext();
        Map<String, String> userInputData = new HashMap<>();
        userInputData.put(PASSWORD_KEY, "SomePassword");
        FlowExecutionContext.getUserInputData().putAll(userInputData);

        InputValidationManagementService mockInputValidationService = mock(InputValidationManagementService.class);
        FlowExecutionEngineDataHolder.getInstance().setInputValidationManagementService(mockInputValidationService);

        ValidationConfiguration passwordConfig = new ValidationConfiguration();
        passwordConfig.setField(PASSWORD_KEY);

        when(mockInputValidationService.getInputValidationConfiguration(anyString()))
                .thenReturn(Collections.singletonList(passwordConfig));
        when(mockInputValidationService.getValidators(anyString())).thenReturn(new HashMap<>());

        ExecutorResponse response = inputValidationService.resolveInputValidationResponse(FlowExecutionContext);
        Assert.assertEquals(response.getResult(), STATUS_COMPLETE);
    }

    @Test
    public void testRunInputValidationUsesRegexConfigWhenRegexIsNotNull()
            throws InputValidationMgtException {

        FlowExecutionContext = initiateFlowContext();
        Map<String, String> userInputData = new HashMap<>();
        userInputData.put(PASSWORD_KEY, "ValidPass@123");
        FlowExecutionContext.getUserInputData().putAll(userInputData);

        InputValidationManagementService mockInputValidationService = mock(InputValidationManagementService.class);
        FlowExecutionEngineDataHolder.getInstance().setInputValidationManagementService(mockInputValidationService);

        Validator mockValidator = mock(Validator.class);
        when(mockValidator.validate(org.mockito.ArgumentMatchers.any(ValidationContext.class))).thenReturn(true);
        Map<String, Validator> validators = new HashMap<>();
        validators.put("RegexValidator", mockValidator);

        ValidationConfiguration passwordConfig = new ValidationConfiguration();
        passwordConfig.setField(PASSWORD_KEY);
        RulesConfiguration regexRule = new RulesConfiguration();
        regexRule.setValidatorName("RegexValidator");
        regexRule.setProperties(new HashMap<>());
        passwordConfig.setRegEx(Collections.singletonList(regexRule));

        when(mockInputValidationService.getInputValidationConfiguration(anyString()))
                .thenReturn(Collections.singletonList(passwordConfig));
        when(mockInputValidationService.getValidators(anyString())).thenReturn(validators);

        ExecutorResponse response = inputValidationService.resolveInputValidationResponse(FlowExecutionContext);
        Assert.assertEquals(response.getResult(), STATUS_COMPLETE);
    }

    @Test
    public void testUniquenessValidationSkippedForUserResolveExecutorNode()
            throws ClaimMetadataException {

        FlowExecutionContext = initiateFlowContext();

        Map<String, String> userInputData = new HashMap<>();
        userInputData.put(CLAIM_URI_PREFIX + "email", "duplicate@example.com");
        FlowExecutionContext.getUserInputData().putAll(userInputData);

        NodeConfig userResolverNode = new NodeConfig.Builder()
                .executorConfig(new ExecutorDTO(Constants.ExecutorTypes.USER_RESOLVER))
                .build();
        FlowExecutionContext.setCurrentNode(userResolverNode);

        ClaimMetadataManagementService mockClaimService = mock(ClaimMetadataManagementService.class);
        FlowExecutionEngineDataHolder.getInstance().setClaimMetadataManagementService(mockClaimService);

        LocalClaim mockLocalClaim = mock(LocalClaim.class);
        Map<String, String> claimProperties = new HashMap<>();
        when(mockLocalClaim.getClaimProperties()).thenReturn(claimProperties);
        when(mockClaimService.getLocalClaim(anyString(), anyString())).thenReturn(Optional.of(mockLocalClaim));

        try (MockedStatic<ClaimValidationUtil> claimValidationUtilMock = mockStatic(ClaimValidationUtil.class)) {
            claimValidationUtilMock.when(() -> ClaimValidationUtil.getClaimUniquenessScope(claimProperties))
                    .thenReturn(ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES);
            claimValidationUtilMock.when(() -> ClaimValidationUtil.shouldValidateUniqueness(
                    ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES)).thenReturn(true);

            // Uniqueness validation must be skipped for UserResolveExecutor; STATUS_COMPLETE is expected.
            ExecutorResponse response = inputValidationService.resolveInputValidationResponse(FlowExecutionContext);
            Assert.assertEquals(response.getResult(), STATUS_COMPLETE);

            // Verify that isClaimDuplicated was never called since uniqueness check is skipped.
            claimValidationUtilMock.verify(
                    () -> ClaimValidationUtil.isClaimDuplicated(anyString(), anyString()), never());
        }
    }

    /**
     * Invokes the private validateUserClaims method via reflection.
     *
     * @param tenantDomain             Tenant domain.
     * @param claimUri                 Claim URI.
     * @param claimValue               Claim value to validate.
     * @param skipUniquenessValidation Whether to skip claim uniqueness validation.
     * @throws FlowEngineException If claim validation fails.
     */
    private void invokeValidateUserClaims(String tenantDomain, String claimUri, String claimValue,
                                          boolean skipUniquenessValidation) throws FlowEngineException {

        try {
            Method method = InputValidationService.class.getDeclaredMethod(
                    "validateUserClaims", String.class, String.class, String.class, boolean.class);
            method.setAccessible(true);
            method.invoke(inputValidationService, tenantDomain, claimUri, claimValue, skipUniquenessValidation);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof FlowEngineException) {
                throw (FlowEngineException) cause;
            }
            throw new RuntimeException(cause);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private FlowExecutionContext initiateFlowContext() {

        FlowExecutionContext context = new FlowExecutionContext();
        context.setTenantDomain("test.com");
        context.setContextIdentifier("flow123");
        return context;
    }
}
