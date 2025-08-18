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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.management.api.exception.ActionDTOModelResolverClientException;
import org.wso2.carbon.identity.action.management.api.exception.ActionDTOModelResolverServerException;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.model.ActionDTO;
import org.wso2.carbon.identity.action.management.api.model.ActionProperty;
import org.wso2.carbon.identity.action.management.api.model.Authentication;
import org.wso2.carbon.identity.action.management.api.model.BinaryObject;
import org.wso2.carbon.identity.action.management.api.model.EndpointConfig;
import org.wso2.carbon.identity.certificate.management.exception.CertificateMgtClientException;
import org.wso2.carbon.identity.certificate.management.exception.CertificateMgtException;
import org.wso2.carbon.identity.certificate.management.exception.CertificateMgtServerException;
import org.wso2.carbon.identity.certificate.management.model.Certificate;
import org.wso2.carbon.identity.certificate.management.service.CertificateManagementService;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.user.pre.update.password.action.api.model.PasswordSharing;
import org.wso2.carbon.identity.user.pre.update.password.action.internal.component.PreUpdatePasswordActionServiceComponentHolder;
import org.wso2.carbon.identity.user.pre.update.password.action.internal.management.PreUpdatePasswordActionDTOModelResolver;

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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.identity.user.pre.update.password.action.internal.constant.PreUpdatePasswordActionConstants.ATTRIBUTES;
import static org.wso2.carbon.identity.user.pre.update.password.action.internal.constant.PreUpdatePasswordActionConstants.CERTIFICATE;
import static org.wso2.carbon.identity.user.pre.update.password.action.internal.constant.PreUpdatePasswordActionConstants.PASSWORD_SHARING_FORMAT;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.DUPLICATED_TEST_ATTRIBUTES;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.INVALID_TEST_ATTRIBUTES;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.INVALID_TEST_ATTRIBUTES_COUNT;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.INVALID_TEST_ATTRIBUTES_TYPE;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.INVALID_TEST_ATTRIBUTES_VALUES;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.ROLES_CLAIM_ATTRIBUTE;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.ROLE_CLAIM_URI;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.SAMPLE_LOCAL_CLAIM_URI_1;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.SAMPLE_LOCAL_CLAIM_URI_2;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.SAMPLE_LOCAL_CLAIM_URI_3;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.SAMPLE_LOCAL_CLAIM_URI_4;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TENANT_DOMAIN;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_ACTION;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_ATTRIBUTES;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_CERTIFICATE;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_CERTIFICATE_ID;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_CERTIFICATE_NAME;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_DESCRIPTION;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_EMPTY_ATTRIBUTES;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_ID;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_PASSWORD;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_UPDATED_CERTIFICATE;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_URL;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_USERNAME;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.UPDATED_TEST_ATTRIBUTES;

public class PreUpdatePasswordActionDTOModelResolverTest {

