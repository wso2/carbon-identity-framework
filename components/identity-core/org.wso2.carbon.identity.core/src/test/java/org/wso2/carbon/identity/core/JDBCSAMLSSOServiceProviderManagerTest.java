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

package org.wso2.carbon.identity.core;

import org.apache.commons.lang.StringUtils;
import org.mockito.MockedStatic;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.database.utils.jdbc.exceptions.TransactionException;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.dao.JDBCSAMLSSOServiceProviderDAOImpl;
import org.wso2.carbon.identity.core.model.SAMLSSOServiceProviderDO;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.core.util.JdbcUtils;
import org.wso2.carbon.identity.core.util.TestUtils;

import java.io.File;
import java.net.URL;
import java.sql.Connection;

import javax.sql.DataSource;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.testng.Assert.assertEquals;

import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.identity.core.constant.TestConstants.*;
import static org.mockito.ArgumentMatchers.any;

/**
 * This class tests the methods of the SAMLSSOServiceProviderManager class.
 */
@Listeners(MockitoTestNGListener.class)
public class JDBCSAMLSSOServiceProviderManagerTest {

    private MockedStatic<IdentityUtil> identityUtil;
    private MockedStatic<IdentityTenantUtil> identityTenantUtil;
    private MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil;

    public SAMLSSOServiceProviderManager samlSSOServiceProviderManager;

    public SAMLSSOServiceProviderDO sampleServiceProvider1;
    public SAMLSSOServiceProviderDO sampleServiceProvider2;
    public SAMLSSOServiceProviderDO invalidServiceProviderDO;

    @BeforeMethod
    public void setUp() throws Exception {

        URL root = this.getClass().getClassLoader().getResource(".");
        File file = new File(root.getPath());
        System.setProperty("carbon.home", file.getAbsolutePath());

        samlSSOServiceProviderManager = new SAMLSSOServiceProviderManager();
        samlSSOServiceProviderManager.serviceProviderDAO = new JDBCSAMLSSOServiceProviderDAOImpl();

        sampleServiceProvider1 = createServiceProviderDO(ISSUER1);
        sampleServiceProvider2 = createServiceProviderDO(ISSUER2);
        invalidServiceProviderDO = createServiceProviderDO(null);

        TestUtils.initiateH2Base();
        DataSource dataSource = mock(DataSource.class);
        identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
        identityUtil = mockStatic(IdentityUtil.class);
        identityTenantUtil = mockStatic(IdentityTenantUtil.class);

        identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSource);

        Connection connection = TestUtils.getConnection();
        Connection spyConnection = TestUtils.spyConnection(connection);

