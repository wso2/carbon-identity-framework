/*
 * Copyright (c) 2024-2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.action.management.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.management.api.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.model.ActionDTO;
import org.wso2.carbon.identity.action.management.api.model.ActionProperty;
import org.wso2.carbon.identity.action.management.api.model.Authentication;
import org.wso2.carbon.identity.action.management.api.model.BinaryObject;
import org.wso2.carbon.identity.action.management.api.model.EndpointConfig;
import org.wso2.carbon.identity.action.management.internal.dao.impl.ActionManagementDAOImpl;
import org.wso2.carbon.identity.action.management.internal.util.ActionDTOBuilder;
import org.wso2.carbon.identity.action.management.util.TestUtil;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.wso2.carbon.identity.action.management.util.TestUtil.PRE_ISSUE_ACCESS_TOKEN_ACTION_ID;
import static org.wso2.carbon.identity.action.management.util.TestUtil.PRE_ISSUE_ACCESS_TOKEN_TYPE;
import static org.wso2.carbon.identity.action.management.util.TestUtil.PRE_UPDATE_PASSWORD_ACTION_ID;
import static org.wso2.carbon.identity.action.management.util.TestUtil.PRE_UPDATE_PASSWORD_TYPE;
import static org.wso2.carbon.identity.action.management.util.TestUtil.PRE_UPDATE_PROFILE_ACTION_ID;
import static org.wso2.carbon.identity.action.management.util.TestUtil.PRE_UPDATE_PROFILE_TYPE;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TENANT_ID;

/**
 * This class is a test suite for the ActionManagementDAOImpl class.
 * It contains unit tests to verify the functionality of the methods in the ActionManagementDAOImpl class.
 * This test class will utilize {@link TestActionDTOModelResolver} class as the test implementation for
 * ActionPropertyResolver interface.
 */
@WithH2Database(files = {"dbscripts/h2.sql"})
@WithCarbonHome
public class ActionManagementDAOImplTest {

    private ActionManagementDAOImpl daoImpl;
    private ActionDTO createdActionDTO;

    @BeforeClass
    public void setUpClass() {

        daoImpl = new ActionManagementDAOImpl();
    }

    @Test(priority = 1)
    public void testAddAction() throws ActionMgtException {

        ActionDTO creatingActionDTO = new ActionDTOBuilder()
                .id(PRE_ISSUE_ACCESS_TOKEN_ACTION_ID)
                .type(Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN)
                .name(TestUtil.TEST_ACTION_NAME)
                .description(TestUtil.TEST_ACTION_DESCRIPTION)
                .status(Action.Status.INACTIVE)
                .actionVersion(TestUtil.TEST_DEFAULT_LATEST_ACTION_VERSION)
                .endpoint(new EndpointConfig.EndpointConfigBuilder()
                        .uri(TestUtil.TEST_ACTION_URI)
                        .authentication(TestUtil.buildMockBasicAuthentication(TestUtil.TEST_USERNAME_SECRET_REFERENCE,
                                TestUtil.TEST_PASSWORD_SECRET_REFERENCE))
                        .allowedHeaders(TestUtil.buildMockAllowedHeaders())
                        .allowedParameters(TestUtil.buildMockAllowedParameters())
                        .build())
                .property(TestUtil.TEST_ACTION_PROPERTY_NAME_1,
                        new ActionProperty.BuilderForDAO(TestUtil.TEST_ACTION_PROPERTY_VALUE_1).build())
                .property(TestUtil.TEST_ACTION_PROPERTY_NAME_2,
                        new ActionProperty.BuilderForDAO(TestUtil.TEST_ACTION_PROPERTY_VALUE_2).build())
                .build();

        try {
            daoImpl.addAction(creatingActionDTO, TENANT_ID);
        } catch (Exception e) {
            Assert.fail();
        }
        createdActionDTO = daoImpl.getActionByActionId(PRE_ISSUE_ACCESS_TOKEN_TYPE, PRE_ISSUE_ACCESS_TOKEN_ACTION_ID,
                TENANT_ID);
        Assert.assertEquals(createdActionDTO.getId(), creatingActionDTO.getId());
        Assert.assertEquals(createdActionDTO.getType(), creatingActionDTO.getType());
        Assert.assertEquals(createdActionDTO.getName(), creatingActionDTO.getName());
        Assert.assertEquals(createdActionDTO.getDescription(), creatingActionDTO.getDescription());
        Assert.assertEquals(createdActionDTO.getStatus(), Action.Status.INACTIVE);
        Assert.assertEquals(createdActionDTO.getActionVersion(), TestUtil.TEST_DEFAULT_LATEST_ACTION_VERSION);
        Assert.assertNotNull(createdActionDTO.getCreatedAt());
        Assert.assertNotNull(createdActionDTO.getUpdatedAt());
        Assert.assertEquals(createdActionDTO.getCreatedAt().getTime(), createdActionDTO.getUpdatedAt().getTime());
        Assert.assertEquals(createdActionDTO.getEndpoint().getUri(), creatingActionDTO.getEndpoint().getUri());
        Assert.assertEquals(createdActionDTO.getEndpoint().getAllowedHeaders(),
                creatingActionDTO.getEndpoint().getAllowedHeaders());
        Assert.assertEquals(createdActionDTO.getEndpoint().getAllowedParameters(),
                creatingActionDTO.getEndpoint().getAllowedParameters());

        Authentication createdAuthentication = createdActionDTO.getEndpoint().getAuthentication();
        Assert.assertEquals(createdAuthentication.getType(),
                creatingActionDTO.getEndpoint().getAuthentication().getType());
        Assert.assertEquals(createdAuthentication.getProperties().size(),
                creatingActionDTO.getEndpoint().getAuthentication().getProperties().size());
        Assert.assertEquals(createdAuthentication.getProperty(Authentication.Property.USERNAME).getValue(),
                TestUtil.TEST_USERNAME_SECRET_REFERENCE);
        Assert.assertEquals(createdAuthentication.getProperty(Authentication.Property.PASSWORD).getValue(),
                TestUtil.TEST_PASSWORD_SECRET_REFERENCE);

        Assert.assertEquals(createdActionDTO.getProperties().size(), creatingActionDTO.getProperties().size());
        Assert.assertEquals(createdActionDTO.getPropertyValue(TestUtil.TEST_ACTION_PROPERTY_NAME_1),
                creatingActionDTO.getPropertyValue(TestUtil.TEST_ACTION_PROPERTY_NAME_1));
        Assert.assertEquals(createdActionDTO.getPropertyValue(TestUtil.TEST_ACTION_PROPERTY_NAME_2),
                creatingActionDTO.getPropertyValue(TestUtil.TEST_ACTION_PROPERTY_NAME_2));
    }

