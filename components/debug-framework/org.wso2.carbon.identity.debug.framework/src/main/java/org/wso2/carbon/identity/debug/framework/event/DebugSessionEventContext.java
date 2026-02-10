/**
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

package org.wso2.carbon.identity.debug.framework.event;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Context object passed to event listeners containing session information.
 */
public class DebugSessionEventContext {

    private final String sessionId;
    private final DebugSessionLifecycleEvent event;
    private final long timestamp;
    private final Map<String, Object> properties;
    private final boolean successful;
    private final String errorMessage;

    private DebugSessionEventContext(Builder builder) {

        this.sessionId = builder.sessionId;
        this.event = builder.event;
        this.timestamp = builder.timestamp;
        this.properties = Collections.unmodifiableMap(new HashMap<>(builder.properties));
        this.successful = builder.successful;
        this.errorMessage = builder.errorMessage;
    }

    /**
     * Gets the session ID.
     *
     * @return Session ID.
     */
    public String getSessionId() {

        return sessionId;
    }

    /**
     * Gets the lifecycle event type.
     *
     * @return Lifecycle event.
     */
    public DebugSessionLifecycleEvent getEvent() {

        return event;
    }

    /**
     * Gets the event timestamp.
     *
     * @return Timestamp in milliseconds.
     */
    public long getTimestamp() {

        return timestamp;
    }

    /**
     * Gets the event properties.
     *
     * @return Unmodifiable map of properties.
     */
    public Map<String, Object> getProperties() {

        return properties;
    }

    /**
     * Gets a specific property value.
     *
     * @param key Property key.
     * @return Property value or null if not found.
     */
    public Object getProperty(String key) {

        return properties.get(key);
    }

    /**
     * Checks if the session was successful.
     *
     * @return true if successful, false otherwise.
     */
    public boolean isSuccessful() {

        return successful;
    }

    /**
     * Gets the error message if the session failed.
     *
     * @return Error message or null if successful.
     */
    public String getErrorMessage() {

        return errorMessage;
    }

    /**
     * Creates a new builder for DebugSessionEventContext.
     *
     * @return New builder instance.
     */
    public static Builder builder() {

        return new Builder();
    }

    /**
     * Builder class for DebugSessionEventContext.
     */
    public static class Builder {

        private String sessionId;
        private DebugSessionLifecycleEvent event;
        private long timestamp = System.currentTimeMillis();
        private Map<String, Object> properties = new HashMap<>();
        private boolean successful = true;
        private String errorMessage;

        public Builder sessionId(String sessionId) {

            this.sessionId = sessionId;
            return this;
        }

        public Builder event(DebugSessionLifecycleEvent event) {

            this.event = event;
            return this;
        }

        public Builder timestamp(long timestamp) {

            this.timestamp = timestamp;
            return this;
        }

        public Builder property(String key, Object value) {

            this.properties.put(key, value);
            return this;
        }

        public Builder properties(Map<String, Object> properties) {

            if (properties != null) {
                this.properties.putAll(properties);
            }
            return this;
        }

        public Builder successful(boolean successful) {

            this.successful = successful;
            return this;
        }

        public Builder errorMessage(String errorMessage) {

            this.errorMessage = errorMessage;
            return this;
        }

        public DebugSessionEventContext build() {

            if (sessionId == null || sessionId.isEmpty()) {
                throw new IllegalArgumentException("Session ID is required");
            }
            if (event == null) {
                throw new IllegalArgumentException("Event type is required");
            }
            return new DebugSessionEventContext(this);
        }
    }
}
