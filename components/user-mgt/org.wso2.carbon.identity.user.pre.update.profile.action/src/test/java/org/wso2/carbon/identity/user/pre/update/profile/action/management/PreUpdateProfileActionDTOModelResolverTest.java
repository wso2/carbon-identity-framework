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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.management.api.exception.ActionDTOModelResolverClientException;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.model.ActionDTO;
import org.wso2.carbon.identity.action.management.api.model.ActionProperty;
import org.wso2.carbon.identity.action.management.api.model.Authentication;
import org.wso2.carbon.identity.action.management.api.model.BinaryObject;
import org.wso2.carbon.identity.action.management.api.model.EndpointConfig;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.user.pre.update.profile.action.internal.component.PreUpdateProfileActionServiceComponentHolder;
import org.wso2.carbon.identity.user.pre.update.profile.action.internal.management.PreUpdateProfileActionDTOModelResolver;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.ATTRIBUTES;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.DUPLICATED_TEST_ATTRIBUTES;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.INVALID_TEST_ATTRIBUTES;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.INVALID_TEST_ATTRIBUTES_COUNT;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.INVALID_TEST_ATTRIBUTES_TYPE;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.INVALID_TEST_ATTRIBUTES_VALUES;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.ROLES_CLAIM_ATTRIBUTE;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.ROLE_CLAIM_URI;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.SAMPLE_LOCAL_CLAIM_URI_1;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.SAMPLE_LOCAL_CLAIM_URI_2;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.SAMPLE_LOCAL_CLAIM_URI_3;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.SAMPLE_LOCAL_CLAIM_URI_4;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.TENANT_DOMAIN;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.TEST_ACTION;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.TEST_ATTRIBUTES;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.TEST_DESCRIPTION;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.TEST_EMPTY_ATTRIBUTES;
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
    private AutoCloseable closeable;
    @Mock
    private ClaimMetadataManagementService claimMetadataManagementService;
    @Mock
    private LocalClaim localClaim1;
    @Mock
    private LocalClaim localClaim2;
    @Mock
    private LocalClaim localClaim3;
    @Mock
    private LocalClaim localClaim4;
    @Mock
    private LocalClaim rolesClaim;

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

        Map<String, ActionProperty> properties = new HashMap<>();
        properties.put(ATTRIBUTES, new ActionProperty.BuilderForService(TEST_ATTRIBUTES).build());
        existingActionDTO = new ActionDTO.Builder(action).properties(properties).build();
        existingActionDTOWithoutProperties = new ActionDTO.Builder(action).build();
    }

    @BeforeMethod
    public void setUp() throws Exception {

        closeable = MockitoAnnotations.openMocks(this);
        PreUpdateProfileActionServiceComponentHolder.getInstance()
                .setClaimManagementService(claimMetadataManagementService);
        List<LocalClaim> mockLocalClaims = Arrays.asList(localClaim1, localClaim2, localClaim3, localClaim4,
                rolesClaim);
        doReturn(SAMPLE_LOCAL_CLAIM_URI_1).when(localClaim1).getClaimURI();
        doReturn(SAMPLE_LOCAL_CLAIM_URI_2).when(localClaim2).getClaimURI();
        doReturn(SAMPLE_LOCAL_CLAIM_URI_3).when(localClaim3).getClaimURI();
        doReturn(SAMPLE_LOCAL_CLAIM_URI_4).when(localClaim4).getClaimURI();
        doReturn(ROLE_CLAIM_URI).when(rolesClaim).getClaimURI();
        doReturn(mockLocalClaims).when(claimMetadataManagementService).getLocalClaims(anyString());
    }

    @AfterMethod
    public void teardown() throws Exception {

        closeable.close();
    }

    @Test
    public void testGetSupportedActionType() {

        Action.ActionTypes actionType = resolver.getSupportedActionType();
        assertEquals(actionType, Action.ActionTypes.PRE_UPDATE_PROFILE);
    }

    @Test
    public void testResolveForAddOperation() throws Exception {

        Map<String, ActionProperty> properties = new HashMap<>();
        properties.put(ATTRIBUTES, new ActionProperty.BuilderForService(TEST_ATTRIBUTES).build());
        ActionDTO actionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();

        ActionDTO result = resolver.resolveForAddOperation(actionDTO, TENANT_DOMAIN);

        assertNotNull(result);
        verifyCommonFields(actionDTO, result);
        assertTrue(result.getPropertyValue(ATTRIBUTES) instanceof BinaryObject);
        assertNotNull(((BinaryObject) result.getPropertyValue(ATTRIBUTES)).getJSONString());
        assertEquals(getAttributes(((BinaryObject) result.getPropertyValue(ATTRIBUTES)).getJSONString()),
                TEST_ATTRIBUTES);
    }

    @Test
    public void testResolveForAddOperationWithoutAttributes() throws Exception {

        Map<String, ActionProperty> properties = new HashMap<>();
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

        Map<String, ActionProperty> properties = new HashMap<>();
        properties.put(ATTRIBUTES, new ActionProperty.BuilderForService(INVALID_TEST_ATTRIBUTES_TYPE).build());
        ActionDTO actionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();
        resolver.resolveForAddOperation(actionDTO, TENANT_DOMAIN);
    }

    @Test(expectedExceptions = ActionDTOModelResolverClientException.class,
            expectedExceptionsMessageRegExp = "Invalid attributes format.")
    public void testResolveForAddOperationWithInvalidAttributesValueType() throws Exception {

        Map<String, ActionProperty> properties = new HashMap<>();
        properties.put(ATTRIBUTES, new ActionProperty.BuilderForService(INVALID_TEST_ATTRIBUTES_VALUES).build());
        ActionDTO actionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();
        resolver.resolveForAddOperation(actionDTO, TENANT_DOMAIN);
    }

    @Test(expectedExceptions = ActionDTOModelResolverClientException.class,
            expectedExceptionsMessageRegExp = "Maximum attributes limit exceeded.")
    public void testResolveForAddOperationWithExceededAttributesCount() throws Exception {

        Map<String, ActionProperty> properties = new HashMap<>();
        properties.put(ATTRIBUTES, new ActionProperty.BuilderForService(INVALID_TEST_ATTRIBUTES_COUNT).build());
        ActionDTO actionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();
        resolver.resolveForAddOperation(actionDTO, TENANT_DOMAIN);
    }

    @Test
    public void testResolveForAddOperationWithDuplicatedAttributes() throws Exception {

        Map<String, ActionProperty> properties = new HashMap<>();
        properties.put(ATTRIBUTES, new ActionProperty.BuilderForService(DUPLICATED_TEST_ATTRIBUTES).build());
        ActionDTO actionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();
        ActionDTO result = resolver.resolveForAddOperation(actionDTO, TENANT_DOMAIN);

        assertNotNull(result);
        verifyCommonFields(actionDTO, result);
        assertTrue(result.getPropertyValue(ATTRIBUTES) instanceof BinaryObject);
        assertNotNull(((BinaryObject) result.getPropertyValue(ATTRIBUTES)).getInputStream());
        assertEquals(getAttributes(((BinaryObject) result.getPropertyValue(ATTRIBUTES))
                .getJSONString()), Collections.singletonList(DUPLICATED_TEST_ATTRIBUTES.get(0)));
    }

    @Test(expectedExceptions = ActionDTOModelResolverClientException.class,
            expectedExceptionsMessageRegExp = "Invalid attribute provided.")
    public void testResolveForAddOperationWithInvalidAttributes() throws Exception {

        Map<String, ActionProperty> properties = new HashMap<>();
        properties.put(ATTRIBUTES, new ActionProperty.BuilderForService(INVALID_TEST_ATTRIBUTES).build());
        ActionDTO actionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();
        resolver.resolveForAddOperation(actionDTO, TENANT_DOMAIN);
    }

    @Test(expectedExceptions = ActionDTOModelResolverClientException.class,
            expectedExceptionsMessageRegExp = "Not supported.")
    public void testResolveForAddOperationWithRoleAttribute() throws Exception {

        Map<String, ActionProperty> properties = new HashMap<>();
        properties.put(ATTRIBUTES, new ActionProperty.BuilderForService(ROLES_CLAIM_ATTRIBUTE).build());
        ActionDTO actionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();
        resolver.resolveForAddOperation(actionDTO, TENANT_DOMAIN);
    }

    @Test
    public void testResolveForGetOperation() throws Exception {

        Map<String, ActionProperty> properties = new HashMap<>();

        properties.put(ATTRIBUTES, new ActionProperty.BuilderForDAO(getBinaryObject(TEST_ATTRIBUTES)).build());
        ActionDTO actionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();

        ActionDTO result = resolver.resolveForGetOperation(actionDTO, TENANT_DOMAIN);

        assertNotNull(result);
        verifyCommonFields(actionDTO, result);
        assertEquals(result.getPropertyValue(ATTRIBUTES), TEST_ATTRIBUTES);
    }

    @Test
    public void testResolveForGetOperationForActionList() throws Exception {

        Map<String, ActionProperty> properties = new HashMap<>();
        properties.put(ATTRIBUTES, new ActionProperty.BuilderForDAO(getBinaryObject(TEST_ATTRIBUTES)).build());
        ActionDTO actionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();

        List<ActionDTO> result = resolver.resolveForGetOperation(Collections.singletonList(actionDTO), TENANT_DOMAIN);

        assertNotNull(result);
        for (ActionDTO dto : result) {
            verifyCommonFields(actionDTO, dto);
            assertEquals(dto.getPropertyValue(ATTRIBUTES), TEST_ATTRIBUTES);
        }
    }

    @Test
    public void testResolveForUpdateOperationWithExistingAttributes() throws Exception {

        Map<String, ActionProperty> properties = new HashMap<>();
        properties.put(ATTRIBUTES, new ActionProperty.BuilderForService(UPDATED_TEST_ATTRIBUTES).build());
        ActionDTO updatingActionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();
        ActionDTO result = resolver.resolveForUpdateOperation(updatingActionDTO, existingActionDTO, TENANT_DOMAIN);

        assertNotNull(result);
        verifyCommonFields(updatingActionDTO, result);
        assertEquals(result.getProperties().size(), 1);
        assertTrue(result.getPropertyValue(ATTRIBUTES) instanceof BinaryObject);
        assertNotNull(((BinaryObject) result.getPropertyValue(ATTRIBUTES)).getJSONString());
        assertEquals(getAttributes(((BinaryObject) result.getPropertyValue(ATTRIBUTES)).getJSONString()),
                UPDATED_TEST_ATTRIBUTES);
    }

    @Test
    public void testResolveForUpdateOperationToDeleteAttributes() throws Exception {

        Map<String, ActionProperty> properties = new HashMap<>();
        properties.put(ATTRIBUTES, new ActionProperty.BuilderForService(TEST_EMPTY_ATTRIBUTES).build());
        ActionDTO updatingActionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();
        ActionDTO result = resolver.resolveForUpdateOperation(updatingActionDTO, existingActionDTO, TENANT_DOMAIN);

        assertNotNull(result);
        verifyCommonFields(updatingActionDTO, result);
        assertEquals(result.getProperties().size(), 0);
        assertNull(result.getPropertyValue(ATTRIBUTES));
    }

    @Test
    public void testResolveForUpdateOperationWithUpdatingNullAttributes() throws Exception {

        Map<String, ActionProperty> properties = new HashMap<>();
        properties.put(ATTRIBUTES, new ActionProperty.BuilderForService(null).build());
        ActionDTO updatingActionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();
        ActionDTO result = resolver.resolveForUpdateOperation(updatingActionDTO, existingActionDTO, TENANT_DOMAIN);

        assertNotNull(result);
        verifyCommonFields(updatingActionDTO, result);
        // Since no attributes are updated, the existing attributes in ActionDTO should be verified.
        assertEquals(result.getProperties().size(), existingActionDTO.getProperties().size());
        assertTrue(result.getPropertyValue(ATTRIBUTES) instanceof BinaryObject);
        assertNotNull(((BinaryObject) result.getPropertyValue(ATTRIBUTES)).getJSONString());
        assertEquals(getAttributes(((BinaryObject) result.getPropertyValue(ATTRIBUTES))
                .getJSONString()), existingActionDTO.getPropertyValue(ATTRIBUTES));
    }

    @Test(expectedExceptions = ActionDTOModelResolverClientException.class,
            expectedExceptionsMessageRegExp = "Maximum attributes limit exceeded.")
    public void testResolveForUpdateOperationWithExceededAttributesCountWithExistingAttributes() throws Exception {

        Map<String, ActionProperty> properties = new HashMap<>();
        properties.put(ATTRIBUTES, new ActionProperty.BuilderForService(INVALID_TEST_ATTRIBUTES_COUNT).build());
        ActionDTO actionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();
        resolver.resolveForUpdateOperation(actionDTO, existingActionDTO, TENANT_DOMAIN);
    }

    @Test
    public void testResolveForUpdateOperationWithDuplicatedAttributesWithExistingAttributes() throws Exception {

        Map<String, ActionProperty> properties = new HashMap<>();
        properties.put(ATTRIBUTES, new ActionProperty.BuilderForService(DUPLICATED_TEST_ATTRIBUTES).build());
        ActionDTO actionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();
        ActionDTO result = resolver.resolveForUpdateOperation(actionDTO, existingActionDTO, TENANT_DOMAIN);

        assertNotNull(result);
        verifyCommonFields(actionDTO, result);
        assertTrue(result.getPropertyValue(ATTRIBUTES) instanceof BinaryObject);
        assertNotNull(((BinaryObject) result.getPropertyValue(ATTRIBUTES)).getInputStream());
        assertEquals(getAttributes(((BinaryObject) result.getPropertyValue(ATTRIBUTES)).getJSONString()),
                Collections.singletonList(DUPLICATED_TEST_ATTRIBUTES.get(0)));
    }

    @Test(expectedExceptions = ActionDTOModelResolverClientException.class,
            expectedExceptionsMessageRegExp = "Invalid attribute provided.")
    public void testResolveForUpdateOperationWithInvalidAttributesWithExistingAttributes() throws Exception {

        Map<String, ActionProperty> properties = new HashMap<>();
        properties.put(ATTRIBUTES, new ActionProperty.BuilderForService(INVALID_TEST_ATTRIBUTES).build());
        ActionDTO actionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();
        resolver.resolveForUpdateOperation(actionDTO, existingActionDTO, TENANT_DOMAIN);
    }

    @Test(expectedExceptions = ActionDTOModelResolverClientException.class,
            expectedExceptionsMessageRegExp = "Not supported.")
    public void testResolveForUpdateOperationWithRoleAttributeWithExistingAttributes() throws Exception {

        Map<String, ActionProperty> properties = new HashMap<>();
        properties.put(ATTRIBUTES, new ActionProperty.BuilderForService(ROLES_CLAIM_ATTRIBUTE).build());
        ActionDTO actionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();
        resolver.resolveForUpdateOperation(actionDTO, existingActionDTO, TENANT_DOMAIN);
    }

    @Test
    public void testResolveUpdateOperationWithoutUpdatingAttributesWithExistingAttributes() throws Exception {

        Map<String, ActionProperty> properties = new HashMap<>();
        ActionDTO updatingActionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();

        ActionDTO result = resolver.resolveForUpdateOperation(updatingActionDTO, existingActionDTO, TENANT_DOMAIN);
        assertNotNull(result);
        verifyCommonFields(updatingActionDTO, result);
        // Since no attributes are updated, the existing attributes in ActionDTO should be verified.
        assertEquals(result.getProperties().size(), existingActionDTO.getProperties().size());
        assertTrue(result.getPropertyValue(ATTRIBUTES) instanceof BinaryObject);
        assertNotNull(((BinaryObject) result.getPropertyValue(ATTRIBUTES)).getJSONString());
        assertEquals(getAttributes(((BinaryObject) result.getPropertyValue(ATTRIBUTES)).getJSONString()),
                existingActionDTO.getPropertyValue(ATTRIBUTES));
    }

    @Test
    public void testResolveForUpdateOperationWithoutExistingAttributes() throws Exception {

        Map<String, ActionProperty> properties = new HashMap<>();
        properties.put(ATTRIBUTES, new ActionProperty.BuilderForService(TEST_ATTRIBUTES).build());
        ActionDTO updatingActionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();
        ActionDTO result = resolver.resolveForUpdateOperation(updatingActionDTO, existingActionDTOWithoutProperties,
                TENANT_DOMAIN);

        assertNotNull(result);
        verifyCommonFields(updatingActionDTO, result);
        assertEquals(result.getProperties().size(), 1);
        assertTrue(result.getPropertyValue(ATTRIBUTES) instanceof BinaryObject);
        assertNotNull(((BinaryObject) result.getPropertyValue(ATTRIBUTES)).getJSONString());
        assertEquals(getAttributes(((BinaryObject) result.getPropertyValue(ATTRIBUTES)).getJSONString()),
                TEST_ATTRIBUTES);
    }

    @Test
    public void testResolveUpdateOperationWithoutBothUpdatingAndExistingAttributes() throws Exception {

        Map<String, ActionProperty> properties = new HashMap<>();
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
        assertEquals(result.getStatus(), actionDTO.getStatus());
        assertEquals(result.getCreatedAt(), actionDTO.getCreatedAt());
        assertEquals(result.getUpdatedAt(), actionDTO.getUpdatedAt());
        assertEquals(result.getEndpoint().getUri(), actionDTO.getEndpoint().getUri());
        assertEquals(result.getEndpoint().getAuthentication().getType(),
                actionDTO.getEndpoint().getAuthentication().getType());
        assertEquals(result.getEndpoint().getAuthentication().getProperty(Authentication.Property.USERNAME),
                actionDTO.getEndpoint().getAuthentication().getProperty(Authentication.Property.USERNAME));
        assertEquals(result.getEndpoint().getAuthentication().getProperty(Authentication.Property.PASSWORD),
                actionDTO.getEndpoint().getAuthentication().getProperty(Authentication.Property.PASSWORD));
    }

    private BinaryObject getBinaryObject(List<String> attributes) throws Exception {

        return BinaryObject.fromInputStream(getInputStream(attributes));
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

    private List<String> getAttributes(String value) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(value, new TypeReference<List<String>>() { });
    }
}
