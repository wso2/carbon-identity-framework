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

package org.wso2.carbon.identity.flow.data.provider.dfdp.event;

import java.util.Map;

/**
 * DFDP Claim Processing Event.
 * Part 5: DFDP Analysis Components - Base event for DFDP claim processing monitoring.
 */
public class DFDPClaimEvent {

    private String eventType;
    private String requestId;
    private String contextId;
    private String authenticatorName;
    private String identityProviderName;
    private Map<String, String> claims;
    private String processingStage;
    private long timestamp;
    private String userId;
    private Map<String, Object> additionalData;

    /**
     * Constructor.
     */
    public DFDPClaimEvent() {
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Constructor with basic parameters.
     * 
     * @param eventType Event type
     * @param requestId Request ID
     * @param claims Claims data
     */
    public DFDPClaimEvent(String eventType, String requestId, Map<String, String> claims) {
        this();
        this.eventType = eventType;
        this.requestId = requestId;
        this.claims = claims;
    }

    /**
     * Gets event type.
     * 
     * @return Event type
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * Sets event type.
     * 
     * @param eventType Event type
     */
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    /**
     * Gets request ID.
     * 
     * @return Request ID
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * Sets request ID.
     * 
     * @param requestId Request ID
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /**
     * Gets context ID.
     * 
     * @return Context ID
     */
    public String getContextId() {
        return contextId;
    }

    /**
     * Sets context ID.
     * 
     * @param contextId Context ID
     */
    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    /**
     * Gets authenticator name.
     * 
     * @return Authenticator name
     */
    public String getAuthenticatorName() {
        return authenticatorName;
    }

    /**
     * Sets authenticator name.
     * 
     * @param authenticatorName Authenticator name
     */
    public void setAuthenticatorName(String authenticatorName) {
        this.authenticatorName = authenticatorName;
    }

    /**
     * Gets Identity Provider name.
     * 
     * @return Identity Provider name
     */
    public String getIdentityProviderName() {
        return identityProviderName;
    }

    /**
     * Sets Identity Provider name.
     * 
     * @param identityProviderName Identity Provider name
     */
    public void setIdentityProviderName(String identityProviderName) {
        this.identityProviderName = identityProviderName;
    }

    /**
     * Gets claims.
     * 
     * @return Claims map
     */
    public Map<String, String> getClaims() {
        return claims;
    }

    /**
     * Sets claims.
     * 
     * @param claims Claims map
     */
    public void setClaims(Map<String, String> claims) {
        this.claims = claims;
    }

    /**
     * Gets processing stage.
     * 
     * @return Processing stage
     */
    public String getProcessingStage() {
        return processingStage;
    }

    /**
     * Sets processing stage.
     * 
     * @param processingStage Processing stage
     */
    public void setProcessingStage(String processingStage) {
        this.processingStage = processingStage;
    }

    /**
     * Gets timestamp.
     * 
     * @return Timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Sets timestamp.
     * 
     * @param timestamp Timestamp
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Gets user ID.
     * 
     * @return User ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets user ID.
     * 
     * @param userId User ID
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Gets additional data.
     * 
     * @return Additional data map
     */
    public Map<String, Object> getAdditionalData() {
        return additionalData;
    }

    /**
     * Sets additional data.
     * 
     * @param additionalData Additional data map
     */
    public void setAdditionalData(Map<String, Object> additionalData) {
        this.additionalData = additionalData;
    }

    /**
     * Gets claim count.
     * 
     * @return Number of claims
     */
    public int getClaimCount() {
        return claims != null ? claims.size() : 0;
    }

    /**
     * Checks if event has claims.
     * 
     * @return true if has claims
     */
    public boolean hasClaims() {
        return claims != null && !claims.isEmpty();
    }

    /**
     * Gets event summary.
     * 
     * @return Event summary string
     */
    public String getEventSummary() {
        return String.format("%s - %s [%s] - %d claims at stage '%s'",
                eventType, requestId, identityProviderName, getClaimCount(), processingStage);
    }

    @Override
    public String toString() {
        return "DFDPClaimEvent{" +
                "eventType='" + eventType + '\'' +
                ", requestId='" + requestId + '\'' +
                ", contextId='" + contextId + '\'' +
                ", authenticatorName='" + authenticatorName + '\'' +
                ", identityProviderName='" + identityProviderName + '\'' +
                ", processingStage='" + processingStage + '\'' +
                ", claimCount=" + getClaimCount() +
                ", timestamp=" + timestamp +
                '}';
    }
}
