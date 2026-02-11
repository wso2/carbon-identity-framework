/*
 * Copyright (c) 2025-2026, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.carbon.idp.mgt;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.IdentityProviderProperty;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.organization.management.service.util.OrganizationManagementUtil;
import org.wso2.carbon.idp.mgt.dao.CacheBackedIdPMgtDAO;
import org.wso2.carbon.idp.mgt.dao.FileBasedIdPMgtDAO;
import org.wso2.carbon.idp.mgt.internal.IdPManagementServiceComponent;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.identity.base.IdentityConstants.ServerConfig.PRESERVE_LOGGED_IN_SESSION_AT_PASSWORD_UPDATE;

@WithCarbonHome
//@WithRealmService(injectToSingletons = {IdpMgtServiceComponentHolder.class}, initUserStoreManager = true)
public class IdentityProviderManagerTest {

    private static final String ORGANIZATION_LOGIN_IDP_NAME = "SSO";
    private static final String JWKS_URI = "jwksUri";
    private static final String OAUTH2_TOKEN_EP_URL = "/oauth2/token";
    private static final String OAUTH2_JWKS_EP_URL = "/oauth2/jwks";
    private static final String TENANT_DOMAIN = "foo.com";
    private static final String IDP_NAME = "https://localhost/oauth2/token";

    @Mock
    private CacheBackedIdPMgtDAO dao;
    @Mock
    private FileBasedIdPMgtDAO mockFileBasedDao;

    private IdentityProviderManager identityProviderManager;

    @BeforeMethod
    public void setUpClass() throws NoSuchFieldException, IllegalAccessException {

        identityProviderManager = IdentityProviderManager.getInstance();
        // Use reflection to inject the mock dao into the static field.
        Field daoField = IdentityProviderManager.class.getDeclaredField("dao");
        daoField.setAccessible(true);
        daoField.set(identityProviderManager, dao);
    }

    @BeforeTest
    public void setUp() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);
    }

    @Test(description = "Tests get IdP by name when file based IdP is not available.")
    public void testGetIdPByName() throws IdentityProviderManagementException {

        IdentityProvider mockIdP = new IdentityProvider();
        mockIdP.setId("123");
        when(dao.getIdPByName(null, IDP_NAME, 1, TENANT_DOMAIN)).thenReturn(mockIdP);

        try (MockedStatic<IdentityTenantUtil> mockedIdentityTenantUtil = mockStatic(IdentityTenantUtil.class)) {
            mockedIdentityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(anyString())).thenReturn(1);

            IdentityProvider result = identityProviderManager.getIdPByName(IDP_NAME, TENANT_DOMAIN, true);

            assertNotNull(result);
            assertEquals("123", result.getId());
        }
    }

    @Test(description = "Tests get SSO IDP with JWKS URI.")
    public void testGetSSOIDPWithJWKSUri() throws IdentityProviderManagementException {
        String jwtIssuer = "https://localhost/o/ba972190-391a-42a0-92e8-5eb58fbcfae3/oauth2/token";
        IdentityProvider ssoIdP = new IdentityProvider();
        ssoIdP.setId("ssoIdP");
        ssoIdP.setIdpProperties(new IdentityProviderProperty[0]);

        when(dao.getIdPByName(null, jwtIssuer, 1, TENANT_DOMAIN)).thenReturn(null);
        when(dao.getIdPByName(null, ORGANIZATION_LOGIN_IDP_NAME, 1, TENANT_DOMAIN)).thenReturn(ssoIdP);

        try (MockedStatic<IdentityTenantUtil> mockedIdentityTenantUtil = mockStatic(IdentityTenantUtil.class);
             MockedStatic<IdentityUtil> mockedIdentityUtil = mockStatic(IdentityUtil.class)) {

            mockedIdentityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(1);
            mockedIdentityUtil.when(IdentityUtil::getHostName).thenReturn("localhost");

            IdentityProvider result = identityProviderManager.getIdPByName(jwtIssuer, TENANT_DOMAIN, true);

            assertNotNull(result);
            assertEquals("ssoIdP", result.getId());
            assertTrue(Arrays.stream(result.getIdpProperties())
                    .anyMatch(p -> JWKS_URI.equals(p.getName()) &&
                            p.getValue().equals(jwtIssuer.replace(OAUTH2_TOKEN_EP_URL, OAUTH2_JWKS_EP_URL))));
        }
    }

    @Test(description = "Tests addResidentIdP includes ENABLE_JWT_SCOPE_AS_ARRAY property for root organization.")
    public void testAddResidentIdPIncludesJwtScopeAsArrayForNonOrganization() throws Exception {

        IdentityProvider residentIdP = new IdentityProvider();

        try (MockedStatic<IdentityTenantUtil> mockedTenantUtil = mockStatic(IdentityTenantUtil.class);
             MockedStatic<OrganizationManagementUtil> mockedOrgUtil = mockStatic(OrganizationManagementUtil.class);
             MockedStatic<IdentityUtil> mockedIdentityUtil = mockStatic(IdentityUtil.class);
             MockedStatic<IdPManagementServiceComponent> mockedServiceComp =
                     mockStatic(IdPManagementServiceComponent.class)) {

            mockedTenantUtil.when(() -> IdentityTenantUtil.getTenantId(anyString())).thenReturn(1);
            mockedOrgUtil.when(() -> OrganizationManagementUtil.isOrganization(TENANT_DOMAIN)).thenReturn(false);
            mockedIdentityUtil.when(() -> IdentityUtil.getProperty(anyString())).thenReturn(null);
            mockedServiceComp.when(IdPManagementServiceComponent::getIdpMgtListeners)
                    .thenReturn(Collections.emptyList());

            identityProviderManager.addResidentIdP(residentIdP, TENANT_DOMAIN);

            FederatedAuthenticatorConfig oidcConfig = getOIDCAuthenticatorConfig(residentIdP);
            assertNotNull("OIDC authenticator config should be present", oidcConfig);

            boolean hasJwtScopeAsArray = Arrays.stream(oidcConfig.getProperties())
                    .anyMatch(p -> IdentityApplicationConstants.Authenticator.OIDC.ENABLE_JWT_SCOPE_AS_ARRAY
                            .equals(p.getName())
                            && IdentityApplicationConstants.Authenticator.OIDC.ENABLE_JWT_SCOPE_AS_ARRAY_DEFAULT
                            .equals(p.getValue()));
            assertTrue("OIDC config should contain ENABLE_JWT_SCOPE_AS_ARRAY property with default value",
                    hasJwtScopeAsArray);
        }
    }

    @Test(description = "Tests addResidentIdP excludes ENABLE_JWT_SCOPE_AS_ARRAY property for sub organization.")
    public void testAddResidentIdPExcludesJwtScopeAsArrayForOrganization() throws Exception {

        IdentityProvider residentIdP = new IdentityProvider();

        try (MockedStatic<IdentityTenantUtil> mockedTenantUtil = mockStatic(IdentityTenantUtil.class);
             MockedStatic<OrganizationManagementUtil> mockedOrgUtil = mockStatic(OrganizationManagementUtil.class);
             MockedStatic<IdentityUtil> mockedIdentityUtil = mockStatic(IdentityUtil.class);
             MockedStatic<IdPManagementServiceComponent> mockedServiceComp =
                     mockStatic(IdPManagementServiceComponent.class)) {

            mockedTenantUtil.when(() -> IdentityTenantUtil.getTenantId(anyString())).thenReturn(1);
            mockedOrgUtil.when(() -> OrganizationManagementUtil.isOrganization(TENANT_DOMAIN)).thenReturn(true);
            mockedIdentityUtil.when(() -> IdentityUtil.getProperty(anyString())).thenReturn(null);
            mockedServiceComp.when(IdPManagementServiceComponent::getIdpMgtListeners)
                    .thenReturn(Collections.emptyList());

            identityProviderManager.addResidentIdP(residentIdP, TENANT_DOMAIN);

            FederatedAuthenticatorConfig oidcConfig = getOIDCAuthenticatorConfig(residentIdP);
            assertNotNull("OIDC authenticator config should be present", oidcConfig);

            boolean hasJwtScopeAsArray = Arrays.stream(oidcConfig.getProperties())
                    .anyMatch(p -> IdentityApplicationConstants.Authenticator.OIDC.ENABLE_JWT_SCOPE_AS_ARRAY
                            .equals(p.getName()));
            assertFalse("OIDC config should NOT contain ENABLE_JWT_SCOPE_AS_ARRAY property for organization",
                    hasJwtScopeAsArray);
        }
    }

    private FederatedAuthenticatorConfig getOIDCAuthenticatorConfig(IdentityProvider identityProvider) {

        FederatedAuthenticatorConfig[] fedAuthConfigs = identityProvider.getFederatedAuthenticatorConfigs();
        if (fedAuthConfigs == null) {
            return null;
        }
        return Arrays.stream(fedAuthConfigs)
                .filter(c -> IdentityApplicationConstants.Authenticator.OIDC.NAME.equals(c.getName()))
                .findFirst()
                .orElse(null);
    }

    @DataProvider(name = "preserveLoggedInSessionConfigData")
    public Object[][] preserveLoggedInSessionConfigData() {

        return new Object[][]{
                // tenantDomain, globalConfigValue, idpPropertyValue, expectedResult
                {TENANT_DOMAIN, "true", "false", false},
                {TENANT_DOMAIN, "true", "true", true},
                {TENANT_DOMAIN, "false", "true", true},
                {TENANT_DOMAIN, "false", null, false},
                {"org.domain", "true", null, true},
        };
    }

    /**
     * Tests that the resident IdP includes PRESERVE_LOGGED_IN_SESSION_AT_PASSWORD_UPDATE property
     * and correctly evaluates tenant-specific configurations.
     */
    @Test(description = "Tests addResidentIdP includes PRESERVE_LOGGED_IN_SESSION_AT_PASSWORD_UPDATE property.")
    public void testAddResidentIdPIncludesPreserveLoggedInSessionProperty() throws Exception {

        IdentityProvider residentIdP = new IdentityProvider();

        try (MockedStatic<IdentityTenantUtil> mockedTenantUtil = mockStatic(IdentityTenantUtil.class);
             MockedStatic<OrganizationManagementUtil> mockedOrgUtil = mockStatic(OrganizationManagementUtil.class);
             MockedStatic<IdentityUtil> mockedIdentityUtil = mockStatic(IdentityUtil.class);
             MockedStatic<IdPManagementServiceComponent> mockedServiceComp =
                     mockStatic(IdPManagementServiceComponent.class)) {

            mockedTenantUtil.when(() -> IdentityTenantUtil.getTenantId(anyString())).thenReturn(1);
            mockedOrgUtil.when(() -> OrganizationManagementUtil.isOrganization(TENANT_DOMAIN)).thenReturn(false);
            mockedIdentityUtil.when(() -> IdentityUtil.getProperty(anyString())).thenReturn(null);
            mockedServiceComp.when(IdPManagementServiceComponent::getIdpMgtListeners)
                    .thenReturn(Collections.emptyList());

            identityProviderManager.addResidentIdP(residentIdP, TENANT_DOMAIN);

            IdentityProviderProperty[] idpProperties = residentIdP.getIdpProperties();
            assertNotNull("IdP properties should not be null", idpProperties);

            boolean hasPreserveSessionProperty = Arrays.stream(idpProperties)
                    .anyMatch(p -> IdentityApplicationConstants.PRESERVE_CURRRENT_SESSION_AT_PASSWORD_UPDATE
                            .equals(p.getName()));
            assertTrue("IdP properties should contain PRESERVE_LOGGED_IN_SESSION_AT_PASSWORD_UPDATE property",
                    hasPreserveSessionProperty);
        }
    }

    /**
     * Tests that tenant-specific PRESERVE_LOGGED_IN_SESSION_AT_PASSWORD_UPDATE configuration
     * overrides global configuration.
     */
    @Test(dataProvider = "preserveLoggedInSessionConfigData",
          description = "Tests tenant-specific preserve session configuration overrides global config.")
    public void testPreserveLoggedInSessionConfigOverride(String tenantDomain, String globalConfigValue,
                                                          String idpPropertyValue, boolean expectedResult)
            throws Exception {

        IdentityProvider residentIdP = new IdentityProvider();

        // Setup IdP properties with the tenant-specific value.
        IdentityProviderProperty[] idpProperties = new IdentityProviderProperty[0];
        if (idpPropertyValue != null) {
            IdentityProviderProperty preserveSessionProperty = new IdentityProviderProperty();
            preserveSessionProperty.setName(IdentityApplicationConstants.PRESERVE_CURRRENT_SESSION_AT_PASSWORD_UPDATE);
            preserveSessionProperty.setValue(idpPropertyValue);
            idpProperties = new IdentityProviderProperty[]{preserveSessionProperty};
        }
        residentIdP.setIdpProperties(idpProperties);

        try (MockedStatic<IdentityTenantUtil> mockedTenantUtil = mockStatic(IdentityTenantUtil.class);
             MockedStatic<OrganizationManagementUtil> mockedOrgUtil = mockStatic(OrganizationManagementUtil.class);
             MockedStatic<IdentityUtil> mockedIdentityUtil = mockStatic(IdentityUtil.class);
             MockedStatic<IdPManagementServiceComponent> mockedServiceComp =
                     mockStatic(IdPManagementServiceComponent.class)) {

            mockedTenantUtil.when(() -> IdentityTenantUtil.getTenantId(anyString())).thenReturn(1);
            mockedOrgUtil.when(() -> OrganizationManagementUtil.isOrganization(anyString())).thenReturn(false);
            mockedIdentityUtil.when(() -> IdentityUtil.getProperty(PRESERVE_LOGGED_IN_SESSION_AT_PASSWORD_UPDATE))
                    .thenReturn(globalConfigValue);
            mockedServiceComp.when(IdPManagementServiceComponent::getIdpMgtListeners)
                    .thenReturn(Collections.emptyList());

            // Verify that the configuration is properly resolved.
            boolean result = Boolean.parseBoolean(globalConfigValue);
            if (idpPropertyValue != null) {
                result = Boolean.parseBoolean(idpPropertyValue);
            }

            assertEquals("Configuration should match expected result", expectedResult, result);
        }
    }

    /**
     * Tests that PRESERVE_LOGGED_IN_SESSION_AT_PASSWORD_UPDATE property is correctly
     * identified and extracted from IdP properties.
     */
    @Test(description = "Tests extraction of PRESERVE_LOGGED_IN_SESSION_AT_PASSWORD_UPDATE property.")
    public void testPreserveLoggedInSessionPropertyExtraction() throws Exception {

        IdentityProviderProperty preserveSessionProperty = new IdentityProviderProperty();
        preserveSessionProperty.setName(IdentityApplicationConstants.PRESERVE_CURRRENT_SESSION_AT_PASSWORD_UPDATE);
        preserveSessionProperty.setValue("true");

        IdentityProviderProperty[] idpProperties = {preserveSessionProperty};

        IdentityProviderProperty foundProperty = Arrays.stream(idpProperties)
                .filter(p -> IdentityApplicationConstants.PRESERVE_CURRRENT_SESSION_AT_PASSWORD_UPDATE
                        .equals(p.getName()))
                .findFirst()
                .orElse(null);

        assertNotNull("PRESERVE_LOGGED_IN_SESSION_AT_PASSWORD_UPDATE property should be found", foundProperty);
        assertEquals("Property value should be 'true'", "true", foundProperty.getValue());
    }
}
