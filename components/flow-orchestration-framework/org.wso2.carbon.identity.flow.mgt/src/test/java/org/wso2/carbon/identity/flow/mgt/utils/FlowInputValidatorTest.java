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
import org.wso2.carbon.identity.flow.mgt.Constants.FlowInputConfigs;
import org.wso2.carbon.identity.flow.mgt.Constants;
import org.wso2.carbon.identity.flow.mgt.exception.FlowMgtClientException;
import org.wso2.carbon.identity.flow.mgt.model.FlowDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowInputDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowInputDataType;
import org.wso2.carbon.identity.flow.mgt.model.FlowInputsDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowVariableInputDTO;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.expectThrows;

/**
 * Tests the checks on the headers, query parameters and variables a flow defines, when it is saved.
 */
public class FlowInputValidatorTest {

    private MockedStatic<IdentityUtil> identityUtilMock;

    @BeforeMethod
    public void setUp() {

        identityUtilMock = mockStatic(IdentityUtil.class, CALLS_REAL_METHODS);
        identityUtilMock.when(() -> IdentityUtil.getProperty(ArgumentMatchers.anyString())).thenReturn(null);
        identityUtilMock.when(() -> IdentityUtil.getPropertyAsList(ArgumentMatchers.anyString()))
                .thenReturn(Collections.emptyList());
        identityUtilMock.when(() -> IdentityUtil.getProperty(FlowInputConfigs.ENABLE)).thenReturn("true");
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

        FlowInputsDTO inputs = new FlowInputsDTO();
        inputs.getHeaders().add(input("X-AB-Variant", FlowInputDataType.STRING_ARRAY));
        inputs.getQueryParams().add(input("campaign", FlowInputDataType.STRING_ARRAY));
        FlowVariableInputDTO attempts = variable("attempts", FlowInputDataType.INTEGER);
        attempts.setValue(3);
        attempts.setClientWritable(true);
        inputs.getVariables().add(attempts);

        validate(inputs);

        assertEquals(inputs.getHeaders().get(0).getName(), "x-ab-variant");
        // The A/B testing key the flow left out comes first.
        assertEquals(inputs.getVariables().get(0).getName(), Constants.SystemVariables.AB_TESTING_KEY);
        assertEquals(inputs.getVariables().get(1).getValue(), 3L);
    }

    @Test
    public void testHeaderMustHoldAnArray() {

        FlowInputsDTO inputs = new FlowInputsDTO();
        inputs.getHeaders().add(input("x-ab-variant", FlowInputDataType.STRING));

        assertRejected(inputs, "Invalid data type.");
    }

    @Test
    public void testExcludedHeaderIsRejected() {

        FlowInputsDTO inputs = new FlowInputsDTO();
        inputs.getHeaders().add(input("Authorization", FlowInputDataType.STRING_ARRAY));

        assertRejected(inputs, "Excluded header.");
    }

    @Test
    public void testExcludedQueryParamIsRejected() {

        FlowInputsDTO inputs = new FlowInputsDTO();
        inputs.getQueryParams().add(input("sessionDataKey", FlowInputDataType.STRING_ARRAY));

        assertRejected(inputs, "Excluded query parameter.");
    }

    @Test
    public void testHeadersDifferingOnlyInCaseAreDuplicates() {

        FlowInputsDTO inputs = new FlowInputsDTO();
        inputs.getHeaders().add(input("X-Tier", FlowInputDataType.STRING_ARRAY));
        inputs.getHeaders().add(input("x-tier", FlowInputDataType.STRING_ARRAY));

        assertRejected(inputs, "Duplicate header.");
    }

    /**
     * The A/B testing key every flow defines takes a new initial value from 0 to 99, or the default without one.
     */
    @Test
    public void testAbTestingKeyTakesOnlyANewInitialValue() throws Exception {

        FlowInputsDTO inputs = new FlowInputsDTO();
        FlowVariableInputDTO abTestingKey = abTestingKey();
        abTestingKey.setValue(42);
        inputs.getVariables().add(abTestingKey);

        validate(inputs);

        assertEquals(abTestingKey.getValue(), 42L);

        abTestingKey.setValue(null);
        validate(inputs);

        assertEquals(abTestingKey.getValue(), Constants.SystemVariables.AB_TESTING_KEY_INITIAL_VALUE);
    }

    @Test
    public void testAbTestingKeyOutsideItsRangeIsRejected() {

        for (int value : new int[]{-1, 100}) {
            FlowInputsDTO inputs = new FlowInputsDTO();
            FlowVariableInputDTO abTestingKey = abTestingKey();
            abTestingKey.setValue(value);
            inputs.getVariables().add(abTestingKey);

            assertRejected(inputs, "Invalid initial value.");
        }
    }

    @Test
    public void testAbTestingKeyTypeAndUpdatabilityCannotChange() {

        FlowInputsDTO inputs = new FlowInputsDTO();
        inputs.getVariables().add(variable(Constants.SystemVariables.AB_TESTING_KEY, FlowInputDataType.STRING));
        assertRejected(inputs, "Invalid system variable.");

        inputs = new FlowInputsDTO();
        FlowVariableInputDTO abTestingKey = abTestingKey();
        abTestingKey.setClientWritable(true);
        inputs.getVariables().add(abTestingKey);
        assertRejected(inputs, "Invalid system variable.");
    }

    @Test
    public void testAbTestingKeyDoesNotCountTowardsTheLimit() throws Exception {

        identityUtilMock.when(() -> IdentityUtil.getProperty(FlowInputConfigs.MAX_VARIABLES)).thenReturn("1");
        FlowInputsDTO inputs = new FlowInputsDTO();
        inputs.getVariables().add(variable("plan", FlowInputDataType.STRING));
        inputs.getVariables().add(abTestingKey());

        validate(inputs);

        inputs.getVariables().add(variable("tier", FlowInputDataType.STRING));
        assertRejected(inputs, "Too many variables.");
    }

