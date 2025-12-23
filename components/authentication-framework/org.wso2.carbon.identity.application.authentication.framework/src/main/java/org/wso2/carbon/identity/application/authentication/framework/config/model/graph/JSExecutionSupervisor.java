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

import com.sun.management.ThreadMXBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Supervises the execution of any script engine, and kills the thread if the time taken is too much.
 */
public class JSExecutionSupervisor {

    private static final Log LOG = LogFactory.getLog(JSExecutionSupervisor.class);
    private static final String JS_EXECUTION_MONITOR = "JS-Exec-Monitor";
    private final long timeoutInMillis;
    private final long memoryLimitInBytes;
    private final boolean timeoutCheckEnabled;
    private long taskExecutionRateInMillis = 50L;
    private Map<String, TaskHolder> currentScriptExecutions = new HashMap<>();
    private ScheduledExecutorService monitoringService;
    private static final int MONITOR_TYPE_TIME = 0;
    private static final int MONITOR_TYPE_MEMORY = 1;
    private static final int WARN_THRESHOLD = 70;

    public JSExecutionSupervisor(int threadCount, long timeoutInMillis) {

        this(threadCount, true, timeoutInMillis, 0L);
    }

    public JSExecutionSupervisor(int threadCount, long timeoutInMillis, long memoryLimit) {

        this(threadCount, true, timeoutInMillis, memoryLimit);
    }

    /**
     * Create JS execution supervisor with timeout check. If timeoutCheckEnabled is false, no time based supervision
     * will be done.
     *
     * @param threadCount         Thread count for the monitoring service.
     * @param timeoutCheckEnabled Whether time based supervision should be done.
     * @param timeoutInMillis     Timeout in milliseconds. If the `timeoutCheckEnabled` is false, this value will
     *                            be ignored.
     * @param memoryLimit         Memory limit in bytes.
     */
    public JSExecutionSupervisor(int threadCount, boolean timeoutCheckEnabled, long timeoutInMillis, long memoryLimit) {

        if (taskExecutionRateInMillis > timeoutInMillis) {
            taskExecutionRateInMillis = timeoutInMillis;
        }

        this.timeoutCheckEnabled = timeoutCheckEnabled;
        this.timeoutInMillis = timeoutInMillis;

        if (memoryLimit > 0) {
            this.memoryLimitInBytes = memoryLimit;
        } else {
            // We are not checking for memory usage.
            memoryLimitInBytes = -1;
        }

        monitoringService = new ScheduledThreadPoolExecutor(threadCount, r -> new Thread(r, JS_EXECUTION_MONITOR));
    }

    /**
     * Shutdown the execution supervisor.
     */
    public void shutdown() {

        monitoringService.shutdown();
    }

    /**
     * Start monitoring an adaptive auth execution.
     *
     * @param identifier          Monitoring task identifier.
     * @param serviceProvider     Service provider of the adaptive auth script.
     * @param tenantDomain        Tenant domain.
     * @param elapsedTimeInMillis Elapsed time in milliseconds if a previous
     *                            adaptive auth execution happened in the same flow.
     */
    public void monitor(String identifier, String serviceProvider, String tenantDomain, long elapsedTimeInMillis) {

        monitor(identifier, serviceProvider, tenantDomain, elapsedTimeInMillis, 0L);
    }

    /**
     * Start monitoring an adaptive auth execution.
     *
     * @param identifier            Monitoring task identifier.
     * @param serviceProvider       Service provider of the adaptive auth script.
     * @param tenantDomain          Tenant domain.
     * @param elapsedTimeInMillis   Elapsed time in milliseconds if a previous
     *                              adaptive auth execution happened in the same flow.
     * @param consumedMemoryInBytes Consumed memory in bytes if a previous
     *                              adaptive auth execution happened in the same flow.
     */
    public void monitor(String identifier, String serviceProvider, String tenantDomain, long elapsedTimeInMillis,
                        long consumedMemoryInBytes) {

        MonitoringTask monitoringTask = new MonitoringTask(Thread.currentThread(), identifier, serviceProvider,
                tenantDomain, elapsedTimeInMillis, consumedMemoryInBytes);
        ScheduledFuture<?> monitoredFuture = monitoringService.
                scheduleAtFixedRate(monitoringTask, taskExecutionRateInMillis, taskExecutionRateInMillis,
                        TimeUnit.MILLISECONDS);
        currentScriptExecutions.put(identifier, new TaskHolder(monitoringTask, monitoredFuture));
    }