    @Test(priority = 2, expectedExceptions = ActionMgtException.class,
            expectedExceptionsMessageRegExp = "Error while adding Action Basic information in the system.")
    public void testAddActionWithoutName() throws ActionMgtException {

        ActionDTO creatingActionDTO = new ActionDTOBuilder()
                .id(String.valueOf(UUID.randomUUID()))
                .type(Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN)
                .name(null)
                .endpoint(new EndpointConfig.EndpointConfigBuilder()
                        .uri(TestUtil.TEST_ACTION_URI)
                        .authentication(TestUtil.buildMockBasicAuthentication(TestUtil.TEST_USERNAME_SECRET_REFERENCE,
                                TestUtil.TEST_PASSWORD_SECRET_REFERENCE))
                        .allowedHeaders(TestUtil.buildMockAllowedHeaders())
                        .allowedParameters(TestUtil.buildMockAllowedParameters())
                        .build())
                .build();

        daoImpl.addAction(creatingActionDTO, TENANT_ID);
    }

    @Test(priority = 3)
    public void testGetActionsByActionType() throws ActionMgtException {

        List<ActionDTO> actionDTOList = daoImpl.getActionsByActionType(PRE_ISSUE_ACCESS_TOKEN_TYPE, TENANT_ID);
        Assert.assertEquals(actionDTOList.size(), 1);
        ActionDTO result = actionDTOList.get(0);
        Assert.assertEquals(result.getId(), createdActionDTO.getId());
        Assert.assertEquals(result.getType(), createdActionDTO.getType());
        Assert.assertEquals(result.getName(), createdActionDTO.getName());
        Assert.assertEquals(result.getDescription(), createdActionDTO.getDescription());
        Assert.assertEquals(result.getStatus(), createdActionDTO.getStatus());
        Assert.assertEquals(createdActionDTO.getActionVersion(), TestUtil.TEST_DEFAULT_LATEST_ACTION_VERSION);
        Assert.assertNotNull(result.getCreatedAt());
        Assert.assertEquals(result.getCreatedAt().getTime(), createdActionDTO.getCreatedAt().getTime());
        Assert.assertNotNull(result.getUpdatedAt());
        Assert.assertEquals(result.getEndpoint().getUri(), createdActionDTO.getEndpoint().getUri());
        Assert.assertEquals(result.getEndpoint().getAllowedHeaders(),
                createdActionDTO.getEndpoint().getAllowedHeaders());
        Assert.assertEquals(result.getEndpoint().getAllowedParameters(),
                createdActionDTO.getEndpoint().getAllowedParameters());
        Assert.assertEquals(result.getEndpoint().getAuthentication().getType(),
                createdActionDTO.getEndpoint().getAuthentication().getType());

        Authentication createdAuthentication = result.getEndpoint().getAuthentication();
        Assert.assertEquals(createdAuthentication.getType(),
                createdActionDTO.getEndpoint().getAuthentication().getType());
        Assert.assertEquals(createdAuthentication.getProperties().size(),
                createdActionDTO.getEndpoint().getAuthentication().getProperties().size());
        Assert.assertEquals(createdAuthentication.getProperty(Authentication.Property.USERNAME).getValue(),
                TestUtil.TEST_USERNAME_SECRET_REFERENCE);
        Assert.assertEquals(createdAuthentication.getProperty(Authentication.Property.PASSWORD).getValue(),
                TestUtil.TEST_PASSWORD_SECRET_REFERENCE);

        Assert.assertEquals(result.getProperties().size(), createdActionDTO.getProperties().size());
        Assert.assertEquals(result.getPropertyValue(TestUtil.TEST_ACTION_PROPERTY_NAME_1),
                createdActionDTO.getPropertyValue(TestUtil.TEST_ACTION_PROPERTY_NAME_1));
        Assert.assertEquals(result.getPropertyValue(TestUtil.TEST_ACTION_PROPERTY_NAME_2),
                createdActionDTO.getPropertyValue(TestUtil.TEST_ACTION_PROPERTY_NAME_2));
    }

    @Test(priority = 4)
    public void testDeleteAction() throws ActionMgtException {

        try {
            daoImpl.deleteAction(createdActionDTO, TENANT_ID);
        } catch (Exception e) {
            Assert.fail();
        }
        Assert.assertNull(daoImpl.getActionByActionId(PRE_ISSUE_ACCESS_TOKEN_TYPE, PRE_ISSUE_ACCESS_TOKEN_ACTION_ID,
                TENANT_ID));
    }

