package org.wso2.carbon.identity.application.authentication.framework.config;

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.annotations.BeforeTest;
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

            when(fileBasedConfigurationBuilder.getAuthenticationEndpointMissingClaimsURL()).
                    thenReturn(getValueFromFileBasedConfig.get());
            when(fileBasedConfigurationBuilder.getAuthenticationEndpointMissingClaimsURLV2()).
                    thenReturn(getV2ValueFromFileBasedConfig.get());

            String result = configurationFacade.getAuthenticationEndpointMissingClaimsURL();
            assertEquals(expectedUrl, result);
        }
    }
}
