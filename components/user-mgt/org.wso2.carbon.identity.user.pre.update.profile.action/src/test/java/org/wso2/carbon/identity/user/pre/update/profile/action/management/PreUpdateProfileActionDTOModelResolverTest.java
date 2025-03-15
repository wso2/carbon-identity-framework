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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.BeforeClass;
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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.ATTRIBUTES;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.INVALID_TEST_ATTRIBUTES;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.INVALID_TEST_ATTRIBUTES_VALUES;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.TENANT_DOMAIN;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.TEST_ACTION;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.TEST_ATTRIBUTES;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.TEST_DESCRIPTION;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.TEST_ID;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.TEST_PASSWORD;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.TEST_URL;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.TEST_USERNAME;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.UPDATED_TEST_ATTRIBUTES;

/**
 * Unit tests for PreUpdateProfileDTOModelResolver.
 */
public class PreUpdateProfileActionDTOModelResolverTest {

    private PreUpdateProfileActionDTOModelResolver resolver;
    private Action action;
    private ActionDTO existingActionDTO;
    private ActionDTO existingActionDTOWithoutProperties;

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
        existingActionDTOWithoutProperties = new ActionDTO.Builder(action).build();
    }

    @Test
    public void testGetSupportedActionType() {

        Action.ActionTypes actionType = resolver.getSupportedActionType();
        assertEquals(actionType, Action.ActionTypes.PRE_UPDATE_PROFILE);
    }

    @Test
    public void testResolveForAddOperation() throws Exception {

        Map<String, Object> properties = new HashMap<>();
        properties.put(ATTRIBUTES, TEST_ATTRIBUTES);
        ActionDTO actionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();

        ActionDTO result = resolver.resolveForAddOperation(actionDTO, TENANT_DOMAIN);

        assertNotNull(result);
        verifyCommonFields(actionDTO, result);
        assertTrue(result.getProperty(ATTRIBUTES) instanceof ActionPropertyForDAO);
        assertTrue(((ActionPropertyForDAO) result.getProperty(ATTRIBUTES)).getValue() instanceof BinaryObject);
        assertNotNull(((BinaryObject) ((ActionPropertyForDAO) result.getProperty(ATTRIBUTES)).getValue())
                .getInputStream());
        assertEquals(getAttributes(((BinaryObject) ((ActionPropertyForDAO) result.getProperty(ATTRIBUTES)).getValue())
                .getInputStream()), TEST_ATTRIBUTES);
    }

    @Test
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

    @Test(expectedExceptions = ActionDTOModelResolverClientException.class,
            expectedExceptionsMessageRegExp = "Invalid attributes format.")
    public void testResolveForAddOperationWithInvalidAttributesFormat() throws Exception {

        Map<String, Object> properties = new HashMap<>();
        properties.put(ATTRIBUTES, INVALID_TEST_ATTRIBUTES);
        ActionDTO actionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();
        resolver.resolveForAddOperation(actionDTO, TENANT_DOMAIN);
    }

    @Test(expectedExceptions = ActionDTOModelResolverClientException.class,
            expectedExceptionsMessageRegExp = "Invalid attributes format.")
    public void testResolveForAddOperationWithInvalidAttributesValueType() throws Exception {

        Map<String, Object> properties = new HashMap<>();
        properties.put(ATTRIBUTES, INVALID_TEST_ATTRIBUTES_VALUES);
        ActionDTO actionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();
        resolver.resolveForAddOperation(actionDTO, TENANT_DOMAIN);
    }

    @Test
    public void testResolveForGetOperation() throws Exception {

        Map<String, Object> properties = new HashMap<>();

        properties.put(ATTRIBUTES, new ActionPropertyForDAO(getBinaryObject(TEST_ATTRIBUTES)));
        ActionDTO actionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();

        ActionDTO result = resolver.resolveForGetOperation(actionDTO, TENANT_DOMAIN);

        assertNotNull(result);
        verifyCommonFields(actionDTO, result);
        assertEquals(result.getProperty(ATTRIBUTES), TEST_ATTRIBUTES);
    }

    @Test(expectedExceptions = ActionDTOModelResolverServerException.class,
            expectedExceptionsMessageRegExp = "Unable to retrieve attributes.")
    public void testResolveForGetOperationWithInvalidPropertyValue() throws Exception {

        Map<String, Object> properties = new HashMap<>();

        properties.put(ATTRIBUTES, TEST_ATTRIBUTES);
        ActionDTO actionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();

        resolver.resolveForGetOperation(actionDTO, TENANT_DOMAIN);
    }

    @Test
    public void testResolveForGetOperationForActionList() throws Exception {

        Map<String, Object> properties = new HashMap<>();
        properties.put(ATTRIBUTES, new ActionPropertyForDAO(getBinaryObject(TEST_ATTRIBUTES)));
        ActionDTO actionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();

        List<ActionDTO> result = resolver.resolveForGetOperation(Collections.singletonList(actionDTO), TENANT_DOMAIN);

        assertNotNull(result);
        for (ActionDTO dto : result) {
            verifyCommonFields(actionDTO, dto);
            assertEquals(dto.getProperty(ATTRIBUTES), TEST_ATTRIBUTES);
        }
    }

    @Test
    public void testResolveForUpdateOperationWithExistingAttributes() throws Exception {

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
        assertTrue(((ActionPropertyForDAO) result.getProperty(ATTRIBUTES)).getValue() instanceof BinaryObject);
        assertNotNull(((BinaryObject) ((ActionPropertyForDAO) result.getProperty(ATTRIBUTES)).getValue())
                .getInputStream());
        assertEquals(getAttributes(((BinaryObject) ((ActionPropertyForDAO) result.getProperty(ATTRIBUTES)).getValue())
                .getInputStream()), UPDATED_TEST_ATTRIBUTES);
    }

    @Test
    public void testResolveUpdateOperationWithoutUpdatingAttributesWithExistingAttributes() throws Exception {

        Map<String, Object> properties = new HashMap<>();
        ActionDTO updatingActionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();

        ActionDTO result = resolver.resolveForUpdateOperation(updatingActionDTO, existingActionDTO, TENANT_DOMAIN);
        assertNotNull(result);
        verifyCommonFields(updatingActionDTO, result);
        // Since no attributes are updated, the existing attributes in ActionDTO should be verified.
        assertEquals(result.getProperties().size(), existingActionDTO.getProperties().size());
        assertTrue(result.getProperty(ATTRIBUTES) instanceof ActionPropertyForDAO);
        assertTrue(((ActionPropertyForDAO) result.getProperty(ATTRIBUTES)).getValue() instanceof BinaryObject);
        assertNotNull(((BinaryObject) ((ActionPropertyForDAO) result.getProperty(ATTRIBUTES)).getValue())
                .getInputStream());
        assertEquals(getAttributes(((BinaryObject) ((ActionPropertyForDAO) result.getProperty(ATTRIBUTES)).getValue())
                .getInputStream()), existingActionDTO.getProperty(ATTRIBUTES));
    }

    @Test
    public void testResolveForUpdateOperationWithoutExistingAttributes() throws Exception {

        Map<String, Object> properties = new HashMap<>();
        properties.put(ATTRIBUTES, TEST_ATTRIBUTES);
        ActionDTO updatingActionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();
        ActionDTO result = resolver.resolveForUpdateOperation(updatingActionDTO, existingActionDTOWithoutProperties,
                TENANT_DOMAIN);

        assertNotNull(result);
        verifyCommonFields(updatingActionDTO, result);
        assertEquals(result.getProperties().size(), 1);
        assertTrue(result.getProperty(ATTRIBUTES) instanceof ActionPropertyForDAO);
        assertTrue(((ActionPropertyForDAO) result.getProperty(ATTRIBUTES)).getValue() instanceof BinaryObject);
        assertNotNull(((BinaryObject) ((ActionPropertyForDAO) result.getProperty(ATTRIBUTES)).getValue())
                .getInputStream());
        assertEquals(getAttributes(((BinaryObject) ((ActionPropertyForDAO) result.getProperty(ATTRIBUTES)).getValue())
                .getInputStream()), TEST_ATTRIBUTES);
    }

    @Test
    public void testResolveUpdateOperationWithoutBothUpdatingAndExistingAttributes() throws Exception {

        Map<String, Object> properties = new HashMap<>();
        ActionDTO updatingActionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();

        ActionDTO result = resolver.resolveForUpdateOperation(updatingActionDTO, existingActionDTOWithoutProperties,
                TENANT_DOMAIN);
        assertNotNull(result);
        verifyCommonFields(updatingActionDTO, result);
        assertEquals(result.getProperties().size(), 0);
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

    private BinaryObject getBinaryObject(List<String> attributes) throws Exception {

        return new BinaryObject(getInputStream(attributes));
    }

    private InputStream getInputStream(List<String> attributes) throws Exception {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            // Convert the attributes to a JSON array and generate the input stream.
            return new ByteArrayInputStream(objectMapper.writeValueAsString(attributes)
                    .getBytes(StandardCharsets.UTF_8));
        } catch (JsonProcessingException e) {
            throw new Exception("Error occurred while converting attributes to input stream.", e);
        }
    }

    private List<String> getAttributes(InputStream stream) throws Exception {

        StringBuilder sb = new StringBuilder();
        List<String> attributes;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            ObjectMapper objectMapper = new ObjectMapper();
            attributes = objectMapper.readValue(sb.toString(), new TypeReference<List<String>>() { });
        } catch (IOException e) {
            throw new Exception("Error while converting InputStream to List<String>.", e);
        }

        return attributes;
    }
}
