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

import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.dao.HybridSAMLSSOServiceProviderDAOImpl;
import org.wso2.carbon.identity.core.dao.JDBCSAMLSSOServiceProviderDAOImpl;
import org.wso2.carbon.identity.core.dao.RegistrySAMLSSOServiceProviderDAOImpl;
import org.wso2.carbon.identity.core.model.SAMLSSOServiceProviderDO;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;

import static org.mockito.Mockito.when;
import static org.testng.Assert.*;
import static org.wso2.carbon.identity.core.constant.TestConstants.ASSERTION_CONSUMER_URLS;
import static org.wso2.carbon.identity.core.constant.TestConstants.ASSERTION_ENCRYPTION_ALGORITHM_URI;
import static org.wso2.carbon.identity.core.constant.TestConstants.ATTRIBUTE_CONSUMING_SERVICE_INDEX;
import static org.wso2.carbon.identity.core.constant.TestConstants.CERT_ALIAS;
import static org.wso2.carbon.identity.core.constant.TestConstants.DEFAULT_ASSERTION_CONSUMER_URL;
import static org.wso2.carbon.identity.core.constant.TestConstants.DIGEST_ALGORITHM_URI;
import static org.wso2.carbon.identity.core.constant.TestConstants.DO_ENABLE_ENCRYPTED_ASSERTION;
import static org.wso2.carbon.identity.core.constant.TestConstants.DO_FRONT_CHANNEL_LOGOUT;
import static org.wso2.carbon.identity.core.constant.TestConstants.DO_SIGN_ASSERTIONS;
import static org.wso2.carbon.identity.core.constant.TestConstants.DO_SIGN_RESPONSE;
import static org.wso2.carbon.identity.core.constant.TestConstants.DO_SINGLE_LOGOUT;
import static org.wso2.carbon.identity.core.constant.TestConstants.DO_VALIDATE_SIGNATURE_IN_ARTIFACT_RESOLVE;
import static org.wso2.carbon.identity.core.constant.TestConstants.DO_VALIDATE_SIGNATURE_IN_REQUESTS;
import static org.wso2.carbon.identity.core.constant.TestConstants.ENABLE_ATTRIBUTES_BY_DEFAULT;
import static org.wso2.carbon.identity.core.constant.TestConstants.ENABLE_SAML2_ARTIFACT_BINDING;
import static org.wso2.carbon.identity.core.constant.TestConstants.FRONT_CHANNEL_LOGOUT_BINDING;
import static org.wso2.carbon.identity.core.constant.TestConstants.IDP_ENTITY_ID_ALIAS;
import static org.wso2.carbon.identity.core.constant.TestConstants.IDP_INIT_SLO_ENABLED;
import static org.wso2.carbon.identity.core.constant.TestConstants.IDP_INIT_SLO_RETURN_TO_URLS;
import static org.wso2.carbon.identity.core.constant.TestConstants.ISSUER1;
import static org.wso2.carbon.identity.core.constant.TestConstants.ISSUER2;
import static org.wso2.carbon.identity.core.constant.TestConstants.ISSUER_QUALIFIER;
import static org.wso2.carbon.identity.core.constant.TestConstants.IS_ASSERTION_QUERY_REQUEST_PROFILE_ENABLED;
import static org.wso2.carbon.identity.core.constant.TestConstants.IS_IDP_INIT_SSO_ENABLED;
import static org.wso2.carbon.identity.core.constant.TestConstants.KEY_ENCRYPTION_ALGORITHM_URI;
import static org.wso2.carbon.identity.core.constant.TestConstants.NAME_ID_FORMAT;
import static org.wso2.carbon.identity.core.constant.TestConstants.REQUESTED_AUDIENCES;
import static org.wso2.carbon.identity.core.constant.TestConstants.REQUESTED_RECIPIENTS;
import static org.wso2.carbon.identity.core.constant.TestConstants.SAML_ECP;
import static org.wso2.carbon.identity.core.constant.TestConstants.SIGNING_ALGORITHM_URI;
import static org.wso2.carbon.identity.core.constant.TestConstants.SLO_REQUEST_URL;
import static org.wso2.carbon.identity.core.constant.TestConstants.SLO_RESPONSE_URL;
import static org.wso2.carbon.identity.core.constant.TestConstants.SUPPORTED_ASSERTION_QUERY_REQUEST_TYPES;
import static org.wso2.carbon.identity.core.constant.TestConstants.TENANT_ID;

public class HybridSAMLSSOServiceProviderDAOImplTest {

