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

import org.apache.commons.lang.StringUtils;
import org.mockito.Mock;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.IdentityRegistryResources;
import org.wso2.carbon.identity.core.model.SAMLSSOServiceProviderDO;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test class for SAMLSSOServiceProviderDAO.
 */
public class SAMLSSOServiceProviderDAOTest {

    private SAMLSSOServiceProviderDAO objUnderTest;

    @Mock
    private Registry mockRegistry;

    @BeforeClass
    public void setUp() throws Exception {
        objUnderTest = new SAMLSSOServiceProviderDAO(mockRegistry);
    }

    @AfterClass
    public void tearDown() throws Exception {
    }

    @DataProvider(name = "ResourceToObjectData")
    public Object[][] getResourceToObjectData() {
        Map<String, List<String>> dummyBasicProperties = new HashMap<>();
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

        Map<String, List<String>> dummyAdvProperties = new HashMap<>();
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

        return new Object[][]{
                {dummyBasicProperties},
                {dummyAdvProperties}
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
                (IdentityRegistryResources.PROP_SAML_SSO_ENABLE_ATTRIBUTES_BY_DEFAULT))), "Enable attribs by default " +
                "mismatch");
        assertEquals(serviceProviderDO.isIdPInitSSOEnabled(), Boolean.parseBoolean(dummyResource.getProperty(
                (IdentityRegistryResources.PROP_SAML_SSO_IDP_INIT_SSO_ENABLED))), "Idp SSO enabled mismatch");
        assertEquals(serviceProviderDO.isIdPInitSLOEnabled(), Boolean.parseBoolean(dummyResource.getProperty(
                (IdentityRegistryResources.PROP_SAML_SLO_IDP_INIT_SLO_ENABLED))), "Idp SLO enabled mismatch");
        assertEquals(serviceProviderDO.isDoEnableEncryptedAssertion(), Boolean.parseBoolean(dummyResource.getProperty(
                (IdentityRegistryResources.PROP_SAML_SSO_ENABLE_ENCRYPTED_ASSERTION))), "Assertion encrypted mismatch");
        assertEquals(serviceProviderDO.isDoValidateSignatureInRequests(), Boolean.parseBoolean(dummyResource
                .getProperty((IdentityRegistryResources.PROP_SAML_SSO_VALIDATE_SIGNATURE_IN_REQUESTS))));
        assertEquals(serviceProviderDO.getNameIDFormat(), dummyResource.getProperty(IdentityRegistryResources
                .PROP_SAML_SSO_NAMEID_FORMAT), "Name id format Mismatch.");
        assertEquals(serviceProviderDO.getNameIdClaimUri(), dummyResource.getProperty(IdentityRegistryResources
                .PROP_SAML_SSO_NAMEID_CLAIMURI), "Name id claim URI Mismatch.");
        assertEquals(serviceProviderDO.getSloResponseURL(), dummyResource.getProperty(IdentityRegistryResources
                .PROP_SAML_SLO_RESPONSE_URL), "SLO response URL Mismatch.");
        assertEquals(serviceProviderDO.getSloRequestURL(), dummyResource.getProperty(IdentityRegistryResources
                .PROP_SAML_SLO_REQUEST_URL), "SLO req url Mismatch.");

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
    }

    @Test
    public void testAddServiceProvider() throws Exception {
    }

    @Test
    public void testGetServiceProviders() throws Exception {
    }

    @Test
    public void testRemoveServiceProvider() throws Exception {
    }

    @Test
    public void testGetServiceProvider() throws Exception {
    }

    @Test
    public void testIsServiceProviderExists() throws Exception {
    }

    @Test
    public void testUploadServiceProvider() throws Exception {
    }

    @Test
    public void testGetAllObjects() throws Exception {
    }

    @Test
    public void testGetAllObjectsWithPropertyValue() throws Exception {
    }

    @Test
    public void testGetFirstObjectWithPropertyValue() throws Exception {
    }

    @Test
    public void testResourceToObject1() throws Exception {
    }

}
