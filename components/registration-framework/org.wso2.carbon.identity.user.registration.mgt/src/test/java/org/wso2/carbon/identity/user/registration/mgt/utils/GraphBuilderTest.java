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

package org.wso2.carbon.identity.user.registration.mgt.utils;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.user.registration.mgt.Constants;
import org.wso2.carbon.identity.user.registration.mgt.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.registration.mgt.model.ActionDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.ComponentDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.DataDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.ExecutorDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.NodeConfig;
import org.wso2.carbon.identity.user.registration.mgt.model.RegistrationFlowDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.RegistrationGraphConfig;
import org.wso2.carbon.identity.user.registration.mgt.model.StepDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Test class for GraphBuilder.
 */
public class GraphBuilderTest {

    public static final String STEP_1 = "step_qx3a";
    public static final String STEP_2 = "step_we12";
    public static final String STEP_3 = "step_sd21";
    public static final String STEP_4 = "step_on12";
    public static final String GOOGLE_IDP_NAME = "Google1";
    public static final String OIDC_AUTHENTICATOR = "OIDCAuthenticator";
    public static final String OIDC_IDP = "OIDC_IDP";
    public static final String GOOGLE_OIDC_AUTHENTICATOR = "GoogleOIDCAuthenticator";
    public static Map<String, String> stepIdToNextNodeIdMap = new HashMap<>();

    @BeforeClass
    public void setUpClass() {
    }

    @AfterMethod
    public void tearDown() {
    }

    @Test
    public void testConvert() throws RegistrationFrameworkException {

        // Create a sample RegistrationFlowDTO.
        RegistrationFlowDTO flowDTO = new RegistrationFlowDTO();
        flowDTO.setSteps(new ArrayList<>());

        Map<String, Object> configs = new HashMap<>();
        configs.put("key1", "value1");
        configs.put("key2", "value2");

        ComponentDTO componentDTO1 = new ComponentDTO.Builder()
                .id("component_2sa1")
                .type("BUTTON")
                .configs(configs)
                .category("SUBMIT")
                .action(new ActionDTO.Builder()
                        .type(Constants.ActionTypes.NEXT)
                        .nextId(STEP_2)
                        .build())
                .build();

        // Add a sample StepDTO.
        StepDTO stepDTO1 = new StepDTO.Builder()
                .id(STEP_1)
                .type(Constants.StepTypes.VIEW)
                .coordinateX(0)
                .coordinateY(0)
                .height(100.15)
                .width(200.25)
                .data(new DataDTO.Builder()
                        .components(Collections.singletonList(componentDTO1))
                        .build())
                .build();

        ComponentDTO componentDTO2 = new ComponentDTO.Builder()
                .id("component_2sa1")
                .type("BUTTON")
                .configs(configs)
                .action(new ActionDTO.Builder()
                        .type(Constants.ActionTypes.EXECUTOR)
                        .executor(new ExecutorDTO.Builder()
                                .name(OIDC_AUTHENTICATOR)
                                .idpName(OIDC_IDP)
                                .build())
                        .nextId(STEP_3)
                        .build())
                .build();

        StepDTO stepDTO2 = new StepDTO.Builder()
                .id(STEP_2)
                .type(Constants.StepTypes.VIEW)
                .coordinateX(0)
                .coordinateY(0)
                .height(100.15)
                .width(200.25)
                .data(new DataDTO.Builder()
                        .components(Collections.singletonList(componentDTO2))
                        .build())
                .build();

        StepDTO stepDTO3 = new StepDTO.Builder()
                .id(STEP_3)
                .type(Constants.StepTypes.REDIRECTION)
                .coordinateX(0)
                .coordinateY(0)
                .height(100.15)
                .width(200.25)
                .data(new DataDTO.Builder()
                        .action(new ActionDTO.Builder()
                                .type(Constants.ActionTypes.EXECUTOR)
                                .executor(new ExecutorDTO.Builder()
                                        .name(GOOGLE_OIDC_AUTHENTICATOR)
                                        .idpName(GOOGLE_IDP_NAME)
                                        .build())
                                .nextId(STEP_4)
                                .build())
                        .build())
                .build();

        StepDTO stepDTO4 = new StepDTO.Builder()
                .id(STEP_4)
                .type(Constants.StepTypes.USER_ONBOARD)
                .coordinateX(0)
                .coordinateY(0)
                .height(100.15)
                .width(200.25)
                .build();
        flowDTO.setSteps(Arrays.asList(stepDTO1, stepDTO2, stepDTO3, stepDTO4));

        // Convert the flowDTO to a RegistrationGraphConfig.
        RegistrationGraphConfig graphConfig = new GraphBuilder().withSteps(flowDTO.getSteps()).build();
        // Assert the converted graphConfig.
        assertNotNull(graphConfig);
        assertNotNull(graphConfig.getId());
        assertEquals(graphConfig.getNodeConfigs().size(), 4);
        assertEquals(graphConfig.getNodePageMappings().size(), 4);
        assertEquals(graphConfig.getFirstNodeId(), STEP_1);

        graphConfig.getNodePageMappings().forEach((nodeId, stepDTO) -> {
            stepIdToNextNodeIdMap.put(stepDTO.getId(), graphConfig.getNodeConfigs().get(nodeId).getNextNodeId());
        });

        graphConfig.getNodePageMappings().forEach((nodeId, stepDTO) -> {
            assertNotNull(nodeId);
            assertNotNull(stepDTO);
            NodeConfig nodeConfig = graphConfig.getNodeConfigs().get(nodeId);
            assertNotNull(nodeConfig);

            switch (stepDTO.getId()) {
                case STEP_1:
                    assertEquals(nodeConfig.getType(), Constants.NodeTypes.PROMPT_ONLY);
                    assertTrue(nodeConfig.isFirstNode());
                    assertEquals(nodeConfig.getNextNodeId(), stepIdToNextNodeIdMap.get(STEP_1));
                    assertNull(nodeConfig.getExecutorConfig());
                    break;
                case STEP_2:
                    assertEquals(nodeConfig.getType(), Constants.NodeTypes.TASK_EXECUTION);
                    assertEquals(nodeConfig.getExecutorConfig().getName(), OIDC_AUTHENTICATOR);
                    assertEquals(nodeConfig.getExecutorConfig().getIdpName(), OIDC_IDP);
                    assertEquals(nodeConfig.getNextNodeId(), stepIdToNextNodeIdMap.get(STEP_2));
                    break;
                case STEP_3:
                    assertEquals(nodeConfig.getType(), Constants.NodeTypes.TASK_EXECUTION);
                    assertEquals(nodeConfig.getExecutorConfig().getName(), GOOGLE_OIDC_AUTHENTICATOR);
                    assertEquals(nodeConfig.getExecutorConfig().getIdpName(), GOOGLE_IDP_NAME);
                    assertEquals(nodeConfig.getNextNodeId(), stepIdToNextNodeIdMap.get(STEP_3));
                    break;
                default:
                    break;
            }
        });
    }
}
