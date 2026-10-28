/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.flow.mgt.dao;

import org.apache.commons.dbcp.BasicDataSource;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.flow.mgt.Constants;
import org.wso2.carbon.identity.flow.mgt.Constants.FlowInputConfigs;
import org.wso2.carbon.identity.flow.mgt.model.DataDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowInputDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowInputDataType;
import org.wso2.carbon.identity.flow.mgt.model.FlowInputsDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowVariableInputDTO;
import org.wso2.carbon.identity.flow.mgt.model.StepDTO;
import org.wso2.carbon.identity.flow.mgt.utils.GraphBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.identity.flow.mgt.TestHelperMethods.closeH2Database;
import static org.wso2.carbon.identity.flow.mgt.TestHelperMethods.getFilePath;
import static org.wso2.carbon.identity.flow.mgt.TestHelperMethods.initiateH2Database;

/**
 * Tests that the headers, query parameters and variables of a flow are stored in their own table and read back in
 * the order they were defined.
 */
@WithCarbonHome
public class FlowDAOImplInputTest {

    private static final int TENANT_ID = -1234;
    private static final String FLOW_TYPE = "REGISTRATION";

    private BasicDataSource dataSource;
    private MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil;
    private MockedStatic<IdentityUtil> identityUtilMock;
    private final FlowDAOImpl flowDAO = new FlowDAOImpl();

