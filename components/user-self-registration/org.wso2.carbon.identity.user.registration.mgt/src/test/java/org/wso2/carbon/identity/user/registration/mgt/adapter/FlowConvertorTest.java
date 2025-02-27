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

public class FlowConvertorTest {

//    public static RegistrationFlowDTO createSampleRegistrationFlow2() {
//
//        RegistrationFlowDTO registrationFlowDTO = new RegistrationFlowDTO();
//
//        // Step 1
//        StepDTO step1 = new StepDTO.Builder()
//                .id("step_1")
//                .type("VIEW")
//                .coordinateX(0)
//                .coordinateY(0)
//                .width(120)
//                .height(240)
//                .data(createStep2_1Data())
//                .build();
//
//        // Step 3
//        StepDTO step4 = new StepDTO.Builder()
//                .id("step_4")
//                .type("REDIRECTION")
//                .coordinateX(0)
//                .coordinateY(0)
//                .width(120)
//                .height(240)
//                .data(createStepGoogleRedirection())
//                .build();
//
//        // Add steps to registration flow
//        List<StepDTO> steps = new ArrayList<>();
//        steps.add(step1);
//        steps.add(step4);
//        registrationFlowDTO.setSteps(steps);
//
//        return registrationFlowDTO;
//    }
//
//    public static RegistrationFlowDTO createSampleRegistrationFlow1() {
//
//        RegistrationFlowDTO registrationFlowDTO = new RegistrationFlowDTO();
//
//        // Step 1
//        StepDTO step1 = new StepDTO.Builder()
//                .id("step_1")
//                .type("VIEW")
//                .coordinateX(0)
//                .coordinateY(0)
//                .width(120)
//                .height(240)
//                .data(createStep1_1Data())
//                .build();
//
//        // Step 2
//        StepDTO step2 = new StepDTO.Builder()
//                .id("step_2")
//                .type("VIEW")
//                .coordinateX(0)
//                .coordinateY(0)
//                .width(120)
//                .height(240)
//                .data(createPwdEnterStep())
//                .build();
//
//        // Step 2
//        StepDTO step3 = new StepDTO.Builder()
//                .id("step_3")
//                .type("VIEW")
//                .coordinateX(0)
//                .coordinateY(0)
//                .width(120)
//                .height(240)
//                .data(createEmailOTPEnterStep())
//                .build();
//
//        // Step 3
//        StepDTO step4 = new StepDTO.Builder()
//                .id("step_4")
//                .type("REDIRECTION")
//                .coordinateX(0)
//                .coordinateY(0)
//                .width(120)
//                .height(240)
//                .data(createStepGoogleRedirection())
//                .build();
//
//        // Add steps to registration flow
//        List<StepDTO> steps = new ArrayList<>();
//        steps.add(step1);
//        steps.add(step2);
//        steps.add(step3);
//        steps.add(step4);
//        registrationFlowDTO.setSteps(steps);
//
//        return registrationFlowDTO;
//    }
//
//    private static Map<String, Object> createStep1_1Data() {
//
//        Map<String, Object> data = new HashMap<>();
//        Map<String, Object> components = new HashMap<>();
//
//        ComponentDTO blockComponent = new ComponentDTO.Builder()
//                .id("component_232d")
//                .category("BLOCK")
//                .type("FORM")
//                .configs(createForm1_1())
//                .build();
//
//        ComponentDTO buttonComponent = new ComponentDTO.Builder()
//                .id("element_gd43")
//                .category("BUTTON")
//                .type("BUTTON")
//                .config("text", "Continue with Google")
//                .action(new ActionDTO.Builder()
//                                .setType("NEXT")
//                                .setNextId("step_4")
//                                .build())
//                .build();
//
//        components.put(blockComponent.getId(), blockComponent);
//        components.put(buttonComponent.getId(), buttonComponent);
//        data.put("components", components);
//
//        return data;
//    }
//
//    private static Map<String, Object> createStep2_1Data() {
//
//        Map<String, Object> data = new HashMap<>();
//        List<ComponentDTO> components = new ArrayList<>();
//
//        ComponentDTO blockComponent = new ComponentDTO.Builder()
//                .id("component_232d")
//                .category("BLOCK")
//                .type("FORM")
//                .configs(createForm2_1())
//                .build();
//
//        ComponentDTO buttonComponent = new ComponentDTO.Builder()
//                .id("element_gd43")
//                .category("BUTTON")
//                .type("BUTTON")
//                .config("text", "Continue with Google")
//                .action(new ActionDTO.Builder()
//                                .setType("NEXT")
//                                .setNextId("step_4")
//                                .build())
//                .build();
//
////        components.put(blockComponent.getId(), blockComponent);
////        components.put(buttonComponent.getId(), buttonComponent);
//        components.add(blockComponent);
//        components.add(buttonComponent);
//        data.put("components", components);
//
//        return data;
//    }
//
//    private static Map<String, Object> createForm1_1() {
//
////        Map<String, Object> components = new HashMap<>();
//        List<ComponentDTO> components = new ArrayList<>();
//
//        ComponentDTO usernameField = new ComponentDTO.Builder()
//                .id("element_un")
//                .category("FIELD")
//                .type("INPUT")
//                .config("label", "Username")
//                .config("placeholder", "Enter your username")
//                .config("required", true)
//                .config("type", "text")
//                .build();
//
//        ComponentDTO emailField = new ComponentDTO.Builder()
//                .id("element_email")
//                .category("FIELD")
//                .type("INPUT")
//                .config("label", "EmailAddress")
//                .config("placeholder", "Enter your emailaddress")
//                .config("required", true)
//                .config("type", "text")
//                .build();
//
//        ComponentDTO continueWithPasswordBtn = new ComponentDTO.Builder()
//                .id("element_cntPwd")
//                .category("BUTTON")
//                .type("BUTTON")
//                .config("text", "Continue")
//                .action(new ActionDTO.Builder()
//                                .setType("NEXT")
//                                .setNextId("step_2")
//                                .build())
//                .build();
//
//        ComponentDTO continueWithEmailOtpBtn = new ComponentDTO.Builder()
//                .id("element_ctdEmail")
//                .category("BUTTON")
//                .type("BUTTON")
//                .config("text", "Continue")
//                .action(new ActionDTO.Builder()
//                                .setType("NEXT")
//                                .setNextId("step_3")
//                                .build())
//                .build();
//
////        components.put(usernameField.getId(), usernameField);
////        components.put(emailField.getId(), emailField);
////        components.put(continueWithPasswordBtn.getId(), continueWithPasswordBtn);
////        components.put(continueWithEmailOtpBtn.getId(), continueWithEmailOtpBtn);
//
//        components.add(usernameField);
//        components.add(emailField);
//        components.add(continueWithPasswordBtn);
//        components.add(continueWithEmailOtpBtn);
//        Map<String, Object> subComponents = new HashMap<>();
//        subComponents.put(Constants.Fields.COMPONENTS, components);
//        return subComponents;
//    }
//
//    private static Map<String, Object> createForm2_1() {
//
//        Map<String, Object> components = new HashMap<>();
////        List<ComponentDTO> components = new ArrayList<>();
//        ComponentDTO usernameField = new ComponentDTO.Builder()
//                .id("element_un")
//                .category("FIELD")
//                .type("INPUT")
//                .config("label", "Username")
//                .config("placeholder", "Enter your username")
//                .config("required", true)
//                .config("type", "text")
//                .build();
//
//        ComponentDTO pwdField = new ComponentDTO.Builder()
//                .id("element_pwd")
//                .category("FIELD")
//                .type("INPUT")
//                .config("label", "Password")
//                .config("placeholder", "Enter your password")
//                .config("required", true)
//                .config("type", "text")
//                .build();
//
//        ComponentDTO submitPwdBtn = new ComponentDTO.Builder()
//                .id("element_56jd")
//                .category("BUTTON")
//                .type("BUTTON")
//                .config("text", "Continue")
//                .action(new ActionDTO.Builder()
//                                .setType("EXECUTOR")
//                                .setExecutor(new ExecutorDTO.Builder().name("PasswordOnboardExecutor").build())
//                                .setNextId("COMPLETE")
//                                .build())
//                .build();
//
//        components.put(usernameField.getId(), usernameField);
//        components.put(pwdField.getId(), pwdField);
//        components.put(submitPwdBtn.getId(), submitPwdBtn);
////
////        components.add(usernameField);
////        components.add(pwdField);
////        components.add(submitPwdBtn);
//
//        Map<String, Object> subComponents = new HashMap<>();
//        subComponents.put(Constants.Fields.COMPONENTS, components);
//        return subComponents;
//    }
//
//    private static Map<String, Object> createPwdEnterStep() {
//
//        Map<String, Object> data = new HashMap<>();
//        Map<String, ComponentDTO> components = new HashMap<>();
////        List<ComponentDTO> components = new ArrayList<>();
////
//        ComponentDTO passwordField = new ComponentDTO.Builder()
//                .id("element_23dx")
//                .category("FIELD")
//                .type("INPUT")
//                .config("label", "Password")
//                .config("placeholder", "Enter your password")
//                .config("required", true)
//                .config("type", "password")
//                .build();
//
//        ComponentDTO submitPwdBtn = new ComponentDTO.Builder()
//                .id("element_56jd")
//                .category("BUTTON")
//                .type("BUTTON")
//                .config("text", "Continue")
//                .action(new ActionDTO.Builder()
//                                .setType("EXECUTOR")
//                                .setExecutor(new ExecutorDTO.Builder().name("PasswordOnboardExecutor").build())
//                                .setNextId("COMPLETE")
//                                .build())
//                .build();
//
//        components.put(passwordField.getId(), passwordField);
//        components.put(submitPwdBtn.getId(), submitPwdBtn);
////        components.add(passwordField);
////        components.add(submitPwdBtn);
//        data.put("components", components);
//
//        return data;
//    }
//
//    private static Map<String, Object> createEmailOTPEnterStep() {
//
//        Map<String, Object> data = new HashMap<>();
//        Map<String, ComponentDTO> components = new HashMap<>();
//
////        List<ComponentDTO> components = new ArrayList<>();
//        ComponentDTO emailOtpField = new ComponentDTO.Builder()
//                .id("element_emailOTP")
//                .category("FIELD")
//                .type("INPUT")
//                .config("label", "emailOTP")
//                .config("placeholder", "Enter your otp")
//                .config("required", true)
//                .config("type", "otp")
//                .build();
//
//        ComponentDTO submitOtpBtn = new ComponentDTO.Builder()
//                .id("element_otpBtn")
//                .category("BUTTON")
//                .type("BUTTON")
//                .config("text", "Continue")
//                .action(new ActionDTO.Builder()
//                                .setType("EXECUTOR")
//                                .setExecutor(new ExecutorDTO.Builder().name("EmailOTP").build())
//                                .setNextId("COMPLETE")
//                                .build())
//                .build();
//
//        components.put(emailOtpField.getId(), emailOtpField);
//        components.put(submitOtpBtn.getId(), submitOtpBtn);
////        components.add(emailOtpField);
////        components.add(submitOtpBtn);
//        data.put("components", components);
//
//        return data;
//    }
//
//    private static Map<String, Object> createStepGoogleRedirection() {
//
//        Map<String, Object> data = new HashMap<>();
//
//        ActionDTO action = new ActionDTO.Builder()
//                .setType("EXECUTOR")
//                .setExecutor(new ExecutorDTO.Builder().name("GoogleOIDCAuthenticator").build())
//                .setNextId("COMPLETE")
//                .build();
//
//        data.put("action", action);
//
//        return data;
//    }
//
//    @Test()
//    void testScenario1() throws IOException {
//
//        RegistrationFlowDTO registrationFlowDTO = createSampleRegistrationFlow1();
//        try {
//            RegistrationFlowConfig config = FlowConvertor.convert(registrationFlowDTO);
//        } catch (RegistrationFrameworkException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Test()
//    void testScenario2() throws IOException {
//
//        RegistrationFlowDTO registrationFlowDTO = createSampleRegistrationFlow2();
//        try {
//            RegistrationFlowConfig config = FlowConvertor.convert(registrationFlowDTO);
//        } catch (RegistrationFrameworkException e) {
//            throw new RuntimeException(e);
//        }
//    }
}