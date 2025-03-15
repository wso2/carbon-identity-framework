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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.management.api.exception.ActionDTOModelResolverClientException;
import org.wso2.carbon.identity.action.management.api.exception.ActionDTOModelResolverServerException;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.model.ActionDTO;
import org.wso2.carbon.identity.action.management.api.model.ActionPropertyForDAO;
import org.wso2.carbon.identity.action.management.api.model.Authentication;
import org.wso2.carbon.identity.action.management.api.model.BinaryObject;
import org.wso2.carbon.identity.action.management.api.model.EndpointConfig;
import org.wso2.carbon.identity.user.pre.update.profile.action.internal.management.PreUpdateProfileActionDTOModelResolver;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestUtil.ATTRIBUTES;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestUtil.INVALID_TEST_ATTRIBUTES;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestUtil.INVALID_TEST_ATTRIBUTES_VALUES;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestUtil.TENANT_DOMAIN;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestUtil.TEST_ACTION;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestUtil.TEST_ATTRIBUTES;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestUtil.TEST_DESCRIPTION;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestUtil.TEST_ID;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestUtil.TEST_PASSWORD;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestUtil.TEST_URL;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestUtil.TEST_USERNAME;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestUtil.UPDATED_TEST_ATTRIBUTES;

/**
 * Unit tests for PreUpdateProfileDTOModelResolver.
 */
public class PreUpdateProfileActionDTOModelResolverTest {

    private PreUpdateProfileActionDTOModelResolver resolver;
    private Action action;
    private ActionDTO existingActionDTO;
    private ActionDTO resolvedGetActionDTO;
    private ActionDTO resolvedGetActionDTO2;

    @BeforeClass
    public void init() {

        resolver = new PreUpdateProfileActionDTOModelResolver();
        action = new Action.ActionResponseBuilder()
                .id(TEST_ID)
                .name(TEST_ACTION)
                .description(TEST_DESCRIPTION)
                .endpoint(new EndpointConfig.EndpointConfigBuilder()
                        .uri(TEST_URL)
                        .authentication(new Authentication.BasicAuthBuilder(TEST_USERNAME, TEST_PASSWORD).build())
                        .build())
                .build();

        Map<String, Object> properties = new HashMap<>();
        properties.put(ATTRIBUTES, TEST_ATTRIBUTES);
        existingActionDTO = new ActionDTO.Builder(action).properties(properties).build();
    }

    @BeforeMethod
    public void setUp() {

    }

    @Test(priority = 1)
    public void testGetSupportedActionType() {

        Action.ActionTypes actionType = resolver.getSupportedActionType();
        assertEquals(actionType, Action.ActionTypes.PRE_UPDATE_PROFILE);
    }

    @Test(priority = 2)
    public void testResolveForAddOperation() throws Exception {

        Map<String, Object> properties = new HashMap<>();
        properties.put(ATTRIBUTES, TEST_ATTRIBUTES);
        ActionDTO actionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();

        resolvedGetActionDTO = resolver.resolveForAddOperation(actionDTO, TENANT_DOMAIN);
        resolvedGetActionDTO2 = resolver.resolveForAddOperation(actionDTO, TENANT_DOMAIN);

        assertNotNull(resolvedGetActionDTO);
        verifyCommonFields(actionDTO, resolvedGetActionDTO);
        assertNotNull(((BinaryObject) ((ActionPropertyForDAO) resolvedGetActionDTO.getProperty(ATTRIBUTES)).getValue())
                .getInputStream());
        assertTrue(resolvedGetActionDTO.getProperty(ATTRIBUTES) instanceof ActionPropertyForDAO);
    }

    @Test(priority = 3)
    public void testResolveForAddOperationWithoutAttributes() throws Exception {

        Map<String, Object> properties = new HashMap<>();
        ActionDTO actionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();

        ActionDTO result = resolver.resolveForAddOperation(actionDTO, TENANT_DOMAIN);

        assertNotNull(result);
        verifyCommonFields(actionDTO, result);
        assertEquals(result.getProperties().size(), 0);
    }

    @Test(priority = 4, expectedExceptions = ActionDTOModelResolverClientException.class,
            expectedExceptionsMessageRegExp = "Invalid attributes format.")
    public void testResolveForAddOperationWithInvalidAttributesFormat() throws Exception {

        Map<String, Object> properties = new HashMap<>();
        properties.put(ATTRIBUTES, INVALID_TEST_ATTRIBUTES);
        ActionDTO actionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();
        resolver.resolveForAddOperation(actionDTO, TENANT_DOMAIN);
    }

