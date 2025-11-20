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

/**
 * Unit tests for APIInvocationConfig class.
 */
@WithCarbonHome
public class APIInvocationConfigTest {

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
     * Test typical use case scenarios.
     */
    @Test
    public void testTypicalUseCaseScenarios() throws APIClientConfigException {

        APIInvocationConfig config = new APIInvocationConfig();
        
        // Scenario 1: Default value from configuration (2 retries as per identity.xml)
        assertEquals(config.getAllowedRetryCount(), 2);
        
        // Scenario 2: Enable retries for resilience
        config.setAllowedRetryCount(3);
        assertEquals(config.getAllowedRetryCount(), 3);
        
        // Scenario 3: High availability scenario with more retries
        config.setAllowedRetryCount(10);
        assertEquals(config.getAllowedRetryCount(), 10);
        
        // Scenario 4: Disable retries
        config.setAllowedRetryCount(0);
        assertEquals(config.getAllowedRetryCount(), 0);
    }

    /**
     * Test that setting a negative retry count throws an exception.
     */
    @Test(expectedExceptions = APIClientConfigException.class)
    public void testSetNegativeRetryCountThrowsException() throws APIClientConfigException {

        APIInvocationConfig config = new APIInvocationConfig();
        config.setAllowedRetryCount(-1);
    }
}
