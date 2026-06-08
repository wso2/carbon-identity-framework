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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityUtil;

/**
 * Loads the default {@link StaticPolicy} and {@link RuntimePolicy} from identity.xml.
 */
public final class DefaultPolicyConfigurationLoader {

    private static final Log LOG = LogFactory.getLog(DefaultPolicyConfigurationLoader.class);

    private DefaultPolicyConfigurationLoader() {

    }

    /**
     * Returns the static policy loaded from identity.xml at startup.
     *
     * @return the {@link StaticPolicy} instance.
     */
    public static StaticPolicy getStaticPolicy() {

        return Holder.STATIC_POLICY;
    }

    /**
     * Returns the runtime policy loaded from identity.xml at startup.
     *
     * @return the {@link RuntimePolicy} instance.
     */
    public static RuntimePolicy getRuntimePolicy() {

        return Holder.RUNTIME_POLICY;
    }

    private static StaticPolicy loadStaticPolicy() {

        return StaticPolicy.builder()
                .setEnabled(parseBoolean(CircuitBreakerConstants.PropertyKeys.ENABLED,
                        CircuitBreakerConstants.Defaults.ENABLED))
                .setTenantServiceCacheCapacity(parseInt(CircuitBreakerConstants.PropertyKeys.TENANT_SERVICE_CACHE_CAPACITY,
                        CircuitBreakerConstants.Defaults.TENANT_SERVICE_CACHE_CAPACITY, 1))
                .setTenantServiceEvictionThreshold(parseDouble(CircuitBreakerConstants.PropertyKeys.TENANT_SERVICE_EVICTION_THRESHOLD,
                        CircuitBreakerConstants.Defaults.TENANT_SERVICE_EVICTION_THRESHOLD, 0.0, 1.0))
                .setTenantServiceEntryIdleTimeout(parseLong(CircuitBreakerConstants.PropertyKeys.TENANT_SERVICE_ENTRY_IDLE_TIMEOUT,
                        CircuitBreakerConstants.Defaults.TENANT_SERVICE_ENTRY_IDLE_TIMEOUT, 1L))
                .build();
    }

    private static RuntimePolicy loadRuntimePolicy() {

        return RuntimePolicy.builder()
                .setWindowSize(parseInt(CircuitBreakerConstants.PropertyKeys.WINDOW_SIZE,
                        CircuitBreakerConstants.Defaults.WINDOW_SIZE, 1))
                .setMinCallsToEvaluate(parseInt(CircuitBreakerConstants.PropertyKeys.MIN_CALLS_TO_EVALUATE,
                        CircuitBreakerConstants.Defaults.MIN_CALLS_TO_EVALUATE, 1))
                .setFailureRateThreshold(parseDouble(CircuitBreakerConstants.PropertyKeys.FAILURE_RATE_THRESHOLD,
                        CircuitBreakerConstants.Defaults.FAILURE_RATE_THRESHOLD, 0D, 1D))
                .setOpenDuration(parseLong(CircuitBreakerConstants.PropertyKeys.OPEN_DURATION,
                        CircuitBreakerConstants.Defaults.OPEN_DURATION, 1L))
                .setMaxInFlight(parseInt(CircuitBreakerConstants.PropertyKeys.MAX_IN_FLIGHT,
                        CircuitBreakerConstants.Defaults.MAX_IN_FLIGHT, 1))
                .build();
    }

    private static boolean parseBoolean(String key, boolean defaultValue) {

        String value = IdentityUtil.getProperty(key);
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value.trim());
    }

    private static int parseInt(String key, int defaultValue, int min) {

        String value = IdentityUtil.getProperty(key);
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        try {
            return Math.max(Integer.parseInt(value.trim()), min);
        } catch (NumberFormatException e) {
            LOG.warn("Invalid integer for property '" + key + "', using default " + defaultValue + ".", e);
            return defaultValue;
        }
    }

    private static long parseLong(String key, long defaultValue, long min) {

        String value = IdentityUtil.getProperty(key);
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        try {
            return Math.max(Long.parseLong(value.trim()), min);
        } catch (NumberFormatException e) {
            LOG.warn("Invalid long for property '" + key + "', using default " + defaultValue + ".", e);
            return defaultValue;
        }
    }

    private static double parseDouble(String key, double defaultValue, double min, double max) {

        String value = IdentityUtil.getProperty(key);
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        try {
            return Math.max(Math.min(Double.parseDouble(value.trim()), max), min);
        } catch (NumberFormatException e) {
            LOG.warn("Invalid double for property '" + key + "', using default " + defaultValue + ".", e);
            return defaultValue;
        }
    }

    private static final class Holder {

        static final StaticPolicy STATIC_POLICY = loadStaticPolicy();
        static final RuntimePolicy RUNTIME_POLICY = loadRuntimePolicy();
    }
}