    @Test(priority = 5, expectedExceptions = ActionDTOModelResolverClientException.class,
            expectedExceptionsMessageRegExp = "Invalid attributes format.")
    public void testResolveForAddOperationWithInvalidAttributesValueType() throws Exception {

        Map<String, Object> properties = new HashMap<>();
        properties.put(ATTRIBUTES, INVALID_TEST_ATTRIBUTES_VALUES);
        ActionDTO actionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();
        resolver.resolveForAddOperation(actionDTO, TENANT_DOMAIN);
    }

    @Test(priority = 6, dependsOnMethods = "testResolveForAddOperation")
    public void testResolveForGetOperation() throws Exception {

        Map<String, Object> properties = new HashMap<>();

        properties.put(ATTRIBUTES, resolvedGetActionDTO.getProperty(ATTRIBUTES));
        ActionDTO actionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();

        ActionDTO result = resolver.resolveForGetOperation(actionDTO, TENANT_DOMAIN);

        assertNotNull(result);
        assertEquals(result.getProperty(ATTRIBUTES), TEST_ATTRIBUTES);
    }

    @Test(priority = 7, expectedExceptions = ActionDTOModelResolverServerException.class,
            expectedExceptionsMessageRegExp = "Unable to retrieve attributes.")
    public void testResolveForGetOperationWithInvalidPropertyValue() throws Exception {

        Map<String, Object> properties = new HashMap<>();

        properties.put(ATTRIBUTES, TEST_ATTRIBUTES);
        ActionDTO actionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();

        resolver.resolveForGetOperation(actionDTO, TENANT_DOMAIN);
    }

    @Test(priority = 8, dependsOnMethods = "testResolveForAddOperation")
    public void testResolveForGetOperationForActionList() throws Exception {

        Map<String, Object> properties = new HashMap<>();
        properties.put(ATTRIBUTES, resolvedGetActionDTO2.getProperty(ATTRIBUTES));
        ActionDTO actionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();

        List<ActionDTO> result = resolver.resolveForGetOperation(Collections.singletonList(actionDTO), TENANT_DOMAIN);

        assertNotNull(result);
        for (ActionDTO dto : result) {
            verifyCommonFields(actionDTO, dto);
            List<String> attributes = (List<String>) dto.getProperty(ATTRIBUTES);
            assertEquals(attributes, TEST_ATTRIBUTES);
        }
    }

    @Test(priority = 9)
    public void testResolveForUpdateOperation() throws Exception {

        Map<String, Object> properties = new HashMap<>();
        properties.put(ATTRIBUTES, UPDATED_TEST_ATTRIBUTES);
        ActionDTO updatingActionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();
        ActionDTO result = resolver.resolveForUpdateOperation(updatingActionDTO, existingActionDTO, TENANT_DOMAIN);

        assertNotNull(result);
        verifyCommonFields(updatingActionDTO, result);
        assertEquals(result.getProperties().size(), 1);
        assertTrue(result.getProperty(ATTRIBUTES) instanceof ActionPropertyForDAO);
    }

    @Test(priority = 10)
    public void testResolveForUpdateOperationWithoutAttributes() throws Exception {

        Map<String, Object> properties = new HashMap<>();
        ActionDTO updatingActionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();

        ActionDTO result = resolver.resolveForUpdateOperation(updatingActionDTO, existingActionDTO, TENANT_DOMAIN);
        assertNotNull(result);
        verifyCommonFields(updatingActionDTO, result);
    }

    private void verifyCommonFields(ActionDTO actionDTO, ActionDTO result) {

        assertEquals(result.getId(), actionDTO.getId());
        assertEquals(result.getName(), actionDTO.getName());
        assertEquals(result.getDescription(), actionDTO.getDescription());
        assertEquals(result.getEndpoint().getUri(), actionDTO.getEndpoint().getUri());
        assertEquals(result.getEndpoint().getAuthentication().getType(),
                actionDTO.getEndpoint().getAuthentication().getType());
        assertEquals(result.getEndpoint().getAuthentication().getProperty(Authentication.Property.USERNAME),
                actionDTO.getEndpoint().getAuthentication().getProperty(Authentication.Property.USERNAME));
        assertEquals(result.getEndpoint().getAuthentication().getProperty(Authentication.Property.PASSWORD),
                actionDTO.getEndpoint().getAuthentication().getProperty(Authentication.Property.PASSWORD));
    }
}
