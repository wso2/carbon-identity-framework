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