    /**
     * Mark adaptive auth script execution as complete and stop monitoring.
     *
     * @param identifier Monitoring task identifier.
     * @return Total elapsed time of adaptive auth execution in the current thread.
     */
    public JSExecutionMonitorData completed(String identifier) {

        TaskHolder taskHolder = currentScriptExecutions.remove(identifier);
        if (taskHolder == null) {
            // Nothing to be done as there was no such task with the given identifier.
            return null;
        }

        ScheduledFuture<?> monitoredFuture = taskHolder.getScheduledFuture();
        if (taskHolder.getScheduledFuture() != null) {
            monitoredFuture.cancel(true);
        }

        MonitoringTask task = taskHolder.getMonitoringTask();
        long elapsedTime = 0L;
        long consumedMemory = 0L;
        if (task != null) {
            elapsedTime = task.getTotalElapsedTime();
            consumedMemory = task.getTotalConsumedMemory();
            task.turnOffThreadMemoryCounting();
        }

        return new JSExecutionMonitorData(elapsedTime, consumedMemory);
    }

    private class TaskHolder {

        private MonitoringTask monitoringTask;
        private ScheduledFuture<?> scheduledFuture;

        public TaskHolder(MonitoringTask monitoringTask, ScheduledFuture<?> scheduledFuture) {

            this.monitoringTask = monitoringTask;
            this.scheduledFuture = scheduledFuture;
        }

        public MonitoringTask getMonitoringTask() {

            return monitoringTask;
        }

        public ScheduledFuture<?> getScheduledFuture() {

            return scheduledFuture;
        }
    }

    private class MonitoringTask implements Runnable {

        private Thread originalThread;
        private String id;
        private String serviceProvider;
        private String tenantDomain;
        private long timeCreated;
        private long elapsedTimeInMillis;
        private long startMemoryInBytes;
        private long consumedMemoryInBytes;
        private ThreadMXBean memoryCounter = null;

        public MonitoringTask(Thread originalThread, String id, String serviceProvider, String tenantDomain,
                              long elapsedTimeInMillis) {

            this(originalThread, id, serviceProvider, tenantDomain, elapsedTimeInMillis, 0L);
        }

        public MonitoringTask(Thread originalThread, String id, String serviceProvider, String tenantDomain,
                              long elapsedTimeInMillis, long consumedMemoryInBytes) {

            this.originalThread = originalThread;
            this.id = id;
            this.serviceProvider = serviceProvider;
            this.tenantDomain = tenantDomain;
            this.timeCreated = System.currentTimeMillis();
            this.elapsedTimeInMillis = elapsedTimeInMillis;
            this.consumedMemoryInBytes = consumedMemoryInBytes;

            if (memoryLimitInBytes > 0) {
                java.lang.management.ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
                if (threadMXBean instanceof com.sun.management.ThreadMXBean) {
                    memoryCounter = (ThreadMXBean) threadMXBean;
                    try {
                        turnOnThreadMemoryCounting();
                        startMemoryInBytes = getCurrentMemory(originalThread.getId());
                    } catch (UnsupportedOperationException e) {
                        LOG.error("Thread allocated memory measurement is not supported by the JVM. Therefore memory " +
                                "supervision will not be done for adaptive auth script executions.", e);
                    }
                } else {
                    LOG.error("Thread allocated memory measurement is not supported by the JVM. Therefore memory " +
                            "supervision will not be done for adaptive auth script executions.");
                }
            }
        }

        @Override
        public void run() {

            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("JS execution monitoring task running. Thread: %s, service " +
                        "provider: %s, tenant: %s.", originalThread.getName(), serviceProvider, tenantDomain));
            }

