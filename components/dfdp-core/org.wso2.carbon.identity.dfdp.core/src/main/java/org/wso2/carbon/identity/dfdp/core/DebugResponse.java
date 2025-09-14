package org.wso2.carbon.identity.dfdp.core;

import java.util.List;
import java.util.Map;

/**
 * Core model for a debug authentication response.
 */
public class DebugResponse {
    private String sessionId;
    private String targetIdp;
    private String authenticatorUsed;
    private String status;
    private AuthenticationResult authenticationResult;
    private ClaimsAnalysis claimsAnalysis;
    private List<FlowEvent> flowEvents;
    private List<DebugError> errors;
    private Map<String, Object> metadata;

    // Getters and setters for all fields...
    // Nested static classes for AuthenticationResult, ClaimsAnalysis, FlowEvent, DebugError
    public static class AuthenticationResult {
        private boolean success;
        private boolean userExists;
        private long responseTime;
        private Map<String, String> userDetails;
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public boolean isUserExists() { return userExists; }
        public void setUserExists(boolean userExists) { this.userExists = userExists; }
        public long getResponseTime() { return responseTime; }
        public void setResponseTime(long responseTime) { this.responseTime = responseTime; }
        public Map<String, String> getUserDetails() { return userDetails; }
        public void setUserDetails(Map<String, String> userDetails) { this.userDetails = userDetails; }
    }
    public static class ClaimsAnalysis {
        private Map<String, String> originalRemoteClaims;
        private Map<String, String> mappedLocalClaims;
        private Map<String, String> filteredClaims;
        private List<String> mappingErrors;
        public Map<String, String> getOriginalRemoteClaims() { return originalRemoteClaims; }
        public void setOriginalRemoteClaims(Map<String, String> originalRemoteClaims) { this.originalRemoteClaims = originalRemoteClaims; }
        public Map<String, String> getMappedLocalClaims() { return mappedLocalClaims; }
        public void setMappedLocalClaims(Map<String, String> mappedLocalClaims) { this.mappedLocalClaims = mappedLocalClaims; }
        public Map<String, String> getFilteredClaims() { return filteredClaims; }
        public void setFilteredClaims(Map<String, String> filteredClaims) { this.filteredClaims = filteredClaims; }
        public List<String> getMappingErrors() { return mappingErrors; }
        public void setMappingErrors(List<String> mappingErrors) { this.mappingErrors = mappingErrors; }
    }
    public static class FlowEvent {
        private long timestamp;
        private String eventType;
        private String step;
        private boolean success;
        private String authenticator;
        private Map<String, Object> data;
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        public String getStep() { return step; }
        public void setStep(String step) { this.step = step; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getAuthenticator() { return authenticator; }
        public void setAuthenticator(String authenticator) { this.authenticator = authenticator; }
        public Map<String, Object> getData() { return data; }
        public void setData(Map<String, Object> data) { this.data = data; }
    }
    public static class DebugError {
        private String code;
        private String message;
        private String step;
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getStep() { return step; }
        public void setStep(String step) { this.step = step; }
    }
    // Getters and setters for main fields
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getTargetIdp() { return targetIdp; }
    public void setTargetIdp(String targetIdp) { this.targetIdp = targetIdp; }
    public String getAuthenticatorUsed() { return authenticatorUsed; }
    public void setAuthenticatorUsed(String authenticatorUsed) { this.authenticatorUsed = authenticatorUsed; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public AuthenticationResult getAuthenticationResult() { return authenticationResult; }
    public void setAuthenticationResult(AuthenticationResult authenticationResult) { this.authenticationResult = authenticationResult; }
    public ClaimsAnalysis getClaimsAnalysis() { return claimsAnalysis; }
    public void setClaimsAnalysis(ClaimsAnalysis claimsAnalysis) { this.claimsAnalysis = claimsAnalysis; }
    public List<FlowEvent> getFlowEvents() { return flowEvents; }
    public void setFlowEvents(List<FlowEvent> flowEvents) { this.flowEvents = flowEvents; }
    public List<DebugError> getErrors() { return errors; }
    public void setErrors(List<DebugError> errors) { this.errors = errors; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}
