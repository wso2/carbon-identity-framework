/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.flow.execution.engine.model;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.Map;

import static org.wso2.carbon.identity.flow.execution.engine.Constants.RETAIN_OTFI;

/**
 * Retention of the one time flow id by an executor; only that executor's nodes may run while it is set.
 */
public class OTFIRetention implements Serializable {

    private static final long serialVersionUID = 6203581734092615847L;
    private static final String EXECUTOR_NAME = "executorName";

    private final String executorName;

    private OTFIRetention(Builder builder) {

        this.executorName = builder.executorName;
    }

    /**
     * Returns the name of the executor retaining the one time flow id.
     *
     * @return Executor name.
     */
    public String getExecutorName() {

        return executorName;
    }

    /**
     * Reads the retention from the flow context.
     *
     * @param context Flow execution context.
     * @return Retention, or null if none is set.
     */
    public static OTFIRetention fromContext(FlowExecutionContext context) {

        if (context == null || context.getProperties() == null) {
            return null;
        }
        Object value = context.getProperty(RETAIN_OTFI);
        if (value instanceof OTFIRetention) {
            return (OTFIRetention) value;
        }
        // Context properties are JSON round tripped by the context store, so the value comes back as a map.
        if (value instanceof Map) {
            Object executorName = ((Map<?, ?>) value).get(EXECUTOR_NAME);
            if (executorName instanceof String && StringUtils.isNotBlank((String) executorName)) {
                return new Builder().executorName((String) executorName).build();
            }
        }
        return null;
    }

    /**
     * Builder for {@link OTFIRetention}.
     */
    public static class Builder {

        private String executorName;

        /**
         * Sets the name of the retaining executor.
         *
         * @param executorName Executor name.
         * @return Builder instance.
         */
        public Builder executorName(String executorName) {

            this.executorName = executorName;
            return this;
        }

        /**
         * Builds the retention.
         *
         * @return OTFI retention.
         * @throws IllegalArgumentException If the executor name is blank.
         */
        public OTFIRetention build() {

            if (StringUtils.isBlank(executorName)) {
                throw new IllegalArgumentException("Executor name cannot be null or blank.");
            }
            return new OTFIRetention(this);
        }
    }
}
