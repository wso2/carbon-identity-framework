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
package org.wso2.carbon.identity.application.authentication.framework.config;

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.config.builder.FileBasedConfigurationBuilder;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

import java.nio.file.Paths;
import java.util.function.Supplier;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/*
 * Unit tests for ConfigurationFacade class
 */
public class ConfigurationFacadeTest {

    private ConfigurationFacade configurationFacade;
    private FileBasedConfigurationBuilder fileBasedConfigurationBuilder;

    @BeforeTest
    public void setUp() {
        // Set necessary system properties
        String carbonHome = Paths.get(System.getProperty("user.dir"), "target", "test-classes", "repository").
                toString();
        System.setProperty(CarbonBaseConstants.CARBON_HOME, carbonHome);
        configurationFacade = ConfigurationFacade.getInstance();
        fileBasedConfigurationBuilder = mock(FileBasedConfigurationBuilder.class);
    }

    @Test
    public void testGetAuthenticationEndpointMissingClaimsURL() {
        try (MockedStatic<FileBasedConfigurationBuilder> fileBasedConfigMock =
                     Mockito.mockStatic(FileBasedConfigurationBuilder.class);
             MockedStatic<PrivilegedCarbonContext> carbonContextMock =
                     Mockito.mockStatic(PrivilegedCarbonContext.class);
             MockedStatic<IdentityTenantUtil> identityTenantUtilMock = Mockito.mockStatic(IdentityTenantUtil.class)) {

            fileBasedConfigMock.when(FileBasedConfigurationBuilder::getInstance).thenReturn(
                    fileBasedConfigurationBuilder);

            String defaultContext = "/authenticationendpoint/missing-claims.do";
            String expectedUrl = "https://localhost:9443/authenticationendpoint/missing-claims.do";
            Supplier<String> getValueFromFileBasedConfig = () ->
                    "https://localhost:9443/authenticationendpoint/missing-claims.do";
            Supplier<String> getV2ValueFromFileBasedConfig = () -> null;

            PrivilegedCarbonContext carbonContext = mock(PrivilegedCarbonContext.class);
            carbonContextMock.when(PrivilegedCarbonContext::getThreadLocalCarbonContext).thenReturn(carbonContext);
            when(carbonContext.getApplicationName()).thenReturn(null);
            identityTenantUtilMock.when(IdentityTenantUtil::isTenantQualifiedUrlsEnabled).thenReturn(false);
            identityTenantUtilMock.when(IdentityTenantUtil::shouldUseTenantQualifiedURLs).thenReturn(false);

            when(fileBasedConfigurationBuilder.getAuthenticationEndpointMissingClaimsURL()).
                    thenReturn(getValueFromFileBasedConfig.get());
            when(fileBasedConfigurationBuilder.getAuthenticationEndpointMissingClaimsURLV2()).
                    thenReturn(getV2ValueFromFileBasedConfig.get());

            String result = configurationFacade.getAuthenticationEndpointMissingClaimsURL();
            assertEquals(expectedUrl, result);
        }
    }

    @DataProvider
    public Object[][] provideIsConsentPageRedirectParamsAllowed() {
        return new Object[][]{
                {true},
                {false}
        };
    }

    @Test(dataProvider = "provideIsConsentPageRedirectParamsAllowed")
    public void testIsConsentPageRedirectParamsAllowed(boolean allowConsentPageRedirectParams) {

        try (MockedStatic<FileBasedConfigurationBuilder> fileBasedConfigMock =
                     Mockito.mockStatic(FileBasedConfigurationBuilder.class)) {
            fileBasedConfigMock.when(FileBasedConfigurationBuilder::getInstance).thenReturn(
                    fileBasedConfigurationBuilder);
            when(fileBasedConfigurationBuilder.isConsentPageRedirectParamsAllowed())
                    .thenReturn(allowConsentPageRedirectParams);
            assertEquals(configurationFacade.isConsentPageRedirectParamsAllowed(), allowConsentPageRedirectParams);
        }
    }
}
