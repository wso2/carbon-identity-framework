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

package org.wso2.carbon.identity.debug.framework.core.store;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.debug.framework.dao.DebugSessionDAO;
import org.wso2.carbon.identity.debug.framework.dao.impl.DebugSessionDAOImpl;
import org.wso2.carbon.identity.debug.framework.exception.DebugFrameworkServerException;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service to handle periodic cleanup of expired debug sessions.
 */
public class DebugSessionCleanupService {

    private static final Log LOG = LogFactory.getLog(DebugSessionCleanupService.class);

    private static final int INITIAL_DELAY = 1;
    private static final int DELAY_BETWEEN_RUNS = 1440; // 1440 minutes = 24 hours (once a day)

    private final ScheduledExecutorService scheduler;
    private final DebugSessionDAO debugSessionDAO;

    public DebugSessionCleanupService() {

        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.debugSessionDAO = new DebugSessionDAOImpl();
    }

    /**
     * Starts the periodic cleanup task.
     */
    public void activate() {

        scheduler.scheduleWithFixedDelay(new DebugSessionCleanupTask(), INITIAL_DELAY,
                DELAY_BETWEEN_RUNS, TimeUnit.MINUTES);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Debug session cleanup service activated.");
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
     * Runnable task to perform the actual cleanup.
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
                LOG.error("Error occurred while performing periodic cleanup of expired debug sessions.", e);
            }
        }
    }
}