    public SAMLSSOServiceProviderManager samlSSOServiceProviderManager;
    public SAMLSSOServiceProviderDO sampleServiceProvider1;
    public SAMLSSOServiceProviderDO sampleServiceProvider2;
    private MockedStatic<IdentityTenantUtil> identityTenantUtil;

    RegistrySAMLSSOServiceProviderDAOImpl registrySAMLSSOServiceProviderDAOImpl =
            Mockito.mock(RegistrySAMLSSOServiceProviderDAOImpl.class);
    JDBCSAMLSSOServiceProviderDAOImpl jdbcSAMLSSOServiceProviderDAOImpl =
            Mockito.mock(JDBCSAMLSSOServiceProviderDAOImpl.class);

    @BeforeMethod
    public void setUp() throws Exception {

        HybridSAMLSSOServiceProviderDAOImpl hybridSAMLSSOServiceProviderDAOImpl =
                Mockito.spy(new HybridSAMLSSOServiceProviderDAOImpl());
        identityTenantUtil = mockStatic(IdentityTenantUtil.class);

        Field jdbcField =
                HybridSAMLSSOServiceProviderDAOImpl.class.getDeclaredField("jdbcSAMLSSOServiceProviderDAOImpl");
        jdbcField.setAccessible(true);
        jdbcField.set(hybridSAMLSSOServiceProviderDAOImpl, jdbcSAMLSSOServiceProviderDAOImpl);

        Field registryField =
                HybridSAMLSSOServiceProviderDAOImpl.class.getDeclaredField("registrySAMLSSOServiceProviderDAOImpl");
        registryField.setAccessible(true);
        registryField.set(hybridSAMLSSOServiceProviderDAOImpl, registrySAMLSSOServiceProviderDAOImpl);

        URL root = this.getClass().getClassLoader().getResource(".");
        File file = new File(root.getPath());
        System.setProperty("carbon.home", file.getAbsolutePath());

        samlSSOServiceProviderManager = new SAMLSSOServiceProviderManager();
        samlSSOServiceProviderManager.serviceProviderDAO = hybridSAMLSSOServiceProviderDAOImpl;
        sampleServiceProvider1 = createServiceProviderDO(ISSUER1);
        sampleServiceProvider2 = createServiceProviderDO(ISSUER2);

    }

    @AfterMethod
    public void tearDown() throws Exception {

        Mockito.reset(jdbcSAMLSSOServiceProviderDAOImpl, registrySAMLSSOServiceProviderDAOImpl);
        identityTenantUtil.close();
    }

    @Test
    public void testAddServiceProvider() throws Exception {

        when(jdbcSAMLSSOServiceProviderDAOImpl.addServiceProvider(sampleServiceProvider1, TENANT_ID)).thenReturn(true);
        assertTrue(samlSSOServiceProviderManager.addServiceProvider(sampleServiceProvider1, TENANT_ID));
    }

    @Test
    public void testUpdateServiceProviderInRegistry() throws Exception {

        when(jdbcSAMLSSOServiceProviderDAOImpl.isServiceProviderExists(ISSUER1, TENANT_ID)).thenReturn(false);
        when(registrySAMLSSOServiceProviderDAOImpl.updateServiceProvider(sampleServiceProvider1, ISSUER1,
                TENANT_ID)).thenReturn(true);
        assertTrue(samlSSOServiceProviderManager.updateServiceProvider(sampleServiceProvider1, ISSUER1, TENANT_ID));
        verify(jdbcSAMLSSOServiceProviderDAOImpl, never()).updateServiceProvider(sampleServiceProvider1, ISSUER1,
                TENANT_ID);
    }

    @Test
    public void testUpdateServiceProviderInDatabase() throws Exception {

        when(jdbcSAMLSSOServiceProviderDAOImpl.isServiceProviderExists(ISSUER1, TENANT_ID)).thenReturn(true);
        when(jdbcSAMLSSOServiceProviderDAOImpl.updateServiceProvider(sampleServiceProvider1, ISSUER1,
                TENANT_ID)).thenReturn(true);
        assertTrue(samlSSOServiceProviderManager.updateServiceProvider(sampleServiceProvider1, ISSUER1, TENANT_ID));
        verify(registrySAMLSSOServiceProviderDAOImpl, never()).updateServiceProvider(sampleServiceProvider1, ISSUER1,
                TENANT_ID);
    }

    @Test
    public void testGetServiceProviders() throws Exception {

        SAMLSSOServiceProviderDO[] samlSSOServiceProviderDOs = {sampleServiceProvider1, sampleServiceProvider2};
        when(jdbcSAMLSSOServiceProviderDAOImpl.getServiceProviders(TENANT_ID)).thenReturn(samlSSOServiceProviderDOs);
        when(registrySAMLSSOServiceProviderDAOImpl.getServiceProviders(TENANT_ID)).thenReturn(
                samlSSOServiceProviderDOs);
        assertEquals(samlSSOServiceProviderManager.getServiceProviders(TENANT_ID), samlSSOServiceProviderDOs);
    }

