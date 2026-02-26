/*
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

package org.wso2.carbon.identity.core.circuitbreaker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Objects;
import java.util.function.Function;

/**
 * Property-based configuration loader for {@link Policy}.
 */
public final class PolicyConfiguration {

    private static final Log LOG = LogFactory.getLog(PolicyConfiguration.class);

    private static final String PROPERTY_ENABLED = "Enabled";
    private static final String PROPERTY_WINDOW_SIZE = "WindowSize";
    private static final String PROPERTY_MIN_CALLS = "MinCallsToEvaluate";
    private static final String PROPERTY_FAILURE_RATE = "FailureRateThreshold";
    private static final String PROPERTY_OPEN_DURATION = "OpenDurationMs";
    private static final String PROPERTY_MAX_IN_FLIGHT = "MaxInFlight";
    private static final String PROPERTY_IDLE_EVICT_MS = "TenantEntryIdleEvictMs";
    private static final String PROPERTY_MAX_TENANTS = "MaxTenantsInCache";
    private static final String PROPERTY_CLEANUP_INTERVAL = "CleanupEveryRequests";
    private static final String PROPERTY_EVICTION_SCAN_LIMIT = "EvictionScanLimit";
    private static final String PROPERTY_HARD_CAP_EVICTION_SCAN_LIMIT = "HardCapEvictionScanLimit";
    private static final String PROPERTY_CACHE_STRIPES = "CacheStripes";

    private PolicyConfiguration() {

    }

    public static Policy fromProperties(String propertyPrefix, Function<String, String> propertyResolver) {

        return fromProperties(propertyPrefix, propertyResolver, Policy.builder().build());
    }

    public static Policy fromProperties(String propertyPrefix, Function<String, String> propertyResolver,
                                        Policy defaults) {

        Objects.requireNonNull(propertyResolver, "propertyResolver cannot be null");
        Objects.requireNonNull(defaults, "defaults cannot be null");

        String prefix = normalizePrefix(propertyPrefix);
        Policy.Builder builder = Policy.builder();
        builder.setEnabled(parseBoolean(resolve(prefix, PROPERTY_ENABLED, propertyResolver), defaults.isEnabled()));
        builder.setWindowSize(parseInt(prefix + PROPERTY_WINDOW_SIZE,
                resolve(prefix, PROPERTY_WINDOW_SIZE, propertyResolver), defaults.getWindowSize(), 1));
        builder.setMinCallsToEvaluate(parseInt(prefix + PROPERTY_MIN_CALLS,
                resolve(prefix, PROPERTY_MIN_CALLS, propertyResolver), defaults.getMinCallsToEvaluate(), 1));
        builder.setFailureRateThreshold(parseDouble(prefix + PROPERTY_FAILURE_RATE,
                resolve(prefix, PROPERTY_FAILURE_RATE, propertyResolver), defaults.getFailureRateThreshold(), 0D, 1D));
        builder.setOpenDurationMs(parseLong(prefix + PROPERTY_OPEN_DURATION,
                resolve(prefix, PROPERTY_OPEN_DURATION, propertyResolver), defaults.getOpenDurationMs(), 1L));
        builder.setMaxInFlight(parseInt(prefix + PROPERTY_MAX_IN_FLIGHT,
                resolve(prefix, PROPERTY_MAX_IN_FLIGHT, propertyResolver), defaults.getMaxInFlight(), 1));
        builder.setTenantEntryIdleEvictMs(parseLong(prefix + PROPERTY_IDLE_EVICT_MS,
                resolve(prefix, PROPERTY_IDLE_EVICT_MS, propertyResolver), defaults.getTenantEntryIdleEvictMs(), 1L));
        builder.setMaxTenantsInCache(parseInt(prefix + PROPERTY_MAX_TENANTS,
                resolve(prefix, PROPERTY_MAX_TENANTS, propertyResolver), defaults.getMaxTenantsInCache(), 1));
        builder.setCleanupTriggerEveryRequests(parseInt(prefix + PROPERTY_CLEANUP_INTERVAL,
                resolve(prefix, PROPERTY_CLEANUP_INTERVAL, propertyResolver), defaults.getCleanupTriggerEveryRequests(),
                1));
        builder.setEvictionScanLimit(parseInt(prefix + PROPERTY_EVICTION_SCAN_LIMIT,
                resolve(prefix, PROPERTY_EVICTION_SCAN_LIMIT, propertyResolver), defaults.getEvictionScanLimit(), 1));
        builder.setHardCapEvictionScanLimit(parseInt(prefix + PROPERTY_HARD_CAP_EVICTION_SCAN_LIMIT,
                resolve(prefix, PROPERTY_HARD_CAP_EVICTION_SCAN_LIMIT, propertyResolver),
                defaults.getHardCapEvictionScanLimit(), 1));
        builder.setCacheStripes(parseInt(prefix + PROPERTY_CACHE_STRIPES,
                resolve(prefix, PROPERTY_CACHE_STRIPES, propertyResolver), defaults.getCacheStripes(), 1));
        return builder.build();
    }

    private static String normalizePrefix(String propertyPrefix) {

        if (isBlank(propertyPrefix)) {
            return "";
        }

        String trimmed = propertyPrefix.trim();
        return trimmed.endsWith(".") ? trimmed : trimmed + ".";
    }

    private static String resolve(String prefix, String propertySuffix, Function<String, String> resolver) {

        return resolver.apply(prefix + propertySuffix);
    }

    private static boolean parseBoolean(String configuredValue, boolean defaultValue) {

        if (isBlank(configuredValue)) {
            return defaultValue;
        }
        return Boolean.parseBoolean(configuredValue.trim());
    }

    private static int parseInt(String propertyName, String configuredValue, int defaultValue, int min) {

        if (isBlank(configuredValue)) {
            return defaultValue;
        }

        try {
            return Math.max(Integer.parseInt(configuredValue.trim()), min);
        } catch (NumberFormatException exception) {
            LOG.warn("Invalid integer for property: " + propertyName + ". Falling back to default.", exception);
            return defaultValue;
        }
    }

    private static long parseLong(String propertyName, String configuredValue, long defaultValue, long min) {

        if (isBlank(configuredValue)) {
            return defaultValue;
        }

        try {
            return Math.max(Long.parseLong(configuredValue.trim()), min);
        } catch (NumberFormatException exception) {
            LOG.warn("Invalid long for property: " + propertyName + ". Falling back to default.", exception);
            return defaultValue;
        }
    }

    private static double parseDouble(String propertyName, String configuredValue, double defaultValue,
                                      double min, double max) {

        if (isBlank(configuredValue)) {
            return defaultValue;
        }

        try {
            return Math.max(Math.min(Double.parseDouble(configuredValue.trim()), max), min);
        } catch (NumberFormatException exception) {
            LOG.warn("Invalid double for property: " + propertyName + ". Falling back to default.", exception);
            return defaultValue;
        }
    }

    private static boolean isBlank(String value) {

        return value == null || value.trim().isEmpty();
    }
}
