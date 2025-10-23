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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Enforces script execution alerts and blocks tenants exceeding thresholds.
 */
public class JSExecutionEnforcer {

    private static final Log LOG = LogFactory.getLog(JSExecutionEnforcer.class);
    private static final String ALERT_CLEANUP_THREAD = "JS-Alert-Cleanup";
    private final Map<String, List<JSExecutionAlert>> tenantAlerts = new ConcurrentHashMap<>();
    private final Set<String> blockedTenants = new CopyOnWriteArraySet<>();
    private final Map<String, Long> blockTimestamps = new ConcurrentHashMap<>();
    private final int maxViolationsPerTenant;
    private final long violationWindowInMillis;
    private final long blockDurationInMillis;
    private final long criticalMemoryLimit;
    private final ScheduledExecutorService cleanupService;
    private final TenantViolationStore tenantViolationStore;

    /**
     * Constructor with configuration parameters.
     *
     * @param maxViolationsPerTenant    Maximum number of alerts allowed per tenant in the time window.
     * @param violationWindowInMillis   Time window in milliseconds for counting alerts (alert expiry duration).
     * @param blockDurationInMillis Duration in milliseconds for which a tenant should be blocked.
     * @param criticalMemoryLimit   Memory limit in bytes that will block a tenant with a single violation.
     */
    public JSExecutionEnforcer(int maxViolationsPerTenant, long violationWindowInMillis,
                               long blockDurationInMillis, long criticalMemoryLimit) {

        this.maxViolationsPerTenant = maxViolationsPerTenant;
        this.violationWindowInMillis = violationWindowInMillis;
        this.blockDurationInMillis = blockDurationInMillis;
        this.criticalMemoryLimit = criticalMemoryLimit;
        this.tenantViolationStore = new TenantViolationStore();

        // Start cleanup service to remove expired alerts and unblock tenants.
        // Run cleanup at half the violation window interval to ensure timely cleanup.
        long cleanupIntervalInMillis = Math.max(violationWindowInMillis / 2, 60000L); // Minimum 1 minute.
        cleanupService = Executors.newSingleThreadScheduledExecutor(r ->
                new Thread(r, ALERT_CLEANUP_THREAD));
        cleanupService.scheduleAtFixedRate(this::cleanupExpiredAlerts,
                cleanupIntervalInMillis, cleanupIntervalInMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * Create a new instance of JSExecutionEnforcer by loading configuration from the system.
     *
     * @return A new instance of JSExecutionEnforcer with loaded configuration.
     */
    public static JSExecutionEnforcer createFromConfiguration() {

        int maxViolationsPerTenant = loadMaxViolationsPerTenant();
        long violationWindowInMillis = loadViolationWindow();
        long blockDurationInMillis = loadBlockDuration();
        long criticalMemoryLimit = loadCriticalMemoryLimit();

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Initialized JS Execution Alert Service with config: " +
                            "maxViolationsPerTenant=%d, violationWindowInMillis=%d, blockDurationInMillis=%d, " +
                            "criticalMemoryLimit=%d",
                    maxViolationsPerTenant, violationWindowInMillis, blockDurationInMillis, criticalMemoryLimit));
        }

