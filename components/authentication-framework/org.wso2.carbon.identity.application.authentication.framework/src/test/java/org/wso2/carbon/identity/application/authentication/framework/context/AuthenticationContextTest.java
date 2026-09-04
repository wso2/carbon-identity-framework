/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.context;

import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

/**
 * Unit tests for {@link AuthenticationContext}.
 */
public class AuthenticationContextTest {

    /**
     * The login flow restart path (DefaultRequestCoordinator, RESTART_LOGIN_FLOW branch) can call
     * initializeAnalyticsData() on a context which was already initialized, when the same context
     * instance is reached by more than one request. Re-initializing must reset the analytics data
     * rather than fail with "Parameters map trying to override existing key dataMap".
     */
    @Test
    public void testInitializeAnalyticsDataIsIdempotent() {

        AuthenticationContext context = new AuthenticationContext();

        context.initializeAnalyticsData();
        context.setAnalyticsData("stale-key", "stale-value");
        assertNotNull(context.getAnalyticsData("stale-key"));

        // Must not throw, and must start from a clean analytics data map.
        context.initializeAnalyticsData();

        assertNull(context.getAnalyticsData("stale-key"),
                "Analytics data should be reset when the context is re-initialized.");
        assertNotNull(context.getParameter(FrameworkConstants.AnalyticsData.DATA_MAP),
                "Analytics data map should be present after re-initialization.");
        assertNotNull(context.getAnalyticsData(FrameworkConstants.AnalyticsData.AUTHENTICATION_START_TIME),
                "Authentication start time should be re-recorded on re-initialization.");
    }

    /**
     * A restarted login flow can reach the same context instance from more than one thread, so the
     * re-initialization must hold up under concurrent callers as well as sequential ones.
     */
    @Test
    public void testInitializeAnalyticsDataUnderConcurrentCalls() throws Exception {

        int threads = 8;
        int iterationsPerThread = 200;
        AuthenticationContext context = new AuthenticationContext();

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();
        try {
            for (int i = 0; i < threads; i++) {
                futures.add(executor.submit(() -> {
                    start.await();
                    for (int j = 0; j < iterationsPerThread; j++) {
                        context.initializeAnalyticsData();
                    }
                    return null;
                }));
            }
            start.countDown();
            for (Future<?> future : futures) {
                // Surfaces IdentityRuntimeException from any worker as a test failure.
                future.get();
            }
        } finally {
            executor.shutdownNow();
        }

        assertNotNull(context.getParameter(FrameworkConstants.AnalyticsData.DATA_MAP));
        assertNotNull(context.getAnalyticsData(FrameworkConstants.AnalyticsData.AUTHENTICATION_START_TIME));
    }
}
