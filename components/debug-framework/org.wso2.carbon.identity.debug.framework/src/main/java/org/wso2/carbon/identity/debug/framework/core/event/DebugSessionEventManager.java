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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manager class for registering and firing debug session lifecycle events.
 * Thread-safe implementation using CopyOnWriteArrayList for listener
 * management.
 */
public class DebugSessionEventManager {

    private static final Log LOG = LogFactory.getLog(DebugSessionEventManager.class);

    private static final DebugSessionEventManager INSTANCE = new DebugSessionEventManager();

    private final List<DebugSessionEventListener> listeners = new CopyOnWriteArrayList<>();

    private DebugSessionEventManager() {

        // Private constructor for singleton
    }

    /**
     * Gets the singleton instance.
     *
     * @return DebugSessionEventManager instance.
     */
    public static DebugSessionEventManager getInstance() {

        return INSTANCE;
    }

    /**
     * Registers an event listener.
     *
     * @param listener The listener to register.
     */
    public void registerListener(DebugSessionEventListener listener) {

        if (listener == null) {
            LOG.warn("Attempted to register null listener");
            return;
        }

        listeners.add(listener);
        sortListeners();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Registered debug session event listener: " + listener.getListenerName());
        }
    }

    /**
     * Unregisters an event listener.
     *
     * @param listener The listener to unregister.
     */
    public void unregisterListener(DebugSessionEventListener listener) {

        if (listener == null) {
            return;
        }

        boolean removed = listeners.remove(listener);

        if (removed && LOG.isDebugEnabled()) {
            LOG.debug("Unregistered debug session event listener: " + listener.getListenerName());
        }
    }

    /**
     * Unregisters a listener by name.
     *
     * @param listenerName Name of the listener to unregister.
     */
    public void unregisterListener(String listenerName) {

        listeners.removeIf(l -> l.getListenerName().equals(listenerName));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Unregistered debug session event listener by name: " + listenerName);
        }
    }

    /**
     * Fires an event to all registered listeners.
     *
     * @param context The event context.
     */
    public void fireEvent(DebugSessionEventContext context) {

        if (context == null) {
            LOG.warn("Attempted to fire event with null context");
            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Firing debug session event: " + context.getEvent().getEventName()
                    + " for session: " + context.getSessionId());
        }

        for (DebugSessionEventListener listener : listeners) {
            if (!listener.isEnabled()) {
                continue;
            }

            try {
                invokeListenerMethod(listener, context);
            } catch (Exception e) {
                LOG.error("Error invoking listener " + listener.getListenerName()
                        + " for event " + context.getEvent().getEventName(), e);
            }
        }
    }

    /**
     * Invokes the appropriate listener method based on the event type.
     *
     * @param listener The listener to invoke.
     * @param context  The event context.
     */
    private void invokeListenerMethod(DebugSessionEventListener listener, DebugSessionEventContext context) {

        switch (context.getEvent()) {
            case ON_CREATING:
                listener.onCreating(context);
                break;
            case ON_CREATED:
                listener.onCreated(context);
                break;
            case ON_COMPLETING:
                listener.onCompleting(context);
                break;
            case ON_COMPLETION:
                listener.onCompletion(context);
                break;
            case ON_ERROR:
                listener.onError(context);
                break;
            case ON_RETRIEVED:
                listener.onRetrieved(context);
                break;
            default:
                LOG.warn("Unknown event type: " + context.getEvent());
        }
    }

    /**
     * Gets the count of registered listeners.
     *
     * @return Number of registered listeners.
     */
    public int getListenerCount() {

        return listeners.size();
    }

    /**
     * Gets a copy of all registered listeners.
     *
     * @return List of registered listeners.
     */
    public List<DebugSessionEventListener> getListeners() {

        return new ArrayList<>(listeners);
    }

    /**
     * Clears all registered listeners.
     * Use with caution - mainly for testing purposes.
     */
    public void clearListeners() {

        listeners.clear();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Cleared all debug session event listeners");
        }
    }

    /**
     * Sorts listeners by their order value.
     */
    private void sortListeners() {

        List<DebugSessionEventListener> sorted = new ArrayList<>(listeners);
        sorted.sort(Comparator.comparingInt(DebugSessionEventListener::getOrder));
        listeners.clear();
        listeners.addAll(sorted);
    }
}
