/*
 * Copyright (c) 2025-2026, WSO2 LLC. (http://www.wso2.com).
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
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.IdentityProviderProperty;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.base.IdentityConstants;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.organization.management.service.util.OrganizationManagementUtil;
import org.wso2.carbon.idp.mgt.dao.CacheBackedIdPMgtDAO;
import org.wso2.carbon.idp.mgt.dao.FileBasedIdPMgtDAO;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

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

    /**
     * Provides test data for maximum session timeout configuration tests.
     *
     * @return Test data with enable flag, timeout value, and expected results.
     */
    @DataProvider(name = "sessionTimeoutConfigProvider")
    public Object[][] sessionTimeoutConfigProvider() {

        return new Object[][]{
                // Valid configuration.
                {"true", "43260", "true", "43260"},
                // Invalid boolean value should default to false.
                {"invalid", "43200", "false", IdentityApplicationConstants.MAXIMUM_SESSION_TIME_OUT_DEFAULT},
                // Invalid numeric value should default to default timeout.
                {"false", "0", "false", IdentityApplicationConstants.MAXIMUM_SESSION_TIME_OUT_DEFAULT},
                {"true", "invalid", "true", IdentityApplicationConstants.MAXIMUM_SESSION_TIME_OUT_DEFAULT},
                // Empty values should use defaults.
                {"false", "", "false", IdentityApplicationConstants.MAXIMUM_SESSION_TIME_OUT_DEFAULT}
        };
    }

    @Test(description = "Tests resident IdP properties include maximum session timeout configurations.",
            dataProvider = "sessionTimeoutConfigProvider")
    public void testAddResidentIdPWithSessionTimeoutProperties(String enableMaxSessionTimeout,
                                                                String maxSessionTimeout,
                                                                String expectedEnableValue,
                                                                String expectedTimeoutValue)
            throws Exception {

        IdentityProvider residentIdP = new IdentityProvider();
        residentIdP.setIdentityProviderName("LOCAL");

        when(dao.addIdP(any(IdentityProvider.class), anyInt(), anyString())).thenReturn("test-resource-id");

        try (MockedStatic<IdentityTenantUtil> mockedIdentityTenantUtil = mockStatic(IdentityTenantUtil.class);
             MockedStatic<IdentityUtil> mockedIdentityUtil = mockStatic(IdentityUtil.class);
             MockedStatic<OrganizationManagementUtil> mockedOrgUtil = mockStatic(OrganizationManagementUtil.class)) {

            mockedIdentityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(1);
            mockedOrgUtil.when(() -> OrganizationManagementUtil.isOrganization(TENANT_DOMAIN)).thenReturn(false);

            mockedIdentityUtil.when(() -> IdentityUtil.getProperty(
                    IdentityConstants.ServerConfig.REMEMBER_ME_TIME_OUT))
                    .thenReturn("20160");
            mockedIdentityUtil.when(() -> IdentityUtil.getProperty(
                    IdentityConstants.ServerConfig.SESSION_IDLE_TIMEOUT))
                    .thenReturn("15");
            mockedIdentityUtil.when(() -> IdentityUtil.getProperty(
                    IdentityConstants.ServerConfig.ENABLE_MAXIMUM_SESSION_TIMEOUT))
                    .thenReturn(enableMaxSessionTimeout);
            mockedIdentityUtil.when(() -> IdentityUtil.getProperty(
                    IdentityConstants.ServerConfig.MAXIMUM_SESSION_TIMEOUT))
                    .thenReturn(maxSessionTimeout);

            identityProviderManager.addResidentIdP(residentIdP, TENANT_DOMAIN);

            // Verify the properties are set correctly.
            IdentityProviderProperty[] idpProperties = residentIdP.getIdpProperties();
            assertNotNull("IdP properties should not be null", idpProperties);
            assertEquals("Should have 4 properties", 4, idpProperties.length);

            // Verify enable maximum session timeout property.
            IdentityProviderProperty enableProp = Arrays.stream(idpProperties)
                    .filter(p -> IdentityApplicationConstants.ENABLE_MAXIMUM_SESSION_TIME_OUT.equals(p.getName()))
                    .findFirst()
                    .orElse(null);
            assertNotNull("Enable maximum session timeout property should exist", enableProp);
            assertEquals("Enable value should match expected", expectedEnableValue, enableProp.getValue());

            // Verify maximum session timeout property.
            IdentityProviderProperty timeoutProp = Arrays.stream(idpProperties)
                    .filter(p -> IdentityApplicationConstants.MAXIMUM_SESSION_TIME_OUT.equals(p.getName()))
                    .findFirst()
                    .orElse(null);
            assertNotNull("Maximum session timeout property should exist", timeoutProp);
            assertEquals("Timeout value should match expected", expectedTimeoutValue, timeoutProp.getValue());
        }
    }

}
