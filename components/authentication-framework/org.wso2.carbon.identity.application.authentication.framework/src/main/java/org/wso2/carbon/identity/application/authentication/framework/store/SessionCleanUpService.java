/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Database cleanup. Timer task is running for pre-defined period to clear the
 * invalid sessions
 */
public final class SessionCleanUpService {

    private static final int NUM_THREADS = 1;
    private static final Log log = LogFactory.getLog(SessionCleanUpService.class);
    private final ScheduledExecutorService scheduler;
    private final long initialDelay;
    private final long delayBetweenRuns;

    /**
     * @param initialDelay
     * @param delayBetweenRuns
     */
    public SessionCleanUpService(long initialDelay, long delayBetweenRuns) {
        this.initialDelay = initialDelay;
        this.delayBetweenRuns = delayBetweenRuns;
        this.scheduler = Executors.newScheduledThreadPool(NUM_THREADS);
    }

    /**
     *
     */
    public void activateCleanUp() {
        Runnable databaseCleanUpTask = new DatabaseCleanUpTask();
        scheduler.scheduleWithFixedDelay(databaseCleanUpTask, initialDelay, delayBetweenRuns,
                                         TimeUnit.MINUTES);

    }

    /**
     *
     *
     */
    private static final class DatabaseCleanUpTask implements Runnable {

        @Override
        public void run() {

            log.debug("Start running the Session Data cleanup task.");
            Date date = new Date();
            int sessionTimeout = IdentityUtil.getCleanUpTimeout();
            Timestamp timestamp = new Timestamp((date.getTime() - (sessionTimeout * 60 * 1000)));
            SessionDataStore.getInstance().removeExpiredSessionData(timestamp);
            log.debug("Stop running the Session Data cleanup task.");
            log.info("Session Data cleanup task is running successfully for removing expired Data");
        }
    }
}
