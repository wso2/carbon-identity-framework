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
import org.wso2.carbon.identity.flow.mgt.Constants.FlowVariableConfigs;
import org.wso2.carbon.identity.flow.mgt.model.DataDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowRuntimeValueDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowVariableDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowVariableDataType;
import org.wso2.carbon.identity.flow.mgt.model.FlowVariablesDTO;
import org.wso2.carbon.identity.flow.mgt.model.StepDTO;
import org.wso2.carbon.identity.flow.mgt.utils.GraphBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.identity.flow.mgt.TestHelperMethods.closeH2Database;
import static org.wso2.carbon.identity.flow.mgt.TestHelperMethods.getFilePath;
import static org.wso2.carbon.identity.flow.mgt.TestHelperMethods.initiateH2Database;

/**
 * Tests that the headers and runtime values of a flow are stored in their own table and read back in
 * the order they were defined.
 */
@WithCarbonHome
public class FlowDAOImplVariableTest {

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
        identityUtilMock.when(() -> IdentityUtil.getProperty(FlowVariableConfigs.ENABLE)).thenReturn("true");

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
    public void testVariablesAreReadBackInTheOrderDefined() throws Exception {

        FlowVariablesDTO inputs = new FlowVariablesDTO();
        inputs.getHeaders().add(new FlowVariableDTO("x-ab-variant", FlowVariableDataType.STRING_ARRAY));
        FlowRuntimeValueDTO abTestingKey =
                new FlowRuntimeValueDTO(Constants.SystemRuntimeValues.AB_TESTING_KEY, FlowVariableDataType.INTEGER);
        abTestingKey.setValue(42L);
        FlowRuntimeValueDTO plan = new FlowRuntimeValueDTO("plan", FlowVariableDataType.STRING);
        plan.setValue("basic");
        plan.setClientWritable(true);
        FlowRuntimeValueDTO tags = new FlowRuntimeValueDTO("tags", FlowVariableDataType.STRING_ARRAY);
        inputs.getRuntimeValues().addAll(Arrays.asList(abTestingKey, plan, tags));

        flowDAO.updateFlow(FLOW_TYPE, new GraphBuilder().withSteps(Collections.singletonList(endStep()))
                .withFlowVariables(inputs).build(), TENANT_ID, "Registration");
        FlowVariablesDTO stored = flowDAO.getFlow(FLOW_TYPE, TENANT_ID).getFlowVariables();

        assertEquals(stored.getHeaders().size(), 1);
        assertEquals(stored.getHeaders().get(0).getName(), "x-ab-variant");
        assertEquals(stored.getHeaders().get(0).getDataType(), FlowVariableDataType.STRING_ARRAY);
        List<FlowRuntimeValueDTO> variables = stored.getRuntimeValues();
        assertEquals(variables.size(), 3);
        assertEquals(variables.get(0).getName(), Constants.SystemRuntimeValues.AB_TESTING_KEY);
        assertEquals(variables.get(0).getValue(), 42L);
        assertTrue(variables.get(0).isClientWritable());
        assertEquals(variables.get(1).getName(), "plan");
        assertEquals(variables.get(1).getValue(), "basic");
        assertTrue(variables.get(1).isClientWritable());
        assertEquals(variables.get(2).getName(), "tags");
        assertNull(variables.get(2).getValue());
        assertEquals(flowDAO.getGraphConfig(FLOW_TYPE, TENANT_ID).getFlowVariables().getRuntimeValues().size(), 3);
    }

    /**
     * Saving again replaces the variables, rather than adding to them.
     */
    @Test(dependsOnMethods = "testVariablesAreReadBackInTheOrderDefined")
    public void testSavingAgainReplacesTheVariables() throws Exception {

        FlowVariablesDTO inputs = new FlowVariablesDTO();
        inputs.getRuntimeValues().add(new FlowRuntimeValueDTO("attempts", FlowVariableDataType.INTEGER));

        flowDAO.updateFlow(FLOW_TYPE, new GraphBuilder().withSteps(Collections.singletonList(endStep()))
                .withFlowVariables(inputs).build(), TENANT_ID, "Registration");
        FlowVariablesDTO stored = flowDAO.getFlow(FLOW_TYPE, TENANT_ID).getFlowVariables();

        assertTrue(stored.getHeaders().isEmpty());
        // The A/B testing key the saved flow left out is given back when it is read.
        assertEquals(stored.getRuntimeValues().size(), 2);
        assertEquals(stored.getRuntimeValues().get(0).getName(), Constants.SystemRuntimeValues.AB_TESTING_KEY);
        assertEquals(stored.getRuntimeValues().get(1).getName(), "attempts");
    }

