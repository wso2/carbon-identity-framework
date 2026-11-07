/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineClientException;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.execution.engine.internal.FlowExecutionEngineDataHolder;
import org.wso2.carbon.identity.flow.execution.engine.model.ExecutorResponse;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.mgt.Constants.FlowTypes;
import org.wso2.carbon.identity.input.validation.mgt.exceptions.InputValidationMgtClientException;
import org.wso2.carbon.identity.input.validation.mgt.exceptions.InputValidationMgtException;
import org.wso2.carbon.identity.input.validation.mgt.model.RulesConfiguration;
import org.wso2.carbon.identity.input.validation.mgt.model.ValidationConfiguration;
import org.wso2.carbon.identity.input.validation.mgt.model.ValidationContext;
import org.wso2.carbon.identity.input.validation.mgt.model.Validator;
import org.wso2.carbon.identity.input.validation.mgt.services.InputValidationManagementService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_FLOW_INPUT_VALIDATOR_FAILURE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_PASSWORD_FORMAT_VALIDATION_FAILED;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorStatus.STATUS_COMPLETE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorStatus.STATUS_RETRY;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.PASSWORD_KEY;

/**
 * Tests for the flow input validator extension point.
 */
public class FlowInputValidatorTest {

    private static final String USERNAME_CLAIM_URI = "http://wso2.org/claims/username";
    private static final String REJECT_CODE = "TEST-60001";
    private static final String REJECT_MESSAGE = "The input is not allowed.";

    private final InputValidationService inputValidationService = InputValidationService.getInstance();
    private final List<FlowInputValidator> registeredValidators = new ArrayList<>();
    private final List<String> invocations = new ArrayList<>();

    @BeforeMethod
    public void setUp() {

        FlowExecutionEngineDataHolder.getInstance().setInputValidationManagementService(null);
        invocations.clear();
    }

    @AfterMethod
    public void tearDown() {

        registeredValidators.forEach(FlowExecutionEngineDataHolder.getInstance()::removeFlowInputValidator);
        registeredValidators.clear();
        FlowExecutionEngineDataHolder.getInstance().setInputValidationManagementService(null);
    }

    @Test
    public void testAcceptingValidatorCompletesValidation() {

        register(new TestValidator("accept", 100, Collections.emptySet(), true, request -> { }));
        FlowExecutionContext context = buildContext(FlowTypes.REGISTRATION, PASSWORD_KEY, "Password@123");

        ExecutorResponse response = inputValidationService.resolveInputValidationResponse(context);

        Assert.assertEquals(response.getResult(), STATUS_COMPLETE);
        Assert.assertEquals(invocations, Collections.singletonList("accept"));
    }

    @Test
    public void testRejectingValidatorReturnsRetryWithItsError() {

        register(new TestValidator("reject", 100, Collections.emptySet(), true, request -> {
            throw new RejectException();
        }));
        FlowExecutionContext context = buildContext(FlowTypes.REGISTRATION, PASSWORD_KEY, "Password@123");

        ExecutorResponse response = inputValidationService.resolveInputValidationResponse(context);

        Assert.assertEquals(response.getResult(), STATUS_RETRY);
        Assert.assertEquals(response.getErrorCode(), REJECT_CODE);
        Assert.assertEquals(response.getErrorMessage(), REJECT_MESSAGE);
    }

