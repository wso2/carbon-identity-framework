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

package org.wso2.carbon.identity.user.pre.update.password.action.execution;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionRequestContext;
import org.wso2.carbon.identity.action.execution.api.model.FlowContext;
import org.wso2.carbon.identity.action.management.api.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.model.Authentication;
import org.wso2.carbon.identity.action.management.api.model.EndpointConfig;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.core.context.IdentityContext;
import org.wso2.carbon.identity.core.context.model.Flow;
import org.wso2.carbon.identity.user.pre.update.password.action.internal.component.PreUpdatePasswordActionServiceComponentHolder;
import org.wso2.carbon.identity.user.pre.update.password.action.internal.execution.PreUpdatePasswordActionVersioningHandler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@WithCarbonHome
@WithRealmService(injectToSingletons = {PreUpdatePasswordActionServiceComponentHolder.class},
        initUserStoreManager = true)
public class PreUpdatePasswordActionVersioningHandlerTest {

    PreUpdatePasswordActionVersioningHandler handler = new PreUpdatePasswordActionVersioningHandler();

    Action action;
    ActionExecutionRequestContext requestContext;
    FlowContext mockFlowContext;

    @BeforeClass
    public void setUp() throws Exception {

        action = createAction();
        requestContext = ActionExecutionRequestContext.create(action);
        mockFlowContext = mock(FlowContext.class);
        IdentityContext.getThreadLocalIdentityContext().enterFlow(buildMockedFlow());
    }

    @Test(description = "Check the return value when the action is triggerable for the registration flow")
    public void canExecuteWhenTriggerable() throws Exception {

        boolean result = handler.canExecute(requestContext, mockFlowContext);
        Assert.assertFalse(result);
    }

    private Action createAction() throws ActionMgtException {

        Action action = mock(Action.class);
        when(action.getStatus()).thenReturn(Action.Status.ACTIVE);
        when(action.getId()).thenReturn("actionId");
        when(action.getType()).thenReturn(Action.ActionTypes.PRE_UPDATE_PASSWORD);
        when(action.getActionVersion()).thenReturn("v1");

        EndpointConfig endpointConfig = mock(EndpointConfig.class);
        when(action.getEndpoint()).thenReturn(endpointConfig);
        when(endpointConfig.getUri()).thenReturn("http://example.com");

        Authentication mockAuthenticationConfig = new Authentication.BasicAuthBuilder("testuser",
                "testpassword").build();
        Authentication authenticationConfig = mock(Authentication.class);
        when(authenticationConfig.getPropertiesWithDecryptedValues(any()))
                .thenReturn(mockAuthenticationConfig.getProperties());
        when(authenticationConfig.getType()).thenReturn(mockAuthenticationConfig.getType());
        when(endpointConfig.getAuthentication()).thenReturn(authenticationConfig);
        return action;
    }

    private static Flow buildMockedFlow() {

        if (Flow.isCredentialFlow(Flow.Name.REGISTER)) {
            return new Flow.CredentialFlowBuilder()
                    .name(Flow.Name.REGISTER)
                    .initiatingPersona(Flow.InitiatingPersona.USER)
                    .credentialType(Flow.CredentialType.PASSWORD)
                    .build();
        }
        return new Flow.Builder()
                .name(Flow.Name.REGISTER)
                .initiatingPersona(Flow.InitiatingPersona.USER)
                .build();
    }
}
