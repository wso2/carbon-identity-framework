/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.testng.annotations.Test;

import java.util.UUID;

/**
 * Test class for JSExecutionSupervisor.
 */
public class JSExecutionSupervisorTest {

    private static final Log LOG = LogFactory.getLog(JSExecutionSupervisor.class);

    @Test
    public void testMonitor() {

        JSExecutionSupervisor supervisor = new JSExecutionSupervisor(1, 100L);
        try {
            String identifier = UUID.randomUUID().toString();
            supervisor.monitor(identifier, "dummySP", "dummyTenant", 0L);
            long executionDuration = 50L;
            Thread.sleep(executionDuration);

            long elapsedTime = supervisor.completed(identifier);
            Assert.assertTrue("Elapsed time should equal or greater than " + executionDuration
                    + " but the recorded elapsed time is: " + elapsedTime, elapsedTime >= executionDuration);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            Assert.fail("This is within the valid execution period but the monitor has killed the thread. " +
                    "Error message: " + e.getMessage());
        } finally {
            supervisor.shutdown();
        }
    }

    @Test
    public void testMonitorNegative() throws InterruptedException {

        final JSExecutionSupervisor supervisor = new JSExecutionSupervisor(1, 50L);
        try {
            Thread testExecutionThread = new Thread(() -> {
                try {
                    String identifier = UUID.randomUUID().toString();
                    supervisor.monitor(identifier, "dummySP", "dummyTenant", 0L);
                    Thread.sleep(2000L);
                } catch (InterruptedException ignored) {
                    // We are expecting that a exception will be thrown as the monitor will kill the thread.
                }
            });
            testExecutionThread.start();

            // Sleeping until the test completes.
            Thread.sleep(300L);

            Assert.assertFalse("The monitor should have killed the testExecutionThread but it didn't happen.",
                    testExecutionThread.isAlive());
        } finally {
            supervisor.shutdown();
        }
    }

    @Test
    public void testMonitorWithAlreadyElapsedTime() {

        JSExecutionSupervisor supervisor = new JSExecutionSupervisor(1, 2000L);
        try {
            String identifier = UUID.randomUUID().toString();
            long elapsedTime = 1000L;
            supervisor.monitor(identifier, "dummySP", "dummyTenant", elapsedTime);
            long executionDuration = 50L;
            Thread.sleep(executionDuration);

            long totalElapsedTime = supervisor.completed(identifier);
            long totalExecutionDuration = elapsedTime + executionDuration;
            Assert.assertTrue("Elapsed time should be equal or greater than " + executionDuration
                    + " but the recorded elapsed time is: " + elapsedTime, totalElapsedTime >= totalExecutionDuration);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            Assert.fail("This is within the valid execution period but the monitor has killed the thread. " +
                    "Error message: " + e.getMessage());
        } finally {
            supervisor.shutdown();
        }
    }
}
