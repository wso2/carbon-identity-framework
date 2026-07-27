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

package org.wso2.carbon.identity.external.api.client.api.service;

import com.sun.net.httpserver.HttpServer;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.external.api.client.api.model.APIClientConfig;

import java.io.IOException;

/**
 * Tests for AbstractAPIClientManager.
 */
public class AbstractAPIClientManagerTest {

    private HttpServer httpServer;
    private ConcreteAPIClientManager manager;

    /**
     * Minimal concrete subclass used in tests.
     */
    private static class ConcreteAPIClientManager extends AbstractAPIClientManager {

        ConcreteAPIClientManager(APIClientConfig config) {

            super(config);
        }
    }

    @BeforeMethod
    public void setUp() throws Exception {

        APIClientConfig config = new APIClientConfig.Builder()
                .httpReadTimeoutInMillis(5000)
                .httpConnectionRequestTimeoutInMillis(3000)
                .httpConnectionTimeoutInMillis(3000)
                .poolSizeToBeSet(20)
                .defaultMaxPerRoute(10)
                .build();
        manager = new ConcreteAPIClientManager(config);
    }

    @AfterMethod
    public void tearDown() throws IOException {

        if (httpServer != null) {
            httpServer.stop(0);
        }
        if (manager != null) {
            manager.close();
        }
    }

    /**
     * Test that close() completes without throwing an exception on a newly created manager.
     */
    @Test
    public void testCloseSucceeds() throws IOException {

        manager.close();
    }
}
