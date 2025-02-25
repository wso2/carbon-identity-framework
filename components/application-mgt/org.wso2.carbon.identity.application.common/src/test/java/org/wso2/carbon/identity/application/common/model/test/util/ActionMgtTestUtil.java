/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.common.model.test.util;

import org.wso2.carbon.identity.action.management.api.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.model.Authentication;
import org.wso2.carbon.identity.action.management.api.model.EndpointConfig;
import org.wso2.carbon.identity.action.management.api.service.ActionManagementService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ActionMgtTestUtil {

    public static final String ASSOCIATED_ACTION_ID = "Dummy_Action_ID";

    public static Action createAction(EndpointConfig endpointConfig) {

        Action.ActionResponseBuilder actionResponseBuilder = new Action.ActionResponseBuilder();
        actionResponseBuilder.id(ASSOCIATED_ACTION_ID);
        actionResponseBuilder.name("SampleAssociatedAction");
        actionResponseBuilder.type(Action.ActionTypes.AUTHENTICATION);
        actionResponseBuilder.description("SampleDescription");
        actionResponseBuilder.status(Action.Status.ACTIVE);
        actionResponseBuilder.endpoint(endpointConfig);
        return actionResponseBuilder.build();
    }

    public static EndpointConfig createEndpointConfig(String uri, String username, String password) {

        EndpointConfig.EndpointConfigBuilder endpointConfigBuilder = new EndpointConfig.EndpointConfigBuilder();
        endpointConfigBuilder.uri(uri);
        endpointConfigBuilder.authentication(
                new Authentication.BasicAuthBuilder(username, password).build());
        return endpointConfigBuilder.build();
    }

    public static ActionManagementService mockActionService(Action action) throws ActionMgtException {

        ActionManagementService actionManagementService = mock(ActionManagementService.class);

        when(actionManagementService.addAction(anyString(), any(), any())).thenReturn(action);
        when(actionManagementService.updateAction(anyString(), any(), any(), any())).thenReturn(action);
        when(actionManagementService.getActionByActionId(anyString(), any(), any())).thenReturn(action);
        doNothing().when(actionManagementService).deleteAction(anyString(), any(), any());

        return actionManagementService;
    }
}
