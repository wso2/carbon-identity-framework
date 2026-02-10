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

/**
 * Enum representing debug session lifecycle events.
 * These events are fired at different stages of the debug session lifecycle.
 */
public enum DebugSessionLifecycleEvent {

    /**
     * Fired when a debug session is being created.
     */
    ON_CREATING("onCreate"),

    /**
     * Fired when a debug session has been successfully created.
     */
    ON_CREATED("onCreated"),

    /**
     * Fired when a debug session is about to complete.
     */
    ON_COMPLETING("onCompleting"),

    /**
     * Fired when a debug session has completed (success or failure).
     */
    ON_COMPLETION("onCompletion"),

    /**
     * Fired when a debug session fails with an error.
     */
    ON_ERROR("onError"),

    /**
     * Fired when a debug session result is retrieved/viewed.
     */
    ON_RETRIEVED("onRetrieved");

    private final String eventName;

    DebugSessionLifecycleEvent(String eventName) {

        this.eventName = eventName;
    }

    /**
     * Gets the event name.
     *
     * @return Event name string.
     */
    public String getEventName() {

        return eventName;
    }
}
