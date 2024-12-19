/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.identity.core;

import org.slf4j.MDC;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for @ThreadLocalAwareThreadPoolExecutor
 */
public class ThreadLocalAwareThreadPoolExecutorTest {

    private static final String DEFAULT_TENANT_DOMAIN = "tenant1";
    private static final int DEFAULT_TENANT_ID = 1;

    ExecutorService executorService;

    @BeforeClass
    public void setUp() {

        executorService = ThreadLocalAwareExecutors.newFixedThreadPool(5);
    }

    @BeforeMethod
    public void setupMdc() {

        setupMDC(DEFAULT_TENANT_DOMAIN, DEFAULT_TENANT_ID);
    }

    @DataProvider(name = "mdcDataProvider")
    public Object[][] getResourceToObjectData() {

        return new Object[][]{
                {"tenant2", 2},
                {"tenant3", 3},
                {"tenant4", 4},
                {"tenant5", 5},
                {"tenant6", 6},
        };
    }

    @Test
    public void testMDCThreadLocalForExecute() {

        executorService.execute(new TestAsyncTask(DEFAULT_TENANT_DOMAIN, DEFAULT_TENANT_ID));
    }

    @Test
    public void testMDCThreadLocalForSubmit() {

        executorService.submit(new TestAsyncTask(DEFAULT_TENANT_DOMAIN, DEFAULT_TENANT_ID));
    }

    @Test(dataProvider = "mdcDataProvider")
    public void testOverrideMDCThreadLocalForExecute(String tenantDomain, int tenantID) {

        setupMDC(tenantDomain, tenantID);
        executorService.execute(new TestAsyncTask(tenantDomain, tenantID));
    }

    @Test(dataProvider = "mdcDataProvider")
    public void testOverrideMDCThreadLocalForSubmit(String tenantDomain, int tenantID) {

        setupMDC(tenantDomain, tenantID);
        executorService.submit(new TestAsyncTask(tenantDomain, tenantID));
    }

    @Test
    public void testExecutorServiceClassType() {

        assertTrue(executorService instanceof ThreadLocalAwareThreadPoolExecutor);
    }

    private static void setupMDC(String tenantDomain, int tenantID) {
        Map<String, String> contextMap = new HashMap<>();
        contextMap.put("tenantDomain", tenantDomain);
        contextMap.put("tenantID", String.valueOf(tenantID));
        // Set MDC values to the parent thread
        MDC.setContextMap(contextMap);
    }

    private static class TestAsyncTask implements Runnable {

        private final String tenantDomain;
        private final int tenantID;

        public TestAsyncTask(String tenantDomain, int tenantID) {

            this.tenantDomain = tenantDomain;
            this.tenantID = tenantID;
        }

        @Override
        public void run() {

            assertEquals(tenantDomain, MDC.get("tenantDomain"));
            assertEquals(tenantID, Integer.parseInt(MDC.get("tenantID")));
        }
    }
}
