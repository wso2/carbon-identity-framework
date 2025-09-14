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

package org.wso2.carbon.identity.dfdp.core.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.bean.context.MessageContext;
import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.identity.event.event.Event;
import org.wso2.carbon.identity.event.handler.AbstractEventHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DFDP Event Listener for capturing authentication flow events and claim processing.
 * This listener replaces direct logging with event-driven architecture for claim analysis.
 */
public class DFDPEventListener extends AbstractEventHandler {

    private static final Log LOG = LogFactory.getLog(DFDPEventListener.class);

    // Event types for DFDP debugging
    public static final String DFDP_AUTHENTICATION_STARTED = "DFDP_AUTHENTICATION_STARTED";
    public static final String DFDP_CLAIM_MAPPING = "DFDP_CLAIM_MAPPING";
    public static final String DFDP_AUTHENTICATION_COMPLETED = "DFDP_AUTHENTICATION_COMPLETED";
    public static final String DFDP_ERROR_OCCURRED = "DFDP_ERROR_OCCURRED";

    // Session-based event storage for debugging sessions
    private static final Map<String, List<DFDPEvent>> sessionEvents = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, Object>> sessionData = new ConcurrentHashMap<>();

    /**
     * Initialize a new debug session for event capture.
     * 
     * @param sessionId Debug session ID
     */
    public void initializeSession(String sessionId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Initializing DFDP event capture for session: " + sessionId);
        }
        sessionEvents.put(sessionId, new ArrayList<>());
        sessionData.put(sessionId, new ConcurrentHashMap<>());
    }

    /**
     * Capture claim mapping events during authentication flow.
     * 
     * @param sessionId Debug session ID
     * @param eventType Type of claim event (ORIGINAL_CLAIMS, MAPPED_CLAIMS, etc.)
     * @param claims Claims data to capture
     */
    public void captureClaimMappingEvent(String sessionId, String eventType, Map<String, String> claims) {
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Capturing claim mapping event: " + eventType + " for session: " + sessionId + 
                         " with " + claims.size() + " claims");
            }

            DFDPEvent event = new DFDPEvent();
            event.setSessionId(sessionId);
            event.setTimestamp(System.currentTimeMillis());
            event.setEventType(DFDP_CLAIM_MAPPING);
            event.setSubType(eventType);
            event.setData(new ConcurrentHashMap<>(claims));
            event.setSuccess(true);

            // Store event for this session
            List<DFDPEvent> events = sessionEvents.get(sessionId);
            if (events != null) {
                events.add(event);
            }

            // Store claims data for later retrieval
            Map<String, Object> data = sessionData.get(sessionId);
            if (data != null) {
                data.put(eventType.toLowerCase(Locale.ENGLISH), claims);
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Successfully captured claim mapping event: " + eventType + " for session: " + sessionId);
            }

        } catch (Exception e) {
            LOG.error("Error capturing claim mapping event for session: " + sessionId, e);
        }
    }

    /**
     * Capture authentication flow events.
     * 
     * @param sessionId Debug session ID
     * @param eventType Event type
     * @param step Authentication step
     * @param authenticator Authenticator name
     * @param success Success status
     * @param data Additional event data
     */
    public void captureAuthenticationEvent(String sessionId, String eventType, String step, 
                                         String authenticator, boolean success, Map<String, Object> data) {
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Capturing authentication event: " + eventType + " for session: " + sessionId);
            }

            DFDPEvent event = new DFDPEvent();
            event.setSessionId(sessionId);
            event.setTimestamp(System.currentTimeMillis());
            event.setEventType(eventType);
            event.setStep(step);
            event.setAuthenticator(authenticator);
            event.setSuccess(success);
            event.setData(data != null ? new ConcurrentHashMap<>(data) : new ConcurrentHashMap<>());

            // Store event for this session
            List<DFDPEvent> events = sessionEvents.get(sessionId);
            if (events != null) {
                events.add(event);
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Successfully captured authentication event: " + eventType + " for session: " + sessionId);
            }

        } catch (Exception e) {
            LOG.error("Error capturing authentication event for session: " + sessionId, e);
        }
    }

    /**
     * Get all events for a debug session.
     * 
     * @param sessionId Debug session ID
     * @return List of DFDP events for the session
     */
    public List<DFDPEvent> getSessionEvents(String sessionId) {
        return sessionEvents.get(sessionId);
    }

    /**
     * Get session data captured by event listeners.
     * 
     * @param sessionId Debug session ID
     * @return Map of session data
     */
    public Map<String, Object> getSessionData(String sessionId) {
        return sessionData.get(sessionId);
    }

    /**
     * Clear session data to prevent memory leaks.
     * 
     * @param sessionId Debug session ID
     */
    public void clearSession(String sessionId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Clearing DFDP session data for session: " + sessionId);
        }
        sessionEvents.remove(sessionId);
        sessionData.remove(sessionId);
    }

    @Override
    public void handleEvent(Event event) throws IdentityEventException {
        // Handle WSO2 IS framework events if needed
        if (LOG.isDebugEnabled()) {
            LOG.debug("Received WSO2 IS framework event: " + event.getEventName());
        }

        // Process authentication framework events for DFDP debugging
        String eventName = event.getEventName();
        Map<String, Object> eventProperties = event.getEventProperties();

        // Check if this is a DFDP-related event
        if (eventProperties.containsKey("dfdp.session.id")) {
            String sessionId = (String) eventProperties.get("dfdp.session.id");
            processFrameworkEvent(sessionId, eventName, eventProperties);
        }
    }

    /**
     * Process WSO2 IS authentication framework events for DFDP debugging.
     * 
     * @param sessionId DFDP session ID
     * @param eventName Framework event name
     * @param eventProperties Event properties
     */
    private void processFrameworkEvent(String sessionId, String eventName, Map<String, Object> eventProperties) {
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Processing framework event: " + eventName + " for DFDP session: " + sessionId);
            }

            // Map framework events to DFDP events
            String dfdpEventType = mapFrameworkEventToDFDPEvent(eventName);
            if (dfdpEventType != null) {
                captureAuthenticationEvent(sessionId, dfdpEventType, 
                    (String) eventProperties.get("step"),
                    (String) eventProperties.get("authenticator"),
                    Boolean.TRUE.equals(eventProperties.get("success")),
                    eventProperties);
            }

        } catch (Exception e) {
            LOG.error("Error processing framework event for DFDP session: " + sessionId, e);
        }
    }

    /**
     * Map WSO2 IS framework event names to DFDP event types.
     * 
     * @param frameworkEventName Framework event name
     * @return DFDP event type or null if not mapped
     */
    private String mapFrameworkEventToDFDPEvent(String frameworkEventName) {
        switch (frameworkEventName) {
            case "PRE_AUTHENTICATION":
                return DFDP_AUTHENTICATION_STARTED;
            case "POST_AUTHENTICATION":
                return DFDP_AUTHENTICATION_COMPLETED;
            case "AUTHENTICATION_STEP_SUCCESS":
                return "DFDP_STEP_SUCCESS";
            case "AUTHENTICATION_STEP_FAILURE":
                return "DFDP_STEP_FAILURE";
            default:
                return null;
        }
    }

    @Override
    public String getName() {
        return "DFDPEventListener";
    }

    @Override
    @SuppressWarnings("rawtypes")
    public int getPriority(MessageContext messageContext) {
        return 100; // High priority for debugging
    }

    /**
     * DFDP Event data structure for storing debug events.
     */
    public static class DFDPEvent {
        private String sessionId;
        private long timestamp;
        private String eventType;
        private String subType;
        private String step;
        private String authenticator;
        private boolean success;
        private Map<String, Object> data;

        // Getters and setters
        public String getSessionId() {
            return sessionId;
        }
        
        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
        
        public String getEventType() {
            return eventType;
        }
        
        public void setEventType(String eventType) {
            this.eventType = eventType;
        }
        
        public String getSubType() {
            return subType;
        }
        
        public void setSubType(String subType) {
            this.subType = subType;
        }
        
        public String getStep() {
            return step;
        }
        
        public void setStep(String step) {
            this.step = step;
        }
        
        public String getAuthenticator() {
            return authenticator;
        }
        
        public void setAuthenticator(String authenticator) {
            this.authenticator = authenticator;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public void setSuccess(boolean success) {
            this.success = success;
        }
        
        public Map<String, Object> getData() {
            return data;
        }
        
        public void setData(Map<String, Object> data) {
            this.data = data;
        }
    }
}
