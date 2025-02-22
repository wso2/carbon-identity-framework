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

package org.wso2.carbon.identity.user.registration.mgt.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.user.registration.mgt.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.registration.mgt.model.ActionDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.ComponentDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.ExecutorDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.RegistrationFlowConfig;

import java.io.IOException;
import org.wso2.carbon.identity.user.registration.mgt.model.RegistrationFlowDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.StepDTO;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

public class FlowConvertorTest {

    @Test()
    void testSequenceWithNoNodes() throws IOException {

        RegistrationFlowDTO registrationFlowDTO = createSampleRegistrationFlow();
        try {
            RegistrationFlowConfig config = FlowConvertor.getSequence(registrationFlowDTO);
        } catch (RegistrationFrameworkException e) {
            throw new RuntimeException(e);
        }
    }

    public static RegistrationFlowDTO createSampleRegistrationFlow() {
        RegistrationFlowDTO registrationFlowDTO = new RegistrationFlowDTO();

        // Step 1
        StepDTO step1 = new StepDTO.Builder()
                .id("step_23sd")
                .type("VIEW")
                .coordinateX(0)
                .coordinateY(0)
                .width(120)
                .height(240)
                .data(createStep1Data())
                .build();

        // Step 2
        StepDTO step2 = new StepDTO.Builder()
                .id("step_a5sf")
                .type("VIEW")
                .coordinateX(0)
                .coordinateY(0)
                .width(120)
                .height(240)
                .data(createStep2Data())
                .build();

        // Step 3
        StepDTO step3 = new StepDTO.Builder()
                .id("step_dfr2")
                .type("REDIRECTION")
                .coordinateX(0)
                .coordinateY(0)
                .width(120)
                .height(240)
                .data(createStep3Data())
                .build();

        // Add steps to registration flow
        List<StepDTO> steps = new ArrayList<>();
        steps.add(step1);
        steps.add(step2);
        steps.add(step3);
        registrationFlowDTO.setSteps(steps);

        return registrationFlowDTO;
    }

    private static Map<String, Object> createStep1Data() {
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> components = new HashMap<>();

        ComponentDTO blockComponent = new ComponentDTO.Builder()
                .id("component_232d")
                .category("BLOCK")
                .type("FORM")
                .properties(createStep1BlockComponents())
                .build();

        ComponentDTO buttonComponent = new ComponentDTO.Builder()
                .id("element_gd43")
                .category("BUTTON")
                .type("BUTTON")
                .property("text", "Continue with Google")
                .action(new ActionDTO.Builder()
                                .setType("NEXT")
                                .setNextId("step_dfr2")
                                .build())
                .build();

        components.put(blockComponent.getId(), blockComponent);
        components.put(buttonComponent.getId(), buttonComponent);
        data.put("components", components);

        return data;
    }

    private static Map<String, Object> createStep1BlockComponents() {
        Map<String, Object> components = new HashMap<>();

        ComponentDTO usernameField = new ComponentDTO.Builder()
                .id("element_gd43")
                .category("FIELD")
                .type("INPUT")
                .property("label", "Username")
                .property("placeholder", "Enter your username")
                .property("required", true)
                .property("type", "text")
                .build();

        ComponentDTO passwordField = new ComponentDTO.Builder()
                .id("element_23dx")
                .category("FIELD")
                .type("INPUT")
                .property("label", "Password")
                .property("placeholder", "Enter your password")
                .property("required", true)
                .property("type", "password")
                .build();

        ComponentDTO continueButton = new ComponentDTO.Builder()
                .id("element_56jd")
                .category("BUTTON")
                .type("BUTTON")
                .property("text", "Continue")
                .action(new ActionDTO.Builder()
                                .setType("EXECUTOR")
                                .setExecutor(new ExecutorDTO.Builder().name("PasswordOnboardExecutor").build())
                                .setNextId("step_a5sf")
                                .build())
                .build();

        components.put(usernameField.getId(), usernameField);
        components.put(passwordField.getId(), passwordField);
        components.put(continueButton.getId(), continueButton);

        return components;
    }

    private static Map<String, Object> createStep2Data() {
        Map<String, Object> data = new HashMap<>();
        Map<String, ComponentDTO> components = new HashMap<>();

        ComponentDTO blockComponent = new ComponentDTO.Builder()
                .id("component_232d")
                .category("BLOCK")
                .type("FORM")
                .properties(createStep2BlockComponents())
                .build();

        components.put(blockComponent.getId(), blockComponent);
        data.put("components", components);

        return data;
    }

    private static Map<String, Object> createStep2BlockComponents() {
        Map<String, Object> components = new HashMap<>();

        ComponentDTO emailField = new ComponentDTO.Builder()
                .id("element_gd43")
                .category("FIELD")
                .type("INPUT")
                .property("label", "Email")
                .property("placeholder", "Enter your email")
                .property("required", true)
                .property("type", "text")
                .build();

        ComponentDTO phoneField = new ComponentDTO.Builder()
                .id("element_23dx")
                .category("FIELD")
                .type("INPUT")
                .property("label", "Phone")
                .property("placeholder", "Enter your phone")
                .property("required", true)
                .property("type", "text")
                .build();

        ComponentDTO completeButton = new ComponentDTO.Builder()
                .id("element_56jd")
                .category("BUTTON")
                .type("BUTTON")
                .property("text", "Complete Registration")
                .action(new ActionDTO.Builder()
                                .setType("NEXT")
                                .setNextId("COMPLETE")
                                .build())
                .build();

        components.put(emailField.getId(), emailField);
        components.put(phoneField.getId(), phoneField);
        components.put(completeButton.getId(), completeButton);

        return components;
    }

    private static Map<String, Object> createStep3Data() {
        Map<String, Object> data = new HashMap<>();

        ActionDTO action = new ActionDTO.Builder()
                .setType("EXECUTOR")
                .setExecutor(new ExecutorDTO.Builder().name("GoogleOIDCAuthenticator").build())
                .setNextId("COMPLETE")
                .build();

        data.put("action", action);

        return data;
    }

}