    @Test(priority = 5)
    public void testAddActionWithoutDescription() throws ActionMgtException {

        ActionDTO creatingActionDTO = new ActionDTOBuilder()
                .id(PRE_ISSUE_ACCESS_TOKEN_ACTION_ID)
                .type(Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN)
                .name(TestUtil.TEST_ACTION_NAME)
                .status(Action.Status.INACTIVE)
                .actionVersion(TestUtil.TEST_DEFAULT_LATEST_ACTION_VERSION)
                .endpoint(new EndpointConfig.EndpointConfigBuilder()
                        .uri(TestUtil.TEST_ACTION_URI)
                        .authentication(TestUtil.buildMockBasicAuthentication(TestUtil.TEST_USERNAME_SECRET_REFERENCE,
                                TestUtil.TEST_PASSWORD_SECRET_REFERENCE))
                        .allowedHeaders(TestUtil.buildMockAllowedHeaders())
                        .allowedParameters(TestUtil.buildMockAllowedParameters())
                        .build())
                .property(TestUtil.TEST_ACTION_PROPERTY_NAME_1,
                        new ActionProperty.BuilderForDAO(TestUtil.TEST_ACTION_PROPERTY_VALUE_1).build())
                .property(TestUtil.TEST_ACTION_PROPERTY_NAME_2,
                        new ActionProperty.BuilderForDAO(TestUtil.TEST_ACTION_PROPERTY_VALUE_2).build())
                .build();
        try {
            daoImpl.addAction(creatingActionDTO, TENANT_ID);
        } catch (Exception e) {
            Assert.fail();
        }
        createdActionDTO = daoImpl.getActionByActionId(PRE_ISSUE_ACCESS_TOKEN_TYPE, PRE_ISSUE_ACCESS_TOKEN_ACTION_ID,
                TENANT_ID);
        Assert.assertEquals(createdActionDTO.getId(), creatingActionDTO.getId());
        Assert.assertEquals(createdActionDTO.getType(), creatingActionDTO.getType());
        Assert.assertEquals(createdActionDTO.getName(), creatingActionDTO.getName());
        Assert.assertNull(createdActionDTO.getDescription());
        Assert.assertEquals(createdActionDTO.getStatus(), Action.Status.INACTIVE);
        Assert.assertEquals(createdActionDTO.getActionVersion(), TestUtil.TEST_DEFAULT_LATEST_ACTION_VERSION);
        Assert.assertNotNull(createdActionDTO.getCreatedAt());
        Assert.assertNotNull(createdActionDTO.getUpdatedAt());
        Assert.assertEquals(createdActionDTO.getCreatedAt().getTime(), createdActionDTO.getUpdatedAt().getTime());
        Assert.assertEquals(createdActionDTO.getEndpoint().getUri(), creatingActionDTO.getEndpoint().getUri());
        Assert.assertEquals(createdActionDTO.getEndpoint().getAllowedHeaders(),
                creatingActionDTO.getEndpoint().getAllowedHeaders());
        Assert.assertEquals(createdActionDTO.getEndpoint().getAllowedParameters(),
                creatingActionDTO.getEndpoint().getAllowedParameters());

        Authentication createdAuthentication = createdActionDTO.getEndpoint().getAuthentication();
        Assert.assertEquals(createdAuthentication.getType(),
                creatingActionDTO.getEndpoint().getAuthentication().getType());
        Assert.assertEquals(createdAuthentication.getProperties().size(),
                creatingActionDTO.getEndpoint().getAuthentication().getProperties().size());
        Assert.assertEquals(createdAuthentication.getProperty(Authentication.Property.USERNAME).getValue(),
                TestUtil.TEST_USERNAME_SECRET_REFERENCE);
        Assert.assertEquals(createdAuthentication.getProperty(Authentication.Property.PASSWORD).getValue(),
                TestUtil.TEST_PASSWORD_SECRET_REFERENCE);

        Assert.assertEquals(createdActionDTO.getProperties().size(), creatingActionDTO.getProperties().size());
        Assert.assertEquals(createdActionDTO.getPropertyValue(TestUtil.TEST_ACTION_PROPERTY_NAME_1),
                creatingActionDTO.getPropertyValue(TestUtil.TEST_ACTION_PROPERTY_NAME_1));
        Assert.assertEquals(createdActionDTO.getPropertyValue(TestUtil.TEST_ACTION_PROPERTY_NAME_2),
                creatingActionDTO.getPropertyValue(TestUtil.TEST_ACTION_PROPERTY_NAME_2));
    }

    @Test(priority = 7, dependsOnMethods = "testAddActionWithoutDescription")
    public void testUpdateCompleteAction() throws ActionMgtException {

        ActionDTO updatingAction = new ActionDTOBuilder()
                .id(createdActionDTO.getId())
                .type(Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN)
                .name(TestUtil.TEST_ACTION_NAME_UPDATED)
                .description(TestUtil.TEST_ACTION_DESCRIPTION_UPDATED)
                .endpoint(new EndpointConfig.EndpointConfigBuilder()
                        .uri(TestUtil.TEST_ACTION_URI_UPDATED)
                        .authentication(TestUtil.buildMockBearerAuthentication(
                                TestUtil.TEST_ACCESS_TOKEN_SECRET_REFERENCE))
                        .allowedHeaders(TestUtil.buildMockAllowedHeaders())
                        .allowedParameters(TestUtil.buildMockAllowedParameters())
                        .build())
                .property(TestUtil.TEST_ACTION_PROPERTY_NAME_1,
                        new ActionProperty.BuilderForDAO(TestUtil.TEST_ACTION_PROPERTY_VALUE_1_UPDATED).build())
                .property(TestUtil.TEST_ACTION_PROPERTY_NAME_2,
                        new ActionProperty.BuilderForDAO(TestUtil.TEST_ACTION_PROPERTY_VALUE_2_UPDATED).build())
                .build();
        try {
            daoImpl.updateAction(updatingAction, createdActionDTO, TENANT_ID);
        } catch (Exception e) {
            Assert.fail();
        }
        ActionDTO result = daoImpl.getActionByActionId(PRE_ISSUE_ACCESS_TOKEN_TYPE, updatingAction.getId(), TENANT_ID);
        Assert.assertEquals(result.getId(), createdActionDTO.getId());
        Assert.assertEquals(result.getType(), createdActionDTO.getType());
        Assert.assertEquals(result.getName(), updatingAction.getName());
        Assert.assertEquals(result.getDescription(), updatingAction.getDescription());
        Assert.assertEquals(result.getStatus(), createdActionDTO.getStatus());
        Assert.assertEquals(result.getActionVersion(), TestUtil.TEST_DEFAULT_LATEST_ACTION_VERSION);
        Assert.assertNotNull(result.getCreatedAt());
        Assert.assertEquals(result.getCreatedAt().getTime(), createdActionDTO.getCreatedAt().getTime());
        Assert.assertNotNull(result.getUpdatedAt());
        Assert.assertTrue(result.getUpdatedAt().after(createdActionDTO.getUpdatedAt()));
        Assert.assertEquals(result.getEndpoint().getUri(), updatingAction.getEndpoint().getUri());
        Assert.assertEquals(result.getEndpoint().getAllowedHeaders(),
                updatingAction.getEndpoint().getAllowedHeaders());
        Assert.assertEquals(result.getEndpoint().getAllowedParameters(),
                updatingAction.getEndpoint().getAllowedParameters());

        Authentication updatedAuthentication = result.getEndpoint().getAuthentication();
        Assert.assertEquals(updatedAuthentication.getType(),
                updatingAction.getEndpoint().getAuthentication().getType());
        Assert.assertEquals(updatedAuthentication.getProperties().size(),
                updatingAction.getEndpoint().getAuthentication().getProperties().size());
        Assert.assertEquals(updatedAuthentication.getProperty(Authentication.Property.ACCESS_TOKEN).getValue(),
                TestUtil.TEST_ACCESS_TOKEN_SECRET_REFERENCE);

        Assert.assertEquals(result.getProperties().size(), updatingAction.getProperties().size());
        Assert.assertEquals(result.getPropertyValue(TestUtil.TEST_ACTION_PROPERTY_NAME_1),
                updatingAction.getPropertyValue(TestUtil.TEST_ACTION_PROPERTY_NAME_1));
        Assert.assertEquals(result.getPropertyValue(TestUtil.TEST_ACTION_PROPERTY_NAME_2),
                updatingAction.getPropertyValue(TestUtil.TEST_ACTION_PROPERTY_NAME_2));
        createdActionDTO = result;
    }

