/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.flow.mgt.dao;

import org.apache.commons.dbcp.BasicDataSource;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.flow.mgt.Constants;
import org.wso2.carbon.identity.flow.mgt.model.ActionDTO;
import org.wso2.carbon.identity.flow.mgt.model.BranchDTO;
import org.wso2.carbon.identity.flow.mgt.model.DataDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowDTO;
import org.wso2.carbon.identity.flow.mgt.model.GraphConfig;
import org.wso2.carbon.identity.flow.mgt.model.StepDTO;
import org.wso2.carbon.identity.flow.mgt.utils.GraphBuilder;
import org.wso2.carbon.identity.rule.management.api.model.ANDCombinedRule;
import org.wso2.carbon.identity.rule.management.api.model.Expression;
import org.wso2.carbon.identity.rule.management.api.model.FieldReference;
import org.wso2.carbon.identity.rule.management.api.model.ORCombinedRule;
import org.wso2.carbon.identity.rule.management.api.model.Value;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mockStatic;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.wso2.carbon.identity.flow.mgt.TestHelperMethods.closeH2Database;
import static org.wso2.carbon.identity.flow.mgt.TestHelperMethods.getFilePath;
import static org.wso2.carbon.identity.flow.mgt.TestHelperMethods.initiateH2Database;

/**
 * Tests that the rules of a rule evaluation step are stored in their own table and read back onto the step.
 */
@WithCarbonHome
public class FlowDAOImplRuleTest {

    private static final int TENANT_ID = -1234;
    private static final String FLOW_TYPE = "REGISTRATION";
    private static final String STEP_ID = "step_route";
    private static final String COUNTRY_CLAIM = "http://wso2.org/claims/country";

    private BasicDataSource dataSource;
    private MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil;
    private final FlowDAOImpl flowDAO = new FlowDAOImpl();

