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
import org.wso2.carbon.identity.action.management.api.model.ActionProperty;
import org.wso2.carbon.identity.action.management.api.model.Authentication;
import org.wso2.carbon.identity.action.management.api.model.EndpointConfig;
import org.wso2.carbon.identity.user.pre.update.profile.action.api.model.PreUpdateProfileAction;
import org.wso2.carbon.identity.user.pre.update.profile.action.internal.management.PreUpdateProfileActionConverter;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.ATTRIBUTES;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.TEST_ACTION;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.TEST_ATTRIBUTES;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.TEST_DESCRIPTION;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.TEST_ID;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.TEST_PASSWORD;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.TEST_URL;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.TEST_USERNAME;

/**
 * Unit tests for PreUpdateProfileActionConverter.
 */
public class PreUpdateProfileActionConverterTest {

    private PreUpdateProfileActionConverter converter;

    private PreUpdateProfileAction action;

    @BeforeClass
    public void init() {

        converter = new PreUpdateProfileActionConverter();
        action = new PreUpdateProfileAction.ResponseBuilder()
                .id(TEST_ID)
                .type(Action.ActionTypes.PRE_UPDATE_PROFILE)
                .name(TEST_ACTION)
                .description(TEST_DESCRIPTION)
                .status(Action.Status.ACTIVE)
                .createdAt(new Timestamp(new Date().getTime()))
                .updatedAt(new Timestamp(new Date().getTime() + 5000))
                .endpoint(new EndpointConfig.EndpointConfigBuilder()
                        .uri(TEST_URL)
                        .authentication(new Authentication
                                .BasicAuthBuilder(TEST_USERNAME, TEST_PASSWORD).build())
                        .build())
                .attributes(TEST_ATTRIBUTES)
                .build();
    }

    @Test(description = "Test getSupportedActionType returns PRE_UPDATE_PROFILE")
    public void testGetSupportedActionType() {
        assertEquals(converter.getSupportedActionType(), Action.ActionTypes.PRE_UPDATE_PROFILE,
                "Supported action type should be PRE_UPDATE_PROFILE");
    }

    @Test(description = "Test ActionConverter returns action dto ")
    public void testBuildActionDTOForGetOperationWithAllAttributes() {

        // Convert the action to DTO
        ActionDTO dto = converter.buildActionDTO(action);

        // Verify basic fields
        assertEquals(dto.getId(), action.getId());
        assertEquals(dto.getType(), action.getType());
        assertEquals(dto.getName(), action.getName());
        assertEquals(dto.getDescription(), action.getDescription());
        assertEquals(dto.getStatus(), action.getStatus());
        assertEquals(dto.getCreatedAt(), action.getCreatedAt());
        assertEquals(dto.getUpdatedAt(), action.getUpdatedAt());
        assertEquals(dto.getEndpoint().getUri(), action.getEndpoint().getUri());
        assertEquals(dto.getEndpoint().getAuthentication().getType(),
                action.getEndpoint().getAuthentication().getType());
        assertEquals(dto.getEndpoint().getAuthentication().getProperty(Authentication.Property.USERNAME),
                action.getEndpoint().getAuthentication().getProperty(Authentication.Property.USERNAME));
        assertEquals(dto.getEndpoint().getAuthentication().getProperty(Authentication.Property.PASSWORD),
                action.getEndpoint().getAuthentication().getProperty(Authentication.Property.PASSWORD));

        // Verify properties map
        Map<String, ActionProperty> properties = dto.getProperties();
        assertNotNull(properties);
        assertEquals(properties.get(ATTRIBUTES).getValue(), action.getAttributes());
    }

    @Test(description = "Test ActionConverter returns action dto with all the properties ")
    public void testBuildActionForGetOperationWithAllAttributes() {

        Map<String, ActionProperty> properties = new HashMap<>();
        properties.put(ATTRIBUTES, new ActionProperty.BuilderForService(TEST_ATTRIBUTES).build());
        ActionDTO dto = new ActionDTO.Builder(action)
                .properties(properties)
                .build();

        // Convert to Action object
        PreUpdateProfileAction convertedAction = (PreUpdateProfileAction) converter.buildAction(dto);

        // Verify basic fields
        assertEquals(convertedAction.getId(), dto.getId());
        assertEquals(convertedAction.getType(), dto.getType());
        assertEquals(convertedAction.getName(), dto.getName());
        assertEquals(convertedAction.getDescription(), dto.getDescription());
        assertEquals(convertedAction.getStatus(), dto.getStatus());
        assertEquals(convertedAction.getCreatedAt(), dto.getCreatedAt());
        assertEquals(convertedAction.getUpdatedAt(), dto.getUpdatedAt());
        assertEquals(convertedAction.getEndpoint().getUri(), dto.getEndpoint().getUri());
        assertEquals(convertedAction.getEndpoint().getAuthentication().getType(),
                dto.getEndpoint().getAuthentication().getType());
        assertEquals(convertedAction.getEndpoint().getAuthentication().getProperty(Authentication.Property.USERNAME),
                dto.getEndpoint().getAuthentication().getProperty(Authentication.Property.USERNAME));
        assertEquals(convertedAction.getEndpoint().getAuthentication().getProperty(Authentication.Property.PASSWORD),
                dto.getEndpoint().getAuthentication().getProperty(Authentication.Property.PASSWORD));

        // Verify attributes
        List<String> attributes = convertedAction.getAttributes();
        assertNotNull(attributes);
        assertEquals(attributes, TEST_ATTRIBUTES);
    }

    @Test(description = "Test ActionConverter returns action dto with partial properties")
    public void testBuildActionForGetOperationWithPartialAttributes() {

        Action dummyAction = new Action.ActionRequestBuilder().name(TEST_ACTION).build();

        ActionDTO dto = new ActionDTO.Builder(dummyAction)
                .properties(new HashMap<String, ActionProperty>() {{
                    put(ATTRIBUTES, new ActionProperty.BuilderForService(TEST_ATTRIBUTES).build());
                }}).build();
        PreUpdateProfileAction convertedAction = (PreUpdateProfileAction) converter.buildAction(dto);
        assertNotNull(convertedAction.getAttributes());
        assertEquals(convertedAction.getAttributes(), TEST_ATTRIBUTES);
    }

    @Test(description = "Test buildAction with empty properties")
    public void testBuildActionForGetOperationDTOWithPartialAttributes() {

        PreUpdateProfileAction profileAction = new PreUpdateProfileAction.ResponseBuilder()
                .name(TEST_ACTION)
                .attributes(null)
                .build();
        ActionDTO dto = converter.buildActionDTO(profileAction);
        assertNotNull(dto.getProperties());
        assertEquals(dto.getProperties().size(), 0);

        profileAction = new PreUpdateProfileAction.ResponseBuilder()
                .name(TEST_ACTION)
                .attributes(TEST_ATTRIBUTES)
                .build();
        dto = converter.buildActionDTO(profileAction);
        assertNotNull(dto.getProperties());
        assertEquals(dto.getProperties().size(), 1);
        assertEquals(dto.getProperties().get(ATTRIBUTES).getValue(), TEST_ATTRIBUTES);
    }
}