    @Test
    public void testSystemVariablesAreAddedOnlyWhenLeftOut() {

        FlowInputsDTO inputs = FlowInputUtils.withSystemVariables(null);

        assertEquals(inputs.getVariables().size(), 1);
        assertEquals(inputs.getVariables().get(0).getName(), Constants.SystemVariables.AB_TESTING_KEY);
        assertEquals(inputs.getVariables().get(0).getDataType(), FlowInputDataType.INTEGER);
        assertEquals(inputs.getVariables().get(0).getValue(),
                Constants.SystemVariables.AB_TESTING_KEY_INITIAL_VALUE);

        inputs.getVariables().get(0).setValue(42L);
        assertSame(FlowInputUtils.withSystemVariables(inputs), inputs);
        assertEquals(inputs.getVariables().size(), 1);
        assertEquals(inputs.getVariables().get(0).getValue(), 42L);

        identityUtilMock.when(() -> IdentityUtil.getProperty(FlowInputConfigs.ENABLE)).thenReturn("false");
        assertNull(FlowInputUtils.withSystemVariables(null));
    }

    /**
     * A flow saved without inputs is still given the system variables, so they are stored with it.
     */
    @Test
    public void testFlowSavedWithoutInputsIsGivenTheSystemVariables() throws FlowMgtClientException {

        FlowDTO flowDTO = new FlowDTO();

        FlowInputValidator.validate(flowDTO);

        assertEquals(flowDTO.getFlowInputs().getVariables().size(), 1);
        assertEquals(flowDTO.getFlowInputs().getVariables().get(0).getName(),
                Constants.SystemVariables.AB_TESTING_KEY);

        identityUtilMock.when(() -> IdentityUtil.getProperty(FlowInputConfigs.ENABLE)).thenReturn("false");
        FlowDTO withoutFlowInputs = new FlowDTO();

        FlowInputValidator.validate(withoutFlowInputs);

        assertNull(withoutFlowInputs.getFlowInputs());
    }

    @Test
    public void testInitialValueMustMatchTheType() {

        FlowInputsDTO inputs = new FlowInputsDTO();
        FlowVariableInputDTO attempts = variable("attempts", FlowInputDataType.INTEGER);
        attempts.setValue("3");
        inputs.getVariables().add(attempts);

        assertRejected(inputs, "Invalid initial value.");
    }

    /**
     * A variable name holds letters and digits only, up to 255 characters.
     */
    @Test
    public void testInvalidVariableNameIsRejected() {

        for (String name : Arrays.asList("plan.name", "plan_name", "plan-name", "plan name", "", null,
                StringUtils.repeat("a", 256))) {
            FlowInputsDTO inputs = new FlowInputsDTO();
            inputs.getVariables().add(variable(name, FlowInputDataType.STRING));

            assertRejected(inputs, "Invalid variable name.");
        }
    }

    @Test
    public void testAlphanumericVariableNamesAreAccepted() throws FlowMgtClientException {

        FlowInputsDTO inputs = new FlowInputsDTO();
        inputs.getVariables().add(variable("plan2", FlowInputDataType.STRING));
        inputs.getVariables().add(variable("2fa", FlowInputDataType.BOOLEAN));
        inputs.getVariables().add(variable(StringUtils.repeat("a", 255), FlowInputDataType.STRING));

        validate(inputs);
    }

    @Test
    public void testConfiguredLimitApplies() {

        identityUtilMock.when(() -> IdentityUtil.getProperty(FlowInputConfigs.MAX_QUERY_PARAMS)).thenReturn("1");
        FlowInputsDTO inputs = new FlowInputsDTO();
        inputs.getQueryParams().add(input("campaign", FlowInputDataType.STRING_ARRAY));
        inputs.getQueryParams().add(input("source", FlowInputDataType.STRING_ARRAY));

        assertRejected(inputs, "Too many query parameters.");
    }

    @Test
    public void testFlowInputsAreRejectedWhenDisabled() {

        identityUtilMock.when(() -> IdentityUtil.getProperty(FlowInputConfigs.ENABLE)).thenReturn("false");
        FlowInputsDTO inputs = new FlowInputsDTO();
        inputs.getQueryParams().add(input("campaign", FlowInputDataType.STRING_ARRAY));

        FlowMgtClientException e = expectThrows(FlowMgtClientException.class,
                () -> validate(inputs));
        assertEquals(e.getErrorCode(), Constants.ErrorMessages.ERROR_CODE_FLOW_INPUTS_DISABLED.getCode());
    }

    private static void validate(FlowInputsDTO inputs) throws FlowMgtClientException {

        FlowDTO flowDTO = new FlowDTO();
        flowDTO.setFlowInputs(inputs);
        FlowInputValidator.validate(flowDTO);
    }

    private static void assertRejected(FlowInputsDTO inputs, String message) {

        FlowMgtClientException e = expectThrows(FlowMgtClientException.class,
                () -> validate(inputs));
        assertEquals(e.getErrorCode(), Constants.ErrorMessages.ERROR_CODE_INVALID_FLOW_INPUTS.getCode());
        assertEquals(e.getMessage(), message);
    }

    private static FlowInputDTO input(String name, FlowInputDataType dataType) {

        return new FlowInputDTO(name, dataType);
    }

    private static FlowVariableInputDTO variable(String name, FlowInputDataType dataType) {

        return new FlowVariableInputDTO(name, dataType);
    }

    private static FlowVariableInputDTO abTestingKey() {

        return variable(Constants.SystemVariables.AB_TESTING_KEY, FlowInputDataType.INTEGER);
    }
}
