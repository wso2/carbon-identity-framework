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

package org.wso2.carbon.identity.external.api.client.internal.util;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.utils.ServerConstants;

import java.nio.file.Paths;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for APIClientUtils class.
 */
@WithCarbonHome
public class APIClientUtilsTest {

    private static final int EXPECTED_HTTP_READ_TIMEOUT = 10000;
    private static final int EXPECTED_HTTP_CONNECTION_REQUEST_TIMEOUT = 8000;
    private static final int EXPECTED_HTTP_CONNECTION_TIMEOUT = 5000;
    private static final int EXPECTED_POOL_SIZE = 50;

    /**
     * Sets up the test environment before all tests.
     */
    @BeforeClass
    public void setUp() {

        String identityXmlPath = Paths.get(System.getProperty("user.dir"), "src", "test", "resources",
                "identity.xml").toString();
        System.setProperty(ServerConstants.CARBON_HOME, ".");
        IdentityConfigParser.getInstance(identityXmlPath);
    }

    /**
     * Cleans up the test environment after all tests.
     */
    @AfterClass
    public void tearDown() {

        System.clearProperty(ServerConstants.CARBON_HOME);
    }

    /**
     * Test getting default HTTP read timeout returns the configured value.
     */
    @Test
    public void testGetDefaultHttpReadTimeoutInMillis() {

        int timeout = APIClientUtils.getDefaultHttpReadTimeoutInMillis();
        assertEquals(timeout, EXPECTED_HTTP_READ_TIMEOUT,
                "HTTP read timeout should match the configured value from identity.xml");
    }

    /**
     * Test getting default HTTP connection request timeout returns the configured value.
     */
    @Test
    public void testGetDefaultHttpConnectionRequestTimeoutInMillis() {

        int timeout = APIClientUtils.getDefaultHttpConnectionRequestTimeoutInMillis();
        assertEquals(timeout, EXPECTED_HTTP_CONNECTION_REQUEST_TIMEOUT,
                "HTTP connection request timeout should match the configured value from identity.xml");
    }

    /**
     * Test getting default HTTP connection timeout returns the configured value.
     */
    @Test
    public void testGetDefaultHttpConnectionTimeoutInMillis() {

        int timeout = APIClientUtils.getDefaultHttpConnectionTimeoutInMillis();
        assertEquals(timeout, EXPECTED_HTTP_CONNECTION_TIMEOUT,
                "HTTP connection timeout should match the configured value from identity.xml");
    }

    /**
     * Test getting default pool size returns the configured value.
     */
    @Test
    public void testGetDefaultPoolSizeToBeSet() {

        int poolSize = APIClientUtils.getDefaultPoolSizeToBeSet();
        assertEquals(poolSize, EXPECTED_POOL_SIZE,
                "Pool size should match the configured value from identity.xml");
    }

    /**
     * Test that all getter methods return positive values.
     */
    @Test
    public void testGettersReturnPositiveValues() {

        int readTimeout = APIClientUtils.getDefaultHttpReadTimeoutInMillis();
        assertTrue(readTimeout > 0, "HTTP read timeout should be a positive value");

        int connectionRequestTimeout = APIClientUtils.getDefaultHttpConnectionRequestTimeoutInMillis();
        assertTrue(connectionRequestTimeout > 0, "HTTP connection request timeout should be a positive value");

        int connectionTimeout = APIClientUtils.getDefaultHttpConnectionTimeoutInMillis();
        assertTrue(connectionTimeout > 0, "HTTP connection timeout should be a positive value");

        int poolSize = APIClientUtils.getDefaultPoolSizeToBeSet();
        assertTrue(poolSize > 0, "Pool size should be a positive value");
    }

    /**
     * Test that all getter methods return consistent values across multiple calls.
     */
    @Test
    public void testGettersReturnConsistentValues() {

        int readTimeout1 = APIClientUtils.getDefaultHttpReadTimeoutInMillis();
        int readTimeout2 = APIClientUtils.getDefaultHttpReadTimeoutInMillis();
        assertEquals(readTimeout1, readTimeout2, "HTTP read timeout should return consistent values");

        int connectionRequestTimeout1 = APIClientUtils.getDefaultHttpConnectionRequestTimeoutInMillis();
        int connectionRequestTimeout2 = APIClientUtils.getDefaultHttpConnectionRequestTimeoutInMillis();
        assertEquals(connectionRequestTimeout1, connectionRequestTimeout2,
                "HTTP connection request timeout should return consistent values");

        int connectionTimeout1 = APIClientUtils.getDefaultHttpConnectionTimeoutInMillis();
        int connectionTimeout2 = APIClientUtils.getDefaultHttpConnectionTimeoutInMillis();
        assertEquals(connectionTimeout1, connectionTimeout2,
                "HTTP connection timeout should return consistent values");

        int poolSize1 = APIClientUtils.getDefaultPoolSizeToBeSet();
        int poolSize2 = APIClientUtils.getDefaultPoolSizeToBeSet();
        assertEquals(poolSize1, poolSize2, "Pool size should return consistent values");
    }
}
