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

/**
 * DFDP Event Listener Interface.
 * Part 5: DFDP Analysis Components - Interface for DFDP claim processing event listeners.
 */
public interface DFDPEventListener {

    /**
     * Called when a DFDP claim event occurs.
     * 
     * @param event DFDP claim event
     */
    void onDFDPClaimEvent(DFDPClaimEvent event);

    /**
     * Gets the listener name.
     * 
     * @return Listener name
     */
    String getListenerName();

    /**
     * Gets the listener priority.
     * Higher values indicate higher priority.
     * 
     * @return Priority value
     */
    int getPriority();

    /**
     * Checks if this listener is enabled.
     * 
     * @return true if enabled
     */
    boolean isEnabled();

    /**
     * Checks if this listener supports the given event type.
     * 
     * @param eventType Event type to check
     * @return true if supported
     */
    boolean supportsEventType(String eventType);
}
