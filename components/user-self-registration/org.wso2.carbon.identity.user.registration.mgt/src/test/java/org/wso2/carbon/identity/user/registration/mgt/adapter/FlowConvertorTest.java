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
import java.util.List;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.user.registration.mgt.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.registration.mgt.model.ActionDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.BlockDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.ElementDTO;
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

        RegistrationFlowDTO registrationFlowDTO = createSampleRegistrationFlow2();
        try {
            RegistrationFlowConfig config = FlowConvertor.getSequence(registrationFlowDTO);
        } catch (RegistrationFrameworkException e) {
            throw new RuntimeException(e);
        }
    }

    public static RegistrationFlowDTO createSampleRegistrationFlow1() {
        RegistrationFlowDTO registrationFlowDTO = new RegistrationFlowDTO();

        // Step 1
        StepDTO step1 = new StepDTO();
        step1.setId("dnd-step-3fa85f64-5717-4562-b3fc-2c963f66afa6");
        step1.setType("VIEW");
        step1.setCoordinateX(100);
        step1.setCoordinateY(100);
        step1.setWidth(600);
        step1.setHeight(500);

        BlockDTO block1 = new BlockDTO();
        block1.setId("dnd-block-345e95c0-d280-65b0-9646-754bb340f64");

        ElementDTO element1 = new ElementDTO("dnd-element-210e95c0-c580-40b0-9646-7054bb340f64", "INPUT");
        element1.setCategory("FIELD");
        element1.addProperty("label", "First Name");
        element1.addProperty("placeholder", "Enter your first name");

        ElementDTO element2 = new ElementDTO("dnd-element-211e95c0-c580-40b0-9646-7054bb340f65", "INPUT");
        element2.setCategory("FIELD");
        element2.addProperty("label", "Last Name");
        element2.addProperty("placeholder", "Enter your last name");

        ElementDTO element3 = new ElementDTO("dnd-element-212e95c0-c580-40b0-9646-7054bb340f66", "INPUT");
        element3.setCategory("FIELD");
        element3.addProperty("label", "Email");
        element3.addProperty("placeholder", "Enter your email");

        ElementDTO element4 = new ElementDTO("dnd-element-213e95c0-c580-40b0-9646-7054bb340f67", "INPUT");
        element4.setCategory("FIELD");
        element4.addProperty("label", "Password");
        element4.addProperty("placeholder", "Enter your password");

        ElementDTO element5 = new ElementDTO("dnd-element-214e95c0-c580-40b0-9646-7054bb340f68", "BUTTON");
        element5.setCategory("ACTION");
        element5.addProperty("text", "Next");
        ActionDTO action1 = new ActionDTO("EXECUTOR");
        ExecutorDTO executor1 = new ExecutorDTO("PasswordOnboarder");
        action1.setExecutor(executor1);
        action1.setNextId("dnd-step-4fa85f64-5717-4562-b3fc-2c963f66afa7");
        element5.setAction(action1);

        block1.addElementDto(element1);
        block1.addElementDto(element2);
        block1.addElementDto(element3);
        block1.addElementDto(element4);
        block1.addElementDto(element5);

        BlockDTO block2 = new BlockDTO();
        block2.setId("dnd-block-545e95c0-d280-65b0-9646-754bb340f66");

        ElementDTO element6 = new ElementDTO("dnd-element-217e95c0-c580-40b0-9646-7054bb340f71", "BUTTON");
        element6.setCategory("ACTION");
        element6.addProperty("label", "Sign up with Google");
        element6.addProperty("icon", "google");
        element6.addProperty("position", "CENTER");
        ActionDTO action2 = new ActionDTO("NEXT");
        action2.setNextId("dnd-step-5fa85f64-5717-4562-b3fc-2c963f66afa8");
        element6.setAction(action2);

        block2.addElementDto(element6);

        step1.getBlocks().add(block1);
        step1.getBlocks().add(block2);

        // Step 2
        StepDTO step2 = new StepDTO();
        step2.setId("dnd-step-4fa85f64-5717-4562-b3fc-2c963f66afa7");
        step2.setType("VIEW");
        step2.setCoordinateX(100);
        step2.setCoordinateY(550);
        step2.setWidth(600);
        step2.setHeight(300);

        BlockDTO block3 = new BlockDTO();
        block3.setId("dnd-block-445e95c0-d280-65b0-9646-754bb340f65");

        ElementDTO element7 = new ElementDTO("dnd-element-215e95c0-c580-40b0-9646-7054bb340f69", "INPUT");
        element7.setCategory("FIELD");
        element7.addProperty("label", "Email Verification Code");
        element7.addProperty("placeholder", "Enter the OTP sent to your email");

        ElementDTO element8 = new ElementDTO("dnd-element-216e95c0-c580-40b0-9646-7054bb340f70", "BUTTON");
        element8.setCategory("ACTION");
        element8.addProperty("label", "Submit");
        element8.addProperty("position", "RIGHT");
        ActionDTO action3 = new ActionDTO("EXECUTOR");
        ExecutorDTO executor2 = new ExecutorDTO("EmailOTPExecutor");
        action3.setExecutor(executor2);
        action3.setNextId("COMPLETE");
        element8.setAction(action3);

        block3.addElementDto(element7);
        block3.addElementDto(element8);

        step2.getBlocks().add(block3);

        // Step 3
        StepDTO step3 = new StepDTO();
        step3.setId("dnd-step-5fa85f64-5717-4562-b3fc-2c963f66afa8");
        step3.setType("SERVICE");
        step3.setCoordinateX(350);
        step3.setCoordinateY(300);
        step3.setWidth(150);
        step3.setHeight(150);

        ActionDTO action4 = new ActionDTO("EXECUTOR");
        ExecutorDTO executor3 = new ExecutorDTO("GoogleOIDCAuthenticator", "google");
        action4.setExecutor(executor3);
        action4.setNextId("COMPLETE");
        step3.setActionDTO(action4);

        // Add steps to registration flow
        List<StepDTO> steps = new ArrayList<>();
        steps.add(step1);
        steps.add(step2);
        steps.add(step3);
        registrationFlowDTO.setSteps(steps);

        return registrationFlowDTO;
    }

    public static RegistrationFlowDTO createSampleRegistrationFlow2() {
        RegistrationFlowDTO registrationFlowDTO = new RegistrationFlowDTO();

        // Step 1
        StepDTO step1 = new StepDTO();
        step1.setId("step1");
        step1.setType("VIEW");
        step1.setCoordinateX(100);
        step1.setCoordinateY(100);
        step1.setWidth(600);
        step1.setHeight(500);

        BlockDTO block1 = new BlockDTO();
        block1.setId("dnd-block-345e95c0-d280-65b0-9646-754bb340f64");

        ElementDTO element1 = new ElementDTO("dnd-element-210e95c0-c580-40b0-9646-7054bb340f64", "INPUT");
        element1.setCategory("FIELD");
        element1.addProperty("label", "First Name");
        element1.addProperty("placeholder", "Enter your first name");

        ElementDTO element2 = new ElementDTO("dnd-element-211e95c0-c580-40b0-9646-7054bb340f65", "INPUT");
        element2.setCategory("FIELD");
        element2.addProperty("label", "Last Name");
        element2.addProperty("placeholder", "Enter your last name");

        ElementDTO element3 = new ElementDTO("dnd-element-212e95c0-c580-40b0-9646-7054bb340f66", "INPUT");
        element3.setCategory("FIELD");
        element3.addProperty("label", "Email");
        element3.addProperty("placeholder", "Enter your email");

        ElementDTO element4 = new ElementDTO("dnd-element-213e95c0-c580-40b0-9646-7054bb340f67", "INPUT");
        element4.setCategory("FIELD");
        element4.addProperty("label", "Password");
        element4.addProperty("placeholder", "Enter your password");

        ElementDTO element5 = new ElementDTO("pwdElementWithEx", "BUTTON");
        element5.setCategory("ACTION");
        element5.addProperty("text", "Next");
        ActionDTO action1 = new ActionDTO("EXECUTOR");
        ExecutorDTO executor1 = new ExecutorDTO("PasswordOnboarder");
        action1.setExecutor(executor1);
        action1.setNextId("step2");
        element5.setAction(action1);

        block1.addElementDto(element1);
        block1.addElementDto(element2);
        block1.addElementDto(element3);
        block1.addElementDto(element4);
        block1.addElementDto(element5);

        BlockDTO block2 = new BlockDTO();
        block2.setId("dnd-block-545e95c0-d280-65b0-9646-754bb340f66");

        ElementDTO element6 = new ElementDTO("googleElement", "BUTTON");
        element6.setCategory("ACTION");
        element6.addProperty("label", "Sign up with Google");
        element6.addProperty("icon", "google");
        element6.addProperty("position", "CENTER");
        ActionDTO action2 = new ActionDTO("NEXT");
        action2.setNextId("step3");
        element6.setAction(action2);

        block2.addElementDto(element6);

        step1.getBlocks().add(block1);
        step1.getBlocks().add(block2);

        // Step 2
        StepDTO step2 = new StepDTO();
        step2.setId("step2");
        step2.setType("VIEW");
        step2.setCoordinateX(100);
        step2.setCoordinateY(550);
        step2.setWidth(600);
        step2.setHeight(300);

        BlockDTO block3 = new BlockDTO();
        block3.setId("dnd-block-445e95c0-d280-65b0-9646-754bb340f65");

        ElementDTO element7 = new ElementDTO("otpElementWithEx", "INPUT");
        element7.setCategory("FIELD");
        element7.addProperty("label", "Email Verification Code");
        element7.addProperty("placeholder", "Enter the OTP sent to your email");

        ElementDTO element8 = new ElementDTO("dnd-element-216e95c0-c580-40b0-9646-7054bb340f70", "BUTTON");
        element8.setCategory("ACTION");
        element8.addProperty("label", "Submit");
        element8.addProperty("position", "RIGHT");
        ActionDTO action3 = new ActionDTO("EXECUTOR");
        ExecutorDTO executor2 = new ExecutorDTO("EmailOTPExecutor");
        action3.setExecutor(executor2);
        action3.setNextId("COMPLETE");
        element8.setAction(action3);

        block3.addElementDto(element7);
        block3.addElementDto(element8);

        step2.getBlocks().add(block3);

        // Step 3
        StepDTO step3 = new StepDTO();
        step3.setId("step3");
        step3.setType("SERVICE");
        step3.setCoordinateX(350);
        step3.setCoordinateY(300);
        step3.setWidth(150);
        step3.setHeight(150);

        ActionDTO action4 = new ActionDTO("EXECUTOR");
        ExecutorDTO executor3 = new ExecutorDTO("GoogleOIDCAuthenticator", "google");
        action4.setExecutor(executor3);
        action4.setNextId("COMPLETE");
        step3.setActionDTO(action4);

        // Add steps to registration flow
        List<StepDTO> steps = new ArrayList<>();
        steps.add(step1);
        steps.add(step2);
        steps.add(step3);
        registrationFlowDTO.setSteps(steps);

        return registrationFlowDTO;
    }

}