    @Test
    public void testRequestExposesOnlyTheSubmittedInputs() {

        Map<String, String> seenInputs = new HashMap<>();
        register(new TestValidator("inspect", 100, Collections.emptySet(), true, request -> {
            seenInputs.putAll(request.getInputs());
            Assert.assertEquals(request.getTenantDomain(), "test.com");
            Assert.assertEquals(request.getFlowType(), FlowTypes.REGISTRATION.getType());
            Assert.assertThrows(UnsupportedOperationException.class,
                    () -> request.getInputs().put("other", "value"));
            // Reject so that the flow stops before the inputs are handled, which needs a realm service.
            throw new RejectException();
        }));
        FlowExecutionContext context = buildContext(FlowTypes.REGISTRATION, PASSWORD_KEY, "Password@123");
        // A claim collected in an earlier step must not appear in the request.
        context.getFlowUser().getClaims().put(USERNAME_CLAIM_URI, "john");

        ExecutorResponse response = inputValidationService.resolveInputValidationResponse(context);

        Assert.assertEquals(response.getErrorCode(), REJECT_CODE);
        Assert.assertEquals(seenInputs, Collections.singletonMap(PASSWORD_KEY, "Password@123"));
    }

    @Test
    public void testValidatorForOtherFlowTypeIsSkipped() {

        register(new TestValidator("recovery-only", 100,
                Collections.singleton(FlowTypes.PASSWORD_RECOVERY), true, request -> {
            throw new RejectException();
        }));
        FlowExecutionContext context = buildContext(FlowTypes.REGISTRATION, PASSWORD_KEY, "Password@123");

        ExecutorResponse response = inputValidationService.resolveInputValidationResponse(context);

        Assert.assertEquals(response.getResult(), STATUS_COMPLETE);
        Assert.assertTrue(invocations.isEmpty());
    }

    @Test
    public void testValidatorThatCannotHandleIsSkipped() {

        register(new TestValidator("no-handle", 100, Collections.emptySet(), false, request -> {
            throw new RejectException();
        }));
        FlowExecutionContext context = buildContext(FlowTypes.REGISTRATION, PASSWORD_KEY, "Password@123");

        ExecutorResponse response = inputValidationService.resolveInputValidationResponse(context);

        Assert.assertEquals(response.getResult(), STATUS_COMPLETE);
        Assert.assertTrue(invocations.isEmpty());
    }

    @Test
    public void testValidatorsRunByOrderAndStopAtFirstRejection() {

        register(new TestValidator("third", 300, Collections.emptySet(), true, request -> { }));
        register(new TestValidator("second", 200, Collections.emptySet(), true, request -> {
            throw new RejectException();
        }));
        register(new TestValidator("first", 100, Collections.emptySet(), true, request -> { }));
        FlowExecutionContext context = buildContext(FlowTypes.REGISTRATION, PASSWORD_KEY, "Password@123");

        ExecutorResponse response = inputValidationService.resolveInputValidationResponse(context);

        Assert.assertEquals(response.getResult(), STATUS_RETRY);
        Assert.assertEquals(invocations, Arrays.asList("first", "second"));
    }

    @Test
    public void testRuntimeExceptionBecomesServerError() {

        register(new TestValidator("broken", 100, Collections.emptySet(), true, request -> {
            throw new IllegalStateException("Unexpected failure");
        }));
        FlowExecutionContext context = buildContext(FlowTypes.REGISTRATION, PASSWORD_KEY, "Password@123");

        ExecutorResponse response = inputValidationService.resolveInputValidationResponse(context);

        Assert.assertEquals(response.getResult(), STATUS_RETRY);
        Assert.assertEquals(response.getErrorCode(), ERROR_CODE_FLOW_INPUT_VALIDATOR_FAILURE.getCode());
    }

    @Test
    public void testBuiltInValidationFailureSkipsValidators() throws InputValidationMgtException {

        register(new TestValidator("after-format", 100, Collections.emptySet(), true, request -> { }));
        mockFailingPasswordFormatValidation();
        FlowExecutionContext context = buildContext(FlowTypes.REGISTRATION, PASSWORD_KEY, "weak");

        ExecutorResponse response = inputValidationService.resolveInputValidationResponse(context);

        Assert.assertEquals(response.getResult(), STATUS_RETRY);
        Assert.assertEquals(response.getErrorCode(), ERROR_CODE_PASSWORD_FORMAT_VALIDATION_FAILED.getCode());
        Assert.assertTrue(invocations.isEmpty());
    }

