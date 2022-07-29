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

import java.util.HashMap;
import java.util.UUID;

/**
 * Test class for JSExecutionSupervisor.
 */
public class JSExecutionSupervisorTest {

    private static final Log LOG = LogFactory.getLog(JSExecutionSupervisor.class);

    @Test
    public void testMonitor() {

        long timeout = 100L;
        long memoryLimit = 50000L;

        JSExecutionSupervisor supervisor = new JSExecutionSupervisor(1, timeout, memoryLimit);
        try {
            String identifier = UUID.randomUUID().toString();
            supervisor.monitor(identifier, "dummySP", "dummyTenant", 0L, 0L);
            long executionDuration = 50L;
            Thread.sleep(executionDuration);

            JSExecutionMonitorData result = supervisor.completed(identifier);

            Assert.assertNotNull("The execution monitor result should not be null.", result);
            long elapsedTime = result.getElapsedTime();
            Assert.assertTrue("Elapsed time should equal or greater than " + executionDuration
                    + " but the recorded elapsed time is: " + elapsedTime, elapsedTime >= executionDuration);
            long consumedMemory = result.getConsumedMemory();
            Assert.assertTrue("Conusumed memory should be greater than 0. Consumed memory: " + consumedMemory,
                    consumedMemory > 0);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            Assert.fail("This is within the valid execution period but the monitor has killed the thread. " +
                    "Error message: " + e.getMessage());
        } finally {
            supervisor.shutdown();
        }
    }

    @Test
    public void testTimeBasedMonitorNegative() throws InterruptedException {

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
    public void testTimeBasedMonitorWithAlreadyElapsedTime() {

        JSExecutionSupervisor supervisor = new JSExecutionSupervisor(1, 2000L);
        try {
            String identifier = UUID.randomUUID().toString();
            long elapsedTime = 1000L;
            supervisor.monitor(identifier, "dummySP", "dummyTenant", elapsedTime);
            long executionDuration = 50L;
            Thread.sleep(executionDuration);

            JSExecutionMonitorData result = supervisor.completed(identifier);
            long totalElapsedTime = result.getElapsedTime();
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

    @Test
    public void testMemoryBasedMonitorNegative() throws InterruptedException {

        final JSExecutionSupervisor supervisor = new JSExecutionSupervisor(1, 5000L, 4000L);
        Thread testExecutionThread = null;
        try {
            testExecutionThread = new Thread(() -> {
                try {
                    String identifier = UUID.randomUUID().toString();
                    supervisor.monitor(identifier, "dummySP", "dummyTenant", 0L, 0L);
                    HashMap<Object, Object> objectHashMap = new HashMap<>();
                    for (int i = 0; i < 200; i++) {
                        Thread.sleep(1);
                        objectHashMap.put(new Object(), new Object());
                    }
                } catch (Exception ignored) {
                    // We are expecting that a exception will be thrown as the monitor will kill the thread.
                }
            });
            testExecutionThread.start();
            // Sleeping until the test completes.
            Thread.sleep(300L);
            Assert.assertFalse("The monitor should have killed the testExecutionThread but it didn't happen.",
                    testExecutionThread.isAlive());
        } finally {
            if (testExecutionThread != null && testExecutionThread.isAlive()) {
                testExecutionThread.interrupt();
                testExecutionThread.stop();
            }
            supervisor.shutdown();
        }
    }

    @Test
    public void testMemoryBasedMonitorWithAlreadyConsumedMemory() {

        long memoryLimit = 60000000L; // 60mb;
        JSExecutionSupervisor supervisor = new JSExecutionSupervisor(1, 2000L, memoryLimit);
        try {
            String identifier = UUID.randomUUID().toString();
            long consumedMemory = 50000000L; // 50mb
            supervisor.monitor(identifier, "dummySP", "dummyTenant", 0L, consumedMemory);
            JSExecutionMonitorData result = supervisor.completed(identifier);
            long totalConsumedMemory = result.getConsumedMemory();
            Assert.assertTrue("Consumed memory should be greater than " + consumedMemory + " but the recorded " +
                    "consumed memory is: " + totalConsumedMemory, totalConsumedMemory >= consumedMemory);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            Assert.fail("This is within the valid execution period but the monitor has killed the thread. " +
                    "Error message: " + e.getMessage());
        } finally {
            supervisor.shutdown();
        }
    }
}
