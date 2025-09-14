package org.wso2.carbon.identity.dfdp.core;

import java.util.Map;

/**
 * Core model for a debug authentication request.
 */
public class DebugRequest {
    private String targetIdp;
    private String targetAuthenticator;
    private String testUser;
    private Map<String, String> testClaims;
    private Boolean enableEventCapture;
    private String debugMode;

    public String getTargetIdp() { return targetIdp; }
    public void setTargetIdp(String targetIdp) { this.targetIdp = targetIdp; }
    public String getTargetAuthenticator() { return targetAuthenticator; }
    public void setTargetAuthenticator(String targetAuthenticator) { this.targetAuthenticator = targetAuthenticator; }
    public String getTestUser() { return testUser; }
    public void setTestUser(String testUser) { this.testUser = testUser; }
    public Map<String, String> getTestClaims() { return testClaims; }
    public void setTestClaims(Map<String, String> testClaims) { this.testClaims = testClaims; }
    public Boolean getEnableEventCapture() { return enableEventCapture; }
    public void setEnableEventCapture(Boolean enableEventCapture) { this.enableEventCapture = enableEventCapture; }
    public String getDebugMode() { return debugMode; }
    public void setDebugMode(String debugMode) { this.debugMode = debugMode; }
}