    @Test
    public void testRemovedValidatorIsNotCalled() {

        FlowInputValidator validator = new TestValidator("removed", 100, Collections.emptySet(), true, request -> {
            throw new RejectException();
        });
        FlowExecutionEngineDataHolder.getInstance().addFlowInputValidator(validator);
        FlowExecutionEngineDataHolder.getInstance().removeFlowInputValidator(validator);
        FlowExecutionContext context = buildContext(FlowTypes.REGISTRATION, PASSWORD_KEY, "Password@123");

        ExecutorResponse response = inputValidationService.resolveInputValidationResponse(context);

        Assert.assertEquals(response.getResult(), STATUS_COMPLETE);
        Assert.assertTrue(invocations.isEmpty());
    }

    private void register(FlowInputValidator validator) {

        FlowExecutionEngineDataHolder.getInstance().addFlowInputValidator(validator);
        registeredValidators.add(validator);
    }

    private FlowExecutionContext buildContext(FlowTypes flowType, String inputKey, String inputValue) {

        FlowExecutionContext context = new FlowExecutionContext();
        context.setTenantDomain("test.com");
        context.setContextIdentifier("flow123");
        context.setFlowType(flowType.getType());
        context.getUserInputData().put(inputKey, inputValue);
        return context;
    }

    private void mockFailingPasswordFormatValidation() throws InputValidationMgtException {

        InputValidationManagementService service = mock(InputValidationManagementService.class);
        Validator lengthValidator = mock(Validator.class);
        when(lengthValidator.validate(any(ValidationContext.class)))
                .thenThrow(new InputValidationMgtClientException("60001", "Password too short", "Password too short"));
        ValidationConfiguration passwordConfig = new ValidationConfiguration();
        passwordConfig.setField(PASSWORD_KEY);
        RulesConfiguration rule = new RulesConfiguration();
        rule.setValidatorName("LengthValidator");
        rule.setProperties(new HashMap<>());
        passwordConfig.setRules(Collections.singletonList(rule));
        when(service.getInputValidationConfiguration(anyString())).thenReturn(Collections.singletonList(passwordConfig));
        when(service.getValidators(anyString())).thenReturn(Collections.singletonMap("LengthValidator",
                lengthValidator));
        FlowExecutionEngineDataHolder.getInstance().setInputValidationManagementService(service);
    }

    /**
     * Unchecked wrapper so that test lambdas can signal a client rejection.
     */
    private static class RejectException extends RuntimeException {

        private static final long serialVersionUID = 1L;
    }

    /**
     * Configurable flow input validator for tests. It records each call to validate.
     */
    private class TestValidator implements FlowInputValidator {

        private final String name;
        private final int order;
        private final Set<FlowTypes> flowTypes;
        private final boolean canHandle;
        private final Consumer<FlowInputValidationRequest> behavior;

        TestValidator(String name, int order, Set<FlowTypes> flowTypes, boolean canHandle,
                      Consumer<FlowInputValidationRequest> behavior) {

            this.name = name;
            this.order = order;
            this.flowTypes = flowTypes;
            this.canHandle = canHandle;
            this.behavior = behavior;
        }

        @Override
        public String getName() {

            return name;
        }

        @Override
        public int getOrder() {

            return order;
        }

        @Override
        public Set<FlowTypes> getSupportedFlowTypes() {

            return flowTypes;
        }

        @Override
        public boolean canHandle(FlowInputValidationRequest request) {

            return canHandle;
        }

        @Override
        public void validate(FlowInputValidationRequest request) throws FlowEngineException {

            invocations.add(name);
            try {
                behavior.accept(request);
            } catch (RejectException e) {
                throw new FlowEngineClientException(REJECT_CODE, REJECT_MESSAGE, "Rejected by " + name);
            }
        }
    }
}
