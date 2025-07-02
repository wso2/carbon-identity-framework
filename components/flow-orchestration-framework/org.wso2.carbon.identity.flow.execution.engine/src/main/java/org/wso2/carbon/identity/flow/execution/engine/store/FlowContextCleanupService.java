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

package org.wso2.carbon.identity.flow.execution.engine.store;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.flow.execution.engine.Constants.FlowExecutionConfigs;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service for cleaning up expired flow contexts from the store.
 */
public class FlowContextCleanupService {

    private static final Log LOG = LogFactory.getLog(FlowContextCleanupService.class);

    private static final int NUM_THREADS = 1;
    private static final long DEFAULT_INITIAL_DELAY_MINUTES = 30L;
    private static final long DEFAULT_INTERVAL_MINUTES = 60L;

    private final ScheduledExecutorService scheduler;
    private final long initialDelay;
    private final long interval;

    public FlowContextCleanupService() {

        this.initialDelay = loadInitialDelay();
        this.interval = loadInterval();
        this.scheduler = Executors.newScheduledThreadPool(NUM_THREADS);
        addShutdownHook();
    }

    /**
     * Activates the cleanup scheduler if enabled via config.
     */
    public void activateCleanUp() {

        String enabledStr = IdentityUtil.getProperty(FlowExecutionConfigs.CLEANUP_ENABLED_PROPERTY);
        if (!Boolean.parseBoolean(StringUtils.defaultIfBlank(enabledStr, "true"))) {
            LOG.warn("Flow context cleanup service is disabled via config.");
            return;
        }

        LOG.info(String.format("Starting FlowContextCleanupService with initialDelay: %d min, interval: %d min.",
                initialDelay, interval));

        scheduler.scheduleWithFixedDelay(
                new DatabaseCleanUpTask(),
                initialDelay,
                interval,
                TimeUnit.MINUTES
        );
    }

    /**
     * Cleanup task that delegates to FlowContextStore.
     */
    private static class DatabaseCleanUpTask implements Runnable {

        @Override
        public void run() {

            LOG.debug("Running scheduled flow context cleanup task.");
            try {
                FlowContextStore.getInstance().cleanupExpiredContexts();
                LOG.debug("Flow context cleanup task completed.");
            } catch (FlowEngineException e) {
                LOG.error("Failed to cleanup expired flow contexts.", e);
            }
        }
    }

    private long loadInitialDelay() {

        return parseConfigProperty(
                FlowExecutionConfigs.CLEANUP_INITIAL_DELAY_PROPERTY,
                DEFAULT_INITIAL_DELAY_MINUTES,
                "initial delay"
        );
    }

    private long loadInterval() {

        return parseConfigProperty(
                FlowExecutionConfigs.CLEANUP_INTERVAL_PROPERTY,
                DEFAULT_INTERVAL_MINUTES,
                "cleanup interval"
        );
    }

    private long parseConfigProperty(String property, long defaultValue, String label) {

        String value = IdentityUtil.getProperty(property);
        if (StringUtils.isNotBlank(value)) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                LOG.warn(String.format("Invalid %s value: %s. Falling back to default: %d min.",
                        label, value, defaultValue), e);
            }
        } else {
            LOG.debug(String.format("No config found for %s. Using default: %d min.", property, defaultValue));
        }
        return defaultValue;
    }

    /**
     * Adds a shutdown hook to cleanly shutdown the scheduler on JVM exit.
     */
    private void addShutdownHook() {

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Shutting down FlowContextCleanupService scheduler.");
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                LOG.warn("Shutdown interrupted. Forcing scheduler shutdown.");
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }));
    }
}
