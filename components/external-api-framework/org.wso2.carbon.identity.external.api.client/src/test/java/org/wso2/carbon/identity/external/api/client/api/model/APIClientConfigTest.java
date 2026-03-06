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

package org.wso2.carbon.identity.external.api.client.api.model;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.external.api.client.api.exception.APIClientConfigException;
import org.wso2.carbon.utils.ServerConstants;

import java.io.File;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for APIClientConfig class.
 */
@WithCarbonHome
public class APIClientConfigTest {

    private static final long EXPECTED_DEFAULT_RESPONSE_LIMIT = 1048576L;

    @BeforeClass
    public void setUp() {

        String testResourcesPath = new File(
                "src/test/resources/repository/conf/identity/identity.xml").getAbsolutePath();
        System.setProperty("carbon.home", testResourcesPath);
        IdentityConfigParser.getInstance(testResourcesPath);
    }

    @AfterClass
    public void tearDown() {

        System.clearProperty(ServerConstants.CARBON_HOME);
    }

    /**
     * Test that the default response limit falls back to the server-level configured value.
     */
    @Test
    public void testDefaultResponseLimitEqualsServerConfig() throws APIClientConfigException {

        APIClientConfig config = new APIClientConfig.Builder().build();
        assertEquals(config.getResponseLimitInBytes(), EXPECTED_DEFAULT_RESPONSE_LIMIT,
                "Default response limit should match the value configured in identity.xml.");
    }

    /**
     * Test that a custom response limit set via the builder is stored correctly.
     */
    @Test
    public void testBuilderStoresCustomResponseLimit() throws APIClientConfigException {

        long customLimit = 512L * 1024L;
        APIClientConfig config = new APIClientConfig.Builder()
                .responseLimitInBytes(customLimit)
                .build();
        assertEquals(config.getResponseLimitInBytes(), customLimit,
                "Response limit should reflect the value supplied to the builder.");
    }

    /**
     * Test that getResponseLimitInBytes returns a long type value.
     */
    @Test
    public void testGetResponseLimitInBytesReturnsLong() throws APIClientConfigException {

        long largeLimit = 3L * 1024L * 1024L * 1024L; // 3 GB - exceeds int range.
        APIClientConfig config = new APIClientConfig.Builder()
                .responseLimitInBytes(largeLimit)
                .build();
        assertTrue(config.getResponseLimitInBytes() > Integer.MAX_VALUE,
                "Response limit should support values beyond int range.");
        assertEquals(config.getResponseLimitInBytes(), largeLimit,
                "Response limit should accurately hold a value larger than Integer.MAX_VALUE.");
    }

    /**
     * Test that the builder throws an exception when response limit is zero.
     */
    @Test(expectedExceptions = APIClientConfigException.class)
    public void testBuilderThrowsOnZeroResponseLimit() throws APIClientConfigException {

        new APIClientConfig.Builder().responseLimitInBytes(0L).build();
    }

    /**
     * Test that the builder throws an exception when response limit is negative.
     */
    @Test(expectedExceptions = APIClientConfigException.class)
    public void testBuilderThrowsOnNegativeResponseLimit() throws APIClientConfigException {

        new APIClientConfig.Builder().responseLimitInBytes(-1L).build();
    }
}
