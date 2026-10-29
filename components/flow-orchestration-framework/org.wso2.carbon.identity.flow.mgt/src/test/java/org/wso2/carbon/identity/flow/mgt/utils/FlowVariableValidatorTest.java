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

import org.apache.commons.lang.StringUtils;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.flow.mgt.Constants;
import org.wso2.carbon.identity.flow.mgt.Constants.FlowVariableConfigs;
import org.wso2.carbon.identity.flow.mgt.exception.FlowMgtClientException;
import org.wso2.carbon.identity.flow.mgt.model.FlowDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowRuntimeValueDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowVariableDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowVariableDataType;
import org.wso2.carbon.identity.flow.mgt.model.FlowVariablesDTO;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

/**
 * Tests the checks on the headers and runtime values a flow defines, when it is saved.
 */
public class FlowVariableValidatorTest {

    private MockedStatic<IdentityUtil> identityUtilMock;

    @BeforeMethod
    public void setUp() {

        identityUtilMock = mockStatic(IdentityUtil.class, CALLS_REAL_METHODS);
        identityUtilMock.when(() -> IdentityUtil.getProperty(ArgumentMatchers.anyString())).thenReturn(null);
        identityUtilMock.when(() -> IdentityUtil.getPropertyAsList(ArgumentMatchers.anyString()))
                .thenReturn(Collections.emptyList());
        identityUtilMock.when(() -> IdentityUtil.getProperty(FlowVariableConfigs.ENABLE)).thenReturn("true");
    }

    @AfterMethod
    public void tearDown() {

        identityUtilMock.close();
    }

    /**
     * What is saved is what the flow runs with: header names in lower case, initial values in the form of their type.
     */
    @Test
    public void testValidContextIsNormalized() throws Exception {

        FlowVariablesDTO inputs = new FlowVariablesDTO();
        inputs.getHeaders().add(input("X-AB-Variant", FlowVariableDataType.STRING_ARRAY));
        FlowRuntimeValueDTO attempts = variable("attempts", FlowVariableDataType.INTEGER);
        attempts.setValue(3);
        attempts.setClientWritable(true);
        inputs.getRuntimeValues().add(attempts);

        validate(inputs);

        assertEquals(inputs.getHeaders().get(0).getName(), "x-ab-variant");
        // The A/B testing key the flow left out comes first.
        assertEquals(inputs.getRuntimeValues().get(0).getName(), Constants.SystemRuntimeValues.AB_TESTING_KEY);
        assertEquals(inputs.getRuntimeValues().get(1).getValue(), 3L);
    }

    @Test
    public void testHeaderMustHoldAnArray() {

        FlowVariablesDTO inputs = new FlowVariablesDTO();
        inputs.getHeaders().add(input("x-ab-variant", FlowVariableDataType.STRING));

        assertRejected(inputs, "Invalid data type.");
    }

    @Test
    public void testExcludedHeaderIsRejected() {

        FlowVariablesDTO inputs = new FlowVariablesDTO();
        inputs.getHeaders().add(input("Authorization", FlowVariableDataType.STRING_ARRAY));

        assertRejected(inputs, "Excluded header.");
    }

    @Test
    public void testHeadersDifferingOnlyInCaseAreDuplicates() {

        FlowVariablesDTO inputs = new FlowVariablesDTO();
        inputs.getHeaders().add(input("X-Tier", FlowVariableDataType.STRING_ARRAY));
        inputs.getHeaders().add(input("x-tier", FlowVariableDataType.STRING_ARRAY));

        assertRejected(inputs, "Duplicate header.");
    }

    /**
     * The A/B testing key every flow defines takes a new initial value from 0 to 99, or the default without one.
     */
    @Test
    public void testAbTestingKeyTakesOnlyANewInitialValue() throws Exception {

        FlowVariablesDTO inputs = new FlowVariablesDTO();
        FlowRuntimeValueDTO abTestingKey = abTestingKey();
        abTestingKey.setValue(42);
        inputs.getRuntimeValues().add(abTestingKey);

        validate(inputs);

        assertEquals(abTestingKey.getValue(), 42L);

        abTestingKey.setValue(null);
        validate(inputs);

        assertEquals(abTestingKey.getValue(), Constants.SystemRuntimeValues.AB_TESTING_KEY_INITIAL_VALUE);
    }

