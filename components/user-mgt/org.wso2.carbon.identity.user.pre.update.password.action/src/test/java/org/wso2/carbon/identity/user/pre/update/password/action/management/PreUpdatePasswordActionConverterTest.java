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

package org.wso2.carbon.identity.user.pre.update.password.action.management;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.model.ActionDTO;
import org.wso2.carbon.identity.action.management.api.model.ActionProperty;
import org.wso2.carbon.identity.action.management.api.model.ActionPropertyForService;
import org.wso2.carbon.identity.action.management.api.model.Authentication;
import org.wso2.carbon.identity.action.management.api.model.EndpointConfig;
import org.wso2.carbon.identity.certificate.management.model.Certificate;
import org.wso2.carbon.identity.user.pre.update.password.action.api.model.PasswordSharing;
import org.wso2.carbon.identity.user.pre.update.password.action.api.model.PreUpdatePasswordAction;
import org.wso2.carbon.identity.user.pre.update.password.action.internal.management.PreUpdatePasswordActionConverter;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.identity.user.pre.update.password.action.internal.constant.PreUpdatePasswordActionConstants.CERTIFICATE;
import static org.wso2.carbon.identity.user.pre.update.password.action.internal.constant.PreUpdatePasswordActionConstants.PASSWORD_SHARING_FORMAT;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_ACTION;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_DESCRIPTION;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_ID;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_PASSWORD;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_URL;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_USERNAME;

/**
 * Unit tests for PreUpdatePasswordActionConverter.
 */
public class PreUpdatePasswordActionConverterTest {

    private PreUpdatePasswordActionConverter converter;

    private Certificate mockCertificate;
    private PreUpdatePasswordAction action;

    @BeforeClass
    public void init() {

        mockCertificate = mock(Certificate.class);
        converter = new PreUpdatePasswordActionConverter();
        action = new PreUpdatePasswordAction.ResponseBuilder()
                .id(TEST_ID)
                .type(Action.ActionTypes.PRE_UPDATE_PASSWORD)
                .name(TEST_ACTION)
                .description(TEST_DESCRIPTION)
                .status(Action.Status.ACTIVE)
                .endpoint(new EndpointConfig.EndpointConfigBuilder()
                        .uri(TEST_URL)
                        .authentication(new Authentication
                                .BasicAuthBuilder(TEST_USERNAME, TEST_PASSWORD).build())
                        .build())
                .passwordSharing(new PasswordSharing.Builder()
                        .format(PasswordSharing.Format.SHA256_HASHED)
                        .certificate(mockCertificate)
                        .build())
                .build();
    }

    @Test(description = "Test getSupportedActionType returns PRE_UPDATE_PASSWORD")
    public void testGetSupportedActionType() {
        assertEquals(converter.getSupportedActionType(), Action.ActionTypes.PRE_UPDATE_PASSWORD,
                "Supported action type should be PRE_UPDATE_PASSWORD");
    }

    @Test
    public void testBuildActionForGetOperationDTOWithAllAttributes() {

        // Convert the action to DTO
        ActionDTO dto = converter.buildActionDTO(action);

        // Verify basic fields
        assertEquals(dto.getId(), action.getId());
        assertEquals(dto.getType(), action.getType());
        assertEquals(dto.getName(), action.getName());
        assertEquals(dto.getDescription(), action.getDescription());
        assertEquals(dto.getStatus(), action.getStatus());
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
        assertTrue(properties.get(PASSWORD_SHARING_FORMAT) instanceof ActionPropertyForService);
        assertTrue(properties.get(CERTIFICATE) instanceof ActionPropertyForService);
        assertEquals(properties.get(PASSWORD_SHARING_FORMAT).getValue(), action.getPasswordSharing().getFormat());
        assertEquals(properties.get(CERTIFICATE).getValue(), mockCertificate);
    }

