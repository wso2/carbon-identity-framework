/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
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
package org.wso2.carbon.identity.application.authentication.framework.store;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Database cleanup. Timer task is running for pre-defined period to clear the
 * expired pushed authorization requests
 */
public class PushedAuthReqCleanupService {

    private static final int NUM_THREADS = 1;
    private static final Log log = LogFactory.getLog(PushedAuthReqCleanupService.class);
    private final ScheduledExecutorService scheduler;
    private final long initialDelay;
    private final long delayBetweenRuns;

    /**
     * Constructor for PushedAuthReqCleanupService
     *
     * @param initialDelay     Initial delay in minutes
     * @param delayBetweenRuns Delay between runs in minutes
     */
    public PushedAuthReqCleanupService(long initialDelay, long delayBetweenRuns) {

        this.initialDelay = initialDelay;
        this.delayBetweenRuns = delayBetweenRuns;
        this.scheduler = Executors.newScheduledThreadPool(NUM_THREADS);
    }

    /**
     * Start the cleanup task
     */
    public void activateCleanUp() {

        Runnable databaseCleanUpTask = new PushedAuthReqCleanupService.DatabaseCleanUpTask();
        scheduler.scheduleWithFixedDelay(databaseCleanUpTask, initialDelay, delayBetweenRuns, TimeUnit.MINUTES);
    }

    /**
     * Database cleanup task
     */
    private static final class DatabaseCleanUpTask implements Runnable {

        @Override
        public void run() {

            log.debug("Start running the Pushed authorization request cleanup task.");
            PushedAuthDataStore.getInstance().removeExpiredRequests();
            log.info("Pushed authorization request cleanup task is running successfully for removing expired requests");
        }
    }
}
