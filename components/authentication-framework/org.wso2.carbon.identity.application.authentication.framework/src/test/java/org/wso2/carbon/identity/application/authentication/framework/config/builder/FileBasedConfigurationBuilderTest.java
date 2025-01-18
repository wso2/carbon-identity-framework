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
package org.wso2.carbon.identity.application.authentication.framework.config.builder;

import org.apache.axiom.om.OMElement;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationManagementUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.utils.CarbonUtils;

import java.lang.reflect.Method;
import java.nio.file.Paths;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
/*
 * Unit tests for FileBasedConfigurationBuilder class
 */
public class FileBasedConfigurationBuilderTest {

    private FileBasedConfigurationBuilder fileBasedConfigurationBuilder;
    private OMElement documentElement;
    private OMElement v2UrlElem;

    @BeforeTest
    public void setUp() {

        String carbonHome = Paths.get(System.getProperty("user.dir"), "target", "test-classes", "repository").
                toString();
        System.setProperty(CarbonBaseConstants.CARBON_HOME, carbonHome);

        try (MockedStatic<IdentityUtil> identityUtilMock = Mockito.mockStatic(IdentityUtil.class);
             MockedStatic<CarbonUtils> carbonUtilsMock = Mockito.mockStatic(CarbonUtils.class)) {

            identityUtilMock.when(IdentityUtil::getIdentityConfigDirPath).thenReturn("src/tests/resources");
            carbonUtilsMock.when(CarbonUtils::getCarbonHome).thenReturn(carbonHome);

            fileBasedConfigurationBuilder = FileBasedConfigurationBuilder.getInstance();
            // Mock OMElement
            documentElement = mock(OMElement.class);
            v2UrlElem = mock(OMElement.class);
        }

    }

    @Test
    public void testGetInstance() {

        assertNotNull(fileBasedConfigurationBuilder);
    }

    @Test
    public void testGetMaxLoginAttemptCount() {

        // Assuming the test configuration file sets max login attempt count to 5
        assertEquals(5, fileBasedConfigurationBuilder.getMaxLoginAttemptCount());
    }

    @Test
    public void testGetExtensions() {

        // Assuming the test configuration file has some extensions
        assertNotNull(fileBasedConfigurationBuilder.getExtensions());
    }

    @Test
    public void testGetCacheTimeouts() {

        // Assuming the test configuration file has some cache timeouts
        assertNotNull(fileBasedConfigurationBuilder.getCacheTimeouts());
    }

    @Test
    public void testGetAuthenticatorNameMappings() {

        // Assuming the test configuration file has some authenticator name mappings
        assertNotNull(fileBasedConfigurationBuilder.getAuthenticatorNameMappings());
    }

    @Test
    public void testReadAuthenticationEndpointV2URL() throws Exception {
        // Mock the getFirstChildWithName method to return v2UrlElem
        when(documentElement.getFirstChildWithName(IdentityApplicationManagementUtil.
                getQNameWithIdentityApplicationNS(FrameworkConstants.Config.V2)))
                .thenReturn(v2UrlElem);

        // Use reflection to access the private method
        Method method = FileBasedConfigurationBuilder.class.getDeclaredMethod("readAuthenticationEndpointV2URL",
                OMElement.class);
        method.setAccessible(true);

        // Call the method to be tested
        method.invoke(fileBasedConfigurationBuilder, documentElement);

        // Use reflection to access and verify the private helper methods
        Method readAuthenticationEndpointURLV2 = FileBasedConfigurationBuilder.class.getDeclaredMethod(
                "readAuthenticationEndpointURLV2", OMElement.class);
        readAuthenticationEndpointURLV2.setAccessible(true);
        readAuthenticationEndpointURLV2.invoke(fileBasedConfigurationBuilder, v2UrlElem);

        Method readAuthenticationEndpointRetryURLV2 = FileBasedConfigurationBuilder.class.getDeclaredMethod(
                "readAuthenticationEndpointRetryURLV2", OMElement.class);
        readAuthenticationEndpointRetryURLV2.setAccessible(true);
        readAuthenticationEndpointRetryURLV2.invoke(fileBasedConfigurationBuilder, v2UrlElem);

        Method readAuthenticationEndpointErrorURLV2 = FileBasedConfigurationBuilder.class.getDeclaredMethod(
                "readAuthenticationEndpointErrorURLV2", OMElement.class);
        readAuthenticationEndpointErrorURLV2.setAccessible(true);
        readAuthenticationEndpointErrorURLV2.invoke(fileBasedConfigurationBuilder, v2UrlElem);

        Method readAuthenticationEndpointWaitURLV2 = FileBasedConfigurationBuilder.class.getDeclaredMethod(
                "readAuthenticationEndpointWaitURLV2", OMElement.class);
        readAuthenticationEndpointWaitURLV2.setAccessible(true);
        readAuthenticationEndpointWaitURLV2.invoke(fileBasedConfigurationBuilder, v2UrlElem);

        Method readIdentifierFirstConfirmationURLV2 = FileBasedConfigurationBuilder.class.getDeclaredMethod(
                "readIdentifierFirstConfirmationURLV2", OMElement.class);
        readIdentifierFirstConfirmationURLV2.setAccessible(true);
        readIdentifierFirstConfirmationURLV2.invoke(fileBasedConfigurationBuilder, v2UrlElem);

        Method readAuthenticationEndpointPromptURLV2 = FileBasedConfigurationBuilder.class.getDeclaredMethod(
                "readAuthenticationEndpointPromptURLV2", OMElement.class);
        readAuthenticationEndpointPromptURLV2.setAccessible(true);
        readAuthenticationEndpointPromptURLV2.invoke(fileBasedConfigurationBuilder, v2UrlElem);

        Method readAuthenticationEndpointMissingClaimsURLV2 = FileBasedConfigurationBuilder.class.getDeclaredMethod(
                "readAuthenticationEndpointMissingClaimsURLV2", OMElement.class);
        readAuthenticationEndpointMissingClaimsURLV2.setAccessible(true);
        readAuthenticationEndpointMissingClaimsURLV2.invoke(fileBasedConfigurationBuilder, v2UrlElem);
    }

}