    @Test(priority = 8)
    public void testUpdateActionBasicInfo() throws ActionMgtException {

        ActionDTO updatingAction = new ActionDTOBuilder()
                .id(createdActionDTO.getId())
                .type(Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN)
                .name(TestUtil.TEST_ACTION_NAME)
                .description(TestUtil.TEST_ACTION_DESCRIPTION)
                .build();

        try {
            daoImpl.updateAction(updatingAction, createdActionDTO, TENANT_ID);
        } catch (Exception e) {
            Assert.fail();
        }
        ActionDTO result = daoImpl.getActionByActionId(PRE_ISSUE_ACCESS_TOKEN_TYPE, updatingAction.getId(), TENANT_ID);
        Assert.assertEquals(result.getId(), createdActionDTO.getId());
        Assert.assertEquals(result.getType(), createdActionDTO.getType());
        Assert.assertEquals(result.getName(), updatingAction.getName());
        Assert.assertEquals(result.getDescription(), updatingAction.getDescription());
        Assert.assertEquals(result.getStatus(), createdActionDTO.getStatus());
        Assert.assertEquals(result.getActionVersion(), TestUtil.TEST_DEFAULT_LATEST_ACTION_VERSION);
        Assert.assertNotNull(result.getCreatedAt());
        Assert.assertEquals(result.getCreatedAt().getTime(), createdActionDTO.getCreatedAt().getTime());
        Assert.assertNotNull(result.getUpdatedAt());
        Assert.assertTrue(result.getUpdatedAt().after(createdActionDTO.getUpdatedAt()));
        Assert.assertEquals(result.getEndpoint().getUri(), createdActionDTO.getEndpoint().getUri());

        Authentication resultAuthentication = result.getEndpoint().getAuthentication();
        Assert.assertEquals(resultAuthentication.getType(),
                createdActionDTO.getEndpoint().getAuthentication().getType());
        Assert.assertEquals(resultAuthentication.getProperties().size(),
                createdActionDTO.getEndpoint().getAuthentication().getProperties().size());
        Assert.assertEquals(resultAuthentication.getProperty(Authentication.Property.ACCESS_TOKEN).getValue(),
                TestUtil.TEST_ACCESS_TOKEN_SECRET_REFERENCE);

        Assert.assertEquals(result.getProperties().size(), createdActionDTO.getProperties().size());
        Assert.assertEquals(result.getPropertyValue(TestUtil.TEST_ACTION_PROPERTY_NAME_1),
                createdActionDTO.getPropertyValue(TestUtil.TEST_ACTION_PROPERTY_NAME_1));
        Assert.assertEquals(result.getPropertyValue(TestUtil.TEST_ACTION_PROPERTY_NAME_2),
                createdActionDTO.getPropertyValue(TestUtil.TEST_ACTION_PROPERTY_NAME_2));
        createdActionDTO = result;
    }

