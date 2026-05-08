/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.carbon.identity.debug.framework.store;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.debug.framework.dao.DebugSessionDAO;
import org.wso2.carbon.identity.debug.framework.dao.impl.DebugSessionDAOImpl;
import org.wso2.carbon.identity.debug.framework.exception.DebugFrameworkServerException;

import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service to handle periodic cleanup of expired debug sessions.
 */
public class DebugSessionCleanupService {

    private static final Log LOG = LogFactory.getLog(DebugSessionCleanupService.class);

    private static final int INITIAL_DELAY = 1;
    private static final String CLEANUP_INTERVAL_PROPERTY = "debug.session.cleanup.interval.minutes";
    private static final int DEFAULT_CLEANUP_INTERVAL = 1440; // 1440 minutes = 24 hours (default)

    private final ScheduledExecutorService scheduler;
    private final DebugSessionDAO debugSessionDAO;
    private final int cleanupIntervalMinutes;

    public DebugSessionCleanupService() {

        this(new DebugSessionDAOImpl());
    }

    /**
     * Constructs a new cleanup service with a custom DAO.
     * Useful for testing and dependency injection.
     *
     * @param debugSessionDAO Custom DAO implementation.
     */
    public DebugSessionCleanupService(DebugSessionDAO debugSessionDAO) {

        this.scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread cleanupThread = new Thread(runnable, "IdentityDebugSessionCleanupTask");
            cleanupThread.setDaemon(true);
            return cleanupThread;
        });
        this.debugSessionDAO = debugSessionDAO;
        this.cleanupIntervalMinutes = getConfiguredCleanupInterval();
    }

    /**
     * Loads the cleanup interval from system properties or environment variables.
     * Falls back to default (24 hours) if not configured.
     *
     * @return Cleanup interval in minutes.
     */
    private int getConfiguredCleanupInterval() {

        // Check system property first, then environment variable.
        String intervalStr = System.getProperty(CLEANUP_INTERVAL_PROPERTY,
                System.getenv("DEBUG_SESSION_CLEANUP_INTERVAL_MINUTES"));

        if (intervalStr != null && !intervalStr.trim().isEmpty()) {
            try {
                int interval = Integer.parseInt(intervalStr.trim());
                if (interval > 0) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Using configured debug session cleanup interval: " + interval + " minutes");
                    }
                    return interval;
                } else {
                    LOG.warn("Invalid debug session cleanup interval configured: " + interval
                            + ". Using default: " + DEFAULT_CLEANUP_INTERVAL + " minutes");
                }
            } catch (NumberFormatException e) {
                LOG.warn("Failed to parse debug session cleanup interval: " + intervalStr
                        + ". Using default: " + DEFAULT_CLEANUP_INTERVAL + " minutes", e);
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Using default debug session cleanup interval: " + DEFAULT_CLEANUP_INTERVAL + " minutes");
        }
        return DEFAULT_CLEANUP_INTERVAL;
    }

    /**
     * Starts the periodic cleanup task.
     * Uses scheduleAtFixedRate to maintain a consistent interval even if cleanup takes longer than expected.
     *
     * @throws IllegalStateException If the cleanup task cannot be scheduled.
     */
    public void activate() {

        try {
            scheduler.scheduleAtFixedRate(new DebugSessionCleanupTask(), INITIAL_DELAY,
                    cleanupIntervalMinutes, TimeUnit.MINUTES);
        } catch (RejectedExecutionException e) {
            throw new IllegalStateException("Failed to activate debug session cleanup service: "
                    + "scheduler rejected the cleanup task. The scheduler may have been shut down.", e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Debug session cleanup service activated with interval: " + cleanupIntervalMinutes + " minutes");
        }
    }

    /**
     * Shuts down the cleanup service.
     */
    public void deactivate() {

        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Debug session cleanup service deactivated.");
        }
    }

    /**
     * Runnable task to perform the cleanup.
     */
    private final class DebugSessionCleanupTask implements Runnable {

        @Override
        public void run() {

            if (LOG.isDebugEnabled()) {
                LOG.debug("Starting periodic cleanup of expired debug sessions.");
            }

            try {
                debugSessionDAO.deleteExpiredDebugSessions();
            } catch (DebugFrameworkServerException e) {
                LOG.error("Error occurred while performing periodic cleanup of expired debug sessions. Cause: " 
                        + e.getMessage());
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Stack trace for expired debug sessions cleanup failure: ", e);
                }
            }
        }
    }
}
