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

package org.wso2.carbon.identity.user.pre.update.profile.action.management;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.model.ActionDTO;
import org.wso2.carbon.identity.action.management.api.model.Authentication;
import org.wso2.carbon.identity.action.management.api.model.EndpointConfig;
import org.wso2.carbon.identity.user.pre.update.profile.action.internal.management.PreUpdateProfileActionDTOModelResolver;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.TENANT_DOMAIN;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.TEST_ACTION;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.TEST_DESCRIPTION;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.TEST_ID;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.TEST_PASSWORD;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.TEST_URL;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.TEST_USERNAME;

/**
 * Unit tests for PreUpdateProfileDTOModelResolver.
 */
public class PreUpdateProfileActionDTOModelResolverTest {

    private PreUpdateProfileActionDTOModelResolver resolver;
    private Action action;
    private ActionDTO actionDTO;

    @BeforeClass
    public void init() {

        resolver = new PreUpdateProfileActionDTOModelResolver();
        action = new Action.ActionResponseBuilder()
                .id(TEST_ID)
                .name(TEST_ACTION)
                .description(TEST_DESCRIPTION)
                .status(Action.Status.ACTIVE)
                .createdAt(new Timestamp(new Date().getTime()))
                .updatedAt(new Timestamp(new Date().getTime() + 5000))
                .endpoint(new EndpointConfig.EndpointConfigBuilder()
                        .uri(TEST_URL)
                        .authentication(new Authentication.BasicAuthBuilder(TEST_USERNAME, TEST_PASSWORD).build())
                        .build())
                .build();
        actionDTO = new ActionDTO.Builder(action).build();
    }

    @Test
    public void testGetSupportedActionType() {

        Action.ActionTypes actionType = resolver.getSupportedActionType();
        assertEquals(actionType, Action.ActionTypes.PRE_UPDATE_PROFILE);
    }

    @Test
    public void testResolveForAddOperation() throws Exception {

        ActionDTO result = resolver.resolveForAddOperation(actionDTO, TENANT_DOMAIN);
        assertNotNull(result);
        verifyCommonFields(actionDTO, result);
    }

    @Test
    public void testResolveForGetOperation() throws Exception {

        ActionDTO result = resolver.resolveForGetOperation(actionDTO, TENANT_DOMAIN);
        assertNotNull(result);
        verifyCommonFields(actionDTO, result);
    }

    @Test
    public void testResolveForGetOperationForActionList() throws Exception {

        List<ActionDTO> result = resolver.resolveForGetOperation(Collections.singletonList(actionDTO), TENANT_DOMAIN);
        assertNotNull(result);
        assertEquals(result.size(), 1);
        verifyCommonFields(actionDTO, result.get(0));
    }

    @Test
    public void testResolveForUpdateOperation() throws Exception {

        ActionDTO existingActionDTO = new ActionDTO.Builder(action).build();
        ActionDTO result = resolver.resolveForUpdateOperation(actionDTO, existingActionDTO, TENANT_DOMAIN);
        assertNotNull(result);
        verifyCommonFields(actionDTO, result);
    }

    @Test
    public void testResolveForDeleteOperation() throws Exception {

        resolver.resolveForDeleteOperation(actionDTO, TENANT_DOMAIN);
    }

    private void verifyCommonFields(ActionDTO expected, ActionDTO result) {

        assertEquals(result.getId(), expected.getId());
        assertEquals(result.getName(), expected.getName());
        assertEquals(result.getDescription(), expected.getDescription());
        assertEquals(result.getStatus(), expected.getStatus());
        assertEquals(result.getCreatedAt(), expected.getCreatedAt());
        assertEquals(result.getUpdatedAt(), expected.getUpdatedAt());
        assertEquals(result.getEndpoint().getUri(), expected.getEndpoint().getUri());
        assertEquals(result.getEndpoint().getAuthentication().getType(),
                expected.getEndpoint().getAuthentication().getType());
        assertEquals(result.getEndpoint().getAuthentication().getProperty(Authentication.Property.USERNAME),
                expected.getEndpoint().getAuthentication().getProperty(Authentication.Property.USERNAME));
        assertEquals(result.getEndpoint().getAuthentication().getProperty(Authentication.Property.PASSWORD),
                expected.getEndpoint().getAuthentication().getProperty(Authentication.Property.PASSWORD));
    }
}

