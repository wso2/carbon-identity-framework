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

        RegistrationFlowDTO registrationFlowDTO = createSampleRegistrationFlow1();
        try {
            RegistrationFlowConfig config = FlowConvertor.getSequence(registrationFlowDTO);
        } catch (RegistrationFrameworkException e) {
            throw new RuntimeException(e);
        }
    }

    public static RegistrationFlowDTO createSampleRegistrationFlow1() {
        RegistrationFlowDTO registrationFlowDTO = new RegistrationFlowDTO();

        // Step 1
        StepDTO step1 = new StepDTO.Builder()
                .id("dnd-step-3fa85f64-5717-4562-b3fc-2c963f66afa6")
                .type("VIEW")
                .coordinateX(100)
                .coordinateY(100)
                .width(600)
                .height(500)
                .build();

        BlockDTO block1 = new BlockDTO.Builder()
                .id("dnd-block-345e95c0-d280-65b0-9646-754bb340f64")
                .component(new ComponentDTO.Builder()
                                       .id("dnd-element-210e95c0-c580-40b0-9646-7054bb340f64")
                                       .type("INPUT")
                                       .category("FIELD")
                                       .property("label", "First Name")
                                       .property("placeholder", "Enter your first name")
                                       .build())
                .component(new ComponentDTO.Builder()
                                       .id("dnd-element-211e95c0-c580-40b0-9646-7054bb340f65")
                                       .type("INPUT")
                                       .category("FIELD")
                                       .property("label", "Last Name")
                                       .property("placeholder", "Enter your last name")
                                       .build())
                .component(new ComponentDTO.Builder()
                                       .id("dnd-element-212e95c0-c580-40b0-9646-7054bb340f66")
                                       .type("INPUT")
                                       .category("FIELD")
                                       .property("label", "Email")
                                       .property("placeholder", "Enter your email")
                                       .build())
                .component(new ComponentDTO.Builder()
                                       .id("dnd-element-213e95c0-c580-40b0-9646-7054bb340f67")
                                       .type("INPUT")
                                       .category("FIELD")
                                       .property("label", "Password")
                                       .property("placeholder", "Enter your password")
                                       .build())
                .component(new ComponentDTO.Builder()
                                       .id("dnd-element-214e95c0-c580-40b0-9646-7054bb340f68")
                                       .type("BUTTON")
                                       .category("ACTION")
                                       .property("text", "Next")
                                       .action(new ActionDTO.Builder()
                                                          .setType("EXECUTOR")
                                                          .setExecutor(new ExecutorDTO.Builder()
                                                                               .name("PasswordOnboarder")
                                                                               .build())
                                                          .setNextId("dnd-step-4fa85f64-5717-4562-b3fc-2c963f66afa7")
                                                          .build())
                                       .build())
                .build();

        BlockDTO block2 = new BlockDTO.Builder()
                .id("dnd-block-545e95c0-d280-65b0-9646-754bb340f66")
                .component(new ComponentDTO.Builder()
                                       .id("dnd-element-217e95c0-c580-40b0-9646-7054bb340f71")
                                       .type("BUTTON")
                                       .category("ACTION")
                                       .property("label", "Sign up with Google")
                                       .property("icon", "google")
                                       .property("position", "CENTER")
                                       .action(new ActionDTO.Builder()
                                                          .setType("NEXT")
                                                          .setNextId("dnd-step-5fa85f64-5717-4562-b3fc-2c963f66afa8")
                                                          .build())
                                       .build())
                .build();

        step1.getBlocks().add(block1);
        step1.getBlocks().add(block2);

        // Step 2
        StepDTO step2 = new StepDTO.Builder()
                .id("dnd-step-4fa85f64-5717-4562-b3fc-2c963f66afa7")
                .type("VIEW")
                .coordinateX(100)
                .coordinateY(550)
                .width(600)
                .height(300)
                .build();

        BlockDTO block3 = new BlockDTO.Builder()
                .id("dnd-block-445e95c0-d280-65b0-9646-754bb340f65")
                .component(new ComponentDTO.Builder()
                                       .id("dnd-element-215e95c0-c580-40b0-9646-7054bb340f69")
                                       .type("INPUT")
                                       .category("FIELD")
                                       .property("label", "Email Verification Code")
                                       .property("placeholder", "Enter the OTP sent to your email")
                                       .build())
                .component(new ComponentDTO.Builder()
                                       .id("dnd-element-216e95c0-c580-40b0-9646-7054bb340f70")
                                       .type("BUTTON")
                                       .category("ACTION")
                                       .property("label", "Submit")
                                       .property("position", "RIGHT")
                                       .action(new ActionDTO.Builder()
                                                          .setType("EXECUTOR")
                                                          .setExecutor(new ExecutorDTO.Builder()
                                                                               .name("EmailOTPExecutor")
                                                                               .build())
                                                          .setNextId("COMPLETE")
                                                          .build())
                                       .build())
                .build();

        step2.getBlocks().add(block3);

        // Step 3
        StepDTO step3 = new StepDTO.Builder()
                .id("dnd-step-5fa85f64-5717-4562-b3fc-2c963f66afa8")
                .type("SERVICE")
                .coordinateX(350)
                .coordinateY(300)
                .width(150)
                .height(150)
                .action(new ActionDTO.Builder()
                                      .setType("EXECUTOR")
                                      .setExecutor(new ExecutorDTO.Builder()
                                                           .name("GoogleOIDCAuthenticator")
                                                           .idpName("google")
                                                           .build())
                                      .setNextId("COMPLETE")
                                      .build())
                .build();

        // Add steps to registration flow
        List<StepDTO> steps = new ArrayList<>();
        steps.add(step1);
        steps.add(step2);
        steps.add(step3);
        registrationFlowDTO.setSteps(steps);

        return registrationFlowDTO;
    }

}