    @BeforeClass
    public void setUp() throws Exception {

        dataSource = initiateH2Database(getFilePath("identity.sql"), "flow_rule_dao_db");
        identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
        identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSource);
    }

    @AfterClass
    public void tearDown() throws Exception {

        identityDatabaseUtil.close();
        closeH2Database(dataSource);
    }

    @Test
    public void testRulesAreReadBackInOrderWithTheirTargets() throws Exception {

        flowDAO.updateFlow(FLOW_TYPE, graphWithEndStep(), TENANT_ID, "Registration");

        List<BranchDTO> branches = flowDAO.getGraphConfig(FLOW_TYPE, TENANT_ID).getNodePageMappings()
                .get(STEP_ID).getData().getAction().getBranches();

        assertEquals(branches.size(), 2);
        assertEquals(branches.get(0).getId(), "rule_lk");
        assertEquals(branches.get(0).getName(), "Sri Lanka");
        assertEquals(branches.get(0).getNextId(), Constants.END_NODE_ID);
        assertNotNull(branches.get(0).getRule());
        assertEquals(branches.get(0).getRule().getRules().get(0).getExpressions().get(0).getFieldQualifier(),
                COUNTRY_CLAIM);
        assertEquals(branches.get(1).getId(), "rule_default");
        assertNull(branches.get(1).getRule(), "The default rule is stored without content.");
    }

    @Test(dependsOnMethods = "testRulesAreReadBackInOrderWithTheirTargets")
    public void testFlowReadForTheApiCarriesTheRules() throws Exception {

        FlowDTO flow = flowDAO.getFlow(FLOW_TYPE, TENANT_ID);

        StepDTO step = flow.getSteps().stream().filter(s -> STEP_ID.equals(s.getId())).findFirst().orElse(null);
        assertNotNull(step);
        assertEquals(step.getData().getAction().getBranches().size(), 2);
    }

    /**
     * The page content is what the builder renders, so the rules are not kept there as well.
     */
    @Test(dependsOnMethods = "testRulesAreReadBackInOrderWithTheirTargets")
    public void testRulesAreNotStoredInThePageContent() throws Exception {

        try (Connection connection = dataSource.getConnection();
             ResultSet resultSet = connection.createStatement().executeQuery(
                     "SELECT PAGE_CONTENT FROM IDN_FLOW_PAGE WHERE STEP_ID = '" + STEP_ID + "'")) {
            resultSet.next();
            String pageContent = new String(resultSet.getBytes(1), StandardCharsets.UTF_8);
            assertFalse(pageContent.contains("branches"), pageContent);
        }
        try (Connection connection = dataSource.getConnection();
             ResultSet resultSet = connection.createStatement().executeQuery(
                     "SELECT COUNT(*) FROM IDN_FLOW_NODE_BRANCH")) {
            resultSet.next();
            assertEquals(resultSet.getInt(1), 2);
        }
    }

    /**
     * Without an explicit end step an edge to END is not recorded, so a rule with no edge leads to END.
     */
    @Test(dependsOnMethods = {"testFlowReadForTheApiCarriesTheRules", "testRulesAreNotStoredInThePageContent"})
    public void testRuleWithoutAnEdgeLeadsToEnd() throws Exception {

        GraphConfig graph = new GraphBuilder().withSteps(new ArrayList<>(Arrays.asList(ruleStep()))).build();
        flowDAO.updateFlow(FLOW_TYPE, graph, TENANT_ID, "Registration");

        List<BranchDTO> branches = flowDAO.getGraphConfig(FLOW_TYPE, TENANT_ID).getNodePageMappings()
                .get(STEP_ID).getData().getAction().getBranches();

        assertEquals(branches.get(0).getNextId(), Constants.END_NODE_ID);
        assertEquals(branches.get(1).getNextId(), Constants.END_NODE_ID);
    }

    /**
     * A rule comparing two fields keeps the field it reads through a save and a load.
     */
    @Test(dependsOnMethods = "testRuleWithoutAnEdgeLeadsToEnd")
    public void testFieldReferenceSurvivesStorage() throws Exception {

        ORCombinedRule compared = new ORCombinedRule.Builder()
                .addRule(new ANDCombinedRule.Builder()
                        .addExpression(new Expression.Builder()
                                .field("user.collectedClaims").fieldQualifier(COUNTRY_CLAIM).operator("notEquals")
                                .value(new Value(new FieldReference("user.claims", COUNTRY_CLAIM))).build())
                        .build())
                .build();
        StepDTO step = ruleStep();
        step.getData().getAction().getBranches().get(0).setRule(compared);
        flowDAO.updateFlow(FLOW_TYPE, new GraphBuilder().withSteps(new ArrayList<>(Arrays.asList(step))).build(),
                TENANT_ID, "Registration");

        Value value = flowDAO.getGraphConfig(FLOW_TYPE, TENANT_ID).getNodePageMappings().get(STEP_ID)
                .getData().getAction().getBranches().get(0).getRule()
                .getRules().get(0).getExpressions().get(0).getValue();

        assertEquals(value.getType(), Value.Type.FIELD);
        assertEquals(value.getFieldReference().getName(), "user.claims");
        assertEquals(value.getFieldReference().getQualifier(), COUNTRY_CLAIM);
    }

    private GraphConfig graphWithEndStep() throws Exception {

        StepDTO endStep = new StepDTO.Builder()
                .id(Constants.END_NODE_ID)
                .type(Constants.StepTypes.END)
                .data(new DataDTO.Builder().build())
                .build();
        return new GraphBuilder().withSteps(Arrays.asList(ruleStep(), endStep)).build();
    }

    private StepDTO ruleStep() {

        List<BranchDTO> branches = new ArrayList<>();
        branches.add(new BranchDTO.Builder().id("rule_lk").name("Sri Lanka")
                .nextId(Constants.END_NODE_ID).rule(countryIsSriLanka()).build());
        branches.add(new BranchDTO.Builder().id("rule_default").name("Otherwise")
                .nextId(Constants.END_NODE_ID).build());

        ActionDTO action = new ActionDTO.Builder()
                .type(Constants.ActionTypes.RULE_EVALUATOR)
                .branches(branches)
                .build();
        return new StepDTO.Builder()
                .id(STEP_ID)
                .type(Constants.StepTypes.RULE_EVALUATION)
                .data(new DataDTO.Builder().action(action).build())
                .build();
    }

    private ORCombinedRule countryIsSriLanka() {

        return new ORCombinedRule.Builder()
                .addRule(new ANDCombinedRule.Builder()
                        .addExpression(new Expression.Builder()
                                .field("user.collectedClaims").fieldQualifier(COUNTRY_CLAIM).operator("equals")
                                .value(new Value(Value.Type.STRING, "Sri Lanka")).build())
                        .build())
                .build();
    }
}
