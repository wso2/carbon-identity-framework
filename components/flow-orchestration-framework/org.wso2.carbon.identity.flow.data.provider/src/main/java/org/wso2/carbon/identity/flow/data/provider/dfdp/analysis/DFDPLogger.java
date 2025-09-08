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

package org.wso2.carbon.identity.flow.data.provider.dfdp.analysis;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.flow.data.provider.dfdp.event.DFDPClaimEvent;
import org.wso2.carbon.identity.flow.data.provider.dfdp.event.DFDPEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DFDP Logger.
 * Part 5: DFDP Analysis Components - Event listener that captures and logs claim processing steps.
 */
public class DFDPLogger implements DFDPEventListener {

    private static final Log log = LogFactory.getLog(DFDPLogger.class);
    
    private final Map<String, List<DFDPClaimLogEntry>> requestLogs;
    private final boolean enabled;
    private final int maxLogSize;

    /**
     * Constructor.
     */
    public DFDPLogger() {
        this.requestLogs = new ConcurrentHashMap<>();
        this.enabled = true;
        this.maxLogSize = 1000; // Maximum log entries per request
    }

    @Override
    public void onDFDPClaimEvent(DFDPClaimEvent event) {
        if (!enabled || event == null) {
            return;
        }

        try {
            String requestId = event.getRequestId();
            if (requestId == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Ignoring DFDP event with null request ID");
                }
                return;
            }

            // Create log entry
            DFDPClaimLogEntry logEntry = createLogEntry(event);
            
            // Add to request logs
            requestLogs.computeIfAbsent(requestId, k -> new ArrayList<>()).add(logEntry);
            
            // Enforce max log size
            List<DFDPClaimLogEntry> logs = requestLogs.get(requestId);
            if (logs.size() > maxLogSize) {
                logs.remove(0); // Remove oldest entry
            }

            if (log.isDebugEnabled()) {
                log.debug("DFDP claim logged: " + logEntry.getSummary());
            }

        } catch (Exception e) {
            log.error("Error logging DFDP claim event", e);
        }
    }

    /**
     * Creates a log entry from a DFDP claim event.
     * 
     * @param event DFDP claim event
     * @return Log entry
     */
    private DFDPClaimLogEntry createLogEntry(DFDPClaimEvent event) {
        DFDPClaimLogEntry entry = new DFDPClaimLogEntry();
        entry.setTimestamp(event.getTimestamp());
        entry.setEventType(event.getEventType());
        entry.setRequestId(event.getRequestId());
        entry.setContextId(event.getContextId());
        entry.setAuthenticatorName(event.getAuthenticatorName());
        entry.setIdentityProviderName(event.getIdentityProviderName());
        entry.setProcessingStage(event.getProcessingStage());
        entry.setClaimCount(event.getClaimCount());
        
        // Copy claims to avoid modification
        if (event.getClaims() != null) {
            entry.setClaims(new HashMap<>(event.getClaims()));
        }
        
        // Copy additional data
        if (event.getAdditionalData() != null) {
            entry.setAdditionalData(new HashMap<>(event.getAdditionalData()));
        }
        
        return entry;
    }

    /**
     * Gets claim logs for a specific request.
     * 
     * @param requestId Request ID
     * @return List of log entries
     */
    public List<DFDPClaimLogEntry> getClaimLogs(String requestId) {
        List<DFDPClaimLogEntry> logs = requestLogs.get(requestId);
        return logs != null ? new ArrayList<>(logs) : new ArrayList<>();
    }

    /**
     * Gets all log entries for all requests.
     * 
     * @return Map of request ID to log entries
     */
    public Map<String, List<DFDPClaimLogEntry>> getAllLogs() {
        Map<String, List<DFDPClaimLogEntry>> allLogs = new HashMap<>();
        for (Map.Entry<String, List<DFDPClaimLogEntry>> entry : requestLogs.entrySet()) {
            allLogs.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return allLogs;
    }

    /**
     * Gets claims at a specific processing stage for a request.
     * 
     * @param requestId Request ID
     * @param stage Processing stage
     * @return Claims at the specified stage
     */
    public Map<String, String> getClaimsAtStage(String requestId, String stage) {
        List<DFDPClaimLogEntry> logs = getClaimLogs(requestId);
        for (DFDPClaimLogEntry entry : logs) {
            if (stage.equals(entry.getProcessingStage())) {
                return entry.getClaims();
            }
        }
        return new HashMap<>();
    }

    /**
     * Gets processing timeline for a request.
     * 
     * @param requestId Request ID
     * @return List of processing stages with timestamps
     */
    public List<String> getProcessingTimeline(String requestId) {
        List<String> timeline = new ArrayList<>();
        List<DFDPClaimLogEntry> logs = getClaimLogs(requestId);
        
        for (DFDPClaimLogEntry entry : logs) {
            String timelineEntry = String.format("[%d] %s - %s (%d claims)",
                    entry.getTimestamp(), entry.getEventType(), 
                    entry.getProcessingStage(), entry.getClaimCount());
            timeline.add(timelineEntry);
        }
        
        return timeline;
    }

    /**
     * Clears logs for a specific request.
     * 
     * @param requestId Request ID
     */
    public void clearRequestLogs(String requestId) {
        requestLogs.remove(requestId);
        
        if (log.isDebugEnabled()) {
            log.debug("Cleared DFDP logs for request: " + requestId);
        }
    }

    /**
     * Clears all logs.
     */
    public void clearAllLogs() {
        requestLogs.clear();
        
        if (log.isDebugEnabled()) {
            log.debug("Cleared all DFDP logs");
        }
    }

    /**
     * Gets total log entry count across all requests.
     * 
     * @return Total log entries
     */
    public int getTotalLogCount() {
        return requestLogs.values().stream().mapToInt(List::size).sum();
    }

    /**
     * Gets active request count.
     * 
     * @return Number of requests with logs
     */
    public int getActiveRequestCount() {
        return requestLogs.size();
    }

    @Override
    public String getListenerName() {
        return "DFDPLogger";
    }

    @Override
    public int getPriority() {
        return 100; // High priority to ensure logging happens first
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean supportsEventType(String eventType) {
        // Support all claim-related events
        return eventType != null && (
                eventType.startsWith("CLAIM_") ||
                eventType.equals("DFDP_START") ||
                eventType.equals("DFDP_END")
        );
    }

    /**
     * DFDP Claim Log Entry.
     * Represents a single logged claim processing step.
     */
    public static class DFDPClaimLogEntry {
        
        private long timestamp;
        private String eventType;
        private String requestId;
        private String contextId;
        private String authenticatorName;
        private String identityProviderName;
        private String processingStage;
        private int claimCount;
        private Map<String, String> claims;
        private Map<String, Object> additionalData;

        // Getters and setters
        
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

        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public String getContextId() {
            return contextId;
        }

        public void setContextId(String contextId) {
            this.contextId = contextId;
        }

        public String getAuthenticatorName() {
            return authenticatorName;
        }

        public void setAuthenticatorName(String authenticatorName) {
            this.authenticatorName = authenticatorName;
        }

        public String getIdentityProviderName() {
            return identityProviderName;
        }

        public void setIdentityProviderName(String identityProviderName) {
            this.identityProviderName = identityProviderName;
        }

        public String getProcessingStage() {
            return processingStage;
        }

        public void setProcessingStage(String processingStage) {
            this.processingStage = processingStage;
        }

        public int getClaimCount() {
            return claimCount;
        }

        public void setClaimCount(int claimCount) {
            this.claimCount = claimCount;
        }

        public Map<String, String> getClaims() {
            return claims;
        }

        public void setClaims(Map<String, String> claims) {
            this.claims = claims;
        }

        public Map<String, Object> getAdditionalData() {
            return additionalData;
        }

        public void setAdditionalData(Map<String, Object> additionalData) {
            this.additionalData = additionalData;
        }

        public String getSummary() {
            return String.format("%s [%s] - %s: %d claims",
                    eventType, identityProviderName, processingStage, claimCount);
        }
    }
}
