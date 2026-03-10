/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.common.model.test.model;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.IdentityProviderProperty;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Optional;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for the IdentityProvider model.
 */
@Test
public class IdentityProviderTest {

    private static final String SAML_METADATA_URI = "samlMetadataUri";

    @Test(description = "Test that SAMLMetadataEndpoint element is parsed and stored as an idpProperty " +
            "with name 'samlMetadataUri'.")
    public void testBuildWithSamlMetadataEndpoint() {

        final String samlMetadataUrl = "https://localhost:9443/identity/metadata/saml2";
        final String idpXml = "<IdentityProvider>\n" +
                "    <IdentityProviderName>TestSAMLIdP</IdentityProviderName>\n" +
                "    <SAMLMetadataEndpoint>" + samlMetadataUrl + "</SAMLMetadataEndpoint>\n" +
                "</IdentityProvider>";

        OMElement rootElement =
                OMXMLBuilderFactory.createOMBuilder(new StringReader(idpXml)).getDocumentElement();
        IdentityProvider identityProvider = IdentityProvider.build(rootElement);

        assertNotNull(identityProvider, "IdentityProvider must not be null.");
        IdentityProviderProperty[] properties = identityProvider.getIdpProperties();
        assertNotNull(properties, "IdpProperties must not be null.");

        Optional<IdentityProviderProperty> samlMetadataProp = Arrays.stream(properties)
                .filter(p -> SAML_METADATA_URI.equals(p.getName()))
                .findFirst();

        assertTrue(samlMetadataProp.isPresent(), "Property 'samlMetadataUri' must be present.");
        assertEquals(samlMetadataProp.get().getValue(), samlMetadataUrl,
                "The samlMetadataUri value must match the configured endpoint URL.");
        assertEquals(samlMetadataProp.get().getDisplayName(), "Identity Provider's SAML Metadata Endpoint",
                "The display name of the samlMetadataUri property must match the expected value.");
    }

    @Test(description = "Test that when SAMLMetadataEndpoint is absent, no 'samlMetadataUri' property is " +
            "added to idpProperties.")
    public void testBuildWithoutSamlMetadataEndpoint() {

        final String idpXml = "<IdentityProvider>\n" +
                "    <IdentityProviderName>TestIdPNoSAML</IdentityProviderName>\n" +
                "    <JWKSEndpoint>https://localhost:9443/oauth2/jwks</JWKSEndpoint>\n" +
                "</IdentityProvider>";

        OMElement rootElement =
                OMXMLBuilderFactory.createOMBuilder(new StringReader(idpXml)).getDocumentElement();
        IdentityProvider identityProvider = IdentityProvider.build(rootElement);

        assertNotNull(identityProvider, "IdentityProvider must not be null.");
        IdentityProviderProperty[] properties = identityProvider.getIdpProperties();

        boolean hasSamlMetadataProp = Arrays.stream(properties)
                .anyMatch(p -> SAML_METADATA_URI.equals(p.getName()));

        assertFalse(hasSamlMetadataProp,
                "Property 'samlMetadataUri' must not be present when SAMLMetadataEndpoint is not configured.");
    }
}
