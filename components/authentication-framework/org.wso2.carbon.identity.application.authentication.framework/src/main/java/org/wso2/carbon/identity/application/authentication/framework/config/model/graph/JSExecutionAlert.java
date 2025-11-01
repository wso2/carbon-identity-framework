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

/**
 * Represents an alert generated when a script execution violates constraints.
 */
public class JSExecutionAlert {

    private final String tenantDomain;
    private final String serviceProvider;
    private final AlertType alertType;
    private final long resourceValue;
    private final long threshold;
    private final long timestamp;
    private final String threadName;

    /**
     * Alert type enumeration.
     */
    public enum AlertType {

        TIMEOUT_EXCEEDED,
        MEMORY_LIMIT_EXCEEDED
    }

    /**
     * Constructor for JSExecutionAlert.
     *
     * @param tenantDomain    Tenant domain where the alert occurred.
     * @param serviceProvider Service provider name.
     * @param alertType       Type of alert.
     * @param resourceValue   Actual resource value that triggered the alert.
     * @param threshold       Threshold value that was exceeded.
     * @param threadName      Name of the thread where alert occurred.
     */
    public JSExecutionAlert(String tenantDomain, String serviceProvider, AlertType alertType,
                            long resourceValue, long threshold, String threadName) {

        this.tenantDomain = tenantDomain;
        this.serviceProvider = serviceProvider;
        this.alertType = alertType;
        this.resourceValue = resourceValue;
        this.threshold = threshold;
        this.threadName = threadName;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Get tenant domain.
     *
     * @return Tenant domain.
     */
    public String getTenantDomain() {

        return tenantDomain;
    }

    /**
     * Get service provider.
     *
     * @return Service provider name.
     */
    public String getServiceProvider() {

        return serviceProvider;
    }

    /**
     * Get alert type.
     *
     * @return Alert type.
     */
    public AlertType getAlertType() {

        return alertType;
    }

    /**
     * Get resource value.
     *
     * @return Resource value that triggered the alert.
     */
    public long getResourceValue() {

        return resourceValue;
    }

    /**
     * Get threshold.
     *
     * @return Threshold value.
     */
    public long getThreshold() {

        return threshold;
    }

    /**
     * Get timestamp.
     *
     * @return Timestamp when alert was created.
     */
    public long getTimestamp() {

        return timestamp;
    }

    /**
     * Get thread name.
     *
     * @return Thread name where alert occurred.
     */
    public String getThreadName() {

        return threadName;
    }

    @Override
    public String toString() {

        return "JSExecutionAlert{" +
                "tenantDomain='" + tenantDomain + '\'' +
                ", serviceProvider='" + serviceProvider + '\'' +
                ", alertType=" + alertType +
                ", resourceValue=" + resourceValue +
                ", threshold=" + threshold +
                ", timestamp=" + timestamp +
                ", threadName='" + threadName + '\'' +
                '}';
    }
}