    @Test(priority = 9)
    public void testUpdateActionEndpoint() throws ActionMgtException {

        ActionDTO updatingAction = new ActionDTOBuilder()
                .id(createdActionDTO.getId())
                .type(Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN)
                .endpoint(new EndpointConfig.EndpointConfigBuilder()
                        .uri(TestUtil.TEST_ACTION_URI)
                        .authentication(TestUtil.buildMockAPIKeyAuthentication(TestUtil.TEST_API_KEY_HEADER,
                                TestUtil.TEST_API_KEY_VALUE_SECRET_REFERENCE))
                        .allowedHeaders(TestUtil.buildMockAllowedHeaders())
                        .allowedParameters(TestUtil.buildMockAllowedParameters())
                        .build())
                .build();

        try {
            daoImpl.updateAction(updatingAction, createdActionDTO, TENANT_ID);
        } catch (Exception e) {
            Assert.fail();
        }
        ActionDTO result = daoImpl.getActionByActionId(PRE_ISSUE_ACCESS_TOKEN_TYPE, updatingAction.getId(), TENANT_ID);
        Assert.assertEquals(result.getId(), createdActionDTO.getId());
        Assert.assertEquals(result.getType(), createdActionDTO.getType());
        Assert.assertEquals(result.getName(), createdActionDTO.getName());
        Assert.assertEquals(result.getDescription(), createdActionDTO.getDescription());
        Assert.assertEquals(result.getStatus(), createdActionDTO.getStatus());
        Assert.assertEquals(result.getActionVersion(), TestUtil.TEST_DEFAULT_LATEST_ACTION_VERSION);
        Assert.assertNotNull(result.getCreatedAt());
        Assert.assertEquals(result.getCreatedAt().getTime(), createdActionDTO.getCreatedAt().getTime());
        Assert.assertNotNull(result.getUpdatedAt());
        Assert.assertTrue(result.getUpdatedAt().after(createdActionDTO.getUpdatedAt()));
        Assert.assertEquals(result.getEndpoint().getUri(), updatingAction.getEndpoint().getUri());
        Assert.assertEquals(result.getEndpoint().getAllowedHeaders(),
                updatingAction.getEndpoint().getAllowedHeaders());
        Assert.assertEquals(result.getEndpoint().getAllowedParameters(),
                updatingAction.getEndpoint().getAllowedParameters());

        Authentication updatedAuthentication = result.getEndpoint().getAuthentication();
        Assert.assertEquals(updatedAuthentication.getType(),
                updatingAction.getEndpoint().getAuthentication().getType());
        Assert.assertEquals(updatedAuthentication.getProperties().size(),
                updatingAction.getEndpoint().getAuthentication().getProperties().size());
        Assert.assertEquals(updatedAuthentication.getProperty(Authentication.Property.HEADER).getValue(),
                TestUtil.TEST_API_KEY_HEADER);
        Assert.assertEquals(updatedAuthentication.getProperty(Authentication.Property.VALUE).getValue(),
                TestUtil.TEST_API_KEY_VALUE_SECRET_REFERENCE);

        Assert.assertEquals(result.getProperties().size(), createdActionDTO.getProperties().size());
        Assert.assertEquals(result.getPropertyValue(TestUtil.TEST_ACTION_PROPERTY_NAME_1),
                createdActionDTO.getPropertyValue(TestUtil.TEST_ACTION_PROPERTY_NAME_1));
        Assert.assertEquals(result.getPropertyValue(TestUtil.TEST_ACTION_PROPERTY_NAME_2),
                createdActionDTO.getPropertyValue(TestUtil.TEST_ACTION_PROPERTY_NAME_2));
        createdActionDTO = result;
    }

    @Test(priority = 10)
    public void testUpdateActionEndpointUri() throws ActionMgtException {

        ActionDTO updatingAction = new ActionDTOBuilder()
                .id(createdActionDTO.getId())
                .type(Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN)
                .endpoint(new EndpointConfig.EndpointConfigBuilder()
                        .uri(TestUtil.TEST_ACTION_URI_UPDATED)
                        .build())
                .build();

        try {
            daoImpl.updateAction(updatingAction, createdActionDTO, TENANT_ID);
        } catch (Exception e) {
            Assert.fail();
        }
        ActionDTO result = daoImpl.getActionByActionId(PRE_ISSUE_ACCESS_TOKEN_TYPE, updatingAction.getId(), TENANT_ID);
        Assert.assertEquals(result.getId(), createdActionDTO.getId());
        Assert.assertEquals(result.getType(), createdActionDTO.getType());
        Assert.assertEquals(result.getName(), createdActionDTO.getName());
        Assert.assertEquals(result.getDescription(), createdActionDTO.getDescription());
        Assert.assertEquals(result.getStatus(), createdActionDTO.getStatus());
        Assert.assertEquals(result.getActionVersion(), TestUtil.TEST_DEFAULT_LATEST_ACTION_VERSION);
        Assert.assertNotNull(result.getCreatedAt());
        Assert.assertEquals(result.getCreatedAt().getTime(), createdActionDTO.getCreatedAt().getTime());
        Assert.assertNotNull(result.getUpdatedAt());
        Assert.assertTrue(result.getUpdatedAt().after(createdActionDTO.getUpdatedAt()));
        Assert.assertEquals(result.getEndpoint().getUri(), updatingAction.getEndpoint().getUri());

        Authentication resultAuthentication = result.getEndpoint().getAuthentication();
        Assert.assertEquals(resultAuthentication.getType(),
                createdActionDTO.getEndpoint().getAuthentication().getType());
        Assert.assertEquals(resultAuthentication.getProperties().size(),
                createdActionDTO.getEndpoint().getAuthentication().getProperties().size());
        Assert.assertEquals(resultAuthentication.getProperty(Authentication.Property.HEADER).getValue(),
                TestUtil.TEST_API_KEY_HEADER);
        Assert.assertEquals(resultAuthentication.getProperty(Authentication.Property.VALUE).getValue(),
                TestUtil.TEST_API_KEY_VALUE_SECRET_REFERENCE);

        Assert.assertEquals(result.getProperties().size(), createdActionDTO.getProperties().size());
        Assert.assertEquals(result.getPropertyValue(TestUtil.TEST_ACTION_PROPERTY_NAME_1),
                createdActionDTO.getPropertyValue(TestUtil.TEST_ACTION_PROPERTY_NAME_1));
        Assert.assertEquals(result.getPropertyValue(TestUtil.TEST_ACTION_PROPERTY_NAME_2),
                createdActionDTO.getPropertyValue(TestUtil.TEST_ACTION_PROPERTY_NAME_2));
        createdActionDTO = result;
    }

