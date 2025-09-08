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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * DFDP Event Manager.
 * Part 5: DFDP Analysis Components - Manages DFDP event listeners and event publishing.
 */
public class DFDPEventManager {

    private static final Log log = LogFactory.getLog(DFDPEventManager.class);
    
    private static DFDPEventManager instance;
    private final List<DFDPEventListener> listeners;
    private final ExecutorService executorService;
    private boolean enabled;

    /**
     * Private constructor for singleton.
     */
    private DFDPEventManager() {
        this.listeners = new CopyOnWriteArrayList<>();
        this.executorService = Executors.newFixedThreadPool(3, r -> {
            Thread t = new Thread(r, "DFDP-EventManager-" + System.currentTimeMillis());
            t.setDaemon(true);
            return t;
        });
        this.enabled = true;
    }

    /**
     * Gets singleton instance.
     * 
     * @return DFDPEventManager instance
     */
    public static synchronized DFDPEventManager getInstance() {
        if (instance == null) {
            instance = new DFDPEventManager();
        }
        return instance;
    }

    /**
     * Registers a DFDP event listener.
     * 
     * @param listener Event listener to register
     */
    public void registerListener(DFDPEventListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
            // Sort by priority (higher priority first)
            listeners.sort((l1, l2) -> Integer.compare(l2.getPriority(), l1.getPriority()));
            
            if (log.isDebugEnabled()) {
                log.debug("DFDP event listener registered: " + listener.getListenerName() + 
                         " (Priority: " + listener.getPriority() + ")");
            }
        }
    }

    /**
     * Unregisters a DFDP event listener.
     * 
     * @param listener Event listener to unregister
     */
    public void unregisterListener(DFDPEventListener listener) {
        if (listener != null && listeners.remove(listener)) {
            if (log.isDebugEnabled()) {
                log.debug("DFDP event listener unregistered: " + listener.getListenerName());
            }
        }
    }

    /**
     * Publishes a DFDP claim event to all registered listeners.
     * 
     * @param event Event to publish
     */
    public void publishEvent(DFDPClaimEvent event) {
        if (!enabled || event == null) {
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("Publishing DFDP event: " + event.getEventType() + 
                     " for request: " + event.getRequestId());
        }

        // Asynchronously notify all listeners
        executorService.submit(() -> {
            for (DFDPEventListener listener : listeners) {
                try {
                    if (listener.isEnabled() && listener.supportsEventType(event.getEventType())) {
                        listener.onDFDPClaimEvent(event);
                    }
                } catch (Exception e) {
                    log.error("Error in DFDP event listener: " + listener.getListenerName(), e);
                }
            }
        });
    }

    /**
     * Publishes a claim retrieval event.
     * 
     * @param requestId Request ID
     * @param contextId Context ID
     * @param authenticatorName Authenticator name
     * @param identityProviderName Identity Provider name
     * @param claims Retrieved claims
     */
    public void publishClaimRetrievalEvent(String requestId, String contextId, String authenticatorName,
                                         String identityProviderName, java.util.Map<String, String> claims) {
        
        DFDPClaimEvent event = new DFDPClaimEvent("CLAIM_RETRIEVAL", requestId, claims);
        event.setContextId(contextId);
        event.setAuthenticatorName(authenticatorName);
        event.setIdentityProviderName(identityProviderName);
        event.setProcessingStage("RETRIEVAL");
        
        publishEvent(event);
    }

    /**
     * Publishes a claim mapping event.
     * 
     * @param requestId Request ID
     * @param contextId Context ID
     * @param authenticatorName Authenticator name
     * @param identityProviderName Identity Provider name
     * @param originalClaims Original claims before mapping
     * @param mappedClaims Claims after mapping
     */
    public void publishClaimMappingEvent(String requestId, String contextId, String authenticatorName,
                                       String identityProviderName, java.util.Map<String, String> originalClaims,
                                       java.util.Map<String, String> mappedClaims) {
        
        DFDPClaimEvent event = new DFDPClaimEvent("CLAIM_MAPPING", requestId, mappedClaims);
        event.setContextId(contextId);
        event.setAuthenticatorName(authenticatorName);
        event.setIdentityProviderName(identityProviderName);
        event.setProcessingStage("MAPPING");
        
        // Add original claims as additional data
        if (event.getAdditionalData() == null) {
            event.setAdditionalData(new java.util.HashMap<>());
        }
        event.getAdditionalData().put("originalClaims", originalClaims);
        
        publishEvent(event);
    }

    /**
     * Publishes a claim processing completion event.
     * 
     * @param requestId Request ID
     * @param contextId Context ID
     * @param authenticatorName Authenticator name
     * @param identityProviderName Identity Provider name
     * @param finalClaims Final processed claims
     * @param status Processing status
     */
    public void publishClaimCompletionEvent(String requestId, String contextId, String authenticatorName,
                                          String identityProviderName, java.util.Map<String, String> finalClaims,
                                          String status) {
        
        DFDPClaimEvent event = new DFDPClaimEvent("CLAIM_COMPLETION", requestId, finalClaims);
        event.setContextId(contextId);
        event.setAuthenticatorName(authenticatorName);
        event.setIdentityProviderName(identityProviderName);
        event.setProcessingStage("COMPLETION");
        
        // Add status as additional data
        if (event.getAdditionalData() == null) {
            event.setAdditionalData(new java.util.HashMap<>());
        }
        event.getAdditionalData().put("status", status);
        
        publishEvent(event);
    }

    /**
     * Gets all registered listeners.
     * 
     * @return List of listeners
     */
    public List<DFDPEventListener> getListeners() {
        return new ArrayList<>(listeners);
    }

    /**
     * Gets enabled listeners count.
     * 
     * @return Count of enabled listeners
     */
    public int getEnabledListenersCount() {
        return (int) listeners.stream().filter(DFDPEventListener::isEnabled).count();
    }

    /**
     * Enables or disables event manager.
     * 
     * @param enabled Enable status
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        
        if (log.isDebugEnabled()) {
            log.debug("DFDP Event Manager " + (enabled ? "enabled" : "disabled"));
        }
    }

    /**
     * Checks if event manager is enabled.
     * 
     * @return true if enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Shuts down the event manager.
     */
    public void shutdown() {
        enabled = false;
        listeners.clear();
        executorService.shutdown();
        
        if (log.isDebugEnabled()) {
            log.debug("DFDP Event Manager shut down");
        }
    }
}
