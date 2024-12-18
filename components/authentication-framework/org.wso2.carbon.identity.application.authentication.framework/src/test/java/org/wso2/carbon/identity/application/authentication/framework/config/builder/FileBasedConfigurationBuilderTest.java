/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class FileBasedConfigurationBuilderTest {

    private FileBasedConfigurationBuilder fileBasedConfigurationBuilder;

    @Before
    public void setUp() {
        // Mock IdentityUtil.getIdentityConfigDirPath() to return a test path
        Mockito.mockStatic(IdentityUtil.class);
        when(IdentityUtil.getIdentityConfigDirPath()).thenReturn("src/test/resources");

        // Initialize the FileBasedConfigurationBuilder instance
        fileBasedConfigurationBuilder = FileBasedConfigurationBuilder.getInstance();
    }

    @Test
    public void testGetInstance() {
        assertNotNull(fileBasedConfigurationBuilder);
    }


    @Test
    public void testIsDumbMode() {
        // Assuming the test configuration file sets dumb mode to true
        assertTrue(fileBasedConfigurationBuilder.isDumbMode());
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
    public void testGetAuthenticationEndpointURLV2() {
        String expectedURL = "https://localhost:9443/authenticationendpoint/v2/login.do";
        assertEquals(expectedURL, fileBasedConfigurationBuilder.getAuthenticationEndpointURLV2());
    }

    @Test
    public void testGetAuthenticationEndpointRetryURLV2() {
        String expectedURL = "https://localhost:9443/authenticationendpoint/v2/retry.do";
        assertEquals(expectedURL, fileBasedConfigurationBuilder.getAuthenticationEndpointRetryURLV2());
    }

    @Test
    public void testGetAuthenticationEndpointErrorURLV2() {
        String expectedURL = "https://localhost:9443/authenticationendpoint/v2/error.do";
        assertEquals(expectedURL, fileBasedConfigurationBuilder.getAuthenticationEndpointErrorURLV2());
    }

    @Test
    public void testGetAuthenticationEndpointWaitURLV2() {
        String expectedURL = "https://localhost:9443/authenticationendpoint/v2/wait.do";
        assertEquals(expectedURL, fileBasedConfigurationBuilder.getAuthenticationEndpointWaitURLV2());
    }

    @Test
    public void testGetIdentifierFirstConfirmationURLV2() {
        String expectedURL = "https://localhost:9443/authenticationendpoint/v2/idf-confirm.do";
        assertEquals(expectedURL, fileBasedConfigurationBuilder.getIdentifierFirstConfirmationURLV2());
    }

    @Test
    public void testGetAuthenticationEndpointPromptURLV2() {
        String expectedURL = "https://localhost:9443/authenticationendpoint/v2/prompt.do";
        assertEquals(expectedURL, fileBasedConfigurationBuilder.getAuthenticationEndpointPromptURLV2());
    }

    @Test
    public void testGetAuthenticationEndpointMissingClaimsURLV2() {
        String expectedURL = "https://localhost:9443/authenticationendpoint/v2/missing-claims.do";
        assertEquals(expectedURL, fileBasedConfigurationBuilder.getAuthenticationEndpointMissingClaimsURLV2());
    }
}