    @Test
    public void testAbTestingKeyOutsideItsRangeIsRejected() {

        for (int value : new int[]{-1, 100}) {
            FlowVariablesDTO inputs = new FlowVariablesDTO();
            FlowRuntimeValueDTO abTestingKey = abTestingKey();
            abTestingKey.setValue(value);
            inputs.getRuntimeValues().add(abTestingKey);

            assertRejected(inputs, "Invalid initial value.");
        }
    }

    @Test
    public void testAbTestingKeyTypeCannotChange() {

        FlowVariablesDTO inputs = new FlowVariablesDTO();
        inputs.getRuntimeValues().add(variable(Constants.SystemRuntimeValues.AB_TESTING_KEY, FlowVariableDataType.STRING));
        assertRejected(inputs, "Invalid system runtime value.");
    }

    @Test
    public void testAbTestingKeyIsAlwaysClientWritable() throws Exception {

        FlowVariablesDTO inputs = new FlowVariablesDTO();
        FlowRuntimeValueDTO abTestingKey = abTestingKey();
        abTestingKey.setClientWritable(false);
        inputs.getRuntimeValues().add(abTestingKey);

        validate(inputs);

        assertTrue(abTestingKey.isClientWritable());
    }

    @Test
    public void testAbTestingKeyDoesNotCountTowardsTheLimit() throws Exception {

        identityUtilMock.when(() -> IdentityUtil.getProperty(FlowVariableConfigs.MAX_RUNTIME_VALUES)).thenReturn("1");
        FlowVariablesDTO inputs = new FlowVariablesDTO();
        inputs.getRuntimeValues().add(variable("plan", FlowVariableDataType.STRING));
        inputs.getRuntimeValues().add(abTestingKey());

        validate(inputs);

        inputs.getRuntimeValues().add(variable("tier", FlowVariableDataType.STRING));
        assertRejected(inputs, "Too many runtime values.");
    }

    @Test
    public void testSystemRuntimeValuesAreAddedOnlyWhenLeftOut() {

        FlowVariablesDTO inputs = FlowVariableUtils.withSystemRuntimeValues(null);

        assertEquals(inputs.getRuntimeValues().size(), 1);
        assertEquals(inputs.getRuntimeValues().get(0).getName(), Constants.SystemRuntimeValues.AB_TESTING_KEY);
        assertEquals(inputs.getRuntimeValues().get(0).getDataType(), FlowVariableDataType.INTEGER);
        assertEquals(inputs.getRuntimeValues().get(0).getValue(),
                Constants.SystemRuntimeValues.AB_TESTING_KEY_INITIAL_VALUE);

        inputs.getRuntimeValues().get(0).setValue(42L);
        assertSame(FlowVariableUtils.withSystemRuntimeValues(inputs), inputs);
        assertEquals(inputs.getRuntimeValues().size(), 1);
        assertEquals(inputs.getRuntimeValues().get(0).getValue(), 42L);

        identityUtilMock.when(() -> IdentityUtil.getProperty(FlowVariableConfigs.ENABLE)).thenReturn("false");
        assertNull(FlowVariableUtils.withSystemRuntimeValues(null));
    }

    /**
     * A flow saved without inputs is still given the system variables, so they are stored with it.
     */
    @Test
    public void testFlowSavedWithoutInputsIsGivenTheSystemRuntimeValues() throws FlowMgtClientException {

        FlowDTO flowDTO = new FlowDTO();

        FlowVariableValidator.validate(flowDTO);

        assertEquals(flowDTO.getFlowVariables().getRuntimeValues().size(), 1);
        assertEquals(flowDTO.getFlowVariables().getRuntimeValues().get(0).getName(),
                Constants.SystemRuntimeValues.AB_TESTING_KEY);

        identityUtilMock.when(() -> IdentityUtil.getProperty(FlowVariableConfigs.ENABLE)).thenReturn("false");
        FlowDTO withoutFlowVariables = new FlowDTO();

        FlowVariableValidator.validate(withoutFlowVariables);

        assertNull(withoutFlowVariables.getFlowVariables());
    }

