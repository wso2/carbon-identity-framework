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

package org.wso2.carbon.identity.debug.framework.core.event;

/**
 * Interface for debug session event listeners.
 * Implementations can subscribe to debug session lifecycle events.
 */
public interface DebugSessionEventListener {

    /**
     * Called when a debug session is being created.
     * This is called before the session is persisted.
     *
     * @param context Event context containing session information.
     */
    default void onCreating(DebugSessionEventContext context) {

        // Default: no-op
    }

    /**
     * Called after a debug session has been successfully created.
     *
     * @param context Event context containing session information.
     */
    default void onCreated(DebugSessionEventContext context) {

        // Default: no-op
    }

    /**
     * Called when a debug session is about to complete.
     *
     * @param context Event context containing session information.
     */
    default void onCompleting(DebugSessionEventContext context) {

        // Default: no-op
    }

    /**
     * Called when a debug session has completed.
     * This is called after all processing is done.
     *
     * @param context Event context containing session information.
     */
    default void onCompletion(DebugSessionEventContext context) {

        // Default: no-op
    }

    /**
     * Called when a debug session encounters an error.
     *
     * @param context Event context containing error information.
     */
    default void onError(DebugSessionEventContext context) {

        // Default: no-op
    }

    /**
     * Called when a debug session result is retrieved/viewed.
     *
     * @param context Event context containing session information.
     */
    default void onRetrieved(DebugSessionEventContext context) {
        // Default: no-op
    }

    /**
     * Gets the name of this listener for logging and identification purposes.
     *
     * @return Listener name.
     */
    String getListenerName();

    /**
     * Gets the order/priority of this listener.
     * Lower values execute first.
     *
     * @return Order value (default: 100).
     */
    default int getOrder() {

        return 100;
    }

    /**
     * Checks if this listener is enabled.
     *
     * @return true if enabled, false otherwise.
     */
    default boolean isEnabled() {

        return true;
    }
}
