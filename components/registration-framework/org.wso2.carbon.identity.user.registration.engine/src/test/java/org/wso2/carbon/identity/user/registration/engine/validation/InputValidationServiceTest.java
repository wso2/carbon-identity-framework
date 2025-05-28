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

package org.wso2.carbon.identity.user.registration.engine.validation;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.user.registration.engine.exception.RegistrationEngineClientException;
import org.wso2.carbon.identity.user.registration.engine.exception.RegistrationEngineException;
import org.wso2.carbon.identity.user.registration.engine.exception.RegistrationEngineServerException;
import org.wso2.carbon.identity.user.registration.engine.model.RegistrationContext;
import org.wso2.carbon.identity.user.registration.mgt.Constants;
import org.wso2.carbon.identity.user.registration.mgt.model.ActionDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.ComponentDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.DataDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.RegistrationGraphConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.wso2.carbon.identity.user.registration.engine.Constants.CLAIM_URI_PREFIX;
import static org.wso2.carbon.identity.user.registration.engine.Constants.DEFAULT_ACTION;

public class InputValidationServiceTest {

    public static final String ACTION_1 = "action1";
    private RegistrationContext registrationContext;
    private InputValidationService inputValidationService;
    private RegistrationGraphConfig defaultGraph;

    @BeforeMethod
    public void setUp() {

        defaultGraph = new RegistrationGraphConfig();
        inputValidationService = InputValidationService.getInstance();
    }

    @AfterMethod
    public void tearDown() {

    }

    @Test(expectedExceptions = RegistrationEngineClientException.class)
    public void testValidateInputsMissingInputs() throws RegistrationEngineException {

        registrationContext = initiateRegistrationContext();
        registrationContext.setRegGraph(defaultGraph);
        Map<String, Set<String>> inputData = new HashMap<>();
        Set<String> value = new HashSet<>();
        value.add("input1");
        inputData.put(ACTION_1, value);
        registrationContext.setCurrentRequiredInputs(inputData);
        registrationContext.setCurrentStepInputs(inputData);

        Map<String, String> userInputData = new HashMap<>();
        userInputData.put("input2", "value2");
        registrationContext.getUserInputData().putAll(userInputData);
        registrationContext.setCurrentActionId(ACTION_1);
        inputValidationService.validateInputs(registrationContext);
    }

    @Test(expectedExceptions = RegistrationEngineClientException.class)
    public void testValidateInputsExtraInputs() throws RegistrationEngineException {

        registrationContext = initiateRegistrationContext();
        registrationContext.setRegGraph(defaultGraph);
        Map<String, Set<String>> inputData = new HashMap<>();
        Set<String> value = new HashSet<>();
        value.add("input1");
        inputData.put(ACTION_1, value);
        registrationContext.setCurrentRequiredInputs(inputData);
        registrationContext.setCurrentStepInputs(inputData);

        Map<String, String> userInputData = new HashMap<>();
        userInputData.put("input1", "value1");
        userInputData.put("input2", "value2");
        registrationContext.getUserInputData().putAll(userInputData);
        registrationContext.setCurrentActionId(ACTION_1);
        inputValidationService.validateInputs(registrationContext);
    }

    @Test
    public void testHandleStepInputsViewStep() throws RegistrationEngineServerException {

        registrationContext = initiateRegistrationContext();
        registrationContext.setRegGraph(defaultGraph);
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
        inputValidationService.prepareStepInputs(dataDTO, registrationContext);
        Assert.assertEquals(registrationContext.getCurrentStepInputs().size(), 2);
        Assert.assertEquals(registrationContext.getCurrentStepInputs().get("action1").size(), 2);
        Assert.assertTrue(registrationContext.getCurrentStepInputs().get("action1").contains("input1"));
        Assert.assertTrue(registrationContext.getCurrentStepInputs().get("action1").contains("input2"));
        Assert.assertEquals(registrationContext.getCurrentRequiredInputs().get("action1").size(), 1);
        Assert.assertTrue(registrationContext.getCurrentRequiredInputs().get("action1").contains("input2"));
    }

    @Test
    public void testHandleStepInputsRedirectionStep() throws RegistrationEngineServerException {

        registrationContext = initiateRegistrationContext();
        registrationContext.setRegGraph(defaultGraph);
        DataDTO dataDTO = new DataDTO.Builder()
                .requiredParams(Collections.singletonList("input1"))
                .build();
        inputValidationService.prepareStepInputs(dataDTO, registrationContext);
        Assert.assertEquals(registrationContext.getCurrentStepInputs().size(), 1);
        Assert.assertEquals(registrationContext.getCurrentStepInputs().get(DEFAULT_ACTION).size(), 1);
        Assert.assertTrue(registrationContext.getCurrentStepInputs().get(DEFAULT_ACTION).contains("input1"));
    }

    @Test
    public void testHandleUserInputs() throws RegistrationEngineServerException {

        registrationContext = initiateRegistrationContext();
        registrationContext.setRegGraph(defaultGraph);
        Map<String, String> userInputData = new HashMap<>();
        userInputData.put(CLAIM_URI_PREFIX + "input1", "value1");
        userInputData.put("input2", "value2");
        registrationContext.getUserInputData().putAll(userInputData);
        inputValidationService.handleUserInputs(registrationContext);
        Assert.assertEquals(registrationContext.getRegisteringUser().getClaims().size(), 1);
        Assert.assertEquals(registrationContext.getUserInputData().size(), 1);
        Assert.assertEquals(registrationContext.getRegisteringUser().getClaims().get(CLAIM_URI_PREFIX + "input1"),
                "value1");
        Assert.assertEquals(registrationContext.getUserInputData().get("input2"), "value2");
    }

    private RegistrationContext initiateRegistrationContext() {

        RegistrationContext context = new RegistrationContext();
        context.setTenantDomain("test.com");
        context.setContextIdentifier("flow123");
        return context;
    }
}
