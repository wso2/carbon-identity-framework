/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.application.authentication.framework.store;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Database cleanup. Timer task is running for pre-defined period to clear the
 * DELETE operations and related STORE operations
 */
public final class OperationCleanUpService {

    private static final int NUM_THREADS = 1;
    private static final Log log = LogFactory.getLog(OperationCleanUpService.class);
    private final ScheduledExecutorService scheduler;
    private final long initialDelay;
    private final long delayBetweenRuns;

    // Time skew in minute
    private static final int defaultTimeSkew = 60;

    /**
     * @param initialDelay
     * @param delayBetweenRuns
     */
    public OperationCleanUpService(long initialDelay, long delayBetweenRuns) {
        this.initialDelay = initialDelay;
        this.delayBetweenRuns = delayBetweenRuns;
        this.scheduler = Executors.newScheduledThreadPool(NUM_THREADS);
    }

    public void activateCleanUp() {
        Runnable databaseCleanUpTask = new DatabaseOperationCleanUpTask();
        scheduler.scheduleWithFixedDelay(databaseCleanUpTask, initialDelay, delayBetweenRuns,
                                         TimeUnit.MINUTES);
    }

    private static final class DatabaseOperationCleanUpTask implements Runnable {

        @Override
        public void run() {

            log.debug("Start running the Session Operation Data cleanup task.");

            Date date = new Date();
            // Convert defaultTimeSkew (minutes) to milliseconds
            Timestamp timestamp = new Timestamp((date.getTime() - (defaultTimeSkew * 60 * 1000)));
            SessionDataStore.getInstance().removeExpiredOperationData(timestamp);
            log.debug("Stop running the Operation Data cleanup task.");
            log.info("Session Operation Data cleanup task is running successfully for removing expired Operation Data");
        }
    }
}
