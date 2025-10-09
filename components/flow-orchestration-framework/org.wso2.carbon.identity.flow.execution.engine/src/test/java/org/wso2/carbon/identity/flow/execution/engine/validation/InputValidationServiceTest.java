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

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineClientException;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineServerException;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.mgt.Constants;
import org.wso2.carbon.identity.flow.mgt.model.ActionDTO;
import org.wso2.carbon.identity.flow.mgt.model.ComponentDTO;
import org.wso2.carbon.identity.flow.mgt.model.DataDTO;
import org.wso2.carbon.identity.flow.mgt.model.GraphConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.wso2.carbon.identity.flow.execution.engine.Constants.CLAIM_URI_PREFIX;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.DEFAULT_ACTION;

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
    public void testHandleUserInputs() throws FlowEngineServerException {

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

    private FlowExecutionContext initiateFlowContext() {

        FlowExecutionContext context = new FlowExecutionContext();
        context.setTenantDomain("test.com");
        context.setContextIdentifier("flow123");
        return context;
    }
}
