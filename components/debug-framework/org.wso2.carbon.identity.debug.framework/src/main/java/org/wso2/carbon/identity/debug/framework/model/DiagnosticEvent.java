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

package org.wso2.carbon.identity.debug.framework.model;

import org.wso2.carbon.identity.debug.framework.DebugFrameworkConstants;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a single diagnostic event in the debug timeline.
 */
public class DiagnosticEvent {

    private String stage;
    private String status;
    private String message;
    private long timestamp;
    private String errorCode;
    private String errorDescription;
    private Map<String, Object> details;

    /**
     * Create an empty diagnostic event with the current timestamp.
     */
    public DiagnosticEvent() {

        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Create a diagnostic event with all properties.
     *
     * @param stage Stage where the event was generated.
     * @param status Status of the stage.
     * @param message Human-readable event message.
     * @param timestamp Event timestamp in milliseconds.
     * @param errorCode Optional error code.
     * @param errorDescription Optional error description.
     * @param details Optional structured details.
     */
    public DiagnosticEvent(String stage, String status, String message, long timestamp,
            String errorCode, String errorDescription, Map<String, Object> details) {

        this.stage = stage;
        this.status = status;
        this.message = message;
        this.timestamp = timestamp;
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
        this.details = details;
    }

    /**
     * Create a fluent builder for diagnostic events.
     *
     * @return Diagnostic event builder.
     */
    public static Builder builder() {

        return new Builder();
    }

    /**
     * Build a {@link DiagnosticEvent} from a serialized map representation.
     *
     * @param eventMap Serialized event map.
     * @return Parsed diagnostic event, or {@code null} if input is {@code null}.
     */
    @SuppressWarnings("unchecked")
    public static DiagnosticEvent fromMap(Map<String, Object> eventMap) {

        if (eventMap == null) {
            return null;
        }

        DiagnosticEvent event = new DiagnosticEvent();
        event.setStage(asString(eventMap.get(DebugFrameworkConstants.DIAGNOSTIC_STAGE)));
        event.setStatus(asString(eventMap.get(DebugFrameworkConstants.DIAGNOSTIC_STATUS)));
        event.setMessage(asString(eventMap.get(DebugFrameworkConstants.DIAGNOSTIC_MESSAGE)));
        event.setTimestamp(asLong(eventMap.get(DebugFrameworkConstants.DIAGNOSTIC_TIMESTAMP),
                System.currentTimeMillis()));
        event.setErrorCode(asString(eventMap.get(DebugFrameworkConstants.DIAGNOSTIC_ERROR_CODE)));
        event.setErrorDescription(asString(eventMap.get(DebugFrameworkConstants.DIAGNOSTIC_ERROR_DESCRIPTION)));

        Object detailsObj = eventMap.get(DebugFrameworkConstants.DIAGNOSTIC_DETAILS);
        if (detailsObj instanceof Map) {
            event.setDetails(new LinkedHashMap<>((Map<String, Object>) detailsObj));
        }
        return event;
    }

    /**
     * Convert this event to a map representation.
     *
     * @return Serialized map containing event properties.
     */
    public Map<String, Object> toMap() {

        Map<String, Object> eventMap = new LinkedHashMap<>();
        eventMap.put(DebugFrameworkConstants.DIAGNOSTIC_STAGE, stage);
        eventMap.put(DebugFrameworkConstants.DIAGNOSTIC_STATUS, status);
        eventMap.put(DebugFrameworkConstants.DIAGNOSTIC_MESSAGE, message);
        eventMap.put(DebugFrameworkConstants.DIAGNOSTIC_TIMESTAMP, timestamp);

        if (isNotBlank(errorCode)) {
            eventMap.put(DebugFrameworkConstants.DIAGNOSTIC_ERROR_CODE, errorCode);
        }
        if (isNotBlank(errorDescription)) {
            eventMap.put(DebugFrameworkConstants.DIAGNOSTIC_ERROR_DESCRIPTION, errorDescription);
        }
        if (details != null && !details.isEmpty()) {
            eventMap.put(DebugFrameworkConstants.DIAGNOSTIC_DETAILS, details);
        }
        return eventMap;
    }

    /**
     * Get the diagnostic stage.
     *
     * @return Stage name.
     */
    public String getStage() {

        return stage;
    }

    /**
     * Set the diagnostic stage.
     *
     * @param stage Stage name.
     */
    public void setStage(String stage) {

        this.stage = stage;
    }

    /**
     * Get the diagnostic status.
     *
     * @return Status value.
     */
    public String getStatus() {

        return status;
    }

    /**
     * Set the diagnostic status.
     *
     * @param status Status value.
     */
    public void setStatus(String status) {

        this.status = status;
    }

    /**
     * Get the diagnostic message.
     *
     * @return Event message.
     */
    public String getMessage() {

        return message;
    }

    /**
     * Set the diagnostic message.
     *
     * @param message Event message.
     */
    public void setMessage(String message) {

        this.message = message;
    }

    /**
     * Get the event timestamp.
     *
     * @return Timestamp in milliseconds.
     */
    public long getTimestamp() {

        return timestamp;
    }

    /**
     * Set the event timestamp.
     *
     * @param timestamp Timestamp in milliseconds.
     */
    public void setTimestamp(long timestamp) {

        this.timestamp = timestamp;
    }

    /**
     * Get the optional error code.
     *
     * @return Error code.
     */
    public String getErrorCode() {

        return errorCode;
    }

    /**
     * Set the optional error code.
     *
     * @param errorCode Error code.
     */
    public void setErrorCode(String errorCode) {

        this.errorCode = errorCode;
    }

    /**
     * Get the optional error description.
     *
     * @return Error description.
     */
    public String getErrorDescription() {

        return errorDescription;
    }

    /**
     * Set the optional error description.
     *
     * @param errorDescription Error description.
     */
    public void setErrorDescription(String errorDescription) {

        this.errorDescription = errorDescription;
    }

    /**
     * Get structured diagnostic details.
     *
     * @return Details map.
     */
    public Map<String, Object> getDetails() {

        return details;
    }

    /**
     * Set structured diagnostic details.
     *
     * @param details Details map.
     */
    public void setDetails(Map<String, Object> details) {

        this.details = details;
    }

    private static boolean isNotBlank(String value) {

        return value != null && !value.trim().isEmpty();
    }

    private static String asString(Object value) {

        return value != null ? String.valueOf(value) : null;
    }

    private static long asLong(Object value, long fallback) {

        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        if (value != null) {
            try {
                return Long.parseLong(String.valueOf(value));
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    /**
     * Builder for diagnostic events.
     */
    public static class Builder {

        private String stage;
        private String status;
        private String message;
        private long timestamp = System.currentTimeMillis();
        private String errorCode;
        private String errorDescription;
        private Map<String, Object> details;

        /**
         * Set the stage value.
         *
         * @param stage Stage name.
         * @return This builder instance.
         */
        public Builder stage(String stage) {

            this.stage = stage;
            return this;
        }

        /**
         * Set the status value.
         *
         * @param status Status value.
         * @return This builder instance.
         */
        public Builder status(String status) {

            this.status = status;
            return this;
        }

        /**
         * Set the message value.
         *
         * @param message Event message.
         * @return This builder instance.
         */
        public Builder message(String message) {

            this.message = message;
            return this;
        }

        /**
         * Set the timestamp value.
         *
         * @param timestamp Timestamp in milliseconds.
         * @return This builder instance.
         */
        public Builder timestamp(long timestamp) {

            this.timestamp = timestamp;
            return this;
        }

        /**
         * Set the error code value.
         *
         * @param errorCode Error code.
         * @return This builder instance.
         */
        public Builder errorCode(String errorCode) {

            this.errorCode = errorCode;
            return this;
        }

        /**
         * Set the error description value.
         *
         * @param errorDescription Error description.
         * @return This builder instance.
         */
        public Builder errorDescription(String errorDescription) {

            this.errorDescription = errorDescription;
            return this;
        }

        /**
         * Set structured details.
         *
         * @param details Structured details map.
         * @return This builder instance.
         */
        public Builder details(Map<String, Object> details) {

            this.details = details;
            return this;
        }

        /**
         * Build the diagnostic event.
         *
         * @return Built diagnostic event.
         */
        public DiagnosticEvent build() {

            return new DiagnosticEvent(stage, status, message, timestamp, errorCode,
                    errorDescription, details);
        }
    }
}
