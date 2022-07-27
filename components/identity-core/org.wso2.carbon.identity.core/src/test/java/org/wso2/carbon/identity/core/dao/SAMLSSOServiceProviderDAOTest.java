/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.identity.core.dao;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.IdentityRegistryResources;
import org.wso2.carbon.identity.core.model.SAMLSSOServiceProviderDO;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.registry.core.CollectionImpl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.utils.Transaction;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Test class for SAMLSSOServiceProviderDAO.
 */
@PrepareForTest({Transaction.class, IdentityTenantUtil.class})
public class SAMLSSOServiceProviderDAOTest extends PowerMockTestCase {

    private SAMLSSOServiceProviderDAO objUnderTest;
    private boolean transactionStarted = false;

    private Registry mockRegistry;

    private Map<String, List<String>> dummyBasicProperties;
    private Map<String, List<String>> dummyAdvProperties;
    private Map<String, List<String>> dummyPropertiesWithAnIssuerQualifier;

    @BeforeMethod
    public void setUp() throws Exception {
        mockStatic(Transaction.class);
        mockRegistry = mock(UserRegistry.class);
        when(Transaction.isStarted()).thenReturn(transactionStarted);
        //Mock commit transaction
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                transactionStarted = false;
                return null;
            }
        }).when(mockRegistry).commitTransaction();
        //Mock begin transaction
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                transactionStarted = true;
                return null;
            }
        }).when(mockRegistry).beginTransaction();

        objUnderTest = new SAMLSSOServiceProviderDAO(mockRegistry);
        when(mockRegistry.newResource()).thenReturn(new ResourceImpl());
    }

    private void setUpResources() throws Exception {
        dummyBasicProperties = new HashMap<>();
        dummyBasicProperties.put(IdentityRegistryResources.PROP_SAML_SSO_ISSUER, Collections.singletonList
                ("DummyIssuer"));
        dummyBasicProperties.put(IdentityRegistryResources.PROP_SAML_SSO_ASSERTION_CONS_URLS, Arrays.asList
                ("DefaultACS", "AdditionalACS"));
        dummyBasicProperties.put(IdentityRegistryResources.PROP_DEFAULT_SAML_SSO_ASSERTION_CONS_URL, Collections
                .singletonList("DefaultACS"));
        dummyBasicProperties.put(IdentityRegistryResources.PROP_SAML_SSO_ISSUER_CERT_ALIAS, Collections.singletonList
                ("dummyAlias"));
        dummyBasicProperties.put(IdentityRegistryResources.PROP_SAML_SSO_LOGIN_PAGE_URL, Collections.singletonList
                ("loginPage"));

        dummyAdvProperties = new HashMap<>();
        dummyAdvProperties.put(IdentityRegistryResources.PROP_SAML_SSO_ISSUER, Collections.singletonList
                ("DummyAdvIssuer"));
        dummyAdvProperties.putAll(dummyBasicProperties);
        dummyAdvProperties.put(IdentityRegistryResources.PROP_SAML_SSO_SIGNING_ALGORITHM, Collections.singletonList
                ("RSASHA1"));
        dummyAdvProperties.put(IdentityRegistryResources
                .PROP_SAML_SSO_ASSERTION_QUERY_REQUEST_PROFILE_ENABLED, Collections.singletonList("true"));
        dummyAdvProperties.put(IdentityRegistryResources
                .PROP_SAML_SSO_SUPPORTED_ASSERTION_QUERY_REQUEST_TYPES, Collections.singletonList("dummyQueryType"));
        dummyAdvProperties.put(IdentityRegistryResources.PROP_SAML_SSO_DIGEST_ALGORITHM, Collections.singletonList
                ("SHA1"));
        dummyAdvProperties.put(IdentityRegistryResources.PROP_SAML_SSO_ASSERTION_ENCRYPTION_ALGORITHM, Collections
                .singletonList("RSA"));
        dummyAdvProperties.put(IdentityRegistryResources.PROP_SAML_SSO_KEY_ENCRYPTION_ALGORITHM, Collections
                .singletonList("KEY_ENC_RSA"));
        dummyAdvProperties.put(IdentityRegistryResources.PROP_SAML_SSO_DO_SINGLE_LOGOUT, Collections.singletonList
                ("true"));
        dummyAdvProperties.put(IdentityRegistryResources.PROP_SAML_SSO_NAMEID_FORMAT, Collections.singletonList
                ("email"));
        dummyAdvProperties.put(IdentityRegistryResources.PROP_SAML_SSO_ENABLE_NAMEID_CLAIMURI, Collections
                .singletonList("true"));
        dummyAdvProperties.put(IdentityRegistryResources.PROP_SAML_SSO_NAMEID_CLAIMURI, Collections
                .singletonList("wso2.org/nameid"));
        dummyAdvProperties.put(IdentityRegistryResources.PROP_SAML_SSO_DO_SIGN_RESPONSE, Collections.singletonList
                ("true"));
        dummyAdvProperties.put(IdentityRegistryResources.PROP_SAML_SLO_RESPONSE_URL, Collections.singletonList
                ("http://slo.resp.url"));
        dummyAdvProperties.put(IdentityRegistryResources.PROP_SAML_SLO_REQUEST_URL, Collections.singletonList
                ("http://slo.req.url"));
        dummyAdvProperties.put(IdentityRegistryResources.PROP_SAML_SSO_DO_SIGN_ASSERTIONS, Collections.singletonList
                ("true"));
        dummyAdvProperties.put(IdentityRegistryResources.PROP_SAML_SSO_ENABLE_ATTRIBUTES_BY_DEFAULT, Collections
                .singletonList("true"));
        dummyAdvProperties.put(IdentityRegistryResources.PROP_SAML_SSO_IDP_INIT_SSO_ENABLED, Collections
                .singletonList("true"));
        dummyAdvProperties.put(IdentityRegistryResources.PROP_SAML_SLO_IDP_INIT_SLO_ENABLED, Collections
                .singletonList("true"));
        dummyAdvProperties.put(IdentityRegistryResources.PROP_SAML_SSO_ENABLE_ENCRYPTED_ASSERTION, Collections
                .singletonList("true"));
        dummyAdvProperties.put(IdentityRegistryResources.PROP_SAML_SSO_VALIDATE_SIGNATURE_IN_REQUESTS, Collections
                .singletonList("true"));
        dummyAdvProperties.put(IdentityRegistryResources.PROP_SAML_SSO_REQUESTED_CLAIMS, Arrays.asList("givenName",
                "lastname"));
        dummyAdvProperties.put(IdentityRegistryResources.PROP_SAML_SSO_REQUESTED_AUDIENCES, Arrays.asList
                ("audience1", "audience2"));
        dummyAdvProperties.put(IdentityRegistryResources.PROP_SAML_IDP_INIT_SLO_RETURN_URLS, Arrays.asList
                ("idp.slo.url1", "idp.slo.url1"));
        dummyAdvProperties.put(IdentityRegistryResources.PROP_SAML_SSO_REQUESTED_RECIPIENTS, Arrays.asList
                ("recipient1", "recipient2"));
        dummyAdvProperties.put(IdentityRegistryResources.PROP_SAML_SSO_ATTRIB_CONSUMING_SERVICE_INDEX, Collections
                .singletonList("attribConsumingSvcIndex"));
        dummyAdvProperties.put(IdentityRegistryResources.PROP_SAML_ENABLE_ECP, Collections
                .singletonList("true"));
        dummyAdvProperties.put(IdentityRegistryResources.PROP_SAML_SSO_IDP_ENTITY_ID_ALIAS, Collections
                .singletonList("dummyIdPEntityAlias"));

        dummyPropertiesWithAnIssuerQualifier = new HashMap<>();
        dummyPropertiesWithAnIssuerQualifier.putAll(dummyBasicProperties);
        dummyPropertiesWithAnIssuerQualifier.put(IdentityRegistryResources.PROP_SAML_SSO_ISSUER, Collections
                .singletonList("DummyIssuer"));
        dummyPropertiesWithAnIssuerQualifier.put(IdentityRegistryResources.PROP_SAML_SSO_ISSUER_QUALIFIER,
                Collections.singletonList("DummyIssuerQualifier"));
    }

    @AfterMethod
    public void tearDown() throws Exception {
    }

    @DataProvider(name = "ResourceToObjectData")
    public Object[][] getResourceToObjectData() throws Exception {
        setUpResources();
        return new Object[][]{
                {dummyBasicProperties},
                {dummyAdvProperties},
                {dummyPropertiesWithAnIssuerQualifier}
        };
    }

    @Test(dataProvider = "ResourceToObjectData")
    public void testResourceToObject(Object paramMapObj) throws Exception {
        Properties properties = new Properties();
        properties.putAll((Map<?, ?>) paramMapObj);
        Resource dummyResource = new ResourceImpl();
        dummyResource.setProperties(properties);
        SAMLSSOServiceProviderDO serviceProviderDO = objUnderTest.resourceToObject(dummyResource);

        assertEquals(serviceProviderDO.getIssuer(), dummyResource.getProperty(IdentityRegistryResources
                .PROP_SAML_SSO_ISSUER), "Issuer Mismatch.");
        assertEquals(serviceProviderDO.getAssertionConsumerUrlList(), dummyResource.getPropertyValues
                (IdentityRegistryResources.PROP_SAML_SSO_ASSERTION_CONS_URLS), "ACS URLs Mismatch");
        assertEquals(serviceProviderDO.getDefaultAssertionConsumerUrl(), dummyResource.getProperty
                (IdentityRegistryResources.PROP_DEFAULT_SAML_SSO_ASSERTION_CONS_URL), "Default ACS URL Mismatch");
        assertEquals(serviceProviderDO.getCertAlias(), dummyResource.getProperty(IdentityRegistryResources
                .PROP_SAML_SSO_ISSUER_CERT_ALIAS), "Cert Alias Mismatch");
        assertEquals(serviceProviderDO.getLoginPageURL(), dummyResource.getProperty(
                (IdentityRegistryResources.PROP_SAML_SSO_LOGIN_PAGE_URL)), "Login page url mismatch");

        assertEquals(serviceProviderDO.getIssuerQualifier(), dummyResource.getProperty(IdentityRegistryResources.
                PROP_SAML_SSO_ISSUER_QUALIFIER), "Issuer Qualifier Value Mismatch");
        String sigAlg = dummyResource.getProperty(IdentityRegistryResources.PROP_SAML_SSO_SIGNING_ALGORITHM);
        if (StringUtils.isBlank(sigAlg)) {
            sigAlg = IdentityCoreConstants.XML_SIGNATURE_ALGORITHM_RSA_SHA1_URI;
        }

        assertEquals(serviceProviderDO.getSigningAlgorithmUri(), sigAlg, "Sign algorithm mismatch");
        assertEquals(serviceProviderDO.isAssertionQueryRequestProfileEnabled(), Boolean.parseBoolean(dummyResource
                        .getProperty((IdentityRegistryResources.PROP_SAML_SSO_ASSERTION_QUERY_REQUEST_PROFILE_ENABLED))),
                "Query profile enable mismatch");
        assertEquals(serviceProviderDO.getSupportedAssertionQueryRequestTypes(), dummyResource.getProperty(
                (IdentityRegistryResources.PROP_SAML_SSO_SUPPORTED_ASSERTION_QUERY_REQUEST_TYPES)), "Query request " +
                "type mismatch");

        assertEquals(serviceProviderDO.isEnableSAML2ArtifactBinding(), Boolean.parseBoolean(dummyResource
                        .getProperty((IdentityRegistryResources.PROP_SAML_SSO_ENABLE_SAML2_ARTIFACT_BINDING))),
                "SAML2 artifact binding enable mismatch");

        String digestAlg = dummyResource.getProperty(IdentityRegistryResources.PROP_SAML_SSO_DIGEST_ALGORITHM);
        if (StringUtils.isBlank(digestAlg)) {
            digestAlg = IdentityCoreConstants.XML_DIGEST_ALGORITHM_SHA1;
        }
        assertEquals(serviceProviderDO.getDigestAlgorithmUri(), digestAlg, "Digest algorithm mismatch");

        String asEncAlg = dummyResource.getProperty(IdentityRegistryResources
                .PROP_SAML_SSO_ASSERTION_ENCRYPTION_ALGORITHM);
        if (StringUtils.isBlank(asEncAlg)) {
            asEncAlg = IdentityCoreConstants.XML_ASSERTION_ENCRYPTION_ALGORITHM_AES256;
        }
        assertEquals(serviceProviderDO.getAssertionEncryptionAlgorithmUri(), asEncAlg, "Assertion encryption " +
                "algorithm mismatch");

        String keyEncAlg = dummyResource.getProperty(IdentityRegistryResources
                .PROP_SAML_SSO_KEY_ENCRYPTION_ALGORITHM);
        if (StringUtils.isBlank(keyEncAlg)) {
            keyEncAlg = IdentityCoreConstants.XML_KEY_ENCRYPTION_ALGORITHM_RSAOAEP;
        }
        assertEquals(serviceProviderDO.getKeyEncryptionAlgorithmUri(), keyEncAlg, "Key encryption " +
                "algorithm mismatch");

        assertEquals(serviceProviderDO.isDoSingleLogout(), Boolean.parseBoolean(dummyResource.getProperty(
                (IdentityRegistryResources.PROP_SAML_SSO_DO_SINGLE_LOGOUT))), "Is SLO enabled mismatch");
        assertEquals(serviceProviderDO.isDoSignAssertions(), Boolean.parseBoolean(dummyResource.getProperty(
                (IdentityRegistryResources.PROP_SAML_SSO_DO_SIGN_ASSERTIONS))), "Is sign assertions mismatch");
        assertEquals(serviceProviderDO.isEnableAttributesByDefault(), Boolean.parseBoolean(dummyResource.getProperty(
                (IdentityRegistryResources.PROP_SAML_SSO_ENABLE_ATTRIBUTES_BY_DEFAULT))), "Enable attributes " +
                "by default mismatch");
        assertEquals(serviceProviderDO.isIdPInitSSOEnabled(), Boolean.parseBoolean(dummyResource.getProperty(
                (IdentityRegistryResources.PROP_SAML_SSO_IDP_INIT_SSO_ENABLED))), "Idp SSO enabled mismatch");
        assertEquals(serviceProviderDO.isIdPInitSLOEnabled(), Boolean.parseBoolean(dummyResource.getProperty(
                (IdentityRegistryResources.PROP_SAML_SLO_IDP_INIT_SLO_ENABLED))), "Idp SLO enabled mismatch");
        assertEquals(serviceProviderDO.isDoEnableEncryptedAssertion(), Boolean.parseBoolean(dummyResource.getProperty(
                (IdentityRegistryResources.PROP_SAML_SSO_ENABLE_ENCRYPTED_ASSERTION))), "Assertion encrypted mismatch");
        assertEquals(serviceProviderDO.isDoValidateSignatureInRequests(), Boolean.parseBoolean(dummyResource
                .getProperty((IdentityRegistryResources.PROP_SAML_SSO_VALIDATE_SIGNATURE_IN_REQUESTS))));
        assertEquals(serviceProviderDO.isDoValidateSignatureInArtifactResolve(), Boolean.parseBoolean(dummyResource
                .getProperty((IdentityRegistryResources.PROP_SAML_SSO_VALIDATE_SIGNATURE_IN_ARTIFACT_RESOLVE))));
        assertEquals(serviceProviderDO.getNameIDFormat(), dummyResource.getProperty(IdentityRegistryResources
                .PROP_SAML_SSO_NAMEID_FORMAT), "Name id format Mismatch.");
        assertEquals(serviceProviderDO.getNameIdClaimUri(), dummyResource.getProperty(IdentityRegistryResources
                .PROP_SAML_SSO_NAMEID_CLAIMURI), "Name id claim URI Mismatch.");
        assertEquals(serviceProviderDO.getSloResponseURL(), dummyResource.getProperty(IdentityRegistryResources
                .PROP_SAML_SLO_RESPONSE_URL), "SLO response URL Mismatch.");
        assertEquals(serviceProviderDO.getSloRequestURL(), dummyResource.getProperty(IdentityRegistryResources
                .PROP_SAML_SLO_REQUEST_URL), "SLO req url Mismatch.");
        assertEquals(serviceProviderDO.isSamlECP(), Boolean.parseBoolean(dummyResource.getProperty(IdentityRegistryResources
                .PROP_SAML_ENABLE_ECP)), "ECP enabled mismatch");

        if (dummyResource.getPropertyValues
                (IdentityRegistryResources.PROP_SAML_SSO_REQUESTED_CLAIMS) == null) {
            assertTrue(serviceProviderDO.getRequestedClaimsList().isEmpty(), "Requested claims should be empty");
        } else {
            assertEquals(serviceProviderDO.getRequestedClaimsList(), dummyResource.getPropertyValues
                    (IdentityRegistryResources.PROP_SAML_SSO_REQUESTED_CLAIMS), "Requested claim Mismatch.");
        }

        if (dummyResource.getPropertyValues
                (IdentityRegistryResources.PROP_SAML_SSO_REQUESTED_AUDIENCES) == null) {
            assertTrue(serviceProviderDO.getRequestedAudiencesList().isEmpty(), "Audience should be empty");
        } else {
            assertEquals(serviceProviderDO.getRequestedAudiencesList(), dummyResource.getPropertyValues
                    (IdentityRegistryResources.PROP_SAML_SSO_REQUESTED_AUDIENCES), "Audience Mismatch.");

        }

        if (dummyResource.getPropertyValues
                (IdentityRegistryResources.PROP_SAML_IDP_INIT_SLO_RETURN_URLS) == null) {
            assertTrue(serviceProviderDO.getIdpInitSLOReturnToURLList().isEmpty(), "SLO return URL should be empty");
        } else {
            assertEquals(serviceProviderDO.getIdpInitSLOReturnToURLList(), dummyResource.getPropertyValues
                    (IdentityRegistryResources.PROP_SAML_IDP_INIT_SLO_RETURN_URLS), "SLO return URL Mismatch.");
        }

        if (dummyResource.getPropertyValues
                (IdentityRegistryResources.PROP_SAML_SSO_REQUESTED_RECIPIENTS) == null) {
            assertTrue(serviceProviderDO.getRequestedRecipientsList().isEmpty(), "Recipients should be empty");
        } else {
            assertEquals(serviceProviderDO.getRequestedRecipientsList(), dummyResource.getPropertyValues
                    (IdentityRegistryResources.PROP_SAML_SSO_REQUESTED_RECIPIENTS), "Recipient Mismatch.");
        }

        if (dummyResource.getPropertyValues
                (IdentityRegistryResources.PROP_SAML_SSO_ATTRIB_CONSUMING_SERVICE_INDEX) == null) {
            dummyResource.setProperty(IdentityRegistryResources.PROP_SAML_SSO_ATTRIB_CONSUMING_SERVICE_INDEX,
                    StringUtils.EMPTY);
        }
        assertEquals(serviceProviderDO.getAttributeConsumingServiceIndex(), dummyResource.getProperty
                (IdentityRegistryResources.PROP_SAML_SSO_ATTRIB_CONSUMING_SERVICE_INDEX), "Attrib consuming service " +
                "index Mismatch.");
        assertEquals(serviceProviderDO.getIdpEntityIDAlias(), dummyResource.getProperty(IdentityRegistryResources
                .PROP_SAML_SSO_IDP_ENTITY_ID_ALIAS), "IdP Entity ID Alias Mismatch.");
    }

    @Test(dataProvider = "ResourceToObjectData")
    public void testAddServiceProvider(Object paramMapObj) throws Exception {
        Properties properties = new Properties();
        properties.putAll((Map<?, ?>) paramMapObj);
        Resource dummyResource = new ResourceImpl();
        dummyResource.setProperties(properties);
        SAMLSSOServiceProviderDO serviceProviderDO = objUnderTest.resourceToObject(dummyResource);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        String expectedPath = getPath(dummyResource
                .getProperty(IdentityRegistryResources.PROP_SAML_SSO_ISSUER));
        if (StringUtils.isNotBlank(serviceProviderDO.getIssuerQualifier())) {
            expectedPath = getPath(dummyResource.getProperty(IdentityRegistryResources.PROP_SAML_SSO_ISSUER)
                    + IdentityRegistryResources.QUALIFIER_ID + dummyResource.getProperty(IdentityRegistryResources.
                    PROP_SAML_SSO_ISSUER_QUALIFIER));
        }
        objUnderTest.addServiceProvider(serviceProviderDO);
        verify(mockRegistry).put(captor.capture(), any(Resource.class));
        assertEquals(captor.getValue(), expectedPath, "Resource is not added at correct path");
    }

    @Test
    public void testAddExistingServiceProvider() throws Exception {
        SAMLSSOServiceProviderDO serviceProviderDO = new SAMLSSOServiceProviderDO();
        String existingPath = getPath("existingIssuer");
        serviceProviderDO.setIssuer("existingIssuer");
        when(mockRegistry.resourceExists(existingPath)).thenReturn(true);
        assertFalse(objUnderTest.addServiceProvider(serviceProviderDO), "Resource should not have added.");
    }

    @Test(expectedExceptions = {IdentityException.class})
    public void testAddServiceProviderRegistryError() throws Exception {

        SAMLSSOServiceProviderDO serviceProviderDO = new SAMLSSOServiceProviderDO();
        String existingPath = getPath("erringIssuer");
        serviceProviderDO.setIssuer("erringIssuer");
        doThrow(RegistryException.class).when(mockRegistry).put(eq(existingPath), any(Resource.class));
        objUnderTest.addServiceProvider(serviceProviderDO);
    }

    @Test
    public void testGetServiceProviders() throws Exception {
        when(mockRegistry.resourceExists(IdentityRegistryResources.SAML_SSO_SERVICE_PROVIDERS)).thenReturn(true);
        Resource collection = new CollectionImpl();
        String[] paths = new String[]{
                getPath("DummyIssuer"), getPath("DummyAdvIssuer"), getPath("https://example.com/url?abc")
        };
        Properties dummyResourceProperties = new Properties();
        dummyResourceProperties.putAll(dummyBasicProperties);
        Resource dummyResource = new ResourceImpl();
        dummyResource.setProperties(dummyResourceProperties);

        Properties dummyAdvProperties = new Properties();
        dummyAdvProperties.putAll((Map<?, ?>) dummyAdvProperties);
        Resource dummyAdvResource = new ResourceImpl();
        dummyAdvResource.setProperties(dummyAdvProperties);

        Properties urlBasedIssuerResourceProperties = new Properties();
        urlBasedIssuerResourceProperties.putAll((Map<?, ?>) urlBasedIssuerResourceProperties);
        Resource urlBasedIssuerResource = new ResourceImpl();
        urlBasedIssuerResource.setProperties(urlBasedIssuerResourceProperties);
        Resource[] spResources = new Resource[]{dummyResource, dummyAdvResource, urlBasedIssuerResource};
        collection.setContent(paths);
        when(mockRegistry.get(IdentityRegistryResources.SAML_SSO_SERVICE_PROVIDERS)).thenReturn(collection);
        when(mockRegistry.get(paths[0])).thenReturn(spResources[0]);
        when(mockRegistry.get(paths[1])).thenReturn(spResources[1]);
        when(mockRegistry.get(paths[2])).thenReturn(spResources[2]);
        when(mockRegistry.resourceExists(paths[0])).thenReturn(true);
        when(mockRegistry.resourceExists(paths[1])).thenReturn(true);
        when(mockRegistry.resourceExists(paths[2])).thenReturn(true);
        SAMLSSOServiceProviderDO[] serviceProviders = objUnderTest.getServiceProviders();
        assertEquals(serviceProviders.length, 3, "Should have returned 3 service providers.");
    }

    @Test
    public void testRemoveServiceProvider() throws Exception {
        String existingIssuer = "ExistingIssuer";
        String path = getPath(existingIssuer);
        when(mockRegistry.resourceExists(path)).thenReturn(true);
        assertTrue(objUnderTest.removeServiceProvider(existingIssuer), "SP Resource is not deleted from path");
    }

    @Test
    public void testRemoveNonExistingServiceProvider() throws Exception {
        String nonExistingIssuer = "NonExistingIssuer";
        String path = getPath(nonExistingIssuer);
        when(mockRegistry.resourceExists(path)).thenReturn(false);
        assertFalse(objUnderTest.removeServiceProvider(nonExistingIssuer), "SP Resource should not have existed to " +
                "delete.");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRemoveEmptyServiceProvider() throws Exception {
        objUnderTest.removeServiceProvider("");
        fail("SP Resource with empty name could not have been deleted.");
    }

    @Test
    public void testGetServiceProvider() throws Exception {
        mockStatic(IdentityTenantUtil.class);
        RealmService mockRealmService = mock(RealmService.class);
        TenantManager mockTenantManager = mock(TenantManager.class);
        when(IdentityTenantUtil.getRealmService()).thenReturn(mockRealmService);
        when(mockRealmService.getTenantManager()).thenReturn(mockTenantManager);
        when(mockTenantManager.getDomain(anyInt())).thenReturn("test.com");

        Properties dummyResourceProperties = new Properties();
        dummyResourceProperties.putAll(dummyBasicProperties);
        Resource dummyResource = new ResourceImpl();
        dummyResource.setProperties(dummyResourceProperties);

        String path = getPath(dummyResource.getProperty(IdentityRegistryResources.PROP_SAML_SSO_ISSUER));
        when(mockRegistry.resourceExists(path)).thenReturn(true);
        when(mockRegistry.get(path)).thenReturn(dummyResource);

        SAMLSSOServiceProviderDO serviceProviderDO = objUnderTest.getServiceProvider(dummyResource.getProperty
                (IdentityRegistryResources.PROP_SAML_SSO_ISSUER));
        assertEquals(serviceProviderDO.getTenantDomain(), "test.com", "Retrieved resource's tenant domain mismatch");
    }

    @Test
    public void testIsServiceProviderExists() throws Exception {
        String validSP = "ValidSP";
        String path = getPath(validSP);
        when(mockRegistry.resourceExists(path)).thenReturn(true);
        assertTrue(objUnderTest.isServiceProviderExists(validSP));
    }

    @Test
    public void testNonExistingSPIsServiceProviderExists() throws Exception {
        String invalidSP = "InvalidSP";
        String path = getPath(invalidSP);
        when(mockRegistry.resourceExists(path)).thenReturn(false);
        assertFalse(objUnderTest.isServiceProviderExists(invalidSP));
    }

    @Test
    public void testUploadServiceProvider() throws Exception {
        setUpResources();
        Properties properties = new Properties();
        properties.putAll(dummyBasicProperties);
        Resource dummyResource = new ResourceImpl();
        dummyResource.setProperties(properties);
        String expectedPath = getPath(dummyResource
                .getProperty(IdentityRegistryResources.PROP_SAML_SSO_ISSUER));
        when(mockRegistry.resourceExists(expectedPath)).thenReturn(false);
        SAMLSSOServiceProviderDO serviceProviderDO = objUnderTest.resourceToObject(dummyResource);
        assertEquals(objUnderTest.uploadServiceProvider(serviceProviderDO), serviceProviderDO, "Same resource should" +
                " have returned after successful upload.");
    }

    @Test(expectedExceptions = IdentityException.class)
    public void testUploadExistingServiceProvider() throws Exception {
        setUpResources();
        Properties properties = new Properties();
        properties.putAll(dummyAdvProperties);
        Resource dummyResource = new ResourceImpl();
        dummyResource.setProperties(properties);
        String expectedPath = getPath(dummyResource
                .getProperty(IdentityRegistryResources.PROP_SAML_SSO_ISSUER));
        when(mockRegistry.resourceExists(expectedPath)).thenReturn(true);
        SAMLSSOServiceProviderDO serviceProviderDO = objUnderTest.resourceToObject(dummyResource);
        objUnderTest.uploadServiceProvider(serviceProviderDO);
        fail("Uploading an existing SP should have failed");
    }

    private String getPath(String path) {
        String encodedStr = new String(Base64.encodeBase64(path.getBytes()));
        return IdentityRegistryResources.SAML_SSO_SERVICE_PROVIDERS + encodedStr.replace("=", "");
    }
}