    private PreUpdatePasswordActionDTOModelResolver resolver;
    private Action action;
    private ActionDTO existingActionDTO;
    @Mock
    private CertificateManagementService certificateManagementService;
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
        properties.put(PASSWORD_SHARING_FORMAT,
                new ActionProperty.BuilderForService(PasswordSharing.Format.SHA256_HASHED).build());
        properties.put(CERTIFICATE, new ActionProperty.BuilderForService(new Certificate.Builder()
                .id(TEST_CERTIFICATE_ID).name(TEST_CERTIFICATE_NAME).certificateContent(TEST_CERTIFICATE).build())
                .build());
        properties.put(ATTRIBUTES, new ActionProperty.BuilderForService(TEST_ATTRIBUTES).build());
        existingActionDTO = new ActionDTO.Builder(action).properties(properties).build();
    }

    @BeforeMethod
    public void setUp() throws ClaimMetadataException {

        MockitoAnnotations.openMocks(this);
        resolver = new PreUpdatePasswordActionDTOModelResolver();
        PreUpdatePasswordActionServiceComponentHolder.getInstance()
                .setCertificateManagementService(certificateManagementService);
        PreUpdatePasswordActionServiceComponentHolder.getInstance()
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

    @Test
    public void testGetSupportedActionType() {

        Action.ActionTypes actionType = resolver.getSupportedActionType();
        assertEquals(actionType, Action.ActionTypes.PRE_UPDATE_PASSWORD);
    }

    @Test
    public void testResolveForAddOperation() throws Exception {

        Map<String, ActionProperty> properties = new HashMap<>();
        properties.put(PASSWORD_SHARING_FORMAT,
                new ActionProperty.BuilderForService(PasswordSharing.Format.SHA256_HASHED).build());
        properties.put(CERTIFICATE, new ActionProperty.BuilderForService(new
                Certificate.Builder().certificateContent(TEST_CERTIFICATE).build()).build());
        properties.put(ATTRIBUTES, new ActionProperty.BuilderForService(TEST_ATTRIBUTES).build());
        ActionDTO actionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();
        doReturn(TEST_CERTIFICATE_ID).when(certificateManagementService).addCertificate(any(), anyString());

        ActionDTO result = resolver.resolveForAddOperation(actionDTO, TENANT_DOMAIN);

        assertNotNull(result);
        verifyCommonFields(actionDTO, result);
        assertEquals(result.getPropertyValue(CERTIFICATE), TEST_CERTIFICATE_ID);
        assertEquals(result.getPropertyValue(PASSWORD_SHARING_FORMAT),
                PasswordSharing.Format.SHA256_HASHED.name());
        verify(certificateManagementService, times(1)).addCertificate(any(), anyString());

        assertTrue(result.getPropertyValue(ATTRIBUTES) instanceof BinaryObject);
        assertNotNull(((BinaryObject) result.getPropertyValue(ATTRIBUTES)).getJSONString());
        assertEquals(getAttributesFromActionDTO(result), TEST_ATTRIBUTES);
        verify(claimMetadataManagementService, times(1)).getLocalClaims(TENANT_DOMAIN);
    }

    @Test
    public void testResolveForAddOperationWithoutCOptionalFields() throws Exception {

        Map<String, ActionProperty> properties = new HashMap<>();
        properties.put(PASSWORD_SHARING_FORMAT,
                new ActionProperty.BuilderForService(PasswordSharing.Format.SHA256_HASHED).build());
        ActionDTO actionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();

        ActionDTO result = resolver.resolveForAddOperation(actionDTO, TENANT_DOMAIN);

        assertNotNull(result);
        verifyCommonFields(actionDTO, result);
        assertNull(result.getPropertyValue(CERTIFICATE));
        assertNull(result.getPropertyValue(ATTRIBUTES));
        assertEquals(result.getPropertyValue(PASSWORD_SHARING_FORMAT), PasswordSharing.Format.SHA256_HASHED.name());
        verify(certificateManagementService, never()).addCertificate(any(), anyString());
        verify(claimMetadataManagementService, never()).getLocalClaims(TENANT_DOMAIN);
    }

    @Test(expectedExceptions = ActionDTOModelResolverClientException.class,
            expectedExceptionsMessageRegExp = "Invalid Request")
    public void testResolveForAddOperationWithMissingPasswordSharingFormat() throws Exception {

        ActionDTO actionDTO = new ActionDTO.Builder(action).build();
        resolver.resolveForAddOperation(actionDTO, TENANT_DOMAIN);
    }

    @Test(expectedExceptions = ActionDTOModelResolverClientException.class,
            expectedExceptionsMessageRegExp = "Invalid Password Sharing Format.")
    public void testResolveForAddOperationWithInvalidPasswordSharingFormat() throws Exception {

        Map<String, ActionProperty> properties = new HashMap<>();
        properties.put(PASSWORD_SHARING_FORMAT, new ActionProperty.BuilderForService("Plain text").build());
        ActionDTO actionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();
        resolver.resolveForAddOperation(actionDTO, TENANT_DOMAIN);
    }

    @Test(expectedExceptions = ActionDTOModelResolverClientException.class,
            expectedExceptionsMessageRegExp = "Invalid Certificate.")
    public void testResolveForAddOperationWithInvalidCertificateObject() throws Exception {

        Map<String, ActionProperty> properties = new HashMap<>();
        properties.put(PASSWORD_SHARING_FORMAT,
                new ActionProperty.BuilderForService(PasswordSharing.Format.PLAIN_TEXT).build());
        properties.put(CERTIFICATE, new ActionProperty.BuilderForService(TEST_CERTIFICATE).build());
        ActionDTO actionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();
        resolver.resolveForAddOperation(actionDTO, TENANT_DOMAIN);
    }

    @Test(expectedExceptions = ActionDTOModelResolverClientException.class,
            expectedExceptionsMessageRegExp = "Error while adding the certificate.")
    public void testResolveForAddOperationWithInvalidCertificate() throws Exception {

        Map<String, ActionProperty> properties = new HashMap<>();
        properties.put(PASSWORD_SHARING_FORMAT,
                new ActionProperty.BuilderForService(PasswordSharing.Format.PLAIN_TEXT).build());
        properties.put(CERTIFICATE, new ActionProperty.BuilderForService(new Certificate.Builder()
                .certificateContent(TEST_CERTIFICATE).build()).build());
        ActionDTO actionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();

        CertificateMgtClientException error = mock(CertificateMgtClientException.class);
        doThrow(error).when(certificateManagementService).addCertificate(any(), anyString());

        resolver.resolveForAddOperation(actionDTO, TENANT_DOMAIN);
    }

    @Test(expectedExceptions = ActionDTOModelResolverServerException.class,
            expectedExceptionsMessageRegExp = "Error while adding the certificate.")
    public void testResolveForAddOperationWithServerErrorFromCertificateMgtService() throws Exception {

        Map<String, ActionProperty> properties = new HashMap<>();
        properties.put(PASSWORD_SHARING_FORMAT,
                new ActionProperty.BuilderForService(PasswordSharing.Format.SHA256_HASHED).build());
        properties.put(CERTIFICATE, new ActionProperty.BuilderForService(new Certificate.Builder()
                .certificateContent(TEST_CERTIFICATE).build()).build());
        ActionDTO actionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();

        CertificateMgtServerException error = mock(CertificateMgtServerException.class);
        doThrow(error).when(certificateManagementService).addCertificate(any(), anyString());

        resolver.resolveForAddOperation(actionDTO, TENANT_DOMAIN);
    }

    @DataProvider
    public Object[][] invalidAttributesDataProvider() {

        return new Object[][]{
                {INVALID_TEST_ATTRIBUTES_TYPE, "Invalid attributes format."},
                {INVALID_TEST_ATTRIBUTES_VALUES, "Invalid attributes format."},
                {INVALID_TEST_ATTRIBUTES_COUNT, "Maximum attributes limit exceeded."},
                {INVALID_TEST_ATTRIBUTES, "Invalid attribute provided."},
                {ROLES_CLAIM_ATTRIBUTE, "Not supported."}
        };
    }

    @Test(dataProvider = "invalidAttributesDataProvider")
    public void testResolveForAddOperationWithInvalidAttributes(Object attributes, String expectedError)
            throws Exception {

        Map<String, ActionProperty> properties = new HashMap<>();
        properties.put(PASSWORD_SHARING_FORMAT,
                new ActionProperty.BuilderForService(PasswordSharing.Format.SHA256_HASHED).build());
        properties.put(ATTRIBUTES, new ActionProperty.BuilderForService(attributes).build());
        ActionDTO actionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();
        try {
            resolver.resolveForAddOperation(actionDTO, TENANT_DOMAIN);
            // If no exception is thrown, the test should fail.
            Assert.fail();
        } catch (ActionDTOModelResolverClientException e) {
            assertEquals(e.getMessage(), expectedError);
        }
    }

    @Test
    public void testResolveForAddOperationWithDuplicatedAttributes() throws Exception {

        Map<String, ActionProperty> properties = new HashMap<>();
        properties.put(PASSWORD_SHARING_FORMAT,
                new ActionProperty.BuilderForService(PasswordSharing.Format.SHA256_HASHED).build());
        properties.put(ATTRIBUTES, new ActionProperty.BuilderForService(DUPLICATED_TEST_ATTRIBUTES).build());
        ActionDTO actionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();
        ActionDTO result = resolver.resolveForAddOperation(actionDTO, TENANT_DOMAIN);

        assertNotNull(result);
        verifyCommonFields(actionDTO, result);
        assertTrue(result.getPropertyValue(ATTRIBUTES) instanceof BinaryObject);
        assertNotNull(((BinaryObject) result.getPropertyValue(ATTRIBUTES)).getInputStream());
        assertEquals(getAttributesFromActionDTO(result), Collections.singletonList(DUPLICATED_TEST_ATTRIBUTES.get(0)));
    }

    @Test
    public void testResolveForGetOperation() throws Exception {

        Map<String, ActionProperty> properties = new HashMap<>();
        properties.put(PASSWORD_SHARING_FORMAT,
                new ActionProperty.BuilderForDAO(PasswordSharing.Format.SHA256_HASHED.name()).build());
        properties.put(CERTIFICATE, new ActionProperty.BuilderForDAO(TEST_CERTIFICATE_ID).build());
        properties.put(ATTRIBUTES, new ActionProperty.BuilderForDAO(getBinaryObject(TEST_ATTRIBUTES)).build());
        ActionDTO actionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();

        Certificate certificate = new Certificate.Builder()
                .id(TEST_CERTIFICATE_ID)
                .name(TEST_CERTIFICATE_NAME)
                .certificateContent(TEST_CERTIFICATE)
                .build();
        doReturn(certificate).when(certificateManagementService).getCertificate(any(), anyString());
        ActionDTO result = resolver.resolveForGetOperation(actionDTO, TENANT_DOMAIN);

        assertNotNull(result);

        Certificate resultCert = (Certificate) result.getPropertyValue(CERTIFICATE);
        assertEquals(resultCert.getId(), TEST_CERTIFICATE_ID);
        assertEquals(resultCert.getName(), TEST_CERTIFICATE_NAME);
        assertEquals(resultCert.getCertificateContent(), TEST_CERTIFICATE);
        assertEquals(result.getPropertyValue(PASSWORD_SHARING_FORMAT), PasswordSharing.Format.SHA256_HASHED);
        verify(certificateManagementService, times(1)).getCertificate(anyString(), anyString());
        assertEquals(result.getPropertyValue(ATTRIBUTES), TEST_ATTRIBUTES);
    }

    @Test
    public void testResolveForGetOperationWithoutOptionalFields() throws Exception {

        Map<String, ActionProperty> properties = new HashMap<>();
        properties.put(PASSWORD_SHARING_FORMAT,
                new ActionProperty.BuilderForDAO(PasswordSharing.Format.PLAIN_TEXT.name()).build());
        ActionDTO actionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();

        ActionDTO result = resolver.resolveForGetOperation(actionDTO, TENANT_DOMAIN);

        assertNotNull(result);
        assertNull(result.getPropertyValue(CERTIFICATE));
        assertEquals(result.getPropertyValue(PASSWORD_SHARING_FORMAT), PasswordSharing.Format.PLAIN_TEXT);
        verify(certificateManagementService, never()).getCertificate(anyString(), anyString());
    }

    @Test(expectedExceptions = ActionDTOModelResolverServerException.class,
            expectedExceptionsMessageRegExp = "Error while retrieving the password sharing format.")
    public void testResolveForGetOperationWithMissingPasswordSharingFormat() throws Exception {

        ActionDTO actionDTO = new ActionDTO.Builder(action).build();
        resolver.resolveForGetOperation(actionDTO, TENANT_DOMAIN);
    }

    @Test(expectedExceptions = ActionDTOModelResolverServerException.class,
            expectedExceptionsMessageRegExp = "Error while retrieving the certificate.")
    public void testResolveForGetOperationWithErrorFromCertificateMgtService() throws Exception {

        Map<String, ActionProperty> properties = new HashMap<>();
        properties.put(PASSWORD_SHARING_FORMAT,
                new ActionProperty.BuilderForDAO(PasswordSharing.Format.SHA256_HASHED.name()).build());
        properties.put(CERTIFICATE, new ActionProperty.BuilderForDAO(TEST_CERTIFICATE_ID).build());
        ActionDTO actionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();

        CertificateMgtException error = mock(CertificateMgtException.class);
        doThrow(error).when(certificateManagementService).getCertificate(anyString(), anyString());

        resolver.resolveForGetOperation(actionDTO, TENANT_DOMAIN);
    }

    @Test
    public void testResolveForGetOperationForActionList() throws Exception {

        Map<String, ActionProperty> properties = new HashMap<>();
        properties.put(PASSWORD_SHARING_FORMAT,
                new ActionProperty.BuilderForDAO(PasswordSharing.Format.SHA256_HASHED.name()).build());
        properties.put(CERTIFICATE, new ActionProperty.BuilderForDAO(TEST_CERTIFICATE_ID).build());
        properties.put(ATTRIBUTES, new ActionProperty.BuilderForDAO(getBinaryObject(TEST_ATTRIBUTES)).build());
        ActionDTO actionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();

        Certificate certificate = new Certificate.Builder()
                .id(TEST_CERTIFICATE_ID)
                .name(TEST_CERTIFICATE_NAME)
                .certificateContent(TEST_CERTIFICATE)
                .build();
        doReturn(certificate).when(certificateManagementService).getCertificate(any(), anyString());
        List<ActionDTO> result = resolver.resolveForGetOperation(Collections.singletonList(actionDTO), TENANT_DOMAIN);

        assertNotNull(result);
        for (ActionDTO dto : result) {
            verifyCommonFields(actionDTO, dto);
            Certificate resultCert = (Certificate) dto.getPropertyValue(CERTIFICATE);
            assertEquals(resultCert.getId(), TEST_CERTIFICATE_ID);
            assertEquals(resultCert.getName(), TEST_CERTIFICATE_NAME);
            assertEquals(resultCert.getCertificateContent(), TEST_CERTIFICATE);
            assertEquals(dto.getPropertyValue(PASSWORD_SHARING_FORMAT), PasswordSharing.Format.SHA256_HASHED);
            assertEquals(dto.getPropertyValue(ATTRIBUTES), TEST_ATTRIBUTES);
        }
        verify(certificateManagementService, times(1)).getCertificate(anyString(), anyString());
    }

    @Test
    public void testResolveForUpdateOperation() throws Exception {

        Map<String, ActionProperty> properties = new HashMap<>();
        properties.put(PASSWORD_SHARING_FORMAT,
                new ActionProperty.BuilderForService(PasswordSharing.Format.PLAIN_TEXT).build());
        properties.put(CERTIFICATE, new ActionProperty.BuilderForService(new Certificate.Builder().certificateContent(
                TEST_UPDATED_CERTIFICATE).build()).build());
        properties.put(ATTRIBUTES, new ActionProperty.BuilderForService(UPDATED_TEST_ATTRIBUTES).build());
        ActionDTO updatingActionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();
        doNothing().when(certificateManagementService).updateCertificateContent(anyString(), anyString(), anyString());
        ActionDTO result = resolver.resolveForUpdateOperation(updatingActionDTO, existingActionDTO, TENANT_DOMAIN);

        assertNotNull(result);
        verifyCommonFields(updatingActionDTO, result);
        assertEquals(result.getPropertyValue(CERTIFICATE), TEST_CERTIFICATE_ID);
        assertEquals(result.getPropertyValue(PASSWORD_SHARING_FORMAT), PasswordSharing.Format.PLAIN_TEXT.name());
        verify(certificateManagementService, times(1))
                .updateCertificateContent(anyString(), anyString(), anyString());
        assertEquals(getAttributesFromActionDTO(result), UPDATED_TEST_ATTRIBUTES);
        verify(claimMetadataManagementService, times(1)).getLocalClaims(TENANT_DOMAIN);
    }

    @Test
    public void testResolveForUpdateOperationWithPasswordSharingFormat() throws Exception {

        Map<String, ActionProperty> properties = new HashMap<>();
        properties.put(PASSWORD_SHARING_FORMAT,
                new ActionProperty.BuilderForService(PasswordSharing.Format.PLAIN_TEXT).build());
        ActionDTO updatingActionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();

        ActionDTO result = resolver.resolveForUpdateOperation(updatingActionDTO, existingActionDTO, TENANT_DOMAIN);
        assertNotNull(result);
        verifyCommonFields(updatingActionDTO, result);
        assertEquals(result.getPropertyValue(CERTIFICATE),
                ((Certificate) existingActionDTO.getPropertyValue(CERTIFICATE)).getId());
        assertEquals(result.getPropertyValue(PASSWORD_SHARING_FORMAT), PasswordSharing.Format.PLAIN_TEXT.name());
    }

    @Test
    public void testResolveForUpdateOperationWithDeleteCertificate() throws Exception {

        Map<String, ActionProperty> properties = new HashMap<>();
        properties.put(CERTIFICATE, new ActionProperty.BuilderForService(new Certificate.Builder()
                .certificateContent(StringUtils.EMPTY).build()).build());
        ActionDTO updatingActionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();

        doNothing().when(certificateManagementService).deleteCertificate(anyString(), anyString());
        ActionDTO result = resolver.resolveForUpdateOperation(updatingActionDTO, existingActionDTO, TENANT_DOMAIN);

        assertNotNull(result);
        verifyCommonFields(updatingActionDTO, result);
        assertNull(result.getPropertyValue(CERTIFICATE));
        assertEquals(result.getPropertyValue(PASSWORD_SHARING_FORMAT),
                ((PasswordSharing.Format) existingActionDTO.getPropertyValue(PASSWORD_SHARING_FORMAT)).name());
        verify(certificateManagementService, times(1))
                .deleteCertificate(anyString(), anyString());
    }

    @Test
    public void testResolveForUpdateOperationWithAddCertificate() throws Exception {

        Map<String, ActionProperty> properties = new HashMap<>();
        properties.put(PASSWORD_SHARING_FORMAT,
                new ActionProperty.BuilderForService(PasswordSharing.Format.SHA256_HASHED).build());
        ActionDTO existingActionDTOWithoutCert = new ActionDTO.Builder(action)
                .properties(properties)
                .build();

        properties = new HashMap<>();
        properties.put(PASSWORD_SHARING_FORMAT,
                new ActionProperty.BuilderForService(PasswordSharing.Format.PLAIN_TEXT).build());
        properties.put(CERTIFICATE, new ActionProperty.BuilderForService(new Certificate.Builder()
                .certificateContent(TEST_CERTIFICATE).build()).build());
        ActionDTO updatingActionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();
        doReturn(TEST_CERTIFICATE_ID).when(certificateManagementService).addCertificate(any(), anyString());
        ActionDTO result = resolver.resolveForUpdateOperation(updatingActionDTO, existingActionDTOWithoutCert,
                TENANT_DOMAIN);

        assertNotNull(result);
        verifyCommonFields(updatingActionDTO, result);
        assertNotNull(result.getPropertyValue(CERTIFICATE));
        assertEquals(result.getPropertyValue(CERTIFICATE), TEST_CERTIFICATE_ID);
        assertEquals(result.getPropertyValue(PASSWORD_SHARING_FORMAT), PasswordSharing.Format.PLAIN_TEXT.name());
        verify(certificateManagementService, times(1)).addCertificate(any(), anyString());
    }

    @Test(expectedExceptions = ActionDTOModelResolverClientException.class,
            expectedExceptionsMessageRegExp = "Error while updating the certificate.")
    public void testResolveForUpdateOperationWithInvalidCertificate() throws Exception {

        Map<String, ActionProperty> properties = new HashMap<>();
        properties.put(CERTIFICATE, new ActionProperty.BuilderForService(new Certificate.Builder()
                .certificateContent(TEST_UPDATED_CERTIFICATE).build()).build());
        ActionDTO actionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();
        CertificateMgtClientException error = mock(CertificateMgtClientException.class);
        doThrow(error).when(certificateManagementService).updateCertificateContent(anyString(), anyString(),
                anyString());

        resolver.resolveForUpdateOperation(actionDTO, existingActionDTO, TENANT_DOMAIN);
    }

    @Test(expectedExceptions = ActionDTOModelResolverServerException.class,
            expectedExceptionsMessageRegExp = "Error while updating the certificate.")
    public void testResolveForUpdateOperationWithServerErrorFromCertificateMgtService() throws Exception {

        Map<String, ActionProperty> properties = new HashMap<>();
        properties.put(CERTIFICATE, new ActionProperty.BuilderForService(new Certificate.Builder()
                .certificateContent(TEST_UPDATED_CERTIFICATE).build()).build());
        ActionDTO actionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();
        CertificateMgtServerException error = mock(CertificateMgtServerException.class);
        doThrow(error).when(certificateManagementService).updateCertificateContent(any(), any(), anyString());

        resolver.resolveForUpdateOperation(actionDTO, existingActionDTO, TENANT_DOMAIN);
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
        assertEquals(result.getProperties().size(), 2);
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
        assertEquals(getAttributesFromActionDTO(result), existingActionDTO.getPropertyValue(ATTRIBUTES));
    }

    @Test(dataProvider = "invalidAttributesDataProvider")
    public void testResolveForUpdateOperationWithInvalidAttributes(Object attributes, String expectedError)
            throws Exception {

        Map<String, ActionProperty> properties = new HashMap<>();
        properties.put(ATTRIBUTES, new ActionProperty.BuilderForService(attributes).build());
        ActionDTO updatingActionDTO = new ActionDTO.Builder(action)
                .properties(properties)
                .build();
        try {
            resolver.resolveForUpdateOperation(updatingActionDTO, existingActionDTO, TENANT_DOMAIN);
            // If no exception is thrown, the test should fail.
            Assert.fail();
        } catch (ActionDTOModelResolverClientException e) {
            assertEquals(e.getMessage(), expectedError);
        }
    }

    @Test
    public void testResolveForDeleteOperation() throws Exception {

        doNothing().when(certificateManagementService).deleteCertificate(anyString(), anyString());
        resolver.resolveForDeleteOperation(existingActionDTO, TENANT_DOMAIN);
        verify(certificateManagementService, times(1))
                .deleteCertificate(anyString(), anyString());
    }

    @Test(expectedExceptions = ActionDTOModelResolverServerException.class,
            expectedExceptionsMessageRegExp = "Error while deleting the certificate.")
    public void testResolveForDeleteOperationErrorFromCertificateMgtService() throws Exception {

        CertificateMgtException error = mock(CertificateMgtException.class);
        doThrow(error).when(certificateManagementService).deleteCertificate(anyString(), anyString());
        resolver.resolveForDeleteOperation(existingActionDTO, TENANT_DOMAIN);
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

    private List<String> getAttributesFromActionDTO(ActionDTO actionDTO) throws IOException {

        String jsonString = ((BinaryObject) actionDTO.getPropertyValue(ATTRIBUTES)).getJSONString();
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(jsonString, new TypeReference<List<String>>() { });
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
}