        return new JSExecutionEnforcer(maxViolationsPerTenant, violationWindowInMillis, blockDurationInMillis,
                criticalMemoryLimit);
    }

    /**
     * Load max violations per tenant configuration.
     *
     * @return Maximum violations per tenant.
     */
    private static int loadMaxViolationsPerTenant() {

        String maxViolationsPerTenantString = IdentityUtil.getProperty(
                FrameworkConstants.AdaptiveAuthentication.CONF_EXECUTION_SUPERVISOR_MAX_VIOLATIONS_PER_TENANT);
        int maxViolationsPerTenant = FrameworkConstants.AdaptiveAuthentication.DEFAULT_ALERT_MAX_VIOLATIONS_PER_TENANT;

        if (StringUtils.isNotBlank(maxViolationsPerTenantString)) {
            try {
                maxViolationsPerTenant = Integer.parseInt(maxViolationsPerTenantString);
            } catch (NumberFormatException e) {
                LOG.error("Error while parsing adaptive authentication max violations per tenant config: "
                        + maxViolationsPerTenantString + ", setting to default value: " + maxViolationsPerTenant, e);
            }
        }

        return maxViolationsPerTenant;
    }

    /**
     * Load violation window configuration.
     *
     * @return Violation window in milliseconds.
     */
    private static long loadViolationWindow() {

        String alertWindowString = IdentityUtil.getProperty(
                FrameworkConstants.AdaptiveAuthentication.CONF_EXECUTION_SUPERVISOR_VIOLATION_TRACKING_WINDOW_MILLIS);
        long alertWindowInMillis = FrameworkConstants.AdaptiveAuthentication
                .DEFAULT_EXECUTION_SUPERVISOR_VIOLATION_TRACKING_WINDOW_MILLIS;

        if (StringUtils.isNotBlank(alertWindowString)) {
            try {
                alertWindowInMillis = Long.parseLong(alertWindowString);
            } catch (NumberFormatException e) {
                LOG.error("Error while parsing adaptive authentication violation tracking window config: " +
                        alertWindowString + ", setting to default value: " + alertWindowInMillis, e);
            }
        }

        return alertWindowInMillis;
    }

    /**
     * Load block duration configuration.
     *
     * @return Block duration in milliseconds.
     */
    private static long loadBlockDuration() {

        String blockDurationString = IdentityUtil.getProperty(
                FrameworkConstants.AdaptiveAuthentication.CONF_EXECUTION_SUPERVISOR_BLOCK_DURATION_MILLIS);
        long blockDurationInMillis = FrameworkConstants.AdaptiveAuthentication
                .DEFAULT_EXECUTION_SUPERVISOR_BLOCK_DURATION_MILLIS;

        if (StringUtils.isNotBlank(blockDurationString)) {
            try {
                blockDurationInMillis = Long.parseLong(blockDurationString);
            } catch (NumberFormatException e) {
                LOG.error("Error while parsing adaptive authentication alert block duration config: "
                        + blockDurationString + ", setting to default value: " + blockDurationInMillis, e);
            }
        }

        return blockDurationInMillis;
    }

    /**
     * Load critical memory limit configuration.
     *
     * @return Critical memory limit in bytes.
     */
    private static long loadCriticalMemoryLimit() {

        String criticalMemoryLimitString = IdentityUtil.getProperty(
                FrameworkConstants.AdaptiveAuthentication.CONF_EXECUTION_SUPERVISOR_CRITICAL_MEMORY_LIMIT);
        long criticalMemoryLimit = FrameworkConstants.AdaptiveAuthentication.DEFAULT_ALERT_CRITICAL_MEMORY_LIMIT;

        if (StringUtils.isNotBlank(criticalMemoryLimitString)) {
            try {
                criticalMemoryLimit = Long.parseLong(criticalMemoryLimitString);
            } catch (NumberFormatException e) {
                LOG.error("Error while parsing adaptive authentication critical memory limit config: "
                        + criticalMemoryLimitString + ", setting to default value: " + criticalMemoryLimit, e);
            }
        }

        return criticalMemoryLimit;
    }

    public void pushAlert(JSExecutionAlert alert) {

        if (alert == null || alert.getTenantDomain() == null) {
            return;
        }

        String tenantDomain = alert.getTenantDomain();

        // Check if this is a memory limit violation that exceeds the immediate block threshold.
        if (alert.getAlertType() == JSExecutionAlert.AlertType.MEMORY_LIMIT_EXCEEDED
                && criticalMemoryLimit > 0 && alert.getResourceValue() >= criticalMemoryLimit) {
            // Block tenant immediately for severe memory violations.
            blockTenant(tenantDomain);
            tenantViolationStore.blockTenant(tenantDomain);
            LOG.warn(String.format("Tenant '%s' has been immediately blocked due to excessive memory usage: %d bytes " +
                            "(threshold: %d bytes) in service provider '%s'.",
                    tenantDomain, alert.getResourceValue(), criticalMemoryLimit, alert.getServiceProvider()));
            return;
        }

        tenantAlerts.computeIfAbsent(tenantDomain, k -> new ArrayList<>()).add(alert);

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Alert pushed for tenant: %s, type: %s, SP: %s",
                    tenantDomain, alert.getAlertType(), alert.getServiceProvider()));
        }

        int nonExpiredAlertCount = getNonExpiredAlertCount(tenantDomain);

        // Update violation count in configuration management.
        tenantViolationStore.updateViolationCount(tenantDomain, nonExpiredAlertCount);

        if (nonExpiredAlertCount >= maxViolationsPerTenant && !blockedTenants.contains(tenantDomain)) {
            blockTenant(tenantDomain);
            tenantViolationStore.blockTenant(tenantDomain);
            LOG.warn(String.format("Tenant '%s' has been blocked due to %d script execution alerts " +
                            "exceeding the threshold of %d alerts within %d milliseconds.",
                    tenantDomain, nonExpiredAlertCount, maxViolationsPerTenant, violationWindowInMillis));
        }
    }

    public boolean isTenantBlocked(String tenantDomain) {

        if (tenantDomain == null) {
            return false;
        }

        // Check in-memory blocked status first.
        if (blockedTenants.contains(tenantDomain)) {
            Long blockTime = blockTimestamps.get(tenantDomain);
            if (blockTime != null) {
                long elapsedTime = System.currentTimeMillis() - blockTime;
                if (elapsedTime >= blockDurationInMillis) {
                    unblockTenant(tenantDomain);
                    return false;
                }
            }
            return true;
        }

        // Check persisted blocked status in configuration management.
        if (tenantViolationStore.isBlocked(tenantDomain)) {
            long blockTime = tenantViolationStore.getBlockTimestamp(tenantDomain);
            if (blockTime > 0) {
                long elapsedTime = System.currentTimeMillis() - blockTime;
                if (elapsedTime >= blockDurationInMillis) {
                    unblockTenant(tenantDomain);
                    return false;
                }
            }
            // Sync in-memory state with persisted state.
            blockedTenants.add(tenantDomain);
            blockTimestamps.put(tenantDomain, blockTime);
            return true;
        }

        return false;
    }

    public int getAlertCount(String tenantDomain) {

        return getNonExpiredAlertCount(tenantDomain);
    }

    public void clearAlerts(String tenantDomain) {

        if (tenantDomain == null) {
            return;
        }

        tenantAlerts.remove(tenantDomain);
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Cleared alerts for tenant: %s", tenantDomain));
        }
    }

    public void blockTenant(String tenantDomain) {

        if (tenantDomain == null) {
            return;
        }

        blockedTenants.add(tenantDomain);
        blockTimestamps.put(tenantDomain, System.currentTimeMillis());
        LOG.warn(String.format("Tenant '%s' has been blocked from login due to excessive script execution alerts.",
                tenantDomain));
    }

    public void unblockTenant(String tenantDomain) {

        if (tenantDomain == null) {
            return;
        }

        blockedTenants.remove(tenantDomain);
        blockTimestamps.remove(tenantDomain);
        clearAlerts(tenantDomain);

        // Clear persisted data in configuration management.
        tenantViolationStore.unblockTenant(tenantDomain);

        LOG.info(String.format("Tenant '%s' has been unblocked.", tenantDomain));
    }

    /**
     * Get the count of non-expired alerts for a tenant.
     * An alert is considered expired if it's older than the alert window duration.
     *
     * @param tenantDomain Tenant domain.
     * @return Number of non-expired alerts.
     */
    private int getNonExpiredAlertCount(String tenantDomain) {

        if (tenantDomain == null) {
            return 0;
        }

        List<JSExecutionAlert> alerts = tenantAlerts.get(tenantDomain);
        if (alerts == null) {
            return 0;
        }

        long currentTime = System.currentTimeMillis();
        long expiryThreshold = currentTime - violationWindowInMillis;

        synchronized (alerts) {
            return (int) alerts.stream()
                    .filter(alert -> alert.getTimestamp() >= expiryThreshold)
                    .count();
        }
    }

    /**
     * Cleanup expired alerts and check for expired blocks.
     */
    private void cleanupExpiredAlerts() {

        try {
            long currentTime = System.currentTimeMillis();
            long expiryThreshold = currentTime - violationWindowInMillis;

            for (Map.Entry<String, List<JSExecutionAlert>> entry : tenantAlerts.entrySet()) {
                List<JSExecutionAlert> alerts = entry.getValue();
                synchronized (alerts) {
                    alerts.removeIf(alert -> alert.getTimestamp() < expiryThreshold);
                }

                if (alerts.isEmpty()) {
                    tenantAlerts.remove(entry.getKey());
                }
            }

            for (String tenantDomain : new ArrayList<>(blockedTenants)) {
                isTenantBlocked(tenantDomain);
            }

        } catch (Exception e) {
            LOG.error("Error during alert cleanup.", e);
        }
    }

    /**
     * Shutdown the cleanup service.
     */
    public void shutdown() {

        if (cleanupService != null) {
            cleanupService.shutdown();
            try {
                if (!cleanupService.awaitTermination(5, TimeUnit.SECONDS)) {
                    cleanupService.shutdownNow();
                }
            } catch (InterruptedException e) {
                cleanupService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
