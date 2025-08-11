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

package org.wso2.carbon.identity.user.pre.update.profile.action.rule;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.context.IdentityContext;
import org.wso2.carbon.identity.core.context.model.Flow;
import org.wso2.carbon.identity.rule.evaluation.api.exception.RuleEvaluationDataProviderException;
import org.wso2.carbon.identity.rule.evaluation.api.model.Field;
import org.wso2.carbon.identity.rule.evaluation.api.model.FieldValue;
import org.wso2.carbon.identity.rule.evaluation.api.model.FlowContext;
import org.wso2.carbon.identity.rule.evaluation.api.model.FlowType;
import org.wso2.carbon.identity.rule.evaluation.api.model.RuleEvaluationContext;
import org.wso2.carbon.identity.rule.evaluation.api.model.ValueType;
import org.wso2.carbon.identity.user.action.api.model.UserActionContext;
import org.wso2.carbon.identity.user.action.api.model.UserActionRequestDTO;
import org.wso2.carbon.identity.user.pre.update.profile.action.internal.rule.PreUpdateProfileActionRuleEvaluationDataProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.doReturn;
import static org.testng.Assert.assertEquals;

/**
 * Test class for PreUpdateProfileActionRuleEvaluationDataProvider.
 */
@WithCarbonHome
public class PreUpdateProfileActionRuleEvaluationDataProviderTest {

    @Mock
    private RuleEvaluationContext ruleEvaluationContext;
    @Mock
    private FlowContext flowContext;
    @Mock
    private Flow flow;
    @Mock
    private UserActionContext userActionContext;
    @Mock
    private UserActionRequestDTO userActionRequestDTO;

    private PreUpdateProfileActionRuleEvaluationDataProvider dataProvider;

    @BeforeMethod
    public void setUp() {

        MockitoAnnotations.openMocks(this);
        dataProvider = new PreUpdateProfileActionRuleEvaluationDataProvider();
    }

    @AfterMethod
    public void tearDown() {

        IdentityContext.destroyCurrentContext();
    }

    @Test
    public void testGetSupportedFlowType() {

        assertEquals(dataProvider.getSupportedFlowType(), FlowType.PRE_UPDATE_PROFILE);
    }

    @DataProvider(name = "flowData")
    public Object[][] flowData() {

        return new Object[][]{
                {Flow.Name.PROFILE_UPDATE, Flow.InitiatingPersona.ADMIN, "adminInitiatedProfileUpdate"},
                {Flow.Name.PROFILE_UPDATE, Flow.InitiatingPersona.USER, "userInitiatedProfileUpdate"},
                {Flow.Name.PROFILE_UPDATE, Flow.InitiatingPersona.APPLICATION, "applicationInitiatedProfileUpdate"}
        };
    }

    @DataProvider(name = "claimData")
    public Object[][] claimData() {

        return new Object[][]{
                {new ArrayList<>(Arrays.asList("http://wso2.org/claims/country", "http://wso2.org/claims/givenname"))}
        };
    }

    @DataProvider(name = "unsupportedFlowData")
    public Object[][] unsupportedFlowData() {

        return Arrays.stream(Flow.Name.values())
                .filter(name -> name != Flow.Name.PROFILE_UPDATE) //Remove the supported flow
                .map(name -> new Object[]{name})
                .toArray(Object[][]::new);
    }

    @Test(dataProvider = "flowData")
    public void testGetEvaluationDataForFlow(Flow.Name flowName, Flow.InitiatingPersona initiatingPersona,
                                             String fieldValue) throws RuleEvaluationDataProviderException {

        Field flowField = new Field("flow", ValueType.STRING);
        doReturn(Collections.singletonList(flowField)).when(ruleEvaluationContext).getFields();
        doReturn(flowName).when(flow).getName();
        doReturn(initiatingPersona).when(flow).getInitiatingPersona();
        IdentityContext.getThreadLocalIdentityContext().enterFlow(flow);
        List<FieldValue> fieldValues = dataProvider.getEvaluationData(ruleEvaluationContext, flowContext, "test.com");
        assertEquals(fieldValues.size(), 1);
        assertEquals(fieldValues.get(0).getValue(), fieldValue);
    }

    @Test(dataProvider = "claimData")
    public void testGetEvaluationDataForClaim(ArrayList<String> claims)
            throws RuleEvaluationDataProviderException {

        Field claimField = new Field("claim", ValueType.REFERENCE);
        doReturn(Collections.singletonList(claimField)).when(ruleEvaluationContext).getFields();
        Map<String, Object> contextMap = new HashMap<>();
        contextMap.put(UserActionContext.USER_ACTION_CONTEXT_REFERENCE_KEY, userActionContext);
        doReturn(contextMap).when(flowContext).getContextData();
        doReturn(userActionRequestDTO).when(userActionContext).getUserActionRequestDTO();
        Map<String, Object> claimMap = new HashMap<>();
        claimMap.put(claims.get(0), "testValue1");
        claimMap.put(claims.get(1), "testValue2");
        doReturn(claimMap).when(userActionRequestDTO).getClaims();
        IdentityContext.getThreadLocalIdentityContext().enterFlow(flow);

        List<FieldValue> fieldValues = dataProvider.getEvaluationData(ruleEvaluationContext, flowContext, "test.com");
        assertEquals(fieldValues.size(), 1);
        assertEquals(fieldValues.get(0).getValue(), claims);
    }

    @Test(expectedExceptions = RuleEvaluationDataProviderException.class)
    public void testGetEvaluationDataWithUnsupportedField() throws RuleEvaluationDataProviderException {

        Field field = new Field("unsupported_field", ValueType.STRING);
        doReturn(Collections.singletonList(field)).when(ruleEvaluationContext).getFields();
        dataProvider.getEvaluationData(ruleEvaluationContext, flowContext, "test.com");
    }

    @Test(dataProvider = "unsupportedFlowData", expectedExceptions = RuleEvaluationDataProviderException.class)
    public void testGetEvaluationDataWithUnsupportedFlows(Flow.Name flowName)
            throws RuleEvaluationDataProviderException {

        Field flowField = new Field("flow", ValueType.STRING);
        doReturn(Collections.singletonList(flowField)).when(ruleEvaluationContext).getFields();
        doReturn(flowName).when(flow).getName();
        doReturn(Flow.InitiatingPersona.APPLICATION).when(flow).getInitiatingPersona();
        IdentityContext.getThreadLocalIdentityContext().enterFlow(flow);
        dataProvider.getEvaluationData(ruleEvaluationContext, flowContext, "test.com");
    }
}
