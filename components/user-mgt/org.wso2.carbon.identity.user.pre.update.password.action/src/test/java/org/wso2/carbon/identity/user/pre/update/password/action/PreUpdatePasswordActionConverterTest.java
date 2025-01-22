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

package org.wso2.carbon.identity.user.pre.update.password.action;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.management.model.Action;
import org.wso2.carbon.identity.action.management.model.ActionDTO;
import org.wso2.carbon.identity.action.management.model.Authentication;
import org.wso2.carbon.identity.action.management.model.EndpointConfig;
import org.wso2.carbon.identity.certificate.management.model.Certificate;
import org.wso2.carbon.identity.user.pre.update.password.action.management.PreUpdatePasswordActionConverter;
import org.wso2.carbon.identity.user.pre.update.password.action.model.PasswordSharing;
import org.wso2.carbon.identity.user.pre.update.password.action.model.PreUpdatePasswordAction;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.wso2.carbon.identity.user.pre.update.password.action.constant.PreUpdatePasswordActionConstants.CERTIFICATE;
import static org.wso2.carbon.identity.user.pre.update.password.action.constant.PreUpdatePasswordActionConstants.PASSWORD_SHARING_FORMAT;

/**
 * Unit tests for PreUpdatePasswordActionConverter.
 */
public class PreUpdatePasswordActionConverterTest {

    public static final String TEST_ID = "test-id";
    public static final String TEST_ACTION = "Test Action";
    public static final String TEST_DESCRIPTION = "Test Description";
    public static final String TEST_URL = "https://test.endpoint";
    public static final String TEST_USERNAME = "test-username";
    public static final String TEST_PASSWORD = "test-password";
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
    public void testBuildActionDTOWithAllAttributes() {

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
        Map<String, Object> properties = dto.getProperties();
        assertNotNull(properties);
        assertEquals((String) properties.get(PASSWORD_SHARING_FORMAT), action.getPasswordSharing().getFormat().name());
        assertEquals(properties.get(CERTIFICATE), mockCertificate);
    }

    @Test
    public void testBuildActionWithAllAttributes() {

        Map<String, Object> properties = new HashMap<>();
        properties.put(PASSWORD_SHARING_FORMAT, PasswordSharing.Format.SHA256_HASHED.name());
        properties.put(CERTIFICATE, mockCertificate);
        ActionDTO dto = new ActionDTO.Builder(action)
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
    public void testBuildActionWithPartialAttributes() {

        Action dummyAction = new Action.ActionRequestBuilder().name(TEST_ACTION).build();

        ActionDTO dto = new ActionDTO.Builder(dummyAction)
                .properties(null)
                .build();
        PreUpdatePasswordAction action = (PreUpdatePasswordAction) converter.buildAction(dto);
        assertNull(action.getPasswordSharing());

        dto = new ActionDTO.Builder(dummyAction)
                .properties(new HashMap<String, Object>() {{
                    put(PASSWORD_SHARING_FORMAT, PasswordSharing.Format.SHA256_HASHED.name());
                }}).build();
        action = (PreUpdatePasswordAction) converter.buildAction(dto);
        assertNotNull(action.getPasswordSharing());
        assertEquals(action.getPasswordSharing().getFormat(), PasswordSharing.Format.SHA256_HASHED);

        dto = new ActionDTO.Builder(dummyAction)
                .properties(new HashMap<String, Object>() {{
                    put(CERTIFICATE, mockCertificate);
                }}).build();
        action = (PreUpdatePasswordAction) converter.buildAction(dto);
        assertNotNull(action.getPasswordSharing());
        assertEquals(action.getPasswordSharing().getCertificate(), mockCertificate);
    }

    @Test(description = "Test buildAction with empty properties")
    public void testBuildActionDTOWithPartialAttributes() {

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
        assertEquals(dto.getProperties().get(PASSWORD_SHARING_FORMAT), PasswordSharing.Format.SHA256_HASHED.name());

        passwordAction = new PreUpdatePasswordAction.ResponseBuilder()
                .name(TEST_ACTION)
                .passwordSharing(new PasswordSharing.Builder().certificate(mockCertificate).build())
                .build();
        dto = converter.buildActionDTO(passwordAction);
        assertNotNull(dto.getProperties());
        assertEquals(dto.getProperties().size(), 1);
        assertEquals(dto.getProperties().get(CERTIFICATE), mockCertificate);
    }
}