    @Test(priority = 11)
    public void testUpdateAllowedHeadersAndParameters() throws ActionMgtException {

        List<String> updatedAllowedHeaders = new ArrayList<>();
        updatedAllowedHeaders.add("new-test-header-1");
        updatedAllowedHeaders.add("new-test-header-2");

        List<String> updatedAllowedParameters = new ArrayList<>();
        updatedAllowedParameters.add("new_testParam_1");
        updatedAllowedParameters.add("testParam_2");

        ActionDTO updatingAction = new ActionDTOBuilder()
                .id(createdActionDTO.getId())
                .type(Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN)
                .endpoint(new EndpointConfig.EndpointConfigBuilder()
                        .uri(createdActionDTO.getEndpoint().getUri())
                        .allowedHeaders(updatedAllowedHeaders)
                        .allowedParameters(updatedAllowedParameters)
                        .build())
                .build();

        try {
            daoImpl.updateAction(updatingAction, createdActionDTO, TENANT_ID);
        } catch (Exception e) {
            Assert.fail();
        }
        ActionDTO result = daoImpl.getActionByActionId(PRE_ISSUE_ACCESS_TOKEN_TYPE, updatingAction.getId(), TENANT_ID);
        Assert.assertEquals(result.getId(), createdActionDTO.getId());
        Assert.assertEquals(result.getType(), createdActionDTO.getType());
        Assert.assertEquals(result.getName(), createdActionDTO.getName());
        Assert.assertEquals(result.getDescription(), createdActionDTO.getDescription());
        Assert.assertEquals(result.getStatus(), createdActionDTO.getStatus());
        Assert.assertEquals(result.getActionVersion(), TestUtil.TEST_DEFAULT_LATEST_ACTION_VERSION);
        Assert.assertNotNull(result.getCreatedAt());
        Assert.assertEquals(result.getCreatedAt().getTime(), createdActionDTO.getCreatedAt().getTime());
        Assert.assertNotNull(result.getUpdatedAt());
        Assert.assertTrue(result.getUpdatedAt().after(createdActionDTO.getUpdatedAt()));
        Assert.assertEquals(result.getEndpoint().getUri(), updatingAction.getEndpoint().getUri());
        Assert.assertEquals(result.getEndpoint().getAllowedHeaders(),
                updatingAction.getEndpoint().getAllowedHeaders());
        Assert.assertEquals(result.getEndpoint().getAllowedParameters(),
                updatingAction.getEndpoint().getAllowedParameters());

        Authentication resultAuthentication = result.getEndpoint().getAuthentication();
        Assert.assertEquals(resultAuthentication.getType(),
                createdActionDTO.getEndpoint().getAuthentication().getType());
        Assert.assertEquals(resultAuthentication.getProperties().size(),
                createdActionDTO.getEndpoint().getAuthentication().getProperties().size());
        Assert.assertEquals(resultAuthentication.getProperty(Authentication.Property.HEADER).getValue(),
                TestUtil.TEST_API_KEY_HEADER);
        Assert.assertEquals(resultAuthentication.getProperty(Authentication.Property.VALUE).getValue(),
                TestUtil.TEST_API_KEY_VALUE_SECRET_REFERENCE);

        Assert.assertEquals(result.getProperties().size(), createdActionDTO.getProperties().size());
        Assert.assertEquals(result.getPropertyValue(TestUtil.TEST_ACTION_PROPERTY_NAME_1),
                createdActionDTO.getPropertyValue(TestUtil.TEST_ACTION_PROPERTY_NAME_1));
        Assert.assertEquals(result.getPropertyValue(TestUtil.TEST_ACTION_PROPERTY_NAME_2),
                createdActionDTO.getPropertyValue(TestUtil.TEST_ACTION_PROPERTY_NAME_2));
        createdActionDTO = result;
    }

    @Test(priority = 12)
    public void testUpdateActionEndpointAuthenticationWithSameAuthType() throws ActionMgtException {

        ActionDTO updatingAction = new ActionDTOBuilder()
                .id(createdActionDTO.getId())
                .type(Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN)
                .endpoint(new EndpointConfig.EndpointConfigBuilder()
                        .authentication(TestUtil.buildMockAPIKeyAuthentication(TestUtil.TEST_API_KEY_HEADER_UPDATED,
                                TestUtil.TEST_API_KEY_VALUE_SECRET_REFERENCE))
                        .build())
                .build();

        try {
            daoImpl.updateAction(updatingAction, createdActionDTO, TENANT_ID);
        } catch (Exception e) {
            Assert.fail();
        }
        ActionDTO result = daoImpl.getActionByActionId(PRE_ISSUE_ACCESS_TOKEN_TYPE, updatingAction.getId(), TENANT_ID);
        Assert.assertEquals(result.getId(), createdActionDTO.getId());
        Assert.assertEquals(result.getType(), createdActionDTO.getType());
        Assert.assertEquals(result.getName(), createdActionDTO.getName());
        Assert.assertEquals(result.getDescription(), createdActionDTO.getDescription());
        Assert.assertEquals(result.getStatus(), createdActionDTO.getStatus());
        Assert.assertEquals(result.getActionVersion(), TestUtil.TEST_DEFAULT_LATEST_ACTION_VERSION);
        Assert.assertNotNull(result.getCreatedAt());
        Assert.assertEquals(result.getCreatedAt().getTime(), createdActionDTO.getCreatedAt().getTime());
        Assert.assertNotNull(result.getUpdatedAt());
        Assert.assertTrue(result.getUpdatedAt().after(createdActionDTO.getUpdatedAt()));
        Assert.assertEquals(result.getEndpoint().getUri(), createdActionDTO.getEndpoint().getUri());

        Authentication updatedAuthentication = result.getEndpoint().getAuthentication();
        Assert.assertEquals(updatedAuthentication.getType(),
                updatingAction.getEndpoint().getAuthentication().getType());
        Assert.assertEquals(updatedAuthentication.getProperties().size(),
                updatingAction.getEndpoint().getAuthentication().getProperties().size());
        Assert.assertEquals(updatedAuthentication.getProperty(Authentication.Property.HEADER).getValue(),
                TestUtil.TEST_API_KEY_HEADER_UPDATED);
        Assert.assertEquals(updatedAuthentication.getProperty(Authentication.Property.VALUE).getValue(),
                TestUtil.TEST_API_KEY_VALUE_SECRET_REFERENCE);

        Assert.assertEquals(result.getProperties().size(), createdActionDTO.getProperties().size());
        Assert.assertEquals(result.getPropertyValue(TestUtil.TEST_ACTION_PROPERTY_NAME_1),
                createdActionDTO.getPropertyValue(TestUtil.TEST_ACTION_PROPERTY_NAME_1));
        Assert.assertEquals(result.getPropertyValue(TestUtil.TEST_ACTION_PROPERTY_NAME_2),
                createdActionDTO.getPropertyValue(TestUtil.TEST_ACTION_PROPERTY_NAME_2));

        createdActionDTO = result;
    }