    @Test
    public void testBuildActionForGetOperationWithAllAttributes() {

        Map<String, ActionPropertyForService> properties = new HashMap<>();
        properties.put(PASSWORD_SHARING_FORMAT, new ActionPropertyForService(PasswordSharing.Format.SHA256_HASHED));
        properties.put(CERTIFICATE, new ActionPropertyForService(mockCertificate));
        ActionDTO dto = new ActionDTO.BuilderForService(action)
                .properties(properties)
                .build();

        // Convert to Action object
        PreUpdatePasswordAction convertedAction = (PreUpdatePasswordAction) converter.buildAction(dto);

        // Verify basic fields
        assertEquals(convertedAction.getId(), dto.getId());
        assertEquals(convertedAction.getType(), dto.getType());
        assertEquals(convertedAction.getName(), dto.getName());
        assertEquals(convertedAction.getDescription(), dto.getDescription());
        assertEquals(convertedAction.getStatus(), dto.getStatus());
        assertEquals(convertedAction.getEndpoint().getUri(), dto.getEndpoint().getUri());
        assertEquals(convertedAction.getEndpoint().getAuthentication().getType(),
                dto.getEndpoint().getAuthentication().getType());
        assertEquals(convertedAction.getEndpoint().getAuthentication().getProperty(Authentication.Property.USERNAME),
                dto.getEndpoint().getAuthentication().getProperty(Authentication.Property.USERNAME));
        assertEquals(convertedAction.getEndpoint().getAuthentication().getProperty(Authentication.Property.PASSWORD),
                dto.getEndpoint().getAuthentication().getProperty(Authentication.Property.PASSWORD));

        // Verify PasswordSharing object
        PasswordSharing passwordSharing = convertedAction.getPasswordSharing();
        assertNotNull(passwordSharing);
        assertEquals(passwordSharing.getFormat(), PasswordSharing.Format.SHA256_HASHED);
        assertEquals(passwordSharing.getCertificate(), mockCertificate);
    }

    @Test
    public void testBuildActionForGetOperationWithPartialAttributes() {

        Action dummyAction = new Action.ActionRequestBuilder().name(TEST_ACTION).build();

        ActionDTO dto = new ActionDTO.BuilderForService(dummyAction)
                .properties(new HashMap<String, ActionPropertyForService>() {{
                    put(PASSWORD_SHARING_FORMAT, new ActionPropertyForService(PasswordSharing.Format.SHA256_HASHED));
                }}).build();
        PreUpdatePasswordAction convertedAction = (PreUpdatePasswordAction) converter.buildAction(dto);
        assertNotNull(convertedAction.getPasswordSharing());
        assertEquals(convertedAction.getPasswordSharing().getFormat(), PasswordSharing.Format.SHA256_HASHED);
        HashMap<String, ActionPropertyForService> properties = new HashMap<>();
        properties.put(CERTIFICATE, new ActionPropertyForService(mockCertificate));
        properties.put(PASSWORD_SHARING_FORMAT, new ActionPropertyForService(PasswordSharing.Format.SHA256_HASHED));
        dto = new ActionDTO.BuilderForService(dummyAction).properties(properties).build();
        convertedAction = (PreUpdatePasswordAction) converter.buildAction(dto);
        assertNotNull(convertedAction.getPasswordSharing());
        assertEquals(convertedAction.getPasswordSharing().getCertificate(), mockCertificate);
    }

    @Test(description = "Test buildAction with empty properties")
    public void testBuildActionForGetOperationDTOWithPartialAttributes() {

        PreUpdatePasswordAction passwordAction = new PreUpdatePasswordAction.ResponseBuilder()
                .name(TEST_ACTION)
                .passwordSharing(null)
                .build();
        ActionDTO dto = converter.buildActionDTO(passwordAction);
        assertNotNull(dto.getProperties());
        assertEquals(dto.getProperties().size(), 0);

        passwordAction = new PreUpdatePasswordAction.ResponseBuilder()
                .name(TEST_ACTION)
                .passwordSharing(new PasswordSharing.Builder().format(PasswordSharing.Format.SHA256_HASHED).build())
                .build();
        dto = converter.buildActionDTO(passwordAction);
        assertNotNull(dto.getProperties());
        assertEquals(dto.getProperties().size(), 1);
        assertTrue(dto.getProperties().get(PASSWORD_SHARING_FORMAT) instanceof ActionPropertyForService);
        assertEquals(dto.getProperties().get(PASSWORD_SHARING_FORMAT).getValue(), PasswordSharing.Format.SHA256_HASHED);

        passwordAction = new PreUpdatePasswordAction.ResponseBuilder()
                .name(TEST_ACTION)
                .passwordSharing(new PasswordSharing.Builder().certificate(mockCertificate).build())
                .build();
        dto = converter.buildActionDTO(passwordAction);
        assertNotNull(dto.getProperties());
        assertEquals(dto.getProperties().size(), 1);
        assertTrue(dto.getProperties().get(CERTIFICATE) instanceof ActionPropertyForService);
        assertEquals(dto.getProperties().get(CERTIFICATE).getValue(), mockCertificate);
    }
}