    @BeforeClass
    public void setUp() throws Exception {

        dataSource = initiateH2Database(getFilePath("identity.sql"), "flow_input_dao_db");
        identityUtilMock = mockStatic(IdentityUtil.class, CALLS_REAL_METHODS);
        identityUtilMock.when(() -> IdentityUtil.getProperty(FlowInputConfigs.ENABLE)).thenReturn("true");

        identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
        identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSource);
    }

    @AfterClass
    public void tearDown() throws Exception {

        identityDatabaseUtil.close();
        identityUtilMock.close();
        closeH2Database(dataSource);
    }

    @Test
    public void testInputsAreReadBackInTheOrderDefined() throws Exception {

        FlowInputsDTO inputs = new FlowInputsDTO();
        inputs.getHeaders().add(new FlowInputDTO("x-ab-variant", FlowInputDataType.STRING_ARRAY));
        inputs.getQueryParams().add(new FlowInputDTO("age", FlowInputDataType.NUMBER_ARRAY));
        FlowVariableInputDTO abTestingKey =
                new FlowVariableInputDTO(Constants.SystemVariables.AB_TESTING_KEY, FlowInputDataType.INTEGER);
        abTestingKey.setValue(42L);
        FlowVariableInputDTO plan = new FlowVariableInputDTO("plan", FlowInputDataType.STRING);
        plan.setValue("basic");
        plan.setClientWritable(true);
        FlowVariableInputDTO tags = new FlowVariableInputDTO("tags", FlowInputDataType.STRING_ARRAY);
        inputs.getVariables().addAll(Arrays.asList(abTestingKey, plan, tags));

        flowDAO.updateFlow(FLOW_TYPE, new GraphBuilder().withSteps(Collections.singletonList(endStep()))
                .withFlowInputs(inputs).build(), TENANT_ID, "Registration");
        FlowInputsDTO stored = flowDAO.getFlow(FLOW_TYPE, TENANT_ID).getFlowInputs();

        assertEquals(stored.getHeaders().size(), 1);
        assertEquals(stored.getHeaders().get(0).getName(), "x-ab-variant");
        assertEquals(stored.getHeaders().get(0).getDataType(), FlowInputDataType.STRING_ARRAY);
        assertEquals(stored.getQueryParams().size(), 1);
        assertEquals(stored.getQueryParams().get(0).getName(), "age");
        assertEquals(stored.getQueryParams().get(0).getDataType(), FlowInputDataType.NUMBER_ARRAY);
        List<FlowVariableInputDTO> variables = stored.getVariables();
        assertEquals(variables.size(), 3);
        assertEquals(variables.get(0).getName(), Constants.SystemVariables.AB_TESTING_KEY);
        assertEquals(variables.get(0).getValue(), 42L);
        assertFalse(variables.get(0).isClientWritable());
        assertEquals(variables.get(1).getName(), "plan");
        assertEquals(variables.get(1).getValue(), "basic");
        assertTrue(variables.get(1).isClientWritable());
        assertEquals(variables.get(2).getName(), "tags");
        assertNull(variables.get(2).getValue());
        assertEquals(flowDAO.getGraphConfig(FLOW_TYPE, TENANT_ID).getFlowInputs().getVariables().size(), 3);
    }

    /**
     * Saving again replaces the inputs, rather than adding to them.
     */
    @Test(dependsOnMethods = "testInputsAreReadBackInTheOrderDefined")
    public void testSavingAgainReplacesTheInputs() throws Exception {

        FlowInputsDTO inputs = new FlowInputsDTO();
        inputs.getVariables().add(new FlowVariableInputDTO("attempts", FlowInputDataType.INTEGER));

        flowDAO.updateFlow(FLOW_TYPE, new GraphBuilder().withSteps(Collections.singletonList(endStep()))
                .withFlowInputs(inputs).build(), TENANT_ID, "Registration");
        FlowInputsDTO stored = flowDAO.getFlow(FLOW_TYPE, TENANT_ID).getFlowInputs();

        assertTrue(stored.getHeaders().isEmpty());
        assertTrue(stored.getQueryParams().isEmpty());
        // The A/B testing key the saved flow left out is given back when it is read.
        assertEquals(stored.getVariables().size(), 2);
        assertEquals(stored.getVariables().get(0).getName(), Constants.SystemVariables.AB_TESTING_KEY);
        assertEquals(stored.getVariables().get(1).getName(), "attempts");
    }

    /**
     * Several inputs of each kind, saved together, keep their order within each kind, and every data type and
     * initial value is read back as it was saved.
     */
    @Test(dependsOnMethods = "testSavingAgainReplacesTheInputs")
    public void testManyInputsOfEachKindKeepTheirOrderAndTypes() throws Exception {

        FlowInputsDTO inputs = new FlowInputsDTO();
        inputs.getHeaders().add(new FlowInputDTO("x-region", FlowInputDataType.STRING));
        inputs.getHeaders().add(new FlowInputDTO("x-tier", FlowInputDataType.INTEGER));
        inputs.getHeaders().add(new FlowInputDTO("x-beta", FlowInputDataType.BOOLEAN));
        inputs.getQueryParams().add(new FlowInputDTO("score", FlowInputDataType.DECIMAL));
        inputs.getQueryParams().add(new FlowInputDTO("ids", FlowInputDataType.NUMBER_ARRAY));
        FlowVariableInputDTO enabled = new FlowVariableInputDTO("enabled", FlowInputDataType.BOOLEAN);
        enabled.setValue(true);
        FlowVariableInputDTO ratio = new FlowVariableInputDTO("ratio", FlowInputDataType.DECIMAL);
        ratio.setValue(0.5d);
        FlowVariableInputDTO roles = new FlowVariableInputDTO("roles", FlowInputDataType.STRING_ARRAY);
        roles.setValue(Arrays.asList("admin", "user"));
        inputs.getVariables().addAll(Arrays.asList(enabled, ratio, roles));

        flowDAO.updateFlow(FLOW_TYPE, new GraphBuilder().withSteps(Collections.singletonList(endStep()))
                .withFlowInputs(inputs).build(), TENANT_ID, "Registration");
        FlowInputsDTO stored = flowDAO.getFlow(FLOW_TYPE, TENANT_ID).getFlowInputs();

        assertEquals(stored.getHeaders().size(), 3);
        assertEquals(stored.getHeaders().get(0).getName(), "x-region");
        assertEquals(stored.getHeaders().get(0).getDataType(), FlowInputDataType.STRING);
        assertEquals(stored.getHeaders().get(1).getName(), "x-tier");
        assertEquals(stored.getHeaders().get(1).getDataType(), FlowInputDataType.INTEGER);
        assertEquals(stored.getHeaders().get(2).getName(), "x-beta");
        assertEquals(stored.getHeaders().get(2).getDataType(), FlowInputDataType.BOOLEAN);
        assertEquals(stored.getQueryParams().size(), 2);
        assertEquals(stored.getQueryParams().get(0).getName(), "score");
        assertEquals(stored.getQueryParams().get(0).getDataType(), FlowInputDataType.DECIMAL);
        assertEquals(stored.getQueryParams().get(1).getName(), "ids");
        assertEquals(stored.getQueryParams().get(1).getDataType(), FlowInputDataType.NUMBER_ARRAY);
        List<FlowVariableInputDTO> variables = stored.getVariables();
        // The A/B testing key comes first, followed by the variables in the order they were saved.
        assertEquals(variables.size(), 4);
        assertEquals(variables.get(0).getName(), Constants.SystemVariables.AB_TESTING_KEY);
        assertEquals(variables.get(1).getName(), "enabled");
        assertEquals(variables.get(1).getValue(), true);
        assertEquals(variables.get(2).getName(), "ratio");
        assertEquals(variables.get(2).getValue(), 0.5d);
        assertEquals(variables.get(3).getName(), "roles");
        assertEquals(variables.get(3).getValue(), Arrays.asList("admin", "user"));
    }

    /**
     * Saving a flow with no inputs removes the inputs it had.
     */
    @Test(dependsOnMethods = "testManyInputsOfEachKindKeepTheirOrderAndTypes")
    public void testSavingWithNoInputsClearsThem() throws Exception {

        flowDAO.updateFlow(FLOW_TYPE, new GraphBuilder().withSteps(Collections.singletonList(endStep()))
                .withFlowInputs(new FlowInputsDTO()).build(), TENANT_ID, "Registration");
        FlowInputsDTO stored = flowDAO.getFlow(FLOW_TYPE, TENANT_ID).getFlowInputs();

        assertTrue(stored.getHeaders().isEmpty());
        assertTrue(stored.getQueryParams().isEmpty());
        assertEquals(stored.getVariables().size(), 1);
        assertEquals(stored.getVariables().get(0).getName(), Constants.SystemVariables.AB_TESTING_KEY);
    }

    private static StepDTO endStep() {

        return new StepDTO.Builder()
                .id(Constants.END_NODE_ID)
                .type(Constants.StepTypes.END)
                .data(new DataDTO.Builder().build())
                .build();
    }
}