    @Test(priority = 13)
    public void testUpdateActionEndpointAuthenticationWithDifferentAuthType() throws ActionMgtException {

        ActionDTO updatingAction = new ActionDTOBuilder()
                .id(createdActionDTO.getId())
                .type(Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN)
                .endpoint(new EndpointConfig.EndpointConfigBuilder()
                        .authentication(new Authentication.NoneAuthBuilder().build())
                        .build())
                .build();

        try {
            daoImpl.updateAction(updatingAction, createdActionDTO, TENANT_ID);
        } catch (Exception e) {
            Assert.fail();
        }
        ActionDTO result = daoImpl.getActionByActionId(PRE_ISSUE_ACCESS_TOKEN_TYPE, updatingAction.getId(), TENANT_ID);
        Assert.assertEquals(result.getId(), createdActionDTO.getId());
        Assert.assertEquals(result.getType(), createdActionDTO.getType());
        Assert.assertEquals(result.getName(), createdActionDTO.getName());
        Assert.assertEquals(result.getDescription(), createdActionDTO.getDescription());
        Assert.assertEquals(result.getStatus(), createdActionDTO.getStatus());
        Assert.assertEquals(result.getActionVersion(), TestUtil.TEST_DEFAULT_LATEST_ACTION_VERSION);
        Assert.assertNotNull(result.getCreatedAt());
        Assert.assertEquals(result.getCreatedAt().getTime(), createdActionDTO.getCreatedAt().getTime());
        Assert.assertNotNull(result.getUpdatedAt());
        Assert.assertTrue(result.getUpdatedAt().after(createdActionDTO.getUpdatedAt()));
        Assert.assertEquals(result.getEndpoint().getUri(), createdActionDTO.getEndpoint().getUri());

        Authentication updatedAuthentication = result.getEndpoint().getAuthentication();
        Assert.assertEquals(updatedAuthentication.getType(),
                updatingAction.getEndpoint().getAuthentication().getType());
        Assert.assertEquals(updatedAuthentication.getProperties().size(),
                updatingAction.getEndpoint().getAuthentication().getProperties().size());

        Assert.assertEquals(result.getProperties().size(), createdActionDTO.getProperties().size());
        Assert.assertEquals(result.getPropertyValue(TestUtil.TEST_ACTION_PROPERTY_NAME_1),
                createdActionDTO.getPropertyValue(TestUtil.TEST_ACTION_PROPERTY_NAME_1));
        Assert.assertEquals(result.getPropertyValue(TestUtil.TEST_ACTION_PROPERTY_NAME_2),
                createdActionDTO.getPropertyValue(TestUtil.TEST_ACTION_PROPERTY_NAME_2));
        createdActionDTO = result;
    }

    @Test(priority = 14)
    public void testUpdateActionProperties() throws ActionMgtException {

        ActionDTO updatingAction = new ActionDTOBuilder()
                .id(createdActionDTO.getId())
                .type(Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN)
                .property(TestUtil.TEST_ACTION_PROPERTY_NAME_1,
                        new ActionProperty.BuilderForDAO(TestUtil.TEST_ACTION_PROPERTY_VALUE_1).build())
                .property(TestUtil.TEST_ACTION_PROPERTY_NAME_2,
                        new ActionProperty.BuilderForDAO(TestUtil.TEST_ACTION_PROPERTY_VALUE_2).build())
                .build();

        try {
            daoImpl.updateAction(updatingAction, createdActionDTO, TENANT_ID);
        } catch (Exception e) {
            Assert.fail();
        }
        ActionDTO result = daoImpl.getActionByActionId(PRE_ISSUE_ACCESS_TOKEN_TYPE, updatingAction.getId(), TENANT_ID);
        Assert.assertEquals(result.getId(), createdActionDTO.getId());
        Assert.assertEquals(result.getType(), createdActionDTO.getType());
        Assert.assertEquals(result.getName(), createdActionDTO.getName());
        Assert.assertEquals(result.getDescription(), createdActionDTO.getDescription());
        Assert.assertEquals(result.getStatus(), createdActionDTO.getStatus());
        Assert.assertEquals(result.getActionVersion(), TestUtil.TEST_DEFAULT_LATEST_ACTION_VERSION);
        Assert.assertNotNull(result.getCreatedAt());
        Assert.assertEquals(result.getCreatedAt().getTime(), createdActionDTO.getCreatedAt().getTime());
        Assert.assertNotNull(result.getUpdatedAt());
        Assert.assertTrue(result.getUpdatedAt().after(createdActionDTO.getUpdatedAt()));
        Assert.assertEquals(result.getEndpoint().getUri(), createdActionDTO.getEndpoint().getUri());

        Authentication resultAuthentication = result.getEndpoint().getAuthentication();
        Assert.assertEquals(resultAuthentication.getType(),
                createdActionDTO.getEndpoint().getAuthentication().getType());
        Assert.assertEquals(resultAuthentication.getProperties().size(),
                createdActionDTO.getEndpoint().getAuthentication().getProperties().size());

        Assert.assertEquals(result.getProperties().size(), updatingAction.getProperties().size());
        Assert.assertEquals(result.getPropertyValue(TestUtil.TEST_ACTION_PROPERTY_NAME_1),
                updatingAction.getPropertyValue(TestUtil.TEST_ACTION_PROPERTY_NAME_1));
        Assert.assertEquals(result.getPropertyValue(TestUtil.TEST_ACTION_PROPERTY_NAME_2),
                updatingAction.getPropertyValue(TestUtil.TEST_ACTION_PROPERTY_NAME_2));
        createdActionDTO = result;
    }

