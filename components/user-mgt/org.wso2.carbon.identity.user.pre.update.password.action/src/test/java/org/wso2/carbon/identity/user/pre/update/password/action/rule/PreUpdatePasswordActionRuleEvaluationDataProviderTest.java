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

package org.wso2.carbon.identity.user.pre.update.password.action.rule;

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
import org.wso2.carbon.identity.user.pre.update.password.action.internal.rule.PreUpdatePasswordActionRuleEvaluationDataProvider;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.doReturn;
import static org.testng.Assert.assertEquals;

/**
 * Test class for PreUpdatePasswordActionRuleEvaluationDataProvider.
 */
@WithCarbonHome
public class PreUpdatePasswordActionRuleEvaluationDataProviderTest {

    @Mock
    private RuleEvaluationContext ruleEvaluationContext;
    @Mock
    private FlowContext flowContext;
    @Mock
    private Flow flow;

    private PreUpdatePasswordActionRuleEvaluationDataProvider dataProvider;

    @BeforeMethod
    public void setUp() {

        MockitoAnnotations.openMocks(this);
        dataProvider = new PreUpdatePasswordActionRuleEvaluationDataProvider();
        Field field = new Field("flow", ValueType.STRING);
        doReturn(Collections.singletonList(field)).when(ruleEvaluationContext).getFields();
    }

    @AfterMethod
    public void tearDown() {

        IdentityContext.destroyCurrentContext();
    }

    @Test
    public void testGetSupportedFlowType() {

        assertEquals(dataProvider.getSupportedFlowType(), FlowType.PRE_UPDATE_PASSWORD);
    }

    @DataProvider(name = "flowData")
    public Object[][] flowData() {

        return new Object[][]{
                {Flow.Name.PROFILE_UPDATE, Flow.InitiatingPersona.ADMIN, "adminInitiatedPasswordUpdate"},
                {Flow.Name.PROFILE_UPDATE, Flow.InitiatingPersona.USER, "userInitiatedPasswordUpdate"},
                {Flow.Name.PROFILE_UPDATE, Flow.InitiatingPersona.APPLICATION, "applicationInitiatedPasswordUpdate"},
                {Flow.Name.CREDENTIAL_RESET, Flow.InitiatingPersona.ADMIN, "adminInitiatedPasswordReset"},
                {Flow.Name.CREDENTIAL_RESET, Flow.InitiatingPersona.USER, "userInitiatedPasswordReset"},
                {Flow.Name.INVITE, Flow.InitiatingPersona.ADMIN,
                        "adminInitiatedUserInviteToSetPassword"},
                {Flow.Name.INVITED_USER_REGISTRATION, Flow.InitiatingPersona.ADMIN,
                        "adminInitiatedUserInviteToSetPassword"},
                {Flow.Name.REGISTER, Flow.InitiatingPersona.ADMIN,
                        "adminInitiatedRegistration"},
                {Flow.Name.REGISTER, Flow.InitiatingPersona.APPLICATION,
                        "applicationInitiatedRegistration"},
                {Flow.Name.REGISTER, Flow.InitiatingPersona.USER,
                        "userInitiatedRegistration"}
        };
    }

    @Test(dataProvider = "flowData")
    public void testGetEvaluationData(Flow.Name flowName, Flow.InitiatingPersona initiatingPersona, String fieldValue)
            throws RuleEvaluationDataProviderException {

        doReturn(flowName).when(flow).getName();
        doReturn(initiatingPersona).when(flow).getInitiatingPersona();
        IdentityContext.getThreadLocalIdentityContext().enterFlow(flow);

        List<FieldValue> fieldValues = dataProvider.getEvaluationData(ruleEvaluationContext, flowContext, "test.com");
        assertEquals(fieldValues.size(), 1);
        assertEquals(fieldValues.get(0).getValue(), fieldValue);
    }

    @Test(expectedExceptions = RuleEvaluationDataProviderException.class)
    public void testGetEvaluationDataWithUnsupportedField() throws RuleEvaluationDataProviderException {

        Field field = new Field("unsupported_field", ValueType.STRING);
        doReturn(Collections.singletonList(field)).when(ruleEvaluationContext).getFields();
        dataProvider.getEvaluationData(ruleEvaluationContext, flowContext, "test.com");
    }

    @Test(expectedExceptions = RuleEvaluationDataProviderException.class)
    public void testGetEvaluationDataWithUnsupportedFlow() throws RuleEvaluationDataProviderException {

        doReturn(Flow.Name.CREDENTIAL_RESET).when(flow).getName();
        doReturn(Flow.InitiatingPersona.APPLICATION).when(flow).getInitiatingPersona();
        IdentityContext.getThreadLocalIdentityContext().enterFlow(flow);
        dataProvider.getEvaluationData(ruleEvaluationContext, flowContext, "test.com");
    }
}