    @Test
    public void testRemoveServiceProviderInRegistry() throws Exception {

        when(registrySAMLSSOServiceProviderDAOImpl.isServiceProviderExists(ISSUER1, TENANT_ID)).thenReturn(true);
        when(registrySAMLSSOServiceProviderDAOImpl.removeServiceProvider(ISSUER1, TENANT_ID)).thenReturn(true);
        assertTrue(samlSSOServiceProviderManager.removeServiceProvider(ISSUER1, TENANT_ID));
        verify(jdbcSAMLSSOServiceProviderDAOImpl, never()).removeServiceProvider(ISSUER1, TENANT_ID);
    }

    @Test
    public void testRemoveServiceProviderInDatabase() throws Exception {

        when(jdbcSAMLSSOServiceProviderDAOImpl.isServiceProviderExists(ISSUER1, TENANT_ID)).thenReturn(true);
        when(jdbcSAMLSSOServiceProviderDAOImpl.removeServiceProvider(ISSUER1, TENANT_ID)).thenReturn(true);
        assertTrue(samlSSOServiceProviderManager.removeServiceProvider(ISSUER1, TENANT_ID));
        verify(registrySAMLSSOServiceProviderDAOImpl, never()).removeServiceProvider(ISSUER1, TENANT_ID);
    }

    @Test
    public void testGetServiceProviderInRegistry() throws Exception {

        when(registrySAMLSSOServiceProviderDAOImpl.isServiceProviderExists(ISSUER1, TENANT_ID)).thenReturn(true);
        when(registrySAMLSSOServiceProviderDAOImpl.getServiceProvider(ISSUER1, TENANT_ID)).thenReturn(
                sampleServiceProvider1);
        assertEquals(samlSSOServiceProviderManager.getServiceProvider(ISSUER1, TENANT_ID), sampleServiceProvider1);
        verify(jdbcSAMLSSOServiceProviderDAOImpl, never()).getServiceProvider(ISSUER1, TENANT_ID);
    }

    @Test
    public void testGetServiceProviderInDatabase() throws Exception {

        when(jdbcSAMLSSOServiceProviderDAOImpl.isServiceProviderExists(ISSUER1, TENANT_ID)).thenReturn(true);
        when(jdbcSAMLSSOServiceProviderDAOImpl.getServiceProvider(ISSUER1, TENANT_ID)).thenReturn(
                sampleServiceProvider1);
        assertEquals(samlSSOServiceProviderManager.getServiceProvider(ISSUER1, TENANT_ID), sampleServiceProvider1);
        verify(registrySAMLSSOServiceProviderDAOImpl, never()).getServiceProvider(ISSUER1, TENANT_ID);
    }

    @Test
    public void testIsServiceProviderExistsInRegistry() throws Exception {

        when(registrySAMLSSOServiceProviderDAOImpl.isServiceProviderExists(ISSUER1, TENANT_ID)).thenReturn(true);
        assertTrue(samlSSOServiceProviderManager.isServiceProviderExists(ISSUER1, TENANT_ID));
    }

    @Test
    public void testIsServiceProviderExistsInDatabase() throws Exception {

        when(jdbcSAMLSSOServiceProviderDAOImpl.isServiceProviderExists(ISSUER1, TENANT_ID)).thenReturn(true);
        assertTrue(samlSSOServiceProviderManager.isServiceProviderExists(ISSUER1, TENANT_ID));
    }

    @Test
    public void testIsServiceProviderExistsFalse() throws Exception {

        when(jdbcSAMLSSOServiceProviderDAOImpl.isServiceProviderExists(ISSUER1, TENANT_ID)).thenReturn(false);
        when(registrySAMLSSOServiceProviderDAOImpl.isServiceProviderExists(ISSUER1, TENANT_ID)).thenReturn(false);
        assertFalse(samlSSOServiceProviderManager.isServiceProviderExists(ISSUER1, TENANT_ID));
    }

    @Test
    public void testUploadServiceProvider() throws Exception {

        when(jdbcSAMLSSOServiceProviderDAOImpl.uploadServiceProvider(sampleServiceProvider1, TENANT_ID)).thenReturn(
                sampleServiceProvider1);
        assertEquals(samlSSOServiceProviderManager.uploadServiceProvider(sampleServiceProvider1, TENANT_ID),
                sampleServiceProvider1);
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

        return serviceProviderDO;
    }
}