    @Test(priority = 15)
    public void testActivateAction() throws ActionMgtException {

        Assert.assertEquals(createdActionDTO.getStatus(), Action.Status.INACTIVE);
        ActionDTO activatedActionDTO = daoImpl.activateAction(PRE_ISSUE_ACCESS_TOKEN_TYPE, createdActionDTO.getId(),
                TENANT_ID);
        Assert.assertEquals(activatedActionDTO.getStatus(), Action.Status.ACTIVE);
        Assert.assertNotNull(activatedActionDTO.getCreatedAt());
        Assert.assertEquals(activatedActionDTO.getCreatedAt().getTime(), createdActionDTO.getCreatedAt().getTime());
        Assert.assertNotNull(activatedActionDTO.getUpdatedAt());
        Assert.assertTrue(activatedActionDTO.getUpdatedAt().after(createdActionDTO.getUpdatedAt()));
        createdActionDTO = activatedActionDTO;
    }

    @Test(priority = 16)
    public void testDeactivateAction() throws ActionMgtException {

        Assert.assertEquals(createdActionDTO.getStatus(), Action.Status.ACTIVE);
        ActionDTO deactivatedActionDTO = daoImpl.deactivateAction(PRE_ISSUE_ACCESS_TOKEN_TYPE, createdActionDTO.getId(),
                TENANT_ID);
        Assert.assertEquals(deactivatedActionDTO.getStatus(), Action.Status.INACTIVE);
        Assert.assertNotNull(deactivatedActionDTO.getCreatedAt());
        Assert.assertEquals(deactivatedActionDTO.getCreatedAt().getTime(), createdActionDTO.getCreatedAt().getTime());
        Assert.assertNotNull(deactivatedActionDTO.getUpdatedAt());
        Assert.assertTrue(deactivatedActionDTO.getUpdatedAt().after(createdActionDTO.getUpdatedAt()));
    }

    @Test(priority = 17)
    public void testGetActionsCountPerType() throws ActionMgtException {

        ActionDTO creatingPreUpdatePasswordActionDTO = new ActionDTOBuilder()
                .id(PRE_UPDATE_PASSWORD_ACTION_ID)
                .type(Action.ActionTypes.PRE_UPDATE_PASSWORD)
                .name(TestUtil.TEST_ACTION_NAME)
                .status(Action.Status.INACTIVE)
                .actionVersion(TestUtil.TEST_DEFAULT_LATEST_ACTION_VERSION)
                .endpoint(new EndpointConfig.EndpointConfigBuilder()
                        .uri(TestUtil.TEST_ACTION_URI)
                        .authentication(new Authentication.NoneAuthBuilder().build())
                        .build())
                .build();

        daoImpl.addAction(creatingPreUpdatePasswordActionDTO, TENANT_ID);
        ActionDTO createdPreUpdatePasswordActionDTO = daoImpl.getActionByActionId(PRE_UPDATE_PASSWORD_TYPE,
                PRE_UPDATE_PASSWORD_ACTION_ID, TENANT_ID);

        Map<String, Integer> actionMap = daoImpl.getActionsCountPerType(TENANT_ID);
        Assert.assertTrue(actionMap.containsKey(PRE_ISSUE_ACCESS_TOKEN_TYPE));
        Assert.assertEquals(actionMap.get(PRE_ISSUE_ACCESS_TOKEN_TYPE).intValue(), 1);
        Assert.assertTrue(actionMap.containsKey(PRE_UPDATE_PASSWORD_TYPE));
        Assert.assertEquals(actionMap.get(PRE_UPDATE_PASSWORD_TYPE).intValue(), 1);

        daoImpl.deleteAction(createdPreUpdatePasswordActionDTO, TENANT_ID);
        daoImpl.deleteAction(createdActionDTO, TENANT_ID);
    }

    @Test(priority = 18)
    public void testAddPreUpdateProfileAction() throws Exception {

        ActionDTO creatingPreUpdateProfileActionDTO = new ActionDTOBuilder()
                .id(PRE_UPDATE_PROFILE_ACTION_ID)
                .type(Action.ActionTypes.PRE_UPDATE_PROFILE)
                .name(TestUtil.TEST_ACTION_NAME)
                .status(Action.Status.INACTIVE)
                .actionVersion(TestUtil.TEST_DEFAULT_LATEST_ACTION_VERSION)
                .endpoint(new EndpointConfig.EndpointConfigBuilder()
                        .uri(TestUtil.TEST_ACTION_URI)
                        .authentication(new Authentication.NoneAuthBuilder().build())
                        .build())
                .property(TestUtil.TEST_ACTION_PROPERTY_NAME_1,
                        new ActionProperty.BuilderForDAO(BinaryObject.fromJsonString(buildJsonString(
                                TestUtil.TEST_ACTION_OBJECT_PROPERTY_VALUE))).build())
                .build();

        daoImpl.addAction(creatingPreUpdateProfileActionDTO, TENANT_ID);
        ActionDTO createdPreUpdateProfileActionDTO = daoImpl.getActionByActionId(PRE_UPDATE_PROFILE_TYPE,
                PRE_UPDATE_PROFILE_ACTION_ID, TENANT_ID);

        Map<String, Integer> actionMap = daoImpl.getActionsCountPerType(TENANT_ID);
        Assert.assertTrue(actionMap.containsKey(PRE_UPDATE_PROFILE_TYPE));
        Assert.assertEquals(actionMap.get(PRE_UPDATE_PROFILE_TYPE).intValue(), 1);
        Assert.assertTrue(actionMap.containsKey(PRE_UPDATE_PROFILE_TYPE));
        Assert.assertEquals(actionMap.get(PRE_UPDATE_PROFILE_TYPE).intValue(), 1);

        daoImpl.deleteAction(createdPreUpdateProfileActionDTO, TENANT_ID);
        daoImpl.deleteAction(createdActionDTO, TENANT_ID);
    }

    private String buildJsonString(List<String> value) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(value);
    }
}