        lenient().when(dataSource.getConnection()).thenReturn(spyConnection);
        lenient().doNothing().when(spyConnection).close();
    }

    @AfterMethod
    public void tearDown() throws Exception {

        identityUtil.close();
        identityTenantUtil.close();
        identityDatabaseUtil.close();
        TestUtils.closeH2Base();
    }

    @Test
    public void testAddServiceProvider() throws Exception {

        samlSSOServiceProviderManager.addServiceProvider(sampleServiceProvider1, TENANT_ID);
        SAMLSSOServiceProviderDO serviceProviderFromStorage =
                samlSSOServiceProviderManager.getServiceProvider(getIssuerWithQualifier(ISSUER1), TENANT_ID);
        assertEquals(serviceProviderFromStorage, sampleServiceProvider1);
    }

    @Test
    public void testAddEmptyServiceProvider() {

        assertThrows(IdentityException.class,
                () -> samlSSOServiceProviderManager.addServiceProvider(invalidServiceProviderDO, TENANT_ID));
    }

    @Test
    public void addServiceProviderWithDuplicateIssuer() throws Exception {

        samlSSOServiceProviderManager.addServiceProvider(sampleServiceProvider1, TENANT_ID);
        System.out.println("sampleServiceProvider iss " + sampleServiceProvider1);
        assertFalse(samlSSOServiceProviderManager.addServiceProvider(sampleServiceProvider1, TENANT_ID));
    }

    @Test
    public void testAddServiceProviderWithException() throws Exception {

        SAMLSSOServiceProviderDO serviceProviderDO = createServiceProviderDO(ISSUER1);

        try (MockedStatic<JdbcUtils> jdbcUtilsMockedStatic = mockStatic(JdbcUtils.class)) {
            NamedJdbcTemplate namedJdbcTemplate = mock(NamedJdbcTemplate.class);
            jdbcUtilsMockedStatic.when(JdbcUtils::getNewNamedJdbcTemplate).thenReturn(namedJdbcTemplate);
            doThrow(new TransactionException("Transaction failed")).when(namedJdbcTemplate).withTransaction(any());

            assertThrows(IdentityException.class,
                    () -> samlSSOServiceProviderManager.addServiceProvider(serviceProviderDO, TENANT_ID));
        }
    }

    @Test
    public void testUpdateServiceProvider() throws Exception {

        samlSSOServiceProviderManager.addServiceProvider(sampleServiceProvider1, TENANT_ID);
        sampleServiceProvider1.setDoSingleLogout(UPDATED_DO_SINGLE_LOGOUT);
        sampleServiceProvider1.setRequestedRecipients(UPDATED_REQUESTED_RECIPIENTS);

        samlSSOServiceProviderManager.updateServiceProvider(sampleServiceProvider1, getIssuerWithQualifier(ISSUER1),
                TENANT_ID);

        SAMLSSOServiceProviderDO updatedServiceProvider =
                samlSSOServiceProviderManager.getServiceProvider(getIssuerWithQualifier(ISSUER1), TENANT_ID);
        assertEquals(sampleServiceProvider1, updatedServiceProvider);
    }

    @Test
    public void testUpdateServiceProviderWithDuplicateIssuer() throws Exception {

        sampleServiceProvider1 = createServiceProviderDO(ISSUER1);
        sampleServiceProvider2 = createServiceProviderDO(ISSUER2);
        samlSSOServiceProviderManager.addServiceProvider(sampleServiceProvider1, TENANT_ID);
        samlSSOServiceProviderManager.addServiceProvider(sampleServiceProvider2, TENANT_ID);

        sampleServiceProvider2.setIssuer(getIssuerWithQualifier(ISSUER1));
        assertFalse(samlSSOServiceProviderManager.updateServiceProvider(sampleServiceProvider2, ISSUER2, TENANT_ID));
    }

    @Test
    public void testUpdateWithInvalidServiceProvider() throws Exception {

        samlSSOServiceProviderManager.addServiceProvider(sampleServiceProvider1, TENANT_ID);
        assertThrows(IdentityException.class,
                () -> samlSSOServiceProviderManager.updateServiceProvider(invalidServiceProviderDO,
                        getIssuerWithQualifier(ISSUER1), TENANT_ID));
    }

    @Test
    public void testUpdateServiceProviderWithException() throws Exception {

        SAMLSSOServiceProviderDO serviceProviderDO = createServiceProviderDO(ISSUER1);

        try (MockedStatic<JdbcUtils> jdbcUtilsMockedStatic = mockStatic(JdbcUtils.class)) {
            NamedJdbcTemplate namedJdbcTemplate = mock(NamedJdbcTemplate.class);
            jdbcUtilsMockedStatic.when(JdbcUtils::getNewNamedJdbcTemplate).thenReturn(namedJdbcTemplate);
            lenient().doThrow(new TransactionException("Transaction failed")).when(namedJdbcTemplate)
                    .withTransaction(any());

            assertThrows(IdentityException.class,
                    () -> samlSSOServiceProviderManager.updateServiceProvider(serviceProviderDO,
                            getIssuerWithQualifier(ISSUER1), TENANT_ID));
        }
    }

    @Test
    public void testGetServiceProviders() throws Exception {

        samlSSOServiceProviderManager.addServiceProvider(sampleServiceProvider1, TENANT_ID);
        sampleServiceProvider2 = createServiceProviderDO(ISSUER2);
        samlSSOServiceProviderManager.addServiceProvider(sampleServiceProvider2, TENANT_ID);

        SAMLSSOServiceProviderDO[] serviceProviders = samlSSOServiceProviderManager.getServiceProviders(TENANT_ID);

        assertEquals(serviceProviders, new SAMLSSOServiceProviderDO[]{sampleServiceProvider1, sampleServiceProvider2});
    }

    @Test
    public void testGetServiceProvidersWithDataAccessException() throws Exception {

        try (MockedStatic<JdbcUtils> jdbcUtilsMockedStatic = mockStatic(JdbcUtils.class)) {
            NamedJdbcTemplate namedJdbcTemplate = mock(NamedJdbcTemplate.class);
            jdbcUtilsMockedStatic.when(JdbcUtils::getNewNamedJdbcTemplate).thenReturn(namedJdbcTemplate);
            doThrow(new DataAccessException("Data access error")).when(namedJdbcTemplate)
                    .executeQuery(any(), any(), any());

            assertThrows(IdentityException.class, () -> samlSSOServiceProviderManager.getServiceProviders(TENANT_ID));
        }
    }

    @Test
    public void testGetServiceProvider() throws Exception {

        samlSSOServiceProviderManager.addServiceProvider(sampleServiceProvider1, TENANT_ID);

        SAMLSSOServiceProviderDO serviceProviderFromStorage =
                samlSSOServiceProviderManager.getServiceProvider(getIssuerWithQualifier(ISSUER1), TENANT_ID);

        assertEquals(serviceProviderFromStorage, sampleServiceProvider1);
    }

    @Test
    public void testProcessGetServiceProviderWithDataAccessException() throws Exception {

        try (MockedStatic<JdbcUtils> jdbcUtilsMockedStatic = mockStatic(JdbcUtils.class)) {
            NamedJdbcTemplate namedJdbcTemplate = mock(NamedJdbcTemplate.class);
            jdbcUtilsMockedStatic.when(JdbcUtils::getNewNamedJdbcTemplate).thenReturn(namedJdbcTemplate);
            doThrow(new DataAccessException("Data access error")).when(namedJdbcTemplate)
                    .fetchSingleRecord(any(), any(), any());

            assertThrows(IdentityException.class,
                    () -> samlSSOServiceProviderManager.getServiceProvider(ISSUER1, TENANT_ID));
        }
    }

    @Test
    public void testIsServiceProviderExists() throws Exception {

        samlSSOServiceProviderManager.addServiceProvider(sampleServiceProvider1, TENANT_ID);
        assertTrue(samlSSOServiceProviderManager.isServiceProviderExists(getIssuerWithQualifier(ISSUER1), TENANT_ID));
    }

    @Test
    public void testNonExistingIsServiceProviderExists() throws Exception {

        assertFalse(samlSSOServiceProviderManager.isServiceProviderExists(getIssuerWithQualifier(ISSUER1), TENANT_ID));
    }

    @Test
    public void testRemoveServiceProvider() throws Exception {

        samlSSOServiceProviderManager.addServiceProvider(sampleServiceProvider1, TENANT_ID);
        assertTrue(samlSSOServiceProviderManager.removeServiceProvider(getIssuerWithQualifier(ISSUER1), TENANT_ID));
        assertNull(samlSSOServiceProviderManager.getServiceProvider(getIssuerWithQualifier(ISSUER1), TENANT_ID));
    }

    @Test
    public void testRemoveNonExistingServiceProvider() throws Exception {

        assertFalse(samlSSOServiceProviderManager.removeServiceProvider(getIssuerWithQualifier(ISSUER1), TENANT_ID));
    }

    @Test
    public void testRemoveEmptyServiceProvider() {

        assertThrows(IllegalArgumentException.class,
                () -> samlSSOServiceProviderManager.removeServiceProvider(null, TENANT_ID));
    }

    @Test
    public void testUploadServiceProvider() throws Exception {

        samlSSOServiceProviderManager.uploadServiceProvider(sampleServiceProvider1, TENANT_ID);

        SAMLSSOServiceProviderDO serviceProviderFromStorage =
                samlSSOServiceProviderManager.getServiceProvider(getIssuerWithQualifier(ISSUER1), TENANT_ID);
        assertEquals(serviceProviderFromStorage, sampleServiceProvider1);
    }

    @Test
    public void testUploadDuplicateServiceProvider() throws Exception {

        samlSSOServiceProviderManager.uploadServiceProvider(sampleServiceProvider1, TENANT_ID);
        assertThrows(IdentityException.class,
                () -> samlSSOServiceProviderManager.uploadServiceProvider(sampleServiceProvider1, TENANT_ID));
    }

    @Test
    public void testUploadServiceProviderWithDataAccessException() throws Exception {

        SAMLSSOServiceProviderDO serviceProviderDO = createServiceProviderDO(ISSUER1);

        try (MockedStatic<JdbcUtils> jdbcUtilsMockedStatic = mockStatic(JdbcUtils.class)) {
            NamedJdbcTemplate namedJdbcTemplate = mock(NamedJdbcTemplate.class);
            jdbcUtilsMockedStatic.when(JdbcUtils::getNewNamedJdbcTemplate).thenReturn(namedJdbcTemplate);
            doThrow(new TransactionException("Transaction failed")).when(namedJdbcTemplate).withTransaction(any());

            assertThrows(IdentityException.class,
                    () -> samlSSOServiceProviderManager.uploadServiceProvider(serviceProviderDO, TENANT_ID));
        }
    }

    private SAMLSSOServiceProviderDO createServiceProviderDO(String issuer) {

        SAMLSSOServiceProviderDO serviceProviderDO = new SAMLSSOServiceProviderDO();
        serviceProviderDO.setIssuer(issuer);
        serviceProviderDO.setIssuerQualifier(ISSUER_QUALIFIER);
        serviceProviderDO.setAssertionConsumerUrls(ASSERTION_CONSUMER_URLS);
        serviceProviderDO.setDefaultAssertionConsumerUrl(DEFAULT_ASSERTION_CONSUMER_URL);
        serviceProviderDO.setCertAlias(CERT_ALIAS);
        serviceProviderDO.setSloResponseURL(SLO_RESPONSE_URL);
        serviceProviderDO.setSloRequestURL(SLO_REQUEST_URL);
        serviceProviderDO.setDoSingleLogout(DO_SINGLE_LOGOUT);
        serviceProviderDO.setDoSignResponse(DO_SIGN_RESPONSE);
        serviceProviderDO.setDoSignAssertions(DO_SIGN_ASSERTIONS);
        serviceProviderDO.setAttributeConsumingServiceIndex(ATTRIBUTE_CONSUMING_SERVICE_INDEX);
        serviceProviderDO.setRequestedAudiences(REQUESTED_AUDIENCES);
        serviceProviderDO.setRequestedRecipients(REQUESTED_RECIPIENTS);
        serviceProviderDO.setEnableAttributesByDefault(ENABLE_ATTRIBUTES_BY_DEFAULT);
        serviceProviderDO.setNameIDFormat(NAME_ID_FORMAT);
        serviceProviderDO.setIdPInitSSOEnabled(IS_IDP_INIT_SSO_ENABLED);
        serviceProviderDO.setIdPInitSLOEnabled(IDP_INIT_SLO_ENABLED);
        serviceProviderDO.setIdpInitSLOReturnToURLs(IDP_INIT_SLO_RETURN_TO_URLS);
        serviceProviderDO.setDoEnableEncryptedAssertion(DO_ENABLE_ENCRYPTED_ASSERTION);
        serviceProviderDO.setDoValidateSignatureInRequests(DO_VALIDATE_SIGNATURE_IN_REQUESTS);
        serviceProviderDO.setDoValidateSignatureInArtifactResolve(DO_VALIDATE_SIGNATURE_IN_ARTIFACT_RESOLVE);
        serviceProviderDO.setSigningAlgorithmUri(SIGNING_ALGORITHM_URI);
        serviceProviderDO.setDigestAlgorithmUri(DIGEST_ALGORITHM_URI);
        serviceProviderDO.setAssertionEncryptionAlgorithmUri(ASSERTION_ENCRYPTION_ALGORITHM_URI);
        serviceProviderDO.setKeyEncryptionAlgorithmUri(KEY_ENCRYPTION_ALGORITHM_URI);
        serviceProviderDO.setAssertionQueryRequestProfileEnabled(IS_ASSERTION_QUERY_REQUEST_PROFILE_ENABLED);
        serviceProviderDO.setSupportedAssertionQueryRequestTypes(SUPPORTED_ASSERTION_QUERY_REQUEST_TYPES);
        serviceProviderDO.setEnableSAML2ArtifactBinding(ENABLE_SAML2_ARTIFACT_BINDING);
        serviceProviderDO.setSamlECP(SAML_ECP);
        serviceProviderDO.setIdpEntityIDAlias(IDP_ENTITY_ID_ALIAS);
        serviceProviderDO.setDoFrontChannelLogout(DO_FRONT_CHANNEL_LOGOUT);
        serviceProviderDO.setFrontChannelLogoutBinding(FRONT_CHANNEL_LOGOUT_BINDING);
        serviceProviderDO.setAttributeNameFormat(ATTRIBUTE_NAME_FORMAT);

        return serviceProviderDO;
    }

    public String getIssuerWithQualifier(String issuer) {

        return StringUtils.isNotBlank(ISSUER_QUALIFIER) ?
                issuer + IdentityRegistryResources.QUALIFIER_ID + ISSUER_QUALIFIER : issuer;
    }
}