    @Test
    public void testInitialValueMustMatchTheType() {

        FlowVariablesDTO inputs = new FlowVariablesDTO();
        FlowRuntimeValueDTO attempts = variable("attempts", FlowVariableDataType.INTEGER);
        attempts.setValue("3");
        inputs.getRuntimeValues().add(attempts);

        assertRejected(inputs, "Invalid initial value.");
    }

    /**
     * A runtime value name holds letters and digits only, up to 255 characters.
     */
    @Test
    public void testInvalidVariableNameIsRejected() {

        for (String name : Arrays.asList("plan.name", "plan_name", "plan-name", "plan name", "", null,
                StringUtils.repeat("a", 256))) {
            FlowVariablesDTO inputs = new FlowVariablesDTO();
            inputs.getRuntimeValues().add(variable(name, FlowVariableDataType.STRING));

            assertRejected(inputs, "Invalid runtime value name.");
        }
    }

    @Test
    public void testAlphanumericVariableNamesAreAccepted() throws FlowMgtClientException {

        FlowVariablesDTO inputs = new FlowVariablesDTO();
        inputs.getRuntimeValues().add(variable("plan2", FlowVariableDataType.STRING));
        inputs.getRuntimeValues().add(variable("2fa", FlowVariableDataType.BOOLEAN));
        inputs.getRuntimeValues().add(variable(StringUtils.repeat("a", 255), FlowVariableDataType.STRING));

        validate(inputs);
    }

    @Test
    public void testConfiguredLimitApplies() {

        identityUtilMock.when(() -> IdentityUtil.getProperty(FlowVariableConfigs.MAX_HEADERS)).thenReturn("1");
        FlowVariablesDTO inputs = new FlowVariablesDTO();
        inputs.getHeaders().add(input("x-campaign", FlowVariableDataType.STRING_ARRAY));
        inputs.getHeaders().add(input("x-source", FlowVariableDataType.STRING_ARRAY));

        assertRejected(inputs, "Too many headers.");
    }

    @Test
    public void testFlowVariablesAreRejectedWhenDisabled() {

        identityUtilMock.when(() -> IdentityUtil.getProperty(FlowVariableConfigs.ENABLE)).thenReturn("false");
        FlowVariablesDTO inputs = new FlowVariablesDTO();
        inputs.getHeaders().add(input("x-campaign", FlowVariableDataType.STRING_ARRAY));

        FlowMgtClientException e = expectThrows(FlowMgtClientException.class,
                () -> validate(inputs));
        assertEquals(e.getErrorCode(), Constants.ErrorMessages.ERROR_CODE_FLOW_VARIABLES_DISABLED.getCode());
    }

    private static void validate(FlowVariablesDTO inputs) throws FlowMgtClientException {

        FlowDTO flowDTO = new FlowDTO();
        flowDTO.setFlowVariables(inputs);
        FlowVariableValidator.validate(flowDTO);
    }

    private static void assertRejected(FlowVariablesDTO inputs, String message) {

        FlowMgtClientException e = expectThrows(FlowMgtClientException.class,
                () -> validate(inputs));
        assertEquals(e.getErrorCode(), Constants.ErrorMessages.ERROR_CODE_INVALID_FLOW_VARIABLES.getCode());
        assertEquals(e.getMessage(), message);
    }

    private static FlowVariableDTO input(String name, FlowVariableDataType dataType) {

        return new FlowVariableDTO(name, dataType);
    }

    private static FlowRuntimeValueDTO variable(String name, FlowVariableDataType dataType) {

        return new FlowRuntimeValueDTO(name, dataType);
    }

    private static FlowRuntimeValueDTO abTestingKey() {

        return variable(Constants.SystemRuntimeValues.AB_TESTING_KEY, FlowVariableDataType.INTEGER);
    }
}