            long elapsedTime = getTotalElapsedTime();

            if (timeoutCheckEnabled && elapsedTime > timeoutInMillis) {
                terminateScriptExecutingThread(MONITOR_TYPE_TIME, elapsedTime);
                return;
            }

            if (timeoutCheckEnabled && isTimeBasedWarnThresholdReached(elapsedTime)) {
                printThresholdReachedWarnLog(MONITOR_TYPE_TIME, elapsedTime);
                return;
            }

            if (memoryLimitInBytes > 0 && memoryCounter != null) {
                long consumedMemory = getTotalConsumedMemory();
                if (consumedMemory > memoryLimitInBytes) {
                    terminateScriptExecutingThread(MONITOR_TYPE_MEMORY, consumedMemory);
                    return;
                }

                if (isMemoryBasedWarnThresholdReached(consumedMemory)) {
                    printThresholdReachedWarnLog(MONITOR_TYPE_MEMORY, consumedMemory);
                    return;
                }
            }
        }

        private void terminateScriptExecutingThread(int monitorType, long consumedResourceValue) {

            String warnLog;
            if (MONITOR_TYPE_TIME == monitorType) {
                warnLog = String.format("The script took too much time to execute. Thread: %s, service provider: %s, " +
                                "tenant: %s, execution duration: %s(ms).", originalThread.getName(), serviceProvider,
                        tenantDomain, consumedResourceValue);
            } else {
                warnLog = String.format("The script took too much memory to execute. Thread: %s, service provider: " +
                                "%s, tenant: %s, consumed memory: %s(bytes).", originalThread.getName(),
                        serviceProvider, tenantDomain, consumedResourceValue);
            }

            StackTraceElement[] stackTraceElements = originalThread.getStackTrace();
            Throwable throwable = new Throwable();
            throwable.setStackTrace(stackTraceElements);
            LOG.warn(warnLog, throwable);
            originalThread.interrupt();
            originalThread.stop();

            // Marking current monitoring task as complete.
            completed(id);
        }

        private void printThresholdReachedWarnLog(int monitorType, long consumedResourceValue) {

            String warnLog;
            if (MONITOR_TYPE_TIME == monitorType) {
                warnLog = String.format("The script has consumed over 70%% of the allocated time. Thread: %s, service" +
                        " provider: %s, tenant: %s, execution duration: %s(ms).", originalThread.getName(),
                        serviceProvider, tenantDomain, consumedResourceValue);
            } else {
                warnLog = String.format("The script has consumed over 70%% of the allocated memory. Thread: %s, " +
                        "service provider: %s, tenant: %s, consumed memory: %s(bytes).", originalThread.getName(),
                        serviceProvider, tenantDomain, consumedResourceValue);
            }

            LOG.warn(warnLog);
        }

        private long getTotalElapsedTime() {

            return (System.currentTimeMillis() - timeCreated) + elapsedTimeInMillis;
        }

        private long getTotalConsumedMemory() {

            return (getCurrentMemory(originalThread.getId()) - startMemoryInBytes) + consumedMemoryInBytes;
        }

        private long getCurrentMemory(long threadId) {

            if (memoryCounter != null) {
                return memoryCounter.getThreadAllocatedBytes(threadId);
            }
            return 0L;
        }

        private void turnOnThreadMemoryCounting() {

            if (memoryCounter != null) {
                memoryCounter.setThreadAllocatedMemoryEnabled(true);
            }
        }

        private void turnOffThreadMemoryCounting() {

            if (memoryCounter != null) {
                memoryCounter.setThreadAllocatedMemoryEnabled(false);
            }
        }

        private boolean isTimeBasedWarnThresholdReached(long elapsedTime) {

            return ((elapsedTime * 100) / timeoutInMillis) >= WARN_THRESHOLD;
        }

        private boolean isMemoryBasedWarnThresholdReached(long consumedMemory) {

            return ((consumedMemory * 100) / memoryLimitInBytes) >= WARN_THRESHOLD;
        }
    }
}
