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

import org.testng.annotations.Test;
import org.wso2.carbon.identity.external.api.client.api.exception.APIClientConfigException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertThrows;

/**
 * Unit tests for APIInvocationConfig class.
 */
public class APIInvocationConfigTest {

    private static final int DEFAULT_RETRY_COUNT = 0;
    private static final int CUSTOM_RETRY_COUNT = 3;
    private static final int NEGATIVE_RETRY_COUNT = -1;
    private static final int ZERO_RETRY_COUNT = 0;

    /**
     * Test successful creation of APIInvocationConfig with default values.
     */
    @Test
    public void testCreateAPIInvocationConfigWithDefaultValues() {

        APIInvocationConfig config = new APIInvocationConfig();

        assertNotNull(config);
        assertEquals(config.getAllowedRetryCount(), DEFAULT_RETRY_COUNT);
    }

    /**
     * Test setting allowed retry count.
     */
    @Test
    public void testSetAllowedRetryCount() throws APIClientConfigException {

        APIInvocationConfig config = new APIInvocationConfig();
        config.setAllowedRetryCount(CUSTOM_RETRY_COUNT);

        assertEquals(config.getAllowedRetryCount(), CUSTOM_RETRY_COUNT);
    }

    /**
     * Test setting zero retry count.
     */
    @Test
    public void testSetZeroRetryCount() throws APIClientConfigException {

        APIInvocationConfig config = new APIInvocationConfig();
        config.setAllowedRetryCount(ZERO_RETRY_COUNT);

        assertEquals(config.getAllowedRetryCount(), ZERO_RETRY_COUNT);
    }

    /**
     * Test setting negative retry count.
     */
    @Test
    public void testSetNegativeRetryCount() {

        APIInvocationConfig config = new APIInvocationConfig();
        assertThrows(APIClientConfigException.class, () -> {
            config.setAllowedRetryCount(NEGATIVE_RETRY_COUNT);
        });
    }

    /**
     * Test getter method returns correct value.
     */
    @Test
    public void testGetAllowedRetryCount() throws APIClientConfigException {

        APIInvocationConfig config = new APIInvocationConfig();
        
        // Test default value
        assertEquals(config.getAllowedRetryCount(), DEFAULT_RETRY_COUNT);
        
        // Test after setting custom value
        config.setAllowedRetryCount(CUSTOM_RETRY_COUNT);
        assertEquals(config.getAllowedRetryCount(), CUSTOM_RETRY_COUNT);
    }

    /**
     * Test that setter modifies the state correctly.
     */
    @Test
    public void testSetterModifiesState() throws APIClientConfigException {

        APIInvocationConfig config = new APIInvocationConfig();
        int initialValue = config.getAllowedRetryCount();
        
        config.setAllowedRetryCount(CUSTOM_RETRY_COUNT);
        int newValue = config.getAllowedRetryCount();

        assertEquals(newValue, CUSTOM_RETRY_COUNT);
        assertNotEquals(initialValue, newValue, "Initial value should differ from new value");
    }

    /**
     * Test object state consistency.
     */
    @Test
    public void testObjectStateConsistency() throws APIClientConfigException {

        APIInvocationConfig config = new APIInvocationConfig();
        
        // Test multiple get calls return same value
        int firstCall = config.getAllowedRetryCount();
        int secondCall = config.getAllowedRetryCount();
        assertEquals(firstCall, secondCall);
        
        // Set a value and test consistency
        config.setAllowedRetryCount(CUSTOM_RETRY_COUNT);
        int thirdCall = config.getAllowedRetryCount();
        int fourthCall = config.getAllowedRetryCount();
        assertEquals(thirdCall, fourthCall);
        assertEquals(thirdCall, CUSTOM_RETRY_COUNT);
    }

    /**
     * Test typical use case scenarios.
     */
    @Test
    public void testTypicalUseCaseScenarios() throws APIClientConfigException {

        APIInvocationConfig config = new APIInvocationConfig();
        
        // Scenario 1: No retries (default)
        assertEquals(config.getAllowedRetryCount(), 0);
        
        // Scenario 2: Enable retries for resilience
        config.setAllowedRetryCount(3);
        assertEquals(config.getAllowedRetryCount(), 3);
        
        // Scenario 3: High availability scenario with more retries
        config.setAllowedRetryCount(10);
        assertEquals(config.getAllowedRetryCount(), 10);
        
        // Scenario 4: Disable retries again
        config.setAllowedRetryCount(0);
        assertEquals(config.getAllowedRetryCount(), 0);
    }
}
