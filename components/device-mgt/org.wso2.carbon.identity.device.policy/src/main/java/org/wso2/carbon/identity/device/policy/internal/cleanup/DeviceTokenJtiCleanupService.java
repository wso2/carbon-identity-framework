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

package org.wso2.carbon.identity.device.policy.internal.cleanup;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.device.policy.api.exception.DevicePolicyServerException;
import org.wso2.carbon.identity.device.policy.internal.service.impl.DeviceTokenReplayProtectionService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Scheduled task that periodically removes expired device token jti records from the replay store.
 * Modeled on the framework's pushed authorization request cleanup task.
 */
public class DeviceTokenJtiCleanupService {

    private static final int NUM_THREADS = 1;
    private static final Log LOG = LogFactory.getLog(DeviceTokenJtiCleanupService.class);
    private final ScheduledExecutorService scheduler;
    private final long initialDelay;
    private final long delayBetweenRuns;

    /**
     * Constructor for DeviceTokenJtiCleanupService.
     *
     * @param initialDelay     Initial delay in minutes before the first run.
     * @param delayBetweenRuns Delay between runs in minutes.
     */
    public DeviceTokenJtiCleanupService(long initialDelay, long delayBetweenRuns) {

        this.initialDelay = initialDelay;
        this.delayBetweenRuns = delayBetweenRuns;
        this.scheduler = Executors.newScheduledThreadPool(NUM_THREADS);
    }

    /**
     * Starts the cleanup task.
     */
    public void activateCleanUp() {

        scheduler.scheduleWithFixedDelay(
                new DeviceTokenJtiCleanupTask(), initialDelay, delayBetweenRuns, TimeUnit.MINUTES);
    }

    /**
     * Stops the cleanup task. Called when the bundle is deactivated.
     */
    public void shutdown() {

        scheduler.shutdownNow();
    }

    /**
     * Device token jti cleanup task.
     */
    private static final class DeviceTokenJtiCleanupTask implements Runnable {

        private final DeviceTokenReplayProtectionService replayService =
                DeviceTokenReplayProtectionService.getInstance();

        @Override
        public void run() {

            if (LOG.isDebugEnabled()) {
                LOG.debug("Running the device token jti cleanup task.");
            }
            try {
                replayService.removeExpiredTokens();
            } catch (DevicePolicyServerException e) {
                LOG.error("Error while running the device token jti cleanup task.", e);
            }
        }
    }
}