    /**
     * Several variables of each kind, saved together, keep their order within each kind, and every data type and
     * initial value is read back as it was saved.
     */
    @Test(dependsOnMethods = "testSavingAgainReplacesTheVariables")
    public void testManyVariablesOfEachKindKeepTheirOrderAndTypes() throws Exception {

        FlowVariablesDTO inputs = new FlowVariablesDTO();
        inputs.getHeaders().add(new FlowVariableDTO("x-region", FlowVariableDataType.STRING));
        inputs.getHeaders().add(new FlowVariableDTO("x-tier", FlowVariableDataType.INTEGER));
        inputs.getHeaders().add(new FlowVariableDTO("x-beta", FlowVariableDataType.BOOLEAN));
        FlowRuntimeValueDTO enabled = new FlowRuntimeValueDTO("enabled", FlowVariableDataType.BOOLEAN);
        enabled.setValue(true);
        FlowRuntimeValueDTO ratio = new FlowRuntimeValueDTO("ratio", FlowVariableDataType.DECIMAL);
        ratio.setValue(0.5d);
        FlowRuntimeValueDTO roles = new FlowRuntimeValueDTO("roles", FlowVariableDataType.STRING_ARRAY);
        roles.setValue(Arrays.asList("admin", "user"));
        inputs.getRuntimeValues().addAll(Arrays.asList(enabled, ratio, roles));

        flowDAO.updateFlow(FLOW_TYPE, new GraphBuilder().withSteps(Collections.singletonList(endStep()))
                .withFlowVariables(inputs).build(), TENANT_ID, "Registration");
        FlowVariablesDTO stored = flowDAO.getFlow(FLOW_TYPE, TENANT_ID).getFlowVariables();

        assertEquals(stored.getHeaders().size(), 3);
        assertEquals(stored.getHeaders().get(0).getName(), "x-region");
        assertEquals(stored.getHeaders().get(0).getDataType(), FlowVariableDataType.STRING);
        assertEquals(stored.getHeaders().get(1).getName(), "x-tier");
        assertEquals(stored.getHeaders().get(1).getDataType(), FlowVariableDataType.INTEGER);
        assertEquals(stored.getHeaders().get(2).getName(), "x-beta");
        assertEquals(stored.getHeaders().get(2).getDataType(), FlowVariableDataType.BOOLEAN);
        List<FlowRuntimeValueDTO> variables = stored.getRuntimeValues();
        // The A/B testing key comes first, followed by the runtime values in the order they were saved.
        assertEquals(variables.size(), 4);
        assertEquals(variables.get(0).getName(), Constants.SystemRuntimeValues.AB_TESTING_KEY);
        assertEquals(variables.get(1).getName(), "enabled");
        assertEquals(variables.get(1).getValue(), true);
        assertEquals(variables.get(2).getName(), "ratio");
        assertEquals(variables.get(2).getValue(), 0.5d);
        assertEquals(variables.get(3).getName(), "roles");
        assertEquals(variables.get(3).getValue(), Arrays.asList("admin", "user"));
    }

    /**
     * Saving a flow with no variables removes the variables it had.
     */
    @Test(dependsOnMethods = "testManyVariablesOfEachKindKeepTheirOrderAndTypes")
    public void testSavingWithNoVariablesClearsThem() throws Exception {

        flowDAO.updateFlow(FLOW_TYPE, new GraphBuilder().withSteps(Collections.singletonList(endStep()))
                .withFlowVariables(new FlowVariablesDTO()).build(), TENANT_ID, "Registration");
        FlowVariablesDTO stored = flowDAO.getFlow(FLOW_TYPE, TENANT_ID).getFlowVariables();

        assertTrue(stored.getHeaders().isEmpty());
        assertEquals(stored.getRuntimeValues().size(), 1);
        assertEquals(stored.getRuntimeValues().get(0).getName(), Constants.SystemRuntimeValues.AB_TESTING_KEY);
    }

    private static StepDTO endStep() {

        return new StepDTO.Builder()
                .id(Constants.END_NODE_ID)
                .type(Constants.StepTypes.END)
                .data(new DataDTO.Builder().build())
                .build();
    }
}
